package autismclient.ai;

import autismclient.api.ai.AiOperation;
import autismclient.compat263.Compat263;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.util.List;
import java.util.Locale;
import java.util.Map.Entry;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.inventory.AnvilMenu;
import net.minecraft.world.inventory.ContainerInput;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

final class AiAnvil {
   private static final int load = 80;
   private static final int read = 100;
   private static final int write = 40;
   private static final int update = 40;
   private static final int sync = 40;
   static final int MAX_LEVEL_COST = 39;

   private AiAnvil() {
   }

   static AiOperation create(JsonObject args) {
      Minecraft client = Minecraft.getInstance();
      if (client.player != null && client.level != null) {
         final AiAnvil.AnvilMode mode = parseMode(AiJson.getString(args, "mode", "repair"));
         final String leftId = requireItemId(AiJson.getString(args, "left", ""), "left");
         final String rightId = requireItemId(AiJson.getString(args, "right", ""), "right");
         final int timeoutSeconds = Math.max(10, Math.min(3600, AiJson.getInt(args, "timeoutSeconds", 300)));
         return new AiOperation() {
            final Minecraft client = Minecraft.getInstance();
            final long deadlineNanos = System.nanoTime() + timeoutSeconds * 1000000000L;
            final autismclient.ai.engine.AiAim aim = new autismclient.ai.engine.AiAim();
            final autismclient.ai.engine.AiPlacer placer = new autismclient.ai.engine.AiPlacer();
            final int startLevel = this.client.player.experienceLevel;
            BlockPos anvilPos;
            int aimTicks;
            int placeTicks;
            int searchCooldown;
            int update;
            int outputWaitTicks;
            int confirmWaitTicks;
            long takeRevision = -1L;
            boolean placing;
            boolean done;
            ItemStack expectedOutput = ItemStack.EMPTY;
            int cost = -1;
            int menuId = -1;
            int outputCountBefore;
            String pickedItem = "";
            int pickedSlot = -1;
            boolean picked;

            @Override
            public AiOperation.Result tick() {
               if (!this.done && this.client.player != null && this.client.level != null) {
                  if (System.nanoTime() > this.deadlineNanos) {
                     return this.finish(AiOperation.Status.TIMED_OUT, "Anvil deadline reached");
                  } else if (this.client.player.getHealth() <= 4.0F && !AiGuard.isLowHealthOverrideActive()) {
                     return this.fail(AiFailure.LOW_HEALTH, AiGuard.formatLowHealthAbort("anvil work", this.client.player.getHealth(), this.client.player.getMaxHealth()));
                  } else if (!(this.client.player.containerMenu instanceof AnvilMenu menu)) {
                     AiOperation.Result open = this.ensureAnvil();
                     return open != null ? open : this.running("Opening the anvil");
                  } else {
                     if (this.menuId != menu.containerId) {
                        for (int slot = 0; slot <= 2; slot++) {
                           if (!menu.getSlot(slot).getItem().isEmpty()) {
                              return this.fail(
                                 AiFailure.IMPOSSIBLE_STATE,
                                 "The anvil is in use: slot "
                                    + slot
                                    + " holds "
                                    + BuiltInRegistries.ITEM.getKey(menu.getSlot(slot).getItem().getItem())
                                    + "; empty it first"
                              );
                           }
                        }

                        this.menuId = menu.containerId;
                     }

                     AiOperation.Result missing = this.init(menu);
                     if (missing != null) {
                        return missing;
                     }

                     if (this.takeRevision >= 0L) {
                        if (AiServerEvidence.containerRevision(menu.containerId) > this.takeRevision) {
                           if (menu.getSlot(2).getItem().isEmpty()
                              && AiGameState.countInventoryItem(BuiltInRegistries.ITEM.getKey(this.expectedOutput.getItem()).toString()) > this.outputCountBefore) {
                              return this.finish(AiOperation.Status.SUCCEEDED, "Anvil produced " + BuiltInRegistries.ITEM.getKey(this.expectedOutput.getItem()));
                           } else {
                              return ++this.confirmWaitTicks > 40
                                 ? this.finish(
                                    AiOperation.Status.UNCERTAIN,
                                    menu.getSlot(2).getItem().isEmpty()
                                       ? "The result left the anvil but was not observed in the inventory; reobserve before repeating"
                                       : "The result is still in the anvil (inventory full?); free space and take it before repeating"
                                 )
                                 : this.running("Verifying the anvil result reached the inventory");
                           }
                        } else {
                           return ++this.confirmWaitTicks > 40
                              ? this.finish(AiOperation.Status.UNCERTAIN, "The result was not confirmed by the server; reobserve before repeating")
                              : this.running("Waiting for the server to apply the anvil result");
                        }
                     } else {
                        if (menu.getSlot(0).getItem().isEmpty()) {
                           this.moveItemToSlot(menu, leftId, 0);
                           return this.running("Placing the left item");
                        }

                        if (menu.getSlot(1).getItem().isEmpty()) {
                           this.moveItemToSlot(menu, rightId, 1);
                           return this.running("Placing the right item");
                        }

                        ItemStack out = menu.getSlot(2).getItem();
                        int cost = menu.getCost();
                        if (out.isEmpty() && this.outputWaitTicks++ < 40) {
                           return this.running("Waiting for the anvil result");
                        }

                        String failure = AiAnvil.refusalReason(out.isEmpty(), cost, this.client.player.experienceLevel);
                        if (failure != null) {
                           this.cost = cost;
                           AiFailure var10001 = AiFailure.IMPOSSIBLE_STATE;
                           String var10002;
                           switch (failure) {
                              case "too_expensive":
                                 var10002 = "Too expensive: level cost " + cost + " exceeds 39";
                                 break;
                              case "insufficient_levels":
                                 var10002 = "Insufficient levels: needs "
                                    + cost
                                    + ", have "
                                    + this.client.player.experienceLevel
                                    + " (deficit "
                                    + (cost - this.client.player.experienceLevel)
                                    + ")";
                                 break;
                              default:
                                 var10002 = "The combination produces no result (incompatible)";
                           }

                           return this.fail(var10001, var10002);
                        } else {
                           this.cost = cost;
                           this.expectedOutput = out.copy();
                           this.outputCountBefore = AiGameState.countInventoryItem(BuiltInRegistries.ITEM.getKey(this.expectedOutput.getItem()).toString());
                           this.takeRevision = AiServerEvidence.containerRevision(menu.containerId);
                           AiContainerTransactions.sendPredictedClick(menu.containerId, 2, 0, ContainerInput.QUICK_MOVE);
                           return this.running("Taking the result");
                        }
                     }
                  }
               } else {
                  return this.finish(AiOperation.Status.CANCELLED, "Anvil work cancelled; committed anvil contents stay");
               }
            }

            AiOperation.Result init(AnvilMenu menu) {
               if (this.update <= 0 && menu.getSlot(2).getItem().isEmpty()) {
                  JsonObject missing = new JsonObject();
                  if (menu.getSlot(0).getItem().isEmpty() && AiGameState.countMainInventoryItem(leftId) < 1) {
                     missing.addProperty(leftId, 1);
                  }

                  if (menu.getSlot(1).getItem().isEmpty() && AiGameState.countMainInventoryItem(rightId) < 1) {
                     missing.addProperty(rightId, 1);
                  }

                  return missing.size() > 0 ? this.fail(AiFailure.MISSING_MATERIALS, "Missing anvil materials", AiJson.objectOf("missing", missing)) : null;
               } else {
                  return null;
               }
            }

            void moveItemToSlot(AnvilMenu menu, String itemId, int targetSlot) {
               int slot = this.findItemSlot(itemId, true);
               if (slot < 0) {
                  slot = this.findItemSlot(itemId, false);
               }

               if (slot >= 0) {
                  this.recordPick(itemId, slot);
                  int menuSlot = this.menuSlot(slot);
                  AiContainerTransactions.sendPredictedClick(menu.containerId, menuSlot, 0, ContainerInput.PICKUP);
                  AiContainerTransactions.sendPredictedClick(menu.containerId, targetSlot, 0, ContainerInput.PICKUP);
                  this.update++;
               }
            }

            int findItemSlot(String itemId, boolean plainOnly) {
               for (int slot = 0; slot < 36; slot++) {
                  ItemStack stack = this.client.player.getInventory().getItem(slot);
                  if (!stack.isEmpty() && BuiltInRegistries.ITEM.getKey(stack.getItem()).toString().equals(itemId) && (!plainOnly || AiItems.isPlainItem(stack))) {
                     return slot;
                  }
               }

               return -1;
            }

            void recordPick(String itemId, int slot) {
               int sameId = 0;

               for (int i = 0; i < 36; i++) {
                  ItemStack stack = this.client.player.getInventory().getItem(i);
                  if (!stack.isEmpty() && BuiltInRegistries.ITEM.getKey(stack.getItem()).toString().equals(itemId)) {
                     sameId++;
                  }
               }

               if (sameId > 1) {
                  this.pickedItem = itemId;
                  this.pickedSlot = slot;
                  this.picked = true;
               }
            }

            int menuSlot(int inventorySlot) {
               return inventorySlot < 9 ? inventorySlot + 30 : inventorySlot - 9 + 3;
            }

            AiOperation.Result ensureAnvil() {
               if (this.anvilPos == null && this.searchCooldown-- <= 0) {
                  this.anvilPos = this.findAnvil();
                  this.searchCooldown = 40;
               }

               if (this.anvilPos == null && this.searchCooldown < 40 && !this.placing) {
                  return this.running("Looking for an anvil");
               }

               if (this.anvilPos == null) {
                  if (!this.placing) {
                     if (AiGameState.countInventoryItem("minecraft:anvil") <= 0) {
                        JsonObject missing = new JsonObject();
                        missing.addProperty("minecraft:anvil", 1);
                        return this.fail(AiFailure.MISSING_STATION, "No anvil in reach or in the bag", AiJson.objectOf("missing", missing));
                     }

                     BlockPos spot = AiCraft.findPlacementSpot();
                     if (spot == null) {
                        return this.finish(AiOperation.Status.FAILED, "No free supported spot next to the player for an anvil");
                     }

                     this.placer.start(spot, BuiltInRegistries.ITEM.getOptional(Identifier.parse("minecraft:anvil")).orElseThrow());
                     this.placing = true;
                     this.placeTicks = 0;
                  }

                  autismclient.ai.engine.AiPlacer.PlaceStatus status = this.placer.tick();
                  if (status == autismclient.ai.engine.AiPlacer.PlaceStatus.PLACED) {
                     this.anvilPos = this.placer.targetPos();
                     this.placing = false;
                     return this.running("Anvil placed");
                  }

                  if (status != autismclient.ai.engine.AiPlacer.PlaceStatus.FAILED && ++this.placeTicks <= 100) {
                     return this.running("Placing an anvil");
                  }

                  this.placing = false;
                  return this.finish(AiOperation.Status.FAILED, "Could not place an anvil: " + this.placer.failureReason());
               } else {
                  double reach = Math.max(4.5, this.client.player.blockInteractionRange());
                  if (this.client.player.getEyePosition().distanceToSqr(Vec3.atCenterOf(this.anvilPos)) > reach * reach) {
                     return this.finish(AiOperation.Status.FAILED, "The anvil fell out of reach");
                  }

                  this.aim.aimAt(Vec3.atCenterOf(this.anvilPos));
                  this.aim.tick();
                  if (++this.aimTicks > 80) {
                     return this.finish(AiOperation.Status.FAILED, "Could not open the anvil");
                  }

                  if (!autismclient.ai.engine.AiAim.isLookingAtBlock(this.anvilPos)) {
                     return this.running("Turning toward the anvil");
                  }

                  BlockHitResult hit = autismclient.ai.engine.AiAim.raycastLookBlock();
                  if (hit == null) {
                     return this.running("Turning toward the anvil");
                  }

                  this.client.gameMode.useItemOn(this.client.player, InteractionHand.MAIN_HAND, hit);
                  Compat263.swing(this.client.player, InteractionHand.MAIN_HAND);
                  return null;
               }
            }

            BlockPos findAnvil() {
               Block wanted = AiCraft.requireBlock("minecraft:anvil");
               BlockPos origin = this.client.player.blockPosition();
               BlockPos best = null;
               double bestDistance = 1.7976931348623157E308;
               int radius = 16;

               for (BlockPos pos : BlockPos.betweenClosed(origin.offset(-radius, -8, -radius), origin.offset(radius, 8, radius))) {
                  if (AiTerrain.isLoaded(this.client.level, pos) && this.client.level.getBlockState(pos).getBlock() == wanted) {
                     double distance = this.client.player.distanceToSqr(Vec3.atCenterOf(pos));
                     if (distance < bestDistance) {
                        bestDistance = distance;
                        best = pos.immutable();
                     }
                  }
               }

               return best;
            }

            JsonObject statusData() {
               JsonObject out = AiJson.objectOf(
                  "mode",
                  mode.name().toLowerCase(Locale.ROOT),
                  "left",
                  leftId,
                  "right",
                  rightId,
                  "levelsSpent",
                  this.startLevel - (this.client.player == null ? this.startLevel : this.client.player.experienceLevel),
                  "stationAt",
                  this.anvilPos == null ? null : this.anvilPos.toShortString()
               );
               if (this.cost >= 0) {
                  out.addProperty("levelCost", this.cost);
               }

               if (this.picked) {
                  out.add("picked", AiJson.objectOf("item", this.pickedItem, "slot", this.pickedSlot));
               }

               if (!this.expectedOutput.isEmpty()) {
                  out.addProperty("result", BuiltInRegistries.ITEM.getKey(this.expectedOutput.getItem()).toString());
                  out.addProperty("resultCount", this.expectedOutput.getCount());
                  out.addProperty("damageValue", this.expectedOutput.getDamageValue());
                  out.add("enchantSummary", AiWorldReaders.summary(this.expectedOutput));
               }

               return out;
            }

            AiOperation.Result running(String message) {
               return new AiOperation.Result(AiOperation.Status.RUNNING, message, this.statusData().toString(), List.of());
            }

            AiOperation.Result finish(AiOperation.Status status, String message) {
               return this.finish(status, message, null);
            }

            AiOperation.Result fail(AiFailure code, String message) {
               return this.fail(code, message, null);
            }

            AiOperation.Result fail(AiFailure code, String message, JsonObject extra) {
               JsonObject data = this.statusData();
               if (extra != null) {
                  for (Entry<String, JsonElement> entry : extra.entrySet()) {
                     data.add(entry.getKey(), entry.getValue());
                  }
               }

               this.cancel();
               return AiOperation.Result.failed(code, message, data.toString());
            }

            AiOperation.Result finish(AiOperation.Status status, String message, JsonObject extra) {
               JsonObject data = this.statusData();
               if (extra != null) {
                  for (Entry<String, JsonElement> entry : extra.entrySet()) {
                     data.add(entry.getKey(), entry.getValue());
                  }
               }

               this.cancel();
               return new AiOperation.Result(status, message, data.toString(), List.of());
            }

            @Override
            public void cancel() {
               if (!this.done) {
                  this.done = true;
                  this.aim.clear();
                  this.placer.reset();
                  if (this.client.player != null && this.client.player.containerMenu instanceof AnvilMenu) {
                     this.client.player.closeContainer();
                  }
               }
            }
         };
      } else {
         throw new IllegalArgumentException("Anvil work requires a connected living player");
      }
   }

   private static String requireItemId(String spec, String arg) {
      if (spec.isBlank()) {
         throw new IllegalArgumentException(arg + " is required");
      } else {
         AiItems.ItemSpec target = AiItems.resolveItem(spec);
         if (target.items().size() != 1) {
            throw new IllegalArgumentException(arg + " must resolve to a single item id, got " + target.items().size() + " from \"" + spec + "\"");
         } else {
            return BuiltInRegistries.ITEM.getKey(target.items().iterator().next()).toString();
         }
      }
   }

   private static AiAnvil.AnvilMode parseMode(String text) {
      try {
         return AiAnvil.AnvilMode.valueOf(text.trim().toUpperCase(Locale.ROOT));
      } catch (IllegalArgumentException e) {
         throw new IllegalArgumentException("mode must be repair or combine");
      }
   }

   static String refusalReason(boolean resultEmpty, int cost, int playerLevel) {
      if (resultEmpty) {
         return "incompatible";
      } else if (cost > 39) {
         return "too_expensive";
      } else {
         return playerLevel < cost ? "insufficient_levels" : null;
      }
   }

   enum AnvilMode {
      REPAIR,
      COMBINE;
   }
}

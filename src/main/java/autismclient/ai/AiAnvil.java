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
   static final int init = 39;

   private AiAnvil() {
   }

   static AiOperation init(JsonObject args) {
      Minecraft client = Minecraft.getInstance();
      if (client.player != null && client.level != null) {
         final AiAnvil.Inner_DataUtil mode = init(AiJson.getString(args, "mode", "repair"));
         final String leftId = init(AiJson.getString(args, "left", ""), "left");
         final String rightId = init(AiJson.getString(args, "right", ""), "right");
         final int timeoutSeconds = Math.max(10, Math.min(3600, AiJson.getInt(args, "timeoutSeconds", 300)));
         return new AiOperation() {
            final Minecraft init_Minecraft = Minecraft.getInstance();
            final long init_J = System.nanoTime() + timeoutSeconds * 1000000000L;
            final autismclient.ai.engine.AiAim init_DataUtil = new autismclient.ai.engine.AiAim();
            final autismclient.ai.engine.AiPlacer init_DataPipe = new autismclient.ai.engine.AiPlacer();
            final int init_I = this.init_Minecraft.player.experienceLevel;
            BlockPos init_BlockPos;
            int load_I;
            int read_I;
            int write;
            int update;
            int sync;
            int flush;
            long load_J = -1L;
            boolean init_Z;
            boolean load_Z;
            ItemStack init_ItemStack = ItemStack.EMPTY;
            int close = -1;
            int open = -1;
            int poll;
            String init_Str = "";
            int push = -1;
            boolean read_Z;

            @Override
            public AiOperation.Result tick() {
               if (!this.load_Z && this.init_Minecraft.player != null && this.init_Minecraft.level != null) {
                  if (System.nanoTime() > this.init_J) {
                     return this.init(AiOperation.Status.TIMED_OUT, "Anvil deadline reached");
                  } else if (this.init_Minecraft.player.getHealth() <= 4.0F && !AiGuard.isLowHealthOverrideActive()) {
                     return this.init(AiFailure.LOW_HEALTH, AiGuard.formatLowHealthAbort("anvil work", this.init_Minecraft.player.getHealth(), this.init_Minecraft.player.getMaxHealth()));
                  } else if (!(this.init_Minecraft.player.containerMenu instanceof AnvilMenu menu)) {
                     AiOperation.Result open = this.init_AiOperation_Result_3();
                     return open != null ? open : this.init("Opening the anvil");
                  } else {
                     if (this.open != menu.containerId) {
                        for (int slot = 0; slot <= 2; slot++) {
                           if (!menu.getSlot(slot).getItem().isEmpty()) {
                              return this.init(
                                 AiFailure.IMPOSSIBLE_STATE,
                                 "The anvil is in use: slot "
                                    + slot
                                    + " holds "
                                    + BuiltInRegistries.ITEM.getKey(menu.getSlot(slot).getItem().getItem())
                                    + "; empty it first"
                              );
                           }
                        }

                        this.open = menu.containerId;
                     }

                     AiOperation.Result missing = this.init(menu);
                     if (missing != null) {
                        return missing;
                     }

                     if (this.load_J >= 0L) {
                        if (AiServerEvidence.containerRevision(menu.containerId) > this.load_J) {
                           if (menu.getSlot(2).getItem().isEmpty()
                              && AiGameState.countInventoryItem(BuiltInRegistries.ITEM.getKey(this.init_ItemStack.getItem()).toString()) > this.poll) {
                              return this.init(AiOperation.Status.SUCCEEDED, "Anvil produced " + BuiltInRegistries.ITEM.getKey(this.init_ItemStack.getItem()));
                           } else {
                              return ++this.flush > 40
                                 ? this.init(
                                    AiOperation.Status.UNCERTAIN,
                                    menu.getSlot(2).getItem().isEmpty()
                                       ? "The result left the anvil but was not observed in the inventory; reobserve before repeating"
                                       : "The result is still in the anvil (inventory full?); free space and take it before repeating"
                                 )
                                 : this.init("Verifying the anvil result reached the inventory");
                           }
                        } else {
                           return ++this.flush > 40
                              ? this.init(AiOperation.Status.UNCERTAIN, "The result was not confirmed by the server; reobserve before repeating")
                              : this.init("Waiting for the server to apply the anvil result");
                        }
                     } else {
                        if (menu.getSlot(0).getItem().isEmpty()) {
                           this.init(menu, leftId, 0);
                           return this.init("Placing the left item");
                        }

                        if (menu.getSlot(1).getItem().isEmpty()) {
                           this.init(menu, rightId, 1);
                           return this.init("Placing the right item");
                        }

                        ItemStack out = menu.getSlot(2).getItem();
                        int cost = menu.getCost();
                        if (out.isEmpty() && this.sync++ < 40) {
                           return this.init("Waiting for the anvil result");
                        }

                        String failure = AiAnvil.init(out.isEmpty(), cost, this.init_Minecraft.player.experienceLevel);
                        if (failure != null) {
                           this.close = cost;
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
                                    + this.init_Minecraft.player.experienceLevel
                                    + " (deficit "
                                    + (cost - this.init_Minecraft.player.experienceLevel)
                                    + ")";
                                 break;
                              default:
                                 var10002 = "The combination produces no result (incompatible)";
                           }

                           return this.init(var10001, var10002);
                        } else {
                           this.close = cost;
                           this.init_ItemStack = out.copy();
                           this.poll = AiGameState.countInventoryItem(BuiltInRegistries.ITEM.getKey(this.init_ItemStack.getItem()).toString());
                           this.load_J = AiServerEvidence.containerRevision(menu.containerId);
                           AiContainerTransactions.init(menu.containerId, 2, 0, ContainerInput.QUICK_MOVE);
                           return this.init("Taking the result");
                        }
                     }
                  }
               } else {
                  return this.init(AiOperation.Status.CANCELLED, "Anvil work cancelled; committed anvil contents stay");
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

                  return missing.size() > 0 ? this.init(AiFailure.MISSING_MATERIALS, "Missing anvil materials", AiJson.objectOf("missing", missing)) : null;
               } else {
                  return null;
               }
            }

            void init(AnvilMenu menu, String itemId, int targetSlot) {
               int slot = this.init(itemId, true);
               if (slot < 0) {
                  slot = this.init(itemId, false);
               }

               if (slot >= 0) {
                  this.init(itemId, slot);
                  int menuSlot = this.init(slot);
                  AiContainerTransactions.init(menu.containerId, menuSlot, 0, ContainerInput.PICKUP);
                  AiContainerTransactions.init(menu.containerId, targetSlot, 0, ContainerInput.PICKUP);
                  this.update++;
               }
            }

            int init(String itemId, boolean plainOnly) {
               for (int slot = 0; slot < 36; slot++) {
                  ItemStack stack = this.init_Minecraft.player.getInventory().getItem(slot);
                  if (!stack.isEmpty() && BuiltInRegistries.ITEM.getKey(stack.getItem()).toString().equals(itemId) && (!plainOnly || AiItems.isPlainItem(stack))) {
                     return slot;
                  }
               }

               return -1;
            }

            void init(String itemId, int slot) {
               int sameId = 0;

               for (int i = 0; i < 36; i++) {
                  ItemStack stack = this.init_Minecraft.player.getInventory().getItem(i);
                  if (!stack.isEmpty() && BuiltInRegistries.ITEM.getKey(stack.getItem()).toString().equals(itemId)) {
                     sameId++;
                  }
               }

               if (sameId > 1) {
                  this.init_Str = itemId;
                  this.push = slot;
                  this.read_Z = true;
               }
            }

            int init(int inventorySlot) {
               return inventorySlot < 9 ? inventorySlot + 30 : inventorySlot - 9 + 3;
            }

            AiOperation.Result init_AiOperation_Result_3() {
               if (this.init_BlockPos == null && this.write-- <= 0) {
                  this.init_BlockPos = this.init_BlockPos();
                  this.write = 40;
               }

               if (this.init_BlockPos == null && this.write < 40 && !this.init_Z) {
                  return this.init("Looking for an anvil");
               }

               if (this.init_BlockPos == null) {
                  if (!this.init_Z) {
                     if (AiGameState.countInventoryItem("minecraft:anvil") <= 0) {
                        JsonObject missing = new JsonObject();
                        missing.addProperty("minecraft:anvil", 1);
                        return this.init(AiFailure.MISSING_STATION, "No anvil in reach or in the bag", AiJson.objectOf("missing", missing));
                     }

                     BlockPos spot = AiCraft.findPlacementSpot();
                     if (spot == null) {
                        return this.init(AiOperation.Status.FAILED, "No free supported spot next to the player for an anvil");
                     }

                     this.init_DataPipe.init_V_BlockPos_Item(spot, BuiltInRegistries.ITEM.getOptional(Identifier.parse("minecraft:anvil")).orElseThrow());
                     this.init_Z = true;
                     this.read_I = 0;
                  }

                  autismclient.ai.engine.AiPlacer.PlaceStatus status = this.init_DataPipe.init_DataPipe_DataUtil();
                  if (status == autismclient.ai.engine.AiPlacer.PlaceStatus.PLACED) {
                     this.init_BlockPos = this.init_DataPipe.init_BlockPos();
                     this.init_Z = false;
                     return this.init("Anvil placed");
                  }

                  if (status != autismclient.ai.engine.AiPlacer.PlaceStatus.FAILED && ++this.read_I <= 100) {
                     return this.init("Placing an anvil");
                  }

                  this.init_Z = false;
                  return this.init(AiOperation.Status.FAILED, "Could not place an anvil: " + this.init_DataPipe.init_Str());
               } else {
                  double reach = Math.max(4.5, this.init_Minecraft.player.blockInteractionRange());
                  if (this.init_Minecraft.player.getEyePosition().distanceToSqr(Vec3.atCenterOf(this.init_BlockPos)) > reach * reach) {
                     return this.init(AiOperation.Status.FAILED, "The anvil fell out of reach");
                  }

                  this.init_DataUtil.aimAt(Vec3.atCenterOf(this.init_BlockPos));
                  this.init_DataUtil.tick();
                  if (++this.load_I > 80) {
                     return this.init(AiOperation.Status.FAILED, "Could not open the anvil");
                  }

                  if (!autismclient.ai.engine.AiAim.isLookingAtBlock(this.init_BlockPos)) {
                     return this.init("Turning toward the anvil");
                  }

                  BlockHitResult hit = autismclient.ai.engine.AiAim.raycastLookBlock();
                  if (hit == null) {
                     return this.init("Turning toward the anvil");
                  }

                  this.init_Minecraft.gameMode.useItemOn(this.init_Minecraft.player, InteractionHand.MAIN_HAND, hit);
                  Compat263.swing(this.init_Minecraft.player, InteractionHand.MAIN_HAND);
                  return null;
               }
            }

            BlockPos init_BlockPos() {
               Block wanted = AiCraft.requireBlock("minecraft:anvil");
               BlockPos origin = this.init_Minecraft.player.blockPosition();
               BlockPos best = null;
               double bestDistance = 1.7976931348623157E308;
               int radius = 16;

               for (BlockPos pos : BlockPos.betweenClosed(origin.offset(-radius, -8, -radius), origin.offset(radius, 8, radius))) {
                  if (AiTerrain.isLoaded(this.init_Minecraft.level, pos) && this.init_Minecraft.level.getBlockState(pos).getBlock() == wanted) {
                     double distance = this.init_Minecraft.player.distanceToSqr(Vec3.atCenterOf(pos));
                     if (distance < bestDistance) {
                        bestDistance = distance;
                        best = pos.immutable();
                     }
                  }
               }

               return best;
            }

            JsonObject init_JsonObject_4() {
               JsonObject out = AiJson.objectOf(
                  "mode",
                  mode.name().toLowerCase(Locale.ROOT),
                  "left",
                  leftId,
                  "right",
                  rightId,
                  "levelsSpent",
                  this.init_I - (this.init_Minecraft.player == null ? this.init_I : this.init_Minecraft.player.experienceLevel),
                  "stationAt",
                  this.init_BlockPos == null ? null : this.init_BlockPos.toShortString()
               );
               if (this.close >= 0) {
                  out.addProperty("levelCost", this.close);
               }

               if (this.read_Z) {
                  out.add("picked", AiJson.objectOf("item", this.init_Str, "slot", this.push));
               }

               if (!this.init_ItemStack.isEmpty()) {
                  out.addProperty("result", BuiltInRegistries.ITEM.getKey(this.init_ItemStack.getItem()).toString());
                  out.addProperty("resultCount", this.init_ItemStack.getCount());
                  out.addProperty("damageValue", this.init_ItemStack.getDamageValue());
                  out.add("enchantSummary", AiWorldReaders.init(this.init_ItemStack));
               }

               return out;
            }

            AiOperation.Result init(String message) {
               return new AiOperation.Result(AiOperation.Status.RUNNING, message, this.init_JsonObject_4().toString(), List.of());
            }

            AiOperation.Result init(AiOperation.Status status, String message) {
               return this.init(status, message, null);
            }

            AiOperation.Result init(AiFailure code, String message) {
               return this.init(code, message, null);
            }

            AiOperation.Result init(AiFailure code, String message, JsonObject extra) {
               JsonObject data = this.init_JsonObject_4();
               if (extra != null) {
                  for (Entry<String, JsonElement> entry : extra.entrySet()) {
                     data.add(entry.getKey(), entry.getValue());
                  }
               }

               this.cancel();
               return AiOperation.Result.failed(code, message, data.toString());
            }

            AiOperation.Result init(AiOperation.Status status, String message, JsonObject extra) {
               JsonObject data = this.init_JsonObject_4();
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
               if (!this.load_Z) {
                  this.load_Z = true;
                  this.init_DataUtil.clear();
                  this.init_DataPipe.init_V();
                  if (this.init_Minecraft.player != null && this.init_Minecraft.player.containerMenu instanceof AnvilMenu) {
                     this.init_Minecraft.player.closeContainer();
                  }
               }
            }
         };
      } else {
         throw new IllegalArgumentException("Anvil work requires a connected living player");
      }
   }

   private static String init(String spec, String arg) {
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

   private static AiAnvil.Inner_DataUtil init(String text) {
      try {
         return AiAnvil.Inner_DataUtil.valueOf(text.trim().toUpperCase(Locale.ROOT));
      } catch (IllegalArgumentException e) {
         throw new IllegalArgumentException("mode must be repair or combine");
      }
   }

   static String init(boolean resultEmpty, int cost, int playerLevel) {
      if (resultEmpty) {
         return "incompatible";
      } else if (cost > 39) {
         return "too_expensive";
      } else {
         return playerLevel < cost ? "insufficient_levels" : null;
      }
   }

   enum Inner_DataUtil {
      REPAIR,
      COMBINE;
   }
}

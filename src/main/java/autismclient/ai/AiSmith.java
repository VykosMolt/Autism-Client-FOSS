package autismclient.ai;

import autismclient.api.ai.AiOperation;
import autismclient.compat263.Compat263;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.Map.Entry;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.inventory.ContainerInput;
import net.minecraft.world.inventory.SmithingMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

final class AiSmith {
   private static final int init_I = 80;
   private static final int load_I = 100;
   private static final int read = 40;
   private static final int write = 40;
   private static final Set<String> NETHERITE_UPGRADE_BASES = Set.of(
      "minecraft:diamond_sword",
      "minecraft:diamond_pickaxe",
      "minecraft:diamond_axe",
      "minecraft:diamond_shovel",
      "minecraft:diamond_hoe",
      "minecraft:diamond_helmet",
      "minecraft:diamond_chestplate",
      "minecraft:diamond_leggings",
      "minecraft:diamond_boots"
   );
   private static final Set<String> TRIM_MATERIALS = Set.of(
      "minecraft:iron_ingot",
      "minecraft:copper_ingot",
      "minecraft:gold_ingot",
      "minecraft:lapis_lazuli",
      "minecraft:emerald",
      "minecraft:diamond",
      "minecraft:netherite_ingot",
      "minecraft:redstone",
      "minecraft:quartz",
      "minecraft:amethyst_shard"
   );

   private AiSmith() {
   }

   static AiOperation init(JsonObject args) {
      Minecraft client = Minecraft.getInstance();
      if (client.player != null && client.level != null) {
         final AiSmith.Inner_DataUtil mode = parseMode(AiJson.getString(args, "mode", "upgrade"));
         final String templateId = requireItemId(AiJson.getString(args, "template", ""), "template");
         final String baseId = requireItemId(AiJson.getString(args, "base", ""), "base");
         final String additionId = requireItemId(AiJson.getString(args, "addition", ""), "addition");
         String combo = validateInputs(mode, templateId, baseId, additionId);
         if (combo != null) {
            throw new IllegalArgumentException(combo);
         }

         final int timeoutSeconds = Math.max(10, Math.min(3600, AiJson.getInt(args, "timeoutSeconds", 300)));
         return new AiOperation() {
            final Minecraft client = Minecraft.getInstance();
            final long deadlineNanos = System.nanoTime() + timeoutSeconds * 1000000000L;
            final autismclient.ai.engine.AiAim aim = new autismclient.ai.engine.AiAim();
            final autismclient.ai.engine.AiPlacer placer = new autismclient.ai.engine.AiPlacer();
            BlockPos tablePos;
            int aimTicks;
            int placeTicks;
            int searchCooldown;
            int write;
            int resultWaitTicks;
            int confirmWaitTicks;
            long takeRevision = -1L;
            boolean placing;
            boolean done;
            ItemStack expectedOutput = ItemStack.EMPTY;
            int menuId = -1;
            int outputCountBefore;
            String pickedItem = "";
            int pickedSlot = -1;
            boolean picked;

            @Override
            public AiOperation.Result tick() {
               if (!this.done && this.client.player != null && this.client.level != null) {
                  if (System.nanoTime() > this.deadlineNanos) {
                     return this.finish(AiOperation.Status.TIMED_OUT, "Smithing deadline reached");
                  } else if (this.client.player.getHealth() <= 4.0F && !AiGuard.isLowHealthOverrideActive()) {
                     return this.fail(AiFailure.LOW_HEALTH, AiGuard.formatLowHealthAbort("smithing", this.client.player.getHealth(), this.client.player.getMaxHealth()));
                  } else if (!(this.client.player.containerMenu instanceof SmithingMenu menu)) {
                     AiOperation.Result open = this.ensureTable();
                     return open != null ? open : this.running("Opening the smithing table");
                  } else {
                     if (this.menuId != menu.containerId) {
                        for (int slot = 0; slot <= 3; slot++) {
                           if (!menu.getSlot(slot).getItem().isEmpty()) {
                              return this.fail(
                                 AiFailure.IMPOSSIBLE_STATE,
                                 "The smithing table is in use: slot "
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
                           if (menu.getSlot(3).getItem().isEmpty()
                              && AiGameState.countInventoryItem(BuiltInRegistries.ITEM.getKey(this.expectedOutput.getItem()).toString()) > this.outputCountBefore) {
                              return this.finish(AiOperation.Status.SUCCEEDED, "Smithed " + BuiltInRegistries.ITEM.getKey(this.expectedOutput.getItem()));
                           } else {
                              return ++this.confirmWaitTicks > 40
                                 ? this.finish(
                                    AiOperation.Status.UNCERTAIN,
                                    menu.getSlot(3).getItem().isEmpty()
                                       ? "The result left the station but was not observed in the inventory; reobserve before repeating"
                                       : "The result is still in the station (inventory full?); free space and take it before repeating"
                                 )
                                 : this.running("Verifying the smithed result reached the inventory");
                           }
                        } else {
                           return ++this.confirmWaitTicks > 40
                              ? this.finish(AiOperation.Status.UNCERTAIN, "The result was not confirmed by the server; reobserve before repeating")
                              : this.running("Waiting for the server to apply the smith");
                        }
                     } else {
                        if (menu.getSlot(0).getItem().isEmpty()) {
                           this.moveItemToSlot(menu, templateId, 0);
                           return this.running("Placing the template");
                        }

                        if (menu.getSlot(1).getItem().isEmpty()) {
                           this.moveItemToSlot(menu, baseId, 1);
                           return this.running("Placing the base item");
                        }

                        if (menu.getSlot(2).getItem().isEmpty()) {
                           this.moveItemToSlot(menu, additionId, 2);
                           return this.running("Placing the addition");
                        }

                        ItemStack out = menu.getSlot(3).getItem();
                        if (out.isEmpty()) {
                           return ++this.resultWaitTicks > 40
                              ? this.fail(AiFailure.IMPOSSIBLE_STATE, "The combination produced no result; check the inputs")
                              : this.running("Waiting for the result");
                        }

                        this.expectedOutput = out.copy();
                        this.outputCountBefore = AiGameState.countInventoryItem(BuiltInRegistries.ITEM.getKey(this.expectedOutput.getItem()).toString());
                        this.takeRevision = AiServerEvidence.containerRevision(menu.containerId);
                        AiContainerTransactions.sendPredictedClick(menu.containerId, 3, 0, ContainerInput.QUICK_MOVE);
                        return this.running("Taking the result");
                     }
                  }
               } else {
                  return this.finish(AiOperation.Status.CANCELLED, "Smithing cancelled; committed table contents stay");
               }
            }

            AiOperation.Result init(SmithingMenu menu) {
               if (this.write <= 0 && menu.getSlot(3).getItem().isEmpty()) {
                  JsonObject missing = new JsonObject();
                  if (menu.getSlot(0).getItem().isEmpty() && AiGameState.countMainInventoryItem(templateId) < 1) {
                     missing.addProperty(templateId, 1);
                  }

                  if (menu.getSlot(1).getItem().isEmpty() && AiGameState.countMainInventoryItem(baseId) < 1) {
                     missing.addProperty(baseId, 1);
                  }

                  if (menu.getSlot(2).getItem().isEmpty() && AiGameState.countMainInventoryItem(additionId) < 1) {
                     missing.addProperty(additionId, 1);
                  }

                  return missing.size() > 0 ? this.fail(AiFailure.MISSING_MATERIALS, "Missing smithing materials", AiJson.objectOf("missing", missing)) : null;
               } else {
                  return null;
               }
            }

            void moveItemToSlot(SmithingMenu menu, String itemId, int targetSlot) {
               int slot = this.findItemSlot(itemId, true);
               if (slot < 0) {
                  slot = this.findItemSlot(itemId, false);
               }

               if (slot >= 0) {
                  this.recordPick(itemId, slot);
                  int menuSlot = this.menuSlot(slot);
                  AiContainerTransactions.sendPredictedClick(menu.containerId, menuSlot, 0, ContainerInput.PICKUP);
                  AiContainerTransactions.sendPredictedClick(menu.containerId, targetSlot, 0, ContainerInput.PICKUP);
                  this.write++;
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
               return inventorySlot < 9 ? inventorySlot + 31 : inventorySlot - 9 + 4;
            }

            AiOperation.Result ensureTable() {
               if (this.tablePos == null && this.searchCooldown-- <= 0) {
                  this.tablePos = this.findTable();
                  this.searchCooldown = 40;
               }

               if (this.tablePos == null && this.searchCooldown < 40 && !this.placing) {
                  return this.running("Looking for a smithing table");
               }

               if (this.tablePos == null) {
                  if (!this.placing) {
                     if (AiGameState.countInventoryItem("minecraft:smithing_table") <= 0) {
                        JsonObject missing = new JsonObject();
                        missing.addProperty("minecraft:smithing_table", 1);
                        return this.fail(AiFailure.MISSING_STATION, "No smithing table in reach or in the bag", AiJson.objectOf("missing", missing));
                     }

                     BlockPos spot = AiCraft.findPlacementSpot();
                     if (spot == null) {
                        return this.finish(AiOperation.Status.FAILED, "No free supported spot next to the player for a smithing table");
                     }

                     this.placer
                        .start(spot, BuiltInRegistries.ITEM.getOptional(Identifier.parse("minecraft:smithing_table")).orElseThrow());
                     this.placing = true;
                     this.placeTicks = 0;
                  }

                  autismclient.ai.engine.AiPlacer.PlaceStatus status = this.placer.tick();
                  if (status == autismclient.ai.engine.AiPlacer.PlaceStatus.PLACED) {
                     this.tablePos = this.placer.targetPos();
                     this.placing = false;
                     return this.running("Smithing table placed");
                  }

                  if (status != autismclient.ai.engine.AiPlacer.PlaceStatus.FAILED && ++this.placeTicks <= 100) {
                     return this.running("Placing a smithing table");
                  }

                  this.placing = false;
                  return this.finish(AiOperation.Status.FAILED, "Could not place a smithing table: " + this.placer.failureReason());
               } else {
                  double reach = Math.max(4.5, this.client.player.blockInteractionRange());
                  if (this.client.player.getEyePosition().distanceToSqr(Vec3.atCenterOf(this.tablePos)) > reach * reach) {
                     return this.finish(AiOperation.Status.FAILED, "The smithing table fell out of reach");
                  }

                  this.aim.aimAt(Vec3.atCenterOf(this.tablePos));
                  this.aim.tick();
                  if (++this.aimTicks > 80) {
                     return this.finish(AiOperation.Status.FAILED, "Could not open the smithing table");
                  }

                  if (!autismclient.ai.engine.AiAim.isLookingAtBlock(this.tablePos)) {
                     return this.running("Turning toward the smithing table");
                  }

                  BlockHitResult hit = autismclient.ai.engine.AiAim.raycastLookBlock();
                  if (hit == null) {
                     return this.running("Turning toward the smithing table");
                  }

                  this.client.gameMode.useItemOn(this.client.player, InteractionHand.MAIN_HAND, hit);
                  Compat263.swing(this.client.player, InteractionHand.MAIN_HAND);
                  return null;
               }
            }

            BlockPos findTable() {
               Block wanted = AiCraft.requireBlock("minecraft:smithing_table");
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

            JsonObject materialCounts() {
               return AiJson.objectOf("template", AiGameState.countInventoryItem(templateId), "base", AiGameState.countInventoryItem(baseId), "addition", AiGameState.countInventoryItem(additionId));
            }

            JsonObject statusData() {
               JsonObject out = AiJson.objectOf(
                  "mode",
                  mode.name().toLowerCase(Locale.ROOT),
                  "template",
                  templateId,
                  "base",
                  baseId,
                  "addition",
                  additionId,
                  "result",
                  this.expectedOutput.isEmpty() ? null : BuiltInRegistries.ITEM.getKey(this.expectedOutput.getItem()).toString(),
                  "resultCount",
                  this.expectedOutput.isEmpty() ? 0 : this.expectedOutput.getCount(),
                  "remainingMaterials",
                  this.materialCounts(),
                  "stationAt",
                  this.tablePos == null ? null : this.tablePos.toShortString()
               );
               if (this.picked) {
                  out.add("picked", AiJson.objectOf("item", this.pickedItem, "slot", this.pickedSlot));
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
                  if (this.client.player != null && this.client.player.containerMenu instanceof SmithingMenu) {
                     this.client.player.closeContainer();
                  }
               }
            }
         };
      } else {
         throw new IllegalArgumentException("Smithing requires a connected living player");
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

   private static AiSmith.Inner_DataUtil parseMode(String text) {
      try {
         return AiSmith.Inner_DataUtil.valueOf(text.trim().toUpperCase(Locale.ROOT));
      } catch (IllegalArgumentException e) {
         throw new IllegalArgumentException("mode must be upgrade or trim");
      }
   }

   static String validateInputs(AiSmith.Inner_DataUtil mode, String templateId, String baseId, String additionId) {
      if (mode == AiSmith.Inner_DataUtil.UPGRADE) {
         if (!templateId.equals("minecraft:netherite_upgrade_smithing_template")) {
            return "upgrade needs minecraft:netherite_upgrade_smithing_template as the template";
         } else if (!additionId.equals("minecraft:netherite_ingot")) {
            return "upgrade needs minecraft:netherite_ingot as the addition";
         } else {
            return !NETHERITE_UPGRADE_BASES.contains(baseId) ? "upgrade base must be diamond-tier gear, got " + baseId : null;
         }
      } else if (!templateId.endsWith("_armor_trim_smithing_template")) {
         return "trim needs an *_armor_trim_smithing_template as the template, got " + templateId;
      } else if (!TRIM_MATERIALS.contains(additionId)) {
         return "trim addition must be a trim material (iron/copper/gold/lapis/emerald/diamond/netherite/redstone/quartz/amethyst), got " + additionId;
      } else {
         return !isArmorPiece(baseId) ? "trim base must be an armor piece, got " + baseId : null;
      }
   }

   private static boolean isArmorPiece(String itemId) {
      String path = itemId.substring(itemId.indexOf(58) + 1);
      return path.endsWith("_helmet") || path.endsWith("_chestplate") || path.endsWith("_leggings") || path.endsWith("_boots") || path.equals("turtle_helmet");
   }

   enum Inner_DataUtil {
      UPGRADE,
      TRIM;
   }
}

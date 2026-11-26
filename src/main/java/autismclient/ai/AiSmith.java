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
   private static final Set<String> init_Set = Set.of(
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
   private static final Set<String> load_Set = Set.of(
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
         final AiSmith.Inner_DataUtil mode = init_cj_DataUtil(AiJson.init_Str_JsonObject(args, "mode", "upgrade"));
         final String templateId = init(AiJson.init_Str_JsonObject(args, "template", ""), "template");
         final String baseId = init(AiJson.init_Str_JsonObject(args, "base", ""), "base");
         final String additionId = init(AiJson.init_Str_JsonObject(args, "addition", ""), "addition");
         String combo = init(mode, templateId, baseId, additionId);
         if (combo != null) {
            throw new IllegalArgumentException(combo);
         }

         final int timeoutSeconds = Math.max(10, Math.min(3600, AiJson.init_I_JsonObject(args, "timeoutSeconds", 300)));
         return new AiOperation() {
            final Minecraft init_Minecraft = Minecraft.getInstance();
            final long init_J = System.nanoTime() + timeoutSeconds * 1000000000L;
            final autismclient.ai.a.AiAim init_DataUtil = new autismclient.ai.a.AiAim();
            final autismclient.ai.a.AiPlacer init_DataPipe = new autismclient.ai.a.AiPlacer();
            BlockPos init_BlockPos;
            int init_I;
            int load_I;
            int read_I;
            int write;
            int update;
            int sync;
            long load_J = -1L;
            boolean init_Z;
            boolean load_Z;
            ItemStack init_ItemStack = ItemStack.EMPTY;
            int flush = -1;
            int close;
            String init_Str = "";
            int open = -1;
            boolean read_Z;

            @Override
            public AiOperation.Result tick() {
               if (!this.load_Z && this.init_Minecraft.player != null && this.init_Minecraft.level != null) {
                  if (System.nanoTime() > this.init_J) {
                     return this.init(AiOperation.Status.TIMED_OUT, "Smithing deadline reached");
                  } else if (this.init_Minecraft.player.getHealth() <= 4.0F && !AiGuard.close()) {
                     return this.init(AiFailure.start, AiGuard.init_Str_Str("smithing", this.init_Minecraft.player.getHealth(), this.init_Minecraft.player.getMaxHealth()));
                  } else if (!(this.init_Minecraft.player.containerMenu instanceof SmithingMenu menu)) {
                     AiOperation.Result open = this.init_AiOperation_Result_33();
                     return open != null ? open : this.init("Opening the smithing table");
                  } else {
                     if (this.flush != menu.containerId) {
                        for (int slot = 0; slot <= 3; slot++) {
                           if (!menu.getSlot(slot).getItem().isEmpty()) {
                              return this.init(
                                 AiFailure.data,
                                 "The smithing table is in use: slot "
                                    + slot
                                    + " holds "
                                    + BuiltInRegistries.ITEM.getKey(menu.getSlot(slot).getItem().getItem())
                                    + "; empty it first"
                              );
                           }
                        }

                        this.flush = menu.containerId;
                     }

                     AiOperation.Result missing = this.init(menu);
                     if (missing != null) {
                        return missing;
                     }

                     if (this.load_J >= 0L) {
                        if (AiServerEvidence.init_J_I(menu.containerId) > this.load_J) {
                           if (menu.getSlot(3).getItem().isEmpty()
                              && AiGameState.init_I_Str(BuiltInRegistries.ITEM.getKey(this.init_ItemStack.getItem()).toString()) > this.close) {
                              return this.init(AiOperation.Status.SUCCEEDED, "Smithed " + BuiltInRegistries.ITEM.getKey(this.init_ItemStack.getItem()));
                           } else {
                              return ++this.sync > 40
                                 ? this.init(
                                    AiOperation.Status.UNCERTAIN,
                                    menu.getSlot(3).getItem().isEmpty()
                                       ? "The result left the station but was not observed in the inventory; reobserve before repeating"
                                       : "The result is still in the station (inventory full?); free space and take it before repeating"
                                 )
                                 : this.init("Verifying the smithed result reached the inventory");
                           }
                        } else {
                           return ++this.sync > 40
                              ? this.init(AiOperation.Status.UNCERTAIN, "The result was not confirmed by the server; reobserve before repeating")
                              : this.init("Waiting for the server to apply the smith");
                        }
                     } else {
                        if (menu.getSlot(0).getItem().isEmpty()) {
                           this.init(menu, templateId, 0);
                           return this.init("Placing the template");
                        }

                        if (menu.getSlot(1).getItem().isEmpty()) {
                           this.init(menu, baseId, 1);
                           return this.init("Placing the base item");
                        }

                        if (menu.getSlot(2).getItem().isEmpty()) {
                           this.init(menu, additionId, 2);
                           return this.init("Placing the addition");
                        }

                        ItemStack out = menu.getSlot(3).getItem();
                        if (out.isEmpty()) {
                           return ++this.update > 40
                              ? this.init(AiFailure.data, "The combination produced no result; check the inputs")
                              : this.init("Waiting for the result");
                        }

                        this.init_ItemStack = out.copy();
                        this.close = AiGameState.init_I_Str(BuiltInRegistries.ITEM.getKey(this.init_ItemStack.getItem()).toString());
                        this.load_J = AiServerEvidence.init_J_I(menu.containerId);
                        AiContainerTransactions.init(menu.containerId, 3, 0, ContainerInput.QUICK_MOVE);
                        return this.init("Taking the result");
                     }
                  }
               } else {
                  return this.init(AiOperation.Status.CANCELLED, "Smithing cancelled; committed table contents stay");
               }
            }

            AiOperation.Result init(SmithingMenu menu) {
               if (this.write <= 0 && menu.getSlot(3).getItem().isEmpty()) {
                  JsonObject missing = new JsonObject();
                  if (menu.getSlot(0).getItem().isEmpty() && AiGameState.load(templateId) < 1) {
                     missing.addProperty(templateId, 1);
                  }

                  if (menu.getSlot(1).getItem().isEmpty() && AiGameState.load(baseId) < 1) {
                     missing.addProperty(baseId, 1);
                  }

                  if (menu.getSlot(2).getItem().isEmpty() && AiGameState.load(additionId) < 1) {
                     missing.addProperty(additionId, 1);
                  }

                  return missing.size() > 0 ? this.init(AiFailure.pop, "Missing smithing materials", AiJson.init_JsonObject_ObjArr("missing", missing)) : null;
               } else {
                  return null;
               }
            }

            void init(SmithingMenu menu, String itemId, int targetSlot) {
               int slot = this.init(itemId, true);
               if (slot < 0) {
                  slot = this.init(itemId, false);
               }

               if (slot >= 0) {
                  this.init(itemId, slot);
                  int menuSlot = this.init(slot);
                  AiContainerTransactions.init(menu.containerId, menuSlot, 0, ContainerInput.PICKUP);
                  AiContainerTransactions.init(menu.containerId, targetSlot, 0, ContainerInput.PICKUP);
                  this.write++;
               }
            }

            int init(String itemId, boolean plainOnly) {
               for (int slot = 0; slot < 36; slot++) {
                  ItemStack stack = this.init_Minecraft.player.getInventory().getItem(slot);
                  if (!stack.isEmpty() && BuiltInRegistries.ITEM.getKey(stack.getItem()).toString().equals(itemId) && (!plainOnly || AiItems.init_Z(stack))) {
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
                  this.open = slot;
                  this.read_Z = true;
               }
            }

            int init(int inventorySlot) {
               return inventorySlot < 9 ? inventorySlot + 31 : inventorySlot - 9 + 4;
            }

            AiOperation.Result init_AiOperation_Result_33() {
               if (this.init_BlockPos == null && this.read_I-- <= 0) {
                  this.init_BlockPos = this.init_BlockPos_14();
                  this.read_I = 40;
               }

               if (this.init_BlockPos == null && this.read_I < 40 && !this.init_Z) {
                  return this.init("Looking for a smithing table");
               }

               if (this.init_BlockPos == null) {
                  if (!this.init_Z) {
                     if (AiGameState.init_I_Str("minecraft:smithing_table") <= 0) {
                        JsonObject missing = new JsonObject();
                        missing.addProperty("minecraft:smithing_table", 1);
                        return this.init(AiFailure.set, "No smithing table in reach or in the bag", AiJson.init_JsonObject_ObjArr("missing", missing));
                     }

                     BlockPos spot = AiCraft.init_BlockPos();
                     if (spot == null) {
                        return this.init(AiOperation.Status.FAILED, "No free supported spot next to the player for a smithing table");
                     }

                     this.init_DataPipe
                        .init_V_BlockPos_Item(spot, BuiltInRegistries.ITEM.getOptional(Identifier.parse("minecraft:smithing_table")).orElseThrow());
                     this.init_Z = true;
                     this.load_I = 0;
                  }

                  autismclient.ai.a.AiPlacer.Inner_DataUtil status = this.init_DataPipe.init_DataPipe_DataUtil();
                  if (status == autismclient.ai.a.AiPlacer.Inner_DataUtil.load) {
                     this.init_BlockPos = this.init_DataPipe.init_BlockPos();
                     this.init_Z = false;
                     return this.init("Smithing table placed");
                  }

                  if (status != autismclient.ai.a.AiPlacer.Inner_DataUtil.read && ++this.load_I <= 100) {
                     return this.init("Placing a smithing table");
                  }

                  this.init_Z = false;
                  return this.init(AiOperation.Status.FAILED, "Could not place a smithing table: " + this.init_DataPipe.init_Str());
               } else {
                  double reach = Math.max(4.5, this.init_Minecraft.player.blockInteractionRange());
                  if (this.init_Minecraft.player.getEyePosition().distanceToSqr(Vec3.atCenterOf(this.init_BlockPos)) > reach * reach) {
                     return this.init(AiOperation.Status.FAILED, "The smithing table fell out of reach");
                  }

                  this.init_DataUtil.init_V_Vec3(Vec3.atCenterOf(this.init_BlockPos));
                  this.init_DataUtil.load_V();
                  if (++this.init_I > 80) {
                     return this.init(AiOperation.Status.FAILED, "Could not open the smithing table");
                  }

                  if (!autismclient.ai.a.AiAim.init_Z_BlockPos(this.init_BlockPos)) {
                     return this.init("Turning toward the smithing table");
                  }

                  BlockHitResult hit = autismclient.ai.a.AiAim.init_BlockHitResult();
                  if (hit == null) {
                     return this.init("Turning toward the smithing table");
                  }

                  this.init_Minecraft.gameMode.useItemOn(this.init_Minecraft.player, InteractionHand.MAIN_HAND, hit);
                  Compat263.swing(this.init_Minecraft.player, InteractionHand.MAIN_HAND);
                  return null;
               }
            }

            BlockPos init_BlockPos_14() {
               Block wanted = AiCraft.init_Block("minecraft:smithing_table");
               BlockPos origin = this.init_Minecraft.player.blockPosition();
               BlockPos best = null;
               double bestDistance = 1.7976931348623157E308;
               int radius = 16;

               for (BlockPos pos : BlockPos.betweenClosed(origin.offset(-radius, -8, -radius), origin.offset(radius, 8, radius))) {
                  if (AiTerrain.init(this.init_Minecraft.level, pos) && this.init_Minecraft.level.getBlockState(pos).getBlock() == wanted) {
                     double distance = this.init_Minecraft.player.distanceToSqr(Vec3.atCenterOf(pos));
                     if (distance < bestDistance) {
                        bestDistance = distance;
                        best = pos.immutable();
                     }
                  }
               }

               return best;
            }

            JsonObject init_JsonObject_50() {
               return AiJson.init_JsonObject_ObjArr("template", AiGameState.init_I_Str(templateId), "base", AiGameState.init_I_Str(baseId), "addition", AiGameState.init_I_Str(additionId));
            }

            JsonObject load() {
               JsonObject out = AiJson.init_JsonObject_ObjArr(
                  "mode",
                  mode.name().toLowerCase(Locale.ROOT),
                  "template",
                  templateId,
                  "base",
                  baseId,
                  "addition",
                  additionId,
                  "result",
                  this.init_ItemStack.isEmpty() ? null : BuiltInRegistries.ITEM.getKey(this.init_ItemStack.getItem()).toString(),
                  "resultCount",
                  this.init_ItemStack.isEmpty() ? 0 : this.init_ItemStack.getCount(),
                  "remainingMaterials",
                  this.init_JsonObject_50(),
                  "stationAt",
                  this.init_BlockPos == null ? null : this.init_BlockPos.toShortString()
               );
               if (this.read_Z) {
                  out.add("picked", AiJson.init_JsonObject_ObjArr("item", this.init_Str, "slot", this.open));
               }

               return out;
            }

            AiOperation.Result init(String message) {
               return new AiOperation.Result(AiOperation.Status.RUNNING, message, this.load().toString(), List.of());
            }

            AiOperation.Result init(AiOperation.Status status, String message) {
               return this.init(status, message, null);
            }

            AiOperation.Result init(AiFailure code, String message) {
               return this.init(code, message, null);
            }

            AiOperation.Result init(AiFailure code, String message, JsonObject extra) {
               JsonObject data = this.load();
               if (extra != null) {
                  for (Entry<String, JsonElement> entry : extra.entrySet()) {
                     data.add(entry.getKey(), entry.getValue());
                  }
               }

               this.cancel();
               return AiOperation.Result.failed(code, message, data.toString());
            }

            AiOperation.Result init(AiOperation.Status status, String message, JsonObject extra) {
               JsonObject data = this.load();
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
                  this.init_DataUtil.init_V();
                  this.init_DataPipe.init_V();
                  if (this.init_Minecraft.player != null && this.init_Minecraft.player.containerMenu instanceof SmithingMenu) {
                     this.init_Minecraft.player.closeContainer();
                  }
               }
            }
         };
      } else {
         throw new IllegalArgumentException("Smithing requires a connected living player");
      }
   }

   private static String init(String spec, String arg) {
      if (spec.isBlank()) {
         throw new IllegalArgumentException(arg + " is required");
      } else {
         AiItems.Inner_DataUtil target = AiItems.load_af_DataUtil(spec);
         if (target.items().size() != 1) {
            throw new IllegalArgumentException(arg + " must resolve to a single item id, got " + target.items().size() + " from \"" + spec + "\"");
         } else {
            return BuiltInRegistries.ITEM.getKey(target.items().iterator().next()).toString();
         }
      }
   }

   private static AiSmith.Inner_DataUtil init_cj_DataUtil(String text) {
      try {
         return AiSmith.Inner_DataUtil.valueOf(text.trim().toUpperCase(Locale.ROOT));
      } catch (IllegalArgumentException e) {
         throw new IllegalArgumentException("mode must be upgrade or trim");
      }
   }

   static String init(AiSmith.Inner_DataUtil mode, String templateId, String baseId, String additionId) {
      if (mode == AiSmith.Inner_DataUtil.init) {
         if (!templateId.equals("minecraft:netherite_upgrade_smithing_template")) {
            return "upgrade needs minecraft:netherite_upgrade_smithing_template as the template";
         } else if (!additionId.equals("minecraft:netherite_ingot")) {
            return "upgrade needs minecraft:netherite_ingot as the addition";
         } else {
            return !init_Set.contains(baseId) ? "upgrade base must be diamond-tier gear, got " + baseId : null;
         }
      } else if (!templateId.endsWith("_armor_trim_smithing_template")) {
         return "trim needs an *_armor_trim_smithing_template as the template, got " + templateId;
      } else if (!load_Set.contains(additionId)) {
         return "trim addition must be a trim material (iron/copper/gold/lapis/emerald/diamond/netherite/redstone/quartz/amethyst), got " + additionId;
      } else {
         return !init_Z(baseId) ? "trim base must be an armor piece, got " + baseId : null;
      }
   }

   private static boolean init_Z(String itemId) {
      String path = itemId.substring(itemId.indexOf(58) + 1);
      return path.endsWith("_helmet") || path.endsWith("_chestplate") || path.endsWith("_leggings") || path.endsWith("_boots") || path.equals("turtle_helmet");
   }

   enum Inner_DataUtil {
      init,
      load;
   }
}

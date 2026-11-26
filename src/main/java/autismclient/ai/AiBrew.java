package autismclient.ai;

import autismclient.api.ai.AiOperation;
import autismclient.compat263.Compat263;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Map.Entry;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.inventory.BrewingStandMenu;
import net.minecraft.world.inventory.ContainerInput;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

final class AiBrew {
   private static final int init = 80;
   private static final int load = 100;
   private static final int read = 40;
   private static final int write = 120;
   private static final int update = 20;
   private static final int sync = 40;
   private static final int flush = 4;
   private static final int close = 3;
   private static final int open = 0;
   private static final int poll = 2;

   private AiBrew() {
   }

   static AiOperation init(JsonObject args) {
      Minecraft client = Minecraft.getInstance();
      if (client.player != null && client.level != null) {
         String ingredientSpec = AiJson.init_Str_JsonObject(args, "ingredient", "");
         if (ingredientSpec.isBlank()) {
            throw new IllegalArgumentException("ingredient is required (e.g. nether_wart, blaze_powder)");
         }

         final AiItems.Inner_DataUtil ingredient = AiItems.load_af_DataUtil(ingredientSpec);
         if (ingredient.items().size() != 1) {
            throw new IllegalArgumentException(
               "ingredient must resolve to a single item id, got " + ingredient.items().size() + " from \"" + ingredientSpec + "\""
            );
         }

         final String inputSpec = AiJson.init_Str_JsonObject(args, "input", "water_bottle");
         final String inputType = init(inputSpec);
         final int count = Math.max(1, Math.min(3, AiJson.init_I_JsonObject(args, "count", 3)));
         final int timeoutSeconds = Math.max(10, Math.min(3600, AiJson.init_I_JsonObject(args, "timeoutSeconds", 300)));
         final String ingredientId = BuiltInRegistries.ITEM.getKey(ingredient.items().iterator().next()).toString();
         return new AiOperation() {
            final Minecraft init_Minecraft = Minecraft.getInstance();
            final long init_J = System.nanoTime() + timeoutSeconds * 1000000000L;
            final autismclient.ai.engine.AiAim init_DataUtil = new autismclient.ai.engine.AiAim();
            final autismclient.ai.engine.AiPlacer init_DataPipe = new autismclient.ai.engine.AiPlacer();
            final JsonArray init_JsonArray = new JsonArray();
            BlockPos init_BlockPos;
            int init_I;
            int load_I;
            int read_I;
            int write_I;
            int update_I;
            int sync_I;
            int flush;
            long load_J = -1L;
            long read_J = -1L;
            boolean init_Z;
            boolean load_Z;
            boolean read_Z;
            boolean write_Z;
            boolean update_Z;
            boolean sync_Z;
            int close = -1;
            String[] init_StrArr = new String[3];

            @Override
            public AiOperation.Result tick() {
               if (!this.load_Z && this.init_Minecraft.player != null && this.init_Minecraft.level != null) {
                  if (System.nanoTime() > this.init_J) {
                     return this.init(AiOperation.Status.TIMED_OUT, "Brewing deadline reached");
                  } else if (this.init_Minecraft.player.getHealth() <= 4.0F && !AiGuard.close()) {
                     return this.init(AiFailure.LOW_HEALTH, AiGuard.init_Str_Str("brewing", this.init_Minecraft.player.getHealth(), this.init_Minecraft.player.getMaxHealth()));
                  } else if (!(this.init_Minecraft.player.containerMenu instanceof BrewingStandMenu menu)) {
                     AiOperation.Result open = this.load_AiOperation_Result_4();
                     return open != null ? open : this.init("Opening the brewing stand");
                  } else {
                     if (this.close != menu.containerId) {
                        for (int slot = 0; slot <= 4; slot++) {
                           if (!menu.getSlot(slot).getItem().isEmpty()) {
                              return this.init(
                                 AiFailure.IMPOSSIBLE_STATE,
                                 "The brewing stand is in use: slot "
                                    + slot
                                    + " holds "
                                    + BuiltInRegistries.ITEM.getKey(menu.getSlot(slot).getItem().getItem())
                                    + "; empty it first"
                              );
                           }
                        }

                        this.close = menu.containerId;
                     }

                     AiOperation.Result missing = this.init_AiOperation_Result_5();
                     if (missing != null) {
                        return missing;
                     }

                     if (!this.read_Z) {
                        if (!menu.getSlot(4).getItem().isEmpty()) {
                           this.read_Z = true;
                           this.sync_I = 0;
                           return this.init("Fuel loaded");
                        } else if (this.init(menu, Items.BLAZE_POWDER)) {
                           this.sync_I = 0;
                           return this.init("Loading blaze powder fuel");
                        } else {
                           return this.init("minecraft:blaze_powder", 1, "Loading blaze powder fuel");
                        }
                     } else if (this.write_Z) {
                        if (!this.update_Z) {
                           if (!menu.getSlot(3).getItem().isEmpty()) {
                              this.update_Z = true;
                              this.sync_I = 0;
                              this.load_J = AiServerEvidence.init_J_I(menu.containerId);
                              this.write_I = 0;
                              return this.init("Brewing (400 ticks)");
                           } else if (this.init(menu, ingredient.items().iterator().next())) {
                              this.sync_I = 0;
                              return this.init("Loading the ingredient");
                           } else {
                              return this.init(ingredientId, 1, "Loading the ingredient");
                           }
                        } else if (!this.sync_Z) {
                           long revision = AiServerEvidence.init_J_I(menu.containerId);
                           if (revision > this.load_J) {
                              this.load_J = revision;
                              this.write_I = 0;
                           } else if (++this.write_I > 120) {
                              return this.init(
                                 AiFailure.IMPOSSIBLE_STATE,
                                 "Nothing brewable: " + ingredientId + " does not apply to the input bottles",
                                 AiJson.init_JsonObject_ObjArr("reason", "no_progress")
                              );
                           }

                           if (this.init_Z(menu)) {
                              this.read_J = AiServerEvidence.init_J_I(menu.containerId);
                              this.init_V(menu);
                              this.sync_Z = true;
                              this.flush = 0;
                              return this.init("Collecting the finished potions");
                           } else {
                              return this.init("Brewing (400 ticks)");
                           }
                        } else {
                           boolean emptied = true;

                           for (int slot = 0; slot <= 2; slot++) {
                              ItemStack bottle = menu.getSlot(slot).getItem();
                              if (!bottle.isEmpty() && !AiBrew.init(bottle).equals(this.init_StrArr[slot])) {
                                 emptied = false;
                              }
                           }

                           if (AiServerEvidence.init_J_I(menu.containerId) > this.read_J && emptied) {
                              return this.init(AiOperation.Status.SUCCEEDED, "Brewed " + this.update_I + " potion(s)");
                           } else {
                              return ++this.flush > 40
                                 ? this.init(
                                    AiOperation.Status.UNCERTAIN,
                                    emptied
                                       ? "The potion collection was not confirmed by the server; reobserve before repeating"
                                       : "The potions are still in the stand (inventory full?); take them before repeating"
                                 )
                                 : this.init("Waiting for the server to confirm the collection");
                           }
                        }
                     } else {
                        int inserted = 0;

                        for (int slot = 0; slot <= 2; slot++) {
                           if (!menu.getSlot(slot).getItem().isEmpty()) {
                              inserted++;
                           }
                        }

                        if (inserted >= count) {
                           this.write_Z = true;

                           for (int slot = 0; slot <= 2; slot++) {
                              this.init_StrArr[slot] = AiBrew.init(menu.getSlot(slot).getItem());
                           }
                        } else if (!this.load(menu)) {
                           return this.init(
                              AiOperation.Status.FAILED,
                              "No " + inputSpec + " bottles left to load",
                              AiJson.init_JsonObject_ObjArr("missing", AiJson.init_JsonObject_ObjArr("minecraft:potion", count - inserted))
                           );
                        }

                        return this.init("Loading input bottles");
                     }
                  }
               } else {
                  return this.init(AiOperation.Status.CANCELLED, "Brewing cancelled; committed stand contents stay");
               }
            }

            boolean init_Z(BrewingStandMenu menu) {
               if (!menu.getSlot(3).getItem().isEmpty()) {
                  return false;
               }

               for (int slot = 0; slot <= 2; slot++) {
                  ItemStack bottle = menu.getSlot(slot).getItem();
                  if (!bottle.isEmpty() && !AiBrew.init(bottle).equals(this.init_StrArr[slot])) {
                     return true;
                  }
               }

               return false;
            }

            void init_V(BrewingStandMenu menu) {
               for (int slot = 0; slot <= 2; slot++) {
                  ItemStack bottle = menu.getSlot(slot).getItem();
                  if (!bottle.isEmpty() && !AiBrew.init(bottle).equals(this.init_StrArr[slot])) {
                     this.init_JsonArray.add(bottle.getHoverName().getString());
                     this.update_I++;
                     AiContainerTransactions.init(menu.containerId, slot, 0, ContainerInput.QUICK_MOVE);
                  }
               }
            }

            AiOperation.Result init(String itemId, int amount, String stage) {
               if (++this.sync_I > 20) {
                  JsonObject missing = new JsonObject();
                  missing.addProperty(itemId, amount);
                  return this.init(AiFailure.MISSING_MATERIALS, "Could not move " + itemId + " into the stand", AiJson.init_JsonObject_ObjArr("missing", missing));
               } else {
                  return this.init(stage);
               }
            }

            AiOperation.Result init_AiOperation_Result_5() {
               if (!this.read_Z && !this.update_Z && this.init_I_3() <= 0) {
                  JsonObject missing = new JsonObject();
                  if (this.load_I_2() + this.init_I_3() < count) {
                     missing.addProperty("minecraft:potion(" + inputSpec + ")", count - this.load_I_2() - this.init_I_3());
                  }

                  int ingredientHeld = AiGameState.load(ingredientId);
                  int blazeHeld = AiGameState.load("minecraft:blaze_powder");
                  if (ingredientId.equals("minecraft:blaze_powder")) {
                     if (ingredientHeld < 2) {
                        missing.addProperty("minecraft:blaze_powder", 2 - ingredientHeld);
                     }
                  } else {
                     if (ingredientHeld < 1) {
                        missing.addProperty(ingredientId, 1);
                     }

                     if (blazeHeld < 1) {
                        missing.addProperty("minecraft:blaze_powder", 1);
                     }
                  }

                  return missing.size() > 0 ? this.init(AiFailure.MISSING_MATERIALS, "Missing brewing materials", AiJson.init_JsonObject_ObjArr("missing", missing)) : null;
               } else {
                  return null;
               }
            }

            int init_I_3() {
               if (this.write_Z) {
                  return count;
               }

               if (this.init_Minecraft.player.containerMenu instanceof BrewingStandMenu menu) {
                  int var4 = 0;

                  for (int slot = 0; slot <= 2; slot++) {
                     if (!menu.getSlot(slot).getItem().isEmpty()) {
                        var4++;
                     }
                  }

                  return var4;
               } else {
                  return 0;
               }
            }

            int load_I_2() {
               int found = 0;

               for (int slot = 0; slot < 36; slot++) {
                  ItemStack stack = this.init_Minecraft.player.getInventory().getItem(slot);
                  if (!stack.isEmpty() && stack.is(Items.POTION) && (inputType.isEmpty() || AiBrew.init(stack).equals(inputType))) {
                     found++;
                  }
               }

               return found;
            }

            boolean load(BrewingStandMenu menu) {
               for (int slot = 0; slot < 36; slot++) {
                  ItemStack stack = this.init_Minecraft.player.getInventory().getItem(slot);
                  if (!stack.isEmpty() && stack.is(Items.POTION) && (inputType.isEmpty() || AiBrew.init(stack).equals(inputType))) {
                     AiContainerTransactions.init(menu.containerId, this.init(slot), 0, ContainerInput.QUICK_MOVE);
                     return true;
                  }
               }

               return false;
            }

            boolean init(BrewingStandMenu menu, Item item) {
               String itemId = BuiltInRegistries.ITEM.getKey(item).toString();

               for (int slot = 0; slot < 36; slot++) {
                  ItemStack stack = this.init_Minecraft.player.getInventory().getItem(slot);
                  if (!stack.isEmpty() && BuiltInRegistries.ITEM.getKey(stack.getItem()).toString().equals(itemId)) {
                     AiContainerTransactions.init(menu.containerId, this.init(slot), 0, ContainerInput.QUICK_MOVE);
                     return true;
                  }
               }

               return false;
            }

            int init(int inventorySlot) {
               return inventorySlot < 9 ? inventorySlot + 32 : inventorySlot - 9 + 5;
            }

            AiOperation.Result load_AiOperation_Result_4() {
               if (this.init_BlockPos == null && this.read_I-- <= 0) {
                  this.init_BlockPos = this.init_BlockPos_2();
                  this.read_I = 40;
               }

               if (this.init_BlockPos == null && this.read_I < 40 && !this.init_Z) {
                  return this.init("Looking for a brewing stand");
               }

               if (this.init_BlockPos == null) {
                  if (!this.init_Z) {
                     if (AiGameState.init_I_Str("minecraft:brewing_stand") <= 0) {
                        JsonObject missing = new JsonObject();
                        missing.addProperty("minecraft:brewing_stand", 1);
                        return this.init(AiFailure.MISSING_STATION, "No brewing stand in reach or in the bag", AiJson.init_JsonObject_ObjArr("missing", missing));
                     }

                     BlockPos spot = AiCraft.init_BlockPos();
                     if (spot == null) {
                        return this.init(AiOperation.Status.FAILED, "No free supported spot next to the player for a brewing stand");
                     }

                     this.init_DataPipe
                        .init_V_BlockPos_Item(spot, BuiltInRegistries.ITEM.getOptional(Identifier.parse("minecraft:brewing_stand")).orElseThrow());
                     this.init_Z = true;
                     this.load_I = 0;
                  }

                  autismclient.ai.engine.AiPlacer.Inner_DataUtil status = this.init_DataPipe.init_DataPipe_DataUtil();
                  if (status == autismclient.ai.engine.AiPlacer.Inner_DataUtil.PLACED) {
                     this.init_BlockPos = this.init_DataPipe.init_BlockPos();
                     this.init_Z = false;
                     return this.init("Brewing stand placed");
                  }

                  if (status != autismclient.ai.engine.AiPlacer.Inner_DataUtil.FAILED && ++this.load_I <= 100) {
                     return this.init("Placing a brewing stand");
                  }

                  this.init_Z = false;
                  return this.init(AiOperation.Status.FAILED, "Could not place a brewing stand: " + this.init_DataPipe.init_Str());
               } else {
                  double reach = Math.max(4.5, this.init_Minecraft.player.blockInteractionRange());
                  if (this.init_Minecraft.player.getEyePosition().distanceToSqr(Vec3.atCenterOf(this.init_BlockPos)) > reach * reach) {
                     return this.init(AiOperation.Status.FAILED, "The brewing stand fell out of reach");
                  }

                  this.init_DataUtil.init_V_Vec3(Vec3.atCenterOf(this.init_BlockPos));
                  this.init_DataUtil.load_V();
                  if (++this.init_I > 80) {
                     return this.init(AiOperation.Status.FAILED, "Could not open the brewing stand");
                  }

                  if (!autismclient.ai.engine.AiAim.init_Z_BlockPos(this.init_BlockPos)) {
                     return this.init("Turning toward the brewing stand");
                  }

                  BlockHitResult hit = autismclient.ai.engine.AiAim.init_BlockHitResult();
                  if (hit == null) {
                     return this.init("Turning toward the brewing stand");
                  }

                  this.init_Minecraft.gameMode.useItemOn(this.init_Minecraft.player, InteractionHand.MAIN_HAND, hit);
                  Compat263.swing(this.init_Minecraft.player, InteractionHand.MAIN_HAND);
                  return null;
               }
            }

            BlockPos init_BlockPos_2() {
               Block wanted = AiCraft.init_Block("minecraft:brewing_stand");
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

            JsonObject init_JsonObject_7() {
               return AiJson.init_JsonObject_ObjArr(
                  "ingredient",
                  ingredientId,
                  "input",
                  inputSpec,
                  "count",
                  count,
                  "brewed",
                  this.update_I,
                  "output",
                  this.init_JsonArray,
                  "fuelUsed",
                  !this.read_Z && !this.sync_Z ? 0 : 1,
                  "stationAt",
                  this.init_BlockPos == null ? null : this.init_BlockPos.toShortString()
               );
            }

            AiOperation.Result init(String message) {
               return new AiOperation.Result(AiOperation.Status.RUNNING, message, this.init_JsonObject_7().toString(), List.of());
            }

            AiOperation.Result init(AiOperation.Status status, String message) {
               return this.init(status, message, null);
            }

            AiOperation.Result init(AiFailure code, String message) {
               return this.init(code, message, null);
            }

            AiOperation.Result init(AiFailure code, String message, JsonObject extra) {
               JsonObject data = this.init_JsonObject_7();
               if (extra != null) {
                  for (Entry<String, JsonElement> entry : extra.entrySet()) {
                     data.add(entry.getKey(), entry.getValue());
                  }
               }

               this.cancel();
               return AiOperation.Result.failed(code, message, data.toString());
            }

            AiOperation.Result init(AiOperation.Status status, String message, JsonObject extra) {
               JsonObject data = this.init_JsonObject_7();
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
               }
            }
         };
      } else {
         throw new IllegalArgumentException("Brewing requires a connected living player");
      }
   }

   static String init(String spec) {
      String clean = spec == null ? "" : spec.trim().toLowerCase(Locale.ROOT);
      if (clean.isEmpty() || clean.equals("water_bottle")) {
         return "minecraft:water";
      } else if (clean.equals("potion") || clean.equals("minecraft:potion")) {
         return "";
      } else if (clean.startsWith("potion:")) {
         return "minecraft:" + clean.substring("potion:".length());
      } else if (clean.startsWith("minecraft:potion:")) {
         return clean;
      } else if (clean.endsWith("_potion")) {
         return "minecraft:" + clean.substring(0, clean.length() - "_potion".length());
      } else {
         return clean.contains(":") ? clean : "minecraft:" + clean;
      }
   }

   static String init(ItemStack stack) {
      if (stack != null && !stack.isEmpty()) {
         PotionContents contents = stack.get(DataComponents.POTION_CONTENTS);
         if (contents == null) {
            return "";
         }

         Optional<Holder<Potion>> potion = contents.potion();
         return potion.isEmpty() ? "" : potion.get().unwrapKey().map(key -> key.identifier().toString()).orElse("");
      } else {
         return "";
      }
   }
}

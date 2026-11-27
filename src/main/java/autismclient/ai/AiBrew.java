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
         String ingredientSpec = AiJson.getString(args, "ingredient", "");
         if (ingredientSpec.isBlank()) {
            throw new IllegalArgumentException("ingredient is required (e.g. nether_wart, blaze_powder)");
         }

         final AiItems.ItemSpec ingredient = AiItems.resolveItem(ingredientSpec);
         if (ingredient.items().size() != 1) {
            throw new IllegalArgumentException(
               "ingredient must resolve to a single item id, got " + ingredient.items().size() + " from \"" + ingredientSpec + "\""
            );
         }

         final String inputSpec = AiJson.getString(args, "input", "water_bottle");
         final String inputType = init(inputSpec);
         final int count = Math.max(1, Math.min(3, AiJson.getInt(args, "count", 3)));
         final int timeoutSeconds = Math.max(10, Math.min(3600, AiJson.getInt(args, "timeoutSeconds", 300)));
         final String ingredientId = BuiltInRegistries.ITEM.getKey(ingredient.items().iterator().next()).toString();
         return new AiOperation() {
            final Minecraft mc = Minecraft.getInstance();
            final long deadlineNanos = System.nanoTime() + timeoutSeconds * 1000000000L;
            final autismclient.ai.engine.AiAim aim = new autismclient.ai.engine.AiAim();
            final autismclient.ai.engine.AiPlacer placer = new autismclient.ai.engine.AiPlacer();
            final JsonArray brewedNames = new JsonArray();
            BlockPos brewingStand;
            int init_I;
            int placeTicks;
            int standSearchCooldown;
            int write_I;
            int brewed;
            int sync_I;
            int flush;
            long revision = -1L;
            long containerRevision = -1L;
            boolean init_Z;
            boolean cancelled;
            boolean read_Z;
            boolean write_Z;
            boolean update_Z;
            boolean sync_Z;
            int close = -1;
            String[] bottleKeys = new String[3];

            @Override
            public AiOperation.Result tick() {
               if (!this.cancelled && this.mc.player != null && this.mc.level != null) {
                  if (System.nanoTime() > this.deadlineNanos) {
                     return this.init(AiOperation.Status.TIMED_OUT, "Brewing deadline reached");
                  } else if (this.mc.player.getHealth() <= 4.0F && !AiGuard.isLowHealthOverrideActive()) {
                     return this.init(AiFailure.LOW_HEALTH, AiGuard.formatLowHealthAbort("brewing", this.mc.player.getHealth(), this.mc.player.getMaxHealth()));
                  } else if (!(this.mc.player.containerMenu instanceof BrewingStandMenu menu)) {
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
                              this.revision = AiServerEvidence.containerRevision(menu.containerId);
                              this.write_I = 0;
                              return this.init("Brewing (400 ticks)");
                           } else if (this.init(menu, ingredient.items().iterator().next())) {
                              this.sync_I = 0;
                              return this.init("Loading the ingredient");
                           } else {
                              return this.init(ingredientId, 1, "Loading the ingredient");
                           }
                        } else if (!this.sync_Z) {
                           long revision = AiServerEvidence.containerRevision(menu.containerId);
                           if (revision > this.revision) {
                              this.revision = revision;
                              this.write_I = 0;
                           } else if (++this.write_I > 120) {
                              return this.init(
                                 AiFailure.IMPOSSIBLE_STATE,
                                 "Nothing brewable: " + ingredientId + " does not apply to the input bottles",
                                 AiJson.objectOf("reason", "no_progress")
                              );
                           }

                           if (this.init_Z(menu)) {
                              this.containerRevision = AiServerEvidence.containerRevision(menu.containerId);
                              this.recordBrewed(menu);
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
                              if (!bottle.isEmpty() && !AiBrew.init(bottle).equals(this.bottleKeys[slot])) {
                                 emptied = false;
                              }
                           }

                           if (AiServerEvidence.containerRevision(menu.containerId) > this.containerRevision && emptied) {
                              return this.init(AiOperation.Status.SUCCEEDED, "Brewed " + this.brewed + " potion(s)");
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
                              this.bottleKeys[slot] = AiBrew.init(menu.getSlot(slot).getItem());
                           }
                        } else if (!this.load(menu)) {
                           return this.init(
                              AiOperation.Status.FAILED,
                              "No " + inputSpec + " bottles left to load",
                              AiJson.objectOf("missing", AiJson.objectOf("minecraft:potion", count - inserted))
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
                  if (!bottle.isEmpty() && !AiBrew.init(bottle).equals(this.bottleKeys[slot])) {
                     return true;
                  }
               }

               return false;
            }

            void recordBrewed(BrewingStandMenu menu) {
               for (int slot = 0; slot <= 2; slot++) {
                  ItemStack bottle = menu.getSlot(slot).getItem();
                  if (!bottle.isEmpty() && !AiBrew.init(bottle).equals(this.bottleKeys[slot])) {
                     this.brewedNames.add(bottle.getHoverName().getString());
                     this.brewed++;
                     AiContainerTransactions.sendPredictedClick(menu.containerId, slot, 0, ContainerInput.QUICK_MOVE);
                  }
               }
            }

            AiOperation.Result init(String itemId, int amount, String stage) {
               if (++this.sync_I > 20) {
                  JsonObject missing = new JsonObject();
                  missing.addProperty(itemId, amount);
                  return this.init(AiFailure.MISSING_MATERIALS, "Could not move " + itemId + " into the stand", AiJson.objectOf("missing", missing));
               } else {
                  return this.init(stage);
               }
            }

            AiOperation.Result init_AiOperation_Result_5() {
               if (!this.read_Z && !this.update_Z && this.init_I_3() <= 0) {
                  JsonObject missing = new JsonObject();
                  if (this.inventoryPotions() + this.init_I_3() < count) {
                     missing.addProperty("minecraft:potion(" + inputSpec + ")", count - this.inventoryPotions() - this.init_I_3());
                  }

                  int ingredientHeld = AiGameState.countMainInventoryItem(ingredientId);
                  int blazeHeld = AiGameState.countMainInventoryItem("minecraft:blaze_powder");
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

                  return missing.size() > 0 ? this.init(AiFailure.MISSING_MATERIALS, "Missing brewing materials", AiJson.objectOf("missing", missing)) : null;
               } else {
                  return null;
               }
            }

            int init_I_3() {
               if (this.write_Z) {
                  return count;
               }

               if (this.mc.player.containerMenu instanceof BrewingStandMenu menu) {
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

            int inventoryPotions() {
               int found = 0;

               for (int slot = 0; slot < 36; slot++) {
                  ItemStack stack = this.mc.player.getInventory().getItem(slot);
                  if (!stack.isEmpty() && stack.is(Items.POTION) && (inputType.isEmpty() || AiBrew.init(stack).equals(inputType))) {
                     found++;
                  }
               }

               return found;
            }

            boolean load(BrewingStandMenu menu) {
               for (int slot = 0; slot < 36; slot++) {
                  ItemStack stack = this.mc.player.getInventory().getItem(slot);
                  if (!stack.isEmpty() && stack.is(Items.POTION) && (inputType.isEmpty() || AiBrew.init(stack).equals(inputType))) {
                     AiContainerTransactions.sendPredictedClick(menu.containerId, this.init(slot), 0, ContainerInput.QUICK_MOVE);
                     return true;
                  }
               }

               return false;
            }

            boolean init(BrewingStandMenu menu, Item item) {
               String itemId = BuiltInRegistries.ITEM.getKey(item).toString();

               for (int slot = 0; slot < 36; slot++) {
                  ItemStack stack = this.mc.player.getInventory().getItem(slot);
                  if (!stack.isEmpty() && BuiltInRegistries.ITEM.getKey(stack.getItem()).toString().equals(itemId)) {
                     AiContainerTransactions.sendPredictedClick(menu.containerId, this.init(slot), 0, ContainerInput.QUICK_MOVE);
                     return true;
                  }
               }

               return false;
            }

            int init(int inventorySlot) {
               return inventorySlot < 9 ? inventorySlot + 32 : inventorySlot - 9 + 5;
            }

            AiOperation.Result load_AiOperation_Result_4() {
               if (this.brewingStand == null && this.standSearchCooldown-- <= 0) {
                  this.brewingStand = this.findBrewingStand();
                  this.standSearchCooldown = 40;
               }

               if (this.brewingStand == null && this.standSearchCooldown < 40 && !this.init_Z) {
                  return this.init("Looking for a brewing stand");
               }

               if (this.brewingStand == null) {
                  if (!this.init_Z) {
                     if (AiGameState.countInventoryItem("minecraft:brewing_stand") <= 0) {
                        JsonObject missing = new JsonObject();
                        missing.addProperty("minecraft:brewing_stand", 1);
                        return this.init(AiFailure.MISSING_STATION, "No brewing stand in reach or in the bag", AiJson.objectOf("missing", missing));
                     }

                     BlockPos spot = AiCraft.findPlacementSpot();
                     if (spot == null) {
                        return this.init(AiOperation.Status.FAILED, "No free supported spot next to the player for a brewing stand");
                     }

                     this.placer
                        .start(spot, BuiltInRegistries.ITEM.getOptional(Identifier.parse("minecraft:brewing_stand")).orElseThrow());
                     this.init_Z = true;
                     this.placeTicks = 0;
                  }

                  autismclient.ai.engine.AiPlacer.PlaceStatus status = this.placer.tick();
                  if (status == autismclient.ai.engine.AiPlacer.PlaceStatus.PLACED) {
                     this.brewingStand = this.placer.targetPos();
                     this.init_Z = false;
                     return this.init("Brewing stand placed");
                  }

                  if (status != autismclient.ai.engine.AiPlacer.PlaceStatus.FAILED && ++this.placeTicks <= 100) {
                     return this.init("Placing a brewing stand");
                  }

                  this.init_Z = false;
                  return this.init(AiOperation.Status.FAILED, "Could not place a brewing stand: " + this.placer.failureReason());
               } else {
                  double reach = Math.max(4.5, this.mc.player.blockInteractionRange());
                  if (this.mc.player.getEyePosition().distanceToSqr(Vec3.atCenterOf(this.brewingStand)) > reach * reach) {
                     return this.init(AiOperation.Status.FAILED, "The brewing stand fell out of reach");
                  }

                  this.aim.aimAt(Vec3.atCenterOf(this.brewingStand));
                  this.aim.tick();
                  if (++this.init_I > 80) {
                     return this.init(AiOperation.Status.FAILED, "Could not open the brewing stand");
                  }

                  if (!autismclient.ai.engine.AiAim.isLookingAtBlock(this.brewingStand)) {
                     return this.init("Turning toward the brewing stand");
                  }

                  BlockHitResult hit = autismclient.ai.engine.AiAim.raycastLookBlock();
                  if (hit == null) {
                     return this.init("Turning toward the brewing stand");
                  }

                  this.mc.gameMode.useItemOn(this.mc.player, InteractionHand.MAIN_HAND, hit);
                  Compat263.swing(this.mc.player, InteractionHand.MAIN_HAND);
                  return null;
               }
            }

            BlockPos findBrewingStand() {
               Block wanted = AiCraft.requireBlock("minecraft:brewing_stand");
               BlockPos origin = this.mc.player.blockPosition();
               BlockPos best = null;
               double bestDistance = 1.7976931348623157E308;
               int radius = 16;

               for (BlockPos pos : BlockPos.betweenClosed(origin.offset(-radius, -8, -radius), origin.offset(radius, 8, radius))) {
                  if (AiTerrain.isLoaded(this.mc.level, pos) && this.mc.level.getBlockState(pos).getBlock() == wanted) {
                     double distance = this.mc.player.distanceToSqr(Vec3.atCenterOf(pos));
                     if (distance < bestDistance) {
                        bestDistance = distance;
                        best = pos.immutable();
                     }
                  }
               }

               return best;
            }

            JsonObject resultData() {
               return AiJson.objectOf(
                  "ingredient",
                  ingredientId,
                  "input",
                  inputSpec,
                  "count",
                  count,
                  "brewed",
                  this.brewed,
                  "output",
                  this.brewedNames,
                  "fuelUsed",
                  !this.read_Z && !this.sync_Z ? 0 : 1,
                  "stationAt",
                  this.brewingStand == null ? null : this.brewingStand.toShortString()
               );
            }

            AiOperation.Result init(String message) {
               return new AiOperation.Result(AiOperation.Status.RUNNING, message, this.resultData().toString(), List.of());
            }

            AiOperation.Result init(AiOperation.Status status, String message) {
               return this.init(status, message, null);
            }

            AiOperation.Result init(AiFailure code, String message) {
               return this.init(code, message, null);
            }

            AiOperation.Result init(AiFailure code, String message, JsonObject extra) {
               JsonObject data = this.resultData();
               if (extra != null) {
                  for (Entry<String, JsonElement> entry : extra.entrySet()) {
                     data.add(entry.getKey(), entry.getValue());
                  }
               }

               this.cancel();
               return AiOperation.Result.failed(code, message, data.toString());
            }

            AiOperation.Result init(AiOperation.Status status, String message, JsonObject extra) {
               JsonObject data = this.resultData();
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
               if (!this.cancelled) {
                  this.cancelled = true;
                  this.aim.clear();
                  this.placer.reset();
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

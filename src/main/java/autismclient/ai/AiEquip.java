package autismclient.ai;

import autismclient.api.ai.AiOperation;
import autismclient.modules.Module;
import com.google.gson.JsonObject;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import net.minecraft.client.Minecraft;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.inventory.ContainerInput;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

final class AiEquip {
   private static final int init_I = 40;
   private static final int load = 400;
   private static final Map<EquipmentSlot, Integer> ARMOR_MENU_SLOTS = Map.of(
      EquipmentSlot.HEAD, 5, EquipmentSlot.CHEST, 6, EquipmentSlot.LEGS, 7, EquipmentSlot.FEET, 8
   );

   private AiEquip() {
   }

   static AiOperation create(JsonObject args) {
      Minecraft mc = Minecraft.getInstance();
      if (mc.player != null && mc.level != null) {
         String what = AiJson.getString(args, "what", "").trim().toLowerCase(Locale.ROOT);
         if (what.isEmpty()) {
            throw new IllegalArgumentException("what is required: armor, weapon, tool, shield, or an item spec");
         }

         String prefer = AiJson.getString(args, "prefer", "").trim();
         if ((what.equals("armor") || what.equals("armour")) && AiRunResources.isCampaignActive()) {
            prefer = "minecraft:netherite_helmet,minecraft:diamond_helmet,minecraft:netherite_chestplate,minecraft:diamond_chestplate,minecraft:netherite_leggings,minecraft:diamond_leggings,minecraft:golden_boots";
         }

         if (!prefer.isEmpty() && !what.equals("armor") && !what.equals("armour")) {
            throw new IllegalArgumentException("prefer only applies to what:armor");
         }

         String hand = normalizeHand(AiJson.getString(args, "hand", "main"));
         if (hand.equals("offhand")) {
            if (!what.equals("armor") && !what.equals("armour") && !what.equals("weapon") && !what.equals("tool")) {
               return equipOffhand(AiItems.resolveItem(what), what);
            } else {
               throw new IllegalArgumentException("hand:offhand equips a specific item spec (e.g. shield), not " + what);
            }
         } else {
            if (what.equals("armor") || what.equals("armour")) {
               return equipArmor(prefer);
            }

            if (what.equals("weapon")) {
               return holdSlot(AiItems.bestWeaponSlot(36), "weapon");
            }

            if (what.equals("tool")) {
               String blockSpec = AiJson.getString(args, "forBlock", "");
               if (blockSpec.isBlank()) {
                  throw new IllegalArgumentException("tool needs forBlock, the block the tool is for");
               }

               AiItems.ItemSpec block = AiItems.resolveBlock(blockSpec);
               BlockState state = block.blocks().iterator().next().defaultBlockState();
               return holdSlot(AiItems.bestToolFor(state, 36), "tool for " + blockSpec);
            } else {
               return holdItem(AiItems.resolveItem(what), what);
            }
         }
      } else {
         throw new IllegalArgumentException("Equipping requires a connected living player");
      }
   }

   static String normalizeHand(String raw) {
      String hand = raw == null ? "main" : raw.trim().toLowerCase(Locale.ROOT);
      if (hand.isEmpty()) {
         return "main";
      } else if (!hand.equals("main") && !hand.equals("offhand")) {
         throw new IllegalArgumentException("hand must be main or offhand");
      } else {
         return hand;
      }
   }

   private static AiOperation equipOffhand(final AiItems.ItemSpec target, final String what) {
      final Minecraft mc = Minecraft.getInstance();
      return new AiOperation() {
         long repliesAtClick = -1L;
         final Object startLevel = mc.level;
         final Object startConnection = mc.getConnection();
         final Object startPlayer = mc.player;
         int waitTicks;
         boolean swapSent;
         boolean done;

         @Override
         public AiOperation.Result tick() {
            if (!this.done && mc.player != null) {
               if (mc.level == this.startLevel && mc.getConnection() == this.startConnection && mc.player == this.startPlayer && mc.player.isAlive()) {
                  if (!target.matches(mc.player.getOffhandItem()) || this.swapSent && AiServerEvidence.containerReplyCount(mc.player.inventoryMenu.containerId) <= this.repliesAtClick) {
                     if (!this.swapSent) {
                        if (mc.player.containerMenu == mc.player.inventoryMenu && mc.player.containerMenu.getCarried().isEmpty()) {
                           int source = AiItems.findSlot(target, 36);
                           if (source < 0) {
                              this.done = true;
                              return AiOperation.Result.failed(
                                 AiFailure.MISSING_MATERIALS,
                                 "No " + what + " in inventory",
                                 AiJson.objectOf("what", what, "matches", AiJson.GSON.toJsonTree(target.itemIds())).toString()
                              );
                           }

                           this.repliesAtClick = AiServerEvidence.containerReplyCount(mc.player.inventoryMenu.containerId);
                           int menuSlot = autismclient.util.InventorySlotHelper.toScreenSlot(mc, source);
                           this.swapSent = menuSlot >= 0;
                           if (this.swapSent) {
                              AiContainerTransactions.sendPredictedClick(mc.player.inventoryMenu.containerId, menuSlot, 40, ContainerInput.SWAP);
                           }

                           if (!this.swapSent) {
                              this.done = true;
                              return AiOperation.Result.failed(
                                 AiFailure.IMPOSSIBLE_STATE,
                                 "Could not move " + what + " to the offhand (the cursor must be empty)",
                                 AiJson.objectOf("what", what).toString()
                              );
                           } else {
                              return this.running("Moving " + what + " to the offhand");
                           }
                        } else {
                           return AiGameOperations.result(
                              AiOperation.Status.FAILED, "Offhand swap requires the player inventory with an empty cursor", new JsonObject()
                           );
                        }
                     } else if (++this.waitTicks > 40) {
                        this.done = true;
                        return AiGameOperations.result(
                           AiOperation.Status.UNCERTAIN,
                           "The offhand swap went out but no applied server inventory confirmation arrived",
                           AiJson.objectOf("what", what)
                        );
                     } else {
                        return this.running("Waiting for applied server inventory confirmation");
                     }
                  } else {
                     this.done = true;
                     return AiGameOperations.result(
                        AiOperation.Status.SUCCEEDED, "Offhand holds " + AiEquip.offhandItemId(), AiJson.objectOf("what", what, "hand", "offhand", "held", AiEquip.offhandItemId())
                     );
                  }
               } else {
                  return AiGameOperations.result(AiOperation.Status.UNCERTAIN, "Offhand equipment player or world changed", new JsonObject());
               }
            } else {
               return AiGameOperations.result(AiOperation.Status.CANCELLED, "Equip cancelled", new JsonObject());
            }
         }

         AiOperation.Result running(String message) {
            return AiGameOperations.result(AiOperation.Status.RUNNING, message, AiJson.objectOf("what", what, "hand", "offhand"));
         }

         @Override
         public void cancel() {
            this.done = true;
         }

         @Override
         public boolean canInterruptNow() {
            return !this.swapSent || this.done;
         }
      };
   }

   private static String offhandItemId() {
      Minecraft mc = Minecraft.getInstance();
      if (mc.player == null) {
         return "";
      }

      ItemStack stack = mc.player.getOffhandItem();
      return stack.isEmpty() ? "minecraft:air" : BuiltInRegistries.ITEM.getKey(stack.getItem()).toString();
   }

   private static AiOperation holdSlot(int slot, String description) {
      return holdInHand(slot, null, description);
   }

   private static AiOperation holdItem(AiItems.ItemSpec target, String description) {
      return holdInHand(-1, target, description);
   }

   private static AiOperation holdInHand(final int plannedSlot, final AiItems.ItemSpec target, final String description) {
      final Minecraft mc = Minecraft.getInstance();
      return new AiOperation() {
         final Object startLevel = mc.level;
         final Object startConnection = mc.getConnection();
         final Object startPlayer = mc.player;
         boolean done;
         boolean clickSent;
         int hotbarSlot = -1;
         int waitTicks;
         long repliesAtClick;
         ItemStack expectedStack = ItemStack.EMPTY;

         @Override
         public AiOperation.Result tick() {
            if (!this.done && mc.player != null) {
               if (mc.level != this.startLevel || mc.getConnection() != this.startConnection || mc.player != this.startPlayer || !mc.player.isAlive()) {
                  return this.finish(AiOperation.Status.UNCERTAIN, "Hand equipment player or world changed");
               }

               if (mc.player.containerMenu != mc.player.inventoryMenu || !mc.player.inventoryMenu.getCarried().isEmpty()) {
                  return this.finish(
                     this.clickSent ? AiOperation.Status.UNCERTAIN : AiOperation.Status.FAILED,
                     "Hand equipment requires the player inventory with an empty cursor"
                  );
               }

               if (!this.clickSent) {
                  int source = target == null ? plannedSlot : AiItems.findSlot(target, 36);
                  if (source >= 0 && source < 36 && !mc.player.getInventory().getItem(source).isEmpty()) {
                     this.expectedStack = mc.player.getInventory().getItem(source).copy();
                     this.hotbarSlot = source < 9 ? source : AiItems.pickHotbarDestination();
                     if (source < 9) {
                        autismclient.util.InventorySlotHelper.selectHotbarSlot(mc, this.hotbarSlot);
                        return this.finish(AiOperation.Status.SUCCEEDED, "Holding " + AiEquip.mainHandItemId());
                     }

                     int menuSlot = autismclient.util.InventorySlotHelper.toScreenSlot(mc, source);
                     if (menuSlot < 0) {
                        return this.finish(AiOperation.Status.FAILED, "Could not resolve the equipment's inventory slot");
                     }

                     this.repliesAtClick = AiServerEvidence.containerReplyCount(mc.player.inventoryMenu.containerId);
                     AiContainerTransactions.sendPredictedClick(mc.player.inventoryMenu.containerId, menuSlot, this.hotbarSlot, ContainerInput.SWAP);
                     this.clickSent = true;
                     return AiGameOperations.result(AiOperation.Status.RUNNING, "Moving " + description + " into the hotbar", this.statusData());
                  } else {
                     return this.finish(AiOperation.Status.FAILED, "No suitable " + description + " in inventory");
                  }
               } else if (AiServerEvidence.containerReplyCount(mc.player.inventoryMenu.containerId) > this.repliesAtClick) {
                  if (!ItemStack.matches(this.expectedStack, mc.player.getInventory().getItem(this.hotbarSlot))) {
                     return this.finish(AiOperation.Status.FAILED, "Server did not apply the expected hotbar swap; no repeat click issued");
                  }

                  autismclient.util.InventorySlotHelper.selectHotbarSlot(mc, this.hotbarSlot);
                  return this.finish(AiOperation.Status.SUCCEEDED, "Holding " + AiEquip.mainHandItemId());
               } else {
                  return ++this.waitTicks > 40
                     ? this.finish(AiOperation.Status.UNCERTAIN, "Hotbar swap lacks an applied server snapshot")
                     : AiGameOperations.result(AiOperation.Status.RUNNING, "Waiting for the hotbar swap to apply", this.statusData());
               }
            } else {
               return this.finish(AiOperation.Status.CANCELLED, "Equip cancelled");
            }
         }

         JsonObject statusData() {
            return AiJson.objectOf("what", description, "slot", this.hotbarSlot, "held", AiEquip.mainHandItemId(), "pending", this.clickSent && !this.done);
         }

         AiOperation.Result finish(AiOperation.Status status, String message) {
            this.done = true;
            return AiGameOperations.result(status, message, this.statusData());
         }

         @Override
         public void cancel() {
            this.done = true;
         }

         @Override
         public boolean canInterruptNow() {
            return !this.clickSent || this.done;
         }
      };
   }

   private static AiOperation equipArmor(final String preferSpec) {
      final Minecraft mc = Minecraft.getInstance();
      Module module = autismclient.modules.al.init_Module_Str("auto-armor");
      final boolean preferElytra = !AiRunResources.isCampaignActive() && module != null && "true".equals(module.value("prefer-elytra"));
      final boolean allowCursed = module != null && "true".equals(module.value("allow-cursed"));
      final boolean swapArmor = module != null && "true".equals(module.value("can-swap-armor"));
      final boolean hotbarSwap = module == null || !"false".equals(module.value("hotbar"));
      boolean saveArmor = module != null && "true".equals(module.value("save-armor"));
      final int threshold = Math.max(saveArmor ? durabilityThreshold(module) : -2147483648, AiRunResources.isCampaignActive() ? 32 : -2147483648);
      final AiItems.ItemSpec prefer = preferSpec.isEmpty() ? null : parsePreferSpec(preferSpec);
      return new AiOperation() {
         final String wornBefore = AiEquip.wornArmorSummary();
         final Object startLevel = mc.level;
         final Object startConnection = mc.getConnection();
         final Object startPlayer = mc.player;
         ItemStack expectedStack = ItemStack.EMPTY;
         EquipmentSlot movedSlot;
         int ticks;
         int armorMoves;
         long repliesAtClick = -1L;
         int waitTicks;
         boolean movePending;
         boolean done;
         boolean preferChecked;

         @Override
         public AiOperation.Result tick() {
            if (!this.done && mc.player != null) {
               if (mc.level != this.startLevel || mc.getConnection() != this.startConnection || mc.player != this.startPlayer || !mc.player.isAlive()) {
                  return this.finish(AiOperation.Status.UNCERTAIN, "Armor player or world changed; reconcile any pending move");
               }

               if (++this.ticks > 400) {
                  return this.finish(AiOperation.Status.UNCERTAIN, "Armour equip did not settle in time");
               }

               if (mc.player.containerMenu != mc.player.inventoryMenu) {
                  return this.finish(this.movePending ? AiOperation.Status.UNCERTAIN : AiOperation.Status.FAILED, "Close the open container before equipping armour");
               }

               if (!mc.player.containerMenu.getCarried().isEmpty()) {
                  return this.finish(AiOperation.Status.FAILED, "Clear the cursor before equipping armour");
               }

               if (this.movePending) {
                  if (AiServerEvidence.containerReplyCount(mc.player.inventoryMenu.containerId) <= this.repliesAtClick) {
                     return ++this.waitTicks > 40
                        ? this.finish(AiOperation.Status.UNCERTAIN, "The armour click went out but no applied server inventory confirmation arrived")
                        : this.running("Waiting for applied server inventory confirmation");
                  }

                  if (!ItemStack.matches(this.expectedStack, mc.player.getItemBySlot(this.movedSlot))) {
                     return this.finish(AiOperation.Status.FAILED, "Server did not apply the expected armor move; no repeat click issued");
                  }

                  this.movePending = false;
                  this.armorMoves++;
                  this.waitTicks = 0;
                  return this.running("Armour move applied; re-evaluating");
               } else {
                  if (prefer != null && !this.preferChecked) {
                     this.preferChecked = true;
                     if (AiEquip.preferredCandidates(autismclient.util.ArmorEvaluationHelper.candidatesBySlot(this.inventoryView(), allowCursed, true), prefer).isEmpty()) {
                        JsonObject data = AiJson.objectOf(
                           "what",
                           "armor",
                           "prefer",
                           preferSpec,
                           "matches",
                           AiJson.GSON.toJsonTree(prefer.itemIds()),
                           "worn",
                           AiEquip.wornArmorSummary(),
                           "wornBefore",
                           this.wornBefore
                        );
                        this.cancel();
                        return AiOperation.Result.failed(AiFailure.MISSING_MATERIALS, "prefer '" + preferSpec + "' matches no armor piece, carried or worn", data.toString());
                     }
                  }

                  AiEquip.EquipMove move = this.nextMove();
                  if (move == null) {
                     String now = AiEquip.wornArmorSummary();
                     return this.finish(
                        AiOperation.Status.SUCCEEDED,
                        this.armorMoves == 0 ? "Already wearing the best available armour" : "Equipped " + now + " (" + this.armorMoves + " verified moves)"
                     );
                  }

                  if (move.kind() == AiEquip.MoveKind.CLEAR_WORN && mc.player.getInventory().getFreeSlot() < 0) {
                     return this.finish(AiOperation.Status.FAILED, "No free inventory slot to swap out the worn " + AiEquip.itemId(mc.player.getItemBySlot(move.slot())));
                  }

                  this.repliesAtClick = AiServerEvidence.containerReplyCount(mc.player.inventoryMenu.containerId);
                  AiContainerTransactions.sendPredictedClick(mc.player.containerMenu.containerId, move.menuSlot(), move.button(), move.input());
                  this.movedSlot = move.slot();
                  this.expectedStack = mc.player.getItemBySlot(this.movedSlot).copy();
                  this.movePending = true;
                  this.waitTicks = 0;
                  return this.running("Moving " + move.description());
               }
            } else {
               return this.finish(AiOperation.Status.CANCELLED, "Armour equip cancelled");
            }
         }

         AiEquip.EquipMove nextMove() {
            Map<EquipmentSlot, autismclient.util.ArmorEvaluationHelper.ArmorCandidate> best = AiEquip.bestBySlot(
               autismclient.util.ArmorEvaluationHelper.candidatesBySlot(this.inventoryView(), allowCursed, true), threshold, prefer
            );
            if (preferElytra) {
               autismclient.util.ArmorEvaluationHelper.ArmorCandidate elytra = this.wornElytraCandidate();
               if (elytra != null) {
                  best.put(EquipmentSlot.CHEST, elytra);
               }
            }

            for (EquipmentSlot slot : autismclient.util.ArmorEvaluationHelper.ARMOR_SLOTS) {
               autismclient.util.ArmorEvaluationHelper.ArmorCandidate candidate = best.get(slot);
               if (candidate != null && candidate.kind() != autismclient.util.ArmorEvaluationHelper.SourceKind.ARMOR) {
                  ItemStack wornStack = mc.player.getItemBySlot(slot);
                  if (wornStack.isEmpty() || !wornStack.is(Items.ELYTRA) && !autismclient.util.ArmorEvaluationHelper.hasBindingCurse(wornStack)) {
                     int armorMenuSlot = AiEquip.ARMOR_MENU_SLOTS.get(slot);
                     if (wornStack.isEmpty()) {
                        return candidate.kind() == autismclient.util.ArmorEvaluationHelper.SourceKind.HOTBAR && hotbarSwap
                           ? new AiEquip.EquipMove(
                              slot, armorMenuSlot, candidate.inventorySlot(), ContainerInput.SWAP, AiEquip.MoveKind.MOVE_IN, AiEquip.itemId(candidate.stack())
                           )
                           : new AiEquip.EquipMove(
                              slot, this.screenSlot(candidate), 0, ContainerInput.QUICK_MOVE, AiEquip.MoveKind.MOVE_IN, AiEquip.itemId(candidate.stack())
                           );
                     } else {
                        return candidate.kind() == autismclient.util.ArmorEvaluationHelper.SourceKind.HOTBAR && hotbarSwap && swapArmor
                           ? new AiEquip.EquipMove(
                              slot, armorMenuSlot, candidate.inventorySlot(), ContainerInput.SWAP, AiEquip.MoveKind.MOVE_IN, AiEquip.itemId(candidate.stack())
                           )
                           : new AiEquip.EquipMove(slot, armorMenuSlot, 0, ContainerInput.QUICK_MOVE, AiEquip.MoveKind.CLEAR_WORN, AiEquip.itemId(wornStack));
                     }
                  }
               }
            }

            return null;
         }

         autismclient.util.ArmorEvaluationHelper.StackSource inventoryView() {
            return new autismclient.util.ArmorEvaluationHelper.StackSource() {
               @Override
               public ItemStack init(int index) {
                  return mc.player.getInventory().getItem(index);
               }

               @Override
               public ItemStack init(EquipmentSlot slot) {
                  return mc.player.getItemBySlot(slot);
               }
            };
         }

         autismclient.util.ArmorEvaluationHelper.ArmorCandidate wornElytraCandidate() {
            ItemStack wornChest = mc.player.getItemBySlot(EquipmentSlot.CHEST);
            if (wornChest.is(Items.ELYTRA)) {
               return new autismclient.util.ArmorEvaluationHelper.ArmorCandidate(wornChest, EquipmentSlot.CHEST, autismclient.util.ArmorEvaluationHelper.SourceKind.ARMOR, -1);
            }

            for (int inv = 0; inv < 36; inv++) {
               ItemStack stack = mc.player.getInventory().getItem(inv);
               if (stack.is(Items.ELYTRA)) {
                  return new autismclient.util.ArmorEvaluationHelper.ArmorCandidate(
                     stack,
                     EquipmentSlot.CHEST,
                     inv < 9 ? autismclient.util.ArmorEvaluationHelper.SourceKind.HOTBAR : autismclient.util.ArmorEvaluationHelper.SourceKind.INVENTORY,
                     inv
                  );
               }
            }

            ItemStack offhand = mc.player.getItemBySlot(EquipmentSlot.OFFHAND);
            return offhand.is(Items.ELYTRA)
               ? new autismclient.util.ArmorEvaluationHelper.ArmorCandidate(offhand, EquipmentSlot.CHEST, autismclient.util.ArmorEvaluationHelper.SourceKind.INVENTORY, 40)
               : null;
         }

         int screenSlot(autismclient.util.ArmorEvaluationHelper.ArmorCandidate candidate) {
            return autismclient.util.InventorySlotHelper.toScreenSlot(mc, candidate.inventorySlot());
         }

         AiOperation.Result running(String message) {
            return AiGameOperations.result(
               AiOperation.Status.RUNNING,
               message,
               AiJson.objectOf("what", "armor", "worn", AiEquip.wornArmorSummary(), "wornBefore", this.wornBefore, "armorMoves", this.armorMoves)
            );
         }

         AiOperation.Result finish(AiOperation.Status status, String message) {
            JsonObject data = AiJson.objectOf("what", "armor", "worn", AiEquip.wornArmorSummary(), "wornBefore", this.wornBefore, "armorMoves", this.armorMoves);
            this.cancel();
            return new AiOperation.Result(status, message, data.toString(), List.of());
         }

         @Override
         public void cancel() {
            this.done = true;
         }

         @Override
         public boolean canInterruptNow() {
            return !this.movePending || this.done;
         }
      };
   }

   static Map<EquipmentSlot, List<autismclient.util.ArmorEvaluationHelper.ArmorCandidate>> preferredCandidates(
      Map<EquipmentSlot, List<autismclient.util.ArmorEvaluationHelper.ArmorCandidate>> byType, AiItems.ItemSpec prefer
   ) {
      Map<EquipmentSlot, List<autismclient.util.ArmorEvaluationHelper.ArmorCandidate>> matching = new EnumMap<>(EquipmentSlot.class);

      for (Entry<EquipmentSlot, List<autismclient.util.ArmorEvaluationHelper.ArmorCandidate>> entry : byType.entrySet()) {
         List<autismclient.util.ArmorEvaluationHelper.ArmorCandidate> kept = new ArrayList<>();

         for (autismclient.util.ArmorEvaluationHelper.ArmorCandidate candidate : entry.getValue()) {
            if (prefer.matches(candidate.stack())) {
               kept.add(candidate);
            }
         }

         if (!kept.isEmpty()) {
            matching.put(entry.getKey(), kept);
         }
      }

      return matching;
   }

   static Map<EquipmentSlot, autismclient.util.ArmorEvaluationHelper.ArmorCandidate> bestBySlot(
      Map<EquipmentSlot, List<autismclient.util.ArmorEvaluationHelper.ArmorCandidate>> byType, int threshold, AiItems.ItemSpec prefer
   ) {
      Map<EquipmentSlot, autismclient.util.ArmorEvaluationHelper.ArmorCandidate> best = autismclient.util.ArmorEvaluationHelper.bestBySlot(byType, threshold);
      if (prefer != null) {
         best.putAll(autismclient.util.ArmorEvaluationHelper.bestBySlot(preferredCandidates(byType, prefer), threshold));
      }

      return best;
   }

   static AiItems.ItemSpec parsePreferSpec(String spec) {
      if (spec.contains(",")) {
         Set<Item> items = new LinkedHashSet<>();
         Set<Block> blocks = new LinkedHashSet<>();

         for (String part : spec.split(",", -1)) {
            if (part.isBlank()) {
               throw new IllegalArgumentException("prefer list contains an empty item specification");
            }

            AiItems.ItemSpec target = parsePreferSpec(part.trim());
            items.addAll(target.items());
            blocks.addAll(target.blocks());
         }

         return new AiItems.ItemSpec(spec, Set.copyOf(items), Set.copyOf(blocks));
      } else {
         String clean = spec.trim().toLowerCase(Locale.ROOT);
         if (!clean.endsWith("*")) {
            return AiItems.resolveItem(clean);
         }

         String prefix = clean.substring(0, clean.length() - 1);
         if (prefix.startsWith("minecraft:")) {
            prefix = prefix.substring("minecraft:".length());
         }

         if (!prefix.isEmpty() && !prefix.contains(":")) {
            Set<Item> items = new LinkedHashSet<>();

            for (Identifier key : BuiltInRegistries.ITEM.keySet()) {
               if (key.getPath().startsWith(prefix)) {
                  BuiltInRegistries.ITEM.getOptional(key).ifPresent(items::add);
               }
            }

            if (items.isEmpty()) {
               throw new IllegalArgumentException("prefer wildcard '" + spec + "' matches no item");
            } else {
               return new AiItems.ItemSpec(clean, items, Set.of());
            }
         } else {
            throw new IllegalArgumentException("prefer wildcard needs a minecraft path prefix, like golden_*");
         }
      }
   }

   private static int durabilityThreshold(Module module) {
      try {
         return Integer.parseInt(module.value("durability-threshold"));
      } catch (NumberFormatException e) {
         return -2147483648;
      }
   }

   private static String itemId(ItemStack stack) {
      return stack.isEmpty() ? "minecraft:air" : BuiltInRegistries.ITEM.getKey(stack.getItem()).toString();
   }

   private static String mainHandItemId() {
      Minecraft mc = Minecraft.getInstance();
      if (mc.player == null) {
         return "";
      }

      ItemStack stack = mc.player.getMainHandItem();
      return stack.isEmpty() ? "minecraft:air" : BuiltInRegistries.ITEM.getKey(stack.getItem()).toString();
   }

   private static String wornArmorSummary() {
      Minecraft mc = Minecraft.getInstance();
      if (mc.player == null) {
         return "";
      }

      StringBuilder text = new StringBuilder();

      for (EquipmentSlot slot : List.of(EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET)) {
         ItemStack stack = mc.player.getItemBySlot(slot);
         if (text.length() > 0) {
            text.append(", ");
         }

         text.append(stack.isEmpty() ? "none" : BuiltInRegistries.ITEM.getKey(stack.getItem()).getPath());
      }

      return text.toString();
   }

   private enum MoveKind {
      MOVE_IN,
      CLEAR_WORN;
   }

   private record EquipMove(EquipmentSlot slot, int menuSlot, int button, ContainerInput input, AiEquip.MoveKind kind, String description) {
   }
}

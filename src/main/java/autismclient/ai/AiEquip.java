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
   private static final Map<EquipmentSlot, Integer> init_Map = Map.of(
      EquipmentSlot.HEAD, 5, EquipmentSlot.CHEST, 6, EquipmentSlot.LEGS, 7, EquipmentSlot.FEET, 8
   );

   private AiEquip() {
   }

   static AiOperation init(JsonObject args) {
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

         String hand = init_Str(AiJson.getString(args, "hand", "main"));
         if (hand.equals("offhand")) {
            if (!what.equals("armor") && !what.equals("armour") && !what.equals("weapon") && !what.equals("tool")) {
               return init(AiItems.resolveItem(what), what);
            } else {
               throw new IllegalArgumentException("hand:offhand equips a specific item spec (e.g. shield), not " + what);
            }
         } else {
            if (what.equals("armor") || what.equals("armour")) {
               return init_AiOperation(prefer);
            }

            if (what.equals("weapon")) {
               return init(AiItems.bestWeaponSlot(36), "weapon");
            }

            if (what.equals("tool")) {
               String blockSpec = AiJson.getString(args, "forBlock", "");
               if (blockSpec.isBlank()) {
                  throw new IllegalArgumentException("tool needs forBlock, the block the tool is for");
               }

               AiItems.ItemSpec block = AiItems.resolveBlock(blockSpec);
               BlockState state = block.blocks().iterator().next().defaultBlockState();
               return init(AiItems.bestToolFor(state, 36), "tool for " + blockSpec);
            } else {
               return load(AiItems.resolveItem(what), what);
            }
         }
      } else {
         throw new IllegalArgumentException("Equipping requires a connected living player");
      }
   }

   static String init_Str(String raw) {
      String hand = raw == null ? "main" : raw.trim().toLowerCase(Locale.ROOT);
      if (hand.isEmpty()) {
         return "main";
      } else if (!hand.equals("main") && !hand.equals("offhand")) {
         throw new IllegalArgumentException("hand must be main or offhand");
      } else {
         return hand;
      }
   }

   private static AiOperation init(final AiItems.ItemSpec target, final String what) {
      final Minecraft mc = Minecraft.getInstance();
      return new AiOperation() {
         long init_J = -1L;
         final Object init_Obj = mc.level;
         final Object load_Obj = mc.getConnection();
         final Object read = mc.player;
         int init_I;
         boolean init_Z;
         boolean load_Z;

         @Override
         public AiOperation.Result tick() {
            if (!this.load_Z && mc.player != null) {
               if (mc.level == this.init_Obj && mc.getConnection() == this.load_Obj && mc.player == this.read && mc.player.isAlive()) {
                  if (!target.matches(mc.player.getOffhandItem()) || this.init_Z && AiServerEvidence.containerReplyCount(mc.player.inventoryMenu.containerId) <= this.init_J) {
                     if (!this.init_Z) {
                        if (mc.player.containerMenu == mc.player.inventoryMenu && mc.player.containerMenu.getCarried().isEmpty()) {
                           int source = AiItems.findSlot(target, 36);
                           if (source < 0) {
                              this.load_Z = true;
                              return AiOperation.Result.failed(
                                 AiFailure.MISSING_MATERIALS,
                                 "No " + what + " in inventory",
                                 AiJson.objectOf("what", what, "matches", AiJson.GSON.toJsonTree(target.itemIds())).toString()
                              );
                           }

                           this.init_J = AiServerEvidence.containerReplyCount(mc.player.inventoryMenu.containerId);
                           int menuSlot = autismclient.util.InventorySlotHelper.toScreenSlot(mc, source);
                           this.init_Z = menuSlot >= 0;
                           if (this.init_Z) {
                              AiContainerTransactions.init(mc.player.inventoryMenu.containerId, menuSlot, 40, ContainerInput.SWAP);
                           }

                           if (!this.init_Z) {
                              this.load_Z = true;
                              return AiOperation.Result.failed(
                                 AiFailure.IMPOSSIBLE_STATE,
                                 "Could not move " + what + " to the offhand (the cursor must be empty)",
                                 AiJson.objectOf("what", what).toString()
                              );
                           } else {
                              return this.init("Moving " + what + " to the offhand");
                           }
                        } else {
                           return AiGameOperations.result(
                              AiOperation.Status.FAILED, "Offhand swap requires the player inventory with an empty cursor", new JsonObject()
                           );
                        }
                     } else if (++this.init_I > 40) {
                        this.load_Z = true;
                        return AiGameOperations.result(
                           AiOperation.Status.UNCERTAIN,
                           "The offhand swap went out but no applied server inventory confirmation arrived",
                           AiJson.objectOf("what", what)
                        );
                     } else {
                        return this.init("Waiting for applied server inventory confirmation");
                     }
                  } else {
                     this.load_Z = true;
                     return AiGameOperations.result(
                        AiOperation.Status.SUCCEEDED, "Offhand holds " + AiEquip.init(), AiJson.objectOf("what", what, "hand", "offhand", "held", AiEquip.init())
                     );
                  }
               } else {
                  return AiGameOperations.result(AiOperation.Status.UNCERTAIN, "Offhand equipment player or world changed", new JsonObject());
               }
            } else {
               return AiGameOperations.result(AiOperation.Status.CANCELLED, "Equip cancelled", new JsonObject());
            }
         }

         AiOperation.Result init(String message) {
            return AiGameOperations.result(AiOperation.Status.RUNNING, message, AiJson.objectOf("what", what, "hand", "offhand"));
         }

         @Override
         public void cancel() {
            this.load_Z = true;
         }

         @Override
         public boolean canInterruptNow() {
            return !this.init_Z || this.load_Z;
         }
      };
   }

   private static String init() {
      Minecraft mc = Minecraft.getInstance();
      if (mc.player == null) {
         return "";
      }

      ItemStack stack = mc.player.getOffhandItem();
      return stack.isEmpty() ? "minecraft:air" : BuiltInRegistries.ITEM.getKey(stack.getItem()).toString();
   }

   private static AiOperation init(int slot, String description) {
      return init(slot, null, description);
   }

   private static AiOperation load(AiItems.ItemSpec target, String description) {
      return init(-1, target, description);
   }

   private static AiOperation init(final int plannedSlot, final AiItems.ItemSpec target, final String description) {
      final Minecraft mc = Minecraft.getInstance();
      return new AiOperation() {
         final Object init_Obj = mc.level;
         final Object load_Obj = mc.getConnection();
         final Object read = mc.player;
         boolean init_Z;
         boolean load_Z;
         int init_I = -1;
         int load_I;
         long init_J;
         ItemStack init_ItemStack = ItemStack.EMPTY;

         @Override
         public AiOperation.Result tick() {
            if (!this.init_Z && mc.player != null) {
               if (mc.level != this.init_Obj || mc.getConnection() != this.load_Obj || mc.player != this.read || !mc.player.isAlive()) {
                  return this.init(AiOperation.Status.UNCERTAIN, "Hand equipment player or world changed");
               }

               if (mc.player.containerMenu != mc.player.inventoryMenu || !mc.player.inventoryMenu.getCarried().isEmpty()) {
                  return this.init(
                     this.load_Z ? AiOperation.Status.UNCERTAIN : AiOperation.Status.FAILED,
                     "Hand equipment requires the player inventory with an empty cursor"
                  );
               }

               if (!this.load_Z) {
                  int source = target == null ? plannedSlot : AiItems.findSlot(target, 36);
                  if (source >= 0 && source < 36 && !mc.player.getInventory().getItem(source).isEmpty()) {
                     this.init_ItemStack = mc.player.getInventory().getItem(source).copy();
                     this.init_I = source < 9 ? source : AiItems.pickHotbarDestination();
                     if (source < 9) {
                        autismclient.util.InventorySlotHelper.selectHotbarSlot(mc, this.init_I);
                        return this.init(AiOperation.Status.SUCCEEDED, "Holding " + AiEquip.load());
                     }

                     int menuSlot = autismclient.util.InventorySlotHelper.toScreenSlot(mc, source);
                     if (menuSlot < 0) {
                        return this.init(AiOperation.Status.FAILED, "Could not resolve the equipment's inventory slot");
                     }

                     this.init_J = AiServerEvidence.containerReplyCount(mc.player.inventoryMenu.containerId);
                     AiContainerTransactions.init(mc.player.inventoryMenu.containerId, menuSlot, this.init_I, ContainerInput.SWAP);
                     this.load_Z = true;
                     return AiGameOperations.result(AiOperation.Status.RUNNING, "Moving " + description + " into the hotbar", this.init());
                  } else {
                     return this.init(AiOperation.Status.FAILED, "No suitable " + description + " in inventory");
                  }
               } else if (AiServerEvidence.containerReplyCount(mc.player.inventoryMenu.containerId) > this.init_J) {
                  if (!ItemStack.matches(this.init_ItemStack, mc.player.getInventory().getItem(this.init_I))) {
                     return this.init(AiOperation.Status.FAILED, "Server did not apply the expected hotbar swap; no repeat click issued");
                  }

                  autismclient.util.InventorySlotHelper.selectHotbarSlot(mc, this.init_I);
                  return this.init(AiOperation.Status.SUCCEEDED, "Holding " + AiEquip.load());
               } else {
                  return ++this.load_I > 40
                     ? this.init(AiOperation.Status.UNCERTAIN, "Hotbar swap lacks an applied server snapshot")
                     : AiGameOperations.result(AiOperation.Status.RUNNING, "Waiting for the hotbar swap to apply", this.init());
               }
            } else {
               return this.init(AiOperation.Status.CANCELLED, "Equip cancelled");
            }
         }

         JsonObject init() {
            return AiJson.objectOf("what", description, "slot", this.init_I, "held", AiEquip.load(), "pending", this.load_Z && !this.init_Z);
         }

         AiOperation.Result init(AiOperation.Status status, String message) {
            this.init_Z = true;
            return AiGameOperations.result(status, message, this.init());
         }

         @Override
         public void cancel() {
            this.init_Z = true;
         }

         @Override
         public boolean canInterruptNow() {
            return !this.load_Z || this.init_Z;
         }
      };
   }

   private static AiOperation init_AiOperation(final String preferSpec) {
      final Minecraft mc = Minecraft.getInstance();
      Module module = autismclient.modules.al.init_Module_Str("auto-armor");
      final boolean preferElytra = !AiRunResources.isCampaignActive() && module != null && "true".equals(module.value("prefer-elytra"));
      final boolean allowCursed = module != null && "true".equals(module.value("allow-cursed"));
      final boolean swapArmor = module != null && "true".equals(module.value("can-swap-armor"));
      final boolean hotbarSwap = module == null || !"false".equals(module.value("hotbar"));
      boolean saveArmor = module != null && "true".equals(module.value("save-armor"));
      final int threshold = Math.max(saveArmor ? init(module) : -2147483648, AiRunResources.isCampaignActive() ? 32 : -2147483648);
      final AiItems.ItemSpec prefer = preferSpec.isEmpty() ? null : init_af_DataUtil(preferSpec);
      return new AiOperation() {
         final String init_Str = AiEquip.read();
         final Object init_Obj = mc.level;
         final Object load_Obj = mc.getConnection();
         final Object read_Obj = mc.player;
         ItemStack init_ItemStack = ItemStack.EMPTY;
         EquipmentSlot init_EquipmentSlot;
         int init_I;
         int load_I;
         long init_J = -1L;
         int read_I;
         boolean init_Z;
         boolean load_Z;
         boolean read_Z;

         @Override
         public AiOperation.Result tick() {
            if (!this.load_Z && mc.player != null) {
               if (mc.level != this.init_Obj || mc.getConnection() != this.load_Obj || mc.player != this.read_Obj || !mc.player.isAlive()) {
                  return this.init(AiOperation.Status.UNCERTAIN, "Armor player or world changed; reconcile any pending move");
               }

               if (++this.init_I > 400) {
                  return this.init(AiOperation.Status.UNCERTAIN, "Armour equip did not settle in time");
               }

               if (mc.player.containerMenu != mc.player.inventoryMenu) {
                  return this.init(this.init_Z ? AiOperation.Status.UNCERTAIN : AiOperation.Status.FAILED, "Close the open container before equipping armour");
               }

               if (!mc.player.containerMenu.getCarried().isEmpty()) {
                  return this.init(AiOperation.Status.FAILED, "Clear the cursor before equipping armour");
               }

               if (this.init_Z) {
                  if (AiServerEvidence.containerReplyCount(mc.player.inventoryMenu.containerId) <= this.init_J) {
                     return ++this.read_I > 40
                        ? this.init(AiOperation.Status.UNCERTAIN, "The armour click went out but no applied server inventory confirmation arrived")
                        : this.init("Waiting for applied server inventory confirmation");
                  }

                  if (!ItemStack.matches(this.init_ItemStack, mc.player.getItemBySlot(this.init_EquipmentSlot))) {
                     return this.init(AiOperation.Status.FAILED, "Server did not apply the expected armor move; no repeat click issued");
                  }

                  this.init_Z = false;
                  this.load_I++;
                  this.read_I = 0;
                  return this.init("Armour move applied; re-evaluating");
               } else {
                  if (prefer != null && !this.read_Z) {
                     this.read_Z = true;
                     if (AiEquip.init(autismclient.util.ArmorEvaluationHelper.init_Map_TaskTimer_BufferHe(this.init_TaskTimer_BufferHe(), allowCursed, true), prefer).isEmpty()) {
                        JsonObject data = AiJson.objectOf(
                           "what",
                           "armor",
                           "prefer",
                           preferSpec,
                           "matches",
                           AiJson.GSON.toJsonTree(prefer.itemIds()),
                           "worn",
                           AiEquip.read(),
                           "wornBefore",
                           this.init_Str
                        );
                        this.cancel();
                        return AiOperation.Result.failed(AiFailure.MISSING_MATERIALS, "prefer '" + preferSpec + "' matches no armor piece, carried or worn", data.toString());
                     }
                  }

                  AiEquip.Inner_StreamUtil move = this.init_w_StreamUtil();
                  if (move == null) {
                     String now = AiEquip.read();
                     return this.init(
                        AiOperation.Status.SUCCEEDED,
                        this.load_I == 0 ? "Already wearing the best available armour" : "Equipped " + now + " (" + this.load_I + " verified moves)"
                     );
                  }

                  if (move.kind() == AiEquip.Inner_DataUtil.CLEAR_WORN && mc.player.getInventory().getFreeSlot() < 0) {
                     return this.init(AiOperation.Status.FAILED, "No free inventory slot to swap out the worn " + AiEquip.init(mc.player.getItemBySlot(move.slot())));
                  }

                  this.init_J = AiServerEvidence.containerReplyCount(mc.player.inventoryMenu.containerId);
                  AiContainerTransactions.init(mc.player.containerMenu.containerId, move.menuSlot(), move.button(), move.input());
                  this.init_EquipmentSlot = move.slot();
                  this.init_ItemStack = mc.player.getItemBySlot(this.init_EquipmentSlot).copy();
                  this.init_Z = true;
                  this.read_I = 0;
                  return this.init("Moving " + move.description());
               }
            } else {
               return this.init(AiOperation.Status.CANCELLED, "Armour equip cancelled");
            }
         }

         AiEquip.Inner_StreamUtil init_w_StreamUtil() {
            Map<EquipmentSlot, autismclient.util.ArmorEvaluationHelper.Inner_DataUtil> best = AiEquip.init(
               autismclient.util.ArmorEvaluationHelper.init_Map_TaskTimer_BufferHe(this.init_TaskTimer_BufferHe(), allowCursed, true), threshold, prefer
            );
            if (preferElytra) {
               autismclient.util.ArmorEvaluationHelper.Inner_DataUtil elytra = this.init_TaskTimer_DataUtil();
               if (elytra != null) {
                  best.put(EquipmentSlot.CHEST, elytra);
               }
            }

            for (EquipmentSlot slot : autismclient.util.ArmorEvaluationHelper.init_EquipmentSlotArr) {
               autismclient.util.ArmorEvaluationHelper.Inner_DataUtil candidate = best.get(slot);
               if (candidate != null && candidate.init_TaskTimer_StreamUt() != autismclient.util.ArmorEvaluationHelper.Inner_StreamUtil.ARMOR) {
                  ItemStack wornStack = mc.player.getItemBySlot(slot);
                  if (wornStack.isEmpty() || !wornStack.is(Items.ELYTRA) && !autismclient.util.ArmorEvaluationHelper.load_Z(wornStack)) {
                     int armorMenuSlot = AiEquip.init_Map.get(slot);
                     if (wornStack.isEmpty()) {
                        return candidate.init_TaskTimer_StreamUt() == autismclient.util.ArmorEvaluationHelper.Inner_StreamUtil.HOTBAR && hotbarSwap
                           ? new AiEquip.Inner_StreamUtil(
                              slot, armorMenuSlot, candidate.init_I(), ContainerInput.SWAP, AiEquip.Inner_DataUtil.MOVE_IN, AiEquip.init(candidate.init_ItemStack())
                           )
                           : new AiEquip.Inner_StreamUtil(
                              slot, this.init(candidate), 0, ContainerInput.QUICK_MOVE, AiEquip.Inner_DataUtil.MOVE_IN, AiEquip.init(candidate.init_ItemStack())
                           );
                     } else {
                        return candidate.init_TaskTimer_StreamUt() == autismclient.util.ArmorEvaluationHelper.Inner_StreamUtil.HOTBAR && hotbarSwap && swapArmor
                           ? new AiEquip.Inner_StreamUtil(
                              slot, armorMenuSlot, candidate.init_I(), ContainerInput.SWAP, AiEquip.Inner_DataUtil.MOVE_IN, AiEquip.init(candidate.init_ItemStack())
                           )
                           : new AiEquip.Inner_StreamUtil(slot, armorMenuSlot, 0, ContainerInput.QUICK_MOVE, AiEquip.Inner_DataUtil.CLEAR_WORN, AiEquip.init(wornStack));
                     }
                  }
               }
            }

            return null;
         }

         autismclient.util.ArmorEvaluationHelper.Inner_BufferHelper init_TaskTimer_BufferHe() {
            return new autismclient.util.ArmorEvaluationHelper.Inner_BufferHelper() {
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

         autismclient.util.ArmorEvaluationHelper.Inner_DataUtil init_TaskTimer_DataUtil() {
            ItemStack wornChest = mc.player.getItemBySlot(EquipmentSlot.CHEST);
            if (wornChest.is(Items.ELYTRA)) {
               return new autismclient.util.ArmorEvaluationHelper.Inner_DataUtil(wornChest, EquipmentSlot.CHEST, autismclient.util.ArmorEvaluationHelper.Inner_StreamUtil.ARMOR, -1);
            }

            for (int inv = 0; inv < 36; inv++) {
               ItemStack stack = mc.player.getInventory().getItem(inv);
               if (stack.is(Items.ELYTRA)) {
                  return new autismclient.util.ArmorEvaluationHelper.Inner_DataUtil(
                     stack,
                     EquipmentSlot.CHEST,
                     inv < 9 ? autismclient.util.ArmorEvaluationHelper.Inner_StreamUtil.HOTBAR : autismclient.util.ArmorEvaluationHelper.Inner_StreamUtil.INVENTORY,
                     inv
                  );
               }
            }

            ItemStack offhand = mc.player.getItemBySlot(EquipmentSlot.OFFHAND);
            return offhand.is(Items.ELYTRA)
               ? new autismclient.util.ArmorEvaluationHelper.Inner_DataUtil(offhand, EquipmentSlot.CHEST, autismclient.util.ArmorEvaluationHelper.Inner_StreamUtil.INVENTORY, 40)
               : null;
         }

         int init(autismclient.util.ArmorEvaluationHelper.Inner_DataUtil candidate) {
            return autismclient.util.InventorySlotHelper.toScreenSlot(mc, candidate.init_I());
         }

         AiOperation.Result init(String message) {
            return AiGameOperations.result(
               AiOperation.Status.RUNNING,
               message,
               AiJson.objectOf("what", "armor", "worn", AiEquip.read(), "wornBefore", this.init_Str, "armorMoves", this.load_I)
            );
         }

         AiOperation.Result init(AiOperation.Status status, String message) {
            JsonObject data = AiJson.objectOf("what", "armor", "worn", AiEquip.read(), "wornBefore", this.init_Str, "armorMoves", this.load_I);
            this.cancel();
            return new AiOperation.Result(status, message, data.toString(), List.of());
         }

         @Override
         public void cancel() {
            this.load_Z = true;
         }

         @Override
         public boolean canInterruptNow() {
            return !this.init_Z || this.load_Z;
         }
      };
   }

   static Map<EquipmentSlot, List<autismclient.util.ArmorEvaluationHelper.Inner_DataUtil>> init(
      Map<EquipmentSlot, List<autismclient.util.ArmorEvaluationHelper.Inner_DataUtil>> byType, AiItems.ItemSpec prefer
   ) {
      Map<EquipmentSlot, List<autismclient.util.ArmorEvaluationHelper.Inner_DataUtil>> matching = new EnumMap<>(EquipmentSlot.class);

      for (Entry<EquipmentSlot, List<autismclient.util.ArmorEvaluationHelper.Inner_DataUtil>> entry : byType.entrySet()) {
         List<autismclient.util.ArmorEvaluationHelper.Inner_DataUtil> kept = new ArrayList<>();

         for (autismclient.util.ArmorEvaluationHelper.Inner_DataUtil candidate : entry.getValue()) {
            if (prefer.matches(candidate.init_ItemStack())) {
               kept.add(candidate);
            }
         }

         if (!kept.isEmpty()) {
            matching.put(entry.getKey(), kept);
         }
      }

      return matching;
   }

   static Map<EquipmentSlot, autismclient.util.ArmorEvaluationHelper.Inner_DataUtil> init(
      Map<EquipmentSlot, List<autismclient.util.ArmorEvaluationHelper.Inner_DataUtil>> byType, int threshold, AiItems.ItemSpec prefer
   ) {
      Map<EquipmentSlot, autismclient.util.ArmorEvaluationHelper.Inner_DataUtil> best = autismclient.util.ArmorEvaluationHelper.init_Map_Map_I(byType, threshold);
      if (prefer != null) {
         best.putAll(autismclient.util.ArmorEvaluationHelper.init_Map_Map_I(init(byType, prefer), threshold));
      }

      return best;
   }

   static AiItems.ItemSpec init_af_DataUtil(String spec) {
      if (spec.contains(",")) {
         Set<Item> items = new LinkedHashSet<>();
         Set<Block> blocks = new LinkedHashSet<>();

         for (String part : spec.split(",", -1)) {
            if (part.isBlank()) {
               throw new IllegalArgumentException("prefer list contains an empty item specification");
            }

            AiItems.ItemSpec target = init_af_DataUtil(part.trim());
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

   private static int init(Module module) {
      try {
         return Integer.parseInt(module.value("durability-threshold"));
      } catch (NumberFormatException e) {
         return -2147483648;
      }
   }

   private static String init(ItemStack stack) {
      return stack.isEmpty() ? "minecraft:air" : BuiltInRegistries.ITEM.getKey(stack.getItem()).toString();
   }

   private static String load() {
      Minecraft mc = Minecraft.getInstance();
      if (mc.player == null) {
         return "";
      }

      ItemStack stack = mc.player.getMainHandItem();
      return stack.isEmpty() ? "minecraft:air" : BuiltInRegistries.ITEM.getKey(stack.getItem()).toString();
   }

   private static String read() {
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

   private enum Inner_DataUtil {
      MOVE_IN,
      CLEAR_WORN;
   }

   private record Inner_StreamUtil(EquipmentSlot slot, int menuSlot, int button, ContainerInput input, AiEquip.Inner_DataUtil kind, String description) {
   }
}

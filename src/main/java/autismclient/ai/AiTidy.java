package autismclient.ai;

import autismclient.api.ai.AiOperation;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.OptionalInt;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.ContainerInput;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.EmptyBlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.FallingBlock;
import net.minecraft.world.level.block.state.BlockState;

final class AiTidy {
   private static final int open = 20;
   static final int init_I = 100;
   static final int load_I = 90;
   static final int read_I = 80;
   static final int write_I = 60;
   static final int update = 40;
   static final int sync = 20;
   static final int flush = 0;
   private static final List<String> VALUABLE_ITEMS = List.of(
      "minecraft:diamond",
      "minecraft:emerald",
      "minecraft:ancient_debris",
      "minecraft:netherite_scrap",
      "minecraft:netherite_ingot",
      "minecraft:iron_ingot",
      "minecraft:gold_ingot",
      "minecraft:raw_iron",
      "minecraft:raw_gold",
      "minecraft:ender_pearl",
      "minecraft:blaze_rod",
      "minecraft:blaze_powder",
      "minecraft:ender_eye",
      "minecraft:obsidian",
      "minecraft:lapis_lazuli",
      "minecraft:redstone",
      "minecraft:coal",
      "minecraft:charcoal",
      "minecraft:string",
      "minecraft:gunpowder",
      "minecraft:experience_bottle",
      "minecraft:totem_of_undying",
      "minecraft:golden_apple",
      "minecraft:enchanted_golden_apple",
      "minecraft:nether_star",
      "minecraft:elytra"
   );
   private static final List<String> UTILITY_ITEMS = List.of(
      "minecraft:bucket",
      "minecraft:water_bucket",
      "minecraft:lava_bucket",
      "minecraft:flint_and_steel",
      "minecraft:fire_charge",
      "minecraft:torch",
      "minecraft:crafting_table",
      "minecraft:furnace",
      "minecraft:chest",
      "minecraft:ender_chest",
      "minecraft:shield",
      "minecraft:arrow",
      "minecraft:fishing_rod",
      "minecraft:shears",
      "minecraft:clock",
      "minecraft:compass",
      "minecraft:map",
      "minecraft:flint",
      "minecraft:stick",
      "minecraft:scaffolding"
   );
   private static final List<String> JUNK_ITEMS = List.of(
      "minecraft:dirt",
      "minecraft:coarse_dirt",
      "minecraft:rooted_dirt",
      "minecraft:gravel",
      "minecraft:andesite",
      "minecraft:diorite",
      "minecraft:granite",
      "minecraft:tuff",
      "minecraft:cobbled_deepslate",
      "minecraft:deepslate",
      "minecraft:netherrack",
      "minecraft:basalt",
      "minecraft:blackstone",
      "minecraft:soul_sand",
      "minecraft:soul_soil",
      "minecraft:magma_block",
      "minecraft:rotten_flesh",
      "minecraft:poisonous_potato",
      "minecraft:spider_eye",
      "minecraft:wheat_seeds",
      "minecraft:beetroot_seeds",
      "minecraft:melon_seeds",
      "minecraft:pumpkin_seeds",
      "minecraft:short_grass",
      "minecraft:tall_grass",
      "minecraft:dead_bush",
      "minecraft:kelp",
      "minecraft:seagrass",
      "minecraft:bone_meal",
      "minecraft:ink_sac"
   );
   private static final List<String> KEEP_ITEMS = List.of(
      "minecraft:water_bucket",
      "minecraft:lava_bucket",
      "minecraft:bucket",
      "minecraft:flint_and_steel",
      "minecraft:fire_charge",
      "minecraft:arrow",
      "minecraft:spectral_arrow",
      "minecraft:tipped_arrow",
      "minecraft:gold_block",
      "minecraft:gold_nugget",
      "minecraft:crafting_table",
      "minecraft:potion",
      "minecraft:splash_potion"
   );
   static final int close = 64;

   private AiTidy() {
   }

   static AiOperation create(JsonObject args) {
      final Minecraft mc = Minecraft.getInstance();
      if (mc.player != null && mc.level != null) {
         AiTidy.TidyAction action = parseAction(AiJson.getString(args, "action", "inspect"));
         final int wantFree = Math.max(1, Math.min(36, AiJson.getInt(args, "freeSlots", 4)));
         final int maxDrops = Math.max(1, Math.min(36, AiJson.getInt(args, "maxDrops", 9)));
         final int timeoutSeconds = Math.max(5, Math.min(300, AiJson.getInt(args, "timeoutSeconds", 60)));
         final List<AiItems.ItemSpec> keep = new ArrayList<>();
         if (args.has("keep") && args.get("keep").isJsonArray()) {
            for (JsonElement element : args.getAsJsonArray("keep")) {
               keep.add(AiItems.resolve(element.getAsString()));
            }
         } else if (args.has("keep") && args.get("keep").isJsonPrimitive()) {
            for (String part : args.get("keep").getAsString().split(",")) {
               if (!part.isBlank()) {
                  keep.add(AiItems.resolve(part.trim()));
               }
            }
         }

         return action == AiTidy.TidyAction.INSPECT
            ? AiGameOperations.immediate(AiOperation.Status.SUCCEEDED, "Ranked " + AiItems.countFreeSlots() + " free slot(s) and the current inventory", rankingJson(mc, keep, wantFree))
            : new AiOperation() {
               final long deadline = System.nanoTime() + timeoutSeconds * 1000000000L;
               final int startFreeSlots = AiItems.countFreeSlots();
               final Object startLevel = mc.level;
               final Object startConnection = mc.getConnection();
               final Object startPlayer = mc.player;
               final JsonArray dropped = new JsonArray();
               int pendingSlot = -1;
               int confirmWaitTicks;
               long replyBaseline;
               int countBeforeDrop;
               AiTidy.SlotRank worst;
               int drops;
               boolean cancelled;

               @Override
               public AiOperation.Result tick() {
                  if (!this.cancelled && mc.player != null) {
                     if (mc.level != this.startLevel || mc.getConnection() != this.startConnection || mc.player != this.startPlayer || !mc.player.isAlive()) {
                        return this.finish(AiOperation.Status.UNCERTAIN, "Tidy player or world changed; reconcile any pending drop");
                     }

                     if (System.nanoTime() > this.deadline) {
                        return this.finish(this.pendingSlot >= 0 ? AiOperation.Status.UNCERTAIN : AiOperation.Status.TIMED_OUT, "Tidy deadline reached");
                     }

                     if (mc.player.containerMenu != mc.player.inventoryMenu) {
                        return this.finish(AiOperation.Status.FAILED, "Close the open container before tidying");
                     }

                     if (!mc.player.containerMenu.getCarried().isEmpty()) {
                        return this.finish(AiOperation.Status.FAILED, "Clear the cursor stack before tidying");
                     }

                     if (this.pendingSlot >= 0) {
                        OptionalInt confirmedCount = AiServerEvidence.minItemCountAfterReply(mc.player.inventoryMenu.containerId, this.replyBaseline, this.worst.item());
                        if (confirmedCount.isPresent() && confirmedCount.getAsInt() <= this.countBeforeDrop - this.worst.count()) {
                           this.dropped
                              .add(
                                 AiJson.objectOf(
                                    "slot",
                                    this.worst.slot(),
                                    "item",
                                    this.worst.item(),
                                    "count",
                                    this.worst.count(),
                                    "rank",
                                    this.worst.rank(),
                                    "confirmed",
                                    true
                                 )
                              );
                           this.drops++;
                           this.pendingSlot = -1;
                           this.worst = null;
                           this.confirmWaitTicks = 0;
                           return this.running("Drop confirmed");
                        } else {
                           return ++this.confirmWaitTicks > 20
                              ? this.finish(AiOperation.Status.UNCERTAIN, "A drop was not confirmed by the server; reobserve before repeating")
                              : this.running("Waiting for the drop to apply");
                        }
                     } else {
                        if (AiItems.countFreeSlots() >= wantFree) {
                           return this.finish(
                              AiOperation.Status.SUCCEEDED, "Inventory has " + AiItems.countFreeSlots() + " free slot(s) after dropping " + this.drops + " stack(s)"
                           );
                        }

                        if (this.drops >= maxDrops) {
                           return this.failFull(
                              "Dropped the allowed "
                                 + maxDrops
                                 + " stack(s) and still have only "
                                 + AiItems.countFreeSlots()
                                 + " free slot(s); raise maxDrops or deposit into a container"
                           );
                        }

                        AiTidy.SlotRank worst = AiTidy.worstSlot(mc, keep);
                        if (worst == null) {
                           return this.failFull("Nothing left is safe to drop; everything remaining is kept, equipped or high value");
                        }

                        this.replyBaseline = AiServerEvidence.containerReplyCount(mc.player.inventoryMenu.containerId);
                        this.pendingSlot = worst.slot();
                        this.worst = worst;
                        this.countBeforeDrop = AiCraft.countItem(worst.item());
                        AiContainerTransactions.sendPredictedClick(mc.player.containerMenu.containerId, AiTidy.screenSlotOrThrow(mc, worst.slot()), 1, ContainerInput.THROW);
                        return this.running("Dropping " + worst.count() + " " + worst.item());
                     }
                  } else {
                     return this.finish(AiOperation.Status.CANCELLED, "Tidy cancelled");
                  }
               }

               JsonObject progressData() {
                  JsonObject out = AiJson.objectOf(
                     "freeSlots", AiItems.countFreeSlots(), "target", wantFree, "startedWith", this.startFreeSlots, "drops", this.drops, "maxDrops", maxDrops
                  );
                  if (this.worst != null) {
                     out.add(
                        "pendingDrop",
                        AiJson.objectOf(
                           "slot",
                           this.pendingSlot,
                           "item",
                           this.worst.item(),
                           "count",
                           this.worst.count(),
                           "beforeCount",
                           this.countBeforeDrop,
                           "currentCount",
                           AiCraft.countItem(this.worst.item()),
                           "minimumServerCountAfterClick",
                           AiServerEvidence.minItemCountAfterReply(mc.player.inventoryMenu.containerId, this.replyBaseline, this.worst.item()).orElse(-1),
                           "repliesBefore",
                           this.replyBaseline,
                           "repliesNow",
                           AiServerEvidence.containerReplyCount(mc.player.inventoryMenu.containerId),
                           "confirmed",
                           false
                        )
                     );
                  }

                  out.add("dropped", this.dropped);
                  return out;
               }

               AiOperation.Result running(String message) {
                  return new AiOperation.Result(AiOperation.Status.RUNNING, message, this.progressData().toString(), List.of());
               }

               AiOperation.Result finish(AiOperation.Status status, String message) {
                  JsonObject data = this.progressData();
                  this.cancel();
                  return new AiOperation.Result(status, message, data.toString(), List.of());
               }

               AiOperation.Result failFull(String message) {
                  JsonObject receipt = this.progressData();
                  this.cancel();
                  return AiOperation.Result.failed(AiFailure.INVENTORY_FULL, message, receipt.toString());
               }

               @Override
               public void cancel() {
                  this.cancelled = true;
               }

               @Override
               public boolean canInterruptNow() {
                  return this.pendingSlot < 0;
               }
            };
      } else {
         throw new IllegalArgumentException("Tidying requires a connected living player");
      }
   }

   static JsonObject rankingJson(Minecraft mc, List<AiItems.ItemSpec> keep, int wantFree) {
      JsonArray ranked = new JsonArray();

      for (AiTidy.SlotRank entry : rankSlots(mc, keep)) {
         ranked.add(
            AiJson.objectOf("slot", entry.slot(), "item", entry.item(), "count", entry.count(), "rank", entry.rank(), "tier", rankCategory(entry.rank()))
         );
      }

      AiTidy.SlotRank worst = worstSlot(mc, keep);
      JsonObject out = AiJson.objectOf("freeSlots", AiItems.countFreeSlots(), "wanted", wantFree, "wouldDropFirst", worst == null ? null : worst.item());
      out.add("inventory", ranked);
      out.addProperty(
         "policy",
         "Higher rank is kept longer: tools 100, food 90, valuables 80, utility 60, building blocks 40, bulk 20, junk 0. Equipped armour, the offhand and the held item are never candidates. Plain wooden/stone/iron mining spares dominated by an observed equally capable, longer-lasting replacement rank as junk, subject to keep lists and resource reserves."
      );
      return out;
   }

   static List<AiTidy.SlotRank> rankSlots(Minecraft mc, List<AiItems.ItemSpec> keep) {
      List<AiTidy.SlotRank> ranked = new ArrayList<>();
      if (mc.player == null) {
         return ranked;
      }

      Inventory inventory = mc.player.getInventory();
      int selected = inventory.getSelectedSlot();
      List<ItemStack> held = new ArrayList<>(36);

      for (int slot = 0; slot < 36; slot++) {
         held.add(inventory.getItem(slot));
      }

      int blocks = 0;

      for (int slot = 0; slot < 36; slot++) {
         if (init_Z(inventory.getItem(slot))) {
            blocks += inventory.getItem(slot).getCount();
         }
      }

      int wood = autismclient.ai.engine.AiRestock.totalWoodUnits(inventory);
      int keptWood = autismclient.ai.engine.AiRestock.reservedWood();
      int sticks = 0;
      int stone = 0;

      for (int slot = 0; slot < 36; slot++) {
         ItemStack stack = inventory.getItem(slot);
         if (stack.is(Items.STICK)) {
            sticks += stack.getCount();
         } else if (autismclient.ai.engine.AiRestock.isStoneBlock(stack)) {
            stone += stack.getCount();
         }
      }

      for (int slot = 0; slot < 36; slot++) {
         ItemStack stack = inventory.getItem(slot);
         if (!stack.isEmpty() && slot != selected && !isKept(stack, keep)) {
            int reserved = Math.max(AiRunResources.totalReservedCount(stack.getItem()), autismclient.ai.engine.AiRestock.reservedItemCount(stack.getItem()));
            if ((reserved <= 0 || AiGameState.countMainInventoryItem(itemId(stack)) - stack.getCount() >= reserved) && (!init_Z(stack) || blocks - stack.getCount() >= 64)) {
               int stackWood = stack.is(ItemTags.PLANKS) ? stack.getCount() : (stack.is(ItemTags.LOGS) ? 4 * stack.getCount() : 0);
               if ((stackWood <= 0 || wood - stackWood >= keptWood)
                  && (!stack.is(Items.STICK) || sticks - stack.getCount() >= 2)
                  && (!autismclient.ai.engine.AiRestock.isStoneBlock(stack) || stone - stack.getCount() >= autismclient.ai.engine.AiRestock.baseStoneReserve())) {
                  int rank = rankAgainstHeld(stack, held);
                  ranked.add(new AiTidy.SlotRank(slot, itemId(stack), stack.getCount(), rank));
               }
            }
         }
      }

      ranked.sort((left, right) -> {
         int byRank = Integer.compare(left.rank(), right.rank());
         return byRank != 0 ? byRank : Integer.compare(left.count(), right.count());
      });
      return ranked;
   }

   static AiTidy.SlotRank worstSlot(Minecraft mc, List<AiItems.ItemSpec> keep) {
      List<AiTidy.SlotRank> ranked = rankSlots(mc, keep);
      if (ranked.isEmpty()) {
         return null;
      }

      AiTidy.SlotRank worst = ranked.get(0);
      return worst.rank() >= 80 ? null : worst;
   }

   static int init_I(ItemStack stack) {
      if (stack.isEmpty()) {
         return 0;
      } else {
         String id = itemId(stack);
         if (stack.isDamageableItem()) {
            return 100;
         } else if (JUNK_ITEMS.contains(id)) {
            return 0;
         } else if (stack.has(DataComponents.FOOD)) {
            return 90;
         } else if (VALUABLE_ITEMS.contains(id) || KEEP_ITEMS.contains(id) || stack.is(ItemTags.BEDS) || stack.is(ItemTags.WOOL)) {
            return 80;
         } else if (UTILITY_ITEMS.contains(id)) {
            return 60;
         } else {
            return stack.getItem() instanceof BlockItem ? 40 : 20;
         }
      }
   }

   static int rankAgainstHeld(ItemStack stack, List<ItemStack> held) {
      if (AiItems.isDisposableTierTool(stack)) {
         for (ItemStack replacement : held) {
            if (replacement != stack && isReplacedBy(stack, replacement)) {
               return 0;
            }
         }
      }

      return init_I(stack);
   }

   static boolean isReplacedBy(ItemStack stack, ItemStack replacement) {
      if (AiItems.isDisposableTierTool(stack) && replacement != null && AiItems.hasEnoughDurability(replacement, 2) && stack.has(DataComponents.TOOL) && replacement.has(DataComponents.TOOL)) {
         String kind = toolKind(stack);
         int tier = toolTier(stack);
         int otherTier = toolTier(replacement);
         if (!kind.isEmpty() && kind.equals(toolKind(replacement)) && tier >= 0 && otherTier >= tier && durabilityLeft(replacement) >= durabilityLeft(stack)) {
            for (BlockState state : probeBlocksFor(kind)) {
               float speed = stack.getDestroySpeed(state);
               float otherSpeed = replacement.getDestroySpeed(state);
               if (!Float.isFinite(speed)
                  || !Float.isFinite(otherSpeed)
                  || otherSpeed < speed
                  || stack.isCorrectToolForDrops(state) && !replacement.isCorrectToolForDrops(state)) {
                  return false;
               }
            }

            return true;
         } else {
            return false;
         }
      } else {
         return false;
      }
   }

   private static int durabilityLeft(ItemStack stack) {
      return stack.getMaxDamage() - stack.getDamageValue();
   }

   private static String toolKind(ItemStack stack) {
      if (!stack.isEmpty() && BuiltInRegistries.ITEM.getKey(stack.getItem()).getNamespace().equals("minecraft")) {
         String path = BuiltInRegistries.ITEM.getKey(stack.getItem()).getPath();

         for (String kind : List.of("pickaxe", "axe", "shovel")) {
            if (path.endsWith("_" + kind)) {
               return kind;
            }
         }

         return "";
      } else {
         return "";
      }
   }

   private static int toolTier(ItemStack stack) {
      String path = BuiltInRegistries.ITEM.getKey(stack.getItem()).getPath();
      byte var10000;
      switch (path.substring(0, path.indexOf(95) < 0 ? path.length() : path.indexOf(95))) {
         case "wooden":
         case "golden":
            var10000 = 0;
            break;
         case "stone":
            var10000 = 1;
            break;
         case "iron":
            var10000 = 2;
            break;
         case "diamond":
         case "netherite":
            var10000 = 3;
            break;
         default:
            var10000 = -1;
      }

      return var10000;
   }

   private static List<BlockState> probeBlocksFor(String kind) {
      List var10000;
      switch (kind) {
         case "pickaxe":
            var10000 = List.of(
               Blocks.STONE.defaultBlockState(),
               Blocks.DEEPSLATE.defaultBlockState(),
               Blocks.IRON_ORE.defaultBlockState(),
               Blocks.DIAMOND_ORE.defaultBlockState(),
               Blocks.OBSIDIAN.defaultBlockState()
            );
            break;
         case "axe":
            var10000 = List.of(Blocks.OAK_LOG.defaultBlockState(), Blocks.CRAFTING_TABLE.defaultBlockState(), Blocks.BOOKSHELF.defaultBlockState());
            break;
         case "shovel":
            var10000 = List.of(Blocks.DIRT.defaultBlockState(), Blocks.SAND.defaultBlockState(), Blocks.GRAVEL.defaultBlockState());
            break;
         default:
            var10000 = List.of();
      }

      return var10000;
   }

   static boolean init_Z(ItemStack stack) {
      if (!stack.isEmpty() && stack.getItem() instanceof BlockItem item) {
         Block block = item.getBlock();
         BlockState state = block.defaultBlockState();
         String id = itemId(stack);
         if (VALUABLE_ITEMS.contains(id)
            || KEEP_ITEMS.contains(id)
            || stack.is(ItemTags.BEDS)
            || stack.is(ItemTags.WOOL)
            || stack.is(ItemTags.LOGS)
            || stack.is(ItemTags.PLANKS)) {
            return false;
         }

         if (!(block instanceof FallingBlock) && !state.hasBlockEntity()) {
            try {
               return state.isCollisionShapeFullBlock(EmptyBlockGetter.INSTANCE, BlockPos.ZERO);
            } catch (RuntimeException e) {
               return false;
            }
         } else {
            return false;
         }
      } else {
         return false;
      }
   }

   static String rankCategory(int rank) {
      if (rank >= 100) {
         return "tool";
      } else if (rank >= 90) {
         return "food";
      } else if (rank >= 80) {
         return "valuable";
      } else if (rank >= 60) {
         return "utility";
      } else if (rank >= 40) {
         return "building";
      } else {
         return rank >= 20 ? "bulk" : "junk";
      }
   }

   private static boolean isKept(ItemStack stack, List<AiItems.ItemSpec> keep) {
      for (AiItems.ItemSpec target : keep) {
         if (target.matches(stack)) {
            return true;
         }
      }

      return false;
   }

   private static String itemId(ItemStack stack) {
      return BuiltInRegistries.ITEM.getKey(stack.getItem()).toString();
   }

   private static int screenSlotOrThrow(Minecraft mc, int inventorySlot) {
      int handler = autismclient.util.InventorySlotHelper.toScreenSlot(mc, inventorySlot);
      if (handler < 0) {
         throw new IllegalStateException("Inventory slot " + inventorySlot + " has no menu slot");
      } else {
         return handler;
      }
   }

   private static AiTidy.TidyAction parseAction(String text) {
      try {
         return AiTidy.TidyAction.valueOf(text.trim().toUpperCase(Locale.ROOT));
      } catch (IllegalArgumentException e) {
         throw new IllegalArgumentException("action must be inspect or drop");
      }
   }

   enum TidyAction {
      INSPECT,
      DROP;
   }

   record SlotRank(int slot, String item, int count, int rank) {
   }
}

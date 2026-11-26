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
   private static final List<String> init_List = List.of(
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
   private static final List<String> load_List = List.of(
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
   private static final List<String> read_List = List.of(
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
   private static final List<String> write_List = List.of(
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

   static AiOperation init(JsonObject args) {
      final Minecraft mc = Minecraft.getInstance();
      if (mc.player != null && mc.level != null) {
         AiTidy.Inner_DataUtil action = init_cx_DataUtil(AiJson.init_Str_JsonObject(args, "action", "inspect"));
         final int wantFree = Math.max(1, Math.min(36, AiJson.init_I_JsonObject(args, "freeSlots", 4)));
         final int maxDrops = Math.max(1, Math.min(36, AiJson.init_I_JsonObject(args, "maxDrops", 9)));
         final int timeoutSeconds = Math.max(5, Math.min(300, AiJson.init_I_JsonObject(args, "timeoutSeconds", 60)));
         final List<AiItems.Inner_DataUtil> keep = new ArrayList<>();
         if (args.has("keep") && args.get("keep").isJsonArray()) {
            for (JsonElement element : args.getAsJsonArray("keep")) {
               keep.add(AiItems.init_af_DataUtil(element.getAsString()));
            }
         } else if (args.has("keep") && args.get("keep").isJsonPrimitive()) {
            for (String part : args.get("keep").getAsString().split(",")) {
               if (!part.isBlank()) {
                  keep.add(AiItems.init_af_DataUtil(part.trim()));
               }
            }
         }

         return action == AiTidy.Inner_DataUtil.init
            ? AiGameOperations.init_AiOperation(AiOperation.Status.SUCCEEDED, "Ranked " + AiItems.init_I() + " free slot(s) and the current inventory", init(mc, keep, wantFree))
            : new AiOperation() {
               final long init_J = System.nanoTime() + timeoutSeconds * 1000000000L;
               final int init_I = AiItems.init_I();
               final Object init_Obj = mc.level;
               final Object load_Obj = mc.getConnection();
               final Object read_Obj = mc.player;
               final JsonArray init_JsonArray = new JsonArray();
               int load_I = -1;
               int read_I;
               long load_J;
               int write;
               AiTidy.Inner_StreamUtil init_cx_StreamUtil;
               int update;
               boolean init_Z;

               @Override
               public AiOperation.Result tick() {
                  if (!this.init_Z && mc.player != null) {
                     if (mc.level != this.init_Obj || mc.getConnection() != this.load_Obj || mc.player != this.read_Obj || !mc.player.isAlive()) {
                        return this.init(AiOperation.Status.UNCERTAIN, "Tidy player or world changed; reconcile any pending drop");
                     }

                     if (System.nanoTime() > this.init_J) {
                        return this.init(this.load_I >= 0 ? AiOperation.Status.UNCERTAIN : AiOperation.Status.TIMED_OUT, "Tidy deadline reached");
                     }

                     if (mc.player.containerMenu != mc.player.inventoryMenu) {
                        return this.init(AiOperation.Status.FAILED, "Close the open container before tidying");
                     }

                     if (!mc.player.containerMenu.getCarried().isEmpty()) {
                        return this.init(AiOperation.Status.FAILED, "Clear the cursor stack before tidying");
                     }

                     if (this.load_I >= 0) {
                        OptionalInt confirmedCount = AiServerEvidence.init_OptionalInt_I(mc.player.inventoryMenu.containerId, this.load_J, this.init_cx_StreamUtil.item());
                        if (confirmedCount.isPresent() && confirmedCount.getAsInt() <= this.write - this.init_cx_StreamUtil.count()) {
                           this.init_JsonArray
                              .add(
                                 AiJson.init_JsonObject_ObjArr(
                                    "slot",
                                    this.init_cx_StreamUtil.slot(),
                                    "item",
                                    this.init_cx_StreamUtil.item(),
                                    "count",
                                    this.init_cx_StreamUtil.count(),
                                    "rank",
                                    this.init_cx_StreamUtil.rank(),
                                    "confirmed",
                                    true
                                 )
                              );
                           this.update++;
                           this.load_I = -1;
                           this.init_cx_StreamUtil = null;
                           this.read_I = 0;
                           return this.init("Drop confirmed");
                        } else {
                           return ++this.read_I > 20
                              ? this.init(AiOperation.Status.UNCERTAIN, "A drop was not confirmed by the server; reobserve before repeating")
                              : this.init("Waiting for the drop to apply");
                        }
                     } else {
                        if (AiItems.init_I() >= wantFree) {
                           return this.init(
                              AiOperation.Status.SUCCEEDED, "Inventory has " + AiItems.init_I() + " free slot(s) after dropping " + this.update + " stack(s)"
                           );
                        }

                        if (this.update >= maxDrops) {
                           return this.load(
                              "Dropped the allowed "
                                 + maxDrops
                                 + " stack(s) and still have only "
                                 + AiItems.init_I()
                                 + " free slot(s); raise maxDrops or deposit into a container"
                           );
                        }

                        AiTidy.Inner_StreamUtil worst = AiTidy.init_cx_StreamUtil(mc, keep);
                        if (worst == null) {
                           return this.load("Nothing left is safe to drop; everything remaining is kept, equipped or high value");
                        }

                        this.load_J = AiServerEvidence.load_J_I(mc.player.inventoryMenu.containerId);
                        this.load_I = worst.slot();
                        this.init_cx_StreamUtil = worst;
                        this.write = AiCraft.init_I(worst.item());
                        AiContainerTransactions.init(mc.player.containerMenu.containerId, AiTidy.init(mc, worst.slot()), 1, ContainerInput.THROW);
                        return this.init("Dropping " + worst.count() + " " + worst.item());
                     }
                  } else {
                     return this.init(AiOperation.Status.CANCELLED, "Tidy cancelled");
                  }
               }

               JsonObject init() {
                  JsonObject out = AiJson.init_JsonObject_ObjArr(
                     "freeSlots", AiItems.init_I(), "target", wantFree, "startedWith", this.init_I, "drops", this.update, "maxDrops", maxDrops
                  );
                  if (this.init_cx_StreamUtil != null) {
                     out.add(
                        "pendingDrop",
                        AiJson.init_JsonObject_ObjArr(
                           "slot",
                           this.load_I,
                           "item",
                           this.init_cx_StreamUtil.item(),
                           "count",
                           this.init_cx_StreamUtil.count(),
                           "beforeCount",
                           this.write,
                           "currentCount",
                           AiCraft.init_I(this.init_cx_StreamUtil.item()),
                           "minimumServerCountAfterClick",
                           AiServerEvidence.init_OptionalInt_I(mc.player.inventoryMenu.containerId, this.load_J, this.init_cx_StreamUtil.item()).orElse(-1),
                           "repliesBefore",
                           this.load_J,
                           "repliesNow",
                           AiServerEvidence.load_J_I(mc.player.inventoryMenu.containerId),
                           "confirmed",
                           false
                        )
                     );
                  }

                  out.add("dropped", this.init_JsonArray);
                  return out;
               }

               AiOperation.Result init(String message) {
                  return new AiOperation.Result(AiOperation.Status.RUNNING, message, this.init().toString(), List.of());
               }

               AiOperation.Result init(AiOperation.Status status, String message) {
                  JsonObject data = this.init();
                  this.cancel();
                  return new AiOperation.Result(status, message, data.toString(), List.of());
               }

               AiOperation.Result load(String message) {
                  JsonObject receipt = this.init();
                  this.cancel();
                  return AiOperation.Result.failed(AiFailure.put, message, receipt.toString());
               }

               @Override
               public void cancel() {
                  this.init_Z = true;
               }

               @Override
               public boolean canInterruptNow() {
                  return this.load_I < 0;
               }
            };
      } else {
         throw new IllegalArgumentException("Tidying requires a connected living player");
      }
   }

   static JsonObject init(Minecraft mc, List<AiItems.Inner_DataUtil> keep, int wantFree) {
      JsonArray ranked = new JsonArray();

      for (AiTidy.Inner_StreamUtil entry : init_List(mc, keep)) {
         ranked.add(
            AiJson.init_JsonObject_ObjArr("slot", entry.slot(), "item", entry.item(), "count", entry.count(), "rank", entry.rank(), "tier", init(entry.rank()))
         );
      }

      AiTidy.Inner_StreamUtil worst = init_cx_StreamUtil(mc, keep);
      JsonObject out = AiJson.init_JsonObject_ObjArr("freeSlots", AiItems.init_I(), "wanted", wantFree, "wouldDropFirst", worst == null ? null : worst.item());
      out.add("inventory", ranked);
      out.addProperty(
         "policy",
         "Higher rank is kept longer: tools 100, food 90, valuables 80, utility 60, building blocks 40, bulk 20, junk 0. Equipped armour, the offhand and the held item are never candidates. Plain wooden/stone/iron mining spares dominated by an observed equally capable, longer-lasting replacement rank as junk, subject to keep lists and resource reserves."
      );
      return out;
   }

   static List<AiTidy.Inner_StreamUtil> init_List(Minecraft mc, List<AiItems.Inner_DataUtil> keep) {
      List<AiTidy.Inner_StreamUtil> ranked = new ArrayList<>();
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

      int wood = autismclient.ai.a.AiRestock.load(inventory);
      int keptWood = autismclient.ai.a.AiRestock.init_I();
      int sticks = 0;
      int stone = 0;

      for (int slot = 0; slot < 36; slot++) {
         ItemStack stack = inventory.getItem(slot);
         if (stack.is(Items.STICK)) {
            sticks += stack.getCount();
         } else if (autismclient.ai.a.AiRestock.init(stack)) {
            stone += stack.getCount();
         }
      }

      for (int slot = 0; slot < 36; slot++) {
         ItemStack stack = inventory.getItem(slot);
         if (!stack.isEmpty() && slot != selected && !init_Z(stack, keep)) {
            int reserved = Math.max(AiRunResources.init(stack.getItem()), autismclient.ai.a.AiRestock.init(stack.getItem()));
            if ((reserved <= 0 || AiGameState.load(load_Str(stack)) - stack.getCount() >= reserved) && (!init_Z(stack) || blocks - stack.getCount() >= 64)) {
               int stackWood = stack.is(ItemTags.PLANKS) ? stack.getCount() : (stack.is(ItemTags.LOGS) ? 4 * stack.getCount() : 0);
               if ((stackWood <= 0 || wood - stackWood >= keptWood)
                  && (!stack.is(Items.STICK) || sticks - stack.getCount() >= 2)
                  && (!autismclient.ai.a.AiRestock.init(stack) || stone - stack.getCount() >= autismclient.ai.a.AiRestock.read())) {
                  int rank = init_I(stack, held);
                  ranked.add(new AiTidy.Inner_StreamUtil(slot, load_Str(stack), stack.getCount(), rank));
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

   static AiTidy.Inner_StreamUtil init_cx_StreamUtil(Minecraft mc, List<AiItems.Inner_DataUtil> keep) {
      List<AiTidy.Inner_StreamUtil> ranked = init_List(mc, keep);
      if (ranked.isEmpty()) {
         return null;
      }

      AiTidy.Inner_StreamUtil worst = ranked.get(0);
      return worst.rank() >= 80 ? null : worst;
   }

   static int init_I(ItemStack stack) {
      if (stack.isEmpty()) {
         return 0;
      } else {
         String id = load_Str(stack);
         if (stack.isDamageableItem()) {
            return 100;
         } else if (read_List.contains(id)) {
            return 0;
         } else if (stack.has(DataComponents.FOOD)) {
            return 90;
         } else if (init_List.contains(id) || write_List.contains(id) || stack.is(ItemTags.BEDS) || stack.is(ItemTags.WOOL)) {
            return 80;
         } else if (load_List.contains(id)) {
            return 60;
         } else {
            return stack.getItem() instanceof BlockItem ? 40 : 20;
         }
      }
   }

   static int init_I(ItemStack stack, List<ItemStack> held) {
      if (AiItems.load_Z(stack)) {
         for (ItemStack replacement : held) {
            if (replacement != stack && init(stack, replacement)) {
               return 0;
            }
         }
      }

      return init_I(stack);
   }

   static boolean init(ItemStack stack, ItemStack replacement) {
      if (AiItems.load_Z(stack) && replacement != null && AiItems.init(replacement, 2) && stack.has(DataComponents.TOOL) && replacement.has(DataComponents.TOOL)) {
         String kind = init_Str(stack);
         int tier = read(stack);
         int otherTier = read(replacement);
         if (!kind.isEmpty() && kind.equals(init_Str(replacement)) && tier >= 0 && otherTier >= tier && load_I(replacement) >= load_I(stack)) {
            for (BlockState state : init_List(kind)) {
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

   private static int load_I(ItemStack stack) {
      return stack.getMaxDamage() - stack.getDamageValue();
   }

   private static String init_Str(ItemStack stack) {
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

   private static int read(ItemStack stack) {
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

   private static List<BlockState> init_List(String kind) {
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
         String id = load_Str(stack);
         if (init_List.contains(id)
            || write_List.contains(id)
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

   static String init(int rank) {
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

   private static boolean init_Z(ItemStack stack, List<AiItems.Inner_DataUtil> keep) {
      for (AiItems.Inner_DataUtil target : keep) {
         if (target.matches(stack)) {
            return true;
         }
      }

      return false;
   }

   private static String load_Str(ItemStack stack) {
      return BuiltInRegistries.ITEM.getKey(stack.getItem()).toString();
   }

   private static int init(Minecraft mc, int inventorySlot) {
      int handler = autismclient.util.InventorySlotHelper.load_I(mc, inventorySlot);
      if (handler < 0) {
         throw new IllegalStateException("Inventory slot " + inventorySlot + " has no menu slot");
      } else {
         return handler;
      }
   }

   private static AiTidy.Inner_DataUtil init_cx_DataUtil(String text) {
      try {
         return AiTidy.Inner_DataUtil.valueOf(text.trim().toUpperCase(Locale.ROOT));
      } catch (IllegalArgumentException e) {
         throw new IllegalArgumentException("action must be inspect or drop");
      }
   }

   enum Inner_DataUtil {
      init,
      load;
   }

   record Inner_StreamUtil(int slot, String item, int count, int rank) {
   }
}

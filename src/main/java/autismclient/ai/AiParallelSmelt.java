package autismclient.ai;

import autismclient.api.ai.AiOperation;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.inventory.AbstractFurnaceMenu;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;

final class AiParallelSmelt implements AiOperation {
   static final int init_I = 8;
   private final Minecraft init_Minecraft = Minecraft.getInstance();
   private final Object init_Obj = this.init_Minecraft.level;
   private final Object load_Obj = this.init_Minecraft.getConnection();
   private final Object read_Obj = this.init_Minecraft.player;
   private final AiSmelt.Inner_CachePool init_ci_CachePool;
   private final String init_Str;
   private final String load_Str;
   private final String read_Str;
   private final int load_I;
   private final int read_I;
   private final long init_J;
   private final List<BlockPos> init_List = new ArrayList<>();
   private final JsonArray init_JsonArray = new JsonArray();
   private final AiGuardLayer init_Y = new AiGuardLayer(AiGuard.GuardPolicy.standard(), "smelt");
   private List<Integer> load_List = List.of();
   private final List<Integer> read_List = new ArrayList<>();
   private AiOperation init_AiOperation;
   private AiStationPlacer init_cn;
   private boolean init_Z;
   private boolean load_Z;
   private boolean read_Z;
   private boolean write_Z;
   private boolean update_Z;
   private boolean sync_Z;
   private int write_I;
   private int update_I;
   private int sync_I;
   private int flush;
   private int close;
   private int open;
   private String write_Str = "Choose nearby furnaces before making any";

   static int init_I_I_3(int items) {
      return Math.max(1, Math.min(8, items / 16));
   }

   static List<Integer> init_List_I_I(int items, int furnaces) {
      int count = Math.max(1, Math.min(8, furnaces));
      int batch = Math.min(items, count * 64);
      List<Integer> out = new ArrayList<>();

      for (int i = 0; i < count && i < batch; i++) {
         out.add(batch / count + (i < batch % count ? 1 : 0));
      }

      return List.copyOf(out);
   }

   static boolean init_Z(int items, int existing, int cookTicks, int setupTicks) {
      int before = (items + Math.max(1, existing) - 1) / Math.max(1, existing);
      int after = (items + existing) / (existing + 1);
      return existing < init_I_I_3(items) && (long)(before - after) * cookTicks > setupTicks;
   }

   static int init_I(int items, int furnaces, int cookTicks, int burnTicks) {
      if (burnTicks <= 0) {
         return 2147483647;
      }

      int total = 0;

      while (items > 0) {
         List<Integer> wave = init_List_I_I(items, furnaces);

         for (int batch : wave) {
            total += (int)(((long)batch * cookTicks + burnTicks - 1L) / burnTicks);
         }

         items -= wave.stream().mapToInt(Integer::intValue).sum();
      }

      return total;
   }

   AiParallelSmelt(AiSmelt.Inner_CachePool recipe, int target, int cookTicks, int seconds) {
      this.init_ci_CachePool = recipe;
      this.load_I = target;
      this.read_I = cookTicks;
      this.init_Str = BuiltInRegistries.ITEM.getKey(recipe.output()).toString();
      this.read_Str = "Smelting " + recipe.output().getDefaultInstance().getHoverName().getString();
      this.load_Str = BuiltInRegistries.ITEM.getKey(recipe.input()).toString();
      this.init_J = System.nanoTime() + seconds * 1000000000L;
      this.flush = init_I_I_3(target - AiGameState.countInventoryItem(this.init_Str));
      BlockPos origin = this.init_Minecraft.player.blockPosition();
      Block block = AiCraft.requireBlock(recipe.stationId());

      for (BlockPos p : BlockPos.betweenClosed(origin.offset(-16, -8, -16), origin.offset(16, 8, 16))) {
         if (AiTerrain.isLoaded(this.init_Minecraft.level, p) && this.init_Minecraft.level.getBlockState(p).getBlock() == block) {
            this.init_List.add(p.immutable());
         }
      }

      this.init_List.sort(Comparator.comparingDouble(px -> px.distSqr(origin)));
      if (this.init_List.size() > this.flush) {
         this.init_List.subList(this.flush, this.init_List.size()).clear();
      }

      this.sync_I = this.init_List.size();
   }

   @Override
   public AiOperation.Result tick() {
      if (this.load_Z
         || this.init_Minecraft.player == null
         || this.init_Minecraft.player != this.read_Obj
         || this.init_Minecraft.level != this.init_Obj
         || this.init_Minecraft.getConnection() != this.load_Obj) {
         return this.init_AiOperation_Result_AiOperation_Status_Str_6(
            AiOperation.Status.CANCELLED, "Smelting world/player changed; loaded furnaces keep cooking"
         );
      }

      if (!this.init_Minecraft.player.isAlive()) {
         return this.init_AiOperation_Result_AiOperation_Status_Str_6(AiOperation.Status.FAILED, "Player died while smelting");
      }

      if (System.nanoTime() >= this.init_J) {
         return this.init_AiOperation_Result_AiOperation_Status_Str_6(
            this.canInterruptNow() ? AiOperation.Status.TIMED_OUT : AiOperation.Status.UNCERTAIN, "Smelting deadline reached"
         );
      }

      AiOperation.Result reflex = this.init_Y.tick(this, () -> {
         if (this.init_AiOperation != null) {
            this.init_AiOperation.cancel();
            this.init_AiOperation = null;
            this.sync_Z = this.init_Z;
         }

         if (this.init_cn != null) {
            this.init_cn.cancel();
            this.init_cn = null;
         }

         if (this.init_Minecraft.player.containerMenu instanceof AbstractFurnaceMenu) {
            this.init_Minecraft.player.closeContainer();
         }
      }, this::init_I_20);
      if (reflex != null) {
         return reflex.terminal()
            ? this.init_AiOperation_Result_AiOperation_Status_Str_6(reflex.status(), reflex.message())
            : this.init_AiOperation_Result_Str_8(reflex.message());
      }

      if (this.init_AiOperation != null) {
         AiOperation.Result r = this.init_AiOperation.tick();
         if (!r.terminal()) {
            return this.init_AiOperation_Result_Str_8(this.write_Str + ": " + r.message());
         }

         boolean uncertain = r.status() == AiOperation.Status.UNCERTAIN || this.init_AiOperation.requiresReconciliationAfterEmergency();
         this.init_AiOperation.cancel();
         this.init_AiOperation = null;
         this.init_JsonArray
            .add(AiJson.objectOf("activity", this.write_Str, "status", r.status().name(), "receipt", AiRunResources.parseEvidenceJson(r.dataJson())));
         if (uncertain) {
            return this.init_AiOperation_Result_AiOperation_Status_Str_6(AiOperation.Status.UNCERTAIN, r.message());
         }

         if (r.status() == AiOperation.Status.CANCELLED) {
            return this.init_AiOperation_Result_AiOperation_Status_Str_6(r.status(), r.message());
         }

         if (r.status() != AiOperation.Status.SUCCEEDED) {
            if (!this.init_Z && this.update_Z && this.init_List.isEmpty()) {
               return this.init_AiOperation_Result_AiOperation_Status_Str_6(r.status(), r.message());
            }

            if (this.init_Z) {
               return this.init_AiOperation_Result_AiOperation_Status_Str_6(r.status(), "Furnace " + (this.write_I + 1) + ": " + r.message());
            }

            this.write_Z = true;
            this.flush = Math.max(1, this.init_List.size());
         } else if (this.init_Z) {
            if (!this.read_Z) {
               this.read_List
                  .set(
                     this.write_I,
                     Math.min(this.load_List.get(this.write_I), this.read_List.get(this.write_I) + Math.max(0, AiGameState.countInventoryItem(this.init_Str) - this.open))
                  );
            }

            this.write_I++;
            this.close = 0;
         }
      }

      if (!this.init_Z) {
         return this.init_AiOperation_Result_19();
      }

      if (AiGameState.countInventoryItem(this.init_Str) >= this.load_I) {
         return this.init_AiOperation_Result_AiOperation_Status_Str_6(
            AiOperation.Status.SUCCEEDED, "Smelted " + this.init_Str + " using " + this.init_List.size() + " furnaces"
         );
      }

      if (this.load_List.isEmpty()) {
         this.load_List = init_List_I_I(this.load_I - AiGameState.countInventoryItem(this.init_Str), this.init_List.size());
         this.write_I = 0;
         this.read_Z = false;
         this.read_List.clear();

         for (int ignored : this.load_List) {
            this.read_List.add(0);
         }
      }

      if (this.write_I >= this.load_List.size()) {
         if (this.read_Z) {
            this.load_List = List.of();
            return this.tick();
         }

         this.read_Z = true;
         this.write_I = 0;
      }

      int amount = this.load_List.get(this.write_I) - (this.read_Z ? this.read_List.get(this.write_I) : 0);
      if (amount == 0) {
         this.write_I++;
         this.close = 0;
         return this.tick();
      }

      BlockPos station = this.init_List.get(this.write_I);
      if (this.read_Z && this.close == 0) {
         this.close = Math.min(this.load_I, AiGameState.countInventoryItem(this.init_Str) + amount);
      }

      Item availableFuel = this.read_Z ? this.init_ci_CachePool.fuel() : AiSmelt.init("", this.init_ci_CachePool.input(), amount);
      String fuel = BuiltInRegistries.ITEM.getKey(availableFuel == null ? this.init_ci_CachePool.fuel() : availableFuel).toString();
      if (!this.sync_Z) {
         this.open = AiGameState.countInventoryItem(this.init_Str);
      }

      this.sync_Z = false;
      this.init_AiOperation = AiSmelt.load(
         AiJson.objectOf(
            "item",
            this.init_Str,
            "input",
            this.load_Str,
            "fuel",
            fuel,
            "count",
            this.read_Z ? this.close : AiGameState.countInventoryItem(this.init_Str) + amount,
            "batchSize",
            amount,
            "x",
            station.getX(),
            "y",
            station.getY(),
            "z",
            station.getZ(),
            "station",
            this.init_ci_CachePool.stationId(),
            "collect",
            this.read_Z,
            "loadOnly",
            !this.read_Z,
            "singleFurnace",
            true,
            "timeoutSeconds",
            this.init_I_20()
         )
      );
      this.write_Str = (this.read_Z ? "Wait and collect at furnace " : "Load furnace ") + (this.write_I + 1) + "/" + this.load_List.size();
      return this.init_AiOperation_Result_Str_8(this.write_Str);
   }

   private AiOperation.Result init_AiOperation_Result_19() {
      if (this.init_cn != null) {
         AiStationPlacer.Inner_DataUtil state = this.init_cn.tick();
         if (state == AiStationPlacer.Inner_DataUtil.PLACED) {
            this.init_List.add(this.init_cn.getPlaced());
            this.init_cn = null;
            this.update_I++;
         } else {
            if (state != AiStationPlacer.Inner_DataUtil.FAILED) {
               return this.init_AiOperation_Result_Str_8(this.init_cn.getStatus());
            }

            String why = this.init_cn.getFailureReason();
            this.init_cn.cancel();
            this.init_cn = null;
            if (this.init_List.isEmpty()) {
               return this.init_AiOperation_Result_AiOperation_Status_Str_6(AiOperation.Status.FAILED, "Cannot place furnace: " + why);
            }

            this.write_Z = true;
            this.flush = this.init_List.size();
         }
      }

      if (this.init_List.size() >= this.flush) {
         this.init_Z = true;
         return this.tick();
      }

      if (AiGameState.countInventoryItem(this.init_ci_CachePool.stationId()) > 0) {
         this.init_cn = new AiStationPlacer(
            this.init_ci_CachePool.stationId().equals("minecraft:furnace")
               ? Items.FURNACE
               : BuiltInRegistries.ITEM.getValue(Identifier.parse(this.init_ci_CachePool.stationId())),
            "smelting station"
         );
         return this.init_AiOperation_Result_Str_8("Place a carried smelting station");
      }

      int missing = this.load_I - AiGameState.countInventoryItem(this.init_Str);
      int neededCobble = 8 + (this.load_Str.equals("minecraft:cobblestone") ? missing : 0);
      int deficit = Math.max(0, neededCobble - AiCraft.countItem("minecraft:cobblestone"));
      boolean tableEasy = AiCraft.isCraftingTableInReach() || AiCraft.countItem("minecraft:crafting_table") > 0 || AiCraftPlanner.init(AiCraft.buildRecipeGraph(), "minecraft:crafting_table", 1).complete();
      double stoneDistance = deficit == 0 ? 0.0 : AiTerrain.nearestBlockDistSqr(this.init_Minecraft, List.of(Blocks.STONE, Blocks.COBBLESTONE), 8);
      boolean localStone = deficit == 0 || stoneDistance <= 64.0 && AiItems.hasUsableToolFor(Blocks.STONE.defaultBlockState());
      int setupTicks = 100 + deficit * 40 + (tableEasy ? 0 : 200);
      boolean easy = this.init_ci_CachePool.stationId().equals("minecraft:furnace")
         && tableEasy
         && localStone
         && (this.init_List.isEmpty() || !this.write_Z && init_Z(missing, this.init_List.size(), this.read_I, setupTicks));
      if (easy) {
         if (deficit > 0) {
            this.write_Str = "Mine nearby cobblestone for a faster smelt without spending the requested input";
            this.init_AiOperation = autismclient.ai.engine.AiBlockMine.create(
               AiJson.objectOf(
                  "blocks",
                  autismclient.ai.engine.AiBlockMine.init(List.of("minecraft:stone", "minecraft:cobblestone")),
                  "drops",
                  autismclient.ai.engine.AiBlockMine.init(List.of("minecraft:cobblestone")),
                  "held",
                  neededCobble,
                  "radius",
                  16,
                  "explore",
                  false,
                  "timeoutSeconds",
                  Math.min(30, this.init_I_20())
               )
            );
         } else {
            this.write_Str = "Craft another furnace; its setup costs less than the cooking time saved";
            this.init_AiOperation = AiCraft.craft(
               AiJson.objectOf("item", this.init_ci_CachePool.stationId(), "count", 1, "timeoutSeconds", Math.min(30, this.init_I_20()))
            );
         }

         return this.init_AiOperation_Result_Str_8(this.write_Str);
      } else if (this.init_List.isEmpty()) {
         this.write_Str = "Obtain the first furnace and its prerequisites";
         this.init_AiOperation = AiObtain.init(
            AiJson.objectOf("item", this.init_ci_CachePool.stationId(), "count", 1, "staged", true, "timeoutSeconds", this.init_I_20())
         );
         this.update_Z = true;
         this.write_Z = true;
         this.flush = 1;
         return this.init_AiOperation_Result_Str_8(this.write_Str);
      } else {
         this.flush = this.init_List.size();
         this.init_Z = true;
         return this.tick();
      }
   }

   private int init_I_20() {
      return (int)Math.max(1L, (this.init_J - System.nanoTime()) / 1000000000L);
   }

   private JsonObject init_JsonObject_34() {
      return AiJson.objectOf(
         "item",
         this.init_Str,
         "target",
         this.load_I,
         "held",
         AiGameState.countInventoryItem(this.init_Str),
         "furnaceCount",
         this.init_List.size(),
         "builtFurnaces",
         this.update_I,
         "reusedFurnaces",
         this.sync_I,
         "maximumFurnaces",
         8,
         "batches",
         this.load_List,
         "collecting",
         this.read_Z,
         "activity",
         this.write_Str,
         "receipts",
         this.init_JsonArray
      );
   }

   private AiOperation.Result init_AiOperation_Result_Str_8(String message) {
      AiVisualization.init(this, this.read_Str, AiTaskStatus.init(message, "Preparing the furnaces"), AiGameState.countInventoryItem(this.init_Str), this.load_I);
      return AiGameOperations.result(AiOperation.Status.RUNNING, message, this.init_JsonObject_34());
   }

   private AiOperation.Result init_AiOperation_Result_AiOperation_Status_Str_6(AiOperation.Status status, String message) {
      JsonObject receipt = this.init_JsonObject_34();
      this.cancel();
      return AiGameOperations.result(status, message, receipt);
   }

   @Override
   public boolean canInterruptNow() {
      return this.init_Y.canInterruptNow()
         && (this.init_AiOperation == null || this.init_AiOperation.canInterruptNow())
         && (this.init_cn == null || this.init_cn.canInterrupt());
   }

   @Override
   public boolean requiresReconciliationAfterEmergency() {
      return this.init_Y.requiresReconciliation()
         || this.init_AiOperation != null && this.init_AiOperation.requiresReconciliationAfterEmergency()
         || this.init_cn != null && this.init_cn.needsReconciliation();
   }

   @Override
   public void cancel() {
      this.load_Z = true;
      AiVisualization.init_V(this);
      this.init_Y.cancel();
      if (this.init_AiOperation != null) {
         this.init_AiOperation.cancel();
      }

      if (this.init_cn != null) {
         this.init_cn.cancel();
      }
   }
}

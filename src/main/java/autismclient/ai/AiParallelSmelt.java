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
   static final int MAX_FURNACES = 8;
   private final Minecraft mc = Minecraft.getInstance();
   private final Object startLevel = this.mc.level;
   private final Object startConnection = this.mc.getConnection();
   private final Object startPlayer = this.mc.player;
   private final AiSmelt.SmeltRecipe recipe;
   private final String outputId;
   private final String inputId;
   private final String label;
   private final int target;
   private final int cookTicks;
   private final long deadline;
   private final List<BlockPos> furnaces = new ArrayList<>();
   private final JsonArray receipts = new JsonArray();
   private final AiGuardLayer guard = new AiGuardLayer(AiGuard.GuardPolicy.standard(), "smelt");
   private List<Integer> batches = List.of();
   private final List<Integer> collected = new ArrayList<>();
   private AiOperation child;
   private AiStationPlacer stationPlacer;
   private boolean furnacesReady;
   private boolean cancelled;
   private boolean collecting;
   private boolean furnaceSetupDone;
   private boolean obtainingFirstFurnace;
   private boolean keepCollectBaseline;
   private int furnaceIndex;
   private int builtFurnaces;
   private int reusedFurnaces;
   private int furnaceGoal;
   private int collectTarget;
   private int outputBaseline;
   private String activity = "Choose nearby furnaces before making any";

   static int furnaceCountFor(int items) {
      return Math.max(1, Math.min(8, items / 16));
   }

   static List<Integer> batchSizes(int items, int furnaces) {
      int count = Math.max(1, Math.min(8, furnaces));
      int batch = Math.min(items, count * 64);
      List<Integer> out = new ArrayList<>();

      for (int i = 0; i < count && i < batch; i++) {
         out.add(batch / count + (i < batch % count ? 1 : 0));
      }

      return List.copyOf(out);
   }

   static boolean worthAnotherFurnace(int items, int existing, int cookTicks, int setupTicks) {
      int before = (items + Math.max(1, existing) - 1) / Math.max(1, existing);
      int after = (items + existing) / (existing + 1);
      return existing < furnaceCountFor(items) && (long)(before - after) * cookTicks > setupTicks;
   }

   static int fuelNeeded(int items, int furnaces, int cookTicks, int burnTicks) {
      if (burnTicks <= 0) {
         return 2147483647;
      }

      int total = 0;

      while (items > 0) {
         List<Integer> wave = batchSizes(items, furnaces);

         for (int batch : wave) {
            total += (int)(((long)batch * cookTicks + burnTicks - 1L) / burnTicks);
         }

         items -= wave.stream().mapToInt(Integer::intValue).sum();
      }

      return total;
   }

   AiParallelSmelt(AiSmelt.SmeltRecipe recipe, int target, int cookTicks, int seconds) {
      this.recipe = recipe;
      this.target = target;
      this.cookTicks = cookTicks;
      this.outputId = BuiltInRegistries.ITEM.getKey(recipe.output()).toString();
      this.label = "Smelting " + recipe.output().getDefaultInstance().getHoverName().getString();
      this.inputId = BuiltInRegistries.ITEM.getKey(recipe.input()).toString();
      this.deadline = System.nanoTime() + seconds * 1000000000L;
      this.furnaceGoal = furnaceCountFor(target - AiGameState.countInventoryItem(this.outputId));
      BlockPos origin = this.mc.player.blockPosition();
      Block block = AiCraft.requireBlock(recipe.stationId());

      for (BlockPos p : BlockPos.betweenClosed(origin.offset(-16, -8, -16), origin.offset(16, 8, 16))) {
         if (AiTerrain.isLoaded(this.mc.level, p) && this.mc.level.getBlockState(p).getBlock() == block) {
            this.furnaces.add(p.immutable());
         }
      }

      this.furnaces.sort(Comparator.comparingDouble(px -> px.distSqr(origin)));
      if (this.furnaces.size() > this.furnaceGoal) {
         this.furnaces.subList(this.furnaceGoal, this.furnaces.size()).clear();
      }

      this.reusedFurnaces = this.furnaces.size();
   }

   @Override
   public AiOperation.Result tick() {
      if (this.cancelled
         || this.mc.player == null
         || this.mc.player != this.startPlayer
         || this.mc.level != this.startLevel
         || this.mc.getConnection() != this.startConnection) {
         return this.finish(
            AiOperation.Status.CANCELLED, "Smelting world/player changed; loaded furnaces keep cooking"
         );
      }

      if (!this.mc.player.isAlive()) {
         return this.finish(AiOperation.Status.FAILED, "Player died while smelting");
      }

      if (System.nanoTime() >= this.deadline) {
         return this.finish(
            this.canInterruptNow() ? AiOperation.Status.TIMED_OUT : AiOperation.Status.UNCERTAIN, "Smelting deadline reached"
         );
      }

      AiOperation.Result reflex = this.guard.tick(this, () -> {
         if (this.child != null) {
            this.child.cancel();
            this.child = null;
            this.keepCollectBaseline = this.furnacesReady;
         }

         if (this.stationPlacer != null) {
            this.stationPlacer.cancel();
            this.stationPlacer = null;
         }

         if (this.mc.player.containerMenu instanceof AbstractFurnaceMenu) {
            this.mc.player.closeContainer();
         }
      }, this::secondsLeft);
      if (reflex != null) {
         return reflex.terminal()
            ? this.finish(reflex.status(), reflex.message())
            : this.running(reflex.message());
      }

      if (this.child != null) {
         AiOperation.Result r = this.child.tick();
         if (!r.terminal()) {
            return this.running(this.activity + ": " + r.message());
         }

         boolean uncertain = r.status() == AiOperation.Status.UNCERTAIN || this.child.requiresReconciliationAfterEmergency();
         this.child.cancel();
         this.child = null;
         this.receipts
            .add(AiJson.objectOf("activity", this.activity, "status", r.status().name(), "receipt", AiRunResources.parseEvidenceJson(r.dataJson())));
         if (uncertain) {
            return this.finish(AiOperation.Status.UNCERTAIN, r.message());
         }

         if (r.status() == AiOperation.Status.CANCELLED) {
            return this.finish(r.status(), r.message());
         }

         if (r.status() != AiOperation.Status.SUCCEEDED) {
            if (!this.furnacesReady && this.obtainingFirstFurnace && this.furnaces.isEmpty()) {
               return this.finish(r.status(), r.message());
            }

            if (this.furnacesReady) {
               return this.finish(r.status(), "Furnace " + (this.furnaceIndex + 1) + ": " + r.message());
            }

            this.furnaceSetupDone = true;
            this.furnaceGoal = Math.max(1, this.furnaces.size());
         } else if (this.furnacesReady) {
            if (!this.collecting) {
               this.collected
                  .set(
                     this.furnaceIndex,
                     Math.min(this.batches.get(this.furnaceIndex), this.collected.get(this.furnaceIndex) + Math.max(0, AiGameState.countInventoryItem(this.outputId) - this.outputBaseline))
                  );
            }

            this.furnaceIndex++;
            this.collectTarget = 0;
         }
      }

      if (!this.furnacesReady) {
         return this.placeFurnaceStep();
      }

      if (AiGameState.countInventoryItem(this.outputId) >= this.target) {
         return this.finish(
            AiOperation.Status.SUCCEEDED, "Smelted " + this.outputId + " using " + this.furnaces.size() + " furnaces"
         );
      }

      if (this.batches.isEmpty()) {
         this.batches = batchSizes(this.target - AiGameState.countInventoryItem(this.outputId), this.furnaces.size());
         this.furnaceIndex = 0;
         this.collecting = false;
         this.collected.clear();

         for (int ignored : this.batches) {
            this.collected.add(0);
         }
      }

      if (this.furnaceIndex >= this.batches.size()) {
         if (this.collecting) {
            this.batches = List.of();
            return this.tick();
         }

         this.collecting = true;
         this.furnaceIndex = 0;
      }

      int amount = this.batches.get(this.furnaceIndex) - (this.collecting ? this.collected.get(this.furnaceIndex) : 0);
      if (amount == 0) {
         this.furnaceIndex++;
         this.collectTarget = 0;
         return this.tick();
      }

      BlockPos station = this.furnaces.get(this.furnaceIndex);
      if (this.collecting && this.collectTarget == 0) {
         this.collectTarget = Math.min(this.target, AiGameState.countInventoryItem(this.outputId) + amount);
      }

      Item availableFuel = this.collecting ? this.recipe.fuel() : AiSmelt.init("", this.recipe.input(), amount);
      String fuel = BuiltInRegistries.ITEM.getKey(availableFuel == null ? this.recipe.fuel() : availableFuel).toString();
      if (!this.keepCollectBaseline) {
         this.outputBaseline = AiGameState.countInventoryItem(this.outputId);
      }

      this.keepCollectBaseline = false;
      this.child = AiSmelt.load(
         AiJson.objectOf(
            "item",
            this.outputId,
            "input",
            this.inputId,
            "fuel",
            fuel,
            "count",
            this.collecting ? this.collectTarget : AiGameState.countInventoryItem(this.outputId) + amount,
            "batchSize",
            amount,
            "x",
            station.getX(),
            "y",
            station.getY(),
            "z",
            station.getZ(),
            "station",
            this.recipe.stationId(),
            "collect",
            this.collecting,
            "loadOnly",
            !this.collecting,
            "singleFurnace",
            true,
            "timeoutSeconds",
            this.secondsLeft()
         )
      );
      this.activity = (this.collecting ? "Wait and collect at furnace " : "Load furnace ") + (this.furnaceIndex + 1) + "/" + this.batches.size();
      return this.running(this.activity);
   }

   private AiOperation.Result placeFurnaceStep() {
      if (this.stationPlacer != null) {
         AiStationPlacer.Inner_DataUtil state = this.stationPlacer.tick();
         if (state == AiStationPlacer.Inner_DataUtil.PLACED) {
            this.furnaces.add(this.stationPlacer.getPlaced());
            this.stationPlacer = null;
            this.builtFurnaces++;
         } else {
            if (state != AiStationPlacer.Inner_DataUtil.FAILED) {
               return this.running(this.stationPlacer.getStatus());
            }

            String why = this.stationPlacer.getFailureReason();
            this.stationPlacer.cancel();
            this.stationPlacer = null;
            if (this.furnaces.isEmpty()) {
               return this.finish(AiOperation.Status.FAILED, "Cannot place furnace: " + why);
            }

            this.furnaceSetupDone = true;
            this.furnaceGoal = this.furnaces.size();
         }
      }

      if (this.furnaces.size() >= this.furnaceGoal) {
         this.furnacesReady = true;
         return this.tick();
      }

      if (AiGameState.countInventoryItem(this.recipe.stationId()) > 0) {
         this.stationPlacer = new AiStationPlacer(
            this.recipe.stationId().equals("minecraft:furnace")
               ? Items.FURNACE
               : BuiltInRegistries.ITEM.getValue(Identifier.parse(this.recipe.stationId())),
            "smelting station"
         );
         return this.running("Place a carried smelting station");
      }

      int missing = this.target - AiGameState.countInventoryItem(this.outputId);
      int neededCobble = 8 + (this.inputId.equals("minecraft:cobblestone") ? missing : 0);
      int deficit = Math.max(0, neededCobble - AiCraft.countItem("minecraft:cobblestone"));
      boolean tableEasy = AiCraft.isCraftingTableInReach() || AiCraft.countItem("minecraft:crafting_table") > 0 || AiCraftPlanner.init(AiCraft.buildRecipeGraph(), "minecraft:crafting_table", 1).complete();
      double stoneDistance = deficit == 0 ? 0.0 : AiTerrain.nearestBlockDistSqr(this.mc, List.of(Blocks.STONE, Blocks.COBBLESTONE), 8);
      boolean localStone = deficit == 0 || stoneDistance <= 64.0 && AiItems.hasUsableToolFor(Blocks.STONE.defaultBlockState());
      int setupTicks = 100 + deficit * 40 + (tableEasy ? 0 : 200);
      boolean easy = this.recipe.stationId().equals("minecraft:furnace")
         && tableEasy
         && localStone
         && (this.furnaces.isEmpty() || !this.furnaceSetupDone && worthAnotherFurnace(missing, this.furnaces.size(), this.cookTicks, setupTicks));
      if (easy) {
         if (deficit > 0) {
            this.activity = "Mine nearby cobblestone for a faster smelt without spending the requested input";
            this.child = autismclient.ai.engine.AiBlockMine.create(
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
                  Math.min(30, this.secondsLeft())
               )
            );
         } else {
            this.activity = "Craft another furnace; its setup costs less than the cooking time saved";
            this.child = AiCraft.craft(
               AiJson.objectOf("item", this.recipe.stationId(), "count", 1, "timeoutSeconds", Math.min(30, this.secondsLeft()))
            );
         }

         return this.running(this.activity);
      } else if (this.furnaces.isEmpty()) {
         this.activity = "Obtain the first furnace and its prerequisites";
         this.child = AiObtain.init(
            AiJson.objectOf("item", this.recipe.stationId(), "count", 1, "staged", true, "timeoutSeconds", this.secondsLeft())
         );
         this.obtainingFirstFurnace = true;
         this.furnaceSetupDone = true;
         this.furnaceGoal = 1;
         return this.running(this.activity);
      } else {
         this.furnaceGoal = this.furnaces.size();
         this.furnacesReady = true;
         return this.tick();
      }
   }

   private int secondsLeft() {
      return (int)Math.max(1L, (this.deadline - System.nanoTime()) / 1000000000L);
   }

   private JsonObject progressJson() {
      return AiJson.objectOf(
         "item",
         this.outputId,
         "target",
         this.target,
         "held",
         AiGameState.countInventoryItem(this.outputId),
         "furnaceCount",
         this.furnaces.size(),
         "builtFurnaces",
         this.builtFurnaces,
         "reusedFurnaces",
         this.reusedFurnaces,
         "maximumFurnaces",
         8,
         "batches",
         this.batches,
         "collecting",
         this.collecting,
         "activity",
         this.activity,
         "receipts",
         this.receipts
      );
   }

   private AiOperation.Result running(String message) {
      AiVisualization.init(this, this.label, AiTaskStatus.init(message, "Preparing the furnaces"), AiGameState.countInventoryItem(this.outputId), this.target);
      return AiGameOperations.result(AiOperation.Status.RUNNING, message, this.progressJson());
   }

   private AiOperation.Result finish(AiOperation.Status status, String message) {
      JsonObject receipt = this.progressJson();
      this.cancel();
      return AiGameOperations.result(status, message, receipt);
   }

   @Override
   public boolean canInterruptNow() {
      return this.guard.canInterruptNow()
         && (this.child == null || this.child.canInterruptNow())
         && (this.stationPlacer == null || this.stationPlacer.canInterrupt());
   }

   @Override
   public boolean requiresReconciliationAfterEmergency() {
      return this.guard.requiresReconciliation()
         || this.child != null && this.child.requiresReconciliationAfterEmergency()
         || this.stationPlacer != null && this.stationPlacer.needsReconciliation();
   }

   @Override
   public void cancel() {
      this.cancelled = true;
      AiVisualization.clearOwner(this);
      this.guard.cancel();
      if (this.child != null) {
         this.child.cancel();
      }

      if (this.stationPlacer != null) {
         this.stationPlacer.cancel();
      }
   }
}

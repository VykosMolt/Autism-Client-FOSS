package autismclient.ai;

import autismclient.ai.engine.AiGoto;
import autismclient.api.ai.AiOperation;
import com.google.gson.JsonObject;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.attribute.EnvironmentAttributes;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

final class AiObsidian {
   private AiObsidian() {
   }

   static AiOperation init_AiOperation_I(int seconds) {
      return init(1, seconds);
   }

   static AiObsidian.Inner_StreamUtil init(int want, int water, int empty) {
      int target = Math.max(0, want - Math.max(0, water));
      int craft = Math.max(0, target - Math.max(0, empty));
      return new AiObsidian.Inner_StreamUtil(target, craft, craft * 3);
   }

   static boolean init(int want, int water, boolean childActive) {
      return !childActive && water >= want;
   }

   static AiOperation init(final int want, final int seconds) {
      return new AiOperation() {
         final Minecraft init_Minecraft = Minecraft.getInstance();
         final long init_J = System.nanoTime() + Math.max(10, seconds) * 1000000000L;
         AiOperation init_AiOperation;
         BlockPos init_BlockPos;
         List<BlockPos> init_List = List.of();
         int init_I;
         int load_I;
         int read_I;
         String init_Str = "";
         boolean init_Z;
         boolean load_Z;
         boolean read_Z;
         boolean write;
         final AiPreparationReceipt init_bb = new AiPreparationReceipt();
         final Set<BlockPos> init_Set = new HashSet<>();

         @Override
         public boolean canInterruptNow() {
            return !this.init_bb.init_Z(this.init_AiOperation) && (this.init_AiOperation == null || this.init_AiOperation.canInterruptNow());
         }

         @Override
         public boolean requiresReconciliationAfterEmergency() {
            return this.init_bb.init_Z(this.init_AiOperation);
         }

         @Override
         public AiOperation.Result tick() {
            if (!this.init_Z && this.init_Minecraft.player != null && this.init_Minecraft.level != null) {
               if (this.init_Minecraft.level.environmentAttributes().getDimensionValue(EnvironmentAttributes.WATER_EVAPORATES)) {
                  return this.init(AiOperation.Status.FAILED, "Water cannot be prepared in a dimension where it evaporates");
               }

               if (AiObsidian.init(want, AiCraft.countItem("minecraft:water_bucket"), this.init_AiOperation != null)) {
                  return this.init(AiOperation.Status.SUCCEEDED, want > 1 ? want + " water buckets ready" : "Water bucket ready");
               }

               if (System.nanoTime() > this.init_J) {
                  return this.init(AiOperation.Status.TIMED_OUT, "Water preparation deadline");
               }

               if (this.init_AiOperation != null) {
                  AiOperation.Result result = this.init_bb.init_AiOperation_Result(this.init_AiOperation);
                  if (!result.terminal()) {
                     return result;
                  }

                  this.init_bb.init_V(this.init_AiOperation);
                  this.init_AiOperation = null;
                  if (result.status() == AiOperation.Status.UNCERTAIN) {
                     return this.init(AiOperation.Status.UNCERTAIN, result.message());
                  }

                  if (result.status() != AiOperation.Status.SUCCEEDED) {
                     if (this.init_Str.equals("carry")) {
                        return this.init(result.status(), "Crafting table recovery: " + result.message());
                     }

                     if (this.init_BlockPos != null && this.init_Set.size() < 4) {
                        this.init_Set.add(this.init_BlockPos);
                        this.init_BlockPos = null;
                        this.init_I = 0;
                        return AiObsidian.init_AiOperation_Result_Str("Trying another reachable water source");
                     }

                     return this.init(result.status(), result.message());
                  }

                  if (this.init_Str.equals("fill")) {
                     this.read_I++;
                  }

                  if (this.init_Str.equals("approach")) {
                     this.init_BlockPos = AiObsidian.init(result, this.init_BlockPos);
                  }

                  if (!this.init_Str.equals("carry")) {
                     this.init_I++;
                  }

                  if (this.init_I >= 2) {
                     if (this.init_BlockPos != null && this.init_Minecraft.level.getFluidState(this.init_BlockPos).isSource()) {
                        this.init_I = 1;
                     } else {
                        if (this.init_BlockPos != null) {
                           this.init_Set.add(this.init_BlockPos);
                        }

                        this.init_BlockPos = null;
                        this.init_I = 0;
                     }
                  }
               }

               if (AiObsidian.init(want, AiCraft.countItem("minecraft:water_bucket"), false)) {
                  return this.init(AiOperation.Status.SUCCEEDED, want > 1 ? want + " water buckets ready" : "Water bucket ready");
               }

               AiObsidian.Inner_StreamUtil stock = AiObsidian.init(want, AiCraft.countItem("minecraft:water_bucket"), AiCraft.countItem("minecraft:bucket"));
               if (stock.craft() > 0) {
                  if (AiCraft.countItem("minecraft:water_bucket") > 0 && AiCraft.countItem("minecraft:iron_ingot") < stock.iron()) {
                     return this.init(
                        AiOperation.Status.FAILED,
                        "Need "
                           + want
                           + " filled water buckets; holding "
                           + AiCraft.countItem("minecraft:water_bucket")
                           + " with "
                           + stock.iron()
                           + " more iron ingots needed for empty buckets"
                     );
                  }

                  if (AiRunResources.isCampaignActive() && !this.read_Z && !AiCraft.isCraftingTableInReach() && AiCraft.countItem("minecraft:crafting_table") == 0) {
                     this.read_Z = true;
                     AiOperation carry = AiProgression.init(Math.min(90, AiObsidian.init_I_J(this.init_J)));
                     if (carry != null) {
                        this.init_AiOperation = carry;
                        this.init_Str = "carry";
                        return AiObsidian.init_AiOperation_Result_Str("Recovering the observed crafting table before making the buckets");
                     }
                  }

                  this.init_BlockPos = null;
                  this.init_I = -1;
                  this.load_I++;
                  this.init_Str = "craft";
                  this.init_AiOperation = AiObtain.init(
                     AiJson.objectOf("item", "minecraft:bucket", "count", stock.emptyTarget(), "timeoutSeconds", AiObsidian.init_I_J(this.init_J))
                  );
                  return AiObsidian.init_AiOperation_Result_Str("Preparing all empty water buckets before travelling");
               } else {
                  if (AiRunResources.isCampaignActive() && !this.write && AiCraft.countItem("minecraft:crafting_table") == 0) {
                     this.write = true;
                     AiOperation carry = AiProgression.init(Math.min(90, AiObsidian.init_I_J(this.init_J)));
                     if (carry != null) {
                        this.init_AiOperation = carry;
                        this.init_Str = "carry";
                        return AiObsidian.init_AiOperation_Result_Str("Carrying the crafting table onward after the bucket batch");
                     }
                  }

                  if (this.init_BlockPos == null) {
                     this.init_List = AiTerrain.findFluidSources(this.init_Minecraft, Blocks.WATER, 96, 72, true)
                        .stream()
                        .filter(p -> !this.init_Set.contains(p))
                        .filter(p -> this.init_Minecraft.level.getBlockState(p.above()).isAir())
                        .toList();
                     this.init_BlockPos = this.init_List.isEmpty() ? null : this.init_List.getFirst();
                     this.init_I = 0;
                     if (this.init_BlockPos == null && !this.load_Z) {
                        this.load_Z = true;
                        this.init_I = -1;
                        this.init_Str = "surface";
                        this.init_AiOperation = autismclient.ai.engine.AiSeek.init(AiJson.objectOf("timeoutSeconds", Math.min(150, AiObsidian.init_I_J(this.init_J))));
                        return AiObsidian.init_AiOperation_Result_Str("Up to open sky for water");
                     }

                     if (this.init_BlockPos == null) {
                        return this.init(AiOperation.Status.FAILED, "No exposed water source in loaded terrain for the bucket");
                     }
                  }

                  if (this.init_I == 0) {
                     this.init_Str = "approach";
                     this.init_AiOperation = AiGoto.init(this.init_List, Math.min(45, AiObsidian.init_I_J(this.init_J)));
                  } else {
                     this.init_Str = "fill";
                     this.init_AiOperation = AiBucket.init(
                        AiJson.objectOf(
                           "action",
                           "fill",
                           "fluid",
                           "water",
                           "x",
                           this.init_BlockPos.getX(),
                           "y",
                           this.init_BlockPos.getY(),
                           "z",
                           this.init_BlockPos.getZ(),
                           "timeoutSeconds",
                           8
                        )
                     );
                  }

                  return AiObsidian.init_AiOperation_Result_Str(this.init_I == 0 ? "Approaching the water source" : "Filling the water bucket");
               }
            } else {
               return this.init(AiOperation.Status.CANCELLED, "Water preparation cancelled");
            }
         }

         AiOperation.Result init(AiOperation.Status status, String message) {
            JsonObject data = AiJson.objectOf(
               "wantedWaterBuckets",
               want,
               "waterBuckets",
               AiCraft.countItem("minecraft:water_bucket"),
               "emptyBuckets",
               AiCraft.countItem("minecraft:bucket"),
               "bucketCraftAttempts",
               this.load_I,
               "acknowledgedFills",
               this.read_I,
               "carriedTable",
               AiCraft.countItem("minecraft:crafting_table") > 0,
               "lastChild",
               this.init_bb.init() == null ? new JsonObject() : AiRunResources.parseEvidenceJson(this.init_bb.init().dataJson())
            );
            AiOperation.Result result = AiGameOperations.result(status, message, data);
            result = this.init_bb.init(result, this.requiresReconciliationAfterEmergency());
            this.cancel();
            return result;
         }

         @Override
         public void cancel() {
            this.init_bb.init_V(this.init_AiOperation);
            this.init_Z = true;
         }
      };
   }

   private static BlockPos init(AiOperation.Result result, BlockPos fallback) {
      JsonObject data = AiJson.parseObject(result.dataJson());
      return data.has("fluidSourceX") && data.has("fluidSourceY") && data.has("fluidSourceZ")
         ? new BlockPos(data.get("fluidSourceX").getAsInt(), data.get("fluidSourceY").getAsInt(), data.get("fluidSourceZ").getAsInt())
         : fallback;
   }

   static AiOperation load(final int held, int seconds) {
      final AiOperation acquisition = read(held, seconds);
      return new AiOperation() {
         @Override
         public AiOperation.Result tick() {
            AiOperation.Result result = acquisition.tick();
            if (!result.terminal()) {
               AiVisualization.init(
                  this,
                  "Getting " + Items.OBSIDIAN.getDefaultInstance().getHoverName().getString(),
                  AiTaskStatus.init(result.message(), "Preparing obsidian"),
                  AiCraft.countItem("minecraft:obsidian"),
                  held
               );
            } else {
               AiVisualization.init_V(this);
            }

            return result;
         }

         @Override
         public boolean canInterruptNow() {
            return acquisition.canInterruptNow();
         }

         @Override
         public boolean requiresReconciliationAfterEmergency() {
            return acquisition.requiresReconciliationAfterEmergency();
         }

         @Override
         public void cancel() {
            AiVisualization.init_V(this);
            acquisition.cancel();
         }
      };
   }

   private static AiOperation read(final int held, final int seconds) {
      Minecraft mc = Minecraft.getInstance();
      if (AiCraft.countItem("minecraft:lava_bucket") > 0) {
         return write(held, seconds);
      } else {
         return AiTerrain.nearestBlockDistSqr(mc, List.of(Blocks.OBSIDIAN), 48, pos -> {
               for (Direction d : Direction.values()) {
                  if (!mc.level.getFluidState(pos.relative(d)).isEmpty()) {
                     return false;
                  }
               }

               return true;
            }) != 1.7976931348623157E308
            ? new AiOperation() {
               final long init_J = System.nanoTime() + Math.max(10, seconds) * 1000000000L;
               AiOperation init_AiOperation = autismclient.ai.engine.AiBlockMine.init_AiOperation(
                  AiJson.objectOf("block", "obsidian", "held", held, "timeoutSeconds", Math.min(120, seconds))
               );
               boolean init_Z;

               @Override
               public AiOperation.Result tick() {
                  AiOperation.Result result = this.init_AiOperation.tick();
                  if (result.terminal() && result.status() != AiOperation.Status.SUCCEEDED && result.status() != AiOperation.Status.CANCELLED && !this.init_Z) {
                     this.init_AiOperation.cancel();
                     this.init_Z = true;
                     this.init_AiOperation = AiObsidian.write(held, AiObsidian.init_I_J(this.init_J));
                     return AiObsidian.init_AiOperation_Result_Str("Casting obsidian after the existing sources proved inaccessible");
                  } else {
                     return result;
                  }
               }

               @Override
               public boolean canInterruptNow() {
                  return this.init_AiOperation.canInterruptNow();
               }

               @Override
               public boolean requiresReconciliationAfterEmergency() {
                  return this.init_AiOperation.requiresReconciliationAfterEmergency();
               }

               @Override
               public void cancel() {
                  this.init_AiOperation.cancel();
               }
            }
            : write(held, seconds);
      }
   }

   private static AiOperation write(final int held, final int seconds) {
      return new AiOperation() {
         final Minecraft init_Minecraft = Minecraft.getInstance();
         final long init_J = System.nanoTime() + Math.max(10, seconds) * 1000000000L;
         final autismclient.ai.engine.AiPlacer init_DataPipe = new autismclient.ai.engine.AiPlacer();
         final Set<BlockPos> init_Set = new HashSet<>();
         final List<BlockPos> init_List = new ArrayList<>();
         final Set<BlockPos> load_Set = new HashSet<>();
         AiObsidian.Inner_DataUtil init_aD_DataUtil = AiObsidian.Inner_DataUtil.WATER;
         AiOperation init_AiOperation;
         BlockPos init_BlockPos;
         BlockPos load_BlockPos;
         BlockPos read_BlockPos;
         int init_I;
         int load_I;
         int read_I;
         int write_I;
         final int update_I = AiCraft.countItem("minecraft:obsidian");
         int sync;
         boolean init_Z;
         boolean load_Z;
         boolean read_Z;
         boolean write_Z;
         boolean update_Z = true;

         @Override
         public boolean canInterruptNow() {
            return !this.load_Z && this.init_aD_DataUtil != AiObsidian.Inner_DataUtil.VERIFY && (this.init_AiOperation == null || this.init_AiOperation.canInterruptNow());
         }

         @Override
         public boolean requiresReconciliationAfterEmergency() {
            return this.init_DataPipe.load_Z()
               || this.init_aD_DataUtil == AiObsidian.Inner_DataUtil.VERIFY
               || this.init_AiOperation != null && this.init_AiOperation.requiresReconciliationAfterEmergency();
         }

         @Override
         public AiOperation.Result tick() {
            if (!this.init_Z && this.init_Minecraft.player != null && this.init_Minecraft.level != null) {
               if (System.nanoTime() > this.init_J) {
                  return this.init(AiOperation.Status.TIMED_OUT, "Obsidian casting deadline");
               }

               if (this.init_Minecraft.level.environmentAttributes().getDimensionValue(EnvironmentAttributes.WATER_EVAPORATES)) {
                  return this.init(AiOperation.Status.FAILED, "Water evaporates in this dimension; refusing to cast obsidian");
               }

               if (this.init_AiOperation != null) {
                  AiOperation.Result result = this.init_AiOperation.tick();
                  if (!result.terminal()) {
                     return AiObsidian.init_AiOperation_Result_Str(this.init_aD_DataUtil + ": " + result.message());
                  }

                  this.init_AiOperation.cancel();
                  this.init_AiOperation = null;
                  this.init_V_19();
                  if (result.status() != AiOperation.Status.SUCCEEDED) {
                     if (this.init_aD_DataUtil == AiObsidian.Inner_DataUtil.GO_SOURCE
                        || this.init_aD_DataUtil == AiObsidian.Inner_DataUtil.REFILL
                        || this.init_aD_DataUtil == AiObsidian.Inner_DataUtil.FILL_LAVA) {
                        this.init_Set.add(this.init_BlockPos);
                        if (this.init_Set.size() < 6) {
                           this.init_aD_DataUtil = AiObsidian.Inner_DataUtil.SOURCE;
                           return AiObsidian.init_AiOperation_Result_Str("Trying another reachable lava source");
                        }
                     }

                     return this.init(result.status(), result.message());
                  }

                  if (this.init_aD_DataUtil == AiObsidian.Inner_DataUtil.GO_SOURCE || this.init_aD_DataUtil == AiObsidian.Inner_DataUtil.REFILL) {
                     this.init_BlockPos = AiObsidian.init(result, this.init_BlockPos);
                  }

                  AiObsidian.Inner_DataUtil var10001;
                  switch (this.init_aD_DataUtil) {
                     case WATER:
                        var10001 = AiObsidian.Inner_DataUtil.SOURCE;
                        break;
                     case SOURCE:
                     case SITE:
                     case EXCAVATE:
                     case WALLS:
                     case VERIFY:
                     default:
                        var10001 = this.init_aD_DataUtil;
                        break;
                     case GO_SOURCE:
                        if (this.load_BlockPos == null) {
                           var10001 = AiObsidian.Inner_DataUtil.SITE;
                        } else {
                           if (!this.load_Z_11() && !this.init_Z_17()) {
                              var10001 = AiObsidian.Inner_DataUtil.WORK;
                              break;
                           }

                           var10001 = AiObsidian.Inner_DataUtil.FILL_LAVA;
                        }
                        break;
                     case REFILL:
                        var10001 = AiObsidian.Inner_DataUtil.FILL_LAVA;
                        break;
                     case FILL_LAVA:
                        var10001 = AiObsidian.Inner_DataUtil.RETURN;
                        break;
                     case RETURN:
                        var10001 = AiObsidian.Inner_DataUtil.POUR_LAVA;
                        break;
                     case WALL_APPROACH:
                        var10001 = AiObsidian.Inner_DataUtil.WALLS;
                        break;
                     case WORK:
                        var10001 = this.init_I < AiObsidian.init_List_BlockPos(this.load_BlockPos).size() ? AiObsidian.Inner_DataUtil.WALLS : AiObsidian.Inner_DataUtil.POUR_WATER;
                        break;
                     case POUR_WATER:
                        var10001 = AiObsidian.Inner_DataUtil.REFILL;
                        break;
                     case POUR_LAVA:
                        var10001 = AiObsidian.Inner_DataUtil.VERIFY;
                        break;
                     case SCOOP:
                        var10001 = AiObsidian.Inner_DataUtil.CLEAN;
                        break;
                     case BREAK:
                        if (this.update_I + this.read_I < held && this.init_Z_17()) {
                           var10001 = AiObsidian.Inner_DataUtil.SOURCE;
                           break;
                        }

                        var10001 = AiObsidian.Inner_DataUtil.PICKUP;
                        break;
                     case PICKUP:
                        var10001 = AiCraft.countItem("minecraft:obsidian") >= held
                           ? (this.init_Z_17() ? AiObsidian.Inner_DataUtil.SCOOP : AiObsidian.Inner_DataUtil.CLEAN)
                           : (this.init_Z_17() ? AiObsidian.Inner_DataUtil.SOURCE : AiObsidian.Inner_DataUtil.WATER);
                        break;
                     case CLEAN:
                        var10001 = AiObsidian.Inner_DataUtil.CLEAN;
                  }

                  this.init_aD_DataUtil = var10001;
                  this.load_I = 0;
               }

               switch (this.init_aD_DataUtil) {
                  case WATER:
                     this.init_AiOperation = AiObsidian.init_AiOperation_I(AiObsidian.init_I_J(this.init_J));
                     break;
                  case SOURCE:
                     List<BlockPos> exposed = AiTerrain.findFluidSources(this.init_Minecraft, Blocks.LAVA, 96, 72)
                        .stream()
                        .filter(p -> !this.init_Set.contains(p))
                        .filter(p -> this.init_Minecraft.level.getBlockState(p.above()).isAir())
                        .toList();
                     this.init_BlockPos = this.read_BlockPos == null
                        ? null
                        : exposed.stream()
                           .filter(this::read)
                           .min(Comparator.comparingDouble(p -> this.init_Vec3_7().distanceToSqr(Vec3.atCenterOf(p))))
                           .orElse(null);
                     if (this.init_BlockPos == null) {
                        this.init_BlockPos = exposed.isEmpty() ? null : exposed.getFirst();
                     }

                     if (this.init_BlockPos == null) {
                        return this.init(AiOperation.Status.FAILED, "No exposed lava source in loaded terrain for contained obsidian casting");
                     }

                     this.init_aD_DataUtil = AiObsidian.Inner_DataUtil.GO_SOURCE;
                     this.init_AiOperation = AiGoto.init_AiOperation_BlockPos_3(
                        this.init_BlockPos, Math.min(60, AiObsidian.init_I_J(this.init_J)), this.init_Set_5(), true, this.load_Set_3()
                     );
                     break;
                  case GO_SOURCE:
                     this.init_AiOperation = AiGoto.init_AiOperation_BlockPos_3(
                        this.init_BlockPos, Math.min(60, AiObsidian.init_I_J(this.init_J)), this.init_Set_5(), true, this.load_Set_3()
                     );
                     break;
                  case REFILL:
                     this.init_AiOperation = AiGoto.init_AiOperation_BlockPos_3(
                        this.init_BlockPos, Math.min(60, AiObsidian.init_I_J(this.init_J)), this.init_Set_5(), true, this.load_Set_3()
                     );
                     break;
                  case FILL_LAVA:
                     this.init_AiOperation = this.init("fill", "lava", this.init_BlockPos);
                     break;
                  case RETURN:
                     this.init_AiOperation = AiGoto.init_AiOperation_BlockPos(this.read_BlockPos, 0.25, 30, this.init_Set_5(), this.load_Set_3());
                     break;
                  case SITE:
                     if (!this.init_Minecraft.player.onGround()) {
                        if (++this.load_I > 40) {
                           return this.init(AiOperation.Status.FAILED, "Lava-shore stance did not settle");
                        }
                     } else {
                        BlockPos standing = this.init_Minecraft.player.blockPosition();
                        double nearest = 1.7976931348623157E308;

                        for (BlockPos candidate : BlockPos.betweenClosed(standing.offset(-4, -1, -4), standing.offset(4, 0, 4))) {
                           double distance = this.init_Minecraft.player.position().distanceToSqr(Vec3.atBottomCenterOf(candidate));
                           if (distance < nearest && this.init(candidate, false)) {
                              this.load_BlockPos = candidate.immutable();
                              nearest = distance;
                           }
                        }

                        if (this.load_BlockPos == null) {
                           for (BlockPos candidate : BlockPos.betweenClosed(standing.offset(-4, -1, -4), standing.offset(4, -1, 4))) {
                              double distance = this.init_Minecraft.player.position().distanceToSqr(Vec3.atBottomCenterOf(candidate));
                              if (distance < nearest && this.init(candidate, true)) {
                                 this.load_BlockPos = candidate.immutable();
                                 nearest = distance;
                              }
                           }

                           if (this.load_BlockPos == null) {
                              this.init_Set.add(this.init_BlockPos);
                              if (this.init_Set.size() >= 6) {
                                 return this.init(AiOperation.Status.FAILED, "No solid-bottomed casting pocket reachable from the checked lava shores");
                              }

                              this.init_aD_DataUtil = AiObsidian.Inner_DataUtil.SOURCE;
                              break;
                           }

                           this.init_aD_DataUtil = AiObsidian.Inner_DataUtil.EXCAVATE;
                        } else {
                           this.init_aD_DataUtil = AiObsidian.Inner_DataUtil.WALLS;
                        }

                        this.read_BlockPos = this.load_BlockPos.south().above();
                     }
                     break;
                  case EXCAVATE:
                     List<BlockPos> cells = AiObsidian.load(this.load_BlockPos);
                     if (this.sync == cells.size()) {
                        this.init_aD_DataUtil = AiObsidian.Inner_DataUtil.WALLS;
                     } else {
                        BlockPos cell = cells.get(this.sync);
                        if (this.init_Minecraft.level.getBlockState(cell).isAir()) {
                           this.sync++;
                        } else {
                           if (!this.load(cell) || !this.init(this.load_BlockPos.below()) || !this.init(this.load_BlockPos.north().below())) {
                              return this.init(AiOperation.Status.FAILED, "Casting pocket terrain changed before excavation");
                           }

                           HashSet<Long> protect = new HashSet<>();

                           for (BlockPos wallCell : AiObsidian.init_List_BlockPos(this.load_BlockPos)) {
                              protect.add(wallCell.asLong());
                           }

                           protect.add(this.load_BlockPos.below().asLong());
                           protect.add(this.load_BlockPos.north().below().asLong());
                           this.init_AiOperation = autismclient.ai.engine.AiBreakBlock.init(cell, 10, false, protect, false);
                        }
                     }
                  case WALL_APPROACH:
                  default:
                     break;
                  case WALLS:
                     if (this.init_I > 0 && !this.write_Z) {
                        this.write_Z = true;
                        this.init_aD_DataUtil = AiObsidian.Inner_DataUtil.WORK;
                     } else {
                        List<BlockPos> pollCells = AiObsidian.init_List_BlockPos(this.load_BlockPos);
                        if (this.init_I == pollCells.size()) {
                           this.init_aD_DataUtil = AiObsidian.Inner_DataUtil.WORK;
                        } else {
                           BlockPos cell = pollCells.get(this.init_I);
                           if (!this.load_Z && this.init(cell)) {
                              this.init_I++;
                           } else {
                              if (!this.load_Z) {
                                 Item item = AiCraft.countItem("minecraft:cobblestone") > 0 ? Items.COBBLESTONE : Items.DIRT;
                                 if (!autismclient.ai.engine.AiPlacer.init_Z_BlockPos_Item(cell, item) && this.load_Set.add(cell)) {
                                    this.init_aD_DataUtil = AiObsidian.Inner_DataUtil.WALL_APPROACH;
                                    this.init_AiOperation = AiGoto.init(
                                       cell, item, Math.min(30, AiObsidian.init_I_J(this.init_J)), null, false, this.init_Set_5(), this.load_Set_3()
                                    );
                                    break;
                                 }

                                 this.init_DataPipe.init_V_BlockPos_Item(cell, item);
                                 this.load_Z = true;
                              }

                              switch (this.init_DataPipe.init_DataPipe_DataUtil()) {
                                 case WORKING:
                                    return AiObsidian.init_AiOperation_Result_Str("Sealing the casting mold");
                                 case FAILED:
                                    String why = this.init_DataPipe.init_Str();
                                    this.init_DataPipe.init_V();
                                    this.load_Z = false;
                                    if (this.read_I == 0 && this.init_BlockPos != null && this.init_Set.size() < 6) {
                                       this.init_Set.add(this.init_BlockPos);
                                       this.load_BlockPos = null;
                                       this.read_BlockPos = null;
                                       this.init_I = 0;
                                       this.write_Z = false;
                                       this.sync = 0;
                                       this.init_aD_DataUtil = AiObsidian.Inner_DataUtil.SOURCE;
                                       return AiObsidian.init_AiOperation_Result_Str("Mold wall would not seal (" + why + "); another lava shore");
                                    }

                                    return this.init(AiOperation.Status.FAILED, "Could not seal the casting mold: " + why);
                                 case PLACED:
                                    this.init_List.add(cell);
                                    this.init_I++;
                                    this.init_DataPipe.init_V();
                                    this.load_Z = false;
                              }
                           }
                        }
                     }
                     break;
                  case WORK:
                     this.init_AiOperation = AiGoto.init_AiOperation_BlockPos(this.read_BlockPos, 0.25, 30, this.init_Set_5(), this.load_Set_3());
                     break;
                  case POUR_WATER:
                     if (!this.write()) {
                        return this.init(AiOperation.Status.FAILED, "Casting walls or floor changed at " + this.load_BlockPos.toShortString());
                     }

                     if (!this.read()) {
                        if (++this.load_I > 40) {
                           return this.init(AiOperation.Status.FAILED, "Could not center safely on the casting wall at " + this.read_BlockPos.toShortString());
                        }
                     } else {
                        this.init_AiOperation = this.init("empty", "water", this.load_BlockPos.north());
                     }
                     break;
                  case POUR_LAVA:
                     if (!this.write()) {
                        return this.init(AiOperation.Status.FAILED, "Refusing a lava pour without a sealed mold at " + this.load_BlockPos.toShortString());
                     }

                     if (!this.load_Z_11()) {
                        FluidState reservoir = this.init_Minecraft.level.getFluidState(this.load_BlockPos.north());
                        if (reservoir.is(FluidTags.WATER)
                           && reservoir.isSource()
                           && this.init_Minecraft.level.getBlockState(this.load_BlockPos).isAir()
                           && ++this.load_I <= 20) {
                           return AiObsidian.init_AiOperation_Result_Str("Waiting for the placed water to reach the sealed lava cell");
                        }

                        return this.init(AiOperation.Status.FAILED, "Refusing a lava pour without active water at " + this.load_BlockPos.toShortString());
                     }

                     if (!this.read()) {
                        if (++this.load_I > 40) {
                           return this.init(AiOperation.Status.FAILED, "Working stance did not settle for a safe lava pour");
                        }
                     } else {
                        this.init_AiOperation = this.init("empty", "lava", this.load_BlockPos);
                     }
                     break;
                  case VERIFY:
                     if (this.init_Minecraft.level.getBlockState(this.load_BlockPos).is(Blocks.OBSIDIAN)) {
                        this.read_I++;
                        this.init_aD_DataUtil = AiObsidian.Inner_DataUtil.BREAK;
                     } else if (++this.load_I > 80) {
                        return this.init(AiOperation.Status.FAILED, "The contained lava did not become obsidian");
                     }
                     break;
                  case SCOOP:
                     this.init_AiOperation = this.init("fill", "water", this.load_BlockPos.north());
                     break;
                  case BREAK:
                     this.init_AiOperation = autismclient.ai.engine.AiBreakBlock.init(
                        this.load_BlockPos, 30, true, Set.of(), false, Set.of(this.load_BlockPos, this.load_BlockPos.north())
                     );
                     break;
                  case PICKUP:
                     this.init_AiOperation = autismclient.ai.engine.AiBlockMine.init(
                        AiJson.objectOf(
                           "block",
                           "obsidian",
                           "drop",
                           "minecraft:obsidian",
                           "dropsOnly",
                           true,
                           "held",
                           Math.min(held, AiCraft.countItem("minecraft:obsidian") + 1),
                           "timeoutSeconds",
                           12,
                           "radius",
                           12
                        ),
                        this.init_Set_5()
                     );
                     break;
                  case CLEAN:
                     if (this.init_List.isEmpty()) {
                        return this.init(AiOperation.Status.SUCCEEDED, "Obtained " + AiCraft.countItem("minecraft:obsidian") + " obsidian safely");
                     }

                     if (this.load_BlockPos != null
                        && (
                           !this.init_Minecraft.level.getFluidState(this.load_BlockPos).isEmpty()
                              || !this.init_Minecraft.level.getFluidState(this.load_BlockPos.north()).isEmpty()
                        )
                        && ++this.load_I <= 40) {
                        return AiObsidian.init_AiOperation_Result_Str("Letting the mold's water drain");
                     }

                     this.init_AiOperation = autismclient.ai.engine.AiBreakBlock.init(this.init_List.removeFirst(), 10, true, Set.of(), false);
               }

               if (this.init_AiOperation != null) {
                  this.init_AiOperation = AiRunDiagnostics.init_AiOperation_Str("obsidian", this.init_aD_DataUtil.name(), this.init_AiOperation);
               }

               if (this.init_AiOperation != null && this.write_I < 4) {
                  this.write_I++;

                  try {
                     return this.tick();
                  } finally {
                     this.write_I--;
                  }
               } else {
                  return AiObsidian.init_AiOperation_Result_Str("Obsidian casting: " + this.init_aD_DataUtil.name().toLowerCase(Locale.ROOT));
               }
            } else {
               return this.init(AiOperation.Status.CANCELLED, "Obsidian casting cancelled");
            }
         }

         boolean init(BlockPos pos) {
            if (!AiTerrain.isLoaded(this.init_Minecraft.level, pos)) {
               return false;
            }

            BlockState state = this.init_Minecraft.level.getBlockState(pos);
            return state.getFluidState().isEmpty()
               && state.isCollisionShapeFullBlock(this.init_Minecraft.level, pos)
               && !state.hasBlockEntity()
               && (
                  state.is(BlockTags.BASE_STONE_OVERWORLD)
                     || state.is(BlockTags.DIRT)
                     || state.is(Blocks.GRASS_BLOCK)
                     || state.is(Blocks.COBBLESTONE)
                     || state.is(Blocks.COBBLED_DEEPSLATE)
                     || state.is(Blocks.OBSIDIAN)
                     || state.is(Blocks.BLACKSTONE)
                     || state.is(Blocks.END_STONE)
                     || state.is(Blocks.SCULK)
               );
         }

         boolean init(BlockPos candidate, boolean excavate) {
            for (BlockPos cell : List.of(candidate, candidate.north())) {
               if (!AiTerrain.isLoaded(this.init_Minecraft.level, cell)
                  || !this.init(cell.below())
                  || !this.init_Minecraft.level.getBlockState(cell).isAir() && (!excavate || !this.init(cell))
                  || !this.init_Minecraft.level.getBlockState(cell.above()).isAir() && (!excavate || !this.init(cell.above()))
                  || this.init_Minecraft.player.getBoundingBox().inflate(0.35).intersects(new AABB(cell))) {
                  return false;
               }
            }

            for (BlockPos wall : AiObsidian.init_List_BlockPos(candidate)) {
               if (!this.init(wall) && (!this.init(wall.below()) || !this.init_Minecraft.level.getBlockState(wall).isAir())) {
                  return false;
               }
            }

            for (BlockPos cell : AiObsidian.load(candidate)) {
               if (!this.load(cell) || !excavate && !this.init_Minecraft.level.getBlockState(cell).isAir()) {
                  return false;
               }
            }

            return (this.init_Minecraft.level.getBlockState(candidate.south().above()).isAir() || excavate && this.init(candidate.south().above()))
               && (this.init_Minecraft.level.getBlockState(candidate.south().above(2)).isAir() || excavate && this.init(candidate.south().above(2)));
         }

         boolean load(BlockPos cell) {
            if (!AiTerrain.isLoaded(this.init_Minecraft.level, cell)) {
               return false;
            }

            for (Direction face : Direction.values()) {
               if (this.init_Minecraft.level.getFluidState(cell.relative(face)).is(FluidTags.LAVA)) {
                  return false;
               }
            }

            return this.init_Minecraft.level.getBlockState(cell).isAir() || this.init(cell);
         }

         Set<Long> init_Set_5() {
            if (this.load_BlockPos == null) {
               return Set.of();
            }

            HashSet<Long> keep = new HashSet<>();

            for (BlockPos wallCell : AiObsidian.init_List_BlockPos(this.load_BlockPos)) {
               keep.add(wallCell.asLong());
            }

            keep.add(this.load_BlockPos.below().asLong());
            keep.add(this.load_BlockPos.north().below().asLong());
            keep.add(this.load_BlockPos.asLong());
            keep.add(this.load_BlockPos.north().asLong());
            return keep;
         }

         Set<Long> load_Set_3() {
            if (this.load_BlockPos == null) {
               return Set.of();
            }

            HashSet<Long> keep = new HashSet<>();

            for (BlockPos pocket : List.of(this.load_BlockPos, this.load_BlockPos.north())) {
               keep.add(pocket.asLong());
               keep.add(pocket.above().asLong());
            }

            return keep;
         }

         boolean init_Z_17() {
            if (this.load_BlockPos == null) {
               return false;
            }

            FluidState supply = this.init_Minecraft.level.getFluidState(this.load_BlockPos.north());
            return supply.is(FluidTags.WATER) && supply.isSource();
         }

         Vec3 init_Vec3_7() {
            return Vec3.atBottomCenterOf(this.read_BlockPos).add(0.0, this.init_Minecraft.player.getEyeHeight(), 0.0);
         }

         boolean read(BlockPos p) {
            double reach = this.init_Minecraft.player.blockInteractionRange() - 0.35;
            return this.init_Vec3_7().distanceToSqr(Vec3.atCenterOf(p).add(0.0, 0.4, 0.0)) <= reach * reach;
         }

         boolean load_Z_11() {
            FluidState supply = this.init_Minecraft.level.getFluidState(this.load_BlockPos.north());
            return supply.is(FluidTags.WATER) && supply.isSource() && this.init_Minecraft.level.getFluidState(this.load_BlockPos).is(FluidTags.WATER);
         }

         boolean read() {
            this.read_Z = true;
            this.init_Minecraft.options.keyShift.setDown(false);
            this.init_Minecraft.options.keyJump.setDown(false);
            this.init_Minecraft.options.keySprint.setDown(false);
            this.init_Minecraft.options.keyUp.setDown(false);
            this.init_Minecraft.options.keyDown.setDown(false);
            this.init_Minecraft.options.keyLeft.setDown(false);
            this.init_Minecraft.options.keyRight.setDown(false);
            if (!this.init_Minecraft.player.onGround() || Math.abs(this.init_Minecraft.player.getY() - this.read_BlockPos.getY()) > 0.1) {
               this.update_Z = true;
               return false;
            }

            if (this.init_Minecraft.player.getPose() != Pose.STANDING) {
               return false;
            }

            Vec3 center = Vec3.atBottomCenterOf(this.read_BlockPos);
            double dx = center.x - this.init_Minecraft.player.getX();
            double dz = center.z - this.init_Minecraft.player.getZ();
            boolean bodyClear = !this.init_Minecraft.player.getBoundingBox().inflate(0.15).intersects(new AABB(this.load_BlockPos));
            double offset = Math.hypot(dx, dz);
            if (!bodyClear || offset > 0.3) {
               this.update_Z = true;
            } else if (offset <= 0.12) {
               this.update_Z = false;
            }

            if (this.update_Z) {
               double yaw = Math.toRadians(this.init_Minecraft.player.getYRot());
               double forward = dx * -Math.sin(yaw) + dz * Math.cos(yaw);
               double right = dx * -Math.cos(yaw) + dz * -Math.sin(yaw);
               double f = forward > 0.01 ? 1.0 : (forward < -0.01 ? -1.0 : 0.0);
               double strafe = right > 0.01 ? -1.0 : (right < -0.01 ? 1.0 : 0.0);
               if (autismclient.ai.engine.AiMotionSafety.isSegmentHazardFree(this.init_Minecraft.player.position(), center)
                  && autismclient.ai.engine.AiMotionSafety.isHorizontalMoveSafe(f, strafe)) {
                  this.init_Minecraft.options.keyUp.setDown(f > 0.0);
                  this.init_Minecraft.options.keyDown.setDown(f < 0.0);
                  this.init_Minecraft.options.keyRight.setDown(strafe < 0.0);
                  this.init_Minecraft.options.keyLeft.setDown(strafe > 0.0);
                  return false;
               } else {
                  return false;
               }
            } else {
               return this.init_Minecraft.player.getDeltaMovement().horizontalDistanceSqr() <= 4.0E-4 && bodyClear;
            }
         }

         void init_V_19() {
            if (this.read_Z) {
               this.read_Z = false;

               for (KeyMapping key : List.of(
                  this.init_Minecraft.options.keyUp,
                  this.init_Minecraft.options.keyDown,
                  this.init_Minecraft.options.keyLeft,
                  this.init_Minecraft.options.keyRight,
                  this.init_Minecraft.options.keyJump,
                  this.init_Minecraft.options.keySprint,
                  this.init_Minecraft.options.keyShift
               )) {
                  key.setDown(autismclient.util.KeyMappingControl.of(key).autism$physicalForBody());
               }
            }
         }

         boolean write() {
            if (this.init(this.load_BlockPos.below()) && this.init(this.load_BlockPos.north().below())) {
               for (BlockPos cell : AiObsidian.init_List_BlockPos(this.load_BlockPos)) {
                  if (!this.init(cell)) {
                     return false;
                  }
               }

               return true;
            } else {
               return false;
            }
         }

         AiOperation init(String action, String fluid, BlockPos pos) {
            return AiBucket.init(
               AiJson.objectOf("action", action, "fluid", fluid, "x", pos.getX(), "y", pos.getY(), "z", pos.getZ(), "timeoutSeconds", 8)
            );
         }

         AiOperation.Result init(AiOperation.Status status, String message) {
            this.cancel();
            return AiGameOperations.result(
               status,
               message,
               AiJson.objectOf(
                  "held",
                  AiCraft.countItem("minecraft:obsidian"),
                  "casts",
                  this.read_I,
                  "phase",
                  this.init_aD_DataUtil.name(),
                  "site",
                  this.load_BlockPos,
                  "work",
                  this.read_BlockPos,
                  "position",
                  this.init_Minecraft.player == null ? null : this.init_Minecraft.player.position(),
                  "stance",
                  this.init_Minecraft.player == null
                     ? null
                     : AiJson.objectOf(
                        "grounded",
                        this.init_Minecraft.player.onGround(),
                        "pose",
                        this.init_Minecraft.player.getPose().name(),
                        "velocity",
                        this.init_Minecraft.player.getDeltaMovement(),
                        "body",
                        this.init_Minecraft.player.getBoundingBox().toString(),
                        "water",
                        this.init_Minecraft.player.isInWater(),
                        "clear",
                        this.load_BlockPos == null || !this.init_Minecraft.player.getBoundingBox().inflate(0.15).intersects(new AABB(this.load_BlockPos))
                     )
               )
            );
         }

         @Override
         public void cancel() {
            this.init_Z = true;
            if (this.init_AiOperation != null) {
               this.init_AiOperation.cancel();
            }

            this.init_DataPipe.init_V();
            this.init_V_19();
         }
      };
   }

   static List<BlockPos> init_List_BlockPos(BlockPos site) {
      return List.of(site.south(), site.north(2), site.north().east(), site.north().west(), site.east(), site.west());
   }

   static List<BlockPos> load(BlockPos site) {
      BlockPos work = site.south().above();
      return List.of(
         site.north().above(3),
         site.above(3),
         work.above(2),
         site.north().above(2),
         site.above(2),
         site.north().above(),
         site.above(),
         work.above(),
         work,
         site,
         site.north()
      );
   }

   private static int init_I_J(long deadline) {
      return (int)Math.max(10L, (deadline - System.nanoTime()) / 1000000000L);
   }

   private static AiOperation.Result init_AiOperation_Result_Str(String message) {
      return AiGameOperations.result(AiOperation.Status.RUNNING, message, new JsonObject());
   }

   private enum Inner_DataUtil {
      WATER,
      SOURCE,
      GO_SOURCE,
      REFILL,
      FILL_LAVA,
      RETURN,
      SITE,
      EXCAVATE,
      WALL_APPROACH,
      WALLS,
      WORK,
      POUR_WATER,
      POUR_LAVA,
      VERIFY,
      SCOOP,
      BREAK,
      PICKUP,
      CLEAN;
   }

   record Inner_StreamUtil(int emptyTarget, int craft, int iron) {
   }
}

package autismclient.ai;

import autismclient.ai.a.HashUtil;
import autismclient.api.ai.AiOperation;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.BaseFireBlock;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

final class cp {
   private cp() {
   }

   private static boolean init(BlockState state) {
      return state.is(Blocks.STONE_BRICKS)
         || state.is(Blocks.MOSSY_STONE_BRICKS)
         || state.is(Blocks.CRACKED_STONE_BRICKS)
         || state.is(Blocks.INFESTED_STONE_BRICKS)
         || state.is(Blocks.INFESTED_MOSSY_STONE_BRICKS)
         || state.is(Blocks.INFESTED_CRACKED_STONE_BRICKS)
         || state.is(Blocks.STONE_BRICK_STAIRS);
   }

   private static long init(BlockPos at) {
      return new BlockPos(Math.floorDiv(at.getX(), 6), Math.floorDiv(at.getY(), 4), Math.floorDiv(at.getZ(), 6)).asLong();
   }

   static AiOperation init(Vec3 lead, int seconds) {
      return init(lead, seconds, new cp.Inner_DataUtil());
   }

   static AiOperation init(final Vec3 lead, final int seconds, final cp.Inner_DataUtil memory) {
      final Minecraft mc = Minecraft.getInstance();
      memory.init(mc.level, mc.getConnection(), lead);
      return new AiOperation() {
         final Object init_Obj = mc.level;
         final Object load_Obj = mc.getConnection();
         final long init_J = System.nanoTime() + Math.max(30, Math.min(360, seconds)) * 1000000000L;
         final Set<Long> init_Set;
         final Set<Long> load_Set;
         final JsonArray init_JsonArray;
         AiOperation init_AiOperation;
         BlockPos init_BlockPos;
         BlockPos load_BlockPos;
         int init_I;
         boolean init_Z;

         {
            this.init_Set = memory.init_Set;
            this.load_Set = memory.load_Set;
            this.init_JsonArray = memory.init_JsonArray;
         }

         @Override
         public AiOperation.Result tick() {
            if (this.init_Z || mc.player == null || mc.level != this.init_Obj || this.load_Obj != mc.getConnection()) {
               return this.init(AiOperation.Status.CANCELLED, "Local stronghold world changed");
            }

            if (!mc.player.isAlive()) {
               return this.init(AiOperation.Status.FAILED, "Player died in the stronghold search");
            }

            BlockPos frame = cO.init_BlockPos_Predicate_I(s -> s.is(Blocks.END_PORTAL_FRAME), 12);
            if (frame != null) {
               return this.init(AiOperation.Status.SUCCEEDED, "Portal-frame evidence loaded at " + frame.toShortString());
            }

            if (System.nanoTime() <= this.init_J && this.init_I < 24) {
               if (this.init_AiOperation != null) {
                  AiOperation.Result result = this.init_AiOperation.tick();
                  if (!result.terminal()) {
                     return R.init_AiOperation_Result(
                        AiOperation.Status.RUNNING, "Exploring supported structure terrain: " + result.message(), this.init_JsonObject_54()
                     );
                  }

                  boolean pending = this.init_AiOperation.requiresReconciliationAfterEmergency();
                  this.init_AiOperation.cancel();
                  this.init_AiOperation = null;
                  this.init_JsonArray
                     .add(
                        ag.init_JsonObject_ObjArr(
                           "from",
                           this.load_BlockPos.toShortString(),
                           "toward",
                           this.init_BlockPos.toShortString(),
                           "actualArrival",
                           mc.player.blockPosition().toShortString(),
                           "status",
                           result.status().name(),
                           "evidence",
                           bK.load_JsonObject(result.dataJson())
                        )
                     );
                  if (result.status() == AiOperation.Status.UNCERTAIN || pending) {
                     return this.init(AiOperation.Status.UNCERTAIN, "Structure route needs reconciliation: " + result.message());
                  }

                  if (result.status() == AiOperation.Status.CANCELLED) {
                     return this.init(AiOperation.Status.CANCELLED, "Structure approach cancelled");
                  }

                  if (result.status() != AiOperation.Status.SUCCEEDED) {
                     this.load_Set.add(cp.init(this.init_BlockPos));
                  } else if (!memory.init_Z) {
                     memory.init_Z = this.init_Z_8(mc.player.blockPosition());
                  }

                  this.init_Set.add(cp.init(this.init_BlockPos));
                  this.init_Set.add(cp.init(mc.player.blockPosition()));
               }

               BlockPos feet = mc.player.blockPosition();
               if (!memory.init_Z) {
                  this.init_BlockPos = this.init_BlockPos_16();
                  if (this.init_BlockPos == null) {
                     return this.init("No untried loaded masonry supports a standing approach at this Eye estimate");
                  }
               } else {
                  this.init_BlockPos = this.load_BlockPos_3(feet);
                  if (this.init_BlockPos == null) {
                     return this.init("Observed local corridors exhausted; unloaded rooms remain unknown");
                  }
               }

               this.load_BlockPos = feet;
               this.init_I++;
               this.init_AiOperation = HashUtil.init(
                  this.init_BlockPos,
                  1.2,
                  true,
                  true,
                  false,
                  Math.min(90, Math.max(15, (int)((this.init_J - System.nanoTime()) / 1000000000L))),
                  autismclient.ai.a.ClassCache.Inner_IoWorker.load,
                  true,
                  2.0,
                  null,
                  HashUtil.Inner_BufferHelper.DEFAULT
               );
               return R.init_AiOperation_Result(
                  AiOperation.Status.RUNNING, "Following observed stronghold frontier " + this.init_BlockPos.toShortString(), this.init_JsonObject_54()
               );
            } else {
               return this.init("Local search budget ended; portal room remains unobserved");
            }
         }

         BlockPos init_BlockPos_16() {
            for (BlockPos evidence : cO.init(cp::init, 8, 16, 256)) {
               if (!(Math.hypot(evidence.getX() - lead.x, evidence.getZ() - lead.z) > 240.0)) {
                  BlockPos candidate = this.init_BlockPos_7(evidence.above());
                  if (candidate != null && !this.init_Set.contains(cp.init(candidate)) && !this.load_Set.contains(cp.init(candidate))) {
                     return candidate;
                  }
               }
            }

            return null;
         }

         boolean init_Z_8(BlockPos feet) {
            for (BlockPos p : BlockPos.betweenClosed(feet.offset(-2, -2, -2), feet.offset(2, 1, 2))) {
               if (cu.init(mc.level, p) && cp.init(mc.level.getBlockState(p))) {
                  return true;
               }
            }

            return false;
         }

         BlockPos init_BlockPos_7(BlockPos center) {
            BlockPos best = null;
            double distance = 1.7976931348623157E308;

            for (BlockPos p : BlockPos.betweenClosed(center.offset(-4, -4, -4), center.offset(4, 4, 4))) {
               if (!this.init_Set.contains(cp.init(p)) && !this.load_Set.contains(cp.init(p)) && this.load_Z_6(p) && ValueSink.init_Z_Minecraft_BlockPos(mc, p)
                  )
                {
                  double d = p.distSqr(center);
                  if (d < distance) {
                     best = p.immutable();
                     distance = d;
                  }
               }
            }

            return best;
         }

         BlockPos load_BlockPos_3(BlockPos feet) {
            BlockPos best = null;
            double score = 1.7976931348623157E308;

            for (BlockPos p : BlockPos.betweenClosed(feet.offset(-24, -12, -24), feet.offset(24, 12, 24))) {
               long key = cp.init(p);
               if (!this.init_Set.contains(key)
                  && !this.load_Set.contains(key)
                  && !(p.distSqr(feet) < 16.0)
                  && this.load_Z_6(p)
                  && cp.init(mc.level.getBlockState(p.below()))
                  && ValueSink.init_Z_Minecraft_BlockPos(mc, p)) {
                  double value = Math.sqrt(p.distSqr(feet)) + Math.abs(p.getY() - feet.getY()) * 2;
                  if (value < score) {
                     best = p.immutable();
                     score = value;
                  }
               }
            }

            return best;
         }

         boolean load_Z_6(BlockPos at) {
            if (cu.init(mc.level, at) && cu.init(mc.level, at.above()) && cu.init(mc.level, at.below())) {
               for (BlockPos cell : List.of(at, at.above())) {
                  BlockState state = mc.level.getBlockState(cell);
                  if (!state.getCollisionShape(mc.level, cell).isEmpty() || !state.getFluidState().isEmpty() || state.getBlock() instanceof BaseFireBlock) {
                     return false;
                  }
               }

               return !mc.level.getBlockState(at.below()).getCollisionShape(mc.level, at.below()).isEmpty();
            } else {
               return false;
            }
         }

         JsonObject init_JsonObject_54() {
            return ag.init_JsonObject_ObjArr(
               "leadIsEstimate",
               true,
               "leadX",
               lead.x,
               "leadZ",
               lead.z,
               "visitedRegions",
               this.init_Set.size(),
               "knownDeadEnds",
               this.load_Set.size(),
               "routeGraph",
               this.init_JsonArray,
               "steps",
               this.init_I
            );
         }

         AiOperation.Result init(String message) {
            AiOperation.Result result = cp.init(message, this.init_JsonObject_54(), this.requiresReconciliationAfterEmergency());
            this.cancel();
            return result;
         }

         AiOperation.Result init(AiOperation.Status status, String message) {
            AiOperation.Result result = Y.init_AiOperation_Result_Z_AiOperation_Result(
               this.requiresReconciliationAfterEmergency(), R.init_AiOperation_Result(status, message, this.init_JsonObject_54())
            );
            this.cancel();
            return result;
         }

         @Override
         public boolean canInterruptNow() {
            return this.init_AiOperation == null || this.init_AiOperation.canInterruptNow();
         }

         @Override
         public boolean requiresReconciliationAfterEmergency() {
            return this.init_AiOperation != null && this.init_AiOperation.requiresReconciliationAfterEmergency();
         }

         @Override
         public void cancel() {
            this.init_Z = true;
            if (this.init_AiOperation != null) {
               this.init_AiOperation.cancel();
               this.init_AiOperation = null;
            }
         }
      };
   }

   static AiOperation.Result init(String message, JsonObject data, boolean pending) {
      return Y.init_AiOperation_Result_Z_AiOperation_Result(pending, AiOperation.Result.failed(A.init_A, message, data.toString()));
   }

   static final class Inner_DataUtil {
      private Object init_Obj;
      private Object load_Obj;
      private Vec3 init_Vec3;
      final Set<Long> init_Set = new HashSet<>();
      final Set<Long> load_Set = new HashSet<>();
      final JsonArray init_JsonArray = new JsonArray();
      boolean init_Z;

      void init(Object world, Object session, Vec3 nextLead) {
         if (this.init_Obj != world
            || this.load_Obj != session
            || this.init_Vec3 != null && Math.hypot(this.init_Vec3.x - nextLead.x, this.init_Vec3.z - nextLead.z) > 240.0) {
            this.init_Set.clear();
            this.load_Set.clear();

            while (!this.init_JsonArray.isEmpty()) {
               this.init_JsonArray.remove(this.init_JsonArray.size() - 1);
            }

            this.init_Z = false;
         }

         this.init_Obj = world;
         this.load_Obj = session;
         this.init_Vec3 = nextLead;
      }
   }
}

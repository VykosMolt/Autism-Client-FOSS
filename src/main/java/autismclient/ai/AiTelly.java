package autismclient.ai;

import autismclient.api.ai.AiOperation;
import com.google.gson.JsonObject;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.FallingBlock;
import net.minecraft.world.phys.Vec3;

final class AiTelly {
   static int init_I_I_List(int held, List<AiTelly.HotbarEntry> hotbar) {
      if (held >= 0 && held < hotbar.size()) {
         AiTelly.HotbarEntry entry = hotbar.get(held);
         if (entry.construction() && entry.count() > 0) {
            return held;
         }
      }

      for (int i = 0; i < hotbar.size(); i++) {
         AiTelly.HotbarEntry entry = hotbar.get(i);
         if (entry.construction() && entry.count() > 0) {
            return i;
         }
      }

      return -1;
   }

   static boolean init_Z_D_D(double playerY, double walkSurfaceY) {
      return playerY < walkSurfaceY - 2.0;
   }

   static List<AiTelly.HotbarEntry> init_List(Minecraft mc) {
      List<AiTelly.HotbarEntry> view = new ArrayList<>(9);

      for (int i = 0; i < 9; i++) {
         ItemStack stack = mc.player.getInventory().getItem(i);
         boolean construction = stack.getItem() instanceof BlockItem block
            && !(block.getBlock() instanceof FallingBlock)
            && block.getBlock().defaultBlockState().isCollisionShapeFullBlock(mc.level, mc.player.blockPosition())
            && !block.getBlock().defaultBlockState().hasBlockEntity()
            && autismclient.ai.engine.AiRestock.isConsumableBuildingBlock(stack, mc.player.getInventory());
         view.add(new AiTelly.HotbarEntry(construction, construction ? stack.getCount() : 0));
      }

      return view;
   }

   static int init_I(Minecraft mc) {
      int total = 0;

      for (AiTelly.HotbarEntry entry : init_List(mc)) {
         total += entry.count();
      }

      return total;
   }

   static boolean init_Z_Minecraft_BlockPos(Minecraft mc, BlockPos support) {
      for (int i = 1; i <= 3; i++) {
         if (mc.level.getBlockState(support.below(i)).is(Blocks.LAVA)) {
            return true;
         }
      }

      return false;
   }

   static AiOperation create(JsonObject args) {
      final AiTelly.TellyPlan plan = AiTelly.TellyPlan.parse(args);
      final Minecraft mc = Minecraft.getInstance();
      if (mc.player != null && mc.level != null) {
         return new AiOperation() {
            static final int init_I = 100;
            static final int load_I = 20;
            final BlockPos init_BlockPos = mc.player.blockPosition();
            final long init_J = System.nanoTime() + plan.timeoutSeconds() * 1000000000L;
            final int read = mc.player.getInventory().getSelectedSlot();
            final autismclient.ai.engine.AiAim init_DataUtil = new autismclient.ai.engine.AiAim();
            final List<BlockPos> init_List = new ArrayList<>();
            autismclient.ai.engine.AiBridgeDriver init_NetHelper;
            int write = -1;
            int update = -1;
            int sync;
            double init_D = 0.0 / 0.0;
            double load_D;
            boolean init_Z;

            autismclient.ai.engine.AiBridgeDriver init_NetHelper() {
               BlockPos feet = mc.player.blockPosition();
               int dx;
               int dz;
               int length;
               if (plan.targetX() != null) {
                  int ax = plan.targetX() - feet.getX();
                  int az = plan.targetZ() - feet.getZ();
                  if (ax == 0 && az == 0) {
                     return null;
                  }

                  if (Math.abs(ax) >= Math.abs(az)) {
                     dx = Integer.signum(ax);
                     dz = 0;
                     length = Math.abs(ax);
                  } else {
                     dx = 0;
                     dz = Integer.signum(az);
                     length = Math.abs(az);
                  }
               } else {
                  int[] d = autismclient.ai.engine.AiBridgeDriver.cardinalStep(plan.yawDegrees());
                  dx = d[0];
                  dz = d[1];
                  length = plan.blocks() - this.init_List.size();
               }

               length = Math.min(length, plan.blocks() - this.init_List.size());
               if (length <= 0) {
                  return null;
               }

               autismclient.ai.engine.AiBridger.Inner_CachePool lane = new autismclient.ai.engine.AiBridger.Inner_CachePool(
                  feet.getX(), feet.getZ(), feet.getY() - 1, dx, dz, length
               );
               return new autismclient.ai.engine.AiBridgeDriver(
                  lane,
                  autismclient.ai.engine.AiBridgeDriver.chooseStyle(plan.mode() == AiTelly.BridgeMode.TELLY, lane, mc.player, AiTelly.init_I(mc)),
                  this.init_DataUtil
               );
            }

            void init(int slot) {
               if (slot != mc.player.getInventory().getSelectedSlot()) {
                  mc.player.getInventory().setSelectedSlot(slot);
                  this.write = slot;
               }
            }

            @Override
            public AiOperation.Result tick() {
               if (!this.init_Z && mc.player != null && mc.level != null) {
                  if (mc.player.getHealth() <= 4.0F && !AiGuard.isLowHealthOverrideActive()) {
                     return this.init(AiOperation.Status.FAILED, AiGuard.formatLowHealthAbort("bridging", mc.player.getHealth(), mc.player.getMaxHealth()));
                  }

                  if (System.nanoTime() > this.init_J) {
                     return this.init(AiOperation.Status.TIMED_OUT, "Telly bridge deadline reached");
                  }

                  if (!AiTerrain.isLoaded(mc.level, mc.player.blockPosition())) {
                     autismclient.ai.engine.AiMotionSafety.releaseMovementKeys();
                     return AiGameOperations.result(
                        AiOperation.Status.RUNNING, "Waiting for current terrain to load", AiJson.objectOf("placed", this.init_List.size())
                     );
                  }

                  if (this.update >= 0) {
                     autismclient.ai.engine.AiMotionSafety.releaseMovementKeys();
                     if (++this.update < 20) {
                        return AiGameOperations.result(
                           AiOperation.Status.RUNNING, "Verifying the supports", AiJson.objectOf("placed", this.init_List.size())
                        );
                     }

                     long gone = this.init_List.stream().filter(c -> mc.level.getBlockState(c).getCollisionShape(mc.level, c).isEmpty()).count();
                     return gone == 0L
                        ? this.init_AiOperation_Result_36()
                        : this.init(AiOperation.Status.UNCERTAIN, gone + " placed supports are gone again (the server refused them)");
                  } else {
                     if (this.init_NetHelper == null) {
                        this.init_NetHelper = this.init_NetHelper();
                        if (this.init_NetHelper == null) {
                           if (plan.verify()) {
                              this.update = 0;
                              return AiGameOperations.result(
                                 AiOperation.Status.RUNNING, "Verifying the supports", AiJson.objectOf("placed", this.init_List.size())
                              );
                           }

                           return this.init_AiOperation_Result_36();
                        }
                     }

                     BlockPos support = this.init_NetHelper.nextSupport();
                     if (support != null && AiTelly.init_Z_Minecraft_BlockPos(mc, support)) {
                        return this.init(AiOperation.Status.FAILED, "Refusing to bridge over lava at " + support.toShortString());
                     }

                     int slot = AiTelly.init_I_I_List(mc.player.getInventory().getSelectedSlot(), AiTelly.init_List(mc));
                     if (slot < 0 && support != null) {
                        autismclient.ai.engine.AiMotionSafety.releaseMovementKeys();
                        mc.options.keyUp.setDown(false);
                        mc.options.keyDown.setDown(false);
                        mc.options.keyLeft.setDown(false);
                        mc.options.keyRight.setDown(false);
                        mc.options.keyJump.setDown(false);
                        mc.options.keyShift.setDown(true);
                        Vec3 v = mc.player.getDeltaMovement();
                        return mc.player.onGround() && v.x * v.x + v.z * v.z < 1.0E-4
                           ? this.init(AiOperation.Status.FAILED, "Out of blocks")
                           : AiGameOperations.result(
                              AiOperation.Status.RUNNING, "Out of blocks, stopping on the bridge", AiJson.objectOf("placed", this.init_List.size())
                           );
                     }

                     autismclient.ai.engine.AiBridgeDriver.BridgeStatus step = this.init_NetHelper.init(slot, this::init, this.init_List::add);
                     if (step == autismclient.ai.engine.AiBridgeDriver.BridgeStatus.INVALID_FOOTING) {
                        return this.init(AiOperation.Status.FAILED, "Bridge needs footing at the support's top level");
                     }

                     if (step != autismclient.ai.engine.AiBridgeDriver.BridgeStatus.FELL
                        && !AiTelly.init_Z_D_D(mc.player.getY(), this.init_NetHelper.getLane().floorY() + 1)) {
                        String style = this.init_NetHelper.getBridger().getStyle().name().toLowerCase(Locale.ROOT);
                        if (step == autismclient.ai.engine.AiBridgeDriver.BridgeStatus.DONE) {
                           this.init_NetHelper = null;
                           return AiGameOperations.result(
                              AiOperation.Status.RUNNING, "Lane done", AiJson.objectOf("placed", this.init_List.size(), "mode", style)
                           );
                        }

                        Vec3 pos = mc.player.position();
                        if (Double.compare(this.init_D, pos.x) == 0 && Double.compare(this.load_D, pos.z) == 0) {
                           if (++this.sync > 100) {
                              return this.init(AiOperation.Status.FAILED, "Wedged against terrain; no progress");
                           }
                        } else {
                           this.sync = 0;
                        }

                        this.init_D = pos.x;
                        this.load_D = pos.z;
                        return AiGameOperations.result(
                           AiOperation.Status.RUNNING,
                           "Bridging (" + style + "), " + this.init_List.size() + " placed",
                           AiJson.objectOf(
                              "placed",
                              this.init_List.size(),
                              "mode",
                              style,
                              "remainingBlocks",
                              AiTelly.init_I(mc),
                              "nextCell",
                              support == null ? "secured" : support.toShortString()
                           )
                        );
                     } else {
                        return this.init(AiOperation.Status.FAILED, "Fell off the bridge");
                     }
                  }
               } else {
                  return this.init(AiOperation.Status.CANCELLED, "Telly bridge cancelled");
               }
            }

            AiOperation.Result init_AiOperation_Result_36() {
               return plan.targetX() != null
                  ? this.init(AiOperation.Status.SUCCEEDED, "Arrived at the target column")
                  : this.init(AiOperation.Status.SUCCEEDED, "Placed the requested blocks");
            }

            AiOperation.Result init(AiOperation.Status status, String message) {
               this.cancel();
               return AiGameOperations.result(
                  status,
                  message,
                  AiJson.objectOf(
                     "placed",
                     this.init_List.size(),
                     "from",
                     this.init_BlockPos.toShortString(),
                     "to",
                     mc.player != null ? mc.player.blockPosition().toShortString() : this.init_BlockPos.toShortString()
                  )
               );
            }

            @Override
            public void cancel() {
               if (!this.init_Z) {
                  this.init_Z = true;
                  autismclient.ai.engine.AiMotionSafety.releaseMovementKeys();
                  this.init_DataUtil.clear();
                  if (mc.player != null && this.write >= 0 && mc.player.getInventory().getSelectedSlot() == this.write) {
                     mc.player.getInventory().setSelectedSlot(this.read);
                  }
               }
            }
         };
      } else {
         throw new IllegalArgumentException("Telly bridging requires a connected living player");
      }
   }

   record TellyPlan(Integer targetX, Integer targetZ, Double yawDegrees, int blocks, AiTelly.BridgeMode mode, boolean verify, int timeoutSeconds) {
      static AiTelly.TellyPlan parse(JsonObject args) {
         boolean hasX = args.has("x");
         boolean hasZ = args.has("z");
         boolean hasDirection = args.has("direction");
         if (hasX != hasZ) {
            throw new IllegalArgumentException("A target column requires both x and z");
         }

         if (hasX == hasDirection) {
            throw new IllegalArgumentException(
               hasX ? "Pass either the x/z target column or direction, not both" : "Pass a target column (x and z) or a direction yaw in degrees"
            );
         }

         AiTelly.BridgeMode var10000;
         switch (AiJson.getString(args, "mode", "telly").toLowerCase(Locale.ROOT)) {
            case "walk":
               var10000 = AiTelly.BridgeMode.WALK;
               break;
            case "sprint":
               var10000 = AiTelly.BridgeMode.SPRINT;
               break;
            case "telly":
               var10000 = AiTelly.BridgeMode.TELLY;
               break;
            default:
               throw new IllegalArgumentException("mode must be walk, sprint or telly");
         }

         AiTelly.BridgeMode mode = var10000;
         return new AiTelly.TellyPlan(
            hasX ? AiJson.getInt(args, "x", 0) : null,
            hasX ? AiJson.getInt(args, "z", 0) : null,
            hasDirection ? AiJson.getDouble(args, "direction", 0.0) : null,
            Math.max(1, Math.min(256, AiJson.getInt(args, "blocks", 32))),
            mode,
            AiJson.getBoolean(args, "verify", false),
            Math.max(1, Math.min(300, AiJson.getInt(args, "timeoutSeconds", 60)))
         );
      }

      double dirX() {
         return -Math.sin(Math.toRadians(this.yawDegrees));
      }

      double dirZ() {
         return Math.cos(Math.toRadians(this.yawDegrees));
      }
   }

   record HotbarEntry(boolean construction, int count) {
   }

   enum BridgeMode {
      WALK,
      SPRINT,
      TELLY;
   }
}

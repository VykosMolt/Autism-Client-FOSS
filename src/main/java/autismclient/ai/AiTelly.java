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

   static boolean fellBelowLane(double playerY, double walkSurfaceY) {
      return playerY < walkSurfaceY - 2.0;
   }

   static List<AiTelly.HotbarEntry> hotbarBlocks(Minecraft mc) {
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

   static int hotbarBlockCount(Minecraft mc) {
      int total = 0;

      for (AiTelly.HotbarEntry entry : hotbarBlocks(mc)) {
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
            final BlockPos startPos = mc.player.blockPosition();
            final long deadline = System.nanoTime() + plan.timeoutSeconds() * 1000000000L;
            final int originalSlot = mc.player.getInventory().getSelectedSlot();
            final autismclient.ai.engine.AiAim aim = new autismclient.ai.engine.AiAim();
            final List<BlockPos> placed = new ArrayList<>();
            autismclient.ai.engine.AiBridgeDriver bridge;
            int selectedSlot = -1;
            int loadWait = -1;
            int stuckTicks;
            double lastX = 0.0 / 0.0;
            double lastZ;
            boolean cancelled;

            autismclient.ai.engine.AiBridgeDriver createBridge() {
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
                  length = plan.blocks() - this.placed.size();
               }

               length = Math.min(length, plan.blocks() - this.placed.size());
               if (length <= 0) {
                  return null;
               }

               autismclient.ai.engine.AiBridger.Inner_CachePool lane = new autismclient.ai.engine.AiBridger.Inner_CachePool(
                  feet.getX(), feet.getZ(), feet.getY() - 1, dx, dz, length
               );
               return new autismclient.ai.engine.AiBridgeDriver(
                  lane,
                  autismclient.ai.engine.AiBridgeDriver.chooseStyle(plan.mode() == AiTelly.BridgeMode.TELLY, lane, mc.player, AiTelly.hotbarBlockCount(mc)),
                  this.aim
               );
            }

            void selectSlot(int slot) {
               if (slot != mc.player.getInventory().getSelectedSlot()) {
                  mc.player.getInventory().setSelectedSlot(slot);
                  this.selectedSlot = slot;
               }
            }

            @Override
            public AiOperation.Result tick() {
               if (!this.cancelled && mc.player != null && mc.level != null) {
                  if (mc.player.getHealth() <= 4.0F && !AiGuard.isLowHealthOverrideActive()) {
                     return this.finish(AiOperation.Status.FAILED, AiGuard.formatLowHealthAbort("bridging", mc.player.getHealth(), mc.player.getMaxHealth()));
                  }

                  if (System.nanoTime() > this.deadline) {
                     return this.finish(AiOperation.Status.TIMED_OUT, "Telly bridge deadline reached");
                  }

                  if (!AiTerrain.isLoaded(mc.level, mc.player.blockPosition())) {
                     autismclient.ai.engine.AiMotionSafety.releaseMovementKeys();
                     return AiGameOperations.result(
                        AiOperation.Status.RUNNING, "Waiting for current terrain to load", AiJson.objectOf("placed", this.placed.size())
                     );
                  }

                  if (this.loadWait >= 0) {
                     autismclient.ai.engine.AiMotionSafety.releaseMovementKeys();
                     if (++this.loadWait < 20) {
                        return AiGameOperations.result(
                           AiOperation.Status.RUNNING, "Verifying the supports", AiJson.objectOf("placed", this.placed.size())
                        );
                     }

                     long gone = this.placed.stream().filter(c -> mc.level.getBlockState(c).getCollisionShape(mc.level, c).isEmpty()).count();
                     return gone == 0L
                        ? this.arrived()
                        : this.finish(AiOperation.Status.UNCERTAIN, gone + " placed supports are gone again (the server refused them)");
                  } else {
                     if (this.bridge == null) {
                        this.bridge = this.createBridge();
                        if (this.bridge == null) {
                           if (plan.verify()) {
                              this.loadWait = 0;
                              return AiGameOperations.result(
                                 AiOperation.Status.RUNNING, "Verifying the supports", AiJson.objectOf("placed", this.placed.size())
                              );
                           }

                           return this.arrived();
                        }
                     }

                     BlockPos support = this.bridge.nextSupport();
                     if (support != null && AiTelly.init_Z_Minecraft_BlockPos(mc, support)) {
                        return this.finish(AiOperation.Status.FAILED, "Refusing to bridge over lava at " + support.toShortString());
                     }

                     int slot = AiTelly.init_I_I_List(mc.player.getInventory().getSelectedSlot(), AiTelly.hotbarBlocks(mc));
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
                           ? this.finish(AiOperation.Status.FAILED, "Out of blocks")
                           : AiGameOperations.result(
                              AiOperation.Status.RUNNING, "Out of blocks, stopping on the bridge", AiJson.objectOf("placed", this.placed.size())
                           );
                     }

                     autismclient.ai.engine.AiBridgeDriver.BridgeStatus step = this.bridge.step(slot, this::selectSlot, this.placed::add);
                     if (step == autismclient.ai.engine.AiBridgeDriver.BridgeStatus.INVALID_FOOTING) {
                        return this.finish(AiOperation.Status.FAILED, "Bridge needs footing at the support's top level");
                     }

                     if (step != autismclient.ai.engine.AiBridgeDriver.BridgeStatus.FELL
                        && !AiTelly.fellBelowLane(mc.player.getY(), this.bridge.getLane().floorY() + 1)) {
                        String style = this.bridge.getBridger().getStyle().name().toLowerCase(Locale.ROOT);
                        if (step == autismclient.ai.engine.AiBridgeDriver.BridgeStatus.DONE) {
                           this.bridge = null;
                           return AiGameOperations.result(
                              AiOperation.Status.RUNNING, "Lane done", AiJson.objectOf("placed", this.placed.size(), "mode", style)
                           );
                        }

                        Vec3 pos = mc.player.position();
                        if (Double.compare(this.lastX, pos.x) == 0 && Double.compare(this.lastZ, pos.z) == 0) {
                           if (++this.stuckTicks > 100) {
                              return this.finish(AiOperation.Status.FAILED, "Wedged against terrain; no progress");
                           }
                        } else {
                           this.stuckTicks = 0;
                        }

                        this.lastX = pos.x;
                        this.lastZ = pos.z;
                        return AiGameOperations.result(
                           AiOperation.Status.RUNNING,
                           "Bridging (" + style + "), " + this.placed.size() + " placed",
                           AiJson.objectOf(
                              "placed",
                              this.placed.size(),
                              "mode",
                              style,
                              "remainingBlocks",
                              AiTelly.hotbarBlockCount(mc),
                              "nextCell",
                              support == null ? "secured" : support.toShortString()
                           )
                        );
                     } else {
                        return this.finish(AiOperation.Status.FAILED, "Fell off the bridge");
                     }
                  }
               } else {
                  return this.finish(AiOperation.Status.CANCELLED, "Telly bridge cancelled");
               }
            }

            AiOperation.Result arrived() {
               return plan.targetX() != null
                  ? this.finish(AiOperation.Status.SUCCEEDED, "Arrived at the target column")
                  : this.finish(AiOperation.Status.SUCCEEDED, "Placed the requested blocks");
            }

            AiOperation.Result finish(AiOperation.Status status, String message) {
               this.cancel();
               return AiGameOperations.result(
                  status,
                  message,
                  AiJson.objectOf(
                     "placed",
                     this.placed.size(),
                     "from",
                     this.startPos.toShortString(),
                     "to",
                     mc.player != null ? mc.player.blockPosition().toShortString() : this.startPos.toShortString()
                  )
               );
            }

            @Override
            public void cancel() {
               if (!this.cancelled) {
                  this.cancelled = true;
                  autismclient.ai.engine.AiMotionSafety.releaseMovementKeys();
                  this.aim.clear();
                  if (mc.player != null && this.selectedSlot >= 0 && mc.player.getInventory().getSelectedSlot() == this.selectedSlot) {
                     mc.player.getInventory().setSelectedSlot(this.originalSlot);
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

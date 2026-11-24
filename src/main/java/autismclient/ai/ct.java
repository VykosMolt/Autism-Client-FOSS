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

final class ct {
   static int init_I_I_List(int held, List<ct.Inner_DataUtil> hotbar) {
      if (held >= 0 && held < hotbar.size()) {
         ct.Inner_DataUtil entry = hotbar.get(held);
         if (entry.construction() && entry.count() > 0) {
            return held;
         }
      }

      for (int i = 0; i < hotbar.size(); i++) {
         ct.Inner_DataUtil entry = hotbar.get(i);
         if (entry.construction() && entry.count() > 0) {
            return i;
         }
      }

      return -1;
   }

   static boolean init_Z_D_D(double playerY, double walkSurfaceY) {
      return playerY < walkSurfaceY - 2.0;
   }

   static List<ct.Inner_DataUtil> init_List(Minecraft mc) {
      List<ct.Inner_DataUtil> view = new ArrayList<>(9);

      for (int i = 0; i < 9; i++) {
         ItemStack stack = mc.player.getInventory().getItem(i);
         boolean construction = stack.getItem() instanceof BlockItem block
            && !(block.getBlock() instanceof FallingBlock)
            && block.getBlock().defaultBlockState().isCollisionShapeFullBlock(mc.level, mc.player.blockPosition())
            && !block.getBlock().defaultBlockState().hasBlockEntity()
            && autismclient.ai.a.UtilCore.load(stack, mc.player.getInventory());
         view.add(new ct.Inner_DataUtil(construction, construction ? stack.getCount() : 0));
      }

      return view;
   }

   static int init_I(Minecraft mc) {
      int total = 0;

      for (ct.Inner_DataUtil entry : init_List(mc)) {
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

   static AiOperation init(JsonObject args) {
      final ct.Inner_CachePool plan = ct.Inner_CachePool.parse(args);
      final Minecraft mc = Minecraft.getInstance();
      if (mc.player != null && mc.level != null) {
         return new AiOperation() {
            static final int init_I = 100;
            static final int load_I = 20;
            final BlockPos init_BlockPos = mc.player.blockPosition();
            final long init_J = System.nanoTime() + plan.timeoutSeconds() * 1000000000L;
            final int read = mc.player.getInventory().getSelectedSlot();
            final autismclient.ai.a.DataUtil init_DataUtil = new autismclient.ai.a.DataUtil();
            final List<BlockPos> init_List = new ArrayList<>();
            autismclient.ai.a.NetHelper init_NetHelper;
            int write = -1;
            int update = -1;
            int sync;
            double init_D = 0.0 / 0.0;
            double load_D;
            boolean init_Z;

            autismclient.ai.a.NetHelper init_NetHelper() {
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
                  int[] d = autismclient.ai.a.NetHelper.init(plan.yawDegrees());
                  dx = d[0];
                  dz = d[1];
                  length = plan.blocks() - this.init_List.size();
               }

               length = Math.min(length, plan.blocks() - this.init_List.size());
               if (length <= 0) {
                  return null;
               }

               autismclient.ai.a.JsonUtil.Inner_CachePool lane = new autismclient.ai.a.JsonUtil.Inner_CachePool(
                  feet.getX(), feet.getZ(), feet.getY() - 1, dx, dz, length
               );
               return new autismclient.ai.a.NetHelper(
                  lane,
                  autismclient.ai.a.NetHelper.init_JsonUtil_ConfigSto_Z(plan.mode() == ct.Inner_StreamUtil.read, lane, mc.player, ct.init_I(mc)),
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
                  if (mc.player.getHealth() <= 4.0F && !X.close()) {
                     return this.init(AiOperation.Status.FAILED, X.init_Str_Str("bridging", mc.player.getHealth(), mc.player.getMaxHealth()));
                  }

                  if (System.nanoTime() > this.init_J) {
                     return this.init(AiOperation.Status.TIMED_OUT, "Telly bridge deadline reached");
                  }

                  if (!cu.init(mc.level, mc.player.blockPosition())) {
                     autismclient.ai.a.AiMotionSafety.putAt_V();
                     return R.init_AiOperation_Result(
                        AiOperation.Status.RUNNING, "Waiting for current terrain to load", ag.init_JsonObject_ObjArr("placed", this.init_List.size())
                     );
                  }

                  if (this.update >= 0) {
                     autismclient.ai.a.AiMotionSafety.putAt_V();
                     if (++this.update < 20) {
                        return R.init_AiOperation_Result(
                           AiOperation.Status.RUNNING, "Verifying the supports", ag.init_JsonObject_ObjArr("placed", this.init_List.size())
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
                              return R.init_AiOperation_Result(
                                 AiOperation.Status.RUNNING, "Verifying the supports", ag.init_JsonObject_ObjArr("placed", this.init_List.size())
                              );
                           }

                           return this.init_AiOperation_Result_36();
                        }
                     }

                     BlockPos support = this.init_NetHelper.init_BlockPos();
                     if (support != null && ct.init_Z_Minecraft_BlockPos(mc, support)) {
                        return this.init(AiOperation.Status.FAILED, "Refusing to bridge over lava at " + support.toShortString());
                     }

                     int slot = ct.init_I_I_List(mc.player.getInventory().getSelectedSlot(), ct.init_List(mc));
                     if (slot < 0 && support != null) {
                        autismclient.ai.a.AiMotionSafety.putAt_V();
                        mc.options.keyUp.setDown(false);
                        mc.options.keyDown.setDown(false);
                        mc.options.keyLeft.setDown(false);
                        mc.options.keyRight.setDown(false);
                        mc.options.keyJump.setDown(false);
                        mc.options.keyShift.setDown(true);
                        Vec3 v = mc.player.getDeltaMovement();
                        return mc.player.onGround() && v.x * v.x + v.z * v.z < 1.0E-4
                           ? this.init(AiOperation.Status.FAILED, "Out of blocks")
                           : R.init_AiOperation_Result(
                              AiOperation.Status.RUNNING, "Out of blocks, stopping on the bridge", ag.init_JsonObject_ObjArr("placed", this.init_List.size())
                           );
                     }

                     autismclient.ai.a.NetHelper.Inner_DataUtil step = this.init_NetHelper.init(slot, this::init, this.init_List::add);
                     if (step == autismclient.ai.a.NetHelper.Inner_DataUtil.write) {
                        return this.init(AiOperation.Status.FAILED, "Bridge needs footing at the support's top level");
                     }

                     if (step != autismclient.ai.a.NetHelper.Inner_DataUtil.read
                        && !ct.init_Z_D_D(mc.player.getY(), this.init_NetHelper.init_JsonUtil_CachePool().floorY() + 1)) {
                        String style = this.init_NetHelper.init_JsonUtil().init_JsonUtil_ConfigSto().name().toLowerCase(Locale.ROOT);
                        if (step == autismclient.ai.a.NetHelper.Inner_DataUtil.load) {
                           this.init_NetHelper = null;
                           return R.init_AiOperation_Result(
                              AiOperation.Status.RUNNING, "Lane done", ag.init_JsonObject_ObjArr("placed", this.init_List.size(), "mode", style)
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
                        return R.init_AiOperation_Result(
                           AiOperation.Status.RUNNING,
                           "Bridging (" + style + "), " + this.init_List.size() + " placed",
                           ag.init_JsonObject_ObjArr(
                              "placed",
                              this.init_List.size(),
                              "mode",
                              style,
                              "remainingBlocks",
                              ct.init_I(mc),
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
               return R.init_AiOperation_Result(
                  status,
                  message,
                  ag.init_JsonObject_ObjArr(
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
                  autismclient.ai.a.AiMotionSafety.putAt_V();
                  this.init_DataUtil.init_V();
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

   record Inner_CachePool(Integer targetX, Integer targetZ, Double yawDegrees, int blocks, ct.Inner_StreamUtil mode, boolean verify, int timeoutSeconds) {
      static ct.Inner_CachePool parse(JsonObject args) {
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

         ct.Inner_StreamUtil var10000;
         switch (ag.init_Str_JsonObject(args, "mode", "telly").toLowerCase(Locale.ROOT)) {
            case "walk":
               var10000 = ct.Inner_StreamUtil.init;
               break;
            case "sprint":
               var10000 = ct.Inner_StreamUtil.load;
               break;
            case "telly":
               var10000 = ct.Inner_StreamUtil.read;
               break;
            default:
               throw new IllegalArgumentException("mode must be walk, sprint or telly");
         }

         ct.Inner_StreamUtil mode = var10000;
         return new ct.Inner_CachePool(
            hasX ? ag.init_I_JsonObject(args, "x", 0) : null,
            hasX ? ag.init_I_JsonObject(args, "z", 0) : null,
            hasDirection ? ag.init_D_JsonObject(args, "direction", 0.0) : null,
            Math.max(1, Math.min(256, ag.init_I_JsonObject(args, "blocks", 32))),
            mode,
            ag.init_Z_JsonObject(args, "verify", false),
            Math.max(1, Math.min(300, ag.init_I_JsonObject(args, "timeoutSeconds", 60)))
         );
      }

      double dirX() {
         return -Math.sin(Math.toRadians(this.yawDegrees));
      }

      double dirZ() {
         return Math.cos(Math.toRadians(this.yawDegrees));
      }
   }

   record Inner_DataUtil(boolean construction, int count) {
   }

   enum Inner_StreamUtil {
      init,
      load,
      read;
   }
}

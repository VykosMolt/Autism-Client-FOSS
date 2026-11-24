package autismclient.ai;

import autismclient.api.ai.AiOperation;
import autismclient.util.U;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.FallingBlock;
import net.minecraft.world.phys.Vec3;

final class bN {
   static int init(double yaw) {
      long q = Math.round(yaw / 45.0) * 45L;
      return (int)((q % 360L + 360L) % 360L);
   }

   static AiOperation init(JsonObject args) {
      final bN.Inner_CachePool plan = bN.Inner_CachePool.parse(args);
      final Minecraft mc = Minecraft.getInstance();
      if (mc.player != null && mc.level != null) {
         return new AiOperation() {
            static final double init_D = 1.6;
            static final double load_D = 4.5;
            static final double read_D = -0.35;
            static final int init_I = 5;
            static final int load_I = 10;
            static final int read_I = 60;
            static final double write_D = 1.0;
            final BlockPos init_BlockPos = mc.player.blockPosition();
            final long init_J = System.nanoTime() + plan.timeoutSeconds() * 1000000000L;
            final autismclient.ai.a.DataUtil init_DataUtil = new autismclient.ai.a.DataUtil();
            final int write_I = bN.init(mc.player.getYRot());
            autismclient.ai.a.NetHelper init_NetHelper;
            final int update_I = mc.player.getInventory().getSelectedSlot();
            int sync_I;
            int flush_I = -1;
            BlockPos load_BlockPos;
            long load_J;
            int close_I;
            boolean init_Z;
            boolean load_Z;
            boolean read_Z;
            boolean write_Z;
            boolean update_Z;
            int open_I;
            int poll;
            double update_D = 0.0 / 0.0;
            BlockPos read_BlockPos;
            BlockPos write_BlockPos;
            double sync_D = 0.0 / 0.0;
            double flush_D = 0.0 / 0.0;
            double close_D;
            double open_D;
            int push;
            String init_Str = "Scaffolding";

            bN.Inner_DataUtil init_bN_DataUtil() {
               return plan.legs().get(this.open_I);
            }

            boolean init_Z_21() {
               return this.open_I >= plan.legs().size() - 1;
            }

            boolean init(BlockPos cell) {
               return cu.init(mc.level, cell) && !mc.level.getBlockState(cell).getCollisionShape(mc.level, cell).isEmpty();
            }

            boolean init(int bx, int bz) {
               BlockPos cell = new BlockPos(bx, (int)Math.floor(this.update_D - 1.0), bz);
               return !cu.init(mc.level, cell) ? true : !mc.level.getBlockState(cell).getCollisionShape(mc.level, cell).isEmpty();
            }

            boolean load(BlockPos support) {
               for (int i = 1; i <= 3; i++) {
                  if (mc.level.getBlockState(support.below(i)).is(Blocks.LAVA)) {
                     return true;
                  }
               }

               return false;
            }

            List<ct.Inner_DataUtil> init_List_7() {
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

            int init_I_27() {
               int total = 0;

               for (ct.Inner_DataUtil entry : this.init_List_7()) {
                  total += entry.count();
               }

               return total;
            }

            boolean load() {
               int slot = ct.init_I_I_List(mc.player.getInventory().getSelectedSlot(), this.init_List_7());
               if (slot < 0) {
                  return false;
               }

               if (slot != mc.player.getInventory().getSelectedSlot()) {
                  mc.player.getInventory().setSelectedSlot(slot);
                  this.flush_I = slot;
               }

               return true;
            }

            boolean read(BlockPos cell) {
               ItemStack stack = mc.player.getInventory().getItem(mc.player.getInventory().getSelectedSlot());
               U.Inner_StateCache request = new U.Inner_StateCache(
                     cell, mc.player.getEyePosition(), Math.max(4.5, mc.player.blockInteractionRange()), U.init(stack, mc.player, InteractionHand.MAIN_HAND)
                  )
                  .init_U_StateCache_Z(mc.player.isShiftKeyDown())
                  .init_U_StateCache_U_StreamUtil(new U.Inner_StreamUtil(6));
               U.Inner_CachePool candidate = U.init_U_CachePool(request);
               if (candidate == null) {
                  return false;
               }

               float prevYaw = mc.player.getYRot();
               float prevPitch = mc.player.getXRot();
               mc.player.setYRot(candidate.init_U_DataUtil().load().init());
               mc.player.setXRot(candidate.init_U_DataUtil().load().load());
               autismclient.ai.a.AiCameraTurn.putAt(mc.player, prevYaw, prevPitch);
               this.write_Z = true;
               mc.gameMode.useItemOn(mc.player, InteractionHand.MAIN_HAND, candidate.init_BlockHitResult());
               return true;
            }

            bN.Inner_StreamUtil init_bN_StreamUtil() {
               return mc.player.getFoodData().getFoodLevel() <= 6 ? bN.Inner_StreamUtil.init : plan.mode();
            }

            void init(String reason) {
               this.open_I++;
               this.poll = 0;
               this.update_D = 0.0 / 0.0;
               this.read_BlockPos = null;
               this.write_BlockPos = null;
               this.sync_D = 0.0 / 0.0;
               if (!this.init_Z_21()) {
                  this.init_Str = "Leg advanced (" + reason + ")";
               }
            }

            @Override
            public AiOperation.Result tick() {
               if (!this.update_Z && mc.player != null && mc.level != null) {
                  if (mc.player.getHealth() <= 4.0F && !X.close()) {
                     return this.init(AiOperation.Status.FAILED, X.init_Str_Str("scaffolding", mc.player.getHealth(), mc.player.getMaxHealth()));
                  }

                  if (System.nanoTime() > this.init_J) {
                     return this.init(AiOperation.Status.TIMED_OUT, "Scaffold deadline reached");
                  }

                  if (!cu.init(mc.level, mc.player.blockPosition())) {
                     autismclient.ai.a.AiMotionSafety.putAt_V();
                     return R.init_AiOperation_Result(
                        AiOperation.Status.RUNNING, "Waiting for current terrain to load", ag.init_JsonObject_ObjArr("placed", this.sync_I)
                     );
                  }

                  this.write_Z = false;
                  Vec3 pos = mc.player.position();
                  this.read_Z = false;
                  if (this.load_BlockPos != null) {
                     if (this.init(this.load_BlockPos)) {
                        if (!this.load_Z) {
                           this.sync_I++;
                           this.poll++;
                           this.read_Z = true;
                        }

                        if (plan.verify() && ca.init_J_BlockPos(this.load_BlockPos) <= this.load_J) {
                           if (++this.close_I > 60) {
                              return this.init(AiOperation.Status.UNCERTAIN, "Placement dispatched; no applied server block confirmation");
                           }
                        } else {
                           this.load_BlockPos = null;
                        }
                     } else if (++this.close_I > 5) {
                        if (!this.init_Z) {
                           this.init_Z = true;
                           if (!this.load()) {
                              return this.init(AiOperation.Status.FAILED, "Out of blocks");
                           }

                           this.read(this.load_BlockPos);
                        } else if (this.close_I > 10) {
                           return this.init(AiOperation.Status.FAILED, "Placement was not applied at " + this.load_BlockPos.toShortString());
                        }
                     }
                  }

                  if (!this.read_Z
                     && Double.compare(this.flush_D, pos.x) == 0
                     && Double.compare(this.close_D, pos.y) == 0
                     && Double.compare(this.open_D, pos.z) == 0) {
                     if (++this.push > 100) {
                        return this.init(AiOperation.Status.FAILED, "Wedged against terrain; no progress");
                     }
                  } else {
                     this.push = 0;
                  }

                  this.flush_D = pos.x;
                  this.close_D = pos.y;
                  this.open_D = pos.z;
                  if (this.sync_I >= plan.blocks()) {
                     return this.init(AiOperation.Status.SUCCEEDED, "Placed the requested blocks");
                  } else if (this.open_I >= plan.legs().size()) {
                     return this.init(AiOperation.Status.SUCCEEDED, "Scaffold route complete");
                  } else {
                     return this.init_bN_DataUtil().tower() ? this.load(pos) : this.init(pos);
                  }
               } else {
                  return this.init(AiOperation.Status.CANCELLED, "Scaffold cancelled");
               }
            }

            void init(int slot) {
               if (slot != mc.player.getInventory().getSelectedSlot()) {
                  mc.player.getInventory().setSelectedSlot(slot);
                  this.flush_I = slot;
               }
            }

            AiOperation.Result init(Vec3 pos) {
               if (this.init_NetHelper == null) {
                  if (this.poll >= this.init_bN_DataUtil().blocks() || this.read_BlockPos != null) {
                     if (this.init_Z_21()) {
                        return this.init(AiOperation.Status.SUCCEEDED, "Placed the requested blocks");
                     }

                     this.init("bridge leg done");
                     return R.init_AiOperation_Result(AiOperation.Status.RUNNING, "Leg advanced", ag.init_JsonObject_ObjArr("placed", this.sync_I));
                  }

                  BlockPos feet = mc.player.blockPosition();
                  int[] d = autismclient.ai.a.NetHelper.init(plan.follow() ? this.write_I : this.init_bN_DataUtil().direction());
                  autismclient.ai.a.JsonUtil.Inner_CachePool cells = new autismclient.ai.a.JsonUtil.Inner_CachePool(
                     feet.getX(), feet.getZ(), feet.getY() - 1, d[0], d[1], this.init_bN_DataUtil().blocks() - this.poll
                  );
                  this.init_NetHelper = new autismclient.ai.a.NetHelper(
                     cells,
                     autismclient.ai.a.NetHelper.init_JsonUtil_ConfigSto_Z(
                        this.init_bN_StreamUtil() == bN.Inner_StreamUtil.read, cells, mc.player, this.init_I_27()
                     ),
                     this.init_DataUtil
                  );
                  this.update_D = feet.getY();
               }

               if (pos.y < this.update_D - 2.0) {
                  return this.init(AiOperation.Status.FAILED, "Fell off the scaffold");
               } else {
                  BlockPos support = this.init_NetHelper.init_BlockPos();
                  if (support != null && this.load(support)) {
                     return this.init(AiOperation.Status.FAILED, "Refusing to scaffold over lava at " + support.toShortString());
                  } else {
                     int slot = ct.init_I_I_List(mc.player.getInventory().getSelectedSlot(), this.init_List_7());
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
                              AiOperation.Status.RUNNING, "Out of blocks, stopping on the scaffold", ag.init_JsonObject_ObjArr("placed", this.sync_I)
                           );
                     } else {
                        autismclient.ai.a.NetHelper.Inner_DataUtil step = this.init_NetHelper.init(slot, this::init, cell -> {
                           this.sync_I++;
                           this.poll++;
                           this.read_Z = true;
                        });
                        if (step == autismclient.ai.a.NetHelper.Inner_DataUtil.write) {
                           return this.init(AiOperation.Status.FAILED, "Scaffold bridge needs footing at the support's top level");
                        } else if (step == autismclient.ai.a.NetHelper.Inner_DataUtil.read) {
                           return this.init(AiOperation.Status.FAILED, "Fell off the scaffold");
                        } else {
                           String style = this.init_NetHelper.init_JsonUtil().init_JsonUtil_ConfigSto().name().toLowerCase(Locale.ROOT);
                           if (step == autismclient.ai.a.NetHelper.Inner_DataUtil.load) {
                              this.read_BlockPos = mc.player.blockPosition();
                              this.init_NetHelper = null;
                              return R.init_AiOperation_Result(
                                 AiOperation.Status.RUNNING, "Leg done", ag.init_JsonObject_ObjArr("placed", this.sync_I, "leg", this.open_I)
                              );
                           } else {
                              return R.init_AiOperation_Result(
                                 AiOperation.Status.RUNNING,
                                 this.init_Str + ", " + this.sync_I + " placed",
                                 ag.init_JsonObject_ObjArr(
                                    "placed",
                                    this.sync_I,
                                    "mode",
                                    style,
                                    "leg",
                                    this.open_I,
                                    "remainingBlocks",
                                    this.init_I_27(),
                                    "nextCell",
                                    support == null ? "secured" : support.toShortString()
                                 )
                              );
                           }
                        }
                     }
                  }
               }
            }

            AiOperation.Result load(Vec3 pos) {
               if (this.poll >= this.init_bN_DataUtil().blocks()) {
                  if (this.init_Z_21()) {
                     return this.init(AiOperation.Status.SUCCEEDED, "Placed the requested blocks");
                  }

                  this.init("tower done");
                  return R.init_AiOperation_Result(AiOperation.Status.RUNNING, "Leg advanced", ag.init_JsonObject_ObjArr("placed", this.sync_I));
               } else {
                  BlockPos feet = mc.player.blockPosition();
                  boolean onGround = mc.player.onGround();
                  if (this.init(feet.above(2))) {
                     if (this.init_Z_21()) {
                        return this.init(AiOperation.Status.SUCCEEDED, "Ceiling reached");
                     }

                     this.init("ceiling reached");
                     return R.init_AiOperation_Result(AiOperation.Status.RUNNING, "Leg advanced (ceiling)", ag.init_JsonObject_ObjArr("placed", this.sync_I));
                  } else {
                     boolean jump = false;
                     if (onGround) {
                        this.write_BlockPos = feet;
                        this.sync_D = pos.y;
                        jump = true;
                     } else if (this.write_BlockPos != null
                        && !Double.isNaN(this.sync_D)
                        && pos.y >= this.sync_D + 1.0
                        && !this.init(this.write_BlockPos)
                        && this.load_BlockPos == null) {
                        if (this.load(this.write_BlockPos)) {
                           return this.init(AiOperation.Status.FAILED, "Refusing to scaffold over lava at " + this.write_BlockPos.toShortString());
                        }

                        if (!this.load()) {
                           return this.init(AiOperation.Status.FAILED, "Out of blocks");
                        }

                        if (this.read(this.write_BlockPos)) {
                           this.load_BlockPos = this.write_BlockPos;
                           this.load_J = ca.init_J_BlockPos(this.write_BlockPos);
                           this.close_I = 0;
                           this.init_Z = false;
                           this.load_Z = false;
                           if (this.init(this.write_BlockPos)) {
                              this.sync_I++;
                              this.poll++;
                              this.load_Z = true;
                              this.read_Z = true;
                              if (!plan.verify()) {
                                 this.load_BlockPos = null;
                              }
                           }
                        }
                     }

                     mc.options.keyUp.setDown(false);
                     mc.options.keySprint.setDown(false);
                     mc.options.keyJump.setDown(jump);
                     mc.options.keyShift.setDown(true);
                     if (!this.write_Z) {
                        mc.player.setXRot(60.0F);
                     }

                     return R.init_AiOperation_Result(
                        AiOperation.Status.RUNNING,
                        this.init_Str + " (tower), " + this.sync_I + " placed",
                        ag.init_JsonObject_ObjArr("placed", this.sync_I, "mode", "tower", "leg", this.open_I, "remainingBlocks", this.init_I_27())
                     );
                  }
               }
            }

            AiOperation.Result init(AiOperation.Status status, String message) {
               this.cancel();
               return R.init_AiOperation_Result(
                  status,
                  message,
                  ag.init_JsonObject_ObjArr(
                     "placed",
                     this.sync_I,
                     "from",
                     this.init_BlockPos.toShortString(),
                     "to",
                     mc.player != null ? mc.player.blockPosition().toShortString() : this.init_BlockPos.toShortString()
                  )
               );
            }

            @Override
            public void cancel() {
               if (!this.update_Z) {
                  this.update_Z = true;
                  autismclient.ai.a.AiMotionSafety.putAt_V();
                  this.init_DataUtil.init_V();
                  if (mc.player != null && this.flush_I >= 0 && mc.player.getInventory().getSelectedSlot() == this.flush_I) {
                     mc.player.getInventory().setSelectedSlot(this.update_I);
                  }
               }
            }
         };
      } else {
         throw new IllegalArgumentException("Scaffolding requires a connected living player");
      }
   }

   record Inner_CachePool(List<bN.Inner_DataUtil> legs, boolean follow, int blocks, bN.Inner_StreamUtil mode, boolean verify, int timeoutSeconds) {
      static bN.Inner_CachePool parse(JsonObject args) {
         boolean hasDirection = args.has("direction");
         boolean follow = ag.init_Z_JsonObject(args, "follow", false);
         boolean tower = ag.init_Z_JsonObject(args, "tower", false);
         boolean hasLegs = args.has("legs");
         int kinds = (hasDirection ? 1 : 0) + (follow ? 1 : 0) + (tower ? 1 : 0) + (hasLegs ? 1 : 0);
         if (kinds != 1) {
            throw new IllegalArgumentException("Pass exactly one of direction, follow:true, tower:true or legs");
         }

         int total = Math.max(1, Math.min(256, ag.init_I_JsonObject(args, "blocks", 32)));
         List<bN.Inner_DataUtil> legs = new ArrayList<>();
         if (hasLegs) {
            JsonArray raw = args.getAsJsonArray("legs");
            if (raw == null || raw.isEmpty()) {
               throw new IllegalArgumentException("legs must be a non-empty array");
            }

            for (JsonElement element : raw) {
               if (!element.isJsonObject()) {
                  throw new IllegalArgumentException("each leg must be an object");
               }

               JsonObject leg = element.getAsJsonObject();
               boolean legTower = ag.init_Z_JsonObject(leg, "tower", false);
               int legBlocks = Math.max(1, Math.min(256, ag.init_I_JsonObject(leg, "blocks", 1)));
               if (!legTower && !leg.has("direction")) {
                  throw new IllegalArgumentException("a bridge leg needs direction (yaw degrees)");
               }

               legs.add(new bN.Inner_DataUtil(legTower, legTower ? 0.0 : ag.init_D_JsonObject(leg, "direction", 0.0), legBlocks));
            }
         } else if (tower) {
            legs.add(new bN.Inner_DataUtil(true, 0.0, total));
         } else {
            legs.add(new bN.Inner_DataUtil(false, hasDirection ? ag.init_D_JsonObject(args, "direction", 0.0) : 0.0, total));
         }

         bN.Inner_StreamUtil var10000;
         switch (ag.init_Str_JsonObject(args, "mode", "walk").toLowerCase(Locale.ROOT)) {
            case "walk":
               var10000 = bN.Inner_StreamUtil.init;
               break;
            case "sprint":
               var10000 = bN.Inner_StreamUtil.load;
               break;
            case "telly":
               var10000 = bN.Inner_StreamUtil.read;
               break;
            default:
               throw new IllegalArgumentException("mode must be walk, sprint or telly");
         }

         bN.Inner_StreamUtil mode = var10000;
         return new bN.Inner_CachePool(
            List.copyOf(legs),
            follow,
            total,
            mode,
            ag.init_Z_JsonObject(args, "verify", false),
            Math.max(1, Math.min(300, ag.init_I_JsonObject(args, "timeoutSeconds", 60)))
         );
      }
   }

   record Inner_DataUtil(boolean tower, double direction, int blocks) {
      double dirX() {
         return -Math.sin(Math.toRadians(this.direction));
      }

      double dirZ() {
         return Math.cos(Math.toRadians(this.direction));
      }
   }

   enum Inner_StreamUtil {
      init,
      load,
      read;
   }
}

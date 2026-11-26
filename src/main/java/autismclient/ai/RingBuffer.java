package autismclient.ai;

import autismclient.AutismClientAddon;
import autismclient.ai.a.HashUtil;
import autismclient.api.ai.AiOperation;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import java.util.function.BooleanSupplier;
import java.util.function.Predicate;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Plane;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.monster.Enderman;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.FallingBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

final class RingBuffer {
   static final int init_I = 600;
   static final int load = 900;
   private static final int read = 3;
   private static final int write = 4;
   private static final int update = 40;
   private static final List<Direction> init_List = List.of(Direction.NORTH, Direction.EAST, Direction.SOUTH, Direction.WEST);

   private RingBuffer() {
   }

   static RingBuffer.Inner_StreamUtil init(boolean loaded, boolean fluid, boolean unbreakable, boolean open, boolean safeFloorBelow) {
      if (!loaded || fluid || unbreakable) {
         return RingBuffer.Inner_StreamUtil.read;
      } else if (open) {
         return safeFloorBelow ? RingBuffer.Inner_StreamUtil.load : RingBuffer.Inner_StreamUtil.read;
      } else {
         return RingBuffer.Inner_StreamUtil.init;
      }
   }

   static BlockPos init_BlockPos(BlockPos feet) {
      return feet.above(2);
   }

   static boolean init(long dayTime) {
      return dayTime >= 0L && dayTime < 1000L;
   }

   static List<BlockPos> init_List(BlockPos feet) {
      List<BlockPos> cells = new ArrayList<>();
      cells.add(feet.below());

      for (Direction side : init_List) {
         cells.add(feet.relative(side));
         cells.add(feet.above().relative(side));
      }

      cells.add(init_BlockPos(feet));
      return List.copyOf(cells);
   }

   static boolean init_Z_BlockPos_Predicate(BlockPos feet, Predicate<BlockPos> fullBlock) {
      return init_List(feet).stream().allMatch(fullBlock);
   }

   static List<BlockPos> init_List_BlockPos_Predicate(BlockPos feet, Predicate<BlockPos> fullBlock) {
      List<BlockPos> cells = new ArrayList<>();
      Set<BlockPos> planned = new HashSet<>();
      Predicate<BlockPos> support = pos -> fullBlock.test(pos) || planned.contains(pos);

      for (Direction side : init_List) {
         BlockPos wall = feet.relative(side);
         if (!fullBlock.test(wall)) {
            boolean adjacent = false;

            for (Direction face : Direction.values()) {
               if (support.test(wall.relative(face))) {
                  adjacent = true;
                  break;
               }
            }

            if (!adjacent && !support.test(wall.below())) {
               cells.add(wall.below());
               planned.add(wall.below());
            }

            cells.add(wall);
            planned.add(wall);
         }
      }

      for (Direction side : List.of(Direction.EAST, Direction.SOUTH, Direction.WEST, Direction.NORTH)) {
         BlockPos wall = feet.above().relative(side);
         if (!fullBlock.test(wall)) {
            cells.add(wall);
            planned.add(wall);
         }
      }

      BlockPos roof = init_BlockPos(feet);
      if (!fullBlock.test(roof)) {
         boolean adjacent = false;

         for (Direction face : Direction.values()) {
            if (support.test(roof.relative(face))) {
               adjacent = true;
               break;
            }
         }

         if (!adjacent) {
            BlockPos beam = roof.north();
            cells.add(beam);
            planned.add(beam);
         }

         cells.add(roof);
      }

      return List.copyOf(cells);
   }

   static boolean init(float health, float maxHealth, int food, boolean hasFood, boolean sealed, boolean supported, boolean interruptible, boolean pending) {
      return health < maxHealth - 0.01F && food < 18 && hasFood && sealed && supported && interruptible && !pending;
   }

   static boolean init(
      boolean healing,
      boolean waiting,
      boolean burning,
      boolean onFireDamage,
      boolean supported,
      boolean sealed,
      boolean hostileInside,
      boolean touchingFireOrLava
   ) {
      return healing && waiting && burning && onFireDamage && supported && sealed && !hostileInside && !touchingFireOrLava;
   }

   static boolean init_Z_Z_BooleanSupplier(boolean placing, BooleanSupplier pending) {
      return placing && pending.getAsBoolean();
   }

   static boolean init_Z_F_J(float healTo, long dayTime) {
      return healTo <= 0.0F && init(dayTime);
   }

   static boolean init(float healTo, boolean supported, boolean sealed, boolean hostileInside, boolean bodyHazard) {
      return healTo > 0.0F && supported && sealed && !hostileInside && !bodyHazard;
   }

   static boolean init(BlockPos feet, AABB body, double feetY) {
      return body.minX >= feet.getX() + 0.001
         && body.maxX <= feet.getX() + 0.999
         && body.minZ >= feet.getZ() + 0.001
         && body.maxZ <= feet.getZ() + 0.999
         && feetY >= feet.getY() - 0.05
         && feetY < feet.getY() + 1.5;
   }

   static boolean init(int feetY, int groundY, boolean supported) {
      return feetY <= groundY && supported;
   }

   static boolean init(AiOperation.Status status, boolean pendingBefore, boolean pendingAfter) {
      return status == AiOperation.Status.UNCERTAIN || pendingAfter || status != AiOperation.Status.SUCCEEDED && pendingBefore;
   }

   static BlockPos init(BlockPos feet, Predicate<BlockPos> supportedStand, Predicate<BlockPos> safeColumn) {
      BlockPos best = null;
      int price = 2147483647;

      for (int x = -3; x <= 3; x++) {
         for (int z = -3; z <= 3; z++) {
            for (int y = -1; y <= 1; y++) {
               int walk = Math.abs(x) + Math.abs(z);
               if (walk != 0 && walk <= 3) {
                  BlockPos pos = feet.offset(x, y, z);
                  int candidate = walk + 4 * Math.abs(y);
                  if (candidate < price && supportedStand.test(pos) && safeColumn.test(pos)) {
                     best = pos;
                     price = candidate;
                  }
               }
            }
         }
      }

      return best;
   }

   static boolean init() {
      Minecraft mc = Minecraft.getInstance();
      if (mc.player == null) {
         return false;
      }

      int blocks = 0;

      for (int slot = 0; slot < 36; slot++) {
         ItemStack stack = mc.player.getInventory().getItem(slot);
         if (!stack.isEmpty()
            && stack.getItem() instanceof BlockItem block
            && !(block.getBlock() instanceof FallingBlock)
            && block.getBlock().defaultBlockState().isCollisionShapeFullBlock(mc.level, mc.player.blockPosition())) {
            blocks += stack.getCount();
         }
      }

      return blocks >= 8;
   }

   static AiOperation init(JsonObject args) {
      final Minecraft mc = Minecraft.getInstance();
      if (mc.player != null && mc.level != null) {
         final int timeoutSeconds = Math.max(60, Math.min(900, ag.init_I_JsonObject(args, "timeoutSeconds", 600)));
         final float healTo = (float)ag.init_D_JsonObject(args, "healTo", 0.0);
         final boolean stay = ag.init_Z_JsonObject(args, "stay", false);
         final boolean towerOk = ag.init_Z_JsonObject(args, "towerOk", true);
         return new AiOperation() {
            final long init_J = System.nanoTime() + timeoutSeconds * 1000000000L;
            final Object init_Obj = mc.level;
            final Object load_Obj = mc.player;
            final Object read_Obj = mc.getConnection();
            final BlockPos init_BlockPos = mc.player.blockPosition().immutable();
            final autismclient.ai.a.DataPipe init_DataPipe = new autismclient.ai.a.DataPipe().init_DataPipe();
            final autismclient.ai.a.UtilCore init_UtilCore = new autismclient.ai.a.UtilCore(mc);
            RingBuffer.Inner_DataUtil init_RingBuffer_DataUti = RingBuffer.Inner_DataUtil.init;
            AiOperation init_AiOperation;
            AiOperation load_AiOperation;
            BlockPos load_BlockPos = this.init_BlockPos;
            BlockPos read_BlockPos;
            BlockPos write_BlockPos;
            BlockPos update_BlockPos;
            List<BlockPos> init_List;
            Item init_Item;
            int init_I;
            int load_I;
            int read_I;
            int write_I;
            int update_I;
            int sync_I;
            boolean init_Z;
            boolean load_Z;
            boolean read_Z;
            boolean write_Z;
            boolean update_Z;
            boolean sync_Z;
            boolean flush_Z;
            JsonObject init_JsonObject = new JsonObject();
            float init_F = mc.player.getHealth();
            int flush_I;

            @Override
            public AiOperation.Result tick() {
               if (!this.read_Z && mc.player != null && mc.level != null) {
                  if (mc.level != this.init_Obj || mc.player != this.load_Obj || mc.getConnection() != this.read_Obj) {
                     return this.init(AiOperation.Status.FAILED, "World/player changed during burrow");
                  }

                  if (!mc.player.isAlive()) {
                     return this.init(AiOperation.Status.FAILED, "Player died during burrow");
                  }

                  if (this.sync_Z) {
                     mc.options.keyJump.setDown(false);
                     this.sync_Z = false;
                  }

                  BlockPos feet = mc.player.blockPosition();
                  float health = mc.player.getHealth();
                  float previousHealth = this.init_F;
                  boolean hurt = health < previousHealth - 0.01F;
                  this.init_F = health;
                  if (this.init_RingBuffer_DataUti == RingBuffer.Inner_DataUtil.init
                     && this.init_AiOperation == null
                     && RingBuffer.init(
                        healTo,
                        autismclient.ai.a.AiMotionSafety.fill_Z(),
                        RingBuffer.init_Z_BlockPos_Predicate(feet, this::init_Z_3) && this.write_Z(feet),
                        this.update_Z(feet),
                        mc.player.isInWater() || mc.player.isInLava() || u.init_Z(mc)
                     )) {
                     this.update_BlockPos = feet.immutable();
                     this.write_BlockPos = RingBuffer.init_BlockPos(this.update_BlockPos);
                     this.load_Z = this.read_Z(this.update_BlockPos);
                     this.init_RingBuffer_DataUti = this.load_Z ? RingBuffer.Inner_DataUtil.sync : RingBuffer.Inner_DataUtil.read;
                  }

                  boolean exiting = this.init_RingBuffer_DataUti == RingBuffer.Inner_DataUtil.flush
                     || this.init_RingBuffer_DataUti == RingBuffer.Inner_DataUtil.close;
                  if (!exiting && this.init_RingBuffer_DataUti != RingBuffer.Inner_DataUtil.write && mc.player.isInWater()) {
                     this.flush_Z = this.flush_Z | this.requiresReconciliationAfterEmergency();
                     if (this.init_AiOperation != null) {
                        this.init_AiOperation.cancel();
                        this.init_AiOperation = null;
                     }

                     if (this.load_AiOperation != null) {
                        this.load_AiOperation.cancel();
                        this.load_AiOperation = null;
                     }

                     this.init_DataPipe.init_V();
                     this.init_Z = false;
                     this.write_Z = true;
                     this.init_RingBuffer_DataUti = this.load_Z ? RingBuffer.Inner_DataUtil.flush : RingBuffer.Inner_DataUtil.close;
                     return this.init("Water in the burrow; climbing out");
                  } else {
                     boolean building = this.init_RingBuffer_DataUti == RingBuffer.Inner_DataUtil.init
                        || this.init_RingBuffer_DataUti == RingBuffer.Inner_DataUtil.load
                        || this.init_RingBuffer_DataUti == RingBuffer.Inner_DataUtil.read
                        || this.init_RingBuffer_DataUti == RingBuffer.Inner_DataUtil.write
                        || this.init_RingBuffer_DataUti == RingBuffer.Inner_DataUtil.update;
                     boolean residualBurn = hurt
                        && RingBuffer.init(
                           healTo > 0.0F,
                           this.init_RingBuffer_DataUti == RingBuffer.Inner_DataUtil.sync,
                           mc.player.isOnFire(),
                           ca.init_Z_UUID(mc.player.getUUID(), mc.player.tickCount, previousHealth, health),
                           autismclient.ai.a.AiMotionSafety.fill_Z(),
                           this.init_RingBuffer_DataUti == RingBuffer.Inner_DataUtil.sync && this.init_Z_6(),
                           this.init_RingBuffer_DataUti == RingBuffer.Inner_DataUtil.sync && this.update_Z(this.update_BlockPos),
                           mc.player.isInLava() || u.init_Z(mc)
                        );
                     if (hurt && !exiting && (!(healTo > 0.0F) || !building) && !residualBurn) {
                        return this.init(
                           AiOperation.Status.FAILED, "burrow compromised (hurt while " + this.init_RingBuffer_DataUti.name().toLowerCase(Locale.ROOT) + ")"
                        );
                     }

                     if (this.init_RingBuffer_DataUti != RingBuffer.Inner_DataUtil.sync || this.init_Z_6() && !this.update_Z(this.update_BlockPos)) {
                        if (this.init_RingBuffer_DataUti == RingBuffer.Inner_DataUtil.read && !this.load_Z && this.init(feet, 1.0)) {
                           return this.init(AiOperation.Status.FAILED, "burrow compromised (a hostile in the shaft before the cap)");
                        }

                        if (System.nanoTime() > this.init_J && !exiting && this.init_RingBuffer_DataUti != RingBuffer.Inner_DataUtil.sync) {
                           return this.init(AiOperation.Status.TIMED_OUT, "Burrow deadline reached before cover");
                        }

                        if (this.init_UtilCore.init_Z()) {
                           autismclient.ai.a.UtilCore.Inner_BufferHelper status = this.init_UtilCore.load_UtilCore_BufferHel();
                           if (status == autismclient.ai.a.UtilCore.Inner_BufferHelper.write) {
                              return this.init(AiOperation.Status.FAILED, "Construction stock swap was not confirmed");
                           }

                           if (status == autismclient.ai.a.UtilCore.Inner_BufferHelper.read) {
                              this.init_UtilCore.init_V();
                           }

                           return this.init("Confirming shelter construction stock in the hotbar");
                        } else {
                           AiOperation.Result var10000;
                           switch (this.init_RingBuffer_DataUti) {
                              case init:
                                 var10000 = this.init_AiOperation_Result(feet);
                                 break;
                              case load:
                                 var10000 = this.init_AiOperation_Result_7();
                                 break;
                              case read:
                              case update:
                                 var10000 = this.load_AiOperation_Result(feet);
                                 break;
                              case write:
                                 var10000 = this.read_AiOperation_Result(feet);
                                 break;
                              case sync:
                                 var10000 = this.write_AiOperation_Result(feet);
                                 break;
                              case flush:
                                 var10000 = this.update_AiOperation_Result(feet);
                                 break;
                              case close:
                                 var10000 = this.load_AiOperation_Result_6();
                                 break;
                              default:
                                 throw new MatchException(null, null);
                           }

                           return var10000;
                        }
                     } else {
                        return this.init(AiOperation.Status.FAILED, "burrow compromised (cover opened or a hostile inside)");
                     }
                  }
               } else {
                  return this.init(AiOperation.Status.CANCELLED, "Burrow cancelled");
               }
            }

            AiOperation.Result init_AiOperation_Result(BlockPos feet) {
               int descended = this.load_BlockPos.getY() - feet.getY();
               if (descended >= 3) {
                  this.init_RingBuffer_DataUti = RingBuffer.Inner_DataUtil.read;
                  return this.init("In the shaft; capping");
               }

               if (this.init_AiOperation != null) {
                  boolean pending = this.init_AiOperation.requiresReconciliationAfterEmergency();
                  AiOperation.Result r = this.init_AiOperation.tick();
                  if (!r.terminal()) {
                     return this.init("Digging the shaft");
                  }

                  if (RingBuffer.init(r.status(), pending, this.init_AiOperation.requiresReconciliationAfterEmergency())) {
                     return this.init(r, "Shaft digging");
                  }

                  this.init_AiOperation.cancel();
                  this.init_AiOperation = null;
                  return r.status() != AiOperation.Status.SUCCEEDED
                     ? this.init(feet, descended, "a shaft cell would not break")
                     : this.init("Digging the shaft");
               } else {
                  switch (this.init_RingBuffer_StreamU(feet.below())) {
                     case init:
                        this.init_AiOperation = autismclient.ai.a.ConfigStore.init(feet.below(), 20);
                        return this.init("Digging the shaft");
                     case load:
                        if (++this.load_I > 40) {
                           return this.init(feet, descended, "the shaft would not swallow the player");
                        }

                        return this.init("Stepping into the shaft");
                     default:
                        return this.init(feet, descended, "the column below is unsafe");
                  }
               }
            }

            AiOperation.Result init(BlockPos feet, int descended, String why) {
               if (descended == 0 && !this.update_Z) {
                  this.update_Z = true;
                  BlockPos nearby = this.init_BlockPos_2(feet);
                  if (nearby != null) {
                     this.load_BlockPos = nearby;
                     this.init_AiOperation = HashUtil.init_AiOperation_BlockPos(nearby, 0.2, false, false, false, 10);
                     this.init_RingBuffer_DataUti = RingBuffer.Inner_DataUtil.load;
                     return this.init(why + "; moving a few steps to a safe shaft");
                  }
               }

               if (descended < 2 && !towerOk) {
                  return this.init(AiOperation.Status.FAILED, why + "; no cover here");
               } else if (descended < 2) {
                  this.init_RingBuffer_DataUti = RingBuffer.Inner_DataUtil.write;
                  return this.init(why + "; towering instead");
               } else {
                  this.init_RingBuffer_DataUti = RingBuffer.Inner_DataUtil.read;
                  return this.init(why + "; capping the shallow cover");
               }
            }

            AiOperation.Result init_AiOperation_Result_7() {
               boolean pending = this.init_AiOperation.requiresReconciliationAfterEmergency();
               AiOperation.Result r = this.init_AiOperation.tick();
               if (!r.terminal()) {
                  return this.init("Moving to nearby solid ground for the shelter");
               }

               if (RingBuffer.init(r.status(), pending, this.init_AiOperation.requiresReconciliationAfterEmergency())) {
                  return this.init(r, "Shelter approach");
               }

               this.init_AiOperation.cancel();
               this.init_AiOperation = null;
               if (r.status() != AiOperation.Status.SUCCEEDED) {
                  return this.init(r.status(), "Nearby shelter could not be reached: " + r.message());
               }

               this.init_RingBuffer_DataUti = RingBuffer.Inner_DataUtil.init;
               return this.init("On safe ground; digging the shelter");
            }

            AiOperation.Result load_AiOperation_Result(BlockPos feet) {
               boolean beamInFlight = this.init_Z
                  && this.update_BlockPos != null
                  && this.write_BlockPos.getY() == this.update_BlockPos.getY() + 2
                  && !this.write_BlockPos.equals(RingBuffer.init_BlockPos(this.update_BlockPos))
                  && this.load_Z(this.update_BlockPos);
               if (!autismclient.ai.a.AiMotionSafety.fill_Z() && !beamInFlight) {
                  return this.init("Settling before closing the shelter");
               }

               if (this.update_BlockPos == null) {
                  this.update_BlockPos = feet.immutable();
               }

               if (this.init_AiOperation != null) {
                  boolean pending = this.init_AiOperation.requiresReconciliationAfterEmergency();
                  AiOperation.Result r = this.init_AiOperation.tick();
                  if (!r.terminal()) {
                     return this.init("Centering inside the shelter");
                  }

                  if (RingBuffer.init(r.status(), pending, this.init_AiOperation.requiresReconciliationAfterEmergency())) {
                     return this.init(r, "Shelter centering");
                  }

                  this.init_AiOperation.cancel();
                  this.init_AiOperation = null;
                  if (r.status() != AiOperation.Status.SUCCEEDED) {
                     return this.init(r.status(), "Could not settle inside shelter: " + r.message());
                  }
               }

               if (!this.read_Z(this.update_BlockPos)) {
                  if (++this.sync_I > 3) {
                     return this.init(AiOperation.Status.FAILED, "Could not settle into the shelter's own cell");
                  }

                  this.init_AiOperation = HashUtil.init_AiOperation_BlockPos(this.update_BlockPos, 0.15, false, false, false, 5);
                  return this.init("Centering inside the shelter");
               } else {
                  if (this.update_Z(this.update_BlockPos)) {
                     return this.init(AiOperation.Status.FAILED, "A hostile occupies the shelter interior");
                  }

                  if (this.init_List == null) {
                     this.init_List = RingBuffer.init_List_BlockPos_Predicate(this.update_BlockPos, this::init_Z_3);
                     if (this.load_I_5() < this.init_List.size()) {
                        return this.init(AiOperation.Status.FAILED, "Not enough spare blocks to close the shelter", this.init(this.init_List.size()));
                     }

                     for (BlockPos cell : this.init_List) {
                        if (!this.init_Z_3(cell)
                           && (!cu.init(mc.level, cell) || !mc.level.getBlockState(cell).canBeReplaced() || !mc.level.getFluidState(cell).isEmpty())) {
                           return this.init(AiOperation.Status.FAILED, "Shelter boundary cannot be safely filled at " + cell.toShortString());
                        }
                     }
                  }

                  if (!this.init_Z) {
                     while (this.init_I < this.init_List.size() && this.init_Z_3(this.init_List.get(this.init_I))) {
                        this.init_I++;
                     }

                     if (this.init_I >= this.init_List.size()) {
                        if (!this.init_Z_6()) {
                           return this.init(AiOperation.Status.FAILED, "Shelter boundary is still open after construction");
                        }

                        this.write_BlockPos = RingBuffer.init_BlockPos(this.update_BlockPos);
                        this.load_Z = true;
                        this.init_RingBuffer_DataUti = RingBuffer.Inner_DataUtil.sync;
                        return this.init("Shelter sealed; waiting safely until dawn");
                     }

                     int slot = this.init_I_9();
                     if (slot < 0) {
                        if (this.read()) {
                           return this.init("Moving spare shelter blocks to the hotbar");
                        }

                        return this.init(
                           AiOperation.Status.FAILED, "No spare construction block to finish the shelter", this.init(this.init_List.size() - this.init_I)
                        );
                     }

                     this.write_BlockPos = this.init_List.get(this.init_I);
                     autismclient.ai.a.FileCache.init_V(this.write_BlockPos);
                     this.init_Item = mc.player.getInventory().getItem(slot).getItem();
                     this.init_DataPipe.init_V_BlockPos_Item(this.write_BlockPos, this.init_Item);
                     this.init_Z = true;
                  }

                  if (this.write_BlockPos.getY() == this.update_BlockPos.getY() + 2
                     && !this.write_BlockPos.equals(RingBuffer.init_BlockPos(this.update_BlockPos))
                     && !this.init_Z_3(this.write_BlockPos)
                     && this.load_Z(this.update_BlockPos)
                     && !this.init_DataPipe.load_Z()
                     && autismclient.ai.a.AiMotionSafety.fill_Z()
                     && !autismclient.ai.a.DataPipe.init_Z_BlockPos_Item(this.write_BlockPos, this.init_Item)) {
                     mc.options.keyJump.setDown(true);
                     this.sync_Z = true;
                  }

                  autismclient.ai.a.DataPipe.Inner_DataUtil status = this.init_DataPipe.init_DataPipe_DataUtil();
                  if (status == autismclient.ai.a.DataPipe.Inner_DataUtil.init) {
                     return this.init("Closing shelter walls and roof");
                  } else {
                     boolean pending = this.init_DataPipe.load_Z();
                     this.init_DataPipe.init_V();
                     this.init_Z = false;
                     if (status != autismclient.ai.a.DataPipe.Inner_DataUtil.load) {
                        this.flush_Z |= pending;
                        return this.init(AiOperation.Status.FAILED, "Shelter placement failed: " + this.init_DataPipe.init_Str());
                     } else {
                        this.init_I++;
                        return this.init("Shelter boundary confirmed; closing remaining gaps");
                     }
                  }
               }
            }

            AiOperation.Result read_AiOperation_Result(BlockPos feet) {
               if (this.read_BlockPos == null) {
                  BlockPos top = feet.above(4);
                  List<BlockPos> boundary = RingBuffer.init_List_BlockPos_Predicate(top, pos -> pos.equals(top.below()) || this.init_Z_3(pos));
                  int needed = 4 + boundary.size();
                  if (this.load_I_5() < needed) {
                     return this.init(AiOperation.Status.FAILED, "Unsafe column and insufficient spare stock for a closed tower shelter", this.init(needed));
                  }

                  for (BlockPos cell : boundary) {
                     if (!this.init_Z_3(cell)
                        && (!cu.init(mc.level, cell) || !mc.level.getBlockState(cell).canBeReplaced() || !mc.level.getFluidState(cell).isEmpty())) {
                        return this.init(AiOperation.Status.FAILED, "No observed safe boundary for a closed tower shelter at " + cell.toShortString());
                     }
                  }

                  if (this.init_I_9() < 0) {
                     if (this.read()) {
                        return this.init("Moving spare tower blocks to the hotbar");
                     }

                     return this.init(AiOperation.Status.FAILED, "No spare construction stock for the tower shelter", this.init(needed));
                  }

                  this.read_BlockPos = feet;
                  this.init_AiOperation = HashUtil.init_AiOperation_BlockPos(this.read_BlockPos.above(4), 0.5, true, true, false, 15);
               }

               boolean pending = this.init_AiOperation.requiresReconciliationAfterEmergency();
               AiOperation.Result r = this.init_AiOperation.tick();
               if (!r.terminal()) {
                  return this.init("Towering up");
               }

               if (RingBuffer.init(r.status(), pending, this.init_AiOperation.requiresReconciliationAfterEmergency())) {
                  return this.init(r, "Shelter tower");
               }

               this.init_AiOperation.cancel();
               this.init_AiOperation = null;
               if (r.status() != AiOperation.Status.SUCCEEDED) {
                  return this.init(AiOperation.Status.FAILED, "Could not tower up (" + r.message() + ")");
               }

               this.init_RingBuffer_DataUti = RingBuffer.Inner_DataUtil.update;
               return this.init("Tower up; roofing");
            }

            AiOperation.Result write_AiOperation_Result(BlockPos feet) {
               this.read_I++;
               if (this.read_I % 200 == 0) {
                  AutismClientAddon.LOG
                     .info(
                        "[AI] burrow: hiding at {}, health {}, food {}",
                        feet.toShortString(),
                        (int)mc.player.getHealth(),
                        mc.player.getFoodData().getFoodLevel()
                     );
               }

               if (this.load_AiOperation != null) {
                  boolean pending = this.load_AiOperation.requiresReconciliationAfterEmergency();
                  AiOperation.Result r = this.load_AiOperation.tick();
                  if (!r.terminal()) {
                     return this.init("Eating in the sealed shelter to enable regeneration");
                  }

                  if (RingBuffer.init(r.status(), pending, this.load_AiOperation.requiresReconciliationAfterEmergency())) {
                     return this.init(r, "Shelter meal");
                  }

                  this.load_AiOperation.cancel();
                  this.load_AiOperation = null;
                  return this.init("Shelter meal observed; waiting safely");
               } else {
                  if (this.update_I < 3
                     && this.init_J - System.nanoTime() >= 10000000000L
                     && RingBuffer.init(
                        mc.player.getHealth(),
                        mc.player.getMaxHealth(),
                        mc.player.getFoodData().getFoodLevel(),
                        cq.init(mc.player.getInventory()) >= 0,
                        this.init_Z_6(),
                        autismclient.ai.a.AiMotionSafety.fill_Z(),
                        this.canInterruptNow(),
                        this.requiresReconciliationAfterEmergency()
                     )) {
                     this.update_I++;
                     this.load_AiOperation = cq.init(ag.init_JsonObject_ObjArr("food", 20, "timeoutSeconds", 10));
                     return this.init("Eating in the sealed shelter to enable regeneration");
                  }

                  if (healTo > 0.0F && mc.player.getFoodData().getFoodLevel() < 18 && mc.player.getHealth() < healTo && this.read_I - this.flush_I >= 100) {
                     if (stay && this.read_BlockPos == null) {
                        return this.init(
                           AiOperation.Status.SUCCEEDED,
                           "No mob in sight for five seconds (health " + (int)mc.player.getHealth() + ", too hungry to heal); going on from here"
                        );
                     }

                     this.init_RingBuffer_DataUti = this.read_BlockPos == null && !this.load_Z
                        ? RingBuffer.Inner_DataUtil.close
                        : RingBuffer.Inner_DataUtil.flush;
                     return this.init("No mob in sight; leaving cover");
                  } else {
                     if (healTo > 0.0F && this.load_Z_3()) {
                        this.flush_I = this.read_I;
                     }

                     if (healTo > 0.0F && mc.player.getHealth() >= healTo && this.read_I - this.flush_I >= 60) {
                        if (stay && this.read_BlockPos == null) {
                           return this.init(AiOperation.Status.SUCCEEDED, "Healed to " + (int)mc.player.getHealth() + " in the hole; going on from here");
                        }

                        this.init_RingBuffer_DataUti = this.read_BlockPos == null && !this.load_Z
                           ? RingBuffer.Inner_DataUtil.close
                           : RingBuffer.Inner_DataUtil.flush;
                        return this.init("Healed to " + (int)mc.player.getHealth() + "; leaving cover");
                     } else {
                        if (!RingBuffer.init_Z_F_J(healTo, ch.init_J()) && System.nanoTime() <= this.init_J) {
                           return this.init(
                              healTo > 0.0F
                                 ? "Healing in the sealed shelter (" + (int)mc.player.getHealth() + "/" + (int)healTo + ")"
                                 : "Hiding until dawn (day time " + ch.init_J() + ")"
                           );
                        }

                        this.init_RingBuffer_DataUti = this.read_BlockPos == null && !this.load_Z
                           ? RingBuffer.Inner_DataUtil.close
                           : RingBuffer.Inner_DataUtil.flush;
                        return this.init(healTo > 0.0F ? "Shelter healing budget ended; leaving cover" : "Dawn; leaving cover");
                     }
                  }
               }
            }

            AiOperation.Result update_AiOperation_Result(BlockPos feet) {
               if (this.read_BlockPos == null) {
                  if (this.init_AiOperation == null) {
                     this.init_AiOperation = autismclient.ai.a.ConfigStore.init(this.write_BlockPos, 20);
                  }

                  boolean pending = this.init_AiOperation.requiresReconciliationAfterEmergency();
                  AiOperation.Result r = this.init_AiOperation.tick();
                  if (!r.terminal()) {
                     return this.init("Breaking the cap");
                  }

                  if (RingBuffer.init(r.status(), pending, this.init_AiOperation.requiresReconciliationAfterEmergency())) {
                     return this.init(r, "Shelter opening");
                  }

                  this.init_AiOperation.cancel();
                  this.init_AiOperation = null;
                  if (r.status() != AiOperation.Status.SUCCEEDED) {
                     return this.init(r.status(), "Could not open the shelter cap: " + r.message());
                  }

                  this.init_RingBuffer_DataUti = RingBuffer.Inner_DataUtil.close;
                  return this.init("Cap open; climbing out");
               } else {
                  if (RingBuffer.init(feet.getY(), this.read_BlockPos.getY(), autismclient.ai.a.AiMotionSafety.fill_Z())) {
                     return this.init(AiOperation.Status.SUCCEEDED, "Down from the tower; the night is over");
                  }

                  if (!autismclient.ai.a.AiMotionSafety.fill_Z()) {
                     return this.init("Settling onto the ground while descending the shelter");
                  }

                  if (mc.level.getBlockState(feet.below()).isAir()) {
                     if (!mc.player.onGround()) {
                        return this.init("Stepping down the tower");
                     } else if (++this.write_I > 40) {
                        this.init_V_8();
                        return this.init(AiOperation.Status.FAILED, "Could not center over the shelter column for a safe descent");
                     } else {
                        double dx = this.read_BlockPos.getX() + 0.5 - mc.player.getX();
                        double dz = this.read_BlockPos.getZ() + 0.5 - mc.player.getZ();
                        double len = Math.hypot(dx, dz);
                        double yaw = Math.toRadians(mc.player.getYRot());
                        double f = len < 0.001 ? 0.0 : (-dx * Math.sin(yaw) + dz * Math.cos(yaw)) / len;
                        double l = len < 0.001 ? 0.0 : (dx * Math.cos(yaw) + dz * Math.sin(yaw)) / len;
                        mc.options.keyUp.setDown(len > 0.2 && f > 0.38);
                        mc.options.keyDown.setDown(len > 0.2 && f < -0.38);
                        mc.options.keyLeft.setDown(len > 0.2 && l > 0.38);
                        mc.options.keyRight.setDown(len > 0.2 && l < -0.38);
                        return this.init("Stepping back over the tower");
                     }
                  } else {
                     if (this.write_I > 0) {
                        mc.options.keyUp.setDown(false);
                        mc.options.keyDown.setDown(false);
                        mc.options.keyLeft.setDown(false);
                        mc.options.keyRight.setDown(false);
                     }

                     this.write_I = 0;
                     if (this.init_AiOperation == null) {
                        this.init_AiOperation = autismclient.ai.a.ConfigStore.init(feet.below(), 20);
                     }

                     boolean pending = this.init_AiOperation.requiresReconciliationAfterEmergency();
                     AiOperation.Result r = this.init_AiOperation.tick();
                     if (!r.terminal()) {
                        return this.init("Digging down the tower");
                     }

                     if (RingBuffer.init(r.status(), pending, this.init_AiOperation.requiresReconciliationAfterEmergency())) {
                        return this.init(r, "Shelter descent");
                     }

                     this.init_AiOperation.cancel();
                     this.init_AiOperation = null;
                     return r.status() == AiOperation.Status.SUCCEEDED
                        ? this.init("Digging down the tower")
                        : this.init(r.status(), "Could not safely descend the shelter tower: " + r.message());
                  }
               }
            }

            AiOperation.Result load_AiOperation_Result_6() {
               if (this.init_AiOperation == null) {
                  this.init_AiOperation = HashUtil.init_AiOperation_BlockPos(this.init_BlockPos, 1.2, true, true, false, 60);
               }

               boolean pending = this.init_AiOperation.requiresReconciliationAfterEmergency();
               AiOperation.Result r = this.init_AiOperation.tick();
               if (!r.terminal()) {
                  return this.init("Climbing out");
               } else if (RingBuffer.init(r.status(), pending, this.init_AiOperation.requiresReconciliationAfterEmergency())) {
                  return this.init(r, "Shelter exit");
               } else {
                  this.init_AiOperation.cancel();
                  this.init_AiOperation = null;
                  if (r.status() == AiOperation.Status.SUCCEEDED && !autismclient.ai.a.AiMotionSafety.fill_Z()) {
                     return this.init("Settling on the surface before handing back control");
                  } else if (r.status() == AiOperation.Status.SUCCEEDED && this.write_Z) {
                     return this.init(
                        AiOperation.Status.FAILED, "Water reached the burrow; back on the surface, still night - use game:sleep or shelter elsewhere"
                     );
                  } else {
                     return r.status() == AiOperation.Status.SUCCEEDED
                        ? this.init(AiOperation.Status.SUCCEEDED, "Back on the surface; the night is over")
                        : this.init(AiOperation.Status.FAILED, "Burrowed but could not climb out (" + r.message() + "); dig up manually");
                  }
               }
            }

            boolean init_Z_3(BlockPos pos) {
               return cu.init(mc.level, pos) && mc.level.getFluidState(pos).isEmpty() && mc.level.getBlockState(pos).isCollisionShapeFullBlock(mc.level, pos);
            }

            boolean load_Z(BlockPos feet) {
               for (Direction side : RingBuffer.init_List) {
                  if (!this.init_Z_3(feet.relative(side)) || !this.init_Z_3(feet.above().relative(side))) {
                     return false;
                  }
               }

               return true;
            }

            boolean read_Z(BlockPos feet) {
               return RingBuffer.init(feet, mc.player.getBoundingBox(), mc.player.getY());
            }

            boolean init_Z_6() {
               return this.update_BlockPos != null
                     && this.read_Z(this.update_BlockPos)
                     && RingBuffer.init_Z_BlockPos_Predicate(this.update_BlockPos, this::init_Z_3)
                  ? this.write_Z(this.update_BlockPos)
                  : false;
            }

            boolean write_Z(BlockPos feet) {
               for (BlockPos inside : List.of(feet, feet.above())) {
                  if (!cu.init(mc.level, inside)
                     || !mc.level.getFluidState(inside).isEmpty()
                     || !mc.level.getBlockState(inside).getCollisionShape(mc.level, inside).isEmpty()) {
                     return false;
                  }
               }

               return true;
            }

            boolean update_Z(BlockPos feet) {
               AABB room = new AABB(feet.getX() + 0.001, feet.getY(), feet.getZ() + 0.001, feet.getX() + 0.999, feet.getY() + 2, feet.getZ() + 0.999);

               for (Entity entity : mc.level.entitiesForRendering()) {
                  if (entity instanceof Enemy && entity.isAlive() && entity.getBoundingBox().intersects(room)) {
                     return true;
                  }
               }

               return false;
            }

            BlockPos init_BlockPos_2(BlockPos feet) {
               return RingBuffer.init(
                  feet,
                  pos -> {
                     if (this.init_Z_3(pos.below()) && !this.init(pos, 3.0)) {
                        for (BlockPos inside : List.of(pos, pos.above())) {
                           if (!cu.init(mc.level, inside)
                              || !mc.level.getFluidState(inside).isEmpty()
                              || !mc.level.getBlockState(inside).getCollisionShape(mc.level, inside).isEmpty()) {
                              return false;
                           }
                        }

                        return true;
                     } else {
                        return false;
                     }
                  },
                  pos -> {
                     for (int down = 1; down <= 3; down++) {
                        if (!this.init_Z_3(pos.below(down)) || this.init_RingBuffer_StreamU(pos.below(down)) != RingBuffer.Inner_StreamUtil.init) {
                           return false;
                        }
                     }

                     BlockPos floor = pos.below(3);
                     return this.init_Z_3(floor.below()) && this.load_Z(floor);
                  }
               );
            }

            RingBuffer.Inner_StreamUtil init_RingBuffer_StreamU(BlockPos pos) {
               if (!cu.init(mc.level, pos)) {
                  return RingBuffer.Inner_StreamUtil.read;
               }

               BlockState state = mc.level.getBlockState(pos);
               boolean fluid = !mc.level.getFluidState(pos).isEmpty() || state.is(Blocks.ICE);

               for (Direction side : Plane.HORIZONTAL) {
                  if (!mc.level.getFluidState(pos.relative(side)).isEmpty()) {
                     fluid = true;
                  }
               }

               boolean unbreakable = state.getDestroySpeed(mc.level, pos) < 0.0F;
               boolean open = state.getCollisionShape(mc.level, pos).isEmpty();
               boolean safeFloor = ValueSink.init_Z_Minecraft_BlockPos(mc, pos.below()) && !this.init(pos, 3.0);

               for (Direction side : Plane.HORIZONTAL) {
                  BlockPos wall = pos.relative(side);
                  if (!this.init_Z_3(wall)) {
                     return RingBuffer.Inner_StreamUtil.read;
                  }
               }

               return open && healTo > 0.0F ? RingBuffer.Inner_StreamUtil.read : RingBuffer.init(true, fluid, unbreakable, open, safeFloor);
            }

            boolean load_Z_3() {
               for (Entity e : mc.level.entitiesForRendering()) {
                  if (e instanceof Enemy && e.isAlive() && e.distanceToSqr(mc.player) < 256.0 && mc.player.hasLineOfSight(e)) {
                     return true;
                  }
               }

               return false;
            }

            boolean init(BlockPos pos, double radius) {
               for (Entity e : mc.level.entitiesForRendering()) {
                  if (e instanceof Enemy && !(e instanceof Enderman) && e.isAlive() && e.distanceToSqr(Vec3.atCenterOf(pos)) < radius * radius) {
                     return true;
                  }
               }

               return false;
            }

            boolean init(ItemStack stack) {
               return stack.getItem() instanceof BlockItem block
                  && autismclient.ai.a.UtilCore.init_Z(BuiltInRegistries.ITEM.getKey(stack.getItem()).toString())
                  && !(block.getBlock() instanceof FallingBlock)
                  && block.getBlock().defaultBlockState().isCollisionShapeFullBlock(mc.level, mc.player.blockPosition());
            }

            int init_I_9() {
               for (int slot = 0; slot < 9; slot++) {
                  ItemStack stack = mc.player.getInventory().getItem(slot);
                  if (!stack.isEmpty() && this.init(stack) && autismclient.ai.a.UtilCore.init_Z_ItemStack(stack, mc.player.getInventory(), true)) {
                     return slot;
                  }
               }

               return -1;
            }

            int load_I_5() {
               Map<Item, Integer> counts = new HashMap<>();

               for (int slot = 0; slot < 36; slot++) {
                  ItemStack stack = mc.player.getInventory().getItem(slot);
                  if (!stack.isEmpty() && this.init(stack)) {
                     counts.merge(stack.getItem(), stack.getCount(), Integer::sum);
                  }
               }

               int count = 0;
               int stone = 0;
               int planks = 0;

               for (Entry<Item, Integer> entry : counts.entrySet()) {
                  int spare = Math.max(0, entry.getValue() - autismclient.ai.a.UtilCore.init(entry.getKey()));
                  ItemStack stack = entry.getKey().getDefaultInstance();
                  if (autismclient.ai.a.UtilCore.init(stack)) {
                     stone += spare;
                  } else if (stack.is(ItemTags.PLANKS)) {
                     planks += spare;
                  } else {
                     count += spare;
                  }
               }

               return count + Math.max(0, stone - autismclient.ai.a.UtilCore.flush()) + Math.max(0, planks - autismclient.ai.a.UtilCore.sync());
            }

            boolean read() {
               if (mc.player.containerMenu == mc.player.inventoryMenu && mc.player.containerMenu.getCarried().isEmpty()) {
                  int source = -1;
                  int rank = 2147483647;

                  for (int slot = 9; slot < 36; slot++) {
                     ItemStack stack = mc.player.getInventory().getItem(slot);
                     if (!stack.isEmpty() && this.init(stack) && autismclient.ai.a.UtilCore.init_Z_ItemStack(stack, mc.player.getInventory(), true)) {
                        int candidate = autismclient.ai.a.UtilCore.init_I(BuiltInRegistries.ITEM.getKey(stack.getItem()).toString());
                        if (candidate < rank) {
                           source = slot;
                           rank = candidate;
                        }
                     }
                  }

                  if (source < 0) {
                     return false;
                  }

                  this.init_UtilCore.init_V_I(source, af.load(), BuiltInRegistries.ITEM.getKey(mc.player.getInventory().getItem(source).getItem()).toString());
                  return true;
               } else {
                  return false;
               }
            }

            JsonObject init(int needed) {
               JsonObject missing = new JsonObject();
               missing.addProperty("minecraft:dirt", Math.max(1, needed - this.load_I_5()));
               return ag.init_JsonObject_ObjArr("missing", missing, "constructionRequired", needed, "constructionAvailable", this.load_I_5());
            }

            JsonObject init_JsonObject_15() {
               JsonObject data = ag.init_JsonObject_ObjArr(
                  "slept",
                  false,
                  "hidTicks",
                  this.read_I,
                  "capPlaced",
                  this.load_Z,
                  "sealed",
                  mc.player != null && mc.level == this.init_Obj && this.init_Z_6(),
                  "mode",
                  this.read_BlockPos != null ? "tower" : "shaft",
                  "phase",
                  this.init_RingBuffer_DataUti.name(),
                  "dayTime",
                  ch.init_J(),
                  "meals",
                  this.update_I,
                  "coverBlocksConfirmed",
                  this.init_I,
                  "restocks",
                  this.init_UtilCore.close(),
                  "pendingChangesNeedObservation",
                  this.requiresReconciliationAfterEmergency()
               );
               if (!this.init_JsonObject.isEmpty()) {
                  data.add("pendingChild", this.init_JsonObject);
               }

               return data;
            }

            AiOperation.Result init(String message) {
               return R.init_AiOperation_Result(AiOperation.Status.RUNNING, message, this.init_JsonObject_15());
            }

            AiOperation.Result init(AiOperation.Status status, String message) {
               return this.init(status, message, new JsonObject());
            }

            AiOperation.Result init(AiOperation.Status status, String message, JsonObject extra) {
               boolean pending = this.requiresReconciliationAfterEmergency();
               JsonObject data = this.init_JsonObject_15();

               for (Entry<String, JsonElement> entry : extra.entrySet()) {
                  data.add(entry.getKey(), entry.getValue());
               }

               this.cancel();
               return Y.init_AiOperation_Result_Z_AiOperation_Result(pending, R.init_AiOperation_Result(status, message, data));
            }

            AiOperation.Result init(AiOperation.Result result, String action) {
               this.flush_Z = true;
               this.init_JsonObject = ag.init_JsonObject_ObjArr(
                  "status", result.status().name(), "message", result.message(), "failureCode", result.failure().name()
               );
               this.init_JsonObject.add("result", bK.init_JsonObject(result.dataJson()));
               JsonObject data = this.init_JsonObject_15();
               this.cancel();
               return Y.init_AiOperation_Result_Z_AiOperation_Result(
                  true, new AiOperation.Result(result.status(), action + ": " + result.message(), data.toString(), result.evidence(), result.failure())
               );
            }

            void init_V_8() {
               for (KeyMapping key : List.of(mc.options.keyUp, mc.options.keyDown, mc.options.keyLeft, mc.options.keyRight)) {
                  key.setDown(autismclient.util.KeyMappingControl.init(key).autism$physicalForBody());
               }
            }

            @Override
            public boolean canInterruptNow() {
               return !this.flush_Z
                  && autismclient.ai.a.AiMotionSafety.fill_Z()
                  && !this.sync_Z
                  && !RingBuffer.init_Z_Z_BooleanSupplier(this.init_Z, this.init_DataPipe::load_Z)
                  && !this.init_UtilCore.load_Z()
                  && (this.init_AiOperation == null || this.init_AiOperation.canInterruptNow())
                  && (this.load_AiOperation == null || this.load_AiOperation.canInterruptNow());
            }

            @Override
            public boolean requiresReconciliationAfterEmergency() {
               return this.flush_Z
                  || RingBuffer.init_Z_Z_BooleanSupplier(this.init_Z, this.init_DataPipe::load_Z)
                  || this.init_UtilCore.load_Z()
                  || this.init_AiOperation != null && this.init_AiOperation.requiresReconciliationAfterEmergency()
                  || this.load_AiOperation != null && this.load_AiOperation.requiresReconciliationAfterEmergency();
            }

            @Override
            public void cancel() {
               if (!this.read_Z) {
                  this.flush_Z = this.flush_Z | this.requiresReconciliationAfterEmergency();
                  this.read_Z = true;
                  if (this.init_AiOperation != null) {
                     this.init_AiOperation.cancel();
                     this.init_AiOperation = null;
                  }

                  if (this.load_AiOperation != null) {
                     this.load_AiOperation.cancel();
                     this.load_AiOperation = null;
                  }

                  this.init_DataPipe.init_V();
                  this.init_UtilCore.init_V();
                  if (this.sync_Z) {
                     mc.options.keyJump.setDown(autismclient.util.KeyMappingControl.init(mc.options.keyJump).autism$physicalForBody());
                     this.sync_Z = false;
                  }

                  if (this.write_I > 0) {
                     this.init_V_8();
                  }
               }
            }
         };
      } else {
         throw new IllegalArgumentException("Burrowing requires a connected living player");
      }
   }

   private enum Inner_DataUtil {
      init,
      load,
      read,
      write,
      update,
      sync,
      flush,
      close;
   }

   enum Inner_StreamUtil {
      init,
      load,
      read;
   }
}

package autismclient.ai;

import autismclient.AutismClientAddon;
import autismclient.ai.a.AiGoto;
import autismclient.api.ai.AiOperation;
import autismclient.compat263.Compat263;
import autismclient.mixin.accessor.AutismClientLevelPredictionAccessor;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BooleanSupplier;
import java.util.function.Supplier;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.prediction.BlockStatePredictionHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.FallingBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

final class AiDimension {
   private static final int write = 400;
   private static final int update = 40;
   private static final int sync = 48;
   static final int[] init_IArr = new int[]{1, 0, 2, -1};
   static volatile BlockPos init_BlockPos;
   static volatile BlockPos load_BlockPos;
   static volatile Direction init_Direction;
   static volatile String init_Str = "";
   static volatile String load_Str = "";
   static volatile Object init_Obj;
   static final Set<Long> init_Set = ConcurrentHashMap.newKeySet();
   static final int init_I = 20;
   static final int load_I = 3;
   private static final int flush = 120;
   private static final int close = 40;
   static final int read = 10;
   static final Set<String> load_Set = Set.of("minecraft:golden_helmet", "minecraft:golden_chestplate", "minecraft:golden_leggings", "minecraft:golden_boots");
   private static final Map<String, Block> init_Map = new LinkedHashMap<>();

   private AiDimension() {
   }

   static void init_V() {
      Minecraft mc = Minecraft.getInstance();
      if (!init_Str.equals(AiWorldIdentity.init(mc)) || !load_Str.equals(init_Str()) || init_Obj != mc.getConnection()) {
         init_BlockPos = null;
         init_Direction = null;
         load_BlockPos = null;
         init_Set.clear();
      }
   }

   static Set<Long> init_Set(BlockPos origin, Direction axis) {
      Set<Long> cells = new HashSet<>();
      if (origin != null && axis != null) {
         for (BlockPos cell : init_List(origin, axis)) {
            cells.add(cell.asLong());
         }

         for (BlockPos cell : write(origin, axis)) {
            cells.add(cell.asLong());
         }

         return cells;
      } else {
         return cells;
      }
   }

   static int init_I() {
      init_V();
      Minecraft mc = Minecraft.getInstance();
      BlockPos origin = init_BlockPos;
      Direction axis = init_Direction;
      if (mc.level != null && origin != null && axis != null && init_Str.equals(AiWorldIdentity.init(mc)) && load_Str.equals(init_Str()) && AiTerrain.init(mc.level, origin)) {
         if (mc.level.getBlockState(origin).is(Blocks.NETHER_PORTAL)) {
            return 0;
         }

         int n = 0;

         for (BlockPos cell : init_List(origin, axis)) {
            if (mc.level.getBlockState(cell).is(Blocks.OBSIDIAN)) {
               n++;
            }
         }

         return n;
      } else {
         return 0;
      }
   }

   static boolean init(List<String> armorItemIds) {
      for (String id : armorItemIds) {
         if (load_Set.contains(id)) {
            return true;
         }
      }

      return false;
   }

   static AiOperation init(JsonObject args) {
      final Minecraft mc = Minecraft.getInstance();
      if (mc.player != null && mc.level != null) {
         final AiDimension.Inner_DataUtil action = init_n_DataUtil(AiJson.init_Str_JsonObject(args, "action", "enter"));
         init_V();
         final String target = init_Str(AiJson.init_Str_JsonObject(args, "target", "minecraft:the_nether"));
         final boolean build = AiJson.init_Z_JsonObject(args, "build", false);
         final int timeoutSeconds = Math.max(10, Math.min(1800, AiJson.init_I_JsonObject(args, "timeoutSeconds", 300)));
         final Block portalBlock = init_Map.get(target);
         if (portalBlock == null) {
            throw new IllegalArgumentException("Unknown dimension '" + target + "'; known: " + String.join(", ", init_Map.keySet()));
         } else {
            return new AiOperation() {
               final long init_J = System.nanoTime() + timeoutSeconds * 1000000000L;
               final String init_Str = AiDimension.init_Str();
               final String load_Str = AiWorldIdentity.init(mc);
               final Object init_Obj = mc.getConnection();
               final Object load_Obj = mc.level;
               final Vec3 init_Vec3 = mc.player.position();
               final autismclient.ai.a.AiAim init_DataUtil = new autismclient.ai.a.AiAim();
               final autismclient.ai.a.AiPlacer init_DataPipe = new autismclient.ai.a.AiPlacer();
               final AiDimension.Inner_StreamUtil init_n_StreamUtil = new AiDimension.Inner_StreamUtil();
               final AiDimension.Inner_CachePool init_n_CachePool = new AiDimension.Inner_CachePool();
               final List<BlockPos> init_List = new ArrayList<>();
               AiDimension.Inner_BufferHelper init_n_BufferHelper;
               Block init_Block;
               JsonObject init_JsonObject;
               BlockPos init_BlockPos;
               BlockState init_BlockState;
               AiOperation init_AiOperation;
               BlockPos load_BlockPos;
               JsonObject load_JsonObject = new JsonObject();
               BlockPos read_BlockPos;
               final Set<Long> init_Set;
               Direction init_Direction;
               int init_I;
               int load_I;
               int read_I;
               int write_I;
               int update_I;
               boolean init_Z;
               BlockPos write_BlockPos;
               final autismclient.ai.a.AiAim load_DataUtil;
               int sync_I;
               int flush_I;
               int close_I;
               boolean load_Z;
               boolean read_Z;
               boolean write_Z;
               final ArrayDeque<BlockPos> init_ArrayDeque;
               AiOperation load_AiOperation;
               BlockPos update_BlockPos;
               Direction load_Direction;
               boolean update_Z;
               boolean sync_Z;
               int open;
               boolean flush_Z;
               final Set<BlockPos> load_Set;
               AiOperation read_AiOperation;
               boolean close_Z;
               final Set<BlockPos> read_Set;
               BlockPos sync_BlockPos;
               final Set<BlockPos> write_Set;
               int poll;
               int push;
               int pop;
               int tick;
               int run;
               int get;

               {
                  this.init_Set = AiDimension.init_Set;
                  this.init_Direction = Direction.EAST;
                  this.load_DataUtil = new autismclient.ai.a.AiAim();
                  this.init_ArrayDeque = new ArrayDeque<>();
                  this.load_Set = new HashSet<>();
                  this.read_Set = new HashSet<>();
                  this.write_Set = new HashSet<>();
               }

               BlockPos init_BlockPos_8(BlockPos portal) {
                  BlockPos bottom = portal;

                  while (mc.level.getBlockState(bottom.below()).getBlock() == mc.level.getBlockState(portal).getBlock()) {
                     bottom = bottom.below();
                  }

                  BlockState state = mc.level.getBlockState(bottom);
                  boolean alongZ = state.hasProperty(BlockStateProperties.HORIZONTAL_AXIS) && state.getValue(BlockStateProperties.HORIZONTAL_AXIS) == Axis.Z;
                  BlockPos best = null;
                  double bestDistance = 1.7976931348623157E308;

                  for (int side : new int[]{-1, 1}) {
                     BlockPos stand = alongZ ? bottom.offset(side, 0, 0) : bottom.offset(0, 0, side);
                     if (this.init_Z_9(stand)
                        && this.init_Z_9(stand.above())
                        && !mc.level.getBlockState(stand.below()).getCollisionShape(mc.level, stand.below()).isEmpty()) {
                        double distance = stand.distSqr(mc.player.blockPosition());
                        if (distance < bestDistance) {
                           bestDistance = distance;
                           best = stand;
                        }
                     }
                  }

                  return best;
               }

               AiOperation init(BlockPos targetCell, double radius, int seconds) {
                  HashSet<Long> keep = new HashSet<>();

                  for (BlockPos cell : BlockPos.betweenClosed(this.load_BlockPos.offset(-3, -5, -3), this.load_BlockPos.offset(3, 5, 3))) {
                     Block block = mc.level.getBlockState(cell).getBlock();
                     if (block == portalBlock || block == Blocks.OBSIDIAN || block == Blocks.END_PORTAL_FRAME) {
                        keep.add(cell.asLong());
                     }
                  }

                  return AiRunDiagnostics.init_AiOperation_Str(
                     "dimension",
                     "approach portal " + this.load_BlockPos.toShortString(),
                     AiGoto.init(
                        targetCell,
                        radius,
                        true,
                        true,
                        true,
                        seconds,
                        autismclient.ai.a.AiPathfinder.Inner_IoWorker.load,
                        true,
                        2.0,
                        AiGoto.read(autismclient.ai.a.AiPathfinder.Inner_JsonUtil.init_ClassCache_JsonUti_BlockPos_D(targetCell, radius), keep)
                     )
                  );
               }

               @Override
               public AiOperation.Result tick() {
                  if (!this.write_Z && mc.player != null && mc.level != null) {
                     if (!this.load_Str.equals(AiWorldIdentity.init(mc)) || this.init_Obj != mc.getConnection()) {
                        return this.init(AiOperation.Status.CANCELLED, "World or connection identity changed during portal travel");
                     }

                     if (!mc.player.isAlive()) {
                        return this.init(AiOperation.Status.FAILED, "Player died during portal travel");
                     }

                     if (System.nanoTime() > this.init_J) {
                        return this.init(AiOperation.Status.TIMED_OUT, "Still in " + AiDimension.init_Str() + " at the deadline");
                     }

                     if (this.init_n_StreamUtil.init_Z()) {
                        return this.init(AiOperation.Status.UNCERTAIN, "Portal work has pending changes that need observation");
                     }

                     if (action == AiDimension.Inner_DataUtil.load) {
                        return this.init(AiOperation.Status.SUCCEEDED, "Read portals in loaded terrain");
                     }

                     if (!AiDimension.init_Str().equals(this.init_Str)) {
                        if (this.write_I > 0) {
                           this.write_I = 0;
                           mc.options.keyUp.setDown(false);
                           mc.options.keyJump.setDown(false);
                           this.load_DataUtil.init_V();
                        }

                        if (++this.flush_I < 40) {
                           return this.init("Arrived in " + AiDimension.init_Str() + "; waiting for the world");
                        } else {
                           String hazard = this.init_Str_19();
                           AiPortalLinks.init(this.init_Str, this.load_BlockPos, AiDimension.init_Str(), mc.player.blockPosition(), hazard == null, this.load_JsonObject);
                           if (!AiDimension.init_Str().equals(target)) {
                              return this.init(AiOperation.Status.UNCERTAIN, "Portal led to " + AiDimension.init_Str() + " rather than " + target);
                           } else {
                              return hazard != null
                                 ? this.init(AiOperation.Status.UNCERTAIN, "Arrived in " + target + " but " + hazard, this.init_JsonObject_59())
                                 : this.init(AiOperation.Status.SUCCEEDED, "Arrived in " + target, this.init_JsonObject_59());
                           }
                        }
                     } else {
                        this.flush_I = 0;
                        if (AiDimension.init_Str().equals(target)) {
                           return this.init(AiOperation.Status.SUCCEEDED, "Already in " + target);
                        }

                        if (this.load_AiOperation != null || !this.init_ArrayDeque.isEmpty()) {
                           AiOperation.Result dug = this.read_AiOperation_Result_13();
                           if (dug != null) {
                              return dug;
                           }
                        }

                        if (this.load_Z) {
                           AiOperation.Result result = this.load_AiOperation_Result_18();
                           if (result != null) {
                              return result;
                           }
                        }

                        if ((this.load_BlockPos == null || mc.level.getBlockState(this.load_BlockPos).getBlock() != portalBlock) && this.sync_I-- <= 0) {
                           this.load_BlockPos = this.init(portalBlock);
                           this.sync_I = 40;
                        }

                        if (this.load_BlockPos == null && this.sync_I < 40) {
                           return this.init("Looking for a portal");
                        }

                        if (this.load_BlockPos != null) {
                           BlockPos bottom = this.load_BlockPos;

                           while (mc.level.getBlockState(bottom.below()).getBlock() == portalBlock) {
                              bottom = bottom.below();
                           }

                           if (mc.player.blockPosition().distSqr(bottom) > 2.0) {
                              if (this.init_AiOperation == null) {
                                 BlockPos feetNow = mc.player.blockPosition();
                                 this.update_I = feetNow.equals(this.write_BlockPos) ? this.update_I + 1 : 0;
                                 this.write_BlockPos = feetNow;
                                 BlockPos stance = this.update_I >= 4 ? this.init_BlockPos_8(this.load_BlockPos) : null;
                                 if (stance != null) {
                                    AutismClientAddon.LOG
                                       .info(
                                          "[AI] dimension: the walk into the portal restarted {} times from {}; to the stance {}",
                                          this.update_I,
                                          feetNow.toShortString(),
                                          stance.toShortString()
                                       );
                                    this.update_I = 0;
                                    this.init_AiOperation = this.init(stance, 0.5, Math.min(60, this.init_I_37()));
                                 } else {
                                    this.init_AiOperation = this.init(bottom, 1.6, Math.min(180, this.init_I_37()));
                                 }
                              }

                              AiOperation.Result legResult = this.init_n_StreamUtil.init_AiOperation_Result_AiOperation(this.init_AiOperation);
                              if (!legResult.terminal()) {
                                 return this.init("Walking into the portal");
                              }

                              this.init_n_StreamUtil.init_V_AiOperation(this.init_AiOperation);
                              this.init_AiOperation = null;
                              if (legResult.status() == AiOperation.Status.UNCERTAIN || legResult.status() == AiOperation.Status.CANCELLED) {
                                 return this.init(legResult.status(), "Portal approach interrupted: " + legResult.message());
                              }

                              if (legResult.status() != AiOperation.Status.SUCCEEDED) {
                                 BlockPos stance = this.init_Z ? null : this.init_BlockPos_8(this.load_BlockPos);
                                 this.init_Z = true;
                                 if (stance != null) {
                                    AutismClientAddon.LOG
                                       .info(
                                          "[AI] dimension: no route into the portal from {}; to the stance {}",
                                          mc.player.blockPosition().toShortString(),
                                          stance.toShortString()
                                       );
                                    this.init_AiOperation = this.init(stance, 0.5, Math.min(60, this.init_I_37()));
                                    return this.init("To a stance by the portal");
                                 } else {
                                    return this.init(AiOperation.Status.FAILED, "No route into the portal: " + legResult.message());
                                 }
                              } else {
                                 return this.init("At the portal");
                              }
                           } else {
                              if (this.init_AiOperation != null) {
                                 this.init_n_StreamUtil.init_V_AiOperation(this.init_AiOperation);
                                 this.init_AiOperation = null;
                                 if (this.init_n_StreamUtil.init_Z()) {
                                    return this.init(AiOperation.Status.UNCERTAIN, "Portal approach needs observation before stepping in");
                                 }
                              }

                              BlockPos feet = mc.player.blockPosition();
                              boolean inside = mc.level.getBlockState(feet).getBlock() == portalBlock
                                 || mc.level.getBlockState(feet.above()).getBlock() == portalBlock;
                              if (inside && this.load_JsonObject.isEmpty()) {
                                 this.load_JsonObject = AiPortalLinks.init(this.load_BlockPos);
                              }

                              if (!inside) {
                                 if (++this.write_I > 60) {
                                    this.write_I = 0;
                                    mc.options.keyUp.setDown(false);
                                    mc.options.keyJump.setDown(false);
                                    this.load_BlockPos = null;
                                    return this.init("Lost the portal's plane; looking again");
                                 } else {
                                    this.load_DataUtil.init_V_Vec3(Vec3.atCenterOf(bottom));
                                    this.load_DataUtil.load_V();
                                    mc.options.keyUp.setDown(true);
                                    mc.options.keyJump.setDown(mc.player.horizontalCollision && mc.player.onGround());
                                    this.read_I = 0;
                                    return this.init("Stepping into the portal");
                                 }
                              } else {
                                 if (this.write_I > 0) {
                                    this.write_I = 0;
                                    mc.options.keyUp.setDown(false);
                                    mc.options.keyJump.setDown(false);
                                    this.load_DataUtil.init_V();
                                 }

                                 if (this.read_I > 200 && this.read_I % 20 < 2 && mc.player.onGround()) {
                                    mc.options.keyJump.setDown(true);
                                    return this.init("In the portal, waiting for the transfer (" + this.read_I / 20 + "s); hopping through the plane");
                                 } else {
                                    mc.options.keyJump.setDown(false);
                                    if (++this.read_I > 400) {
                                       BlockState planeState = mc.level.getBlockState(feet);
                                       AutismClientAddon.LOG
                                          .info(
                                             "[AI] dimension: stood in the portal without transferring at {} (blockAtFeet {}, above {}, below {}, portalBlock {}, axis {}, playerY {}, bottom {}, distance {})",
                                             feet.toShortString(),
                                             planeState.getBlock(),
                                             mc.level.getBlockState(feet.above()).getBlock(),
                                             mc.level.getBlockState(feet.below()).getBlock(),
                                             portalBlock,
                                             planeState.hasProperty(BlockStateProperties.HORIZONTAL_AXIS)
                                                ? planeState.getValue(BlockStateProperties.HORIZONTAL_AXIS)
                                                : "?",
                                             mc.player.getY(),
                                             bottom.toShortString(),
                                             (int)Math.sqrt(mc.player.blockPosition().distSqr(bottom))
                                          );
                                       return this.init(AiOperation.Status.FAILED, "Stood in the portal for 20s without transferring");
                                    } else {
                                       return this.init("In the portal, waiting for the transfer (" + this.read_I / 20 + "s)");
                                    }
                                 }
                              }
                           }
                        } else {
                           if (!build) {
                              return this.init(
                                 AiOperation.Status.FAILED,
                                 "No " + BuiltInRegistries.BLOCK.getKey(portalBlock) + " within 48 blocks; set build:true to construct one"
                              );
                           }

                           if (portalBlock != Blocks.NETHER_PORTAL) {
                              return this.init(AiOperation.Status.FAILED, "Only nether portals can be constructed; an end portal must be found");
                           }

                           AiOperation.Result prepared = this.init_AiOperation_Result_41();
                           return prepared != null ? prepared : this.init("Building a nether portal");
                        }
                     }
                  } else {
                     return this.init(AiOperation.Status.CANCELLED, "Dimension travel cancelled");
                  }
               }

               String init_Str_19() {
                  return AiPortalLinks.init_Str();
               }

               JsonObject init_JsonObject_59() {
                  if (!"minecraft:the_nether".equals(AiDimension.init_Str())) {
                     return null;
                  }

                  List<String> armor = new ArrayList<>();

                  for (EquipmentSlot slot : List.of(EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET)) {
                     ItemStack stack = mc.player.getItemBySlot(slot);
                     if (!stack.isEmpty()) {
                        armor.add(BuiltInRegistries.ITEM.getKey(stack.getItem()).toString());
                     }
                  }

                  return AiDimension.init(armor) ? null : AiJson.init_JsonObject_ObjArr("warning", "No gold armor equipped; piglins attack on sight in the nether");
               }

               AiOperation.Result init_AiOperation_Result_41() {
                  int obsidian = AiGameState.init_I_Str("minecraft:obsidian");
                  BlockPos reused = this.init_BlockPos_18();
                  int needed = reused == null ? 10 : 10 - this.init_I(reused, this.init_Direction);
                  if (obsidian < needed) {
                     JsonObject missing = new JsonObject();
                     missing.addProperty("minecraft:obsidian", needed - obsidian);
                     return this.init(
                        AiOperation.Status.FAILED,
                        "A nether portal needs " + needed + " more obsidian; holding " + obsidian,
                        AiJson.init_JsonObject_ObjArr("missing", missing)
                     );
                  }

                  if (AiGameState.init_I_Str("minecraft:flint_and_steel") <= 0 && AiGameState.init_I_Str("minecraft:fire_charge") <= 0) {
                     JsonObject missing = new JsonObject();
                     missing.addProperty("minecraft:flint_and_steel", 1);
                     return this.init(AiOperation.Status.FAILED, "Nothing to light the portal with", AiJson.init_JsonObject_ObjArr("missing", missing));
                  }

                  BlockPos site = this.init_BlockPos_18();
                  if (site == null && this.update_BlockPos != null && this.init(this.update_BlockPos, this.load_Direction, true) && !this.sync_Z) {
                     this.sync_Z = true;
                     BlockPos stand = this.init_BlockPos(this.update_BlockPos, this.load_Direction);
                     if (stand != null) {
                        this.load_AiOperation = AiGoto.init_AiOperation_BlockPos(stand, 0.5, false, true, false, 20);
                        return this.init("Stepping out of the dug portal site");
                     }
                  }

                  if (site == null && !this.update_Z) {
                     this.update_Z = true;
                     List<BlockPos> digs = this.init_List_10();
                     if (digs != null) {
                        this.init_ArrayDeque.addAll(digs);
                        AutismClientAddon.LOG
                           .info("[AI] dimension: digging {} blocks out for a portal site at {}", digs.size(), this.update_BlockPos.toShortString());
                        return this.init("Clearing a site for the portal frame");
                     }
                  }

                  if (site == null) {
                     return this.init(AiOperation.Status.FAILED, "No clear 4x5 space near the player for a portal frame");
                  }

                  this.read_BlockPos = site;
                  AiDimension.init_BlockPos = site.immutable();
                  AiDimension.init_Direction = this.init_Direction;
                  AiDimension.init_Str = this.load_Str;
                  AiDimension.load_Str = this.init_Str;
                  AiDimension.init_Obj = mc.getConnection();
                  this.init_List.clear();
                  this.init_List.addAll(AiDimension.init_List(site, this.init_Direction));
                  this.read_Set.clear();

                  for (BlockPos corner : AiDimension.load(site, this.init_Direction)) {
                     if (this.init_Z_9(corner)) {
                        this.read_Set.add(corner.immutable());
                     }
                  }

                  this.init_List.addAll(this.read_Set);
                  int top = site.getY() + 3;
                  this.init_List
                     .sort(Comparator.<BlockPos>comparingInt(c -> c.getY()).thenComparingInt(c -> this.read_Set.contains(c) == (c.getY() == top) ? 0 : 1));
                  this.init_I = 0;
                  this.flush_Z = false;
                  this.load_Z = true;

                  for (BlockPos cell : AiDimension.write(site, this.init_Direction)) {
                     if (!this.load_Z_8(cell)) {
                        this.init_ArrayDeque.add(cell.immutable());
                     }
                  }

                  if (!this.init_ArrayDeque.isEmpty()) {
                     AutismClientAddon.LOG
                        .info(
                           "[AI] dimension: {} blocks in the frame's interior at {}; digging them out first", this.init_ArrayDeque.size(), site.toShortString()
                        );
                     return this.init("Clearing the frame's interior");
                  } else {
                     return null;
                  }
               }

               AiOperation.Result load_AiOperation_Result_18() {
                  if (this.read_AiOperation != null) {
                     AiOperation.Result r = this.init_n_StreamUtil.init_AiOperation_Result_AiOperation(this.read_AiOperation);
                     if (!r.terminal()) {
                        return this.init("Moving to reach a frame block");
                     }

                     this.init_n_StreamUtil.init_V_AiOperation(this.read_AiOperation);
                     this.read_AiOperation = null;
                     if (r.status() == AiOperation.Status.UNCERTAIN || r.status() == AiOperation.Status.CANCELLED) {
                        return this.init(r.status(), "Frame approach interrupted: " + r.message());
                     }
                  }

                  if (this.init_n_BufferHelper != null
                     && (
                        this.init_n_BufferHelper.requiresReconciliationAfterEmergency()
                           || this.init_DataPipe.init_BlockPos() != null
                              && mc.level.getBlockState(this.init_DataPipe.init_BlockPos()).getBlock() == this.init_Block
                     )) {
                     AiOperation.Result r = this.init_n_StreamUtil.init_AiOperation_Result_AiOperation(this.init_n_BufferHelper);
                     if (!r.terminal()) {
                        return this.init("Waiting for the portal frame placement acknowledgement");
                     }

                     this.load_V_14();
                     if (r.status() != AiOperation.Status.SUCCEEDED) {
                        return this.init(r.status(), "Portal frame placement interrupted: " + r.message());
                     }
                  }

                  if (this.init_I < this.init_List.size()) {
                     BlockPos cell = this.init_List.get(this.init_I);
                     if (this.read_Set.contains(cell)) {
                        AiOperation.Result out = this.init_AiOperation_Result_4(cell);
                        if (out != null) {
                           return out;
                        }

                        Item block = AiShield.init(mc);
                        if (block != null && this.update(cell)) {
                           this.init(cell, block);
                           AiOperation.Result placed = this.init_n_StreamUtil.init_AiOperation_Result_AiOperation(this.init_n_BufferHelper);
                           if (placed.status() == AiOperation.Status.UNCERTAIN) {
                              return this.init(placed.status(), placed.message());
                           }

                           autismclient.ai.a.AiPlacer.Inner_DataUtil status = this.init(placed);
                           if ((status == autismclient.ai.a.AiPlacer.Inner_DataUtil.read || this.load_I + 1 > 120) && this.load_Set.add(cell)) {
                              this.load_V_14();
                              this.load_I = 0;
                              this.read_AiOperation = AiGoto.init(
                                 cell, block, 20, null, false, AiDimension.init_Set(this.read_BlockPos, this.init_Direction), this.init_Set_8()
                              );
                              return this.init("Moving to reach the frame's corner " + cell.toShortString());
                           }

                           if (status == autismclient.ai.a.AiPlacer.Inner_DataUtil.load
                              || status == autismclient.ai.a.AiPlacer.Inner_DataUtil.read
                              || ++this.load_I > 120) {
                              this.load_V_14();
                              this.init_I++;
                              this.load_I = 0;
                           }

                           return this.init("Placing the frame's corner " + cell.toShortString());
                        } else {
                           this.load_V_14();
                           this.init_I++;
                           this.load_I = 0;
                           return this.init("Corner " + cell.toShortString() + " skipped");
                        }
                     } else {
                        if (this.sync_BlockPos == null && !mc.level.getBlockState(cell).is(Blocks.OBSIDIAN) && !this.write(cell) && this.write_Set.add(cell)) {
                           this.sync_BlockPos = this.load_BlockPos_4(cell);
                        }

                        if (this.sync_BlockPos != null) {
                           AiOperation.Result out = this.init_AiOperation_Result_4(this.sync_BlockPos);
                           if (out != null) {
                              return out;
                           }

                           Item block = AiShield.init(mc);
                           if (block != null && this.update(this.sync_BlockPos)) {
                              this.init(this.sync_BlockPos, block);
                              AiOperation.Result placed = this.init_n_StreamUtil.init_AiOperation_Result_AiOperation(this.init_n_BufferHelper);
                              if (placed.status() == AiOperation.Status.UNCERTAIN) {
                                 return this.init(placed.status(), placed.message());
                              }

                              autismclient.ai.a.AiPlacer.Inner_DataUtil status = this.init(placed);
                              if (status == autismclient.ai.a.AiPlacer.Inner_DataUtil.load
                                 || status == autismclient.ai.a.AiPlacer.Inner_DataUtil.read
                                 || ++this.load_I > 120) {
                                 this.load_V_14();
                                 this.sync_BlockPos = null;
                                 this.load_I = 0;
                              }

                              return this.init("Placing a support for frame block " + (this.init_I + 1));
                           }

                           this.load_V_14();
                           this.sync_BlockPos = null;
                        }

                        if (mc.level.getBlockState(cell).is(Blocks.OBSIDIAN)) {
                           this.load_V_14();
                           this.init_I++;
                           this.load_I = 0;
                           return this.init("Frame " + this.init_I + "/" + this.init_List.size());
                        }

                        AiOperation.Result out = this.init_AiOperation_Result_4(cell);
                        if (out != null) {
                           return out;
                        }

                        this.init(cell, Items.OBSIDIAN);
                        AiOperation.Result placed = this.init_n_StreamUtil.init_AiOperation_Result_AiOperation(this.init_n_BufferHelper);
                        if (placed.status() == AiOperation.Status.UNCERTAIN) {
                           return this.init(placed.status(), placed.message());
                        }

                        autismclient.ai.a.AiPlacer.Inner_DataUtil status = this.init(placed);
                        if (status == autismclient.ai.a.AiPlacer.Inner_DataUtil.load) {
                           this.load_V_14();
                           this.init_I++;
                           this.load_I = 0;
                           return this.init("Frame " + this.init_I + "/" + this.init_List.size());
                        }

                        if (status != autismclient.ai.a.AiPlacer.Inner_DataUtil.read && ++this.load_I <= 120) {
                           return this.init("Placing frame block " + (this.init_I + 1) + "/" + this.init_List.size());
                        }

                        if (this.load_Set.add(cell)) {
                           this.load_V_14();
                           this.load_I = 0;
                           this.read_AiOperation = AiGoto.init(
                              cell, Items.OBSIDIAN, 20, null, false, AiDimension.init_Set(this.read_BlockPos, this.init_Direction), this.init_Set_8()
                           );
                           return this.init("Moving to reach frame block " + (this.init_I + 1));
                        }

                        this.load_Z = false;
                        if (this.read_BlockPos != null) {
                           this.init_Set.add(this.read_BlockPos.asLong());
                        }

                        return this.init(AiOperation.Status.FAILED, "Could not place the portal frame: " + this.init_DataPipe.init_Str());
                     }
                  } else {
                     this.init_V_31();
                     if (this.init_n_CachePool.init_Z()) {
                        return ++this.close_I > 120
                           ? this.init(AiOperation.Status.TIMED_OUT, "Portal lighting use was not acknowledged by the server")
                           : this.init("Waiting for the portal lighting acknowledgement");
                     }

                     if (!mc.level.getBlockState(this.read_BlockPos).is(Blocks.NETHER_PORTAL)
                        || this.init_BlockPos != null
                           && !this.init_n_CachePool.load()
                           && AiServerEvidence.init_J_BlockPos(this.read_BlockPos) <= this.init_n_CachePool.init_J()) {
                        if (this.close_I % 20 == 0 && this.push < 3) {
                           List<BlockPos> missing = this.load_List_4();
                           if (!missing.isEmpty()) {
                              this.push++;
                              this.load_Set.removeAll(missing);
                              this.write_Set.removeAll(missing);
                              int first = this.init_List.size();

                              for (BlockPos cell : missing) {
                                 int i = this.init_List.indexOf(cell);
                                 if (i >= 0) {
                                    first = Math.min(first, i);
                                 }
                              }

                              AutismClientAddon.LOG.info("[AI] dimension: frame cells {} hold no obsidian; placing them again", missing);
                              if (first < this.init_List.size()) {
                                 this.init_I = first;
                                 this.close_I = 0;
                                 return this.init("Placing the frame's missing blocks");
                              }
                           }
                        }

                        if (++this.close_I > 120) {
                           if (!this.close_Z) {
                              this.close_Z = true;

                              for (BlockPos cell : AiDimension.write(this.read_BlockPos, this.init_Direction)) {
                                 if (!this.load_Z_8(cell)) {
                                    this.init_ArrayDeque.add(cell.immutable());
                                 }
                              }

                              if (!this.init_ArrayDeque.isEmpty()) {
                                 this.close_I = 0;
                                 AutismClientAddon.LOG
                                    .info("[AI] dimension: {} blocks in the portal's interior; digging them out", this.init_ArrayDeque.size());
                                 return this.init("Clearing the portal's interior");
                              }
                           }

                           this.load_Z = false;
                           AutismClientAddon.LOG
                              .info(
                                 "[AI] dimension: the portal at {} did not light after {} clicks:{}",
                                 this.read_BlockPos.toShortString(),
                                 this.pop,
                                 this.load_Str_4()
                              );
                           return this.init(AiOperation.Status.FAILED, "The frame is complete but the portal did not light");
                        } else {
                           if (AiItems.read(AiItems.load_af_DataUtil("minecraft:flint_and_steel")) < 0 && AiItems.read(AiItems.load_af_DataUtil("minecraft:fire_charge")) < 0) {
                              this.load_Z = false;
                              return this.init(AiOperation.Status.FAILED, "Lost the lighter before the portal was lit");
                           }

                           BlockPos[] blocks = new BlockPos[]{
                              this.read_BlockPos.below(),
                              this.read_BlockPos.relative(this.init_Direction).below(),
                              this.read_BlockPos.relative(this.init_Direction.getOpposite()),
                              this.read_BlockPos.relative(this.init_Direction, 2)
                           };
                           Direction[] faces = new Direction[]{Direction.UP, Direction.UP, this.init_Direction, this.init_Direction.getOpposite()};
                           int k = Math.floorMod(this.get, 4);
                           BlockPos floor = blocks[k];
                           this.init_DataUtil.init_V_Vec3(autismclient.ai.a.AiAim.init_Vec3_BlockPos_Direction(floor, faces[k]));
                           this.init_DataUtil.load_V();
                           BlockHitResult seen = autismclient.ai.a.AiAim.init_BlockHitResult();
                           boolean onFace = seen != null && seen.getBlockPos().equals(floor) && seen.getDirection() == faces[k];
                           if (!onFace) {
                              if (this.tick % 10 == 9) {
                                 this.get++;
                              }

                              if (++this.tick > 40 && this.run < 3) {
                                 BlockPos stand = this.read_BlockPos_4();
                                 if (stand == null && this.tick % 100 == 41) {
                                    AutismClientAddon.LOG
                                       .info(
                                          "[AI] dimension: no view of the portal's floor or jambs from {}, and no stance to light from",
                                          mc.player.blockPosition().toShortString()
                                       );
                                 }

                                 if (stand != null) {
                                    this.run++;
                                    this.tick = 0;
                                    this.close_I = 0;
                                    AutismClientAddon.LOG
                                       .info(
                                          "[AI] dimension: no view of the portal's floor from {}; lighting from {}",
                                          mc.player.blockPosition().toShortString(),
                                          stand.toShortString()
                                       );
                                    this.read_AiOperation = AiGoto.init_AiOperation_BlockPos(stand, 0.4, false, false, false, 15);
                                    return this.init("Moving in front of the frame to light it");
                                 }
                              }

                              return this.init("Turning to light the portal");
                           } else {
                              BlockHitResult hit = autismclient.ai.a.AiAim.init_BlockHitResult();
                              if (hit == null) {
                                 return this.init("Turning to light the portal");
                              }

                              this.init_BlockPos = hit.getBlockPos().relative(hit.getDirection()).immutable();
                              this.init_BlockState = mc.level.getBlockState(this.init_BlockPos);
                              BlockStatePredictionHandler prediction = ((AutismClientLevelPredictionAccessor)mc.level).autism$getBlockPredictionHandler();
                              this.init_n_CachePool.init(prediction.currentSequence(), AiServerEvidence.init_J_BlockPos(this.init_BlockPos));
                              InteractionResult used = mc.gameMode.useItemOn(mc.player, InteractionHand.MAIN_HAND, hit);
                              this.init_n_CachePool.init(prediction.currentSequence());
                              Compat263.swing(mc.player, InteractionHand.MAIN_HAND);
                              if (this.pop++ % 10 == 0) {
                                 AutismClientAddon.LOG
                                    .info(
                                       "[AI] dimension: lighting click {} on {} {} -> {}",
                                       this.pop,
                                       hit.getBlockPos().toShortString(),
                                       hit.getDirection(),
                                       used
                                    );
                              }

                              if (!this.init_n_CachePool.init_Z()) {
                                 this.get++;
                              }

                              return this.init("Lighting the portal");
                           }
                        }
                     } else {
                        this.load_Z = false;
                        this.read_Z = true;
                        this.load_BlockPos = this.read_BlockPos;
                        AiDimension.load_BlockPos = this.read_BlockPos.immutable();
                        return this.init("Portal lit");
                     }
                  }
               }

               void init_V_31() {
                  if (this.init_n_CachePool.init_Z() && this.init_BlockPos != null && mc.level == this.load_Obj) {
                     BlockState state = mc.level.getBlockState(this.init_BlockPos);
                     this.init_n_CachePool
                        .init(
                           state.is(Blocks.NETHER_PORTAL),
                           state.is(Blocks.FIRE) || state.is(Blocks.SOUL_FIRE),
                           state == this.init_BlockState,
                           AiServerEvidence.init_J_BlockPos(this.init_BlockPos),
                           autismclient.util.villager.BlockChangeAckTracker.init_Z(mc.level, this.init_n_CachePool.init_I())
                        );
                     if (!this.init_n_CachePool.init_Z() && !this.init_n_CachePool.load()) {
                        this.get++;
                     }
                  }
               }

               void init(BlockPos cell, Item item) {
                  if (this.init_n_BufferHelper == null || !cell.equals(this.init_DataPipe.init_BlockPos())) {
                     this.load_V_14();
                     long before = AiServerEvidence.init_J_BlockPos(cell);
                     this.init_DataPipe.init_V_BlockPos_Item(cell, item);
                     this.init_Block = item instanceof BlockItem block ? block.getBlock() : null;
                     this.init_n_BufferHelper = new AiDimension.Inner_BufferHelper(
                        this.init_DataPipe::init_DataPipe_DataUtil,
                        this.init_DataPipe::load_Z,
                        () -> AiJson.init_JsonObject_ObjArr(
                           "cell",
                           cell.toShortString(),
                           "item",
                           BuiltInRegistries.ITEM.getKey(item).toString(),
                           "blockRevisionBefore",
                           before,
                           "blockRevisionNow",
                           AiServerEvidence.init_J_BlockPos(cell),
                           "state",
                           this.init_DataPipe.load_Str(),
                           "pending",
                           this.init_DataPipe.load_Z()
                        ),
                        this.init_DataPipe::init_Str,
                        this.init_DataPipe::init_V
                     );
                     this.load_I = 0;
                  }
               }

               autismclient.ai.a.AiPlacer.Inner_DataUtil init(AiOperation.Result result) {
                  return result.status() == AiOperation.Status.RUNNING
                     ? autismclient.ai.a.AiPlacer.Inner_DataUtil.init
                     : (
                        result.status() == AiOperation.Status.SUCCEEDED
                           ? autismclient.ai.a.AiPlacer.Inner_DataUtil.load
                           : autismclient.ai.a.AiPlacer.Inner_DataUtil.read
                     );
               }

               void load_V_14() {
                  if (this.init_n_BufferHelper != null) {
                     this.init_JsonObject = this.init_n_BufferHelper.init();
                  }

                  this.init_n_StreamUtil.init_V_AiOperation(this.init_n_BufferHelper);
                  this.init_n_BufferHelper = null;
                  this.init_Block = null;
               }

               int init_I(BlockPos origin, Direction axis) {
                  int cnt = 0;

                  for (BlockPos cell : AiDimension.init_List(origin, axis)) {
                     if (mc.level.getBlockState(cell).is(Blocks.OBSIDIAN)) {
                        cnt++;
                     }
                  }

                  return cnt;
               }

               BlockPos init_BlockPos_18() {
                  BlockPos reuse = null;
                  Direction reuseAxis = null;
                  int most = 0;
                  BlockPos at = mc.player.blockPosition();

                  for (int radius = 0; radius <= 8; radius++) {
                     for (int dx = -radius; dx <= radius; dx++) {
                        for (int dz = -radius; dz <= radius; dz++) {
                           if (Math.abs(dx) == radius || Math.abs(dz) == radius) {
                              for (int dy = -3; dy <= 3; dy++) {
                                 for (Direction axis : List.of(Direction.EAST, Direction.SOUTH)) {
                                    BlockPos candidate = at.offset(dx, dy, dz);
                                    if (!this.init_Set.contains(candidate.asLong())) {
                                       int n = this.init_I(candidate, axis);
                                       if (n > most && this.init_Z(candidate, axis)) {
                                          most = n;
                                          reuse = candidate;
                                          reuseAxis = axis;
                                       }
                                    }
                                 }
                              }
                           }
                        }
                     }
                  }

                  if (reuse != null) {
                     this.init_Direction = reuseAxis;
                     return reuse;
                  } else {
                     return this.load_BlockPos_5();
                  }
               }

               boolean init_Z(BlockPos origin, Direction axis) {
                  for (BlockPos cell : AiDimension.init_List(origin, axis)) {
                     if (!mc.level.getBlockState(cell).is(Blocks.OBSIDIAN) && !this.init_Z_9(cell)) {
                        return false;
                     }
                  }

                  Set<BlockPos> site = new HashSet<>(AiDimension.init_List(origin, axis));
                  site.addAll(AiDimension.write(origin, axis));

                  for (BlockPos cell : AiDimension.init_List(origin, axis)) {
                     if (!mc.level.getBlockState(cell).is(Blocks.OBSIDIAN) && !this.write(cell)) {
                        boolean supportable = false;

                        for (Direction d : Direction.values()) {
                           BlockPos side = cell.relative(d);
                           if (!site.contains(side) && this.init_Z_9(side) && this.write(side)) {
                              supportable = true;
                              break;
                           }
                        }

                        if (!supportable && !site.contains(cell.below())) {
                           return false;
                        }
                     }
                  }

                  for (BlockPos cell : AiDimension.write(origin, axis)) {
                     if (!this.init_Z_9(cell) && !this.read(cell)) {
                        return false;
                     }
                  }

                  return true;
               }

               BlockPos load_BlockPos_5() {
                  BlockPos feet = mc.player.blockPosition();

                  for (int radius = 2; radius <= 8; radius++) {
                     for (int dx = -radius; dx <= radius; dx++) {
                        for (int dz = -radius; dz <= radius; dz++) {
                           if (Math.abs(dx) == radius || Math.abs(dz) == radius) {
                              for (int dy : AiDimension.init_IArr) {
                                 for (Direction axis : List.of(Direction.EAST, Direction.SOUTH)) {
                                    BlockPos candidate = feet.offset(dx, dy, dz);
                                    if (!this.init_Set.contains(candidate.asLong()) && this.load(candidate, axis)) {
                                       this.init_Direction = axis;
                                       return candidate;
                                    }
                                 }
                              }
                           }
                        }
                     }
                  }

                  return null;
               }

               boolean load(BlockPos origin, Direction axis) {
                  return this.init(origin, axis, false);
               }

               boolean init(BlockPos origin, Direction axis, boolean ignorePlayer) {
                  for (BlockPos cell : AiDimension.init_List(origin, axis)) {
                     if (!mc.level.getBlockState(cell).is(Blocks.OBSIDIAN) && (ignorePlayer ? !this.init_Z_9(cell) : !this.update(cell))) {
                        return false;
                     }
                  }

                  for (BlockPos cell : AiDimension.write(origin, axis)) {
                     if (ignorePlayer ? !this.init_Z_9(cell) : !this.update(cell)) {
                        return false;
                     }
                  }

                  for (BlockPos support : AiDimension.read(origin, axis)) {
                     if (!AiTerrain.init(mc.level, support)) {
                        return false;
                     }

                     if (mc.level.getBlockState(support).getCollisionShape(mc.level, support).isEmpty()) {
                        return false;
                     }
                  }

                  return true;
               }

               boolean init_Z_9(BlockPos cell) {
                  if (!AiTerrain.init(mc.level, cell)) {
                     return false;
                  }

                  BlockState state = mc.level.getBlockState(cell);
                  return state.canBeReplaced() && state.getFluidState().isEmpty();
               }

               boolean load_Z_8(BlockPos cell) {
                  BlockState state = mc.level.getBlockState(cell);
                  return state.isAir() || state.is(BlockTags.FIRE) || state.is(Blocks.NETHER_PORTAL);
               }

               boolean read(BlockPos cell) {
                  BlockState state = mc.level.getBlockState(cell);
                  if (state.getFluidState().isEmpty()
                     && !state.hasBlockEntity()
                     && !state.is(Blocks.OBSIDIAN)
                     && !state.is(Blocks.BEDROCK)
                     && !(state.getBlock() instanceof FallingBlock)) {
                     float hardness = state.getDestroySpeed(mc.level, cell);
                     if (!(hardness < 0.0F) && !(hardness > 5.0F)) {
                        if (state.requiresCorrectToolForDrops()) {
                           boolean tool = false;

                           for (int slot = 0; slot < 36 && !tool; slot++) {
                              tool = mc.player.getInventory().getItem(slot).isCorrectToolForDrops(state);
                           }

                           if (!tool) {
                              return false;
                           }
                        }

                        for (Direction d : Direction.values()) {
                           FluidState fluid = mc.level.getFluidState(cell.relative(d));
                           if (fluid.is(FluidTags.LAVA)) {
                              return false;
                           }

                           if (fluid.is(FluidTags.WATER) && d != Direction.DOWN) {
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

               List<BlockPos> init_List_10() {
                  BlockPos feet = mc.player.blockPosition();
                  List<BlockPos> best = null;
                  double bestScore = 1.7976931348623157E308;

                  for (int radius = 2; radius <= 6; radius++) {
                     for (int dx = -radius; dx <= radius; dx++) {
                        for (int dz = -radius; dz <= radius; dz++) {
                           if (Math.abs(dx) == radius || Math.abs(dz) == radius) {
                              for (int dy : AiDimension.init_IArr) {
                                 for (Direction axis : List.of(Direction.EAST, Direction.SOUTH)) {
                                    BlockPos origin = feet.offset(dx, dy, dz);
                                    List<BlockPos> digs = new ArrayList<>();
                                    boolean ok = true;
                                    List<BlockPos> cells = new ArrayList<>(AiDimension.init_List(origin, axis));
                                    cells.addAll(AiDimension.write(origin, axis));

                                    for (BlockPos cell : cells) {
                                       if (!AiTerrain.init(mc.level, cell)) {
                                          ok = false;
                                          break;
                                       }

                                       if (!this.init_Z_9(cell)) {
                                          if (!this.read(cell)) {
                                             ok = false;
                                             break;
                                          }

                                          digs.add(cell.immutable());
                                       }
                                    }

                                    if (ok && digs.size() <= 20) {
                                       for (BlockPos support : AiDimension.read(origin, axis)) {
                                          if (mc.level.getBlockState(support).getCollisionShape(mc.level, support).isEmpty()) {
                                             ok = false;
                                             break;
                                          }
                                       }

                                       if (ok) {
                                          double score = digs.size() * 10.0 + origin.distSqr(feet);
                                          if (score < bestScore) {
                                             bestScore = score;
                                             best = digs;
                                             this.update_BlockPos = origin;
                                             this.load_Direction = axis;
                                          }
                                       }
                                    }
                                 }
                              }
                           }
                        }
                     }
                  }

                  if (best != null) {
                     best.sort(Comparator.comparingInt(c -> -c.getY()));
                  }

                  return best;
               }

               AiOperation.Result read_AiOperation_Result_13() {
                  if (this.load_AiOperation != null) {
                     AiOperation.Result r = this.init_n_StreamUtil.init_AiOperation_Result_AiOperation(this.load_AiOperation);
                     if (!r.terminal()) {
                        return this.init(
                           this.init_ArrayDeque.isEmpty()
                              ? "Stepping out of the dug portal site"
                              : "Clearing a site for the portal (" + (this.init_ArrayDeque.size() + 1) + " left)"
                        );
                     }

                     this.init_n_StreamUtil.init_V_AiOperation(this.load_AiOperation);
                     this.load_AiOperation = null;
                     if (r.status() == AiOperation.Status.UNCERTAIN || r.status() == AiOperation.Status.CANCELLED) {
                        return this.init(r.status(), "Portal excavation interrupted: " + r.message());
                     }

                     if (r.status() != AiOperation.Status.SUCCEEDED && !this.init_ArrayDeque.isEmpty() && ++this.open > 3) {
                        this.init_ArrayDeque.clear();
                        return this.init(AiOperation.Status.FAILED, "Could not clear a portal site: " + r.message());
                     }
                  }

                  while (!this.init_ArrayDeque.isEmpty() && this.load_Z_8(this.init_ArrayDeque.peek())) {
                     this.init_ArrayDeque.poll();
                  }

                  if (this.init_ArrayDeque.isEmpty()) {
                     return null;
                  }

                  BlockPos cell = this.init_ArrayDeque.poll();
                  this.load_AiOperation = autismclient.ai.a.AiBreakBlock.init(cell, 20, false);
                  return this.init("Clearing a site for the portal at " + cell.toShortString());
               }

               BlockPos init_BlockPos(BlockPos origin, Direction axis) {
                  Set<BlockPos> site = new HashSet<>(AiDimension.init_List(origin, axis));
                  site.addAll(AiDimension.write(origin, axis));
                  site.addAll(AiDimension.load(origin, axis));
                  BlockPos best = null;
                  double bestDistance = 1.7976931348623157E308;

                  for (BlockPos cell : site) {
                     for (int dx = -3; dx <= 3; dx++) {
                        for (int dy = -2; dy <= 2; dy++) {
                           for (int dz = -3; dz <= 3; dz++) {
                              BlockPos stand = cell.offset(dx, dy, dz);
                              if (!site.contains(stand) && !site.contains(stand.above()) && this.init_Z_9(stand) && this.init_Z_9(stand.above())) {
                                 BlockPos floor = stand.below();
                                 if (!mc.level.getBlockState(floor).getCollisionShape(mc.level, floor).isEmpty() && !site.contains(floor)) {
                                    double distance = stand.distSqr(mc.player.blockPosition());
                                    if (distance < bestDistance) {
                                       bestDistance = distance;
                                       best = stand.immutable();
                                    }
                                 }
                              }
                           }
                        }
                     }
                  }

                  return best;
               }

               BlockPos load_BlockPos_4(BlockPos cell) {
                  Set<BlockPos> site = new HashSet<>(AiDimension.init_List(this.read_BlockPos, this.init_Direction));
                  site.addAll(AiDimension.write(this.read_BlockPos, this.init_Direction));
                  BlockPos below = cell.below();
                  if (!site.contains(below) && this.init_Z_9(below) && this.write(below)) {
                     return below;
                  }

                  BlockPos best = null;
                  double bestDistance = 1.7976931348623157E308;

                  for (Direction d : Direction.values()) {
                     BlockPos side = cell.relative(d);
                     if (d != Direction.DOWN && !site.contains(side) && this.update(side) && this.write(side)) {
                        double distance = mc.player.getEyePosition().distanceToSqr(Vec3.atCenterOf(side));
                        if (distance < bestDistance) {
                           bestDistance = distance;
                           best = side;
                        }
                     }
                  }

                  return best;
               }

               boolean write(BlockPos cell) {
                  for (Direction d : Direction.values()) {
                     BlockPos side = cell.relative(d);
                     if (!mc.level.getBlockState(side).getCollisionShape(mc.level, side).isEmpty()) {
                        return true;
                     }
                  }

                  return false;
               }

               AiOperation.Result init_AiOperation_Result_4(BlockPos cell) {
                  if (this.poll < 6 && mc.player.getBoundingBox().intersects(new AABB(cell))) {
                     BlockPos stand = this.init_BlockPos(this.read_BlockPos, this.init_Direction);
                     if (stand == null) {
                        return null;
                     }

                     this.poll++;
                     this.load_V_14();
                     this.load_I = 0;
                     this.read_AiOperation = AiGoto.init_AiOperation_BlockPos(stand, 0.5, false, true, false, 20);
                     return this.init("Stepping out of frame cell " + cell.toShortString());
                  } else {
                     return null;
                  }
               }

               Set<Long> init_Set_8() {
                  Set<Long> cells = AiDimension.init_Set(this.read_BlockPos, this.init_Direction);

                  for (BlockPos corner : AiDimension.load(this.read_BlockPos, this.init_Direction)) {
                     cells.add(corner.asLong());
                  }

                  return cells;
               }

               BlockPos read_BlockPos_4() {
                  Direction normal = this.init_Direction.getClockWise();
                  BlockPos best = null;
                  double bestDistance = 1.7976931348623157E308;
                  BlockPos at = mc.player.blockPosition();

                  for (int side : new int[]{1, -1}) {
                     for (int step = 0; step < 2; step++) {
                        for (int out = 1; out <= 2; out++) {
                           BlockPos stand = this.read_BlockPos.relative(this.init_Direction, step).relative(normal, side * out);
                           if (!stand.equals(at) || this.run <= 0) {
                              BlockPos below = stand.below();
                              if (this.init_Z_9(stand)
                                 && this.init_Z_9(stand.above())
                                 && !mc.level.getBlockState(below).getCollisionShape(mc.level, below).isEmpty()) {
                                 double distance = stand.distSqr(at) + (out - 1) * 2;
                                 if (distance < bestDistance) {
                                    bestDistance = distance;
                                    best = stand;
                                 }
                              }
                           }
                        }
                     }
                  }

                  return best;
               }

               List<BlockPos> load_List_4() {
                  List<BlockPos> missing = new ArrayList<>();

                  for (BlockPos cell : AiDimension.init_List(this.read_BlockPos, this.init_Direction)) {
                     if (!mc.level.getBlockState(cell).is(Blocks.OBSIDIAN)) {
                        missing.add(cell);
                     }
                  }

                  return missing;
               }

               String load_Str_4() {
                  StringBuilder sb = new StringBuilder();

                  for (BlockPos cell : AiDimension.init_List(this.read_BlockPos, this.init_Direction)) {
                     BlockState state = mc.level.getBlockState(cell);
                     if (!state.is(Blocks.OBSIDIAN)) {
                        sb.append(" frame ").append(cell.toShortString()).append('=').append(state.getBlock().getDescriptionId());
                     }
                  }

                  for (BlockPos cell : AiDimension.write(this.read_BlockPos, this.init_Direction)) {
                     BlockState state = mc.level.getBlockState(cell);
                     if (!state.isAir()) {
                        sb.append(" interior ").append(cell.toShortString()).append('=').append(state.getBlock().getDescriptionId());
                     }
                  }

                  return sb.length() == 0 ? " frame and interior as planned" : sb.toString();
               }

               boolean update(BlockPos cell) {
                  if (!AiTerrain.init(mc.level, cell)) {
                     return false;
                  }

                  BlockState state = mc.level.getBlockState(cell);
                  return state.canBeReplaced() && state.getFluidState().isEmpty() ? !mc.player.getBoundingBox().intersects(new AABB(cell)) : false;
               }

               BlockPos init(Block wanted) {
                  BlockPos origin = mc.player.blockPosition();
                  BlockPos best = null;
                  double bestDistance = 1.7976931348623157E308;
                  int vertical = Math.min(48, 32);

                  for (BlockPos pos : BlockPos.betweenClosed(origin.offset(-48, -vertical, -48), origin.offset(48, vertical, 48))) {
                     if (AiTerrain.init(mc.level, pos) && mc.level.getBlockState(pos).getBlock() == wanted) {
                        double distance = mc.player.distanceToSqr(Vec3.atCenterOf(pos));
                        if (distance < bestDistance) {
                           bestDistance = distance;
                           best = pos.immutable();
                        }
                     }
                  }

                  return best;
               }

               int init_I_37() {
                  return (int)Math.max(1L, (this.init_J - System.nanoTime()) / 1000000000L);
               }

               JsonObject load_JsonObject_4() {
                  JsonObject out = AiJson.init_JsonObject_ObjArr(
                     "action",
                     action.name().toLowerCase(Locale.ROOT),
                     "target",
                     target,
                     "dimension",
                     AiDimension.init_Str(),
                     "obsidian",
                     AiGameState.init_I_Str("minecraft:obsidian"),
                     "framePlaced",
                     this.init_I,
                     "frameBlocks",
                     this.init_List.size(),
                     "lit",
                     this.read_Z,
                     "observedLinks",
                     AiPortalLinks.init_JsonArray()
                  );
                  out.add("arrivalSearchPrediction", AiPortalLinks.init(this.init_Vec3, this.init_Str, target));
                  if (this.load_BlockPos != null) {
                     out.addProperty("portalAt", this.load_BlockPos.toShortString());
                  }

                  this.init_n_StreamUtil.init_V_JsonObject(out);
                  if (this.init_n_BufferHelper != null) {
                     out.add("placementReceipt", this.init_n_BufferHelper.init());
                  } else if (this.init_JsonObject != null) {
                     out.add("placementReceipt", this.init_JsonObject);
                  }

                  if (this.init_BlockPos != null) {
                     JsonObject receipt = this.init_n_CachePool.init_JsonObject();
                     receipt.addProperty("cell", this.init_BlockPos.toShortString());
                     out.add("lightingReceipt", receipt);
                  }

                  if (action == AiDimension.Inner_DataUtil.load) {
                     out.add("portalsNearby", this.init_JsonArray_3());
                  }

                  return out;
               }

               JsonArray init_JsonArray_3() {
                  JsonArray found = new JsonArray();

                  for (Block candidate : List.of(Blocks.NETHER_PORTAL, Blocks.END_PORTAL, Blocks.END_GATEWAY)) {
                     BlockPos nearest = this.init(candidate);
                     if (nearest != null) {
                        found.add(
                           AiJson.init_JsonObject_ObjArr(
                              "block",
                              BuiltInRegistries.BLOCK.getKey(candidate).toString(),
                              "x",
                              nearest.getX(),
                              "y",
                              nearest.getY(),
                              "z",
                              nearest.getZ(),
                              "distance",
                              Math.round(Math.sqrt(mc.player.distanceToSqr(Vec3.atCenterOf(nearest))) * 10.0) / 10.0
                           )
                        );
                     }
                  }

                  return found;
               }

               AiOperation.Result init(String message) {
                  return new AiOperation.Result(AiOperation.Status.RUNNING, message, this.load_JsonObject_4().toString(), List.of());
               }

               AiOperation.Result init(AiOperation.Status status, String message) {
                  return this.init(status, message, null);
               }

               AiOperation.Result init(AiOperation.Status status, String message, JsonObject extra) {
                  this.read_V_9();
                  JsonObject data = this.load_JsonObject_4();
                  if (extra != null) {
                     for (Entry<String, JsonElement> entry : extra.entrySet()) {
                        data.add(entry.getKey(), entry.getValue());
                     }
                  }

                  AiOperation.Result result = this.init_n_StreamUtil
                     .init_AiOperation_Result_AiOperation_Result_Z(
                        new AiOperation.Result(status, message, data.toString(), List.of()), this.init_n_CachePool.init_Z()
                     );
                  this.cancel();
                  return result;
               }

               void read_V_9() {
                  this.init_V_31();
                  this.init_n_StreamUtil.init_V_AiOperationArr(this.init_AiOperation, this.load_AiOperation, this.read_AiOperation, this.init_n_BufferHelper);
               }

               @Override
               public boolean canInterruptNow() {
                  return !this.requiresReconciliationAfterEmergency()
                     && (this.init_AiOperation == null || this.init_AiOperation.canInterruptNow())
                     && (this.load_AiOperation == null || this.load_AiOperation.canInterruptNow())
                     && (this.read_AiOperation == null || this.read_AiOperation.canInterruptNow());
               }

               @Override
               public boolean requiresReconciliationAfterEmergency() {
                  return this.init_n_StreamUtil.init_Z()
                     || this.init_n_CachePool.init_Z()
                     || this.init_n_BufferHelper != null && this.init_n_BufferHelper.requiresReconciliationAfterEmergency()
                     || this.init_AiOperation != null && this.init_AiOperation.requiresReconciliationAfterEmergency()
                     || this.load_AiOperation != null && this.load_AiOperation.requiresReconciliationAfterEmergency()
                     || this.read_AiOperation != null && this.read_AiOperation.requiresReconciliationAfterEmergency();
               }

               @Override
               public void cancel() {
                  if (!this.write_Z) {
                     this.read_V_9();
                     this.write_Z = true;
                     this.init_DataUtil.init_V();
                     this.load_DataUtil.init_V();
                     if (this.write_I > 0 && mc.options != null) {
                        mc.options.keyUp.setDown(false);
                        mc.options.keyJump.setDown(false);
                     }

                     this.load_V_14();
                     if (this.load_AiOperation != null) {
                        this.init_n_StreamUtil.init_V_AiOperation(this.load_AiOperation);
                        this.load_AiOperation = null;
                     }

                     if (this.read_AiOperation != null) {
                        this.init_n_StreamUtil.init_V_AiOperation(this.read_AiOperation);
                        this.read_AiOperation = null;
                     }

                     if (this.init_AiOperation != null) {
                        this.init_n_StreamUtil.init_V_AiOperation(this.init_AiOperation);
                        this.init_AiOperation = null;
                     }
                  }
               }
            };
         }
      } else {
         throw new IllegalArgumentException("Dimension travel requires a connected living player");
      }
   }

   static List<BlockPos> init_List(BlockPos origin, Direction axis) {
      List<BlockPos> cells = new ArrayList<>(10);

      for (int step = 0; step < 2; step++) {
         cells.add(origin.relative(axis, step).below());
         cells.add(origin.relative(axis, step).above(3));
      }

      for (int height = 0; height < 3; height++) {
         cells.add(origin.relative(axis, -1).above(height));
         cells.add(origin.relative(axis, 2).above(height));
      }

      return cells;
   }

   static List<BlockPos> load(BlockPos origin, Direction axis) {
      return List.of(origin.relative(axis, -1).below(), origin.relative(axis, 2).below(), origin.relative(axis, -1).above(3), origin.relative(axis, 2).above(3));
   }

   static List<BlockPos> read(BlockPos origin, Direction axis) {
      List<BlockPos> cells = new ArrayList<>(2);

      for (int step = 0; step < 2; step++) {
         cells.add(origin.relative(axis, step).below(2));
      }

      return cells;
   }

   static List<BlockPos> write(BlockPos origin, Direction axis) {
      List<BlockPos> cells = new ArrayList<>(6);

      for (int step = 0; step < 2; step++) {
         for (int height = 0; height < 3; height++) {
            cells.add(origin.relative(axis, step).above(height));
         }
      }

      return cells;
   }

   static String init_Str(String spec) {
      String clean = spec.trim().toLowerCase(Locale.ROOT);
      if (clean.equals("nether")) {
         clean = "the_nether";
      }

      if (clean.equals("end")) {
         clean = "the_end";
      }

      return clean.contains(":") ? clean : "minecraft:" + clean;
   }

   private static String init_Str() {
      Minecraft mc = Minecraft.getInstance();
      return mc.level == null ? "" : mc.level.dimension().identifier().toString();
   }

   private static AiDimension.Inner_DataUtil init_n_DataUtil(String text) {
      try {
         return AiDimension.Inner_DataUtil.valueOf(text.trim().toUpperCase(Locale.ROOT));
      } catch (IllegalArgumentException e) {
         throw new IllegalArgumentException("action must be enter or find");
      }
   }

   static {
      init_Map.put("minecraft:the_nether", Blocks.NETHER_PORTAL);
      init_Map.put("minecraft:overworld", Blocks.NETHER_PORTAL);
      init_Map.put("minecraft:the_end", Blocks.END_PORTAL);
   }

   static final class Inner_BufferHelper implements AiOperation {
      private final Supplier<autismclient.ai.a.AiPlacer.Inner_DataUtil> init_Supplier;
      private final BooleanSupplier init_BooleanSupplier;
      private final Supplier<JsonObject> load_Supplier;
      private final Supplier<String> read;
      private final Runnable init_Runnable;
      private boolean init_Z = true;
      private boolean load_Z;
      private int init_I;

      Inner_BufferHelper(
         Supplier<autismclient.ai.a.AiPlacer.Inner_DataUtil> work,
         BooleanSupplier pending,
         Supplier<JsonObject> receipt,
         Supplier<String> failure,
         Runnable release
      ) {
         this.init_Supplier = work;
         this.init_BooleanSupplier = pending;
         this.load_Supplier = receipt;
         this.read = failure;
         this.init_Runnable = release;
      }

      @Override
      public AiOperation.Result tick() {
         if (!this.init_Z) {
            return new AiOperation.Result(AiOperation.Status.CANCELLED, "Portal frame placement cancelled", "{}", List.of());
         }

         if (this.load_Z && this.init_BooleanSupplier.getAsBoolean()) {
            return new AiOperation.Result(
               ++this.init_I > 120 ? AiOperation.Status.TIMED_OUT : AiOperation.Status.RUNNING,
               "Waiting for the portal frame placement acknowledgement",
               AiJson.init_JsonObject_ObjArr("placementReceipt", this.load_Supplier.get()).toString(),
               List.of()
            );
         }

         this.load_Z = true;
         autismclient.ai.a.AiPlacer.Inner_DataUtil status = this.init_Supplier.get();
         return new AiOperation.Result(
            status == autismclient.ai.a.AiPlacer.Inner_DataUtil.init
               ? AiOperation.Status.RUNNING
               : (status == autismclient.ai.a.AiPlacer.Inner_DataUtil.load ? AiOperation.Status.SUCCEEDED : AiOperation.Status.FAILED),
            status == autismclient.ai.a.AiPlacer.Inner_DataUtil.read ? this.read.get() : "Portal frame placement",
            AiJson.init_JsonObject_ObjArr("placementReceipt", this.load_Supplier.get()).toString(),
            List.of()
         );
      }

      @Override
      public boolean canInterruptNow() {
         return !this.requiresReconciliationAfterEmergency();
      }

      @Override
      public boolean requiresReconciliationAfterEmergency() {
         return this.init_Z && this.init_BooleanSupplier.getAsBoolean();
      }

      @Override
      public void cancel() {
         if (this.init_Z) {
            this.init_Z = false;
            this.init_Runnable.run();
         }
      }

      JsonObject init() {
         return this.load_Supplier.get();
      }
   }

   static final class Inner_CachePool {
      private int init_I;
      private int load = -1;
      private long init_J;
      private boolean init_Z;
      private String init_Str = "unsent";

      void init(int sequence, long revision) {
         if (this.init_Z) {
            throw new IllegalStateException("The previous portal lighting use is still unacknowledged");
         }

         this.init_I = sequence;
         this.init_J = revision;
      }

      void init(int sequence) {
         this.load = sequence;
         this.init_Z = sequence > this.init_I;
         this.init_Str = this.init_Z ? "awaiting-server" : "unsent";
      }

      void init(boolean portal, boolean fire, boolean unchanged, long revision, boolean acknowledged) {
         if (this.init_Z) {
            if (portal && revision > this.init_J) {
               this.init_Z = false;
               this.init_Str = "portal-confirmed";
            } else if (acknowledged && revision > this.init_J && unchanged && !fire && !portal) {
               this.init_Z = false;
               this.init_Str = "server-refused";
            }
         }
      }

      boolean init_Z() {
         return this.init_Z;
      }

      boolean load() {
         return this.init_Str.equals("portal-confirmed");
      }

      long init_J() {
         return this.init_J;
      }

      int init_I() {
         return this.load;
      }

      JsonObject init_JsonObject() {
         return AiJson.init_JsonObject_ObjArr(
            "beforeSequence", this.init_I, "sentSequence", this.load, "blockRevisionBefore", this.init_J, "pending", this.init_Z, "outcome", this.init_Str
         );
      }
   }

   enum Inner_DataUtil {
      init,
      load;
   }

   static final class Inner_StreamUtil {
      private final IdentityHashMap<AiOperation, AiOperation.Result> init_IdentityHashMap = new IdentityHashMap<>();
      private AiOperation init_AiOperation;
      private AiOperation.Result init_AiOperation_Result;
      private AiOperation.Result load;
      private boolean init_Z;

      AiOperation.Result init_AiOperation_Result_AiOperation(AiOperation child) {
         boolean before = child.requiresReconciliationAfterEmergency();
         AiOperation.Result actual = child.tick();
         if (actual.terminal()) {
            boolean unresolved = child.requiresReconciliationAfterEmergency()
               || actual.status() != AiOperation.Status.SUCCEEDED && before
               || init_Z_AiOperation_Result(actual);
            actual = AiGuardLayer.init_AiOperation_Result_Z_AiOperation_Result(unresolved, actual);
            this.init_V_AiOperation(child, unresolved, actual);
         }

         this.init_IdentityHashMap.put(child, actual);
         this.init_AiOperation_Result = actual;
         return actual;
      }

      private static boolean init_Z_AiOperation_Result(AiOperation.Result result) {
         if (result == null) {
            return false;
         }

         JsonObject data = AiRunResources.init_JsonObject(result.dataJson());
         return AiJson.init_Z_JsonObject(data, "interruptedActionNeedsReconciliation", false) || AiJson.init_Z_JsonObject(data, "reobserveBeforeResuming", false);
      }

      private void init_V_AiOperation(AiOperation child, boolean unresolved, AiOperation.Result actual) {
         if (unresolved || init_Z_AiOperation_Result(actual)) {
            this.init_Z = true;
            if (this.init_AiOperation == null) {
               this.init_AiOperation = child;
            }

            if (child == this.init_AiOperation && actual != null) {
               this.load = actual;
            }
         }
      }

      void init_V_AiOperationArr(AiOperation... children) {
         for (AiOperation child : children) {
            if (child != null) {
               this.init_V_AiOperation(child, child.requiresReconciliationAfterEmergency(), this.init_IdentityHashMap.get(child));
            }
         }
      }

      void init_V_AiOperation(AiOperation child) {
         if (child != null) {
            this.init_V_AiOperationArr(child);
            child.cancel();
            this.init_IdentityHashMap.remove(child);
         }
      }

      boolean init_Z() {
         return this.init_Z;
      }

      void init_V_JsonObject(JsonObject data) {
         if (this.init_AiOperation_Result != null) {
            data.add("dimensionChild", AiRunResources.load_JsonObject(this.init_AiOperation_Result.dataJson()));
            data.addProperty("dimensionChildStatus", this.init_AiOperation_Result.status().name());
         }

         if (this.load != null) {
            data.add("pendingChild", AiRunResources.load_JsonObject(this.load.dataJson()));
            data.addProperty("pendingChildStatus", this.load.status().name());
            data.addProperty("pendingChildFailure", this.load.failure().name());
         }
      }

      AiOperation.Result init_AiOperation_Result_AiOperation_Result_Z(AiOperation.Result boundary, boolean interactionPending) {
         JsonObject data = AiRunResources.init_JsonObject(boundary.dataJson());
         this.init_V_JsonObject(data);
         AiOperation.Result actual = this.init_AiOperation_Result != null ? this.init_AiOperation_Result : this.load;
         AiOperation.Result result = AiNetherRun.init_AiOperation_Result_AiOperation_Status(
            boundary.status(), boundary.message(), data, this.init_Z || interactionPending, actual
         );
         return new AiOperation.Result(
            result.status(),
            result.message(),
            result.dataJson(),
            actual == null ? boundary.evidence() : actual.evidence(),
            actual != null && actual.failure() != AiFailure.value ? actual.failure() : boundary.failure()
         );
      }
   }
}

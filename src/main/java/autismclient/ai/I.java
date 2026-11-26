package autismclient.ai;

import autismclient.AutismClientAddon;
import autismclient.ai.a.HashUtil;
import autismclient.api.ai.AiOperation;
import autismclient.compat263.Compat263;
import com.google.gson.JsonObject;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.ItemTags;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResult.Fail;
import net.minecraft.world.InteractionResult.Success;
import net.minecraft.world.InteractionResult.SwingSource;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;

final class I {
   static final int init = 6;
   static final int load = 20;

   private I() {
   }

   static I.Inner_DataUtil init(BlockState ground, BlockState above, Block crop, boolean seeds) {
      boolean open = above.isAir();
      if (above.is(crop)) {
         return I.Inner_DataUtil.read;
      } else if (autismclient.util.FarmCropCatalog.init_Z_Block_BlockState(crop, ground)) {
         return !open ? I.Inner_DataUtil.write : (seeds ? I.Inner_DataUtil.load : I.Inner_DataUtil.read);
      } else if (crop != Blocks.NETHER_WART && (ground.is(Blocks.GRASS_BLOCK) || ground.is(Blocks.DIRT) || ground.is(Blocks.DIRT_PATH))) {
         return open ? I.Inner_DataUtil.init : I.Inner_DataUtil.write;
      } else {
         return I.Inner_DataUtil.write;
      }
   }

   static List<Vec3> init(BlockPos origin, int width, int length) {
      List<Vec3> out = new ArrayList<>();
      int bands = (length + 8) / 9;

      for (int b = 0; b < bands; b++) {
         int z0 = b * 9;
         int z1 = Math.min(length - 1, z0 + 8);
         double z = origin.getZ() + (z0 + z1) / 2 + 0.5;
         double west = origin.getX() - 0.5;
         double east = origin.getX() + width + 0.5;
         if (b % 2 == 0) {
            out.add(new Vec3(west, origin.getY() + 1, z));
            out.add(new Vec3(east, origin.getY() + 1, z));
         } else {
            out.add(new Vec3(east, origin.getY() + 1, z));
            out.add(new Vec3(west, origin.getY() + 1, z));
         }
      }

      return out;
   }

   static List<Vec3> init(int minX, int minZ, int maxX, int maxZ, int y) {
      List<Vec3> out = new ArrayList<>();
      int row = 0;

      for (int z = minZ + 1; z <= maxZ + 1; row++) {
         double lineZ = Math.min(z, maxZ) + 0.5;
         double west = minX - 0.5;
         double east = maxX + 1.5;
         if (row % 2 == 0) {
            out.add(new Vec3(west, y, lineZ));
            out.add(new Vec3(east, y, lineZ));
         } else {
            out.add(new Vec3(east, y, lineZ));
            out.add(new Vec3(west, y, lineZ));
         }

         z += 3;
      }

      return out;
   }

   static AiOperation init(List<BlockPos> field, String cropBlock, String seedItem, String cropItem, int target) {
      return init(field, cropBlock, seedItem, cropItem, target, true);
   }

   static AiOperation init(final List<BlockPos> field, String cropBlock, final String seedItem, final String cropItem, final int target, final boolean replant) {
      if (field.isEmpty()) {
         throw new IllegalArgumentException("Choose a nonempty field");
      }

      final Minecraft mc = Minecraft.getInstance();
      final Block crop = BuiltInRegistries.BLOCK.getValue(Identifier.parse(cropBlock));
      int minX = 2147483647;
      int minZ = 2147483647;
      int maxX = -2147483648;
      int maxZ = -2147483648;

      for (BlockPos p : field) {
         minX = Math.min(minX, p.getX());
         minZ = Math.min(minZ, p.getZ());
         maxX = Math.max(maxX, p.getX());
         maxZ = Math.max(maxZ, p.getZ());
      }

      final List<Vec3> path = H.init(field, 3, mc.player.blockPosition());
      final int minXf = minX;
      final int minZf = minZ;
      final int maxXf = maxX;
      final int maxZf = maxZ;
      final int yf = field.getFirst().getY() + 1;
      return new AiOperation() {
         final autismclient.ai.a.DataUtil init_DataUtil = new autismclient.ai.a.DataUtil();
         final Map<Long, Integer> init_Map;
         final Map<Long, Integer> load_Map;
         boolean init_Z;
         boolean load_Z;
         int init_I;
         final int load_I;
         final Item init_Item;
         final boolean read_Z;
         int read_I;
         int write_I;
         int update_I;
         int sync;
         int flush;
         int close;
         int open;
         int poll;
         int push;
         int pop;
         H.Inner_StreamUtil init_H_StreamUtil;
         final Map<Long, Long> read_Map;
         final Set<Long> init_Set;
         AiOperation init_AiOperation;
         boolean write_Z;
         boolean update_Z;
         Vec3 init_Vec3;
         final Set<Long> load_Set;

         {
            this.init_DataUtil.init_V_DataUtil_DataUtil(autismclient.ai.a.DataUtil.Inner_DataUtil.init_DataUtil_DataUtil);
            this.init_Map = new HashMap<>();
            this.load_Map = new HashMap<>();
            this.load_I = mc.player.getInventory().getSelectedSlot();
            this.init_Item = mc.player.getMainHandItem().getItem();
            this.read_Z = mc.player.getMainHandItem().isDamageableItem();
            this.read_I = 1;
            this.read_Map = new HashMap<>();
            this.init_Set = new HashSet<>();
            this.load_Set = new HashSet<>();
         }

         @Override
         public AiOperation.Result tick() {
            if (!this.update_Z && mc.player != null && mc.level != null) {
               this.update_I++;
               if (mc.gui.screen() != null) {
                  autismclient.util.BlockAttackGraceWindow.init();
                  this.load_V_2();
                  this.init_DataUtil.init_V();
                  this.load_Z = false;
                  this.init_H_StreamUtil = null;
                  return this.init_AiOperation_Result_4("Harvest paused while a screen is open");
               }

               this.init_V_4();
               if (!this.init_Z && this.poll >= 3 && H.init(field)) {
                  path.clear();
                  path.addAll(H.init(field, 1, mc.player.blockPosition()));
                  this.read_I = 1;
                  this.init_Z = true;
                  this.load_Z = false;
               }

               for (long key : this.load_Set) {
                  ca.Inner_DataUtil update = ca.init_ca_DataUtil_BlockPos(BlockPos.of(key).above());
                  boolean breakConfirmed = update != null && update.revision() > this.read_Map.get(key) && update.state().isAir();
                  int since = this.load_Map.getOrDefault(key, this.init_Map.getOrDefault(key, this.update_I));
                  if ((!breakConfirmed || this.load_Map.containsKey(key)) && this.update_I - since > 120) {
                     return this.init(AiOperation.Status.FAILED, "Server has not confirmed the crop/replant at " + BlockPos.of(key).above().toShortString());
                  }
               }

               List<ItemEntity> lying = this.init_List_3();
               int onGround = 0;

               for (ItemEntity item : lying) {
                  if (cropItem.equals(BuiltInRegistries.ITEM.getKey(item.getItem().getItem()).toString())) {
                     onGround += item.getItem().getCount();
                  }
               }

               int held = S.init_I_Str(cropItem);
               boolean enough = held + onGround >= target;
               if (!enough && !this.init_Z_2(cropItem)) {
                  return this.init(AiOperation.Status.FAILED, "Inventory full: make room for " + cropItem + " before harvesting");
               }

               if (held >= target && this.load_Set.isEmpty() && lying.isEmpty()) {
                  return ++this.pop < 20
                     ? this.init_AiOperation_Result_4("Harvest: confirming the final drops")
                     : this.init(
                        AiOperation.Status.SUCCEEDED,
                        "Harvest inventory target reached; "
                           + this.flush
                           + " replanted, "
                           + this.poll
                           + " server replants, "
                           + this.load_Set.size()
                           + " unresolved; drops picked up"
                     );
               }

               this.pop = 0;
               if (!this.write_Z) {
                  if (this.init_AiOperation == null) {
                     this.init_AiOperation = HashUtil.init_AiOperation_BlockPos(BlockPos.containing(path.getFirst()), 1.2, false, false, false, 60);
                  }

                  AiOperation.Result r = this.init_AiOperation.tick();
                  if (!r.terminal()) {
                     return this.init_AiOperation_Result_4("Walking to the field: " + r.message());
                  }

                  this.init_AiOperation.cancel();
                  this.init_AiOperation = null;
                  this.write_Z = true;
               }

               this.load_V_2();
               if (this.init_AiOperation != null) {
                  AiOperation.Result r = this.init_AiOperation.tick();
                  if (!r.terminal()) {
                     return this.init_AiOperation_Result_4("Walking around the field obstruction: " + r.message());
                  }

                  this.init_AiOperation.cancel();
                  this.init_AiOperation = null;
                  if (r.status() != AiOperation.Status.SUCCEEDED) {
                     return this.init(AiOperation.Status.FAILED, "No safe walking route around the field obstruction");
                  }
               }

               af.Inner_DataUtil seed = seedItem == null ? null : af.load_af_DataUtil(seedItem);
               boolean seedAvailable = seed != null && S.init_I_Str(seedItem) > 0;
               Vec3 eye = mc.player.getEyePosition();
               double reach = mc.player.blockInteractionRange() - 0.15;
               Vec3 heading = this.init_Vec3();
               BlockPos best = null;
               BlockHitResult bestHit = null;
               boolean bestBreak = false;
               double bestKey = 1.7976931348623157E308;
               int ripe = 0;
               int behind = 0;
               boolean fixedRow = this.init_Z && this.read_I % 2 == 1;
               if (fixedRow && !this.load_Z) {
                  float yaw = Math.abs(heading.x) > Math.abs(heading.z) ? 0.0F : 90.0F;
                  this.init_DataUtil.init_V_ce_DataUtil(new autismclient.util.RotationMath.Inner_DataUtil(yaw, 80.0F));
                  this.load_Z = true;
               }

               BlockHitResult currentRay = autismclient.ai.a.DataUtil.init_BlockHitResult();

               for (BlockPos ground : field) {
                  BlockPos at = ground.above();
                  BlockState state = mc.level.getBlockState(at);
                  long key = ground.asLong();
                  boolean ripeHere = !enough && !this.load_Set.contains(key) && state.is(crop) && B.init_Z(state);
                  boolean sow = replant
                     && !ripeHere
                     && seedAvailable
                     && state.isAir()
                     && this.init(ground)
                     && autismclient.util.FarmCropCatalog.init_Z_Block_BlockState(crop, mc.level.getBlockState(ground));
                  if (ripeHere || sow) {
                     if (ripeHere) {
                        ripe++;
                     }

                     double along = (ground.getX() + 0.5 - mc.player.getX()) * heading.x + (ground.getZ() + 0.5 - mc.player.getZ()) * heading.z;
                     if (along < 0.6) {
                        behind++;
                     }

                     BlockPos clickPos = ripeHere ? at : ground;
                     if (!(eye.distanceToSqr(Vec3.atCenterOf(clickPos)) > (reach + 1.0) * (reach + 1.0))) {
                        boolean onRay = currentRay != null
                           && currentRay.getBlockPos().equals(clickPos)
                           && (ripeHere || currentRay.getDirection() == Direction.UP);
                        if (H.init(this.init_Z, this.read_I, onRay)) {
                           BlockHitResult hit = onRay
                              ? currentRay
                              : (ripeHere ? autismclient.ai.a.CachePool.init(mc, at) : autismclient.ai.a.CachePool.init(mc, ground, Direction.UP));
                           if (hit != null && !(eye.distanceTo(hit.getLocation()) > reach)) {
                              double score = onRay ? -1.0E9 : along * 4.0 + eye.distanceToSqr(hit.getLocation()) * 0.1;
                              if (score < bestKey) {
                                 bestKey = score;
                                 best = clickPos;
                                 bestHit = hit;
                                 bestBreak = ripeHere;
                              }
                           }
                        }
                     }
                  }
               }

               boolean acted = false;
               if (best != null) {
                  H.Inner_StreamUtil nextGoal = H.Inner_StreamUtil.of(best, bestBreak ? bestHit.getDirection() : Direction.UP);
                  if (H.init(this.init_H_StreamUtil, nextGoal, autismclient.ai.a.DataUtil.init_BlockHitResult())) {
                     this.init_DataUtil.init_V_Vec3(bestHit.getLocation());
                     this.open++;
                     this.load_Z = false;
                  }

                  this.init_H_StreamUtil = nextGoal;
               }

               this.init_DataUtil.load_V();
               if (best != null) {
                  BlockHitResult ray = autismclient.ai.a.DataUtil.init_BlockHitResult();
                  if (ray != null && ray.getBlockPos().equals(best)) {
                     if (bestBreak) {
                        if (this.read_Z && mc.player.getInventory().getItem(this.load_I).is(this.init_Item)) {
                           autismclient.util.InventorySlotHelper.init_V(mc, this.load_I);
                        }

                        long ground = best.below().asLong();
                        long revision = ca.init_J_BlockPos(best);
                        BlockState before = mc.level.getBlockState(best);
                        if (autismclient.ai.a.AiBlockDigger.init_Z_Minecraft(mc, best, ray.getDirection())
                           && bG.init(before, mc.level.getBlockState(best), crop)) {
                           this.sync++;
                           acted = true;
                           if (fixedRow) {
                              this.init_I++;
                           }

                           if (replant && this.load_Set.add(ground)) {
                              this.read_Map.put(ground, revision);
                              this.init_Map.put(ground, this.update_I);
                           }

                           this.init_Set.remove(ground);
                        }
                     } else if (ray.getDirection() == Direction.UP && this.init(best) && mc.level.getBlockState(best.above()).isAir() && this.init(seed)) {
                        for (InteractionHand hand : InteractionHand.values()) {
                           InteractionResult result = mc.gameMode.useItemOn(mc.player, hand, ray);
                           if (result instanceof Success success) {
                              if (success.swingSource() == SwingSource.PREDICTED) {
                                 Compat263.swing(mc.player, hand);
                              }

                              this.flush++;
                              acted = true;
                              this.init_Set.add(best.asLong());
                              this.load_Map.put(best.asLong(), this.update_I);
                              break;
                           }

                           if (result instanceof Fail) {
                              break;
                           }
                        }
                     }
                  }
               }

               if (enough && !lying.isEmpty()) {
                  ItemEntity nearest = lying.stream().min(Comparator.comparingDouble(i -> i.distanceToSqr(mc.player))).orElseThrow();
                  if (behind <= 0 || best == null) {
                     this.init(nearest.position());
                  }

                  Vec3 pos = mc.player.position();
                  boolean moved = this.init_Vec3 != null && pos.distanceToSqr(this.init_Vec3) > 1.0E-4;
                  this.init_Vec3 = pos;
                  this.write_I = !acted && !moved ? this.write_I + 1 : 0;
                  return this.write_I > 60
                     ? this.init(AiOperation.Status.FAILED, "Drops out of reach: " + lying.size() + " left lying")
                     : this.init_AiOperation_Result_4("Harvest: picking up " + lying.size() + " drops");
               } else {
                  Vec3 targetx = path.get(Math.min(this.read_I, path.size() - 1));
                  if (Math.hypot(targetx.x - mc.player.getX(), targetx.z - mc.player.getZ()) < 0.35) {
                     if (this.read_I < path.size() - 1) {
                        this.read_I++;
                     } else {
                        int bare = this.load_I_3();
                        if (ripe == 0 && !this.load_Set.isEmpty()) {
                           boolean waiting = this.load_Set
                              .stream()
                              .anyMatch(key -> autismclient.util.BlockBreakGraceCalc.init(this.update_I, this.init_Map.get(key), this.init_I_4()));
                           if (lying.isEmpty() && bare > 0 && S.init_I_Str(seedItem) <= 0 && !waiting) {
                              return this.init(AiOperation.Status.FAILED, "Harvest needs " + seedItem + " to replant " + bare + " plots");
                           }

                           if (bare == 0 || waiting) {
                              return this.init_AiOperation_Result_4("Harvest: waiting for " + this.load_Set.size() + " server crop confirmations");
                           }
                        }

                        if (ripe == 0 && bare == 0 || ++this.close > 2) {
                           return this.init(
                              ripe == 0 && bare == 0 ? AiOperation.Status.SUCCEEDED : AiOperation.Status.FAILED,
                              ripe == 0 && bare == 0
                                 ? "Field harvested: "
                                    + this.sync
                                    + " broken, "
                                    + this.flush
                                    + " sown, "
                                    + this.poll
                                    + " server replants, "
                                    + this.load_Set.size()
                                    + " unresolved"
                                 : ripe + " ripe crops and " + bare + " bare plots out of reach of the field's lines"
                           );
                        }

                        Collections.reverse(path);
                        this.read_I = 1;
                     }
                  }

                  if (behind <= 0 || best == null) {
                     this.init(targetx);
                  }

                  Vec3 pos = mc.player.position();
                  boolean moved = this.init_Vec3 != null && pos.distanceToSqr(this.init_Vec3) > 1.0E-4;
                  this.init_Vec3 = pos;
                  this.write_I = !acted && !moved ? this.write_I + 1 : 0;
                  return this.write_I > 100
                     ? this.init(AiOperation.Status.FAILED, "Harvest stalled with " + ripe + " ripe crops left")
                     : this.init_AiOperation_Result_4("Harvest: " + this.sync + " broken, " + this.flush + " sown, " + ripe + " ripe left");
               }
            } else {
               return this.init(AiOperation.Status.CANCELLED, "Harvest cancelled");
            }
         }

         void init(Vec3 point) {
            if (!I.init(mc, point) && this.init_AiOperation == null) {
               this.init_AiOperation = HashUtil.init_AiOperation_BlockPos(BlockPos.containing(point), 0.65, false, false, false, 20);
            }
         }

         Vec3 init_Vec3() {
            Vec3 to = path.get(Math.min(this.read_I, path.size() - 1));
            Vec3 from = path.get(Math.max(0, Math.min(this.read_I, path.size() - 1) - 1));
            Vec3 d = new Vec3(to.x - from.x, 0.0, to.z - from.z);
            return d.lengthSqr() < 1.0E-6 ? new Vec3(1.0, 0.0, 0.0) : d.normalize();
         }

         List<ItemEntity> init_List_3() {
            AABB box = new AABB(minXf - 2, yf - 2, minZf - 2, maxXf + 3, yf + 3, maxZf + 3);
            return mc.level.getEntitiesOfClass(ItemEntity.class, box, item -> {
               String id = BuiltInRegistries.ITEM.getKey(item.getItem().getItem()).toString();
               return item.isAlive() && (id.equals(cropItem) || id.equals(seedItem));
            });
         }

         void init_V_4() {
            Iterator<Long> iterator = this.load_Set.iterator();

            while (iterator.hasNext()) {
               long key = iterator.next();
               BlockPos ground = BlockPos.of(key);
               BlockPos above = ground.above();
               boolean ownClick = this.init_Set.contains(key);
               bG.Inner_DataUtil state = bG.init(
                  mc.level.getBlockState(above),
                  ca.init_ca_DataUtil_BlockPos(above),
                  this.read_Map.getOrDefault(key, 0L),
                  crop,
                  ownClick,
                  S.init_I_Str(seedItem)
               );
               if (state == bG.Inner_DataUtil.load || state == bG.Inner_DataUtil.read) {
                  iterator.remove();
                  this.init_Set.remove(key);
                  this.init_Map.remove(key);
                  this.load_Map.remove(key);
                  this.read_Map.remove(key);
                  if (state == bG.Inner_DataUtil.load) {
                     this.push++;
                  } else {
                     this.poll++;
                  }
               }
            }
         }

         boolean init(af.Inner_DataUtil seed) {
            if (seed == null) {
               return false;
            }

            if (seed.matches(mc.player.getMainHandItem())) {
               return true;
            }

            if (seed.matches(mc.player.getOffhandItem())) {
               if (!(mc.player.getMainHandItem().getItem() instanceof BlockItem)) {
                  return true;
               } else {
                  return !autismclient.util.InventorySlotHelper.init_Z(mc, mc.player.getInventory().getSelectedSlot()) ? false : seed.matches(mc.player.getMainHandItem());
               }
            } else {
               return af.read(seed) >= 0 && seed.matches(mc.player.getMainHandItem());
            }
         }

         int init_I_4() {
            ClientPacketListener connection = mc.getConnection();
            PlayerInfo info = connection == null ? null : connection.getPlayerInfo(mc.player.getUUID());
            return info == null ? 0 : info.getLatency();
         }

         boolean init(BlockPos ground) {
            long key = ground.asLong();
            if (this.init_Set.contains(key)) {
               return false;
            } else {
               return !this.load_Set.contains(key)
                  ? true
                  : bG.init(
                     mc.level.getBlockState(ground.above()),
                     ca.init_ca_DataUtil_BlockPos(ground.above()),
                     this.read_Map.get(key),
                     this.update_I,
                     this.init_Map.get(key),
                     this.init_I_4(),
                     false
                  );
            }
         }

         boolean init_Z_2(String itemId) {
            af.Inner_DataUtil wanted = af.load_af_DataUtil(itemId);
            Inventory inventory = mc.player.getInventory();

            for (int slot = 0; slot < 36; slot++) {
               ItemStack stack = inventory.getItem(slot);
               if (stack.isEmpty() || wanted.matches(stack) && stack.getCount() < stack.getMaxStackSize()) {
                  return true;
               }
            }

            return false;
         }

         int load_I_3() {
            if (replant && seedItem != null) {
               int n = 0;

               for (long key : this.load_Set) {
                  BlockPos ground = BlockPos.of(key);
                  if (autismclient.util.FarmCropCatalog.init_Z_Block_BlockState(crop, mc.level.getBlockState(ground)) && mc.level.getBlockState(ground.above()).isAir()) {
                     n++;
                  }
               }

               return n;
            } else {
               return 0;
            }
         }

         void load_V_2() {
            I.init(mc);
         }

         JsonObject init_JsonObject_8() {
            return ag.init_JsonObject_ObjArr(
               "broken",
               this.sync,
               "sown",
               this.flush,
               "confirmedSown",
               this.push,
               "externalReplants",
               this.poll,
               "unresolvedReplants",
               this.load_Set.size(),
               "aimRequests",
               this.open,
               "rowSweep",
               this.init_Z,
               "rowHarvests",
               this.init_I,
               "replant",
               replant,
               "ticks",
               this.update_I
            );
         }

         AiOperation.Result init_AiOperation_Result_4(String message) {
            return R.init_AiOperation_Result(AiOperation.Status.RUNNING, message, this.init_JsonObject_8());
         }

         AiOperation.Result init(AiOperation.Status status, String message) {
            this.cancel();
            return R.init_AiOperation_Result(status, message, this.init_JsonObject_8());
         }

         @Override
         public void cancel() {
            this.update_Z = true;
            if (this.init_AiOperation != null) {
               this.init_AiOperation.cancel();
            }

            autismclient.util.BlockAttackGraceWindow.init();
            if (mc.player != null) {
               this.load_V_2();
            }

            this.init_DataUtil.init_V();
         }
      };
   }

   static boolean init(Minecraft mc, Vec3 target) {
      double dx = target.x - mc.player.getX();
      double dz = target.z - mc.player.getZ();
      if (dx * dx + dz * dz < 0.01) {
         return true;
      }

      double distance = Math.hypot(dx, dz);
      double step = Math.min(0.55, distance);
      double nextX = mc.player.getX() + dx / distance * step;
      double nextZ = mc.player.getZ() + dz / distance * step;

      for (double ox : new double[]{-0.28, 0.28}) {
         for (double oz : new double[]{-0.28, 0.28}) {
            int x = Mth.floor(nextX + ox);
            int z = Mth.floor(nextZ + oz);
            double floor = -1.0 / 0.0;
            int y = Mth.floor(mc.player.getY());

            for (int by = y - 1; by <= y + 1; by++) {
               BlockPos pos = new BlockPos(x, by, z);
               if (!mc.level.hasChunkAt(pos)) {
                  return false;
               }

               VoxelShape shape = mc.level.getBlockState(pos).getCollisionShape(mc.level, pos);
               if (!shape.isEmpty()) {
                  double top = by + shape.max(Axis.Y);
                  if (top <= mc.player.getY() + 0.6 + 1.0E-5) {
                     floor = Math.max(floor, top);
                  }
               }
            }

            if (floor < mc.player.getY() - 0.49) {
               return false;
            }
         }
      }

      double yaw = Math.toRadians(mc.player.getYRot());
      double forward = -Math.sin(yaw) * dx + Math.cos(yaw) * dz;
      double left = Math.cos(yaw) * dx + Math.sin(yaw) * dz;
      double len = Math.hypot(forward, left);
      forward /= len;
      left /= len;
      mc.options.keyUp.setDown(forward > 0.38);
      mc.options.keyDown.setDown(forward < -0.38);
      mc.options.keyLeft.setDown(left > 0.38);
      mc.options.keyRight.setDown(left < -0.38);
      return true;
   }

   static void init(Minecraft mc) {
      mc.options.keyAttack.setDown(false);
      if (mc.gameMode != null && mc.gameMode.isDestroying() && !autismclient.util.BlockAttackGraceWindow.init(mc)) {
         mc.gameMode.stopDestroyBlock();
      }

      mc.options.keyUp.setDown(false);
      mc.options.keyDown.setDown(false);
      mc.options.keyLeft.setDown(false);
      mc.options.keyRight.setDown(false);
      mc.options.keyJump.setDown(false);
      mc.options.keySprint.setDown(false);
      mc.player.setSprinting(false);
   }

   static AiOperation init(final List<BlockPos> plots, final BlockPos origin, final int width, final int length, String cropBlock, final String seedItem) {
      final Minecraft mc = Minecraft.getInstance();
      final Block crop = BuiltInRegistries.BLOCK.getValue(Identifier.parse(cropBlock));
      final List<Vec3> path = H.init(plots, 9, mc.player.blockPosition());
      return new AiOperation() {
         final autismclient.ai.a.DataUtil init_DataUtil = new autismclient.ai.a.DataUtil();
         final Set<Long> init_Set = new HashSet<>();
         int init_I;
         int load_I;
         int read_I;
         int write_I;
         int update;
         int sync;
         int flush;
         int close;
         int open;
         int poll;
         H.Inner_StreamUtil init_H_StreamUtil;
         final Set<Long> load_Set;
         final Set<Long> read_Set;
         double init_D;
         final Set<Integer> write_Set;
         String init_Str;
         AiOperation init_AiOperation;
         boolean init_Z;
         boolean load_Z;
         Vec3 init_Vec3;
         int push;
         int pop;

         {
            for (BlockPos p : plots) {
               this.init_Set.add(p.asLong());
            }

            this.init_DataUtil.init_V_DataUtil_DataUtil(autismclient.ai.a.DataUtil.Inner_DataUtil.write);
            this.init_I = 1;
            this.open = -1;
            this.load_Set = new HashSet<>();
            this.read_Set = new HashSet<>();
            this.write_Set = new HashSet<>();
            this.init_Str = "-";
         }

         @Override
         public AiOperation.Result tick() {
            if (!this.load_Z && mc.player != null && mc.level != null) {
               this.read_I++;
               if (mc.gui.screen() != null) {
                  autismclient.util.BlockAttackGraceWindow.init();
                  this.load();
                  this.init_DataUtil.init_V();
                  return this.init("Field work paused while a screen is open");
               }

               if (!this.init_Z) {
                  if (this.init_AiOperation == null) {
                     this.init_AiOperation = HashUtil.init_AiOperation_BlockPos(BlockPos.containing(path.getFirst()), 1.2, false, false, false, 60);
                  }

                  AiOperation.Result r = this.init_AiOperation.tick();
                  if (!r.terminal()) {
                     return this.init("Walking to the field: " + r.message());
                  }

                  this.init_AiOperation.cancel();
                  this.init_AiOperation = null;
                  this.init_Z = true;
               }

               if (this.init_AiOperation != null) {
                  AiOperation.Result r = this.init_AiOperation.tick();
                  if (!r.terminal()) {
                     return this.init("Walking to a plot the lines missed: " + r.message());
                  }

                  this.init_AiOperation.cancel();
                  this.init_AiOperation = null;
                  this.pop = 20;
               }

               this.load();
               if (!this.init_Z_3()) {
                  return ++this.sync > 40
                     ? this.init(AiOperation.Status.FAILED, "Could not take the hoe and the seeds in hand")
                     : this.init("Taking the hoe and the seeds in hand");
               }

               this.sync = 0;
               this.init_V_5();
               Vec3 eye = mc.player.getEyePosition();
               double reach = mc.player.blockInteractionRange() - 0.15;
               boolean seeds = S.init_I_Str(seedItem) > 0;
               Vec3 heading = this.init_Vec3_2();
               BlockPos best = null;
               BlockHitResult bestHit = null;
               double bestKey = 1.7976931348623157E308;
               int pendingBehind = 0;
               int pending = 0;

               for (BlockPos p : plots) {
                  I.Inner_DataUtil need = I.init(mc.level.getBlockState(p), mc.level.getBlockState(p.above()), crop, seeds);
                  if (need != I.Inner_DataUtil.read && need != I.Inner_DataUtil.write) {
                     pending++;
                     double along = (p.getX() + 0.5 - mc.player.getX()) * heading.x + (p.getZ() + 0.5 - mc.player.getZ()) * heading.z;
                     if (along < 0.6) {
                        pendingBehind++;
                     }

                     Vec3 top = Vec3.atCenterOf(p).add(0.0, 0.5, 0.0);
                     if (!(eye.distanceToSqr(top) > (reach + 1.0) * (reach + 1.0))) {
                        BlockHitResult hit = autismclient.ai.a.CachePool.init(mc, p, Direction.UP);
                        if (hit != null && !(eye.distanceTo(hit.getLocation()) > reach)) {
                           double key = along * 4.0 + eye.distanceToSqr(hit.getLocation()) * 0.1;
                           if (key < bestKey) {
                              bestKey = key;
                              best = p;
                              bestHit = hit;
                           }
                        }
                     }
                  }
               }

               if (pending == 0) {
                  return this.init(AiOperation.Status.SUCCEEDED, "Field tilled and sown: " + this.write_I + " tilled, " + this.update + " sown");
               }

               boolean clicked = false;
               if (best != null) {
                  H.Inner_StreamUtil nextGoal = H.Inner_StreamUtil.of(best, Direction.UP);
                  if (H.init(this.init_H_StreamUtil, nextGoal, autismclient.ai.a.DataUtil.init_BlockHitResult())) {
                     this.init_DataUtil.init_V_Vec3(bestHit.getLocation());
                     this.poll++;
                  }

                  this.init_H_StreamUtil = nextGoal;
               }

               this.init_DataUtil.load_V();
               if (best != null) {
                  BlockHitResult ray = autismclient.ai.a.DataUtil.init_BlockHitResult();
                  if (ray != null && ray.getBlockPos().equals(best) && ray.getDirection() == Direction.UP) {
                     clicked = this.init(best, ray);
                  }
               }

               boolean holdBack = pendingBehind > 0 && best != null;
               Vec3 target = path.get(Math.min(this.init_I, path.size() - 1));
               double gap = Math.hypot(target.x - mc.player.getX(), target.z - mc.player.getZ());
               if (gap < 0.35) {
                  if (this.init_I < path.size() - 1) {
                     this.init_I++;
                  } else if (pending > 0 && !holdBack && best == null) {
                     BlockPos missed = this.flush >= 2 && this.push < 6 ? this.init(seeds) : null;
                     if (missed != null) {
                        this.push++;
                        this.init_AiOperation = HashUtil.init_AiOperation_BlockPos(missed.above(), 1.6, false, false, false, 20);
                        return this.init("Walking to a plot the lines missed at " + missed.toShortString());
                     }

                     if (++this.flush > 2) {
                        StringBuilder left = new StringBuilder();
                        int listed = 0;

                        for (BlockPos p : plots) {
                           I.Inner_DataUtil need = I.init(mc.level.getBlockState(p), mc.level.getBlockState(p.above()), crop, seeds);
                           if (need != I.Inner_DataUtil.read && need != I.Inner_DataUtil.write && listed++ < 6) {
                              left.append(' ')
                                 .append(p.toShortString())
                                 .append(' ')
                                 .append(need)
                                 .append(" (")
                                 .append(mc.level.getBlockState(p).getBlock().getName().getString())
                                 .append(autismclient.ai.a.CachePool.init(mc, p, Direction.UP) == null ? ", top unseen" : "")
                                 .append(')');
                           }
                        }

                        AutismClientAddon.LOG.info("[AI] field: unreached plots from {}:{}", mc.player.blockPosition().toShortString(), left);
                        return this.init(AiOperation.Status.FAILED, pending + " plots could not be reached from the field's lines");
                     }

                     Collections.reverse(path);
                     this.init_I = 1;
                  }
               }

               if (this.pop > 0) {
                  this.pop--;
               } else if (!holdBack) {
                  this.init(target);
               }

               Vec3 pos = mc.player.position();
               boolean moved = this.init_Vec3 != null && pos.distanceToSqr(this.init_Vec3) > 1.0E-4;
               this.init_Vec3 = pos;
               this.load_I = !clicked && !moved ? this.load_I + 1 : 0;
               return this.load_I > 100
                  ? this.init(AiOperation.Status.FAILED, "Field work stalled with " + pending + " plots left")
                  : this.init("Field: " + this.write_I + " tilled, " + this.update + " sown, " + pending + " left");
            } else {
               return this.init(AiOperation.Status.CANCELLED, "Field work cancelled");
            }
         }

         BlockPos init(boolean seeds) {
            BlockPos best = null;
            double bestDistance = 1.7976931348623157E308;

            for (BlockPos p : plots) {
               I.Inner_DataUtil need = I.init(mc.level.getBlockState(p), mc.level.getBlockState(p.above()), crop, seeds);
               if (need != I.Inner_DataUtil.read && need != I.Inner_DataUtil.write) {
                  double d = p.distToCenterSqr(mc.player.position());
                  if (d < bestDistance) {
                     bestDistance = d;
                     best = p;
                  }
               }
            }

            return best;
         }

         void init_V_5() {
            int seedsNow = S.init_I_Str(seedItem);
            boolean debug = Boolean.getBoolean("autism.navigation.debug");
            if (debug && this.open >= 0 && seedsNow > this.open) {
               AutismClientAddon.LOG
                  .info(
                     "[AI] field: seeds rose {} -> {} at {} (ground {})",
                     this.open,
                     seedsNow,
                     mc.player.position(),
                     mc.level.getBlockState(mc.player.blockPosition().below())
                  );
            }

            this.open = seedsNow;
            this.init_D = Math.max(this.init_D, mc.player.fallDistance);
            AABB box = new AABB(
               origin.getX() - 3, origin.getY() - 2, origin.getZ() - 3, origin.getX() + width + 3, origin.getY() + 4, origin.getZ() + length + 3
            );
            if (debug) {
               for (ItemEntity item : mc.level.getEntitiesOfClass(ItemEntity.class, box)) {
                  if (this.write_Set.add(item.getId())) {
                     BlockPos at = item.blockPosition();
                     AutismClientAddon.LOG
                        .info(
                           "[AI] field: item {} x{} appeared at {} (block {}, below {}), last click {}, tick {}",
                           BuiltInRegistries.ITEM.getKey(item.getItem().getItem()).getPath(),
                           item.getItem().getCount(),
                           item.position(),
                           mc.level.getBlockState(at),
                           mc.level.getBlockState(at.below()),
                           this.init_Str,
                           this.read_I
                        );
                  }
               }
            }

            for (BlockPos p : plots) {
               boolean farm = mc.level.getBlockState(p).is(Blocks.FARMLAND);
               if (farm) {
                  this.read_Set.add(p.asLong());
               } else if (this.read_Set.remove(p.asLong())) {
                  AutismClientAddon.LOG
                     .info(
                        "[AI] field: farmland lost at {} -> {}: body {} onGround {} vy {} fall {} (max {}) jump {} sprint {} keys {}{}{}{}",
                        p.toShortString(),
                        mc.level.getBlockState(p),
                        mc.player.position(),
                        mc.player.onGround(),
                        mc.player.getDeltaMovement().y,
                        mc.player.fallDistance,
                        this.init_D,
                        mc.options.keyJump.isDown(),
                        mc.player.isSprinting(),
                        mc.options.keyUp.isDown() ? "W" : "",
                        mc.options.keyLeft.isDown() ? "A" : "",
                        mc.options.keyDown.isDown() ? "S" : "",
                        mc.options.keyRight.isDown() ? "D" : ""
                     );
               }

               boolean has = mc.level.getBlockState(p.above()).is(crop);
               if (has) {
                  this.load_Set.add(p.asLong());
               } else if (this.load_Set.remove(p.asLong())) {
                  this.close++;
                  AutismClientAddon.LOG
                     .info(
                        "[AI] field: crop lost at {}: ground {}, above {}, body {} onGround {} vy {} fall {}",
                        p.toShortString(),
                        mc.level.getBlockState(p),
                        mc.level.getBlockState(p.above()),
                        mc.player.position(),
                        mc.player.onGround(),
                        mc.player.getDeltaMovement().y,
                        mc.player.fallDistance
                     );
               }
            }
         }

         boolean init(BlockPos p, BlockHitResult hit) {
            boolean wasTill = crop != Blocks.NETHER_WART && !mc.level.getBlockState(p).is(Blocks.FARMLAND);

            for (InteractionHand hand : InteractionHand.values()) {
               InteractionResult result = mc.gameMode.useItemOn(mc.player, hand, hit);
               this.init_Str = p.toShortString()
                  + " "
                  + hand
                  + " "
                  + result.getClass().getSimpleName()
                  + " "
                  + mc.player.getItemInHand(hand).getItem()
                  + " tick "
                  + this.read_I;
               if (result instanceof Success success) {
                  if (success.swingSource() == SwingSource.PREDICTED) {
                     Compat263.swing(mc.player, hand);
                  }

                  if (wasTill) {
                     this.write_I++;
                  } else {
                     this.update++;
                  }

                  return true;
               }

               if (result instanceof Fail) {
                  return false;
               }
            }

            return false;
         }

         boolean init_Z_3() {
            af.Inner_DataUtil seed = af.load_af_DataUtil(seedItem);
            if (crop == Blocks.NETHER_WART) {
               if (seed.matches(mc.player.getMainHandItem()) || seed.matches(mc.player.getOffhandItem())) {
                  return true;
               }

               if (S.init_I_Str(seedItem) <= 0) {
                  return true;
               }

               if (af.read(seed) >= 0) {
                  return true;
               }

               af.init_Z(seed);
               return false;
            } else if (!mc.player.getMainHandItem().is(ItemTags.HOES) && af.read(af.load_af_DataUtil("hoe")) < 0) {
               return false;
            } else if (!seed.matches(mc.player.getOffhandItem()) && S.init_I_Str(seedItem) > 0) {
               af.init_Z(seed);
               return false;
            } else {
               return mc.player.getMainHandItem().is(ItemTags.HOES);
            }
         }

         Vec3 init_Vec3_2() {
            Vec3 to = path.get(Math.min(this.init_I, path.size() - 1));
            Vec3 from = path.get(Math.max(0, Math.min(this.init_I, path.size() - 1) - 1));
            Vec3 d = new Vec3(to.x - from.x, 0.0, to.z - from.z);
            return d.lengthSqr() < 1.0E-6 ? new Vec3(1.0, 0.0, 0.0) : d.normalize();
         }

         void init(Vec3 target) {
            if (!I.init(mc, target) && this.init_AiOperation == null) {
               this.init_AiOperation = HashUtil.init_AiOperation_BlockPos(BlockPos.containing(target), 0.65, false, false, false, 20);
            }
         }

         void load() {
            I.init(mc);
         }

         AiOperation.Result init(String message) {
            return R.init_AiOperation_Result(AiOperation.Status.RUNNING, message, this.init_JsonObject_9());
         }

         AiOperation.Result init(AiOperation.Status status, String message) {
            this.cancel();
            return R.init_AiOperation_Result(status, message, this.init_JsonObject_9());
         }

         JsonObject init_JsonObject_9() {
            return ag.init_JsonObject_ObjArr("tilled", this.write_I, "sown", this.update, "lost", this.close, "ticks", this.read_I, "aimRequests", this.poll);
         }

         @Override
         public void cancel() {
            this.load_Z = true;
            if (this.init_AiOperation != null) {
               this.init_AiOperation.cancel();
            }

            autismclient.util.BlockAttackGraceWindow.init();
            if (mc.player != null) {
               this.load();
            }

            this.init_DataUtil.init_V();
         }
      };
   }

   enum Inner_DataUtil {
      init,
      load,
      read,
      write;
   }
}

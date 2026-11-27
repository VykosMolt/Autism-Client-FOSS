package autismclient.ai;

import autismclient.ai.engine.AiGoto;
import autismclient.ai.engine.AiFallGuard;
import autismclient.api.ai.AiOperation;
import autismclient.compat263.Compat263;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.boss.enderdragon.EndCrystal;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.entity.boss.enderdragon.phases.DragonPhaseInstance;
import net.minecraft.world.entity.boss.enderdragon.phases.EnderDragonPhase;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

final class AiCrystals {
   static final double init_D = 64.0;
   static final float init_F = 4.0F;
   static final double load_D = 24.0;
   static final double read_D = 14.0;
   static final double write_D = 6.0;
   static final double update_D = 0.7;
   static final int init_I = 3;
   static final int load_I = 3;
   static final int read_I = 40;
   static final int write_I = 25;
   static final int update_I = 40;
   static final int sync = 2;

   private AiCrystals() {
   }

   static boolean init(AiCrystals.Inner_StreamUtil phase, boolean towerRoute) {
      return phase == AiCrystals.Inner_StreamUtil.PILLAR
         || phase == AiCrystals.Inner_StreamUtil.OPEN_CAGE
         || phase == AiCrystals.Inner_StreamUtil.DESCEND
         || towerRoute && (phase == AiCrystals.Inner_StreamUtil.MELEE || phase == AiCrystals.Inner_StreamUtil.SHOOT);
   }

   static boolean init(double damage, double health, boolean lethal, boolean voidFall) {
      return !lethal && !voidFall && Double.isFinite(damage) && damage >= 0.0 && damage <= 4.0 && damage < health;
   }

   static boolean init(boolean fullCollision, AABB cover, Vec3 blast, Vec3 body) {
      return fullCollision && cover.clip(blast, body).isPresent();
   }

   static boolean init_Z_I(int feetY, int groundY, boolean supported) {
      return supported && feetY <= groundY + 1;
   }

   static boolean init_Z_h_StreamUtil(AiCrystals.Inner_StreamUtil phase, boolean towerRoute, boolean settling, boolean legSafe, boolean guardSafe) {
      return !init(phase, towerRoute) && !settling && legSafe && guardSafe;
   }

   static boolean init_Z_Z(boolean settling, boolean legPending, boolean guardPending) {
      return settling || legPending || guardPending;
   }

   static boolean init(Minecraft mc, BlockPos cell) {
      List<String> nearby = new ArrayList<>(32);

      for (int dx = -2; dx <= 2; dx++) {
         for (int dy = -2; dy <= 2; dy++) {
            for (int dz = -2; dz <= 2; dz++) {
               if (dx != 0 || dy != 0 || dz != 0) {
                  nearby.add(BuiltInRegistries.BLOCK.getKey(mc.level.getBlockState(cell.offset(dx, dy, dz)).getBlock()).getPath());
               }
            }
         }
      }

      return init_Z(nearby);
   }

   static boolean init_Z(List<String> nearbyBlockPaths) {
      for (String path : nearbyBlockPaths) {
         if ("iron_bars".equals(path)) {
            return true;
         }
      }

      return false;
   }

   static List<AiCrystals.Inner_DataUtil> init_List(List<AiCrystals.Inner_DataUtil> targets) {
      List<AiCrystals.Inner_DataUtil> ordered = new ArrayList<>(targets);
      ordered.sort(
         Comparator.comparing(AiCrystals.Inner_DataUtil::caged).thenComparingDouble(AiCrystals.Inner_DataUtil::distanceSqr).thenComparingInt(AiCrystals.Inner_DataUtil::entityId)
      );
      return ordered;
   }

   static boolean init_Z_I(int bows, int crossbows, int arrows, int tridents, int throwables) {
      return bows + crossbows > 0 && arrows > 0 || tridents > 0 || throwables > 0;
   }

   static String init(boolean caged, boolean meleeAdjacent, boolean blastSafe, boolean ranged) {
      if (caged) {
         return "cage";
      } else if (meleeAdjacent && blastSafe) {
         return "melee";
      } else if (ranged) {
         return "shoot";
      } else {
         return AiThreats.isLegacyCombatEnabled() ? null : "cage";
      }
   }

   static boolean init_Z_D(double horizontal, double dy, double distance) {
      return distance > 64.0 || horizontal < Math.max(6.0, dy * 0.7);
   }

   static int[] init_IArr_I(int cx, int cz, double dx, double dz, double dy) {
      double len = Math.hypot(dx, dz);
      double ux = len < 1.0E-6 ? 1.0 : dx / len;
      double uz = len < 1.0E-6 ? 0.0 : dz / len;
      double d = Math.max(12.0, dy + 6.0);
      return new int[]{(int)Math.round(cx + ux * d), (int)Math.round(cz + uz * d)};
   }

   static int[] init(double dx, double dz) {
      return Math.abs(dx) >= Math.abs(dz) ? new int[]{dx >= 0.0 ? 1 : -1, 0} : new int[]{0, dz >= 0.0 ? 1 : -1};
   }

   static String init_Str(List<String> hotbarItemPaths) {
      return hotbarItemPaths.contains("water_bucket") ? "mlg" : "dig";
   }

   static JsonObject init() {
      JsonObject missing = new JsonObject();
      missing.addProperty("minecraft:bow", 1);
      missing.addProperty("minecraft:arrow", 8);
      return missing;
   }

   private static List<String> init(LocalPlayer player) {
      List<String> out = new ArrayList<>(9);

      for (int i = 0; i < 9; i++) {
         out.add(BuiltInRegistries.ITEM.getKey(player.getInventory().getItem(i).getItem()).getPath());
      }

      return out;
   }

   static AiOperation init(JsonObject args) {
      final Minecraft mc = Minecraft.getInstance();
      if (mc.player != null && mc.level != null) {
         final int timeoutSeconds = Math.max(30, Math.min(1800, AiJson.getInt(args, "timeoutSeconds", 600)));
         final boolean skipCaged = AiJson.getBoolean(args, "skipCaged", false);
         AiService service = AiService.getExistingInstance();
         final boolean silentAim = service != null && service.getSettings().aiSilentAim;
         return new AiOperation() {
            final long init_J = System.nanoTime() + timeoutSeconds * 1000000000L;
            final autismclient.ai.engine.AiAim init_DataUtil = new autismclient.ai.engine.AiAim();
            final AiGuard.GuardPolicy init_X_CachePool = AiGuard.GuardPolicy.standard();
            final JsonArray init_JsonArray = new JsonArray();
            final Set<Integer> init_Set = new LinkedHashSet<>();
            final AiCrystals.Inner_CachePool init_h_CachePool = new AiCrystals.Inner_CachePool();
            AiOperation init_AiOperation;
            AiOperation load_AiOperation;
            AiCrystals.Inner_DataUtil init_h_DataUtil;
            AiCrystals.Inner_StreamUtil init_h_StreamUtil = AiCrystals.Inner_StreamUtil.SELECT;
            String init_Str = "";
            String load_Str = "";
            String read_Str;
            JsonObject init_JsonObject;
            BlockPos init_BlockPos;
            BlockPos load_BlockPos;
            BlockPos read_BlockPos;
            boolean init_Z;
            int init_I;
            int load_I;
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
            int tick;
            boolean load_Z;
            boolean read_Z;
            boolean write_Z;
            boolean update_Z;

            {
               this.init_DataUtil.setProfile(autismclient.ai.engine.AiAim.AimProfile.CALM);
               this.init_DataUtil.setSilent(silentAim);
            }

            @Override
            public AiOperation.Result tick() {
               if (!this.write_Z && mc.player != null && mc.level != null) {
                  if (System.nanoTime() > this.init_J) {
                     return this.init(
                        this.canInterruptNow() ? AiOperation.Status.TIMED_OUT : AiOperation.Status.UNCERTAIN,
                        AiFailure.TIMEOUT,
                        "Crystal clearing deadline reached with " + this.init_I + " cleared"
                     );
                  }

                  if (!AiThreats.isLegacyCombatEnabled()
                     && this.init_Z_26()
                     && this.init_h_StreamUtil != AiCrystals.Inner_StreamUtil.DESCEND
                     && this.init_h_DataUtil != null
                     && mc.player.getHealth() < 10.0F) {
                     this.init_V_30();
                     return this.init(this.init_h_DataUtil, "heal_first");
                  }

                  if (!this.init_Z_26()) {
                     if (mc.player.getHealth() <= 4.0F && !AiGuard.isLowHealthOverrideActive()) {
                        return this.init(AiFailure.LOW_HEALTH, AiGuard.formatLowHealthAbort("crystal clearing", mc.player.getHealth(), mc.player.getMaxHealth()));
                     }

                     AiOperation.Result guarded = this.init_AiOperation_Result_40();
                     if (guarded != null) {
                        return guarded;
                     }
                  }

                  if (!AiThreats.isLegacyCombatEnabled()
                     && this.load_Z
                     && this.init_h_DataUtil != null
                     && mc.player.getY() > this.flush + 4
                     && (
                        this.init_h_StreamUtil == AiCrystals.Inner_StreamUtil.PILLAR
                           || this.init_h_StreamUtil == AiCrystals.Inner_StreamUtil.OPEN_CAGE
                           || this.init_h_StreamUtil == AiCrystals.Inner_StreamUtil.MELEE
                     )
                     && this.init(14.0, 40)) {
                     this.init_V_30();
                     AiSpeedrun.init_V_Str_ObjArr("[AI] crystals: the dragon is near; off the tower at {}", mc.player.blockPosition().toShortString());
                     return this.init(this.init_h_DataUtil, "dragon_near");
                  }

                  AiOperation.Result var10000;
                  switch (this.init_h_StreamUtil) {
                     case SELECT:
                        var10000 = this.load_AiOperation_Result_17();
                        break;
                     case APPROACH:
                        var10000 = this.read_AiOperation_Result_12();
                        break;
                     case SHOOT:
                        var10000 = this.write_AiOperation_Result_7();
                        break;
                     case MELEE:
                        var10000 = this.update();
                        break;
                     case PILLAR:
                        var10000 = this.sync();
                        break;
                     case OPEN_CAGE:
                        var10000 = this.flush();
                        break;
                     case DESCEND:
                        var10000 = this.close();
                        break;
                     default:
                        throw new MatchException(null, null);
                  }

                  return var10000;
               } else {
                  return this.init(AiOperation.Status.CANCELLED, AiFailure.CANCELLED, "Crystal clearing cancelled");
               }
            }

            boolean init_Z_26() {
               return AiCrystals.init(this.init_h_StreamUtil, this.load_Z && this.init_h_DataUtil != null);
            }

            AiOperation.Result init_AiOperation_Result_40() {
               if (this.load_AiOperation != null) {
                  AiOperation.Result result = this.load_AiOperation.tick();
                  if (!result.terminal()) {
                     return this.load("Interrupted: " + this.load_Str + " - " + result.message());
                  }

                  this.load_AiOperation.cancel();
                  this.load_AiOperation = null;
                  return this.load("Resuming after " + this.load_Str);
               } else {
                  AiGuard.StatusSnapshot vitals = AiGuard.sampleStatus();
                  AiOperation action = AiGuard.createResponseOperation(this.init_X_CachePool, vitals, this.load_I_14());
                  if (action == null) {
                     return null;
                  }

                  this.load_Str = AiGuard.decisionReason(this.init_X_CachePool, vitals);
                  this.load_I++;
                  this.init_V_30();
                  this.load_AiOperation = action;
                  return this.load("Interrupted: " + this.load_Str);
               }
            }

            AiOperation.Result load_AiOperation_Result_17() {
               List<AiCrystals.Inner_DataUtil> targets = AiCrystals.init_List(this.init_List_9());
               this.pop = targets.size();
               AiCrystals.Inner_DataUtil next = null;

               for (AiCrystals.Inner_DataUtil t : targets) {
                  if (!this.init_Set.contains(t.entityId())) {
                     next = t;
                     break;
                  }
               }

               if (next == null) {
                  if (targets.isEmpty()) {
                     if (this.init_I > 0) {
                        AiRunTrace.recordGlobalSplit("clear_crystals", this.init_I + " cleared");
                     }

                     return this.init(
                        AiOperation.Status.SUCCEEDED, null, this.init_I == 0 ? "No End crystals observed" : "All End crystals cleared (" + this.init_I + ")"
                     );
                  } else {
                     return this.push();
                  }
               } else {
                  this.init_h_DataUtil = next;
                  this.init_Str = "";
                  this.load_Z = false;
                  this.read_Z = false;
                  this.read_I = 0;
                  this.write_I = 0;
                  this.update_I = 0;
                  this.sync = 0;
                  this.poll = 0;
                  this.push = 0;
                  this.init_BlockPos = null;
                  this.load_BlockPos = null;
                  this.read_BlockPos = null;
                  this.init_Z = false;
                  this.tick = -1;
                  Entity entity = this.init_Entity_h_DataUtil(next);
                  if (entity == null) {
                     this.init_Set.add(next.entityId());
                     return this.load("Crystal vanished; re-selecting");
                  }

                  String chosen = AiCrystals.init(next.caged(), this.init_Z_Entity_3(entity), this.read_Z_11(), this.write_Z_8());
                  AiSpeedrun.init_V_Str_ObjArr(
                     "[AI] crystals: target #{} at {} {} {} caged={} invulnerable={} method={} arrows={}",
                     next.entityId(),
                     next.x(),
                     next.y(),
                     next.z(),
                     next.caged(),
                     entity.isInvulnerable(),
                     chosen,
                     AiRunResources.totalArrowCount()
                  );
                  if (chosen == null) {
                     return this.init(next, "shoot", "no_ranged");
                  }

                  if ("cage".equals(chosen) && skipCaged) {
                     return this.init(next, "cage", "caged_skipped");
                  }

                  if ("cage".equals(chosen) && !AiThreats.isLegacyCombatEnabled() && mc.player.getHealth() < 16.0F) {
                     return this.init(next, "cage", "heal_first");
                  }

                  if ("cage".equals(chosen) && !AiThreats.isLegacyCombatEnabled() && this.init(24.0, 60)) {
                     return this.init(next, "cage", "dragon_near");
                  }

                  if ("cage".equals(chosen)) {
                     this.load_Z = true;
                     this.flush = mc.player.blockPosition().getY();
                     int shortage = next.y() + 2 - this.flush - this.init_I_36();
                     if (shortage > 0) {
                        this.open = Math.max(this.open, shortage);
                        return this.init(next, "cage", "no_blocks");
                     } else {
                        this.init_h_StreamUtil = AiCrystals.Inner_StreamUtil.PILLAR;
                        return this.load("Pillaring up beside the cage at " + next.x() + " " + next.y() + " " + next.z());
                     }
                  } else {
                     this.init_Str = chosen;
                     if ("melee".equals(chosen)) {
                        this.init_h_StreamUtil = AiCrystals.Inner_StreamUtil.MELEE;
                        return this.load("Punching the adjacent crystal");
                     } else {
                        this.init_h_StreamUtil = this.read(entity) ? AiCrystals.Inner_StreamUtil.APPROACH : AiCrystals.Inner_StreamUtil.SHOOT;
                        return this.load("Shooting crystal #" + next.entityId());
                     }
                  }
               }
            }

            AiOperation.Result read_AiOperation_Result_12() {
               if (this.init_h_DataUtil == null) {
                  this.init_h_StreamUtil = AiCrystals.Inner_StreamUtil.SELECT;
                  return this.load("Re-selecting");
               }

               Entity entity = this.init_Entity_h_DataUtil(this.init_h_DataUtil);
               if (entity == null) {
                  return this.poll();
               }

               if (this.init_AiOperation == null) {
                  BlockPos stand = this.init_BlockPos_Entity_2(entity);
                  if (stand == null) {
                     this.init_h_StreamUtil = AiCrystals.Inner_StreamUtil.SHOOT;
                     return this.load("No firing cell in loaded terrain; shooting from here");
                  }

                  this.init_BlockPos = stand;
                  this.init_AiOperation = AiGoto.travelToCompat(
                     stand, 1.5, false, true, true, this.init_I_I_4(120), autismclient.ai.engine.AiPathfinder.RouteStrategy.FASTEST, true
                  );
               }

               AiOperation.Result r = this.init_AiOperation.tick();
               if (!r.terminal()) {
                  return this.load("Moving to a firing position");
               } else {
                  this.init_V_30();
                  if (r.status() == AiOperation.Status.SUCCEEDED) {
                     this.init_h_StreamUtil = AiCrystals.Inner_StreamUtil.SHOOT;
                     return this.load("In position; solving the arc");
                  } else {
                     return this.load(this.init_h_DataUtil, "shoot", "unreachable");
                  }
               }
            }

            AiOperation.Result write_AiOperation_Result_7() {
               if (this.init_h_DataUtil == null) {
                  this.init_h_StreamUtil = AiCrystals.Inner_StreamUtil.SELECT;
                  return this.load("Re-selecting");
               }

               if (this.init_Entity_h_DataUtil(this.init_h_DataUtil) == null) {
                  return this.poll();
               }

               if (this.read_Z) {
                  if (++this.update_I > 40) {
                     this.read_Z = false;
                     return ++this.read_I >= 3
                        ? this.load(this.init_h_DataUtil, this.init_Str, "shots_missed")
                        : this.load("Shot did not land; firing again (" + this.read_I + "/3)");
                  } else {
                     return this.load("Waiting for the shot to land");
                  }
               } else {
                  if (this.init_AiOperation == null) {
                     this.init_AiOperation = AiRanged.init(
                        AiJson.objectOf("entityId", this.init_h_DataUtil.entityId(), "count", 1, "timeoutSeconds", Math.min(8, this.init_I_I_4(45)))
                     );
                  }

                  AiOperation.Result r = this.init_AiOperation.tick();
                  if (!r.terminal()) {
                     return this.load("Shooting crystal #" + this.init_h_DataUtil.entityId() + ": " + r.message());
                  } else {
                     AiSpeedrun.init_V_Str_ObjArr("[AI] crystals: shot #{} {} - {}", this.init_h_DataUtil.entityId(), r.status(), r.message());
                     this.init_V_30();
                     if (this.init_Entity_h_DataUtil(this.init_h_DataUtil) == null) {
                        return this.poll();
                     } else if (r.status() == AiOperation.Status.CANCELLED) {
                        return this.init(AiOperation.Status.CANCELLED, AiFailure.CANCELLED, "Crystal clearing cancelled");
                     } else if (r.status() == AiOperation.Status.SUCCEEDED || r.status() == AiOperation.Status.UNCERTAIN) {
                        this.read_Z = true;
                        this.update_I = 0;
                        return this.load("Shot away");
                     } else if (r.dataJson() != null && r.dataJson().contains("\"missing\"")) {
                        return this.load_Z ? this.init(this.init_h_DataUtil, "no_ranged") : this.init(this.init_h_DataUtil, "shoot", "no_ranged");
                     } else {
                        return ++this.read_I >= 3
                           ? this.load(this.init_h_DataUtil, this.init_Str, "shots_missed")
                           : this.load("Shot failed (" + r.message() + "); retrying");
                     }
                  }
               }
            }

            AiOperation.Result update() {
               if (this.init_h_DataUtil == null) {
                  this.init_h_StreamUtil = AiCrystals.Inner_StreamUtil.SELECT;
                  return this.load("Re-selecting");
               }

               Entity entity = this.init_Entity_h_DataUtil(this.init_h_DataUtil);
               if (entity == null) {
                  return this.poll();
               }

               if (this.read_Z) {
                  if (++this.update_I > 40) {
                     this.read_Z = false;
                     return ++this.write_I >= 3
                        ? this.load(this.init_h_DataUtil, this.init_Str, "melee_failed")
                        : this.load("The swing did not register; re-swinging");
                  } else {
                     return this.load("Waiting for the pop");
                  }
               } else if (!this.init_Z_Entity_3(entity)) {
                  if (this.load_Z) {
                     return this.load(this.init_h_DataUtil, this.init_Str, "unreachable");
                  } else if (this.write_Z_8()) {
                     this.init_Str = "shoot";
                     this.init_h_StreamUtil = AiCrystals.Inner_StreamUtil.APPROACH;
                     return this.load("No longer adjacent; switching to the bow");
                  } else {
                     return this.init(this.init_h_DataUtil, "melee", "no_ranged");
                  }
               } else if (!this.read_Z_11() || !AiThreats.isLegacyCombatEnabled() && this.load_Z && !this.load(entity)) {
                  if (!this.write_Z_8() || this.load_Z && !AiThreats.isLegacyCombatEnabled()) {
                     return this.load_Z ? this.init(this.init_h_DataUtil, "lethal_blast") : this.init(this.init_h_DataUtil, "melee", "lethal_blast");
                  }

                  this.init_Str = this.load_Z ? "cage_shoot" : "shoot";
                  this.init_h_StreamUtil = this.load_Z ? AiCrystals.Inner_StreamUtil.SHOOT : AiCrystals.Inner_StreamUtil.APPROACH;
                  return this.load("Blast would be lethal point-blank; switching to the bow");
               } else {
                  this.init_DataUtil.aimAt(autismclient.ai.engine.AiAim.defaultEntityAimPoint(entity));
                  this.init_DataUtil.tick();
                  if (!this.init_DataUtil.isSettled() && ++this.sync < 25) {
                     return this.load("Aiming at the crystal");
                  }

                  mc.gameMode.attack(mc.player, entity);
                  Compat263.swing(mc.player, InteractionHand.MAIN_HAND);
                  mc.player.resetAttackStrengthTicker();
                  this.read_Z = true;
                  this.update_I = 0;
                  return this.load("Struck the crystal");
               }
            }

            AiOperation.Result sync() {
               if (this.init_h_DataUtil == null) {
                  this.init_h_StreamUtil = AiCrystals.Inner_StreamUtil.SELECT;
                  return this.load("Re-selecting");
               }

               Entity entity = this.init_Entity_h_DataUtil(this.init_h_DataUtil);
               if (entity == null) {
                  return this.init_AiOperation_Result_Str_12("cage");
               }

               if (this.init_AiOperation == null) {
                  int[] side = AiCrystals.init(mc.player.getX() - (this.init_h_DataUtil.x() + 0.5), mc.player.getZ() - (this.init_h_DataUtil.z() + 0.5));
                  this.init_BlockPos = new BlockPos(this.init_h_DataUtil.x() + side[0] * 3, this.init_h_DataUtil.y(), this.init_h_DataUtil.z() + side[1] * 3);
                  this.load_BlockPos = new BlockPos(this.init_h_DataUtil.x() + side[0] * 2, this.init_h_DataUtil.y(), this.init_h_DataUtil.z() + side[1] * 2);
                  this.init_AiOperation = AiGoto.travelToCompat(
                     this.init_BlockPos, 0.7, false, true, true, this.init_I_I_4(120), autismclient.ai.engine.AiPathfinder.RouteStrategy.FASTEST, true
                  );
               }

               AiOperation.Result r = this.init_AiOperation.tick();
               if (!r.terminal()) {
                  return this.load("Pillaring up beside the cage");
               } else {
                  this.init_V_30();
                  if (r.status() == AiOperation.Status.SUCCEEDED) {
                     this.init_h_StreamUtil = AiCrystals.Inner_StreamUtil.OPEN_CAGE;
                     return this.load("Beside the cage; opening the bars");
                  } else {
                     return ++this.poll >= 2
                        ? this.load(this.init_h_DataUtil, "cage", "unreachable")
                        : this.load("Pillar leg failed (" + r.message() + "); retrying");
                  }
               }
            }

            AiOperation.Result flush() {
               if (this.init_h_DataUtil == null) {
                  this.init_h_StreamUtil = AiCrystals.Inner_StreamUtil.SELECT;
                  return this.load("Re-selecting");
               }

               Entity entity = this.init_Entity_h_DataUtil(this.init_h_DataUtil);
               if (entity == null) {
                  return this.init_AiOperation_Result_Str_12("cage");
               }

               if (this.load_BlockPos != null && this.read_BlockPos == null) {
                  this.read_BlockPos = this.load_BlockPos;
               }

               if (this.load_BlockPos != null && !this.read(this.load_BlockPos)) {
                  this.load_BlockPos = null;
               }

               if (this.load_BlockPos != null) {
                  if (this.init_AiOperation == null) {
                     this.init_AiOperation = AiGameOperations.blockAction(
                        "break",
                        AiJson.objectOf(
                           "x",
                           this.load_BlockPos.getX(),
                           "y",
                           this.load_BlockPos.getY(),
                           "z",
                           this.load_BlockPos.getZ(),
                           "timeoutSeconds",
                           this.init_I_I_4(30)
                        )
                     );
                  }

                  AiOperation.Result r = this.init_AiOperation.tick();
                  if (!r.terminal()) {
                     return this.load("Breaking the iron bars");
                  }

                  this.init_V_30();
                  this.load_BlockPos = null;
               }

               if (!AiThreats.isLegacyCombatEnabled() && this.read_BlockPos != null) {
                  BlockPos eyeBar = this.read_BlockPos.above();
                  if (this.read(eyeBar)) {
                     if (this.init_AiOperation == null) {
                        this.init_AiOperation = AiGameOperations.blockAction(
                           "break",
                           AiJson.objectOf("x", eyeBar.getX(), "y", eyeBar.getY(), "z", eyeBar.getZ(), "timeoutSeconds", this.init_I_I_4(30))
                        );
                     }

                     AiOperation.Result r = this.init_AiOperation.tick();
                     if (!r.terminal()) {
                        return this.load("Breaking the bars at eye level");
                     }

                     this.init_V_30();
                     if (this.read(eyeBar)) {
                        this.read_BlockPos = null;
                     }
                  }

                  if (this.read_BlockPos != null && !this.init_Z && mc.level.getBlockState(this.read_BlockPos).canBeReplaced()) {
                     if (this.init_AiOperation == null) {
                        String block = this.init_Str_18();
                        if (block != null && AiItems.equipHotbar(AiItems.resolveItem(block)) >= 0) {
                           this.init_AiOperation = AiGameOperations.blockAction(
                              "place",
                              AiJson.objectOf(
                                 "x", this.read_BlockPos.getX(), "y", this.read_BlockPos.getY() - 1, "z", this.read_BlockPos.getZ(), "timeoutSeconds", 6
                              )
                           );
                        } else {
                           this.init_Z = true;
                           this.tick = mc.player.tickCount;
                        }
                     }

                     if (this.init_AiOperation != null) {
                        AiOperation.Result r = this.init_AiOperation.tick();
                        if (!r.terminal()) {
                           return this.load("Setting a block in the opening");
                        }

                        this.init_V_30();
                        this.init_Z = true;
                        this.tick = mc.player.tickCount;
                     }
                  }

                  if (this.init_Z && mc.player.tickCount - this.tick < 2) {
                     return this.load("Verifying crystal cover and blast damage");
                  }

                  if (this.load(entity) && this.read_Z_11()) {
                     this.init_Str = "cage_melee";
                     this.init_h_StreamUtil = AiCrystals.Inner_StreamUtil.MELEE;
                     return this.load("Cover set; striking the crystal over it");
                  }
               }

               if (!this.read_Z_11() || !AiThreats.isLegacyCombatEnabled() && this.load_Z && !this.load(entity)) {
                  if (AiThreats.isLegacyCombatEnabled() && this.write_Z_8()) {
                     this.init_Str = "cage_shoot";
                     this.init_h_StreamUtil = AiCrystals.Inner_StreamUtil.SHOOT;
                     return this.load("Blast lethal point-blank; shooting through the opening");
                  } else {
                     return this.init(this.init_h_DataUtil, "lethal_blast");
                  }
               } else {
                  this.init_Str = "cage_melee";
                  this.init_h_StreamUtil = AiCrystals.Inner_StreamUtil.MELEE;
                  return this.load("Bars open; punching the crystal");
               }
            }

            AiOperation.Result close() {
               BlockPos feet = mc.player.blockPosition();
               if (this.update_Z) {
                  return this.init_AiOperation_Result_BlockPos(feet);
               } else if (this.load_Z_15()) {
                  return this.open();
               } else {
                  return this.push < 2 && ("mlg".equals(AiCrystals.init_Str(AiCrystals.init(mc.player))) || !AiThreats.isLegacyCombatEnabled() && AiGameState.countInventoryItem("minecraft:water_bucket") > 0)
                     ? this.init_AiOperation_Result_BlockPos(feet)
                     : this.load_AiOperation_Result_3(feet);
               }
            }

            AiOperation.Result init_AiOperation_Result_BlockPos(BlockPos feet) {
               if (this.init_AiOperation == null) {
                  if (mc.player.onGround() && feet.getY() > this.flush + 1) {
                     if (++this.close > 40) {
                        this.push++;
                        this.init_V_30();
                        return this.load("Could not step off cleanly; digging down instead");
                     }

                     this.init_DataUtil.aimAt(this.init_Vec3_8());
                     this.init_DataUtil.tick();
                     AiFallGuard.load();
                     mc.options.keyUp.setDown(true);
                     return this.load("Stepping off for the water clutch");
                  }

                  mc.options.keyUp.setDown(false);
                  this.init_AiOperation = AiMlg.init_AiOperation_JsonObject(AiJson.objectOf("method", "auto", "collect", true, "timeoutSeconds", 15));
                  this.update_Z = true;
               }

               AiOperation.Result r = this.init_AiOperation.tick();
               if (!r.terminal()) {
                  return this.load("Falling with the water clutch");
               }

               mc.options.keyUp.setDown(false);
               this.init_V_30();
               if (r.status() == AiOperation.Status.SUCCEEDED) {
                  return this.load_Z_15() ? this.open() : this.load("Landed short; continuing down");
               }

               this.push++;
               return this.load("Clutch unavailable (" + r.message() + "); digging down instead");
            }

            AiOperation.Result load_AiOperation_Result_3(BlockPos feet) {
               if (!mc.player.onGround()) {
                  return this.load("Stepping down the tower");
               }

               BlockPos below = feet.below();
               if (mc.level.getBlockState(below).isAir()) {
                  return this.load("Stepping down the tower");
               }

               String path = BuiltInRegistries.BLOCK.getKey(mc.level.getBlockState(below).getBlock()).getPath();
               if (!"obsidian".equals(path) && !"bedrock".equals(path)) {
                  if (this.init_AiOperation == null) {
                     this.init_AiOperation = autismclient.ai.engine.AiBreakBlock.init(below, 20);
                  }

                  AiOperation.Result r = this.init_AiOperation.tick();
                  if (!r.terminal()) {
                     return this.load("Digging down the tower");
                  }

                  this.init_V_30();
                  return r.status() == AiOperation.Status.SUCCEEDED
                     ? this.load("Digging down the tower")
                     : this.init(AiOperation.Status.UNCERTAIN, r.failure(), "Tower descent failed before safe ground: " + r.message());
               } else {
                  return this.init(
                     AiOperation.Status.UNCERTAIN, AiFailure.ENVIRONMENTAL_HAZARD, "Clutch unavailable and the remaining tower cannot be dug down; safe ground is not reached"
                  );
               }
            }

            boolean load_Z_15() {
               return AiCrystals.init_Z_I(mc.player.blockPosition().getY(), this.flush, mc.player.onGround() || mc.player.isInWater() || mc.player.onClimbable());
            }

            AiOperation.Result open() {
               this.init_V_30();
               this.init_h_DataUtil = null;
               this.read_Z = false;
               this.init_h_StreamUtil = AiCrystals.Inner_StreamUtil.SELECT;
               return this.load("Back on the ground");
            }

            AiOperation.Result poll() {
               AiCrystals.Inner_DataUtil t = this.init_h_DataUtil;
               this.read_Z = false;
               this.init_JsonArray
                  .add(
                     AiJson.objectOf(
                        "id", t.entityId(), "caged", t.caged(), "method", this.init_Str.isEmpty() ? "shoot" : this.init_Str, "result", "cleared"
                     )
                  );
               this.init_I++;
               AiRunTrace.recordGlobalSplit("crystal", (this.init_Str.isEmpty() ? "shoot" : this.init_Str) + " #" + t.entityId());
               if (this.load_Z && !this.load_Z_15()) {
                  this.init_h_StreamUtil = AiCrystals.Inner_StreamUtil.DESCEND;
                  this.close = 0;
                  return this.load("Crystal cleared; getting down");
               } else {
                  this.init_h_DataUtil = null;
                  this.init_h_StreamUtil = AiCrystals.Inner_StreamUtil.SELECT;
                  return this.load("Crystal cleared (" + this.init_I + ")");
               }
            }

            AiOperation.Result init_AiOperation_Result_Str_12(String planned) {
               AiCrystals.Inner_DataUtil t = this.init_h_DataUtil;
               this.read_Z = false;
               this.init_JsonArray.add(AiJson.objectOf("id", t.entityId(), "caged", t.caged(), "method", planned, "result", "cleared"));
               this.init_I++;
               if (!this.load_Z_15()) {
                  this.init_h_StreamUtil = AiCrystals.Inner_StreamUtil.DESCEND;
                  this.close = 0;
                  return this.load("Crystal gone; getting down");
               } else {
                  this.init_h_DataUtil = null;
                  this.init_h_StreamUtil = AiCrystals.Inner_StreamUtil.SELECT;
                  return this.load("Crystal gone; re-selecting");
               }
            }

            AiOperation.Result init(AiCrystals.Inner_DataUtil t, String planned, String reason) {
               this.init_V_30();
               this.init_Set.add(t.entityId());
               this.init_JsonArray.add(AiJson.objectOf("id", t.entityId(), "caged", t.caged(), "method", planned, "result", "skipped:" + reason));
               this.init_h_CachePool.init(reason);
               this.init_h_DataUtil = null;
               this.init_h_StreamUtil = AiCrystals.Inner_StreamUtil.SELECT;
               return this.load("Crystal #" + t.entityId() + " skipped: " + reason);
            }

            AiOperation.Result init(AiCrystals.Inner_DataUtil t, String reason) {
               this.init_Set.add(t.entityId());
               this.init_JsonArray
                  .add(
                     AiJson.objectOf(
                        "id", t.entityId(), "caged", t.caged(), "method", this.init_Str.isEmpty() ? "cage" : this.init_Str, "result", "skipped:" + reason
                     )
                  );
               this.init_h_CachePool.init(reason);
               this.init_h_StreamUtil = AiCrystals.Inner_StreamUtil.DESCEND;
               this.close = 0;
               return this.load("Crystal #" + t.entityId() + " skipped: " + reason + "; getting down");
            }

            AiOperation.Result load(AiCrystals.Inner_DataUtil t, String planned, String reason) {
               this.init_V_30();
               this.init_Set.add(t.entityId());
               this.init_JsonArray.add(AiJson.objectOf("id", t.entityId(), "caged", t.caged(), "method", planned, "result", "failed:" + reason));
               if (this.load_Z && mc.player.blockPosition().getY() > this.flush + 1) {
                  this.init_h_StreamUtil = AiCrystals.Inner_StreamUtil.DESCEND;
                  this.close = 0;
                  return this.load("Crystal #" + t.entityId() + " failed: " + reason + "; getting down");
               } else {
                  this.init_h_DataUtil = null;
                  this.init_h_StreamUtil = AiCrystals.Inner_StreamUtil.SELECT;
                  return this.load("Crystal #" + t.entityId() + " failed: " + reason);
               }
            }

            AiOperation.Result push() {
               String suffix = this.init_I > 0 ? " after clearing " + this.init_I : "";
               if (this.init_h_CachePool.init > 0) {
                  this.read_Str = "NO_RANGED";
                  this.init_JsonObject = AiCrystals.init();
                  return this.init(AiFailure.NO_RANGED, "No ranged weapon or arrows for the remaining uncaged crystals" + suffix);
               } else if (this.init_h_CachePool.load > 0) {
                  this.init_JsonObject = AiJson.objectOf("minecraft:dirt", Math.max(1, this.open));
                  return this.init(AiFailure.MISSING_MATERIALS, "No throwaway construction blocks for pillaring to the caged crystals" + suffix);
               } else {
                  return this.init(AiFailure.EXECUTION_FAILED, this.init_h_CachePool.init(this.init_I));
               }
            }

            List<AiCrystals.Inner_DataUtil> init_List_9() {
               List<AiCrystals.Inner_DataUtil> out = new ArrayList<>();

               for (Entity entity : mc.level.entitiesForRendering()) {
                  if (entity instanceof EndCrystal && !entity.isRemoved()) {
                     BlockPos cell = entity.blockPosition();
                     out.add(
                        new AiCrystals.Inner_DataUtil(
                           entity.getId(), cell.getX(), cell.getY(), cell.getZ(), mc.player.distanceToSqr(entity), this.init_Z_BlockPos_7(cell)
                        )
                     );
                  }
               }

               return out;
            }

            boolean init_Z_BlockPos_7(BlockPos cell) {
               return AiCrystals.init(mc, cell);
            }

            boolean load_Z_7(BlockPos cell) {
               List<String> nearby = new ArrayList<>(32);

               for (int dx = -2; dx <= 2; dx++) {
                  for (int dy = -2; dy <= 2; dy++) {
                     for (int dz = -2; dz <= 2; dz++) {
                        if (dx != 0 || dy != 0 || dz != 0) {
                           nearby.add(BuiltInRegistries.BLOCK.getKey(mc.level.getBlockState(cell.offset(dx, dy, dz)).getBlock()).getPath());
                        }
                     }
                  }
               }

               return AiCrystals.init_Z(nearby);
            }

            boolean read(BlockPos pos) {
               return "iron_bars".equals(BuiltInRegistries.BLOCK.getKey(mc.level.getBlockState(pos).getBlock()).getPath());
            }

            Entity init_Entity_h_DataUtil(AiCrystals.Inner_DataUtil target) {
               Entity entity = mc.level.getEntity(target.entityId());
               return entity != null && !entity.isRemoved() ? entity : null;
            }

            boolean init_Z_Entity_3(Entity entity) {
               double reach = Math.max(3.0, mc.player.getAttributeValue(Attributes.ENTITY_INTERACTION_RANGE)) - 0.1;
               return entity.getBoundingBox().distanceToSqr(mc.player.getEyePosition()) <= reach * reach;
            }

            boolean init_Z_D_3(double distance) {
               for (Entity e : mc.level.entitiesForRendering()) {
                  if (e instanceof EnderDragon dragon && dragon.isAlive()) {
                     DragonPhaseInstance phase = dragon.getPhaseManager().getCurrentPhase();
                     if (!phase.isSitting() && phase.getPhase() != EnderDragonPhase.LANDING && dragon.distanceTo(mc.player) < distance) {
                        return true;
                     }
                  }
               }

               return false;
            }

            boolean init(double within, int ticks) {
               for (Entity e : mc.level.entitiesForRendering()) {
                  if (e instanceof EnderDragon dragon && dragon.isAlive()) {
                     DragonPhaseInstance phase = dragon.getPhaseManager().getCurrentPhase();
                     if (!phase.isSitting() && phase.getPhase() != EnderDragonPhase.LANDING) {
                        EnderDragonPhase<?> kind = phase.getPhase();
                        double d = dragon.distanceTo(mc.player);
                        if ((kind == EnderDragonPhase.CHARGING_PLAYER || kind == EnderDragonPhase.STRAFE_PLAYER) && d < 96.0) {
                           return true;
                        }

                        if (d < within) {
                           return true;
                        }

                        double vx = dragon.getX() - dragon.xo;
                        double vy = dragon.getY() - dragon.yo;
                        double vz = dragon.getZ() - dragon.zo;

                        for (int t = 5; t <= ticks; t += 5) {
                           double x = dragon.getX() + vx * t - mc.player.getX();
                           double y = dragon.getY() + vy * t - mc.player.getY();
                           double z = dragon.getZ() + vz * t - mc.player.getZ();
                           if (x * x + y * y + z * z < within * within) {
                              return true;
                           }
                        }
                     }
                  }
               }

               return false;
            }

            boolean read_Z_11() {
               if (AiThreats.isLegacyCombatEnabled()) {
                  return !AiDanger.init_i_DataUtil_Minecraft(mc).lethal();
               }

               AiDanger.Inner_DataUtil danger = AiDanger.init_i_DataUtil_Minecraft(mc);
               Entity crystal = this.init_h_DataUtil == null ? null : this.init_Entity_h_DataUtil(this.init_h_DataUtil);
               if (crystal == null) {
                  return false;
               }

               double damage = autismclient.util.ExplosionDamageCalculator.calculateDamage(
                  mc.player,
                  crystal.position(),
                  6.0F,
                  autismclient.util.ExplosionDamageCalculator.SimOptions.DEFAULT.withDamageSource(Explosion.getDefaultDamageSource(mc.level, crystal))
               );
               return AiCrystals.init(damage, mc.player.getHealth() + mc.player.getAbsorptionAmount(), danger.lethal(), danger.voidFall())
                  && danger.worstSource() <= 4.0F;
            }

            boolean load(Entity crystal) {
               if (this.init_Z && this.read_BlockPos != null && crystal != null) {
                  BlockState state = mc.level.getBlockState(this.read_BlockPos);
                  boolean solid = state.isCollisionShapeFullBlock(mc.level, this.read_BlockPos);
                  Vec3 body = mc.player.position().add(0.0, 0.4, 0.0);
                  return state.getCollisionShape(mc.level, this.read_BlockPos)
                     .toAabbs()
                     .stream()
                     .anyMatch(box -> AiCrystals.init(solid, box.move(this.read_BlockPos), crystal.position(), body));
               } else {
                  return false;
               }
            }

            boolean write_Z_8() {
               return AiCrystals.init_Z_I(
                  AiGameState.countInventoryItem("minecraft:bow"),
                  AiGameState.countInventoryItem("minecraft:crossbow"),
                  AiRunResources.totalArrowCount(),
                  AiGameState.countInventoryItem("minecraft:trident"),
                  AiGameState.countInventoryItem("minecraft:snowball") + AiGameState.countInventoryItem("minecraft:egg")
               );
            }

            boolean read(Entity entity) {
               double dx = entity.getX() - mc.player.getX();
               double dz = entity.getZ() - mc.player.getZ();
               double horizontal = Math.hypot(dx, dz);
               double dy = entity.getY() - mc.player.getEyePosition().y;
               return AiCrystals.init_Z_D(horizontal, dy, Math.sqrt(horizontal * horizontal + dy * dy));
            }

            BlockPos init_BlockPos_Entity_2(Entity entity) {
               double dx = mc.player.getX() - entity.getX();
               double dz = mc.player.getZ() - entity.getZ();
               double dy = Math.max(0.0, entity.getY() - mc.player.getEyePosition().y);
               int[] xz = AiCrystals.init_IArr_I(this.init_h_DataUtil.x(), this.init_h_DataUtil.z(), dx, dz, dy);

               for (int y = this.init_h_DataUtil.y(); y > mc.level.getMinY(); y--) {
                  BlockPos p = new BlockPos(xz[0], y, xz[1]);
                  if (!mc.level.getBlockState(p).isAir()) {
                     return p.above();
                  }
               }

               return null;
            }

            String init_Str_18() {
               String best = null;
               int bestRank = 2147483647;

               for (int slot = 0; slot < 36; slot++) {
                  ItemStack stack = mc.player.getInventory().getItem(slot);
                  if (!stack.isEmpty()) {
                     String id = BuiltInRegistries.ITEM.getKey(stack.getItem()).toString();
                     int rank = autismclient.ai.engine.AiRestock.scaffoldRank(id);
                     if (rank >= 0 && rank < bestRank) {
                        bestRank = rank;
                        best = id;
                     }
                  }
               }

               return best;
            }

            int init_I_36() {
               int total = 0;

               for (int slot = 0; slot < 36; slot++) {
                  ItemStack stack = mc.player.getInventory().getItem(slot);
                  if (!stack.isEmpty() && autismclient.ai.engine.AiRestock.isScaffoldBlockId(BuiltInRegistries.ITEM.getKey(stack.getItem()).toString())) {
                     total += stack.getCount();
                  }
               }

               return total + AiPlanks.init(mc.player.getInventory());
            }

            Vec3 init_Vec3_8() {
               double cx = this.init_h_DataUtil == null ? mc.player.getX() : this.init_h_DataUtil.x() + 0.5;
               double cz = this.init_h_DataUtil == null ? mc.player.getZ() : this.init_h_DataUtil.z() + 0.5;
               double dx = mc.player.getX() - cx;
               double dz = mc.player.getZ() - cz;
               double len = Math.hypot(dx, dz);
               if (len < 1.0E-6) {
                  dx = 1.0;
                  dz = 0.0;
                  len = 1.0;
               }

               return new Vec3(mc.player.getX() + dx / len * 2.0, mc.player.getEyePosition().y, mc.player.getZ() + dz / len * 2.0);
            }

            int init_I_I_4(int cap) {
               return Math.max(10, Math.min(cap, this.load_I_14()));
            }

            int load_I_14() {
               return (int)Math.max(1L, (this.init_J - System.nanoTime()) / 1000000000L);
            }

            void init_V_30() {
               if (this.init_AiOperation != null) {
                  this.init_AiOperation.cancel();
                  this.init_AiOperation = null;
               }

               this.update_Z = false;
               mc.options.keyUp.setDown(false);
            }

            JsonObject init_JsonObject_58() {
               JsonObject out = AiJson.objectOf(
                  "cleared",
                  this.init_I,
                  "remaining",
                  this.pop,
                  "skipped",
                  this.init_Set.size(),
                  "phase",
                  this.init_h_StreamUtil.name().toLowerCase(Locale.ROOT),
                  "towerCommitted",
                  this.init_Z_26()
               );
               out.add("outcomes", this.init_JsonArray);
               if (this.init_h_DataUtil != null) {
                  out.add(
                     "current",
                     AiJson.objectOf(
                        "id",
                        this.init_h_DataUtil.entityId(),
                        "caged",
                        this.init_h_DataUtil.caged(),
                        "method",
                        this.init_Str,
                        "x",
                        this.init_h_DataUtil.x(),
                        "y",
                        this.init_h_DataUtil.y(),
                        "z",
                        this.init_h_DataUtil.z()
                     )
                  );
               }

               out.add("survival", AiGuard.statusToJson(AiGuard.sampleStatus(), this.load_I, this.load_Str));
               if (this.init_JsonObject != null) {
                  out.add("missing", this.init_JsonObject);
               }

               if (this.read_Str != null) {
                  out.addProperty("code", this.read_Str);
               }

               return out;
            }

            AiOperation.Result load(String message) {
               return AiGameOperations.result(AiOperation.Status.RUNNING, message, this.init_JsonObject_58());
            }

            AiOperation.Result init(AiOperation.Status status, AiFailure failure, String message) {
               JsonObject data = this.init_JsonObject_58();
               this.cancel();
               return new AiOperation.Result(status, message, data.toString(), List.of(), failure);
            }

            AiOperation.Result init(AiFailure failure, String message) {
               return this.init(AiOperation.Status.FAILED, failure, message);
            }

            @Override
            public boolean canInterruptNow() {
               return AiCrystals.init_Z_h_StreamUtil(
                  this.init_h_StreamUtil,
                  this.load_Z && this.init_h_DataUtil != null,
                  this.read_Z,
                  this.init_AiOperation == null || this.init_AiOperation.canInterruptNow(),
                  this.load_AiOperation == null || this.load_AiOperation.canInterruptNow()
               );
            }

            @Override
            public boolean requiresReconciliationAfterEmergency() {
               return AiCrystals.init_Z_Z(
                  this.read_Z,
                  this.init_AiOperation != null && this.init_AiOperation.requiresReconciliationAfterEmergency(),
                  this.load_AiOperation != null && this.load_AiOperation.requiresReconciliationAfterEmergency()
               );
            }

            @Override
            public void cancel() {
               if (!this.write_Z) {
                  this.write_Z = true;
                  this.init_V_30();
                  if (this.load_AiOperation != null) {
                     this.load_AiOperation.cancel();
                     this.load_AiOperation = null;
                  }

                  this.init_DataUtil.clear();
               }
            }
         };
      } else {
         throw new IllegalArgumentException("Crystal clearing requires a connected living player");
      }
   }

   static final class Inner_CachePool {
      int init;
      int load;
      int read;

      void init(String reason) {
         if ("no_ranged".equals(reason)) {
            this.init++;
         }

         if ("no_blocks".equals(reason)) {
            this.load++;
         }

         if ("dragon_near".equals(reason)) {
            this.read++;
         }
      }

      String init(int cleared) {
         return "Every remaining crystal is unreachable; see outcomes"
            + (cleared > 0 ? " after clearing " + cleared : "")
            + (this.read > 0 ? " (dragon near the towers)" : "");
      }
   }

   record Inner_DataUtil(int entityId, int x, int y, int z, double distanceSqr, boolean caged) {
   }

   enum Inner_StreamUtil {
      SELECT,
      APPROACH,
      SHOOT,
      MELEE,
      PILLAR,
      OPEN_CAGE,
      DESCEND;
   }
}

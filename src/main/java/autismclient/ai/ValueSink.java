package autismclient.ai;

import autismclient.AutismClientAddon;
import autismclient.ai.a.HashUtil;
import autismclient.api.ai.AiOperation;
import autismclient.compat263.Compat263;
import com.google.gson.JsonObject;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.BlockPos.MutableBlockPos;
import net.minecraft.core.Direction.Axis;
import net.minecraft.core.Direction.Plane;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.animal.sheep.Sheep;
import net.minecraft.world.entity.monster.Blaze;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.entity.monster.Enderman;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.monster.Ghast;
import net.minecraft.world.entity.monster.Phantom;
import net.minecraft.world.entity.monster.Vex;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;

final class ValueSink {
   private static final double sync_D = 0.92;
   private static final float init_F = 0.92F;
   private static final double flush_D = 6.0;
   private static final int close_I = 40;
   static final int init_I = 3;
   static final int load_I = 12;
   private static final int open_I = 8;
   static final long init_J = 15000000000L;
   static final int read_I = 60;
   static final int write_I = 45;
   static final int update_I = 12;
   static final int sync_I = 8;
   static final double init_D = 0.5;
   static final double load_D = 70.0;
   private static final Map<Integer, Long> load_Map = new HashMap<>();
   private static long write_J = -9223372036854775808L;
   static final long load_J = 2500000000L;
   static final double read_D = 10.0;
   static final long read_J = 8000000000L;
   static final double write_D = 2.0;
   static final int flush_I = 8;
   static final Map<UUID, Long> init_Map = new ConcurrentHashMap<>();
   private static final int poll = 12;
   private static final int push = 3;
   private static final double close_D = 1.4;
   private static final double open_D = 1.0;
   private static final int pop = 12;
   static final double update_D = 16.0;

   private ValueSink() {
   }

   static double init_D_D_Z(double reach, boolean holding) {
      return holding ? reach - 0.03 : reach * 0.92;
   }

   static by.Inner_StreamUtil init_by_StreamUtil(Minecraft mc) {
      by.Inner_StreamUtil best = null;

      for (by.Inner_StreamUtil t : by.init(mc)) {
         if (by.init(t) && (best == null || t.secondsToClosest() < best.secondsToClosest())) {
            best = t;
         }
      }

      return best;
   }

   static by.Inner_StreamUtil load(Minecraft mc) {
      by.Inner_StreamUtil best = null;

      for (by.Inner_StreamUtil shot : by.init(mc)) {
         if (by.init(shot)
            && k.init(mc, mc.level.getEntity(shot.entityId()))
            && k.init(mc, init(shot))
            && (best == null || shot.secondsToClosest() < best.secondsToClosest())) {
            best = shot;
         }
      }

      return best;
   }

   static int init(by.Inner_StreamUtil t) {
      return (int)Math.ceil(t.secondsToClosest() * 20.0);
   }

   static Vec3 init_Vec3_Minecraft_Vec3(Minecraft mc, Vec3 preferred) {
      List<by.Inner_StreamUtil> threats = new ArrayList<>();

      for (by.Inner_StreamUtil shot : by.init(mc)) {
         if (by.init(shot) && k.init(mc, mc.level.getEntity(shot.entityId())) && k.init(mc, init(shot))) {
            threats.add(shot);
         }
      }

      Vec3 eye = mc.player.getEyePosition();
      Vec3 keep = eye.add(mc.player.getViewVector(1.0F).scale(4.0));
      if (threats.isEmpty()) {
         return preferred != null ? preferred : keep;
      }

      threats.sort(Comparator.comparingDouble(by.Inner_StreamUtil::secondsToClosest).thenComparingInt(by.Inner_StreamUtil::entityId));
      int window = Math.max(8, init(threats.getFirst()));
      List<Float> yaws = new ArrayList<>();

      for (by.Inner_StreamUtil shot : threats) {
         if (init(shot) <= window) {
            yaws.add(init_F_Minecraft(mc, shot.x(), shot.z()));
         }
      }

      float bestYaw = CacheEntry.init_F_F(mc.player.getYRot(), preferred == null ? null : init_F_Minecraft(mc, preferred.x, preferred.z), yaws, 70.0);
      double r = Math.toRadians(bestYaw);
      return eye.add(new Vec3(-Math.sin(r), 0.0, Math.cos(r)).scale(4.0));
   }

   static float init_F_Minecraft(Minecraft mc, double x, double z) {
      return (float)Math.toDegrees(Math.atan2(-(x - mc.player.getX()), z - mc.player.getZ()));
   }

   static boolean init_Z_Minecraft(Minecraft mc, by.Inner_StreamUtil t, float yaw) {
      double dx = t.x() - mc.player.getX();
      double dz = t.z() - mc.player.getZ();
      return dx * dx + dz * dz < 1.0E-4 ? true : Math.abs(Mth.wrapDegrees(init_F_Minecraft(mc, t.x(), t.z()) - yaw)) <= 70.0;
   }

   static boolean init_Z_Minecraft_2(Minecraft mc, List<by.Inner_StreamUtil> ts, float yaw) {
      for (by.Inner_StreamUtil t : ts) {
         if (!init_Z_Minecraft(mc, t, yaw)) {
            return false;
         }
      }

      return true;
   }

   static Creeper init_Creeper(Minecraft mc) {
      Creeper best = null;
      double score = -1.0 / 0.0;

      for (Entity entity : mc.level.entitiesForRendering()) {
         if (entity instanceof Creeper creeper
            && creeper.isAlive()
            && creeper.getSwellDir() > 0
            && !(creeper.distanceToSqr(mc.player) >= 56.25)
            && mc.player.hasLineOfSight(creeper)) {
            double urgency = creeper.getSwelling(1.0F) * 20.0F - creeper.distanceTo(mc.player);
            if (urgency > score || urgency == score && best != null && creeper.getId() < best.getId()) {
               best = creeper;
               score = urgency;
            }
         }
      }

      return best;
   }

   static Entity init_Entity(Minecraft mc) {
      if (mc.player != null && mc.level != null) {
         long now = autismclient.util.AutismSessionState.init_ck().state_I();
         if (now != write_J) {
            write_J = now;
            Set<Integer> seen = new HashSet<>();

            for (Entity entity : mc.level.entitiesForRendering()) {
               if (entity instanceof Blaze blaze && blaze.isAlive() && !(blaze.distanceToSqr(mc.player) > 1600.0) && blaze.isOnFire()) {
                  load_Map.putIfAbsent(blaze.getId(), now);
                  seen.add(blaze.getId());
               }
            }

            load_Map.keySet().retainAll(seen);
         }

         Entity best = null;
         double bestD = 1.7976931348623157E308;

         for (Entry<Integer, Long> entry : load_Map.entrySet()) {
            if (now - entry.getValue() >= 45L) {
               Entity blaze = mc.level.getEntity(entry.getKey());
               if (blaze != null && blaze.isAlive()) {
                  double d2 = blaze.distanceToSqr(mc.player);
                  if (!(d2 < 4.840000000000001) && !(d2 > 576.0) && mc.player.hasLineOfSight(blaze) && d2 < bestD) {
                     bestD = d2;
                     best = blaze;
                  }
               }
            }
         }

         return best;
      } else {
         return null;
      }
   }

   static AiOperation init(final int ticks) {
      final Minecraft mc = Minecraft.getInstance();
      return new AiOperation() {
         final autismclient.ai.a.DataUtil init_DataUtil = new autismclient.ai.a.DataUtil();
         int init_I = ticks;
         boolean init_Z;

         {
            this.init_DataUtil.init_V_DataUtil_DataUtil(autismclient.ai.a.DataUtil.Inner_DataUtil.write);
         }

         @Override
         public AiOperation.Result tick() {
            if (mc.player == null) {
               return this.init(AiOperation.Status.CANCELLED);
            }

            if (--this.init_I <= 0) {
               return this.init(AiOperation.Status.SUCCEEDED);
            }

            boolean hasShield = k.init_Z(mc);
            Entity shooter = !hasShield ? null : (cv.init_Z() ? ValueSink.init_Entity(mc) : cv.init(mc, 12));
            by.Inner_StreamUtil coming = hasShield && !cv.init_Z() ? ValueSink.load(mc) : null;
            if (coming != null) {
               this.init_DataUtil
                  .init_V_Vec3(ValueSink.init_Vec3_Minecraft_Vec3(mc, shooter == null ? null : autismclient.ai.a.DataUtil.load_Vec3_Entity(shooter)));
               this.init_DataUtil.load_V();
            } else if (shooter != null) {
               this.init_DataUtil.init_V_Vec3(autismclient.ai.a.DataUtil.load_Vec3_Entity(shooter));
               this.init_DataUtil.load_V();
            }

            boolean want = shooter != null || coming != null;
            if (want != this.init_Z) {
               this.init_Z = want;
               mc.options.keyUse.setDown(want);
            }

            return new AiOperation.Result(AiOperation.Status.RUNNING, this.init_Z ? "Shield up against a blaze" : "Waiting", "{}", List.of());
         }

         AiOperation.Result init(AiOperation.Status status) {
            if (this.init_Z && mc.options != null) {
               mc.options.keyUse.setDown(false);
            }

            this.init_Z = false;
            this.init_DataUtil.init_V();
            return new AiOperation.Result(status, status == AiOperation.Status.SUCCEEDED ? "waited" : "cancelled", "{}", List.of());
         }

         @Override
         public void cancel() {
            this.init(AiOperation.Status.CANCELLED);
         }
      };
   }

   static int init_I_D(double gap, double slack, double readyIn, double chase, boolean kite) {
      if (readyIn <= 1.0) {
         return slack < 0.05 ? 1 : 0;
      } else if (!kite) {
         return slack < -0.5 ? 1 : 0;
      } else {
         double arriving = gap - chase * readyIn;
         if (!(gap < 0.6) && !(arriving < 0.2)) {
            return slack < -0.4 && arriving > 1.2 && readyIn < 5.0 ? 1 : 0;
         } else {
            return -1;
         }
      }
   }

   private static void init_V_Entity_J(Entity entity, long until) {
      long now = System.nanoTime();
      init_Map.values().removeIf(expiry -> expiry < now);
      init_Map.put(entity.getUUID(), until);

      while (init_Map.size() > 1024) {
         init_Map.remove(init_Map.keySet().iterator().next());
      }
   }

   static boolean init(Entity entity) {
      if (entity == null) {
         return true;
      }

      Long until = init_Map.get(entity.getUUID());
      return until != null && until > System.nanoTime();
   }

   static boolean load(Entity entity) {
      return entity.isUnderWater() ? true : entity.isInWater() && entity.level().getFluidState(entity.blockPosition().below()).is(FluidTags.WATER);
   }

   static boolean init_Z_Z(boolean unreachable, double distanceSq, double reachGap, boolean deepWater, boolean legacy) {
      return (!unreachable || distanceSq < 9.0 && reachGap <= 1.0) && (legacy || !deepWater || reachGap <= 1.0);
   }

   static boolean init_Z_Minecraft_Entity(Minecraft mc, Entity entity) {
      if (mc.player != null && entity != null && entity.isAlive()) {
         double gap = entity instanceof Mob mob ? cv.init(mob, mc.player.getBoundingBox()) : 0.0;
         return init_Z_Z(init(entity), entity.distanceToSqr(mc.player), gap, load(entity), cv.init_Z());
      } else {
         return false;
      }
   }

   static Comparator<ValueSink.Inner_StreamUtil> init(ValueSink.Inner_CachePool priority) {
      Comparator var10000;
      switch (priority) {
         case init:
            var10000 = Comparator.<ValueSink.Inner_StreamUtil>comparingDouble(c -> c.distanceSqr() - (c.hurtPlayer() ? 64 : 0));
            break;
         case load:
            var10000 = Comparator.comparingDouble(ValueSink.Inner_StreamUtil::health).thenComparingDouble(ValueSink.Inner_StreamUtil::distanceSqr);
            break;
         case read:
            var10000 = Comparator.<ValueSink.Inner_StreamUtil>comparingDouble(c -> -c.maxHealth()).thenComparingDouble(ValueSink.Inner_StreamUtil::distanceSqr);
            break;
         case write:
            var10000 = Comparator.<ValueSink.Inner_StreamUtil, Boolean>comparing(c -> !c.attacking())
               .thenComparingDouble(ValueSink.Inner_StreamUtil::distanceSqr);
            break;
         default:
            throw new MatchException(null, null);
      }

      return var10000;
   }

   static String init(boolean healAttempted) {
      return healAttempted ? "healed_and_reengaging_exhausted" : "health_threshold";
   }

   static ValueSink.Inner_DataUtil init(boolean targetOnGround, double horizontal, double verticalGap, double reach) {
      if (!targetOnGround && !(verticalGap <= 1.0)) {
         if (Math.hypot(horizontal, verticalGap) <= reach * 0.92) {
            return ValueSink.Inner_DataUtil.init;
         } else if (init_I_D_D(verticalGap, reach) < 0) {
            return ValueSink.Inner_DataUtil.write;
         } else {
            return horizontal <= 1.4 ? ValueSink.Inner_DataUtil.read : ValueSink.Inner_DataUtil.load;
         }
      } else {
         return ValueSink.Inner_DataUtil.init;
      }
   }

   static int init_I_D_D(double verticalGap, double reach) {
      double effective = reach * 0.92;

      for (int pillar = 1; pillar <= 3; pillar++) {
         if (Math.hypot(1.0, Math.max(0.0, verticalGap - pillar)) <= effective) {
            return pillar;
         }
      }

      return -1;
   }

   static boolean init_Z_Minecraft_BlockPos(Minecraft mc, BlockPos step) {
      if (!cu.init(mc.level, step)) {
         return false;
      }

      if (mc.level.getFluidState(step).is(FluidTags.LAVA)) {
         return false;
      }

      for (int drop = 1; drop <= 4; drop++) {
         BlockPos below = step.below(drop);
         if (!cu.init(mc.level, below)) {
            return false;
         }

         if (mc.level.getFluidState(below).is(FluidTags.LAVA)) {
            return false;
         }

         VoxelShape shape = mc.level.getBlockState(below).getCollisionShape(mc.level, below);
         if (!shape.isEmpty()) {
            return drop <= 3
               && autismclient.ai.a.AiMotionSafety.putAt_Z_Vec3_Vec3(
                  mc.player.position(), new Vec3(step.getX() + 0.5, below.getY() + shape.max(Axis.Y), step.getZ() + 0.5)
               );
         }
      }

      return false;
   }

   static int init_I_Minecraft(Minecraft mc, Entity target, double maxHorizontal) {
      double dx = target.getX() - mc.player.getX();
      double dz = target.getZ() - mc.player.getZ();
      double horizontal = Math.hypot(dx, dz);
      double below = mc.player.getY() - target.getY();
      double dropAllowance = !mc.player.onGround() && !mc.player.isInWater() && !mc.player.onClimbable() ? 1.3 : 1.2;
      if (!(horizontal > maxHorizontal) && !(below > dropAllowance) && !(below < -1.2)) {
         if (!mc.player.hasLineOfSight(target)) {
            return -1;
         }

         int floorY = mc.player.blockPosition().getY();
         int startY = floorY;
         int stepAhead = 0;
         MutableBlockPos cell = new MutableBlockPos();
         int steps = (int)Math.ceil(horizontal * 2.0);

         for (int i = 1; i <= steps; i++) {
            double t = (double)i / steps;
            int x = Mth.floor(mc.player.getX() + dx * t);
            int z = Mth.floor(mc.player.getZ() + dz * t);
            int found = -2147483648;

            for (int y = floorY + 1; y >= floorY - 1 && found == -2147483648; y--) {
               if (init_Z_Minecraft(mc, cell, x, y, z)) {
                  found = y;
               }
            }

            if (found == -2147483648) {
               return -1;
            }

            if (found > startY && horizontal * t <= 1.2) {
               stepAhead = 1;
            }

            floorY = found;
         }

         return stepAhead;
      } else {
         return -1;
      }
   }

   private static boolean init_Z_Minecraft(Minecraft mc, MutableBlockPos cell, int x, int y, int z) {
      BlockState floor = mc.level.getBlockState(cell.set(x, y - 1, z));
      if (floor.getFluidState().isEmpty() && !floor.getCollisionShape(mc.level, cell).isEmpty()) {
         for (int h = 0; h <= 1; h++) {
            BlockState state = mc.level.getBlockState(cell.set(x, y + h, z));
            if (!state.getFluidState().isEmpty() || !state.getCollisionShape(mc.level, cell).isEmpty()) {
               return false;
            }
         }

         return true;
      } else {
         return false;
      }
   }

   private static af.Inner_DataUtil init() {
      return af.load_af_DataUtil("minecraft:shield");
   }

   static boolean init_Z(Minecraft mc) {
      BlockPos feet = mc.player.blockPosition();

      for (int dx = -1; dx <= 1; dx++) {
         for (int dz = -1; dz <= 1; dz++) {
            for (int dy = -1; dy <= 0; dy++) {
               if (mc.level.getBlockState(feet.offset(dx, dy, dz)).is(Blocks.FARMLAND)) {
                  return true;
               }
            }
         }
      }

      return false;
   }

   static boolean init_Z_AABB(AABB target, Vec3 eye, double reach) {
      return target.distanceToSqr(eye.add(0.0, 1.25, 0.0)) <= reach * reach;
   }

   static Vec3 init_Vec3_BlockPos_Vec3(BlockPos base, Vec3 toward) {
      double x = base.getX() + 0.5;
      double z = base.getZ() + 0.5;
      double dx = toward.x - x;
      double dz = toward.z - z;
      double scale = Math.max(Math.abs(dx), Math.abs(dz));
      return scale < 0.01 ? new Vec3(x, base.getY(), z) : new Vec3(x + dx / scale * 0.56, base.getY(), z + dz / scale * 0.56);
   }

   static AiOperation init(JsonObject args) {
      final Minecraft mc = Minecraft.getInstance();
      if (mc.player != null && mc.level != null) {
         final String spec = ag.init_Str_JsonObject(args, "target", "hostiles").trim();
         final int explicitId = ag.init_I_JsonObject(args, "entityId", -1);
         if ((spec.equals("minecraft:blaze") || spec.equals("blaze") || explicitId >= 0 && mc.level.getEntity(explicitId) instanceof Blaze)
            && ag.init_Z_JsonObject(args, "approach", true)
            && !ag.init_Z_JsonObject(args, "hold", false)
            && ag.init_I_JsonObject(args, "perch", 0) == 0) {
            return UiHelper.init(args);
         }

         final int kills = Math.max(1, Math.min(256, ag.init_I_JsonObject(args, "count", 1)));
         final double radius = Math.max(4.0, Math.min(128.0, ag.init_D_JsonObject(args, "radius", 16.0)));
         final float retreatHealth = (float)Math.max(0.0, Math.min(20.0, ag.init_D_JsonObject(args, "retreatHealth", 7.0)));
         final int perch = Math.max(0, Math.min(3, ag.init_I_JsonObject(args, "perch", 0)));
         final boolean approach = perch == 0 && ag.init_Z_JsonObject(args, "approach", true);
         final boolean hold = perch > 0 || ag.init_Z_JsonObject(args, "hold", false);
         final int timeoutSeconds = Math.max(5, Math.min(1800, ag.init_I_JsonObject(args, "timeoutSeconds", 120)));
         final boolean useShield = ag.init_Z_JsonObject(args, "useShield", false);
         final boolean untilClear = ag.init_Z_JsonObject(args, "untilClear", false);
         final int eatBelow = Math.max(0, Math.min(20, ag.init_I_JsonObject(args, "eatBelow", 0)));
         final String healWith = ag.init_Str_JsonObject(args, "healWith", "");
         final af.Inner_DataUtil healTarget = healWith.isBlank() ? null : af.load_af_DataUtil(healWith);
         final ValueSink.Inner_CachePool priority = ValueSink.Inner_CachePool.init(ag.init_Str_JsonObject(args, "priority", "nearest"));
         cc service = cc.load_cc();
         final boolean silentAim = service != null && service.init_AiSettings().aiSilentAim;
         final Set<EntityType<?>> types = explicitId >= 0 ? Set.of() : init(spec);
         final Set<String> sheepColors = new HashSet<>();

         for (String color : ag.init_Str_JsonObject(args, "sheepColors", "").split(",")) {
            if (!color.isBlank()) {
               sheepColors.add(color.trim());
            }
         }

         return new AiOperation() {
            final long init_J = System.nanoTime() + timeoutSeconds * 1000000000L;
            final Object init_Obj = mc.level;
            final Object load_Obj = mc.getConnection();
            final LocalPlayer init_LocalPlayer = mc.player;
            final UUID init_UUID = explicitId >= 0 && mc.level.getEntity(explicitId) != null ? mc.level.getEntity(explicitId).getUUID() : null;
            final autismclient.ai.a.DataUtil init_DataUtil = new autismclient.ai.a.DataUtil();
            final int init_I = mc.player.getInventory().getSelectedSlot();
            final float init_F = mc.player.getHealth();
            final DataPipe init_DataPipe = new DataPipe();
            Entity init_Entity;
            AiOperation init_AiOperation;
            AiOperation load_AiOperation;
            AiOperation read_AiOperation;
            AiOperation write_AiOperation;
            int load_I;
            int read_I;
            int write_I;
            int update_I;
            int sync_I;
            int flush_I;
            int close_I;
            String init_Str = "initial target";
            boolean init_Z;
            int open_I;
            int poll_I;
            int push_I;
            int pop_I;
            boolean load_Z = true;
            boolean read_Z;
            boolean write_Z;
            JsonObject init_JsonObject = new JsonObject();
            boolean update_Z;
            boolean sync_Z;
            boolean flush_Z;
            boolean close_Z;
            int tick;
            String load_Str;
            String read_Str;
            float load_F;
            AiOperation update_AiOperation;
            int run;
            BlockPos init_BlockPos;
            boolean open_Z;
            int get;
            int set;
            int put = -1;
            int add;
            double init_D = 1.7976931348623157E308;
            int clear;
            int reset;
            boolean poll_Z;
            boolean push_Z;
            final autismclient.ai.a.DataPipe init_DataPipe_2 = new autismclient.ai.a.DataPipe().init_DataPipe();
            boolean pop_Z;
            Entity load_Entity;
            int start;
            int stop;
            int next;
            Vec3 init_Vec3;
            int prev;
            int valid = -1;
            double load_D;
            long load_J;
            int size;
            int data;
            int value;
            final Map<UUID, Long> init_Map;

            {
               this.init_DataUtil.init_V_DataUtil_DataUtil(autismclient.ai.a.DataUtil.Inner_DataUtil.write);
               this.init_DataUtil.init_V_Z(silentAim);
               this.load_F = this.init_F;
               if (useShield && !k.init_Z(mc)) {
                  af.init_Z(ValueSink.init());
               }

               this.init_Map = ValueSink.init_Map;
            }

            @Override
            public AiOperation.Result tick() {
               if (this.read_Z
                  || mc.player == null
                  || mc.level == null
                  || mc.level != this.init_Obj
                  || mc.getConnection() != this.load_Obj
                  || mc.player != this.init_LocalPlayer) {
                  return this.init(AiOperation.Status.CANCELLED, "Fight cancelled or world/player changed");
               }

               if (!mc.player.isAlive()) {
                  return this.init(AiOperation.Status.FAILED, "Player died during the fight");
               }

               this.init_DataPipe.init(mc);
               this.load_I = this.init_DataPipe.init_I();
               this.sync_I++;
               this.load_F = Math.min(this.load_F, mc.player.getHealth());
               if (System.nanoTime() > this.init_J) {
                  return this.init(AiOperation.Status.TIMED_OUT, "Fight deadline reached");
               }

               if (this.open_Z) {
                  mc.options.keyShift.setDown(true);
               }

               if (!cv.init_Z() && !this.open_Z && mc.player.onGround() && u.init_Z(mc)) {
                  BlockPos out = u.init_BlockPos(mc);
                  if (out != null) {
                     this.flush();
                     this.sync_V();
                     int[] keys = autismclient.ai.a.AiMotionSafety.putAt_IArr_F_F(
                        mc.player.getYRot(), autismclient.ai.a.NodeWalker.init_F_D(mc.player.getX(), mc.player.getZ(), out.getX() + 0.5, out.getZ() + 0.5)
                     );
                     mc.options.keyUp.setDown(keys[0] > 0);
                     mc.options.keyDown.setDown(keys[0] < 0);
                     mc.options.keyRight.setDown(keys[1] > 0);
                     mc.options.keyLeft.setDown(keys[1] < 0);
                     return this.load("Out of the fire");
                  }
               }

               if (!cv.init_Z() && !this.open_Z) {
                  Creeper blast = ValueSink.init_Creeper(mc);
                  if (blast != null && (blast != this.init_Entity || blast.getSwelling(1.0F) >= 0.08F)) {
                     this.write_Z = this.write_Z | this.requiresReconciliationAfterEmergency();
                     this.flush();
                     if (this.load_AiOperation != null) {
                        this.load_AiOperation.cancel();
                        this.load_AiOperation = null;
                     }

                     if (this.read_AiOperation != null) {
                        this.read_AiOperation.cancel();
                        this.read_AiOperation = null;
                     }

                     this.sync_V();
                     this.load_V(blast);
                     return this.load("Escaping the most urgent creeper fuse before attacking");
                  }
               }

               if (this.read_AiOperation != null) {
                  AiOperation.Result r = this.read_AiOperation.tick();
                  if (!r.terminal()) {
                     return this.load("Using " + healWith + " before re-engaging");
                  }

                  this.init_V_AiOperation_Result(r);
                  this.read_AiOperation.cancel();
                  this.read_AiOperation = null;
                  return this.load("Re-engaging after " + healWith);
               } else {
                  if (this.write_AiOperation != null) {
                     AiOperation.Result defense = this.write_AiOperation.tick();
                     if (!defense.terminal()) {
                        return this.load("Answering an unblockable projectile: " + defense.message());
                     }

                     this.write_AiOperation.cancel();
                     this.write_AiOperation = null;
                     if (defense.status() == AiOperation.Status.UNCERTAIN) {
                        this.cancel();
                        return defense;
                     }
                  }

                  if (!cv.init_Z() && this.canInterruptNow() && autismclient.ai.a.AiMotionSafety.fill_Z()) {
                     by.Inner_StreamUtil shot = ValueSink.init_by_StreamUtil(mc);
                     if (shot != null && ValueSink.init(shot) <= 8 && (!k.init(mc, mc.level.getEntity(shot.entityId())) || !k.init(mc, ValueSink.init(shot)))) {
                        this.flush();
                        this.close();
                        this.write_AiOperation = p.init(ag.init_JsonObject_ObjArr("deflect", true, "shield", true, "timeoutSeconds", 4));
                        AiOperation.Result defense = this.write_AiOperation.tick();
                        if (defense.terminal()) {
                           this.write_AiOperation.cancel();
                           this.write_AiOperation = null;
                           if (defense.status() == AiOperation.Status.UNCERTAIN) {
                              this.cancel();
                              return defense;
                           }
                        }

                        return this.load("Dodging a projectile the shield cannot stop in time: " + defense.message());
                     }
                  }

                  if (perch > 0) {
                     AiOperation.Result held = this.load_AiOperation_Result_8();
                     if (held != null) {
                        return held;
                     }
                  } else if (hold) {
                     AiOperation.Result held = this.read_AiOperation_Result_5();
                     if (held != null) {
                        return held;
                     }
                  }

                  if (mc.player.getHealth() <= retreatHealth && !this.open_Z) {
                     if (healTarget != null && !this.sync_Z && af.init_I(healTarget) > 0) {
                        this.sync_Z = true;
                        this.flush();
                        this.close();
                        this.read_AiOperation = this.init_AiOperation_2();
                        return this.load("Retreating to use " + healWith);
                     } else {
                        this.read_Str = ValueSink.init(this.sync_Z);
                        return this.init(
                           A.start,
                           "Disengaged at " + (int)mc.player.getHealth() + " health (retreat threshold " + (int)retreatHealth + "); heal before re-engaging"
                        );
                     }
                  } else {
                     if (this.load_I >= kills) {
                        return this.init(AiOperation.Status.SUCCEEDED, "Killed " + this.load_I + " target(s)");
                     }

                     if (eatBelow > 0) {
                        if (this.load_AiOperation != null) {
                           if (!this.update_Z_2()) {
                              this.write_Z = this.write_Z | this.load_AiOperation.requiresReconciliationAfterEmergency();
                              this.load_AiOperation.cancel();
                              this.load_AiOperation = null;
                              return this.load("A threat closed in while eating; re-engaging");
                           }

                           AiOperation.Result r = this.load_AiOperation.tick();
                           if (!r.terminal()) {
                              return this.load("Eating before re-engaging");
                           }

                           this.init_V_AiOperation_Result(r);
                           this.load_AiOperation.cancel();
                           this.load_AiOperation = null;
                           return this.load("Re-engaging after eating");
                        }

                        if (mc.player.getFoodData().getFoodLevel() < eatBelow && this.update_Z_2()) {
                           this.flush();
                           this.close();
                           this.load_AiOperation = cq.init(ag.init_JsonObject_ObjArr("timeoutSeconds", 15));
                           return this.load("Disengaging to eat");
                        }
                     }

                     if (this.init_Z
                        && this.init_Entity != null
                        && (
                           !this.init_Entity.isAlive()
                              || this.init_Entity.isRemoved()
                              || this.init_Entity instanceof LivingEntity dying && dying.isDeadOrDying()
                        )) {
                        this.init_Z = false;
                        this.init_Entity = null;
                        this.flush();
                        this.close();
                     }

                     if (this.init_Entity != null && this.init_Entity instanceof LivingEntity living && living.isDeadOrDying()) {
                        this.init_Entity = null;
                        this.flush();
                        this.close();
                        if (this.load_I >= kills) {
                           return this.init(AiOperation.Status.SUCCEEDED, "Killed " + this.load_I + " target(s)");
                        }
                     }

                     if (this.init_Entity != null
                        && (!this.init_Entity.isAlive() || this.init_Entity.isRemoved() || mc.level.getEntity(this.init_Entity.getId()) != this.init_Entity)) {
                        this.init_Entity = null;
                        this.flush();
                        this.close();
                     }

                     if (this.init_Entity == null) {
                        this.init_Entity = this.init_Entity_3();
                        this.flush_I = this.sync_I;
                        if (this.init_Entity instanceof LivingEntity requested) {
                           this.init_DataPipe.init(requested);
                        }

                        this.read_I = 0;
                        this.open_I = 0;
                        this.next = 0;
                        this.init_Vec3 = null;
                        this.tick = 0;
                        if (this.init_Entity == null) {
                           AiOperation.Result waiting = this.init_AiOperation_Result_9();
                           if (waiting != null) {
                              return waiting;
                           }

                           this.close();
                           if (CacheEntry.init_Z_I(this.load_I, kills, untilClear, untilClear && X.init_X_BufferHelper().hostilesNear() == 0)) {
                              return this.init(AiOperation.Status.SUCCEEDED, "Threats cleared after " + this.load_I + " verified kill(s)");
                           }

                           return this.init(A.add, "No matching live target within " + (int)radius + " blocks");
                        }
                     }

                     if (this.init_Z && this.init_Entity.distanceToSqr(mc.player) > 49.0) {
                        this.init_Entity = null;
                        this.init_Z = false;
                        this.flush();
                        this.close();
                        return this.load("Defensive threat left; reacquiring the requested target");
                     }

                     if (!cv.init_Z() && !this.init_Z && --this.update_I <= 0) {
                        this.update_I = 4;
                        Entity striker = this.load_Entity_2();
                        if (striker != null && striker != this.init_Entity && !this.load_Z(this.init_Entity)) {
                           this.init(striker, "melee attacker at the body");
                           this.read_I = 0;
                           this.open_I = 0;
                           this.next = 0;
                           this.init_Vec3 = null;
                        }
                     }

                     this.write_V_3();
                     double distance = mc.player.distanceTo(this.init_Entity);
                     double reach = k.init_D(mc);
                     boolean creeperSwing = !cv.init_Z()
                        && this.init_Entity instanceof Creeper early
                        && early.getSwelling(1.0F) < 0.08F
                        && mc.player.getAttackStrengthScale(0.0F) >= 0.92F
                        && this.init_Entity.getBoundingBox().distanceToSqr(mc.player.getEyePosition())
                           <= ValueSink.init_D_D_Z(reach, false) * ValueSink.init_D_D_Z(reach, false);
                     if (!creeperSwing && this.init_Entity instanceof Creeper creeper && creeper.getSwellDir() > 0 && distance < (cv.init_Z() ? 6.0 : 7.5)) {
                        this.flush();
                        this.load_V(this.init_Entity);
                        if (cv.init_Z()) {
                           this.init_DataUtil.init_V_Vec3(autismclient.ai.a.DataUtil.load_Vec3_Entity(this.init_Entity));
                           this.init_DataUtil.load_V();
                        }

                        return this.load("Backing out of creeper blast range");
                     } else {
                        if (!cv.init_Z() && k.init_Z(mc)) {
                           by.Inner_StreamUtil coming = ValueSink.load(mc);
                           int in = coming == null ? 2147483647 : ValueSink.init(coming);
                           boolean raised = mc.player.isUsingItem() && mc.player.getUseItem().is(Items.SHIELD);
                           if (coming != null && in <= 8 && (raised || in >= 5)) {
                              this.flush();
                              this.init_V_13();
                              this.init_DataUtil
                                 .init_V_Vec3(ValueSink.init_Vec3_Minecraft_Vec3(mc, autismclient.ai.a.DataUtil.load_Vec3_Entity(this.init_Entity)));
                              this.init_DataUtil.load_V();
                              if (!this.update_Z) {
                                 this.update_Z = true;
                                 mc.options.keyUse.setDown(true);
                              }

                              return this.load("Shield up against the incoming " + coming.type().replace("minecraft:", ""));
                           }
                        }

                        double hitDistance = Math.sqrt(this.init_Entity.getBoundingBox().distanceToSqr(mc.player.getEyePosition()));
                        double attackDistance = ValueSink.init_D_D_Z(reach, this.open_Z || hold);
                        if ((this.open_Z || hold) && hitDistance > attackDistance && ++this.open_I > 12) {
                           Entity next = this.init_Entity_3();
                           if (next != null
                              && next != this.init_Entity
                              && next.getBoundingBox().distanceToSqr(mc.player.getEyePosition()) <= attackDistance * attackDistance) {
                              this.init(next, "reachable target while holding ground");
                              hitDistance = Math.sqrt(this.init_Entity.getBoundingBox().distanceToSqr(mc.player.getEyePosition()));
                              this.read_I = this.open_I = 0;
                              this.next = 0;
                              this.init_Vec3 = null;
                           }
                        }

                        if (!(hitDistance > attackDistance)) {
                           this.open_I = 0;
                           Vec3 hitPoint = autismclient.ai.a.DataUtil.load_Vec3_Entity(this.init_Entity);
                           if (!this.open_Z && !hold && !autismclient.ai.a.DataUtil.init_Z_Entity_Vec3(this.init_Entity, hitPoint)) {
                              return !approach ? this.init(A.close, "Target's attack ray is obstructed and approach is disabled") : this.init(distance, true);
                           }

                           this.flush();
                           this.valid = -1;
                           if (this.open_Z
                              && !autismclient.ai.a.DataUtil.init_Z_Entity_Vec3(this.init_Entity, hitPoint)
                              && this.next < 20
                              && mc.player.onGround()) {
                              if (this.init_Vec3 == null || !this.init_Z_Vec3(this.init_Vec3)) {
                                 this.init_Vec3 = this.init_Vec3_5();
                              }

                              if (this.init_Vec3 != null && Math.hypot(this.init_Vec3.x - mc.player.getX(), this.init_Vec3.z - mc.player.getZ()) > 0.04) {
                                 double x = this.init_Vec3.x;
                                 double z = this.init_Vec3.z;
                                 this.next++;
                                 this.init_DataUtil.init_V_Vec3(hitPoint);
                                 this.init_DataUtil.load_V();
                                 this.init(x, z);
                                 return this.load("Exposing the target past the pillar");
                              }
                           }

                           if (this.open_Z) {
                              this.close();
                           }

                           this.init_DataUtil.init_V_Vec3(hitPoint);
                           this.init_DataUtil.load_V();
                           if (this.init_DataUtil.load_Z_Entity(this.init_Entity)) {
                              this.read_I = 0;
                              this.write_I = 0;
                              this.write_V_3();
                              if (!cv.init_Z()) {
                                 return this.init_AiOperation_Result_Vec3(hitPoint);
                              }

                              this.update_V_2();
                              if (this.load_Z_6()) {
                                 this.load_V_6();
                              }

                              Entity volley = k.init_Z(mc) ? ValueSink.init_Entity(mc) : null;
                              if (volley != null && volley != this.init_Entity) {
                                 this.init_DataUtil.init_V_Vec3(autismclient.ai.a.DataUtil.load_Vec3_Entity(volley));
                                 this.init_DataUtil.load_V();
                                 return this.load("Shield up against a blaze");
                              }

                              if (mc.player.getAttackStrengthScale(0.0F) >= 0.92F && volley == null && k.init(mc, this.init_Entity, this.init_DataUtil)) {
                                 mc.options.keySprint.setDown(false);
                                 this.sync_V();
                                 if (!this.init_Z && this.init_Entity instanceof LivingEntity requested) {
                                    this.init_DataPipe.init(requested);
                                 }

                                 mc.gameMode.attack(mc.player, this.init_Entity);
                                 Compat263.swing(mc.player, InteractionHand.MAIN_HAND);
                                 mc.player.resetAttackStrengthTicker();
                                 this.push_I++;
                                 return this.load(
                                    "Hit "
                                       + BuiltInRegistries.ENTITY_TYPE.getKey(this.init_Entity.getType()).getPath()
                                       + " ("
                                       + this.load_I
                                       + "/"
                                       + kills
                                       + ")"
                                 );
                              } else {
                                 this.read_V_4();
                                 return this.load("Waiting out the attack cooldown");
                              }
                           } else if (++this.read_I <= 40) {
                              if (this.load_Z_6()) {
                                 this.load_V_6();
                              }

                              return this.load("Tracking target");
                           } else {
                              Entity dropped = this.init_Entity;
                              this.init_Entity = null;
                              if (!this.open_Z && !hold) {
                                 ValueSink.init_V_Entity_J(dropped, System.nanoTime() + 15000000000L);
                              } else if (++this.write_I >= 3) {
                                 ValueSink.init_V_Entity_J(dropped, System.nanoTime() + 15000000000L);
                                 this.close();
                                 return this.init(
                                    AiOperation.Status.SUCCEEDED,
                                    "The " + BuiltInRegistries.ENTITY_TYPE.getKey(dropped.getType()).getPath() + " cannot be hit from here; going on"
                                 );
                              }

                              return this.load("Could not acquire " + BuiltInRegistries.ENTITY_TYPE.getKey(dropped.getType()).getPath());
                           }
                        } else if (this.open_Z || hold) {
                           this.close();
                           boolean hasShield = k.init_Z(mc);
                           Entity shooter = hasShield ? (cv.init_Z() ? ValueSink.init_Entity(mc) : cv.init(mc, 12)) : null;
                           by.Inner_StreamUtil coming = hasShield && !cv.init_Z() ? ValueSink.load(mc) : null;
                           if (coming != null) {
                              this.init_DataUtil
                                 .init_V_Vec3(
                                    ValueSink.init_Vec3_Minecraft_Vec3(
                                       mc, autismclient.ai.a.DataUtil.load_Vec3_Entity(shooter != null ? shooter : this.init_Entity)
                                    )
                                 );
                           } else {
                              this.init_DataUtil.init_V_Vec3(autismclient.ai.a.DataUtil.load_Vec3_Entity(shooter != null ? shooter : this.init_Entity));
                           }

                           this.init_DataUtil.load_V();
                           boolean wantShield = hasShield && (shooter != null || coming != null || this.sync_Z());
                           if (wantShield != this.update_Z) {
                              this.update_Z = wantShield;
                              mc.options.keyUse.setDown(wantShield);
                           }

                           return this.load(shooter != null ? "Shield up against a blaze" : "Holding the pillar top");
                        } else if (!approach) {
                           this.close();
                           return this.init(A.close, "Target is out of reach and approach is disabled");
                        } else if (!cv.init_Z()
                           && cv.load(cv.init_cv_DataUtil(this.init_Entity))
                           && distance < 7.0
                           && cv.init(this.init_Entity, 3)
                           && mc.player.hasLineOfSight(this.init_Entity)) {
                           this.flush();
                           this.close();
                           this.init_DataUtil.init_V_Vec3(autismclient.ai.a.DataUtil.load_Vec3_Entity(this.init_Entity));
                           this.init_DataUtil.load_V();
                           return this.load("Letting the " + BuiltInRegistries.ENTITY_TYPE.getKey(this.init_Entity.getType()).getPath() + " come");
                        } else if (!cv.init_Z() && types.isEmpty() && explicitId < 0 && this.init_Entity instanceof Enderman && distance > 10.0) {
                           Entity dropped = this.init_Entity;
                           this.init_Entity = null;
                           this.flush();
                           this.close();
                           ValueSink.init_V_Entity_J(dropped, System.nanoTime() + 8000000000L);
                           return this.load("The enderman teleported off; not chasing it");
                        } else {
                           int run = ValueSink.init_I_Minecraft(mc, this.init_Entity, 16.0);
                           return run >= 0 ? this.load(distance, run == 1) : this.init_AiOperation_Result_D(distance);
                        }
                     }
                  }
               }
            }

            AiOperation.Result init_AiOperation_Result_Vec3(Vec3 hitPoint) {
               boolean shield = k.init_Z(mc);
               float strength = mc.player.getAttackStrengthScale(0.0F);
               double recharge = 20.0 / Math.max(0.1, mc.player.getAttributeValue(Attributes.ATTACK_SPEED));
               double readyIn = Math.max(0.0, (0.92F - strength) * recharge);
               boolean ready = strength >= 0.92F;
               cv.Inner_DataUtil profile = cv.init_cv_DataUtil(this.init_Entity);
               Entity volley = shield ? cv.init(mc, 12) : null;
               if (volley == null || volley == this.init_Entity && ready && cv.init_I(mc, volley) > 11) {
                  if (!cv.init_Z() && !this.open_Z && !hold && cv.load(profile)) {
                     double knock = profile == cv.Inner_DataUtil.run ? 6.0 : 2.5;
                     if (this.init(mc.player.getX(), mc.player.getZ(), knock)) {
                        int side = this.init_I_D(knock);
                        if (side != 0) {
                           mc.options.keySprint.setDown(false);
                           mc.options.keyJump.setDown(false);
                           mc.options.keyUp.setDown(false);
                           mc.options.keyDown.setDown(false);
                           mc.options.keyRight.setDown(side > 0 && mc.player.onGround());
                           mc.options.keyLeft.setDown(side < 0 && mc.player.onGround());
                           this.init_DataUtil.init_V_Vec3(autismclient.ai.a.DataUtil.load_Vec3_Entity(this.init_Entity));
                           this.init_DataUtil.load_V();
                           boolean up = shield && !ready;
                           if (up != this.update_Z) {
                              this.update_Z = up;
                              mc.options.keyUse.setDown(up);
                           }

                           return this.load("Turning the fight off the edge behind");
                        }
                     }
                  }

                  double gap = this.init_Entity instanceof Mob mob ? cv.init(mob, mc.player.getBoundingBox()) : 10.0;
                  double reach = k.init_D(mc);
                  double slack = ValueSink.init_D_D_Z(reach, this.open_Z || hold)
                     - Math.sqrt(this.init_Entity.getBoundingBox().distanceToSqr(mc.player.getEyePosition()));
                  boolean meleeMob = cv.load(profile) && !this.open_Z;
                  boolean kite = meleeMob && cv.init_D(this.init_Entity) < 0.19;
                  boolean cornered = kite && !cv.init_Z() && !this.open_Z && !this.init_Z_Entity(this.init_Entity);
                  if (cornered) {
                     kite = false;
                  }

                  if (ready && !mc.player.onGround() && mc.player.getDeltaMovement().y > 0.0 && this.data > 0 && this.data < 12) {
                     this.data++;
                     return this.load("Waiting for the fall to crit");
                  }

                  if (ready && k.init(mc, this.init_Entity, this.init_DataUtil)) {
                     this.sync_V();
                     mc.options.keySprint.setDown(false);
                     if (!this.init_Z && this.init_Entity instanceof LivingEntity requested) {
                        this.init_DataPipe.init(requested);
                     }

                     mc.gameMode.attack(mc.player, this.init_Entity);
                     Compat263.swing(mc.player, InteractionHand.MAIN_HAND);
                     mc.player.resetAttackStrengthTicker();
                     this.push_I++;
                     if (kite && gap < 1.0) {
                        this.init_V_I(-1);
                     } else {
                        this.init_V_13();
                     }

                     return this.load(
                        "Hit " + BuiltInRegistries.ENTITY_TYPE.getKey(this.init_Entity.getType()).getPath() + " (" + this.load_I + "/" + kills + ")"
                     );
                  } else {
                     Entity crowd = null;
                     double crowdGap = 0.8;

                     for (Entity other : mc.level.entitiesForRendering()) {
                        if (other != this.init_Entity && other instanceof Mob mob && mob.isAlive() && !(mob.distanceToSqr(mc.player) > 64.0)) {
                           if (other instanceof Creeper creeper && creeper.getSwellDir() > 0 && creeper.distanceTo(mc.player) < 7.5) {
                              this.sync_V();
                              this.load_V(creeper);
                              return this.load("Out of the creeper's blast");
                           }

                           if (cv.load(cv.init_cv_DataUtil(other)) && cv.init_Z(mc, other)) {
                              double g = cv.init(mob, mc.player.getBoundingBox());
                              if (g < crowdGap) {
                                 crowdGap = g;
                                 crowd = other;
                              }
                           }
                        }
                     }

                     if (crowd != null) {
                        boolean room = this.init_Z_Entity(crowd);
                        double attackRange = ValueSink.init_D_D_Z(k.init_D(mc), this.open_Z || hold);
                        boolean otherHittable = mc.player.hasLineOfSight(crowd)
                           && crowd.getBoundingBox().distanceToSqr(mc.player.getEyePosition()) <= attackRange * attackRange;
                        if (!cv.init_Z()
                           && (!room || ++this.value > 10)
                           && CacheEntry.init_Z_D_2(gap, crowdGap, this.sync_I - this.flush_I, slack >= 0.0, otherHittable)) {
                           this.value = 0;
                           this.init(crowd, "closer reachable melee threat");
                           return this.load("Turning on the " + BuiltInRegistries.ENTITY_TYPE.getKey(crowd.getType()).getPath() + " at the player");
                        }

                        if (room) {
                           this.init_V_Entity(crowd);
                           return this.load("Out of the " + BuiltInRegistries.ENTITY_TYPE.getKey(crowd.getType()).getPath() + "'s reach");
                        }

                        kite = false;
                        cornered = true;
                     }

                     this.value = 0;
                     boolean wantShield = shield && (this.sync_Z() || meleeMob && !kite && gap < 1.0 && readyIn > 3.0);
                     if (wantShield != this.update_Z) {
                        this.update_Z = wantShield;
                        mc.options.keyUse.setDown(wantShield);
                     }

                     if (!meleeMob) {
                        if (this.load_Z_6()) {
                           this.load_V_6();
                        }

                        this.read_V_4();
                        return this.load("Waiting out the attack cooldown");
                     } else {
                        int radial = ValueSink.init_I_D(gap, slack, readyIn, cv.init_D(this.init_Entity), kite);
                        this.init_V_I(radial);
                        if (cornered && radial == 0 && slack > 0.2 && readyIn <= 7.0 && this.write_Z_2()) {
                           this.read_V_4();
                        } else if (radial == 0 && gap > 0.6 && slack > 0.2 && readyIn < 8.0 && !wantShield) {
                           this.read_V_4();
                        } else {
                           mc.options.keyJump.setDown(false);
                        }

                        if (mc.options.keyJump.isDown()) {
                           if (this.data == 0) {
                              this.data = 1;
                           }
                        } else if (mc.player.onGround()) {
                           this.data = 0;
                        } else if (this.data > 0) {
                           this.data++;
                        }

                        return this.load(
                           radial < 0 ? "Keeping out of its reach" : (radial > 0 ? "Stepping in for the swing" : "Waiting out the attack cooldown")
                        );
                     }
                  }
               } else {
                  this.init_V_13();
                  this.init_DataUtil.init_V_Vec3(autismclient.ai.a.DataUtil.load_Vec3_Entity(volley));
                  this.init_DataUtil.load_V();
                  if (!this.update_Z) {
                     this.update_Z = true;
                     mc.options.keyUse.setDown(true);
                  }

                  return this.load("Shield up against the " + BuiltInRegistries.ENTITY_TYPE.getKey(volley.getType()).getPath());
               }
            }

            boolean init(double x, double z, double knock) {
               double dx = x - this.init_Entity.getX();
               double dz = z - this.init_Entity.getZ();
               double len = Math.hypot(dx, dz);
               if (len < 0.001) {
                  return false;
               }

               dx /= len;
               dz /= len;
               int feetY = Mth.floor(mc.player.getY() + 0.01);

               for (double t = 0.5; t <= knock; t += 0.5) {
                  int cx = Mth.floor(x + dx * t);
                  int cz = Mth.floor(z + dz * t);
                  boolean floor = false;

                  for (int dy = 1; dy >= -4; dy--) {
                     BlockPos cell = new BlockPos(cx, feetY + dy, cz);
                     if (mc.level.getFluidState(cell).is(FluidTags.LAVA)) {
                        return true;
                     }

                     if (dy <= 0 && !mc.level.getBlockState(cell).getCollisionShape(mc.level, cell).isEmpty()) {
                        if (dy == 0) {
                           return false;
                        }

                        floor = true;
                        break;
                     }
                  }

                  if (!floor) {
                     return true;
                  }
               }

               return false;
            }

            int init_I_D(double knock) {
               double yaw = Math.toRadians(mc.player.getYRot());
               double rx = -Math.cos(yaw);
               double rz = -Math.sin(yaw);

               for (int side : new int[]{1, -1}) {
                  double x = mc.player.getX() + rx * side * 1.5;
                  double z = mc.player.getZ() + rz * side * 1.5;
                  if (ValueSink.init_Z_Minecraft_BlockPos(mc, BlockPos.containing(x, mc.player.getY(), z))
                     && (!mc.player.onGround() || autismclient.ai.a.SyncManager.init(mc, 0.0, side > 0 ? -1.0 : 1.0, 6) != null)
                     && !this.init(x, z, knock)) {
                     return side;
                  }
               }

               return 0;
            }

            void init_V_I(int radial) {
               mc.options.keySprint.setDown(false);
               mc.options.keyJump.setDown(false);
               if (radial > 0) {
                  boolean safe = this.read_Z_3()
                     && autismclient.ai.a.AiMotionSafety.putAt_Z_D_D(1.0, 0.0)
                     && (!mc.player.onGround() || autismclient.ai.a.SyncManager.init(mc, 1.0, 0.0, 6) != null);
                  mc.options.keyUp.setDown(safe);
                  mc.options.keyDown.setDown(false);
                  mc.options.keyLeft.setDown(false);
                  mc.options.keyRight.setDown(false);
               } else if (radial >= 0) {
                  this.init_V_13();
               } else {
                  double radians = Math.toRadians(mc.player.getYRot());
                  boolean behind = ValueSink.init_Z_Minecraft_BlockPos(
                        mc, BlockPos.containing(mc.player.getX() + Math.sin(radians), mc.player.getY(), mc.player.getZ() - Math.cos(radians))
                     )
                     && autismclient.ai.a.AiMotionSafety.putAt_Z_D_D(-1.0, 0.0);
                  behind &= !mc.player.onGround() || autismclient.ai.a.SyncManager.init(mc, -1.0, 0.0, 6) != null;
                  if (behind) {
                     mc.options.keyUp.setDown(false);
                     mc.options.keyDown.setDown(true);
                     mc.options.keyLeft.setDown(false);
                     mc.options.keyRight.setDown(false);
                  } else {
                     mc.options.keyDown.setDown(false);
                     if (this.load_Z_6()) {
                        this.load_V_6();
                     }
                  }
               }
            }

            void init_V_Entity(Entity from) {
               float away = autismclient.ai.a.NodeWalker.init_F_D(from.getX(), from.getZ(), mc.player.getX(), mc.player.getZ());
               int[] keys = autismclient.ai.a.AiMotionSafety.putAt_IArr_F_F(mc.player.getYRot(), away);
               double radians = Math.toRadians(away);
               boolean safe = ValueSink.init_Z_Minecraft_BlockPos(
                     mc, BlockPos.containing(mc.player.getX() - Math.sin(radians), mc.player.getY(), mc.player.getZ() + Math.cos(radians))
                  )
                  && autismclient.ai.a.AiMotionSafety.putAt_Z_D_D(keys[0], -keys[1]);
               mc.options.keySprint.setDown(false);
               mc.options.keyJump.setDown(false);
               mc.options.keyUp.setDown(safe && keys[0] > 0);
               mc.options.keyDown.setDown(safe && keys[0] < 0);
               mc.options.keyRight.setDown(safe && keys[1] > 0);
               mc.options.keyLeft.setDown(safe && keys[1] < 0);
            }

            void init_V_13() {
               mc.options.keyUp.setDown(false);
               mc.options.keyDown.setDown(false);
               mc.options.keyLeft.setDown(false);
               mc.options.keyRight.setDown(false);
               mc.options.keyJump.setDown(false);
               mc.options.keySprint.setDown(false);
            }

            AiOperation.Result init_AiOperation_Result_D(double distance) {
               return this.init(distance, false);
            }

            AiOperation.Result init(double distance, boolean obstructed) {
               double reach = k.init_D(mc);
               double horizontal = Math.hypot(this.init_Entity.getX() - mc.player.getX(), this.init_Entity.getZ() - mc.player.getZ());
               BlockPos goalCell = this.init_Entity.blockPosition();
               if (!cv.init_Z()) {
                  Entity due = k.init_Z(mc) ? cv.init(mc, 12) : null;
                  if (due != null) {
                     this.flush();
                     this.init_V_13();
                     this.init_DataUtil.init_V_Vec3(autismclient.ai.a.DataUtil.load_Vec3_Entity(due));
                     this.init_DataUtil.load_V();
                     if (!this.update_Z) {
                        this.update_Z = true;
                        mc.options.keyUse.setDown(true);
                     }

                     this.load_J = System.nanoTime();
                     return this.load("Shield up against the " + BuiltInRegistries.ENTITY_TYPE.getKey(due.getType()).getPath() + " on the way in");
                  }

                  this.sync_V();
                  if (!this.init_Entity.onGround() && this.init_Entity.getBoundingBox().minY - mc.player.getEyeY() < reach * 0.8) {
                     BlockPos under = this.init_BlockPos_Entity(this.init_Entity);
                     if (under != null) {
                        goalCell = under;
                     }
                  }
               }

               boolean flier = this.init_Entity instanceof Blaze
                  || this.init_Entity instanceof Ghast
                  || this.init_Entity instanceof Phantom
                  || this.init_Entity instanceof Vex
                  || this.init_Entity.isNoGravity();
               ValueSink.Inner_DataUtil plan = goalCell != this.init_Entity.blockPosition() || !cv.init_Z() && !flier
                  ? ValueSink.Inner_DataUtil.init
                  : ValueSink.init(this.init_Entity.onGround(), horizontal, this.init_Entity.getY() - mc.player.getY(), reach);
               if (plan == ValueSink.Inner_DataUtil.write) {
                  if (!cv.init_Z() && this.init_Entity instanceof Blaze) {
                     this.flush();
                     this.init_V_13();
                     Entity due = k.init_Z(mc) ? cv.init(mc, 12) : null;
                     this.init_DataUtil.init_V_Vec3(autismclient.ai.a.DataUtil.load_Vec3_Entity(due != null ? due : this.init_Entity));
                     this.init_DataUtil.load_V();
                     boolean up = due != null;
                     if (up != this.update_Z) {
                        this.update_Z = up;
                        mc.options.keyUse.setDown(up);
                     }

                     this.load_J = System.nanoTime();
                     return this.load("Waiting for the blaze to come down");
                  } else {
                     return this.init_AiOperation_Result_Str_5("hovering above the 3-block pillar cap");
                  }
               } else {
                  if (plan != ValueSink.Inner_DataUtil.init) {
                     return this.init(plan, distance, reach);
                  }

                  if (this.init_AiOperation != null && obstructed && !this.close_Z) {
                     this.flush();
                  }

                  if (this.init_AiOperation == null) {
                     this.close_Z = obstructed;
                     this.init_AiOperation = HashUtil.init(
                        goalCell, obstructed ? 1.2 : 2.0, true, true, true, 8, autismclient.ai.a.ClassCache.Inner_IoWorker.load
                     );
                     this.flush_Z = false;
                  }

                  long now = System.nanoTime();
                  if (this.valid != this.init_Entity.getId() || distance < this.load_D - 1.0) {
                     this.valid = this.init_Entity.getId();
                     this.load_D = distance;
                     this.load_J = now;
                  } else if (autismclient.ai.a.f.init(this.init_Entity)
                     && (
                        now - this.load_J > 2500000000L
                           || distance > this.load_D + 2.0
                              && autismclient.ai.a.f.init_Z_BlockGetter_3(
                                 mc.level, this.init_Entity.getEyePosition(), mc.player.getEyePosition(), -9223372036854775808L, -9223372036854775808L
                              )
                     )) {
                     Entity dropped = this.init_Entity;
                     this.init_Entity = null;
                     this.flush();
                     this.close();
                     ValueSink.init_V_Entity_J(dropped, now + 15000000000L);
                     return this.load("Not closing on the " + BuiltInRegistries.ENTITY_TYPE.getKey(dropped.getType()).getPath());
                  }

                  AiOperation.Result legResult = this.init_AiOperation.tick();
                  if (legResult.terminal()) {
                     this.flush();
                     if (legResult.status() == AiOperation.Status.FAILED) {
                        Entity dropped = this.init_Entity;
                        this.init_Entity = null;
                        ValueSink.init_V_Entity_J(dropped, System.nanoTime() + 15000000000L);
                        return this.load("No route to " + BuiltInRegistries.ENTITY_TYPE.getKey(dropped.getType()).getPath());
                     }
                  }

                  return this.load("Closing on target (" + (int)distance + "m)");
               }
            }

            AiOperation.Result load(double distance, boolean stepUp) {
               this.flush();
               if (!cv.init_Z() && k.init_Z(mc)) {
                  Entity due = cv.init(mc, 12);
                  double reach = k.init_D(mc);
                  if (due != null
                     && (due != this.init_Entity || Math.sqrt(this.init_Entity.getBoundingBox().distanceToSqr(mc.player.getEyePosition())) > reach + 1.2)) {
                     this.init_V_13();
                     this.init_DataUtil.init_V_Vec3(autismclient.ai.a.DataUtil.load_Vec3_Entity(due));
                     this.init_DataUtil.load_V();
                     if (!this.update_Z) {
                        this.update_Z = true;
                        mc.options.keyUse.setDown(true);
                     }

                     this.load_J = System.nanoTime();
                     return this.load("Shield up against the " + BuiltInRegistries.ENTITY_TYPE.getKey(due.getType()).getPath() + " on the run in");
                  }
               }

               mc.options.keyDown.setDown(false);
               mc.options.keyLeft.setDown(false);
               mc.options.keyRight.setDown(false);
               this.sync_V();
               long now = System.nanoTime();
               if (this.valid != this.init_Entity.getId() || distance < this.load_D - 1.0) {
                  this.valid = this.init_Entity.getId();
                  this.load_D = distance;
                  this.load_J = now;
               } else if (autismclient.ai.a.f.init(this.init_Entity) && now - this.load_J > 2500000000L) {
                  Entity dropped = this.init_Entity;
                  this.init_Entity = null;
                  this.close();
                  ValueSink.init_V_Entity_J(dropped, now + 15000000000L);
                  return this.load("Not closing on the " + BuiltInRegistries.ENTITY_TYPE.getKey(dropped.getType()).getPath());
               }

               this.init_DataUtil.init_V_Vec3(autismclient.ai.a.DataUtil.load_Vec3_Entity(this.init_Entity));
               this.init_DataUtil.load_V();
               boolean facing = Math.abs(Mth.wrapDegrees(this.init_F_Entity(this.init_Entity) - mc.player.getYRot())) < 35.0F;
               boolean jump = facing && stepUp && mc.player.onGround();
               boolean safe = facing && autismclient.ai.a.AiMotionSafety.putAt_Z_D(1.0, 0.0, jump, true);
               mc.options.keyUp.setDown(safe);
               mc.options.keySprint.setDown(safe);
               mc.options.keyJump.setDown(safe && jump);
               if (facing && !safe) {
                  ValueSink.init_V_Entity_J(this.init_Entity, now + 15000000000L);
                  this.init_Entity = null;
                  if (!autismclient.ai.a.AiMotionSafety.putAt_Z_D_D(0.0, 0.0)) {
                     autismclient.ai.a.AiMotionSafety.fill_V();
                  }

                  return this.load("Leaving a chase whose movement would touch lava");
               } else {
                  return this.load("Running at the " + BuiltInRegistries.ENTITY_TYPE.getKey(this.init_Entity.getType()).getPath() + " (" + (int)distance + "m)");
               }
            }

            float init_F_Entity(Entity entity) {
               return (float)(Math.toDegrees(Math.atan2(entity.getZ() - mc.player.getZ(), entity.getX() - mc.player.getX())) - 90.0);
            }

            AiOperation.Result init(ValueSink.Inner_DataUtil plan, double distance, double reach) {
               if (this.init_AiOperation == null) {
                  BlockPos under = this.init_BlockPos_Entity(this.init_Entity);
                  if (under == null) {
                     return this.init_AiOperation_Result_Str_5("no standable, lava-free ground under the target");
                  }

                  if (plan == ValueSink.Inner_DataUtil.read) {
                     int needed = ValueSink.init_I_D_D(this.init_Entity.getY() - mc.player.getY(), reach);
                     if (needed < 0 || this.tick + needed > 3) {
                        return this.init_AiOperation_Result_Str_5("the target out-climbs the 3-block pillar cap");
                     }

                     if (!ValueSink.init_Z_Minecraft_BlockPos(mc, under)) {
                        return this.init_AiOperation_Result_Str_5("unsafe footing under the target");
                     }

                     if (!this.init_Z_11()) {
                        return this.init_AiOperation_Result_Str_5("no hotbar construction blocks to pillar with");
                     }

                     this.init_AiOperation = HashUtil.init_AiOperation_BlockPos(under.above(needed), 0.8, false, true, false, 8);
                     this.flush_Z = true;
                     this.tick += needed;
                  } else {
                     this.init_AiOperation = HashUtil.init_AiOperation_BlockPos(under, 1.2, false, false, false, 8);
                     this.flush_Z = false;
                  }
               }

               AiOperation.Result legResult = this.init_AiOperation.tick();
               if (legResult.terminal()) {
                  boolean pillar = this.flush_Z;
                  this.flush();
                  return legResult.status() != AiOperation.Status.SUCCEEDED
                     ? this.init_AiOperation_Result_Str_5(pillar ? "the pillar climb could not complete" : "no route to the ground under the target")
                     : this.load(pillar ? "Pillared up under the target" : "Under the target; re-evaluating");
               } else {
                  return this.load(this.flush_Z ? "Pillaring up to the target" : "Moving under the target (" + (int)distance + "m)");
               }
            }

            BlockPos init_BlockPos_Entity(Entity entity) {
               BlockPos at = entity.blockPosition();

               for (int drop = 0; drop < 12; at = at.below()) {
                  if (!cu.init(mc.level, at)) {
                     return null;
                  }

                  if (mc.level.getFluidState(at).is(FluidTags.LAVA)) {
                     return null;
                  }

                  if (!this.init_Z_BlockPos_3(at)) {
                     return null;
                  }

                  if (this.init_Z_BlockPos_3(at.above()) && !this.init_Z_BlockPos_3(at.below())) {
                     return at;
                  }

                  drop++;
               }

               return null;
            }

            boolean init_Z_BlockPos_3(BlockPos cell) {
               return mc.level.getBlockState(cell).getCollisionShape(mc.level, cell).isEmpty();
            }

            boolean init_Z_11() {
               for (int slot = 0; slot < 9; slot++) {
                  ItemStack stack = mc.player.getInventory().getItem(slot);
                  if (stack.getItem() instanceof BlockItem && autismclient.ai.a.UtilCore.init_Z(BuiltInRegistries.ITEM.getKey(stack.getItem()).toString())) {
                     return true;
                  }
               }

               return aV.init(mc.player.getInventory()) > 0;
            }

            AiOperation.Result init_AiOperation_Result_9() {
               if (!cv.init_Z() && this.size <= 400) {
                  Entity blaze = null;
                  double wait = radius + 16.0;

                  for (Entity entity : mc.level.entitiesForRendering()) {
                     if ((entity instanceof Blaze || entity instanceof Enderman)
                        && entity.isAlive()
                        && this.read(entity)
                        && entity.distanceToSqr(mc.player) <= wait * wait) {
                        blaze = entity;
                        break;
                     }
                  }

                  if (blaze == null) {
                     return null;
                  }

                  double reachNow = k.init_D(mc);
                  if (blaze.getBoundingBox().distanceToSqr(mc.player.getEyePosition()) <= reachNow * reachNow) {
                     this.init_Map.remove(blaze.getId());
                     this.init_Entity = blaze;
                     this.read_I = 0;
                     this.open_I = 0;
                     return this.load("In reach: fighting the " + BuiltInRegistries.ENTITY_TYPE.getKey(blaze.getType()).getPath());
                  }

                  this.size++;
                  this.flush();
                  this.init_V_13();
                  Entity due = k.init_Z(mc) ? cv.init(mc, 12) : null;
                  this.init_DataUtil.init_V_Vec3(autismclient.ai.a.DataUtil.load_Vec3_Entity(due != null ? due : blaze));
                  this.init_DataUtil.load_V();
                  boolean up = due != null;
                  if (up != this.update_Z) {
                     this.update_Z = up;
                     mc.options.keyUse.setDown(up);
                  }

                  if (this.size % 40 == 0) {
                     this.init_Map.remove(blaze.getId());
                  }

                  return this.load("Waiting for the blaze to come into reach");
               } else {
                  return null;
               }
            }

            AiOperation.Result init_AiOperation_Result_Str_5(String why) {
               if (this.init_Entity instanceof Blaze || this.init_Entity instanceof Enderman) {
                  ValueSink.init_V_Entity_J(this.init_Entity, System.nanoTime() + 2000000000L);
                  this.init_Entity = null;
                  AiOperation.Result waiting = this.init_AiOperation_Result_9();
                  if (waiting != null) {
                     return waiting;
                  }
               }

               this.load_Str = "unreachable_airborne";
               return this.init(
                  A.open, "Target is airborne out of melee reach (" + why + "); use a bow, carry construction blocks, or fight it where it must descend"
               );
            }

            boolean load_Z_6() {
               if (this.open_Z) {
                  mc.options.keyLeft.setDown(false);
                  mc.options.keyRight.setDown(false);
                  return false;
               } else {
                  return this.tick == 0 || this.init_Entity == null || this.init_Entity.onGround();
               }
            }

            void load_V_6() {
               if (++this.poll_I >= 12) {
                  this.poll_I = 0;
                  this.load_Z = mc.player.getRandom().nextBoolean();
               }

               boolean rightSafe = this.init_Z_Z(true);
               boolean leftSafe = this.init_Z_Z(false);
               if (this.load_Z && !rightSafe) {
                  this.load_Z = false;
               } else if (!this.load_Z && !leftSafe) {
                  this.load_Z = true;
               }

               boolean safe = (this.load_Z ? rightSafe : leftSafe) && autismclient.ai.a.AiMotionSafety.putAt_Z_D_D(0.0, this.load_Z ? -1.0 : 1.0);
               mc.options.keyUp.setDown(false);
               mc.options.keyDown.setDown(false);
               mc.options.keyLeft.setDown(safe && !this.load_Z);
               mc.options.keyRight.setDown(safe && this.load_Z);
               mc.options.keySprint.setDown(false);
            }

            boolean init_Z_Z(boolean right) {
               float yaw = mc.player.getYRot() + (right ? 90.0F : -90.0F);
               double radians = Math.toRadians(yaw);
               Vec3 step = mc.player.position().add(-Math.sin(radians), 0.0, Math.cos(radians));
               if (!ValueSink.init_Z_Minecraft_BlockPos(mc, BlockPos.containing(step))) {
                  return false;
               }

               if (autismclient.ai.a.SyncManager.init(mc, 0.0, right ? -1.0 : 1.0, 6) == null) {
                  return false;
               }

               if (this.init_Entity == null) {
                  return true;
               }

               double reach = ValueSink.init_D_D_Z(k.init_D(mc), false);
               Vec3 eye = mc.player.getEyePosition().add(step.x - mc.player.getX(), 0.0, step.z - mc.player.getZ());
               return this.init_Entity.getBoundingBox().distanceToSqr(eye) <= reach * reach && this.init_Z_Vec3(step);
            }

            boolean read_Z_3() {
               double radians = Math.toRadians(mc.player.getYRot());
               return ValueSink.init_Z_Minecraft_BlockPos(
                  mc, BlockPos.containing(mc.player.getX() - Math.sin(radians), mc.player.getY(), mc.player.getZ() + Math.cos(radians))
               );
            }

            void load_V(Entity source) {
               Float safeRetreat = source instanceof Creeper creeper ? StateCache.init(mc, creeper) : null;
               if (safeRetreat != null) {
                  StateCache.init(mc, safeRetreat);
               } else {
                  Vec3 away = mc.player.position().subtract(source.position());
                  if (away.lengthSqr() < 1.0E-4) {
                     away = new Vec3(1.0, 0.0, 0.0);
                  }

                  float yaw = (float)(Math.toDegrees(Math.atan2(away.z, away.x)) - 90.0);
                  mc.player.setYRot(yaw);
                  if (this.read_Z_3() && autismclient.ai.a.AiMotionSafety.putAt_Z_D(1.0, 0.0, false, mc.player.getFoodData().getFoodLevel() > 6)) {
                     mc.options.keyLeft.setDown(false);
                     mc.options.keyRight.setDown(false);
                     mc.options.keyUp.setDown(true);
                     mc.options.keySprint.setDown(mc.player.getFoodData().getFoodLevel() > 6);
                  } else {
                     this.load_V_6();
                  }
               }
            }

            boolean init_Z_Entity(Entity from) {
               double dx = mc.player.getX() - from.getX();
               double dz = mc.player.getZ() - from.getZ();
               double len = Math.sqrt(dx * dx + dz * dz);
               if (len < 0.001) {
                  return false;
               }

               dx /= len;
               dz /= len;
               int y = mc.player.getBlockY();

               for (double d = 1.0; d <= 2.0; d++) {
                  BlockPos cell = BlockPos.containing(mc.player.getX() + dx * d, y, mc.player.getZ() + dz * d);
                  if (!cu.init(mc.level, cell)) {
                     return false;
                  }

                  if (!mc.level.getBlockState(cell).getCollisionShape(mc.level, cell).isEmpty()
                     || !mc.level.getBlockState(cell.above()).getCollisionShape(mc.level, cell.above()).isEmpty()) {
                     return false;
                  }

                  if (!mc.level.getFluidState(cell).isEmpty()) {
                     return false;
                  }

                  if (mc.level.getBlockState(cell.below()).getCollisionShape(mc.level, cell.below()).isEmpty()) {
                     return false;
                  }
               }

               return true;
            }

            boolean write_Z_2() {
               BlockPos over = mc.player.blockPosition().above(2);
               return mc.level.getBlockState(over).getCollisionShape(mc.level, over).isEmpty();
            }

            void read_V_4() {
               boolean nearlyReady = mc.player.getAttackStrengthScale(0.0F) > 0.6F;
               double reach = ValueSink.init_D_D_Z(k.init_D(mc), false);
               double forward = (mc.options.keyUp.isDown() ? 1 : 0) - (mc.options.keyDown.isDown() ? 1 : 0);
               double strafe = (mc.options.keyLeft.isDown() ? 1 : 0) - (mc.options.keyRight.isDown() ? 1 : 0);
               boolean attempt = nearlyReady
                  && mc.player.onGround()
                  && !this.open_Z
                  && !ValueSink.init_Z(mc)
                  && !mc.player.isInWater()
                  && !mc.player.onClimbable()
                  && !mc.player.isPassenger()
                  && !mc.player.hasEffect(MobEffects.BLINDNESS)
                  && !this.update_Z
                  && !this.sync_Z()
                  && this.write_Z_2()
                  && ValueSink.init_Z_AABB(this.init_Entity.getBoundingBox(), mc.player.getEyePosition(), reach);
               boolean safe = attempt && autismclient.ai.a.AiMotionSafety.putAt_Z_D(forward, strafe, true, mc.player.isSprinting());
               if (attempt && !safe) {
                  this.pop_I++;
               }

               mc.options.keyJump.setDown(safe);
            }

            void write_V_3() {
               int slot = af.init(36);
               if (slot >= 0 && mc.player.getInventory().getSelectedSlot() != slot) {
                  af.load(slot);
               }
            }

            void update_V_2() {
               boolean hasShield = k.init_Z(mc);
               boolean wantShield = hasShield && (ValueSink.init_Entity(mc) != null || mc.player.getAttackStrengthScale(0.0F) < 0.92F && this.sync_Z());
               if (wantShield != this.update_Z) {
                  this.update_Z = wantShield;
                  if (wantShield) {
                     mc.options.keyUse.setDown(true);
                  } else {
                     mc.options.keyUse.setDown(false);
                  }
               }
            }

            void sync_V() {
               if (this.update_Z) {
                  this.update_Z = false;
                  mc.options.keyUse.setDown(false);
               }
            }

            boolean update_Z_2() {
               return !this.sync_Z() && cq.init_Entity(mc) == null;
            }

            AiOperation init_AiOperation_2() {
               return new AiOperation() {
                  final int init_I = af.init_I(healTarget);
                  int load;
                  boolean init_Z;

                  @Override
                  public AiOperation.Result tick() {
                     if (!this.init_Z && mc.player != null) {
                        if (af.init_I(healTarget) < this.init_I) {
                           return this.init(AiOperation.Status.SUCCEEDED);
                        }

                        if (++this.load > 60) {
                           return this.init(AiOperation.Status.FAILED);
                        }

                        if (af.read(healTarget) < 0) {
                           return this.init(AiOperation.Status.FAILED);
                        }

                        mc.options.keyUse.setDown(true);
                        return new AiOperation.Result(AiOperation.Status.RUNNING, "Using " + healWith, "{}", List.of());
                     } else {
                        return this.init(AiOperation.Status.CANCELLED);
                     }
                  }

                  AiOperation.Result init(AiOperation.Status status) {
                     if (mc.options != null) {
                        mc.options.keyUse.setDown(false);
                     }

                     return new AiOperation.Result(status, status.name(), "{}", List.of());
                  }

                  @Override
                  public void cancel() {
                     this.init_Z = true;
                     if (mc.options != null) {
                        mc.options.keyUse.setDown(false);
                     }
                  }
               };
            }

            boolean sync_Z() {
               for (by.Inner_StreamUtil shot : by.init(mc)) {
                  if (by.init(shot) && k.init(mc, mc.level.getEntity(shot.entityId())) && k.init(mc, ValueSink.init(shot))) {
                     return true;
                  }
               }

               return false;
            }

            Entity init_Entity_3() {
               if (explicitId >= 0) {
                  Entity explicit = mc.level.getEntity(explicitId);
                  if (explicit != null && this.init_UUID != null && this.init_UUID.equals(explicit.getUUID())) {
                     Long until = this.init_Map.get(this.init_UUID);
                     return explicit != null && explicit.isAlive() && (until == null || until <= System.nanoTime()) ? explicit : null;
                  } else {
                     return null;
                  }
               } else {
                  List<Entity> found = new ArrayList<>();
                  List<ValueSink.Inner_StreamUtil> candidates = new ArrayList<>();
                  double eyeReach = k.init_D(mc) * 0.8;
                  long now = System.nanoTime();
                  this.init_Map.values().removeIf(until -> until < now);

                  for (Entity entity : mc.level.entitiesForRendering()) {
                     if (entity != mc.player && entity.isAlive() && entity instanceof LivingEntity living && this.read(entity)) {
                        if (!cv.init_Z() && ValueSink.load(entity) && entity instanceof Mob swimmer && cv.init(swimmer, mc.player.getBoundingBox()) > 1.0) {
                           ValueSink.init_V_Entity_J(entity, now + 15000000000L);
                        } else if (ValueSink.init_Z_Minecraft_Entity(mc, entity)) {
                           double distanceSq = entity.distanceToSqr(mc.player);
                           if (!(distanceSq > radius * radius)) {
                              if (!cv.init_Z() && !entity.onGround() && entity.getBoundingBox().minY - mc.player.getEyeY() > eyeReach) {
                                 distanceSq += 400.0;
                              }

                              found.add(entity);
                              candidates.add(
                                 new ValueSink.Inner_StreamUtil(distanceSq, living.getHealth(), living.getMaxHealth(), cv.init_Z(mc, entity), cv.init_Z(entity))
                              );
                           }
                        }
                     }
                  }

                  if (candidates.isEmpty()) {
                     return null;
                  }

                  Comparator<ValueSink.Inner_StreamUtil> order = ValueSink.init(priority);
                  int best = 0;
                  double range = ValueSink.init_D_D_Z(k.init_D(mc), true);

                  for (int i = 1; i < candidates.size(); i++) {
                     boolean in = found.get(i).getBoundingBox().distanceToSqr(mc.player.getEyePosition()) <= range * range;
                     boolean bestIn = found.get(best).getBoundingBox().distanceToSqr(mc.player.getEyePosition()) <= range * range;
                     boolean preferred = (this.open_Z || hold) && in != bestIn ? in : order.compare(candidates.get(i), candidates.get(best)) < 0;
                     if (preferred
                        || order.compare(candidates.get(i), candidates.get(best)) == 0 && in == bestIn && found.get(i).getId() < found.get(best).getId()) {
                        best = i;
                     }
                  }

                  return found.get(best);
               }
            }

            Entity load_Entity_2() {
               Entity best = null;
               double bestGap = 0.5;

               for (Entity entity : mc.level.entitiesForRendering()) {
                  if (entity instanceof Mob mob && mob.isAlive() && entity instanceof Enemy && !(mob.distanceToSqr(mc.player) > 36.0)) {
                     cv.Inner_DataUtil profile = cv.init_cv_DataUtil(entity);
                     if ((cv.load(profile) || profile == cv.Inner_DataUtil.close) && cv.init_Z(mc, entity) && mc.player.hasLineOfSight(entity)) {
                        double gap = cv.init(mob, mc.player.getBoundingBox());
                        if (gap < bestGap) {
                           bestGap = gap;
                           best = entity;
                        }
                     }
                  }
               }

               return best;
            }

            void init(Entity next, String why) {
               if (next != this.init_Entity) {
                  Entity previous = this.init_Entity;
                  this.init_Entity = next;
                  this.init_Z = explicitId >= 0 && next.getId() != explicitId || !this.read(next);
                  if (!this.init_Z && next instanceof LivingEntity requested) {
                     this.init_DataPipe.init(requested);
                  }

                  this.flush_I = this.sync_I;
                  this.close_I++;
                  this.init_Str = why;
                  this.flush();
                  this.read_I = this.open_I = 0;
                  this.next = 0;
                  this.init_Vec3 = null;
                  bL.init_V_Str_JsonObject(
                     "combat_target_switch",
                     ag.init_JsonObject_ObjArr(
                        "from", previous == null ? -1 : previous.getId(), "to", next.getId(), "reason", why, "defensiveDetour", this.init_Z, "heldTicks", 12
                     )
                  );
               }
            }

            boolean load_Z(Entity entity) {
               return entity instanceof Mob mob && mob.isAlive() && cv.init(mob, mc.player.getBoundingBox()) < 0.5;
            }

            boolean read(Entity entity) {
               if (!(!sheepColors.isEmpty() && entity instanceof Sheep sheep)
                  || !sheep.isSheared() && sheepColors.contains(sheep.getColor().getSerializedName())) {
                  return !types.isEmpty()
                     ? types.contains(entity.getType())
                     : entity instanceof Enemy
                        && (
                           cv.init_Z()
                              || cv.init_cv_DataUtil(entity) != cv.Inner_DataUtil.stop && (!cv.init(cv.init_cv_DataUtil(entity)) || cv.init_Z(mc, entity))
                        );
               } else {
                  return false;
               }
            }

            void flush() {
               if (this.init_AiOperation != null) {
                  this.write_Z = this.write_Z | this.init_AiOperation.requiresReconciliationAfterEmergency();
                  this.init_AiOperation.cancel();
                  this.init_AiOperation = null;
               }
            }

            void close() {
               if (mc.options != null) {
                  mc.options.keyUp.setDown(false);
                  mc.options.keyDown.setDown(false);
                  mc.options.keyLeft.setDown(false);
                  mc.options.keyRight.setDown(false);
                  mc.options.keyJump.setDown(false);
                  mc.options.keySprint.setDown(false);
                  if (this.update_Z) {
                     mc.options.keyUse.setDown(false);
                     this.update_Z = false;
                  }
               }
            }

            JsonObject init_JsonObject_23() {
               JsonObject out = ag.init_JsonObject_ObjArr(
                  "target",
                  spec,
                  "killed",
                  this.load_I,
                  "requested",
                  kills,
                  "untilClear",
                  untilClear,
                  "partialCompletion",
                  this.load_I > 0 && this.load_I < kills,
                  "swings",
                  this.push_I,
                  "targetSwitches",
                  this.close_I,
                  "selectionReason",
                  this.init_Str,
                  "defensiveDetour",
                  this.init_Z,
                  "lavaJumpsRefused",
                  this.pop_I,
                  "health",
                  mc.player == null ? 0.0F : mc.player.getHealth(),
                  "healthLost",
                  Math.max(0.0F, this.init_F - (mc.player == null ? this.init_F : mc.player.getHealth())),
                  "lowestHealth",
                  this.load_F,
                  "shieldEquipped",
                  mc.player != null && k.init_Z(mc)
               );
               if (this.read_Str != null) {
                  out.addProperty("retreatReason", this.read_Str);
               }

               if (this.load_Str != null) {
                  out.addProperty("airborneReason", this.load_Str);
               }

               out.add("killedTypes", this.init_DataPipe.init_JsonArray());
               out.addProperty("pendingChangesNeedObservation", this.write_Z);
               if (!this.init_JsonObject.isEmpty()) {
                  out.add("pendingChild", this.init_JsonObject);
               }

               if (this.init_Entity != null && mc.player != null) {
                  out.add(
                     "engaging",
                     ag.init_JsonObject_ObjArr(
                        "entityId",
                        this.init_Entity.getId(),
                        "type",
                        BuiltInRegistries.ENTITY_TYPE.getKey(this.init_Entity.getType()).toString(),
                        "distance",
                        Math.round(mc.player.distanceTo(this.init_Entity) * 10.0F) / 10.0,
                        "health",
                        this.init_Entity instanceof LivingEntity living ? living.getHealth() : 0
                     )
                  );
               }

               ItemStack weapon = mc.player == null ? ItemStack.EMPTY : mc.player.getMainHandItem();
               if (!weapon.isEmpty()) {
                  out.add(
                     "weapon",
                     ag.init_JsonObject_ObjArr(
                        "item",
                        BuiltInRegistries.ITEM.getKey(weapon.getItem()).toString(),
                        "durabilityLeft",
                        weapon.isDamageableItem() ? weapon.getMaxDamage() - weapon.getDamageValue() : -1
                     )
                  );
               }

               return out;
            }

            AiOperation.Result load_AiOperation_Result_8() {
               for (Entity entity : mc.level.entitiesForRendering()) {
                  if (entity instanceof Creeper creeper && creeper.isAlive() && creeper.distanceToSqr(mc.player) < 25.0) {
                     if (this.update_AiOperation != null) {
                        this.update_AiOperation.cancel();
                        this.update_AiOperation = null;
                     }

                     return this.init(A.add, "A creeper at the pillar; leaving the perch to the guard");
                  }
               }

               if (this.open_Z) {
                  if (mc.player.onGround() && mc.player.getY() < this.init_BlockPos.getY() + perch - 0.5) {
                     this.close();
                     mc.options.keyShift.setDown(false);
                     if (++this.clear > 2) {
                        return this.init(A.close, "Knocked off the perch three times");
                     }

                     this.open_Z = false;
                     this.run = 0;
                     this.prev = 0;
                     if (this.pop_Z) {
                        this.init_DataPipe_2.init_V();
                        this.pop_Z = false;
                     }

                     return this.load("Knocked off the perch; back up");
                  } else {
                     if (!mc.player.onGround() && !mc.player.isInWater()) {
                        this.close();
                        mc.options.keyShift.setDown(true);
                        return this.load("Landing on the pillar before choosing cover");
                     }

                     if (mc.player.onGround() && this.reset < 40) {
                        double cx = this.init_BlockPos.getX() + 0.5;
                        double cz = this.init_BlockPos.getZ() + 0.5;
                        double dx = cx - mc.player.getX();
                        double dz = cz - mc.player.getZ();
                        if (dx * dx + dz * dz > 0.0144) {
                           this.reset++;
                           int[] keys = autismclient.ai.a.AiMotionSafety.putAt_IArr_F_F(
                              mc.player.getYRot(), autismclient.ai.a.NodeWalker.init_F_D(mc.player.getX(), mc.player.getZ(), cx, cz)
                           );
                           mc.options.keyShift.setDown(true);
                           if (!autismclient.ai.a.AiMotionSafety.putAt_Z_D_D(keys[0], -keys[1])) {
                              autismclient.ai.a.AiMotionSafety.fill_V();
                              this.reset = 40;
                              return this.load("Holding the safe side of the pillar");
                           }

                           mc.options.keyUp.setDown(keys[0] > 0);
                           mc.options.keyDown.setDown(keys[0] < 0);
                           mc.options.keyRight.setDown(keys[1] > 0);
                           mc.options.keyLeft.setDown(keys[1] < 0);
                           mc.options.keyShift.setDown(true);
                           return this.load("Stepping to the middle of the pillar");
                        }

                        this.reset = 40;
                        this.close();
                        mc.options.keyShift.setDown(true);
                     }

                     return this.read_AiOperation_Result_5();
                  }
               } else {
                  if (this.update_AiOperation == null) {
                     this.close();
                     if (!mc.player.onGround() && ++this.run < 10) {
                        return this.load("Landing before the pillar");
                     }

                     int up = this.init_I_11();
                     if (up >= perch) {
                        this.init_BlockPos = mc.player.blockPosition().below(perch);
                        this.open_Z = true;
                        this.reset = 0;
                        return this.load("On the pillar");
                     }

                     this.init_BlockPos = mc.player.blockPosition().below(up);

                     for (int h = 2; h <= perch - up + 1; h++) {
                        BlockPos over = mc.player.blockPosition().above(h);
                        if (!mc.level.getBlockState(over).getCollisionShape(mc.level, over).isEmpty()) {
                           return this.init(A.close, "No head room for a pillar here");
                        }
                     }

                     this.update_AiOperation = HashUtil.init_AiOperation_BlockPos(this.init_BlockPos.above(perch), 0.5, false, true, false, 4);
                  }

                  AiOperation.Result r = this.update_AiOperation.tick();
                  if (!r.terminal()) {
                     return this.load("Pillaring up out of reach");
                  } else {
                     this.update_AiOperation.cancel();
                     this.update_AiOperation = null;
                     AutismClientAddon.LOG.info("[AI] fight: perch {} ({}) at {}", r.status(), r.message(), mc.player.blockPosition().toShortString());
                     if (r.status() == AiOperation.Status.SUCCEEDED && !(mc.player.getY() < this.init_BlockPos.getY() + perch - 0.5)) {
                        this.open_Z = true;
                        this.reset = 0;
                        return this.load("On the pillar");
                     } else {
                        return this.init(A.close, "Could not pillar up out of reach: " + r.message());
                     }
                  }
               }
            }

            int init_I_11() {
               BlockPos feet = mc.player.blockPosition();
               if (mc.level.getBlockState(feet.below()).getCollisionShape(mc.level, feet.below()).isEmpty()) {
                  return 0;
               }

               int height = 2147483647;

               for (Direction side : Plane.HORIZONTAL) {
                  int open = 0;

                  for (int d = 0; d <= 6; d++) {
                     BlockPos cell = feet.relative(side).below(d);
                     if (!mc.level.getBlockState(cell).getCollisionShape(mc.level, cell).isEmpty()) {
                        break;
                     }

                     open++;
                  }

                  height = Math.min(height, Math.max(0, open - 1));
               }

               return height == 2147483647 ? 0 : height;
            }

            AiOperation.Result read_AiOperation_Result_5() {
               if (perch == 0 && this.sync_I % 10 == 0 && Boolean.getBoolean("autism.ai.quick")) {
                  System.out.println("[AI quick] hold support " + X.write_Z() + " at " + mc.player.blockPosition().toShortString());
               }

               if (perch == 0 && !X.write_Z()) {
                  return this.init(A.close, "Holding ground is unsafe here");
               }

               Entity shooter = cg.init_Entity(mc, k.init_D(mc) * 0.92);
               if (shooter == null && this.pop_Z && this.load_Entity != null && this.load_Entity.isAlive() && ++this.start <= 12) {
                  shooter = this.load_Entity;
               } else if (shooter != null) {
                  this.start = 0;
               }

               if (shooter != null) {
                  if (!cg.init_Z(mc, 6.0)) {
                     this.close();
                     return this.init(
                        AiOperation.Status.SUCCEEDED,
                        "A " + BuiltInRegistries.ENTITY_TYPE.getKey(shooter.getType()).getPath() + " shoots from out of reach; leaving it to the guard"
                     );
                  }

                  AiOperation.Result shielding = this.init_AiOperation_Result_Entity(shooter);
                  if (shielding != null) {
                     return shielding;
                  }
               } else if (this.pop_Z) {
                  this.init_DataPipe_2.init_V();
                  this.pop_Z = false;
               }

               boolean near = false;

               for (Entity entity : mc.level.entitiesForRendering()) {
                  if (entity != mc.player
                     && entity.isAlive()
                     && this.read(entity)
                     && entity.distanceToSqr(mc.player) <= radius * radius
                     && mc.player.hasLineOfSight(entity)) {
                     near = true;
                     break;
                  }
               }

               this.get = near ? 0 : this.get + 1;
               if (this.get >= 40) {
                  return this.init(
                     AiOperation.Status.SUCCEEDED, this.load_I > 0 ? "Killed " + this.load_I + " holding ground; none left near" : "None left near"
                  );
               }

               if (this.init_Entity != null
                  && !(Math.sqrt(this.init_Entity.getBoundingBox().distanceToSqr(mc.player.getEyePosition())) > 3.5)
                  && mc.player.hasLineOfSight(this.init_Entity)) {
                  this.set = 0;
               } else {
                  this.set++;
               }

               if (this.set > 200) {
                  return this.init(AiOperation.Status.SUCCEEDED, "The mobs keep their distance; going on");
               }

               if (perch == 0 && this.init_Entity != null) {
                  double d = Math.sqrt(this.init_Entity.getBoundingBox().distanceToSqr(mc.player.getEyePosition()));
                  if (this.put == this.init_Entity.getId() && !(d <= 3.5) && !(d < this.init_D - 1.0)) {
                     if (++this.add > 60) {
                        long until = System.nanoTime() + 15000000000L;

                        for (Entity entity : mc.level.entitiesForRendering()) {
                           if (entity != mc.player
                              && entity.isAlive()
                              && this.read(entity)
                              && entity.distanceToSqr(mc.player) <= radius * radius
                              && entity.getBoundingBox().distanceToSqr(mc.player.getEyePosition()) > 12.25) {
                              ValueSink.init_V_Entity_J(entity, until);
                           }
                        }

                        this.close();
                        return this.init(AiOperation.Status.SUCCEEDED, "The mobs come no closer; going on");
                     }
                  } else {
                     this.put = this.init_Entity.getId();
                     this.init_D = d;
                     this.add = 0;
                  }
               }

               return null;
            }

            double init_D_2() {
               return this.init_Entity == null ? 1.7976931348623157E308 : mc.player.distanceTo(this.init_Entity);
            }

            Vec3 init_Vec3_5() {
               List<Vec3> edges = new ArrayList<>();
               edges.add(ValueSink.init_Vec3_BlockPos_Vec3(this.init_BlockPos, this.init_Entity.position()));
               double x = this.init_BlockPos.getX() + 0.5;
               double z = this.init_BlockPos.getZ() + 0.5;

               for (int dx = -1; dx <= 1; dx++) {
                  for (int dz = -1; dz <= 1; dz++) {
                     if (dx != 0 || dz != 0) {
                        edges.add(new Vec3(x + dx * 0.56, mc.player.getY(), z + dz * 0.56));
                     }
                  }
               }

               Vec3 best = null;
               double bestDistance = 1.7976931348623157E308;
               int bestExposure = 2147483647;

               for (Vec3 edge : edges) {
                  double distance = Math.hypot(edge.x - mc.player.getX(), edge.z - mc.player.getZ());
                  if (this.init_Z_Vec3(edge)) {
                     Vec3 eye = mc.player.getEyePosition().add(edge.x - mc.player.getX(), 0.0, edge.z - mc.player.getZ());
                     int exposure = 0;

                     for (Entity entity : mc.level.entitiesForRendering()) {
                        if (entity instanceof Enemy
                           && entity.isAlive()
                           && autismclient.ai.a.f.init(entity)
                           && entity.distanceTo(mc.player) <= 16.5
                           && autismclient.ai.a.f.init_Z_BlockGetter_3(mc.level, entity.getEyePosition(), eye, -9223372036854775808L, -9223372036854775808L)) {
                           exposure++;
                        }
                     }

                     if (exposure < bestExposure || exposure == bestExposure && distance < bestDistance) {
                        best = edge;
                        bestDistance = distance;
                        bestExposure = exposure;
                     }
                  }
               }

               return best;
            }

            boolean init_Z_Vec3(Vec3 edge) {
               double dx = edge.x - mc.player.getX();
               double dz = edge.z - mc.player.getZ();

               for (int i = 1; i <= 4; i++) {
                  if (!mc.level.noCollision(mc.player, mc.player.getBoundingBox().move(dx * i / 4.0, 0.0, dz * i / 4.0))) {
                     return false;
                  }
               }

               Vec3 eye = mc.player.getEyePosition().add(dx, 0.0, dz);
               return autismclient.ai.a.DataUtil.init(this.init_Entity, eye, autismclient.ai.a.DataUtil.init_Vec3_Entity_Vec3(this.init_Entity, eye));
            }

            void init(double x, double z) {
               int[] keys = autismclient.ai.a.AiMotionSafety.putAt_IArr_F_F(
                  mc.player.getYRot(), autismclient.ai.a.NodeWalker.init_F_D(mc.player.getX(), mc.player.getZ(), x, z)
               );
               mc.options.keyShift.setDown(true);
               if (!autismclient.ai.a.AiMotionSafety.putAt_Z_D_D(keys[0], -keys[1])) {
                  autismclient.ai.a.AiMotionSafety.fill_V();
               } else {
                  mc.options.keyUp.setDown(keys[0] > 0);
                  mc.options.keyDown.setDown(keys[0] < 0);
                  mc.options.keyRight.setDown(keys[1] > 0);
                  mc.options.keyLeft.setDown(keys[1] < 0);
                  mc.options.keyShift.setDown(true);
               }
            }

            AiOperation.Result init_AiOperation_Result_Entity(Entity shooter) {
               if (!this.open_Z) {
                  for (Entity entity : mc.level.entitiesForRendering()) {
                     if (entity instanceof Enemy && entity.isAlive() && !autismclient.ai.a.f.init(entity) && mc.player.distanceTo(entity) <= 3.2) {
                        if (this.pop_Z) {
                           this.init_DataPipe_2.init_V();
                           this.pop_Z = false;
                        }

                        return null;
                     }
                  }
               }

               if (this.pop_Z) {
                  if (this.open_Z
                     && this.init_DataPipe_2.init_BlockPos() != null
                     && mc.player.getBoundingBox().intersects(new AABB(this.init_DataPipe_2.init_BlockPos()))
                     && this.stop < 40
                     && mc.player.onGround()) {
                     double x = this.init_BlockPos.getX() + 0.5;
                     double z = this.init_BlockPos.getZ() + 0.5;
                     this.init(x, z);
                     this.stop++;
                     return this.load("Clearing the shield cell");
                  }

                  if (this.open_Z
                     && this.stop < 12
                     && this.init_DataPipe_2.init_BlockPos() != null
                     && !autismclient.ai.a.DataPipe.init_Z_BlockPos_Item(this.init_DataPipe_2.init_BlockPos(), cg.init(mc))) {
                     BlockPos cell = this.init_DataPipe_2.init_BlockPos();
                     if (mc.player.onGround()) {
                        Vec3 edge = ValueSink.init_Vec3_BlockPos_Vec3(this.init_BlockPos, Vec3.atCenterOf(cell));
                        double x = edge.x;
                        double z = edge.z;
                        if (Math.hypot(x - mc.player.getX(), z - mc.player.getZ()) > 0.04) {
                           this.init(x, z);
                           this.stop++;
                           return this.load("Exposing the pillar's support face");
                        }
                     }
                  }

                  this.close();
                  if (this.open_Z) {
                     mc.options.keyShift.setDown(true);
                  }

                  autismclient.ai.a.DataPipe.Inner_DataUtil s = this.init_DataPipe_2.init_DataPipe_DataUtil();
                  if (s == autismclient.ai.a.DataPipe.Inner_DataUtil.init) {
                     return this.load("Shielding the pillar from the " + BuiltInRegistries.ENTITY_TYPE.getKey(shooter.getType()).getPath());
                  }

                  this.pop_Z = false;
                  if (s == autismclient.ai.a.DataPipe.Inner_DataUtil.load) {
                     this.prev++;
                     this.reset = 0;
                     this.init_Vec3 = null;
                     this.next = 0;
                     AutismClientAddon.LOG
                        .info(
                           "[AI] fight: shield block at {} against the {}",
                           this.init_DataPipe_2.init_BlockPos().toShortString(),
                           BuiltInRegistries.ENTITY_TYPE.getKey(shooter.getType()).getPath()
                        );
                  } else if (this.poll_Z) {
                     if (Boolean.getBoolean("autism.navigation.debug")) {
                        AutismClientAddon.LOG
                           .info("[AI] fight: backstop failed at {}: {}", this.init_DataPipe_2.init_BlockPos(), this.init_DataPipe_2.init_Str());
                     }

                     this.push_Z = true;
                  } else {
                     if (Boolean.getBoolean("autism.navigation.debug")) {
                        AutismClientAddon.LOG
                           .info(
                              "[AI] fight: shield failed at {}: {} (body {})",
                              this.init_DataPipe_2.init_BlockPos(),
                              this.init_DataPipe_2.init_Str(),
                              mc.player.position()
                           );
                     }

                     this.prev = 8;
                  }

                  return this.load("Checking the shield");
               } else {
                  if (this.prev >= 8) {
                     return null;
                  }

                  Item block = cg.init(mc);
                  BlockPos behind = this.open_Z && block != null && !this.push_Z ? cg.load(mc, shooter) : null;
                  BlockPos cell = block == null ? null : (behind != null ? behind : cg.init(mc, shooter));
                  this.poll_Z = behind != null;
                  if (cell == null) {
                     if (Boolean.getBoolean("autism.navigation.debug")) {
                        AutismClientAddon.LOG
                           .info("[AI] fight: no shield cell after {} blocks (body {}, shooter {})", this.prev, mc.player.position(), shooter.position());
                     }

                     this.prev = 8;
                     return null;
                  } else {
                     this.init_DataPipe_2.init_V_BlockPos_Item(cell, block);
                     this.load_Entity = shooter;
                     this.start = 0;
                     this.pop_Z = true;
                     this.stop = 0;
                     return this.init_AiOperation_Result_Entity(shooter);
                  }
               }
            }

            AiOperation.Result load(String message) {
               return new AiOperation.Result(AiOperation.Status.RUNNING, message, this.init_JsonObject_23().toString(), List.of());
            }

            void init_V_AiOperation_Result(AiOperation.Result child) {
               if (child.status() == AiOperation.Status.UNCERTAIN) {
                  this.write_Z = true;
                  this.init_JsonObject = ag.init_JsonObject_ObjArr(
                     "status",
                     child.status().name(),
                     "message",
                     child.message(),
                     "failure",
                     child.failure().name(),
                     "data",
                     bK.init_JsonObject(child.dataJson())
                  );
               }
            }

            AiOperation.Result init(AiOperation.Status status, String message) {
               boolean pending = this.requiresReconciliationAfterEmergency();
               JsonObject data = this.init_JsonObject_23();
               this.cancel();
               return Y.init_AiOperation_Result_Z_AiOperation_Result(pending, new AiOperation.Result(status, message, data.toString(), List.of()));
            }

            AiOperation.Result init(A code, String message) {
               boolean pending = this.requiresReconciliationAfterEmergency();
               JsonObject data = this.init_JsonObject_23();
               this.cancel();
               return Y.init_AiOperation_Result_Z_AiOperation_Result(pending, AiOperation.Result.failed(code, message, data.toString()));
            }

            @Override
            public boolean canInterruptNow() {
               return !this.init_DataPipe_2.load_Z()
                  && (this.init_AiOperation == null || this.init_AiOperation.canInterruptNow())
                  && (this.update_AiOperation == null || this.update_AiOperation.canInterruptNow())
                  && (this.load_AiOperation == null || this.load_AiOperation.canInterruptNow())
                  && (this.read_AiOperation == null || this.read_AiOperation.canInterruptNow())
                  && (this.write_AiOperation == null || this.write_AiOperation.canInterruptNow());
            }

            @Override
            public boolean requiresReconciliationAfterEmergency() {
               return this.write_Z
                  || this.init_DataPipe_2.load_Z()
                  || this.init_AiOperation != null && this.init_AiOperation.requiresReconciliationAfterEmergency()
                  || this.update_AiOperation != null && this.update_AiOperation.requiresReconciliationAfterEmergency()
                  || this.load_AiOperation != null && this.load_AiOperation.requiresReconciliationAfterEmergency()
                  || this.read_AiOperation != null && this.read_AiOperation.requiresReconciliationAfterEmergency()
                  || this.write_AiOperation != null && this.write_AiOperation.requiresReconciliationAfterEmergency();
            }

            @Override
            public void cancel() {
               if (!this.read_Z) {
                  this.read_Z = true;
                  this.flush();
                  if (this.update_AiOperation != null) {
                     this.update_AiOperation.cancel();
                     this.update_AiOperation = null;
                  }

                  if (this.write_AiOperation != null) {
                     this.write_AiOperation.cancel();
                     this.write_AiOperation = null;
                  }

                  if (this.load_AiOperation != null) {
                     this.load_AiOperation.cancel();
                     this.load_AiOperation = null;
                  }

                  if (this.read_AiOperation != null) {
                     this.read_AiOperation.cancel();
                     this.read_AiOperation = null;
                  }

                  this.close();
                  if (this.open_Z && mc.options != null) {
                     mc.options.keyShift.setDown(false);
                  }

                  if (this.pop_Z) {
                     this.init_DataPipe_2.init_V();
                     this.pop_Z = false;
                  }

                  this.init_DataUtil.init_V();
                  if (mc.level == this.init_Obj && mc.player == this.init_LocalPlayer && mc.player.getInventory().getSelectedSlot() != this.init_I) {
                     autismclient.util.InventorySlotHelper.init_V(mc, this.init_I);
                  }
               }
            }
         };
      } else {
         throw new IllegalArgumentException("Combat requires a connected living player");
      }
   }

   static Set<EntityType<?>> init(String spec) {
      String clean = spec.toLowerCase(Locale.ROOT).trim();
      if (!clean.isEmpty() && !clean.equals("hostiles") && !clean.equals("hostile") && !clean.equals("monsters")) {
         Set<EntityType<?>> types = new LinkedHashSet<>();

         for (String part : clean.split("\\s*,\\s*")) {
            if (!part.isEmpty()) {
               Identifier id = Identifier.tryParse(part.contains(":") ? part : "minecraft:" + part);
               if (id != null) {
                  BuiltInRegistries.ENTITY_TYPE.getOptional(id).ifPresent(types::add);
               }
            }
         }

         if (!types.isEmpty()) {
            return Set.copyOf(types);
         }

         List<String> near = new ArrayList<>();

         for (Identifier key : BuiltInRegistries.ENTITY_TYPE.keySet()) {
            if (key.getPath().contains(clean)) {
               near.add(key.toString());
            }

            if (near.size() >= 5) {
               break;
            }
         }

         throw new IllegalArgumentException(
            "Unknown entity type '"
               + spec
               + "'"
               + (near.isEmpty() ? "" : " (did you mean " + String.join(", ", near) + "?)")
               + "; use a registry id, a comma-separated list, or \"hostiles\""
         );
      } else {
         return Set.of();
      }
   }

   enum Inner_CachePool {
      init,
      load,
      read,
      write;

      static ValueSink.Inner_CachePool init(String text) {
         try {
            return valueOf(text.trim().toUpperCase(Locale.ROOT));
         } catch (IllegalArgumentException | NullPointerException e) {
            throw new IllegalArgumentException("priority must be nearest, weakest, strongest or attacker");
         }
      }
   }

   enum Inner_DataUtil {
      init,
      load,
      read,
      write;
   }

   record Inner_StreamUtil(double distanceSqr, float health, float maxHealth, boolean attacking, boolean hurtPlayer) {
   }
}

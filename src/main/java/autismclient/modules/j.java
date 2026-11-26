package autismclient.modules;

import autismclient.api.module.BoolSetting;
import autismclient.api.module.ColorSetting;
import autismclient.api.module.DoubleSetting;
import autismclient.api.module.IntSetting;
import autismclient.compat263.Compat263;
import autismclient.mixin.accessor.AutismEntityAccessor;
import autismclient.mixin.accessor.AutismLivingEntityAccessor;
import autismclient.mixin.accessor.AutismLocalPlayerAccessor;
import autismclient.util.AutismConfig;
import autismclient.util.bM;
import autismclient.util.cd;
import autismclient.util.ce;
import autismclient.util.cg;
import autismclient.util.ck;
import autismclient.util.cn;
import autismclient.util.g.PkgCls_a;
import autismclient.util.macro.MacroExecutor;
import it.unimi.dsi.fastutil.longs.Long2ByteOpenHashMap;
import it.unimi.dsi.fastutil.longs.Long2DoubleOpenHashMap;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.function.DoubleUnaryOperator;
import java.util.function.ToDoubleFunction;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.BlockPos.MutableBlockPos;
import net.minecraft.core.Direction.Axis;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundDamageEventPacket;
import net.minecraft.network.protocol.game.ClientboundExplodePacket;
import net.minecraft.network.protocol.game.ServerboundAttackPacket;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket;
import net.minecraft.network.protocol.game.ServerboundUseItemOnPacket;
import net.minecraft.network.protocol.game.ServerboundUseItemPacket;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.FluidTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.InterpolationHandler;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.boss.enderdragon.EndCrystal;
import net.minecraft.world.entity.player.Input;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.projectile.arrow.AbstractArrow;
import net.minecraft.world.entity.projectile.hurtingprojectile.Fireball;
import net.minecraft.world.entity.projectile.hurtingprojectile.windcharge.AbstractWindCharge;
import net.minecraft.world.entity.projectile.throwableitemprojectile.Snowball;
import net.minecraft.world.entity.projectile.throwableitemprojectile.ThrownEgg;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.ClipContext.Block;
import net.minecraft.world.level.ClipContext.Fluid;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.FallingBlock;
import net.minecraft.world.level.block.TntBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.LevelChunkSection;
import net.minecraft.world.level.chunk.status.ChunkStatus;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.HitResult.Type;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public final class j extends Module implements cn.Inner_DataUtil {
   private static final int init_I = 8;
   private static final int load_I = 8;
   private static final int read_I = 20;
   private static final int write_I = 4;
   private static final int update_I = 3;
   private static final int sync_I = 20;
   private static final double init_D = 0.55;
   private static final int flush_I = 20;
   private static final int close_I = 10;
   private static final int open_I = 160;
   private static final double load_D = 4.0;
   private final Map<Integer, j.Inner_CachePool> init_Map = new HashMap<>();
   private final Map<BlockPos, Integer> load_Map = new HashMap<>();
   private Object init_Obj;
   private i.Inner_TaskQueue init_i_TaskQueue;
   private int poll_I = -2147483648;
   private int push_I = -2147483648;
   private int pop_I;
   private int tick;
   private int run = -2147483648;
   private boolean init_Z;
   private boolean load_Z;
   private boolean read_Z;
   private boolean write_Z;
   private boolean update_Z;
   private int get;
   private int set = 1;
   private int put;
   private int add = -2147483648;
   private boolean sync_Z;
   private final i.Inner_NetHelper init_i_NetHelper = new i.Inner_NetHelper(160);
   private Vec3 init_Vec3;
   private Vec3 load_Vec3 = Vec3.ZERO;
   private int clear = -2147483648;
   private int reset = -2147483648;
   private int start = -2147483648;
   private String init_Str = "";
   private int stop = -2147483648;
   private int next = -1;
   private int prev = -1;
   private int valid = -1;
   private int size = -1;
   private int data;
   private j.Inner_DataUtil init_j_DataUtil;
   private BlockPos init_BlockPos;
   private BlockPos load_BlockPos;
   private BlockPos read_BlockPos;
   private BlockPos write_BlockPos;
   private int value = -2147483648;
   private int cache;
   private boolean flush_Z;
   private int state;
   private boolean close_Z;
   private boolean open_Z;
   private boolean poll_Z;
   private int queue = -2147483648;
   private Vec3 read_Vec3;
   private Boolean init_Boolean;
   private boolean push_Z;
   private volatile j.Inner_ConfigStore init_j_ConfigStore;
   private int buffer = -2147483648;
   private int entry = -2147483648;
   private boolean pop_Z;
   private int node = -2147483648;
   private int task = -2147483648;
   private int pool = -2147483648;
   private Vec3 write_Vec3;
   private int index = -2147483648;
   private Vec3 update_Vec3;
   private static final int count = 100;
   private int flags;
   private int level;
   private String load_Str = "Idle";
   private String read_Str = "Idle";
   private BlockHitResult init_BlockHitResult;
   private BlockPos update_BlockPos;
   private int frame = -1;
   private int block = -1;
   private int chunk = -2147483648;
   private BlockPos sync_BlockPos;
   private static final double read_D = 0.05;
   private static final double write_D = 0.6;
   private static final double update_D = 0.15;
   private static final int table = 3;
   private static final int store = 8;
   private final j.Inner_StreamUtil init_j_StreamUtil = new j.Inner_StreamUtil();

   public j() {
      super("clutch", "Clutch", ae.init_ae, "Saves you from knockback falls");
      this.load_V_32();
      this.add(new BoolSetting("melee", "Melee Hits", true).group("Triggers"));
      this.add(new BoolSetting("projectiles", "Projectiles", true).group("Triggers").description("Arrows, snowballs and eggs"));
      this.add(new BoolSetting("explosions", "Explosions", true).group("Triggers").description("Fireballs and wind charges"));
      this.add(new BoolSetting("own-knockback", "Own Falls", true).group("Triggers").description("Catch falls after your own jumps"));
      this.add(
         new BoolSetting("own-void-only", "Void Only", true)
            .group("Triggers")
            .description("Own falls into void only")
            .visibleWhen(() -> this.bool("own-knockback"))
      );
      this.add(
         new IntSetting("own-fall-height", "Fall Height", 10, 3, 40, 1)
            .group("Triggers")
            .description("Own drop that gets caught")
            .visibleWhen(() -> this.bool("own-knockback") && !this.bool("own-void-only"))
      );
      this.add(new IntSetting("safe-fall", "Safe Fall", 3, 1, 10, 1).group("Rescue").description("Harmless drop in blocks"));
      this.add(new DoubleSetting("guard-time", "Guard Time", 0.0, 0.0, 10.0, 0.5).group("Rescue").description("Protect jumps after rescue"));
      this.add(new BoolSetting("steer", "Enemy Movement", true).group("Rescue").description("Brake, jump and sneak after enemy attacks"));
      this.add(new BoolSetting("own-steer", "Own Fall Movement", false).group("Rescue").description("Brake, jump and sneak for your own falls"));
      this.add(new BoolSetting("restore-slot", "Restore Hotbar", true).group("Rescue"));
      this.add(new BoolSetting("place-animation", "Place Animation", true).group("Animation").description("Placement ripple effect"));
      this.add(new BoolSetting("animation-custom", "Custom Color", false).group("Animation").visibleWhen(() -> this.bool("place-animation")));
      this.add(
         new ColorSetting("animation-color", "Color", -50373)
            .group("Animation")
            .visibleWhen(() -> this.bool("place-animation") && this.bool("animation-custom"))
      );
   }

   @Override
   public void onEnable() {
      this.init_i_NetHelper.init();
      this.init_V_49();
   }

   @Override
   protected void onOptionValueChanged(String settingId) {
      this.init_V_49();
   }

   private void init_V_49() {
      cg.init(
         cg.Inner_DataUtil.load,
         this.isEnabled() && this.bool("place-animation"),
         this.bool("animation-custom"),
         am.init_I_Module(this, "animation-color", -50373)
      );
   }

   private void load_V_32() {
      AutismConfig.ModuleState saved = AutismConfig.getGlobal().modules.get(this.id());
      if (saved != null && saved.settings != null) {
         Map<String, String> settings = saved.settings;
         if (!settings.containsKey("projectiles") && (settings.containsKey("arrows") || settings.containsKey("snowballs"))) {
            settings.put("projectiles", Boolean.toString(!"false".equals(settings.get("arrows")) || !"false".equals(settings.get("snowballs"))));
         }

         if (!settings.containsKey("explosions") && settings.containsKey("fireballs")) {
            settings.put("explosions", settings.get("fireballs"));
         }
      }
   }

   public static j init_j() {
      return al.init_Module_Str("clutch") instanceof j clutch ? clutch : null;
   }

   public static boolean init_Z_49() {
      j clutch = init_j();
      return clutch != null && clutch.init_Z && !clutch.update_Z && !aD.init_Z() && !aC.init_Z();
   }

   public static boolean read_Z_20() {
      j clutch = init_j();
      return clutch != null && clutch.init_Z && !clutch.load_Z && clutch.push_Z_4() && clutch.state != 3 && !aD.init_Z() && !aC.init_Z();
   }

   public static boolean sync_Z_10() {
      j clutch = init_j();
      return clutch != null && clutch.init_Z && !clutch.load_Z && clutch.push_Z_4() && clutch.state != 3 && !i.init_Z_I(clutch.read_I_9(), clutch.entry, 4);
   }

   public static boolean flush_Z_9() {
      j clutch = init_j();
      return clutch != null
         && clutch.isEnabled()
         && clutch.set()
         && (clutch.init_Z && !clutch.load_Z ? clutch.push_Z_4() && clutch.state != 3 : clutch.put > 0 && clutch.pop_Z_3());
   }

   public static boolean close_Z_8() {
      j clutch = init_j();
      return clutch != null && clutch.init_Z && "clutch".equals(autismclient.util.aD.init_Str()) && !aD.init_Z() && !aC.init_Z();
   }

   public static boolean open_Z_5() {
      j clutch = init_j();
      return init_Z_49() && (clutch.read_Z || clutch.load_Z || clutch.write_Z);
   }

   @Override
   public boolean load_Z_23() {
      return (close_Z_8() || this.tick_Z_6()) && !aD.init_Z() && !aC.init_Z();
   }

   @Override
   public boolean hasDisabledTickWork() {
      return this.init_Z || this.tick_Z_6() || this.valid >= 0;
   }

   @Override
   public void tick() {
      if (!this.isEnabled() && MC.player != null) {
         if (this.init_Z) {
            this.push_V_4();
         }

         this.pop_V_3();
         this.run_V_3();
      }
   }

   public String init_Str_16() {
      return this.load_Str;
   }

   public boolean poll_Z_5() {
      return this.init_i_NetHelper.init_Z(this.read_I_9());
   }

   public int init_I_29() {
      return this.flags;
   }

   public int load_I_15() {
      return this.level;
   }

   private int read_I_9() {
      return ck.init_ck().state_I();
   }

   private boolean init_Z_i_TaskQueue(i.Inner_TaskQueue value) {
      return i.init_Z_i_TaskQueue(value, this.bool("own-knockback"), this.bool("melee"), this.bool("projectiles"), this.bool("explosions"));
   }

   private boolean push_Z_4() {
      return i.init_Z_i_TaskQueue(this.init_i_TaskQueue, this.bool("steer"), this.bool("own-steer"))
         && (!i.init_Z_i_TaskQueue(this.init_i_TaskQueue) || this.init_i_NetHelper.init_Z(this.read_I_9()));
   }

   private boolean pop_Z_3() {
      return this.init_i_NetHelper.init_Z(this.read_I_9()) && this.bool("own-steer") && (this.bool("own-knockback") || this.get_Z_3() || this.prev());
   }

   public static void init_V_LivingEntity_5(LivingEntity entity) {
      j clutch = init_j();
      if (clutch != null
         && clutch.isEnabled()
         && entity == MC.player
         && MC.level != null
         && !(entity.getDeltaMovement().y <= 0.0)
         && !(((AutismLivingEntityAccessor)entity).autism$getJumpPower() <= 1.0E-5F)) {
         clutch.flush_V_9();
         if (!clutch.run_Z_3() && (!clutch.init_Z || i.init_Z_i_TaskQueue(clutch.init_i_TaskQueue))) {
            clutch.init_i_NetHelper.init_V(clutch.read_I_9());
         }
      }
   }

   private boolean tick_Z_6() {
      return "clutch".equals(autismclient.util.aD.init_Str()) && autismclient.util.aD.init_Z();
   }

   public static void init_V_ClientboundDamageE_3(ClientboundDamageEventPacket packet) {
      j clutch = init_j();
      if (clutch != null && clutch.isEnabled() && MC.player != null && MC.level != null && packet.entityId() == MC.player.getId()) {
         clutch.flush_V_9();
         DamageSource damage = packet.getSource(MC.level);
         Entity direct = damage.getDirectEntity();
         Entity owner = damage.getEntity();
         i.Inner_StreamUtil cause = init_i_StreamUtil_Entity(direct);
         if (cause == i.Inner_StreamUtil.poll && damage.is(DamageTypes.ARROW)) {
            cause = i.Inner_StreamUtil.sync;
         }

         if (cause == i.Inner_StreamUtil.poll && (damage.is(DamageTypes.PLAYER_ATTACK) || damage.is(DamageTypes.MOB_ATTACK))) {
            cause = i.Inner_StreamUtil.init;
         }

         j.Inner_CachePool seen = direct == null ? null : clutch.init_Map.get(direct.getId());
         boolean own = owner == MC.player || owner == null && seen != null && seen.init_Z;
         i.Inner_TaskQueue hit = new i.Inner_TaskQueue(
            cause, own, cause != i.Inner_StreamUtil.poll || owner instanceof LivingEntity || direct instanceof Projectile && owner != null, clutch.read_I_9()
         );
         if (cause != i.Inner_StreamUtil.poll || direct instanceof Projectile) {
            clutch.start = clutch.init_Z_i_TaskQueue(hit) ? -2147483648 : clutch.read_I_9();
            if (!clutch.init_Z_i_TaskQueue(hit)) {
               clutch.write_V_20();
            }
         }

         if (clutch.init_Z_i_TaskQueue(hit)) {
            clutch.init_i_TaskQueue = hit;
            clutch.poll_I = clutch.read_I_9();
            clutch.write_BlockPos = clutch.read_BlockPos;
            clutch.push_I = clutch.read_I_9();
            clutch.update_V_16();
            if (!hit.init_Z()) {
               clutch.entry = -2147483648;
            }
         } else if (hit.init_Z() && cause != i.Inner_StreamUtil.poll) {
            clutch.read_V_21();
         }

         clutch.init_Str = "damage "
            + cause
            + " owner="
            + (owner == null ? "-" : owner.getType().toShortString())
            + " direct="
            + (direct == null ? "-" : direct.getType().toShortString())
            + " eligible="
            + clutch.init_Z_i_TaskQueue(hit);
         clutch.stop = clutch.read_I_9();
         if (!clutch.init_Z_i_TaskQueue(hit) && cause == i.Inner_StreamUtil.poll && owner == null && direct == null) {
            clutch.reset = clutch.read_I_9();
            if (i.init_Z_I(clutch.read_I_9(), clutch.clear, 2)) {
               clutch.init_V_Z_10(true);
            }
         }
      }
   }

   public static void init_V_I_11(int entityId) {
      j clutch = init_j();
      if (clutch != null && clutch.isEnabled() && MC.player != null && entityId == MC.player.getId()) {
         clutch.flush_V_9();
         clutch.init_Vec3 = MC.player.getDeltaMovement();
      }
   }

   public static void load_V_I_3(int entityId) {
      j clutch = init_j();
      if (clutch != null && clutch.isEnabled() && MC.player != null && entityId == MC.player.getId()) {
         clutch.flush_V_9();
         clutch.push_I = clutch.read_I_9();
         Vec3 before = clutch.init_Vec3 == null ? Vec3.ZERO : clutch.init_Vec3;
         clutch.init_Vec3 = null;
         Vec3 push = MC.player.getDeltaMovement().subtract(before);
         boolean impulse = i.init_Z_Vec3(before, MC.player.getDeltaMovement(), i.init_Z_I(clutch.read_I_9(), clutch.reset, 3));
         if (impulse) {
            clutch.clear = clutch.read_I_9();
            clutch.load_Vec3 = MC.player.getDeltaMovement();
         }

         if (impulse || clutch.init_Str.isEmpty()) {
            String line = String.format(Locale.ROOT, "push %.2f/%.2f before=%s", push.horizontalDistance(), push.y, before == Vec3.ZERO ? "none" : "seen");
            clutch.init_Str = clutch.stop == clutch.read_I_9() ? clutch.init_Str + " | " + line : line;
            clutch.stop = clutch.read_I_9();
         }

         if (i.init_Z_I(clutch.read_I_9(), clutch.entry, 3)) {
            clutch.read_V_21();
         } else {
            j.Inner_CachePool nearest = null;
            double score = 1.0 / 0.0;

            for (j.Inner_CachePool sample : clutch.init_Map.values()) {
               if (i.init_Z_I(clutch.read_I_9(), sample.load_I, 4) && !(sample.load_Vec3.lengthSqr() < 0.001)) {
                  double distance = init_D_AABB(MC.player.getBoundingBox().inflate(0.45), sample.init_Vec3, sample.init_Vec3.add(sample.load_Vec3.scale(4.0)));
                  if (Double.isFinite(distance)) {
                     distance = sample.init_Vec3.distanceToSqr(MC.player.getEyePosition()) * 0.001;
                  }

                  if (distance < score) {
                     score = distance;
                     nearest = sample;
                  }
               }
            }

            if (nearest != null && score <= 0.025) {
               i.Inner_TaskQueue hit = new i.Inner_TaskQueue(
                  nearest.init_i_StreamUtil, nearest.init_Z || i.init_Z_I(clutch.read_I_9(), clutch.entry, 3), nearest.load_Z || impulse, clutch.read_I_9()
               );
               boolean enemyDamage = clutch.init_i_TaskQueue != null && !clutch.init_i_TaskQueue.init_Z() && i.init_Z_I(clutch.read_I_9(), clutch.poll_I, 2);
               if (nearest.init_Z && !enemyDamage || clutch.init_i_TaskQueue == null || !i.init_Z_I(clutch.read_I_9(), clutch.init_i_TaskQueue.init_I(), 4)) {
                  clutch.init_Str = clutch.init_Str + " projectile " + nearest.init_i_StreamUtil + (nearest.init_Z ? " own" : "");
                  clutch.init_i_TaskQueue = clutch.init_Z_i_TaskQueue(hit) ? hit : null;
               }

               if (clutch.init_Z_i_TaskQueue(hit)) {
                  clutch.write_BlockPos = clutch.read_BlockPos;
               } else if (nearest.init_Z && !enemyDamage) {
                  clutch.read_V_21();
               }
            }

            boolean excluded = i.init_Z_I(clutch.read_I_9(), clutch.start, 3)
               || nearest != null
                  && !clutch.init_Z_i_TaskQueue(new i.Inner_TaskQueue(nearest.init_i_StreamUtil, nearest.init_Z, nearest.load_Z || impulse, clutch.read_I_9()));
            if (!impulse
               || excluded
               || clutch.init_Z_i_TaskQueue(clutch.init_i_TaskQueue) && i.init_Z_I(clutch.read_I_9(), clutch.init_i_TaskQueue.init_I(), 2)) {
               if (impulse && excluded) {
                  clutch.write_V_20();
               }
            } else {
               clutch.init_V_Z_10(i.init_Z_I(clutch.read_I_9(), clutch.reset, 3));
            }

            if (clutch.init_Z && clutch.init_Z_i_TaskQueue(clutch.init_i_TaskQueue) && i.init_Z_I(clutch.read_I_9(), clutch.init_i_TaskQueue.init_I(), 2)) {
               clutch.update_V_16();
            }

            if (clutch.init_Z_i_TaskQueue(clutch.init_i_TaskQueue) && i.init_Z_I(clutch.read_I_9(), clutch.init_i_TaskQueue.init_I(), 2)) {
               clutch.sync_V_11();
            }
         }
      }
   }

   public static void init_V_ClientboundExplode(ClientboundExplodePacket packet) {
      j clutch = init_j();
      if (clutch != null && clutch.isEnabled() && MC.player != null) {
         clutch.flush_V_9();
         j.Inner_CachePool nearest = null;
         double best = 16.0;

         for (j.Inner_CachePool sample : clutch.init_Map.values()) {
            if ((sample.init_i_StreamUtil == i.Inner_StreamUtil.read || sample.init_i_StreamUtil == i.Inner_StreamUtil.write)
               && i.init_Z_I(clutch.read_I_9(), sample.load_I, 5)) {
               double distance = sample.init_Vec3.distanceToSqr(packet.center());
               if (distance < best) {
                  nearest = sample;
                  best = distance;
               }
            }
         }

         if (!packet.playerKnockback().isEmpty() && !(packet.playerKnockback().get().lengthSqr() < 0.0025)) {
            boolean ownBlast = clutch.update_Vec3 != null
                  && i.init_Z_I(clutch.read_I_9(), clutch.index, 100)
                  && clutch.update_Vec3.distanceToSqr(packet.center()) <= 64.0
               || clutch.init_Z_Vec3_3(packet.center());
            boolean own = nearest != null
               ? nearest.init_Z || i.init_Z_I(clutch.read_I_9(), clutch.entry, 3)
               : ownBlast || i.init_Z_I(clutch.read_I_9(), clutch.entry, 3);
            i.Inner_TaskQueue hit = new i.Inner_TaskQueue(
               nearest != null ? nearest.init_i_StreamUtil : i.Inner_StreamUtil.update, own, nearest == null || nearest.load_Z, clutch.read_I_9()
            );
            if (nearest == null && !clutch.init_Z_i_TaskQueue(hit)) {
               boolean enemyHit = clutch.init_i_TaskQueue != null
                  && !clutch.init_i_TaskQueue.init_Z()
                  && i.init_Z_I(clutch.read_I_9(), clutch.init_i_TaskQueue.init_I(), 4);
               if (own && !enemyHit) {
                  clutch.read_V_21();
               } else {
                  clutch.write_V_20();
               }
            } else {
               clutch.start = clutch.init_Z_i_TaskQueue(hit) ? -2147483648 : clutch.read_I_9();
               if (!clutch.init_Z_i_TaskQueue(hit)) {
                  clutch.write_V_20();
               }

               clutch.init_i_TaskQueue = clutch.init_Z_i_TaskQueue(hit) ? hit : null;
               clutch.push_I = clutch.read_I_9();
               if (clutch.init_Z_i_TaskQueue(hit)) {
                  clutch.write_BlockPos = clutch.read_BlockPos;
               } else if (own) {
                  clutch.read_V_21();
               }

               if (clutch.init_Z && clutch.init_Z_i_TaskQueue(hit)) {
                  clutch.update_V_16();
               }

               if (clutch.init_Z_i_TaskQueue(hit)) {
                  clutch.sync_V_11();
               }
            }
         } else {
            boolean ours = nearest != null ? nearest.init_Z : clutch.init_Z_Vec3_3(packet.center());
            if (ours && !clutch.init_Z && !clutch.run_Z_3()) {
               clutch.read_V_21();
            }
         }
      }
   }

   private void read_V_21() {
      if (this.init_i_TaskQueue != null) {
         this.init_Str = this.init_Str + " own boost dropped " + this.init_i_TaskQueue.init_i_StreamUtil();
      }

      this.init_i_TaskQueue = null;
      this.poll_I = -2147483648;
      this.entry = this.read_I_9();
      this.pool = -2147483648;
      this.write_V_20();
      if (this.init_Z) {
         autismclient.util.s.write_V();
         this.push_V_4();
      }
   }

   private void write_V_20() {
      this.pop_Z = true;
      this.node = this.read_I_9();
   }

   private boolean init_Z_Vec3_3(Vec3 near) {
      return this.write_Vec3 != null && near != null && i.init_Z_I(this.read_I_9(), this.buffer, 20) && this.write_Vec3.distanceToSqr(near) <= 64.0;
   }

   private boolean run_Z_3() {
      return i.load(this.init_i_TaskQueue) && this.init_Z_i_TaskQueue(this.init_i_TaskQueue) && i.init_Z_I(this.read_I_9(), this.init_i_TaskQueue.init_I(), 4);
   }

   private void init_V_Z_10(boolean hurt) {
      if (this.bool("melee") && MC.level != null && !i.init_Z_I(this.read_I_9(), this.entry, 3) && !i.init_Z_I(this.read_I_9(), this.start, 3)) {
         if (!this.init_Z && this.load_Vec3.y >= 0.55 && this.init_Z_Vec3_3(MC.player.position())) {
            this.init_Str = this.init_Str + " own boost";
            this.read_V_21();
         } else {
            this.init_Str = this.init_Str + " server impulse hurt=" + hurt;
            this.init_i_TaskQueue = new i.Inner_TaskQueue(i.Inner_StreamUtil.load, false, true, this.read_I_9());
            this.poll_I = this.push_I = this.read_I_9();
            this.write_BlockPos = this.read_BlockPos;
            this.update_V_16();
            this.sync_V_11();
         }
      }
   }

   private void update_V_16() {
      if (this.init_Z && i.load(this.init_i_TaskQueue)) {
         if (this.state == 0 || this.state == 3) {
            this.flush_Z = this.stop();
         }

         this.state = i.init_I_I(this.state);
         this.load_Z = false;
         this.get = 0;
         this.tick = 0;
         this.pop_I = this.read_I_9();
      }
   }

   private boolean init_Z_I_9(int tick) {
      return i.load(this.init_i_TaskQueue) && !MC.player.onGround() && i.init_Z_I(tick, this.init_i_TaskQueue.init_I(), this.clear, this.value, 160);
   }

   private boolean get_Z_3() {
      int ticks = (int)Math.round(this.decimal("guard-time") * 20.0);
      return ticks > 0 && this.add != -2147483648 && i.init_Z_I(this.read_I_9(), this.add, ticks);
   }

   private void sync_V_11() {
      float jump = ((AutismLivingEntityAccessor)MC.player).autism$getJumpPower();
      if (MC.level != null
         && MC.player.onGround()
         && MC.player.getDeltaMovement().y < jump
         && !MC.level.noCollision(MC.player, MC.player.getBoundingBox().move(0.0, -0.08, 0.0))) {
         this.pool = this.read_I_9();
      }
   }

   private boolean init_Z_Z_I_2(boolean stocked, int mode) {
      if (stocked && mode != 2 && !this.load_Z && MC.player.onGround() && ((AutismLivingEntityAccessor)MC.player).autism$getNoJumpDelay() <= 1) {
         return i.init_Z_I(this.read_I_9(), this.pool, 2) ? true : this.init_Z && !this.read_Z && i.load(this.init_i_TaskQueue) && !this.next();
      } else {
         return false;
      }
   }

   private static double init_D_AABB(AABB box, Vec3 from, Vec3 to) {
      return !box.contains(from) && !box.clip(from, to).isPresent() ? 1.0 / 0.0 : 0.0;
   }

   private static i.Inner_StreamUtil init_i_StreamUtil_Entity(Entity entity) {
      if (entity instanceof Fireball) {
         return i.Inner_StreamUtil.read;
      } else if (entity instanceof AbstractWindCharge) {
         return i.Inner_StreamUtil.write;
      } else if (entity instanceof AbstractArrow) {
         return i.Inner_StreamUtil.sync;
      } else {
         return !(entity instanceof Snowball) && !(entity instanceof ThrownEgg) ? i.Inner_StreamUtil.poll : i.Inner_StreamUtil.flush;
      }
   }

   private void flush_V_9() {
      if (this.init_Obj != MC.level) {
         this.get_V_3();
         this.init_Obj = MC.level;
      }
   }

   public static void init_V_Vec3(Vec3 before) {
      j clutch = init_j();
      if (clutch != null && MC.player != null && before != null) {
         if (MC.player.position().distanceToSqr(before) > 16.0) {
            if (clutch.init_Z) {
               autismclient.util.s.write_V();
            }

            int slot = clutch.valid;
            int from = clutch.size;
            if (slot < 0 && clutch.init_Z && clutch.bool("restore-slot") && clutch.next >= 0 && clutch.prev >= 0 && clutch.next != clutch.prev) {
               slot = clutch.next;
               from = clutch.prev;
            }

            clutch.get_V_3();
            clutch.init_Obj = MC.level;
            if (slot >= 0) {
               clutch.valid = slot;
               clutch.size = from;
               clutch.data = 20;
            }
         } else {
            clutch.init_j_ConfigStore = null;
            clutch.init_j_DataUtil = null;
         }
      }
   }

   private void close_V_8() {
      int tick = this.read_I_9();
      this.init_Map.values().removeIf(sample -> !i.init_Z_I(tick, sample.load_I, 5));

      for (Entity entity : MC.level
         .getEntities(MC.player, MC.player.getBoundingBox().inflate(10.0), candidate -> init_i_StreamUtil_Entity(candidate) != i.Inner_StreamUtil.poll)) {
         Projectile projectile = (Projectile)entity;
         Entity owner = projectile.getOwner();
         j.Inner_CachePool old = this.init_Map.get(entity.getId());
         boolean own = owner != null ? owner == MC.player : old != null && old.init_Z;
         boolean launched = init_i_StreamUtil_Entity(entity) == i.Inner_StreamUtil.read || init_i_StreamUtil_Entity(entity) == i.Inner_StreamUtil.write;
         if (old == null
            && owner == null
            && launched
            && this.write_Vec3 != null
            && i.init_Z_I(tick, this.buffer, 8)
            && entity.position().distanceToSqr(this.write_Vec3) < 9.0) {
            own = true;
         }

         boolean incoming = entity.position().distanceToSqr(MC.player.getEyePosition()) > 9.0
            && entity.getDeltaMovement().dot(MC.player.getEyePosition().subtract(entity.position())) > 0.05;
         boolean attributed = own || owner instanceof LivingEntity || old != null && old.load_Z || incoming;
         this.init_Map
            .put(
               entity.getId(),
               new j.Inner_CachePool(entity.getId(), init_i_StreamUtil_Entity(entity), own, attributed, entity.position(), entity.getDeltaMovement(), tick)
            );
      }
   }

   @Override
   public boolean onPacketSend(Packet<?> packet) {
      if (packet instanceof ServerboundUseItemOnPacket use
         && use.hand() == InteractionHand.MAIN_HAND
         && use.hitResult() == this.init_BlockHitResult
         && i.init_Z_I(this.read_I_9(), this.chunk, 1)) {
         this.block = use.sequence();
      }

      InteractionHand hand = packet instanceof ServerboundUseItemPacket use
         ? use.hand()
         : (packet instanceof ServerboundUseItemOnPacket use ? use.hand() : null);
      if (MC.player != null && hand != null && (MC.player.getItemInHand(hand).is(Items.FIRE_CHARGE) || MC.player.getItemInHand(hand).is(Items.WIND_CHARGE))) {
         this.buffer = this.read_I_9();
         this.write_Vec3 = MC.player.getEyePosition();
      }

      if (MC.player != null && MC.level != null && packet instanceof ServerboundUseItemOnPacket use) {
         BlockPos clicked = use.hitResult().getBlockPos();
         BlockState block = MC.level.getBlockState(clicked);
         ItemStack held = MC.player.getItemInHand(use.hand());
         if (held.is(Items.TNT)
            || held.is(Items.END_CRYSTAL)
            || held.is(Items.RESPAWN_ANCHOR)
            || held.is(Items.FLINT_AND_STEEL)
            || held.is(Items.FIRE_CHARGE)
            || held.is(ItemTags.BEDS)
            || block.is(Blocks.TNT)
            || block.is(Blocks.RESPAWN_ANCHOR)
            || block.is(BlockTags.BEDS)) {
            this.index = this.read_I_9();
            this.update_Vec3 = Vec3.atCenterOf(clicked);
         }
      }

      if (MC.level != null && packet instanceof ServerboundAttackPacket attack && MC.level.getEntity(attack.entityId()) instanceof EndCrystal crystal) {
         this.index = this.read_I_9();
         this.update_Vec3 = crystal.position();
      }

      return false;
   }

   public static void init_V_InteractionHand_2(InteractionHand hand, BlockHitResult hit, InteractionResult result) {
      j clutch = init_j();
      if (clutch != null
         && hit != null
         && hand == InteractionHand.MAIN_HAND
         && hit == clutch.init_BlockHitResult
         && i.init_Z_I(clutch.read_I_9(), clutch.chunk, 1)) {
         if (result != null && result.consumesAction()) {
            aI.init_V_I_I(clutch.frame, clutch.block);
            if (clutch.update_BlockPos != null && MC.level != null && !MC.level.getBlockState(clutch.update_BlockPos).isAir()) {
               cg.init(cg.Inner_DataUtil.load, clutch.update_BlockPos);
            }
         }

         clutch.init_BlockHitResult = null;
         clutch.update_BlockPos = null;
         clutch.frame = clutch.block = -1;
      }
   }

   private boolean set() {
      return MC.player != null
         && MC.level != null
         && MC.gameMode != null
         && MC.getConnection() != null
         && MC.player.isAlive()
         && !MC.player.isSpectator()
         && !MC.player.isPassenger()
         && !MC.player.getAbilities().flying
         && !MC.player.isFallFlying()
         && !MC.player.isInWater()
         && !MC.player.isInLava()
         && MC.gui.screen() == null
         && MC.gui.overlay() == null
         && !aD.init_Z()
         && !aC.init_Z()
         && !cd.init_Z()
         && !HashUtil.init_Z()
         && !PkgCls_a.init_Z()
         && !MacroExecutor.write_Z()
         && !FieldCache.init_Z_6();
   }

   private static boolean put() {
      Module module = al.init_Module_Str("scaffold");
      return module != null && module.isEnabled();
   }

   @Override
   public void preMovementTick() {
      this.init_j_StreamUtil.init_V();

      try {
         this.open_V_5();
      } finally {
         this.init_j_StreamUtil.load();
      }
   }

   private void open_V_5() {
      if (MC.player != null && MC.level != null) {
         this.flush_V_9();
         this.write_Z = false;
         this.pop_V_3();
         this.run_V_3();
         if (this.pop_Z && (MC.player.onGround() || MC.player.isInWater() || MC.player.isInLava()) && !i.init_Z_I(this.read_I_9(), this.node, 2)) {
            this.pop_Z = false;
         }

         if (!MC.player.isInWater() && !MC.player.isInLava()) {
            this.init_i_NetHelper.init(this.read_I_9(), MC.player.onGround(), MC.player.getDeltaMovement().y);
         } else {
            this.init_i_NetHelper.init();
         }

         if (this.isEnabled() && this.set()) {
            if (this.close_Z && !this.push_Z_4()) {
               this.close_Z = false;
            }

            this.close_V_8();
            int tick = this.read_I_9();
            if (MC.player.onGround()) {
               this.write(tick);
            }

            if (!this.init_Z
               && i.load(this.init_i_TaskQueue)
               && !i.init_Z_I(tick, this.init_i_TaskQueue.init_I(), 2)
               && !i.init_Z_I(tick, this.push_I, 2)
               && this.next()) {
               this.init_i_TaskQueue = null;
               this.poll_I = -2147483648;
               this.push_Z = false;
            }

            if (this.close_Z && this.init_BlockPos != null && !this.load_Z_I_5(tick)) {
               this.poll_V_5();
               this.push_V_4();
            } else {
               if (this.close_Z && !MC.player.onGround()) {
                  this.close_Z = false;
               }

               this.load_Map.entrySet().removeIf(entry -> tick - entry.getValue() > 1);
               boolean freshHit = this.init_Z_i_TaskQueue(this.init_i_TaskQueue)
                  && (i.init_Z_I(tick, this.init_i_TaskQueue.init_I(), 24) && i.init_Z_I(tick, this.push_I, 24) || this.init_Z_I_9(tick));
               boolean walkingEdge = !this.init_Z && !freshHit && this.reset();
               if (this.init_Z || freshHit || walkingEdge || this.read_Z(tick)) {
                  i.Inner_ConfigStore forecast = walkingEdge ? this.load_i_ConfigStore() : this.init_i_ConfigStore();
                  if (!this.init_Z && !freshHit) {
                     if (walkingEdge) {
                        if (!forecast.init_Z() || forecast.init_List().isEmpty()) {
                           return;
                        }

                        AABB last = forecast.init_List().getLast().init_AABB();
                        if (!this.init_Z_D_D_2(Math.max(0.0, MC.player.getY() - last.minY), this.init_D_AABB_2(last))) {
                           return;
                        }
                     } else if (!this.init_Z_I_i_ConfigStore(tick, forecast)) {
                        return;
                     }

                     freshHit = true;
                  }

                  this.read_Z = !forecast.init_Z();
                  j.Inner_BufferHelper acquiredRoute = null;
                  boolean acquireCrouch = false;
                  boolean triedCrouch = false;
                  if (!this.init_Z) {
                     if (!forecast.init_Z()) {
                        return;
                     }

                     int slot = this.sync_I_2();
                     boolean chosenAxis = Math.abs(MC.player.getDeltaMovement().x) > Math.abs(MC.player.getDeltaMovement().z);
                     boolean ownFlight = i.init_Z_i_TaskQueue(this.init_i_TaskQueue);
                     int chosenMode = ownFlight ? 3 : 0;
                     j.Inner_BufferHelper first = ownFlight && forecast.init_Z() ? this.init_j_BufferHelper_i_ConfigStore_I(forecast, slot) : null;
                     if (this.push_Z_4() && !walkingEdge && first == null) {
                        forecast = this.init_i_ConfigStore_Z(chosenAxis);
                        chosenMode = 0;
                     }

                     if (first == null && forecast.init_Z()) {
                        first = this.init_j_BufferHelper_i_ConfigStore_I(forecast, slot);
                     }

                     if (this.push_Z_4() && !walkingEdge && forecast.init_Z() && this.init_Z_j_BufferHelper(first) && chosenMode != 3) {
                        i.Inner_ConfigStore alternateForecast = this.init_i_ConfigStore_Z(!chosenAxis);
                        j.Inner_BufferHelper alternate = alternateForecast.init_Z() ? this.init_j_BufferHelper_i_ConfigStore_I(alternateForecast, slot) : null;
                        if (!alternateForecast.init_Z() || this.init_Z_j_BufferHelper_j_BufferHelper(alternate, first)) {
                           chosenAxis = !chosenAxis;
                           forecast = alternateForecast;
                           first = alternate;
                        }

                        for (int mode = 1; mode <= 2 && forecast.init_Z() && this.init_Z_j_BufferHelper(first); mode++) {
                           i.Inner_ConfigStore candidate = this.init_i_ConfigStore_Z_I(chosenAxis, mode);
                           j.Inner_BufferHelper catchRoute = candidate.init_Z() ? this.init_j_BufferHelper_i_ConfigStore_I(candidate, slot) : null;
                           if (!candidate.init_Z()
                              || this.init_Z_j_BufferHelper_j_BufferHelper(catchRoute, first)
                              || mode == 2 && this.read_BlockPos != null && first == null) {
                              forecast = candidate;
                              first = catchRoute;
                              chosenMode = mode;
                           }
                        }
                     }

                     if (this.push_Z_4() && !walkingEdge && forecast.init_Z() && first != null && chosenMode == 0) {
                        i.Inner_ConfigStore diagonalForecast = this.init_i_ConfigStore_Z_I(chosenAxis, 1);
                        j.Inner_BufferHelper diagonal = diagonalForecast.init_Z() ? this.init_j_BufferHelper_i_ConfigStore_I(diagonalForecast, slot) : null;
                        if (!diagonalForecast.init_Z() || this.load_Z_j_BufferHelper_j_BufferHelper(diagonal, first)) {
                           forecast = diagonalForecast;
                           first = diagonal;
                           chosenMode = 1;
                        }
                     }

                     if (first == null && slot >= 0 && !walkingEdge && forecast.init_Z() && this.clear()) {
                        triedCrouch = true;
                        i.Inner_ConfigStore crouched = this.init_i_ConfigStore_Z_2(chosenAxis, 1, true);
                        j.Inner_BufferHelper catchRoute = this.init_j_BufferHelper_i_ConfigStore_I(crouched, slot);
                        if (catchRoute != null) {
                           forecast = crouched;
                           first = catchRoute;
                           chosenMode = 1;
                           acquireCrouch = true;
                        }
                     }

                     if (first == null && this.push_Z_4() && !walkingEdge && forecast.init_Z() && this.init_BlockPos_7() != null) {
                        i.Inner_ConfigStore rowReturn = this.init_i_ConfigStore_Z_I(chosenAxis, 4);
                        j.Inner_BufferHelper catchRoute = this.init(rowReturn, slot, null, false, Set.of(), true);
                        if (catchRoute != null) {
                           forecast = rowReturn;
                           first = catchRoute;
                           chosenMode = 4;
                        }
                     }

                     if (first == null && !walkingEdge && forecast.init_Z()) {
                        j.Inner_BufferHelper urgent = this.load_j_BufferHelper_i_ConfigStore_I(forecast, slot);
                        if (urgent != null) {
                           first = urgent;
                        }
                     }

                     if (first == null && !walkingEdge && forecast.init_Z()) {
                        first = this.init_j_BufferHelper_i_ConfigStore_2(forecast, chosenAxis, chosenMode, slot);
                     }

                     if (first == null && !walkingEdge && forecast.init_Z()) {
                        first = this.init_j_BufferHelper_i_ConfigStore(forecast, slot, null, true);
                     }

                     if (walkingEdge && first == null) {
                        return;
                     }

                     if (forecast.init_Z() && first == null && !this.push_Z_4()) {
                        this.load_Str = "Out of reach";
                        return;
                     }

                     if (forecast.init_Z() && forecast.init_I() > 8 && (first == null || first.init_k_JsonUtil().init_k_NetHelper().load_I() > 8)) {
                        return;
                     }

                     if (walkingEdge) {
                        this.init_i_TaskQueue = new i.Inner_TaskQueue(i.Inner_StreamUtil.close, true, true, tick);
                        this.write_BlockPos = this.read_BlockPos;
                        this.push_I = tick;
                     }

                     this.init_V_I_Z(tick, first != null);
                     if (acquireCrouch) {
                        this.queue = tick;
                     }

                     acquiredRoute = first;
                     this.flush_Z = chosenAxis;
                     this.state = chosenMode;
                     this.close_Z = walkingEdge;
                     if (first != null && !first.init_k_JsonUtil().init_Z_2() && !first.init_Z()) {
                        this.load_BlockPos = first.init_k_JsonUtil().init_BlockPos();
                        this.cache = this.load_BlockPos.getY();
                     }

                     this.read_Z = !forecast.init_Z();
                  }

                  if (MC.player.onGround() && MC.player.getDeltaMovement().y < 0.6) {
                     this.push_Z = false;
                  }

                  if (freshHit
                     && i.load(this.init_i_TaskQueue)
                     && MC.player.getDeltaMovement().y > 0.15
                     && (MC.player.getDeltaMovement().y >= 0.6 || this.init_Z_I_9(tick))) {
                     this.push_Z = true;
                  }

                  if (this.load_Z) {
                     if (!freshHit || !forecast.init_Z()) {
                        this.push_V_4();
                        return;
                     }

                     this.load_Z = false;
                     this.get = 0;
                     this.tick = 0;
                  }

                  if (this.close_Z ? !forecast.init_Z() : this.next()) {
                     this.tick++;
                  } else {
                     this.tick = 0;
                  }

                  if (this.tick < 1 && tick - this.pop_I <= (MC.player.onGround() ? 80 : 160)) {
                     int slot = this.update_I_3();
                     j.Inner_BufferHelper tower = null;
                     if (this.read_Z) {
                        this.init_j_DataUtil = null;
                        if (!this.load_Z_I_5(tick)) {
                           this.poll_V_5();
                        }

                        this.load_Str = "Landing";
                     } else {
                        j.Inner_BufferHelper route = this.read_Z
                           ? null
                           : (acquiredRoute != null ? acquiredRoute : this.init_j_BufferHelper_i_ConfigStore_I(forecast, slot));
                        if (!this.read_Z && this.push_Z_4() && !this.close_Z && this.init_Z_j_BufferHelper(route)) {
                           i.Inner_ConfigStore alternateForecast = this.init_i_ConfigStore_Z(!this.flush_Z);
                           j.Inner_BufferHelper alternate = alternateForecast.init_Z()
                              ? this.init_j_BufferHelper_i_ConfigStore_I(alternateForecast, slot)
                              : null;
                           if (!alternateForecast.init_Z()) {
                              this.flush_Z = !this.flush_Z;
                              this.state = 0;
                              this.read_Z = true;
                              this.init_j_DataUtil = null;
                              this.load_Str = "Landing";
                              return;
                           }

                           if (this.init_Z_j_BufferHelper_j_BufferHelper(alternate, route)) {
                              this.flush_Z = !this.flush_Z;
                              this.state = 0;
                              forecast = alternateForecast;
                              route = alternate;
                           }

                           for (int mode = 1; mode <= 2 && this.init_Z_j_BufferHelper(route); mode++) {
                              i.Inner_ConfigStore candidate = this.init_i_ConfigStore_Z_I(this.flush_Z, mode);
                              j.Inner_BufferHelper catchRoute = candidate.init_Z() ? this.init_j_BufferHelper_i_ConfigStore_I(candidate, slot) : null;
                              if (!candidate.init_Z()) {
                                 this.state = mode;
                                 this.read_Z = true;
                                 this.init_j_DataUtil = null;
                                 this.poll_V_5();
                                 this.load_Str = "Landing";
                                 return;
                              }

                              if (this.init_Z_j_BufferHelper_j_BufferHelper(catchRoute, route)) {
                                 this.state = mode;
                                 forecast = candidate;
                                 route = catchRoute;
                              }
                           }
                        }

                        if (!this.read_Z && route == null && slot >= 0 && !this.close_Z && !triedCrouch && this.clear()) {
                           i.Inner_ConfigStore crouched = this.init_i_ConfigStore_Z_2(this.flush_Z, 1, true);
                           j.Inner_BufferHelper catchRoute = this.init_j_BufferHelper_i_ConfigStore_I(crouched, slot);
                           if (catchRoute != null) {
                              this.queue = tick;
                              this.state = 1;
                              forecast = crouched;
                              route = catchRoute;
                           }
                        }

                        if (!this.read_Z && route == null && this.push_Z_4() && !this.close_Z && this.init_BlockPos_7() != null) {
                           i.Inner_ConfigStore rowReturn = this.init_i_ConfigStore_Z_I(this.flush_Z, 4);
                           j.Inner_BufferHelper catchRoute = this.init(rowReturn, slot, null, false, Set.of(), true);
                           if (catchRoute != null) {
                              forecast = rowReturn;
                              route = catchRoute;
                              this.state = 4;
                           }
                        }

                        if (!this.read_Z && route == null && !this.close_Z) {
                           route = this.load_j_BufferHelper_i_ConfigStore_I(forecast, slot);
                        }

                        if (!this.read_Z && route == null && !this.close_Z) {
                           route = this.init_j_BufferHelper_i_ConfigStore_2(forecast, this.flush_Z, this.state, slot);
                        }

                        if (!this.read_Z && route == null && !this.close_Z) {
                           route = this.init_j_BufferHelper_i_ConfigStore(forecast, slot, null, true);
                        }

                        if (!this.read_Z && !this.close_Z) {
                           int reserve = route != null && !route.init_k_JsonUtil().init_Z_2() ? route.init_k_JsonUtil().init_List().size() : 0;
                           tower = this.init_j_BufferHelper_i_ConfigStore(forecast, slot, reserve);
                        }

                        if (tower != null && (route == null || route.init_k_JsonUtil().init_k_NetHelper().load_I() >= 3)) {
                           route = tower;
                        }

                        this.write_Z = z.init_Z_52() && (route == null || route.init_Z() || route.init_k_JsonUtil().init_k_NetHelper().load_I() >= 4);
                        if (this.write_Z) {
                           this.init_j_DataUtil = null;
                           this.load_Str = "Danger returning";
                        } else if (route == null) {
                           this.init_j_DataUtil = null;
                           this.load_Str = slot < 0 ? "Braking without blocks" : "No reachable catch";
                           if (!this.load_Z_I_5(tick)) {
                              this.poll_V_5();
                           }

                           if (this.close_Z) {
                              this.push_V_4();
                           } else {
                              if (!this.valid() && !i.init_Z_I(tick, this.task, 3) && !this.load_Z_I_5(tick)) {
                                 this.poll_V_5();
                              }

                              if (!this.update_Z) {
                                 ce.Inner_DataUtil aim = autismclient.util.aD.init_ce_DataUtil();
                                 if (aim == null) {
                                    aim = ce.init(MC.player);
                                 }

                                 autismclient.util.aD.init_V_Str(this.id(), 50, aim);
                              }
                           }
                        } else {
                           this.task = tick;
                           this.read_Vec3 = forecast.load().size() > 1 ? forecast.load().get(1) : null;
                           boolean reacquiringHand = this.update_Z;
                           if (this.update_Z) {
                              this.update_Z = false;
                              this.next = this.write_I_7();
                              this.valid = -1;
                              autismclient.util.s.write_V();
                              autismclient.util.aD.init_V();
                           }

                           this.prev = slot;
                           boolean hand = autismclient.util.ah.init_Z(this.id(), this.prev);
                           if (hand && (MC.player.getInventory().getSelectedSlot() != this.prev || reacquiringHand)) {
                              autismclient.util.as.init_V_I(this.prev);
                           }

                           if (!route.init_k_JsonUtil().init_Z_2() && !route.init_Z()) {
                              this.load_BlockPos = route.init_k_JsonUtil().init_BlockPos();
                              this.cache = this.load_BlockPos.getY();
                           }

                           k.Inner_NetHelper link = route.init_k_JsonUtil().init_k_NetHelper();
                           this.set = this.init_I_k_NetHelper(link);
                           if (hand
                              && this.init_j_DataUtil != null
                              && this.init_j_DataUtil.load_I == this.prev
                              && !this.load_Map.containsKey(this.init_j_DataUtil.init_BlockPos)
                              && (!route.init_Z() || route.init_k_JsonUtil().init_k_NetHelper().init_BlockPos().equals(this.init_j_DataUtil.init_BlockPos))) {
                              for (int i = 0; i < route.init_k_JsonUtil().init_List().size(); i++) {
                                 k.Inner_NetHelper preparedLink = route.init_k_JsonUtil().init_List().get(i);
                                 if (preparedLink.init_BlockPos().equals(this.init_j_DataUtil.init_BlockPos)) {
                                    Direction nextFace = i + 1 < route.init_k_JsonUtil().init_List().size()
                                          && route.init_k_JsonUtil().init_List().get(i + 1).load_BlockPos().equals(this.init_j_DataUtil.init_BlockPos)
                                       ? route.init_k_JsonUtil().init_List().get(i + 1).init_Direction()
                                       : null;
                                    this.init_j_DataUtil = new j.Inner_DataUtil(
                                       this.init_j_DataUtil.init_BlockPos,
                                       this.init_j_DataUtil.init_U_CachePool,
                                       this.init_j_DataUtil.init_I,
                                       this.init_j_DataUtil.load_I,
                                       this.init_j_DataUtil.init_Z,
                                       this.init_j_DataUtil.load_Z,
                                       nextFace,
                                       route.init_k_JsonUtil().read()
                                    );
                                    break;
                                 }
                              }

                              if (this.start()) {
                                 return;
                              }
                           }

                           j.Inner_DataUtil next = this.init_j_DataUtil_j_BufferHelper(route, link, this.prev);
                           if (next == null && !route.init_Z() && !this.close_Z) {
                              j.Inner_BufferHelper alternate = this.init_j_BufferHelper_i_ConfigStore(
                                 forecast, this.prev, null, route.init_k_JsonUtil().load(), Set.of(link.init_BlockPos())
                              );
                              if (alternate != null) {
                                 j.Inner_DataUtil candidate = this.init_j_DataUtil_j_BufferHelper(
                                    alternate, alternate.init_k_JsonUtil().init_k_NetHelper(), this.prev
                                 );
                                 if (candidate != null) {
                                    route = alternate;
                                    link = route.init_k_JsonUtil().init_k_NetHelper();
                                    next = candidate;
                                    this.set = this.init_I_k_NetHelper(link);
                                    if (!route.init_k_JsonUtil().init_Z_2()) {
                                       this.load_BlockPos = route.init_k_JsonUtil().init_BlockPos();
                                       this.cache = this.load_BlockPos.getY();
                                    }
                                 }
                              }
                           }

                           if (next == null) {
                              this.init_j_DataUtil = null;
                              this.load_Str = "Turning to the catch";
                              autismclient.util.aD.init_V_Str(this.id(), 50, route.init_ce_DataUtil_k_NetHelper_I(link, this.load_I_k_NetHelper(link)));
                              this.read_V(this.set);
                           } else {
                              this.init_j_DataUtil = next;
                              this.load_Str = !hand
                                 ? "Hand busy"
                                 : (
                                    this.close_Z
                                       ? "Securing the edge"
                                       : (
                                          route.init_Z()
                                             ? "Building under the lift"
                                             : (route.init_k_JsonUtil().load() ? "Reaching toward the fall" : "Catching knockback")
                                       )
                                 );
                              if (this.init_j_ConfigStore != null
                                 && this.init_j_ConfigStore.init_I < this.read_I_9()
                                 && MC.player.getInventory().getSelectedSlot() == this.init_j_DataUtil.load_I) {
                                 Vec3 previousEye = this.init_j_ConfigStore.init_Vec3.add(0.0, this.init_j_ConfigStore.init_D, 0.0);
                                 double reach = MC.player.blockInteractionRange() - 0.03;
                                 if (hand && link.init_I() == 0) {
                                    j.Inner_DataUtil current = this.load_I_k_NetHelper(link) == 0
                                       ? this.init_j_DataUtil
                                       : this.init_j_DataUtil_j_BufferHelper(
                                          route, new k.Inner_NetHelper(link.init_BlockPos(), link.load_BlockPos(), link.init_Direction(), 0, 0), this.prev
                                       );
                                    if (current != null
                                       && aI.init_Z_Vec3(
                                          previousEye,
                                          this.init_j_ConfigStore.init_ce_DataUtil,
                                          current.init_U_CachePool.init_U_TaskQueue().load_BlockPos(),
                                          reach
                                       )
                                       && aI.init_Z_Vec3(
                                          previousEye,
                                          current.init_U_CachePool.init_U_DataUtil().init_ce_DataUtil(),
                                          current.init_U_CachePool.init_U_TaskQueue().load_BlockPos(),
                                          reach
                                       )) {
                                       this.init_j_DataUtil = current;
                                       this.set = 1;
                                    }
                                 }

                                 if (!this.init_j_DataUtil.read
                                    && aI.init_Z_Vec3(
                                       previousEye,
                                       this.init_j_ConfigStore.init_ce_DataUtil,
                                       this.init_j_DataUtil.init_U_CachePool.init_U_TaskQueue().load_BlockPos(),
                                       reach
                                    )) {
                                    autismclient.util.U.Inner_CachePool carry = this.init_U_CachePool_j_DataUtil(
                                       this.init_j_DataUtil, this.init_j_DataUtil.init_U_CachePool.init_U_DataUtil().init_ce_DataUtil(), previousEye, reach
                                    );
                                    if (carry != null) {
                                       this.init_j_DataUtil = new j.Inner_DataUtil(
                                          this.init_j_DataUtil.init_BlockPos,
                                          carry,
                                          this.init_j_DataUtil.init_I,
                                          this.init_j_DataUtil.load_I,
                                          this.init_j_DataUtil.init_Z,
                                          this.init_j_DataUtil.load_Z,
                                          this.init_j_DataUtil.init_Direction,
                                          this.init_j_DataUtil.read
                                       );
                                    }
                                 }
                              }

                              autismclient.util.aD.init_V_Str(this.id(), 50, this.init_j_DataUtil.init_U_CachePool.init_U_DataUtil().init_ce_DataUtil());
                              this.read_V(this.set);
                              if (hand) {
                                 this.start();
                              }
                           }
                        }
                     }
                  } else {
                     int caughtRow = this.write_BlockPos == null ? this.cache : Math.min(this.cache, this.write_BlockPos.getY());
                     if (this.tick >= 1 && MC.player.getY() >= caughtRow + 1 - this.init_D_13()) {
                        this.flags++;
                        this.add = tick;
                        this.sync_Z = true;
                     }

                     if (this.tick >= 1 && !this.load_Z_I_5(tick)) {
                        this.poll_V_5();
                     }

                     this.init_i_TaskQueue = null;
                     this.poll_I = -2147483648;
                     this.push_V_4();
                  }
               }
            }
         } else if (this.init_Z) {
            this.push_V_4();
         }
      } else {
         this.get_V_3();
      }
   }

   private void read_V(int ticksLeft) {
      if ((this.state == 4 || this.init_j_DataUtil != null && this.init_j_DataUtil.read) && ticksLeft <= 1) {
         autismclient.util.aD.init_V_Str_3(this.id(), MC.player, 180.0F);
      } else {
         autismclient.util.aD.init_V_Str_4(this.id(), MC.player, ticksLeft);
      }
   }

   private void init_V_I_Z(int tick, boolean placement) {
      this.init_Z = true;
      this.update_Z = !placement;
      this.load_Z = false;
      this.get = 0;
      this.pop_I = tick;
      this.tick = 0;
      this.state = 0;
      this.close_Z = false;
      this.push_Z = false;
      this.sync_Z = false;
      this.init_Boolean = null;
      this.flush_Z = Math.abs(MC.player.getDeltaMovement().x) > Math.abs(MC.player.getDeltaMovement().z);
      this.cache = this.write_BlockPos != null ? this.write_BlockPos.getY() : (int)Math.floor(MC.player.getY()) - 1;
      this.init_BlockPos = null;
      this.load_BlockPos = null;
      this.task = tick;
      this.next = placement ? this.write_I_7() : -1;
      if (placement) {
         this.valid = -1;
         autismclient.util.aD.init_V();
         autismclient.util.s.write_V();
         autismclient.util.as.update_V();
         if (MC.player.isUsingItem()) {
            MC.gameMode.releaseUsingItem(MC.player);
         }
      }

      this.load_Str = "Catching knockback";
   }

   private int write_I_7() {
      int selected = MC.player.getInventory().getSelectedSlot();
      return this.valid >= 0 && selected == this.size ? this.valid : selected;
   }

   private boolean load_Z_I_5(int tick) {
      return this.run == tick || this.init_BlockHitResult != null && i.init_Z_I(tick, this.chunk, 1);
   }

   private void write(int tick) {
      AABB body = MC.player.getBoundingBox().expandTowards(MC.player.xo - MC.player.getX(), 0.0, MC.player.zo - MC.player.getZ());
      double best = 1.0 / 0.0;

      for (int x = (int)Math.floor(body.minX); x <= (int)Math.floor(body.maxX); x++) {
         for (int z = (int)Math.floor(body.minZ); z <= (int)Math.floor(body.maxZ); z++) {
            BlockPos pos = new BlockPos(x, (int)Math.floor(body.minY - 0.05), z);
            VoxelShape shape = MC.level.getBlockState(pos).getCollisionShape(MC.level, pos);
            if (!shape.isEmpty()) {
               double distance = Vec3.atCenterOf(pos).distanceToSqr(MC.player.position());
               if (distance < best) {
                  best = distance;
                  this.read_BlockPos = pos;
                  this.value = tick;
               }
            }
         }
      }
   }

   private boolean read_Z(int tick) {
      if (!MC.player.onGround() && this.read_BlockPos != null && this.init_i_NetHelper.init_Z(tick)) {
         boolean underLaunchRow = MC.player.getDeltaMovement().y < 0.0 && MC.player.getY() < this.read_BlockPos.getY();
         if (this.pop_Z && !underLaunchRow) {
            return false;
         } else if (!this.get_Z_3()) {
            return !this.bool("own-knockback")
               ? false
               : i.init_Z_I(tick, this.value, MC.player.getY(), this.read_BlockPos.getY(), MC.player.getDeltaMovement().y, this.pop_Z, put());
         } else {
            return !put() || MC.player.getY() < this.read_BlockPos.getY() + 0.4;
         }
      } else {
         return false;
      }
   }

   private boolean init_Z_I_i_ConfigStore(int tick, i.Inner_ConfigStore forecast) {
      if (!this.read_Z(tick) || !forecast.init_Z() || forecast.init_List().isEmpty()) {
         return false;
      }

      if (this.get_Z_3()) {
         this.init_i_TaskQueue = new i.Inner_TaskQueue(i.Inner_StreamUtil.open, false, true, tick);
         this.write_BlockPos = this.read_BlockPos;
         this.push_I = tick;
         this.poll_I = -2147483648;
         return true;
      }

      AABB last = forecast.init_List().getLast().init_AABB();
      if (!this.init_Z_D_D_2(this.read_BlockPos.getY() + 1 - last.minY, this.init_D_AABB_2(last))) {
         return false;
      }

      this.init_i_TaskQueue = new i.Inner_TaskQueue(i.Inner_StreamUtil.close, true, true, tick);
      this.write_BlockPos = this.read_BlockPos;
      this.push_I = tick;
      this.poll_I = -2147483648;
      return true;
   }

   private boolean init_Z_D_D_2(double fallen, double below) {
      return i.init_Z_D(fallen, below, this.bool("own-void-only"), this.integer("own-fall-height"));
   }

   private boolean load_Z_D_D_2(double fallen, double below) {
      if (Double.isNaN(below)) {
         return false;
      }

      if (Double.isInfinite(below)) {
         return true;
      }

      double health = MC.player.getHealth() + MC.player.getAbsorptionAmount();
      return i.init_D_D(fallen + below) >= health;
   }

   private double init_D_AABB_2(AABB body) {
      int x0 = Mth.floor(body.minX + 0.001);
      int x1 = Mth.floor(body.maxX - 0.001);
      int z0 = Mth.floor(body.minZ + 0.001);
      int z1 = Mth.floor(body.maxZ - 0.001);
      MutableBlockPos pos = new MutableBlockPos();

      for (int y = Mth.floor(body.minY - 0.001); y >= MC.level.getMinY(); y--) {
         for (int x = x0; x <= x1; x++) {
            for (int z = z0; z <= z1; z++) {
               pos.set(x, y, z);
               if (!MC.level.hasChunkAt(pos)) {
                  return 0.0 / 0.0;
               }

               BlockState state = MC.level.getBlockState(pos);
               FluidState fluid = state.getFluidState();
               if (fluid.is(FluidTags.WATER)) {
                  return 0.0 / 0.0;
               }

               if (fluid.is(FluidTags.LAVA)) {
                  return 1.0 / 0.0;
               }

               VoxelShape shape = state.getCollisionShape(MC.level, pos);
               if (!shape.isEmpty()) {
                  return Math.max(0.0, body.minY - (y + shape.max(Axis.Y)));
               }
            }
         }
      }

      return 1.0 / 0.0;
   }

   private i.Inner_BufferHelper init_i_BufferHelper_AABB(AABB body) {
      AABB box = body.deflate(0.001);
      MutableBlockPos pos = new MutableBlockPos();

      for (int x = Mth.floor(box.minX); x <= Mth.floor(box.maxX); x++) {
         for (int y = Mth.floor(box.minY); y <= Mth.floor(box.maxY); y++) {
            for (int z = Mth.floor(box.minZ); z <= Mth.floor(box.maxZ); z++) {
               pos.set(x, y, z);
               BlockState state = MC.level.getBlockState(pos);
               FluidState fluid = state.getFluidState();
               if (fluid.is(FluidTags.LAVA)) {
                  return i.Inner_BufferHelper.read;
               }

               if (fluid.is(FluidTags.WATER) && y + fluid.getHeight(MC.level, pos) > box.minY) {
                  return i.Inner_BufferHelper.load;
               }

               if (state.is(Blocks.COBWEB) || state.is(Blocks.POWDER_SNOW)) {
                  return i.Inner_BufferHelper.load;
               }
            }
         }
      }

      Vec3 centre = body.getCenter();
      pos.set(Mth.floor(centre.x), Mth.floor(body.minY - 0.05), Mth.floor(centre.z));
      return MC.level.getBlockState(pos).is(Blocks.SLIME_BLOCK) ? i.Inner_BufferHelper.load : i.Inner_BufferHelper.init;
   }

   private double init_D_13() {
      double health = MC.player.getHealth() + MC.player.getAbsorptionAmount();
      return Math.max(1.0, Math.min(this.integer("safe-fall"), 2.0 + health));
   }

   private i.Inner_ConfigStore init_i_ConfigStore() {
      if (this.close_Z) {
         return this.load_i_ConfigStore();
      }

      i.Inner_CachePool collision = (body, motion) -> Entity.collideBoundingBox(MC.player, motion, body, MC.level, List.of());
      i.Inner_JsonUtil medium = this::init_i_BufferHelper_AABB;
      if (this.init_Z && !this.load_Z && this.push_Z_4() && this.state != 3) {
         return this.init_i_ConfigStore_Z_I(this.flush_Z, this.state);
      }

      Vec3 acceleration = Vec3.ZERO;
      if (MC.player.input != null) {
         acceleration = this.init_Vec3_7();
         if (acceleration.lengthSqr() > 0.0) {
            acceleration = acceleration.scale(MC.player.isSprinting() ? 0.026 : 0.02);
         }
      }

      return i.init_i_ConfigStore_AABB(
         MC.player.getBoundingBox(), MC.player.getDeltaMovement(), acceleration, this.init_D_13(), collision, medium, MC.player.fallDistance
      );
   }

   private i.Inner_ConfigStore init_i_ConfigStore_Z(boolean axis) {
      return this.init_i_ConfigStore_Z_I(axis, 0);
   }

   private i.Inner_ConfigStore init_i_ConfigStore_Z_I(boolean axis, int mode) {
      return this.init_i_ConfigStore_Z(axis, mode, List.of());
   }

   private i.Inner_ConfigStore init_i_ConfigStore_Z(boolean axis, int mode, List<VoxelShape> extra, int placementTick) {
      if (this.push_Z_4() && !this.load_Z && mode != 3) {
         return this.load(axis, mode, extra, placementTick);
      }

      Vec3 acceleration = Vec3.ZERO;
      if (MC.player.input != null) {
         acceleration = this.init_Vec3_7();
         if (acceleration.lengthSqr() > 0.0) {
            acceleration = acceleration.scale(MC.player.isSprinting() ? 0.026 : 0.02);
         }
      }

      Vec3 input = acceleration;
      return i.init_i_ConfigStore_AABB(
         MC.player.getBoundingBox(),
         MC.player.getDeltaMovement(),
         this.init_D_13(),
         (body, motion) -> Entity.collideBoundingBox(MC.player, motion, body, MC.level, List.of()),
         (body, motion, supported) -> input,
         this::init_i_BufferHelper_AABB,
         (body, motion) -> Entity.collideBoundingBox(MC.player, motion, body, MC.level, extra),
         placementTick,
         MC.player.fallDistance
      );
   }

   private i.Inner_ConfigStore init_i_ConfigStore_Z(boolean axis, int mode, List<VoxelShape> extra) {
      return this.load(axis, mode, extra, 0);
   }

   private i.Inner_ConfigStore load(boolean axis, int mode, List<VoxelShape> extra, int placementTick) {
      if (mode == 3) {
         return this.init_i_ConfigStore_Z(axis, mode, extra, placementTick);
      }

      if (MC.player.onGround()
         || !this.add()
            && (!(Math.abs(MC.player.getBoundingBox().getXsize() - 0.6) < 0.001) || !(Math.abs(MC.player.getBoundingBox().getYsize() - 1.5) < 0.001))) {
         Vec3 initialMotion = MC.player.getDeltaMovement();
         if (this.push_Z_4() && this.init_Z_Z_I_2(this.sync_I_2() >= 0, mode)) {
            float jump = ((AutismLivingEntityAccessor)MC.player).autism$getJumpPower();
            initialMotion = new Vec3(initialMotion.x, Math.max(initialMotion.y, jump), initialMotion.z);
         }

         return i.init_i_ConfigStore_AABB(
            MC.player.getBoundingBox(),
            initialMotion,
            this.init_D_13(),
            (body, motion) -> Entity.collideBoundingBox(MC.player, motion, body, MC.level, List.of()),
            (body, motion, supported) -> this.init_Vec3_AABB(body, motion, axis, mode, supported).scale(supported ? 0.1 : 0.02),
            this::init_i_BufferHelper_AABB,
            (body, motion) -> Entity.collideBoundingBox(MC.player, motion, body, MC.level, extra),
            placementTick,
            MC.player.fallDistance
         );
      } else {
         return this.init_i_ConfigStore_Z(axis, mode, false, extra, placementTick);
      }
   }

   private Vec3 init_Vec3_AABB(AABB body, Vec3 motion, boolean axis, int mode, boolean supported) {
      if (supported || this.load_Z) {
         return i.init_Vec3_Vec3(motion);
      }

      if (mode == 4 && this.sync_BlockPos != null) {
         Vec3 centre = body.getCenter();
         double error = axis ? this.sync_BlockPos.getZ() + 0.5 - centre.z - motion.z * 5.0 : this.sync_BlockPos.getX() + 0.5 - centre.x - motion.x * 5.0;
         return axis ? new Vec3(0.0, 0.0, Math.signum(error)) : new Vec3(Math.signum(error), 0.0, 0.0);
      }

      if (mode == 2 && this.read_BlockPos != null && !MC.level.getBlockState(this.read_BlockPos).getCollisionShape(MC.level, this.read_BlockPos).isEmpty()) {
         Vec3 home = Vec3.atCenterOf(this.read_BlockPos);
         Vec3 centre = body.getCenter();
         Vec3 wanted = new Vec3(home.x - centre.x - motion.x * 5.0, 0.0, home.z - centre.z - motion.z * 5.0);
         if (wanted.horizontalDistanceSqr() > 0.0025) {
            return wanted.normalize();
         }
      }

      return mode == 0 ? i.init_Vec3_Vec3_Z(motion, axis) : i.init_Vec3_Vec3(motion);
   }

   private BlockPos init_BlockPos_7() {
      if (this.read_BlockPos != null && !MC.level.getBlockState(this.read_BlockPos).getCollisionShape(MC.level, this.read_BlockPos).isEmpty()) {
         return this.sync_BlockPos = this.read_BlockPos;
      }

      if (this.sync_BlockPos != null && !MC.level.getBlockState(this.sync_BlockPos).getCollisionShape(MC.level, this.sync_BlockPos).isEmpty()) {
         return this.sync_BlockPos;
      }

      int x = Mth.floor(MC.player.getX());
      int z = Mth.floor(MC.player.getZ());
      int y = Mth.floor(MC.player.getY()) - 1;
      double best = 1.0 / 0.0;
      BlockPos found = null;

      for (int dx = -3; dx <= 3; dx++) {
         for (int dz = -3; dz <= 3; dz++) {
            for (int dy = 0; dy < 3; dy++) {
               BlockPos pos = new BlockPos(x + dx, y - dy, z + dz);
               if (!MC.level.getBlockState(pos).getCollisionShape(MC.level, pos).isEmpty()) {
                  double cost = Vec3.atCenterOf(pos).distanceToSqr(MC.player.position());
                  if (cost < best) {
                     best = cost;
                     found = pos;
                  }
               }
            }
         }
      }

      return this.sync_BlockPos = found;
   }

   private boolean add() {
      return i.init_Z_I(this.read_I_9(), this.queue, 2) && !MC.player.onGround();
   }

   private boolean clear() {
      AABB body = MC.player.getBoundingBox();
      return this.push_Z_4()
         && !MC.player.onGround()
         && !this.add()
         && !MC.options.keyShift.isDown()
         && Math.abs(body.getXsize() - 0.6) < 0.001
         && Math.abs(body.getYsize() - 1.8) < 0.001
         && Math.abs(MC.player.getEyeHeight() - 1.62) < 0.001;
   }

   private i.Inner_ConfigStore init_i_ConfigStore_Z_2(boolean axis, int mode, boolean start) {
      return this.init_i_ConfigStore_Z(axis, mode, start, List.of(), 0);
   }

   private i.Inner_ConfigStore init_i_ConfigStore_Z(boolean axis, int mode, boolean start, List<VoxelShape> extra, int placementTick) {
      return i.init(
         autismclient.ai.a.NodeWalker.Inner_StreamUtil.of(MC.player),
         MC.options.keyShift.isDown() ? 64 : (!start && this.read_I_9() != this.queue ? 0 : 1),
         MC.player.getYRot(),
         this.init_D_13(),
         autismclient.ai.a.NodeWalker.init_NodeWalker_CachePo_ClientLevel_Entity(MC.level, MC.player),
         (body, motion, supported) -> this.init_Vec3_AABB(body, motion, axis, mode, supported),
         this::init_i_BufferHelper_AABB,
         extra.stream().flatMap(shape -> shape.toAabbs().stream()).toList(),
         placementTick,
         MC.player.fallDistance
      );
   }

   private Input init_Input_2() {
      return new Input(
         MC.options.keyUp.isDown(),
         MC.options.keyDown.isDown(),
         MC.options.keyLeft.isDown(),
         MC.options.keyRight.isDown(),
         MC.options.keyJump.isDown(),
         MC.options.keyShift.isDown(),
         MC.options.keySprint.isDown()
      );
   }

   private boolean reset() {
      return this.init_i_NetHelper.init_Z(this.read_I_9())
         && this.bool("own-knockback")
         && this.bool("own-steer")
         && this.put > 0
         && MC.player.onGround()
         && this.read_BlockPos != null
         && !put()
         && !this.pop_Z
         && init_Vec3_Input_F(this.init_Input_2(), MC.player.getYRot()).horizontalDistanceSqr() > 0.0;
   }

   private i.Inner_ConfigStore load_i_ConfigStore() {
      Input input = this.init_Input_2();
      autismclient.ai.a.NodeWalker.Inner_DataUtil pressed = new autismclient.ai.a.NodeWalker.Inner_DataUtil(
         (input.forward() ? 1 : 0) - (input.backward() ? 1 : 0), (input.left() ? 1 : 0) - (input.right() ? 1 : 0), false, false, true, cn.init_F(MC.player)
      );
      return i.init_i_ConfigStore_NodeWalker_StreamU(
         autismclient.ai.a.NodeWalker.Inner_StreamUtil.of(MC.player),
         pressed,
         autismclient.ai.a.NodeWalker.init_NodeWalker_CachePo_ClientLevel_Entity(MC.level, MC.player),
         this.init_D_13(),
         this::init_i_BufferHelper_AABB
      );
   }

   private Vec3 init_Vec3_7() {
      return this.init_Z
         ? init_Vec3_Input_F(this.init_Input_2(), MC.player.getYRot())
         : init_Vec3_Input_F(MC.player.input.keyPresses, cn.init_F(MC.player, MC.player.getYRot()));
   }

   private static Vec3 init_Vec3_Input_F(Input input, float keyYaw) {
      double forward = (input.forward() ? 1 : 0) - (input.backward() ? 1 : 0);
      double side = (input.left() ? 1 : 0) - (input.right() ? 1 : 0);
      double yaw = Math.toRadians(keyYaw);
      Vec3 direction = new Vec3(side * Math.cos(yaw) - forward * Math.sin(yaw), 0.0, forward * Math.cos(yaw) + side * Math.sin(yaw));
      return direction.lengthSqr() > 0.0 ? direction.normalize() : Vec3.ZERO;
   }

   private int update_I_3() {
      int selected = MC.player.getInventory().getSelectedSlot();
      return this.prev >= 0
            && this.prev == selected
            && this.init_Z_ItemStack_9(MC.player.getInventory().getItem(selected))
            && !autismclient.util.ah.init(selected, this.id())
            && aI.load_I_I(selected) > 0
         ? selected
         : this.sync_I_2();
   }

   private int sync_I_2() {
      int selected = MC.player.getInventory().getSelectedSlot();
      int best = -1;
      int most = 0;

      for (int i = 0; i < 9; i++) {
         if (this.init_Z_ItemStack_9(MC.player.getInventory().getItem(i)) && !autismclient.util.ah.init(i, this.id())) {
            int available = aI.load_I_I(i);
            if (i == selected && available >= 8) {
               return selected;
            }

            if (available > most) {
               best = i;
               most = available;
            }
         }
      }

      return best;
   }

   private boolean init_Z_ItemStack_9(ItemStack stack) {
      if (!stack.isEmpty() && stack.getItem() instanceof BlockItem block) {
         BlockState state = block.getBlock().defaultBlockState();
         return state.isCollisionShapeFullBlock(MC.level, MC.player.blockPosition())
            && !(block.getBlock() instanceof FallingBlock)
            && !(block.getBlock() instanceof TntBlock);
      } else {
         return false;
      }
   }

   private j.Inner_BufferHelper init_j_BufferHelper_i_ConfigStore_I(i.Inner_ConfigStore forecast, int slot) {
      return this.init_j_BufferHelper_i_ConfigStore(forecast, slot, null, false);
   }

   private j.Inner_BufferHelper init_j_BufferHelper_i_ConfigStore_2(i.Inner_ConfigStore forecast, int slot, BlockPos assumed) {
      return this.init_j_BufferHelper_i_ConfigStore(forecast, slot, assumed, false);
   }

   private j.Inner_BufferHelper init_j_BufferHelper_i_ConfigStore(i.Inner_ConfigStore forecast, int slot, BlockPos assumed, boolean bestEffort) {
      return this.init_j_BufferHelper_i_ConfigStore(forecast, slot, assumed, bestEffort, Set.of());
   }

   private j.Inner_BufferHelper init_j_BufferHelper_i_ConfigStore(
      i.Inner_ConfigStore forecast, int slot, BlockPos assumed, boolean bestEffort, Set<BlockPos> rejectedStarts
   ) {
      j.Inner_BufferHelper prepared = this.init(forecast, slot, assumed, bestEffort, rejectedStarts, this.state == 4);
      return prepared == null && !bestEffort && assumed == null
         ? (this.init_j_DataUtil != null && this.init_j_DataUtil.read ? this.load_j_BufferHelper_i_ConfigStore_I(forecast, slot) : null)
         : prepared;
   }

   private j.Inner_BufferHelper init(
      i.Inner_ConfigStore forecast, int slot, BlockPos assumed, boolean bestEffort, Set<BlockPos> rejectedStarts, boolean marginal
   ) {
      if (!forecast.init_List().isEmpty() && slot >= 0) {
         List<AABB> bodies = new ArrayList<>(forecast.init_List().size() + 1);
         bodies.add(MC.player.getBoundingBox());

         for (i.Inner_UiHelper step : forecast.init_List()) {
            bodies.add(step.init_AABB());
         }

         double eyeHeight = MC.player.getEyeHeight();
         double range = MC.player.blockInteractionRange() - 0.03;
         ce.Inner_DataUtil from = this.init_ce_DataUtil_6();
         ToDoubleFunction<k.Inner_NetHelper> turnCost = link -> {
            Vec3 eye = forecast.load().isEmpty()
               ? j.Inner_BufferHelper.init(bodies, eyeHeight, this.load_I_k_NetHelper(link))
               : forecast.load().get(Mth.clamp(this.load_I_k_NetHelper(link), 0, forecast.load().size() - 1));
            float angle = ce.load(from, j.Inner_BufferHelper.init_ce_DataUtil_Vec3_k_NetHelper(eye, link));
            return Math.max(0.0F, angle / this.init_I_k_NetHelper(link) - 25.0F) * 0.04;
         };
         k.Inner_JsonUtil plan = null;
         boolean own = i.init_Z_i_TaskQueue(this.init_i_TaskQueue);

         for (int pass = own ? 0 : 2; pass < 3 && plan == null; pass++) {
            k.Inner_DataUtil world = this.init_k_DataUtil_BlockPos(assumed, range, rejectedStarts, pass < 2 ? this.flush_I_2() : 2147483647);
            plan = k.init(
               bodies,
               eyeHeight,
               range - 0.05,
               world,
               assumed == null ? 0 : 1,
               MC.player.fallDistance,
               this.load_D_4(),
               this.load_BlockPos,
               this.flush_I_2(),
               turnCost,
               Math.max(0, aI.load_I_I(slot) - (assumed == null ? 0 : 1)),
               this.init_DoubleUnaryOperato(),
               bestEffort ? range : 0.0,
               this.init_D_i_ConfigStore(forecast),
               forecast.load(),
               marginal || pass == 1
            );
         }

         return plan == null ? null : new j.Inner_BufferHelper(plan, bodies, eyeHeight, false, forecast.load());
      } else {
         return null;
      }
   }

   private j.Inner_BufferHelper load_j_BufferHelper_i_ConfigStore_I(i.Inner_ConfigStore forecast, int slot) {
      if (slot >= 0 && !forecast.init_List().isEmpty()) {
         List<AABB> bodies = new ArrayList<>();
         bodies.add(MC.player.getBoundingBox());

         for (i.Inner_UiHelper step : forecast.init_List()) {
            bodies.add(step.init_AABB());
         }

         double reach = MC.player.blockInteractionRange() - 0.03;
         k.Inner_JsonUtil urgent = k.init(
            bodies,
            MC.player.getEyeHeight(),
            reach - 0.05,
            this.init_k_DataUtil_BlockPos_D(null, reach),
            0,
            MC.player.fallDistance,
            this.load_D_4(),
            this.load_BlockPos,
            this.flush_I_2(),
            null,
            aI.load_I_I(slot),
            this.init_DoubleUnaryOperato(),
            this.init_D_i_ConfigStore(forecast),
            forecast.load(),
            true
         );
         return urgent == null ? null : new j.Inner_BufferHelper(urgent, bodies, MC.player.getEyeHeight(), false, forecast.load());
      } else {
         return null;
      }
   }

   private j.Inner_BufferHelper init_j_BufferHelper_i_ConfigStore(i.Inner_ConfigStore forecast, int slot, int reserve) {
      if (forecast.init_Z() && this.push_Z && i.load(this.init_i_TaskQueue) && slot >= 0 && !forecast.init_List().isEmpty() && aI.load_I_I(slot) > reserve) {
         List<AABB> bodies = new ArrayList<>(forecast.init_List().size() + 1);
         bodies.add(MC.player.getBoundingBox());

         for (i.Inner_UiHelper step : forecast.init_List()) {
            bodies.add(step.init_AABB());
         }

         double eyeHeight = MC.player.getEyeHeight();
         double range = MC.player.blockInteractionRange() - 0.03;
         k.Inner_JsonUtil plan = k.load(
            bodies, eyeHeight, range - 0.05, this.init_k_DataUtil_BlockPos(null, range, Set.of(), 2147483647, this.read_Z), 0, forecast.load()
         );
         return plan == null ? null : new j.Inner_BufferHelper(plan, bodies, eyeHeight, true, forecast.load());
      } else {
         return null;
      }
   }

   private j.Inner_BufferHelper init_j_BufferHelper_i_ConfigStore_2(i.Inner_ConfigStore forecast, boolean axis, int mode, int slot) {
      if (!forecast.init_List().isEmpty() && slot >= 0) {
         List<AABB> bodies = new ArrayList<>(forecast.init_List().size() + 1);
         bodies.add(MC.player.getBoundingBox());

         for (i.Inner_UiHelper step : forecast.init_List()) {
            bodies.add(step.init_AABB());
         }

         double eyeHeight = MC.player.getEyeHeight();
         double range = MC.player.blockInteractionRange() - 0.03;

         for (k.Inner_NetHelper link : k.init_List_List(bodies, eyeHeight, range - 0.05, this.init_k_DataUtil_BlockPos_D(null, range), 0, 8, forecast.load())) {
            if (!this.init_i_ConfigStore_Z(axis, mode, List.of(Shapes.create(new AABB(link.init_BlockPos()))), link.init_I()).init_Z()) {
               k.Inner_JsonUtil plan = new k.Inner_JsonUtil(List.of(link), link.init_BlockPos(), link.init_I() + 1, MC.player.fallDistance, false);
               return new j.Inner_BufferHelper(plan, bodies, eyeHeight, false, forecast.load());
            }
         }

         return null;
      } else {
         return null;
      }
   }

   private k.Inner_DataUtil init_k_DataUtil_BlockPos_D(BlockPos assumed, double range) {
      return this.init_k_DataUtil_BlockPos(assumed, range, Set.of());
   }

   private k.Inner_DataUtil init_k_DataUtil_BlockPos(BlockPos assumed, double range, Set<BlockPos> rejectedStarts) {
      return this.init_k_DataUtil_BlockPos(assumed, range, rejectedStarts, 2147483647);
   }

   private k.Inner_DataUtil init_k_DataUtil_BlockPos(BlockPos assumed, double range, Set<BlockPos> rejectedStarts, int maxRow) {
      return this.init_k_DataUtil_BlockPos(assumed, range, rejectedStarts, maxRow, false);
   }

   private k.Inner_DataUtil init_k_DataUtil_BlockPos(
      final BlockPos assumed, double range, final Set<BlockPos> rejectedStarts, final int maxRow, boolean optional
   ) {
      final boolean sneaking = MC.player.isSecondaryUseActive();
      final BlockHitResult writtenRay = this.init_j_StreamUtil.init(range);
      final List<AABB> others = this.init_j_StreamUtil.init(optional);
      return new k.Inner_DataUtil() {
         private boolean update(int x, int y, int z) {
            return assumed != null && assumed.getX() == x && assumed.getY() == y && assumed.getZ() == z;
         }

         @Override
         public boolean init(int x, int y, int z) {
            return rejectedStarts.isEmpty() || !rejectedStarts.contains(new BlockPos(x, y, z));
         }

         @Override
         public boolean load(int x, int y, int z) {
            return this.update(x, y, z) || j.this.init_j_StreamUtil.init(x, y, z, sneaking);
         }

         @Override
         public boolean read(int x, int y, int z) {
            if (y <= maxRow && !this.update(x, y, z) && j.this.init_j_StreamUtil.init(x, y, z)) {
               if (!j.this.load_Map.isEmpty() && j.this.load_Map.containsKey(new BlockPos(x, y, z))) {
                  return false;
               }

               for (AABB other : others) {
                  if (other.minX < x + 1 && other.maxX > x && other.minY < y + 1 && other.maxY > y && other.minZ < z + 1 && other.maxZ > z) {
                     return false;
                  }
               }

               return true;
            } else {
               return false;
            }
         }

         @Override
         public boolean init(Vec3 eye, double distance) {
            return assumed != null && eye.distanceToSqr(Vec3.atCenterOf(assumed)) <= distance * distance
               ? false
               : j.this.init_j_StreamUtil.init_Z_Vec3_D(eye, distance);
         }

         @Override
         public boolean write(int x, int y, int z) {
            BlockPos pos = new BlockPos(x, y, z);
            return writtenRay != null && !pos.equals(assumed) && writtenRay.getBlockPos().equals(pos);
         }

         @Override
         public boolean init(int x, int y, int z, Direction face) {
            BlockPos pos = new BlockPos(x, y, z);
            return !pos.equals(assumed) && i.init_Z_BlockHitResult(writtenRay, pos, face);
         }
      };
   }

   private List<AABB> init_List_Z(boolean optional) {
      int lead = Mth.clamp(Mth.ceil(autismclient.util.macro.ab.init_I() / 50.0F) + 4, 4, 10);
      List<AABB> others = new ArrayList<>();

      for (Entity entity : MC.level
         .getEntities(MC.player, MC.player.getBoundingBox().inflate(10.0), candidate -> candidate.blocksBuilding && candidate.isAlive())) {
         Vec3 step = entity.position().subtract(entity.xo, entity.yo, entity.zo);
         InterpolationHandler interpolation = entity.getInterpolation();
         Vec3 latest = interpolation != null && interpolation.hasActiveInterpolation()
            ? Compat263.interpPos(interpolation).subtract(entity.position())
            : Vec3.ZERO;
         AABB swept = optional ? i.load(entity.getBoundingBox(), latest, step, lead) : i.init_AABB_AABB(entity.getBoundingBox(), latest, step, lead);
         others.add(swept);
      }

      return others;
   }

   private static boolean init_Z_BlockPos_List(BlockPos cell, List<AABB> bodies) {
      AABB box = new AABB(cell);

      for (AABB body : bodies) {
         if (body.intersects(box)) {
            return true;
         }
      }

      return false;
   }

   private double init_D_i_ConfigStore(i.Inner_ConfigStore forecast) {
      List<i.Inner_UiHelper> steps = forecast.init_List();
      if (!steps.isEmpty() && steps.getLast().init_Z()) {
         double highest = MC.player.getBoundingBox().minY;

         for (i.Inner_UiHelper step : steps) {
            highest = Math.max(highest, step.init_AABB().minY);
         }

         return this.init_DoubleUnaryOperato().applyAsDouble(MC.player.fallDistance + Math.max(0.0, highest - steps.getLast().init_AABB().minY));
      } else {
         return 1.0 / 0.0;
      }
   }

   private boolean init_Z_j_BufferHelper(j.Inner_BufferHelper route) {
      return this.init_D_j_BufferHelper(route) >= 4.0;
   }

   private boolean init_Z_j_BufferHelper_j_BufferHelper(j.Inner_BufferHelper candidate, j.Inner_BufferHelper current) {
      return candidate != null && this.init_D_j_BufferHelper(candidate) <= this.init_D_j_BufferHelper(current) - 4.0;
   }

   private boolean load_Z_j_BufferHelper_j_BufferHelper(j.Inner_BufferHelper candidate, j.Inner_BufferHelper current) {
      if (candidate != null
         && !candidate.init_k_JsonUtil().load()
         && !current.init_Z()
         && !(this.init_D_j_BufferHelper(candidate) > this.init_D_j_BufferHelper(current))) {
         int links = candidate.init_k_JsonUtil().init_List().size();
         int previous = current.init_k_JsonUtil().init_List().size();
         return links < previous || links == previous && candidate.init_k_JsonUtil().init_I() < current.init_k_JsonUtil().init_I();
      } else {
         return false;
      }
   }

   private double init_D_j_BufferHelper(j.Inner_BufferHelper route) {
      return route != null && !route.init_k_JsonUtil().load() ? this.init_DoubleUnaryOperato().applyAsDouble(route.init_k_JsonUtil().init_D()) : 1.0 / 0.0;
   }

   private double load_D_4() {
      return MC.player.getOffhandItem().is(Items.TOTEM_OF_UNDYING) ? 1.0 / 0.0 : MC.player.getHealth() + MC.player.getAbsorptionAmount();
   }

   private DoubleUnaryOperator init_DoubleUnaryOperato() {
      return this.init_j_StreamUtil.init_DoubleUnaryOperato();
   }

   private DoubleUnaryOperator load_DoubleUnaryOperato() {
      DamageSource source = MC.player.damageSources().fall();
      Map<Integer, Double> byRaw = new HashMap<>();
      Long2DoubleOpenHashMap byFall = new Long2DoubleOpenHashMap();
      byFall.defaultReturnValue(0.0 / 0.0);
      return fall -> {
         long bits = Double.doubleToLongBits(fall);
         double known = byFall.get(bits);
         if (!Double.isNaN(known)) {
            return known;
         }

         int raw = ((AutismLivingEntityAccessor)MC.player).autism$calculateFallDamage(fall, 1.0F);
         double result = raw <= 0 ? 0.0 : byRaw.computeIfAbsent(raw, amount -> autismclient.util.S.init(MC.player, source, amount.intValue(), true));
         byFall.put(bits, result);
         return result;
      };
   }

   private int flush_I_2() {
      if (this.write_BlockPos != null) {
         return this.write_BlockPos.getY();
      } else {
         return this.read_BlockPos != null && i.init_Z_I(this.read_I_9(), this.value, 40)
            ? this.read_BlockPos.getY()
            : Mth.floor(MC.player.getY() + 1.0E-6) - 1;
      }
   }

   private ce.Inner_DataUtil init_ce_DataUtil_6() {
      ce.Inner_DataUtil look = "clutch".equals(autismclient.util.aD.init_Str()) ? autismclient.util.aD.init_ce_DataUtil() : null;
      if (look == null) {
         look = cn.init_ce_DataUtil(MC.player);
      }

      return look != null ? look : ce.init(MC.player);
   }

   private int init_I_k_NetHelper(k.Inner_NetHelper link) {
      return i.init_I_I(link.init_I(), link.load_I(), i.load(this.init_i_TaskQueue));
   }

   private int load_I_k_NetHelper(k.Inner_NetHelper link) {
      return i.init_I_I_I(link.init_I(), link.load_I());
   }

   private j.Inner_DataUtil init_j_DataUtil_j_BufferHelper(j.Inner_BufferHelper route, k.Inner_NetHelper link, int slot) {
      ItemStack stack = MC.player.getInventory().getItem(slot);
      int at = route.init_k_JsonUtil().read() ? link.init_I() : this.load_I_k_NetHelper(link);
      double reach = MC.player.blockInteractionRange() - 0.03;
      autismclient.util.U.Inner_NetHelper placement = autismclient.util.U.init(stack, MC.player, InteractionHand.MAIN_HAND);
      ce.Inner_DataUtil from = this.init_ce_DataUtil_6();
      autismclient.util.U.Inner_StreamUtil budget = new autismclient.util.U.Inner_StreamUtil(24);
      boolean sneaking = MC.player.isSecondaryUseActive();
      Vec3 eye = this.close_Z ? MC.player.getEyePosition() : route.init(at);
      Vec3 leadEye = !this.close_Z && !route.init_k_JsonUtil().init_Z_2() && !route.init_k_JsonUtil().read() ? route.init(at + 1) : eye;
      autismclient.util.U.Inner_CachePool candidate = autismclient.util.U.init_U_CachePool(
         new autismclient.util.U.Inner_StateCache(link.init_BlockPos(), eye, reach, placement)
            .init_U_StateCache_ce_DataUtil(from)
            .init_U_StateCache_Vec3(leadEye)
            .init_U_StateCache_EnumSet(EnumSet.of(link.init_Direction()))
            .read(false)
            .init_U_StateCache_D(89.5)
            .init_U_StateCache_Z(sneaking)
            .load(false)
            .load(!route.init_k_JsonUtil().load() && route.load().isEmpty() ? 1.0 / 0.0 : 0.02)
            .init_U_StateCache_U_StreamUtil(budget)
      );
      if (candidate == null) {
         candidate = autismclient.util.U.init_U_CachePool(
            new autismclient.util.U.Inner_StateCache(link.init_BlockPos(), eye, reach, placement)
               .init_U_StateCache_ce_DataUtil(from)
               .init_U_StateCache_Vec3(leadEye)
               .init_U_StateCache_D(89.5)
               .init_U_StateCache_Z(sneaking)
               .load(false)
               .load(!route.init_k_JsonUtil().load() && route.load().isEmpty() ? 1.0 / 0.0 : 0.02)
               .init_U_StateCache_U_StreamUtil(budget)
         );
      }

      Direction nextFace = route.init_k_JsonUtil().init_List().size() > 1
            && route.init_k_JsonUtil().init_List().get(1).load_BlockPos().equals(link.init_BlockPos())
         ? route.init_k_JsonUtil().init_List().get(1).init_Direction()
         : null;
      if (candidate == null) {
         return null;
      }

      j.Inner_DataUtil prepared = new j.Inner_DataUtil(
         link.init_BlockPos(), candidate, link.load_I(), slot, route.init_k_JsonUtil().init_Z_2(), route.init_Z(), nextFace, route.init_k_JsonUtil().read()
      );
      if (nextFace != null && !prepared.read) {
         autismclient.util.U.Inner_CachePool carried = this.init(
            prepared, candidate.init_U_DataUtil().init_ce_DataUtil(), eye, route.init(at + 1), null, reach, at
         );
         if (carried != null) {
            prepared = new j.Inner_DataUtil(
               prepared.init_BlockPos, carried, prepared.init_I, prepared.load_I, prepared.init_Z, prepared.load_Z, nextFace, prepared.read
            );
         }
      }

      return prepared;
   }

   private BlockHitResult init_BlockHitResult_Vec3(Vec3 eye, ce.Inner_DataUtil rotation, double reach) {
      HitResult result = MC.level
         .clip(new ClipContext(eye, eye.add(Vec3.directionFromRotation(rotation.load(), rotation.init()).scale(reach)), Block.OUTLINE, Fluid.NONE, MC.player));
      return result instanceof BlockHitResult hit && result.getType() == Type.BLOCK ? hit : null;
   }

   private boolean start() {
      if (this.init_j_DataUtil != null
         && this.run != this.read_I_9()
         && MC.player.getInventory().getSelectedSlot() == this.init_j_DataUtil.load_I
         && !MC.player.isUsingItem()
         && this.init_j_ConfigStore != null
         && this.init_j_ConfigStore.init_I < this.read_I_9()
         && !HashUtil.load_Z()) {
         ce.Inner_DataUtil rotation = autismclient.util.aD.init_ce_DataUtil();
         if (rotation == null) {
            return this.init_Z_Str_3("No rotation");
         }

         ItemStack stack = MC.player.getMainHandItem();
         double reach = MC.player.blockInteractionRange() - 0.03;
         BlockPos support = this.init_j_DataUtil.init_U_CachePool.init_U_TaskQueue().load_BlockPos();
         Vec3 wireEye = this.init_j_ConfigStore.init_Vec3.add(0.0, this.init_j_ConfigStore.init_D, 0.0);
         if (!this.init_j_DataUtil.read
            && !i.init_Z_BlockHitResult(
               this.init_BlockHitResult_Vec3(wireEye, this.init_j_ConfigStore.init_ce_DataUtil, reach),
               support,
               this.init_j_DataUtil.init_U_CachePool.init_U_TaskQueue().init_Direction()
            )) {
            return this.init_Z_Str_3("Preparing written face");
         }

         List<AABB> otherBodies = this.init_List_Z(this.init_j_DataUtil.load_Z && this.read_Z);
         if (init_Z_BlockPos_List(this.init_j_DataUtil.init_BlockPos, otherBodies)) {
            return this.init_Z_Str_3("Moving body enters placement");
         }

         autismclient.util.U.Inner_CachePool carry = this.init_j_DataUtil.read
            ? null
            : this.init_U_CachePool_j_DataUtil(this.init_j_DataUtil, rotation, wireEye, reach);
         if (carry != null) {
            this.init_j_DataUtil = new j.Inner_DataUtil(
               this.init_j_DataUtil.init_BlockPos,
               carry,
               this.init_j_DataUtil.init_I,
               this.init_j_DataUtil.load_I,
               this.init_j_DataUtil.init_Z,
               this.init_j_DataUtil.load_Z,
               this.init_j_DataUtil.init_Direction,
               this.init_j_DataUtil.read
            );
            autismclient.util.aD.init_V_Str(this.id(), 50, carry.init_U_DataUtil().init_ce_DataUtil());
            this.read_V(1);
            rotation = autismclient.util.aD.init_ce_DataUtil();
            if (rotation == null) {
               return this.init_Z_Str_3("No rotation");
            }
         }

         if (!aI.init_Z_Vec3(wireEye, rotation, support, reach)) {
            return this.init_Z_Str_3("Corrected cube look");
         } else {
            Vec3 eye = MC.player.getEyePosition();
            HitResult found = MC.level
               .clip(
                  new ClipContext(eye, eye.add(Vec3.directionFromRotation(rotation.load(), rotation.init()).scale(reach)), Block.OUTLINE, Fluid.NONE, MC.player)
               );
            if (!(
               found instanceof BlockHitResult hit
                  && found.getType() == Type.BLOCK
                  && hit.getBlockPos().equals(support)
                  && !(wireEye.distanceTo(hit.getLocation()) > reach)
                  && !autismclient.util.U.init(MC.level.getBlockState(support), support, MC.player.isSecondaryUseActive())
            )) {
               return this.init_Z_Str_3("Native face or reach");
            } else {
               if (!i.init_Z_BlockHitResult(
                  this.init_BlockHitResult_Vec3(wireEye, this.init_j_DataUtil.read ? rotation : this.init_j_ConfigStore.init_ce_DataUtil, reach),
                  support,
                  hit.getDirection()
               )) {
                  return this.init_Z_Str_3("Preparing written face");
               }

               BlockPlaceContext context = new BlockPlaceContext(MC.player, InteractionHand.MAIN_HAND, stack, hit);
               BlockPos placedCell = context.getClickedPos();
               if (!placedCell.equals(this.init_j_DataUtil.init_BlockPos)) {
                  double feet = Math.min(MC.player.getY(), this.init_j_ConfigStore.init_Vec3.y);
                  if (hit.getDirection() != Direction.UP || placedCell.getY() + 1 > feet - 1.0E-4 || this.load_Map.containsKey(placedCell)) {
                     return this.init_Z_Str_3("Native cell differs");
                  }

                  if (i.init_Z_i_TaskQueue(this.init_i_TaskQueue)
                     && !this.init_j_DataUtil.init_Z
                     && placedCell.getY() > this.init_j_DataUtil.init_BlockPos.getY()) {
                     return this.init_Z_Str_3("Keeping own catch row");
                  }
               }

               if (init_Z_BlockPos_List(placedCell, otherBodies)) {
                  return this.init_Z_Str_3("Moving body enters placement");
               }

               if (!context.canPlace()) {
                  return this.init_Z_Str_3("Native placement unavailable");
               }

               if (new AABB(placedCell).intersects(MC.player.getBoundingBox())
                  || new AABB(placedCell).intersects(MC.player.getBoundingBox().move(this.init_j_ConfigStore.init_Vec3.subtract(MC.player.position())))) {
                  return this.init_Z_Str_3("Placement intersects body");
               }

               if (!MC.level.isUnobstructed(((BlockItem)stack.getItem()).getBlock().defaultBlockState(), placedCell, CollisionContext.empty())) {
                  return this.init_Z_Str_3("Placement obstructed");
               }

               if (!autismclient.util.ah.read_Z(this.id()) || !bM.init(this.id())) {
                  return this.init_Z_Str_3("Placement hand busy");
               }

               if (this.init_j_DataUtil.read
                  ? autismclient.util.s.init(hit, InteractionHand.MAIN_HAND, rotation)
                  : autismclient.util.s.init(hit, InteractionHand.MAIN_HAND)) {
                  this.init_BlockHitResult = hit;
                  this.update_BlockPos = placedCell.immutable();
                  this.frame = this.init_j_DataUtil.load_I;
                  this.block = -1;
                  this.chunk = this.read_I_9();
                  this.read_Str = "Placed";
                  this.run = this.read_I_9();
                  this.level++;
                  this.load_Map.put(placedCell, this.read_I_9());
                  this.init_BlockPos = placedCell;
                  if (hit.getDirection().getAxis() != Axis.Y) {
                     this.init_Boolean = hit.getDirection().getAxis() == Axis.Z;
                  }

                  this.load_Str = "Catch placed";
                  autismclient.util.aD.init_V_Str(this.id(), 50, rotation);
                  return true;
               } else {
                  return this.init_Z_Str_3("Click queue");
               }
            }
         }
      } else {
         return this.init_Z_Str_3("Item or look interval");
      }
   }

   private autismclient.util.U.Inner_CachePool init_U_CachePool_j_DataUtil(
      j.Inner_DataUtil candidatePlan, ce.Inner_DataUtil rotation, Vec3 wireEye, double reach
   ) {
      Vec3 eye = MC.player.getEyePosition();
      Vec3 next = this.read_Vec3 != null ? this.read_Vec3 : eye.add(MC.player.getDeltaMovement());
      return this.init(candidatePlan, rotation, eye, next, wireEye, reach, 0);
   }

   private i.Inner_DataUtil init_i_DataUtil_I_F(int lead, float yaw) {
      return i.init(
         MC.player.getBoundingBox(),
         MC.player.getDeltaMovement(),
         MC.player.getEyeHeight(),
         MC.player.getYRot(),
         yaw,
         lead,
         (body, motion, supported) -> this.init_Vec3_AABB(body, motion, this.flush_Z, 4, supported),
         AutismEntityAccessor::autism$getInputVector
      );
   }

   private autismclient.util.U.Inner_CachePool init(
      j.Inner_DataUtil candidatePlan, ce.Inner_DataUtil rotation, Vec3 eye, Vec3 next, Vec3 wireEye, double reach, int lead
   ) {
      boolean moving = this.state == 4
         && lead <= 1
         && !MC.player.onGround()
         && Math.abs(MC.player.getBoundingBox().getYsize() - 1.8) < 0.001
         && !MC.options.keyShift.isDown()
         && Math.abs(MC.player.getGravity() - 0.08) < 1.0E-9
         && !MC.player.hasEffect(MobEffects.LEVITATION)
         && !MC.player.hasEffect(MobEffects.SLOW_FALLING);
      if (!moving
         && k.init_Z_Vec3_2(
            next, Vec3.directionFromRotation(rotation.load(), rotation.init()), candidatePlan.init_BlockPos, candidatePlan.init_Direction, reach
         )) {
         return null;
      }

      autismclient.util.U.Inner_TaskQueue option = candidatePlan.init_U_CachePool.init_U_TaskQueue();
      AABB rect = option.init_AABB();
      double margin = this.state == 4 ? 1.0E-5 : 0.02;
      autismclient.util.U.Inner_NetHelper placement = autismclient.util.U.init(MC.player.getMainHandItem(), MC.player, InteractionHand.MAIN_HAND);
      autismclient.util.U.Inner_StreamUtil budget = new autismclient.util.U.Inner_StreamUtil(8);
      List<Vec3> points = new ArrayList<>(k.init_List_Vec3(eye, next, rect, candidatePlan.init_BlockPos, candidatePlan.init_Direction, margin));

      for (double a : k.init_DArr) {
         for (double b : k.init_DArr) {
            Axis axis = option.init_Direction().getAxis();
            points.add(
               axis == Axis.X
                  ? new Vec3(rect.minX, rect.minY + (rect.maxY - rect.minY) * a, rect.minZ + (rect.maxZ - rect.minZ) * b)
                  : (
                     axis == Axis.Z
                        ? new Vec3(rect.minX + (rect.maxX - rect.minX) * b, rect.minY + (rect.maxY - rect.minY) * a, rect.minZ)
                        : new Vec3(rect.minX + (rect.maxX - rect.minX) * a, rect.minY, rect.minZ + (rect.maxZ - rect.minZ) * b)
                  )
            );
         }
      }

      List<ce.Inner_DataUtil> goals = new ArrayList<>();

      for (Vec3 point : points) {
         goals.add(ce.init_ce_DataUtil(ce.init(point, eye), rotation));
      }

      if (this.state == 4) {
         List<k.Inner_StreamUtil> counts = moving && candidatePlan.init_Direction != null && candidatePlan.init_Direction.getAxis() != Axis.Y
            ? k.init_List_Vec3(
               eye,
               rect,
               option.load_BlockPos(),
               option.init_Direction(),
               candidatePlan.init_BlockPos,
               candidatePlan.init_Direction,
               reach,
               rotation.init(),
               rotation.load(),
               ce.init(),
               margin,
               yaw -> this.init_i_DataUtil_I_F(lead, (float)yaw)
            )
            : k.init_List_Vec3_2(
               eye,
               next,
               rect,
               option.load_BlockPos(),
               option.init_Direction(),
               candidatePlan.init_BlockPos,
               candidatePlan.init_Direction,
               reach,
               rotation.init(),
               rotation.load(),
               ce.init(),
               margin
            );

         for (int i = counts.size() - 1; i >= 0; i--) {
            goals.addFirst(new ce.Inner_DataUtil(counts.get(i).init(), counts.get(i).load()));
         }
      }

      for (ce.Inner_DataUtil goal : goals) {
         i.Inner_DataUtil window = moving ? this.init_i_DataUtil_I_F(lead, goal.init()) : null;
         Vec3 clickEye = window == null ? eye : window.init_Vec3();
         Vec3 nextEye = window == null ? next : window.load();
         if (window == null || MC.level.noCollision(MC.player, window.init_AABB().deflate(1.0E-5))) {
            Vec3 point = clickEye.add(Vec3.directionFromRotation(goal.load(), goal.init()));
            if (!(Math.abs(goal.load()) > 89.5)
               && (
                  wireEye == null
                     || i.init_Z_BlockHitResult(this.init_BlockHitResult_Vec3(wireEye, goal, reach), option.load_BlockPos(), option.init_Direction())
               )
               && k.init_Z_Vec3_2(
                  nextEye, Vec3.directionFromRotation(goal.load(), goal.init()), candidatePlan.init_BlockPos, candidatePlan.init_Direction, reach
               )) {
               autismclient.util.U.Inner_StateCache request = new autismclient.util.U.Inner_StateCache(candidatePlan.init_BlockPos, clickEye, reach, placement)
                  .init_U_StateCache_ce_DataUtil(rotation)
                  .init_U_StateCache_D(89.5)
                  .init_U_StateCache_Z(MC.player.isSecondaryUseActive())
                  .load(false)
                  .load(margin)
                  .init_U_StateCache_U_StreamUtil(budget);
               autismclient.util.U.Inner_DataUtil aim = new autismclient.util.U.Inner_DataUtil(
                  point, goal, goal, clickEye.distanceTo(point), ce.load(rotation, goal), autismclient.util.U.Inner_UiHelper.load
               );
               autismclient.util.U.Inner_CachePool candidate = autismclient.util.U.init_U_CachePool_U_TaskQueue(option, aim, request, null);
               if (candidate != null
                  && (!moving || candidate.init_U_DataUtil().init_ce_DataUtil().equals(goal))
                  && candidate.init_U_TaskQueue().load_BlockPos().equals(option.load_BlockPos())
                  && candidate.init_U_TaskQueue().init_Direction() == option.init_Direction()
                  && (
                     wireEye == null
                        || i.init_Z_BlockHitResult(
                           this.init_BlockHitResult_Vec3(wireEye, candidate.init_U_DataUtil().init_ce_DataUtil(), reach),
                           option.load_BlockPos(),
                           option.init_Direction()
                        )
                  )
                  && k.init_Z_Vec3_2(
                     nextEye,
                     Vec3.directionFromRotation(candidate.init_U_DataUtil().init_ce_DataUtil().load(), candidate.init_U_DataUtil().init_ce_DataUtil().init()),
                     candidatePlan.init_BlockPos,
                     candidatePlan.init_Direction,
                     reach
                  )) {
                  return candidate;
               }

               if (request.init_U_StreamUtil().init_I() == 0) {
                  return null;
               }
            }
         }
      }

      return null;
   }

   private boolean init_Z_Str_3(String reason) {
      this.read_Str = reason;
      return false;
   }

   private boolean stop() {
      return this.init_Boolean != null ? this.init_Boolean : Math.abs(MC.player.getDeltaMovement().x) > Math.abs(MC.player.getDeltaMovement().z);
   }

   private boolean next() {
      return i.init(
         MC.player.getBoundingBox(),
         MC.player.getDeltaMovement(),
         MC.player.onGround(),
         this.init_D_13(),
         (body, motion) -> Entity.collideBoundingBox(MC.player, motion, body, MC.level, List.of()),
         this::init_i_BufferHelper_AABB
      );
   }

   private boolean prev() {
      return this.add != -2147483648 && i.init_Z_I(this.read_I_9(), this.add, 10);
   }

   private boolean init_Z_Input_2(Input input) {
      if ("Holding unsafe jump".equals(this.load_Str)) {
         this.load_Str = "Idle";
      }

      if (this.pop_Z_3() && MC.player.onGround() && (!input.shift() || input.jump()) && !put() && this.set()) {
         Vec3 wish = init_Vec3_Input_F(input, cn.init_F(MC.player));
         Vec3 motion = MC.player.getDeltaMovement();
         AABB body = MC.player.getBoundingBox();
         Vec3 centre = body.getCenter();
         AABB column = new AABB(centre.x - 0.05, body.minY - 0.6, centre.z - 0.05, centre.x + 0.05, body.minY - 0.01, centre.z + 0.05);
         boolean hanging = MC.level.noCollision(MC.player, column) && this.init_Z_D_5(this.init_D_AABB_2(column.move(0.0, body.minY - column.minY, 0.0)));
         if (hanging) {
            this.put = Math.max(this.put, 2);
         } else if (wish.lengthSqr() == 0.0 && motion.horizontalDistanceSqr() < 0.0025) {
            this.put = 0;
         }

         if (wish.lengthSqr() > 0.0 || motion.horizontalDistanceSqr() >= 0.0025) {
            double reserve = this.sync_I_2() < 0 ? 0.6 : 0.3;
            AABB ahead = body.move(motion.x * 3.0 + wish.x * reserve, 0.0, motion.z * 3.0 + wish.z * reserve);
            Vec3 next = ahead.getCenter();
            AABB feet = new AABB(next.x - 0.05, ahead.minY - 0.6, next.z - 0.05, next.x + 0.05, ahead.minY - 0.01, next.z + 0.05);
            if (MC.level.noCollision(MC.player, feet) && this.init_Z_D_5(this.init_D_AABB_2(feet.move(0.0, body.minY - feet.minY, 0.0)))) {
               this.put = 6;
            }
         }

         if (input.jump()) {
            if (((AutismLivingEntityAccessor)MC.player).autism$getNoJumpDelay() > 1) {
               return this.put-- > 0;
            }

            if (this.pop_Z || this.init_Z_Vec3_3(MC.player.position()) || MC.player.hasEffect(MobEffects.JUMP_BOOST) || !this.load_Z_Input_2(input)) {
               this.put = 0;
               return false;
            }

            this.put = Math.max(this.put, 6);
         }

         return this.put-- > 0;
      } else {
         this.put = 0;
         return false;
      }
   }

   private boolean load_Z_Input_2(Input input) {
      autismclient.ai.a.NodeWalker.Inner_StreamUtil start = autismclient.ai.a.NodeWalker.Inner_StreamUtil.of(MC.player);
      if (start.jumpDelay() > 1) {
         return false;
      }

      boolean sprint = MC.player.isSprinting() || input.sprint();
      if (!sprint && al.init_Module_Str("sprint") instanceof f.Inner_MapStore module && module.isEnabled()) {
         sprint = module.init(false, false);
      }

      autismclient.ai.a.NodeWalker.Inner_DataUtil pressed = new autismclient.ai.a.NodeWalker.Inner_DataUtil(
         (input.forward() ? 1 : 0) - (input.backward() ? 1 : 0),
         (input.left() ? 1 : 0) - (input.right() ? 1 : 0),
         true,
         sprint,
         input.shift(),
         cn.init_F(MC.player)
      );
      autismclient.ai.a.NodeWalker.Inner_CachePool world = autismclient.ai.a.NodeWalker.init_NodeWalker_CachePo_ClientLevel_Entity(MC.level, MC.player);
      boolean unsafe = this.init_Z_NodeWalker_StreamU(start, pressed, world);
      if (!unsafe && !input.shift() && start.pose() != 0) {
         unsafe = this.init_Z_NodeWalker_StreamU(i.init_NodeWalker_StreamU_NodeWalker_StreamU(start), pressed, world);
      }

      if (unsafe) {
         this.load_Str = "Holding unsafe jump";
      }

      return unsafe;
   }

   private boolean init_Z_NodeWalker_StreamU(
      autismclient.ai.a.NodeWalker.Inner_StreamUtil start,
      autismclient.ai.a.NodeWalker.Inner_DataUtil pressed,
      autismclient.ai.a.NodeWalker.Inner_CachePool world
   ) {
      i.Inner_ConfigStore intended = i.init(start, pressed, world, this.init_D_13(), null, this::init_i_BufferHelper_AABB);
      if (intended.init_Z() && !intended.init_List().isEmpty()) {
         AABB last = intended.init_List().getLast().init_AABB();
         if (!this.init_Z_D_5(this.init_D_AABB_2(last) + Math.max(0.0, start.y() - last.minY))) {
            return false;
         }

         int slot = this.sync_I_2();
         if (slot >= 0 && this.bool("own-steer") && (this.bool("own-knockback") || this.get_Z_3())) {
            for (boolean axis : new boolean[]{true, false}) {
               i.Inner_ConfigStore braking = i.init(start, pressed, world, this.init_D_13(), axis, this::init_i_BufferHelper_AABB);
               if (!braking.init_Z()) {
                  return false;
               }

               j.Inner_BufferHelper route = this.init_j_BufferHelper_i_ConfigStore_I(braking, slot);
               if (route != null) {
                  return false;
               }
            }
         }

         return true;
      } else {
         return false;
      }
   }

   private boolean init_Z_D_5(double below) {
      return this.init_i_NetHelper.init_Z(this.read_I_9())
         && (this.bool("own-knockback") && this.init_Z_D_D_2(0.0, below) || (this.get_Z_3() || this.prev()) && this.load_Z_D_D_2(0.0, below));
   }

   private Input load_Input_Input_2(Input input) {
      float keyYaw = cn.init_F(MC.player);
      Vec3 motion = MC.player.getDeltaMovement();
      Vec3 wish = init_Vec3_Input_F(input, keyYaw);
      boolean stocked = this.sync_I_2() >= 0;
      double ahead = stocked ? 0.15 : 0.6;
      double radius = stocked ? 0.24 : 0.05;
      Vec3 at = MC.player.getBoundingBox().getCenter().add(motion.x * 2.0 + wish.x * ahead, 0.0, motion.z * 2.0 + wish.z * ahead);
      AABB feet = new AABB(at.x - radius, MC.player.getY() - 0.6, at.z - radius, at.x + radius, MC.player.getY() - 0.01, at.z + radius);
      if (input.jump() || MC.level.noCollision(MC.player, feet) && this.init_Z_D_5(this.init_D_AABB_2(feet.move(0.0, MC.player.getY() - feet.minY, 0.0)))) {
         Vec3 brake = i.init_Vec3_Vec3(motion);
         double yaw = Math.toRadians(keyYaw);
         double forward = brake.z * Math.cos(yaw) - brake.x * Math.sin(yaw);
         double side = brake.x * Math.cos(yaw) + brake.z * Math.sin(yaw);
         return new Input(forward > 0.08, forward < -0.08, side > 0.08, side < -0.08, false, true, false);
      } else {
         return new Input(input.forward(), input.backward(), input.left(), input.right(), false, true, false);
      }
   }

   public static Input init_Input_Input_3(Input input) {
      j clutch = init_j();
      if (input != null && clutch != null && MC.player != null && MC.level != null) {
         Input result = init_Input_j_Input(clutch, input);
         boolean owned = result.shift() && (!input.shift() || clutch.init_Z);
         if (result.shift() != clutch.open_Z && (owned || clutch.poll_Z)) {
            ((AutismLocalPlayerAccessor)MC.player).autism$setPositionReminder(20);
         }

         clutch.open_Z = result.shift();
         clutch.poll_Z = owned;
         return result;
      } else {
         return input;
      }
   }

   private static Input init_Input_j_Input(j clutch, Input input) {
      if (clutch.init_Z && !clutch.load_Z) {
         if (!clutch.push_Z_4() || !clutch.set() || clutch.state == 3 || i.init_Z_I(clutch.read_I_9(), clutch.entry, 4)) {
            return input;
         }

         if (clutch.close_Z) {
            return clutch.load_Input_Input_2(input);
         }

         Vec3 motion = MC.player.getDeltaMovement();
         Vec3 wanted = clutch.init_Vec3_AABB(MC.player.getBoundingBox(), motion, clutch.flush_Z, clutch.state, MC.player.onGround());
         if (MC.player.onGround() && motion.y <= 0.0) {
            wanted = i.init_Vec3_Vec3(motion);
         }

         double yaw = Math.toRadians(MC.player.getYRot());
         double forward = wanted.z * Math.cos(yaw) - wanted.x * Math.sin(yaw);
         double side = wanted.x * Math.cos(yaw) + wanted.z * Math.sin(yaw);
         boolean edge = MC.player.onGround() && motion.y <= 0.0 && MC.level.noCollision(MC.player, MC.player.getBoundingBox().move(motion.x, -0.2, motion.z));
         boolean freshImpulse = clutch.init_i_TaskQueue != null && i.init_Z_I(clutch.read_I_9(), clutch.init_i_TaskQueue.init_I(), 2);
         boolean jump = clutch.init_Z_Z_I_2(clutch.update_I_3() >= 0, clutch.state);
         return new Input(
            forward > 0.08,
            forward < -0.08,
            side > 0.08,
            side < -0.08,
            jump,
            input.shift() || clutch.read_I_9() == clutch.queue || edge && (!freshImpulse || clutch.update_I_3() < 0) && !jump,
            false
         );
      } else {
         return clutch.isEnabled() && clutch.init_Z_Input_2(input) ? clutch.load_Input_Input_2(input) : input;
      }
   }

   public static void init_V_Packet_2(Packet<?> packet) {
      if (packet instanceof ServerboundMovePlayerPacket move) {
         j clutch = init_j();
         if (clutch != null && MC.player != null && (clutch.isEnabled() || clutch.init_Z)) {
            j.Inner_ConfigStore old = clutch.init_j_ConfigStore;
            Vec3 fallback = old == null ? MC.player.position() : old.init_Vec3;
            ce.Inner_DataUtil oldAim = old == null ? ce.init(MC.player) : old.init_ce_DataUtil;
            clutch.init_j_ConfigStore = new j.Inner_ConfigStore(
               new Vec3(move.getX(fallback.x), move.getY(fallback.y), move.getZ(fallback.z)),
               new ce.Inner_DataUtil(move.getYRot(oldAim.init()), move.getXRot(oldAim.load())),
               MC.player.getEyeHeight(),
               clutch.read_I_9()
            );
         }
      }
   }

   private void poll_V_5() {
      if (!this.update_Z) {
         this.update_Z = true;
         autismclient.util.ah.load_V(this.id());
         if (this.bool("restore-slot") && this.next >= 0 && this.prev >= 0 && this.next != this.prev) {
            this.valid = this.next;
            this.size = this.prev;
            this.data = 20;
            this.run_V_3();
         }

         if ("clutch".equals(autismclient.util.aD.init_Str()) && !this.valid()) {
            autismclient.util.aD.init(this.id());
         }
      }
   }

   private void push_V_4() {
      if (this.init_Z) {
         this.init_j_DataUtil = null;
         autismclient.util.ah.load_V(this.id());
         if (MC.player != null && MC.level != null && MC.player.isAlive()) {
            if (!this.load_Z && !this.update_Z) {
               this.load_Z = true;
               this.get = 0;
               if (this.bool("restore-slot") && this.next >= 0 && this.prev >= 0 && this.next != this.prev) {
                  this.valid = this.next;
                  this.size = this.prev;
                  this.data = 20;
               }

               this.run_V_3();
            }

            this.get++;
            ce.Inner_DataUtil resume = aI.read_ce_DataUtil();
            boolean bridge = this.valid();
            if (this.update_Z) {
               this.load_V_Z_3(bridge);
            } else {
               if (bridge && "clutch".equals(autismclient.util.aD.init_Str()) && this.get < 20) {
                  ce.Inner_DataUtil goal = resume != null ? resume : ce.init(MC.player);
                  autismclient.util.aD.init_V_Str(this.id(), 50, goal);
                  autismclient.util.aD.init_V_Str_4(this.id(), MC.player, Math.max(1, Math.min(8, 19 - this.get)));
                  ce.Inner_DataUtil end = autismclient.util.aD.init_ce_DataUtil();
                  if (end != null && ce.load(end, goal) > 1.0F) {
                     this.load_Str = "Handing back";
                     return;
                  }
               }

               this.load_V_Z_3(bridge);
            }
         } else {
            this.get_V_3();
         }
      }
   }

   private boolean valid() {
      return aI.read_ce_DataUtil() != null || put();
   }

   private void load_V_Z_3(boolean bridge) {
      ce.Inner_DataUtil end = autismclient.util.aD.init_ce_DataUtil();
      boolean ours = "clutch".equals(autismclient.util.aD.init_Str());
      if (bridge && end != null) {
         if (ours) {
            float offset = aI.load_F_F_F(end.init(), MC.player.getYRot());
            if (offset != 0.0F && Float.isFinite(offset)) {
               MC.player.setYRot(MC.player.getYRot() + offset);
               MC.player.yRotO += offset;
               MC.player.setYHeadRot(MC.player.getYHeadRot() + offset);
               MC.player.yHeadRotO += offset;
               MC.player.yBodyRot += offset;
               MC.player.yBodyRotO += offset;
               MC.player.yBob += offset;
               MC.player.yBobO += offset;
            }
         }

         aI.load_V_ce_DataUtil(end);
         if (ours) {
            autismclient.util.aD.load_V();
         }
      } else if (ours) {
         autismclient.util.aD.init(this.id());
      }

      if (this.sync_Z) {
         this.add = this.read_I_9();
      }

      this.sync_Z = false;
      this.init_Z = this.load_Z = this.write_Z = this.update_Z = this.close_Z = false;
      this.state = 0;
      this.get = 0;
      this.load_Str = "Idle";
      this.next = this.prev = -1;
   }

   private void pop_V_3() {
      if (!this.init_Z && MC.player != null && this.tick_Z_6()) {
         autismclient.util.aD.init_V_Str_LocalPlayer(this.id(), MC.player);
      }
   }

   private void run_V_3() {
      if (this.valid >= 0 && MC.player != null) {
         if ((!this.init_Z || this.load_Z || this.update_Z) && --this.data >= 0 && MC.player.getInventory().getSelectedSlot() == this.size) {
            autismclient.util.as.init(this.valid, this.size);
         } else {
            this.valid = -1;
         }
      }
   }

   @Override
   public boolean shouldCancelAttack(HitResult hit) {
      return init_Z_49();
   }

   @Override
   public boolean shouldCancelUse(HitResult hit, InteractionHand hand) {
      return init_Z_49();
   }

   @Override
   public void onDisable() {
      cg.init_V(cg.Inner_DataUtil.load);
      this.init_i_TaskQueue = null;
      this.poll_I = -2147483648;
      if (this.init_Z) {
         autismclient.util.s.write_V();
      }

      this.push_V_4();
   }

   @Override
   public void onGameLeft() {
      this.get_V_3();
      cg.load(cg.Inner_DataUtil.load);
   }

   private void get_V_3() {
      if ("clutch".equals(autismclient.util.aD.init_Str())) {
         autismclient.util.aD.load_V();
      }

      autismclient.util.ah.update_V(this.id());
      this.init_Map.clear();
      this.load_Map.clear();
      this.init_i_TaskQueue = null;
      this.poll_I = -2147483648;
      this.init_j_ConfigStore = null;
      this.init_Obj = null;
      this.init_Z = this.load_Z = this.read_Z = this.write_Z = this.update_Z = false;
      this.get = 0;
      this.next = this.prev = -1;
      this.init_j_DataUtil = null;
      this.add = -2147483648;
      this.sync_Z = false;
      this.init_i_NetHelper.init();
      this.init_Vec3 = null;
      this.load_Vec3 = Vec3.ZERO;
      this.clear = this.reset = -2147483648;
      this.start = -2147483648;
      this.valid = this.size = -1;
      this.init_BlockPos = this.load_BlockPos = null;
      this.read_BlockPos = this.write_BlockPos = this.sync_BlockPos = null;
      this.value = -2147483648;
      this.load_Str = "Idle";
      this.tick = 0;
      this.push_I = -2147483648;
      this.put = 0;
      this.state = 0;
      this.close_Z = false;
      this.push_Z = false;
      this.init_Boolean = null;
      this.open_Z = this.poll_Z = false;
      this.queue = -2147483648;
      this.read_Vec3 = null;
      this.init_Str = "";
      this.buffer = -2147483648;
      this.write_Vec3 = null;
      this.index = -2147483648;
      this.update_Vec3 = null;
      this.entry = -2147483648;
      this.pop_Z = false;
      this.node = -2147483648;
      this.task = -2147483648;
      this.pool = -2147483648;
      this.init_BlockHitResult = null;
      this.update_BlockPos = null;
      this.frame = this.block = -1;
      this.chunk = -2147483648;
   }

   private record Inner_BufferHelper(k.Inner_JsonUtil init_k_JsonUtil, List<AABB> init_List, double init_D, boolean init_Z, List<Vec3> load) {
      Inner_BufferHelper(k.Inner_JsonUtil plan, List<AABB> bodies, double eyeHeight) {
         this(plan, bodies, eyeHeight, false, List.of());
      }

      Inner_BufferHelper(k.Inner_JsonUtil plan, List<AABB> bodies, double eyeHeight, boolean tower) {
         this(plan, bodies, eyeHeight, tower, List.of());
      }

      Vec3 init(int tick) {
         return this.load.isEmpty() ? init(this.init_List, this.init_D, tick) : this.load.get(Mth.clamp(tick, 0, this.load.size() - 1));
      }

      ce.Inner_DataUtil init_ce_DataUtil_k_NetHelper_I(k.Inner_NetHelper link, int tick) {
         return init_ce_DataUtil_Vec3_k_NetHelper(this.init(tick), link);
      }

      static Vec3 init(List<AABB> bodies, double eyeHeight, int tick) {
         AABB body = bodies.get(Mth.clamp(tick, 0, bodies.size() - 1));
         return new Vec3((body.minX + body.maxX) * 0.5, body.minY + eyeHeight, (body.minZ + body.maxZ) * 0.5);
      }

      static ce.Inner_DataUtil init(List<AABB> bodies, double eyeHeight, k.Inner_NetHelper link, int tick) {
         return init_ce_DataUtil_Vec3_k_NetHelper(init(bodies, eyeHeight, tick), link);
      }

      static ce.Inner_DataUtil init_ce_DataUtil_Vec3_k_NetHelper(Vec3 eye, k.Inner_NetHelper link) {
         BlockPos support = link.load_BlockPos();
         ce.Inner_DataUtil goal = ce.init(k.init_Vec3_Vec3(eye, support.getX(), support.getY(), support.getZ(), link.init_Direction()), eye);
         return new ce.Inner_DataUtil(goal.init(), Mth.clamp(goal.load(), -89.5F, 89.5F));
      }
   }

   private record Inner_CachePool(int init_I, i.Inner_StreamUtil init_i_StreamUtil, boolean init_Z, boolean load_Z, Vec3 init_Vec3, Vec3 load_Vec3, int load_I) {
   }

   private record Inner_ConfigStore(Vec3 init_Vec3, ce.Inner_DataUtil init_ce_DataUtil, double init_D, int init_I) {
   }

   private record Inner_DataUtil(
      BlockPos init_BlockPos,
      autismclient.util.U.Inner_CachePool init_U_CachePool,
      int init_I,
      int load_I,
      boolean init_Z,
      boolean load_Z,
      Direction init_Direction,
      boolean read
   ) {
   }

   private final class Inner_StreamUtil {
      private boolean init_Z;
      private final Long2ByteOpenHashMap init_Long2ByteOpenHashM = new Long2ByteOpenHashMap();
      private final Long2ByteOpenHashMap load_Long2ByteOpenHashM = new Long2ByteOpenHashMap();
      private final Long2ByteOpenHashMap read = new Long2ByteOpenHashMap();
      private final MutableBlockPos init_BlockPos_MutableBl = new MutableBlockPos();
      private boolean load_Z;
      private List<AABB> init_List;
      private List<AABB> load_List;
      private j.Inner_ConfigStore init_j_ConfigStore;
      private double init_D = 0.0 / 0.0;
      private BlockHitResult init_BlockHitResult;
      private DoubleUnaryOperator init_DoubleUnaryOperato;

      void init_V() {
         this.init_Z = true;
         this.init_Long2ByteOpenHashM.clear();
         this.load_Long2ByteOpenHashM.clear();
         this.read.clear();
         this.init_List = this.load_List = null;
         this.init_j_ConfigStore = null;
         this.init_D = 0.0 / 0.0;
         this.init_BlockHitResult = null;
         this.init_DoubleUnaryOperato = null;
      }

      void load() {
         this.init_Z = false;
         this.init_List = this.load_List = null;
         this.init_j_ConfigStore = null;
         this.init_BlockHitResult = null;
         this.init_DoubleUnaryOperato = null;
      }

      boolean init(int x, int y, int z, boolean sneaking) {
         if (!this.init_Z) {
            return this.load(x, y, z, sneaking);
         }

         if (this.load_Z != sneaking) {
            this.init_Long2ByteOpenHashM.clear();
            this.load_Z = sneaking;
         }

         long key = BlockPos.asLong(x, y, z);
         byte known = this.init_Long2ByteOpenHashM.get(key);
         if (known == 0) {
            this.init_Long2ByteOpenHashM.put(key, known = (byte)(this.load(x, y, z, sneaking) ? 1 : 2));
         }

         return known == 1;
      }

      boolean init(int x, int y, int z) {
         if (!this.init_Z) {
            return this.read(x, y, z);
         }

         long key = BlockPos.asLong(x, y, z);
         byte known = this.load_Long2ByteOpenHashM.get(key);
         if (known == 0) {
            this.load_Long2ByteOpenHashM.put(key, known = (byte)(this.read(x, y, z) ? 1 : 2));
         }

         return known == 1;
      }

      boolean init_Z_Vec3_D(Vec3 eye, double distance) {
         double reach = distance * distance;
         int minX = Mth.floor(eye.x - distance - 0.5) >> 2;
         int maxX = Mth.ceil(eye.x + distance - 0.5) >> 2;
         int minY = Mth.floor(eye.y - distance - 0.5) >> 2;
         int maxY = Mth.ceil(eye.y + distance - 0.5) >> 2;
         int minZ = Mth.floor(eye.z - distance - 0.5) >> 2;
         int maxZ = Mth.ceil(eye.z + distance - 0.5) >> 2;

         for (int qx = minX; qx <= maxX; qx++) {
            double dx = init_D_D_I(eye.x, qx);

            for (int qz = minZ; qz <= maxZ; qz++) {
               double dz = init_D_D_I(eye.z, qz);
               if (!(dx * dx + dz * dz > reach)) {
                  for (int qy = minY; qy <= maxY; qy++) {
                     double dy = init_D_D_I(eye.y, qy);
                     if (!(dx * dx + dy * dy + dz * dz > reach)) {
                        if (!this.init_Z) {
                           if (this.load(qx, qy, qz)) {
                              return false;
                           }
                        } else {
                           long key = BlockPos.asLong(qx, qy, qz);
                           byte known = this.read.get(key);
                           if (known == 0) {
                              this.read.put(key, known = (byte)(this.load(qx, qy, qz) ? 2 : 1));
                           }

                           if (known == 2) {
                              return false;
                           }
                        }
                     }
                  }
               }
            }
         }

         return true;
      }

      private static double init_D_D_I(double eye, int cube) {
         double low = cube * 4 + 0.5;
         double high = cube * 4 + 3.5;
         return eye < low ? low - eye : (eye > high ? eye - high : 0.0);
      }

      private boolean load(int qx, int qy, int qz) {
         int sy = qy >> 2;
         if (sy >= Module.MC.level.getMinSectionY() && sy <= Module.MC.level.getMaxSectionY()) {
            if (!(Module.MC.level.getChunkSource().getChunk(qx >> 2, qz >> 2, ChunkStatus.FULL, false) instanceof LevelChunk chunk)) {
               return false;
            } else {
               LevelChunkSection[] var15 = chunk.getSections();
               int index = Module.MC.level.getSectionIndexFromSectionY(sy);
               if (index >= 0 && index < var15.length) {
                  LevelChunkSection section = var15[index];
                  if (section != null && !section.hasOnlyAir()) {
                     int lx = (qx & 3) << 2;
                     int ly = (qy & 3) << 2;
                     int lz = (qz & 3) << 2;

                     for (int x = lx; x < lx + 4; x++) {
                        for (int y = ly; y < ly + 4; y++) {
                           for (int z = lz; z < lz + 4; z++) {
                              if (!section.getBlockState(x, y, z).canBeReplaced()) {
                                 return true;
                              }
                           }
                        }
                     }

                     return false;
                  } else {
                     return false;
                  }
               } else {
                  return false;
               }
            }
         } else {
            return false;
         }
      }

      List<AABB> init(boolean optional) {
         if (!this.init_Z) {
            return j.this.init_List_Z(optional);
         } else if (optional) {
            return this.load_List != null ? this.load_List : (this.load_List = j.this.init_List_Z(true));
         } else {
            return this.init_List != null ? this.init_List : (this.init_List = j.this.init_List_Z(false));
         }
      }

      BlockHitResult init(double range) {
         j.Inner_ConfigStore written = j.this.init_j_ConfigStore;
         if (this.init_Z && written == this.init_j_ConfigStore && range == this.init_D) {
            return this.init_BlockHitResult;
         }

         BlockHitResult result = written != null && written.init_I < j.this.read_I_9()
            ? j.this.init_BlockHitResult_Vec3(written.init_Vec3.add(0.0, written.init_D, 0.0), written.init_ce_DataUtil, range)
            : null;
         if (this.init_Z) {
            this.init_j_ConfigStore = written;
            this.init_D = range;
            this.init_BlockHitResult = result;
         }

         return result;
      }

      DoubleUnaryOperator init_DoubleUnaryOperato() {
         if (!this.init_Z) {
            return j.this.load_DoubleUnaryOperato();
         } else {
            return this.init_DoubleUnaryOperato != null ? this.init_DoubleUnaryOperato : (this.init_DoubleUnaryOperato = j.this.load_DoubleUnaryOperato());
         }
      }

      private boolean load(int x, int y, int z, boolean sneaking) {
         this.init_BlockPos_MutableBl.set(x, y, z);
         if (!Module.MC.level.hasChunkAt(this.init_BlockPos_MutableBl)) {
            return false;
         }

         BlockState state = Module.MC.level.getBlockState(this.init_BlockPos_MutableBl);
         return !state.canBeReplaced() && autismclient.util.U.load(state, this.init_BlockPos_MutableBl.immutable(), sneaking);
      }

      private boolean read(int x, int y, int z) {
         this.init_BlockPos_MutableBl.set(x, y, z);
         if (!Module.MC.level.isOutsideBuildHeight(this.init_BlockPos_MutableBl) && Module.MC.level.hasChunkAt(this.init_BlockPos_MutableBl)) {
            BlockState state = Module.MC.level.getBlockState(this.init_BlockPos_MutableBl);
            return state.canBeReplaced() && state.getFluidState().isEmpty();
         } else {
            return false;
         }
      }
   }
}

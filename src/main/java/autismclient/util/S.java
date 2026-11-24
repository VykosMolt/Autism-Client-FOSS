package autismclient.util;

import autismclient.mixin.accessor.AutismEntityAccessor;
import autismclient.mixin.accessor.AutismLivingEntityAccessor;
import java.lang.ref.WeakReference;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.atomic.AtomicLong;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.util.Mth;
import net.minecraft.world.Difficulty;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.arrow.AbstractArrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.BlocksAttacks;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerExplosion;
import net.minecraft.world.level.ClipContext.Block;
import net.minecraft.world.level.ClipContext.Fluid;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.HitResult.Type;
import net.minecraft.world.phys.shapes.CollisionContext;

public final class S {
   public static final float init_F = 6.0F;
   public static final float load_F = 5.0F;
   public static final float read_F = 5.0F;
   public static final float write = 9.0F;
   private static final EquipmentSlot[] init_EquipmentSlotArr = new EquipmentSlot[]{
      EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET
   };
   private static final int load_I = 256;
   private static final long init_J = -1L;
   private static final Object init_Obj = new Object();
   private static final Map<S.Inner_DataUtil, Double> init_Map = new LinkedHashMap<S.Inner_DataUtil, Double>(256, 1.0F, true) {
      @Override
      protected boolean removeEldestEntry(Entry<S.Inner_DataUtil, Double> eldest) {
         return this.size() > 256;
      }
   };
   private static final AtomicLong init_AtomicLong = new AtomicLong();
   private static long load_J = -1L;
   private static long read_J = -1L;
   private static WeakReference<Level> init_WeakReference = new WeakReference<>(null);
   private static int read_I = -2147483648;
   public static final int init_I = 10;
   public static final double init_D = 1.15;
   public static final double load_D = 2.0;
   public static final double read_D = 1.0;

   private S() {
   }

   public static double init_D_LivingEntity(LivingEntity target, Vec3 explosionPos, float power) {
      return init_D(target, explosionPos, power, S.Inner_CachePool.init_S_CachePool);
   }

   public static double init_D(LivingEntity target, Vec3 explosionPos, float power, S.Inner_CachePool options) {
      return init(target, explosionPos, power, options == null ? S.Inner_CachePool.init_S_CachePool : options, true);
   }

   private static double init(LivingEntity target, Vec3 explosionPos, float power, S.Inner_CachePool options, boolean sampleExposure) {
      if (target != null && explosionPos != null && power > 0.0F) {
         Level level = target.level();
         if (level != null && !init_Z_LivingEntity(target)) {
            float range = power * 2.0F;
            double distanceSqr = init_D_LivingEntity_2(target, explosionPos, options);
            if (distanceSqr > (double)range * range) {
               return 0.0;
            }

            double decay = 1.0 - Math.sqrt(distanceSqr) / range;
            double seen = (sampleExposure ? init_F_LivingEntity_2(target, explosionPos, options) : 1.0F) * decay;
            double raw = (seen * seen + seen) / 2.0 * 7.0 * range + 1.0;
            if (raw <= 0.0) {
               return 0.0;
            }

            DamageSource source = options.init_DamageSource() != null ? options.init_DamageSource() : init_DamageSource_Level_Vec3(level, explosionPos);
            return init(target, source, (float)raw, options.load());
         } else {
            return 0.0;
         }
      } else {
         return 0.0;
      }
   }

   private static double init_D_LivingEntity_2(LivingEntity target, Vec3 explosionPos, S.Inner_CachePool options) {
      AABB box = options.init_AABB();
      if (box == null) {
         return target.distanceToSqr(explosionPos);
      }

      double dx = (box.minX + box.maxX) / 2.0 - explosionPos.x;
      double dy = box.minY - explosionPos.y;
      double dz = (box.minZ + box.maxZ) / 2.0 - explosionPos.z;
      return dx * dx + dy * dy + dz * dz;
   }

   public static double init_D_Vec3_F(Vec3 explosionPos, float power) {
      return init_D_Vec3(explosionPos, power, S.Inner_CachePool.init_S_CachePool);
   }

   public static double init_D_Vec3(Vec3 explosionPos, float power, S.Inner_CachePool options) {
      return init_D(Minecraft.getInstance().player, explosionPos, power, options);
   }

   public static boolean init_Z_LivingEntity_2(LivingEntity target, Vec3 explosionPos, float power) {
      return init_Z(target, explosionPos, power, S.Inner_CachePool.init_S_CachePool);
   }

   public static boolean init_Z(LivingEntity target, Vec3 explosionPos, float power, S.Inner_CachePool options) {
      double health = init_D_LivingEntity(target);
      return health <= 0.0 ? false : init_D(target, explosionPos, power, options) >= health;
   }

   public static boolean init_Z_Vec3_F(Vec3 explosionPos, float power) {
      return init_Z(Minecraft.getInstance().player, explosionPos, power, S.Inner_CachePool.init_S_CachePool);
   }

   public static double init_D_LivingEntity(LivingEntity target) {
      return target != null && !target.isDeadOrDying() ? (double)target.getHealth() + target.getAbsorptionAmount() : 0.0;
   }

   public static boolean init_Z_LivingEntity_DamageSource(LivingEntity target, DamageSource source) {
      if (target == null) {
         return false;
      } else {
         return source != null && source.is(DamageTypeTags.BYPASSES_COOLDOWN) ? false : target.getInvulnerableTime() > 10;
      }
   }

   public static boolean load_Z(LivingEntity target, Vec3 explosionPos, float power, S.Inner_CachePool options) {
      return init_Z_LivingEntity(target, init_D(target, explosionPos, power, options), options);
   }

   public static boolean init_Z_LivingEntity(LivingEntity target, double predictedDamage, S.Inner_CachePool options) {
      if (target != null && predictedDamage > 0.0) {
         double health = init_D_LivingEntity(target);
         if (health <= 0.0) {
            return false;
         } else {
            return init_Z_LivingEntity_DamageSource(target, options == null ? null : options.init_DamageSource()) ? false : predictedDamage * 1.0 >= health;
         }
      } else {
         return false;
      }
   }

   public static boolean init_Z_Vec3(Vec3 explosionPos, float power, S.Inner_CachePool options) {
      return init_Z_D(init_D_Vec3(explosionPos, power, options));
   }

   public static boolean init_Z_D(double predictedSelfDamage) {
      LocalPlayer player = Minecraft.getInstance().player;
      double health = init_D_LivingEntity(player);
      return health <= 0.0 ? false : predictedSelfDamage * 1.15 + 2.0 >= health;
   }

   public static boolean load(double predictedSelfDamage) {
      return !init_Z_D(predictedSelfDamage) ? false : !init_Z();
   }

   private static boolean init_Z() {
      LocalPlayer player = Minecraft.getInstance().player;
      return player == null ? false : player.getMainHandItem().is(Items.TOTEM_OF_UNDYING) || player.getOffhandItem().is(Items.TOTEM_OF_UNDYING);
   }

   public static S.Inner_StreamUtil init_S_StreamUtil(LivingEntity target, Vec3 explosionPos, float power, S.Inner_CachePool options) {
      return init_S_StreamUtil_S_BufferHelper(init_S_BufferHelper(target, explosionPos, power, options), target, options);
   }

   public static S.Inner_StreamUtil load_S_StreamUtil(LivingEntity target, Vec3 explosionPos, float power, S.Inner_CachePool options) {
      return init_S_StreamUtil_S_BufferHelper(load_S_BufferHelper(target, explosionPos, power, options), target, options);
   }

   public static S.Inner_StreamUtil init_S_StreamUtil_S_BufferHelper(S.Inner_BufferHelper ranking, LivingEntity target, S.Inner_CachePool options) {
      return ranking == null
         ? new S.Inner_StreamUtil(false, false, 0.0, 0.0)
         : new S.Inner_StreamUtil(init_Z_LivingEntity(target, ranking.load(), options), init_Z_D(ranking.read()), ranking.load(), ranking.read());
   }

   public static S.Inner_BufferHelper init_S_BufferHelper_LivingEntity(LivingEntity target, Vec3 explosionPos, float power) {
      return init_S_BufferHelper(target, explosionPos, power, S.Inner_CachePool.init_S_CachePool);
   }

   public static S.Inner_BufferHelper init_S_BufferHelper(LivingEntity target, Vec3 explosionPos, float power, S.Inner_CachePool options) {
      double targetDamage = init_D(target, explosionPos, power, options);
      LocalPlayer player = Minecraft.getInstance().player;
      double self = target == player ? targetDamage : init_D(player, explosionPos, power, options);
      return new S.Inner_BufferHelper(targetDamage, self);
   }

   public static double load(LivingEntity target, Vec3 explosionPos, float power) {
      return load_D(target, explosionPos, power, S.Inner_CachePool.init_S_CachePool);
   }

   public static double load_D(LivingEntity target, Vec3 explosionPos, float power, S.Inner_CachePool options) {
      return init(target, explosionPos, power, options == null ? S.Inner_CachePool.init_S_CachePool : options, false);
   }

   public static double load(Vec3 explosionPos, float power, S.Inner_CachePool options) {
      return load_D(Minecraft.getInstance().player, explosionPos, power, options);
   }

   public static S.Inner_ConfigStore init_S_ConfigStore() {
      Minecraft mc = Minecraft.getInstance();
      Level level = mc.level;
      int tick = init_I_Minecraft(mc);
      synchronized (init_Obj) {
         load_J = init_AtomicLong.incrementAndGet();
         init_V_Level_I(level, tick);
         return new S.Inner_ConfigStore(load_J);
      }
   }

   private static void init_V_J(long passId) {
      synchronized (init_Obj) {
         if (load_J == passId) {
            init_Map.clear();
            init_AtomicLong.incrementAndGet();
            load_J = -1L;
            read_J = -1L;
            init_WeakReference = new WeakReference<>(null);
            read_I = -2147483648;
         }
      }
   }

   public static double read(LivingEntity target, Vec3 explosionPos, float power, S.Inner_CachePool options) {
      if (target != null && explosionPos != null && power > 0.0F) {
         S.Inner_CachePool opts = options == null ? S.Inner_CachePool.init_S_CachePool : options;
         Minecraft mc = Minecraft.getInstance();
         Level level = mc.level;
         long generation;
         synchronized (init_Obj) {
            if (load_J == -1L) {
               generation = -1L;
            } else {
               int tick = init_I_Minecraft(mc);
               if (init_WeakReference.get() != level || read_I != tick) {
                  init_V_Level_I(level, tick);
               }

               generation = read_J;
            }
         }

         if (generation == -1L) {
            return init_D(target, explosionPos, power, opts);
         }

         S.Inner_DataUtil key = new S.Inner_DataUtil(target, target.getBoundingBox(), explosionPos, power, opts, init_J_LivingEntity(target));
         synchronized (init_Obj) {
            if (read_J != generation) {
               return init_D(target, explosionPos, power, opts);
            }

            Double cached = init_Map.get(key);
            if (cached != null) {
               return cached;
            }
         }

         double damage = init_D(target, explosionPos, power, opts);
         boolean moved = init_Z_S_DataUtil_LivingEntity(key, target);
         synchronized (init_Obj) {
            if (read_J != generation) {
               return damage;
            }

            if (init_WeakReference.get() == level && read_I == init_I_Minecraft(mc)) {
               if (!moved) {
                  init_Map.put(key, damage);
               }

               return damage;
            } else {
               return damage;
            }
         }
      } else {
         return 0.0;
      }
   }

   private static int init_I_Minecraft(Minecraft mc) {
      return mc.player == null ? -2147483648 : ck.init_ck().state_I();
   }

   private static void init_V_Level_I(Level level, int tick) {
      init_Map.clear();
      read_J = init_AtomicLong.incrementAndGet();
      init_WeakReference = new WeakReference<>(level);
      read_I = tick;
   }

   public static double read(Vec3 explosionPos, float power, S.Inner_CachePool options) {
      return read(Minecraft.getInstance().player, explosionPos, power, options);
   }

   public static S.Inner_BufferHelper load_S_BufferHelper(LivingEntity target, Vec3 explosionPos, float power, S.Inner_CachePool options) {
      double targetDamage = read(target, explosionPos, power, options);
      LocalPlayer player = Minecraft.getInstance().player;
      double self = target == player ? targetDamage : read(player, explosionPos, power, options);
      return new S.Inner_BufferHelper(targetDamage, self);
   }

   public static void init_V() {
      synchronized (init_Obj) {
         init_Map.clear();
         long generation = init_AtomicLong.incrementAndGet();
         if (load_J != -1L) {
            read_J = generation;
         }
      }
   }

   private static boolean init_Z_S_DataUtil_LivingEntity(S.Inner_DataUtil key, LivingEntity target) {
      return !key.init_AABB().equals(target.getBoundingBox()) ? true : key.init_J() != init_J_LivingEntity(target);
   }

   private static long init_J_LivingEntity(LivingEntity target) {
      long hash = 17L;
      hash = hash * 31L + (target.isRemoved() ? 1L : 0L);
      hash = hash * 31L + (target.isDeadOrDying() ? 1L : 0L);
      hash = hash * 31L + (target.isSpectator() ? 1L : 0L);
      hash = hash * 31L + (target.isInvulnerable() ? 1L : 0L);
      if (target instanceof Player player) {
         hash = hash * 31L + (player.getAbilities().invulnerable ? 1L : 0L);
      }

      Level level = target.level();
      hash = hash * 31L + (level == null ? -1L : level.getDifficulty().ordinal());
      hash = hash * 31L + target.getArmorValue();
      hash = hash * 31L + Double.doubleToLongBits(target.getAttributeValue(Attributes.ARMOR_TOUGHNESS));
      MobEffectInstance resistance = target.getEffect(MobEffects.RESISTANCE);
      hash = hash * 31L + (resistance == null ? -1L : resistance.getAmplifier());
      hash = hash * 31L + (target.hasEffect(MobEffects.FIRE_RESISTANCE) ? 1L : 0L);
      hash = hash * 31L + Float.floatToIntBits(target.getYHeadRot());
      hash = hash * 31L + init_J_ItemStack(target.getItemBlockingWith());

      for (EquipmentSlot slot : init_EquipmentSlotArr) {
         hash = hash * 31L + init_J_ItemStack(target.getItemBySlot(slot));
      }

      return hash;
   }

   private static long init_J_ItemStack(ItemStack stack) {
      if (stack != null && !stack.isEmpty()) {
         long hash = stack.getItem().hashCode();
         hash = hash * 31L + stack.getDamageValue();
         ItemEnchantments enchantments = stack.getEnchantments();
         return hash * 31L + (enchantments == null ? 0L : enchantments.hashCode());
      } else {
         return 0L;
      }
   }

   public static float init_F_LivingEntity_2(LivingEntity target, Vec3 explosionPos, S.Inner_CachePool options) {
      if (target != null && explosionPos != null) {
         Level level = target.level();
         if (level == null) {
            return 0.0F;
         }

         S.Inner_CachePool opts = options == null ? S.Inner_CachePool.init_S_CachePool : options;
         if (!opts.init_Z()) {
            return ServerExplosion.getSeenPercent(explosionPos, target);
         }

         AABB box = opts.init_AABB() != null ? opts.init_AABB() : target.getBoundingBox();
         CollisionContext shapeContext = opts.init_AABB() != null ? CollisionContext.withPosition(target, box.minY) : CollisionContext.of(target);
         double stepX = 1.0 / ((box.maxX - box.minX) * 2.0 + 1.0);
         double stepY = 1.0 / ((box.maxY - box.minY) * 2.0 + 1.0);
         double stepZ = 1.0 / ((box.maxZ - box.minZ) * 2.0 + 1.0);
         if (!(stepX < 0.0) && !(stepY < 0.0) && !(stepZ < 0.0)) {
            double offsetX = (1.0 - Math.floor(1.0 / stepX) * stepX) / 2.0;
            double offsetZ = (1.0 - Math.floor(1.0 / stepZ) * stepZ) / 2.0;
            int hits = 0;
            int totalRays = 0;

            for (double x = 0.0; x <= 1.0; x += stepX) {
               for (double y = 0.0; y <= 1.0; y += stepY) {
                  for (double z = 0.0; z <= 1.0; z += stepZ) {
                     Vec3 sample = new Vec3(
                        Mth.lerp(x, box.minX, box.maxX) + offsetX, Mth.lerp(y, box.minY, box.maxY), Mth.lerp(z, box.minZ, box.maxZ) + offsetZ
                     );
                     ClipContext clip = new ClipContext(sample, explosionPos, Block.COLLIDER, Fluid.NONE, shapeContext);
                     if (init_BlockHitResult_Level(level, clip, opts).getType() == Type.MISS) {
                        hits++;
                     }

                     totalRays++;
                  }
               }
            }

            return (float)hits / totalRays;
         } else {
            return 0.0F;
         }
      } else {
         return 0.0F;
      }
   }

   private static BlockHitResult init_BlockHitResult_Level(Level level, ClipContext context, S.Inner_CachePool options) {
      List<BlockPos> exclude = options.init_List();
      BlockPos include = options.init_BlockPos();
      Float maxBlastResistance = options.init_Float();
      return BlockGetter.traverseBlocks(context.getFrom(), context.getTo(), context, (ctx, pos) -> {
         boolean excluded = !exclude.isEmpty() && exclude.contains(pos);
         BlockState blockState;
         if (excluded) {
            blockState = Blocks.VOID_AIR.defaultBlockState();
         } else if (pos.equals(include)) {
            blockState = Blocks.OBSIDIAN.defaultBlockState();
         } else {
            blockState = level.getBlockState(pos);
            if (maxBlastResistance != null && blockState.getBlock().getExplosionResistance() < maxBlastResistance) {
               blockState = Blocks.VOID_AIR.defaultBlockState();
            }
         }

         FluidState fluidState;
         if (excluded) {
            fluidState = Fluids.EMPTY.defaultFluidState();
         } else {
            fluidState = level.getFluidState(pos);
            if (maxBlastResistance != null && fluidState.getExplosionResistance() < maxBlastResistance) {
               fluidState = Fluids.EMPTY.defaultFluidState();
            }
         }

         Vec3 from = ctx.getFrom();
         Vec3 to = ctx.getTo();
         BlockHitResult blockHit = level.clipWithInteractionOverride(from, to, pos, ctx.getBlockShape(blockState, level, pos), blockState);
         BlockHitResult fluidHit = ctx.getFluidShape(fluidState, level, pos).clip(from, to, pos);
         double blockDistance = blockHit == null ? 1.7976931348623157E308 : from.distanceToSqr(blockHit.getLocation());
         double fluidDistance = fluidHit == null ? 1.7976931348623157E308 : from.distanceToSqr(fluidHit.getLocation());
         return blockDistance <= fluidDistance ? blockHit : fluidHit;
      }, ctx -> {
         Vec3 delta = ctx.getFrom().subtract(ctx.getTo());
         return BlockHitResult.miss(ctx.getTo(), Direction.getApproximateNearest(delta.x, delta.y, delta.z), BlockPos.containing(ctx.getTo()));
      });
   }

   public static double init(LivingEntity target, DamageSource source, float damage, boolean estimateProtection) {
      if (target != null && source != null) {
         if (((AutismEntityAccessor)target).autism$isInvulnerableToBase(source)) {
            return 0.0;
         }

         if (target.isDeadOrDying()) {
            return 0.0;
         }

         float amount = damage;
         if (target instanceof Player player) {
            if (player.getAbilities().invulnerable && !source.is(DamageTypeTags.BYPASSES_INVULNERABILITY)) {
               return 0.0;
            }

            if (source.scalesWithDifficulty()) {
               Difficulty difficulty = target.level().getDifficulty();
               if (difficulty == Difficulty.PEACEFUL) {
                  amount = 0.0F;
               } else if (difficulty == Difficulty.EASY) {
                  amount = Math.min(amount / 2.0F + 1.0F, amount);
               } else if (difficulty == Difficulty.HARD) {
                  amount = amount * 3.0F / 2.0F;
               }
            }
         }

         if (amount <= 0.0F) {
            return 0.0;
         }

         if (source.is(DamageTypeTags.IS_FIRE) && target.hasEffect(MobEffects.FIRE_RESISTANCE)) {
            return 0.0;
         }

         amount -= init_F_LivingEntity(target, source, amount);
         if (amount <= 0.0F) {
            return 0.0;
         }

         AutismLivingEntityAccessor accessor = (AutismLivingEntityAccessor)target;
         amount = accessor.autism$getDamageAfterArmorAbsorb(source, amount);
         amount = accessor.autism$getDamageAfterMagicAbsorb(source, amount);
         if (estimateProtection) {
            amount = load(target, source, amount);
         }

         return Math.max(amount, 0.0F);
      } else {
         return 0.0;
      }
   }

   private static float init_F_LivingEntity(LivingEntity target, DamageSource source, float amount) {
      if (amount <= 0.0F) {
         return 0.0F;
      } else {
         ItemStack blocking = target.getItemBlockingWith();
         if (blocking == null) {
            return 0.0F;
         } else {
            BlocksAttacks blocksAttacks = blocking.get(DataComponents.BLOCKS_ATTACKS);
            if (blocksAttacks == null) {
               return 0.0F;
            } else if (blocksAttacks.bypassedBy().isPresent() && blocksAttacks.bypassedBy().get().contains(source.typeHolder())) {
               return 0.0F;
            } else if (source.getDirectEntity() instanceof AbstractArrow arrow && arrow.getPierceLevel() > 0) {
               return 0.0F;
            } else {
               double horizontalAngle = 3.141592653589793;
               Vec3 sourcePosition = source.getSourcePosition();
               if (sourcePosition != null) {
                  float yaw = target.getYHeadRot() * 0.017453292F;
                  Vec3 view = new Vec3(-Mth.sin(yaw), 0.0, Mth.cos(yaw));
                  Vec3 direction = sourcePosition.subtract(target.position());
                  direction = new Vec3(direction.x, 0.0, direction.z).normalize();
                  horizontalAngle = Math.acos(direction.dot(view));
               }

               return blocksAttacks.resolveBlockedDamage(source, amount, horizontalAngle);
            }
         }
      }
   }

   private static float load(LivingEntity target, DamageSource source, float amount) {
      if (amount <= 0.0F) {
         return amount;
      }

      if (source.is(DamageTypeTags.BYPASSES_ENCHANTMENTS)) {
         return amount;
      }

      if (target.level() instanceof ServerLevel) {
         return amount;
      }

      float points = init_F_LivingEntity_DamageSource(target, source);
      return points <= 0.0F ? amount : amount * (1.0F - Mth.clamp(points, 0.0F, 20.0F) / 25.0F);
   }

   private static float init_F_LivingEntity_DamageSource(LivingEntity target, DamageSource source) {
      boolean explosion = source.is(DamageTypeTags.IS_EXPLOSION);
      boolean generic = !source.is(DamageTypeTags.BYPASSES_INVULNERABILITY);
      boolean fall = generic && source.is(DamageTypeTags.IS_FALL);
      if (!explosion && !generic) {
         return 0.0F;
      }

      float points = 0.0F;

      for (EquipmentSlot slot : init_EquipmentSlotArr) {
         ItemStack stack = target.getItemBySlot(slot);
         if (!stack.isEmpty()) {
            ItemEnchantments enchantments = stack.getEnchantments();
            if (!enchantments.isEmpty()) {
               for (Holder<Enchantment> holder : enchantments.keySet()) {
                  int level = enchantments.getLevel(holder);
                  if (level > 0) {
                     if (generic && holder.is(Enchantments.PROTECTION)) {
                        points += level;
                     }

                     if (explosion && holder.is(Enchantments.BLAST_PROTECTION)) {
                        points += level * 2.0F;
                     }

                     if (fall && holder.is(Enchantments.FEATHER_FALLING)) {
                        points += level * 3.0F;
                     }
                  }
               }
            }
         }
      }

      return points;
   }

   public static DamageSource init_DamageSource_Level_Vec3(Level level, Vec3 explosionPos) {
      return new DamageSource(level.damageSources().explosion((Explosion)null).typeHolder(), explosionPos);
   }

   public static DamageSource load(Level level, Vec3 explosionPos) {
      return level.damageSources().badRespawnPointExplosion(explosionPos);
   }

   private static boolean init_Z_LivingEntity(LivingEntity target) {
      if (!target.isRemoved() && !target.isDeadOrDying() && !target.isSpectator()) {
         return target instanceof Player player && player.getAbilities().invulnerable ? true : target.isInvulnerable();
      } else {
         return true;
      }
   }

   public record Inner_BufferHelper(double init, double load) {
      public double init_D() {
         return this.init - this.load;
      }

      public boolean init_Z() {
         return this.init > this.load;
      }

      public boolean init(double minTargetDamage, double maxSelfDamage) {
         return this.init >= minTargetDamage && this.load <= maxSelfDamage;
      }

      public double load() {
         return this.init;
      }

      public double read() {
         return this.load;
      }
   }

   public record Inner_CachePool(
      List<BlockPos> init_List, BlockPos init_BlockPos, Float init_Float, AABB init_AABB, DamageSource init_DamageSource, boolean init_Z
   ) {
      public static final S.Inner_CachePool init_S_CachePool = new S.Inner_CachePool(List.of(), null, null, null, null, true);

      public Inner_CachePool {
         init_List = init_List == null ? List.of() : List.copyOf(init_List);
      }

      public S.Inner_CachePool init(List<BlockPos> positions) {
         return new S.Inner_CachePool(positions, this.init_BlockPos, this.init_Float, this.init_AABB, this.init_DamageSource, this.init_Z);
      }

      public S.Inner_CachePool init(BlockPos pos) {
         return new S.Inner_CachePool(this.init_List, pos, this.init_Float, this.init_AABB, this.init_DamageSource, this.init_Z);
      }

      public S.Inner_CachePool init(Float resistance) {
         return new S.Inner_CachePool(this.init_List, this.init_BlockPos, resistance, this.init_AABB, this.init_DamageSource, this.init_Z);
      }

      public S.Inner_CachePool init(boolean terrain) {
         return this.init(terrain ? 9.0F : null);
      }

      public S.Inner_CachePool init(AABB box) {
         return new S.Inner_CachePool(this.init_List, this.init_BlockPos, this.init_Float, box, this.init_DamageSource, this.init_Z);
      }

      public S.Inner_CachePool init(DamageSource source) {
         return new S.Inner_CachePool(this.init_List, this.init_BlockPos, this.init_Float, this.init_AABB, source, this.init_Z);
      }

      public S.Inner_CachePool load(boolean estimate) {
         return new S.Inner_CachePool(this.init_List, this.init_BlockPos, this.init_Float, this.init_AABB, this.init_DamageSource, estimate);
      }

      public boolean init_Z() {
         return !this.init_List.isEmpty() || this.init_BlockPos != null || this.init_Float != null || this.init_AABB != null;
      }

      public boolean load() {
         return this.init_Z;
      }
   }

   public static final class Inner_ConfigStore implements AutoCloseable {
      private final long init;

      private Inner_ConfigStore(long id) {
         this.init = id;
      }

      @Override
      public void close() {
         S.init_V_J(this.init);
      }
   }

   private record Inner_DataUtil(LivingEntity init_LivingEntity, AABB init_AABB, Vec3 init_Vec3, float init_F, S.Inner_CachePool init_S_CachePool, long init_J) {
   }

   public record Inner_StreamUtil(boolean init_Z, boolean load_Z, double init_D, double load_D) {
      public boolean init_Z() {
         return this.init_Z && !this.load_Z;
      }

      public boolean load_Z() {
         return this.init_Z;
      }

      public boolean read() {
         return this.load_Z;
      }
   }
}

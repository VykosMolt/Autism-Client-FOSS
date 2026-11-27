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

public final class ExplosionDamageCalculator {
   public static final float CRYSTAL_EXPLOSION_POWER = 6.0F;
   public static final float ANCHOR_EXPLOSION_POWER = 5.0F;
   public static final float BED_EXPLOSION_POWER = 5.0F;
   public static final float TERRAIN_BLAST_RESISTANCE = 9.0F;
   private static final EquipmentSlot[] ARMOR_SLOTS = new EquipmentSlot[]{
      EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET
   };
   private static final int SAMPLE_CACHE_CAPACITY = 256;
   private static final long NO_ACTIVE_PASS = -1L;
   private static final Object CACHE_LOCK = new Object();
   private static final Map<ExplosionDamageCalculator.EntitySample, Double> DAMAGE_CACHE = new LinkedHashMap<ExplosionDamageCalculator.EntitySample, Double>(256, 1.0F, true) {
      @Override
      protected boolean removeEldestEntry(Entry<ExplosionDamageCalculator.EntitySample, Double> eldest) {
         return this.size() > 256;
      }
   };
   private static final AtomicLong PASS_SEQUENCE = new AtomicLong();
   private static long activePassId = -1L;
   private static long cacheGeneration = -1L;
   private static WeakReference<Level> cachedLevelRef = new WeakReference<>(null);
   private static int cachedTick = -2147483648;
   public static final int HALF_INVULNERABLE_TICKS = 10;
   public static final double SELF_LETHAL_MULTIPLIER = 1.15;
   public static final double SELF_LETHAL_HEALTH_BUFFER = 2.0;
   public static final double TARGET_LETHAL_MULTIPLIER = 1.0;

   private ExplosionDamageCalculator() {
   }

   public static double calculateDamage(LivingEntity target, Vec3 explosionPos, float power) {
      return calculateDamage(target, explosionPos, power, ExplosionDamageCalculator.SimOptions.DEFAULT);
   }

   public static double calculateDamage(LivingEntity target, Vec3 explosionPos, float power, ExplosionDamageCalculator.SimOptions options) {
      return calculateDamageInternal(target, explosionPos, power, options == null ? ExplosionDamageCalculator.SimOptions.DEFAULT : options, true);
   }

   private static double calculateDamageInternal(LivingEntity target, Vec3 explosionPos, float power, ExplosionDamageCalculator.SimOptions options, boolean sampleExposure) {
      if (target != null && explosionPos != null && power > 0.0F) {
         Level level = target.level();
         if (level != null && !isEntityImmune(target)) {
            float range = power * 2.0F;
            double distanceSqr = distanceToExplosionSqr(target, explosionPos, options);
            if (distanceSqr > (double)range * range) {
               return 0.0;
            }

            double decay = 1.0 - Math.sqrt(distanceSqr) / range;
            double seen = (sampleExposure ? sampleExposure(target, explosionPos, options) : 1.0F) * decay;
            double raw = (seen * seen + seen) / 2.0 * 7.0 * range + 1.0;
            if (raw <= 0.0) {
               return 0.0;
            }

            DamageSource source = options.damageSource() != null ? options.damageSource() : explosionDamageSource(level, explosionPos);
            return applyMitigations(target, source, (float)raw, options.estimateProtection());
         } else {
            return 0.0;
         }
      } else {
         return 0.0;
      }
   }

   private static double distanceToExplosionSqr(LivingEntity target, Vec3 explosionPos, ExplosionDamageCalculator.SimOptions options) {
      AABB box = options.targetBoxOverride();
      if (box == null) {
         return target.distanceToSqr(explosionPos);
      }

      double dx = (box.minX + box.maxX) / 2.0 - explosionPos.x;
      double dy = box.minY - explosionPos.y;
      double dz = (box.minZ + box.maxZ) / 2.0 - explosionPos.z;
      return dx * dx + dy * dy + dz * dz;
   }

   public static double calculateSelfDamage(Vec3 explosionPos, float power) {
      return calculateSelfDamage(explosionPos, power, ExplosionDamageCalculator.SimOptions.DEFAULT);
   }

   public static double calculateSelfDamage(Vec3 explosionPos, float power, ExplosionDamageCalculator.SimOptions options) {
      return calculateDamage(Minecraft.getInstance().player, explosionPos, power, options);
   }

   public static boolean isLethal(LivingEntity target, Vec3 explosionPos, float power) {
      return isLethal(target, explosionPos, power, ExplosionDamageCalculator.SimOptions.DEFAULT);
   }

   public static boolean isLethal(LivingEntity target, Vec3 explosionPos, float power, ExplosionDamageCalculator.SimOptions options) {
      double health = effectiveHealth(target);
      return health <= 0.0 ? false : calculateDamage(target, explosionPos, power, options) >= health;
   }

   public static boolean isLethalToSelf(Vec3 explosionPos, float power) {
      return isLethal(Minecraft.getInstance().player, explosionPos, power, ExplosionDamageCalculator.SimOptions.DEFAULT);
   }

   public static double effectiveHealth(LivingEntity target) {
      return target != null && !target.isDeadOrDying() ? (double)target.getHealth() + target.getAbsorptionAmount() : 0.0;
   }

   public static boolean isInHurtCooldown(LivingEntity target, DamageSource source) {
      if (target == null) {
         return false;
      } else {
         return source != null && source.is(DamageTypeTags.BYPASSES_COOLDOWN) ? false : target.getInvulnerableTime() > 10;
      }
   }

   public static boolean willKillTarget(LivingEntity target, Vec3 explosionPos, float power, ExplosionDamageCalculator.SimOptions options) {
      return willDamageKillTarget(target, calculateDamage(target, explosionPos, power, options), options);
   }

   public static boolean willDamageKillTarget(LivingEntity target, double predictedDamage, ExplosionDamageCalculator.SimOptions options) {
      if (target != null && predictedDamage > 0.0) {
         double health = effectiveHealth(target);
         if (health <= 0.0) {
            return false;
         } else {
            return isInHurtCooldown(target, options == null ? null : options.damageSource()) ? false : predictedDamage * 1.0 >= health;
         }
      } else {
         return false;
      }
   }

   public static boolean wouldPopOrKillSelf(Vec3 explosionPos, float power, ExplosionDamageCalculator.SimOptions options) {
      return wouldSelfDamagePopOrKill(calculateSelfDamage(explosionPos, power, options));
   }

   public static boolean wouldSelfDamagePopOrKill(double predictedSelfDamage) {
      LocalPlayer player = Minecraft.getInstance().player;
      double health = effectiveHealth(player);
      return health <= 0.0 ? false : predictedSelfDamage * 1.15 + 2.0 >= health;
   }

   public static boolean wouldSelfDamageKillWithoutTotem(double predictedSelfDamage) {
      return !wouldSelfDamagePopOrKill(predictedSelfDamage) ? false : !isPlayerHoldingTotem();
   }

   private static boolean isPlayerHoldingTotem() {
      LocalPlayer player = Minecraft.getInstance().player;
      return player == null ? false : player.getMainHandItem().is(Items.TOTEM_OF_UNDYING) || player.getOffhandItem().is(Items.TOTEM_OF_UNDYING);
   }

   public static ExplosionDamageCalculator.SafetyResult evaluateSafety(LivingEntity target, Vec3 explosionPos, float power, ExplosionDamageCalculator.SimOptions options) {
      return evaluateSafety(calculateDamagePair(target, explosionPos, power, options), target, options);
   }

   public static ExplosionDamageCalculator.SafetyResult evaluateSafetyCached(LivingEntity target, Vec3 explosionPos, float power, ExplosionDamageCalculator.SimOptions options) {
      return evaluateSafety(calculateDamagePairCached(target, explosionPos, power, options), target, options);
   }

   public static ExplosionDamageCalculator.SafetyResult evaluateSafety(ExplosionDamageCalculator.DamagePair ranking, LivingEntity target, ExplosionDamageCalculator.SimOptions options) {
      return ranking == null
         ? new ExplosionDamageCalculator.SafetyResult(false, false, 0.0, 0.0)
         : new ExplosionDamageCalculator.SafetyResult(willDamageKillTarget(target, ranking.load(), options), wouldSelfDamagePopOrKill(ranking.selfDamage()), ranking.load(), ranking.selfDamage());
   }

   public static ExplosionDamageCalculator.DamagePair calculateDamagePair(LivingEntity target, Vec3 explosionPos, float power) {
      return calculateDamagePair(target, explosionPos, power, ExplosionDamageCalculator.SimOptions.DEFAULT);
   }

   public static ExplosionDamageCalculator.DamagePair calculateDamagePair(LivingEntity target, Vec3 explosionPos, float power, ExplosionDamageCalculator.SimOptions options) {
      double targetDamage = calculateDamage(target, explosionPos, power, options);
      LocalPlayer player = Minecraft.getInstance().player;
      double self = target == player ? targetDamage : calculateDamage(player, explosionPos, power, options);
      return new ExplosionDamageCalculator.DamagePair(targetDamage, self);
   }

   public static double calculateUpperBoundDamage(LivingEntity target, Vec3 explosionPos, float power) {
      return calculateUpperBoundDamage(target, explosionPos, power, ExplosionDamageCalculator.SimOptions.DEFAULT);
   }

   public static double calculateUpperBoundDamage(LivingEntity target, Vec3 explosionPos, float power, ExplosionDamageCalculator.SimOptions options) {
      return calculateDamageInternal(target, explosionPos, power, options == null ? ExplosionDamageCalculator.SimOptions.DEFAULT : options, false);
   }

   public static double calculateUpperBoundSelfDamage(Vec3 explosionPos, float power, ExplosionDamageCalculator.SimOptions options) {
      return calculateUpperBoundDamage(Minecraft.getInstance().player, explosionPos, power, options);
   }

   public static ExplosionDamageCalculator.SimScope beginSimScope() {
      Minecraft mc = Minecraft.getInstance();
      Level level = mc.level;
      int tick = currentClientTick(mc);
      synchronized (CACHE_LOCK) {
         activePassId = PASS_SEQUENCE.incrementAndGet();
         resetCacheForTick(level, tick);
         return new ExplosionDamageCalculator.SimScope(activePassId);
      }
   }

   private static void endSimScope(long passId) {
      synchronized (CACHE_LOCK) {
         if (activePassId == passId) {
            DAMAGE_CACHE.clear();
            PASS_SEQUENCE.incrementAndGet();
            activePassId = -1L;
            cacheGeneration = -1L;
            cachedLevelRef = new WeakReference<>(null);
            cachedTick = -2147483648;
         }
      }
   }

   public static double calculateDamageCached(LivingEntity target, Vec3 explosionPos, float power, ExplosionDamageCalculator.SimOptions options) {
      if (target != null && explosionPos != null && power > 0.0F) {
         ExplosionDamageCalculator.SimOptions opts = options == null ? ExplosionDamageCalculator.SimOptions.DEFAULT : options;
         Minecraft mc = Minecraft.getInstance();
         Level level = mc.level;
         long generation;
         synchronized (CACHE_LOCK) {
            if (activePassId == -1L) {
               generation = -1L;
            } else {
               int tick = currentClientTick(mc);
               if (cachedLevelRef.get() != level || cachedTick != tick) {
                  resetCacheForTick(level, tick);
               }

               generation = cacheGeneration;
            }
         }

         if (generation == -1L) {
            return calculateDamage(target, explosionPos, power, opts);
         }

         ExplosionDamageCalculator.EntitySample key = new ExplosionDamageCalculator.EntitySample(target, target.getBoundingBox(), explosionPos, power, opts, hashEntityDefenseState(target));
         synchronized (CACHE_LOCK) {
            if (cacheGeneration != generation) {
               return calculateDamage(target, explosionPos, power, opts);
            }

            Double cached = DAMAGE_CACHE.get(key);
            if (cached != null) {
               return cached;
            }
         }

         double damage = calculateDamage(target, explosionPos, power, opts);
         boolean moved = didEntityStateChange(key, target);
         synchronized (CACHE_LOCK) {
            if (cacheGeneration != generation) {
               return damage;
            }

            if (cachedLevelRef.get() == level && cachedTick == currentClientTick(mc)) {
               if (!moved) {
                  DAMAGE_CACHE.put(key, damage);
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

   private static int currentClientTick(Minecraft mc) {
      return mc.player == null ? -2147483648 : AutismSessionState.getInstance().getClientTick();
   }

   private static void resetCacheForTick(Level level, int tick) {
      DAMAGE_CACHE.clear();
      cacheGeneration = PASS_SEQUENCE.incrementAndGet();
      cachedLevelRef = new WeakReference<>(level);
      cachedTick = tick;
   }

   public static double calculateSelfDamageCached(Vec3 explosionPos, float power, ExplosionDamageCalculator.SimOptions options) {
      return calculateDamageCached(Minecraft.getInstance().player, explosionPos, power, options);
   }

   public static ExplosionDamageCalculator.DamagePair calculateDamagePairCached(LivingEntity target, Vec3 explosionPos, float power, ExplosionDamageCalculator.SimOptions options) {
      double targetDamage = calculateDamageCached(target, explosionPos, power, options);
      LocalPlayer player = Minecraft.getInstance().player;
      double self = target == player ? targetDamage : calculateDamageCached(player, explosionPos, power, options);
      return new ExplosionDamageCalculator.DamagePair(targetDamage, self);
   }

   public static void invalidateCache() {
      synchronized (CACHE_LOCK) {
         DAMAGE_CACHE.clear();
         long generation = PASS_SEQUENCE.incrementAndGet();
         if (activePassId != -1L) {
            cacheGeneration = generation;
         }
      }
   }

   private static boolean didEntityStateChange(ExplosionDamageCalculator.EntitySample key, LivingEntity target) {
      return !key.boundingBox().equals(target.getBoundingBox()) ? true : key.stateHash() != hashEntityDefenseState(target);
   }

   private static long hashEntityDefenseState(LivingEntity target) {
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
      hash = hash * 31L + hashItemStack(target.getItemBlockingWith());

      for (EquipmentSlot slot : ARMOR_SLOTS) {
         hash = hash * 31L + hashItemStack(target.getItemBySlot(slot));
      }

      return hash;
   }

   private static long hashItemStack(ItemStack stack) {
      if (stack != null && !stack.isEmpty()) {
         long hash = stack.getItem().hashCode();
         hash = hash * 31L + stack.getDamageValue();
         ItemEnchantments enchantments = stack.getEnchantments();
         return hash * 31L + (enchantments == null ? 0L : enchantments.hashCode());
      } else {
         return 0L;
      }
   }

   public static float sampleExposure(LivingEntity target, Vec3 explosionPos, ExplosionDamageCalculator.SimOptions options) {
      if (target != null && explosionPos != null) {
         Level level = target.level();
         if (level == null) {
            return 0.0F;
         }

         ExplosionDamageCalculator.SimOptions opts = options == null ? ExplosionDamageCalculator.SimOptions.DEFAULT : options;
         if (!opts.init_Z()) {
            return ServerExplosion.getSeenPercent(explosionPos, target);
         }

         AABB box = opts.targetBoxOverride() != null ? opts.targetBoxOverride() : target.getBoundingBox();
         CollisionContext shapeContext = opts.targetBoxOverride() != null ? CollisionContext.withPosition(target, box.minY) : CollisionContext.of(target);
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
                     if (clipWithOverrides(level, clip, opts).getType() == Type.MISS) {
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

   private static BlockHitResult clipWithOverrides(Level level, ClipContext context, ExplosionDamageCalculator.SimOptions options) {
      List<BlockPos> exclude = options.excludedBlocks();
      BlockPos include = options.placedObsidian();
      Float maxBlastResistance = options.maxBlastResistance();
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

   public static double applyMitigations(LivingEntity target, DamageSource source, float damage, boolean estimateProtection) {
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

         amount -= computeBlockedDamage(target, source, amount);
         if (amount <= 0.0F) {
            return 0.0;
         }

         AutismLivingEntityAccessor accessor = (AutismLivingEntityAccessor)target;
         amount = accessor.autism$getDamageAfterArmorAbsorb(source, amount);
         amount = accessor.autism$getDamageAfterMagicAbsorb(source, amount);
         if (estimateProtection) {
            amount = applyProtectionEstimate(target, source, amount);
         }

         return Math.max(amount, 0.0F);
      } else {
         return 0.0;
      }
   }

   private static float computeBlockedDamage(LivingEntity target, DamageSource source, float amount) {
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

   private static float applyProtectionEstimate(LivingEntity target, DamageSource source, float amount) {
      if (amount <= 0.0F) {
         return amount;
      }

      if (source.is(DamageTypeTags.BYPASSES_ENCHANTMENTS)) {
         return amount;
      }

      if (target.level() instanceof ServerLevel) {
         return amount;
      }

      float points = sumProtectionPoints(target, source);
      return points <= 0.0F ? amount : amount * (1.0F - Mth.clamp(points, 0.0F, 20.0F) / 25.0F);
   }

   private static float sumProtectionPoints(LivingEntity target, DamageSource source) {
      boolean explosion = source.is(DamageTypeTags.IS_EXPLOSION);
      boolean generic = !source.is(DamageTypeTags.BYPASSES_INVULNERABILITY);
      boolean fall = generic && source.is(DamageTypeTags.IS_FALL);
      if (!explosion && !generic) {
         return 0.0F;
      }

      float points = 0.0F;

      for (EquipmentSlot slot : ARMOR_SLOTS) {
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

   public static DamageSource explosionDamageSource(Level level, Vec3 explosionPos) {
      return new DamageSource(level.damageSources().explosion((Explosion)null).typeHolder(), explosionPos);
   }

   public static DamageSource bedAnchorDamageSource(Level level, Vec3 explosionPos) {
      return level.damageSources().badRespawnPointExplosion(explosionPos);
   }

   private static boolean isEntityImmune(LivingEntity target) {
      if (!target.isRemoved() && !target.isDeadOrDying() && !target.isSpectator()) {
         return target instanceof Player player && player.getAbilities().invulnerable ? true : target.isInvulnerable();
      } else {
         return true;
      }
   }

   public record DamagePair(double init, double load) {
      public double netAdvantage() {
         return this.init - this.load;
      }

      public boolean favorsTarget() {
         return this.init > this.load;
      }

      public boolean meetsThresholds(double minTargetDamage, double maxSelfDamage) {
         return this.init >= minTargetDamage && this.load <= maxSelfDamage;
      }

      public double load() {
         return this.init;
      }

      public double selfDamage() {
         return this.load;
      }
   }

   public record SimOptions(
      List<BlockPos> excludedBlocks, BlockPos placedObsidian, Float maxBlastResistance, AABB targetBoxOverride, DamageSource damageSource, boolean init_Z
   ) {
      public static final ExplosionDamageCalculator.SimOptions DEFAULT = new ExplosionDamageCalculator.SimOptions(List.of(), null, null, null, null, true);

      public SimOptions {
         excludedBlocks = excludedBlocks == null ? List.of() : List.copyOf(excludedBlocks);
      }

      public ExplosionDamageCalculator.SimOptions withExcludedBlocks(List<BlockPos> positions) {
         return new ExplosionDamageCalculator.SimOptions(positions, this.placedObsidian, this.maxBlastResistance, this.targetBoxOverride, this.damageSource, this.init_Z);
      }

      public ExplosionDamageCalculator.SimOptions withPlacedObsidian(BlockPos pos) {
         return new ExplosionDamageCalculator.SimOptions(this.excludedBlocks, pos, this.maxBlastResistance, this.targetBoxOverride, this.damageSource, this.init_Z);
      }

      public ExplosionDamageCalculator.SimOptions withMaxBlastResistance(Float resistance) {
         return new ExplosionDamageCalculator.SimOptions(this.excludedBlocks, this.placedObsidian, resistance, this.targetBoxOverride, this.damageSource, this.init_Z);
      }

      public ExplosionDamageCalculator.SimOptions withIgnoreTerrain(boolean terrain) {
         return this.withMaxBlastResistance(terrain ? 9.0F : null);
      }

      public ExplosionDamageCalculator.SimOptions withTargetBox(AABB box) {
         return new ExplosionDamageCalculator.SimOptions(this.excludedBlocks, this.placedObsidian, this.maxBlastResistance, box, this.damageSource, this.init_Z);
      }

      public ExplosionDamageCalculator.SimOptions withDamageSource(DamageSource source) {
         return new ExplosionDamageCalculator.SimOptions(this.excludedBlocks, this.placedObsidian, this.maxBlastResistance, this.targetBoxOverride, source, this.init_Z);
      }

      public ExplosionDamageCalculator.SimOptions withEstimateProtection(boolean estimate) {
         return new ExplosionDamageCalculator.SimOptions(this.excludedBlocks, this.placedObsidian, this.maxBlastResistance, this.targetBoxOverride, this.damageSource, estimate);
      }

      public boolean init_Z() {
         return !this.excludedBlocks.isEmpty() || this.placedObsidian != null || this.maxBlastResistance != null || this.targetBoxOverride != null;
      }

      public boolean estimateProtection() {
         return this.init_Z;
      }
   }

   public static final class SimScope implements AutoCloseable {
      private final long passId;

      private SimScope(long id) {
         this.passId = id;
      }

      @Override
      public void close() {
         ExplosionDamageCalculator.endSimScope(this.passId);
      }
   }

   private record EntitySample(LivingEntity entity, AABB boundingBox, Vec3 explosionPos, float power, ExplosionDamageCalculator.SimOptions options, long stateHash) {
   }

   public record SafetyResult(boolean init_Z, boolean load_Z, double targetDamage, double selfDamage) {
      public boolean init_Z() {
         return this.init_Z && !this.load_Z;
      }

      public boolean load_Z() {
         return this.init_Z;
      }

      public boolean popsOrKillsSelf() {
         return this.load_Z;
      }
   }
}

package autismclient.ai;

import autismclient.api.ai.AiOperation;
import autismclient.mixin.accessor.AutismEntityAccessor;
import autismclient.mixin.accessor.AutismLivingEntityAccessor;
import autismclient.modules.Module;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.BlockPos.MutableBlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.Mth;
import net.minecraft.world.Difficulty;
import net.minecraft.world.attribute.EnvironmentAttributes;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.boss.enderdragon.EndCrystal;
import net.minecraft.world.entity.item.PrimedTnt;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.entity.projectile.arrow.AbstractArrow;
import net.minecraft.world.entity.vehicle.minecart.MinecartTNT;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.BlocksAttacks;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.block.BedBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.RespawnAnchorBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BedPart;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

final class AiDanger {
   static final String END_CRYSTAL = "end_crystal";
   static final String PRIMED_TNT = "primed_tnt";
   static final String TNT_MINECART = "tnt_minecart";
   static final String CREEPER = "creeper";
   static final String BED = "bed";
   static final String RESPAWN_ANCHOR = "respawn_anchor";
   static final int init_I = 12;
   static final int load_I = 2;
   static final int read_I = 20;
   static final double init_D = -0.08;
   static final double load_D = 0.01;
   static final int write_I = 30;
   static final double read_D = 0.91;
   static final double write_D = 0.98;
   private static final List<BlockPos> BLOCK_SCAN_OFFSETS = sphereOffsets(10.0F);
   static final String PREDICTION_NOTE = "predictions from vanilla ballistics/explosion math at client state; server resolves authoritatively";
   private static final AiDanger.Assessment assess = new AiDanger.Assessment(
      List.of(), 0.0F, 0.0F, 0.0F, 0, 0, 0, 0.0, -1, false, 0.0F, "", 0.0F, 0.0F, false, false, false
   );
   private static final AiDanger.DangerSnapshot EMPTY_SNAPSHOT = new AiDanger.DangerSnapshot(assess, -1, null);
   private static AiDanger.DangerSnapshot cachedSnapshot;
   private static int cachedSnapshotTick = -2;

   private AiDanger() {
   }

   static float explosionPower(String kind, boolean creeperPowered, double minecartHorizontalSpeed) {
      float var10000;
      switch (kind) {
         case "end_crystal":
            var10000 = 6.0F;
            break;
         case "primed_tnt":
            var10000 = 4.0F;
            break;
         case "tnt_minecart":
            var10000 = 4.0F + (float)Math.min(Math.max(minecartHorizontalSpeed, 0.0), 5.0) * 1.5F;
            break;
         case "creeper":
            var10000 = creeperPowered ? 6.0F : 3.0F;
            break;
         case "bed":
            var10000 = 5.0F;
            break;
         case "respawn_anchor":
            var10000 = 5.0F;
            break;
         case "large_fireball":
         case "wither_skull":
            var10000 = 1.0F;
            break;
         default:
            var10000 = 0.0F;
      }

      return var10000;
   }

   static boolean bedExplodes(boolean bedRuleExplodes) {
      return bedRuleExplodes;
   }

   static boolean anchorExplodes(boolean respawnAnchorWorks, int charge) {
      return !respawnAnchorWorks && charge > 0;
   }

   static float fallDamageMultiplier(String landingBlockId, boolean suppressingBounce) {
      if (landingBlockId != null && !landingBlockId.isEmpty()) {
         float var10000;
         switch (landingBlockId) {
            case "water":
            case "cobweb":
            case "powder_snow":
               var10000 = 0.0F;
               break;
            case "hay_block":
            case "honey_block":
               var10000 = 0.2F;
               break;
            case "slime_block":
               var10000 = suppressingBounce ? 1.0F : 0.0F;
               break;
            default:
               var10000 = landingBlockId.endsWith("_bed") ? 0.5F : 1.0F;
         }

         return var10000;
      } else {
         return 1.0F;
      }
   }

   static int tntEtaTicks(int fuseTicks) {
      return Math.max(0, fuseTicks);
   }

   static int creeperEtaTicks(float swelling) {
      float clamped = Math.max(0.0F, Math.min(1.0F, swelling));
      return Math.round((1.0F - clamped) * 30.0F);
   }

   static double nextMotionY(double motionY, double gravity, boolean slowFalling) {
      double my = motionY - (motionY <= 0.0 && slowFalling ? Math.min(gravity, 0.01) : gravity);
      return my * 0.98;
   }

   static float worstSourceDamage(List<AiDanger.ThreatSource> sources) {
      float worst = 0.0F;

      for (AiDanger.ThreatSource s : sources) {
         worst = Math.max(worst, s.damage());
      }

      return worst;
   }

   static float totalPredictedDamage(float worstSource, float fallDamage) {
      return Math.max(0.0F, worstSource) + Math.max(0.0F, fallDamage);
   }

   static AiDanger.Verdict verdict(float totalPredicted, AiDanger.TotemEvaluator evaluator) {
      return new AiDanger.Verdict(evaluator.popsOrKills(totalPredicted), evaluator.killsWithoutTotem(totalPredicted));
   }

   static AiDanger.Verdict verdict(float totalPredicted, boolean voidFall, AiDanger.TotemEvaluator evaluator) {
      return voidFall ? new AiDanger.Verdict(true, true) : verdict(totalPredicted, evaluator);
   }

   static List<AiDanger.ThreatSource> topSources(List<AiDanger.ThreatSource> sources, int cap) {
      int limit = Math.max(0, Math.min(12, cap));
      List<AiDanger.ThreatSource> sorted = new ArrayList<>(sources);
      sorted.sort(
         Comparator.<AiDanger.ThreatSource>comparingInt(s -> s.etaTicks() < 0 ? 2147483647 : s.etaTicks())
            .thenComparing(Comparator.comparingDouble(AiDanger.ThreatSource::damage).reversed())
      );
      return List.copyOf(sorted.size() <= limit ? sorted : sorted.subList(0, limit));
   }

   static JsonArray sourcesJson(List<AiDanger.ThreatSource> sources) {
      JsonArray out = new JsonArray();

      for (AiDanger.ThreatSource s : sources) {
         out.add(AiJson.objectOf("kind", s.kind(), "x", s.x(), "y", s.y(), "z", s.z(), "damage", s.damage(), "etaTicks", s.etaTicks()));
      }

      return out;
   }

   static AiDanger.Assessment assess(Minecraft mc) {
      try {
         return snapshot(mc).assessment();
      } catch (Throwable t) {
         return assess;
      }
   }

   static JsonObject snapshotJson(Minecraft mc, int cap) {
      try {
         AiDanger.DangerSnapshot snap = snapshot(mc);
         AiDanger.Assessment a = snap.assessment();
         return AiJson.objectOf(
            "sources",
            sourcesJson(topSources(a.sources(), cap)),
            "worstSource",
            a.worstSource(),
            "fallDamage",
            a.fallDamage(),
            "fallEtaTicks",
            snap.fallEtaTicks(),
            "totalPredicted",
            a.totalPredicted(),
            "totemsHeld",
            a.totemsHeld(),
            "totemsInventory",
            a.totemsInventory(),
            "armorPoints",
            a.armorPoints(),
            "toughness",
            a.toughness(),
            "resistanceAmplifier",
            a.resistanceAmplifier(),
            "fireResistant",
            a.fireResistant(),
            "absorption",
            a.absorption(),
            "blockingItem",
            a.blockingItem(),
            "effectiveHealth",
            a.effectiveHealth(),
            "afterWorstHit",
            a.afterWorstHit(),
            "lethal",
            a.lethal(),
            "lethalThroughTotem",
            a.lethalThroughTotem(),
            "voidFall",
            a.voidFall(),
            "note",
            "predictions from vanilla ballistics/explosion math at client state; server resolves authoritatively"
         );
      } catch (Throwable t) {
         return AiJson.objectOf(
            "sources", new JsonArray(), "note", "predictions from vanilla ballistics/explosion math at client state; server resolves authoritatively"
         );
      }
   }

   static AiOperation create(JsonObject args) {
      int cap = args == null ? 12 : AiJson.getInt(args, "cap", 12);
      return AiGameOperations.immediate(AiOperation.Status.SUCCEEDED, "Predicted danger assessment", snapshotJson(Minecraft.getInstance(), cap));
   }

   private static AiDanger.DangerSnapshot snapshot(Minecraft mc) {
      if (mc == null) {
         mc = Minecraft.getInstance();
      }

      if (mc.player != null && mc.level != null) {
         int now = autismclient.util.AutismSessionState.getInstance().getClientTick();
         AiDanger.DangerSnapshot cached = cachedSnapshot;
         if (cached != null && cached.player() == mc.player && now - cachedSnapshotTick < 2) {
            return cached;
         }

         List<AiDanger.ThreatSource> sources = new ArrayList<>();
         collectEntitySources(mc, sources);
         collectBlockSources(mc, sources);
         collectProjectileSources(mc, sources);
         AiDanger.FallPrediction fall = predictFall(mc);
         float fallDamage = fall == null ? 0.0F : fall.damage();
         boolean voidFall = fall != null && fall.voidFall();
         AiDanger.DangerSnapshot snap = new AiDanger.DangerSnapshot(assess(mc, sources, fallDamage, voidFall), fall == null ? -1 : fall.etaTicks(), mc.player);
         cachedSnapshot = snap;
         cachedSnapshotTick = now;
         return snap;
      } else {
         return EMPTY_SNAPSHOT;
      }
   }

   private static AiDanger.Assessment assess(Minecraft mc, List<AiDanger.ThreatSource> sources, float fallDamage, boolean voidFall) {
      LocalPlayer player = mc.player;
      float worst = worstSourceDamage(sources);
      float total = totalPredictedDamage(worst, fallDamage);
      int held = (isTotem(player.getMainHandItem()) ? 1 : 0) + (isTotem(player.getOffhandItem()) ? 1 : 0);
      int inventory = 0;

      for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
         ItemStack stack = player.getInventory().getItem(i);
         if (isTotem(stack)) {
            inventory += stack.getCount();
         }
      }

      MobEffectInstance resistance = player.getEffect(MobEffects.RESISTANCE);
      ItemStack blocking = player.getItemBlockingWith();
      float effectiveHealth = (float)autismclient.util.ExplosionDamageCalculator.effectiveHealth(player);
      AiDanger.Verdict verdict = verdict(total, voidFall, AiDanger.TotemEvaluator.VANILLA);
      return new AiDanger.Assessment(
         List.copyOf(sources),
         worst,
         fallDamage,
         total,
         held,
         inventory,
         player.getArmorValue(),
         player.getAttributeValue(Attributes.ARMOR_TOUGHNESS),
         resistance == null ? -1 : resistance.getAmplifier(),
         player.hasEffect(MobEffects.FIRE_RESISTANCE),
         player.getAbsorptionAmount(),
         blocking == null ? "" : blocking.getHoverName().getString(),
         effectiveHealth,
         effectiveHealth - total,
         verdict.lethal(),
         verdict.lethalThroughTotem(),
         voidFall
      );
   }

   private static boolean isTotem(ItemStack stack) {
      return !stack.isEmpty() && stack.has(DataComponents.DEATH_PROTECTION);
   }

   private static void collectEntitySources(Minecraft mc, List<AiDanger.ThreatSource> out) {
      float partialTick = mc.getDeltaTracker().getGameTimeDeltaPartialTick(true);

      for (Entity entity : mc.level.entitiesForRendering()) {
         String kind;
         Vec3 pos;
         float power;
         int eta;
         if (entity instanceof EndCrystal) {
            kind = "end_crystal";
            pos = entity.position();
            power = explosionPower(kind, false, 0.0);
            eta = -1;
         } else if (entity instanceof PrimedTnt tnt) {
            kind = "primed_tnt";
            pos = entity.position().add(0.0, 0.0625, 0.0);
            power = explosionPower(kind, false, 0.0);
            eta = tntEtaTicks(tnt.getFuse());
         } else if (entity instanceof MinecartTNT cart) {
            kind = "tnt_minecart";
            pos = cart.position();
            power = explosionPower(kind, false, Math.sqrt(cart.getDeltaMovement().horizontalDistanceSqr()));
            eta = -1;
         } else {
            if (!(entity instanceof Creeper creeper)) {
               continue;
            }

            kind = "creeper";
            pos = creeper.position();
            power = explosionPower(kind, creeper.isPowered(), 0.0);
            float swell = creeper.getSwelling(partialTick);
            eta = !creeper.isIgnited() && !(swell > 0.001F) ? -1 : creeperEtaTicks(swell);
         }

         float damage = (float)autismclient.util.ExplosionDamageCalculator.calculateSelfDamage(
            pos, power, autismclient.util.ExplosionDamageCalculator.SimOptions.DEFAULT.withDamageSource(Explosion.getDefaultDamageSource(mc.level, entity))
         );
         if (damage > 0.0F) {
            out.add(new AiDanger.ThreatSource(kind, pos.x, pos.y, pos.z, damage, eta));
         }
      }
   }

   private static void collectBlockSources(Minecraft mc, List<AiDanger.ThreatSource> out) {
      boolean bedsExplode = mc.level.environmentAttributes().getDimensionValue(EnvironmentAttributes.BED_RULE).destroyOnUse();
      boolean anchorWorks = mc.level.environmentAttributes().getDimensionValue(EnvironmentAttributes.RESPAWN_ANCHOR_WORKS);
      if (bedsExplode || !anchorWorks) {
         BlockPos center = mc.player.blockPosition();

         for (BlockPos offset : BLOCK_SCAN_OFFSETS) {
            BlockPos pos = center.offset(offset);
            BlockState state = mc.level.getBlockState(pos);
            Block block = state.getBlock();
            String kind = null;
            List<BlockPos> exclude = List.of();
            if (block instanceof BedBlock && state.getValue(BedBlock.PART) == BedPart.HEAD && bedExplodes(bedsExplode)) {
               kind = "bed";
               exclude = List.of(pos, pos.relative(state.getValue(HorizontalDirectionalBlock.FACING).getOpposite()));
            } else if (block instanceof RespawnAnchorBlock && anchorExplodes(anchorWorks, state.getValue(RespawnAnchorBlock.CHARGE))) {
               kind = "respawn_anchor";
               exclude = List.of(pos);
            }

            if (kind != null) {
               Vec3 blast = Vec3.atCenterOf(pos);
               float damage = (float)autismclient.util.ExplosionDamageCalculator.calculateSelfDamage(
                  blast,
                  explosionPower(kind, false, 0.0),
                  autismclient.util.ExplosionDamageCalculator.SimOptions.DEFAULT.withExcludedBlocks(exclude).withDamageSource(autismclient.util.ExplosionDamageCalculator.bedAnchorDamageSource(mc.level, blast))
               );
               if (damage > 0.0F) {
                  out.add(new AiDanger.ThreatSource(kind, blast.x, blast.y, blast.z, damage, -1));
               }
            }
         }
      }
   }

   private static void collectProjectileSources(Minecraft mc, List<AiDanger.ThreatSource> out) {
      for (AiProjectiles.IncomingProjectile t : AiProjectiles.incoming(mc)) {
         String type = t.type();
         String kind = type.indexOf(58) >= 0 ? type.substring(type.indexOf(58) + 1) : type;
         float power = explosionPower(kind, false, 0.0);
         if (!(power <= 0.0F) && !(t.secondsToClosest() < 0.0)) {
            double ticks = t.secondsToClosest() * 20.0;
            Vec3 impact = new Vec3(t.x() + t.vx() * ticks, t.y() + t.vy() * ticks, t.z() + t.vz() * ticks);
            float damage = (float)autismclient.util.ExplosionDamageCalculator.calculateSelfDamage(impact, power);
            if (damage > 0.0F) {
               out.add(new AiDanger.ThreatSource(kind, impact.x, impact.y, impact.z, damage, (int)Math.round(ticks)));
            }
         }
      }
   }

   private static AiDanger.FallPrediction predictFall(Minecraft mc) {
      LocalPlayer player = mc.player;
      Vec3 delta = player.getDeltaMovement();
      if (player.fallDistance <= 3.0 && delta.y >= -0.08) {
         return null;
      }

      if (player.isFallFlying()) {
         return null;
      }

      if (player.isInWater()) {
         return null;
      }

      Module noFall = autismclient.modules.al.init_Module_Str("no-fall");
      if (noFall != null && noFall.isEnabled()) {
         return null;
      }

      double x = player.getX();
      double y = player.getY();
      double z = player.getZ();
      double mx = delta.x;
      double my = delta.y;
      double mz = delta.z;
      double gravity = player.getGravity();
      boolean slowFalling = player.hasEffect(MobEffects.SLOW_FALLING);
      double fall = player.fallDistance;

      for (int tick = 1; tick <= 20; tick++) {
         Vec3 start = new Vec3(x, y, z);
         my = nextMotionY(my, gravity, slowFalling);
         mx *= 0.91;
         mz *= 0.91;
         if (my < 0.0) {
            fall -= my;
         }

         x += mx;
         y += my;
         z += mz;
         AABB swept = player.getDimensions(player.getPose()).makeBoundingBox(start).expandTowards(new Vec3(x, y, z).subtract(start));
         if (sweptTouchesWater(mc, swept)) {
            return new AiDanger.FallPrediction(0.0F, tick, false);
         }

         Optional<BlockPos> support = mc.level.findSupportingBlock(player, swept);
         if (support.isPresent()) {
            String id = BuiltInRegistries.BLOCK.getKey(mc.level.getBlockState(support.get()).getBlock()).getPath();
            float multiplier = fallDamageMultiplier(id, player.isSuppressingBounce());
            if (multiplier <= 0.0F) {
               return new AiDanger.FallPrediction(0.0F, tick, false);
            }

            float amount = ((AutismLivingEntityAccessor)player).autism$calculateFallDamage(fall, multiplier);
            return new AiDanger.FallPrediction(damageAfterReductions(mc, player.damageSources().fall(), amount), tick, false);
         }
      }

      boolean voidFall = y < mc.level.getMinY();
      float amount = ((AutismLivingEntityAccessor)player).autism$calculateFallDamage(fall, 1.0F);
      return new AiDanger.FallPrediction(damageAfterReductions(mc, player.damageSources().fall(), amount), -1, voidFall);
   }

   private static boolean sweptTouchesWater(Minecraft mc, AABB swept) {
      MutableBlockPos p = new MutableBlockPos();

      for (int bx = (int)Math.floor(swept.minX); bx <= (int)Math.floor(swept.maxX); bx++) {
         for (int by = (int)Math.floor(swept.minY); by <= (int)Math.floor(swept.maxY); by++) {
            for (int bz = (int)Math.floor(swept.minZ); bz <= (int)Math.floor(swept.maxZ); bz++) {
               if (mc.level.getFluidState(p.set(bx, by, bz)).is(FluidTags.WATER)) {
                  return true;
               }
            }
         }
      }

      return false;
   }

   private static float damageAfterReductions(Minecraft mc, DamageSource source, float damage) {
      LocalPlayer player = mc.player;
      if (!((AutismEntityAccessor)player).autism$isInvulnerableToBase(source) && !player.isDeadOrDying()) {
         float amount = damage;
         if (player.getAbilities().invulnerable && !source.is(DamageTypeTags.BYPASSES_INVULNERABILITY)) {
            return 0.0F;
         }

         if (source.scalesWithDifficulty()) {
            Difficulty difficulty = mc.level.getDifficulty();
            if (difficulty == Difficulty.PEACEFUL) {
               amount = 0.0F;
            } else if (difficulty == Difficulty.EASY) {
               amount = Math.min(amount / 2.0F + 1.0F, amount);
            } else if (difficulty == Difficulty.HARD) {
               amount = amount * 3.0F / 2.0F;
            }
         }

         if (amount == 0.0F) {
            return 0.0F;
         }

         if (source.is(DamageTypeTags.IS_FIRE) && player.hasEffect(MobEffects.FIRE_RESISTANCE)) {
            return 0.0F;
         }

         amount -= shieldBlockedAmount(player, source, amount);
         if (amount == 0.0F) {
            return 0.0F;
         }

         amount = ((AutismLivingEntityAccessor)player).autism$getDamageAfterArmorAbsorb(source, amount);
         return ((AutismLivingEntityAccessor)player).autism$getDamageAfterMagicAbsorb(source, amount);
      } else {
         return 0.0F;
      }
   }

   private static float shieldBlockedAmount(LocalPlayer player, DamageSource source, float amount) {
      if (amount <= 0.0F) {
         return 0.0F;
      } else {
         ItemStack blocking = player.getItemBlockingWith();
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
               float horizontalAngle = 3.1415927F;
               Vec3 sourcePosition = source.getSourcePosition();
               if (sourcePosition != null) {
                  Vec3 view = lookVector(player);
                  Vec3 direction = sourcePosition.subtract(player.position());
                  direction = new Vec3(direction.x, 0.0, direction.z).normalize();
                  horizontalAngle = (float)Math.acos(direction.dot(view));
               }

               return blocksAttacks.resolveBlockedDamage(source, amount, horizontalAngle);
            }
         }
      }
   }

   private static Vec3 lookVector(LocalPlayer player) {
      float yaw = player.getYHeadRot() * 0.017453292F;
      return new Vec3(-Mth.sin(yaw), 0.0, Mth.cos(yaw));
   }

   private static List<BlockPos> sphereOffsets(float radius) {
      List<BlockPos> out = new ArrayList<>();
      int r = Mth.ceil(radius);
      float rSq = radius * radius;

      for (int x = -r; x <= r; x++) {
         for (int y = -r; y <= r; y++) {
            for (int z = -r; z <= r; z++) {
               if (x * x + y * y + z * z <= rSq) {
                  out.add(new BlockPos(x, y, z));
               }
            }
         }
      }

      out.sort(Comparator.comparingInt(p -> p.getX() * p.getX() + p.getY() * p.getY() + p.getZ() * p.getZ()));
      return out;
   }

   private record DangerSnapshot(AiDanger.Assessment assessment, int fallEtaTicks, LocalPlayer player) {
   }

   interface TotemEvaluator {
      AiDanger.TotemEvaluator VANILLA = new AiDanger.TotemEvaluator() {
         @Override
         public boolean popsOrKills(float totalPredicted) {
            return autismclient.util.ExplosionDamageCalculator.wouldSelfDamagePopOrKill(totalPredicted);
         }

         @Override
         public boolean killsWithoutTotem(float totalPredicted) {
            return autismclient.util.ExplosionDamageCalculator.wouldSelfDamageKillWithoutTotem(totalPredicted);
         }
      };

      boolean popsOrKills(float var1);

      boolean killsWithoutTotem(float var1);
   }

   record ThreatSource(String kind, double x, double y, double z, float damage, int etaTicks) {
   }

   record Assessment(
      List<AiDanger.ThreatSource> sources,
      float worstSource,
      float fallDamage,
      float totalPredicted,
      int totemsHeld,
      int totemsInventory,
      int armorPoints,
      double toughness,
      int resistanceAmplifier,
      boolean fireResistant,
      float absorption,
      String blockingItem,
      float effectiveHealth,
      float afterWorstHit,
      boolean lethal,
      boolean lethalThroughTotem,
      boolean voidFall
   ) {
   }

   private record FallPrediction(float damage, int etaTicks, boolean voidFall) {
   }

   record Verdict(boolean lethal, boolean lethalThroughTotem) {
   }
}

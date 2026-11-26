package autismclient.modules;

import autismclient.util.AutismLiteVariant;
import java.util.List;
import java.util.Locale;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.block.Block;

public final class NoRenderState {
   private static volatile boolean enabled;
   static volatile boolean portalOverlay;
   static volatile boolean spyglassOverlay;
   static volatile boolean nausea;
   static volatile boolean pumpkinOverlay;
   static volatile boolean powderedSnowOverlay;
   static volatile boolean fireOverlay;
   static volatile boolean liquidOverlay;
   static volatile boolean inWallOverlay;
   static volatile boolean vignette;
   static volatile boolean guiBackground;
   static volatile boolean totemAnimation;
   static volatile boolean eatingParticles;
   static volatile boolean enchantmentGlint;
   static volatile boolean hurtCam;
   static volatile boolean bossBar;
   static volatile boolean scoreboard;
   static volatile boolean crosshair;
   static volatile boolean title;
   static volatile boolean heldItemName;
   static volatile boolean disableObfuscation;
   static volatile boolean potionIcons;
   static volatile boolean weather;
   static volatile boolean worldBorder;
   static volatile boolean blindness;
   static volatile boolean darkness;
   static volatile boolean fog;
   static volatile boolean enchantmentTableBook;
   static volatile boolean signText;
   static volatile boolean blockBreakOverlay;
   static volatile boolean blockBreakParticles;
   static volatile boolean beaconBeams;
   static volatile boolean fallingBlocks;
   static volatile boolean mapMarkers;
   static volatile boolean mapContents;
   static volatile boolean banners;
   static volatile boolean fireworkExplosions;
   static volatile boolean hideAllParticles;
   static volatile boolean constantTextureRotations;
   static volatile boolean sky;
   static volatile boolean stars;
   static volatile boolean sun;
   static volatile boolean moon;
   static volatile boolean sunriseGlow;
   static volatile boolean clouds;
   static volatile boolean forceTimeEnabled;
   static volatile long forcedTimeTicks;
   static volatile boolean forceWeatherEnabled;
   static volatile float forcedRainLevel;
   static volatile float forcedThunderLevel;
   static volatile boolean armor;
   static volatile boolean showInvisibleEntities;
   static volatile boolean disableGlowing;
   static volatile boolean spawnerEntities;
   static volatile boolean deadEntities;
   static volatile boolean nametags;
   static volatile boolean dropSpawnPackets;
   static volatile List<String> hiddenEntities = List.of();
   static volatile List<String> hiddenBlockEntities = List.of();

   private NoRenderState() {
   }

   static void disable() {
      enabled = false;
   }

   static void enable() {
      enabled = true;
   }

   private static boolean isAllowedByLicense() {
      return AutismLiteVariant.enabled() || autismclient.e.TokenStore.init();
   }

   public static boolean hidePortalOverlay() {
      return enabled && isAllowedByLicense() && portalOverlay;
   }

   public static boolean hideSpyglassOverlay() {
      return enabled && isAllowedByLicense() && spyglassOverlay;
   }

   public static boolean hideNausea() {
      return enabled && isAllowedByLicense() && nausea;
   }

   public static boolean hidePumpkinOverlay() {
      return enabled && isAllowedByLicense() && pumpkinOverlay;
   }

   public static boolean hidePowderedSnowOverlay() {
      return enabled && isAllowedByLicense() && powderedSnowOverlay;
   }

   public static boolean hideFireOverlay() {
      return enabled && isAllowedByLicense() && fireOverlay;
   }

   public static boolean hideLiquidOverlay() {
      return enabled && isAllowedByLicense() && liquidOverlay;
   }

   public static boolean hideInWallOverlay() {
      return enabled && isAllowedByLicense() && inWallOverlay;
   }

   public static boolean hideVignette() {
      return enabled && isAllowedByLicense() && vignette;
   }

   public static boolean hideGuiBackground() {
      return enabled && isAllowedByLicense() && guiBackground;
   }

   public static boolean hideTotemAnimation() {
      return enabled && isAllowedByLicense() && totemAnimation;
   }

   public static boolean hideEatingParticles() {
      return enabled && isAllowedByLicense() && eatingParticles;
   }

   public static boolean hideEnchantmentGlint() {
      return enabled && isAllowedByLicense() && enchantmentGlint;
   }

   public static boolean hideHurtCam() {
      return enabled && isAllowedByLicense() && hurtCam;
   }

   public static boolean hideBossBar() {
      return enabled && isAllowedByLicense() && bossBar;
   }

   public static boolean hideScoreboard() {
      return enabled && isAllowedByLicense() && scoreboard;
   }

   public static boolean hideCrosshair() {
      return enabled && isAllowedByLicense() && crosshair;
   }

   public static boolean hideTitle() {
      return enabled && isAllowedByLicense() && title;
   }

   public static boolean hideHeldItemName() {
      return enabled && isAllowedByLicense() && heldItemName;
   }

   public static boolean disableTextObfuscation() {
      return enabled && isAllowedByLicense() && disableObfuscation;
   }

   public static boolean hidePotionIcons() {
      return enabled && isAllowedByLicense() && potionIcons;
   }

   public static boolean hideWeather() {
      return enabled && isAllowedByLicense() && weather;
   }

   public static boolean hideWorldBorder() {
      return enabled && isAllowedByLicense() && worldBorder;
   }

   public static boolean hideBlindness() {
      return enabled && isAllowedByLicense() && blindness;
   }

   public static boolean hideDarkness() {
      return enabled && isAllowedByLicense() && darkness;
   }

   public static boolean hideFog() {
      return enabled && isAllowedByLicense() && fog;
   }

   public static boolean hideEnchantmentTableBook() {
      return enabled && isAllowedByLicense() && enchantmentTableBook;
   }

   public static boolean hideSignText() {
      return enabled && isAllowedByLicense() && signText;
   }

   public static boolean hideBlockBreakOverlay() {
      return enabled && isAllowedByLicense() && blockBreakOverlay;
   }

   public static boolean hideBlockBreakParticles() {
      return enabled && isAllowedByLicense() && blockBreakParticles;
   }

   public static boolean hideBeaconBeams() {
      return enabled && isAllowedByLicense() && beaconBeams;
   }

   public static boolean hideFallingBlocks() {
      return enabled && isAllowedByLicense() && fallingBlocks;
   }

   public static boolean hideMapMarkers() {
      return enabled && isAllowedByLicense() && mapMarkers;
   }

   public static boolean hideMapContents() {
      return enabled && isAllowedByLicense() && mapContents;
   }

   public static boolean hideBanners() {
      return enabled && isAllowedByLicense() && banners;
   }

   public static boolean useConstantTextureRotations() {
      return enabled && isAllowedByLicense() && constantTextureRotations;
   }

   public static boolean hideSky() {
      return enabled && isAllowedByLicense() && sky;
   }

   public static boolean hideStars() {
      return enabled && isAllowedByLicense() && stars;
   }

   public static boolean hideSun() {
      return enabled && isAllowedByLicense() && sun;
   }

   public static boolean hideMoon() {
      return enabled && isAllowedByLicense() && moon;
   }

   public static boolean hideSunriseGlow() {
      return enabled && isAllowedByLicense() && sunriseGlow;
   }

   public static boolean hideClouds() {
      return enabled && isAllowedByLicense() && clouds;
   }

   public static boolean isForceTimeEnabled() {
      return enabled && isAllowedByLicense() && forceTimeEnabled;
   }

   public static long getForcedTimeTicks() {
      return forcedTimeTicks;
   }

   public static boolean isForceWeatherEnabled() {
      return enabled && isAllowedByLicense() && forceWeatherEnabled;
   }

   public static float getForcedRainLevel() {
      return forcedRainLevel;
   }

   public static float getForcedThunderLevel() {
      return forcedThunderLevel;
   }

   public static boolean hideArmor() {
      return enabled && isAllowedByLicense() && armor;
   }

   public static boolean showInvisibleEntities() {
      return enabled && isAllowedByLicense() && showInvisibleEntities;
   }

   public static boolean disableGlowing() {
      return enabled && isAllowedByLicense() && disableGlowing;
   }

   public static boolean hideSpawnerEntities() {
      return enabled && isAllowedByLicense() && spawnerEntities;
   }

   public static boolean hideDeadEntities() {
      return enabled && isAllowedByLicense() && deadEntities;
   }

   public static boolean hideNametags() {
      return enabled && isAllowedByLicense() && nametags;
   }

   public static boolean shouldHideParticle(ParticleType<?> type) {
      if (!enabled || !isAllowedByLicense()) {
         return false;
      }

      if (hideAllParticles) {
         return true;
      }

      if (fireworkExplosions && type != null) {
         Identifier id = BuiltInRegistries.PARTICLE_TYPE.getKey(type);
         if (id != null && id.getPath().equals("firework")) {
            return true;
         }
      }

      return false;
   }

   public static boolean shouldHideEntity(Entity entity) {
      return entity != null && shouldHideEntityType(entity.getType());
   }

   public static boolean shouldHideEntityType(EntityType<?> type) {
      if (enabled && type != null && isAllowedByLicense()) {
         List<String> list = hiddenEntities;
         if (list.isEmpty()) {
            return false;
         }

         Identifier id = BuiltInRegistries.ENTITY_TYPE.getKey(type);
         return id != null && matchesRegistryList(list, id);
      } else {
         return false;
      }
   }

   public static boolean shouldDropSpawnPacket(EntityType<?> type) {
      return enabled && isAllowedByLicense() && dropSpawnPackets && shouldHideEntityType(type);
   }

   public static boolean shouldHideBlockEntity(Block block) {
      if (enabled && block != null && isAllowedByLicense()) {
         List<String> list = hiddenBlockEntities;
         if (list.isEmpty()) {
            return false;
         }

         Identifier id = BuiltInRegistries.BLOCK.getKey(block);
         return id != null && matchesRegistryList(list, id);
      } else {
         return false;
      }
   }

   private static boolean matchesRegistryList(List<String> list, Identifier id) {
      String full = id.toString();
      String path = id.getPath();

      for (String entry : list) {
         if (entry != null) {
            String e = entry.trim().toLowerCase(Locale.ROOT);
            if (e.equals(full) || e.equals(path)) {
               return true;
            }
         }
      }

      return false;
   }
}

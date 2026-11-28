package autismclient.util;

import autismclient.AutismClientAddon;
import autismclient.api.hud.HudElementProvider;
import autismclient.api.hud.HudElements;
import autismclient.mixin.accessor.AutismMultiPlayerGameModeAccessor;
import autismclient.modules.Module;
import java.awt.Color;
import java.net.InetSocketAddress;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Options;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.ChatScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ARGB;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.HitResult.Type;

public final class AutismHudManager {
   public static final String ID_ACTIVE_MODULES = "active_modules";
   public static final String TPS_ELEMENT_ID = "tps";
   public static final String ID_COORDINATES = "coordinates";
   public static final String ID_NETHER_COORDS = "nether_coords";
   public static final String ID_FPS = "fps";
   public static final String ID_PING = "ping";
   public static final String ID_SPEED = "speed";
   public static final String ID_GAME_MODE = "game_mode";
   public static final String ID_DURABILITY = "durability";
   public static final String ID_LOOKING_AT = "looking_at";
   public static final String ID_BREAKING_PROGRESS = "breaking_progress";
   public static final String ID_SERVER = "server";
   public static final String ID_WEATHER = "weather";
   public static final String ID_BIOME = "biome";
   public static final String ID_WORLD_TIME = "world_time";
   public static final String ID_REAL_TIME = "real_time";
   public static final String ID_ROTATION = "rotation";
   public static final String ID_WATERMARK = "watermark";
   public static final String ID_ARMOR = "armor";
   public static final String ID_INVENTORY = "inventory";
   public static final String ID_ITEM_COUNTER = "item_counter";
   public static final String ID_POTION_TIMERS = "potion_timers";
   public static final String ID_COMPASS = "compass";
   public static final String ID_ANTI_VANISH = "anti_vanish";
   public static final String ID_CPS = "cps";
   public static final String ID_KEYSTROKES = "keystrokes";
   public static final String ID_MEMORY = "memory";
   public static final String ID_SERVER_IP = "server_ip";
   public static final String ID_SERVER_BRAND = "server_brand";
   public static final String ID_FPS_GRAPH = "fps_graph";
   public static final String ID_BED_DANGER = "bed_danger";
   public static final String ID_GIF = "gif";
   private static final String KEY_PADDING = "padding";
   private static final String KEY_VERTICAL_PADDING = "vertical-padding";
   private static final String KEY_OUTLINE = "outline";
   private static final String KEY_OUTLINE_COLOR = "outline-color";
   private static final String count = "background";
   private static final String flags = "compass-width";
   private static final String KEY_RAINBOW_SPEED = "rainbow-speed";
   private static final String KEY_RAINBOW_SPREAD = "rainbow-spread";
   private static final String KEY_RAINBOW_SATURATION = "rainbow-saturation";
   private static final String KEY_RAINBOW_BRIGHTNESS = "rainbow-brightness";
   private static final String KEY_STAIR_SNAP = "stair-snap";
   private static final String KEY_LOGO_WIDTH = "logo-width";
   private static final String KEY_LOGO_RIGHT_PADDING = "logo-right-padding";
   private static final String KEY_KEYSTROKE_ACTIVE_COLOR = "keystroke-active-color";
   private static final String KEY_KEYSTROKE_IDLE_COLOR = "keystroke-idle-color";
   private static final String KEY_KEYSTROKE_TEXT_COLOR = "keystroke-text-color";
   private static final String KEY_KEYSTROKE_SHOW_SPACE = "keystroke-show-space";
   private static final String KEY_KEYSTROKE_SHOW_MOUSE = "keystroke-show-mouse";
   private static final String KEY_KEYSTROKE_SIZE = "keystroke-size";
   private static final String f_B_Str = "1";
   private static final String f_C_Str = "0";
   private static final String f_D_Str = "false";
   private static final String f_E_Str = "FF750000";
   private static final String f_F_Str = "true";
   private static final String f_G_Str = "112";
   private static final String f_Q_Str = "1.0";
   private static final String f_R_Str = "0.035";
   private static final String f_S_Str = "0.35";
   private static final String f_T_Str = "1.0";
   private static final String f_Z_Str = "2";
   private static final Minecraft MC = Minecraft.getInstance();
   private static long lastRenderFailureLogMs;
   private static final autismclient.gui.f.b.CharEncoder THEME = new autismclient.gui.f.b.CharEncoder();
   private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("HH:mm:ss", Locale.ROOT);
   private static final Identifier HUD_TEXTURE = Identifier.fromNamespaceAndPath("autismclient", "textures/gui/hud/autismclient_hud.png");
   private static final int LOGO_TEXTURE_WIDTH = 1600;
   private static final int LOGO_TEXTURE_HEIGHT = 151;
   private static final int update_I = 1;
   private static final int sync_I = 1;
   private static final int flush_I = 2;
   private static final int close_I = 2;
   private static final List<String> BUILTIN_ELEMENT_IDS = List.of(
      "active_modules",
      "tps",
      "coordinates",
      "nether_coords",
      "fps",
      "ping",
      "speed",
      "game_mode",
      "durability",
      "looking_at",
      "breaking_progress",
      "server",
      "server_ip",
      "server_brand",
      "weather",
      "biome",
      "world_time",
      "real_time",
      "rotation",
      "watermark",
      "armor",
      "inventory",
      "item_counter",
      "potion_timers",
      "compass",
      "anti_vanish",
      "cps",
      "keystrokes",
      "memory",
      "fps_graph",
      "bed_danger",
      "gif"
   );
   private static double lastSpeedX;
   private static double lastSpeedZ;
   private static long lastSpeedGameTime = -9223372036854775808L;
   private static boolean lastSpeedFreecam;
   private static double smoothedSpeed;
   private static double cachedRainbowSpeed = 1.0;
   private static int rainbowSpeedRevision = -2147483648;
   private static final Map<String, AutismHudManager.CachedElementLayout> layoutCache = new HashMap<>();
   private static final Map<String, Map<String, String>> elementSettingsCache = new HashMap<>();
   private static long renderFrameCount;
   private static int heldWidth;
   private static long heldWidthUntil;
   private static List<AutismHudManager.HudElementBounds> activeOccluders = List.of();
   private static boolean elementStatesLoaded;
   private static List<String> cachedElementIds = BUILTIN_ELEMENT_IDS;
   private static int cachedElementIdsRevision = -1;
   private static int lastHudRevision = -1;
   private static final Map<String, AutismConfig.HudElementState> elementStateCache = new HashMap<>();
   private static AutismConfig cachedConfig;
   private static int layoutRevision;
   private static boolean fastPathEnabled;
   private static boolean frameInputsChanged;
   private static long frameTimeMs;
   private static int lastScreenWidth;
   private static int lastScreenHeight;
   private static int lastPlayerId;
   private static int lastFontId;
   private static int lastLayoutRevision;
   private static int lastModuleRevision;
   private static int lastHudElementsRevision;
   private static int lastConfigId;
   private static int lastThemeRevision;
   private static final String[] COMPASS_LABELS = new String[]{"S", "SW", "W", "NW", "N", "NE", "E", "SE"};
   private static final int[] COMPASS_LABEL_WIDTHS = new int[COMPASS_LABELS.length];
   private static Font compassFont;
   private static int compassReloadGen = -2147483648;
   private static final float[] keyPressAnimation = new float[7];
   private static long lastAnimationNanos;
   private static final int FPS_HISTORY_SIZE = 200;
   private static final int[] fpsHistory = new int[200];
   private static int fpsHistoryIndex;
   private static int fpsHistoryCount;
   private static double lowestRecentTps = 20.0;
   private static long lowestTpsAt;
   private static int lastPing;
   private static final Map<String, String> gifSizeSignatures = new HashMap<>();
   private static final Map<String, Integer> ELEMENT_ORDER = buildElementOrder();
   private static List<AutismHudManager.PlacedHudItem> placedItemsSnapshot;
   private static final AutismHudManager.CachedElementLayout[] visibleLayouts = new AutismHudManager.CachedElementLayout[BUILTIN_ELEMENT_IDS.size()];
   private static List<AutismHudManager.PlacedHudItem> cachedPlacedItems = List.of();
   private static long cachedPlacedItemsSignature = -9223372036854775808L;

   private AutismHudManager() {
   }

   private static List<String> cachedElementIds() {
      if (HudElements.isEmpty()) {
         return BUILTIN_ELEMENT_IDS;
      }

      int revision = HudElements.revision();
      if (revision != cachedElementIdsRevision) {
         List<String> ids = new ArrayList<>(BUILTIN_ELEMENT_IDS);
         ids.addAll(HudElements.ids());
         cachedElementIds = ids;
         cachedElementIdsRevision = revision;
      }

      return cachedElementIds;
   }

   public static int enabledCount() {
      ensureInitialized();
      int count = 0;

      for (String id : cachedElementIds()) {
         if (getState(id).enabled) {
            count++;
         }
      }

      return count;
   }

   public static void ensureInitialized() {
      AutismConfig config = AutismConfig.getGlobal();
      int hudRevision = HudElements.revision();
      if (!elementStatesLoaded || !config.hudLayoutMigrated || hudRevision != lastHudRevision) {
         elementStateCache.clear();
         if (!config.hudLayoutMigrated) {
            migrateLegacyLayout(config);
         }

         for (String id : cachedElementIds()) {
            getState(id);
         }

         migrateLegacyRowLayout(config);
         elementStatesLoaded = true;
         lastHudRevision = hudRevision;
      }
   }

   public static List<String> elementIds() {
      ensureInitialized();
      return cachedElementIds();
   }

   public static String displayName(String id) {
      String var10000;
      switch (id) {
         case "active_modules":
            var10000 = "Active Modules";
            break;
         case "tps":
            var10000 = "TPS";
            break;
         case "coordinates":
            var10000 = "Coordinates";
            break;
         case "nether_coords":
            var10000 = "Nether Coords";
            break;
         case "fps":
            var10000 = "FPS";
            break;
         case "ping":
            var10000 = "Ping";
            break;
         case "speed":
            var10000 = "Speed";
            break;
         case "game_mode":
            var10000 = "Game Mode";
            break;
         case "durability":
            var10000 = "Durability";
            break;
         case "looking_at":
            var10000 = "Looking At";
            break;
         case "breaking_progress":
            var10000 = "Breaking Progress";
            break;
         case "server":
            var10000 = "Server";
            break;
         case "weather":
            var10000 = "Weather";
            break;
         case "biome":
            var10000 = "Biome";
            break;
         case "world_time":
            var10000 = "World Time";
            break;
         case "real_time":
            var10000 = "Real Time";
            break;
         case "rotation":
            var10000 = "Rotation";
            break;
         case "watermark":
            var10000 = "Logo";
            break;
         case "armor":
            var10000 = "Armor";
            break;
         case "inventory":
            var10000 = "Inventory";
            break;
         case "item_counter":
            var10000 = "Item Counter";
            break;
         case "potion_timers":
            var10000 = "Potion Timers";
            break;
         case "compass":
            var10000 = "Compass";
            break;
         case "anti_vanish":
            var10000 = "Anti Vanish";
            break;
         case "cps":
            var10000 = "CPS";
            break;
         case "keystrokes":
            var10000 = "Keystrokes";
            break;
         case "memory":
            var10000 = "Memory";
            break;
         case "server_ip":
            var10000 = "Server IP";
            break;
         case "server_brand":
            var10000 = "Server Brand";
            break;
         case "fps_graph":
            var10000 = "FPS Graph";
            break;
         case "bed_danger":
            var10000 = "Bed Danger";
            break;
         case "gif":
            var10000 = "GIF";
            break;
         default:
            HudElementProvider provider = HudElements.get(id);
            String label = null;

            try {
               label = provider != null ? provider.label() : null;
            } catch (Throwable t) {
               AutismClientAddon.LOG.warn("[Hud] Addon element '{}' label failed", id, t);
            }

            var10000 = label != null ? label : id;
      }

      return var10000;
   }

   public static String description(String id) {
      String var10000;
      switch (id) {
         case "active_modules":
            var10000 = "Enabled Autism modules with optional info and keybinds.";
            break;
         case "tps":
            var10000 = "Estimated server TPS from Autism's tick tracker.";
            break;
         case "coordinates":
            var10000 = "Current player coordinates in this dimension.";
            break;
         case "nether_coords":
            var10000 = "Overworld/Nether converted coordinates using the 8:1 ratio.";
            break;
         case "fps":
            var10000 = "Current client FPS.";
            break;
         case "ping":
            var10000 = "Current player latency.";
            break;
         case "speed":
            var10000 = "Horizontal player speed in blocks per second.";
            break;
         case "game_mode":
            var10000 = "Current game mode.";
            break;
         case "durability":
            var10000 = "Main hand durability.";
            break;
         case "looking_at":
            var10000 = "Block or entity currently under the crosshair.";
            break;
         case "breaking_progress":
            var10000 = "Current vanilla block-breaking progress.";
            break;
         case "server":
            var10000 = "Current server or world.";
            break;
         case "weather":
            var10000 = "Current world weather.";
            break;
         case "biome":
            var10000 = "Current biome.";
            break;
         case "world_time":
            var10000 = "Current world day time.";
            break;
         case "real_time":
            var10000 = "Local system time.";
            break;
         case "rotation":
            var10000 = "Camera direction, yaw, and pitch.";
            break;
         case "watermark":
            var10000 = "Movable AUTISM Client logo image.";
            break;
         case "armor":
            var10000 = "Equipped armor with vanilla item overlays.";
            break;
         case "inventory":
            var10000 = "Main inventory grid.";
            break;
         case "item_counter":
            var10000 = "Counts a picked or held item across the inventory.";
            break;
         case "potion_timers":
            var10000 = "Active status effects and durations.";
            break;
         case "compass":
            var10000 = "Compact facing compass.";
            break;
         case "anti_vanish":
            var10000 = "Vanish detections.";
            break;
         case "cps":
            var10000 = "Left and right mouse clicks per second (counts Fast Use too).";
            break;
         case "keystrokes":
            var10000 = "Movement keys and mouse buttons with fill animation.";
            break;
         case "memory":
            var10000 = "JVM heap memory usage and max.";
            break;
         case "server_ip":
            var10000 = "Connected server IP and port.";
            break;
         case "server_brand":
            var10000 = "Server software brand and version.";
            break;
         case "fps_graph":
            var10000 = "Recent FPS history as a graph.";
            break;
         case "bed_danger":
            var10000 = "Enemy warnings near your bed; configure Warning Distance in Danger > Bed Danger.";
            break;
         case "gif":
            var10000 = "Animated image (GIF/PNG/JPEG).";
            break;
         default:
            HudElementProvider provider = HudElements.get(id);
            String description = null;

            try {
               description = provider != null ? provider.description() : null;
            } catch (Throwable t) {
               AutismClientAddon.LOG.warn("[Hud] Addon element '{}' description failed", id, t);
            }

            var10000 = description != null ? description : "";
      }

      return var10000;
   }

   public static AutismConfig.HudElementState getState(String id) {
      AutismConfig config = AutismConfig.getGlobal();
      if (config != cachedConfig) {
         elementStateCache.clear();
         cachedConfig = config;
      }

      AutismConfig.HudElementState cachedState = elementStateCache.get(id);
      if (cachedState != null) {
         return cachedState;
      }

      ensureHudElements(config);
      AutismConfig.HudElementState state = config.hudElements.computeIfAbsent(id, key -> defaultState(key));
      if (state.settings == null) {
         state.settings = new LinkedHashMap<>();
      }

      for (Entry<String, String> entry : defaultSettings(id).entrySet()) {
         state.settings.putIfAbsent(entry.getKey(), entry.getValue());
      }

      elementStateCache.put(id, state);
      return state;
   }

   public static void saveConfig() {
      AutismConfig.getGlobal().save();
   }

   public static boolean isNativeHudVisible(Screen screen, Module hud) {
      if (autismclient.modules.aD.init_Z()) {
         return false;
      } else if (hud == null || !hud.isEnabled() || MC.player == null || MC.gui.hud.isHidden()) {
         return false;
      } else if (screen == null) {
         return true;
      } else if (screen instanceof autismclient.gui.screens.AutismHudEditorScreen) {
         return false;
      } else if (screen instanceof ChatScreen) {
         return booleanSetting(hud, "show-in-chat", false);
      } else if (screen.isPauseScreen()) {
         return booleanSetting(hud, "show-in-pause", false);
      } else {
         return booleanSetting(hud, "hide-in-guis", true) ? false : !(screen instanceof AbstractContainerScreen) || !booleanSetting(hud, "hide-in-guis", true);
      }
   }

   public static void updateSpeedSample() {
      if (MC.player == null) {
         lastSpeedGameTime = -9223372036854775808L;
         smoothedSpeed = 0.0;
      } else if (MC.level != null) {
         long gameTime = MC.level.getGameTime();
         if (gameTime != lastSpeedGameTime) {
            boolean freecam = isFreecamActive();
            if (freecam != lastSpeedFreecam) {
               lastSpeedFreecam = freecam;
               lastSpeedGameTime = -9223372036854775808L;
               smoothedSpeed = 0.0;
            }

            Vec3 sample = viewPosition();
            double x = sample.x;
            double z = sample.z;
            if (lastSpeedGameTime != -9223372036854775808L) {
               long ticks = Math.max(1L, gameTime - lastSpeedGameTime);
               double dx = x - lastSpeedX;
               double dz = z - lastSpeedZ;
               double distance = Math.sqrt(dx * dx + dz * dz);
               double instant = distance * (20.0 / ticks);
               if (instant > 80.0 && !freecam) {
                  instant = instantHorizontalSpeed();
               }

               if (instant < 0.01) {
                  instant = 0.0;
               }

               smoothedSpeed = smoothedSpeed * 0.7 + instant * 0.3;
               if (smoothedSpeed < 0.03) {
                  smoothedSpeed = 0.0;
               }
            }

            lastSpeedX = x;
            lastSpeedZ = z;
            lastSpeedGameTime = gameTime;
         }
      }
   }

   private static double instantHorizontalSpeed() {
      if (MC.player == null) {
         return 0.0;
      }

      double vx = MC.player.getDeltaMovement().x;
      double vz = MC.player.getDeltaMovement().z;
      return Math.sqrt(vx * vx + vz * vz) * 20.0;
   }

   public static void render(GuiGraphicsExtractor context, Font font, boolean editor, String selectedId, int mouseX, int mouseY) {
      render(context, font, editor, selectedId, mouseX, mouseY, List.of());
   }

   private static void logRenderFailure(String where, Throwable t) {
      long now = System.currentTimeMillis();
      if (now - lastRenderFailureLogMs >= 5000L) {
         lastRenderFailureLogMs = now;
         AutismClientAddon.LOG.warn("[Autism] HUD '{}' render failed; skipped to protect the UI", where, t);
      }
   }

   public static void render(
      GuiGraphicsExtractor context, Font font, boolean editor, String selectedId, int mouseX, int mouseY, List<AutismHudManager.HudElementBounds> occluders
   ) {
      long perf = bK.load_J();
      ensureInitialized();
      autismclient.gui.sdf.MsdfTextRenderer.setForceVanilla(!isCustomFontEnabled());
      refreshFrameState(font, editor);
      activeOccluders = !editor && occluders != null && !occluders.isEmpty() ? occluders : List.of();

      try {
         renderFrameCount++;
         updateSpeedSample();
         placedItemsSnapshot = buildPlacedItems(font, editor);

         for (String id : cachedElementIds()) {
            AutismConfig.HudElementState state = getState(id);
            if (state.enabled
               && (editor || !"bed_danger".equals(id))
               && (editor || !"anti_vanish".equals(id) || autismclient.modules.AntiVanishModule.isHudVisible())
               && !isShownInFpsElement(id)) {
               AutismHudManager.CachedElementLayout cached = cachedLayout(id, font);
               AutismHudManager.HudElementBounds bounds = cached.layout().toBounds();
               if (bounds.read > 0 && bounds.write > 0) {
                  int dodgeY = 0;
                  if (!editor && "anti_vanish".equals(id)) {
                     dodgeY = dodgeOffsetY(bounds);
                  } else if (!"active_modules".equals(id) && overlapsOccluder(bounds)) {
                     continue;
                  }

                  boolean hovered = isHovered(mouseX, mouseY, bounds);

                  try {
                     renderElement(context, font, id, state, cached, editor, selectedId != null && selectedId.equals(id), hovered, dodgeY);
                  } catch (Throwable t) {
                     logRenderFailure("element:" + id, t);
                  }
               }
            }
         }
      } finally {
         activeOccluders = List.of();
         placedItemsSnapshot = null;
         autismclient.gui.sdf.MsdfTextRenderer.setForceVanilla(false);
         disableFastPath();
         bK.init("hud.render", perf);
      }
   }

   private static boolean isCustomFontEnabled() {
      try {
         Module hud = autismclient.modules.al.init_Module_Str("hud");
         return hud != null && Boolean.parseBoolean(hud.value("custom-font"));
      } catch (Throwable t) {
         return false;
      }
   }

   public static AutismHudManager.ScaledElementBox layoutFor(String id, Font font) {
      return cachedLayout(id, font).layout();
   }

   public static void renderElement(GuiGraphicsExtractor context, Font font, String id) {
      if (context != null && font != null && id != null && !autismclient.modules.aD.init_Z()) {
         ensureInitialized();
         AutismConfig.HudElementState state = getState(id);
         if (state.enabled && (!"anti_vanish".equals(id) || autismclient.modules.AntiVanishModule.isHudVisible())) {
            autismclient.gui.sdf.MsdfTextRenderer.setForceVanilla(!isCustomFontEnabled());
            refreshFrameState(font, false);

            try {
               placedItemsSnapshot = buildPlacedItems(font, false);
               AutismHudManager.CachedElementLayout cached = cachedLayout(id, font);
               AutismHudManager.HudElementBounds bounds = cached.layout().toBounds();
               if (bounds.update() <= 0 || bounds.sync() <= 0) {
                  return;
               }

               renderElement(context, font, id, state, cached, false, false, false, 0);
            } finally {
               placedItemsSnapshot = null;
               autismclient.gui.sdf.MsdfTextRenderer.setForceVanilla(false);
               disableFastPath();
            }

            return;
         }
      }
   }

   private static AutismHudManager.ScaledElementBox computeLayout(
      String id, Font font, AutismConfig.HudElementState state, List<AutismHudManager.ColoredTextLine> lines, List<Integer> widths
   ) {
      int pad = padding(id);
      int width;
      int height;
      if ("bed_danger".equals(id) && !AutismLiteVariant.enabled()) {
         int[] size = autismclient.modules.BedDangerHudRenderer.size(font);
         width = size[0];
         height = size[1];
      } else if ("watermark".equals(id)) {
         int logoW = logoWidth(id);
         width = logoW + pad * 2 + logoRightPadding(id);
         height = logoHeight(logoW) + pad * 2;
      } else if ("gif".equals(id)) {
         width = gifWidth(id) + pad * 2;
         height = gifHeight(id) + pad * 2;
      } else if ("armor".equals(id)) {
         width = pad * 2 + 72;
         height = pad * 2 + 18;
      } else if ("inventory".equals(id)) {
         width = pad * 2 + 162;
         height = pad * 2 + 54;
      } else if ("compass".equals(id)) {
         width = pad * 2 + compassWidth(id);
         height = pad * 2 + 18;
      } else if ("keystrokes".equals(id)) {
         int unit = keystrokeUnit(id);
         int keyH = keyHeight(unit);
         int gap = 2;
         width = pad * 2 + 3 * unit + 2 * gap;
         int rows = 2 * keyH + gap;
         if (showSpaceKey(id)) {
            rows += gap + keyH;
         }

         if (showMouseKeys(id)) {
            rows += gap + keyH;
         }

         height = pad * 2 + rows;
      } else if (HudElements.isAddon(id)) {
         HudElementProvider provider = HudElements.get(id);
         int pw = 16;
         int ph = 10;

         try {
            pw = Math.max(1, provider.width());
            ph = Math.max(1, provider.height());
         } catch (Throwable t) {
            AutismClientAddon.LOG.warn("[Hud] Addon element '{}' sizing failed", id, t);
         }

         width = pad * 2 + pw;
         height = pad * 2 + ph;
      } else if ("active_modules".equals(id)) {
         int maxW = 0;

         for (Integer lineWidth : widths) {
            maxW = Math.max(maxW, lineWidth);
         }

         int rowH = rowHeight(id);
         int gap = lineGap(id);
         width = Math.max(32, maxW + pad * 2);
         height = lines.isEmpty() ? 0 : lines.size() * rowH + Math.max(0, lines.size() - 1) * gap;
      } else if ("fps".equals(id)) {
         int maxW = 0;

         for (Integer lineWidth : widths) {
            maxW = Math.max(maxW, lineWidth);
         }

         width = stableWidth(maxW) + pad * 2;
         height = THEME.init_I_l(autismclient.gui.f.b.l.read) + verticalPadding(id) * 2;
      } else {
         int lineH = lineHeight(id);
         int maxW = 0;

         for (Integer lineWidth : widths) {
            maxW = Math.max(maxW, lineWidth);
         }

         width = Math.max(32, maxW + pad * 2);
         int vpad = verticalPadding(id);
         height = Math.max(THEME.init_I_l(autismclient.gui.f.b.l.read) + vpad * 2, lines.size() * lineH - lineGap(id) + vpad * 2);
      }

      int renderX = clampX(id, anchoredX(state.anchor, state.f_x_I, width), width);
      int renderY = clampY(id, anchoredY(state.anchor, state.f_y_I, height), height);
      return new AutismHudManager.ScaledElementBox(id, renderX, renderY, width, height, 1.0);
   }

   public static AutismHudManager.HudElementBounds bounds(String id, Font font) {
      return layoutFor(id, font).toBounds();
   }

   private static AutismHudManager.HudElementBounds measureBounds(String id, Font font) {
      AutismHudManager.CachedElementLayout cached = cachedLayout(id, font);
      if (!"active_modules".equals(id)) {
         AutismHudManager.HudElementBounds bounds = cached.layout().toBounds();
         AutismHudManager.LayoutRect rect = expandForOutline(
            new AutismHudManager.LayoutRect(id, bounds.read(), bounds.write(), bounds.update(), bounds.sync()), cached.style()
         );
         return new AutismHudManager.HudElementBounds(id, rect.read(), rect.write(), rect.width(), rect.height());
      }

      List<AutismHudManager.LayoutRect> rects = visualRects(id, cached);
      if (rects.isEmpty()) {
         return cached.layout().toBounds();
      }

      int left = 2147483647;
      int top = 2147483647;
      int right = -2147483648;
      int bottom = -2147483648;

      for (AutismHudManager.LayoutRect rect : rects) {
         left = Math.min(left, rect.read());
         top = Math.min(top, rect.write());
         right = Math.max(right, rect.right());
         bottom = Math.max(bottom, rect.bottom());
      }

      return new AutismHudManager.HudElementBounds(id, left, top, Math.max(0, right - left), Math.max(0, bottom - top));
   }

   public static String elementAt(Font font, int mouseX, int mouseY) {
      ensureInitialized();
      List<String> ids = cachedElementIds();

      for (int i = ids.size() - 1; i >= 0; i--) {
         String id = ids.get(i);
         if (getState(id).enabled && !isShownInFpsElement(id)) {
            AutismHudManager.HudElementBounds bounds = measureBounds(id, font);
            if (isHovered(mouseX, mouseY, bounds)) {
               return id;
            }
         }
      }

      return null;
   }

   public static void setPosition(String id, int x, int y, int screenW, int screenH) {
      if (screenW > 0 && screenH > 0) {
         AutismConfig.HudElementState state = getState(id);
         AutismHudManager.ScaledElementBox layout = layoutFor(id, MC.font);
         int w = layout.init_I();
         int h = layout.load();
         int px = clamp(x, minX(id), maxX(id, screenW, w));
         int py = clamp(y, minY(id), maxY(id, screenH, h));
         boolean right = px > screenW / 2 || px + w >= screenW - 2 || px + w / 2 >= screenW / 2;
         boolean bottom = py > screenH / 2 || py + h >= screenH - 2 || py + h / 2 >= screenH / 2;
         state.anchor = (bottom ? "BOTTOM_" : "TOP_") + (right ? "RIGHT" : "LEFT");
         state.f_x_I = right ? px + w - screenW : px;
         state.f_y_I = bottom ? py + h - screenH : py;
         layoutCache.remove(id);
         layoutRevision++;
         saveConfig();
      }
   }

   public static void setEnabled(String id, boolean enabled) {
      getState(id).enabled = enabled;
      layoutRevision++;
      saveConfig();
   }

   public static void toggleEnabled(String id) {
      AutismConfig.HudElementState state = getState(id);
      state.enabled = !state.enabled;
      layoutRevision++;
      saveConfig();
   }

   public static String setting(String id, String key) {
      return getState(id).settings.getOrDefault(key, defaultSettings(id).getOrDefault(key, ""));
   }

   public static String defaultSetting(String id, String key) {
      return defaultSettings(id).getOrDefault(key, "");
   }

   public static void setSetting(String id, String key, String value) {
      getState(id).settings.put(key, value == null ? "" : value);
      layoutRevision++;
      saveConfig();
   }

   public static boolean boolSetting(String id, String key) {
      return Boolean.parseBoolean(setting(id, key));
   }

   public static int intSetting(String id, String key, int fallback) {
      try {
         return Integer.parseInt(setting(id, key));
      } catch (Exception ignored) {
         return fallback;
      }
   }

   public static double doubleSetting(String id, String key, double fallback) {
      try {
         return Double.parseDouble(setting(id, key));
      } catch (Exception ignored) {
         return fallback;
      }
   }

   public static void resetElement(String id) {
      AutismConfig config = AutismConfig.getGlobal();
      config.hudElements.put(id, defaultState(id));
      elementStateCache.remove(id);
      layoutRevision++;
      saveConfig();
   }

   public static void resetAll() {
      AutismConfig config = AutismConfig.getGlobal();
      ensureHudElements(config);
      config.hudElements.clear();

      for (String id : cachedElementIds()) {
         config.hudElements.put(id, defaultState(id));
      }

      elementStatesLoaded = false;
      layoutCache.clear();
      elementStateCache.clear();
      layoutRevision++;
      saveConfig();
   }

   public static List<String> textLines(String id) {
      List<String> lines = new ArrayList<>();

      for (AutismHudManager.ColoredTextLine line : cachedLayout(id, MC.font).lines()) {
         lines.add(line.text());
      }

      if (lines.isEmpty()) {
         lines.add(displayName(id));
      }

      return lines;
   }

   public static int revision() {
      return layoutRevision;
   }

   private static void refreshFrameState(Font font, boolean editor) {
      frameTimeMs = System.currentTimeMillis();
      int screenW = screenWidth();
      int screenH = screenHeight();
      int playerId = MC.player == null ? 0 : MC.player.getId();
      int fontId = System.identityHashCode(font);
      int moduleRev = autismclient.modules.al.init_I();
      int hudElsRev = HudElements.revision();
      int configId = System.identityHashCode(AutismConfig.getGlobal());
      int themeRev = ThemePaletteManager.generation();
      frameInputsChanged = screenW != lastScreenWidth
         || screenH != lastScreenHeight
         || playerId != lastPlayerId
         || fontId != lastFontId
         || layoutRevision != lastLayoutRevision
         || moduleRev != lastModuleRevision
         || hudElsRev != lastHudElementsRevision
         || configId != lastConfigId
         || themeRev != lastThemeRevision;
      lastScreenWidth = screenW;
      lastScreenHeight = screenH;
      lastPlayerId = playerId;
      lastFontId = fontId;
      lastLayoutRevision = layoutRevision;
      lastModuleRevision = moduleRev;
      lastHudElementsRevision = hudElsRev;
      lastConfigId = configId;
      lastThemeRevision = themeRev;
      fastPathEnabled = !editor;
   }

   private static void disableFastPath() {
      fastPathEnabled = false;
   }

   private static AutismHudManager.CachedElementLayout cachedLayout(String id, Font font) {
      AutismHudManager.CachedElementLayout fastCached = layoutCache.get(id);
      if (fastPathEnabled && !frameInputsChanged && fastCached != null && frameTimeMs < fastCached.nextCheckAt) {
         return fastCached;
      }

      AutismConfig.HudElementState state = getState(id);
      long now = fastPathEnabled ? frameTimeMs : System.currentTimeMillis();
      AutismHudManager.LayoutSignature signature = layoutSignature(id, state, font, now);
      long interval = refreshIntervalMs(id);
      long nextCheck = (now / interval + 1L) * interval;
      AutismHudManager.CachedElementLayout cached = layoutCache.get(id);
      if (cached != null && cached.signature().equals(signature)) {
         cached.nextCheckAt = nextCheck;
         return cached;
      }

      List<AutismHudManager.ColoredTextLine> lines = !"armor".equals(id)
            && !"inventory".equals(id)
            && !"compass".equals(id)
            && !"watermark".equals(id)
            && !"keystrokes".equals(id)
            && !"bed_danger".equals(id)
            && !"gif".equals(id)
            && !HudElements.isAddon(id)
         ? buildLines(id)
         : List.of();
      List<Integer> widths = new ArrayList<>(lines.size());

      for (AutismHudManager.ColoredTextLine line : lines) {
         widths.add(lineWidth(id, font, line));
      }

      AutismHudManager.ScaledElementBox layout = computeLayout(id, font, state, lines, widths);
      AutismHudManager.CachedElementLayout next = new AutismHudManager.CachedElementLayout(signature, lines, widths, layout, elementStyle(id));
      next.nextCheckAt = nextCheck;
      layoutCache.put(id, next);
      return next;
   }

   private static int lineWidth(String id, Font font, AutismHudManager.ColoredTextLine line) {
      int width = measureLine(font, line);
      if ("breaking_progress".equals(id)) {
         width = Math.max(width, measureLine(font, labeledLine(id, "Breaking", "100%")));
      }

      return width;
   }

   private static AutismHudManager.LayoutSignature layoutSignature(String id, AutismConfig.HudElementState state, Font font, long now) {
      long nowBucket = now / refreshIntervalMs(id);
      int screenW = screenWidth();
      int screenH = screenHeight();
      int playerId = MC.player == null ? 0 : MC.player.getId();
      int activeRevision = "active_modules".equals(id) ? autismclient.modules.al.load_I() : 0;
      int moduleRevision = "active_modules".equals(id) ? autismclient.modules.al.init_I() : 0;
      return new AutismHudManager.LayoutSignature(
         state.enabled,
         state.anchor,
         state.f_x_I,
         state.f_y_I,
         state.settings.hashCode(),
         nowBucket,
         screenW,
         screenH,
         playerId,
         activeRevision,
         moduleRevision,
         System.identityHashCode(font),
         ThemePaletteManager.generation()
      );
   }

   private static long refreshIntervalMs(String id) {
      long var10000;
      switch (id) {
         case "active_modules":
         case "coordinates":
         case "nether_coords":
         case "speed":
         case "looking_at":
         case "breaking_progress":
         case "rotation":
         case "compass":
         case "anti_vanish":
         case "cps":
         case "keystrokes":
         case "bed_danger":
         case "gif":
            var10000 = 50L;
            break;
         case "fps":
         case "tps":
         case "ping":
         case "real_time":
         case "world_time":
         case "potion_timers":
         case "item_counter":
            var10000 = 250L;
            break;
         default:
            var10000 = 500L;
      }

      return var10000;
   }

   private static boolean isFreecamActive() {
      return autismclient.modules.FreecamController.isActive();
   }

   private static Vec3 viewPosition() {
      if (isFreecamActive()) {
         return autismclient.modules.FreecamController.getInterpolatedFeetPos(1.0F);
      } else {
         return MC.player == null ? Vec3.ZERO : MC.player.position();
      }
   }

   private static BlockPos viewBlockPos() {
      if (isFreecamActive()) {
         return BlockPos.containing(autismclient.modules.FreecamController.getInterpolatedFeetPos(1.0F));
      } else {
         return MC.player == null ? BlockPos.ZERO : MC.player.blockPosition();
      }
   }

   private static float viewYaw() {
      if (isFreecamActive()) {
         return autismclient.modules.FreecamController.getInterpolatedYaw(1.0F);
      } else {
         return MC.player == null ? 180.0F : MC.player.getYRot();
      }
   }

   private static float viewPitch() {
      if (isFreecamActive()) {
         return autismclient.modules.FreecamController.getInterpolatedPitch(1.0F);
      } else {
         return MC.player == null ? 0.0F : MC.player.getXRot();
      }
   }

   private static Vec3 interactionPosition() {
      if (isFreecamActive() && autismclient.modules.FreecamController.isInteractionAllowed()) {
         return autismclient.modules.FreecamController.getInterpolatedFeetPos(1.0F);
      } else {
         return MC.player == null ? Vec3.ZERO : MC.player.position();
      }
   }

   private static List<AutismHudManager.ColoredTextLine> buildLines(String id) {
      List<AutismHudManager.ColoredTextLine> lines = new ArrayList<>();
      if (MC.player == null) {
         lines.add(labeledLine(id, displayName(id), "Preview"));
         return lines;
      }

      switch (id) {
         case "active_modules":
            addActiveModuleLines(lines);
            break;
         case "tps":
            lines.add(tpsLine());
            break;
         case "coordinates":
            Vec3 v = viewPosition();
            double[] p = CoordinateSpoofer.spoof(v.x, v.y, v.z);
            lines.add(labeledLine(id, "Pos", formatBlockCoords(p[0], p[1], p[2])));
            break;
         case "nether_coords":
            lines.add(init_al_NetHelper());
            break;
         case "fps":
            lines.add(fpsLine());
            break;
         case "ping":
            lines.add(pingLine());
            break;
         case "speed":
            lines.add(labeledLine(id, "Speed", String.format(Locale.ROOT, "%.2f b/s", smoothedSpeed)));
            break;
         case "game_mode":
            lines.add(labeledLine(id, "Game", gameModeText()));
            break;
         case "durability":
            lines.add(durabilityLine());
            break;
         case "looking_at":
            lines.add(lookingAtLine());
            break;
         case "breaking_progress":
            lines.add(breakingProgressLine());
            break;
         case "server":
            lines.add(serverLine());
            break;
         case "weather":
            lines.add(weatherLine());
            break;
         case "biome":
            lines.add(biomeLine());
            break;
         case "world_time":
            lines.add(worldTimeLine());
            break;
         case "real_time":
            lines.add(realTimeLine());
            break;
         case "rotation":
            lines.add(labeledLine(id, "Rot", String.format(Locale.ROOT, "%.1f yaw, %.1f pitch", Mth.wrapDegrees(viewYaw()), viewPitch())));
         case "watermark":
            break;
         case "item_counter":
            lines.add(itemCounterLine());
            break;
         case "potion_timers":
            addPotionLines(lines);
            break;
         case "compass":
            lines.add(labeledLine(id, "Compass", facingText()));
            break;
         case "anti_vanish":
            addAntiVanishLines(lines);
            break;
         case "cps":
            addCpsLines(lines);
            break;
         case "memory":
            lines.add(memoryLine());
            break;
         case "server_ip":
            lines.add(serverIpLine());
            break;
         case "server_brand":
            lines.add(labeledLine(id, "Brand", serverBrandText()));
            break;
         case "fps_graph":
            lines.add(fpsGraphLine());
            break;
         default:
            lines.add(labeledLine(id, displayName(id), ""));
      }

      if ("active_modules".equals(id) && lines.isEmpty()) {
         return lines;
      }

      if (lines.isEmpty()) {
         lines.add(labeledLine(id, displayName(id), ""));
      }

      return lines;
   }

   private static void addActiveModuleLines(List<AutismHudManager.ColoredTextLine> lines) {
      boolean showInfo = boolSetting("active_modules", "module-info");
      boolean showKeybind = boolSetting("active_modules", "show-keybind");
      String hidden = "|" + setting("active_modules", "hidden-modules").toLowerCase(Locale.ROOT) + "|";
      List<Module> modules = new ArrayList<>(autismclient.modules.al.load_List());
      modules.removeIf(modulex -> !modulex.showInArrayList());
      modules.removeIf(modulex -> hidden.contains("|" + modulex.id().toLowerCase(Locale.ROOT) + "|"));
      String sort = setting("active_modules", "sort");
      if ("Name".equals(sort)) {
         modules.sort(Comparator.comparing(Module::name, String.CASE_INSENSITIVE_ORDER));
      } else if ("Category".equals(sort)) {
         modules.sort(Comparator.<Module, String>comparing(m -> m.category().load()).thenComparing(Module::name));
      } else {
         Map<Module, Integer> plainWidths = new IdentityHashMap<>(modules.size() * 2);

         for (Module module : modules) {
            plainWidths.put(module, init_I_Module_2(module, showInfo, showKeybind));
         }

         modules.sort((a, b) -> {
            int widthCompare = Integer.compare(plainWidths.get(b), plainWidths.get(a));
            return widthCompare != 0 ? widthCompare : a.name().compareToIgnoreCase(b.name());
         });
      }

      String colorMode = setting("active_modules", "color-mode");
      int moduleCount = modules.size();
      AutismHudManager.RainbowParams rainbow = rainbowParams(colorMode, moduleCount);
      int infoColor = activeModulesColor("module-info-color", -4743522);

      for (int i = 0; i < modules.size(); i++) {
         Module module = modules.get(i);
         int moduleColor = moduleColor(module, i, rainbow);
         AutismHudManager.ColoredTextLine line = new AutismHudManager.ColoredTextLine();
         line.append(module.name(), moduleColor);
         String info = module.info();
         if (showInfo && info != null && !info.isBlank()) {
            line.append(" " + info, infoColor);
         }

         if (showKeybind && module.keybind() != -1) {
            line.append(" [" + ValueSink.init_Str(module.keybind()) + "]", infoColor);
         }

         lines.add(line);
      }
   }

   private static void addAntiVanishLines(List<AutismHudManager.ColoredTextLine> lines) {
      for (autismclient.modules.AntiVanishModule.HudRow entry : autismclient.modules.AntiVanishModule.entries()) {
         lines.add(
            labeledLine(
               "anti_vanish",
               autismclient.modules.AntiVanishModule.statusLabel(entry),
               autismclient.modules.AntiVanishModule.roleLabel(entry)
            )
         );
      }

      if (lines.isEmpty()) {
         lines.add(labeledLine("anti_vanish", "Vanish", "Clear"));
      }
   }

   private static AutismHudManager.RainbowParams rainbowParams(String colorMode, int moduleCount) {
      return new AutismHudManager.RainbowParams(
         colorMode,
         rainbowPhase(rainbowSpeed()),
         (float)doubleSetting("active_modules", "rainbow-spread", 0.035),
         Math.min(0.35F, (float)doubleSetting("active_modules", "rainbow-saturation", 0.35)),
         (float)doubleSetting("active_modules", "rainbow-brightness", 1.0),
         colorSetting("active_modules", "flat-color", -50373),
         colorSetting("active_modules", "gradient-start-color", -50373),
         colorSetting("active_modules", "gradient-end-color", -10538),
         colorSetting("active_modules", "value-color", -791321),
         Math.max(1.0, moduleCount - 1.0)
      );
   }

   private static double rainbowSpeed() {
      if (rainbowSpeedRevision != layoutRevision) {
         cachedRainbowSpeed = doubleSetting("active_modules", "rainbow-speed", 1.0)
            * ("Reverse".equals(setting("active_modules", "rainbow-direction")) ? -1.0 : 1.0);
         rainbowSpeedRevision = layoutRevision;
      }

      return cachedRainbowSpeed;
   }

   private static float rainbowPhase(double speed) {
      if (speed != 0.0 && !Double.isNaN(speed)) {
         double periodMs = 1000.0 / (0.0525 * Math.abs(speed));
         long period = Math.max(1L, (long)periodMs);
         float phase = (float)((double)(System.currentTimeMillis() % period) / period);
         return speed < 0.0 ? (1.0F - phase) % 1.0F : phase;
      } else {
         return 0.0F;
      }
   }

   private static int moduleColor(Module module, int index, AutismHudManager.RainbowParams rainbow) {
      if ("Flat".equals(rainbow.colorMode())) {
         return rainbow.flatColor();
      } else if ("Random".equals(rainbow.colorMode())) {
         return 0xFF000000 | module.id().hashCode() & 16777215;
      } else if ("Gradient".equals(rainbow.colorMode())) {
         double spread = rainbow.spread() * 3.0;
         double phase = (rainbow.phase() + index * spread + index / rainbow.moduleSpan()) % 1.0;
         double t = phase < 0.5 ? phase * 2.0 : (1.0 - phase) * 2.0;
         return lerpColor(rainbow.gradientStartColor(), rainbow.gradientEndColor(), t);
      } else {
         float hue = (rainbow.phase() + index * rainbow.spread()) % 1.0F;
         int rgb = Color.HSBtoRGB(hue, rainbow.saturation(), rainbow.brightness());
         return mixColor(-402653184 | rgb & 16777215, rainbow.valueColor(), 0.55);
      }
   }

   private static String moduleLabel(Module module, boolean showInfo, boolean showKeybind) {
      StringBuilder line = new StringBuilder(module.name());
      String info = module.info();
      if (showInfo && info != null && !info.isBlank()) {
         line.append(' ').append(info);
      }

      if (showKeybind && module.keybind() != -1) {
         line.append(" [").append(ValueSink.init_Str(module.keybind())).append(']');
      }

      return line.toString();
   }

   private static int init_I_Module_2(Module module, boolean showInfo, boolean showKeybind) {
      return autismclient.gui.f.b.k.init(
         MC.font,
         moduleLabel(module, showInfo, showKeybind),
         THEME.init_Identifier_l(autismclient.gui.f.b.l.read),
         colorSetting("active_modules", "text-color", -791321)
      );
   }

   private static AutismHudManager.ColoredTextLine init_al_NetHelper() {
      if (MC.level != null && MC.player != null) {
         Identifier dimension = MC.level.dimension().identifier();
         String path = dimension.getPath();
         Vec3 v = viewPosition();
         double[] p = CoordinateSpoofer.spoof(v.x, v.y, v.z);
         double x = p[0];
         double y = p[1];
         double z = p[2];
         if ("overworld".equals(path)) {
            return labeledLine("nether_coords", "Nether", formatBlockCoords(x / 8.0, y, z / 8.0));
         } else {
            return "the_nether".equals(path)
               ? labeledLine("nether_coords", "Overworld", formatBlockCoords(x * 8.0, y, z * 8.0))
               : labeledLine("nether_coords", "Opposite", "N/A");
         }
      } else {
         return labeledLine("nether_coords", "Opposite", "N/A");
      }
   }

   private static void renderElement(
      GuiGraphicsExtractor context,
      Font font,
      String id,
      AutismConfig.HudElementState state,
      AutismHudManager.CachedElementLayout cached,
      boolean editor,
      boolean selected,
      boolean hovered,
      int dodgeY
   ) {
      float alpha = 1.0F;
      AutismHudManager.ScaledElementBox layout = cached.layout();
      int x = layout.read();
      int y = layout.write() + dodgeY;
      int unscaledW = layout.width();
      int unscaledH = layout.height();
      if ("bed_danger".equals(id) && !AutismLiteVariant.enabled()) {
         autismclient.modules.BedDangerHudRenderer.init(context, font, x, y, editor);
         if (editor || selected || hovered) {
            drawRectOutline(context, x, y, unscaledW, unscaledH, colorSetting(id, selected ? "accent-color" : "outline-color", -1426113733), 1);
         }
      } else if ("active_modules".equals(id)) {
         renderTextLines(context, font, id, cached, editor, selected, hovered, alpha);
      } else {
         AutismHudManager.ElementStyle style = cached.style();
         boolean flatGrid = ("armor".equals(id) || "inventory".equals(id)) && isFlatSlotStyle(id);
         boolean background = style.background() && !flatGrid;
         AutismHudManager.LayoutRect contentRect = new AutismHudManager.LayoutRect(id, x, y, unscaledW, unscaledH);
         AutismHudManager.LayoutRect rect = expandForOutline(contentRect, style);
         boolean inGameOutline = style.outline() && !flatGrid && !editor && !selected && !hovered;
         boolean needsBlockers = background && !"compass".equals(id) || inGameOutline;
         List<AutismHudManager.LayoutRect> blockers = needsBlockers ? outlineBlockers(font, id, rect) : null;
         if (background) {
            if ("compass".equals(id)) {
               fillWithAlpha(context, rect.read(), rect.write(), rect.width(), rect.height(), alpha, style.close());
            } else {
               init_V_GuiGraphicsExtract_3(context, font, id, rect, withAlpha(style.close(), alpha), blockers);
            }
         } else if (editor) {
            autismclient.gui.f.b.k.init(context, rect.read(), rect.write(), rect.right(), rect.bottom(), editorBackdropColor(selected));
         }

         if (style.outline() && !flatGrid || editor || selected || hovered) {
            int border = selected ? colorSetting(id, "accent-color", -50373) : (editor ? colorSetting(id, "outline-color", -1426113733) : style.open());
            int outlineColor = withAlpha(border, alpha);
            if (!editor && !selected && !hovered) {
               drawOutline(context, rect, blockers, outlineColor, style.outlineWidth());
            } else {
               drawRectOutline(context, rect.read(), rect.write(), rect.width(), rect.height(), outlineColor, style.outlineWidth());
            }
         }

         int pad = style.padding();
         if ("watermark".equals(id)) {
            drawLogo(context, x + pad, y + pad, logoWidth(id), logoHeight(logoWidth(id)), alpha);
         } else if ("gif".equals(id)) {
            renderGif(context, font, id, x + pad, y + pad, unscaledW - pad * 2, unscaledH - pad * 2, alpha, editor);
         } else if ("armor".equals(id)) {
            drawArmor(context, x + pad, y + pad, alpha, style);
         } else if ("inventory".equals(id)) {
            renderInventory(context, x + pad, y + pad, alpha, style);
         } else if ("compass".equals(id)) {
            init_V_GuiGraphicsExtract_3(context, font, id, x + pad, y + pad, unscaledW - pad * 2, unscaledH - pad * 2, alpha, style);
         } else if ("keystrokes".equals(id)) {
            renderKeystrokes(context, font, id, x + pad, y + pad, unscaledW - pad * 2, unscaledH - pad * 2, alpha);
         } else if (HudElements.isAddon(id)) {
            HudElementProvider provider = HudElements.get(id);

            try {
               provider.render(context, font, x + pad, y + pad, alpha);
            } catch (Exception e) {
               AutismClientAddon.LOG.warn("[Hud] Addon element '{}' render failed", id, e);
            }
         } else {
            renderTextElement(context, font, id, state, cached, x, y, unscaledW, alpha);
         }
      }
   }

   private static void renderTextLines(
      GuiGraphicsExtractor context, Font font, String id, AutismHudManager.CachedElementLayout cached, boolean editor, boolean selected, boolean hovered, float alpha
   ) {
      List<AutismHudManager.ColoredTextLine> lines = cached.lines();
      List<AutismHudManager.LayoutRect> rowRects = layoutRects(id, cached);
      AutismHudManager.ElementStyle style = cached.style();
      int pad = style.padding();
      boolean background = style.background();
      boolean drawOutline = style.outline() || editor || selected || hovered;
      boolean shadow = style.shadow();
      int backgroundColor = withAlpha(style.close(), alpha);
      int editorColor = editorBackdropColor(selected);
      int border = selected ? colorSetting(id, "accent-color", -50373) : (editor ? colorSetting(id, "outline-color", -1426113733) : style.open());
      int outlineColor = withAlpha(border, alpha);
      int outlineWidth = style.outlineWidth();
      int verticalPad = style.verticalPadding();

      for (int i = 0; i < lines.size() && i < rowRects.size(); i++) {
         AutismHudManager.LayoutRect contentRect = rowRects.get(i);
         AutismHudManager.LayoutRect rect = expandForOutline(contentRect, style);
         if (!rectOverlapsOccluder(rect)) {
            List<AutismHudManager.LayoutRect> blockers = !background && !drawOutline ? null : outlineBlockers(font, id, rect);
            if (background) {
               init_V_GuiGraphicsExtract_3(context, font, id, rect, backgroundColor, blockers);
            } else if (editor) {
               autismclient.gui.f.b.k.init(context, rect.read(), rect.write(), rect.right(), rect.bottom(), editorColor);
            }

            if (drawOutline) {
               drawOutline(context, rect, blockers, outlineColor, outlineWidth);
            }

            int lineW = i < cached.widths().size() ? cached.widths().get(i) : -1;
            drawLine(
               context,
               font,
               lines.get(i),
               contentRect.read() + pad,
               contentRect.write() + verticalPad,
               Math.max(1, contentRect.width() - pad * 2),
               alpha,
               shadow,
               lineW
            );
         }
      }
   }

   private static void drawLogo(GuiGraphicsExtractor context, int x, int y, int width, int height, float alpha) {
      context.blit(
         RenderPipelines.GUI_TEXTURED,
         ThemeTextureRecolorer.init(HUD_TEXTURE, ThemePaletteManager.ColorRole.ACCENT),
         x,
         y,
         0.0F,
         0.0F,
         width,
         height,
         LOGO_TEXTURE_WIDTH,
         LOGO_TEXTURE_HEIGHT,
         LOGO_TEXTURE_WIDTH,
         LOGO_TEXTURE_HEIGHT,
         ARGB.white(alpha)
      );
   }

   private static void renderGif(
      GuiGraphicsExtractor context, Font font, String id, int x, int y, int width, int height, float alpha, boolean editor
   ) {
      String path = setting(id, "gif-path").trim();
      if (!path.isEmpty() && boolSetting(id, "gif-natural")) {
         syncGifNaturalSize(id, path);
      }

      Identifier texture = path.isEmpty() ? null : autismclient.util.gif.GifTextureManager.texture(path, gifFps(id), gifQualityScale(id), id);
      if (texture != null) {
         context.blit(texture, x, y, x + width, y + height, 0.0F, 1.0F, 0.0F, 1.0F);
      } else if (editor) {
         int tagColor = withAlpha(colorSetting(id, "label-color", -4743522), alpha);
         String tag = "GIF";
         int tagWidth = autismclient.gui.f.b.k.init(font, tag, THEME.init_Identifier_l(autismclient.gui.f.b.l.read), tagColor);
         int tagX = x + Math.max(0, (width - tagWidth) / 2);
         int tagY = y + Math.max(0, (height - THEME.init_I_l(autismclient.gui.f.b.l.read)) / 2);
         autismclient.gui.f.b.k.init(context, font, tag, THEME.init_Identifier_l(autismclient.gui.f.b.l.read), tagColor, tagX, tagY, false);
      }
   }

   private static void renderTextElement(
      GuiGraphicsExtractor context,
      Font font,
      String id,
      AutismConfig.HudElementState state,
      AutismHudManager.CachedElementLayout cached,
      int renderX,
      int renderY,
      int unscaledW,
      float alpha
   ) {
      List<AutismHudManager.ColoredTextLine> lines = cached.lines();
      AutismHudManager.ElementStyle style = cached.style();
      int pad = style.padding();
      int y = renderY + style.verticalPadding();
      int lineH = style.update();
      String alignment = style.alignment();
      boolean shadow = style.shadow();
      int maxW = Math.max(1, unscaledW - pad * 2);

      for (int i = 0; i < lines.size(); i++) {
         AutismHudManager.ColoredTextLine line = lines.get(i);
         int lineW = i < cached.widths().size() ? cached.widths().get(i) : measureLine(font, line);
         int tx = renderX + pad;
         if ("Center".equals(alignment)) {
            tx = renderX + Math.max(pad, (unscaledW - lineW) / 2);
         } else if ("Right".equals(alignment)) {
            tx = renderX + Math.max(pad, unscaledW - pad - lineW);
         }

         drawLine(context, font, line, tx, y, maxW, alpha, shadow, lineW);
         y += lineH;
      }
   }

   private static void drawLine(
      GuiGraphicsExtractor context, Font font, AutismHudManager.ColoredTextLine line, int x, int y, int maxW, float alpha, boolean shadow, int lineWidth
   ) {
      int drawX = x;
      int used = 0;
      boolean fits = lineWidth >= 0 && lineWidth <= maxW;
      int[] cachedWidths = fits ? line.segmentWidths : null;
      boolean cachedWidthsValid = cachedWidths != null && cachedWidths.length == line.segments.size();

      for (int i = 0; i < line.segments.size(); i++) {
         AutismHudManager.ColoredSegment segment = line.segments.get(i);
         int remaining = Math.max(1, maxW - used);
         String text = fits
            ? segment.text
            : autismclient.gui.f.b.k.init(font, segment.text, remaining, THEME.init_Identifier_l(autismclient.gui.f.b.l.read), segment.color);
         autismclient.gui.f.b.k.init(
            context, font, text, THEME.init_Identifier_l(autismclient.gui.f.b.l.read), withAlpha(segment.color, alpha), drawX, y, shadow
         );
         int width = cachedWidthsValid
            ? cachedWidths[i]
            : autismclient.gui.f.b.k.init(font, text, THEME.init_Identifier_l(autismclient.gui.f.b.l.read), segment.color);
         drawX += width;
         used += width;
         if (used >= maxW) {
            break;
         }
      }
   }

   private static void drawArmor(GuiGraphicsExtractor context, int x, int y, float alpha, AutismHudManager.ElementStyle style) {
      if (MC.player != null) {
         EquipmentSlot[] slots = new EquipmentSlot[]{EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET};
         boolean flat = isFlatSlotStyle("armor");
         if (flat) {
            drawSlotGrid(context, x, y, slots.length, 1, alpha, style);
         }

         for (int i = 0; i < slots.length; i++) {
            int sx = x + i * 18;
            if (!flat) {
               init_V_GuiGraphicsExtract(context, sx, y);
            }

            ItemStack stack = MC.player.getItemBySlot(slots[i]);
            drawItem(context, stack, sx + 1, y + 1);
         }
      }
   }

   private static void renderInventory(GuiGraphicsExtractor context, int x, int y, float alpha, AutismHudManager.ElementStyle style) {
      if (MC.player != null) {
         boolean flat = isFlatSlotStyle("inventory");
         if (flat) {
            drawSlotGrid(context, x, y, 9, 3, alpha, style);
         }

         for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
               int sx = x + col * 18;
               int sy = y + row * 18;
               if (!flat) {
                  init_V_GuiGraphicsExtract(context, sx, sy);
               }

               ItemStack stack = MC.player.getInventory().getItem(9 + row * 9 + col);
               drawItem(context, stack, sx + 1, sy + 1);
            }
         }
      }
   }

   private static void drawItem(GuiGraphicsExtractor context, ItemStack stack, int x, int y) {
      if (!stack.isEmpty() && MC.font != null) {
         context.item(stack, x, y);
         context.itemDecorations(MC.font, stack, x, y);
      }
   }

   private static boolean isFlatSlotStyle(String id) {
      return "Flat".equals(setting(id, "slot-style"));
   }

   private static void drawSlotGrid(GuiGraphicsExtractor context, int x, int y, int cols, int rows, float alpha, AutismHudManager.ElementStyle style) {
      int w = cols * 18;
      int h = rows * 18;
      if (style.background()) {
         autismclient.gui.f.b.k.init(context, x, y, x + w, y + h, withAlpha(style.close(), alpha));
      }

      if (style.outline()) {
         int line = withAlpha(style.open(), alpha);

         for (int i = 0; i <= cols; i++) {
            int lx = i == cols ? x + w - 1 : x + i * 18;
            autismclient.gui.f.b.k.init(context, lx, y, lx + 1, y + h, line);
         }

         for (int j = 0; j <= rows; j++) {
            int ly = j == rows ? y + h - 1 : y + j * 18;
            autismclient.gui.f.b.k.init(context, x, ly, x + w, ly + 1, line);
         }
      }
   }

   private static void init_V_GuiGraphicsExtract(GuiGraphicsExtractor context, int x, int y) {
      int fill = ThemePaletteManager.recolor(538579477, ThemePaletteManager.ColorRole.BACKDROP);
      int lit = ThemePaletteManager.recolor(1435577909, ThemePaletteManager.ColorRole.OUTLINE);
      int shade = ThemePaletteManager.recolor(1427835662, ThemePaletteManager.ColorRole.BACKDROP);
      autismclient.gui.f.b.k.init(context, x, y, x + 18, y + 18, fill);
      autismclient.gui.f.b.k.init(context, x, y, x + 18, y + 1, lit);
      autismclient.gui.f.b.k.init(context, x, y + 17, x + 18, y + 18, shade);
      autismclient.gui.f.b.k.init(context, x, y, x + 1, y + 18, lit);
      autismclient.gui.f.b.k.init(context, x + 17, y, x + 18, y + 18, shade);
   }

   private static void init_V_GuiGraphicsExtract_3(
      GuiGraphicsExtractor context, Font font, String id, int x, int y, int width, int height, float alpha, AutismHudManager.ElementStyle style
   ) {
      int stripH = Math.max(16, height);
      int centerX = x + width / 2;
      autismclient.gui.f.b.k.init(context, centerX, y + 2, centerX + 1, y + stripH - 2, withAlpha(style.poll(), alpha * 0.88F));
      String[] labels = COMPASS_LABELS;
      int reloadGen = autismclient.gui.f.b.k.init_I();
      if (compassFont != font || compassReloadGen != reloadGen) {
         for (int i = 0; i < labels.length; i++) {
            COMPASS_LABEL_WIDTHS[i] = autismclient.gui.f.b.k.init(font, labels[i], THEME.init_Identifier_l(autismclient.gui.f.b.l.read), -1);
         }

         compassFont = font;
         compassReloadGen = reloadGen;
      }

      float yaw = viewYaw();
      double normalized = (yaw % 360.0 + 360.0) % 360.0;
      int yText = y + Math.max(2, (stripH - THEME.init_I_l(autismclient.gui.f.b.l.read)) / 2);
      double visibleDegrees = 118.0;
      boolean fullHeightHighlight = style.background();
      boolean shadow = style.shadow();

      for (int i = 0; i < labels.length; i++) {
         double centerAngle = i * 45.0;
         double delta = wrapDegrees(centerAngle - normalized);
         double distance = Math.abs(delta);
         if (!(distance > visibleDegrees)) {
            double presence = 1.0 - distance / visibleDegrees;
            presence = presence * presence * (3.0 - 2.0 * presence);
            int tx = centerX + (int)Math.round(delta / visibleDegrees * (width / 2.0 - 8.0));
            double face = Math.max(0.0, 1.0 - distance / 24.0);
            int baseColor = lerpColor(style.push(), style.poll(), face);
            int textColor = withAlpha(baseColor, alpha * (0.18F + (float)presence * 0.82F));
            int tw = COMPASS_LABEL_WIDTHS[i];
            int halo = withAlpha(ThemePaletteManager.recolor(942148882, ThemePaletteManager.ColorRole.BACKDROP), alpha * (float)face);
            if (face > 0.0) {
               int haloTop = fullHeightHighlight ? y : y + 2;
               int haloBottom = fullHeightHighlight ? y + stripH : y + stripH - 2;
               autismclient.gui.f.b.k.init(context, tx - tw / 2 - 3, haloTop, tx + tw / 2 + 3, haloBottom, halo);
            }

            autismclient.gui.f.b.k.init(
               context,
               font,
               labels[i],
               THEME.init_Identifier_l(autismclient.gui.f.b.l.read),
               withAlpha(-872415232, alpha * (0.24F + (float)presence * 0.36F)),
               tx - tw / 2 + 1,
               yText + 1,
               false
            );
            autismclient.gui.f.b.k.init(
               context, font, labels[i], THEME.init_Identifier_l(autismclient.gui.f.b.l.read), textColor, tx - tw / 2, yText, shadow
            );
         }
      }
   }

   private static int rainbowColor(float phase, float hueOffset, float saturation, float brightness, int softenWith) {
      float hue = (phase + hueOffset) % 1.0F;
      int rgb = Color.HSBtoRGB(hue, saturation, brightness);
      return mixColor(-402653184 | rgb & 16777215, softenWith, 0.55);
   }

   private static int keystrokeUnit(String id) {
      return clamp(intSetting(id, "keystroke-size", 18), 12, 40);
   }

   private static int keyHeight(int unit) {
      return Math.max(10, Math.round(unit * 0.86F));
   }

   private static boolean showSpaceKey(String id) {
      return boolSetting(id, "keystroke-show-space");
   }

   private static boolean showMouseKeys(String id) {
      return boolSetting(id, "keystroke-show-mouse");
   }

   private static void renderKeystrokes(GuiGraphicsExtractor context, Font font, String id, int x, int y, int width, int height, float alpha) {
      int unit = keystrokeUnit(id);
      int gap = 2;
      int active = colorSetting(id, "keystroke-active-color", -50373);
      int idle = colorSetting(id, "keystroke-idle-color", -1274015724);
      int text = colorSetting(id, "keystroke-text-color", -1);
      boolean showMouse = showMouseKeys(id);
      long now = System.nanoTime();
      float dt = lastAnimationNanos == 0L ? 0.0F : (float)Math.min(0.1, (now - lastAnimationNanos) / 1.0E9);
      lastAnimationNanos = now;
      float factor = 1.0F - (float)Math.exp(-dt * 16.0);
      boolean[] pressed = pressedKeys();

      for (int i = 0; i < keyPressAnimation.length; i++) {
         float target = pressed[i] ? 1.0F : 0.0F;
         keyPressAnimation[i] = keyPressAnimation[i] + (target - keyPressAnimation[i]) * factor;
         if (Math.abs(target - keyPressAnimation[i]) < 0.003F) {
            keyPressAnimation[i] = target;
         }
      }

      int keyH = keyHeight(unit);
      int rowW = 3 * unit + 2 * gap;
      drawKey(context, font, x + (rowW - unit) / 2, y, unit, keyH, "W", keyPressAnimation[0], idle, active, text, alpha);
      int row2Y = y + keyH + gap;
      drawKey(context, font, x, row2Y, unit, keyH, "A", keyPressAnimation[1], idle, active, text, alpha);
      drawKey(context, font, x + unit + gap, row2Y, unit, keyH, "S", keyPressAnimation[2], idle, active, text, alpha);
      drawKey(context, font, x + 2 * (unit + gap), row2Y, unit, keyH, "D", keyPressAnimation[3], idle, active, text, alpha);
      int nextRowY = row2Y + keyH + gap;
      if (showSpaceKey(id)) {
         drawKey(context, font, x, nextRowY, rowW, keyH, keyPressAnimation[6], idle, active, text, alpha);
         nextRowY += keyH + gap;
      }

      if (showMouse) {
         int leftW = (rowW - gap) / 2;
         drawKey(context, font, x, nextRowY, leftW, keyH, Integer.toString(ClickRateTracker.leftCps()), keyPressAnimation[4], idle, active, text, alpha);
         drawKey(context, font, x + leftW + gap, nextRowY, rowW - leftW - gap, keyH, Integer.toString(ClickRateTracker.rightCps()), keyPressAnimation[5], idle, active, text, alpha);
      }
   }

   private static boolean[] pressedKeys() {
      Options o = MC == null ? null : MC.options;
      return new boolean[]{
         isKeyDown(o == null ? null : o.keyUp),
         isKeyDown(o == null ? null : o.keyLeft),
         isKeyDown(o == null ? null : o.keyDown),
         isKeyDown(o == null ? null : o.keyRight),
         isKeyDown(o == null ? null : o.keyAttack) || ClickRateTracker.init(120L),
         isKeyDown(o == null ? null : o.keyUse) || ClickRateTracker.load(120L),
         isKeyDown(o == null ? null : o.keyJump)
      };
   }

   private static boolean isKeyDown(KeyMapping mapping) {
      return mapping != null && KeyMappingControl.of(mapping).autism$isActuallyDown();
   }

   private static void drawKey(GuiGraphicsExtractor context, Font font, int x, int y, int w, int h, float fill, int idle, int active, int text, float alpha) {
      drawKey(context, font, x, y, w, h, "", fill, idle, active, text, alpha);
      int lineW = Math.max(6, Math.round(w * 0.45F));
      int lineH = Math.max(2, h / 7);
      int lx = x + (w - lineW) / 2;
      int ly = y + (h - lineH) / 2;
      autismclient.gui.f.b.k.init(context, lx, ly, lx + lineW, ly + lineH, withAlpha(text, alpha));
   }

   private static void drawKey(
      GuiGraphicsExtractor context, Font font, int x, int y, int w, int h, String label, float fill, int idle, int active, int text, float alpha
   ) {
      if (w > 0 && h > 0) {
         autismclient.gui.f.b.k.init(context, x, y, x + w, y + h, withAlpha(idle, alpha));
         if (fill > 0.003F) {
            float eased = fill * fill * (3.0F - 2.0F * fill);
            int fillColor = withAlpha(active, alpha * (0.6F + 0.4F * eased));
            if (eased >= 0.999F) {
               autismclient.gui.f.b.k.init(context, x, y, x + w, y + h, fillColor);
            } else {
               double cx = x + w / 2.0;
               double cy = y + h / 2.0;
               double maxR = Math.sqrt(w / 2.0 * (w / 2.0) + h / 2.0 * (h / 2.0));
               double r = eased * maxR;
               int top = Math.max(y, (int)Math.floor(cy - r));
               int bot = Math.min(y + h, (int)Math.ceil(cy + r));

               for (int row = top; row < bot; row++) {
                  double dy = row + 0.5 - cy;
                  double span = r * r - dy * dy;
                  if (!(span <= 0.0)) {
                     double hw = Math.sqrt(span);
                     int rx1 = Math.max(x, (int)Math.round(cx - hw));
                     int rx2 = Math.min(x + w, (int)Math.round(cx + hw));
                     if (rx2 > rx1) {
                        autismclient.gui.f.b.k.init(context, rx1, row, rx2, row + 1, fillColor);
                     }
                  }
               }
            }
         }

         drawRectOutline(context, x, y, w, h, withAlpha(1711276032, alpha), 1);
         Identifier fontId = THEME.init_Identifier_l(autismclient.gui.f.b.l.read);
         int tw = autismclient.gui.f.b.k.init(font, label, fontId, text);
         int fhgt = THEME.init_I_l(autismclient.gui.f.b.l.read);
         int tx = x + (w - tw) / 2;
         int ty = y + (h - fhgt) / 2 + 1;
         autismclient.gui.f.b.k.init(context, font, label, fontId, withAlpha(-872415232, alpha * 0.55F), tx + 1, ty + 1, false);
         autismclient.gui.f.b.k.init(context, font, label, fontId, withAlpha(text, alpha), tx, ty, false);
      }
   }

   private static void fillWithAlpha(GuiGraphicsExtractor context, int x, int y, int width, int height, float alpha, int base) {
      if (width > 0 && height > 0 && !(alpha <= 0.001F)) {
         int maxAlpha = (int)((base >>> 24 & 0xFF) * Math.max(0.0F, Math.min(1.0F, alpha)));
         int rgb = base & 16777215;
         int center = Math.max(1, width / 2);

         for (int i = 0; i < width; i++) {
            double edgeDistance = Math.abs(i + 0.5 - width / 2.0) / center;
            double presence = 1.0 - Math.min(1.0, edgeDistance);
            presence = presence * presence * (3.0 - 2.0 * presence);
            int a = (int)Math.round(maxAlpha * presence);
            if (a > 0) {
               autismclient.gui.f.b.k.init(context, x + i, y, x + i + 1, y + height, a << 24 | rgb);
            }
         }
      }
   }

   private static double wrapDegrees(double value) {
      double wrapped = value % 360.0;
      if (wrapped >= 180.0) {
         wrapped -= 360.0;
      }

      if (wrapped < -180.0) {
         wrapped += 360.0;
      }

      return wrapped;
   }

   private static AutismHudManager.ColoredTextLine labeledLine(String elementId, String label, String value) {
      AutismHudManager.ColoredTextLine line = new AutismHudManager.ColoredTextLine();
      line.append(label + ": ", colorSetting(elementId, "label-color", -4743522));
      line.append(value != null && !value.isBlank() ? value : "N/A", colorSetting(elementId, "value-color", -791321));
      return line;
   }

   private static AutismHudManager.ColoredTextLine fpsLine() {
      AutismHudManager.ColoredTextLine line = new AutismHudManager.ColoredTextLine();
      int labelColor = colorSetting("fps", "label-color", -4743522);
      int valueColor = colorSetting("fps", "value-color", -791321);
      int muted = withAlpha(labelColor, 0.72F);
      line.append("FPS: ", labelColor);
      line.append(Integer.toString(MC.getFps()), valueColor);
      line.append(" ", muted);
      line.append("TPS: ", labelColor);
      line.append(String.format(Locale.ROOT, "%.1f", autismclient.util.macro.ServerTickPhaseTracker.getTps()), valueColor);
      line.append(" ", muted);
      line.append("Ping: ", labelColor);
      line.append(autismclient.util.macro.ServerTickPhaseTracker.getLatencyMs() + " ms", valueColor);
      return line;
   }

   private static String gameModeText() {
      if (MC.gameMode == null) {
         return "N/A";
      }

      try {
         String name = MC.gameMode.getPlayerMode().getName();
         return name != null && !name.isBlank() ? RegistryDisplayNameCache.update(name) : "N/A";
      } catch (Throwable ignored) {
         return MC.gameMode.toString();
      }
   }

   private static String durabilityText() {
      ItemStack stack = MC.player == null ? ItemStack.EMPTY : MC.player.getMainHandItem();
      if (stack.isEmpty()) {
         return "Empty";
      }

      if (!stack.isDamageableItem()) {
         return "No durability";
      }

      int left = Math.max(0, stack.getMaxDamage() - stack.getDamageValue());
      int max = Math.max(1, stack.getMaxDamage());
      return String.format(Locale.ROOT, "%d / %d (%d%%)", left, max, Math.round(left * 100.0F / max));
   }

   private static AutismHudManager.ColoredTextLine memoryLine() {
      Runtime rt = Runtime.getRuntime();
      long used = rt.totalMemory() - rt.freeMemory() >> 20;
      long max = rt.maxMemory() >> 20;
      boolean showBar = boolSetting("memory", "show-bar");
      boolean showPct = boolSetting("memory", "show-percent");
      String fmt = setting("memory", "memory-format");
      long pct = max > 0L ? used * 100L / max : 0L;
      AutismHudManager.ColoredTextLine line = new AutismHudManager.ColoredTextLine();
      line.append("Memory: ", colorSetting("memory", "label-color", -4743522));
      boolean isPctFormat = !"used/max".equals(fmt);
      if (isPctFormat) {
         line.append(pct + "%", colorSetting("memory", "value-color", -791321));
      } else {
         line.append(used + " / " + max + " MB", colorSetting("memory", "value-color", -791321));
      }

      if (showPct && !isPctFormat) {
         line.append(" (" + pct + "%)", colorSetting("memory", "value-color", -791321));
      }

      if (showBar) {
         int barColor = pct > 85L ? -50373 : (pct > 60L ? -10752 : -12588930);
         int barW = Math.min(10, Math.max(1, (int)(pct * 10L / 100L)));
         line.append(" [", withAlpha(colorSetting("memory", "label-color", -4743522), 0.5F));
         StringBuilder bar = new StringBuilder();

         for (int i = 0; i < barW; i++) {
            bar.append('|');
         }

         for (int i = barW; i < 10; i++) {
            bar.append('.');
         }

         line.append(bar.toString(), barColor);
         line.append("]", withAlpha(colorSetting("memory", "label-color", -4743522), 0.5F));
      }

      return line;
   }

   private static AutismHudManager.ColoredTextLine serverIpLine() {
      AutismHudManager.ColoredTextLine line = new AutismHudManager.ColoredTextLine();
      line.append("IP: ", colorSetting("server_ip", "label-color", -4743522));
      if (MC.getCurrentServer() != null && MC.getCurrentServer().ip != null && !MC.getCurrentServer().ip.isBlank()) {
         String ip = MC.getCurrentServer().ip;
         if (boolSetting("server_ip", "show-port")
            && MC.getConnection() != null
            && MC.getConnection().getConnection().getRemoteAddress() instanceof InetSocketAddress inet) {
            ip = ip.contains(":") ? ip : ip + ":" + inet.getPort();
         }

         line.append(ip, colorSetting("server_ip", "value-color", -791321));
      } else {
         line.append(MC.hasSingleplayerServer() ? "Singleplayer" : "N/A", colorSetting("server_ip", "value-color", -791321));
      }

      return line;
   }

   private static String serverBrandText() {
      if (MC.getConnection() == null) {
         return "N/A";
      }

      try {
         String brand = MC.getConnection().serverBrand();
         return brand != null && !brand.isBlank() ? brand : "Unknown";
      } catch (Throwable ignored) {
         return "Unknown";
      }
   }

   private static AutismHudManager.ColoredTextLine fpsGraphLine() {
      int fps = MC.getFps();
      fpsHistory[fpsHistoryIndex] = fps;
      fpsHistoryIndex = (fpsHistoryIndex + 1) % 200;
      if (fpsHistoryCount < 200) {
         fpsHistoryCount++;
      }

      int samples = intSetting("fps_graph", "graph-samples", 100);
      samples = Math.max(10, Math.min(200, samples));
      int count = Math.min(samples, fpsHistoryCount);
      if (count <= 0) {
         return labeledLine("fps_graph", "FPS", Integer.toString(fps));
      }

      int minFps = fps;
      int maxFps = fps;
      int read = fpsHistoryIndex;

      for (int i = 0; i < count; i++) {
         read = (read - 1 + 200) % 200;
         int v = fpsHistory[read];
         if (v < minFps) {
            minFps = v;
         }

         if (v > maxFps) {
            maxFps = v;
         }
      }

      int range = Math.max(1, maxFps - minFps);
      AutismHudManager.ColoredTextLine line = new AutismHudManager.ColoredTextLine();
      if (boolSetting("fps_graph", "show-current-fps")) {
         line.append(Integer.toString(fps), colorSetting("fps_graph", "value-color", -791321));
      }

      String[] bars = new String[]{"▁", "▂", "▃", "▄", "▅", "▆", "▇", "█"};
      read = fpsHistoryIndex;
      int step = Math.max(1, count / 20);
      int shown = 0;
      line.append(" ", withAlpha(colorSetting("fps_graph", "label-color", -4743522), 0.3F));

      for (int i = 0; i < count && shown < 20; i += step) {
         read = (fpsHistoryIndex - i - 1 + 200) % 200;
         int v = fpsHistory[read];
         int barIndex = Math.min(7, (v - minFps) * 8 / range);
         float fraction = (float)(v - minFps) / range;
         int r = Math.round(255.0F * (1.0F - fraction));
         int g = Math.round(255.0F * fraction);
         int barColor = 0xFF000000 | Math.min(255, r) << 16 | Math.min(255, g) << 8;
         line.append(bars[barIndex], barColor);
         shown++;
      }

      return line;
   }

   private static void addCpsLines(List<AutismHudManager.ColoredTextLine> lines) {
      boolean showTotal = boolSetting("cps", "show-total");
      int leftCps = ClickRateTracker.leftCps();
      int rightCps = ClickRateTracker.rightCps();
      lines.add(labeledLine("cps", "Left", Integer.toString(leftCps)));
      lines.add(labeledLine("cps", "Right", Integer.toString(rightCps)));
      if (showTotal) {
         lines.add(labeledLine("cps", "Total", Integer.toString(leftCps + rightCps)));
      }
   }

   private static String lookingAtText() {
      HitResult hit = MC.hitResult;
      if (hit == null || hit.getType() == Type.MISS) {
         return "Nothing";
      } else if (hit instanceof EntityHitResult entityHit) {
         return entityHit.getEntity().getName().getString();
      } else if (hit instanceof BlockHitResult blockHit && MC.level != null) {
         BlockPos pos = blockHit.getBlockPos();
         Identifier id = BuiltInRegistries.BLOCK.getKey(MC.level.getBlockState(pos).getBlock());
         return RegistryDisplayNameCache.load(id.toString()) + " " + formatBlockCoords(pos.getX(), pos.getY(), pos.getZ());
      } else {
         return RegistryDisplayNameCache.update(hit.getType().name());
      }
   }

   private static String breakingProgressText() {
      if (MC.hitResult instanceof BlockHitResult && MC.gameMode != null) {
         try {
            AutismMultiPlayerGameModeAccessor accessor = (AutismMultiPlayerGameModeAccessor)MC.gameMode;
            return !accessor.autism$isDestroying() ? "0%" : Math.round(clampDouble(accessor.autism$getDestroyProgress(), 0.0, 1.0) * 100.0) + "%";
         } catch (Throwable ignored) {
            return "0%";
         }
      } else {
         return "0%";
      }
   }

   private static AutismHudManager.ColoredTextLine tpsLine() {
      double tps = autismclient.util.macro.ServerTickPhaseTracker.getTps();
      if (tps < lowestRecentTps || System.currentTimeMillis() - lowestTpsAt > 5000L) {
         lowestRecentTps = tps;
         lowestTpsAt = System.currentTimeMillis();
      }

      String value = String.format(Locale.ROOT, boolSetting("tps", "tps-precise") ? "%.2f" : "%.1f", tps);
      int valueColor = colorSetting("tps", "value-color", -791321);
      if (boolSetting("tps", "tps-color-threshold")) {
         valueColor = tps >= 19.0 ? -12588930 : (tps >= 15.0 ? -10752 : -50373);
      }

      AutismHudManager.ColoredTextLine line = new AutismHudManager.ColoredTextLine();
      line.append("TPS: ", colorSetting("tps", "label-color", -4743522));
      line.append(value, valueColor);
      if (boolSetting("tps", "tps-show-jitter")) {
         line.append(" (" + String.format(Locale.ROOT, "%.1f", lowestRecentTps) + ")", withAlpha(valueColor, 0.65F));
      }

      return line;
   }

   private static AutismHudManager.ColoredTextLine pingLine() {
      int ping = autismclient.util.macro.ServerTickPhaseTracker.getLatencyMs();
      int valueColor = colorSetting("ping", "value-color", -791321);
      if (boolSetting("ping", "ping-color-threshold")) {
         valueColor = ping <= 50 ? -12588930 : (ping <= 150 ? -10752 : -50373);
      }

      AutismHudManager.ColoredTextLine line = new AutismHudManager.ColoredTextLine();
      line.append("Ping: ", colorSetting("ping", "label-color", -4743522));
      line.append(ping + " ms", valueColor);
      if (boolSetting("ping", "ping-show-jitter")) {
         int old = lastPing;
         lastPing = ping;
         int jitter = old > 0 ? Math.abs(ping - old) : 0;
         if (jitter > 0) {
            line.append(" ±" + jitter, withAlpha(valueColor, 0.65F));
         }
      }

      return line;
   }

   private static AutismHudManager.ColoredTextLine durabilityLine() {
      ItemStack stack = MC.player == null ? ItemStack.EMPTY : MC.player.getMainHandItem();
      AutismHudManager.ColoredTextLine line = new AutismHudManager.ColoredTextLine();
      if (boolSetting("durability", "show-item-name") && !stack.isEmpty()) {
         line.append(stack.getHoverName().getString() + ": ", colorSetting("durability", "label-color", -4743522));
      } else {
         line.append("Durability: ", colorSetting("durability", "label-color", -4743522));
      }

      if (stack.isEmpty()) {
         line.append("Empty", colorSetting("durability", "value-color", -791321));
      } else if (!stack.isDamageableItem()) {
         line.append("No durability", colorSetting("durability", "value-color", -791321));
      } else {
         int left = Math.max(0, stack.getMaxDamage() - stack.getDamageValue());
         int max = Math.max(1, stack.getMaxDamage());
         int pct = Math.round(left * 100.0F / max);
         int valColor = colorSetting("durability", "value-color", -791321);
         if (boolSetting("durability", "low-durability-warn")) {
            valColor = pct > 50 ? -12588930 : (pct > 20 ? -10752 : -50373);
         }

         line.append(String.format(Locale.ROOT, "%d / %d (%d%%)", left, max, pct), valColor);
      }

      return line;
   }

   private static AutismHudManager.ColoredTextLine lookingAtLine() {
      AutismHudManager.ColoredTextLine line = new AutismHudManager.ColoredTextLine();
      HitResult hit = MC.hitResult;
      line.append("Looking: ", colorSetting("looking_at", "label-color", -4743522));
      if (hit != null && hit.getType() != Type.MISS) {
         String name;
         if (hit instanceof EntityHitResult entityHit) {
            name = entityHit.getEntity().getName().getString();
         } else if (hit instanceof BlockHitResult blockHit && MC.level != null) {
            BlockState state = MC.level.getBlockState(blockHit.getBlockPos());
            Identifier id = BuiltInRegistries.BLOCK.getKey(state.getBlock());
            if (boolSetting("looking_at", "show-block-id")) {
               name = id.toString();
            } else {
               name = RegistryDisplayNameCache.load(id.toString());
            }

            if (boolSetting("looking_at", "show-distance")) {
               double dist = interactionPosition().distanceTo(blockHit.getLocation());
               name = name + " " + String.format(Locale.ROOT, "%.1fm", dist);
            }
         } else {
            name = RegistryDisplayNameCache.update(hit.getType().name());
         }

         line.append(name, colorSetting("looking_at", "value-color", -791321));
         return line;
      } else {
         line.append("Nothing", colorSetting("looking_at", "value-color", -791321));
         return line;
      }
   }

   private static AutismHudManager.ColoredTextLine breakingProgressLine() {
      AutismHudManager.ColoredTextLine line = new AutismHudManager.ColoredTextLine();
      if (MC.hitResult instanceof BlockHitResult && MC.gameMode != null && MC.level != null) {
         double progress = 0.0;

         try {
            AutismMultiPlayerGameModeAccessor accessor = (AutismMultiPlayerGameModeAccessor)MC.gameMode;
            if (accessor.autism$isDestroying()) {
               progress = accessor.autism$getDestroyProgress();
            }

            progress = Math.max(0.0, Math.min(1.0, progress));
         } catch (Throwable var6) {
         }

         int pct = (int)Math.round(progress * 100.0);
         if (boolSetting("breaking_progress", "show-block-name")) {
            BlockPos pos = ((BlockHitResult)MC.hitResult).getBlockPos();
            String blockName = RegistryDisplayNameCache.load(BuiltInRegistries.BLOCK.getKey(MC.level.getBlockState(pos).getBlock()).toString());
            line.append(blockName + ": ", colorSetting("breaking_progress", "label-color", -4743522));
         } else {
            line.append("Breaking: ", colorSetting("breaking_progress", "label-color", -4743522));
         }

         line.append(pct + "%", pct >= 80 ? -12588930 : (pct >= 40 ? -10752 : colorSetting("breaking_progress", "value-color", -791321)));
         return line;
      } else {
         line.append("Breaking: 0%", colorSetting("breaking_progress", "label-color", -4743522));
         return line;
      }
   }

   private static AutismHudManager.ColoredTextLine serverLine() {
      AutismHudManager.ColoredTextLine line = new AutismHudManager.ColoredTextLine();
      line.append("Server: ", colorSetting("server", "label-color", -4743522));
      if (MC.getCurrentServer() != null && MC.getCurrentServer().ip != null && !MC.getCurrentServer().ip.isBlank()) {
         line.append(MC.getCurrentServer().ip, colorSetting("server", "value-color", -791321));
      } else if (MC.hasSingleplayerServer()) {
         line.append("Singleplayer", colorSetting("server", "value-color", -791321));
      } else {
         line.append("N/A", colorSetting("server", "value-color", -791321));
      }

      return line;
   }

   private static AutismHudManager.ColoredTextLine weatherLine() {
      String w = weatherText();
      AutismHudManager.ColoredTextLine line = new AutismHudManager.ColoredTextLine();
      line.append("Weather: ", colorSetting("weather", "label-color", -4743522));
      int var10000;
      switch (w) {
         case "Thunder":
            var10000 = -50373;
            break;
         case "Rain":
            var10000 = -10036737;
            break;
         default:
            var10000 = colorSetting("weather", "value-color", -791321);
      }

      int wColor = var10000;
      line.append(w, wColor);
      if (boolSetting("weather", "show-temperature") && MC.level != null) {
         Holder<Biome> biome = MC.level.getBiome(viewBlockPos());
         float temp = biome.value().getBaseTemperature();
         line.append(" " + String.format(Locale.ROOT, "%.1f°C", temp), withAlpha(wColor, 0.65F));
      }

      return line;
   }

   private static AutismHudManager.ColoredTextLine biomeLine() {
      String b = biomeText();
      AutismHudManager.ColoredTextLine line = new AutismHudManager.ColoredTextLine();
      line.append("Biome: ", colorSetting("biome", "label-color", -4743522));
      line.append(b, colorSetting("biome", "value-color", -791321));
      return line;
   }

   private static AutismHudManager.ColoredTextLine worldTimeLine() {
      String fmt = setting("world_time", "world-time-format");
      AutismHudManager.ColoredTextLine line = new AutismHudManager.ColoredTextLine();
      line.append("Time: ", colorSetting("world_time", "label-color", -4743522));
      if ("ticks".equals(fmt) && MC.level != null) {
         line.append(Long.toString(overworldClockTime() % 24000L), colorSetting("world_time", "value-color", -791321));
      } else {
         line.append(worldTimeText(), colorSetting("world_time", "value-color", -791321));
      }

      if (boolSetting("world_time", "show-day") && MC.level != null) {
         long day = overworldClockTime() / 24000L;
         line.append(" Day " + day, withAlpha(colorSetting("world_time", "value-color", -791321), 0.65F));
      }

      return line;
   }

   private static AutismHudManager.ColoredTextLine realTimeLine() {
      String fmt = setting("real_time", "real-time-format");
      DateTimeFormatter formatter = "24h".equals(fmt) ? DateTimeFormatter.ofPattern("HH:mm") : DateTimeFormatter.ofPattern("h:mm a", Locale.US);
      AutismHudManager.ColoredTextLine line = new AutismHudManager.ColoredTextLine();
      line.append("Time: ", colorSetting("real_time", "label-color", -4743522));
      line.append(LocalTime.now().format(formatter), colorSetting("real_time", "value-color", -791321));
      if (boolSetting("real_time", "show-date")) {
         line.append(" " + LocalDate.now().format(DateTimeFormatter.ofPattern("MM/dd")), withAlpha(colorSetting("real_time", "value-color", -791321), 0.65F));
      }

      return line;
   }

   private static String serverAddressText() {
      if (MC.getCurrentServer() != null && MC.getCurrentServer().ip != null && !MC.getCurrentServer().ip.isBlank()) {
         return MC.getCurrentServer().ip;
      } else {
         return MC.hasSingleplayerServer() ? "Singleplayer" : "N/A";
      }
   }

   private static String weatherText() {
      if (MC.level == null) {
         return "N/A";
      } else if (MC.level.isThundering()) {
         return "Thunder";
      } else {
         return MC.level.isRaining() ? "Rain" : "Clear";
      }
   }

   private static String biomeText() {
      if (MC.level != null && MC.player != null) {
         try {
            return MC.level.getBiome(viewBlockPos()).unwrapKey().map(key -> RegistryDisplayNameCache.update(key.identifier().toString())).orElse("Unknown");
         } catch (Throwable ignored) {
            return "Unknown";
         }
      } else {
         return "N/A";
      }
   }

   private static String worldTimeText() {
      if (MC.level == null) {
         return "N/A";
      }

      long time = overworldClockTime() % 24000L;
      long hours = (time / 1000L + 6L) % 24L;
      long minutes = time % 1000L * 60L / 1000L;
      return String.format(Locale.ROOT, "%02d:%02d", hours, minutes);
   }

   private static long overworldClockTime() {
      return MC.level == null ? 0L : MC.level.getOverworldClockTime();
   }

   private static String facingText() {
      if (MC.player == null) {
         return "N";
      }

      String var10000;
      switch (Direction.fromYRot(viewYaw())) {
         case NORTH:
            var10000 = "North";
            break;
         case SOUTH:
            var10000 = "South";
            break;
         case EAST:
            var10000 = "East";
            break;
         case WEST:
            var10000 = "West";
            break;
         case UP:
            var10000 = "Up";
            break;
         case DOWN:
            var10000 = "Down";
            break;
         default:
            throw new MatchException(null, null);
      }

      return var10000;
   }

   private static AutismHudManager.ColoredTextLine itemCounterLine() {
      Item target = counterTargetItem();
      AutismHudManager.ColoredTextLine line = new AutismHudManager.ColoredTextLine();
      if (boolSetting("item_counter", "item-show-name")) {
         line.append(itemCounterLabel(target) + ": ", colorSetting("item_counter", "label-color", -4743522));
      }

      line.append(Integer.toString(countItem(target)), colorSetting("item_counter", "value-color", -791321));
      return line;
   }

   private static Item counterTargetItem() {
      if (MC.player == null) {
         return null;
      }

      if (boolSetting("item_counter", "item-count-held")) {
         ItemStack held = MC.player.getMainHandItem();
         return held.isEmpty() ? null : held.getItem();
      }

      String raw = setting("item_counter", "item-id");
      if (raw != null && !raw.isBlank()) {
         try {
            Identifier id = Identifier.parse(raw.contains(":") ? raw : "minecraft:" + raw);
            return BuiltInRegistries.ITEM.getOptional(id).orElse(null);
         } catch (Exception ignored) {
            return null;
         }
      } else {
         return null;
      }
   }

   private static String itemCounterLabel(Item target) {
      if (target == null) {
         return boolSetting("item_counter", "item-count-held") ? "Hand" : "Items";
      } else {
         return RegistryDisplayNameCache.init(BuiltInRegistries.ITEM.getKey(target).toString());
      }
   }

   private static int countItem(Item target) {
      if (MC.player != null && target != null) {
         int count = 0;

         for (int i = 0; i < MC.player.getInventory().getContainerSize(); i++) {
            ItemStack stack = MC.player.getInventory().getItem(i);
            if (!stack.isEmpty() && stack.getItem() == target) {
               count += stack.getCount();
            }
         }

         return count;
      } else {
         return 0;
      }
   }

   private static String formatBlockCoords(double x, double y, double z) {
      return String.format(Locale.ROOT, "%d, %d, %d", (int)Math.floor(x), (int)Math.floor(y), (int)Math.floor(z));
   }

   private static void addPotionLines(List<AutismHudManager.ColoredTextLine> lines) {
      if (MC.player != null && !MC.player.getActiveEffects().isEmpty()) {
         for (MobEffectInstance effect : MC.player.getActiveEffects()) {
            String name = Component.translatable(effect.getDescriptionId()).getString();
            int amplifier = effect.getAmplifier() + 1;
            if (amplifier > 1) {
               name = name + " " + romanNumeral(amplifier);
            }

            int seconds = Math.max(0, effect.getDuration() / 20);
            lines.add(labeledLine("potion_timers", name, String.format(Locale.ROOT, "%d:%02d", seconds / 60, seconds % 60)));
         }
      } else {
         lines.add(labeledLine("potion_timers", "Effects", "None"));
      }
   }

   private static String romanNumeral(int value) {
      String var10000;
      switch (Math.max(1, Math.min(10, value))) {
         case 1:
            var10000 = "I";
            break;
         case 2:
            var10000 = "II";
            break;
         case 3:
            var10000 = "III";
            break;
         case 4:
            var10000 = "IV";
            break;
         case 5:
            var10000 = "V";
            break;
         case 6:
            var10000 = "VI";
            break;
         case 7:
            var10000 = "VII";
            break;
         case 8:
            var10000 = "VIII";
            break;
         case 9:
            var10000 = "IX";
            break;
         default:
            var10000 = "X";
      }

      return var10000;
   }

   private static int measureLine(Font font, AutismHudManager.ColoredTextLine line) {
      int[] widths = new int[line.segments.size()];
      int width = 0;

      for (int i = 0; i < line.segments.size(); i++) {
         AutismHudManager.ColoredSegment segment = line.segments.get(i);
         int w = autismclient.gui.f.b.k.init(font, segment.text, THEME.init_Identifier_l(autismclient.gui.f.b.l.read), segment.color);
         widths[i] = w;
         width += w;
      }

      line.segmentWidths = widths;
      return width;
   }

   private static int lineHeight(String id) {
      return THEME.init_I_l(autismclient.gui.f.b.l.read) + lineGap(id);
   }

   private static int rowHeight(String id) {
      return THEME.init_I_l(autismclient.gui.f.b.l.read) + verticalPadding(id) * 2;
   }

   private static int stairSnap(String id) {
      return clamp(intSetting(id, "stair-snap", 2), 0, 24);
   }

   private static int lineGap(String id) {
      return clamp(intSetting(id, "line-gap", 0), 0, 10);
   }

   private static void migrateLegacyLayout(AutismConfig config) {
      ensureHudElements(config);
      AutismConfig.ModuleState old = config.modules == null ? null : config.modules.get("hud");
      Map<String, String> settings = old != null && old.settings != null ? old.settings : Map.of();
      int x = parseIntOr(settings.get("x"), 8);
      int y = parseIntOr(settings.get("y"), 8);
      boolean modules = parseBoolean(settings.get("modules"), true);
      boolean metrics = parseBoolean(settings.get("metrics"), true);
      boolean coords = parseBoolean(settings.get("coords"), true);
      placeElement(config, "active_modules", modules, x, y);
      placeElement(config, "tps", metrics, x, y + 24);
      placeElement(config, "coordinates", coords, x, y + 44);
      placeElement(config, "nether_coords", coords, x, y + 64);
      config.hudLayoutMigrated = true;
      config.save();
   }

   private static void placeElement(AutismConfig config, String id, boolean enabled, int x, int y) {
      AutismConfig.HudElementState state = config.hudElements.computeIfAbsent(id, ignored -> defaultState(id));
      state.enabled = enabled;
      state.f_x_I = x;
      state.f_y_I = y;
      if (state.settings == null) {
         state.settings = new LinkedHashMap<>();
      }

      for (Entry<String, String> entry : defaultSettings(id).entrySet()) {
         state.settings.putIfAbsent(entry.getKey(), entry.getValue());
      }
   }

   private static void migrateLegacyRowLayout(AutismConfig config) {
      if (config != null && config.hudElements != null) {
         int rowH = baseRowHeight();
         int oldRowH = THEME.init_I_l(autismclient.gui.f.b.l.read) + 2;
         int oldLogoBottom = logoHeight(180) + 1;
         int oldSpacedLogoBottom = logoHeight(180) + 6;
         int newLogoBottom = logoBottom();
         boolean changed = false;
         AutismConfig.HudElementState fps = config.hudElements.get("fps");
         AutismConfig.HudElementState tps = config.hudElements.get("tps");
         AutismConfig.HudElementState ping = config.hudElements.get("ping");
         AutismConfig.HudElementState speed = config.hudElements.get("speed");
         AutismConfig.HudElementState compass = config.hudElements.get("compass");
         AutismConfig.HudElementState coordinates = config.hudElements.get("coordinates");
         AutismConfig.HudElementState nether = config.hudElements.get("nether_coords");
         AutismConfig.HudElementState rotation = config.hudElements.get("rotation");
         AutismConfig.HudElementState antiVanish = config.hudElements.get("anti_vanish");
         AutismConfig.HudElementState logo = config.hudElements.get("watermark");
         if (!config.hudLayoutNormalizedV2) {
            relayoutLegacyColumn(fps, tps, ping, speed, compass, coordinates, nether, rotation, antiVanish, rowH, oldRowH, oldLogoBottom, oldSpacedLogoBottom, newLogoBottom);
            config.hudLayoutNormalizedV2 = true;
            changed = true;
         }

         if (compass != null && compass.settings != null && !compass.settings.containsKey("compass-style-migrated")) {
            if ("3".equals(compass.settings.get("padding"))) {
               compass.settings.put("padding", "1");
               changed = true;
            }

            if ("86".equals(compass.settings.get("compass-width"))) {
               compass.settings.put("compass-width", "112");
               changed = true;
            }

            if ("true".equalsIgnoreCase(compass.settings.get("outline"))) {
               compass.settings.put("outline", "false");
               changed = true;
            }

            compass.settings.put("compass-style-migrated", "true");
            changed = true;
         }

         if (logo != null && logo.settings != null && !logo.settings.containsKey("logo-style-migrated")) {
            if ("false".equalsIgnoreCase(logo.settings.get("background"))) {
               logo.settings.put("background", "true");
               changed = true;
            }

            if ("1".equals(logo.settings.get("padding"))) {
               logo.settings.put("padding", "1");
               changed = true;
            }

            logo.settings.put("logo-style-migrated", "true");
            changed = true;
         }

         for (AutismConfig.HudElementState element : config.hudElements.values()) {
            if (element != null && element.settings != null) {
               if (!element.settings.containsKey("vertical-padding")) {
                  element.settings.put("vertical-padding", "0");
                  changed = true;
               }

               if (!element.settings.containsKey("legacy-style-migrated")) {
                  String outlineColor = element.settings.get("outline-color");
                  if ("FF8F1F24".equalsIgnoreCase(outlineColor) || "FFD3424D".equalsIgnoreCase(outlineColor)) {
                     element.settings.put("outline-color", "FF750000");
                     changed = true;
                  }

                  if ("5".equals(element.settings.get("stair-snap"))) {
                     element.settings.put("stair-snap", "2");
                     changed = true;
                  }

                  element.settings.put("legacy-style-migrated", "true");
                  changed = true;
               }
            }
         }

         if (changed) {
            config.save();
         }
      }
   }

   private static void relayoutLegacyColumn(
      AutismConfig.HudElementState fps,
      AutismConfig.HudElementState tps,
      AutismConfig.HudElementState ping,
      AutismConfig.HudElementState speed,
      AutismConfig.HudElementState compass,
      AutismConfig.HudElementState coordinates,
      AutismConfig.HudElementState nether,
      AutismConfig.HudElementState rotation,
      AutismConfig.HudElementState antiVanish,
      int rowH,
      int oldRowH,
      int oldLogoBottom,
      int oldSpacedLogoBottom,
      int newLogoBottom
   ) {
      if (isTopLeftAt(fps, 0, oldLogoBottom) || isTopLeftAt(fps, 0, oldSpacedLogoBottom)) {
         fps.f_y_I = newLogoBottom;
      }

      if (isTopLeftAt(tps, 0, oldLogoBottom + rowH) || isTopLeftAt(tps, 0, oldSpacedLogoBottom + rowH)) {
         tps.enabled = false;
         tps.f_y_I = newLogoBottom;
      }

      if (isTopLeftAt(ping, 0, oldLogoBottom + rowH * 2) || isTopLeftAt(ping, 0, oldSpacedLogoBottom + rowH * 2)) {
         ping.enabled = false;
         ping.f_y_I = newLogoBottom;
      }

      if (isTopLeftAt(speed, 0, oldLogoBottom + rowH * 3) || isTopLeftAt(speed, 0, oldSpacedLogoBottom + rowH)) {
         speed.f_y_I = newLogoBottom + rowH;
      }

      if (isTopLeftAt(compass, 0, oldLogoBottom + rowH * 5) || isTopLeftAt(compass, 0, oldSpacedLogoBottom + rowH * 2)) {
         compass.f_y_I = newLogoBottom + rowH * 2;
      }

      if (init_Z_AutismConfig_HudEl(antiVanish)) {
         applyPosition(antiVanish, "TOP_LEFT", 0, newLogoBottom + rowH * 2);
      }

      int rightStackX = -1;
      int rightStackBottom = -2;
      if (init_Z_AutismConfig_HudEl_2(coordinates, nether, rotation)
         || init_Z_AutismConfig_HudEl_2(coordinates, nether, rotation, 0, oldRowH)
         || init_Z_AutismConfig_HudEl_2(coordinates, nether, rotation, 0, rowH)) {
         applyPosition(coordinates, "BOTTOM_RIGHT", rightStackX, rightStackBottom - rowH * 2);
         applyPosition(nether, "BOTTOM_RIGHT", rightStackX, rightStackBottom - rowH);
         applyPosition(rotation, "BOTTOM_RIGHT", rightStackX, rightStackBottom);
      }

      if (init_Z_AutismConfig_HudEl(compass, oldLogoBottom, oldSpacedLogoBottom, newLogoBottom, rowH)) {
         applyPosition(compass, "TOP_CENTER", 0, 2);
      }
   }

   private static boolean isTopLeftAt(AutismConfig.HudElementState state, int x, int y) {
      return state != null && "TOP_LEFT".equals(state.anchor) && state.f_x_I == x && state.f_y_I == y;
   }

   private static boolean init_Z_AutismConfig_HudEl(AutismConfig.HudElementState state) {
      return isAt(state, "TOP_RIGHT", 0, 72) || isAt(state, "TOP_LEFT", 0, 72);
   }

   private static boolean init_Z_AutismConfig_HudEl_2(
      AutismConfig.HudElementState coordinates, AutismConfig.HudElementState nether, AutismConfig.HudElementState rotation
   ) {
      int legacy = 0;
      if (isNearDefaultTopLeft(coordinates)) {
         legacy++;
      }

      if (isNearDefaultTopLeft(nether)) {
         legacy++;
      }

      if (isNearDefaultTopLeft(rotation)) {
         legacy++;
      }

      return legacy >= 2;
   }

   private static boolean isNearDefaultTopLeft(AutismConfig.HudElementState state) {
      if (state == null) {
         return false;
      }

      String anchor = state.anchor == null ? "" : state.anchor.toUpperCase(Locale.ROOT);
      return !anchor.contains("RIGHT") || state.f_x_I <= 0 && state.f_y_I <= 0
         ? "TOP_LEFT".equals(anchor) && state.f_x_I >= 0 && state.f_x_I <= 16 && state.f_y_I >= 0 && state.f_y_I <= 140
         : true;
   }

   private static boolean init_Z_AutismConfig_HudEl_2(
      AutismConfig.HudElementState coordinates, AutismConfig.HudElementState nether, AutismConfig.HudElementState rotation, int bottomOffset, int rowH
   ) {
      return isAt(coordinates, "BOTTOM_RIGHT", bottomOffset, -rowH * 2)
         && isAt(nether, "BOTTOM_RIGHT", bottomOffset, -rowH)
         && isAt(rotation, "BOTTOM_RIGHT", bottomOffset, 0);
   }

   private static boolean isAt(AutismConfig.HudElementState state, String anchor, int x, int y) {
      return state != null && anchor.equals(state.anchor) && state.f_x_I == x && state.f_y_I == y;
   }

   private static boolean init_Z_AutismConfig_HudEl(AutismConfig.HudElementState state, int oldLogoBottom, int oldSpacedLogoBottom, int newLogoBottom, int rowH) {
      if (state == null) {
         return false;
      } else {
         String anchor = state.anchor == null ? "" : state.anchor.toUpperCase(Locale.ROOT);
         if ("TOP_CENTER".equals(anchor) && state.f_x_I == 0 && state.f_y_I == 2) {
            return false;
         } else {
            return state.f_x_I != 0 && Math.abs(state.f_x_I) > 16
               ? false
               : "TOP_LEFT".equals(anchor)
                  || "TOP_CENTER".equals(anchor)
                     && (state.f_y_I == newLogoBottom + rowH * 2 || state.f_y_I == oldLogoBottom + rowH * 5 || state.f_y_I == oldSpacedLogoBottom + rowH * 2);
         }
      }
   }

   private static boolean applyPosition(AutismConfig.HudElementState state, String anchor, int x, int y) {
      if (state == null) {
         return false;
      }

      boolean changed = !anchor.equals(state.anchor) || state.f_x_I != x || state.f_y_I != y;
      if (!changed) {
         return false;
      }

      state.anchor = anchor;
      state.f_x_I = x;
      state.f_y_I = y;
      return true;
   }

   private static AutismConfig.HudElementState defaultState(String id) {
      AutismConfig.HudElementState state = new AutismConfig.HudElementState();
      state.enabled = update_Z(id);
      state.scale = 1.0;
      state.anchor = read_Str(id);
      int[] position = init_IArr_Str(id);
      state.f_x_I = position[0];
      state.f_y_I = position[1];
      state.settings = new LinkedHashMap<>(defaultSettings(id));
      if ("bed_danger".equals(id)) {
         AutismConfig.ModuleState danger = AutismConfig.getGlobal().modules.get("danger");
         if (danger != null) {
            try {
               state.f_y_I = Math.clamp(Integer.parseInt(danger.settings.getOrDefault("bed-warning-y", "52")), 0, 240);
            } catch (NumberFormatException var5) {
            }
         }
      }

      return state;
   }

   private static String read_Str(String id) {
      HudElementProvider provider = HudElements.get(id);
      if (provider != null) {
         try {
            String anchor = provider.defaultAnchor();
            return anchor != null ? anchor : "TOP_LEFT";
         } catch (Throwable t) {
            AutismClientAddon.LOG.warn("[Hud] Addon element '{}' defaultAnchor failed", id, t);
            return "TOP_LEFT";
         }
      }

      String var10000;
      switch (id) {
         case "active_modules":
            var10000 = "TOP_RIGHT";
            break;
         case "coordinates":
         case "nether_coords":
         case "rotation":
            var10000 = "BOTTOM_RIGHT";
            break;
         case "compass":
         case "bed_danger":
            var10000 = "TOP_CENTER";
            break;
         case "keystrokes":
            var10000 = "MIDDLE_LEFT";
            break;
         default:
            var10000 = "TOP_LEFT";
      }

      return var10000;
   }

   private static int[] init_IArr_Str(String id) {
      HudElementProvider provider = HudElements.get(id);
      if (provider != null) {
         try {
            return new int[]{provider.defaultX(), provider.defaultY()};
         } catch (Throwable t) {
            AutismClientAddon.LOG.warn("[Hud] Addon element '{}' default position failed", id, t);
            return new int[]{0, 0};
         }
      }

      int rowH = baseRowHeight();
      int logoBottom = logoBottom();
      int[] var10000;
      switch (id) {
         case "watermark":
            var10000 = new int[]{0, 0};
            break;
         case "fps":
            var10000 = new int[]{0, logoBottom};
            break;
         case "tps":
         case "ping":
            var10000 = new int[]{0, logoBottom};
            break;
         case "speed":
            var10000 = new int[]{0, logoBottom + rowH};
            break;
         case "active_modules":
            var10000 = new int[]{0, 0};
            break;
         case "anti_vanish":
            var10000 = new int[]{0, logoBottom + rowH * 2};
            break;
         case "rotation":
            var10000 = new int[]{-1, -2};
            break;
         case "nether_coords":
            var10000 = new int[]{-1, -2 - rowH};
            break;
         case "coordinates":
            var10000 = new int[]{-1, -2 - rowH * 2};
            break;
         case "armor":
            var10000 = new int[]{0, logoBottom + rowH * 2};
            break;
         case "compass":
            var10000 = new int[]{0, 2};
            break;
         case "bed_danger":
            var10000 = new int[]{0, 52};
            break;
         case "potion_timers":
            var10000 = new int[]{0, logoBottom + rowH * 4};
            break;
         case "keystrokes":
            var10000 = new int[]{4, 0};
            break;
         default:
            var10000 = new int[]{0, 0};
      }

      return var10000;
   }

   private static boolean update_Z(String id) {
      HudElementProvider provider = HudElements.get(id);
      if (provider != null) {
         try {
            return provider.defaultEnabled();
         } catch (Throwable t) {
            AutismClientAddon.LOG.warn("[Hud] Addon element '{}' defaultEnabled failed", id, t);
            return false;
         }
      }

      return "active_modules".equals(id)
            || "bed_danger".equals(id)
            || "watermark".equals(id)
            || "fps".equals(id)
            || "speed".equals(id)
            || "coordinates".equals(id)
            || "nether_coords".equals(id)
            || "rotation".equals(id)
            || "compass".equals(id)
            || "anti_vanish".equals(id)
            || "keystrokes".equals(id);
   }

   private static Map<String, String> defaultSettings(String id) {
      Map<String, String> cached = elementSettingsCache.get(id);
      if (cached != null) {
         return cached;
      }

      Map<String, String> settings = new LinkedHashMap<>();
      settings.put("shadow", "true");
      settings.put("use-custom-colors", "false");
      settings.put("background", "true");
      settings.put("background-color", "32191919");
      settings.put("text-color", "FFF3ECE7");
      settings.put("label-color", "FFB79E9E");
      settings.put("value-color", "FFF3ECE7");
      settings.put("accent-color", "FFFF3B3B");
      settings.put("alignment", "Left");
      settings.put("outline", "false");
      settings.put("outline-color", "FF750000");
      settings.put("outline-width", "1");
      settings.put("padding", "1");
      settings.put("vertical-padding", "0");
      settings.put("line-gap", "0");
      if ("watermark".equals(id)) {
         settings.put("shadow", "false");
         settings.put("background", "true");
         settings.put("outline", "false");
         settings.put("padding", "1");
         settings.put("logo-right-padding", "3");
         settings.put("logo-width", "180");
      }

      if ("gif".equals(id)) {
         settings.put("shadow", "false");
         settings.put("background", "false");
         settings.put("outline", "false");
         settings.put("gif-path", "");
         settings.put("gif-width", "120");
         settings.put("gif-height", "120");
         settings.put("gif-keep-aspect", "false");
         settings.put("gif-fps", "0");
         settings.put("gif-quality", "Original");
         settings.put("gif-natural", "true");
      }

      if ("compass".equals(id)) {
         settings.put("padding", "1");
         settings.put("compass-width", "112");
         settings.put("outline", "false");
      }

      if ("armor".equals(id) || "inventory".equals(id)) {
         settings.put("outline", "true");
         settings.put("slot-style", "Flat");
      }

      if ("tps".equals(id)) {
         settings.put("tps-precise", "false");
         settings.put("tps-color-threshold", "true");
         settings.put("tps-show-jitter", "false");
      }

      if ("ping".equals(id)) {
         settings.put("ping-color-threshold", "true");
         settings.put("ping-show-jitter", "true");
      }

      if ("cps".equals(id)) {
         settings.put("show-total", "false");
      }

      if ("durability".equals(id)) {
         settings.put("show-item-name", "false");
         settings.put("low-durability-warn", "true");
      }

      if ("looking_at".equals(id)) {
         settings.put("show-distance", "false");
         settings.put("show-block-id", "false");
      }

      if ("breaking_progress".equals(id)) {
         settings.put("show-block-name", "false");
      }

      if ("weather".equals(id)) {
         settings.put("show-temperature", "false");
      }

      if ("world_time".equals(id)) {
         settings.put("world-time-format", "24h");
         settings.put("show-day", "false");
      }

      if ("real_time".equals(id)) {
         settings.put("real-time-format", "12h");
         settings.put("show-date", "false");
      }

      if ("keystrokes".equals(id)) {
         settings.put("background", "false");
         settings.put("padding", "0");
         settings.put("keystroke-active-color", "FFFF3B3B");
         settings.put("keystroke-idle-color", "B4101014");
         settings.put("keystroke-text-color", "FFFFFFFF");
         settings.put("keystroke-show-space", "true");
         settings.put("keystroke-show-mouse", "true");
         settings.put("keystroke-size", "18");
      }

      if ("active_modules".equals(id) || "coordinates".equals(id) || "nether_coords".equals(id) || "rotation".equals(id)) {
         settings.put("alignment", "Right");
      }

      if ("active_modules".equals(id)) {
         settings.put("use-custom-colors", "true");
         settings.put("module-info", "true");
         settings.put("show-keybind", "false");
         settings.put("sort", "Width");
         settings.put("hidden-modules", "hud");
         settings.put("color-mode", "Rainbow");
         settings.put("flat-color", "FFFF3B3B");
         settings.put("gradient-start-color", "FFFF3B3B");
         settings.put("gradient-end-color", "FFFFD6D6");
         settings.put("module-info-color", "FFB79E9E");
         settings.put("rainbow-speed", "1.0");
         settings.put("rainbow-spread", "0.035");
         settings.put("rainbow-saturation", "0.35");
         settings.put("rainbow-brightness", "1.0");
         settings.put("stair-snap", "2");
      }

      if ("item_counter".equals(id)) {
         settings.put("item-id", "minecraft:totem_of_undying");
         settings.put("item-show-name", "true");
         settings.put("item-count-held", "false");
      }

      if ("memory".equals(id)) {
         settings.put("show-bar", "true");
         settings.put("show-percent", "true");
         settings.put("memory-format", "used/max");
      }

      if ("server_ip".equals(id)) {
         settings.put("show-port", "true");
      }

      if ("fps_graph".equals(id)) {
         settings.put("graph-samples", "100");
         settings.put("show-current-fps", "true");
      }

      Map<String, String> immutable = Collections.unmodifiableMap(new LinkedHashMap<>(settings));
      elementSettingsCache.put(id, immutable);
      return immutable;
   }

   private static int padding(String id) {
      return clamp(intSetting(id, "padding", 0), 0, 16);
   }

   private static int verticalPadding(String id) {
      return clamp(intSetting(id, "vertical-padding", 0), 0, 16);
   }

   private static int baseRowHeight() {
      return THEME.init_I_l(autismclient.gui.f.b.l.read);
   }

   private static int logoWidth(String id) {
      return clamp(intSetting(id, "logo-width", 180), 48, 420);
   }

   private static int logoHeight(int width) {
      return Math.max(1, Math.round(width * 0.0942029F));
   }

   private static int logoBottom() {
      return logoHeight(180) + 2;
   }

   private static int logoRightPadding(String id) {
      return clamp(intSetting(id, "logo-right-padding", 3), 0, 16);
   }

   private static int gifWidth(String id) {
      return clamp(intSetting(id, "gif-width", 120), 16, 512);
   }

   private static int gifHeight(String id) {
      return clamp(intSetting(id, "gif-height", 120), 16, 512);
   }

   private static double gifQualityScale(String id) {
      String value = setting(id, "gif-quality");
      double var10000;
      switch (value == null ? "" : value) {
         case "Low":
            var10000 = 0.25;
            break;
         case "Medium":
            var10000 = 0.5;
            break;
         case "High":
            var10000 = 0.75;
            break;
         default:
            var10000 = 1.0;
      }

      return var10000;
   }

   private static double gifFps(String id) {
      double fps = doubleSetting(id, "gif-fps", 0.0);
      return fps <= 0.0 ? 0.0 : Math.min(60.0, fps);
   }

   private static void syncGifNaturalSize(String id, String path) {
      int[] natural = autismclient.util.gif.GifTextureManager.naturalSize(path);
      if (natural != null && natural[0] > 0 && natural[1] > 0) {
         String signature = path + "|" + natural[0] + "x" + natural[1];
         if (!signature.equals(gifSizeSignatures.get(id))) {
            int maxDim = Math.max(gifWidth(id), gifHeight(id));
            double scale = (double)maxDim / Math.max(natural[0], natural[1]);
            int w = clamp((int)Math.round(natural[0] * scale), 16, 512);
            int h = clamp((int)Math.round(natural[1] * scale), 16, 512);
            gifSizeSignatures.put(id, signature);
            setSetting(id, "gif-width", Integer.toString(w));
            setSetting(id, "gif-height", Integer.toString(h));
         }
      }
   }

   private static int compassWidth(String id) {
      return clamp(intSetting(id, "compass-width", 112), 72, 200);
   }

   static int screenWidth() {
      if (MC.getWindow() == null) {
         return 854;
      }

      int sw = cA.init_I();
      return sw > 0 ? sw : 854;
   }

   static int screenHeight() {
      if (MC.getWindow() == null) {
         return 480;
      }

      int sh = cA.load_I();
      return sh > 0 ? sh : 480;
   }

   private static int anchoredX(String anchor, int x, int width) {
      String normalized = anchor == null ? "" : anchor.toUpperCase(Locale.ROOT);
      if (normalized.contains("RIGHT")) {
         return screenWidth() + x - width;
      } else {
         return normalized.contains("CENTER") ? (screenWidth() - width) / 2 + x : x;
      }
   }

   private static int anchoredY(String anchor, int y, int height) {
      String normalized = anchor == null ? "" : anchor.toUpperCase(Locale.ROOT);
      if (normalized.contains("BOTTOM")) {
         return screenHeight() + y - height;
      } else {
         return normalized.contains("MIDDLE") ? (screenHeight() - height) / 2 + y : y;
      }
   }

   private static int clampX(String id, int x, int width) {
      return clamp(x, minX(id), maxX(id, screenWidth(), width));
   }

   private static int clampY(String id, int y, int height) {
      return clamp(y, minY(id), maxY(id, screenHeight(), height));
   }

   private static int minX(String id) {
      return "watermark".equals(id) ? 0 : 1;
   }

   private static int minY(String id) {
      return !"watermark".equals(id) && !"active_modules".equals(id) ? 2 : 0;
   }

   private static int maxX(String id, int screenW, int width) {
      int safe = minX(id);
      return Math.max(safe, screenW - Math.max(0, width) - safe);
   }

   private static int maxY(String id, int screenH, int height) {
      int safe = minY(id);
      return Math.max(safe, screenH - Math.max(0, height) - safe);
   }

   private static int activeModulesColor(String key, int fallback) {
      return colorSetting("active_modules", key, fallback);
   }

   private static int colorSetting(String id, String key, int fallback) {
      if (!boolSetting(id, "use-custom-colors")) {
         int themed = parseColor(defaultSetting(id, key), fallback);
         return ThemePaletteManager.recolor(themed, colorRoleFor(key));
      } else {
         return parseColor(setting(id, key), fallback);
      }
   }

   private static ThemePaletteManager.ColorRole colorRoleFor(String key) {
      if (key == null) {
         return ThemePaletteManager.ColorRole.ACCENT;
      }

      ThemePaletteManager.ColorRole var10000;
      switch (key) {
         case "outline-color":
            var10000 = ThemePaletteManager.ColorRole.OUTLINE;
            break;
         case "label-color":
         case "value-color":
         case "text-color":
         case "module-info-color":
         case "keystroke-text-color":
            var10000 = ThemePaletteManager.ColorRole.TEXT;
            break;
         case "background-color":
         case "keystroke-idle-color":
            var10000 = ThemePaletteManager.ColorRole.BACKDROP;
            break;
         default:
            var10000 = ThemePaletteManager.ColorRole.ACCENT;
      }

      return var10000;
   }

   private static int editorBackdropColor(boolean selected) {
      return ThemePaletteManager.recolor(selected ? 942280982 : 571675672, ThemePaletteManager.ColorRole.BACKDROP);
   }

   private static int withAlpha(int color, float alpha) {
      if (alpha >= 0.999F) {
         return color;
      }

      int a = (int)((color >>> 24 & 0xFF) * Math.max(0.0F, Math.min(1.0F, alpha)));
      return color & 16777215 | a << 24;
   }

   private static int lerpColor(int from, int to, double t) {
      double clamped = Math.max(0.0, Math.min(1.0, t));
      int a0 = from >>> 24 & 0xFF;
      int r0 = from >>> 16 & 0xFF;
      int g0 = from >>> 8 & 0xFF;
      int b0 = from & 0xFF;
      int a = (int)Math.round(a0 + ((to >>> 24 & 0xFF) - a0) * clamped);
      int r = (int)Math.round(r0 + ((to >>> 16 & 0xFF) - r0) * clamped);
      int g = (int)Math.round(g0 + ((to >>> 8 & 0xFF) - g0) * clamped);
      int b = (int)Math.round(b0 + ((to & 0xFF) - b0) * clamped);
      return a << 24 | r << 16 | g << 8 | b;
   }

   private static int mixColor(int color, int base, double amount) {
      return lerpColor(color, base, amount);
   }

   private static void ensureHudElements(AutismConfig config) {
      if (config.hudElements == null) {
         config.hudElements = new LinkedHashMap<>();
      }
   }

   private static boolean booleanSetting(Module module, String id, boolean fallback) {
      AutismHudManager.SettingValueCache option = new AutismHudManager.SettingValueCache(module, id);
      return !option.present ? fallback : Boolean.parseBoolean(option.value);
   }

   private static boolean parseBoolean(String value, boolean fallback) {
      return value == null ? fallback : Boolean.parseBoolean(value);
   }

   private static int parseIntOr(String value, int fallback) {
      try {
         return Integer.parseInt(value);
      } catch (Exception ignored) {
         return fallback;
      }
   }

   public static int parseColor(String value, int fallback) {
      if (value != null && !value.isBlank()) {
         String normalized = value.startsWith("#") ? value.substring(1) : value;

         try {
            long parsed = Long.parseLong(normalized, 16);
            if (normalized.length() == 6) {
               parsed |= 4278190080L;
            }

            return (int)parsed;
         } catch (Exception ignored) {
            return fallback;
         }
      } else {
         return fallback;
      }
   }

   private static int clamp(int value, int min, int max) {
      return Math.max(min, Math.min(max, value));
   }

   private static double clampDouble(double value, double min, double max) {
      return Math.max(min, Math.min(max, value));
   }

   private static boolean isHovered(int mouseX, int mouseY, AutismHudManager.HudElementBounds bounds) {
      return isInside(mouseX, mouseY, bounds.init_I, bounds.load, bounds.read, bounds.write);
   }

   private static boolean isInside(int mouseX, int mouseY, int x, int y, int w, int h) {
      return mouseX >= x && mouseX < x + w && mouseY >= y && mouseY < y + h;
   }

   private static boolean isRightAnchored(String anchor) {
      return anchor != null && anchor.toUpperCase(Locale.ROOT).contains("RIGHT");
   }

   private static boolean isShownInFpsElement(String id) {
      return ("tps".equals(id) || "ping".equals(id)) && getState("fps").enabled;
   }

   private static int stableWidth(int measuredWidth) {
      long now = System.currentTimeMillis();
      int width = Math.max(1, measuredWidth);
      if (width >= heldWidth) {
         heldWidth = width;
         heldWidthUntil = now + 350L;
         return heldWidth;
      }

      if (now < heldWidthUntil) {
         return heldWidth;
      }

      heldWidth = Math.max(width, heldWidth - 6);
      return heldWidth;
   }

   private static void init_V_GuiGraphicsExtract_3(
      GuiGraphicsExtractor context, Font font, String id, AutismHudManager.LayoutRect rect, int color, List<AutismHudManager.LayoutRect> blockers
   ) {
      List<AutismHudManager.LayoutRect> occluders = occludersFor(font, id, rect);
      fillAroundOccluders(context, rect, occluders, color);
      load(context, rect, blockers != null ? blockers : outlineBlockers(font, id, rect), color);
   }

   private static AutismHudManager.LayoutRect clampToSafeArea(AutismHudManager.LayoutRect rect) {
      if (rect == null) {
         return null;
      }

      int screenW = screenWidth();
      int screenH = screenHeight();
      int safeX = minX(rect.id());
      int safeY = minY(rect.id());
      int x = rect.read();
      int y = rect.write();
      int right = rect.right();
      int bottom = rect.bottom();
      if (x == safeX) {
         x = 0;
      }

      if (y == safeY) {
         y = 0;
      }

      if (right == screenW - safeX) {
         right = screenW;
      }

      if (bottom == screenH - safeY) {
         bottom = screenH;
      }

      x = clamp(x, 0, screenW);
      y = clamp(y, 0, screenH);
      right = clamp(right, x, screenW);
      bottom = clamp(bottom, y, screenH);
      return new AutismHudManager.LayoutRect(rect.id(), x, y, Math.max(0, right - x), Math.max(0, bottom - y));
   }

   private static AutismHudManager.LayoutRect expandForOutline(AutismHudManager.LayoutRect contentRect, AutismHudManager.ElementStyle style) {
      if (contentRect == null) {
         return null;
      }

      AutismHudManager.LayoutRect rect = contentRect;
      if (style.outline()) {
         int pad = 2;
         rect = new AutismHudManager.LayoutRect(
            contentRect.id(), contentRect.read() - pad, contentRect.write() - pad, contentRect.width() + pad * 2, contentRect.height() + pad * 2
         );
      }

      return clampToSafeArea(rect);
   }

   private static Map<String, Integer> buildElementOrder() {
      Map<String, Integer> index = new HashMap<>();

      for (int i = 0; i < BUILTIN_ELEMENT_IDS.size(); i++) {
         index.put(BUILTIN_ELEMENT_IDS.get(i), i);
      }

      return index;
   }

   private static List<AutismHudManager.PlacedHudItem> buildPlacedItems(Font font, boolean editor) {
      boolean changed = false;

      for (int i = 0; i < BUILTIN_ELEMENT_IDS.size(); i++) {
         String id = BUILTIN_ELEMENT_IDS.get(i);
         boolean hiddenBedDanger = "bed_danger".equals(id) && !editor;
         if (hiddenBedDanger && !AutismLiteVariant.enabled()) {
            hiddenBedDanger = !autismclient.modules.BedDangerHudRenderer.init();
         }

         AutismHudManager.CachedElementLayout current = !isShownInFpsElement(id) && getState(id).enabled && !hiddenBedDanger
            ? cachedLayout(id, font)
            : null;
         if (visibleLayouts[i] != current) {
            visibleLayouts[i] = current;
            changed = true;
         }
      }

      long occluderSignature = occluderSignature();
      if (!changed && occluderSignature == cachedPlacedItemsSignature) {
         return cachedPlacedItems;
      }

      List<AutismHudManager.PlacedHudItem> entries = new ArrayList<>();

      for (int i = 0; i < BUILTIN_ELEMENT_IDS.size(); i++) {
         String other = BUILTIN_ELEMENT_IDS.get(i);
         AutismHudManager.CachedElementLayout cached = visibleLayouts[i];
         if (cached != null) {
            AutismHudManager.ElementStyle style = cached.style();
            if (style.background() || style.outline()) {
               for (AutismHudManager.LayoutRect rect : init_List_Str_Font(other, font)) {
                  entries.add(new AutismHudManager.PlacedHudItem(other, i, rect, style.background()));
               }
            }
         }
      }

      cachedPlacedItemsSignature = occluderSignature;
      cachedPlacedItems = List.copyOf(entries);
      return cachedPlacedItems;
   }

   private static long occluderSignature() {
      long signature = activeOccluders.size();

      for (AutismHudManager.HudElementBounds bounds : activeOccluders) {
         if (bounds != null) {
            signature = signature * 31L + bounds.read();
            signature = signature * 31L + bounds.write();
            signature = signature * 31L + bounds.update();
            signature = signature * 31L + bounds.sync();
         }
      }

      return signature;
   }

   private static List<AutismHudManager.LayoutRect> occludersFor(Font font, String id, AutismHudManager.LayoutRect self) {
      List<AutismHudManager.LayoutRect> occluders = new ArrayList<>();
      List<AutismHudManager.PlacedHudItem> snapshot = placedItemsSnapshot;
      if (snapshot != null) {
         int selfOrder = ELEMENT_ORDER.getOrDefault(id, -1);

         for (AutismHudManager.PlacedHudItem entry : snapshot) {
            if (entry.background() && entry.order() <= selfOrder) {
               AutismHudManager.LayoutRect rect = entry.rect();
               if (!rect.sameRect(self) && (entry.order() != selfOrder || sortsBefore(rect, self)) && rect.intersects(self)) {
                  occluders.add(rect);
               }
            }
         }

         return occluders;
      } else {
         int selfOrder = BUILTIN_ELEMENT_IDS.indexOf(id);

         for (String other : BUILTIN_ELEMENT_IDS) {
            if (!isShownInFpsElement(other) && getState(other).enabled && boolSetting(other, "background")) {
               int otherOrder = BUILTIN_ELEMENT_IDS.indexOf(other);
               if (otherOrder <= selfOrder) {
                  for (AutismHudManager.LayoutRect rect : init_List_Str_Font(other, font)) {
                     if (!rect.sameRect(self) && (otherOrder != selfOrder || sortsBefore(rect, self)) && rect.intersects(self)) {
                        occluders.add(rect);
                     }
                  }
               }
            }
         }

         return occluders;
      }
   }

   private static boolean sortsBefore(AutismHudManager.LayoutRect rect, AutismHudManager.LayoutRect self) {
      if (rect.write() != self.write()) {
         return rect.write() < self.write();
      } else if (rect.read() != self.read()) {
         return rect.read() < self.read();
      } else {
         return rect.width() != self.width() ? rect.width() < self.width() : rect.height() < self.height();
      }
   }

   private static void fillAroundOccluders(GuiGraphicsExtractor context, AutismHudManager.LayoutRect rect, List<AutismHudManager.LayoutRect> occluders, int color) {
      if (occluders.isEmpty()) {
         autismclient.gui.f.b.k.init(context, rect.read(), rect.write(), rect.right(), rect.bottom(), color);
      } else {
         List<Integer> cuts = new ArrayList<>();
         cuts.add(rect.write());
         cuts.add(rect.bottom());

         for (AutismHudManager.LayoutRect other : occluders) {
            int top = clamp(other.write(), rect.write(), rect.bottom());
            int bottom = clamp(other.bottom(), rect.write(), rect.bottom());
            if (top < bottom) {
               cuts.add(top);
               cuts.add(bottom);
            }
         }

         cuts.sort(Integer::compareTo);

         for (int i = 0; i < cuts.size() - 1; i++) {
            int bandY = cuts.get(i);
            int bandBottom = cuts.get(i + 1);
            if (bandY < bandBottom) {
               List<int[]> blocked = new ArrayList<>();

               for (AutismHudManager.LayoutRect other : occluders) {
                  if (other.write() < bandBottom && other.bottom() > bandY) {
                     int left = clamp(other.read(), rect.read(), rect.right());
                     int right = clamp(other.right(), rect.read(), rect.right());
                     if (left < right) {
                        blocked.add(new int[]{left, right});
                     }
                  }
               }

               drawUnblockedSpans(
                  context, rect.read(), rect.right(), blocked, 0, (a, b) -> autismclient.gui.f.b.k.init(context, a, bandY, b, bandBottom, color)
               );
            }
         }
      }
   }

   private static void load(GuiGraphicsExtractor context, AutismHudManager.LayoutRect rect, List<AutismHudManager.LayoutRect> blockers, int color) {
      for (AutismHudManager.LayoutRect other : blockers) {
         if (rect.right() <= other.read() && other.read() - rect.right() <= 1) {
            int y0 = Math.max(rect.write(), other.write());
            int y1 = Math.min(rect.bottom(), other.bottom());
            if (y0 < y1 && rect.right() < other.read()) {
               autismclient.gui.f.b.k.init(context, rect.right(), y0, other.read(), y1, color);
            }
         } else if (other.right() <= rect.read() && rect.read() - other.right() <= 1) {
            int y0 = Math.max(rect.write(), other.write());
            int y1 = Math.min(rect.bottom(), other.bottom());
            if (y0 < y1 && other.right() < rect.read()) {
               autismclient.gui.f.b.k.init(context, other.right(), y0, rect.read(), y1, color);
            }
         }

         if (rect.bottom() <= other.write() && other.write() - rect.bottom() <= 1) {
            int x0 = Math.max(rect.read(), other.read());
            int x1 = Math.min(rect.right(), other.right());
            if (x0 < x1 && rect.bottom() < other.write()) {
               autismclient.gui.f.b.k.init(context, x0, rect.bottom(), x1, other.write(), color);
            }
         } else if (other.bottom() <= rect.write() && rect.write() - other.bottom() <= 1) {
            int x0 = Math.max(rect.read(), other.read());
            int x1 = Math.min(rect.right(), other.right());
            if (x0 < x1 && other.bottom() < rect.write()) {
               autismclient.gui.f.b.k.init(context, x0, other.bottom(), x1, rect.write(), color);
            }
         }
      }
   }

   private static void drawElementOutline(GuiGraphicsExtractor context, Font font, String id, int x, int y, int w, int h, int color, int width) {
      AutismHudManager.LayoutRect rect = new AutismHudManager.LayoutRect(id, x, y, w, h);
      drawOutline(context, rect, outlineBlockers(font, id, rect), color, width);
   }

   private static void drawOutline(GuiGraphicsExtractor context, AutismHudManager.LayoutRect rect, List<AutismHudManager.LayoutRect> blockers, int color, int width) {
      int size = Math.max(1, width);
      int screenW = screenWidth();
      int screenH = screenHeight();
      int x0 = clamp(rect.read(), 0, screenW);
      int x1 = clamp(rect.right(), 0, screenW);
      int y0 = clamp(rect.write(), 0, screenH);
      int y1 = clamp(rect.bottom(), 0, screenH);
      if (x0 < x1 && y0 < y1) {
         if (y0 > 0) {
            drawOutlineEdge(context, blockers, x0, x1, y0, y0, size, color, true);
         }

         if (y1 < screenH) {
            drawOutlineEdge(context, blockers, x0, x1, Math.max(y0, y1 - size), y1, size, color, false);
         }

         if (x0 > 0) {
            load(context, blockers, y0, y1, x0, x0, size, color, true);
         }

         if (x1 < screenW) {
            load(context, blockers, y0, y1, Math.max(x0, x1 - size), x1, size, color, false);
         }
      }
   }

   private static void drawOutlineEdge(
      GuiGraphicsExtractor context, List<AutismHudManager.LayoutRect> blockers, int fromX, int toX, int drawY, int edgeY, int size, int color, boolean top
   ) {
      List<int[]> blocked = new ArrayList<>();

      for (AutismHudManager.LayoutRect other : blockers) {
         boolean connected = top ? other.write() < edgeY && other.bottom() >= edgeY - 1 : other.write() <= edgeY + 1 && other.bottom() > edgeY;
         if (connected) {
            int overlapStart = Math.max(fromX, other.read());
            int overlapEnd = Math.min(toX, other.right());
            if (overlapStart < overlapEnd) {
               blocked.add(new int[]{overlapStart, overlapEnd});
            }
         }
      }

      drawUnblockedSpans(context, fromX, toX, blocked, size, (a, b) -> autismclient.gui.f.b.k.init(context, a, drawY, b, drawY + size, color));
   }

   private static void load(
      GuiGraphicsExtractor context, List<AutismHudManager.LayoutRect> blockers, int fromY, int toY, int drawX, int edgeX, int size, int color, boolean left
   ) {
      List<int[]> blocked = new ArrayList<>();

      for (AutismHudManager.LayoutRect other : blockers) {
         boolean connected = left ? other.read() < edgeX && other.right() >= edgeX - 1 : other.read() <= edgeX + 1 && other.right() > edgeX;
         if (connected) {
            int overlapStart = Math.max(fromY, other.write());
            int overlapEnd = Math.min(toY, other.bottom());
            if (overlapStart < overlapEnd) {
               blocked.add(new int[]{overlapStart, overlapEnd});
            }
         }
      }

      drawUnblockedSpans(context, fromY, toY, blocked, size, (a, b) -> autismclient.gui.f.b.k.init(context, drawX, a, drawX + size, b, color));
   }

   private static List<AutismHudManager.LayoutRect> outlineBlockers(Font font, String id, AutismHudManager.LayoutRect self) {
      List<AutismHudManager.LayoutRect> blockers = new ArrayList<>();
      List<AutismHudManager.PlacedHudItem> snapshot = placedItemsSnapshot;
      if (snapshot != null) {
         for (AutismHudManager.PlacedHudItem entry : snapshot) {
            if (!entry.id().equals(id) || "active_modules".equals(entry.id())) {
               AutismHudManager.LayoutRect rect = entry.rect();
               if (self == null || !rect.sameRect(self)) {
                  blockers.add(rect);
               }
            }
         }

         return blockers;
      } else {
         for (String other : BUILTIN_ELEMENT_IDS) {
            if (!isShownInFpsElement(other) && getState(other).enabled && hasBackgroundOrOutline(other) && (!other.equals(id) || "active_modules".equals(other))) {
               for (AutismHudManager.LayoutRect rect : init_List_Str_Font(other, font)) {
                  if (self == null || !rect.sameRect(self)) {
                     blockers.add(rect);
                  }
               }
            }
         }

         return blockers;
      }
   }

   private static List<AutismHudManager.LayoutRect> init_List_Str_Font(String id, Font font) {
      AutismHudManager.CachedElementLayout cached = cachedLayout(id, font);
      if ("active_modules".equals(id)) {
         List<AutismHudManager.LayoutRect> rects = visualRects(id, cached);
         if (!activeOccluders.isEmpty() && !rects.isEmpty()) {
            List<AutismHudManager.LayoutRect> visible = new ArrayList<>(rects.size());

            for (AutismHudManager.LayoutRect rect : rects) {
               if (!rectOverlapsOccluder(rect)) {
                  visible.add(rect);
               }
            }

            return visible;
         } else {
            return rects;
         }
      } else {
         AutismHudManager.HudElementBounds bounds = cached.layout().toBounds();
         return overlapsOccluder(bounds)
            ? List.of()
            : List.of(
               expandForOutline(
                  new AutismHudManager.LayoutRect(id, bounds.read(), bounds.write(), bounds.update(), bounds.sync()), cached.style()
               )
            );
      }
   }

   private static List<AutismHudManager.LayoutRect> visualRects(String id, AutismHudManager.CachedElementLayout cached) {
      List<AutismHudManager.LayoutRect> content = layoutRects(id, cached);
      if (content.isEmpty()) {
         return content;
      }

      List<AutismHudManager.LayoutRect> visual = new ArrayList<>(content.size());
      AutismHudManager.ElementStyle style = cached.style();

      for (AutismHudManager.LayoutRect rect : content) {
         visual.add(expandForOutline(rect, style));
      }

      return visual;
   }

   private static List<AutismHudManager.LayoutRect> layoutRects(String id, AutismHudManager.CachedElementLayout cached) {
      if (cached.layoutRects != null) {
         return cached.layoutRects;
      }

      List<AutismHudManager.LayoutRect> rects = new ArrayList<>();
      AutismHudManager.ScaledElementBox layout = cached.layout();
      AutismHudManager.ElementStyle style = cached.style();
      int rowH = style.sync();
      int gap = style.write();
      int stairSnap = style.flush();
      int pad = style.padding();
      int groupWidth = -1;

      for (int i = 0; i < cached.lines().size(); i++) {
         int lineW = i < cached.widths().size() ? cached.widths().get(i) : measureLine(MC.font, cached.lines().get(i));
         int rowW = Math.max(1, lineW + pad * 2);
         if (groupWidth >= 0 && Math.abs(groupWidth - rowW) <= stairSnap) {
            rowW = groupWidth;
         } else {
            groupWidth = rowW;
         }

         int rowX = alignedRowX(style.alignment(), layout.read(), layout.width(), rowW);
         int rowY = layout.write() + i * (rowH + gap);
         rects.add(new AutismHudManager.LayoutRect(id, rowX, rowY, rowW, rowH));
      }

      cached.layoutRects = List.copyOf(rects);
      return cached.layoutRects;
   }

   private static int alignedRowX(String alignment, int renderX, int unscaledW, int rowW) {
      if ("Center".equals(alignment)) {
         return renderX + Math.max(0, (unscaledW - rowW) / 2);
      } else {
         return "Right".equals(alignment) ? renderX + Math.max(0, unscaledW - rowW) : renderX;
      }
   }

   private static boolean hasBackgroundOrOutline(String id) {
      AutismHudManager.ElementStyle style = cachedLayout(id, MC.font).style();
      return style.background() || style.outline();
   }

   private static boolean overlapsOccluder(AutismHudManager.HudElementBounds bounds) {
      if (bounds != null && !activeOccluders.isEmpty()) {
         for (AutismHudManager.HudElementBounds occluder : activeOccluders) {
            if (bounds.intersects(occluder)) {
               return true;
            }
         }

         return false;
      } else {
         return false;
      }
   }

   private static boolean rectOverlapsOccluder(AutismHudManager.LayoutRect rect) {
      if (rect != null && !activeOccluders.isEmpty()) {
         for (AutismHudManager.HudElementBounds occluder : activeOccluders) {
            if (rect.read() < occluder.init_I() && rect.right() > occluder.read() && rect.write() < occluder.load() && rect.bottom() > occluder.write()) {
               return true;
            }
         }

         return false;
      } else {
         return false;
      }
   }

   private static int dodgeOffsetY(AutismHudManager.HudElementBounds bounds) {
      if (bounds != null && !activeOccluders.isEmpty() && overlapsOccluder(bounds)) {
         int sh = screenHeight();
         int down = dodgeShift(bounds, 1, sh);
         int up = dodgeShift(bounds, -1, sh);
         boolean haveDown = down != -2147483648;
         boolean haveUp = up != -2147483648;
         if (haveDown && haveUp) {
            return Math.abs(down) <= Math.abs(up) ? down : up;
         } else if (haveDown) {
            return down;
         } else {
            return haveUp ? up : 0;
         }
      } else {
         return 0;
      }
   }

   private static int dodgeShift(AutismHudManager.HudElementBounds bounds, int dir, int sh) {
      int shift = 0;

      for (int iter = 0; iter < 32; iter++) {
         AutismHudManager.HudElementBounds moved = new AutismHudManager.HudElementBounds(bounds.id(), bounds.read(), bounds.write() + shift, bounds.update(), bounds.sync());
         AutismHudManager.HudElementBounds hit = null;

         for (AutismHudManager.HudElementBounds occ : activeOccluders) {
            if (moved.intersects(occ)) {
               hit = occ;
               break;
            }
         }

         if (hit == null) {
            int top = bounds.write() + shift;
            return top >= 0 && top + bounds.sync() <= sh ? shift : -2147483648;
         }

         int next = dir > 0 ? hit.load() - bounds.write() + 1 : hit.write() - bounds.sync() - bounds.write() - 1;
         if (dir > 0 ? next <= shift : next >= shift) {
            next = shift + dir;
         }

         shift = next;
         int top = bounds.write() + shift;
         if (top < 0 || top + bounds.sync() > sh) {
            return -2147483648;
         }
      }

      return -2147483648;
   }

   private static void drawUnblockedSpans(
      GuiGraphicsExtractor context, int from, int to, List<int[]> blocked, int capOverlap, AutismHudManager.LineDrawCallback drawer
   ) {
      if (from < to) {
         if (blocked.isEmpty()) {
            drawer.draw(from, to);
         } else {
            blocked.sort(Comparator.comparingInt(a -> a[0]));
            int cursor = from;

            for (int[] block : blocked) {
               int start = clamp(block[0], from, to);
               int end = clamp(block[1], from, to);
               if (cursor < start) {
                  drawer.draw(cursor, Math.min(to, start + capOverlap));
               }

               cursor = Math.max(cursor, end);
               if (cursor >= to) {
                  return;
               }
            }

            if (cursor < to) {
               drawer.draw(Math.max(from, cursor - capOverlap), to);
            }
         }
      }
   }

   private static void drawRectOutline(GuiGraphicsExtractor context, int x, int y, int w, int h, int color, int width) {
      int size = Math.max(1, width);
      int screenW = screenWidth();
      int screenH = screenHeight();
      int x0 = clamp(x, 0, screenW);
      int y0 = clamp(y, 0, screenH);
      int x1 = clamp(x + w, x0, screenW);
      int y1 = clamp(y + h, y0, screenH);
      if (x0 < x1 && y0 < y1) {
         int edge = Math.min(size, Math.max(1, Math.min(x1 - x0, y1 - y0)));
         autismclient.gui.f.b.k.init(context, x0, y0, x1, Math.min(y1, y0 + edge), color);
         if (y1 - y0 > edge) {
            autismclient.gui.f.b.k.init(context, x0, Math.max(y0, y1 - edge), x1, y1, color);
         }

         autismclient.gui.f.b.k.init(context, x0, y0, Math.min(x1, x0 + edge), y1, color);
         if (x1 - x0 > edge) {
            autismclient.gui.f.b.k.init(context, Math.max(x0, x1 - edge), y0, x1, y1, color);
         }
      }
   }

   private static AutismHudManager.ElementStyle elementStyle(String id) {
      return new AutismHudManager.ElementStyle(
         boolSetting(id, "background"),
         boolSetting(id, "outline"),
         boolSetting(id, "shadow"),
         Math.max(1, intSetting(id, "outline-width", 1)),
         padding(id),
         verticalPadding(id),
         lineGap(id),
         lineHeight(id),
         rowHeight(id),
         stairSnap(id),
         setting(id, "alignment"),
         colorSetting(id, "background-color", 840505625),
         colorSetting(id, "outline-color", "active_modules".equals(id) ? -7397596 : -9109504),
         colorSetting(id, "accent-color", -50373),
         colorSetting(id, "label-color", -4743522)
      );
   }

   public record HudElementBounds(String id, int init_I, int load, int read, int write) {
      public int init_I() {
         return this.init_I + this.read;
      }

      public int load() {
         return this.load + this.write;
      }

      public boolean intersects(AutismHudManager.HudElementBounds other) {
         return other != null && this.init_I < other.init_I() && this.init_I() > other.read() && this.load < other.load() && this.load() > other.write();
      }

      public int read() {
         return this.init_I;
      }

      public int write() {
         return this.load;
      }

      public int update() {
         return this.read;
      }

      public int sync() {
         return this.write;
      }
   }

   private static final class CachedElementLayout {
      private final AutismHudManager.LayoutSignature signature;
      private final List<AutismHudManager.ColoredTextLine> lines;
      private final List<Integer> widths;
      private final AutismHudManager.ScaledElementBox layout;
      private final AutismHudManager.ElementStyle style;
      private long nextCheckAt;
      private List<AutismHudManager.LayoutRect> layoutRects;

      private CachedElementLayout(
         AutismHudManager.LayoutSignature signature, List<AutismHudManager.ColoredTextLine> lines, List<Integer> widths, AutismHudManager.ScaledElementBox layout, AutismHudManager.ElementStyle style
      ) {
         this.signature = signature;
         this.lines = lines;
         this.widths = widths;
         this.layout = layout;
         this.style = style;
      }

      AutismHudManager.LayoutSignature signature() {
         return this.signature;
      }

      List<AutismHudManager.ColoredTextLine> lines() {
         return this.lines;
      }

      List<Integer> widths() {
         return this.widths;
      }

      AutismHudManager.ScaledElementBox layout() {
         return this.layout;
      }

      AutismHudManager.ElementStyle style() {
         return this.style;
      }
   }

   private record LayoutSignature(
      boolean enabled, String anchor, int x, int load, int read, long nowBucket, int write, int update, int sync, int flush, int close, int open, int poll
   ) {
   }

   private record RainbowParams(
      String colorMode, float phase, float spread, float saturation, float brightness, int flatColor, int gradientStartColor, int gradientEndColor, int valueColor, double moduleSpan
   ) {
   }

   @FunctionalInterface
   private interface LineDrawCallback {
      void draw(int var1, int var2);
   }

   private record LayoutRect(String id, int init_I, int load, int read, int write) {
      int right() {
         return this.init_I + this.read;
      }

      int bottom() {
         return this.load + this.write;
      }

      boolean sameRect(AutismHudManager.LayoutRect other) {
         return other != null && this.init_I == other.init_I && this.load == other.load && this.read == other.read && this.write == other.write;
      }

      boolean intersects(AutismHudManager.LayoutRect other) {
         return other != null
            && this.init_I < other.right()
            && this.right() > other.read()
            && this.load < other.bottom()
            && this.bottom() > other.write();
      }

      public int read() {
         return this.init_I;
      }

      public int write() {
         return this.load;
      }

      public int width() {
         return this.read;
      }

      public int height() {
         return this.write;
      }
   }

   private record PlacedHudItem(String id, int order, AutismHudManager.LayoutRect rect, boolean background) {
   }

   private static final class ColoredTextLine {
      private final List<AutismHudManager.ColoredSegment> segments = new ArrayList<>();
      private int[] segmentWidths;

      void append(String text, int color) {
         if (text != null && !text.isEmpty()) {
            this.segments.add(new AutismHudManager.ColoredSegment(text, color));
         }
      }

      String text() {
         StringBuilder builder = new StringBuilder();

         for (AutismHudManager.ColoredSegment segment : this.segments) {
            builder.append(segment.text);
         }

         return builder.toString();
      }
   }

   private record ColoredSegment(String text, int color) {
   }

   private record ElementStyle(
      boolean background,
      boolean outline,
      boolean shadow,
      int outlineWidth,
      int padding,
      int verticalPadding,
      int write,
      int update,
      int sync,
      int flush,
      String alignment,
      int close,
      int open,
      int poll,
      int push
   ) {
   }

   public record ScaledElementBox(String id, int init_I, int load, int read, int write, double scale) {
      public int init_I() {
         return (int)Math.ceil(this.read * this.scale);
      }

      public int load() {
         return (int)Math.ceil(this.write * this.scale);
      }

      public AutismHudManager.HudElementBounds toBounds() {
         return new AutismHudManager.HudElementBounds(this.id, this.init_I, this.load, this.init_I(), this.load());
      }

      public int read() {
         return this.init_I;
      }

      public int write() {
         return this.load;
      }

      public int width() {
         return this.read;
      }

      public int height() {
         return this.write;
      }
   }

   private record SettingValueCache(boolean present, String value) {
      SettingValueCache(Module module, String id) {
         this(module != null && module.setting(id) != null, module != null && module.setting(id) != null ? module.value(id) : "");
      }
   }
}

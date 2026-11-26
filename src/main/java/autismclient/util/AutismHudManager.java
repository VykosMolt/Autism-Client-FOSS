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
   public static final String init_Str = "active_modules";
   public static final String load_Str = "tps";
   public static final String read_Str = "coordinates";
   public static final String write_Str = "nether_coords";
   public static final String update_Str = "fps";
   public static final String sync_Str = "ping";
   public static final String flush_Str = "speed";
   public static final String close_Str = "game_mode";
   public static final String open_Str = "durability";
   public static final String poll_Str = "looking_at";
   public static final String push_Str = "breaking_progress";
   public static final String pop_Str = "server";
   public static final String tick_Str = "weather";
   public static final String run_Str = "biome";
   public static final String get_Str = "world_time";
   public static final String set_Str = "real_time";
   public static final String put_Str = "rotation";
   public static final String add_Str = "watermark";
   public static final String clear_Str = "armor";
   public static final String reset_Str = "inventory";
   public static final String start_Str = "item_counter";
   public static final String stop_Str = "potion_timers";
   public static final String next_Str = "compass";
   public static final String prev_Str = "anti_vanish";
   public static final String valid_Str = "cps";
   public static final String size_Str = "keystrokes";
   public static final String data_Str = "memory";
   public static final String value_Str = "server_ip";
   public static final String cache_Str = "server_brand";
   public static final String state_Str = "fps_graph";
   public static final String buffer_Str = "bed_danger";
   public static final String entry_Str = "gif";
   private static final String node_Str = "padding";
   private static final String task_Str = "vertical-padding";
   private static final String pool_Str = "outline";
   private static final String index_Str = "outline-color";
   private static final String count = "background";
   private static final String flags = "compass-width";
   private static final String f_c_Str = "rainbow-speed";
   private static final String f_d_Str = "rainbow-spread";
   private static final String f_e_Str = "rainbow-saturation";
   private static final String f_f_Str = "rainbow-brightness";
   private static final String f_l_Str = "stair-snap";
   private static final String f_m_Str = "logo-width";
   private static final String f_n_Str = "logo-right-padding";
   private static final String f_o_Str = "keystroke-active-color";
   private static final String f_p_Str = "keystroke-idle-color";
   private static final String f_q_Str = "keystroke-text-color";
   private static final String f_r_Str = "keystroke-show-space";
   private static final String f_s_Str = "keystroke-show-mouse";
   private static final String f_t_Str = "keystroke-size";
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
   private static final Minecraft init_Minecraft = Minecraft.getInstance();
   private static long init_J;
   private static final autismclient.gui.f.b.CharEncoder init_CharEncoder = new autismclient.gui.f.b.CharEncoder();
   private static final DateTimeFormatter init_DateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss", Locale.ROOT);
   private static final Identifier init_Identifier = Identifier.fromNamespaceAndPath("autismclient", "textures/gui/hud/autismclient_hud.png");
   private static final int init_I = 1600;
   private static final int load_I = 151;
   private static final int update_I = 1;
   private static final int sync_I = 1;
   private static final int flush_I = 2;
   private static final int close_I = 2;
   private static final List<String> init_List = List.of(
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
   private static double init_D;
   private static double load_D;
   private static long load_J = -9223372036854775808L;
   private static boolean init_Z;
   private static double read_D;
   private static double write_D = 1.0;
   private static int open_I = -2147483648;
   private static final Map<String, AutismHudManager.Inner_CachePool> init_Map = new HashMap<>();
   private static final Map<String, Map<String, String>> load_Map = new HashMap<>();
   private static long read_J;
   private static int poll_I;
   private static long write_J;
   private static List<AutismHudManager.Inner_BufferHelper> load_List = List.of();
   private static boolean load_Z;
   private static List<String> read_List = init_List;
   private static int push_I = -1;
   private static int pop_I = -1;
   private static final Map<String, AutismConfig.HudElementState> read_Map = new HashMap<>();
   private static AutismConfig init_AutismConfig;
   private static int tick_I;
   private static boolean read_Z;
   private static boolean write_Z;
   private static long update_J;
   private static int run_I;
   private static int get_I;
   private static int set_I;
   private static int put_I;
   private static int add_I;
   private static int clear_I;
   private static int reset_I;
   private static int start_I;
   private static int stop_I;
   private static final String[] init_StrArr = new String[]{"S", "SW", "W", "NW", "N", "NE", "E", "SE"};
   private static final int[] init_IArr = new int[init_StrArr.length];
   private static Font init_Font;
   private static int next_I = -2147483648;
   private static final float[] init_FArr = new float[7];
   private static long get_J;
   private static final int entry_I = 200;
   private static final int[] load_IArr = new int[200];
   private static int node_I;
   private static int task_I;
   private static double flush_D = 20.0;
   private static long set_J;
   private static int pool_I;
   private static final Map<String, String> write_Map = new HashMap<>();
   private static final Map<String, Integer> update_Map = init_Map();
   private static List<AutismHudManager.Inner_JsonUtil> write_List;
   private static final AutismHudManager.Inner_CachePool[] init_al_CachePoolArr = new AutismHudManager.Inner_CachePool[init_List.size()];
   private static List<AutismHudManager.Inner_JsonUtil> update_List = List.of();
   private static long put_J = -9223372036854775808L;

   private AutismHudManager() {
   }

   private static List<String> load_List() {
      if (HudElements.isEmpty()) {
         return init_List;
      }

      int revision = HudElements.revision();
      if (revision != push_I) {
         List<String> ids = new ArrayList<>(init_List);
         ids.addAll(HudElements.ids());
         read_List = ids;
         push_I = revision;
      }

      return read_List;
   }

   public static int init_I() {
      init_V();
      int count = 0;

      for (String id : load_List()) {
         if (init_AutismConfig_HudEl_Str(id).enabled) {
            count++;
         }
      }

      return count;
   }

   public static void init_V() {
      AutismConfig config = AutismConfig.getGlobal();
      int hudRevision = HudElements.revision();
      if (!load_Z || !config.hudLayoutMigrated || hudRevision != pop_I) {
         read_Map.clear();
         if (!config.hudLayoutMigrated) {
            init_V_AutismConfig(config);
         }

         for (String id : load_List()) {
            init_AutismConfig_HudEl_Str(id);
         }

         load(config);
         load_Z = true;
         pop_I = hudRevision;
      }
   }

   public static List<String> init_List() {
      init_V();
      return load_List();
   }

   public static String init_Str_Str(String id) {
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

   public static String load_Str(String id) {
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

   public static AutismConfig.HudElementState init_AutismConfig_HudEl_Str(String id) {
      AutismConfig config = AutismConfig.getGlobal();
      if (config != init_AutismConfig) {
         read_Map.clear();
         init_AutismConfig = config;
      }

      AutismConfig.HudElementState cachedState = read_Map.get(id);
      if (cachedState != null) {
         return cachedState;
      }

      read(config);
      AutismConfig.HudElementState state = config.hudElements.computeIfAbsent(id, key -> load_AutismConfig_HudEl(key));
      if (state.settings == null) {
         state.settings = new LinkedHashMap<>();
      }

      for (Entry<String, String> entry : init_Map_Str(id).entrySet()) {
         state.settings.putIfAbsent(entry.getKey(), entry.getValue());
      }

      read_Map.put(id, state);
      return state;
   }

   public static void load_V() {
      AutismConfig.getGlobal().save();
   }

   public static boolean init_Z_Screen_Module(Screen screen, Module hud) {
      if (autismclient.modules.aD.init_Z()) {
         return false;
      } else if (hud == null || !hud.isEnabled() || init_Minecraft.player == null || init_Minecraft.gui.hud.isHidden()) {
         return false;
      } else if (screen == null) {
         return true;
      } else if (screen instanceof autismclient.gui.d.AutismHudEditorScreen) {
         return false;
      } else if (screen instanceof ChatScreen) {
         return init_Z_Module(hud, "show-in-chat", false);
      } else if (screen.isPauseScreen()) {
         return init_Z_Module(hud, "show-in-pause", false);
      } else {
         return init_Z_Module(hud, "hide-in-guis", true) ? false : !(screen instanceof AbstractContainerScreen) || !init_Z_Module(hud, "hide-in-guis", true);
      }
   }

   public static void write_V() {
      if (init_Minecraft.player == null) {
         load_J = -9223372036854775808L;
         read_D = 0.0;
      } else if (init_Minecraft.level != null) {
         long gameTime = init_Minecraft.level.getGameTime();
         if (gameTime != load_J) {
            boolean freecam = load_Z();
            if (freecam != init_Z) {
               init_Z = freecam;
               load_J = -9223372036854775808L;
               read_D = 0.0;
            }

            Vec3 sample = init_Vec3();
            double x = sample.x;
            double z = sample.z;
            if (load_J != -9223372036854775808L) {
               long ticks = Math.max(1L, gameTime - load_J);
               double dx = x - init_D;
               double dz = z - load_D;
               double distance = Math.sqrt(dx * dx + dz * dz);
               double instant = distance * (20.0 / ticks);
               if (instant > 80.0 && !freecam) {
                  instant = init_D();
               }

               if (instant < 0.01) {
                  instant = 0.0;
               }

               read_D = read_D * 0.7 + instant * 0.3;
               if (read_D < 0.03) {
                  read_D = 0.0;
               }
            }

            init_D = x;
            load_D = z;
            load_J = gameTime;
         }
      }
   }

   private static double init_D() {
      if (init_Minecraft.player == null) {
         return 0.0;
      }

      double vx = init_Minecraft.player.getDeltaMovement().x;
      double vz = init_Minecraft.player.getDeltaMovement().z;
      return Math.sqrt(vx * vx + vz * vz) * 20.0;
   }

   public static void init_V_GuiGraphicsExtract_4(GuiGraphicsExtractor context, Font font, boolean editor, String selectedId, int mouseX, int mouseY) {
      init_V_GuiGraphicsExtract_4(context, font, editor, selectedId, mouseX, mouseY, List.of());
   }

   private static void init_V_Str_Throwable(String where, Throwable t) {
      long now = System.currentTimeMillis();
      if (now - init_J >= 5000L) {
         init_J = now;
         AutismClientAddon.LOG.warn("[Autism] HUD '{}' render failed; skipped to protect the UI", where, t);
      }
   }

   public static void init_V_GuiGraphicsExtract_4(
      GuiGraphicsExtractor context, Font font, boolean editor, String selectedId, int mouseX, int mouseY, List<AutismHudManager.Inner_BufferHelper> occluders
   ) {
      long perf = bK.load_J();
      init_V();
      autismclient.gui.e.MsdfTextRenderer.init_V_Z(!init_Z());
      init_V_Font_Z(font, editor);
      load_List = !editor && occluders != null && !occluders.isEmpty() ? occluders : List.of();

      try {
         read_J++;
         write_V();
         write_List = init_List_Font_Z(font, editor);

         for (String id : load_List()) {
            AutismConfig.HudElementState state = init_AutismConfig_HudEl_Str(id);
            if (state.enabled
               && (editor || !"bed_danger".equals(id))
               && (editor || !"anti_vanish".equals(id) || autismclient.modules.AntiVanishModule.init_Z_19())
               && !flush_Z(id)) {
               AutismHudManager.Inner_CachePool cached = init_al_CachePool_Str_Font(id, font);
               AutismHudManager.Inner_BufferHelper bounds = cached.init_al_TaskQueue().init_al_BufferHelper();
               if (bounds.read > 0 && bounds.write > 0) {
                  int dodgeY = 0;
                  if (!editor && "anti_vanish".equals(id)) {
                     dodgeY = init_I_al_BufferHelper(bounds);
                  } else if (!"active_modules".equals(id) && init_Z_al_BufferHelper(bounds)) {
                     continue;
                  }

                  boolean hovered = init_Z_I(mouseX, mouseY, bounds);

                  try {
                     init_V_GuiGraphicsExtract_7(context, font, id, state, cached, editor, selectedId != null && selectedId.equals(id), hovered, dodgeY);
                  } catch (Throwable t) {
                     init_V_Str_Throwable("element:" + id, t);
                  }
               }
            }
         }
      } finally {
         load_List = List.of();
         write_List = null;
         autismclient.gui.e.MsdfTextRenderer.init_V_Z(false);
         sync_V();
         bK.init("hud.render", perf);
      }
   }

   private static boolean init_Z() {
      try {
         Module hud = autismclient.modules.al.init_Module_Str("hud");
         return hud != null && Boolean.parseBoolean(hud.value("custom-font"));
      } catch (Throwable t) {
         return false;
      }
   }

   public static AutismHudManager.Inner_TaskQueue init_al_TaskQueue_Str_Font(String id, Font font) {
      return init_al_CachePool_Str_Font(id, font).init_al_TaskQueue();
   }

   public static void init_V_GuiGraphicsExtract_2(GuiGraphicsExtractor context, Font font, String id) {
      if (context != null && font != null && id != null && !autismclient.modules.aD.init_Z()) {
         init_V();
         AutismConfig.HudElementState state = init_AutismConfig_HudEl_Str(id);
         if (state.enabled && (!"anti_vanish".equals(id) || autismclient.modules.AntiVanishModule.init_Z_19())) {
            autismclient.gui.e.MsdfTextRenderer.init_V_Z(!init_Z());
            init_V_Font_Z(font, false);

            try {
               write_List = init_List_Font_Z(font, false);
               AutismHudManager.Inner_CachePool cached = init_al_CachePool_Str_Font(id, font);
               AutismHudManager.Inner_BufferHelper bounds = cached.init_al_TaskQueue().init_al_BufferHelper();
               if (bounds.update() <= 0 || bounds.sync() <= 0) {
                  return;
               }

               init_V_GuiGraphicsExtract_7(context, font, id, state, cached, false, false, false, 0);
            } finally {
               write_List = null;
               autismclient.gui.e.MsdfTextRenderer.init_V_Z(false);
               sync_V();
            }

            return;
         }
      }
   }

   private static AutismHudManager.Inner_TaskQueue init_al_TaskQueue_Str(
      String id, Font font, AutismConfig.HudElementState state, List<AutismHudManager.Inner_NetHelper> lines, List<Integer> widths
   ) {
      int pad = flush_I(id);
      int width;
      int height;
      if ("bed_danger".equals(id) && !AutismLiteVariant.enabled()) {
         int[] size = autismclient.modules.BedDangerHudRenderer.init_IArr(font);
         width = size[0];
         height = size[1];
      } else if ("watermark".equals(id)) {
         int logoW = open(id);
         width = logoW + pad * 2 + poll(id);
         height = load(logoW) + pad * 2;
      } else if ("gif".equals(id)) {
         width = push(id) + pad * 2;
         height = pop(id) + pad * 2;
      } else if ("armor".equals(id)) {
         width = pad * 2 + 72;
         height = pad * 2 + 18;
      } else if ("inventory".equals(id)) {
         width = pad * 2 + 162;
         height = pad * 2 + 54;
      } else if ("compass".equals(id)) {
         width = pad * 2 + tick(id);
         height = pad * 2 + 18;
      } else if ("keystrokes".equals(id)) {
         int unit = load_I(id);
         int keyH = init_I_I(unit);
         int gap = 2;
         width = pad * 2 + 3 * unit + 2 * gap;
         int rows = 2 * keyH + gap;
         if (read_Z(id)) {
            rows += gap + keyH;
         }

         if (write_Z(id)) {
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

         int rowH = write_I(id);
         int gap = sync_I(id);
         width = Math.max(32, maxW + pad * 2);
         height = lines.isEmpty() ? 0 : lines.size() * rowH + Math.max(0, lines.size() - 1) * gap;
      } else if ("fps".equals(id)) {
         int maxW = 0;

         for (Integer lineWidth : widths) {
            maxW = Math.max(maxW, lineWidth);
         }

         width = read(maxW) + pad * 2;
         height = init_CharEncoder.init_I_l(autismclient.gui.f.b.l.read) + close_I(id) * 2;
      } else {
         int lineH = read_I(id);
         int maxW = 0;

         for (Integer lineWidth : widths) {
            maxW = Math.max(maxW, lineWidth);
         }

         width = Math.max(32, maxW + pad * 2);
         int vpad = close_I(id);
         height = Math.max(init_CharEncoder.init_I_l(autismclient.gui.f.b.l.read) + vpad * 2, lines.size() * lineH - sync_I(id) + vpad * 2);
      }

      int renderX = read(id, init_I_Str(state.anchor, state.f_x_I, width), width);
      int renderY = write(id, load_I_Str(state.anchor, state.f_y_I, height), height);
      return new AutismHudManager.Inner_TaskQueue(id, renderX, renderY, width, height, 1.0);
   }

   public static AutismHudManager.Inner_BufferHelper init_al_BufferHelper_Str_Font(String id, Font font) {
      return init_al_TaskQueue_Str_Font(id, font).init_al_BufferHelper();
   }

   private static AutismHudManager.Inner_BufferHelper load(String id, Font font) {
      AutismHudManager.Inner_CachePool cached = init_al_CachePool_Str_Font(id, font);
      if (!"active_modules".equals(id)) {
         AutismHudManager.Inner_BufferHelper bounds = cached.init_al_TaskQueue().init_al_BufferHelper();
         AutismHudManager.Inner_HashUtil rect = init_al_HashUtil_al_HashUtil_al_SyncManager(
            new AutismHudManager.Inner_HashUtil(id, bounds.read(), bounds.write(), bounds.update(), bounds.sync()), cached.init_al_SyncManager()
         );
         return new AutismHudManager.Inner_BufferHelper(id, rect.read(), rect.write(), rect.update(), rect.sync());
      }

      List<AutismHudManager.Inner_HashUtil> rects = init_List_Str_al_CachePool(id, cached);
      if (rects.isEmpty()) {
         return cached.init_al_TaskQueue().init_al_BufferHelper();
      }

      int left = 2147483647;
      int top = 2147483647;
      int right = -2147483648;
      int bottom = -2147483648;

      for (AutismHudManager.Inner_HashUtil rect : rects) {
         left = Math.min(left, rect.read());
         top = Math.min(top, rect.write());
         right = Math.max(right, rect.init_I_2());
         bottom = Math.max(bottom, rect.load_I());
      }

      return new AutismHudManager.Inner_BufferHelper(id, left, top, Math.max(0, right - left), Math.max(0, bottom - top));
   }

   public static String init_Str_Font(Font font, int mouseX, int mouseY) {
      init_V();
      List<String> ids = load_List();

      for (int i = ids.size() - 1; i >= 0; i--) {
         String id = ids.get(i);
         if (init_AutismConfig_HudEl_Str(id).enabled && !flush_Z(id)) {
            AutismHudManager.Inner_BufferHelper bounds = load(id, font);
            if (init_Z_I(mouseX, mouseY, bounds)) {
               return id;
            }
         }
      }

      return null;
   }

   public static void init_V_Str(String id, int x, int y, int screenW, int screenH) {
      if (screenW > 0 && screenH > 0) {
         AutismConfig.HudElementState state = init_AutismConfig_HudEl_Str(id);
         AutismHudManager.Inner_TaskQueue layout = init_al_TaskQueue_Str_Font(id, init_Minecraft.font);
         int w = layout.init_I();
         int h = layout.load();
         int px = init_I_I_2(x, get(id), update(id, screenW, w));
         int py = init_I_I_2(y, set(id), sync(id, screenH, h));
         boolean right = px > screenW / 2 || px + w >= screenW - 2 || px + w / 2 >= screenW / 2;
         boolean bottom = py > screenH / 2 || py + h >= screenH - 2 || py + h / 2 >= screenH / 2;
         state.anchor = (bottom ? "BOTTOM_" : "TOP_") + (right ? "RIGHT" : "LEFT");
         state.f_x_I = right ? px + w - screenW : px;
         state.f_y_I = bottom ? py + h - screenH : py;
         init_Map.remove(id);
         tick_I++;
         load_V();
      }
   }

   public static void init_V_Str_Z(String id, boolean enabled) {
      init_AutismConfig_HudEl_Str(id).enabled = enabled;
      tick_I++;
      load_V();
   }

   public static void init_V_Str(String id) {
      AutismConfig.HudElementState state = init_AutismConfig_HudEl_Str(id);
      state.enabled = !state.enabled;
      tick_I++;
      load_V();
   }

   public static String init_Str_Str_Str(String id, String key) {
      return init_AutismConfig_HudEl_Str(id).settings.getOrDefault(key, init_Map_Str(id).getOrDefault(key, ""));
   }

   public static String load_Str(String id, String key) {
      return init_Map_Str(id).getOrDefault(key, "");
   }

   public static void init_V_Str(String id, String key, String value) {
      init_AutismConfig_HudEl_Str(id).settings.put(key, value == null ? "" : value);
      tick_I++;
      load_V();
   }

   public static boolean init_Z_Str_Str(String id, String key) {
      return Boolean.parseBoolean(init_Str_Str_Str(id, key));
   }

   public static int init_I_Str_2(String id, String key, int fallback) {
      try {
         return Integer.parseInt(init_Str_Str_Str(id, key));
      } catch (Exception ignored) {
         return fallback;
      }
   }

   public static double init_D_Str(String id, String key, double fallback) {
      try {
         return Double.parseDouble(init_Str_Str_Str(id, key));
      } catch (Exception ignored) {
         return fallback;
      }
   }

   public static void load_V(String id) {
      AutismConfig config = AutismConfig.getGlobal();
      config.hudElements.put(id, load_AutismConfig_HudEl(id));
      read_Map.remove(id);
      tick_I++;
      load_V();
   }

   public static void update_V() {
      AutismConfig config = AutismConfig.getGlobal();
      read(config);
      config.hudElements.clear();

      for (String id : load_List()) {
         config.hudElements.put(id, load_AutismConfig_HudEl(id));
      }

      load_Z = false;
      init_Map.clear();
      read_Map.clear();
      tick_I++;
      load_V();
   }

   public static List<String> init_List_Str(String id) {
      List<String> lines = new ArrayList<>();

      for (AutismHudManager.Inner_NetHelper line : init_al_CachePool_Str_Font(id, init_Minecraft.font).init_List()) {
         lines.add(line.init());
      }

      if (lines.isEmpty()) {
         lines.add(init_Str_Str(id));
      }

      return lines;
   }

   public static int load_I() {
      return tick_I;
   }

   private static void init_V_Font_Z(Font font, boolean editor) {
      update_J = System.currentTimeMillis();
      int screenW = read_I();
      int screenH = write_I();
      int playerId = init_Minecraft.player == null ? 0 : init_Minecraft.player.getId();
      int fontId = System.identityHashCode(font);
      int moduleRev = autismclient.modules.al.init_I();
      int hudElsRev = HudElements.revision();
      int configId = System.identityHashCode(AutismConfig.getGlobal());
      int themeRev = ThemePaletteManager.init_I();
      write_Z = screenW != run_I
         || screenH != get_I
         || playerId != set_I
         || fontId != put_I
         || tick_I != add_I
         || moduleRev != clear_I
         || hudElsRev != reset_I
         || configId != start_I
         || themeRev != stop_I;
      run_I = screenW;
      get_I = screenH;
      set_I = playerId;
      put_I = fontId;
      add_I = tick_I;
      clear_I = moduleRev;
      reset_I = hudElsRev;
      start_I = configId;
      stop_I = themeRev;
      read_Z = !editor;
   }

   private static void sync_V() {
      read_Z = false;
   }

   private static AutismHudManager.Inner_CachePool init_al_CachePool_Str_Font(String id, Font font) {
      AutismHudManager.Inner_CachePool fastCached = init_Map.get(id);
      if (read_Z && !write_Z && fastCached != null && update_J < fastCached.init_J) {
         return fastCached;
      }

      AutismConfig.HudElementState state = init_AutismConfig_HudEl_Str(id);
      long now = read_Z ? update_J : System.currentTimeMillis();
      AutismHudManager.Inner_ConfigStore signature = init_al_ConfigStore_Str(id, state, font, now);
      long interval = init_J_Str(id);
      long nextCheck = (now / interval + 1L) * interval;
      AutismHudManager.Inner_CachePool cached = init_Map.get(id);
      if (cached != null && cached.init_al_ConfigStore().equals(signature)) {
         cached.init_J = nextCheck;
         return cached;
      }

      List<AutismHudManager.Inner_NetHelper> lines = !"armor".equals(id)
            && !"inventory".equals(id)
            && !"compass".equals(id)
            && !"watermark".equals(id)
            && !"keystrokes".equals(id)
            && !"bed_danger".equals(id)
            && !"gif".equals(id)
            && !HudElements.isAddon(id)
         ? load_List(id)
         : List.of();
      List<Integer> widths = new ArrayList<>(lines.size());

      for (AutismHudManager.Inner_NetHelper line : lines) {
         widths.add(init_I_Str_3(id, font, line));
      }

      AutismHudManager.Inner_TaskQueue layout = init_al_TaskQueue_Str(id, font, state, lines, widths);
      AutismHudManager.Inner_CachePool next = new AutismHudManager.Inner_CachePool(signature, lines, widths, layout, init_al_SyncManager_Str(id));
      next.init_J = nextCheck;
      init_Map.put(id, next);
      return next;
   }

   private static int init_I_Str_3(String id, Font font, AutismHudManager.Inner_NetHelper line) {
      int width = init_I_Font_al_NetHelper(font, line);
      if ("breaking_progress".equals(id)) {
         width = Math.max(width, init_I_Font_al_NetHelper(font, init_al_NetHelper_Str(id, "Breaking", "100%")));
      }

      return width;
   }

   private static AutismHudManager.Inner_ConfigStore init_al_ConfigStore_Str(String id, AutismConfig.HudElementState state, Font font, long now) {
      long nowBucket = now / init_J_Str(id);
      int screenW = read_I();
      int screenH = write_I();
      int playerId = init_Minecraft.player == null ? 0 : init_Minecraft.player.getId();
      int activeRevision = "active_modules".equals(id) ? autismclient.modules.al.load_I() : 0;
      int moduleRevision = "active_modules".equals(id) ? autismclient.modules.al.init_I() : 0;
      return new AutismHudManager.Inner_ConfigStore(
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
         ThemePaletteManager.init_I()
      );
   }

   private static long init_J_Str(String id) {
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

   private static boolean load_Z() {
      return autismclient.modules.FreecamController.init_Z();
   }

   private static Vec3 init_Vec3() {
      if (load_Z()) {
         return autismclient.modules.FreecamController.load_Vec3_F(1.0F);
      } else {
         return init_Minecraft.player == null ? Vec3.ZERO : init_Minecraft.player.position();
      }
   }

   private static BlockPos init_BlockPos() {
      if (load_Z()) {
         return BlockPos.containing(autismclient.modules.FreecamController.load_Vec3_F(1.0F));
      } else {
         return init_Minecraft.player == null ? BlockPos.ZERO : init_Minecraft.player.blockPosition();
      }
   }

   private static float init_F() {
      if (load_Z()) {
         return autismclient.modules.FreecamController.init_F_F(1.0F);
      } else {
         return init_Minecraft.player == null ? 180.0F : init_Minecraft.player.getYRot();
      }
   }

   private static float load_F() {
      if (load_Z()) {
         return autismclient.modules.FreecamController.load_F_F(1.0F);
      } else {
         return init_Minecraft.player == null ? 0.0F : init_Minecraft.player.getXRot();
      }
   }

   private static Vec3 load_Vec3() {
      if (load_Z() && autismclient.modules.FreecamController.load_Z()) {
         return autismclient.modules.FreecamController.load_Vec3_F(1.0F);
      } else {
         return init_Minecraft.player == null ? Vec3.ZERO : init_Minecraft.player.position();
      }
   }

   private static List<AutismHudManager.Inner_NetHelper> load_List(String id) {
      List<AutismHudManager.Inner_NetHelper> lines = new ArrayList<>();
      if (init_Minecraft.player == null) {
         lines.add(init_al_NetHelper_Str(id, init_Str_Str(id), "Preview"));
         return lines;
      }

      switch (id) {
         case "active_modules":
            init_V_List(lines);
            break;
         case "tps":
            lines.add(sync_al_NetHelper());
            break;
         case "coordinates":
            Vec3 v = init_Vec3();
            double[] p = CoordinateSpoofer.init_DArr(v.x, v.y, v.z);
            lines.add(init_al_NetHelper_Str(id, "Pos", init_Str_D(p[0], p[1], p[2])));
            break;
         case "nether_coords":
            lines.add(init_al_NetHelper());
            break;
         case "fps":
            lines.add(load_al_NetHelper());
            break;
         case "ping":
            lines.add(flush_al_NetHelper());
            break;
         case "speed":
            lines.add(init_al_NetHelper_Str(id, "Speed", String.format(Locale.ROOT, "%.2f b/s", read_D)));
            break;
         case "game_mode":
            lines.add(init_al_NetHelper_Str(id, "Game", init_Str()));
            break;
         case "durability":
            lines.add(close_al_NetHelper());
            break;
         case "looking_at":
            lines.add(open_al_NetHelper());
            break;
         case "breaking_progress":
            lines.add(poll_al_NetHelper());
            break;
         case "server":
            lines.add(push());
            break;
         case "weather":
            lines.add(pop());
            break;
         case "biome":
            lines.add(tick());
            break;
         case "world_time":
            lines.add(run());
            break;
         case "real_time":
            lines.add(get());
            break;
         case "rotation":
            lines.add(init_al_NetHelper_Str(id, "Rot", String.format(Locale.ROOT, "%.1f yaw, %.1f pitch", Mth.wrapDegrees(init_F()), load_F())));
         case "watermark":
            break;
         case "item_counter":
            lines.add(set());
            break;
         case "potion_timers":
            write(lines);
            break;
         case "compass":
            lines.add(init_al_NetHelper_Str(id, "Compass", poll_Str()));
            break;
         case "anti_vanish":
            load(lines);
            break;
         case "cps":
            read(lines);
            break;
         case "memory":
            lines.add(read_al_NetHelper());
            break;
         case "server_ip":
            lines.add(write_al_NetHelper());
            break;
         case "server_brand":
            lines.add(init_al_NetHelper_Str(id, "Brand", read_Str()));
            break;
         case "fps_graph":
            lines.add(update_al_NetHelper());
            break;
         default:
            lines.add(init_al_NetHelper_Str(id, init_Str_Str(id), ""));
      }

      if ("active_modules".equals(id) && lines.isEmpty()) {
         return lines;
      }

      if (lines.isEmpty()) {
         lines.add(init_al_NetHelper_Str(id, init_Str_Str(id), ""));
      }

      return lines;
   }

   private static void init_V_List(List<AutismHudManager.Inner_NetHelper> lines) {
      boolean showInfo = init_Z_Str_Str("active_modules", "module-info");
      boolean showKeybind = init_Z_Str_Str("active_modules", "show-keybind");
      String hidden = "|" + init_Str_Str_Str("active_modules", "hidden-modules").toLowerCase(Locale.ROOT) + "|";
      List<Module> modules = new ArrayList<>(autismclient.modules.al.load_List());
      modules.removeIf(modulex -> !modulex.showInArrayList());
      modules.removeIf(modulex -> hidden.contains("|" + modulex.id().toLowerCase(Locale.ROOT) + "|"));
      String sort = init_Str_Str_Str("active_modules", "sort");
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

      String colorMode = init_Str_Str_Str("active_modules", "color-mode");
      int moduleCount = modules.size();
      AutismHudManager.Inner_EventBus rainbow = init_al_EventBus_Str_I(colorMode, moduleCount);
      int infoColor = load("module-info-color", -4743522);

      for (int i = 0; i < modules.size(); i++) {
         Module module = modules.get(i);
         int moduleColor = init_I_Module(module, i, rainbow);
         AutismHudManager.Inner_NetHelper line = new AutismHudManager.Inner_NetHelper();
         line.init(module.name(), moduleColor);
         String info = module.info();
         if (showInfo && info != null && !info.isBlank()) {
            line.init(" " + info, infoColor);
         }

         if (showKeybind && module.keybind() != -1) {
            line.init(" [" + ValueSink.init_Str(module.keybind()) + "]", infoColor);
         }

         lines.add(line);
      }
   }

   private static void load(List<AutismHudManager.Inner_NetHelper> lines) {
      for (autismclient.modules.AntiVanishModule.Inner_StreamUtil entry : autismclient.modules.AntiVanishModule.init_List_8()) {
         lines.add(
            init_al_NetHelper_Str(
               "anti_vanish",
               autismclient.modules.AntiVanishModule.init_Str_SyncManager_Stream(entry),
               autismclient.modules.AntiVanishModule.load_Str_SyncManager_Stream(entry)
            )
         );
      }

      if (lines.isEmpty()) {
         lines.add(init_al_NetHelper_Str("anti_vanish", "Vanish", "Clear"));
      }
   }

   private static AutismHudManager.Inner_EventBus init_al_EventBus_Str_I(String colorMode, int moduleCount) {
      return new AutismHudManager.Inner_EventBus(
         colorMode,
         init_F_D(load_D()),
         (float)init_D_Str("active_modules", "rainbow-spread", 0.035),
         Math.min(0.35F, (float)init_D_Str("active_modules", "rainbow-saturation", 0.35)),
         (float)init_D_Str("active_modules", "rainbow-brightness", 1.0),
         load_I_Str_2("active_modules", "flat-color", -50373),
         load_I_Str_2("active_modules", "gradient-start-color", -50373),
         load_I_Str_2("active_modules", "gradient-end-color", -10538),
         load_I_Str_2("active_modules", "value-color", -791321),
         Math.max(1.0, moduleCount - 1.0)
      );
   }

   private static double load_D() {
      if (open_I != tick_I) {
         write_D = init_D_Str("active_modules", "rainbow-speed", 1.0)
            * ("Reverse".equals(init_Str_Str_Str("active_modules", "rainbow-direction")) ? -1.0 : 1.0);
         open_I = tick_I;
      }

      return write_D;
   }

   private static float init_F_D(double speed) {
      if (speed != 0.0 && !Double.isNaN(speed)) {
         double periodMs = 1000.0 / (0.0525 * Math.abs(speed));
         long period = Math.max(1L, (long)periodMs);
         float phase = (float)((double)(System.currentTimeMillis() % period) / period);
         return speed < 0.0 ? (1.0F - phase) % 1.0F : phase;
      } else {
         return 0.0F;
      }
   }

   private static int init_I_Module(Module module, int index, AutismHudManager.Inner_EventBus rainbow) {
      if ("Flat".equals(rainbow.init_Str())) {
         return rainbow.init_I();
      } else if ("Random".equals(rainbow.init_Str())) {
         return 0xFF000000 | module.id().hashCode() & 16777215;
      } else if ("Gradient".equals(rainbow.init_Str())) {
         double spread = rainbow.load_F() * 3.0;
         double phase = (rainbow.init_F() + index * spread + index / rainbow.init_D()) % 1.0;
         double t = phase < 0.5 ? phase * 2.0 : (1.0 - phase) * 2.0;
         return init_I_I(rainbow.load_I(), rainbow.read_I(), t);
      } else {
         float hue = (rainbow.init_F() + index * rainbow.load_F()) % 1.0F;
         int rgb = Color.HSBtoRGB(hue, rainbow.read_F(), rainbow.write_F());
         return load_I_I(-402653184 | rgb & 16777215, rainbow.write_I(), 0.55);
      }
   }

   private static String init_Str_Module(Module module, boolean showInfo, boolean showKeybind) {
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
         init_Minecraft.font,
         init_Str_Module(module, showInfo, showKeybind),
         init_CharEncoder.init_Identifier_l(autismclient.gui.f.b.l.read),
         load_I_Str_2("active_modules", "text-color", -791321)
      );
   }

   private static AutismHudManager.Inner_NetHelper init_al_NetHelper() {
      if (init_Minecraft.level != null && init_Minecraft.player != null) {
         Identifier dimension = init_Minecraft.level.dimension().identifier();
         String path = dimension.getPath();
         Vec3 v = init_Vec3();
         double[] p = CoordinateSpoofer.init_DArr(v.x, v.y, v.z);
         double x = p[0];
         double y = p[1];
         double z = p[2];
         if ("overworld".equals(path)) {
            return init_al_NetHelper_Str("nether_coords", "Nether", init_Str_D(x / 8.0, y, z / 8.0));
         } else {
            return "the_nether".equals(path)
               ? init_al_NetHelper_Str("nether_coords", "Overworld", init_Str_D(x * 8.0, y, z * 8.0))
               : init_al_NetHelper_Str("nether_coords", "Opposite", "N/A");
         }
      } else {
         return init_al_NetHelper_Str("nether_coords", "Opposite", "N/A");
      }
   }

   private static void init_V_GuiGraphicsExtract_7(
      GuiGraphicsExtractor context,
      Font font,
      String id,
      AutismConfig.HudElementState state,
      AutismHudManager.Inner_CachePool cached,
      boolean editor,
      boolean selected,
      boolean hovered,
      int dodgeY
   ) {
      float alpha = 1.0F;
      AutismHudManager.Inner_TaskQueue layout = cached.init_al_TaskQueue();
      int x = layout.read();
      int y = layout.write() + dodgeY;
      int unscaledW = layout.update();
      int unscaledH = layout.sync();
      if ("bed_danger".equals(id) && !AutismLiteVariant.enabled()) {
         autismclient.modules.BedDangerHudRenderer.init(context, font, x, y, editor);
         if (editor || selected || hovered) {
            init_V_GuiGraphicsExtract_3(context, x, y, unscaledW, unscaledH, load_I_Str_2(id, selected ? "accent-color" : "outline-color", -1426113733), 1);
         }
      } else if ("active_modules".equals(id)) {
         init(context, font, id, cached, editor, selected, hovered, alpha);
      } else {
         AutismHudManager.Inner_SyncManager style = cached.init_al_SyncManager();
         boolean flatGrid = ("armor".equals(id) || "inventory".equals(id)) && load_Z(id);
         boolean background = style.init_Z() && !flatGrid;
         AutismHudManager.Inner_HashUtil contentRect = new AutismHudManager.Inner_HashUtil(id, x, y, unscaledW, unscaledH);
         AutismHudManager.Inner_HashUtil rect = init_al_HashUtil_al_HashUtil_al_SyncManager(contentRect, style);
         boolean inGameOutline = style.load_Z() && !flatGrid && !editor && !selected && !hovered;
         boolean needsBlockers = background && !"compass".equals(id) || inGameOutline;
         List<AutismHudManager.Inner_HashUtil> blockers = needsBlockers ? load_List_Font(font, id, rect) : null;
         if (background) {
            if ("compass".equals(id)) {
               init_V_GuiGraphicsExtract(context, rect.read(), rect.write(), rect.update(), rect.sync(), alpha, style.close());
            } else {
               init_V_GuiGraphicsExtract_3(context, font, id, rect, init_I_I_F(style.close(), alpha), blockers);
            }
         } else if (editor) {
            autismclient.gui.f.b.k.init(context, rect.read(), rect.write(), rect.init_I_2(), rect.load_I(), init_I_Z(selected));
         }

         if (style.load_Z() && !flatGrid || editor || selected || hovered) {
            int border = selected ? load_I_Str_2(id, "accent-color", -50373) : (editor ? load_I_Str_2(id, "outline-color", -1426113733) : style.open());
            int outlineColor = init_I_I_F(border, alpha);
            if (!editor && !selected && !hovered) {
               init_V_GuiGraphicsExtract_2(context, rect, blockers, outlineColor, style.init_I());
            } else {
               init_V_GuiGraphicsExtract_3(context, rect.read(), rect.write(), rect.update(), rect.sync(), outlineColor, style.init_I());
            }
         }

         int pad = style.load_I();
         if ("watermark".equals(id)) {
            init_V_GuiGraphicsExtract(context, x + pad, y + pad, open(id), load(open(id)), alpha);
         } else if ("gif".equals(id)) {
            init_V_GuiGraphicsExtract_4(context, font, id, x + pad, y + pad, unscaledW - pad * 2, unscaledH - pad * 2, alpha, editor);
         } else if ("armor".equals(id)) {
            init_V_GuiGraphicsExtract(context, x + pad, y + pad, alpha, style);
         } else if ("inventory".equals(id)) {
            load(context, x + pad, y + pad, alpha, style);
         } else if ("compass".equals(id)) {
            init_V_GuiGraphicsExtract_3(context, font, id, x + pad, y + pad, unscaledW - pad * 2, unscaledH - pad * 2, alpha, style);
         } else if ("keystrokes".equals(id)) {
            init(context, font, id, x + pad, y + pad, unscaledW - pad * 2, unscaledH - pad * 2, alpha);
         } else if (HudElements.isAddon(id)) {
            HudElementProvider provider = HudElements.get(id);

            try {
               provider.render(context, font, x + pad, y + pad, alpha);
            } catch (Exception e) {
               AutismClientAddon.LOG.warn("[Hud] Addon element '{}' render failed", id, e);
            }
         } else {
            init_V_GuiGraphicsExtract_6(context, font, id, state, cached, x, y, unscaledW, alpha);
         }
      }
   }

   private static void init(
      GuiGraphicsExtractor context, Font font, String id, AutismHudManager.Inner_CachePool cached, boolean editor, boolean selected, boolean hovered, float alpha
   ) {
      List<AutismHudManager.Inner_NetHelper> lines = cached.init_List();
      List<AutismHudManager.Inner_HashUtil> rowRects = load(id, cached);
      AutismHudManager.Inner_SyncManager style = cached.init_al_SyncManager();
      int pad = style.load_I();
      boolean background = style.init_Z();
      boolean drawOutline = style.load_Z() || editor || selected || hovered;
      boolean shadow = style.read_Z();
      int backgroundColor = init_I_I_F(style.close(), alpha);
      int editorColor = init_I_Z(selected);
      int border = selected ? load_I_Str_2(id, "accent-color", -50373) : (editor ? load_I_Str_2(id, "outline-color", -1426113733) : style.open());
      int outlineColor = init_I_I_F(border, alpha);
      int outlineWidth = style.init_I();
      int verticalPad = style.read_I();

      for (int i = 0; i < lines.size() && i < rowRects.size(); i++) {
         AutismHudManager.Inner_HashUtil contentRect = rowRects.get(i);
         AutismHudManager.Inner_HashUtil rect = init_al_HashUtil_al_HashUtil_al_SyncManager(contentRect, style);
         if (!init_Z_al_HashUtil(rect)) {
            List<AutismHudManager.Inner_HashUtil> blockers = !background && !drawOutline ? null : load_List_Font(font, id, rect);
            if (background) {
               init_V_GuiGraphicsExtract_3(context, font, id, rect, backgroundColor, blockers);
            } else if (editor) {
               autismclient.gui.f.b.k.init(context, rect.read(), rect.write(), rect.init_I_2(), rect.load_I(), editorColor);
            }

            if (drawOutline) {
               init_V_GuiGraphicsExtract_2(context, rect, blockers, outlineColor, outlineWidth);
            }

            int lineW = i < cached.load().size() ? cached.load().get(i) : -1;
            init_V_GuiGraphicsExtract_2(
               context,
               font,
               lines.get(i),
               contentRect.read() + pad,
               contentRect.write() + verticalPad,
               Math.max(1, contentRect.update() - pad * 2),
               alpha,
               shadow,
               lineW
            );
         }
      }
   }

   private static void init_V_GuiGraphicsExtract(GuiGraphicsExtractor context, int x, int y, int width, int height, float alpha) {
      context.blit(
         RenderPipelines.GUI_TEXTURED,
         ThemeTextureRecolorer.init(init_Identifier, ThemePaletteManager.Inner_DataUtil.ACCENT),
         x,
         y,
         0.0F,
         0.0F,
         width,
         height,
         init_I,
         load_I,
         init_I,
         load_I,
         ARGB.white(alpha)
      );
   }

   private static void init_V_GuiGraphicsExtract_4(
      GuiGraphicsExtractor context, Font font, String id, int x, int y, int width, int height, float alpha, boolean editor
   ) {
      String path = init_Str_Str_Str(id, "gif-path").trim();
      if (!path.isEmpty() && init_Z_Str_Str(id, "gif-natural")) {
         init_V_Str_Str(id, path);
      }

      Identifier texture = path.isEmpty() ? null : autismclient.util.c.GifTextureManager.init_Identifier_Str(path, load_D(id), init_D_Str(id), id);
      if (texture != null) {
         context.blit(texture, x, y, x + width, y + height, 0.0F, 1.0F, 0.0F, 1.0F);
      } else if (editor) {
         int tagColor = init_I_I_F(load_I_Str_2(id, "label-color", -4743522), alpha);
         String tag = "GIF";
         int tagWidth = autismclient.gui.f.b.k.init(font, tag, init_CharEncoder.init_Identifier_l(autismclient.gui.f.b.l.read), tagColor);
         int tagX = x + Math.max(0, (width - tagWidth) / 2);
         int tagY = y + Math.max(0, (height - init_CharEncoder.init_I_l(autismclient.gui.f.b.l.read)) / 2);
         autismclient.gui.f.b.k.init(context, font, tag, init_CharEncoder.init_Identifier_l(autismclient.gui.f.b.l.read), tagColor, tagX, tagY, false);
      }
   }

   private static void init_V_GuiGraphicsExtract_6(
      GuiGraphicsExtractor context,
      Font font,
      String id,
      AutismConfig.HudElementState state,
      AutismHudManager.Inner_CachePool cached,
      int renderX,
      int renderY,
      int unscaledW,
      float alpha
   ) {
      List<AutismHudManager.Inner_NetHelper> lines = cached.init_List();
      AutismHudManager.Inner_SyncManager style = cached.init_al_SyncManager();
      int pad = style.load_I();
      int y = renderY + style.read_I();
      int lineH = style.update();
      String alignment = style.init_Str();
      boolean shadow = style.read_Z();
      int maxW = Math.max(1, unscaledW - pad * 2);

      for (int i = 0; i < lines.size(); i++) {
         AutismHudManager.Inner_NetHelper line = lines.get(i);
         int lineW = i < cached.load().size() ? cached.load().get(i) : init_I_Font_al_NetHelper(font, line);
         int tx = renderX + pad;
         if ("Center".equals(alignment)) {
            tx = renderX + Math.max(pad, (unscaledW - lineW) / 2);
         } else if ("Right".equals(alignment)) {
            tx = renderX + Math.max(pad, unscaledW - pad - lineW);
         }

         init_V_GuiGraphicsExtract_2(context, font, line, tx, y, maxW, alpha, shadow, lineW);
         y += lineH;
      }
   }

   private static void init_V_GuiGraphicsExtract_2(
      GuiGraphicsExtractor context, Font font, AutismHudManager.Inner_NetHelper line, int x, int y, int maxW, float alpha, boolean shadow, int lineWidth
   ) {
      int drawX = x;
      int used = 0;
      boolean fits = lineWidth >= 0 && lineWidth <= maxW;
      int[] cachedWidths = fits ? line.init_IArr : null;
      boolean cachedWidthsValid = cachedWidths != null && cachedWidths.length == line.init_List.size();

      for (int i = 0; i < line.init_List.size(); i++) {
         AutismHudManager.Inner_StateCache segment = line.init_List.get(i);
         int remaining = Math.max(1, maxW - used);
         String text = fits
            ? segment.init_Str
            : autismclient.gui.f.b.k.init(font, segment.init_Str, remaining, init_CharEncoder.init_Identifier_l(autismclient.gui.f.b.l.read), segment.init_I);
         autismclient.gui.f.b.k.init(
            context, font, text, init_CharEncoder.init_Identifier_l(autismclient.gui.f.b.l.read), init_I_I_F(segment.init_I, alpha), drawX, y, shadow
         );
         int width = cachedWidthsValid
            ? cachedWidths[i]
            : autismclient.gui.f.b.k.init(font, text, init_CharEncoder.init_Identifier_l(autismclient.gui.f.b.l.read), segment.init_I);
         drawX += width;
         used += width;
         if (used >= maxW) {
            break;
         }
      }
   }

   private static void init_V_GuiGraphicsExtract(GuiGraphicsExtractor context, int x, int y, float alpha, AutismHudManager.Inner_SyncManager style) {
      if (init_Minecraft.player != null) {
         EquipmentSlot[] slots = new EquipmentSlot[]{EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET};
         boolean flat = load_Z("armor");
         if (flat) {
            init_V_GuiGraphicsExtract_2(context, x, y, slots.length, 1, alpha, style);
         }

         for (int i = 0; i < slots.length; i++) {
            int sx = x + i * 18;
            if (!flat) {
               init_V_GuiGraphicsExtract(context, sx, y);
            }

            ItemStack stack = init_Minecraft.player.getItemBySlot(slots[i]);
            init_V_GuiGraphicsExtract_2(context, stack, sx + 1, y + 1);
         }
      }
   }

   private static void load(GuiGraphicsExtractor context, int x, int y, float alpha, AutismHudManager.Inner_SyncManager style) {
      if (init_Minecraft.player != null) {
         boolean flat = load_Z("inventory");
         if (flat) {
            init_V_GuiGraphicsExtract_2(context, x, y, 9, 3, alpha, style);
         }

         for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
               int sx = x + col * 18;
               int sy = y + row * 18;
               if (!flat) {
                  init_V_GuiGraphicsExtract(context, sx, sy);
               }

               ItemStack stack = init_Minecraft.player.getInventory().getItem(9 + row * 9 + col);
               init_V_GuiGraphicsExtract_2(context, stack, sx + 1, sy + 1);
            }
         }
      }
   }

   private static void init_V_GuiGraphicsExtract_2(GuiGraphicsExtractor context, ItemStack stack, int x, int y) {
      if (!stack.isEmpty() && init_Minecraft.font != null) {
         context.item(stack, x, y);
         context.itemDecorations(init_Minecraft.font, stack, x, y);
      }
   }

   private static boolean load_Z(String id) {
      return "Flat".equals(init_Str_Str_Str(id, "slot-style"));
   }

   private static void init_V_GuiGraphicsExtract_2(GuiGraphicsExtractor context, int x, int y, int cols, int rows, float alpha, AutismHudManager.Inner_SyncManager style) {
      int w = cols * 18;
      int h = rows * 18;
      if (style.init_Z()) {
         autismclient.gui.f.b.k.init(context, x, y, x + w, y + h, init_I_I_F(style.close(), alpha));
      }

      if (style.load_Z()) {
         int line = init_I_I_F(style.open(), alpha);

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
      int fill = ThemePaletteManager.init_I_I_ct_DataUtil(538579477, ThemePaletteManager.Inner_DataUtil.BACKDROP);
      int lit = ThemePaletteManager.init_I_I_ct_DataUtil(1435577909, ThemePaletteManager.Inner_DataUtil.OUTLINE);
      int shade = ThemePaletteManager.init_I_I_ct_DataUtil(1427835662, ThemePaletteManager.Inner_DataUtil.BACKDROP);
      autismclient.gui.f.b.k.init(context, x, y, x + 18, y + 18, fill);
      autismclient.gui.f.b.k.init(context, x, y, x + 18, y + 1, lit);
      autismclient.gui.f.b.k.init(context, x, y + 17, x + 18, y + 18, shade);
      autismclient.gui.f.b.k.init(context, x, y, x + 1, y + 18, lit);
      autismclient.gui.f.b.k.init(context, x + 17, y, x + 18, y + 18, shade);
   }

   private static void init_V_GuiGraphicsExtract_3(
      GuiGraphicsExtractor context, Font font, String id, int x, int y, int width, int height, float alpha, AutismHudManager.Inner_SyncManager style
   ) {
      int stripH = Math.max(16, height);
      int centerX = x + width / 2;
      autismclient.gui.f.b.k.init(context, centerX, y + 2, centerX + 1, y + stripH - 2, init_I_I_F(style.poll(), alpha * 0.88F));
      String[] labels = init_StrArr;
      int reloadGen = autismclient.gui.f.b.k.init_I();
      if (init_Font != font || next_I != reloadGen) {
         for (int i = 0; i < labels.length; i++) {
            init_IArr[i] = autismclient.gui.f.b.k.init(font, labels[i], init_CharEncoder.init_Identifier_l(autismclient.gui.f.b.l.read), -1);
         }

         init_Font = font;
         next_I = reloadGen;
      }

      float yaw = init_F();
      double normalized = (yaw % 360.0 + 360.0) % 360.0;
      int yText = y + Math.max(2, (stripH - init_CharEncoder.init_I_l(autismclient.gui.f.b.l.read)) / 2);
      double visibleDegrees = 118.0;
      boolean fullHeightHighlight = style.init_Z();
      boolean shadow = style.read_Z();

      for (int i = 0; i < labels.length; i++) {
         double centerAngle = i * 45.0;
         double delta = init_D_D(centerAngle - normalized);
         double distance = Math.abs(delta);
         if (!(distance > visibleDegrees)) {
            double presence = 1.0 - distance / visibleDegrees;
            presence = presence * presence * (3.0 - 2.0 * presence);
            int tx = centerX + (int)Math.round(delta / visibleDegrees * (width / 2.0 - 8.0));
            double face = Math.max(0.0, 1.0 - distance / 24.0);
            int baseColor = init_I_I(style.push(), style.poll(), face);
            int textColor = init_I_I_F(baseColor, alpha * (0.18F + (float)presence * 0.82F));
            int tw = init_IArr[i];
            int halo = init_I_I_F(ThemePaletteManager.init_I_I_ct_DataUtil(942148882, ThemePaletteManager.Inner_DataUtil.BACKDROP), alpha * (float)face);
            if (face > 0.0) {
               int haloTop = fullHeightHighlight ? y : y + 2;
               int haloBottom = fullHeightHighlight ? y + stripH : y + stripH - 2;
               autismclient.gui.f.b.k.init(context, tx - tw / 2 - 3, haloTop, tx + tw / 2 + 3, haloBottom, halo);
            }

            autismclient.gui.f.b.k.init(
               context,
               font,
               labels[i],
               init_CharEncoder.init_Identifier_l(autismclient.gui.f.b.l.read),
               init_I_I_F(-872415232, alpha * (0.24F + (float)presence * 0.36F)),
               tx - tw / 2 + 1,
               yText + 1,
               false
            );
            autismclient.gui.f.b.k.init(
               context, font, labels[i], init_CharEncoder.init_Identifier_l(autismclient.gui.f.b.l.read), textColor, tx - tw / 2, yText, shadow
            );
         }
      }
   }

   private static int init_I_F(float phase, float hueOffset, float saturation, float brightness, int softenWith) {
      float hue = (phase + hueOffset) % 1.0F;
      int rgb = Color.HSBtoRGB(hue, saturation, brightness);
      return load_I_I(-402653184 | rgb & 16777215, softenWith, 0.55);
   }

   private static int load_I(String id) {
      return init_I_I_2(init_I_Str_2(id, "keystroke-size", 18), 12, 40);
   }

   private static int init_I_I(int unit) {
      return Math.max(10, Math.round(unit * 0.86F));
   }

   private static boolean read_Z(String id) {
      return init_Z_Str_Str(id, "keystroke-show-space");
   }

   private static boolean write_Z(String id) {
      return init_Z_Str_Str(id, "keystroke-show-mouse");
   }

   private static void init(GuiGraphicsExtractor context, Font font, String id, int x, int y, int width, int height, float alpha) {
      int unit = load_I(id);
      int gap = 2;
      int active = load_I_Str_2(id, "keystroke-active-color", -50373);
      int idle = load_I_Str_2(id, "keystroke-idle-color", -1274015724);
      int text = load_I_Str_2(id, "keystroke-text-color", -1);
      boolean showMouse = write_Z(id);
      long now = System.nanoTime();
      float dt = get_J == 0L ? 0.0F : (float)Math.min(0.1, (now - get_J) / 1.0E9);
      get_J = now;
      float factor = 1.0F - (float)Math.exp(-dt * 16.0);
      boolean[] pressed = init_ZArr();

      for (int i = 0; i < init_FArr.length; i++) {
         float target = pressed[i] ? 1.0F : 0.0F;
         init_FArr[i] = init_FArr[i] + (target - init_FArr[i]) * factor;
         if (Math.abs(target - init_FArr[i]) < 0.003F) {
            init_FArr[i] = target;
         }
      }

      int keyH = init_I_I(unit);
      int rowW = 3 * unit + 2 * gap;
      init(context, font, x + (rowW - unit) / 2, y, unit, keyH, "W", init_FArr[0], idle, active, text, alpha);
      int row2Y = y + keyH + gap;
      init(context, font, x, row2Y, unit, keyH, "A", init_FArr[1], idle, active, text, alpha);
      init(context, font, x + unit + gap, row2Y, unit, keyH, "S", init_FArr[2], idle, active, text, alpha);
      init(context, font, x + 2 * (unit + gap), row2Y, unit, keyH, "D", init_FArr[3], idle, active, text, alpha);
      int nextRowY = row2Y + keyH + gap;
      if (read_Z(id)) {
         init(context, font, x, nextRowY, rowW, keyH, init_FArr[6], idle, active, text, alpha);
         nextRowY += keyH + gap;
      }

      if (showMouse) {
         int leftW = (rowW - gap) / 2;
         init(context, font, x, nextRowY, leftW, keyH, Integer.toString(ClickRateTracker.init_I()), init_FArr[4], idle, active, text, alpha);
         init(context, font, x + leftW + gap, nextRowY, rowW - leftW - gap, keyH, Integer.toString(ClickRateTracker.load_I()), init_FArr[5], idle, active, text, alpha);
      }
   }

   private static boolean[] init_ZArr() {
      Options o = init_Minecraft == null ? null : init_Minecraft.options;
      return new boolean[]{
         init_Z_KeyMapping(o == null ? null : o.keyUp),
         init_Z_KeyMapping(o == null ? null : o.keyLeft),
         init_Z_KeyMapping(o == null ? null : o.keyDown),
         init_Z_KeyMapping(o == null ? null : o.keyRight),
         init_Z_KeyMapping(o == null ? null : o.keyAttack) || ClickRateTracker.init(120L),
         init_Z_KeyMapping(o == null ? null : o.keyUse) || ClickRateTracker.load(120L),
         init_Z_KeyMapping(o == null ? null : o.keyJump)
      };
   }

   private static boolean init_Z_KeyMapping(KeyMapping mapping) {
      return mapping != null && KeyMappingControl.init(mapping).autism$isActuallyDown();
   }

   private static void init(GuiGraphicsExtractor context, Font font, int x, int y, int w, int h, float fill, int idle, int active, int text, float alpha) {
      init(context, font, x, y, w, h, "", fill, idle, active, text, alpha);
      int lineW = Math.max(6, Math.round(w * 0.45F));
      int lineH = Math.max(2, h / 7);
      int lx = x + (w - lineW) / 2;
      int ly = y + (h - lineH) / 2;
      autismclient.gui.f.b.k.init(context, lx, ly, lx + lineW, ly + lineH, init_I_I_F(text, alpha));
   }

   private static void init(
      GuiGraphicsExtractor context, Font font, int x, int y, int w, int h, String label, float fill, int idle, int active, int text, float alpha
   ) {
      if (w > 0 && h > 0) {
         autismclient.gui.f.b.k.init(context, x, y, x + w, y + h, init_I_I_F(idle, alpha));
         if (fill > 0.003F) {
            float eased = fill * fill * (3.0F - 2.0F * fill);
            int fillColor = init_I_I_F(active, alpha * (0.6F + 0.4F * eased));
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

         init_V_GuiGraphicsExtract_3(context, x, y, w, h, init_I_I_F(1711276032, alpha), 1);
         Identifier fontId = init_CharEncoder.init_Identifier_l(autismclient.gui.f.b.l.read);
         int tw = autismclient.gui.f.b.k.init(font, label, fontId, text);
         int fhgt = init_CharEncoder.init_I_l(autismclient.gui.f.b.l.read);
         int tx = x + (w - tw) / 2;
         int ty = y + (h - fhgt) / 2 + 1;
         autismclient.gui.f.b.k.init(context, font, label, fontId, init_I_I_F(-872415232, alpha * 0.55F), tx + 1, ty + 1, false);
         autismclient.gui.f.b.k.init(context, font, label, fontId, init_I_I_F(text, alpha), tx, ty, false);
      }
   }

   private static void init_V_GuiGraphicsExtract(GuiGraphicsExtractor context, int x, int y, int width, int height, float alpha, int base) {
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

   private static double init_D_D(double value) {
      double wrapped = value % 360.0;
      if (wrapped >= 180.0) {
         wrapped -= 360.0;
      }

      if (wrapped < -180.0) {
         wrapped += 360.0;
      }

      return wrapped;
   }

   private static AutismHudManager.Inner_NetHelper init_al_NetHelper_Str(String elementId, String label, String value) {
      AutismHudManager.Inner_NetHelper line = new AutismHudManager.Inner_NetHelper();
      line.init(label + ": ", load_I_Str_2(elementId, "label-color", -4743522));
      line.init(value != null && !value.isBlank() ? value : "N/A", load_I_Str_2(elementId, "value-color", -791321));
      return line;
   }

   private static AutismHudManager.Inner_NetHelper load_al_NetHelper() {
      AutismHudManager.Inner_NetHelper line = new AutismHudManager.Inner_NetHelper();
      int labelColor = load_I_Str_2("fps", "label-color", -4743522);
      int valueColor = load_I_Str_2("fps", "value-color", -791321);
      int muted = init_I_I_F(labelColor, 0.72F);
      line.init("FPS: ", labelColor);
      line.init(Integer.toString(init_Minecraft.getFps()), valueColor);
      line.init(" ", muted);
      line.init("TPS: ", labelColor);
      line.init(String.format(Locale.ROOT, "%.1f", autismclient.util.macro.ServerTickPhaseTracker.init_D()), valueColor);
      line.init(" ", muted);
      line.init("Ping: ", labelColor);
      line.init(autismclient.util.macro.ServerTickPhaseTracker.init_I() + " ms", valueColor);
      return line;
   }

   private static String init_Str() {
      if (init_Minecraft.gameMode == null) {
         return "N/A";
      }

      try {
         String name = init_Minecraft.gameMode.getPlayerMode().getName();
         return name != null && !name.isBlank() ? RegistryDisplayNameCache.update(name) : "N/A";
      } catch (Throwable ignored) {
         return init_Minecraft.gameMode.toString();
      }
   }

   private static String load_Str() {
      ItemStack stack = init_Minecraft.player == null ? ItemStack.EMPTY : init_Minecraft.player.getMainHandItem();
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

   private static AutismHudManager.Inner_NetHelper read_al_NetHelper() {
      Runtime rt = Runtime.getRuntime();
      long used = rt.totalMemory() - rt.freeMemory() >> 20;
      long max = rt.maxMemory() >> 20;
      boolean showBar = init_Z_Str_Str("memory", "show-bar");
      boolean showPct = init_Z_Str_Str("memory", "show-percent");
      String fmt = init_Str_Str_Str("memory", "memory-format");
      long pct = max > 0L ? used * 100L / max : 0L;
      AutismHudManager.Inner_NetHelper line = new AutismHudManager.Inner_NetHelper();
      line.init("Memory: ", load_I_Str_2("memory", "label-color", -4743522));
      boolean isPctFormat = !"used/max".equals(fmt);
      if (isPctFormat) {
         line.init(pct + "%", load_I_Str_2("memory", "value-color", -791321));
      } else {
         line.init(used + " / " + max + " MB", load_I_Str_2("memory", "value-color", -791321));
      }

      if (showPct && !isPctFormat) {
         line.init(" (" + pct + "%)", load_I_Str_2("memory", "value-color", -791321));
      }

      if (showBar) {
         int barColor = pct > 85L ? -50373 : (pct > 60L ? -10752 : -12588930);
         int barW = Math.min(10, Math.max(1, (int)(pct * 10L / 100L)));
         line.init(" [", init_I_I_F(load_I_Str_2("memory", "label-color", -4743522), 0.5F));
         StringBuilder bar = new StringBuilder();

         for (int i = 0; i < barW; i++) {
            bar.append('|');
         }

         for (int i = barW; i < 10; i++) {
            bar.append('.');
         }

         line.init(bar.toString(), barColor);
         line.init("]", init_I_I_F(load_I_Str_2("memory", "label-color", -4743522), 0.5F));
      }

      return line;
   }

   private static AutismHudManager.Inner_NetHelper write_al_NetHelper() {
      AutismHudManager.Inner_NetHelper line = new AutismHudManager.Inner_NetHelper();
      line.init("IP: ", load_I_Str_2("server_ip", "label-color", -4743522));
      if (init_Minecraft.getCurrentServer() != null && init_Minecraft.getCurrentServer().ip != null && !init_Minecraft.getCurrentServer().ip.isBlank()) {
         String ip = init_Minecraft.getCurrentServer().ip;
         if (init_Z_Str_Str("server_ip", "show-port")
            && init_Minecraft.getConnection() != null
            && init_Minecraft.getConnection().getConnection().getRemoteAddress() instanceof InetSocketAddress inet) {
            ip = ip.contains(":") ? ip : ip + ":" + inet.getPort();
         }

         line.init(ip, load_I_Str_2("server_ip", "value-color", -791321));
      } else {
         line.init(init_Minecraft.hasSingleplayerServer() ? "Singleplayer" : "N/A", load_I_Str_2("server_ip", "value-color", -791321));
      }

      return line;
   }

   private static String read_Str() {
      if (init_Minecraft.getConnection() == null) {
         return "N/A";
      }

      try {
         String brand = init_Minecraft.getConnection().serverBrand();
         return brand != null && !brand.isBlank() ? brand : "Unknown";
      } catch (Throwable ignored) {
         return "Unknown";
      }
   }

   private static AutismHudManager.Inner_NetHelper update_al_NetHelper() {
      int fps = init_Minecraft.getFps();
      load_IArr[node_I] = fps;
      node_I = (node_I + 1) % 200;
      if (task_I < 200) {
         task_I++;
      }

      int samples = init_I_Str_2("fps_graph", "graph-samples", 100);
      samples = Math.max(10, Math.min(200, samples));
      int count = Math.min(samples, task_I);
      if (count <= 0) {
         return init_al_NetHelper_Str("fps_graph", "FPS", Integer.toString(fps));
      }

      int minFps = fps;
      int maxFps = fps;
      int read = node_I;

      for (int i = 0; i < count; i++) {
         read = (read - 1 + 200) % 200;
         int v = load_IArr[read];
         if (v < minFps) {
            minFps = v;
         }

         if (v > maxFps) {
            maxFps = v;
         }
      }

      int range = Math.max(1, maxFps - minFps);
      AutismHudManager.Inner_NetHelper line = new AutismHudManager.Inner_NetHelper();
      if (init_Z_Str_Str("fps_graph", "show-current-fps")) {
         line.init(Integer.toString(fps), load_I_Str_2("fps_graph", "value-color", -791321));
      }

      String[] bars = new String[]{"▁", "▂", "▃", "▄", "▅", "▆", "▇", "█"};
      read = node_I;
      int step = Math.max(1, count / 20);
      int shown = 0;
      line.init(" ", init_I_I_F(load_I_Str_2("fps_graph", "label-color", -4743522), 0.3F));

      for (int i = 0; i < count && shown < 20; i += step) {
         read = (node_I - i - 1 + 200) % 200;
         int v = load_IArr[read];
         int barIndex = Math.min(7, (v - minFps) * 8 / range);
         float fraction = (float)(v - minFps) / range;
         int r = Math.round(255.0F * (1.0F - fraction));
         int g = Math.round(255.0F * fraction);
         int barColor = 0xFF000000 | Math.min(255, r) << 16 | Math.min(255, g) << 8;
         line.init(bars[barIndex], barColor);
         shown++;
      }

      return line;
   }

   private static void read(List<AutismHudManager.Inner_NetHelper> lines) {
      boolean showTotal = init_Z_Str_Str("cps", "show-total");
      int leftCps = ClickRateTracker.init_I();
      int rightCps = ClickRateTracker.load_I();
      lines.add(init_al_NetHelper_Str("cps", "Left", Integer.toString(leftCps)));
      lines.add(init_al_NetHelper_Str("cps", "Right", Integer.toString(rightCps)));
      if (showTotal) {
         lines.add(init_al_NetHelper_Str("cps", "Total", Integer.toString(leftCps + rightCps)));
      }
   }

   private static String write_Str() {
      HitResult hit = init_Minecraft.hitResult;
      if (hit == null || hit.getType() == Type.MISS) {
         return "Nothing";
      } else if (hit instanceof EntityHitResult entityHit) {
         return entityHit.getEntity().getName().getString();
      } else if (hit instanceof BlockHitResult blockHit && init_Minecraft.level != null) {
         BlockPos pos = blockHit.getBlockPos();
         Identifier id = BuiltInRegistries.BLOCK.getKey(init_Minecraft.level.getBlockState(pos).getBlock());
         return RegistryDisplayNameCache.load(id.toString()) + " " + init_Str_D(pos.getX(), pos.getY(), pos.getZ());
      } else {
         return RegistryDisplayNameCache.update(hit.getType().name());
      }
   }

   private static String update_Str() {
      if (init_Minecraft.hitResult instanceof BlockHitResult && init_Minecraft.gameMode != null) {
         try {
            AutismMultiPlayerGameModeAccessor accessor = (AutismMultiPlayerGameModeAccessor)init_Minecraft.gameMode;
            return !accessor.autism$isDestroying() ? "0%" : Math.round(init_D_D(accessor.autism$getDestroyProgress(), 0.0, 1.0) * 100.0) + "%";
         } catch (Throwable ignored) {
            return "0%";
         }
      } else {
         return "0%";
      }
   }

   private static AutismHudManager.Inner_NetHelper sync_al_NetHelper() {
      double tps = autismclient.util.macro.ServerTickPhaseTracker.init_D();
      if (tps < flush_D || System.currentTimeMillis() - set_J > 5000L) {
         flush_D = tps;
         set_J = System.currentTimeMillis();
      }

      String value = String.format(Locale.ROOT, init_Z_Str_Str("tps", "tps-precise") ? "%.2f" : "%.1f", tps);
      int valueColor = load_I_Str_2("tps", "value-color", -791321);
      if (init_Z_Str_Str("tps", "tps-color-threshold")) {
         valueColor = tps >= 19.0 ? -12588930 : (tps >= 15.0 ? -10752 : -50373);
      }

      AutismHudManager.Inner_NetHelper line = new AutismHudManager.Inner_NetHelper();
      line.init("TPS: ", load_I_Str_2("tps", "label-color", -4743522));
      line.init(value, valueColor);
      if (init_Z_Str_Str("tps", "tps-show-jitter")) {
         line.init(" (" + String.format(Locale.ROOT, "%.1f", flush_D) + ")", init_I_I_F(valueColor, 0.65F));
      }

      return line;
   }

   private static AutismHudManager.Inner_NetHelper flush_al_NetHelper() {
      int ping = autismclient.util.macro.ServerTickPhaseTracker.init_I();
      int valueColor = load_I_Str_2("ping", "value-color", -791321);
      if (init_Z_Str_Str("ping", "ping-color-threshold")) {
         valueColor = ping <= 50 ? -12588930 : (ping <= 150 ? -10752 : -50373);
      }

      AutismHudManager.Inner_NetHelper line = new AutismHudManager.Inner_NetHelper();
      line.init("Ping: ", load_I_Str_2("ping", "label-color", -4743522));
      line.init(ping + " ms", valueColor);
      if (init_Z_Str_Str("ping", "ping-show-jitter")) {
         int old = pool_I;
         pool_I = ping;
         int jitter = old > 0 ? Math.abs(ping - old) : 0;
         if (jitter > 0) {
            line.init(" ±" + jitter, init_I_I_F(valueColor, 0.65F));
         }
      }

      return line;
   }

   private static AutismHudManager.Inner_NetHelper close_al_NetHelper() {
      ItemStack stack = init_Minecraft.player == null ? ItemStack.EMPTY : init_Minecraft.player.getMainHandItem();
      AutismHudManager.Inner_NetHelper line = new AutismHudManager.Inner_NetHelper();
      if (init_Z_Str_Str("durability", "show-item-name") && !stack.isEmpty()) {
         line.init(stack.getHoverName().getString() + ": ", load_I_Str_2("durability", "label-color", -4743522));
      } else {
         line.init("Durability: ", load_I_Str_2("durability", "label-color", -4743522));
      }

      if (stack.isEmpty()) {
         line.init("Empty", load_I_Str_2("durability", "value-color", -791321));
      } else if (!stack.isDamageableItem()) {
         line.init("No durability", load_I_Str_2("durability", "value-color", -791321));
      } else {
         int left = Math.max(0, stack.getMaxDamage() - stack.getDamageValue());
         int max = Math.max(1, stack.getMaxDamage());
         int pct = Math.round(left * 100.0F / max);
         int valColor = load_I_Str_2("durability", "value-color", -791321);
         if (init_Z_Str_Str("durability", "low-durability-warn")) {
            valColor = pct > 50 ? -12588930 : (pct > 20 ? -10752 : -50373);
         }

         line.init(String.format(Locale.ROOT, "%d / %d (%d%%)", left, max, pct), valColor);
      }

      return line;
   }

   private static AutismHudManager.Inner_NetHelper open_al_NetHelper() {
      AutismHudManager.Inner_NetHelper line = new AutismHudManager.Inner_NetHelper();
      HitResult hit = init_Minecraft.hitResult;
      line.init("Looking: ", load_I_Str_2("looking_at", "label-color", -4743522));
      if (hit != null && hit.getType() != Type.MISS) {
         String name;
         if (hit instanceof EntityHitResult entityHit) {
            name = entityHit.getEntity().getName().getString();
         } else if (hit instanceof BlockHitResult blockHit && init_Minecraft.level != null) {
            BlockState state = init_Minecraft.level.getBlockState(blockHit.getBlockPos());
            Identifier id = BuiltInRegistries.BLOCK.getKey(state.getBlock());
            if (init_Z_Str_Str("looking_at", "show-block-id")) {
               name = id.toString();
            } else {
               name = RegistryDisplayNameCache.load(id.toString());
            }

            if (init_Z_Str_Str("looking_at", "show-distance")) {
               double dist = load_Vec3().distanceTo(blockHit.getLocation());
               name = name + " " + String.format(Locale.ROOT, "%.1fm", dist);
            }
         } else {
            name = RegistryDisplayNameCache.update(hit.getType().name());
         }

         line.init(name, load_I_Str_2("looking_at", "value-color", -791321));
         return line;
      } else {
         line.init("Nothing", load_I_Str_2("looking_at", "value-color", -791321));
         return line;
      }
   }

   private static AutismHudManager.Inner_NetHelper poll_al_NetHelper() {
      AutismHudManager.Inner_NetHelper line = new AutismHudManager.Inner_NetHelper();
      if (init_Minecraft.hitResult instanceof BlockHitResult && init_Minecraft.gameMode != null && init_Minecraft.level != null) {
         double progress = 0.0;

         try {
            AutismMultiPlayerGameModeAccessor accessor = (AutismMultiPlayerGameModeAccessor)init_Minecraft.gameMode;
            if (accessor.autism$isDestroying()) {
               progress = accessor.autism$getDestroyProgress();
            }

            progress = Math.max(0.0, Math.min(1.0, progress));
         } catch (Throwable var6) {
         }

         int pct = (int)Math.round(progress * 100.0);
         if (init_Z_Str_Str("breaking_progress", "show-block-name")) {
            BlockPos pos = ((BlockHitResult)init_Minecraft.hitResult).getBlockPos();
            String blockName = RegistryDisplayNameCache.load(BuiltInRegistries.BLOCK.getKey(init_Minecraft.level.getBlockState(pos).getBlock()).toString());
            line.init(blockName + ": ", load_I_Str_2("breaking_progress", "label-color", -4743522));
         } else {
            line.init("Breaking: ", load_I_Str_2("breaking_progress", "label-color", -4743522));
         }

         line.init(pct + "%", pct >= 80 ? -12588930 : (pct >= 40 ? -10752 : load_I_Str_2("breaking_progress", "value-color", -791321)));
         return line;
      } else {
         line.init("Breaking: 0%", load_I_Str_2("breaking_progress", "label-color", -4743522));
         return line;
      }
   }

   private static AutismHudManager.Inner_NetHelper push() {
      AutismHudManager.Inner_NetHelper line = new AutismHudManager.Inner_NetHelper();
      line.init("Server: ", load_I_Str_2("server", "label-color", -4743522));
      if (init_Minecraft.getCurrentServer() != null && init_Minecraft.getCurrentServer().ip != null && !init_Minecraft.getCurrentServer().ip.isBlank()) {
         line.init(init_Minecraft.getCurrentServer().ip, load_I_Str_2("server", "value-color", -791321));
      } else if (init_Minecraft.hasSingleplayerServer()) {
         line.init("Singleplayer", load_I_Str_2("server", "value-color", -791321));
      } else {
         line.init("N/A", load_I_Str_2("server", "value-color", -791321));
      }

      return line;
   }

   private static AutismHudManager.Inner_NetHelper pop() {
      String w = flush_Str();
      AutismHudManager.Inner_NetHelper line = new AutismHudManager.Inner_NetHelper();
      line.init("Weather: ", load_I_Str_2("weather", "label-color", -4743522));
      int var10000;
      switch (w) {
         case "Thunder":
            var10000 = -50373;
            break;
         case "Rain":
            var10000 = -10036737;
            break;
         default:
            var10000 = load_I_Str_2("weather", "value-color", -791321);
      }

      int wColor = var10000;
      line.init(w, wColor);
      if (init_Z_Str_Str("weather", "show-temperature") && init_Minecraft.level != null) {
         Holder<Biome> biome = init_Minecraft.level.getBiome(init_BlockPos());
         float temp = biome.value().getBaseTemperature();
         line.init(" " + String.format(Locale.ROOT, "%.1f°C", temp), init_I_I_F(wColor, 0.65F));
      }

      return line;
   }

   private static AutismHudManager.Inner_NetHelper tick() {
      String b = close_Str();
      AutismHudManager.Inner_NetHelper line = new AutismHudManager.Inner_NetHelper();
      line.init("Biome: ", load_I_Str_2("biome", "label-color", -4743522));
      line.init(b, load_I_Str_2("biome", "value-color", -791321));
      return line;
   }

   private static AutismHudManager.Inner_NetHelper run() {
      String fmt = init_Str_Str_Str("world_time", "world-time-format");
      AutismHudManager.Inner_NetHelper line = new AutismHudManager.Inner_NetHelper();
      line.init("Time: ", load_I_Str_2("world_time", "label-color", -4743522));
      if ("ticks".equals(fmt) && init_Minecraft.level != null) {
         line.init(Long.toString(init_J() % 24000L), load_I_Str_2("world_time", "value-color", -791321));
      } else {
         line.init(open_Str(), load_I_Str_2("world_time", "value-color", -791321));
      }

      if (init_Z_Str_Str("world_time", "show-day") && init_Minecraft.level != null) {
         long day = init_J() / 24000L;
         line.init(" Day " + day, init_I_I_F(load_I_Str_2("world_time", "value-color", -791321), 0.65F));
      }

      return line;
   }

   private static AutismHudManager.Inner_NetHelper get() {
      String fmt = init_Str_Str_Str("real_time", "real-time-format");
      DateTimeFormatter formatter = "24h".equals(fmt) ? DateTimeFormatter.ofPattern("HH:mm") : DateTimeFormatter.ofPattern("h:mm a", Locale.US);
      AutismHudManager.Inner_NetHelper line = new AutismHudManager.Inner_NetHelper();
      line.init("Time: ", load_I_Str_2("real_time", "label-color", -4743522));
      line.init(LocalTime.now().format(formatter), load_I_Str_2("real_time", "value-color", -791321));
      if (init_Z_Str_Str("real_time", "show-date")) {
         line.init(" " + LocalDate.now().format(DateTimeFormatter.ofPattern("MM/dd")), init_I_I_F(load_I_Str_2("real_time", "value-color", -791321), 0.65F));
      }

      return line;
   }

   private static String sync_Str() {
      if (init_Minecraft.getCurrentServer() != null && init_Minecraft.getCurrentServer().ip != null && !init_Minecraft.getCurrentServer().ip.isBlank()) {
         return init_Minecraft.getCurrentServer().ip;
      } else {
         return init_Minecraft.hasSingleplayerServer() ? "Singleplayer" : "N/A";
      }
   }

   private static String flush_Str() {
      if (init_Minecraft.level == null) {
         return "N/A";
      } else if (init_Minecraft.level.isThundering()) {
         return "Thunder";
      } else {
         return init_Minecraft.level.isRaining() ? "Rain" : "Clear";
      }
   }

   private static String close_Str() {
      if (init_Minecraft.level != null && init_Minecraft.player != null) {
         try {
            return init_Minecraft.level.getBiome(init_BlockPos()).unwrapKey().map(key -> RegistryDisplayNameCache.update(key.identifier().toString())).orElse("Unknown");
         } catch (Throwable ignored) {
            return "Unknown";
         }
      } else {
         return "N/A";
      }
   }

   private static String open_Str() {
      if (init_Minecraft.level == null) {
         return "N/A";
      }

      long time = init_J() % 24000L;
      long hours = (time / 1000L + 6L) % 24L;
      long minutes = time % 1000L * 60L / 1000L;
      return String.format(Locale.ROOT, "%02d:%02d", hours, minutes);
   }

   private static long init_J() {
      return init_Minecraft.level == null ? 0L : init_Minecraft.level.getOverworldClockTime();
   }

   private static String poll_Str() {
      if (init_Minecraft.player == null) {
         return "N";
      }

      String var10000;
      switch (Direction.fromYRot(init_F())) {
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

   private static AutismHudManager.Inner_NetHelper set() {
      Item target = init_Item();
      AutismHudManager.Inner_NetHelper line = new AutismHudManager.Inner_NetHelper();
      if (init_Z_Str_Str("item_counter", "item-show-name")) {
         line.init(init_Str_Item(target) + ": ", load_I_Str_2("item_counter", "label-color", -4743522));
      }

      line.init(Integer.toString(init_I_Item(target)), load_I_Str_2("item_counter", "value-color", -791321));
      return line;
   }

   private static Item init_Item() {
      if (init_Minecraft.player == null) {
         return null;
      }

      if (init_Z_Str_Str("item_counter", "item-count-held")) {
         ItemStack held = init_Minecraft.player.getMainHandItem();
         return held.isEmpty() ? null : held.getItem();
      }

      String raw = init_Str_Str_Str("item_counter", "item-id");
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

   private static String init_Str_Item(Item target) {
      if (target == null) {
         return init_Z_Str_Str("item_counter", "item-count-held") ? "Hand" : "Items";
      } else {
         return RegistryDisplayNameCache.init(BuiltInRegistries.ITEM.getKey(target).toString());
      }
   }

   private static int init_I_Item(Item target) {
      if (init_Minecraft.player != null && target != null) {
         int count = 0;

         for (int i = 0; i < init_Minecraft.player.getInventory().getContainerSize(); i++) {
            ItemStack stack = init_Minecraft.player.getInventory().getItem(i);
            if (!stack.isEmpty() && stack.getItem() == target) {
               count += stack.getCount();
            }
         }

         return count;
      } else {
         return 0;
      }
   }

   private static String init_Str_D(double x, double y, double z) {
      return String.format(Locale.ROOT, "%d, %d, %d", (int)Math.floor(x), (int)Math.floor(y), (int)Math.floor(z));
   }

   private static void write(List<AutismHudManager.Inner_NetHelper> lines) {
      if (init_Minecraft.player != null && !init_Minecraft.player.getActiveEffects().isEmpty()) {
         for (MobEffectInstance effect : init_Minecraft.player.getActiveEffects()) {
            String name = Component.translatable(effect.getDescriptionId()).getString();
            int amplifier = effect.getAmplifier() + 1;
            if (amplifier > 1) {
               name = name + " " + init_Str_I(amplifier);
            }

            int seconds = Math.max(0, effect.getDuration() / 20);
            lines.add(init_al_NetHelper_Str("potion_timers", name, String.format(Locale.ROOT, "%d:%02d", seconds / 60, seconds % 60)));
         }
      } else {
         lines.add(init_al_NetHelper_Str("potion_timers", "Effects", "None"));
      }
   }

   private static String init_Str_I(int value) {
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

   private static int init_I_Font_al_NetHelper(Font font, AutismHudManager.Inner_NetHelper line) {
      int[] widths = new int[line.init_List.size()];
      int width = 0;

      for (int i = 0; i < line.init_List.size(); i++) {
         AutismHudManager.Inner_StateCache segment = line.init_List.get(i);
         int w = autismclient.gui.f.b.k.init(font, segment.init_Str, init_CharEncoder.init_Identifier_l(autismclient.gui.f.b.l.read), segment.init_I);
         widths[i] = w;
         width += w;
      }

      line.init_IArr = widths;
      return width;
   }

   private static int read_I(String id) {
      return init_CharEncoder.init_I_l(autismclient.gui.f.b.l.read) + sync_I(id);
   }

   private static int write_I(String id) {
      return init_CharEncoder.init_I_l(autismclient.gui.f.b.l.read) + close_I(id) * 2;
   }

   private static int update_I(String id) {
      return init_I_I_2(init_I_Str_2(id, "stair-snap", 2), 0, 24);
   }

   private static int sync_I(String id) {
      return init_I_I_2(init_I_Str_2(id, "line-gap", 0), 0, 10);
   }

   private static void init_V_AutismConfig(AutismConfig config) {
      read(config);
      AutismConfig.ModuleState old = config.modules == null ? null : config.modules.get("hud");
      Map<String, String> settings = old != null && old.settings != null ? old.settings : Map.of();
      int x = read(settings.get("x"), 8);
      int y = read(settings.get("y"), 8);
      boolean modules = init_Z_Str_Z(settings.get("modules"), true);
      boolean metrics = init_Z_Str_Z(settings.get("metrics"), true);
      boolean coords = init_Z_Str_Z(settings.get("coords"), true);
      init_V_AutismConfig(config, "active_modules", modules, x, y);
      init_V_AutismConfig(config, "tps", metrics, x, y + 24);
      init_V_AutismConfig(config, "coordinates", coords, x, y + 44);
      init_V_AutismConfig(config, "nether_coords", coords, x, y + 64);
      config.hudLayoutMigrated = true;
      config.save();
   }

   private static void init_V_AutismConfig(AutismConfig config, String id, boolean enabled, int x, int y) {
      AutismConfig.HudElementState state = config.hudElements.computeIfAbsent(id, ignored -> load_AutismConfig_HudEl(id));
      state.enabled = enabled;
      state.f_x_I = x;
      state.f_y_I = y;
      if (state.settings == null) {
         state.settings = new LinkedHashMap<>();
      }

      for (Entry<String, String> entry : init_Map_Str(id).entrySet()) {
         state.settings.putIfAbsent(entry.getKey(), entry.getValue());
      }
   }

   private static void load(AutismConfig config) {
      if (config != null && config.hudElements != null) {
         int rowH = update_I();
         int oldRowH = init_CharEncoder.init_I_l(autismclient.gui.f.b.l.read) + 2;
         int oldLogoBottom = load(180) + 1;
         int oldSpacedLogoBottom = load(180) + 6;
         int newLogoBottom = sync_I();
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
            init(fps, tps, ping, speed, compass, coordinates, nether, rotation, antiVanish, rowH, oldRowH, oldLogoBottom, oldSpacedLogoBottom, newLogoBottom);
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

   private static void init(
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
      if (init_Z_AutismConfig_HudEl(fps, 0, oldLogoBottom) || init_Z_AutismConfig_HudEl(fps, 0, oldSpacedLogoBottom)) {
         fps.f_y_I = newLogoBottom;
      }

      if (init_Z_AutismConfig_HudEl(tps, 0, oldLogoBottom + rowH) || init_Z_AutismConfig_HudEl(tps, 0, oldSpacedLogoBottom + rowH)) {
         tps.enabled = false;
         tps.f_y_I = newLogoBottom;
      }

      if (init_Z_AutismConfig_HudEl(ping, 0, oldLogoBottom + rowH * 2) || init_Z_AutismConfig_HudEl(ping, 0, oldSpacedLogoBottom + rowH * 2)) {
         ping.enabled = false;
         ping.f_y_I = newLogoBottom;
      }

      if (init_Z_AutismConfig_HudEl(speed, 0, oldLogoBottom + rowH * 3) || init_Z_AutismConfig_HudEl(speed, 0, oldSpacedLogoBottom + rowH)) {
         speed.f_y_I = newLogoBottom + rowH;
      }

      if (init_Z_AutismConfig_HudEl(compass, 0, oldLogoBottom + rowH * 5) || init_Z_AutismConfig_HudEl(compass, 0, oldSpacedLogoBottom + rowH * 2)) {
         compass.f_y_I = newLogoBottom + rowH * 2;
      }

      if (init_Z_AutismConfig_HudEl(antiVanish)) {
         load(antiVanish, "TOP_LEFT", 0, newLogoBottom + rowH * 2);
      }

      int rightStackX = -1;
      int rightStackBottom = -2;
      if (init_Z_AutismConfig_HudEl_2(coordinates, nether, rotation)
         || init_Z_AutismConfig_HudEl_2(coordinates, nether, rotation, 0, oldRowH)
         || init_Z_AutismConfig_HudEl_2(coordinates, nether, rotation, 0, rowH)) {
         load(coordinates, "BOTTOM_RIGHT", rightStackX, rightStackBottom - rowH * 2);
         load(nether, "BOTTOM_RIGHT", rightStackX, rightStackBottom - rowH);
         load(rotation, "BOTTOM_RIGHT", rightStackX, rightStackBottom);
      }

      if (init_Z_AutismConfig_HudEl(compass, oldLogoBottom, oldSpacedLogoBottom, newLogoBottom, rowH)) {
         load(compass, "TOP_CENTER", 0, 2);
      }
   }

   private static boolean init_Z_AutismConfig_HudEl(AutismConfig.HudElementState state, int x, int y) {
      return state != null && "TOP_LEFT".equals(state.anchor) && state.f_x_I == x && state.f_y_I == y;
   }

   private static boolean init_Z_AutismConfig_HudEl(AutismConfig.HudElementState state) {
      return init_Z_AutismConfig_HudEl(state, "TOP_RIGHT", 0, 72) || init_Z_AutismConfig_HudEl(state, "TOP_LEFT", 0, 72);
   }

   private static boolean init_Z_AutismConfig_HudEl_2(
      AutismConfig.HudElementState coordinates, AutismConfig.HudElementState nether, AutismConfig.HudElementState rotation
   ) {
      int legacy = 0;
      if (load(coordinates)) {
         legacy++;
      }

      if (load(nether)) {
         legacy++;
      }

      if (load(rotation)) {
         legacy++;
      }

      return legacy >= 2;
   }

   private static boolean load(AutismConfig.HudElementState state) {
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
      return init_Z_AutismConfig_HudEl(coordinates, "BOTTOM_RIGHT", bottomOffset, -rowH * 2)
         && init_Z_AutismConfig_HudEl(nether, "BOTTOM_RIGHT", bottomOffset, -rowH)
         && init_Z_AutismConfig_HudEl(rotation, "BOTTOM_RIGHT", bottomOffset, 0);
   }

   private static boolean init_Z_AutismConfig_HudEl(AutismConfig.HudElementState state, String anchor, int x, int y) {
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

   private static boolean load(AutismConfig.HudElementState state, String anchor, int x, int y) {
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

   private static AutismConfig.HudElementState load_AutismConfig_HudEl(String id) {
      AutismConfig.HudElementState state = new AutismConfig.HudElementState();
      state.enabled = update_Z(id);
      state.scale = 1.0;
      state.anchor = read_Str(id);
      int[] position = init_IArr_Str(id);
      state.f_x_I = position[0];
      state.f_y_I = position[1];
      state.settings = new LinkedHashMap<>(init_Map_Str(id));
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

      int rowH = update_I();
      int logoBottom = sync_I();
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

   private static Map<String, String> init_Map_Str(String id) {
      Map<String, String> cached = load_Map.get(id);
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
      load_Map.put(id, immutable);
      return immutable;
   }

   private static int flush_I(String id) {
      return init_I_I_2(init_I_Str_2(id, "padding", 0), 0, 16);
   }

   private static int close_I(String id) {
      return init_I_I_2(init_I_Str_2(id, "vertical-padding", 0), 0, 16);
   }

   private static int update_I() {
      return init_CharEncoder.init_I_l(autismclient.gui.f.b.l.read);
   }

   private static int open(String id) {
      return init_I_I_2(init_I_Str_2(id, "logo-width", 180), 48, 420);
   }

   private static int load(int width) {
      return Math.max(1, Math.round(width * 0.0942029F));
   }

   private static int sync_I() {
      return load(180) + 2;
   }

   private static int poll(String id) {
      return init_I_I_2(init_I_Str_2(id, "logo-right-padding", 3), 0, 16);
   }

   private static int push(String id) {
      return init_I_I_2(init_I_Str_2(id, "gif-width", 120), 16, 512);
   }

   private static int pop(String id) {
      return init_I_I_2(init_I_Str_2(id, "gif-height", 120), 16, 512);
   }

   private static double init_D_Str(String id) {
      String value = init_Str_Str_Str(id, "gif-quality");
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

   private static double load_D(String id) {
      double fps = init_D_Str(id, "gif-fps", 0.0);
      return fps <= 0.0 ? 0.0 : Math.min(60.0, fps);
   }

   private static void init_V_Str_Str(String id, String path) {
      int[] natural = autismclient.util.c.GifTextureManager.init_IArr_Str(path);
      if (natural != null && natural[0] > 0 && natural[1] > 0) {
         String signature = path + "|" + natural[0] + "x" + natural[1];
         if (!signature.equals(write_Map.get(id))) {
            int maxDim = Math.max(push(id), pop(id));
            double scale = (double)maxDim / Math.max(natural[0], natural[1]);
            int w = init_I_I_2((int)Math.round(natural[0] * scale), 16, 512);
            int h = init_I_I_2((int)Math.round(natural[1] * scale), 16, 512);
            write_Map.put(id, signature);
            init_V_Str(id, "gif-width", Integer.toString(w));
            init_V_Str(id, "gif-height", Integer.toString(h));
         }
      }
   }

   private static int tick(String id) {
      return init_I_I_2(init_I_Str_2(id, "compass-width", 112), 72, 200);
   }

   static int read_I() {
      if (init_Minecraft.getWindow() == null) {
         return 854;
      }

      int sw = cA.init_I();
      return sw > 0 ? sw : 854;
   }

   static int write_I() {
      if (init_Minecraft.getWindow() == null) {
         return 480;
      }

      int sh = cA.load_I();
      return sh > 0 ? sh : 480;
   }

   private static int init_I_Str(String anchor, int x, int width) {
      String normalized = anchor == null ? "" : anchor.toUpperCase(Locale.ROOT);
      if (normalized.contains("RIGHT")) {
         return read_I() + x - width;
      } else {
         return normalized.contains("CENTER") ? (read_I() - width) / 2 + x : x;
      }
   }

   private static int load_I_Str(String anchor, int y, int height) {
      String normalized = anchor == null ? "" : anchor.toUpperCase(Locale.ROOT);
      if (normalized.contains("BOTTOM")) {
         return write_I() + y - height;
      } else {
         return normalized.contains("MIDDLE") ? (write_I() - height) / 2 + y : y;
      }
   }

   private static int read(String id, int x, int width) {
      return init_I_I_2(x, get(id), update(id, read_I(), width));
   }

   private static int write(String id, int y, int height) {
      return init_I_I_2(y, set(id), sync(id, write_I(), height));
   }

   private static int get(String id) {
      return "watermark".equals(id) ? 0 : 1;
   }

   private static int set(String id) {
      return !"watermark".equals(id) && !"active_modules".equals(id) ? 2 : 0;
   }

   private static int update(String id, int screenW, int width) {
      int safe = get(id);
      return Math.max(safe, screenW - Math.max(0, width) - safe);
   }

   private static int sync(String id, int screenH, int height) {
      int safe = set(id);
      return Math.max(safe, screenH - Math.max(0, height) - safe);
   }

   private static int load(String key, int fallback) {
      return load_I_Str_2("active_modules", key, fallback);
   }

   private static int load_I_Str_2(String id, String key, int fallback) {
      if (!init_Z_Str_Str(id, "use-custom-colors")) {
         int themed = init_I_Str_I(load_Str(id, key), fallback);
         return ThemePaletteManager.init_I_I_ct_DataUtil(themed, init_ct_DataUtil_Str(key));
      } else {
         return init_I_Str_I(init_Str_Str_Str(id, key), fallback);
      }
   }

   private static ThemePaletteManager.Inner_DataUtil init_ct_DataUtil_Str(String key) {
      if (key == null) {
         return ThemePaletteManager.Inner_DataUtil.ACCENT;
      }

      ThemePaletteManager.Inner_DataUtil var10000;
      switch (key) {
         case "outline-color":
            var10000 = ThemePaletteManager.Inner_DataUtil.OUTLINE;
            break;
         case "label-color":
         case "value-color":
         case "text-color":
         case "module-info-color":
         case "keystroke-text-color":
            var10000 = ThemePaletteManager.Inner_DataUtil.TEXT;
            break;
         case "background-color":
         case "keystroke-idle-color":
            var10000 = ThemePaletteManager.Inner_DataUtil.BACKDROP;
            break;
         default:
            var10000 = ThemePaletteManager.Inner_DataUtil.ACCENT;
      }

      return var10000;
   }

   private static int init_I_Z(boolean selected) {
      return ThemePaletteManager.init_I_I_ct_DataUtil(selected ? 942280982 : 571675672, ThemePaletteManager.Inner_DataUtil.BACKDROP);
   }

   private static int init_I_I_F(int color, float alpha) {
      if (alpha >= 0.999F) {
         return color;
      }

      int a = (int)((color >>> 24 & 0xFF) * Math.max(0.0F, Math.min(1.0F, alpha)));
      return color & 16777215 | a << 24;
   }

   private static int init_I_I(int from, int to, double t) {
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

   private static int load_I_I(int color, int base, double amount) {
      return init_I_I(color, base, amount);
   }

   private static void read(AutismConfig config) {
      if (config.hudElements == null) {
         config.hudElements = new LinkedHashMap<>();
      }
   }

   private static boolean init_Z_Module(Module module, String id, boolean fallback) {
      AutismHudManager.Inner_UiHelper option = new AutismHudManager.Inner_UiHelper(module, id);
      return !option.init_Z ? fallback : Boolean.parseBoolean(option.init_Str);
   }

   private static boolean init_Z_Str_Z(String value, boolean fallback) {
      return value == null ? fallback : Boolean.parseBoolean(value);
   }

   private static int read(String value, int fallback) {
      try {
         return Integer.parseInt(value);
      } catch (Exception ignored) {
         return fallback;
      }
   }

   public static int init_I_Str_I(String value, int fallback) {
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

   private static int init_I_I_2(int value, int min, int max) {
      return Math.max(min, Math.min(max, value));
   }

   private static double init_D_D(double value, double min, double max) {
      return Math.max(min, Math.min(max, value));
   }

   private static boolean init_Z_I(int mouseX, int mouseY, AutismHudManager.Inner_BufferHelper bounds) {
      return init_Z_I(mouseX, mouseY, bounds.init_I, bounds.load, bounds.read, bounds.write);
   }

   private static boolean init_Z_I(int mouseX, int mouseY, int x, int y, int w, int h) {
      return mouseX >= x && mouseX < x + w && mouseY >= y && mouseY < y + h;
   }

   private static boolean sync_Z(String anchor) {
      return anchor != null && anchor.toUpperCase(Locale.ROOT).contains("RIGHT");
   }

   private static boolean flush_Z(String id) {
      return ("tps".equals(id) || "ping".equals(id)) && init_AutismConfig_HudEl_Str("fps").enabled;
   }

   private static int read(int measuredWidth) {
      long now = System.currentTimeMillis();
      int width = Math.max(1, measuredWidth);
      if (width >= poll_I) {
         poll_I = width;
         write_J = now + 350L;
         return poll_I;
      }

      if (now < write_J) {
         return poll_I;
      }

      poll_I = Math.max(width, poll_I - 6);
      return poll_I;
   }

   private static void init_V_GuiGraphicsExtract_3(
      GuiGraphicsExtractor context, Font font, String id, AutismHudManager.Inner_HashUtil rect, int color, List<AutismHudManager.Inner_HashUtil> blockers
   ) {
      List<AutismHudManager.Inner_HashUtil> occluders = init_List_Font(font, id, rect);
      init_V_GuiGraphicsExtract(context, rect, occluders, color);
      load(context, rect, blockers != null ? blockers : load_List_Font(font, id, rect), color);
   }

   private static AutismHudManager.Inner_HashUtil init_al_HashUtil_al_HashUtil(AutismHudManager.Inner_HashUtil rect) {
      if (rect == null) {
         return null;
      }

      int screenW = read_I();
      int screenH = write_I();
      int safeX = get(rect.init_Str());
      int safeY = set(rect.init_Str());
      int x = rect.read();
      int y = rect.write();
      int right = rect.init_I_2();
      int bottom = rect.load_I();
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

      x = init_I_I_2(x, 0, screenW);
      y = init_I_I_2(y, 0, screenH);
      right = init_I_I_2(right, x, screenW);
      bottom = init_I_I_2(bottom, y, screenH);
      return new AutismHudManager.Inner_HashUtil(rect.init_Str(), x, y, Math.max(0, right - x), Math.max(0, bottom - y));
   }

   private static AutismHudManager.Inner_HashUtil init_al_HashUtil_al_HashUtil_al_SyncManager(AutismHudManager.Inner_HashUtil contentRect, AutismHudManager.Inner_SyncManager style) {
      if (contentRect == null) {
         return null;
      }

      AutismHudManager.Inner_HashUtil rect = contentRect;
      if (style.load_Z()) {
         int pad = 2;
         rect = new AutismHudManager.Inner_HashUtil(
            contentRect.init_Str(), contentRect.read() - pad, contentRect.write() - pad, contentRect.update() + pad * 2, contentRect.sync() + pad * 2
         );
      }

      return init_al_HashUtil_al_HashUtil(rect);
   }

   private static Map<String, Integer> init_Map() {
      Map<String, Integer> index = new HashMap<>();

      for (int i = 0; i < init_List.size(); i++) {
         index.put(init_List.get(i), i);
      }

      return index;
   }

   private static List<AutismHudManager.Inner_JsonUtil> init_List_Font_Z(Font font, boolean editor) {
      boolean changed = false;

      for (int i = 0; i < init_List.size(); i++) {
         String id = init_List.get(i);
         boolean hiddenBedDanger = "bed_danger".equals(id) && !editor;
         if (hiddenBedDanger && !AutismLiteVariant.enabled()) {
            hiddenBedDanger = !autismclient.modules.BedDangerHudRenderer.init();
         }

         AutismHudManager.Inner_CachePool current = !flush_Z(id) && init_AutismConfig_HudEl_Str(id).enabled && !hiddenBedDanger
            ? init_al_CachePool_Str_Font(id, font)
            : null;
         if (init_al_CachePoolArr[i] != current) {
            init_al_CachePoolArr[i] = current;
            changed = true;
         }
      }

      long occluderSignature = load_J();
      if (!changed && occluderSignature == put_J) {
         return update_List;
      }

      List<AutismHudManager.Inner_JsonUtil> entries = new ArrayList<>();

      for (int i = 0; i < init_List.size(); i++) {
         String other = init_List.get(i);
         AutismHudManager.Inner_CachePool cached = init_al_CachePoolArr[i];
         if (cached != null) {
            AutismHudManager.Inner_SyncManager style = cached.init_al_SyncManager();
            if (style.init_Z() || style.load_Z()) {
               for (AutismHudManager.Inner_HashUtil rect : init_List_Str_Font(other, font)) {
                  entries.add(new AutismHudManager.Inner_JsonUtil(other, i, rect, style.init_Z()));
               }
            }
         }
      }

      put_J = occluderSignature;
      update_List = List.copyOf(entries);
      return update_List;
   }

   private static long load_J() {
      long signature = load_List.size();

      for (AutismHudManager.Inner_BufferHelper bounds : load_List) {
         if (bounds != null) {
            signature = signature * 31L + bounds.read();
            signature = signature * 31L + bounds.write();
            signature = signature * 31L + bounds.update();
            signature = signature * 31L + bounds.sync();
         }
      }

      return signature;
   }

   private static List<AutismHudManager.Inner_HashUtil> init_List_Font(Font font, String id, AutismHudManager.Inner_HashUtil self) {
      List<AutismHudManager.Inner_HashUtil> occluders = new ArrayList<>();
      List<AutismHudManager.Inner_JsonUtil> snapshot = write_List;
      if (snapshot != null) {
         int selfOrder = update_Map.getOrDefault(id, -1);

         for (AutismHudManager.Inner_JsonUtil entry : snapshot) {
            if (entry.init_Z() && entry.init_I() <= selfOrder) {
               AutismHudManager.Inner_HashUtil rect = entry.init_al_HashUtil();
               if (!rect.init(self) && (entry.init_I() != selfOrder || init_Z_al_HashUtil_al_HashUtil(rect, self)) && rect.load(self)) {
                  occluders.add(rect);
               }
            }
         }

         return occluders;
      } else {
         int selfOrder = init_List.indexOf(id);

         for (String other : init_List) {
            if (!flush_Z(other) && init_AutismConfig_HudEl_Str(other).enabled && init_Z_Str_Str(other, "background")) {
               int otherOrder = init_List.indexOf(other);
               if (otherOrder <= selfOrder) {
                  for (AutismHudManager.Inner_HashUtil rect : init_List_Str_Font(other, font)) {
                     if (!rect.init(self) && (otherOrder != selfOrder || init_Z_al_HashUtil_al_HashUtil(rect, self)) && rect.load(self)) {
                        occluders.add(rect);
                     }
                  }
               }
            }
         }

         return occluders;
      }
   }

   private static boolean init_Z_al_HashUtil_al_HashUtil(AutismHudManager.Inner_HashUtil rect, AutismHudManager.Inner_HashUtil self) {
      if (rect.write() != self.write()) {
         return rect.write() < self.write();
      } else if (rect.read() != self.read()) {
         return rect.read() < self.read();
      } else {
         return rect.update() != self.update() ? rect.update() < self.update() : rect.sync() < self.sync();
      }
   }

   private static void init_V_GuiGraphicsExtract(GuiGraphicsExtractor context, AutismHudManager.Inner_HashUtil rect, List<AutismHudManager.Inner_HashUtil> occluders, int color) {
      if (occluders.isEmpty()) {
         autismclient.gui.f.b.k.init(context, rect.read(), rect.write(), rect.init_I_2(), rect.load_I(), color);
      } else {
         List<Integer> cuts = new ArrayList<>();
         cuts.add(rect.write());
         cuts.add(rect.load_I());

         for (AutismHudManager.Inner_HashUtil other : occluders) {
            int top = init_I_I_2(other.write(), rect.write(), rect.load_I());
            int bottom = init_I_I_2(other.load_I(), rect.write(), rect.load_I());
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

               for (AutismHudManager.Inner_HashUtil other : occluders) {
                  if (other.write() < bandBottom && other.load_I() > bandY) {
                     int left = init_I_I_2(other.read(), rect.read(), rect.init_I_2());
                     int right = init_I_I_2(other.init_I_2(), rect.read(), rect.init_I_2());
                     if (left < right) {
                        blocked.add(new int[]{left, right});
                     }
                  }
               }

               init_V_GuiGraphicsExtract_2(
                  context, rect.read(), rect.init_I_2(), blocked, 0, (a, b) -> autismclient.gui.f.b.k.init(context, a, bandY, b, bandBottom, color)
               );
            }
         }
      }
   }

   private static void load(GuiGraphicsExtractor context, AutismHudManager.Inner_HashUtil rect, List<AutismHudManager.Inner_HashUtil> blockers, int color) {
      for (AutismHudManager.Inner_HashUtil other : blockers) {
         if (rect.init_I_2() <= other.read() && other.read() - rect.init_I_2() <= 1) {
            int y0 = Math.max(rect.write(), other.write());
            int y1 = Math.min(rect.load_I(), other.load_I());
            if (y0 < y1 && rect.init_I_2() < other.read()) {
               autismclient.gui.f.b.k.init(context, rect.init_I_2(), y0, other.read(), y1, color);
            }
         } else if (other.init_I_2() <= rect.read() && rect.read() - other.init_I_2() <= 1) {
            int y0 = Math.max(rect.write(), other.write());
            int y1 = Math.min(rect.load_I(), other.load_I());
            if (y0 < y1 && other.init_I_2() < rect.read()) {
               autismclient.gui.f.b.k.init(context, other.init_I_2(), y0, rect.read(), y1, color);
            }
         }

         if (rect.load_I() <= other.write() && other.write() - rect.load_I() <= 1) {
            int x0 = Math.max(rect.read(), other.read());
            int x1 = Math.min(rect.init_I_2(), other.init_I_2());
            if (x0 < x1 && rect.load_I() < other.write()) {
               autismclient.gui.f.b.k.init(context, x0, rect.load_I(), x1, other.write(), color);
            }
         } else if (other.load_I() <= rect.write() && rect.write() - other.load_I() <= 1) {
            int x0 = Math.max(rect.read(), other.read());
            int x1 = Math.min(rect.init_I_2(), other.init_I_2());
            if (x0 < x1 && other.load_I() < rect.write()) {
               autismclient.gui.f.b.k.init(context, x0, other.load_I(), x1, rect.write(), color);
            }
         }
      }
   }

   private static void init_V_GuiGraphicsExtract_5(GuiGraphicsExtractor context, Font font, String id, int x, int y, int w, int h, int color, int width) {
      AutismHudManager.Inner_HashUtil rect = new AutismHudManager.Inner_HashUtil(id, x, y, w, h);
      init_V_GuiGraphicsExtract_2(context, rect, load_List_Font(font, id, rect), color, width);
   }

   private static void init_V_GuiGraphicsExtract_2(GuiGraphicsExtractor context, AutismHudManager.Inner_HashUtil rect, List<AutismHudManager.Inner_HashUtil> blockers, int color, int width) {
      int size = Math.max(1, width);
      int screenW = read_I();
      int screenH = write_I();
      int x0 = init_I_I_2(rect.read(), 0, screenW);
      int x1 = init_I_I_2(rect.init_I_2(), 0, screenW);
      int y0 = init_I_I_2(rect.write(), 0, screenH);
      int y1 = init_I_I_2(rect.load_I(), 0, screenH);
      if (x0 < x1 && y0 < y1) {
         if (y0 > 0) {
            init_V_GuiGraphicsExtract(context, blockers, x0, x1, y0, y0, size, color, true);
         }

         if (y1 < screenH) {
            init_V_GuiGraphicsExtract(context, blockers, x0, x1, Math.max(y0, y1 - size), y1, size, color, false);
         }

         if (x0 > 0) {
            load(context, blockers, y0, y1, x0, x0, size, color, true);
         }

         if (x1 < screenW) {
            load(context, blockers, y0, y1, Math.max(x0, x1 - size), x1, size, color, false);
         }
      }
   }

   private static void init_V_GuiGraphicsExtract(
      GuiGraphicsExtractor context, List<AutismHudManager.Inner_HashUtil> blockers, int fromX, int toX, int drawY, int edgeY, int size, int color, boolean top
   ) {
      List<int[]> blocked = new ArrayList<>();

      for (AutismHudManager.Inner_HashUtil other : blockers) {
         boolean connected = top ? other.write() < edgeY && other.load_I() >= edgeY - 1 : other.write() <= edgeY + 1 && other.load_I() > edgeY;
         if (connected) {
            int overlapStart = Math.max(fromX, other.read());
            int overlapEnd = Math.min(toX, other.init_I_2());
            if (overlapStart < overlapEnd) {
               blocked.add(new int[]{overlapStart, overlapEnd});
            }
         }
      }

      init_V_GuiGraphicsExtract_2(context, fromX, toX, blocked, size, (a, b) -> autismclient.gui.f.b.k.init(context, a, drawY, b, drawY + size, color));
   }

   private static void load(
      GuiGraphicsExtractor context, List<AutismHudManager.Inner_HashUtil> blockers, int fromY, int toY, int drawX, int edgeX, int size, int color, boolean left
   ) {
      List<int[]> blocked = new ArrayList<>();

      for (AutismHudManager.Inner_HashUtil other : blockers) {
         boolean connected = left ? other.read() < edgeX && other.init_I_2() >= edgeX - 1 : other.read() <= edgeX + 1 && other.init_I_2() > edgeX;
         if (connected) {
            int overlapStart = Math.max(fromY, other.write());
            int overlapEnd = Math.min(toY, other.load_I());
            if (overlapStart < overlapEnd) {
               blocked.add(new int[]{overlapStart, overlapEnd});
            }
         }
      }

      init_V_GuiGraphicsExtract_2(context, fromY, toY, blocked, size, (a, b) -> autismclient.gui.f.b.k.init(context, drawX, a, drawX + size, b, color));
   }

   private static List<AutismHudManager.Inner_HashUtil> load_List_Font(Font font, String id, AutismHudManager.Inner_HashUtil self) {
      List<AutismHudManager.Inner_HashUtil> blockers = new ArrayList<>();
      List<AutismHudManager.Inner_JsonUtil> snapshot = write_List;
      if (snapshot != null) {
         for (AutismHudManager.Inner_JsonUtil entry : snapshot) {
            if (!entry.init_Str().equals(id) || "active_modules".equals(entry.init_Str())) {
               AutismHudManager.Inner_HashUtil rect = entry.init_al_HashUtil();
               if (self == null || !rect.init(self)) {
                  blockers.add(rect);
               }
            }
         }

         return blockers;
      } else {
         for (String other : init_List) {
            if (!flush_Z(other) && init_AutismConfig_HudEl_Str(other).enabled && close_Z(other) && (!other.equals(id) || "active_modules".equals(other))) {
               for (AutismHudManager.Inner_HashUtil rect : init_List_Str_Font(other, font)) {
                  if (self == null || !rect.init(self)) {
                     blockers.add(rect);
                  }
               }
            }
         }

         return blockers;
      }
   }

   private static List<AutismHudManager.Inner_HashUtil> init_List_Str_Font(String id, Font font) {
      AutismHudManager.Inner_CachePool cached = init_al_CachePool_Str_Font(id, font);
      if ("active_modules".equals(id)) {
         List<AutismHudManager.Inner_HashUtil> rects = init_List_Str_al_CachePool(id, cached);
         if (!load_List.isEmpty() && !rects.isEmpty()) {
            List<AutismHudManager.Inner_HashUtil> visible = new ArrayList<>(rects.size());

            for (AutismHudManager.Inner_HashUtil rect : rects) {
               if (!init_Z_al_HashUtil(rect)) {
                  visible.add(rect);
               }
            }

            return visible;
         } else {
            return rects;
         }
      } else {
         AutismHudManager.Inner_BufferHelper bounds = cached.init_al_TaskQueue().init_al_BufferHelper();
         return init_Z_al_BufferHelper(bounds)
            ? List.of()
            : List.of(
               init_al_HashUtil_al_HashUtil_al_SyncManager(
                  new AutismHudManager.Inner_HashUtil(id, bounds.read(), bounds.write(), bounds.update(), bounds.sync()), cached.init_al_SyncManager()
               )
            );
      }
   }

   private static List<AutismHudManager.Inner_HashUtil> init_List_Str_al_CachePool(String id, AutismHudManager.Inner_CachePool cached) {
      List<AutismHudManager.Inner_HashUtil> content = load(id, cached);
      if (content.isEmpty()) {
         return content;
      }

      List<AutismHudManager.Inner_HashUtil> visual = new ArrayList<>(content.size());
      AutismHudManager.Inner_SyncManager style = cached.init_al_SyncManager();

      for (AutismHudManager.Inner_HashUtil rect : content) {
         visual.add(init_al_HashUtil_al_HashUtil_al_SyncManager(rect, style));
      }

      return visual;
   }

   private static List<AutismHudManager.Inner_HashUtil> load(String id, AutismHudManager.Inner_CachePool cached) {
      if (cached.read != null) {
         return cached.read;
      }

      List<AutismHudManager.Inner_HashUtil> rects = new ArrayList<>();
      AutismHudManager.Inner_TaskQueue layout = cached.init_al_TaskQueue();
      AutismHudManager.Inner_SyncManager style = cached.init_al_SyncManager();
      int rowH = style.sync();
      int gap = style.write();
      int stairSnap = style.flush();
      int pad = style.load_I();
      int groupWidth = -1;

      for (int i = 0; i < cached.init_List().size(); i++) {
         int lineW = i < cached.load().size() ? cached.load().get(i) : init_I_Font_al_NetHelper(init_Minecraft.font, cached.init_List().get(i));
         int rowW = Math.max(1, lineW + pad * 2);
         if (groupWidth >= 0 && Math.abs(groupWidth - rowW) <= stairSnap) {
            rowW = groupWidth;
         } else {
            groupWidth = rowW;
         }

         int rowX = init_I_Str(style.init_Str(), layout.read(), layout.update(), rowW);
         int rowY = layout.write() + i * (rowH + gap);
         rects.add(new AutismHudManager.Inner_HashUtil(id, rowX, rowY, rowW, rowH));
      }

      cached.read = List.copyOf(rects);
      return cached.read;
   }

   private static int init_I_Str(String alignment, int renderX, int unscaledW, int rowW) {
      if ("Center".equals(alignment)) {
         return renderX + Math.max(0, (unscaledW - rowW) / 2);
      } else {
         return "Right".equals(alignment) ? renderX + Math.max(0, unscaledW - rowW) : renderX;
      }
   }

   private static boolean close_Z(String id) {
      AutismHudManager.Inner_SyncManager style = init_al_CachePool_Str_Font(id, init_Minecraft.font).init_al_SyncManager();
      return style.init_Z() || style.load_Z();
   }

   private static boolean init_Z_al_BufferHelper(AutismHudManager.Inner_BufferHelper bounds) {
      if (bounds != null && !load_List.isEmpty()) {
         for (AutismHudManager.Inner_BufferHelper occluder : load_List) {
            if (bounds.init(occluder)) {
               return true;
            }
         }

         return false;
      } else {
         return false;
      }
   }

   private static boolean init_Z_al_HashUtil(AutismHudManager.Inner_HashUtil rect) {
      if (rect != null && !load_List.isEmpty()) {
         for (AutismHudManager.Inner_BufferHelper occluder : load_List) {
            if (rect.read() < occluder.init_I() && rect.init_I_2() > occluder.read() && rect.write() < occluder.load() && rect.load_I() > occluder.write()) {
               return true;
            }
         }

         return false;
      } else {
         return false;
      }
   }

   private static int init_I_al_BufferHelper(AutismHudManager.Inner_BufferHelper bounds) {
      if (bounds != null && !load_List.isEmpty() && init_Z_al_BufferHelper(bounds)) {
         int sh = write_I();
         int down = init_I_al_BufferHelper(bounds, 1, sh);
         int up = init_I_al_BufferHelper(bounds, -1, sh);
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

   private static int init_I_al_BufferHelper(AutismHudManager.Inner_BufferHelper bounds, int dir, int sh) {
      int shift = 0;

      for (int iter = 0; iter < 32; iter++) {
         AutismHudManager.Inner_BufferHelper moved = new AutismHudManager.Inner_BufferHelper(bounds.init_Str(), bounds.read(), bounds.write() + shift, bounds.update(), bounds.sync());
         AutismHudManager.Inner_BufferHelper hit = null;

         for (AutismHudManager.Inner_BufferHelper occ : load_List) {
            if (moved.init(occ)) {
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

   private static void init_V_GuiGraphicsExtract_2(
      GuiGraphicsExtractor context, int from, int to, List<int[]> blocked, int capOverlap, AutismHudManager.Inner_FileCache drawer
   ) {
      if (from < to) {
         if (blocked.isEmpty()) {
            drawer.draw(from, to);
         } else {
            blocked.sort(Comparator.comparingInt(a -> a[0]));
            int cursor = from;

            for (int[] block : blocked) {
               int start = init_I_I_2(block[0], from, to);
               int end = init_I_I_2(block[1], from, to);
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

   private static void init_V_GuiGraphicsExtract_3(GuiGraphicsExtractor context, int x, int y, int w, int h, int color, int width) {
      int size = Math.max(1, width);
      int screenW = read_I();
      int screenH = write_I();
      int x0 = init_I_I_2(x, 0, screenW);
      int y0 = init_I_I_2(y, 0, screenH);
      int x1 = init_I_I_2(x + w, x0, screenW);
      int y1 = init_I_I_2(y + h, y0, screenH);
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

   private static AutismHudManager.Inner_SyncManager init_al_SyncManager_Str(String id) {
      return new AutismHudManager.Inner_SyncManager(
         init_Z_Str_Str(id, "background"),
         init_Z_Str_Str(id, "outline"),
         init_Z_Str_Str(id, "shadow"),
         Math.max(1, init_I_Str_2(id, "outline-width", 1)),
         flush_I(id),
         close_I(id),
         sync_I(id),
         read_I(id),
         write_I(id),
         update_I(id),
         init_Str_Str_Str(id, "alignment"),
         load_I_Str_2(id, "background-color", 840505625),
         load_I_Str_2(id, "outline-color", "active_modules".equals(id) ? -7397596 : -9109504),
         load_I_Str_2(id, "accent-color", -50373),
         load_I_Str_2(id, "label-color", -4743522)
      );
   }

   public record Inner_BufferHelper(String init_Str, int init_I, int load, int read, int write) {
      public int init_I() {
         return this.init_I + this.read;
      }

      public int load() {
         return this.load + this.write;
      }

      public boolean init(AutismHudManager.Inner_BufferHelper other) {
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

   private static final class Inner_CachePool {
      private final AutismHudManager.Inner_ConfigStore init_al_ConfigStore;
      private final List<AutismHudManager.Inner_NetHelper> init_List;
      private final List<Integer> load;
      private final AutismHudManager.Inner_TaskQueue init_al_TaskQueue;
      private final AutismHudManager.Inner_SyncManager init_al_SyncManager;
      private long init_J;
      private List<AutismHudManager.Inner_HashUtil> read;

      private Inner_CachePool(
         AutismHudManager.Inner_ConfigStore signature, List<AutismHudManager.Inner_NetHelper> lines, List<Integer> widths, AutismHudManager.Inner_TaskQueue layout, AutismHudManager.Inner_SyncManager style
      ) {
         this.init_al_ConfigStore = signature;
         this.init_List = lines;
         this.load = widths;
         this.init_al_TaskQueue = layout;
         this.init_al_SyncManager = style;
      }

      AutismHudManager.Inner_ConfigStore init_al_ConfigStore() {
         return this.init_al_ConfigStore;
      }

      List<AutismHudManager.Inner_NetHelper> init_List() {
         return this.init_List;
      }

      List<Integer> load() {
         return this.load;
      }

      AutismHudManager.Inner_TaskQueue init_al_TaskQueue() {
         return this.init_al_TaskQueue;
      }

      AutismHudManager.Inner_SyncManager init_al_SyncManager() {
         return this.init_al_SyncManager;
      }
   }

   private record Inner_ConfigStore(
      boolean init_Z, String init_Str, int init_I, int load, int read, long init_J, int write, int update, int sync, int flush, int close, int open, int poll
   ) {
   }

   private record Inner_EventBus(
      String init_Str, float init_F, float load_F, float read_F, float write_F, int init_I, int load_I, int read_I, int write_I, double init_D
   ) {
   }

   @FunctionalInterface
   private interface Inner_FileCache {
      void draw(int var1, int var2);
   }

   private record Inner_HashUtil(String init_Str, int init_I, int load, int read, int write) {
      int init_I_2() {
         return this.init_I + this.read;
      }

      int load_I() {
         return this.load + this.write;
      }

      boolean init(AutismHudManager.Inner_HashUtil other) {
         return other != null && this.init_I == other.init_I && this.load == other.load && this.read == other.read && this.write == other.write;
      }

      boolean load(AutismHudManager.Inner_HashUtil other) {
         return other != null
            && this.init_I < other.init_I_2()
            && this.init_I_2() > other.read()
            && this.load < other.load_I()
            && this.load_I() > other.write();
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

   private record Inner_JsonUtil(String init_Str, int init_I, AutismHudManager.Inner_HashUtil init_al_HashUtil, boolean init_Z) {
   }

   private static final class Inner_NetHelper {
      private final List<AutismHudManager.Inner_StateCache> init_List = new ArrayList<>();
      private int[] init_IArr;

      void init(String text, int color) {
         if (text != null && !text.isEmpty()) {
            this.init_List.add(new AutismHudManager.Inner_StateCache(text, color));
         }
      }

      String init() {
         StringBuilder builder = new StringBuilder();

         for (AutismHudManager.Inner_StateCache segment : this.init_List) {
            builder.append(segment.init_Str);
         }

         return builder.toString();
      }
   }

   private record Inner_StateCache(String init_Str, int init_I) {
   }

   private record Inner_SyncManager(
      boolean init_Z,
      boolean load_Z,
      boolean read_Z,
      int init_I,
      int load_I,
      int read_I,
      int write,
      int update,
      int sync,
      int flush,
      String init_Str,
      int close,
      int open,
      int poll,
      int push
   ) {
   }

   public record Inner_TaskQueue(String init_Str, int init_I, int load, int read, int write, double init_D) {
      public int init_I() {
         return (int)Math.ceil(this.read * this.init_D);
      }

      public int load() {
         return (int)Math.ceil(this.write * this.init_D);
      }

      public AutismHudManager.Inner_BufferHelper init_al_BufferHelper() {
         return new AutismHudManager.Inner_BufferHelper(this.init_Str, this.init_I, this.load, this.init_I(), this.load());
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

   private record Inner_UiHelper(boolean init_Z, String init_Str) {
      Inner_UiHelper(Module module, String id) {
         this(module != null && module.setting(id) != null, module != null && module.setting(id) != null ? module.value(id) : "");
      }
   }
}

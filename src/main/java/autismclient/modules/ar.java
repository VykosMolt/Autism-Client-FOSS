package autismclient.modules;

import autismclient.AutismClientAddon;
import autismclient.util.AuditWaypointScope;
import autismclient.util.AutismLiteVariant;
import autismclient.util.AutismWaypoints;
import autismclient.util.PkgCls_c;
import autismclient.util.bK;
import autismclient.util.cI;
import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.PoseStack.Pose;
import com.mojang.renderpearl.api.GpuFormat;
import com.mojang.renderpearl.api.textures.FilterMode;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BiConsumer;
import net.fabricmc.fabric.api.client.rendering.v1.level.LevelRenderEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font.DisplayMode;
import net.minecraft.client.gui.Font.GlyphVisitor;
import net.minecraft.client.gui.font.TextRenderable;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.CardinalLighting;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;
import org.joml.Matrix4fc;
import org.joml.Quaternionf;
import org.joml.Vector3f;

public final class ar {
   private static final PkgCls_c.Inner_DataUtil init_c_DataUtil = new PkgCls_c.Inner_DataUtil(4194304);
   private static final PkgCls_c.Inner_DataUtil load_c_DataUtil = new PkgCls_c.Inner_DataUtil(4194304);
   private static final PkgCls_c.Inner_DataUtil read_c_DataUtil = new PkgCls_c.Inner_DataUtil(786432);
   private static final autismclient.util.P init_P = new autismclient.util.P("autism_esp_fill");
   private static final autismclient.util.P load_P = new autismclient.util.P("autism_esp_wire");
   private static final Pose init_PoseStack_Pose = new PoseStack().last();
   private static boolean init_Z = true;
   private static boolean load_Z;
   private static boolean read_Z;
   private static Vec3 init_Vec3 = Vec3.ZERO;
   private static int load_I = -2147483648;
   private static float init_F = 0.0F / 0.0F;
   private static final long[] init_JArr = new long[7];
   private static final double init_D = 64.0;
   private static boolean write_Z;
   private static ar.Inner_CachePool init_ar_CachePool = ar.Inner_CachePool.init_ar_CachePool();
   private static ar.Inner_CachePool load_ar_CachePool = ar.Inner_CachePool.init_ar_CachePool();
   private static ar.Inner_CachePool read_ar_CachePool = ar.Inner_CachePool.init_ar_CachePool();
   private static ar.Inner_CachePool write_ar_CachePool = ar.Inner_CachePool.init_ar_CachePool();
   private static ar.Inner_CachePool update_ar_CachePool = ar.Inner_CachePool.init_ar_CachePool();
   private static ar.Inner_CachePool sync_ar_CachePool = ar.Inner_CachePool.init_ar_CachePool();
   private static ar.Inner_JsonUtil init_ar_JsonUtil = ar.Inner_JsonUtil.init_ar_JsonUtil();
   private static ClientLevel init_ClientLevel;
   private static long init_J = -9223372036854775808L;
   private static int read_I = -2147483648;
   private static List<ar.Inner_DataUtil> init_List = List.of();
   private static List<ar.Inner_ConfigStore> load_List = List.of();
   private static final ArrayList<ar.Inner_ConfigStore> init_ArrayList = new ArrayList<>();
   private static Vec3 load_Vec3 = Vec3.ZERO;
   private static Vec3 traceForward = new Vec3(0.0, 0.0, -1.0);
   private static Vec3 traceUp = new Vec3(0.0, 1.0, 0.0);
   private static ar.Inner_JsonUtil load_ar_JsonUtil = ar.Inner_JsonUtil.init_ar_JsonUtil();
   private static Vec3 read_Vec3 = Vec3.ZERO;
   private static final Quaternionf init_Quaternionf = new Quaternionf();
   private static ar.Inner_CachePool flush_ar_CachePool = ar.Inner_CachePool.init_ar_CachePool();
   private static ar.Inner_CachePool close_ar_CachePool = ar.Inner_CachePool.init_ar_CachePool();
   private static ar.Inner_CachePool open_ar_CachePool = ar.Inner_CachePool.init_ar_CachePool();
   private static ar.Inner_CachePool poll_ar_CachePool = ar.Inner_CachePool.init_ar_CachePool();
   private static ar.Inner_CachePool push_ar_CachePool = ar.Inner_CachePool.init_ar_CachePool();
   private static ar.Inner_CachePool pop_ar_CachePool = ar.Inner_CachePool.init_ar_CachePool();
   private static ar.Inner_CachePool tick_ar_CachePool = ar.Inner_CachePool.init_ar_CachePool();
   private static List<TrajectoriesModule.Inner_CachePool> read_List = List.of();
   private static AABB init_AABB;
   private static int write_I;
   private static List<AABB> write_List = List.of();
   private static int update_I;
   private static boolean update_Z;
   private static boolean sync_Z;
   private static AABB load_AABB;
   private static int sync_I;
   private static boolean flush_Z;
   private static boolean close_Z;
   private static List<FileCache> update_List = List.of();
   private static boolean open_Z;
   private static boolean poll_Z;
   private static boolean push_Z;
   private static boolean pop_Z;
   private static boolean tick_Z;
   private static boolean run_Z;
   private static boolean get_Z;
   private static boolean set_Z;
   private static boolean put_Z;
   private static boolean add_Z;
   private static boolean clear;
   private static boolean reset;
   private static boolean start;
   private static Vec3 write_Vec3 = Vec3.ZERO;
   static final int init_I = 2048;
   private static final int flush_I = 2176;
   private static final double load_D = 0.015625;
   private static final long[] load_JArr = new long[2176];
   private static final BlockState[] init_BlockStateArr = new BlockState[2176];
   private static final double[] init_DArr = new double[2176];
   private static final long[] read_JArr = new long[2048];
   private static final int[] init_IArr = new int[2048];
   private static final LongOpenHashSet init_LongOpenHashSet = new LongOpenHashSet();
   private static int close_I;
   private static long load_J = -9223372036854775808L;
   private static int open_I = -1;
   private static BlockPos init_BlockPos;
   private static long read_J = -9223372036854775808L;
   private static int poll_I;
   private static int push_I = -1;
   private static int pop_I = -1;
   private static Vec3 update_Vec3;
   private static int tick_I;
   private static Module init_Module;
   private static Module load_Module;
   private static Module read_Module;
   private static Module write_Module;
   private static Module update_Module;
   private static Module sync_Module;
   private static AirPlaceModule init_StreamUtil;
   private static GhostBlockModule init_J_2;
   private static TpClickModule init_aR;
   private static Module flush_Module;
   private static int run_I = -1;
   private static boolean stop;
   private static boolean next;
   private static boolean prev;
   private static boolean valid;
   private static boolean size;
   private static boolean data;
   private static boolean value;
   private static boolean cache;
   private static long write_J = -9223372036854775808L;
   private static float load_F = 0.3F;
   private static ar.Inner_CachePool run_ar_CachePool = ar.Inner_CachePool.init_ar_CachePool();
   private static BlockPos load_BlockPos;
   private static BlockPos read_BlockPos;
   private static long update_J = -9223372036854775808L;
   private static int get_I = -1;
   private static final double read_D = 256.0;
   private static final float read_F = 0.35F;
   private static final int set_I = 2;
   private static final double write_D = 14.0;
   private static final double update_D = 6.0;
   private static final double sync_D = 2.5;
   private static final double flush_D = 12.0;
   private static final double close_D = 12.0;
   private static final float write_F = 0.6F;
   private static final double open_D = 0.046;
   private static final double poll_D = 0.06;
   private static final double push_D = 0.115;
   private static final float update_F = 0.03125F;
   private static final float sync_F = -4.5F;
   private static final int put_I = -15066598;
   private static final float flush_F = 0.375F;
   private static final float[][] init_FArr2 = new float[][]{
      {-1.0F, -1.0F}, {0.0F, -1.0F}, {1.0F, -1.0F}, {-1.0F, 0.0F}, {1.0F, 0.0F}, {-1.0F, 1.0F}, {0.0F, 1.0F}, {1.0F, 1.0F}
   };
   private static final Identifier init_Identifier = Identifier.fromNamespaceAndPath("autismclient", "dynamic/waypoint_disc");
   private static final int add_I = 32;
   private static final double pop_D = 12.0;
   private static final double tick_D = 14.0;
   private static final Vector3f init_Vector3f = new Vector3f();
   private static boolean state;
   private static boolean queue;

   private static Module init_Module() {
      Module m = sync_Module;
      return m != null ? m : (sync_Module = al.init_Module_Str("xray"));
   }

   private static Module load_Module() {
      Module m = init_Module;
      return m != null ? m : (init_Module = al.init_Module_Str("tracers"));
   }

   private static Module read_Module() {
      Module m = read_Module;
      return m != null ? m : (read_Module = al.init_Module_Str("trajectories"));
   }

   private static Module write_Module() {
      Module m = load_Module;
      return m != null ? m : (load_Module = al.init_Module_Str("storage-esp"));
   }

   private static Module update() {
      Module m = update_Module;
      return m != null ? m : (update_Module = al.init_Module_Str("spawner-esp"));
   }

   private static Module sync() {
      Module m = write_Module;
      return m != null ? m : (write_Module = al.init_Module_Str("block-esp"));
   }

   private static AirPlaceModule init_StreamUtil() {
      AirPlaceModule cached = init_StreamUtil;
      if (cached != null) {
         return cached;
      } else {
         return al.init_Module_Str("air-place") instanceof AirPlaceModule airPlace ? (init_StreamUtil = airPlace) : null;
      }
   }

   private static GhostBlockModule init_J() {
      GhostBlockModule cached = init_J_2;
      if (cached != null) {
         return cached;
      } else {
         return al.init_Module_Str("ghostblock") instanceof GhostBlockModule ghostBlock ? (init_J_2 = ghostBlock) : null;
      }
   }

   private static TpClickModule init_aR() {
      TpClickModule cached = init_aR;
      if (cached != null) {
         return cached;
      } else {
         return al.init_Module_Str("tp-click") instanceof TpClickModule tpClick ? (init_aR = tpClick) : null;
      }
   }

   private static Module flush() {
      Module m = flush_Module;
      return m != null ? m : (flush_Module = al.init_Module_Str("waypoints"));
   }

   private static void init_V_Module(Module storage, Module blockEsp, Module spawnerEsp, Module xray) {
      int rev = al.init_I();
      if (rev != run_I) {
         run_I = rev;
         cache = xray != null && Boolean.parseBoolean(xray.value("fill"));
         stop = storage != null && Boolean.parseBoolean(storage.value("fill"));
         next = storage != null && Boolean.parseBoolean(storage.value("tracers"));
         prev = blockEsp != null && Boolean.parseBoolean(blockEsp.value("fill"));
         valid = blockEsp != null && Boolean.parseBoolean(blockEsp.value("tracers"));
         size = blockEsp != null && Boolean.parseBoolean(blockEsp.value("barriers"));
         data = spawnerEsp != null && Boolean.parseBoolean(spawnerEsp.value("fill"));
         value = spawnerEsp != null && Boolean.parseBoolean(spawnerEsp.value("tracers"));
      }
   }

   private ar() {
   }

   static void init_V() {
      if (!write_Z) {
         write_Z = true;
         LevelRenderEvents.COLLECT_SUBMITS
            .register(
               context -> {
                  load_List = List.of();
                  load_ar_JsonUtil = ar.Inner_JsonUtil.init_ar_JsonUtil();
                  get_Z = false;
                  pop_Z = false;
                  push_Z = false;
                  poll_Z = false;
                  open_Z = false;
                  start = false;
                  add_Z = false;
                  put_Z = false;
                  set_Z = false;
                  clear = false;
                  tick_Z = false;
                  reset = false;
                  run_Z = false;
                  close_Z = false;
                  flush_Z = false;
                  read_List = List.of();
                  init_AABB = null;
                  load_AABB = null;
                  write_List = List.of();
                  update_List = List.of();
                  tick_I = 0;
                  Minecraft mc = Minecraft.getInstance();
                  if (!aD.init_Z()
                     && (AutismLiteVariant.enabled() || autismclient.e.UiHelper.init())
                     && mc != null
                     && mc.level != null
                     && mc.player != null
                     && !mc.gui.hud.isHidden()) {
                     boolean drawTracers = am.tick();
                     Module storage = write_Module();
                     Module blockEsp = sync();
                     Module spawnerEsp = update();
                     AirPlaceModule airPlace = init_StreamUtil();
                     Module waypoints = flush();
                     Module xray = init_Module();
                     boolean storageEnabled = storage != null && storage.isEnabled();
                     boolean blockEnabled = blockEsp != null && blockEsp.isEnabled();
                     boolean spawnerEnabled = spawnerEsp != null && spawnerEsp.isEnabled();
                     ah.Inner_CachePool frameEsp = ah.init_ah_CachePool();
                     boolean frameEnabled = ah.init(frameEsp);
                     boolean xrayBoxes = ak.read_Z(xray);
                     boolean xrayGhosts = ak.write_Z(xray);
                     boolean waypointsEnabled = waypoints != null && waypoints.isEnabled();
                     Module trajectories = read_Module();
                     boolean trajectoriesEnabled = trajectories != null && trajectories.isEnabled();
                     BlockPos airTarget = airPlace != null && airPlace.isEnabled() ? airPlace.init_BlockPos_3() : null;
                     boolean airGuide = airTarget != null;
                     GhostBlockModule ghostBlock = init_J();
                     List<AABB> ghostBoxes = List.of();
                     if (ghostBlock != null) {
                        List<AABB> boxes = ghostBlock.init_List_4();
                        if (!boxes.isEmpty() && ghostBlock.load_Z_5()) {
                           ghostBoxes = boxes;
                        }
                     }

                     TpClickModule tpClick = init_aR();
                     AABB tpClickBox = tpClick != null && tpClick.isEnabled() ? tpClick.init_AABB_2() : null;
                     int tpClickColor = tpClickBox != null ? tpClick.init_I_19() : 0;
                     if (!AutismLiteVariant.enabled()) {
                        autismclient.ai.a.j.Inner_StreamUtil destination = autismclient.ai.aP.init_j_StreamUtil();
                        if (destination != null) {
                           tpClickBox = destination.box();
                           tpClickColor = destination.color();
                        }
                     }

                     ar.Inner_JsonUtil waypointFrame = waypointsEnabled ? init(waypoints, mc, mc.level) : ar.Inner_JsonUtil.init_ar_JsonUtil();
                     List<ar.Inner_NetHelper> waypointBoxes = waypointFrame.init_List();
                     List<FileCache> auraPreviewBoxes = List.of();
                     if (!AutismLiteVariant.enabled()) {
                        List<FileCache> a1 = CrystalAuraModule.init_List_18();
                        List<FileCache> a2 = AnchorAuraModule.init_List();
                        List<FileCache> a3 = BacktrackModule.init_List_9();
                        if (!a1.isEmpty() || !a2.isEmpty() || !a3.isEmpty()) {
                           auraPreviewBoxes = new ArrayList<>(a1.size() + a2.size() + a3.size());
                           auraPreviewBoxes.addAll(a1);
                           auraPreviewBoxes.addAll(a2);
                           auraPreviewBoxes.addAll(a3);
                        }
                     }

                     if ((
                           drawTracers
                              || storageEnabled
                              || blockEnabled
                              || spawnerEnabled
                              || frameEnabled
                              || xrayBoxes
                              || xrayGhosts
                              || airGuide
                              || !ghostBoxes.isEmpty()
                              || tpClickBox != null
                              || !waypointBoxes.isEmpty()
                              || !auraPreviewBoxes.isEmpty()
                              || trajectoriesEnabled
                        )
                        && !am.clear()) {
                        Module tracer = load_Module();
                        init_V_Module(storage, blockEsp, spawnerEsp, xray);
                        boolean storageFill = storageEnabled && stop;
                        boolean storageWire = storageEnabled;
                        boolean storageTrace = storageEnabled && next;
                        boolean blockFill = blockEnabled && prev;
                        boolean blockWire = blockEnabled;
                        boolean blockTrace = blockEnabled && valid;
                        boolean barrierFill = blockEnabled && size;
                        boolean spawnerFill = spawnerEnabled && data;
                        boolean spawnerWire = spawnerEnabled;
                        boolean spawnerTrace = spawnerEnabled && value;
                        boolean frameFill = frameEnabled && frameEsp.update();
                        boolean frameWire = frameEnabled && frameEsp.write();
                        boolean frameTrace = frameEnabled && frameEsp.sync();
                        boolean xrayFill = xrayBoxes && cache;
                        boolean xrayWire = xrayBoxes;
                        boolean airFill = airGuide && airPlace.init_Z_18();
                        if (drawTracers
                           || storageFill
                           || storageWire
                           || storageTrace
                           || blockFill
                           || blockWire
                           || blockTrace
                           || barrierFill
                           || spawnerFill
                           || spawnerWire
                           || spawnerTrace
                           || frameFill
                           || frameWire
                           || frameTrace
                           || xrayWire
                           || xrayGhosts
                           || airGuide
                           || !ghostBoxes.isEmpty()
                           || tpClickBox != null
                           || !waypointBoxes.isEmpty()
                           || !auraPreviewBoxes.isEmpty()
                           || trajectoriesEnabled) {
                           CameraRenderState cameraState = context.levelState().cameraRenderState;
                           Vec3 camera = cameraState.pos;
                           float tickDelta = mc.getDeltaTracker().getGameTimeDeltaPartialTick(false);
                           Vec3 camFwd = init_Vec3_CameraRenderState(cameraState);
                           Vector3f upVec = new Vector3f(0.0F, 1.0F, 0.0F);
                           if (cameraState != null && cameraState.orientation != null) {
                              cameraState.orientation.transform(upVec);
                           }

                           Vec3 camUp = upVec.lengthSquared() > 1.0E-8F ? new Vec3(upVec.x, upVec.y, upVec.z).normalize() : new Vec3(0.0, 1.0, 0.0);
                           Vec3 traceStart = camera.add(camFwd.scale(2.0));
                           float tracerWidth = init_F_Str(tracer == null ? null : tracer.value("line-width"), 2.0F, 2.0F, 6.0F);
                           ar.Inner_CachePool storageFrame = storageEnabled
                              ? init_ar_CachePool_Module(storage, mc.level, mc.player, tickDelta)
                              : ar.Inner_CachePool.init_ar_CachePool();
                           ar.Inner_CachePool blockFrame = blockEnabled ? init(blockEsp, mc.level, mc.player) : ar.Inner_CachePool.init_ar_CachePool();
                           ar.Inner_CachePool barrierFrame = barrierFill ? update(blockEsp, mc.level, mc.player) : ar.Inner_CachePool.init_ar_CachePool();
                           ar.Inner_CachePool spawnerFrame = spawnerEnabled ? load(spawnerEsp, mc.level, mc.player) : ar.Inner_CachePool.init_ar_CachePool();
                           ar.Inner_CachePool frameFrame = frameEnabled ? init(frameEsp, mc.level, mc.player) : ar.Inner_CachePool.init_ar_CachePool();
                           ar.Inner_CachePool xrayFrame = xrayBoxes ? read(xray, mc.level, mc.player) : ar.Inner_CachePool.init_ar_CachePool();
                           ar.Inner_CachePool xrayNearFrame = xrayBoxes ? write(xray, mc.level, mc.player) : ar.Inner_CachePool.init_ar_CachePool();
                           tick_I = xrayGhosts ? init_I_Module(xray, mc.level, mc.player, camera) : 0;
                           AABB airBox = airGuide ? new AABB(airTarget).inflate(0.002) : null;
                           int airColor = airGuide ? airPlace.init_I_11() : 0;
                           init_ArrayList.clear();
                           List<ar.Inner_ConfigStore> tracerLines = init_ArrayList;
                           if (drawTracers) {
                              boolean heightLine = Boolean.parseBoolean(tracer == null ? null : tracer.value("height-line"));
                              init(mc.level, traceStart, tickDelta, tracerWidth, heightLine, tracerLines);
                           }

                           if (storageTrace && storageFrame != null) {
                              float traceW = 2.0F;
                              storageFrame.init((target, color) -> tracerLines.add(new ar.Inner_ConfigStore(traceStart, target, color, 2.0F)));
                           }

                           if (blockTrace && blockFrame != null) {
                              float traceW = 2.0F;
                              blockFrame.init((target, color) -> tracerLines.add(new ar.Inner_ConfigStore(traceStart, target, color, 2.0F)));
                           }

                           if (blockTrace && barrierFill && barrierFrame != null) {
                              barrierFrame.init((target, color) -> tracerLines.add(new ar.Inner_ConfigStore(traceStart, target, color, 2.0F)));
                           }

                           if (spawnerTrace && spawnerFrame != null) {
                              float traceW = 2.0F;
                              spawnerFrame.init((target, color) -> tracerLines.add(new ar.Inner_ConfigStore(traceStart, target, color, 2.0F)));
                           }

                           if (frameTrace && frameFrame != null) {
                              float traceW = 2.0F;
                              frameFrame.init((target, color) -> tracerLines.add(new ar.Inner_ConfigStore(traceStart, target, color, 2.0F)));
                           }

                           if (!tracerLines.isEmpty()) {
                              load_List = tracerLines;
                              load_Vec3 = camera;
                              traceForward = camFwd;
                              traceUp = camUp;
                           }

                           boolean waypointDraw = !waypointBoxes.isEmpty();
                           open_Z = storageFill;
                           poll_Z = blockFill;
                           push_Z = barrierFill;
                           pop_Z = spawnerFill;
                           tick_Z = frameFill;
                           run_Z = xrayFill;
                           get_Z = airFill;
                           set_Z = storageWire;
                           put_Z = blockWire;
                           add_Z = spawnerWire;
                           clear = frameWire;
                           reset = xrayWire;
                           start = airGuide;
                           flush_ar_CachePool = storageFrame;
                           close_ar_CachePool = blockFrame;
                           open_ar_CachePool = barrierFrame;
                           poll_ar_CachePool = spawnerFrame;
                           push_ar_CachePool = frameFrame;
                           pop_ar_CachePool = xrayFrame;
                           tick_ar_CachePool = xrayNearFrame;
                           load_F = ak.init_F(xray);
                           init_AABB = airBox;
                           write_I = airColor;
                           flush_Z = close_Z = tpClickBox != null;
                           load_AABB = tpClickBox;
                           sync_I = tpClickColor;
                           sync_Z = !ghostBoxes.isEmpty();
                           update_Z = sync_Z && ghostBlock.read_Z_4();
                           write_List = ghostBoxes;
                           update_I = sync_Z ? ghostBlock.read_I_3() : 0;
                           update_List = auraPreviewBoxes;
                           write_Vec3 = camera;

                           try {
                              TrajectoriesModule.init_V_F_2(tickDelta);
                           } catch (Throwable error) {
                              init_V_List(List.of());
                              AutismClientAddon.LOG.debug("Trajectory collect failed", error);
                           }

                           if (waypointDraw) {
                              load_ar_JsonUtil = waypointFrame;
                              read_Vec3 = camera;
                              init_Quaternionf.set(cameraState.orientation);
                           }
                        }
                     }
                  }
               }
            );
      }
   }

   public static boolean init_Z() {
      return !load_List.isEmpty();
   }

   public static void init_V_PoseStack(PoseStack matrices) {
      List<ar.Inner_ConfigStore> lines = load_List;
      if (!lines.isEmpty()) {
         load_List = List.of();
         Vec3 cam = load_Vec3;
         PkgCls_c bufferSource = init_c_DataUtil.init();
         VertexConsumer buffer = bufferSource.init(autismclient.render.AutismRenderTypes.tracerEspLines());
         Pose pose = matrices.last();

         for (ar.Inner_ConfigStore seg : lines) {
            init(
               pose,
               buffer,
               seg.init_Vec3().x - cam.x,
               seg.init_Vec3().y - cam.y,
               seg.init_Vec3().z - cam.z,
               seg.load().x - cam.x,
               seg.load().y - cam.y,
               seg.load().z - cam.z,
               seg.init_I(),
               seg.init_F()
            );
         }

         bufferSource.init();
      }
   }

   public static void init_V_List(List<TrajectoriesModule.Inner_CachePool> paths) {
      read_List = paths == null ? List.of() : paths;
   }

   public static boolean load_Z() {
      if (!update_List.isEmpty()) {
         return true;
      } else {
         return !AutismLiteVariant.enabled() && aW.init_aV_DataUtil() != null
            ? true
            : open_Z
               || poll_Z
               || push_Z
               || pop_Z
               || get_Z
               || set_Z
               || put_Z
               || add_Z
               || start
               || tick_Z
               || clear
               || run_Z
               || reset
               || update_Z
               || sync_Z
               || tick_I > 0
               || flush_Z
               || close_Z
               || !read_List.isEmpty();
      }
   }

   public static void load(PoseStack matrices) {
      if (load_Z()) {
         long flushPerf = bK.load_J();

         try {
            write(matrices);
         } finally {
            bK.init("frame.espFlush", flushPerf);
         }
      }
   }

   private static void write(PoseStack matrices) {
      boolean storageFill = open_Z;
      boolean blockFill = poll_Z;
      boolean barrierFill = push_Z;
      boolean spawnerFill = pop_Z;
      boolean frameFill = tick_Z;
      boolean xrayFill = run_Z;
      boolean airFill = get_Z;
      boolean storageWire = set_Z;
      boolean blockWire = put_Z;
      boolean spawnerWire = add_Z;
      boolean frameWire = clear;
      boolean xrayWire = reset;
      boolean airWire = start;
      boolean tpClickFill = flush_Z;
      boolean tpClickWire = close_Z;
      boolean ghostFill = update_Z;
      boolean ghostWire = sync_Z;
      List<TrajectoriesModule.Inner_CachePool> trajectories = read_List;
      read_List = List.of();
      get_Z = false;
      pop_Z = false;
      push_Z = false;
      poll_Z = false;
      open_Z = false;
      start = false;
      add_Z = false;
      put_Z = false;
      set_Z = false;
      clear = false;
      tick_Z = false;
      reset = false;
      run_Z = false;
      close_Z = false;
      flush_Z = false;
      sync_Z = false;
      update_Z = false;
      ar.Inner_CachePool storageFrame = flush_ar_CachePool;
      ar.Inner_CachePool blockFrame = close_ar_CachePool;
      ar.Inner_CachePool barrierFrame = open_ar_CachePool;
      ar.Inner_CachePool spawnerFrame = poll_ar_CachePool;
      ar.Inner_CachePool frameFrame = push_ar_CachePool;
      ar.Inner_CachePool xrayFrame = pop_ar_CachePool;
      ar.Inner_CachePool xrayNearFrame = tick_ar_CachePool;
      float xrayFillAlpha = load_F;
      AABB airBox = init_AABB;
      int airColor = write_I;
      AABB tpClickBox = load_AABB;
      int tpClickColor = sync_I;
      List<AABB> ghostBoxes = write_List;
      int ghostColor = update_I;
      write_List = List.of();
      List<FileCache> auraPreviewBoxes = update_List;
      update_List = List.of();
      Vec3 cam = write_Vec3;
      int oreGhosts = tick_I;
      tick_I = 0;
      PkgCls_c bufferSource = load_c_DataUtil.init();
      Pose pose = matrices.last();
      boolean meshed = init(
         pose,
         cam,
         xrayFrame,
         xrayNearFrame,
         storageFrame,
         blockFrame,
         barrierFrame,
         spawnerFrame,
         frameFrame,
         xrayFill,
         xrayWire,
         storageFill,
         storageWire,
         blockFill,
         blockWire,
         barrierFill,
         spawnerFill,
         spawnerWire,
         frameFill,
         frameWire,
         xrayFillAlpha
      );
      if (oreGhosts > 0) {
         init_V_PoseStack_Pose(pose, bufferSource, oreGhosts, cam);
      }

      if (storageFill || blockFill || barrierFill || spawnerFill || frameFill || xrayFill || airFill || tpClickFill || ghostFill || !auraPreviewBoxes.isEmpty()
         )
       {
         VertexConsumer buffer = bufferSource.init(autismclient.render.AutismRenderTypes.storageEspFillSeeThrough());
         if (!meshed) {
            if (xrayFill && xrayFrame != null) {
               xrayFrame.init(pose, buffer, cam, xrayFillAlpha);
            }

            if (xrayFill && xrayNearFrame != null) {
               xrayNearFrame.init(pose, buffer, cam, 0.6F);
            }

            if (storageFill && storageFrame != null) {
               storageFrame.init(pose, buffer, cam, 0.3F);
            }

            if (blockFill && blockFrame != null) {
               blockFrame.init(pose, buffer, cam, 0.3F);
            }

            if (barrierFill && barrierFrame != null) {
               barrierFrame.init(pose, buffer, cam, 0.2F);
            }

            if (spawnerFill && spawnerFrame != null) {
               spawnerFrame.init(pose, buffer, cam, 0.3F);
            }

            if (frameFill && frameFrame != null) {
               frameFrame.init(pose, buffer, cam, 0.35F);
            }
         }

         if (airFill && airBox != null) {
            load(pose, buffer, airBox.move(-cam.x, -cam.y, -cam.z), init_I_I_F(airColor, 0.12F));
         }

         if (tpClickFill && tpClickBox != null) {
            load(pose, buffer, tpClickBox.move(-cam.x, -cam.y, -cam.z), init_I_I_F(tpClickColor, 0.12F));
         }

         if (ghostFill) {
            int color = init_I_I_F(ghostColor, 0.15F);

            for (AABB box : ghostBoxes) {
               load(pose, buffer, box.move(-cam.x, -cam.y, -cam.z), color);
            }
         }

         for (FileCache preview : auraPreviewBoxes) {
            if (preview.init_AABB() != null) {
               load(pose, buffer, preview.init_AABB().move(-cam.x, -cam.y, -cam.z), preview.init_I());
            }
         }
      }

      if (!AutismLiteVariant.enabled() && (!trajectories.isEmpty() || aW.init_aV_DataUtil() != null)) {
         aY.init(matrices, bufferSource, cam, trajectories);
      }

      if (storageWire || blockWire || spawnerWire || frameWire || xrayWire || airWire || tpClickWire || ghostWire || !auraPreviewBoxes.isEmpty()) {
         VertexConsumer buffer = bufferSource.init(autismclient.render.AutismRenderTypes.tracerEspLines());
         if (!meshed) {
            if (xrayWire && xrayFrame != null) {
               xrayFrame.load(pose, buffer, cam, 1.5F);
            }

            if (xrayWire && xrayNearFrame != null) {
               xrayNearFrame.load(pose, buffer, cam, 1.5F);
            }

            if (storageWire && storageFrame != null) {
               storageFrame.load(pose, buffer, cam, 1.5F);
            }

            if (blockWire && blockFrame != null) {
               blockFrame.load(pose, buffer, cam, 1.5F);
            }

            if (spawnerWire && spawnerFrame != null) {
               spawnerFrame.load(pose, buffer, cam, 1.5F);
            }

            if (frameWire && frameFrame != null) {
               frameFrame.load(pose, buffer, cam, 1.5F);
            }
         }

         if (airWire && airBox != null) {
            init(pose, buffer, airBox.move(-cam.x, -cam.y, -cam.z), init_I_I_F(airColor, 0.8F), 1.5F);
         }

         if (tpClickWire && tpClickBox != null) {
            init(pose, buffer, tpClickBox.move(-cam.x, -cam.y, -cam.z), init_I_I_F(tpClickColor, 0.8F), 1.5F);
         }

         if (ghostWire) {
            int color = init_I_I_F(ghostColor, 0.85F);

            for (AABB box : ghostBoxes) {
               init(pose, buffer, box.move(-cam.x, -cam.y, -cam.z), color, 1.5F);
            }
         }

         for (FileCache preview : auraPreviewBoxes) {
            if (preview.init_AABB() != null) {
               init(pose, buffer, preview.init_AABB().move(-cam.x, -cam.y, -cam.z), preview.load_I(), preview.init_F());
            } else if (preview.init_Vec3() != null && preview.load_Vec3() != null) {
               Vec3 from = preview.init_Vec3().subtract(cam);
               Vec3 to = preview.load_Vec3().subtract(cam);
               load(pose, buffer, from.x, from.y, from.z, to.x, to.y, to.z, preview.load_I(), preview.init_F());
            }
         }
      }

      bufferSource.init();
   }

   private static boolean init(
      Pose pose,
      Vec3 camera,
      ar.Inner_CachePool xrayFrame,
      ar.Inner_CachePool xrayNearFrame,
      ar.Inner_CachePool storageFrame,
      ar.Inner_CachePool blockFrame,
      ar.Inner_CachePool barrierFrame,
      ar.Inner_CachePool spawnerFrame,
      ar.Inner_CachePool frameFrame,
      boolean xrayFill,
      boolean xrayWire,
      boolean storageFill,
      boolean storageWire,
      boolean blockFill,
      boolean blockWire,
      boolean barrierFill,
      boolean spawnerFill,
      boolean spawnerWire,
      boolean frameFill,
      boolean frameWire,
      float xrayFillAlpha
   ) {
      if (!init_Z) {
         return false;
      }

      try {
         Vec3 anchor = init_Vec3;
         if (Math.abs(camera.x - anchor.x) > 64.0 || Math.abs(camera.y - anchor.y) > 64.0 || Math.abs(camera.z - anchor.z) > 64.0) {
            anchor = new Vec3(Math.floor(camera.x / 16.0) * 16.0, Math.floor(camera.y / 16.0) * 16.0, Math.floor(camera.z / 16.0) * 16.0);
         }

         int flags = (xrayFill ? 1 : 0)
            | (xrayWire ? 2 : 0)
            | (storageFill ? 4 : 0)
            | (storageWire ? 8 : 0)
            | (blockFill ? 16 : 0)
            | (blockWire ? 32 : 0)
            | (barrierFill ? 64 : 0)
            | (spawnerFill ? 128 : 0)
            | (spawnerWire ? 256 : 0)
            | (frameFill ? 512 : 0)
            | (frameWire ? 1024 : 0);
         boolean stale = flags != load_I
            || !anchor.equals(init_Vec3)
            || Float.floatToIntBits(xrayFillAlpha) != Float.floatToIntBits(init_F)
            || init_JArr[0] != init_J_ar_CachePool(xrayFrame)
            || init_JArr[1] != init_J_ar_CachePool(xrayNearFrame)
            || init_JArr[2] != init_J_ar_CachePool(storageFrame)
            || init_JArr[3] != init_J_ar_CachePool(blockFrame)
            || init_JArr[4] != init_J_ar_CachePool(barrierFrame)
            || init_JArr[5] != init_J_ar_CachePool(spawnerFrame)
            || init_JArr[6] != init_J_ar_CachePool(frameFrame);
         if (stale) {
            long perf = bK.load_J();
            Vec3 origin = anchor;
            load_Z = init_P.init(autismclient.render.AutismRenderTypes.storageEspFillSeeThrough(), buffer -> {
               if (xrayFill && xrayFrame != null) {
                  xrayFrame.init(init_PoseStack_Pose, buffer, origin, xrayFillAlpha);
               }

               if (xrayFill && xrayNearFrame != null) {
                  xrayNearFrame.init(init_PoseStack_Pose, buffer, origin, 0.6F);
               }

               if (storageFill && storageFrame != null) {
                  storageFrame.init(init_PoseStack_Pose, buffer, origin, 0.3F);
               }

               if (blockFill && blockFrame != null) {
                  blockFrame.init(init_PoseStack_Pose, buffer, origin, 0.3F);
               }

               if (barrierFill && barrierFrame != null) {
                  barrierFrame.init(init_PoseStack_Pose, buffer, origin, 0.2F);
               }

               if (spawnerFill && spawnerFrame != null) {
                  spawnerFrame.init(init_PoseStack_Pose, buffer, origin, 0.3F);
               }

               if (frameFill && frameFrame != null) {
                  frameFrame.init(init_PoseStack_Pose, buffer, origin, 0.35F);
               }
            });
            read_Z = load_P.init(autismclient.render.AutismRenderTypes.tracerEspLines(), buffer -> {
               if (xrayWire && xrayFrame != null) {
                  xrayFrame.load(init_PoseStack_Pose, buffer, origin, 1.5F);
               }

               if (xrayWire && xrayNearFrame != null) {
                  xrayNearFrame.load(init_PoseStack_Pose, buffer, origin, 1.5F);
               }

               if (storageWire && storageFrame != null) {
                  storageFrame.load(init_PoseStack_Pose, buffer, origin, 1.5F);
               }

               if (blockWire && blockFrame != null) {
                  blockFrame.load(init_PoseStack_Pose, buffer, origin, 1.5F);
               }

               if (spawnerWire && spawnerFrame != null) {
                  spawnerFrame.load(init_PoseStack_Pose, buffer, origin, 1.5F);
               }

               if (frameWire && frameFrame != null) {
                  frameFrame.load(init_PoseStack_Pose, buffer, origin, 1.5F);
               }
            });
            init_Vec3 = anchor;
            load_I = flags;
            init_F = xrayFillAlpha;
            init_JArr[0] = init_J_ar_CachePool(xrayFrame);
            init_JArr[1] = init_J_ar_CachePool(xrayNearFrame);
            init_JArr[2] = init_J_ar_CachePool(storageFrame);
            init_JArr[3] = init_J_ar_CachePool(blockFrame);
            init_JArr[4] = init_J_ar_CachePool(barrierFrame);
            init_JArr[5] = init_J_ar_CachePool(spawnerFrame);
            init_JArr[6] = init_J_ar_CachePool(frameFrame);
            bK.init("frame.espMeshBake", perf);
         }

         double dx = init_Vec3.x - camera.x;
         double dy = init_Vec3.y - camera.y;
         double dz = init_Vec3.z - camera.z;
         Matrix4fc framePose = pose.pose();
         if (load_Z) {
            init_P.init(autismclient.render.AutismRenderTypes.storageEspFillSeeThrough(), framePose, dx, dy, dz);
         }

         if (read_Z) {
            load_P.init(autismclient.render.AutismRenderTypes.tracerEspLines(), framePose, dx, dy, dz);
         }

         return true;
      } catch (Throwable error) {
         init_Z = false;
         read_Z = false;
         load_Z = false;

         try {
            init_P.init_V();
            load_P.init_V();
         } catch (Throwable var31) {
         }

         AutismClientAddon.LOG.warn("ESP mesh buffers disabled, falling back to immediate mode", error);
         return false;
      }
   }

   private static long init_J_ar_CachePool(ar.Inner_CachePool frame) {
      return frame == null ? 0L : frame.load_J();
   }

   public static void load_V() {
      read_Z = false;
      load_Z = false;
      load_I = -2147483648;
      Arrays.fill(init_JArr, 0L);
      init_P.init_V();
      load_P.init_V();
   }

   private static void init_V_PoseStack_Pose(Pose pose, PkgCls_c bufferSource, int count, Vec3 cam) {
      Minecraft mc = Minecraft.getInstance();
      ClientLevel level = mc == null ? null : mc.level;
      if (level != null) {
         CardinalLighting lighting = level.cardinalLighting();
         load(count, cam);
         VertexConsumer buffer = bufferSource.init(autismclient.render.AutismRenderTypes.oreGhostSeeThrough());

         for (int i = 0; i < count; i++) {
            long packed = load_JArr[i];
            BlockState state = init_BlockStateArr[i];
            if (state != null) {
               autismclient.util.oresim.OreGhostModelCache.Inner_StreamUtil template = autismclient.util.oresim.OreGhostModelCache.init(state);
               if (!template.init_List().isEmpty()) {
                  int bx = BlockPos.getX(packed);
                  int by = BlockPos.getY(packed);
                  int bz = BlockPos.getZ(packed);
                  double ox = bx - cam.x;
                  double oy = by - cam.y;
                  double oz = bz - cam.z;
                  double relX = cam.x - (bx + 0.5);
                  double relY = cam.y - (by + 0.5);
                  double relZ = cam.z - (bz + 0.5);

                  for (autismclient.util.oresim.OreGhostModelCache.Inner_DataUtil face : template.init_List()) {
                     Direction cull = face.init_Direction();
                     if (cull == null
                        || !init_LongOpenHashSet.contains(BlockPos.asLong(bx + cull.getStepX(), by + cull.getStepY(), bz + cull.getStepZ()))
                           && !(relX * cull.getStepX() + relY * cull.getStepY() + relZ * cull.getStepZ() <= 0.0)) {
                        int color = init_I_F(face.load() == null ? 1.0F : lighting.byFace(face.load()));
                        float[] data = face.init_FArr();

                        for (int v = 0; v < 4; v++) {
                           int base = v * 5;
                           buffer.addVertex(pose, (float)(ox + data[base]), (float)(oy + data[base + 1]), (float)(oz + data[base + 2]))
                              .setUv(data[base + 3], data[base + 4])
                              .setColor(color);
                        }
                     }
                  }
               }
            }
         }
      }
   }

   private static int init_I_F(float shade) {
      int value = Mth.clamp(Math.round(shade * 255.0F), 0, 255);
      return 0xFF000000 | value << 16 | value << 8 | value;
   }

   static void drawClippedSegment(
      Pose entry, VertexConsumer buffer, double x1, double y1, double z1, double x2, double y2, double z2, Vec3 fwd, Vec3 up, int color, float width
   ) {
      double fx = fwd.x;
      double fy = fwd.y;
      double fz = fwd.z;
      double d1 = x1 * fx + y1 * fy + z1 * fz;
      double d2 = x2 * fx + y2 * fy + z2 * fz;
      double near = 0.15;
      if (!(d1 < near) || !(d2 < near)) {
         if (d1 < near) {
            double t = (near - d1) / (d2 - d1);
            x1 += (x2 - x1) * t;
            y1 += (y2 - y1) * t;
            z1 += (z2 - z1) * t;
         } else if (d2 < near) {
            double t = (near - d1) / (d2 - d1);
            x2 = x1 + (x2 - x1) * t;
            y2 = y1 + (y2 - y1) * t;
            z2 = z1 + (z2 - z1) * t;
            double offX = x2 - near * fx;
            double offY = y2 - near * fy;
            double offZ = z2 - near * fz;
            if (offX * offX + offY * offY + offZ * offZ < 1.0E-6) {
               x2 -= up.x * 0.05;
               y2 -= up.y * 0.05;
               z2 -= up.z * 0.05;
            }
         }

         float dx = (float)(x2 - x1);
         float dy = (float)(y2 - y1);
         float dz = (float)(z2 - z1);
         float lenSq = dx * dx + dy * dy + dz * dz;
         if (!(lenSq <= 1.0E-8F)) {
            float invLen = 1.0F / (float)Math.sqrt(lenSq);
            float nx = dx * invLen;
            float ny = dy * invLen;
            float nz = dz * invLen;
            if (nx * fx + ny * fy + nz * fz < 0.0) {
               nx = -nx;
               ny = -ny;
               nz = -nz;
            }

            buffer.addVertex(entry, (float)x1, (float)y1, (float)z1).setColor(color).setNormal(entry, nx, ny, nz).setLineWidth(width);
            buffer.addVertex(entry, (float)x2, (float)y2, (float)z2).setColor(color).setNormal(entry, nx, ny, nz).setLineWidth(width);
         }
      }
   }

   private static void init(Pose entry, VertexConsumer buffer, double x1, double y1, double z1, double x2, double y2, double z2, int color, float width) {
      drawClippedSegment(entry, buffer, x1, y1, z1, x2, y2, z2, traceForward, traceUp, color, width);
   }

   private static Vec3 init_Vec3_CameraRenderState(CameraRenderState camera) {
      if (camera == null) {
         return new Vec3(0.0, 0.0, -1.0);
      }

      Vector3f forward = new Vector3f(0.0F, 0.0F, -1.0F);
      camera.orientation.transform(forward);
      Vec3 result = new Vec3(forward.x, forward.y, forward.z);
      return result.lengthSqr() <= 1.0E-8 ? Vec3.directionFromRotation(camera.xRot, camera.yRot) : result.normalize();
   }

   private static void init(ClientLevel level, Vec3 from, float tickDelta, float width, boolean heightLine, List<ar.Inner_ConfigStore> out) {
      if (level != null) {
         long gameTime = level.getGameTime();
         int revision = al.init_I();
         if (init_ClientLevel != level || init_J != gameTime || read_I != revision) {
            List<ar.Inner_DataUtil> selected = new ArrayList<>();

            for (Entity entity : level.entitiesForRendering()) {
               if (am.init_Z(entity)) {
                  selected.add(new ar.Inner_DataUtil(entity, am.load_I(entity)));
               }
            }

            init_ClientLevel = level;
            init_J = gameTime;
            read_I = revision;
            init_List = List.copyOf(selected);
         }

         for (ar.Inner_DataUtil target : init_List) {
            Entity entity = target.init_Entity();
            if (entity != null && !entity.isRemoved()) {
               Vec3 base = init_Vec3_Entity_F(entity, tickDelta);
               Vec3 top = base.add(0.0, entity.getBbHeight(), 0.0);
               int color = target.init_I();
               if (from.distanceToSqr(base) > 1.0E-8) {
                  out.add(new ar.Inner_ConfigStore(from, base, color, width));
               }

               if (heightLine && base.distanceToSqr(top) > 1.0E-8) {
                  out.add(new ar.Inner_ConfigStore(base, top, color, width));
               }
            }
         }
      }
   }

   private static Vec3 init_Vec3_Entity_F(Entity entity, float tickDelta) {
      double dx = entity.getX() - entity.xOld;
      double dy = entity.getY() - entity.yOld;
      double dz = entity.getZ() - entity.zOld;
      return entity.tickCount > 1 && !(dx * dx + dy * dy + dz * dz > 256.0)
         ? new Vec3(
            Mth.lerp(tickDelta, entity.xOld, entity.getX()), Mth.lerp(tickDelta, entity.yOld, entity.getY()), Mth.lerp(tickDelta, entity.zOld, entity.getZ())
         )
         : entity.position();
   }

   private static void init_V_PoseStack_Pose_2(Pose pose, VertexConsumer buffer, AABB box, int color) {
      init(pose, buffer, box, color, 1.5F);
   }

   private static void init(Pose pose, VertexConsumer buffer, AABB box, int color, float width) {
      double x1 = box.minX;
      double y1 = box.minY;
      double z1 = box.minZ;
      double x2 = box.maxX;
      double y2 = box.maxY;
      double z2 = box.maxZ;
      load(pose, buffer, x1, y1, z1, x2, y1, z1, color, width);
      load(pose, buffer, x2, y1, z1, x2, y1, z2, color, width);
      load(pose, buffer, x2, y1, z2, x1, y1, z2, color, width);
      load(pose, buffer, x1, y1, z2, x1, y1, z1, color, width);
      load(pose, buffer, x1, y2, z1, x2, y2, z1, color, width);
      load(pose, buffer, x2, y2, z1, x2, y2, z2, color, width);
      load(pose, buffer, x2, y2, z2, x1, y2, z2, color, width);
      load(pose, buffer, x1, y2, z2, x1, y2, z1, color, width);
      load(pose, buffer, x1, y1, z1, x1, y2, z1, color, width);
      load(pose, buffer, x2, y1, z1, x2, y2, z1, color, width);
      load(pose, buffer, x2, y1, z2, x2, y2, z2, color, width);
      load(pose, buffer, x1, y1, z2, x1, y2, z2, color, width);
   }

   private static void init_V_PoseStack_Pose(Pose pose, VertexConsumer buffer, Vec3 a, Vec3 b, Vec3 planePoint, Vec3 forward, int color, float width) {
      double da = init(a, planePoint, forward);
      double db = init(b, planePoint, forward);
      if (!(da < 0.0) || !(db < 0.0)) {
         double ax = a.x;
         double ay = a.y;
         double az = a.z;
         double bx = b.x;
         double by = b.y;
         double bz = b.z;
         if (da < 0.0) {
            double t = -da / (db - da);
            ax = a.x + (b.x - a.x) * t;
            ay = a.y + (b.y - a.y) * t;
            az = a.z + (b.z - a.z) * t;
         } else if (db < 0.0) {
            double t = -db / (da - db);
            bx = b.x + (a.x - b.x) * t;
            by = b.y + (a.y - b.y) * t;
            bz = b.z + (a.z - b.z) * t;
         }

         cI.init(pose, buffer, ax, ay, az, bx, by, bz, color, width);
      }
   }

   private static double init(Vec3 point, Vec3 planePoint, Vec3 forward) {
      return (point.x - planePoint.x) * forward.x + (point.y - planePoint.y) * forward.y + (point.z - planePoint.z) * forward.z;
   }

   private static void load(Pose pose, VertexConsumer buffer, double x1, double y1, double z1, double x2, double y2, double z2, int color, float width) {
      cI.init(pose, buffer, x1, y1, z1, x2, y2, z2, color, width);
   }

   private static void load(Pose pose, VertexConsumer buffer, AABB box, int color) {
      if ((color >>> 24 & 0xFF) > 0) {
         init(pose, buffer, box.minX, box.minY, box.minZ, box.maxX, box.minY, box.minZ, box.maxX, box.minY, box.maxZ, box.minX, box.minY, box.maxZ, color);
         init(pose, buffer, box.minX, box.maxY, box.maxZ, box.maxX, box.maxY, box.maxZ, box.maxX, box.maxY, box.minZ, box.minX, box.maxY, box.minZ, color);
         init(pose, buffer, box.minX, box.minY, box.maxZ, box.maxX, box.minY, box.maxZ, box.maxX, box.maxY, box.maxZ, box.minX, box.maxY, box.maxZ, color);
         init(pose, buffer, box.maxX, box.minY, box.minZ, box.minX, box.minY, box.minZ, box.minX, box.maxY, box.minZ, box.maxX, box.maxY, box.minZ, color);
         init(pose, buffer, box.minX, box.minY, box.minZ, box.minX, box.minY, box.maxZ, box.minX, box.maxY, box.maxZ, box.minX, box.maxY, box.minZ, color);
         init(pose, buffer, box.maxX, box.minY, box.maxZ, box.maxX, box.minY, box.minZ, box.maxX, box.maxY, box.minZ, box.maxX, box.maxY, box.maxZ, color);
      }
   }

   private static void init(
      Pose pose,
      VertexConsumer buffer,
      double x1,
      double y1,
      double z1,
      double x2,
      double y2,
      double z2,
      double x3,
      double y3,
      double z3,
      double x4,
      double y4,
      double z4,
      int color
   ) {
      buffer.addVertex(pose, (float)x1, (float)y1, (float)z1).setColor(color);
      buffer.addVertex(pose, (float)x2, (float)y2, (float)z2).setColor(color);
      buffer.addVertex(pose, (float)x3, (float)y3, (float)z3).setColor(color);
      buffer.addVertex(pose, (float)x4, (float)y4, (float)z4).setColor(color);
   }

   private static int init_I_I_F(int color, float alphaMultiplier) {
      int alpha = Math.max(0, Math.min(255, (int)((color >>> 24 & 0xFF) * alphaMultiplier)));
      return alpha << 24 | color & 16777215;
   }

   private static int load(int color, float factor) {
      int r = color >>> 16 & 0xFF;
      int g = color >>> 8 & 0xFF;
      int b = color & 0xFF;
      r += (int)((255 - r) * factor);
      g += (int)((255 - g) * factor);
      b += (int)((255 - b) * factor);
      return color & 0xFF000000 | r << 16 | g << 8 | b;
   }

   private static float init_F_Str(String value, float fallback, float min, float max) {
      try {
         return Mth.clamp(Float.parseFloat(value), min, max);
      } catch (Exception ignored) {
         return fallback;
      }
   }

   private static ar.Inner_CachePool init_ar_CachePool_Module(Module module, ClientLevel level, Player player, float tickDelta) {
      if (module != null && level != null && player != null) {
         long scanKey = af.init_I();
         int revision = al.init_I();
         int chunkX = player.chunkPosition().x();
         int chunkZ = player.chunkPosition().z();
         ar.Inner_CachePool cached = init_ar_CachePool;
         if (cached.init(scanKey, revision, chunkX, chunkZ, level.getGameTime())) {
            return cached;
         }

         long perf = bK.read_J();
         List<ar.Inner_StreamUtil> boxes = new ArrayList<>();
         List<ar.Inner_BufferHelper> traces = new ArrayList<>();
         ap.init(
            module,
            level,
            player,
            tickDelta,
            (box, color, meshable) -> boxes.add(new ar.Inner_StreamUtil(box, color, meshable)),
            (target, color) -> traces.add(new ar.Inner_BufferHelper(target, color))
         );
         ar.Inner_CachePool next = ar.Inner_CachePool.init(
            scanKey, revision, chunkX, chunkZ, boxes, traces, Boolean.parseBoolean(module.value("meshing")), cached, level.getGameTime()
         );
         init_ar_CachePool = next;
         bK.init("join.storageEsp.scan", perf, 6000000L);
         return next;
      } else {
         return ar.Inner_CachePool.init_ar_CachePool();
      }
   }

   public static int init_I() {
      return init_ar_CachePool.init_List().size();
   }

   static int init_I_AABB(AABB region) {
      return init_ar_CachePool.init(region);
   }

   static int load(AABB region) {
      return load_ar_CachePool.init(region);
   }

   public static int load_I() {
      return init_ar_CachePool.load_List().size();
   }

   public static int read_I() {
      return load_ar_CachePool.init_List().size();
   }

   public static int write_I() {
      return load_ar_CachePool.load_List().size();
   }

   private static ar.Inner_CachePool init(Module module, ClientLevel level, Player player) {
      if (module != null && level != null && player != null) {
         long scanKey = af.init_I();
         int revision = al.init_I();
         int chunkX = player.chunkPosition().x();
         int chunkZ = player.chunkPosition().z();
         ar.Inner_CachePool cached = load_ar_CachePool;
         if (cached.init(scanKey, revision, chunkX, chunkZ, level.getGameTime())) {
            return cached;
         }

         long perf = bK.read_J();
         List<ar.Inner_StreamUtil> boxes = new ArrayList<>();
         List<ar.Inner_BufferHelper> traces = new ArrayList<>();
         ad.init(
            module,
            level,
            player,
            (box, color) -> boxes.add(new ar.Inner_StreamUtil(box, color, true)),
            (target, color) -> traces.add(new ar.Inner_BufferHelper(target, color))
         );
         ar.Inner_CachePool next = ar.Inner_CachePool.init(
            scanKey, revision, chunkX, chunkZ, boxes, traces, Boolean.parseBoolean(module.value("meshing")), cached, level.getGameTime()
         );
         load_ar_CachePool = next;
         bK.init("join.blockEsp.scan", perf, 6000000L);
         return next;
      } else {
         return ar.Inner_CachePool.init_ar_CachePool();
      }
   }

   private static ar.Inner_CachePool load(Module module, ClientLevel level, Player player) {
      if (module != null && level != null && player != null) {
         long scanKey = af.init_I();
         int revision = al.init_I();
         int chunkX = player.chunkPosition().x();
         int chunkZ = player.chunkPosition().z();
         ar.Inner_CachePool cached = write_ar_CachePool;
         if (cached.init(scanKey, revision, chunkX, chunkZ, level.getGameTime())) {
            return cached;
         }

         long perf = bK.read_J();
         List<ar.Inner_StreamUtil> boxes = new ArrayList<>();
         List<ar.Inner_BufferHelper> traces = new ArrayList<>();
         ao.init(
            module,
            level,
            player,
            (box, color) -> boxes.add(new ar.Inner_StreamUtil(box, color, true)),
            (target, color) -> traces.add(new ar.Inner_BufferHelper(target, color))
         );
         ar.Inner_CachePool next = ar.Inner_CachePool.init(
            scanKey, revision, chunkX, chunkZ, boxes, traces, Boolean.parseBoolean(module.value("meshing")), cached, level.getGameTime()
         );
         write_ar_CachePool = next;
         bK.init("join.spawnerEsp.scan", perf, 6000000L);
         return next;
      } else {
         return ar.Inner_CachePool.init_ar_CachePool();
      }
   }

   private static ar.Inner_CachePool init(ah.Inner_CachePool settings, ClientLevel level, Player player) {
      if (settings != null && level != null && player != null) {
         long scanKey = level.getGameTime();
         int revision = al.init_I();
         int chunkX = player.chunkPosition().x();
         int chunkZ = player.chunkPosition().z();
         ar.Inner_CachePool cached = update_ar_CachePool;
         if (cached.init(scanKey, revision, chunkX, chunkZ, level.getGameTime())) {
            return cached;
         }

         List<ar.Inner_StreamUtil> boxes = new ArrayList<>();
         List<ar.Inner_BufferHelper> traces = new ArrayList<>();
         ah.init(
            settings,
            level,
            player,
            (box, color) -> boxes.add(new ar.Inner_StreamUtil(box, color, false)),
            settings.sync() ? (target, color) -> traces.add(new ar.Inner_BufferHelper(target, color)) : null
         );
         ar.Inner_CachePool next = ar.Inner_CachePool.init(scanKey, revision, chunkX, chunkZ, boxes, traces, false, cached, level.getGameTime());
         update_ar_CachePool = next;
         return next;
      } else {
         return ar.Inner_CachePool.init_ar_CachePool();
      }
   }

   private static ar.Inner_CachePool read(Module module, ClientLevel level, Player player) {
      if (module != null && level != null && player != null) {
         int revision = al.init_I();
         long contentKey = ak.init(module, level);
         BlockPos anchor = player.blockPosition();
         int chunkX = player.chunkPosition().x();
         int chunkZ = player.chunkPosition().z();
         ar.Inner_CachePool cached = sync_ar_CachePool;
         if (contentKey == write_J && cached.init_I() == revision && cached.init(level.getGameTime()) && init_Z_BlockPos_BlockPos(anchor, read_BlockPos)) {
            return cached;
         }

         read_BlockPos = anchor;
         long perf = bK.read_J();
         List<ar.Inner_StreamUtil> boxes = new ArrayList<>();
         ak.init(module, level, player, (java.util.function.BiConsumer<AABB, Integer>)(box, color) -> boxes.add(new ar.Inner_StreamUtil(box, color, true)));
         ar.Inner_CachePool next = ar.Inner_CachePool.init(contentKey, revision, chunkX, chunkZ, boxes, List.of(), true, cached, level.getGameTime());
         sync_ar_CachePool = next;
         write_J = contentKey;
         bK.init("join.xrayEsp.scan", perf, 6000000L);
         return next;
      } else {
         return ar.Inner_CachePool.init_ar_CachePool();
      }
   }

   private static boolean init_Z_BlockPos_BlockPos(BlockPos now, BlockPos previous) {
      return previous != null
         && Math.abs(now.getX() - previous.getX()) < 2
         && Math.abs(now.getY() - previous.getY()) < 2
         && Math.abs(now.getZ() - previous.getZ()) < 2;
   }

   private static ar.Inner_CachePool write(Module module, ClientLevel level, Player player) {
      if (module == null || level == null || player == null) {
         return ar.Inner_CachePool.init_ar_CachePool();
      }

      if (!ak.init_Z(module)) {
         return ar.Inner_CachePool.init_ar_CachePool();
      }

      int revision = al.init_I();
      BlockPos anchor = player.blockPosition();
      long slot = level.getGameTime() >> 2;
      int engineRevision = autismclient.util.oresim.OreSimEngine.read_I();
      ar.Inner_CachePool cached = run_ar_CachePool;
      if (anchor.equals(load_BlockPos) && cached.init_I() == revision && slot == update_J && engineRevision == get_I) {
         return cached;
      }

      List<ar.Inner_StreamUtil> boxes = new ArrayList<>();
      ak.init(module, level, player, (ak.Inner_DataUtil)(pos, state) -> boxes.add(new ar.Inner_StreamUtil(new AABB(pos), ak.init(module, state.getBlock()), true)));
      ar.Inner_CachePool next = ar.Inner_CachePool.init(
         level.getGameTime(), revision, player.chunkPosition().x(), player.chunkPosition().z(), boxes, List.of(), true, cached, level.getGameTime()
      );
      run_ar_CachePool = next;
      load_BlockPos = anchor;
      update_J = slot;
      get_I = engineRevision;
      return next;
   }

   private static int init_I_Module(Module module, ClientLevel level, Player player, Vec3 camera) {
      if (module != null && level != null && player != null) {
         int revision = al.init_I();
         long contentKey = ak.init(module, level);
         BlockPos anchor = player.blockPosition();
         long slot = level.getGameTime() >> 2;
         if (contentKey == load_J && revision == open_I && anchor.equals(init_BlockPos) && slot == read_J) {
            return close_I;
         }

         load_J = contentKey;
         open_I = revision;
         init_BlockPos = anchor;
         read_J = slot;
         long perf = bK.read_J();
         int simCount = ak.init(module, player, read_JArr, init_IArr);
         init_LongOpenHashSet.clear();
         int candidateCount = 0;

         for (int i = 0; i < simCount && candidateCount < 2176; i++) {
            BlockState state = autismclient.util.oresim.OreBlockCatalog.Inner_StreamUtil.init_BlockState(init_IArr[i]);
            if (state != null && init_LongOpenHashSet.add(read_JArr[i])) {
               load_JArr[candidateCount] = read_JArr[i];
               init_BlockStateArr[candidateCount] = state;
               candidateCount++;
            }
         }

         int[] candidateCountRef = new int[]{candidateCount};
         ak.init(module, level, player, (ak.Inner_DataUtil)(pos, state) -> {
            long packed = pos.asLong();
            int index = candidateCountRef[0];
            if (index < 2176 && init_LongOpenHashSet.add(packed)) {
               load_JArr[index] = packed;
               init_BlockStateArr[index] = state;
               candidateCountRef[0] = index + 1;
            }
         });
         candidateCount = candidateCountRef[0];
         init_V_I_Vec3(candidateCount, player.getEyePosition());
         close_I = Math.min(candidateCount, 2048);
         init_LongOpenHashSet.clear();

         for (int i = 0; i < close_I; i++) {
            init_LongOpenHashSet.add(load_JArr[i]);
         }

         init_V_I(close_I);
         poll_I++;
         load(close_I, camera);
         bK.init("join.xrayGhost.scan", perf, 6000000L);
         return close_I;
      } else {
         return 0;
      }
   }

   private static void init_V_I_Vec3(int count, Vec3 eyes) {
      for (int i = 0; i < count; i++) {
         init_DArr[i] = init_D_J_Vec3(load_JArr[i], eyes);
      }

      for (int i = 1; i < count; i++) {
         double key = init_DArr[i];
         long position = load_JArr[i];
         BlockState state = init_BlockStateArr[i];

         int j;
         for (j = i - 1; j >= 0 && (init_DArr[j] > key || Double.compare(init_DArr[j], key) == 0 && Long.compare(load_JArr[j], position) > 0); j--) {
            init_DArr[j + 1] = init_DArr[j];
            load_JArr[j + 1] = load_JArr[j];
            init_BlockStateArr[j + 1] = init_BlockStateArr[j];
         }

         init_DArr[j + 1] = key;
         load_JArr[j + 1] = position;
         init_BlockStateArr[j + 1] = state;
      }
   }

   private static void load(int count, Vec3 cam) {
      if (count <= 1) {
         push_I = poll_I;
         pop_I = count;
         update_Vec3 = cam;
      } else if (push_I != poll_I || pop_I != count || update_Vec3 == null || !(update_Vec3.distanceToSqr(cam) < 0.015625)) {
         read(count, cam);
         push_I = poll_I;
         pop_I = count;
         update_Vec3 = cam;
      }
   }

   private static void init_V_I(int count) {
      int left = 0;

      for (int right = count - 1; left < right; right--) {
         long position = load_JArr[left];
         load_JArr[left] = load_JArr[right];
         load_JArr[right] = position;
         BlockState state = init_BlockStateArr[left];
         init_BlockStateArr[left] = init_BlockStateArr[right];
         init_BlockStateArr[right] = state;
         left++;
      }
   }

   private static void read(int count, Vec3 cam) {
      for (int i = 0; i < count; i++) {
         init_DArr[i] = init_D_J_Vec3(load_JArr[i], cam);
      }

      for (int i = 1; i < count; i++) {
         double key = init_DArr[i];
         long position = load_JArr[i];
         BlockState state = init_BlockStateArr[i];

         int j;
         for (j = i - 1; j >= 0 && (init_DArr[j] < key || Double.compare(init_DArr[j], key) == 0 && Long.compare(load_JArr[j], position) < 0); j--) {
            init_DArr[j + 1] = init_DArr[j];
            load_JArr[j + 1] = load_JArr[j];
            init_BlockStateArr[j + 1] = init_BlockStateArr[j];
         }

         init_DArr[j + 1] = key;
         load_JArr[j + 1] = position;
         init_BlockStateArr[j + 1] = state;
      }
   }

   private static double init_D_J_Vec3(long packed, Vec3 origin) {
      double dx = BlockPos.getX(packed) + 0.5 - origin.x;
      double dy = BlockPos.getY(packed) + 0.5 - origin.y;
      double dz = BlockPos.getZ(packed) + 0.5 - origin.z;
      return dx * dx + dy * dy + dz * dz;
   }

   private static ar.Inner_CachePool update(Module module, ClientLevel level, Player player) {
      if (module != null && level != null && player != null) {
         long scanKey = af.init_I();
         int revision = al.init_I();
         int chunkX = player.chunkPosition().x();
         int chunkZ = player.chunkPosition().z();
         ar.Inner_CachePool cached = read_ar_CachePool;
         if (cached.init(scanKey, revision, chunkX, chunkZ, level.getGameTime())) {
            return cached;
         }

         long perf = bK.read_J();
         List<ar.Inner_StreamUtil> boxes = new ArrayList<>();
         List<ar.Inner_BufferHelper> traces = new ArrayList<>();
         ad.load(
            module,
            level,
            player,
            (box, color) -> boxes.add(new ar.Inner_StreamUtil(box, color, true)),
            (target, color) -> traces.add(new ar.Inner_BufferHelper(target, color))
         );
         ar.Inner_CachePool next = ar.Inner_CachePool.init(
            scanKey, revision, chunkX, chunkZ, boxes, traces, Boolean.parseBoolean(module.value("meshing")), cached, level.getGameTime()
         );
         read_ar_CachePool = next;
         bK.init("join.barrierEsp.scan", perf, 6000000L);
         return next;
      } else {
         return ar.Inner_CachePool.init_ar_CachePool();
      }
   }

   private static ar.Inner_JsonUtil init(Module module, Minecraft mc, ClientLevel level) {
      if (module != null && mc != null && level != null) {
         long gameTime = level.getGameTime();
         AutismWaypoints store = AutismWaypoints.init_AutismWaypoints();
         long revision = store.init_J();
         String scope = AuditWaypointScope.current(mc);
         float dotSize = init_F_Str(module.value("dot-size"), 3.0F, 1.0F, 10.0F);
         ar.Inner_JsonUtil cached = init_ar_JsonUtil;
         if (cached.init(gameTime, revision, scope, dotSize)) {
            return cached;
         }

         List<ar.Inner_NetHelper> previous = cached.load() == revision && cached.init_Str().equals(scope) ? cached.init_List() : null;
         List<ar.Inner_NetHelper> dots = new ArrayList<>();
         double half = 0.15 * dotSize * 0.5;
         double discRadius = 0.046 * dotSize;
         int index = 0;

         for (AutismWaypoints.Waypoint waypoint : store.init_List_Str(scope)) {
            double cx = waypoint.x() + 0.5;
            double cy = waypoint.y() + 0.5;
            double cz = waypoint.z() + 0.5;
            ar.Inner_NetHelper prior = previous != null && index < previous.size() ? previous.get(index) : null;
            FormattedCharSequence name;
            float nameWidth;
            if (prior != null) {
               name = prior.init_FormattedCharSeque();
               nameWidth = prior.init_F();
            } else {
               name = Component.literal(waypoint.name()).getVisualOrderText();
               nameWidth = mc.font.width(name);
            }

            dots.add(new ar.Inner_NetHelper(new AABB(cx - half, cy - half, cz - half, cx + half, cy + half, cz + half), waypoint.color(), name, nameWidth));
            index++;
         }

         ar.Inner_JsonUtil next = new ar.Inner_JsonUtil(gameTime, revision, scope, dotSize, discRadius, List.copyOf(dots));
         init_ar_JsonUtil = next;
         return next;
      } else {
         return ar.Inner_JsonUtil.init_ar_JsonUtil();
      }
   }

   private static float init_F_D(double dist) {
      if (dist >= 14.0) {
         return (float)Mth.clamp(dist / 12.0, 1.0, 12.0);
      } else {
         return dist >= 6.0 ? 1.0F : 0.6F + 0.39999998F * (float)((dist - 2.5) / 3.5);
      }
   }

   private static float load(double dist) {
      return dist >= 6.0 ? 1.0F : (float)Mth.clamp((dist - 2.5) / 3.5, 0.0, 1.0);
   }

   public static boolean read_Z() {
      return !load_ar_JsonUtil.init_List().isEmpty();
   }

   public static void read(PoseStack matrices) {
      ar.Inner_JsonUtil snapshot = load_ar_JsonUtil;
      if (!snapshot.init_List().isEmpty()) {
         load_ar_JsonUtil = ar.Inner_JsonUtil.init_ar_JsonUtil();
         Minecraft mc = Minecraft.getInstance();
         if (mc != null && mc.font != null) {
            Vec3 camera = read_Vec3;
            Quaternionf orientation = init_Quaternionf;
            Vector3f right = orientation.transform(init_Vector3f.set(1.0F, 0.0F, 0.0F));
            float rx = right.x;
            float ry = right.y;
            float rz = right.z;
            Vector3f up = orientation.transform(init_Vector3f.set(0.0F, 1.0F, 0.0F));
            float ux = up.x;
            float uy = up.y;
            float uz = up.z;
            Pose pose = matrices.last();
            final PkgCls_c bufferSource = read_c_DataUtil.init();
            final Matrix4f textPose = new Matrix4f();
            GlyphVisitor waypointGlyphs = new GlyphVisitor() {
               @Override
               public void acceptRenderable(TextRenderable renderable) {
                  renderable.render(textPose, bufferSource.init(renderable.renderType(DisplayMode.SEE_THROUGH)), 15728880, false);
               }
            };
            Identifier discTexture = init_Identifier_Minecraft(mc);
            double baseRadius = snapshot.init_D();
            double maxRenderDist = 128.0;
            if (discTexture != null) {
               VertexConsumer discBuffer = bufferSource.init(autismclient.render.AutismRenderTypes.waypointDiscSeeThrough(discTexture));

               for (ar.Inner_NetHelper dot : snapshot.init_List()) {
                  double cx = (dot.init_AABB().minX + dot.init_AABB().maxX) * 0.5;
                  double centerY = (dot.init_AABB().minY + dot.init_AABB().maxY) * 0.5;
                  double cz = (dot.init_AABB().minZ + dot.init_AABB().maxZ) * 0.5;
                  double dx = cx - camera.x;
                  double dy = centerY - camera.y;
                  double dz = cz - camera.z;
                  double distSq = dx * dx + dy * dy + dz * dz;
                  if (!(distSq < 6.25)) {
                     double dist = Math.sqrt(distSq);
                     double renderDist = dist > maxRenderDist ? maxRenderDist : dist;
                     double factor = dist > maxRenderDist ? maxRenderDist / dist : 1.0;
                     double rdx = dx * factor;
                     double rdy = dy * factor;
                     double rdz = dz * factor;
                     float scale = init_F_D(renderDist);
                     float alpha = load(dist);
                     if (!(alpha <= 0.0F)) {
                        float radius = (float)(baseRadius * scale);
                        int color = init_I_I_F(dot.init_I(), alpha);
                        init_V_VertexConsumer(
                           discBuffer, pose, rdx + (-rx - ux) * radius, rdy + (-ry - uy) * radius, rdz + (-rz - uz) * radius, 0.0F, 1.0F, color
                        );
                        init_V_VertexConsumer(
                           discBuffer, pose, rdx + (-rx + ux) * radius, rdy + (-ry + uy) * radius, rdz + (-rz + uz) * radius, 0.0F, 0.0F, color
                        );
                        init_V_VertexConsumer(discBuffer, pose, rdx + (rx + ux) * radius, rdy + (ry + uy) * radius, rdz + (rz + uz) * radius, 1.0F, 0.0F, color);
                        init_V_VertexConsumer(discBuffer, pose, rdx + (rx - ux) * radius, rdy + (ry - uy) * radius, rdz + (rz - uz) * radius, 1.0F, 1.0F, color);
                     }
                  }
               }
            }

            Module waypoints = flush();
            boolean showDistance = waypoints == null || Boolean.parseBoolean(waypoints.value("show-distance"));

            for (ar.Inner_NetHelper dot : snapshot.init_List()) {
               double cx = (dot.init_AABB().minX + dot.init_AABB().maxX) * 0.5;
               double centerY = (dot.init_AABB().minY + dot.init_AABB().maxY) * 0.5;
               double cz = (dot.init_AABB().minZ + dot.init_AABB().maxZ) * 0.5;
               double dx = cx - camera.x;
               double dy = centerY - camera.y;
               double dz = cz - camera.z;
               double distSq = dx * dx + dy * dy + dz * dz;
               if (!(distSq < 6.25)) {
                  double dist = Math.sqrt(distSq);
                  double renderDist = dist > maxRenderDist ? maxRenderDist : dist;
                  double factor = dist > maxRenderDist ? maxRenderDist / dist : 1.0;
                  double rdx = dx * factor;
                  double rdy = dy * factor;
                  double rdz = dz * factor;
                  float scale = init_F_D(renderDist);
                  float alpha = load(dist);
                  if (!(alpha <= 0.0F)) {
                     double labelOffsetY = scale * (baseRadius + 0.08 + 0.14);
                     int color = init_I_I_F(dot.init_I(), alpha);
                     int bgColor = init_I_I_F(-2013265920, alpha);
                     float poseScale = 0.028F * scale;
                     String distanceText;
                     float distanceWidth;
                     if (showDistance) {
                        int distMeters = (int)dist;
                        if (dot.cachedDistMeters != distMeters) {
                           dot.cachedDistMeters = distMeters;
                           dot.cachedDistText = " [" + distMeters + "m]";
                           dot.cachedDistWidth = mc.font.width(dot.cachedDistText);
                        }

                        distanceText = dot.cachedDistText;
                        distanceWidth = dot.cachedDistWidth;
                     } else {
                        distanceText = "";
                        distanceWidth = 0.0F;
                     }

                     float nameX = -(dot.init_F() + distanceWidth) * 0.5F;
                     textPose.set(pose.pose())
                        .translate((float)rdx, (float)(rdy + labelOffsetY), (float)rdz)
                        .rotate(orientation)
                        .scale(poseScale, -poseScale, poseScale);
                     mc.font.prepareText(dot.init_FormattedCharSeque(), nameX, -4.5F, color, true, false, bgColor).visit(waypointGlyphs);
                     if (showDistance) {
                        int distanceColor = init_I_I_F(-3355444, alpha);
                        float distanceX = nameX + dot.init_F();
                        mc.font.prepareText(distanceText, distanceX, -4.5F, distanceColor, true, bgColor).visit(waypointGlyphs);
                     }
                  }
               }
            }

            bufferSource.init();
         }
      }
   }

   private static void init_V_VertexConsumer(VertexConsumer buffer, Pose pose, double x, double y, double z, float u, float v, int color) {
      buffer.addVertex(pose, (float)x, (float)y, (float)z).setUv(u, v).setColor(color);
   }

   private static Identifier init_Identifier_Minecraft(Minecraft mc) {
      if (state) {
         return queue ? null : init_Identifier;
      }

      state = true;

      try {
         mc.getTextureManager().register(init_Identifier, new ar.Inner_TaskQueue(init_NativeImage()));
      } catch (Throwable t) {
         queue = true;
         AutismClientAddon.LOG.warn("Failed to create the waypoint disc texture", t);
      }

      return queue ? null : init_Identifier;
   }

   private static NativeImage init_NativeImage() {
      NativeImage image = new NativeImage(16, 16, false);
      int transparent = 0;
      int border = -15198184;
      int shadow = -5723992;
      int mid = -1644826;
      int bright = -1;

      for (int y = 0; y < 16; y++) {
         for (int x = 0; x < 16; x++) {
            int md = Math.abs(x - 7) + Math.abs(y - 7);
            if (x == 15 || y == 15 || md > 7) {
               image.setPixel(x, y, transparent);
            } else if (md == 7) {
               image.setPixel(x, y, border);
            } else if (md == 6) {
               image.setPixel(x, y, x + y <= 14 ? bright : shadow);
            } else if (md <= 2) {
               image.setPixel(x, y, bright);
            } else {
               image.setPixel(x, y, x <= 7 && y <= 7 ? bright : (x >= 8 && y >= 8 ? shadow : mid));
            }
         }
      }

      return image;
   }

   private record Inner_BufferHelper(Vec3 init_Vec3, int init_I) {
   }

   private record Inner_CachePool(
      long init_J,
      int init_I,
      int load_I,
      int read_I,
      long load_J,
      List<ar.Inner_StreamUtil> init_List,
      List<ar.Inner_BufferHelper> load_List,
      boolean init_Z,
      List<ar.Inner_StreamUtil> read_List,
      List<ag.Inner_DataUtil> write_List,
      AtomicReference<ag.Inner_CachePool> init_ag_CachePool,
      long read_J
   ) {
      private static long write_J = 1L;
      private static final AtomicReference<ag.Inner_CachePool> EMPTY_MESH = new AtomicReference<>(ag.init_ag_CachePool(List.of()));
      private static final ar.Inner_CachePool init_ar_CachePool = new ar.Inner_CachePool(
         -9223372036854775808L, -1, -2147483648, -2147483648, 0L, List.of(), List.of(), false, List.of(), List.of(), EMPTY_MESH, -9223372036854775808L
      );

      private static ar.Inner_CachePool init_ar_CachePool() {
         return init_ar_CachePool;
      }

      private static ar.Inner_CachePool init(
         long scanKey,
         int revision,
         int chunkX,
         int chunkZ,
         List<ar.Inner_StreamUtil> mutableBoxes,
         List<ar.Inner_BufferHelper> mutableTraces,
         boolean meshing,
         ar.Inner_CachePool previous,
         long gameTime
      ) {
         List<ar.Inner_StreamUtil> boxes = List.copyOf(mutableBoxes);
         List<ar.Inner_BufferHelper> traces = List.copyOf(mutableTraces);
         if (!meshing) {
            boolean same = previous != null && !previous.init_Z() && previous.init_List().size() == boxes.size() && previous.init_List().equals(boxes);
            return new ar.Inner_CachePool(
               scanKey, revision, chunkX, chunkZ, same ? previous.load_J() : write_J++, boxes, traces, false, List.of(), List.of(), EMPTY_MESH, gameTime
            );
         }

         List<ar.Inner_StreamUtil> unmeshed = new ArrayList<>();
         List<ag.Inner_DataUtil> source = new ArrayList<>();

         for (ar.Inner_StreamUtil target : boxes) {
            if (target.init_Z()) {
               source.add(new ag.Inner_DataUtil(target.init_AABB(), target.init_I()));
            } else {
               unmeshed.add(target);
            }
         }

         source.sort(ar.Inner_CachePool::init);
         List<ag.Inner_DataUtil> frozenSource = List.copyOf(source);
         List<ar.Inner_StreamUtil> frozenUnmeshed = List.copyOf(unmeshed);
         boolean same = previous != null
            && previous.init_Z()
            && previous.write_List().size() == frozenSource.size()
            && previous.write_List().equals(frozenSource)
            && previous.read_List().equals(frozenUnmeshed);
         AtomicReference<ag.Inner_CachePool> geometryRef;
         if (same) {
            geometryRef = previous.init_ag_CachePool();
         } else if (frozenSource.size() <= 48) {
            geometryRef = new AtomicReference<>(ag.init_ag_CachePool(frozenSource));
         } else {
            geometryRef = new AtomicReference<>(null);
            af.submitEspWork(() -> {
               try {
                  geometryRef.set(ag.init_ag_CachePool(frozenSource));
               } catch (Throwable var3x) {
               }
            });
         }

         return new ar.Inner_CachePool(
            scanKey, revision, chunkX, chunkZ, same ? previous.load_J() : write_J++, boxes, traces, true, frozenUnmeshed, frozenSource, geometryRef, gameTime
         );
      }

      private static int init(ag.Inner_DataUtil left, ag.Inner_DataUtil right) {
         int compared = Integer.compare(left.init_I(), right.init_I());
         if (compared != 0) {
            return compared;
         }

         compared = Double.compare(left.init_AABB().minX, right.init_AABB().minX);
         if (compared != 0) {
            return compared;
         }

         compared = Double.compare(left.init_AABB().minY, right.init_AABB().minY);
         return compared != 0 ? compared : Double.compare(left.init_AABB().minZ, right.init_AABB().minZ);
      }

      private boolean init(long scanKey, int revision, int chunkX, int chunkZ, long gameTime) {
         return this.init_J == scanKey && this.init_I == revision && this.load_I == chunkX && this.read_I == chunkZ && this.init(gameTime);
      }

      private boolean init(long gameTime) {
         return gameTime >= this.read_J && gameTime - this.read_J < 100L;
      }

      private int init(AABB region) {
         int count = 0;

         for (ar.Inner_StreamUtil target : this.init_List) {
            if (target.init_AABB().intersects(region)) {
               count++;
            }
         }

         return count;
      }

      private void init(BiConsumer<Vec3, Integer> consumer) {
         for (ar.Inner_BufferHelper trace : this.load_List) {
            consumer.accept(trace.init_Vec3(), trace.init_I());
         }
      }

      private void init(Pose pose, VertexConsumer buffer, Vec3 camera, float opacity) {
         ag.Inner_CachePool geometry = this.init_Z ? this.init_ag_CachePool.get() : null;
         if (geometry == null) {
            for (ar.Inner_StreamUtil target : this.init_List) {
               ar.load(pose, buffer, target.init_AABB().move(-camera.x, -camera.y, -camera.z), ar.init_I_I_F(target.init_I(), opacity));
            }
         } else {
            for (ag.Inner_ConfigStore face : geometry.init_List()) {
               ar.init(
                  pose,
                  buffer,
                  face.init_D() - camera.x,
                  face.load() - camera.y,
                  face.read() - camera.z,
                  face.write() - camera.x,
                  face.update() - camera.y,
                  face.sync() - camera.z,
                  face.flush() - camera.x,
                  face.close() - camera.y,
                  face.open() - camera.z,
                  face.poll() - camera.x,
                  face.push() - camera.y,
                  face.pop() - camera.z,
                  ar.init_I_I_F(face.init_I(), opacity)
               );
            }

            for (ar.Inner_StreamUtil target : this.read_List) {
               ar.load(pose, buffer, target.init_AABB().move(-camera.x, -camera.y, -camera.z), ar.init_I_I_F(target.init_I(), opacity));
            }
         }
      }

      private void load(Pose pose, VertexConsumer buffer, Vec3 camera, float width) {
         ag.Inner_CachePool geometry = this.init_Z ? this.init_ag_CachePool.get() : null;
         if (geometry == null) {
            for (ar.Inner_StreamUtil target : this.init_List) {
               ar.init(pose, buffer, target.init_AABB().move(-camera.x, -camera.y, -camera.z), target.init_I(), width);
            }
         } else {
            for (ag.Inner_StreamUtil edge : geometry.load()) {
               ar.load(
                  pose,
                  buffer,
                  edge.init_D() - camera.x,
                  edge.load() - camera.y,
                  edge.read() - camera.z,
                  edge.write() - camera.x,
                  edge.update() - camera.y,
                  edge.sync() - camera.z,
                  edge.init_I(),
                  width
               );
            }

            for (ar.Inner_StreamUtil target : this.read_List) {
               ar.init(pose, buffer, target.init_AABB().move(-camera.x, -camera.y, -camera.z), target.init_I(), width);
            }
         }
      }
   }

   private record Inner_ConfigStore(Vec3 init_Vec3, Vec3 load, int init_I, float init_F) {
   }

   private record Inner_DataUtil(Entity init_Entity, int init_I) {
   }

   private record Inner_JsonUtil(long init_J, long load, String init_Str, float init_F, double init_D, List<ar.Inner_NetHelper> init_List) {
      private static final ar.Inner_JsonUtil init_ar_JsonUtil = new ar.Inner_JsonUtil(-9223372036854775808L, -1L, "", -1.0F, 0.0, List.of());

      private static ar.Inner_JsonUtil init_ar_JsonUtil() {
         return init_ar_JsonUtil;
      }

      private boolean init(long gameTime, long revision, String scope, float dotSize) {
         return this.load == revision && this.init_F == dotSize && this.init_Str.equals(scope);
      }
   }

   private static final class Inner_NetHelper {
      private final AABB init_AABB;
      private final int init_I;
      private final FormattedCharSequence init_FormattedCharSeque;
      private final float init_F;
      private int cachedDistMeters = -1;
      private String cachedDistText = "";
      private float cachedDistWidth = 0.0F;

      private Inner_NetHelper(AABB init_AABB, int init_I, FormattedCharSequence init_FormattedCharSeque, float init_F) {
         this.init_AABB = init_AABB;
         this.init_I = init_I;
         this.init_FormattedCharSeque = init_FormattedCharSeque;
         this.init_F = init_F;
      }

      private AABB init_AABB() {
         return this.init_AABB;
      }

      private int init_I() {
         return this.init_I;
      }

      private FormattedCharSequence init_FormattedCharSeque() {
         return this.init_FormattedCharSeque;
      }

      private float init_F() {
         return this.init_F;
      }
   }

   private record Inner_StreamUtil(AABB init_AABB, int init_I, boolean init_Z) {
   }

   private static final class Inner_TaskQueue extends AbstractTexture {
      private Inner_TaskQueue(NativeImage pixels) {
         try {
            this.texture = RenderSystem.getDevice().createTexture("waypoint_disc", 5, GpuFormat.RGBA8_UNORM, pixels.getWidth(), pixels.getHeight(), 1, 1);
            this.sampler = RenderSystem.getSamplerCache().getClampToEdge(FilterMode.NEAREST);
            this.textureView = RenderSystem.getDevice().createTextureView(this.texture);
            RenderSystem.getDevice().createCommandEncoder().writeToTexture(this.texture, pixels);
         } finally {
            pixels.close();
         }
      }
   }
}

package autismclient.ai;

import autismclient.AutismClientAddon;
import autismclient.util.AutismBufferSource;
import autismclient.util.OverlayManager;
import autismclient.util.OverlayBounds;
import autismclient.util.AutismOverlay;
import com.google.gson.JsonObject;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.PoseStack.Pose;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;
import java.util.function.Supplier;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.rendering.v1.level.LevelRenderEvents;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.client.color.block.BlockTintSource;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public final class AiVisualization {
   public static final int init_I = -12067963;
   public static final int load_I = -11093505;
   public static final int read_I = -20923;
   public static final int write_I = -885830;
   public static final int update_I = 1885197528;
   public static final int sync_I = -610078735;
   private static final int flush_I = 192;
   private static final int maxRouteSegments = 384;
   private static final int maxMarkers = 32;
   private static final int close = 64;
   private static final int open = 8;
   private static final int poll = 16384;
   private static final long init_J = 450000000L;
   private static final long load_J = 200000000L;
   private static final Map<Object, AiVisualization.OverlayLayer> layers = new IdentityHashMap<>();
   private static final Map<BlockState, autismclient.util.oresim.OreGhostModelCache.Inner_StreamUtil> ghostModelCache = new IdentityHashMap<>();
   private static final AutismBufferSource.LazySource ghostBufferAllocator = new AutismBufferSource.LazySource(4194304);
   private static AiSettings settings = new AiSettings();
   private static boolean initialized;
   private static ClientLevel lastLevel;
   private static Object lastModelSet;
   private static AiVisualization.RenderFrame frame = AiVisualization.RenderFrame.EMPTY;
   private static AiVisualization.RenderFrame hudFrame = AiVisualization.RenderFrame.EMPTY;
   private static int tickCounter;
   private static int statQuads;
   private static int statGhosts;
   private static int statSegments;
   private static long statRenderNanos;
   private static final long[] frameCostNanos = new long[120];
   private static int frameCostCursor;
   private static int frameCostCount;
   private static int statGreen;
   private static int statBlue;
   private static int statOrange;
   private static long statCollectNanos;
   private static int opacityPreview = -1;
   private static long opacityPreviewUntil;
   private static long lastSettingsRefresh;
   private static long updateSequence;
   private static int statMiningTargets;
   private static int statHudWidth;
   private static int statHudContentWidth;
   private static final int circleSegs = 12;
   private static final double[] circleCos = new double[13];
   private static final double[] circleSin = new double[13];

   static {
      for (int i = 0; i <= 12; i++) {
         double angle = (Math.PI * 2) * i / 12.0;
         circleCos[i] = Math.cos(angle);
         circleSin[i] = Math.sin(angle);
      }
   }

   private AiVisualization() {
   }

   public static void initialize() {
      if (!initialized) {
         initialized = true;
         ClientTickEvents.END_CLIENT_TICK.register(mc -> {
            if (lastLevel != mc.level) {
               clearAllLayers();
               lastLevel = mc.level;
            }

            if (++tickCounter % 5 == 0) {
               AiService s = AiService.getExistingInstance();
               if (s != null) {
                  settings = s.getSettings();
               } else if (!layers.isEmpty() && System.nanoTime() - lastSettingsRefresh > 5000000000L) {
                  settings = AiDirectOps.settings();
                  lastSettingsRefresh = System.nanoTime();
               }
            }

            long now = System.nanoTime();
            layers.values().removeIf(l -> {
               boolean expired = l.level != mc.level || now - l.touchedNanos > 450000000L;
               if (expired) {
                  l.closeMesh();
               }

               return expired;
            });
         });
         LevelRenderEvents.COLLECT_SUBMITS.register(context -> collectFrame(context.levelState().cameraRenderState.pos));
         ScreenEvents.AFTER_INIT.register((mc, screen, width, height) -> {
            if (screen instanceof AbstractContainerScreen) {
               ScreenEvents.afterExtract(screen).register((shown, graphics, mouseX, mouseY, delta) -> {
                  if (isHudVisible() && !mc.gui.hud.isHidden() && !autismclient.modules.aD.init_Z() && autismclient.e.TokenStore.init()) {
                     graphics.nextStratum();
                     autismclient.util.cA.init_V_GuiGraphicsExtract(graphics);

                     try {
                        renderHud(graphics);
                     } finally {
                        autismclient.util.cA.load_V_GuiGraphicsExtract(graphics);
                     }
                  }
               });
            }
         });
      }
   }

   private static AiVisualization.OverlayLayer layerFor(Object owner) {
      Minecraft mc = Minecraft.getInstance();
      if (mc.level != null && mc.player != null) {
         AiVisualization.OverlayLayer l = layers.computeIfAbsent(owner, k -> {
            AiVisualization.OverlayLayer created = new AiVisualization.OverlayLayer();
            created.creationOrder = ++updateSequence;
            return created;
         });
         l.level = mc.level;
         l.touchedNanos = System.nanoTime();
         return l;
      } else {
         return null;
      }
   }

   public static void clearOwner(Object owner) {
      AiVisualization.OverlayLayer l = layers.remove(owner);
      if (l != null) {
         l.closeMesh();
      }
   }

   public static void clearAllLayers() {
      for (AiVisualization.OverlayLayer l : layers.values()) {
         l.closeMesh();
      }

      layers.clear();
      frame = hudFrame = AiVisualization.RenderFrame.EMPTY;
      ghostModelCache.clear();
      lastModelSet = null;
      statHudWidth = 0;
   }

   public static void showCount(Object owner, String title, String activity, int completed, int target) {
      AiVisualization.OverlayLayer l = layerFor(owner);
      if (l != null) {
         l.taskStatus = AiTaskStatus.progressStatus(title, activity, completed, target);
         l.immediateStatus = false;
         l.statusOrder = ++updateSequence;
      }
   }

   public static void showDetail(Object owner, String title, String activity, String detail, int percent) {
      AiVisualization.OverlayLayer l = layerFor(owner);
      if (l != null) {
         l.taskStatus = new AiTaskStatus.Inner_StreamUtil(title, activity, detail, percent);
         l.immediateStatus = true;
         l.statusOrder = ++updateSequence;
      }
   }

   public static void showProgress(Object owner, String title, String activity, int metersAway, int percent) {
      AiVisualization.OverlayLayer l = layerFor(owner);
      if (l != null) {
         l.navStatus = new AiTaskStatus.Inner_StreamUtil(title, activity, metersAway + " m away", percent);
         l.navPriority = 12;
         l.navOrder = ++updateSequence;
      }
   }

   static void showNavStatus(Object owner, String title, String activity, int percent) {
      AiVisualization.OverlayLayer l = layerFor(owner);
      if (l != null) {
         l.navStatus = new AiTaskStatus.Inner_StreamUtil(title, activity, "", percent);
         l.navPriority = 5;
         l.navOrder = ++updateSequence;
      }
   }

   static void showContent(Object owner, AiTaskStatus.Inner_StreamUtil content) {
      AiVisualization.OverlayLayer l = layerFor(owner);
      if (l != null) {
         l.content = content;
         l.contentOrder = ++updateSequence;
      }
   }

   static int init_I() {
      long now = System.nanoTime();
      return layers.values()
         .stream()
         .filter(l -> !l.hasTarget && l.priority == 40 && now - l.touchedNanos <= 450000000L && l.level == Minecraft.getInstance().level)
         .mapToInt(l -> l.targets.size())
         .sum();
   }

   public static void putAt_V_Obj_3(Object owner, List<autismclient.ai.engine.AiPathfinder.PathStep> path, int index, Supplier<BlockState> supportState) {
      showPath(owner, path, index, supportState);
   }

   public static void showPath(Object owner, List<autismclient.ai.engine.AiPathfinder.PathStep> path, int index, Supplier<BlockState> supportState) {
      AiVisualization.OverlayLayer l = layerFor(owner);
      if (l != null && settings.visualizePaths && (l.pathSource != path || l.pathIndex != index)) {
         l.pathSource = path;
         l.pathIndex = index;
         l.lastScriptLength = -1;
         l.lastCheckpointIndex = -1;
         l.activityStatus = new AiTaskStatus.Inner_StreamUtil("Travelling", "Following the planned route", "");
         l.priority = 10;
         List<AiVisualization.RouteSegment> segments = new ArrayList<>();
         List<AiVisualization.Marker> markers = new ArrayList<>();
         buildPathVisuals(path, index, segments, markers);
         List<AiVisualization.GhostBlock> edits = new ArrayList<>();
         Minecraft mc = Minecraft.getInstance();
         int end = Math.min(path.size(), Math.max(0, index) + 192);
         BlockState support = supportState.get();

         for (int i = Math.max(0, index); i < end; i++) {
            autismclient.ai.engine.AiPathfinder.PathStep m = path.get(i);

            for (BlockPos p : m.cellsToBreak()) {
               if (edits.size() < 64 && AiTerrain.isLoaded(mc.level, p)) {
                  edits.add(ghost(p, mc.level.getBlockState(p), false));
               }
            }

            for (BlockPos p : m.cellsToPlace()) {
               if (edits.size() < 64 && AiTerrain.isLoaded(mc.level, p)) {
                  edits.add(coloredGhost(p, support == null ? Blocks.AIR.defaultBlockState() : support, support == null ? -885830 : -11093505));
               }
            }
         }

         l.segments = List.copyOf(segments);
         l.markers = List.copyOf(markers);
         l.ghosts = edits.stream().filter(g -> !g.mining).toList();
         l.targets = edits.stream().filter(g -> g.mining).toList();
      }
   }

   public static void showPaceScript(Object owner, autismclient.ai.engine.AiPaceScript.Inner_IoHelper script, int tickIndex, List<BlockPos> checkpoints, int checkpointIndex) {
      AiVisualization.OverlayLayer l = layerFor(owner);
      if (l != null && script != null && settings.visualizePaths) {
         int length = script.size();
         if (l.pathSource != script || l.pathIndex != tickIndex || l.lastScriptLength != length || l.lastCheckpointIndex != checkpointIndex) {
            l.pathSource = script;
            l.pathIndex = tickIndex;
            l.lastScriptLength = length;
            l.lastCheckpointIndex = checkpointIndex;
            List<AiVisualization.RouteSegment> segments = new ArrayList<>();
            List<AiVisualization.Marker> markers = new ArrayList<>();
            buildScriptVisuals(script, tickIndex, checkpoints, checkpointIndex, segments, markers);
            l.segments = List.copyOf(segments);
            l.markers = List.copyOf(markers);
            l.ghosts = List.of();
            l.targets = List.of();
         }
      }
   }

   public static void showPreview(Object owner, autismclient.ai.engine.AiPaceScript.Inner_JsonCache preview, List<BlockPos> checkpoints, int checkpointIndex) {
      AiVisualization.OverlayLayer l = layerFor(owner);
      if (l != null && preview != null && settings.visualizePaths && (l.pathSource != preview || l.lastCheckpointIndex != checkpointIndex)) {
         l.pathSource = preview;
         l.pathIndex = 0;
         l.lastScriptLength = -1;
         l.lastCheckpointIndex = checkpointIndex;
         List<AiVisualization.RouteSegment> segments = new ArrayList<>();
         List<AiVisualization.Marker> markers = new ArrayList<>();
         buildPreviewVisuals(preview, checkpoints, checkpointIndex, segments, markers);
         l.segments = List.copyOf(segments);
         l.markers = List.copyOf(markers);
         l.ghosts = List.of();
         l.targets = List.of();
      }
   }

   private static boolean isAirMove(autismclient.ai.engine.AiPathfinder.MoveType type) {
      return type == autismclient.ai.engine.AiPathfinder.MoveType.JUMP
         || type == autismclient.ai.engine.AiPathfinder.MoveType.SPRINT_JUMP
         || type == autismclient.ai.engine.AiPathfinder.MoveType.JUMP_GAP_2
         || type == autismclient.ai.engine.AiPathfinder.MoveType.JUMP_GAP_3
         || type == autismclient.ai.engine.AiPathfinder.MoveType.PARKOUR_ASCEND
         || type == autismclient.ai.engine.AiPathfinder.MoveType.LADDER_JUMP
         || type == autismclient.ai.engine.AiPathfinder.MoveType.SLIME_BOUNCE;
   }

   static void buildPathVisuals(
      List<autismclient.ai.engine.AiPathfinder.PathStep> path, int index, List<AiVisualization.RouteSegment> segments, List<AiVisualization.Marker> markers
   ) {
      if (path != null && !path.isEmpty()) {
         int startIdx = Math.max(0, index);
         int endIdx = Math.min(path.size(), startIdx + 192);
         if (startIdx < endIdx) {
            ArrayList<Vec3> points = new ArrayList<>(endIdx - startIdx + 1);
            ArrayList<Boolean> airFlags = new ArrayList<>(endIdx - startIdx);
            points.add(Vec3.atBottomCenterOf(path.get(startIdx).from()).add(0.0, 0.08, 0.0));

            for (int i = startIdx; i < endIdx; i++) {
               autismclient.ai.engine.AiPathfinder.PathStep m = path.get(i);
               Vec3 to = Vec3.atBottomCenterOf(m.to()).add(0.0, 0.08, 0.0);
               boolean air = isAirMove(m.type());
               points.add(to);
               airFlags.add(air);
               if (air && markers.size() < 31) {
                  markers.add(new AiVisualization.Marker(to.add(0.0, -0.04, 0.0), 0.22F, -1537414444, 1.25F));
               }
            }

            if (markers.size() < 32) {
               Vec3 goal = points.getLast();
               markers.add(new AiVisualization.Marker(goal.add(0.0, -0.04, 0.0), 0.34F, -329780571, 1.75F));
            }

            buildRouteSegments(points, airFlags, segments);
         }
      }
   }

   static void buildPreviewVisuals(
      autismclient.ai.engine.AiPaceScript.Inner_JsonCache preview,
      List<BlockPos> checkpoints,
      int checkpointIndex,
      List<AiVisualization.RouteSegment> segments,
      List<AiVisualization.Marker> markers
   ) {
      double[] xs = preview.x();
      double[] ys = preview.y();
      double[] zs = preview.z();
      if (xs.length >= 2) {
         int startIdx = checkpointIndex > 0 && checkpointIndex - 1 < preview.checkpointPoint().length ? preview.checkpointPoint()[checkpointIndex - 1] : 0;
         int endIdx = Math.min(xs.length, startIdx + 192);
         ArrayList<Vec3> points = new ArrayList<>(endIdx - startIdx);
         ArrayList<Boolean> airFlags = new ArrayList<>(Math.max(0, endIdx - startIdx - 1));

         for (int i = startIdx; i < endIdx; i++) {
            Vec3 pt = new Vec3(xs[i], ys[i] + 0.08, zs[i]);
            if (i > startIdx) {
               Vec3 prev = points.getLast();
               double horiz = Math.hypot(pt.x - prev.x, pt.z - prev.z);
               boolean air = preview.free()[i] || !preview.climb()[i] && horiz > 1.55;
               airFlags.add(air);
            }

            points.add(pt);
         }

         buildRouteSegments(points, airFlags, segments);
         addCheckpointMarkers(checkpoints, checkpointIndex, markers);
      }
   }

   static void buildScriptVisuals(
      autismclient.ai.engine.AiPaceScript.Inner_IoHelper script,
      int tickIndex,
      List<BlockPos> checkpoints,
      int checkpointIndex,
      List<AiVisualization.RouteSegment> segments,
      List<AiVisualization.Marker> markers
   ) {
      int total = script.size();
      int startIdx = Math.max(0, tickIndex);
      if (startIdx < total) {
         int endIdx = Math.min(total, startIdx + 220);
         ArrayList<Vec3> points = new ArrayList<>(Math.min(220, endIdx - startIdx));
         ArrayList<Boolean> airFlags = new ArrayList<>(Math.min(220, endIdx - startIdx));
         autismclient.ai.engine.AiPhysics.MotionState prev = script.putAt_TokenCore_TableCac_I(startIdx);
         if (prev != null) {
            points.add(new Vec3(prev.x(), prev.y() + (prev.onGround() ? 0.06 : 0.04), prev.z()));
            int airTicks = prev.onGround() ? 0 : 1;
            int skipped = 0;

            for (int i = startIdx + 1; i < endIdx; i++) {
               autismclient.ai.engine.AiPhysics.MotionState cur = script.putAt_TokenCore_TableCac_I(i);
               if (cur == null) {
                  break;
               }

               boolean air = !cur.onGround() || cur.vy() > 0.1;
               Vec3 pt = new Vec3(cur.x(), cur.y() + (air ? 0.04 : 0.06), cur.z());
               if (!air && airTicks >= 3 && markers.size() < 28) {
                  markers.add(new AiVisualization.Marker(new Vec3(cur.x(), cur.y() + 0.035, cur.z()), 0.22F, -1470305580, 1.25F));
               }

               airTicks = air ? airTicks + 1 : 0;
               boolean keep = air || i == endIdx - 1 || skipped >= 2 || !prev.onGround();
               if (!keep && points.size() >= 2) {
                  Vec3 p0 = points.get(points.size() - 2);
                  Vec3 p1 = points.getLast();
                  Vec3 v0 = p1.subtract(p0);
                  Vec3 v1 = pt.subtract(p1);
                  if (v0.lengthSqr() > 1.0E-6 && v1.lengthSqr() > 1.0E-6) {
                     double dot = v0.normalize().dot(v1.normalize());
                     keep = dot < 0.996 || Math.abs(pt.y - p1.y) > 0.015;
                  }
               }

               if (keep && pt.distanceToSqr(points.getLast()) > 1.0E-4) {
                  points.add(pt);
                  airFlags.add(air || !prev.onGround());
                  skipped = 0;
               } else {
                  skipped++;
               }

               prev = cur;
            }

            if (points.size() >= 2) {
               int segCount = Math.min(points.size() - 1, 384);
               double[] cum = new double[segCount + 1];

               for (int i = 0; i < segCount; i++) {
                  cum[i + 1] = cum[i] + points.get(i).distanceTo(points.get(i + 1));
               }

               double totalDist = Math.max(cum[segCount], 0.001);

               for (int i = 0; i < segCount; i++) {
                  float t0 = (float)(cum[i] / totalDist);
                  float t1 = (float)(cum[i + 1] / totalDist);
                  segments.add(new AiVisualization.RouteSegment(points.get(i), points.get(i + 1), t0, t1, airFlags.get(i), cum[i] < 2.2));
               }
            }

            addCheckpointMarkers(checkpoints, checkpointIndex, markers);
         }
      }
   }

   private static void addCheckpointMarkers(List<BlockPos> checkpoints, int checkpointIndex, List<AiVisualization.Marker> markers) {
      if (checkpoints != null && !checkpoints.isEmpty()) {
         int startIdx = Math.max(0, checkpointIndex);

         for (int i = startIdx; i < checkpoints.size() && markers.size() < 32; i++) {
            BlockPos cp = checkpoints.get(i);
            boolean finish = i == checkpoints.size() - 1;
            int color = finish ? -329780571 : (i == startIdx ? -318779034 : -1199777537);
            float radius = finish ? 0.38F : 0.32F;
            markers.add(new AiVisualization.Marker(new Vec3(cp.getX() + 0.5, cp.getY() + 0.04, cp.getZ() + 0.5), radius, color, finish ? 1.85F : 1.55F));
         }
      }
   }

   private static void buildRouteSegments(List<Vec3> points, List<Boolean> airFlags, List<AiVisualization.RouteSegment> segments) {
      int n = points.size();
      if (n >= 2) {
         ArrayList<Vec3> smooth = new ArrayList<>(Math.min(384, n * 3));
         ArrayList<Boolean> smoothAir = new ArrayList<>(Math.min(384, n * 3));
         smooth.add(points.getFirst());
         Vec3 cursor = points.getFirst();

         for (int i = 0; i < n - 1 && smooth.size() < 384; i++) {
            Vec3 a = cursor;
            Vec3 b = points.get(i + 1);
            boolean air = i < airFlags.size() && airFlags.get(i);
            if (air) {
               double horiz = Math.hypot(b.x - a.x, b.z - a.z);
               double apex = Math.clamp(0.28 + 0.14 * horiz, 0.34, 0.92);
               int steps = horiz > 2.2 ? 6 : 4;

               for (int s = 1; s <= steps && smooth.size() < 384; s++) {
                  double t = (double)s / steps;
                  double lift = 4.0 * apex * t * (1.0 - t);
                  smooth.add(new Vec3(a.x + (b.x - a.x) * t, a.y + (b.y - a.y) * t + lift, a.z + (b.z - a.z) * t));
                  smoothAir.add(true);
               }

               cursor = b;
            } else {
               boolean hasNextGround = i + 1 < n - 1 && (i + 1 >= airFlags.size() || !airFlags.get(i + 1));
               Vec3 pre = null;
               Vec3 post = null;
               if (hasNextGround) {
                  Vec3 c = points.get(i + 2);
                  Vec3 v0 = b.subtract(a);
                  Vec3 v1 = c.subtract(b);
                  double l0 = v0.length();
                  double l1 = v1.length();
                  if (l0 > 0.25 && l1 > 0.25 && Math.abs(v0.y) < 0.35 && Math.abs(v1.y) < 0.35) {
                     double cos = v0.dot(v1) / (l0 * l1);
                     if (cos < 0.975 && cos > -0.85) {
                        double trim = Math.min(0.34, Math.min(l0, l1) * 0.38);
                        pre = b.subtract(v0.scale(trim / l0));
                        post = b.add(v1.scale(trim / l1));
                     }
                  }
               }

               if (pre != null) {
                  if (pre.distanceToSqr(smooth.getLast()) > 1.0E-4) {
                     smooth.add(pre);
                     smoothAir.add(false);
                  }

                  for (int s = 1; s <= 3 && smooth.size() < 384; s++) {
                     double t = s / 3.0;
                     double u = 1.0 - t;
                     smooth.add(
                        new Vec3(
                           u * u * pre.x + 2.0 * u * t * b.x + t * t * post.x,
                           u * u * pre.y + 2.0 * u * t * b.y + t * t * post.y,
                           u * u * pre.z + 2.0 * u * t * b.z + t * t * post.z
                        )
                     );
                     smoothAir.add(false);
                  }

                  cursor = post;
               } else {
                  if (b.distanceToSqr(smooth.getLast()) > 1.0E-4) {
                     smooth.add(b);
                     smoothAir.add(false);
                  }

                  cursor = b;
               }
            }
         }

         int segCount = Math.min(smooth.size() - 1, 384);
         if (segCount > 0) {
            double[] cum = new double[segCount + 1];

            for (int i = 0; i < segCount; i++) {
               cum[i + 1] = cum[i] + smooth.get(i).distanceTo(smooth.get(i + 1));
            }

            double totalDist = Math.max(cum[segCount], 0.001);

            for (int i = 0; i < segCount; i++) {
               float t0 = (float)(cum[i] / totalDist);
               float t1 = (float)(cum[i + 1] / totalDist);
               segments.add(new AiVisualization.RouteSegment(smooth.get(i), smooth.get(i + 1), t0, t1, smoothAir.get(i), cum[i] < 2.2));
            }
         }
      }
   }

   static int routeColor(float t, boolean air) {
      float clamped = Math.clamp(t, 0.0F, 1.0F);
      if (air) {
         return clamped < 0.35F ? lerpColor(-154731009, -361566977, clamped / 0.35F) : lerpColor(-361566977, 1279697650, (clamped - 0.35F) / 0.65F);
      } else if (clamped < 0.18F) {
         return lerpColor(-186449928, -346232108, clamped / 0.18F);
      } else {
         return clamped < 0.58F ? lerpColor(-346232108, -766853387, (clamped - 0.18F) / 0.4F) : lerpColor(-766853387, 943231717, (clamped - 0.58F) / 0.42F);
      }
   }

   static int haloColor(float t) {
      return lerpColor(1980371744, 436867872, Math.clamp(t, 0.0F, 1.0F));
   }

   static int lerpColor(int a, int b, float t) {
      float clamped = Math.clamp(t, 0.0F, 1.0F);
      int alpha = Math.round((a >>> 24 & 0xFF) + ((b >>> 24 & 0xFF) - (a >>> 24 & 0xFF)) * clamped);
      int red = Math.round((a >>> 16 & 0xFF) + ((b >>> 16 & 0xFF) - (a >>> 16 & 0xFF)) * clamped);
      int green = Math.round((a >>> 8 & 0xFF) + ((b >>> 8 & 0xFF) - (a >>> 8 & 0xFF)) * clamped);
      int blue = Math.round((a & 0xFF) + ((b & 0xFF) - (a & 0xFF)) * clamped);
      return alpha << 24 | red << 16 | green << 8 | blue;
   }

   public static void showTargets(Object owner, List<BlockPos> targets) {
      AiVisualization.OverlayLayer l = layerFor(owner);
      if (l != null && settings.visualizeTargets && l.touchedNanos - l.targetUpdateNanos >= 200000000L) {
         l.targetUpdateNanos = l.touchedNanos;
         l.priority = 40;
         List<AiVisualization.GhostBlock> out = new ArrayList<>();

         for (BlockPos p : targets) {
            if (out.size() >= 64) {
               break;
            }

            if (AiTerrain.isLoaded(l.level, p)) {
               BlockState state = l.level.getBlockState(p);
               if (!state.isAir()) {
                  out.add(ghost(p, state, false));
               }
            }
         }

         l.targets = List.copyOf(out);
         l.activityStatus = out.isEmpty()
            ? AiTaskStatus.Inner_StreamUtil.EMPTY
            : new AiTaskStatus.Inner_StreamUtil("Mining", "Mining " + out.getFirst().state.getBlock().getName().getString(), "");
      }
   }

   public static void showTarget(Object owner, BlockPos target) {
      AiVisualization.OverlayLayer l = layerFor(owner);
      if (l != null && settings.visualizeTargets && AiTerrain.isLoaded(l.level, target)) {
         BlockState state = l.level.getBlockState(target);
         l.hasTarget = true;
         l.priority = 30;
         l.targets = state.isAir() ? List.of() : List.of(ghost(target, state, true));
         l.activityStatus = state.isAir()
            ? AiTaskStatus.Inner_StreamUtil.EMPTY
            : new AiTaskStatus.Inner_StreamUtil("Mining", "Breaking " + state.getBlock().getName().getString(), "");
      }
   }

   public static void showPlacement(Object owner, BlockPos target, BlockState state) {
      AiVisualization.OverlayLayer l = layerFor(owner);
      if (l != null && state != null && settings.visualizeTargets) {
         l.hasTarget = true;
         if (l.touchedNanos - l.targetUpdateNanos >= 200000000L) {
            l.targetUpdateNanos = l.touchedNanos;
            l.priority = 30;
            l.activityStatus = new AiTaskStatus.Inner_StreamUtil("Building", "Placing " + state.getBlock().getName().getString(), "");
            l.targets = List.of(
               coloredGhost(
                  target,
                  state,
                  cellColor(
                     state.equals(l.level.getBlockState(target)),
                     l.level.getBlockState(target).canBeReplaced(),
                     AiTerrain.isLoaded(l.level, target)
                  )
               )
            );
         }
      }
   }

   static int cellColor(boolean satisfied, boolean replaceable, boolean loaded) {
      return !loaded ? -885830 : (satisfied ? -12067963 : (replaceable ? -11093505 : -20923));
   }

   private static AiVisualization.GhostBlock coloredGhost(BlockPos p, BlockState state, int color) {
      return new AiVisualization.GhostBlock(p.immutable(), state, color, state.isSolidRender(), false, false);
   }

   private static AiVisualization.GhostBlock ghost(BlockPos p, BlockState state, boolean active) {
      return new AiVisualization.GhostBlock(p.immutable(), state, active ? -610078735 : 1885197528, false, true, active);
   }

   static void showPlan(Object owner, BlockPos origin, List<AiBuilder.CellSpec> cells, int cursor, boolean preview) {
      showPlan(owner, origin, cells, cursor, preview, preview);
   }

   static void showCells(Object owner, BlockPos origin, List<AiBuilder.CellSpec> cells, Predicate<BlockPos> hiddenSolid) {
      AiVisualization.OverlayLayer l = layerFor(owner);
      if (l != null) {
         l.hiddenSolid = hiddenSolid;
         showPlan(owner, origin, cells, 0, true, true);
      }
   }

   static void touch(Object owner, long now) {
      AiVisualization.OverlayLayer l = layers.get(owner);
      if (l != null) {
         l.touchedNanos = now;
      }
   }

   static void showPlan(Object owner, BlockPos origin, List<AiBuilder.CellSpec> cells, int cursor, boolean preview, boolean fullPlan) {
      AiVisualization.OverlayLayer l = layerFor(owner);
      if (l != null && settings.visualizeGhosts && l.touchedNanos - l.targetUpdateNanos >= 200000000L) {
         l.targetUpdateNanos = l.touchedNanos;
         l.fullPlan = fullPlan;
         l.priority = preview ? 60 : 50;
         if (fullPlan) {
            updatePlanMesh(l, origin, cells, cursor, preview);
         } else {
            l.closeMesh();
            int cap = preview ? cells.size() : clampGhostLimit(settings.visualizationBlocks);
            int stride = preview ? 1 : (fullPlan ? Math.max(1, (cells.size() + cap - 1) / cap) : 1);
            l.activityStatus = preview
               ? new AiTaskStatus.Inner_StreamUtil("Schematic preview", cells.size() + " planned blocks", "Blue: place / Amber: obstruction / Green: complete")
               : AiTaskStatus.progressStatus("Building", "Following the build plan", cursor, cells.size());
            List<AiVisualization.GhostBlock> out = new ArrayList<>();
            int x = 0;
            int y = 0;
            int z = 0;

            for (AiBuilder.CellSpec cell : cells) {
               x = Math.max(x, cell.dx() + 1);
               y = Math.max(y, cell.dy() + 1);
               z = Math.max(z, cell.dz() + 1);
            }

            l.planBounds = cells.isEmpty()
               ? null
               : new AABB(origin.getX(), origin.getY(), origin.getZ(), origin.getX() + x, origin.getY() + y, origin.getZ() + z);

            for (int i = fullPlan ? 0 : Math.max(0, cursor - 32); i < cells.size() && out.size() < cap; i += stride) {
               AiBuilder.CellSpec c = cells.get(i);
               BlockPos at = origin.offset(c.dx(), c.dy(), c.dz());
               boolean loaded = AiTerrain.isLoaded(l.level, at);
               BlockState live = loaded ? l.level.getBlockState(at) : Blocks.AIR.defaultBlockState();
               boolean satisfied = loaded && AiBuilder.matchesSchematicCell(c, live);
               int color = cellColor(satisfied, live.canBeReplaced(), loaded);
               if (fullPlan
                  && loaded
                  && !satisfied
                  && !live.isAir()
                  && (live.hasBlockEntity() || live.getDestroySpeed(l.level, at) < 0.0F || !live.getFluidState().isEmpty() || !AiSchematicActions.isNaturalTerrain(live))) {
                  color = -885830;
               }

               out.add(coloredGhost(at, AiBuilder.toBlockState(c), color));
            }

            l.ghosts = List.copyOf(out);
         }
      }
   }

   private static void updatePlanMesh(AiVisualization.OverlayLayer l, BlockPos origin, List<AiBuilder.CellSpec> cells, int cursor, boolean preview) {
      if (l.planMesh == null || !l.planMesh.matches(origin, cells, preview)) {
         l.closeMesh();
         l.planMesh = new AiPlanMesh(origin, cells, preview, l.hiddenSolid);
      }

      if (preview) {
         l.planMesh.setOrigin(origin);
      }

      l.planMesh.setMoving(l.moving);
      l.planMesh.refreshFromWorld(l.level);
      if (!l.planMesh.isReady()) {
         l.activityStatus = AiTaskStatus.progressStatus(
            preview ? "Preparing schematic preview" : "Preparing build plan", "Reading every planned block", l.planMesh.preparedCount(), cells.size()
         );
         l.planBounds = null;
         l.ghosts = List.of();
      } else {
         l.activityStatus = preview
            ? new AiTaskStatus.Inner_StreamUtil("Schematic preview", cells.size() + " planned blocks", "Blue: place / Amber: obstruction / Green: complete")
            : AiTaskStatus.progressStatus("Building", "Following the build plan", cursor, cells.size());
         l.planBounds = cells.isEmpty()
            ? null
            : new AABB(
               origin.getX(),
               origin.getY(),
               origin.getZ(),
               origin.getX() + l.planMesh.sizeX,
               origin.getY() + l.planMesh.sizeY,
               origin.getZ() + l.planMesh.sizeZ
            );
         Minecraft mc = Minecraft.getInstance();
         Vec3 eye = mc.player == null ? Vec3.atCenterOf(origin) : mc.player.getEyePosition();
         int outlines = preview ? 0 : 12;
         List<Integer> nearest = l.planMesh.nearest(eye, outlines, 4096, System.nanoTime() + 2000000L);
         List<AiVisualization.GhostBlock> out = new ArrayList<>(nearest.size());

         for (int i : nearest) {
            if (l.planMesh.colorAt(i) != -12067963) {
               BlockState state = l.planMesh.stateAt(i);
               out.add(new AiVisualization.GhostBlock(l.planMesh.posAt(i), state, l.planMesh.colorAt(i), state.isSolidRender(), false, false, true));
            }
         }

         l.ghosts = List.copyOf(out);
      }
   }

   static void refreshTargets(Object owner) {
      AiVisualization.OverlayLayer l = layers.get(owner);
      if (l != null) {
         l.targetUpdateNanos = 0L;
      }
   }

   static void setMoving(Object owner, boolean moving) {
      AiVisualization.OverlayLayer l = layerFor(owner);
      if (l != null) {
         l.moving = moving;
         if (l.planMesh != null) {
            l.planMesh.setMoving(moving);
         }
      }
   }

   static int clampGhostLimit(int value) {
      return Math.max(64, Math.min(4096, value));
   }

   static int clampPercent(int value) {
      return Math.max(0, Math.min(100, value));
   }

   static void setGhostLimit(int value) {
      opacityPreview = clampPercent(value);
      opacityPreviewUntil = System.nanoTime() + 2000000000L;
   }

   private static void collectFrame(Vec3 camera) {
      long started = System.nanoTime();
      Minecraft mc = Minecraft.getInstance();
      if ((!layers.isEmpty() || aP.init_cU() != null) && mc.level != null && !autismclient.modules.aD.init_Z() && autismclient.e.ListPool.init()) {
         int cap = clampGhostLimit(settings.visualizationBlocks);
         int distance = Math.max(16, Math.min(256, settings.visualizationDistance));
         double range = (double)distance * distance;
         Map<Long, AiVisualization.GhostBlock> ghosts = new LinkedHashMap<>();
         Map<Long, AiVisualization.GhostBlock> targets = new LinkedHashMap<>();
         List<AiVisualization.RouteSegment> route = new ArrayList<>();
         List<AiVisualization.Marker> markers = new ArrayList<>();
         AABB bounds = null;
         List<AiPlanMesh> plans = new ArrayList<>();
         List<AiTaskStatus.Inner_DataUtil> statuses = new ArrayList<>();
         long now = System.nanoTime();
         ArrayList<AiVisualization.OverlayLayer> activeLayers = new ArrayList<>(layers.values());
         activeLayers.sort(Comparator.comparingLong(l -> l.creationOrder));

         for (AiVisualization.OverlayLayer l : activeLayers) {
            if (l.level == mc.level && now - l.touchedNanos <= 450000000L) {
               if (settings.visualizeGhosts && l.planMesh != null) {
                  plans.add(l.planMesh);
               }

               if (settings.visualizeGhosts) {
                  for (AiVisualization.GhostBlock g : l.ghosts) {
                     if ((l.fullPlan || g.pos.distToCenterSqr(camera) <= range) && (ghosts.size() < cap || ghosts.containsKey(g.pos.asLong()))) {
                        if (l.fullPlan) {
                           ghosts.put(g.pos.asLong(), g);
                        } else {
                           ghosts.putIfAbsent(g.pos.asLong(), g);
                        }
                     }
                  }
               }

               if (settings.visualizeTargets) {
                  for (AiVisualization.GhostBlock g : l.targets) {
                     if (g.pos.distToCenterSqr(camera) <= range) {
                        AiVisualization.GhostBlock previous = targets.get(g.pos.asLong());
                        if (previous == null || g.active) {
                           targets.put(g.pos.asLong(), g);
                        }
                     }
                  }
               }

               if (settings.visualizePaths) {
                  for (AiVisualization.RouteSegment s : l.segments) {
                     if (route.size() < 384 && Math.min(s.a.distanceToSqr(camera), s.b.distanceToSqr(camera)) <= range) {
                        route.add(s);
                     }
                  }

                  for (AiVisualization.Marker m : l.markers) {
                     if (markers.size() < 32 && m.pos.distanceToSqr(camera) <= range) {
                        markers.add(m);
                     }
                  }
               }

               if (l.planBounds != null && (l.fullPlan || bounds == null)) {
                  bounds = l.planBounds;
               }

               boolean visible = settings.visualizeGhosts && (!l.ghosts.isEmpty() || l.planBounds != null)
                  || settings.visualizeTargets && !l.targets.isEmpty()
                  || settings.visualizePaths && !l.segments.isEmpty();
               if (visible) {
                  statuses.add(new AiTaskStatus.Inner_DataUtil(l.activityStatus, l.priority, l.creationOrder));
               }

               if (l.navStatus.visible()) {
                  statuses.add(new AiTaskStatus.Inner_DataUtil(l.navStatus, l.navPriority, l.navOrder));
               }

               if (l.taskStatus.visible()) {
                  statuses.add(new AiTaskStatus.Inner_DataUtil(l.taskStatus, 100, l.statusOrder, l.immediateStatus));
               }

               if (l.content.visible()) {
                  statuses.add(new AiTaskStatus.Inner_DataUtil(l.content, 200, l.contentOrder));
               }
            }
         }

         if (settings.visualizeGhosts) {
            for (AiVisualization.GhostBlock g : targets.values()) {
               if (!g.mining && (ghosts.size() < cap || ghosts.containsKey(g.pos.asLong()))) {
                  ghosts.put(g.pos.asLong(), g);
               }
            }
         }

         ArrayList<AiVisualization.GhostBlock> sorted = new ArrayList<>(ghosts.values());
         sorted.sort(Comparator.<AiVisualization.GhostBlock>comparingDouble(g -> g.pos.distToCenterSqr(camera)).reversed());
         Vec3 body = mc.player == null ? camera : mc.player.getEyePosition();
         List<AiVisualization.GhostBlock> quietTargets = selectQuietTargets(targets.values(), body);
         frame = new AiVisualization.RenderFrame(
            mc.level, camera, List.copyOf(sorted), quietTargets, List.copyOf(route), List.copyOf(markers), bounds, AiTaskStatus.pickStatus(statuses, now), List.copyOf(plans)
         );
         hudFrame = frame;
         statCollectNanos = System.nanoTime() - started;
      } else {
         frame = AiVisualization.RenderFrame.EMPTY;
         hudFrame = AiVisualization.RenderFrame.EMPTY;
         statHudWidth = 0;
      }
   }

   static List<AiVisualization.GhostBlock> selectQuietTargets(Collection<AiVisualization.GhostBlock> targets, Vec3 body) {
      ArrayList<AiVisualization.GhostBlock> targetView = new ArrayList<>(targets);
      targetView.sort(
         Comparator.<AiVisualization.GhostBlock, Boolean>comparing(gx -> !gx.active)
            .thenComparingDouble(gx -> gx.pos.distToCenterSqr(body))
            .thenComparingLong(gx -> gx.pos.asLong())
      );
      List<AiVisualization.GhostBlock> quietTargets = new ArrayList<>();
      int miningCount = 0;

      for (AiVisualization.GhostBlock g : targetView) {
         if (!g.mining || (g.active || !(g.pos.distToCenterSqr(body) > 64.0)) && miningCount < 8) {
            if (g.mining) {
               miningCount++;
            }

            quietTargets.add(g);
            if (quietTargets.size() >= 64) {
               break;
            }
         }
      }

      return List.copyOf(quietTargets);
   }

   public static boolean hasContent() {
      return frame.world != null
         && (
            !frame.ghosts.isEmpty()
               || !frame.route.isEmpty()
               || !frame.targets.isEmpty()
               || !frame.plans.isEmpty()
               || settings.visualizeGhosts && frame.bounds != null
               || aP.init_cU() != null
         );
   }

   public static void render(PoseStack matrices) {
      AiVisualization.RenderFrame f = frame;
      frame = AiVisualization.RenderFrame.EMPTY;
      Minecraft mc = Minecraft.getInstance();
      if (f.world != null && f.world == mc.level) {
         long started = System.nanoTime();
         Object currentModels = mc.getModelManager().getBlockStateModelSet();
         if (lastModelSet != currentModels) {
            ghostModelCache.clear();
            lastModelSet = currentModels;
         }

         AutismBufferSource source = ghostBufferAllocator.init();
         Pose pose = matrices.last();
         int alpha = (opacityPreview >= 0 && System.nanoTime() < opacityPreviewUntil ? opacityPreview : clampPercent(settings.ghostOpacity)) * 255 / 100;
         int quads = 0;
         int baked = 0;
         int planCells = 0;
         if (alpha > 0) {
            for (AiPlanMesh plan : f.plans) {
               try {
                  plan.render(pose, f.camera, alpha, 512.0, started + 4000000L, f.world);
                  quads += plan.quadCount;
                  planCells += plan.cellCount;
               } catch (Throwable error) {
                  AutismClientAddon.LOG.warn("[AI] plan mesh draw failed; the plan stays outlined only", error);
                  plan.close();
               }
            }
         }

         try {
            VertexConsumer texture = source.buffer(autismclient.render.AutismRenderTypes.aiGhost());
            Set<Long> solid = new HashSet<>();

            for (AiVisualization.GhostBlock g : f.ghosts) {
               if (g.fullCube) {
                  solid.add(g.pos.asLong());
               }
            }

            if (alpha > 0) {
               for (AiVisualization.GhostBlock g : f.ghosts) {
                  if (!g.mining && !g.planned && !g.state.isAir()) {
                     autismclient.util.oresim.OreGhostModelCache.Inner_StreamUtil model = ghostModelCache.get(g.state);
                     if (model == null && baked < 8 && System.nanoTime() - started < 2000000L) {
                        model = autismclient.util.oresim.OreGhostModelCache.init(g.state);
                        if (model.faces().isEmpty()) {
                           model = spriteGhostModel(g.state);
                        }

                        if (ghostModelCache.size() >= 2048) {
                           ghostModelCache.clear();
                        }

                        ghostModelCache.put(g.state, model);
                        baked++;
                     }

                     if (model != null) {
                        BlockPos p = g.pos;
                        double ox = p.getX() - f.camera.x;
                        double oy = p.getY() - f.camera.y;
                        double oz = p.getZ() - f.camera.z;

                        for (autismclient.util.oresim.OreGhostModelCache.Inner_DataUtil face : model.faces()) {
                           if (quads >= 16384) {
                              break;
                           }

                           Direction d = face.cullFace();
                           if (d == null
                              || !solid.contains(p.relative(d).asLong())
                                 && !(
                                    (f.camera.x - p.getX() - 0.5) * d.getStepX()
                                          + (f.camera.y - p.getY() - 0.5) * d.getStepY()
                                          + (f.camera.z - p.getZ() - 0.5) * d.getStepZ()
                                       <= 0.0
                                 )) {
                              BlockTintSource tintSource = face.tintIndex() < 0 ? null : mc.getBlockColors().getTintSource(g.state, face.tintIndex());
                              int tint = tintSource == null
                                 ? 16777215
                                 : (g.color == -885830 ? tintSource.color(g.state) : tintSource.colorInWorld(g.state, f.world, p));
                              float shade = face.load() == null ? 1.0F : f.world.cardinalLighting().byFace(face.load());
                              int color = ghostColor(g.color, tint, shade, alpha);
                              float[] data = face.vertices();

                              for (int v = 0; v < 4; v++) {
                                 int k = v * 5;
                                 texture.addVertex(pose, (float)(ox + data[k]), (float)(oy + data[k + 1]), (float)(oz + data[k + 2]))
                                    .setUv(data[k + 3], data[k + 4])
                                    .setColor(color);
                              }

                              quads++;
                           }
                        }
                     }
                  }
               }
            }

            VertexConsumer lines = source.buffer(autismclient.render.AutismRenderTypes.aiPlanLines());
            Vec3 offset = f.camera.scale(-1.0);
            SchematicSelection selection = aP.init_cU();
            if (selection != null) {
               SchematicCapture.Inner_DataUtil area = selection.getArea();
               if (area != null) {
                  autismclient.util.WorldLineRenderer.box(pose, lines, new AABB(area.min()).minmax(new AABB(area.max())).move(offset), -1336493569, 1.3F);
               }

               if (selection.cornerA != null) {
                  autismclient.util.WorldLineRenderer.box(pose, lines, new AABB(selection.cornerA).move(offset), -12067963, 1.8F);
               }

               if (selection.cornerB != null) {
                  autismclient.util.WorldLineRenderer.box(pose, lines, new AABB(selection.cornerB).move(offset), -20923, 1.8F);
               }

               if (selection.aimPos != null) {
                  autismclient.util.WorldLineRenderer.box(pose, lines, new AABB(selection.aimPos).move(offset).inflate(0.008), -11093505, 2.0F);
               }
            }

            if (settings.visualizeGhosts) {
               for (int i = Math.max(0, f.ghosts.size() - 192); i < f.ghosts.size(); i++) {
                  AiVisualization.GhostBlock g = f.ghosts.get(i);
                  autismclient.util.WorldLineRenderer.box(pose, lines, new AABB(g.pos).move(offset).deflate(0.006), -1342177280 | g.color & 16777215, 1.1F);
               }
            }

            if (f.bounds != null && settings.visualizeGhosts) {
               autismclient.util.WorldLineRenderer.box(pose, lines, f.bounds.move(offset), -1873364481, 1.6F);
            }

            for (AiVisualization.RouteSegment s : f.route) {
               float tm = (s.t0 + s.t1) * 0.5F;
               float w = (s.active ? 3.8F : 3.4F) - 1.7F * tm;
               autismclient.util.WorldLineRenderer.line(
                  pose, lines, s.a.x + offset.x, s.a.y + offset.y, s.a.z + offset.z, s.b.x + offset.x, s.b.y + offset.y, s.b.z + offset.z, haloColor(s.t0), haloColor(s.t1), w
               );
            }

            double nextArrow = 1.8;
            double traveled = 0.0;

            for (AiVisualization.RouteSegment s : f.route) {
               Vec3 a = s.a.add(offset);
               Vec3 b = s.b.add(offset);
               float tm = (s.t0 + s.t1) * 0.5F;
               float w = (s.active ? 2.4F : 2.15F) - 1.05F * tm;
               int c0 = routeColor(s.t0, s.air);
               int c1 = routeColor(s.t1, s.air);
               autismclient.util.WorldLineRenderer.line(pose, lines, a.x, a.y, a.z, b.x, b.y, b.z, c0, c1, w);
               double segLen = a.distanceTo(b);
               if (traveled + segLen >= nextArrow && tm < 0.88F) {
                  Vec3 heading = b.subtract(a);
                  double length = heading.horizontalDistance();
                  if (length > 0.08) {
                     double dx = heading.x / length;
                     double dz = heading.z / length;
                     int arrowColor = lerpColor(-253296647, 1145218798, tm);
                     float arrowWidth = 1.85F - 0.7F * tm;
                     autismclient.util.WorldLineRenderer.line(pose, lines, b.add(-dx * 0.21 - dz * 0.11, 0.0, -dz * 0.21 + dx * 0.11), b, arrowColor, arrowWidth);
                     autismclient.util.WorldLineRenderer.line(pose, lines, b.add(-dx * 0.21 + dz * 0.11, 0.0, -dz * 0.21 - dx * 0.11), b, arrowColor, arrowWidth);
                     nextArrow = traveled + segLen + 5.5;
                  }
               }

               traveled += segLen;
            }

            for (AiVisualization.Marker m : f.markers) {
               Vec3 center = m.pos.add(offset);
               drawCircle(pose, lines, center, m.radius, 1510609696, m.width + 1.1F);
               drawCircle(pose, lines, center, m.radius, m.color, m.width);
            }

            for (AiVisualization.GhostBlock g : f.targets) {
               AABB b = new AABB(g.pos).move(offset).inflate(g.mining ? 0.003 : 0.014);
               if (g.mining) {
                  List<Vec3> corners = faceCorners(b, faceTowardCamera(g.pos, f.camera));

                  for (int i = 0; i < 4; i++) {
                     autismclient.util.WorldLineRenderer.line(pose, lines, corners.get(i), corners.get((i + 1) % 4), g.color, g.active ? 1.45F : 0.9F);
                  }
               } else {
                  autismclient.util.WorldLineRenderer.box(pose, lines, b, g.color, 1.7F);
               }
            }
         } finally {
            source.drawAll();
            statQuads = quads;
            statGhosts = f.ghosts.size() + planCells;
            statSegments = f.route.size();
            statMiningTargets = (int)f.targets.stream().filter(gx -> gx.mining).count();
            statRenderNanos = System.nanoTime() - started;
            statOrange = 0;
            statBlue = 0;
            statGreen = 0;

            for (AiVisualization.GhostBlock g : f.ghosts) {
               if (!g.planned) {
                  if (g.color == -12067963) {
                     statGreen++;
                  } else if (g.color == -11093505) {
                     statBlue++;
                  } else if (g.color == -20923) {
                     statOrange++;
                  }
               }
            }

            for (AiPlanMesh plan : f.plans) {
               statGreen = statGreen + plan.greenCells;
               statBlue = statBlue + plan.blueCells;
               statOrange = statOrange + plan.orangeCells;
            }

            frameCostNanos[frameCostCursor++ % frameCostNanos.length] = statRenderNanos + statCollectNanos;
            frameCostCount = Math.min(frameCostNanos.length, frameCostCount + 1);
         }
      }
   }

   private static void drawCircle(Pose pose, VertexConsumer lines, Vec3 center, float radius, int color, float width) {
      for (int i = 0; i < 12; i++) {
         autismclient.util.WorldLineRenderer.init(
            pose,
            lines,
            center.x + circleCos[i] * radius,
            center.y,
            center.z + circleSin[i] * radius,
            center.x + circleCos[i + 1] * radius,
            center.y,
            center.z + circleSin[i + 1] * radius,
            color,
            width
         );
      }
   }

   static Direction faceTowardCamera(BlockPos pos, Vec3 camera) {
      double x = camera.x - pos.getX() - 0.5;
      double y = camera.y - pos.getY() - 0.5;
      double z = camera.z - pos.getZ() - 0.5;
      if (Math.abs(x) >= Math.abs(y) && Math.abs(x) >= Math.abs(z)) {
         return x >= 0.0 ? Direction.EAST : Direction.WEST;
      } else if (Math.abs(z) >= Math.abs(y)) {
         return z >= 0.0 ? Direction.SOUTH : Direction.NORTH;
      } else {
         return y >= 0.0 ? Direction.UP : Direction.DOWN;
      }
   }

   static List<Vec3> faceCorners(AABB b, Direction face) {
      List var10000;
      switch (face) {
         case EAST:
         case WEST:
            double x = face == Direction.EAST ? b.maxX : b.minX;
            var10000 = List.of(new Vec3(x, b.minY, b.minZ), new Vec3(x, b.maxY, b.minZ), new Vec3(x, b.maxY, b.maxZ), new Vec3(x, b.minY, b.maxZ));
            break;
         case UP:
         case DOWN:
            double y = face == Direction.UP ? b.maxY : b.minY;
            var10000 = List.of(new Vec3(b.minX, y, b.minZ), new Vec3(b.maxX, y, b.minZ), new Vec3(b.maxX, y, b.maxZ), new Vec3(b.minX, y, b.maxZ));
            break;
         case SOUTH:
         case NORTH:
            double z = face == Direction.SOUTH ? b.maxZ : b.minZ;
            var10000 = List.of(new Vec3(b.minX, b.minY, z), new Vec3(b.maxX, b.minY, z), new Vec3(b.maxX, b.maxY, z), new Vec3(b.minX, b.maxY, z));
            break;
         default:
            throw new MatchException(null, null);
      }

      return var10000;
   }

   static int ghostColor(int status, int tint, float shade, int alpha) {
      int r = Math.round(((status >> 16 & 0xFF) * 0.38F + 158.1F) * (tint >> 16 & 0xFF) / 255.0F * shade);
      int g = Math.round(((status >> 8 & 0xFF) * 0.38F + 158.1F) * (tint >> 8 & 0xFF) / 255.0F * shade);
      int b = Math.round(((status & 0xFF) * 0.38F + 158.1F) * (tint & 0xFF) / 255.0F * shade);
      return alpha << 24 | Math.min(255, r) << 16 | Math.min(255, g) << 8 | Math.min(255, b);
   }

   private static autismclient.util.oresim.OreGhostModelCache.Inner_StreamUtil spriteGhostModel(BlockState state) {
      TextureAtlasSprite sprite = Minecraft.getInstance().getModelManager().getBlockStateModelSet().getParticleMaterial(state).sprite();
      List<autismclient.util.oresim.OreGhostModelCache.Inner_DataUtil> faces = new ArrayList<>();

      for (Direction d : Direction.values()) {
         float[] var10000;
         switch (d) {
            case EAST:
               var10000 = new float[]{1.0F, 0.0F, 1.0F, 1.0F, 0.0F, 0.0F, 1.0F, 1.0F, 0.0F, 1.0F, 1.0F, 1.0F};
               break;
            case WEST:
               var10000 = new float[]{0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 1.0F, 0.0F, 1.0F, 1.0F, 0.0F, 1.0F, 0.0F};
               break;
            case UP:
               var10000 = new float[]{0.0F, 1.0F, 1.0F, 1.0F, 1.0F, 1.0F, 1.0F, 1.0F, 0.0F, 0.0F, 1.0F, 0.0F};
               break;
            case DOWN:
               var10000 = new float[]{0.0F, 0.0F, 0.0F, 1.0F, 0.0F, 0.0F, 1.0F, 0.0F, 1.0F, 0.0F, 0.0F, 1.0F};
               break;
            case SOUTH:
               var10000 = new float[]{0.0F, 0.0F, 1.0F, 1.0F, 0.0F, 1.0F, 1.0F, 1.0F, 1.0F, 0.0F, 1.0F, 1.0F};
               break;
            case NORTH:
               var10000 = new float[]{1.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 1.0F, 0.0F, 1.0F, 1.0F, 0.0F};
               break;
            default:
               throw new MatchException(null, null);
         }

         float[] corners = var10000;
         float[] vertices = new float[20];

         for (int i = 0; i < 4; i++) {
            vertices[i * 5] = corners[i * 3];
            vertices[i * 5 + 1] = corners[i * 3 + 1];
            vertices[i * 5 + 2] = corners[i * 3 + 2];
            vertices[i * 5 + 3] = i != 0 && i != 3 ? sprite.getU1() : sprite.getU0();
            vertices[i * 5 + 4] = i < 2 ? sprite.getV1() : sprite.getV0();
         }

         faces.add(new autismclient.util.oresim.OreGhostModelCache.Inner_DataUtil(d, d, vertices, -1));
      }

      return new autismclient.util.oresim.OreGhostModelCache.Inner_StreamUtil(List.copyOf(faces));
   }

   public static JsonObject statsJson() {
      long[] costs = Arrays.copyOf(frameCostNanos, frameCostCount);
      Arrays.sort(costs);
      long now = System.nanoTime();
      List<AiVisualization.OverlayLayer> plans = layers.values()
         .stream()
         .filter(l -> l.fullPlan && l.level == Minecraft.getInstance().level && now - l.touchedNanos <= 450000000L)
         .toList();
      return AiJson.objectOf(
         "ghosts",
         statGhosts,
         "quads",
         statQuads,
         "segments",
         statSegments,
         "renderMicros",
         statRenderNanos / 1000L,
         "fullPlanLayers",
         plans.size(),
         "fullPlanGhosts",
         plans.stream().mapToInt(l -> l.planMesh != null ? l.planMesh.size() : l.ghosts.size()).sum(),
         "buildPlanLayers",
         plans.stream().filter(l -> l.priority == 50).count(),
         "miningTargets",
         statMiningTargets,
         "miningTexturedQuads",
         0,
         "hudTitle",
         hudFrame.status.title(),
         "hudActivity",
         hudFrame.status.activity(),
         "hudDetail",
         hudFrame.status.detail(),
         "hudPercent",
         hudFrame.status.percent(),
         "hudWidth",
         statHudWidth,
         "hudContentWidth",
         statHudContentWidth,
         "blockBudget",
         clampGhostLimit(settings.visualizationBlocks),
         "quadBudget",
         16384,
         "opacity",
         clampPercent(settings.ghostOpacity),
         "green",
         statGreen,
         "blue",
         statBlue,
         "orange",
         statOrange,
         "frames",
         frameCostCount,
         "cpuMedianMicros",
         frameCostCount == 0 ? 0L : costs[frameCostCount / 2] / 1000L,
         "cpuP95Micros",
         frameCostCount == 0 ? 0L : costs[Math.min(frameCostCount - 1, (int)(frameCostCount * 0.95))] / 1000L
      );
   }

   static void resetStats() {
      frameCostCursor = 0;
      frameCostCount = 0;
      Arrays.fill(frameCostNanos, 0L);
      statOrange = 0;
      statBlue = 0;
      statGreen = 0;
      statSegments = 0;
      statQuads = 0;
      statGhosts = 0;
      statMiningTargets = 0;
      statHudContentWidth = 0;
      statHudWidth = 0;
      statCollectNanos = 0L;
      statRenderNanos = 0L;
   }

   public static boolean isHudVisible() {
      if (aP.init_Z()) {
         return true;
      }

      Screen screen = Minecraft.getInstance().gui.screen();
      return (screen == null || screen instanceof AbstractContainerScreen)
         && hudFrame.world == Minecraft.getInstance().level
         && hudFrame.status.visible()
         && !layers.isEmpty();
   }

   public static autismclient.gui.f.NetHelper hudBounds(GuiGraphicsExtractor g) {
      if (aP.init_Z()) {
         return aP.init_NetHelper_GuiGraphicsExtract(g);
      }

      autismclient.gui.f.SyncManager ui = autismclient.gui.f.UiHelper.init(g, Minecraft.getInstance().font, 0, 0);
      AiTaskStatus.Inner_StreamUtil status = hudFrame.status;
      statHudContentWidth = AiTaskStatus.contentWidth(status, ui.init_PoolEntry()::measureWidth, 2147483647);
      int width = AiTaskStatus.contentWidth(status, ui.init_PoolEntry()::measureWidth, Math.min(ui.init_I() - 16, 360));
      statHudWidth = width;
      int height = autismclient.gui.ui.widgets.StatusToastCardRenderer.init(ui, width, status.bannerActivity(), status.bannerDetail(), status.percent());
      autismclient.gui.f.NetHelper bounds = autismclient.gui.f.NetHelper.init(8, 8, width, height);

      for (AutismOverlay overlay : OverlayManager.getInstance().getOverlays()) {
         if (overlay.isVisible() && overlay.getOverlayId().equals("ai-assistant")) {
            OverlayBounds window = overlay.getBounds();
            if (bounds.read() < window.x + window.width
               && bounds.init() > window.x
               && bounds.write() < window.y + window.height
               && bounds.load() > window.y) {
               bounds = autismclient.gui.f.NetHelper.init(Math.max(8, ui.init_I() - width - 8), 8, width, height);
            }
         }
      }

      return bounds;
   }

   public static void renderHud(GuiGraphicsExtractor g) {
      if (aP.init_Z()) {
         aP.init_V_GuiGraphicsExtract(g);
      } else if (isHudVisible()) {
         autismclient.gui.f.SyncManager ui = autismclient.gui.f.UiHelper.init(g, Minecraft.getInstance().font, 0, 0);
         autismclient.gui.f.NetHelper b = hudBounds(g);
         AiTaskStatus.Inner_StreamUtil status = hudFrame.status;
         autismclient.gui.ui.widgets.StatusToastCardRenderer.render(
            ui, b, status.title(), status.bannerActivity(), status.bannerDetail(), ui.init_TableIndex().colors().textPrimary, status.percent()
         );
      }
   }

   record RouteSegment(Vec3 a, Vec3 b, float t0, float t1, boolean air, boolean active) {
   }

   private static final class OverlayLayer {
      ClientLevel level;
      long touchedNanos;
      long targetUpdateNanos;
      long creationOrder;
      long statusOrder;
      long contentOrder;
      List<AiVisualization.GhostBlock> ghosts = List.of();
      List<AiVisualization.GhostBlock> targets = List.of();
      List<AiVisualization.RouteSegment> segments = List.of();
      List<AiVisualization.Marker> markers = List.of();
      Object pathSource;
      int pathIndex = -1;
      int lastScriptLength = -1;
      int lastCheckpointIndex = -1;
      AABB planBounds;
      AiTaskStatus.Inner_StreamUtil activityStatus = AiTaskStatus.Inner_StreamUtil.EMPTY;
      AiTaskStatus.Inner_StreamUtil navStatus = AiTaskStatus.Inner_StreamUtil.EMPTY;
      AiTaskStatus.Inner_StreamUtil taskStatus = AiTaskStatus.Inner_StreamUtil.EMPTY;
      AiTaskStatus.Inner_StreamUtil content = AiTaskStatus.Inner_StreamUtil.EMPTY;
      int priority;
      int navPriority;
      long navOrder;
      boolean fullPlan;
      boolean moving;
      boolean hasTarget;
      boolean immediateStatus;
      AiPlanMesh planMesh;
      Predicate<BlockPos> hiddenSolid;

      void closeMesh() {
         if (this.planMesh != null) {
            this.planMesh.close();
            this.planMesh = null;
         }
      }
   }

   private record RenderFrame(
      ClientLevel world,
      Vec3 camera,
      List<AiVisualization.GhostBlock> ghosts,
      List<AiVisualization.GhostBlock> targets,
      List<AiVisualization.RouteSegment> route,
      List<AiVisualization.Marker> markers,
      AABB bounds,
      AiTaskStatus.Inner_StreamUtil status,
      List<AiPlanMesh> plans
   ) {
      static final AiVisualization.RenderFrame EMPTY = new AiVisualization.RenderFrame(
         null, Vec3.ZERO, List.of(), List.of(), List.of(), List.of(), null, AiTaskStatus.Inner_StreamUtil.EMPTY, List.of()
      );
   }

   record GhostBlock(BlockPos pos, BlockState state, int color, boolean fullCube, boolean mining, boolean active, boolean planned) {
      GhostBlock(BlockPos pos, BlockState state, int color, boolean fullCube, boolean mining, boolean active) {
         this(pos, state, color, fullCube, mining, active, false);
      }
   }

   record Marker(Vec3 pos, float radius, int color, float width) {
   }
}

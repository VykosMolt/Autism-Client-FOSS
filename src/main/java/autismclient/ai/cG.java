package autismclient.ai;

import autismclient.AutismClientAddon;
import autismclient.util.PkgCls_c;
import autismclient.util.bk;
import autismclient.util.cH;
import autismclient.util.cX;
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

public final class cG {
   public static final int init_I = -12067963;
   public static final int load_I = -11093505;
   public static final int read_I = -20923;
   public static final int write_I = -885830;
   public static final int update_I = 1885197528;
   public static final int sync_I = -610078735;
   private static final int flush_I = 192;
   private static final int close = 64;
   private static final int open = 8;
   private static final int poll = 16384;
   private static final long init_J = 450000000L;
   private static final long load_J = 200000000L;
   private static final Map<Object, cG.Inner_CachePool> init_Map = new IdentityHashMap<>();
   private static final Map<BlockState, autismclient.util.oresim.DataUtil.Inner_StreamUtil> load_Map = new IdentityHashMap<>();
   private static final PkgCls_c.Inner_DataUtil init_c_DataUtil = new PkgCls_c.Inner_DataUtil(4194304);
   private static AiSettings init_AiSettings = new AiSettings();
   private static boolean init_Z;
   private static ClientLevel init_ClientLevel;
   private static Object init_Obj;
   private static cG.Inner_DataUtil init_cG_DataUtil = cG.Inner_DataUtil.EMPTY;
   private static cG.Inner_DataUtil load_cG_DataUtil = cG.Inner_DataUtil.EMPTY;
   private static int push;
   private static int pop;
   private static int tick;
   private static int run;
   private static long read_J;
   private static final long[] init_JArr = new long[120];
   private static int get;
   private static int set;
   private static int put;
   private static int add;
   private static int clear;
   private static long write_J;
   private static int reset = -1;
   private static long update_J;
   private static long sync_J;
   private static long flush_J;
   private static int start;
   private static int stop;
   private static int next;

   private cG() {
   }

   public static void init_V() {
      if (!init_Z) {
         init_Z = true;
         ClientTickEvents.END_CLIENT_TICK.register(mc -> {
            if (init_ClientLevel != mc.level) {
               load_V();
               init_ClientLevel = mc.level;
            }

            if (++push % 5 == 0) {
               cc s = cc.load_cc();
               if (s != null) {
                  init_AiSettings = s.init_AiSettings();
               } else if (!init_Map.isEmpty() && System.nanoTime() - sync_J > 5000000000L) {
                  init_AiSettings = o.init_AiSettings();
                  sync_J = System.nanoTime();
               }
            }

            long now = System.nanoTime();
            init_Map.values().removeIf(l -> {
               boolean expired = l.init_ClientLevel != mc.level || now - l.init_J > 450000000L;
               if (expired) {
                  l.init();
               }

               return expired;
            });
         });
         LevelRenderEvents.COLLECT_SUBMITS.register(context -> init(context.levelState().cameraRenderState.pos));
         ScreenEvents.AFTER_INIT.register((mc, screen, width, height) -> {
            if (screen instanceof AbstractContainerScreen) {
               ScreenEvents.afterExtract(screen).register((shown, graphics, mouseX, mouseY, delta) -> {
                  if (load_Z() && !mc.gui.hud.isHidden() && !autismclient.modules.aD.init_Z() && autismclient.e.TokenStore.init()) {
                     graphics.nextStratum();
                     autismclient.util.cA.init_V_GuiGraphicsExtract(graphics);

                     try {
                        init_V(graphics);
                     } finally {
                        autismclient.util.cA.load_V_GuiGraphicsExtract(graphics);
                     }
                  }
               });
            }
         });
      }
   }

   private static cG.Inner_CachePool init_cG_CachePool(Object owner) {
      Minecraft mc = Minecraft.getInstance();
      if (mc.level != null && mc.player != null) {
         cG.Inner_CachePool l = init_Map.computeIfAbsent(owner, k -> {
            cG.Inner_CachePool created = new cG.Inner_CachePool();
            created.read_J = ++flush_J;
            return created;
         });
         l.init_ClientLevel = mc.level;
         l.init_J = System.nanoTime();
         return l;
      } else {
         return null;
      }
   }

   public static void init_V(Object owner) {
      cG.Inner_CachePool l = init_Map.remove(owner);
      if (l != null) {
         l.init();
      }
   }

   public static void load_V() {
      for (cG.Inner_CachePool l : init_Map.values()) {
         l.init();
      }

      init_Map.clear();
      init_cG_DataUtil = load_cG_DataUtil = cG.Inner_DataUtil.EMPTY;
      load_Map.clear();
      init_Obj = null;
      stop = 0;
   }

   public static void init(Object owner, String title, String activity, int completed, int target) {
      cG.Inner_CachePool l = init_cG_CachePool(owner);
      if (l != null) {
         l.load_cs_StreamUtil = cs.init(title, activity, completed, target);
         l.write = ++flush_J;
      }
   }

   static void init(Object owner, cs.Inner_StreamUtil content) {
      cG.Inner_CachePool l = init_cG_CachePool(owner);
      if (l != null) {
         l.read_cs_StreamUtil = content;
         l.update = ++flush_J;
      }
   }

   static int init_I() {
      long now = System.nanoTime();
      return init_Map.values()
         .stream()
         .filter(l -> !l.read_Z && l.load_I == 40 && now - l.init_J <= 450000000L && l.init_ClientLevel == Minecraft.getInstance().level)
         .mapToInt(l -> l.load_List.size())
         .sum();
   }

   public static void init_V_Obj(Object owner, List<autismclient.ai.a.ClassCache.UiHelper> path, int index, Supplier<BlockState> supportState) {
      cG.Inner_CachePool l = init_cG_CachePool(owner);
      if (l != null && init_AiSettings.visualizePaths && (l.init_Obj != path || l.init_I != index)) {
         l.init_Obj = path;
         l.init_I = index;
         l.init_cs_StreamUtil = new cs.Inner_StreamUtil("Travelling", "Following the planned route", "");
         l.load_I = 10;
         List<cG.Inner_BufferHelper> segments = new ArrayList<>();
         List<cG.Inner_StreamUtil> edits = new ArrayList<>();
         Minecraft mc = Minecraft.getInstance();
         int end = Math.min(path.size(), Math.max(0, index) + 192);
         BlockState support = supportState.get();

         for (int i = Math.max(0, index); i < end; i++) {
            autismclient.ai.a.ClassCache.UiHelper m = path.get(i);
            segments.add(
               new cG.Inner_BufferHelper(Vec3.atBottomCenterOf(m.from()).add(0.0, 0.12, 0.0), Vec3.atBottomCenterOf(m.to()).add(0.0, 0.12, 0.0), i == index)
            );

            for (BlockPos p : m.cellsToBreak()) {
               if (edits.size() < 64 && cu.init(mc.level, p)) {
                  edits.add(init(p, mc.level.getBlockState(p), false));
               }
            }

            for (BlockPos p : m.cellsToPlace()) {
               if (edits.size() < 64 && cu.init(mc.level, p)) {
                  edits.add(init(p, support == null ? Blocks.AIR.defaultBlockState() : support, support == null ? -885830 : -11093505));
               }
            }
         }

         l.read_List = List.copyOf(segments);
         l.init_List = edits.stream().filter(g -> !g.mining).toList();
         l.load_List = edits.stream().filter(g -> g.mining).toList();
      }
   }

   public static void init(Object owner, List<BlockPos> targets) {
      cG.Inner_CachePool l = init_cG_CachePool(owner);
      if (l != null && init_AiSettings.visualizeTargets && l.init_J - l.load_J >= 200000000L) {
         l.load_J = l.init_J;
         l.load_I = 40;
         List<cG.Inner_StreamUtil> out = new ArrayList<>();

         for (BlockPos p : targets) {
            if (out.size() >= 64) {
               break;
            }

            if (cu.init(l.init_ClientLevel, p)) {
               BlockState state = l.init_ClientLevel.getBlockState(p);
               if (!state.isAir()) {
                  out.add(init(p, state, false));
               }
            }
         }

         l.load_List = List.copyOf(out);
         l.init_cs_StreamUtil = out.isEmpty()
            ? cs.Inner_StreamUtil.EMPTY
            : new cs.Inner_StreamUtil("Mining", "Mining " + out.getFirst().state.getBlock().getName().getString(), "");
      }
   }

   public static void init(Object owner, BlockPos target) {
      cG.Inner_CachePool l = init_cG_CachePool(owner);
      if (l != null && init_AiSettings.visualizeTargets && cu.init(l.init_ClientLevel, target)) {
         BlockState state = l.init_ClientLevel.getBlockState(target);
         l.read_Z = true;
         l.load_I = 30;
         l.load_List = state.isAir() ? List.of() : List.of(init(target, state, true));
         l.init_cs_StreamUtil = state.isAir()
            ? cs.Inner_StreamUtil.EMPTY
            : new cs.Inner_StreamUtil("Mining", "Breaking " + state.getBlock().getName().getString(), "");
      }
   }

   public static void init(Object owner, BlockPos target, BlockState state) {
      cG.Inner_CachePool l = init_cG_CachePool(owner);
      if (l != null && state != null && init_AiSettings.visualizeTargets) {
         l.read_Z = true;
         if (l.init_J - l.load_J >= 200000000L) {
            l.load_J = l.init_J;
            l.load_I = 30;
            l.init_cs_StreamUtil = new cs.Inner_StreamUtil("Building", "Placing " + state.getBlock().getName().getString(), "");
            l.load_List = List.of(
               init(
                  target,
                  state,
                  init(
                     state.equals(l.init_ClientLevel.getBlockState(target)),
                     l.init_ClientLevel.getBlockState(target).canBeReplaced(),
                     cu.init(l.init_ClientLevel, target)
                  )
               )
            );
         }
      }
   }

   static int init(boolean satisfied, boolean replaceable, boolean loaded) {
      return !loaded ? -885830 : (satisfied ? -12067963 : (replaceable ? -11093505 : -20923));
   }

   private static cG.Inner_StreamUtil init(BlockPos p, BlockState state, int color) {
      return new cG.Inner_StreamUtil(p.immutable(), state, color, state.isSolidRender(), false, false);
   }

   private static cG.Inner_StreamUtil init(BlockPos p, BlockState state, boolean active) {
      return new cG.Inner_StreamUtil(p.immutable(), state, active ? -610078735 : 1885197528, false, true, active);
   }

   static void init(Object owner, BlockPos origin, List<TableIndex.Inner_StreamUtil> cells, int cursor, boolean preview) {
      init(owner, origin, cells, cursor, preview, preview);
   }

   static void init_V_Obj_2(Object owner, BlockPos origin, List<TableIndex.Inner_StreamUtil> cells, Predicate<BlockPos> hiddenSolid) {
      cG.Inner_CachePool l = init_cG_CachePool(owner);
      if (l != null) {
         l.init_Predicate = hiddenSolid;
         init(owner, origin, cells, 0, true, true);
      }
   }

   static void init(Object owner, long now) {
      cG.Inner_CachePool l = init_Map.get(owner);
      if (l != null) {
         l.init_J = now;
      }
   }

   static void init(Object owner, BlockPos origin, List<TableIndex.Inner_StreamUtil> cells, int cursor, boolean preview, boolean fullPlan) {
      cG.Inner_CachePool l = init_cG_CachePool(owner);
      if (l != null && init_AiSettings.visualizeGhosts && l.init_J - l.load_J >= 200000000L) {
         l.load_J = l.init_J;
         l.init_Z = fullPlan;
         l.load_I = preview ? 60 : 50;
         if (fullPlan) {
            init(l, origin, cells, cursor, preview);
         } else {
            l.init();
            int cap = preview ? cells.size() : init_I(init_AiSettings.visualizationBlocks);
            int stride = preview ? 1 : (fullPlan ? Math.max(1, (cells.size() + cap - 1) / cap) : 1);
            l.init_cs_StreamUtil = preview
               ? new cs.Inner_StreamUtil("Schematic preview", cells.size() + " planned blocks", "Blue: place / Amber: obstruction / Green: complete")
               : cs.init("Building", "Following the build plan", cursor, cells.size());
            List<cG.Inner_StreamUtil> out = new ArrayList<>();
            int x = 0;
            int y = 0;
            int z = 0;

            for (TableIndex.Inner_StreamUtil cell : cells) {
               x = Math.max(x, cell.dx() + 1);
               y = Math.max(y, cell.dy() + 1);
               z = Math.max(z, cell.dz() + 1);
            }

            l.init_AABB = cells.isEmpty()
               ? null
               : new AABB(origin.getX(), origin.getY(), origin.getZ(), origin.getX() + x, origin.getY() + y, origin.getZ() + z);

            for (int i = fullPlan ? 0 : Math.max(0, cursor - 32); i < cells.size() && out.size() < cap; i += stride) {
               TableIndex.Inner_StreamUtil c = cells.get(i);
               BlockPos at = origin.offset(c.dx(), c.dy(), c.dz());
               boolean loaded = cu.init(l.init_ClientLevel, at);
               BlockState live = loaded ? l.init_ClientLevel.getBlockState(at) : Blocks.AIR.defaultBlockState();
               boolean satisfied = loaded && TableIndex.update(c, live);
               int color = init(satisfied, live.canBeReplaced(), loaded);
               if (fullPlan
                  && loaded
                  && !satisfied
                  && !live.isAir()
                  && (live.hasBlockEntity() || live.getDestroySpeed(l.init_ClientLevel, at) < 0.0F || !live.getFluidState().isEmpty() || !bQ.init(live))) {
                  color = -885830;
               }

               out.add(init(at, TableIndex.init_BlockState(c), color));
            }

            l.init_List = List.copyOf(out);
         }
      }
   }

   private static void init(cG.Inner_CachePool l, BlockPos origin, List<TableIndex.Inner_StreamUtil> cells, int cursor, boolean preview) {
      if (l.init_aT == null || !l.init_aT.init(origin, cells, preview)) {
         l.init();
         l.init_aT = new aT(origin, cells, preview, l.init_Predicate);
      }

      if (preview) {
         l.init_aT.init_V_BlockPos(origin);
      }

      l.init_aT.init_V_Z(l.load_Z);
      l.init_aT.init_V_ClientLevel(l.init_ClientLevel);
      if (!l.init_aT.init_Z()) {
         l.init_cs_StreamUtil = cs.init(
            preview ? "Preparing schematic preview" : "Preparing build plan", "Reading every planned block", l.init_aT.init_I(), cells.size()
         );
         l.init_AABB = null;
         l.init_List = List.of();
      } else {
         l.init_cs_StreamUtil = preview
            ? new cs.Inner_StreamUtil("Schematic preview", cells.size() + " planned blocks", "Blue: place / Amber: obstruction / Green: complete")
            : cs.init("Building", "Following the build plan", cursor, cells.size());
         l.init_AABB = cells.isEmpty()
            ? null
            : new AABB(
               origin.getX(),
               origin.getY(),
               origin.getZ(),
               origin.getX() + l.init_aT.init_I,
               origin.getY() + l.init_aT.load_I,
               origin.getZ() + l.init_aT.read_I
            );
         Minecraft mc = Minecraft.getInstance();
         Vec3 eye = mc.player == null ? Vec3.atCenterOf(origin) : mc.player.getEyePosition();
         int outlines = preview ? 0 : 12;
         List<Integer> nearest = l.init_aT.init(eye, outlines, 4096, System.nanoTime() + 2000000L);
         List<cG.Inner_StreamUtil> out = new ArrayList<>(nearest.size());

         for (int i : nearest) {
            if (l.init_aT.init_I_I(i) != -12067963) {
               BlockState state = l.init_aT.init_BlockState_I(i);
               out.add(new cG.Inner_StreamUtil(l.init_aT.init_BlockPos_I(i), state, l.init_aT.init_I_I(i), state.isSolidRender(), false, false, true));
            }
         }

         l.init_List = List.copyOf(out);
      }
   }

   static void load(Object owner) {
      cG.Inner_CachePool l = init_Map.get(owner);
      if (l != null) {
         l.load_J = 0L;
      }
   }

   static void init(Object owner, boolean moving) {
      cG.Inner_CachePool l = init_cG_CachePool(owner);
      if (l != null) {
         l.load_Z = moving;
         if (l.init_aT != null) {
            l.init_aT.init_V_Z(moving);
         }
      }
   }

   static int init_I(int value) {
      return Math.max(64, Math.min(4096, value));
   }

   static int load(int value) {
      return Math.max(0, Math.min(100, value));
   }

   static void init_V(int value) {
      reset = load(value);
      update_J = System.nanoTime() + 2000000000L;
   }

   private static void init(Vec3 camera) {
      long started = System.nanoTime();
      Minecraft mc = Minecraft.getInstance();
      if ((!init_Map.isEmpty() || aP.init_cU() != null) && mc.level != null && !autismclient.modules.aD.init_Z() && autismclient.e.ListPool.init()) {
         int cap = init_I(init_AiSettings.visualizationBlocks);
         int distance = Math.max(16, Math.min(256, init_AiSettings.visualizationDistance));
         double range = (double)distance * distance;
         Map<Long, cG.Inner_StreamUtil> ghosts = new LinkedHashMap<>();
         Map<Long, cG.Inner_StreamUtil> targets = new LinkedHashMap<>();
         List<cG.Inner_BufferHelper> route = new ArrayList<>();
         AABB bounds = null;
         List<aT> plans = new ArrayList<>();
         List<cs.Inner_DataUtil> statuses = new ArrayList<>();
         long now = System.nanoTime();
         ArrayList<cG.Inner_CachePool> activeLayers = new ArrayList<>(init_Map.values());
         activeLayers.sort(Comparator.comparingLong(l -> l.read_J));

         for (cG.Inner_CachePool l : activeLayers) {
            if (l.init_ClientLevel == mc.level && now - l.init_J <= 450000000L) {
               if (init_AiSettings.visualizeGhosts && l.init_aT != null) {
                  plans.add(l.init_aT);
               }

               if (init_AiSettings.visualizeGhosts) {
                  for (cG.Inner_StreamUtil g : l.init_List) {
                     if ((l.init_Z || g.pos.distToCenterSqr(camera) <= range) && (ghosts.size() < cap || ghosts.containsKey(g.pos.asLong()))) {
                        if (l.init_Z) {
                           ghosts.put(g.pos.asLong(), g);
                        } else {
                           ghosts.putIfAbsent(g.pos.asLong(), g);
                        }
                     }
                  }
               }

               if (init_AiSettings.visualizeTargets) {
                  for (cG.Inner_StreamUtil g : l.load_List) {
                     if (g.pos.distToCenterSqr(camera) <= range) {
                        cG.Inner_StreamUtil previous = targets.get(g.pos.asLong());
                        if (previous == null || g.active) {
                           targets.put(g.pos.asLong(), g);
                        }
                     }
                  }
               }

               if (init_AiSettings.visualizePaths) {
                  for (cG.Inner_BufferHelper s : l.read_List) {
                     if (route.size() < 192 && Math.min(s.a.distanceToSqr(camera), s.b.distanceToSqr(camera)) <= range) {
                        route.add(s);
                     }
                  }
               }

               if (l.init_AABB != null && (l.init_Z || bounds == null)) {
                  bounds = l.init_AABB;
               }

               boolean visible = init_AiSettings.visualizeGhosts && (!l.init_List.isEmpty() || l.init_AABB != null)
                  || init_AiSettings.visualizeTargets && !l.load_List.isEmpty()
                  || init_AiSettings.visualizePaths && !l.read_List.isEmpty();
               if (visible) {
                  statuses.add(new cs.Inner_DataUtil(l.init_cs_StreamUtil, l.load_I, l.read_J));
               }

               if (l.load_cs_StreamUtil.visible()) {
                  statuses.add(new cs.Inner_DataUtil(l.load_cs_StreamUtil, 100, l.write));
               }

               if (l.read_cs_StreamUtil.visible()) {
                  statuses.add(new cs.Inner_DataUtil(l.read_cs_StreamUtil, 200, l.update));
               }
            }
         }

         if (init_AiSettings.visualizeGhosts) {
            for (cG.Inner_StreamUtil g : targets.values()) {
               if (!g.mining && (ghosts.size() < cap || ghosts.containsKey(g.pos.asLong()))) {
                  ghosts.put(g.pos.asLong(), g);
               }
            }
         }

         ArrayList<cG.Inner_StreamUtil> sorted = new ArrayList<>(ghosts.values());
         sorted.sort(Comparator.<cG.Inner_StreamUtil>comparingDouble(g -> g.pos.distToCenterSqr(camera)).reversed());
         Vec3 body = mc.player == null ? camera : mc.player.getEyePosition();
         List<cG.Inner_StreamUtil> quietTargets = init(targets.values(), body);
         init_cG_DataUtil = new cG.Inner_DataUtil(
            mc.level, camera, List.copyOf(sorted), quietTargets, List.copyOf(route), bounds, cs.init(statuses, now), List.copyOf(plans)
         );
         load_cG_DataUtil = init_cG_DataUtil;
         write_J = System.nanoTime() - started;
      } else {
         init_cG_DataUtil = cG.Inner_DataUtil.EMPTY;
         load_cG_DataUtil = cG.Inner_DataUtil.EMPTY;
         stop = 0;
      }
   }

   static List<cG.Inner_StreamUtil> init(Collection<cG.Inner_StreamUtil> targets, Vec3 body) {
      ArrayList<cG.Inner_StreamUtil> targetView = new ArrayList<>(targets);
      targetView.sort(
         Comparator.<cG.Inner_StreamUtil, Boolean>comparing(gx -> !gx.active)
            .thenComparingDouble(gx -> gx.pos.distToCenterSqr(body))
            .thenComparingLong(gx -> gx.pos.asLong())
      );
      List<cG.Inner_StreamUtil> quietTargets = new ArrayList<>();
      int miningCount = 0;

      for (cG.Inner_StreamUtil g : targetView) {
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

   public static boolean init_Z() {
      return init_cG_DataUtil.world != null
         && (
            !init_cG_DataUtil.ghosts.isEmpty()
               || !init_cG_DataUtil.route.isEmpty()
               || !init_cG_DataUtil.targets.isEmpty()
               || !init_cG_DataUtil.plans.isEmpty()
               || init_AiSettings.visualizeGhosts && init_cG_DataUtil.bounds != null
               || aP.init_cU() != null
         );
   }

   public static void init(PoseStack matrices) {
      cG.Inner_DataUtil f = init_cG_DataUtil;
      init_cG_DataUtil = cG.Inner_DataUtil.EMPTY;
      Minecraft mc = Minecraft.getInstance();
      if (f.world != null && f.world == mc.level) {
         long started = System.nanoTime();
         Object currentModels = mc.getModelManager().getBlockStateModelSet();
         if (init_Obj != currentModels) {
            load_Map.clear();
            init_Obj = currentModels;
         }

         PkgCls_c source = init_c_DataUtil.init();
         Pose pose = matrices.last();
         int alpha = (reset >= 0 && System.nanoTime() < update_J ? reset : load(init_AiSettings.ghostOpacity)) * 255 / 100;
         int quads = 0;
         int baked = 0;
         int planCells = 0;
         if (alpha > 0) {
            for (aT plan : f.plans) {
               try {
                  plan.init_V_PoseStack_Pose(pose, f.camera, alpha, 512.0, started + 4000000L, f.world);
                  quads += plan.close;
                  planCells += plan.flush;
               } catch (Throwable error) {
                  AutismClientAddon.LOG.warn("[AI] plan mesh draw failed; the plan stays outlined only", error);
                  plan.close();
               }
            }
         }

         try {
            VertexConsumer texture = source.init(net.minecraft.client.renderer.rendertype.CachePool.load());
            Set<Long> solid = new HashSet<>();

            for (cG.Inner_StreamUtil g : f.ghosts) {
               if (g.fullCube) {
                  solid.add(g.pos.asLong());
               }
            }

            if (alpha > 0) {
               for (cG.Inner_StreamUtil g : f.ghosts) {
                  if (!g.mining && !g.planned && !g.state.isAir()) {
                     autismclient.util.oresim.DataUtil.Inner_StreamUtil model = load_Map.get(g.state);
                     if (model == null && baked < 8 && System.nanoTime() - started < 2000000L) {
                        model = autismclient.util.oresim.DataUtil.init(g.state);
                        if (model.init_List().isEmpty()) {
                           model = init(g.state);
                        }

                        if (load_Map.size() >= 2048) {
                           load_Map.clear();
                        }

                        load_Map.put(g.state, model);
                        baked++;
                     }

                     if (model != null) {
                        BlockPos p = g.pos;
                        double ox = p.getX() - f.camera.x;
                        double oy = p.getY() - f.camera.y;
                        double oz = p.getZ() - f.camera.z;

                        for (autismclient.util.oresim.DataUtil.Inner_DataUtil face : model.init_List()) {
                           if (quads >= 16384) {
                              break;
                           }

                           Direction d = face.init_Direction();
                           if (d == null
                              || !solid.contains(p.relative(d).asLong())
                                 && !(
                                    (f.camera.x - p.getX() - 0.5) * d.getStepX()
                                          + (f.camera.y - p.getY() - 0.5) * d.getStepY()
                                          + (f.camera.z - p.getZ() - 0.5) * d.getStepZ()
                                       <= 0.0
                                 )) {
                              BlockTintSource tintSource = face.init_I() < 0 ? null : mc.getBlockColors().getTintSource(g.state, face.init_I());
                              int tint = tintSource == null
                                 ? 16777215
                                 : (g.color == -885830 ? tintSource.color(g.state) : tintSource.colorInWorld(g.state, f.world, p));
                              float shade = face.load() == null ? 1.0F : f.world.cardinalLighting().byFace(face.load());
                              int color = init_I_I(g.color, tint, shade, alpha);
                              float[] data = face.init_FArr();

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

            VertexConsumer lines = source.init(net.minecraft.client.renderer.rendertype.CachePool.read());
            Vec3 offset = f.camera.scale(-1.0);
            cU selection = aP.init_cU();
            if (selection != null) {
               cP.Inner_DataUtil area = selection.init_cP_DataUtil();
               if (area != null) {
                  autismclient.util.cI.init(pose, lines, new AABB(area.min()).minmax(new AABB(area.max())).move(offset), -1336493569, 1.3F);
               }

               if (selection.init_BlockPos != null) {
                  autismclient.util.cI.init(pose, lines, new AABB(selection.init_BlockPos).move(offset), -12067963, 1.8F);
               }

               if (selection.load != null) {
                  autismclient.util.cI.init(pose, lines, new AABB(selection.load).move(offset), -20923, 1.8F);
               }

               if (selection.read != null) {
                  autismclient.util.cI.init(pose, lines, new AABB(selection.read).move(offset).inflate(0.008), -11093505, 2.0F);
               }
            }

            if (init_AiSettings.visualizeGhosts) {
               for (int i = Math.max(0, f.ghosts.size() - 192); i < f.ghosts.size(); i++) {
                  cG.Inner_StreamUtil g = f.ghosts.get(i);
                  autismclient.util.cI.init(pose, lines, new AABB(g.pos).move(offset).deflate(0.006), -1342177280 | g.color & 16777215, 1.1F);
               }
            }

            if (f.bounds != null && init_AiSettings.visualizeGhosts) {
               autismclient.util.cI.init(pose, lines, f.bounds.move(offset), -1873364481, 1.6F);
            }

            for (cG.Inner_BufferHelper s : f.route) {
               autismclient.util.cI.init(pose, lines, s.a.add(offset), s.b.add(offset), -1072159693, s.next ? 5.0F : 4.0F);
            }

            for (cG.Inner_BufferHelper s : f.route) {
               Vec3 a = s.a.add(offset);
               Vec3 b = s.b.add(offset);
               autismclient.util.cI.init(pose, lines, a, b, s.next ? -1 : -312222235, s.next ? 2.8F : 2.0F);
               autismclient.util.cI.init(pose, lines, b.add(-0.12, 0.0, 0.0), b.add(0.12, 0.0, 0.0), -547103259, 1.5F);
               if (s.next) {
                  Vec3 heading = b.subtract(a);
                  double length = heading.horizontalDistance();
                  if (length > 0.001) {
                     double dx = heading.x / length;
                     double dz = heading.z / length;
                     autismclient.util.cI.init(pose, lines, b.add(-dx * 0.28 - dz * 0.14, 0.0, -dz * 0.28 + dx * 0.14), b, -1, 2.0F);
                     autismclient.util.cI.init(pose, lines, b.add(-dx * 0.28 + dz * 0.14, 0.0, -dz * 0.28 - dx * 0.14), b, -1, 2.0F);
                  }
               }
            }

            for (cG.Inner_StreamUtil g : f.targets) {
               AABB b = new AABB(g.pos).move(offset).inflate(g.mining ? 0.003 : 0.014);
               if (g.mining) {
                  List<Vec3> corners = init(b, init(g.pos, f.camera));

                  for (int i = 0; i < 4; i++) {
                     autismclient.util.cI.init(pose, lines, corners.get(i), corners.get((i + 1) % 4), g.color, g.active ? 1.45F : 0.9F);
                  }
               } else {
                  autismclient.util.cI.init(pose, lines, b, g.color, 1.7F);
               }
            }
         } finally {
            source.init();
            pop = quads;
            tick = f.ghosts.size() + planCells;
            run = f.route.size();
            start = (int)f.targets.stream().filter(gx -> gx.mining).count();
            read_J = System.nanoTime() - started;
            clear = 0;
            add = 0;
            put = 0;

            for (cG.Inner_StreamUtil g : f.ghosts) {
               if (!g.planned) {
                  if (g.color == -12067963) {
                     put++;
                  } else if (g.color == -11093505) {
                     add++;
                  } else if (g.color == -20923) {
                     clear++;
                  }
               }
            }

            for (aT plan : f.plans) {
               put = put + plan.write_I;
               add = add + plan.update;
               clear = clear + plan.sync;
            }

            init_JArr[get++ % init_JArr.length] = read_J + write_J;
            set = Math.min(init_JArr.length, set + 1);
         }
      }
   }

   static Direction init(BlockPos pos, Vec3 camera) {
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

   static List<Vec3> init(AABB b, Direction face) {
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

   static int init_I_I(int status, int tint, float shade, int alpha) {
      int r = Math.round(((status >> 16 & 0xFF) * 0.38F + 158.1F) * (tint >> 16 & 0xFF) / 255.0F * shade);
      int g = Math.round(((status >> 8 & 0xFF) * 0.38F + 158.1F) * (tint >> 8 & 0xFF) / 255.0F * shade);
      int b = Math.round(((status & 0xFF) * 0.38F + 158.1F) * (tint & 0xFF) / 255.0F * shade);
      return alpha << 24 | Math.min(255, r) << 16 | Math.min(255, g) << 8 | Math.min(255, b);
   }

   private static autismclient.util.oresim.DataUtil.Inner_StreamUtil init(BlockState state) {
      TextureAtlasSprite sprite = Minecraft.getInstance().getModelManager().getBlockStateModelSet().getParticleMaterial(state).sprite();
      List<autismclient.util.oresim.DataUtil.Inner_DataUtil> faces = new ArrayList<>();

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

         faces.add(new autismclient.util.oresim.DataUtil.Inner_DataUtil(d, d, vertices, -1));
      }

      return new autismclient.util.oresim.DataUtil.Inner_StreamUtil(List.copyOf(faces));
   }

   public static JsonObject init_JsonObject() {
      long[] costs = Arrays.copyOf(init_JArr, set);
      Arrays.sort(costs);
      long now = System.nanoTime();
      List<cG.Inner_CachePool> plans = init_Map.values()
         .stream()
         .filter(l -> l.init_Z && l.init_ClientLevel == Minecraft.getInstance().level && now - l.init_J <= 450000000L)
         .toList();
      return ag.init_JsonObject_ObjArr(
         "ghosts",
         tick,
         "quads",
         pop,
         "segments",
         run,
         "renderMicros",
         read_J / 1000L,
         "fullPlanLayers",
         plans.size(),
         "fullPlanGhosts",
         plans.stream().mapToInt(l -> l.init_aT != null ? l.init_aT.read() : l.init_List.size()).sum(),
         "buildPlanLayers",
         plans.stream().filter(l -> l.load_I == 50).count(),
         "miningTargets",
         start,
         "miningTexturedQuads",
         0,
         "hudTitle",
         load_cG_DataUtil.status.title(),
         "hudActivity",
         load_cG_DataUtil.status.activity(),
         "hudDetail",
         load_cG_DataUtil.status.detail(),
         "hudWidth",
         stop,
         "hudContentWidth",
         next,
         "blockBudget",
         init_I(init_AiSettings.visualizationBlocks),
         "quadBudget",
         16384,
         "opacity",
         load(init_AiSettings.ghostOpacity),
         "green",
         put,
         "blue",
         add,
         "orange",
         clear,
         "frames",
         set,
         "cpuMedianMicros",
         set == 0 ? 0L : costs[set / 2] / 1000L,
         "cpuP95Micros",
         set == 0 ? 0L : costs[Math.min(set - 1, (int)(set * 0.95))] / 1000L
      );
   }

   static void read() {
      get = 0;
      set = 0;
      Arrays.fill(init_JArr, 0L);
      clear = 0;
      add = 0;
      put = 0;
      run = 0;
      pop = 0;
      tick = 0;
      start = 0;
      next = 0;
      stop = 0;
      write_J = 0L;
      read_J = 0L;
   }

   public static boolean load_Z() {
      if (aP.init_Z()) {
         return true;
      }

      Screen screen = Minecraft.getInstance().gui.screen();
      return (screen == null || screen instanceof AbstractContainerScreen)
         && load_cG_DataUtil.world == Minecraft.getInstance().level
         && load_cG_DataUtil.status.visible()
         && !init_Map.isEmpty();
   }

   public static autismclient.gui.f.NetHelper init_NetHelper(GuiGraphicsExtractor g) {
      if (aP.init_Z()) {
         return aP.init_NetHelper_GuiGraphicsExtract(g);
      }

      autismclient.gui.f.SyncManager ui = autismclient.gui.f.UiHelper.init(g, Minecraft.getInstance().font, 0, 0);
      cs.Inner_StreamUtil status = load_cG_DataUtil.status;
      next = cs.init(status, ui.init_PoolEntry()::init, 2147483647);
      int width = cs.init(status, ui.init_PoolEntry()::init, Math.min(ui.init_I() - 16, 360));
      stop = width;
      int height = autismclient.gui.f.b.StreamUtil.init(ui, width, status.activity(), status.detail());
      autismclient.gui.f.NetHelper bounds = autismclient.gui.f.NetHelper.init(8, 8, width, height);

      for (cX overlay : bk.init_bk().init_List()) {
         if (overlay.sync_Z() && overlay.init_Str().equals("ai-assistant")) {
            cH window = overlay.init_cH();
            if (bounds.read() < window.init_I + window.read
               && bounds.init() > window.init_I
               && bounds.write() < window.load_I + window.write
               && bounds.load() > window.load_I) {
               bounds = autismclient.gui.f.NetHelper.init(Math.max(8, ui.init_I() - width - 8), 8, width, height);
            }
         }
      }

      return bounds;
   }

   public static void init_V(GuiGraphicsExtractor g) {
      if (aP.init_Z()) {
         aP.init_V_GuiGraphicsExtract(g);
      } else if (load_Z()) {
         autismclient.gui.f.SyncManager ui = autismclient.gui.f.UiHelper.init(g, Minecraft.getInstance().font, 0, 0);
         autismclient.gui.f.NetHelper b = init_NetHelper(g);
         cs.Inner_StreamUtil status = load_cG_DataUtil.status;
         autismclient.gui.f.b.StreamUtil.init(ui, b, status.title(), status.activity(), status.detail());
      }
   }

   record Inner_BufferHelper(Vec3 a, Vec3 b, boolean next) {
   }

   private static final class Inner_CachePool {
      ClientLevel init_ClientLevel;
      long init_J;
      long load_J;
      long read_J;
      long write;
      long update;
      List<cG.Inner_StreamUtil> init_List = List.of();
      List<cG.Inner_StreamUtil> load_List = List.of();
      List<cG.Inner_BufferHelper> read_List = List.of();
      Object init_Obj;
      int init_I = -1;
      AABB init_AABB;
      cs.Inner_StreamUtil init_cs_StreamUtil = cs.Inner_StreamUtil.EMPTY;
      cs.Inner_StreamUtil load_cs_StreamUtil = cs.Inner_StreamUtil.EMPTY;
      cs.Inner_StreamUtil read_cs_StreamUtil = cs.Inner_StreamUtil.EMPTY;
      int load_I;
      boolean init_Z;
      boolean load_Z;
      boolean read_Z;
      aT init_aT;
      Predicate<BlockPos> init_Predicate;

      void init() {
         if (this.init_aT != null) {
            this.init_aT.close();
            this.init_aT = null;
         }
      }
   }

   private record Inner_DataUtil(
      ClientLevel world,
      Vec3 camera,
      List<cG.Inner_StreamUtil> ghosts,
      List<cG.Inner_StreamUtil> targets,
      List<cG.Inner_BufferHelper> route,
      AABB bounds,
      cs.Inner_StreamUtil status,
      List<aT> plans
   ) {
      static final cG.Inner_DataUtil EMPTY = new cG.Inner_DataUtil(null, Vec3.ZERO, List.of(), List.of(), List.of(), null, cs.Inner_StreamUtil.EMPTY, List.of());
   }

   record Inner_StreamUtil(BlockPos pos, BlockState state, int color, boolean fullCube, boolean mining, boolean active, boolean planned) {
      Inner_StreamUtil(BlockPos pos, BlockState state, int color, boolean fullCube, boolean mining, boolean active) {
         this(pos, state, color, fullCube, mining, active, false);
      }
   }
}

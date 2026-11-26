package autismclient.util;

import autismclient.AutismClientAddon;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.ChatScreen;
import net.minecraft.client.gui.screens.InBedChatScreen;
import net.minecraft.client.gui.screens.Screen;

public class OverlayManager {
   private static final OverlayManager init_bk = new OverlayManager();
   public static final int init_I = -10000;
   private static final double init_D = 3.0;
   private final List<AutismOverlay> init_List = new CopyOnWriteArrayList<>();
   private final List<AutismOverlay> load_List = new ArrayList<>();
   private final Map<AutismOverlay, autismclient.gui.ui.widgets.OverlayWindowLayerAdapter> init_Map = new IdentityHashMap<>();
   private final Map<AutismOverlay, AutismOverlay.RenderLayer> load_Map = new IdentityHashMap<>();
   private final Set<String> init_Set = new HashSet<>();
   private final Set<AutismOverlay> load_Set = Collections.newSetFromMap(new IdentityHashMap<>());
   private boolean init_Z = true;
   private WeakReference<Screen> init_WeakReference = new WeakReference<>(null);
   private final autismclient.gui.ui.UiTheme init_TableIndex = new autismclient.gui.ui.UiTheme();
   private final autismclient.gui.ui.UiLayerHost init_ByteReader = new autismclient.gui.ui.UiLayerHost();
   private final autismclient.gui.ui.UiLayerInputRouter init_FileCache = new autismclient.gui.ui.UiLayerInputRouter(this.init_ByteReader);
   private autismclient.gui.ui.UiTextRenderer init_PoolEntry;
   private boolean load_Z = true;
   private AutismOverlay init_cX = null;
   private AutismOverlay load_cX;
   private boolean read_Z = true;
   private double load_D = 0.0 / 0.0;
   private double read_D = 0.0 / 0.0;
   private long init_J;
   private boolean write_Z;
   private final Map<String, OverlayBounds> read_Map = new HashMap<>();
   private static final int load_I = 64;
   private AutismOverlay read_cX = null;
   private AutismOverlay write_cX = null;
   private AutismOverlay update_cX = null;
   private int read_I = -1;
   private int write_I = -1;
   private volatile boolean update_Z = false;
   private boolean sync_Z = false;
   private boolean flush_Z = false;
   private OverlayBounds init_cH = null;
   private OverlayBounds load_cH = null;
   private OverlayBounds read_cH = null;
   private double write_D = 0.0;
   private double update_D = 0.0;
   private double sync_D = 0.0;
   private double flush_D = 0.0;
   private AutismOverlay sync_cX;
   private long load_J;
   private static final long read_J = 350L;
   private long write_J;

   private OverlayManager() {
   }

   public static OverlayManager init_bk() {
      return init_bk;
   }

   public List<AutismOverlay> init_List() {
      return this.init_List;
   }

   public boolean init_Z() {
      return !this.init_List.isEmpty();
   }

   public boolean load_Z() {
      if (autismclient.modules.aD.init_Z()) {
         return false;
      } else {
         return sync_Z() ? false : this.update_Z;
      }
   }

   private static boolean sync_Z() {
      return !AutismLiteVariant.enabled() && !autismclient.e.PacketUtil.init();
   }

   public String init_Str() {
      StringBuilder visible = new StringBuilder();
      int visibleCount = 0;

      for (AutismOverlay overlay : this.init_List) {
         if (this.sync_Z(overlay)) {
            if (visibleCount++ > 0) {
               visible.append(", ");
            }

            visible.append(overlay.init_Str());
         }
      }

      return "registered=" + this.init_List.size() + " visible=" + visibleCount + " [" + visible + "]";
   }

   public void init_V(AutismOverlay overlay) {
      this.init_V_cX_cX_DataUtil(overlay, this.init_cX_DataUtil(overlay));
   }

   public void load_V(AutismOverlay overlay) {
      if (overlay != null && this.init_List.contains(overlay)) {
         this.load_Map.put(overlay, overlay.init_cX_DataUtil());
      }
   }

   public void init_V_cX_cX_DataUtil(AutismOverlay overlay, AutismOverlay.RenderLayer scope) {
      if (overlay != null) {
         this.sync_V(overlay);
         if (!this.init_List.contains(overlay)) {
            this.init_List.add(overlay);
         }

         this.load_Map.put(overlay, scope == null ? overlay.init_cX_DataUtil() : scope);
         this.load_Set.remove(overlay);
         this.init_ArrayUtil(overlay);
         this.load_Z = true;
         this.read_Z = true;
         this.load(true);
         this.get();
         this.poll();
         this.tick();
         if (overlay instanceof AutismLauncherOverlay || "autism-launcher".equals(overlay.init_Str())) {
            this.flush();
         }
      }
   }

   public void read_V(AutismOverlay overlay) {
      if (overlay != null) {
         autismclient.gui.ui.widgets.OverlayWindowLayerAdapter adapter = this.init_Map.get(overlay);
         if (adapter != null && this.init_List.contains(overlay) && !overlay.sync_Z()) {
            adapter.init_V();
            Minecraft mc = Minecraft.getInstance();
            Screen screen = mc == null ? null : mc.gui.screen();
            if (adapter.init_Z() && !this.load_Z(overlay) && this.load(overlay, screen)) {
               this.load_Set.add(overlay);
               if (this.init_cX == overlay) {
                  this.init_cX = null;
               }

               if (this.load_cX == overlay) {
                  this.load_cX = null;
               }

               this.read_Z = true;
               this.get();
               return;
            }
         }

         this.flush_V(overlay);
      }
   }

   private void sync_V(AutismOverlay incoming) {
      String id = incoming.init_Str();
      if (id != null && !id.isEmpty()) {
         for (AutismOverlay existing : this.init_List) {
            if (existing != incoming && id.equals(existing.init_Str())) {
               this.flush_V(existing);
            }
         }
      }
   }

   private void flush_V(AutismOverlay overlay) {
      this.load_Set.remove(overlay);
      this.init_List.remove(overlay);
      this.init_Map.remove(overlay);
      this.load_Map.remove(overlay);
      this.load_Z = true;
      this.init_Set.remove(overlay.init_Str());
      if (this.init_cX == overlay) {
         this.init_cX = null;
      }

      if (this.load_cX == overlay) {
         this.load_cX = null;
      }

      overlay.push_V();
      if (this.read_cX == overlay) {
         this.read_cX = null;
         this.read_cH = null;
      }

      if (this.write_cX == overlay) {
         this.write_cX = null;
         this.load_cH = null;
      }

      if (this.update_cX == overlay) {
         this.update_cX = null;
         this.init_cH = null;
         this.sync_Z = false;
      }

      this.read_Z = true;
      if (this.init_List.isEmpty()) {
         this.load(false);
      }

      this.get();
      this.pop();
   }

   public void write_V(AutismOverlay overlay) {
      autismclient.gui.ui.widgets.OverlayWindowLayerAdapter adapter = overlay == null ? null : this.init_Map.get(overlay);
      if (adapter != null) {
         adapter.read();
      }
   }

   public void init_V() {
      this.init_Z = true;
   }

   public boolean init_Z(AutismOverlay overlay) {
      autismclient.gui.ui.widgets.OverlayWindowLayerAdapter adapter = overlay == null ? null : this.init_Map.get(overlay);
      if (adapter == null) {
         return false;
      }

      adapter.init_V();
      return adapter.init_Z();
   }

   public void load_V() {
      if (!OverlayScreenController.read_Z()) {
         for (AutismOverlay pending : new ArrayList<>(this.load_Set)) {
            this.flush_V(pending);
         }

         this.init_Z = true;
         this.init_List.removeIf(overlay -> {
            if (overlay.f_()) {
               return false;
            }

            if (!AutismLiteVariant.enabled() && overlay instanceof AutismProfilesOverlay) {
               overlay.init_V_Z(false);
            }

            this.init_Map.remove(overlay);
            this.load_Map.remove(overlay);
            return true;
         });
         this.init_ByteReader.init();
         this.load_Z = true;
         this.init_Set.clear();
         this.read_cX = null;
         this.read_cH = null;
         this.write_cX = null;
         this.update_cX = null;
         this.init_cX = null;
         this.load_cX = null;
         this.read_Z = false;
         this.sync_Z = false;
         this.init_cH = null;
         this.load_cH = null;
         this.flush_Z = false;
         this.load(!this.init_List.isEmpty());
         this.get();
      }
   }

   public void init_V_cX_Z(AutismOverlay overlay, boolean hidden) {
      if (overlay != null) {
         String id = overlay.init_Str();
         if (id != null && !id.isEmpty()) {
            if (hidden) {
               this.init_Set.add(id);
               overlay.push_V();
               if (this.init_cX == overlay) {
                  this.init_cX = null;
               }

               if (this.read_cX == overlay) {
                  this.read_cX = null;
                  this.read_cH = null;
               }

               if (this.write_cX == overlay) {
                  this.write_cX = null;
               }

               if (this.update_cX == overlay) {
                  this.update_cX = null;
               }
            } else {
               this.init_Set.remove(id);
            }

            this.read_Z = true;
            this.get();
         }
      }
   }

   public void read_V() {
      this.init_Set.clear();
      this.read_Z = true;
      this.get();
   }

   public boolean load_Z(AutismOverlay overlay) {
      if (overlay == null) {
         return false;
      }

      String id = overlay.init_Str();
      return id != null && this.init_Set.contains(id);
   }

   private boolean sync_Z(AutismOverlay overlay) {
      Minecraft mc = Minecraft.getInstance();
      return this.init_Z_cX_Screen(overlay, mc == null ? null : mc.gui.screen());
   }

   private boolean init_Z_cX_Screen(AutismOverlay overlay, Screen screen) {
      return !OverlayScreenController.init_Z() && overlay != null && overlay.sync_Z() && !this.load_Z(overlay) && this.load(overlay, screen);
   }

   private AutismOverlay.RenderLayer init_cX_DataUtil(AutismOverlay overlay) {
      Minecraft mc = Minecraft.getInstance();
      Screen screen = mc == null ? null : mc.gui.screen();
      if (overlay != null && overlay.init_cX_DataUtil() == AutismOverlay.RenderLayer.BACKGROUND_STATUS) {
         return AutismOverlay.RenderLayer.BACKGROUND_STATUS;
      } else if (!AutismLiteVariant.enabled() && screen instanceof autismclient.gui.screens.AutismModulesScreen) {
         return AutismOverlay.RenderLayer.MODULE_MENU;
      } else if (!(screen instanceof autismclient.gui.screens.AutismOverlaysScreen) && screen != null && !(screen instanceof ChatScreen)) {
         return AutismOverlay.RenderLayer.CONTAINER_GUI;
      } else {
         return overlay == null ? AutismOverlay.RenderLayer.HOST_SCREEN : overlay.init_cX_DataUtil();
      }
   }

   private boolean load(AutismOverlay overlay, Screen screen) {
      AutismOverlay.RenderLayer scope = this.load_Map.getOrDefault(overlay, overlay == null ? AutismOverlay.RenderLayer.HOST_SCREEN : overlay.init_cX_DataUtil());
      if (scope == AutismOverlay.RenderLayer.BACKGROUND_STATUS) {
         return true;
      }

      if (screen == null || screen instanceof ChatScreen || screen instanceof InBedChatScreen) {
         return false;
      }

      if (!AutismLiteVariant.enabled() && screen instanceof autismclient.gui.screens.AutismModulesScreen) {
         return !this.flush_Z(overlay);
      }

      if (screen instanceof autismclient.gui.screens.AutismOverlaysScreen) {
         return true;
      }

      boolean var10000;
      switch (scope) {
         case BACKGROUND_STATUS:
            var10000 = true;
            break;
         case HOST_SCREEN:
         case MODULE_MENU:
         case CONTAINER_GUI:
            var10000 = true;
            break;
         default:
            throw new MatchException(null, null);
      }

      return var10000;
   }

   public void write_V() {
      Minecraft mc = Minecraft.getInstance();
      Screen screen = mc == null ? null : mc.gui.screen();
      boolean changed = false;

      for (AutismOverlay overlay : this.init_List) {
         if (overlay != null && overlay.sync_Z() && !this.load(overlay, screen)) {
            overlay.push_V();
            if (this.init_cX == overlay) {
               this.init_cX = null;
            }

            if (this.read_cX == overlay) {
               this.read_cX = null;
               this.read_cH = null;
            }

            if (this.write_cX == overlay) {
               this.write_cX = null;
            }

            if (this.update_cX == overlay) {
               this.update_cX = null;
            }

            changed = true;
         }
      }

      if (changed) {
         this.read_Z = true;
         this.get();
      }
   }

   public void update_V() {
      this.init(false);
   }

   public void sync_V() {
      this.init(true);
   }

   private void init(boolean rememberWindows) {
      this.init_Set.clear();

      for (AutismOverlay overlay : this.init_List) {
         if (overlay != null && this.load_Map.getOrDefault(overlay, overlay.init_cX_DataUtil()) != AutismOverlay.RenderLayer.BACKGROUND_STATUS) {
            if (overlay.sync_Z()) {
               if (rememberWindows && overlay.update_Z()) {
                  this.init_V_cX_Z(overlay, true);
               } else {
                  overlay.init_V_Z(false);
               }
            }

            overlay.push_V();
         }
      }

      this.read_cX = null;
      this.read_cH = null;
      this.write_cX = null;
      this.update_cX = null;
      this.init_cX = null;
      this.load_cX = null;
      this.read_Z = false;
      this.flush_Z = false;
      this.get();
   }

   public boolean read_Z() {
      for (AutismOverlay overlay : this.init_List) {
         if (overlay != null && overlay.sync_Z() && !this.load_Z(overlay) && !this.flush_Z(overlay) && overlay.init_cX_DataUtil() != AutismOverlay.RenderLayer.BACKGROUND_STATUS) {
            return true;
         }
      }

      return false;
   }

   public void update_V(AutismOverlay overlay) {
      if (overlay != null) {
         this.sync_V(overlay);
         this.init_List.remove(overlay);
         this.init_List.add(overlay);
         this.load_Z = true;
         this.load(true);
         this.init_cX = overlay;
         this.read_Z = true;
         AutismSessionState.init_ck().init_V_Str(overlay.init_Str());
         this.get();
         this.tick();
         this.pop();
      }
   }

   public void init(Object childComponent) {
      if (childComponent != null) {
         for (AutismOverlay overlay : this.init_List) {
            if (overlay instanceof AutismCustomFilterOverlay filterOverlay && filterOverlay.init_AutismPacketSelect() == childComponent) {
               this.update_V(overlay);
               return;
            }
         }
      }
   }

   private void poll() {
      if (this.init_List.size() < 2) {
         this.push();
         this.tick();
      } else {
         List<String> savedOrder = AutismSessionState.init_ck().init_List();
         if (savedOrder.isEmpty()) {
            this.push();
            this.tick();
         } else {
            String focusedId = AutismSessionState.init_ck().init_Str();
            Map<String, Integer> positions = new HashMap<>();

            for (int i = 0; i < savedOrder.size(); i++) {
               positions.putIfAbsent(savedOrder.get(i), i);
            }

            List<AutismOverlay> ordered = new ArrayList<>(this.init_List);
            ordered.sort(
               Comparator.<AutismOverlay>comparingInt(overlay -> positions.getOrDefault(overlay.init_Str(), 2147483647))
                  .thenComparingInt(overlay -> focusedId.equals(overlay.init_Str()) ? 1 : 0)
            );
            this.init_List.clear();
            this.init_List.addAll(ordered);
            this.load_Z = true;
            this.push();
            this.tick();
         }
      }
   }

   private void push() {
      String focusedId = AutismSessionState.init_ck().init_Str();
      if (focusedId.isEmpty()) {
         this.init_cX = null;
      } else {
         this.init_cX = null;

         for (int i = this.init_List.size() - 1; i >= 0; i--) {
            AutismOverlay overlay = this.init_List.get(i);
            if (overlay != null && focusedId.equals(overlay.init_Str()) && this.sync_Z(overlay)) {
               this.init_cX = overlay;
               break;
            }
         }
      }
   }

   private void pop() {
      List<String> order = new ArrayList<>(this.init_List.size());

      for (AutismOverlay overlay : this.init_List) {
         String id = overlay.init_Str();
         if (id != null && !id.isEmpty() && !order.contains(id) && !this.flush_Z(overlay) && !this.close_Z(overlay)) {
            order.add(id);
         }
      }

      AutismSessionState.init_ck().init_V_List(order);
   }

   private void tick() {
      if (!this.init_List.isEmpty()) {
         List<AutismOverlay> launchers = new ArrayList<>();
         List<AutismOverlay> others = new ArrayList<>();

         for (AutismOverlay overlay : this.init_List) {
            if (this.flush_Z(overlay)) {
               launchers.add(overlay);
            } else {
               others.add(overlay);
            }
         }

         if (!launchers.isEmpty()) {
            this.init_List.clear();
            this.init_List.addAll(launchers);
            this.init_List.addAll(others);
            this.load_Z = true;
         }
      }
   }

   private boolean flush_Z(AutismOverlay overlay) {
      return overlay instanceof AutismLauncherOverlay || overlay != null && "autism-launcher".equals(overlay.init_Str());
   }

   private boolean close_Z(AutismOverlay overlay) {
      return overlay != null && "macro-step-picker".equals(overlay.init_Str());
   }

   public void flush() {
      for (AutismOverlay overlay : this.init_List) {
         this.close_V(overlay);
      }

      this.run();
      this.get();
   }

   private void run() {
      if (!this.read_Map.isEmpty()) {
         int sw = cA.init_I();
         int sh = cA.load_I();
         if (sw > 0 && sh > 0) {
            this.read_Map.values().removeIf(stashed -> init_Z_cH(stashed, sw, sh));
            if (this.read_Map.size() > 64) {
               Set<String> live = new HashSet<>();

               for (AutismOverlay overlay : this.init_List) {
                  if (overlay != null && overlay.init_Str() != null) {
                     live.add(overlay.init_Str());
                  }
               }

               this.read_Map.keySet().removeIf(id -> !live.contains(id));
            }
         }
      }
   }

   static boolean init_Z_cH(OverlayBounds bounds, int screenWidth, int screenHeight) {
      return bounds != null && screenWidth > 0 && screenHeight > 0
         ? init_Z_cH_cH(OverlayLayoutBase.init_cH_cH(bounds, bounds.read, bounds.write, screenWidth, screenHeight), bounds)
         : false;
   }

   private void close_V(AutismOverlay overlay) {
      if (overlay != null) {
         int sw = cA.init_I();
         int sh = cA.load_I();
         if (sw > 0 && sh > 0) {
            String id = overlay.init_Str();
            OverlayBounds stashed = id == null ? null : this.read_Map.get(id);
            OverlayBounds basis = stashed != null ? stashed : this.init_cH_cX(overlay, sw, sh);
            if (basis != null) {
               basis = init_cH_cH_cH(basis, overlay.init_cH());
               OverlayBounds clamped = OverlayLayoutBase.init_cH_cH(basis, overlay.init_I(), overlay.load_I(), sw, sh);
               if (init_Z_cH_cH(clamped, basis)) {
                  if (stashed != null) {
                     this.read_Map.remove(id);
                     overlay.init_V_cH(basis);
                  }
               } else {
                  if (stashed == null && id != null) {
                     this.read_Map.put(id, basis);
                  }

                  overlay.init_V_cH(basis);
               }
            }
         }
      }
   }

   static OverlayBounds init_cH_cH_cH(OverlayBounds geometry, OverlayBounds live) {
      if (geometry == null) {
         return null;
      } else {
         return live == null ? geometry : new OverlayBounds(geometry.init_I, geometry.load_I, geometry.read, geometry.write, live.init_Z, live.load_Z);
      }
   }

   private OverlayBounds init_cH_cX(AutismOverlay overlay, int sw, int sh) {
      OverlayBounds current = overlay.init_cH();
      if (current == null) {
         return null;
      }

      OverlayBounds persisted = AutismSessionState.init_ck().init_cH_Str(overlay.init_Str());
      if (persisted == null) {
         return current;
      }

      OverlayBounds clampOfPersisted = OverlayLayoutBase.init_cH_cH(persisted, overlay.init_I(), overlay.load_I(), sw, sh);
      return !init_Z_cH_cH(persisted, clampOfPersisted) && init_Z_cH_cH(current, clampOfPersisted) ? persisted : current;
   }

   private static boolean init_Z_cH_cH(OverlayBounds a, OverlayBounds b) {
      return a != null && b != null && a.init_I == b.init_I && a.load_I == b.load_I && a.read == b.read && a.write == b.write;
   }

   public OverlayBounds init(String overlayId) {
      return overlayId == null ? null : this.read_Map.get(overlayId);
   }

   public void close() {
      if (!this.read_Map.isEmpty()) {
         int sw = cA.init_I();
         int sh = cA.load_I();
         if (sw > 0 && sh > 0) {
            for (AutismOverlay overlay : this.init_List) {
               if (overlay != null && overlay.init_Str() != null) {
                  OverlayBounds stashed = this.read_Map.get(overlay.init_Str());
                  if (stashed != null) {
                     stashed = init_cH_cH_cH(stashed, overlay.init_cH());
                     OverlayBounds clamped = OverlayLayoutBase.init_cH_cH(stashed, overlay.init_I(), overlay.load_I(), sw, sh);
                     if (init_Z_cH_cH(clamped, stashed)) {
                        this.read_Map.remove(overlay.init_Str());
                        overlay.init_V_cH(stashed);
                     }
                  }
               }
            }
         }
      }
   }

   private void load(boolean active) {
      if (this.update_Z != active) {
         this.update_Z = active;
         cf.init_V_J_Z(256L, active);
      }
   }

   public boolean write_Z() {
      return this.read_cX != null || this.write_cX != null;
   }

   public boolean read_Z(AutismOverlay overlay) {
      return overlay != null && this.write_cX == overlay;
   }

   private void init(Throwable t) {
      long now = System.currentTimeMillis();
      if (now - this.write_J >= 5000L) {
         this.write_J = now;
         AutismClientAddon.LOG.warn("[Overlay] render failed; skipped to protect the UI", t);
      }
   }

   public void init(GuiGraphicsExtractor context, int mouseX, int mouseY, float delta) {
      if (!OverlayScreenController.init_Z() && !autismclient.modules.aD.init_Z() && !sync_Z()) {
         Minecraft mc = Minecraft.getInstance();
         if (mc != null) {
            this.write_V();
            if (mc.getWindow() != null) {
               int sw = cA.init_I();
               int sh = cA.load_I();
               if (sw > 0 && sh > 0 && (sw != this.read_I || sh != this.write_I)) {
                  this.read_I = sw;
                  this.write_I = sh;
                  this.get();

                  for (AutismOverlay overlay : this.init_List) {
                     this.close_V(overlay);
                  }
               }
            }

            Screen screen = mc.gui.screen();
            this.init(screen);
            int virtualMouseX = (int)Math.round(cA.init_D_D(mouseX));
            int virtualMouseY = (int)Math.round(cA.init_D_D(mouseY));
            this.load_List.clear();

            for (AutismOverlay overlay : this.init_List) {
               if (this.init_Z_cX_Screen(overlay, screen)) {
                  this.load_List.add(overlay);
               }
            }

            boolean anyOverlayAnimating = false;

            for (AutismOverlay overlay : this.init_List) {
               if (!this.init_Z_cX_Screen(overlay, screen)) {
                  autismclient.gui.ui.widgets.OverlayWindowLayerAdapter adapter = this.init_Map.get(overlay);
                  if (adapter != null && adapter.load_Z() && !this.load_Z(overlay) && this.load(overlay, screen)) {
                     anyOverlayAnimating = true;
                     break;
                  }
               }
            }

            this.load(!this.load_List.isEmpty());
            if (this.load_List.isEmpty() && !anyOverlayAnimating) {
               for (autismclient.gui.ui.widgets.OverlayWindowLayerAdapter adapter : this.init_Map.values()) {
                  adapter.load(true);
               }
            } else {
               int hoveredOverlayIndex = -1;

               for (int i = this.load_List.size() - 1; i >= 0; i--) {
                  AutismOverlay overlay = this.load_List.get(i);
                  if (overlay.init_Z_D_D(virtualMouseX, virtualMouseY)) {
                     hoveredOverlayIndex = i;
                     break;
                  }
               }

               if (this.init_PoolEntry == null || this.init_PoolEntry.init() != mc.font) {
                  this.init_PoolEntry = autismclient.gui.f.UiHelper.init(mc.font);
               }

               autismclient.gui.f.SyncManager uiContext = new autismclient.gui.f.SyncManager(
                  context, this.init_TableIndex, this.init_PoolEntry, this.read_I, this.write_I, virtualMouseX, virtualMouseY, delta
               );
               int visibleIndex = 0;

               for (AutismOverlay overlay : this.init_List) {
                  autismclient.gui.ui.widgets.OverlayWindowLayerAdapter adapter = this.init_ArrayUtil(overlay);
                  boolean interactive = this.init_Z_cX_Screen(overlay, screen);
                  boolean animating = !interactive && adapter.load_Z() && !this.load_Z(overlay) && this.load(overlay, screen);
                  adapter.load(!interactive && !animating);
                  adapter.write(!interactive);
                  adapter.read(interactive && hoveredOverlayIndex > visibleIndex);
                  if (interactive) {
                     visibleIndex++;
                  }
               }

               this.add();
               cA.init_V_GuiGraphicsExtract(context);

               try {
                  autismclient.gui.ui.UiClipStack.init_HashUtil().load(context);

                  try {
                     this.init_ByteReader.init_V_SyncManager(uiContext);
                  } catch (Throwable t) {
                     autismclient.gui.ui.UiClipStack.init_HashUtil().load(context);
                     this.init(t);
                  }
               } finally {
                  autismclient.gui.ui.UiClipStack.init_HashUtil().load(context);
                  cA.load_V_GuiGraphicsExtract(context);
                  this.load_List.clear();
               }
            }
         }
      }
   }

   public boolean init_Z_D_D(double mouseX, double mouseY) {
      if (autismclient.modules.aD.init_Z()) {
         return false;
      } else {
         return sync_Z() ? false : this.read(cA.init_D_D(mouseX), cA.init_D_D(mouseY));
      }
   }

   private boolean read(double mouseX, double mouseY) {
      for (AutismOverlay overlay : this.init_List) {
         if (this.sync_Z(overlay) && overlay.init_Z_D_D(mouseX, mouseY)) {
            return true;
         }
      }

      return false;
   }

   public boolean load(double mouseX, double mouseY) {
      if (autismclient.modules.aD.init_Z()) {
         return false;
      }

      if (sync_Z()) {
         return false;
      }

      if (this.init_List.isEmpty()) {
         return false;
      }

      if (!this.update_Z) {
         return false;
      }

      long now = System.nanoTime();
      if (Double.compare(mouseX, this.load_D) == 0 && Double.compare(mouseY, this.read_D) == 0 && now - this.init_J < 16000000L) {
         return this.write_Z;
      }

      boolean result = this.read(cA.init_D_D(mouseX), cA.init_D_D(mouseY));
      this.load_D = mouseX;
      this.read_D = mouseY;
      this.init_J = now;
      this.write_Z = result;
      return result;
   }

   private void get() {
      this.load_D = 0.0 / 0.0;
      this.read_D = 0.0 / 0.0;
      this.init_J = 0L;
      this.write_Z = false;
   }

   private void set() {
      for (AutismOverlay overlay : this.init_List) {
         if (this.sync_Z(overlay) && overlay.write_Z()) {
            overlay.push_V();
         }
      }

      this.load_cX = null;
      this.read_Z = false;
   }

   private AutismOverlay init_cX_D_D(double mouseX, double mouseY) {
      for (int i = this.init_List.size() - 1; i >= 0; i--) {
         AutismOverlay overlay = this.init_List.get(i);
         if (this.sync_Z(overlay) && overlay.init_Z_D_D(mouseX, mouseY)) {
            return overlay;
         }
      }

      return null;
   }

   public boolean write_Z(AutismOverlay overlay) {
      if (overlay != null && !this.init_List.isEmpty()) {
         for (int i = this.init_List.size() - 1; i >= 0; i--) {
            AutismOverlay candidate = this.init_List.get(i);
            if (this.sync_Z(candidate)) {
               return candidate == overlay;
            }
         }

         return false;
      } else {
         return false;
      }
   }

   public boolean update_Z(AutismOverlay overlay) {
      return overlay != null && overlay == this.init_cX;
   }

   public boolean init_Z_D_2(double mouseX, double mouseY, int button) {
      if (autismclient.modules.aD.init_Z()) {
         return false;
      }

      if (sync_Z()) {
         return false;
      }

      this.flush_Z = false;
      mouseX = cA.init_D_D(mouseX);
      mouseY = cA.init_D_D(mouseY);
      AutismOverlay topOverlay = this.init_cX_D_D(mouseX, mouseY);
      if (topOverlay == null) {
         this.set();
         this.init_cX = null;
         AutismSessionState.init_ck().init_V_Str("");
         this.update_cX = null;
         this.sync_Z = false;
         this.init_cH = null;
         this.flush_Z = true;
         return false;
      }

      this.set();
      if (button == 0) {
         if (topOverlay.read_Z_D_D(mouseX, mouseY)) {
            long now = System.currentTimeMillis();
            if (topOverlay.read_Z() && this.sync_cX == topOverlay && now - this.load_J <= 350L) {
               this.sync_cX = null;
               topOverlay.run_V();
               this.update_V(topOverlay);
               this.get();
               return true;
            }

            this.sync_cX = topOverlay;
            this.load_J = now;
            this.write_cX = topOverlay;
            this.load_cH = topOverlay.init_cH();
            this.sync_D = mouseX;
            this.flush_D = mouseY;
            this.update_V(topOverlay);
            return true;
         }

         if (topOverlay.load_Z_D_D(mouseX, mouseY)) {
            this.read_cX = topOverlay;
            this.read_cH = topOverlay.init_cH();
            if (topOverlay.load_Z()) {
               this.update_cX = topOverlay;
               this.sync_Z = false;
               this.write_D = mouseX;
               this.update_D = mouseY;
               this.init_cH = topOverlay.init_cH();
            } else {
               this.update_cX = null;
               this.sync_Z = false;
               this.init_cH = null;
            }

            this.update_V(topOverlay);
            this.init_ArrayUtil(topOverlay).init_EventBus_I_2((int)mouseX, (int)mouseY, button);
            this.open(topOverlay);
            return true;
         }
      }

      this.update_cX = null;
      this.sync_Z = false;
      this.init_cH = null;
      this.update_V(topOverlay);
      this.put();
      autismclient.gui.ui.widgets.OverlayWindowLayerAdapter topComponent = this.init_ArrayUtil(topOverlay);
      autismclient.gui.f.NetHelper topHitBounds = topComponent.load_NetHelper();
      if (topHitBounds != null && topHitBounds.init_Z((int)mouseX, (int)mouseY)) {
         this.init_FileCache.init_Z_I_2((int)mouseX, (int)mouseY, button);
      } else {
         topComponent.init_EventBus_I_2((int)mouseX, (int)mouseY, button);
      }

      this.open(topOverlay);
      return true;
   }

   public boolean load(double mouseX, double mouseY, int button) {
      if (autismclient.modules.aD.init_Z()) {
         return false;
      }

      if (sync_Z()) {
         return false;
      }

      mouseX = cA.init_D_D(mouseX);
      mouseY = cA.init_D_D(mouseY);
      boolean wasDraggingOrResizing = this.read_cX != null || this.write_cX != null;
      AutismOverlay prevDragging = this.read_cX;
      AutismOverlay prevResizing = this.write_cX;
      boolean shouldToggleHeaderCollapse = button == 0
         && prevDragging != null
         && prevDragging == this.update_cX
         && prevDragging.load_Z()
         && !this.sync_Z
         && Math.abs(mouseX - this.write_D) < 3.0
         && Math.abs(mouseY - this.update_D) < 3.0
         && prevDragging.load_Z_D_D(mouseX, mouseY);
      OverlayBounds headerStartBounds = this.init_cH;
      if (button == 0) {
         if (prevDragging != null && prevDragging.init_Str() != null && this.read_cH != null && !init_Z_cH_cH(prevDragging.init_cH(), this.read_cH)) {
            this.read_Map.remove(prevDragging.init_Str());
         }

         if (prevResizing != null && prevResizing.init_Str() != null) {
            this.read_Map.remove(prevResizing.init_Str());
         }

         this.read_cX = null;
         this.read_cH = null;
         if (this.write_cX != null) {
            this.write_cX.tick_V();
            this.write_cX.a__V();
         }

         this.write_cX = null;
         this.load_cH = null;
         this.update_cX = null;
         this.sync_Z = false;
         this.init_cH = null;
      }

      if (button == 0 && prevDragging != null) {
         this.init_ArrayUtil(prevDragging).load((int)mouseX, (int)mouseY, button);
      }

      if (button == 0 && prevResizing != null && prevResizing != prevDragging) {
         this.init_ArrayUtil(prevResizing).load((int)mouseX, (int)mouseY, button);
      }

      if (shouldToggleHeaderCollapse && this.sync_Z(prevDragging)) {
         if (headerStartBounds != null) {
            OverlayBounds current = prevDragging.init_cH();
            prevDragging.init_V_cH(new OverlayBounds(headerStartBounds.init_I, headerStartBounds.load_I, current.read, current.write, current.init_Z, current.load_Z));
         }

         prevDragging.start_V();
         prevDragging.a__V();
         this.get();
         return true;
      } else {
         if (wasDraggingOrResizing) {
            return true;
         }

         if (this.flush_Z) {
            this.flush_Z = false;
            return false;
         }

         for (int i = this.init_List.size() - 1; i >= 0; i--) {
            AutismOverlay overlay = this.init_List.get(i);
            if (this.sync_Z(overlay) && this.init_ArrayUtil(overlay).load((int)mouseX, (int)mouseY, button) == autismclient.gui.ui.UiInputResult.HANDLED) {
               return true;
            }
         }

         return this.read(mouseX, mouseY);
      }
   }

   public boolean init(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
      if (autismclient.modules.aD.init_Z()) {
         return false;
      }

      if (sync_Z()) {
         return false;
      }

      mouseX = cA.init_D_D(mouseX);
      mouseY = cA.init_D_D(mouseY);
      deltaX = cA.init_D_D(deltaX);
      deltaY = cA.init_D_D(deltaY);
      if (button == 0 && this.write_cX != null && this.load_cH != null && this.sync_Z(this.write_cX)) {
         this.write_cX.init_V_I_I(this.load_cH.read + (int)Math.round(mouseX - this.sync_D), this.load_cH.write + (int)Math.round(mouseY - this.flush_D));
         this.get();
         return true;
      }

      if (button == 0 && this.sync_Z(this.read_cX)) {
         if (this.read_cX == this.update_cX && !this.sync_Z) {
            if (Math.abs(mouseX - this.write_D) >= 3.0 || Math.abs(mouseY - this.update_D) >= 3.0) {
               this.sync_Z = true;
            }

            if (!this.sync_Z) {
               return true;
            }
         }

         boolean handled = this.init_ArrayUtil(this.read_cX).init_EventBus_I((int)mouseX, (int)mouseY, button, deltaX, deltaY)
            == autismclient.gui.ui.UiInputResult.HANDLED;
         this.get();
         return handled;
      } else {
         if (this.flush_Z) {
            return false;
         }

         for (int i = this.init_List.size() - 1; i >= 0; i--) {
            AutismOverlay overlay = this.init_List.get(i);
            if (this.sync_Z(overlay)
               && this.init_ArrayUtil(overlay).init_EventBus_I((int)mouseX, (int)mouseY, button, deltaX, deltaY) == autismclient.gui.ui.UiInputResult.HANDLED) {
               return true;
            }
         }

         return this.read(mouseX, mouseY);
      }
   }

   public boolean init_Z_D(double mouseX, double mouseY, double amount) {
      if (autismclient.modules.aD.init_Z()) {
         return false;
      }

      if (sync_Z()) {
         return false;
      }

      mouseX = cA.init_D_D(mouseX);
      mouseY = cA.init_D_D(mouseY);
      AutismOverlay topOverlay = this.init_cX_D_D(mouseX, mouseY);
      if (topOverlay == null) {
         return false;
      }

      this.update_V(topOverlay);
      this.put();
      autismclient.gui.ui.widgets.OverlayWindowLayerAdapter topComponent = this.init_ArrayUtil(topOverlay);
      autismclient.gui.f.NetHelper topHitBounds = topComponent.load_NetHelper();
      if (topHitBounds != null && topHitBounds.init_Z((int)mouseX, (int)mouseY)) {
         this.init_FileCache.init_Z_I((int)mouseX, (int)mouseY, amount);
      } else {
         topComponent.init_EventBus_I((int)mouseX, (int)mouseY, amount);
      }

      return true;
   }

   public boolean init_Z_I(int keyCode, int scanCode, int modifiers) {
      if (autismclient.modules.aD.init_Z()) {
         return false;
      }

      if (sync_Z()) {
         return false;
      }

      for (int i = this.init_List.size() - 1; i >= 0; i--) {
         AutismOverlay overlay = this.init_List.get(i);
         if (this.sync_Z(overlay) && overlay.e_() && this.init_ArrayUtil(overlay).read(keyCode, scanCode, modifiers) == autismclient.gui.ui.UiInputResult.HANDLED) {
            this.update_V(overlay);
            this.open(overlay);
            return true;
         }
      }

      AutismOverlay focusedTextOverlay = this.init_cX();
      if (focusedTextOverlay != null) {
         this.init_ArrayUtil(focusedTextOverlay).read(keyCode, scanCode, modifiers);
         this.open(focusedTextOverlay);
         this.init_cX = focusedTextOverlay;
         AutismSessionState.init_ck().init_V_Str(focusedTextOverlay.init_Str());
         return true;
      } else {
         AutismOverlay keyboardTarget = this.load_cX();
         if (keyboardTarget != null && this.init_ArrayUtil(keyboardTarget).read(keyCode, scanCode, modifiers) == autismclient.gui.ui.UiInputResult.HANDLED) {
            this.open(keyboardTarget);
            return true;
         } else {
            return this.update_Z();
         }
      }
   }

   public boolean init_Z_C_I(char chr, int modifiers) {
      if (autismclient.modules.aD.init_Z()) {
         return false;
      } else if (sync_Z()) {
         return false;
      } else {
         AutismOverlay focusedTextOverlay = this.init_cX();
         if (focusedTextOverlay != null) {
            this.init_ArrayUtil(focusedTextOverlay).init(chr, modifiers);
            this.open(focusedTextOverlay);
            this.init_cX = focusedTextOverlay;
            AutismSessionState.init_ck().init_V_Str(focusedTextOverlay.init_Str());
            return true;
         } else {
            AutismOverlay keyboardTarget = this.load_cX();
            if (keyboardTarget != null && this.init_ArrayUtil(keyboardTarget).init(chr, modifiers) == autismclient.gui.ui.UiInputResult.HANDLED) {
               this.open(keyboardTarget);
               return true;
            } else {
               return this.update_Z();
            }
         }
      }
   }

   private AutismOverlay init_cX() {
      if (!this.read_Z) {
         AutismOverlay cached = this.load_cX;
         if (cached != null && this.sync_Z(cached) && cached.write_Z()) {
            return cached;
         }
      }

      for (int i = this.init_List.size() - 1; i >= 0; i--) {
         AutismOverlay overlay = this.init_List.get(i);
         if (this.sync_Z(overlay) && overlay.write_Z()) {
            this.load_cX = overlay;
            this.read_Z = false;
            return overlay;
         }
      }

      this.load_cX = null;
      this.read_Z = false;
      return null;
   }

   private void open(AutismOverlay overlay) {
      this.load_cX = this.sync_Z(overlay) && overlay.write_Z() ? overlay : null;
      this.read_Z = false;
   }

   private AutismOverlay load_cX() {
      if (this.sync_Z(this.init_cX)) {
         return this.init_cX;
      }

      for (int i = this.init_List.size() - 1; i >= 0; i--) {
         AutismOverlay overlay = this.init_List.get(i);
         if (this.sync_Z(overlay)) {
            return overlay;
         }
      }

      return null;
   }

   public boolean update_Z() {
      if (autismclient.modules.aD.init_Z()) {
         return false;
      } else {
         return sync_Z() ? false : this.init_cX() != null;
      }
   }

   public void open() {
      this.set();
      this.init_cX = null;
      AutismSessionState.init_ck().init_V_Str("");
   }

   private autismclient.gui.ui.widgets.OverlayWindowLayerAdapter init_ArrayUtil(AutismOverlay overlay) {
      autismclient.gui.ui.widgets.OverlayWindowLayerAdapter adapter = this.init_Map.get(overlay);
      if (adapter == null) {
         adapter = new autismclient.gui.ui.widgets.OverlayWindowLayerAdapter(overlay);
         adapter.init_V_Z(!this.close_Z(overlay));
         this.init_Map.put(overlay, adapter);
      }

      return adapter;
   }

   private void init(Screen screen) {
      boolean contextSwitch = this.init_Z || screen != this.init_WeakReference.get();
      this.init_Z = false;
      if (screen != this.init_WeakReference.get()) {
         this.init_WeakReference = new WeakReference<>(screen);
      }

      if (contextSwitch) {
         for (AutismOverlay pending : new ArrayList<>(this.load_Set)) {
            this.flush_V(pending);
         }
      }

      for (autismclient.gui.ui.widgets.OverlayWindowLayerAdapter adapter : this.init_Map.values()) {
         if (contextSwitch) {
            adapter.load_V();
         } else {
            adapter.init_V();
         }
      }

      if (!this.load_Set.isEmpty()) {
         for (AutismOverlay pending : new ArrayList<>(this.load_Set)) {
            autismclient.gui.ui.widgets.OverlayWindowLayerAdapter adapter = this.init_Map.get(pending);
            if (pending.sync_Z()) {
               this.load_Set.remove(pending);
            } else if (adapter == null || !adapter.load_Z()) {
               this.flush_V(pending);
            }
         }
      }
   }

   private void put() {
      for (AutismOverlay overlay : this.init_List) {
         this.init_ArrayUtil(overlay).write(!this.sync_Z(overlay));
      }

      this.add();
   }

   private void add() {
      if (this.load_Z) {
         this.init_ByteReader.init();

         for (AutismOverlay overlay : this.init_List) {
            this.init_ByteReader
               .init(this.close_Z(overlay) ? autismclient.gui.ui.UiLayer.DROPDOWN : autismclient.gui.ui.UiLayer.FLOATING, this.init_ArrayUtil(overlay));
         }

         this.load_Z = false;
      }
   }
}

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
   private static final OverlayManager INSTANCE = new OverlayManager();
   public static final int OFFSCREEN_COORD = -10000;
   private static final double HEADER_DRAG_THRESHOLD = 3.0;
   private final List<AutismOverlay> overlays = new CopyOnWriteArrayList<>();
   private final List<AutismOverlay> visibleOverlaysScratch = new ArrayList<>();
   private final Map<AutismOverlay, autismclient.gui.ui.widgets.OverlayWindowLayerAdapter> adapters = new IdentityHashMap<>();
   private final Map<AutismOverlay, AutismOverlay.RenderLayer> renderLayers = new IdentityHashMap<>();
   private final Set<String> hiddenOverlayIds = new HashSet<>();
   private final Set<AutismOverlay> closingOverlays = Collections.newSetFromMap(new IdentityHashMap<>());
   private boolean screenContextDirty = true;
   private WeakReference<Screen> lastScreenRef = new WeakReference<>(null);
   private final autismclient.gui.ui.UiTheme uiTheme = new autismclient.gui.ui.UiTheme();
   private final autismclient.gui.ui.UiLayerHost layerHost = new autismclient.gui.ui.UiLayerHost();
   private final autismclient.gui.ui.UiLayerInputRouter layerInputRouter = new autismclient.gui.ui.UiLayerInputRouter(this.layerHost);
   private autismclient.gui.ui.UiTextRenderer textRenderer;
   private boolean layerHostDirty = true;
   private AutismOverlay focusedOverlay = null;
   private AutismOverlay cachedTextInputOverlay;
   private boolean textInputCacheDirty = true;
   private double cachedHoverX = 0.0 / 0.0;
   private double cachedHoverY = 0.0 / 0.0;
   private long cachedHoverNanos;
   private boolean cachedHoverResult;
   private final Map<String, OverlayBounds> stashedBoundsById = new HashMap<>();
   private static final int MAX_STASHED_BOUNDS = 64;
   private AutismOverlay draggingOverlay = null;
   private AutismOverlay resizingOverlay = null;
   private AutismOverlay headerClickCandidate = null;
   private int lastScreenWidth = -1;
   private int lastScreenHeight = -1;
   private volatile boolean active = false;
   private boolean headerDragExceededThreshold = false;
   private boolean clickedOutsideOverlays = false;
   private OverlayBounds headerClickStartBounds = null;
   private OverlayBounds resizeStartBounds = null;
   private OverlayBounds dragStartBounds = null;
   private double headerClickStartX = 0.0;
   private double headerClickStartY = 0.0;
   private double resizeStartX = 0.0;
   private double resizeStartY = 0.0;
   private AutismOverlay lastResizeGripOverlay;
   private long lastResizeGripClickMs;
   private static final long DOUBLE_CLICK_WINDOW_MS = 350L;
   private long lastRenderErrorLogMs;

   private OverlayManager() {
   }

   public static OverlayManager getInstance() {
      return INSTANCE;
   }

   public List<AutismOverlay> getOverlays() {
      return this.overlays;
   }

   public boolean hasOverlays() {
      return !this.overlays.isEmpty();
   }

   public boolean isActive() {
      if (autismclient.modules.aD.init_Z()) {
         return false;
      } else {
         return isLocked() ? false : this.active;
      }
   }

   private static boolean isLocked() {
      return !AutismLiteVariant.enabled() && !autismclient.e.PacketUtil.init();
   }

   public String debugSummary() {
      StringBuilder visible = new StringBuilder();
      int visibleCount = 0;

      for (AutismOverlay overlay : this.overlays) {
         if (this.isVisibleNow(overlay)) {
            if (visibleCount++ > 0) {
               visible.append(", ");
            }

            visible.append(overlay.init_Str());
         }
      }

      return "registered=" + this.overlays.size() + " visible=" + visibleCount + " [" + visible + "]";
   }

   public void register(AutismOverlay overlay) {
      this.register(overlay, this.resolveRenderLayer(overlay));
   }

   public void refreshRenderLayer(AutismOverlay overlay) {
      if (overlay != null && this.overlays.contains(overlay)) {
         this.renderLayers.put(overlay, overlay.init_cX_DataUtil());
      }
   }

   public void register(AutismOverlay overlay, AutismOverlay.RenderLayer scope) {
      if (overlay != null) {
         this.removeDuplicateId(overlay);
         if (!this.overlays.contains(overlay)) {
            this.overlays.add(overlay);
         }

         this.renderLayers.put(overlay, scope == null ? overlay.init_cX_DataUtil() : scope);
         this.closingOverlays.remove(overlay);
         this.getOrCreateAdapter(overlay);
         this.layerHostDirty = true;
         this.textInputCacheDirty = true;
         this.setActive(true);
         this.invalidateHoverCache();
         this.restoreSavedOrder();
         this.keepLaunchersFirst();
         if (overlay instanceof AutismLauncherOverlay || "autism-launcher".equals(overlay.init_Str())) {
            this.reclampAll();
         }
      }
   }

   public void unregister(AutismOverlay overlay) {
      if (overlay != null) {
         autismclient.gui.ui.widgets.OverlayWindowLayerAdapter adapter = this.adapters.get(overlay);
         if (adapter != null && this.overlays.contains(overlay) && !overlay.sync_Z()) {
            adapter.init_V();
            Minecraft mc = Minecraft.getInstance();
            Screen screen = mc == null ? null : mc.gui.screen();
            if (adapter.init_Z() && !this.isHidden(overlay) && this.isAllowedOnScreen(overlay, screen)) {
               this.closingOverlays.add(overlay);
               if (this.focusedOverlay == overlay) {
                  this.focusedOverlay = null;
               }

               if (this.cachedTextInputOverlay == overlay) {
                  this.cachedTextInputOverlay = null;
               }

               this.textInputCacheDirty = true;
               this.invalidateHoverCache();
               return;
            }
         }

         this.removeImmediate(overlay);
      }
   }

   private void removeDuplicateId(AutismOverlay incoming) {
      String id = incoming.init_Str();
      if (id != null && !id.isEmpty()) {
         for (AutismOverlay existing : this.overlays) {
            if (existing != incoming && id.equals(existing.init_Str())) {
               this.removeImmediate(existing);
            }
         }
      }
   }

   private void removeImmediate(AutismOverlay overlay) {
      this.closingOverlays.remove(overlay);
      this.overlays.remove(overlay);
      this.adapters.remove(overlay);
      this.renderLayers.remove(overlay);
      this.layerHostDirty = true;
      this.hiddenOverlayIds.remove(overlay.init_Str());
      if (this.focusedOverlay == overlay) {
         this.focusedOverlay = null;
      }

      if (this.cachedTextInputOverlay == overlay) {
         this.cachedTextInputOverlay = null;
      }

      overlay.push_V();
      if (this.draggingOverlay == overlay) {
         this.draggingOverlay = null;
         this.dragStartBounds = null;
      }

      if (this.resizingOverlay == overlay) {
         this.resizingOverlay = null;
         this.resizeStartBounds = null;
      }

      if (this.headerClickCandidate == overlay) {
         this.headerClickCandidate = null;
         this.headerClickStartBounds = null;
         this.headerDragExceededThreshold = false;
      }

      this.textInputCacheDirty = true;
      if (this.overlays.isEmpty()) {
         this.setActive(false);
      }

      this.invalidateHoverCache();
      this.saveOrderToSession();
   }

   public void markClosing(AutismOverlay overlay) {
      autismclient.gui.ui.widgets.OverlayWindowLayerAdapter adapter = overlay == null ? null : this.adapters.get(overlay);
      if (adapter != null) {
         adapter.read();
      }
   }

   public void invalidateScreenContext() {
      this.screenContextDirty = true;
   }

   public boolean isClosing(AutismOverlay overlay) {
      autismclient.gui.ui.widgets.OverlayWindowLayerAdapter adapter = overlay == null ? null : this.adapters.get(overlay);
      if (adapter == null) {
         return false;
      }

      adapter.init_V();
      return adapter.init_Z();
   }

   public void clearNonPersistent() {
      if (!OverlayScreenController.read_Z()) {
         for (AutismOverlay pending : new ArrayList<>(this.closingOverlays)) {
            this.removeImmediate(pending);
         }

         this.screenContextDirty = true;
         this.overlays.removeIf(overlay -> {
            if (overlay.f_()) {
               return false;
            }

            if (!AutismLiteVariant.enabled() && overlay instanceof AutismProfilesOverlay) {
               overlay.init_V_Z(false);
            }

            this.adapters.remove(overlay);
            this.renderLayers.remove(overlay);
            return true;
         });
         this.layerHost.init();
         this.layerHostDirty = true;
         this.hiddenOverlayIds.clear();
         this.draggingOverlay = null;
         this.dragStartBounds = null;
         this.resizingOverlay = null;
         this.headerClickCandidate = null;
         this.focusedOverlay = null;
         this.cachedTextInputOverlay = null;
         this.textInputCacheDirty = false;
         this.headerDragExceededThreshold = false;
         this.headerClickStartBounds = null;
         this.resizeStartBounds = null;
         this.clickedOutsideOverlays = false;
         this.setActive(!this.overlays.isEmpty());
         this.invalidateHoverCache();
      }
   }

   public void setHidden(AutismOverlay overlay, boolean hidden) {
      if (overlay != null) {
         String id = overlay.init_Str();
         if (id != null && !id.isEmpty()) {
            if (hidden) {
               this.hiddenOverlayIds.add(id);
               overlay.push_V();
               if (this.focusedOverlay == overlay) {
                  this.focusedOverlay = null;
               }

               if (this.draggingOverlay == overlay) {
                  this.draggingOverlay = null;
                  this.dragStartBounds = null;
               }

               if (this.resizingOverlay == overlay) {
                  this.resizingOverlay = null;
               }

               if (this.headerClickCandidate == overlay) {
                  this.headerClickCandidate = null;
               }
            } else {
               this.hiddenOverlayIds.remove(id);
            }

            this.textInputCacheDirty = true;
            this.invalidateHoverCache();
         }
      }
   }

   public void unhideAll() {
      this.hiddenOverlayIds.clear();
      this.textInputCacheDirty = true;
      this.invalidateHoverCache();
   }

   public boolean isHidden(AutismOverlay overlay) {
      if (overlay == null) {
         return false;
      }

      String id = overlay.init_Str();
      return id != null && this.hiddenOverlayIds.contains(id);
   }

   private boolean isVisibleNow(AutismOverlay overlay) {
      Minecraft mc = Minecraft.getInstance();
      return this.isVisibleOnScreen(overlay, mc == null ? null : mc.gui.screen());
   }

   private boolean isVisibleOnScreen(AutismOverlay overlay, Screen screen) {
      return !OverlayScreenController.init_Z() && overlay != null && overlay.sync_Z() && !this.isHidden(overlay) && this.isAllowedOnScreen(overlay, screen);
   }

   private AutismOverlay.RenderLayer resolveRenderLayer(AutismOverlay overlay) {
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

   private boolean isAllowedOnScreen(AutismOverlay overlay, Screen screen) {
      AutismOverlay.RenderLayer scope = this.renderLayers.getOrDefault(overlay, overlay == null ? AutismOverlay.RenderLayer.HOST_SCREEN : overlay.init_cX_DataUtil());
      if (scope == AutismOverlay.RenderLayer.BACKGROUND_STATUS) {
         return true;
      }

      if (screen == null || screen instanceof ChatScreen || screen instanceof InBedChatScreen) {
         return false;
      }

      if (!AutismLiteVariant.enabled() && screen instanceof autismclient.gui.screens.AutismModulesScreen) {
         return !this.isLauncherOverlay(overlay);
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

   public void blurOffscreenOverlays() {
      Minecraft mc = Minecraft.getInstance();
      Screen screen = mc == null ? null : mc.gui.screen();
      boolean changed = false;

      for (AutismOverlay overlay : this.overlays) {
         if (overlay != null && overlay.sync_Z() && !this.isAllowedOnScreen(overlay, screen)) {
            overlay.push_V();
            if (this.focusedOverlay == overlay) {
               this.focusedOverlay = null;
            }

            if (this.draggingOverlay == overlay) {
               this.draggingOverlay = null;
               this.dragStartBounds = null;
            }

            if (this.resizingOverlay == overlay) {
               this.resizingOverlay = null;
            }

            if (this.headerClickCandidate == overlay) {
               this.headerClickCandidate = null;
            }

            changed = true;
         }
      }

      if (changed) {
         this.textInputCacheDirty = true;
         this.invalidateHoverCache();
      }
   }

   public void closeAll() {
      this.dismissOverlays(false);
   }

   public void minimizeAll() {
      this.dismissOverlays(true);
   }

   private void dismissOverlays(boolean rememberWindows) {
      this.hiddenOverlayIds.clear();

      for (AutismOverlay overlay : this.overlays) {
         if (overlay != null && this.renderLayers.getOrDefault(overlay, overlay.init_cX_DataUtil()) != AutismOverlay.RenderLayer.BACKGROUND_STATUS) {
            if (overlay.sync_Z()) {
               if (rememberWindows && overlay.update_Z()) {
                  this.setHidden(overlay, true);
               } else {
                  overlay.init_V_Z(false);
               }
            }

            overlay.push_V();
         }
      }

      this.draggingOverlay = null;
      this.dragStartBounds = null;
      this.resizingOverlay = null;
      this.headerClickCandidate = null;
      this.focusedOverlay = null;
      this.cachedTextInputOverlay = null;
      this.textInputCacheDirty = false;
      this.clickedOutsideOverlays = false;
      this.invalidateHoverCache();
   }

   public boolean hasVisibleWindows() {
      for (AutismOverlay overlay : this.overlays) {
         if (overlay != null && overlay.sync_Z() && !this.isHidden(overlay) && !this.isLauncherOverlay(overlay) && overlay.init_cX_DataUtil() != AutismOverlay.RenderLayer.BACKGROUND_STATUS) {
            return true;
         }
      }

      return false;
   }

   public void bringToFront(AutismOverlay overlay) {
      if (overlay != null) {
         this.removeDuplicateId(overlay);
         this.overlays.remove(overlay);
         this.overlays.add(overlay);
         this.layerHostDirty = true;
         this.setActive(true);
         this.focusedOverlay = overlay;
         this.textInputCacheDirty = true;
         AutismSessionState.init_ck().init_V_Str(overlay.init_Str());
         this.invalidateHoverCache();
         this.keepLaunchersFirst();
         this.saveOrderToSession();
      }
   }

   public void bringOwnerToFront(Object childComponent) {
      if (childComponent != null) {
         for (AutismOverlay overlay : this.overlays) {
            if (overlay instanceof AutismCustomFilterOverlay filterOverlay && filterOverlay.init_AutismPacketSelect() == childComponent) {
               this.bringToFront(overlay);
               return;
            }
         }
      }
   }

   private void restoreSavedOrder() {
      if (this.overlays.size() < 2) {
         this.restoreFocusFromSession();
         this.keepLaunchersFirst();
      } else {
         List<String> savedOrder = AutismSessionState.init_ck().init_List();
         if (savedOrder.isEmpty()) {
            this.restoreFocusFromSession();
            this.keepLaunchersFirst();
         } else {
            String focusedId = AutismSessionState.init_ck().init_Str();
            Map<String, Integer> positions = new HashMap<>();

            for (int i = 0; i < savedOrder.size(); i++) {
               positions.putIfAbsent(savedOrder.get(i), i);
            }

            List<AutismOverlay> ordered = new ArrayList<>(this.overlays);
            ordered.sort(
               Comparator.<AutismOverlay>comparingInt(overlay -> positions.getOrDefault(overlay.init_Str(), 2147483647))
                  .thenComparingInt(overlay -> focusedId.equals(overlay.init_Str()) ? 1 : 0)
            );
            this.overlays.clear();
            this.overlays.addAll(ordered);
            this.layerHostDirty = true;
            this.restoreFocusFromSession();
            this.keepLaunchersFirst();
         }
      }
   }

   private void restoreFocusFromSession() {
      String focusedId = AutismSessionState.init_ck().init_Str();
      if (focusedId.isEmpty()) {
         this.focusedOverlay = null;
      } else {
         this.focusedOverlay = null;

         for (int i = this.overlays.size() - 1; i >= 0; i--) {
            AutismOverlay overlay = this.overlays.get(i);
            if (overlay != null && focusedId.equals(overlay.init_Str()) && this.isVisibleNow(overlay)) {
               this.focusedOverlay = overlay;
               break;
            }
         }
      }
   }

   private void saveOrderToSession() {
      List<String> order = new ArrayList<>(this.overlays.size());

      for (AutismOverlay overlay : this.overlays) {
         String id = overlay.init_Str();
         if (id != null && !id.isEmpty() && !order.contains(id) && !this.isLauncherOverlay(overlay) && !this.isStepPickerOverlay(overlay)) {
            order.add(id);
         }
      }

      AutismSessionState.init_ck().init_V_List(order);
   }

   private void keepLaunchersFirst() {
      if (!this.overlays.isEmpty()) {
         List<AutismOverlay> launchers = new ArrayList<>();
         List<AutismOverlay> others = new ArrayList<>();

         for (AutismOverlay overlay : this.overlays) {
            if (this.isLauncherOverlay(overlay)) {
               launchers.add(overlay);
            } else {
               others.add(overlay);
            }
         }

         if (!launchers.isEmpty()) {
            this.overlays.clear();
            this.overlays.addAll(launchers);
            this.overlays.addAll(others);
            this.layerHostDirty = true;
         }
      }
   }

   private boolean isLauncherOverlay(AutismOverlay overlay) {
      return overlay instanceof AutismLauncherOverlay || overlay != null && "autism-launcher".equals(overlay.init_Str());
   }

   private boolean isStepPickerOverlay(AutismOverlay overlay) {
      return overlay != null && "macro-step-picker".equals(overlay.init_Str());
   }

   public void reclampAll() {
      for (AutismOverlay overlay : this.overlays) {
         this.reclampOverlay(overlay);
      }

      this.pruneStashedBounds();
      this.invalidateHoverCache();
   }

   private void pruneStashedBounds() {
      if (!this.stashedBoundsById.isEmpty()) {
         int sw = cA.init_I();
         int sh = cA.load_I();
         if (sw > 0 && sh > 0) {
            this.stashedBoundsById.values().removeIf(stashed -> fitsInViewport(stashed, sw, sh));
            if (this.stashedBoundsById.size() > 64) {
               Set<String> live = new HashSet<>();

               for (AutismOverlay overlay : this.overlays) {
                  if (overlay != null && overlay.init_Str() != null) {
                     live.add(overlay.init_Str());
                  }
               }

               this.stashedBoundsById.keySet().removeIf(id -> !live.contains(id));
            }
         }
      }
   }

   static boolean fitsInViewport(OverlayBounds bounds, int screenWidth, int screenHeight) {
      return bounds != null && screenWidth > 0 && screenHeight > 0
         ? sameGeometry(OverlayLayoutBase.clampBounds(bounds, bounds.width, bounds.height, screenWidth, screenHeight), bounds)
         : false;
   }

   private void reclampOverlay(AutismOverlay overlay) {
      if (overlay != null) {
         int sw = cA.init_I();
         int sh = cA.load_I();
         if (sw > 0 && sh > 0) {
            String id = overlay.init_Str();
            OverlayBounds stashed = id == null ? null : this.stashedBoundsById.get(id);
            OverlayBounds basis = stashed != null ? stashed : this.resolveBasisBounds(overlay, sw, sh);
            if (basis != null) {
               basis = mergeGeometryWithLiveFlags(basis, overlay.init_cH());
               OverlayBounds clamped = OverlayLayoutBase.clampBounds(basis, overlay.init_I(), overlay.load_I(), sw, sh);
               if (sameGeometry(clamped, basis)) {
                  if (stashed != null) {
                     this.stashedBoundsById.remove(id);
                     overlay.init_V_cH(basis);
                  }
               } else {
                  if (stashed == null && id != null) {
                     this.stashedBoundsById.put(id, basis);
                  }

                  overlay.init_V_cH(basis);
               }
            }
         }
      }
   }

   static OverlayBounds mergeGeometryWithLiveFlags(OverlayBounds geometry, OverlayBounds live) {
      if (geometry == null) {
         return null;
      } else {
         return live == null ? geometry : new OverlayBounds(geometry.x, geometry.y, geometry.width, geometry.height, live.visible, live.collapsed);
      }
   }

   private OverlayBounds resolveBasisBounds(AutismOverlay overlay, int sw, int sh) {
      OverlayBounds current = overlay.init_cH();
      if (current == null) {
         return null;
      }

      OverlayBounds persisted = AutismSessionState.init_ck().init_cH_Str(overlay.init_Str());
      if (persisted == null) {
         return current;
      }

      OverlayBounds clampOfPersisted = OverlayLayoutBase.clampBounds(persisted, overlay.init_I(), overlay.load_I(), sw, sh);
      return !sameGeometry(persisted, clampOfPersisted) && sameGeometry(current, clampOfPersisted) ? persisted : current;
   }

   private static boolean sameGeometry(OverlayBounds a, OverlayBounds b) {
      return a != null && b != null && a.x == b.x && a.y == b.y && a.width == b.width && a.height == b.height;
   }

   public OverlayBounds getStashedBounds(String overlayId) {
      return overlayId == null ? null : this.stashedBoundsById.get(overlayId);
   }

   public void restoreStashedIfFits() {
      if (!this.stashedBoundsById.isEmpty()) {
         int sw = cA.init_I();
         int sh = cA.load_I();
         if (sw > 0 && sh > 0) {
            for (AutismOverlay overlay : this.overlays) {
               if (overlay != null && overlay.init_Str() != null) {
                  OverlayBounds stashed = this.stashedBoundsById.get(overlay.init_Str());
                  if (stashed != null) {
                     stashed = mergeGeometryWithLiveFlags(stashed, overlay.init_cH());
                     OverlayBounds clamped = OverlayLayoutBase.clampBounds(stashed, overlay.init_I(), overlay.load_I(), sw, sh);
                     if (sameGeometry(clamped, stashed)) {
                        this.stashedBoundsById.remove(overlay.init_Str());
                        overlay.init_V_cH(stashed);
                     }
                  }
               }
            }
         }
      }
   }

   private void setActive(boolean active) {
      if (this.active != active) {
         this.active = active;
         cf.init_V_J_Z(256L, active);
      }
   }

   public boolean isDraggingOrResizing() {
      return this.draggingOverlay != null || this.resizingOverlay != null;
   }

   public boolean isResizing(AutismOverlay overlay) {
      return overlay != null && this.resizingOverlay == overlay;
   }

   private void logRenderFailure(Throwable t) {
      long now = System.currentTimeMillis();
      if (now - this.lastRenderErrorLogMs >= 5000L) {
         this.lastRenderErrorLogMs = now;
         AutismClientAddon.LOG.warn("[Overlay] render failed; skipped to protect the UI", t);
      }
   }

   public void render(GuiGraphicsExtractor context, int mouseX, int mouseY, float delta) {
      if (!OverlayScreenController.init_Z() && !autismclient.modules.aD.init_Z() && !isLocked()) {
         Minecraft mc = Minecraft.getInstance();
         if (mc != null) {
            this.blurOffscreenOverlays();
            if (mc.getWindow() != null) {
               int sw = cA.init_I();
               int sh = cA.load_I();
               if (sw > 0 && sh > 0 && (sw != this.lastScreenWidth || sh != this.lastScreenHeight)) {
                  this.lastScreenWidth = sw;
                  this.lastScreenHeight = sh;
                  this.invalidateHoverCache();

                  for (AutismOverlay overlay : this.overlays) {
                     this.reclampOverlay(overlay);
                  }
               }
            }

            Screen screen = mc.gui.screen();
            this.syncScreenContext(screen);
            int virtualMouseX = (int)Math.round(cA.init_D_D(mouseX));
            int virtualMouseY = (int)Math.round(cA.init_D_D(mouseY));
            this.visibleOverlaysScratch.clear();

            for (AutismOverlay overlay : this.overlays) {
               if (this.isVisibleOnScreen(overlay, screen)) {
                  this.visibleOverlaysScratch.add(overlay);
               }
            }

            boolean anyOverlayAnimating = false;

            for (AutismOverlay overlay : this.overlays) {
               if (!this.isVisibleOnScreen(overlay, screen)) {
                  autismclient.gui.ui.widgets.OverlayWindowLayerAdapter adapter = this.adapters.get(overlay);
                  if (adapter != null && adapter.load_Z() && !this.isHidden(overlay) && this.isAllowedOnScreen(overlay, screen)) {
                     anyOverlayAnimating = true;
                     break;
                  }
               }
            }

            this.setActive(!this.visibleOverlaysScratch.isEmpty());
            if (this.visibleOverlaysScratch.isEmpty() && !anyOverlayAnimating) {
               for (autismclient.gui.ui.widgets.OverlayWindowLayerAdapter adapter : this.adapters.values()) {
                  adapter.load(true);
               }
            } else {
               int hoveredOverlayIndex = -1;

               for (int i = this.visibleOverlaysScratch.size() - 1; i >= 0; i--) {
                  AutismOverlay overlay = this.visibleOverlaysScratch.get(i);
                  if (overlay.init_Z_D_D(virtualMouseX, virtualMouseY)) {
                     hoveredOverlayIndex = i;
                     break;
                  }
               }

               if (this.textRenderer == null || this.textRenderer.font() != mc.font) {
                  this.textRenderer = autismclient.gui.f.UiHelper.init(mc.font);
               }

               autismclient.gui.f.SyncManager uiContext = new autismclient.gui.f.SyncManager(
                  context, this.uiTheme, this.textRenderer, this.lastScreenWidth, this.lastScreenHeight, virtualMouseX, virtualMouseY, delta
               );
               int visibleIndex = 0;

               for (AutismOverlay overlay : this.overlays) {
                  autismclient.gui.ui.widgets.OverlayWindowLayerAdapter adapter = this.getOrCreateAdapter(overlay);
                  boolean interactive = this.isVisibleOnScreen(overlay, screen);
                  boolean animating = !interactive && adapter.load_Z() && !this.isHidden(overlay) && this.isAllowedOnScreen(overlay, screen);
                  adapter.load(!interactive && !animating);
                  adapter.write(!interactive);
                  adapter.read(interactive && hoveredOverlayIndex > visibleIndex);
                  if (interactive) {
                     visibleIndex++;
                  }
               }

               this.rebuildLayerHost();
               cA.init_V_GuiGraphicsExtract(context);

               try {
                  autismclient.gui.ui.UiClipStack.getInstance().clear(context);

                  try {
                     this.layerHost.init_V_SyncManager(uiContext);
                  } catch (Throwable t) {
                     autismclient.gui.ui.UiClipStack.getInstance().clear(context);
                     this.logRenderFailure(t);
                  }
               } finally {
                  autismclient.gui.ui.UiClipStack.getInstance().clear(context);
                  cA.load_V_GuiGraphicsExtract(context);
                  this.visibleOverlaysScratch.clear();
               }
            }
         }
      }
   }

   public boolean isMouseOver(double mouseX, double mouseY) {
      if (autismclient.modules.aD.init_Z()) {
         return false;
      } else {
         return isLocked() ? false : this.isMouseOverVirtual(cA.init_D_D(mouseX), cA.init_D_D(mouseY));
      }
   }

   private boolean isMouseOverVirtual(double mouseX, double mouseY) {
      for (AutismOverlay overlay : this.overlays) {
         if (this.isVisibleNow(overlay) && overlay.init_Z_D_D(mouseX, mouseY)) {
            return true;
         }
      }

      return false;
   }

   public boolean isMouseOverCached(double mouseX, double mouseY) {
      if (autismclient.modules.aD.init_Z()) {
         return false;
      }

      if (isLocked()) {
         return false;
      }

      if (this.overlays.isEmpty()) {
         return false;
      }

      if (!this.active) {
         return false;
      }

      long now = System.nanoTime();
      if (Double.compare(mouseX, this.cachedHoverX) == 0 && Double.compare(mouseY, this.cachedHoverY) == 0 && now - this.cachedHoverNanos < 16000000L) {
         return this.cachedHoverResult;
      }

      boolean result = this.isMouseOverVirtual(cA.init_D_D(mouseX), cA.init_D_D(mouseY));
      this.cachedHoverX = mouseX;
      this.cachedHoverY = mouseY;
      this.cachedHoverNanos = now;
      this.cachedHoverResult = result;
      return result;
   }

   private void invalidateHoverCache() {
      this.cachedHoverX = 0.0 / 0.0;
      this.cachedHoverY = 0.0 / 0.0;
      this.cachedHoverNanos = 0L;
      this.cachedHoverResult = false;
   }

   private void blurTextInputs() {
      for (AutismOverlay overlay : this.overlays) {
         if (this.isVisibleNow(overlay) && overlay.write_Z()) {
            overlay.push_V();
         }
      }

      this.cachedTextInputOverlay = null;
      this.textInputCacheDirty = false;
   }

   private AutismOverlay topOverlayAt(double mouseX, double mouseY) {
      for (int i = this.overlays.size() - 1; i >= 0; i--) {
         AutismOverlay overlay = this.overlays.get(i);
         if (this.isVisibleNow(overlay) && overlay.init_Z_D_D(mouseX, mouseY)) {
            return overlay;
         }
      }

      return null;
   }

   public boolean isTopmost(AutismOverlay overlay) {
      if (overlay != null && !this.overlays.isEmpty()) {
         for (int i = this.overlays.size() - 1; i >= 0; i--) {
            AutismOverlay candidate = this.overlays.get(i);
            if (this.isVisibleNow(candidate)) {
               return candidate == overlay;
            }
         }

         return false;
      } else {
         return false;
      }
   }

   public boolean isFocused(AutismOverlay overlay) {
      return overlay != null && overlay == this.focusedOverlay;
   }

   public boolean mouseClicked(double mouseX, double mouseY, int button) {
      if (autismclient.modules.aD.init_Z()) {
         return false;
      }

      if (isLocked()) {
         return false;
      }

      this.clickedOutsideOverlays = false;
      mouseX = cA.init_D_D(mouseX);
      mouseY = cA.init_D_D(mouseY);
      AutismOverlay topOverlay = this.topOverlayAt(mouseX, mouseY);
      if (topOverlay == null) {
         this.blurTextInputs();
         this.focusedOverlay = null;
         AutismSessionState.init_ck().init_V_Str("");
         this.headerClickCandidate = null;
         this.headerDragExceededThreshold = false;
         this.headerClickStartBounds = null;
         this.clickedOutsideOverlays = true;
         return false;
      }

      this.blurTextInputs();
      if (button == 0) {
         if (topOverlay.read_Z_D_D(mouseX, mouseY)) {
            long now = System.currentTimeMillis();
            if (topOverlay.read_Z() && this.lastResizeGripOverlay == topOverlay && now - this.lastResizeGripClickMs <= 350L) {
               this.lastResizeGripOverlay = null;
               topOverlay.run_V();
               this.bringToFront(topOverlay);
               this.invalidateHoverCache();
               return true;
            }

            this.lastResizeGripOverlay = topOverlay;
            this.lastResizeGripClickMs = now;
            this.resizingOverlay = topOverlay;
            this.resizeStartBounds = topOverlay.init_cH();
            this.resizeStartX = mouseX;
            this.resizeStartY = mouseY;
            this.bringToFront(topOverlay);
            return true;
         }

         if (topOverlay.load_Z_D_D(mouseX, mouseY)) {
            this.draggingOverlay = topOverlay;
            this.dragStartBounds = topOverlay.init_cH();
            if (topOverlay.load_Z()) {
               this.headerClickCandidate = topOverlay;
               this.headerDragExceededThreshold = false;
               this.headerClickStartX = mouseX;
               this.headerClickStartY = mouseY;
               this.headerClickStartBounds = topOverlay.init_cH();
            } else {
               this.headerClickCandidate = null;
               this.headerDragExceededThreshold = false;
               this.headerClickStartBounds = null;
            }

            this.bringToFront(topOverlay);
            this.getOrCreateAdapter(topOverlay).init_EventBus_I_2((int)mouseX, (int)mouseY, button);
            this.updateFocusedTextCache(topOverlay);
            return true;
         }
      }

      this.headerClickCandidate = null;
      this.headerDragExceededThreshold = false;
      this.headerClickStartBounds = null;
      this.bringToFront(topOverlay);
      this.syncLayerInputFlags();
      autismclient.gui.ui.widgets.OverlayWindowLayerAdapter topComponent = this.getOrCreateAdapter(topOverlay);
      autismclient.gui.f.NetHelper topHitBounds = topComponent.load_NetHelper();
      if (topHitBounds != null && topHitBounds.init_Z((int)mouseX, (int)mouseY)) {
         this.layerInputRouter.init_Z_I_2((int)mouseX, (int)mouseY, button);
      } else {
         topComponent.init_EventBus_I_2((int)mouseX, (int)mouseY, button);
      }

      this.updateFocusedTextCache(topOverlay);
      return true;
   }

   public boolean mouseReleased(double mouseX, double mouseY, int button) {
      if (autismclient.modules.aD.init_Z()) {
         return false;
      }

      if (isLocked()) {
         return false;
      }

      mouseX = cA.init_D_D(mouseX);
      mouseY = cA.init_D_D(mouseY);
      boolean wasDraggingOrResizing = this.draggingOverlay != null || this.resizingOverlay != null;
      AutismOverlay prevDragging = this.draggingOverlay;
      AutismOverlay prevResizing = this.resizingOverlay;
      boolean shouldToggleHeaderCollapse = button == 0
         && prevDragging != null
         && prevDragging == this.headerClickCandidate
         && prevDragging.load_Z()
         && !this.headerDragExceededThreshold
         && Math.abs(mouseX - this.headerClickStartX) < 3.0
         && Math.abs(mouseY - this.headerClickStartY) < 3.0
         && prevDragging.load_Z_D_D(mouseX, mouseY);
      OverlayBounds headerStartBounds = this.headerClickStartBounds;
      if (button == 0) {
         if (prevDragging != null && prevDragging.init_Str() != null && this.dragStartBounds != null && !sameGeometry(prevDragging.init_cH(), this.dragStartBounds)) {
            this.stashedBoundsById.remove(prevDragging.init_Str());
         }

         if (prevResizing != null && prevResizing.init_Str() != null) {
            this.stashedBoundsById.remove(prevResizing.init_Str());
         }

         this.draggingOverlay = null;
         this.dragStartBounds = null;
         if (this.resizingOverlay != null) {
            this.resizingOverlay.tick_V();
            this.resizingOverlay.a__V();
         }

         this.resizingOverlay = null;
         this.resizeStartBounds = null;
         this.headerClickCandidate = null;
         this.headerDragExceededThreshold = false;
         this.headerClickStartBounds = null;
      }

      if (button == 0 && prevDragging != null) {
         this.getOrCreateAdapter(prevDragging).load((int)mouseX, (int)mouseY, button);
      }

      if (button == 0 && prevResizing != null && prevResizing != prevDragging) {
         this.getOrCreateAdapter(prevResizing).load((int)mouseX, (int)mouseY, button);
      }

      if (shouldToggleHeaderCollapse && this.isVisibleNow(prevDragging)) {
         if (headerStartBounds != null) {
            OverlayBounds current = prevDragging.init_cH();
            prevDragging.init_V_cH(new OverlayBounds(headerStartBounds.x, headerStartBounds.y, current.width, current.height, current.visible, current.collapsed));
         }

         prevDragging.start_V();
         prevDragging.a__V();
         this.invalidateHoverCache();
         return true;
      } else {
         if (wasDraggingOrResizing) {
            return true;
         }

         if (this.clickedOutsideOverlays) {
            this.clickedOutsideOverlays = false;
            return false;
         }

         for (int i = this.overlays.size() - 1; i >= 0; i--) {
            AutismOverlay overlay = this.overlays.get(i);
            if (this.isVisibleNow(overlay) && this.getOrCreateAdapter(overlay).load((int)mouseX, (int)mouseY, button) == autismclient.gui.ui.UiInputResult.HANDLED) {
               return true;
            }
         }

         return this.isMouseOverVirtual(mouseX, mouseY);
      }
   }

   public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
      if (autismclient.modules.aD.init_Z()) {
         return false;
      }

      if (isLocked()) {
         return false;
      }

      mouseX = cA.init_D_D(mouseX);
      mouseY = cA.init_D_D(mouseY);
      deltaX = cA.init_D_D(deltaX);
      deltaY = cA.init_D_D(deltaY);
      if (button == 0 && this.resizingOverlay != null && this.resizeStartBounds != null && this.isVisibleNow(this.resizingOverlay)) {
         this.resizingOverlay.init_V_I_I(this.resizeStartBounds.width + (int)Math.round(mouseX - this.resizeStartX), this.resizeStartBounds.height + (int)Math.round(mouseY - this.resizeStartY));
         this.invalidateHoverCache();
         return true;
      }

      if (button == 0 && this.isVisibleNow(this.draggingOverlay)) {
         if (this.draggingOverlay == this.headerClickCandidate && !this.headerDragExceededThreshold) {
            if (Math.abs(mouseX - this.headerClickStartX) >= 3.0 || Math.abs(mouseY - this.headerClickStartY) >= 3.0) {
               this.headerDragExceededThreshold = true;
            }

            if (!this.headerDragExceededThreshold) {
               return true;
            }
         }

         boolean handled = this.getOrCreateAdapter(this.draggingOverlay).init_EventBus_I((int)mouseX, (int)mouseY, button, deltaX, deltaY)
            == autismclient.gui.ui.UiInputResult.HANDLED;
         this.invalidateHoverCache();
         return handled;
      } else {
         if (this.clickedOutsideOverlays) {
            return false;
         }

         for (int i = this.overlays.size() - 1; i >= 0; i--) {
            AutismOverlay overlay = this.overlays.get(i);
            if (this.isVisibleNow(overlay)
               && this.getOrCreateAdapter(overlay).init_EventBus_I((int)mouseX, (int)mouseY, button, deltaX, deltaY) == autismclient.gui.ui.UiInputResult.HANDLED) {
               return true;
            }
         }

         return this.isMouseOverVirtual(mouseX, mouseY);
      }
   }

   public boolean mouseScrolled(double mouseX, double mouseY, double amount) {
      if (autismclient.modules.aD.init_Z()) {
         return false;
      }

      if (isLocked()) {
         return false;
      }

      mouseX = cA.init_D_D(mouseX);
      mouseY = cA.init_D_D(mouseY);
      AutismOverlay topOverlay = this.topOverlayAt(mouseX, mouseY);
      if (topOverlay == null) {
         return false;
      }

      this.bringToFront(topOverlay);
      this.syncLayerInputFlags();
      autismclient.gui.ui.widgets.OverlayWindowLayerAdapter topComponent = this.getOrCreateAdapter(topOverlay);
      autismclient.gui.f.NetHelper topHitBounds = topComponent.load_NetHelper();
      if (topHitBounds != null && topHitBounds.init_Z((int)mouseX, (int)mouseY)) {
         this.layerInputRouter.init_Z_I((int)mouseX, (int)mouseY, amount);
      } else {
         topComponent.init_EventBus_I((int)mouseX, (int)mouseY, amount);
      }

      return true;
   }

   public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
      if (autismclient.modules.aD.init_Z()) {
         return false;
      }

      if (isLocked()) {
         return false;
      }

      for (int i = this.overlays.size() - 1; i >= 0; i--) {
         AutismOverlay overlay = this.overlays.get(i);
         if (this.isVisibleNow(overlay) && overlay.e_() && this.getOrCreateAdapter(overlay).read(keyCode, scanCode, modifiers) == autismclient.gui.ui.UiInputResult.HANDLED) {
            this.bringToFront(overlay);
            this.updateFocusedTextCache(overlay);
            return true;
         }
      }

      AutismOverlay focusedTextOverlay = this.findFocusedTextOverlay();
      if (focusedTextOverlay != null) {
         this.getOrCreateAdapter(focusedTextOverlay).read(keyCode, scanCode, modifiers);
         this.updateFocusedTextCache(focusedTextOverlay);
         this.focusedOverlay = focusedTextOverlay;
         AutismSessionState.init_ck().init_V_Str(focusedTextOverlay.init_Str());
         return true;
      } else {
         AutismOverlay keyboardTarget = this.findKeyboardTarget();
         if (keyboardTarget != null && this.getOrCreateAdapter(keyboardTarget).read(keyCode, scanCode, modifiers) == autismclient.gui.ui.UiInputResult.HANDLED) {
            this.updateFocusedTextCache(keyboardTarget);
            return true;
         } else {
            return this.hasFocusedTextInput();
         }
      }
   }

   public boolean charTyped(char chr, int modifiers) {
      if (autismclient.modules.aD.init_Z()) {
         return false;
      } else if (isLocked()) {
         return false;
      } else {
         AutismOverlay focusedTextOverlay = this.findFocusedTextOverlay();
         if (focusedTextOverlay != null) {
            this.getOrCreateAdapter(focusedTextOverlay).init(chr, modifiers);
            this.updateFocusedTextCache(focusedTextOverlay);
            this.focusedOverlay = focusedTextOverlay;
            AutismSessionState.init_ck().init_V_Str(focusedTextOverlay.init_Str());
            return true;
         } else {
            AutismOverlay keyboardTarget = this.findKeyboardTarget();
            if (keyboardTarget != null && this.getOrCreateAdapter(keyboardTarget).init(chr, modifiers) == autismclient.gui.ui.UiInputResult.HANDLED) {
               this.updateFocusedTextCache(keyboardTarget);
               return true;
            } else {
               return this.hasFocusedTextInput();
            }
         }
      }
   }

   private AutismOverlay findFocusedTextOverlay() {
      if (!this.textInputCacheDirty) {
         AutismOverlay cached = this.cachedTextInputOverlay;
         if (cached != null && this.isVisibleNow(cached) && cached.write_Z()) {
            return cached;
         }
      }

      for (int i = this.overlays.size() - 1; i >= 0; i--) {
         AutismOverlay overlay = this.overlays.get(i);
         if (this.isVisibleNow(overlay) && overlay.write_Z()) {
            this.cachedTextInputOverlay = overlay;
            this.textInputCacheDirty = false;
            return overlay;
         }
      }

      this.cachedTextInputOverlay = null;
      this.textInputCacheDirty = false;
      return null;
   }

   private void updateFocusedTextCache(AutismOverlay overlay) {
      this.cachedTextInputOverlay = this.isVisibleNow(overlay) && overlay.write_Z() ? overlay : null;
      this.textInputCacheDirty = false;
   }

   private AutismOverlay findKeyboardTarget() {
      if (this.isVisibleNow(this.focusedOverlay)) {
         return this.focusedOverlay;
      }

      for (int i = this.overlays.size() - 1; i >= 0; i--) {
         AutismOverlay overlay = this.overlays.get(i);
         if (this.isVisibleNow(overlay)) {
            return overlay;
         }
      }

      return null;
   }

   public boolean hasFocusedTextInput() {
      if (autismclient.modules.aD.init_Z()) {
         return false;
      } else {
         return isLocked() ? false : this.findFocusedTextOverlay() != null;
      }
   }

   public void clearFocus() {
      this.blurTextInputs();
      this.focusedOverlay = null;
      AutismSessionState.init_ck().init_V_Str("");
   }

   private autismclient.gui.ui.widgets.OverlayWindowLayerAdapter getOrCreateAdapter(AutismOverlay overlay) {
      autismclient.gui.ui.widgets.OverlayWindowLayerAdapter adapter = this.adapters.get(overlay);
      if (adapter == null) {
         adapter = new autismclient.gui.ui.widgets.OverlayWindowLayerAdapter(overlay);
         adapter.init_V_Z(!this.isStepPickerOverlay(overlay));
         this.adapters.put(overlay, adapter);
      }

      return adapter;
   }

   private void syncScreenContext(Screen screen) {
      boolean contextSwitch = this.screenContextDirty || screen != this.lastScreenRef.get();
      this.screenContextDirty = false;
      if (screen != this.lastScreenRef.get()) {
         this.lastScreenRef = new WeakReference<>(screen);
      }

      if (contextSwitch) {
         for (AutismOverlay pending : new ArrayList<>(this.closingOverlays)) {
            this.removeImmediate(pending);
         }
      }

      for (autismclient.gui.ui.widgets.OverlayWindowLayerAdapter adapter : this.adapters.values()) {
         if (contextSwitch) {
            adapter.load_V();
         } else {
            adapter.init_V();
         }
      }

      if (!this.closingOverlays.isEmpty()) {
         for (AutismOverlay pending : new ArrayList<>(this.closingOverlays)) {
            autismclient.gui.ui.widgets.OverlayWindowLayerAdapter adapter = this.adapters.get(pending);
            if (pending.sync_Z()) {
               this.closingOverlays.remove(pending);
            } else if (adapter == null || !adapter.load_Z()) {
               this.removeImmediate(pending);
            }
         }
      }
   }

   private void syncLayerInputFlags() {
      for (AutismOverlay overlay : this.overlays) {
         this.getOrCreateAdapter(overlay).write(!this.isVisibleNow(overlay));
      }

      this.rebuildLayerHost();
   }

   private void rebuildLayerHost() {
      if (this.layerHostDirty) {
         this.layerHost.init();

         for (AutismOverlay overlay : this.overlays) {
            this.layerHost
               .init(this.isStepPickerOverlay(overlay) ? autismclient.gui.ui.UiLayer.DROPDOWN : autismclient.gui.ui.UiLayer.FLOATING, this.getOrCreateAdapter(overlay));
         }

         this.layerHostDirty = false;
      }
   }
}

package autismclient.util;

import autismclient.gui.ui.widgets.SearchFilterList;
import autismclient.gui.ui.widgets.UiLayoutMath;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.protocol.Packet;

public class AutismPacketSelectorOverlay extends DraggableOverlayWindow {
   private static final Minecraft MC = Minecraft.getInstance();
   private static final int flush_I = 230;
   private static final int close_I = 1;
   private static final float init_F_2 = 3.0F;
   private static final int open_I = 6;
   private final Font textRenderer;
   private final autismclient.gui.f.b.CharEncoder theme = new autismclient.gui.f.b.CharEncoder();
   private final autismclient.gui.ui.layout.CollapsibleWindowNode windowNode = new autismclient.gui.ui.layout.CollapsibleWindowNode("Select Packet");
   private final autismclient.gui.ui.layout.LayoutTreeHost layoutHost = new autismclient.gui.ui.layout.LayoutTreeHost(this.theme, this.windowNode);
   private final autismclient.gui.ui.widgets.TextInputWidget searchInput = new autismclient.gui.ui.widgets.TextInputWidget();
   private final autismclient.gui.ui.layout.LabelNode countLabel = new autismclient.gui.ui.layout.LabelNode("", autismclient.gui.f.b.l.write).setTrimToBounds(true);
   private final autismclient.gui.ui.layout.VerticalSpacerNode listSpacer = new autismclient.gui.ui.layout.VerticalSpacerNode();
   private autismclient.gui.ui.layout.ScrollableListView listView = null;
   private final List<Class<? extends Packet<?>>> init_List = new ArrayList<>();
   private final List<Class<? extends Packet<?>>> c2sPackets = new ArrayList<>();
   private final List<Class<? extends Packet<?>>> s2cPackets = new ArrayList<>();
   private final List<Class<? extends Packet<?>>> write_List = new ArrayList<>();
   private final Set<Class<? extends Packet<?>>> c2sPacketSet = new LinkedHashSet<>();
   private final Map<Class<? extends Packet<?>>, String> packetLabels = new HashMap<>();
   private final SearchFilterList<Class<? extends Packet<?>>> searchList = new SearchFilterList<>(
      packetClassx -> this.packetLabels.computeIfAbsent(packetClassx, AutismPacketSelectorOverlay::packetLabel)
   );
   private List<Class<? extends Packet<?>>> update_List = List.of();
   private List<Class<? extends Packet<?>>> filteredPackets = List.of();
   private Set<Class<? extends Packet<?>>> excludedPackets = Set.of();
   private Set<Class<? extends Packet<?>>> selectedPackets = Set.of();
   private boolean overlayVisible = false;
   private boolean collapsed = false;
   private boolean dragging = false;
   private boolean dragMoved = false;
   private float dragOffsetX;
   private float dragOffsetY;
   private float uiMouseX;
   private float uiMouseY;
   private int dragStartX;
   private int dragStartY;
   private int pop;
   private int tick;
   private int run = 230;
   private int get = 0;
   private int set = 0;
   private boolean close_Z = false;
   private int put = 0;
   private Consumer<Class<? extends Packet<?>>> onSelect;
   private BiConsumer<Class<? extends Packet<?>>, Boolean> onToggleSelect;
   private boolean closeOnSelect = true;
   private boolean multiSelectMode = false;
   private boolean push_Z = true;
   private final autismclient.gui.ui.VisibilityTransition visibilityTransition = new autismclient.gui.ui.VisibilityTransition(false);

   public AutismPacketSelectorOverlay(Font textRenderer) {
      this.textRenderer = textRenderer;
      this.c2sPackets.addAll(PacketTypeRegistry.getC2sPackets());
      this.c2sPackets.sort(Comparator.comparing(PacketNameResolver::nameOfClass, String.CASE_INSENSITIVE_ORDER));
      this.s2cPackets.addAll(PacketTypeRegistry.getS2cPackets());
      this.s2cPackets.sort(Comparator.comparing(PacketNameResolver::nameOfClass, String.CASE_INSENSITIVE_ORDER));
      this.c2sPacketSet.addAll(this.c2sPackets);
      this.write_List.addAll(this.c2sPackets);
      this.write_List.addAll(this.s2cPackets);
      this.init_List.addAll(this.write_List);
      this.init_List.sort(Comparator.comparing(PacketNameResolver::nameOfClass, String.CASE_INSENSITIVE_ORDER));

      for (Class<? extends Packet<?>> packetClass : this.init_List) {
         this.packetLabels.put(packetClass, packetLabel(packetClass));
      }

      this.update_List = List.copyOf(this.init_List);
      this.filteredPackets = List.copyOf(this.init_List);
      this.setupLayout();
   }

   private void setupLayout() {
      this.run = this.computeMinWidth();
      this.windowNode.setCenterTitle(false);
      this.windowNode.setTitleTone(autismclient.gui.f.b.l.load);
      this.windowNode.setHeaderButtons(true, true);
      this.windowNode.setTitleInsets(this.run_I_20() + 2, this.run_I_20() + this.put() + this.add_I_15() + this.clear() + 12);
      this.windowNode.body().withGap(this.get_I_18()).withPadding(autismclient.gui.ui.layout.EdgeInsets.uniform(this.run_I_20()));
      this.searchInput
         .setPlaceholder("Search packets...")
         .setFixedHeight(this.set_I_18())
         .withGrowX(true)
         .setOnChange(text -> this.applySearch(text));
      this.rebuildLayout();
   }

   private void rebuildLayout() {
      this.windowNode.body().clearChildren();
      this.windowNode.setTitle(this.multiSelectMode ? "Packet Selector [Toggle]" : "Packet Selector");
      this.windowNode.body().addChild(this.searchInput);
      this.listSpacer.withHeight(this.listHeightFor(this.filteredPackets.size()));
      this.windowNode.body().addChild(this.listSpacer);
      this.countLabel.init(this.multiSelectMode ? this.filteredPackets.size() + " packets | Selected " + this.selectedPackets.size() : this.filteredPackets.size() + " packets");
      this.windowNode.body().addChild(this.countLabel);
      this.set = this.filteredPackets.size() * this.rowHeight();
      this.push_Z = false;
   }

   private int listHeightFor(int itemCount) {
      int visibleRows = Math.max(3, Math.min(this.maxVisibleRows(), Math.max(1, itemCount)));
      return visibleRows * this.rowHeight() + 2;
   }

   public void openSinglePicker(Consumer<Class<? extends Packet<?>>> onSelect) {
      this.openSelect(onSelect, this.init_List, Set.of(), true);
   }

   public void load(Consumer<Class<? extends Packet<?>>> onSelect) {
      this.openForC2s(onSelect, Set.of(), true);
   }

   public void openForC2s(Consumer<Class<? extends Packet<?>>> onSelect, Collection<Class<? extends Packet<?>>> excludedPackets, boolean closeOnSelect) {
      this.openSelect(onSelect, this.c2sPackets, excludedPackets, closeOnSelect);
   }

   public void read(Consumer<Class<? extends Packet<?>>> onSelect) {
      this.openForS2c(onSelect, Set.of(), true);
   }

   public void openForS2c(Consumer<Class<? extends Packet<?>>> onSelect, Collection<Class<? extends Packet<?>>> excludedPackets, boolean closeOnSelect) {
      this.openSelect(onSelect, this.s2cPackets, excludedPackets, closeOnSelect);
   }

   public void read(Consumer<Class<? extends Packet<?>>> onSelect, Collection<Class<? extends Packet<?>>> excludedPackets, boolean closeOnSelect) {
      this.openSelect(onSelect, this.write_List, excludedPackets, closeOnSelect);
   }

   public void openC2sMultiSelect(
      BiConsumer<Class<? extends Packet<?>>, Boolean> onToggleSelect, Collection<Class<? extends Packet<?>>> selectedPackets
   ) {
      this.openToggleSelect(onToggleSelect, this.c2sPackets, selectedPackets, Set.of());
   }

   public void openToggleForC2s(
      BiConsumer<Class<? extends Packet<?>>, Boolean> onToggleSelect,
      Collection<Class<? extends Packet<?>>> selectedPackets,
      Collection<Class<? extends Packet<?>>> excludedPackets
   ) {
      this.openToggleSelect(onToggleSelect, this.c2sPackets, selectedPackets, excludedPackets);
   }

   public void openS2cMultiSelect(
      BiConsumer<Class<? extends Packet<?>>, Boolean> onToggleSelect, Collection<Class<? extends Packet<?>>> selectedPackets
   ) {
      this.openToggleSelect(onToggleSelect, this.s2cPackets, selectedPackets, Set.of());
   }

   public void openToggleForS2c(
      BiConsumer<Class<? extends Packet<?>>, Boolean> onToggleSelect,
      Collection<Class<? extends Packet<?>>> selectedPackets,
      Collection<Class<? extends Packet<?>>> excludedPackets
   ) {
      this.openToggleSelect(onToggleSelect, this.s2cPackets, selectedPackets, excludedPackets);
   }

   public void openAnyMultiSelect(
      BiConsumer<Class<? extends Packet<?>>, Boolean> onToggleSelect, Collection<Class<? extends Packet<?>>> selectedPackets
   ) {
      this.openToggleSelect(onToggleSelect, this.write_List, selectedPackets, Set.of());
   }

   private void openSelect(
      Consumer<Class<? extends Packet<?>>> onSelect,
      List<Class<? extends Packet<?>>> pool,
      Collection<Class<? extends Packet<?>>> excludedPackets,
      boolean closeOnSelect
   ) {
      this.overlayVisible = true;
      this.collapsed = false;
      this.onSelect = onSelect;
      this.onToggleSelect = null;
      this.closeOnSelect = closeOnSelect;
      this.multiSelectMode = false;
      this.excludedPackets = excludedPackets == null ? Set.of() : new LinkedHashSet<>(excludedPackets);
      this.selectedPackets = Set.of();
      this.update_List = this.withoutExcluded(pool);
      this.applySearch("");
      this.flush_V_15();
   }

   private void openToggleSelect(
      BiConsumer<Class<? extends Packet<?>>, Boolean> onToggleSelect,
      List<Class<? extends Packet<?>>> pool,
      Collection<Class<? extends Packet<?>>> selectedPackets,
      Collection<Class<? extends Packet<?>>> excludedPackets
   ) {
      this.overlayVisible = true;
      this.collapsed = false;
      this.onSelect = null;
      this.onToggleSelect = onToggleSelect;
      this.closeOnSelect = false;
      this.multiSelectMode = true;
      this.excludedPackets = excludedPackets == null ? Set.of() : new LinkedHashSet<>(excludedPackets);
      this.selectedPackets = selectedPackets == null ? new LinkedHashSet<>() : new LinkedHashSet<>(selectedPackets);
      this.update_List = this.withoutExcluded(pool);
      this.applySearch("");
      this.flush_V_15();
   }

   private void flush_V_15() {
      this.rebuildLayout();
      autismclient.gui.ui.layout.LayoutContext metrics = this.layoutHost.createMeasureContext();
      if (metrics != null) {
         this.get = Math.round(this.windowNode.measureHeight(metrics, this.run));
      }

      autismclient.gui.ui.layout.LayoutViewport viewport = this.layoutHost.createViewport();
      this.pop = Math.max(8, Math.round((viewport.width() - this.run) / 2.0F));
      this.tick = Math.max(8, Math.round((viewport.height() - this.get) / 2.0F));
      OverlayBounds clamped = this.clampBounds(new OverlayBounds(this.pop, this.tick, this.run, this.get, this.overlayVisible, this.collapsed));
      this.pop = clamped.x;
      this.tick = clamped.y;
      this.run = clamped.width;
      this.get = clamped.height;
      this.searchInput.setValue("");
      this.searchInput.setFocused(true);
      this.windowNode.setShowBody(true);
      OverlayManager.getInstance().bringOwnerToFront(this);
   }

   public void resetSelection() {
      this.overlayVisible = false;
      this.collapsed = false;
      this.dragging = false;
      this.dragMoved = false;
      this.onSelect = null;
      this.onToggleSelect = null;
      this.excludedPackets = Set.of();
      this.selectedPackets = Set.of();
      this.closeOnSelect = true;
      this.multiSelectMode = false;
      this.layoutHost.clearFocus();
      this.windowNode.setShowBody(true);
   }

   @Override
   public boolean isVisible() {
      return this.overlayVisible;
   }

   private void applySearch(String query) {
      List<Class<? extends Packet<?>>> pool = this.withoutExcluded(this.update_List);
      this.searchList.setItems(pool);
      this.searchList.setQuery(query);
      this.filteredPackets = this.searchList.filtered();
      this.set = this.filteredPackets.size() * this.rowHeight();
      this.rebuildLayout();
   }

   @Override
   public void render(GuiGraphicsExtractor context, int mouseX, int mouseY, float delta) {
      this.visibilityTransition.animateTo(this.overlayVisible);
      if (this.visibilityTransition.shouldRender(this.overlayVisible) && MC != null && MC.font != null) {
         boolean closing = !this.overlayVisible;
         this.visibilityTransition
            .render(
               context,
               autismclient.gui.f.NetHelper.init(this.pop, this.tick, this.run, Math.max(this.theme.load(), this.get)),
               true,
               () -> this.load(context, closing ? -10000 : mouseX, closing ? -10000 : mouseY, delta)
            );
      }
   }

   private void load(GuiGraphicsExtractor context, int mouseX, int mouseY, float delta) {
      if (this.push_Z) {
         this.rebuildLayout();
      }

      context.nextStratum();
      autismclient.gui.ui.layout.LayoutViewport viewport = this.layoutHost.createViewport();
      float uiMouseX = viewport.toUiX(mouseX);
      float uiMouseY = viewport.toUiY(mouseY);
      boolean headerHovered = this.isOverHeaderUi(uiMouseX, uiMouseY);
      autismclient.gui.ui.layout.LayoutContext metrics = new autismclient.gui.ui.layout.LayoutContext(
         context, MC.font, viewport, this.theme, uiMouseX, uiMouseY, delta
      );
      this.windowNode.setExpanded(!this.collapsed);
      this.windowNode.setActive(true);
      this.windowNode.setHeaderHovered(headerHovered);
      this.get = Math.round(this.windowNode.measureHeight(metrics, this.run));
      this.get = Math.max(this.theme.load(), this.get);
      OverlayBounds clamped = this.clampBounds(new OverlayBounds(this.pop, this.tick, this.run, this.get, this.overlayVisible, this.collapsed));
      this.pop = clamped.x;
      this.tick = clamped.y;
      this.run = clamped.width;
      this.get = clamped.height;
      this.windowNode.setBounds(this.pop, this.tick, this.run, this.get);
      this.layoutHost.render(context, mouseX, mouseY, delta);
      context.nextStratum();
      if (this.windowNode.pushBodyClip(context)) {
         try {
            this.renderPacketList(context, viewport, uiMouseX, uiMouseY, delta);
         } finally {
            autismclient.gui.ui.UiClipStack.getInstance().pop(context);
         }
      }
   }

   private void renderPacketList(
      GuiGraphicsExtractor context, autismclient.gui.ui.layout.LayoutViewport viewport, float uiMouseX, float uiMouseY, float delta
   ) {
      int viewX = Math.round(this.listSpacer.x());
      int viewY = Math.round(this.listSpacer.y());
      int viewW = Math.round(this.listSpacer.width());
      int viewH = Math.round(this.listSpacer.height());
      if (viewW > 2 && viewH > 2) {
         int contentHeight = this.filteredPackets.size() * this.rowHeight();
         if (this.listView == null
            || this.listView.getX() != viewX
            || this.listView.getY() != viewY
            || this.listView.getWidth() != viewW
            || this.listView.getHeight() != viewH) {
            int preservedScroll = this.listView == null ? 0 : this.listView.getScrollOffset();
            this.listView = new autismclient.gui.ui.layout.ScrollableListView(viewX, viewY, viewW, viewH, this.rowHeight(), 6);
            this.listView.setContentHeight(contentHeight);
            this.listView.setScrollOffset(preservedScroll);
         }

         this.listView.setContentHeight(contentHeight);
         this.listView.beginFrame(context, this.theme.set(), this.theme.reset());

         try {
            this.listView.forEachVisibleRow(context, this.filteredPackets.size(), (idx, bnd) -> {
               Class<? extends Packet<?>> packetClass = this.filteredPackets.get(idx);
               this.renderPacketRow(context, packetClass, bnd.x, bnd.y, bnd.width, idx);
            });
         } finally {
            this.listView.endFrame(context);
         }

         this.init(context, viewX, viewY, viewW, viewH, uiMouseX, uiMouseY);
      }
   }

   private void renderPacketRow(GuiGraphicsExtractor context, Class<? extends Packet<?>> packetClass, int x, int y, int width, int index) {
      boolean c2s = this.c2sPacketSet.contains(packetClass);
      boolean selected = this.multiSelectMode && this.selectedPackets.contains(packetClass);
      int rowColor = selected ? UiColorPalette.filterRowFill(false) : UiColorPalette.packetRowFill(c2s, index, false);
      int color = selected ? UiColorPalette.filterActiveText() : UiColorPalette.packetRowAccent(c2s, index);
      autismclient.gui.ui.widgets.PanelSurfaceHelper.fillRect(context, x, y, width, this.rowHeight(), rowColor);
      String name = PacketNameResolver.nameOfClass(packetClass);
      int textWidth = width - 10;
      String trimmed = autismclient.gui.f.b.k.init(
         this.textRenderer, name, textWidth, this.theme.init_Identifier_l(autismclient.gui.f.b.l.read), color
      );
      int textY = UiLayoutMath.centerTextY(y, this.rowHeight(), this.theme.init_I_l(autismclient.gui.f.b.l.read), this.theme.flush());
      autismclient.gui.f.b.k.init(
         context, this.textRenderer, trimmed, this.theme.init_Identifier_l(autismclient.gui.f.b.l.read), color, x + 6, textY, false
      );
      autismclient.gui.ui.widgets.PanelSurfaceHelper.drawDividerColored(context, x + 4, y + this.rowHeight() - 1, width - 8, UiColorPalette.subtleDivider());
   }

   private void init(GuiGraphicsExtractor context, int viewX, int viewY, int viewW, int viewH, float uiMouseX, float uiMouseY) {
      if (this.listView != null) {
         this.listView.renderScrollbar(context, uiMouseX, uiMouseY);
      }
   }

   @Override
   public boolean mouseClicked(double mouseX, double mouseY, int button) {
      if (!this.overlayVisible) {
         return false;
      }

      autismclient.gui.ui.layout.LayoutViewport viewport = this.layoutHost.createViewport();
      float uiMouseX = viewport.toUiX(mouseX);
      float uiMouseY = viewport.toUiY(mouseY);
      if (button == 0 && this.isOverCloseButton(uiMouseX, uiMouseY)) {
         this.resetSelection();
         return true;
      }

      if (button == 0 && this.isOverHeaderUi(uiMouseX, uiMouseY)) {
         this.dragging = true;
         this.dragMoved = false;
         this.dragOffsetX = uiMouseX - this.pop;
         this.dragOffsetY = uiMouseY - this.tick;
         this.uiMouseX = uiMouseX;
         this.uiMouseY = uiMouseY;
         this.dragStartX = this.pop;
         this.dragStartY = this.tick;
         return true;
      }

      if (!this.collapsed && this.layoutHost.mouseClicked(mouseX, mouseY, button)) {
         return true;
      }

      if (!this.collapsed
         && button == 0
         && this.isInsideRect(
            this.listSpacer.x(),
            this.listSpacer.y(),
            this.listSpacer.width(),
            this.listSpacer.height(),
            uiMouseX,
            uiMouseY
         )
         && this.listView != null
         && this.listView.mouseClicked(uiMouseX, uiMouseY, button)) {
         return true;
      }

      if (!this.collapsed
         && button == 0
         && this.isInsideRect(
            this.listSpacer.x(),
            this.listSpacer.y(),
            this.listSpacer.width(),
            this.listSpacer.height(),
            uiMouseX,
            uiMouseY
         )) {
         int index = (int)((uiMouseY - this.listSpacer.y() + this.listView.getScrollOffset()) / this.rowHeight());
         if (index >= 0 && index < this.filteredPackets.size()) {
            Class<? extends Packet<?>> selectedPacket = this.filteredPackets.get(index);
            if (this.multiSelectMode) {
               this.selectedPackets = new LinkedHashSet<>(this.selectedPackets);
               boolean nowSelected;
               if (this.selectedPackets.contains(selectedPacket)) {
                  this.selectedPackets.remove(selectedPacket);
                  nowSelected = false;
               } else {
                  this.selectedPackets.add(selectedPacket);
                  nowSelected = true;
               }

               if (this.onToggleSelect != null) {
                  this.onToggleSelect.accept(selectedPacket, nowSelected);
               }

               this.rebuildLayout();
               this.searchInput.setFocused(true);
            } else {
               if (this.onSelect != null) {
                  this.onSelect.accept(selectedPacket);
               }

               if (this.closeOnSelect) {
                  this.resetSelection();
               } else {
                  this.excludedPackets = new LinkedHashSet<>(this.excludedPackets);
                  this.excludedPackets.add(selectedPacket);
                  this.update_List = this.withoutExcluded(this.update_List);
                  this.applySearch(this.searchInput.getValue());
                  this.searchInput.setFocused(true);
               }
            }

            return true;
         }
      }

      if (!this.isInsideRect(this.pop, this.tick, this.run, this.get, uiMouseX, uiMouseY)) {
         this.layoutHost.clearFocus();
         return true;
      } else {
         return true;
      }
   }

   @Override
   public boolean mouseReleased(double mouseX, double mouseY, int button) {
      autismclient.gui.ui.layout.LayoutViewport viewport = this.layoutHost.createViewport();
      float uiMouseX = viewport.toUiX(mouseX);
      float uiMouseY = viewport.toUiY(mouseY);
      if (button == 0 && this.dragging) {
         boolean moved = this.dragMoved
            || Math.abs(uiMouseX - this.uiMouseX) >= 3.0F
            || Math.abs(uiMouseY - this.uiMouseY) >= 3.0F
            || this.pop != this.dragStartX
            || this.tick != this.dragStartY;
         this.dragging = false;
         this.dragMoved = false;
         if (!moved && this.isOverHeaderUi(uiMouseX, uiMouseY) && !this.isOverCloseButton(uiMouseX, uiMouseY)) {
            this.setCollapsed(!this.collapsed);
         }

         return true;
      } else {
         if (button == 0 && this.listView != null) {
            this.listView.mouseReleased();
         }

         return !this.collapsed && this.layoutHost.mouseReleased(mouseX, mouseY, button)
            ? true
            : this.overlayVisible && this.isInsideRect(this.pop, this.tick, this.run, this.get, uiMouseX, uiMouseY);
      }
   }

   @Override
   public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
      if (!this.overlayVisible) {
         return false;
      }

      autismclient.gui.ui.layout.LayoutViewport viewport = this.layoutHost.createViewport();
      float uiMouseX = viewport.toUiX(mouseX);
      float uiMouseY = viewport.toUiY(mouseY);
      if (this.dragging && button == 0) {
         if (!this.dragMoved && Math.abs(uiMouseX - this.uiMouseX) < 3.0F && Math.abs(uiMouseY - this.uiMouseY) < 3.0F) {
            return true;
         }

         this.dragMoved = true;
         int nextX = Math.round(uiMouseX - this.dragOffsetX);
         int nextY = Math.round(uiMouseY - this.dragOffsetY);
         OverlayBounds clamped = this.clampBounds(new OverlayBounds(nextX, nextY, this.run, this.get, this.overlayVisible, this.collapsed));
         this.pop = clamped.x;
         this.tick = clamped.y;
         return true;
      } else if (!this.collapsed
         && this.isInsideRect(
            this.listSpacer.x(),
            this.listSpacer.y(),
            this.listSpacer.width(),
            this.listSpacer.height(),
            uiMouseX,
            uiMouseY
         )
         && this.listView != null
         && this.listView.isDraggingScrollbar()) {
         this.listView.mouseDragged(uiMouseX, uiMouseY);
         return true;
      } else {
         return !this.collapsed && this.layoutHost.mouseDragged(mouseX, mouseY, button, deltaX, deltaY)
            ? true
            : this.overlayVisible && this.isInsideRect(this.pop, this.tick, this.run, this.get, uiMouseX, uiMouseY);
      }
   }

   @Override
   public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
      if (!this.overlayVisible) {
         return false;
      }

      if (keyCode == 256) {
         if (this.searchInput.isFocused()) {
            this.searchInput.setFocused(false);
            return true;
         } else {
            this.resetSelection();
            return true;
         }
      } else {
         if (this.collapsed) {
            return false;
         }

         this.layoutHost.keyPressed(keyCode, scanCode, modifiers);
         return this.searchInput.isFocused();
      }
   }

   @Override
   public boolean charTyped(char chr, int modifiers) {
      return this.overlayVisible && !this.collapsed && this.layoutHost.charTyped(chr, modifiers);
   }

   @Override
   public boolean mouseScrolled(double mouseX, double mouseY, double amount) {
      if (!this.overlayVisible) {
         return false;
      } else {
         autismclient.gui.ui.layout.LayoutViewport viewport = this.layoutHost.createViewport();
         float uiMouseX = viewport.toUiX(mouseX);
         float uiMouseY = viewport.toUiY(mouseY);
         if (!this.collapsed
            && this.isInsideRect(
               this.listSpacer.x(),
               this.listSpacer.y(),
               this.listSpacer.width(),
               this.listSpacer.height(),
               uiMouseX,
               uiMouseY
            )
            && this.listView != null) {
            this.listView.mouseScrolled(uiMouseX, uiMouseY, amount);
            return true;
         } else {
            return this.searchInput.isFocused() ? false : this.isInsideRect(this.pop, this.tick, this.run, this.get, uiMouseX, uiMouseY);
         }
      }
   }

   private List<Class<? extends Packet<?>>> withoutExcluded(List<Class<? extends Packet<?>>> packets) {
      if (packets != null && !packets.isEmpty()) {
         if (this.excludedPackets.isEmpty()) {
            return new ArrayList<>(packets);
         }

         List<Class<? extends Packet<?>>> filtered = new ArrayList<>(packets.size());

         for (Class<? extends Packet<?>> packetClass : packets) {
            if (!this.excludedPackets.contains(packetClass)) {
               filtered.add(packetClass);
            }
         }

         return filtered;
      } else {
         return List.of();
      }
   }

   private static String packetLabel(Class<? extends Packet<?>> packetClass) {
      return packetClass == null ? "" : (packetClass.getSimpleName() + "\n" + PacketNameResolver.nameOfClass(packetClass)).toLowerCase(Locale.ROOT);
   }

   @Override
   public boolean hasFocusedTextInput() {
      return this.overlayVisible && this.layoutHost.hasFocusedInput();
   }

   @Override
   public OverlayBounds getBounds() {
      return new OverlayBounds(this.pop, this.tick, this.run, this.get, this.overlayVisible, this.collapsed);
   }

   @Override
   public void setBounds(OverlayBounds bounds) {
      if (bounds != null) {
         OverlayBounds clamped = this.clampBounds(bounds);
         this.pop = clamped.x;
         this.tick = clamped.y;
         this.run = clamped.width;
         this.get = clamped.height;
         this.overlayVisible = clamped.visible;
         this.collapsed = clamped.collapsed;
         this.windowNode.setShowBody(!this.collapsed);
      }
   }

   @Override
   public boolean isCollapsed() {
      return this.collapsed;
   }

   @Override
   public void setCollapsed(boolean collapsed) {
      if (this.collapsed != collapsed) {
         this.collapsed = collapsed;
         this.dragging = false;
         this.dragMoved = false;
         this.windowNode.setShowBody(!collapsed);
         if (collapsed) {
            this.clearFocusAndDismissMenu();
         }

         this.saveBounds();
      }
   }

   @Override
   public void clearInputFocus() {
      this.layoutHost.clearFocus();
   }

   @Override
   public boolean containsPoint(double mouseX, double mouseY) {
      if (!this.overlayVisible) {
         return false;
      }

      autismclient.gui.ui.layout.LayoutViewport viewport = this.layoutHost.createViewport();
      float uiMouseX = viewport.toUiX(mouseX);
      float uiMouseY = viewport.toUiY(mouseY);
      int renderedHeight = this.collapsed ? this.theme.load() : this.get;
      return this.isInsideRect(this.pop, this.tick, this.run, renderedHeight, uiMouseX, uiMouseY);
   }

   @Override
   public boolean isOverHeader(double mouseX, double mouseY) {
      if (!this.overlayVisible) {
         return false;
      }

      autismclient.gui.ui.layout.LayoutViewport viewport = this.layoutHost.createViewport();
      float uiMouseX = viewport.toUiX(mouseX);
      float uiMouseY = viewport.toUiY(mouseY);
      return this.isOverHeaderUi(uiMouseX, uiMouseY) && !this.isOverCloseButton(uiMouseX, uiMouseY);
   }

   @Override
   public int getMinWidth() {
      return this.computeMinWidth();
   }

   @Override
   public int getMinHeight() {
      return this.theme.load() + 20;
   }

   private boolean isOverHeaderUi(float uiMouseX, float uiMouseY) {
      return uiMouseX >= this.pop && uiMouseX < this.pop + this.run && uiMouseY >= this.tick && uiMouseY < this.tick + this.theme.load();
   }

   private boolean isOverCloseButton(float uiMouseX, float uiMouseY) {
      return autismclient.gui.ui.widgets.WindowHeaderHelper.init(
         autismclient.gui.f.NetHelper.init(this.pop, this.tick, this.run, Math.max(this.theme.load(), this.get)),
         this.theme.load(),
         uiMouseX,
         uiMouseY
      );
   }

   private OverlayBounds clampBounds(OverlayBounds bounds) {
      autismclient.gui.ui.layout.LayoutViewport viewport = this.layoutHost.createViewport();
      int margin = 4;
      int viewportW = Math.round(viewport.width());
      int viewportH = Math.round(viewport.height());
      int availableW = Math.max(1, viewportW - margin * 2);
      int availableH = Math.max(this.theme.load(), viewportH - margin * 2);
      int width = Math.max(Math.min(this.computeMinWidth(), availableW), Math.min(bounds.width, availableW));
      int minHeight = bounds.collapsed ? this.theme.load() : this.theme.load() + 20;
      int height = Math.max(Math.min(minHeight, availableH), Math.min(bounds.height, availableH));
      int renderedHeight = bounds.collapsed ? this.theme.load() : height;
      int x = Math.max(margin, Math.min(bounds.x, Math.max(margin, viewportW - margin - width)));
      int y = Math.max(margin, Math.min(bounds.y, Math.max(margin, viewportH - margin - renderedHeight)));
      return new OverlayBounds(x, y, width, height, bounds.visible, bounds.collapsed);
   }

   private boolean isInsideRect(float x, float y, float width, float height, float mouseX, float mouseY) {
      return mouseX >= x && mouseX <= x + width && mouseY >= y && mouseY <= y + height;
   }

   private int computeMinWidth() {
      return 230;
   }

   private int rowHeight() {
      return autismclient.gui.ui.UiDensityMetrics.scaledHeight(12);
   }

   private int maxVisibleRows() {
      return 12;
   }

   private int run_I_20() {
      return 4;
   }

   private int get_I_18() {
      return 2;
   }

   private int set_I_18() {
      return autismclient.gui.ui.UiDensityMetrics.scaledHeight(13);
   }

   private int put() {
      return 12;
   }

   private int add_I_15() {
      return 10;
   }

   private int clear() {
      return 3;
   }
}

package autismclient.util;

import autismclient.gui.ui.widgets.UiLayoutMath;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.protocol.Packet;

public class AutismCustomFilterOverlay extends DraggableOverlayWindow {
   private static final Minecraft unusedClient = Minecraft.getInstance();
   private static final int flush_I = 220;
   private static final int close_I = 10;
   private static final int open = 3;
   private static final int poll = 0;
   private static final int push = 1;
   private static final int pop = 2;
   private final Font font;
   private final autismclient.gui.f.b.CharEncoder theme = new autismclient.gui.f.b.CharEncoder();
   private final autismclient.gui.ui.widgets.TextInputWidget c2sSearch;
   private final autismclient.gui.ui.widgets.TextInputWidget s2cSearch;
   private final AutismPacketSelectorOverlay packetSelector;
   private final AutismCustomFilterPresetOverlay presetOverlay;
   private final List<AutismCustomFilterOverlay.Inner_DataUtil> actionButtons = new ArrayList<>();
   private final AutismCustomFilterOverlay.Inner_StreamUtil c2sList = new AutismCustomFilterOverlay.Inner_StreamUtil();
   private final AutismCustomFilterOverlay.Inner_StreamUtil s2cList = new AutismCustomFilterOverlay.Inner_StreamUtil();
   private int tick = 260;
   private int run = 36;
   private int get = 220;
   private int set = 200;
   private boolean boundsVisible = false;
   private boolean boundsCollapsed = false;
   private boolean draggingWindow = false;
   private double dragOffsetX = 0.0;
   private double dragOffsetY = 0.0;
   private int put = 0;
   private int add = 0;

   public AutismCustomFilterOverlay(Font textRenderer) {
      this.font = textRenderer;
      this.get = this.pop_I_5();
      this.set = this.tick_I_3();
      this.c2sSearch = this.createSearchInput("Search C2S packets...");
      this.s2cSearch = this.createSearchInput("Search S2C packets...");
      this.packetSelector = new AutismPacketSelectorOverlay(textRenderer);
      this.presetOverlay = new AutismCustomFilterPresetOverlay(textRenderer);
      this.c2sSearch.setOnChange(text -> this.c2sList.scroll.jumpTo(0, 0));
      this.s2cSearch.setOnChange(text -> this.s2cList.scroll.jumpTo(0, 0));
   }

   private autismclient.gui.ui.widgets.TextInputWidget createSearchInput(String placeholder) {
      return new autismclient.gui.ui.widgets.TextInputWidget()
         .setPlaceholder(placeholder)
         .setFixedHeight(this.clear())
         .setMinWidth(120.0F)
         .setPreferredWidth(120.0F)
         .setTextTone(autismclient.gui.f.b.l.read)
         .setPlaceholderTone(autismclient.gui.f.b.l.write);
   }

   public AutismCustomFilterPresetOverlay getPresetOverlay() {
      return this.presetOverlay;
   }

   public AutismPacketSelectorOverlay getPacketSelector() {
      return this.packetSelector;
   }

   @Override
   public String getOverlayId() {
      return "autism-custom-filter";
   }

   @Override
   public int getMinWidth() {
      return this.pop_I_5();
   }

   @Override
   public int getMinHeight() {
      return this.push_I_5() + this.load(1) * 2;
   }

   @Override
   public OverlayBounds getBounds() {
      return new OverlayBounds(this.tick, this.run, this.get, this.set, this.boundsVisible, this.boundsCollapsed);
   }

   @Override
   public void setBounds(OverlayBounds bounds) {
      if (bounds != null) {
         OverlayBounds clamped = this.clampToScreen(this, bounds);
         this.tick = clamped.x;
         this.run = clamped.y;
         this.get = clamped.width;
         this.set = clamped.height;
         this.boundsVisible = clamped.visible;
         this.boundsCollapsed = clamped.collapsed;
      }
   }

   @Override
   public void setVisible(boolean visible) {
      this.boundsVisible = visible;
      if (visible) {
         OverlayManager.getInstance().bringToFront(this);
      } else {
         this.blurSearchFields();
         this.packetSelector.resetSelection();
      }

      this.saveBounds();
   }

   public void toggleFilterOverlay() {
      this.setVisible(!this.boundsVisible);
   }

   @Override
   public boolean isVisible() {
      return this.boundsVisible;
   }

   @Override
   public boolean isCollapsed() {
      return this.boundsCollapsed;
   }

   @Override
   public void setCollapsed(boolean collapsed) {
      if (this.boundsCollapsed != collapsed) {
         this.boundsCollapsed = collapsed;
         if (collapsed) {
            this.clearFocusAndDismissMenu();
            this.packetSelector.resetSelection();
         }

         this.saveBounds();
      }
   }

   @Override
   public boolean supportsHeaderCollapseClick() {
      return true;
   }

   @Override
   public boolean isOverHeader(double mouseX, double mouseY) {
      if (!this.boundsVisible) {
         return false;
      }

      OverlayBounds bounds = this.getBounds();
      return mouseX >= this.tick && mouseX <= this.tick + this.get && mouseY >= this.run && mouseY <= this.run + 14 && !this.isOverHeaderControl(mouseX, mouseY, bounds);
   }

   @Override
   public boolean hasFocusedTextInput() {
      return this.packetSelector.isVisible() || this.c2sSearch.isFocused() || this.s2cSearch.isFocused();
   }

   @Override
   public void clearInputFocus() {
      this.blurSearchFields();
   }

   @Override
   public void render(GuiGraphicsExtractor context, int mouseX, int mouseY, float delta) {
      if (this.boundsVisible || this.isClosing()) {
         this.actionButtons.clear();
         autismclient.modules.ModuleRuntimeManager module = autismclient.modules.ModuleRuntimeManager.getInstance();
         int wantC2s = this.load(this.init(module.getCustomC2sPacketsCopy().size()));
         int wantS2c = this.load(this.init(module.getCustomS2cPacketsCopy().size()));
         this.set = this.push_I_5() + wantC2s + wantS2c;
         OverlayBounds clamped = this.clampToScreen(this, new OverlayBounds(this.tick, this.run, this.get, this.set, this.boundsVisible, this.boundsCollapsed));
         this.tick = clamped.x;
         this.run = clamped.y;
         this.get = clamped.width;
         this.set = clamped.height;
         OverlayBounds bounds = new OverlayBounds(this.tick, this.run, this.get, this.set, this.boundsVisible, this.boundsCollapsed);
         this.renderWindowFrame(context, mouseX, mouseY, bounds, "Packet Delay", this.boundsCollapsed, this.draggingWindow);
         boolean clipBody = this.beginBodyClip(context, bounds, this.boundsCollapsed);
         if (!clipBody) {
            this.renderInactiveDim(context, bounds, this.boundsCollapsed, this.draggingWindow);
         } else {
            try {
               int x = this.tick + this.run_I_3();
               int y = this.run + 14 + this.get_I_3();
               int width = this.get - this.run_I_3() * 2;
               y = this.init(
                  context,
                  mouseX,
                  mouseY,
                  x,
                  y,
                  width,
                  "Custom Packets " + (module.isUseCustomPackets() ? "On" : "Off"),
                  () -> module.setUseCustomPackets(!module.isUseCustomPackets()),
                  module.isUseCustomPackets(),
                  "custom-filter:enabled"
               );
               int presetsY = y;
               y = this.init(context, mouseX, mouseY, x, y, width, "Presets", () -> {
                  this.presetOverlay.toggleVisible();
                  OverlayManager.getInstance().bringToFront(this.presetOverlay);
               }, autismclient.gui.ui.widgets.OverlayActionButton.ButtonTone.SECONDARY, true);
               String counts = "C2S " + module.getCustomC2sPacketsCopy().size() + " | S2C " + module.getCustomS2cPacketsCopy().size();
               int countsW = ThemedTextHelper.init(this.font, counts, ThemedTextHelper.TextTone.MUTED);
               ThemedTextHelper.init(
                  context,
                  this.font,
                  counts,
                  ThemedTextHelper.TextTone.MUTED,
                  x + width - countsW,
                  UiLayoutMath.centerTextY(presetsY, this.set_I_3(), this.theme.init_I_l(autismclient.gui.f.b.l.write), this.theme.flush()),
                  false
               );
               y += this.add_I_3() - this.put();
               int[] lists = this.splitWidths(wantC2s, wantS2c, this.set - this.push_I_5());
               y = this.init(context, mouseX, mouseY, delta, x, y, width, lists[0], true, module, this.c2sSearch, this.c2sList);
               this.init(context, mouseX, mouseY, delta, x, y, width, lists[1], false, module, this.s2cSearch, this.s2cList);
            } finally {
               this.endBodyClip(context, clipBody);
               this.renderInactiveDim(context, bounds, this.boundsCollapsed, this.draggingWindow);
            }

            if (this.packetSelector.isVisible()) {
               this.packetSelector.render(context, mouseX, mouseY, delta);
            }
         }
      }
   }

   private int init(
      GuiGraphicsExtractor context,
      int mouseX,
      int mouseY,
      float delta,
      int x,
      int y,
      int width,
      int listHeight,
      boolean c2s,
      autismclient.modules.ModuleRuntimeManager module,
      autismclient.gui.ui.widgets.TextInputWidget searchField,
      AutismCustomFilterOverlay.Inner_StreamUtil listState
   ) {
      String label = c2s ? "C2S Packets" : "S2C Packets";
      Set<Class<? extends Packet<?>>> selectedPackets = c2s ? module.getCustomC2sPacketsCopy() : module.getCustomS2cPacketsCopy();
      List<Class<? extends Packet<?>>> filteredPackets = this.cachedFilteredPackets(listState, selectedPackets, searchField.getValue());
      int headerY = y;
      autismclient.gui.ui.widgets.ListRowRenderer.drawHeaderTitle(
         context,
         this.font,
         label + " (" + selectedPackets.size() + ")",
         x,
         UiLayoutMath.centerTextY(headerY, this.prev(), this.theme.init_I_l(autismclient.gui.f.b.l.load), this.theme.flush())
      );
      y += this.prev();
      String addLabel = c2s ? "Add C2S" : "Add S2C";
      int addW = Math.min(width / 2, this.buttonWidthFor(addLabel));
      int searchW = Math.max(1, width - addW - this.put());
      searchField.setBounds(x, y, searchW, this.clear());
      searchField.render(this.layoutContext(context, mouseX, mouseY, delta));
      this.init(
         context,
         mouseX,
         mouseY,
         x + searchW + this.put(),
         y,
         addW,
         addLabel,
         () -> {
            if (c2s) {
               this.packetSelector
                  .openC2sMultiSelect((packetClass, selected) -> this.setPacketSelected(true, packetClass, selected), module.getCustomC2sPacketsCopy());
            } else {
               this.packetSelector
                  .openS2cMultiSelect((packetClass, selected) -> this.setPacketSelected(false, packetClass, selected), module.getCustomS2cPacketsCopy());
            }
         },
         autismclient.gui.ui.widgets.OverlayActionButton.ButtonTone.SECONDARY,
         true
      );
      y += Math.max(this.clear(), this.set_I_3()) + this.put();
      autismclient.gui.ui.widgets.ScrollListLayout.ListLayout baseLayout = this.init(x, y, width, listHeight, filteredPackets.size(), 0);
      int visualScroll = listState.scroll.tickAndClamp(delta, baseLayout.maxScroll());
      autismclient.gui.ui.widgets.ScrollListLayout.ListLayout listLayout = this.init(x, y, width, listHeight, filteredPackets.size(), visualScroll);
      boolean focused = OverlayManager.getInstance().isFocused(this) || OverlayManager.getInstance().isTopmost(this);
      listLayout.drawFrame(context, focused);
      int contentWidth = Math.max(24, listLayout.contentWidth());
      listState.init(x, y, width, listHeight, listLayout.scrollbar());
      if (filteredPackets.isEmpty()) {
         autismclient.gui.ui.widgets.ListRowRenderer.drawEmptyPlaceholder(context, this.font, "No packets", x, y, contentWidth);
      } else {
         listLayout.pushContentClip(context);

         try {
            listLayout.forEachVisibleRow(
               filteredPackets.size(),
               (index, rowY) -> {
                  Class<? extends Packet<?>> packetClass = filteredPackets.get(index);
                  boolean hovered = mouseX >= x + 2 && mouseX < x + 2 + contentWidth && mouseY >= rowY && mouseY < rowY + this.reset();
                  String packetName = PacketNameResolver.nameOfClass(packetClass);
                  autismclient.gui.ui.widgets.ListRowRenderer.drawRow(
                     context,
                     this.font,
                     packetName,
                     x + 2,
                     rowY,
                     contentWidth,
                     this.reset(),
                     hovered,
                     false,
                     c2s ? autismclient.gui.ui.widgets.ListRowRenderer.RowTone.WARNING : autismclient.gui.ui.widgets.ListRowRenderer.RowTone.NORMAL
                  );
                  int iconSize = this.reset() - 2;
                  int iconX = x + 2 + contentWidth - iconSize - 1;
                  int iconY = rowY + 1;
                  autismclient.gui.ui.widgets.ListRowRenderer.drawDeleteButtonSquare(context, iconX, iconY, iconSize, hovered);
                  autismclient.gui.ui.widgets.ListRowRenderer.drawRowSeparator(context, x + 2, rowY + this.reset(), contentWidth);
                  this.actionButtons
                     .add(
                        new AutismCustomFilterOverlay.Inner_DataUtil(x + 2, rowY, contentWidth, this.reset(), () -> this.deselectPacket(c2s, packetClass), true)
                     );
               }
            );
         } finally {
            listLayout.popContentClip(context);
         }
      }

      listLayout.renderScrollbar(context, mouseX, mouseY, this.put == (c2s ? 1 : 2));
      String footer = filteredPackets.isEmpty()
         ? ""
         : (
            listLayout.maxScroll() > 0
               ? "Scroll: "
                  + Math.min(filteredPackets.size(), Math.round((float)listState.scroll.getOffset() / this.start_I_3()) + 1)
                  + "/"
                  + Math.max(1, Math.round((float)listLayout.maxScroll() / this.start_I_3()) + 1)
               : "Click a row to remove"
         );
      if (!footer.isEmpty()) {
         int footerW = ThemedTextHelper.init(this.font, footer, ThemedTextHelper.TextTone.MUTED);
         ThemedTextHelper.init(
            context,
            this.font,
            footer,
            ThemedTextHelper.TextTone.MUTED,
            x + width - footerW,
            UiLayoutMath.centerTextY(headerY, this.prev(), this.theme.init_I_l(autismclient.gui.f.b.l.write), this.theme.flush()),
            false
         );
      }

      return y + listHeight + this.add_I_3();
   }

   private int buttonWidthFor(String label) {
      return autismclient.gui.ui.UiDensityMetrics.buttonWidthForText(
         autismclient.gui.f.b.k.init(this.font, label, this.theme.init_Identifier_l(autismclient.gui.f.b.l.read), 0)
      );
   }

   private int init(int count) {
      return count <= 0 ? 1 : Math.max(3, Math.min(10, count));
   }

   private int load(int rows) {
      return rows * this.start_I_3() + 4;
   }

   private int push_I_5() {
      int top = 14 + this.get_I_3() + this.set_I_3() + this.put() + this.set_I_3() + this.put() + this.add_I_3() - this.put();
      int section = this.prev() + Math.max(this.clear(), this.set_I_3()) + this.put() + this.add_I_3();
      return top + section * 2 - this.add_I_3() + this.run_I_3();
   }

   private int[] splitWidths(int wantA, int wantB, int available) {
      if (wantA + wantB <= available) {
         return new int[]{wantA, wantB};
      } else {
         int min = this.load(1);
         int half = Math.max(min, available / 2);
         if (wantA <= half) {
            return new int[]{wantA, Math.max(min, available - wantA)};
         } else {
            return wantB <= half ? new int[]{Math.max(min, available - wantB), wantB} : new int[]{half, Math.max(min, available - half)};
         }
      }
   }

   private List<Class<? extends Packet<?>>> filterPackets(List<Class<? extends Packet<?>>> packets, String query) {
      if (query != null && !query.isBlank()) {
         String lowered = query.toLowerCase(Locale.ROOT);
         List<Class<? extends Packet<?>>> filtered = new ArrayList<>();

         for (Class<? extends Packet<?>> packetClass : packets) {
            String name = PacketNameResolver.nameOfClass(packetClass).toLowerCase(Locale.ROOT);
            String fullName = packetClass.getName().toLowerCase(Locale.ROOT);
            if (name.contains(lowered) || fullName.contains(lowered)) {
               filtered.add(packetClass);
            }
         }

         return filtered;
      } else {
         return packets;
      }
   }

   private List<Class<? extends Packet<?>>> cachedFilteredPackets(
      AutismCustomFilterOverlay.Inner_StreamUtil state, Set<Class<? extends Packet<?>>> packets, String query
   ) {
      String normalizedQuery = query == null ? "" : query.trim().toLowerCase(Locale.ROOT);
      int sourceHash = packets == null ? 0 : packets.hashCode();
      int sourceSize = packets == null ? 0 : packets.size();
      if (state.update == sourceHash && state.sync == sourceSize && state.cachedQuery.equals(normalizedQuery)) {
         return state.filteredPackets;
      }

      List<Class<? extends Packet<?>>> sorted = packets == null ? new ArrayList<>() : new ArrayList<>(packets);
      sorted.sort(Comparator.comparing(PacketNameResolver::nameOfClass, String.CASE_INSENSITIVE_ORDER));
      state.filteredPackets = List.copyOf(this.filterPackets(sorted, normalizedQuery));
      state.update = sourceHash;
      state.sync = sourceSize;
      state.cachedQuery = normalizedQuery;
      return state.filteredPackets;
   }

   private void selectPacket(boolean c2s, Class<? extends Packet<?>> packetClass) {
      autismclient.modules.ModuleRuntimeManager module = autismclient.modules.ModuleRuntimeManager.getInstance();
      Set<Class<? extends Packet<?>>> packets = new LinkedHashSet<>(c2s ? module.getCustomC2sPacketsCopy() : module.getCustomS2cPacketsCopy());
      if (packets.add(packetClass)) {
         if (c2s) {
            module.setCustomC2sPackets(packets);
         } else {
            module.setCustomS2cPackets(packets);
         }
      }
   }

   private void deselectPacket(boolean c2s, Class<? extends Packet<?>> packetClass) {
      autismclient.modules.ModuleRuntimeManager module = autismclient.modules.ModuleRuntimeManager.getInstance();
      Set<Class<? extends Packet<?>>> packets = new LinkedHashSet<>(c2s ? module.getCustomC2sPacketsCopy() : module.getCustomS2cPacketsCopy());
      if (packets.remove(packetClass)) {
         if (c2s) {
            module.setCustomC2sPackets(packets);
         } else {
            module.setCustomS2cPackets(packets);
         }
      }
   }

   private void setPacketSelected(boolean c2s, Class<? extends Packet<?>> packetClass, boolean selected) {
      if (selected) {
         this.selectPacket(c2s, packetClass);
      } else {
         this.deselectPacket(c2s, packetClass);
      }
   }

   private int init(
      GuiGraphicsExtractor context,
      int mouseX,
      int mouseY,
      int x,
      int y,
      int width,
      String label,
      Runnable action,
      autismclient.gui.ui.widgets.OverlayActionButton.ButtonTone variant,
      boolean enabled
   ) {
      int w = Math.min(width, this.buttonWidthFor(label));
      autismclient.gui.ui.widgets.ButtonDrawHelper.init(
         context,
         this.font,
         x,
         y,
         w,
         this.set_I_3(),
         label,
         enabled ? variant : autismclient.gui.ui.widgets.OverlayActionButton.ButtonTone.GHOST,
         enabled,
         mouseX,
         mouseY
      );
      this.actionButtons.add(new AutismCustomFilterOverlay.Inner_DataUtil(x, y, w, this.set_I_3(), action, enabled));
      return y + this.set_I_3() + this.put();
   }

   private int init(
      GuiGraphicsExtractor context, int mouseX, int mouseY, int x, int y, int width, String label, Runnable action, boolean enabled, String animationKey
   ) {
      autismclient.gui.ui.widgets.ButtonDrawHelper.init(context, this.font, x, y, width, this.set_I_3(), label, enabled, animationKey, mouseX, mouseY);
      this.actionButtons.add(new AutismCustomFilterOverlay.Inner_DataUtil(x, y, width, this.set_I_3(), action, true));
      return y + this.set_I_3() + this.put();
   }

   @Override
   public boolean mouseClicked(double mouseX, double mouseY, int button) {
      if (!this.boundsVisible) {
         return false;
      }

      if (this.packetSelector.isVisible()) {
         return this.packetSelector.mouseClicked(mouseX, mouseY, button);
      }

      if (button == 0 && mouseX >= this.tick && mouseX < this.tick + this.get && mouseY >= this.run && mouseY < this.run + 14) {
         OverlayBounds bounds = this.getBounds();
         if (this.isOverHeaderCloseButton(mouseX, mouseY, bounds)) {
            this.setVisible(false);
            return true;
         } else {
            this.draggingWindow = true;
            this.dragOffsetX = mouseX - this.tick;
            this.dragOffsetY = mouseY - this.run;
            this.blurSearchFields();
            return true;
         }
      } else {
         if (this.boundsCollapsed) {
            return false;
         }

         if (button == 0) {
            if (this.init_Z_AutismCustomFilter(this.c2sList, 1, mouseX, mouseY)) {
               return true;
            }

            if (this.init_Z_AutismCustomFilter(this.s2cList, 2, mouseX, mouseY)) {
               return true;
            }
         }

         if (this.clickSearchField(this.c2sSearch, mouseX, mouseY, button)) {
            this.c2sList.scroll.jumpTo(0, 0);
            return true;
         }

         if (this.clickSearchField(this.s2cSearch, mouseX, mouseY, button)) {
            this.s2cList.scroll.jumpTo(0, 0);
            return true;
         }

         this.blurSearchFields();
         if (button == 0) {
            for (AutismCustomFilterOverlay.Inner_DataUtil actionButton : this.actionButtons) {
               if (actionButton.init(mouseX, mouseY)) {
                  if (!actionButton.enabled()) {
                     return true;
                  }

                  actionButton.action.run();
                  return true;
               }
            }
         }

         return this.containsPoint(mouseX, mouseY);
      }
   }

   private boolean clickSearchField(autismclient.gui.ui.widgets.TextInputWidget field, double mouseX, double mouseY, int button) {
      boolean clicked = field.mouseClicked(this.inputLayoutContext(mouseX, mouseY), (float)mouseX, (float)mouseY, button);
      if (clicked) {
         this.blurSearchFields();
         field.setFocused(true);
      }

      return clicked;
   }

   @Override
   public boolean mouseReleased(double mouseX, double mouseY, int button) {
      if (this.packetSelector.isVisible() && this.packetSelector.mouseReleased(mouseX, mouseY, button)) {
         return true;
      }

      if (this.c2sSearch.mouseReleased(this.inputLayoutContext(mouseX, mouseY), (float)mouseX, (float)mouseY, button)) {
         return true;
      }

      if (this.s2cSearch.mouseReleased(this.inputLayoutContext(mouseX, mouseY), (float)mouseX, (float)mouseY, button)) {
         return true;
      }

      if (button == 0 && this.put != 0) {
         this.put = 0;
         return true;
      }

      if (button == 0) {
         if (this.draggingWindow) {
            this.saveBounds();
         }

         this.draggingWindow = false;
      }

      return false;
   }

   @Override
   public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
      if (this.packetSelector.isVisible() && this.packetSelector.mouseDragged(mouseX, mouseY, button, deltaX, deltaY)) {
         return true;
      } else if (this.c2sSearch
         .mouseDragged(this.inputLayoutContext(mouseX, mouseY), (float)mouseX, (float)mouseY, button, (float)deltaX, (float)deltaY)) {
         return true;
      } else if (this.s2cSearch
         .mouseDragged(this.inputLayoutContext(mouseX, mouseY), (float)mouseX, (float)mouseY, button, (float)deltaX, (float)deltaY)) {
         return true;
      } else if (this.put == 1) {
         this.c2sList.scroll.dragToMouseYStepped(this.c2sList.scrollbarMetrics, mouseY, this.add, this.start_I_3());
         return true;
      } else if (this.put == 2) {
         this.s2cList.scroll.dragToMouseYStepped(this.s2cList.scrollbarMetrics, mouseY, this.add, this.start_I_3());
         return true;
      } else if (this.draggingWindow && button == 0) {
         OverlayBounds nextBounds = this.clampToScreen(
            this, new OverlayBounds((int)(mouseX - this.dragOffsetX), (int)(mouseY - this.dragOffsetY), this.get, this.set, this.boundsVisible, this.boundsCollapsed)
         );
         this.tick = nextBounds.x;
         this.run = nextBounds.y;
         return true;
      } else {
         return false;
      }
   }

   @Override
   public boolean mouseScrolled(double mouseX, double mouseY, double amount) {
      if (this.packetSelector.isVisible()) {
         return this.packetSelector.mouseScrolled(mouseX, mouseY, amount);
      } else if (this.c2sList.init(mouseX, mouseY)) {
         int maxScroll = this.init(
               this.c2sList.init_I,
               this.c2sList.load,
               this.c2sList.read,
               this.c2sList.write,
               this.c2sList.filteredPackets.size(),
               0
            )
            .maxScroll();
         this.c2sList.scroll.scrollByWheel(amount, this.start_I_3(), maxScroll);
         return true;
      } else if (this.s2cList.init(mouseX, mouseY)) {
         int maxScroll = this.init(
               this.s2cList.init_I,
               this.s2cList.load,
               this.s2cList.read,
               this.s2cList.write,
               this.s2cList.filteredPackets.size(),
               0
            )
            .maxScroll();
         this.s2cList.scroll.scrollByWheel(amount, this.start_I_3(), maxScroll);
         return true;
      } else {
         return false;
      }
   }

   private autismclient.gui.ui.widgets.ScrollListLayout.ListLayout init(int x, int y, int width, int listHeight, int rowCount, int scrollOffset) {
      return autismclient.gui.ui.widgets.ScrollListLayout.compute(x, y, width, listHeight, rowCount, this.reset(), this.start_I_3(), scrollOffset, this.next(), this.stop());
   }

   private boolean init_Z_AutismCustomFilter(AutismCustomFilterOverlay.Inner_StreamUtil listState, int dragId, double mouseX, double mouseY) {
      autismclient.gui.ui.widgets.ScrollbarMetricsHelper.Metrics metrics = listState.scrollbarMetrics;
      if (metrics == null || !metrics.isScrollable()) {
         return false;
      }

      if (!metrics.containsTrack(mouseX, mouseY)) {
         return false;
      }

      this.put = dragId;
      this.add = Math.max(0, (int)mouseY - metrics.thumbY());
      listState.scroll.dragToMouseYStepped(metrics, mouseY, this.add, this.start_I_3());
      return true;
   }

   @Override
   public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
      if (this.packetSelector.isVisible()) {
         return this.packetSelector.keyPressed(keyCode, scanCode, modifiers);
      } else if (!this.boundsVisible) {
         return false;
      } else if (keyCode == 256) {
         this.blurSearchFields();
         return false;
      } else {
         autismclient.gui.ui.widgets.TextInputWidget focusedField = this.focusedSearchField();
         if (focusedField == null) {
            return false;
         } else if (keyCode != 257 && keyCode != 335) {
            focusedField.keyPressed(this.inputLayoutContext(0.0, 0.0), keyCode, scanCode, modifiers);
            return true;
         } else {
            return true;
         }
      }
   }

   @Override
   public boolean charTyped(char chr, int modifiers) {
      if (this.packetSelector.isVisible()) {
         return this.packetSelector.charTyped(chr, modifiers);
      }

      if (!this.boundsVisible) {
         return false;
      }

      autismclient.gui.ui.widgets.TextInputWidget focusedField = this.focusedSearchField();
      return focusedField == null ? false : focusedField.charTyped(this.inputLayoutContext(0.0, 0.0), chr, modifiers);
   }

   @Override
   public boolean containsPoint(double mouseX, double mouseY) {
      if (!this.boundsVisible) {
         return false;
      }

      if (this.packetSelector.isVisible()) {
         return true;
      }

      OverlayBounds bounds = this.getBounds();
      int frameHeight = this.boundsCollapsed ? 14 : this.set;
      return mouseX >= bounds.x && mouseX <= bounds.x + bounds.width && mouseY >= bounds.y && mouseY <= bounds.y + frameHeight
         ? true
         : this.isOverField(this.c2sSearch, mouseX, mouseY) || this.isOverField(this.s2cSearch, mouseX, mouseY);
   }

   private autismclient.gui.ui.widgets.TextInputWidget focusedSearchField() {
      if (this.c2sSearch.isFocused()) {
         return this.c2sSearch;
      } else {
         return this.s2cSearch.isFocused() ? this.s2cSearch : null;
      }
   }

   private int pop_I_5() {
      return 220;
   }

   private int tick_I_3() {
      return this.push_I_5() + this.load(3) * 2;
   }

   private int run_I_3() {
      return 4;
   }

   private int get_I_3() {
      return 4;
   }

   private int set_I_3() {
      return autismclient.gui.ui.UiDensityMetrics.scaledHeight(13);
   }

   private int put() {
      return 2;
   }

   private int add_I_3() {
      return 4;
   }

   private int clear() {
      return autismclient.gui.ui.UiDensityMetrics.scaledHeight(13);
   }

   private int reset() {
      return autismclient.gui.ui.UiDensityMetrics.scaledHeight(14);
   }

   private int start_I_3() {
      return this.reset() + 1;
   }

   private int stop() {
      return 8;
   }

   private int next() {
      return 3;
   }

   private int prev() {
      return autismclient.gui.ui.UiDensityMetrics.scaledHeight(12);
   }

   private void blurSearchFields() {
      this.c2sSearch.setFocused(false);
      this.s2cSearch.setFocused(false);
   }

   private autismclient.gui.ui.layout.LayoutContext layoutContext(GuiGraphicsExtractor context, int mouseX, int mouseY, float delta) {
      autismclient.gui.ui.layout.LayoutViewport viewport = autismclient.gui.ui.layout.LayoutViewport.create(1.0F);
      return new autismclient.gui.ui.layout.LayoutContext(
         context, this.font, viewport, this.theme, viewport.toUiX(mouseX), viewport.toUiY(mouseY), delta
      );
   }

   private autismclient.gui.ui.layout.LayoutContext inputLayoutContext(double mouseX, double mouseY) {
      autismclient.gui.ui.layout.LayoutViewport viewport = autismclient.gui.ui.layout.LayoutViewport.create(1.0F);
      return new autismclient.gui.ui.layout.LayoutContext(null, this.font, viewport, this.theme, viewport.toUiX(mouseX), viewport.toUiY(mouseY), 0.0F);
   }

   private boolean isOverField(autismclient.gui.ui.widgets.TextInputWidget field, double mouseX, double mouseY) {
      return mouseX >= field.x() && mouseX <= field.x() + field.width() && mouseY >= field.y() && mouseY <= field.y() + field.height();
   }

   private record Inner_DataUtil(int init_I, int load, int read, int write, Runnable action, boolean enabled) {
      boolean init(double mouseX, double mouseY) {
         return mouseX >= this.init_I && mouseX < this.init_I + this.read && mouseY >= this.load && mouseY < this.load + this.write;
      }
   }

   private static final class Inner_StreamUtil {
      private int init_I;
      private int load;
      private int read;
      private int write;
      private final autismclient.gui.ui.widgets.ScrollOffsetController scroll = new autismclient.gui.ui.widgets.ScrollOffsetController();
      private autismclient.gui.ui.widgets.ScrollbarMetricsHelper.Metrics scrollbarMetrics = null;
      private List<Class<? extends Packet<?>>> filteredPackets = List.of();
      private int update;
      private int sync = -1;
      private String cachedQuery = "";

      private void init(int x, int y, int width, int height, autismclient.gui.ui.widgets.ScrollbarMetricsHelper.Metrics scrollbarMetrics) {
         this.init_I = x;
         this.load = y;
         this.read = width;
         this.write = height;
         this.scrollbarMetrics = scrollbarMetrics;
      }

      private boolean init(double mouseX, double mouseY) {
         return mouseX >= this.init_I && mouseX <= this.init_I + this.read && mouseY >= this.load && mouseY <= this.load + this.write;
      }
   }
}

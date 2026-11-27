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
   private static final Minecraft init_Minecraft = Minecraft.getInstance();
   private static final int flush_I = 220;
   private static final int close_I = 10;
   private static final int open = 3;
   private static final int poll = 0;
   private static final int push = 1;
   private static final int pop = 2;
   private final Font init_Font;
   private final autismclient.gui.f.b.CharEncoder init_CharEncoder = new autismclient.gui.f.b.CharEncoder();
   private final autismclient.gui.ui.widgets.TextInputWidget init_BitSetUtil;
   private final autismclient.gui.ui.widgets.TextInputWidget load_BitSetUtil;
   private final AutismPacketSelectorOverlay init_AutismPacketSelect;
   private final AutismCustomFilterPresetOverlay init_AutismCustomFilter_2;
   private final List<AutismCustomFilterOverlay.Inner_DataUtil> init_List = new ArrayList<>();
   private final AutismCustomFilterOverlay.Inner_StreamUtil init_AutismCustomFilter = new AutismCustomFilterOverlay.Inner_StreamUtil();
   private final AutismCustomFilterOverlay.Inner_StreamUtil load_AutismCustomFilter = new AutismCustomFilterOverlay.Inner_StreamUtil();
   private int tick = 260;
   private int run = 36;
   private int get = 220;
   private int set = 200;
   private boolean write_Z = false;
   private boolean update_Z = false;
   private boolean sync_Z = false;
   private double init_D = 0.0;
   private double load_D = 0.0;
   private int put = 0;
   private int add = 0;

   public AutismCustomFilterOverlay(Font textRenderer) {
      this.init_Font = textRenderer;
      this.get = this.pop_I_5();
      this.set = this.tick_I_3();
      this.init_BitSetUtil = this.init_BitSetUtil("Search C2S packets...");
      this.load_BitSetUtil = this.init_BitSetUtil("Search S2C packets...");
      this.init_AutismPacketSelect = new AutismPacketSelectorOverlay(textRenderer);
      this.init_AutismCustomFilter_2 = new AutismCustomFilterPresetOverlay(textRenderer);
      this.init_BitSetUtil.init_BitSetUtil_Consumer(text -> this.init_AutismCustomFilter.init_CacheEntry.jumpTo(0, 0));
      this.load_BitSetUtil.init_BitSetUtil_Consumer(text -> this.load_AutismCustomFilter.init_CacheEntry.jumpTo(0, 0));
   }

   private autismclient.gui.ui.widgets.TextInputWidget init_BitSetUtil(String placeholder) {
      return new autismclient.gui.ui.widgets.TextInputWidget()
         .load_BitSetUtil_Str(placeholder)
         .read_BitSetUtil_I(this.clear())
         .init_BitSetUtil_F(120.0F)
         .read_BitSetUtil_F(120.0F)
         .init_BitSetUtil_l(autismclient.gui.f.b.l.read)
         .load_BitSetUtil_l(autismclient.gui.f.b.l.write);
   }

   public AutismCustomFilterPresetOverlay init_AutismCustomFilter() {
      return this.init_AutismCustomFilter_2;
   }

   public AutismPacketSelectorOverlay init_AutismPacketSelect() {
      return this.init_AutismPacketSelect;
   }

   @Override
   public String init_Str() {
      return "autism-custom-filter";
   }

   @Override
   public int init_I() {
      return this.pop_I_5();
   }

   @Override
   public int load_I() {
      return this.push_I_5() + this.load(1) * 2;
   }

   @Override
   public OverlayBounds init_cH() {
      return new OverlayBounds(this.tick, this.run, this.get, this.set, this.write_Z, this.update_Z);
   }

   @Override
   public void init_V_cH(OverlayBounds bounds) {
      if (bounds != null) {
         OverlayBounds clamped = this.clampToScreen(this, bounds);
         this.tick = clamped.x;
         this.run = clamped.y;
         this.get = clamped.width;
         this.set = clamped.height;
         this.write_Z = clamped.visible;
         this.update_Z = clamped.collapsed;
      }
   }

   @Override
   public void init_V_Z(boolean visible) {
      this.write_Z = visible;
      if (visible) {
         OverlayManager.getInstance().bringToFront(this);
      } else {
         this.write_V_6();
         this.init_AutismPacketSelect.write_V_21();
      }

      this.a__V();
   }

   public void load_V_5() {
      this.init_V_Z(!this.write_Z);
   }

   @Override
   public boolean sync_Z() {
      return this.write_Z;
   }

   @Override
   public boolean flush_Z() {
      return this.update_Z;
   }

   @Override
   public void write_V_Z(boolean collapsed) {
      if (this.update_Z != collapsed) {
         this.update_Z = collapsed;
         if (collapsed) {
            this.get_V();
            this.init_AutismPacketSelect.write_V_21();
         }

         this.a__V();
      }
   }

   @Override
   public boolean load_Z() {
      return true;
   }

   @Override
   public boolean load_Z_D_D(double mouseX, double mouseY) {
      if (!this.write_Z) {
         return false;
      }

      OverlayBounds bounds = this.init_cH();
      return mouseX >= this.tick && mouseX <= this.tick + this.get && mouseY >= this.run && mouseY <= this.run + 14 && !this.isOverHeaderControl(mouseX, mouseY, bounds);
   }

   @Override
   public boolean write_Z() {
      return this.init_AutismPacketSelect.sync_Z() || this.init_BitSetUtil.load_Z() || this.load_BitSetUtil.load_Z();
   }

   @Override
   public void push_V() {
      this.write_V_6();
   }

   @Override
   public void init_V_GuiGraphicsExtract_15(GuiGraphicsExtractor context, int mouseX, int mouseY, float delta) {
      if (this.write_Z || this.a__Z()) {
         this.init_List.clear();
         autismclient.modules.ModuleRuntimeManager module = autismclient.modules.ModuleRuntimeManager.getInstance();
         int wantC2s = this.load(this.init(module.getCustomC2sPacketsCopy().size()));
         int wantS2c = this.load(this.init(module.getCustomS2cPacketsCopy().size()));
         this.set = this.push_I_5() + wantC2s + wantS2c;
         OverlayBounds clamped = this.clampToScreen(this, new OverlayBounds(this.tick, this.run, this.get, this.set, this.write_Z, this.update_Z));
         this.tick = clamped.x;
         this.run = clamped.y;
         this.get = clamped.width;
         this.set = clamped.height;
         OverlayBounds bounds = new OverlayBounds(this.tick, this.run, this.get, this.set, this.write_Z, this.update_Z);
         this.renderWindowFrame(context, mouseX, mouseY, bounds, "Packet Delay", this.update_Z, this.sync_Z);
         boolean clipBody = this.beginBodyClip(context, bounds, this.update_Z);
         if (!clipBody) {
            this.renderInactiveDim(context, bounds, this.update_Z, this.sync_Z);
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
                  this.init_AutismCustomFilter_2.load_V_6();
                  OverlayManager.getInstance().bringToFront(this.init_AutismCustomFilter_2);
               }, autismclient.gui.ui.widgets.OverlayActionButton.ButtonTone.SECONDARY, true);
               String counts = "C2S " + module.getCustomC2sPacketsCopy().size() + " | S2C " + module.getCustomS2cPacketsCopy().size();
               int countsW = ThemedTextHelper.init(this.init_Font, counts, ThemedTextHelper.TextTone.MUTED);
               ThemedTextHelper.init(
                  context,
                  this.init_Font,
                  counts,
                  ThemedTextHelper.TextTone.MUTED,
                  x + width - countsW,
                  UiLayoutMath.centerTextY(presetsY, this.set_I_3(), this.init_CharEncoder.init_I_l(autismclient.gui.f.b.l.write), this.init_CharEncoder.flush()),
                  false
               );
               y += this.add_I_3() - this.put();
               int[] lists = this.init_IArr_I(wantC2s, wantS2c, this.set - this.push_I_5());
               y = this.init(context, mouseX, mouseY, delta, x, y, width, lists[0], true, module, this.init_BitSetUtil, this.init_AutismCustomFilter);
               this.init(context, mouseX, mouseY, delta, x, y, width, lists[1], false, module, this.load_BitSetUtil, this.load_AutismCustomFilter);
            } finally {
               this.endBodyClip(context, clipBody);
               this.renderInactiveDim(context, bounds, this.update_Z, this.sync_Z);
            }

            if (this.init_AutismPacketSelect.sync_Z()) {
               this.init_AutismPacketSelect.init_V_GuiGraphicsExtract_15(context, mouseX, mouseY, delta);
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
      List<Class<? extends Packet<?>>> filteredPackets = this.init_List_AutismCustomFilter(listState, selectedPackets, searchField.init_Str());
      int headerY = y;
      autismclient.gui.ui.widgets.ListRowRenderer.drawHeaderTitle(
         context,
         this.init_Font,
         label + " (" + selectedPackets.size() + ")",
         x,
         UiLayoutMath.centerTextY(headerY, this.prev(), this.init_CharEncoder.init_I_l(autismclient.gui.f.b.l.load), this.init_CharEncoder.flush())
      );
      y += this.prev();
      String addLabel = c2s ? "Add C2S" : "Add S2C";
      int addW = Math.min(width / 2, this.init_I_2(addLabel));
      int searchW = Math.max(1, width - addW - this.put());
      searchField.init_TableIndex_F(x, y, searchW, this.clear());
      searchField.init_V_SyncManager(this.init_SyncManager_GuiGraphicsExtract(context, mouseX, mouseY, delta));
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
               this.init_AutismPacketSelect
                  .init_V_BiConsumer_Collection((packetClass, selected) -> this.init_V_Z_2(true, packetClass, selected), module.getCustomC2sPacketsCopy());
            } else {
               this.init_AutismPacketSelect
                  .load_V_BiConsumer_Collection((packetClass, selected) -> this.init_V_Z_2(false, packetClass, selected), module.getCustomS2cPacketsCopy());
            }
         },
         autismclient.gui.ui.widgets.OverlayActionButton.ButtonTone.SECONDARY,
         true
      );
      y += Math.max(this.clear(), this.set_I_3()) + this.put();
      autismclient.gui.ui.widgets.ScrollListLayout.ListLayout baseLayout = this.init(x, y, width, listHeight, filteredPackets.size(), 0);
      int visualScroll = listState.init_CacheEntry.tickAndClamp(delta, baseLayout.open());
      autismclient.gui.ui.widgets.ScrollListLayout.ListLayout listLayout = this.init(x, y, width, listHeight, filteredPackets.size(), visualScroll);
      boolean focused = OverlayManager.getInstance().isFocused(this) || OverlayManager.getInstance().isTopmost(this);
      listLayout.init(context, focused);
      int contentWidth = Math.max(24, listLayout.push());
      listState.init(x, y, width, listHeight, listLayout.init_PoolEntry_DataUtil());
      if (filteredPackets.isEmpty()) {
         autismclient.gui.ui.widgets.ListRowRenderer.drawEmptyPlaceholder(context, this.init_Font, "No packets", x, y, contentWidth);
      } else {
         listLayout.init(context);

         try {
            listLayout.init(
               filteredPackets.size(),
               (index, rowY) -> {
                  Class<? extends Packet<?>> packetClass = filteredPackets.get(index);
                  boolean hovered = mouseX >= x + 2 && mouseX < x + 2 + contentWidth && mouseY >= rowY && mouseY < rowY + this.reset();
                  String packetName = PacketNameResolver.init_Str_Class(packetClass);
                  autismclient.gui.ui.widgets.ListRowRenderer.drawRow(
                     context,
                     this.init_Font,
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
                  this.init_List
                     .add(
                        new AutismCustomFilterOverlay.Inner_DataUtil(x + 2, rowY, contentWidth, this.reset(), () -> this.load_V_Z_Class(c2s, packetClass), true)
                     );
               }
            );
         } finally {
            listLayout.load(context);
         }
      }

      listLayout.init(context, mouseX, mouseY, this.put == (c2s ? 1 : 2));
      String footer = filteredPackets.isEmpty()
         ? ""
         : (
            listLayout.open() > 0
               ? "Scroll: "
                  + Math.min(filteredPackets.size(), Math.round((float)listState.init_CacheEntry.getOffset() / this.start_I_3()) + 1)
                  + "/"
                  + Math.max(1, Math.round((float)listLayout.open() / this.start_I_3()) + 1)
               : "Click a row to remove"
         );
      if (!footer.isEmpty()) {
         int footerW = ThemedTextHelper.init(this.init_Font, footer, ThemedTextHelper.TextTone.MUTED);
         ThemedTextHelper.init(
            context,
            this.init_Font,
            footer,
            ThemedTextHelper.TextTone.MUTED,
            x + width - footerW,
            UiLayoutMath.centerTextY(headerY, this.prev(), this.init_CharEncoder.init_I_l(autismclient.gui.f.b.l.write), this.init_CharEncoder.flush()),
            false
         );
      }

      return y + listHeight + this.add_I_3();
   }

   private int init_I_2(String label) {
      return autismclient.gui.ui.UiDensityMetrics.buttonWidthForText(
         autismclient.gui.f.b.k.init(this.init_Font, label, this.init_CharEncoder.init_Identifier_l(autismclient.gui.f.b.l.read), 0)
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

   private int[] init_IArr_I(int wantA, int wantB, int available) {
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

   private List<Class<? extends Packet<?>>> init_List_List_Str_2(List<Class<? extends Packet<?>>> packets, String query) {
      if (query != null && !query.isBlank()) {
         String lowered = query.toLowerCase(Locale.ROOT);
         List<Class<? extends Packet<?>>> filtered = new ArrayList<>();

         for (Class<? extends Packet<?>> packetClass : packets) {
            String name = PacketNameResolver.init_Str_Class(packetClass).toLowerCase(Locale.ROOT);
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

   private List<Class<? extends Packet<?>>> init_List_AutismCustomFilter(
      AutismCustomFilterOverlay.Inner_StreamUtil state, Set<Class<? extends Packet<?>>> packets, String query
   ) {
      String normalizedQuery = query == null ? "" : query.trim().toLowerCase(Locale.ROOT);
      int sourceHash = packets == null ? 0 : packets.hashCode();
      int sourceSize = packets == null ? 0 : packets.size();
      if (state.update == sourceHash && state.sync == sourceSize && state.init_Str.equals(normalizedQuery)) {
         return state.init_List;
      }

      List<Class<? extends Packet<?>>> sorted = packets == null ? new ArrayList<>() : new ArrayList<>(packets);
      sorted.sort(Comparator.comparing(PacketNameResolver::init_Str_Class, String.CASE_INSENSITIVE_ORDER));
      state.init_List = List.copyOf(this.init_List_List_Str_2(sorted, normalizedQuery));
      state.update = sourceHash;
      state.sync = sourceSize;
      state.init_Str = normalizedQuery;
      return state.init_List;
   }

   private void init_V_Z_Class(boolean c2s, Class<? extends Packet<?>> packetClass) {
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

   private void load_V_Z_Class(boolean c2s, Class<? extends Packet<?>> packetClass) {
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

   private void init_V_Z_2(boolean c2s, Class<? extends Packet<?>> packetClass, boolean selected) {
      if (selected) {
         this.init_V_Z_Class(c2s, packetClass);
      } else {
         this.load_V_Z_Class(c2s, packetClass);
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
      int w = Math.min(width, this.init_I_2(label));
      autismclient.gui.ui.widgets.ButtonDrawHelper.init(
         context,
         this.init_Font,
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
      this.init_List.add(new AutismCustomFilterOverlay.Inner_DataUtil(x, y, w, this.set_I_3(), action, enabled));
      return y + this.set_I_3() + this.put();
   }

   private int init(
      GuiGraphicsExtractor context, int mouseX, int mouseY, int x, int y, int width, String label, Runnable action, boolean enabled, String animationKey
   ) {
      autismclient.gui.ui.widgets.ButtonDrawHelper.init(context, this.init_Font, x, y, width, this.set_I_3(), label, enabled, animationKey, mouseX, mouseY);
      this.init_List.add(new AutismCustomFilterOverlay.Inner_DataUtil(x, y, width, this.set_I_3(), action, true));
      return y + this.set_I_3() + this.put();
   }

   @Override
   public boolean init_Z_D_3(double mouseX, double mouseY, int button) {
      if (!this.write_Z) {
         return false;
      }

      if (this.init_AutismPacketSelect.sync_Z()) {
         return this.init_AutismPacketSelect.init_Z_D_3(mouseX, mouseY, button);
      }

      if (button == 0 && mouseX >= this.tick && mouseX < this.tick + this.get && mouseY >= this.run && mouseY < this.run + 14) {
         OverlayBounds bounds = this.init_cH();
         if (this.isOverHeaderCloseButton(mouseX, mouseY, bounds)) {
            this.init_V_Z(false);
            return true;
         } else {
            this.sync_Z = true;
            this.init_D = mouseX - this.tick;
            this.load_D = mouseY - this.run;
            this.write_V_6();
            return true;
         }
      } else {
         if (this.update_Z) {
            return false;
         }

         if (button == 0) {
            if (this.init_Z_AutismCustomFilter(this.init_AutismCustomFilter, 1, mouseX, mouseY)) {
               return true;
            }

            if (this.init_Z_AutismCustomFilter(this.load_AutismCustomFilter, 2, mouseX, mouseY)) {
               return true;
            }
         }

         if (this.init_Z_BitSetUtil(this.init_BitSetUtil, mouseX, mouseY, button)) {
            this.init_AutismCustomFilter.init_CacheEntry.jumpTo(0, 0);
            return true;
         }

         if (this.init_Z_BitSetUtil(this.load_BitSetUtil, mouseX, mouseY, button)) {
            this.load_AutismCustomFilter.init_CacheEntry.jumpTo(0, 0);
            return true;
         }

         this.write_V_6();
         if (button == 0) {
            for (AutismCustomFilterOverlay.Inner_DataUtil actionButton : this.init_List) {
               if (actionButton.init(mouseX, mouseY)) {
                  if (!actionButton.init_Z()) {
                     return true;
                  }

                  actionButton.init_Runnable.run();
                  return true;
               }
            }
         }

         return this.init_Z_D_D(mouseX, mouseY);
      }
   }

   private boolean init_Z_BitSetUtil(autismclient.gui.ui.widgets.TextInputWidget field, double mouseX, double mouseY, int button) {
      boolean clicked = field.init_Z_SyncManager_2(this.init_SyncManager_D_D(mouseX, mouseY), (float)mouseX, (float)mouseY, button);
      if (clicked) {
         this.write_V_6();
         field.init_V_Z(true);
      }

      return clicked;
   }

   @Override
   public boolean load_Z_D_3(double mouseX, double mouseY, int button) {
      if (this.init_AutismPacketSelect.sync_Z() && this.init_AutismPacketSelect.load_Z_D_3(mouseX, mouseY, button)) {
         return true;
      }

      if (this.init_BitSetUtil.load_Z_SyncManager(this.init_SyncManager_D_D(mouseX, mouseY), (float)mouseX, (float)mouseY, button)) {
         return true;
      }

      if (this.load_BitSetUtil.load_Z_SyncManager(this.init_SyncManager_D_D(mouseX, mouseY), (float)mouseX, (float)mouseY, button)) {
         return true;
      }

      if (button == 0 && this.put != 0) {
         this.put = 0;
         return true;
      }

      if (button == 0) {
         if (this.sync_Z) {
            this.a__V();
         }

         this.sync_Z = false;
      }

      return false;
   }

   @Override
   public boolean init_Z_D(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
      if (this.init_AutismPacketSelect.sync_Z() && this.init_AutismPacketSelect.init_Z_D(mouseX, mouseY, button, deltaX, deltaY)) {
         return true;
      } else if (this.init_BitSetUtil
         .init_Z_SyncManager(this.init_SyncManager_D_D(mouseX, mouseY), (float)mouseX, (float)mouseY, button, (float)deltaX, (float)deltaY)) {
         return true;
      } else if (this.load_BitSetUtil
         .init_Z_SyncManager(this.init_SyncManager_D_D(mouseX, mouseY), (float)mouseX, (float)mouseY, button, (float)deltaX, (float)deltaY)) {
         return true;
      } else if (this.put == 1) {
         this.init_AutismCustomFilter.init_CacheEntry.dragToMouseYStepped(this.init_AutismCustomFilter.init_PoolEntry_DataUtil, mouseY, this.add, this.start_I_3());
         return true;
      } else if (this.put == 2) {
         this.load_AutismCustomFilter.init_CacheEntry.dragToMouseYStepped(this.load_AutismCustomFilter.init_PoolEntry_DataUtil, mouseY, this.add, this.start_I_3());
         return true;
      } else if (this.sync_Z && button == 0) {
         OverlayBounds nextBounds = this.clampToScreen(
            this, new OverlayBounds((int)(mouseX - this.init_D), (int)(mouseY - this.load_D), this.get, this.set, this.write_Z, this.update_Z)
         );
         this.tick = nextBounds.x;
         this.run = nextBounds.y;
         return true;
      } else {
         return false;
      }
   }

   @Override
   public boolean init_Z_D_2(double mouseX, double mouseY, double amount) {
      if (this.init_AutismPacketSelect.sync_Z()) {
         return this.init_AutismPacketSelect.init_Z_D_2(mouseX, mouseY, amount);
      } else if (this.init_AutismCustomFilter.init(mouseX, mouseY)) {
         int maxScroll = this.init(
               this.init_AutismCustomFilter.init_I,
               this.init_AutismCustomFilter.load,
               this.init_AutismCustomFilter.read,
               this.init_AutismCustomFilter.write,
               this.init_AutismCustomFilter.init_List.size(),
               0
            )
            .open();
         this.init_AutismCustomFilter.init_CacheEntry.scrollByWheel(amount, this.start_I_3(), maxScroll);
         return true;
      } else if (this.load_AutismCustomFilter.init(mouseX, mouseY)) {
         int maxScroll = this.init(
               this.load_AutismCustomFilter.init_I,
               this.load_AutismCustomFilter.load,
               this.load_AutismCustomFilter.read,
               this.load_AutismCustomFilter.write,
               this.load_AutismCustomFilter.init_List.size(),
               0
            )
            .open();
         this.load_AutismCustomFilter.init_CacheEntry.scrollByWheel(amount, this.start_I_3(), maxScroll);
         return true;
      } else {
         return false;
      }
   }

   private autismclient.gui.ui.widgets.ScrollListLayout.ListLayout init(int x, int y, int width, int listHeight, int rowCount, int scrollOffset) {
      return autismclient.gui.ui.widgets.ScrollListLayout.init(x, y, width, listHeight, rowCount, this.reset(), this.start_I_3(), scrollOffset, this.next(), this.stop());
   }

   private boolean init_Z_AutismCustomFilter(AutismCustomFilterOverlay.Inner_StreamUtil listState, int dragId, double mouseX, double mouseY) {
      autismclient.gui.ui.widgets.ScrollbarMetricsHelper.Metrics metrics = listState.init_PoolEntry_DataUtil;
      if (metrics == null || !metrics.isScrollable()) {
         return false;
      }

      if (!metrics.containsTrack(mouseX, mouseY)) {
         return false;
      }

      this.put = dragId;
      this.add = Math.max(0, (int)mouseY - metrics.thumbY());
      listState.init_CacheEntry.dragToMouseYStepped(metrics, mouseY, this.add, this.start_I_3());
      return true;
   }

   @Override
   public boolean init_Z_I(int keyCode, int scanCode, int modifiers) {
      if (this.init_AutismPacketSelect.sync_Z()) {
         return this.init_AutismPacketSelect.init_Z_I(keyCode, scanCode, modifiers);
      } else if (!this.write_Z) {
         return false;
      } else if (keyCode == 256) {
         this.write_V_6();
         return false;
      } else {
         autismclient.gui.ui.widgets.TextInputWidget focusedField = this.init_BitSetUtil_2();
         if (focusedField == null) {
            return false;
         } else if (keyCode != 257 && keyCode != 335) {
            focusedField.init_Z_SyncManager_3(this.init_SyncManager_D_D(0.0, 0.0), keyCode, scanCode, modifiers);
            return true;
         } else {
            return true;
         }
      }
   }

   @Override
   public boolean init_Z_C_I(char chr, int modifiers) {
      if (this.init_AutismPacketSelect.sync_Z()) {
         return this.init_AutismPacketSelect.init_Z_C_I(chr, modifiers);
      }

      if (!this.write_Z) {
         return false;
      }

      autismclient.gui.ui.widgets.TextInputWidget focusedField = this.init_BitSetUtil_2();
      return focusedField == null ? false : focusedField.init_Z_SyncManager_2(this.init_SyncManager_D_D(0.0, 0.0), chr, modifiers);
   }

   @Override
   public boolean init_Z_D_D(double mouseX, double mouseY) {
      if (!this.write_Z) {
         return false;
      }

      if (this.init_AutismPacketSelect.sync_Z()) {
         return true;
      }

      OverlayBounds bounds = this.init_cH();
      int frameHeight = this.update_Z ? 14 : this.set;
      return mouseX >= bounds.x && mouseX <= bounds.x + bounds.width && mouseY >= bounds.y && mouseY <= bounds.y + frameHeight
         ? true
         : this.init_Z_BitSetUtil(this.init_BitSetUtil, mouseX, mouseY) || this.init_Z_BitSetUtil(this.load_BitSetUtil, mouseX, mouseY);
   }

   private autismclient.gui.ui.widgets.TextInputWidget init_BitSetUtil_2() {
      if (this.init_BitSetUtil.load_Z()) {
         return this.init_BitSetUtil;
      } else {
         return this.load_BitSetUtil.load_Z() ? this.load_BitSetUtil : null;
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

   private void write_V_6() {
      this.init_BitSetUtil.init_V_Z(false);
      this.load_BitSetUtil.init_V_Z(false);
   }

   private autismclient.gui.ui.layout.LayoutContext init_SyncManager_GuiGraphicsExtract(GuiGraphicsExtractor context, int mouseX, int mouseY, float delta) {
      autismclient.gui.ui.layout.LayoutViewport viewport = autismclient.gui.ui.layout.LayoutViewport.create(1.0F);
      return new autismclient.gui.ui.layout.LayoutContext(
         context, this.init_Font, viewport, this.init_CharEncoder, viewport.toUiX(mouseX), viewport.toUiY(mouseY), delta
      );
   }

   private autismclient.gui.ui.layout.LayoutContext init_SyncManager_D_D(double mouseX, double mouseY) {
      autismclient.gui.ui.layout.LayoutViewport viewport = autismclient.gui.ui.layout.LayoutViewport.create(1.0F);
      return new autismclient.gui.ui.layout.LayoutContext(null, this.init_Font, viewport, this.init_CharEncoder, viewport.toUiX(mouseX), viewport.toUiY(mouseY), 0.0F);
   }

   private boolean init_Z_BitSetUtil(autismclient.gui.ui.widgets.TextInputWidget field, double mouseX, double mouseY) {
      return mouseX >= field.load_F() && mouseX <= field.load_F() + field.write_F() && mouseY >= field.read_F() && mouseY <= field.read_F() + field.update_F();
   }

   private record Inner_DataUtil(int init_I, int load, int read, int write, Runnable init_Runnable, boolean init_Z) {
      boolean init(double mouseX, double mouseY) {
         return mouseX >= this.init_I && mouseX < this.init_I + this.read && mouseY >= this.load && mouseY < this.load + this.write;
      }
   }

   private static final class Inner_StreamUtil {
      private int init_I;
      private int load;
      private int read;
      private int write;
      private final autismclient.gui.ui.widgets.ScrollOffsetController init_CacheEntry = new autismclient.gui.ui.widgets.ScrollOffsetController();
      private autismclient.gui.ui.widgets.ScrollbarMetricsHelper.Metrics init_PoolEntry_DataUtil = null;
      private List<Class<? extends Packet<?>>> init_List = List.of();
      private int update;
      private int sync = -1;
      private String init_Str = "";

      private void init(int x, int y, int width, int height, autismclient.gui.ui.widgets.ScrollbarMetricsHelper.Metrics scrollbarMetrics) {
         this.init_I = x;
         this.load = y;
         this.read = width;
         this.write = height;
         this.init_PoolEntry_DataUtil = scrollbarMetrics;
      }

      private boolean init(double mouseX, double mouseY) {
         return mouseX >= this.init_I && mouseX <= this.init_I + this.read && mouseY >= this.load && mouseY <= this.load + this.write;
      }
   }
}

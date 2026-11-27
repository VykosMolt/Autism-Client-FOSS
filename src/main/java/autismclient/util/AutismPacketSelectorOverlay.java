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
   private static final Minecraft init_Minecraft = Minecraft.getInstance();
   private static final int flush_I = 230;
   private static final int close_I = 1;
   private static final float init_F_2 = 3.0F;
   private static final int open_I = 6;
   private final Font init_Font;
   private final autismclient.gui.f.b.CharEncoder init_CharEncoder_2 = new autismclient.gui.f.b.CharEncoder();
   private final autismclient.gui.ui.layout.CollapsibleWindowNode init_CharEncoder_3 = new autismclient.gui.ui.layout.CollapsibleWindowNode("Select Packet");
   private final autismclient.gui.ui.layout.LayoutTreeHost init_TextFormat = new autismclient.gui.ui.layout.LayoutTreeHost(this.init_CharEncoder_2, this.init_CharEncoder_3);
   private final autismclient.gui.ui.widgets.TextInputWidget init_BitSetUtil = new autismclient.gui.ui.widgets.TextInputWidget();
   private final autismclient.gui.ui.layout.LabelNode init_PoolEntry = new autismclient.gui.ui.layout.LabelNode("", autismclient.gui.f.b.l.write).load_PoolEntry(true);
   private final autismclient.gui.ui.layout.VerticalSpacerNode init_BitSetUtil_2 = new autismclient.gui.ui.layout.VerticalSpacerNode();
   private autismclient.gui.ui.layout.ScrollableListView init_EventBus = null;
   private final List<Class<? extends Packet<?>>> init_List = new ArrayList<>();
   private final List<Class<? extends Packet<?>>> load_List = new ArrayList<>();
   private final List<Class<? extends Packet<?>>> read_List = new ArrayList<>();
   private final List<Class<? extends Packet<?>>> write_List = new ArrayList<>();
   private final Set<Class<? extends Packet<?>>> init_Set = new LinkedHashSet<>();
   private final Map<Class<? extends Packet<?>>, String> init_Map = new HashMap<>();
   private final SearchFilterList<Class<? extends Packet<?>>> init_a = new SearchFilterList<>(
      packetClassx -> this.init_Map.computeIfAbsent(packetClassx, AutismPacketSelectorOverlay::init_Str_Class_2)
   );
   private List<Class<? extends Packet<?>>> update_List = List.of();
   private List<Class<? extends Packet<?>>> sync_List = List.of();
   private Set<Class<? extends Packet<?>>> load_Set = Set.of();
   private Set<Class<? extends Packet<?>>> read_Set = Set.of();
   private boolean write_Z = false;
   private boolean update_Z = false;
   private boolean sync_Z = false;
   private boolean flush_Z = false;
   private float load_F;
   private float read_F;
   private float write_F;
   private float update_F;
   private int poll_I;
   private int push_I;
   private int pop;
   private int tick;
   private int run = 230;
   private int get = 0;
   private int set = 0;
   private boolean close_Z = false;
   private int put = 0;
   private Consumer<Class<? extends Packet<?>>> init_Consumer;
   private BiConsumer<Class<? extends Packet<?>>, Boolean> init_BiConsumer;
   private boolean open_Z = true;
   private boolean poll_Z = false;
   private boolean push_Z = true;
   private final autismclient.gui.ui.VisibilityTransition init_CharEncoder = new autismclient.gui.ui.VisibilityTransition(false);

   public AutismPacketSelectorOverlay(Font textRenderer) {
      this.init_Font = textRenderer;
      this.load_List.addAll(PacketTypeRegistry.getC2sPackets());
      this.load_List.sort(Comparator.comparing(PacketNameResolver::init_Str_Class, String.CASE_INSENSITIVE_ORDER));
      this.read_List.addAll(PacketTypeRegistry.getS2cPackets());
      this.read_List.sort(Comparator.comparing(PacketNameResolver::init_Str_Class, String.CASE_INSENSITIVE_ORDER));
      this.init_Set.addAll(this.load_List);
      this.write_List.addAll(this.load_List);
      this.write_List.addAll(this.read_List);
      this.init_List.addAll(this.write_List);
      this.init_List.sort(Comparator.comparing(PacketNameResolver::init_Str_Class, String.CASE_INSENSITIVE_ORDER));

      for (Class<? extends Packet<?>> packetClass : this.init_List) {
         this.init_Map.put(packetClass, init_Str_Class_2(packetClass));
      }

      this.update_List = List.copyOf(this.init_List);
      this.sync_List = List.copyOf(this.init_List);
      this.update_V_18();
   }

   private void update_V_18() {
      this.run = this.push_I_22();
      this.init_CharEncoder_3.setCenterTitle(false);
      this.init_CharEncoder_3.setTitleTone(autismclient.gui.f.b.l.load);
      this.init_CharEncoder_3.setHeaderButtons(true, true);
      this.init_CharEncoder_3.setTitleInsets(this.run_I_20() + 2, this.run_I_20() + this.put() + this.add_I_15() + this.clear() + 12);
      this.init_CharEncoder_3.body().init(this.get_I_18()).init(autismclient.gui.ui.layout.EdgeInsets.uniform(this.run_I_20()));
      this.init_BitSetUtil
         .setPlaceholder("Search packets...")
         .setFixedHeight(this.set_I_18())
         .withGrowX(true)
         .setOnChange(text -> this.init_V_Str_10(text));
      this.sync_V_16();
   }

   private void sync_V_16() {
      this.init_CharEncoder_3.body().clearChildren();
      this.init_CharEncoder_3.setTitle(this.poll_Z ? "Packet Selector [Toggle]" : "Packet Selector");
      this.init_CharEncoder_3.body().addChild(this.init_BitSetUtil);
      this.init_BitSetUtil_2.init(this.init_I_I_9(this.sync_List.size()));
      this.init_CharEncoder_3.body().addChild(this.init_BitSetUtil_2);
      this.init_PoolEntry.init(this.poll_Z ? this.sync_List.size() + " packets | Selected " + this.read_Set.size() : this.sync_List.size() + " packets");
      this.init_CharEncoder_3.body().addChild(this.init_PoolEntry);
      this.set = this.sync_List.size() * this.pop_I_22();
      this.push_Z = false;
   }

   private int init_I_I_9(int itemCount) {
      int visibleRows = Math.max(3, Math.min(this.tick_I_20(), Math.max(1, itemCount)));
      return visibleRows * this.pop_I_22() + 2;
   }

   public void init_V_Consumer(Consumer<Class<? extends Packet<?>>> onSelect) {
      this.init_V_Consumer(onSelect, this.init_List, Set.of(), true);
   }

   public void load(Consumer<Class<? extends Packet<?>>> onSelect) {
      this.init_V_Consumer(onSelect, Set.of(), true);
   }

   public void init_V_Consumer(Consumer<Class<? extends Packet<?>>> onSelect, Collection<Class<? extends Packet<?>>> excludedPackets, boolean closeOnSelect) {
      this.init_V_Consumer(onSelect, this.load_List, excludedPackets, closeOnSelect);
   }

   public void read(Consumer<Class<? extends Packet<?>>> onSelect) {
      this.load_V_Consumer(onSelect, Set.of(), true);
   }

   public void load_V_Consumer(Consumer<Class<? extends Packet<?>>> onSelect, Collection<Class<? extends Packet<?>>> excludedPackets, boolean closeOnSelect) {
      this.init_V_Consumer(onSelect, this.read_List, excludedPackets, closeOnSelect);
   }

   public void read(Consumer<Class<? extends Packet<?>>> onSelect, Collection<Class<? extends Packet<?>>> excludedPackets, boolean closeOnSelect) {
      this.init_V_Consumer(onSelect, this.write_List, excludedPackets, closeOnSelect);
   }

   public void init_V_BiConsumer_Collection(
      BiConsumer<Class<? extends Packet<?>>, Boolean> onToggleSelect, Collection<Class<? extends Packet<?>>> selectedPackets
   ) {
      this.init_V_BiConsumer(onToggleSelect, this.load_List, selectedPackets, Set.of());
   }

   public void init_V_BiConsumer(
      BiConsumer<Class<? extends Packet<?>>, Boolean> onToggleSelect,
      Collection<Class<? extends Packet<?>>> selectedPackets,
      Collection<Class<? extends Packet<?>>> excludedPackets
   ) {
      this.init_V_BiConsumer(onToggleSelect, this.load_List, selectedPackets, excludedPackets);
   }

   public void load_V_BiConsumer_Collection(
      BiConsumer<Class<? extends Packet<?>>, Boolean> onToggleSelect, Collection<Class<? extends Packet<?>>> selectedPackets
   ) {
      this.init_V_BiConsumer(onToggleSelect, this.read_List, selectedPackets, Set.of());
   }

   public void load_V_BiConsumer(
      BiConsumer<Class<? extends Packet<?>>, Boolean> onToggleSelect,
      Collection<Class<? extends Packet<?>>> selectedPackets,
      Collection<Class<? extends Packet<?>>> excludedPackets
   ) {
      this.init_V_BiConsumer(onToggleSelect, this.read_List, selectedPackets, excludedPackets);
   }

   public void read_V_BiConsumer_Collection(
      BiConsumer<Class<? extends Packet<?>>, Boolean> onToggleSelect, Collection<Class<? extends Packet<?>>> selectedPackets
   ) {
      this.init_V_BiConsumer(onToggleSelect, this.write_List, selectedPackets, Set.of());
   }

   private void init_V_Consumer(
      Consumer<Class<? extends Packet<?>>> onSelect,
      List<Class<? extends Packet<?>>> pool,
      Collection<Class<? extends Packet<?>>> excludedPackets,
      boolean closeOnSelect
   ) {
      this.write_Z = true;
      this.update_Z = false;
      this.init_Consumer = onSelect;
      this.init_BiConsumer = null;
      this.open_Z = closeOnSelect;
      this.poll_Z = false;
      this.load_Set = excludedPackets == null ? Set.of() : new LinkedHashSet<>(excludedPackets);
      this.read_Set = Set.of();
      this.update_List = this.init_List_List_3(pool);
      this.init_V_Str_10("");
      this.flush_V_15();
   }

   private void init_V_BiConsumer(
      BiConsumer<Class<? extends Packet<?>>, Boolean> onToggleSelect,
      List<Class<? extends Packet<?>>> pool,
      Collection<Class<? extends Packet<?>>> selectedPackets,
      Collection<Class<? extends Packet<?>>> excludedPackets
   ) {
      this.write_Z = true;
      this.update_Z = false;
      this.init_Consumer = null;
      this.init_BiConsumer = onToggleSelect;
      this.open_Z = false;
      this.poll_Z = true;
      this.load_Set = excludedPackets == null ? Set.of() : new LinkedHashSet<>(excludedPackets);
      this.read_Set = selectedPackets == null ? new LinkedHashSet<>() : new LinkedHashSet<>(selectedPackets);
      this.update_List = this.init_List_List_3(pool);
      this.init_V_Str_10("");
      this.flush_V_15();
   }

   private void flush_V_15() {
      this.sync_V_16();
      autismclient.gui.ui.layout.LayoutContext metrics = this.init_TextFormat.createMeasureContext();
      if (metrics != null) {
         this.get = Math.round(this.init_CharEncoder_3.measureHeight(metrics, this.run));
      }

      autismclient.gui.ui.layout.LayoutViewport viewport = this.init_TextFormat.createViewport();
      this.pop = Math.max(8, Math.round((viewport.width() - this.run) / 2.0F));
      this.tick = Math.max(8, Math.round((viewport.height() - this.get) / 2.0F));
      OverlayBounds clamped = this.init_cH_cH_4(new OverlayBounds(this.pop, this.tick, this.run, this.get, this.write_Z, this.update_Z));
      this.pop = clamped.x;
      this.tick = clamped.y;
      this.run = clamped.width;
      this.get = clamped.height;
      this.init_BitSetUtil.setValue("");
      this.init_BitSetUtil.setFocused(true);
      this.init_CharEncoder_3.setShowBody(true);
      OverlayManager.getInstance().bringOwnerToFront(this);
   }

   public void write_V_21() {
      this.write_Z = false;
      this.update_Z = false;
      this.sync_Z = false;
      this.flush_Z = false;
      this.init_Consumer = null;
      this.init_BiConsumer = null;
      this.load_Set = Set.of();
      this.read_Set = Set.of();
      this.open_Z = true;
      this.poll_Z = false;
      this.init_TextFormat.clearFocus();
      this.init_CharEncoder_3.setShowBody(true);
   }

   @Override
   public boolean sync_Z() {
      return this.write_Z;
   }

   private void init_V_Str_10(String query) {
      List<Class<? extends Packet<?>>> pool = this.init_List_List_3(this.update_List);
      this.init_a.init_V_List(pool);
      this.init_a.init_V_Str(query);
      this.sync_List = this.init_a.init_List();
      this.set = this.sync_List.size() * this.pop_I_22();
      this.sync_V_16();
   }

   @Override
   public void init_V_GuiGraphicsExtract_15(GuiGraphicsExtractor context, int mouseX, int mouseY, float delta) {
      this.init_CharEncoder.init_V(this.write_Z);
      if (this.init_CharEncoder.load_Z(this.write_Z) && init_Minecraft != null && init_Minecraft.font != null) {
         boolean closing = !this.write_Z;
         this.init_CharEncoder
            .init(
               context,
               autismclient.gui.f.NetHelper.init(this.pop, this.tick, this.run, Math.max(this.init_CharEncoder_2.load(), this.get)),
               true,
               () -> this.load(context, closing ? -10000 : mouseX, closing ? -10000 : mouseY, delta)
            );
      }
   }

   private void load(GuiGraphicsExtractor context, int mouseX, int mouseY, float delta) {
      if (this.push_Z) {
         this.sync_V_16();
      }

      context.nextStratum();
      autismclient.gui.ui.layout.LayoutViewport viewport = this.init_TextFormat.createViewport();
      float uiMouseX = viewport.toUiX(mouseX);
      float uiMouseY = viewport.toUiY(mouseY);
      boolean headerHovered = this.init_Z_F_F_5(uiMouseX, uiMouseY);
      autismclient.gui.ui.layout.LayoutContext metrics = new autismclient.gui.ui.layout.LayoutContext(
         context, init_Minecraft.font, viewport, this.init_CharEncoder_2, uiMouseX, uiMouseY, delta
      );
      this.init_CharEncoder_3.setExpanded(!this.update_Z);
      this.init_CharEncoder_3.setActive(true);
      this.init_CharEncoder_3.setHeaderHovered(headerHovered);
      this.get = Math.round(this.init_CharEncoder_3.measureHeight(metrics, this.run));
      this.get = Math.max(this.init_CharEncoder_2.load(), this.get);
      OverlayBounds clamped = this.init_cH_cH_4(new OverlayBounds(this.pop, this.tick, this.run, this.get, this.write_Z, this.update_Z));
      this.pop = clamped.x;
      this.tick = clamped.y;
      this.run = clamped.width;
      this.get = clamped.height;
      this.init_CharEncoder_3.setBounds(this.pop, this.tick, this.run, this.get);
      this.init_TextFormat.render(context, mouseX, mouseY, delta);
      context.nextStratum();
      if (this.init_CharEncoder_3.pushBodyClip(context)) {
         try {
            this.init_V_GuiGraphicsExtract_10(context, viewport, uiMouseX, uiMouseY, delta);
         } finally {
            autismclient.gui.ui.UiClipStack.getInstance().pop(context);
         }
      }
   }

   private void init_V_GuiGraphicsExtract_10(
      GuiGraphicsExtractor context, autismclient.gui.ui.layout.LayoutViewport viewport, float uiMouseX, float uiMouseY, float delta
   ) {
      int viewX = Math.round(this.init_BitSetUtil_2.x());
      int viewY = Math.round(this.init_BitSetUtil_2.y());
      int viewW = Math.round(this.init_BitSetUtil_2.width());
      int viewH = Math.round(this.init_BitSetUtil_2.height());
      if (viewW > 2 && viewH > 2) {
         int contentHeight = this.sync_List.size() * this.pop_I_22();
         if (this.init_EventBus == null
            || this.init_EventBus.getX() != viewX
            || this.init_EventBus.getY() != viewY
            || this.init_EventBus.getWidth() != viewW
            || this.init_EventBus.getHeight() != viewH) {
            int preservedScroll = this.init_EventBus == null ? 0 : this.init_EventBus.getScrollOffset();
            this.init_EventBus = new autismclient.gui.ui.layout.ScrollableListView(viewX, viewY, viewW, viewH, this.pop_I_22(), 6);
            this.init_EventBus.setContentHeight(contentHeight);
            this.init_EventBus.setScrollOffset(preservedScroll);
         }

         this.init_EventBus.setContentHeight(contentHeight);
         this.init_EventBus.beginFrame(context, this.init_CharEncoder_2.set(), this.init_CharEncoder_2.reset());

         try {
            this.init_EventBus.forEachVisibleRow(context, this.sync_List.size(), (idx, bnd) -> {
               Class<? extends Packet<?>> packetClass = this.sync_List.get(idx);
               this.init_V_GuiGraphicsExtract_9(context, packetClass, bnd.x, bnd.y, bnd.width, idx);
            });
         } finally {
            this.init_EventBus.endFrame(context);
         }

         this.init(context, viewX, viewY, viewW, viewH, uiMouseX, uiMouseY);
      }
   }

   private void init_V_GuiGraphicsExtract_9(GuiGraphicsExtractor context, Class<? extends Packet<?>> packetClass, int x, int y, int width, int index) {
      boolean c2s = this.init_Set.contains(packetClass);
      boolean selected = this.poll_Z && this.read_Set.contains(packetClass);
      int rowColor = selected ? UiColorPalette.filterRowFill(false) : UiColorPalette.packetRowFill(c2s, index, false);
      int color = selected ? UiColorPalette.filterActiveText() : UiColorPalette.packetRowAccent(c2s, index);
      autismclient.gui.ui.widgets.PanelSurfaceHelper.fillRect(context, x, y, width, this.pop_I_22(), rowColor);
      String name = PacketNameResolver.init_Str_Class(packetClass);
      int textWidth = width - 10;
      String trimmed = autismclient.gui.f.b.k.init(
         this.init_Font, name, textWidth, this.init_CharEncoder_2.init_Identifier_l(autismclient.gui.f.b.l.read), color
      );
      int textY = UiLayoutMath.centerTextY(y, this.pop_I_22(), this.init_CharEncoder_2.init_I_l(autismclient.gui.f.b.l.read), this.init_CharEncoder_2.flush());
      autismclient.gui.f.b.k.init(
         context, this.init_Font, trimmed, this.init_CharEncoder_2.init_Identifier_l(autismclient.gui.f.b.l.read), color, x + 6, textY, false
      );
      autismclient.gui.ui.widgets.PanelSurfaceHelper.drawDividerColored(context, x + 4, y + this.pop_I_22() - 1, width - 8, UiColorPalette.subtleDivider());
   }

   private void init(GuiGraphicsExtractor context, int viewX, int viewY, int viewW, int viewH, float uiMouseX, float uiMouseY) {
      if (this.init_EventBus != null) {
         this.init_EventBus.renderScrollbar(context, uiMouseX, uiMouseY);
      }
   }

   @Override
   public boolean init_Z_D_3(double mouseX, double mouseY, int button) {
      if (!this.write_Z) {
         return false;
      }

      autismclient.gui.ui.layout.LayoutViewport viewport = this.init_TextFormat.createViewport();
      float uiMouseX = viewport.toUiX(mouseX);
      float uiMouseY = viewport.toUiY(mouseY);
      if (button == 0 && this.load_Z_F_F_4(uiMouseX, uiMouseY)) {
         this.write_V_21();
         return true;
      }

      if (button == 0 && this.init_Z_F_F_5(uiMouseX, uiMouseY)) {
         this.sync_Z = true;
         this.flush_Z = false;
         this.load_F = uiMouseX - this.pop;
         this.read_F = uiMouseY - this.tick;
         this.write_F = uiMouseX;
         this.update_F = uiMouseY;
         this.poll_I = this.pop;
         this.push_I = this.tick;
         return true;
      }

      if (!this.update_Z && this.init_TextFormat.mouseClicked(mouseX, mouseY, button)) {
         return true;
      }

      if (!this.update_Z
         && button == 0
         && this.init_Z_F_2(
            this.init_BitSetUtil_2.x(),
            this.init_BitSetUtil_2.y(),
            this.init_BitSetUtil_2.width(),
            this.init_BitSetUtil_2.height(),
            uiMouseX,
            uiMouseY
         )
         && this.init_EventBus != null
         && this.init_EventBus.mouseClicked(uiMouseX, uiMouseY, button)) {
         return true;
      }

      if (!this.update_Z
         && button == 0
         && this.init_Z_F_2(
            this.init_BitSetUtil_2.x(),
            this.init_BitSetUtil_2.y(),
            this.init_BitSetUtil_2.width(),
            this.init_BitSetUtil_2.height(),
            uiMouseX,
            uiMouseY
         )) {
         int index = (int)((uiMouseY - this.init_BitSetUtil_2.y() + this.init_EventBus.getScrollOffset()) / this.pop_I_22());
         if (index >= 0 && index < this.sync_List.size()) {
            Class<? extends Packet<?>> selectedPacket = this.sync_List.get(index);
            if (this.poll_Z) {
               this.read_Set = new LinkedHashSet<>(this.read_Set);
               boolean nowSelected;
               if (this.read_Set.contains(selectedPacket)) {
                  this.read_Set.remove(selectedPacket);
                  nowSelected = false;
               } else {
                  this.read_Set.add(selectedPacket);
                  nowSelected = true;
               }

               if (this.init_BiConsumer != null) {
                  this.init_BiConsumer.accept(selectedPacket, nowSelected);
               }

               this.sync_V_16();
               this.init_BitSetUtil.setFocused(true);
            } else {
               if (this.init_Consumer != null) {
                  this.init_Consumer.accept(selectedPacket);
               }

               if (this.open_Z) {
                  this.write_V_21();
               } else {
                  this.load_Set = new LinkedHashSet<>(this.load_Set);
                  this.load_Set.add(selectedPacket);
                  this.update_List = this.init_List_List_3(this.update_List);
                  this.init_V_Str_10(this.init_BitSetUtil.getValue());
                  this.init_BitSetUtil.setFocused(true);
               }
            }

            return true;
         }
      }

      if (!this.init_Z_F_2(this.pop, this.tick, this.run, this.get, uiMouseX, uiMouseY)) {
         this.init_TextFormat.clearFocus();
         return true;
      } else {
         return true;
      }
   }

   @Override
   public boolean load_Z_D_3(double mouseX, double mouseY, int button) {
      autismclient.gui.ui.layout.LayoutViewport viewport = this.init_TextFormat.createViewport();
      float uiMouseX = viewport.toUiX(mouseX);
      float uiMouseY = viewport.toUiY(mouseY);
      if (button == 0 && this.sync_Z) {
         boolean moved = this.flush_Z
            || Math.abs(uiMouseX - this.write_F) >= 3.0F
            || Math.abs(uiMouseY - this.update_F) >= 3.0F
            || this.pop != this.poll_I
            || this.tick != this.push_I;
         this.sync_Z = false;
         this.flush_Z = false;
         if (!moved && this.init_Z_F_F_5(uiMouseX, uiMouseY) && !this.load_Z_F_F_4(uiMouseX, uiMouseY)) {
            this.write_V_Z(!this.update_Z);
         }

         return true;
      } else {
         if (button == 0 && this.init_EventBus != null) {
            this.init_EventBus.mouseReleased();
         }

         return !this.update_Z && this.init_TextFormat.mouseReleased(mouseX, mouseY, button)
            ? true
            : this.write_Z && this.init_Z_F_2(this.pop, this.tick, this.run, this.get, uiMouseX, uiMouseY);
      }
   }

   @Override
   public boolean init_Z_D(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
      if (!this.write_Z) {
         return false;
      }

      autismclient.gui.ui.layout.LayoutViewport viewport = this.init_TextFormat.createViewport();
      float uiMouseX = viewport.toUiX(mouseX);
      float uiMouseY = viewport.toUiY(mouseY);
      if (this.sync_Z && button == 0) {
         if (!this.flush_Z && Math.abs(uiMouseX - this.write_F) < 3.0F && Math.abs(uiMouseY - this.update_F) < 3.0F) {
            return true;
         }

         this.flush_Z = true;
         int nextX = Math.round(uiMouseX - this.load_F);
         int nextY = Math.round(uiMouseY - this.read_F);
         OverlayBounds clamped = this.init_cH_cH_4(new OverlayBounds(nextX, nextY, this.run, this.get, this.write_Z, this.update_Z));
         this.pop = clamped.x;
         this.tick = clamped.y;
         return true;
      } else if (!this.update_Z
         && this.init_Z_F_2(
            this.init_BitSetUtil_2.x(),
            this.init_BitSetUtil_2.y(),
            this.init_BitSetUtil_2.width(),
            this.init_BitSetUtil_2.height(),
            uiMouseX,
            uiMouseY
         )
         && this.init_EventBus != null
         && this.init_EventBus.isDraggingScrollbar()) {
         this.init_EventBus.mouseDragged(uiMouseX, uiMouseY);
         return true;
      } else {
         return !this.update_Z && this.init_TextFormat.mouseDragged(mouseX, mouseY, button, deltaX, deltaY)
            ? true
            : this.write_Z && this.init_Z_F_2(this.pop, this.tick, this.run, this.get, uiMouseX, uiMouseY);
      }
   }

   @Override
   public boolean init_Z_I(int keyCode, int scanCode, int modifiers) {
      if (!this.write_Z) {
         return false;
      }

      if (keyCode == 256) {
         if (this.init_BitSetUtil.isFocused()) {
            this.init_BitSetUtil.setFocused(false);
            return true;
         } else {
            this.write_V_21();
            return true;
         }
      } else {
         if (this.update_Z) {
            return false;
         }

         this.init_TextFormat.keyPressed(keyCode, scanCode, modifiers);
         return this.init_BitSetUtil.isFocused();
      }
   }

   @Override
   public boolean init_Z_C_I(char chr, int modifiers) {
      return this.write_Z && !this.update_Z && this.init_TextFormat.charTyped(chr, modifiers);
   }

   @Override
   public boolean init_Z_D_2(double mouseX, double mouseY, double amount) {
      if (!this.write_Z) {
         return false;
      } else {
         autismclient.gui.ui.layout.LayoutViewport viewport = this.init_TextFormat.createViewport();
         float uiMouseX = viewport.toUiX(mouseX);
         float uiMouseY = viewport.toUiY(mouseY);
         if (!this.update_Z
            && this.init_Z_F_2(
               this.init_BitSetUtil_2.x(),
               this.init_BitSetUtil_2.y(),
               this.init_BitSetUtil_2.width(),
               this.init_BitSetUtil_2.height(),
               uiMouseX,
               uiMouseY
            )
            && this.init_EventBus != null) {
            this.init_EventBus.mouseScrolled(uiMouseX, uiMouseY, amount);
            return true;
         } else {
            return this.init_BitSetUtil.isFocused() ? false : this.init_Z_F_2(this.pop, this.tick, this.run, this.get, uiMouseX, uiMouseY);
         }
      }
   }

   private List<Class<? extends Packet<?>>> init_List_List_3(List<Class<? extends Packet<?>>> packets) {
      if (packets != null && !packets.isEmpty()) {
         if (this.load_Set.isEmpty()) {
            return new ArrayList<>(packets);
         }

         List<Class<? extends Packet<?>>> filtered = new ArrayList<>(packets.size());

         for (Class<? extends Packet<?>> packetClass : packets) {
            if (!this.load_Set.contains(packetClass)) {
               filtered.add(packetClass);
            }
         }

         return filtered;
      } else {
         return List.of();
      }
   }

   private static String init_Str_Class_2(Class<? extends Packet<?>> packetClass) {
      return packetClass == null ? "" : (packetClass.getSimpleName() + "\n" + PacketNameResolver.init_Str_Class(packetClass)).toLowerCase(Locale.ROOT);
   }

   @Override
   public boolean write_Z() {
      return this.write_Z && this.init_TextFormat.hasFocusedInput();
   }

   @Override
   public OverlayBounds init_cH() {
      return new OverlayBounds(this.pop, this.tick, this.run, this.get, this.write_Z, this.update_Z);
   }

   @Override
   public void init_V_cH(OverlayBounds bounds) {
      if (bounds != null) {
         OverlayBounds clamped = this.init_cH_cH_4(bounds);
         this.pop = clamped.x;
         this.tick = clamped.y;
         this.run = clamped.width;
         this.get = clamped.height;
         this.write_Z = clamped.visible;
         this.update_Z = clamped.collapsed;
         this.init_CharEncoder_3.setShowBody(!this.update_Z);
      }
   }

   @Override
   public boolean flush_Z() {
      return this.update_Z;
   }

   @Override
   public void write_V_Z(boolean collapsed) {
      if (this.update_Z != collapsed) {
         this.update_Z = collapsed;
         this.sync_Z = false;
         this.flush_Z = false;
         this.init_CharEncoder_3.setShowBody(!collapsed);
         if (collapsed) {
            this.get_V();
         }

         this.a__V();
      }
   }

   @Override
   public void push_V() {
      this.init_TextFormat.clearFocus();
   }

   @Override
   public boolean init_Z_D_D(double mouseX, double mouseY) {
      if (!this.write_Z) {
         return false;
      }

      autismclient.gui.ui.layout.LayoutViewport viewport = this.init_TextFormat.createViewport();
      float uiMouseX = viewport.toUiX(mouseX);
      float uiMouseY = viewport.toUiY(mouseY);
      int renderedHeight = this.update_Z ? this.init_CharEncoder_2.load() : this.get;
      return this.init_Z_F_2(this.pop, this.tick, this.run, renderedHeight, uiMouseX, uiMouseY);
   }

   @Override
   public boolean load_Z_D_D(double mouseX, double mouseY) {
      if (!this.write_Z) {
         return false;
      }

      autismclient.gui.ui.layout.LayoutViewport viewport = this.init_TextFormat.createViewport();
      float uiMouseX = viewport.toUiX(mouseX);
      float uiMouseY = viewport.toUiY(mouseY);
      return this.init_Z_F_F_5(uiMouseX, uiMouseY) && !this.load_Z_F_F_4(uiMouseX, uiMouseY);
   }

   @Override
   public int init_I() {
      return this.push_I_22();
   }

   @Override
   public int load_I() {
      return this.init_CharEncoder_2.load() + 20;
   }

   private boolean init_Z_F_F_5(float uiMouseX, float uiMouseY) {
      return uiMouseX >= this.pop && uiMouseX < this.pop + this.run && uiMouseY >= this.tick && uiMouseY < this.tick + this.init_CharEncoder_2.load();
   }

   private boolean load_Z_F_F_4(float uiMouseX, float uiMouseY) {
      return autismclient.gui.ui.widgets.WindowHeaderHelper.init(
         autismclient.gui.f.NetHelper.init(this.pop, this.tick, this.run, Math.max(this.init_CharEncoder_2.load(), this.get)),
         this.init_CharEncoder_2.load(),
         uiMouseX,
         uiMouseY
      );
   }

   private OverlayBounds init_cH_cH_4(OverlayBounds bounds) {
      autismclient.gui.ui.layout.LayoutViewport viewport = this.init_TextFormat.createViewport();
      int margin = 4;
      int viewportW = Math.round(viewport.width());
      int viewportH = Math.round(viewport.height());
      int availableW = Math.max(1, viewportW - margin * 2);
      int availableH = Math.max(this.init_CharEncoder_2.load(), viewportH - margin * 2);
      int width = Math.max(Math.min(this.push_I_22(), availableW), Math.min(bounds.width, availableW));
      int minHeight = bounds.collapsed ? this.init_CharEncoder_2.load() : this.init_CharEncoder_2.load() + 20;
      int height = Math.max(Math.min(minHeight, availableH), Math.min(bounds.height, availableH));
      int renderedHeight = bounds.collapsed ? this.init_CharEncoder_2.load() : height;
      int x = Math.max(margin, Math.min(bounds.x, Math.max(margin, viewportW - margin - width)));
      int y = Math.max(margin, Math.min(bounds.y, Math.max(margin, viewportH - margin - renderedHeight)));
      return new OverlayBounds(x, y, width, height, bounds.visible, bounds.collapsed);
   }

   private boolean init_Z_F_2(float x, float y, float width, float height, float mouseX, float mouseY) {
      return mouseX >= x && mouseX <= x + width && mouseY >= y && mouseY <= y + height;
   }

   private int push_I_22() {
      return 230;
   }

   private int pop_I_22() {
      return autismclient.gui.ui.UiDensityMetrics.scaledHeight(12);
   }

   private int tick_I_20() {
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

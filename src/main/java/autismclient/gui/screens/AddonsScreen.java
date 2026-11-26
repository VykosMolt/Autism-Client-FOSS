package autismclient.gui.screens;

import autismclient.compat263.Compat263;
import autismclient.gui.f.b.k;
import autismclient.gui.f.b.l;
import autismclient.util.bi;
import autismclient.util.cA;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Util;

public final class AddonsScreen extends VirtualScaledScreen {
   private static final autismclient.gui.f.b.CharEncoder init_CharEncoder = new autismclient.gui.f.b.CharEncoder();
   private static final int init_I = -7429889;
   private static final int load_I = 560;
   private static final int read_I = 3;
   private static final String[] init_StrArr = new String[]{"Status", "API", "Version", "Authors", "Extensions", "Rejected", "Runtime Errors"};
   private final Screen init_Screen;
   private final List<autismclient.gui.ui.widgets.OverlayActionButton> init_List = new ArrayList<>();
   private final autismclient.gui.ui.widgets.ScrollOffsetController init_CacheEntry = new autismclient.gui.ui.widgets.ScrollOffsetController();
   private EditBox init_EditBox;
   private String init_Str = "";
   private String load_Str = "";
   private boolean init_Z;
   private String read_Str = "";
   private int write_I;
   private boolean load_Z;
   private int update = -2147483648;
   private int sync;
   private List<autismclient.b.DataUtil.StreamUtil> load_List = List.of();
   private String write_Str = "";
   private List<autismclient.b.DataUtil.StreamUtil> read_List = List.of();

   public AddonsScreen(Screen parent) {
      super(Component.literal("Addons"));
      this.init_Screen = parent;
   }

   @Override
   protected void init() {
      this.load_List = List.copyOf(autismclient.b.DataUtil.load_List());
      this.write_Str = "\u0000";
      this.read_List = List.of();
      this.init_EditBox = new MsdfEditBox(this.font, this.set() + 4, this.reset(), this.get() - 8, this.sync_I_2(), Component.literal("Search addons"));
      this.init_EditBox.setHint(Component.literal("Search addons..."));
      this.init_EditBox.setMaxLength(96);
      this.init_EditBox.setResponder(value -> {
         this.load_Str = init_Str_Str(value);
         this.init_Z = true;
      });
      this.addRenderableWidget(this.init_EditBox);
      this.read_V_3();
      this.load_V_5();
   }

   private void load_V_5() {
      this.init_List.clear();
      this.update = this.next();
      int ctl = this.sync_I_2();
      this.init_List
         .add(
            autismclient.gui.ui.widgets.OverlayActionButton.init(
                  this.write_I_3(), this.pop(), this.read_I_3(), ctl, Component.literal("Back"), b -> this.minecraft.gui.setScreen(this.init_Screen)
               )
               .init_ByteReader_ByteReader_StreamU(autismclient.gui.ui.widgets.OverlayActionButton.ButtonTone.SECONDARY)
         );
      autismclient.b.DataUtil.StreamUtil selected = this.init_DataUtil_StreamUti();
      int gap = 2;
      int btnLeft = this.put() + 4;
      int areaW = this.clear();
      int row2Y = this.next() - 4 - ctl;
      int row1Y = row2Y - ctl - gap;
      boolean disabledRestart = selected != null && autismclient.b.DataUtil.read(selected.init_Str());
      String toggleLabel = init_Str_DataUtil_StreamUti(selected);
      int copyIdW = this.init_I_Str_4("Copy ID");
      int copyReportW = this.init_I_Str_4("Copy Report");
      int toggleW = this.init_I_Str_4(toggleLabel);
      int modsFolderW = this.init_I_Str_4("Mods Folder");
      autismclient.gui.ui.widgets.OverlayActionButton toggle;
      if (this.init_Z_DataUtil_StreamUti(selected)) {
         int x = btnLeft;
         int y = row2Y;
         this.init_List
            .add(
               autismclient.gui.ui.widgets.OverlayActionButton.init(x, y, copyIdW, ctl, Component.literal("Copy ID"), b -> this.write_V_3())
                  .init_ByteReader_ByteReader_StreamU(autismclient.gui.ui.widgets.OverlayActionButton.ButtonTone.SECONDARY)
            );
         x += copyIdW + gap;
         this.init_List
            .add(
               autismclient.gui.ui.widgets.OverlayActionButton.init(x, y, copyReportW, ctl, Component.literal("Copy Report"), b -> this.update_V_3())
                  .init_ByteReader_ByteReader_StreamU(autismclient.gui.ui.widgets.OverlayActionButton.ButtonTone.SECONDARY)
            );
         x += copyReportW + gap;
         toggle = autismclient.gui.ui.widgets.OverlayActionButton.init(x, y, toggleW, ctl, Component.literal(toggleLabel), b -> this.sync_V_2());
         x += toggleW + gap;
         this.init_List
            .add(
               autismclient.gui.ui.widgets.OverlayActionButton.init(x, y, modsFolderW, ctl, Component.literal("Mods Folder"), b -> this.flush_V_2())
                  .init_ByteReader_ByteReader_StreamU(autismclient.gui.ui.widgets.OverlayActionButton.ButtonTone.PRIMARY)
            );
      } else {
         int halfW = (areaW - gap) / 2;
         int rightW = areaW - halfW - gap;
         int rightX = btnLeft + halfW + gap;
         boolean row1Fits = copyIdW + copyReportW + gap <= areaW;
         boolean row2Fits = toggleW + modsFolderW + gap <= areaW;
         this.init_List
            .add(
               autismclient.gui.ui.widgets.OverlayActionButton.init(btnLeft, row1Y, row1Fits ? copyIdW : halfW, ctl, Component.literal("Copy ID"), b -> this.write_V_3())
                  .init_ByteReader_ByteReader_StreamU(autismclient.gui.ui.widgets.OverlayActionButton.ButtonTone.SECONDARY)
            );
         this.init_List
            .add(
               autismclient.gui.ui.widgets.OverlayActionButton.init(
                     row1Fits ? btnLeft + copyIdW + gap : rightX,
                     row1Y,
                     row1Fits ? copyReportW : rightW,
                     ctl,
                     Component.literal("Copy Report"),
                     b -> this.update_V_3()
                  )
                  .init_ByteReader_ByteReader_StreamU(autismclient.gui.ui.widgets.OverlayActionButton.ButtonTone.SECONDARY)
            );
         toggle = autismclient.gui.ui.widgets.OverlayActionButton.init(btnLeft, row2Y, row2Fits ? toggleW : halfW, ctl, Component.literal(toggleLabel), b -> this.sync_V_2());
         this.init_List
            .add(
               autismclient.gui.ui.widgets.OverlayActionButton.init(
                     row2Fits ? btnLeft + toggleW + gap : rightX,
                     row2Y,
                     row2Fits ? modsFolderW : rightW,
                     ctl,
                     Component.literal("Mods Folder"),
                     b -> this.flush_V_2()
                  )
                  .init_ByteReader_ByteReader_StreamU(autismclient.gui.ui.widgets.OverlayActionButton.ButtonTone.PRIMARY)
            );
      }

      toggle.init_ByteReader_ByteReader_StreamU(
         disabledRestart ? autismclient.gui.ui.widgets.OverlayActionButton.ButtonTone.SUCCESS : autismclient.gui.ui.widgets.OverlayActionButton.ButtonTone.DANGER
      );
      toggle.init_Z = selected != null;
      this.init_List.add(toggle);
   }

   private static String init_Str_DataUtil_StreamUti(autismclient.b.DataUtil.StreamUtil selected) {
      return selected != null && autismclient.b.DataUtil.read(selected.init_Str()) ? "Enable Restart" : "Disable Restart";
   }

   private boolean init_Z_DataUtil_StreamUti(autismclient.b.DataUtil.StreamUtil selected) {
      return this.init_I_Str_4("Copy ID")
            + this.init_I_Str_4("Copy Report")
            + this.init_I_Str_4(init_Str_DataUtil_StreamUti(selected))
            + this.init_I_Str_4("Mods Folder")
            + 6
         <= this.clear();
   }

   private int init_I_DataUtil_StreamUti(autismclient.b.DataUtil.StreamUtil selected) {
      int ctl = this.sync_I_2();
      return this.init_Z_DataUtil_StreamUti(selected) ? ctl : ctl * 2 + 2;
   }

   private int init_I_Str_4(String label) {
      return autismclient.gui.ui.UiDensityMetrics.buttonWidthForText(k.init(this.font, label, init_CharEncoder.init_Identifier_l(l.read), 0));
   }

   private int read_I_3() {
      return this.init_I_Str_4("Back");
   }

   private int write_I_3() {
      return this.poll() + 4;
   }

   private void read_V_3() {
      List<autismclient.b.DataUtil.StreamUtil> reports = this.init_List_3();
      if (reports.isEmpty()) {
         this.read_Str = "";
      } else {
         for (autismclient.b.DataUtil.StreamUtil report : reports) {
            if (report.init_Str().equals(this.read_Str)) {
               return;
            }
         }

         this.read_Str = reports.get(0).init_Str();
      }
   }

   @Override
   public void tick() {
      super.tick();
      if (this.init_Z) {
         this.init_Z = false;
         this.init_Str = this.load_Str;
         this.write_I = 0;
         this.init_CacheEntry.load(0, 0);
         this.read_V_3();
         this.load_V_5();
      }

      this.init_CacheEntry.init_I_F_I(1.0F, 1);
   }

   @Override
   public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float delta) {
      int mx = cA.init_I_D(mouseX);
      int my = cA.init_I_D(mouseY);
      cA.init_V_GuiGraphicsExtract(graphics);

      try {
         if (this.update != this.next()) {
            this.load_V_5();
         }

         autismclient.gui.f.PacketUtil.init(graphics, autismclient.gui.f.NetHelper.init(0, 0, this.init_I(), this.load_I()), -15987181);
         this.init_V_GuiGraphicsExtract_2(graphics, this.poll(), this.push(), this.open(), this.tick_I_4(), -401402080);
         this.init_V_GuiGraphicsExtract_2(graphics, this.set(), this.run(), this.get(), this.prev(), -1206840035);
         this.init_V_GuiGraphicsExtract_2(graphics, this.put(), this.run(), this.add(), this.prev(), -1206840035);
         int ctl = this.sync_I_2();
         int titleX = this.write_I_3() + this.read_I_3() + 4;
         int titleY = this.init_I_I_I_3(this.pop(), ctl);
         this.init(graphics, "Addons", titleX, titleY, GuiColorPalette.flush, false);
         int summaryLeft = titleX + this.load_I_Str_2("Addons") + 8;
         this.init(
            graphics, this.init_Str_2(), this.poll() + this.open() - 4, titleY, GuiColorPalette.close, Math.max(1, this.poll() + this.open() - 4 - summaryLeft)
         );
         this.init(graphics, "Addon Library", this.set() + 4, this.init_I_I_I_3(this.run() + 4, 12), GuiColorPalette.flush, false, this.get() - 8);
         this.init_V_GuiGraphicsExtract_3(graphics, mx, my);
         this.init_V_GuiGraphicsExtract(graphics);

         for (autismclient.gui.ui.widgets.OverlayActionButton button : this.init_List) {
            autismclient.gui.ui.widgets.OverlayActionButton.init(graphics, this.font, button, mx, my);
         }

         autismclient.gui.ui.widgets.ScrollbarMetricsHelper.Metrics scrollbar = this.init_PoolEntry_DataUtil_I_2(this.init_List_3().size());
         autismclient.gui.ui.widgets.ScrollbarMetricsHelper.render(graphics, scrollbar, scrollbar.containsTrack(mx, my), this.load_Z);
         super.extractRenderState(graphics, mx, my, delta);
      } finally {
         cA.load_V_GuiGraphicsExtract(graphics);
      }
   }

   private void init_V_GuiGraphicsExtract_3(GuiGraphicsExtractor graphics, int mouseX, int mouseY) {
      List<autismclient.b.DataUtil.StreamUtil> reports = this.init_List_3();
      int pitch = this.close_I_2();
      int maxScroll = this.init_I_I_3(reports.size());
      this.write_I = init_I_I(this.write_I, pitch, maxScroll);
      this.init_CacheEntry.load(this.write_I, maxScroll);
      int first = this.write_I / pitch;
      int top = this.start();
      int bottom = this.stop();
      int h = this.flush_I_2();
      int y = top - this.write_I % pitch;

      for (int i = first; i < reports.size() && y + h <= bottom; i++) {
         if (y + h > top) {
            this.init(graphics, reports.get(i), y, mouseX, mouseY);
         }

         y += pitch;
      }

      if (reports.isEmpty()) {
         this.init(
            graphics,
            this.load_List.isEmpty() ? "No AUTISM addons discovered." : "No addons match the search.",
            this.valid(),
            this.init_I_I_I_3(this.start(), this.flush_I_2()),
            GuiColorPalette.close,
            false,
            this.value()
         );
      }
   }

   private void init(GuiGraphicsExtractor graphics, autismclient.b.DataUtil.StreamUtil report, int y, int mouseX, int mouseY) {
      int h = this.flush_I_2();
      boolean selected = report.init_Str().equals(this.read_Str);
      boolean hovered = mouseX >= this.valid() && mouseX < this.data() && mouseY >= y && mouseY < y + h;
      int fill = selected ? 858052714 : (hovered ? 606804509 : 403771667);
      int status = selected ? GuiColorPalette.sync : this.load_I_DataUtil_StreamUti(report);
      autismclient.gui.f.PacketUtil.init(graphics, autismclient.gui.f.NetHelper.init(this.valid(), y, this.value(), h), fill);
      autismclient.gui.f.PacketUtil.init(graphics, autismclient.gui.f.NetHelper.init(this.valid(), y, 2, h), status);
      autismclient.gui.f.PacketUtil.init(graphics, autismclient.gui.f.NetHelper.init(this.valid() + 4, y + 3, 3, h - 6), report.init_I());
      int ty = this.init_I_I_I_3(y, h);
      String statusText = this.load_Str_DataUtil_StreamUti(report);
      int statusW = Math.min(this.load_I_Str_2(statusText), this.value() / 3);
      int statusX = this.data() - 4 - statusW;
      this.init(graphics, statusText, statusX, ty, this.load_I_DataUtil_StreamUti(report), false, statusW);
      int nameX = this.valid() + 10;
      int nameMax = Math.max(1, statusX - 4 - nameX);
      int nameW = Math.min(this.load_I_Str_2(report.load_Str()), nameMax);
      this.init(graphics, report.load_Str(), nameX, ty, GuiColorPalette.flush, false, nameMax);
      int idX = nameX + nameW + 4;
      int idMax = statusX - 4 - idX;
      if (idMax > 12) {
         this.init(graphics, report.init_Str() + (report.read_Str().isBlank() ? "" : "  v" + report.read_Str()), idX, ty, GuiColorPalette.close, false, idMax);
      }
   }

   private void init_V_GuiGraphicsExtract(GuiGraphicsExtractor graphics) {
      this.init_I_GuiGraphicsExtract_DataUtil_StreamUti(graphics, this.init_DataUtil_StreamUti());
   }

   private int init_I_GuiGraphicsExtract_DataUtil_StreamUti(GuiGraphicsExtractor graphics, autismclient.b.DataUtil.StreamUtil report) {
      int line = 12;
      int x = this.put() + 4;
      int y = this.run() + 4;
      int width = this.clear();
      this.init(graphics, "Details", x, this.init_I_I_I_3(y, 12), GuiColorPalette.flush, false, width);
      y += 14;
      if (report == null) {
         this.init(graphics, "Select an addon to inspect its status.", x, this.init_I_I_I_3(y, line), GuiColorPalette.close, false, width);
         return y + line;
      }

      int labelW = this.update_I_3();
      this.init(graphics, report.load_Str(), x, this.init_I_I_I_3(y, line), GuiColorPalette.flush, false, width);
      y += line;
      this.init(graphics, report.init_Str(), x, this.init_I_I_I_3(y, line), GuiColorPalette.close, false, width);
      y += line + 2;
      y = this.init(graphics, x, y, width, labelW, "Status", this.load_Str_DataUtil_StreamUti(report), this.load_I_DataUtil_StreamUti(report));
      y = this.init(graphics, x, y, width, labelW, "API", this.read(report), -7429889);
      y = this.init(graphics, x, y, width, labelW, "Version", report.read_Str().isBlank() ? "unknown" : report.read_Str(), GuiColorPalette.flush);
      y = this.init(graphics, x, y, width, labelW, "Authors", report.write_Str().isBlank() ? "unknown" : report.write_Str(), GuiColorPalette.flush);
      y = this.init(graphics, x, y, width, labelW, "Extensions", report.close(), GuiColorPalette.open);
      if (report.sync_I() > 0) {
         y = this.init(graphics, x, y, width, labelW, "Rejected", Integer.toString(report.sync_I()), -14249);
      }

      if (report.write_I() > 0) {
         y = this.init(graphics, x, y, width, labelW, "Runtime Errors", Integer.toString(report.write_I()), GuiColorPalette.poll);
      }

      if (autismclient.b.DataUtil.read(report.init_Str())) {
         y += 2;
         this.init(graphics, "Will be disabled after restart.", x, this.init_I_I_I_3(y, line), -14249, false, width);
         y += line;
      }

      if (!report.sync_Str().isBlank()) {
         y += 2;
         this.init(graphics, report.sync_Str(), x, this.init_I_I_I_3(y, line), GuiColorPalette.poll, false, width);
         y += line;
      }

      if (!report.init_List().isEmpty()) {
         y += 2;
         this.init(graphics, "Latest rejection:", x, this.init_I_I_I_3(y, line), GuiColorPalette.close, false, width);
         y += line;
         this.init(graphics, report.init_List().get(report.init_List().size() - 1), x, this.init_I_I_I_3(y, line), -14249, false, width);
         y += line;
      } else if (!report.flush().isBlank()) {
         y += 2;
         this.init(graphics, report.flush(), x, this.init_I_I_I_3(y, line), -14249, false, width);
         y += line;
      }

      return y;
   }

   private int init(GuiGraphicsExtractor graphics, int x, int y, int width, int labelW, String label, String value, int valueColor) {
      int ty = this.init_I_I_I_3(y, 12);
      this.init(graphics, label + ":", x, ty, GuiColorPalette.close, false, labelW);
      this.init(graphics, value, x + width, ty, valueColor, Math.max(1, width - labelW - 4));
      return y + 12;
   }

   private int update_I_3() {
      int w = 0;

      for (String label : init_StrArr) {
         w = Math.max(w, this.load_I_Str_2(label + ":"));
      }

      return w;
   }

   @Override
   public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
      MouseButtonEvent virtual = init_MouseButtonEvent_MouseButtonEvent(event);
      if (Compat263.mouseEventGlfwButton(virtual) != 0) {
         return super.mouseClicked(virtual, doubleClick);
      }

      for (autismclient.gui.ui.widgets.OverlayActionButton button : this.init_List) {
         if (autismclient.gui.ui.widgets.OverlayActionButton.init(button, virtual.x(), virtual.y(), Compat263.mouseEventGlfwButton(virtual))) {
            return true;
         }
      }

      autismclient.gui.ui.widgets.ScrollbarMetricsHelper.Metrics scrollbar = this.init_PoolEntry_DataUtil_I_2(this.init_List_3().size());
      if (scrollbar.isScrollable() && scrollbar.containsTrack(virtual.x(), virtual.y())) {
         this.load_Z = true;
         this.sync = scrollbar.containsThumb(virtual.x(), virtual.y()) ? Math.max(0, (int)Math.round(virtual.y()) - scrollbar.thumbY()) : scrollbar.thumbHeight() / 2;
         this.write_I = init_I_I(autismclient.gui.ui.widgets.ScrollbarMetricsHelper.scrollOffsetForMouseY(scrollbar, virtual.y(), this.sync), this.close_I_2(), scrollbar.maxScroll());
         this.init_CacheEntry.load(this.write_I, scrollbar.maxScroll());
         return true;
      } else {
         autismclient.b.DataUtil.StreamUtil row = this.init_DataUtil_StreamUti_D_D(virtual.x(), virtual.y());
         if (row != null) {
            this.read_Str = row.init_Str();
            this.load_V_5();
            this.close_V_2();
            return true;
         } else {
            return super.mouseClicked(virtual, doubleClick);
         }
      }
   }

   @Override
   public boolean mouseReleased(MouseButtonEvent event) {
      if (this.load_Z) {
         this.load_Z = false;
         return true;
      } else {
         return super.mouseReleased(init_MouseButtonEvent_MouseButtonEvent(event));
      }
   }

   @Override
   public boolean mouseDragged(MouseButtonEvent event, double dx, double dy) {
      MouseButtonEvent virtual = init_MouseButtonEvent_MouseButtonEvent(event);
      if (this.load_Z) {
         autismclient.gui.ui.widgets.ScrollbarMetricsHelper.Metrics scrollbar = this.init_PoolEntry_DataUtil_I_2(this.init_List_3().size());
         this.write_I = init_I_I(autismclient.gui.ui.widgets.ScrollbarMetricsHelper.scrollOffsetForMouseY(scrollbar, virtual.y(), this.sync), this.close_I_2(), scrollbar.maxScroll());
         this.init_CacheEntry.load(this.write_I, scrollbar.maxScroll());
         return true;
      } else {
         return super.mouseDragged(virtual, cA.init_D_D(dx), cA.init_D_D(dy));
      }
   }

   @Override
   public boolean mouseScrolled(double x, double y, double scrollX, double scrollY) {
      x = cA.init_D_D(x);
      y = cA.init_D_D(y);
      if (!(x < this.set()) && !(x >= this.set() + this.get()) && !(y < this.run()) && !(y >= this.next())) {
         int maxScroll = this.init_I_I_3(this.init_List_3().size());
         if (maxScroll <= 0) {
            return true;
         }

         this.write_I = init_I_I(this.write_I - (int)Math.signum(scrollY) * this.close_I_2(), this.close_I_2(), maxScroll);
         this.init_CacheEntry.init_V_I_I(this.write_I, maxScroll);
         return true;
      } else {
         return super.mouseScrolled(x, y, scrollX, scrollY);
      }
   }

   @Override
   public void onClose() {
      this.minecraft.gui.setScreen(this.init_Screen);
   }

   private autismclient.b.DataUtil.StreamUtil init_DataUtil_StreamUti_D_D(double mouseX, double mouseY) {
      if (!(mouseX < this.valid()) && !(mouseX >= this.data()) && !(mouseY < this.start()) && !(mouseY >= this.stop())) {
         List<autismclient.b.DataUtil.StreamUtil> reports = this.init_List_3();
         int local = (int)mouseY - this.start() + this.write_I;
         if (local % this.close_I_2() >= this.flush_I_2()) {
            return null;
         } else {
            int index = local / this.close_I_2();
            if (this.start() + index * this.close_I_2() - this.write_I + this.flush_I_2() > this.stop()) {
               return null;
            } else {
               return index >= 0 && index < reports.size() ? reports.get(index) : null;
            }
         }
      } else {
         return null;
      }
   }

   private void write_V_3() {
      autismclient.b.DataUtil.StreamUtil report = this.init_DataUtil_StreamUti();
      if (report != null && this.minecraft != null) {
         this.minecraft.keyboardHandler.setClipboard(report.init_Str());
         bi.init("Addon ID copied.");
      }
   }

   private void update_V_3() {
      autismclient.b.DataUtil.StreamUtil report = this.init_DataUtil_StreamUti();
      if (report != null && this.minecraft != null) {
         this.minecraft.keyboardHandler.setClipboard(report.open());
         bi.init("Addon report copied.");
      }
   }

   private void sync_V_2() {
      autismclient.b.DataUtil.StreamUtil report = this.init_DataUtil_StreamUti();
      if (report != null) {
         boolean next = !autismclient.b.DataUtil.read(report.init_Str());
         autismclient.b.DataUtil.init(report.init_Str(), next);
         bi.init(next ? "Addon disables after restart." : "Addon enables after restart.", next ? -14249 : GuiColorPalette.open);
         this.load_V_5();
      }
   }

   private void flush_V_2() {
      Compat263.openFile(Util.getPlatform(), autismclient.b.DataUtil.init_File());
   }

   private List<autismclient.b.DataUtil.StreamUtil> init_List_3() {
      String query = load_Str_Str_3(this.init_Str);
      if (query.equals(this.write_Str)) {
         return this.read_List;
      }

      this.write_Str = query;
      if (query.isEmpty()) {
         this.read_List = this.load_List;
         return this.read_List;
      }

      List<autismclient.b.DataUtil.StreamUtil> out = new ArrayList<>();

      for (autismclient.b.DataUtil.StreamUtil report : this.load_List) {
         if (load_Str_Str_3(report.load_Str()).contains(query)
            || load_Str_Str_3(report.init_Str()).contains(query)
            || load_Str_Str_3(report.write_Str()).contains(query)) {
            out.add(report);
         }
      }

      this.read_List = List.copyOf(out);
      return this.read_List;
   }

   private autismclient.b.DataUtil.StreamUtil init_DataUtil_StreamUti() {
      for (autismclient.b.DataUtil.StreamUtil report : this.load_List) {
         if (report.init_Str().equals(this.read_Str)) {
            return report;
         }
      }

      return null;
   }

   private String init_Str_2() {
      int total = this.load_List.size();
      int loaded = 0;
      int issues = 0;
      int disabled = 0;

      for (autismclient.b.DataUtil.StreamUtil report : this.load_List) {
         if (report.init_DataUtil_DataUtil() == autismclient.b.DataUtil.Inner_DataUtil.LOADED) {
            loaded++;
         }

         if (report.init_DataUtil_DataUtil().init_Z()) {
            issues++;
         }

         if (autismclient.b.DataUtil.read(report.init_Str()) || report.init_DataUtil_DataUtil() == autismclient.b.DataUtil.Inner_DataUtil.DISABLED) {
            disabled++;
         }
      }

      return total + " discovered  " + loaded + " loaded  " + issues + " issue(s)  " + disabled + " disabled/restart";
   }

   private String load_Str_DataUtil_StreamUti(autismclient.b.DataUtil.StreamUtil report) {
      if (report == null) {
         return "";
      } else {
         return autismclient.b.DataUtil.read(report.init_Str())
               && report.init_DataUtil_DataUtil() == autismclient.b.DataUtil.Inner_DataUtil.LOADED
            ? "Disables"
            : report.init_DataUtil_DataUtil().init_Str();
      }
   }

   private int load_I_DataUtil_StreamUti(autismclient.b.DataUtil.StreamUtil report) {
      if (report == null) {
         return GuiColorPalette.close;
      } else if (report.init_DataUtil_DataUtil().init_Z()) {
         return GuiColorPalette.poll;
      } else {
         return !autismclient.b.DataUtil.read(report.init_Str()) && report.init_DataUtil_DataUtil() != autismclient.b.DataUtil.Inner_DataUtil.DISABLED
            ? GuiColorPalette.open
            : -14249;
      }
   }

   private String read(autismclient.b.DataUtil.StreamUtil report) {
      String addon = report.load_I() < 0 ? "unknown" : Integer.toString(report.load_I());
      return "addon v" + addon + " / host v" + report.read_I();
   }

   private autismclient.gui.ui.widgets.ScrollbarMetricsHelper.Metrics init_PoolEntry_DataUtil_I_2(int rows) {
      int view = Math.max(1, this.stop() - this.start());
      return autismclient.gui.ui.widgets.ScrollbarMetricsHelper.compute(
         this.load_I_I_2(rows), view, this.size(), this.start(), 6, this.stop() - this.start(), this.write_I
      );
   }

   private int init_I_I_3(int rows) {
      return Math.max(0, this.load_I_I_2(rows) - Math.max(1, this.stop() - this.start()));
   }

   private int load_I_I_2(int rows) {
      return rows <= 0 ? 0 : rows * this.close_I_2() - 2;
   }

   private void close_V_2() {
      if (this.init_EditBox != null) {
         this.init_EditBox.setFocused(false);
      }

      this.setFocused(null);
   }

   private void init(GuiGraphicsExtractor graphics, String text, int x, int y, int color, boolean center) {
      this.init(graphics, text, x, y, color, center, 2147483647);
   }

   private void init(GuiGraphicsExtractor graphics, String text, int x, int y, int color, boolean center, int maxWidth) {
      if (graphics != null) {
         Font renderer = this.font;
         Identifier fontId = init_CharEncoder.init_Identifier_l(l.read);
         String value = text == null ? "" : text;
         if (maxWidth != 2147483647 && !center) {
            k.load(graphics, renderer, value, fontId, color, x, y, Math.max(1, maxWidth), false);
         } else {
            if (maxWidth != 2147483647) {
               value = k.load(renderer, value, maxWidth, fontId, color);
            }

            int w = k.init(renderer, value, fontId, color);
            k.init(graphics, renderer, value, fontId, color, center ? x - w / 2 : x, y, false);
         }
      }
   }

   private void init(GuiGraphicsExtractor graphics, String text, int right, int y, int color, int maxWidth) {
      if (graphics != null) {
         Identifier fontId = init_CharEncoder.init_Identifier_l(l.read);
         String value = k.load(this.font, text == null ? "" : text, Math.max(1, maxWidth), fontId, color);
         k.init(graphics, this.font, value, fontId, color, right - k.init(this.font, value, fontId, color), y, false);
      }
   }

   private int load_I_Str_2(String text) {
      return k.init(this.font, text == null ? "" : text, init_CharEncoder.init_Identifier_l(l.read), 0);
   }

   private int init_I_I_I_3(int top, int height) {
      return autismclient.gui.f.UiHelper.init(this.font).centerTextY(autismclient.gui.f.NetHelper.init(0, top, 1, height));
   }

   private int sync_I_2() {
      return autismclient.gui.ui.UiDensityMetrics.scaledHeight(13);
   }

   private int flush_I_2() {
      return autismclient.gui.ui.UiDensityMetrics.scaledHeight(14);
   }

   private int close_I_2() {
      return this.flush_I_2() + 2;
   }

   private int open() {
      return Math.max(200, Math.min(560, this.init_I() - 12));
   }

   private int poll() {
      return (this.init_I() - this.open()) / 2;
   }

   private int push() {
      return 6;
   }

   private int pop() {
      return this.push() + 4;
   }

   private int tick_I_4() {
      return this.sync_I_2() + 8;
   }

   private int run() {
      return this.push() + this.tick_I_4() + 4;
   }

   private int get() {
      return Math.max(140, Math.min(260, (this.open() - 4) * 45 / 100));
   }

   private int set() {
      return this.poll();
   }

   private int put() {
      return this.set() + this.get() + 4;
   }

   private int add() {
      return Math.max(1, this.poll() + this.open() - this.put());
   }

   private int clear() {
      return Math.max(40, this.add() - 8);
   }

   private int reset() {
      return this.run() + 4 + 12 + 2;
   }

   private int start() {
      return this.reset() + this.sync_I_2() + 2;
   }

   private int stop() {
      return this.next() - 4;
   }

   private int next() {
      return this.run() + this.prev();
   }

   private int prev() {
      int available = Math.max(60, this.load_I() - 6 - this.run());
      int maxRows = Math.max(3, this.load_I() * 65 / 100 / this.close_I_2());
      int rows = Math.max(3, Math.min(maxRows, this.init_List_3().size()));
      int list = this.start() - this.run() + this.load_I_I_2(rows) + 4;
      autismclient.b.DataUtil.StreamUtil selected = this.init_DataUtil_StreamUti();
      int details = this.init_I_GuiGraphicsExtract_DataUtil_StreamUti(null, selected) - this.run() + 4 + this.init_I_DataUtil_StreamUti(selected) + 4;
      return Math.min(available, Math.max(list, details));
   }

   private int valid() {
      return this.set() + 4;
   }

   private int size() {
      return this.set() + this.get() - 4 - 6;
   }

   private int data() {
      return this.size() - 2;
   }

   private int value() {
      return Math.max(1, this.data() - this.valid());
   }

   private static int init_I_I(int value, int step, int maxScroll) {
      int clamped = Math.max(0, Math.min(maxScroll, value));
      if (step <= 0) {
         return clamped;
      }

      int rounded = Math.round((float)clamped / step) * step;
      return Math.max(0, Math.min(maxScroll, rounded));
   }

   private static String load_Str_Str_3(String value) {
      return init_Str_Str(value).toLowerCase(Locale.ROOT);
   }
}

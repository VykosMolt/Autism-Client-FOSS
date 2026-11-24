package autismclient.gui.d;

import autismclient.compat263.Compat263;
import autismclient.gui.f.b.j;
import autismclient.gui.f.b.k;
import autismclient.gui.f.b.l;
import autismclient.util.AutismMacroEditorOverlay;
import autismclient.util.aM;
import autismclient.util.aN;
import autismclient.util.bk;
import autismclient.util.cA;
import autismclient.util.cX;
import autismclient.util.cY;
import autismclient.util.ct;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.function.Consumer;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;

public final class BitSetUtil extends KeyHolder {
   private static final autismclient.gui.f.b.CharEncoder init_CharEncoder = new autismclient.gui.f.b.CharEncoder();
   private static final int init_I = 6;
   private static final int load_I = 4;
   private static final int read_I = 240;
   private static final int write_I = 3;
   private final Screen init_Screen;
   private String init_Str;
   private final Consumer<String> init_Consumer;
   private final List<autismclient.gui.f.b.ByteReader> init_List = new ArrayList<>();
   private final List<BitSetUtil.Inner_DataUtil> load_List = new ArrayList<>();
   private EditBox init_EditBox;
   private String load_Str = "";
   private String read_Str = "";
   private int update;
   private boolean init_Z;
   private int sync;
   private long init_J = -9223372036854775808L;
   private String write_Str = "";
   private List<cY.Inner_DataUtil> read_List = List.of();
   private boolean load_Z;
   private boolean read_Z = true;
   private long load_J = -9223372036854775808L;
   private int flush = 6;
   private int close = 6;
   private int open = 240;
   private int poll = 3;

   public BitSetUtil(Screen parent, String currentName, Consumer<String> onPick) {
      super(Component.literal("Choose Macro"));
      this.init_Screen = parent;
      this.init_Str = currentName == null ? "" : currentName;
      this.init_Consumer = onPick;
   }

   @Override
   public void tick() {
      super.tick();
      if (this.load_Z || aN.init_aN().init_J() != this.init_J) {
         this.load_Z = false;
         this.load_V_2();
      }
   }

   @Override
   protected void init() {
      this.read_Z = true;
      this.init_EditBox = new StateCache(this.font, 0, 0, 1, read(), Component.literal("Search macros"));
      this.init_EditBox.setHint(Component.literal("Search macros..."));
      this.init_EditBox.setMaxLength(128);
      this.init_EditBox.setValue(this.load_Str);
      this.init_EditBox.setResponder(value -> {
         this.load_Str = init_Str_Str(value);
         this.update = 0;
         this.load_Z = true;
      });
      this.addRenderableWidget(this.init_EditBox);
      this.load_V_2();
   }

   private void load_V_2() {
      long revision = aN.init_aN().init_J();
      if (this.read_Z || revision != this.load_J) {
         this.init_V_J(revision);
      }

      this.init_List.clear();
      this.load_List.clear();
      int ctl = read();
      int gap = 2;
      int backW = this.init_I_Str_2("Back");
      int newW = this.init_I_Str_2("New Macro");
      int backX = this.flush + this.open - 4 - backW;
      this.init_List
         .add(
            autismclient.gui.f.b.ByteReader.init(backX, this.close(), backW, ctl, Component.literal("Back"), b -> this.onClose())
               .init_ByteReader_ByteReader_StreamU(autismclient.gui.f.b.ByteReader.Inner_StreamUtil.init)
         );
      this.init_List
         .add(
            autismclient.gui.f.b.ByteReader.init(backX - gap - newW, this.close(), newW, ctl, Component.literal("New Macro"), b -> this.init_V_aM(null))
               .init_ByteReader_ByteReader_StreamU(autismclient.gui.f.b.ByteReader.Inner_StreamUtil.write)
         );
      List<cY.Inner_DataUtil> names = this.init_List_2();
      int total = 1 + names.size();
      int visible = Math.max(1, this.poll);
      this.update = Math.max(0, Math.min(this.update, Math.max(0, total - visible)));
      int rowH = write();
      int btnH = sync();
      int editW = this.init_I_Str_2("Edit");
      int deleteW = this.flush();
      int y = this.pop();

      for (int i = this.update; i < total && y + rowH <= this.tick_I_2(); i++) {
         cY.Inner_DataUtil item = i == 0 ? null : names.get(i - 1);
         String name = item != null && !item.init_Z() ? item.init_aM().init_Str : null;
         autismclient.gui.f.b.ByteReader edit = null;
         autismclient.gui.f.b.ByteReader delete = null;
         if (name != null) {
            String macroName = name;
            int btnY = y + (rowH - btnH) / 2;
            int editX = this.push() - gap - editW;
            edit = autismclient.gui.f.b.ByteReader.init(
                  editX, btnY, editW, btnH, Component.literal("Edit"), b -> this.init_V_aM(aN.init_aN().init_aM_Str(macroName))
               )
               .init_ByteReader_ByteReader_StreamU(autismclient.gui.f.b.ByteReader.Inner_StreamUtil.init);
            boolean arming = macroName.equals(this.read_Str);
            delete = autismclient.gui.f.b.ByteReader.init(
                  editX - gap - deleteW, btnY, deleteW, btnH, Component.literal(arming ? "Sure?" : "Delete"), b -> this.init_V_Str(macroName)
               )
               .init_ByteReader_ByteReader_StreamU(autismclient.gui.f.b.ByteReader.Inner_StreamUtil.load);
         }

         this.load_List.add(new BitSetUtil.Inner_DataUtil(name, y, delete, edit, item));
         y += update();
      }
   }

   private void init_V_J(long revision) {
      this.read_Z = false;
      this.load_J = revision;
      int gap = 2;
      int ctl = read();
      Identifier fontId = init_CharEncoder.init_Identifier_l(l.read);
      int header = k.init(this.font, "Choose Macro", fontId, 0) + 8 + this.init_I_Str_2("New Macro") + gap + this.init_I_Str_2("Back");
      int nameW = k.init(this.font, "None (clear macro)", fontId, 0);

      for (aM macro : aN.init_aN().init_List()) {
         if (macro != null && macro.init_Str != null) {
            nameW = Math.max(nameW, k.init(this.font, macro.init_Str, fontId, 0));
         }
      }

      int row = 5 + nameW + gap * 3 + this.flush() + this.init_I_Str_2("Edit") + 6 + gap;
      int maxW = Math.max(1, this.init_I() - 12);
      this.open = Math.min(maxW, Math.max(240, Math.max(header, row) + 8));
      int total = 1 + aN.init_aN().init_List_Str("").size();
      int chrome = 4 + ctl + gap + ctl + 4 + 4 - gap;
      int fit = Math.max(1, (this.load_I() - 12 - chrome) / update());
      this.poll = Math.max(Math.min(3, fit), Math.min(total, fit));
      int panelH = chrome + this.poll * update();
      this.flush = Math.max(6, (this.init_I() - this.open) / 2);
      this.close = Math.max(6, (this.load_I() - panelH) / 2);
      if (this.init_EditBox != null) {
         this.init_EditBox.setX(this.flush + 4);
         this.init_EditBox.setY(this.open());
         this.init_EditBox.setWidth(Math.max(1, this.open - 8));
      }
   }

   private void init_V_Str(String name) {
      if (name != null && !name.isBlank()) {
         if (name.equals(this.read_Str)) {
            aM macro = aN.init_aN().init_aM_Str(name);
            if (macro != null) {
               aN.init_aN().read(macro);
            }

            autismclient.util.g.TaskTimer.init_TaskTimer().init_I(name, "");
            autismclient.util.g.PacketUtil.init_PacketUtil().load_V(name, "");
            if (name.equals(this.init_Str)) {
               this.init_Str = "";
               if (this.init_Consumer != null) {
                  this.init_Consumer.accept("");
               }
            }

            this.read_Str = "";
         } else {
            this.read_Str = name;
         }

         this.load_V_2();
      }
   }

   private void init_V_aM(aM macro) {
      AutismMacroEditorOverlay editor = AutismMacroEditorOverlay.init_AutismMacroEditorO_2();
      if (editor != null) {
         bk.init_bk().init_V_cX_cX_DataUtil(editor, cX.Inner_DataUtil.init);
         boolean inWorld = this.minecraft != null && this.minecraft.player != null && this.minecraft.level != null;
         editor.load_V_Z_3(!inWorld);
         String oldName = macro != null && macro.init_Str != null ? macro.init_Str : "";
         editor.load_V_aM_Consumer(macro, saved -> {
            if (saved != null && saved.init_Str != null && !oldName.isBlank() && !oldName.equals(saved.init_Str)) {
               autismclient.util.g.TaskTimer.init_TaskTimer().init_I(oldName, saved.init_Str);
               autismclient.util.g.PacketUtil.init_PacketUtil().load_V(oldName, saved.init_Str);
               if (oldName.equals(this.init_Str)) {
                  this.init_Str = saved.init_Str;
                  if (this.init_Consumer != null) {
                     this.init_Consumer.accept(saved.init_Str);
                  }
               }
            }
         });
         if (this.minecraft != null) {
            this.minecraft.gui.setScreen(new TaskTimer(editor, this, true));
         }
      }
   }

   private void load(String name) {
      if (this.init_Consumer != null) {
         this.init_Consumer.accept(name == null ? "" : name);
      }

      if (this.minecraft != null) {
         this.minecraft.gui.setScreen(this.init_Screen);
      }
   }

   private List<cY.Inner_DataUtil> init_List_2() {
      String query = this.load_Str.toLowerCase(Locale.ROOT);
      long revision = aN.init_aN().init_J();
      if (revision == this.init_J && query.equals(this.write_Str)) {
         return this.read_List;
      }

      List<cY.Inner_DataUtil> out = aN.init_aN().init_List_Str(query);
      this.init_J = revision;
      this.write_Str = query;
      this.read_List = List.copyOf(out);
      return this.read_List;
   }

   @Override
   public boolean mouseClicked(MouseButtonEvent event, boolean doubled) {
      MouseButtonEvent virtualEvent = init_MouseButtonEvent_MouseButtonEvent(event);
      if (Compat263.mouseEventGlfwButton(virtualEvent) == 0) {
         autismclient.gui.f.b.PoolEntry.Inner_DataUtil bar = this.init_PoolEntry_DataUtil();
         if (bar.init_Z() && bar.init(virtualEvent.x(), virtualEvent.y())) {
            this.init_Z = true;
            this.sync = bar.load(virtualEvent.x(), virtualEvent.y()) ? (int)Math.round(virtualEvent.y() - bar.update()) : bar.sync() / 2;
            this.update = this.init_I_I_2(autismclient.gui.f.b.PoolEntry.init(bar, virtualEvent.y(), this.sync) / update());
            this.load_V_2();
            return true;
         }

         for (autismclient.gui.f.b.ByteReader button : this.init_List) {
            if (autismclient.gui.f.b.ByteReader.init(button, virtualEvent.x(), virtualEvent.y(), Compat263.mouseEventGlfwButton(virtualEvent))) {
               return true;
            }
         }

         for (BitSetUtil.Inner_DataUtil row : this.load_List) {
            if (row.init_ByteReader() != null
               && autismclient.gui.f.b.ByteReader.init(row.init_ByteReader(), virtualEvent.x(), virtualEvent.y(), Compat263.mouseEventGlfwButton(virtualEvent))
               )
             {
               return true;
            }
         }

         for (BitSetUtil.Inner_DataUtil row : this.load_List) {
            if (row.load() != null
               && autismclient.gui.f.b.ByteReader.init(row.load(), virtualEvent.x(), virtualEvent.y(), Compat263.mouseEventGlfwButton(virtualEvent))) {
               return true;
            }
         }

         for (BitSetUtil.Inner_DataUtil row : this.load_List) {
            if (virtualEvent.x() >= this.poll()
               && virtualEvent.x() < this.push()
               && virtualEvent.y() >= row.init_I()
               && virtualEvent.y() < row.init_I() + write()) {
               if (row.init_cY_DataUtil() != null && row.init_cY_DataUtil().init_Z()) {
                  aN.init_aN().init_V_Str(row.init_cY_DataUtil().load_Str());
                  this.read_Z = true;
                  this.load_V_2();
                  return true;
               }

               this.read_Str = "";
               this.load(row.init_Str() == null ? "" : row.init_Str());
               return true;
            }
         }
      }

      return super.mouseClicked(virtualEvent, doubled);
   }

   @Override
   public boolean mouseDragged(MouseButtonEvent event, double dragX, double dragY) {
      MouseButtonEvent virtualEvent = init_MouseButtonEvent_MouseButtonEvent(event);
      if (this.init_Z) {
         autismclient.gui.f.b.PoolEntry.Inner_DataUtil bar = this.init_PoolEntry_DataUtil();
         this.update = this.init_I_I_2(autismclient.gui.f.b.PoolEntry.init(bar, virtualEvent.y(), this.sync) / update());
         this.load_V_2();
         return true;
      } else {
         return super.mouseDragged(virtualEvent, cA.init_D_D(dragX), cA.init_D_D(dragY));
      }
   }

   @Override
   public boolean mouseReleased(MouseButtonEvent event) {
      MouseButtonEvent virtualEvent = init_MouseButtonEvent_MouseButtonEvent(event);
      if (this.init_Z) {
         this.init_Z = false;
         return true;
      } else {
         return super.mouseReleased(virtualEvent);
      }
   }

   @Override
   public boolean mouseScrolled(double mouseX, double mouseY, double horizontal, double vertical) {
      this.update = this.init_I_I_2(this.update + (vertical < 0.0 ? 1 : -1));
      this.load_V_2();
      return true;
   }

   private autismclient.gui.f.b.PoolEntry.Inner_DataUtil init_PoolEntry_DataUtil() {
      int total = 1 + this.init_List_2().size();
      int viewport = this.tick_I_2() - this.pop();
      int trackX = this.flush + this.open - 4 - 6;
      return autismclient.gui.f.b.PoolEntry.init_PoolEntry_DataUtil_I_2(total * update() - 2, viewport, trackX, this.pop(), 6, viewport, this.update * update());
   }

   private int init_I_I_2(int rowIndex) {
      int total = 1 + this.init_List_2().size();
      int visible = Math.max(1, this.poll);
      return Math.max(0, Math.min(rowIndex, Math.max(0, total - visible)));
   }

   @Override
   public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float delta) {
      int virtualMouseX = cA.init_I_D(mouseX);
      int virtualMouseY = cA.init_I_D(mouseY);
      cA.init_V_GuiGraphicsExtract(graphics);

      try {
         autismclient.gui.f.PacketUtil.init(graphics, autismclient.gui.f.NetHelper.init(0, 0, this.init_I(), this.load_I()), run());
         autismclient.gui.f.PacketUtil.load(
            graphics, autismclient.gui.f.NetHelper.init(this.flush, this.close, this.open, this.tick_I_2() + 4 - this.close), 6, get(), put()
         );
         this.init(graphics, "Choose Macro", this.flush + 4, this.init_I(this.close(), read()), add());

         for (BitSetUtil.Inner_DataUtil row : this.load_List) {
            this.init(graphics, row);
         }

         for (autismclient.gui.f.b.ByteReader button : this.init_List) {
            autismclient.gui.f.b.ByteReader.init(graphics, this.font, button, virtualMouseX, virtualMouseY);
         }

         autismclient.gui.f.b.PoolEntry.Inner_DataUtil bar = this.init_PoolEntry_DataUtil();
         if (bar.init_Z()) {
            autismclient.gui.f.b.PoolEntry.init(graphics, bar, bar.init(virtualMouseX, virtualMouseY), this.init_Z);
         }

         super.extractRenderState(graphics, virtualMouseX, virtualMouseY, delta);
      } finally {
         cA.load_V_GuiGraphicsExtract(graphics);
      }
   }

   private void init(GuiGraphicsExtractor graphics, BitSetUtil.Inner_DataUtil row) {
      if (row.init_cY_DataUtil() != null && row.init_cY_DataUtil().init_Z()) {
         autismclient.gui.f.b.TaskQueue.load(
            autismclient.gui.f.UiHelper.init(graphics, this.font, -2147483648, -2147483648),
            autismclient.gui.f.NetHelper.init(this.poll(), row.init_I(), this.push() - this.poll(), write()),
            row.init_cY_DataUtil().init_Str(),
            row.init_cY_DataUtil().load_Z(),
            false
         );
      } else {
         boolean none = row.init_Str() == null;
         boolean selected = none ? this.init_Str.isBlank() : this.init_Str.equals(row.init_Str());
         int fill = selected ? set() : ct.init_I_I_ct_DataUtil(-1206840035, ct.Inner_DataUtil.close);
         int rowH = write();
         autismclient.gui.f.PacketUtil.load(graphics, autismclient.gui.f.NetHelper.init(this.poll(), row.init_I(), this.push() - this.poll(), rowH), 4, fill);
         if (selected) {
            autismclient.gui.f.PacketUtil.init(
               graphics, autismclient.gui.f.NetHelper.init(this.poll(), row.init_I(), 2, rowH), ct.init_I_I_ct_DataUtil(ValueSink.sync, ct.Inner_DataUtil.sync)
            );
         }

         String label = none ? "None (clear macro)" : row.init_Str();
         int textX = this.poll() + 5;
         int textRight = row.init_ByteReader() != null ? row.init_ByteReader().init_I() - 2 : (row.load() != null ? row.load().init_I() - 2 : this.push() - 5);
         this.init(graphics, label, textX, this.init_I(row.init_I(), rowH), Math.max(20, textRight - textX), selected ? clear() : add());
         if (row.init_ByteReader() != null) {
            autismclient.gui.f.b.ByteReader.init(graphics, this.font, row.init_ByteReader(), -2147483648, -2147483648);
         }

         if (row.load() != null) {
            autismclient.gui.f.b.ByteReader.init(graphics, this.font, row.load(), -2147483648, -2147483648);
         }
      }
   }

   @Override
   public void onClose() {
      if (this.minecraft != null) {
         this.minecraft.gui.setScreen(this.init_Screen);
      }
   }

   private static int read() {
      return autismclient.gui.f.RenderState.init(13);
   }

   private static int write() {
      return autismclient.gui.f.RenderState.init(14);
   }

   private static int update() {
      return write() + 2;
   }

   private static int sync() {
      return Math.max(11, write() - 2);
   }

   private int init_I_Str_2(String text) {
      return autismclient.gui.f.RenderState.load(k.init(this.font, text, init_CharEncoder.init_Identifier_l(l.read), 0));
   }

   private int flush() {
      return Math.max(this.init_I_Str_2("Delete"), this.init_I_Str_2("Sure?"));
   }

   private int init_I(int y, int h) {
      return j.init(y, h, k.init(init_CharEncoder.init_Identifier_l(l.read)), 0);
   }

   private int close() {
      return this.close + 4;
   }

   private int open() {
      return this.close() + read() + 2;
   }

   private int poll() {
      return this.flush + 4;
   }

   private int push() {
      return this.flush + this.open - 4 - 6 - 2;
   }

   private int pop() {
      return this.open() + read() + 4;
   }

   private int tick_I_2() {
      return this.pop() + this.poll * update() - 2;
   }

   private void init(GuiGraphicsExtractor graphics, String text, int x, int y, int color) {
      k.init(graphics, this.font, text, init_CharEncoder.init_Identifier_l(l.read), color, x, y, false);
   }

   private void init(GuiGraphicsExtractor graphics, String text, int x, int y, int maxWidth, int color) {
      Identifier fontId = init_CharEncoder.init_Identifier_l(l.read);
      String safe = k.load(this.font, text == null ? "" : text, Math.max(1, maxWidth), fontId, color);
      k.init(graphics, this.font, safe, fontId, color, x, y, false);
   }

   private static int run() {
      return ct.init_I_I_ct_DataUtil(-15987181, ct.Inner_DataUtil.update);
   }

   private static int get() {
      return ct.init_I_I_ct_DataUtil(-401402080, ct.Inner_DataUtil.close);
   }

   private static int set() {
      return ct.init_I_I_ct_DataUtil(858052714, ct.Inner_DataUtil.sync);
   }

   private static int put() {
      return ct.init_I_I_ct_DataUtil(ValueSink.update, ct.Inner_DataUtil.load);
   }

   private static int add() {
      return ct.init_I_I_ct_DataUtil(ValueSink.flush, ct.Inner_DataUtil.read);
   }

   private static int clear() {
      return ct.init_I_I_ct_DataUtil(ValueSink.open, ct.Inner_DataUtil.sync);
   }

   private record Inner_DataUtil(
      String init_Str, int init_I, autismclient.gui.f.b.ByteReader init_ByteReader, autismclient.gui.f.b.ByteReader load, cY.Inner_DataUtil init_cY_DataUtil
   ) {
   }
}

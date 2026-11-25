package autismclient.gui.d;

import autismclient.compat263.Compat263;
import autismclient.gui.f.b.j;
import autismclient.gui.f.b.k;
import autismclient.gui.f.b.l;
import autismclient.util.cA;
import autismclient.util.ct;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.Button.OnPress;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.Identifier;
import net.minecraft.util.FormattedCharSequence;

public final class UiHelper extends KeyHolder {
   private static final autismclient.gui.f.b.CharEncoder init_CharEncoder = new autismclient.gui.f.b.CharEncoder();
   private static final int init_I = 16;
   private static final int load_I = 9;
   private static final int read_I = 10;
   private static final String init_Str = "ABCDEFGHJKLMNPQRSTUVWXYZabcdefghijkmnpqrstuvwxyz23456789";
   private final Screen init_Screen;
   private final Consumer<autismclient.util.g.DataUtil> init_Consumer;
   private final autismclient.util.g.DataUtil init_DataUtil;
   private final boolean init_Z;
   private EditBox init_EditBox;
   private boolean load_Z;
   private String load_Str;
   private int write;
   private final List<int[]> init_List = new ArrayList<>();
   private String read_Str = "";
   private int update = ValueSink.close;
   private int sync = 124;

   public UiHelper(
      Screen parent, autismclient.util.g.DataUtil draft, Set<String> initiallySelected, Consumer<autismclient.util.g.DataUtil> onSave, boolean passwordsOnly
   ) {
      super(Component.literal("Login Passwords"));
      this.init_Screen = parent;
      this.init_DataUtil = draft == null ? new autismclient.util.g.DataUtil(null, null, null, null) : draft.init_DataUtil();
      this.init_Consumer = onSave == null ? ignored -> {} : onSave;
      this.init_Z = passwordsOnly;
      List<String> ids = this.init_List_13();
      if (initiallySelected != null) {
         for (String id : initiallySelected) {
            if (ids.contains(id)) {
               this.load_Str = id;
               break;
            }
         }
      }

      if (this.load_Str == null && !ids.isEmpty()) {
         this.load_Str = ids.get(0);
      }
   }

   @Override
   protected void init() {
      this.clearWidgets();
      UiHelper.Inner_DataUtil l = this.init_UiHelper_DataUtil();
      int right = l.write() + 4;
      int rightW = l.update() - 8;
      int gap = 2;
      int ctrlH = this.read_I_16();
      int step = ctrlH + gap;
      int fieldY = l.close();
      String revealLabel = this.load_Z ? "Show" : "Hide";
      int revealW = this.init_I_Str_19(revealLabel);
      this.init_EditBox = new StateCache(this.font, right, fieldY, rightW - revealW - gap, ctrlH, Component.literal("Password"));
      this.init_EditBox.setMaxLength(16);
      this.init_EditBox.setHint(Component.literal("Type a password"));
      this.load_V_23();
      if (this.load_Str != null) {
         this.init_EditBox.setValue(this.load(this.load_Str));
      }

      this.addRenderableWidget(this.init_EditBox);
      this.init(right + rightW - revealW, fieldY, revealW, revealLabel, autismclient.gui.f.b.CachePool.Inner_DataUtil.load, b -> {
         this.load_Z = !this.load_Z;
         this.load_V_23();
      });
      int y = fieldY + step;
      int selW = this.init_I_Str_19("Set for Selected");
      int allW = this.init_I_Str_19("Set for All");
      if (selW + gap + allW <= rightW) {
         this.init(right, y, selW, "Set for Selected", autismclient.gui.f.b.CachePool.Inner_DataUtil.read, b -> this.read_V_16());
         this.init(right + selW + gap, y, allW, "Set for All", autismclient.gui.f.b.CachePool.Inner_DataUtil.write, b -> this.write_V_14());
      } else {
         int half = Math.max(1, (rightW - gap) / 2);
         this.init(right, y, half, "Set for Selected", autismclient.gui.f.b.CachePool.Inner_DataUtil.read, b -> this.read_V_16());
         this.init(right + half + gap, y, rightW - half - gap, "Set for All", autismclient.gui.f.b.CachePool.Inner_DataUtil.write, b -> this.write_V_14());
      }

      y += step;
      this.init(
         right,
         y,
         Math.min(this.init_I_Str_19("Generate Random for Each Account"), rightW),
         "Generate Random for Each Account",
         autismclient.gui.f.b.CachePool.Inner_DataUtil.load,
         b -> this.update_V_13()
      );
      y += step;
      this.sync = y + 2;
      int saveW = this.init_I_Str_19("Save");
      int backW = this.init_I_Str_19("Back");
      int footerRight = l.write() + l.update();
      this.init(footerRight - backW - gap - saveW, l.open(), saveW, "Save", autismclient.gui.f.b.CachePool.Inner_DataUtil.write, b -> this.close());
      this.init(footerRight - backW, l.open(), backW, "Back", autismclient.gui.f.b.CachePool.Inner_DataUtil.load, b -> this.onClose());
   }

   private UiHelper.Inner_DataUtil init_UiHelper_DataUtil() {
      int margin = 6;
      int sw = this.init_I();
      int sh = this.load_I();
      int left = margin;
      int leftW = Math.max(140, Math.min(240, sw / 3));
      int right = left + leftW + 4;
      int rightW = Math.max(150, sw - right - margin);
      int footerY = sh - margin - this.read_I_16();
      int panelY = margin;
      int panelH = Math.max(1, footerY - 4 - panelY);
      int contentTop = panelY + 4 + this.write_I_14() + 2;
      return new UiHelper.Inner_DataUtil(left, leftW, right, rightW, panelY, panelH, contentTop, footerY);
   }

   private int read_I_16() {
      return autismclient.gui.f.RenderState.init(13);
   }

   private int write_I_14() {
      return autismclient.gui.f.RenderState.init(12);
   }

   private int update_I_13() {
      return autismclient.gui.f.RenderState.init(14);
   }

   private int init_I_UiHelper_DataUtil(UiHelper.Inner_DataUtil l) {
      return l.init_I() - 4;
   }

   private void load_V_23() {
      if (this.init_EditBox != null) {
         this.init_EditBox.addFormatter((value, offset) -> FormattedCharSequence.forward(this.load_Z ? "*".repeat(value.length()) : value, Style.EMPTY));
      }
   }

   private void read_V_16() {
      if (this.load_Str == null) {
         this.load("Select an account on the left first.", ValueSink.poll);
      } else {
         String password = this.init_EditBox.getValue();
         if (this.init_Z_Str_5(password)) {
            this.init_DataUtil.init(this.load_Str, "password", password);
            this.load("Password set for " + this.read(this.load_Str) + ". Press Save to keep it.", ValueSink.open);
         }
      }
   }

   private void write_V_14() {
      String password = this.init_EditBox.getValue();
      if (this.init_Z_Str_5(password)) {
         List<String> ids = this.init_List_13();

         for (String id : ids) {
            this.init_DataUtil.init(id, "password", password);
         }

         this.load("Password set for all " + ids.size() + " account(s). Press Save to keep it.", ValueSink.open);
      }
   }

   private boolean init_Z_Str_5(String password) {
      if (password != null && !password.isBlank()) {
         if (password.length() > 16) {
            this.load("Password must be at most 16 characters.", ValueSink.poll);
            return false;
         } else {
            return true;
         }
      } else {
         this.load("Type a password first.", ValueSink.poll);
         return false;
      }
   }

   private void update_V_13() {
      List<String> ids = this.init_List_13();
      if (ids.isEmpty()) {
         this.load("No accounts.", ValueSink.poll);
      } else {
         for (String id : ids) {
            this.init_DataUtil.init(id, "password", init_Str_12());
         }

         this.load_Z = false;
         if (this.load_Str != null && this.init_EditBox != null) {
            this.init_EditBox.setValue(this.load(this.load_Str));
         }

         this.load_V_23();
         this.load("Generated a random password (9-16 chars) for each account - shown in the list. Press Save to keep them.", ValueSink.open);
      }
   }

   private static String init_Str_12() {
      SecureRandom random = new SecureRandom();
      int length = 9 + random.nextInt(8);
      StringBuilder out = new StringBuilder(length);

      for (int i = 0; i < length; i++) {
         out.append(
            "ABCDEFGHJKLMNPQRSTUVWXYZabcdefghijkmnpqrstuvwxyz23456789"
               .charAt(random.nextInt("ABCDEFGHJKLMNPQRSTUVWXYZabcdefghijkmnpqrstuvwxyz23456789".length()))
         );
      }

      return out.toString();
   }

   private String load_Str_7() {
      return "Type a password (you can see it), then Set for Selected or Set for All - or Generate a random one per account. Used to fill custom login screens.";
   }

   private void close() {
      this.init_Consumer.accept(this.init_DataUtil.init_DataUtil());
      if (this.minecraft != null) {
         this.minecraft.gui.setScreen(this.init_Screen);
      }
   }

   private String load(String id) {
      return this.init_DataUtil.init_Map_Str(id).getOrDefault("password", "");
   }

   private List<String> init_List_13() {
      List<String> ids = new ArrayList<>();

      for (autismclient.util.g.DataUtil.Inner_DataUtil account : this.init_DataUtil.init_List()) {
         ids.add(account.init());
      }

      return ids;
   }

   private String read(String id) {
      if ("default".equals(id)) {
         return "Current account";
      }

      return init_Str_Str_I_3(id, 32);
   }

   private static String init_Str_Str_I_3(String text, int maxChars) {
      if (text != null && !text.isBlank()) {
         StringBuilder out = new StringBuilder(Math.min(text.length(), Math.max(16, maxChars)));
         boolean spaced = false;

         for (int i = 0; i < text.length(); i++) {
            char ch = text.charAt(i);
            boolean space = Character.isISOControl(ch) || Character.isWhitespace(ch);
            if (space) {
               if (!spaced && out.length() > 0) {
                  out.append(' ');
                  spaced = true;
               }
            } else {
               out.append(ch);
               spaced = false;
               if (maxChars > 3 && out.length() >= maxChars) {
                  break;
               }
            }
         }

         String safe = out.toString().trim();
         if (maxChars > 3 && safe.length() > maxChars - 3) {
            safe = safe.substring(0, maxChars - 3).trim() + "...";
         }

         return safe;
      } else {
         return "";
      }
   }

   private void load(String text, int color) {
      this.read_Str = text == null ? "" : text;
      this.update = color;
   }

   private int init_I_Str_19(String label) {
      return autismclient.gui.f.RenderState.load(k.init(this.font, label, init_CharEncoder.init_Identifier_l(l.read), 0));
   }

   private void init(int x, int y, int w, int h, String label, autismclient.gui.f.b.CachePool.Inner_DataUtil tone, OnPress press) {
      int width = w <= 0 ? this.init_I_Str_19(label) : Math.max(1, w);
      this.addRenderableWidget(new StreamGate(x, y, width, h, Component.literal(label), tone, press));
   }

   private void init(int x, int y, int w, String label, autismclient.gui.f.b.CachePool.Inner_DataUtil tone, OnPress press) {
      this.init(x, y, w, this.read_I_16(), label, tone, press);
   }

   @Override
   public boolean mouseClicked(MouseButtonEvent event, boolean doubled) {
      MouseButtonEvent virtualEvent = init_MouseButtonEvent_MouseButtonEvent(event);
      if (Compat263.mouseEventGlfwButton(virtualEvent) == 0) {
         for (int[] rect : this.init_List) {
            if (virtualEvent.x() >= rect[0] && virtualEvent.x() < rect[0] + rect[2] && virtualEvent.y() >= rect[1] && virtualEvent.y() < rect[1] + rect[3]) {
               List<String> ids = this.init_List_13();
               if (rect[4] >= 0 && rect[4] < ids.size()) {
                  this.load_Str = ids.get(rect[4]);
                  if (this.init_EditBox != null) {
                     this.init_EditBox.setValue(this.load(this.load_Str));
                  }
               }

               return true;
            }
         }
      }

      return super.mouseClicked(virtualEvent, doubled);
   }

   @Override
   public boolean mouseScrolled(double mouseX, double mouseY, double horizontal, double vertical) {
      double vx = cA.init_D_D(mouseX);
      double vy = cA.init_D_D(mouseY);
      UiHelper.Inner_DataUtil l = this.init_UiHelper_DataUtil();
      if (vx >= l.load() && vx < l.load() + l.read() && vy >= l.close() && vy < this.init_I_UiHelper_DataUtil(l)) {
         this.write = Math.max(0, this.write + (vertical < 0.0 ? 1 : -1));
         return true;
      } else {
         return super.mouseScrolled(mouseX, mouseY, horizontal, vertical);
      }
   }

   @Override
   public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float delta) {
      int virtualMouseX = cA.init_I_D(mouseX);
      int virtualMouseY = cA.init_I_D(mouseY);
      cA.init_V_GuiGraphicsExtract(graphics);

      try {
         autismclient.gui.f.PacketUtil.init(
            graphics, autismclient.gui.f.NetHelper.init(0, 0, this.init_I(), this.load_I()), ct.init_I_I_ct_DataUtil(-15987181, ct.Inner_DataUtil.update)
         );
         UiHelper.Inner_DataUtil l = this.init_UiHelper_DataUtil();
         int left = l.load();
         int leftW = l.read();
         int right = l.write();
         int rightW = l.update();
         int border = ct.init_I_I_ct_DataUtil(ValueSink.update, ct.Inner_DataUtil.load);
         int textColor = ct.init_I_I_ct_DataUtil(ValueSink.flush, ct.Inner_DataUtil.read);
         int muted = ct.init_I_I_ct_DataUtil(ValueSink.close, ct.Inner_DataUtil.read);
         autismclient.gui.f.PacketUtil.load(
            graphics,
            autismclient.gui.f.NetHelper.init(left, l.sync(), leftW, l.flush()),
            5,
            ct.init_I_I_ct_DataUtil(-1206840035, ct.Inner_DataUtil.close),
            border
         );
         autismclient.gui.f.PacketUtil.load(
            graphics,
            autismclient.gui.f.NetHelper.init(right, l.sync(), rightW, l.flush()),
            5,
            ct.init_I_I_ct_DataUtil(-401402080, ct.Inner_DataUtil.close),
            border
         );
         int titleY = this.init_I_I_I_10(l.sync() + 4, this.write_I_14());
         this.init(graphics, "Accounts (click to edit)", left + 4, titleY, textColor);
         this.init(graphics, "Login Password", right + 4, titleY, textColor);
         this.init(graphics, l, left + 4, leftW - 8, textColor, muted, border);
         int textX = right + 4;
         int textW = rightW - 8;
         int hy = this.sync;

         for (FormattedCharSequence line : this.font.split(FormattedText.of(this.load_Str_7()), textW)) {
            graphics.text(this.font, line, textX, hy, muted, false);
            hy += 10;
         }

         if (!this.read_Str.isBlank()) {
            List<FormattedCharSequence> lines = this.font.split(FormattedText.of(this.read_Str), textW);
            int sy = l.init_I() - 4 - lines.size() * 10;

            for (FormattedCharSequence line : lines) {
               graphics.text(this.font, line, textX, sy, this.update, false);
               sy += 10;
            }
         }

         super.extractRenderState(graphics, virtualMouseX, virtualMouseY, delta);
      } finally {
         cA.load_V_GuiGraphicsExtract(graphics);
      }
   }

   private void init(GuiGraphicsExtractor graphics, UiHelper.Inner_DataUtil l, int x, int w, int textColor, int muted, int border) {
      this.init_List.clear();
      List<String> ids = this.init_List_13();
      int top = l.close();
      int rowH = this.update_I_13();
      int visible = Math.max(1, (this.init_I_UiHelper_DataUtil(l) - top) / rowH);
      this.write = Math.max(0, Math.min(this.write, Math.max(0, ids.size() - visible)));
      Identifier fontId = init_CharEncoder.init_Identifier_l(autismclient.gui.f.b.l.read);
      int pad = 4;
      int y = top;

      for (int i = this.write; i < Math.min(ids.size(), this.write + visible); i++) {
         String id = ids.get(i);
         boolean selected = id.equals(this.load_Str);
         int fill = selected ? ValueSink.open & 16777215 | 855638016 : 402653184;
         autismclient.gui.f.PacketUtil.init(graphics, autismclient.gui.f.NetHelper.init(x, y, w, rowH - 1), fill);
         if (selected) {
            autismclient.gui.f.PacketUtil.init(
               graphics, autismclient.gui.f.NetHelper.init(x, y, 2, rowH - 1), ct.init_I_I_ct_DataUtil(ValueSink.open, ct.Inner_DataUtil.sync)
            );
         }

         int ty = this.init_I_I_I_10(y, rowH - 1);
         String pass = this.load(id);
         String shown = pass.isEmpty() ? "(no password)" : (this.load_Z ? "*".repeat(pass.length()) : pass);
         String passLine = k.load(this.font, shown, Math.max(1, (w - pad * 3) / 2), fontId, muted);
         int passW = k.init(this.font, passLine, fontId, muted);
         k.init(graphics, this.font, passLine, fontId, pass.isEmpty() ? muted : textColor, x + w - pad - passW, ty, false);
         String name = k.load(this.font, this.read(id), Math.max(1, w - passW - pad * 3), fontId, textColor);
         k.init(graphics, this.font, name, fontId, selected ? ct.init_I_I_ct_DataUtil(ValueSink.open, ct.Inner_DataUtil.sync) : textColor, x + pad, ty, false);
         this.init_List.add(new int[]{x, y, w, rowH - 1, i});
         y += rowH;
      }
   }

   private int init_I_I_I_10(int boxY, int boxH) {
      return j.init(boxY, boxH, init_CharEncoder.init_I_l(l.read), init_CharEncoder.flush());
   }

   private void init(GuiGraphicsExtractor graphics, String text, int x, int y, int color) {
      Identifier fontId = init_CharEncoder.init_Identifier_l(autismclient.gui.f.b.l.read);
      k.init(graphics, this.font, init_Str_Str_I_3(text, 100), fontId, color, x, y, false);
   }

   @Override
   public void onClose() {
      if (this.minecraft != null) {
         this.minecraft.gui.setScreen(this.init_Screen);
      }
   }

   private record Inner_DataUtil(int init, int load, int read, int write, int update, int sync, int flush, int close) {
      int init_I() {
         return this.update + this.sync;
      }

      public int load() {
         return this.init;
      }

      public int read() {
         return this.load;
      }

      public int write() {
         return this.read;
      }

      public int update() {
         return this.write;
      }

      public int sync() {
         return this.update;
      }

      public int flush() {
         return this.sync;
      }

      public int close() {
         return this.flush;
      }

      public int open() {
         return this.close;
      }
   }
}

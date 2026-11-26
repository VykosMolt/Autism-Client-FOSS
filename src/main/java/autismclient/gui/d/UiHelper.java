package autismclient.gui.d;

import autismclient.gui.f.b.j;
import autismclient.gui.f.b.k;
import autismclient.gui.f.b.l;
import autismclient.util.cA;
import autismclient.util.ct;
import java.security.SecureRandom;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
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
   private static final String init_Str = "ABCDEFGHJKLMNPQRSTUVWXYZabcdefghijkmnpqrstuvwxyz23456789";
   private final Screen init_Screen;
   private final Consumer<Map<String, String>> init_Consumer;
   private final Map<String, String> init_Map;
   private EditBox init_EditBox;
   private boolean load_Z = true;
   private String read_Str = "";
   private int update = ValueSink.close;
   private int sync = 124;

   public UiHelper(Screen parent, Map<String, String> values, Consumer<Map<String, String>> onSave) {
      super(Component.literal("Login Password"));
      this.init_Screen = parent;
      this.init_Map = values == null ? new LinkedHashMap<>() : new LinkedHashMap<>(values);
      this.init_Consumer = onSave == null ? ignored -> {} : onSave;
   }

   @Override
   protected void init() {
      this.clearWidgets();
      UiHelper.Layout l = this.computeLayout();
      int innerX = l.x() + 6;
      int innerW = l.w() - 12;
      int gap = 2;
      int ctrlH = this.read_I_16();
      int step = ctrlH + gap;
      int fieldY = l.contentTop();
      int revealW = Math.max(this.init_I_Str_19("Show"), this.init_I_Str_19("Hide"));
      String existing = this.init_EditBox != null ? this.init_EditBox.getValue() : Objects.requireNonNullElse(this.init_Map.get("password"), "");
      this.init_EditBox = new StateCache(this.font, innerX, fieldY, Math.max(40, innerW - revealW - gap), ctrlH, Component.literal("Password"));
      this.init_EditBox.setMaxLength(16);
      this.init_EditBox.setHint(Component.literal("Type a password"));
      this.load_V_23();
      this.init_EditBox.setValue(existing);
      this.addRenderableWidget(this.init_EditBox);
      this.setInitialFocus(this.init_EditBox);
      this.addRenderableWidget(
         new StreamGate(
            innerX + innerW - revealW,
            fieldY,
            revealW,
            ctrlH,
            Component.literal(this.load_Z ? "Show" : "Hide"),
            autismclient.gui.f.b.CachePool.Inner_DataUtil.load,
            () -> this.load_Z ? "Show" : "Hide",
            b -> this.load_Z = !this.load_Z
         )
      );
      int y = fieldY + step;
      int genW = this.init_I_Str_19("Generate Random");
      int clearW = this.init_I_Str_19("Clear");
      if (genW + gap + clearW <= innerW) {
         this.init(innerX, y, genW, "Generate Random", autismclient.gui.f.b.CachePool.Inner_DataUtil.read, b -> this.update_V_13());
         this.init(innerX + genW + gap, y, clearW, "Clear", autismclient.gui.f.b.CachePool.Inner_DataUtil.load, b -> this.read_V_16());
      } else {
         int half = Math.max(1, (innerW - gap) / 2);
         this.init(innerX, y, half, "Generate Random", autismclient.gui.f.b.CachePool.Inner_DataUtil.read, b -> this.update_V_13());
         this.init(innerX + half + gap, y, innerW - half - gap, "Clear", autismclient.gui.f.b.CachePool.Inner_DataUtil.load, b -> this.read_V_16());
      }

      y += step;
      this.sync = y + 4;
      int saveW = this.init_I_Str_19("Save");
      int backW = this.init_I_Str_19("Back");
      int footerRight = l.x() + l.w();
      this.init(footerRight - backW - gap - saveW, l.footerY(), saveW, "Save", autismclient.gui.f.b.CachePool.Inner_DataUtil.write, b -> this.close());
      this.init(footerRight - backW, l.footerY(), backW, "Back", autismclient.gui.f.b.CachePool.Inner_DataUtil.load, b -> this.onClose());
   }

   private UiHelper.Layout computeLayout() {
      int margin = 12;
      int sw = this.init_I();
      int sh = this.load_I();
      int panelW = Math.max(200, Math.min(360, sw - margin * 2));
      int panelX = (sw - panelW) / 2;
      int footerY = sh - margin - this.read_I_16();
      int availH = Math.max(1, footerY - 4 - margin);
      int panelH = Math.min(180, availH);
      int panelY = Math.max(margin, (footerY - 4 - panelH) / 2);
      int contentTop = panelY + 6 + this.write_I_14() + 4;
      return new UiHelper.Layout(panelX, panelW, panelY, panelH, contentTop, footerY);
   }

   private int read_I_16() {
      return autismclient.gui.f.RenderState.init(13);
   }

   private int write_I_14() {
      return autismclient.gui.f.RenderState.init(12);
   }

   private void load_V_23() {
      if (this.init_EditBox != null) {
         this.init_EditBox.addFormatter((value, offset) -> FormattedCharSequence.forward(this.load_Z ? "*".repeat(value.length()) : value, Style.EMPTY));
      }
   }

   private void read_V_16() {
      if (this.init_EditBox != null) {
         this.init_EditBox.setValue("");
      }

      this.load("Password cleared. Press Save to apply.", ValueSink.open);
   }

   private void update_V_13() {
      String generated = init_Str_12();
      this.load_Z = false;
      if (this.init_EditBox != null) {
         this.init_EditBox.setValue(generated);
      }

      this.load("Generated a random password (9-16 chars). Press Save to keep it.", ValueSink.open);
   }

   private static String init_Str_12() {
      SecureRandom random = new SecureRandom();
      int length = 9 + random.nextInt(8);
      StringBuilder out = new StringBuilder(length);

      for (int i = 0; i < length; i++) {
         out.append(init_Str.charAt(random.nextInt(init_Str.length())));
      }

      return out.toString();
   }

   private String load_Str_7() {
      return "Password used to fill {secret.password} in Join Macros and custom login screens. Leave blank and press Save to clear.";
   }

   private void close() {
      String password = this.init_EditBox == null ? "" : this.init_EditBox.getValue();
      if (password != null && password.length() > 16) {
         this.load("Password must be at most 16 characters.", ValueSink.poll);
         return;
      }

      Map<String, String> updated = new LinkedHashMap<>(this.init_Map);
      if (password == null || password.isBlank()) {
         updated.remove("password");
      } else {
         updated.put("password", password);
      }

      this.init_Consumer.accept(updated);
      if (this.minecraft != null) {
         this.minecraft.gui.setScreen(this.init_Screen);
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
      return super.mouseClicked(init_MouseButtonEvent_MouseButtonEvent(event), doubled);
   }

   @Override
   public boolean mouseReleased(MouseButtonEvent event) {
      return super.mouseReleased(init_MouseButtonEvent_MouseButtonEvent(event));
   }

   @Override
   public boolean mouseDragged(MouseButtonEvent event, double deltaX, double deltaY) {
      return super.mouseDragged(init_MouseButtonEvent_MouseButtonEvent(event), cA.init_D_D(deltaX), cA.init_D_D(deltaY));
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
         UiHelper.Layout l = this.computeLayout();
         int border = ct.init_I_I_ct_DataUtil(ValueSink.update, ct.Inner_DataUtil.load);
         int textColor = ct.init_I_I_ct_DataUtil(ValueSink.flush, ct.Inner_DataUtil.read);
         int muted = ct.init_I_I_ct_DataUtil(ValueSink.close, ct.Inner_DataUtil.read);
         autismclient.gui.f.PacketUtil.load(
            graphics,
            autismclient.gui.f.NetHelper.init(l.x(), l.y(), l.w(), l.h()),
            5,
            ct.init_I_I_ct_DataUtil(-401402080, ct.Inner_DataUtil.close),
            border
         );
         int titleY = this.init_I_I_I_10(l.y() + 6, this.write_I_14());
         this.init(graphics, "Login Password", l.x() + 6, titleY, textColor);
         int textX = l.x() + 6;
         int textW = l.w() - 12;
         int maxTextY = l.bottom() - 6;
         List<FormattedCharSequence> statusLines = this.read_Str.isBlank() ? List.of() : this.font.split(FormattedText.of(this.read_Str), textW);
         int statusReserve = statusLines.isEmpty() ? 0 : statusLines.size() * 10 + 4;
         int helpLimitY = maxTextY - statusReserve;
         int hy = this.sync;

         for (FormattedCharSequence line : this.font.split(FormattedText.of(this.load_Str_7()), textW)) {
            if (hy + 9 > helpLimitY) {
               break;
            }

            graphics.text(this.font, line, textX, hy, muted, false);
            hy += 10;
         }

         if (!statusLines.isEmpty()) {
            int sy = Math.max(this.sync, maxTextY - statusLines.size() * 10);

            for (FormattedCharSequence line : statusLines) {
               if (sy + 9 > maxTextY) {
                  break;
               }

               graphics.text(this.font, line, textX, sy, this.update, false);
               sy += 10;
            }
         }

         super.extractRenderState(graphics, virtualMouseX, virtualMouseY, delta);
      } finally {
         cA.load_V_GuiGraphicsExtract(graphics);
      }
   }

   private int init_I_I_I_10(int boxY, int boxH) {
      return j.init(boxY, boxH, init_CharEncoder.init_I_l(l.read), init_CharEncoder.flush());
   }

   private void init(GuiGraphicsExtractor graphics, String text, int x, int y, int color) {
      Identifier fontId = init_CharEncoder.init_Identifier_l(autismclient.gui.f.b.l.read);
      k.init(graphics, this.font, text, fontId, color, x, y, false);
   }

   @Override
   public void onClose() {
      if (this.minecraft != null) {
         this.minecraft.gui.setScreen(this.init_Screen);
      }
   }

   private record Layout(int x, int w, int y, int h, int contentTop, int footerY) {
      int bottom() {
         return this.y + this.h;
      }
   }
}

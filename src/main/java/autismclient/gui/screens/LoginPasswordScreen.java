package autismclient.gui.screens;

import autismclient.gui.ui.widgets.UiLayoutMath;
import autismclient.gui.f.b.k;
import autismclient.gui.f.b.l;
import autismclient.util.cA;
import autismclient.util.ThemePaletteManager;
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

public final class LoginPasswordScreen extends VirtualScaledScreen {
   private static final autismclient.gui.f.b.CharEncoder STYLE_THEME = new autismclient.gui.f.b.CharEncoder();
   private static final int init_I = 16;
   private static final int load_I = 9;
   private static final String PASSWORD_ALPHABET = "ABCDEFGHJKLMNPQRSTUVWXYZabcdefghijkmnpqrstuvwxyz23456789";
   private final Screen parent;
   private final Consumer<Map<String, String>> onSave;
   private final Map<String, String> init_Map;
   private EditBox passwordField;
   private boolean masked = true;
   private String statusText = "";
   private int statusColor = GuiColorPalette.close;
   private int statusY = 124;

   public LoginPasswordScreen(Screen parent, Map<String, String> values, Consumer<Map<String, String>> onSave) {
      super(Component.literal("Login Password"));
      this.parent = parent;
      this.init_Map = values == null ? new LinkedHashMap<>() : new LinkedHashMap<>(values);
      this.onSave = onSave == null ? ignored -> {} : onSave;
   }

   @Override
   protected void init() {
      this.clearWidgets();
      LoginPasswordScreen.Layout l = this.computeLayout();
      int innerX = l.x() + 6;
      int innerW = Math.max(1, l.w() - 12);
      int gap = 2;
      int ctrlH = this.read_I_16();
      int step = ctrlH + gap;
      int fieldY = l.contentTop();
      int revealW = Math.max(this.buttonWidth("Show"), this.buttonWidth("Hide"));
      revealW = Math.max(1, Math.min(revealW, innerW - gap - 1));
      String existing = this.passwordField != null ? this.passwordField.getValue() : Objects.requireNonNullElse(this.init_Map.get("password"), "");
      int editW = Math.max(1, innerW - revealW - gap);
      this.passwordField = new MsdfEditBox(this.font, innerX, fieldY, editW, ctrlH, Component.literal("Password"));
      this.passwordField.setMaxLength(16);
      this.passwordField.setHint(Component.literal("Type a password"));
      this.applyMask();
      this.passwordField.setValue(existing);
      this.addRenderableWidget(this.passwordField);
      this.addRenderableWidget(
         new StyledButton(
            innerX + innerW - revealW,
            fieldY,
            revealW,
            ctrlH,
            Component.literal(this.masked ? "Show" : "Hide"),
            autismclient.gui.f.b.CachePool.Inner_DataUtil.load,
            () -> this.masked ? "Show" : "Hide",
            b -> this.masked = !this.masked
         )
      );
      int y = fieldY + step;
      int genW = this.buttonWidth("Generate Random");
      int clearW = this.buttonWidth("Clear");
      if (genW + gap + clearW <= innerW) {
         this.addButton(innerX, y, genW, "Generate Random", autismclient.gui.f.b.CachePool.Inner_DataUtil.read, b -> this.generatePassword());
         this.addButton(innerX + genW + gap, y, clearW, "Clear", autismclient.gui.f.b.CachePool.Inner_DataUtil.load, b -> this.clearPassword());
      } else {
         int half = Math.max(1, (innerW - gap) / 2);
         this.addButton(innerX, y, half, "Generate Random", autismclient.gui.f.b.CachePool.Inner_DataUtil.read, b -> this.generatePassword());
         this.addButton(innerX + half + gap, y, Math.max(1, innerW - half - gap), "Clear", autismclient.gui.f.b.CachePool.Inner_DataUtil.load, b -> this.clearPassword());
      }

      y += step;
      this.statusY = y + (l.h() < 110 ? 2 : 4);
      int saveW = this.buttonWidth("Save");
      int backW = this.buttonWidth("Back");
      int footerRight = l.x() + l.w();
      if (saveW + gap + backW <= l.w()) {
         this.addButton(footerRight - backW - gap - saveW, l.footerY(), saveW, "Save", autismclient.gui.f.b.CachePool.Inner_DataUtil.write, b -> this.savePassword());
         this.addButton(footerRight - backW, l.footerY(), backW, "Back", autismclient.gui.f.b.CachePool.Inner_DataUtil.load, b -> this.onClose());
      } else {
         int half = Math.max(1, (l.w() - gap) / 2);
         this.addButton(l.x(), l.footerY(), half, "Save", autismclient.gui.f.b.CachePool.Inner_DataUtil.write, b -> this.savePassword());
         this.addButton(l.x() + half + gap, l.footerY(), Math.max(1, l.w() - half - gap), "Back", autismclient.gui.f.b.CachePool.Inner_DataUtil.load, b -> this.onClose());
      }
   }

   @Override
   protected void setInitialFocus() {
      if (this.passwordField != null) {
         this.setInitialFocus(this.passwordField);
      }
   }

   private LoginPasswordScreen.Layout computeLayout() {
      int sw = this.virtualWidth();
      int sh = this.virtualHeight();
      int margin = sw < 224 || sh < 150 ? 4 : 12;
      int panelW = Math.max(1, Math.min(360, sw - margin * 2));
      int panelX = Math.max(0, (sw - panelW) / 2);
      int ctrlH = this.read_I_16();
      int footerY = Math.max(margin, sh - margin - ctrlH);
      int availH = Math.max(1, footerY - 4 - margin);
      int panelH = Math.min(180, availH);
      int panelY = Math.max(margin, (footerY - 4 - panelH) / 2);
      int contentTop = panelY + 6 + this.write_I_14() + 4;
      return new LoginPasswordScreen.Layout(panelX, panelW, panelY, panelH, contentTop, footerY);
   }

   private int read_I_16() {
      return autismclient.gui.ui.UiDensityMetrics.scaledHeight(13);
   }

   private int write_I_14() {
      return autismclient.gui.ui.UiDensityMetrics.scaledHeight(12);
   }

   private void applyMask() {
      if (this.passwordField != null) {
         this.passwordField.addFormatter((value, offset) -> FormattedCharSequence.forward(this.masked ? "*".repeat(value.length()) : value, Style.EMPTY));
      }
   }

   private void clearPassword() {
      if (this.passwordField != null) {
         this.passwordField.setValue("");
      }

      this.setStatus("Password cleared. Press Save to apply.", GuiColorPalette.open);
   }

   private void generatePassword() {
      String generated = randomPassword();
      this.masked = false;
      if (this.passwordField != null) {
         this.passwordField.setValue(generated);
      }

      this.setStatus("Generated a random password (9-16 chars). Press Save to keep it.", GuiColorPalette.open);
   }

   private static String randomPassword() {
      SecureRandom random = new SecureRandom();
      int length = 9 + random.nextInt(8);
      StringBuilder out = new StringBuilder(length);

      for (int i = 0; i < length; i++) {
         out.append(PASSWORD_ALPHABET.charAt(random.nextInt(PASSWORD_ALPHABET.length())));
      }

      return out.toString();
   }

   private String helpText() {
      return "Password used to fill {secret.password} in Join Macros and custom login screens. Leave blank and press Save to clear.";
   }

   private void savePassword() {
      String password = this.passwordField == null ? "" : this.passwordField.getValue();
      if (password != null && password.length() > 16) {
         this.setStatus("Password must be at most 16 characters.", GuiColorPalette.poll);
         return;
      }

      Map<String, String> updated = new LinkedHashMap<>(this.init_Map);
      if (password == null || password.isBlank()) {
         updated.remove("password");
      } else {
         updated.put("password", password);
      }

      this.onSave.accept(updated);
      if (this.minecraft != null) {
         this.minecraft.gui.setScreen(this.parent);
      }
   }

   private void setStatus(String text, int color) {
      this.statusText = text == null ? "" : text;
      this.statusColor = color;
   }

   private int buttonWidth(String label) {
      return autismclient.gui.ui.UiDensityMetrics.buttonWidthForText(k.init(this.font, label, STYLE_THEME.init_Identifier_l(l.read), 0));
   }

   private void addButton(int x, int y, int w, int h, String label, autismclient.gui.f.b.CachePool.Inner_DataUtil tone, OnPress press) {
      int width = w <= 0 ? this.buttonWidth(label) : Math.max(1, w);
      this.addRenderableWidget(new StyledButton(x, y, width, h, Component.literal(label), tone, press));
   }

   private void addButton(int x, int y, int w, String label, autismclient.gui.f.b.CachePool.Inner_DataUtil tone, OnPress press) {
      this.addButton(x, y, w, this.read_I_16(), label, tone, press);
   }

   @Override
   public boolean mouseClicked(MouseButtonEvent event, boolean doubled) {
      return super.mouseClicked(toVirtual(event), doubled);
   }

   @Override
   public boolean mouseReleased(MouseButtonEvent event) {
      return super.mouseReleased(toVirtual(event));
   }

   @Override
   public boolean mouseDragged(MouseButtonEvent event, double deltaX, double deltaY) {
      return super.mouseDragged(toVirtual(event), cA.init_D_D(deltaX), cA.init_D_D(deltaY));
   }

   @Override
   public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float delta) {
      int virtualMouseX = cA.init_I_D(mouseX);
      int virtualMouseY = cA.init_I_D(mouseY);
      cA.init_V_GuiGraphicsExtract(graphics);

      try {
         autismclient.gui.f.PacketUtil.init(
            graphics, autismclient.gui.f.NetHelper.init(0, 0, this.virtualWidth(), this.virtualHeight()), ThemePaletteManager.recolor(-15987181, ThemePaletteManager.ColorRole.BACKDROP)
         );
         LoginPasswordScreen.Layout l = this.computeLayout();
         int border = ThemePaletteManager.recolor(GuiColorPalette.update, ThemePaletteManager.ColorRole.OUTLINE);
         int textColor = ThemePaletteManager.recolor(GuiColorPalette.flush, ThemePaletteManager.ColorRole.TEXT);
         int muted = ThemePaletteManager.recolor(GuiColorPalette.close, ThemePaletteManager.ColorRole.TEXT);
         autismclient.gui.f.PacketUtil.load(
            graphics,
            autismclient.gui.f.NetHelper.init(l.x(), l.y(), l.w(), l.h()),
            5,
            ThemePaletteManager.recolor(-401402080, ThemePaletteManager.ColorRole.BUTTON),
            border
         );
         int titleY = this.centeredTextY(l.y() + 6, this.write_I_14());
         this.drawText(graphics, "Login Password", l.x() + 6, titleY, textColor);
         int textX = l.x() + 6;
         int textW = Math.max(1, l.w() - 12);
         int maxTextY = l.bottom() - (l.h() < 110 ? 3 : 6);
         List<FormattedCharSequence> statusLines = this.statusText.isBlank() ? List.of() : this.font.split(FormattedText.of(this.statusText), textW);
         int statusReserve = statusLines.isEmpty() ? 0 : statusLines.size() * 10 + 4;
         int helpLimitY = maxTextY - statusReserve;
         int hy = this.statusY;

         for (FormattedCharSequence line : this.font.split(FormattedText.of(this.helpText()), textW)) {
            if (hy + 9 > helpLimitY) {
               break;
            }

            graphics.text(this.font, line, textX, hy, muted, false);
            hy += 10;
         }

         if (!statusLines.isEmpty()) {
            int sy = Math.max(this.statusY, maxTextY - statusLines.size() * 10);

            for (FormattedCharSequence line : statusLines) {
               if (sy + 9 > maxTextY) {
                  break;
               }

               graphics.text(this.font, line, textX, sy, this.statusColor, false);
               sy += 10;
            }
         }

         super.extractRenderState(graphics, virtualMouseX, virtualMouseY, delta);
      } finally {
         cA.load_V_GuiGraphicsExtract(graphics);
      }
   }

   private int centeredTextY(int boxY, int boxH) {
      return UiLayoutMath.centerTextY(boxY, boxH, STYLE_THEME.init_I_l(l.read), STYLE_THEME.flush());
   }

   private void drawText(GuiGraphicsExtractor graphics, String text, int x, int y, int color) {
      Identifier fontId = STYLE_THEME.init_Identifier_l(autismclient.gui.f.b.l.read);
      k.init(graphics, this.font, text, fontId, color, x, y, false);
   }

   @Override
   public void onClose() {
      if (this.minecraft != null) {
         this.minecraft.gui.setScreen(this.parent);
      }
   }

   private record Layout(int x, int w, int y, int h, int contentTop, int footerY) {
      int bottom() {
         return this.y + this.h;
      }
   }
}

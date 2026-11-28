package autismclient.gui.screens;

import autismclient.compat263.Compat263;
import autismclient.gui.f.b.k;
import autismclient.gui.f.b.l;
import autismclient.util.cA;
import autismclient.util.ThemePaletteManager;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;

public class SimpleVoiceChatDetectedScreen extends Screen {
   private static final Identifier TITLE_FONT = autismclient.gui.f.a.DataUtil.read;
   private static final Identifier BODY_FONT = autismclient.gui.f.a.DataUtil.load;
   private static final long init_J = 5000L;
   private static final int init_I = -2828;
   private static final int load_I = -45747;
   private static final int read = -234223092;
   private static final int write = -46518;
   private static final int update = 1728006730;
   private static final int sync = 2;
   private static final int flush = 6;
   private static final int close = 340;
   private static final int open = 2;
   private static final String init_Str = "Simple Voice Chat detected";
   private static final String[][] SECTIONS = new String[][]{
      {"Voice chat won't fucking work right now", "Client fakes being vanilla so servers can't see it", "That hides the voicechat channel, so it can't connect"},
      {
            "Switch to Modded and here's what changes",
            "Voice chat starts working",
            "Servers can now tell you're on Fabric",
            "Kept: module hiding, channel filter, payload spoof",
            "Lost: the vanilla disguise. Not full protection anymore"
      }
   };
   private final Screen parent;
   private final autismclient.gui.f.b.CharEncoder styleTheme = new autismclient.gui.f.b.CharEncoder();
   private final long openedAtMillis = System.currentTimeMillis();
   private final SimpleVoiceChatDetectedScreen.Inner_DataUtil keepVanillaButton = new SimpleVoiceChatDetectedScreen.Inner_DataUtil("Keep Vanilla", () -> this.applyChoice(true));
   private final SimpleVoiceChatDetectedScreen.Inner_DataUtil switchModdedButton = new SimpleVoiceChatDetectedScreen.Inner_DataUtil("Switch to Modded", () -> this.applyChoice(false));
   private int layoutScreenW = -1;
   private int layoutScreenH = -1;
   private int layoutScaleKey = -1;
   private int wrappedWidth = -1;
   private List<List<String>> wrappedSections = List.of();
   private int widthScaleKey = -1;
   private int cachedContentWidth;

   public SimpleVoiceChatDetectedScreen(Screen parent) {
      super(Component.literal("Simple Voice Chat detected"));
      this.parent = parent;
   }

   private void applyChoice(boolean keepVanillaMode) {
      autismclient.modules.ModuleRuntimeManager module = autismclient.modules.ModuleRuntimeManager.getInstance();
      if (module != null) {
         module.setSpoofClientVanilla(keepVanillaMode);
      }

      this.minecraft.gui.setScreen(this.parent);
   }

   @Override
   public boolean isPauseScreen() {
      return false;
   }

   @Override
   public boolean shouldCloseOnEsc() {
      return false;
   }

   @Override
   public void extractBackground(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float delta) {
   }

   private boolean isReady() {
      return System.currentTimeMillis() - this.openedAtMillis >= 5000L;
   }

   private int countdownSeconds() {
      long remaining = 5000L - (System.currentTimeMillis() - this.openedAtMillis);
      return (int)Math.max(1.0, Math.ceil(remaining / 1000.0));
   }

   @Override
   public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float delta) {
      float uiMouseX = (float)cA.init_D_D(mouseX);
      float uiMouseY = (float)cA.init_D_D(mouseY);
      this.layoutButtons();
      cA.init_V_GuiGraphicsExtract(graphics);

      try {
         int screenW = cA.init_I();
         int screenH = cA.load_I();
         autismclient.gui.f.PacketUtil.init(graphics, autismclient.gui.f.NetHelper.init(0, 0, screenW, screenH), -939524096);
         int cardW = this.cardWidth();
         int cardX = (screenW - cardW) / 2;
         int cardH = this.cardHeight();
         int cardY = this.cardY(cardH);
         int innerW = cardW - 12;
         int x = cardX + 6;
         autismclient.gui.f.PacketUtil.init(graphics, autismclient.gui.f.NetHelper.init(cardX, cardY, cardW, cardH), -234223092);
         init_V_GuiGraphicsExtract(graphics, cardX, cardY, cardW, cardH, ThemePaletteManager.recolor(-46518, ThemePaletteManager.ColorRole.OUTLINE), 2);
         int y = cardY + 6;
         this.drawCentered(
            graphics, "Simple Voice Chat detected", TITLE_FONT, ThemePaletteManager.recolor(-2828, ThemePaletteManager.ColorRole.TEXT), x, innerW, y
         );
         y += k.init(TITLE_FONT) + 4;
         autismclient.gui.f.PacketUtil.init(
            graphics, autismclient.gui.f.NetHelper.init(x, y, innerW, 1), ThemePaletteManager.recolor(1728006730, ThemePaletteManager.ColorRole.OUTLINE)
         );
         y += 5;
         int lineH = this.lineHeight();
         int bodyColor = this.styleTheme.load(l.write);
         List<List<String>> linesBySection = this.wrapSections(innerW);

         for (int i = 0; i < SECTIONS.length; i++) {
            k.init(graphics, this.font, SECTIONS[i][0], BODY_FONT, ThemePaletteManager.recolor(-45747, ThemePaletteManager.ColorRole.ACCENT), x, y, false);
            y += lineH;

            for (String line : linesBySection.get(i)) {
               k.init(graphics, this.font, line, BODY_FONT, bodyColor, x, y, false);
               y += lineH;
            }

            y += 4;
         }

         boolean ready = this.isReady();
         this.switchModdedButton.label = ready ? "Switch to Modded" : "Switch to Modded (" + this.countdownSeconds() + ")";
         this.renderButton(graphics, this.keepVanillaButton, uiMouseX, uiMouseY, ready);
         this.renderButton(graphics, this.switchModdedButton, uiMouseX, uiMouseY, ready);
      } finally {
         cA.load_V_GuiGraphicsExtract(graphics);
      }
   }

   @Override
   public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
      if (Compat263.mouseEventGlfwButton(event) == 0 && this.isReady()) {
         float mx = (float)cA.init_D_D(event.x());
         float my = (float)cA.init_D_D(event.y());
         this.layoutButtons();
         if (this.keepVanillaButton.contains(mx, my)) {
            this.keepVanillaButton.action.run();
            return true;
         } else if (this.switchModdedButton.contains(mx, my)) {
            this.switchModdedButton.action.run();
            return true;
         } else {
            return false;
         }
      } else {
         return false;
      }
   }

   private int lineHeight() {
      return k.init(BODY_FONT) + 2;
   }

   private int buttonHeight() {
      return autismclient.gui.ui.UiDensityMetrics.scaledHeight(13);
   }

   private int cardWidth() {
      int screenW = cA.init_I();
      return Math.max(1, Math.min(Math.max(1, screenW - 12), Math.min(340, this.contentWidth())));
   }

   private int contentWidth() {
      int scaleKey = Math.round(autismclient.gui.sdf.MsdfTextRenderer.fontScale() * 100.0F);
      if (scaleKey != this.widthScaleKey) {
         int widest = k.init(this.font, "Simple Voice Chat detected", TITLE_FONT, -1);

         for (String[] section : SECTIONS) {
            widest = Math.max(widest, k.init(this.font, section[0], BODY_FONT, -1));

            for (int b = 1; b < section.length; b++) {
               widest = Math.max(widest, k.init(this.font, "- " + section[b], BODY_FONT, -1));
            }
         }

         this.widthScaleKey = scaleKey;
         this.cachedContentWidth = widest + 12;
      }

      return this.cachedContentWidth;
   }

   private int cardY(int cardH) {
      int screenH = cA.load_I();
      return Math.max(6, (screenH - cardH) / 2);
   }

   private int cardHeight() {
      int innerW = Math.max(1, this.cardWidth() - 12);
      int lineH = this.lineHeight();
      int h = 6 + k.init(TITLE_FONT) + 4 + 1 + 4;
      List<List<String>> linesBySection = this.wrapSections(innerW);

      for (int i = 0; i < SECTIONS.length; i++) {
         h += lineH + linesBySection.get(i).size() * lineH + 4;
      }

      return h + this.buttonHeight() + 6;
   }

   private void layoutButtons() {
      int screenW = cA.init_I();
      int screenH = cA.load_I();
      int scaleKey = Math.round(autismclient.gui.sdf.MsdfTextRenderer.fontScale() * 100.0F);
      if (this.layoutScreenW != screenW || this.layoutScreenH != screenH || this.layoutScaleKey != scaleKey) {
         this.layoutScreenW = screenW;
         this.layoutScreenH = screenH;
         this.layoutScaleKey = scaleKey;
         int cardW = this.cardWidth();
         int cardX = (screenW - cardW) / 2;
         int cardH = this.cardHeight();
         int cardY = this.cardY(cardH);
         int innerW = Math.max(1, cardW - 12);
         int btnH = this.buttonHeight();
         int btnW = Math.max(1, (innerW - 2) / 2);
         int btnY = cardY + cardH - 6 - btnH;
         int bx = cardX + 6;
         this.keepVanillaButton.setBounds(bx, btnY, btnW, btnH);
         this.switchModdedButton.setBounds(bx + btnW + 2, btnY, cardX + cardW - 6 - (bx + btnW + 2), btnH);
      }
   }

   private List<String> init(String text, int maxWidth) {
      List<String> lines = new ArrayList<>();
      StringBuilder current = new StringBuilder();

      for (String word : text.split(" ")) {
         String candidate = current.length() == 0 ? word : current + " " + word;
         if (current.length() != 0 && k.init(this.font, candidate, BODY_FONT, -1) > maxWidth) {
            lines.add(current.toString());
            current.setLength(0);
            current.append(word);
         } else {
            current.setLength(0);
            current.append(candidate);
         }
      }

      if (current.length() > 0) {
         lines.add(current.toString());
      }

      return lines;
   }

   private List<List<String>> wrapSections(int innerWidth) {
      if (this.wrappedWidth == innerWidth && this.wrappedSections.size() == SECTIONS.length) {
         return this.wrappedSections;
      }

      List<List<String>> next = new ArrayList<>(SECTIONS.length);

      for (String[] section : SECTIONS) {
         List<String> lines = new ArrayList<>();

         for (int b = 1; b < section.length; b++) {
            lines.addAll(this.init("- " + section[b], innerWidth));
         }

         next.add(List.copyOf(lines));
      }

      this.wrappedWidth = innerWidth;
      this.wrappedSections = List.copyOf(next);
      return this.wrappedSections;
   }

   private void drawCentered(GuiGraphicsExtractor graphics, String text, Identifier fontId, int color, int x, int width, int y) {
      int textW = k.init(this.font, text, fontId, color);
      k.init(graphics, this.font, text, fontId, color, x + (width - textW) / 2, y, false);
   }

   private void renderButton(GuiGraphicsExtractor graphics, SimpleVoiceChatDetectedScreen.Inner_DataUtil btn, float mouseX, float mouseY, boolean enabled) {
      boolean hovered = enabled && btn.contains(mouseX, mouseY);
      autismclient.gui.f.SyncManager ctx = autismclient.gui.f.UiHelper.init(graphics, this.font, (int)mouseX, (int)mouseY);
      autismclient.gui.f.NetHelper bounds = autismclient.gui.f.NetHelper.init(btn.x, btn.y, btn.width, btn.height);
      autismclient.gui.f.b.CachePool.init(ctx, bounds, btn.label, autismclient.gui.f.b.CachePool.Inner_DataUtil.load, hovered, false);
      if (!enabled) {
         autismclient.gui.f.PacketUtil.init(graphics, bounds, 1711276032);
      }
   }

   private static void init_V_GuiGraphicsExtract(GuiGraphicsExtractor graphics, int x, int y, int width, int height, int color, int t) {
      if (width > 0 && height > 0) {
         autismclient.gui.f.PacketUtil.init(graphics, autismclient.gui.f.NetHelper.init(x, y, width, t), color);
         autismclient.gui.f.PacketUtil.init(graphics, autismclient.gui.f.NetHelper.init(x, y + height - t, width, t), color);
         autismclient.gui.f.PacketUtil.init(graphics, autismclient.gui.f.NetHelper.init(x, y, t, height), color);
         autismclient.gui.f.PacketUtil.init(graphics, autismclient.gui.f.NetHelper.init(x + width - t, y, t, height), color);
      }
   }

   private static final class Inner_DataUtil {
      private String label;
      private final Runnable action;
      private int x;
      private int y;
      private int width;
      private int height;

      private Inner_DataUtil(String label, Runnable action) {
         this.label = label;
         this.action = action;
      }

      private void setBounds(int x, int y, int w, int h) {
         this.x = x;
         this.y = y;
         this.width = w;
         this.height = h;
      }

      private boolean contains(float mx, float my) {
         return mx >= this.x && my >= this.y && mx < this.x + this.width && my < this.y + this.height;
      }
   }
}

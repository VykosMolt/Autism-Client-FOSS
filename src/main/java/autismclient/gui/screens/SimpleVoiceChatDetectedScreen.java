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
   private static final Identifier init_Identifier = autismclient.gui.f.a.DataUtil.read;
   private static final Identifier load_Identifier = autismclient.gui.f.a.DataUtil.load;
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
   private static final String[][] init_StrArr2 = new String[][]{
      {"Voice chat won't fucking work right now", "Client fakes being vanilla so servers can't see it", "That hides the voicechat channel, so it can't connect"},
      {
            "Switch to Modded and here's what changes",
            "Voice chat starts working",
            "Servers can now tell you're on Fabric",
            "Kept: module hiding, channel filter, payload spoof",
            "Lost: the vanilla disguise. Not full protection anymore"
      }
   };
   private final Screen init_Screen;
   private final autismclient.gui.f.b.CharEncoder init_CharEncoder = new autismclient.gui.f.b.CharEncoder();
   private final long load_J = System.currentTimeMillis();
   private final SimpleVoiceChatDetectedScreen.Inner_DataUtil init_a_DataUtil = new SimpleVoiceChatDetectedScreen.Inner_DataUtil("Keep Vanilla", () -> this.init(true));
   private final SimpleVoiceChatDetectedScreen.Inner_DataUtil load_a_DataUtil = new SimpleVoiceChatDetectedScreen.Inner_DataUtil("Switch to Modded", () -> this.init(false));
   private int poll = -1;
   private int push = -1;
   private int pop = -1;
   private int tick = -1;
   private List<List<String>> init_List = List.of();
   private int run = -1;
   private int get;

   public SimpleVoiceChatDetectedScreen(Screen parent) {
      super(Component.literal("Simple Voice Chat detected"));
      this.init_Screen = parent;
   }

   private void init(boolean keepVanillaMode) {
      autismclient.modules.ModuleRuntimeManager module = autismclient.modules.ModuleRuntimeManager.init_PoolEntry();
      if (module != null) {
         module.update(keepVanillaMode);
      }

      this.minecraft.gui.setScreen(this.init_Screen);
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

   private boolean init_Z() {
      return System.currentTimeMillis() - this.load_J >= 5000L;
   }

   private int init_I() {
      long remaining = 5000L - (System.currentTimeMillis() - this.load_J);
      return (int)Math.max(1.0, Math.ceil(remaining / 1000.0));
   }

   @Override
   public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float delta) {
      float uiMouseX = (float)cA.init_D_D(mouseX);
      float uiMouseY = (float)cA.init_D_D(mouseY);
      this.load_V();
      cA.init_V_GuiGraphicsExtract(graphics);

      try {
         int screenW = cA.init_I();
         int screenH = cA.load_I();
         autismclient.gui.f.PacketUtil.init(graphics, autismclient.gui.f.NetHelper.init(0, 0, screenW, screenH), -939524096);
         int cardW = this.write();
         int cardX = (screenW - cardW) / 2;
         int cardH = this.sync();
         int cardY = this.init_I(cardH);
         int innerW = cardW - 12;
         int x = cardX + 6;
         autismclient.gui.f.PacketUtil.init(graphics, autismclient.gui.f.NetHelper.init(cardX, cardY, cardW, cardH), -234223092);
         init_V_GuiGraphicsExtract(graphics, cardX, cardY, cardW, cardH, ThemePaletteManager.init_I_I_ct_DataUtil(-46518, ThemePaletteManager.Inner_DataUtil.OUTLINE), 2);
         int y = cardY + 6;
         this.init_V_GuiGraphicsExtract_2(
            graphics, "Simple Voice Chat detected", init_Identifier, ThemePaletteManager.init_I_I_ct_DataUtil(-2828, ThemePaletteManager.Inner_DataUtil.TEXT), x, innerW, y
         );
         y += k.init(init_Identifier) + 4;
         autismclient.gui.f.PacketUtil.init(
            graphics, autismclient.gui.f.NetHelper.init(x, y, innerW, 1), ThemePaletteManager.init_I_I_ct_DataUtil(1728006730, ThemePaletteManager.Inner_DataUtil.OUTLINE)
         );
         y += 5;
         int lineH = this.load_I();
         int bodyColor = this.init_CharEncoder.load(l.write);
         List<List<String>> linesBySection = this.init_List(innerW);

         for (int i = 0; i < init_StrArr2.length; i++) {
            k.init(graphics, this.font, init_StrArr2[i][0], load_Identifier, ThemePaletteManager.init_I_I_ct_DataUtil(-45747, ThemePaletteManager.Inner_DataUtil.ACCENT), x, y, false);
            y += lineH;

            for (String line : linesBySection.get(i)) {
               k.init(graphics, this.font, line, load_Identifier, bodyColor, x, y, false);
               y += lineH;
            }

            y += 4;
         }

         boolean ready = this.init_Z();
         this.load_a_DataUtil.init_Str = ready ? "Switch to Modded" : "Switch to Modded (" + this.init_I() + ")";
         this.init(graphics, this.init_a_DataUtil, uiMouseX, uiMouseY, ready);
         this.init(graphics, this.load_a_DataUtil, uiMouseX, uiMouseY, ready);
      } finally {
         cA.load_V_GuiGraphicsExtract(graphics);
      }
   }

   @Override
   public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
      if (Compat263.mouseEventGlfwButton(event) == 0 && this.init_Z()) {
         float mx = (float)cA.init_D_D(event.x());
         float my = (float)cA.init_D_D(event.y());
         this.load_V();
         if (this.init_a_DataUtil.init(mx, my)) {
            this.init_a_DataUtil.init_Runnable.run();
            return true;
         } else if (this.load_a_DataUtil.init(mx, my)) {
            this.load_a_DataUtil.init_Runnable.run();
            return true;
         } else {
            return false;
         }
      } else {
         return false;
      }
   }

   private int load_I() {
      return k.init(load_Identifier) + 2;
   }

   private int read() {
      return autismclient.gui.ui.UiDensityMetrics.init(13);
   }

   private int write() {
      int screenW = cA.init_I();
      return Math.max(1, Math.min(Math.max(1, screenW - 12), Math.min(340, this.update())));
   }

   private int update() {
      int scaleKey = Math.round(autismclient.gui.sdf.MsdfTextRenderer.init_F() * 100.0F);
      if (scaleKey != this.run) {
         int widest = k.init(this.font, "Simple Voice Chat detected", init_Identifier, -1);

         for (String[] section : init_StrArr2) {
            widest = Math.max(widest, k.init(this.font, section[0], load_Identifier, -1));

            for (int b = 1; b < section.length; b++) {
               widest = Math.max(widest, k.init(this.font, "- " + section[b], load_Identifier, -1));
            }
         }

         this.run = scaleKey;
         this.get = widest + 12;
      }

      return this.get;
   }

   private int init_I(int cardH) {
      int screenH = cA.load_I();
      return Math.max(6, (screenH - cardH) / 2);
   }

   private int sync() {
      int innerW = Math.max(1, this.write() - 12);
      int lineH = this.load_I();
      int h = 6 + k.init(init_Identifier) + 4 + 1 + 4;
      List<List<String>> linesBySection = this.init_List(innerW);

      for (int i = 0; i < init_StrArr2.length; i++) {
         h += lineH + linesBySection.get(i).size() * lineH + 4;
      }

      return h + this.read() + 6;
   }

   private void load_V() {
      int screenW = cA.init_I();
      int screenH = cA.load_I();
      int scaleKey = Math.round(autismclient.gui.sdf.MsdfTextRenderer.init_F() * 100.0F);
      if (this.poll != screenW || this.push != screenH || this.pop != scaleKey) {
         this.poll = screenW;
         this.push = screenH;
         this.pop = scaleKey;
         int cardW = this.write();
         int cardX = (screenW - cardW) / 2;
         int cardH = this.sync();
         int cardY = this.init_I(cardH);
         int innerW = Math.max(1, cardW - 12);
         int btnH = this.read();
         int btnW = Math.max(1, (innerW - 2) / 2);
         int btnY = cardY + cardH - 6 - btnH;
         int bx = cardX + 6;
         this.init_a_DataUtil.init(bx, btnY, btnW, btnH);
         this.load_a_DataUtil.init(bx + btnW + 2, btnY, cardX + cardW - 6 - (bx + btnW + 2), btnH);
      }
   }

   private List<String> init(String text, int maxWidth) {
      List<String> lines = new ArrayList<>();
      StringBuilder current = new StringBuilder();

      for (String word : text.split(" ")) {
         String candidate = current.length() == 0 ? word : current + " " + word;
         if (current.length() != 0 && k.init(this.font, candidate, load_Identifier, -1) > maxWidth) {
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

   private List<List<String>> init_List(int innerWidth) {
      if (this.tick == innerWidth && this.init_List.size() == init_StrArr2.length) {
         return this.init_List;
      }

      List<List<String>> next = new ArrayList<>(init_StrArr2.length);

      for (String[] section : init_StrArr2) {
         List<String> lines = new ArrayList<>();

         for (int b = 1; b < section.length; b++) {
            lines.addAll(this.init("- " + section[b], innerWidth));
         }

         next.add(List.copyOf(lines));
      }

      this.tick = innerWidth;
      this.init_List = List.copyOf(next);
      return this.init_List;
   }

   private void init_V_GuiGraphicsExtract_2(GuiGraphicsExtractor graphics, String text, Identifier fontId, int color, int x, int width, int y) {
      int textW = k.init(this.font, text, fontId, color);
      k.init(graphics, this.font, text, fontId, color, x + (width - textW) / 2, y, false);
   }

   private void init(GuiGraphicsExtractor graphics, SimpleVoiceChatDetectedScreen.Inner_DataUtil btn, float mouseX, float mouseY, boolean enabled) {
      boolean hovered = enabled && btn.init(mouseX, mouseY);
      autismclient.gui.f.SyncManager ctx = autismclient.gui.f.UiHelper.init(graphics, this.font, (int)mouseX, (int)mouseY);
      autismclient.gui.f.NetHelper bounds = autismclient.gui.f.NetHelper.init(btn.init_I, btn.load, btn.read, btn.write);
      autismclient.gui.f.b.CachePool.init(ctx, bounds, btn.init_Str, autismclient.gui.f.b.CachePool.Inner_DataUtil.load, hovered, false);
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
      private String init_Str;
      private final Runnable init_Runnable;
      private int init_I;
      private int load;
      private int read;
      private int write;

      private Inner_DataUtil(String label, Runnable action) {
         this.init_Str = label;
         this.init_Runnable = action;
      }

      private void init(int x, int y, int w, int h) {
         this.init_I = x;
         this.load = y;
         this.read = w;
         this.write = h;
      }

      private boolean init(float mx, float my) {
         return mx >= this.init_I && my >= this.load && mx < this.init_I + this.read && my < this.load + this.write;
      }
   }
}

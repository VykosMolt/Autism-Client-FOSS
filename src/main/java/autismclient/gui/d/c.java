package autismclient.gui.d;

import autismclient.compat263.Compat263;
import autismclient.gui.f.b.j;
import autismclient.gui.f.b.k;
import autismclient.gui.f.b.l;
import autismclient.util.aJ;
import autismclient.util.cA;
import autismclient.util.ct;
import autismclient.util.cu;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;

public class c extends Screen implements TaskQueue {
   private static final Identifier init_Identifier = autismclient.gui.f.a.DataUtil.read;
   private static final Identifier load_Identifier = autismclient.gui.f.a.DataUtil.load;
   private static final Identifier read_Identifier = autismclient.gui.f.a.DataUtil.init;
   private static final long init_J = 7000L;
   private static final int init_I = -2828;
   private static final int load_I = -45747;
   private static final int read_I = -234223092;
   private static final int write_I = -46518;
   private static final int update_I = 1728006730;
   private static final Identifier write_Identifier = Identifier.fromNamespaceAndPath("autismclient", "textures/gui/hud/autismclient_welcome.png");
   private static final int sync = 878;
   private static final int flush = 83;
   private static final int close = 878;
   private static final int open = 83;
   private static final Identifier update_Identifier = Identifier.fromNamespaceAndPath("autismclient", "textures/gui/title/icons/donate.png");
   private static final int poll = 32;
   private static final int push = 32;
   private static final int pop = 16;
   private static final String init_Str = "Thanks for using";
   private static final String[][] init_StrArr2 = new String[][]{
      {"Donate", "If AUTISM Client has helped you or made Minecraft more fun, a donation is a kind way to support the time behind it."},
      {"Choose Method", "Use Card or PayPal for the normal route, or Crypto if you want the direct one."}
   };
   private static final int tick = 8;
   private static final int run = 340;
   private static final int get = 16;
   private static final int set = 4;
   private static final int put = 5;
   private static final int add = 6;
   private static final int clear = 6;
   private static final int reset = 6;
   private final autismclient.gui.f.b.CharEncoder init_CharEncoder = new autismclient.gui.f.b.CharEncoder();
   private final long load_J = System.currentTimeMillis();
   private final c.Inner_DataUtil init_c_DataUtil = new c.Inner_DataUtil(
      "crypto", "Crypto", update_Identifier, 32, 32, () -> aJ.init_V("https://nowpayments.io/donation/melonikautismclient")
   );
   private final c.Inner_DataUtil load_c_DataUtil = new c.Inner_DataUtil(
      "donate", "Card/PayPal", update_Identifier, 32, 32, () -> aJ.init_V("https://ko-fi.com/melonik")
   );
   private final c.Inner_DataUtil read_c_DataUtil = new c.Inner_DataUtil("continue", "Continue", () -> this.minecraft.gui.setScreen(new TitleScreen()));
   private int start = -1;
   private int stop = -1;
   private int next = -1;
   private List<List<String>> init_List = List.of();

   public c() {
      super(Component.literal("Thanks for using"));
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
      return System.currentTimeMillis() - this.load_J >= 7000L;
   }

   private int init_I_2() {
      long remaining = 7000L - (System.currentTimeMillis() - this.load_J);
      return (int)Math.max(1.0, Math.ceil(remaining / 1000.0));
   }

   @Override
   public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float delta) {
      this.minecraft.gameRenderer.panorama().extractRenderState(graphics, this.width, this.height);
      float uiMouseX = (float)cA.init_D_D(mouseX);
      float uiMouseY = (float)cA.init_D_D(mouseY);
      this.load_V_2();
      cA.init_V_GuiGraphicsExtract(graphics);

      try {
         int screenW = cA.init_I();
         int screenH = cA.load_I();
         autismclient.gui.f.PacketUtil.init(graphics, autismclient.gui.f.NetHelper.init(0, 0, screenW, screenH), -939524096);
         int cardW = this.load_I();
         int cardX = (screenW - cardW) / 2;
         int cardH = this.read();
         int cardY = Math.max(6, (screenH - cardH) / 2);
         int innerW = cardW - 16;
         int x = cardX + 8;
         autismclient.gui.f.PacketUtil.init(graphics, autismclient.gui.f.NetHelper.init(cardX, cardY, cardW, cardH), -234223092);
         init_V_GuiGraphicsExtract(graphics, cardX, cardY, cardW, cardH, ct.init_I_I_ct_DataUtil(-46518, ct.Inner_DataUtil.load), 2);
         int y = cardY + 8;
         this.init_V_GuiGraphicsExtract_2(graphics, "Thanks for using", init_Identifier, ct.init_I_I_ct_DataUtil(-2828, ct.Inner_DataUtil.read), x, innerW, y);
         y += k.init(init_Identifier) + 5;
         int logoW = init_I_I(innerW);
         int logoH = load(logoW);
         int logoX = x + (innerW - logoW) / 2;
         graphics.blit(RenderPipelines.GUI_TEXTURED, cu.init(write_Identifier, ct.Inner_DataUtil.init), logoX, y, 0.0F, 0.0F, logoW, logoH, 878, 83, 878, 83);
         y += logoH + 6;
         autismclient.gui.f.PacketUtil.init(
            graphics, autismclient.gui.f.NetHelper.init(x, y, innerW, 1), ct.init_I_I_ct_DataUtil(1728006730, ct.Inner_DataUtil.load)
         );
         y += 7;
         int headerH = k.init(load_Identifier);
         int lineH = k.init(load_Identifier) + 3;
         int bodyColor = this.init_CharEncoder.load(l.write);
         List<List<String>> linesBySection = this.init_List_I(innerW);

         for (int i = 0; i < init_StrArr2.length; i++) {
            String[] section = init_StrArr2[i];
            k.init(graphics, this.font, section[0], load_Identifier, ct.init_I_I_ct_DataUtil(-45747, ct.Inner_DataUtil.init), x, y, false);
            y += headerH + 3;

            for (String line : linesBySection.get(i)) {
               k.init(graphics, this.font, line, load_Identifier, bodyColor, x, y, false);
               y += lineH;
            }

            y += 6;
         }

         this.init(graphics, this.load_c_DataUtil, uiMouseX, uiMouseY, true);
         this.init(graphics, this.init_c_DataUtil, uiMouseX, uiMouseY, true);
         boolean unlocked = this.init_Z();
         this.read_c_DataUtil.load_Str = unlocked ? "Continue" : "Continue (" + this.init_I_2() + ")";
         this.init(graphics, this.read_c_DataUtil, uiMouseX, uiMouseY, unlocked);
      } finally {
         cA.load_V_GuiGraphicsExtract(graphics);
      }
   }

   @Override
   public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
      if (Compat263.mouseEventGlfwButton(event) != 0) {
         return false;
      } else {
         float mx = (float)cA.init_D_D(event.x());
         float my = (float)cA.init_D_D(event.y());
         this.load_V_2();
         if (this.load_c_DataUtil.init(mx, my)) {
            this.init_V_c_DataUtil(this.load_c_DataUtil, mx, my);
            return true;
         } else if (this.init_c_DataUtil.init(mx, my)) {
            this.init_V_c_DataUtil(this.init_c_DataUtil, mx, my);
            return true;
         } else if (this.init_Z() && this.read_c_DataUtil.init(mx, my)) {
            this.init_V_c_DataUtil(this.read_c_DataUtil, mx, my);
            return true;
         } else {
            return false;
         }
      }
   }

   private void init_V_c_DataUtil(c.Inner_DataUtil btn, float mx, float my) {
      btn.init_Runnable.run();
   }

   @Override
   public Map<String, autismclient.gui.f.NetHelper> init_Map() {
      this.load_V_2();
      double sx = cA.init_I() == 0 ? 1.0 : (double)this.width / cA.init_I();
      double sy = cA.load_I() == 0 ? 1.0 : (double)this.height / cA.load_I();
      Map<String, autismclient.gui.f.NetHelper> out = new LinkedHashMap<>();
      out.put(this.load_c_DataUtil.load_Str, init_NetHelper_c_DataUtil(this.load_c_DataUtil, sx, sy));
      out.put(this.init_c_DataUtil.load_Str, init_NetHelper_c_DataUtil(this.init_c_DataUtil, sx, sy));
      out.put(this.read_c_DataUtil.load_Str, init_NetHelper_c_DataUtil(this.read_c_DataUtil, sx, sy));
      return out;
   }

   private static autismclient.gui.f.NetHelper init_NetHelper_c_DataUtil(c.Inner_DataUtil btn, double sx, double sy) {
      return autismclient.gui.f.NetHelper.init(
         (int)Math.round(btn.read * sx), (int)Math.round(btn.write * sy), (int)Math.round(btn.update * sx), (int)Math.round(btn.sync * sy)
      );
   }

   @Override
   public void removed() {
   }

   private int load_I() {
      int screenW = cA.init_I();
      return Math.max(1, Math.min(Math.max(1, screenW - 12), 340));
   }

   private int read() {
      int innerW = Math.max(1, this.load_I() - 16);
      int headerH = k.init(load_Identifier);
      int lineH = k.init(load_Identifier) + 3;
      int logoW = init_I_I(innerW);
      int h = 8 + k.init(init_Identifier) + 5 + load(logoW) + 6 + 1 + 6;
      List<List<String>> linesBySection = this.init_List_I(innerW);

      for (int i = 0; i < init_StrArr2.length; i++) {
         h += headerH + 3 + linesBySection.get(i).size() * lineH + 6;
      }

      return h + 24;
   }

   private void load_V_2() {
      int screenW = cA.init_I();
      int screenH = cA.load_I();
      if (this.start != screenW || this.stop != screenH) {
         this.start = screenW;
         this.stop = screenH;
         int cardW = this.load_I();
         int cardX = (screenW - cardW) / 2;
         int cardH = this.read();
         int cardY = Math.max(6, (screenH - cardH) / 2);
         int innerW = Math.max(1, cardW - 16);
         int btnW = Math.max(1, (innerW - 8) / 3);
         int btnY = cardY + cardH - 8 - 16;
         int bx = cardX + 8;
         this.load_c_DataUtil.init(bx, btnY, btnW, 16);
         this.init_c_DataUtil.init(bx + btnW + 4, btnY, btnW, 16);
         int lastX = bx + (btnW + 4) * 2;
         this.read_c_DataUtil.init(lastX, btnY, cardX + cardW - 8 - lastX, 16);
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

   private List<List<String>> init_List_I(int innerWidth) {
      if (this.next == innerWidth && this.init_List.size() == init_StrArr2.length) {
         return this.init_List;
      }

      List<List<String>> next = new ArrayList<>(init_StrArr2.length);

      for (String[] section : init_StrArr2) {
         next.add(List.copyOf(this.init(section[1], innerWidth)));
      }

      this.next = innerWidth;
      this.init_List = List.copyOf(next);
      return this.init_List;
   }

   private void init_V_GuiGraphicsExtract_2(GuiGraphicsExtractor graphics, String text, Identifier fontId, int color, int x, int width, int y) {
      int textW = k.init(this.font, text, fontId, color);
      k.init(graphics, this.font, text, fontId, color, x + (width - textW) / 2, y, false);
   }

   private static int init_I_I(int innerW) {
      return Math.max(170, Math.min(innerW - 18, 286));
   }

   private static int load(int width) {
      return Math.max(1, Math.round(width * 0.094533026F));
   }

   private void init(GuiGraphicsExtractor graphics, c.Inner_DataUtil btn, float mouseX, float mouseY, boolean enabled) {
      boolean hovered = enabled && btn.init(mouseX, mouseY);
      autismclient.gui.f.SyncManager ctx = autismclient.gui.f.UiHelper.init(graphics, this.font, (int)mouseX, (int)mouseY);
      autismclient.gui.f.NetHelper bounds = autismclient.gui.f.NetHelper.init(btn.read, btn.write, btn.update, btn.sync);
      if (btn.init_Identifier != null) {
         autismclient.gui.f.b.CachePool.init(
            ctx, bounds, btn.load_Str, btn.init_Identifier, autismclient.gui.f.b.CachePool.Inner_DataUtil.load, hovered, false, 0.0F
         );
      } else {
         autismclient.gui.f.b.CachePool.init(ctx, bounds, btn.load_Str, autismclient.gui.f.b.CachePool.Inner_DataUtil.load, hovered, false);
      }

      if (!enabled) {
         autismclient.gui.f.PacketUtil.init(graphics, bounds, 1711276032);
      }
   }

   private void init_V_GuiGraphicsExtract_2(GuiGraphicsExtractor graphics, c.Inner_DataUtil btn, int textColor) {
      int textAreaX = btn.read + 3;
      int textAreaW = Math.max(1, btn.update - 6);
      if (btn.init_Identifier != null) {
         int iconSize = Math.min(16, Math.max(1, Math.min(btn.update - 4, btn.sync - 4)));
         textAreaX = btn.read + 4 + iconSize + 7;
         textAreaW = Math.max(1, btn.read + btn.update - 5 - textAreaX);
      }

      String label = k.init(this.font, btn.load_Str, textAreaW, load_Identifier, textColor);
      int textW = k.init(this.font, label, load_Identifier, textColor);
      int textX = btn.init_Identifier != null ? textAreaX : textAreaX + (textAreaW - textW + 1) / 2;
      if ("crypto".equals(btn.init_Str)) {
         int centeredX = btn.read + (btn.update - textW + 1) / 2;
         textX = Math.max(textAreaX, centeredX);
      }

      int textY = j.init(btn.write, btn.sync, k.init(load_Identifier), this.init_CharEncoder.close());
      k.init(graphics, this.font, label, load_Identifier, textColor, textX, textY, false);
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
      private final String init_Str;
      private String load_Str;
      private final Identifier init_Identifier;
      private final int init_I;
      private final int load_I;
      private final Runnable init_Runnable;
      private int read;
      private int write;
      private int update;
      private int sync;

      private Inner_DataUtil(String id, String label, Runnable action) {
         this(id, label, null, 1, 1, action);
      }

      private Inner_DataUtil(String id, String label, Identifier icon, int iconTextureWidth, int iconTextureHeight, Runnable action) {
         this.init_Str = id;
         this.load_Str = label;
         this.init_Identifier = icon;
         this.init_I = Math.max(1, iconTextureWidth);
         this.load_I = Math.max(1, iconTextureHeight);
         this.init_Runnable = action;
      }

      private void init(int x, int y, int w, int h) {
         this.read = x;
         this.write = y;
         this.update = w;
         this.sync = h;
      }

      private boolean init(float mx, float my) {
         return mx >= this.read && my >= this.write && mx < this.read + this.update && my < this.write + this.sync;
      }
   }
}

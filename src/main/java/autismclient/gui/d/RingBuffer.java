package autismclient.gui.d;

import autismclient.compat263.Compat263;
import autismclient.gui.f.b.k;
import autismclient.gui.f.b.l;
import autismclient.util.AutismConfig;
import autismclient.util.AutismLiteVariant;
import autismclient.util.cA;
import autismclient.util.ct;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;

public class RingBuffer extends Screen {
   private static final Identifier init_Identifier = autismclient.gui.f.a.DataUtil.read;
   private static final Identifier load_Identifier = autismclient.gui.f.a.DataUtil.load;
   private static final long init_J = 5000L;
   private static final int init_I = -2828;
   private static final int load_I = -234223092;
   private static final int read = -46518;
   private static final int write = 1728006730;
   private static final int update = 8;
   private static final int sync = 340;
   private static final int flush = 2;
   private static final String init_Str = "[DISCLAIMER]";
   private static final String[] init_StrArr = new String[]{
      "AUTISM just provides the open-source software that lets you run more than one Minecraft account. That's all it does.",
      "What you choose to do with it is not our concern, and not our responsibility.",
      "By clicking Agree, you take full responsibility for whatever you use this feature for."
   };
   private final Screen init_Screen;
   private final Runnable init_Runnable;
   private final autismclient.gui.f.b.CharEncoder init_CharEncoder = new autismclient.gui.f.b.CharEncoder();
   private final long load_J = System.currentTimeMillis();
   private final RingBuffer.Inner_DataUtil init_RingBuffer_DataUti = new RingBuffer.Inner_DataUtil("Back", this::load_V);
   private final RingBuffer.Inner_DataUtil load_RingBuffer_DataUti = new RingBuffer.Inner_DataUtil("I Agree", this::read_V);
   private int close = -1;
   private int open = -1;
   private int poll = -1;
   private List<List<String>> init_List = List.of();

   public static void init(Minecraft minecraft, Screen parent, Runnable proceed) {
      if (!AutismLiteVariant.enabled() && minecraft != null && proceed != null) {
         AutismConfig config = AutismConfig.getGlobal();
         if (config != null && !config.multiDisclaimerAccepted) {
            minecraft.gui.setScreen(new RingBuffer(parent, proceed));
         } else {
            proceed.run();
         }
      }
   }

   public RingBuffer(Screen parent, Runnable onAgree) {
      super(Component.literal("[DISCLAIMER]"));
      this.init_Screen = parent;
      this.init_Runnable = onAgree;
   }

   private void load_V() {
      this.minecraft.gui.setScreen(this.init_Screen);
   }

   private void read_V() {
      AutismConfig config = AutismConfig.getGlobal();
      if (config != null) {
         config.multiDisclaimerAccepted = true;
         config.save();
      }

      if (this.minecraft != null) {
         this.minecraft.gui.setScreen(this.init_Screen);
      }

      if (this.init_Runnable != null) {
         this.init_Runnable.run();
      }
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
      this.write_V();
      cA.init_V_GuiGraphicsExtract(graphics);

      try {
         int screenW = cA.init_I();
         int screenH = cA.load_I();
         autismclient.gui.f.PacketUtil.init(graphics, autismclient.gui.f.NetHelper.init(0, 0, screenW, screenH), -939524096);
         int cardW = this.load_I();
         int cardX = (screenW - cardW) / 2;
         int cardH = this.read_I();
         int cardY = Math.max(6, (screenH - cardH) / 2);
         int innerW = cardW - 16;
         int x = cardX + 8;
         autismclient.gui.f.PacketUtil.init(graphics, autismclient.gui.f.NetHelper.init(cardX, cardY, cardW, cardH), -234223092);
         init_V_GuiGraphicsExtract(graphics, cardX, cardY, cardW, cardH, ct.init_I_I_ct_DataUtil(-46518, ct.Inner_DataUtil.load), 2);
         int y = cardY + 8;
         this.init_V_GuiGraphicsExtract_2(graphics, "[DISCLAIMER]", init_Identifier, ct.init_I_I_ct_DataUtil(-2828, ct.Inner_DataUtil.read), x, innerW, y);
         y += k.init(init_Identifier) + 4;
         autismclient.gui.f.PacketUtil.init(
            graphics, autismclient.gui.f.NetHelper.init(x, y, innerW, 1), ct.init_I_I_ct_DataUtil(1728006730, ct.Inner_DataUtil.load)
         );
         y += 5;
         int lineH = write_I();
         int bodyColor = this.init_CharEncoder.load(l.write);

         for (List<String> paragraph : this.init(innerW)) {
            for (String line : paragraph) {
               k.init(graphics, this.font, line, load_Identifier, bodyColor, x, y, false);
               y += lineH;
            }

            y += 4;
         }

         boolean ready = this.init_Z();
         this.load_RingBuffer_DataUti.init_Str = ready ? "I Agree" : "I Agree (" + this.init_I() + ")";
         this.init(graphics, this.init_RingBuffer_DataUti, uiMouseX, uiMouseY, true);
         this.init(graphics, this.load_RingBuffer_DataUti, uiMouseX, uiMouseY, ready);
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
         this.write_V();
         if (this.init_RingBuffer_DataUti.init(mx, my)) {
            this.init_RingBuffer_DataUti.init_Runnable.run();
            return true;
         } else if (this.init_Z() && this.load_RingBuffer_DataUti.init(mx, my)) {
            this.load_RingBuffer_DataUti.init_Runnable.run();
            return true;
         } else {
            return false;
         }
      }
   }

   private int load_I() {
      int screenW = cA.init_I();
      return Math.max(1, Math.min(Math.max(1, screenW - 12), 340));
   }

   private int read_I() {
      int innerW = Math.max(1, this.load_I() - 16);
      int lineH = write_I();
      int h = 8 + k.init(init_Identifier) + 4 + 1 + 4;

      for (List<String> paragraph : this.init(innerW)) {
         h += paragraph.size() * lineH + 4;
      }

      return h + update() + 8;
   }

   private static int write_I() {
      return Math.max(k.init(load_Identifier) + 3, autismclient.gui.f.RenderState.init(12));
   }

   private static int update() {
      return autismclient.gui.f.RenderState.init(13);
   }

   private void write_V() {
      int screenW = cA.init_I();
      int screenH = cA.load_I();
      if (this.close != screenW || this.open != screenH) {
         this.close = screenW;
         this.open = screenH;
         int cardW = this.load_I();
         int cardX = (screenW - cardW) / 2;
         int cardH = this.read_I();
         int cardY = Math.max(6, (screenH - cardH) / 2);
         int innerW = Math.max(1, cardW - 16);
         int btnW = Math.max(1, (innerW - 2) / 2);
         int btnH = update();
         int btnY = cardY + cardH - 8 - btnH;
         int bx = cardX + 8;
         this.init_RingBuffer_DataUti.init(bx, btnY, btnW, btnH);
         this.load_RingBuffer_DataUti.init(bx + btnW + 2, btnY, cardX + cardW - 8 - (bx + btnW + 2), btnH);
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

   private List<List<String>> init(int innerWidth) {
      if (this.poll == innerWidth && this.init_List.size() == init_StrArr.length) {
         return this.init_List;
      }

      List<List<String>> next = new ArrayList<>(init_StrArr.length);

      for (String paragraph : init_StrArr) {
         next.add(List.copyOf(this.init(paragraph, innerWidth)));
      }

      this.poll = innerWidth;
      this.init_List = List.copyOf(next);
      return this.init_List;
   }

   private void init_V_GuiGraphicsExtract_2(GuiGraphicsExtractor graphics, String text, Identifier fontId, int color, int x, int width, int y) {
      int textW = k.init(this.font, text, fontId, color);
      k.init(graphics, this.font, text, fontId, color, x + (width - textW) / 2, y, false);
   }

   private void init(GuiGraphicsExtractor graphics, RingBuffer.Inner_DataUtil btn, float mouseX, float mouseY, boolean enabled) {
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

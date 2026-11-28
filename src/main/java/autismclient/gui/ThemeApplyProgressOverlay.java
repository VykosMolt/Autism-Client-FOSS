package autismclient.gui;

import autismclient.gui.f.NetHelper;
import autismclient.gui.f.PacketUtil;
import autismclient.gui.ui.UiTextRenderer;
import autismclient.gui.f.SyncManager;
import autismclient.gui.f.UiHelper;
import autismclient.gui.f.b.BufferHelper;
import autismclient.gui.ui.widgets.ProgressBarRenderer;
import autismclient.modules.aD;
import autismclient.util.cA;
import java.util.concurrent.atomic.AtomicBoolean;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Overlay;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.util.Mth;
import net.minecraft.util.Util;

public final class ThemeApplyProgressOverlay extends Overlay {
   private static final Object LOCK = new Object();
   private static final long init_J = 300L;
   private static final long load_J = 10000L;
   private static int totalSteps;
   private static int doneSteps;
   private static String currentTitle = "Applying Theme";
   private static String currentLabel = "";
   private static boolean showing;
   private static Runnable finishAction;
   private final UiTextRenderer text = new UiTextRenderer(Minecraft.getInstance().font);
   private long shownAt = -1L;
   private float progress;
   private int read_I;

   private ThemeApplyProgressOverlay() {
   }

   public static Runnable init(String label) {
      return init(null, label);
   }

   public static Runnable init(String title, String label) {
      synchronized (LOCK) {
         if (doneSteps >= totalSteps) {
            totalSteps = 0;
            doneSteps = 0;
            currentTitle = title != null && !title.isBlank() ? title : "Applying Theme";
         }

         totalSteps++;
         currentLabel = label == null ? "" : label;
      }

      Minecraft mc = Minecraft.getInstance();
      mc.execute(() -> {
         if (!showing && mc.gui.overlay() == null && !aD.init_Z()) {
            showing = true;
            mc.gui.setOverlay(new ThemeApplyProgressOverlay());
         }
      });
      AtomicBoolean completed = new AtomicBoolean();
      return () -> {
         if (completed.compareAndSet(false, true)) {
            synchronized (LOCK) {
               doneSteps = Math.min(totalSteps, doneSteps + 1);
            }
         }
      };
   }

   public static void init(Runnable action) {
      if (aD.init_Z()) {
         Minecraft.getInstance().execute(action);
      } else {
         synchronized (LOCK) {
            finishAction = action;
         }
      }
   }

   @Override
   public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float delta) {
      Minecraft mc = Minecraft.getInstance();
      if (aD.init_Z()) {
         showing = false;
         if (mc.gui.overlay() == this) {
            mc.gui.setOverlay(null);
         }

         init();
      } else {
         long now = Util.getMillis();
         if (this.shownAt < 0L) {
            this.shownAt = now;
         }

         Screen screen = mc.gui.screen();
         if (screen != null) {
            try {
               screen.extractRenderStateWithTooltipAndSubtitles(graphics, mouseX, mouseY, delta);
            } catch (Exception var14) {
            }
         }

         cA.init_V_GuiGraphicsExtract(graphics);

         try {
            float scale = cA.init_F();
            this.init(graphics, mc, now, Math.round(mouseX / scale), Math.round(mouseY / scale));
         } finally {
            cA.load_V_GuiGraphicsExtract(graphics);
         }

         this.read_I++;
         if (this.read_I >= 2) {
            init();
         }
      }
   }

   private void init(GuiGraphicsExtractor graphics, Minecraft mc, long now, int mouseX, int mouseY) {
      int width = cA.init_I();
      int height = cA.load_I();
      graphics.nextStratum();
      PacketUtil.init(graphics, NetHelper.init(0, 0, width, height), -1476395008);
      float target;
      String title;
      String label;
      boolean finished;
      synchronized (LOCK) {
         target = totalSteps == 0 ? 1.0F : (float)doneSteps / totalSteps;
         title = currentTitle;
         label = currentLabel;
         finished = doneSteps >= totalSteps;
      }

      this.progress = Mth.clamp(this.progress + (target - this.progress) * (finished ? 0.35F : 0.15F), 0.0F, 1.0F);
      if (finished && this.progress > 0.98F) {
         this.progress = 1.0F;
      }

      int pad = 6;
      int lineH = 12;
      int barH = 8;
      int panelW = Math.max(1, Math.min(260, width - 12));
      int panelH = pad + lineH * 2 + 4 + barH + pad;
      int px = (width - panelW) / 2;
      int py = (height - panelH) / 2;
      SyncManager context = UiHelper.init(graphics, mc.font, mouseX, mouseY);
      int textColor = context.init_TableIndex().colors().textPrimary;
      BufferHelper.init(context, NetHelper.init(px, py, panelW, panelH));
      this.text.drawCentered(graphics, title, NetHelper.init(px, py + pad, panelW, lineH), textColor);
      if (!label.isBlank()) {
         this.text.drawCentered(graphics, label, NetHelper.init(px, py + pad + lineH, panelW, lineH), context.init_TableIndex().colors().textSecondary);
      }

      int barX = px + pad * 2;
      int barW = Math.max(1, panelW - pad * 4);
      int barY = py + panelH - pad - barH;
      ProgressBarRenderer.draw(UiHelper.init(graphics, mc.font, mouseX, mouseY), NetHelper.init(barX, barY, barW, barH), this.progress);
      boolean minElapsed = now - this.shownAt >= 300L;
      boolean timedOut = now - this.shownAt >= 10000L;
      if (finished && minElapsed && this.progress >= 1.0F || timedOut) {
         showing = false;
         if (mc.gui.overlay() == this) {
            mc.gui.setOverlay(null);
         }
      }
   }

   private static void init() {
      Runnable action;
      synchronized (LOCK) {
         action = finishAction;
         finishAction = null;
      }

      if (action != null) {
         try {
            action.run();
         } catch (Throwable var3) {
         }
      }
   }

   @Override
   public boolean isPausing() {
      return false;
   }
}

package autismclient.gui;

import autismclient.gui.f.NetHelper;
import autismclient.gui.f.PacketUtil;
import autismclient.modules.aD;
import autismclient.util.AutismLiteVariant;
import autismclient.util.ThemePaletteManager;
import autismclient.util.ThemeTextureRecolorer;
import autismclient.util.UiColorPalette;
import com.mojang.blaze3d.platform.Window;
import java.util.Optional;
import java.util.function.Consumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.LoadingOverlay;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.ReloadInstance;
import net.minecraft.util.ARGB;
import net.minecraft.util.Mth;
import net.minecraft.util.Util;
import org.joml.Vector4f;

public class AutismLoadingOverlay extends LoadingOverlay {
   private static final Identifier init_Identifier = Identifier.fromNamespaceAndPath("autismclient", "textures/gui/title/loading_logo.png");
   private static final int init_I = 2106;
   private static final int load_I = 1297;
   private static final int read_I = UiColorPalette.e();
   private static final int write_I = 236;
   private static final int update = 32;
   private static final int sync = 39;
   private static final long init_J = 1000L;
   private static final long load_J = 500L;
   private final Minecraft init_Minecraft;
   private final ReloadInstance init_ReloadInstance;
   private final Consumer<Optional<Throwable>> init_Consumer;
   private final boolean init_Z;
   private float init_F;
   private long read_J = -1L;
   private long write_J = -1L;

   public static LoadingOverlay init_LoadingOverlay_Minecraft(
      Minecraft minecraft, ReloadInstance reload, Consumer<Optional<Throwable>> onFinish, boolean fadeIn
   ) {
      return !aD.init_Z() && !AutismLiteVariant.enabled()
         ? new AutismLoadingOverlay(minecraft, reload, onFinish, fadeIn)
         : new LoadingOverlay(minecraft, reload, onFinish, fadeIn);
   }

   public AutismLoadingOverlay(Minecraft minecraft, ReloadInstance reload, Consumer<Optional<Throwable>> onFinish, boolean fadeIn) {
      super(minecraft, reload, onFinish, fadeIn);
      this.init_Minecraft = minecraft;
      this.init_ReloadInstance = reload;
      this.init_Consumer = onFinish;
      this.init_Z = fadeIn;
   }

   private boolean init() {
      if (!aD.init_Z()) {
         return false;
      }

      if (this.read_J == -1L) {
         this.init_Minecraft.gui.setOverlay(new LoadingOverlay(this.init_Minecraft, this.init_ReloadInstance, this.init_Consumer, false));
      } else {
         this.init_Minecraft.gui.setOverlay(null);
      }

      return true;
   }

   @Override
   public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float a) {
      if (!this.init()) {
         int width = graphics.guiWidth();
         int height = graphics.guiHeight();
         long now = Util.getMillis();
         if (this.init_Z && this.write_J == -1L) {
            this.write_J = now;
         }

         float fadeOutAnim = this.read_J > -1L ? (float)(now - this.read_J) / 1000.0F : -1.0F;
         float fadeInAnim = this.write_J > -1L ? (float)(now - this.write_J) / 500.0F : -1.0F;
         float logoAlpha;
         if (fadeOutAnim >= 1.0F) {
            if (this.init_Minecraft.gui.screen() != null) {
               this.init_V_GuiGraphicsExtract(graphics, 0, 0, a);
            } else {
               this.init_Minecraft.gui.hud.extractDeferredSubtitles();
            }

            int alpha = Mth.ceil((1.0F - Mth.clamp(fadeOutAnim - 1.0F, 0.0F, 1.0F)) * 255.0F);
            graphics.nextStratum();
            PacketUtil.init(graphics, NetHelper.init(0, 0, width, height), init(read_I, alpha));
            logoAlpha = 1.0F - Mth.clamp(fadeOutAnim - 1.0F, 0.0F, 1.0F);
         } else if (!this.init_Z) {
            this.init_Minecraft.gameRenderer.gameRenderState().guiRenderState.clearColorOverride = init(read_I);
            logoAlpha = 1.0F;
         } else {
            if (this.init_Minecraft.gui.screen() != null && fadeInAnim < 1.0F) {
               this.init_V_GuiGraphicsExtract(graphics, mouseX, mouseY, a);
            } else {
               this.init_Minecraft.gui.hud.extractDeferredSubtitles();
            }

            int alpha = Mth.ceil(Mth.clamp(fadeInAnim, 0.15, 1.0) * 255.0);
            graphics.nextStratum();
            PacketUtil.init(graphics, NetHelper.init(0, 0, width, height), init(read_I, alpha));
            logoAlpha = Mth.clamp(fadeInAnim, 0.0F, 1.0F);
         }

         if (logoAlpha > 0.0F) {
            this.load(graphics, width, height, logoAlpha);
         }

         float actualProgress = this.init_ReloadInstance.getActualProgress();
         this.init_F = Mth.clamp(this.init_F * 0.95F + actualProgress * 0.050000012F, 0.0F, 1.0F);
         if (fadeOutAnim < 1.0F) {
            this.read(graphics, width, height, fadeOutAnim);
         }

         if (fadeOutAnim >= 2.0F) {
            this.init_Minecraft.gui.setOverlay(null);
         }
      }
   }

   private void init_V_GuiGraphicsExtract(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float a) {
      Screen screen = this.init_Minecraft.gui.screen();
      if (screen != null) {
         try {
            screen.extractRenderStateWithTooltipAndSubtitles(graphics, mouseX, mouseY, a);
         } catch (Exception var7) {
         }
      }
   }

   private void load(GuiGraphicsExtractor graphics, int width, int height, float alpha) {
      int centerX = width / 2;
      int centerY = height / 2;
      double maxW = width * 0.85;
      double maxH = height * 0.85;
      double scale = Math.min(maxW / 2106.0, maxH / 1297.0) * 0.95;
      int drawW = (int)(2106.0 * scale);
      int drawH = (int)(1297.0 * scale);
      int x = centerX - drawW / 2;
      int y = centerY - drawH / 2;
      int color = ARGB.white(alpha);
      graphics.blit(
         RenderPipelines.GUI_TEXTURED, ThemeTextureRecolorer.init(init_Identifier, ThemePaletteManager.ColorRole.ACCENT), x, y, 0.0F, 0.0F, drawW, drawH, 2106, 1297, 2106, 1297, color
      );
   }

   private void read(GuiGraphicsExtractor graphics, int width, int height, float fadeOutAnim) {
      float barFade = 1.0F - Mth.clamp(fadeOutAnim, 0.0F, 1.0F);
      double maxW = width * 0.85;
      double maxH = height * 0.85;
      double scale = Math.min(maxW / 2106.0, maxH / 1297.0) * 0.95;
      int drawW = (int)(2106.0 * scale);
      int drawH = (int)(1297.0 * scale);
      int centerX = width / 2;
      int logoBottom = height / 2 + drawH / 2;
      int barY = logoBottom + 10;
      int x0 = centerX - drawW / 2;
      int y0 = barY - 5;
      int x1 = centerX + drawW / 2;
      int y1 = barY + 5;
      int barWidth = Mth.ceil((x1 - x0 - 2) * this.init_F);
      int alpha = Math.round(barFade * 255.0F);
      int barColor = ThemePaletteManager.recolor(ARGB.color(alpha, 236, 32, 39), ThemePaletteManager.ColorRole.ACCENT);
      if (barWidth > 0) {
         PacketUtil.init(graphics, NetHelper.init(x0 + 2, y0 + 2, barWidth, y1 - y0 - 4), barColor);
      }

      PacketUtil.load(graphics, NetHelper.init(x0, y0, x1 - x0, y1 - y0), barColor);
   }

   private static int init(int color, int alpha) {
      return color & 16777215 | alpha << 24;
   }

   private static Vector4f init(int color) {
      return new Vector4f((color >>> 16 & 0xFF) / 255.0F, (color >>> 8 & 0xFF) / 255.0F, (color & 0xFF) / 255.0F, (color >>> 24 & 0xFF) / 255.0F);
   }

   @Override
   public void tick() {
      if (!this.init() && this.read_J == -1L && this.init_ReloadInstance.isDone() && this.load()) {
         try {
            this.init_ReloadInstance.checkExceptions();
            this.init_Consumer.accept(Optional.empty());
         } catch (Throwable t) {
            this.init_Consumer.accept(Optional.of(t));
         }

         this.read_J = Util.getMillis();
         if (this.init_Minecraft.gui.screen() != null) {
            Window window = this.init_Minecraft.getWindow();
            this.init_Minecraft.gui.screen().init(window.getGuiScaledWidth(), window.getGuiScaledHeight());
         }
      }
   }

   private boolean load() {
      return !this.init_Z || this.write_J > -1L && Util.getMillis() - this.write_J >= 1000L;
   }

   @Override
   public boolean isPausing() {
      return true;
   }
}

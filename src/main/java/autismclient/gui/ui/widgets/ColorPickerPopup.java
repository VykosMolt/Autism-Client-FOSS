package autismclient.gui.ui.widgets;

import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.renderpearl.api.GpuFormat;
import com.mojang.renderpearl.api.textures.FilterMode;
import java.awt.Color;
import java.util.Locale;
import java.util.function.Consumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.resources.Identifier;
import autismclient.gui.f.b.CachePool;

public final class ColorPickerPopup {
   private static final int WIDTH = 224;
   private static final int HEIGHT = 171;
   private static final int read_I = 14;
   private static final int write = 13;
   private static final int update = 2;
   private static final int sync = 6;
   private static final int CONTENT_TOP_OFFSET = 19;
   private static final int SWATCH_X_OFFSET = 135;
   private static final int SLIDER_TOP_OFFSET = 107;
   private static final int SV_WIDTH = 102;
   private static final int push = 82;
   private static final int pop = 13;
   private static final int tick = 82;
   private static final int run = 360;
   private static final int BACKDROP_COLOR = -1728053248;
   private static final Minecraft MINECRAFT = Minecraft.getInstance();
   private static ColorPickerPopup.PickerTexture hueTexture;
   private static ColorPickerPopup.PickerTexture saturationValueTexture;
   private static int svTextureHueBucket = -2147483648;
   private final Consumer<Integer> onSave;
   private final int initialArgb;
   private final int defaultArgb;
   private final int initialAlpha;
   private autismclient.gui.f.NetHelper bounds;
   private boolean shown = true;
   private float hue;
   private float saturation;
   private float brightness;
   private boolean hexFocused;
   private String hexText;
   private int hexCursor;
   private int hexSelectionAnchor = -1;
   private int rgb;
   private int argb;
   private int red;
   private int green;
   private int blue;
   private ColorPickerPopup.DragTarget dragTarget = ColorPickerPopup.DragTarget.NONE;

   public ColorPickerPopup(autismclient.gui.f.NetHelper anchor, int initialArgb, int defaultArgb, int screenWidth, int screenHeight, Consumer<Integer> onSave) {
      this.onSave = onSave;
      this.initialArgb = initialArgb;
      this.defaultArgb = defaultArgb;
      this.initialAlpha = initialArgb >>> 24 & 0xFF;
      this.setFromRgb(initialArgb & 16777215);
      int x = anchor == null ? 8 : anchor.init() - 224;
      int y = anchor == null ? 8 : anchor.load() + 4;
      if (anchor != null && y + 171 > screenHeight - 4) {
         y = anchor.write() - 171 - 4;
      }

      this.bounds = autismclient.gui.f.NetHelper.init(x, y, 224, 171).init_NetHelper(Math.max(1, screenWidth - 4), Math.max(1, screenHeight - 4));
   }

   public boolean isOpen() {
      return this.shown;
   }

   public void dismiss() {
      this.shown = false;
   }

   public boolean contains(int mouseX, int mouseY) {
      return this.shown && this.bounds.init_Z(mouseX, mouseY);
   }

   public void render(autismclient.gui.f.SyncManager context) {
      if (this.shown) {
         autismclient.gui.f.PacketUtil.init(
            context.init_GuiGraphicsExtract(), autismclient.gui.f.NetHelper.init(0, 0, context.init_I(), context.load_I()), -1728053248
         );
         WindowFrameRenderer.drawFrame(
            context, this.bounds, "Color", false, false, true, this.headerBounds().init_Z(context.read(), context.write()), true, 5, 5, 14
         );
         autismclient.gui.f.NetHelper close = TitleBarRenderer.closeButton(this.headerBounds());
         boolean closeHover = close.init_Z(context.read(), context.write());
         float closeFade = autismclient.gui.ui.HoverTransitionCache.sample("colorpicker-close:" + autismclient.gui.ui.HoverTransitionCache.boundsKey(close), closeHover);
         if (closeFade > 0.001F) {
            autismclient.gui.f.PacketUtil.load(context.init_GuiGraphicsExtract(), close.init(1), 3, Math.round(34.0F * closeFade) << 24 | 16777215);
         }

         autismclient.gui.f.NetHelper sv = this.saturationValueArea();
         this.renderSaturationValue(context, sv);
         this.drawSvCursor(context, sv);
         autismclient.gui.f.NetHelper hueStrip = this.hueStrip();
         this.drawHueStrip(context, hueStrip);
         int hueY = hueStrip.write() + Math.round((1.0F - this.hue) * (hueStrip.sync() - 1));
         autismclient.gui.f.NetHelper hueMarker = autismclient.gui.f.NetHelper.init(hueStrip.read() - 1, hueY - 1, hueStrip.update() + 2, 3);
         autismclient.gui.f.PacketUtil.load(context.init_GuiGraphicsExtract(), hueMarker, 1, -16777216);
         autismclient.gui.f.PacketUtil.load(context.init_GuiGraphicsExtract(), hueMarker.init(1), 0, context.init_TableIndex().colors().textPrimary);
         autismclient.gui.f.NetHelper current = autismclient.gui.f.NetHelper.init(this.bounds.read() + 135, this.bounds.write() + 19, 38, 20);
         autismclient.gui.f.NetHelper draft = autismclient.gui.f.NetHelper.init(this.bounds.read() + 135 + 44, this.bounds.write() + 19, 38, 20);
         this.renderSwatch(context, current, this.initialArgb);
         this.renderSwatch(context, draft, this.currentArgb());
         context.init_PoolEntry()
            .drawCentered(
               context.init_GuiGraphicsExtract(),
               "Old",
               autismclient.gui.f.NetHelper.init(current.read(), current.load() + 2, current.update(), 10),
               context.init_TableIndex().colors().textSecondary
            );
         context.init_PoolEntry()
            .drawCentered(
               context.init_GuiGraphicsExtract(),
               "New",
               autismclient.gui.f.NetHelper.init(draft.read(), draft.load() + 2, draft.update(), 10),
               context.init_TableIndex().colors().textSecondary
            );
         this.renderChannelSlider(context, this.redSlider(), "R", this.red(), -43691);
         this.renderChannelSlider(context, this.greenSlider(), "G", this.green(), -11149961);
         this.renderChannelSlider(context, this.blueSlider(), "B", this.blue(), -10053121);
         autismclient.gui.f.NetHelper hex = this.hexBounds();
         TextFieldRenderer.draw(context, hex, this.hexText, "RRGGBB", this.hexFocused, this.hexCursor, this.selectionStart(), this.selectionEnd());
         context.init_PoolEntry()
            .drawFitted(
               context.init_GuiGraphicsExtract(), "#", hex.read() - 9, context.init_PoolEntry().centerTextY(hex), 8, context.init_TableIndex().colors().textSecondary
            );
         CachePool.init(context, this.saveButton(), "Save", CachePool.Inner_DataUtil.write, this.saveButton().init_Z(context.read(), context.write()), false);
         CachePool.init(context, this.resetButton(), "Reset", CachePool.Inner_DataUtil.init, this.resetButton().init_Z(context.read(), context.write()), false);
         CachePool.init(context, this.cancelButton(), "Cancel", CachePool.Inner_DataUtil.update, this.cancelButton().init_Z(context.read(), context.write()), false);
      }
   }

   public boolean mouseClicked(int mouseX, int mouseY, int button) {
      if (!this.shown) {
         return false;
      }

      if (button != 0) {
         return true;
      }

      if (!this.bounds.init_Z(mouseX, mouseY)) {
         this.dismiss();
         return true;
      }

      if (TitleBarRenderer.closeButton(this.headerBounds()).init_Z(mouseX, mouseY) || this.cancelButton().init_Z(mouseX, mouseY)) {
         this.dismiss();
         return true;
      }

      if (this.saveButton().init_Z(mouseX, mouseY)) {
         if (this.onSave != null) {
            this.onSave.accept(this.currentArgb());
         }

         this.shown = false;
         return true;
      } else {
         if (this.resetButton().init_Z(mouseX, mouseY)) {
            this.setFromRgb(this.defaultArgb & 16777215);
            return true;
         }

         autismclient.gui.f.NetHelper hex = this.hexBounds();
         this.hexFocused = hex.init_Z(mouseX, mouseY);
         if (this.hexFocused) {
            this.hexCursor = this.cursorIndexAt(mouseX, hex);
            this.hexSelectionAnchor = this.hexCursor;
            this.dragTarget = ColorPickerPopup.DragTarget.HEX;
            return true;
         }

         if (this.saturationValueArea().init_Z(mouseX, mouseY)) {
            this.dragTarget = ColorPickerPopup.DragTarget.SV;
            this.dragSaturationValue(mouseX, mouseY);
         } else if (this.hueStrip().init_Z(mouseX, mouseY)) {
            this.dragTarget = ColorPickerPopup.DragTarget.HUE;
            this.dragHue(mouseY);
         } else if (this.redSlider().init_Z(mouseX, mouseY)) {
            this.dragTarget = ColorPickerPopup.DragTarget.RED;
            this.dragChannel(mouseX, this.redSlider(), 0);
         } else if (this.greenSlider().init_Z(mouseX, mouseY)) {
            this.dragTarget = ColorPickerPopup.DragTarget.GREEN;
            this.dragChannel(mouseX, this.greenSlider(), 1);
         } else if (this.blueSlider().init_Z(mouseX, mouseY)) {
            this.dragTarget = ColorPickerPopup.DragTarget.BLUE;
            this.dragChannel(mouseX, this.blueSlider(), 2);
         } else {
            this.dragTarget = ColorPickerPopup.DragTarget.NONE;
         }

         return true;
      }
   }

   public boolean mouseReleased(int mouseX, int mouseY, int button) {
      if (button == 0) {
         this.dragTarget = ColorPickerPopup.DragTarget.NONE;
      }

      return this.shown;
   }

   public boolean mouseDragged(int mouseX, int mouseY, int button, double deltaX, double deltaY) {
      if (this.shown && button == 0) {
         switch (this.dragTarget) {
            case SV:
               this.dragSaturationValue(mouseX, mouseY);
               break;
            case HUE:
               this.dragHue(mouseY);
               break;
            case RED:
               this.dragChannel(mouseX, this.redSlider(), 0);
               break;
            case GREEN:
               this.dragChannel(mouseX, this.greenSlider(), 1);
               break;
            case BLUE:
               this.dragChannel(mouseX, this.blueSlider(), 2);
               break;
            case HEX:
               this.hexCursor = this.cursorIndexAt(mouseX, this.hexBounds());
         }

         return true;
      } else {
         return false;
      }
   }

   public boolean mouseScrolled(int mouseX, int mouseY, double amount) {
      return this.shown;
   }

   public boolean keyPressed(int key, int scanCode, int modifiers) {
      if (!this.shown) {
         return false;
      }

      if (key == 256) {
         this.dismiss();
         return true;
      }

      if (key != 257 && key != 335) {
         if (!this.hexFocused) {
            return true;
         }

         boolean ctrl = (modifiers & 2) != 0;
         if (ctrl && key == 67) {
            Minecraft.getInstance().keyboardHandler.setClipboard(this.selectedText());
            return true;
         }

         if (ctrl && key == 86) {
            this.paste(Minecraft.getInstance().keyboardHandler.getClipboard(), this.hasSelection());
            return true;
         }

         if (ctrl && key == 65) {
            this.selectAll();
            return true;
         }

         if (key == 259 && !this.hexText.isEmpty()) {
            if (this.hasSelection()) {
               this.insertText("");
            } else if (this.hexCursor > 0) {
               this.hexText = this.hexText.substring(0, this.hexCursor - 1) + this.hexText.substring(this.hexCursor);
               this.hexCursor--;
               this.applyHex();
            }
         } else if (key == 261) {
            if (this.hasSelection()) {
               this.insertText("");
            } else if (this.hexCursor < this.hexText.length()) {
               this.hexText = this.hexText.substring(0, this.hexCursor) + this.hexText.substring(this.hexCursor + 1);
               this.applyHex();
            }
         } else if (key == 268 || key == 269 || key == 263 || key == 262) {
            boolean shift = (modifiers & 1) != 0;
            this.moveCursor(key, shift);
            return true;
         }

         return true;
      } else {
         if (this.hexFocused) {
            this.applyHex();
         }

         if (this.onSave != null) {
            this.onSave.accept(this.currentArgb());
         }

         this.shown = false;
         return true;
      }
   }

   public boolean charTyped(char chr) {
      if (this.shown && this.hexFocused) {
         if (isHexDigit(chr)) {
            if (this.hasSelection()) {
               this.insertText(Character.toString(Character.toUpperCase(chr)));
            } else if (this.hexText.length() < 6) {
               this.hexText = (this.hexText.substring(0, this.hexCursor) + Character.toUpperCase(chr) + this.hexText.substring(this.hexCursor))
                  .toUpperCase(Locale.ROOT);
               this.hexCursor++;
               this.clearSelection();
               this.applyHex();
            }
         }

         return true;
      } else {
         return false;
      }
   }

   private int cursorIndexAt(int mouseX, autismclient.gui.f.NetHelper hex) {
      int local = Math.max(0, mouseX - hex.read() - 4);
      autismclient.gui.ui.UiTextRenderer text = autismclient.gui.f.UiHelper.init(MINECRAFT.font);
      int previousWidth = 0;

      for (int i = 1; i <= this.hexText.length(); i++) {
         int width = text.measureFragmentWidth(this.hexText, this.hexText.substring(0, i));
         if (local < (previousWidth + width) / 2.0F) {
            return i - 1;
         }

         previousWidth = width;
      }

      return this.hexText.length();
   }

   private void moveCursor(int key, boolean shift) {
      if (shift && this.hexSelectionAnchor < 0) {
         this.hexSelectionAnchor = this.hexCursor;
      }

      if (!shift) {
         this.clearSelection();
      }

      switch (key) {
         case 262:
            this.hexCursor = Math.min(this.hexText.length(), this.hexCursor + 1);
            break;
         case 263:
            this.hexCursor = Math.max(0, this.hexCursor - 1);
         case 264:
         case 265:
         case 266:
         case 267:
         default:
            break;
         case 268:
            this.hexCursor = 0;
            break;
         case 269:
            this.hexCursor = this.hexText.length();
      }

      if (shift && this.hexSelectionAnchor == this.hexCursor) {
         this.clearSelection();
      }
   }

   private void selectAll() {
      this.hexSelectionAnchor = 0;
      this.hexCursor = this.hexText.length();
   }

   private void clearSelection() {
      this.hexSelectionAnchor = -1;
   }

   private boolean hasSelection() {
      return this.hexSelectionAnchor >= 0 && this.hexSelectionAnchor != this.hexCursor;
   }

   private int selectionStart() {
      return this.hasSelection() ? Math.min(this.hexSelectionAnchor, this.hexCursor) : -1;
   }

   private int selectionEnd() {
      return this.hasSelection() ? Math.max(this.hexSelectionAnchor, this.hexCursor) : -1;
   }

   private String selectedText() {
      return !this.hasSelection() ? this.hexText : this.hexText.substring(this.selectionStart(), this.selectionEnd());
   }

   private void insertText(String value) {
      int start = this.hasSelection() ? this.selectionStart() : this.hexCursor;
      int end = this.hasSelection() ? this.selectionEnd() : this.hexCursor;
      String cleaned = sanitizeHex(value, 6 - (this.hexText.length() - (end - start)));
      this.hexText = (this.hexText.substring(0, start) + cleaned + this.hexText.substring(end)).toUpperCase(Locale.ROOT);
      this.hexCursor = start + cleaned.length();
      this.clearSelection();
      this.applyHex();
   }

   private static String sanitizeHex(String value, int maxLength) {
      if (value != null && maxLength > 0) {
         String text = value.trim();
         if (text.startsWith("#")) {
            text = text.substring(1);
         }

         if (text.length() >= 8) {
            text = text.substring(text.length() - 6);
         }

         StringBuilder cleaned = new StringBuilder(Math.min(6, maxLength));

         for (int i = 0; i < text.length() && cleaned.length() < maxLength; i++) {
            char chr = text.charAt(i);
            if (isHexDigit(chr)) {
               cleaned.append(Character.toUpperCase(chr));
            }
         }

         return cleaned.toString();
      } else {
         return "";
      }
   }

   private void clampCursor() {
      this.hexCursor = Math.max(0, Math.min(this.hexCursor, this.hexText == null ? 0 : this.hexText.length()));
      if (this.hexSelectionAnchor > (this.hexText == null ? 0 : this.hexText.length())) {
         this.hexSelectionAnchor = this.hexText.length();
      }
   }

   private void applyHex() {
      this.clampCursor();
      if (this.hexText.length() == 6) {
         try {
            int cursor = this.hexCursor;
            int anchor = this.hexSelectionAnchor;
            this.setFromRgb(Integer.parseInt(this.hexText, 16) & 16777215);
            this.hexCursor = cursor;
            this.hexSelectionAnchor = anchor;
         } catch (Exception var3) {
         }
      }
   }

   private void paste(String clipboard, boolean replaceSelectionOnly) {
      String cleaned = sanitizeHex(clipboard, 6);
      if (cleaned.length() == 6) {
         if (replaceSelectionOnly && this.hasSelection()) {
            this.insertText(cleaned);
         } else {
            this.hexText = cleaned.toUpperCase(Locale.ROOT);
            this.hexCursor = this.hexText.length();
            this.clearSelection();
            this.applyHex();
         }
      }
   }

   private void renderSaturationValue(autismclient.gui.f.SyncManager context, autismclient.gui.f.NetHelper area) {
      this.drawFrame(context, area);
      getSaturationValueTexture(this.hue).draw(context, area);
   }

   private void drawHueStrip(autismclient.gui.f.SyncManager context, autismclient.gui.f.NetHelper area) {
      this.drawFrame(context, area);
      getHueTexture().draw(context, area);
   }

   private void drawFrame(autismclient.gui.f.SyncManager context, autismclient.gui.f.NetHelper area) {
      autismclient.gui.f.PacketUtil.load(
         context.init_GuiGraphicsExtract(),
         autismclient.gui.f.NetHelper.init(area.read() - 1, area.write() - 1, area.update() + 2, area.sync() + 2),
         5,
         context.init_TableIndex().colors().borderSubtle
      );
   }

   private void renderChannelSlider(autismclient.gui.f.SyncManager context, autismclient.gui.f.NetHelper bounds, String label, int value, int accent) {
      autismclient.gui.ui.UiColorScheme colors = context.init_TableIndex().colors();
      context.init_PoolEntry()
         .drawCentered(context.init_GuiGraphicsExtract(), label, autismclient.gui.f.NetHelper.init(bounds.read() - 15, bounds.write(), 12, bounds.sync()), colors.textSecondary);
      int radius = Math.min(3, bounds.sync() / 2);
      autismclient.gui.f.PacketUtil.load(context.init_GuiGraphicsExtract(), bounds, radius, colors.inputSurface, colors.borderControl);
      int fillW = Math.max(0, Math.round((bounds.update() - 2) * (value / 255.0F)));
      if (fillW > 0) {
         autismclient.gui.f.PacketUtil.load(
            context.init_GuiGraphicsExtract(),
            autismclient.gui.f.NetHelper.init(bounds.read() + 1, bounds.write() + 1, fillW, bounds.sync() - 2),
            Math.max(1, radius - 1),
            accent
         );
      }

      int knobX = Math.min(bounds.init() - 3, bounds.read() + 1 + Math.round((bounds.update() - 3) * (value / 255.0F)));
      autismclient.gui.f.PacketUtil.load(
         context.init_GuiGraphicsExtract(), autismclient.gui.f.NetHelper.init(knobX, bounds.write() + 2, 2, Math.max(1, bounds.sync() - 4)), 1, -1
      );
      context.init_PoolEntry()
         .drawFitted(context.init_GuiGraphicsExtract(), Integer.toString(value), bounds.init() + 5, context.init_PoolEntry().centerTextY(bounds), 24, colors.textPrimary);
   }

   private void renderSwatch(autismclient.gui.f.SyncManager context, autismclient.gui.f.NetHelper bounds, int color) {
      autismclient.gui.f.PacketUtil.load(
         context.init_GuiGraphicsExtract(), bounds, 3, 0xFF000000 | color & 16777215, context.init_TableIndex().colors().borderSubtle
      );
   }

   private void drawSvCursor(autismclient.gui.f.SyncManager context, autismclient.gui.f.NetHelper area) {
      int x = area.read() + Math.round(this.saturation * (area.update() - 1));
      int y = area.write() + Math.round((1.0F - this.brightness) * (area.sync() - 1));
      autismclient.gui.f.PacketUtil.init_V_GuiGraphicsExtract(context.init_GuiGraphicsExtract(), x, y, 4.0F, -16777216);
      autismclient.gui.f.PacketUtil.init_V_GuiGraphicsExtract(context.init_GuiGraphicsExtract(), x, y, 3.0F, -1);
      autismclient.gui.f.PacketUtil.init_V_GuiGraphicsExtract(context.init_GuiGraphicsExtract(), x, y, 2.0F, 0xFF000000 | this.rgb);
   }

   private void dragSaturationValue(int mouseX, int mouseY) {
      autismclient.gui.f.NetHelper area = this.saturationValueArea();
      this.saturation = clamp01((float)(mouseX - area.read()) / Math.max(1, area.update() - 1));
      this.brightness = clamp01(1.0F - (float)(mouseY - area.write()) / Math.max(1, area.sync() - 1));
      this.syncFromHsb();
   }

   private void dragHue(int mouseY) {
      autismclient.gui.f.NetHelper area = this.hueStrip();
      this.hue = clamp01(1.0F - (float)(mouseY - area.write()) / Math.max(1, area.sync() - 1));
      this.syncFromHsb();
   }

   private void dragChannel(int mouseX, autismclient.gui.f.NetHelper bounds, int channel) {
      int value = Math.round(clamp01((float)(mouseX - bounds.read()) / Math.max(1, bounds.update())) * 255.0F);
      int r = this.red;
      int g = this.green;
      int b = this.blue;
      if (channel == 0) {
         r = value;
      } else if (channel == 1) {
         g = value;
      } else {
         b = value;
      }

      this.setFromRgb(r << 16 | g << 8 | b);
   }

   private void setFromRgb(int rgb) {
      float[] hsb = Color.RGBtoHSB(rgb >>> 16 & 0xFF, rgb >>> 8 & 0xFF, rgb & 0xFF, null);
      this.hue = hsb[0];
      this.saturation = hsb[1];
      this.brightness = hsb[2];
      this.setRgbComponents(rgb);
   }

   private void syncFromHsb() {
      this.setRgbComponents(Color.HSBtoRGB(this.hue, this.saturation, this.brightness) & 16777215);
   }

   private void setRgbComponents(int rgb) {
      this.rgb = rgb & 16777215;
      this.red = this.rgb >>> 16 & 0xFF;
      this.green = this.rgb >>> 8 & 0xFF;
      this.blue = this.rgb & 0xFF;
      this.argb = (this.initialAlpha & 0xFF) << 24 | this.rgb;
      this.syncHexText();
   }

   private void syncHexText() {
      this.hexText = String.format(Locale.ROOT, "%06X", this.rgb);
      this.hexCursor = this.hexText.length();
      this.clearSelection();
   }

   private int currentArgb() {
      return this.argb;
   }

   private int red() {
      return this.red;
   }

   private int green() {
      return this.green;
   }

   private int blue() {
      return this.blue;
   }

   private autismclient.gui.f.NetHelper headerBounds() {
      return autismclient.gui.f.NetHelper.init(this.bounds.read(), this.bounds.write(), this.bounds.update(), 14);
   }

   private autismclient.gui.f.NetHelper saturationValueArea() {
      return autismclient.gui.f.NetHelper.init(this.bounds.read() + 6, this.bounds.write() + 19, 102, 82);
   }

   private autismclient.gui.f.NetHelper hueStrip() {
      return autismclient.gui.f.NetHelper.init(this.bounds.read() + 6 + 102 + 6, this.bounds.write() + 19, 13, 82);
   }

   private autismclient.gui.f.NetHelper channelBounds(int index) {
      return autismclient.gui.f.NetHelper.init(this.bounds.read() + 21, this.bounds.write() + 107 + index * 15, 168, 13);
   }

   private autismclient.gui.f.NetHelper redSlider() {
      return this.channelBounds(0);
   }

   private autismclient.gui.f.NetHelper greenSlider() {
      return this.channelBounds(1);
   }

   private autismclient.gui.f.NetHelper blueSlider() {
      return this.channelBounds(2);
   }

   private autismclient.gui.f.NetHelper hexBounds() {
      return autismclient.gui.f.NetHelper.init(this.bounds.read() + 135 + 12, this.bounds.write() + 19 + 36, 70, 13);
   }

   private autismclient.gui.f.NetHelper buttonBounds(int index) {
      int inner = this.bounds.update() - 12;
      int w = (inner - 4) / 3;
      int x = this.bounds.read() + 6 + index * (w + 2);
      int width = index == 2 ? this.bounds.read() + 6 + inner - x : w;
      return autismclient.gui.f.NetHelper.init(x, this.bounds.load() - 4 - 13, width, 13);
   }

   private autismclient.gui.f.NetHelper saveButton() {
      return this.buttonBounds(0);
   }

   private autismclient.gui.f.NetHelper resetButton() {
      return this.buttonBounds(1);
   }

   private autismclient.gui.f.NetHelper cancelButton() {
      return this.buttonBounds(2);
   }

   private static boolean isHexDigit(char chr) {
      return chr >= '0' && chr <= '9' || chr >= 'a' && chr <= 'f' || chr >= 'A' && chr <= 'F';
   }

   private static float clamp01(float value) {
      return Math.max(0.0F, Math.min(1.0F, value));
   }

   private static ColorPickerPopup.PickerTexture getHueTexture() {
      if (hueTexture == null) {
         hueTexture = ColorPickerPopup.PickerTexture.create("color_picker_hue", 13, 82);
         hueTexture.paintHue();
      }

      return hueTexture;
   }

   private static ColorPickerPopup.PickerTexture getSaturationValueTexture(float hue) {
      int bucket = Math.round(clamp01(hue) * 360.0F);
      if (saturationValueTexture == null) {
         saturationValueTexture = ColorPickerPopup.PickerTexture.create("color_picker_sv", 102, 82);
         svTextureHueBucket = -2147483648;
      }

      if (svTextureHueBucket != bucket) {
         saturationValueTexture.paintSaturationValue(bucket / 360.0F);
         svTextureHueBucket = bucket;
      }

      return saturationValueTexture;
   }

   private enum DragTarget {
      NONE,
      SV,
      HUE,
      RED,
      GREEN,
      BLUE,
      HEX;
   }

   private static final class PickerTexture extends AbstractTexture {
      private final Identifier id;
      private final NativeImage image;
      private final int width;
      private final int height;

      private static ColorPickerPopup.PickerTexture create(String name, int width, int height) {
         Identifier id = Identifier.fromNamespaceAndPath("autismclient", "dynamic/ui/" + name);
         ColorPickerPopup.PickerTexture texture = new ColorPickerPopup.PickerTexture(id, name, width, height);
         ColorPickerPopup.MINECRAFT.getTextureManager().register(id, texture);
         return texture;
      }

      private PickerTexture(Identifier id, String name, int width, int height) {
         this.id = id;
         this.width = width;
         this.height = height;
         this.image = new NativeImage(width, height, true);
         this.texture = RenderSystem.getDevice().createTexture("AUTISM " + name, 5, GpuFormat.RGBA8_UNORM, width, height, 1, 1);
         this.sampler = RenderSystem.getSamplerCache().getRepeat(FilterMode.NEAREST);
         this.textureView = RenderSystem.getDevice().createTextureView(this.texture);
      }

      private void paintHue() {
         for (int y = 0; y < this.height; y++) {
            float h = this.height <= 1 ? 0.0F : 1.0F - (float)y / (this.height - 1);
            int color = 0xFF000000 | Color.HSBtoRGB(h, 1.0F, 1.0F) & 16777215;

            for (int x = 0; x < this.width; x++) {
               this.image.setPixel(x, y, color);
            }
         }

         this.upload();
      }

      private void paintSaturationValue(float hue) {
         for (int x = 0; x < this.width; x++) {
            float s = this.width <= 1 ? 0.0F : (float)x / (this.width - 1);

            for (int y = 0; y < this.height; y++) {
               float v = this.height <= 1 ? 0.0F : 1.0F - (float)y / (this.height - 1);
               this.image.setPixel(x, y, 0xFF000000 | Color.HSBtoRGB(hue, s, v) & 16777215);
            }
         }

         this.upload();
      }

      private void upload() {
         RenderSystem.getDevice().createCommandEncoder().writeToTexture(this.texture, this.image);
      }

      private void draw(autismclient.gui.f.SyncManager context, autismclient.gui.f.NetHelper area) {
         context.init_GuiGraphicsExtract()
            .blit(
               RenderPipelines.GUI_TEXTURED,
               this.id,
               area.read(),
               area.write(),
               0.0F,
               0.0F,
               area.update(),
               area.sync(),
               this.width,
               this.height,
               this.width,
               this.height,
               -1
            );
      }

      @Override
      public void close() {
         this.image.close();
         super.close();
      }
   }
}

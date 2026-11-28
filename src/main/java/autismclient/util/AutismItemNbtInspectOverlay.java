package autismclient.util;

import autismclient.gui.ui.widgets.UiLayoutMath;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.world.item.ItemStack;

public class AutismItemNbtInspectOverlay extends DraggableOverlayWindow {
   private static final Minecraft MINECRAFT = Minecraft.getInstance();
   private static final int flush_I = 4;
   private static AutismItemNbtInspectOverlay instance;
   private final Font font;
   private final autismclient.gui.f.b.CharEncoder theme = new autismclient.gui.f.b.CharEncoder();
   private ItemNbtInspector.NbtInspection inspection;
   private autismclient.gui.ui.layout.ScrollableListView listView;
   private List<AutismItemNbtInspectOverlay.WrappedLine> wrappedLines = Collections.emptyList();
   private boolean layoutDirty = true;
   private boolean update_Z;
   private int layoutWidth;
   private boolean sync_Z;
   private boolean flush_Z;
   private double dragOffsetX;
   private double dragOffsetY;

   public AutismItemNbtInspectOverlay(Font textRenderer) {
      super("item-nbt-viewer", 260, 180);
      this.font = textRenderer;
      this.windowX = 110;
      this.windowY = 42;
   }

   public static AutismItemNbtInspectOverlay getOrCreate(Font textRenderer) {
      if (textRenderer == null) {
         return null;
      }

      if (instance == null) {
         instance = new AutismItemNbtInspectOverlay(textRenderer);
      }

      return instance;
   }

   public static void hide() {
      if (instance != null) {
         instance.setVisible(false);
         OverlayManager.getInstance().unregister(instance);
      }
   }

   public static boolean inspectStack(ItemStack stack) {
      if (stack != null && !stack.isEmpty() && MINECRAFT.font != null) {
         AutismItemNbtInspectOverlay overlay = getOrCreate(MINECRAFT.font);
         if (overlay == null) {
            return false;
         }

         int screenW = MINECRAFT.getWindow() == null ? 640 : cA.init_I();
         int x = Math.max(8, (screenW - 320) / 2);
         overlay.inspectStack(stack, x, 42);
         return true;
      } else {
         return false;
      }
   }

   public static boolean inspectSource(ItemNbtInspector.NbtInspection inspection) {
      if (inspection != null && MINECRAFT.font != null) {
         AutismItemNbtInspectOverlay overlay = getOrCreate(MINECRAFT.font);
         if (overlay == null) {
            return false;
         }

         int screenW = MINECRAFT.getWindow() == null ? 640 : cA.init_I();
         overlay.showInspection(inspection, Math.max(8, (screenW - 320) / 2), 42);
         return true;
      } else {
         return false;
      }
   }

   public void inspectStack(ItemStack stack, int anchorX, int anchorY) {
      if (stack != null && !stack.isEmpty()) {
         this.showInspection(ItemNbtInspector.inspect(stack), anchorX, anchorY);
      }
   }

   public void showInspection(ItemNbtInspector.NbtInspection inspection, int anchorX, int anchorY) {
      if (inspection != null) {
         OverlayManager manager = OverlayManager.getInstance();
         manager.register(this);
         this.inspection = inspection;
         this.sync_Z = false;
         this.windowVisible = true;
         this.windowCollapsed = false;
         this.listView = null;
         this.layoutDirty = true;
         int screenW = MINECRAFT.getWindow() == null ? 640 : cA.init_I();
         int screenH = MINECRAFT.getWindow() == null ? 360 : cA.load_I();
         int width = this.resolveWindowWidth(this.preferredWidth(), this.getMinWidth());
         this.windowWidth = width;
         int height = this.resolveWindowHeight(this.preferredHeight(), this.getMinHeight());
         this.setBounds(
            new OverlayBounds(Math.max(8, Math.min(anchorX, screenW - width - 8)), Math.max(8, Math.min(anchorY, screenH - height - 8)), width, height, true, false)
         );
         manager.bringToFront(this);
      }
   }

   private int preferredWidth() {
      int screenW = MINECRAFT.getWindow() == null ? 640 : cA.init_I();
      return Math.min(Math.max(1, screenW - 18), Math.max(this.getMinWidth(), Math.min(300, screenW / 3)));
   }

   private int preferredHeight() {
      int screenH = MINECRAFT.getWindow() == null ? 360 : cA.load_I();
      int availableH = Math.max(14, screenH - 18);
      int maxH = Math.min(availableH, Math.max(this.getMinHeight(), screenH * 65 / 100));
      return Math.min(maxH, Math.max(this.getMinHeight(), this.contentTop() + this.inspectionLines().size() * this.lineHeight() + 2));
   }

   @Override
   public boolean isResizable() {
      return true;
   }

   @Override
   protected void onSizeChanged() {
      this.layoutDirty = true;
      if (!this.hasCustomStoredSize()) {
         OverlayBounds b = this.getBounds();
         this.setBounds(new OverlayBounds(b.x, b.y, this.preferredWidth(), this.preferredHeight(), b.visible, b.collapsed));
      }
   }

   public void hideWindow() {
      this.windowVisible = false;
      this.flush_Z = false;
      this.listView = null;
   }

   @Override
   public void render(GuiGraphicsExtractor context, int mouseX, int mouseY, float delta) {
      if (this.windowVisible || this.isClosing()) {
         OverlayBounds bounds = this.getBounds();
         String title = this.inspection == null ? "NBT Inspector" : this.inspection.init_Str();
         this.renderWindowFrame(context, mouseX, mouseY, bounds, title, this.windowCollapsed, this.flush_Z);
         boolean clipped = this.beginBodyClip(context, bounds, this.windowCollapsed);
         if (!clipped) {
            this.renderInactiveDim(context, bounds, this.windowCollapsed, this.flush_Z);
         } else {
            try {
               if (this.inspection != null) {
                  int tabY = this.tabY();

                  for (AutismItemNbtInspectOverlay.TabButton tab : this.tabs(tabY)) {
                     autismclient.gui.ui.widgets.ButtonDrawHelper.init(
                        context,
                        this.font,
                        tab.init_I(),
                        tab.load(),
                        tab.read(),
                        this.tabHeight(),
                        tab.label(),
                        "Raw".equals(tab.label()) == this.sync_Z,
                        mouseX,
                        mouseY
                     );
                  }

                  int listX = this.windowX + this.padding();
                  int listY = tabY + this.tabHeight() + 2;
                  int listWidth = this.windowWidth - this.padding() * 2;
                  int footerY = this.footerY();
                  int listHeight = Math.max(1, footerY - listY - 4);
                  int lineH = this.lineHeight();
                  int textX = listX + this.textPadding();
                  int maxTextWidth = autismclient.gui.ui.widgets.ScrollbarContentMath.init(listWidth, this.textPadding());
                  this.rewrap(maxTextWidth);
                  if (this.listView == null
                     || this.listView.getX() != listX
                     || this.listView.getY() != listY
                     || this.listView.getWidth() != listWidth
                     || this.listView.getHeight() != listHeight) {
                     int oldScroll = this.listView == null ? 0 : this.listView.getScrollOffset();
                     this.listView = new autismclient.gui.ui.layout.ScrollableListView(listX, listY, listWidth, listHeight, lineH, 4);
                     this.listView.setScrollOffset(oldScroll);
                  }

                  this.listView.setContentHeight(this.wrappedLines.size() * lineH);
                  this.listView.beginFrame(context, this.theme.set(), this.theme.reset());

                  try {
                     this.listView.forEachVisibleRow(context, this.wrappedLines.size(), (idx, bnd) -> {
                        AutismItemNbtInspectOverlay.WrappedLine line = this.wrappedLines.get(idx);
                        int y = UiLayoutMath.centerTextY(bnd.y, lineH, this.theme.init_I_l(autismclient.gui.f.b.l.read), this.theme.flush());
                        this.renderLine(context, line, textX, y);
                     });
                  } finally {
                     this.listView.endFrame(context);
                  }

                  this.listView.renderScrollbar(context, mouseX, mouseY);
                  autismclient.gui.f.b.k.init(
                     context,
                     this.font,
                     this.wrappedLines.size() + " lines",
                     this.theme.init_Identifier_l(autismclient.gui.f.b.l.write),
                     this.theme.load(autismclient.gui.f.b.l.write),
                     this.windowX + this.padding() + 2,
                     UiLayoutMath.centerTextY(footerY, this.tabHeight(), this.theme.init_I_l(autismclient.gui.f.b.l.write), this.theme.flush()),
                     false
                  );

                  for (AutismItemNbtInspectOverlay.TabButton button : this.footerButtons(footerY)) {
                     autismclient.gui.ui.widgets.ButtonDrawHelper.init(
                        context,
                        this.font,
                        button.init_I(),
                        button.load(),
                        button.read(),
                        this.tabHeight(),
                        button.label(),
                        button.active() ? autismclient.gui.ui.widgets.OverlayActionButton.ButtonTone.SUCCESS : autismclient.gui.ui.widgets.OverlayActionButton.ButtonTone.GHOST,
                        true,
                        mouseX,
                        mouseY
                     );
                  }

                  return;
               }

               autismclient.gui.ui.widgets.ListRowRenderer.drawEmptyPlaceholder(
                  context,
                  this.font,
                  "No item selected.",
                  this.windowX + this.padding(),
                  this.windowY + 14 + this.padding(),
                  this.windowWidth - this.padding() * 2
               );
            } finally {
               this.endBodyClip(context, clipped);
               this.renderInactiveDim(context, bounds, this.windowCollapsed, this.flush_Z);
            }

            return;
         }
      }
   }

   @Override
   public boolean mouseClicked(double mouseX, double mouseY, int button) {
      if (!this.windowVisible) {
         return false;
      }

      OverlayBounds bounds = this.getBounds();
      if (button == 0 && mouseY >= this.windowY && mouseY <= this.windowY + 14 && mouseX >= this.windowX && mouseX <= this.windowX + this.windowWidth) {
         if (this.isOverHeaderCloseButton(mouseX, mouseY, bounds)) {
            this.hideWindow();
            return true;
         } else {
            this.flush_Z = true;
            this.dragOffsetX = mouseX - this.windowX;
            this.dragOffsetY = mouseY - this.windowY;
            return true;
         }
      } else {
         if (this.windowCollapsed) {
            return true;
         }

         if (button == 0) {
            int tabY = this.tabY();

            for (AutismItemNbtInspectOverlay.TabButton tab : this.tabs(tabY)) {
               if (tab.contains(mouseX, mouseY, this.tabHeight())) {
                  this.sync_Z = "Raw".equals(tab.label());
                  this.layoutDirty = true;
                  if (this.listView != null) {
                     this.listView.setScrollOffset(0);
                  }

                  return true;
               }
            }

            if (this.listView != null && this.listView.containsPoint(mouseX, mouseY) && this.listView.mouseClicked(mouseX, mouseY, button)) {
               return true;
            }

            int footerY = this.footerY();

            for (AutismItemNbtInspectOverlay.TabButton footer : this.footerButtons(footerY)) {
               if (footer.contains(mouseX, mouseY, this.tabHeight())) {
                  this.copyToClipboard(footer.label());
                  return true;
               }
            }
         }

         return this.containsPoint(mouseX, mouseY);
      }
   }

   @Override
   public boolean mouseReleased(double mouseX, double mouseY, int button) {
      if (button == 0 && this.flush_Z) {
         this.flush_Z = false;
         this.saveBounds();
         return true;
      } else if (button == 0 && this.listView != null) {
         this.listView.mouseReleased();
         return true;
      } else {
         this.flush_Z = false;
         return false;
      }
   }

   @Override
   public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
      if (this.listView != null) {
         this.listView.mouseDragged(mouseX, mouseY);
      }

      if (this.windowVisible && button == 0 && this.flush_Z) {
         this.setBounds(new OverlayBounds((int)Math.round(mouseX - this.dragOffsetX), (int)Math.round(mouseY - this.dragOffsetY), this.windowWidth, this.windowHeight, this.windowVisible, this.windowCollapsed));
         return true;
      } else {
         return false;
      }
   }

   @Override
   public boolean mouseScrolled(double mouseX, double mouseY, double amount) {
      return this.windowVisible && !this.windowCollapsed && this.listView != null && this.listView.containsPoint(mouseX, mouseY)
         ? this.listView.mouseScrolled(mouseX, mouseY, amount)
         : false;
   }

   @Override
   public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
      if (!this.windowVisible) {
         return false;
      } else if (keyCode == 256) {
         this.hideWindow();
         return true;
      } else {
         return false;
      }
   }

   @Override
   public boolean charTyped(char chr, int modifiers) {
      return false;
   }

   @Override
   public void setVisible(boolean visible) {
      if (!visible) {
         this.hideWindow();
      } else {
         this.windowVisible = true;
      }
   }

   @Override
   public boolean containsPoint(double mouseX, double mouseY) {
      if (!this.windowVisible) {
         return false;
      }

      int height = this.windowCollapsed ? 14 : this.windowHeight;
      return mouseX >= this.windowX && mouseX <= this.windowX + this.windowWidth && mouseY >= this.windowY && mouseY <= this.windowY + height;
   }

   @Override
   public boolean isOverHeader(double mouseX, double mouseY) {
      return !this.windowVisible
         ? false
         : mouseX >= this.windowX
            && mouseX <= this.windowX + this.windowWidth
            && mouseY >= this.windowY
            && mouseY <= this.windowY + 14
            && !this.isOverHeaderControl(mouseX, mouseY, this.getBounds());
   }

   @Override
   public boolean isCollapsed() {
      return this.windowCollapsed;
   }

   @Override
   public void setCollapsed(boolean collapsed) {
      if (this.windowCollapsed != collapsed) {
         this.windowCollapsed = collapsed;
         this.flush_Z = false;
         if (collapsed) {
            this.clearFocusAndDismissMenu();
         }

         this.saveBounds();
      }
   }

   @Override
   public boolean supportsHeaderCollapseClick() {
      return true;
   }

   @Override
   public int getZPriority() {
      return 13;
   }

   @Override
   public int getMinWidth() {
      return 220;
   }

   @Override
   public int getMinHeight() {
      return this.contentTop() + this.lineHeight() * 4 + 2;
   }

   private List<ItemNbtInspector.InspectLine> inspectionLines() {
      if (this.inspection == null) {
         return List.of();
      } else {
         return this.sync_Z ? this.inspection.rawLines() : this.inspection.lines();
      }
   }

   private void rewrap(int maxWidth) {
      if (this.layoutDirty || this.update_Z != this.sync_Z || this.layoutWidth != maxWidth) {
         if (this.inspection == null) {
            this.wrappedLines = Collections.emptyList();
            this.layoutDirty = false;
            this.update_Z = this.sync_Z;
            this.layoutWidth = maxWidth;
         } else {
            List<AutismItemNbtInspectOverlay.WrappedLine> wrapped = new ArrayList<>();

            for (ItemNbtInspector.InspectLine line : this.inspectionLines()) {
               this.wrapLine(line, maxWidth, wrapped);
            }

            this.wrappedLines = wrapped;
            this.layoutDirty = false;
            this.update_Z = this.sync_Z;
            this.layoutWidth = maxWidth;
         }
      }
   }

   private void wrapLine(ItemNbtInspector.InspectLine line, int maxWidth, List<AutismItemNbtInspectOverlay.WrappedLine> target) {
      if (line.tokens() != null && !line.tokens().isEmpty()) {
         this.wrapTokens(line.tokens(), maxWidth, target);
      } else {
         String text = line.text();
         if (text == null || text.isEmpty()) {
            target.add(AutismItemNbtInspectOverlay.WrappedLine.plain("", 0, line.color()));
         } else if (this.isBlank(text)) {
            this.wrapText(text, 0, line.color(), maxWidth, target);
         } else {
            int leadingSpaces = this.leadingSpaces(text);
            String indentComponent = " ".repeat(leadingSpaces);
            int indentWidth = this.textWidth(indentComponent);
            int colonIndex = text.indexOf(58, leadingSpaces);
            if (colonIndex > leadingSpaces && colonIndex < text.length() - 1) {
               String label = text.substring(leadingSpaces, colonIndex + 1).trim();
               String value = text.substring(colonIndex + 1).stripLeading();
               String prefix = indentComponent + label + " ";
               int prefixWidth = this.textWidth(prefix);
               int continuationOffset = Math.min(prefixWidth, indentWidth + this.continuationIndent());
               this.wrapValue(prefix, value, line.color(), prefixWidth, continuationOffset, maxWidth, target);
            } else {
               this.wrapText(text.substring(leadingSpaces), indentWidth, line.color(), maxWidth, target);
            }
         }
      }
   }

   private void wrapValue(
      String prefix,
      String value,
      int valueColor,
      int prefixWidth,
      int continuationOffset,
      int maxWidth,
      List<AutismItemNbtInspectOverlay.WrappedLine> target
   ) {
      int safeContinuationOffset = autismclient.gui.ui.widgets.ScrollbarContentMath.load(maxWidth, continuationOffset);
      if (prefixWidth >= maxWidth) {
         this.wrapText(prefix.stripTrailing(), 0, UiColorPalette.silver(), maxWidth, target);
         this.wrapText(value, safeContinuationOffset, valueColor, maxWidth, target);
      } else if (value != null && !value.isEmpty()) {
         String remaining = value;

         for (boolean firstLine = true; !remaining.isEmpty(); firstLine = false) {
            int offset = firstLine ? prefixWidth : safeContinuationOffset;
            int availableWidth = autismclient.gui.ui.widgets.ScrollbarContentMath.read(maxWidth, offset);
            String current = remaining;
            int split = autismclient.gui.ui.TextWrapLayout.init(
               current, 0, current.length(), availableWidth, (start, end) -> this.textWidth(current.substring(start, end))
            );
            String part = remaining.substring(0, split).stripTrailing();
            target.add(this.wrappedLine(firstLine ? prefix : null, part, valueColor, offset));
            remaining = remaining.substring(split).stripLeading();
         }
      } else {
         target.add(
            AutismItemNbtInspectOverlay.WrappedLine.ofTokens(List.of(new ItemNbtInspector.HighlightToken(prefix, UiColorPalette.silver()), new ItemNbtInspector.HighlightToken("", valueColor)), 0)
         );
      }
   }

   private void wrapText(String text, int offset, int color, int maxWidth, List<AutismItemNbtInspectOverlay.WrappedLine> target) {
      if (text != null) {
         if (text.isEmpty()) {
            target.add(AutismItemNbtInspectOverlay.WrappedLine.plain("", offset, color));
         } else {
            int safeOffset = autismclient.gui.ui.widgets.ScrollbarContentMath.load(maxWidth, offset);
            String remaining = text;

            while (!remaining.isEmpty()) {
               int availableWidth = autismclient.gui.ui.widgets.ScrollbarContentMath.read(maxWidth, safeOffset);
               String current = remaining;
               int split = autismclient.gui.ui.TextWrapLayout.init(
                  current, 0, current.length(), availableWidth, (start, end) -> this.textWidth(current.substring(start, end))
               );
               String part = remaining.substring(0, split).stripTrailing();
               target.add(AutismItemNbtInspectOverlay.WrappedLine.plain(part, safeOffset, color));
               remaining = remaining.substring(split).stripLeading();
            }
         }
      }
   }

   private void renderLine(GuiGraphicsExtractor context, AutismItemNbtInspectOverlay.WrappedLine line, int x, int y) {
      if (line != null && line.tokens() != null) {
         int cursor = x + line.indent();

         for (ItemNbtInspector.HighlightToken token : line.tokens()) {
            if (token != null && token.text() != null && !token.text().isEmpty()) {
               autismclient.gui.f.b.k.init(
                  context,
                  this.font,
                  token.text(),
                  this.theme.init_Identifier_l(autismclient.gui.f.b.l.read),
                  token.color(),
                  cursor,
                  y,
                  false
               );
               cursor += this.textWidth(token.text());
            }
         }
      }
   }

   private void wrapTokens(List<ItemNbtInspector.HighlightToken> source, int maxWidth, List<AutismItemNbtInspectOverlay.WrappedLine> target) {
      if (source != null && !source.isEmpty()) {
         int indent = this.tokensWidth(source);
         int continuationOffset = autismclient.gui.ui.widgets.ScrollbarContentMath.load(maxWidth, indent + this.continuationIndent());
         List<ItemNbtInspector.HighlightToken> current = new ArrayList<>();
         int currentWidth = 0;
         boolean continuation = false;

         for (ItemNbtInspector.HighlightToken token : source) {
            if (token != null && token.text() != null && !token.text().isEmpty()) {
               String remaining = token.text();

               while (!remaining.isEmpty()) {
                  int limit = maxWidth - (continuation ? continuationOffset : 0);
                  int room = Math.max(1, limit - currentWidth);
                  String piece = this.fitPrefix(remaining, room);
                  if (piece.isEmpty()) {
                     if (!current.isEmpty()) {
                        target.add(AutismItemNbtInspectOverlay.WrappedLine.ofTokens(current, continuation ? continuationOffset : 0));
                        current = new ArrayList<>();
                        currentWidth = 0;
                        continuation = true;
                        continue;
                     }

                     piece = remaining.substring(0, 1);
                  }

                  current.add(new ItemNbtInspector.HighlightToken(piece, token.color()));
                  currentWidth += this.textWidth(piece);
                  remaining = remaining.substring(piece.length());
                  if (!remaining.isEmpty()) {
                     target.add(AutismItemNbtInspectOverlay.WrappedLine.ofTokens(current, continuation ? continuationOffset : 0));
                     current = new ArrayList<>();
                     currentWidth = 0;
                     continuation = true;
                     remaining = remaining.stripLeading();
                  } else if (currentWidth >= Math.max(22, limit - 4)) {
                     target.add(AutismItemNbtInspectOverlay.WrappedLine.ofTokens(current, continuation ? continuationOffset : 0));
                     current = new ArrayList<>();
                     currentWidth = 0;
                     continuation = true;
                  }
               }
            }
         }

         if (!current.isEmpty()) {
            target.add(AutismItemNbtInspectOverlay.WrappedLine.ofTokens(current, continuation ? continuationOffset : 0));
         }
      } else {
         target.add(AutismItemNbtInspectOverlay.WrappedLine.plain("", 0, UiColorPalette.textMuted()));
      }
   }

   private AutismItemNbtInspectOverlay.WrappedLine wrappedLine(String prefix, String value, int valueColor, int offset) {
      List<ItemNbtInspector.HighlightToken> tokens = new ArrayList<>();
      if (prefix != null && !prefix.isEmpty()) {
         tokens.add(new ItemNbtInspector.HighlightToken(prefix, UiColorPalette.silver()));
      }

      tokens.add(new ItemNbtInspector.HighlightToken(value == null ? "" : value, valueColor));
      return AutismItemNbtInspectOverlay.WrappedLine.ofTokens(tokens, prefix != null && !prefix.isEmpty() ? 0 : offset);
   }

   private int tokensWidth(List<ItemNbtInspector.HighlightToken> tokens) {
      int width = 0;

      for (ItemNbtInspector.HighlightToken token : tokens) {
         if (token != null && token.text() != null && !token.text().isEmpty()) {
            String text = token.text();
            int spaces = 0;

            while (spaces < text.length() && Character.isWhitespace(text.charAt(spaces))) {
               spaces++;
            }

            if (spaces <= 0) {
               return width;
            }

            width += this.textWidth(text.substring(0, spaces));
            if (spaces < text.length()) {
               return width;
            }
         }
      }

      return width;
   }

   private String fitPrefix(String value, int maxWidth) {
      if (value != null && !value.isEmpty()) {
         if (this.textWidth(value) <= maxWidth) {
            return value;
         }

         int low = 0;
         int high = value.length();

         while (low < high) {
            int mid = low + high + 1 >>> 1;
            String candidate = value.substring(0, mid);
            if (this.textWidth(candidate) <= maxWidth) {
               low = mid;
            } else {
               high = mid - 1;
            }
         }

         return low <= 0 ? "" : value.substring(0, low);
      } else {
         return "";
      }
   }

   private boolean isBlank(String text) {
      String trimmed = text == null ? "" : text.trim();
      return trimmed.startsWith("[") && trimmed.endsWith("]");
   }

   private int leadingSpaces(String text) {
      int count = 0;

      while (count < text.length() && text.charAt(count) == ' ') {
         count++;
      }

      return count;
   }

   private int textWidth(String text) {
      return text != null && !text.isEmpty()
         ? autismclient.gui.f.b.k.init(this.font, text, this.theme.init_Identifier_l(autismclient.gui.f.b.l.read), UiColorPalette.softWhite())
         : 0;
   }

   private void copyToClipboard(String label) {
      if (this.inspection != null && MINECRAFT.keyboardHandler != null) {
         switch (label) {
            case "Copy Raw":
               MINECRAFT.keyboardHandler.setClipboard(this.inspection.update());
               bi.init("Copied raw " + this.inspection.load_Str() + " NBT.");
               break;
            case "Copy Nice":
               MINECRAFT.keyboardHandler.setClipboard(this.inspection.write());
               bi.init("Copied " + this.inspection.load_Str() + " NBT summary.");
               break;
            case "Fill Editor":
               if (!AutismLiteVariant.enabled()
                  && this.inspection.stack() != null
                  && !this.inspection.stack().isEmpty()
                  && autismclient.modules.AdminToolsItemHelper.load(this.inspection.stack())) {
                  bi.init("Filled Admin Tools with complete item NBT.", -13248397);
               } else {
                  AutismClientMessaging.sendPrefixed("Admin Tools: could not fill NBT editor.");
               }
         }
      }
   }

   private List<AutismItemNbtInspectOverlay.TabButton> tabs(int y) {
      int x = this.windowX + this.padding();
      int w = Math.max(40, Math.max(this.tabButtonWidth("Nice"), this.tabButtonWidth("Raw")));
      return List.of(
         new AutismItemNbtInspectOverlay.TabButton("Nice", x, y, w, false), new AutismItemNbtInspectOverlay.TabButton("Raw", x + w + 2, y, w, false)
      );
   }

   private int tabY() {
      return this.windowY + 14 + this.padding();
   }

   private int contentTop() {
      return 14 + this.padding() + this.tabHeight() + 2 + 4 + this.footerHeight();
   }

   private int footerY() {
      return this.windowY + this.windowHeight - this.footerHeight();
   }

   private boolean poll_Z_6() {
      return !AutismLiteVariant.enabled()
         && this.inspection != null
         && this.inspection.stack() != null
         && !this.inspection.stack().isEmpty();
   }

   private boolean footerFitsOneRow() {
      int need = autismclient.gui.f.b.k.init(
            this.font,
            "9999 lines",
            this.theme.init_Identifier_l(autismclient.gui.f.b.l.write),
            this.theme.load(autismclient.gui.f.b.l.write)
         )
         + 6
         + this.footerButtonWidth("Copy Raw")
         + this.footerButtonWidth("Copy Nice")
         + this.buttonGap();
      if (this.poll_Z_6()) {
         need += this.footerButtonWidth("Fill Editor") + this.buttonGap();
      }

      return need <= this.windowWidth - this.padding() * 2;
   }

   private List<AutismItemNbtInspectOverlay.TabButton> footerButtons(int y) {
      ArrayList<AutismItemNbtInspectOverlay.TabButton> buttons = new ArrayList<>();
      boolean editor = !AutismLiteVariant.enabled()
         && this.inspection != null
         && this.inspection.stack() != null
         && !this.inspection.stack().isEmpty();
      if (!this.footerFitsOneRow()) {
         int firstCursor = this.windowX + this.windowWidth - this.padding();
         if (editor) {
            this.layoutButtons(buttons, firstCursor, y, "Fill Editor", true);
         }

         int rowY = y + this.tabHeight() + 2;
         int secondCursor = this.windowX + this.windowWidth - this.padding();
         secondCursor = this.layoutButtons(buttons, secondCursor, rowY, "Copy Raw", false);
         this.layoutButtons(buttons, secondCursor, rowY, "Copy Nice", false);
         return buttons;
      } else {
         int cursor = this.windowX + this.windowWidth - this.padding();
         if (editor) {
            cursor = this.layoutButtons(buttons, cursor, y, "Fill Editor", true);
         }

         cursor = this.layoutButtons(buttons, cursor, y, "Copy Raw", false);
         this.layoutButtons(buttons, cursor, y, "Copy Nice", false);
         return buttons;
      }
   }

   private int layoutButtons(List<AutismItemNbtInspectOverlay.TabButton> buttons, int cursorX, int y, String label, boolean primary) {
      int width = this.footerButtonWidth(label);
      int x = cursorX - width;
      buttons.add(new AutismItemNbtInspectOverlay.TabButton(label, x, y, width, primary));
      return x - this.buttonGap();
   }

   private int footerButtonWidth(String label) {
      return autismclient.gui.ui.layout.PanelLayoutMath.measureLabelWidth(this.font, this.theme, autismclient.gui.f.b.l.read, label, 5, 36, 78);
   }

   private int tabButtonWidth(String label) {
      return autismclient.gui.ui.UiDensityMetrics.buttonWidthForText(
         autismclient.gui.f.b.k.init(
            this.font,
            label,
            this.theme.init_Identifier_l(autismclient.gui.f.b.l.read),
            this.theme.load(autismclient.gui.f.b.l.read)
         )
      );
   }

   private int padding() {
      return 4;
   }

   private int textPadding() {
      return 4;
   }

   private int footerHeight() {
      int rows = this.footerFitsOneRow() ? 1 : 2;
      return rows * this.tabHeight() + (rows - 1) * 2 + this.padding();
   }

   private int tabHeight() {
      return autismclient.gui.ui.UiDensityMetrics.scaledHeight(13);
   }

   private int buttonGap() {
      return 2;
   }

   private int lineHeight() {
      return autismclient.gui.ui.UiDensityMetrics.scaledHeight(12);
   }

   private int continuationIndent() {
      return 12;
   }

   private record TabButton(String label, int init_I, int load, int read, boolean active) {
      boolean contains(double mouseX, double mouseY, int height) {
         return mouseX >= this.init_I && mouseX < this.init_I + this.read && mouseY >= this.load && mouseY < this.load + height;
      }
   }

   private record WrappedLine(List<ItemNbtInspector.HighlightToken> tokens, int indent) {
      static AutismItemNbtInspectOverlay.WrappedLine plain(String text, int offset, int color) {
         return new AutismItemNbtInspectOverlay.WrappedLine(List.of(new ItemNbtInspector.HighlightToken(text, color)), offset);
      }

      static AutismItemNbtInspectOverlay.WrappedLine ofTokens(List<ItemNbtInspector.HighlightToken> tokens, int offset) {
         return new AutismItemNbtInspectOverlay.WrappedLine(List.copyOf(tokens), offset);
      }
   }
}

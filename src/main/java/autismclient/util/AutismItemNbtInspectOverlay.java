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
   private static final Minecraft init_Minecraft = Minecraft.getInstance();
   private static final int flush_I = 4;
   private static AutismItemNbtInspectOverlay init_AutismItemNbtInspe;
   private final Font init_Font;
   private final autismclient.gui.f.b.CharEncoder init_CharEncoder = new autismclient.gui.f.b.CharEncoder();
   private ItemNbtInspector.Inner_StreamUtil init_az_StreamUtil;
   private autismclient.gui.ui.layout.ScrollableListView init_EventBus;
   private List<AutismItemNbtInspectOverlay.Inner_StreamUtil> init_List = Collections.emptyList();
   private boolean write_Z = true;
   private boolean update_Z;
   private int close_I;
   private boolean sync_Z;
   private boolean flush_Z;
   private double init_D;
   private double load_D;

   public AutismItemNbtInspectOverlay(Font textRenderer) {
      super("item-nbt-viewer", 260, 180);
      this.init_Font = textRenderer;
      this.init_I_2 = 110;
      this.load_I = 42;
   }

   public static AutismItemNbtInspectOverlay init_AutismItemNbtInspe_Font(Font textRenderer) {
      if (textRenderer == null) {
         return null;
      }

      if (init_AutismItemNbtInspe == null) {
         init_AutismItemNbtInspe = new AutismItemNbtInspectOverlay(textRenderer);
      }

      return init_AutismItemNbtInspe;
   }

   public static void load_V_8() {
      if (init_AutismItemNbtInspe != null) {
         init_AutismItemNbtInspe.init_V_Z(false);
         OverlayManager.getInstance().unregister(init_AutismItemNbtInspe);
      }
   }

   public static boolean init_Z_ItemStack(ItemStack stack) {
      if (stack != null && !stack.isEmpty() && init_Minecraft.font != null) {
         AutismItemNbtInspectOverlay overlay = init_AutismItemNbtInspe_Font(init_Minecraft.font);
         if (overlay == null) {
            return false;
         }

         int screenW = init_Minecraft.getWindow() == null ? 640 : cA.init_I();
         int x = Math.max(8, (screenW - 320) / 2);
         overlay.init_V_ItemStack(stack, x, 42);
         return true;
      } else {
         return false;
      }
   }

   public static boolean init_Z_az_StreamUtil(ItemNbtInspector.Inner_StreamUtil inspection) {
      if (inspection != null && init_Minecraft.font != null) {
         AutismItemNbtInspectOverlay overlay = init_AutismItemNbtInspe_Font(init_Minecraft.font);
         if (overlay == null) {
            return false;
         }

         int screenW = init_Minecraft.getWindow() == null ? 640 : cA.init_I();
         overlay.init_V_az_StreamUtil(inspection, Math.max(8, (screenW - 320) / 2), 42);
         return true;
      } else {
         return false;
      }
   }

   public void init_V_ItemStack(ItemStack stack, int anchorX, int anchorY) {
      if (stack != null && !stack.isEmpty()) {
         this.init_V_az_StreamUtil(ItemNbtInspector.init_az_ConfigStore_ItemStack(stack), anchorX, anchorY);
      }
   }

   public void init_V_az_StreamUtil(ItemNbtInspector.Inner_StreamUtil inspection, int anchorX, int anchorY) {
      if (inspection != null) {
         OverlayManager manager = OverlayManager.getInstance();
         manager.register(this);
         this.init_az_StreamUtil = inspection;
         this.sync_Z = false;
         this.init_Z = true;
         this.load_Z = false;
         this.init_EventBus = null;
         this.write_Z = true;
         int screenW = init_Minecraft.getWindow() == null ? 640 : cA.init_I();
         int screenH = init_Minecraft.getWindow() == null ? 360 : cA.load_I();
         int width = this.init_I_I_I(this.push_I_10(), this.init_I());
         this.read_I = width;
         int height = this.load_I_I_I(this.pop_I_10(), this.load_I());
         this.init_V_cH(
            new OverlayBounds(Math.max(8, Math.min(anchorX, screenW - width - 8)), Math.max(8, Math.min(anchorY, screenH - height - 8)), width, height, true, false)
         );
         manager.bringToFront(this);
      }
   }

   private int push_I_10() {
      int screenW = init_Minecraft.getWindow() == null ? 640 : cA.init_I();
      return Math.min(Math.max(1, screenW - 18), Math.max(this.init_I(), Math.min(300, screenW / 3)));
   }

   private int pop_I_10() {
      int screenH = init_Minecraft.getWindow() == null ? 360 : cA.load_I();
      int availableH = Math.max(14, screenH - 18);
      int maxH = Math.min(availableH, Math.max(this.load_I(), screenH * 65 / 100));
      return Math.min(maxH, Math.max(this.load_I(), this.run_I_8() + this.init_List_5().size() * this.start_I_6() + 2));
   }

   @Override
   public boolean read_Z() {
      return true;
   }

   @Override
   protected void c_() {
      this.write_Z = true;
      if (!this.set_Z()) {
         OverlayBounds b = this.init_cH();
         this.init_V_cH(new OverlayBounds(b.x, b.y, this.push_I_10(), this.pop_I_10(), b.visible, b.collapsed));
      }
   }

   public void read_V_4() {
      this.init_Z = false;
      this.flush_Z = false;
      this.init_EventBus = null;
   }

   @Override
   public void init_V_GuiGraphicsExtract_15(GuiGraphicsExtractor context, int mouseX, int mouseY, float delta) {
      if (this.init_Z || this.a__Z()) {
         OverlayBounds bounds = this.init_cH();
         String title = this.init_az_StreamUtil == null ? "NBT Inspector" : this.init_az_StreamUtil.init_Str();
         this.renderWindowFrame(context, mouseX, mouseY, bounds, title, this.load_Z, this.flush_Z);
         boolean clipped = this.beginBodyClip(context, bounds, this.load_Z);
         if (!clipped) {
            this.renderInactiveDim(context, bounds, this.load_Z, this.flush_Z);
         } else {
            try {
               if (this.init_az_StreamUtil != null) {
                  int tabY = this.tick_I_8();

                  for (AutismItemNbtInspectOverlay.Inner_DataUtil tab : this.init_List_I(tabY)) {
                     autismclient.gui.ui.widgets.ButtonDrawHelper.init(
                        context,
                        this.init_Font,
                        tab.init_I(),
                        tab.load(),
                        tab.read(),
                        this.clear(),
                        tab.init_Str(),
                        "Raw".equals(tab.init_Str()) == this.sync_Z,
                        mouseX,
                        mouseY
                     );
                  }

                  int listX = this.init_I_2 + this.set_I_6();
                  int listY = tabY + this.clear() + 2;
                  int listWidth = this.read_I - this.set_I_6() * 2;
                  int footerY = this.get_I_6();
                  int listHeight = Math.max(1, footerY - listY - 4);
                  int lineH = this.start_I_6();
                  int textX = listX + this.put();
                  int maxTextWidth = autismclient.gui.ui.widgets.ScrollbarContentMath.init(listWidth, this.put());
                  this.init_V_I_3(maxTextWidth);
                  if (this.init_EventBus == null
                     || this.init_EventBus.getX() != listX
                     || this.init_EventBus.getY() != listY
                     || this.init_EventBus.getWidth() != listWidth
                     || this.init_EventBus.getHeight() != listHeight) {
                     int oldScroll = this.init_EventBus == null ? 0 : this.init_EventBus.getScrollOffset();
                     this.init_EventBus = new autismclient.gui.ui.layout.ScrollableListView(listX, listY, listWidth, listHeight, lineH, 4);
                     this.init_EventBus.setScrollOffset(oldScroll);
                  }

                  this.init_EventBus.setContentHeight(this.init_List.size() * lineH);
                  this.init_EventBus.beginFrame(context, this.init_CharEncoder.set(), this.init_CharEncoder.reset());

                  try {
                     this.init_EventBus.forEachVisibleRow(context, this.init_List.size(), (idx, bnd) -> {
                        AutismItemNbtInspectOverlay.Inner_StreamUtil line = this.init_List.get(idx);
                        int y = UiLayoutMath.centerTextY(bnd.y, lineH, this.init_CharEncoder.init_I_l(autismclient.gui.f.b.l.read), this.init_CharEncoder.flush());
                        this.init_V_GuiGraphicsExtract_3(context, line, textX, y);
                     });
                  } finally {
                     this.init_EventBus.endFrame(context);
                  }

                  this.init_EventBus.renderScrollbar(context, mouseX, mouseY);
                  autismclient.gui.f.b.k.init(
                     context,
                     this.init_Font,
                     this.init_List.size() + " lines",
                     this.init_CharEncoder.init_Identifier_l(autismclient.gui.f.b.l.write),
                     this.init_CharEncoder.load(autismclient.gui.f.b.l.write),
                     this.init_I_2 + this.set_I_6() + 2,
                     UiLayoutMath.centerTextY(footerY, this.clear(), this.init_CharEncoder.init_I_l(autismclient.gui.f.b.l.write), this.init_CharEncoder.flush()),
                     false
                  );

                  for (AutismItemNbtInspectOverlay.Inner_DataUtil button : this.load(footerY)) {
                     autismclient.gui.ui.widgets.ButtonDrawHelper.init(
                        context,
                        this.init_Font,
                        button.init_I(),
                        button.load(),
                        button.read(),
                        this.clear(),
                        button.init_Str(),
                        button.init_Z() ? autismclient.gui.ui.widgets.OverlayActionButton.ButtonTone.SUCCESS : autismclient.gui.ui.widgets.OverlayActionButton.ButtonTone.GHOST,
                        true,
                        mouseX,
                        mouseY
                     );
                  }

                  return;
               }

               autismclient.gui.ui.widgets.ListRowRenderer.drawEmptyPlaceholder(
                  context,
                  this.init_Font,
                  "No item selected.",
                  this.init_I_2 + this.set_I_6(),
                  this.load_I + 14 + this.set_I_6(),
                  this.read_I - this.set_I_6() * 2
               );
            } finally {
               this.endBodyClip(context, clipped);
               this.renderInactiveDim(context, bounds, this.load_Z, this.flush_Z);
            }

            return;
         }
      }
   }

   @Override
   public boolean init_Z_D_3(double mouseX, double mouseY, int button) {
      if (!this.init_Z) {
         return false;
      }

      OverlayBounds bounds = this.init_cH();
      if (button == 0 && mouseY >= this.load_I && mouseY <= this.load_I + 14 && mouseX >= this.init_I_2 && mouseX <= this.init_I_2 + this.read_I) {
         if (this.isOverHeaderCloseButton(mouseX, mouseY, bounds)) {
            this.read_V_4();
            return true;
         } else {
            this.flush_Z = true;
            this.init_D = mouseX - this.init_I_2;
            this.load_D = mouseY - this.load_I;
            return true;
         }
      } else {
         if (this.load_Z) {
            return true;
         }

         if (button == 0) {
            int tabY = this.tick_I_8();

            for (AutismItemNbtInspectOverlay.Inner_DataUtil tab : this.init_List_I(tabY)) {
               if (tab.init(mouseX, mouseY, this.clear())) {
                  this.sync_Z = "Raw".equals(tab.init_Str());
                  this.write_Z = true;
                  if (this.init_EventBus != null) {
                     this.init_EventBus.setScrollOffset(0);
                  }

                  return true;
               }
            }

            if (this.init_EventBus != null && this.init_EventBus.containsPoint(mouseX, mouseY) && this.init_EventBus.mouseClicked(mouseX, mouseY, button)) {
               return true;
            }

            int footerY = this.get_I_6();

            for (AutismItemNbtInspectOverlay.Inner_DataUtil footer : this.load(footerY)) {
               if (footer.init(mouseX, mouseY, this.clear())) {
                  this.init_V_Str_4(footer.init_Str());
                  return true;
               }
            }
         }

         return this.init_Z_D_D(mouseX, mouseY);
      }
   }

   @Override
   public boolean load_Z_D_3(double mouseX, double mouseY, int button) {
      if (button == 0 && this.flush_Z) {
         this.flush_Z = false;
         this.a__V();
         return true;
      } else if (button == 0 && this.init_EventBus != null) {
         this.init_EventBus.mouseReleased();
         return true;
      } else {
         this.flush_Z = false;
         return false;
      }
   }

   @Override
   public boolean init_Z_D(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
      if (this.init_EventBus != null) {
         this.init_EventBus.mouseDragged(mouseX, mouseY);
      }

      if (this.init_Z && button == 0 && this.flush_Z) {
         this.init_V_cH(new OverlayBounds((int)Math.round(mouseX - this.init_D), (int)Math.round(mouseY - this.load_D), this.read_I, this.write, this.init_Z, this.load_Z));
         return true;
      } else {
         return false;
      }
   }

   @Override
   public boolean init_Z_D_2(double mouseX, double mouseY, double amount) {
      return this.init_Z && !this.load_Z && this.init_EventBus != null && this.init_EventBus.containsPoint(mouseX, mouseY)
         ? this.init_EventBus.mouseScrolled(mouseX, mouseY, amount)
         : false;
   }

   @Override
   public boolean init_Z_I(int keyCode, int scanCode, int modifiers) {
      if (!this.init_Z) {
         return false;
      } else if (keyCode == 256) {
         this.read_V_4();
         return true;
      } else {
         return false;
      }
   }

   @Override
   public boolean init_Z_C_I(char chr, int modifiers) {
      return false;
   }

   @Override
   public void init_V_Z(boolean visible) {
      if (!visible) {
         this.read_V_4();
      } else {
         this.init_Z = true;
      }
   }

   @Override
   public boolean init_Z_D_D(double mouseX, double mouseY) {
      if (!this.init_Z) {
         return false;
      }

      int height = this.load_Z ? 14 : this.write;
      return mouseX >= this.init_I_2 && mouseX <= this.init_I_2 + this.read_I && mouseY >= this.load_I && mouseY <= this.load_I + height;
   }

   @Override
   public boolean load_Z_D_D(double mouseX, double mouseY) {
      return !this.init_Z
         ? false
         : mouseX >= this.init_I_2
            && mouseX <= this.init_I_2 + this.read_I
            && mouseY >= this.load_I
            && mouseY <= this.load_I + 14
            && !this.isOverHeaderControl(mouseX, mouseY, this.init_cH());
   }

   @Override
   public boolean flush_Z() {
      return this.load_Z;
   }

   @Override
   public void write_V_Z(boolean collapsed) {
      if (this.load_Z != collapsed) {
         this.load_Z = collapsed;
         this.flush_Z = false;
         if (collapsed) {
            this.get_V();
         }

         this.a__V();
      }
   }

   @Override
   public boolean load_Z() {
      return true;
   }

   @Override
   public int b__I() {
      return 13;
   }

   @Override
   public int init_I() {
      return 220;
   }

   @Override
   public int load_I() {
      return this.run_I_8() + this.start_I_6() * 4 + 2;
   }

   private List<ItemNbtInspector.Inner_BufferHelper> init_List_5() {
      if (this.init_az_StreamUtil == null) {
         return List.of();
      } else {
         return this.sync_Z ? this.init_az_StreamUtil.load_List() : this.init_az_StreamUtil.init_List();
      }
   }

   private void init_V_I_3(int maxWidth) {
      if (this.write_Z || this.update_Z != this.sync_Z || this.close_I != maxWidth) {
         if (this.init_az_StreamUtil == null) {
            this.init_List = Collections.emptyList();
            this.write_Z = false;
            this.update_Z = this.sync_Z;
            this.close_I = maxWidth;
         } else {
            List<AutismItemNbtInspectOverlay.Inner_StreamUtil> wrapped = new ArrayList<>();

            for (ItemNbtInspector.Inner_BufferHelper line : this.init_List_5()) {
               this.init_V_az_BufferHelper(line, maxWidth, wrapped);
            }

            this.init_List = wrapped;
            this.write_Z = false;
            this.update_Z = this.sync_Z;
            this.close_I = maxWidth;
         }
      }
   }

   private void init_V_az_BufferHelper(ItemNbtInspector.Inner_BufferHelper line, int maxWidth, List<AutismItemNbtInspectOverlay.Inner_StreamUtil> target) {
      if (line.init_List() != null && !line.init_List().isEmpty()) {
         this.init_V_List_2(line.init_List(), maxWidth, target);
      } else {
         String text = line.init_Str();
         if (text == null || text.isEmpty()) {
            target.add(AutismItemNbtInspectOverlay.Inner_StreamUtil.init("", 0, line.init_I()));
         } else if (this.init_Z_Str_2(text)) {
            this.init_V_Str_3(text, 0, line.init_I(), maxWidth, target);
         } else {
            int leadingSpaces = this.init_I_Str_2(text);
            String indentComponent = " ".repeat(leadingSpaces);
            int indentWidth = this.load(indentComponent);
            int colonIndex = text.indexOf(58, leadingSpaces);
            if (colonIndex > leadingSpaces && colonIndex < text.length() - 1) {
               String label = text.substring(leadingSpaces, colonIndex + 1).trim();
               String value = text.substring(colonIndex + 1).stripLeading();
               String prefix = indentComponent + label + " ";
               int prefixWidth = this.load(prefix);
               int continuationOffset = Math.min(prefixWidth, indentWidth + this.stop());
               this.init(prefix, value, line.init_I(), prefixWidth, continuationOffset, maxWidth, target);
            } else {
               this.init_V_Str_3(text.substring(leadingSpaces), indentWidth, line.init_I(), maxWidth, target);
            }
         }
      }
   }

   private void init(
      String prefix,
      String value,
      int valueColor,
      int prefixWidth,
      int continuationOffset,
      int maxWidth,
      List<AutismItemNbtInspectOverlay.Inner_StreamUtil> target
   ) {
      int safeContinuationOffset = autismclient.gui.ui.widgets.ScrollbarContentMath.load(maxWidth, continuationOffset);
      if (prefixWidth >= maxWidth) {
         this.init_V_Str_3(prefix.stripTrailing(), 0, UiColorPalette.silver(), maxWidth, target);
         this.init_V_Str_3(value, safeContinuationOffset, valueColor, maxWidth, target);
      } else if (value != null && !value.isEmpty()) {
         String remaining = value;

         for (boolean firstLine = true; !remaining.isEmpty(); firstLine = false) {
            int offset = firstLine ? prefixWidth : safeContinuationOffset;
            int availableWidth = autismclient.gui.ui.widgets.ScrollbarContentMath.read(maxWidth, offset);
            String current = remaining;
            int split = autismclient.gui.ui.TextWrapLayout.init(
               current, 0, current.length(), availableWidth, (start, end) -> this.load(current.substring(start, end))
            );
            String part = remaining.substring(0, split).stripTrailing();
            target.add(this.init_AutismItemNbtInspe_Str(firstLine ? prefix : null, part, valueColor, offset));
            remaining = remaining.substring(split).stripLeading();
         }
      } else {
         target.add(
            AutismItemNbtInspectOverlay.Inner_StreamUtil.init(List.of(new ItemNbtInspector.Inner_TaskQueue(prefix, UiColorPalette.silver()), new ItemNbtInspector.Inner_TaskQueue("", valueColor)), 0)
         );
      }
   }

   private void init_V_Str_3(String text, int offset, int color, int maxWidth, List<AutismItemNbtInspectOverlay.Inner_StreamUtil> target) {
      if (text != null) {
         if (text.isEmpty()) {
            target.add(AutismItemNbtInspectOverlay.Inner_StreamUtil.init("", offset, color));
         } else {
            int safeOffset = autismclient.gui.ui.widgets.ScrollbarContentMath.load(maxWidth, offset);
            String remaining = text;

            while (!remaining.isEmpty()) {
               int availableWidth = autismclient.gui.ui.widgets.ScrollbarContentMath.read(maxWidth, safeOffset);
               String current = remaining;
               int split = autismclient.gui.ui.TextWrapLayout.init(
                  current, 0, current.length(), availableWidth, (start, end) -> this.load(current.substring(start, end))
               );
               String part = remaining.substring(0, split).stripTrailing();
               target.add(AutismItemNbtInspectOverlay.Inner_StreamUtil.init(part, safeOffset, color));
               remaining = remaining.substring(split).stripLeading();
            }
         }
      }
   }

   private void init_V_GuiGraphicsExtract_3(GuiGraphicsExtractor context, AutismItemNbtInspectOverlay.Inner_StreamUtil line, int x, int y) {
      if (line != null && line.init_List() != null) {
         int cursor = x + line.init_I();

         for (ItemNbtInspector.Inner_TaskQueue token : line.init_List()) {
            if (token != null && token.init_Str() != null && !token.init_Str().isEmpty()) {
               autismclient.gui.f.b.k.init(
                  context,
                  this.init_Font,
                  token.init_Str(),
                  this.init_CharEncoder.init_Identifier_l(autismclient.gui.f.b.l.read),
                  token.init_I(),
                  cursor,
                  y,
                  false
               );
               cursor += this.load(token.init_Str());
            }
         }
      }
   }

   private void init_V_List_2(List<ItemNbtInspector.Inner_TaskQueue> source, int maxWidth, List<AutismItemNbtInspectOverlay.Inner_StreamUtil> target) {
      if (source != null && !source.isEmpty()) {
         int indent = this.init_I_List_2(source);
         int continuationOffset = autismclient.gui.ui.widgets.ScrollbarContentMath.load(maxWidth, indent + this.stop());
         List<ItemNbtInspector.Inner_TaskQueue> current = new ArrayList<>();
         int currentWidth = 0;
         boolean continuation = false;

         for (ItemNbtInspector.Inner_TaskQueue token : source) {
            if (token != null && token.init_Str() != null && !token.init_Str().isEmpty()) {
               String remaining = token.init_Str();

               while (!remaining.isEmpty()) {
                  int limit = maxWidth - (continuation ? continuationOffset : 0);
                  int room = Math.max(1, limit - currentWidth);
                  String piece = this.init_Str_Str_I_2(remaining, room);
                  if (piece.isEmpty()) {
                     if (!current.isEmpty()) {
                        target.add(AutismItemNbtInspectOverlay.Inner_StreamUtil.init(current, continuation ? continuationOffset : 0));
                        current = new ArrayList<>();
                        currentWidth = 0;
                        continuation = true;
                        continue;
                     }

                     piece = remaining.substring(0, 1);
                  }

                  current.add(new ItemNbtInspector.Inner_TaskQueue(piece, token.init_I()));
                  currentWidth += this.load(piece);
                  remaining = remaining.substring(piece.length());
                  if (!remaining.isEmpty()) {
                     target.add(AutismItemNbtInspectOverlay.Inner_StreamUtil.init(current, continuation ? continuationOffset : 0));
                     current = new ArrayList<>();
                     currentWidth = 0;
                     continuation = true;
                     remaining = remaining.stripLeading();
                  } else if (currentWidth >= Math.max(22, limit - 4)) {
                     target.add(AutismItemNbtInspectOverlay.Inner_StreamUtil.init(current, continuation ? continuationOffset : 0));
                     current = new ArrayList<>();
                     currentWidth = 0;
                     continuation = true;
                  }
               }
            }
         }

         if (!current.isEmpty()) {
            target.add(AutismItemNbtInspectOverlay.Inner_StreamUtil.init(current, continuation ? continuationOffset : 0));
         }
      } else {
         target.add(AutismItemNbtInspectOverlay.Inner_StreamUtil.init("", 0, UiColorPalette.textMuted()));
      }
   }

   private AutismItemNbtInspectOverlay.Inner_StreamUtil init_AutismItemNbtInspe_Str(String prefix, String value, int valueColor, int offset) {
      List<ItemNbtInspector.Inner_TaskQueue> tokens = new ArrayList<>();
      if (prefix != null && !prefix.isEmpty()) {
         tokens.add(new ItemNbtInspector.Inner_TaskQueue(prefix, UiColorPalette.silver()));
      }

      tokens.add(new ItemNbtInspector.Inner_TaskQueue(value == null ? "" : value, valueColor));
      return AutismItemNbtInspectOverlay.Inner_StreamUtil.init(tokens, prefix != null && !prefix.isEmpty() ? 0 : offset);
   }

   private int init_I_List_2(List<ItemNbtInspector.Inner_TaskQueue> tokens) {
      int width = 0;

      for (ItemNbtInspector.Inner_TaskQueue token : tokens) {
         if (token != null && token.init_Str() != null && !token.init_Str().isEmpty()) {
            String text = token.init_Str();
            int spaces = 0;

            while (spaces < text.length() && Character.isWhitespace(text.charAt(spaces))) {
               spaces++;
            }

            if (spaces <= 0) {
               return width;
            }

            width += this.load(text.substring(0, spaces));
            if (spaces < text.length()) {
               return width;
            }
         }
      }

      return width;
   }

   private String init_Str_Str_I_2(String value, int maxWidth) {
      if (value != null && !value.isEmpty()) {
         if (this.load(value) <= maxWidth) {
            return value;
         }

         int low = 0;
         int high = value.length();

         while (low < high) {
            int mid = low + high + 1 >>> 1;
            String candidate = value.substring(0, mid);
            if (this.load(candidate) <= maxWidth) {
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

   private boolean init_Z_Str_2(String text) {
      String trimmed = text == null ? "" : text.trim();
      return trimmed.startsWith("[") && trimmed.endsWith("]");
   }

   private int init_I_Str_2(String text) {
      int count = 0;

      while (count < text.length() && text.charAt(count) == ' ') {
         count++;
      }

      return count;
   }

   private int load(String text) {
      return text != null && !text.isEmpty()
         ? autismclient.gui.f.b.k.init(this.init_Font, text, this.init_CharEncoder.init_Identifier_l(autismclient.gui.f.b.l.read), UiColorPalette.softWhite())
         : 0;
   }

   private void init_V_Str_4(String label) {
      if (this.init_az_StreamUtil != null && init_Minecraft.keyboardHandler != null) {
         switch (label) {
            case "Copy Raw":
               init_Minecraft.keyboardHandler.setClipboard(this.init_az_StreamUtil.update());
               bi.init("Copied raw " + this.init_az_StreamUtil.load_Str() + " NBT.");
               break;
            case "Copy Nice":
               init_Minecraft.keyboardHandler.setClipboard(this.init_az_StreamUtil.write());
               bi.init("Copied " + this.init_az_StreamUtil.load_Str() + " NBT summary.");
               break;
            case "Fill Editor":
               if (!AutismLiteVariant.enabled()
                  && this.init_az_StreamUtil.init_ItemStack() != null
                  && !this.init_az_StreamUtil.init_ItemStack().isEmpty()
                  && autismclient.modules.AdminToolsItemHelper.load(this.init_az_StreamUtil.init_ItemStack())) {
                  bi.init("Filled Admin Tools with complete item NBT.", -13248397);
               } else {
                  AutismClientMessaging.sendPrefixed("Admin Tools: could not fill NBT editor.");
               }
         }
      }
   }

   private List<AutismItemNbtInspectOverlay.Inner_DataUtil> init_List_I(int y) {
      int x = this.init_I_2 + this.set_I_6();
      int w = Math.max(40, Math.max(this.write("Nice"), this.write("Raw")));
      return List.of(
         new AutismItemNbtInspectOverlay.Inner_DataUtil("Nice", x, y, w, false), new AutismItemNbtInspectOverlay.Inner_DataUtil("Raw", x + w + 2, y, w, false)
      );
   }

   private int tick_I_8() {
      return this.load_I + 14 + this.set_I_6();
   }

   private int run_I_8() {
      return 14 + this.set_I_6() + this.clear() + 2 + 4 + this.add_I_6();
   }

   private int get_I_6() {
      return this.load_I + this.write - this.add_I_6();
   }

   private boolean poll_Z_6() {
      return !AutismLiteVariant.enabled()
         && this.init_az_StreamUtil != null
         && this.init_az_StreamUtil.init_ItemStack() != null
         && !this.init_az_StreamUtil.init_ItemStack().isEmpty();
   }

   private boolean push_Z_5() {
      int need = autismclient.gui.f.b.k.init(
            this.init_Font,
            "9999 lines",
            this.init_CharEncoder.init_Identifier_l(autismclient.gui.f.b.l.write),
            this.init_CharEncoder.load(autismclient.gui.f.b.l.write)
         )
         + 6
         + this.read("Copy Raw")
         + this.read("Copy Nice")
         + this.reset();
      if (this.poll_Z_6()) {
         need += this.read("Fill Editor") + this.reset();
      }

      return need <= this.read_I - this.set_I_6() * 2;
   }

   private List<AutismItemNbtInspectOverlay.Inner_DataUtil> load(int y) {
      ArrayList<AutismItemNbtInspectOverlay.Inner_DataUtil> buttons = new ArrayList<>();
      boolean editor = !AutismLiteVariant.enabled()
         && this.init_az_StreamUtil != null
         && this.init_az_StreamUtil.init_ItemStack() != null
         && !this.init_az_StreamUtil.init_ItemStack().isEmpty();
      if (!this.push_Z_5()) {
         int firstCursor = this.init_I_2 + this.read_I - this.set_I_6();
         if (editor) {
            this.init_I_List(buttons, firstCursor, y, "Fill Editor", true);
         }

         int rowY = y + this.clear() + 2;
         int secondCursor = this.init_I_2 + this.read_I - this.set_I_6();
         secondCursor = this.init_I_List(buttons, secondCursor, rowY, "Copy Raw", false);
         this.init_I_List(buttons, secondCursor, rowY, "Copy Nice", false);
         return buttons;
      } else {
         int cursor = this.init_I_2 + this.read_I - this.set_I_6();
         if (editor) {
            cursor = this.init_I_List(buttons, cursor, y, "Fill Editor", true);
         }

         cursor = this.init_I_List(buttons, cursor, y, "Copy Raw", false);
         this.init_I_List(buttons, cursor, y, "Copy Nice", false);
         return buttons;
      }
   }

   private int init_I_List(List<AutismItemNbtInspectOverlay.Inner_DataUtil> buttons, int cursorX, int y, String label, boolean primary) {
      int width = this.read(label);
      int x = cursorX - width;
      buttons.add(new AutismItemNbtInspectOverlay.Inner_DataUtil(label, x, y, width, primary));
      return x - this.reset();
   }

   private int read(String label) {
      return autismclient.gui.ui.layout.PanelLayoutMath.measureLabelWidth(this.init_Font, this.init_CharEncoder, autismclient.gui.f.b.l.read, label, 5, 36, 78);
   }

   private int write(String label) {
      return autismclient.gui.ui.UiDensityMetrics.buttonWidthForText(
         autismclient.gui.f.b.k.init(
            this.init_Font,
            label,
            this.init_CharEncoder.init_Identifier_l(autismclient.gui.f.b.l.read),
            this.init_CharEncoder.load(autismclient.gui.f.b.l.read)
         )
      );
   }

   private int set_I_6() {
      return 4;
   }

   private int put() {
      return 4;
   }

   private int add_I_6() {
      int rows = this.push_Z_5() ? 1 : 2;
      return rows * this.clear() + (rows - 1) * 2 + this.set_I_6();
   }

   private int clear() {
      return autismclient.gui.ui.UiDensityMetrics.scaledHeight(13);
   }

   private int reset() {
      return 2;
   }

   private int start_I_6() {
      return autismclient.gui.ui.UiDensityMetrics.scaledHeight(12);
   }

   private int stop() {
      return 12;
   }

   private record Inner_DataUtil(String init_Str, int init_I, int load, int read, boolean init_Z) {
      boolean init(double mouseX, double mouseY, int height) {
         return mouseX >= this.init_I && mouseX < this.init_I + this.read && mouseY >= this.load && mouseY < this.load + height;
      }
   }

   private record Inner_StreamUtil(List<ItemNbtInspector.Inner_TaskQueue> init_List, int init_I) {
      static AutismItemNbtInspectOverlay.Inner_StreamUtil init(String text, int offset, int color) {
         return new AutismItemNbtInspectOverlay.Inner_StreamUtil(List.of(new ItemNbtInspector.Inner_TaskQueue(text, color)), offset);
      }

      static AutismItemNbtInspectOverlay.Inner_StreamUtil init(List<ItemNbtInspector.Inner_TaskQueue> tokens, int offset) {
         return new AutismItemNbtInspectOverlay.Inner_StreamUtil(List.copyOf(tokens), offset);
      }
   }
}

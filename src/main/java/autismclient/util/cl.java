package autismclient.util;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemContainerContents;

public final class cl {
   private static final int init_I = 9;
   private static final int load_I = 3;
   private static final int read_I = 18;
   private static final int write = 27;
   private static final int update = 12;
   private static final int sync = 4;
   private static final int flush = 2;
   private static final int close = 6;
   private static final int open = 10;
   private static final int poll = 12;
   private static final int push = 12;
   private static final int pop = -15068651;
   private static final int tick = -7262667;
   private static final float init_F = 0.12F;
   private static final long init_J = 120L;
   private static final float load_F = 5.0F;
   private static final ItemStack[] init_ItemStackArr = new ItemStack[27];
   private static int run = -2147483648;
   private static float read_F;
   private static long load_J;

   private cl() {
   }

   public static boolean init_Z(ItemStack stack) {
      return autismclient.modules.R.load_Z_7() && load(stack);
   }

   private static boolean load(ItemStack stack) {
      if (stack != null && !stack.isEmpty()) {
         ItemContainerContents contents = stack.get(DataComponents.CONTAINER);
         return contents != null && contents.nonEmptyItemCopyStream().findAny().isPresent();
      } else {
         return false;
      }
   }

   public static void init(GuiGraphicsExtractor graphics, Font font, ItemStack stack, int targetKey, int mouseX, int mouseY, int screenW, int screenH) {
      try {
         int count = init_I(stack);
         if (count <= 0) {
            return;
         }

         int rows = Math.min(3, Math.max(1, (count + 9 - 1) / 9));
         int columns = Math.min(9, count);
         int width = columns * 18;
         int height = rows * 18;
         int[] placement = init(font, stack, mouseX, mouseY, screenW, screenH, width, height);
         if (placement == null) {
            return;
         }

         int x = placement[0];
         int y = placement[1];
         int awayX = placement[2];
         int awayY = placement[3];
         float eased = init(targetKey);
         float slide = (1.0F - eased) * 5.0F;
         graphics.pose().pushMatrix();
         graphics.pose().translate(awayX * slide, awayY * slide);

         try {
            init(graphics, font, x, y, count);
         } finally {
            graphics.pose().popMatrix();
         }
      } catch (Throwable var24) {
      }
   }

   private static int[] init(Font font, ItemStack stack, int mouseX, int mouseY, int screenW, int screenH, int pw, int ph) {
      int[] tip = init(font, stack, mouseX, mouseY, screenW, screenH);
      if (tip == null) {
         int x = mouseX + 12;
         if (x + pw > screenW - 2) {
            x = mouseX - pw - 12;
         }

         x = init(x, 2, screenW - 2 - pw);
         int y = mouseY - ph - 12;
         if (y < 2) {
            y = mouseY + 18;
         }

         y = init(y, 2, screenH - 2 - ph);
         return new int[]{x, y, 0, -1};
      } else {
         int ax = tip[0];
         int ay = tip[1];
         int aw = tip[2];
         int ah = tip[3];
         int alignedX = init(ax, 2, screenW - 2 - pw);
         int alignedY = init(ay, 2, screenH - 2 - ph);
         int aboveY = ay - 4 - ph;
         if (aboveY >= 2) {
            return new int[]{alignedX, aboveY, 0, -1};
         }

         int belowY = ay + ah + 4;
         if (belowY + ph <= screenH - 2) {
            return new int[]{alignedX, belowY, 0, 1};
         }

         int rightX = ax + aw + 4;
         if (rightX + pw <= screenW - 2) {
            return new int[]{rightX, alignedY, 1, 0};
         }

         int leftX = ax - 4 - pw;
         return leftX >= 2 ? new int[]{leftX, alignedY, -1, 0} : null;
      }
   }

   private static int[] init(Font font, ItemStack stack, int mouseX, int mouseY, int screenW, int screenH) {
      Minecraft mc = Minecraft.getInstance();
      if (mc == null) {
         return null;
      }

      List<Component> lines = Screen.getTooltipFromItem(mc, stack);
      if (lines != null && !lines.isEmpty()) {
         int textWidth = 0;

         for (Component line : lines) {
            textWidth = Math.max(textWidth, font.width(line));
         }

         int textHeight = lines.size() == 1 ? 8 : lines.size() * 10;
         int posX = mouseX + 12;
         int posY = mouseY - 12;
         if (posX + textWidth > screenW) {
            posX = Math.max(posX - 24 - textWidth, 4);
         }

         int paddedHeight = textHeight + 3;
         if (posY + paddedHeight > screenH) {
            posY = screenH - paddedHeight;
         }

         return new int[]{posX - 6, posY - 6, textWidth + 12, textHeight + 12};
      } else {
         return null;
      }
   }

   private static void init(GuiGraphicsExtractor graphics, Font font, int x, int y, int count) {
      for (int index = 0; index < count; index++) {
         int slotX = x + index % 9 * 18;
         int slotY = y + index / 9 * 18;
         autismclient.gui.f.NetHelper cell = autismclient.gui.f.NetHelper.init(slotX, slotY, 18, 18);
         autismclient.gui.f.PacketUtil.init(graphics, cell, -15068651);
         autismclient.gui.f.PacketUtil.load(graphics, cell, -7262667);
         ItemStack item = init_ItemStackArr[index];
         if (item != null && !item.isEmpty()) {
            graphics.item(item, slotX + 1, slotY + 1);
            graphics.itemDecorations(font, item, slotX + 1, slotY + 1);
         }
      }
   }

   private static float init(int targetKey) {
      long now = System.currentTimeMillis();
      if (targetKey != run || now - load_J > 120L) {
         run = targetKey;
         read_F = 0.0F;
      }

      float dt = (float)Math.min(64L, Math.max(0L, now - load_J)) / 1000.0F;
      load_J = now;
      read_F = Math.min(1.0F, read_F + dt / 0.12F);
      float inverse = 1.0F - read_F;
      return 1.0F - inverse * inverse * inverse;
   }

   private static int init_I(ItemStack stack) {
      Arrays.fill(init_ItemStackArr, ItemStack.EMPTY);
      ItemContainerContents contents = stack == null ? null : stack.get(DataComponents.CONTAINER);
      if (contents == null) {
         return 0;
      }

      int filled = 0;
      Iterator<ItemStack> iterator = contents.nonEmptyItemCopyStream().iterator();

      while (iterator.hasNext() && filled < 27) {
         ItemStack item = iterator.next();
         if (item != null && !item.isEmpty()) {
            init_ItemStackArr[filled++] = item;
         }
      }

      return filled;
   }

   private static int init(int value, int min, int max) {
      return max < min ? min : Math.max(min, Math.min(max, value));
   }
}

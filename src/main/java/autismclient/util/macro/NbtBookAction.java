package autismclient.util.macro;

import autismclient.AutismClientAddon;
import autismclient.util.AutismClientMessaging;
import autismclient.util.BookBotFileReader;
import autismclient.util.BookBotPageGenerator;
import java.io.File;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Random;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ServerboundEditBookPacket;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;

public class NbtBookAction implements MacroAction {
   public static final String SOURCE_RANDOM = "Random";
   public static final String SOURCE_PASTED = "Pasted";
   public static final String SOURCE_FILE = "File";
   public int pages = 100;
   public int characters = 1024;
   public String title = "EldenRingLore";
   public boolean sign = true;
   public boolean appendCount = true;
   public boolean onlyAscii = false;
   public String randomType = "Utf8";
   public String dataSource = "Random";
   public String customComponent = "";
   public String customFilePath = "";
   public boolean wordWrap = true;
   public int delayTicks = 20;
   public int bookCount = 1;
   public boolean requireHeldWritableBook = false;
   public boolean dropInventoryBefore = false;
   public boolean disconnectAfter = false;
   private boolean init_Z = true;
   public transient String pasteInfo = "";
   private static final int init_I = 100;
   private static final int load = 14;
   private static final float init_F = 114.0F;

   public boolean init_Z_Minecraft_2(Minecraft mc, int bookIndex, int totalBooks) {
      if (autismclient.modules.aD.read_Z()) {
         return false;
      }

      if (mc.player == null || mc.getConnection() == null) {
         AutismClientMessaging.sendPrefixed("§cNo player or network connection!");
         return false;
      }

      if (bookIndex == 0 && !this.init_Z_Minecraft_3(mc)) {
         return false;
      }

      int bookSlot = this.init_I_Minecraft_2(mc);
      if (bookSlot < 0) {
         if (bookIndex == 0) {
            AutismClientMessaging.sendPrefixed("§cNo Book & Quill found in inventory!");
         } else {
            AutismClientMessaging.sendPrefixed("§eRan out of books after " + bookIndex + " signed.");
         }

         return false;
      } else {
         int hotbarSlot = bookSlot;
         if (bookSlot > 8) {
            int targetHotbar = 0;

            for (int i = 0; i <= 8; i++) {
               if (mc.player.getInventory().getItem(i).isEmpty()) {
                  targetHotbar = i;
                  break;
               }
            }

            autismclient.util.InventorySlotHelper.read(mc, bookSlot, targetHotbar);
            hotbarSlot = targetHotbar;
         }

         autismclient.util.InventorySlotHelper.init_V(mc, hotbarSlot);
         int numPages = Math.min(100, Math.max(1, this.pages));
         List<String> pageList = this.init_List_Minecraft_I(mc, numPages);
         if (pageList.isEmpty()) {
            return false;
         }

         String bookTitle = this.title;
         if (this.appendCount && totalBooks > 1 && bookIndex > 0) {
            bookTitle = this.title + " #" + (bookIndex + 1);
         }

         mc.getConnection().send(new ServerboundEditBookPacket(hotbarSlot, pageList, this.sign ? Optional.of(bookTitle) : Optional.empty()));
         int estimatedBytes = BookBotPageGenerator.init(pageList);
         AutismClientMessaging.sendPrefixed(
            "§aWrote book " + (bookIndex + 1) + "/" + totalBooks + " (~" + estimatedBytes / 1024 + "KB, " + pageList.size() + " pages)"
         );
         return true;
      }
   }

   private List<String> init_List_Minecraft_I(Minecraft mc, int numPages) {
      String source = this.load();
      if (!"Pasted".equals(source)) {
         if ("File".equals(source)) {
            String text = this.init_Str_8();
            if (text != null && !text.isBlank()) {
               return this.load(mc.font, text, numPages);
            }

            AutismClientMessaging.sendPrefixed("§cNBT Book file is missing or empty.");
            return List.of();
         } else {
            return this.init_List_I_3(numPages);
         }
      } else {
         return this.customComponent != null && !this.customComponent.isBlank()
            ? this.load(mc.font, this.customComponent, numPages)
            : this.init_List_I_3(numPages);
      }
   }

   private List<String> load(Font font, String text, int numPages) {
      return this.wordWrap ? init_List_Font(font, text, numPages) : init_List_Str(text, numPages, this.characters);
   }

   private List<String> init_List_I_3(int numPages) {
      return BookBotPageGenerator.init(numPages, this.characters, this.read(), new Random());
   }

   public static File init_File() {
      File dir = new File(AutismClientAddon.FOLDER, "book-sources");
      dir.mkdirs();
      return dir;
   }

   private String init_Str_8() {
      String path = this.customFilePath == null ? "" : this.customFilePath.trim();
      if (path.isBlank()) {
         return "";
      }

      try {
         Path base = init_File().getCanonicalFile().toPath();
         Path resolved = base.resolve(path).toFile().getCanonicalFile().toPath();
         if (!resolved.startsWith(base)) {
            AutismClientMessaging.sendPrefixed("§cBook file must be inside book-sources/.");
            return "";
         } else {
            return BookBotFileReader.init(resolved);
         }
      } catch (Exception ignored) {
         return "";
      }
   }

   @Override
   public void execute(Minecraft mc) {
      if (!autismclient.modules.aD.read_Z() && this.init_Z_Minecraft_2(mc, 0, 1)) {
         this.init_V_Minecraft_8(mc);
      }
   }

   private boolean init_Z_Minecraft_3(Minecraft mc) {
      if (mc.player == null) {
         return false;
      }

      if (this.requireHeldWritableBook) {
         ItemStack held = mc.player.getMainHandItem();
         Identifier writableBookId = Identifier.fromNamespaceAndPath("minecraft", "writable_book");
         if (held.isEmpty() || !BuiltInRegistries.ITEM.getKey(held.getItem()).equals(writableBookId)) {
            AutismClientMessaging.sendPrefixed("§cMust hold a Book & Quill!");
            return false;
         }
      }

      if (this.dropInventoryBefore) {
         int keepSlot = mc.player.getInventory().getSelectedSlot();

         for (int i = 0; i < 36; i++) {
            if (i != keepSlot && !mc.player.getInventory().getItem(i).isEmpty()) {
               autismclient.util.InventoryDropHelper.load(mc, i, 0);
            }
         }
      }

      return true;
   }

   public void init_V_Minecraft_8(Minecraft mc) {
      if (this.disconnectAfter && mc.level != null && mc.getConnection() != null) {
         mc.getConnection().getConnection().disconnect(Component.literal("Disconnected by Macro"));
      }
   }

   private int init_I_Minecraft_2(Minecraft mc) {
      if (mc.player == null) {
         return -1;
      }

      Identifier writableBookId = Identifier.fromNamespaceAndPath("minecraft", "writable_book");

      for (int i = 0; i < mc.player.getInventory().getContainerSize(); i++) {
         ItemStack stack = mc.player.getInventory().getItem(i);
         if (!stack.isEmpty() && BuiltInRegistries.ITEM.getKey(stack.getItem()).equals(writableBookId)) {
            return i;
         }
      }

      return -1;
   }

   public static List<String> init_List_Font(Font tr, String text, int maxPages) {
      if (text != null && !text.isEmpty()) {
         String normalized = BookBotFileReader.init(text);
         String[] paragraphs = normalized.split("\n", -1);
         List<String> visualLines = new ArrayList<>();

         for (String para : paragraphs) {
            if (para.isEmpty()) {
               visualLines.add("");
            } else {
               init_V_Font(tr, para, visualLines);
            }
         }

         List<String> pages = new ArrayList<>();
         StringBuilder page = new StringBuilder();
         int linesOnPage = 0;
         List<String> boundedLines = new ArrayList<>(visualLines.size());

         for (String line : visualLines) {
            if (line.isEmpty()) {
               boundedLines.add("");
            } else {
               int offset = 0;

               while (offset < line.length()) {
                  int end = init_I_Str(line, offset, 1024);
                  boundedLines.add(line.substring(offset, end));
                  offset = end;
               }
            }
         }

         for (String line : boundedLines) {
            int addedLength = line.length() + (linesOnPage > 0 ? 1 : 0);
            if (linesOnPage >= 14 || page.length() + addedLength > 1024) {
               pages.add(page.toString());
               if (pages.size() >= maxPages) {
                  return pages;
               }

               page = new StringBuilder();
               linesOnPage = 0;
            }

            if (linesOnPage > 0) {
               page.append('\n');
            }

            page.append(line);
            linesOnPage++;
         }

         if (page.length() > 0) {
            pages.add(page.toString());
         }

         if (pages.isEmpty()) {
            pages.add("");
         }

         return pages;
      } else {
         return new ArrayList<>(List.of(""));
      }
   }

   private static void init_V_Font(Font tr, String para, List<String> out) {
      int idx = 0;

      while (idx < para.length()) {
         float width = 0.0F;
         int lineEnd = idx;
         int lastSpace = -1;

         while (lineEnd < para.length()) {
            int codePoint = para.codePointAt(lineEnd);
            int next = lineEnd + Character.charCount(codePoint);
            float cw = tr.width(para.substring(lineEnd, next));
            if (width + cw > 114.0F && lineEnd > idx) {
               break;
            }

            if (Character.isWhitespace(codePoint)) {
               lastSpace = lineEnd;
            }

            width += cw;
            lineEnd = next;
         }

         if (lineEnd >= para.length()) {
            out.add(para.substring(idx));
            return;
         }

         if (lastSpace > idx) {
            out.add(para.substring(idx, lastSpace));
            idx = lastSpace + 1;
         } else {
            out.add(para.substring(idx, lineEnd));
            idx = lineEnd;
         }
      }
   }

   public static int init_I_Font_Str(Font tr, String text) {
      return text != null && !text.isEmpty() ? init_List_Font(tr, text, 100).size() : 0;
   }

   public static List<String> init_List_Str(String text, int maxPages, int charsPerPage) {
      if (text != null && !text.isEmpty()) {
         int max = Math.max(1, Math.min(1024, charsPerPage));
         List<String> pages = new ArrayList<>();
         int index = 0;

         while (index < text.length() && pages.size() < maxPages) {
            int end = init_I_Str(text, index, max);
            pages.add(text.substring(index, end));
            index = end;
         }

         if (pages.isEmpty()) {
            pages.add("");
         }

         return pages;
      } else {
         return new ArrayList<>(List.of(""));
      }
   }

   private static int init_I_Str(String text, int start, int maxChars) {
      int end = Math.min(text.length(), start + Math.max(1, maxChars));
      if (end < text.length() && end > start && Character.isHighSurrogate(text.charAt(end - 1)) && Character.isLowSurrogate(text.charAt(end))) {
         end--;
      }

      if (end == start && start < text.length()) {
         end = start + Character.charCount(text.codePointAt(start));
      }

      return end;
   }

   @Override
   public MacroActionType getType() {
      return MacroActionType.NBT_BOOK;
   }

   @Override
   public CompoundTag toTag() {
      CompoundTag tag = new CompoundTag();
      tag.putString("type", this.getType().name());
      tag.putInt("pages", this.pages);
      tag.putInt("characters", this.characters);
      tag.putString("title", this.title);
      tag.putBoolean("sign", this.sign);
      tag.putBoolean("appendCount", this.appendCount);
      tag.putBoolean("onlyAscii", this.onlyAscii);
      tag.putString("randomType", this.read());
      tag.putString("dataSource", this.load());
      tag.putString("customText", this.customComponent);
      tag.putString("customFilePath", this.customFilePath);
      tag.putBoolean("wordWrap", this.wordWrap);
      tag.putInt("delayTicks", this.delayTicks);
      tag.putInt("bookCount", this.bookCount);
      tag.putBoolean("requireHeldWritableBook", this.requireHeldWritableBook);
      tag.putBoolean("dropInventoryBefore", this.dropInventoryBefore);
      tag.putBoolean("disconnectAfter", this.disconnectAfter);
      tag.putBoolean("enabled", this.init_Z);
      return tag;
   }

   @Override
   public void fromTag(CompoundTag tag) {
      if (tag.contains("pages")) {
         this.pages = tag.getIntOr("pages", 100);
      } else if (tag.contains("targetSizeKB")) {
         this.pages = Math.min(100, tag.getIntOr("targetSizeKB", 100));
      }

      this.characters = tag.getIntOr("characters", 1024);
      if (tag.contains("title")) {
         this.title = tag.getStringOr("title", "EldenRingLore");
      }

      this.sign = tag.getBooleanOr("sign", true);
      this.appendCount = tag.getBooleanOr("appendCount", true);
      if (tag.contains("onlyAscii")) {
         this.onlyAscii = tag.getBooleanOr("onlyAscii", false);
      }

      if (tag.contains("randomType")) {
         this.randomType = BookBotPageGenerator.init(tag.getStringOr("randomType", "Utf8"));
      } else if (this.onlyAscii) {
         this.randomType = "Ascii";
      }

      if (tag.contains("customText")) {
         this.customComponent = tag.getStringOr("customText", "");
      }

      if (tag.contains("customFilePath")) {
         this.customFilePath = tag.getStringOr("customFilePath", "");
      }

      this.wordWrap = tag.getBooleanOr("wordWrap", true);
      if (tag.contains("dataSource")) {
         this.dataSource = init_Str_Str_9(tag.getStringOr("dataSource", "Random"));
      } else {
         this.dataSource = load(this.customComponent);
      }

      if (tag.contains("delayTicks")) {
         this.delayTicks = tag.getIntOr("delayTicks", 20);
      }

      if (tag.contains("bookCount")) {
         this.bookCount = tag.getIntOr("bookCount", 1);
      }

      this.requireHeldWritableBook = tag.getBooleanOr("requireHeldWritableBook", false);
      this.dropInventoryBefore = tag.getBooleanOr("dropInventoryBefore", false);
      this.disconnectAfter = tag.getBooleanOr("disconnectAfter", false);
      if (tag.contains("enabled")) {
         this.init_Z = tag.getBooleanOr("enabled", true);
      }
   }

   @Override
   public String getDisplayName() {
      String mode = "Random".equals(this.load()) ? this.read() : this.load();
      String count = this.bookCount > 1 ? ", x" + this.bookCount : "";
      String extras = "";
      if (this.dropInventoryBefore) {
         extras = extras + ", drop inv";
      }

      if (this.disconnectAfter) {
         extras = extras + ", disconnect";
      }

      return "NBT Book (" + this.pages + "pg, " + mode + count + extras + ")";
   }

   private String load() {
      this.dataSource = init_Str_Str_9(this.dataSource);
      return this.dataSource;
   }

   private String read() {
      this.randomType = BookBotPageGenerator.init(this.randomType);
      this.onlyAscii = "Ascii".equals(this.randomType);
      return this.randomType;
   }

   private static String init_Str_Str_9(String source) {
      if (source == null) {
         return "Random";
      }

      String var10000;
      switch (source.trim().toLowerCase(Locale.ROOT)) {
         case "generated":
         case "random":
            var10000 = "Random";
            break;
         case "pasted":
         case "paste":
         case "custom":
            var10000 = "Pasted";
            break;
         case "file":
         case "path":
            var10000 = "File";
            break;
         default:
            var10000 = "Random";
      }

      return var10000;
   }

   private static String load(String text) {
      return text != null && !text.isBlank() && !"Autism Inc - EldenRingLore".equals(text) ? "Pasted" : "Random";
   }

   @Override
   public void sanitizeForSharing() {
      this.customFilePath = "";
      if ("File".equals(init_Str_Str_9(this.dataSource))) {
         this.dataSource = "Random";
      }
   }

   @Override
   public String getIcon() {
      return "B";
   }

   @Override
   public boolean isEnabled() {
      return this.init_Z;
   }

   @Override
   public void setEnabled(boolean e) {
      this.init_Z = e;
   }
}

package autismclient.gui.f.b;

import autismclient.util.ThemedTextHelper;
import java.util.ArrayList;
import java.util.List;
import java.util.function.IntConsumer;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;

public final class SyncManager {
   private static boolean read_Z;
   public boolean init_Z = true;
   public boolean load_Z = true;
   private final RefQueue init_RefQueue;
   private int init_I;
   private int load_I;
   private int read_I;
   private int write;
   private List<String> init_List;
   private List<String> load_List;
   private IntConsumer init_IntConsumer;
   private int update;
   private String init_Str = "";

   public SyncManager(int x, int y, int width, int height, List<String> options, int selectedIndex, IntConsumer onSelect) {
      this.init_I = x;
      this.load_I = y;
      this.read_I = width;
      this.write = height;
      this.init_List = init_List_List(options);
      this.load_List = load_List(this.init_List);
      this.update = init(selectedIndex, this.init_List.size());
      this.init_IntConsumer = onSelect;
      this.init_RefQueue = new RefQueue(this.load_NetHelper(), this.load_List, this.load_Str(), this::init_V_Str);
   }

   public SyncManager init_SyncManager_I(int x, int y, int width, int height) {
      this.init_I = x;
      this.load_I = y;
      this.read_I = width;
      this.write = height;
      this.init_RefQueue.init_V_NetHelper(this.load_NetHelper());
      return this;
   }

   public SyncManager init_SyncManager_List(List<String> options) {
      List<String> next = init_List_List(options);
      if (this.init_List.equals(next)) {
         return this;
      }

      this.init_List = next;
      this.load_List = load_List(next);
      this.update = init(this.update, next.size());
      this.init_RefQueue.init(this.load_List);
      this.init_RefQueue.init(this.load_Str());
      return this;
   }

   public SyncManager init_SyncManager_I(int selectedIndex) {
      this.update = init(selectedIndex, this.init_List.size());
      this.init_RefQueue.init(this.load_Str());
      return this;
   }

   public SyncManager init_SyncManager_IntConsumer(IntConsumer onSelect) {
      this.init_IntConsumer = onSelect;
      return this;
   }

   public SyncManager init_SyncManager_Str(String label) {
      this.init_Str = label == null ? "" : label;
      return this;
   }

   public int init_I() {
      return this.init_I;
   }

   public int load_I() {
      return this.load_I;
   }

   public autismclient.gui.f.NetHelper init_NetHelper() {
      return this.init_RefQueue.read_NetHelper();
   }

   public int read() {
      return this.init_RefQueue.init_I();
   }

   public static void init(GuiGraphicsExtractor graphics, Font font, List<SyncManager> dropdowns, int mouseX, int mouseY) {
      load(graphics, font, dropdowns, mouseX, mouseY);
      read(graphics, font, dropdowns, mouseX, mouseY);
   }

   public static void load(GuiGraphicsExtractor graphics, Font font, List<SyncManager> dropdowns, int mouseX, int mouseY) {
      if (graphics != null && font != null && dropdowns != null) {
         autismclient.gui.f.SyncManager context = init_SyncManager_GuiGraphicsExtract(graphics, font, mouseX, mouseY);
         boolean menuOpen = init_Z_List(dropdowns);
         boolean suppressHover = menuOpen || init_Z();

         for (SyncManager dropdown : dropdowns) {
            if (dropdown != null && dropdown.load_Z) {
               boolean hovered = dropdown.init_Z && !suppressHover && dropdown.load_NetHelper().init_Z(mouseX, mouseY);
               RefQueue.init(context, dropdown.load_NetHelper(), dropdown.init_Str(), hovered, dropdown.init_RefQueue.init_Z_2(), dropdown.init_Z);
            }
         }
      }
   }

   public static void read(GuiGraphicsExtractor graphics, Font font, List<SyncManager> dropdowns, int mouseX, int mouseY) {
      if (graphics != null && font != null && dropdowns != null) {
         for (SyncManager dropdown : dropdowns) {
            if (dropdown != null && dropdown.load_Z && dropdown.init_RefQueue.init_Z_2()) {
               dropdown.init_RefQueue.init_V_SyncManager(init_SyncManager_GuiGraphicsExtract(graphics, font, mouseX, mouseY));
               return;
            }
         }
      }
   }

   public static boolean init_Z_List(List<SyncManager> dropdowns) {
      if (dropdowns == null) {
         read_Z = false;
         return false;
      }

      for (SyncManager dropdown : dropdowns) {
         if (dropdown != null && dropdown.load_Z && dropdown.init_RefQueue.init_Z_2()) {
            return true;
         }
      }

      read_Z = false;
      return false;
   }

   public static boolean load_Z(List<SyncManager> dropdowns) {
      return init_Z() || init_Z_List(dropdowns);
   }

   public static boolean init(List<SyncManager> dropdowns, double mouseX, double mouseY) {
      if (dropdowns == null) {
         return false;
      }

      for (SyncManager dropdown : dropdowns) {
         if (dropdown != null && dropdown.load_Z && dropdown.init_RefQueue.init_Z((int)mouseX, (int)mouseY)) {
            return true;
         }
      }

      return false;
   }

   public static boolean init_Z() {
      return read_Z;
   }

   public static boolean read(List<SyncManager> dropdowns) {
      if (dropdowns == null) {
         return false;
      }

      boolean closed = false;

      for (SyncManager dropdown : dropdowns) {
         if (dropdown != null && dropdown.init_RefQueue.init_Z_2()) {
            dropdown.init_V();
            closed = true;
         }
      }

      return closed;
   }

   public static boolean init_Z_List_2(List<SyncManager> dropdowns, double mouseX, double mouseY, int mouseButton) {
      if (dropdowns == null) {
         return false;
      }

      SyncManager openDropdown = load_SyncManager(dropdowns);
      if (openDropdown != null) {
         if (mouseButton == 0) {
            read_Z = true;
         }

         openDropdown.init_RefQueue.init_EventBus_I_2((int)mouseX, (int)mouseY, mouseButton);
         if (mouseButton != 0
            && !openDropdown.load_NetHelper().init_Z((int)mouseX, (int)mouseY)
            && !openDropdown.init_RefQueue.init_Z((int)mouseX, (int)mouseY)) {
            openDropdown.init_V();
         }

         read_Z = openDropdown.init_RefQueue.init_Z_2();
         return true;
      } else {
         if (mouseButton != 0) {
            return false;
         }

         for (int i = dropdowns.size() - 1; i >= 0; i--) {
            SyncManager dropdown = dropdowns.get(i);
            if (dropdown != null && dropdown.load_Z && dropdown.init_Z && dropdown.load_NetHelper().init_Z((int)mouseX, (int)mouseY)) {
               read(dropdowns);
               dropdown.init_RefQueue.init_V_2();
               read_Z = true;
               return true;
            }
         }

         return false;
      }
   }

   public static boolean init_Z_List(List<SyncManager> dropdowns, double mouseX, double mouseY, double deltaY) {
      SyncManager dropdown = load_SyncManager(dropdowns);
      if (dropdown == null) {
         return false;
      }

      if (!dropdown.init_RefQueue.init_Z((int)mouseX, (int)mouseY)) {
         return false;
      }

      dropdown.init_RefQueue.init_EventBus_I((int)mouseX, (int)mouseY, deltaY);
      return true;
   }

   public static boolean load(List<SyncManager> dropdowns, double mouseX, double mouseY, int button) {
      SyncManager dropdown = load_SyncManager(dropdowns);
      if (dropdown == null) {
         return false;
      }

      dropdown.init_RefQueue.init_EventBus_I((int)mouseX, (int)mouseY, button, 0.0, 0.0);
      return true;
   }

   public static boolean write(List<SyncManager> dropdowns) {
      boolean consumed = read_Z || init_Z_List(dropdowns);
      read_Z = false;
      if (dropdowns == null) {
         return consumed;
      }

      for (SyncManager dropdown : dropdowns) {
         if (dropdown != null && dropdown.init_RefQueue.load(0, 0, 0) == autismclient.gui.f.EventBus.HANDLED) {
            consumed = true;
         }
      }

      return consumed;
   }

   public void init_V() {
      this.init_RefQueue.load_V_2();
      read_Z = false;
   }

   private autismclient.gui.f.NetHelper load_NetHelper() {
      return autismclient.gui.f.NetHelper.init(this.init_I, this.load_I, this.read_I, this.write);
   }

   private String init_Str() {
      return this.init_Str.isBlank() ? this.load_Str() : this.init_Str;
   }

   private String load_Str() {
      return this.load_List.isEmpty() ? "" : this.load_List.get(init(this.update, this.load_List.size()));
   }

   private void init_V_Str(String displayValue) {
      int index = this.load_List.indexOf(displayValue);
      if (index >= 0) {
         this.update = index;
         if (this.init_IntConsumer != null) {
            this.init_IntConsumer.accept(index);
         }
      }
   }

   private static SyncManager load_SyncManager(List<SyncManager> dropdowns) {
      if (dropdowns == null) {
         return null;
      }

      for (SyncManager dropdown : dropdowns) {
         if (dropdown != null && dropdown.load_Z && dropdown.init_RefQueue.init_Z_2()) {
            return dropdown;
         }
      }

      return null;
   }

   private static autismclient.gui.f.SyncManager init_SyncManager_GuiGraphicsExtract(GuiGraphicsExtractor graphics, Font font, int mouseX, int mouseY) {
      return autismclient.gui.f.UiHelper.init(graphics, font, mouseX, mouseY);
   }

   private static List<String> init_List_List(List<String> options) {
      return options == null ? List.of() : List.copyOf(options);
   }

   private static List<String> load_List(List<String> options) {
      List<String> labels = new ArrayList<>(options.size());

      for (String option : options) {
         labels.add(init_Str_Str(option));
      }

      return List.copyOf(labels);
   }

   private static String init_Str_Str(String raw) {
      String sanitized = ThemedTextHelper.init(raw);
      if (sanitized != null && !sanitized.isEmpty()) {
         if (sanitized.length() <= 3 && sanitized.chars().anyMatch(Character::isDigit)) {
            return sanitized;
         }

         boolean looksEnum = true;

         for (int i = 0; i < sanitized.length(); i++) {
            char c = sanitized.charAt(i);
            if (c != '_' && c != ' ' && !Character.isDigit(c) && (!Character.isLetter(c) || c != Character.toUpperCase(c))) {
               looksEnum = false;
               break;
            }
         }

         if (!looksEnum) {
            return sanitized;
         }

         StringBuilder out = new StringBuilder(sanitized.length());
         boolean newWord = true;

         for (int i = 0; i < sanitized.length(); i++) {
            char c = sanitized.charAt(i);
            if (c != '_' && c != ' ') {
               if (newWord) {
                  out.append(Character.toUpperCase(c));
                  newWord = false;
               } else {
                  out.append(Character.toLowerCase(c));
               }
            } else {
               if (!out.isEmpty() && out.charAt(out.length() - 1) != ' ') {
                  out.append(' ');
               }

               newWord = true;
            }
         }

         return out.toString().trim();
      } else {
         return "";
      }
   }

   private static int init(int index, int size) {
      return size <= 0 ? 0 : Math.max(0, Math.min(size - 1, index));
   }
}

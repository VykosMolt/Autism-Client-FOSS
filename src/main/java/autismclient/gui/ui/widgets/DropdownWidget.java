package autismclient.gui.ui.widgets;

import autismclient.util.ThemedTextHelper;
import java.util.ArrayList;
import java.util.List;
import java.util.function.IntConsumer;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;

public final class DropdownWidget {
   private static boolean anyMenuActive;
   public boolean active = true;
   public boolean visible = true;
   private final DropdownMenu menu;
   private int x;
   private int y;
   private int width;
   private int height;
   private List<String> rawOptions;
   private List<String> displayOptions;
   private IntConsumer onSelect;
   private int selectedIndex;
   private String labelOverride = "";

   public DropdownWidget(int x, int y, int width, int height, List<String> options, int selectedIndex, IntConsumer onSelect) {
      this.x = x;
      this.y = y;
      this.width = width;
      this.height = height;
      this.rawOptions = copyOptions(options);
      this.displayOptions = formatDisplayOptions(this.rawOptions);
      this.selectedIndex = clampIndex(selectedIndex, this.rawOptions.size());
      this.onSelect = onSelect;
      this.menu = new DropdownMenu(this.anchorBounds(), this.displayOptions, this.selectedDisplayOption(), this::handleOptionChosen);
   }

   public DropdownWidget setBounds(int x, int y, int width, int height) {
      this.x = x;
      this.y = y;
      this.width = width;
      this.height = height;
      this.menu.setBounds(this.anchorBounds());
      return this;
   }

   public DropdownWidget setOptions(List<String> options) {
      List<String> next = copyOptions(options);
      if (this.rawOptions.equals(next)) {
         return this;
      }

      this.rawOptions = next;
      this.displayOptions = formatDisplayOptions(next);
      this.selectedIndex = clampIndex(this.selectedIndex, next.size());
      this.menu.setOptions(this.displayOptions);
      this.menu.setSelectedOption(this.selectedDisplayOption());
      return this;
   }

   public DropdownWidget setSelectedIndex(int selectedIndex) {
      this.selectedIndex = clampIndex(selectedIndex, this.rawOptions.size());
      this.menu.setSelectedOption(this.selectedDisplayOption());
      return this;
   }

   public DropdownWidget setOnSelect(IntConsumer onSelect) {
      this.onSelect = onSelect;
      return this;
   }

   public DropdownWidget setLabelOverride(String label) {
      this.labelOverride = label == null ? "" : label;
      return this;
   }

   public int getX() {
      return this.x;
   }

   public int getY() {
      return this.y;
   }

   public autismclient.gui.f.NetHelper getPopupBounds() {
      return this.menu.getPopupBounds();
   }

   public int getScrollRow() {
      return this.menu.getScrollRow();
   }

   public static void renderAll(GuiGraphicsExtractor graphics, Font font, List<DropdownWidget> dropdowns, int mouseX, int mouseY) {
      renderTriggers(graphics, font, dropdowns, mouseX, mouseY);
      renderOpenPopup(graphics, font, dropdowns, mouseX, mouseY);
   }

   public static void renderTriggers(GuiGraphicsExtractor graphics, Font font, List<DropdownWidget> dropdowns, int mouseX, int mouseY) {
      if (graphics != null && font != null && dropdowns != null) {
         autismclient.gui.f.SyncManager context = createUiContext(graphics, font, mouseX, mouseY);
         boolean menuOpen = hasOpenMenu(dropdowns);
         boolean suppressHover = menuOpen || isAnyMenuActive();

         for (DropdownWidget dropdown : dropdowns) {
            if (dropdown != null && dropdown.visible) {
               boolean hovered = dropdown.active && !suppressHover && dropdown.anchorBounds().init_Z(mouseX, mouseY);
               DropdownMenu.renderTrigger(context, dropdown.anchorBounds(), dropdown.currentLabel(), hovered, dropdown.menu.isOpen(), dropdown.active);
            }
         }
      }
   }

   public static void renderOpenPopup(GuiGraphicsExtractor graphics, Font font, List<DropdownWidget> dropdowns, int mouseX, int mouseY) {
      if (graphics != null && font != null && dropdowns != null) {
         for (DropdownWidget dropdown : dropdowns) {
            if (dropdown != null && dropdown.visible && dropdown.menu.isOpen()) {
               dropdown.menu.render(createUiContext(graphics, font, mouseX, mouseY));
               return;
            }
         }
      }
   }

   public static boolean hasOpenMenu(List<DropdownWidget> dropdowns) {
      if (dropdowns == null) {
         anyMenuActive = false;
         return false;
      }

      for (DropdownWidget dropdown : dropdowns) {
         if (dropdown != null && dropdown.visible && dropdown.menu.isOpen()) {
            return true;
         }
      }

      anyMenuActive = false;
      return false;
   }

   public static boolean isBlockingHover(List<DropdownWidget> dropdowns) {
      return isAnyMenuActive() || hasOpenMenu(dropdowns);
   }

   public static boolean isPointInAnyPopup(List<DropdownWidget> dropdowns, double mouseX, double mouseY) {
      if (dropdowns == null) {
         return false;
      }

      for (DropdownWidget dropdown : dropdowns) {
         if (dropdown != null && dropdown.visible && dropdown.menu.containsPopupPoint((int)mouseX, (int)mouseY)) {
            return true;
         }
      }

      return false;
   }

   public static boolean isAnyMenuActive() {
      return anyMenuActive;
   }

   public static boolean closeAll(List<DropdownWidget> dropdowns) {
      if (dropdowns == null) {
         return false;
      }

      boolean closed = false;

      for (DropdownWidget dropdown : dropdowns) {
         if (dropdown != null && dropdown.menu.isOpen()) {
            dropdown.close();
            closed = true;
         }
      }

      return closed;
   }

   public static boolean handleMouseClick(List<DropdownWidget> dropdowns, double mouseX, double mouseY, int mouseButton) {
      if (dropdowns == null) {
         return false;
      }

      DropdownWidget openDropdown = findOpenDropdown(dropdowns);
      if (openDropdown != null) {
         if (mouseButton == 0) {
            anyMenuActive = true;
         }

         openDropdown.menu.onMouseClicked((int)mouseX, (int)mouseY, mouseButton);
         if (mouseButton != 0
            && !openDropdown.anchorBounds().init_Z((int)mouseX, (int)mouseY)
            && !openDropdown.menu.containsPopupPoint((int)mouseX, (int)mouseY)) {
            openDropdown.close();
         }

         anyMenuActive = openDropdown.menu.isOpen();
         return true;
      } else {
         if (mouseButton != 0) {
            return false;
         }

         for (int i = dropdowns.size() - 1; i >= 0; i--) {
            DropdownWidget dropdown = dropdowns.get(i);
            if (dropdown != null && dropdown.visible && dropdown.active && dropdown.anchorBounds().init_Z((int)mouseX, (int)mouseY)) {
               closeAll(dropdowns);
               dropdown.menu.open();
               anyMenuActive = true;
               return true;
            }
         }

         return false;
      }
   }

   public static boolean handleMouseScroll(List<DropdownWidget> dropdowns, double mouseX, double mouseY, double deltaY) {
      DropdownWidget dropdown = findOpenDropdown(dropdowns);
      if (dropdown == null) {
         return false;
      }

      if (!dropdown.menu.containsPopupPoint((int)mouseX, (int)mouseY)) {
         return false;
      }

      dropdown.menu.onMouseScrolled((int)mouseX, (int)mouseY, deltaY);
      return true;
   }

   public static boolean handleMouseDrag(List<DropdownWidget> dropdowns, double mouseX, double mouseY, int button) {
      DropdownWidget dropdown = findOpenDropdown(dropdowns);
      if (dropdown == null) {
         return false;
      }

      dropdown.menu.onMouseDragged((int)mouseX, (int)mouseY, button, 0.0, 0.0);
      return true;
   }

   public static boolean handleMouseRelease(List<DropdownWidget> dropdowns) {
      boolean consumed = anyMenuActive || hasOpenMenu(dropdowns);
      anyMenuActive = false;
      if (dropdowns == null) {
         return consumed;
      }

      for (DropdownWidget dropdown : dropdowns) {
         if (dropdown != null && dropdown.menu.onMouseReleased(0, 0, 0) == autismclient.gui.ui.UiInputResult.HANDLED) {
            consumed = true;
         }
      }

      return consumed;
   }

   public void close() {
      this.menu.close();
      anyMenuActive = false;
   }

   private autismclient.gui.f.NetHelper anchorBounds() {
      return autismclient.gui.f.NetHelper.init(this.x, this.y, this.width, this.height);
   }

   private String currentLabel() {
      return this.labelOverride.isBlank() ? this.selectedDisplayOption() : this.labelOverride;
   }

   private String selectedDisplayOption() {
      return this.displayOptions.isEmpty() ? "" : this.displayOptions.get(clampIndex(this.selectedIndex, this.displayOptions.size()));
   }

   private void handleOptionChosen(String displayValue) {
      int index = this.displayOptions.indexOf(displayValue);
      if (index >= 0) {
         this.selectedIndex = index;
         if (this.onSelect != null) {
            this.onSelect.accept(index);
         }
      }
   }

   private static DropdownWidget findOpenDropdown(List<DropdownWidget> dropdowns) {
      if (dropdowns == null) {
         return null;
      }

      for (DropdownWidget dropdown : dropdowns) {
         if (dropdown != null && dropdown.visible && dropdown.menu.isOpen()) {
            return dropdown;
         }
      }

      return null;
   }

   private static autismclient.gui.f.SyncManager createUiContext(GuiGraphicsExtractor graphics, Font font, int mouseX, int mouseY) {
      return autismclient.gui.f.UiHelper.init(graphics, font, mouseX, mouseY);
   }

   private static List<String> copyOptions(List<String> options) {
      return options == null ? List.of() : List.copyOf(options);
   }

   private static List<String> formatDisplayOptions(List<String> options) {
      List<String> labels = new ArrayList<>(options.size());

      for (String option : options) {
         labels.add(formatOptionLabel(option));
      }

      return List.copyOf(labels);
   }

   private static String formatOptionLabel(String raw) {
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

   private static int clampIndex(int index, int size) {
      return size <= 0 ? 0 : Math.max(0, Math.min(size - 1, index));
   }
}

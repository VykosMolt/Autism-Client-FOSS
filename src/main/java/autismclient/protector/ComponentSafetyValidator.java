package autismclient.protector;

import java.util.Optional;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentContents;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.HoverEvent.ShowEntity;
import net.minecraft.network.chat.HoverEvent.ShowText;
import net.minecraft.network.chat.contents.NbtContents;
import net.minecraft.network.chat.contents.ObjectContents;
import net.minecraft.network.chat.contents.PlainTextContents;
import net.minecraft.network.chat.contents.SelectorContents;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.network.chat.numbers.FixedFormat;
import net.minecraft.network.chat.numbers.NumberFormat;

public final class ComponentSafetyValidator {
   private static final int init = 64;
   private static final int load = 65536;
   private static final int read = 16384;

   private ComponentSafetyValidator() {
   }

   public static boolean init(Component component) {
      if (component == null) {
         return true;
      }

      try {
         if (component.getContents() instanceof PlainTextContents plain && component.getSiblings().isEmpty() && component.getStyle().getHoverEvent() == null) {
            return plain.text().length() <= 16384;
         } else {
            int[] charBudget = new int[1];
            if (!init(component, 0, charBudget)) {
               return false;
            }

            component.getString();
            return true;
         }
      } catch (Throwable t) {
         return false;
      }
   }

   public static boolean init(NumberFormat format) {
      if (format == null) {
         return true;
      }

      try {
         return format instanceof FixedFormat fixed ? init(fixed.value()) : true;
      } catch (Throwable t) {
         return false;
      }
   }

   private static boolean init(Component component, int depth, int[] charBudget) {
      if (depth > 64) {
         return false;
      }

      ComponentContents contents = component.getContents();
      if (contents instanceof PlainTextContents plain) {
         if (!init(plain.text(), charBudget)) {
            return false;
         }
      } else if (contents instanceof TranslatableContents translatable) {
         if (!init(translatable.getKey(), charBudget)) {
            return false;
         }

         if (translatable.getFallback() != null && !init(translatable.getFallback(), charBudget)) {
            return false;
         }

         for (Object arg : translatable.getArgs()) {
            if (arg instanceof Component argComponent) {
               if (!init(argComponent, depth + 1, charBudget)) {
                  return false;
               }
            } else if (arg != null && !init(String.valueOf(arg), charBudget)) {
               return false;
            }
         }
      } else if (contents instanceof NbtContents nbt) {
         if (!init(nbt.separator(), depth + 1, charBudget)) {
            return false;
         }
      } else if (contents instanceof SelectorContents selector) {
         if (!init(selector.separator(), depth + 1, charBudget)) {
            return false;
         }
      } else if (contents instanceof ObjectContents object && !init(object.fallback(), depth + 1, charBudget)) {
         return false;
      }

      for (Component sibling : component.getSiblings()) {
         if (!init(sibling, depth + 1, charBudget)) {
            return false;
         }
      }

      HoverEvent hover = component.getStyle().getHoverEvent();
      if (hover instanceof ShowText showText) {
         if (!init(showText.value(), depth + 1, charBudget)) {
            return false;
         }
      } else if (hover instanceof ShowEntity showEntity && !init(showEntity.entity().name, depth + 1, charBudget)) {
         return false;
      }

      return true;
   }

   private static boolean init(Optional<Component> component, int depth, int[] charBudget) {
      return component == null || component.isEmpty() || init(component.get(), depth, charBudget);
   }

   private static boolean init(String value, int[] charBudget) {
      if (value == null) {
         return true;
      }

      if (value.length() > 16384) {
         return false;
      }

      charBudget[0] += value.length();
      return charBudget[0] <= 65536;
   }
}

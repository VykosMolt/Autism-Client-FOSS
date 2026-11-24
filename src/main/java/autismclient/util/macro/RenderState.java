package autismclient.util.macro;

import autismclient.api.custommenu.CustomMenuButton;
import autismclient.api.custommenu.CustomMenuInput;
import autismclient.api.custommenu.CustomMenuSnapshot;
import autismclient.api.custommenu.CustomMenuSubmission;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.Function;

public final class RenderState {
   private static final List<String> init = List.of(
      "login", "register", "submit", "continue", "confirm", "yes", "ok", "accept", "agree", "agreed", "done", "join", "enter"
   );
   private static final List<String> load = List.of("cancel", "quit", "back", "deny", "decline", "no", "exit", "disconnect", "close");
   private static final List<String> read = List.of("accept", "agree", "agreed", "consent", "understood", "acknowledge", "confirm");

   private RenderState() {
   }

   public static boolean init(UiHelper action, String title) {
      return true;
   }

   public static String init(UiHelper action) {
      return "";
   }

   public static RenderState.Inner_DataUtil init(UiHelper action, CustomMenuSnapshot snapshot, Function<String, String> resolver) {
      if (action != null && snapshot != null) {
         Function<String, String> safeResolver = resolver == null ? Function.identity() : resolver;
         Map<String, String> values = new LinkedHashMap<>();

         for (CustomMenuInput input : snapshot.inputs()) {
            values.put(input.key(), input.initialValue());
         }

         List<CustomMenuInput> textInputs = new ArrayList<>();

         for (CustomMenuInput input : snapshot.inputs()) {
            if (input.kind() == CustomMenuInput.Kind.TEXT) {
               textInputs.add(input);
            }
         }

         if (!textInputs.isEmpty()) {
            List<String> fields = action.fieldValues.isEmpty() ? List.of("{secret.password}") : action.fieldValues;
            List<String> resolved = new ArrayList<>(fields.size());

            for (int i = 0; i < fields.size(); i++) {
               String out;
               try {
                  out = safeResolver.apply(init_Str(fields.get(i)));
               } catch (RuntimeException error) {
                  return RenderState.Inner_DataUtil.init("Text field value " + (i + 1) + " is unavailable");
               }

               if (out == null) {
                  return RenderState.Inner_DataUtil.init("Text field value " + (i + 1) + " is unavailable");
               }

               resolved.add(out);
            }

            if (resolved.size() == 1) {
               for (CustomMenuInput input : textInputs) {
                  values.put(input.key(), resolved.get(0));
               }
            } else {
               for (int i = 0; i < textInputs.size() && i < resolved.size(); i++) {
                  values.put(textInputs.get(i).key(), resolved.get(i));
               }
            }
         }

         CustomMenuButton button = init(action, snapshot.buttons());
         return button == null
            ? RenderState.Inner_DataUtil.init(
               init_Str(action.clickButton).isBlank()
                  ? "No obvious button to press; type the button's text (e.g. Register)"
                  : "No button matched \"" + action.clickButton + "\" on this screen"
            )
            : RenderState.Inner_DataUtil.init(new CustomMenuSubmission(values, button));
      } else {
         return RenderState.Inner_DataUtil.init("No custom screen is open");
      }
   }

   public static CustomMenuButton init(UiHelper action, List<CustomMenuButton> buttons) {
      if (buttons != null && !buttons.isEmpty()) {
         List<CustomMenuButton> usable = new ArrayList<>();

         for (CustomMenuButton button : buttons) {
            if (button.serverRelevant()) {
               usable.add(button);
            }
         }

         if (usable.isEmpty()) {
            return null;
         }

         String want = init_Str(action == null ? "" : action.clickButton).trim();
         if (want.isEmpty()) {
            return write(usable);
         }

         if (want.startsWith("#")) {
            try {
               int index = Integer.parseInt(want.substring(1).trim());

               for (CustomMenuButton button : usable) {
                  if (button.index() == index) {
                     return button;
                  }
               }
            } catch (NumberFormatException var7) {
            }
         }

         String lower = want.toLowerCase(Locale.ROOT);

         for (CustomMenuButton button : usable) {
            if (button.label().equalsIgnoreCase(want)) {
               return button;
            }
         }

         for (CustomMenuButton button : usable) {
            if (button.label().toLowerCase(Locale.ROOT).contains(lower)) {
               return button;
            }
         }

         for (CustomMenuButton button : usable) {
            if (!button.actionId().isBlank() && button.actionId().toLowerCase(Locale.ROOT).contains(lower)) {
               return button;
            }
         }

         return null;
      } else {
         return null;
      }
   }

   public static CustomMenuButton init(List<CustomMenuButton> buttons) {
      if (buttons == null) {
         return null;
      }

      for (CustomMenuButton button : buttons) {
         if (button.serverRelevant() && (init(button.label(), read) || init(button.actionId(), read))) {
            return button;
         }
      }

      return null;
   }

   public static CustomMenuButton load(List<CustomMenuButton> buttons) {
      if (buttons == null) {
         return null;
      }

      List<CustomMenuButton> usable = new ArrayList<>();

      for (CustomMenuButton button : buttons) {
         if (button.serverRelevant()) {
            usable.add(button);
         }
      }

      return usable.isEmpty() ? null : write(usable);
   }

   private static CustomMenuButton write(List<CustomMenuButton> usable) {
      List<CustomMenuButton> nonNegative = new ArrayList<>();

      for (CustomMenuButton button : usable) {
         if (!init(button.label(), load) && !init(button.actionId(), load)) {
            nonNegative.add(button);
         }
      }

      if (nonNegative.size() == 1) {
         return nonNegative.get(0);
      }

      List<CustomMenuButton> positive = new ArrayList<>();

      for (CustomMenuButton button : nonNegative) {
         if (init(button.label(), init) || init(button.actionId(), init)) {
            positive.add(button);
         }
      }

      return positive.size() == 1 ? positive.get(0) : null;
   }

   public static CustomMenuButton read(List<CustomMenuButton> buttons) {
      if (buttons == null) {
         return null;
      }

      List<CustomMenuButton> usable = new ArrayList<>();

      for (CustomMenuButton button : buttons) {
         if (button.serverRelevant()) {
            usable.add(button);
         }
      }

      if (usable.isEmpty()) {
         return null;
      }

      List<CustomMenuButton> nonNegative = new ArrayList<>();

      for (CustomMenuButton button : usable) {
         if (!init(button.label(), load) && !init(button.actionId(), load)) {
            nonNegative.add(button);
         }
      }

      for (CustomMenuButton button : nonNegative) {
         if (init_Z(button.labelColor())) {
            return button;
         }
      }

      for (CustomMenuButton button : nonNegative) {
         if (init(button.label(), init) || init(button.actionId(), init)) {
            return button;
         }
      }

      return !nonNegative.isEmpty() ? nonNegative.get(0) : usable.get(0);
   }

   static boolean init_Z(String serializedColor) {
      String value = init_Str(serializedColor).trim().toLowerCase(Locale.ROOT);
      if (value.isEmpty()) {
         return false;
      }

      if (!value.equals("green") && !value.equals("dark_green")) {
         if (value.length() == 7 && value.charAt(0) == '#') {
            int rgb;
            try {
               rgb = Integer.parseInt(value.substring(1), 16);
            } catch (NumberFormatException notHex) {
               return false;
            }

            int r = rgb >> 16 & 0xFF;
            int g = rgb >> 8 & 0xFF;
            int b = rgb & 0xFF;
            return g >= 60 && g > r + 30 && g > b + 30;
         } else {
            return false;
         }
      } else {
         return true;
      }
   }

   private static boolean init(String value, List<String> words) {
      String[] tokens = init_Str(value).toLowerCase(Locale.ROOT).split("[^a-z0-9]+");

      for (String token : tokens) {
         for (String word : words) {
            if (token.equals(word)) {
               return true;
            }
         }
      }

      return false;
   }

   private static String init_Str(String value) {
      return value == null ? "" : value;
   }

   public record Inner_DataUtil(CustomMenuSubmission init_CustomMenuSubmissi, String init_Str) {
      public boolean init_Z() {
         return this.init_CustomMenuSubmissi != null && this.init_Str.isEmpty();
      }

      static RenderState.Inner_DataUtil init(CustomMenuSubmission submission) {
         return new RenderState.Inner_DataUtil(submission, "");
      }

      static RenderState.Inner_DataUtil init(String error) {
         return new RenderState.Inner_DataUtil(null, error == null ? "Custom screen failed" : error);
      }
   }
}

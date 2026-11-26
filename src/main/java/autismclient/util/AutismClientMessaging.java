package autismclient.util;

import autismclient.mixin.accessor.AutismChatComponentAccessor;
import autismclient.mixin.accessor.AutismCommandHistoryAccessor;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.ListIterator;
import java.util.Locale;
import java.util.Set;
import net.minecraft.client.CommandHistory;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.ChatComponent;
import net.minecraft.client.multiplayer.chat.GuiMessage;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;

public final class AutismClientMessaging {
   private static final Minecraft MC = Minecraft.getInstance();
   private static final long DUPLICATE_WINDOW_MS = 600L;
   private static final String PREFIX_TEXT = "[AUTISM] ";
   private static String lastMessageKey = "";
   private static long lastMessageAtMs = 0L;
   private static String lastReplaceGroup = "";
   private static long lastReplaceAtMs = 0L;
   private static final long REPLACE_WINDOW_MS = 4500L;
   private static final Set<String> SENT_MESSAGE_KEYS = new LinkedHashSet<>();
   private static final ThreadLocal<List<String>> CAPTURE = new ThreadLocal<>();

   private AutismClientMessaging() {
   }

   public static AutismClientMessaging.Inner_DataUtil capture(List<String> sink) {
      List<String> previous = CAPTURE.get();
      CAPTURE.set(sink);
      return () -> {
         if (previous == null) {
            CAPTURE.remove();
         } else {
            CAPTURE.set(previous);
         }
      };
   }

   public static void send(String message) {
      deliver(buildBodyText(message), message);
   }

   public static void sendPrefixed(String message) {
      MutableComponent text = Component.empty().append(themedTag("AUTISM")).append(buildBodyText(message));
      deliver(text, message);
   }

   public static void sendPrefixed(String message, Component action) {
      deliver(themedTag("AUTISM").append(buildBodyText(message)).append(action), message + action.getString(), false);
   }

   public static MutableComponent themedTag(String tag) {
      return Component.empty().append(styled("[", p.a())).append(styled(tag == null ? "" : tag, p.init(), true)).append(styled("] ", p.a()));
   }

   public static MutableComponent themedBody(String message) {
      return buildBodyText(message == null ? "" : message);
   }

   public static void send(Component text) {
      deliver(text, null);
   }

   private static void deliver(Component text, String raw) {
      deliver(text, raw, true);
   }

   private static void deliver(Component text, String raw, boolean replaceRecent) {
      if (text != null) {
         List<String> sink = CAPTURE.get();
         if (sink != null) {
            sink.add(raw != null ? raw : text.getString());
         }

         if (MC != null && !autismclient.modules.aD.write_Z()) {
            String key = normalizeDuplicateKey(text.getString());
            long now = System.currentTimeMillis();
            synchronized (AutismClientMessaging.class) {
               if (!key.isEmpty() && key.equals(lastMessageKey) && now - lastMessageAtMs <= 600L) {
                  return;
               }

               lastMessageKey = key;
               lastMessageAtMs = now;
               if (!key.isEmpty()) {
                  SENT_MESSAGE_KEYS.remove(key);
                  while (SENT_MESSAGE_KEYS.size() >= 2048) {
                     var it = SENT_MESSAGE_KEYS.iterator();
                     it.next();
                     it.remove();
                  }
                  SENT_MESSAGE_KEYS.add(key);
               }
            }

            String replaceGroup = replaceRecent ? replacementGroupKey(text.getString()) : "";
            MC.execute(() -> {
               if (MC.player != null) {
                  if (!replaceGroup.isEmpty()) {
                     removeReplaceableMessage(replaceGroup);
                  }

                  MC.gui.hud.getChat().addClientSystemMessage(text);
               } else if (MC.gui.chatListener() != null) {
                  MC.gui.chatListener().handleSystemMessage(text, false);
               }
            });
         }
      }
   }

   public static void clearClientMessages() {
      if (MC != null) {
         MC.execute(() -> {
            if (MC.gui != null && MC.gui.hud.getChat() != null) {
               try {
                  AutismChatComponentAccessor chat = (AutismChatComponentAccessor)MC.gui.hud.getChat();
                  List<GuiMessage> messages = chat.autism$getAllMessages();
                  boolean changed = false;
                  ListIterator<GuiMessage> iterator = messages.listIterator();

                  while (iterator.hasNext()) {
                     GuiMessage existing = iterator.next();
                     if (isClientGeneratedMessage(existing.content().getString())) {
                        iterator.remove();
                        changed = true;
                     }
                  }

                  if (changed) {
                     chat.autism$refreshTrimmedMessages();
                  }
               } catch (Throwable var5) {
               }
            }
         });
      }
   }

   public static void rememberRecentChat(String line) {
      if (MC != null && line != null && !line.isBlank()) {
         MC.execute(() -> {
            try {
               if (MC.gui == null || MC.gui.hud.getChat() == null) {
                  return;
               }

               ChatComponent chat = MC.gui.hud.getChat();
               chat.addRecentChat(line);
               if (autismclient.commands.CommandManager.init_Z(line)) {
                  CommandHistory history = ((AutismChatComponentAccessor)chat).autism$getCommandHistory();
                  if (history != null) {
                     history.addCommand(line);
                  }
               }
            } catch (Throwable var3) {
            }
         });
      }
   }

   public static void clearChatInputHistory() {
      if (MC != null) {
         MC.execute(() -> {
            try {
               if (MC.gui == null || MC.gui.hud.getChat() == null) {
                  return;
               }

               ChatComponent chat = MC.gui.hud.getChat();
               chat.getRecentChat().clear();
               CommandHistory history = ((AutismChatComponentAccessor)chat).autism$getCommandHistory();
               if (history == null) {
                  return;
               }

               AutismCommandHistoryAccessor accessor = (AutismCommandHistoryAccessor)history;
               accessor.autism$getLastCommands().clear();
               accessor.autism$save();
            } catch (Throwable var3) {
            }
         });
      }
   }

   private static boolean isClientGeneratedMessage(String message) {
      String clean = stripLegacyCodes(normalizeLegacyFormatting(message)).replaceAll("\\s+", " ").trim();
      String lower = clean.toLowerCase(Locale.ROOT);
      if (!lower.startsWith("[autism]") && !lower.startsWith("[lan]")) {
         String key = normalizeDuplicateKey(message);
         synchronized (AutismClientMessaging.class) {
            return !key.isEmpty() && SENT_MESSAGE_KEYS.contains(key);
         }
      } else {
         return true;
      }
   }

   private static MutableComponent buildBodyText(String message) {
      String normalized = normalizeLegacyFormatting(message);
      MutableComponent lanChat = tryBuildLanChatText(normalized);
      if (lanChat != null) {
         return lanChat;
      }

      int defaultColor = inferDefaultColor(stripLegacyCodes(normalized));
      return normalized.indexOf(167) >= 0 ? parseLegacyStyled(normalized, defaultColor) : colorizeStructuredText(normalized, defaultColor);
   }

   private static MutableComponent tryBuildLanChatText(String message) {
      if (message != null && message.startsWith("[LAN] <")) {
         int close = message.indexOf("> ");
         if (close <= 7) {
            return null;
         }

         String sender = message.substring(7, close);
         String body = message.substring(close + 2);
         return Component.empty()
            .append(styled("[", p.a()))
            .append(styled("LAN", p.node(), true))
            .append(styled("] ", p.a()))
            .append(styled("<", p.a()))
            .append(styled(sender, p.state(), true))
            .append(styled("> ", p.a()))
            .append(colorizeTokenStream(body, p.scan()));
      } else {
         return null;
      }
   }

   private static MutableComponent colorizeStructuredText(String message, int defaultColor) {
      if (message != null && !message.isEmpty()) {
         int colon = message.indexOf(58);
         if (colon > 0 && colon < 24 && isLikelyLabel(message.substring(0, colon))) {
            String label = message.substring(0, colon);
            String rest = message.substring(colon + 1);
            MutableComponent root = Component.empty().append(styled(label, p.pool(), true)).append(styled(":", p.a()));
            if (!rest.isEmpty()) {
               root.append(styled(leadingWhitespace(rest), p.a()));
               root.append(colorizeTokenStream(rest.stripLeading(), inferDefaultColor(rest.trim())));
            }

            return root;
         } else {
            return colorizeTokenStream(message, defaultColor);
         }
      } else {
         return Component.empty();
      }
   }

   private static MutableComponent colorizeTokenStream(String message, int defaultColor) {
      MutableComponent root = Component.empty();
      if (message != null && !message.isEmpty()) {
         StringBuilder token = new StringBuilder();

         for (int i = 0; i < message.length(); i++) {
            char ch = message.charAt(i);
            if (!Character.isLetterOrDigit(ch) && ch != '_' && ch != '-' && ch != '#' && ch != '.' && ch != '/') {
               if (token.length() > 0) {
                  String word = token.toString();
                  root.append(styled(word, colorForToken(word, defaultColor), isEmphasizedToken(word)));
                  token.setLength(0);
               }

               root.append(styled(String.valueOf(ch), punctuationColor(ch, defaultColor)));
            } else {
               token.append(ch);
            }
         }

         if (token.length() > 0) {
            String word = token.toString();
            root.append(styled(word, colorForToken(word, defaultColor), isEmphasizedToken(word)));
         }

         return root;
      } else {
         return root;
      }
   }

   private static boolean isLikelyLabel(String value) {
      if (value != null && !value.isBlank()) {
         for (int i = 0; i < value.length(); i++) {
            char ch = value.charAt(i);
            if (!Character.isLetterOrDigit(ch) && !Character.isWhitespace(ch) && ch != '[' && ch != ']' && ch != '+' && ch != '-' && ch != '/' && ch != '#') {
               return false;
            }
         }

         return true;
      } else {
         return false;
      }
   }

   private static String leadingWhitespace(String value) {
      if (value != null && !value.isEmpty()) {
         int i = 0;

         while (i < value.length() && Character.isWhitespace(value.charAt(i))) {
            i++;
         }

         return value.substring(0, i);
      } else {
         return "";
      }
   }

   private static int punctuationColor(char ch, int defaultColor) {
      int var10000;
      switch (ch) {
         case '"':
            var10000 = p.index();
            break;
         case '(':
         case ')':
         case ',':
         case ':':
         case '<':
         case '>':
         case '[':
         case ']':
         case '|':
            var10000 = p.a();
            break;
         default:
            var10000 = defaultColor;
      }

      return var10000;
   }

   private static boolean isEmphasizedToken(String token) {
      String normalized = normalizeToken(token);
      return normalized.equals("enabled")
         || normalized.equals("disabled")
         || normalized.equals("started")
         || normalized.equals("stopped")
         || normalized.equals("failed")
         || normalized.equals("error")
         || normalized.equals("queue")
         || normalized.equals("packet")
         || normalized.equals("packets")
         || normalized.equals("gui")
         || normalized.equals("macro")
         || normalized.equals("macros")
         || normalized.equals("sync")
         || normalized.equals("lan");
   }

   private static int colorForToken(String token, int defaultColor) {
      String normalized = normalizeToken(token);
      if (normalized.isEmpty()) {
         return defaultColor;
      } else if (isNumericToken(normalized)) {
         return p.index();
      } else if (normalized.equals("enabled")
         || normalized.equals("started")
         || normalized.equals("joined")
         || normalized.equals("restored")
         || normalized.equals("saved")
         || normalized.equals("loaded")
         || normalized.equals("copied")
         || normalized.equals("imported")
         || normalized.equals("received")
         || normalized.equals("executed")
         || normalized.equals("finished")
         || normalized.equals("reconnected")
         || normalized.equals("host")
         || normalized.equals("sent")
         || normalized.equals("on")
         || normalized.equals("true")) {
         return p.value();
      } else if (normalized.equals("disabled")
         || normalized.equals("stopped")
         || normalized.equals("empty")
         || normalized.equals("already")
         || normalized.equals("requested")
         || normalized.equals("left")
         || normalized.equals("attempting")
         || normalized.equals("timeout")
         || normalized.equals("timed")
         || normalized.equals("out")
         || normalized.equals("off")
         || normalized.equals("false")) {
         return p.entry();
      } else if (normalized.equals("failed")
         || normalized.equals("error")
         || normalized.equals("errors")
         || normalized.equals("denied")
         || normalized.equals("blocked")
         || normalized.equals("crashed")
         || normalized.equals("cannot")
         || normalized.equals("missing")
         || normalized.equals("unavailable")
         || normalized.equals("invalid")
         || normalized.equals("not")
         || normalized.equals("null")) {
         return p.valid();
      } else if (normalized.equals("queue")
         || normalized.equals("packet")
         || normalized.equals("packets")
         || normalized.equals("sync")
         || normalized.equals("lan")
         || normalized.equals("session")
         || normalized.equals("tcp")) {
         return p.node();
      } else if (!normalized.equals("gui")
         && !normalized.equals("screen")
         && !normalized.equals("inventory")
         && !normalized.equals("slot")
         && !normalized.equals("slots")) {
         return !normalized.equals("macro") && !normalized.equals("macros") && !normalized.equals("preset") && !normalized.equals("presets")
            ? defaultColor
            : p.state();
      } else {
         return p.queue();
      }
   }

   private static boolean isNumericToken(String token) {
      int start = token.startsWith("-") ? 1 : 0;
      if (start >= token.length()) {
         return false;
      }

      for (int i = start; i < token.length(); i++) {
         char ch = token.charAt(i);
         if (!Character.isDigit(ch) && ch != '.' && ch != '/' && ch != '#') {
            return false;
         }
      }

      return true;
   }

   private static String normalizeToken(String token) {
      if (token != null && !token.isEmpty()) {
         int start = 0;
         int end = token.length();

         while (start < end && !Character.isLetterOrDigit(token.charAt(start))) {
            start++;
         }

         while (end > start && !Character.isLetterOrDigit(token.charAt(end - 1))) {
            end--;
         }

         return start >= end ? "" : token.substring(start, end).toLowerCase();
      } else {
         return "";
      }
   }

   private static MutableComponent parseLegacyStyled(String message, int defaultColor) {
      MutableComponent root = Component.empty();
      if (message != null && !message.isEmpty()) {
         StringBuilder buffer = new StringBuilder();
         int currentColor = defaultColor;
         boolean bold = false;

         for (int i = 0; i < message.length(); i++) {
            char ch = message.charAt(i);
            if (ch == 167 && i + 1 < message.length()) {
               if (buffer.length() > 0) {
                  root.append(styled(buffer.toString(), currentColor, bold));
                  buffer.setLength(0);
               }

               char code = Character.toLowerCase(message.charAt(++i));
               Integer mapped = mapLegacyColor(code);
               if (mapped != null) {
                  currentColor = mapped;
                  if (isColorCode(code) || code == 'r') {
                     bold = false;
                  }
               } else if (code == 'l') {
                  bold = true;
               } else if (code == 'r') {
                  currentColor = defaultColor;
                  bold = false;
               }
            } else {
               buffer.append(ch);
            }
         }

         if (buffer.length() > 0) {
            root.append(styled(buffer.toString(), currentColor, bold));
         }

         return root;
      } else {
         return root;
      }
   }

   private static MutableComponent styled(String value, int color) {
      return styled(value, color, false);
   }

   private static MutableComponent styled(String value, int color, boolean bold) {
      Style style = Style.EMPTY.withColor(color);
      if (bold) {
         style = style.withBold(true);
      }

      return Component.literal(value).setStyle(style);
   }

   private static boolean isColorCode(char code) {
      return code >= '0' && code <= '9' || code >= 'a' && code <= 'f';
   }

   private static Integer mapLegacyColor(char code) {
      Integer var10000;
      switch (code) {
         case '0':
            var10000 = -10597554;
            break;
         case '1':
            var10000 = -9727272;
            break;
         case '2':
            var10000 = -10897036;
            break;
         case '3':
            var10000 = p.node();
            break;
         case '4':
            var10000 = p.valid();
            break;
         case '5':
            var10000 = -1602869;
            break;
         case '6':
            var10000 = p.queue();
            break;
         case '7':
            var10000 = p.pool();
            break;
         case '8':
            var10000 = p.a();
            break;
         case '9':
            var10000 = p.buffer();
            break;
         case ':':
         case ';':
         case '<':
         case '=':
         case '>':
         case '?':
         case '@':
         case 'A':
         case 'B':
         case 'C':
         case 'D':
         case 'E':
         case 'F':
         case 'G':
         case 'H':
         case 'I':
         case 'J':
         case 'K':
         case 'L':
         case 'M':
         case 'N':
         case 'O':
         case 'P':
         case 'Q':
         case 'R':
         case 'S':
         case 'T':
         case 'U':
         case 'V':
         case 'W':
         case 'X':
         case 'Y':
         case 'Z':
         case '[':
         case '\\':
         case ']':
         case '^':
         case '_':
         case '`':
         case 'g':
         case 'h':
         case 'i':
         case 'j':
         case 'k':
         case 'l':
         case 'm':
         case 'n':
         case 'o':
         case 'p':
         case 'q':
         default:
            var10000 = null;
            break;
         case 'a':
            var10000 = p.value();
            break;
         case 'b':
            var10000 = p.node();
            break;
         case 'c':
            var10000 = p.valid();
            break;
         case 'd':
            var10000 = p.state();
            break;
         case 'e':
            var10000 = p.entry();
            break;
         case 'f':
            var10000 = p.parse();
            break;
         case 'r':
            var10000 = null;
      }

      return var10000;
   }

   private static String normalizeLegacyFormatting(String message) {
      return message == null ? "" : message.replace("Â§", "§");
   }

   private static String stripLegacyCodes(String message) {
      if (message != null && !message.isEmpty()) {
         StringBuilder sb = new StringBuilder(message.length());

         for (int i = 0; i < message.length(); i++) {
            char ch = message.charAt(i);
            if (ch == 167 && i + 1 < message.length()) {
               i++;
            } else {
               sb.append(ch);
            }
         }

         return sb.toString().trim();
      } else {
         return "";
      }
   }

   private static int inferDefaultColor(String message) {
      String lower = message == null ? "" : message.trim().toLowerCase();
      if (lower.isEmpty()) {
         return p.parse();
      } else if (lower.contains("failed")
         || lower.contains("error")
         || lower.startsWith("no ")
         || lower.startsWith("not ")
         || lower.contains(" not ")
         || lower.contains("cannot")
         || lower.contains("denied")
         || lower.contains("blocked")
         || lower.contains("crashed")) {
         return p.valid();
      } else if (lower.contains("enabled")
         || lower.contains("started")
         || lower.contains("joined")
         || lower.contains("restored")
         || lower.contains("saved")
         || lower.contains("loaded")
         || lower.contains("copied")
         || lower.contains("imported")
         || lower.contains("received")
         || lower.contains("executed")
         || lower.contains("finished")
         || lower.startsWith("sent ")
         || lower.startsWith("signed ")
         || lower.startsWith("deleted ")
         || lower.startsWith("broadcasted ")
         || lower.startsWith("queue sent")) {
         return p.value();
      } else if (!lower.contains("disabled")
         && !lower.contains("stopped")
         && !lower.contains("empty")
         && !lower.contains("already")
         && !lower.contains("requested")
         && !lower.contains("left")
         && !lower.contains("attempting")
         && !lower.contains("timed out")) {
         return lower.startsWith("[lan] <") ? p.scan() : p.parse();
      } else {
         return p.entry();
      }
   }

   private static String normalizeDuplicateKey(String message) {
      return message == null ? "" : stripLegacyCodes(normalizeLegacyFormatting(message)).replace("[AUTISM] ", "").replaceAll("\\s+", " ").trim().toLowerCase();
   }

   private static String replacementGroupKey(String message) {
      String clean = stripAutismPrefix(stripLegacyCodes(normalizeLegacyFormatting(message)).replaceAll("\\s+", " ").trim());
      if (clean.isBlank()) {
         return "";
      }

      String lower = clean.toLowerCase(Locale.ROOT);
      if (lower.startsWith("[lan]")) {
         return "";
      }

      if (isErrorLike(lower)) {
         return "";
      }

      int colon = clean.indexOf(58);
      if (colon > 0 && colon <= 38) {
         String label = clean.substring(0, colon).trim();
         if (isReplaceableLabel(label)) {
            return "label:" + label.toLowerCase(Locale.ROOT);
         }
      }

      if (lower.startsWith("macro stopped")) {
         return "macro:stopped";
      } else if (lower.startsWith("macro finished")) {
         return "macro:finished";
      } else if (lower.startsWith("tick sync:")) {
         return "macro:tick-sync";
      } else if (lower.startsWith("rev sync:")) {
         return "macro:rev-sync";
      } else {
         return lower.startsWith("serversync:") ? "macro:server-sync" : "";
      }
   }

   private static void removeReplaceableMessage(String group) {
      long now = System.currentTimeMillis();
      synchronized (AutismClientMessaging.class) {
         if (!group.equals(lastReplaceGroup) || now - lastReplaceAtMs > 4500L) {
            lastReplaceGroup = group;
            lastReplaceAtMs = now;
            return;
         }

         lastReplaceAtMs = now;
      }

      try {
         AutismChatComponentAccessor chat = (AutismChatComponentAccessor)MC.gui.hud.getChat();
         List<GuiMessage> messages = chat.autism$getAllMessages();
         ListIterator<GuiMessage> iterator = messages.listIterator();

         while (iterator.hasNext()) {
            GuiMessage existing = iterator.next();
            if (group.equals(replacementGroupKey(existing.content().getString()))) {
               iterator.remove();
               chat.autism$refreshTrimmedMessages();
               return;
            }
         }
      } catch (Throwable var7) {
      }
   }

   private static String stripAutismPrefix(String message) {
      if (message == null) {
         return "";
      } else {
         String trimmed = message.trim();
         if (trimmed.startsWith("[AUTISM] ")) {
            return trimmed.substring("[AUTISM] ".length()).trim();
         } else {
            return trimmed.startsWith("[AUTISM]") ? trimmed.substring("[AUTISM]".length()).trim() : trimmed;
         }
      }
   }

   private static boolean isReplaceableLabel(String label) {
      if (label != null && !label.isBlank()) {
         String normalized = label.trim().toLowerCase(Locale.ROOT);
         if (!normalized.contains("failed") && !normalized.contains("error") && !normalized.contains("invalid") && !normalized.contains("missing")) {
            return !normalized.equals("saved packet canceller preset") && !normalized.equals("loaded packet canceller preset") ? isLikelyLabel(label) : false;
         } else {
            return false;
         }
      } else {
         return false;
      }
   }

   private static boolean isErrorLike(String lower) {
      return lower.startsWith("failed")
         || lower.startsWith("error")
         || lower.startsWith("invalid")
         || lower.startsWith("missing")
         || lower.startsWith("cannot")
         || lower.contains(" crashed:");
   }

   public interface Inner_DataUtil extends AutoCloseable {
      @Override
      void close();
   }
}

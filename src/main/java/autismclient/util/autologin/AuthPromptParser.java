package autismclient.util.autologin;

import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class AuthPromptParser {
   private static final Pattern REGISTER = Pattern.compile("(?i)/reg(?:ister)?\\b");
   private static final Pattern LOGIN = Pattern.compile("(?i)/log(?:in)?\\b");
   private static final Pattern PLACEHOLDER = Pattern.compile("<[^>]{1,32}>");

   private AuthPromptParser() {
   }

   public static AuthPromptParser.AuthPrompt parse(String rawLine) {
      if (rawLine == null) {
         return AuthPromptParser.AuthPrompt.NONE;
      } else {
         String line = rawLine.trim();
         if (line.isEmpty()) {
            return AuthPromptParser.AuthPrompt.NONE;
         } else {
            Matcher reg = REGISTER.matcher(line);
            if (reg.find()) {
               String command = line.substring(reg.start(), reg.end()).toLowerCase(Locale.ROOT);
               return new AuthPromptParser.AuthPrompt(AuthPromptParser.PromptKind.REGISTER, command, placeholderCount(line, reg.end()));
            } else {
               Matcher log = LOGIN.matcher(line);
               return log.find()
                  ? new AuthPromptParser.AuthPrompt(AuthPromptParser.PromptKind.LOGIN, line.substring(log.start(), log.end()).toLowerCase(Locale.ROOT), 1)
                  : AuthPromptParser.AuthPrompt.NONE;
            }
         }
      }
   }

   public static String loginCommandFor(String registerCommand, String seenLoginCommand) {
      if (seenLoginCommand != null && !seenLoginCommand.isBlank()) {
         return seenLoginCommand;
      } else {
         return "/reg".equalsIgnoreCase(registerCommand == null ? "" : registerCommand.trim()) ? "/log" : "/login";
      }
   }

   private static int placeholderCount(String line, int afterCommand) {
      String tail = line.substring(afterCommand);
      Matcher ph = PLACEHOLDER.matcher(tail);
      int count = 0;
      boolean email = false;

      while (ph.find()) {
         count++;
         String token = ph.group().toLowerCase(Locale.ROOT);
         if (token.contains("mail") || token.contains("correo") || token.contains("courriel")) {
            email = true;
         }
      }

      if (email) {
         return 1;
      } else {
         return count == 1 ? 1 : 2;
      }
   }

   public record AuthPrompt(AuthPromptParser.PromptKind kind, String command, int init_I) {
      public static final AuthPromptParser.AuthPrompt NONE = new AuthPromptParser.AuthPrompt(AuthPromptParser.PromptKind.NONE, "", 0);

      public String loginCommand(String password) {
         String line = this.command + " " + password;
         return this.init_I >= 2 ? line + " " + password : line;
      }
   }

   public enum PromptKind {
      NONE,
      REGISTER,
      LOGIN;
   }
}

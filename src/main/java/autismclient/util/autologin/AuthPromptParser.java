package autismclient.util.autologin;

import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class AuthPromptParser {
   private static final Pattern init = Pattern.compile("(?i)/reg(?:ister)?\\b");
   private static final Pattern load = Pattern.compile("(?i)/log(?:in)?\\b");
   private static final Pattern read = Pattern.compile("<[^>]{1,32}>");

   private AuthPromptParser() {
   }

   public static AuthPromptParser.Inner_DataUtil init(String rawLine) {
      if (rawLine == null) {
         return AuthPromptParser.Inner_DataUtil.init_DataUtil_DataUtil;
      } else {
         String line = rawLine.trim();
         if (line.isEmpty()) {
            return AuthPromptParser.Inner_DataUtil.init_DataUtil_DataUtil;
         } else {
            Matcher reg = init.matcher(line);
            if (reg.find()) {
               String command = line.substring(reg.start(), reg.end()).toLowerCase(Locale.ROOT);
               return new AuthPromptParser.Inner_DataUtil(AuthPromptParser.Inner_StreamUtil.REGISTER, command, init(line, reg.end()));
            } else {
               Matcher log = load.matcher(line);
               return log.find()
                  ? new AuthPromptParser.Inner_DataUtil(AuthPromptParser.Inner_StreamUtil.LOGIN, line.substring(log.start(), log.end()).toLowerCase(Locale.ROOT), 1)
                  : AuthPromptParser.Inner_DataUtil.init_DataUtil_DataUtil;
            }
         }
      }
   }

   public static String init(String registerCommand, String seenLoginCommand) {
      if (seenLoginCommand != null && !seenLoginCommand.isBlank()) {
         return seenLoginCommand;
      } else {
         return "/reg".equalsIgnoreCase(registerCommand == null ? "" : registerCommand.trim()) ? "/log" : "/login";
      }
   }

   private static int init(String line, int afterCommand) {
      String tail = line.substring(afterCommand);
      Matcher ph = read.matcher(tail);
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

   public record Inner_DataUtil(AuthPromptParser.Inner_StreamUtil init_DataUtil_StreamUti, String init_Str, int init_I) {
      public static final AuthPromptParser.Inner_DataUtil init_DataUtil_DataUtil = new AuthPromptParser.Inner_DataUtil(AuthPromptParser.Inner_StreamUtil.NONE, "", 0);

      public String init(String password) {
         String line = this.init_Str + " " + password;
         return this.init_I >= 2 ? line + " " + password : line;
      }
   }

   public enum Inner_StreamUtil {
      NONE,
      REGISTER,
      LOGIN;
   }
}

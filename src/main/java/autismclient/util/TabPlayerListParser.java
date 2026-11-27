package autismclient.util;

import autismclient.util.macro.MacroExecutor;
import com.mojang.authlib.GameProfile;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.world.scores.PlayerTeam;
import net.minecraft.world.scores.Scoreboard;

public final class TabPlayerListParser {
   private TabPlayerListParser() {
   }

   public static List<TabPlayerListParser.ParsedPlayer> init(Minecraft mc) {
      Map<String, TabPlayerListParser.ParsedPlayer> byName = new LinkedHashMap<>();
      if (mc == null) {
         return new ArrayList<>();
      }

      String self = mc.player != null && mc.player.getGameProfile() != null ? mc.player.getGameProfile().name() : null;
      Scoreboard scoreboard = mc.level != null ? mc.level.getScoreboard() : null;
      ClientPacketListener connection = mc.getConnection();
      if (connection != null) {
         for (PlayerInfo info : connection.getOnlinePlayers()) {
            if (info != null) {
               TabPlayerListParser.RawEntry parsed = init_bO_DataUtil(info.getTabListDisplayName());
               if (parsed != null && isOtherPlayerName(parsed.init_Str(), self)) {
                  init(byName, parsed.init_Str(), parsed.load(), parsed.prefixComponent());
               }
            }
         }

         for (PlayerInfo info : connection.getOnlinePlayers()) {
            if (info != null && init(info.getProfile())) {
               String name = info.getProfile().name();
               if (isOtherPlayerName(name, self)) {
                  init(byName, name, init(scoreboard, name, info), null);
               }
            }
         }
      }

      if (mc.level != null) {
         for (AbstractClientPlayer player : mc.level.players()) {
            if (player != null && init(player.getGameProfile())) {
               String name = player.getGameProfile().name();
               if (isOtherPlayerName(name, self)) {
                  PlayerInfo info = connection == null ? null : connection.getPlayerInfoIgnoreCase(name);
                  init(byName, name, init(scoreboard, name, info), null);
               }
            }
         }
      }

      init(byName);
      List<TabPlayerListParser.ParsedPlayer> result = new ArrayList<>(byName.values());
      result.sort((a, b) -> a.name().compareToIgnoreCase(b.name()));
      return result;
   }

   private static void init(Map<String, TabPlayerListParser.ParsedPlayer> byName) {
      if (!byName.isEmpty() && !byName.values().stream().allMatch(TabPlayerListParser.ParsedPlayer::hasPrefix)) {
         List<String> keys = new ArrayList<>(byName.keySet());
         keys.sort((a, b) -> Integer.compare(b.length(), a.length()));

         try {
            for (MacroExecutor.LoggedChatMessage chat : MacroExecutor.getRecentChatMessages()) {
               if (chat != null && chat.displayText() != null && !chat.displayText().isBlank()) {
                  String display = chat.displayText();
                  int sep = display.indexOf(187);
                  String header = sep > 0 ? display.substring(0, sep) : display;
                  String lowerHeader = header.toLowerCase(Locale.ROOT);

                  for (String key : keys) {
                     TabPlayerListParser.ParsedPlayer player = byName.get(key);
                     if (player != null && !player.hasPrefix()) {
                        int nameIdx = lowerHeader.indexOf(key);
                        if (nameIdx > 0) {
                           String rank = read_Str(header.substring(0, nameIdx));
                           if (!rank.isBlank()) {
                              byName.put(key, new TabPlayerListParser.ParsedPlayer(player.name(), rank, null));
                              break;
                           }
                        }
                     }
                  }
               }
            }
         } catch (Throwable var13) {
         }
      }
   }

   private static boolean isOtherPlayerName(String name, String self) {
      return name != null && !name.isBlank() && (self == null || !name.equalsIgnoreCase(self));
   }

   private static boolean init(GameProfile profile) {
      return profile != null && isRealPlayerName(profile.name());
   }

   public static boolean isRealPlayerName(String name) {
      return isPlausibleName(name) && !read_Z(name);
   }

   public static boolean isPlausibleName(String name) {
      if (name == null) {
         return false;
      }

      int len = name.length();
      if (len >= 2 && len <= 32) {
         boolean hasAlnum = false;

         for (int i = 0; i < len; i++) {
            char c = name.charAt(i);
            boolean alnum = c >= 'A' && c <= 'Z' || c >= 'a' && c <= 'z' || c >= '0' && c <= '9';
            if (alnum) {
               hasAlnum = true;
            } else if (c != '_' && c != '.' && c != ' ') {
               return false;
            }
         }

         return hasAlnum;
      } else {
         return false;
      }
   }

   private static boolean read_Z(String name) {
      String lower = name.toLowerCase(Locale.ROOT).trim();
      return lower.matches("(slot|npc|line|spacer|empty|blank|grid|hud|tab|frame|fill)_?-?\\d+") || lower.matches("[#.\\-_ ]*\\d{1,4}[#.\\-_ ]*");
   }

   private static void init(Map<String, TabPlayerListParser.ParsedPlayer> byName, String name, String prefix, Component prefixComponent) {
      String key = name.toLowerCase(Locale.ROOT);
      TabPlayerListParser.ParsedPlayer existing = byName.get(key);
      if (existing == null) {
         byName.put(key, new TabPlayerListParser.ParsedPlayer(name, prefix, prefixComponent));
      } else if (!existing.hasPrefix() && prefix != null && !prefix.isBlank()) {
         byName.put(key, new TabPlayerListParser.ParsedPlayer(existing.name(), prefix, prefixComponent));
      }
   }

   private static TabPlayerListParser.RawEntry init_bO_DataUtil(Component display) {
      if (display == null) {
         return null;
      }

      String text = init_Str(display);
      if (text.isBlank()) {
         return null;
      }

      String rank = read_Str(text);
      if (rank.isBlank()) {
         return null;
      }

      String name = load_Str(text);
      return !name.isBlank() && isPlausibleName(name) && !read_Z(name) ? new TabPlayerListParser.RawEntry(name, rank, init_MutableComponent(display)) : null;
   }

   private static MutableComponent init_MutableComponent(Component display) {
      if (display == null) {
         return null;
      }

      MutableComponent out = Component.empty();
      boolean[] any = new boolean[]{false};
      boolean[] stopped = new boolean[]{false};
      display.visit((style, content) -> {
         StringBuilder seg = new StringBuilder();

         for (int i = 0; i < content.length(); i++) {
            char c = content.charAt(i);
            if (c > 127 && !Character.isWhitespace(c)) {
               seg.append(c);
            } else if (seg.length() != 0 || any[0] || !Character.isWhitespace(c)) {
               stopped[0] = true;
               break;
            }
         }

         if (seg.length() > 0) {
            out.append(Component.literal(seg.toString()).setStyle(style));
            any[0] = true;
         }

         return stopped[0] ? Optional.of(Boolean.TRUE) : Optional.empty();
      }, Style.EMPTY);
      return any[0] ? out : null;
   }

   private static String load_Str(String text) {
      int last = -1;

      for (int i = 0; i < text.length(); i++) {
         char c = text.charAt(i);
         if (c > 127 && !Character.isWhitespace(c)) {
            last = i;
         }
      }

      String tail = last < 0 ? text : text.substring(last + 1);
      return write(tail).trim();
   }

   private static String init(Scoreboard scoreboard, String name, PlayerInfo info) {
      try {
         Component display = info != null ? info.getTabListDisplayName() : null;
         String fromDisplay = init_Str(init_Str(display), name);
         if (!fromDisplay.isBlank()) {
            return fromDisplay;
         }

         PlayerTeam team = info != null ? info.getTeam() : (scoreboard != null ? scoreboard.getPlayersTeam(name) : null);
         if (team != null) {
            String fromPrefix = init_Str(init_Str(team.getPlayerPrefix()), name);
            if (!fromPrefix.isBlank()) {
               return fromPrefix;
            }

            String fromSuffix = init_Str(init_Str(team.getPlayerSuffix()), name);
            if (!fromSuffix.isBlank()) {
               return fromSuffix;
            }

            String fromTeam = init_Str(init_Str(PlayerTeam.formatNameForTeam(team, Component.literal(name))), name);
            if (!fromTeam.isBlank()) {
               return fromTeam;
            }
         }

         return "";
      } catch (Throwable ignored) {
         return "";
      }
   }

   private static String init_Str(String text, String name) {
      if (text != null && !text.isBlank()) {
         String glyphs = read_Str(text);
         if (!glyphs.isBlank()) {
            return glyphs;
         }

         String ascii = write(text);
         int idx = indexOfName(ascii, name);
         if (idx > 0) {
            String prefix = ascii.substring(0, idx).trim();
            if (!prefix.isBlank()) {
               return prefix;
            }
         }

         return idx < 0 && ascii.length() <= 24 ? ascii : "";
      } else {
         return "";
      }
   }

   private static int indexOfName(String text, String name) {
      if (text != null && name != null && !name.isBlank()) {
         String lower = text.toLowerCase(Locale.ROOT);
         int idx = lower.indexOf(name.toLowerCase(Locale.ROOT));
         if (idx < 0 && name.startsWith(".") && name.length() > 1) {
            idx = lower.indexOf(name.substring(1).toLowerCase(Locale.ROOT));
         }

         return idx;
      } else {
         return -1;
      }
   }

   private static String read_Str(String text) {
      StringBuilder sb = new StringBuilder();

      for (int i = 0; i < text.length(); i++) {
         char c = text.charAt(i);
         if (c > 127 && !Character.isWhitespace(c)) {
            sb.append(c);
         }
      }

      return sb.toString().trim();
   }

   private static String write(String text) {
      return text == null ? "" : text.replaceFirst("^\\s*\\[\\s*\\d+\\s*\\]\\s*", "").trim();
   }

   private static String init_Str(Component component) {
      return component == null ? "" : init_Str(component.getString());
   }

   public static String init_Str(String text) {
      if (text == null) {
         return "";
      }

      StringBuilder out = new StringBuilder(text.length());

      for (int i = 0; i < text.length(); i++) {
         char c = text.charAt(i);
         if (c == 167 && i + 1 < text.length()) {
            char code = Character.toLowerCase(text.charAt(i + 1));
            if ("0123456789abcdefklmnorx".indexOf(code) >= 0) {
               i++;
               continue;
            }
         }

         out.append(c);
      }

      return out.toString().trim();
   }

   private record RawEntry(String init_Str, String load, Component prefixComponent) {
   }

   public record ParsedPlayer(String name, String load, Component prefixComponent) {
      public boolean hasPrefix() {
         return this.load != null && !this.load.isBlank();
      }
   }
}

package autismclient.modules;

import java.util.Optional;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.scores.PlayerTeam;
import net.minecraft.world.scores.TeamColor;

final class b {
   private b() {
   }

   static DyeColor init() {
      Minecraft mc = Minecraft.getInstance();
      if (mc.player != null && mc.getConnection() != null) {
         PlayerTeam team = mc.player.getTeam();
         TextColor color = team == null ? null : team.getColor().map(TeamColor::textColor).orElse(null);
         String name = mc.player.getGameProfile().name();
         PlayerInfo info = mc.getConnection().getPlayerInfo(mc.player.getUUID());
         return init(name, info == null ? null : info.getTabListDisplayName(), mc.player.getDisplayName(), color == null ? null : color.getValue());
      } else {
         return null;
      }
   }

   static DyeColor init(String name, Component tab, Component display, Integer teamRgb) {
      b.Inner_DataUtil tabName = init_b_DataUtil(name, tab);
      b.Inner_DataUtil shownName = init_b_DataUtil(name, display);
      if (tabName != null && tabName.init_DyeColor() != null) {
         return tabName.init_DyeColor();
      } else if (shownName != null && shownName.init_DyeColor() != null) {
         return shownName.init_DyeColor();
      } else if (teamRgb != null) {
         return init(teamRgb);
      } else {
         return shownName != null && shownName.init_Z() || tabName != null && tabName.init_Z() ? DyeColor.WHITE : null;
      }
   }

   static DyeColor load(String name, Component tab, Component display, Integer teamRgb) {
      DyeColor tabColor = init_DyeColor(name, tab);
      DyeColor shownColor = init_DyeColor(name, display);
      DyeColor teamColor = teamRgb == null ? null : init(teamRgb);
      DyeColor settled = null;

      for (DyeColor candidate : new DyeColor[]{tabColor, shownColor, teamColor}) {
         if (candidate != null && candidate != DyeColor.WHITE) {
            if (settled != null && settled != candidate) {
               return null;
            }

            settled = candidate;
         }
      }

      if (settled != null) {
         return settled;
      } else if (teamColor != null) {
         return teamColor;
      } else {
         return tabColor != DyeColor.WHITE && shownColor != DyeColor.WHITE ? null : DyeColor.WHITE;
      }
   }

   static DyeColor init_DyeColor(String name, Component display) {
      b.Inner_DataUtil result = init_b_DataUtil(name, display);
      return result == null ? null : result.init_DyeColor();
   }

   private static b.Inner_DataUtil init_b_DataUtil(String name, Component display) {
      if (!at.init(name, display)) {
         return null;
      }

      DyeColor[] color = new DyeColor[]{null};
      boolean[] ambiguous = new boolean[]{false};
      boolean[] plain = new boolean[]{false};
      at.init_MutableComponent(name, display, null).visit((style, text) -> {
         if (!text.isBlank()) {
            if (style.getColor() == null) {
               plain[0] = true;
            } else {
               DyeColor dye = init(style.getColor().getValue());
               if (color[0] != null && color[0] != dye) {
                  ambiguous[0] = true;
               }

               color[0] = dye;
            }
         }

         return Optional.empty();
      }, Style.EMPTY);
      return new b.Inner_DataUtil(!ambiguous[0] && !plain[0] ? color[0] : null, plain[0] && color[0] == null);
   }

   static DyeColor init(int rgb) {
      rgb &= 16777215;

      for (TeamColor format : TeamColor.values()) {
         if (format.textColor().getValue() == rgb) {
            return init(format);
         }
      }

      DyeColor nearest = null;
      long distance = 9223372036854775807L;

      for (DyeColor color : DyeColor.values()) {
         long d = init(rgb, color.getTextureDiffuseColor());
         if (d < distance) {
            nearest = color;
            distance = d;
         }
      }

      for (TeamColor format : TeamColor.values()) {
         long d = init(rgb, format.textColor().getValue());
         if (d < distance) {
            nearest = init(format);
            distance = d;
         }
      }

      return nearest;
   }

   private static long init(int first, int second) {
      int r = (first >> 16 & 0xFF) - (second >> 16 & 0xFF);
      int g = (first >> 8 & 0xFF) - (second >> 8 & 0xFF);
      int b = (first & 0xFF) - (second & 0xFF);
      return r * r + g * g + b * b;
   }

   private static DyeColor init(TeamColor color) {
      DyeColor var10000;
      switch (color) {
         case BLACK:
            var10000 = DyeColor.BLACK;
            break;
         case DARK_BLUE:
         case BLUE:
            var10000 = DyeColor.BLUE;
            break;
         case DARK_GREEN:
            var10000 = DyeColor.GREEN;
            break;
         case GREEN:
            var10000 = DyeColor.LIME;
            break;
         case DARK_AQUA:
         case AQUA:
            var10000 = DyeColor.CYAN;
            break;
         case DARK_RED:
         case RED:
            var10000 = DyeColor.RED;
            break;
         case DARK_PURPLE:
            var10000 = DyeColor.PURPLE;
            break;
         case LIGHT_PURPLE:
            var10000 = DyeColor.PINK;
            break;
         case GOLD:
            var10000 = DyeColor.ORANGE;
            break;
         case YELLOW:
            var10000 = DyeColor.YELLOW;
            break;
         case GRAY:
         case DARK_GRAY:
            var10000 = DyeColor.GRAY;
            break;
         default:
            var10000 = DyeColor.WHITE;
      }

      return var10000;
   }

   private record Inner_DataUtil(DyeColor init_DyeColor, boolean init_Z) {
   }
}

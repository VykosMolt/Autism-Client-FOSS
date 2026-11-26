package autismclient.ai;

import com.mojang.authlib.GameProfile;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.ClickEvent.CopyToClipboard;
import net.minecraft.network.chat.ClickEvent.OpenUrl;
import net.minecraft.network.chat.ClickEvent.RunCommand;
import net.minecraft.network.chat.ClickEvent.SuggestCommand;
import net.minecraft.network.chat.HoverEvent.ShowText;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.world.entity.player.Player;

public final class AiChatLedger {
   public static final int init_I = 64;
   private static final int load = 2048;
   private static final int read = 512;
   private static final int write = 16;
   private static final int update = 256;
   private static final Pattern init_Pattern = Pattern.compile("^<([^<>\\s][^<>]{0,63})>\\s");
   private static final Set<String> init_Set = Set.of(
      "chat", "say", "msg", "team_msg", "emote", "say_command", "msg_command", "team_msg_command", "emote_command", "whisper"
   );
   private static final Object init_Obj = new Object();
   private static final Deque<AiChatLedger.Inner_StreamUtil> init_Deque = new ArrayDeque<>();
   private static long init_J;

   private AiChatLedger() {
   }

   public static void init(Component component, String source) {
      init(component, source, "");
   }

   public static void init(Component component, String source, String sender) {
      if (component != null) {
         String text = init(component.getString(), 2048);
         List<AiChatLedger.Inner_DataUtil> clicks = new ArrayList<>();
         String hover = init(component, clicks);
         if (!text.isBlank() || !clicks.isEmpty()) {
            String candidate = sender == null ? "" : sender.trim();
            if (candidate.isBlank() || init_Set.contains(candidate.toLowerCase(Locale.ROOT))) {
               candidate = init_Str(text);
            }

            String resolvedSource = source == null ? "SERVER" : source;
            String resolvedSender = candidate;
            synchronized (init_Obj) {
               init_Deque.addFirst(new AiChatLedger.Inner_StreamUtil(System.currentTimeMillis(), ++init_J, resolvedSource, resolvedSender, text, clicks, hover));

               while (init_Deque.size() > 64) {
                  init_Deque.removeLast();
               }
            }
         }
      }
   }

   public static void init(Component component, UUID senderId) {
      init(component, senderId, "");
   }

   public static void init(Component component, UUID senderId, String chatType) {
      init(component, init_Z(chatType) ? "PLAYER" : "PLAYER_PRIVATE", init(senderId));
   }

   static boolean init_Z(String chatType) {
      String type = chatType == null ? "" : chatType.toLowerCase(Locale.ROOT);
      return !type.contains("msg") && !type.contains("team") && !type.contains("whisper");
   }

   public static void load(Component component, String chatType) {
      init(component, init_Z(chatType) ? "DISGUISED" : "PLAYER_PRIVATE", "");
   }

   static String init(UUID senderId) {
      if (senderId == null) {
         return "";
      }

      try {
         Minecraft mc = Minecraft.getInstance();
         if (mc.getConnection() != null) {
            PlayerInfo info = mc.getConnection().getPlayerInfo(senderId);
            if (info != null && info.getProfile() != null && info.getProfile().name() != null) {
               return info.getProfile().name();
            }
         }

         if (mc.level != null) {
            Player player = mc.level.getPlayerByUUID(senderId);
            if (player != null) {
               GameProfile profile = player.getGameProfile();
               if (profile != null && profile.name() != null) {
                  return profile.name();
               }

               return player.getName().getString();
            }
         }
      } catch (RuntimeException var4) {
      }

      return "";
   }

   public static List<AiChatLedger.Inner_StreamUtil> init_List(int n) {
      synchronized (init_Obj) {
         List<AiChatLedger.Inner_StreamUtil> out = new ArrayList<>(Math.min(n, init_Deque.size()));

         for (AiChatLedger.Inner_StreamUtil entry : init_Deque) {
            if (out.size() >= n) {
               break;
            }

            out.add(entry);
         }

         return out;
      }
   }

   public static AiChatLedger.Inner_StreamUtil init_TokenStore_StreamU(int index) {
      synchronized (init_Obj) {
         if (index >= 0 && index < init_Deque.size()) {
            int i = 0;

            for (AiChatLedger.Inner_StreamUtil entry : init_Deque) {
               if (i++ == index) {
                  return entry;
               }
            }

            throw new IndexOutOfBoundsException("Chat ledger index " + index + " out of " + init_Deque.size() + " entries");
         } else {
            throw new IndexOutOfBoundsException("Chat ledger index " + index + " out of " + init_Deque.size() + " entries");
         }
      }
   }

   public static int init_I() {
      synchronized (init_Obj) {
         return init_Deque.size();
      }
   }

   public static long init_J() {
      synchronized (init_Obj) {
         return init_J;
      }
   }

   public static void init_V() {
      synchronized (init_Obj) {
         init_Deque.clear();
         init_J++;
      }
   }

   private static String init_Str(String text) {
      Matcher matcher = init_Pattern.matcher(text);
      return matcher.find() ? matcher.group(1) : "";
   }

   private static String init(String text, int max) {
      return text == null ? "" : text.substring(0, Math.min(max, text.length()));
   }

   private static String init(Component root, List<AiChatLedger.Inner_DataUtil> out) {
      Set<AiChatLedger.Inner_DataUtil> seen = new LinkedHashSet<>();
      String hover = "";
      Deque<Component> queue = new ArrayDeque<>();
      Deque<Style> inherited = new ArrayDeque<>();
      queue.add(root);
      inherited.add(Style.EMPTY);
      int nodes = 0;

      while (!queue.isEmpty() && nodes++ < 256 && seen.size() < 16) {
         Component node = queue.poll();
         Style style = node.getStyle().applyTo(inherited.poll());
         AiChatLedger.Inner_DataUtil action = init(style.getClickEvent());
         if (action != null && !action.value().isBlank()) {
            seen.add(action);
         }

         if (hover.isEmpty() && style.getHoverEvent() instanceof ShowText showText) {
            hover = init(showText.value().getString(), 512);
         }

         for (Component sibling : node.getSiblings()) {
            queue.add(sibling);
            inherited.add(style);
         }

         if (node.getContents() instanceof TranslatableContents translatable) {
            for (Object argument : translatable.getArgs()) {
               if (argument instanceof Component argumentComponent) {
                  queue.add(argumentComponent);
                  inherited.add(style);
               }
            }
         }
      }

      out.addAll(seen);
      return hover;
   }

   private static AiChatLedger.Inner_DataUtil init(ClickEvent event) {
      if (event instanceof RunCommand run) {
         return new AiChatLedger.Inner_DataUtil("RUN_COMMAND", run.command());
      } else if (event instanceof SuggestCommand suggest) {
         return new AiChatLedger.Inner_DataUtil("SUGGEST_COMMAND", suggest.command());
      } else if (event instanceof OpenUrl url) {
         return new AiChatLedger.Inner_DataUtil("OPEN_URL", url.uri() == null ? "" : url.uri().toString());
      } else {
         return event instanceof CopyToClipboard copy ? new AiChatLedger.Inner_DataUtil("COPY_TO_CLIPBOARD", copy.value()) : null;
      }
   }

   public record Inner_DataUtil(String action, String value) {
   }

   public record Inner_StreamUtil(long at, long revision, String source, String sender, String text, List<AiChatLedger.Inner_DataUtil> clicks, String hoverText) {
      public Inner_StreamUtil {
         clicks = List.copyOf(clicks);
      }
   }
}

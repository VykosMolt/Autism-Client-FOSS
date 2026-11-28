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
   public static final int CAPACITY = 64;
   private static final int load = 2048;
   private static final int read = 512;
   private static final int write = 16;
   private static final int update = 256;
   private static final Pattern SENDER_PATTERN = Pattern.compile("^<([^<>\\s][^<>]{0,63})>\\s");
   private static final Set<String> IGNORED_SENDERS = Set.of(
      "chat", "say", "msg", "team_msg", "emote", "say_command", "msg_command", "team_msg_command", "emote_command", "whisper"
   );
   private static final Object LOCK = new Object();
   private static final Deque<AiChatLedger.ChatEntry> ledger = new ArrayDeque<>();
   private static long sequence;

   private AiChatLedger() {
   }

   public static void recordSystem(Component component, String source) {
      recordChat(component, source, "");
   }

   public static void recordChat(Component component, String source, String sender) {
      if (component != null) {
         String text = truncate(component.getString(), 2048);
         List<AiChatLedger.ClickAction> clicks = new ArrayList<>();
         String hover = collectClickActions(component, clicks);
         if (!text.isBlank() || !clicks.isEmpty()) {
            String candidate = sender == null ? "" : sender.trim();
            if (candidate.isBlank() || IGNORED_SENDERS.contains(candidate.toLowerCase(Locale.ROOT))) {
               candidate = senderFromText(text);
            }

            String resolvedSource = source == null ? "SERVER" : source;
            String resolvedSender = candidate;
            synchronized (LOCK) {
               ledger.addFirst(new AiChatLedger.ChatEntry(System.currentTimeMillis(), ++sequence, resolvedSource, resolvedSender, text, clicks, hover));

               while (ledger.size() > 64) {
                  ledger.removeLast();
               }
            }
         }
      }
   }

   public static void recordChat(Component component, UUID senderId) {
      recordPlayer(component, senderId, "");
   }

   public static void recordPlayer(Component component, UUID senderId, String chatType) {
      recordChat(component, isPublicChat(chatType) ? "PLAYER" : "PLAYER_PRIVATE", senderName(senderId));
   }

   static boolean isPublicChat(String chatType) {
      String type = chatType == null ? "" : chatType.toLowerCase(Locale.ROOT);
      return !type.contains("msg") && !type.contains("team") && !type.contains("whisper");
   }

   public static void recordDisguised(Component component, String chatType) {
      recordChat(component, isPublicChat(chatType) ? "DISGUISED" : "PLAYER_PRIVATE", "");
   }

   static String senderName(UUID senderId) {
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

   public static List<AiChatLedger.ChatEntry> recent(int n) {
      synchronized (LOCK) {
         List<AiChatLedger.ChatEntry> out = new ArrayList<>(Math.min(n, ledger.size()));

         for (AiChatLedger.ChatEntry entry : ledger) {
            if (out.size() >= n) {
               break;
            }

            out.add(entry);
         }

         return out;
      }
   }

   public static AiChatLedger.ChatEntry entry(int index) {
      synchronized (LOCK) {
         if (index >= 0 && index < ledger.size()) {
            int i = 0;

            for (AiChatLedger.ChatEntry entry : ledger) {
               if (i++ == index) {
                  return entry;
               }
            }

            throw new IndexOutOfBoundsException("Chat ledger index " + index + " out of " + ledger.size() + " entries");
         } else {
            throw new IndexOutOfBoundsException("Chat ledger index " + index + " out of " + ledger.size() + " entries");
         }
      }
   }

   public static int size() {
      synchronized (LOCK) {
         return ledger.size();
      }
   }

   public static long revision() {
      synchronized (LOCK) {
         return sequence;
      }
   }

   public static void clear() {
      synchronized (LOCK) {
         ledger.clear();
         sequence++;
      }
   }

   private static String senderFromText(String text) {
      Matcher matcher = SENDER_PATTERN.matcher(text);
      return matcher.find() ? matcher.group(1) : "";
   }

   private static String truncate(String text, int max) {
      return text == null ? "" : text.substring(0, Math.min(max, text.length()));
   }

   private static String collectClickActions(Component root, List<AiChatLedger.ClickAction> out) {
      Set<AiChatLedger.ClickAction> seen = new LinkedHashSet<>();
      String hover = "";
      Deque<Component> queue = new ArrayDeque<>();
      Deque<Style> inherited = new ArrayDeque<>();
      queue.add(root);
      inherited.add(Style.EMPTY);
      int nodes = 0;

      while (!queue.isEmpty() && nodes++ < 256 && seen.size() < 16) {
         Component node = queue.poll();
         Style style = node.getStyle().applyTo(inherited.poll());
         AiChatLedger.ClickAction action = toClickAction(style.getClickEvent());
         if (action != null && !action.value().isBlank()) {
            seen.add(action);
         }

         if (hover.isEmpty() && style.getHoverEvent() instanceof ShowText showText) {
            hover = truncate(showText.value().getString(), 512);
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

   private static AiChatLedger.ClickAction toClickAction(ClickEvent event) {
      if (event instanceof RunCommand run) {
         return new AiChatLedger.ClickAction("RUN_COMMAND", run.command());
      } else if (event instanceof SuggestCommand suggest) {
         return new AiChatLedger.ClickAction("SUGGEST_COMMAND", suggest.command());
      } else if (event instanceof OpenUrl url) {
         return new AiChatLedger.ClickAction("OPEN_URL", url.uri() == null ? "" : url.uri().toString());
      } else {
         return event instanceof CopyToClipboard copy ? new AiChatLedger.ClickAction("COPY_TO_CLIPBOARD", copy.value()) : null;
      }
   }

   public record ClickAction(String action, String value) {
   }

   public record ChatEntry(long at, long revision, String source, String sender, String text, List<AiChatLedger.ClickAction> clicks, String hoverText) {
      public ChatEntry {
         clicks = List.copyOf(clicks);
      }
   }
}

package autismclient.util;

import autismclient.util.macro.MacroExecutor;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.mojang.brigadier.tree.ArgumentCommandNode;
import com.mojang.brigadier.tree.CommandNode;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.multiplayer.ClientSuggestionProvider;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.arguments.GameProfileArgument;
import net.minecraft.commands.arguments.ScoreHolderArgument;
import net.minecraft.commands.synchronization.SuggestionProviders;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundCommandSuggestionsPacket;
import net.minecraft.network.protocol.game.ServerboundCommandSuggestionPacket;
import net.minecraft.network.protocol.game.ClientboundCommandSuggestionsPacket.Entry;
import net.minecraft.resources.Identifier;
import net.minecraft.world.scores.PlayerTeam;
import net.minecraft.world.scores.ScoreHolder;

public final class CommandTabNameProber {
   private static final String init_Str = "abcdefghijklmnopqrstuvwxyz0123456789_.";
   private static final int init_I = 4;
   private static final int load_I = 1;
   private static final int read_I = 20;
   private static final int write_I = 6;
   private static final int update_I = 2;
   private static final int sync = 4;
   private static final int flush = 28;
   private static final int close = 10;
   private static final long init_J = 2L;
   private static final long load_J = 2500L;
   private static final long read_J = 1500L;
   private static final int open = 2;
   private static final long write_J = 8L;
   private static final long update_J = 500L;
   private static final List<String> MESSAGE_COMMANDS = List.of(
      "msg", "tell", "w", "whisper", "message", "pm", "dm", "emsg", "etell", "tpa", "tpahere", "tpask", "pay", "mail", "friend", "party", "invite"
   );
   private static final Set<String> PLAYER_ARG_NAMES = Set.of(
      "player",
      "players",
      "target",
      "targets",
      "name",
      "username",
      "who",
      "victim",
      "user",
      "users",
      "recipient",
      "nick",
      "gameprofile",
      "profile",
      "targetplayer",
      "playername"
   );
   private static final String[] PROBE_PREFIXES = new String[]{
      "msg ",
      "tell ",
      "w ",
      "whisper ",
      "message ",
      "pm ",
      "pay ",
      "tpa ",
      "tpahere ",
      "tpask ",
      "mail send ",
      "friend add ",
      "party invite ",
      "invite ",
      "minecraft:msg ",
      "minecraft:tell ",
      "minecraft:w "
   };

   private CommandTabNameProber() {
   }

   public static LinkedHashMap<String, String> probe(Minecraft mc, String self) {
      LinkedHashMap<String, String> names = new LinkedHashMap<>();
      if (mc == null) {
         return names;
      }

      ClientPacketListener connection = mc.getConnection();
      if (connection != null) {
         addProfiles(names, safePlayerInfos(connection::getOnlinePlayers), self);
         addProfiles(names, safePlayerInfos(connection::getListedOnlinePlayers), self);

         try {
            addProfiles(names, connection.getSeenPlayers().values(), self);
         } catch (Throwable var8) {
         }
      }

      if (mc.level != null) {
         try {
            for (ScoreHolder holder : mc.level.getScoreboard().getTrackedPlayers()) {
               if (holder != null) {
                  addUsername(names, holder.getScoreboardName(), self);
               }
            }
         } catch (Throwable var10) {
         }

         try {
            for (PlayerTeam team : mc.level.getScoreboard().getPlayerTeams()) {
               if (team != null) {
                  for (String member : team.getPlayers()) {
                     addUsername(names, member, self);
                  }
               }
            }
         } catch (Throwable var9) {
         }
      }

      return names;
   }

   private static Collection<PlayerInfo> safePlayerInfos(Supplier<Collection<PlayerInfo>> supplier) {
      try {
         Collection<PlayerInfo> value = supplier.get();
         return value == null ? List.of() : value;
      } catch (Throwable t) {
         return List.of();
      }
   }

   private static void addProfiles(Map<String, String> names, Collection<PlayerInfo> infos, String self) {
      for (PlayerInfo info : infos) {
         if (info != null && info.getProfile() != null) {
            addRealPlayerName(names, info.getProfile().name(), self);
         }
      }
   }

   public static List<CommandTabNameProber.ProbeVector> discoverVectors(ClientPacketListener connection) {
      Map<String, CommandTabNameProber.ProbeVector> byPrefix = new LinkedHashMap<>();
      if (connection != null) {
         CommandDispatcher<ClientSuggestionProvider> dispatcher = null;

         try {
            dispatcher = connection.getCommands();
         } catch (Throwable var8) {
         }

         if (dispatcher != null && dispatcher.getRoot() != null) {
            try {
               collectVectors(dispatcher.getRoot(), "", byPrefix, 0);
            } catch (Throwable var7) {
            }
         }
      }

      List<CommandTabNameProber.ProbeVector> out = new ArrayList<>(byPrefix.values());
      out.sort(Comparator.comparingInt(CommandTabNameProber.ProbeVector::priority));
      if (out.size() > 6) {
         out = new ArrayList<>(out.subList(0, 6));
      }

      if (out.isEmpty()) {
         for (String prefix : PROBE_PREFIXES) {
            out.add(new CommandTabNameProber.ProbeVector(prefix, 0, prefix.trim()));
         }
      }

      return out;
   }

   private static void collectVectors(CommandNode<ClientSuggestionProvider> node, String path, Map<String, CommandTabNameProber.ProbeVector> byPrefix, int depth) {
      if (node != null && depth <= 6) {
         Collection<CommandNode<ClientSuggestionProvider>> children = node.getChildren();
         if ((children == null || children.isEmpty()) && node.getRedirect() != null) {
            CommandNode<ClientSuggestionProvider> source = node.getRedirect();
            children = source.getChildren();
         }

         if (children != null) {
            for (CommandNode<ClientSuggestionProvider> child : children) {
               if (child != null) {
                  String name = child.getName();
                  if (name != null && !name.isBlank()) {
                     if (child instanceof ArgumentCommandNode<?, ?> arg) {
                        if (!path.isEmpty() && isProbeableArgument(arg)) {
                           String prefix = path + " ";
                           CommandTabNameProber.ProbeVector candidate = new CommandTabNameProber.ProbeVector(prefix, vectorPriority(path, arg), path);
                           byPrefix.merge(prefix, candidate, (a, b) -> (CommandTabNameProber.ProbeVector)(a.priority() <= b.priority() ? a : b));
                        }
                     } else {
                        String childPath = path.isEmpty() ? name : path + " " + name;
                        collectVectors(child, childPath, byPrefix, depth + 1);
                     }
                  }
               }
            }
         }
      }
   }

   private static boolean isProbeableArgument(ArgumentCommandNode<?, ?> arg) {
      Object type = arg.getType();
      return !(type instanceof EntityArgument) && !(type instanceof GameProfileArgument) && !(type instanceof ScoreHolderArgument)
         ? asksServer(arg.getCustomSuggestions())
         : true;
   }

   private static boolean asksServer(SuggestionProvider<?> suggestions) {
      if (suggestions == null) {
         return false;
      }

      try {
         Identifier id = SuggestionProviders.getName(suggestions);
         return id != null && "ask_server".equals(id.getPath());
      } catch (Throwable t) {
         return false;
      }
   }

   private static int vectorPriority(String path, ArgumentCommandNode<?, ?> arg) {
      String first = stripNamespace(path.contains(" ") ? path.substring(0, path.indexOf(32)) : path).toLowerCase(Locale.ROOT);
      int priority = MESSAGE_COMMANDS.indexOf(first);
      if (priority >= 0) {
         return priority;
      }

      String argName = arg.getName() == null ? "" : arg.getName().toLowerCase(Locale.ROOT);
      if (PLAYER_ARG_NAMES.contains(argName)) {
         return 100;
      }

      Object type = arg.getType();
      return !(type instanceof EntityArgument) && !(type instanceof GameProfileArgument) && !(type instanceof ScoreHolderArgument) ? 300 : 200;
   }

   private static String stripNamespace(String literal) {
      int colon = literal.indexOf(58);
      return colon >= 0 ? literal.substring(colon + 1) : literal;
   }

   public static void init(
      ClientPacketListener connection, Map<String, String> names, String self, List<CommandTabNameProber.ProbeVector> vectors, CommandTabNameProber.ProbeControl control
   ) {
      if (connection != null && vectors != null && control != null) {
         int[] queries = new int[]{0};
         long[] gap = new long[]{8L};
         int[] conc = new int[]{10};
         int[] stall = new int[]{0};
         int stallLimit = control.write() ? 2147483647 : 20;
         control.init(names.size(), queries[0], control.maxQueries(), "Probing " + vectors.size() + " commands");
         List<String> baseTexts = new ArrayList<>(vectors.size());

         for (CommandTabNameProber.ProbeVector vector : vectors) {
            baseTexts.add(vector.prefix());
         }

         List<List<String>> bases = requestSuggestions(connection, baseTexts, control, queries, names, self, "Probing commands", 2500L, 0, gap, conc, stall, stallLimit);
         List<CommandTabNameProber.ProbeVector> productive = new ArrayList<>();

         for (int i = 0; i < vectors.size() && i < bases.size(); i++) {
            if (hasNameLike(bases.get(i), control.read())) {
               productive.add(vectors.get(i));
            }
         }

         productive.sort(Comparator.comparingInt(CommandTabNameProber.ProbeVector::priority));
         int maxDepth = control.write() ? 2147483647 : 1;
         int dry = 0;

         for (CommandTabNameProber.ProbeVector vector : productive) {
            if (control.isCancelled() || names.size() >= control.maxNames() || queries[0] >= control.maxQueries() || stall[0] >= stallLimit) {
               break;
            }

            int before = names.size();
            sweepVector(connection, names, self, vector, control, queries, gap, conc, maxDepth, stall, stallLimit);
            if (names.size() == before) {
               if (++dry >= 2) {
                  break;
               }
            } else {
               dry = 0;
            }
         }

         control.init(names.size(), queries[0], control.maxQueries(), "Done");
      }
   }

   public static List<String> probe(ClientPacketListener connection, String query, CommandTabNameProber.ProbeControl control) {
      LinkedHashMap<String, String> names = new LinkedHashMap<>();
      if (connection != null && query != null && !query.isBlank() && control != null) {
         init(connection, names, "", List.of(new CommandTabNameProber.ProbeVector(query, 0, query.trim())), control);
      }

      return new ArrayList<>(names.values());
   }

   private static void sweepVector(
      ClientPacketListener connection,
      Map<String, String> names,
      String self,
      CommandTabNameProber.ProbeVector vector,
      CommandTabNameProber.ProbeControl control,
      int[] queries,
      long[] gap,
      int[] conc,
      int maxDepth,
      int[] stall,
      int stallLimit
   ) {
      int afterBase = names.size();
      List<String> frontier = new ArrayList<>();
      frontier.add("");
      boolean firstLevel = true;
      int depth = 0;

      while (!frontier.isEmpty() && !control.isCancelled() && names.size() < control.maxNames() && queries[0] < control.maxQueries() && stall[0] < stallLimit) {
         depth++;
         int before = names.size();
         List<String> childPrefixes = new ArrayList<>(frontier.size() * "abcdefghijklmnopqrstuvwxyz0123456789_.".length());
         List<String> texts = new ArrayList<>(childPrefixes.size());

         for (String parent : frontier) {
            for (int i = 0; i < "abcdefghijklmnopqrstuvwxyz0123456789_.".length(); i++) {
               String childPrefix = parent + "abcdefghijklmnopqrstuvwxyz0123456789_.".charAt(i);
               childPrefixes.add(childPrefix);
               texts.add(vector.prefix() + childPrefix);
            }
         }

         String status = "Sweeping " + vector.load() + " — " + formatCount(names.size()) + " names (depth " + depth + ")";
         List<List<String>> replies = requestSuggestions(connection, texts, control, queries, names, self, status, 1500L, 2, gap, conc, stall, stallLimit);
         int levelCap = 0;

         for (List<String> reply : replies) {
            levelCap = Math.max(levelCap, reply.size());
         }

         if (firstLevel && names.size() == afterBase) {
            return;
         }

         firstLevel = false;
         if (depth >= maxDepth) {
            return;
         }

         List<String> next = new ArrayList<>();
         if (names.size() > before && levelCap >= 4) {
            for (int i = 0; i < replies.size(); i++) {
               if (replies.get(i).size() >= levelCap) {
                  next.add(childPrefixes.get(i));
               }
            }
         }

         frontier = next;
      }
   }

   private static String formatCount(int n) {
      return String.format(Locale.US, "%,d", n);
   }

   private static List<List<String>> requestSuggestions(
      ClientPacketListener connection,
      List<String> texts,
      CommandTabNameProber.ProbeControl control,
      int[] queries,
      Map<String, String> names,
      String self,
      String status,
      long timeoutMs,
      int retries,
      long[] gap,
      int[] conc,
      int[] stall,
      int stallLimit
   ) {
      int n = texts.size();
      List<List<String>> results = new ArrayList<>(n);

      for (int i = 0; i < n; i++) {
         results.add(null);
      }

      List<Integer> pending = new ArrayList<>(n);

      for (int i = 0; i < n; i++) {
         pending.add(i);
      }

      for (int round = 0; round <= retries && !pending.isEmpty(); round++) {
         boolean lastRound = round == retries;
         List<Integer> retry = new ArrayList<>();
         int start = 0;

         label153:
         while (true) {
            if (start < pending.size()) {
               if (!control.isCancelled() && names.size() < control.maxNames() && queries[0] < control.maxQueries() && stall[0] < stallLimit) {
                  waitWhilePaused(control);
                  int batch = Math.max(4, conc[0]);
                  int end = Math.min(pending.size(), start + batch);
                  List<MacroExecutor.PacketWaiter> listeners = new ArrayList<>(end - start);

                  for (int k = start; k < end; k++) {
                     int idx = pending.get(k);
                     int requestId = CommandProbeIdAllocator.next();
                     MacroExecutor.PacketWaiter listener = MacroExecutor.registerIncomingPacketWaiter(
                        packet -> packet instanceof ClientboundCommandSuggestionsPacket replyx && replyx.id() == requestId
                     );
                     listeners.add(listener);

                     try {
                        connection.send(new ServerboundCommandSuggestionPacket(requestId, texts.get(idx)));
                     } catch (Throwable t) {
                        listener.future.complete(null);
                     }

                     sleepQuietly(2L);
                  }

                  long deadline = System.currentTimeMillis() + timeoutMs;
                  boolean[] done = new boolean[end - start];
                  int remaining = end - start;
                  int timeouts = 0;

                  while (true) {
                     if (remaining > 0) {
                        if (!control.isCancelled() && stall[0] < stallLimit) {
                           boolean progressed = false;

                           for (int k = start; k < end; k++) {
                              int li = k - start;
                              if (!done[li] && listeners.get(li).future.isDone()) {
                                 done[li] = true;
                                 remaining--;
                                 queries[0]++;
                                 List<String> reply = readSuggestions(listeners.get(li));
                                 results.set(pending.get(k), reply);
                                 int prev = names.size();
                                 addSuggestions(names, reply, self, control.maxNames(), control.read());
                                 stall[0] = names.size() > prev ? 0 : stall[0] + 1;
                                 control.init(names.size(), queries[0], control.maxQueries(), status);
                                 progressed = true;
                              }
                           }

                           if (remaining != 0) {
                              if (System.currentTimeMillis() < deadline) {
                                 if (!progressed) {
                                    sleepQuietly(5L);
                                 }
                                 continue;
                              }

                              for (int k = start; k < end; k++) {
                                 int li = k - start;
                                 if (!done[li]) {
                                    queries[0]++;
                                    timeouts++;
                                    stall[0]++;
                                    if (lastRound) {
                                       results.set(pending.get(k), List.of());
                                    } else {
                                       retry.add(pending.get(k));
                                    }

                                    listeners.get(li).cancel();
                                 }
                              }

                              control.init(names.size(), queries[0], control.maxQueries(), status);
                           }
                        } else {
                           for (int li = 0; li < listeners.size(); li++) {
                              if (!done[li]) {
                                 listeners.get(li).cancel();
                              }
                           }
                        }
                     }

                     if (timeouts > 0) {
                        conc[0] = Math.max(4, conc[0] / 2);
                        gap[0] = Math.min(500L, Math.max(8L, gap[0] * 2L + 20L));
                     } else {
                        conc[0] = Math.min(28, conc[0] + 1);
                        gap[0] = Math.max(8L, gap[0] - 10L);
                     }

                     if (!control.isCancelled()) {
                        sleepQuietly(gap[0]);
                     }

                     start = end;
                     continue label153;
                  }
               }

               pending = List.of();
               retry = new ArrayList<>();
            }

            pending = retry;
            break;
         }
      }

      for (int i = 0; i < n; i++) {
         if (results.get(i) == null) {
            results.set(i, List.of());
         }
      }

      return results;
   }

   private static List<String> readSuggestions(MacroExecutor.PacketWaiter listener) {
      try {
         Packet<?> reply = listener.future.getNow(null);
         List<String> out = new ArrayList<>();
         if (reply instanceof ClientboundCommandSuggestionsPacket suggestions && suggestions.suggestions() != null) {
            for (Entry entry : suggestions.suggestions()) {
               if (entry != null && entry.text() != null && !entry.text().isBlank()) {
                  out.add(entry.text().trim());
               }
            }
         }

         return out;
      } catch (Throwable t) {
         return List.of();
      } finally {
         listener.cancel();
      }
   }

   private static int addSuggestions(Map<String, String> names, List<String> suggestions, String self, int cap, boolean usernamesOnly) {
      int added = 0;

      for (String suggestion : suggestions) {
         if (names.size() >= cap) {
            break;
         }

         boolean ok = usernamesOnly ? addUsername(names, suggestion, self) : addName(names, suggestion, self);
         if (ok) {
            added++;
         }
      }

      return added;
   }

   private static boolean hasNameLike(List<String> suggestions, boolean usernamesOnly) {
      for (String suggestion : suggestions) {
         if (suggestion != null) {
            String trimmed = suggestion.trim();
            if (!trimmed.isEmpty() && (!usernamesOnly || TabPlayerListParser.isRealPlayerName(trimmed))) {
               return true;
            }
         }
      }

      return false;
   }

   private static boolean addName(Map<String, String> names, String name, String self) {
      if (name == null) {
         return false;
      }

      String trimmed = name.trim();
      return !trimmed.isEmpty() && (self == null || self.isEmpty() || !trimmed.equalsIgnoreCase(self))
         ? names.putIfAbsent(trimmed.toLowerCase(Locale.ROOT), trimmed) == null
         : false;
   }

   private static void addRealPlayerName(Map<String, String> names, String name, String self) {
      if (name != null) {
         String trimmed = name.trim();
         if (!trimmed.equalsIgnoreCase(self) && TabPlayerListParser.isRealPlayerName(trimmed)) {
            names.putIfAbsent(trimmed.toLowerCase(Locale.ROOT), trimmed);
         }
      }
   }

   private static boolean addUsername(Map<String, String> names, String name, String self) {
      if (name == null) {
         return false;
      }

      String trimmed = name.trim();
      return !trimmed.equalsIgnoreCase(self) && TabPlayerListParser.isRealPlayerName(trimmed) ? names.putIfAbsent(trimmed.toLowerCase(Locale.ROOT), trimmed) == null : false;
   }

   private static void waitWhilePaused(CommandTabNameProber.ProbeControl control) {
      while (control.isPaused() && !control.isCancelled()) {
         sleepQuietly(120L);
      }
   }

   private static void sleepQuietly(long ms) {
      try {
         Thread.sleep(ms);
      } catch (InterruptedException e) {
         Thread.currentThread().interrupt();
      }
   }

   public interface ProbeControl {
      boolean isCancelled();

      boolean isPaused();

      int maxNames();

      int maxQueries();

      default boolean read() {
         return true;
      }

      default boolean write() {
         return false;
      }

      default void init(int names, int queries, int maxQueries, String vector) {
      }
   }

   public record ProbeVector(String prefix, int priority, String load) {
   }
}

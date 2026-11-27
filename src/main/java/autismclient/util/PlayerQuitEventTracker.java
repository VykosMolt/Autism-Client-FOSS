package autismclient.util;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.contents.TranslatableContents;

public final class PlayerQuitEventTracker {
   public static final long init_J = 3000L;
   public static final long load = 20000L;
   public static final long read = 3000L;
   private static final long write = 500L;
   private static final List<String> init_List = List.of(
      " has left the game", " has left the server", " left the game", " left the server", " has quit", " quit", " disconnected", " logged out"
   );
   private final Map<UUID, PlayerQuitEventTracker.Inner_StreamUtil> init_Map = new HashMap<>();
   private final ArrayDeque<PlayerQuitEventTracker.Inner_CachePool> init_ArrayDeque = new ArrayDeque<>();

   public void updateRoster(Collection<AntiVanishRosterTracker.Inner_ConfigStore> profiles, long now) {
      if (profiles.size() > 4096) {
         this.init();
      } else {
         Map<UUID, AntiVanishRosterTracker.Inner_ConfigStore> live = new HashMap<>();
         Set<String> names = new HashSet<>();
         Set<String> duplicates = new HashSet<>();
         Set<UUID> duplicateIds = new HashSet<>();

         for (AntiVanishRosterTracker.Inner_ConfigStore p : profiles) {
            if (p.uuid() != null && p.name() != null) {
               if (live.putIfAbsent(p.uuid(), p) != null) {
                  duplicateIds.add(p.uuid());
               }

               if (!names.add(init_Str_Str(p.name()))) {
                  duplicates.add(init_Str_Str(p.name()));
               }
            }
         }

         for (AntiVanishRosterTracker.Inner_ConfigStore p : live.values()) {
            if (p.load()
               && p.name().matches("[A-Za-z0-9_]{1,16}")
               && !duplicates.contains(init_Str_Str(p.name()))
               && !duplicateIds.contains(p.uuid())) {
               PlayerQuitEventTracker.Inner_StreamUtil m = this.init_Map.get(p.uuid());
               if (m != null && !m.init_Str.equals(p.name())) {
                  this.init_Map.remove(p.uuid());
                  m = null;
               }

               if (m == null) {
                  if (!p.listed()) {
                     continue;
                  }

                  m = new PlayerQuitEventTracker.Inner_StreamUtil(p.uuid(), p.name());
                  this.init_Map.put(p.uuid(), m);
               }

               if (p.listed()) {
                  m.init(now);
               } else {
                  this.init_V_StreamUtil_StreamU_J(m, now);
               }
            } else {
               this.init_Map.remove(p.uuid());
            }
         }

         Map<String, UUID> owners = new HashMap<>();

         for (AntiVanishRosterTracker.Inner_ConfigStore p : live.values()) {
            owners.put(init_Str_Str(p.name()), p.uuid());
         }

         this.init_Map.values().removeIf(mx -> !live.containsKey(mx.init_UUID) && owners.containsKey(init_Str_Str(mx.init_Str)));

         for (PlayerQuitEventTracker.Inner_StreamUtil m : this.init_Map.values()) {
            if (!live.containsKey(m.init_UUID)) {
               this.init_V_StreamUtil_StreamU_J(m, now);
            }
         }

         while (!this.init_ArrayDeque.isEmpty() && now - this.init_ArrayDeque.peekFirst().init_J > 500L) {
            this.init_ArrayDeque.removeFirst();
         }

         if (this.init_ArrayDeque.stream().map(PlayerQuitEventTracker.Inner_CachePool::init_UUID).distinct().count() >= 4L) {
            for (PlayerQuitEventTracker.Inner_CachePool r : this.init_ArrayDeque) {
               PlayerQuitEventTracker.Inner_StreamUtil m = this.init_Map.get(r.init_UUID);
               if (m != null && m.read_J == r.init_J) {
                  m.write_Z = true;
               }
            }
         }

         this.init_Map.values().removeIf(mx -> now - mx.load_J > 30000L);
         if (this.init_Map.size() > 4096) {
            this.init();
         }
      }
   }

   private void init_V_StreamUtil_StreamU_J(PlayerQuitEventTracker.Inner_StreamUtil m, long now) {
      if (m.init_Z) {
         m.load_Z = now - m.init_J >= 3000L;
         m.init_Z = false;
         m.read_J = now;
         m.read_Z = false;
         m.write_Z = false;
         if (m.load_Z) {
            this.init_ArrayDeque.addLast(new PlayerQuitEventTracker.Inner_CachePool(m.init_UUID, now));
         }
      }
   }

   public void onChat(Component message, long now) {
      if (message != null) {
         ArrayDeque<Component> work = new ArrayDeque<>();
         work.add(message);
         int visited = 0;
         boolean canonical = false;

         while (!work.isEmpty()) {
            if (++visited > 64) {
               break;
            }

            Component part = work.removeFirst();
            if (part.getContents() instanceof TranslatableContents translated && translated.getKey().equals("multiplayer.player.left")) {
               canonical = true;
               if (translated.getArgs().length == 1) {
                  Object argument = translated.getArgs()[0];
                  String display = argument instanceof Component c ? c.getString(96) : (argument instanceof String s ? s : "");
                  if (display.length() <= 96) {
                     List<PlayerQuitEventTracker.Inner_StreamUtil> matches = this.init_Map.values().stream().filter(mx -> NormalizedTextMatcher.init_Z(display, mx.init_Str)).toList();
                     if (matches.size() == 1) {
                        matches.getFirst().write_J = now;
                     }
                  }
               }
            }

            if (work.size() + part.getSiblings().size() > 64) {
               return;
            }

            work.addAll(part.getSiblings());
         }

         if (!canonical) {
            String raw = message.getString(257);
            if (raw.length() <= 256) {
               String line = NormalizedTextMatcher.init_Str(raw);

               for (String ending : init_List) {
                  if (line.endsWith(ending)) {
                     String name = line.substring(0, line.length() - ending.length());

                     for (PlayerQuitEventTracker.Inner_StreamUtil m : this.init_Map.values()) {
                        if (m.load_Str.equals(name)) {
                           m.write_J = now;
                        }
                     }
                  }
               }
            }
         }
      }
   }

   public List<PlayerQuitEventTracker.Inner_DataUtil> collectAlerts(long now) {
      List<PlayerQuitEventTracker.Inner_DataUtil> alerts = new ArrayList<>();

      for (PlayerQuitEventTracker.Inner_StreamUtil m : this.init_Map.values()) {
         if (init_Z_StreamUtil_StreamU_J(m, now) && !m.read_Z) {
            m.read_Z = true;
            alerts.add(new PlayerQuitEventTracker.Inner_DataUtil(m.init_UUID, m.init_Str, m.read_J + 3000L + 20000L));
         }
      }

      return List.copyOf(alerts);
   }

   public boolean isSuspect(UUID id, long now) {
      PlayerQuitEventTracker.Inner_StreamUtil m = this.init_Map.get(id);
      return m != null && init_Z_StreamUtil_StreamU_J(m, now);
   }

   private static boolean init_Z_StreamUtil_StreamU_J(PlayerQuitEventTracker.Inner_StreamUtil m, long now) {
      return m.load_Z
         && !m.init_Z
         && !m.write_Z
         && m.read_J >= 0L
         && now - m.read_J >= 3000L
         && now - m.read_J < 23000L
         && now - m.load_J <= 30000L
         && (m.write_J < 0L || m.write_J < m.read_J - 3000L);
   }

   public void init() {
      this.init_Map.clear();
      this.init_ArrayDeque.clear();
   }

   private static String init_Str_Str(String text) {
      return text.toLowerCase(Locale.ROOT);
   }

   private record Inner_CachePool(UUID init_UUID, long init_J) {
   }

   public record Inner_DataUtil(UUID uuid, String name, long expiresAt) {
   }

   private static final class Inner_StreamUtil {
      final UUID init_UUID;
      final String init_Str;
      final String load_Str;
      long init_J = -1L;
      long load_J;
      long read_J = -1L;
      long write_J = -1L;
      boolean init_Z;
      boolean load_Z;
      boolean read_Z;
      boolean write_Z;

      Inner_StreamUtil(UUID id, String name) {
         this.init_UUID = id;
         this.init_Str = name;
         this.load_Str = NormalizedTextMatcher.init_Str(name);
      }

      void init(long now) {
         if (!this.init_Z) {
            this.init_J = now;
            this.read_J = -1L;
            this.write_J = -1L;
            this.load_Z = this.read_Z = this.write_Z = false;
         }

         this.init_Z = true;
         this.load_J = now;
      }
   }
}

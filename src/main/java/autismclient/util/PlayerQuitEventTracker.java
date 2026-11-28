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
   private static final List<String> QUIT_MESSAGE_ENDINGS = List.of(
      " has left the game", " has left the server", " left the game", " left the server", " has quit", " quit", " disconnected", " logged out"
   );
   private final Map<UUID, PlayerQuitEventTracker.TrackedMember> members = new HashMap<>();
   private final ArrayDeque<PlayerQuitEventTracker.Removal> recentRemovals = new ArrayDeque<>();

   public void updateRoster(Collection<AntiVanishRosterTracker.RosterProfile> profiles, long now) {
      if (profiles.size() > 4096) {
         this.clear();
      } else {
         Map<UUID, AntiVanishRosterTracker.RosterProfile> live = new HashMap<>();
         Set<String> names = new HashSet<>();
         Set<String> duplicates = new HashSet<>();
         Set<UUID> duplicateIds = new HashSet<>();

         for (AntiVanishRosterTracker.RosterProfile p : profiles) {
            if (p.uuid() != null && p.name() != null) {
               if (live.putIfAbsent(p.uuid(), p) != null) {
                  duplicateIds.add(p.uuid());
               }

               if (!names.add(lower(p.name()))) {
                  duplicates.add(lower(p.name()));
               }
            }
         }

         for (AntiVanishRosterTracker.RosterProfile p : live.values()) {
            if (p.tracked()
               && p.name().matches("[A-Za-z0-9_]{1,16}")
               && !duplicates.contains(lower(p.name()))
               && !duplicateIds.contains(p.uuid())) {
               PlayerQuitEventTracker.TrackedMember m = this.members.get(p.uuid());
               if (m != null && !m.name.equals(p.name())) {
                  this.members.remove(p.uuid());
                  m = null;
               }

               if (m == null) {
                  if (!p.listed()) {
                     continue;
                  }

                  m = new PlayerQuitEventTracker.TrackedMember(p.uuid(), p.name());
                  this.members.put(p.uuid(), m);
               }

               if (p.listed()) {
                  m.markListed(now);
               } else {
                  this.onRemoved(m, now);
               }
            } else {
               this.members.remove(p.uuid());
            }
         }

         Map<String, UUID> owners = new HashMap<>();

         for (AntiVanishRosterTracker.RosterProfile p : live.values()) {
            owners.put(lower(p.name()), p.uuid());
         }

         this.members.values().removeIf(mx -> !live.containsKey(mx.id) && owners.containsKey(lower(mx.name)));

         for (PlayerQuitEventTracker.TrackedMember m : this.members.values()) {
            if (!live.containsKey(m.id)) {
               this.onRemoved(m, now);
            }
         }

         while (!this.recentRemovals.isEmpty() && now - this.recentRemovals.peekFirst().at > 500L) {
            this.recentRemovals.removeFirst();
         }

         if (this.recentRemovals.stream().map(PlayerQuitEventTracker.Removal::uuid).distinct().count() >= 4L) {
            for (PlayerQuitEventTracker.Removal r : this.recentRemovals) {
               PlayerQuitEventTracker.TrackedMember m = this.members.get(r.uuid);
               if (m != null && m.removedAt == r.at) {
                  m.massRemoval = true;
               }
            }
         }

         this.members.values().removeIf(mx -> now - mx.lastSeenAt > 30000L);
         if (this.members.size() > 4096) {
            this.clear();
         }
      }
   }

   private void onRemoved(PlayerQuitEventTracker.TrackedMember m, long now) {
      if (m.listed) {
         m.wasEstablished = now - m.listedSince >= 3000L;
         m.listed = false;
         m.removedAt = now;
         m.alerted = false;
         m.massRemoval = false;
         if (m.wasEstablished) {
            this.recentRemovals.addLast(new PlayerQuitEventTracker.Removal(m.id, now));
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
                     List<PlayerQuitEventTracker.TrackedMember> matches = this.members.values().stream().filter(mx -> NormalizedTextMatcher.matchesName(display, mx.name)).toList();
                     if (matches.size() == 1) {
                        matches.getFirst().quitMessageAt = now;
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
               String line = NormalizedTextMatcher.normalize(raw);

               for (String ending : QUIT_MESSAGE_ENDINGS) {
                  if (line.endsWith(ending)) {
                     String name = line.substring(0, line.length() - ending.length());

                     for (PlayerQuitEventTracker.TrackedMember m : this.members.values()) {
                        if (m.normalizedName.equals(name)) {
                           m.quitMessageAt = now;
                        }
                     }
                  }
               }
            }
         }
      }
   }

   public List<PlayerQuitEventTracker.QuitAlert> collectAlerts(long now) {
      List<PlayerQuitEventTracker.QuitAlert> alerts = new ArrayList<>();

      for (PlayerQuitEventTracker.TrackedMember m : this.members.values()) {
         if (isSuspectEntry(m, now) && !m.alerted) {
            m.alerted = true;
            alerts.add(new PlayerQuitEventTracker.QuitAlert(m.id, m.name, m.removedAt + 3000L + 20000L));
         }
      }

      return List.copyOf(alerts);
   }

   public boolean isSuspect(UUID id, long now) {
      PlayerQuitEventTracker.TrackedMember m = this.members.get(id);
      return m != null && isSuspectEntry(m, now);
   }

   private static boolean isSuspectEntry(PlayerQuitEventTracker.TrackedMember m, long now) {
      return m.wasEstablished
         && !m.listed
         && !m.massRemoval
         && m.removedAt >= 0L
         && now - m.removedAt >= 3000L
         && now - m.removedAt < 23000L
         && now - m.lastSeenAt <= 30000L
         && (m.quitMessageAt < 0L || m.quitMessageAt < m.removedAt - 3000L);
   }

   public void clear() {
      this.members.clear();
      this.recentRemovals.clear();
   }

   private static String lower(String text) {
      return text.toLowerCase(Locale.ROOT);
   }

   private record Removal(UUID uuid, long at) {
   }

   public record QuitAlert(UUID uuid, String name, long expiresAt) {
   }

   private static final class TrackedMember {
      final UUID id;
      final String name;
      final String normalizedName;
      long listedSince = -1L;
      long lastSeenAt;
      long removedAt = -1L;
      long quitMessageAt = -1L;
      boolean listed;
      boolean wasEstablished;
      boolean alerted;
      boolean massRemoval;

      TrackedMember(UUID id, String name) {
         this.id = id;
         this.name = name;
         this.normalizedName = NormalizedTextMatcher.normalize(name);
      }

      void markListed(long now) {
         if (!this.listed) {
            this.listedSince = now;
            this.removedAt = -1L;
            this.quitMessageAt = -1L;
            this.wasEstablished = this.alerted = this.massRemoval = false;
         }

         this.listed = true;
         this.lastSeenAt = now;
      }
   }
}

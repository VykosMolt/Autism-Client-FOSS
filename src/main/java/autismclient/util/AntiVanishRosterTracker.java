package autismclient.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.Map.Entry;

public final class AntiVanishRosterTracker {
   public static final long init_J = 3000L;
   public static final long load_J = 2000L;
   public static final long read_J = 4000L;
   public static final long write_J = 12000L;
   public static final long update_J = 30000L;
   public static final long sync_J = 20000L;
   public static final long flush = 1500L;
   public static final long close = 75000L;
   public static final long open = 5000L;
   private static final long poll = 250L;
   private final Map<UUID, AntiVanishRosterTracker.RosterMember> members = new HashMap<>();
   private final Map<UUID, AntiVanishRosterTracker.PendingRequest> pendingRequests = new HashMap<>();
   private final Map<UUID, AntiVanishRosterTracker.LastSeen> lastSeen = new HashMap<>();
   private final Map<UUID, Long> lastRequestAt = new HashMap<>();
   private Map<UUID, AntiVanishRosterTracker.RosterProfile> liveProfiles = Map.of();
   private Map<String, UUID> uuidByName = Map.of();

   public void updateRoster(Collection<AntiVanishRosterTracker.RosterProfile> profiles, long now) {
      if (profiles.size() > 4096) {
         this.reset();
      } else {
         Map<UUID, AntiVanishRosterTracker.RosterProfile> live = new HashMap<>();
         Set<String> names = new HashSet<>();
         Set<String> duplicates = new HashSet<>();

         for (AntiVanishRosterTracker.RosterProfile p : profiles) {
            if (p.uuid != null && p.name != null) {
               live.put(p.uuid, p);
               if (!names.add(normalizeName(p.name))) {
                  duplicates.add(normalizeName(p.name));
               }
            }
         }

         this.liveProfiles = Map.copyOf(live);
         Map<String, UUID> byName = new HashMap<>();

         for (AntiVanishRosterTracker.RosterProfile p : live.values()) {
            byName.put(normalizeName(p.name), p.uuid);
         }

         this.uuidByName = Map.copyOf(byName);
         this.lastSeen.keySet().removeIf(live::containsKey);
         this.pendingRequests
            .entrySet()
            .removeIf(
               entry -> {
                  AntiVanishRosterTracker.RosterProfile px = live.get(entry.getKey());
                  String name = normalizeName(entry.getValue().name);
                  UUID client = byName.get(name);
                  return duplicates.contains(name)
                     || client != null && !client.equals(entry.getKey())
                     || px != null && (px.listed || !px.tracked || !px.name.equals(entry.getValue().name));
               }
            );
         int departures = 0;

         for (AntiVanishRosterTracker.RosterMember m : this.members.values()) {
            AntiVanishRosterTracker.RosterProfile p = live.get(m.id);
            if (m.present && (p == null || !p.listed)) {
               departures++;
            }
         }

         for (AntiVanishRosterTracker.RosterProfile p : live.values()) {
            if (p.tracked && !duplicates.contains(normalizeName(p.name)) && p.name.matches("[A-Za-z0-9_]{1,16}")) {
               AntiVanishRosterTracker.RosterMember m = this.members.get(p.uuid);
               if (m == null || !m.name.equals(p.name)) {
                  m = new AntiVanishRosterTracker.RosterMember(p.uuid, p.name);
                  this.members.put(p.uuid, m);
               }

               m.listed = true;
               m.lastSeenAt = now;
               if (p.listed) {
                  m.markPresent(now);
               } else {
                  m.markAbsent(now, departures >= 4);
               }
            } else {
               this.members.remove(p.uuid);
            }
         }

         for (AntiVanishRosterTracker.RosterMember m : this.members.values()) {
            if (!live.containsKey(m.id)) {
               m.listed = false;
               m.markAbsent(now, departures >= 4);
            }
         }

         this.members.values().removeIf(mx -> now - mx.lastSeenAt > 30000L);
         if (this.members.size() > 4096) {
            this.reset();
         }
      }
   }

   public void onProfileReply(Collection<AntiVanishRosterTracker.RosterProfile> profiles, long requestedAt, long now) {
      if (requestedAt <= now && now - requestedAt <= 4000L && profiles.size() <= 4096) {
         Set<UUID> ids = new HashSet<>();
         Set<String> names = new HashSet<>();

         for (AntiVanishRosterTracker.RosterProfile p : profiles) {
            if (p.uuid == null || p.name == null || !ids.add(p.uuid) || !names.add(normalizeName(p.name))) {
               return;
            }
         }

         Set<UUID> seen = new HashSet<>();

         for (AntiVanishRosterTracker.RosterProfile p : profiles) {
            if (p.uuid != null && p.name != null && p.tracked && !p.listed && p.name.matches("[A-Za-z0-9_]{1,16}")) {
               AntiVanishRosterTracker.RosterProfile client = this.liveProfiles.get(p.uuid);
               if (client == null || !client.listed && client.tracked && client.name.equals(p.name)) {
                  UUID other = this.uuidByName.get(normalizeName(p.name));
                  if (other == null || other.equals(p.uuid)) {
                     if (!seen.add(p.uuid)) {
                        this.pendingRequests.remove(p.uuid);
                     } else {
                        AntiVanishRosterTracker.PendingRequest fact = this.pendingRequests.get(p.uuid);
                        if (fact == null || !fact.name.equals(p.name) || now - fact.observedAt > 20000L) {
                           this.pendingRequests.put(p.uuid, new AntiVanishRosterTracker.PendingRequest(p.name, requestedAt, now, 1));
                        } else if (requestedAt - fact.requestedAt >= 1500L) {
                           this.pendingRequests.put(p.uuid, new AntiVanishRosterTracker.PendingRequest(p.name, requestedAt, now, Math.min(2, fact.init_I + 1)));
                        }
                     }
                  }
               }
            }
         }

         this.pendingRequests.keySet().retainAll(seen);
      }
   }

   public void observeUnknownIds(Set<UUID> unknown, long now) {
      if (unknown.size() <= 4096) {
         Set<UUID> accepted = new HashSet<>();

         for (UUID id : unknown) {
            if (id != null && id.variant() == 2 && id.version() >= 3 && id.version() <= 5 && !this.liveProfiles.containsKey(id)) {
               accepted.add(id);
               AntiVanishRosterTracker.LastSeen before = this.lastSeen.get(id);
               if (before == null || now - before.seenAt > 75000L) {
                  this.lastSeen.put(id, new AntiVanishRosterTracker.LastSeen(now, 1));
               } else if (now - before.seenAt >= 5000L) {
                  this.lastSeen.put(id, new AntiVanishRosterTracker.LastSeen(now, Math.min(2, before.init_I + 1)));
               }
            }
         }

         this.lastSeen.keySet().retainAll(accepted);
      }
   }

   public void observeAction(AntiVanishRosterTracker.ActorInfo actor, String action, long now) {
      if (actor != null && actor.init_Z && !actor.npc && action != null && !action.isBlank()) {
         AntiVanishRosterTracker.RosterMember m = this.members.get(actor.id);
         if (m != null && m.name.equals(actor.name) && m.isRecentlyGone(now)) {
            m.expireStale(now);
            if (m.lastActionAt < 0L || now - m.lastActionAt >= 250L) {
               m.lastActionAt = now;
               if (m.actionStreakStart < 0L) {
                  m.actionStreakStart = now;
               }

               m.actionHits = Math.min(2, m.actionHits + 1);
            }
         }
      }
   }

   public void onCompletionReply(Collection<String> names, long requestedAt, long now) {
      if (now >= requestedAt && now - requestedAt <= 4000L) {
         Set<String> offered = new HashSet<>();

         for (String name : names) {
            if (name != null) {
               offered.add(normalizeName(name));
            }
         }

         for (AntiVanishRosterTracker.RosterMember m : this.members.values()) {
            if (m.isRecentlyGone(now) && requestedAt >= m.leftAt + 2000L) {
               if (!offered.contains(normalizeName(m.name))) {
                  m.completionHits = 0;
                  m.lastCompletionRequestAt = -1L;
               } else {
                  m.expireStale(now);
                  if (m.lastCompletionRequestAt < 0L || requestedAt - m.lastCompletionRequestAt >= 1000L) {
                     m.lastCompletionRequestAt = requestedAt;
                     m.completionHits = Math.min(2, m.completionHits + 1);
                  }
               }
            }
         }
      }
   }

   public List<AntiVanishRosterTracker.VanishAlert> collectAlerts(long now) {
      List<AntiVanishRosterTracker.VanishAlert> out = new ArrayList<>();
      this.pendingRequests.entrySet().removeIf(entry -> now - entry.getValue().observedAt > 20000L);

      for (Entry<UUID, AntiVanishRosterTracker.PendingRequest> entry : this.pendingRequests.entrySet()) {
         AntiVanishRosterTracker.PendingRequest fact = entry.getValue();
         if (fact.init_I >= 2 && this.tryRequest(entry.getKey(), now)) {
            out.add(new AntiVanishRosterTracker.VanishAlert(entry.getKey(), fact.name, "online outside TAB in two server roster checks"));
         }
      }

      this.lastSeen.entrySet().removeIf(entry -> now - entry.getValue().seenAt > 75000L);

      for (Entry<UUID, AntiVanishRosterTracker.LastSeen> entry : this.lastSeen.entrySet()) {
         if (entry.getValue().init_I >= 2 && this.tryRequest(entry.getKey(), now)) {
            UUID id = entry.getKey();
            out.add(
               new AntiVanishRosterTracker.VanishAlert(id, "UUID:" + id.toString().substring(0, 8), "unlisted UUID in two complete player-roster updates: " + id, true)
            );
         }
      }

      for (AntiVanishRosterTracker.RosterMember m : this.members.values()) {
         m.expireStale(now);
         if (m.isLikelyVanished(now) && this.tryRequest(m.id, now)) {
            out.add(
               new AntiVanishRosterTracker.VanishAlert(
                  m.id, m.name, m.actionHits >= 2 ? "repeated player actions outside TAB" : "player action + repeated online completion"
               )
            );
         }
      }

      return List.copyOf(out);
   }

   public boolean isSuspectedVanished(UUID id, long now) {
      AntiVanishRosterTracker.PendingRequest fact = this.pendingRequests.get(id);
      if (fact != null && fact.init_I >= 2 && now - fact.observedAt <= 20000L) {
         return true;
      }

      AntiVanishRosterTracker.LastSeen pulse = this.lastSeen.get(id);
      if (pulse != null && pulse.init_I >= 2 && now - pulse.seenAt <= 75000L) {
         return true;
      }

      AntiVanishRosterTracker.RosterMember m = this.members.get(id);
      if (m != null) {
         m.expireStale(now);
      }

      return m != null && m.isLikelyVanished(now);
   }

   public boolean hasRecentHiddenAction(long now) {
      return this.members.values().stream().anyMatch(m -> m.isRecentlyGone(now) && m.lastActionAt >= 0L && now - m.lastActionAt <= 4000L);
   }

   public boolean needsRecheck() {
      return this.pendingRequests.values().stream().anyMatch(fact -> fact.init_I < 2);
   }

   public void onSystemMessage(String systemMessage, long now) {
      for (AntiVanishRosterTracker.RosterMember m : this.members.values()) {
         if (NormalizedTextMatcher.matchesLeave(systemMessage, m.name)) {
            m.resetEvidence();
            if (!m.present) {
               m.leftAt = now;
            }

            this.lastRequestAt.remove(m.id);
         }
      }

      this.pendingRequests.values().removeIf(p -> NormalizedTextMatcher.matchesLeave(systemMessage, p.name));
   }

   public int trackedCount() {
      return this.members.size() + this.pendingRequests.size() + this.lastSeen.size();
   }

   public void resetProbes() {
      this.members.values().forEach(AntiVanishRosterTracker.RosterMember::resetEvidence);
   }

   public void clearPending() {
      this.pendingRequests.clear();
   }

   public void clearLastSeen() {
      this.lastSeen.clear();
   }

   public void reset() {
      this.members.clear();
      this.pendingRequests.clear();
      this.lastSeen.clear();
      this.lastRequestAt.clear();
      this.liveProfiles = Map.of();
      this.uuidByName = Map.of();
   }

   private static String normalizeName(String name) {
      return name.toLowerCase(Locale.ROOT);
   }

   private boolean tryRequest(UUID id, long now) {
      Long last = this.lastRequestAt.get(id);
      if (last != null && now - last < 12000L) {
         return false;
      }

      this.lastRequestAt.put(id, now);
      this.lastRequestAt.entrySet().removeIf(entry -> now - entry.getValue() > 30000L);
      return true;
   }

   private record PendingRequest(String name, long requestedAt, long observedAt, int init_I) {
   }

   private static final class RosterMember {
      final UUID id;
      final String name;
      long presentSince = -1L;
      long leftAt = -1L;
      long lastSeenAt;
      long actionStreakStart = -1L;
      long lastActionAt = -1L;
      long lastCompletionRequestAt = -1L;
      boolean present;
      boolean settled;
      boolean bulk;
      boolean listed;
      int actionHits;
      int completionHits;

      RosterMember(UUID id, String name) {
         this.id = id;
         this.name = name;
      }

      void markPresent(long now) {
         if (!this.present) {
            this.presentSince = now;
            this.resetEvidence();
         }

         this.present = true;
         this.settled = now - this.presentSince >= 3000L;
         this.leftAt = -1L;
         this.bulk = false;
      }

      void markAbsent(long now, boolean bulk) {
         if (this.present) {
            this.settled = this.settled | now - this.presentSince >= 3000L;
            this.present = false;
            this.leftAt = now;
            this.bulk = bulk;
            this.resetEvidence();
         }
      }

      boolean isRecentlyGone(long now) {
         return this.settled && !this.present && !this.bulk && this.leftAt >= 0L && now - this.leftAt >= 2000L && now - this.lastSeenAt <= 30000L;
      }

      boolean isLikelyVanished(long now) {
         return this.isRecentlyGone(now) && this.actionHits > 0 && (this.actionHits >= 2 || this.completionHits >= 2) && (this.listed || this.completionHits >= 2);
      }

      void resetEvidence() {
         this.actionHits = this.completionHits = 0;
         this.actionStreakStart = this.lastActionAt = this.lastCompletionRequestAt = -1L;
      }

      void expireStale(long now) {
         if (this.lastActionAt >= 0L && now - this.lastActionAt > 4000L || this.actionStreakStart >= 0L && now - this.actionStreakStart > 4000L) {
            this.actionHits = 0;
            this.actionStreakStart = this.lastActionAt = -1L;
         }

         if (this.lastCompletionRequestAt >= 0L && now - this.lastCompletionRequestAt > 4000L) {
            this.completionHits = 0;
            this.lastCompletionRequestAt = -1L;
         }
      }
   }

   public record RosterProfile(UUID uuid, String name, boolean listed, boolean tracked) {
   }

   public record ActorInfo(UUID id, String name, boolean init_Z, boolean npc) {
   }

   public record VanishAlert(UUID uuid, String name, String evidence, boolean uuidOnly) {
      public VanishAlert(UUID id, String name, String evidence) {
         this(id, name, evidence, false);
      }
   }

   private record LastSeen(long seenAt, int init_I) {
   }
}

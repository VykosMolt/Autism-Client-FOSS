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
   private final Map<UUID, AntiVanishRosterTracker.Inner_CachePool> members = new HashMap<>();
   private final Map<UUID, AntiVanishRosterTracker.Inner_BufferHelper> pendingRequests = new HashMap<>();
   private final Map<UUID, AntiVanishRosterTracker.Inner_TaskQueue> lastSeen = new HashMap<>();
   private final Map<UUID, Long> write_Map = new HashMap<>();
   private Map<UUID, AntiVanishRosterTracker.Inner_ConfigStore> update_Map = Map.of();
   private Map<String, UUID> sync_Map = Map.of();

   public void init(Collection<AntiVanishRosterTracker.Inner_ConfigStore> profiles, long now) {
      if (profiles.size() > 4096) {
         this.write();
      } else {
         Map<UUID, AntiVanishRosterTracker.Inner_ConfigStore> live = new HashMap<>();
         Set<String> names = new HashSet<>();
         Set<String> duplicates = new HashSet<>();

         for (AntiVanishRosterTracker.Inner_ConfigStore p : profiles) {
            if (p.uuid != null && p.name != null) {
               live.put(p.uuid, p);
               if (!names.add(normalizeName(p.name))) {
                  duplicates.add(normalizeName(p.name));
               }
            }
         }

         this.update_Map = Map.copyOf(live);
         Map<String, UUID> byName = new HashMap<>();

         for (AntiVanishRosterTracker.Inner_ConfigStore p : live.values()) {
            byName.put(normalizeName(p.name), p.uuid);
         }

         this.sync_Map = Map.copyOf(byName);
         this.lastSeen.keySet().removeIf(live::containsKey);
         this.pendingRequests
            .entrySet()
            .removeIf(
               entry -> {
                  AntiVanishRosterTracker.Inner_ConfigStore px = live.get(entry.getKey());
                  String name = normalizeName(entry.getValue().name);
                  UUID client = byName.get(name);
                  return duplicates.contains(name)
                     || client != null && !client.equals(entry.getKey())
                     || px != null && (px.listed || !px.load || !px.name.equals(entry.getValue().name));
               }
            );
         int departures = 0;

         for (AntiVanishRosterTracker.Inner_CachePool m : this.members.values()) {
            AntiVanishRosterTracker.Inner_ConfigStore p = live.get(m.id);
            if (m.present && (p == null || !p.listed)) {
               departures++;
            }
         }

         for (AntiVanishRosterTracker.Inner_ConfigStore p : live.values()) {
            if (p.load && !duplicates.contains(normalizeName(p.name)) && p.name.matches("[A-Za-z0-9_]{1,16}")) {
               AntiVanishRosterTracker.Inner_CachePool m = this.members.get(p.uuid);
               if (m == null || !m.name.equals(p.name)) {
                  m = new AntiVanishRosterTracker.Inner_CachePool(p.uuid, p.name);
                  this.members.put(p.uuid, m);
               }

               m.write_Z = true;
               m.read_J = now;
               if (p.listed) {
                  m.markPresent(now);
               } else {
                  m.init(now, departures >= 4);
               }
            } else {
               this.members.remove(p.uuid);
            }
         }

         for (AntiVanishRosterTracker.Inner_CachePool m : this.members.values()) {
            if (!live.containsKey(m.id)) {
               m.write_Z = false;
               m.init(now, departures >= 4);
            }
         }

         this.members.values().removeIf(mx -> now - mx.read_J > 30000L);
         if (this.members.size() > 4096) {
            this.write();
         }
      }
   }

   public void init(Collection<AntiVanishRosterTracker.Inner_ConfigStore> profiles, long requestedAt, long now) {
      if (requestedAt <= now && now - requestedAt <= 4000L && profiles.size() <= 4096) {
         Set<UUID> ids = new HashSet<>();
         Set<String> names = new HashSet<>();

         for (AntiVanishRosterTracker.Inner_ConfigStore p : profiles) {
            if (p.uuid == null || p.name == null || !ids.add(p.uuid) || !names.add(normalizeName(p.name))) {
               return;
            }
         }

         Set<UUID> seen = new HashSet<>();

         for (AntiVanishRosterTracker.Inner_ConfigStore p : profiles) {
            if (p.uuid != null && p.name != null && p.load && !p.listed && p.name.matches("[A-Za-z0-9_]{1,16}")) {
               AntiVanishRosterTracker.Inner_ConfigStore client = this.update_Map.get(p.uuid);
               if (client == null || !client.listed && client.load && client.name.equals(p.name)) {
                  UUID other = this.sync_Map.get(normalizeName(p.name));
                  if (other == null || other.equals(p.uuid)) {
                     if (!seen.add(p.uuid)) {
                        this.pendingRequests.remove(p.uuid);
                     } else {
                        AntiVanishRosterTracker.Inner_BufferHelper fact = this.pendingRequests.get(p.uuid);
                        if (fact == null || !fact.name.equals(p.name) || now - fact.load > 20000L) {
                           this.pendingRequests.put(p.uuid, new AntiVanishRosterTracker.Inner_BufferHelper(p.name, requestedAt, now, 1));
                        } else if (requestedAt - fact.requestedAt >= 1500L) {
                           this.pendingRequests.put(p.uuid, new AntiVanishRosterTracker.Inner_BufferHelper(p.name, requestedAt, now, Math.min(2, fact.init_I + 1)));
                        }
                     }
                  }
               }
            }
         }

         this.pendingRequests.keySet().retainAll(seen);
      }
   }

   public void init(Set<UUID> unknown, long now) {
      if (unknown.size() <= 4096) {
         Set<UUID> accepted = new HashSet<>();

         for (UUID id : unknown) {
            if (id != null && id.variant() == 2 && id.version() >= 3 && id.version() <= 5 && !this.update_Map.containsKey(id)) {
               accepted.add(id);
               AntiVanishRosterTracker.Inner_TaskQueue before = this.lastSeen.get(id);
               if (before == null || now - before.seenAt > 75000L) {
                  this.lastSeen.put(id, new AntiVanishRosterTracker.Inner_TaskQueue(now, 1));
               } else if (now - before.seenAt >= 5000L) {
                  this.lastSeen.put(id, new AntiVanishRosterTracker.Inner_TaskQueue(now, Math.min(2, before.init_I + 1)));
               }
            }
         }

         this.lastSeen.keySet().retainAll(accepted);
      }
   }

   public void init(AntiVanishRosterTracker.Inner_DataUtil actor, String action, long now) {
      if (actor != null && actor.init_Z && !actor.load && action != null && !action.isBlank()) {
         AntiVanishRosterTracker.Inner_CachePool m = this.members.get(actor.id);
         if (m != null && m.name.equals(actor.name) && m.init_Z(now)) {
            m.expireStale(now);
            if (m.update < 0L || now - m.update >= 250L) {
               m.update = now;
               if (m.write_J < 0L) {
                  m.write_J = now;
               }

               m.init_I = Math.min(2, m.init_I + 1);
            }
         }
      }
   }

   public void load(Collection<String> names, long requestedAt, long now) {
      if (now >= requestedAt && now - requestedAt <= 4000L) {
         Set<String> offered = new HashSet<>();

         for (String name : names) {
            if (name != null) {
               offered.add(normalizeName(name));
            }
         }

         for (AntiVanishRosterTracker.Inner_CachePool m : this.members.values()) {
            if (m.init_Z(now) && requestedAt >= m.load_J + 2000L) {
               if (!offered.contains(normalizeName(m.name))) {
                  m.load_I = 0;
                  m.sync = -1L;
               } else {
                  m.expireStale(now);
                  if (m.sync < 0L || requestedAt - m.sync >= 1000L) {
                     m.sync = requestedAt;
                     m.load_I = Math.min(2, m.load_I + 1);
                  }
               }
            }
         }
      }
   }

   public List<AntiVanishRosterTracker.Inner_StreamUtil> collectAlerts(long now) {
      List<AntiVanishRosterTracker.Inner_StreamUtil> out = new ArrayList<>();
      this.pendingRequests.entrySet().removeIf(entry -> now - entry.getValue().load > 20000L);

      for (Entry<UUID, AntiVanishRosterTracker.Inner_BufferHelper> entry : this.pendingRequests.entrySet()) {
         AntiVanishRosterTracker.Inner_BufferHelper fact = entry.getValue();
         if (fact.init_I >= 2 && this.load(entry.getKey(), now)) {
            out.add(new AntiVanishRosterTracker.Inner_StreamUtil(entry.getKey(), fact.name, "online outside TAB in two server roster checks"));
         }
      }

      this.lastSeen.entrySet().removeIf(entry -> now - entry.getValue().seenAt > 75000L);

      for (Entry<UUID, AntiVanishRosterTracker.Inner_TaskQueue> entry : this.lastSeen.entrySet()) {
         if (entry.getValue().init_I >= 2 && this.load(entry.getKey(), now)) {
            UUID id = entry.getKey();
            out.add(
               new AntiVanishRosterTracker.Inner_StreamUtil(id, "UUID:" + id.toString().substring(0, 8), "unlisted UUID in two complete player-roster updates: " + id, true)
            );
         }
      }

      for (AntiVanishRosterTracker.Inner_CachePool m : this.members.values()) {
         m.expireStale(now);
         if (m.load_Z(now) && this.load(m.id, now)) {
            out.add(
               new AntiVanishRosterTracker.Inner_StreamUtil(
                  m.id, m.name, m.init_I >= 2 ? "repeated player actions outside TAB" : "player action + repeated online completion"
               )
            );
         }
      }

      return List.copyOf(out);
   }

   public boolean init(UUID id, long now) {
      AntiVanishRosterTracker.Inner_BufferHelper fact = this.pendingRequests.get(id);
      if (fact != null && fact.init_I >= 2 && now - fact.load <= 20000L) {
         return true;
      }

      AntiVanishRosterTracker.Inner_TaskQueue pulse = this.lastSeen.get(id);
      if (pulse != null && pulse.init_I >= 2 && now - pulse.seenAt <= 75000L) {
         return true;
      }

      AntiVanishRosterTracker.Inner_CachePool m = this.members.get(id);
      if (m != null) {
         m.expireStale(now);
      }

      return m != null && m.load_Z(now);
   }

   public boolean hasRecentHiddenAction(long now) {
      return this.members.values().stream().anyMatch(m -> m.init_Z(now) && m.update >= 0L && now - m.update <= 4000L);
   }

   public boolean needsRecheck() {
      return this.pendingRequests.values().stream().anyMatch(fact -> fact.init_I < 2);
   }

   public void init(String systemMessage, long now) {
      for (AntiVanishRosterTracker.Inner_CachePool m : this.members.values()) {
         if (NormalizedTextMatcher.load(systemMessage, m.name)) {
            m.init();
            if (!m.present) {
               m.load_J = now;
            }

            this.write_Map.remove(m.id);
         }
      }

      this.pendingRequests.values().removeIf(p -> NormalizedTextMatcher.load(systemMessage, p.name));
   }

   public int trackedCount() {
      return this.members.size() + this.pendingRequests.size() + this.lastSeen.size();
   }

   public void resetProbes() {
      this.members.values().forEach(AntiVanishRosterTracker.Inner_CachePool::init);
   }

   public void load() {
      this.pendingRequests.clear();
   }

   public void read() {
      this.lastSeen.clear();
   }

   public void write() {
      this.members.clear();
      this.pendingRequests.clear();
      this.lastSeen.clear();
      this.write_Map.clear();
      this.update_Map = Map.of();
      this.sync_Map = Map.of();
   }

   private static String normalizeName(String name) {
      return name.toLowerCase(Locale.ROOT);
   }

   private boolean load(UUID id, long now) {
      Long last = this.write_Map.get(id);
      if (last != null && now - last < 12000L) {
         return false;
      }

      this.write_Map.put(id, now);
      this.write_Map.entrySet().removeIf(entry -> now - entry.getValue() > 30000L);
      return true;
   }

   private record Inner_BufferHelper(String name, long requestedAt, long load, int init_I) {
   }

   private static final class Inner_CachePool {
      final UUID id;
      final String name;
      long presentSince = -1L;
      long load_J = -1L;
      long read_J;
      long write_J = -1L;
      long update = -1L;
      long sync = -1L;
      boolean present;
      boolean settled;
      boolean bulk;
      boolean write_Z;
      int init_I;
      int load_I;

      Inner_CachePool(UUID id, String name) {
         this.id = id;
         this.name = name;
      }

      void markPresent(long now) {
         if (!this.present) {
            this.presentSince = now;
            this.init();
         }

         this.present = true;
         this.settled = now - this.presentSince >= 3000L;
         this.load_J = -1L;
         this.bulk = false;
      }

      void init(long now, boolean bulk) {
         if (this.present) {
            this.settled = this.settled | now - this.presentSince >= 3000L;
            this.present = false;
            this.load_J = now;
            this.bulk = bulk;
            this.init();
         }
      }

      boolean init_Z(long now) {
         return this.settled && !this.present && !this.bulk && this.load_J >= 0L && now - this.load_J >= 2000L && now - this.read_J <= 30000L;
      }

      boolean load_Z(long now) {
         return this.init_Z(now) && this.init_I > 0 && (this.init_I >= 2 || this.load_I >= 2) && (this.write_Z || this.load_I >= 2);
      }

      void init() {
         this.init_I = this.load_I = 0;
         this.write_J = this.update = this.sync = -1L;
      }

      void expireStale(long now) {
         if (this.update >= 0L && now - this.update > 4000L || this.write_J >= 0L && now - this.write_J > 4000L) {
            this.init_I = 0;
            this.write_J = this.update = -1L;
         }

         if (this.sync >= 0L && now - this.sync > 4000L) {
            this.load_I = 0;
            this.sync = -1L;
         }
      }
   }

   public record Inner_ConfigStore(UUID uuid, String name, boolean listed, boolean load) {
   }

   public record Inner_DataUtil(UUID id, String name, boolean init_Z, boolean load) {
   }

   public record Inner_StreamUtil(UUID uuid, String name, String load, boolean uuidOnly) {
      public Inner_StreamUtil(UUID id, String name, String evidence) {
         this(id, name, evidence, false);
      }
   }

   private record Inner_TaskQueue(long seenAt, int init_I) {
   }
}

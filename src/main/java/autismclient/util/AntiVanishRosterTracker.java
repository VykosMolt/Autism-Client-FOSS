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
   private final Map<UUID, AntiVanishRosterTracker.Inner_CachePool> init_Map = new HashMap<>();
   private final Map<UUID, AntiVanishRosterTracker.Inner_BufferHelper> load_Map = new HashMap<>();
   private final Map<UUID, AntiVanishRosterTracker.Inner_TaskQueue> read_Map = new HashMap<>();
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
               if (!names.add(init_Str_Str(p.name))) {
                  duplicates.add(init_Str_Str(p.name));
               }
            }
         }

         this.update_Map = Map.copyOf(live);
         Map<String, UUID> byName = new HashMap<>();

         for (AntiVanishRosterTracker.Inner_ConfigStore p : live.values()) {
            byName.put(init_Str_Str(p.name), p.uuid);
         }

         this.sync_Map = Map.copyOf(byName);
         this.read_Map.keySet().removeIf(live::containsKey);
         this.load_Map
            .entrySet()
            .removeIf(
               entry -> {
                  AntiVanishRosterTracker.Inner_ConfigStore px = live.get(entry.getKey());
                  String name = init_Str_Str(entry.getValue().init_Str);
                  UUID client = byName.get(name);
                  return duplicates.contains(name)
                     || client != null && !client.equals(entry.getKey())
                     || px != null && (px.listed || !px.load || !px.name.equals(entry.getValue().init_Str));
               }
            );
         int departures = 0;

         for (AntiVanishRosterTracker.Inner_CachePool m : this.init_Map.values()) {
            AntiVanishRosterTracker.Inner_ConfigStore p = live.get(m.init_UUID);
            if (m.init_Z && (p == null || !p.listed)) {
               departures++;
            }
         }

         for (AntiVanishRosterTracker.Inner_ConfigStore p : live.values()) {
            if (p.load && !duplicates.contains(init_Str_Str(p.name)) && p.name.matches("[A-Za-z0-9_]{1,16}")) {
               AntiVanishRosterTracker.Inner_CachePool m = this.init_Map.get(p.uuid);
               if (m == null || !m.init_Str.equals(p.name)) {
                  m = new AntiVanishRosterTracker.Inner_CachePool(p.uuid, p.name);
                  this.init_Map.put(p.uuid, m);
               }

               m.write_Z = true;
               m.read_J = now;
               if (p.listed) {
                  m.init_V(now);
               } else {
                  m.init(now, departures >= 4);
               }
            } else {
               this.init_Map.remove(p.uuid);
            }
         }

         for (AntiVanishRosterTracker.Inner_CachePool m : this.init_Map.values()) {
            if (!live.containsKey(m.init_UUID)) {
               m.write_Z = false;
               m.init(now, departures >= 4);
            }
         }

         this.init_Map.values().removeIf(mx -> now - mx.read_J > 30000L);
         if (this.init_Map.size() > 4096) {
            this.write();
         }
      }
   }

   public void init(Collection<AntiVanishRosterTracker.Inner_ConfigStore> profiles, long requestedAt, long now) {
      if (requestedAt <= now && now - requestedAt <= 4000L && profiles.size() <= 4096) {
         Set<UUID> ids = new HashSet<>();
         Set<String> names = new HashSet<>();

         for (AntiVanishRosterTracker.Inner_ConfigStore p : profiles) {
            if (p.uuid == null || p.name == null || !ids.add(p.uuid) || !names.add(init_Str_Str(p.name))) {
               return;
            }
         }

         Set<UUID> seen = new HashSet<>();

         for (AntiVanishRosterTracker.Inner_ConfigStore p : profiles) {
            if (p.uuid != null && p.name != null && p.load && !p.listed && p.name.matches("[A-Za-z0-9_]{1,16}")) {
               AntiVanishRosterTracker.Inner_ConfigStore client = this.update_Map.get(p.uuid);
               if (client == null || !client.listed && client.load && client.name.equals(p.name)) {
                  UUID other = this.sync_Map.get(init_Str_Str(p.name));
                  if (other == null || other.equals(p.uuid)) {
                     if (!seen.add(p.uuid)) {
                        this.load_Map.remove(p.uuid);
                     } else {
                        AntiVanishRosterTracker.Inner_BufferHelper fact = this.load_Map.get(p.uuid);
                        if (fact == null || !fact.init_Str.equals(p.name) || now - fact.load > 20000L) {
                           this.load_Map.put(p.uuid, new AntiVanishRosterTracker.Inner_BufferHelper(p.name, requestedAt, now, 1));
                        } else if (requestedAt - fact.init_J >= 1500L) {
                           this.load_Map.put(p.uuid, new AntiVanishRosterTracker.Inner_BufferHelper(p.name, requestedAt, now, Math.min(2, fact.init_I + 1)));
                        }
                     }
                  }
               }
            }
         }

         this.load_Map.keySet().retainAll(seen);
      }
   }

   public void init(Set<UUID> unknown, long now) {
      if (unknown.size() <= 4096) {
         Set<UUID> accepted = new HashSet<>();

         for (UUID id : unknown) {
            if (id != null && id.variant() == 2 && id.version() >= 3 && id.version() <= 5 && !this.update_Map.containsKey(id)) {
               accepted.add(id);
               AntiVanishRosterTracker.Inner_TaskQueue before = this.read_Map.get(id);
               if (before == null || now - before.init_J > 75000L) {
                  this.read_Map.put(id, new AntiVanishRosterTracker.Inner_TaskQueue(now, 1));
               } else if (now - before.init_J >= 5000L) {
                  this.read_Map.put(id, new AntiVanishRosterTracker.Inner_TaskQueue(now, Math.min(2, before.init_I + 1)));
               }
            }
         }

         this.read_Map.keySet().retainAll(accepted);
      }
   }

   public void init(AntiVanishRosterTracker.Inner_DataUtil actor, String action, long now) {
      if (actor != null && actor.init_Z && !actor.load && action != null && !action.isBlank()) {
         AntiVanishRosterTracker.Inner_CachePool m = this.init_Map.get(actor.init_UUID);
         if (m != null && m.init_Str.equals(actor.init_Str) && m.init_Z(now)) {
            m.load_V(now);
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
               offered.add(init_Str_Str(name));
            }
         }

         for (AntiVanishRosterTracker.Inner_CachePool m : this.init_Map.values()) {
            if (m.init_Z(now) && requestedAt >= m.load_J + 2000L) {
               if (!offered.contains(init_Str_Str(m.init_Str))) {
                  m.load_I = 0;
                  m.sync = -1L;
               } else {
                  m.load_V(now);
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
      this.load_Map.entrySet().removeIf(entry -> now - entry.getValue().load > 20000L);

      for (Entry<UUID, AntiVanishRosterTracker.Inner_BufferHelper> entry : this.load_Map.entrySet()) {
         AntiVanishRosterTracker.Inner_BufferHelper fact = entry.getValue();
         if (fact.init_I >= 2 && this.load(entry.getKey(), now)) {
            out.add(new AntiVanishRosterTracker.Inner_StreamUtil(entry.getKey(), fact.init_Str, "online outside TAB in two server roster checks"));
         }
      }

      this.read_Map.entrySet().removeIf(entry -> now - entry.getValue().init_J > 75000L);

      for (Entry<UUID, AntiVanishRosterTracker.Inner_TaskQueue> entry : this.read_Map.entrySet()) {
         if (entry.getValue().init_I >= 2 && this.load(entry.getKey(), now)) {
            UUID id = entry.getKey();
            out.add(
               new AntiVanishRosterTracker.Inner_StreamUtil(id, "UUID:" + id.toString().substring(0, 8), "unlisted UUID in two complete player-roster updates: " + id, true)
            );
         }
      }

      for (AntiVanishRosterTracker.Inner_CachePool m : this.init_Map.values()) {
         m.load_V(now);
         if (m.load_Z(now) && this.load(m.init_UUID, now)) {
            out.add(
               new AntiVanishRosterTracker.Inner_StreamUtil(
                  m.init_UUID, m.init_Str, m.init_I >= 2 ? "repeated player actions outside TAB" : "player action + repeated online completion"
               )
            );
         }
      }

      return List.copyOf(out);
   }

   public boolean init(UUID id, long now) {
      AntiVanishRosterTracker.Inner_BufferHelper fact = this.load_Map.get(id);
      if (fact != null && fact.init_I >= 2 && now - fact.load <= 20000L) {
         return true;
      }

      AntiVanishRosterTracker.Inner_TaskQueue pulse = this.read_Map.get(id);
      if (pulse != null && pulse.init_I >= 2 && now - pulse.init_J <= 75000L) {
         return true;
      }

      AntiVanishRosterTracker.Inner_CachePool m = this.init_Map.get(id);
      if (m != null) {
         m.load_V(now);
      }

      return m != null && m.load_Z(now);
   }

   public boolean hasRecentHiddenAction(long now) {
      return this.init_Map.values().stream().anyMatch(m -> m.init_Z(now) && m.update >= 0L && now - m.update <= 4000L);
   }

   public boolean needsRecheck() {
      return this.load_Map.values().stream().anyMatch(fact -> fact.init_I < 2);
   }

   public void init(String systemMessage, long now) {
      for (AntiVanishRosterTracker.Inner_CachePool m : this.init_Map.values()) {
         if (NormalizedTextMatcher.load(systemMessage, m.init_Str)) {
            m.init();
            if (!m.init_Z) {
               m.load_J = now;
            }

            this.write_Map.remove(m.init_UUID);
         }
      }

      this.load_Map.values().removeIf(p -> NormalizedTextMatcher.load(systemMessage, p.init_Str));
   }

   public int trackedCount() {
      return this.init_Map.size() + this.load_Map.size() + this.read_Map.size();
   }

   public void resetProbes() {
      this.init_Map.values().forEach(AntiVanishRosterTracker.Inner_CachePool::init);
   }

   public void load() {
      this.load_Map.clear();
   }

   public void read() {
      this.read_Map.clear();
   }

   public void write() {
      this.init_Map.clear();
      this.load_Map.clear();
      this.read_Map.clear();
      this.write_Map.clear();
      this.update_Map = Map.of();
      this.sync_Map = Map.of();
   }

   private static String init_Str_Str(String name) {
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

   private record Inner_BufferHelper(String init_Str, long init_J, long load, int init_I) {
   }

   private static final class Inner_CachePool {
      final UUID init_UUID;
      final String init_Str;
      long init_J = -1L;
      long load_J = -1L;
      long read_J;
      long write_J = -1L;
      long update = -1L;
      long sync = -1L;
      boolean init_Z;
      boolean load_Z;
      boolean read_Z;
      boolean write_Z;
      int init_I;
      int load_I;

      Inner_CachePool(UUID id, String name) {
         this.init_UUID = id;
         this.init_Str = name;
      }

      void init_V(long now) {
         if (!this.init_Z) {
            this.init_J = now;
            this.init();
         }

         this.init_Z = true;
         this.load_Z = now - this.init_J >= 3000L;
         this.load_J = -1L;
         this.read_Z = false;
      }

      void init(long now, boolean bulk) {
         if (this.init_Z) {
            this.load_Z = this.load_Z | now - this.init_J >= 3000L;
            this.init_Z = false;
            this.load_J = now;
            this.read_Z = bulk;
            this.init();
         }
      }

      boolean init_Z(long now) {
         return this.load_Z && !this.init_Z && !this.read_Z && this.load_J >= 0L && now - this.load_J >= 2000L && now - this.read_J <= 30000L;
      }

      boolean load_Z(long now) {
         return this.init_Z(now) && this.init_I > 0 && (this.init_I >= 2 || this.load_I >= 2) && (this.write_Z || this.load_I >= 2);
      }

      void init() {
         this.init_I = this.load_I = 0;
         this.write_J = this.update = this.sync = -1L;
      }

      void load_V(long now) {
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

   public record Inner_DataUtil(UUID init_UUID, String init_Str, boolean init_Z, boolean load) {
   }

   public record Inner_StreamUtil(UUID uuid, String name, String load, boolean uuidOnly) {
      public Inner_StreamUtil(UUID id, String name, String evidence) {
         this(id, name, evidence, false);
      }
   }

   private record Inner_TaskQueue(long init_J, int init_I) {
   }
}

package autismclient.util;

import autismclient.AutismClientAddon;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.lang.reflect.Type;
import java.net.SocketAddress;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.client.server.IntegratedServer;
import net.minecraft.world.level.storage.LevelResource;

public final class AutismWaypoints {
   public static final long init_J = 30000L;
   private static final File init_File = new File(AutismClientAddon.FOLDER, "waypoints.json");
   private static final File load_File = new File(AutismClientAddon.FOLDER, "waypoints.json.tmp");
   private static final File auditBackupFile = new File(AutismClientAddon.FOLDER, "waypoints.json.bak");
   private static volatile boolean auditBackupPrimary = true;
   private static final Gson init_Gson = new GsonBuilder().setPrettyPrinting().create();
   private static final Type init_Type = (new TypeToken<Map<String, List<AutismWaypoints.Waypoint>>>() {}).getType();
   private static final int init_I = 512;
   private static final long load_J = 400L;
   private static volatile AutismWaypoints init_AutismWaypoints;
   private static final ScheduledExecutorService init_ScheduledExecutorS = Executors.newSingleThreadScheduledExecutor(r -> {
      Thread t = new Thread(r, "waypoints-writer");
      t.setDaemon(true);
      return t;
   });
   private final AtomicBoolean init_AtomicBoolean = new AtomicBoolean(false);
   private int auditSaveFailures;
   private boolean auditRetryPending;
   private final AtomicBoolean load_AtomicBoolean = new AtomicBoolean(false);
   private final AtomicLong init_AtomicLong = new AtomicLong();
   private final Map<String, List<AutismWaypoints.Waypoint>> init_Map = new LinkedHashMap<>();

   private AutismWaypoints() {
   }

   public static AutismWaypoints getInstance() {
      AutismWaypoints local = init_AutismWaypoints;
      if (local == null) {
         synchronized (AutismWaypoints.class) {
            if (init_AutismWaypoints == null) {
               init_AutismWaypoints = load_AutismWaypoints();
            }

            local = init_AutismWaypoints;
         }
      }

      return local;
   }

   public long revision() {
      return this.init_AtomicLong.get();
   }

   public static String scopeFor(Minecraft mc) {
      if (mc == null) {
         return "unknown";
      }

      ServerData server = mc.getCurrentServer();
      if (server != null && server.ip != null && !server.ip.isBlank()) {
         return server.ip.trim().toLowerCase(Locale.ROOT);
      }

      if (mc.hasSingleplayerServer()) {
         IntegratedServer integrated = mc.getSingleplayerServer();
         if (integrated != null) {
            try {
               Path worldDir = integrated.getWorldPath(LevelResource.ROOT).normalize().getFileName();
               if (worldDir != null) {
                  String folder = worldDir.toString().trim();
                  if (!folder.isEmpty()) {
                     return folder;
                  }
               }
            } catch (Throwable var5) {
            }

            if (integrated.getWorldData() != null) {
               String levelName = integrated.getWorldData().getLevelName();
               if (levelName != null && !levelName.isBlank()) {
                  return levelName.trim();
               }
            }
         }
      }

      if (mc.getConnection() != null && mc.getConnection().getConnection() != null) {
         SocketAddress remote = mc.getConnection().getConnection().getRemoteAddress();
         if (remote != null) {
            String addr = remote.toString().trim().toLowerCase(Locale.ROOT);
            if (addr.startsWith("/")) {
               addr = addr.substring(1);
            }

            if (!addr.isEmpty()) {
               return addr;
            }
         }
      }

      return "unknown";
   }

   public List<AutismWaypoints.Waypoint> list(String scope) {
      synchronized (this.init_Map) {
         List<AutismWaypoints.Waypoint> list = this.init_Map.get(init_Str_Str(scope));
         return list == null ? List.of() : List.copyOf(list);
      }
   }

   public void addWaypoint(String scope, AutismWaypoints.Waypoint waypoint) {
      if (waypoint != null && waypoint.name() != null && !waypoint.name().isBlank()) {
         boolean changed = false;
         synchronized (this.init_Map) {
            List<AutismWaypoints.Waypoint> list = this.init_Map.computeIfAbsent(init_Str_Str(scope), key -> new ArrayList<>());

            for (int i = 0; i < list.size(); i++) {
               if (list.get(i).name().equalsIgnoreCase(waypoint.name())) {
                  list.set(i, waypoint);
                  changed = true;
                  break;
               }
            }

            if (!changed) {
               if (list.size() >= 512) {
                  return;
               }

               list.add(waypoint);
               changed = true;
            }
         }

         if (changed) {
            this.init_V();
         }
      }
   }

   public boolean remove(String scope, String name) {
      if (name != null && !name.isBlank()) {
         boolean removed = false;
         synchronized (this.init_Map) {
            List<AutismWaypoints.Waypoint> list = this.init_Map.get(init_Str_Str(scope));
            if (list != null) {
               removed = list.removeIf(waypoint -> waypoint.name().equalsIgnoreCase(name.trim()));
               if (list.isEmpty()) {
                  this.init_Map.remove(init_Str_Str(scope));
               }
            }
         }

         if (removed) {
            this.init_V();
         }

         return removed;
      } else {
         return false;
      }
   }

   public void clearScope(String scope) {
      boolean removed;
      synchronized (this.init_Map) {
         removed = this.init_Map.remove(init_Str_Str(scope)) != null;
      }

      if (removed) {
         this.init_V();
      }
   }

   public AutismWaypoints.Waypoint find(String scope, String name) {
      if (name == null) {
         return null;
      }

      synchronized (this.init_Map) {
         List<AutismWaypoints.Waypoint> list = this.init_Map.get(init_Str_Str(scope));
         if (list == null) {
            return null;
         }

         for (AutismWaypoints.Waypoint waypoint : list) {
            if (waypoint.name().equalsIgnoreCase(name.trim())) {
               return waypoint;
            }
         }

         return null;
      }
   }

   public int colorOf(String scope, String name, int fallback) {
      AutismWaypoints.Waypoint waypoint = this.find(scope, name);
      return waypoint == null ? fallback : waypoint.color();
   }

   public boolean setColor(String scope, String name, int color) {
      AutismWaypoints.Waypoint updated = null;
      synchronized (this.init_Map) {
         List<AutismWaypoints.Waypoint> list = this.init_Map.get(init_Str_Str(scope));
         if (list != null) {
            for (int i = 0; i < list.size(); i++) {
               AutismWaypoints.Waypoint waypoint = list.get(i);
               if (waypoint.name().equalsIgnoreCase(name == null ? "" : name.trim())) {
                  updated = new AutismWaypoints.Waypoint(
                     waypoint.name(), waypoint.x(), waypoint.y(), waypoint.z(), color, waypoint.createdMs(), waypoint.death()
                  );
                  list.set(i, updated);
                  break;
               }
            }
         }
      }

      if (updated == null) {
         return false;
      }

      this.init_V();
      return true;
   }

   public boolean rename(String scope, String oldName, String newName) {
      if (oldName != null && newName != null) {
         String trimmed = newName.trim();
         if (trimmed.isEmpty()) {
            return false;
         }

         String old = oldName.trim();
         synchronized (this.init_Map) {
            List<AutismWaypoints.Waypoint> list = this.init_Map.get(init_Str_Str(scope));
            if (list == null) {
               return false;
            }

            int index = -1;

            for (int i = 0; i < list.size(); i++) {
               String name = list.get(i).name();
               if (name.equalsIgnoreCase(trimmed) && !name.equalsIgnoreCase(old)) {
                  return false;
               }

               if (name.equalsIgnoreCase(old)) {
                  index = i;
               }
            }

            if (index < 0) {
               return false;
            }

            AutismWaypoints.Waypoint current = list.get(index);
            if (current.name().equals(trimmed)) {
               return true;
            }

            list.set(index, new AutismWaypoints.Waypoint(trimmed, current.x(), current.y(), current.z(), current.color(), current.createdMs(), current.death()));
         }

         this.init_V();
         return true;
      } else {
         return false;
      }
   }

   public String nextName(String var1, String var2) {
      return AuditDeathWaypoints.nextName(this.list(var1), var2);
   }

   public AutismWaypoints.Waypoint init(String var1, int var2, int var3, int var4, int var5, long var6, int var8) {
      AutismWaypoints.Waypoint var10000 = AuditDeathWaypoints.kw_record_AutismWaypoints_Wa(
         this.init_Map, init_Str_Str(var1), var2, var3, var4, var5, var6, var8
      );
      if (var10000 != null) {
         this.init_V();
      }

      return var10000;
   }

   public void trimDeaths(String scope, int maxDeaths) {
      if (maxDeaths >= 1) {
         boolean changed = false;
         synchronized (this.init_Map) {
            List<AutismWaypoints.Waypoint> list = this.init_Map.get(init_Str_Str(scope));
            if (list == null) {
               return;
            }

            List<AutismWaypoints.Waypoint> deaths = new ArrayList<>();

            for (AutismWaypoints.Waypoint waypoint : list) {
               if (waypoint.death()) {
                  deaths.add(waypoint);
               }
            }

            if (deaths.size() <= maxDeaths) {
               return;
            }

            deaths.sort((a, b) -> Long.compare(b.createdMs(), a.createdMs()));

            for (int i = maxDeaths; i < deaths.size(); i++) {
               changed |= list.remove(deaths.get(i));
            }
         }

         if (changed) {
            this.init_V();
         }
      }
   }

   private static String init_Str_Str(String scope) {
      return scope != null && !scope.isBlank() ? scope.trim() : "unknown";
   }

   private void init_V() {
      this.init_AtomicLong.incrementAndGet();
      this.load_V();
   }

   private static AutismWaypoints load_AutismWaypoints() {
      AutismWaypoints waypoints = new AutismWaypoints();
      boolean loaded = false;
      if (init_File.exists()) {
         loaded = auditLoad(waypoints, init_File);
         if (!loaded) {
            File corrupt = new File(init_File.getPath() + ".corrupt-" + System.currentTimeMillis());
            try {
               Files.move(init_File.toPath(), corrupt.toPath());
               AutismClientAddon.LOG.warn("Kept the unreadable waypoints file as {}", corrupt.getName());
            } catch (Throwable t) {
               auditBackupPrimary = false;
               AutismClientAddon.LOG.warn("Could not move the unreadable waypoints file aside", t);
            }

            waypoints.init_Map.clear();
         }
      }

      if (!loaded && auditBackupFile.exists()) {
         if (auditLoad(waypoints, auditBackupFile)) {
            AutismClientAddon.LOG.warn("Restored waypoints from {}", auditBackupFile.getName());
            if (auditBackupPrimary) {
               waypoints.load_V();
            }
         } else {
            waypoints.init_Map.clear();
         }
      }

      Runtime.getRuntime().addShutdownHook(new Thread(waypoints::read, "waypoints-flush"));
      return waypoints;
   }

   private static boolean auditLoad(AutismWaypoints waypoints, File file) {
      try (FileReader reader = new FileReader(file)) {
         Map<String, List<AutismWaypoints.Waypoint>> parsed = init_Gson.fromJson(reader, init_Type);
         if (parsed != null) {
            for (Entry<String, List<AutismWaypoints.Waypoint>> entry : parsed.entrySet()) {
               if (entry.getKey() != null && !entry.getKey().isBlank() && entry.getValue() != null) {
                  List<AutismWaypoints.Waypoint> list = new ArrayList<>();

                  for (AutismWaypoints.Waypoint waypoint : entry.getValue()) {
                     if (waypoint != null && waypoint.name() != null && !waypoint.name().isBlank()) {
                        if (list.size() >= 512) {
                           break;
                        }

                        list.add(waypoint);
                     }
                  }

                  if (!list.isEmpty()) {
                     waypoints.init_Map.put(entry.getKey().trim(), list);
                  }
               }
            }
         }

         return parsed != null;
      } catch (Throwable t) {
         AutismClientAddon.LOG.warn("Failed to read waypoints from {}", file.getName(), t);
         return false;
      }
   }

   private void load_V() {
      this.init_AtomicBoolean.set(true);
      if (this.load_AtomicBoolean.compareAndSet(false, true)) {
         try {
            init_ScheduledExecutorS.schedule(() -> {
               this.load_AtomicBoolean.set(false);
               this.read();
            }, 400L, TimeUnit.MILLISECONDS);
         } catch (Throwable t) {
            this.load_AtomicBoolean.set(false);
            this.read();
         }
      }
   }

   private void read() {
      if (this.init_AtomicBoolean.compareAndSet(true, false)) {
         this.write();
      }
   }

   private synchronized void write() {
      try {
         File dir = init_File.getParentFile();
         if (dir != null) {
            dir.mkdirs();
         }

         Map<String, List<AutismWaypoints.Waypoint>> snapshot = new LinkedHashMap<>();
         synchronized (this.init_Map) {
            for (Entry<String, List<AutismWaypoints.Waypoint>> entry : this.init_Map.entrySet()) {
               snapshot.put(entry.getKey(), List.copyOf(entry.getValue()));
            }
         }

         try (FileWriter out = new FileWriter(load_File)) {
            init_Gson.toJson(snapshot, init_Type, out);
         }

         try {
            if (auditBackupPrimary && init_File.exists()) {
               File bakTmp = new File(auditBackupFile.getParentFile(), auditBackupFile.getName() + ".tmp");
               try {
                  Files.copy(init_File.toPath(), bakTmp.toPath(), StandardCopyOption.REPLACE_EXISTING);
                  try {
                     Files.move(bakTmp.toPath(), auditBackupFile.toPath(), StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
                  } catch (AtomicMoveNotSupportedException notAtomic) {
                     Files.move(bakTmp.toPath(), auditBackupFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
                  }
               } catch (Throwable copyErr) {
                  try {
                     Files.deleteIfExists(bakTmp.toPath());
                  } catch (Throwable ignored) {
                  }
                  throw copyErr;
               }
            }
         } catch (Throwable t) {
            AutismClientAddon.LOG.warn("Failed to back up waypoints before save", t);
         }

         try {
            Files.move(load_File.toPath(), init_File.toPath(), StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
         } catch (AtomicMoveNotSupportedException notAtomic) {
            Files.move(load_File.toPath(), init_File.toPath(), StandardCopyOption.REPLACE_EXISTING);
         }

         auditBackupPrimary = true;
         this.auditSaveFailures = 0;
      } catch (Throwable t) {
         this.init_AtomicBoolean.set(true);
         AutismClientAddon.LOG.warn("Failed to save waypoints", t);
         long retryMs = Math.min(60000L, 1000L << Math.min(this.auditSaveFailures++, 6));
         if (!this.auditRetryPending) {
            this.auditRetryPending = true;
            try {
               init_ScheduledExecutorS.schedule(() -> {
                  synchronized (this) {
                     this.auditRetryPending = false;
                  }

                  this.read();
               }, retryMs, TimeUnit.MILLISECONDS);
            } catch (Throwable rejected) {
               this.auditRetryPending = false;
            }
         }
      }
   }

   public record Waypoint(String name, int x, int y, int z, int color, long createdMs, boolean death) {
      public Waypoint(String name, int x, int y, int z, int color, long createdMs) {
         this(name, x, y, z, color, createdMs, false);
      }
   }
}

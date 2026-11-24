package autismclient.ai;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HexFormat;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CancellationException;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.ThreadPoolExecutor.AbortPolicy;
import java.util.function.Consumer;
import java.util.stream.Stream;

final class cR {
   private static volatile cR init_cR;
   static final ExecutorService init_ExecutorService = new ThreadPoolExecutor(2, 2, 0L, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<>(32), r -> {
      Thread t = new Thread(r, "Schematic files");
      t.setDaemon(true);
      return t;
   }, new AbortPolicy());
   final Path init_Path;
   final Path load_Path;
   final Path read;
   volatile List<Path> init_List = List.of();
   volatile List<cR.Inner_DataUtil> load_List = List.of();
   volatile String init_Str = "";
   volatile int init_I;
   private Future<?> init_Future;
   private long init_J;
   private final Map<String, cR.Inner_DataUtil> init_Map = new HashMap<>();
   private final Gson init_Gson = new GsonBuilder().setPrettyPrinting().create();

   cR(Path game) {
      this.init_Path = game;
      this.load_Path = game.resolve("autismclient/schematics").toAbsolutePath().normalize();
      this.read = game.resolve("config/autism/schematic-library.json");
      this.init_List = List.of(this.load_Path);
      init_cR = this;
   }

   static Path init_Path_Str(String token) {
      if (init_cR != null && token.matches("sc_[0-9a-f]{24}")) {
         for (cR.Inner_DataUtil entry : init_cR.load_List) {
            if (token.equals("sc_" + entry.id) && init_cR.init_Z_Path(entry.file)) {
               return entry.file;
            }
         }

         throw new IllegalArgumentException("The schematic is no longer in a configured folder; refresh the library");
      } else {
         return null;
      }
   }

   cR.Inner_CachePool init() throws IOException {
      Files.createDirectories(this.load_Path);
      LinkedHashSet<String> roots = new LinkedHashSet<>();
      roots.add(this.load_Path.toString());
      ArrayList<cR.Inner_StreamUtil> placed = new ArrayList<>();
      Path legacy = this.init_Path.resolve("schematics").toAbsolutePath().normalize();
      if (!Files.exists(this.read) && Files.isDirectory(legacy)) {
         roots.add(legacy.toString());
      }

      if (Files.isRegularFile(this.read) && Files.size(this.read) < 4194304L) {
         JsonObject root = JsonParser.parseString(Files.readString(this.read)).getAsJsonObject();
         if (root.has("folders")) {
            for (JsonElement v : root.getAsJsonArray("folders")) {
               try {
                  if (roots.size() < 32) {
                     roots.add(Path.of(v.getAsString()).toAbsolutePath().normalize().toString());
                  }
               } catch (RuntimeException var9) {
               }
            }
         }

         if (root.has("placements")) {
            for (JsonElement value : root.getAsJsonArray("placements")) {
               if (placed.size() >= 256) {
                  break;
               }

               try {
                  JsonObject v = value.getAsJsonObject();
                  String mirror = init_Str_JsonObject(v, "mirror", "none");
                  if (!Set.of("none", "x", "z").contains(mirror)) {
                     mirror = "none";
                  }

                  placed.add(
                     new cR.Inner_StreamUtil(
                        init_Str_JsonObject(v, "id", UUID.randomUUID().toString()),
                        init_Str_JsonObject(v, "scope", ""),
                        init_Str_JsonObject(v, "dimension", ""),
                        init_Str_JsonObject(v, "file", ""),
                        init_J_JsonObject(v, "bytes", 0L),
                        init_J_JsonObject(v, "modified", 0L),
                        (int)init_J_JsonObject(v, "x", 0L),
                        (int)init_J_JsonObject(v, "y", 0L),
                        (int)init_J_JsonObject(v, "z", 0L),
                        Math.floorMod((int)init_J_JsonObject(v, "rotation", 0L), 360) / 90 * 90,
                        mirror,
                        !v.has("visible") || v.get("visible").getAsBoolean(),
                        init_Str_JsonObject(v, "name", ""),
                        v.has("paste") && v.get("paste").isJsonObject() ? v.getAsJsonObject("paste").deepCopy() : new JsonObject()
                     )
                  );
               } catch (RuntimeException var10) {
               }
            }
         }
      }

      return new cR.Inner_CachePool(List.copyOf(roots), List.copyOf(placed));
   }

   void init_V_cR_CachePool(cR.Inner_CachePool state) {
      this.init_List = state.folders.stream().map(x$0 -> Path.of(x$0)).toList();
   }

   void load_V_cR_CachePool(cR.Inner_CachePool state) throws IOException {
      JsonObject root = new JsonObject();
      root.addProperty("version", 1);
      JsonArray folders = new JsonArray();
      state.folders.forEach(folders::add);
      root.add("folders", folders);
      JsonArray placements = new JsonArray();

      for (cR.Inner_StreamUtil p : state.placements) {
         JsonObject row = new JsonObject();
         row.addProperty("id", p.id);
         row.addProperty("scope", p.scope);
         row.addProperty("dimension", p.dimension);
         row.addProperty("file", p.file);
         row.addProperty("bytes", p.bytes);
         row.addProperty("modified", p.modified);
         row.addProperty("x", p.x);
         row.addProperty("y", p.y);
         row.addProperty("z", p.z);
         row.addProperty("rotation", p.rotation);
         row.addProperty("mirror", p.mirror);
         row.addProperty("visible", p.visible);
         if (!p.name.isEmpty()) {
            row.addProperty("name", p.name);
         }

         if (!p.paste.isEmpty()) {
            row.add("paste", p.paste);
         }

         placements.add(row);
      }

      root.add("placements", placements);
      Files.createDirectories(this.read.getParent());
      Path temporary = Files.createTempFile(this.read.getParent(), "schematic-library-", ".tmp");

      try {
         Files.writeString(temporary, this.init_Gson.toJson(root), StandardCharsets.UTF_8);
         init_V_Path_Path(temporary, this.read);
      } finally {
         Files.deleteIfExists(temporary);
      }
   }

   synchronized void init_V_Consumer(Consumer<Runnable> client) {
      long token = ++this.init_J;
      if (this.init_Future != null) {
         this.init_Future.cancel(true);
      }

      List<Path> roots = List.copyOf(this.init_List);
      this.init_Str = "Finding files";
      this.init_I = 0;
      Map<String, cR.Inner_DataUtil> cached = new HashMap<>(this.init_Map);
      this.init_Future = init_ExecutorService.submit(
         () -> {
            LinkedHashMap<Path, cR.Inner_DataUtil> found = new LinkedHashMap<>();

            try {
               for (Path folder : roots) {
                  cQ.init();
                  if (Files.isDirectory(folder)) {
                     Path base = folder.toRealPath();

                     try (Stream<Path> paths = Files.walk(base, 6)) {
                        Iterator<Path> it = paths.iterator();

                        while (it.hasNext()) {
                           cQ.init();
                           Path file = it.next();
                           if (load_Z_Path(file) && Files.isRegularFile(file)) {
                              Path real = file.toRealPath();
                              if (real.startsWith(base)) {
                                 BasicFileAttributes a = Files.readAttributes(real, BasicFileAttributes.class);
                                 cR.Inner_DataUtil entry = new cR.Inner_DataUtil(
                                    init_Str_Path(real),
                                    real,
                                    folder.getFileName() == null ? folder.toString() : folder.getFileName().toString(),
                                    file.getFileName().toString(),
                                    a.size(),
                                    a.lastModifiedTime().toMillis(),
                                    a.creationTime().toMillis(),
                                    -1L,
                                    ""
                                 );
                                 cR.Inner_DataUtil old = cached.get(entry.id);
                                 if (old != null && old.bytes == entry.bytes && old.modified == entry.modified && old.added == entry.added) {
                                    entry = old;
                                 }

                                 found.putIfAbsent(real, entry);
                                 if (found.size() >= 4096) {
                                    break;
                                 }
                              }
                           }
                        }
                     }
                  }
               }

               ArrayList<cR.Inner_DataUtil> rows = new ArrayList<>(found.values());
               rows.sort(Comparator.comparing(cR.Inner_DataUtil::name, String.CASE_INSENSITIVE_ORDER).thenComparing(ex -> ex.file.toString()));
               this.init(client, token, rows, "Reading block counts", 0);

               for (int i = 0; i < rows.size(); i++) {
                  cQ.init();
                  cR.Inner_DataUtil entry = rows.get(i);
                  if (entry.blocks < 0L && entry.error.isEmpty()) {
                     try {
                        cQ d = cQ.init_cQ_Path(entry.file);
                        long count = d.init_J_IntConsumer(p -> {});
                        BasicFileAttributes after = Files.readAttributes(entry.file, BasicFileAttributes.class);
                        if (after.size() != entry.bytes || after.lastModifiedTime().toMillis() != entry.modified) {
                           throw new IOException("File changed; refresh the library");
                        }

                        entry = entry.metadata(count, "");
                     } catch (CancellationException e) {
                        throw e;
                     } catch (Exception e) {
                        entry = entry.metadata(-1L, init_Str_Throwable(e));
                     }
                  }

                  rows.set(i, entry);
                  if (i < 5 || (i & 15) == 15) {
                     this.init(client, token, rows, "Reading block counts", (i + 1) * 100 / Math.max(1, rows.size()));
                  }
               }

               this.init(client, token, rows, "", 100);
            } catch (CancellationException var21) {
            } catch (Exception failure) {
               this.init(client, token, List.copyOf(found.values()), init_Str_Throwable(failure), 0);
            }
         }
      );
   }

   private void init(Consumer<Runnable> client, long token, List<cR.Inner_DataUtil> rows, String status, int percent) {
      List<cR.Inner_DataUtil> snapshot = List.copyOf(rows);
      client.accept(() -> {
         synchronized (this) {
            if (token == this.init_J) {
               this.load_List = snapshot;
               if (status.isEmpty()) {
                  this.init_Map.clear();

                  for (cR.Inner_DataUtil e : snapshot) {
                     this.init_Map.put(e.id, e);
                  }
               }

               this.init_Str = status;
               this.init_I = percent;
            }
         }
      });
   }

   boolean init_Z_Path(Path file) {
      Path p = file.toAbsolutePath().normalize();

      for (Path root : this.init_List) {
         if (p.startsWith(root.toAbsolutePath().normalize())) {
            return true;
         }
      }

      return false;
   }

   cR.Inner_DataUtil init_cR_DataUtil_cR_DataUtil_Str(cR.Inner_DataUtil entry, String name) throws IOException {
      Path source = entry.file();
      if (this.init_Z_Path(source) && source.toRealPath().equals(source)) {
         BasicFileAttributes attributes = Files.readAttributes(source, BasicFileAttributes.class);
         if (attributes.isRegularFile() && attributes.size() == entry.bytes() && attributes.lastModifiedTime().toMillis() == entry.modified()) {
            String sourceName = source.getFileName().toString();
            String extension = sourceName.substring(sourceName.lastIndexOf(46));
            String base = name.strip();
            if (base.toLowerCase(Locale.ROOT).endsWith(extension.toLowerCase(Locale.ROOT))) {
               base = base.substring(0, base.length() - extension.length()).strip();
            }

            if (!base.isEmpty() && !base.endsWith(".") && !base.chars().anyMatch(c -> c < 32 || "<>:\"/\\|?*".indexOf(c) >= 0)) {
               Path target = source.resolveSibling(base + extension);
               if (sourceName.equals(target.getFileName().toString())) {
                  return entry;
               }

               if (source.equals(target)) {
                  Path interim = Files.createTempFile(source.getParent(), ".schematic-rename-", ".tmp");
                  Files.delete(interim);
                  Files.move(source, interim);

                  try {
                     Files.move(interim, target);
                  } catch (IOException failure) {
                     try {
                        Files.move(interim, source);
                     } catch (IOException rollback) {
                        failure.addSuppressed(rollback);
                     }

                     throw failure;
                  }
               } else {
                  try {
                     Files.move(source, target);
                  } catch (FileAlreadyExistsException exists) {
                     throw new IOException("That file name is already in use", exists);
                  }
               }

               return new cR.Inner_DataUtil(
                  init_Str_Path(target),
                  target,
                  entry.folder(),
                  target.getFileName().toString(),
                  entry.bytes(),
                  entry.modified(),
                  entry.added(),
                  entry.blocks(),
                  entry.error()
               );
            } else {
               throw new IOException("Enter a valid file name");
            }
         } else {
            throw new IOException("Schematic changed. Refresh the library and try again");
         }
      } else {
         throw new IOException("Schematic is no longer in a configured folder");
      }
   }

   synchronized void init_V_cR_DataUtil(cR.Inner_DataUtil old, cR.Inner_DataUtil updated, Consumer<Runnable> client) {
      this.load_List = this.load_List.stream().map(row -> row.id().equals(old.id()) ? updated : row).toList();
      this.init_Map.remove(old.id());
      this.init_Map.put(updated.id(), updated);
      this.init_V_Consumer(client);
   }

   static boolean load_Z_Path(Path p) {
      String n = p.getFileName().toString().toLowerCase(Locale.ROOT);
      return n.endsWith(".schem") || n.endsWith(".litematic") || n.endsWith(".schematic");
   }

   static String init_Str_Path(Path path) {
      try {
         return HexFormat.of()
            .formatHex(MessageDigest.getInstance("SHA-256").digest(path.toAbsolutePath().normalize().toString().getBytes(StandardCharsets.UTF_8)))
            .substring(0, 24);
      } catch (Exception e) {
         throw new IllegalStateException(e);
      }
   }

   static String init_Str_Throwable(Throwable error) {
      Throwable e = error;

      while (e instanceof CompletionException && e.getCause() != null) {
         e = e.getCause();
      }

      String s = e.getMessage();
      return s != null && !s.isBlank() ? s : e.getClass().getSimpleName();
   }

   static String init_Str_JsonObject(JsonObject o, String key, String fallback) {
      return o.has(key) && !o.get(key).isJsonNull() ? o.get(key).getAsString() : fallback;
   }

   static long init_J_JsonObject(JsonObject o, String key, long fallback) {
      return o.has(key) ? o.get(key).getAsLong() : fallback;
   }

   static void init_V_Path_Path(Path source, Path target) throws IOException {
      try {
         Files.move(source, target, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);
      } catch (AtomicMoveNotSupportedException e) {
         Files.move(source, target, StandardCopyOption.REPLACE_EXISTING);
      }
   }

   record Inner_CachePool(List<String> folders, List<cR.Inner_StreamUtil> placements) {
   }

   record Inner_DataUtil(String id, Path file, String folder, String name, long bytes, long modified, long added, long blocks, String error) {
      cR.Inner_DataUtil metadata(long count, String problem) {
         return new cR.Inner_DataUtil(this.id, this.file, this.folder, this.name, this.bytes, this.modified, this.added, count, problem);
      }
   }

   record Inner_StreamUtil(
      String id,
      String scope,
      String dimension,
      String file,
      long bytes,
      long modified,
      int x,
      int y,
      int z,
      int rotation,
      String mirror,
      boolean visible,
      String name,
      JsonObject paste
   ) {
      cR.Inner_StreamUtil named(String value) {
         return new cR.Inner_StreamUtil(
            this.id,
            this.scope,
            this.dimension,
            this.file,
            this.bytes,
            this.modified,
            this.x,
            this.y,
            this.z,
            this.rotation,
            this.mirror,
            this.visible,
            value,
            this.paste
         );
      }

      cR.Inner_StreamUtil movedTo(cR.Inner_DataUtil entry) {
         return new cR.Inner_StreamUtil(
            this.id,
            this.scope,
            this.dimension,
            entry.file().toString(),
            this.bytes,
            this.modified,
            this.x,
            this.y,
            this.z,
            this.rotation,
            this.mirror,
            this.visible,
            this.name,
            this.paste
         );
      }

      cR.Inner_StreamUtil at(int x, int y, int z, int rotation, String mirror) {
         return new cR.Inner_StreamUtil(
            this.id, this.scope, this.dimension, this.file, this.bytes, this.modified, x, y, z, rotation, mirror, this.visible, this.name, this.paste
         );
      }

      cR.Inner_StreamUtil shown(boolean value) {
         return new cR.Inner_StreamUtil(
            this.id,
            this.scope,
            this.dimension,
            this.file,
            this.bytes,
            this.modified,
            this.x,
            this.y,
            this.z,
            this.rotation,
            this.mirror,
            value,
            this.name,
            this.paste
         );
      }

      cR.Inner_StreamUtil checkpoint(JsonObject value) {
         return new cR.Inner_StreamUtil(
            this.id,
            this.scope,
            this.dimension,
            this.file,
            this.bytes,
            this.modified,
            this.x,
            this.y,
            this.z,
            this.rotation,
            this.mirror,
            this.visible,
            this.name,
            value
         );
      }
   }
}

package autismclient.ai;

import autismclient.AutismClientAddon;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.Block;

final class NodeWalker {
   private static final int init_I = 32;
   private static final Map<String, List<TableIndex.Inner_StreamUtil>> init_Map = new LinkedHashMap<>();

   private NodeWalker() {
   }

   static Path init() {
      return AutismClientAddon.FOLDER.toPath().resolve("ai").resolve("clipboard");
   }

   static String init_Str(String name) {
      String clean = name == null ? "" : name.trim();
      if (clean.isEmpty() || clean.length() > 128) {
         throw new IllegalArgumentException("clipboard name must be 1..128 characters");
      } else if (!clean.contains("..") && !clean.contains("/") && !clean.contains("\\") && !clean.contains(":")) {
         return clean;
      } else {
         throw new IllegalArgumentException("clipboard name must not contain path separators or '..': " + name);
      }
   }

   static synchronized void init(String name, List<TableIndex.Inner_StreamUtil> cells) {
      init(name, cells, init());
   }

   static synchronized void init(String name, List<TableIndex.Inner_StreamUtil> cells, Path directory) {
      init_Map.remove(name);
      init_Map.put(name, cells);

      while (init_Map.size() > 32) {
         init_Map.remove(init_Map.keySet().iterator().next());
      }

      try {
         ag.init_V_Path_Obj(directory.resolve(name + ".json"), init(cells));
      } catch (IOException e) {
         throw new IllegalStateException("Could not persist clipboard \"" + name + "\": " + e.getMessage(), e);
      }
   }

   static synchronized List<TableIndex.Inner_StreamUtil> init_List(String name) {
      return init(name, init());
   }

   static synchronized List<TableIndex.Inner_StreamUtil> init(String name, Path directory) {
      List<TableIndex.Inner_StreamUtil> cells = init_Map.get(name);
      if (cells != null) {
         return cells;
      }

      Path file = directory.resolve(name + ".json");
      if (!Files.isRegularFile(file)) {
         throw new IllegalArgumentException("No clipboard named \"" + name + "\"; game:copy first (stored: " + init(directory) + ")");
      }

      try {
         if (Files.size(file) > 8000000L) {
            throw new IllegalArgumentException("Clipboard \"" + name + "\" exceeds the supported size");
         }

         cells = init(ag.init_JsonObject_Str(Files.readString(file)));
      } catch (IllegalArgumentException e) {
         throw e;
      } catch (Exception e) {
         throw new IllegalArgumentException("Clipboard \"" + name + "\" could not be read: " + e.getMessage());
      }

      init_Map.put(name, cells);
      return cells;
   }

   static synchronized String init(Path directory) {
      List<String> names = new ArrayList<>(init_Map.keySet());
      if (Files.isDirectory(directory)) {
         try (Stream<Path> stream = Files.list(directory)) {
            stream.filter(p -> p.getFileName().toString().endsWith(".json"))
               .map(p -> p.getFileName().toString().replaceAll("\\.json$", ""))
               .filter(n -> !names.contains(n))
               .sorted()
               .forEach(names::add);
         } catch (IOException var7) {
         }
      }

      return names.isEmpty() ? "(none)" : String.join(", ", names);
   }

   static JsonObject init(List<TableIndex.Inner_StreamUtil> cells) {
      JsonArray array = new JsonArray();

      for (TableIndex.Inner_StreamUtil cell : cells) {
         JsonObject entry = ag.init_JsonObject_ObjArr("dx", cell.dx(), "dy", cell.dy(), "dz", cell.dz(), "block", cell.block());
         if (!cell.properties().isEmpty()) {
            JsonObject properties = new JsonObject();
            cell.properties().forEach(properties::addProperty);
            entry.add("properties", properties);
         }

         array.add(entry);
      }

      return ag.init_JsonObject_ObjArr("version", 1, "cells", array);
   }

   static List<TableIndex.Inner_StreamUtil> init(JsonObject root) {
      if (ag.init_I_JsonObject(root, "version", 0) != 1) {
         throw new IllegalArgumentException("Unsupported clipboard version");
      }

      if (root.has("cells") && root.get("cells").isJsonArray()) {
         JsonArray array = root.getAsJsonArray("cells");
         if (array.size() > 4096) {
            throw new IllegalArgumentException("Clipboard has " + array.size() + " cells; the limit is 4096");
         }

         List<TableIndex.Inner_StreamUtil> cells = new ArrayList<>(array.size());

         for (int i = 0; i < array.size(); i++) {
            JsonObject entry = array.get(i).getAsJsonObject();
            String context = "cells[" + i + "]";
            int dx = ag.init_I_JsonObject(entry, "dx", 0);
            int dy = ag.init_I_JsonObject(entry, "dy", 0);
            int dz = ag.init_I_JsonObject(entry, "dz", 0);
            if (Math.abs(dx) > 256 || Math.abs(dy) > 256 || Math.abs(dz) > 256) {
               throw new IllegalArgumentException(context + " offsets must be within +/-256");
            }

            String block = ag.init_Str_JsonObject(entry, "block", "");
            Identifier id = Identifier.tryParse(block);
            Block blockObj = id == null ? null : BuiltInRegistries.BLOCK.getOptional(id).orElse(null);
            if (blockObj == null) {
               throw new IllegalArgumentException(context + " names an unknown block id: " + block);
            }

            cells.add(new TableIndex.Inner_StreamUtil(dx, dy, dz, block, TableIndex.init_Map_JsonObject(entry, context, blockObj)));
         }

         return List.copyOf(cells);
      } else {
         throw new IllegalArgumentException("Clipboard has no cells array");
      }
   }
}

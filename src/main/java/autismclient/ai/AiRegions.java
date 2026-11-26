package autismclient.ai;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

final class AiRegions {
   private final Map<String, AiRegions.Region> init_Map = new LinkedHashMap<>();
   private volatile List<AiRegions.Region> init_List = List.of();
   private long init_J;
   private long load = -1L;

   void init(Path directory) throws IOException {
      Path file = directory.resolve("regions.json");
      if (Files.exists(file)) {
         try {
            if (Files.size(file) > 8000000L) {
               throw new IllegalArgumentException("Region map too large");
            }

            JsonObject root = JsonParser.parseString(Files.readString(file)).getAsJsonObject();
            if (AiJson.getInt(root, "version", 0) != 1) {
               throw new IllegalArgumentException("Unsupported region map version");
            }

            for (JsonElement raw : root.getAsJsonArray("regions")) {
               AiRegions.Region region = AiJson.GSON.fromJson(raw, AiRegions.Region.class);
               if (region.scope() == null || region.evidenceId() == null || region.session() == null || region.reachedFrom() == null) {
                  throw new IllegalArgumentException("Incomplete region observation");
               }

               this.init_Map.put(region.key(), region);
            }

            if (this.init_Map.size() > 8000) {
               throw new IllegalArgumentException("Too many region observations");
            }

            this.init_List = List.copyOf(this.init_Map.values());
         } catch (RuntimeException e) {
            Files.copy(file, directory.resolve("regions-corrupt-" + System.currentTimeMillis() + ".json"));
            this.init_Map.clear();
            this.init_List = List.of();
         }
      }
   }

   synchronized void init(AiRegions.Region region) {
      AiRegions.Region existing = this.init_Map.get(region.key());
      if (existing != null) {
         LinkedHashSet<String> merged = new LinkedHashSet<>(existing.reachedFrom());
         merged.addAll(region.reachedFrom());

         while (merged.size() > 16) {
            merged.remove(merged.iterator().next());
         }

         region = new AiRegions.Region(
            region.scope(),
            region.x(),
            region.y(),
            region.z(),
            region.at(),
            region.session(),
            region.evidenceId(),
            region.beds(),
            region.bells(),
            region.paths(),
            region.farmland(),
            region.villagers(),
            List.copyOf(merged)
         );
      }

      this.init_Map.put(region.key(), region);

      while (this.init_Map.size() > 8000) {
         this.init_Map.remove(this.init_Map.keySet().iterator().next());
      }

      this.init_List = List.copyOf(this.init_Map.values());
      this.init_J++;
   }

   List<AiRegions.Region> init(String scope) {
      return this.init_List.stream().filter(r -> r.scope().equals(scope)).toList();
   }

   void load(Path directory) throws IOException {
      long captured;
      List<AiRegions.Region> snapshot;
      synchronized (this) {
         if (this.load == this.init_J) {
            return;
         }

         captured = this.init_J;
         snapshot = this.init_List;
      }

      AiJson.writeAtomicJson(directory.resolve("regions.json"), AiJson.objectOf("version", 1, "regions", snapshot));
      synchronized (this) {
         this.load = captured;
      }
   }

   static String init(int x, int z) {
      return Math.floorDiv(x, 32) + "," + Math.floorDiv(z, 32);
   }

   static List<AiRegions.Region> init(List<AiRegions.Region> observations, int x, int z, int goalX, int goalZ) {
      if (observations.size() < 2) {
         return List.of();
      }

      Map<String, AiRegions.Region> cells = new HashMap<>();
      Map<String, Set<String>> edges = new HashMap<>();

      for (AiRegions.Region region : observations) {
         cells.put(region.cell(), region);
      }

      for (AiRegions.Region region : observations) {
         for (String from : region.reachedFrom()) {
            if (cells.containsKey(from)) {
               edges.computeIfAbsent(from, k -> new HashSet<>()).add(region.cell());
               edges.computeIfAbsent(region.cell(), k -> new HashSet<>()).add(from);
            }
         }
      }

      AiRegions.Region start = observations.stream().min(Comparator.comparingDouble(r -> Math.hypot(r.x() - x, r.z() - z))).orElseThrow();
      if (Math.hypot(start.x() - x, start.z() - z) > 64.0) {
         return List.of();
      }

      ArrayDeque<String> queue = new ArrayDeque<>();
      Map<String, String> parent = new HashMap<>();
      queue.add(start.cell());
      parent.put(start.cell(), "");
      String nearest = start.cell();

      while (!queue.isEmpty()) {
         String key = queue.removeFirst();
         AiRegions.Region r = cells.get(key);
         AiRegions.Region best = cells.get(nearest);
         if (Math.hypot(r.x() - goalX, r.z() - goalZ) < Math.hypot(best.x() - goalX, best.z() - goalZ)) {
            nearest = key;
         }

         for (String next : edges.getOrDefault(key, Set.of())) {
            if (!parent.containsKey(next)) {
               parent.put(next, key);
               queue.addLast(next);
            }
         }
      }

      ArrayList<AiRegions.Region> result = new ArrayList<>();

      for (String key = nearest; !key.equals(start.cell()); key = parent.get(key)) {
         result.add(cells.get(key));
      }

      Collections.reverse(result);
      return result;
   }

   static List<int[]> init(List<AiRegions.Region> observations, Set<String> visited, int originX, int originZ, int maxDistance) {
      Set<String> known = new HashSet<>(visited);

      for (AiRegions.Region region : observations) {
         known.add(region.cell());
      }

      Map<String, int[]> candidates = new LinkedHashMap<>();

      for (AiRegions.Region region : observations) {
         for (int[] delta : List.of(new int[]{32, 0}, new int[]{-32, 0}, new int[]{0, 32}, new int[]{0, -32})) {
            int x = Math.floorDiv(region.x(), 32) * 32 + 16 + delta[0];
            int z = Math.floorDiv(region.z(), 32) * 32 + 16 + delta[1];
            String key = init(x, z);
            if (!known.contains(key) && Math.hypot(x - originX, z - originZ) <= maxDistance) {
               candidates.putIfAbsent(key, new int[]{x, region.y(), z});
            }
         }
      }

      return List.copyOf(candidates.values());
   }

   record Region(
      String scope,
      int x,
      int y,
      int z,
      long at,
      String session,
      String evidenceId,
      int beds,
      int bells,
      int paths,
      int farmland,
      int villagers,
      List<String> reachedFrom
   ) {
      String cell() {
         return Math.floorDiv(this.x, 32) + "," + Math.floorDiv(this.z, 32);
      }

      String key() {
         return this.scope + "/" + this.cell();
      }
   }
}

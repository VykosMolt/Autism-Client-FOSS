package autismclient.ai;

import com.google.gson.JsonObject;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

final class al {
   private static final Set<String> init = Set.of(
      "game:obtain",
      "game:mine",
      "game:chop",
      "game:craft",
      "game:smelt_auto",
      "game:goto",
      "game:fight",
      "game:farm",
      "game:build",
      "game:eat",
      "game:sleep",
      "game:fish",
      "game:tidy",
      "game:surface",
      "game:recover",
      "game:speedrun"
   );

   static JsonObject init(List<JsonObject> targets, Integer amount) {
      if (targets.isEmpty()) {
         throw new IllegalArgumentException("Choose at least one block to mine");
      }

      String types = targets.stream().map(args -> ag.init_Str_JsonObject(args, "block", "")).collect(Collectors.joining(","));
      int count = amount == null ? (targets.size() == 1 ? ag.init_I_JsonObject(targets.getFirst(), "count", 1) : 16) : amount;
      if (count >= 1 && count <= 640) {
         JsonObject overrides = new JsonObject();

         for (JsonObject target : targets) {
            if (target.has("mineAmount")) {
               overrides.add(ag.init_Str_JsonObject(target, "block", ""), target.get("mineAmount"));
            }
         }

         return as.init_JsonObject_JsonObject(ag.init_JsonObject_ObjArr("block", types, "count", count, "counts", overrides));
      } else {
         throw new IllegalArgumentException("Choose an amount from 1 to 640");
      }
   }

   static JsonObject init(String label, String capability, JsonObject arguments) {
      if (label != null && !label.isBlank() && label.length() <= 16000 && capability != null && init.contains(capability) && arguments != null) {
         return ag.init_JsonObject_ObjArr("label", label, "capability", capability, "arguments", arguments.deepCopy());
      } else {
         throw new IllegalArgumentException("Invalid local action");
      }
   }

   static ad.Inner_CachePool init(JsonObject stored) {
      String label = ag.init_Str_JsonObject(stored, "label", "");
      String capability = ag.init_Str_JsonObject(stored, "capability", "");
      if (stored.has("arguments") && stored.get("arguments").isJsonObject()) {
         JsonObject checked = init(label, capability, stored.getAsJsonObject("arguments"));
         return new ad.Inner_CachePool(List.of(new ad.Inner_BufferHelper(label, capability, checked.getAsJsonObject("arguments"), label)), "");
      } else {
         throw new IllegalArgumentException("Local action has no arguments");
      }
   }

   static Integer init(String text, int max) {
      if (text != null && !text.isBlank()) {
         try {
            int count = Integer.parseInt(text.trim());
            if (count >= 1 && count <= max) {
               return count;
            }
         } catch (NumberFormatException var3) {
         }

         throw new IllegalArgumentException("Choose an amount from 1 to " + max);
      } else {
         return null;
      }
   }

   static JsonObject init(JsonObject arguments, int held, int limit) {
      JsonObject args = arguments.deepCopy();
      int requested = ag.init_I_JsonObject(args, "count", 1);
      long total = (long)held + requested;
      if (held >= 0 && requested >= 1 && total <= limit) {
         args.addProperty("count", total);
         args.addProperty("additional", false);
         return args;
      } else {
         throw new IllegalArgumentException("Inventory target exceeds " + limit + ". Choose a smaller amount.");
      }
   }

   static boolean init(JsonObject stored, String world, String dimension) {
      return !stored.has("placementWorld")
         || ag.init_Str_JsonObject(stored, "placementWorld", "").equals(world) && ag.init_Str_JsonObject(stored, "placementDimension", "").equals(dimension);
   }

   static void init_V(JsonObject stored, Path file) throws IOException {
      stored.add("sourceRevision", ag.init_JsonObject_ObjArr("size", Files.size(file), "modified", Files.getLastModifiedTime(file).toMillis()));
   }

   static boolean init_Z(JsonObject stored, Path file) {
      if (!stored.has("sourceRevision")) {
         return true;
      }

      try {
         JsonObject revision = stored.getAsJsonObject("sourceRevision");
         return Files.size(file) == revision.get("size").getAsLong() && Files.getLastModifiedTime(file).toMillis() == revision.get("modified").getAsLong();
      } catch (Exception e) {
         return false;
      }
   }
}

package autismclient.ai;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;

final class AiProcedures {
   private final AiService service;
   private final Map<String, JsonObject> init_Map = new LinkedHashMap<>();

   AiProcedures(AiService service) {
      this.service = service;
   }

   synchronized void remember(String id, String fingerprint, JsonObject args, JsonObject result) {
      if (AiJson.getString(result, "status", "").equals("SUCCEEDED") && result.has("evidence")) {
         String proof = AiJson.getString(result.getAsJsonObject("evidence"), "id", "");
         args = args.deepCopy();
         if (id.equals("client:run_macro") && result.has("data")) {
            args.addProperty("expectedMacroFingerprint", AiJson.getString(result.getAsJsonObject("data"), "macroFingerprint", ""));
         }

         this.init_Map
            .put(
               proof,
               AiJson.objectOf(
                  "id",
                  id,
                  "fingerprint",
                  fingerprint,
                  "arguments",
                  args.deepCopy(),
                  "evidenceId",
                  proof,
                  "scope",
                  this.service.getDimensionId(),
                  "recordedResult",
                  result.get("data")
               )
            );

         while (this.init_Map.size() > 512) {
            this.init_Map.remove(this.init_Map.keySet().iterator().next());
         }
      }
   }

   synchronized JsonObject save(JsonObject args) throws IOException {
      String name = AiJson.getString(args, "name", "");
      if (!name.isBlank() && name.length() <= 256) {
         JsonArray ids = args.getAsJsonArray("evidenceIds");
         if (ids != null && !ids.isEmpty() && ids.size() <= 32) {
            JsonArray steps = new JsonArray();
            String first = "";

            for (JsonElement id : ids) {
               JsonObject step = this.init_Map.get(id.getAsString());
               if (step == null || !AiJson.getString(step, "scope", "").equals(this.service.getDimensionId())) {
                  throw new IllegalArgumentException("Procedure step lacks a recorded successful operation in this world");
               }

               init(step, this.service.getCatalogSnapshot());
               steps.add(step.deepCopy());
               if (first.isEmpty()) {
                  first = id.getAsString();
               }
            }

            JsonObject payload = AiJson.objectOf(
               "version",
               1,
               "kind",
               "verified-operations",
               "steps",
               steps,
               "reusePolicy",
               "Fresh world observation and current contract validation required; success is reverified on every replay"
            );
            AiMemoryStore.Memory record = this.service
               .getMemoryStore()
               .init(
                  AiMemoryStore.MemoryKind.PROCEDURE,
                  this.service.getDimensionId(),
                  name,
                  first,
                  this.service.getCatalogSnapshot().revision(),
                  payload.toString()
               );
            return AiJson.objectOf("saved", true, "procedure", record);
         } else {
            throw new IllegalArgumentException("Select 1..32 successful operation evidence IDs in execution order");
         }
      } else {
         throw new IllegalArgumentException("Procedure name requires 1..256 characters");
      }
   }

   JsonObject describe(String id) {
      AiMemoryStore.Memory record = this.init_AiMemoryStore_Memo_Str(id);
      JsonObject payload = AiJson.parseObject(record.payload());
      JsonArray reasons = new JsonArray();

      try {
         for (JsonElement step : payload.getAsJsonArray("steps")) {
            init(step.getAsJsonObject(), this.service.getCatalogSnapshot());
         }
      } catch (Exception e) {
         reasons.add(e.getMessage());
      }

      return AiJson.objectOf(
         "procedure",
         record,
         "currentContracts",
         reasons.isEmpty(),
         "unavailableReasons",
         reasons,
         "freshObservationRequired",
         true,
         "previousSuccessProvesFutureSuccess",
         false
      );
   }

   JsonObject run(String id, long generation, AiTools tools) throws Exception {
      AiMemoryStore.Memory record = this.init_AiMemoryStore_Memo_Str(id);
      JsonObject payload = AiJson.parseObject(record.payload());
      if (AiJson.getInt(payload, "version", 0) == 1 && AiJson.getString(payload, "kind", "").equals("verified-operations")) {
         for (JsonElement step : payload.getAsJsonArray("steps")) {
            init(step.getAsJsonObject(), this.service.getCatalogSnapshot());
         }

         this.service.observe(generation, "procedure-revalidation");
         JsonArray results = new JsonArray();
         Path checkpoint = this.service
            .getMemoryStore()
            .directory()
            .resolve("procedure-runs")
            .resolve(AiJson.sha256Hex(this.service.read_Str() + this.service.getDimensionId() + id) + ".json");
         JsonArray steps = payload.getAsJsonArray("steps");
         String fingerprint = AiJson.sha256Hex(record.payload());
         AiProcedures.ResumePoint resume = init_bw_DataUtil_Path(checkpoint, fingerprint, steps.size());

         for (int index = resume.nextStep(); index < steps.size(); index++) {
            JsonElement raw = steps.get(index);
            AiJson.writeAtomicJson(checkpoint, init_JsonObject_Str(fingerprint, index, true));
            AiMemoryStore.prune(checkpoint.getParent(), 64);
            this.service.checkGeneration(generation);
            JsonObject step = raw.getAsJsonObject();
            init(step, this.service.getCatalogSnapshot());
            JsonObject result = tools.call(
               "perform",
               AiJson.objectOf("id", step.get("id"), "fingerprint", step.get("fingerprint"), "arguments", step.get("arguments")),
               generation
            );
            results.add(result);
            if (!AiJson.getString(result, "status", "").equals("SUCCEEDED")) {
               AiJson.writeAtomicJson(checkpoint, init_JsonObject_Str(fingerprint, index, false));
               return AiJson.objectOf(
                  "status",
                  "UNCERTAIN",
                  "message",
                  resume.note()
                     + "Procedure stopped at step "
                     + (index + 1)
                     + " of "
                     + steps.size()
                     + "; completed effects are not repeated automatically; re-run to resume after inspecting",
                  "results",
                  results,
                  "nextStep",
                  index
               );
            }

            AiJson.writeAtomicJson(checkpoint, init_JsonObject_Str(fingerprint, index + 1, false));
         }

         deleteQuietly(checkpoint);
         return AiJson.objectOf(
            "status",
            "SUCCEEDED",
            "message",
            resume.note() + "Recorded operation sequence completed and each operation was reverified; inspect the current objective postcondition",
            "results",
            results
         );
      } else {
         throw new IllegalArgumentException("Unsupported procedure format");
      }
   }

   static AiProcedures.ResumePoint init_bw_DataUtil_Path(Path checkpoint, String fingerprint, int stepCount) throws IOException {
      if (!Files.exists(checkpoint)) {
         return new AiProcedures.ResumePoint(0, "");
      }

      JsonObject saved;
      try {
         saved = AiJson.parseObject(Files.readString(checkpoint));
      } catch (RuntimeException e) {
         deleteQuietly(checkpoint);
         return new AiProcedures.ResumePoint(0, "Discarded an unreadable previous checkpoint; starting fresh. ");
      }

      int next = AiJson.getInt(saved, "nextStep", 0);
      if (next >= stepCount) {
         deleteQuietly(checkpoint);
         return new AiProcedures.ResumePoint(0, "");
      } else if (!AiJson.getString(saved, "procedureFingerprint", "").equals(fingerprint)) {
         throw new IllegalStateException("Procedure changed during a prior run");
      } else if (AiJson.getBoolean(saved, "inFlight", false)) {
         throw new IllegalStateException(
            "Previous procedure step has an uncertain outcome; inspect and repair that step individually before reusing the procedure"
         );
      } else {
         return new AiProcedures.ResumePoint(next, "");
      }
   }

   private static JsonObject init_JsonObject_Str(String fingerprint, int nextStep, boolean inFlight) {
      return AiJson.objectOf("version", 1, "procedureFingerprint", fingerprint, "nextStep", nextStep, "inFlight", inFlight);
   }

   private static void deleteQuietly(Path path) {
      try {
         Files.deleteIfExists(path);
      } catch (Exception var2) {
      }
   }

   private AiMemoryStore.Memory init_AiMemoryStore_Memo_Str(String id) {
      return this.service
         .getMemoryStore()
         .memories()
         .stream()
         .filter(m -> m.id().equals(id) && m.scope().equals(this.service.getDimensionId()) && m.kind() == AiMemoryStore.MemoryKind.PROCEDURE)
         .findFirst()
         .orElseThrow(() -> new IllegalArgumentException("No reusable procedure in this world scope"));
   }

   static void init(JsonObject step, AiCatalog.Snapshot catalog) {
      String id = AiJson.getString(step, "id", "");
      AiCatalog.Entry current = catalog.get(id);
      if (current == null || !current.fingerprint().equals(AiJson.getString(step, "fingerprint", ""))) {
         throw new IllegalArgumentException("Procedure dependency changed or unloaded: " + id);
      }
   }

   record ResumePoint(int nextStep, String note) {
   }
}

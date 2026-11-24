package autismclient.ai;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;

final class bw {
   private final cc init_cc;
   private final Map<String, JsonObject> init_Map = new LinkedHashMap<>();

   bw(cc service) {
      this.init_cc = service;
   }

   synchronized void init(String id, String fingerprint, JsonObject args, JsonObject result) {
      if (ag.init_Str_JsonObject(result, "status", "").equals("SUCCEEDED") && result.has("evidence")) {
         String proof = ag.init_Str_JsonObject(result.getAsJsonObject("evidence"), "id", "");
         args = args.deepCopy();
         if (id.equals("client:run_macro") && result.has("data")) {
            args.addProperty("expectedMacroFingerprint", ag.init_Str_JsonObject(result.getAsJsonObject("data"), "macroFingerprint", ""));
         }

         this.init_Map
            .put(
               proof,
               ag.init_JsonObject_ObjArr(
                  "id",
                  id,
                  "fingerprint",
                  fingerprint,
                  "arguments",
                  args.deepCopy(),
                  "evidenceId",
                  proof,
                  "scope",
                  this.init_cc.poll_Str(),
                  "recordedResult",
                  result.get("data")
               )
            );

         while (this.init_Map.size() > 512) {
            this.init_Map.remove(this.init_Map.keySet().iterator().next());
         }
      }
   }

   synchronized JsonObject init_JsonObject_JsonObject(JsonObject args) throws IOException {
      String name = ag.init_Str_JsonObject(args, "name", "");
      if (!name.isBlank() && name.length() <= 256) {
         JsonArray ids = args.getAsJsonArray("evidenceIds");
         if (ids != null && !ids.isEmpty() && ids.size() <= 32) {
            JsonArray steps = new JsonArray();
            String first = "";

            for (JsonElement id : ids) {
               JsonObject step = this.init_Map.get(id.getAsString());
               if (step == null || !ag.init_Str_JsonObject(step, "scope", "").equals(this.init_cc.poll_Str())) {
                  throw new IllegalArgumentException("Procedure step lacks a recorded successful operation in this world");
               }

               init(step, this.init_cc.init_AiCatalog_Snapshot());
               steps.add(step.deepCopy());
               if (first.isEmpty()) {
                  first = id.getAsString();
               }
            }

            JsonObject payload = ag.init_JsonObject_ObjArr(
               "version",
               1,
               "kind",
               "verified-operations",
               "steps",
               steps,
               "reusePolicy",
               "Fresh world observation and current contract validation required; success is reverified on every replay"
            );
            AiMemoryStore.Memory record = this.init_cc
               .init_AiMemoryStore()
               .init(
                  AiMemoryStore.Inner_DataUtil.read,
                  this.init_cc.poll_Str(),
                  name,
                  first,
                  this.init_cc.init_AiCatalog_Snapshot().revision(),
                  payload.toString()
               );
            return ag.init_JsonObject_ObjArr("saved", true, "procedure", record);
         } else {
            throw new IllegalArgumentException("Select 1..32 successful operation evidence IDs in execution order");
         }
      } else {
         throw new IllegalArgumentException("Procedure name requires 1..256 characters");
      }
   }

   JsonObject init_JsonObject_Str(String id) {
      AiMemoryStore.Memory record = this.init_AiMemoryStore_Memo_Str(id);
      JsonObject payload = ag.init_JsonObject_Str(record.payload());
      JsonArray reasons = new JsonArray();

      try {
         for (JsonElement step : payload.getAsJsonArray("steps")) {
            init(step.getAsJsonObject(), this.init_cc.init_AiCatalog_Snapshot());
         }
      } catch (Exception e) {
         reasons.add(e.getMessage());
      }

      return ag.init_JsonObject_ObjArr(
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

   JsonObject init_JsonObject_Str_2(String id, long generation, cz tools) throws Exception {
      AiMemoryStore.Memory record = this.init_AiMemoryStore_Memo_Str(id);
      JsonObject payload = ag.init_JsonObject_Str(record.payload());
      if (ag.init_I_JsonObject(payload, "version", 0) == 1 && ag.init_Str_JsonObject(payload, "kind", "").equals("verified-operations")) {
         for (JsonElement step : payload.getAsJsonArray("steps")) {
            init(step.getAsJsonObject(), this.init_cc.init_AiCatalog_Snapshot());
         }

         this.init_cc.init_AiObservation_J_Str(generation, "procedure-revalidation");
         JsonArray results = new JsonArray();
         Path checkpoint = this.init_cc
            .init_AiMemoryStore()
            .init_Path()
            .resolve("procedure-runs")
            .resolve(ag.init_Str_Str(this.init_cc.read_Str() + this.init_cc.poll_Str() + id) + ".json");
         JsonArray steps = payload.getAsJsonArray("steps");
         String fingerprint = ag.init_Str_Str(record.payload());
         bw.Inner_DataUtil resume = init_bw_DataUtil_Path(checkpoint, fingerprint, steps.size());

         for (int index = resume.nextStep(); index < steps.size(); index++) {
            JsonElement raw = steps.get(index);
            ag.init_V_Path_Obj(checkpoint, init_JsonObject_Str(fingerprint, index, true));
            AiMemoryStore.init_V_Path_I(checkpoint.getParent(), 64);
            this.init_cc.init_V_J(generation);
            JsonObject step = raw.getAsJsonObject();
            init(step, this.init_cc.init_AiCatalog_Snapshot());
            JsonObject result = tools.init_JsonObject_Str(
               "perform",
               ag.init_JsonObject_ObjArr("id", step.get("id"), "fingerprint", step.get("fingerprint"), "arguments", step.get("arguments")),
               generation
            );
            results.add(result);
            if (!ag.init_Str_JsonObject(result, "status", "").equals("SUCCEEDED")) {
               ag.init_V_Path_Obj(checkpoint, init_JsonObject_Str(fingerprint, index, false));
               return ag.init_JsonObject_ObjArr(
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

            ag.init_V_Path_Obj(checkpoint, init_JsonObject_Str(fingerprint, index + 1, false));
         }

         init_V_Path(checkpoint);
         return ag.init_JsonObject_ObjArr(
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

   static bw.Inner_DataUtil init_bw_DataUtil_Path(Path checkpoint, String fingerprint, int stepCount) throws IOException {
      if (!Files.exists(checkpoint)) {
         return new bw.Inner_DataUtil(0, "");
      }

      JsonObject saved;
      try {
         saved = ag.init_JsonObject_Str(Files.readString(checkpoint));
      } catch (RuntimeException e) {
         init_V_Path(checkpoint);
         return new bw.Inner_DataUtil(0, "Discarded an unreadable previous checkpoint; starting fresh. ");
      }

      int next = ag.init_I_JsonObject(saved, "nextStep", 0);
      if (next >= stepCount) {
         init_V_Path(checkpoint);
         return new bw.Inner_DataUtil(0, "");
      } else if (!ag.init_Str_JsonObject(saved, "procedureFingerprint", "").equals(fingerprint)) {
         throw new IllegalStateException("Procedure changed during a prior run");
      } else if (ag.init_Z_JsonObject(saved, "inFlight", false)) {
         throw new IllegalStateException(
            "Previous procedure step has an uncertain outcome; inspect and repair that step individually before reusing the procedure"
         );
      } else {
         return new bw.Inner_DataUtil(next, "");
      }
   }

   private static JsonObject init_JsonObject_Str(String fingerprint, int nextStep, boolean inFlight) {
      return ag.init_JsonObject_ObjArr("version", 1, "procedureFingerprint", fingerprint, "nextStep", nextStep, "inFlight", inFlight);
   }

   private static void init_V_Path(Path path) {
      try {
         Files.deleteIfExists(path);
      } catch (Exception var2) {
      }
   }

   private AiMemoryStore.Memory init_AiMemoryStore_Memo_Str(String id) {
      return this.init_cc
         .init_AiMemoryStore()
         .init_List()
         .stream()
         .filter(m -> m.id().equals(id) && m.scope().equals(this.init_cc.poll_Str()) && m.kind() == AiMemoryStore.Inner_DataUtil.read)
         .findFirst()
         .orElseThrow(() -> new IllegalArgumentException("No reusable procedure in this world scope"));
   }

   static void init(JsonObject step, AiCatalog.Snapshot catalog) {
      String id = ag.init_Str_JsonObject(step, "id", "");
      AiCatalog.Entry current = catalog.get(id);
      if (current == null || !current.fingerprint().equals(ag.init_Str_JsonObject(step, "fingerprint", ""))) {
         throw new IllegalArgumentException("Procedure dependency changed or unloaded: " + id);
      }
   }

   record Inner_DataUtil(int nextStep, String note) {
   }
}

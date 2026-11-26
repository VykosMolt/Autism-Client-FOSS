package autismclient.ai;

import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.PosixFilePermissions;
import java.util.List;

public class AiStoreFilesCheck {
   static void check(boolean ok, String what) {
      if (!ok) {
         System.out.println("FAIL AiStoreFilesCheck: " + what);
         System.exit(1);
      }
   }

   static boolean ownerOnly(Path file) throws Exception {
      return PosixFilePermissions.toString(Files.getPosixFilePermissions(file)).equals("rw-------");
   }

   static JsonObject user(String text) {
      return AiJson.objectOf("role", "user", "content", text);
   }

   public static void main(String[] args) throws Exception {
      Path dir = Files.createTempDirectory("ai-store");

      // A6: after a rotation, replay still sees the archived lines, oldest first
      AiSessions store = new AiSessions(dir);
      String big = "x".repeat(900000);
      for (int i = 0; i < 5; i++) {
         store.init_V_Str_JsonObject("s", user("old-" + i + big));
      }

      Path archive = dir.resolve("sessions/s.transcript.jsonl.1");
      Path live = dir.resolve("sessions/s.transcript.jsonl");
      check(Files.isRegularFile(archive) && !Files.exists(live), "transcript rotated");
      store.init_V_Str_JsonObject("s", user("new"));
      List<JsonObject> replay = store.init("s", 100000000L, false);
      check(replay.size() == 6, "replay holds archive + live, got " + replay.size());
      check(replay.get(0).get("content").getAsString().startsWith("old-0"), "archive lines come first");
      check(replay.get(5).get("content").equals(new JsonPrimitive("new")), "live line comes last");
      check(ownerOnly(live), "transcript is owner-only");

      // N5: trace rotation keeps older generations
      Path trace = dir.resolve("trace/ai-trace-s.jsonl");
      AiRunTrace t = new AiRunTrace(trace, System.currentTimeMillis(), 300L);
      for (int i = 0; i < 20; i++) {
         t.read("split-" + i, "y".repeat(100));
      }

      for (String suffix : List.of("", ".1", ".2", ".3")) {
         check(Files.isRegularFile(Path.of(trace + suffix)), "trace generation " + suffix + " exists");
      }

      check(ownerOnly(trace), "trace is owner-only");

      // D4: the journal is owner-only
      new AiMemoryStore(dir).init_V_Str_JsonElement("probe", new JsonPrimitive(1));
      try (var files = Files.list(dir)) {
         Path journal = files.filter(p -> p.getFileName().toString().startsWith("journal-")).findFirst().orElseThrow();
         check(ownerOnly(journal), "journal is owner-only");
      }

      System.out.println("PASS AiStoreFilesCheck");
   }
}

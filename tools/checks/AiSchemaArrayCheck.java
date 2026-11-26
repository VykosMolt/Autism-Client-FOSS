package autismclient.ai;

import autismclient.api.ai.AiCapabilityDescriptor;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.util.ArrayList;
import java.util.List;

public class AiSchemaArrayCheck {
   static void check(boolean ok, String what) {
      if (!ok) {
         System.out.println("FAIL AiSchemaArrayCheck: " + what);
         System.exit(1);
      }
   }

   static JsonObject schemaFor(String id) {
      for (AiCapabilityDescriptor d : AiTools.init_List()) {
         if (d.id().equals(id)) {
            return JsonParser.parseString(d.inputSchema()).getAsJsonObject();
         }
      }
      throw new IllegalStateException("no capability " + id);
   }

   static boolean valid(String id, String args) {
      try {
         AiSchemas.init(schemaFor(id), JsonParser.parseString(args));
         return true;
      } catch (IllegalArgumentException e) {
         return false;
      }
   }

   public static void main(String[] args) {
      check(valid("game:plan", "{\"steps\":[{\"id\":\"a\",\"capability\":\"game:chop\",\"arguments\":{},\"needs\":[]}]}"), "plan object step accepted");
      check(valid("game:plan", "{\"steps\":[\"chop 4\"]}"), "plan string step still accepted");
      check(!valid("game:plan", "{\"steps\":[true]}"), "plan boolean step rejected");
      check(valid("game:scaffold", "{\"legs\":[{\"tower\":true,\"blocks\":3},{\"direction\":-90,\"blocks\":8}]}"), "scaffold leg objects accepted");
      check(!valid("game:scaffold", "{\"legs\":[\"north\"]}"), "scaffold string leg rejected");

      JsonObject containerSchema = schemaFor("game:container");
      JsonObject raw = JsonParser.parseString(
         "{\"action\":\"withdraw\",\"x\":1,\"y\":64,\"z\":2,\"items\":[\"minecraft:stone\",{\"item\":\"minecraft:diamond\",\"count\":\"16\",\"extra\":1},{\"item\":\"minecraft:oak_log\",\"count\":null}]}"
      ).getAsJsonObject();
      List<String> ignored = new ArrayList<>();
      JsonObject normalized = AiSchemas.init(containerSchema, raw, true, ignored);
      AiSchemas.init(containerSchema, normalized);
      var items = normalized.getAsJsonArray("items");
      check(items.get(1).getAsJsonObject().get("count").getAsInt() == 16, "anyOf object count string coerced to int");
      check(!items.get(1).getAsJsonObject().has("extra") && ignored.contains("items[].extra"), "anyOf object dropped unknown field");
      check(!items.get(2).getAsJsonObject().has("count"), "anyOf object dropped optional null field");
      System.out.println("PASS AiSchemaArrayCheck");
   }
}

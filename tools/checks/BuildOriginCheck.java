package autismclient.ai;

import com.google.gson.JsonParser;
import net.minecraft.core.BlockPos;

// game:build origin: absent or null falls back to the player; anything but an {x,y,z} object fails instead of building at the player.
public final class BuildOriginCheck {
   public static void main(String[] a) {
      BlockPos player = new BlockPos(1, 2, 3);
      check(AiBuilder.auditOrigin(JsonParser.parseString("{}").getAsJsonObject(), player).equals(player), "missing origin uses the player");
      check(AiBuilder.auditOrigin(JsonParser.parseString("{\"origin\":null}").getAsJsonObject(), player).equals(player), "null origin uses the player");
      check(AiBuilder.auditOrigin(JsonParser.parseString("{\"origin\":{\"x\":100,\"y\":64,\"z\":-5}}").getAsJsonObject(), player).equals(new BlockPos(100, 64, -5)), "object origin");
      for (String bad : new String[]{"\"100,64,100\"", "[100,64,100]", "7"}) {
         try {
            AiBuilder.auditOrigin(JsonParser.parseString("{\"origin\":" + bad + "}").getAsJsonObject(), player);
            check(false, "origin " + bad + " is rejected");
         } catch (IllegalArgumentException expected) {
         }
      }

      System.out.println("PASS BuildOriginCheck");
   }

   static void check(boolean ok, String what) {
      if (!ok) {
         System.out.println("FAIL BuildOriginCheck: " + what);
         System.exit(1);
      }
   }
}

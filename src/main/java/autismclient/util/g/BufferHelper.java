package autismclient.util.g;

import java.util.Locale;
import java.util.Set;
import net.minecraft.world.inventory.ContainerInput;

public final class BufferHelper {
   private static final Set<String> init = Set.of("nbt", "server", "plugins", "multi");
   private static final Set<String> load = Set.of(
      "toggle",
      "t",
      "delay",
      "gamemode",
      "bind",
      "binds",
      "prefix",
      "irc",
      "macro",
      "modules",
      "features",
      "commands",
      "cmds",
      "help",
      "dismount",
      "give",
      "xcarry"
   );
   private static final Set<String> read = Set.of(
      "click-slot", "click-item", "change-slot", "drop", "close", "use", "swing", "send", "say", "damage", "vclip", "hclip"
   );

   private BufferHelper() {
   }

   public static String init_Str(String name) {
      if (name != null && !name.isBlank()) {
         String key = name.toLowerCase(Locale.ROOT);
         if (read.contains(key)) {
            return null;
         } else if (init.contains(key)) {
            return "opens a GUI - not available in the headless console";
         } else {
            return load.contains(key) ? "local-only - not possible on a headless bot" : "unknown or unsupported client command";
         }
      } else {
         return "empty command";
      }
   }

   public static String init_Str(String name, String args) {
      String deny = init_Str(name);
      if (deny != null) {
         return deny;
      }

      String key = name.toLowerCase(Locale.ROOT);
      if (!key.equals("vclip") && !key.equals("hclip")) {
         return null;
      }

      BufferHelper.Inner_StreamUtil request = init_BufferHelper_Strea(key, args);
      return request.init_BufferHelper_Cache() == null ? request.init_Str() : null;
   }

   public static BufferHelper.Inner_StreamUtil init_BufferHelper_Strea(String command, String args) {
      String name = command == null ? "clip" : command.toLowerCase(Locale.ROOT);
      boolean vertical = name.equals("vclip");
      String[] parts = args != null && !args.isBlank() ? args.trim().split("\\s+") : new String[0];
      if (parts.length == 0) {
         return BufferHelper.Inner_StreamUtil.init("Usage: " + name + " <blocks>");
      }

      String mode = parts[0].toLowerCase(Locale.ROOT);
      if (init_Z(mode)) {
         return BufferHelper.Inner_StreamUtil.init(name + " " + mode + " needs client world collision data - headless bots only take exact distances");
      }

      int offset = 0;
      boolean segmented = true;
      boolean custom = false;
      switch (mode) {
         case "single":
         case "normal":
            segmented = false;
            offset = 1;
            break;
         case "default":
         case "paper":
         case "padding":
            offset = 1;
            break;
         case "custom":
            custom = true;
            offset = 1;
      }

      if (offset >= parts.length) {
         return BufferHelper.Inner_StreamUtil.init("Usage: " + name + " <blocks>");
      }

      Double blocks = init_Double(parts[offset]);
      if (blocks == null) {
         return BufferHelper.Inner_StreamUtil.init("Bad " + name + " distance: " + parts[offset]);
      }

      int segment = custom && offset + 1 < parts.length ? init(parts[offset + 1], 1, 50, 10) : 10;
      int maxPackets = custom && offset + 2 < parts.length ? init(parts[offset + 2], 1, 100, 20) : 20;
      boolean forceGround = custom && offset + 5 < parts.length ? Boolean.parseBoolean(parts[offset + 5]) : vertical;
      int segments = segmented ? Math.max(1, (int)Math.ceil(Math.abs(blocks) / segment)) : 1;
      if (segments > maxPackets) {
         segments = 1;
      }

      return BufferHelper.Inner_StreamUtil.init(new BufferHelper.Inner_CachePool(blocks, segments, forceGround));
   }

   public static boolean init_Z(String mode) {
      if (mode == null) {
         return false;
      }

      boolean var10000;
      switch (mode.toLowerCase(Locale.ROOT)) {
         case "top":
         case "bottom":
         case "forward":
         case "back":
            var10000 = true;
            break;
         default:
            var10000 = false;
      }

      return var10000;
   }

   private static Double init_Double(String value) {
      try {
         double parsed = Double.parseDouble(value.trim());
         return Double.isFinite(parsed) ? parsed : null;
      } catch (RuntimeException ignored) {
         return null;
      }
   }

   private static int init(String value, int min, int max, int fallback) {
      try {
         return Math.max(min, Math.min(max, Integer.parseInt(value.trim())));
      } catch (RuntimeException ignored) {
         return fallback;
      }
   }

   public static BufferHelper.Inner_DataUtil init_BufferHelper_DataU(String mode) {
      String m = mode == null ? "" : mode.trim().toLowerCase(Locale.ROOT);
      BufferHelper.Inner_DataUtil var10000;
      switch (m) {
         case "":
         case "left":
         case "pickup": {
            BufferHelper.Inner_DataUtil var15 = new BufferHelper.Inner_DataUtil(ContainerInput.PICKUP, 0);
            BufferHelper.Inner_DataUtil var16 = var15;
            var10000 = var16;
            break;
         }
         case "right": {
            BufferHelper.Inner_DataUtil var14 = new BufferHelper.Inner_DataUtil(ContainerInput.PICKUP, 1);
            BufferHelper.Inner_DataUtil var15 = var14;
            var10000 = var15;
            break;
         }
         case "middle": {
            BufferHelper.Inner_DataUtil var13 = new BufferHelper.Inner_DataUtil(ContainerInput.PICKUP, 2);
            BufferHelper.Inner_DataUtil var14 = var13;
            var10000 = var14;
            break;
         }
         case "clone": {
            BufferHelper.Inner_DataUtil var12 = new BufferHelper.Inner_DataUtil(ContainerInput.CLONE, 2);
            BufferHelper.Inner_DataUtil var13 = var12;
            var10000 = var13;
            break;
         }
         case "shift":
         case "shift-left":
         case "quick-move": {
            BufferHelper.Inner_DataUtil var11 = new BufferHelper.Inner_DataUtil(ContainerInput.QUICK_MOVE, 0);
            BufferHelper.Inner_DataUtil var12 = var11;
            var10000 = var12;
            break;
         }
         case "shift-right": {
            BufferHelper.Inner_DataUtil var10 = new BufferHelper.Inner_DataUtil(ContainerInput.QUICK_MOVE, 1);
            BufferHelper.Inner_DataUtil var11 = var10;
            var10000 = var11;
            break;
         }
         case "drop":
         case "drop-item":
         case "throw": {
            BufferHelper.Inner_DataUtil var9 = new BufferHelper.Inner_DataUtil(ContainerInput.THROW, 0);
            BufferHelper.Inner_DataUtil var10 = var9;
            var10000 = var10;
            break;
         }
         case "drop-stack":
         case "throw-all": {
            BufferHelper.Inner_DataUtil var8 = new BufferHelper.Inner_DataUtil(ContainerInput.THROW, 1);
            BufferHelper.Inner_DataUtil var9 = var8;
            var10000 = var9;
            break;
         }
         default: {
            label86:
            if (m.startsWith("swap")) {
               BufferHelper.Inner_DataUtil var7;
               try {
                  int n = Integer.parseInt(m.substring(4).trim());
                  if (n < 1 || n > 9) {
                     break label86;
                  }

                  var7 = new BufferHelper.Inner_DataUtil(ContainerInput.SWAP, n - 1);
               } catch (RuntimeException var7x) {
                  break label86;
               }

               return var7;
            }

            Object var8 = null;
            var10000 = (BufferHelper.Inner_DataUtil)var8;
         }
      }

      return var10000;
   }

   public static BufferHelper.Inner_DataUtil init(int button, boolean shift, boolean ctrl) {
      if (button == 2) {
         return new BufferHelper.Inner_DataUtil(ContainerInput.CLONE, 2);
      }

      int mouseButton = button == 1 ? 1 : 0;
      return new BufferHelper.Inner_DataUtil(shift ? ContainerInput.QUICK_MOVE : ContainerInput.PICKUP, mouseButton);
   }

   public static BufferHelper.Inner_DataUtil init(boolean wholeStack) {
      return new BufferHelper.Inner_DataUtil(ContainerInput.THROW, wholeStack ? 1 : 0);
   }

   public record Inner_CachePool(double init_D, int init_I, boolean init_Z) {
   }

   public record Inner_DataUtil(ContainerInput init_ContainerInput, int init_I) {
   }

   public record Inner_StreamUtil(BufferHelper.Inner_CachePool init_BufferHelper_Cache, String init_Str) {
      static BufferHelper.Inner_StreamUtil init(BufferHelper.Inner_CachePool spec) {
         return new BufferHelper.Inner_StreamUtil(spec, null);
      }

      static BufferHelper.Inner_StreamUtil init(String message) {
         return new BufferHelper.Inner_StreamUtil(null, message);
      }
   }
}

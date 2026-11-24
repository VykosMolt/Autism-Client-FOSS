package autismclient.util;

import autismclient.compat263.CompatGLFW;
import net.minecraft.client.Minecraft;

public final class ValueSink {
   private static final int init = -1000;

   private ValueSink() {
   }

   public static boolean init_Z(int bindCode) {
      return bindCode <= -1000;
   }

   public static int init_I(int button) {
      return -1000 - button;
   }

   public static int load_I(int bindCode) {
      return -1000 - bindCode;
   }

   public static boolean load_Z(int button) {
      return button > 0 && button <= 7;
   }

   public static boolean init(Minecraft client, int bindCode) {
      if (!AutismLiteVariant.enabled() && autismclient.ai.aP.load_Z()) {
         return false;
      }

      if (bindCode != -1 && client != null && client.getWindow() != null) {
         long handle = client.getWindow().handle();
         if (!init_Z(bindCode)) {
            return CompatGLFW.glfwGetKey(handle, bindCode) == 1;
         }

         int button = load_I(bindCode);
         return load_Z(button) && CompatGLFW.glfwGetMouseButton(handle, button) == 1;
      } else {
         return false;
      }
   }

   public static String init_Str(int bindCode) {
      if (bindCode == -1) {
         return "None";
      }

      if (init_Z(bindCode)) {
         return load_Str(load_I(bindCode));
      }

      String name = CompatGLFW.glfwGetKeyName(bindCode, 0);
      if (name != null) {
         return name.toUpperCase();
      }

      String var10000;
      switch (bindCode) {
         case 32:
            var10000 = "Space";
            break;
         case 257:
            var10000 = "Enter";
            break;
         case 258:
            var10000 = "Tab";
            break;
         case 259:
            var10000 = "Backsp";
            break;
         case 260:
            var10000 = "Insert";
            break;
         case 261:
            var10000 = "Delete";
            break;
         case 262:
            var10000 = "Right";
            break;
         case 263:
            var10000 = "Left";
            break;
         case 264:
            var10000 = "Down";
            break;
         case 265:
            var10000 = "Up";
            break;
         case 266:
            var10000 = "PgUp";
            break;
         case 267:
            var10000 = "PgDn";
            break;
         case 268:
            var10000 = "Home";
            break;
         case 269:
            var10000 = "End";
            break;
         case 280:
            var10000 = "CapsLk";
            break;
         case 281:
            var10000 = "ScrLk";
            break;
         case 282:
            var10000 = "NumLk";
            break;
         case 283:
            var10000 = "PrtSc";
            break;
         case 284:
            var10000 = "Pause";
            break;
         case 290:
            var10000 = "F1";
            break;
         case 291:
            var10000 = "F2";
            break;
         case 292:
            var10000 = "F3";
            break;
         case 293:
            var10000 = "F4";
            break;
         case 294:
            var10000 = "F5";
            break;
         case 295:
            var10000 = "F6";
            break;
         case 296:
            var10000 = "F7";
            break;
         case 297:
            var10000 = "F8";
            break;
         case 298:
            var10000 = "F9";
            break;
         case 299:
            var10000 = "F10";
            break;
         case 300:
            var10000 = "F11";
            break;
         case 301:
            var10000 = "F12";
            break;
         case 335:
            var10000 = "Num Enter";
            break;
         case 340:
            var10000 = "L.Shift";
            break;
         case 341:
            var10000 = "L.Ctrl";
            break;
         case 342:
            var10000 = "L.Alt";
            break;
         case 344:
            var10000 = "R.Shift";
            break;
         case 345:
            var10000 = "R.Ctrl";
            break;
         case 346:
            var10000 = "R.Alt";
            break;
         default:
            var10000 = "Key " + bindCode;
      }

      return var10000;
   }

   public static String load_Str(int button) {
      String var10000;
      switch (button) {
         case 0:
            var10000 = "LMB";
            break;
         case 1:
            var10000 = "RMB";
            break;
         case 2:
            var10000 = "MMB";
            break;
         default:
            var10000 = "M" + (button + 1);
      }

      return var10000;
   }
}

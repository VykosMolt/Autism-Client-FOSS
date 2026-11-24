package autismclient.compat263;

import com.mojang.blaze3d.platform.InputConstants.Type;
import java.nio.ByteBuffer;
import java.util.Arrays;
import org.lwjgl.sdl.SDLClipboard;
import org.lwjgl.sdl.SDLKeyboard;
import org.lwjgl.sdl.SDLMouse;
import org.lwjgl.sdl.SDLVideo;

public final class CompatGLFW {
   public static final int GLFW_RELEASE = 0;
   public static final int GLFW_PRESS = 1;
   public static final int GLFW_REPEAT = 2;
   public static final int GLFW_KEY_UNKNOWN = -1;
   public static final int GLFW_KEY_SPACE = 32;
   public static final int GLFW_KEY_APOSTROPHE = 39;
   public static final int GLFW_KEY_COMMA = 44;
   public static final int GLFW_KEY_MINUS = 45;
   public static final int GLFW_KEY_PERIOD = 46;
   public static final int GLFW_KEY_SLASH = 47;
   public static final int GLFW_KEY_0 = 48;
   public static final int GLFW_KEY_1 = 49;
   public static final int GLFW_KEY_2 = 50;
   public static final int GLFW_KEY_3 = 51;
   public static final int GLFW_KEY_4 = 52;
   public static final int GLFW_KEY_5 = 53;
   public static final int GLFW_KEY_6 = 54;
   public static final int GLFW_KEY_7 = 55;
   public static final int GLFW_KEY_8 = 56;
   public static final int GLFW_KEY_9 = 57;
   public static final int GLFW_KEY_SEMICOLON = 59;
   public static final int GLFW_KEY_EQUAL = 61;
   public static final int GLFW_KEY_A = 65;
   public static final int GLFW_KEY_B = 66;
   public static final int GLFW_KEY_C = 67;
   public static final int GLFW_KEY_D = 68;
   public static final int GLFW_KEY_E = 69;
   public static final int GLFW_KEY_F = 70;
   public static final int GLFW_KEY_G = 71;
   public static final int GLFW_KEY_H = 72;
   public static final int GLFW_KEY_I = 73;
   public static final int GLFW_KEY_J = 74;
   public static final int GLFW_KEY_K = 75;
   public static final int GLFW_KEY_L = 76;
   public static final int GLFW_KEY_M = 77;
   public static final int GLFW_KEY_N = 78;
   public static final int GLFW_KEY_O = 79;
   public static final int GLFW_KEY_P = 80;
   public static final int GLFW_KEY_Q = 81;
   public static final int GLFW_KEY_R = 82;
   public static final int GLFW_KEY_S = 83;
   public static final int GLFW_KEY_T = 84;
   public static final int GLFW_KEY_U = 85;
   public static final int GLFW_KEY_V = 86;
   public static final int GLFW_KEY_W = 87;
   public static final int GLFW_KEY_X = 88;
   public static final int GLFW_KEY_Y = 89;
   public static final int GLFW_KEY_Z = 90;
   public static final int GLFW_KEY_LEFT_BRACKET = 91;
   public static final int GLFW_KEY_BACKSLASH = 92;
   public static final int GLFW_KEY_RIGHT_BRACKET = 93;
   public static final int GLFW_KEY_GRAVE_ACCENT = 96;
   public static final int GLFW_KEY_ESCAPE = 256;
   public static final int GLFW_KEY_ENTER = 257;
   public static final int GLFW_KEY_TAB = 258;
   public static final int GLFW_KEY_BACKSPACE = 259;
   public static final int GLFW_KEY_INSERT = 260;
   public static final int GLFW_KEY_DELETE = 261;
   public static final int GLFW_KEY_RIGHT = 262;
   public static final int GLFW_KEY_LEFT = 263;
   public static final int GLFW_KEY_DOWN = 264;
   public static final int GLFW_KEY_UP = 265;
   public static final int GLFW_KEY_PAGE_UP = 266;
   public static final int GLFW_KEY_PAGE_DOWN = 267;
   public static final int GLFW_KEY_HOME = 268;
   public static final int GLFW_KEY_END = 269;
   public static final int GLFW_KEY_CAPS_LOCK = 280;
   public static final int GLFW_KEY_SCROLL_LOCK = 281;
   public static final int GLFW_KEY_NUM_LOCK = 282;
   public static final int GLFW_KEY_PRINT_SCREEN = 283;
   public static final int GLFW_KEY_PAUSE = 284;
   public static final int GLFW_KEY_F1 = 290;
   public static final int GLFW_KEY_F2 = 291;
   public static final int GLFW_KEY_F3 = 292;
   public static final int GLFW_KEY_F4 = 293;
   public static final int GLFW_KEY_F5 = 294;
   public static final int GLFW_KEY_F6 = 295;
   public static final int GLFW_KEY_F7 = 296;
   public static final int GLFW_KEY_F8 = 297;
   public static final int GLFW_KEY_F9 = 298;
   public static final int GLFW_KEY_F10 = 299;
   public static final int GLFW_KEY_F11 = 300;
   public static final int GLFW_KEY_F12 = 301;
   public static final int GLFW_KEY_LEFT_SHIFT = 340;
   public static final int GLFW_KEY_LEFT_CONTROL = 341;
   public static final int GLFW_KEY_LEFT_ALT = 342;
   public static final int GLFW_KEY_LEFT_SUPER = 343;
   public static final int GLFW_KEY_RIGHT_SHIFT = 344;
   public static final int GLFW_KEY_RIGHT_CONTROL = 345;
   public static final int GLFW_KEY_RIGHT_ALT = 346;
   public static final int GLFW_KEY_RIGHT_SUPER = 347;
   private static final int[] GLFW_TO_SDL_ARR = new int[512];
   private static final int[] SDL_TO_GLFW_ARR = new int[512];

   private static void mapKey(int var0, int var1) {
      if (var0 >= 0 && var0 < GLFW_TO_SDL_ARR.length) {
         GLFW_TO_SDL_ARR[var0] = var1;
      }

      if (var1 >= 0 && var1 < SDL_TO_GLFW_ARR.length) {
         SDL_TO_GLFW_ARR[var1] = var0;
      }
   }

   private CompatGLFW() {
   }

   public static int glfwToSdl(int var0) {
      return var0 >= 0 && var0 < GLFW_TO_SDL_ARR.length ? GLFW_TO_SDL_ARR[var0] : -1;
   }

   public static int sdlToGlfw(int var0) {
      if (var0 >= 0 && var0 < SDL_TO_GLFW_ARR.length) {
         int var1 = SDL_TO_GLFW_ARR[var0];
         if (var1 >= 0) {
            return var1;
         }
      }

      return var0;
   }

   public static int sdlMouseToGlfw(int var0) {
      byte var10000;
      switch (var0) {
         case 1:
            var10000 = 0;
            break;
         case 2:
            var10000 = 2;
            break;
         case 3:
            var10000 = 1;
            break;
         default:
            var10000 = (byte)(var0 > 0 ? var0 - 1 : var0);
      }

      return var10000;
   }

   public static int glfwMouseToSdl(int var0) {
      byte var10000;
      switch (var0) {
         case 0:
            var10000 = 1;
            break;
         case 1:
            var10000 = 3;
            break;
         case 2:
            var10000 = 2;
            break;
         default:
            var10000 = (byte)(var0 >= 0 ? var0 + 1 : var0);
      }

      return var10000;
   }

   public static String glfwGetClipboardString(long var0) {
      try {
         String text = SDLClipboard.SDL_GetClipboardText();
         return text != null ? text : "";
      } catch (Throwable ignored) {
         return "";
      }
   }

   public static void glfwSetClipboardString(long var0, CharSequence var2) {
      if (var2 != null) {
         try {
            SDLClipboard.SDL_SetClipboardText(var2);
         } catch (Throwable var4) {
         }
      }
   }

   public static boolean sdlIsKeyDown(int var0) {
      try {
         ByteBuffer state = SDLKeyboard.SDL_GetKeyboardState();
         return var0 >= 0 && var0 < state.capacity() && state.get(var0) != 0;
      } catch (Throwable var1) {
         return false;
      }
   }

   public static int glfwGetKey(long var0, int var2) {
      if (var2 < 0) {
         return 0;
      }

      int var3 = glfwToSdl(var2);
      if (var3 < 0) {
         return 0;
      }

      try {
         return Compat263.isKeyDown(var3) ? 1 : 0;
      } catch (Throwable var5) {
         return 0;
      }
   }

   public static String glfwGetKeyName(int var0, int var1) {
      if (var0 < 0) {
         return null;
      }

      int var2 = glfwToSdl(var0);
      if (var2 < 0) {
         return null;
      }

      try {
         return Type.KEYBOARD.getOrCreate(var2).getDisplayName().getString();
      } catch (Throwable var4) {
         return null;
      }
   }

   public static int glfwGetMouseButton(long var0, int var2) {
      if (var2 < 0) {
         return 0;
      }

      int var3 = glfwMouseToSdl(var2);
      if (var3 >= 1 && var3 <= 31) {
         try {
            int var4 = SDLMouse.nSDL_GetMouseState(0L, 0L);
            return (var4 & 1 << var3 - 1) != 0 ? 1 : 0;
         } catch (Throwable var5) {
            return 0;
         }
      } else {
         return 0;
      }
   }

   public static void glfwSetWindowIcon(long var0, CompatGLFWImage.Buffer var2) {
   }

   public static void glfwSetWindowSize(long var0, int var2, int var3) {
      if (var0 != 0L && var2 > 0 && var3 > 0) {
         try {
            SDLVideo.SDL_SetWindowSize(var0, var2, var3);
         } catch (Throwable var5) {
         }
      }
   }

   public static String glfwGetMonitorName(long var0) {
      try {
         String name = SDLVideo.SDL_GetDisplayName((int)var0);
         return name != null && !name.isEmpty() ? name : "unknown";
      } catch (Throwable ignored) {
         return "unknown";
      }
   }

   static {
      Arrays.fill(GLFW_TO_SDL_ARR, -1);
      Arrays.fill(SDL_TO_GLFW_ARR, -1);
      mapKey(32, 44);
      mapKey(39, 52);
      mapKey(44, 54);
      mapKey(45, 45);
      mapKey(46, 55);
      mapKey(47, 56);
      mapKey(48, 39);
      mapKey(49, 30);
      mapKey(50, 31);
      mapKey(51, 32);
      mapKey(52, 33);
      mapKey(53, 34);
      mapKey(54, 35);
      mapKey(55, 36);
      mapKey(56, 37);
      mapKey(57, 38);
      mapKey(59, 51);
      mapKey(61, 46);

      for (int var0 = 0; var0 < 26; var0++) {
         mapKey(65 + var0, 4 + var0);
      }

      mapKey(91, 47);
      mapKey(92, 49);
      mapKey(93, 48);
      mapKey(96, 53);
      mapKey(161, 100);
      mapKey(256, 41);
      mapKey(257, 40);
      mapKey(258, 43);
      mapKey(259, 42);
      mapKey(260, 73);
      mapKey(261, 76);
      mapKey(262, 79);
      mapKey(263, 80);
      mapKey(264, 81);
      mapKey(265, 82);
      mapKey(266, 75);
      mapKey(267, 78);
      mapKey(268, 74);
      mapKey(269, 77);
      mapKey(280, 57);
      mapKey(281, 71);
      mapKey(282, 83);
      mapKey(283, 70);
      mapKey(284, 72);

      for (int var1 = 0; var1 < 12; var1++) {
         mapKey(290 + var1, 58 + var1);
      }

      for (int var2 = 0; var2 < 12; var2++) {
         mapKey(302 + var2, 104 + var2);
      }

      mapKey(320, 98);
      mapKey(321, 89);
      mapKey(322, 90);
      mapKey(323, 91);
      mapKey(324, 92);
      mapKey(325, 93);
      mapKey(326, 94);
      mapKey(327, 95);
      mapKey(328, 96);
      mapKey(329, 97);
      mapKey(330, 99);
      mapKey(331, 84);
      mapKey(332, 85);
      mapKey(333, 86);
      mapKey(334, 87);
      mapKey(335, 88);
      mapKey(336, 103);
      mapKey(340, 225);
      mapKey(341, 224);
      mapKey(342, 226);
      mapKey(343, 227);
      mapKey(344, 229);
      mapKey(345, 228);
      mapKey(346, 230);
      mapKey(347, 231);
      mapKey(348, 101);
   }
}

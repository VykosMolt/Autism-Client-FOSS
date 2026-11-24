package autismclient.util;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;

public final class R {
   private static final String init = "gg.essential.util.GuiUtil";
   private static final String load = "net.minecraft.client.gui.screens.Screen";

   private R() {
   }

   public static boolean init_Z() {
      return FabricLoader.getInstance().isModLoaded("essential");
   }

   public static void init_V() {
      init_V_Obj(load("gg.essential.gui.friends.SocialMenu"));
   }

   public static void load() {
      init_V_Obj(load("gg.essential.gui.wardrobe.Wardrobe"));
   }

   public static void read() {
      init_V_Obj(load("gg.essential.gui.screenshot.components.ScreenshotBrowser"));
   }

   public static void write() {
      if (!init_Z_R_DataUtil(modalManager -> init_Obj_Str_ObjArr("gg.essential.gui.sps.WorldSelectionModal", modalManager))) {
         flush();
      }
   }

   public static void update() {
      try {
         Class<?> cfg = Class.forName("gg.essential.config.McEssentialConfig");
         Object instance = cfg.getField("INSTANCE").get(null);
         Method gui = cfg.getMethod("gui", String.class);
         gui.setAccessible(true);
         Object screen = gui.invoke(instance, (Object) null);
         init_V_Obj(screen);
      } catch (Throwable var4) {
      }
   }

   public static void sync() {
      if (!init_Z_R_DataUtil(modalManager -> {
         Object accountManager = init_Obj_Str("gg.essential.gui.menu.AccountManager");
         if (accountManager == null) {
            return null;
         }

         Object accounts = init_Obj_Obj_Str(accountManager, "getAllAccounts");
         return accounts == null ? null : init_Obj_Str_ObjArr("gg.essential.gui.menu.AccountManagerModal", modalManager, accountManager, accounts);
      })) {
         flush();
      }
   }

   public static void flush() {
      try {
         Class<?> clazz = Class.forName("gg.essential.gui.modals.QuickAccessModal");
         Object companion = clazz.getDeclaredField("Companion").get(null);
         Method open = companion.getClass().getDeclaredMethod("open");
         open.setAccessible(true);
         open.invoke(companion);
      } catch (Throwable var3) {
      }
   }

   private static void init_V_Obj(Object screen) {
      if (screen != null) {
         try {
            Class<?> guiUtil = Class.forName("gg.essential.util.GuiUtil");
            Object instance = guiUtil.getField("INSTANCE").get(null);
            Class<?> screenType = Class.forName("net.minecraft.client.gui.screens.Screen");
            Method open = guiUtil.getMethod("openScreen", screenType);
            open.setAccessible(true);
            open.invoke(instance, screen);
         } catch (Throwable t) {
            if (screen instanceof Screen s) {
               Minecraft mc = Minecraft.getInstance();
               if (mc != null) {
                  mc.execute(() -> mc.gui.setScreen(s));
               }
            }
         }
      }
   }

   private static boolean init_Z_R_DataUtil(R.Inner_DataUtil factory) {
      if (factory == null) {
         return false;
      }

      try {
         Class<?> guiUtil = Class.forName("gg.essential.util.GuiUtil");
         Object instance = guiUtil.getField("INSTANCE").get(null);
         Class<?> functionType = Class.forName("kotlin.jvm.functions.Function1");
         Object function = Proxy.newProxyInstance(functionType.getClassLoader(), new Class[]{functionType}, (proxy, method, args) -> {
            String name = method.getName();
            if ("toString".equals(name)) {
               return "AutismEssentialModalFactory";
            } else if ("hashCode".equals(name)) {
               return System.identityHashCode(proxy);
            } else if ("equals".equals(name)) {
               return proxy == (args == null ? null : args[0]);
            } else {
               return "invoke".equals(name) && args != null && args.length == 1 ? factory.create(args[0]) : null;
            }
         });
         Method push = guiUtil.getMethod("pushModal", functionType);
         push.setAccessible(true);
         push.invoke(instance, function);
         return true;
      } catch (Throwable ignored) {
         return false;
      }
   }

   private static Object init_Obj_Str(String className) {
      try {
         Class<?> clazz = Class.forName(className);
         Constructor<?> ctor = clazz.getDeclaredConstructor();
         ctor.setAccessible(true);
         return ctor.newInstance();
      } catch (Throwable ignored) {
         return null;
      }
   }

   private static Object init_Obj_Str_ObjArr(String className, Object... args) {
      try {
         Class<?> clazz = Class.forName(className);

         for (Constructor<?> ctor : clazz.getDeclaredConstructors()) {
            Class<?>[] params = ctor.getParameterTypes();
            if (params.length == (args == null ? 0 : args.length)) {
               boolean match = true;

               for (int i = 0; i < params.length; i++) {
                  Object arg = args[i];
                  if (arg == null) {
                     if (params[i].isPrimitive()) {
                        match = false;
                        break;
                     }
                  } else if (!init_Class_Class(params[i]).isInstance(arg)) {
                     match = false;
                     break;
                  }
               }

               if (match) {
                  ctor.setAccessible(true);
                  return ctor.newInstance(args);
               }
            }
         }
      } catch (Throwable var11) {
      }

      return null;
   }

   private static Object load(String className) {
      try {
         Class<?> clazz = Class.forName(className);

         try {
            Constructor<?> noArg = clazz.getDeclaredConstructor();
            noArg.setAccessible(true);
            return noArg.newInstance();
         } catch (NoSuchMethodException var9) {
            for (Constructor<?> ctor : clazz.getDeclaredConstructors()) {
               Class<?>[] params = ctor.getParameterTypes();
               if (params.length >= 2
                  && params[params.length - 1].getName().equals("kotlin.jvm.internal.DefaultConstructorMarker")
                  && params[params.length - 2] == int.class) {
                  Object[] args = new Object[params.length];

                  for (int i = 0; i < params.length - 2; i++) {
                     args[i] = init_Obj_Class(params[i]);
                  }

                  int realParams = params.length - 2;
                  args[params.length - 2] = realParams >= 32 ? -1 : (1 << realParams) - 1;
                  args[params.length - 1] = null;
                  ctor.setAccessible(true);
                  return ctor.newInstance(args);
               }
            }
         }
      } catch (Throwable var11) {
      }

      return null;
   }

   private static Object init_Obj_Obj_Str(Object instance, String methodName) {
      if (instance != null && methodName != null) {
         try {
            Method method = instance.getClass().getMethod(methodName);
            method.setAccessible(true);
            return method.invoke(instance);
         } catch (Throwable ignored) {
            return null;
         }
      } else {
         return null;
      }
   }

   private static Class<?> init_Class_Class(Class<?> type) {
      if (!type.isPrimitive()) {
         return type;
      } else if (type == boolean.class) {
         return Boolean.class;
      } else if (type == char.class) {
         return Character.class;
      } else if (type == byte.class) {
         return Byte.class;
      } else if (type == short.class) {
         return Short.class;
      } else if (type == int.class) {
         return Integer.class;
      } else if (type == long.class) {
         return Long.class;
      } else if (type == float.class) {
         return Float.class;
      } else {
         return type == double.class ? Double.class : Void.class;
      }
   }

   private static Object init_Obj_Class(Class<?> type) {
      if (!type.isPrimitive()) {
         return null;
      } else if (type == boolean.class) {
         return false;
      } else if (type == char.class) {
         return '\u0000';
      } else if (type == double.class) {
         return 0.0;
      } else if (type == float.class) {
         return 0.0F;
      } else if (type == long.class) {
         return 0L;
      } else if (type == byte.class) {
         return (byte) 0;
      } else if (type == short.class) {
         return (short) 0;
      } else {
         return 0;
      }
   }

   @FunctionalInterface
   private interface Inner_DataUtil {
      Object create(Object var1);
   }
}

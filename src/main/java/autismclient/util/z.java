package autismclient.util;

import autismclient.AutismClientAddon;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import net.fabricmc.loader.api.FabricLoader;

public final class z {
   public static final List<String> init_List = List.of(".", "%", "-", "_", "*", "#", "@", "&", "=");
   private static final String init_Str = "meteordevelopment.meteorclient.systems.macros.Macros";
   private static final String load_Str = "meteordevelopment.meteorclient.systems.macros.Macro";
   private static final String read_Str = "meteordevelopment.meteorclient.systems.modules.Modules";
   private static final boolean init_Z = FabricLoader.getInstance().isModLoaded("meteor-client")
      && load("meteordevelopment.meteorclient.systems.macros.Macros");
   private static volatile String write;
   private static volatile String update;
   private static volatile String sync = ".";
   private static volatile long init_J;
   private static volatile int init_I = -2147483648;
   private static volatile List<String> load_List = List.of();
   private static volatile List<String> read_List;

   private z() {
   }

   public static boolean init_Z() {
      return init_Z;
   }

   public static String init_Str() {
      autismclient.modules.PoolEntry module = autismclient.modules.PoolEntry.init_PoolEntry();
      String stored = module == null ? AutismConfig.getGlobal().commandPrefix : module.init_Str();
      String safeStored = stored == null ? "" : stored;
      String cachedStored = write;
      String cachedPrefix = update;
      if (safeStored.equals(cachedStored) && cachedPrefix != null) {
         return cachedPrefix;
      }

      String normalized = init_Str(safeStored);
      write = safeStored;
      update = normalized;
      return normalized;
   }

   public static String load_Str() {
      return init_Z() ? "%" : ".";
   }

   public static String init_Str(String prefix) {
      String selected = prefix == null ? "" : prefix.trim();
      if (!init_List.contains(selected)) {
         return load_Str();
      } else {
         return ".".equals(selected) && init_Z() ? "%" : selected;
      }
   }

   public static boolean init_Z(String prefix) {
      String selected = prefix == null ? "" : prefix.trim();
      return init_List.contains(selected) && (!".".equals(selected) || !init_Z());
   }

   public static String read_Str() {
      if (!init_Z) {
         return ".";
      }

      long now = System.nanoTime();
      if (now < init_J) {
         return sync;
      }

      synchronized (z.class) {
         now = System.nanoTime();
         if (now < init_J) {
            return sync;
         }

         sync = write_Str();
         init_J = now + 1000000000L;
         return sync;
      }
   }

   public static List<aM> init_List() {
      if (!init_Z()) {
         return Collections.emptyList();
      }

      List<aM> macros = new ArrayList<>();

      try {
         Object meteorMacros = init_Obj();
         Method getAllMethod = meteorMacros.getClass().getMethod("getAll");
         Object allMacros = getAllMethod.invoke(meteorMacros);
         if (!(allMacros instanceof Iterable)) {
            return macros;
         }

         for (Object meteorMacro : (Iterable)allMacros) {
            aM converted = init(meteorMacro);
            if (converted != null) {
               macros.add(converted);
            }
         }
      } catch (Exception e) {
         AutismClientAddon.LOG.warn("[Autism] Failed to read Meteor macros reflectively.", e);
      }

      return macros;
   }

   public static List<String> load_List() {
      List<String> names = new ArrayList<>();

      for (aM macro : init_List()) {
         if (macro != null && macro.init_Str != null && !macro.init_Str.isBlank()) {
            names.add(macro.init_Str);
         }
      }

      return names;
   }

   public static aM init_aM(String macroName) {
      if (macroName != null && !macroName.isBlank()) {
         for (aM macro : init_List()) {
            if (macroName.equalsIgnoreCase(macro.init_Str)) {
               return macro;
            }
         }

         return null;
      } else {
         return null;
      }
   }

   public static boolean init(aM packUtilMacro) {
      return false;
   }

   public static List<String> read_List() {
      int revision = autismclient.modules.al.init_I();
      List<String> names = load_List;
      if (init_I != revision) {
         List<String> rebuilt = new ArrayList<>(autismclient.modules.al.init_List());
         rebuilt.removeIf(name -> name == null || name.isBlank());
         rebuilt.sort(String::compareToIgnoreCase);
         names = List.copyOf(rebuilt);
         load_List = names;
         init_I = revision;
      }

      return new ArrayList<>(names);
   }

   public static List<String> write_List() {
      if (!init_Z()) {
         return Collections.emptyList();
      }

      List<String> cached = read_List;
      if (cached != null) {
         return new ArrayList<>(cached);
      }

      List<String> nativeNames = read_List();
      List<String> names = new ArrayList<>();

      try {
         Object modulesSystem = load_Obj();
         Method getAllMethod = modulesSystem.getClass().getMethod("getAll");
         Object allModules = getAllMethod.invoke(modulesSystem);
         if (allModules instanceof Iterable) {
            for (Object module : (Iterable)allModules) {
               if (load(module, "name") instanceof String s && !s.isBlank() && !init(nativeNames, s) && !init(names, s)) {
                  names.add(s);
               }
            }
         }

         names.sort(String::compareToIgnoreCase);
         read_List = List.copyOf(names);
      } catch (Exception e) {
         AutismClientAddon.LOG.warn("[Autism] Failed to read Meteor modules reflectively.", e);
      }

      return names;
   }

   private static String write_Str() {
      try {
         Class<?> configClass = Class.forName("meteordevelopment.meteorclient.systems.config.Config");
         Object config = configClass.getMethod("get").invoke(null);
         Object prefixSetting = configClass.getField("prefix").get(config);
         if (prefixSetting.getClass().getMethod("get").invoke(prefixSetting) instanceof String stringPrefix && !stringPrefix.isBlank()) {
            return stringPrefix.trim();
         }
      } catch (Throwable var5) {
      }

      return ".";
   }

   public static List<String> update() {
      List<String> names = read_List();
      names.addAll(write_List());
      return names;
   }

   private static boolean init(List<String> values, String target) {
      if (values != null && target != null) {
         for (String value : values) {
            if (value != null && value.equalsIgnoreCase(target)) {
               return true;
            }
         }

         return false;
      } else {
         return false;
      }
   }

   public static boolean init(String moduleName, autismclient.util.macro.ak.Inner_StreamUtil toggleMode) {
      if (autismclient.modules.aD.init_Z() && !autismclient.modules.aD.init(moduleName)) {
         return false;
      }

      if (autismclient.modules.al.init_Z_Str_ak_StreamUtil(moduleName, toggleMode)) {
         return true;
      }

      if (init_Z() && moduleName != null && !moduleName.isBlank()) {
         try {
            Object modulesSystem = load_Obj();
            Method getAllMethod = modulesSystem.getClass().getMethod("getAll");
            Object allModules = getAllMethod.invoke(modulesSystem);
            Object targetModule = null;
            if (allModules instanceof Iterable) {
               for (Object module : (Iterable)allModules) {
                  if (load(module, "name") instanceof String s && moduleName.equalsIgnoreCase(s)) {
                     targetModule = module;
                     break;
                  }
               }
            }

            if (targetModule == null) {
               return false;
            }

            Method isActiveMethod = targetModule.getClass().getMethod("isActive");
            Method toggleMethod = targetModule.getClass().getMethod("toggle");
            boolean isActive = (Boolean)isActiveMethod.invoke(targetModule);
            switch (toggleMode) {
               case load:
                  if (!isActive) {
                     toggleMethod.invoke(targetModule);
                  }
                  break;
               case read:
                  if (isActive) {
                     toggleMethod.invoke(targetModule);
                  }
                  break;
               default:
                  toggleMethod.invoke(targetModule);
            }

            return true;
         } catch (Exception e) {
            AutismClientAddon.LOG.warn("[Autism] Failed to toggle Meteor module '{}' reflectively.", moduleName, e);
            return false;
         }
      } else {
         return false;
      }
   }

   private static aM init(Object meteorMacro) {
      if (meteorMacro == null) {
         return null;
      }

      try {
         aM macro = new aM();
         String name = null;
         List<String> messages = null;
         Object keybindObj = null;

         try {
            if (init(meteorMacro, "name") instanceof String s) {
               name = s;
            }
         } catch (Exception var20) {
         }

         try {
            Object msgVal = init(meteorMacro, "messages");
            if (msgVal instanceof List<?> list) {
               messages = new ArrayList<>();

               for (Object o : list) {
                  if (o instanceof String s) {
                     messages.add(s);
                  }
               }
            } else if (msgVal instanceof Iterable<?> iter) {
               messages = new ArrayList<>();

               for (Object o : iter) {
                  if (o instanceof String s) {
                     messages.add(s);
                  }
               }
            }
         } catch (Exception var23) {
         }

         try {
            keybindObj = init(meteorMacro, "keybind");
         } catch (Exception var19) {
         }

         if (name == null || messages == null) {
            try {
               if (load(meteorMacro, "settings") instanceof Iterable<?> groups) {
                  for (Object group : groups) {
                     if (group instanceof Iterable) {
                        for (Object setting : (Iterable)group) {
                           try {
                              String sName = (String)setting.getClass().getField("name").get(setting);
                              Method getM = setting.getClass().getMethod("get");
                              Object val = getM.invoke(setting);
                              if ("name".equals(sName) && name == null && val instanceof String s) {
                                 name = s;
                              } else if ("messages".equals(sName) && messages == null) {
                                 if (val instanceof List<?> list) {
                                    messages = new ArrayList<>();

                                    for (Object o : list) {
                                       if (o instanceof String s) {
                                          messages.add(s);
                                       }
                                    }
                                 }
                              } else if ("keybind".equals(sName) && keybindObj == null) {
                                 keybindObj = val;
                              }
                           } catch (Exception var21) {
                           }
                        }
                     }
                  }
               }
            } catch (Exception e) {
               AutismClientAddon.LOG.warn("[Autism] Settings iteration fallback failed.", e);
            }
         }

         if (name != null && !name.isBlank()) {
            macro.init_Str = name;
            if (messages != null) {
               for (int mi = 0; mi < messages.size(); mi++) {
                  String msg = messages.get(mi);
                  if (msg != null) {
                     String trimmed = msg.trim();
                     if (!trimmed.isEmpty()) {
                        if (trimmed.startsWith(".toggle ")) {
                           String modName = trimmed.substring(".toggle ".length()).trim();
                           if (!modName.isEmpty()) {
                              macro.init_List.add(new autismclient.util.macro.ak(modName));
                           }
                        } else {
                           if (trimmed.startsWith(".") && !trimmed.startsWith("./") && trimmed.length() > 1) {
                              macro.load_Str = macro.load_Str
                                 + (macro.load_Str.isEmpty() ? "" : "; ")
                                 + "Warning: '"
                                 + trimmed
                                 + "' is a Meteor command, may not work as chat";
                           }

                           macro.init_List.add(new autismclient.util.macro.W(msg));
                        }

                        if (mi < messages.size() - 1) {
                           macro.init_List.add(new autismclient.util.macro.EventBus(50));
                        }
                     }
                  }
               }
            }

            if (keybindObj != null) {
               try {
                  Method getValue = keybindObj.getClass().getMethod("getValue");
                  if (getValue.invoke(keybindObj) instanceof Integer k && k != -1 && k != 0) {
                     macro.load_I = k;
                  }
               } catch (Exception var18) {
               }
            }

            return macro;
         } else {
            return null;
         }
      } catch (Exception e) {
         AutismClientAddon.LOG.warn("[Autism] Error converting Meteor macro.", e);
         return null;
      }
   }

   private static Object init_Obj() throws ClassNotFoundException, IllegalAccessException, NoSuchMethodException, InvocationTargetException {
      Class<?> macrosClass = Class.forName("meteordevelopment.meteorclient.systems.macros.Macros");
      Method getMethod = macrosClass.getMethod("get");
      return getMethod.invoke(null);
   }

   private static Object load_Obj() throws ClassNotFoundException, IllegalAccessException, NoSuchMethodException, InvocationTargetException {
      Class<?> modulesClass = Class.forName("meteordevelopment.meteorclient.systems.modules.Modules");
      Method getMethod = modulesClass.getMethod("get");
      return getMethod.invoke(null);
   }

   private static Object init(Object owner, String fieldName) throws IllegalAccessException, NoSuchMethodException, NoSuchFieldException, InvocationTargetException {
      Object setting = load(owner, fieldName);
      if (setting == null) {
         return null;
      }

      Method getMethod = setting.getClass().getMethod("get");
      return getMethod.invoke(setting);
   }

   private static void init(Object owner, String fieldName, Object value) throws IllegalAccessException, NoSuchMethodException, NoSuchFieldException, InvocationTargetException {
      Object setting = load(owner, fieldName);
      if (setting != null) {
         Method setMethod = setting.getClass().getMethod("set", Object.class);
         setMethod.invoke(setting, value);
      }
   }

   private static Object load(Object owner, String fieldName) throws IllegalAccessException, NoSuchFieldException {
      Field field = owner.getClass().getField(fieldName);
      return field.get(owner);
   }

   private static boolean load(String className) {
      try {
         Class.forName(className);
         return true;
      } catch (Throwable ignored) {
         return false;
      }
   }
}

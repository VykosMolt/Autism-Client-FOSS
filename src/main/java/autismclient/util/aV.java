package autismclient.util;

import autismclient.AutismClientAddon;
import java.io.File;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;

public final class aV {
   private static final String init_Str = "autism-meteor-import.nbt";
   private static final String load = "meteor-client";
   private static final int init_I = 1;
   private static volatile boolean init_Z;

   private aV() {
   }

   public static void init() {
      if (!init_Z) {
         Minecraft mc = Minecraft.getInstance();
         if (mc != null && mc.gameDirectory != null) {
            synchronized (aV.class) {
               if (init_Z) {
                  return;
               }

               init_Z = true;
            }

            try {
               init_V(mc.gameDirectory);
            } catch (Throwable t) {
               AutismClientAddon.LOG.warn("Meteor import failed", t);
            }
         }
      }
   }

   private static void init_V(File gameDir) {
      File meteorDir = new File(gameDir, "meteor-client");
      if (meteorDir.isDirectory()) {
         aV.Inner_DataUtil state = init_aV_DataUtil(gameDir);
         boolean upgradeProxyImport = state.init_I < 1;
         if (upgradeProxyImport) {
            state.load_Str = "";
         }

         boolean changed = init_Z(new File(meteorDir, "accounts.nbt"), state);
         changed |= load(new File(meteorDir, "proxies.nbt"), state);
         if (upgradeProxyImport) {
            state.init_I = 1;
            changed = true;
         }

         if (changed) {
            init_V(gameDir, state);
         }
      }
   }

   private static boolean init_Z(File file, aV.Inner_DataUtil state) {
      if (!file.isFile()) {
         return false;
      }

      String signature = init_Str(file);
      if (signature.equals(state.init_Str)) {
         return false;
      }

      try {
         CompoundTag root = NbtIo.read(file.toPath());
         if (root != null) {
            for (Tag element : root.getListOrEmpty("accounts")) {
               if (element instanceof CompoundTag tag) {
                  BitSetUtil account = init_BitSetUtil(tag);
                  if (account != null && state.init_Set.add(init(account))) {
                     TokenStore.init_TokenStore().load(account);
                  }
               }
            }
         }
      } catch (Exception e) {
         AutismClientAddon.LOG.warn("Failed to read Meteor accounts", e);
         return !state.init_Set.isEmpty();
      }

      state.init_Str = signature;
      return true;
   }

   private static BitSetUtil init_BitSetUtil(CompoundTag tag) {
      String type = tag.getStringOr("type", "");
      String name = tag.getStringOr("name", "");
      String token = tag.getStringOr("token", "");
      String cacheUser = "";
      String cacheUuid = "";
      Optional<CompoundTag> cache = tag.getCompound("cache");
      if (cache.isPresent()) {
         cacheUser = cache.get().getStringOr("username", "");
         cacheUuid = cache.get().getStringOr("uuid", "");
      }

      BitSetUtil account = new BitSetUtil();
      account.sync = cacheUuid;
      account.update = cacheUser;
      switch (type) {
         case "Cracked":
            account.init_MethodUtil = MethodUtil.init;
            account.load_Str = name;
            if (account.update.isBlank()) {
               account.update = name;
            }
            break;
         case "Microsoft":
            account.init_MethodUtil = MethodUtil.read;
            account.load_Str = name;
            break;
         case "Session":
            account.init_MethodUtil = MethodUtil.load;
            account.load_Str = name;
            account.read_Str = token;
            break;
         case "TheAltening":
            account.init_MethodUtil = MethodUtil.write;
            account.load_Str = name;
            account.read_Str = token.isBlank() ? name : token;
            break;
         default:
            return null;
      }

      boolean usable = !account.load_Str.isBlank() || !account.update.isBlank() || !account.read_Str.isBlank();
      return usable ? account : null;
   }

   private static String init(BitSetUtil account) {
      String uuid = account.sync == null ? "" : account.sync.trim().toLowerCase(Locale.ROOT);
      if (!uuid.isEmpty()) {
         return account.init_MethodUtil.name() + "|u|" + uuid;
      }

      String identity = account.update != null && !account.update.isBlank() ? account.update : account.load_Str;
      return account.init_MethodUtil.name() + "|n|" + (identity == null ? "" : identity.trim().toLowerCase(Locale.ROOT));
   }

   private static boolean load(File file, aV.Inner_DataUtil state) {
      if (!file.isFile()) {
         return false;
      }

      String signature = init_Str(file);
      if (signature.equals(state.load_Str)) {
         return false;
      }

      try {
         CompoundTag root = NbtIo.read(file.toPath());
         if (root != null) {
            for (Tag element : root.getListOrEmpty("proxies")) {
               if (element instanceof CompoundTag tag) {
                  ProxyEntry proxy = init_bW(tag);
                  if (proxy != null && proxy.init_Z() && state.load_Set.add(init(proxy))) {
                     ProxyManager.init_bY().init_Z_bW(proxy);
                  }
               }
            }
         }
      } catch (Exception e) {
         AutismClientAddon.LOG.warn("Failed to read Meteor proxies", e);
         return !state.load_Set.isEmpty();
      }

      state.load_Str = signature;
      return true;
   }

   private static ProxyEntry init_bW(CompoundTag proxyTag) {
      Optional<CompoundTag> settings = proxyTag.getCompound("settings");
      return settings.isPresent() ? load(settings.get()) : read(proxyTag);
   }

   private static ProxyEntry load(CompoundTag settings) {
      Map<String, CompoundTag> byName = new HashMap<>();

      for (Tag groupTag : settings.getListOrEmpty("groups")) {
         if (groupTag instanceof CompoundTag group) {
            for (Tag settingTag : group.getListOrEmpty("settings")) {
               if (settingTag instanceof CompoundTag setting) {
                  String settingName = setting.getStringOr("name", "");
                  if (!settingName.isEmpty()) {
                     byName.put(settingName, setting);
                  }
               }
            }
         }
      }

      return init(
         init(byName, "address", ""),
         init(byName, "port", 0),
         init(byName, "name", ""),
         init(byName, "username", ""),
         init(byName, "password", ""),
         init(byName, "type", "Socks5")
      );
   }

   private static ProxyEntry read(CompoundTag tag) {
      return init(
         tag.getStringOr("address", ""),
         tag.getIntOr("port", 0),
         tag.getStringOr("name", ""),
         tag.getStringOr("username", ""),
         tag.getStringOr("password", ""),
         tag.getStringOr("type", "Socks5")
      );
   }

   private static ProxyEntry init(String address, int port, String name, String username, String password, String type) {
      if (address != null && !address.isBlank() && port > 0) {
         ProxyEntry proxy = new ProxyEntry();
         proxy.read = address.trim();
         proxy.init_I = port;
         proxy.load_Str = name == null ? "" : name;
         proxy.write = username == null ? "" : username;
         proxy.update = password == null ? "" : password;
         proxy.init_bZ = type != null && type.toLowerCase(Locale.ROOT).contains("4") ? ProxyType.init : ProxyType.load;
         proxy.init_Z = false;
         return proxy;
      } else {
         return null;
      }
   }

   private static String init(Map<String, CompoundTag> byName, String key, String def) {
      CompoundTag setting = byName.get(key);
      return setting == null ? def : setting.getStringOr("value", def);
   }

   private static int init(Map<String, CompoundTag> byName, String key, int def) {
      CompoundTag setting = byName.get(key);
      return setting == null ? def : setting.getIntOr("value", def);
   }

   private static String init(ProxyEntry proxy) {
      return proxy.init_bZ.name() + "|" + proxy.read.trim().toLowerCase(Locale.ROOT) + "|" + proxy.init_I;
   }

   private static aV.Inner_DataUtil init_aV_DataUtil(File gameDir) {
      aV.Inner_DataUtil state = new aV.Inner_DataUtil();
      File file = new File(gameDir, "autism-meteor-import.nbt");
      if (!file.isFile()) {
         return state;
      }

      try {
         CompoundTag tag = NbtIo.read(file.toPath());
         if (tag == null) {
            return state;
         }

         state.init_Str = tag.getStringOr("accountsSig", "");
         state.load_Str = tag.getStringOr("proxiesSig", "");
         state.init_I = tag.getIntOr("proxyImportVersion", 0);
         init(tag.getListOrEmpty("importedAccounts"), state.init_Set);
         init(tag.getListOrEmpty("importedProxies"), state.load_Set);
      } catch (Exception e) {
         AutismClientAddon.LOG.warn("Failed to read Meteor import state", e);
      }

      return state;
   }

   private static void init_V(File gameDir, aV.Inner_DataUtil state) {
      try {
         CompoundTag tag = new CompoundTag();
         tag.putString("accountsSig", state.init_Str);
         tag.putString("proxiesSig", state.load_Str);
         tag.putInt("proxyImportVersion", state.init_I);
         tag.put("importedAccounts", init(state.init_Set));
         tag.put("importedProxies", init(state.load_Set));
         NbtIo.write(tag, new File(gameDir, "autism-meteor-import.nbt").toPath());
      } catch (Exception e) {
         AutismClientAddon.LOG.warn("Failed to save Meteor import state", e);
      }
   }

   private static void init(ListTag list, Set<String> into) {
      for (Tag element : list) {
         String value = element.asString().orElse("");
         if (!value.isEmpty()) {
            into.add(value);
         }
      }
   }

   private static ListTag init(Set<String> keys) {
      ListTag list = new ListTag();

      for (String key : keys) {
         list.add(StringTag.valueOf(key));
      }

      return list;
   }

   private static String init_Str(File file) {
      return file.lastModified() + ":" + file.length();
   }

   private static final class Inner_DataUtil {
      String init_Str = "";
      String load_Str = "";
      int init_I = 0;
      final Set<String> init_Set = new LinkedHashSet<>();
      final Set<String> load_Set = new LinkedHashSet<>();
   }
}

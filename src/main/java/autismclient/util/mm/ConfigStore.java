package autismclient.util.mm;

import autismclient.api.module.Setting;
import autismclient.mixin.accessor.AbstractContainerScreenAccessor;
import autismclient.modules.Module;
import autismclient.modules.al;
import autismclient.util.bw;
import autismclient.util.ck;
import autismclient.util.o;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.mojang.serialization.JsonOps;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.network.protocol.Packet;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public final class ConfigStore {
   private static volatile Map<String, Class<? extends Packet<?>>> init_Map;
   private static final Set<String> init_Set = Set.of("gif", "auto-login", "schematic");
   private static final String init_Str = "autism_mm_blob";

   private ConfigStore() {
   }

   private static Provider init_HolderLookup_Provi() {
      return o.init_HolderLookup_Provi();
   }

   public static autismclient.util.mm.b.DataUtil.Inner_DataUtil init_DataUtil_DataUtil() {
      Screen screen = Minecraft.getInstance().gui.screen();
      if (!(screen instanceof AbstractContainerScreen)) {
         return null;
      }

      AbstractContainerMenu menu = ((AbstractContainerScreenAccessor)screen).autism$getMenu();
      if (menu == null) {
         return null;
      }

      try {
         Provider provider = init_HolderLookup_Provi();
         CompoundTag root = new CompoundTag();
         JsonElement titleJson = ComponentSerialization.CODEC
            .encodeStart(provider.createSerializationContext(JsonOps.INSTANCE), screen.getTitle())
            .result()
            .orElse(null);
         root.putString("title", titleJson != null ? titleJson.toString() : "");
         ListTag slots = new ListTag();

         for (Slot slot : menu.slots) {
            if (slot.x >= 0 && slot.y >= 0) {
               CompoundTag s = new CompoundTag();
               s.putInt("x", slot.x);
               s.putInt("y", slot.y);
               ItemStack st = slot.getItem();
               if (st != null && !st.isEmpty()) {
                  Tag enc = ItemStack.CODEC.encodeStart(provider.createSerializationContext(NbtOps.INSTANCE), st).result().orElse(null);
                  if (enc instanceof CompoundTag ct) {
                     s.put("item", ct);
                  }
               }

               slots.add(s);
            }
         }

         root.put("slots", slots);
         String data = init(root);
         if (data == null) {
            return null;
         }

         String name = screen.getTitle().getString();
         if (name == null || name.isBlank()) {
            name = "GUI";
         }

         return new autismclient.util.mm.b.DataUtil.Inner_DataUtil("gui", name, slots.size(), data);
      } catch (Throwable t) {
         return null;
      }
   }

   public static ConfigStore.Inner_StreamUtil init_ConfigStore_Stream(autismclient.util.mm.b.DataUtil.Inner_DataUtil b) {
      if (b != null && "gui".equals(b.init_Str)) {
         try {
            CompoundTag root = init_CompoundTag(b.read);
            if (root == null) {
               return null;
            }

            Provider provider = init_HolderLookup_Provi();

            Component title;
            try {
               String titleJson = root.getStringOr("title", "");
               JsonElement el = JsonParser.parseString(titleJson.isBlank() ? "\"\"" : titleJson);
               title = ComponentSerialization.CODEC.parse(provider.createSerializationContext(JsonOps.INSTANCE), el).result().orElse(Component.literal(b.load));
            } catch (Throwable t) {
               title = Component.literal(b.load);
            }

            ListTag slots = root.getList("slots").orElse(new ListTag());
            List<int[]> coords = new ArrayList<>();
            List<ItemStack> stacks = new ArrayList<>();
            int minX = 2147483647;
            int minY = 2147483647;
            int maxX = -2147483648;
            int maxY = -2147483648;

            for (int i = 0; i < slots.size(); i++) {
               if (slots.get(i) instanceof CompoundTag ct) {
                  int sx = ct.getIntOr("x", i % 9 * 18);
                  int sy = ct.getIntOr("y", i / 9 * 18);
                  if (sx >= 0 && sy >= 0) {
                     Tag item = ct.get("item");
                     if (item == null && (ct.contains("id") || ct.contains("count"))) {
                        item = ct;
                     }

                     ItemStack st = ItemStack.EMPTY;
                     if (item instanceof CompoundTag ic && !ic.isEmpty()) {
                        st = ItemStack.CODEC.parse(provider.createSerializationContext(NbtOps.INSTANCE), ic).result().orElse(ItemStack.EMPTY);
                     }

                     coords.add(new int[]{sx, sy});
                     stacks.add(st);
                     minX = Math.min(minX, sx);
                     minY = Math.min(minY, sy);
                     maxX = Math.max(maxX, sx);
                     maxY = Math.max(maxY, sy);
                  }
               }
            }

            if (coords.isEmpty()) {
               minX = 0;
               minY = 0;
               maxX = 0;
               maxY = 0;
            }

            List<ConfigStore.Inner_BufferHelper> views = new ArrayList<>(coords.size());

            for (int i = 0; i < coords.size(); i++) {
               views.add(new ConfigStore.Inner_BufferHelper(coords.get(i)[0] - minX, coords.get(i)[1] - minY, stacks.get(i)));
            }

            return new ConfigStore.Inner_StreamUtil(title, views, maxX - minX + 16, maxY - minY + 16);
         } catch (Throwable t) {
            return null;
         }
      } else {
         return null;
      }
   }

   public static autismclient.util.mm.b.DataUtil.Inner_DataUtil load() {
      LocalPlayer player = Minecraft.getInstance().player;
      if (player == null) {
         return null;
      }

      ItemStack st = player.getMainHandItem();
      if (st != null && !st.isEmpty()) {
         try {
            Tag enc = ItemStack.CODEC.encodeStart(init_HolderLookup_Provi().createSerializationContext(NbtOps.INSTANCE), st).result().orElse(null);
            if (enc instanceof CompoundTag ct) {
               CompoundTag root = new CompoundTag();
               root.put("item", ct);
               String data = init(root);
               return data == null ? null : new autismclient.util.mm.b.DataUtil.Inner_DataUtil("item", st.getHoverName().getString(), st.getCount(), data);
            } else {
               return null;
            }
         } catch (Throwable t) {
            return null;
         }
      } else {
         return null;
      }
   }

   public static ItemStack init_ItemStack(autismclient.util.mm.b.DataUtil.Inner_DataUtil b) {
      if (b != null && "item".equals(b.init_Str)) {
         try {
            CompoundTag root = init_CompoundTag(b.read);
            if (root == null) {
               return ItemStack.EMPTY;
            } else {
               return root.get("item") instanceof CompoundTag ct
                  ? ItemStack.CODEC.parse(init_HolderLookup_Provi().createSerializationContext(NbtOps.INSTANCE), ct).result().orElse(ItemStack.EMPTY)
                  : ItemStack.EMPTY;
            }
         } catch (Throwable t) {
            return ItemStack.EMPTY;
         }
      } else {
         return ItemStack.EMPTY;
      }
   }

   public static autismclient.util.mm.b.DataUtil.Inner_DataUtil read() {
      ck s = ck.init_ck();
      Set<Class<? extends Packet<?>>> c2s = s.load_Set();
      Set<Class<? extends Packet<?>>> s2c = s.read_Set();
      CompoundTag root = new CompoundTag();
      root.putString("c2s", init(c2s));
      root.putString("s2c", init(s2c));
      String data = init(root);
      return data == null ? null : new autismclient.util.mm.b.DataUtil.Inner_DataUtil("filter", "Packet Filter", c2s.size() + s2c.size(), data);
   }

   public static int init_I(autismclient.util.mm.b.DataUtil.Inner_DataUtil b) {
      if (b != null && "filter".equals(b.init_Str)) {
         CompoundTag root = init_CompoundTag(b.read);
         if (root == null) {
            return 0;
         }

         ck s = ck.init_ck();
         Set<Class<? extends Packet<?>>> c2s = new HashSet<>(s.load_Set());
         Set<Class<? extends Packet<?>>> s2c = new HashSet<>(s.read_Set());
         int added = init(root.getStringOr("c2s", ""), c2s) + init(root.getStringOr("s2c", ""), s2c);
         s.init_V_Set(c2s);
         s.load_V_Set(s2c);
         return added;
      } else {
         return 0;
      }
   }

   public static ConfigStore.Inner_DataUtil init_ConfigStore_DataUt(autismclient.util.mm.b.DataUtil.Inner_DataUtil b) {
      if (b != null && "filter".equals(b.init_Str)) {
         CompoundTag root = init_CompoundTag(b.read);
         return root == null
            ? new ConfigStore.Inner_DataUtil(List.of(), List.of())
            : new ConfigStore.Inner_DataUtil(init_List(root.getStringOr("c2s", "")), init_List(root.getStringOr("s2c", "")));
      } else {
         return new ConfigStore.Inner_DataUtil(List.of(), List.of());
      }
   }

   private static List<String> init_List(String joined) {
      List<String> out = new ArrayList<>();
      if (joined != null && !joined.isBlank()) {
         for (String raw : joined.split("\n")) {
            String n = raw.trim();
            if (!n.isEmpty()) {
               int dot = n.lastIndexOf(46);
               out.add(dot >= 0 ? n.substring(dot + 1) : n);
            }
         }

         out.sort(String.CASE_INSENSITIVE_ORDER);
         return out;
      } else {
         return out;
      }
   }

   private static String init(Set<Class<? extends Packet<?>>> set) {
      StringBuilder sb = new StringBuilder();

      for (Class<?> c : set) {
         if (sb.length() > 0) {
            sb.append('\n');
         }

         sb.append(c.getName());
      }

      return sb.toString();
   }

   private static Map<String, Class<? extends Packet<?>>> init_Map() {
      Map<String, Class<? extends Packet<?>>> map = init_Map;
      if (map == null) {
         Map<String, Class<? extends Packet<?>>> built = new HashMap<>();

         try {
            for (Class<? extends Packet<?>> c : bw.load()) {
               built.put(c.getName(), c);
            }

            for (Class<? extends Packet<?>> c : bw.init_Set()) {
               built.put(c.getName(), c);
            }
         } catch (Throwable var4) {
         }

         if (!built.isEmpty()) {
            init_Map = built;
         }

         map = built;
      }

      return map;
   }

   private static int init(String joined, Set<Class<? extends Packet<?>>> into) {
      if (joined != null && !joined.isBlank()) {
         Map<String, Class<? extends Packet<?>>> known = init_Map();
         int added = 0;

         for (String raw : joined.split("\n")) {
            String name = raw.trim();
            if (!name.isEmpty()) {
               Class<? extends Packet<?>> c = known.get(name);
               if (c != null && into.add(c)) {
                  added++;
               }
            }
         }

         return added;
      } else {
         return 0;
      }
   }

   public static autismclient.util.mm.b.DataUtil.Inner_DataUtil init(Module m) {
      if (m == null) {
         return null;
      }

      if (m.settingsShareable() && !init_Set.contains(m.id())) {
         try {
            CompoundTag root = new CompoundTag();
            root.putString("id", m.id());
            root.putString("name", m.name());
            CompoundTag s = new CompoundTag();

            for (Setting<?, ?> opt : m.settings()) {
               s.putString(opt.id(), m.value(opt.id()));
            }

            root.put("settings", s);
            String data = init(root);
            return data == null ? null : new autismclient.util.mm.b.DataUtil.Inner_DataUtil("module", m.name(), m.settings().size(), data);
         } catch (Throwable t) {
            return null;
         }
      } else {
         return null;
      }
   }

   public static ConfigStore.Inner_CachePool init_ConfigStore_CacheP(autismclient.util.mm.b.DataUtil.Inner_DataUtil b) {
      if (b != null && "module".equals(b.init_Str)) {
         CompoundTag root = init_CompoundTag(b.read);
         if (root == null) {
            return null;
         }

         String id = HashUtil.init(root.getStringOr("id", ""), 64);
         if (init_Z(id)) {
            return null;
         }

         String name = HashUtil.init(root.getStringOr("name", b.load), 48);
         CompoundTag s = root.getCompound("settings").orElse(new CompoundTag());
         List<String[]> settings = new ArrayList<>();

         for (String key : s.keySet()) {
            settings.add(new String[]{HashUtil.init(key, 48), HashUtil.init(s.getStringOr(key, ""), 96)});
         }

         settings.sort((a, c) -> a[0].compareToIgnoreCase(c[0]));
         return new ConfigStore.Inner_CachePool(id, name, settings);
      } else {
         return null;
      }
   }

   public static int load_I(autismclient.util.mm.b.DataUtil.Inner_DataUtil b) {
      if (b != null && "module".equals(b.init_Str)) {
         CompoundTag root = init_CompoundTag(b.read);
         if (root == null) {
            return -1;
         }

         Module m = al.init_Module_Str(root.getStringOr("id", ""));
         if (m == null) {
            return -1;
         }

         if (m.settingsShareable() && !init_Set.contains(m.id())) {
            CompoundTag s = root.getCompound("settings").orElse(new CompoundTag());
            int applied = 0;

            for (String key : s.keySet()) {
               if (m.setting(key) != null) {
                  m.setValue(key, s.getStringOr(key, ""));
                  applied++;
               }
            }

            return applied;
         } else {
            return -1;
         }
      } else {
         return -1;
      }
   }

   private static boolean init_Z(String id) {
      if (id == null || id.isBlank()) {
         return false;
      }

      if (init_Set.contains(id)) {
         return true;
      }

      Module m = al.init_Module_Str(id);
      return m != null && (!m.settingsShareable() || init_Set.contains(m.id()));
   }

   public static String init_Str(autismclient.util.mm.b.DataUtil.Inner_DataUtil b) {
      ConfigStore.Inner_CachePool mv = init_ConfigStore_CacheP(b);
      return mv == null ? (b == null ? "" : b.load) : mv.load();
   }

   public static autismclient.util.mm.b.DataUtil.Inner_DataUtil write() {
      LocalPlayer player = Minecraft.getInstance().player;
      if (player != null && player.level() != null) {
         String dim = player.level().dimension().identifier().toString();
         int x = (int)Math.floor(player.getX());
         int y = (int)Math.floor(player.getY());
         int z = (int)Math.floor(player.getZ());
         return new autismclient.util.mm.b.DataUtil.Inner_DataUtil("position", load(dim) + "  " + x + " " + y + " " + z, 0, x + " " + y + " " + z);
      } else {
         return null;
      }
   }

   public static autismclient.util.mm.b.DataUtil.Inner_DataUtil update() {
      Minecraft mc = Minecraft.getInstance();
      ServerData sd = mc.getCurrentServer();
      if (sd != null && sd.ip != null && !sd.ip.isBlank()) {
         String name = sd.name != null && !sd.name.isBlank() ? sd.name : sd.ip;
         int players = 0;
         int ping = -1;

         try {
            ClientPacketListener conn = mc.getConnection();
            if (conn != null && conn.getOnlinePlayers() != null) {
               players = conn.getOnlinePlayers().size();
            }

            if (conn != null && mc.player != null) {
               PlayerInfo info = conn.getPlayerInfo(mc.player.getUUID());
               if (info != null) {
                  ping = Math.max(0, Math.min(99999, info.getLatency()));
               }
            }
         } catch (Throwable var10) {
         }

         if (ping < 0) {
            try {
               ping = (int)Math.max(0L, Math.min(99999L, sd.ping));
            } catch (Throwable var9) {
            }
         }

         int maxPlayers = 0;

         try {
            if (sd.players != null) {
               maxPlayers = Math.max(0, sd.players.max());
            }
         } catch (Throwable var8) {
         }

         String motd = init(sd);
         String data = read(name) + "|" + read(sd.ip) + "|" + players + "|" + maxPlayers + "|" + ping + "|" + HashUtil.init(motd, 120);
         return new autismclient.util.mm.b.DataUtil.Inner_DataUtil("server", name, players, data);
      } else {
         return null;
      }
   }

   private static String init(ServerData sd) {
      try {
         Field f;
         try {
            f = ServerData.class.getField("motd");
         } catch (NoSuchFieldException e) {
            f = ServerData.class.getDeclaredField("motd");
         }

         f.setAccessible(true);
         if (f.get(sd) instanceof Component c) {
            String s = c.getString();
            return s == null ? "" : s.trim();
         }
      } catch (Throwable var5) {
      }

      return "";
   }

   private static String read(String s) {
      return HashUtil.init(s, 80).replace('|', ' ');
   }

   private static String[] init_StrArr(autismclient.util.mm.b.DataUtil.Inner_DataUtil b) {
      return b != null && b.read != null ? b.read.split("\\|", 6) : new String[0];
   }

   public static String load_Str(autismclient.util.mm.b.DataUtil.Inner_DataUtil b) {
      String[] p = init_StrArr(b);
      return p.length >= 1 ? p[0] : "";
   }

   public static String read_Str(autismclient.util.mm.b.DataUtil.Inner_DataUtil b) {
      String[] p = init_StrArr(b);
      return p.length >= 2 ? p[1] : "";
   }

   public static int read_I(autismclient.util.mm.b.DataUtil.Inner_DataUtil b) {
      String[] p = init_StrArr(b);
      if (p.length >= 3) {
         try {
            return Integer.parseInt(p[2].trim());
         } catch (NumberFormatException var3) {
         }
      }

      return b == null ? 0 : b.init_I;
   }

   public static int write_I(autismclient.util.mm.b.DataUtil.Inner_DataUtil b) {
      String[] p = init_StrArr(b);
      if (p.length >= 6) {
         try {
            return Math.max(0, Integer.parseInt(p[3].trim()));
         } catch (NumberFormatException var3) {
         }
      }

      return 0;
   }

   public static int update_I(autismclient.util.mm.b.DataUtil.Inner_DataUtil b) {
      String[] p = init_StrArr(b);
      if (p.length >= 6) {
         try {
            return Integer.parseInt(p[4].trim());
         } catch (NumberFormatException var3) {
         }
      }

      return -1;
   }

   public static String write_Str(autismclient.util.mm.b.DataUtil.Inner_DataUtil b) {
      String[] p = init_StrArr(b);
      if (p.length >= 6) {
         return p[5];
      } else {
         return p.length == 4 ? p[3] : "";
      }
   }

   public static String init_Str(String ip) {
      if (ip == null) {
         return "";
      }

      String s = ip.strip();
      return s.endsWith(":25565") ? s.substring(0, s.length() - ":25565".length()) : s;
   }

   public static String load(String dim) {
      if (dim == null) {
         return "?";
      }

      int i = dim.indexOf(58);
      return i >= 0 ? dim.substring(i + 1) : dim;
   }

   public static String update_Str(autismclient.util.mm.b.DataUtil.Inner_DataUtil b) {
      if (b == null) {
         return "";
      }

      CompoundTag root = new CompoundTag();
      root.putString("type", "autism_mm_blob");
      root.putString("kind", b.init_Str == null ? "" : b.init_Str);
      root.putString("name", b.load == null ? "" : b.load);
      root.putInt("count", b.init_I);
      root.putString("data", b.read == null ? "" : b.read);
      String s = init(root);
      return s == null ? "" : s;
   }

   public static autismclient.util.mm.b.DataUtil.Inner_DataUtil init_DataUtil_DataUtil(String base64) {
      try {
         CompoundTag root = init_CompoundTag(base64);
         if (root != null && "autism_mm_blob".equals(root.getStringOr("type", ""))) {
            String kind = root.getStringOr("kind", "");
            return kind.isBlank()
               ? null
               : new autismclient.util.mm.b.DataUtil.Inner_DataUtil(kind, root.getStringOr("name", ""), root.getIntOr("count", 0), root.getStringOr("data", ""));
         } else {
            return null;
         }
      } catch (Throwable t) {
         return null;
      }
   }

   private static String init(CompoundTag root) {
      try {
         ByteArrayOutputStream out = new ByteArrayOutputStream();
         NbtIo.writeCompressed(root, out);
         return Base64.getEncoder().encodeToString(out.toByteArray());
      } catch (Throwable t) {
         return null;
      }
   }

   private static CompoundTag init_CompoundTag(String base64) {
      try {
         byte[] bytes = Base64.getDecoder().decode(base64);
         return NbtIo.readCompressed(new ByteArrayInputStream(bytes), o.init_NbtAccounter());
      } catch (Throwable t) {
         return null;
      }
   }

   public record Inner_BufferHelper(int init_I, int load, ItemStack init_ItemStack) {
   }

   public record Inner_CachePool(String init_Str, String load, List<String[]> init_List) {
   }

   public record Inner_DataUtil(List<String> init, List<String> load) {
   }

   public record Inner_StreamUtil(Component init_Component, List<ConfigStore.Inner_BufferHelper> init_List, int init_I, int load) {
   }
}

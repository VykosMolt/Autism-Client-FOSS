package autismclient.ai;

import it.unimi.dsi.fastutil.longs.Long2ObjectLinkedOpenHashMap;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.OptionalInt;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.function.LongConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

public final class ca {
   private static final Map<Long, Long> init_Map = new LinkedHashMap<>();
   private static final Map<Long, Long> load_Map = new LinkedHashMap<>();
   private static final Map<Long, ca.Inner_DataUtil> read_Map = new HashMap<>();
   private static final List<Consumer<BlockPos>> init_List = new ArrayList<>();
   private static final List<LongConsumer> load_List = new ArrayList<>();
   private static long init_J;
   private static long load_J;
   private static long read_J;
   private static long write_J;
   private static Object init_Obj;
   private static Object load_Obj;
   private static UUID init_UUID;
   private static int init_I;
   private static int load_I;
   private static int read_I;
   private static float init_F;
   private static boolean init_Z;
   private static ca.Inner_BufferHelper init_ca_BufferHelper;
   private static long update_J;
   private static final Map<Integer, ca.Inner_StreamUtil> write_Map = new LinkedHashMap<>();
   private static final Map<Integer, Long> update_Map = new LinkedHashMap<>();
   private static final Map<Integer, Long> sync = new LinkedHashMap<>();
   private static final Map<Integer, Deque<ca.Inner_CachePool>> flush = new LinkedHashMap<>();
   private static final Deque<Map<String, Object>> init_Deque = new ArrayDeque<>();
   private static final Long2ObjectLinkedOpenHashMap<BlockState> init_Long2ObjectLinkedO = new Long2ObjectLinkedOpenHashMap<>();

   private ca() {
   }

   static void init_V_Consumer(Consumer<BlockPos> observer) {
      if (!init_List.contains(observer)) {
         init_List.add(observer);
      }
   }

   static void load_V_Consumer(Consumer<BlockPos> observer) {
      init_List.remove(observer);
   }

   public static void init_V_BlockPos(BlockPos pos) {
      Minecraft mc = Minecraft.getInstance();
      if (mc != null && mc.level != null && mc.isSameThread() && !init_List.isEmpty()) {
         long key = pos.asLong();
         if (!init_Long2ObjectLinkedO.containsKey(key)) {
            init_Long2ObjectLinkedO.put(key, mc.level.getBlockState(pos));
         }

         while (init_Long2ObjectLinkedO.size() > 4096) {
            init_Long2ObjectLinkedO.removeFirst();
         }
      }
   }

   public static BlockState init_BlockState_J(long key) {
      return init_Long2ObjectLinkedO.remove(key);
   }

   static void load_V_BlockPos(BlockPos pos) {
      if (!init_List.isEmpty()) {
         for (Consumer<BlockPos> observer : List.copyOf(init_List)) {
            observer.accept(pos);
         }
      }
   }

   static void init_V_LongConsumer(LongConsumer observer) {
      if (!load_List.contains(observer)) {
         load_List.add(observer);
      }
   }

   static void load_V_LongConsumer(LongConsumer observer) {
      load_List.remove(observer);
   }

   private static void read(int x, int z) {
      long key = autismclient.ai.a.IoWorker.init_J(x, z);

      for (LongConsumer observer : List.copyOf(load_List)) {
         observer.accept(key);
      }
   }

   public static void init_V(int x, int z) {
      write(x, z);
      read(x, z);
   }

   public static void read(BlockPos pos) {
      load(pos, null);
   }

   public static void init(BlockPos pos, BlockState state) {
      load(pos, Objects.requireNonNull(state));
   }

   private static void load(BlockPos pos, BlockState state) {
      long key = pos.asLong();
      long stamp = ++init_J;
      init_Map.put(key, stamp);
      if (state == null) {
         read_Map.remove(key);
      } else {
         read_Map.put(key, new ca.Inner_DataUtil(stamp, state));
      }

      if (Minecraft.getInstance() != null) {
         cM.init(pos);
      }

      while (init_Map.size() > 4096) {
         long oldest = init_Map.keySet().iterator().next();
         init_Map.remove(oldest);
         read_Map.remove(oldest);
      }

      load_V_BlockPos(pos);
   }

   public static ca.Inner_DataUtil init_ca_DataUtil_BlockPos(BlockPos pos) {
      return read_Map.get(pos.asLong());
   }

   private static void write(int x, int z) {
      read_Map.keySet().removeIf(key -> BlockPos.getX(key) >> 4 == x && BlockPos.getZ(key) >> 4 == z);
   }

   public static void init_V() {
      load_J++;
   }

   public static void init_V_I(int id) {
      load_J++;
      update_Map.put(id, load_J);
      sync.merge(id, 1L, Long::sum);

      while (update_Map.size() > 32) {
         int oldest = update_Map.keySet().iterator().next();
         update_Map.remove(oldest);
         sync.remove(oldest);
         flush.remove(oldest);
      }
   }

   public static void init(int id, List<ItemStack> contents) {
      Minecraft mc = Minecraft.getInstance();
      if (mc.player != null && id == mc.player.inventoryMenu.containerId) {
         InventoryMenu menu = mc.player.inventoryMenu;
         if (contents.size() != menu.slots.size()) {
            init_V_I(id);
         } else {
            Map<String, Integer> counts = new HashMap<>();

            for (int slot = 0; slot < Math.min(contents.size(), menu.slots.size()); slot++) {
               ItemStack stack = contents.get(slot);
               if (menu.slots.get(slot).container == mc.player.getInventory() && !stack.isEmpty()) {
                  String item = BuiltInRegistries.ITEM.getKey(stack.getItem()).toString();
                  counts.merge(item, stack.getCount(), Integer::sum);
               }
            }

            init(id, counts);
         }
      } else {
         init_V_I(id);
      }
   }

   static void init(int id, Map<String, Integer> counts) {
      init_V_I(id);
      Deque<ca.Inner_CachePool> history = flush.computeIfAbsent(id, key -> new ArrayDeque<>());
      history.addLast(new ca.Inner_CachePool(load_J_I(id), Map.copyOf(counts)));

      while (history.size() > 16) {
         history.removeFirst();
      }
   }

   static OptionalInt init_OptionalInt_I(int id, long afterReply, String item) {
      Deque<ca.Inner_CachePool> history = flush.get(id);
      return history == null
         ? OptionalInt.empty()
         : history.stream().filter(reply -> reply.reply() > afterReply).mapToInt(reply -> reply.counts().getOrDefault(item, 0)).min();
   }

   public static long init_J_I(int id) {
      return update_Map.getOrDefault(id, 0L);
   }

   public static long load_J_I(int id) {
      return sync.getOrDefault(id, 0L);
   }

   public static void load(int x, int z) {
      write(x, z);
      load_Map.put(autismclient.ai.a.IoWorker.init_J(x, z), ++init_J);

      while (load_Map.size() > 512) {
         load_Map.remove(load_Map.keySet().iterator().next());
      }

      read(x, z);
   }

   public static long init_J_BlockPos(BlockPos pos) {
      return Math.max(init_Map.getOrDefault(pos.asLong(), 0L), load_Map.getOrDefault(autismclient.ai.a.IoWorker.init_J(pos.getX() >> 4, pos.getZ() >> 4), 0L));
   }

   public static long init_J() {
      return load_J;
   }

   public static void init_V_I(int id, UUID identity, Vec3 velocity) {
      write_Map.put(id, new ca.Inner_StreamUtil(++update_J, identity, velocity));

      while (write_Map.size() > 256) {
         write_Map.remove(write_Map.keySet().iterator().next());
      }
   }

   public static ca.Inner_StreamUtil init_ca_StreamUtil_I(int id) {
      return write_Map.get(id);
   }

   public static void init(Object level, Object player, UUID identity, int tick, float preHealth, boolean onFire) {
      if (init_Z_Obj(level, player, identity, tick)) {
         init_ca_BufferHelper = null;
         if (load_I != 0 && init_Z(tick, init_I)) {
            load_I = Math.min(2, load_I + 1);
         } else {
            load_I = 1;
            init_I = tick;
            init_F = preHealth;
            init_Z = onFire;
         }
      }
   }

   public static void init(Object level, Object player, UUID identity, int tick, float health) {
      if (init_Z_Obj(level, player, identity, tick)) {
         init_ca_BufferHelper = null;
         float loss = init_F - health;
         if (load_I == 1 && init_Z && init_Z(tick, init_I) && Float.isFinite(init_F) && Float.isFinite(health) && loss > 0.0F && loss <= 1.0F) {
            init_ca_BufferHelper = new ca.Inner_BufferHelper(identity, init_I, tick, init_F, health);
         }

         load_I = 0;
         init_Z = false;
      }
   }

   public static boolean init_Z_UUID(UUID identity, int tick, float health) {
      return init_ca_BufferHelper != null
         && init_ca_BufferHelper.identity().equals(identity)
         && init_Z(tick, init_ca_BufferHelper.sourceTick())
         && init_Z(tick, init_ca_BufferHelper.healthTick())
         && Float.compare(health, init_ca_BufferHelper.health()) == 0;
   }

   public static boolean init_Z_UUID(UUID identity, int tick, float previousHealth, float health) {
      return init_Z_UUID(identity, tick, health) && Float.compare(previousHealth, init_ca_BufferHelper.before()) == 0;
   }

   private static boolean init_Z(int tick, int before) {
      long age = (long)tick - before;
      return age >= 0L && age <= 2L;
   }

   private static boolean init_Z_Obj(Object level, Object player, UUID identity, int tick) {
      if (level != null && player != null && identity != null) {
         if (level != init_Obj || player != load_Obj || !identity.equals(init_UUID) || tick < read_I) {
            write();
            init_Obj = level;
            load_Obj = player;
            init_UUID = identity;
         }

         read_I = tick;
         return true;
      } else {
         write();
         return false;
      }
   }

   private static void write() {
      load_Obj = null;
      init_Obj = null;
      init_UUID = null;
      read_I = 0;
      load_I = 0;
      init_I = 0;
      init_F = 0.0F;
      init_Z = false;
      init_ca_BufferHelper = null;
   }

   public static void load_V() {
      write_J++;
   }

   public static long load_J() {
      return write_J;
   }

   public static void init_V_Str(String text) {
      init_Deque.addLast(
         Map.of(
            "at", System.currentTimeMillis(), "revision", ++read_J, "source", "server-system-chat", "text", text.substring(0, Math.min(2048, text.length()))
         )
      );

      while (init_Deque.size() > 32) {
         init_Deque.removeFirst();
      }
   }

   public static long read_J() {
      return read_J;
   }

   public static List<Map<String, Object>> init_List_I(int limit) {
      return init_Deque.stream().skip(Math.max(0, init_Deque.size() - limit)).toList();
   }

   public static void read_V() {
      init_Map.clear();
      load_Map.clear();
      read_Map.clear();
      init_List.clear();
      load_List.clear();
      init_Deque.clear();
      update_Map.clear();
      sync.clear();
      flush.clear();
      PkgCls_b.read();
      write_Map.clear();
      write();
      update_J++;
      init_J++;
      load_J++;
      read_J++;
   }

   private record Inner_BufferHelper(UUID identity, int sourceTick, int healthTick, float before, float health) {
   }

   private record Inner_CachePool(long reply, Map<String, Integer> counts) {
   }

   public record Inner_DataUtil(long revision, BlockState state) {
      public Inner_DataUtil {
         Objects.requireNonNull(state);
      }
   }

   public record Inner_StreamUtil(long revision, UUID identity, Vec3 velocity) {
   }
}

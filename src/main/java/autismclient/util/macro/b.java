package autismclient.util.macro;

import autismclient.api.macro.AddonCondition;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.core.BlockPos.MutableBlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.ClipContext.Fluid;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.HitResult.Type;

public class b {
   private static final List<b.Inner_EventBus> init_List = Collections.synchronizedList(new ArrayList<>());
   private static final AtomicLong init_AtomicLong = new AtomicLong();
   private static volatile long init_J = 0L;
   private static volatile long load = 0L;

   public static long init_J() {
      return init_AtomicLong.get();
   }

   public static int init_I() {
      return init_List.size();
   }

   public static void init_V() {
      init_AtomicLong.incrementAndGet();
   }

   public static CompletableFuture<Void> init_CompletableFuture(String guiTitle) {
      Minecraft mc = Minecraft.getInstance();
      CompletableFuture<Void> future = new CompletableFuture<>();
      b.Inner_JsonUtil condition = new b.Inner_JsonUtil(guiTitle, mc.gui.screen(), future);
      init_List.add(condition);
      return future;
   }

   public static CompletableFuture<Void> load(String guiTitle) {
      CompletableFuture<Void> future = new CompletableFuture<>();
      init_List.add(new b.Inner_NetHelper(guiTitle, future));
      return future;
   }

   public static CompletableFuture<Void> init_CompletableFuture(Screen prevScreen) {
      CompletableFuture<Void> future = new CompletableFuture<>();
      init_List.add(new b.Inner_TaskQueue(prevScreen, future));
      return future;
   }

   public static CompletableFuture<Void> read(String itemName) {
      return init(ArrayUtil.update(itemName));
   }

   public static CompletableFuture<Void> init(ArrayUtil target) {
      CompletableFuture<Void> future = new CompletableFuture<>();
      b.Inner_UiHelper condition = new b.Inner_UiHelper(target, future);
      init_List.add(condition);
      Minecraft mc = Minecraft.getInstance();
      if (init_Z_Minecraft_b_EventBus(mc, condition)) {
         condition.init_V();
         init_List.remove(condition);
      }

      return future;
   }

   public static CompletableFuture<Void> init_CompletableFuture_F_Z(float threshold, boolean below) {
      CompletableFuture<Void> future = new CompletableFuture<>();
      b.Inner_SyncManager condition = new b.Inner_SyncManager(threshold, below, future);
      init_List.add(condition);
      Minecraft mc = Minecraft.getInstance();
      if (init_Z_Minecraft_b_EventBus(mc, condition)) {
         condition.init_V();
         init_List.remove(condition);
      }

      return future;
   }

   public static CompletableFuture<Void> init(at action) {
      CompletableFuture<Void> future = new CompletableFuture<>();
      b.Inner_StreamUtil condition = new b.Inner_StreamUtil(action, future);
      init_List.add(condition);
      Minecraft mc = Minecraft.getInstance();
      if (init_Z_Minecraft_b_EventBus(mc, condition)) {
         condition.init_V();
         init_List.remove(condition);
      }

      return future;
   }

   public static CompletableFuture<Void> init_CompletableFuture_Str_Z(String itemName, boolean checkMainHand) {
      return init_CompletableFuture_ArrayUtil_Z(ArrayUtil.update(itemName), checkMainHand);
   }

   public static CompletableFuture<Void> init_CompletableFuture_ArrayUtil_Z(ArrayUtil target, boolean checkMainHand) {
      CompletableFuture<Void> future = new CompletableFuture<>();
      b.Inner_CachePool condition = new b.Inner_CachePool(target, checkMainHand, future);
      init_List.add(condition);
      Minecraft mc = Minecraft.getInstance();
      if (init_Z_Minecraft_b_EventBus(mc, condition)) {
         condition.init_V();
         init_List.remove(condition);
      }

      return future;
   }

   public static CompletableFuture<Void> init(double x, double y, double z, double leeway, boolean checkRotation, float yaw, float pitch, float rotLeeway) {
      CompletableFuture<Void> future = new CompletableFuture<>();
      b.Inner_IoWorker condition = new b.Inner_IoWorker(x, y, z, leeway, checkRotation, yaw, pitch, rotLeeway, future);
      init_List.add(condition);
      Minecraft mc = Minecraft.getInstance();
      if (init_Z_Minecraft_b_EventBus(mc, condition)) {
         condition.init_V();
         init_List.remove(condition);
      }

      return future;
   }

   public static CompletableFuture<Void> init_CompletableFuture() {
      return init(new aG());
   }

   public static CompletableFuture<Void> init(aG action) {
      Minecraft mc = Minecraft.getInstance();
      CompletableFuture<Void> future = new CompletableFuture<>();
      b.Inner_PoolEntry condition = new b.Inner_PoolEntry(init_Str(mc), action == null ? "" : action.targetDimension, future);
      init_List.add(condition);
      return future;
   }

   public static CompletableFuture<Void> init(aI action) {
      CompletableFuture<Void> future = new CompletableFuture<>();
      b.Inner_ConfigStore condition = new b.Inner_ConfigStore(action, autismclient.util.W.init_W_StreamUtil(), future);
      init_List.add(condition);
      Minecraft mc = Minecraft.getInstance();
      if (init_Z_Minecraft_b_EventBus(mc, condition)) {
         condition.init_V();
         init_List.remove(condition);
      }

      return future;
   }

   public static CompletableFuture<Void> init_CompletableFuture_D_Z(double distance, boolean horizontalOnly) {
      Minecraft mc = Minecraft.getInstance();
      Vec3 origin = mc.player == null ? Vec3.ZERO : mc.player.position();
      return init_CompletableFuture_Vec3(origin, distance, horizontalOnly);
   }

   public static CompletableFuture<Void> init_CompletableFuture_Vec3(Vec3 origin, double distance, boolean horizontalOnly) {
      CompletableFuture<Void> future = new CompletableFuture<>();
      b.Inner_FileCache condition = new b.Inner_FileCache(origin, distance, horizontalOnly, future);
      init_List.add(condition);
      return future;
   }

   public static CompletableFuture<Void> load_CompletableFuture() {
      CompletableFuture<Void> future = new CompletableFuture<>();
      b.Inner_HashUtil condition = new b.Inner_HashUtil(future);
      init_List.add(condition);
      return future;
   }

   public static void init_V(Minecraft mc) {
      if (!init_List.isEmpty()) {
         Screen current = mc.gui.screen();
         List<b.Inner_EventBus> toRemove = new ArrayList<>();
         synchronized (init_List) {
            for (b.Inner_EventBus cond : init_List) {
               if (cond.init_Z()) {
                  toRemove.add(cond);
               } else if (cond instanceof b.Inner_JsonUtil gc) {
                  if (current != null && gc.init(current)) {
                     gc.init_V();
                     toRemove.add(gc);
                  }
               } else if (cond instanceof b.Inner_StateCache hic) {
                  if (hic.init_Z_Minecraft(mc)) {
                     hic.init_V();
                     toRemove.add(hic);
                  }
               } else if (cond instanceof b.Inner_UiHelper ic) {
                  if (ic.init_Z_Minecraft(mc)) {
                     ic.init_V();
                     toRemove.add(ic);
                  }
               } else if (cond instanceof b.Inner_ByteReader scc && scc.init_Z_Minecraft(mc)) {
                  scc.init_V();
                  toRemove.add(scc);
               }
            }

            init_List.removeAll(toRemove);
         }
      }
   }

   public static void init_V(int slot) {
      if (!init_List.isEmpty()) {
         Minecraft mc = Minecraft.getInstance();
         if (mc.player != null) {
            List<b.Inner_EventBus> toRemove = new ArrayList<>();
            synchronized (init_List) {
               for (b.Inner_EventBus cond : init_List) {
                  if (cond.init_Z()) {
                     toRemove.add(cond);
                  } else if (cond instanceof b.Inner_ByteReader scc) {
                     if (scc.init_Z_Minecraft(mc)) {
                        scc.init_V();
                        toRemove.add(scc);
                     }
                  } else if (cond instanceof b.Inner_StateCache hic) {
                     if (hic.init_Z_Minecraft(mc)) {
                        hic.init_V();
                        toRemove.add(hic);
                     }
                  } else if (cond instanceof b.Inner_UiHelper ic && ic.init_Z_Minecraft(mc)) {
                     ic.init_V();
                     toRemove.add(ic);
                  }
               }

               init_List.removeAll(toRemove);
            }
         }
      }
   }

   public static void load(Minecraft mc) {
      if (!init_List.isEmpty()) {
         List<b.Inner_EventBus> toRemove = new ArrayList<>();
         synchronized (init_List) {
            for (b.Inner_EventBus cond : init_List) {
               if (cond.init_Z()) {
                  toRemove.add(cond);
               } else {
                  try {
                     if (cond.init_Z_Minecraft(mc)) {
                        cond.init_V();
                        toRemove.add(cond);
                     }
                  } catch (Throwable t) {
                     cond.load_V();
                     toRemove.add(cond);
                  }
               }
            }

            init_List.removeAll(toRemove);
         }
      }
   }

   public static void init_V(Screen screen) {
      if (!init_List.isEmpty()) {
         List<b.Inner_EventBus> toRemove = new ArrayList<>();
         synchronized (init_List) {
            for (b.Inner_EventBus cond : init_List) {
               if (cond instanceof b.Inner_JsonUtil gc) {
                  if (gc.init(screen)) {
                     gc.init_V();
                     toRemove.add(gc);
                  }
               } else if (cond instanceof b.Inner_StateCache hic) {
                  Minecraft mc = Minecraft.getInstance();
                  if (hic.init_Z_Minecraft(mc)) {
                     hic.init_V();
                     toRemove.add(hic);
                  }
               } else if (cond instanceof b.Inner_ByteReader scc) {
                  Minecraft mc = Minecraft.getInstance();
                  if (scc.init_Z_Minecraft(mc)) {
                     scc.init_V();
                     toRemove.add(scc);
                  }
               }
            }

            init_List.removeAll(toRemove);
         }
      }
   }

   private static boolean init_Z_Minecraft_b_EventBus(Minecraft mc, b.Inner_EventBus condition) {
      if (mc != null && condition != null) {
         try {
            if (mc.isSameThread()) {
               return condition.init_Z_Minecraft(mc);
            }

            CompletableFuture<Boolean> result = new CompletableFuture<>();
            mc.execute(() -> {
               try {
                  result.complete(condition.init_Z_Minecraft(mc));
               } catch (Throwable t) {
                  result.complete(false);
               }
            });
            return Boolean.TRUE.equals(result.get(250L, TimeUnit.MILLISECONDS));
         } catch (Exception ignored) {
            return false;
         }
      } else {
         return false;
      }
   }

   private static void init_V_Minecraft_Runnable(Minecraft mc, Runnable task) {
      if (mc != null && task != null) {
         try {
            if (mc.isSameThread()) {
               task.run();
               return;
            }

            CompletableFuture<Void> result = new CompletableFuture<>();
            mc.execute(() -> {
               try {
                  task.run();
               } finally {
                  result.complete(null);
               }
            });
            result.get(250L, TimeUnit.MILLISECONDS);
         } catch (Exception var3) {
         }
      }
   }

   public static void load_V() {
      synchronized (init_List) {
         for (b.Inner_EventBus cond : init_List) {
            cond.load_V();
         }

         init_List.clear();
      }
   }

   public static boolean init_Z() {
      return !init_List.isEmpty();
   }

   public static boolean load_Z() {
      if (init_List.isEmpty()) {
         return false;
      }

      synchronized (init_List) {
         for (b.Inner_EventBus cond : init_List) {
            if (cond instanceof b.Inner_JsonUtil
               || cond instanceof b.Inner_StateCache
               || cond instanceof b.Inner_UiHelper
               || cond instanceof b.Inner_ByteReader) {
               return true;
            }
         }

         return false;
      }
   }

   public static boolean read_Z() {
      if (init_List.isEmpty()) {
         return false;
      }

      synchronized (init_List) {
         for (b.Inner_EventBus cond : init_List) {
            if (cond instanceof b.Inner_PacketUtil) {
               return true;
            }
         }

         return false;
      }
   }

   public static CompletableFuture<Void> init(AddonCondition condition) {
      CompletableFuture<Void> future = new CompletableFuture<>();
      if (condition == null) {
         future.complete(null);
         return future;
      }

      b.Inner_DataUtil adapter = new b.Inner_DataUtil(condition, future);
      init_List.add(adapter);
      Minecraft mc = Minecraft.getInstance();
      if (init_Z_Minecraft_b_EventBus(mc, adapter)) {
         adapter.init_V();
         init_List.remove(adapter);
      }

      return future;
   }

   public static void read_V() {
      init_J = System.nanoTime();
   }

   public static void write() {
      load = System.nanoTime();
   }

   private static String init_Str(Minecraft mc) {
      try {
         return mc != null && mc.level != null && mc.level.dimension() != null ? mc.level.dimension().identifier().toString() : "";
      } catch (Throwable ignored) {
         return "";
      }
   }

   private static boolean init_Z_Str_Str(String current, String target) {
      return target != null && !target.isBlank() ? init_Str(current).equals(init_Str(target)) : true;
   }

   private static String init_Str(String raw) {
      if (raw == null) {
         return "";
      } else {
         String value = raw.trim().toLowerCase(Locale.ROOT);
         if (value.isEmpty()) {
            return "";
         } else if (value.equals("nether") || value.equals("the_nether")) {
            return "minecraft:the_nether";
         } else if (value.equals("overworld") || value.equals("world")) {
            return "minecraft:overworld";
         } else if (!value.equals("end") && !value.equals("the_end")) {
            return !value.contains(":") ? "minecraft:" + value : value;
         } else {
            return "minecraft:the_end";
         }
      }
   }

   public static CompletableFuture<Void> init_CompletableFuture_I(int bufferMs, int maxWaitMs, boolean ignorePing) {
      CompletableFuture<Void> future = new CompletableFuture<>();
      b.Inner_TextFormat condition = new b.Inner_TextFormat(future, bufferMs, maxWaitMs, ignorePing);
      init_List.add(condition);
      return future;
   }

   public static CompletableFuture<Void> init(aw action) {
      CompletableFuture<Void> future = new CompletableFuture<>();
      b.Inner_BufferHelper condition = new b.Inner_BufferHelper(action, future);
      init_List.add(condition);
      Minecraft mc = Minecraft.getInstance();
      if (condition.init_Z_Minecraft(mc)) {
         condition.init_V();
         init_List.remove(condition);
      }

      return future;
   }

   public static CompletableFuture<Void> write(String itemName) {
      return load(ArrayUtil.update(itemName));
   }

   public static CompletableFuture<Void> load(ArrayUtil target) {
      CompletableFuture<Void> future = new CompletableFuture<>();
      b.Inner_StateCache condition = new b.Inner_StateCache(target, future);
      init_List.add(condition);
      Minecraft mc = Minecraft.getInstance();
      if (init_Z_Minecraft_b_EventBus(mc, condition)) {
         condition.init_V();
         init_List.remove(condition);
      }

      return future;
   }

   public static CompletableFuture<Void> init_CompletableFuture(int slotNumber) {
      return init(new aD(slotNumber));
   }

   public static CompletableFuture<Void> init(aE action) {
      CompletableFuture<Void> future = new CompletableFuture<>();
      init_List.add(new b.Inner_PacketUtil(action, future));
      return future;
   }

   public static void init(String soundId, double x, double y, double z) {
      if (!init_List.isEmpty()) {
         Minecraft mc = Minecraft.getInstance();
         List<b.Inner_EventBus> toRemove = new ArrayList<>();
         synchronized (init_List) {
            for (b.Inner_EventBus cond : init_List) {
               if (cond.init_Z()) {
                  toRemove.add(cond);
               } else if (cond instanceof b.Inner_PacketUtil sc && sc.init(mc, soundId, x, y, z)) {
                  sc.init_aE.init(soundId, x, y, z);
                  sc.init_V();
                  toRemove.add(sc);
               }
            }

            init_List.removeAll(toRemove);
         }
      }
   }

   public static CompletableFuture<Void> init(aD action) {
      Minecraft mc = Minecraft.getInstance();
      Map<Integer, String> initialNames = new HashMap<>();
      Map<Integer, Integer> initialCounts = new HashMap<>();
      init_V_Minecraft_Runnable(mc, () -> {
         if (mc.player != null) {
            AbstractContainerMenu handler = mc.player.containerMenu;

            for (int i = 0; i < action.entries.size(); i++) {
               aD.Inner_DataUtil e = action.entries.get(i);
               ArrayUtil entryTarget = e.init_ArrayUtil();
               if (e.init_aD_StreamUtil == aD.Inner_StreamUtil.ANY_CHANGE && entryTarget != null && entryTarget.init_Z()) {
                  int slotNum = entryTarget.init_I;
                  int handlerSlot = autismclient.util.aw.read_I(mc, slotNum);
                  if (handler != null && handlerSlot >= 0 && handlerSlot < handler.slots.size()) {
                     ItemStack s = handler.slots.get(handlerSlot).getItem();
                     initialNames.put(i, init(s));
                     initialCounts.put(i, s.isEmpty() ? 0 : s.getCount());
                  }
               }
            }
         }
      });
      CompletableFuture<Void> future = new CompletableFuture<>();
      b.Inner_ByteReader condition = new b.Inner_ByteReader(action, initialNames, initialCounts, future);
      init_List.add(condition);
      boolean hasAnyChange = action.entries.stream().anyMatch(e -> e.init_aD_StreamUtil == aD.Inner_StreamUtil.ANY_CHANGE);
      if (!hasAnyChange && init_Z_Minecraft_b_EventBus(mc, condition)) {
         condition.init_V();
         init_List.remove(condition);
      }

      return future;
   }

   private static String init(ItemStack stack) {
      if (stack != null && !stack.isEmpty()) {
         String id = BuiltInRegistries.ITEM.getKey(stack.getItem()).toString();
         String rich = MacroExecutor.init_Str_Component(stack.getHoverName());
         String components = String.valueOf(stack.getComponents());
         return id + "|" + stack.getCount() + "|" + (rich == null ? "" : rich) + "|" + components.hashCode();
      } else {
         return "";
      }
   }

   public static CompletableFuture<Void> init(az action) {
      CompletableFuture<Void> future = new CompletableFuture<>();
      b.Inner_RenderState cond = new b.Inner_RenderState(action, future);
      if (cond.load_Z()) {
         future.complete(null);
      } else {
         init_List.add(cond);
      }

      return future;
   }

   public static void update() {
      if (!init_List.isEmpty()) {
         List<b.Inner_EventBus> toRemove = new ArrayList<>();
         synchronized (init_List) {
            for (b.Inner_EventBus cond : init_List) {
               if (cond.init_Z()) {
                  toRemove.add(cond);
               } else if (cond instanceof b.Inner_RenderState lsc && lsc.load_Z()) {
                  lsc.init_V();
                  toRemove.add(lsc);
               }
            }

            init_List.removeAll(toRemove);
         }
      }
   }

   private static boolean init_Z_Str(String searchLower, String[] searchWords, String target) {
      String normalizedTarget = MacroExecutor.init_Str_Str(target);
      if (!searchLower.isEmpty() && !normalizedTarget.isEmpty()) {
         String tl = normalizedTarget.toLowerCase();
         if (searchLower.equals(tl)) {
            return true;
         }

         if (tl.contains(searchLower)) {
            return true;
         }

         if (searchWords.length == 0) {
            return false;
         }

         String[] targetWords = tl.split("\\s+");

         for (String word : searchWords) {
            boolean found = false;

            for (String tWord : targetWords) {
               if (tWord.contains(word) || word.contains(tWord)) {
                  found = true;
                  break;
               }
            }

            if (!found) {
               return false;
            }
         }

         return true;
      } else {
         return false;
      }
   }

   private static boolean load(String search, String target) {
      if (!search.isEmpty() && !target.isEmpty()) {
         if (search.equals(target)) {
            return true;
         }

         if (target.toLowerCase().contains(search.toLowerCase())) {
            return true;
         }

         String[] searchWords = search.toLowerCase().split("\\s+");
         String[] targetWords = target.toLowerCase().split("\\s+");

         for (String word : searchWords) {
            boolean found = false;

            for (String tWord : targetWords) {
               if (tWord.contains(word) || word.contains(tWord)) {
                  found = true;
                  break;
               }
            }

            if (!found) {
               return false;
            }
         }

         return true;
      } else {
         return false;
      }
   }

   static class Inner_BufferHelper implements b.Inner_EventBus {
      final aw init_aw;
      final Set<String> init_Set;
      final Set<String> load;
      final double init_D;
      final CompletableFuture<Void> init_CompletableFuture;

      Inner_BufferHelper(aw action, CompletableFuture<Void> future) {
         this.init_aw = action;
         this.init_Set = new HashSet<>();
         this.load = new HashSet<>();

         for (String entry : action.entityIds) {
            if (entry.startsWith("~")) {
               String[] p = entry.split("~", 4);
               if (p.length >= 2 && !p[1].isEmpty()) {
                  this.load.add(p[1]);
               }
            } else {
               this.init_Set.add(entry);
            }
         }

         this.init_D = action.radius * action.radius;
         this.init_CompletableFuture = future;
      }

      private boolean init(Entity entity) {
         if (this.init_aw.containerEntitiesOnly) {
            String typeId = BuiltInRegistries.ENTITY_TYPE.getKey(entity.getType()).toString();
            if (!typeId.contains("boat") && !typeId.contains("minecart") && !typeId.contains("llama") && !typeId.contains("chest")) {
               return false;
            }
         }

         if (this.init_Set.isEmpty() && this.load.isEmpty()) {
            return true;
         }

         if (!this.init_Set.isEmpty()) {
            String typeId = BuiltInRegistries.ENTITY_TYPE.getKey(entity.getType()).toString();
            if (this.init_Set.contains(typeId)) {
               return true;
            }
         }

         return !this.load.isEmpty() && this.load.contains(entity.getStringUUID());
      }

      @Override
      public boolean init_Z_Minecraft(Minecraft mc) {
         if (mc.level != null && mc.player != null) {
            switch (this.init_aw.checkMode) {
               case LOOKING_AT:
                  Entity targeted = mc.crosshairPickEntity;
                  if (targeted != null && targeted != mc.player) {
                     return this.init(targeted);
                  }

                  return false;
               case MOUNTED_IN:
                  Entity vehicle = mc.player.getVehicle();
                  return vehicle != null && this.init(vehicle);
               case WITHIN_REACH:
                  double reachSq = mc.player.blockInteractionRange() * mc.player.blockInteractionRange();

                  for (Entity entity : mc.level.entitiesForRendering()) {
                     if (entity != mc.player && !(entity.distanceToSqr(mc.player) > reachSq) && this.init(entity)) {
                        return true;
                     }
                  }

                  return false;
               case NEARBY:
               default:
                  Vec3 center = this.init_aw.centerOnPlayer
                     ? new Vec3(mc.player.getX(), mc.player.getY(), mc.player.getZ())
                     : new Vec3(this.init_aw.f_x_D, this.init_aw.f_y_D, this.init_aw.f_z_D);

                  for (Entity entity : mc.level.entitiesForRendering()) {
                     if (entity != mc.player && !(entity.distanceToSqr(center) > this.init_D) && this.init(entity)) {
                        return true;
                     }
                  }

                  return false;
            }
         } else {
            return false;
         }
      }

      @Override
      public void init_V() {
         this.init_CompletableFuture.complete(null);
      }

      @Override
      public void load_V() {
         this.init_CompletableFuture.cancel(true);
      }

      @Override
      public boolean init_Z() {
         return this.init_CompletableFuture.isCancelled();
      }
   }

   static class Inner_ByteReader implements b.Inner_EventBus {
      final aD init_aD;
      final Map<Integer, String> init_Map;
      final Map<Integer, Integer> load;
      final CompletableFuture<Void> init_CompletableFuture;

      Inner_ByteReader(aD action, Map<Integer, String> initialNames, Map<Integer, Integer> initialCounts, CompletableFuture<Void> future) {
         this.init_aD = action;
         this.init_Map = initialNames;
         this.load = initialCounts;
         this.init_CompletableFuture = future;
      }

      Inner_ByteReader(int slotNumber, String initialItemName, int initialCount, CompletableFuture<Void> future) {
         aD a = new aD(slotNumber);
         this.init_aD = a;
         this.init_Map = new HashMap<>();
         this.load = new HashMap<>();
         if (slotNumber >= 0) {
            this.init_Map.put(0, initialItemName);
            this.load.put(0, initialCount);
         }

         this.init_CompletableFuture = future;
      }

      @Override
      public boolean init_Z_Minecraft(Minecraft mc) {
         if (mc.player == null) {
            return false;
         }

         if (this.init_aD.entries.isEmpty()) {
            return true;
         }

         AbstractContainerMenu handler = mc.player.containerMenu;

         for (int i = 0; i < this.init_aD.entries.size(); i++) {
            if (!this.init(mc, handler, this.init_aD.entries.get(i), i)) {
               return false;
            }
         }

         return true;
      }

      private boolean init(Minecraft mc, AbstractContainerMenu handler, aD.Inner_DataUtil e, int entryIdx) {
         ArrayUtil entryTarget = e.init_ArrayUtil();
         if (entryTarget == null) {
            entryTarget = new ArrayUtil();
         }

         if (entryTarget.init_Z()) {
            int slotNum = entryTarget.init_I;
            if (handler == null) {
               return e.init_aD_StreamUtil == aD.Inner_StreamUtil.IS_EMPTY;
            } else {
               int handlerSlot = autismclient.util.aw.read_I(mc, slotNum);
               if (handlerSlot >= 0 && handlerSlot < handler.slots.size()) {
                  ItemStack stack = handler.slots.get(handlerSlot).getItem();
                  return this.init(stack, entryTarget, slotNum, e, entryIdx);
               } else {
                  return e.init_aD_StreamUtil == aD.Inner_StreamUtil.IS_EMPTY;
               }
            }
         } else if (e.init_aD_StreamUtil == aD.Inner_StreamUtil.IS_EMPTY) {
            if (handler != null) {
               for (Slot s : handler.slots) {
                  if (s != null && !s.getItem().isEmpty()) {
                     int visibleSlot = autismclient.util.aw.init_I(mc, s.index);
                     if (!entryTarget.update() || entryTarget.init_Z_ItemStack_I(s.getItem(), visibleSlot)) {
                        return false;
                     }
                  }
               }
            } else {
               for (int i = 0; i < mc.player.getInventory().getContainerSize(); i++) {
                  ItemStack stack = mc.player.getInventory().getItem(i);
                  if (!stack.isEmpty() && (!entryTarget.update() || entryTarget.init_Z_ItemStack_I(stack, i))) {
                     return false;
                  }
               }
            }

            return true;
         } else {
            if (handler != null) {
               for (Slot s : handler.slots) {
                  if (s != null && !s.getItem().isEmpty()) {
                     ItemStack stack = s.getItem();
                     int visibleSlot = autismclient.util.aw.init_I(mc, s.index);
                     if ((!entryTarget.update() || entryTarget.init_Z_ItemStack_I(stack, visibleSlot))
                        && this.init(stack, entryTarget, visibleSlot, e, entryIdx)) {
                        return true;
                     }
                  }
               }
            } else {
               for (int i = 0; i < mc.player.getInventory().getContainerSize(); i++) {
                  ItemStack stack = mc.player.getInventory().getItem(i);
                  if (!stack.isEmpty() && (!entryTarget.update() || entryTarget.init_Z_ItemStack_I(stack, i)) && this.init(stack, entryTarget, i, e, entryIdx)) {
                     return true;
                  }
               }
            }

            return false;
         }
      }

      private boolean init(ItemStack stack, ArrayUtil target, int visibleSlot, aD.Inner_DataUtil e, int entryIdx) {
         boolean nameMatches = target == null || !target.update() || !stack.isEmpty() && target.init_Z_ItemStack_I(stack, visibleSlot);
         boolean var10000;
         switch (e.init_aD_StreamUtil) {
            case NOT_EMPTY:
               var10000 = !stack.isEmpty() && nameMatches;
               break;
            case IS_EMPTY:
               var10000 = stack.isEmpty();
               break;
            case COUNT_AT_LEAST:
               var10000 = !stack.isEmpty() && nameMatches && stack.getCount() >= e.init_I;
               break;
            case COUNT_BELOW:
               var10000 = stack.isEmpty() || nameMatches && stack.getCount() < e.init_I;
               break;
            case ANY_CHANGE:
               String initName = this.init_Map.getOrDefault(entryIdx, "");
               int initCount = this.load.getOrDefault(entryIdx, 0);
               String curName = b.init(stack);
               int curCount = stack.isEmpty() ? 0 : stack.getCount();
               var10000 = !curName.equals(initName) || curCount != initCount;
               break;
            default:
               throw new MatchException(null, null);
         }

         return var10000;
      }

      @Override
      public void init_V() {
         this.init_CompletableFuture.complete(null);
      }

      @Override
      public void load_V() {
         this.init_CompletableFuture.cancel(true);
      }

      @Override
      public boolean init_Z() {
         return this.init_CompletableFuture.isCancelled();
      }
   }

   static class Inner_CachePool implements b.Inner_EventBus {
      final ArrayUtil init_ArrayUtil;
      final boolean init_Z;
      final CompletableFuture<Void> init_CompletableFuture;

      Inner_CachePool(ArrayUtil target, boolean checkMainHand, CompletableFuture<Void> future) {
         this.init_ArrayUtil = target == null ? new ArrayUtil() : target.init_ArrayUtil();
         this.init_Z = checkMainHand;
         this.init_CompletableFuture = future;
      }

      @Override
      public boolean init_Z_Minecraft(Minecraft mc) {
         if (mc.player == null) {
            return false;
         }

         if (this.init_ArrayUtil.update()) {
            boolean foundAny = false;

            for (int i = 0; i < mc.player.getInventory().getContainerSize(); i++) {
               ItemStack stack = mc.player.getInventory().getItem(i);
               if (!stack.isEmpty() && this.init_ArrayUtil.init_Z_ItemStack_I(stack, i)) {
                  foundAny = true;
                  if (mc.player.getCooldowns().isOnCooldown(stack)) {
                     return false;
                  }
               }
            }

            return foundAny;
         } else {
            ItemStack stack = this.init_Z ? mc.player.getMainHandItem() : mc.player.getOffhandItem();
            return stack.isEmpty() ? true : !mc.player.getCooldowns().isOnCooldown(stack);
         }
      }

      @Override
      public void init_V() {
         this.init_CompletableFuture.complete(null);
      }

      @Override
      public void load_V() {
         this.init_CompletableFuture.cancel(true);
      }

      @Override
      public boolean init_Z() {
         return this.init_CompletableFuture.isCancelled();
      }
   }

   static class Inner_ConfigStore implements b.Inner_EventBus {
      final aI init_aI;
      final autismclient.util.W.Inner_StreamUtil init_W_StreamUtil;
      final CompletableFuture<Void> init_CompletableFuture;
      private volatile boolean init_Z = false;

      Inner_ConfigStore(aI action, autismclient.util.W.Inner_StreamUtil baseline, CompletableFuture<Void> future) {
         this.init_aI = action == null ? new aI() : action;
         this.init_W_StreamUtil = baseline == null ? autismclient.util.W.init_W_StreamUtil() : baseline;
         this.init_CompletableFuture = future;
      }

      @Override
      public boolean init_Z_Minecraft(Minecraft mc) {
         autismclient.util.W.Inner_StreamUtil now = autismclient.util.W.init_W_StreamUtil();
         return now.init_J() > this.init_W_StreamUtil.init_J() && this.init_aI.init(this.init(now.init_GameType()))
            ? true
            : this.init_aI.detectFake && now.load_J() > this.init_W_StreamUtil.load_J() && this.init_aI.init(this.init(now.load_GameType()));
      }

      private GameType init(GameType mode) {
         return mode == null ? GameType.DEFAULT_MODE : mode;
      }

      @Override
      public void init_V() {
         this.init_CompletableFuture.complete(null);
      }

      @Override
      public void load_V() {
         this.init_Z = true;
         this.init_CompletableFuture.cancel(true);
      }

      @Override
      public boolean init_Z() {
         return this.init_Z || this.init_CompletableFuture.isCancelled();
      }
   }

   static final class Inner_DataUtil implements b.Inner_EventBus {
      private final AddonCondition init_AddonCondition;
      private final CompletableFuture<Void> init_CompletableFuture;

      Inner_DataUtil(AddonCondition condition, CompletableFuture<Void> future) {
         this.init_AddonCondition = condition;
         this.init_CompletableFuture = future;
      }

      @Override
      public boolean init_Z_Minecraft(Minecraft mc) {
         return this.init_AddonCondition.check(mc);
      }

      @Override
      public void init_V() {
         try {
            this.init_AddonCondition.onComplete();
         } catch (Throwable var2) {
         }

         this.init_CompletableFuture.complete(null);
      }

      @Override
      public void load_V() {
         this.init_CompletableFuture.cancel(true);
      }

      @Override
      public boolean init_Z() {
         return this.init_CompletableFuture.isCancelled();
      }
   }

   interface Inner_EventBus {
      boolean init_Z_Minecraft(Minecraft var1);

      void init_V();

      void load_V();

      boolean init_Z();
   }

   static class Inner_FileCache implements b.Inner_EventBus {
      final Vec3 init_Vec3;
      final double init_D;
      final boolean init_Z;
      final boolean load;
      final CompletableFuture<Void> init_CompletableFuture;
      private volatile boolean read = false;

      Inner_FileCache(Vec3 origin, double distance, boolean horizontalOnly, CompletableFuture<Void> future) {
         this.init_Vec3 = origin == null ? Vec3.ZERO : origin;
         double d = Math.max(0.0, distance);
         this.load = d <= 0.0;
         this.init_D = d * d;
         this.init_Z = horizontalOnly;
         this.init_CompletableFuture = future;
      }

      @Override
      public boolean init_Z_Minecraft(Minecraft mc) {
         if (mc.player == null) {
            return false;
         }

         Vec3 now = mc.player.position();
         double dx = now.x - this.init_Vec3.x;
         double dy = this.init_Z ? 0.0 : now.y - this.init_Vec3.y;
         double dz = now.z - this.init_Vec3.z;
         double d2 = dx * dx + dy * dy + dz * dz;
         return this.load ? d2 > 1.0E-4 : d2 >= this.init_D;
      }

      @Override
      public void init_V() {
         this.init_CompletableFuture.complete(null);
      }

      @Override
      public void load_V() {
         this.read = true;
         this.init_CompletableFuture.cancel(true);
      }

      @Override
      public boolean init_Z() {
         return this.read || this.init_CompletableFuture.isCancelled();
      }
   }

   static class Inner_HashUtil implements b.Inner_EventBus {
      final CompletableFuture<Void> init;

      Inner_HashUtil(CompletableFuture<Void> future) {
         this.init = future;
      }

      @Override
      public boolean init_Z_Minecraft(Minecraft mc) {
         return true;
      }

      @Override
      public void init_V() {
         this.init.complete(null);
      }

      @Override
      public void load_V() {
         this.init.cancel(true);
      }

      @Override
      public boolean init_Z() {
         return this.init.isCancelled();
      }
   }

   static class Inner_IoWorker implements b.Inner_EventBus {
      final double init_D;
      final double load_D;
      final double read_D;
      final double write;
      final boolean init_Z;
      final float init_F;
      final float load_F;
      final float read_F;
      final CompletableFuture<Void> init_CompletableFuture;

      Inner_IoWorker(
         double x, double y, double z, double leeway, boolean checkRotation, float yaw, float pitch, float rotLeeway, CompletableFuture<Void> future
      ) {
         this.init_D = x;
         this.load_D = y;
         this.read_D = z;
         this.write = leeway * leeway;
         this.init_Z = checkRotation;
         this.init_F = yaw;
         this.load_F = pitch;
         this.read_F = rotLeeway;
         this.init_CompletableFuture = future;
      }

      @Override
      public boolean init_Z_Minecraft(Minecraft mc) {
         if (mc.player == null) {
            return false;
         }

         double dx = mc.player.getX() - this.init_D;
         double dy = mc.player.getY() - this.load_D;
         double dz = mc.player.getZ() - this.read_D;
         if (dx * dx + dy * dy + dz * dz > this.write) {
            return false;
         }

         if (this.init_Z) {
            float currentYaw = Mth.wrapDegrees(mc.player.getYRot());
            float currentPitch = Mth.wrapDegrees(mc.player.getXRot());
            float targetYaw = Mth.wrapDegrees(this.init_F);
            float targetPitch = Mth.wrapDegrees(this.load_F);
            if (Math.abs(currentYaw - targetYaw) > this.read_F) {
               return false;
            }

            if (Math.abs(currentPitch - targetPitch) > this.read_F) {
               return false;
            }
         }

         return true;
      }

      @Override
      public void init_V() {
         this.init_CompletableFuture.complete(null);
      }

      @Override
      public void load_V() {
         this.init_CompletableFuture.cancel(true);
      }

      @Override
      public boolean init_Z() {
         return this.init_CompletableFuture.isCancelled();
      }
   }

   static class Inner_JsonUtil implements b.Inner_EventBus {
      final String init_Str;
      final String load;
      final String[] init_StrArr;
      final WeakReference<Screen> init_WeakReference;
      final CompletableFuture<Void> init_CompletableFuture;

      Inner_JsonUtil(String title, Screen prevScreen, CompletableFuture<Void> future) {
         this.init_Str = MacroExecutor.init_Str_Str(title);
         this.load = this.init_Str.toLowerCase();
         this.init_StrArr = this.load.isEmpty() ? new String[0] : this.load.split("\\s+");
         this.init_WeakReference = new WeakReference<>(prevScreen);
         this.init_CompletableFuture = future;
      }

      @Override
      public boolean init_Z_Minecraft(Minecraft mc) {
         return this.init(mc.gui.screen());
      }

      public boolean init(Screen screen) {
         if (screen == null) {
            return false;
         }

         if (f.init_Z(screen)) {
            return false;
         }

         if (screen == this.init_WeakReference.get()) {
            return false;
         }

         if (!this.load.isEmpty() && !f.init(screen, this.init_Str)) {
            return false;
         }

         if (!this.load.isEmpty()) {
            Minecraft mc = Minecraft.getInstance();
            if (mc.player != null
               && mc.player.containerMenu != null
               && mc.player.containerMenu != mc.player.inventoryMenu
               && screen instanceof AbstractContainerScreen) {
               return mc.player.containerMenu.getStateId() > 0;
            }
         }

         return true;
      }

      @Override
      public void init_V() {
         this.init_CompletableFuture.complete(null);
      }

      @Override
      public void load_V() {
         this.init_CompletableFuture.cancel(true);
      }

      @Override
      public boolean init_Z() {
         return this.init_CompletableFuture.isCancelled();
      }
   }

   static class Inner_NetHelper implements b.Inner_EventBus {
      final String init_Str;
      final String[] init_StrArr;
      final CompletableFuture<Void> init_CompletableFuture;
      private volatile boolean init_Z = false;
      private boolean load = false;

      Inner_NetHelper(String title, CompletableFuture<Void> future) {
         this.init_Str = MacroExecutor.init_Str_Str(title).toLowerCase();
         this.init_StrArr = this.init_Str.isEmpty() ? new String[0] : this.init_Str.split("\\s+");
         this.init_CompletableFuture = future;
      }

      @Override
      public boolean init_Z_Minecraft(Minecraft mc) {
         Screen screen = mc.gui.screen();
         if (screen != null && !this.init_Str.isEmpty() && f.init(screen, this.init_Str)) {
            this.load = true;
            return false;
         }

         if (this.init_Str.isEmpty()) {
            if (screen != null) {
               this.load = true;
               return false;
            } else {
               return this.load;
            }
         } else {
            return this.load || screen == null;
         }
      }

      @Override
      public void init_V() {
         this.init_CompletableFuture.complete(null);
      }

      @Override
      public void load_V() {
         this.init_Z = true;
         this.init_CompletableFuture.cancel(true);
      }

      @Override
      public boolean init_Z() {
         return this.init_Z || this.init_CompletableFuture.isCancelled();
      }
   }

   static class Inner_PacketUtil implements b.Inner_EventBus {
      final aE init_aE;
      final CompletableFuture<Void> init_CompletableFuture;
      private volatile boolean init_Z = false;

      Inner_PacketUtil(aE action, CompletableFuture<Void> future) {
         this.init_aE = action;
         this.init_CompletableFuture = future;
      }

      public boolean init(Minecraft mc, String soundId, double x, double y, double z) {
         if (!this.init_aE.soundIds.isEmpty()) {
            boolean found = false;
            String played = init_Str_Str(soundId);
            String playedPath = load(played);

            for (String id : this.init_aE.soundIds) {
               String expected = init_Str_Str(id);
               if (expected.equals(played) || expected.equals(playedPath) || load(expected).equals(played) || load(expected).equals(playedPath)) {
                  found = true;
                  break;
               }
            }

            if (!found) {
               return false;
            }
         }

         if (this.init_aE.checkDistance && mc != null && mc.player != null) {
            double dx = mc.player.getX() - x;
            double dy = mc.player.getY() - y;
            double dz = mc.player.getZ() - z;
            if (dx * dx + dy * dy + dz * dz > this.init_aE.maxDistance * this.init_aE.maxDistance) {
               return false;
            }
         }

         return true;
      }

      private static String init_Str_Str(String value) {
         return value == null ? "" : value.trim().toLowerCase(Locale.ROOT);
      }

      private static String load(String value) {
         String normalized = init_Str_Str(value);
         int split = normalized.indexOf(58);
         return split >= 0 && split + 1 < normalized.length() ? normalized.substring(split + 1) : normalized;
      }

      @Override
      public boolean init_Z_Minecraft(Minecraft mc) {
         return false;
      }

      @Override
      public void init_V() {
         this.init_CompletableFuture.complete(null);
      }

      @Override
      public void load_V() {
         this.init_Z = true;
         this.init_CompletableFuture.cancel(true);
      }

      @Override
      public boolean init_Z() {
         return this.init_Z || this.init_CompletableFuture.isCancelled();
      }
   }

   static class Inner_PoolEntry implements b.Inner_EventBus {
      final String init_Str;
      final String load;
      final long init_J;
      final CompletableFuture<Void> init_CompletableFuture;
      private volatile boolean init_Z = false;

      Inner_PoolEntry(String initialDimension, String targetDimension, CompletableFuture<Void> future) {
         this.init_Str = initialDimension == null ? "" : initialDimension;
         this.load = b.init_Str(targetDimension);
         this.init_CompletableFuture = future;
         this.init_J = System.nanoTime();
      }

      @Override
      public boolean init_Z_Minecraft(Minecraft mc) {
         if (b.init_J > this.init_J && this.load.isEmpty()) {
            return true;
         }

         String current = b.init_Str(mc);
         return !current.isEmpty() && !current.equals(this.init_Str)
            ? this.load.isEmpty() || b.init_Z_Str_Str(current, this.load)
            : b.init_J > this.init_J && !this.load.isEmpty() && !current.isEmpty() && b.init_Z_Str_Str(current, this.load);
      }

      @Override
      public void init_V() {
         this.init_CompletableFuture.complete(null);
      }

      @Override
      public void load_V() {
         this.init_Z = true;
         this.init_CompletableFuture.cancel(true);
      }

      @Override
      public boolean init_Z() {
         return this.init_Z || this.init_CompletableFuture.isCancelled();
      }
   }

   static class Inner_RenderState implements b.Inner_EventBus {
      final az init_az;
      final CompletableFuture<Void> init_CompletableFuture;
      private volatile boolean init_Z = false;

      Inner_RenderState(az action, CompletableFuture<Void> future) {
         this.init_az = action;
         this.init_CompletableFuture = future;
      }

      boolean load_Z() {
         autismclient.util.aE sync = autismclient.util.aE.init_aE();
         if (!sync.write_Z()) {
            return true;
         }

         String myUsername = sync.load_Str();
         Map<String, Integer> steps = sync.load_Map();
         if (!this.init_az.filterByUser) {
            return this.init(steps, myUsername, this.init_az.defaultStep);
         }

         if (this.init_az.entries.isEmpty()) {
            return this.init(steps, myUsername, this.init_az.defaultStep);
         }

         int peerCount = sync.load_I();

         for (az.Inner_DataUtil req : this.init_az.entries) {
            String targetUser = req.init_Str;
            if (peerCount <= 2 && !targetUser.isEmpty()) {
               boolean nameExists = false;

               for (String name : steps.keySet()) {
                  if (name.equals(targetUser) && !name.equals(myUsername)) {
                     nameExists = true;
                     break;
                  }
               }

               if (!nameExists) {
                  targetUser = "";
               }
            }

            if (targetUser.isEmpty()) {
               boolean anyReached = false;

               for (Entry<String, Integer> entry : steps.entrySet()) {
                  if (!entry.getKey().equals(myUsername) && entry.getValue() >= req.init_I) {
                     anyReached = true;
                     break;
                  }
               }

               if (!anyReached) {
                  return false;
               }
            } else {
               Map<String, ?> clients = sync.init_Map();
               if (clients.containsKey(targetUser)) {
                  int peerStep = steps.getOrDefault(targetUser, 0);
                  if (peerStep < req.init_I) {
                     return false;
                  }
               }
            }
         }

         return true;
      }

      private boolean init(Map<String, Integer> steps, String myUsername, int targetStep) {
         for (Entry<String, Integer> entry : steps.entrySet()) {
            if (!entry.getKey().equals(myUsername) && entry.getValue() >= targetStep) {
               return true;
            }
         }

         return false;
      }

      @Override
      public boolean init_Z_Minecraft(Minecraft mc) {
         return this.load_Z();
      }

      @Override
      public void init_V() {
         this.init_CompletableFuture.complete(null);
      }

      @Override
      public void load_V() {
         this.init_Z = true;
         this.init_CompletableFuture.cancel(true);
      }

      @Override
      public boolean init_Z() {
         return this.init_Z || this.init_CompletableFuture.isCancelled();
      }
   }

   static class Inner_StateCache implements b.Inner_EventBus {
      final ArrayUtil init_ArrayUtil;
      final CompletableFuture<Void> init_CompletableFuture;
      private volatile boolean init_Z = false;

      Inner_StateCache(ArrayUtil target, CompletableFuture<Void> future) {
         this.init_ArrayUtil = target == null ? new ArrayUtil() : target.init_ArrayUtil();
         this.init_CompletableFuture = future;
      }

      @Override
      public boolean init_Z_Minecraft(Minecraft mc) {
         if (mc.player != null && this.init_ArrayUtil.update()) {
            AbstractContainerMenu handler = mc.player.containerMenu;
            if (handler == null) {
               return false;
            }

            for (Slot slot : handler.slots) {
               if (slot != null && !slot.getItem().isEmpty()) {
                  int visibleSlot = autismclient.util.aw.init_I(mc, slot.index);
                  if (this.init_ArrayUtil.init_Z_ItemStack_I(slot.getItem(), visibleSlot)) {
                     return true;
                  }
               }
            }

            return false;
         } else {
            return false;
         }
      }

      @Override
      public void init_V() {
         this.init_CompletableFuture.complete(null);
      }

      @Override
      public void load_V() {
         this.init_Z = true;
         this.init_CompletableFuture.cancel(true);
      }

      @Override
      public boolean init_Z() {
         return this.init_Z || this.init_CompletableFuture.isCancelled();
      }
   }

   static class Inner_StreamUtil implements b.Inner_EventBus {
      final at init_at;
      final Set<String> init_Set;
      final CompletableFuture<Void> init_CompletableFuture;

      Inner_StreamUtil(at action, CompletableFuture<Void> future) {
         this.init_at = action;
         this.init_Set = new HashSet<>(action.blockIds);
         this.init_CompletableFuture = future;
      }

      @Override
      public boolean init_Z_Minecraft(Minecraft mc) {
         if (mc.level != null && mc.player != null) {
            boolean destroyed = this.init_at.waitBehavior == at.Inner_StreamUtil.DESTROYED;
            boolean anyBlock = this.init_at.anyBlock;
            switch (this.init_at.checkMode) {
               case AT_POSITION:
                  Block b = mc.level.getBlockState(this.init_at.blockPos).getBlock();
                  boolean isAir = b == Blocks.AIR;
                  if (destroyed) {
                     if (anyBlock) {
                        return isAir;
                     } else {
                        if (isAir) {
                           return true;
                        }

                        return !this.init_Set.contains(BuiltInRegistries.BLOCK.getKey(b).toString());
                     }
                  } else if (isAir) {
                     return false;
                  } else if (!anyBlock && !this.init_Set.isEmpty() && !this.init_Set.contains(BuiltInRegistries.BLOCK.getKey(b).toString())) {
                     return false;
                  } else {
                     if (this.init_at.mustBeInReach) {
                        return this.load(mc);
                     }

                     return true;
                  }
               case IN_REACH:
                  int r = (int)Math.ceil(this.init_at.searchRadius);
                  int cx = (int)mc.player.getX();
                  int cy = (int)mc.player.getY();
                  int cz = (int)mc.player.getZ();
                  MutableBlockPos mut = new MutableBlockPos();
                  if (destroyed) {
                     if (anyBlock) {
                        return false;
                     } else {
                        for (int dx = -r; dx <= r; dx++) {
                           for (int dy = -r; dy <= r; dy++) {
                              for (int dz = -r; dz <= r; dz++) {
                                 mut.set(cx + dx, cy + dy, cz + dz);
                                 Block bx = mc.level.getBlockState(mut).getBlock();
                                 if (bx != Blocks.AIR && this.init_Set.contains(BuiltInRegistries.BLOCK.getKey(bx).toString())) {
                                    return false;
                                 }
                              }
                           }
                        }

                        return true;
                     }
                  } else {
                     for (int dx = -r; dx <= r; dx++) {
                        for (int dy = -r; dy <= r; dy++) {
                           for (int dz = -r; dz <= r; dz++) {
                              mut.set(cx + dx, cy + dy, cz + dz);
                              Block bx = mc.level.getBlockState(mut).getBlock();
                              if (bx != Blocks.AIR) {
                                 if (anyBlock || this.init_Set.isEmpty()) {
                                    return true;
                                 }

                                 if (this.init_Set.contains(BuiltInRegistries.BLOCK.getKey(bx).toString())) {
                                    return true;
                                 }
                              }
                           }
                        }
                     }

                     return false;
                  }
               case LOOKING_AT:
                  if (destroyed) {
                     if (mc.hitResult != null && mc.hitResult.getType() == Type.BLOCK) {
                        if (!anyBlock && !this.init_Set.isEmpty()) {
                           BlockHitResult bhr = (BlockHitResult)mc.hitResult;
                           Block blk1 = mc.level.getBlockState(bhr.getBlockPos()).getBlock();
                           return !this.init_Set.contains(BuiltInRegistries.BLOCK.getKey(blk1).toString());
                        }

                        return false;
                     }

                     return anyBlock;
                  } else {
                     if (mc.hitResult != null && mc.hitResult.getType() == Type.BLOCK) {
                        BlockHitResult bhr = (BlockHitResult)mc.hitResult;
                        if (!anyBlock && !this.init_Set.isEmpty()) {
                           Block blk2 = mc.level.getBlockState(bhr.getBlockPos()).getBlock();
                           return this.init_Set.contains(BuiltInRegistries.BLOCK.getKey(blk2).toString());
                        }

                        return true;
                     }

                     return false;
                  }
               default:
                  return false;
            }
         } else {
            return false;
         }
      }

      private boolean load(Minecraft mc) {
         Vec3 center = Vec3.atCenterOf(this.init_at.blockPos);
         if (mc.player.distanceToSqr(center) > 36.0) {
            return false;
         }

         ClipContext ctx = new ClipContext(mc.player.getEyePosition(), center, net.minecraft.world.level.ClipContext.Block.OUTLINE, Fluid.NONE, mc.player);
         BlockHitResult result = mc.level.clip(ctx);
         return result.getType() == Type.BLOCK && result.getBlockPos().equals(this.init_at.blockPos);
      }

      @Override
      public void init_V() {
         this.init_CompletableFuture.complete(null);
      }

      @Override
      public void load_V() {
         this.init_CompletableFuture.cancel(true);
      }

      @Override
      public boolean init_Z() {
         return this.init_CompletableFuture.isCancelled();
      }
   }

   static class Inner_SyncManager implements b.Inner_EventBus {
      final float init_F;
      final boolean init_Z;
      final CompletableFuture<Void> init_CompletableFuture;

      Inner_SyncManager(float threshold, boolean below, CompletableFuture<Void> future) {
         this.init_F = threshold;
         this.init_Z = below;
         this.init_CompletableFuture = future;
      }

      @Override
      public boolean init_Z_Minecraft(Minecraft mc) {
         if (mc.player == null) {
            return false;
         }

         float health = mc.player.getHealth();
         return this.init_Z ? health < this.init_F : health > this.init_F;
      }

      @Override
      public void init_V() {
         this.init_CompletableFuture.complete(null);
      }

      @Override
      public void load_V() {
         this.init_CompletableFuture.cancel(true);
      }

      @Override
      public boolean init_Z() {
         return this.init_CompletableFuture.isCancelled();
      }
   }

   static class Inner_TaskQueue implements b.Inner_EventBus {
      final WeakReference<Screen> init_WeakReference;
      final CompletableFuture<Void> init_CompletableFuture;
      private volatile boolean init_Z = false;

      Inner_TaskQueue(Screen prevScreen, CompletableFuture<Void> future) {
         this.init_WeakReference = new WeakReference<>(prevScreen);
         this.init_CompletableFuture = future;
      }

      @Override
      public boolean init_Z_Minecraft(Minecraft mc) {
         return mc.gui.screen() != this.init_WeakReference.get();
      }

      @Override
      public void init_V() {
         this.init_CompletableFuture.complete(null);
      }

      @Override
      public void load_V() {
         this.init_Z = true;
         this.init_CompletableFuture.cancel(true);
      }

      @Override
      public boolean init_Z() {
         return this.init_Z || this.init_CompletableFuture.isCancelled();
      }
   }

   static class Inner_TextFormat implements b.Inner_EventBus {
      final CompletableFuture<Void> init_CompletableFuture;
      final int init_I;
      final int load;
      final boolean init_Z;
      final long init_J;

      Inner_TextFormat(CompletableFuture<Void> future, int bufferMs, int maxWaitMs, boolean ignorePing) {
         this.init_CompletableFuture = future;
         this.init_I = bufferMs;
         this.load = maxWaitMs;
         this.init_Z = ignorePing;
         this.init_J = System.nanoTime();
      }

      @Override
      public boolean init_Z_Minecraft(Minecraft mc) {
         long now = System.nanoTime();
         long elapsedMs = (now - this.init_J) / 1000000L;
         if (elapsedMs >= this.load) {
            return true;
         }

         if (!ab.init_Z()) {
            return false;
         }

         long optimalTime = ab.init(this.init_I, this.init_Z);
         return now >= optimalTime;
      }

      @Override
      public void init_V() {
         this.init_CompletableFuture.complete(null);
      }

      @Override
      public void load_V() {
         this.init_CompletableFuture.cancel(true);
      }

      @Override
      public boolean init_Z() {
         return this.init_CompletableFuture.isCancelled();
      }
   }

   static class Inner_UiHelper implements b.Inner_EventBus {
      final ArrayUtil init_ArrayUtil;
      final CompletableFuture<Void> init_CompletableFuture;

      Inner_UiHelper(ArrayUtil target, CompletableFuture<Void> future) {
         this.init_ArrayUtil = target == null ? new ArrayUtil() : target.init_ArrayUtil();
         this.init_CompletableFuture = future;
      }

      @Override
      public boolean init_Z_Minecraft(Minecraft mc) {
         if (mc.player != null && this.init_ArrayUtil.update()) {
            for (int i = 0; i < mc.player.getInventory().getContainerSize(); i++) {
               ItemStack stack = mc.player.getInventory().getItem(i);
               if (!stack.isEmpty() && this.init_ArrayUtil.init_Z_ItemStack_I(stack, i)) {
                  return true;
               }
            }

            return false;
         } else {
            return false;
         }
      }

      @Override
      public void init_V() {
         this.init_CompletableFuture.complete(null);
      }

      @Override
      public void load_V() {
         this.init_CompletableFuture.cancel(true);
      }

      @Override
      public boolean init_Z() {
         return this.init_CompletableFuture.isCancelled();
      }
   }
}

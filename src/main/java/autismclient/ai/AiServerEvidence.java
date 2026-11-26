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

public final class AiServerEvidence {
   private static final Map<Long, Long> blockRevisions = new LinkedHashMap<>();
   private static final Map<Long, Long> chunkRevisions = new LinkedHashMap<>();
   private static final Map<Long, AiServerEvidence.Inner_DataUtil> blockStates = new HashMap<>();
   private static final List<Consumer<BlockPos>> blockObservers = new ArrayList<>();
   private static final List<LongConsumer> chunkObservers = new ArrayList<>();
   private static long blockRevisionCounter;
   private static long inventoryRevisionCounter;
   private static long serverMessageRevisionCounter;
   private static long deathRevisionCounter;
   private static Object burnTrackLevel;
   private static Object burnTrackPlayer;
   private static UUID burnTrackIdentity;
   private static int burnDamageTick;
   private static int burnDamageCount;
   private static int burnLastTick;
   private static float burnPreHealth;
   private static boolean burnOnFire;
   private static AiServerEvidence.Inner_BufferHelper lastBurnDamage;
   private static long velocityRevisionCounter;
   private static final Map<Integer, AiServerEvidence.Inner_StreamUtil> entityVelocities = new LinkedHashMap<>();
   private static final Map<Integer, Long> containerRevisions = new LinkedHashMap<>();
   private static final Map<Integer, Long> containerReplyCounts = new LinkedHashMap<>();
   private static final Map<Integer, Deque<AiServerEvidence.Inner_CachePool>> containerReplyHistory = new LinkedHashMap<>();
   private static final Deque<Map<String, Object>> recentServerMessages = new ArrayDeque<>();
   private static final Long2ObjectLinkedOpenHashMap<BlockState> preChangeBlockStates = new Long2ObjectLinkedOpenHashMap<>();

   private AiServerEvidence() {
   }

   static void addBlockObserver(Consumer<BlockPos> observer) {
      if (!blockObservers.contains(observer)) {
         blockObservers.add(observer);
      }
   }

   static void removeBlockObserver(Consumer<BlockPos> observer) {
      blockObservers.remove(observer);
   }

   public static void snapshotBeforeBlockChange(BlockPos pos) {
      Minecraft mc = Minecraft.getInstance();
      if (mc != null && mc.level != null && mc.isSameThread() && !blockObservers.isEmpty()) {
         long key = pos.asLong();
         if (!preChangeBlockStates.containsKey(key)) {
            preChangeBlockStates.put(key, mc.level.getBlockState(pos));
         }

         while (preChangeBlockStates.size() > 4096) {
            preChangeBlockStates.removeFirst();
         }
      }
   }

   public static BlockState consumePreChangeState(long key) {
      return preChangeBlockStates.remove(key);
   }

   static void notifyBlockObservers(BlockPos pos) {
      if (!blockObservers.isEmpty()) {
         for (Consumer<BlockPos> observer : List.copyOf(blockObservers)) {
            observer.accept(pos);
         }
      }
   }

   static void addChunkObserver(LongConsumer observer) {
      if (!chunkObservers.contains(observer)) {
         chunkObservers.add(observer);
      }
   }

   static void removeChunkObserver(LongConsumer observer) {
      chunkObservers.remove(observer);
   }

   private static void notifyChunkObservers(int x, int z) {
      long key = autismclient.ai.engine.AiGridRevisions.init_J(x, z);

      for (LongConsumer observer : List.copyOf(chunkObservers)) {
         observer.accept(key);
      }
   }

   public static void recordChunkUnload(int x, int z) {
      evictChunkBlockStates(x, z);
      notifyChunkObservers(x, z);
   }

   public static void recordBlockUpdate(BlockPos pos) {
      applyBlockUpdate(pos, null);
   }

   public static void recordBlockStateUpdate(BlockPos pos, BlockState state) {
      applyBlockUpdate(pos, Objects.requireNonNull(state));
   }

   private static void applyBlockUpdate(BlockPos pos, BlockState state) {
      long key = pos.asLong();
      long stamp = ++blockRevisionCounter;
      blockRevisions.put(key, stamp);
      if (state == null) {
         blockStates.remove(key);
      } else {
         blockStates.put(key, new AiServerEvidence.Inner_DataUtil(stamp, state));
      }

      if (Minecraft.getInstance() != null) {
         AiWorldMemory.init(pos);
      }

      while (blockRevisions.size() > 4096) {
         long oldest = blockRevisions.keySet().iterator().next();
         blockRevisions.remove(oldest);
         blockStates.remove(oldest);
      }

      notifyBlockObservers(pos);
   }

   public static AiServerEvidence.Inner_DataUtil lastBlockUpdate(BlockPos pos) {
      return blockStates.get(pos.asLong());
   }

   private static void evictChunkBlockStates(int x, int z) {
      blockStates.keySet().removeIf(key -> BlockPos.getX(key) >> 4 == x && BlockPos.getZ(key) >> 4 == z);
   }

   public static void bumpInventoryRevision() {
      inventoryRevisionCounter++;
   }

   public static void recordContainerUpdate(int id) {
      inventoryRevisionCounter++;
      containerRevisions.put(id, inventoryRevisionCounter);
      containerReplyCounts.merge(id, 1L, Long::sum);

      while (containerRevisions.size() > 32) {
         int oldest = containerRevisions.keySet().iterator().next();
         containerRevisions.remove(oldest);
         containerReplyCounts.remove(oldest);
         containerReplyHistory.remove(oldest);
      }
   }

   public static void recordContainerContents(int id, List<ItemStack> contents) {
      Minecraft mc = Minecraft.getInstance();
      if (mc.player != null && id == mc.player.inventoryMenu.containerId) {
         InventoryMenu menu = mc.player.inventoryMenu;
         if (contents.size() != menu.slots.size()) {
            recordContainerUpdate(id);
         } else {
            Map<String, Integer> counts = new HashMap<>();

            for (int slot = 0; slot < Math.min(contents.size(), menu.slots.size()); slot++) {
               ItemStack stack = contents.get(slot);
               if (menu.slots.get(slot).container == mc.player.getInventory() && !stack.isEmpty()) {
                  String item = BuiltInRegistries.ITEM.getKey(stack.getItem()).toString();
                  counts.merge(item, stack.getCount(), Integer::sum);
               }
            }

            recordContainerItemCounts(id, counts);
         }
      } else {
         recordContainerUpdate(id);
      }
   }

   static void recordContainerItemCounts(int id, Map<String, Integer> counts) {
      recordContainerUpdate(id);
      Deque<AiServerEvidence.Inner_CachePool> history = containerReplyHistory.computeIfAbsent(id, key -> new ArrayDeque<>());
      history.addLast(new AiServerEvidence.Inner_CachePool(containerReplyCount(id), Map.copyOf(counts)));

      while (history.size() > 16) {
         history.removeFirst();
      }
   }

   static OptionalInt minItemCountAfterReply(int id, long afterReply, String item) {
      Deque<AiServerEvidence.Inner_CachePool> history = containerReplyHistory.get(id);
      return history == null
         ? OptionalInt.empty()
         : history.stream().filter(reply -> reply.reply() > afterReply).mapToInt(reply -> reply.counts().getOrDefault(item, 0)).min();
   }

   public static long containerRevision(int id) {
      return containerRevisions.getOrDefault(id, 0L);
   }

   public static long containerReplyCount(int id) {
      return containerReplyCounts.getOrDefault(id, 0L);
   }

   public static void recordChunkUpdate(int x, int z) {
      evictChunkBlockStates(x, z);
      chunkRevisions.put(autismclient.ai.engine.AiGridRevisions.init_J(x, z), ++blockRevisionCounter);

      while (chunkRevisions.size() > 512) {
         chunkRevisions.remove(chunkRevisions.keySet().iterator().next());
      }

      notifyChunkObservers(x, z);
   }

   public static long blockRevision(BlockPos pos) {
      return Math.max(blockRevisions.getOrDefault(pos.asLong(), 0L), chunkRevisions.getOrDefault(autismclient.ai.engine.AiGridRevisions.init_J(pos.getX() >> 4, pos.getZ() >> 4), 0L));
   }

   public static long inventoryRevision() {
      return inventoryRevisionCounter;
   }

   public static void recordEntityVelocity(int id, UUID identity, Vec3 velocity) {
      entityVelocities.put(id, new AiServerEvidence.Inner_StreamUtil(++velocityRevisionCounter, identity, velocity));

      while (entityVelocities.size() > 256) {
         entityVelocities.remove(entityVelocities.keySet().iterator().next());
      }
   }

   public static AiServerEvidence.Inner_StreamUtil lastEntityVelocity(int id) {
      return entityVelocities.get(id);
   }

   public static void recordDamageEvent(Object level, Object player, UUID identity, int tick, float preHealth, boolean onFire) {
      if (syncBurnTracker(level, player, identity, tick)) {
         lastBurnDamage = null;
         if (burnDamageCount != 0 && isWithinTwoTicks(tick, burnDamageTick)) {
            burnDamageCount = Math.min(2, burnDamageCount + 1);
         } else {
            burnDamageCount = 1;
            burnDamageTick = tick;
            burnPreHealth = preHealth;
            burnOnFire = onFire;
         }
      }
   }

   public static void recordHealthAfterDamage(Object level, Object player, UUID identity, int tick, float health) {
      if (syncBurnTracker(level, player, identity, tick)) {
         lastBurnDamage = null;
         float loss = burnPreHealth - health;
         if (burnDamageCount == 1 && burnOnFire && isWithinTwoTicks(tick, burnDamageTick) && Float.isFinite(burnPreHealth) && Float.isFinite(health) && loss > 0.0F && loss <= 1.0F) {
            lastBurnDamage = new AiServerEvidence.Inner_BufferHelper(identity, burnDamageTick, tick, burnPreHealth, health);
         }

         burnDamageCount = 0;
         burnOnFire = false;
      }
   }

   public static boolean matchesRecentBurnDamage(UUID identity, int tick, float health) {
      return lastBurnDamage != null
         && lastBurnDamage.identity().equals(identity)
         && isWithinTwoTicks(tick, lastBurnDamage.sourceTick())
         && isWithinTwoTicks(tick, lastBurnDamage.healthTick())
         && Float.compare(health, lastBurnDamage.health()) == 0;
   }

   public static boolean matchesRecentBurnDamage(UUID identity, int tick, float previousHealth, float health) {
      return matchesRecentBurnDamage(identity, tick, health) && Float.compare(previousHealth, lastBurnDamage.before()) == 0;
   }

   private static boolean isWithinTwoTicks(int tick, int before) {
      long age = (long)tick - before;
      return age >= 0L && age <= 2L;
   }

   private static boolean syncBurnTracker(Object level, Object player, UUID identity, int tick) {
      if (level != null && player != null && identity != null) {
         if (level != burnTrackLevel || player != burnTrackPlayer || !identity.equals(burnTrackIdentity) || tick < burnLastTick) {
            resetBurnTracker();
            burnTrackLevel = level;
            burnTrackPlayer = player;
            burnTrackIdentity = identity;
         }

         burnLastTick = tick;
         return true;
      } else {
         resetBurnTracker();
         return false;
      }
   }

   private static void resetBurnTracker() {
      burnTrackPlayer = null;
      burnTrackLevel = null;
      burnTrackIdentity = null;
      burnLastTick = 0;
      burnDamageCount = 0;
      burnDamageTick = 0;
      burnPreHealth = 0.0F;
      burnOnFire = false;
      lastBurnDamage = null;
   }

   public static void recordPlayerDeath() {
      deathRevisionCounter++;
   }

   public static long deathRevision() {
      return deathRevisionCounter;
   }

   public static void recordServerMessage(String text) {
      recentServerMessages.addLast(
         Map.of(
            "at", System.currentTimeMillis(), "revision", ++serverMessageRevisionCounter, "source", "server-system-chat", "text", text.substring(0, Math.min(2048, text.length()))
         )
      );

      while (recentServerMessages.size() > 32) {
         recentServerMessages.removeFirst();
      }
   }

   public static long serverMessageRevision() {
      return serverMessageRevisionCounter;
   }

   public static List<Map<String, Object>> recentServerMessages(int limit) {
      return recentServerMessages.stream().skip(Math.max(0, recentServerMessages.size() - limit)).toList();
   }

   public static void clearAll() {
      blockRevisions.clear();
      chunkRevisions.clear();
      blockStates.clear();
      blockObservers.clear();
      chunkObservers.clear();
      recentServerMessages.clear();
      containerRevisions.clear();
      containerReplyCounts.clear();
      containerReplyHistory.clear();
      AiContainerTransactions.read();
      entityVelocities.clear();
      resetBurnTracker();
      velocityRevisionCounter++;
      blockRevisionCounter++;
      inventoryRevisionCounter++;
      serverMessageRevisionCounter++;
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

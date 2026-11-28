package autismclient.modules;

import autismclient.AutismClientAddon;
import autismclient.compat263.CompatSwingPacket;
import autismclient.util.AutismLiteVariant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.common.ClientboundDisconnectPacket;
import net.minecraft.network.protocol.common.ClientboundKeepAlivePacket;
import net.minecraft.network.protocol.common.ClientboundPingPacket;
import net.minecraft.network.protocol.common.ServerboundKeepAlivePacket;
import net.minecraft.network.protocol.common.ServerboundPongPacket;
import net.minecraft.network.protocol.common.ServerboundResourcePackPacket;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBundlePacket;
import net.minecraft.network.protocol.game.ClientboundDisguisedChatPacket;
import net.minecraft.network.protocol.game.ClientboundLoginPacket;
import net.minecraft.network.protocol.game.ClientboundPlayerPositionPacket;
import net.minecraft.network.protocol.game.ClientboundRespawnPacket;
import net.minecraft.network.protocol.game.ClientboundSetHealthPacket;
import net.minecraft.network.protocol.game.ClientboundSoundPacket;
import net.minecraft.network.protocol.game.ClientboundStartConfigurationPacket;
import net.minecraft.network.protocol.game.ClientboundSystemChatPacket;
import net.minecraft.network.protocol.game.ServerboundAttackPacket;
import net.minecraft.network.protocol.game.ServerboundChatCommandPacket;
import net.minecraft.network.protocol.game.ServerboundChatCommandSignedPacket;
import net.minecraft.network.protocol.game.ServerboundChatPacket;
import net.minecraft.network.protocol.game.ServerboundInteractPacket;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket;
import net.minecraft.network.protocol.game.ServerboundPlayerActionPacket;
import net.minecraft.network.protocol.game.ServerboundPunchPacket;
import net.minecraft.network.protocol.game.ServerboundUseItemOnPacket;
import net.minecraft.network.protocol.game.ServerboundUseItemPacket;
import net.minecraft.server.RunningOnDifferentThreadException;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.phys.Vec3;

public final class BlinkPacketManager {
   private static final Minecraft MC = Minecraft.getInstance();
   private static volatile boolean holdIncoming;
   private static volatile boolean holdOutgoing;
   private static volatile boolean autoReleaseEnabled;
   private static volatile int autoReleaseTicks = 100;
   private static volatile boolean holdMovement = true;
   private static volatile boolean holdActions = true;
   private static volatile boolean hasOriginSnapshot;
   private static volatile boolean showClone = true;
   private static volatile double originX;
   private static volatile double originY;
   private static volatile double originZ;
   private static volatile float originYaw;
   private static volatile float originPitch;
   private static volatile float originHeadYaw;
   private static volatile float originBodyYaw;
   private static BlinkClonePlayer cloneEntity;
   private static boolean cloneNeedsSync;
   private static final Queue<Packet<?>> incomingQueue = new ConcurrentLinkedQueue<>();
   private static final Queue<Packet<?>> outgoingQueue = new ConcurrentLinkedQueue<>();
   private static final Set<Packet<?>> reinjectingOutgoingPackets = Collections.synchronizedSet(Collections.newSetFromMap(new IdentityHashMap<>()));
   private static final AtomicInteger reinjectingOutgoingCount = new AtomicInteger();
   private static volatile boolean flushIncomingNextTick;
   private static int heldTickCount;
   private static final List<BlinkPacketManager.BlinkPolicy> policies = new CopyOnWriteArrayList<>();
   private static volatile boolean policyFlushIncomingRequested;
   private static volatile boolean policyFlushOutgoingRequested;
   private static volatile long lastErrorLogMs;
   private static final long ERROR_LOG_COOLDOWN_MS = 5000L;

   private BlinkPacketManager() {
   }

   public static void registerPolicy(BlinkPacketManager.BlinkPolicy policy) {
      if (policy != null && !policies.contains(policy)) {
         policies.add(policy);
      }
   }

   public static void unregisterPolicy(BlinkPacketManager.BlinkPolicy policy) {
      if (policy != null) {
         policies.remove(policy);
      }
   }

   public static void requestPolicyFlush(boolean incoming, boolean outgoing) {
      if (incoming) {
         policyFlushIncomingRequested = true;
      }

      if (outgoing) {
         policyFlushOutgoingRequested = true;
      }
   }

   public static List<Vec3> getQueuedMovementPositions() {
      List<Vec3> positions = new ArrayList<>();

      for (Packet<?> packet : outgoingQueue) {
         if (packet instanceof ServerboundMovePlayerPacket move && move.hasPosition()) {
            positions.add(new Vec3(move.getX(0.0), move.getY(0.0), move.getZ(0.0)));
         }
      }

      return positions;
   }

   private static BlinkPacketManager.PacketDecision classifyWithPolicies(Packet<?> packet, boolean incoming) {
      BlinkPacketManager.PacketDecision merged = BlinkPacketManager.PacketDecision.FLUSH;

      for (BlinkPacketManager.BlinkPolicy policy : policies) {
         BlinkPacketManager.PacketDecision hold;
         try {
            hold = policy.classify(packet, incoming);
         } catch (Throwable t) {
            continue;
         }

         if (hold != null && hold.priority > merged.priority) {
            merged = hold;
         }
      }

      return merged;
   }

   public static void setHoldDirections(boolean incoming, boolean outgoing) {
      boolean wasIncoming = holdIncoming;
      boolean wasOutgoing = holdOutgoing;
      holdIncoming = incoming;
      holdOutgoing = outgoing;
      if (wasIncoming && !incoming && !incomingQueue.isEmpty() && (policies.isEmpty() || classifyWithPolicies(null, true) != BlinkPacketManager.PacketDecision.QUEUE)) {
         flushIncoming();
      }

      if (wasOutgoing && !outgoing && !outgoingQueue.isEmpty() && (policies.isEmpty() || classifyWithPolicies(null, false) != BlinkPacketManager.PacketDecision.QUEUE)) {
         flushOutgoing();
      }
   }

   public static boolean auditHoldingMovement() {
      return holdOutgoing && holdMovement;
   }

   public static void setAutoRelease(boolean enabled, int ticks) {
      autoReleaseEnabled = enabled;
      autoReleaseTicks = Math.max(1, ticks);
   }

   public static void setOutgoingHoldTypes(boolean movement, boolean actions) {
      boolean wasMovement = holdMovement;
      boolean wasActions = holdActions;
      holdMovement = movement;
      holdActions = actions;
      if (((wasMovement && !movement) || (wasActions && !actions))
         && !outgoingQueue.isEmpty()
         && (policies.isEmpty() || classifyWithPolicies(null, false) != BlinkPacketManager.PacketDecision.QUEUE)) {
         flushOutgoing();
      }
   }

   public static void setShowClone(boolean show) {
      showClone = show;
   }

   public static boolean isBlinking() {
      return holdIncoming || holdOutgoing;
   }

   public static boolean isHoldingActionsOnly() {
      return isHoldingActionsOnly(holdOutgoing, holdMovement, holdActions);
   }

   static boolean isHoldingActionsOnly(boolean outgoing, boolean movement, boolean actions) {
      return outgoing && actions && !movement;
   }

   public static int getQueuedPacketCount() {
      return incomingQueue.size() + outgoingQueue.size();
   }

   public static int getAutoReleaseTicksRemaining() {
      return autoReleaseEnabled && getQueuedPacketCount() > 0 ? Math.max(0, autoReleaseTicks - heldTickCount) : -1;
   }

   public static void captureOriginSnapshot() {
      LocalPlayer player = MC == null ? null : MC.player;
      if (player == null) {
         hasOriginSnapshot = false;
      } else {
         originX = player.getX();
         originY = player.getY();
         originZ = player.getZ();
         originYaw = player.getYRot();
         originPitch = player.getXRot();
         originHeadYaw = player.yHeadRot;
         originBodyYaw = player.yBodyRot;
         hasOriginSnapshot = true;
         cloneNeedsSync = true;
      }
   }

   private static void updateCloneEntity() {
      boolean want = holdOutgoing && holdMovement && showClone && hasOriginSnapshot && MC != null && MC.player != null && MC.level != null;
      if (!want) {
         discardCloneEntity();
      } else if (cloneEntity == null || cloneEntity.isRemoved() || cloneEntity.level() != MC.level) {
         spawnCloneEntity();
      } else if (cloneNeedsSync) {
         syncClonePose(cloneEntity);
         cloneNeedsSync = false;
      }
   }

   private static void spawnCloneEntity() {
      discardCloneEntity();
      LocalPlayer player = MC == null ? null : MC.player;
      ClientLevel level = MC == null ? null : MC.level;
      if (player != null && level != null) {
         try {
            BlinkClonePlayer fake = new BlinkClonePlayer(level, player);
            syncClonePose(fake);
            level.addEntity(fake);
            cloneEntity = fake;
            cloneNeedsSync = false;
         } catch (Throwable t) {
            cloneEntity = null;
            AutismClientAddon.LOG.warn("[Autism] Blink clone spawn failed", t);
         }
      }
   }

   private static void syncClonePose(BlinkClonePlayer fake) {
      fake.snapTo(originX, originY, originZ, originYaw, originPitch);
      fake.setRotations(originHeadYaw, originBodyYaw);
   }

   private static void discardCloneEntity() {
      if (cloneEntity != null) {
         try {
            cloneEntity.discard();
         } catch (Throwable var1) {
         }

         cloneEntity = null;
      }
   }

   private static void auditDiscardClone() {
      if (MC == null || MC.isSameThread()) {
         discardCloneEntity();
      } else {
         MC.execute(BlinkPacketManager::discardCloneEntity);
      }
   }

   public static void disableAndFlush() {
      holdIncoming = false;
      holdOutgoing = false;
      autoReleaseEnabled = false;
      flushAll();
      heldTickCount = 0;
      hasOriginSnapshot = false;
      auditDiscardClone();
   }

   public static void auditDisconnect() {
      holdIncoming = false;
      holdOutgoing = false;
      autoReleaseEnabled = false;
      clearQueues();
      auditDiscardClone();
   }

   public static void clearQueues() {
      incomingQueue.clear();
      outgoingQueue.clear();
      reinjectingOutgoingPackets.clear();
      reinjectingOutgoingCount.set(0);
      flushIncomingNextTick = false;
      policyFlushIncomingRequested = false;
      policyFlushOutgoingRequested = false;
      heldTickCount = 0;
      hasOriginSnapshot = false;
   }

   public static boolean interceptIncoming(Packet<?> packet) {
      if (auditIsDisconnect(packet)) {
         auditDisconnect();
         return false;
      } else if (auditIsStartConfiguration(packet)) {
         clearQueues();
         auditDiscardClone();
         return false;
      } else if (!policies.isEmpty() && classifyWithPolicies(packet, true) == BlinkPacketManager.PacketDecision.QUEUE) {
         if (isExemptIncomingChatOrHurt(packet) || isExemptIncomingKeepAlivePacket(packet)) {
            return false;
         } else if (triggersIncomingFlush(packet)) {
            policyFlushIncomingRequested = true;
            return false;
         } else {
            incomingQueue.add(packet);
            return true;
         }
      } else if (!holdIncoming || MC == null || MC.player == null) {
         return false;
      } else if (isExemptIncomingChatOrHurt(packet) || isExemptIncomingKeepAlivePacket(packet)) {
         return false;
      } else if (triggersIncomingFlush(packet)) {
         incomingQueue.add(packet);
         flushIncomingNextTick = true;
         return true;
      } else {
         incomingQueue.add(packet);
         return true;
      }
   }

   public static boolean interceptOutgoing(Packet<?> packet) {
      if (consumeReinjectingOutgoing(packet)) {
         return false;
      }

      if (!AutismLiteVariant.enabled() && !autismclient.e.PacketUtil.init()) {
         return false;
      }

      if (!policies.isEmpty() && classifyWithPolicies(packet, false) == BlinkPacketManager.PacketDecision.QUEUE) {
         if (!isExemptOutgoingChatPacket(packet) && !isExemptOutgoingControlPacket(packet)) {
            outgoingQueue.add(packet);
            return true;
         } else {
            return false;
         }
      } else {
         if (!holdOutgoing || MC == null || MC.player == null) {
            return false;
         }

         if (isExemptOutgoingChatPacket(packet) || isExemptOutgoingControlPacket(packet)) {
            return false;
         }

         if (!shouldHoldOutgoingPacket(packet)) {
            return false;
         }

         outgoingQueue.add(packet);
         return true;
      }
   }

   private static boolean shouldHoldOutgoingPacket(Packet<?> packet) {
      return holdMovement && packet instanceof ServerboundMovePlayerPacket ? true : holdActions && isHeldActionPacket(packet);
   }

   private static boolean isHeldActionPacket(Packet<?> packet) {
      return packet instanceof ServerboundPlayerActionPacket
         || packet instanceof ServerboundUseItemOnPacket
         || packet instanceof ServerboundUseItemPacket
         || packet instanceof ServerboundInteractPacket
         || packet instanceof ServerboundAttackPacket
         || packet instanceof ServerboundPunchPacket
         || packet instanceof CompatSwingPacket;
   }

   public static void processPolicyFlushes() {
      if ((!policies.isEmpty() || policyFlushIncomingRequested || policyFlushOutgoingRequested) && MC != null && MC.getConnection() != null) {
         if (!policies.isEmpty()) {
            if (!holdIncoming && classifyWithPolicies(null, true) == BlinkPacketManager.PacketDecision.FLUSH) {
               policyFlushIncomingRequested = true;
            }

            if (!holdOutgoing && classifyWithPolicies(null, false) == BlinkPacketManager.PacketDecision.FLUSH) {
               policyFlushOutgoingRequested = true;
            }
         }

         if (policyFlushIncomingRequested) {
            policyFlushIncomingRequested = false;
            flushIncoming();
         }

         if (policyFlushOutgoingRequested) {
            policyFlushOutgoingRequested = false;
            if (!holdOutgoing) {
               flushOutgoing();
            }
         }
      }
   }

   public static void tick() {
      if (!holdIncoming && !holdOutgoing && incomingQueue.isEmpty() && outgoingQueue.isEmpty() && reinjectingOutgoingCount.get() == 0 && !flushIncomingNextTick && cloneEntity == null) {
         heldTickCount = 0;
      } else if (MC != null && MC.getConnection() != null) {
         if (flushIncomingNextTick) {
            flushIncomingNextTick = false;
            flushIncoming();
         }

         if (getQueuedPacketCount() > 0) {
            heldTickCount++;
            if (autoReleaseEnabled && heldTickCount >= autoReleaseTicks) {
               flushAll();
               heldTickCount = 0;
               captureOriginSnapshot();
            }
         } else {
            heldTickCount = 0;
         }

         updateCloneEntity();
      } else {
         if (getQueuedPacketCount() > 0 || reinjectingOutgoingCount.get() > 0) {
            clearQueues();
         }

         discardCloneEntity();
      }
   }

   public static void flushAll() {
      flushIncoming();
      flushOutgoing();
   }

   public static void flushIncoming() {
      ClientPacketListener connection = MC == null ? null : MC.getConnection();

      Packet<?> packet;
      while ((packet = incomingQueue.poll()) != null) {
         if (connection != null) {
            dispatchIncomingPacket(connection, packet);
         }
      }
   }

   public static void flushOutgoing() {
      ClientPacketListener connection = MC == null ? null : MC.getConnection();

      Packet<?> packet;
      while ((packet = outgoingQueue.poll()) != null) {
         if (connection != null) {
            markReinjectingOutgoing(packet);

            try {
               connection.send(packet);
            } catch (Throwable t) {
               logRedispatchErrorThrottled("outgoing", packet, t);
            } finally {
               consumeReinjectingOutgoing(packet);
            }
         }
      }
   }

   private static void markReinjectingOutgoing(Packet<?> packet) {
      if (packet != null && reinjectingOutgoingPackets.add(packet)) {
         reinjectingOutgoingCount.incrementAndGet();
      }
   }

   private static boolean consumeReinjectingOutgoing(Packet<?> packet) {
      if (reinjectingOutgoingCount.get() == 0 || packet == null) {
         return false;
      }

      if (!reinjectingOutgoingPackets.remove(packet)) {
         return false;
      }

      reinjectingOutgoingCount.updateAndGet(count -> Math.max(0, count - 1));
      return true;
   }

   private static void dispatchIncomingPacket(ClientPacketListener connection, Packet<?> packet) {
      try {
         Packet<ClientGamePacketListener> gamePacket = (Packet<ClientGamePacketListener>)packet;
         gamePacket.handle(connection);
      } catch (RunningOnDifferentThreadException var3) {
      } catch (Throwable t) {
         logRedispatchErrorThrottled("incoming", packet, t);
      }
   }

   private static void logRedispatchErrorThrottled(String direction, Packet<?> packet, Throwable t) {
      if (!(t instanceof RunningOnDifferentThreadException) && !(t.getCause() instanceof RunningOnDifferentThreadException)) {
         long now = System.currentTimeMillis();
         if (now - lastErrorLogMs >= 5000L) {
            lastErrorLogMs = now;
            AutismClientAddon.LOG.warn("[Autism] Blink re-dispatch ({}) failed for {}", direction, packet.getClass().getSimpleName(), t);
         }
      }
   }

   private static boolean isExemptIncomingChatOrHurt(Packet<?> packet) {
      return packet instanceof ClientboundSystemChatPacket
         || packet instanceof ClientboundDisguisedChatPacket
         || packet instanceof ClientboundSoundPacket sound && sound.getSound().value() == SoundEvents.PLAYER_HURT;
   }

   private static boolean isExemptIncomingKeepAlivePacket(Packet<?> packet) {
      if (packet instanceof ClientboundBundlePacket bundle) {
         for (Packet<?> inner : bundle.subPackets()) {
            if (isExemptIncomingKeepAlivePacket(inner)) {
               return true;
            }
         }

         return false;
      }

      return packet instanceof ClientboundKeepAlivePacket || packet instanceof ClientboundPingPacket;
   }

   private static boolean auditIsStartConfiguration(Packet<?> packet) {
      if (packet instanceof ClientboundStartConfigurationPacket) {
         return true;
      }

      if (packet instanceof ClientboundBundlePacket bundle) {
         for (Packet<?> inner : bundle.subPackets()) {
            if (inner instanceof ClientboundStartConfigurationPacket) {
               return true;
            }
         }
      }

      return false;
   }

   private static boolean auditIsDisconnect(Packet<?> packet) {
      if (packet instanceof ClientboundDisconnectPacket) {
         return true;
      }

      if (packet instanceof ClientboundBundlePacket bundle) {
         for (Packet<?> inner : bundle.subPackets()) {
            if (inner instanceof ClientboundDisconnectPacket) {
               return true;
            }
         }
      }

      return false;
   }

   private static boolean triggersIncomingFlush(Packet<?> packet) {
      if (packet instanceof ClientboundBundlePacket bundle) {
         for (Packet<?> inner : bundle.subPackets()) {
            if (triggersIncomingFlush(inner)) {
               return true;
            }
         }

         return false;
      }

      return packet instanceof ClientboundPlayerPositionPacket
         || packet instanceof ClientboundRespawnPacket
         || packet instanceof ClientboundLoginPacket
         || packet instanceof ClientboundDisconnectPacket
         || packet instanceof ClientboundSetHealthPacket health && health.getHealth() <= 0.0F;
   }

   private static boolean isExemptOutgoingChatPacket(Packet<?> packet) {
      return packet instanceof ServerboundChatPacket
         || packet instanceof ServerboundChatCommandPacket
         || packet instanceof ServerboundChatCommandSignedPacket;
   }

   private static boolean isExemptOutgoingControlPacket(Packet<?> packet) {
      return packet instanceof ServerboundKeepAlivePacket || packet instanceof ServerboundPongPacket || packet instanceof ServerboundResourcePackPacket;
   }

   public enum PacketDecision {
      FLUSH(0),
      PASS(1),
      QUEUE(2);

      private final int priority;

      PacketDecision(int priority) {
         this.priority = priority;
      }
   }

   public interface BlinkPolicy {
      BlinkPacketManager.PacketDecision classify(Packet<?> var1, boolean var2);
   }
}

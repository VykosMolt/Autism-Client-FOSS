package autismclient.modules;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.time.Instant;
import java.util.BitSet;
import java.util.List;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import net.minecraft.commands.arguments.ArgumentSignatures;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.LastSeenMessages;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.common.ClientboundDisconnectPacket;
import net.minecraft.network.protocol.common.ClientboundKeepAlivePacket;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBundlePacket;
import net.minecraft.network.protocol.game.ClientboundSetHealthPacket;
import net.minecraft.network.protocol.game.ClientboundStartConfigurationPacket;
import net.minecraft.network.protocol.game.ServerboundAttackPacket;
import net.minecraft.network.protocol.game.ServerboundChatCommandSignedPacket;
import net.minecraft.network.protocol.game.ServerboundPunchPacket;

/**
 * Verifies MC 26.3 Blink packet classification, bundle & disconnect handling,
 * mid-blink toggle flush, and init_Set/init_AtomicInteger cleanup.
 */
public final class BlinkCheck {
   static int checks, failures;

   static void check(String what, boolean ok) {
      checks++;
      if (!ok) {
         failures++;
         System.out.println("FAIL BlinkCheck: " + what);
      }
   }

   @SuppressWarnings("unchecked")
   public static void main(String[] args) throws Exception {
      Method write = BlinkPacketManager.class.getDeclaredMethod("isHeldActionPacket", Packet.class);
      write.setAccessible(true);
      Method closePred = BlinkPacketManager.class.getDeclaredMethod("triggersIncomingFlush", Packet.class);
      closePred.setAccessible(true);
      Method openPred = BlinkPacketManager.class.getDeclaredMethod("isExemptOutgoingChatPacket", Packet.class);
      openPred.setAccessible(true);
      Method flushPred = BlinkPacketManager.class.getDeclaredMethod("isExemptIncomingKeepAlivePacket", Packet.class);
      flushPred.setAccessible(true);

      Field inQueueF = BlinkPacketManager.class.getDeclaredField("incomingQueue");
      inQueueF.setAccessible(true);
      Queue<Packet<?>> inQueue = (Queue<Packet<?>>) inQueueF.get(null);

      Field outQueueF = BlinkPacketManager.class.getDeclaredField("outgoingQueue");
      outQueueF.setAccessible(true);
      Queue<Packet<?>> outQueue = (Queue<Packet<?>>) outQueueF.get(null);

      Field inSetF = BlinkPacketManager.class.getDeclaredField("reinjectingOutgoingPackets");
      inSetF.setAccessible(true);
      Set<Packet<?>> inSet = (Set<Packet<?>>) inSetF.get(null);

      Field counterF = BlinkPacketManager.class.getDeclaredField("reinjectingOutgoingCount");
      counterF.setAccessible(true);
      AtomicInteger counter = (AtomicInteger) counterF.get(null);

      // 1. MC 26.3 attack & punch packets are held under Hold Attack/Interact
      check("ServerboundAttackPacket held", (Boolean) write.invoke(null, new ServerboundAttackPacket(42)));
      check("ServerboundPunchPacket held", (Boolean) write.invoke(null, ServerboundPunchPacket.INSTANCE));

      // 2. Signed chat commands are exempt in open()
      Packet<?> signedCmd = new ServerboundChatCommandSignedPacket(
         "msg friend hi", Instant.EPOCH, 0L, ArgumentSignatures.EMPTY, new LastSeenMessages.Update(0, new BitSet(), (byte) 0));
      check("ServerboundChatCommandSignedPacket exempt in open()", (Boolean) openPred.invoke(null, signedCmd));

      // 3. ClientboundBundlePacket inspects inner packets for close()
      Packet<ClientGamePacketListener> lethal = new ClientboundSetHealthPacket(0.0F, 20, 0.0F);
      Packet<ClientGamePacketListener> alive = new ClientboundSetHealthPacket(20.0F, 20, 5.0F);
      check("bundled lethal health triggers close()", (Boolean) closePred.invoke(null, new ClientboundBundlePacket(List.of(alive, lethal))));
      check("bundled alive health does not trigger close()", !(Boolean) closePred.invoke(null, new ClientboundBundlePacket(List.of(alive))));

      // 4. Bare and bundled ClientboundDisconnectPacket drop queues and pass through (never cancelled)
      BlinkPacketManager.setHoldDirections(true, true);
      inQueue.add(alive);
      outQueue.add(new ServerboundAttackPacket(7));
      boolean heldBareDisconnect = BlinkPacketManager.interceptIncoming(new ClientboundDisconnectPacket(Component.literal("kicked")));
      check("bare disconnect passes through", !heldBareDisconnect);
      check("bare disconnect clears Blink flags and queues", !BlinkPacketManager.isBlinking() && inQueue.isEmpty() && outQueue.isEmpty());

      BlinkPacketManager.setHoldDirections(true, true);
      inQueue.add(alive);
      outQueue.add(new ServerboundAttackPacket(8));
      Packet<?> bundledDisconnect = new ClientboundBundlePacket(List.of(alive, new ClientboundDisconnectPacket(Component.literal("kicked"))));
      boolean heldBundledDisconnect = BlinkPacketManager.interceptIncoming(bundledDisconnect);
      check("bundled disconnect passes through", !heldBundledDisconnect);
      check("bundled disconnect clears Blink flags and queues", !BlinkPacketManager.isBlinking() && inQueue.isEmpty() && outQueue.isEmpty());

      // 5. Mid-blink toggle of incoming/outgoing drains queues cleanly
      BlinkPacketManager.setHoldDirections(true, true);
      BlinkPacketManager.setOutgoingHoldTypes(true, true);
      check("auditHoldingMovement true when outgoing+hold-movement", BlinkPacketManager.auditHoldingMovement());
      inQueue.add(alive);
      outQueue.add(new ServerboundAttackPacket(9));
      BlinkPacketManager.setHoldDirections(false, false);
      check("disabling incoming/outgoing drains stranded queues", inQueue.isEmpty() && outQueue.isEmpty());
      check("auditHoldingMovement false when outgoing disabled", !BlinkPacketManager.auditHoldingMovement());
      check("init_Set and counter clean after drain", inSet.isEmpty() && counter.get() == 0);

      // 6. Mid-blink unchecking hold-movement or hold-actions drains load_Queue
      BlinkPacketManager.setHoldDirections(false, true);
      BlinkPacketManager.setOutgoingHoldTypes(true, true);
      outQueue.add(new ServerboundAttackPacket(10));
      BlinkPacketManager.setOutgoingHoldTypes(false, true);
      check("disabling hold-movement mid-blink drains outgoing queue", outQueue.isEmpty());

      BlinkPacketManager.setOutgoingHoldTypes(true, true);
      outQueue.add(new ServerboundAttackPacket(11));
      BlinkPacketManager.setOutgoingHoldTypes(true, false);
      check("disabling hold-actions mid-blink drains outgoing queue", outQueue.isEmpty());

      // 7. Bare and bundled ClientboundStartConfigurationPacket drop queues without desyncing flags
      BlinkPacketManager.setHoldDirections(true, true);
      inQueue.add(alive);
      outQueue.add(new ServerboundAttackPacket(12));
      boolean heldStartConfig = BlinkPacketManager.interceptIncoming(ClientboundStartConfigurationPacket.INSTANCE);
      check("bare ClientboundStartConfigurationPacket passes through", !heldStartConfig);
      check("bare ClientboundStartConfigurationPacket clears queues", inQueue.isEmpty() && outQueue.isEmpty());

      inQueue.add(alive);
      outQueue.add(new ServerboundAttackPacket(13));
      Packet<?> bundledStartConfig = new ClientboundBundlePacket(List.of(alive, ClientboundStartConfigurationPacket.INSTANCE));
      boolean heldBundledStartConfig = BlinkPacketManager.interceptIncoming(bundledStartConfig);
      check("bundled ClientboundStartConfigurationPacket passes through", !heldBundledStartConfig);
      check("bundled ClientboundStartConfigurationPacket clears queues", inQueue.isEmpty() && outQueue.isEmpty());

      // 8. Bundled keep-alive exempt in flush()
      Packet<?> bundledKeepAlive = new ClientboundBundlePacket(List.of(alive, new ClientboundKeepAlivePacket(123L)));
      check("bundled keepalive exempt in flush()", (Boolean) flushPred.invoke(null, bundledKeepAlive));

      BlinkPacketManager.auditDisconnect();

      if (failures > 0) {
         System.out.println("FAIL BlinkCheck (" + failures + " of " + checks + " failed)");
         System.exit(1);
      }
      System.out.println("PASS BlinkCheck (" + checks + " checks)");
   }
}

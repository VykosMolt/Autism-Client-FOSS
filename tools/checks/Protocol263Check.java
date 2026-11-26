package autismclient.util.macro;

import autismclient.aggressivepvp.ActionWindow;
import autismclient.aggressivepvp.Rotations;
import autismclient.compat263.CompatGLFW;
import autismclient.compat263.CompatSwingPacket;
import autismclient.modules.PoolEntry;
import autismclient.util.bw;
import io.netty.buffer.Unpooled;
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
import net.minecraft.network.protocol.game.ServerboundClientTickEndPacket;
import net.minecraft.network.protocol.game.ServerboundPunchPacket;
import net.minecraft.world.InteractionHand;

/**
 * Verifies MC 26.3 PingSpoof disconnect/bundle/signed-command fixes,
 * ServerboundPunchPacket coverage, and CompatGLFW ISO/KP/Menu key mappings.
 */
public final class Protocol263Check {
   static int checks, failures;

   static void check(String what, boolean ok) {
      checks++;
      if (!ok) {
         failures++;
         System.out.println("FAIL Protocol263Check: " + what);
      }
   }

   @SuppressWarnings("unchecked")
   public static void main(String[] args) throws Exception {
      Method updatePred = PingSpoofScheduler.class.getDeclaredMethod("update", Packet.class);
      updatePred.setAccessible(true);
      Method syncPred = PingSpoofScheduler.class.getDeclaredMethod("sync", Packet.class);
      syncPred.setAccessible(true);
      Method writePred = PingSpoofScheduler.class.getDeclaredMethod("write", Packet.class);
      writePred.setAccessible(true);

      Field inQueueF = PingSpoofScheduler.class.getDeclaredField("init_Queue");
      inQueueF.setAccessible(true);
      Queue<Object> inQueue = (Queue<Object>) inQueueF.get(null);

      Field inCountF = PingSpoofScheduler.class.getDeclaredField("incomingQueued");
      inCountF.setAccessible(true);
      AtomicInteger inCount = (AtomicInteger) inCountF.get(null);

      // 1. PingSpoof signed command exemption
      Packet<?> signedCmd = new ServerboundChatCommandSignedPacket(
         "msg friend hi", Instant.EPOCH, 0L, ArgumentSignatures.EMPTY, new LastSeenMessages.Update(0, new BitSet(), (byte) 0));
      check("PingSpoof sync() exempts ServerboundChatCommandSignedPacket", (Boolean) syncPred.invoke(null, signedCmd));

      // 2. PingSpoof bundle inspection for update()
      Packet<ClientGamePacketListener> lethal = new ClientboundSetHealthPacket(0.0F, 20, 0.0F);
      Packet<ClientGamePacketListener> alive = new ClientboundSetHealthPacket(20.0F, 20, 5.0F);
      check("PingSpoof update() inspects ClientboundBundlePacket",
         (Boolean) updatePred.invoke(null, new ClientboundBundlePacket(List.of(alive, lethal))));
      check("PingSpoof update() ignores non-critical bundle",
         !(Boolean) updatePred.invoke(null, new ClientboundBundlePacket(List.of(alive))));

      // 3. Bare and bundled disconnect drop queues and pass through
      PingSpoofScheduler.load(150, true, true);
      var dataCtor = Class.forName("autismclient.util.macro.PingSpoofScheduler$Inner_DataUtil").getDeclaredConstructor(Packet.class, long.class);
      dataCtor.setAccessible(true);
      inQueue.add(dataCtor.newInstance(alive, System.currentTimeMillis()));
      inCount.incrementAndGet();
      boolean heldBare = PingSpoofScheduler.init_Z(new ClientboundDisconnectPacket(Component.literal("kicked")));
      check("PingSpoof bare disconnect passes through", !heldBare);
      check("PingSpoof bare disconnect clears queue", inQueue.isEmpty() && inCount.get() == 0);

      inQueue.add(dataCtor.newInstance(alive, System.currentTimeMillis()));
      inCount.incrementAndGet();
      boolean heldBundled = PingSpoofScheduler.init_Z(new ClientboundBundlePacket(List.of(alive, new ClientboundDisconnectPacket(Component.literal("kicked")))));
      check("PingSpoof bundled disconnect passes through", !heldBundled);
      check("PingSpoof bundled disconnect clears queue", inQueue.isEmpty() && inCount.get() == 0);

      // 3b. Bare and bundled StartConfiguration drop queues and pass through; bundled keepalive exempt
      inQueue.add(dataCtor.newInstance(alive, System.currentTimeMillis()));
      inCount.incrementAndGet();
      boolean heldStartCfg = PingSpoofScheduler.init_Z(ClientboundStartConfigurationPacket.INSTANCE);
      check("PingSpoof bare StartConfiguration passes through", !heldStartCfg);
      check("PingSpoof bare StartConfiguration clears queue", inQueue.isEmpty() && inCount.get() == 0);

      inQueue.add(dataCtor.newInstance(alive, System.currentTimeMillis()));
      inCount.incrementAndGet();
      boolean heldBundledStartCfg = PingSpoofScheduler.init_Z(new ClientboundBundlePacket(List.of(alive, ClientboundStartConfigurationPacket.INSTANCE)));
      check("PingSpoof bundled StartConfiguration passes through", !heldBundledStartCfg);
      check("PingSpoof bundled StartConfiguration clears queue", inQueue.isEmpty() && inCount.get() == 0);

      check("PingSpoof write() exempts bundled keepalive",
         (Boolean) writePred.invoke(null, new ClientboundBundlePacket(List.of(alive, new ClientboundKeepAlivePacket(42L)))));
      PingSpoofScheduler.read_V();
      PingSpoofScheduler.write();

      // 4. ServerboundPunchPacket in PoolEntry, bw registry aliases, and StreamCodec compatibility
      Field poolSetF = PoolEntry.class.getDeclaredField("init_Set");
      poolSetF.setAccessible(true);
      Set<Class<?>> poolSet = (Set<Class<?>>) poolSetF.get(null);
      check("PoolEntry ignore set contains ServerboundPunchPacket", poolSet.contains(ServerboundPunchPacket.class));
      check("bw resolves CompatSwingPacket -> ServerboundPunchPacket", bw.init_Class_Str("CompatSwingPacket") == ServerboundPunchPacket.class);
      check("bw resolves HandSwingC2SPacket -> ServerboundPunchPacket", bw.init_Class_Str("HandSwingC2SPacket") == ServerboundPunchPacket.class);
      check("bw resolves ServerboundAttackPacket", bw.init_Class_Str("ServerboundAttackPacket") == ServerboundAttackPacket.class);
      var buf = Unpooled.buffer();
      boolean codecOk = true;
      try {
         @SuppressWarnings("rawtypes")
         var rawCodec = (net.minecraft.network.codec.StreamCodec) ServerboundPunchPacket.STREAM_CODEC;
         rawCodec.encode(buf, new CompatSwingPacket(InteractionHand.MAIN_HAND));
      } catch (Throwable t) {
         codecOk = false;
      } finally {
         buf.release();
      }
      check("CompatSwingPacket encodes via ServerboundPunchPacket.STREAM_CODEC", codecOk);

      // 5. CompatGLFW ISO/KP_DECIMAL/MENU mappings
      check("CompatGLFW 161 <-> 100", CompatGLFW.glfwToSdl(161) == 100 && CompatGLFW.sdlToGlfw(100) == 161);
      check("CompatGLFW 330 <-> 99 (KP_PERIOD)", CompatGLFW.glfwToSdl(330) == 99 && CompatGLFW.sdlToGlfw(99) == 330);
      check("CompatGLFW 348 <-> 101 (MENU)", CompatGLFW.glfwToSdl(348) == 101 && CompatGLFW.sdlToGlfw(101) == 348);

      // 6. Rotations.clearPending() and nested/thread-local ActionWindow.own()
      Rotations.rotateOnce(45.0, 10.0, 50);
      Rotations.clearPending();
      Field pendingF = Rotations.class.getDeclaredField("pending");
      pendingF.setAccessible(true);
      check("Rotations.clearPending() clears stale pending flag", !(Boolean) pendingF.get(null));

      ActionWindow.onSend(ServerboundClientTickEndPacket.INSTANCE);
      ActionWindow.own(() -> {
         ActionWindow.own(() -> {});
         ActionWindow.onSend(new ServerboundAttackPacket(99));
      });
      check("nested ActionWindow.own() preserves outer own flag", !ActionWindow.foreign());
      ActionWindow.onSend(ServerboundClientTickEndPacket.INSTANCE);

      if (failures > 0) {
         System.out.println("FAIL Protocol263Check (" + failures + " of " + checks + " failed)");
         System.exit(1);
      }
      System.out.println("PASS Protocol263Check (" + checks + " checks)");
   }
}

package autismclient.util.macro;

import autismclient.compat263.CompatSwingPacket;
import java.lang.reflect.Field;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.InteractionHand;
import sun.misc.Unsafe;

public class PacketGateCheck {
   public static void main(String[] args) throws Exception {
      Field u = Unsafe.class.getDeclaredField("theUnsafe");
      u.setAccessible(true);
      Unsafe unsafe = (Unsafe)u.get(null);
      Minecraft mc = (Minecraft)unsafe.allocateInstance(Minecraft.class);
      Field instance = Minecraft.class.getDeclaredField("instance");
      instance.setAccessible(true);
      instance.set(null, mc);
      // Panic-hide off without reading the real config from disk.
      autismclient.modules.aD.init((autismclient.util.AutismConfig)null);

      PacketGateManager.load_V();
      PacketGateManager.load(gate("g", PacketGateAction.Inner_CachePool.CANCEL), 0L);
      check(PacketGateManager.init_Z(), "owner-0 gate installed");
      PacketGateManager.load(gate("g", PacketGateAction.Inner_CachePool.DISABLE_GATE), 7L);
      check(!PacketGateManager.init_Z(), "UNBLOCK from a run removes the owner-0 gate with the same id");

      PacketGateManager.load(gate("g", PacketGateAction.Inner_CachePool.CANCEL), 0L);
      PacketGateManager.load(gate("run", PacketGateAction.Inner_CachePool.CANCEL), 7L);
      CompatSwingPacket packet = new CompatSwingPacket(InteractionHand.MAIN_HAND);
      check(PacketGateManager.init(packet, "C2S") == PacketGateManager.Inner_StreamUtil.CANCEL, "gates block on the connection they were made on");

      net.minecraft.SharedConstants.tryDetectVersion();
      net.minecraft.server.Bootstrap.bootStrap();
      LocalPlayer player = (LocalPlayer)unsafe.allocateInstance(LocalPlayer.class);
      Field connection = LocalPlayer.class.getField("connection");
      unsafe.putObject(player, unsafe.objectFieldOffset(connection), unsafe.allocateInstance(ClientPacketListener.class));
      Field playerField = Minecraft.class.getField("player");
      unsafe.putObject(mc, unsafe.objectFieldOffset(playerField), player);
      check(mc.getConnection() != null, "fake connection in place");
      PacketGateManager.init(packet, "C2S");
      check(PacketGateManager.init(7L) == 1, "run-owned gate survives a connection change");
      check(PacketGateManager.init(0L) == 0, "owner-0 gate is dropped on a different connection");
      System.out.println("PASS PacketGateCheck");
   }

   private static PacketGateAction gate(String id, PacketGateAction.Inner_CachePool mode) {
      PacketGateAction gate = new PacketGateAction();
      gate.gateId = id;
      gate.mode = mode;
      return gate;
   }

   private static void check(boolean ok, String what) {
      if (!ok) {
         System.out.println("FAIL " + what);
         System.exit(1);
      }
   }
}

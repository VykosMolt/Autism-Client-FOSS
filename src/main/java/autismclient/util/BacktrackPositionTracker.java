package autismclient.util;

import net.minecraft.network.protocol.game.VecDeltaCodec;
import net.minecraft.world.phys.Vec3;

public final class BacktrackPositionTracker {
   private final VecDeltaCodec deltaCodec = new VecDeltaCodec();
   private volatile Vec3 position = Vec3.ZERO;

   public void init(Vec3 protocolBase, Vec3 receivedPose) {
      this.deltaCodec.setBase(protocolBase);
      this.position = receivedPose;
   }

   public Vec3 init(long x, long y, long z) {
      Vec3 next = this.deltaCodec.decode(x, y, z);
      this.init(next);
      return next;
   }

   public void init(Vec3 next) {
      this.deltaCodec.setBase(next);
      this.position = next;
   }

   public Vec3 init() {
      return this.position;
   }
}

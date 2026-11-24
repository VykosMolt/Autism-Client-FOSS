package autismclient.util;

import net.minecraft.network.protocol.game.VecDeltaCodec;
import net.minecraft.world.phys.Vec3;

public final class cO {
   private final VecDeltaCodec init_VecDeltaCodec = new VecDeltaCodec();
   private volatile Vec3 init_Vec3 = Vec3.ZERO;

   public void init(Vec3 protocolBase, Vec3 receivedPose) {
      this.init_VecDeltaCodec.setBase(protocolBase);
      this.init_Vec3 = receivedPose;
   }

   public Vec3 init(long x, long y, long z) {
      Vec3 next = this.init_VecDeltaCodec.decode(x, y, z);
      this.init(next);
      return next;
   }

   public void init(Vec3 next) {
      this.init_VecDeltaCodec.setBase(next);
      this.init_Vec3 = next;
   }

   public Vec3 init() {
      return this.init_Vec3;
   }
}

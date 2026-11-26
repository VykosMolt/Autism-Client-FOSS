package autismclient.ai.b;

import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.BlockHitResult;

public final class PrintControls {
   public boolean init_Z;
   public boolean load_Z;
   public boolean read_Z;
   public boolean write_Z;
   public boolean update;
   public boolean sync;
   public boolean flush;
   public boolean close;
   public float init_F = 0.0F / 0.0F;
   public float load_F = 0.0F / 0.0F;
   public int init_I = -1;
   public int load_I = -1;
   public int read_I = -1;
   public int write_I = -1;
   public BlockHitResult init_BlockHitResult;
   public boolean open;
   public UUID init_UUID;
   public boolean poll;
   public BlockPos init_BlockPos;
   public Direction init_Direction;

   public static PrintControls init_SyncManager() {
      return new PrintControls();
   }

   public boolean init_Z() {
      return this.init_Z || this.load_Z || this.read_Z || this.write_Z || this.update;
   }

   public PrintControls init(float yaw, float pitch) {
      this.init_F = yaw;
      this.load_F = pitch;
      return this;
   }

   public PrintControls init(PrintMover.Inner_StreamUtil keys) {
      if (keys == null) {
         return this;
      }

      this.init_Z = keys.forward();
      this.load_Z = keys.back();
      this.read_Z = keys.left();
      this.write_Z = keys.right();
      this.update = keys.jump();
      this.sync = keys.sneak();
      this.flush = keys.sprint();
      return this;
   }
}

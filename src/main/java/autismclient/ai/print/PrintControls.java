package autismclient.ai.print;

import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.BlockHitResult;

public final class PrintControls {
   public boolean forward;
   public boolean back;
   public boolean left;
   public boolean right;
   public boolean jump;
   public boolean sneak;
   public boolean sprint;
   public boolean fly;
   public float yaw = 0.0F / 0.0F;
   public float pitch = 0.0F / 0.0F;
   public int selectHotbarSlot = -1;
   public int postClickHotbarSlot = -1;
   public int swapSourceSlot = -1;
   public int swapHotbarSlot = -1;
   public BlockHitResult clickHit;
   public boolean trackInteractionReceipt;
   public UUID interactEntityUuid;
   public boolean useHeldItem;
   public BlockPos digPos;
   public Direction digFace;

   public static PrintControls idle() {
      return new PrintControls();
   }

   public boolean hasMovementInput() {
      return this.forward || this.back || this.left || this.right || this.jump;
   }

   public PrintControls withLook(float yaw, float pitch) {
      this.yaw = yaw;
      this.pitch = pitch;
      return this;
   }

   public PrintControls withMoveKeys(PrintMover.Inner_StreamUtil keys) {
      if (keys == null) {
         return this;
      }

      this.forward = keys.forward();
      this.back = keys.back();
      this.left = keys.left();
      this.right = keys.right();
      this.jump = keys.jump();
      this.sneak = keys.sneak();
      this.sprint = keys.sprint();
      return this;
   }
}

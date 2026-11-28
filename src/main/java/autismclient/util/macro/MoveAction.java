package autismclient.util.macro;

import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.CompoundTag;

public class MoveAction implements MacroAction {
   public MoveAction.MoveDirection direction = MoveAction.MoveDirection.FORWARD;
   public int durationTicks = 20;
   public boolean nonBlocking = false;
   private boolean enabled = true;

   public MoveAction() {
   }

   public MoveAction(MoveAction.MoveDirection direction, int durationTicks) {
      this.direction = direction;
      this.durationTicks = durationTicks;
   }

   public KeyMapping keyMapping(Minecraft mc) {
      if (mc.options == null) {
         return null;
      }

      KeyMapping var10000;
      switch (this.direction) {
         case FORWARD:
            var10000 = mc.options.keyUp;
            break;
         case BACKWARD:
            var10000 = mc.options.keyDown;
            break;
         case LEFT:
            var10000 = mc.options.keyLeft;
            break;
         case RIGHT:
            var10000 = mc.options.keyRight;
            break;
         default:
            throw new MatchException(null, null);
      }

      return var10000;
   }

   @Override
   public void execute(Minecraft mc) {
      KeyMapping key = this.keyMapping(mc);
      if (key != null) {
         key.setDown(true);
      }
   }

   public void releaseKey(Minecraft mc) {
      KeyMapping key = this.keyMapping(mc);
      if (key != null) {
         key.setDown(false);
      }
   }

   @Override
   public CompoundTag toTag() {
      CompoundTag tag = new CompoundTag();
      tag.putString("type", this.getType().name());
      tag.putString("direction", this.direction.name());
      tag.putInt("durationTicks", this.durationTicks);
      tag.putBoolean("nonBlocking", this.nonBlocking);
      tag.putBoolean("enabled", this.enabled);
      return tag;
   }

   @Override
   public void fromTag(CompoundTag tag) {
      if (tag.contains("direction")) {
         try {
            this.direction = MoveAction.MoveDirection.valueOf(tag.getStringOr("direction", "FORWARD"));
         } catch (Exception e) {
            this.direction = MoveAction.MoveDirection.FORWARD;
         }
      }

      if (tag.contains("durationTicks")) {
         this.durationTicks = tag.getIntOr("durationTicks", 20);
      }

      if (tag.contains("nonBlocking")) {
         this.nonBlocking = tag.getBooleanOr("nonBlocking", false);
      }

      if (tag.contains("enabled")) {
         this.enabled = tag.getBooleanOr("enabled", true);
      }
   }

   @Override
   public MacroActionType getType() {
      return MacroActionType.MOVE;
   }

   @Override
   public String getDisplayName() {
      return "Move " + this.direction.displayName() + " (" + this.durationTicks + "t)" + (this.nonBlocking ? " [BG]" : "");
   }

   @Override
   public String getIcon() {
      return "MOV";
   }

   @Override
   public boolean isEnabled() {
      return this.enabled;
   }

   @Override
   public void setEnabled(boolean e) {
      this.enabled = e;
   }

   public enum MoveDirection {
      FORWARD,
      BACKWARD,
      LEFT,
      RIGHT;

      public String displayName() {
         return this.name().charAt(0) + this.name().substring(1).toLowerCase();
      }

      public MoveAction.MoveDirection init_s_DataUtil() {
         MoveAction.MoveDirection[] vals = values();
         return vals[(this.ordinal() + 1) % vals.length];
      }
   }
}

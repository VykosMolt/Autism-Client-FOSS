package autismclient.util.macro;

import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.CompoundTag;

public class MoveAction implements MacroAction {
   public MoveAction.Inner_DataUtil direction = MoveAction.Inner_DataUtil.FORWARD;
   public int durationTicks = 20;
   public boolean nonBlocking = false;
   private boolean init = true;

   public MoveAction() {
   }

   public MoveAction(MoveAction.Inner_DataUtil direction, int durationTicks) {
      this.direction = direction;
      this.durationTicks = durationTicks;
   }

   public KeyMapping init_KeyMapping(Minecraft mc) {
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
      KeyMapping key = this.init_KeyMapping(mc);
      if (key != null) {
         key.setDown(true);
      }
   }

   public void init_V_3(Minecraft mc) {
      KeyMapping key = this.init_KeyMapping(mc);
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
      tag.putBoolean("enabled", this.init);
      return tag;
   }

   @Override
   public void fromTag(CompoundTag tag) {
      if (tag.contains("direction")) {
         try {
            this.direction = MoveAction.Inner_DataUtil.valueOf(tag.getStringOr("direction", "FORWARD"));
         } catch (Exception e) {
            this.direction = MoveAction.Inner_DataUtil.FORWARD;
         }
      }

      if (tag.contains("durationTicks")) {
         this.durationTicks = tag.getIntOr("durationTicks", 20);
      }

      if (tag.contains("nonBlocking")) {
         this.nonBlocking = tag.getBooleanOr("nonBlocking", false);
      }

      if (tag.contains("enabled")) {
         this.init = tag.getBooleanOr("enabled", true);
      }
   }

   @Override
   public StreamGate getType() {
      return StreamGate.MOVE;
   }

   @Override
   public String getDisplayName() {
      return "Move " + this.direction.init_Str() + " (" + this.durationTicks + "t)" + (this.nonBlocking ? " [BG]" : "");
   }

   @Override
   public String getIcon() {
      return "MOV";
   }

   @Override
   public boolean isEnabled() {
      return this.init;
   }

   @Override
   public void setEnabled(boolean e) {
      this.init = e;
   }

   public enum Inner_DataUtil {
      FORWARD,
      BACKWARD,
      LEFT,
      RIGHT;

      public String init_Str() {
         return this.name().charAt(0) + this.name().substring(1).toLowerCase();
      }

      public MoveAction.Inner_DataUtil init_s_DataUtil() {
         MoveAction.Inner_DataUtil[] vals = values();
         return vals[(this.ordinal() + 1) % vals.length];
      }
   }
}

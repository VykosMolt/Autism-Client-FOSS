package autismclient.util.macro;

import autismclient.util.RegistryDisplayNameCache;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;

public class WaitForBlockAction implements MacroAction {
   public WaitForBlockAction.CheckMode checkMode = WaitForBlockAction.CheckMode.AT_POSITION;
   public WaitForBlockAction.WaitBehavior waitBehavior = WaitForBlockAction.WaitBehavior.PLACED;
   public boolean anyBlock = true;
   public List<String> blockIds = new ArrayList<>();
   public BlockPos blockPos = BlockPos.ZERO;
   public boolean mustBeInReach = false;
   public double searchRadius = 8.0;
   public boolean listenDuringPreviousAction = false;
   private boolean enabled = true;

   @Override
   public MacroActionType getType() {
      return MacroActionType.WAIT_BLOCK;
   }

   @Override
   public void execute(Minecraft mc) {
   }

   @Override
   public String getDisplayName() {
      String blockDesc;
      if (this.anyBlock) {
         blockDesc = this.waitBehavior == WaitForBlockAction.WaitBehavior.DESTROYED ? "any block gone" : "any block";
      } else if (this.blockIds.isEmpty()) {
         blockDesc = "no blocks set";
      } else {
         blockDesc = RegistryDisplayNameCache.blockName(this.blockIds.get(0));
         if (this.blockIds.size() > 1) {
            blockDesc = blockDesc + " (+" + (this.blockIds.size() - 1) + ")";
         }
      }

      String verb = this.waitBehavior == WaitForBlockAction.WaitBehavior.DESTROYED ? "Destroyed" : "Placed";
      String var10000;
      switch (this.checkMode) {
         case AT_POSITION:
            var10000 = "Wait " + verb + " @ " + this.blockPos.getX() + "," + this.blockPos.getY() + "," + this.blockPos.getZ();
            break;
         case IN_REACH:
            var10000 = "Wait " + verb + ": " + blockDesc + " nearby";
            break;
         case LOOKING_AT:
            var10000 = "Wait " + verb + ": look at " + blockDesc;
            break;
         default:
            throw new MatchException(null, null);
      }

      return var10000;
   }

   @Override
   public String getIcon() {
      return "Blk";
   }

   @Override
   public boolean isEnabled() {
      return this.enabled;
   }

   @Override
   public void setEnabled(boolean e) {
      this.enabled = e;
   }

   @Override
   public CompoundTag toTag() {
      CompoundTag tag = new CompoundTag();
      tag.putString("type", this.getType().name());
      tag.putString("checkMode", this.checkMode.name());
      tag.putString("waitBehavior", this.waitBehavior.name());
      tag.putBoolean("anyBlock", this.anyBlock);
      tag.putInt("x", this.blockPos.getX());
      tag.putInt("y", this.blockPos.getY());
      tag.putInt("z", this.blockPos.getZ());
      tag.putBoolean("mustBeInReach", this.mustBeInReach);
      tag.putDouble("searchRadius", this.searchRadius);
      ListTag list = new ListTag();

      for (String id : this.blockIds) {
         list.add(StringTag.valueOf(id));
      }

      tag.put("blockIds", list);
      tag.putBoolean("enabled", this.enabled);
      ListenDuringPreviousSerializer.writeToTag(tag, this);
      return tag;
   }

   @Override
   public void fromTag(CompoundTag tag) {
      if (tag.contains("checkMode")) {
         try {
            this.checkMode = WaitForBlockAction.CheckMode.valueOf(tag.getStringOr("checkMode", "AT_POSITION"));
         } catch (IllegalArgumentException ignored) {
            this.checkMode = WaitForBlockAction.CheckMode.AT_POSITION;
         }
      }

      if (tag.contains("waitBehavior")) {
         try {
            this.waitBehavior = WaitForBlockAction.WaitBehavior.valueOf(tag.getStringOr("waitBehavior", "PLACED"));
         } catch (IllegalArgumentException ignored) {
            this.waitBehavior = WaitForBlockAction.WaitBehavior.PLACED;
         }
      }

      if (tag.contains("anyBlock")) {
         this.anyBlock = tag.getBooleanOr("anyBlock", true);
      }

      if (tag.contains("x") && tag.contains("y") && tag.contains("z")) {
         this.blockPos = new BlockPos(tag.getIntOr("x", 0), tag.getIntOr("y", 0), tag.getIntOr("z", 0));
      }

      this.mustBeInReach = tag.getBooleanOr("mustBeInReach", false);
      this.searchRadius = tag.getDoubleOr("searchRadius", 8.0);
      this.blockIds.clear();
      if (tag.contains("blockIds")) {
         for (Tag el : tag.getList("blockIds").orElse(new ListTag())) {
            String s = el.asString().orElse("");
            if (!s.isEmpty()) {
               this.blockIds.add(s);
            }
         }
      } else if (tag.contains("blocks")) {
         for (Tag el : tag.getList("blocks").orElse(new ListTag())) {
            String s = el.asString().orElse("");
            if (!s.isEmpty()) {
               this.blockIds.add(s);
            }
         }
      }

      if (tag.contains("enabled")) {
         this.enabled = tag.getBooleanOr("enabled", true);
      }

      ListenDuringPreviousSerializer.readFromTag(tag, this);
   }

   public enum CheckMode {
      AT_POSITION,
      IN_REACH,
      LOOKING_AT;
   }

   public enum WaitBehavior {
      PLACED,
      DESTROYED;
   }
}

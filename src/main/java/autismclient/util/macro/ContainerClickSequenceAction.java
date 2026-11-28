package autismclient.util.macro;

import autismclient.util.AutismSessionState;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerInput;

public class ContainerClickSequenceAction implements MacroAction {
   public ContainerClickSequenceAction.SlotSource slotSource = ContainerClickSequenceAction.SlotSource.SINGLE;
   public ContainerClickSequenceAction.ContainerSource containerSource = ContainerClickSequenceAction.ContainerSource.CURRENT;
   public int slot = 0;
   public int startSlot = 0;
   public int endSlot = 0;
   public ArrayList<String> slots = new ArrayList<>();
   public int manualContainerId = 0;
   public int savedContainerId = -1;
   public String button = "0 (Left/Primary)";
   public String containerInput = "PICKUP";
   public int repeatCount = 1;
   public int delayTicks = 0;

   @Override
   public void execute(Minecraft mc) {
      if (!autismclient.modules.aD.read_Z() && mc != null && mc.player != null && mc.gameMode != null && mc.player.containerMenu != null) {
         List<Integer> clickSlots = this.slots();

         for (int r = 0; r < Math.max(1, this.repeatCount); r++) {
            for (int visibleSlot : clickSlots) {
               this.clickSlot(mc, visibleSlot);
            }
         }
      }
   }

   boolean clickSlot(Minecraft mc, int visibleSlot) {
      if (!autismclient.modules.aD.read_Z() && mc != null && mc.player != null && mc.gameMode != null && mc.player.containerMenu != null) {
         int handlerSlot = this.containerSource != ContainerClickSequenceAction.ContainerSource.CURRENT && this.containerSource != ContainerClickSequenceAction.ContainerSource.PLAYER_INVENTORY
            ? visibleSlot
            : autismclient.util.InventorySlotHelper.toScreenSlot(mc, visibleSlot);
         if (handlerSlot < 0) {
            return false;
         }

         int buttonNum = 0;

         try {
            buttonNum = Integer.parseInt(this.button.split(" ")[0]);
         } catch (Exception var6) {
         }

         ContainerInput input = MacroNbtCodecHelper.parseEnum(ContainerInput.class, this.containerInput, ContainerInput.PICKUP);
         mc.gameMode.handleContainerInput(this.containerIdFor(mc), handlerSlot, buttonNum, input, mc.player);
         return true;
      } else {
         return false;
      }
   }

   private int containerIdFor(Minecraft mc) {
      int var10000;
      switch (this.containerSource) {
         case CURRENT:
            var10000 = mc.player.containerMenu.containerId;
            break;
         case SAVED_GUI:
            AbstractContainerMenu saved = AutismSessionState.getInstance().getStoredContainerMenu();
            var10000 = saved == null ? this.savedContainerId : saved.containerId;
            break;
         case PLAYER_INVENTORY:
            var10000 = 0;
            break;
         case MANUAL:
            var10000 = this.manualContainerId;
            break;
         default:
            throw new MatchException(null, null);
      }

      return var10000;
   }

   public List<Integer> slots() {
      ArrayList<Integer> out = new ArrayList<>();
      switch (this.slotSource) {
         case SINGLE:
            out.add(this.slot);
            break;
         case RANGE:
            int a = Math.min(this.startSlot, this.endSlot);
            int b = Math.max(this.startSlot, this.endSlot);

            for (int i = a; i <= b; i++) {
               out.add(i);
            }
            break;
         case LIST:
            for (String raw : this.slots) {
               try {
                  out.add(Integer.parseInt(raw.trim()));
               } catch (Exception var8) {
               }
            }
            break;
         case CAPTURED_SEQUENCE:
            for (String raw : this.slots) {
               try {
                  out.add(Integer.parseInt(raw.trim()));
               } catch (Exception var7) {
               }
            }
      }

      return out;
   }

   @Override
   public MacroActionType getType() {
      return MacroActionType.CONTAINER_CLICK_SEQUENCE;
   }

   @Override
   public CompoundTag toTag() {
      CompoundTag tag = new CompoundTag();
      tag.putString("type", "CONTAINER_CLICK_SEQUENCE");
      tag.putString("slotSource", this.slotSource.name());
      tag.putString("containerSource", this.containerSource.name());
      tag.putInt("slot", this.slot);
      tag.putInt("startSlot", this.startSlot);
      tag.putInt("endSlot", this.endSlot);
      tag.put("slots", MacroNbtCodecHelper.toStringListTag(this.slots));
      tag.putInt("manualContainerId", this.manualContainerId);
      tag.putInt("savedContainerId", this.savedContainerId);
      tag.putString("button", this.button);
      tag.putString("containerInput", this.containerInput);
      tag.putInt("repeatCount", this.repeatCount);
      tag.putInt("delayTicks", this.delayTicks);
      return tag;
   }

   @Override
   public void fromTag(CompoundTag tag) {
      this.slotSource = MacroNbtCodecHelper.parseEnum(ContainerClickSequenceAction.SlotSource.class, tag.getStringOr("slotSource", "SINGLE"), ContainerClickSequenceAction.SlotSource.SINGLE);
      this.containerSource = MacroNbtCodecHelper.parseEnum(ContainerClickSequenceAction.ContainerSource.class, tag.getStringOr("containerSource", "CURRENT"), ContainerClickSequenceAction.ContainerSource.CURRENT);
      this.slot = tag.getIntOr("slot", 0);
      this.startSlot = tag.getIntOr("startSlot", 0);
      this.endSlot = tag.getIntOr("endSlot", 0);
      this.slots = MacroNbtCodecHelper.fromStringListTag(tag.getList("slots").orElse(new ListTag()));
      this.manualContainerId = tag.getIntOr("manualContainerId", 0);
      this.savedContainerId = tag.getIntOr("savedContainerId", -1);
      String btnStr = tag.getStringOr("button", "");
      if (!btnStr.isEmpty() && !btnStr.matches("\\d+")) {
         this.button = btnStr;
      } else {
         int b = tag.getIntOr("button", btnStr.isEmpty() ? 0 : Integer.parseInt(btnStr));
         if (b == 0) {
            this.button = "0 (Left/Primary)";
         } else if (b == 1) {
            this.button = "1 (Right/Secondary)";
         } else if (b == 2) {
            this.button = "2 (Middle)";
         } else {
            this.button = String.valueOf(b);
         }
      }

      this.containerInput = tag.getStringOr("containerInput", "PICKUP");
      this.repeatCount = tag.getIntOr("repeatCount", 1);
      this.delayTicks = tag.getIntOr("delayTicks", 0);
   }

   @Override
   public String getDisplayName() {
      return "Click slots " + this.slotSource + " " + this.containerInput;
   }

   @Override
   public String getIcon() {
      return "C";
   }

   public enum ContainerSource {
      CURRENT,
      SAVED_GUI,
      PLAYER_INVENTORY,
      MANUAL;
   }

   public enum SlotSource {
      SINGLE,
      RANGE,
      LIST,
      CAPTURED_SEQUENCE;
   }
}

package autismclient.util;

import it.unimi.dsi.fastutil.ints.Int2ObjectArrayMap;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.HashedStack;
import net.minecraft.network.protocol.game.ServerboundContainerClickPacket;
import net.minecraft.world.inventory.ContainerInput;

public final class ContainerClickSnapshot {
   private ContainerClickSnapshot() {
   }

   public enum ClickMode {
      LEFT_CLICK("Left Click", "Left", 0, ContainerInput.PICKUP),
      RIGHT_CLICK("Right Click", "Right", 1, ContainerInput.PICKUP),
      QUICK_MOVE("Quick Move", "QMove", 0, ContainerInput.QUICK_MOVE);

      public final String init_Str;
      public final String load_Str;
      public final int init_I;
      public final ContainerInput init_ContainerInput;

      ClickMode(String displayName, String shortName, int button, ContainerInput input) {
         this.init_Str = displayName;
         this.load_Str = shortName;
         this.init_I = button;
         this.init_ContainerInput = input;
      }

      public static ContainerClickSnapshot.ClickMode byIndex(int index) {
         ContainerClickSnapshot.ClickMode[] values = values();
         return index >= 0 && index < values.length ? values[index] : LEFT_CLICK;
      }

      public static ContainerClickSnapshot.ClickMode byName(String name) {
         if (name != null) {
            for (ContainerClickSnapshot.ClickMode mode : values()) {
               if (mode.name().equalsIgnoreCase(name) || mode.init_Str.equalsIgnoreCase(name) || mode.load_Str.equalsIgnoreCase(name)) {
                  return mode;
               }
            }
         }

         return LEFT_CLICK;
      }
   }

   public record Snapshot(
      int containerId, int stateId, int handlerSlot, int write, String init_Str, String load_Str, String read_Str, ContainerClickSnapshot.ClickMode mode, long capturedAtMs
   ) {
      public ServerboundContainerClickPacket toPacket() {
         ContainerClickSnapshot.ClickMode effectiveMode = this.mode == null ? ContainerClickSnapshot.ClickMode.LEFT_CLICK : this.mode;
         return new ServerboundContainerClickPacket(
            this.containerId,
            this.stateId,
            (short)this.handlerSlot,
            (byte)effectiveMode.init_I,
            effectiveMode.init_ContainerInput,
            new Int2ObjectArrayMap<>(),
            HashedStack.EMPTY
         );
      }

      public ContainerClickSnapshot.Snapshot withMode(ContainerClickSnapshot.ClickMode newMode) {
         return new ContainerClickSnapshot.Snapshot(
            this.containerId,
            this.stateId,
            this.handlerSlot,
            this.write,
            this.init_Str,
            this.load_Str,
            this.read_Str,
            newMode == null ? ContainerClickSnapshot.ClickMode.LEFT_CLICK : newMode,
            this.capturedAtMs
         );
      }

      public String init_Str() {
         String modeName = (this.mode == null ? ContainerClickSnapshot.ClickMode.LEFT_CLICK : this.mode).init_Str;
         String item = this.read_Str != null && !this.read_Str.isBlank() ? this.read_Str : "empty";
         return modeName + " slot " + this.write + " [" + item + "] menu " + this.containerId + ":" + this.stateId;
      }

      public CompoundTag toTag() {
         CompoundTag tag = new CompoundTag();
         tag.putInt("containerId", this.containerId);
         tag.putInt("stateId", this.stateId);
         tag.putInt("handlerSlot", this.handlerSlot);
         tag.putInt("visibleSlot", this.write);
         tag.putString("screenTitle", this.init_Str == null ? "" : this.init_Str);
         tag.putString("menuClass", this.load_Str == null ? "" : this.load_Str);
         tag.putString("itemSummary", this.read_Str == null ? "" : this.read_Str);
         tag.putString("mode", (this.mode == null ? ContainerClickSnapshot.ClickMode.LEFT_CLICK : this.mode).name());
         tag.putLong("capturedAtMs", this.capturedAtMs);
         return tag;
      }

      public static ContainerClickSnapshot.Snapshot fromTag(CompoundTag tag) {
         if (tag == null) {
            return null;
         }

         int containerId = tag.getIntOr("containerId", -1);
         int stateId = tag.getIntOr("stateId", 0);
         int handlerSlot = tag.getIntOr("handlerSlot", -1);
         int visibleSlot = tag.getIntOr("visibleSlot", handlerSlot);
         return containerId >= 0 && handlerSlot >= 0
            ? new ContainerClickSnapshot.Snapshot(
               containerId,
               stateId,
               handlerSlot,
               visibleSlot,
               tag.getStringOr("screenTitle", ""),
               tag.getStringOr("menuClass", ""),
               tag.getStringOr("itemSummary", ""),
               ContainerClickSnapshot.ClickMode.byName(tag.getStringOr("mode", ContainerClickSnapshot.ClickMode.LEFT_CLICK.name())),
               tag.getLongOr("capturedAtMs", 0L)
            )
            : null;
      }

      public int visibleSlot() {
         return this.write;
      }

      public String load_Str() {
         return this.init_Str;
      }

      public String read_Str() {
         return this.load_Str;
      }

      public String itemLabel() {
         return this.read_Str;
      }
   }
}

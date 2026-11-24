package autismclient.util;

import it.unimi.dsi.fastutil.ints.Int2ObjectArrayMap;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.HashedStack;
import net.minecraft.network.protocol.game.ServerboundContainerClickPacket;
import net.minecraft.world.inventory.ContainerInput;

public final class bo {
   private bo() {
   }

   public enum Inner_DataUtil {
      init_bo_DataUtil("Left Click", "Left", 0, ContainerInput.PICKUP),
      load_bo_DataUtil("Right Click", "Right", 1, ContainerInput.PICKUP),
      read("Quick Move", "QMove", 0, ContainerInput.QUICK_MOVE);

      public final String init_Str;
      public final String load_Str;
      public final int init_I;
      public final ContainerInput init_ContainerInput;

      Inner_DataUtil(String displayName, String shortName, int button, ContainerInput input) {
         this.init_Str = displayName;
         this.load_Str = shortName;
         this.init_I = button;
         this.init_ContainerInput = input;
      }

      public static bo.Inner_DataUtil init(int index) {
         bo.Inner_DataUtil[] values = values();
         return index >= 0 && index < values.length ? values[index] : init_bo_DataUtil;
      }

      public static bo.Inner_DataUtil init(String name) {
         if (name != null) {
            for (bo.Inner_DataUtil mode : values()) {
               if (mode.name().equalsIgnoreCase(name) || mode.init_Str.equalsIgnoreCase(name) || mode.load_Str.equalsIgnoreCase(name)) {
                  return mode;
               }
            }
         }

         return init_bo_DataUtil;
      }
   }

   public record Inner_StreamUtil(
      int init_I, int load_I, int read_I, int write, String init_Str, String load_Str, String read_Str, bo.Inner_DataUtil init_bo_DataUtil, long init_J
   ) {
      public ServerboundContainerClickPacket init_ServerboundContain() {
         bo.Inner_DataUtil effectiveMode = this.init_bo_DataUtil == null ? bo.Inner_DataUtil.init_bo_DataUtil : this.init_bo_DataUtil;
         return new ServerboundContainerClickPacket(
            this.init_I,
            this.load_I,
            (short)this.read_I,
            (byte)effectiveMode.init_I,
            effectiveMode.init_ContainerInput,
            new Int2ObjectArrayMap<>(),
            HashedStack.EMPTY
         );
      }

      public bo.Inner_StreamUtil init_bo_StreamUtil_bo_DataUtil(bo.Inner_DataUtil newMode) {
         return new bo.Inner_StreamUtil(
            this.init_I,
            this.load_I,
            this.read_I,
            this.write,
            this.init_Str,
            this.load_Str,
            this.read_Str,
            newMode == null ? bo.Inner_DataUtil.init_bo_DataUtil : newMode,
            this.init_J
         );
      }

      public String init_Str() {
         String modeName = (this.init_bo_DataUtil == null ? bo.Inner_DataUtil.init_bo_DataUtil : this.init_bo_DataUtil).init_Str;
         String item = this.read_Str != null && !this.read_Str.isBlank() ? this.read_Str : "empty";
         return modeName + " slot " + this.write + " [" + item + "] menu " + this.init_I + ":" + this.load_I;
      }

      public CompoundTag init_CompoundTag() {
         CompoundTag tag = new CompoundTag();
         tag.putInt("containerId", this.init_I);
         tag.putInt("stateId", this.load_I);
         tag.putInt("handlerSlot", this.read_I);
         tag.putInt("visibleSlot", this.write);
         tag.putString("screenTitle", this.init_Str == null ? "" : this.init_Str);
         tag.putString("menuClass", this.load_Str == null ? "" : this.load_Str);
         tag.putString("itemSummary", this.read_Str == null ? "" : this.read_Str);
         tag.putString("mode", (this.init_bo_DataUtil == null ? bo.Inner_DataUtil.init_bo_DataUtil : this.init_bo_DataUtil).name());
         tag.putLong("capturedAtMs", this.init_J);
         return tag;
      }

      public static bo.Inner_StreamUtil init_bo_StreamUtil_CompoundTag(CompoundTag tag) {
         if (tag == null) {
            return null;
         }

         int containerId = tag.getIntOr("containerId", -1);
         int stateId = tag.getIntOr("stateId", 0);
         int handlerSlot = tag.getIntOr("handlerSlot", -1);
         int visibleSlot = tag.getIntOr("visibleSlot", handlerSlot);
         return containerId >= 0 && handlerSlot >= 0
            ? new bo.Inner_StreamUtil(
               containerId,
               stateId,
               handlerSlot,
               visibleSlot,
               tag.getStringOr("screenTitle", ""),
               tag.getStringOr("menuClass", ""),
               tag.getStringOr("itemSummary", ""),
               bo.Inner_DataUtil.init(tag.getStringOr("mode", bo.Inner_DataUtil.init_bo_DataUtil.name())),
               tag.getLongOr("capturedAtMs", 0L)
            )
            : null;
      }

      public int write_I() {
         return this.write;
      }

      public String load_Str() {
         return this.init_Str;
      }

      public String read_Str() {
         return this.load_Str;
      }

      public String write_Str() {
         return this.read_Str;
      }
   }
}

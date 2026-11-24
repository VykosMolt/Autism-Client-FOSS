package autismclient.util.g;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;

public final class ListPool {
   public static final int init_I = 6;
   public String init_Str = "";
   public final List<ListPool.Inner_DataUtil> init_List = new ArrayList<>();

   public ListPool() {
   }

   public ListPool(ListPool source) {
      if (source != null) {
         this.init_Str = source.init_Str;
         this.init_List.addAll(source.init_List);
         this.init_V();
      }
   }

   public ListPool(String name, String packetClass, String arguments) {
      this.init_Str = name;
      if (packetClass != null && !packetClass.isBlank()) {
         this.init_List.add(new ListPool.Inner_DataUtil(packetClass, arguments));
      }

      this.init_V();
   }

   public boolean init_Z() {
      for (ListPool.Inner_DataUtil step : this.init_List) {
         if (!step.init_Z()) {
            return false;
         }
      }

      return true;
   }

   public String init_Str() {
      for (ListPool.Inner_DataUtil step : this.init_List) {
         if (!step.init_Z()) {
            return step.init_Str();
         }
      }

      return "";
   }

   public int init_I() {
      int count = 0;

      for (ListPool.Inner_DataUtil step : this.init_List) {
         if (!step.init_Z()) {
            count++;
         }
      }

      return count;
   }

   public String init_Str_I(int index) {
      this.init_V();
      if (this.init_Z()) {
         return "Empty";
      }

      if (this.init_Str != null && !this.init_Str.isBlank()) {
         return this.init_Str;
      }

      String simple = init_Str_Str(this.init_Str());
      if (simple.isBlank()) {
         return "Slot " + (index + 1);
      }

      int extra = this.init_I() - 1;
      return extra > 0 ? simple + " +" + extra : simple;
   }

   public void init_V() {
      this.init_Str = this.init_Str == null ? "" : PacketUtil.update(this.init_Str, 32);
      this.init_List.removeIf(step -> step == null || step.init_Z());

      while (this.init_List.size() > 6) {
         this.init_List.remove(this.init_List.size() - 1);
      }
   }

   public static String init_Str_Str(String packetClass) {
      if (packetClass != null && !packetClass.isBlank()) {
         int cut = Math.max(packetClass.lastIndexOf(46), packetClass.lastIndexOf(36));
         String simple = cut < 0 ? packetClass : packetClass.substring(cut + 1);
         simple = init(init(simple, "Serverbound"), "Clientbound");
         if (simple.endsWith("Packet")) {
            simple = simple.substring(0, simple.length() - "Packet".length());
         }

         return simple.isBlank() ? packetClass : simple;
      } else {
         return "";
      }
   }

   private static String init(String value, String prefix) {
      return value.startsWith(prefix) && value.length() > prefix.length() ? value.substring(prefix.length()) : value;
   }

   CompoundTag init_CompoundTag() {
      this.init_V();
      CompoundTag tag = new CompoundTag();
      tag.putString("name", this.init_Str);
      ListTag list = new ListTag();

      for (ListPool.Inner_DataUtil step : this.init_List) {
         list.add(step.init_CompoundTag());
      }

      tag.put("steps", list);
      return tag;
   }

   static ListPool init_ListPool_CompoundTag(CompoundTag tag) {
      ListPool action = new ListPool();
      if (tag != null) {
         action.init_Str = tag.getStringOr("name", "");
         ListTag list = tag.getListOrEmpty("steps");
         if (list.isEmpty()) {
            String packet = tag.getStringOr("packet", "");
            if (!packet.isBlank()) {
               action.init_List.add(new ListPool.Inner_DataUtil(packet, tag.getStringOr("args", "")));
            }
         } else {
            for (Tag value : list) {
               if (value instanceof CompoundTag compound) {
                  action.init_List.add(ListPool.Inner_DataUtil.init(compound));
               }
            }
         }
      }

      action.init_V();
      return action;
   }

   public record Inner_DataUtil(String init, String load) {
      public Inner_DataUtil {
         init = init == null ? "" : init.trim();
         load = load == null ? "" : PacketUtil.update(load, 2048);
      }

      public boolean init_Z() {
         return this.init.isBlank();
      }

      CompoundTag init_CompoundTag() {
         CompoundTag tag = new CompoundTag();
         tag.putString("packet", this.init);
         tag.putString("args", this.load);
         return tag;
      }

      static ListPool.Inner_DataUtil init(CompoundTag tag) {
         return new ListPool.Inner_DataUtil(tag.getStringOr("packet", ""), tag.getStringOr("args", ""));
      }

      public String init_Str() {
         return this.init;
      }
   }
}

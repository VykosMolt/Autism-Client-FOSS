package autismclient.util;

import java.util.List;

public record PacketFieldSnapshot(PacketSchemaRegistry.Inner_DataUtil init_bx_DataUtil, Object init_Obj, List<String> init_List, boolean readable, boolean load) {
   public PacketFieldSnapshot {
      init_List = init_List == null ? List.of() : List.copyOf(init_List);
   }

   public String name() {
      return this.init_bx_DataUtil == null ? "field" : this.init_bx_DataUtil.name();
   }

   public String typeName() {
      return this.init_bx_DataUtil == null ? "Object" : this.init_bx_DataUtil.load();
   }

   public String typeLabel() {
      return this.init_bx_DataUtil == null ? "object" : this.init_bx_DataUtil.read();
   }

   public String write() {
      return this.init_List.isEmpty() ? "unavailable" : this.init_List.getFirst();
   }

   public List<String> init_List() {
      return this.init_List.size() <= 1 ? List.of() : this.init_List.subList(1, this.init_List.size());
   }

   public List<String> load_List() {
      return this.init_List;
   }

   public boolean editableCandidate() {
      return this.load;
   }
}

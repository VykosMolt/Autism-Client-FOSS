package autismclient.util.macro;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;

public class MacroVariablesAction implements MacroAction {
   public ArrayList<String> names = new ArrayList<>();
   public ArrayList<String> values = new ArrayList<>();

   @Override
   public void execute(Minecraft mc) {
      int count = Math.min(this.names.size(), this.values.size());
      LinkedHashMap<String, l> resolvedValues = new LinkedHashMap<>();

      for (int i = 0; i < count; i++) {
         j.Inner_CachePool resolved = n.init_j_CachePool_Str_Minecraft(this.values.get(i), mc);
         if (!resolved.init_Z()) {
            return;
         }

         String name = this.names.get(i);
         if (!m.load(name).isEmpty()) {
            resolvedValues.put(name, l.init_l_Obj(resolved.init_Str()));
         }
      }

      n.init_V_Map(resolvedValues);
   }

   @Override
   public StreamGate getType() {
      return StreamGate.MACRO_VARIABLES;
   }

   @Override
   public CompoundTag toTag() {
      CompoundTag tag = new CompoundTag();
      tag.putString("type", "MACRO_VARIABLES");
      tag.put("names", i.init(this.names));
      tag.put("values", i.init(this.values));
      return tag;
   }

   @Override
   public void fromTag(CompoundTag tag) {
      this.names = i.init(tag.getList("names").orElse(new ListTag()));
      this.values = i.init(tag.getList("values").orElse(new ListTag()));
   }

   @Override
   public String getDisplayName() {
      return "Set variables x" + Math.max(this.names.size(), this.values.size());
   }

   @Override
   public String getIcon() {
      return "V";
   }
}

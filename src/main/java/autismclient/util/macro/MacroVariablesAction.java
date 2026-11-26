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
      LinkedHashMap<String, MacroVarValue> resolvedValues = new LinkedHashMap<>();

      for (int i = 0; i < count; i++) {
         MacroTemplateResolver.ResolveResult resolved = MacroVariableContext.resolveTemplate(this.values.get(i), mc);
         if (!resolved.success()) {
            return;
         }

         String name = this.names.get(i);
         if (!MacroVariableStore.load(name).isEmpty()) {
            resolvedValues.put(name, MacroVarValue.ofText(resolved.value()));
         }
      }

      MacroVariableContext.setAll(resolvedValues);
   }

   @Override
   public MacroActionType getType() {
      return MacroActionType.MACRO_VARIABLES;
   }

   @Override
   public CompoundTag toTag() {
      CompoundTag tag = new CompoundTag();
      tag.putString("type", "MACRO_VARIABLES");
      tag.put("names", MacroNbtCodecHelper.toStringListTag(this.names));
      tag.put("values", MacroNbtCodecHelper.toStringListTag(this.values));
      return tag;
   }

   @Override
   public void fromTag(CompoundTag tag) {
      this.names = MacroNbtCodecHelper.fromStringListTag(tag.getList("names").orElse(new ListTag()));
      this.values = MacroNbtCodecHelper.fromStringListTag(tag.getList("values").orElse(new ListTag()));
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

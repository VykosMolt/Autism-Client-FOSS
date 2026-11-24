package autismclient.util.macro;

import net.minecraft.client.Minecraft;
import net.minecraft.nbt.CompoundTag;

public final class r implements MacroAction {
   private String init_Str;
   private CompoundTag init_CompoundTag;

   public r() {
      this.init_Str = "";
      this.init_CompoundTag = new CompoundTag();
   }

   public r(String typeId, CompoundTag raw) {
      this.init_Str = typeId == null ? "" : typeId;
      this.init_CompoundTag = raw == null ? new CompoundTag() : raw;
   }

   public String init() {
      return this.init_Str;
   }

   @Override
   public void execute(Minecraft mc) {
   }

   @Override
   public CompoundTag toTag() {
      return this.init_CompoundTag.copy();
   }

   @Override
   public void fromTag(CompoundTag tag) {
      if (tag != null) {
         this.init_CompoundTag = tag.copy();
         this.init_Str = tag.getStringOr("type", this.init_Str);
      }
   }

   @Override
   public String getTypeId() {
      return this.init_Str;
   }

   @Override
   public String getDisplayName() {
      return "! Missing: " + this.init_Str;
   }

   @Override
   public String getIcon() {
      return "!";
   }

   @Override
   public void sanitizeForSharing() {
      if (this.init_CompoundTag != null) {
         this.init_CompoundTag.remove("customFilePath");
         this.init_CompoundTag.remove("javaSource");
         this.init_CompoundTag.remove("payloadScript");
         this.init_CompoundTag.remove("payloadScriptEnabled");
         this.init_CompoundTag.remove("commandAfter");
      }
   }
}

package autismclient.util.macro;

import net.minecraft.client.Minecraft;
import net.minecraft.nbt.CompoundTag;

public final class MissingAddonAction implements MacroAction {
   private String typeId;
   private CompoundTag rawTag;

   public MissingAddonAction() {
      this.typeId = "";
      this.rawTag = new CompoundTag();
   }

   public MissingAddonAction(String typeId, CompoundTag raw) {
      this.typeId = typeId == null ? "" : typeId;
      this.rawTag = raw == null ? new CompoundTag() : raw;
   }

   public String addonTypeId() {
      return this.typeId;
   }

   @Override
   public void execute(Minecraft mc) {
   }

   @Override
   public CompoundTag toTag() {
      return this.rawTag.copy();
   }

   @Override
   public void fromTag(CompoundTag tag) {
      if (tag != null) {
         this.rawTag = tag.copy();
         this.typeId = tag.getStringOr("type", this.typeId);
      }
   }

   @Override
   public String getTypeId() {
      return this.typeId;
   }

   @Override
   public String getDisplayName() {
      return "! Missing: " + this.typeId;
   }

   @Override
   public String getIcon() {
      return "!";
   }

   @Override
   public void sanitizeForSharing() {
      if (this.rawTag != null) {
         this.rawTag.remove("customFilePath");
         this.rawTag.remove("javaSource");
         this.rawTag.remove("payloadScript");
         this.rawTag.remove("payloadScriptEnabled");
         this.rawTag.remove("commandAfter");
      }
   }
}

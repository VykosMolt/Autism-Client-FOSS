package autismclient.util.macro;

import net.minecraft.client.Minecraft;
import net.minecraft.nbt.CompoundTag;

public class SendCommandPacketAction implements MacroAction {
   public String command = "";
   public boolean stripLeadingSlash = true;

   @Override
   public void execute(Minecraft mc) {
      if (!autismclient.modules.aD.read_Z() && mc != null && mc.getConnection() != null) {
         MacroTemplateResolver.ResolveResult resolution = MacroVariableContext.resolveTemplate(this.command, mc);
         if (resolution.success()) {
            String normalized = resolution.value().trim();
            if (this.stripLeadingSlash && normalized.startsWith("/")) {
               normalized = normalized.substring(1);
            }

            if (!normalized.isBlank()) {
               mc.getConnection().sendCommand(normalized);
            }
         }
      }
   }

   @Override
   public MacroActionType getType() {
      return MacroActionType.SEND_COMMAND_PACKET;
   }

   @Override
   public CompoundTag toTag() {
      CompoundTag tag = new CompoundTag();
      tag.putString("type", "SEND_COMMAND_PACKET");
      tag.putString("command", this.command);
      tag.putBoolean("stripLeadingSlash", this.stripLeadingSlash);
      return tag;
   }

   @Override
   public void fromTag(CompoundTag tag) {
      this.command = tag.getStringOr("command", "");
      this.stripLeadingSlash = tag.getBooleanOr("stripLeadingSlash", true);
   }

   @Override
   public String getDisplayName() {
      return "Command packet " + this.command;
   }

   @Override
   public String getIcon() {
      return "/";
   }
}

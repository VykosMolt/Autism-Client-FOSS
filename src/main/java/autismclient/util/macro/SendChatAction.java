package autismclient.util.macro;

import net.minecraft.client.Minecraft;
import net.minecraft.nbt.CompoundTag;

public class SendChatAction implements MacroAction, GuiWaitAware {
   public String message = "";
   public boolean waitForGuiBefore = false;
   public boolean waitForGuiAfter = true;
   public String guiName = "";
   private boolean enabled = true;

   public SendChatAction() {
   }

   public SendChatAction(String message) {
      this.message = message;
   }

   @Override
   public CompoundTag toTag() {
      CompoundTag tag = new CompoundTag();
      tag.putString("type", MacroActionType.SEND_CHAT.name());
      tag.putString("message", this.message);
      tag.putBoolean("waitForGuiBefore", this.waitForGuiBefore);
      tag.putBoolean("waitForGuiAfter", this.waitForGuiAfter);
      tag.putString("guiName", this.guiName);
      tag.putBoolean("enabled", this.enabled);
      return tag;
   }

   @Override
   public void fromTag(CompoundTag tag) {
      if (tag.contains("message")) {
         this.message = tag.getStringOr("message", "");
      }

      this.waitForGuiBefore = GuiWaitAware.readWaitForGuiBefore(tag, false);
      this.waitForGuiAfter = GuiWaitAware.readWaitForGuiAfter(tag, true);
      if (tag.contains("guiName")) {
         this.guiName = tag.getStringOr("guiName", "");
      }

      if (tag.contains("enabled")) {
         this.enabled = tag.getBooleanOr("enabled", true);
      }
   }

   @Override
   public void execute(Minecraft mc) {
      if (!autismclient.modules.aD.read_Z() && this.message != null && !this.message.isEmpty() && mc.getConnection() != null) {
         MacroTemplateResolver.ResolveResult resolution = MacroVariableContext.resolveTemplate(this.message, mc);
         if (MacroVariableContext.requireResolved("Send chat", resolution)) {
            String resolved = resolution.value();
            if (resolved.startsWith("/") && resolved.length() > 1) {
               mc.getConnection().sendCommand(resolved.substring(1));
            } else {
               mc.getConnection().sendChat(resolved);
            }
         }
      }
   }

   @Override
   public MacroActionType getType() {
      return MacroActionType.SEND_CHAT;
   }

   @Override
   public boolean waitsForGuiBefore() {
      return this.waitForGuiBefore;
   }

   @Override
   public void setWaitForGuiBefore(boolean v) {
      this.waitForGuiBefore = v;
   }

   @Override
   public boolean waitsForGuiAfter() {
      return this.waitForGuiAfter;
   }

   @Override
   public void setWaitForGuiAfter(boolean v) {
      this.waitForGuiAfter = v;
   }

   @Override
   public String getGuiName() {
      return this.guiName;
   }

   @Override
   public void setGuiName(String name) {
      this.guiName = name != null ? name : "";
   }

   @Override
   public String getDisplayName() {
      String preview = this.message.length() > 20 ? this.message.substring(0, 20) + "..." : this.message;
      return "Chat: " + preview + GuiWaitAware.waitSuffix(this);
   }

   @Override
   public String getIcon() {
      return "CH";
   }

   @Override
   public boolean isEnabled() {
      return this.enabled;
   }

   @Override
   public void setEnabled(boolean e) {
      this.enabled = e;
   }
}

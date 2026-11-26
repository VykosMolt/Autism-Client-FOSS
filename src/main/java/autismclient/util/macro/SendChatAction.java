package autismclient.util.macro;

import net.minecraft.client.Minecraft;
import net.minecraft.nbt.CompoundTag;

public class SendChatAction implements MacroAction, GuiWaitAware {
   public String message = "";
   public boolean waitForGuiBefore = false;
   public boolean waitForGuiAfter = true;
   public String guiName = "";
   private boolean init = true;

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
      tag.putBoolean("enabled", this.init);
      return tag;
   }

   @Override
   public void fromTag(CompoundTag tag) {
      if (tag.contains("message")) {
         this.message = tag.getStringOr("message", "");
      }

      this.waitForGuiBefore = GuiWaitAware.init_Z_CompoundTag_Z(tag, false);
      this.waitForGuiAfter = GuiWaitAware.load_Z_CompoundTag_Z(tag, true);
      if (tag.contains("guiName")) {
         this.guiName = tag.getStringOr("guiName", "");
      }

      if (tag.contains("enabled")) {
         this.init = tag.getBooleanOr("enabled", true);
      }
   }

   @Override
   public void execute(Minecraft mc) {
      if (!autismclient.modules.aD.read_Z() && this.message != null && !this.message.isEmpty() && mc.getConnection() != null) {
         MacroTemplateResolver.Inner_CachePool resolution = MacroVariableContext.init_j_CachePool_Str_Minecraft(this.message, mc);
         if (MacroVariableContext.init_Z_Str_j_CachePoolArr("Send chat", resolution)) {
            String resolved = resolution.init_Str();
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
   public boolean read_Z() {
      return this.waitForGuiBefore;
   }

   @Override
   public void load(boolean v) {
      this.waitForGuiBefore = v;
   }

   @Override
   public boolean write_Z() {
      return this.waitForGuiAfter;
   }

   @Override
   public void read(boolean v) {
      this.waitForGuiAfter = v;
   }

   @Override
   public String init_Str_7() {
      return this.guiName;
   }

   @Override
   public void init_V_Str(String name) {
      this.guiName = name != null ? name : "";
   }

   @Override
   public String getDisplayName() {
      String preview = this.message.length() > 20 ? this.message.substring(0, 20) + "..." : this.message;
      return "Chat: " + preview + GuiWaitAware.init_Str_aP(this);
   }

   @Override
   public String getIcon() {
      return "CH";
   }

   @Override
   public boolean isEnabled() {
      return this.init;
   }

   @Override
   public void setEnabled(boolean e) {
      this.init = e;
   }
}

package autismclient.util.macro;

import autismclient.compat263.Compat263;
import autismclient.util.AutismSessionState;
import autismclient.util.SignEditScreenAccessor;
import java.util.Locale;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ServerboundContainerClosePacket;

public class SignEditAction implements MacroAction, GuiWaitAware {
   public SignEditAction.Inner_StreamUtil targetMode = SignEditAction.Inner_StreamUtil.CURRENT_SIGN_GUI;
   public String line1 = "";
   public String line2 = "";
   public String line3 = "";
   public String line4 = "";
   public boolean frontText = true;
   public int f_x_I = 0;
   public int f_y_I = 0;
   public int f_z_I = 0;
   public boolean waitForGuiBefore = true;
   public boolean waitForGuiAfter = false;
   public String guiName = "SIGN";
   public SignEditAction.Inner_DataUtil closeMode = SignEditAction.Inner_DataUtil.STAY_OPEN;
   public boolean closeAfterEdit = false;
   public boolean closeWithPacket = false;
   public boolean sendCommandAfter = false;
   public String commandAfter = "";
   public boolean sendClosePacketAfter = false;
   public int closePacketContainerId = 0;

   @Override
   public void execute(Minecraft mc) {
      if (!autismclient.modules.aD.read_Z() && mc != null && mc.getConnection() != null) {
         BlockPos pos = this.targetPos(mc);
         if (pos != null) {
            MacroTemplateResolver.ResolveResult resolvedLine1 = MacroVariableContext.resolveTemplate(this.line1, mc);
            MacroTemplateResolver.ResolveResult resolvedLine2 = MacroVariableContext.resolveTemplate(this.line2, mc);
            MacroTemplateResolver.ResolveResult resolvedLine3 = MacroVariableContext.resolveTemplate(this.line3, mc);
            MacroTemplateResolver.ResolveResult resolvedLine4 = MacroVariableContext.resolveTemplate(this.line4, mc);
            if (MacroVariableContext.requireResolved("Edit sign", resolvedLine1, resolvedLine2, resolvedLine3, resolvedLine4)) {
               MacroTemplateResolver.ResolveResult resolvedCommand = MacroTemplateResolver.ResolveResult.ok("");
               if (this.sendCommandAfter && this.commandAfter != null && !this.commandAfter.isBlank()) {
                  resolvedCommand = MacroVariableContext.resolveTemplate(this.commandAfter, mc);
                  if (!MacroVariableContext.requireResolved("Edit sign command", resolvedCommand)) {
                     return;
                  }
               }

               AutismSessionState.getInstance().setForceNextSignUpdate(true);
               mc.getConnection()
                  .send(
                     Compat263.newSignUpdatePacket(
                        pos,
                        this.editsFrontText(mc),
                        resolvedLine1.value(),
                        resolvedLine2.value(),
                        resolvedLine3.value(),
                        resolvedLine4.value()
                     )
                  );
               if (this.sendCommandAfter && this.commandAfter != null && !this.commandAfter.isBlank()) {
                  String command = resolvedCommand.value().trim();
                  if (command.startsWith("/")) {
                     command = command.substring(1);
                  }

                  if (!command.isBlank()) {
                     mc.getConnection().sendCommand(command);
                  }
               }

               SignEditAction.Inner_DataUtil mode = this.closeMode == null ? SignEditAction.Inner_DataUtil.STAY_OPEN : this.closeMode;
               if (mode == SignEditAction.Inner_DataUtil.SEND_CLOSE_PACKET_ONLY) {
                  mc.getConnection().send(new ServerboundContainerClosePacket(this.closePacketContainerId));
               }

               if (mode == SignEditAction.Inner_DataUtil.CLOSE_LOCAL || mode == SignEditAction.Inner_DataUtil.CLOSE_WITH_PACKET) {
                  if (mode == SignEditAction.Inner_DataUtil.CLOSE_WITH_PACKET && mc.player != null && mc.player.containerMenu != null) {
                     mc.getConnection().send(new ServerboundContainerClosePacket(mc.player.containerMenu.containerId));
                  }

                  mc.gui.setScreen(null);
               }
            }
         }
      }
   }

   private BlockPos targetPos(Minecraft mc) {
      if (this.targetMode == SignEditAction.Inner_StreamUtil.MANUAL_POS) {
         return new BlockPos(this.f_x_I, this.f_y_I, this.f_z_I);
      } else if (this.targetMode == SignEditAction.Inner_StreamUtil.LAST_INTERACTED_BLOCK) {
         return AutismSessionState.getInstance().getLastInteractedBlockPos();
      } else {
         return mc.gui.screen() instanceof SignEditScreenAccessor access ? access.autism$getSignPos() : null;
      }
   }

   private boolean editsFrontText(Minecraft mc) {
      return this.targetMode == SignEditAction.Inner_StreamUtil.CURRENT_SIGN_GUI && mc.gui.screen() instanceof SignEditScreenAccessor access ? access.autism$isFrontText() : this.frontText;
   }

   @Override
   public MacroActionType getType() {
      return MacroActionType.SIGN_EDIT;
   }

   @Override
   public CompoundTag toTag() {
      CompoundTag tag = new CompoundTag();
      tag.putString("type", "SIGN_EDIT");
      tag.putString("targetMode", this.targetMode.name());
      tag.putString("line1", this.line1);
      tag.putString("line2", this.line2);
      tag.putString("line3", this.line3);
      tag.putString("line4", this.line4);
      tag.putBoolean("frontText", this.frontText);
      tag.putInt("x", this.f_x_I);
      tag.putInt("y", this.f_y_I);
      tag.putInt("z", this.f_z_I);
      tag.putBoolean("waitForGuiBefore", this.waitForGuiBefore);
      tag.putBoolean("waitForGuiAfter", this.waitForGuiAfter);
      tag.putString("guiName", this.guiName);
      tag.putString("closeMode", this.closeMode == null ? SignEditAction.Inner_DataUtil.STAY_OPEN.name() : this.closeMode.name());
      tag.putBoolean("sendCommandAfter", this.sendCommandAfter);
      tag.putString("commandAfter", this.commandAfter);
      tag.putInt("closePacketContainerId", this.closePacketContainerId);
      return tag;
   }

   @Override
   public void fromTag(CompoundTag tag) {
      this.targetMode = MacroNbtCodecHelper.parseEnum(SignEditAction.Inner_StreamUtil.class, tag.getStringOr("targetMode", "CURRENT_SIGN_GUI"), SignEditAction.Inner_StreamUtil.CURRENT_SIGN_GUI);
      this.line1 = tag.getStringOr("line1", "");
      this.line2 = tag.getStringOr("line2", "");
      this.line3 = tag.getStringOr("line3", "");
      this.line4 = tag.getStringOr("line4", "");
      this.frontText = tag.getBooleanOr("frontText", true);
      this.f_x_I = tag.getIntOr("x", 0);
      this.f_y_I = tag.getIntOr("y", 0);
      this.f_z_I = tag.getIntOr("z", 0);
      this.waitForGuiBefore = tag.getBooleanOr("waitForGuiBefore", true);
      this.waitForGuiAfter = tag.getBooleanOr("waitForGuiAfter", false);
      this.guiName = tag.getStringOr("guiName", "SIGN");
      if (tag.contains("closeMode")) {
         this.closeMode = MacroNbtCodecHelper.parseEnum(SignEditAction.Inner_DataUtil.class, tag.getStringOr("closeMode", "STAY_OPEN"), SignEditAction.Inner_DataUtil.STAY_OPEN);
      } else if (tag.getBooleanOr("sendClosePacketAfter", false)) {
         this.closeMode = SignEditAction.Inner_DataUtil.SEND_CLOSE_PACKET_ONLY;
      } else if (tag.getBooleanOr("closeAfterEdit", false)) {
         this.closeMode = tag.getBooleanOr("closeWithPacket", false) ? SignEditAction.Inner_DataUtil.CLOSE_WITH_PACKET : SignEditAction.Inner_DataUtil.CLOSE_LOCAL;
      } else {
         this.closeMode = SignEditAction.Inner_DataUtil.STAY_OPEN;
      }

      this.closeAfterEdit = this.closeMode == SignEditAction.Inner_DataUtil.CLOSE_LOCAL || this.closeMode == SignEditAction.Inner_DataUtil.CLOSE_WITH_PACKET;
      this.closeWithPacket = this.closeMode == SignEditAction.Inner_DataUtil.CLOSE_WITH_PACKET;
      this.sendCommandAfter = tag.getBooleanOr("sendCommandAfter", false);
      this.commandAfter = tag.getStringOr("commandAfter", "");
      this.sendClosePacketAfter = this.closeMode == SignEditAction.Inner_DataUtil.SEND_CLOSE_PACKET_ONLY;
      this.closePacketContainerId = tag.getIntOr("closePacketContainerId", 0);
   }

   @Override
   public String getDisplayName() {
      String suffix = this.closeMode != SignEditAction.Inner_DataUtil.STAY_OPEN
         ? " + " + this.closeMode.name().toLowerCase(Locale.ROOT)
         : (this.sendCommandAfter ? " + command" : "");
      return "Edit sign" + suffix;
   }

   @Override
   public String getIcon() {
      return "S";
   }

   @Override
   public boolean waitsForGuiBefore() {
      return this.waitForGuiBefore;
   }

   @Override
   public boolean waitsForGuiAfter() {
      return this.waitForGuiAfter;
   }

   @Override
   public String getGuiName() {
      return this.guiName;
   }

   @Override
   public void load(boolean value) {
      this.waitForGuiBefore = value;
   }

   @Override
   public void read(boolean value) {
      this.waitForGuiAfter = value;
   }

   @Override
   public void setGuiName(String value) {
      this.guiName = value;
   }

   public enum Inner_DataUtil {
      STAY_OPEN,
      CLOSE_LOCAL,
      CLOSE_WITH_PACKET,
      SEND_CLOSE_PACKET_ONLY;
   }

   public enum Inner_StreamUtil {
      CURRENT_SIGN_GUI,
      LAST_INTERACTED_BLOCK,
      MANUAL_POS;
   }
}

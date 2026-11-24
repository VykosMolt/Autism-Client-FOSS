package autismclient.util.macro;

import autismclient.compat263.Compat263;
import autismclient.util.ck;
import autismclient.util.cm;
import java.util.Locale;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ServerboundContainerClosePacket;

public class ac implements MacroAction, aP {
   public ac.Inner_StreamUtil targetMode = ac.Inner_StreamUtil.init;
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
   public ac.Inner_DataUtil closeMode = ac.Inner_DataUtil.init;
   public boolean closeAfterEdit = false;
   public boolean closeWithPacket = false;
   public boolean sendCommandAfter = false;
   public String commandAfter = "";
   public boolean sendClosePacketAfter = false;
   public int closePacketContainerId = 0;

   @Override
   public void execute(Minecraft mc) {
      if (!autismclient.modules.aD.read_Z() && mc != null && mc.getConnection() != null) {
         BlockPos pos = this.init_BlockPos_Minecraft(mc);
         if (pos != null) {
            j.Inner_CachePool resolvedLine1 = n.init_j_CachePool_Str_Minecraft(this.line1, mc);
            j.Inner_CachePool resolvedLine2 = n.init_j_CachePool_Str_Minecraft(this.line2, mc);
            j.Inner_CachePool resolvedLine3 = n.init_j_CachePool_Str_Minecraft(this.line3, mc);
            j.Inner_CachePool resolvedLine4 = n.init_j_CachePool_Str_Minecraft(this.line4, mc);
            if (n.init_Z_Str_j_CachePoolArr("Edit sign", resolvedLine1, resolvedLine2, resolvedLine3, resolvedLine4)) {
               j.Inner_CachePool resolvedCommand = j.Inner_CachePool.init("");
               if (this.sendCommandAfter && this.commandAfter != null && !this.commandAfter.isBlank()) {
                  resolvedCommand = n.init_j_CachePool_Str_Minecraft(this.commandAfter, mc);
                  if (!n.init_Z_Str_j_CachePoolArr("Edit sign command", resolvedCommand)) {
                     return;
                  }
               }

               ck.init_ck().size(true);
               mc.getConnection()
                  .send(
                     Compat263.newSignUpdatePacket(
                        pos,
                        this.init_Z_Minecraft_2(mc),
                        resolvedLine1.init_Str(),
                        resolvedLine2.init_Str(),
                        resolvedLine3.init_Str(),
                        resolvedLine4.init_Str()
                     )
                  );
               if (this.sendCommandAfter && this.commandAfter != null && !this.commandAfter.isBlank()) {
                  String command = resolvedCommand.init_Str().trim();
                  if (command.startsWith("/")) {
                     command = command.substring(1);
                  }

                  if (!command.isBlank()) {
                     mc.getConnection().sendCommand(command);
                  }
               }

               ac.Inner_DataUtil mode = this.closeMode == null ? ac.Inner_DataUtil.init : this.closeMode;
               if (mode == ac.Inner_DataUtil.write) {
                  mc.getConnection().send(new ServerboundContainerClosePacket(this.closePacketContainerId));
               }

               if (mode == ac.Inner_DataUtil.load || mode == ac.Inner_DataUtil.read) {
                  if (mode == ac.Inner_DataUtil.read && mc.player != null && mc.player.containerMenu != null) {
                     mc.getConnection().send(new ServerboundContainerClosePacket(mc.player.containerMenu.containerId));
                  }

                  mc.gui.setScreen(null);
               }
            }
         }
      }
   }

   private BlockPos init_BlockPos_Minecraft(Minecraft mc) {
      if (this.targetMode == ac.Inner_StreamUtil.read) {
         return new BlockPos(this.f_x_I, this.f_y_I, this.f_z_I);
      } else if (this.targetMode == ac.Inner_StreamUtil.load) {
         return ck.init_ck().init_BlockPos();
      } else {
         return mc.gui.screen() instanceof cm access ? access.autism$getSignPos() : null;
      }
   }

   private boolean init_Z_Minecraft_2(Minecraft mc) {
      return this.targetMode == ac.Inner_StreamUtil.init && mc.gui.screen() instanceof cm access ? access.autism$isFrontText() : this.frontText;
   }

   @Override
   public StreamGate getType() {
      return StreamGate.f_Q_StreamGate;
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
      tag.putString("closeMode", this.closeMode == null ? ac.Inner_DataUtil.init.name() : this.closeMode.name());
      tag.putBoolean("sendCommandAfter", this.sendCommandAfter);
      tag.putString("commandAfter", this.commandAfter);
      tag.putInt("closePacketContainerId", this.closePacketContainerId);
      return tag;
   }

   @Override
   public void fromTag(CompoundTag tag) {
      this.targetMode = i.init(ac.Inner_StreamUtil.class, tag.getStringOr("targetMode", "CURRENT_SIGN_GUI"), ac.Inner_StreamUtil.init);
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
         this.closeMode = i.init(ac.Inner_DataUtil.class, tag.getStringOr("closeMode", "STAY_OPEN"), ac.Inner_DataUtil.init);
      } else if (tag.getBooleanOr("sendClosePacketAfter", false)) {
         this.closeMode = ac.Inner_DataUtil.write;
      } else if (tag.getBooleanOr("closeAfterEdit", false)) {
         this.closeMode = tag.getBooleanOr("closeWithPacket", false) ? ac.Inner_DataUtil.read : ac.Inner_DataUtil.load;
      } else {
         this.closeMode = ac.Inner_DataUtil.init;
      }

      this.closeAfterEdit = this.closeMode == ac.Inner_DataUtil.load || this.closeMode == ac.Inner_DataUtil.read;
      this.closeWithPacket = this.closeMode == ac.Inner_DataUtil.read;
      this.sendCommandAfter = tag.getBooleanOr("sendCommandAfter", false);
      this.commandAfter = tag.getStringOr("commandAfter", "");
      this.sendClosePacketAfter = this.closeMode == ac.Inner_DataUtil.write;
      this.closePacketContainerId = tag.getIntOr("closePacketContainerId", 0);
   }

   @Override
   public String getDisplayName() {
      String suffix = this.closeMode != ac.Inner_DataUtil.init
         ? " + " + this.closeMode.name().toLowerCase(Locale.ROOT)
         : (this.sendCommandAfter ? " + command" : "");
      return "Edit sign" + suffix;
   }

   @Override
   public String getIcon() {
      return "S";
   }

   @Override
   public boolean read_Z() {
      return this.waitForGuiBefore;
   }

   @Override
   public boolean write_Z() {
      return this.waitForGuiAfter;
   }

   @Override
   public String init_Str_7() {
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
   public void init_V_Str(String value) {
      this.guiName = value;
   }

   public enum Inner_DataUtil {
      init,
      load,
      read,
      write;
   }

   public enum Inner_StreamUtil {
      init,
      load,
      read;
   }
}

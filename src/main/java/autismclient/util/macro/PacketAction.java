package autismclient.util.macro;

import autismclient.AutismClientAddon;
import autismclient.util.AutismClientMessaging;
import autismclient.util.by;
import autismclient.util.de;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;

public class PacketAction implements MacroAction {
   public String packetData = "";
   public boolean regenerate = true;
   public String description = "Packet";

   public PacketAction() {
   }

   public PacketAction(String packetData, boolean regenerate, String description) {
      this.packetData = packetData;
      this.regenerate = regenerate;
      this.description = description;
   }

   @Override
   public void execute(Minecraft mc) {
      if (!autismclient.modules.aD.read_Z()) {
         if (mc.getConnection() == null) {
            AutismClientMessaging.sendPrefixed("§cNo network connection!");
         } else {
            MacroTemplateResolver.Inner_CachePool packetDataResolution = MacroVariableContext.init_j_CachePool_Str_Minecraft(this.packetData, mc);
            if (packetDataResolution.init_Z()) {
               String resolvedPacketData = packetDataResolution.init_Str();
               if (resolvedPacketData.isEmpty()) {
                  AutismClientMessaging.sendPrefixed("§cPacket data is empty!");
               } else {
                  try {
                     Packet<?> packet = autismclient.util.o.init_Packet(resolvedPacketData);
                     if (packet == null) {
                        AutismClientMessaging.sendPrefixed("§cFailed to deserialize packet! Invalid data.");
                        return;
                     }

                     if (this.regenerate) {
                        Packet<?> regenerated = de.init(packet);
                        if (regenerated == null) {
                           AutismClientMessaging.sendPrefixed("§cFailed to regenerate packet: " + packet.getClass().getSimpleName());
                           return;
                        }

                        packet = regenerated;
                     }

                     by.init(packet);
                  } catch (Exception e) {
                     AutismClientMessaging.sendPrefixed("§cPacket send error: " + e.getMessage());
                     AutismClientAddon.LOG.error("[MacroExecutor] Packet action failed", e);
                  }
               }
            }
         }
      }
   }

   @Override
   public CompoundTag toTag() {
      CompoundTag tag = new CompoundTag();
      tag.putString("type", this.getType().name());
      tag.putString("packetData", this.packetData);
      tag.putBoolean("regenerate", this.regenerate);
      tag.putString("description", this.description);
      return tag;
   }

   @Override
   public void fromTag(CompoundTag tag) {
      if (tag.contains("packetData")) {
         this.packetData = tag.getStringOr("packetData", "");
      }

      if (tag.contains("regenerate")) {
         this.regenerate = tag.getBooleanOr("regenerate", true);
      }

      if (tag.contains("description")) {
         this.description = tag.getStringOr("description", "");
      }
   }

   @Override
   public MacroActionType getType() {
      return MacroActionType.PACKET;
   }

   @Override
   public String getDisplayName() {
      return this.description.isEmpty() ? "Unknown Packet" : this.description;
   }

   @Override
   public String getIcon() {
      return "Pkt";
   }
}

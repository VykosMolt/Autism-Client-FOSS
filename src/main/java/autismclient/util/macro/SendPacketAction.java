package autismclient.util.macro;

import autismclient.util.AutismClientMessaging;
import autismclient.util.PacketNameResolver;
import autismclient.util.DirectPacketSender;
import autismclient.util.AutismSessionState;
import autismclient.util.PacketRegenerator;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.protocol.Packet;

public class SendPacketAction implements MacroAction, GuiWaitAware {
   public List<AutismSessionState.DelayedPacket> packets = new ArrayList<>();
   public String customName = "";
   public int sendCount = 1;
   public boolean waitForGuiBefore = false;
   public boolean waitForGuiAfter = false;
   public String guiName = "";

   public List<AutismSessionState.DelayedPacket> init_List_8() {
      return this.packets;
   }

   public static Packet<?> init_Packet_ck_CachePool(AutismSessionState.DelayedPacket qp) {
      if (qp != null && qp.packet != null) {
         return qp.isExactReplay() ? qp.packet : PacketRegenerator.init(qp.packet);
      } else {
         return null;
      }
   }

   public static int init_I_I_5(int value) {
      return Math.min(10000, Math.max(1, value));
   }

   public void init_V_10() {
   }

   @Override
   public void execute(Minecraft mc) {
      if (!autismclient.modules.aD.read_Z()) {
         if (mc.player != null && mc.getConnection() != null) {
            int successCount = 0;
            int failCount = 0;
            int repeats = init_I_I_5(this.sendCount);

            for (AutismSessionState.DelayedPacket qp : this.packets) {
               if (qp != null && qp.packet != null) {
                  for (int k = 0; k < repeats; k++) {
                     try {
                        Packet<?> toSend = init_Packet_ck_CachePool(qp);
                        if (toSend == null) {
                           failCount++;
                           break;
                        }

                        DirectPacketSender.init(toSend);
                        successCount++;
                     } catch (Exception e) {
                        AutismClientMessaging.sendPrefixed("§cSend failed: " + e.getMessage());
                        failCount++;
                        break;
                     }
                  }
               } else {
                  failCount++;
               }
            }

            if (failCount > 0) {
               AutismClientMessaging.sendPrefixed(String.format("§eSent %d/%d packets (%d failed)", successCount, this.packets.size() * repeats, failCount));
            }
         } else {
            AutismClientMessaging.sendPrefixed("§cNo network connection!");
         }
      }
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
   public MacroActionType getType() {
      return MacroActionType.SEND_PACKET;
   }

   @Override
   public CompoundTag toTag() {
      CompoundTag tag = new CompoundTag();
      tag.putString("type", "SEND_PACKET");
      tag.putString("customName", this.customName);
      tag.putInt("sendCount", init_I_I_5(this.sendCount));
      tag.putBoolean("waitForGuiBefore", this.waitForGuiBefore);
      tag.putBoolean("waitForGuiAfter", this.waitForGuiAfter);
      tag.putString("guiName", this.guiName);
      ListTag packetList = new ListTag();

      for (AutismSessionState.DelayedPacket qp : this.packets) {
         CompoundTag packetTag = autismclient.util.MacroClipboardCodec.init(qp);
         if (packetTag != null) {
            packetList.add(packetTag);
         }
      }

      tag.put("packets", packetList);
      return tag;
   }

   @Override
   public void fromTag(CompoundTag tag) {
      this.packets.clear();
      this.customName = tag.getStringOr("customName", "");
      this.sendCount = tag.contains("sendCount") ? init_I_I_5(tag.getIntOr("sendCount", 1)) : 1;
      this.waitForGuiBefore = GuiWaitAware.init_Z_CompoundTag_Z(tag, false);
      this.waitForGuiAfter = GuiWaitAware.load_Z_CompoundTag_Z(tag, false);
      if (tag.contains("guiName")) {
         this.guiName = tag.getStringOr("guiName", "");
      }

      if (tag.contains("packets")) {
         ListTag packetList = (ListTag)tag.get("packets");
         if (packetList != null) {
            for (int i = 0; i < packetList.size(); i++) {
               Tag element = packetList.get(i);
               if (element instanceof CompoundTag) {
                  AutismSessionState.DelayedPacket qp = autismclient.util.MacroClipboardCodec.init_ck_CachePool((CompoundTag)element);
                  if (qp != null) {
                     this.packets.add(qp);
                  }
               }
            }
         }
      }
   }

   @Override
   public String getDisplayName() {
      String base;
      if (this.customName != null && !this.customName.trim().isEmpty()) {
         base = "Send " + this.customName.trim();
      } else if (this.packets.isEmpty()) {
         base = "Send (empty)";
      } else {
         Map<String, Integer> nameCounts = new LinkedHashMap<>();

         for (AutismSessionState.DelayedPacket qp : this.packets) {
            if (qp.packet != null) {
               String name = PacketNameResolver.init_Str_Packet(qp.packet);
               nameCounts.merge(name, 1, Integer::sum);
            }
         }

         if (nameCounts.isEmpty()) {
            base = "Send (empty)";
         } else if (nameCounts.size() == 1) {
            Entry<String, Integer> entry = nameCounts.entrySet().iterator().next();
            String pktName = entry.getKey();
            int count = entry.getValue();
            base = count == 1 ? "Send " + pktName : "Send " + pktName + " x" + count;
         } else {
            base = "Send Group (" + this.packets.size() + ")";
         }
      }

      if (this.sendCount > 1) {
         base = base + " x" + this.sendCount;
      }

      return base + GuiWaitAware.init_Str_aP(this);
   }

   @Override
   public String getIcon() {
      return "P";
   }
}

package autismclient.util.macro;

import autismclient.util.AutismClientMessaging;
import autismclient.util.bo;
import autismclient.util.ck;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ServerboundContainerClickPacket;

public class PacketClickAction implements MacroAction {
   public bo.Inner_StreamUtil target;
   public String mode = bo.Inner_DataUtil.LEFT_CLICK.name();
   public int times = 1;
   public boolean queue = false;
   private transient boolean init = false;

   public PacketClickAction() {
   }

   public PacketClickAction(bo.Inner_StreamUtil target, int times, boolean queue) {
      this.target = target;
      this.times = Math.max(1, times);
      this.queue = queue;
      this.load();
   }

   public void init(bo.Inner_StreamUtil newTarget) {
      this.read();
      this.target = newTarget == null ? null : newTarget.init_bo_StreamUtil_bo_DataUtil(this.init_bo_DataUtil());
      this.load();
   }

   private bo.Inner_DataUtil init_bo_DataUtil() {
      if (this.target != null && (this.mode == null || this.mode.isBlank())) {
         return this.target.init_bo_DataUtil() == null ? bo.Inner_DataUtil.LEFT_CLICK : this.target.init_bo_DataUtil();
      } else {
         return bo.Inner_DataUtil.init(this.mode);
      }
   }

   private void load() {
      if (this.target != null && !this.init) {
         autismclient.util.D.init_V(this.target.init_I());
         this.init = true;
      }
   }

   private void load(ClientPacketListener conn) {
      if (this.init && this.target != null) {
         autismclient.util.D.init(this.target.init_I(), conn);
         this.init = false;
      } else {
         this.init = false;
      }
   }

   private void read() {
      if (this.init && this.target != null) {
         autismclient.util.D.init(this.target.init_I(), (ClientPacketListener)null);
         this.init = false;
      } else {
         this.init = false;
      }
   }

   @Override
   public void execute(Minecraft mc) {
      if (!autismclient.modules.aD.read_Z()) {
         if (mc.getConnection() == null) {
            AutismClientMessaging.sendPrefixed("§cNo network connection!");
         } else if (this.target == null) {
            AutismClientMessaging.sendPrefixed("§cPacket Click has no captured target.");
         } else {
            int count = Math.max(1, this.times);
            bo.Inner_StreamUtil effectiveTarget = this.target.init_bo_StreamUtil_bo_DataUtil(this.init_bo_DataUtil());

            for (int i = 0; i < count; i++) {
               ServerboundContainerClickPacket packet = effectiveTarget.init_ServerboundContain();
               if (this.queue) {
                  ck.init_ck().load_V_Packet(packet);
               } else {
                  ck.init_ck().init_V_ClientPacketListen_Packet(mc.getConnection(), packet);
               }
            }

            this.load(mc.getConnection());
         }
      }
   }

   public void init_V_15() {
      this.read();
   }

   public void init(ClientPacketListener conn) {
      this.load(conn);
   }

   @Override
   public CompoundTag toTag() {
      CompoundTag tag = new CompoundTag();
      tag.putString("type", this.getType().name());
      tag.putString("mode", this.init_bo_DataUtil().name());
      tag.putInt("times", Math.max(1, this.times));
      tag.putBoolean("queue", this.queue);
      if (this.target != null) {
         tag.put("target", this.target.init_bo_StreamUtil_bo_DataUtil(this.init_bo_DataUtil()).init_CompoundTag());
      }

      return tag;
   }

   @Override
   public void fromTag(CompoundTag tag) {
      this.times = Math.max(1, tag.getIntOr("times", 1));
      this.queue = tag.getBooleanOr("queue", false);
      this.mode = tag.getStringOr("mode", "");
      this.target = tag.getCompound("target").map(bo.Inner_StreamUtil::init_bo_StreamUtil_CompoundTag).orElse(null);
      if (this.mode == null || this.mode.isBlank()) {
         this.mode = this.target != null && this.target.init_bo_DataUtil() != null
            ? this.target.init_bo_DataUtil().name()
            : bo.Inner_DataUtil.LEFT_CLICK.name();
      }

      if (this.target != null) {
         this.target = this.target.init_bo_StreamUtil_bo_DataUtil(this.init_bo_DataUtil());
      }
   }

   @Override
   public StreamGate getType() {
      return StreamGate.PACKET_CLICK;
   }

   @Override
   public String getDisplayName() {
      if (this.target == null) {
         return "Packet Click (empty)";
      }

      String suffix = this.times > 1 ? " x" + this.times : "";
      return "Packet Click " + this.target.init_bo_StreamUtil_bo_DataUtil(this.init_bo_DataUtil()).init_Str() + suffix;
   }

   @Override
   public String getIcon() {
      return "Pkt";
   }
}

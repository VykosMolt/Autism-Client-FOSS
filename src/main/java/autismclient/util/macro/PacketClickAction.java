package autismclient.util.macro;

import autismclient.util.AutismClientMessaging;
import autismclient.util.ContainerClickSnapshot;
import autismclient.util.AutismSessionState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ServerboundContainerClickPacket;

public class PacketClickAction implements MacroAction {
   public ContainerClickSnapshot.Inner_StreamUtil target;
   public String mode = ContainerClickSnapshot.Inner_DataUtil.LEFT_CLICK.name();
   public int times = 1;
   public boolean queue = false;
   private transient boolean init = false;

   public PacketClickAction() {
   }

   public PacketClickAction(ContainerClickSnapshot.Inner_StreamUtil target, int times, boolean queue) {
      this.target = target;
      this.times = Math.max(1, times);
      this.queue = queue;
      this.load();
   }

   public void setTarget(ContainerClickSnapshot.Inner_StreamUtil newTarget) {
      this.read();
      this.target = newTarget == null ? null : newTarget.withMode(this.init_bo_DataUtil());
      this.load();
   }

   private ContainerClickSnapshot.Inner_DataUtil init_bo_DataUtil() {
      if (this.target != null && (this.mode == null || this.mode.isBlank())) {
         return this.target.mode() == null ? ContainerClickSnapshot.Inner_DataUtil.LEFT_CLICK : this.target.mode();
      } else {
         return ContainerClickSnapshot.Inner_DataUtil.byName(this.mode);
      }
   }

   private void load() {
      if (this.target != null && !this.init) {
         autismclient.util.ContainerCloseGuard.holdOpen(this.target.containerId());
         this.init = true;
      }
   }

   private void load(ClientPacketListener conn) {
      if (this.init && this.target != null) {
         autismclient.util.ContainerCloseGuard.hold(this.target.containerId(), conn);
         this.init = false;
      } else {
         this.init = false;
      }
   }

   private void read() {
      if (this.init && this.target != null) {
         autismclient.util.ContainerCloseGuard.hold(this.target.containerId(), (ClientPacketListener)null);
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
            ContainerClickSnapshot.Inner_StreamUtil effectiveTarget = this.target.withMode(this.init_bo_DataUtil());

            for (int i = 0; i < count; i++) {
               ServerboundContainerClickPacket packet = effectiveTarget.toPacket();
               if (this.queue) {
                  AutismSessionState.getInstance().enqueueExactPacket(packet);
               } else {
                  AutismSessionState.getInstance().sendPacketBypassingInterception(mc.getConnection(), packet);
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
         tag.put("target", this.target.withMode(this.init_bo_DataUtil()).toTag());
      }

      return tag;
   }

   @Override
   public void fromTag(CompoundTag tag) {
      this.times = Math.max(1, tag.getIntOr("times", 1));
      this.queue = tag.getBooleanOr("queue", false);
      this.mode = tag.getStringOr("mode", "");
      this.target = tag.getCompound("target").map(ContainerClickSnapshot.Inner_StreamUtil::fromTag).orElse(null);
      if (this.mode == null || this.mode.isBlank()) {
         this.mode = this.target != null && this.target.mode() != null
            ? this.target.mode().name()
            : ContainerClickSnapshot.Inner_DataUtil.LEFT_CLICK.name();
      }

      if (this.target != null) {
         this.target = this.target.withMode(this.init_bo_DataUtil());
      }
   }

   @Override
   public MacroActionType getType() {
      return MacroActionType.PACKET_CLICK;
   }

   @Override
   public String getDisplayName() {
      if (this.target == null) {
         return "Packet Click (empty)";
      }

      String suffix = this.times > 1 ? " x" + this.times : "";
      return "Packet Click " + this.target.withMode(this.init_bo_DataUtil()).init_Str() + suffix;
   }

   @Override
   public String getIcon() {
      return "Pkt";
   }
}

package autismclient.util.macro;

import autismclient.util.AutismClientMessaging;
import autismclient.util.ContainerClickSnapshot;
import autismclient.util.AutismSessionState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ServerboundContainerClickPacket;

public class PacketClickAction implements MacroAction {
   public ContainerClickSnapshot.Snapshot target;
   public String mode = ContainerClickSnapshot.ClickMode.LEFT_CLICK.name();
   public int times = 1;
   public boolean queue = false;
   private transient boolean holdingOpen = false;

   public PacketClickAction() {
   }

   public PacketClickAction(ContainerClickSnapshot.Snapshot target, int times, boolean queue) {
      this.target = target;
      this.times = Math.max(1, times);
      this.queue = queue;
      this.holdContainerOpen();
   }

   public void setTarget(ContainerClickSnapshot.Snapshot newTarget) {
      this.releaseHoldOffline();
      this.target = newTarget == null ? null : newTarget.withMode(this.effectiveMode());
      this.holdContainerOpen();
   }

   private ContainerClickSnapshot.ClickMode effectiveMode() {
      if (this.target != null && (this.mode == null || this.mode.isBlank())) {
         return this.target.mode() == null ? ContainerClickSnapshot.ClickMode.LEFT_CLICK : this.target.mode();
      } else {
         return ContainerClickSnapshot.ClickMode.byName(this.mode);
      }
   }

   private void holdContainerOpen() {
      if (this.target != null && !this.holdingOpen) {
         autismclient.util.ContainerCloseGuard.holdOpen(this.target.containerId());
         this.holdingOpen = true;
      }
   }

   private void releaseHold(ClientPacketListener conn) {
      if (this.holdingOpen && this.target != null) {
         autismclient.util.ContainerCloseGuard.hold(this.target.containerId(), conn);
         this.holdingOpen = false;
      } else {
         this.holdingOpen = false;
      }
   }

   private void releaseHoldOffline() {
      if (this.holdingOpen && this.target != null) {
         autismclient.util.ContainerCloseGuard.hold(this.target.containerId(), (ClientPacketListener)null);
         this.holdingOpen = false;
      } else {
         this.holdingOpen = false;
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
            ContainerClickSnapshot.Snapshot effectiveTarget = this.target.withMode(this.effectiveMode());

            for (int i = 0; i < count; i++) {
               ServerboundContainerClickPacket packet = effectiveTarget.toPacket();
               if (this.queue) {
                  AutismSessionState.getInstance().enqueueExactPacket(packet);
               } else {
                  AutismSessionState.getInstance().sendPacketBypassingInterception(mc.getConnection(), packet);
               }
            }

            this.releaseHold(mc.getConnection());
         }
      }
   }

   public void init_V_15() {
      this.releaseHoldOffline();
   }

   public void init(ClientPacketListener conn) {
      this.releaseHold(conn);
   }

   @Override
   public CompoundTag toTag() {
      CompoundTag tag = new CompoundTag();
      tag.putString("type", this.getType().name());
      tag.putString("mode", this.effectiveMode().name());
      tag.putInt("times", Math.max(1, this.times));
      tag.putBoolean("queue", this.queue);
      if (this.target != null) {
         tag.put("target", this.target.withMode(this.effectiveMode()).toTag());
      }

      return tag;
   }

   @Override
   public void fromTag(CompoundTag tag) {
      this.times = Math.max(1, tag.getIntOr("times", 1));
      this.queue = tag.getBooleanOr("queue", false);
      this.mode = tag.getStringOr("mode", "");
      this.target = tag.getCompound("target").map(ContainerClickSnapshot.Snapshot::fromTag).orElse(null);
      if (this.mode == null || this.mode.isBlank()) {
         this.mode = this.target != null && this.target.mode() != null
            ? this.target.mode().name()
            : ContainerClickSnapshot.ClickMode.LEFT_CLICK.name();
      }

      if (this.target != null) {
         this.target = this.target.withMode(this.effectiveMode());
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
      return "Packet Click " + this.target.withMode(this.effectiveMode()).init_Str() + suffix;
   }

   @Override
   public String getIcon() {
      return "Pkt";
   }
}

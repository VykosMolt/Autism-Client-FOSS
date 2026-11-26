package autismclient.mixin;

import autismclient.e.FileCache;
import autismclient.g.BufferHelper;
import autismclient.g.RefQueue;
import autismclient.modules.PoolEntry;
import autismclient.modules.aD;
import autismclient.util.AutismConfig;
import autismclient.util.AutismLiteVariant;
import autismclient.util.ci;
import autismclient.util.ck;
import autismclient.util.b.CustomMenuTracker;
import autismclient.util.e.LeafDupePacketTracker;
import java.net.MalformedURLException;
import java.net.URL;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientCommonPacketListenerImpl;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.client.multiplayer.ServerData.ServerPackStatus;
import net.minecraft.client.resources.server.DownloadedPackSource;
import net.minecraft.client.resources.server.ServerPackManager.PackPromptStatus;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.common.ClientboundClearDialogPacket;
import net.minecraft.network.protocol.common.ClientboundPingPacket;
import net.minecraft.network.protocol.common.ClientboundResourcePackPushPacket;
import net.minecraft.network.protocol.common.ClientboundShowDialogPacket;
import net.minecraft.network.protocol.common.ServerboundResourcePackPacket;
import net.minecraft.network.protocol.common.ServerboundResourcePackPacket.Action;
import net.minecraft.network.protocol.configuration.ClientConfigurationPacketListener;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientCommonPacketListenerImpl.class)
public abstract class AutismClientCommonNetworkHandlerMixin {
   @Shadow
   @Final
   protected Minecraft minecraft;
   @Shadow
   @Final
   protected Connection connection;
   @Shadow
   @Final
   protected @Nullable ServerData serverData;

   @Shadow
   public abstract void send(Packet<?> var1);

   @Inject(method = "handleResourcePackPush", at = @At("HEAD"), cancellable = true)
   private void yang$onResourcePackSend(ClientboundResourcePackPushPacket packet, CallbackInfo ci) {
      if (!aD.read_Z() && this.minecraft.isSameThread()) {
         BufferHelper.Inner_StreamUtil context = BufferHelper.init_BufferHelper_Strea(this.connection);
         if (context != null) {
            ck shared = ck.init_ck();
            PoolEntry module = PoolEntry.init_PoolEntry();
            boolean forceDeny = shared.clear_Z() || module != null && module.set_Z();
            boolean sharedBypass = shared.add_Z();
            boolean serializedBypass = module != null ? module.pop_Z() : AutismConfig.getGlobal().pretendPackAccepted;
            if (!AutismLiteVariant.enabled() && !FileCache.init()) {
               forceDeny = false;
               sharedBypass = false;
               serializedBypass = false;
            }

            autismclient.g.FileCache.Inner_DataUtil selectedPolicy = RefQueue.init(context.init_BufferHelper_Cache(), sharedBypass, serializedBypass);
            RefQueue.Inner_StreamUtil verdict = RefQueue.init(packet, forceDeny, selectedPolicy != null);
            if (verdict.init_RefQueue_DataUtil() != RefQueue.Inner_DataUtil.PASS) {
               if (verdict.init_RefQueue_DataUtil() == RefQueue.Inner_DataUtil.INVALID_URL) {
                  this.send(new ServerboundResourcePackPacket(packet.id(), Action.INVALID_URL));
                  ci.cancel();
               } else if (verdict.init_RefQueue_DataUtil() == RefQueue.Inner_DataUtil.BYPASS_SUCCESS || verdict.init_RefQueue_DataUtil() == RefQueue.Inner_DataUtil.DECLINE) {
                  autismclient.g.FileCache.Inner_DataUtil policy = verdict.init_RefQueue_DataUtil() == RefQueue.Inner_DataUtil.DECLINE
                     ? autismclient.g.FileCache.Inner_DataUtil.DECLINE
                     : selectedPolicy;

                  URL url;
                  try {
                     url = new URL(packet.url());
                  } catch (MalformedURLException impossible) {
                     this.send(new ServerboundResourcePackPacket(packet.id(), Action.INVALID_URL));
                     ci.cancel();
                     return;
                  }

                  DownloadedPackSource packSource = this.minecraft.getDownloadedPackSource();
                  ServerPackStatus savedStatus = this.serverData == null ? ServerPackStatus.PROMPT : this.serverData.getResourcePackStatus();
                  boolean prompt = !forceDeny && (savedStatus == ServerPackStatus.PROMPT || packet.required() && savedStatus == ServerPackStatus.DISABLED);
                  PackPromptStatus promptStatus;
                  if (forceDeny) {
                     promptStatus = PackPromptStatus.DECLINED;
                  } else if (prompt) {
                     promptStatus = PackPromptStatus.PENDING;
                  } else if (savedStatus == ServerPackStatus.ENABLED) {
                     promptStatus = PackPromptStatus.ALLOWED;
                  } else {
                     promptStatus = PackPromptStatus.DECLINED;
                  }

                  packSource.configureForServerControl(this.connection, promptStatus);
                  autismclient.g.FileCache.init(context, packet.id(), policy);
                  if (!prompt) {
                     packSource.pushPack(packet.id(), url, packet.hash());
                     ci.cancel();
                  }
               }
            }
         }
      }
   }

   @Inject(method = "handleShowDialog", at = @At("TAIL"))
   private void autism$trackCustomMenu(ClientboundShowDialogPacket packet, CallbackInfo ci) {
      String phase = (Object)this instanceof ClientConfigurationPacketListener ? "CONFIGURATION" : "PLAY";
      CustomMenuTracker.init(packet, phase);
   }

   @Inject(method = "handleClearDialog", at = @At("TAIL"))
   private void autism$clearCustomMenu(ClientboundClearDialogPacket packet, CallbackInfo ci) {
      String phase = (Object)this instanceof ClientConfigurationPacketListener ? "CONFIGURATION" : "PLAY";
      CustomMenuTracker.init(packet, phase);
   }

   @Inject(method = "handlePing", at = @At("HEAD"))
   private void autism$pingAfterOwnFlags(ClientboundPingPacket packet, CallbackInfo ci) {
      if (Minecraft.getInstance().isSameThread()) {
         autismclient.util.ci.init(packet.getId());
         if (!AutismLiteVariant.enabled()) {
            LeafDupePacketTracker.load(packet);
         }
      }
   }
}

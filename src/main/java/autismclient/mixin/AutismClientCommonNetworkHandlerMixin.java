package autismclient.mixin;

import autismclient.e.FileCache;
import autismclient.protector.ConnectionPrivacyRegistry;
import autismclient.protector.ResourcePackPushValidator;
import autismclient.modules.ModuleRuntimeManager;
import autismclient.modules.aD;
import autismclient.util.AutismConfig;
import autismclient.util.AutismLiteVariant;
import autismclient.util.SprintStateTracker;
import autismclient.util.AutismSessionState;
import autismclient.util.custommenu.CustomMenuTracker;
import autismclient.util.leafdupe.LeafDupePacketTracker;
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
         ConnectionPrivacyRegistry.ConnectionSession context = ConnectionPrivacyRegistry.getActive(this.connection);
         if (context != null) {
            AutismSessionState shared = AutismSessionState.getInstance();
            ModuleRuntimeManager module = ModuleRuntimeManager.getInstance();
            boolean forceDeny = shared.isAutoDenyResourcePack() || module != null && module.isAutoDenyResourcePack();
            boolean sharedBypass = shared.isPretendPackAccepted();
            boolean serializedBypass = module != null ? module.isPretendPackAccepted() : AutismConfig.getGlobal().pretendPackAccepted;
            if (!AutismLiteVariant.enabled() && !FileCache.init()) {
               forceDeny = false;
               sharedBypass = false;
               serializedBypass = false;
            }

            autismclient.protector.ResourcePackSessionTracker.PackPolicy selectedPolicy = ResourcePackPushValidator.init(context.privacyMode(), sharedBypass, serializedBypass);
            ResourcePackPushValidator.Inner_StreamUtil verdict = ResourcePackPushValidator.init(packet, forceDeny, selectedPolicy != null);
            if (verdict.verdict() != ResourcePackPushValidator.Inner_DataUtil.PASS) {
               if (verdict.verdict() == ResourcePackPushValidator.Inner_DataUtil.INVALID_URL) {
                  this.send(new ServerboundResourcePackPacket(packet.id(), Action.INVALID_URL));
                  ci.cancel();
               } else if (verdict.verdict() == ResourcePackPushValidator.Inner_DataUtil.BYPASS_SUCCESS || verdict.verdict() == ResourcePackPushValidator.Inner_DataUtil.DECLINE) {
                  autismclient.protector.ResourcePackSessionTracker.PackPolicy policy = verdict.verdict() == ResourcePackPushValidator.Inner_DataUtil.DECLINE
                     ? autismclient.protector.ResourcePackSessionTracker.PackPolicy.DECLINE
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
                  autismclient.protector.ResourcePackSessionTracker.init(context, packet.id(), policy);
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
         autismclient.util.SprintStateTracker.init(packet.getId());
         if (!AutismLiteVariant.enabled()) {
            LeafDupePacketTracker.load(packet);
         }
      }
   }
}

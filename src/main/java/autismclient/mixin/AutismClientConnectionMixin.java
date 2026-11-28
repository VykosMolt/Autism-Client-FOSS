package autismclient.mixin;

import autismclient.AutismClientAddon;
import autismclient.ai.AiMenuClose;
import autismclient.api.custommenu.CustomMenuAdapterRegistry;
import autismclient.protector.ConnectionPrivacyRegistry;
import autismclient.protector.ServerPackSelectionRegistry;
import autismclient.protector.OutboundPacketFilterHandler;
import autismclient.protector.ResourcePackPushValidator;
import autismclient.mixin.accessor.AutismClientConnectionAccessor;
import autismclient.modules.BlinkPacketManager;
import autismclient.modules.Module;
import autismclient.modules.ModuleRuntimeManager;
import autismclient.modules.BacktrackModule;
import autismclient.modules.aD;
import autismclient.modules.ScaffoldModule;
import autismclient.modules.LeafDupeModule;
import autismclient.modules.al;
import autismclient.modules.f;
import autismclient.modules.DangerModule;
import autismclient.util.AutismConfig;
import autismclient.util.AutismLiteVariant;
import autismclient.util.ContainerCloseGuard;
import autismclient.util.InteractionTargetRef;
import autismclient.util.QueuedCombatAction;
import autismclient.util.PacketCaptureState;
import autismclient.util.RotationMath;
import autismclient.util.ServerRotationTracker;
import autismclient.util.SprintStateTracker;
import autismclient.util.AutismSessionState;
import autismclient.util.SilentMovementCoordinator;
import autismclient.util.BedSleepDesyncHelper;
import autismclient.util.CommandProbeIdAllocator;
import autismclient.util.custommenu.CustomMenuTracker;
import autismclient.util.leafdupe.LeafDupePacketTracker;
import autismclient.util.g.PkgCls_a;
import autismclient.util.macro.PingSpoofScheduler;
import autismclient.util.macro.MacroExecutor;
import autismclient.util.macro.XCarryAction;
import autismclient.util.macro.ServerTickPhaseTracker;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.ChannelPromise;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.Collections;
import java.util.Locale;
import java.util.Set;
import net.minecraft.client.Minecraft;
import net.minecraft.network.Connection;
import net.minecraft.network.PacketListener;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.common.ClientboundClearDialogPacket;
import net.minecraft.network.protocol.common.ClientboundDisconnectPacket;
import net.minecraft.network.protocol.common.ClientboundKeepAlivePacket;
import net.minecraft.network.protocol.common.ClientboundPingPacket;
import net.minecraft.network.protocol.common.ClientboundShowDialogPacket;
import net.minecraft.network.protocol.common.ServerboundCustomClickActionPacket;
import net.minecraft.network.protocol.common.ServerboundCustomPayloadPacket;
import net.minecraft.network.protocol.common.ServerboundKeepAlivePacket;
import net.minecraft.network.protocol.common.ServerboundPongPacket;
import net.minecraft.network.protocol.common.ServerboundResourcePackPacket;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBundlePacket;
import net.minecraft.network.protocol.game.ClientboundCommandSuggestionsPacket;
import net.minecraft.network.protocol.game.ClientboundDisguisedChatPacket;
import net.minecraft.network.protocol.game.ClientboundEntityPositionSyncPacket;
import net.minecraft.network.protocol.game.ClientboundOpenScreenPacket;
import net.minecraft.network.protocol.game.ClientboundPlayerChatPacket;
import net.minecraft.network.protocol.game.ClientboundSetEntityMotionPacket;
import net.minecraft.network.protocol.game.ClientboundSystemChatPacket;
import net.minecraft.network.protocol.game.ServerboundChatCommandPacket;
import net.minecraft.network.protocol.game.ServerboundChatCommandSignedPacket;
import net.minecraft.network.protocol.game.ServerboundChatPacket;
import net.minecraft.network.protocol.game.ServerboundContainerButtonClickPacket;
import net.minecraft.network.protocol.game.ServerboundContainerClickPacket;
import net.minecraft.network.protocol.game.ServerboundContainerClosePacket;
import net.minecraft.network.protocol.game.ServerboundEditBookPacket;
import net.minecraft.network.protocol.game.ServerboundInteractPacket;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket;
import net.minecraft.network.protocol.game.ServerboundPlayerActionPacket;
import net.minecraft.network.protocol.game.ServerboundPlayerCommandPacket;
import net.minecraft.network.protocol.game.ServerboundSetCreativeModeSlotPacket;
import net.minecraft.network.protocol.game.ServerboundSignUpdatePacket;
import net.minecraft.network.protocol.game.ServerboundUseItemOnPacket;
import net.minecraft.network.protocol.game.ServerboundUseItemPacket;
import net.minecraft.network.protocol.game.ServerboundPlayerCommandPacket.Action;
import net.minecraft.server.network.EventLoopGroupHolder;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Connection.class)
public abstract class AutismClientConnectionMixin {
   @Unique
   private static final boolean AUTISM_PACKET_TRACE = Boolean.getBoolean("autism.packet.trace");
   @Unique
   private volatile boolean autism$spoofPipelineInstalled;
   @Unique
   private volatile PacketListener autism$protocolHintListener;
   @Unique
   private volatile String autism$protocolHintCache = "";
   @Unique
   private static final String AUTISM_SPOOF_FILTER = "autism_spoof_filter";
   @Unique
   private static volatile Module autism$noFallCached;
   @Unique
   private static volatile int autism$noFallRevision = -1;

   @Unique
   private static Module autism$noFallModule() {
      int revision = al.init_I();
      if (revision != autism$noFallRevision) {
         autism$noFallCached = al.init_Module_Str("no-fall");
         autism$noFallRevision = revision;
      }

      return autism$noFallCached;
   }

   @Unique
   private static void autism$snapshotPrivacyMode(Connection connection, SocketAddress peerAddress) {
      ConnectionPrivacyRegistry.PrivacyMode mode = AutismConfig.getGlobal().spoofClientVanilla ? ConnectionPrivacyRegistry.PrivacyMode.VANILLA : ConnectionPrivacyRegistry.PrivacyMode.MODDED;
      ConnectionPrivacyRegistry.register(connection, mode, false, peerAddress);
   }

   @Inject(
      method = "connect(Ljava/net/InetSocketAddress;Lnet/minecraft/server/network/EventLoopGroupHolder;Lnet/minecraft/network/Connection;)Lio/netty/channel/ChannelFuture;",
      at = @At("HEAD")
   )
   private static void autism$snapshotPrivacyBeforeConnect(
      InetSocketAddress address, EventLoopGroupHolder eventLoopGroup, Connection connection, CallbackInfoReturnable<ChannelFuture> cir
   ) {
      autism$snapshotPrivacyMode(connection, address);
   }

   @Inject(method = "channelActive", at = @At("HEAD"))
   private void autism$onChannelActive(ChannelHandlerContext context, CallbackInfo ci) {
      this.autism$spoofPipelineInstalled = false;
      this.autism$ensureSpoofPipelineFilter();
   }

   @Inject(method = "channelInactive", at = @At("HEAD"))
   private void autism$onChannelInactive(ChannelHandlerContext context, CallbackInfo ci) {
      this.autism$spoofPipelineInstalled = false;
      PacketCaptureState.read();
      if (((Connection)(Object)this).getPacketListener() instanceof ClientGamePacketListener) {
         ServerRotationTracker.reset();
         ScaffoldModule.onDisconnect();
         if (!AutismLiteVariant.enabled()) {
            LeafDupeModule.onDisconnect();
         }

         ServerPackSelectionRegistry.load();
         ResourcePackPushValidator.init();
      }
   }

   @Inject(method = "doSendPacket", at = @At("HEAD"), cancellable = true)
   private void autism$recordWrittenServerRotation(Packet<?> packet, ChannelFutureListener listener, boolean flush, CallbackInfo ci) {
      Channel channel = null;

      try {
         channel = ((AutismClientConnectionAccessor)(Object)this).getChannel();
      } catch (Throwable var7) {
      }

      if (!OutboundPacketFilterHandler.init((Connection)(Object)this, channel, packet, listener, flush)) {
         ci.cancel();
      } else {
         if (!AutismLiteVariant.enabled() && !LeafDupeModule.allowSend((Connection)(Object)this, packet)) {
            ci.cancel();
            return;
         }

         ServerRotationTracker.onOutgoingPacket(packet);
         if (!AutismLiteVariant.enabled()) {
            BacktrackModule.init_V_Connection_Packet((Connection)(Object)this, packet);
         }

         if (!AutismLiteVariant.enabled()) {
            LeafDupePacketTracker.init((Connection)(Object)this, packet);
         }

         ScaffoldModule.onPacketWritten(packet);
         if (!AutismLiteVariant.enabled()) {
            AiMenuClose.init(packet);
         }
      }
   }

   @WrapOperation(
      method = "doSendPacket",
      at = {
            @At(value = "INVOKE", target = "Lio/netty/channel/Channel;write(Ljava/lang/Object;)Lio/netty/channel/ChannelFuture;"),
            @At(value = "INVOKE", target = "Lio/netty/channel/Channel;writeAndFlush(Ljava/lang/Object;)Lio/netty/channel/ChannelFuture;")
      }
   )
   private ChannelFuture autism$completeReviewedPayload(Channel channel, Object packet, Operation<ChannelFuture> original) {
      if (!OutboundPacketFilterHandler.init(packet)) {
         return original.call(channel, packet);
      }

      try {
         return OutboundPacketFilterHandler.init((Connection)(Object)this, packet, original.call(channel, packet));
      } catch (RuntimeException | Error failure) {
         OutboundPacketFilterHandler.init((Connection)(Object)this, packet);
         throw failure;
      }
   }

   @WrapOperation(
      method = "doSendPacket",
      at = {
            @At(
               value = "INVOKE",
               target = "Lio/netty/channel/Channel;write(Ljava/lang/Object;Lio/netty/channel/ChannelPromise;)Lio/netty/channel/ChannelFuture;"
            ),
            @At(
               value = "INVOKE",
               target = "Lio/netty/channel/Channel;writeAndFlush(Ljava/lang/Object;Lio/netty/channel/ChannelPromise;)Lio/netty/channel/ChannelFuture;"
            )
      }
   )
   private ChannelFuture autism$completeReviewedPayloadPromise(Channel channel, Object packet, ChannelPromise promise, Operation<ChannelFuture> original) {
      if (!OutboundPacketFilterHandler.init(packet)) {
         return original.call(channel, packet, promise);
      }

      try {
         ChannelPromise completion = promise.isVoid() ? channel.newPromise() : promise;
         return OutboundPacketFilterHandler.init((Connection)(Object)this, packet, original.call(channel, packet, completion));
      } catch (RuntimeException | Error failure) {
         OutboundPacketFilterHandler.init((Connection)(Object)this, packet);
         throw failure;
      }
   }

   @Inject(method = "configurePacketHandler", at = @At("HEAD"))
   private void autism$snapshotPrivacyBeforeHandler(ChannelPipeline pipeline, CallbackInfo ci) {
      SocketAddress peerAddress = pipeline != null && pipeline.channel() != null ? pipeline.channel().remoteAddress() : null;
      autism$snapshotPrivacyMode((Connection)(Object)this, peerAddress);
   }

   @Inject(method = "configurePacketHandler", at = @At("TAIL"))
   private void autism$onConfigurePacketHandler(ChannelPipeline pipeline, CallbackInfo ci) {
      this.autism$ensureSpoofPipelineFilter();
   }

   @ModifyVariable(
      method = "send(Lnet/minecraft/network/protocol/Packet;Lio/netty/channel/ChannelFutureListener;)V",
      at = @At("HEAD"),
      argsOnly = true,
      ordinal = 0
   )
   private Packet<?> autism$silentUseItemRotation(Packet<?> packet) {
      if (aD.read_Z()) {
         return packet;
      }

      if (packet instanceof ServerboundUseItemPacket usePacket) {
         float yaw = usePacket.yRot();
         float pitch = usePacket.xRot();
         if (f.load()) {
            yaw = f.init(yaw);
            pitch = f.load(pitch);
         } else {
            RotationMath.Rotation rotation = SilentMovementCoordinator.getHeadRenderRotation(Minecraft.getInstance().player);
            if (rotation != null) {
               yaw = rotation.yaw();
               pitch = rotation.pitch();
            }
         }

         return Float.compare(yaw, usePacket.yRot()) == 0 && Float.compare(pitch, usePacket.xRot()) == 0
            ? packet
            : new ServerboundUseItemPacket(usePacket.hand(), usePacket.sequence(), yaw, pitch);
      } else {
         return packet;
      }
   }

   @Inject(method = "send(Lnet/minecraft/network/protocol/Packet;Lio/netty/channel/ChannelFutureListener;)V", at = @At("HEAD"), cancellable = true)
   private void yang$onSendPacket(Packet<?> packet, ChannelFutureListener listener, CallbackInfo ci) {
         ScaffoldModule.init_V_Packet(packet);
         this.autism$trackSentMessage(packet);
         this.autism$ensureSpoofPipelineFilter();
         if (packet instanceof ServerboundResourcePackPacket resourcePackPacket) {
            if (ResourcePackPushValidator.init(resourcePackPacket)) {
               ci.cancel();
               return;
            }

            ServerPackSelectionRegistry.init(resourcePackPacket.id(), resourcePackPacket.action());
         }

         PacketListener packetListener = ((Connection)(Object)this).getPacketListener();
         ModuleRuntimeManager module = ModuleRuntimeManager.getInstance();
         ModuleRuntimeManager.CapturePlan hooks = module == null
            ? ModuleRuntimeManager.CapturePlan.none()
            : module.getPacketCapturePlan(this.isPlayConnectionActive());
         boolean normalLoggerPath = hooks.normalPath();
         String protocolHint = !hooks.loggerCapture() && !hooks.serverInfoObservation() ? "" : this.autism$protocolHint(packetListener);
         if (!aD.read_Z() && !PkgCls_a.load_Z()) {
            if (PkgCls_a.init_Z_Packet(packet)) {
               ci.cancel();
            } else if (BedSleepDesyncHelper.isClientAwake() && packet instanceof ServerboundPlayerCommandPacket autism$cmd && autism$cmd.getAction() == Action.STOP_SLEEPING) {
               ci.cancel();
            } else {
               if (packet instanceof ServerboundMovePlayerPacket) {
                  Module autism$noFall = autism$noFallModule();
                  if (autism$noFall != null && autism$noFall.isEnabled()) {
                     autism$noFall.onPacketSend(packet);
                  }
               }

               if (!this.autism$isLocalClientPlayConnection() || !PingSpoofScheduler.interceptOutgoing(packet) && !BlinkPacketManager.interceptOutgoing(packet)) {
                  if (this.autism$isLocalClientPlayConnection() && packet instanceof ServerboundPongPacket pong) {
                     autismclient.util.SprintStateTracker.load(pong.getId());
                  }

                  boolean payloadLoggedEarly = false;
                  if (module != null && hooks.loggerCapture()) {
                     payloadLoggedEarly = module.captureCustomPayloadPacket(packet, "C2S", protocolHint);
                  }

                  if (module != null && !normalLoggerPath && hooks.serverInfoObservation()) {
                     module.observeOutgoingServerInfoPacket(packet);
                  }

                  AutismSessionState shared = AutismSessionState.getInstance();
                  if (packet instanceof ServerboundUseItemOnPacket pibp
                     && shared.dispatchBlockCapture(pibp.hitResult().getBlockPos(), pibp.hitResult().getDirection())) {
                     ScaffoldModule.load_V_Packet(packet);
                     ci.cancel();
                  } else if (packet instanceof ServerboundInteractPacket && shared.hasEntityCaptureCallback()) {
                     shared.dispatchEntityCapture(Minecraft.getInstance().crosshairPickEntity);
                     ScaffoldModule.load_V_Packet(packet);
                     ci.cancel();
                  } else {
                     if (packet instanceof ServerboundContainerClosePacket closeForHold) {
                        boolean requiredClose = !AutismLiteVariant.enabled() && AiMenuClose.init(closeForHold);
                        if (!requiredClose && shared.pollDropNextContainerClose()) {
                           ci.cancel();
                           return;
                        }

                        if (!requiredClose && ContainerCloseGuard.isHeldOpen(closeForHold.getContainerId())) {
                           ContainerCloseGuard.init(closeForHold.getContainerId(), closeForHold);
                           ci.cancel();
                           return;
                        }

                        if (!requiredClose && this.autism$shouldKeepXCarryOpen(shared, closeForHold)) {
                           ci.cancel();
                           return;
                        }
                     }

                     if (normalLoggerPath) {
                        if (packet instanceof ServerboundUseItemOnPacket pibp) {
                           shared.setLastInteractedBlockPos(pibp.hitResult().getBlockPos());
                           shared.setLastInteractionTarget(InteractionTargetRef.init(pibp.hitResult(), pibp.hand()));
                        }

                        if (packet instanceof ServerboundInteractPacket entityPacket) {
                           Entity targeted = Minecraft.getInstance().crosshairPickEntity;
                           if (targeted != null && targeted != Minecraft.getInstance().player) {
                              InteractionHand capturedHand = entityPacket.hand();
                              Vec3 capturedHitPos = entityPacket.location();
                              shared.setLastInteractionTarget(capturedHitPos != null ? InteractionTargetRef.init(targeted, capturedHand, capturedHitPos) : InteractionTargetRef.init(targeted, capturedHand));
                           }
                        }

                        if (packet instanceof ServerboundSignUpdatePacket && shared.consumeDropNextSignUpdate()) {
                           ci.cancel();
                        } else if (packet instanceof ServerboundEditBookPacket && shared.consumeDropNextBookEdit()) {
                           ci.cancel();
                        } else {
                           boolean forceBookOrSignPacket = packet instanceof ServerboundSignUpdatePacket && shared.consumeForceNextSignUpdate()
                              || packet instanceof ServerboundEditBookPacket && shared.consumeForceNextBookEdit();
                           if (packet instanceof ServerboundSignUpdatePacket && !shared.isSignEditingAllowed()) {
                              shared.setAllowSignEditing(true);
                              if (!forceBookOrSignPacket) {
                                 ci.cancel();
                                 return;
                              }
                           }

                           if (packet instanceof ServerboundEditBookPacket && !shared.isBookEditingAllowed()) {
                              shared.setAllowBookEditing(true);
                              if (!forceBookOrSignPacket) {
                                 ci.cancel();
                                 return;
                              }
                           }

                           if (shared.isGbreakCapturing()) {
                              if (packet instanceof ServerboundPlayerActionPacket) {
                                 shared.onGbreakPacket(packet);
                              }
                           } else if (packet instanceof ServerboundPlayerActionPacket actionPacket
                              && actionPacket.getAction() == net.minecraft.network.protocol.game.ServerboundPlayerActionPacket.Action.START_DESTROY_BLOCK
                              && al.load_Z_BlockPos_Direction(actionPacket.getPos(), actionPacket.getDirection())) {
                              ci.cancel();
                           } else if (module.onPacketSend(packet, payloadLoggedEarly)) {
                              ScaffoldModule.load_V_Packet(packet);
                              ci.cancel();
                           } else if (!forceBookOrSignPacket) {
                              if (!shared.isBypassingPacketInterception()) {
                                 autismclient.util.macro.PacketGateManager.Inner_StreamUtil gateResult = autismclient.util.macro.PacketGateManager.init(packet, "C2S");
                                 if (gateResult == autismclient.util.macro.PacketGateManager.Inner_StreamUtil.CANCEL) {
                                    ScaffoldModule.load_V_Packet(packet);
                                    ci.cancel();
                                    return;
                                 }

                                 if (gateResult == autismclient.util.macro.PacketGateManager.Inner_StreamUtil.DELAY) {
                                    shared.enqueueDelayedPacket(packet);
                                    ci.cancel();
                                    return;
                                 }
                              }

                              if (AutismLiteVariant.enabled() || autismclient.e.RefQueue.init()) {
                                 boolean anyFeatureActive = shared.isDelayPacketsEnabled() || !shared.isSendPacketsEnabled() || shared.isUseCustomPackets();
                                 if (anyFeatureActive && !shared.isBypassingPacketInterception() && !autism$isTransactionSync(packet)) {
                                    boolean shouldHandle = false;
                                    if (shared.isUseCustomPackets()) {
                                       shouldHandle = shared.getCustomC2sPackets().contains(packet.getClass());
                                    } else {
                                       shouldHandle = this.isGuiPacket(packet);
                                    }

                                    if (shouldHandle) {
                                       if (AUTISM_PACKET_TRACE) {
                                          AutismClientAddon.LOG
                                             .debug(
                                                "[Autism] Packet detected: {} | Send={} Delay={} | Custom={}",
                                                packet.getClass().getSimpleName(),
                                                shared.isSendPacketsEnabled(),
                                                shared.isDelayPacketsEnabled(),
                                                shared.isUseCustomPackets()
                                             );
                                       }

                                       if (!shared.isSendPacketsEnabled()) {
                                          if (AUTISM_PACKET_TRACE) {
                                             AutismClientAddon.LOG.debug("[Autism] CANCELLED packet (send disabled)");
                                          }

                                          ScaffoldModule.load_V_Packet(packet);
                                          ci.cancel();
                                       } else if (shared.isDelayPacketsEnabled()) {
                                          if (AUTISM_PACKET_TRACE) {
                                             AutismClientAddon.LOG.debug("[Autism] QUEUED packet (delay enabled)");
                                          }

                                          ModuleRuntimeManager captureModule = ModuleRuntimeManager.getInstance();
                                          AutismSessionState.ReplayMode captureMode = captureModule != null && captureModule.isCaptureAsExact()
                                             ? AutismSessionState.ReplayMode.EXACT
                                             : AutismSessionState.ReplayMode.REGENERATE;
                                          shared.enqueueDelayedPacket(packet, captureMode);
                                          ci.cancel();
                                       }
                                    }
                                 }
                              }
                           }
                        }
                     }
                  }
               } else {
                  ci.cancel();
               }
            }
         }
   }

   @Inject(method = "send(Lnet/minecraft/network/protocol/Packet;Lio/netty/channel/ChannelFutureListener;)V", at = @At("TAIL"))
   private void yang$afterSendPacket(Packet<?> packet, ChannelFutureListener listener, CallbackInfo ci) {
      if (!aD.read_Z() && this.isPlayConnectionActive()) {
         if (!AutismLiteVariant.enabled() && Minecraft.getInstance().isSameThread()) {
            QueuedCombatAction.init(packet);
         }

         if (this.isAutismActive()) {
            MacroExecutor.onOutgoingPacket(packet);
         }
      }
   }

   @Unique
   private static boolean autism$isTransactionSync(Packet<?> packet) {
      return packet instanceof ServerboundPongPacket
         || packet instanceof ClientboundPingPacket
         || packet instanceof ServerboundKeepAlivePacket
         || packet instanceof ClientboundKeepAlivePacket;
   }

   @Unique
   private void autism$trackSentMessage(Packet<?> packet) {
      if (packet instanceof ServerboundChatPacket chat) {
         AutismSessionState.getInstance().setLastSentMessage(chat.message());
      } else if (packet instanceof ServerboundChatCommandPacket command) {
         AutismSessionState.getInstance().setLastSentMessage("/" + command.command());
      } else if (packet instanceof ServerboundChatCommandSignedPacket signed) {
         AutismSessionState.getInstance().setLastSentMessage("/" + signed.command());
      }
   }

   @Unique
   private boolean isGuiPacket(Packet<?> packet) {
      return packet instanceof ServerboundContainerClickPacket
         || packet instanceof ServerboundContainerButtonClickPacket
         || packet instanceof ServerboundSetCreativeModeSlotPacket
         || packet instanceof ServerboundPlayerActionPacket
         || packet instanceof ServerboundUseItemPacket
         || packet instanceof ServerboundSignUpdatePacket
         || packet instanceof ServerboundEditBookPacket
         || packet instanceof ServerboundChatPacket
         || packet instanceof ServerboundChatCommandPacket
         || packet instanceof ServerboundChatCommandSignedPacket
         || packet instanceof ServerboundCustomClickActionPacket;
   }

   @Inject(method = "channelRead0", at = @At("HEAD"), cancellable = true)
   private void yang$onReceivePacket(ChannelHandlerContext ctx, Packet<?> packet, CallbackInfo ci) {
         PacketListener listener = ((Connection)(Object)this).getPacketListener();
         ModuleRuntimeManager module = ModuleRuntimeManager.getInstance();
         ModuleRuntimeManager.CapturePlan hooks = module == null
            ? ModuleRuntimeManager.CapturePlan.none()
            : module.getPacketCapturePlan(isPlayReceiveListener(listener));
         boolean normalLoggerPath = hooks.normalPath();
         boolean vanillaDialogPacket = packet instanceof ClientboundShowDialogPacket || packet instanceof ClientboundClearDialogPacket;
         boolean customMenuPacket = !vanillaDialogPacket && CustomMenuAdapterRegistry.acceptsInbound(packet);
         String protocolHint = !hooks.loggerCapture() && !hooks.serverInfoObservation() && !customMenuPacket ? "" : this.autism$protocolHint(listener);
         if (aD.read_Z()) {
            this.autism$clearRuntimeConnectionStateOnDisconnect(packet);
         } else {
            if (customMenuPacket) {
               CustomMenuTracker.load(packet, protocolHint);
            }

            if (!isPlayReceiveListener(listener) || !PingSpoofScheduler.interceptIncoming(packet) && !BlinkPacketManager.interceptIncoming(packet)) {
               this.autism$clearRuntimeConnectionStateOnDisconnect(packet);
               boolean payloadLoggedEarly = false;
               if (module != null && hooks.loggerCapture()) {
                  payloadLoggedEarly = module.captureCustomPayloadPacket(packet, "S2C", protocolHint);
               }

               if (module != null && !normalLoggerPath && hooks.serverInfoObservation()) {
                  module.observeIncomingServerInfoPacket(packet);
               }

               if (autism$isIncomingChatPacket(packet)) {
                  MacroExecutor.recordIncomingChatPacket(packet);
                  autismclient.modules.AutoLoginModule.init(packet);
               }

               if (!AutismLiteVariant.enabled()
                  && (packet instanceof ClientboundSetEntityMotionPacket || packet instanceof ClientboundEntityPositionSyncPacket)) {
                  DangerModule.forwardPacket(packet);
               }

               if (normalLoggerPath) {
                  ServerTickPhaseTracker.init(packet);
                  MacroExecutor.onIncomingPacket(packet);
                  if (packet instanceof ClientboundCommandSuggestionsPacket macroSuggestions && CommandProbeIdAllocator.init(macroSuggestions.id())) {
                     ci.cancel();
                  } else {
                     if (packet instanceof ClientboundOpenScreenPacket openScreenPacket) {
                        ContainerCloseGuard.load(openScreenPacket.getContainerId());
                     }

                     if (module.onPacketReceive(packet, payloadLoggedEarly)) {
                        ci.cancel();
                     } else {
                        AutismSessionState shared = AutismSessionState.getInstance();
                        autismclient.util.macro.PacketGateManager.Inner_StreamUtil gateResult = autismclient.util.macro.PacketGateManager.init(packet, "S2C");
                        if (gateResult == autismclient.util.macro.PacketGateManager.Inner_StreamUtil.CANCEL) {
                           ci.cancel();
                        } else if (gateResult == autismclient.util.macro.PacketGateManager.Inner_StreamUtil.DELAY) {
                           shared.enqueueDelayedPacket(packet);
                           ci.cancel();
                        } else if (AutismLiteVariant.enabled() || autismclient.e.CharEncoder.init()) {
                           boolean anyFeatureActive = shared.isDelayPacketsEnabled() || !shared.isSendPacketsEnabled() || shared.isUseCustomPackets();
                           if (anyFeatureActive && !autism$isTransactionSync(packet)) {
                              boolean shouldHandle = false;
                              if (shared.isUseCustomPackets()) {
                                 shouldHandle = shared.getCustomS2cPackets().contains(packet.getClass());
                              }

                              if (shouldHandle) {
                                 if (AUTISM_PACKET_TRACE) {
                                    AutismClientAddon.LOG
                                       .debug(
                                          "[Autism] S2C Packet detected: {} | Send={} Delay={}",
                                          packet.getClass().getSimpleName(),
                                          shared.isSendPacketsEnabled(),
                                          shared.isDelayPacketsEnabled()
                                       );
                                 }

                                 if (!shared.isSendPacketsEnabled()) {
                                    ci.cancel();
                                 } else if (shared.isDelayPacketsEnabled()) {
                                    shared.enqueueDelayedPacket(packet);
                                    ci.cancel();
                                 }
                              }
                           }
                        }
                     }
                  }
               }
            } else {
               ci.cancel();
            }
         }
   }

   @Unique
   private boolean isAutismActive() {
      ModuleRuntimeManager module = ModuleRuntimeManager.getInstance();
      return module != null && module.needsPacketHooks();
   }

   @Unique
   private void autism$ensureSpoofPipelineFilter() {
      if (!this.autism$spoofPipelineInstalled) {
         Channel channel = null;

         try {
            channel = ((AutismClientConnectionAccessor)(Object)this).getChannel();
         } catch (Throwable var3) {
         }

         if (channel != null) {
            try {
               ChannelPipeline pipeline = channel.pipeline();
               if (pipeline == null || pipeline.get("autism_spoof_filter") != null) {
                  this.autism$spoofPipelineInstalled = true;
                  return;
               }

               if (pipeline.get("encoder") != null) {
                  pipeline.addAfter("encoder", "autism_spoof_filter", new OutboundPacketFilterHandler((Connection)(Object)this));
                  this.autism$spoofPipelineInstalled = true;
               }
            } catch (Throwable t) {
               AutismClientAddon.LOG.debug("[Autism] Failed to install client spoof payload filter", t);
            }
         }
      }
   }

   @Inject(method = "send(Lnet/minecraft/network/protocol/Packet;Lio/netty/channel/ChannelFutureListener;Z)V", at = @At("HEAD"), cancellable = true)
   private void autism$onSendPacketWithFlush(Packet<?> packet, ChannelFutureListener listener, boolean flush, CallbackInfo ci) {
         ScaffoldModule.init_V_Packet(packet);
         this.autism$trackSentMessage(packet);
         this.autism$ensureSpoofPipelineFilter();
         if (packet instanceof ServerboundResourcePackPacket resourcePackPacket && ResourcePackPushValidator.init(resourcePackPacket)) {
            ci.cancel();
         } else {
            if (packet instanceof ServerboundResourcePackPacket resourcePackPacket) {
               ServerPackSelectionRegistry.init(resourcePackPacket.id(), resourcePackPacket.action());
            }

            if (!(packet instanceof ServerboundCustomPayloadPacket) && !aD.read_Z()) {
               AutismSessionState shared = AutismSessionState.getInstance();
               if (packet instanceof ServerboundContainerClosePacket closeForHold) {
                  boolean requiredClose = !AutismLiteVariant.enabled() && AiMenuClose.init(closeForHold);
                  if (!requiredClose && shared.pollDropNextContainerClose()) {
                     ci.cancel();
                     return;
                  }

                  if (!requiredClose && ContainerCloseGuard.isHeldOpen(closeForHold.getContainerId())) {
                     ContainerCloseGuard.init(closeForHold.getContainerId(), closeForHold);
                     ci.cancel();
                     return;
                  }

                  if (!requiredClose && this.autism$shouldKeepXCarryOpen(shared, closeForHold)) {
                     ci.cancel();
                  }
               }
            }
         }
   }

   @Unique
   private static boolean autism$isDisconnectPacket(Packet<?> packet) {
      if (packet instanceof ClientboundDisconnectPacket) {
         return true;
      }

      if (packet instanceof ClientboundBundlePacket bundle) {
         for (Packet<?> inner : bundle.subPackets()) {
            if (inner instanceof ClientboundDisconnectPacket) {
               return true;
            }
         }
      }

      return false;
   }

   @Unique
   private void autism$clearRuntimeConnectionStateOnDisconnect(Packet<?> packet) {
      if (autism$isDisconnectPacket(packet)) {
         ContainerCloseGuard.clear();
         autismclient.util.macro.PacketGateManager.clearAll();
         PingSpoofScheduler.write();
         BlinkPacketManager.auditDisconnect();
         AutismSessionState s = AutismSessionState.getInstance();
         s.setXCarrySlotMask(Collections.emptySet(), false);
         s.setXCarryForced(false);
         s.setXCarryActive(false);
      }
   }

   @Unique
   private boolean isPlayConnectionActive() {
      Minecraft client = Minecraft.getInstance();
      return client != null && client.getConnection() != null;
   }

   @Unique
   private static boolean isPlayReceiveListener(PacketListener listener) {
      return listener instanceof ClientGamePacketListener;
   }

   @Unique
   private boolean autism$isLocalClientPlayConnection() {
      return ((Connection)(Object)this).getPacketListener() instanceof ClientGamePacketListener;
   }

   @Unique
   private static boolean autism$isIncomingChatPacket(Packet<?> packet) {
      return packet instanceof ClientboundSystemChatPacket || packet instanceof ClientboundDisguisedChatPacket || packet instanceof ClientboundPlayerChatPacket;
   }

   @Unique
   private String autism$protocolHint(PacketListener listener) {
      if (listener == null) {
         return "";
      }

      if (listener == this.autism$protocolHintListener) {
         return this.autism$protocolHintCache;
      }

      String hint;
      if (listener instanceof ClientGamePacketListener) {
         hint = "play";
      } else {
         String name = listener.getClass().getName();
         hint = name.toLowerCase(Locale.ROOT).contains("configuration") ? "configuration" : "";
      }

      this.autism$protocolHintCache = hint;
      this.autism$protocolHintListener = listener;
      return hint;
   }

   @Unique
   private boolean autism$shouldKeepXCarryOpen(AutismSessionState shared, ServerboundContainerClosePacket packet) {
      Minecraft client = Minecraft.getInstance();
      if (client != null && client.player != null) {
         ModuleRuntimeManager module = ModuleRuntimeManager.getInstance();
         boolean allowPassiveXCarry = module != null && module.isXCarryEnabled();
         if (!allowPassiveXCarry && !shared.isXCarryForced()) {
            return false;
         }

         if (packet.getContainerId() != client.player.inventoryMenu.containerId) {
            return false;
         }

         Set<Integer> mask;
         boolean carryCursor;
         if (shared.isXCarryForced()) {
            mask = shared.getXCarrySlotMask();
            carryCursor = shared.isXCarryCarryCursor();
         } else {
            mask = module == null ? null : module.getXCarrySlotSet();
            carryCursor = module == null || module.isXCarryCarryCursor();
         }

         boolean hasItems = XCarryAction.hasCarriedItemsIn(client.player.inventoryMenu, carryCursor, mask);
         shared.setXCarryActive(hasItems);
         return true;
      } else {
         return false;
      }
   }
}

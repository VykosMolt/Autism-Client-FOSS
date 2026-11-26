package autismclient.mixin;

import autismclient.AutismClientAddon;
import autismclient.ai.aq;
import autismclient.api.custommenu.CustomMenuAdapterRegistry;
import autismclient.g.ConnectionPrivacyRegistry;
import autismclient.g.ServerPackSelectionRegistry;
import autismclient.g.OutboundPacketFilterHandler;
import autismclient.g.ResourcePackPushValidator;
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
import autismclient.util.D;
import autismclient.util.E;
import autismclient.util.EventBus;
import autismclient.util.PacketCaptureState;
import autismclient.util.ce;
import autismclient.util.ch;
import autismclient.util.ci;
import autismclient.util.ck;
import autismclient.util.cn;
import autismclient.util.BedSleepDesyncHelper;
import autismclient.util.y;
import autismclient.util.b.CustomMenuTracker;
import autismclient.util.e.LeafDupePacketTracker;
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
      ConnectionPrivacyRegistry.Inner_CachePool mode = AutismConfig.getGlobal().spoofClientVanilla ? ConnectionPrivacyRegistry.Inner_CachePool.VANILLA : ConnectionPrivacyRegistry.Inner_CachePool.MODDED;
      ConnectionPrivacyRegistry.init(connection, mode, false, peerAddress);
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
         ch.init_V();
         ScaffoldModule.update_V_10();
         if (!AutismLiteVariant.enabled()) {
            LeafDupeModule.load_V_21();
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
         if (!AutismLiteVariant.enabled() && !LeafDupeModule.init_Z_Connection_Packet((Connection)(Object)this, packet)) {
            ci.cancel();
            return;
         }

         ch.init(packet);
         if (!AutismLiteVariant.enabled()) {
            BacktrackModule.init_V_Connection_Packet((Connection)(Object)this, packet);
         }

         if (!AutismLiteVariant.enabled()) {
            LeafDupePacketTracker.init((Connection)(Object)this, packet);
         }

         ScaffoldModule.read_V_Packet(packet);
         if (!AutismLiteVariant.enabled()) {
            aq.init(packet);
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
            ce.Inner_DataUtil rotation = cn.load(Minecraft.getInstance().player);
            if (rotation != null) {
               yaw = rotation.init();
               pitch = rotation.load();
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
         ModuleRuntimeManager module = ModuleRuntimeManager.init_PoolEntry();
         ModuleRuntimeManager.Inner_DataUtil hooks = module == null
            ? ModuleRuntimeManager.Inner_DataUtil.init_PoolEntry_DataUtil()
            : module.init_PoolEntry_DataUtil_Z(this.isPlayConnectionActive());
         boolean normalLoggerPath = hooks.init_Z();
         String protocolHint = !hooks.read() && !hooks.write() ? "" : this.autism$protocolHint(packetListener);
         if (!aD.read_Z() && !PkgCls_a.load_Z()) {
            if (PkgCls_a.init_Z_Packet(packet)) {
               ci.cancel();
            } else if (BedSleepDesyncHelper.init_Z() && packet instanceof ServerboundPlayerCommandPacket autism$cmd && autism$cmd.getAction() == Action.STOP_SLEEPING) {
               ci.cancel();
            } else {
               if (packet instanceof ServerboundMovePlayerPacket) {
                  Module autism$noFall = autism$noFallModule();
                  if (autism$noFall != null && autism$noFall.isEnabled()) {
                     autism$noFall.onPacketSend(packet);
                  }
               }

               if (!this.autism$isLocalClientPlayConnection() || !PingSpoofScheduler.load_Z(packet) && !BlinkPacketManager.load(packet)) {
                  if (this.autism$isLocalClientPlayConnection() && packet instanceof ServerboundPongPacket pong) {
                     autismclient.util.ci.load(pong.getId());
                  }

                  boolean payloadLoggedEarly = false;
                  if (module != null && hooks.read()) {
                     payloadLoggedEarly = module.init_Z(packet, "C2S", protocolHint);
                  }

                  if (module != null && !normalLoggerPath && hooks.write()) {
                     module.init_V_Packet(packet);
                  }

                  ck shared = ck.init_ck();
                  if (packet instanceof ServerboundUseItemOnPacket pibp
                     && shared.init_Z_BlockPos_Direction(pibp.hitResult().getBlockPos(), pibp.hitResult().getDirection())) {
                     ScaffoldModule.load_V_Packet(packet);
                     ci.cancel();
                  } else if (packet instanceof ServerboundInteractPacket && shared.frame()) {
                     shared.init_Z_Entity(Minecraft.getInstance().crosshairPickEntity);
                     ScaffoldModule.load_V_Packet(packet);
                     ci.cancel();
                  } else {
                     if (packet instanceof ServerboundContainerClosePacket closeForHold) {
                        boolean requiredClose = !AutismLiteVariant.enabled() && aq.init(closeForHold);
                        if (!requiredClose && shared.queue_Z()) {
                           ci.cancel();
                           return;
                        }

                        if (!requiredClose && D.init_Z(closeForHold.getContainerId())) {
                           D.init(closeForHold.getContainerId(), closeForHold);
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
                           shared.init_V_BlockPos(pibp.hitResult().getBlockPos());
                           shared.init_V_E(E.init(pibp.hitResult(), pibp.hand()));
                        }

                        if (packet instanceof ServerboundInteractPacket entityPacket) {
                           Entity targeted = Minecraft.getInstance().crosshairPickEntity;
                           if (targeted != null && targeted != Minecraft.getInstance().player) {
                              InteractionHand capturedHand = entityPacket.hand();
                              Vec3 capturedHitPos = entityPacket.location();
                              shared.init_V_E(capturedHitPos != null ? E.init(targeted, capturedHand, capturedHitPos) : E.init(targeted, capturedHand));
                           }
                        }

                        if (packet instanceof ServerboundSignUpdatePacket && shared.buffer_Z()) {
                           ci.cancel();
                        } else if (packet instanceof ServerboundEditBookPacket && shared.entry()) {
                           ci.cancel();
                        } else {
                           boolean forceBookOrSignPacket = packet instanceof ServerboundSignUpdatePacket && shared.node()
                              || packet instanceof ServerboundEditBookPacket && shared.task();
                           if (packet instanceof ServerboundSignUpdatePacket && !shared.set_Z()) {
                              shared.open(true);
                              if (!forceBookOrSignPacket) {
                                 ci.cancel();
                                 return;
                              }
                           }

                           if (packet instanceof ServerboundEditBookPacket && !shared.put_Z()) {
                              shared.poll(true);
                              if (!forceBookOrSignPacket) {
                                 ci.cancel();
                                 return;
                              }
                           }

                           if (shared.cache_Z()) {
                              if (packet instanceof ServerboundPlayerActionPacket) {
                                 shared.read_V_Packet(packet);
                              }
                           } else if (packet instanceof ServerboundPlayerActionPacket actionPacket
                              && actionPacket.getAction() == net.minecraft.network.protocol.game.ServerboundPlayerActionPacket.Action.START_DESTROY_BLOCK
                              && al.load_Z_BlockPos_Direction(actionPacket.getPos(), actionPacket.getDirection())) {
                              ci.cancel();
                           } else if (module.init(packet, payloadLoggedEarly)) {
                              ScaffoldModule.load_V_Packet(packet);
                              ci.cancel();
                           } else if (!forceBookOrSignPacket) {
                              if (!shared.get_Z()) {
                                 autismclient.util.macro.PacketGateManager.Inner_StreamUtil gateResult = autismclient.util.macro.PacketGateManager.init(packet, "C2S");
                                 if (gateResult == autismclient.util.macro.PacketGateManager.Inner_StreamUtil.CANCEL) {
                                    ScaffoldModule.load_V_Packet(packet);
                                    ci.cancel();
                                    return;
                                 }

                                 if (gateResult == autismclient.util.macro.PacketGateManager.Inner_StreamUtil.DELAY) {
                                    shared.init_V_Packet(packet);
                                    ci.cancel();
                                    return;
                                 }
                              }

                              if (AutismLiteVariant.enabled() || autismclient.e.RefQueue.init()) {
                                 boolean anyFeatureActive = shared.write_Z() || !shared.read_Z() || shared.reset_Z();
                                 if (anyFeatureActive && !shared.get_Z() && !autism$isTransactionSync(packet)) {
                                    boolean shouldHandle = false;
                                    if (shared.reset_Z()) {
                                       shouldHandle = shared.load_Set().contains(packet.getClass());
                                    } else {
                                       shouldHandle = this.isGuiPacket(packet);
                                    }

                                    if (shouldHandle) {
                                       if (AUTISM_PACKET_TRACE) {
                                          AutismClientAddon.LOG
                                             .debug(
                                                "[Autism] Packet detected: {} | Send={} Delay={} | Custom={}",
                                                packet.getClass().getSimpleName(),
                                                shared.read_Z(),
                                                shared.write_Z(),
                                                shared.reset_Z()
                                             );
                                       }

                                       if (!shared.read_Z()) {
                                          if (AUTISM_PACKET_TRACE) {
                                             AutismClientAddon.LOG.debug("[Autism] CANCELLED packet (send disabled)");
                                          }

                                          ScaffoldModule.load_V_Packet(packet);
                                          ci.cancel();
                                       } else if (shared.write_Z()) {
                                          if (AUTISM_PACKET_TRACE) {
                                             AutismClientAddon.LOG.debug("[Autism] QUEUED packet (delay enabled)");
                                          }

                                          ModuleRuntimeManager captureModule = ModuleRuntimeManager.init_PoolEntry();
                                          ck.Inner_BufferHelper captureMode = captureModule != null && captureModule.next_Z()
                                             ? ck.Inner_BufferHelper.EXACT
                                             : ck.Inner_BufferHelper.REGENERATE;
                                          shared.init_V_Packet_ck_BufferHelper(packet, captureMode);
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
            EventBus.init(packet);
         }

         if (this.isAutismActive()) {
            MacroExecutor.init_V_Packet(packet);
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
         ck.init_ck().read_V_Str(chat.message());
      } else if (packet instanceof ServerboundChatCommandPacket command) {
         ck.init_ck().read_V_Str("/" + command.command());
      } else if (packet instanceof ServerboundChatCommandSignedPacket signed) {
         ck.init_ck().read_V_Str("/" + signed.command());
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
         ModuleRuntimeManager module = ModuleRuntimeManager.init_PoolEntry();
         ModuleRuntimeManager.Inner_DataUtil hooks = module == null
            ? ModuleRuntimeManager.Inner_DataUtil.init_PoolEntry_DataUtil()
            : module.init_PoolEntry_DataUtil_Z(isPlayReceiveListener(listener));
         boolean normalLoggerPath = hooks.init_Z();
         boolean vanillaDialogPacket = packet instanceof ClientboundShowDialogPacket || packet instanceof ClientboundClearDialogPacket;
         boolean customMenuPacket = !vanillaDialogPacket && CustomMenuAdapterRegistry.acceptsInbound(packet);
         String protocolHint = !hooks.read() && !hooks.write() && !customMenuPacket ? "" : this.autism$protocolHint(listener);
         if (aD.read_Z()) {
            this.autism$clearRuntimeConnectionStateOnDisconnect(packet);
         } else {
            if (customMenuPacket) {
               CustomMenuTracker.load(packet, protocolHint);
            }

            if (!isPlayReceiveListener(listener) || !PingSpoofScheduler.init_Z(packet) && !BlinkPacketManager.init_Z(packet)) {
               this.autism$clearRuntimeConnectionStateOnDisconnect(packet);
               boolean payloadLoggedEarly = false;
               if (module != null && hooks.read()) {
                  payloadLoggedEarly = module.init_Z(packet, "S2C", protocolHint);
               }

               if (module != null && !normalLoggerPath && hooks.write()) {
                  module.load_V(packet);
               }

               if (autism$isIncomingChatPacket(packet)) {
                  MacroExecutor.read(packet);
                  autismclient.modules.AutoLoginModule.init(packet);
               }

               if (!AutismLiteVariant.enabled()
                  && (packet instanceof ClientboundSetEntityMotionPacket || packet instanceof ClientboundEntityPositionSyncPacket)) {
                  DangerModule.init_V_Packet_3(packet);
               }

               if (normalLoggerPath) {
                  ServerTickPhaseTracker.init(packet);
                  MacroExecutor.load_V_Packet(packet);
                  if (packet instanceof ClientboundCommandSuggestionsPacket macroSuggestions && y.init(macroSuggestions.id())) {
                     ci.cancel();
                  } else {
                     if (packet instanceof ClientboundOpenScreenPacket openScreenPacket) {
                        D.load(openScreenPacket.getContainerId());
                     }

                     if (module.load(packet, payloadLoggedEarly)) {
                        ci.cancel();
                     } else {
                        ck shared = ck.init_ck();
                        autismclient.util.macro.PacketGateManager.Inner_StreamUtil gateResult = autismclient.util.macro.PacketGateManager.init(packet, "S2C");
                        if (gateResult == autismclient.util.macro.PacketGateManager.Inner_StreamUtil.CANCEL) {
                           ci.cancel();
                        } else if (gateResult == autismclient.util.macro.PacketGateManager.Inner_StreamUtil.DELAY) {
                           shared.init_V_Packet(packet);
                           ci.cancel();
                        } else if (AutismLiteVariant.enabled() || autismclient.e.CharEncoder.init()) {
                           boolean anyFeatureActive = shared.write_Z() || !shared.read_Z() || shared.reset_Z();
                           if (anyFeatureActive && !autism$isTransactionSync(packet)) {
                              boolean shouldHandle = false;
                              if (shared.reset_Z()) {
                                 shouldHandle = shared.read_Set().contains(packet.getClass());
                              }

                              if (shouldHandle) {
                                 if (AUTISM_PACKET_TRACE) {
                                    AutismClientAddon.LOG
                                       .debug(
                                          "[Autism] S2C Packet detected: {} | Send={} Delay={}",
                                          packet.getClass().getSimpleName(),
                                          shared.read_Z(),
                                          shared.write_Z()
                                       );
                                 }

                                 if (!shared.read_Z()) {
                                    ci.cancel();
                                 } else if (shared.write_Z()) {
                                    shared.init_V_Packet(packet);
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
      ModuleRuntimeManager module = ModuleRuntimeManager.init_PoolEntry();
      return module != null && module.read_Z();
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
               ck shared = ck.init_ck();
               if (packet instanceof ServerboundContainerClosePacket closeForHold) {
                  boolean requiredClose = !AutismLiteVariant.enabled() && aq.init(closeForHold);
                  if (!requiredClose && shared.queue_Z()) {
                     ci.cancel();
                     return;
                  }

                  if (!requiredClose && D.init_Z(closeForHold.getContainerId())) {
                     D.init(closeForHold.getContainerId(), closeForHold);
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
         D.init_V();
         autismclient.util.macro.PacketGateManager.load_V();
         PingSpoofScheduler.write();
         BlinkPacketManager.auditDisconnect();
         ck s = ck.init_ck();
         s.init_V_Set_Z(Collections.emptySet(), false);
         s.sync(false);
         s.update(false);
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
   private boolean autism$shouldKeepXCarryOpen(ck shared, ServerboundContainerClosePacket packet) {
      Minecraft client = Minecraft.getInstance();
      if (client != null && client.player != null) {
         ModuleRuntimeManager module = ModuleRuntimeManager.init_PoolEntry();
         boolean allowPassiveXCarry = module != null && module.get_Z();
         if (!allowPassiveXCarry && !shared.sync_Z()) {
            return false;
         }

         if (packet.getContainerId() != client.player.inventoryMenu.containerId) {
            return false;
         }

         Set<Integer> mask;
         boolean carryCursor;
         if (shared.sync_Z()) {
            mask = shared.init_Set();
            carryCursor = shared.flush_Z();
         } else {
            mask = module == null ? null : module.init_Set();
            carryCursor = module == null || module.cache();
         }

         boolean hasItems = XCarryAction.init_Z_InventoryMenu_3(client.player.inventoryMenu, carryCursor, mask);
         shared.update(hasItems);
         return true;
      } else {
         return false;
      }
   }
}

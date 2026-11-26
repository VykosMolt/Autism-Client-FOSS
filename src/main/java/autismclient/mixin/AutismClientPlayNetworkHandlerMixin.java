package autismclient.mixin;

import autismclient.AutismClientAddon;
import autismclient.ai.AiContainerTransactions;
import autismclient.ai.AiChatLedger;
import autismclient.ai.AiServerEvidence;
import autismclient.ai.AiService;
import autismclient.ai.engine.AiGridRevisions;
import autismclient.ai.engine.AiScaffold;
import autismclient.modules.AnchorAuraModule;
import autismclient.modules.AntiBotTracker;
import autismclient.modules.EndPortalModule;
import autismclient.modules.ModuleRuntimeManager;
import autismclient.modules.InventoryTweaksModule;
import autismclient.modules.AntiKnockbackModule;
import autismclient.modules.AntiVanishModule;
import autismclient.modules.KBDisplacementModule;
import autismclient.modules.aD;
import autismclient.modules.ScaffoldModule;
import autismclient.modules.LeafDupeModule;
import autismclient.modules.EspChunkWorker;
import autismclient.modules.al;
import autismclient.modules.WTappingModule;
import autismclient.modules.ClutchModule;
import autismclient.util.AutismClientMessaging;
import autismclient.util.AutismConfig;
import autismclient.util.AutismLiteVariant;
import autismclient.util.AutismServerInfoOverlay;
import autismclient.util.AutoCraftingEngine;
import autismclient.util.MapTooltipPreview;
import autismclient.util.RotationMath;
import autismclient.util.cf;
import autismclient.util.SprintStateTracker;
import autismclient.util.AutismSessionState;
import autismclient.util.SodiumRegionResourceProbe;
import autismclient.util.leafdupe.LeafDupePacketTracker;
import autismclient.util.g.PkgCls_a;
import autismclient.util.villager.VillagerTradeSession;
import autismclient.util.macro.MacroWaitRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket;
import net.minecraft.network.protocol.game.ClientboundAnimatePacket;
import net.minecraft.network.protocol.game.ClientboundBlockChangedAckPacket;
import net.minecraft.network.protocol.game.ClientboundBlockDestructionPacket;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.network.protocol.game.ClientboundBlockEventPacket;
import net.minecraft.network.protocol.game.ClientboundBlockUpdatePacket;
import net.minecraft.network.protocol.game.ClientboundCommandSuggestionsPacket;
import net.minecraft.network.protocol.game.ClientboundCommandsPacket;
import net.minecraft.network.protocol.game.ClientboundContainerSetContentPacket;
import net.minecraft.network.protocol.game.ClientboundContainerSetSlotPacket;
import net.minecraft.network.protocol.game.ClientboundDamageEventPacket;
import net.minecraft.network.protocol.game.ClientboundDisguisedChatPacket;
import net.minecraft.network.protocol.game.ClientboundEntityPositionSyncPacket;
import net.minecraft.network.protocol.game.ClientboundExplodePacket;
import net.minecraft.network.protocol.game.ClientboundForgetLevelChunkPacket;
import net.minecraft.network.protocol.game.ClientboundLevelChunkWithLightPacket;
import net.minecraft.network.protocol.game.ClientboundLevelEventPacket;
import net.minecraft.network.protocol.game.ClientboundLoginPacket;
import net.minecraft.network.protocol.game.ClientboundMapItemDataPacket;
import net.minecraft.network.protocol.game.ClientboundMoveEntityPacket;
import net.minecraft.network.protocol.game.ClientboundMoveVehiclePacket;
import net.minecraft.network.protocol.game.ClientboundPlayerChatPacket;
import net.minecraft.network.protocol.game.ClientboundPlayerCombatKillPacket;
import net.minecraft.network.protocol.game.ClientboundPlayerInfoRemovePacket;
import net.minecraft.network.protocol.game.ClientboundPlayerInfoUpdatePacket;
import net.minecraft.network.protocol.game.ClientboundPlayerPositionPacket;
import net.minecraft.network.protocol.game.ClientboundPlayerRotationPacket;
import net.minecraft.network.protocol.game.ClientboundRemoveEntitiesPacket;
import net.minecraft.network.protocol.game.ClientboundRespawnPacket;
import net.minecraft.network.protocol.game.ClientboundSectionBlocksUpdatePacket;
import net.minecraft.network.protocol.game.ClientboundSetCursorItemPacket;
import net.minecraft.network.protocol.game.ClientboundSetEntityDataPacket;
import net.minecraft.network.protocol.game.ClientboundSetEntityMotionPacket;
import net.minecraft.network.protocol.game.ClientboundSetHealthPacket;
import net.minecraft.network.protocol.game.ClientboundSetHeldSlotPacket;
import net.minecraft.network.protocol.game.ClientboundSetPlayerInventoryPacket;
import net.minecraft.network.protocol.game.ClientboundSetTimePacket;
import net.minecraft.network.protocol.game.ClientboundSoundEntityPacket;
import net.minecraft.network.protocol.game.ClientboundSoundPacket;
import net.minecraft.network.protocol.game.ClientboundSystemChatPacket;
import net.minecraft.network.protocol.game.ClientboundUpdateAttributesPacket;
import net.minecraft.network.syncher.SynchedEntityData.DataValue;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPacketListener.class)
public abstract class AutismClientPlayNetworkHandlerMixin {
   @Unique
   private boolean autism$viewCaptured;
   @Unique
   private float autism$viewYaw;
   @Unique
   private float autism$viewPitch;
   @Unique
   private float autism$viewYawO;
   @Unique
   private float autism$viewPitchO;
   @Unique
   private Vec3 autism$clutchCorrectionFrom;
   @Unique
   private long autism$pacedTeleportCorrectionToken;
   @Unique
   private long autism$pacedTeleportVehicleCorrectionToken;

   @Inject(method = "handleSetHeldSlot", at = @At("RETURN"))
   private void autism$leafDupeServerSlot(ClientboundSetHeldSlotPacket packet, CallbackInfo ci) {
      if (!AutismLiteVariant.enabled()) {
         LeafDupePacketTracker.init(packet.slot());
      }
   }

   @Inject(method = "handleLogin", at = @At("RETURN"))
   private void autism$leafDupeSessionStarted(ClientboundLoginPacket packet, CallbackInfo ci) {
      if (!AutismLiteVariant.enabled()) {
         LeafDupeModule.init_V_28();
      }
   }

   @Inject(method = "handleMapItemData", at = @At("RETURN"))
   private void autism$mapPreviewUpdated(ClientboundMapItemDataPacket packet, CallbackInfo ci) {
      MapTooltipPreview.init_V(packet.mapId());
   }

   @Inject(method = "handleAnimate", at = @At("RETURN"))
   private void autism$combatAnimation(ClientboundAnimatePacket packet, CallbackInfo ci) {
      if (!AutismLiteVariant.enabled()) {
         AntiVanishModule.init_V_ClientPacketListen_Packet((ClientPacketListener)(Object)this, packet);
      }
   }

   @Inject(
      method = "handleSystemChat",
      at = @At(value = "INVOKE", target = "Lnet/minecraft/client/multiplayer/chat/ChatListener;handleSystemMessage(Lnet/minecraft/network/chat/Component;Z)V"),
      cancellable = true
   )
   private void autism$vanishRoster(ClientboundSystemChatPacket packet, CallbackInfo ci) {
      if (!AutismLiteVariant.enabled() && AntiVanishModule.init_Z_ClientPacketListen_ClientboundSystemC((ClientPacketListener)(Object)this, packet)) {
         ci.cancel();
      }
   }

   @Inject(method = "handleSystemChat", at = @At("RETURN"))
   private void autism$aiSystemMessage(ClientboundSystemChatPacket packet, CallbackInfo ci) {
      if (!AutismLiteVariant.enabled()) {
         AntiVanishModule.init_V_ClientPacketListen_Packet((ClientPacketListener)(Object)this, packet);
      }

      if (!AutismLiteVariant.enabled() && AiService.load_cc() != null) {
         AiServerEvidence.init_V_Str(packet.content().getString());
         AiChatLedger.init(packet.content(), "SERVER");
      }
   }

   @Inject(method = "handlePlayerChat", at = @At("RETURN"))
   private void autism$aiPlayerChat(ClientboundPlayerChatPacket packet, CallbackInfo ci) {
      if (!AutismLiteVariant.enabled() && AiService.load_cc() != null) {
         Component content = packet.unsignedContent().orElse(Component.literal(packet.body().content()));
         AiChatLedger.init(content, packet.sender(), packet.chatType().name().getString());
      }
   }

   @Inject(method = "handleDisguisedChat", at = @At("RETURN"))
   private void autism$aiDisguisedChat(ClientboundDisguisedChatPacket packet, CallbackInfo ci) {
      if (!AutismLiteVariant.enabled() && AiService.load_cc() != null) {
         AiChatLedger.load(packet.message(), packet.chatType().name().getString());
      }
   }

   @Inject(method = "handleLogin", at = @At("RETURN"))
   private void autism$espLoginApplied(ClientboundLoginPacket packet, CallbackInfo ci) {
      EspChunkWorker.init_V_Packet(packet);
   }

   @Inject(method = "handleRespawn", at = @At("RETURN"))
   private void autism$espRespawnApplied(ClientboundRespawnPacket packet, CallbackInfo ci) {
      EspChunkWorker.init_V_Packet(packet);
   }

   @Inject(method = "handleBlockEntityData", at = @At("RETURN"), require = 0)
   private void autism$espBlockEntityDataApplied(ClientboundBlockEntityDataPacket packet, CallbackInfo ci) {
      EspChunkWorker.init_V_Packet(packet);
   }

   @Inject(
      method = "handleBlockUpdate",
      at = @At(
         value = "INVOKE",
         target = "Lnet/minecraft/client/multiplayer/ClientLevel;setServerVerifiedBlockState(Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;I)V"
      )
   )
   private void autism$vanishBlockBefore(ClientboundBlockUpdatePacket packet, CallbackInfo ci) {
      if (!AutismLiteVariant.enabled()) {
         AntiVanishModule.init_V_ClientPacketListen((ClientPacketListener)(Object)this, packet.getPos(), packet.getBlockState());
      }
   }

   @Inject(
      method = "handleChunkBlocksUpdate",
      at = @At(
         value = "INVOKE",
         target = "Lnet/minecraft/network/protocol/game/ClientboundSectionBlocksUpdatePacket;runUpdates(Ljava/util/function/BiConsumer;)V"
      )
   )
   private void autism$vanishSectionBefore(ClientboundSectionBlocksUpdatePacket packet, CallbackInfo ci) {
      if (!AutismLiteVariant.enabled()) {
         AntiVanishModule.init_V_ClientPacketListen_ClientboundSection((ClientPacketListener)(Object)this, packet);
      }
   }

   @Inject(method = "handleBlockEvent", at = @At("RETURN"))
   private void autism$vanishMechanism(ClientboundBlockEventPacket packet, CallbackInfo ci) {
      if (!AutismLiteVariant.enabled()) {
         AntiVanishModule.init_V_ClientPacketListen_Packet((ClientPacketListener)(Object)this, packet);
      }
   }

   @Inject(method = "handleLevelEvent", at = @At("RETURN"))
   private void autism$vanishWorldEffect(ClientboundLevelEventPacket packet, CallbackInfo ci) {
      if (!AutismLiteVariant.enabled()) {
         AntiVanishModule.init_V_ClientPacketListen_Packet((ClientPacketListener)(Object)this, packet);
      }
   }

   @Inject(method = "handleExplosion", at = @At("RETURN"))
   private void autism$vanishExplosion(ClientboundExplodePacket packet, CallbackInfo ci) {
      if (!AutismLiteVariant.enabled()) {
         AntiVanishModule.init_V_ClientPacketListen_Packet((ClientPacketListener)(Object)this, packet);
      }
   }

   @Inject(method = "handleBlockUpdate", at = @At("HEAD"))
   private void autism$aiBlockApplying(ClientboundBlockUpdatePacket packet, CallbackInfo ci) {
      if (!AutismLiteVariant.enabled()) {
         AiServerEvidence.init_V_BlockPos(packet.getPos());
      }
   }

   @Inject(method = "handleChunkBlocksUpdate", at = @At("HEAD"))
   private void autism$aiSectionApplying(ClientboundSectionBlocksUpdatePacket packet, CallbackInfo ci) {
      if (!AutismLiteVariant.enabled() && Minecraft.getInstance().isSameThread()) {
         packet.runUpdates((pos, state) -> AiServerEvidence.init_V_BlockPos(pos));
      }
   }

   @Inject(method = "handleBlockUpdate", at = @At("RETURN"))
   private void autism$aiBlockApplied(ClientboundBlockUpdatePacket packet, CallbackInfo ci) {
      EspChunkWorker.init_V_Packet(packet);
      if (!AutismLiteVariant.enabled()) {
         ScaffoldModule.init_V_BlockPos_BlockState_2(packet.getPos(), packet.getBlockState());
         AnchorAuraModule.init_V_BlockPos_BlockState(packet.getPos(), packet.getBlockState());
         AiServerEvidence.init(packet.getPos(), packet.getBlockState());
         AiGridRevisions.init(packet.getPos());
         AiScaffold.init(packet.getPos(), packet.getBlockState());
      }
   }

   @Inject(method = "handleChunkBlocksUpdate", at = @At("RETURN"))
   private void autism$aiGridSectionApplied(ClientboundSectionBlocksUpdatePacket packet, CallbackInfo ci) {
      EspChunkWorker.init_V_Packet(packet);
      if (!AutismLiteVariant.enabled()) {
         boolean[] bumped = new boolean[]{false};
         packet.runUpdates((pos, state) -> {
            ScaffoldModule.init_V_BlockPos_BlockState_2(pos, state);
            AnchorAuraModule.init_V_BlockPos_BlockState(pos, state);
            AiServerEvidence.init(pos, state);
            AiScaffold.init(pos, state);
            if (!bumped[0]) {
               bumped[0] = true;
               AiGridRevisions.init(pos);
            }
         });
      }
   }

   @Inject(method = "handleForgetLevelChunk", at = @At("RETURN"))
   private void autism$aiGridChunkUnloaded(ClientboundForgetLevelChunkPacket packet, CallbackInfo ci) {
      EspChunkWorker.init_V_Packet(packet);
      if (!AutismLiteVariant.enabled()) {
         AntiVanishModule.init_V_ClientPacketListen_Packet((ClientPacketListener)(Object)this, packet);
      }

      if (!AutismLiteVariant.enabled()) {
         AiGridRevisions.init_V(packet.pos().x(), packet.pos().z());
         AiServerEvidence.init_V(packet.pos().x(), packet.pos().z());
      }
   }

   @Inject(method = "handleLevelChunkWithLight", at = @At("RETURN"))
   private void autism$aiFullChunkApplied(ClientboundLevelChunkWithLightPacket packet, CallbackInfo ci) {
      EspChunkWorker.init_V_Packet(packet);
      if (!AutismLiteVariant.enabled()) {
         AntiVanishModule.init_V_ClientPacketListen_Packet((ClientPacketListener)(Object)this, packet);
      }

      if (!AutismLiteVariant.enabled()) {
         AiServerEvidence.load(packet.x(), packet.z());
         AiGridRevisions.init_V(packet.x(), packet.z());
      }
   }

   @Inject(method = "handleContainerSetSlot", at = @At("RETURN"))
   private void autism$aiSlotApplied(ClientboundContainerSetSlotPacket packet, CallbackInfo ci) {
      if (!AutismLiteVariant.enabled()) {
         AiServerEvidence.init_V();
         AiContainerTransactions.init(packet.getContainerId(), packet.getSlot(), packet.getItem());
      }
   }

   @Inject(method = "handleSetPlayerInventory", at = @At("RETURN"))
   private void autism$aiInventoryApplied(ClientboundSetPlayerInventoryPacket packet, CallbackInfo ci) {
      if (!AutismLiteVariant.enabled()) {
         AiServerEvidence.init_V();
         AiContainerTransactions.init(packet.slot(), packet.contents());
      }
   }

   @Inject(method = "handleContainerContent", at = @At("RETURN"))
   private void autism$aiContentsApplied(ClientboundContainerSetContentPacket packet, CallbackInfo ci) {
      VillagerTradeSession.init_V_ClientboundContain(packet);
      AutoCraftingEngine.init_V(packet.containerId());
      if (!AutismLiteVariant.enabled()) {
         AiServerEvidence.init(packet.containerId(), packet.items());
         AiContainerTransactions.init(packet.containerId(), packet.items(), packet.carriedItem());
      }
   }

   @Inject(method = "handleSetEntityData", at = @At("RETURN"))
   private void autism$serverSprintFlag(ClientboundSetEntityDataPacket packet, CallbackInfo ci) {
      LocalPlayer player = Minecraft.getInstance().player;
      if (player != null && packet.id() == player.getId()) {
         for (DataValue<?> value : packet.packedItems()) {
            if (value.id() == 0 && value.value() instanceof Byte flags) {
               autismclient.util.SprintStateTracker.load((flags & 8) != 0);
            }
         }
      }
   }

   @Inject(method = "handleSetEntityMotion", at = @At("HEAD"))
   private void autism$clutchMotionBefore(ClientboundSetEntityMotionPacket packet, CallbackInfo ci) {
      if (!AutismLiteVariant.enabled() && Minecraft.getInstance().isSameThread()) {
         ClutchModule.init_V_I_11(packet.id());
      }
   }

   @Inject(method = "handleSetEntityMotion", at = @At("RETURN"))
   private void autism$aiEntityMotion(ClientboundSetEntityMotionPacket packet, CallbackInfo ci) {
      if (!AutismLiteVariant.enabled()) {
         ClutchModule.load_V_I_3(packet.id());
         AntiKnockbackModule.init_V_I_7(packet.id());
         Minecraft mc = Minecraft.getInstance();
         Entity entity = mc.level == null ? null : mc.level.getEntity(packet.id());
         if (entity != null) {
            AiServerEvidence.init_V_I(packet.id(), entity.getUUID(), packet.movement());
         }
      }
   }

   @Inject(method = "handleDamageEvent", at = @At("RETURN"))
   private void autism$aiPlayerDamageApplied(ClientboundDamageEventPacket packet, CallbackInfo ci) {
      if (!AutismLiteVariant.enabled()) {
         ClutchModule.init_V_ClientboundDamageE_3(packet);
         AntiKnockbackModule.init_V_ClientboundDamageE(packet);
         WTappingModule.init(packet);
         KBDisplacementModule.init_V_ClientboundDamageE_2(packet);
         Minecraft mc = Minecraft.getInstance();
         LocalPlayer player = mc.player;
         if (player != null && mc.level != null && packet.entityId() == player.getId() && mc.level.getEntity(packet.entityId()) == player) {
            AiServerEvidence.init(mc.level, player, player.getUUID(), player.tickCount, player.getHealth(), packet.getSource(mc.level).is(DamageTypes.ON_FIRE));
         }
      }
   }

   @Inject(method = "handleExplosion", at = @At("RETURN"))
   private void autism$clutchExplosion(ClientboundExplodePacket packet, CallbackInfo ci) {
      if (!AutismLiteVariant.enabled()) {
         ClutchModule.init_V_ClientboundExplode(packet);
      }
   }

   @Inject(method = "handleSetHealth", at = @At("RETURN"))
   private void autism$aiHealthApplied(ClientboundSetHealthPacket packet, CallbackInfo ci) {
      if (!AutismLiteVariant.enabled()) {
         Minecraft mc = Minecraft.getInstance();
         if (mc.player != null && mc.level != null) {
            AiServerEvidence.init(mc.level, mc.player, mc.player.getUUID(), mc.player.tickCount, mc.player.getHealth());
         }

         if (packet.getHealth() <= 0.0F) {
            AiServerEvidence.load_V();
         }
      }
   }

   @Inject(method = "handlePlayerCombatKill", at = @At("RETURN"))
   private void autism$aiDeathApplied(ClientboundPlayerCombatKillPacket packet, CallbackInfo ci) {
      if (!AutismLiteVariant.enabled()) {
         LocalPlayer player = Minecraft.getInstance().player;
         if (player != null && packet.playerId() == player.getId()) {
            AiServerEvidence.load_V();
         }
      }
   }

   @Inject(method = "handleBlockChangedAck", at = @At("RETURN"))
   private void autism$onBlockPredictionAckApplied(ClientboundBlockChangedAckPacket packet, CallbackInfo ci) {
      ScaffoldModule.init_V_I_8(packet.sequence());
      if (!AutismLiteVariant.enabled()) {
         AnchorAuraModule.init_V_I(packet.sequence());
      }

      if (!AutismLiteVariant.enabled()) {
         LeafDupePacketTracker.load(packet);
      }

      autismclient.util.villager.BlockChangeAckTracker.init_V(Minecraft.getInstance().level, packet.sequence());
   }

   @Inject(method = "handlePlayerInfoUpdate", at = @At("RETURN"))
   private void autism$vanishPlayerInfo(ClientboundPlayerInfoUpdatePacket packet, CallbackInfo ci) {
      if (!AutismLiteVariant.enabled()) {
         AntiVanishModule.init_V_ClientPacketListen_Packet((ClientPacketListener)(Object)this, packet);
      }
   }

   @Inject(method = "handlePlayerInfoRemove", at = @At("RETURN"))
   private void autism$vanishPlayerRemove(ClientboundPlayerInfoRemovePacket packet, CallbackInfo ci) {
      if (!AutismLiteVariant.enabled()) {
         AntiVanishModule.init_V_ClientPacketListen_Packet((ClientPacketListener)(Object)this, packet);
      }
   }

   @Inject(method = "handleBlockDestruction", at = @At("RETURN"))
   private void autism$vanishPlayerDig(ClientboundBlockDestructionPacket packet, CallbackInfo ci) {
      if (!AutismLiteVariant.enabled()) {
         AntiVanishModule.init_V_ClientPacketListen_Packet((ClientPacketListener)(Object)this, packet);
      }
   }

   @Inject(method = "handleCommandSuggestions", at = @At("RETURN"))
   private void autism$vanishCompletion(ClientboundCommandSuggestionsPacket packet, CallbackInfo ci) {
      if (!AutismLiteVariant.enabled()) {
         AntiVanishModule.init_V_ClientPacketListen_Packet((ClientPacketListener)(Object)this, packet);
      }
   }

   @Unique
   private void autism$captureView() {
      Minecraft mc = Minecraft.getInstance();
      if (mc.player != null) {
         RotationMath.Rotation wire = ScaffoldModule.load_ce_DataUtil_3();
         if (wire != null) {
            this.autism$viewYaw = mc.player.getYRot();
            this.autism$viewPitch = mc.player.getXRot();
            this.autism$viewYawO = mc.player.yRotO;
            this.autism$viewPitchO = mc.player.xRotO;
            this.autism$viewCaptured = true;
            mc.player.setYRot(wire.yaw());
            mc.player.setXRot(wire.pitch());
         }
      }
   }

   @Unique
   private void autism$restoreView(Minecraft mc) {
      if (this.autism$viewCaptured) {
         this.autism$viewCaptured = false;
         if (mc.player != null) {
            ScaffoldModule.init_V_F_F(mc.player.getYRot(), mc.player.getXRot());
            mc.player.setYRot(this.autism$viewYaw);
            mc.player.setXRot(this.autism$viewPitch);
            mc.player.yRotO = this.autism$viewYawO;
            mc.player.xRotO = this.autism$viewPitchO;
         }
      }
   }

   @Inject(method = "handleMovePlayer", at = @At("HEAD"))
   private void autism$disarmViewCaptureMove(ClientboundPlayerPositionPacket packet, CallbackInfo ci) {
      this.autism$viewCaptured = false;
   }

   @Inject(method = "handleRotatePlayer", at = @At("HEAD"))
   private void autism$disarmViewCaptureRotate(ClientboundPlayerRotationPacket packet, CallbackInfo ci) {
      this.autism$viewCaptured = false;
   }

   @Inject(
      method = "handleMovePlayer",
      at = @At(
         value = "INVOKE",
         target = "Lnet/minecraft/client/multiplayer/ClientPacketListener;setValuesFromPositionPacket(Lnet/minecraft/world/entity/PositionMoveRotation;Ljava/util/Set;Lnet/minecraft/world/entity/Entity;Z)Z"
      )
   )
   private void autism$captureViewBeforeTeleport(ClientboundPlayerPositionPacket packet, CallbackInfo ci) {
      LocalPlayer player = Minecraft.getInstance().player;
      this.autism$clutchCorrectionFrom = player == null ? null : player.position();
      this.autism$pacedTeleportCorrectionToken = PkgCls_a.init_J();
      this.autism$captureView();
   }

   @Inject(method = "handleMovePlayer", at = @At("RETURN"))
   private void autism$onServerPositionCorrection(ClientboundPlayerPositionPacket packet, CallbackInfo ci) {
      SodiumRegionResourceProbe.load_V();
      Minecraft mc = Minecraft.getInstance();
      this.autism$restoreView(mc);
      long correctionToken = this.autism$pacedTeleportCorrectionToken;
      this.autism$pacedTeleportCorrectionToken = 0L;
      if (mc.player != null) {
         if (!AutismLiteVariant.enabled()) {
            autismclient.modules.AirStuckModule.init_V();
         }

         if (!AutismLiteVariant.enabled()) {
            AntiKnockbackModule.init_V_16();
         }

         if (!AutismLiteVariant.enabled()) {
            ClutchModule.init_V_Vec3(this.autism$clutchCorrectionFrom);
         }

         this.autism$clutchCorrectionFrom = null;
         PkgCls_a.init_V_J_Vec3(correctionToken, mc.player.position());
         ScaffoldModule.sync_V_6();
      }
   }

   @Inject(method = "handleRotatePlayer", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/player/Player;setYRot(F)V"))
   private void autism$captureViewBeforeRotate(ClientboundPlayerRotationPacket packet, CallbackInfo ci) {
      this.autism$captureView();
   }

   @Inject(method = "handleRotatePlayer", at = @At("RETURN"))
   private void autism$onServerRotationCorrection(ClientboundPlayerRotationPacket packet, CallbackInfo ci) {
      this.autism$restoreView(Minecraft.getInstance());
   }

   @Inject(method = "handleMoveVehicle", at = @At("HEAD"))
   private void autism$captureVehicleCorrection(ClientboundMoveVehiclePacket packet, CallbackInfo ci) {
      this.autism$pacedTeleportVehicleCorrectionToken = PkgCls_a.load_J();
   }

   @Inject(method = "handleMoveVehicle", at = @At("RETURN"))
   private void autism$onServerVehicleCorrection(ClientboundMoveVehiclePacket packet, CallbackInfo ci) {
      Minecraft mc = Minecraft.getInstance();
      long correctionToken = this.autism$pacedTeleportVehicleCorrectionToken;
      this.autism$pacedTeleportVehicleCorrectionToken = 0L;
      if (mc.player != null && mc.player.getVehicle() != null) {
         PkgCls_a.load(correctionToken, mc.player.getVehicle().position());
      }
   }

   @Inject(method = "handleCommands", at = @At("RETURN"))
   private void autism$onCommandTreeApplied(ClientboundCommandsPacket packet, CallbackInfo ci) {
      ModuleRuntimeManager module = ModuleRuntimeManager.init_PoolEntry();
      if (module != null) {
         AutismServerInfoOverlay overlay = module.load_AutismServerInfoOv();
         if (overlay != null) {
            overlay.set_V_9();
         }
      }
   }

   @Inject(method = "sendChat", at = @At("HEAD"), cancellable = true)
   private void autism$infiniChatSplit(String message, CallbackInfo ci) {
      if (AutismConfig.getGlobal().infiniChat
         && !aD.read_Z()
         && message != null
         && message.length() > 256
         && (autismclient.commands.CommandManager.isBypassingIntercept() || !autismclient.commands.CommandManager.hasCommandPrefix(message))) {
         ClientPacketListener self = (ClientPacketListener)(Object)this;

         for (int i = 0; i < message.length(); i += 256) {
            autismclient.commands.CommandManager.sendChatRaw(self, message.substring(i, Math.min(message.length(), i + 256)));
         }

         ci.cancel();
      }
   }

   @Inject(method = "sendChat", at = @At("HEAD"), cancellable = true)
   private void autism$dispatchAutismCommand(String message, CallbackInfo ci) {
      try {
         if (autismclient.commands.CommandManager.isBypassingIntercept()) {
            return;
         }

         if (!autismclient.commands.CommandManager.hasCommandPrefix(message)) {
            return;
         }

         if (autismclient.commands.CommandManager.shouldSuppressInHiddenMode(message)) {
            ci.cancel();
            return;
         }

         String body = autismclient.commands.CommandManager.stripPrefix(message);
         if (body.isBlank()) {
            ci.cancel();
            return;
         }

         AutismClientMessaging.rememberRecentChat(message);
         autismclient.commands.CommandManager.execute(body);
         ci.cancel();
      } catch (Throwable t) {
         AutismClientAddon.LOG.warn("[Commands] sendChat interception failed for '{}'", message, t);
         ci.cancel();
      }
   }

   @Inject(method = "handleContainerContent", at = @At("RETURN"))
   private void yang$onInventory(ClientboundContainerSetContentPacket packet, CallbackInfo ci) {
      if (!aD.read_Z()) {
         MacroWaitRegistry.init_V();
         boolean macroWaits = MacroWaitRegistry.load_Z();
         boolean inventoryTweaks = InventoryTweaksModule.write_Z_3();
         if (macroWaits || inventoryTweaks) {
            if (macroWaits) {
               MacroWaitRegistry.init_V(Minecraft.getInstance());
            }

            if (inventoryTweaks) {
               InventoryTweaksModule.init_V_I_5(packet.containerId());
            }
         }
      }
   }

   @Inject(method = "handleContainerSetSlot", at = @At("RETURN"))
   private void yang$onSlotUpdate(ClientboundContainerSetSlotPacket packet, CallbackInfo ci) {
      if (!aD.read_Z()) {
         MacroWaitRegistry.init_V();
         boolean macroWaits = MacroWaitRegistry.load_Z();
         boolean inventoryTweaks = InventoryTweaksModule.write_Z_3();
         if (macroWaits || inventoryTweaks) {
            if (macroWaits) {
               MacroWaitRegistry.init_V(packet.getSlot());
            }

            if (inventoryTweaks) {
               InventoryTweaksModule.init_V_I_5(packet.getContainerId());
            }
         }
      }
   }

   @Inject(method = "handleSetCursorItem", at = @At("RETURN"))
   private void autism$onSetCursorItem(ClientboundSetCursorItemPacket packet, CallbackInfo ci) {
      if (!AutismLiteVariant.enabled()) {
         AiContainerTransactions.init(packet.contents());
      }

      if (!aD.read_Z()) {
         MacroWaitRegistry.init_V();
         if (MacroWaitRegistry.load_Z()) {
            MacroWaitRegistry.init_V(Minecraft.getInstance());
         }
      }
   }

   @Inject(method = "handleSetPlayerInventory", at = @At("RETURN"))
   private void autism$onSetPlayerInventory(ClientboundSetPlayerInventoryPacket packet, CallbackInfo ci) {
      if (!aD.read_Z()) {
         MacroWaitRegistry.init_V();
         if (MacroWaitRegistry.load_Z()) {
            MacroWaitRegistry.init_V(Minecraft.getInstance());
         }
      }
   }

   @Inject(method = "handleSoundEvent", at = @At("RETURN"))
   private void yang$onPlaySound(ClientboundSoundPacket packet, CallbackInfo ci) {
      if (!aD.read_Z()) {
         boolean macroWaits = MacroWaitRegistry.read_Z();
         boolean moduleHooks = cf.init_Z_J(32L);
         if (macroWaits || moduleHooks) {
            if (macroWaits) {
               autism$dispatchMacroSound(packet);
            }

            if (moduleHooks) {
               al.init_V_ClientboundSoundPa(packet);
            }
         }
      }
   }

   @Inject(method = "handleSoundEntityEvent", at = @At("RETURN"))
   private void autism$onPlayEntitySound(ClientboundSoundEntityPacket packet, CallbackInfo ci) {
      if (!aD.read_Z() && MacroWaitRegistry.read_Z()) {
         try {
            Minecraft mc = Minecraft.getInstance();
            if (mc == null || mc.level == null || packet == null) {
               return;
            }

            Entity entity = mc.level.getEntity(packet.getId());
            if (entity == null) {
               return;
            }

            String soundId = packet.getSound().value().location().toString();
            MacroWaitRegistry.init(soundId, entity.getX(), entity.getY(), entity.getZ());
         } catch (Exception var6) {
         }
      }
   }

   @Inject(method = "handleSetTime", at = @At("RETURN"))
   private void yang$onWorldTimeUpdate(ClientboundSetTimePacket packet, CallbackInfo ci) {
      if (!aD.read_Z() && this.autism$packetHooksActive()) {
         AutismSessionState.init_ck().poll_V();
      }
   }

   @Inject(method = "handleAddEntity", at = @At("RETURN"))
   private void autism$antiBotSpawn(ClientboundAddEntityPacket packet, CallbackInfo ci) {
      if (!AutismLiteVariant.enabled()) {
         EndPortalModule.init((ClientPacketListener)(Object)this, packet);
      }

      if (!AutismLiteVariant.enabled() && packet.getType() == EntityTypes.PLAYER) {
         AntiBotTracker.init_V(packet.getId());
      }
   }

   @Inject(method = "handleRemoveEntities", at = @At("RETURN"))
   private void autism$antiBotRemove(ClientboundRemoveEntitiesPacket packet, CallbackInfo ci) {
      if (!AutismLiteVariant.enabled()) {
         AntiBotTracker.init(packet.entityIds());
      }
   }

   @Inject(method = "handleUpdateAttributes", at = @At("RETURN"))
   private void autism$antiBotAttributes(ClientboundUpdateAttributesPacket packet, CallbackInfo ci) {
      if (!AutismLiteVariant.enabled()) {
         AntiBotTracker.load(packet.getEntityId());
      }
   }

   @Inject(method = "handleAnimate", at = @At("RETURN"))
   private void autism$antiBotAnimate(ClientboundAnimatePacket packet, CallbackInfo ci) {
      if (!AutismLiteVariant.enabled()) {
         AntiBotTracker.init(packet.getId(), packet.getAction());
      }
   }

   @Inject(method = "handlePlayerInfoUpdate", at = @At("RETURN"))
   private void autism$antiBotInfoUpdate(ClientboundPlayerInfoUpdatePacket packet, CallbackInfo ci) {
      if (!AutismLiteVariant.enabled()) {
         AntiBotTracker.init(packet);
      }
   }

   @Inject(method = "handlePlayerInfoRemove", at = @At("RETURN"))
   private void autism$antiBotInfoRemove(ClientboundPlayerInfoRemovePacket packet, CallbackInfo ci) {
      if (!AutismLiteVariant.enabled()) {
         AntiBotTracker.init(packet.profileIds());
      }
   }

   @Inject(method = "handleMoveEntity", at = @At("RETURN"))
   private void autism$antiBotMove(ClientboundMoveEntityPacket packet, CallbackInfo ci) {
      if (!AutismLiteVariant.enabled() && packet.hasPosition()) {
         ClientLevel level = Minecraft.getInstance().level;
         if (level != null) {
            AntiBotTracker.load_V(packet.getEntity(level));
         }
      }
   }

   @Inject(method = "handleEntityPositionSync", at = @At("RETURN"))
   private void autism$antiBotPositionSync(ClientboundEntityPositionSyncPacket packet, CallbackInfo ci) {
      if (!AutismLiteVariant.enabled()) {
         ClientLevel level = Minecraft.getInstance().level;
         if (level != null) {
            AntiBotTracker.load_V(level.getEntity(packet.id()));
         }
      }
   }

   @Unique
   private boolean autism$packetHooksActive() {
      ModuleRuntimeManager module = ModuleRuntimeManager.init_PoolEntry();
      return module != null && module.read_Z();
   }

   @Unique
   private static void autism$dispatchMacroSound(ClientboundSoundPacket packet) {
      try {
         if (packet == null) {
            return;
         }

         String soundId = packet.getSound().value().location().toString();
         MacroWaitRegistry.init(soundId, packet.getX(), packet.getY(), packet.getZ());
      } catch (Exception var2) {
      }
   }
}

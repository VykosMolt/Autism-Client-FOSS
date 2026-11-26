package autismclient.mixin;

import autismclient.ai.ClassCache;
import autismclient.ai.aP;
import autismclient.compat263.CompatGLFW;
import autismclient.g.LateRegisterSynthesizer;
import autismclient.gui.StreamUtil;
import autismclient.gui.d.RefQueue;
import autismclient.gui.d.UtilCore;
import autismclient.gui.macro.editor.ActionEditorOverlay;
import autismclient.modules.AutoTotemModule;
import autismclient.modules.BlinkPacketManager;
import autismclient.modules.HandNoClipModule;
import autismclient.modules.AutoArmorModule;
import autismclient.modules.KBDisplacementModule;
import autismclient.modules.FreecamController;
import autismclient.modules.aD;
import autismclient.modules.ScaffoldModule;
import autismclient.modules.KillAuraModule;
import autismclient.modules.FrameEspCatalog;
import autismclient.modules.MovementModuleHooks;
import autismclient.modules.al;
import autismclient.modules.am;
import autismclient.modules.f;
import autismclient.util.AutismLiteVariant;
import autismclient.util.D;
import autismclient.util.DataPipe;
import autismclient.util.ListPool;
import autismclient.util.StreamGate;
import autismclient.util.aT;
import autismclient.util.aY;
import autismclient.util.as;
import autismclient.util.bI;
import autismclient.util.cG;
import autismclient.util.cd;
import autismclient.util.ck;
import autismclient.util.s;
import autismclient.util.i.VillagerDupeDriver;
import autismclient.util.macro.MacroExecutor;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Map.Entry;
import java.util.function.Consumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.ChatScreen;
import net.minecraft.client.gui.screens.LoadingOverlay;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.client.gui.screens.inventory.SignEditScreen;
import net.minecraft.core.Direction;
import net.minecraft.server.packs.resources.ReloadInstance;
import net.minecraft.util.ModCheck;
import net.minecraft.util.ModCheck.Confidence;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.At.Shift;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Minecraft.class)
public class AutismMinecraftClientMixin {
   @Unique
   private static final String PACKUTIL_WINDOW_TITLE = "AUTISM Client";
   @Unique
   private boolean autism$escapeWasDown;
   @Unique
   private boolean autism$inventoryWasDown;
   @Unique
   private final Map<Integer, Boolean> autism$commandBindWasDown = new HashMap<>();

   @WrapMethod(method = "pick")
   private void autism$wrapPickForCameraOverrides(float partialTicks, Operation<Void> original) {
      Minecraft self = (Minecraft)(Object)this;
      Entity restoreRemote = cd.init(self);
      try {
         Entity fcCam = null;
         double fcX = 0.0;
         double fcY = 0.0;
         double fcZ = 0.0;
         double fcXOld = 0.0;
         double fcYOld = 0.0;
         double fcZOld = 0.0;
         float fcYRot = 0.0F;
         float fcXRot = 0.0F;
         float fcYRotO = 0.0F;
         float fcXRotO = 0.0F;
         if (FreecamController.read()) {
            Entity cam = self.getCameraEntity();
            if (cam != null) {
               double eyeOffset = cam.getEyeY() - cam.getY();
               fcX = cam.getX();
               fcY = cam.getY();
               fcZ = cam.getZ();
               fcXOld = cam.xOld;
               fcYOld = cam.yOld;
               fcZOld = cam.zOld;
               fcYRot = cam.getYRot();
               fcXRot = cam.getXRot();
               fcYRotO = cam.yRotO;
               fcXRotO = cam.xRotO;
               double fx = FreecamController.init_D_F(partialTicks);
               double fy = FreecamController.load_D_F(partialTicks) - eyeOffset;
               double fz = FreecamController.read(partialTicks);
               float fyaw = FreecamController.init_F_F(partialTicks);
               float fpitch = FreecamController.load_F_F(partialTicks);
               cam.setPos(fx, fy, fz);
               cam.xOld = fx;
               cam.yOld = fy;
               cam.zOld = fz;
               cam.setYRot(fyaw);
               cam.setXRot(fpitch);
               cam.yRotO = fyaw;
               cam.xRotO = fpitch;
               fcCam = cam;
            }
         }

         try {
            original.call(partialTicks);
         } finally {
            if (fcCam != null) {
               fcCam.setPos(fcX, fcY, fcZ);
               fcCam.xOld = fcXOld;
               fcCam.yOld = fcYOld;
               fcCam.zOld = fcZOld;
               fcCam.setYRot(fcYRot);
               fcCam.setXRot(fcXRot);
               fcCam.yRotO = fcYRotO;
               fcCam.xRotO = fcXRotO;
            }
         }
      } finally {
         cd.init(self, restoreRemote);
      }
   }

   @Inject(method = "shouldEntityAppearGlowing", at = @At("RETURN"), cancellable = true)
   private void autism$frameEspGlow(Entity entity, CallbackInfoReturnable<Boolean> cir) {
      if (!cir.getReturnValueZ() && FrameEspCatalog.init_Z(entity)) {
         cir.setReturnValue(true);
      }
   }

   @Redirect(method = "*", at = @At(value = "NEW", target = "net/minecraft/client/gui/screens/LoadingOverlay"))
   private static LoadingOverlay autism$replaceLoadingOverlay(
      Minecraft minecraft, ReloadInstance reload, Consumer<Optional<Throwable>> onFinish, boolean fadeIn
   ) {
      return StreamUtil.init_LoadingOverlay_Minecraft(minecraft, reload, onFinish, fadeIn);
   }

   @Inject(method = "createTitle", at = @At("HEAD"), cancellable = true)
   private void autism$createCustomWindowTitle(CallbackInfoReturnable<String> cir) {
      if (!aD.init_Z() && !AutismLiteVariant.enabled()) {
         cir.setReturnValue("AUTISM Client");
      }
   }

   @Inject(method = "checkModStatus", at = @At("HEAD"), cancellable = true)
   private static void autism$reportVanillaWhileHidden(CallbackInfoReturnable<ModCheck> cir) {
      if (aD.init_Z()) {
         cir.setReturnValue(new ModCheck(Confidence.PROBABLY_NOT, "Client jar signature and brand is untouched"));
      }
   }

   @Inject(method = "tick", at = @At("HEAD"))
   private void autism$onTickHead(CallbackInfo ci) {
      cG.read((Minecraft)(Object)this);
      ck.init_ck().push_V();
      as.open();
      al.update_V();
      s.init_V();
      am.read_V();
      if (!AutismLiteVariant.enabled()) {
         aY.load();
         ClassCache.init_V_Minecraft((Minecraft)(Object)this);
      }

      if (aD.read_Z()) {
         as.push();
      } else {
         LateRegisterSynthesizer.init((Minecraft)(Object)this);
         MovementModuleHooks.init_V();
         if (!AutismLiteVariant.enabled()) {
            KBDisplacementModule.load_V_17();
         }

         MacroExecutor.load_V();
         VillagerDupeDriver.init_V_Minecraft((Minecraft)(Object)this);
         if (D.init_Z()) {
            D.load_V();
         }

         autismclient.modules.ModuleRuntimeManager autism = autismclient.modules.ModuleRuntimeManager.init_PoolEntry();
         if (!aD.init_Z() && autism.valid()) {
            this.autism$pollCommandBinds(autism);
         }
      }
   }

   @Unique
   private void autism$pollCommandBinds(autismclient.modules.ModuleRuntimeManager module) {
      Minecraft client = (Minecraft)(Object)this;
      if (client.getWindow() != null && !(client.gui.screen() instanceof ChatScreen) && !(client.gui.screen() instanceof SignEditScreen)) {
         Map<Integer, String> binds = module.init_Map();
         if (!binds.isEmpty()) {
            long handle = client.getWindow().handle();

            for (Entry<Integer, String> entry : binds.entrySet()) {
               int key = entry.getKey();
               boolean down = CompatGLFW.glfwGetKey(handle, key) == 1;
               boolean wasDown = this.autism$commandBindWasDown.getOrDefault(key, false);
               this.autism$commandBindWasDown.put(key, down);
               if (down && !wasDown) {
                  String cmd = entry.getValue();
                  if (cmd != null && !cmd.isBlank()) {
                     autismclient.commands.CommandManager.read(cmd);
                  }
               }
            }
         }
      }
   }

   @Inject(method = "tick", at = @At("TAIL"))
   private void autism$repairStrayTitleScreen(CallbackInfo ci) {
      Minecraft client = (Minecraft)(Object)this;
      if (client.gui.screen() instanceof TitleScreen) {
         if (aD.init_Z()) {
            client.gui.setScreen(new RefQueue());
         } else if (!AutismLiteVariant.enabled() && aT.init()) {
            client.gui.setScreen(new UtilCore());
         }
      }
   }

   @Inject(method = "tick", at = @At("TAIL"))
   private void autism$swapMenuOnPanicChange(CallbackInfo ci) {
      if (!AutismLiteVariant.enabled()) {
         Minecraft client = (Minecraft)(Object)this;
         if (aD.init_Z() && client.gui.screen() instanceof UtilCore) {
            client.gui.setScreen(new RefQueue());
         } else if (!aD.init_Z() && client.gui.screen() instanceof RefQueue && aT.init()) {
            client.gui.setScreen(new UtilCore());
         }
      }
   }

   @Inject(method = "startAttack", at = @At("HEAD"), cancellable = true)
   private void autism$onDoAttack(CallbackInfoReturnable<Boolean> cir) {
      if (!aD.read_Z()) {
         if (!AutismLiteVariant.enabled() && aP.load_Z()) {
            cir.setReturnValue(false);
         } else if (s.update_Z()) {
            if (!s.sync_Z()) {
               cir.setReturnValue(false);
            }
         } else {
            Minecraft client = (Minecraft)(Object)this;
            if (client.hitResult instanceof BlockHitResult && autismclient.util.ai.init(client)) {
               cir.setReturnValue(false);
            } else if (cd.init_Z()) {
               cir.setReturnValue(false);
            } else if (ck.init_ck().frame()) {
               cir.setReturnValue(false);
            } else if (al.init_Z_HitResult(client.hitResult)) {
               cir.setReturnValue(false);
            } else if ((AutismLiteVariant.enabled() || !ListPool.update()) && !DataPipe.load()) {
               if (ck.init_ck().count()) {
                  ck.init_ck().index();
                  cir.setReturnValue(false);
               }
            } else {
               cir.setReturnValue(false);
            }
         }
      }
   }

   @Inject(method = "continueAttack", at = @At("HEAD"), cancellable = true)
   private void autism$pilotContinueAttack(boolean leftClick, CallbackInfo ci) {
      if (!AutismLiteVariant.enabled() && aP.load_Z()) {
         ci.cancel();
      } else if (!AutismLiteVariant.enabled() && ListPool.update()) {
         ci.cancel();
      } else if (cd.init_Z()) {
         ci.cancel();
      } else if (autismclient.util.ai.init((Minecraft)(Object)this)) {
         ci.cancel();
      } else if (AutoTotemModule.init_Z_6() || AutoArmorModule.init_Z_20()) {
         ci.cancel();
      } else if (!s.close_Z() && DataPipe.load()) {
         ci.cancel();
      }
   }

   @WrapMethod(method = "startUseItem")
   private void autism$manualCombatUse(Operation<Void> original) {
      if (!AutismLiteVariant.enabled()) {
         Minecraft client = (Minecraft)(Object)this;
         if (!s.read_Z() && client.player != null && client.hitResult instanceof EntityHitResult && KillAuraModule.init_Z_31()) {
            HitResult previous = client.hitResult;
            client.hitResult = BlockHitResult.miss(client.player.getEyePosition(), Direction.DOWN, client.player.blockPosition());

            try {
               original.call();
            } finally {
               client.hitResult = previous;
            }
         } else if (ListPool.init_Z(client)) {
            ListPool.Inner_DataUtil frame = ListPool.init_ListPool_DataUtil(client);
            if (frame != null) {
               try {
                  original.call();
               } finally {
                  frame.init(client);
               }
            }
         } else {
            original.call();
         }
      } else {
         original.call();
      }
   }

   @Inject(method = "startUseItem", at = @At("HEAD"), cancellable = true)
   private void autism$cancelUseForModules(CallbackInfo ci) {
      if (!AutismLiteVariant.enabled() && aP.load_Z()) {
         ci.cancel();
      } else if (!aD.read_Z()) {
         if (s.read_Z()) {
            if (!s.write_Z()) {
               ci.cancel();
            }
         } else {
            Minecraft client = (Minecraft)(Object)this;
            if (cd.init_Z()) {
               ci.cancel();
            } else if (DataPipe.load()) {
               ci.cancel();
            } else if (StreamGate.init_Z(client)) {
               ci.cancel();
            } else {
               ck captureState = ck.init_ck();
               if (captureState.frame()) {
                  if (client.hitResult instanceof EntityHitResult entityHit) {
                     captureState.init_Z_Entity(entityHit.getEntity());
                  }

                  ci.cancel();
               } else if (captureState.pool()) {
                  if (client.hitResult instanceof BlockHitResult blockHit) {
                     captureState.init_Z_BlockPos_Direction(blockHit.getBlockPos(), blockHit.getDirection());
                  }

                  ci.cancel();
               } else if (al.init_Z_HitResult_InteractionHand(client.hitResult, InteractionHand.MAIN_HAND)) {
                  ci.cancel();
               } else if (f.init_Z() && as.update_Z()) {
                  if (!f.read()) {
                     ci.cancel();
                  }
               } else if (ScaffoldModule.init_Z_26() && !ScaffoldModule.load_Z_11()) {
                  ci.cancel();
               }
            }
         }
      }
   }

   @ModifyExpressionValue(
      method = "startUseItem",
      at = @At(value = "INVOKE", target = "Lnet/minecraft/client/player/LocalPlayer;isWithinEntityInteractionRange(Lnet/minecraft/world/entity/Entity;D)Z")
   )
   private boolean autism$freecamEntityUseReach(boolean original) {
      return original || FreecamController.read();
   }

   @ModifyExpressionValue(
      method = "startAttack",
      at = @At(
         value = "INVOKE",
         target = "Lnet/minecraft/world/item/component/AttackRange;isInRange(Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/world/phys/Vec3;)Z"
      )
   )
   private boolean autism$freecamEntityAttackReach(boolean original) {
      return original || FreecamController.read();
   }

   @ModifyExpressionValue(
      method = "startAttack",
      at = @At(value = "FIELD", target = "Lnet/minecraft/client/Minecraft;hitResult:Lnet/minecraft/world/phys/HitResult;")
   )
   private HitResult autism$attackOnlyWhatTheCameraSees(HitResult original) {
      if (s.close_Z()) {
         return original;
      }

      Minecraft client = (Minecraft)(Object)this;
      HitResult hit = DataPipe.init(client, original);
      return !AutismLiteVariant.enabled() ? HandNoClipModule.init(client, hit, true, true) : hit;
   }

   @ModifyExpressionValue(
      method = "continueAttack",
      at = @At(value = "FIELD", target = "Lnet/minecraft/client/Minecraft;hitResult:Lnet/minecraft/world/phys/HitResult;")
   )
   private HitResult autism$mineOnlyWhatTheCameraSees(HitResult original) {
      if (s.close_Z()) {
         return original;
      }

      Minecraft client = (Minecraft)(Object)this;
      HitResult hit = DataPipe.init(client, original);
      return !AutismLiteVariant.enabled() ? HandNoClipModule.init(client, hit, true, true) : hit;
   }

   @ModifyExpressionValue(
      method = "startUseItem",
      at = @At(value = "FIELD", target = "Lnet/minecraft/client/Minecraft;hitResult:Lnet/minecraft/world/phys/HitResult;")
   )
   private HitResult autism$useOnlyWhatTheCameraSees(HitResult original) {
      if (!AutismLiteVariant.enabled() && ListPool.flush()) {
         return original;
      } else if (!s.flush_Z() && !ScaffoldModule.init_Z_26()) {
         Minecraft client = (Minecraft)(Object)this;
         HitResult hit = DataPipe.init(client, original);
         return !AutismLiteVariant.enabled() ? HandNoClipModule.init(client, hit, false, false) : hit;
      } else {
         return original;
      }
   }

   @ModifyExpressionValue(
      method = "startUseItem",
      at = @At(value = "FIELD", target = "Lnet/minecraft/client/Minecraft;hitResult:Lnet/minecraft/world/phys/HitResult;")
   )
   private HitResult autism$fastExpUseMissesTargets(HitResult original) {
      if (!f.load()) {
         return original;
      }

      Minecraft client = (Minecraft)(Object)this;
      return client.player == null ? original : BlockHitResult.miss(client.player.getEyePosition(), Direction.DOWN, client.player.blockPosition());
   }

   @Inject(method = "handleKeybinds", at = @At("HEAD"), cancellable = true)
   private void autism$cancelCaptureOnEscape(CallbackInfo ci) {
      ScaffoldModule.write_V_12();
      if (!AutismLiteVariant.enabled()) {
         ListPool.init_V((Minecraft)(Object)this);
      }

      DataPipe.init_V((Minecraft)(Object)this);
      s.update_V();
      as.close();
      Minecraft client = (Minecraft)(Object)this;
      if (client.getWindow() == null) {
         s.sync_V();
         as.poll();
      } else if (aD.read_Z()) {
         s.sync_V();
         as.poll();
      } else {
         boolean escapeDown = CompatGLFW.glfwGetKey(client.getWindow().handle(), 256) == 1;
         boolean justPressed = escapeDown && !this.autism$escapeWasDown;
         this.autism$escapeWasDown = escapeDown;
         boolean inventoryDown = client.options != null && client.options.keyInventory.isDown();
         boolean inventoryJustPressed = inventoryDown && !this.autism$inventoryWasDown;
         this.autism$inventoryWasDown = inventoryDown;
         if (justPressed || inventoryJustPressed) {
            if (justPressed && bI.load_Z()) {
               s.sync_V();
               as.poll();
               ci.cancel();
               return;
            }

            if (ck.init_ck().flags()) {
               s.sync_V();
               as.poll();
               ci.cancel();
               return;
            }

            ActionEditorOverlay actionEditor = ActionEditorOverlay.load_ActionEditorOverla();
            if (actionEditor != null && actionEditor.poll_Z_2()) {
               s.sync_V();
               as.poll();
               ci.cancel();
            }
         }

         if (!AutismLiteVariant.enabled() && !ci.isCancelled()) {
            autismclient.util.a.KeyClickOutput.read_V();
         }
      }
   }

   @WrapMethod(method = "handleKeybinds")
   private void autism$restoreManualUseKey(Operation<Void> original) {
      try {
         original.call();
      } finally {
         s.sync_V();
         as.poll();
         if (!AutismLiteVariant.enabled()) {
            autismclient.util.a.KeyClickOutput.write();
         }

         if (!AutismLiteVariant.enabled()) {
            KillAuraModule.read_V_14();
         }

         if (!AutismLiteVariant.enabled()) {
            ListPool.load_V((Minecraft)(Object)this);
         }
      }
   }

   @Inject(method = "pauseGame", at = @At("HEAD"), cancellable = true)
   private void autism$cancelLostFocusPause(boolean suppressPauseMenuIfWeReallyArePausing, CallbackInfo ci) {
      if (!aD.read_Z()) {
         Minecraft client = (Minecraft)(Object)this;
         if (bI.load_Z()) {
            ci.cancel();
         } else if (ck.init_ck().flags()) {
            ci.cancel();
         } else {
            ActionEditorOverlay actionEditor = ActionEditorOverlay.load_ActionEditorOverla();
            if (actionEditor != null && actionEditor.push_Z_2()) {
               if (actionEditor.poll_Z_2()) {
                  ci.cancel();
               } else {
                  ci.cancel();
               }
            } else if (client.getWindow() != null && !client.getWindow().isFocused()) {
               autismclient.modules.ModuleRuntimeManager module = autismclient.modules.ModuleRuntimeManager.init_PoolEntry();
               if (module != null && module.init_Z() && module.poll_Z()) {
                  ci.cancel();
               }
            }
         }
      }
   }

   @Inject(method = "getTickTargetMillis", at = @At("RETURN"), cancellable = true)
   private void autism$applySpeedTimer(float defaultTickTargetMillis, CallbackInfoReturnable<Float> cir) {
      Minecraft client = (Minecraft)(Object)this;
      if (Boolean.getBoolean("autism.ai.schematics") && client.player != null && client.getSingleplayerServer() != null) {
         int qualificationRate = Math.clamp(Integer.getInteger("autism.ai.schematics.tickRate", 20).intValue(), 20, 100);
         if (qualificationRate != 20) {
            cir.setReturnValue(1000.0F / qualificationRate);
            return;
         }
      }

      if (Boolean.getBoolean("autism.ai.quick") && client.player != null && client.getSingleplayerServer() != null) {
         int qualificationRate = Math.clamp(Integer.getInteger("autism.ai.quick.tickRate", 20).intValue(), 20, 100);
         if (qualificationRate != 20) {
            cir.setReturnValue(1000.0F / qualificationRate);
            return;
         }
      }

      if (!aD.read_Z() && ((Minecraft)(Object)this).player != null) {
         float multiplier = MovementModuleHooks.init_F();
         if (multiplier != 1.0F) {
            cir.setReturnValue(cir.getReturnValue() / multiplier);
         }
      }
   }

   @Inject(method = "runTick", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/Minecraft;runAllTasks()V", shift = Shift.BEFORE))
   private void autism$onPacketProcessFrame(CallbackInfo ci) {
      al.open_V();
      BlinkPacketManager.write();
      VillagerDupeDriver.load_V_Minecraft((Minecraft)(Object)this);
   }

   @Inject(method = "runTick", at = @At("TAIL"))
   private void autism$onInputFrame(CallbackInfo ci) {
      if (!AutismLiteVariant.enabled()) {
         autismclient.util.a.KeyClickOutput.update();
         al.poll_V();
         autismclient.util.a.KeyClickOutput.sync();
      }
   }
}

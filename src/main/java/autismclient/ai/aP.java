package autismclient.ai;

import autismclient.compat263.Compat263;
import autismclient.compat263.CompatGLFW;
import autismclient.mixin.accessor.AutismKeyboardHandlerAccessor;
import autismclient.mixin.accessor.AutismMinecraftAccessor;
import autismclient.mixin.accessor.AutismMultiPlayerGameModeAccessor;
import autismclient.modules.Module;
import com.google.gson.JsonObject;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Options;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

public final class aP {
   private static final AiPlacementInput init_aO = new AiPlacementInput();
   private static aP init_aP;
   private static boolean init_Z;
   private final AiPlacementSession init_aR;
   private final Screen init_Screen;
   private final Runnable init_Runnable;
   private final BlockPos init_BlockPos;
   private final int init_I;
   private final String init_Str;
   private boolean load_Z = true;
   private boolean read;
   private double init_D;
   private JsonObject init_JsonObject;
   private int load_I;
   private Vec3 init_Vec3;
   private boolean write;
   private autismclient.modules.ScopedToggleOverride.Inner_StreamUtil init_aP_StreamUtil;
   private autismclient.modules.ScopedToggleOverride.Inner_StreamUtil load_aP_StreamUtil;

   private static boolean init_Z_aR(AiPlacementSession placement) {
      return placement instanceof SchematicSelection || placement instanceof SchematicWorkspace.Inner_CachePool || placement instanceof AiNavigationSelection;
   }

   private aP(AiPlacementSession placement, Runnable done) {
      this.init_aR = placement;
      this.init_Runnable = done;
      this.init_Screen = Minecraft.getInstance().gui.screen();
      this.init_BlockPos = placement.getOrigin();
      this.init_I = placement.getRotation();
      this.init_Str = placement.getMirror();
   }

   static void init_V_bW_Runnable(AiSchematicPlacement placement, Runnable done) {
      if (placement.isReadyToConfirm()) {
         init_V_aR_Runnable(placement, done);
      }
   }

   static void init_V_E_Runnable(AiFarmPlacement placement, Runnable done) {
      init_V_aR_Runnable(placement, done);
   }

   static boolean init_Z_aR_Runnable(AiPlacementSession placement, Runnable done) {
      init_V_aR_Runnable(placement, done);
      return init_aP != null && init_aP.init_aR == placement;
   }

   static SchematicSelection init_cU() {
      return init_aP != null && init_aP.init_aR instanceof SchematicSelection selection ? selection : null;
   }

   private static void init_V_aR_Runnable(AiPlacementSession placement, Runnable done) {
      Minecraft mc = Minecraft.getInstance();
      AiService service = AiService.getExistingInstance();
      if (placement.isActive() && !AiDirectOps.load_Z() && (service == null || !service.isActivelyDriving())) {
         if (!init_Z) {
            init_Z = true;
            ClientTickEvents.END_CLIENT_TICK.register(client -> load_V());
         }

         if (init_aP != null) {
            init_aP.init_V_Z_Z(false, false);
         }

         init_aP = new aP(placement, done);
         if (init_Z_aR(placement)) {
            Module freecam = autismclient.modules.al.init_Module_Str("freecam");
            if (freecam != null) {
               init_aP.load_aP_StreamUtil = freecam.borrowEnabled(true);
            }
         }

         init_aO.init_V();
         autismclient.util.ClientInteractionOverride.clearAll();
         long window = mc.getWindow().handle();

         for (int key = 32; key <= 348; key++) {
            if (CompatGLFW.glfwGetKey(window, key) == 1) {
               init_aO.init(key, true);
            }
         }

         for (int button = 0; button <= 7; button++) {
            if (CompatGLFW.glfwGetMouseButton(window, button) == 1) {
               init_aO.init(init_I_I(button), true);
            }
         }

         if (!init_Z_aR(placement)) {
            AiAssistant.init_ConfigStore().poll();
         }

         if (load(placement)) {
            AiVisualization.init(placement, true);
         }

         autismclient.gui.ui.widgets.TextInputWidget.clearGlobalFocus();
         autismclient.util.OverlayScreenController.setScreenExplicit(mc, null);
         mc.mouseHandler.grabMouse();
      }
   }

   public static boolean init_Z_Z(boolean parkour) {
      return init_Z_Z_Z(parkour, false);
   }

   public static boolean init_Z_Z_Z(boolean parkour, boolean assumeNoFallDamage) {
      return init_Z_Z_cy_CachePool(parkour, new AiToolCallParser.Inner_CachePool(assumeNoFallDamage, -1));
   }

   public static boolean init_Z_Z_cy_CachePool(boolean parkour, AiToolCallParser.Inner_CachePool options) {
      if (!autismclient.modules.aD.init_Z() && autismclient.e.MethodUtil.init()) {
         AiNavigationSelection selection = new AiNavigationSelection(parkour, options);
         init_V_aR_Runnable(selection, selection::write);
         return init_aP != null && init_aP.init_aR == selection;
      } else {
         return false;
      }
   }

   public static autismclient.ai.engine.AiStandingTarget.Inner_StreamUtil init_j_StreamUtil() {
      return init_aP != null && init_aP.init_aR instanceof AiNavigationSelection navigation ? navigation.getTarget() : null;
   }

   static AiNavigationSelection activeNavigationSelection() {
      return init_aP != null && init_aP.init_aR instanceof AiNavigationSelection navigation ? navigation : null;
   }

   private static boolean load(AiPlacementSession session) {
      return !(session instanceof SchematicSelection) && !(session instanceof AiNavigationSelection);
   }

   public static boolean init_Z() {
      return init_aP != null;
   }

   public static void init_V() {
      if (init_aP != null) {
         init_aP.init_V_Z_Z(false, true);
      }
   }

   public static boolean load_Z() {
      return init_aO.load_Z();
   }

   private static int init_I_I(int button) {
      return -1000 - button;
   }

   private static void load_V() {
      Minecraft mc = Minecraft.getInstance();

      for (int key : init_aO.heldKeys()) {
         boolean down = mc.isWindowActive()
            && (
               key <= -1000
                  ? CompatGLFW.glfwGetMouseButton(mc.getWindow().handle(), -1000 - key) == 1
                  : CompatGLFW.glfwGetKey(mc.getWindow().handle(), key) == 1
            );
         if (!down) {
            init_aO.init(key, false);
         }
      }

      aP mode = init_aP;
      if (mode != null) {
         if (mode.init_aR.isActive() && mc.gui.screen() == null && mc.isWindowActive()) {
            if (load(mode.init_aR)) {
               AiVisualization.init(mode.init_aR, mode.load_Z);
            }

            if (mode.load_Z) {
               mode.init_aR.moveToAim(!mode.read);
            }
         } else {
            mode.init_V_Z_Z(false, false);
         }
      }
   }

   public static boolean init_Z_I_KeyEvent(int action, KeyEvent event) {
      boolean wasHeld = init_aO.init(Compat263.keyEventGlfwKey(event));
      boolean owned = init_aO.init(Compat263.keyEventGlfwKey(event), action != 0);
      aP mode = init_aP;
      if (mode == null) {
         return owned;
      }

      if (action == 1 && !wasHeld) {
         if (mode.init_aR instanceof AiNavigationSelection navigation) {
            int key = Compat263.keyEventGlfwKey(event);
            if (key == 256) {
               mode.init_V_Z_Z(false, false);
            }

            if (navigation.isParkour()) {
               if (key == 69) {
                  mode.init_V_Z_Z(false, false);
               }

               if (key == 81) {
                  navigation.undoCheckpoint();
               }
            }
         } else if (mode.init_aR instanceof SchematicSelection) {
            if (Compat263.keyEventGlfwKey(event) == 69 || Compat263.keyEventGlfwKey(event) == 256) {
               mode.init_V_Z_Z(false, true);
            }

            if (Compat263.keyEventGlfwKey(event) == 257) {
               mode.init_V_Z_Z(true, true);
            }
         } else {
            switch (Compat263.keyEventGlfwKey(event)) {
               case 69:
                  mode.init_V_Z_Z(true, true);
                  break;
               case 71:
                  mode.load_Z = false;
                  mode.init_aR.snapToGround();
                  break;
               case 77:
                  mode.init_aR.cycleMirror();
                  break;
               case 82:
                  mode.init_aR.rotate(1);
                  break;
               case 256:
                  mode.init_V_Z_Z(false, true);
                  break;
               case 262:
                  mode.init(1, 0, 0);
                  break;
               case 263:
                  mode.init(-1, 0, 0);
                  break;
               case 264:
                  mode.init(0, 0, 1);
                  break;
               case 265:
                  mode.init(0, 0, -1);
                  break;
               case 266:
                  mode.init(0, 1, 0);
                  break;
               case 267:
                  mode.init(0, -1, 0);
            }
         }
      }

      if (init_aP != null && !init_Z_I(Compat263.keyEventGlfwKey(event))) {
         if (Compat263.keyEventGlfwKey(event) == 81 && mode.init_aR instanceof AiNavigationSelection navigation && navigation.isParkour()) {
            return true;
         }

         Options o = Minecraft.getInstance().options;

         for (KeyMapping mapping : new KeyMapping[]{o.keyUp, o.keyDown, o.keyLeft, o.keyRight, o.keyJump, o.keyShift, o.keySprint}) {
            if (mapping.matches(event)) {
               return false;
            }
         }

         return true;
      } else {
         return true;
      }
   }

   private static boolean init_Z_I(int key) {
      return key == 69 || key == 256 || key == 82 || key == 77 || key == 71 || key >= 262 && key <= 267;
   }

   public static boolean init_Z_I_I(int button, int action) {
      boolean wasHeld = init_aO.init(init_I_I(button));
      boolean owned = init_aO.init(init_I_I(button), action != 0);
      aP mode = init_aP;
      if (mode != null && action == 1 && !wasHeld) {
         if (mode.init_aR instanceof AiNavigationSelection navigation) {
            if (button == 0) {
               if (navigation.isParkour()) {
                  navigation.addCheckpoint();
               } else {
                  mode.init_V_Z_Z(false, false);
               }
            }

            if (button == 1 && navigation.updateAndCheck()) {
               mode.init_V_Z_Z(true, false);
            }
         } else if (mode.init_aR instanceof SchematicSelection selection) {
            if (button == 0 || button == 1) {
               selection.pickCorner(button == 0);
            }
         } else if (button == 1) {
            mode.load_Z = !mode.load_Z;
         }
      }

      return owned;
   }

   public static boolean init_Z_D(double amount) {
      aP mode = init_aP;
      if (mode == null) {
         return init_aO.load_Z();
      } else if (mode.init_aR instanceof AiNavigationSelection) {
         return true;
      } else if (mode.init_aR instanceof SchematicSelection selection) {
         selection.adjustDistance(amount);
         return true;
      } else {
         if (!Double.isFinite(amount)) {
            return true;
         }

         Minecraft mc = Minecraft.getInstance();
         long window = mc.getWindow().handle();
         boolean shift = CompatGLFW.glfwGetKey(window, 340) == 1 || CompatGLFW.glfwGetKey(window, 344) == 1;
         if (shift) {
            mode.init_aR.init(amount, 0.5, 0.5, 1.0, 1.0);
            mode.read = true;
         } else {
            mode.init_D += amount;
            int steps = (int)mode.init_D;
            if (steps != 0) {
               mode.init_D -= steps;
               mode.init_aR.rotate(steps);
            }
         }

         return true;
      }
   }

   private void init(int x, int y, int z) {
      this.load_Z = false;
      this.init_aR.init(x, y, z);
   }

   private void init_V_Z_Z(boolean use, boolean review) {
      if (init_aP == this && (!use || !(this.init_aR instanceof AiFarmPlacement farm && !farm.confirmCorner())) && (!use || this.init_aR.isReadyToConfirm())) {
         init_aP = null;
         init_aO.load_V();
         if (this.init_aP_StreamUtil != null) {
            this.init_aP_StreamUtil.close();
            this.init_aP_StreamUtil = null;
         }

         if (load(this.init_aR)) {
            AiVisualization.init(this.init_aR, false);
         }

         if (!use && this.init_aR.isActive()) {
            this.init_aR.init(this.init_BlockPos, this.init_I, this.init_Str);
         }

         if (this.load_aP_StreamUtil != null) {
            this.load_aP_StreamUtil.close();
            this.load_aP_StreamUtil = null;
         }

         Minecraft mc = Minecraft.getInstance();
         KeyMapping.releaseAll();
         if (this.init_aR instanceof AiNavigationSelection) {
            if (use) {
               this.init_Runnable.run();
            }
         } else if (review && this.init_aR.isActive()) {
            autismclient.util.OverlayScreenController.setScreenExplicit(mc, this.init_Screen);
            if (!init_Z_aR(this.init_aR)) {
               AiAssistant.init_ConfigStore().write_V();
            } else {
               SchematicWorkspace.getInstance().showOverlay();
            }

            if (use) {
               this.init_Runnable.run();
            }
         }
      }
   }

   private static aP.Inner_DataUtil init_aP_DataUtil() {
      aP mode = init_aP;
      if (mode.init_aR instanceof AiNavigationSelection selection) {
         String message = selection.isReadyToConfirm() ? "Destination " + selection.getOrigin().toShortString() : "Aim at a standing surface";
         if (selection.isParkour()) {
            int count = selection.checkpoints().size();
            if (count > 0) {
               message = count + (count == 1 ? " checkpoint  ·  " : " checkpoints  ·  ") + message;
            }

            return new aP.Inner_DataUtil(message, "Left: add  ·  Right: start  ·  Q: undo  ·  E: cancel");
         } else {
            return new aP.Inner_DataUtil(message, "Right-click: route  ·  Left-click: cancel");
         }
      } else if (mode.init_aR instanceof SchematicSelection selection) {
         String label = selection.isReadyToConfirm()
            ? selection.getArea().size() + "  |  Enter to use area"
            : (selection.cornerA == null ? "Pick corner A" : "Pick corner B");
         return new aP.Inner_DataUtil(label, "Left: A  ·  Right: B  ·  Wheel: " + selection.distanceText() + "  ·  E: cancel");
      } else if (mode.init_aR instanceof AiFarmPlacement farm) {
         return new aP.Inner_DataUtil(
            farm.update(),
            "Move and look normally | Right-click: "
               + (mode.load_Z ? "lock" : "follow aim")
               + " | Shift+wheel: distance | Arrows + Page Up/Down: move | G: ground"
         );
      } else {
         String message = mode.init_aR.isReadyToConfirm() ? "Move and look normally. E: confirm | Esc: cancel" : "Updating preview. Move and look normally. Esc: cancel";
         String detail = "Wheel / R: rotate | Shift+wheel: distance | Right-click: "
            + (mode.load_Z ? "lock" : "follow aim")
            + " | Arrows + Page Up/Down: move | M: mirror | G: ground"
            + (mode.load_Z ? " | Lock to compare world blocks" : "");
         return new aP.Inner_DataUtil(message, detail);
      }
   }

   public static autismclient.gui.f.NetHelper init_NetHelper_GuiGraphicsExtract(GuiGraphicsExtractor graphics) {
      autismclient.gui.f.SyncManager ui = autismclient.gui.f.UiHelper.init(graphics, Minecraft.getInstance().font, 0, 0);
      aP.Inner_DataUtil text = init_aP_DataUtil();
      int targetWidth = init_aP.init_aR instanceof AiNavigationSelection
         ? Math.max(ui.init_PoolEntry().measureWidth(text.message()), ui.init_PoolEntry().measureWidth(text.detail())) + 24
         : 380;
      int width = Math.max(1, Math.min(targetWidth, ui.init_I() - 16));
      return autismclient.gui.f.NetHelper.init(8, 8, width, autismclient.gui.ui.widgets.StatusToastCardRenderer.init(ui, width, text.message(), text.detail()));
   }

   public static void init_V_GuiGraphicsExtract(GuiGraphicsExtractor graphics) {
      aP mode = init_aP;
      if (mode != null) {
         autismclient.gui.f.SyncManager ui = autismclient.gui.f.UiHelper.init(graphics, Minecraft.getInstance().font, 0, 0);
         aP.Inner_DataUtil text = init_aP_DataUtil();
         if (mode.init_aR instanceof AiNavigationSelection navigation) {
            autismclient.modules.AiCheckpointHud.render(ui, navigation.checkpoints());
         }

         autismclient.gui.ui.widgets.StatusToastCardRenderer.init(
            ui,
            init_NetHelper_GuiGraphicsExtract(graphics),
            mode.init_aR instanceof AiNavigationSelection navigation
               ? (navigation.isParkour() ? "Parkour route" : "Pick destination")
               : (mode.init_aR instanceof SchematicSelection ? "Select area" : (mode.init_aR instanceof AiFarmPlacement ? "Farm area" : "Schematic placement")),
            text.message(),
            text.detail()
         );
      }
   }

   static JsonObject init_JsonObject() {
      aP mode = init_aP;
      if (mode == null) {
         return new JsonObject();
      }

      if (mode.init_aR instanceof AiFarmPlacement farm) {
         if (mode.load_I == 0) {
            mode.init(1, 0, 0);
         } else {
            mode.init(2, 0, 1);
         }

         Minecraft mc = Minecraft.getInstance();
         AutismKeyboardHandlerAccessor keyboard = (AutismKeyboardHandlerAccessor)mc.keyboardHandler;
         long window = mc.getWindow().handle();
         keyboard.autism$invokeKeyPress(window, 0, Compat263.newGlfwKeyEvent(69, 0, 0));
         keyboard.autism$invokeKeyPress(window, 1, Compat263.newGlfwKeyEvent(69, 0, 0));
         keyboard.autism$invokeKeyPress(window, 0, Compat263.newGlfwKeyEvent(69, 0, 0));
         mode.load_I++;
         return AiJson.objectOf("confirmation", mode.load_I, "active", init_Z(), "report", farm.statusJson());
      } else {
         Minecraft mc = Minecraft.getInstance();
         if (mode.init_JsonObject != null) {
            if (!mode.write) {
               autismclient.util.KeyMappingControl.of(mc.options.keyUp).autism$simulatePress(false);
               mode.init_JsonObject.addProperty("movementControl", mc.player.position().distanceTo(mode.init_Vec3) > 0.005);
               mode.write = true;
            }

            if (!mode.init_aR.isReadyToConfirm()) {
               return AiJson.objectOf("pending", true);
            }

            AutismKeyboardHandlerAccessor keyboard = (AutismKeyboardHandlerAccessor)mc.keyboardHandler;
            long window = mc.getWindow().handle();
            keyboard.autism$invokeKeyPress(window, 0, Compat263.newGlfwKeyEvent(69, 0, 0));
            keyboard.autism$invokeKeyPress(window, 1, Compat263.newGlfwKeyEvent(69, 0, 0));
            boolean bindsHeld = load_Z();
            keyboard.autism$invokeKeyPress(window, 0, Compat263.newGlfwKeyEvent(69, 0, 0));
            mode.init_JsonObject.addProperty("confirmed", !init_Z());
            mode.init_JsonObject.addProperty("confirmOwnsBindings", bindsHeld);
            return mode.init_JsonObject;
         } else {
            Module aura = autismclient.modules.al.init_Module_Str("kill-aura");
            if (aura != null) {
               mode.init_aP_StreamUtil = aura.borrowEnabled(true);
            }

            HitResult previousHit = mc.hitResult;
            BlockPos floor = mc.player.blockPosition().below();
            BlockHitResult hit = new BlockHitResult(Vec3.atCenterOf(floor), Direction.UP, floor, false);

            boolean attackBlocked;
            try {
               mc.hitResult = hit;
               attackBlocked = !((AutismMinecraftAccessor)mc).autism$startAttack();
               autismclient.util.ClientInteractionOverride.queueAttackMiss("kill-aura", hit, true);
               autismclient.util.ClientInteractionOverride.applyBeforeInputTick();
               attackBlocked &= !autismclient.util.ClientInteractionOverride.ownsAttackInput() && !((AutismMultiPlayerGameModeAccessor)mc.gameMode).autism$isDestroying();
            } finally {
               mc.hitResult = previousHit;
            }

            BlockPos origin = mode.init_aR.getOrigin();
            mode.init(1, 0, 0);
            boolean moved = !origin.equals(mode.init_aR.getOrigin());
            int rotation = mode.init_aR.getRotation();
            mode.init_aR.rotate(-1);
            boolean rotated = mode.init_aR.getRotation() == AiPlacementControls.rotateBy(rotation, -1);
            origin = mode.init_aR.getOrigin();
            mode.init_aR.init(8.0, 0.5, 0.5, 1.0, 1.0);
            boolean depth = !origin.equals(mode.init_aR.getOrigin());
            float yaw = mc.player.getYRot();
            float pitch = mc.player.getXRot();
            mc.player.turn(12.0, -3.0);
            mode.init_Vec3 = mc.player.position();
            autismclient.util.KeyMappingControl.of(mc.options.keyUp).autism$simulatePress(true);
            mode.init_JsonObject = AiJson.objectOf(
               "moved",
               moved,
               "rotated",
               rotated,
               "depth",
               depth,
               "killAuraEnabled",
               aura != null && aura.isEnabled(),
               "worldAttacksBlocked",
               attackBlocked,
               "cameraControl",
               yaw != mc.player.getYRot() || pitch != mc.player.getXRot(),
               "captured",
               mc.gui.screen() == null && mc.mouseHandler.isMouseGrabbed(),
               "keybindsBlocked",
               !autismclient.util.at.init(),
               "unlimitedMath",
               AiPlacementControls.adjustDistance(128.0, 1.0) == 129.0
            );
            return AiJson.objectOf("pending", true);
         }
      }
   }

   private record Inner_DataUtil(String message, String detail) {
   }
}

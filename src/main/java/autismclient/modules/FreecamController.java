package autismclient.modules;

import autismclient.ai.AiService;
import autismclient.util.AutismLiteVariant;
import autismclient.util.GridWalkNavigator;
import autismclient.util.RemoteViewController;
import java.util.Locale;
import net.minecraft.client.CameraType;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Options;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.phys.Vec3;

public final class FreecamController {
   private static final Minecraft MC = Minecraft.getInstance();
   private static final double init_D = 0.5;
   private static final double MIN_SPEED = 0.05;
   private static final double MAX_SPEED = 30.0;
   private static boolean active;
   private static Vec3 position = Vec3.ZERO;
   private static Vec3 prevPosition = Vec3.ZERO;
   private static float yaw;
   private static float pitch;
   private static float prevYaw;
   private static float prevPitch;
   private static CameraType savedCameraType = CameraType.FIRST_PERSON;
   private static boolean reloadChunksOnExit;
   private static boolean allowPlayerInteraction = true;

   private FreecamController() {
   }

   public static boolean isActive() {
      return !AutismLiteVariant.enabled() && !autismclient.e.TextFormat.init() ? false : active;
   }

   public static boolean isInteractionAllowed() {
      return allowPlayerInteraction;
   }

   public static boolean canInteractAsPlayer() {
      return isActive() && allowPlayerInteraction && !isAiControllingBody();
   }

   public static boolean isAiControllingBody() {
      if (AutismLiteVariant.enabled()) {
         return false;
      }

      AiService ai = AiService.getExistingInstance();
      return ai != null && ai.isActivelyDriving() || autismclient.ai.AiDirectOps.isDrivingInput();
   }

   public static boolean shouldSuppressKey(KeyMapping mapping) {
      if (mapping != null && MC != null && MC.options != null && isActive()) {
         Options o = MC.options;
         if (mapping != o.keyUp
            && mapping != o.keyDown
            && mapping != o.keyLeft
            && mapping != o.keyRight
            && mapping != o.keyJump
            && mapping != o.keyShift
            && mapping != o.keySprint) {
            return mapping != o.keyAttack
                  && mapping != o.keyUse
                  && mapping != o.keyPickItem
                  && mapping != o.keyDrop
                  && mapping != o.keySwapOffhand
                  && !isHotbarKey(mapping)
               ? false
               : isAiControllingBody();
         } else {
            return true;
         }
      } else {
         return false;
      }
   }

   private static boolean isHotbarKey(KeyMapping mapping) {
      for (KeyMapping hotbar : MC.options.keyHotbarSlots) {
         if (mapping == hotbar) {
            return true;
         }
      }

      return false;
   }

   public static void setInteractionAllowed(boolean enabled) {
      allowPlayerInteraction = enabled;
   }

   public static void enable(boolean shouldReloadChunks) {
      if (MC != null && MC.player != null && MC.level != null) {
         Vec3 startingPos = MC.gameRenderer.mainCamera().position();
         Entity startingView = MC.getCameraEntity() == null ? MC.player : MC.getCameraEntity();
         float startingYaw = startingView.getYRot();
         float startingPitch = startingView.getXRot();
         if (RemoteViewController.isActive()) {
            RemoteViewController.stop(false);
         }

         reloadChunksOnExit = shouldReloadChunks;
         savedCameraType = MC.options.getCameraType();
         position = startingPos;
         prevPosition = position;
         yaw = startingYaw;
         pitch = startingPitch;
         prevYaw = yaw;
         prevPitch = pitch;
         active = true;
         if (!isAiControllingBody()) {
            releaseMovementKeys();
         }

         MC.options.setCameraType(CameraType.FIRST_PERSON);
         if (reloadChunksOnExit) {
            am.init_V();
         }
      }
   }

   public static void disable() {
      if (active) {
         active = false;
         if (MC != null) {
            MC.options.setCameraType(savedCameraType);
            if (reloadChunksOnExit) {
               am.init_V();
            }
         }

         reloadChunksOnExit = false;
      }
   }

   public static void tick(double speed, double verticalSpeed) {
      if (active) {
         if (MC != null && MC.player != null && MC.level != null) {
            prevYaw = yaw;
            prevPitch = pitch;
            prevPosition = position;
            boolean forward = isPhysicallyDown(MC.options.keyUp);
            boolean backward = isPhysicallyDown(MC.options.keyDown);
            boolean right = isPhysicallyDown(MC.options.keyRight);
            boolean left = isPhysicallyDown(MC.options.keyLeft);
            boolean up = isPhysicallyDown(MC.options.keyJump);
            boolean down = isPhysicallyDown(MC.options.keyShift);
            boolean sprint = isPhysicallyDown(MC.options.keySprint);
            if (MC.gui.screen() == null) {
               double horizontal = Math.max(0.0, speed) * (sprint ? 1.0 : 0.5);
               double vertical = Math.max(0.0, verticalSpeed) * (sprint ? 1.0 : 0.5);
               Vec3 forwardVec = Vec3.directionFromRotation(0.0F, yaw);
               Vec3 rightVec = Vec3.directionFromRotation(0.0F, yaw + 90.0F);
               double dx = 0.0;
               double dy = 0.0;
               double dz = 0.0;
               boolean movedForward = false;
               boolean movedSideways = false;
               if (forward) {
                  dx += forwardVec.x * horizontal;
                  dz += forwardVec.z * horizontal;
                  movedForward = true;
               }

               if (backward) {
                  dx -= forwardVec.x * horizontal;
                  dz -= forwardVec.z * horizontal;
                  movedForward = true;
               }

               if (right) {
                  dx += rightVec.x * horizontal;
                  dz += rightVec.z * horizontal;
                  movedSideways = true;
               }

               if (left) {
                  dx -= rightVec.x * horizontal;
                  dz -= rightVec.z * horizontal;
                  movedSideways = true;
               }

               if (movedForward && movedSideways) {
                  double diagonal = 1.0 / Math.sqrt(2.0);
                  dx *= diagonal;
                  dz *= diagonal;
               }

               if (up) {
                  dy += vertical;
               }

               if (down) {
                  dy -= vertical;
               }

               position = position.add(dx, dy, dz);
            }
         } else {
            active = false;
         }
      }
   }

   public static Vec3 filterMovement(MoverType type, Vec3 movement) {
      return movement;
   }

   public static boolean handleMouseScroll(double scrollY) {
      if (!active || scrollY == 0.0) {
         return false;
      }

      if (MC != null && MC.player != null && MC.gui.screen() == null && MC.gui.overlay() == null) {
         Module freecam = al.init_Module_Str("freecam");
         if (freecam == null) {
            return false;
         }

         double step = scrollY > 0.0 ? 0.5 : -0.5;
         double speed = snapSpeed(parseDoubleOrOne(freecam.value("speed")) + step);
         double vertical = snapSpeed(parseDoubleOrOne(freecam.value("vertical-speed")) + step);
         freecam.setValueTransient("speed", formatSpeed(speed));
         freecam.setValueTransient("vertical-speed", formatSpeed(vertical));
         MC.gui.hud.setOverlayMessage(formatSpeedMessage(speed), false);
         return true;
      } else {
         return false;
      }
   }

   private static double snapSpeed(double value) {
      return clampSpeed(Math.ceil(value * 2.0) / 2.0);
   }

   private static Component formatSpeedMessage(double speed) {
      return Component.empty()
         .append(styledText("[", autismclient.util.UiColorPalette.textMuted(), false))
         .append(styledText("AUTISM", autismclient.util.UiColorPalette.accent(), true))
         .append(styledText("] ", autismclient.util.UiColorPalette.textMuted(), false))
         .append(styledText("Freecam Speed", autismclient.util.UiColorPalette.silver(), true))
         .append(styledText(": ", autismclient.util.UiColorPalette.textMuted(), false))
         .append(styledText(formatSpeed(speed), autismclient.util.UiColorPalette.warmWhite(), true));
   }

   private static MutableComponent styledText(String value, int color, boolean bold) {
      Style style = Style.EMPTY.withColor(color);
      if (bold) {
         style = style.withBold(true);
      }

      return Component.literal(value).setStyle(style);
   }

   private static double parseDoubleOrOne(String value) {
      try {
         return Double.parseDouble(value);
      } catch (NumberFormatException | NullPointerException ignored) {
         return 1.0;
      }
   }

   private static double clampSpeed(double value) {
      return Math.max(0.05, Math.min(30.0, value));
   }

   private static String formatSpeed(double value) {
      return String.format(Locale.ROOT, "%.2f", value);
   }

   public static void applyMouseTurn(double deltaYaw, double deltaPitch) {
      if (active) {
         yaw += (float)(deltaYaw * 0.15);
         pitch += (float)(deltaPitch * 0.15);
         pitch = Mth.clamp(pitch, -90.0F, 90.0F);
      }
   }

   public static double getInterpolatedX(float tickDelta) {
      return Mth.lerp(tickDelta, prevPosition.x, position.x);
   }

   public static double getInterpolatedY(float tickDelta) {
      return Mth.lerp(tickDelta, prevPosition.y, position.y);
   }

   public static double getInterpolatedZ(float tickDelta) {
      return Mth.lerp(tickDelta, prevPosition.z, position.z);
   }

   public static float getInterpolatedYaw(float tickDelta) {
      return Mth.lerp(tickDelta, prevYaw, yaw);
   }

   public static float getInterpolatedPitch(float tickDelta) {
      return Mth.lerp(tickDelta, prevPitch, pitch);
   }

   public static Vec3 getInterpolatedEyePos(float tickDelta) {
      return new Vec3(getInterpolatedX(tickDelta), getInterpolatedY(tickDelta), getInterpolatedZ(tickDelta));
   }

   public static Vec3 getInterpolatedFeetPos(float tickDelta) {
      return getInterpolatedEyePos(tickDelta).subtract(0.0, cameraEyeHeight(), 0.0);
   }

   private static double cameraEyeHeight() {
      Entity view = MC == null ? null : MC.getCameraEntity();
      return view == null ? 0.0 : view.getEyeY() - view.getY();
   }

   private static boolean isPhysicallyDown(KeyMapping mapping) {
      return mapping != null && autismclient.util.KeyMappingControl.of(mapping).autism$isActuallyDown();
   }

   private static void releaseMovementKeys() {
      if (MC != null && MC.options != null) {
         boolean released = false;

         for (KeyMapping key : new KeyMapping[]{
            MC.options.keyUp,
            MC.options.keyDown,
            MC.options.keyRight,
            MC.options.keyLeft,
            MC.options.keyJump,
            MC.options.keyShift,
            MC.options.keySprint
         }) {
            if ((key != MC.options.keyShift || !al.tick_Z()) && isPhysicallyDown(key) && key.isDown()) {
               key.setDown(false);
               released = true;
            }
         }

         if (released) {
            GridWalkNavigator.releaseHeldKeys();
         }
      }
   }
}

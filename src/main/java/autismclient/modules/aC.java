package autismclient.modules;

import autismclient.ai.cc;
import autismclient.util.AutismLiteVariant;
import autismclient.util.bA;
import autismclient.util.cd;
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

public final class aC {
   private static final Minecraft init_Minecraft = Minecraft.getInstance();
   private static final double init_D = 0.5;
   private static final double load_D = 0.05;
   private static final double read_D = 30.0;
   private static boolean init_Z;
   private static Vec3 init_Vec3 = Vec3.ZERO;
   private static Vec3 load_Vec3 = Vec3.ZERO;
   private static float init_F;
   private static float load_F;
   private static float read_F;
   private static float write;
   private static CameraType init_CameraType = CameraType.FIRST_PERSON;
   private static boolean load_Z;
   private static boolean read_Z = true;

   private aC() {
   }

   public static boolean init_Z() {
      return !AutismLiteVariant.enabled() && !autismclient.e.TextFormat.init() ? false : init_Z;
   }

   public static boolean load_Z() {
      return read_Z;
   }

   public static boolean read() {
      return init_Z() && read_Z && !write();
   }

   public static boolean write() {
      if (AutismLiteVariant.enabled()) {
         return false;
      }

      cc ai = cc.load_cc();
      return ai != null && ai.open_Z() || autismclient.ai.o.load_Z();
   }

   public static boolean init_Z_KeyMapping(KeyMapping mapping) {
      if (mapping != null && init_Minecraft != null && init_Minecraft.options != null && init_Z()) {
         Options o = init_Minecraft.options;
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
                  && !load_Z_KeyMapping(mapping)
               ? false
               : write();
         } else {
            return true;
         }
      } else {
         return false;
      }
   }

   private static boolean load_Z_KeyMapping(KeyMapping mapping) {
      for (KeyMapping hotbar : init_Minecraft.options.keyHotbarSlots) {
         if (mapping == hotbar) {
            return true;
         }
      }

      return false;
   }

   public static void init_V_Z(boolean enabled) {
      read_Z = enabled;
   }

   public static void load_V_Z(boolean shouldReloadChunks) {
      if (init_Minecraft != null && init_Minecraft.player != null && init_Minecraft.level != null) {
         Vec3 startingPos = init_Minecraft.gameRenderer.mainCamera().position();
         Entity startingView = init_Minecraft.getCameraEntity() == null ? init_Minecraft.player : init_Minecraft.getCameraEntity();
         float startingYaw = startingView.getYRot();
         float startingPitch = startingView.getXRot();
         if (cd.init_Z()) {
            cd.init(false);
         }

         load_Z = shouldReloadChunks;
         init_CameraType = init_Minecraft.options.getCameraType();
         init_Vec3 = startingPos;
         load_Vec3 = init_Vec3;
         init_F = startingYaw;
         load_F = startingPitch;
         read_F = init_F;
         write = load_F;
         init_Z = true;
         if (!write()) {
            load_V();
         }

         init_Minecraft.options.setCameraType(CameraType.FIRST_PERSON);
         if (load_Z) {
            am.init_V();
         }
      }
   }

   public static void init_V() {
      if (init_Z) {
         init_Z = false;
         if (init_Minecraft != null) {
            init_Minecraft.options.setCameraType(init_CameraType);
            if (load_Z) {
               am.init_V();
            }
         }

         load_Z = false;
      }
   }

   public static void init(double speed, double verticalSpeed) {
      if (init_Z) {
         if (init_Minecraft != null && init_Minecraft.player != null && init_Minecraft.level != null) {
            read_F = init_F;
            write = load_F;
            load_Vec3 = init_Vec3;
            boolean forward = read(init_Minecraft.options.keyUp);
            boolean backward = read(init_Minecraft.options.keyDown);
            boolean right = read(init_Minecraft.options.keyRight);
            boolean left = read(init_Minecraft.options.keyLeft);
            boolean up = read(init_Minecraft.options.keyJump);
            boolean down = read(init_Minecraft.options.keyShift);
            boolean sprint = read(init_Minecraft.options.keySprint);
            if (init_Minecraft.gui.screen() == null) {
               double horizontal = Math.max(0.0, speed) * (sprint ? 1.0 : 0.5);
               double vertical = Math.max(0.0, verticalSpeed) * (sprint ? 1.0 : 0.5);
               Vec3 forwardVec = Vec3.directionFromRotation(0.0F, init_F);
               Vec3 rightVec = Vec3.directionFromRotation(0.0F, init_F + 90.0F);
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

               init_Vec3 = init_Vec3.add(dx, dy, dz);
            }
         } else {
            init_Z = false;
         }
      }
   }

   public static Vec3 init(MoverType type, Vec3 movement) {
      return movement;
   }

   public static boolean init_Z_D(double scrollY) {
      if (!init_Z || scrollY == 0.0) {
         return false;
      }

      if (init_Minecraft != null && init_Minecraft.player != null && init_Minecraft.gui.screen() == null && init_Minecraft.gui.overlay() == null) {
         Module freecam = al.init_Module_Str("freecam");
         if (freecam == null) {
            return false;
         }

         double step = scrollY > 0.0 ? 0.5 : -0.5;
         double speed = init_D_D(init_D_Str(freecam.value("speed")) + step);
         double vertical = init_D_D(init_D_Str(freecam.value("vertical-speed")) + step);
         freecam.setValueTransient("speed", init_Str_D(speed));
         freecam.setValueTransient("vertical-speed", init_Str_D(vertical));
         init_Minecraft.gui.hud.setOverlayMessage(init_Component_D(speed), false);
         return true;
      } else {
         return false;
      }
   }

   private static double init_D_D(double value) {
      return load_D_D(Math.ceil(value * 2.0) / 2.0);
   }

   private static Component init_Component_D(double speed) {
      return Component.empty()
         .append(init("[", autismclient.util.p.a(), false))
         .append(init("AUTISM", autismclient.util.p.init(), true))
         .append(init("] ", autismclient.util.p.a(), false))
         .append(init("Freecam Speed", autismclient.util.p.pool(), true))
         .append(init(": ", autismclient.util.p.a(), false))
         .append(init(init_Str_D(speed), autismclient.util.p.index(), true));
   }

   private static MutableComponent init(String value, int color, boolean bold) {
      Style style = Style.EMPTY.withColor(color);
      if (bold) {
         style = style.withBold(true);
      }

      return Component.literal(value).setStyle(style);
   }

   private static double init_D_Str(String value) {
      try {
         return Double.parseDouble(value);
      } catch (NumberFormatException | NullPointerException ignored) {
         return 1.0;
      }
   }

   private static double load_D_D(double value) {
      return Math.max(0.05, Math.min(30.0, value));
   }

   private static String init_Str_D(double value) {
      return String.format(Locale.ROOT, "%.2f", value);
   }

   public static void load(double deltaYaw, double deltaPitch) {
      if (init_Z) {
         init_F += (float)(deltaYaw * 0.15);
         load_F += (float)(deltaPitch * 0.15);
         load_F = Mth.clamp(load_F, -90.0F, 90.0F);
      }
   }

   public static double init_D_F(float tickDelta) {
      return Mth.lerp(tickDelta, load_Vec3.x, init_Vec3.x);
   }

   public static double load_D_F(float tickDelta) {
      return Mth.lerp(tickDelta, load_Vec3.y, init_Vec3.y);
   }

   public static double read(float tickDelta) {
      return Mth.lerp(tickDelta, load_Vec3.z, init_Vec3.z);
   }

   public static float init_F_F(float tickDelta) {
      return Mth.lerp(tickDelta, read_F, init_F);
   }

   public static float load_F_F(float tickDelta) {
      return Mth.lerp(tickDelta, write, load_F);
   }

   public static Vec3 init_Vec3_F(float tickDelta) {
      return new Vec3(init_D_F(tickDelta), load_D_F(tickDelta), read(tickDelta));
   }

   public static Vec3 load_Vec3_F(float tickDelta) {
      return init_Vec3_F(tickDelta).subtract(0.0, init_D(), 0.0);
   }

   private static double init_D() {
      Entity view = init_Minecraft == null ? null : init_Minecraft.getCameraEntity();
      return view == null ? 0.0 : view.getEyeY() - view.getY();
   }

   private static boolean read(KeyMapping mapping) {
      return mapping != null && autismclient.util.aB.init(mapping).autism$isActuallyDown();
   }

   private static void load_V() {
      if (init_Minecraft != null && init_Minecraft.options != null) {
         boolean released = false;

         for (KeyMapping key : new KeyMapping[]{
            init_Minecraft.options.keyUp,
            init_Minecraft.options.keyDown,
            init_Minecraft.options.keyRight,
            init_Minecraft.options.keyLeft,
            init_Minecraft.options.keyJump,
            init_Minecraft.options.keyShift,
            init_Minecraft.options.keySprint
         }) {
            if ((key != init_Minecraft.options.keyShift || !al.tick_Z()) && read(key) && key.isDown()) {
               key.setDown(false);
               released = true;
            }
         }

         if (released) {
            bA.read();
         }
      }
   }
}

package autismclient.ai.a;

import autismclient.AutismClientAddon;
import java.util.Locale;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;

public final class AiMotionAudit {
   private static final boolean init_Z = Boolean.getBoolean("autism.ai.livetask")
      || Boolean.getBoolean("autism.ai.motionAudit")
      || Boolean.getBoolean("autism.ai.parkour")
      || Boolean.getBoolean("autism.ai.gather")
      || Boolean.getBoolean("autism.ai.combat");
   static final int init_I = 8;
   static final float init_F = 60.0F;
   private static final int read_I = 400;
   private static String init_Str;
   private static Vec3 init_Vec3;
   private static float write_F;
   private static int write_I;
   private static int update_I;
   private static int sync_I;
   private static int flush;
   private static int close;
   private static int open;
   private static int poll;
   private static int push;
   private static int pop;
   private static float update_F;
   private static float sync_F;
   private static String load_Str;
   private static String read_Str;
   private static BlockPos init_BlockPos;
   static final float load_F = 25.0F;
   static final float read_F = 10.0F;
   static final int load_I = 12;
   private static final float[] init_FArr = new float[13];
   private static final float[] load_FArr = new float[13];
   private static final String[] init_StrArr = new String[13];
   private static int tick;
   private static int run;
   private static int get;
   private static String write_Str = "";
   private static final boolean load_Z = Boolean.getBoolean("autism.navigation.trace");
   private static int set = -2147483648;
   private static int put = -2147483648;
   private static Object init_Obj;

   public static String init_Str() {
      return write_Str == null ? "" : write_Str;
   }

   public static void init_V(String what) {
      if (init_Z) {
         write_Str = what;
      }
   }

   private AiMotionAudit() {
   }

   public static boolean init_Z() {
      return init_Z;
   }

   public static void init(Minecraft mc) {
      if (init_Z && mc.player != null) {
         set = mc.player.tickCount;
      }
   }

   public static void init(Minecraft mc, String id, String activity) {
      if (init_Z && mc.player != null && (mc.player.tickCount != put || mc.player != init_Obj)) {
         put = mc.player.tickCount;
         init_Obj = mc.player;
         if (!id.equals(init_Str)) {
            init_V();
            init_Str = id;
         }

         LocalPlayer player = mc.player;
         Vec3 pos = player.position();
         float yaw = player.getYRot();
         if (init_Vec3 == null) {
            init_Vec3 = pos;
            write_F = yaw;
         } else {
            write_I++;
            float turn = Mth.wrapDegrees(yaw - write_F);
            if (Math.abs(turn) > 60.0F) {
               open++;
               sync_F = Math.max(sync_F, Math.abs(turn));
               load(String.format(Locale.ROOT, "snap %.0f deg at %s during '%s'", turn, player.blockPosition().toShortString(), init_Str(activity)));
            }

            init_V_F_F(yaw, player.getXRot());
            if (Math.abs(turn) > 15.0F) {
               if (update_F != 0.0F && Math.signum(turn) != Math.signum(update_F) && write_I - pop <= 6) {
                  poll++;
                  load(
                     String.format(
                        Locale.ROOT,
                        "turn reversal %.0f then %.0f deg at %s during '%s'",
                        update_F,
                        turn,
                        player.blockPosition().toShortString(),
                        init_Str(activity)
                     )
                  );
               }

               update_F = turn;
               pop = write_I;
            }

            boolean busy = mc.gui.screen() != null
               || player.isUsingItem()
               || mc.gameMode != null && mc.gameMode.isDestroying()
               || player.isSleeping()
               || player.tickCount - set <= 1;
            boolean still = pos.distanceToSqr(init_Vec3) < 1.0E-4;
            if (still && !busy) {
               if (update_I == 0) {
                  load_Str = activity;
                  init_BlockPos = player.blockPosition();
               }

               update_I++;
               sync_I++;
            } else {
               load();
            }

            if (load_Z) {
               AutismClientAddon.LOG
                  .info(
                     String.format(
                        Locale.ROOT,
                        "[AI] cam %d yaw %.1f pitch %.1f at %.2f %.2f %.2f keys %s%s%s%s%s body %s%s%s%s%s dig %s | %s%s",
                        write_I,
                        yaw,
                        player.getXRot(),
                        pos.x,
                        pos.y,
                        pos.z,
                        mc.options.keyUp.isDown() ? "W" : "",
                        mc.options.keyLeft.isDown() ? "A" : "",
                        mc.options.keyDown.isDown() ? "S" : "",
                        mc.options.keyRight.isDown() ? "D" : "",
                        mc.options.keyJump.isDown() ? "J" : "",
                        player.onGround() ? "g" : "-",
                        player.isSprinting() ? "s" : "-",
                        player.horizontalCollision ? "h" : "-",
                        player.verticalCollision && !player.verticalCollisionBelow ? "c" : "-",
                        player.onClimbable() ? "l" : "-",
                        AiDecisionAudit.init_Z(mc) ? "on" : "-",
                        init_Str_Str_I(activity, 90),
                        write_Str.isEmpty() ? "" : " | " + write_Str
                     )
                  );
            }

            init_Vec3 = pos;
            write_F = yaw;
            AiDecisionAudit.init(mc, init_Str_Str_I(activity, 90) + (write_Str.isEmpty() ? "" : " | " + write_Str));
            write_Str = "";
         }
      }
   }

   public static void init_V() {
      if (init_Z && init_Str != null) {
         load();
         AiDecisionAudit.init(init_Str);
         if (write_I > 0) {
            AutismClientAddon.LOG
               .info(
                  String.format(
                     Locale.ROOT,
                     "[AI] motion %s: %.1f s, still %.1f s in %d pauses (longest %.1f s during '%s'), snaps %d (worst %.0f deg), reversals %d, there-and-backs %d",
                     init_Str,
                     write_I / 20.0,
                     sync_I / 20.0,
                     flush,
                     close / 20.0,
                     read_Str == null ? "-" : init_Str(read_Str),
                     open,
                     sync_F,
                     poll,
                     get
                  )
               );
         }

         init_Str = null;
         init_Vec3 = null;
         pop = 0;
         push = 0;
         poll = 0;
         open = 0;
         close = 0;
         flush = 0;
         sync_I = 0;
         update_I = 0;
         write_I = 0;
         get = 0;
         run = 0;
         tick = 0;
         sync_F = 0.0F;
         update_F = 0.0F;
         read_Str = null;
         load_Str = null;
      }
   }

   private static void init_V_F_F(float yaw, float pitch) {
      int n = init_FArr.length;
      int[] flip = AiDecisionAudit.auditFlip(init_FArr, load_FArr, run, tick, 12, 10.0F, 25.0F, yaw, pitch);
      if (flip != null) {
         int k = flip[0];
         int then = flip[1];
         int peak = flip[2];
         if (init_StrArr[peak] == null || !init_StrArr[peak].startsWith("cap ")) {
            get++;
            load(
               String.format(
                  Locale.ROOT,
                  "there-and-back %.0f deg over %d ticks (yaw %.0f pitch %.0f -> yaw %.0f pitch %.0f -> back) out during '%s', back during '%s'",
                  init(init_FArr[peak], load_FArr[peak], init_FArr[then], load_FArr[then]),
                  k,
                  init_FArr[then],
                  load_FArr[then],
                  init_FArr[peak],
                  load_FArr[peak],
                  init_StrArr[peak],
                  write_Str
               )
            );
         }

         tick = 0;
      }

      init_FArr[run] = yaw;
      load_FArr[run] = pitch;
      init_StrArr[run] = write_Str;
      run = (run + 1) % n;
      tick = Math.min(tick + 1, n);
   }

   private static float init(float yawA, float pitchA, float yawB, float pitchB) {
      float dy = Mth.wrapDegrees(yawA - yawB);
      float dp = pitchA - pitchB;
      return (float)Math.sqrt(dy * dy + dp * dp);
   }

   private static void load() {
      if (update_I >= 8) {
         flush++;
         if (update_I > close) {
            close = update_I;
            read_Str = load_Str;
         }

         load(String.format(Locale.ROOT, "pause %.2f s at %s during '%s'", update_I / 20.0, init_BlockPos.toShortString(), init_Str(load_Str)));
      }

      update_I = 0;
   }

   private static void load(String line) {
      if (++push <= 400) {
         AutismClientAddon.LOG.info("[AI] motion " + line);
      }
   }

   private static String init_Str(String s) {
      return init_Str_Str_I(s, 160);
   }

   private static String init_Str_Str_I(String s, int max) {
      if (s == null) {
         return "";
      } else {
         return s.length() > max ? s.substring(0, max) + "..." : s;
      }
   }
}

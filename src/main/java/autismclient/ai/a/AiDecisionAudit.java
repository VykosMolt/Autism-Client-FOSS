package autismclient.ai.a;

import autismclient.AutismClientAddon;
import autismclient.mixin.accessor.AutismMultiPlayerGameModeAccessor;
import java.util.ArrayDeque;
import java.util.LinkedHashSet;
import java.util.Locale;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.HitResult.Type;

public final class AiDecisionAudit {
   private static final int read_I = 10;
   private static final int write_I = 4;
   private static final int update_I = 10;
   private static final int sync_I = 60;
   private static final int flush = 600;
   private static BlockPos init_BlockPos;
   private static BlockState init_BlockState;
   private static float read_F;
   private static float write_F;
   private static int close;
   private static String init_Str = "";
   private static BlockPos load_BlockPos;
   private static int open;
   private static String load_Str = "";
   private static float update_F;
   private static float sync_F;
   private static Vec3 init_Vec3;
   private static final ArrayDeque<AiDecisionAudit.Inner_DataUtil> init_ArrayDeque = new ArrayDeque<>();
   private static int poll;
   private static int push = -1000;
   private static int pop;
   private static int tick;
   private static int run;
   private static int get;
   private static int set;
   private static int put;
   private static int add;
   static final int init_I = 7;
   private static int clear = -1;
   private static String read_Str = "";
   private static final LinkedHashSet<String> init_LinkedHashSet = new LinkedHashSet<>();
   private static int reset;
   private static int start;
   static final float init_F = 2.5F;
   static final float load_F = 1.0F;
   static final int load_I = 10;
   private static final float[] init_FArr = new float[11];
   private static final float[] load_FArr = new float[11];
   private static final String[] init_StrArr = new String[11];
   private static int stop;
   private static int next;
   private static int prev;
   public static volatile AiDecisionAudit.Inner_StreamUtil init_RenderState_Stream = new AiDecisionAudit.Inner_StreamUtil(0, 0, 0, 0, 0, 0, 0);

   public static boolean init_Z(Minecraft mc) {
      if (!(mc.gameMode instanceof AutismMultiPlayerGameModeAccessor dig && mc.level != null)) {
         return false;
      } else {
         if (dig.autism$getDestroyDelay() > 0) {
            return true;
         }

         BlockPos pos = dig.autism$getDestroyBlockPos();
         return pos != null && dig.autism$getDestroyProgress() > 0.0F && !mc.level.getBlockState(pos).isAir();
      }
   }

   private AiDecisionAudit() {
   }

   static void init(Minecraft mc, String step) {
      LocalPlayer player = mc.player;
      if (player != null && mc.level != null) {
         poll++;
         load(mc, step);
         init(player.getYRot(), player.getXRot(), step);
         read(mc, step);
         init(player.position(), step);
         update_F = player.getYRot();
         sync_F = player.getXRot();
         init_Vec3 = player.position();
      }
   }

   private static void load(Minecraft mc, String step) {
      BlockPos now = null;
      float progress = 0.0F;
      if (mc.gameMode instanceof AutismMultiPlayerGameModeAccessor dig
         && dig.autism$getDestroyBlockPos() != null
         && (dig.autism$isDestroying() || dig.autism$getDestroyProgress() > 0.0F)
         && !mc.level.getBlockState(dig.autism$getDestroyBlockPos()).isAir()) {
         now = dig.autism$getDestroyBlockPos().immutable();
         progress = dig.autism$getDestroyProgress();
      }

      if (init_BlockPos != null) {
         BlockState there = mc.level.getBlockState(init_BlockPos);
         boolean gone = there.isAir() || there.getBlock() != init_BlockState.getBlock();
         if (gone) {
            pop++;
            load(String.format(Locale.ROOT, "break %s at %s in %d ticks during '%s'", init(init_BlockState), init_BlockPos.toShortString(), close, init_Str));
            clear = poll;
            read_Str = init(init_BlockState) + " " + init_BlockPos.toShortString();
            init_LinkedHashSet.clear();
            init_BlockPos = null;
         } else if (now == null || !now.equals(init_BlockPos)) {
            if (read_F >= 0.05F) {
               tick++;
               run = run + close;
               load(
                  String.format(
                     Locale.ROOT,
                     "abandoned %s at %s at %.0f%% after %d ticks during '%s', now '%s'%s",
                     init(init_BlockState),
                     init_BlockPos.toShortString(),
                     read_F * 100.0F,
                     close,
                     init_Str,
                     step,
                     now == null ? "" : " digging " + now.toShortString()
                  )
               );
            }

            init_BlockPos = null;
         }
      }

      if (now == null && clear >= 0) {
         if (poll - clear > 200) {
            clear = -1;
         } else if (init_LinkedHashSet.size() < 5) {
            init_LinkedHashSet.add(step.length() > 70 ? step.substring(0, 70) : step);
         }
      }

      if (now != null) {
         if (init_BlockPos == null && clear >= 0) {
            int gap = poll - clear;
            if (gap - 7 >= 3) {
               reset++;
               start += gap - 7;
               load(
                  String.format(
                     Locale.ROOT,
                     "gap %d ticks (%d over the floor): %s -> %s %s, through %s",
                     gap,
                     gap - 7,
                     read_Str,
                     init(mc.level.getBlockState(now)),
                     now.toShortString(),
                     init_LinkedHashSet
                  )
               );
            }

            clear = -1;
         }

         if (init_BlockPos == null) {
            init_BlockPos = now;
            init_BlockState = mc.level.getBlockState(now);
            read_F = 0.0F;
            close = 0;
            init_Str = step;
         }

         if (progress != write_F || close == 0) {
            close++;
         }

         write_F = progress;
         read_F = Math.max(read_F, progress);
      }
   }

   private static void init(float yaw, float pitch, String step) {
      int n = init_FArr.length;
      int[] flip = auditFlip(init_FArr, load_FArr, next, stop, 10, 1.0F, 2.5F, yaw, pitch);
      if (flip != null) {
         int k = flip[0];
         int then = flip[1];
         int peak = flip[2];
         String during = init_StrArr[peak] == null ? "" : init_StrArr[peak];
         String lower = during.toLowerCase(Locale.ROOT);
         if (!lower.contains("bridge") && !lower.contains("telly") && !lower.contains("| cap ")) {
            prev++;
            load(
               String.format(
                  Locale.ROOT,
                  "flip %.1f deg over %d ticks (yaw %.1f pitch %.1f -> yaw %.1f pitch %.1f -> back) out during '%s', back during '%s'",
                  init(init_FArr[peak], load_FArr[peak], init_FArr[then], load_FArr[then]),
                  k,
                  init_FArr[then],
                  load_FArr[then],
                  init_FArr[peak],
                  load_FArr[peak],
                  during,
                  step
               )
            );
         }

         stop = 0;
      }

      init_FArr[next] = yaw;
      load_FArr[next] = pitch;
      init_StrArr[next] = step;
      next = (next + 1) % n;
      stop = Math.min(stop + 1, n);
   }

   // Newest there-and-back turn in the ring: {ticks, then, peak}, or null. Shared with TaskTimer.
   static int[] auditFlip(float[] yaws, float[] pitches, int next, int filled, int maxTicks, float backWithin, float outAtLeast, float yaw, float pitch) {
      int n = yaws.length;

      for (int k = 2; k <= Math.min(filled, maxTicks); k++) {
         int then = Math.floorMod(next - k, n);
         if (!(init(yaw, pitch, yaws[then], pitches[then]) > backWithin)) {
            float out = 0.0F;
            int peak = then;

            for (int j = 1; j < k; j++) {
               int at = Math.floorMod(next - j, n);
               float a = init(yaws[at], pitches[at], yaws[then], pitches[then]);
               if (a > out) {
                  out = a;
                  peak = at;
               }
            }

            if (!(out < outAtLeast)) {
               return new int[]{k, then, peak};
            }
         }
      }

      return null;
   }

   private static float init(float yawA, float pitchA, float yawB, float pitchB) {
      float dy = Mth.wrapDegrees(yawA - yawB);
      float dp = pitchA - pitchB;
      return (float)Math.sqrt(dy * dy + dp * dp);
   }

   private static void read(Minecraft mc, String step) {
      LocalPlayer player = mc.player;
      boolean still = init_Vec3 != null
         && player.position().distanceToSqr(init_Vec3) < 1.0E-4
         && Math.abs(Mth.wrapDegrees(player.getYRot() - update_F)) < 1.0F
         && Math.abs(player.getXRot() - sync_F) < 1.0F;
      BlockPos looked = null;
      if (mc.hitResult instanceof BlockHitResult hit && hit.getType() == Type.BLOCK && !mc.level.getBlockState(hit.getBlockPos()).isAir()) {
         looked = hit.getBlockPos().immutable();
      }

      boolean digging = init_BlockPos != null
         || mc.gameMode != null && mc.gameMode.isDestroying()
         || mc.gameMode instanceof AutismMultiPlayerGameModeAccessor dig && dig.autism$getDestroyDelay() > 0;
      if (still && looked != null && !digging && mc.gui.screen() == null) {
         if (load_BlockPos == null || !load_BlockPos.equals(looked)) {
            init_V(mc);
            load_BlockPos = looked;
            load_Str = step;
         }

         open++;
      } else {
         init_V(mc);
      }
   }

   private static void init_V(Minecraft mc) {
      if (load_BlockPos != null && open >= 10) {
         get++;
         set = set + open;
         load(
            String.format(
               Locale.ROOT,
               "stare %s at %s for %d ticks during '%s'",
               mc.level == null ? "?" : init(mc.level.getBlockState(load_BlockPos)),
               load_BlockPos.toShortString(),
               open,
               load_Str
            )
         );
      }

      load_BlockPos = null;
      open = 0;
   }

   private static void init(Vec3 pos, String step) {
      if (poll % 10 == 0) {
         init_ArrayDeque.addLast(new AiDecisionAudit.Inner_DataUtil(pos, poll, step));

         while (init_ArrayDeque.size() > 60) {
            init_ArrayDeque.removeFirst();
         }

         if (poll - push >= 100) {
            AiDecisionAudit.Inner_DataUtil[] all = init_ArrayDeque.toArray(new AiDecisionAudit.Inner_DataUtil[0]);

            for (int i = 0; i < all.length - 4; i++) {
               AiDecisionAudit.Inner_DataUtil start = all[i];
               if (!(start.pos.distanceToSqr(pos) > 2.25)) {
                  double far = 0.0;
                  AiDecisionAudit.Inner_DataUtil peak = start;

                  for (int j = i + 1; j < all.length - 1; j++) {
                     double d = Math.sqrt(all[j].pos.distanceToSqr(start.pos));
                     if (d > far) {
                        far = d;
                        peak = all[j];
                     }
                  }

                  if (!(far < 4.0)) {
                     put++;
                     push = poll;
                     load(
                        String.format(
                           Locale.ROOT,
                           "backtrack: back at %s after %.1f blocks away (to %s) over %.1f s; went out during '%s', came back during '%s'",
                           BlockPos.containing(start.pos).toShortString(),
                           far,
                           BlockPos.containing(peak.pos).toShortString(),
                           (poll - start.tick) / 20.0,
                           peak.step,
                           step
                        )
                     );
                     init_ArrayDeque.clear();
                     return;
                  }
               }
            }
         }
      }
   }

   static void init(String op) {
      if (poll > 0) {
         AutismClientAddon.LOG
            .info(
               String.format(
                  Locale.ROOT,
                  "[AI] decide %s: %d breaks, %d abandoned digs (%.1f s), %d stares (%.1f s), %d backtracks, %d long gaps (%.1f s over the floor), %d flips",
                  op,
                  pop,
                  tick,
                  run / 20.0,
                  get,
                  set / 20.0,
                  put,
                  reset,
                  start / 20.0,
                  prev
               )
            );
         init_RenderState_Stream = new AiDecisionAudit.Inner_StreamUtil(pop, tick, get, put, reset, start, prev);
      }

      init_BlockPos = null;
      load_BlockPos = null;
      open = 0;
      init_ArrayDeque.clear();
      init_Vec3 = null;
      add = 0;
      put = 0;
      set = 0;
      get = 0;
      run = 0;
      tick = 0;
      pop = 0;
      poll = 0;
      stop = 0;
      prev = 0;
      start = 0;
      reset = 0;
      clear = -1;
      init_LinkedHashSet.clear();
      push = -1000;
   }

   private static String init(BlockState state) {
      return state == null ? "?" : BuiltInRegistries.BLOCK.getKey(state.getBlock()).getPath();
   }

   private static void load(String line) {
      if (++add <= 600) {
         AutismClientAddon.LOG.info("[AI] decide " + line);
      }
   }

   private record Inner_DataUtil(Vec3 pos, int tick, String step) {
   }

   public record Inner_StreamUtil(int breaks, int abandoned, int stares, int backtracks, int longGaps, int gapExcessTicks, int flips) {
   }
}

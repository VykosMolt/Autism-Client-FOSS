package autismclient.ai.engine;

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
   private static BlockPos breakPos;
   private static BlockState breakState;
   private static float maxProgress;
   private static float lastProgress;
   private static int breakTicks;
   private static String breakStep = "";
   private static BlockPos lookPos;
   private static int stareTicks;
   private static String lookStep = "";
   private static float startYaw;
   private static float startPitch;
   private static Vec3 startPosition;
   private static final ArrayDeque<AiDecisionAudit.Inner_DataUtil> init_ArrayDeque = new ArrayDeque<>();
   private static int tickCount;
   private static int lastBacktrackTick = -1000;
   private static int breaks;
   private static int abandonedDigs;
   private static int abandonedDigTicks;
   private static int stares;
   private static int stareTotalTicks;
   private static int backtracks;
   private static int loggedLines;
   static final int init_I = 7;
   private static int lastBreakTick = -1;
   private static String read_Str = "";
   private static final LinkedHashSet<String> init_LinkedHashSet = new LinkedHashSet<>();
   private static int longGaps;
   private static int longGapTicks;
   static final float init_F = 2.5F;
   static final float load_F = 1.0F;
   static final int load_I = 10;
   private static final float[] init_FArr = new float[11];
   private static final float[] load_FArr = new float[11];
   private static final String[] init_StrArr = new String[11];
   private static int ringFilled;
   private static int ringNext;
   private static int flips;
   public static volatile AiDecisionAudit.Inner_StreamUtil init_RenderState_Stream = new AiDecisionAudit.Inner_StreamUtil(0, 0, 0, 0, 0, 0, 0);

   public static boolean isDigging(Minecraft mc) {
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
         tickCount++;
         auditBreaking(mc, step);
         auditFlips(player.getYRot(), player.getXRot(), step);
         auditStare(mc, step);
         auditBacktrack(player.position(), step);
         startYaw = player.getYRot();
         startPitch = player.getXRot();
         startPosition = player.position();
      }
   }

   private static void auditBreaking(Minecraft mc, String step) {
      BlockPos now = null;
      float progress = 0.0F;
      if (mc.gameMode instanceof AutismMultiPlayerGameModeAccessor dig
         && dig.autism$getDestroyBlockPos() != null
         && (dig.autism$isDestroying() || dig.autism$getDestroyProgress() > 0.0F)
         && !mc.level.getBlockState(dig.autism$getDestroyBlockPos()).isAir()) {
         now = dig.autism$getDestroyBlockPos().immutable();
         progress = dig.autism$getDestroyProgress();
      }

      if (breakPos != null) {
         BlockState there = mc.level.getBlockState(breakPos);
         boolean gone = there.isAir() || there.getBlock() != breakState.getBlock();
         if (gone) {
            breaks++;
            log(String.format(Locale.ROOT, "break %s at %s in %d ticks during '%s'", blockName(breakState), breakPos.toShortString(), breakTicks, breakStep));
            lastBreakTick = tickCount;
            read_Str = blockName(breakState) + " " + breakPos.toShortString();
            init_LinkedHashSet.clear();
            breakPos = null;
         } else if (now == null || !now.equals(breakPos)) {
            if (maxProgress >= 0.05F) {
               abandonedDigs++;
               abandonedDigTicks = abandonedDigTicks + breakTicks;
               log(
                  String.format(
                     Locale.ROOT,
                     "abandoned %s at %s at %.0f%% after %d ticks during '%s', now '%s'%s",
                     blockName(breakState),
                     breakPos.toShortString(),
                     maxProgress * 100.0F,
                     breakTicks,
                     breakStep,
                     step,
                     now == null ? "" : " digging " + now.toShortString()
                  )
               );
            }

            breakPos = null;
         }
      }

      if (now == null && lastBreakTick >= 0) {
         if (tickCount - lastBreakTick > 200) {
            lastBreakTick = -1;
         } else if (init_LinkedHashSet.size() < 5) {
            init_LinkedHashSet.add(step.length() > 70 ? step.substring(0, 70) : step);
         }
      }

      if (now != null) {
         if (breakPos == null && lastBreakTick >= 0) {
            int gap = tickCount - lastBreakTick;
            if (gap - 7 >= 3) {
               longGaps++;
               longGapTicks += gap - 7;
               log(
                  String.format(
                     Locale.ROOT,
                     "gap %d ticks (%d over the floor): %s -> %s %s, through %s",
                     gap,
                     gap - 7,
                     read_Str,
                     blockName(mc.level.getBlockState(now)),
                     now.toShortString(),
                     init_LinkedHashSet
                  )
               );
            }

            lastBreakTick = -1;
         }

         if (breakPos == null) {
            breakPos = now;
            breakState = mc.level.getBlockState(now);
            maxProgress = 0.0F;
            breakTicks = 0;
            breakStep = step;
         }

         if (progress != lastProgress || breakTicks == 0) {
            breakTicks++;
         }

         lastProgress = progress;
         maxProgress = Math.max(maxProgress, progress);
      }
   }

   private static void auditFlips(float yaw, float pitch, String step) {
      int n = init_FArr.length;
      int[] flip = auditFlip(init_FArr, load_FArr, ringNext, ringFilled, 10, 1.0F, 2.5F, yaw, pitch);
      if (flip != null) {
         int k = flip[0];
         int then = flip[1];
         int peak = flip[2];
         String during = init_StrArr[peak] == null ? "" : init_StrArr[peak];
         String lower = during.toLowerCase(Locale.ROOT);
         if (!lower.contains("bridge") && !lower.contains("telly") && !lower.contains("| cap ")) {
            flips++;
            log(
               String.format(
                  Locale.ROOT,
                  "flip %.1f deg over %d ticks (yaw %.1f pitch %.1f -> yaw %.1f pitch %.1f -> back) out during '%s', back during '%s'",
                  angleBetween(init_FArr[peak], load_FArr[peak], init_FArr[then], load_FArr[then]),
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

         ringFilled = 0;
      }

      init_FArr[ringNext] = yaw;
      load_FArr[ringNext] = pitch;
      init_StrArr[ringNext] = step;
      ringNext = (ringNext + 1) % n;
      ringFilled = Math.min(ringFilled + 1, n);
   }

   // Newest there-and-back turn in the ring: {ticks, then, peak}, or null. Shared with TaskTimer.
   static int[] auditFlip(float[] yaws, float[] pitches, int next, int filled, int maxTicks, float backWithin, float outAtLeast, float yaw, float pitch) {
      int n = yaws.length;

      for (int k = 2; k <= Math.min(filled, maxTicks); k++) {
         int then = Math.floorMod(next - k, n);
         if (!(angleBetween(yaw, pitch, yaws[then], pitches[then]) > backWithin)) {
            float out = 0.0F;
            int peak = then;

            for (int j = 1; j < k; j++) {
               int at = Math.floorMod(next - j, n);
               float a = angleBetween(yaws[at], pitches[at], yaws[then], pitches[then]);
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

   private static float angleBetween(float yawA, float pitchA, float yawB, float pitchB) {
      float dy = Mth.wrapDegrees(yawA - yawB);
      float dp = pitchA - pitchB;
      return (float)Math.sqrt(dy * dy + dp * dp);
   }

   private static void auditStare(Minecraft mc, String step) {
      LocalPlayer player = mc.player;
      boolean still = startPosition != null
         && player.position().distanceToSqr(startPosition) < 1.0E-4
         && Math.abs(Mth.wrapDegrees(player.getYRot() - startYaw)) < 1.0F
         && Math.abs(player.getXRot() - startPitch) < 1.0F;
      BlockPos looked = null;
      if (mc.hitResult instanceof BlockHitResult hit && hit.getType() == Type.BLOCK && !mc.level.getBlockState(hit.getBlockPos()).isAir()) {
         looked = hit.getBlockPos().immutable();
      }

      boolean digging = breakPos != null
         || mc.gameMode != null && mc.gameMode.isDestroying()
         || mc.gameMode instanceof AutismMultiPlayerGameModeAccessor dig && dig.autism$getDestroyDelay() > 0;
      if (still && looked != null && !digging && mc.gui.screen() == null) {
         if (lookPos == null || !lookPos.equals(looked)) {
            init_V(mc);
            lookPos = looked;
            lookStep = step;
         }

         stareTicks++;
      } else {
         init_V(mc);
      }
   }

   private static void init_V(Minecraft mc) {
      if (lookPos != null && stareTicks >= 10) {
         stares++;
         stareTotalTicks = stareTotalTicks + stareTicks;
         log(
            String.format(
               Locale.ROOT,
               "stare %s at %s for %d ticks during '%s'",
               mc.level == null ? "?" : blockName(mc.level.getBlockState(lookPos)),
               lookPos.toShortString(),
               stareTicks,
               lookStep
            )
         );
      }

      lookPos = null;
      stareTicks = 0;
   }

   private static void auditBacktrack(Vec3 pos, String step) {
      if (tickCount % 10 == 0) {
         init_ArrayDeque.addLast(new AiDecisionAudit.Inner_DataUtil(pos, tickCount, step));

         while (init_ArrayDeque.size() > 60) {
            init_ArrayDeque.removeFirst();
         }

         if (tickCount - lastBacktrackTick >= 100) {
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
                     backtracks++;
                     lastBacktrackTick = tickCount;
                     log(
                        String.format(
                           Locale.ROOT,
                           "backtrack: back at %s after %.1f blocks away (to %s) over %.1f s; went out during '%s', came back during '%s'",
                           BlockPos.containing(start.pos).toShortString(),
                           far,
                           BlockPos.containing(peak.pos).toShortString(),
                           (tickCount - start.tick) / 20.0,
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
      if (tickCount > 0) {
         AutismClientAddon.LOG
            .info(
               String.format(
                  Locale.ROOT,
                  "[AI] decide %s: %d breaks, %d abandoned digs (%.1f s), %d stares (%.1f s), %d backtracks, %d long gaps (%.1f s over the floor), %d flips",
                  op,
                  breaks,
                  abandonedDigs,
                  abandonedDigTicks / 20.0,
                  stares,
                  stareTotalTicks / 20.0,
                  backtracks,
                  longGaps,
                  longGapTicks / 20.0,
                  flips
               )
            );
         init_RenderState_Stream = new AiDecisionAudit.Inner_StreamUtil(breaks, abandonedDigs, stares, backtracks, longGaps, longGapTicks, flips);
      }

      breakPos = null;
      lookPos = null;
      stareTicks = 0;
      init_ArrayDeque.clear();
      startPosition = null;
      loggedLines = 0;
      backtracks = 0;
      stareTotalTicks = 0;
      stares = 0;
      abandonedDigTicks = 0;
      abandonedDigs = 0;
      breaks = 0;
      tickCount = 0;
      ringFilled = 0;
      flips = 0;
      longGapTicks = 0;
      longGaps = 0;
      lastBreakTick = -1;
      init_LinkedHashSet.clear();
      lastBacktrackTick = -1000;
   }

   private static String blockName(BlockState state) {
      return state == null ? "?" : BuiltInRegistries.BLOCK.getKey(state.getBlock()).getPath();
   }

   private static void log(String line) {
      if (++loggedLines <= 600) {
         AutismClientAddon.LOG.info("[AI] decide " + line);
      }
   }

   private record Inner_DataUtil(Vec3 pos, int tick, String step) {
   }

   public record Inner_StreamUtil(int breaks, int abandoned, int stares, int backtracks, int longGaps, int gapExcessTicks, int flips) {
   }
}

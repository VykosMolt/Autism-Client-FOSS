package autismclient.util.g;

import autismclient.modules.aR;
import autismclient.util.AutismClientMessaging;
import autismclient.util.AutismConfig;
import autismclient.util.AutismLiteVariant;
import autismclient.util.aB;
import autismclient.util.bA;
import autismclient.util.bl;
import autismclient.util.macro.A;
import autismclient.util.macro.F;
import autismclient.util.macro.MacroExecutor;
import autismclient.util.macro.w;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket;
import net.minecraft.network.protocol.game.ServerboundMoveVehiclePacket;
import net.minecraft.network.protocol.game.ServerboundPlayerAbilitiesPacket;
import net.minecraft.network.protocol.game.ServerboundPlayerCommandPacket;
import net.minecraft.network.protocol.game.ServerboundPlayerInputPacket;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket.Pos;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket.StatusOnly;
import net.minecraft.network.protocol.game.ServerboundPlayerCommandPacket.Action;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Input;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.Vec3;

public final class PkgCls_a {
   public static final int init_I = 20;
   public static final int load_I = 500;
   public static final double init_D = 10.0;
   private static final double load_D = 4.0;
   private static final double read_D = 0.0625;
   private static final double write_D = 0.05;
   private static final long init_J = 100L;
   private static final long load_J = 600L;
   private static final long read_J = 250L;
   private static final double update_D = 0.5;
   private static final double sync_D = 0.35;
   private static final long write_J = 5000L;
   private static final long update_J = 3500L;
   private static final long sync_J = 12000L;
   private static final int read_I = 3;
   private static final long flush = 300000L;
   private static final int write_I = 80;
   private static final int update_I = 32;
   private static final ThreadLocal<Boolean> init_ThreadLocal = ThreadLocal.withInitial(() -> false);
   private static final AtomicInteger init_AtomicInteger = new AtomicInteger();
   private static final Object init_Obj = new Object();
   private static final AtomicLong init_AtomicLong = new AtomicLong();
   private static volatile PkgCls_a.Inner_ConfigStore init_a_ConfigStore;
   private static boolean init_Z;

   private PkgCls_a() {
   }

   public static PkgCls_a.Inner_StreamUtil init_a_StreamUtil_Str(String arguments, Vec3 origin, int configuredPackets, int configuredPause) {
      String trimmed = arguments == null ? "" : arguments.trim();
      if (trimmed.isEmpty()) {
         return new PkgCls_a.Inner_StreamUtil(PkgCls_a.Inner_DataUtil.sync, null, 0, 0, "");
      }

      String[] parts = trimmed.split("\\s+");
      String first = parts[0].toLowerCase(Locale.ROOT);
      if ("stop".equals(first)) {
         return parts.length == 1
            ? new PkgCls_a.Inner_StreamUtil(PkgCls_a.Inner_DataUtil.load, null, 0, 0, "")
            : PkgCls_a.Inner_StreamUtil.init("Usage: tp stop");
      }

      if ("status".equals(first)) {
         return parts.length == 1
            ? new PkgCls_a.Inner_StreamUtil(PkgCls_a.Inner_DataUtil.read, null, 0, 0, "")
            : PkgCls_a.Inner_StreamUtil.init("Usage: tp status");
      }

      if ("reset".equals(first)) {
         return parts.length == 1
            ? new PkgCls_a.Inner_StreamUtil(PkgCls_a.Inner_DataUtil.update, null, 20, 500, "")
            : PkgCls_a.Inner_StreamUtil.init("Usage: tp reset");
      }

      if ("config".equals(first)) {
         if (parts.length != 3) {
            return PkgCls_a.Inner_StreamUtil.init("Usage: tp config <maxPackets> <pauseMs>");
         } else {
            Integer packets = init_Integer_Str(parts[1], 1, 100);
            Integer pause = init_Integer_Str(parts[2], 50, 10000);
            if (packets == null) {
               return PkgCls_a.Inner_StreamUtil.init("maxPackets must be 1-100");
            } else {
               return pause == null
                  ? PkgCls_a.Inner_StreamUtil.init("pauseMs must be 50-10000")
                  : new PkgCls_a.Inner_StreamUtil(PkgCls_a.Inner_DataUtil.write, null, packets, pause, "");
            }
         }
      } else {
         int offset = 0;
         if ("fast".equals(first)) {
            offset = 1;
         }

         if (parts.length - offset >= 3 && parts.length - offset <= 6 && origin != null) {
            Double x = init_Double_Str_D(parts[offset], origin.x);
            Double y = init_Double_Str_D(parts[offset + 1], origin.y);
            Double z = init_Double_Str_D(parts[offset + 2], origin.z);
            if (x != null && y != null && z != null) {
               int packets = init_I_I(configuredPackets, 1, 100);
               int pause = init_I_I(configuredPause, 50, 10000);
               int option = offset + 3;
               if (option < parts.length && "fast".equalsIgnoreCase(parts[option])) {
                  option++;
               }

               if (option < parts.length) {
                  Integer parsed = init_Integer_Str(parts[option], 1, 100);
                  if (parsed == null) {
                     return PkgCls_a.Inner_StreamUtil.init("maxPackets must be 1-100");
                  }

                  packets = parsed;
                  option++;
               }

               if (option < parts.length) {
                  Integer parsed = init_Integer_Str(parts[option], 50, 10000);
                  if (parsed == null) {
                     return PkgCls_a.Inner_StreamUtil.init("pauseMs must be 50-10000");
                  }

                  pause = parsed;
                  option++;
               }

               return option != parts.length
                  ? PkgCls_a.Inner_StreamUtil.init("Usage: tp <x> <y> <z> [maxPackets] [pauseMs]")
                  : new PkgCls_a.Inner_StreamUtil(PkgCls_a.Inner_DataUtil.init, new Vec3(x, y, z), packets, pause, "");
            } else {
               return PkgCls_a.Inner_StreamUtil.init("Coordinates must be numbers or ~ offsets");
            }
         } else {
            return PkgCls_a.Inner_StreamUtil.init("Usage: tp <x> <y> <z> [maxPackets] [pauseMs]");
         }
      }
   }

   public static String init_Str_Str(String arguments) {
      Minecraft mc = Minecraft.getInstance();
      LocalPlayer player = mc == null ? null : mc.player;
      AutismConfig config = AutismConfig.getGlobal();
      PkgCls_a.Inner_StreamUtil request = init_a_StreamUtil_Str(
         arguments, player == null ? null : init_Vec3_LocalPlayer(player), config.tpMaxPackets, config.tpPauseMs
      );
      String common = init_Str_a_StreamUtil(request);
      if (common != null) {
         return common;
      }

      if (player == null || mc.getConnection() == null || player.level() == null) {
         return "Join a world first";
      }

      if (MacroExecutor.write_Z()) {
         return "Stop the running macro first";
      }

      Entity vehicle = player.getVehicle();
      synchronized (init_Obj) {
         init_a_ConfigStore = PkgCls_a.Inner_ConfigStore.init(player, mc.getConnection(), player.level(), vehicle, request);
      }

      init_V_LocalPlayer(player);
      return init_Str_Vec3(request.init_Vec3(), request.init_I(), request.load(), vehicle != null);
   }

   public static PkgCls_a.Inner_StateCache init_a_StateCache_w_bl_StreamUtil(w action, bl.Inner_StreamUtil handle) {
      Minecraft mc = Minecraft.getInstance();
      LocalPlayer player = mc == null ? null : mc.player;
      if (handle == null || action == null || player == null || mc.getConnection() == null || player.level() == null) {
         return new PkgCls_a.Inner_StateCache(false, "No world or connection");
      }

      Vec3 origin = init_Vec3_LocalPlayer(player);
      PkgCls_a.Inner_StreamUtil request = init_a_StreamUtil_Str(action.init(), origin, action.maxPackets, action.pauseMs);
      if (request.init_a_DataUtil() != PkgCls_a.Inner_DataUtil.init) {
         return new PkgCls_a.Inner_StateCache(false, request.init_Str().isBlank() ? "Invalid TP action" : request.init_Str());
      }

      init_V_Str("replaced by macro TP");
      Entity vehicle = player.getVehicle();
      PkgCls_a.Inner_ConfigStore job = PkgCls_a.Inner_ConfigStore.init(player, mc.getConnection(), player.level(), vehicle, request, handle.init_J());
      synchronized (init_Obj) {
         init_a_ConfigStore = job;
      }

      init_V_LocalPlayer(player);
      return new PkgCls_a.Inner_StateCache(true, init_Str_Vec3(request.init_Vec3(), request.init_I(), request.load(), vehicle != null));
   }

   public static void init_V_J_Str(long id, String reason) {
      PkgCls_a.Inner_ConfigStore job = init_a_ConfigStore_J(id);
      if (job != null) {
         init_V_a_ConfigStore(job, bl.Inner_DataUtil.write, reason == null ? "macro stopped" : reason);
      }
   }

   public static void load(long id, String reason) {
      PkgCls_a.Inner_ConfigStore job = init_a_ConfigStore_J(id);
      if (job != null) {
         init_V_a_ConfigStore_3(job, false, reason == null ? "timeout" : reason);
      }
   }

   private static PkgCls_a.Inner_ConfigStore init_a_ConfigStore_J(long id) {
      synchronized (init_Obj) {
         return init_a_ConfigStore != null && init_a_ConfigStore.init_J == id ? init_a_ConfigStore : null;
      }
   }

   private static String init_Str_a_StreamUtil(PkgCls_a.Inner_StreamUtil request) {
      if (request.init_a_DataUtil() == PkgCls_a.Inner_DataUtil.flush) {
         return request.init_Str();
      } else if (request.init_a_DataUtil() == PkgCls_a.Inner_DataUtil.sync) {
         return "Usage: tp <x> <y> <z> [maxPackets] [pauseMs]";
      } else if (request.init_a_DataUtil() == PkgCls_a.Inner_DataUtil.write || request.init_a_DataUtil() == PkgCls_a.Inner_DataUtil.update) {
         AutismConfig config = AutismConfig.getGlobal();
         config.tpMaxPackets = request.init_I();
         config.tpPauseMs = request.load();
         config.save();
         return "TP defaults: " + request.init_I() + " packets, " + request.load() + " ms";
      } else if (request.init_a_DataUtil() == PkgCls_a.Inner_DataUtil.read) {
         return init_Str_status();
      } else {
         return request.init_a_DataUtil() == PkgCls_a.Inner_DataUtil.load ? init_Str_stop("stopped by command") : null;
      }
   }

   public static void init_V_Minecraft(Minecraft mc) {
      PkgCls_a.Inner_ConfigStore job = init_a_ConfigStore;
      boolean settling = job != null && job.close != 0L;
      boolean blockInput = job != null && job.init_a_NetHelper == PkgCls_a.Inner_NetHelper.init && mc != null && !settling && aR.init_Z_30();
      if (blockInput) {
         load(mc);
         init_Z = true;
      } else if (init_Z) {
         init_Z = false;
         read(mc);
      }

      if (job != null && mc != null) {
         job.read_J++;
         long now = System.currentTimeMillis();
         String invalid = init_Str_a_ConfigStore_Minecraft(job, mc);
         if (invalid != null) {
            init_V_a_ConfigStore_3(job, false, invalid);
         } else if (now - job.update_J > 300000L) {
            init_V_a_ConfigStore_3(job, false, "timeout");
         } else if (job.open != 0L && now >= job.open) {
            init_V_a_ConfigStore_3(job, false, "authoritative arrival timeout");
         } else {
            if (now - job.flush_J > 3500L) {
               if (job.load_I < 3) {
                  job.load_I++;
                  job.flush_J = now;
                  job.load_Deque.clear();
                  job.init_Deque.clear();
                  job.update_Vec3 = null;
                  job.write_J = -9223372036854775808L;
               } else if (now - job.flush_J > 15500L) {
                  init_V_a_ConfigStore_3(job, false, "timeout");
                  return;
               }
            }

            if (!settling && init_Z_Minecraft(mc)) {
               init_V_a_ConfigStore_2(job, job.load_Vec3, load(job, job.load_Vec3));
               init_V_a_ConfigStore_3(job, false, "cancelled by movement input");
            } else {
               PkgCls_a.Inner_CachePool correction;
               synchronized (init_Obj) {
                  correction = job.init_a_CachePool;
                  job.init_a_CachePool = null;
               }

               if (correction != null) {
                  double predictedDistance = job.load_Vec3.distanceTo(job.init_Vec3);
                  job.load_Vec3 = correction.init_Vec3();
                  if (job.load_Vec3.distanceTo(job.init_Vec3) <= 2.5) {
                     job.load_Deque.clear();
                     job.init_Deque.clear();
                     job.update_Vec3 = null;
                     job.sync_J = now;
                     if (init_Z_a_ConfigStore(job)) {
                        job.close = 0L;
                        if (correction.init_J() == job.load_J && job.load_Vec3.distanceTo(job.init_Vec3) <= 0.35) {
                           init_V_a_ConfigStore_2(job, job.load_Vec3, load(job, job.load_Vec3));
                           init_V_a_ConfigStore_3(job, true, init_Str_a_ConfigStore(job));
                           return;
                        }

                        init_V_a_ConfigStore_J(job, now);
                        init_V_a_ConfigStore_2(job, job.load_Vec3, load(job, job.load_Vec3));
                        return;
                     }

                     job.close = now;
                     init_V_a_ConfigStore_2(job, job.load_Vec3, load(job, job.load_Vec3));
                     return;
                  }

                  job.load_Deque.clear();
                  job.init_Deque.clear();
                  job.update_Vec3 = null;
                  job.close = 0L;
                  job.open = 0L;
                  double distance = job.load_Vec3.distanceTo(job.init_Vec3);
                  double predictionError = correction.init_Vec3().distanceTo(job.read_Vec3);
                  if (init_Z_D(job.load_D, predictedDistance, distance, predictionError)) {
                     job.load_D = Math.min(job.load_D, distance);
                     job.write_I = 0;
                     job.sync_J = now;
                     job.flush_Vec3 = null;
                     job.update_I = 0;
                     job.write_D = 0.0 / 0.0;
                     job.init_D = Math.min(10.0, job.init_D * 1.25);
                     job.init_a_JsonUtil.init_V(now, job.read_Z ? 100L : 0L);
                  } else {
                     job.write_I++;
                     if (job.sync_Vec3 != null) {
                        if (job.flush_Vec3 != null && job.flush_Vec3.distanceTo(job.sync_Vec3) < 1.5) {
                           job.update_I++;
                        } else {
                           job.update_I = 1;
                        }

                        job.flush_Vec3 = job.sync_Vec3;
                     }

                     double reduced = job.write_I >= 3
                        ? job.init_D * 0.5
                        : (job.write_I == 2 ? Math.max(0.5, job.init_D * 0.25) : Math.max(2.0, job.init_D * 0.5));
                     job.init_D = Math.max(0.0625, reduced);
                     job.read_Z = true;
                     job.read_D = distance;
                     job.poll = now + 100L;
                     job.push = now + 600L;
                     job.init_a_JsonUtil.init_V(now, 100L);
                  }

                  if (read(job, job.load_Vec3)) {
                     job.read_I = 0;
                  }

                  init_V_a_ConfigStore_2(job, job.load_Vec3, load(job, job.load_Vec3));
               }

               if (init_Z_a_ConfigStore_J(job, now)) {
                  Vec3 destination = job.write_Z ? job.init_Vec3 : init_Vec3_a_ConfigStore(job);
                  if (destination != null) {
                     if (job.load_Vec3.distanceTo(destination) <= 0.5) {
                        F.Inner_StreamUtil view = init_F_StreamUtil_a_ConfigStore(job);
                        if (view != null && view.init_Z(job.load_Vec3) && !view.load(job.load_Vec3)) {
                           Vec3 clearSpot = F.init(view, job.load_Vec3, 3.0);
                           if (clearSpot != null && view.update(clearSpot)) {
                              job.update_Vec3 = clearSpot;
                              job.close = 0L;
                              job.open = 0L;
                              return;
                           }
                        }

                        if (init_Z_a_ConfigStore(job)) {
                           init_V_a_ConfigStore_J(job, now);
                           if (now >= job.open) {
                              init_V_a_ConfigStore_3(job, false, "authoritative arrival timeout");
                           }
                        } else {
                           if (job.close == 0L) {
                              job.close = now;
                           }

                           if (now - job.close >= 250L) {
                              init_V_a_ConfigStore_3(job, true, init_Str_a_ConfigStore(job));
                           }
                        }
                     } else {
                        job.close = 0L;
                        job.open = 0L;
                        if (job.read_Z) {
                           double recoveryDistance = job.load_Vec3.distanceTo(destination);
                           if (now >= job.push && job.read_D - recoveryDistance >= 0.5) {
                              job.read_Z = false;
                              job.poll = 0L;
                              job.init_D = Math.min(10.0, Math.max(1.0, job.init_D * 2.0));
                              job.init_a_JsonUtil.init_V(now, 0L);
                           } else if (now < job.poll) {
                              return;
                           }
                        }

                        if (!job.init_a_JsonUtil.init_Z(now)) {
                           int budget = job.read_Z ? 1 : job.init_a_JsonUtil.init_I();

                           while (budget-- > 0) {
                              if (job.load_Deque.isEmpty() && !init_Z_a_ConfigStore(job, destination, now, budget + 1)) {
                                 return;
                              }

                              PkgCls_a.Inner_TaskQueue frame = job.load_Deque.pollFirst();
                              if (frame == null) {
                                 return;
                              }

                              if (!init_Z_a_ConfigStore_a_TaskQueue(job, frame)) {
                                 job.load_Deque.clear();
                                 job.init_Deque.clear();
                                 job.update_Vec3 = null;
                                 job.write_Vec3 = null;
                                 job.write_Z = false;
                                 job.init_a_JsonUtil.init_V(now, 100L);
                                 return;
                              }

                              boolean emergencyFrame = job.write_Z && frame.init_Z();
                              if (!load(job, frame)) {
                                 init_V_a_ConfigStore_3(job, false, "movement connection became unavailable");
                                 return;
                              }

                              int sentPackets = init_I_a_ConfigStore_a_TaskQueue(job, frame);
                              job.init_I += sentPackets;

                              for (int i = 0; i < sentPackets; i++) {
                                 job.init_a_JsonUtil.init_V(now);
                              }

                              budget -= sentPackets - 1;
                              if (frame.init_Z()) {
                                 job.load_Vec3 = frame.init_Vec3();
                                 job.read_Vec3 = job.load_Vec3;
                                 job.flush_J = now;
                                 if (job.load_Vec3.distanceTo(destination) < job.load_D - 0.01) {
                                    job.sync_J = now;
                                 }

                                 job.read_I = init_I_I_Z(job.read_I, read(job, job.load_Vec3));
                                 init_V_a_ConfigStore_2(job, job.load_Vec3, frame.load());
                                 if (job.read_Z) {
                                    job.poll = now + 100L;
                                 }
                              }

                              if (emergencyFrame) {
                                 job.write_Z = false;
                                 job.read_Z = false;
                                 job.init_a_JsonUtil.init_V(now, 0L);
                                 return;
                              }

                              if (job.load_Vec3.distanceTo(destination) <= 0.5
                                 && job.load_Deque.isEmpty()
                                 && job.init_Deque.isEmpty()
                                 && job.update_Vec3 == null) {
                                 if (init_Z_a_ConfigStore(job)) {
                                    init_V_a_ConfigStore_J(job, now);
                                 } else {
                                    job.close = now;
                                 }

                                 return;
                              }

                              if (job.init_a_JsonUtil.init_Z(now)) {
                                 return;
                              }
                           }
                        }
                     }
                  }
               }
            }
         }
      }
   }

   private static boolean init_Z_a_ConfigStore(PkgCls_a.Inner_ConfigStore job) {
      return AutismLiteVariant.enabled() ? job.init_a_NetHelper == PkgCls_a.Inner_NetHelper.init : false;
   }

   private static void init_V_a_ConfigStore_J(PkgCls_a.Inner_ConfigStore job, long now) {
      if (job.open == 0L) {
         job.open = now + 5000L;
      }
   }

   private static Vec3 init_Vec3_a_ConfigStore(PkgCls_a.Inner_ConfigStore job) {
      F.Inner_StreamUtil view = init_F_StreamUtil_a_ConfigStore(job);
      if (job.write_Vec3 != null) {
         if (view == null || !view.init_Z(job.write_Vec3)) {
            return null;
         }

         if (view.load(job.write_Vec3) && view.update(job.write_Vec3) && (!job.load_Z || view.sync(job.write_Vec3))) {
            return job.write_Vec3;
         }

         job.write_Vec3 = null;
         job.load_Z = false;
      }

      if (view != null && view.init_Z(job.init_Vec3)) {
         boolean targetLavaSafe = view.update(job.init_Vec3);
         if (!targetLavaSafe && System.currentTimeMillis() < job.pop) {
            return null;
         } else {
            Vec3 clear = init_Vec3_F_StreamUtil_2(view, job.init_Vec3, job.sync_I);
            if (clear != null) {
               job.write_Vec3 = clear;
               job.load_Z = clear.distanceTo(job.init_Vec3) > 0.5;
               return clear;
            } else if (targetLavaSafe) {
               job.write_Vec3 = job.init_Vec3;
               return job.write_Vec3;
            } else {
               job.sync_I = Math.min(32, job.sync_I + 8);
               job.pop = System.currentTimeMillis() + 1000L;
               return null;
            }
         }
      } else {
         return job.init_a_NetHelper == PkgCls_a.Inner_NetHelper.load ? job.init_Vec3 : job.init_Vec3;
      }
   }

   static Vec3 init_Vec3_F_StreamUtil_2(F.Inner_StreamUtil view, Vec3 requested, double lavaSearchRadius) {
      if (view == null || requested == null || !view.init_Z(requested)) {
         return null;
      }

      if (!view.update(requested)) {
         return F.read(view, requested, lavaSearchRadius);
      }

      Vec3 clear = F.init(view, requested, 3.0);
      return clear == null ? requested : clear;
   }

   private static boolean init_Z_a_ConfigStore(PkgCls_a.Inner_ConfigStore job, Vec3 destination, long now, int packetBudget) {
      if (job.update_Vec3 == null && !job.init_Deque.isEmpty()) {
         job.update_Vec3 = job.init_Deque.pollFirst();
      }

      if (job.update_Vec3 == null) {
         if (job.write_J == job.read_J) {
            return false;
         }

         job.write_J = job.read_J;
         F.Inner_StreamUtil view = init_F_StreamUtil_a_ConfigStore(job);
         if (job.close == 0L && init_Z_I(job.read_I) && view != null && view.init_Z(job.load_Vec3)) {
            Vec3 localAnchor = F.write(view, job.load_Vec3, 32.0);
            if (localAnchor != null && localAnchor.distanceTo(job.load_Vec3) > 0.5) {
               job.update_Vec3 = localAnchor;
            } else if (localAnchor != null) {
               job.read_I = 0;
            }
         }

         F.Inner_BufferHelper route = null;
         if (job.update_Vec3 == null && view != null) {
            Vec3 direct = write(view, job, destination);
            if (direct == null) {
               direct = init_Vec3_F_StreamUtil_3(view, job.load_Vec3, destination);
            }

            if (init_Z_a_ConfigStore_Vec3(job, direct)) {
               direct = null;
            }

            if (direct == null) {
               direct = load_Vec3(view, job.load_Vec3, destination);
               if (init_Z_a_ConfigStore_Vec3(job, direct)) {
                  direct = null;
               }
            }

            if (direct == null) {
               direct = read_Vec3(view, job.load_Vec3, destination);
               if (init_Z_a_ConfigStore_Vec3(job, direct)) {
                  direct = null;
               }
            }

            if (direct == null) {
               direct = write(view, job.load_Vec3, destination);
               if (init_Z_a_ConfigStore_Vec3(job, direct)) {
                  direct = null;
               }
            }

            if (direct == null && job.update_I >= 2) {
               direct = load(view, job, destination);
            }

            if (direct != null) {
               job.update_Vec3 = direct;
               job.sync_Vec3 = direct;
            } else {
               route = F.read(view, job.load_Vec3, destination, 80 + job.load_I * 24, 128, 3);
            }
         }

         if (job.update_Vec3 == null) {
            if (init_Z_F_BufferHelper(route, job.load_Vec3, destination) && init_Z_F_StreamUtil(view, job.load_Vec3, route.init_List())) {
               job.init_Deque.addAll(route.init_List());
               job.update_Vec3 = job.init_Deque.pollFirst();
               job.sync_Vec3 = job.update_Vec3;
            } else {
               job.init_Deque.addAll(init_List_F_StreamUtil(view, job.load_Vec3, destination, job.load_I));
               job.update_Vec3 = job.init_Deque.pollFirst();
            }
         }

         if (job.update_Vec3 == null) {
            return false;
         }
      }

      Vec3 from = job.load_Vec3;
      Vec3 to = job.update_Vec3;
      double distance = from.distanceTo(to);
      if (distance <= 0.5) {
         job.update_Vec3 = null;
         return init_Z_a_ConfigStore(job, destination, now, packetBudget);
      }

      if (job.write_Z) {
         job.update_Vec3 = null;
         job.load_Deque.addLast(PkgCls_a.Inner_TaskQueue.init(from, to, load(job, to)));
         return true;
      }

      int available = Math.max(1, packetBudget);
      double stepLimit = job.init_Entity != null ? Math.min(4.0, job.init_D) : job.init_D;
      PkgCls_a.Inner_BufferHelper clip = init_a_BufferHelper_D(distance, stepLimit, available, job.read_Z);
      Vec3 next = from.add(to.subtract(from).scale(clip.init_D() / distance));
      boolean deepDescent = next.y - from.y < -3.0;
      if (deepDescent && !job.read_Z) {
         clip = init_a_BufferHelper_D(distance, job.init_D, Math.max(1, available - 2), false);
         next = from.add(to.subtract(from).scale(clip.init_D() / distance));
      }

      boolean rerouting = false;
      F.Inner_StreamUtil sweepView = init_F_StreamUtil_a_ConfigStore(job);
      Entity sweepEntity = sweepView == null ? null : sweepView.init_Entity();
      if (sweepEntity != null && Math.hypot(next.x - from.x, next.z - from.z) > 0.5) {
         Vec3 requested = next.subtract(from);
         Vec3 allowed = A.init_Vec3(sweepEntity, from, next);
         if (!allowed.equals(requested)) {
            double wanted = Math.hypot(requested.x, requested.z);
            double contact = Math.hypot(allowed.x, allowed.z);
            if (contact >= wanted - 1.0E-6) {
               next = from.add(allowed);
            } else {
               job.sync_Vec3 = to;
               job.flush_Vec3 = to;
               job.update_I += contact >= 0.5 ? 1 : 2;
               job.init_Deque.clear();
               job.update_Vec3 = null;
               if (contact < 0.5) {
                  Vec3 over = read(sweepView, job, destination);
                  if (over != null) {
                     job.update_Vec3 = over;
                     job.sync_Vec3 = over;
                  } else {
                     Vec3 pocket = init_Vec3_F_StreamUtil(sweepView, job, destination);
                     if (pocket != null && !init_Z_a_ConfigStore_Vec3(job, pocket)) {
                        job.update_Vec3 = pocket;
                        job.sync_Vec3 = pocket;
                     }
                  }

                  return false;
               }

               next = from.add(allowed);
               rerouting = true;
            }
         }
      }

      if (!rerouting && next.distanceTo(to) <= 0.5) {
         next = to;
         job.update_Vec3 = null;
      }

      boolean fromGrounded = load(job, from);
      boolean nextGrounded = load(job, next);

      for (int i = 1; i < clip.init_I(); i++) {
         job.load_Deque.addLast(PkgCls_a.Inner_TaskQueue.init(from, fromGrounded));
      }

      job.load_Deque.addLast(PkgCls_a.Inner_TaskQueue.init(from, next, nextGrounded));
      return true;
   }

   private static boolean init_Z_Vec3(Vec3 from, Vec3 end, Vec3 destination) {
      if (from != null && end != null && destination != null) {
         double fromHorizontal = Math.hypot(destination.x - from.x, destination.z - from.z);
         double endHorizontal = Math.hypot(destination.x - end.x, destination.z - end.z);
         return endHorizontal + 0.5 < fromHorizontal ? true : fromHorizontal <= 0.5 && Math.abs(destination.y - end.y) + 0.5 < Math.abs(destination.y - from.y);
      } else {
         return false;
      }
   }

   static boolean init_Z_F_BufferHelper(F.Inner_BufferHelper route, Vec3 from, Vec3 destination) {
      return route != null
         && route.load()
         && (route.init_F_ConfigStore() == F.Inner_ConfigStore.read || init_Z_Vec3(from, route.init_List().getLast(), destination));
   }

   private static boolean init_Z_a_ConfigStore_J(PkgCls_a.Inner_ConfigStore job, long now) {
      F.Inner_StreamUtil view = init_F_StreamUtil_a_ConfigStore(job);
      if (view == null || !view.init_Z(job.load_Vec3)) {
         return false;
      }

      if (view.update(job.load_Vec3)) {
         return true;
      }

      Vec3 escape = F.read(view, job.load_Vec3, 32.0);
      if (escape == null) {
         return false;
      }

      job.load_Deque.clear();
      job.init_Deque.clear();
      job.update_Vec3 = escape;
      job.write_Z = true;
      job.read_Z = false;
      job.init_a_JsonUtil.init_V(now, 0L);
      return true;
   }

   private static boolean init_Z_a_ConfigStore_a_TaskQueue(PkgCls_a.Inner_ConfigStore job, PkgCls_a.Inner_TaskQueue frame) {
      F.Inner_StreamUtil view = init_F_StreamUtil_a_ConfigStore(job);
      if (view != null && frame != null) {
         return frame.init_Z() ? init_Z_F_StreamUtil_List(view, frame.init_List()) : view.init_Z(job.load_Vec3) && view.update(job.load_Vec3);
      } else {
         return false;
      }
   }

   static boolean init_Z_F_StreamUtil_List(F.Inner_StreamUtil view, List<A.Inner_DataUtil> steps) {
      if (view != null && steps != null && !steps.isEmpty()) {
         for (A.Inner_DataUtil step : steps) {
            if (step == null || step.init_Vec3() == null || !view.init_Z(step.init_Vec3()) || !view.update(step.init_Vec3())) {
               return false;
            }
         }

         return true;
      } else {
         return false;
      }
   }

   private static boolean init_Z_F_StreamUtil(F.Inner_StreamUtil view, Vec3 start, List<Vec3> route) {
      if (view != null && start != null && route != null && !route.isEmpty()) {
         for (Vec3 waypoint : route) {
            if (waypoint == null || !view.init_Z(waypoint) || !view.update(waypoint)) {
               return false;
            }
         }

         return true;
      } else {
         return false;
      }
   }

   static boolean init_Z_F_StreamUtil_2(F.Inner_StreamUtil view, Vec3 from, Vec3 to) {
      if (view != null && from != null && to != null) {
         int samples = Math.max(1, (int)Math.ceil(from.distanceTo(to) / 0.3));

         for (int i = 1; i <= samples; i++) {
            Vec3 sample = from.lerp(to, (double)i / samples);
            if (!view.init_Z(sample) || !view.update(sample)) {
               return false;
            }
         }

         return true;
      } else {
         return false;
      }
   }

   private static Vec3 init_Vec3_F_StreamUtil_3(F.Inner_StreamUtil view, Vec3 from, Vec3 destination) {
      if (view != null && from != null && destination != null) {
         if (from.distanceTo(destination) <= 0.5) {
            return null;
         } else if (view.init_Z(destination) && view.load(destination) && view.update(destination) && view.sync(destination)) {
            return read_Z(view, from, destination) && init_Z_F_StreamUtil_2(view, from, destination) ? destination : null;
         } else {
            return null;
         }
      } else {
         return null;
      }
   }

   private static Vec3 load_Vec3(F.Inner_StreamUtil view, Vec3 from, Vec3 destination) {
      if (view != null && from != null && destination != null) {
         if (Math.abs(destination.y - from.y) < 4.0) {
            return null;
         } else {
            Vec3 vertical = new Vec3(from.x, destination.y, from.z);
            if (!load_Z(view, from, vertical)) {
               return null;
            } else {
               return Math.hypot(destination.x - vertical.x, destination.z - vertical.z) > 0.5 && !load_Z(view, vertical, destination) ? null : vertical;
            }
         }
      } else {
         return null;
      }
   }

   private static Vec3 read_Vec3(F.Inner_StreamUtil view, Vec3 from, Vec3 destination) {
      if (view != null && from != null && destination != null) {
         if (from.distanceTo(destination) < 6.0) {
            return null;
         }

         if (view.init_Z(destination) && view.load(destination) && view.update(destination) && view.sync(destination)) {
            for (int lift = 1; lift <= 4; lift++) {
               Vec3 mid = from.add(destination).scale(0.5).add(0.0, lift, 0.0);
               if (view.init_Z(mid)
                  && view.load(mid)
                  && view.update(mid)
                  && !load(view, mid)
                  && read_Z(view, from, mid)
                  && init_Z_F_StreamUtil_2(view, from, mid)
                  && read_Z(view, mid, destination)
                  && init_Z_F_StreamUtil_2(view, mid, destination)) {
                  return mid;
               }
            }

            return null;
         } else {
            return null;
         }
      } else {
         return null;
      }
   }

   private static Vec3 write(F.Inner_StreamUtil view, Vec3 from, Vec3 destination) {
      if (view != null && from != null && destination != null) {
         double dx = destination.x - from.x;
         double dz = destination.z - from.z;
         if (Math.abs(dx) <= 0.5 && Math.abs(dz) <= 0.5) {
            return null;
         }

         Vec3[] baseCorners = new Vec3[]{
            new Vec3(destination.x, from.y, from.z),
            new Vec3(from.x, from.y, destination.z),
            new Vec3(destination.x, destination.y, from.z),
            new Vec3(from.x, destination.y, destination.z)
         };
         Vec3 best = null;
         double bestLength = 0.0;

         for (Vec3 base : baseCorners) {
            for (int lift = 0; lift <= 4; lift++) {
               Vec3 corner = lift == 0 ? base : base.add(0.0, lift, 0.0);
               double length = from.distanceTo(corner);
               if (length < 4.0) {
                  break;
               }

               if (load_Z(view, from, corner) && !load(view, corner)) {
                  if (length > bestLength) {
                     bestLength = length;
                     best = corner;
                  }
                  break;
               }
            }
         }

         double horizontal = Math.hypot(dx, dz);
         return best != null && bestLength >= Math.max(4.0, horizontal * 0.34) ? best : null;
      } else {
         return null;
      }
   }

   private static boolean load_Z(F.Inner_StreamUtil view, Vec3 from, Vec3 to) {
      if (view.init_Z(to) && view.load(to) && view.update(to)) {
         Entity entity = view.init_Entity();
         if (entity != null) {
            double distance = from.distanceTo(to);
            int segments = Math.max(1, (int)Math.ceil(distance / 8.0));
            Vec3 prev = from;

            for (int i = 1; i <= segments; i++) {
               Vec3 next = from.lerp(to, (double)i / segments);
               if (!A.init_Z(entity, prev, next)) {
                  return false;
               }

               prev = next;
            }

            return init_Z_F_StreamUtil_2(view, from, to);
         } else {
            return read_Z(view, from, to) && init_Z_F_StreamUtil_2(view, from, to);
         }
      } else {
         return false;
      }
   }

   private static boolean load(F.Inner_StreamUtil view, Vec3 pos) {
      Entity entity = view == null ? null : view.init_Entity();
      if (entity != null && entity.level() != null && pos != null) {
         double eyeY = pos.y + entity.getEyeHeight();
         BlockPos eyeBlock = BlockPos.containing(pos.x, eyeY, pos.z);
         FluidState fluid = entity.level().getFluidState(eyeBlock);
         return !fluid.isEmpty() && eyeY < eyeBlock.getY() + fluid.getHeight(entity.level(), eyeBlock);
      } else {
         return false;
      }
   }

   private static Vec3 init_Vec3_F_StreamUtil(F.Inner_StreamUtil view, PkgCls_a.Inner_ConfigStore job, Vec3 destination) {
      if (view != null && job != null && destination != null) {
         Vec3 current = job.load_Vec3;
         Vec3 best = null;
         double bestScore = -1.0 / 0.0;

         for (int dir = 0; dir < 8; dir++) {
            double angle = dir * 0.7853981633974483;
            double dx = -Math.sin(angle);
            double dz = Math.cos(angle);
            double reach = 0.0;
            double maxReach = 4.0 + job.load_I * 4.0;

            for (int step = 1; step <= maxReach; step++) {
               Vec3 candidate = new Vec3(current.x + dx * step, current.y, current.z + dz * step);
               if (!load_Z(view, current, candidate)) {
                  break;
               }

               reach = step;
            }

            if (!(reach <= 0.0)) {
               Vec3 end = new Vec3(current.x + dx * reach, current.y, current.z + dz * reach);
               double regression = end.distanceTo(destination) - current.distanceTo(destination);
               double score = reach - regression * 0.25;
               if (score > bestScore) {
                  bestScore = score;
                  best = end;
               }
            }
         }

         return best;
      } else {
         return null;
      }
   }

   private static boolean init_Z_a_ConfigStore_Vec3(PkgCls_a.Inner_ConfigStore job, Vec3 candidate) {
      return candidate != null && job.flush_Vec3 != null && candidate.distanceTo(job.flush_Vec3) < 1.5;
   }

   private static Vec3 load(F.Inner_StreamUtil view, PkgCls_a.Inner_ConfigStore job, Vec3 destination) {
      if (view != null && job != null) {
         Vec3 flatAtFeet = new Vec3(destination.x, job.load_Vec3.y, destination.z);
         return load_Z(view, job.load_Vec3, flatAtFeet) ? null : read(view, job, destination);
      } else {
         return null;
      }
   }

   private static Vec3 read(F.Inner_StreamUtil view, PkgCls_a.Inner_ConfigStore job, Vec3 destination) {
      if (view != null && job != null && destination != null) {
         Vec3 current = job.load_Vec3;
         double horizontal = Math.hypot(destination.x - current.x, destination.z - current.z);
         if (horizontal <= 0.5) {
            return null;
         }

         double reach = Math.min(8.0, horizontal);
         double dirX = (destination.x - current.x) / horizontal;
         double dirZ = (destination.z - current.z) / horizontal;
         int maxLift = 32 + job.load_I * 8;

         for (int lift = 1; lift <= maxLift; lift++) {
            Vec3 lifted = new Vec3(current.x, current.y + lift, current.z);
            if (!load_Z(view, current, lifted)) {
               return null;
            }

            Vec3 ahead = new Vec3(lifted.x + dirX * reach, lifted.y, lifted.z + dirZ * reach);
            if (load_Z(view, lifted, ahead)) {
               job.write_D = lifted.y;
               return lifted;
            }
         }

         return null;
      } else {
         return null;
      }
   }

   private static Vec3 write(F.Inner_StreamUtil view, PkgCls_a.Inner_ConfigStore job, Vec3 destination) {
      if (view != null && job != null && !Double.isNaN(job.write_D)) {
         Vec3 current = job.load_Vec3;
         Vec3 drop = new Vec3(current.x, destination.y, current.z);
         if (load_Z(view, current, drop) && load_Z(view, drop, destination)) {
            job.write_D = 0.0 / 0.0;
            return drop;
         }

         double dx = destination.x - current.x;
         double dz = destination.z - current.z;
         double horizontal = Math.hypot(dx, dz);
         if (horizontal > 0.5) {
            double step = Math.min(4.0, horizontal);
            Vec3 flat = new Vec3(current.x + dx / horizontal * step, job.write_D, current.z + dz / horizontal * step);
            if (load_Z(view, current, flat)) {
               return flat;
            }
         }

         job.write_D = 0.0 / 0.0;
         return null;
      } else {
         return null;
      }
   }

   private static boolean read_Z(F.Inner_StreamUtil view, Vec3 from, Vec3 to) {
      int samples = Math.max(1, (int)Math.ceil(from.distanceTo(to) / 0.3));

      for (int i = 1; i <= samples; i++) {
         Vec3 sample = from.lerp(to, (double)i / samples);
         if (!view.init_Z(sample) || !view.load(sample)) {
            return false;
         }
      }

      return true;
   }

   static List<Vec3> init_List_F_StreamUtil(F.Inner_StreamUtil view, Vec3 start, Vec3 destination) {
      return init_List_F_StreamUtil(view, start, destination, 0);
   }

   static List<Vec3> init_List_F_StreamUtil(F.Inner_StreamUtil view, Vec3 start, Vec3 destination, int rescueLevel) {
      return F.init_List_F_StreamUtil(view, start, destination, 32 + Math.max(0, rescueLevel) * 16);
   }

   static List<Vec3> init_List_Vec3_Vec3(Vec3 start, Vec3 destination) {
      return List.of();
   }

   static int init_I_I_Z(int current, boolean stable) {
      return stable ? 0 : Math.min(5, Math.max(0, current) + 1);
   }

   static boolean init_Z_I(int spatialLegsSinceStable) {
      return spatialLegsSinceStable >= 4;
   }

   static PkgCls_a.Inner_BufferHelper init_a_BufferHelper_D(double distance, double step, int packetBudget, boolean recovery) {
      double safeDistance = Math.max(0.0, distance);
      double safeStep = Math.max(0.0625, Math.min(10.0, step));
      int available = Math.max(1, packetBudget);
      if (recovery) {
         return new PkgCls_a.Inner_BufferHelper(Math.min(safeDistance, safeStep), 1, false);
      }

      int packets = Math.max(1, Math.min(available, (int)Math.ceil(safeDistance / safeStep)));
      return new PkgCls_a.Inner_BufferHelper(safeDistance, packets, true);
   }

   static boolean init_Z_D(double bestDistance, double predictedDistance, double correctedDistance, double predictionError) {
      return bestDistance - correctedDistance >= 0.25 || predictionError <= 0.05 || correctedDistance + 0.05 < predictedDistance;
   }

   private static boolean load(PkgCls_a.Inner_ConfigStore job, PkgCls_a.Inner_TaskQueue frame) {
      Minecraft mc = Minecraft.getInstance();
      if (mc.getConnection() != null && mc.player != null) {
         boolean noFall = job.init_a_NetHelper == PkgCls_a.Inner_NetHelper.init && aR.load_Z_12();
         boolean previousOwnedSend = write();

         try {
            if (job.init_Entity != null) {
               if (frame.init_Z()) {
                  for (A.Inner_DataUtil step : frame.init_List()) {
                     job.init_Entity.setPos(step.init_Vec3());
                     mc.getConnection().send(ServerboundMoveVehiclePacket.fromEntity(job.init_Entity));
                  }

                  job.init_Entity.positionRider(mc.player);
               } else {
                  mc.getConnection().send(ServerboundMoveVehiclePacket.fromEntity(job.init_Entity));
               }
            } else if (!frame.init_Z()) {
               mc.getConnection().send(new StatusOnly(noFall || frame.load(), false));
            } else {
               for (A.Inner_DataUtil step : frame.init_List()) {
                  mc.getConnection().send(new Pos(init_Vec3_a_ConfigStore_Vec3(job, step.init_Vec3()), step.init_Z(), false));
               }
            }

            return true;
         } finally {
            init_V_Z(previousOwnedSend);
         }
      } else {
         return false;
      }
   }

   private static Vec3 init_Vec3_a_ConfigStore_Vec3(PkgCls_a.Inner_ConfigStore job, Vec3 position) {
      if (job.init_a_NetHelper == PkgCls_a.Inner_NetHelper.init && aR.read()) {
         long now = System.currentTimeMillis();
         if (now - job.tick < 1000L) {
            return position;
         } else {
            double lastY = job.read_Vec3 == null ? position.y : job.read_Vec3.y;
            if (lastY - position.y >= 0.0313) {
               return position;
            } else {
               F.Inner_StreamUtil view = init_F_StreamUtil_a_ConfigStore(job);
               if (view != null && view.load(position) && !view.read(position)) {
                  job.tick = now;
                  return new Vec3(position.x, position.y - 0.0313, position.z);
               } else {
                  return position;
               }
            }
         }
      } else {
         return position;
      }
   }

   private static int init_I_a_ConfigStore_a_TaskQueue(PkgCls_a.Inner_ConfigStore job, PkgCls_a.Inner_TaskQueue frame) {
      return !frame.init_Z() ? 1 : Math.max(1, frame.init_List().size());
   }

   private static void init_V_a_ConfigStore_2(PkgCls_a.Inner_ConfigStore job, Vec3 position, boolean onGround) {
      LocalPlayer player = job.init_LocalPlayer;
      if (player != null) {
         if (job.init_Entity != null) {
            job.init_Entity.setPos(position);
         }

         if (job.init_Entity != null) {
            job.init_Entity.positionRider(player);
         } else {
            player.setPos(position.x, position.y, position.z);
         }

         player.setOnGround(onGround);
         player.setDeltaMovement(Vec3.ZERO);
         player.xxa = 0.0F;
         player.zza = 0.0F;
         player.setJumping(false);
         player.resetFallDistance();
      }
   }

   private static F.Inner_StreamUtil init_F_StreamUtil_a_ConfigStore(PkgCls_a.Inner_ConfigStore job) {
      Entity entity = job.init_Entity != null ? job.init_Entity : job.init_LocalPlayer;
      return F.init_F_StreamUtil_Entity(entity);
   }

   private static boolean load(PkgCls_a.Inner_ConfigStore job, Vec3 position) {
      F.Inner_StreamUtil view = init_F_StreamUtil_a_ConfigStore(job);
      return init_Z_F_StreamUtil_Vec3(view, position);
   }

   private static boolean read(PkgCls_a.Inner_ConfigStore job, Vec3 position) {
      F.Inner_StreamUtil view = init_F_StreamUtil_a_ConfigStore(job);
      return view != null && position != null && view.init_Z(position) && view.load(position) && view.update(position) && view.sync(position);
   }

   static boolean init_Z_F_StreamUtil_Vec3(F.Inner_StreamUtil view, Vec3 position) {
      return view != null && position != null && view.init_Z(position) && view.load(position) && view.update(position) && view.read(position);
   }

   public static long init_J() {
      return init_J_Z(false);
   }

   public static long load_J() {
      return init_J_Z(true);
   }

   private static long init_J_Z(boolean vehicle) {
      synchronized (init_Obj) {
         PkgCls_a.Inner_ConfigStore job = init_a_ConfigStore;
         return job != null && init_Z_a_ConfigStore_Z(job, vehicle) ? job.load_J : 0L;
      }
   }

   public static void init_V_J_Vec3(long token, Vec3 corrected) {
      if (token != 0L) {
         init_V_J(token, false, corrected);
      }
   }

   public static void load(long token, Vec3 corrected) {
      if (token != 0L) {
         init_V_J(token, true, corrected);
      }
   }

   public static void init_V_Vec3(Vec3 corrected) {
      init_V_J(0L, false, corrected);
   }

   public static void load(Vec3 corrected) {
      init_V_J(0L, true, corrected);
   }

   private static void init_V_J(long token, boolean vehicle, Vec3 corrected) {
      if (corrected != null) {
         synchronized (init_Obj) {
            PkgCls_a.Inner_ConfigStore job = init_a_ConfigStore;
            if (job != null && (token == 0L || token == job.load_J) && init_Z_a_ConfigStore_Z(job, vehicle)) {
               job.init_a_CachePool = new PkgCls_a.Inner_CachePool(corrected, token);
            }
         }
      }
   }

   private static boolean init_Z_a_ConfigStore_Z(PkgCls_a.Inner_ConfigStore job, boolean vehicle) {
      if (job.init_a_NetHelper != PkgCls_a.Inner_NetHelper.init) {
         return false;
      } else {
         Minecraft mc = Minecraft.getInstance();
         if (mc != null && mc.player == job.init_LocalPlayer && mc.getConnection() == job.init_Obj && mc.level == job.load_Obj) {
            Entity currentVehicle = job.init_LocalPlayer.getVehicle();
            return vehicle ? job.init_Entity != null && currentVehicle == job.init_Entity : job.init_Entity == null && currentVehicle == null;
         } else {
            return false;
         }
      }
   }

   public static boolean init_Z() {
      PkgCls_a.Inner_ConfigStore job = init_a_ConfigStore;
      return job != null && job.init_a_NetHelper == PkgCls_a.Inner_NetHelper.init;
   }

   public static boolean load_Z() {
      return init_AtomicInteger.get() == 0 ? false : Boolean.TRUE.equals(init_ThreadLocal.get());
   }

   public static void init_V_Runnable(Runnable action) {
      if (action != null) {
         boolean previous = write();

         try {
            action.run();
         } finally {
            init_V_Z(previous);
         }
      }
   }

   private static boolean write() {
      boolean previous = Boolean.TRUE.equals(init_ThreadLocal.get());
      if (!previous) {
         init_ThreadLocal.set(true);
         init_AtomicInteger.incrementAndGet();
      }

      return previous;
   }

   private static void init_V_Z(boolean previous) {
      if (!previous) {
         init_ThreadLocal.set(false);
         init_AtomicInteger.decrementAndGet();
      }
   }

   public static boolean init_Z_Packet(Packet<?> packet) {
      if (!(packet instanceof ServerboundMovePlayerPacket)
         && !(packet instanceof ServerboundMoveVehiclePacket)
         && !(packet instanceof ServerboundPlayerInputPacket)) {
         return false;
      }

      PkgCls_a.Inner_ConfigStore job = init_a_ConfigStore;
      return job != null && job.init_a_NetHelper == PkgCls_a.Inner_NetHelper.init && !load_Z() ? job.close == 0L : false;
   }

   public static void init_V_Str(String reason) {
      PkgCls_a.Inner_ConfigStore job;
      synchronized (init_Obj) {
         job = init_a_ConfigStore;
      }

      if (job != null) {
         init_V_a_ConfigStore_3(job, false, reason == null ? "cancelled" : reason);
      }
   }

   private static String init_Str_a_ConfigStore_Minecraft(PkgCls_a.Inner_ConfigStore job, Minecraft mc) {
      if (job.init_Z ? MacroExecutor.write_Z() : !MacroExecutor.write_Z()) {
         if (mc.player != job.init_LocalPlayer || mc.getConnection() != job.init_Obj || mc.level != job.load_Obj) {
            return "player connection changed";
         } else if (job.init_LocalPlayer.isDeadOrDying()) {
            return "player died";
         } else {
            return job.init_Entity != null && job.init_LocalPlayer.getVehicle() != job.init_Entity ? "vehicle ownership changed" : null;
         }
      } else {
         return job.init_Z ? "owning macro stopped" : "macro took movement ownership";
      }
   }

   private static boolean init_Z_Minecraft(Minecraft mc) {
      return mc.options.keyUp.isDown()
         || mc.options.keyDown.isDown()
         || mc.options.keyLeft.isDown()
         || mc.options.keyRight.isDown()
         || mc.options.keyJump.isDown()
         || mc.options.keyShift.isDown();
   }

   private static void load(Minecraft mc) {
      if (mc.options != null) {
         mc.options.keyUp.setDown(false);
         mc.options.keyDown.setDown(false);
         mc.options.keyLeft.setDown(false);
         mc.options.keyRight.setDown(false);
         mc.options.keyJump.setDown(false);
         mc.options.keyShift.setDown(false);
         bA.read();
      }
   }

   private static void read(Minecraft mc) {
      if (mc != null && mc.options != null) {
         aB.init(mc.options.keyUp).autism$resetPressedState();
         aB.init(mc.options.keyDown).autism$resetPressedState();
         aB.init(mc.options.keyLeft).autism$resetPressedState();
         aB.init(mc.options.keyRight).autism$resetPressedState();
         aB.init(mc.options.keyJump).autism$resetPressedState();
         aB.init(mc.options.keyShift).autism$resetPressedState();
      }
   }

   private static String init_Str_status() {
      synchronized (init_Obj) {
         if (init_a_ConfigStore == null) {
            return "No active TP";
         } else {
            return String.format(
               Locale.ROOT,
               "TP %s%s: %.2f %.2f %.2f -> %.2f %.2f %.2f, %d packets, step %.3f",
               "main",
               init_a_ConfigStore.read_Z ? " recovery" : "",
               init_a_ConfigStore.load_Vec3.x,
               init_a_ConfigStore.load_Vec3.y,
               init_a_ConfigStore.load_Vec3.z,
               init_a_ConfigStore.init_Vec3.x,
               init_a_ConfigStore.init_Vec3.y,
               init_a_ConfigStore.init_Vec3.z,
               init_a_ConfigStore.init_I,
               init_a_ConfigStore.init_D
            );
         }
      }
   }

   private static String init_Str_stop(String reason) {
      PkgCls_a.Inner_ConfigStore job;
      synchronized (init_Obj) {
         job = init_a_ConfigStore;
         if (job == null) {
            return "No active TP";
         }
      }

      synchronized (init_Obj) {
         if (init_a_ConfigStore == job) {
            init_a_ConfigStore = null;
         }
      }

      if (job.init_a_NetHelper == PkgCls_a.Inner_NetHelper.init && job.init_LocalPlayer != null) {
         job.init_LocalPlayer.setDeltaMovement(Vec3.ZERO);
      }

      if (job.init_J != 0L) {
         bl.init(job.init_J, bl.Inner_DataUtil.write);
      }

      return "TP stopped";
   }

   private static void init_V_a_ConfigStore_3(PkgCls_a.Inner_ConfigStore job, boolean success, String reason) {
      init_V_a_ConfigStore(job, success ? bl.Inner_DataUtil.load : bl.Inner_DataUtil.read, reason);
   }

   private static void init_V_a_ConfigStore(PkgCls_a.Inner_ConfigStore job, bl.Inner_DataUtil completion, String reason) {
      synchronized (init_Obj) {
         if (init_a_ConfigStore != job) {
            return;
         }

         init_a_ConfigStore = null;
      }

      if (job.init_a_NetHelper == PkgCls_a.Inner_NetHelper.init && init_Z) {
         init_Z = false;
         read(Minecraft.getInstance());
      }

      if (job.init_a_NetHelper == PkgCls_a.Inner_NetHelper.init && job.init_LocalPlayer != null) {
         job.init_LocalPlayer.setDeltaMovement(Vec3.ZERO);
      }

      if (job.init_J != 0L) {
         bl.init(job.init_J, completion);
      }

      boolean success = completion == bl.Inner_DataUtil.load;
      String color = success ? "§a" : "§e";
      AutismClientMessaging.sendPrefixed(color + "TP " + (success ? "complete: " : "stopped: ") + "§f" + reason);
   }

   private static Vec3 init_Vec3_LocalPlayer(LocalPlayer player) {
      Entity vehicle = player.getVehicle();
      return vehicle == null ? player.position() : vehicle.position();
   }

   private static void init_V_LocalPlayer(LocalPlayer player) {
      if (player != null && Minecraft.getInstance().getConnection() != null) {
         boolean sprinting = player.isSprinting();
         player.xxa = 0.0F;
         player.zza = 0.0F;
         player.setJumping(false);
         player.setShiftKeyDown(false);
         player.setSprinting(false);
         player.stopFallFlying();
         player.getAbilities().flying = false;
         F.Inner_StreamUtil view = F.init_F_StreamUtil_Entity(player);
         player.setOnGround(view != null && view.init_Z(player.position()) && view.update(player.position()) && view.read(player.position()));
         player.setDeltaMovement(Vec3.ZERO);
         boolean previousOwnedSend = write();

         try {
            Minecraft.getInstance().getConnection().send(new ServerboundPlayerInputPacket(Input.EMPTY));
            Minecraft.getInstance().getConnection().send(new ServerboundPlayerAbilitiesPacket(player.getAbilities()));
            if (sprinting) {
               Minecraft.getInstance().getConnection().send(new ServerboundPlayerCommandPacket(player, Action.STOP_SPRINTING));
            }
         } finally {
            init_V_Z(previousOwnedSend);
         }
      }
   }

   private static String init_Str_a_ConfigStore(PkgCls_a.Inner_ConfigStore job) {
      return !job.load_Z
         ? String.format(Locale.ROOT, "arrived at %.2f %.2f %.2f", job.load_Vec3.x, job.load_Vec3.y, job.load_Vec3.z)
         : String.format(Locale.ROOT, "adjusted landing %.2f %.2f %.2f", job.load_Vec3.x, job.load_Vec3.y, job.load_Vec3.z);
   }

   private static String init_Str_Vec3(Vec3 destination, int packets, int pause, boolean vehicle) {
      return String.format(
         Locale.ROOT,
         "TP started%s: %.2f %.2f %.2f · %d packets / %d ms",
         vehicle ? " for vehicle" : "",
         destination.x,
         destination.y,
         destination.z,
         packets,
         pause
      );
   }

   private static Double init_Double_Str_D(String token, double origin) {
      if (token != null && !token.isBlank() && token.charAt(0) != '^') {
         try {
            double value;
            if (token.charAt(0) == '~') {
               value = token.length() == 1 ? origin : origin + Double.parseDouble(token.substring(1));
            } else {
               value = Double.parseDouble(token);
            }

            return Double.isFinite(value) ? value : null;
         } catch (NumberFormatException ignored) {
            return null;
         }
      } else {
         return null;
      }
   }

   private static Integer init_Integer_Str(String value, int min, int max) {
      try {
         int parsed = Integer.parseInt(value);
         return parsed >= min && parsed <= max ? parsed : null;
      } catch (NumberFormatException ignored) {
         return null;
      }
   }

   private static int init_I_I(int value, int min, int max) {
      return Math.max(min, Math.min(max, value));
   }

   record Inner_BufferHelper(double init_D, int init_I, boolean init_Z) {
   }

   private record Inner_CachePool(Vec3 init_Vec3, long init_J) {
   }

   private static final class Inner_ConfigStore {
      final PkgCls_a.Inner_NetHelper init_a_NetHelper;
      final LocalPlayer init_LocalPlayer;
      final Object init_Obj;
      final Object load_Obj;
      final Entity init_Entity;
      final String init_Str;
      final Vec3 init_Vec3;
      final PkgCls_a.Inner_JsonUtil init_a_JsonUtil;
      final boolean init_Z;
      final long init_J;
      final long load_J = PkgCls_a.init_AtomicLong.incrementAndGet();
      final Deque<Vec3> init_Deque = new ArrayDeque<>();
      final Deque<PkgCls_a.Inner_TaskQueue> load_Deque = new ArrayDeque<>();
      Vec3 load_Vec3;
      Vec3 read_Vec3;
      Vec3 write_Vec3;
      Vec3 update_Vec3;
      double init_D;
      double load_D;
      int init_I;
      long read_J;
      long write_J = -9223372036854775808L;
      final long update_J = System.currentTimeMillis();
      long sync_J = this.update_J;
      long flush_J = this.update_J;
      int load_I;
      long close;
      long open;
      long poll;
      long push;
      long pop;
      double read_D;
      int read_I;
      int write_I;
      Vec3 sync_Vec3;
      Vec3 flush_Vec3;
      int update_I;
      double write_D = 0.0 / 0.0;
      long tick;
      int sync_I = 12;
      boolean load_Z;
      boolean read_Z;
      boolean write_Z;
      PkgCls_a.Inner_CachePool init_a_CachePool;

      private Inner_ConfigStore(
         PkgCls_a.Inner_NetHelper owner,
         LocalPlayer player,
         Object mainConnection,
         Object level,
         Entity vehicle,
         String dimension,
         Vec3 start,
         PkgCls_a.Inner_StreamUtil request,
         boolean macroOwned,
         long macroId
      ) {
         this.init_a_NetHelper = owner;
         this.init_LocalPlayer = player;
         this.init_Obj = mainConnection;
         this.load_Obj = level;
         this.init_Entity = vehicle;
         this.init_Str = dimension == null ? "" : dimension;
         this.load_Vec3 = start;
         this.read_Vec3 = start;
         this.init_Vec3 = request.init_Vec3();
         this.init_D = 10.0;
         this.load_D = start.distanceTo(request.init_Vec3());
         this.init_a_JsonUtil = new PkgCls_a.Inner_JsonUtil(request.init_I(), request.load());
         this.init_Z = macroOwned;
         this.init_J = macroId;
      }

      static PkgCls_a.Inner_ConfigStore init(LocalPlayer player, Object connection, Object level, Entity vehicle, PkgCls_a.Inner_StreamUtil request) {
         return new PkgCls_a.Inner_ConfigStore(
            PkgCls_a.Inner_NetHelper.init,
            player,
            connection,
            level,
            vehicle,
            "",
            vehicle == null ? player.position() : vehicle.position(),
            request,
            false,
            0L
         );
      }

      static PkgCls_a.Inner_ConfigStore init(
         LocalPlayer player, Object connection, Object level, Entity vehicle, PkgCls_a.Inner_StreamUtil request, long macroId
      ) {
         return new PkgCls_a.Inner_ConfigStore(
            PkgCls_a.Inner_NetHelper.init,
            player,
            connection,
            level,
            vehicle,
            "",
            vehicle == null ? player.position() : vehicle.position(),
            request,
            true,
            macroId
         );
      }
   }

   public enum Inner_DataUtil {
      init,
      load,
      read,
      write,
      update,
      sync,
      flush;
   }

   public static final class Inner_JsonUtil {
      private final int init_I;
      private final int load_I;
      private int read;
      private long init_J;
      private long load_J = -9223372036854775808L;

      public Inner_JsonUtil(int maxPackets, int pauseMs) {
         this.init_I = PkgCls_a.init_I_I(maxPackets, 1, 100);
         this.load_I = PkgCls_a.init_I_I(pauseMs, 50, 10000);
      }

      public boolean init_Z(long tick, long now) {
         if (tick != this.load_J && now >= this.init_J) {
            this.load_J = tick;
            return true;
         } else {
            return false;
         }
      }

      public void init_V(long now) {
         this.read++;
         if (this.read >= this.init_I) {
            this.read = 0;
            this.init_J = now + this.load_I;
         }
      }

      public boolean init_Z(long now) {
         return now < this.init_J;
      }

      public int init_I() {
         return Math.max(1, this.init_I - this.read);
      }

      public int load() {
         return this.read;
      }

      public long init_J() {
         return this.init_J;
      }

      public void init_V(long now, long delayMs) {
         this.read = 0;
         this.init_J = now + Math.max(0L, delayMs);
         this.load_J = -9223372036854775808L;
      }
   }

   private enum Inner_NetHelper {
      init,
      load;
   }

   public record Inner_StateCache(boolean init_Z, String init_Str) {
   }

   public record Inner_StreamUtil(PkgCls_a.Inner_DataUtil init_a_DataUtil, Vec3 init_Vec3, int init_I, int load, String init_Str) {
      static PkgCls_a.Inner_StreamUtil init(String message) {
         return new PkgCls_a.Inner_StreamUtil(PkgCls_a.Inner_DataUtil.flush, null, 0, 0, message);
      }
   }

   private record Inner_TaskQueue(Vec3 init_Vec3, boolean init_Z, boolean load, List<A.Inner_DataUtil> init_List) {
      Inner_TaskQueue {
         init_List = init_List == null ? List.of() : List.copyOf(init_List);
      }

      static PkgCls_a.Inner_TaskQueue init(Vec3 position, boolean onGround) {
         return new PkgCls_a.Inner_TaskQueue(position, false, onGround, List.of());
      }

      static PkgCls_a.Inner_TaskQueue init(Vec3 from, Vec3 to, boolean onGround) {
         return new PkgCls_a.Inner_TaskQueue(to, true, onGround, A.init(from, to, onGround));
      }
   }
}

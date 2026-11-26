package autismclient.util;

import autismclient.AutismClientAddon;
import autismclient.util.macro.MacroExecutor;
import autismclient.util.macro.MacroWaitRegistry;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.LockSupport;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.TagParser;

public class aE {
   private static volatile aE init_aE;
   private static final int init_I = 60000;
   private static final int load_I = 32000;
   private static final long init_J = 120000L;
   private static final int read_I = 10;
   private static final String init_Str = "__QUEUE_FLUSH__";
   private static final String load_Str = "__DELAY_PACKETS__\t";
   private final AtomicBoolean init_AtomicBoolean = new AtomicBoolean(false);
   private volatile boolean init_Z;
   private volatile String read_Str;
   private volatile boolean load_Z = false;
   private volatile String write_Str = "";
   private final Map<String, aE.Inner_DataUtil> init_Map = new ConcurrentHashMap<>();
   private final Map<String, String> load_Map = new ConcurrentHashMap<>();
   private final ExecutorService init_ExecutorService = Executors.newSingleThreadExecutor(r -> {
      Thread t = new Thread(r, "Autism-LAN-Send");
      t.setDaemon(true);
      return t;
   });
   private final Set<String> init_Set = ConcurrentHashMap.newKeySet();
   private volatile boolean read_Z = false;
   private int write_I = 0;
   private static final int update_I = 400;
   private static final int sync_I = 5;
   private volatile Runnable init_Runnable;
   private volatile Runnable load_Runnable;
   private volatile Runnable read_Runnable;
   private volatile Runnable write_Runnable;
   private volatile Runnable update_Runnable;
   private volatile Runnable sync_Runnable;
   private volatile Runnable flush_Runnable;
   private volatile Runnable close_Runnable;
   private volatile ConcurrentHashMap<String, Map<String, AutismMacro>> init_ConcurrentHashMap = new ConcurrentHashMap<>();
   private final Map<String, aE.Inner_BufferHelper> write_Map = new ConcurrentHashMap<>();
   private volatile Map<String, String> update_Map = new ConcurrentHashMap<>();
   private volatile String update_Str = "";
   private volatile Runnable open_Runnable;
   private volatile aE.Inner_TaskQueue init_aE_TaskQueue = null;
   private final CopyOnWriteArrayList<aE.Inner_TaskQueue> init_CopyOnWriteArrayLi = new CopyOnWriteArrayList<>();
   private static final int flush_I = 10;
   private volatile aE.Inner_NetHelper init_aE_NetHelper = null;
   private volatile aE.Inner_JsonUtil init_aE_JsonUtil = aE.Inner_JsonUtil.IDLE;
   private volatile aE.Inner_StreamUtil init_aE_StreamUtil = aE.Inner_StreamUtil.INSTANT;
   private volatile aE.Inner_ConfigStore init_aE_ConfigStore = null;
   private volatile long load_J = -1L;
   private final Map<String, Long> sync_Map = new ConcurrentHashMap<>();
   private final Map<String, Long> flush_Map = new ConcurrentHashMap<>();
   private final Map<String, aE.Inner_StateCache> close_Map = new ConcurrentHashMap<>();
   private final Set<Long> load_Set = ConcurrentHashMap.newKeySet();
   private final Map<String, Integer> open_Map = new ConcurrentHashMap<>();
   private volatile Runnable poll_Runnable;
   private static final int close_I = 25568;
   private ServerSocket init_ServerSocket;
   private Thread init_Thread;
   private final Map<String, Socket> poll_Map = new ConcurrentHashMap<>();
   private Socket init_Socket;
   private Thread load_Thread;
   private final Object init_Obj = new Object();
   private final Map<String, Object> push = new ConcurrentHashMap<>();
   private final Map<String, Long> pop = new ConcurrentHashMap<>();
   private long read_J = 0L;
   private static final long write_J = 9000L;
   private static final long update_J = 15000L;
   private static final long sync_J = 30000L;
   private volatile boolean write_Z = false;
   private volatile String sync_Str = null;
   private volatile String flush_Str = null;
   private final AtomicLong init_AtomicLong = new AtomicLong();

   private aE() {
   }

   public static aE init_aE() {
      aE local = init_aE;
      if (local == null) {
         synchronized (aE.class) {
            if (init_aE == null) {
               init_aE = new aE();
            }

            local = init_aE;
         }
      }

      return local;
   }

   public void init_V() {
      if (!autismclient.modules.aD.read_Z() && !this.init_AtomicBoolean.get()) {
         this.init_AtomicBoolean.set(true);
         AutismClientMessaging.sendPrefixed("§aLAN Sync started (TCP mode on port 25568).");
         AutismClientAddon.LOG.info("[Autism-LAN] Started in TCP-only mode on port {}", 25568);
      }
   }

   public void load_V() {
      if (this.init_AtomicBoolean.get()) {
         if (this.read_Str != null) {
            this.update_V();
         }

         this.init_AtomicBoolean.set(false);
         this.set();
         this.load_Map.clear();
         AutismClientMessaging.sendPrefixed("§eLAN Sync stopped.");
      }
   }

   public void read_V() {
      if (this.read_Str != null) {
         this.write_Z = false;
         this.sync_Str = null;
         this.flush_Str = null;
         this.load(new autismclient.util.d.LanSyncPacket.StateCache(this.read_Str, this.update_Str()));
      }

      this.init_AtomicBoolean.set(false);
      this.set();
      this.load_Map.clear();
   }

   public void write_V() {
      if (!this.init_AtomicBoolean.get()) {
         AutismClientMessaging.sendPrefixed("§cStart LAN Sync first.");
      } else {
         this.open_V();
         this.load_Map.clear();
         if (this.read_Str != null) {
            this.update_V();
         }

         this.prev();
         this.read_Str = this.write_Str();
         this.load_Z = true;
         this.write_Str = this.update_Str();
         if (this.write_Str.isEmpty()) {
            this.write_Str = "Host";
         }

         this.init_Map.put(this.write_Str, new aE.Inner_DataUtil(this.write_Str, true));
         this.put();
         this.stop();
         AutismClientMessaging.sendPrefixed("§aSession created: " + this.read_Str + " [TCP Sync]");
         this.push(this.update_Runnable);
      }
   }

   public void init_V_Str(String targetSessionId) {
      this.init_V_Str_Str(targetSessionId, "127.0.0.1");
   }

   public void init_V_Str_Str(String targetSessionId, String hostIp) {
      if (!this.init_AtomicBoolean.get()) {
         AutismClientMessaging.sendPrefixed("§cStart LAN Sync first.");
      } else if (this.read_Str != null && this.read_Str.equals(targetSessionId)) {
         AutismClientMessaging.sendPrefixed("§cAlready in session " + this.read_Str);
      } else {
         this.open_V();
         this.load_Map.clear();
         if (this.read_Str != null) {
            this.update_V();
         }

         this.prev();
         this.read_Str = targetSessionId;
         this.flush_Str = targetSessionId;
         this.load_Z = false;
         this.write_Str = this.update_Str();
         if (this.write_Str.isEmpty()) {
            this.write_Str = "Player";
         }

         this.init_Map.put(this.write_Str, new aE.Inner_DataUtil(this.write_Str, false));
         this.put();
         if (hostIp != null && !hostIp.isBlank() && !read_Z_Str(hostIp)) {
            AutismClientMessaging.sendPrefixed("§cLAN Sync is same-machine only; ignoring remote host " + hostIp + ".");
            AutismClientAddon.LOG.warn("[TCP] Refused non-loopback LAN Sync host {}", hostIp);
         } else {
            this.sync_Str = "127.0.0.1";
            this.close(this.sync_Str);
            AutismClientAddon.LOG.info("[TCP] Connecting to {}:{}", this.sync_Str, 25568);
            this.push(this.init_Runnable);
            this.push(this.update_Runnable);
         }
      }
   }

   private static boolean read_Z_Str(String host) {
      String h = host.trim().toLowerCase(Locale.ROOT);
      if (!h.equals("localhost") && !h.equals("::1") && !h.equals("0:0:0:0:0:0:0:1")) {
         try {
            for (InetAddress addr : InetAddress.getAllByName(h)) {
               if (!addr.isLoopbackAddress()) {
                  return false;
               }
            }

            return true;
         } catch (UnknownHostException ex) {
            return false;
         }
      } else {
         return true;
      }
   }

   public void update_V() {
      if (this.read_Str != null) {
         this.write_Z = false;
         this.sync_Str = null;
         this.flush_Str = null;
         this.load(new autismclient.util.d.LanSyncPacket.StateCache(this.read_Str, this.update_Str()));
         this.set();
         AutismClientMessaging.sendPrefixed("§eLeft session.");
         this.push(this.update_Runnable);
      }
   }

   private void set() {
      this.read_Str = null;
      this.load_Z = false;
      this.write_Str = "";
      this.write_Z = false;
      this.sync_Str = null;
      this.flush_Str = null;
      this.init_Map.clear();
      this.init_ConcurrentHashMap.clear();
      this.init_AtomicLong.incrementAndGet();
      this.update_Map.clear();
      this.update_Str = "";
      this.read_Z = false;
      this.write_I = 0;
      this.init_aE_TaskQueue = null;
      this.load_J = -1L;
      this.put();
      this.pop.clear();
      this.prev();
   }

   public void sync_V() {
      if (this.read_Str != null) {
         this.put();
      }
   }

   private void put() {
      this.init_aE_NetHelper = null;
      this.init_V_aE_JsonUtil(aE.Inner_JsonUtil.IDLE);
      this.sync_Map.clear();
      this.flush_Map.clear();
      this.close_Map.clear();
      this.init_aE_ConfigStore = null;
      this.load_Set.clear();
      this.init_Set.clear();
      this.open_Map.clear();
   }

   private void init_V_aE_JsonUtil(aE.Inner_JsonUtil state) {
      this.init_aE_JsonUtil = state;
      this.push(this.sync_Runnable);
   }

   private void init_V_Z(boolean isMacro, String macroName, String command) {
      this.init(isMacro, macroName, command, null);
   }

   private void init(boolean isMacro, String macroName, String command, Map<String, String> macroAssignments) {
      if (this.open_Z()) {
         if (!this.close_Z()) {
            AutismClientMessaging.sendPrefixed("§cLAN Sync is busy with another synchronized action.");
         } else {
            long executionId = System.nanoTime();
            if (isMacro) {
               if (macroAssignments != null && !macroAssignments.isEmpty()) {
                  Map<String, String> missingAssignments = this.init_Map_Map(macroAssignments);
                  if (!missingAssignments.isEmpty()) {
                     AutismClientMessaging.sendPrefixed("§cMissing assigned macros on: " + this.init_Str_Map(missingAssignments));
                     return;
                  }
               } else if (macroName != null && !macroName.isBlank()) {
                  List<String> missingPeers = this.init_List_Str(macroName);
                  if (!missingPeers.isEmpty()) {
                     AutismClientMessaging.sendPrefixed("§cMissing '" + macroName + "' on: " + this.init_Str_List(missingPeers));
                     return;
                  }
               }
            }

            String myMacroName = macroName;
            if (macroAssignments != null && macroAssignments.containsKey(this.write_Str)) {
               myMacroName = macroAssignments.get(this.write_Str);
            }

            AutismMacro macro = null;
            if (isMacro && myMacroName != null) {
               macro = aN.init_aN().init_aM_Str(myMacroName);
               if (macro == null) {
                  AutismClientMessaging.sendPrefixed("§cMacro not found: " + myMacroName);
                  return;
               }

               macro.load_V();
            }

            aE.Inner_StreamUtil method = this.init_aE_StreamUtil;
            this.init_aE_NetHelper = new aE.Inner_NetHelper(executionId, isMacro, myMacroName, command, macro, true, method, -1L);
            this.init_Set.clear();
            this.sync_Map.clear();
            this.flush_Map.clear();
            this.close_Map.clear();
            this.load_Set.clear();
            this.init_aE_ConfigStore = null;
            this.open_Map.clear();
            this.init_Set.add(this.write_Str);
            this.init_V_aE_JsonUtil(aE.Inner_JsonUtil.PREPARING);
            AutismClientAddon.LOG.info("[Sync] Initiating GO-sync: execId={}", executionId);
            String assignmentStr = "";
            if (macroAssignments != null && !macroAssignments.isEmpty()) {
               StringBuilder sb = new StringBuilder();

               for (Entry<String, String> entry : macroAssignments.entrySet()) {
                  if (sb.length() > 0) {
                     sb.append('\t');
                  }

                  sb.append(entry.getKey()).append('\t').append(entry.getValue());
               }

               assignmentStr = sb.toString();
            }

            autismclient.util.d.LanSyncPacket.HashUtil preparePacket = new autismclient.util.d.LanSyncPacket.HashUtil(
               this.read_Str, executionId, isMacro, macroName != null ? macroName : "", command, this.write_Str
            );
            preparePacket.update = assignmentStr;
            preparePacket.init_I = method.ordinal();
            preparePacket.load_J = -1L;
            this.load(preparePacket);
            this.init_V_aE_JsonUtil_Str(aE.Inner_JsonUtil.PREPARING, this.init_Set.size() + "/" + this.init_Map.size() + " ready");
            (new Thread(
                  () -> {
                     Thread.currentThread().setPriority(10);

                     try {
                        long waitStart = System.currentTimeMillis();
                        int expectedClients = this.init_Map.size();

                        while (this.init_Set.size() < expectedClients) {
                           if (System.currentTimeMillis() - waitStart > 3000L) {
                              AutismClientAddon.LOG.warn("[Sync] Timeout waiting for clients. Got {}/{}", this.init_Set.size(), expectedClients);
                              break;
                           }

                           LockSupport.parkNanos(500000L);
                        }

                        this.init_V_aE_JsonUtil(aE.Inner_JsonUtil.ALL_READY);
                        this.init_V_aE_JsonUtil_Str(aE.Inner_JsonUtil.ALL_READY, this.init_Set.size() + "/" + expectedClients + " ready");
                        AutismClientAddon.LOG.info("[Sync] All {} clients ready!", this.init_Set.size());
                        this.init_V_aE_JsonUtil(aE.Inner_JsonUtil.DISPATCHING_GO);
                        long targetTick = -1L;
                        if (method == aE.Inner_StreamUtil.TICK) {
                           long currentTick = this.load_J();
                           if (currentTick < 0L) {
                              AutismClientMessaging.sendPrefixed("§cCannot tick-sync without a loaded world.");
                              this.init_V_aE_JsonUtil(aE.Inner_JsonUtil.IDLE);
                              return;
                           }

                           targetTick = currentTick + 10L;
                           aE.Inner_NetHelper ctx = this.init_aE_NetHelper;
                           if (ctx != null && ctx.init_J == executionId) {
                              ctx.load_J = targetTick;
                           }
                        }

                        byte[] goPayload = this.init_BArr_DataUtil(
                           new autismclient.util.d.LanSyncPacket.TaskQueue(this.read_Str, executionId, method.ordinal(), targetTick)
                        );
                        this.init_V_aE_JsonUtil(aE.Inner_JsonUtil.EXECUTING);

                        for (Entry<String, Socket> entryx : this.poll_Map.entrySet()) {
                           Socket clientSocket = entryx.getValue();
                           String clientName = entryx.getKey();

                           try {
                              if (!clientSocket.isClosed()) {
                                 synchronized (this.push.computeIfAbsent(clientName, k -> new Object())) {
                                    OutputStream out = clientSocket.getOutputStream();
                                    out.write(goPayload);
                                    out.flush();
                                 }
                              }
                           } catch (IOException e) {
                              AutismClientAddon.LOG.warn("[Sync] Failed to send GO to {}", clientName);
                           }
                        }

                        if (method == aE.Inner_StreamUtil.TICK) {
                           this.init_V_J_J(executionId, targetTick);
                        } else {
                           int hostOffset = this.load_I(this.write_Str);
                           if (hostOffset > 0) {
                              try {
                                 Thread.sleep(hostOffset);
                              } catch (InterruptedException var17) {
                              }
                           }

                           this.clear();
                        }
                     } catch (Exception e) {
                        AutismClientAddon.LOG.error("[Sync] Error in GO-sync: ", e);
                        this.init_V_aE_JsonUtil(aE.Inner_JsonUtil.IDLE);
                     }
                  },
                  "Sync-GO-Initiator"
               ) {
                  {
                     this.setDaemon(true);
                  }
               })
               .start();
         }
      }
   }

   private void add() {
      if (this.open_Z()) {
         long executionId = System.nanoTime();
         aE.Inner_StreamUtil method = this.init_aE_StreamUtil;
         this.init_aE_NetHelper = new aE.Inner_NetHelper(executionId, false, null, "__QUEUE_FLUSH__", null, true, method, -1L);
         this.init_Set.clear();
         this.sync_Map.clear();
         this.flush_Map.clear();
         this.close_Map.clear();
         this.load_Set.clear();
         this.init_aE_ConfigStore = null;
         this.init_Set.add(this.write_Str);
         this.init_V_aE_JsonUtil(aE.Inner_JsonUtil.PREPARING);
         AutismClientAddon.LOG.info("[Sync] Initiating queue flush GO-sync: execId={}", executionId);
         autismclient.util.d.LanSyncPacket.HashUtil preparePacket = new autismclient.util.d.LanSyncPacket.HashUtil(
            this.read_Str, executionId, false, "", "__QUEUE_FLUSH__", this.write_Str
         );
         preparePacket.init_I = method.ordinal();
         preparePacket.load_J = -1L;
         this.load(preparePacket);
         this.init_V_aE_JsonUtil_Str(aE.Inner_JsonUtil.PREPARING, this.init_Set.size() + "/" + this.init_Map.size() + " ready");
         (new Thread(() -> {
            Thread.currentThread().setPriority(10);

            try {
               long waitStart = System.currentTimeMillis();
               int expectedClients = this.init_Map.size();

               while (this.init_Set.size() < expectedClients) {
                  if (System.currentTimeMillis() - waitStart > 3000L) {
                     AutismClientAddon.LOG.warn("[Sync] Timeout waiting for clients. Got {}/{}", this.init_Set.size(), expectedClients);
                     break;
                  }

                  LockSupport.parkNanos(500000L);
               }

               this.init_V_aE_JsonUtil(aE.Inner_JsonUtil.ALL_READY);
               this.init_V_aE_JsonUtil_Str(aE.Inner_JsonUtil.ALL_READY, this.init_Set.size() + "/" + expectedClients + " ready");
               this.init_V_aE_JsonUtil(aE.Inner_JsonUtil.DISPATCHING_GO);
               long targetTick = -1L;
               if (method == aE.Inner_StreamUtil.TICK) {
                  long currentTick = this.load_J();
                  if (currentTick < 0L) {
                     AutismClientMessaging.sendPrefixed("§cCannot tick-sync without a loaded world.");
                     this.init_V_aE_JsonUtil(aE.Inner_JsonUtil.IDLE);
                     return;
                  }

                  targetTick = currentTick + 10L;
                  aE.Inner_NetHelper ctx = this.init_aE_NetHelper;
                  if (ctx != null && ctx.init_J == executionId) {
                     ctx.load_J = targetTick;
                  }
               }

               byte[] goPayload = this.init_BArr_DataUtil(new autismclient.util.d.LanSyncPacket.TaskQueue(this.read_Str, executionId, method.ordinal(), targetTick));
               this.init_V_aE_JsonUtil(aE.Inner_JsonUtil.EXECUTING);

               for (Entry<String, Socket> entry : this.poll_Map.entrySet()) {
                  Socket clientSocket = entry.getValue();
                  String clientName = entry.getKey();

                  try {
                     if (!clientSocket.isClosed()) {
                        synchronized (this.push.computeIfAbsent(clientName, k -> new Object())) {
                           OutputStream out = clientSocket.getOutputStream();
                           out.write(goPayload);
                           out.flush();
                        }
                     }
                  } catch (IOException e) {
                     AutismClientAddon.LOG.warn("[Sync] Failed to send GO to {}", clientName);
                  }
               }

               if (method == aE.Inner_StreamUtil.TICK) {
                  this.init_V_J_J(executionId, targetTick);
               } else {
                  int hostOffset = this.load_I(this.write_Str);
                  if (hostOffset > 0) {
                     try {
                        Thread.sleep(hostOffset);
                     } catch (InterruptedException var17) {
                     }
                  }

                  this.clear();
               }
            } catch (Exception e) {
               AutismClientAddon.LOG.error("[Sync] Error in queue GO-sync: ", e);
               this.init_V_aE_JsonUtil(aE.Inner_JsonUtil.IDLE);
            }
         }, "Sync-GO-QueueFlush") {
            {
               this.setDaemon(true);
            }
         }).start();
      }
   }

   private void init(
      long executionId, boolean isMacro, String macroName, String command, String senderUsername, String macroAssignments, int methodOrdinal, long targetTick
   ) {
      if (this.open_Z() && (senderUsername == null || !senderUsername.equals(this.write_Str))) {
         AutismClientAddon.LOG.info("[Sync] Received PREPARE from {}", senderUsername);
         this.open_Map.clear();
         this.close_Map.clear();
         this.load_Set.clear();
         this.init_aE_ConfigStore = null;
         String myMacroName = macroName;
         if (macroAssignments != null && !macroAssignments.isEmpty()) {
            String[] parts = macroAssignments.split("\t");

            for (int pi = 0; pi + 1 < parts.length; pi += 2) {
               if (parts[pi].equals(this.write_Str)) {
                  myMacroName = parts[pi + 1];
                  break;
               }
            }
         }

         AutismMacro macro = null;
         if (isMacro && myMacroName != null && !myMacroName.isEmpty()) {
            macro = aN.init_aN().init_aM_Str(myMacroName);
            if (macro != null) {
               macro.load_V();
            }
         } else if ("__QUEUE_FLUSH__".equals(command)) {
            ck.init_ck().load_V();
         }

         aE.Inner_StreamUtil method = this.init_aE_StreamUtil_I(methodOrdinal);
         this.init_aE_StreamUtil = method;
         this.init_aE_NetHelper = new aE.Inner_NetHelper(executionId, isMacro, myMacroName, command, macro, false, method, targetTick);
         this.init_V_aE_JsonUtil(aE.Inner_JsonUtil.PREPARING);
         this.load(new autismclient.util.d.LanSyncPacket.BufferHelper(this.read_Str, executionId, this.write_Str));
         AutismClientAddon.LOG.info("[Sync] Sent READY signal");
      }
   }

   private void init_V_J_Str(long executionId, String username) {
      aE.Inner_NetHelper ctx = this.init_aE_NetHelper;
      if (ctx != null && executionId == ctx.init_J) {
         this.init_Set.add(username);
         this.init_V_aE_JsonUtil_Str(aE.Inner_JsonUtil.PREPARING, this.init_Set.size() + "/" + this.init_Map.size() + " ready");
         AutismClientAddon.LOG.info("[Sync] Client ready: {} (total: {})", username, this.init_Set.size());
      }
   }

   private void init_V_J(long executionId, int methodOrdinal, long targetTick) {
      if (this.open_Z()) {
         aE.Inner_NetHelper ctx = this.init_aE_NetHelper;
         if (ctx == null || executionId != ctx.init_J) {
            AutismClientAddon.LOG.warn("[Sync] GO for wrong execId, ignoring");
         } else if (!ctx.load_Z) {
            aE.Inner_StreamUtil method = this.init_aE_StreamUtil_I(methodOrdinal);
            ctx.load_J = targetTick;
            this.init_aE_StreamUtil = method;
            if (method == aE.Inner_StreamUtil.TICK && targetTick >= 0L) {
               AutismClientAddon.LOG.info("[Sync] Received GO! Scheduling tick execution for {}", targetTick);
               this.init_V_aE_JsonUtil(aE.Inner_JsonUtil.EXECUTING);
               this.init_V_J_J(executionId, targetTick);
            } else {
               int offset = this.load_I(this.write_Str);
               if (offset > 0) {
                  AutismClientAddon.LOG.info("[Sync] Received GO! Applying {}ms offset before executing.", offset);
                  this.init_V_aE_JsonUtil(aE.Inner_JsonUtil.EXECUTING);

                  try {
                     Thread.sleep(offset);
                  } catch (InterruptedException var10) {
                  }

                  this.clear();
               } else {
                  AutismClientAddon.LOG.info("[Sync] Received GO! Executing immediately.");
                  this.init_V_aE_JsonUtil(aE.Inner_JsonUtil.EXECUTING);
                  this.clear();
               }
            }
         }
      }
   }

   private void clear() {
      if ((AutismLiteVariant.enabled() || autismclient.e.BitSetUtil.init()) && this.open_Z() && !autismclient.modules.aD.read_Z()) {
         aE.Inner_NetHelper ctx = this.init_aE_NetHelper;
         if (ctx != null) {
            Minecraft mc = Minecraft.getInstance();
            if (!mc.isSameThread()) {
               mc.execute(this::clear);
            } else if (this.load_Set.add(ctx.init_J)) {
               long executedAtMs = System.currentTimeMillis();
               long actualTick = this.load_J();
               boolean tickMode = ctx.init_aE_StreamUtil == aE.Inner_StreamUtil.TICK;
               boolean late = tickMode && (actualTick < 0L || ctx.load_J >= 0L && actualTick > ctx.load_J);
               if ("__QUEUE_FLUSH__".equals(ctx.load_Str)) {
                  ClientPacketListener nh = mc.getConnection();
                  if (nh != null) {
                     int count = ck.init_ck().init_I_ClientPacketListen(nh);
                     AutismClientAddon.LOG.info("[Sync] Queue flushed! {} packets sent", count);
                  }
               } else if (ctx.load_Str != null && ctx.load_Str.startsWith("__DELAY_PACKETS__\t")) {
                  boolean enabled = "1".equals(ctx.load_Str.substring("__DELAY_PACKETS__\t".length()));
                  autismclient.modules.ModuleRuntimeManager module = autismclient.modules.ModuleRuntimeManager.init_PoolEntry();
                  int flushed = module.init_I_Z(enabled);
                  module.init(enabled, flushed);
                  AutismClientAddon.LOG.info("[Sync] Delay packets set to {} (flushed={})", enabled, flushed);
               } else if (ctx.load_Str != null && ctx.load_Str.startsWith("__CHAT__\t")) {
                  String chatMsg = ctx.load_Str.substring("__CHAT__\t".length());
                  ClientPacketListener nh = mc.getConnection();
                  if (nh != null) {
                     if (chatMsg.startsWith("/")) {
                        nh.sendCommand(chatMsg.substring(1));
                     } else {
                        nh.sendChat(chatMsg);
                     }

                     AutismClientAddon.LOG.info("[Sync] Chat sent: {}", chatMsg);
                  }
               } else if (ctx.init_Z && ctx.init_aM != null) {
                  ctx.init_aM.init_V_Z(false);
               }

               AutismClientAddon.LOG.info("[Sync] EXECUTED at {} tick {}", executedAtMs, actualTick);
               this.sync_Map.put(this.write_Str, executedAtMs);
               this.flush_Map.put(this.write_Str, System.nanoTime());
               if (tickMode) {
                  this.close_Map.put(this.write_Str, new aE.Inner_StateCache(actualTick, late));
               }

               this.load(
                  new autismclient.util.d.LanSyncPacket.MethodUtil(
                     this.read_Str, this.write_Str, executedAtMs, ctx.init_aE_StreamUtil.ordinal(), ctx.load_J, actualTick, late
                  )
               );
               this.init_V_aE_JsonUtil(aE.Inner_JsonUtil.REPORTING);
               if (ctx.load_Z) {
                  (new Thread(() -> {
                     try {
                        int expected = this.init_Map.size();
                        long start = System.currentTimeMillis();

                        while (this.init_I_aE_StreamUtil(ctx.init_aE_StreamUtil) < expected && System.currentTimeMillis() - start < 1500L) {
                           LockSupport.parkNanos(1000000L);
                        }

                        if (ctx.init_aE_StreamUtil == aE.Inner_StreamUtil.TICK) {
                           this.load(ctx.load_J);
                        } else {
                           this.reset();
                        }
                     } catch (Exception e) {
                        AutismClientAddon.LOG.error("[Sync] Error calculating spread", e);
                     }
                  }, "Sync-SpreadCalc") {
                     {
                        this.setDaemon(true);
                     }
                  }).start();
               }
            }
         }
      }
   }

   private void init(String username, long timestampMs, int methodOrdinal, long targetTick, long actualTick, boolean late) {
      this.sync_Map.put(username, timestampMs);
      this.flush_Map.put(username, System.nanoTime());
      aE.Inner_StreamUtil method = this.init_aE_StreamUtil_I(methodOrdinal);
      if (method == aE.Inner_StreamUtil.TICK) {
         this.close_Map.put(username, new aE.Inner_StateCache(actualTick, late || targetTick >= 0L && actualTick > targetTick));
      }

      AutismClientAddon.LOG
         .info("[Sync] ACK from {}: remoteMs={}, hostReceiptNano={}, tick={}", username, timestampMs, this.flush_Map.get(username), actualTick);
   }

   private long load_J() {
      Minecraft mc = Minecraft.getInstance();
      if (mc.level == null) {
         return -1L;
      }

      if (mc.isSameThread()) {
         this.load_J = mc.level.getGameTime();
      }

      return this.load_J >= 0L ? this.load_J : mc.level.getGameTime();
   }

   private void init_V_J_J(long executionId, long targetTick) {
      aE.Inner_NetHelper ctx = this.init_aE_NetHelper;
      if (ctx != null && ctx.init_J == executionId) {
         ctx.load_J = targetTick;
         long currentTick = this.load_J();
         if (currentTick >= 0L && currentTick < targetTick) {
            aE.Inner_ConfigStore sched = new aE.Inner_ConfigStore(executionId, targetTick);
            if (autismclient.util.macro.ServerTickPhaseTracker.init_Z()) {
               long ticksOut = targetTick - currentTick;
               long now = System.nanoTime();
               long nextTickNano = autismclient.util.macro.ServerTickPhaseTracker.init_J();
               long targetBoundaryNano = nextTickNano + Math.max(0L, ticksOut - 1L) * 50000000L;
               int pingMs = autismclient.util.macro.ServerTickPhaseTracker.init_I();
               long halfPingNano = pingMs * 1000000L / 2L;
               long fireAt = targetBoundaryNano - halfPingNano;
               if (fireAt <= now) {
                  fireAt = now;
               }

               sched.read = fireAt;
               long fireAtFinal = fireAt;
               long execId = executionId;
               Thread t = new Thread(() -> {
                  try {
                     Thread.currentThread().setPriority(10);
                  } catch (Throwable var11x) {
                  }

                  long parkUntil = fireAtFinal - 1000000L;

                  long nowNano;
                  while ((nowNano = System.nanoTime()) < parkUntil) {
                     LockSupport.parkNanos(parkUntil - nowNano);
                  }

                  while (System.nanoTime() < fireAtFinal) {
                  }

                  aE.Inner_NetHelper sc = this.init_aE_NetHelper;
                  if (sc != null && sc.init_J == execId && sc.init_aE_StreamUtil == aE.Inner_StreamUtil.TICK) {
                     aE.Inner_ConfigStore s = this.init_aE_ConfigStore;
                     if (s != null && s.init_J == execId) {
                        this.init_aE_ConfigStore = null;
                        this.init_V_aE_JsonUtil(aE.Inner_JsonUtil.EXECUTING);
                        this.clear();
                     }
                  }
               }, "Sync-TickPrecise-" + executionId);
               sched.init_Thread = t;
               t.setDaemon(true);
               t.start();
            }

            this.init_aE_ConfigStore = sched;
         } else {
            this.init_aE_ConfigStore = null;
            this.clear();
         }
      }
   }

   public void init_V_J(long currentTick) {
      this.load_J = currentTick;
      aE.Inner_ConfigStore scheduled = this.init_aE_ConfigStore;
      if (scheduled != null && currentTick >= scheduled.load) {
         aE.Inner_NetHelper ctx = this.init_aE_NetHelper;
         if (ctx != null && ctx.init_J == scheduled.init_J && ctx.init_aE_StreamUtil == aE.Inner_StreamUtil.TICK) {
            ctx.load_J = scheduled.load;
            this.init_aE_ConfigStore = null;
            this.init_V_aE_JsonUtil(aE.Inner_JsonUtil.EXECUTING);
            this.clear();
         } else {
            this.init_aE_ConfigStore = null;
         }
      }
   }

   private int init_I_aE_StreamUtil(aE.Inner_StreamUtil method) {
      return method == aE.Inner_StreamUtil.TICK ? this.close_Map.size() : this.flush_Map.size();
   }

   private aE.Inner_StreamUtil init_aE_StreamUtil_I(int ordinal) {
      aE.Inner_StreamUtil[] values = aE.Inner_StreamUtil.values();
      return ordinal >= 0 && ordinal < values.length ? values[ordinal] : aE.Inner_StreamUtil.INSTANT;
   }

   private aE.Inner_StreamUtil init_aE_StreamUtil_Str(String name) {
      if (name == null) {
         return aE.Inner_StreamUtil.INSTANT;
      }

      try {
         return aE.Inner_StreamUtil.valueOf(name.trim().toUpperCase(Locale.ROOT));
      } catch (IllegalArgumentException e) {
         return aE.Inner_StreamUtil.INSTANT;
      }
   }

   private void reset() {
      Map<String, Long> timings = this.flush_Map.isEmpty() ? this.sync_Map : this.flush_Map;
      if (timings.size() < 2) {
         this.init_V_aE_JsonUtil(aE.Inner_JsonUtil.DONE);
      } else {
         long earliest = 9223372036854775807L;
         long latest = -9223372036854775808L;
         String earliestClient = "";
         String latestClient = "";

         for (Entry<String, Long> entry : timings.entrySet()) {
            long ts = entry.getValue();
            if (ts < earliest) {
               earliest = ts;
               earliestClient = entry.getKey();
            }

            if (ts > latest) {
               latest = ts;
               latestClient = entry.getKey();
            }
         }

         boolean isNanos = !this.flush_Map.isEmpty();
         long spreadNanos = latest - earliest;
         long totalSpreadMs = isNanos ? spreadNanos / 1000000L : latest - earliest;
         this.init_aE_TaskQueue = new aE.Inner_TaskQueue(isNanos ? spreadNanos : totalSpreadMs * 1000000L, latestClient, timings.size());
         this.init_CopyOnWriteArrayLi.add(this.init_aE_TaskQueue);

         while (this.init_CopyOnWriteArrayLi.size() > 10) {
            this.init_CopyOnWriteArrayLi.remove(0);
         }

         String report;
         if (totalSpreadMs == 0L) {
            report = "§aPerfect sync!";
         } else {
            report = "§e" + latestClient + " §7was §c+" + totalSpreadMs + "ms §7late";
         }

         AutismClientMessaging.sendPrefixed(report);
         this.load(new autismclient.util.d.LanSyncPacket.Inner_DataUtil(this.read_Str, "__SYNC__", report));
         this.load(new autismclient.util.d.LanSyncPacket.Inner_DataUtil(this.read_Str, "__SPREAD_DATA__", spreadNanos + "\t" + latestClient + "\t" + timings.size()));
         this.init_V_aE_JsonUtil_Str(aE.Inner_JsonUtil.DONE, totalSpreadMs + "ms spread");
         this.init_V_aE_JsonUtil(aE.Inner_JsonUtil.DONE);
         this.push(this.flush_Runnable);
         (new Thread(() -> {
            try {
               Thread.sleep(3000L);
            } catch (InterruptedException var2x) {
            }

            if (this.init_aE_JsonUtil == aE.Inner_JsonUtil.DONE) {
               this.init_V_aE_JsonUtil(aE.Inner_JsonUtil.IDLE);
            }
         }, "Sync-ResetIdle") {
            {
               this.setDaemon(true);
            }
         }).start();
      }
   }

   private void load(long targetTick) {
      int expected = this.init_Map.size();
      TreeSet<String> peers = new TreeSet<>(this.init_Map.keySet());
      boolean perfect = targetTick >= 0L && this.close_Map.size() >= expected;
      int maxOffset = 0;
      List<String> offPeers = new ArrayList<>();

      for (String peer : peers) {
         aE.Inner_StateCache report = this.close_Map.get(peer);
         if (report == null) {
            perfect = false;
            offPeers.add(peer + " no report");
         } else {
            long deltaLong = targetTick >= 0L ? report.init_J - targetTick : 0L;
            int delta = (int)Math.max(-2147483648L, Math.min(2147483647L, deltaLong));
            maxOffset = Math.max(maxOffset, Math.abs(delta));
            if (delta != 0 || report.init_Z) {
               perfect = false;
               String direction = delta >= 0 ? "+" : "";
               String timing = delta >= 0 ? "late" : "early";
               offPeers.add(peer + " was " + direction + delta + " tick(s) " + timing + " on tick " + report.init_J + ", target " + targetTick);
            }
         }
      }

      String report;
      if (perfect) {
         report = "§aPerfect sync on server tick " + targetTick;
      } else {
         report = "§eTick sync target " + targetTick + ": §c" + String.join("; ", offPeers);
      }

      this.init_aE_TaskQueue = new aE.Inner_TaskQueue(
         maxOffset * 50000000L,
         perfect ? "" : "tick",
         Math.max(this.close_Map.size(), expected),
         aE.Inner_StreamUtil.TICK,
         targetTick,
         maxOffset,
         perfect,
         report
      );
      this.init_CopyOnWriteArrayLi.add(this.init_aE_TaskQueue);

      while (this.init_CopyOnWriteArrayLi.size() > 10) {
         this.init_CopyOnWriteArrayLi.remove(0);
      }

      AutismClientMessaging.sendPrefixed(report);
      this.load(new autismclient.util.d.LanSyncPacket.Inner_DataUtil(this.read_Str, "__SYNC__", report));
      this.load(
         new autismclient.util.d.LanSyncPacket.Inner_DataUtil(
            this.read_Str,
            "__TICK_SPREAD_DATA__",
            targetTick + "\t" + maxOffset + "\t" + Math.max(this.close_Map.size(), expected) + "\t" + perfect + "\t" + report
         )
      );
      this.init_V_aE_JsonUtil_Str(aE.Inner_JsonUtil.DONE, perfect ? "tick perfect" : maxOffset + " tick offset");
      this.init_V_aE_JsonUtil(aE.Inner_JsonUtil.DONE);
      this.push(this.flush_Runnable);
      (new Thread(() -> {
         try {
            Thread.sleep(3000L);
         } catch (InterruptedException var2) {
         }

         if (this.init_aE_JsonUtil == aE.Inner_JsonUtil.DONE) {
            this.init_V_aE_JsonUtil(aE.Inner_JsonUtil.IDLE);
         }
      }, "Sync-ResetIdle") {
         {
            this.setDaemon(true);
         }
      }).start();
   }

   public void flush_V() {
      if (this.open_Z()) {
         if (this.read_Str == null) {
            AutismClientMessaging.sendPrefixed("§cJoin a session first.");
         } else {
            ck.init_ck().load_V();
            if (this.load_Z) {
               this.add();
            } else {
               autismclient.util.d.LanSyncPacket.BitSetUtil request = new autismclient.util.d.LanSyncPacket.BitSetUtil(
                  this.read_Str, this.write_Str, false, "", "__QUEUE_FLUSH__"
               );
               request.init_I = this.init_aE_StreamUtil.ordinal();
               this.load(request);
               AutismClientMessaging.sendPrefixed("§eRequested sync execution from host...");
            }
         }
      }
   }

   public void init_V_Z(boolean enabled) {
      if (this.open_Z()) {
         if (this.read_Str == null) {
            AutismClientMessaging.sendPrefixed("§cJoin a session first.");
         } else {
            String command = "__DELAY_PACKETS__\t" + (enabled ? "1" : "0");
            if (this.load_Z) {
               this.init_V_Z(false, "", command);
            } else {
               autismclient.util.d.LanSyncPacket.BitSetUtil request = new autismclient.util.d.LanSyncPacket.BitSetUtil(this.read_Str, this.write_Str, false, "", command);
               request.init_I = this.init_aE_StreamUtil.ordinal();
               this.load(request);
               AutismClientMessaging.sendPrefixed("§eRequested sync delay toggle from host...");
            }
         }
      }
   }

   public void load_V(String macroName) {
      if (!this.open_Z()) {
         AutismClientMessaging.sendPrefixed("§cLAN Sync execution requires a connected world.");
      } else if (this.read_Str == null) {
         AutismClientMessaging.sendPrefixed("§cJoin a session first.");
      } else if (!this.close_Z()) {
         AutismClientMessaging.sendPrefixed("§cLAN Sync is busy with another synchronized action.");
      } else if (this.load_Z) {
         this.init_V_Z(true, macroName, "");
      } else {
         autismclient.util.d.LanSyncPacket.BitSetUtil request = new autismclient.util.d.LanSyncPacket.BitSetUtil(this.read_Str, this.write_Str, true, macroName, "");
         request.init_I = this.init_aE_StreamUtil.ordinal();
         this.load(request);
         AutismClientMessaging.sendPrefixed("§eRequested sync execution from host...");
      }
   }

   public void init_V_Map(Map<String, String> assignments) {
      if (this.open_Z()) {
         if (this.read_Str == null) {
            AutismClientMessaging.sendPrefixed("§cJoin a session first.");
         } else if (assignments != null && !assignments.isEmpty()) {
            String defaultMacro = assignments.values().iterator().next();
            if (this.load_Z) {
               this.init(true, defaultMacro, "", assignments);
            } else {
               StringBuilder sb = new StringBuilder();

               for (Entry<String, String> entry : assignments.entrySet()) {
                  if (sb.length() > 0) {
                     sb.append('\t');
                  }

                  sb.append(entry.getKey()).append('\t').append(entry.getValue());
               }

               autismclient.util.d.LanSyncPacket.BitSetUtil rsp = new autismclient.util.d.LanSyncPacket.BitSetUtil(
                  this.read_Str, this.write_Str, true, defaultMacro, "__PER_USER__\t" + sb
               );
               rsp.init_I = this.init_aE_StreamUtil.ordinal();
               this.load(rsp);
               AutismClientMessaging.sendPrefixed("§eRequested per-user sync from host...");
            }
         }
      }
   }

   public void close_V() {
      if (!this.init_AtomicBoolean.get()) {
         AutismClientMessaging.sendPrefixed("§cStart LAN Sync first.");
      } else if (this.write_Z()) {
         AutismClientMessaging.sendPrefixed("§cAlready in session.");
      } else {
         if (this.write_Str.isEmpty()) {
            this.write_Str = this.update_Str();
         }

         this.load_Map.clear();
         this.read_Z = true;
         this.write_I = 400;
         (new Thread(() -> {
            while (this.read_Z && this.write_I > 0) {
               try {
                  Socket searchSocket = new Socket();
                  searchSocket.connect(new InetSocketAddress(InetAddress.getLoopbackAddress(), 25568), 500);
                  searchSocket.setTcpNoDelay(true);
                  DataOutputStream out = new DataOutputStream(searchSocket.getOutputStream());
                  DataInputStream in = new DataInputStream(searchSocket.getInputStream());
                  out.writeInt(autismclient.util.d.LanSyncPacketType.SEARCH_REQUEST.init());
                  out.writeUTF("");
                  out.flush();
                  int typeId = in.readInt();
                  String foundSessionId = in.readUTF();
                  if (typeId == autismclient.util.d.LanSyncPacketType.SESSION.init()) {
                     autismclient.util.d.LanSyncPacket.TokenStore session = new autismclient.util.d.LanSyncPacket.TokenStore();
                     session.init(foundSessionId);
                     session.init(in);
                     this.load_Map.put(foundSessionId, session.load);
                  }

                  searchSocket.close();
                  Thread.sleep(1000L);
               } catch (IOException e) {
                  try {
                     Thread.sleep(1000L);
                  } catch (InterruptedException var7) {
                  }
               } catch (InterruptedException e) {
                  break;
               }
            }
         }, "TCP-Search-Loop") {
            {
               this.setDaemon(true);
            }
         }).start();
      }
   }

   public void open_V() {
      this.read_Z = false;
      this.write_I = 0;
   }

   public void poll() {
      if (this.init_AtomicBoolean.get() || this.read_Str != null) {
         if (this.read_Str != null) {
            long nowMs = System.currentTimeMillis();
            if (nowMs - this.read_J >= 9000L) {
               this.read_J = nowMs;
               this.load(new autismclient.util.d.LanSyncPacket.NetHelper(this.read_Str, this.write_Str, nowMs));
               this.start();
            }
         }

         Minecraft mc = Minecraft.getInstance();
         if (mc.level != null) {
            long currentTick = mc.level.getGameTime();
            if (this.read_Str != null && !this.init_Map.isEmpty() && currentTick % 600L == 37L) {
               this.push();
            }

            if (this.read_Z && this.write_I > 0) {
               this.write_I--;
               if (!this.load_Map.isEmpty()) {
                  String firstSessionId = this.load_Map.keySet().iterator().next();
                  String hostName = this.load_Map.get(firstSessionId);
                  this.read_Z = false;
                  this.write_I = 0;
                  this.init_V_Str(firstSessionId);
                  AutismClientMessaging.sendPrefixed("§aJoined session " + firstSessionId + " (Host: " + hostName + ").");
                  return;
               }

               if (this.write_I == 0) {
                  this.read_Z = false;
                  AutismClientMessaging.sendPrefixed("§cNo sessions found.");
               }
            }
         }
      }
   }

   private void start() {
      long now = System.currentTimeMillis();
      List<String> deadPeers = new ArrayList<>();

      for (Entry<String, Long> entry : this.pop.entrySet()) {
         String peer = entry.getKey();
         if (!peer.equals(this.write_Str)) {
            long elapsed = now - entry.getValue();
            if (elapsed > 30000L) {
               deadPeers.add(peer);
            }
         }
      }

      for (String dead : deadPeers) {
         this.init_Map.remove(dead);
         this.pop.remove(dead);
         this.init_ConcurrentHashMap.remove(dead);
         this.init_AtomicLong.incrementAndGet();
         this.open_Map.remove(dead);
         if (this.load_Z) {
            Socket s = this.poll_Map.remove(dead);
            if (s != null) {
               try {
                  s.close();
               } catch (IOException var9) {
               }
            }

            this.push.remove(dead);
         }

         AutismClientMessaging.sendPrefixed("§e" + dead + " timed out.");
         AutismClientAddon.LOG.info("[Heartbeat] Peer timed out: {}", dead);
         this.push(this.load_Runnable);
         this.push(this.close_Runnable);
         MacroWaitRegistry.update();
      }

      if (this.load_Z && !deadPeers.isEmpty()) {
         this.valid();
      }
   }

   public aE.Inner_CachePool init_aE_CachePool_Str(String username) {
      if (username.equals(this.write_Str)) {
         return aE.Inner_CachePool.HEALTHY;
      }

      Long lastBeat = this.pop.get(username);
      if (lastBeat == null) {
         return aE.Inner_CachePool.UNKNOWN;
      }

      long elapsed = System.currentTimeMillis() - lastBeat;
      return elapsed > 15000L ? aE.Inner_CachePool.STALE : aE.Inner_CachePool.HEALTHY;
   }

   private void init_V_DataUtil(autismclient.util.d.LanSyncPacket packet) {
      try {
         switch (packet.init_StreamUtil()) {
            case SEARCH_REQUEST:
            case PLAYER_OFFSETS:
            case CLIENT_OFFSET_UPDATE:
            case EXECUTE_NOW:
            default:
               break;
            case SESSION:
               if (packet instanceof autismclient.util.d.LanSyncPacket.TokenStore p) {
                  String sid = p.init_Str();
                  String host = p.load;
                  if (!host.equals(this.write_Str) && !this.write_Z()) {
                     boolean isNew = !this.load_Map.containsKey(sid);
                     this.load_Map.put(sid, host);
                     if (isNew) {
                        AutismClientMessaging.sendPrefixed("§eFound session " + sid + " (Host: " + host + ").");
                     }
                  }
               }
               break;
            case JOIN:
               if (packet instanceof autismclient.util.d.LanSyncPacket.JsonUtil && packet.init_Str().equals(this.read_Str)) {
                  autismclient.util.d.LanSyncPacket.JsonUtil p = (autismclient.util.d.LanSyncPacket.JsonUtil)packet;
                  String username = p.load;
                  if (username != null && !username.isEmpty() && !username.equals(this.write_Str) && !this.init_Map.containsKey(username)) {
                     this.init_Map.put(username, new aE.Inner_DataUtil(username, false));
                     this.pop.put(username, System.currentTimeMillis());
                     AutismClientMessaging.sendPrefixed("§e" + username + " joined.");
                     this.sync_Map.clear();
                     if (this.load_Z) {
                        this.valid();
                        this.push();
                     }

                     this.push(this.init_Runnable);
                     this.push(this.close_Runnable);
                  }
               }
               break;
            case CLIENT_LIST:
               if (packet instanceof autismclient.util.d.LanSyncPacket.Inner_StreamUtil && packet.init_Str().equals(this.read_Str)) {
                  autismclient.util.d.LanSyncPacket.Inner_StreamUtil p = (autismclient.util.d.LanSyncPacket.Inner_StreamUtil)packet;
                  if (!this.load_Z) {
                     aE.Inner_StreamUtil hostMethod = this.init_aE_StreamUtil_I(p.init_I);
                     if (this.init_aE_StreamUtil != hostMethod) {
                        this.init_aE_StreamUtil = hostMethod;
                        this.push(this.close_Runnable);
                     }
                  }

                  if (!p.init_List.isEmpty()) {
                     aE.Inner_DataUtil ourInfo = this.init_Map.get(this.write_Str);
                     long now = System.currentTimeMillis();
                     Set<String> listedPeers = new HashSet<>();
                     Set<String> listed = new HashSet<>();

                     for (autismclient.util.d.LanSyncPacket.Inner_StreamUtil.Inner_DataUtil entry : p.init_List) {
                        this.init_Map.put(entry.init_Str, new aE.Inner_DataUtil(entry.init_Str, entry.init_Z));
                        listed.add(entry.init_Str);
                        if (!entry.init_Str.equals(this.write_Str)) {
                           this.pop.put(entry.init_Str, now);
                           listedPeers.add(entry.init_Str);
                        }
                     }

                     if (!listed.contains(this.write_Str) && ourInfo != null) {
                        this.init_Map.put(this.write_Str, ourInfo);
                        listed.add(this.write_Str);
                     }

                     this.init_Map.keySet().retainAll(listed);
                     this.pop.keySet().retainAll(listedPeers);
                  }
               }
               break;
            case LEAVE:
               if (packet instanceof autismclient.util.d.LanSyncPacket.StateCache && packet.init_Str().equals(this.read_Str)) {
                  autismclient.util.d.LanSyncPacket.StateCache p = (autismclient.util.d.LanSyncPacket.StateCache)packet;
                  String username = p.load;
                  if (username != null && !username.isEmpty() && !username.equals(this.write_Str)) {
                     aE.Inner_DataUtil leavingClient = this.init_Map.get(username);
                     boolean leavingWasHost = leavingClient != null && leavingClient.init_Z;
                     this.init_Map.remove(username);
                     this.pop.remove(username);
                     this.init_ConcurrentHashMap.remove(username);
                     this.init_AtomicLong.incrementAndGet();
                     this.open_Map.remove(username);
                     AutismClientMessaging.sendPrefixed("§e" + username + " left.");
                     if (this.load_Z) {
                        Socket s = this.poll_Map.remove(username);
                        if (s != null) {
                           try {
                              s.close();
                           } catch (IOException var10) {
                           }
                        }

                        this.push.remove(username);
                        this.init_V_DataUtil_Str(packet, username);
                        this.valid();
                     }

                     if (leavingWasHost && !this.init_Map.isEmpty()) {
                        String newHostUsername = this.init_Map.keySet().stream().sorted().findFirst().orElse(null);
                        if (newHostUsername != null && newHostUsername.equals(this.write_Str)) {
                           this.load_Z = true;
                           this.init_Map.put(this.write_Str, new aE.Inner_DataUtil(this.write_Str, true));
                           AutismClientMessaging.sendPrefixed("§aYou are now the host!");
                           this.sync_Map.clear();
                           this.valid();
                        }
                     }

                     this.push(this.load_Runnable);
                     this.push(this.close_Runnable);
                     MacroWaitRegistry.update();
                  }
               }
               break;
            case REQUEST_CLIENT_LIST:
               if (this.load_Z) {
                  this.valid();
               }
               break;
            case REQUEST_MACRO:
               this.update(packet);
               break;
            case MACRO_DATA:
               this.read_V_DataUtil(packet);
               break;
            case MACRO_DATA_CHUNK:
               this.write(packet);
               break;
            case MACRO_DELETE:
               this.flush(packet);
               break;
            case MACRO_LIST:
               this.close(packet);
               break;
            case PRESET_DATA:
               this.sync(packet);
               break;
            case QUEUE_SYNC:
               if (packet instanceof autismclient.util.d.LanSyncPacket.PoolEntry && packet.init_Str().equals(this.read_Str)) {
                  autismclient.util.d.LanSyncPacket.PoolEntry qp = (autismclient.util.d.LanSyncPacket.PoolEntry)packet;
                  this.read(qp.load, qp.read);
               }
               break;
            case CHAT_MESSAGE:
               if (packet instanceof autismclient.util.d.LanSyncPacket.Inner_DataUtil && packet.init_Str().equals(this.read_Str)) {
                  autismclient.util.d.LanSyncPacket.Inner_DataUtil cp = (autismclient.util.d.LanSyncPacket.Inner_DataUtil)packet;
                  this.write(cp.load, cp.read);
               }
               break;
            case PREPARE_EXECUTION:
               if (packet instanceof autismclient.util.d.LanSyncPacket.HashUtil && packet.init_Str().equals(this.read_Str)) {
                  autismclient.util.d.LanSyncPacket.HashUtil p = (autismclient.util.d.LanSyncPacket.HashUtil)packet;
                  this.init(p.init_J, p.init_Z, p.load_Str, p.read, p.write, p.update, p.init_I, p.load_J);
               }
               break;
            case CLIENT_READY:
               if (packet instanceof autismclient.util.d.LanSyncPacket.BufferHelper && packet.init_Str().equals(this.read_Str)) {
                  autismclient.util.d.LanSyncPacket.BufferHelper p = (autismclient.util.d.LanSyncPacket.BufferHelper)packet;
                  this.init_V_J_Str(p.init_J, p.load);
               }
               break;
            case GO:
               if (packet instanceof autismclient.util.d.LanSyncPacket.TaskQueue && packet.init_Str().equals(this.read_Str)) {
                  autismclient.util.d.LanSyncPacket.TaskQueue p = (autismclient.util.d.LanSyncPacket.TaskQueue)packet;
                  this.init_V_J(p.init_J, p.init_I, p.load);
               }
               break;
            case TCP_COMMAND_ACK:
               if (packet instanceof autismclient.util.d.LanSyncPacket.MethodUtil && packet.init_Str().equals(this.read_Str)) {
                  autismclient.util.d.LanSyncPacket.MethodUtil p = (autismclient.util.d.LanSyncPacket.MethodUtil)packet;
                  this.init(p.load_Str, p.init_J, p.init_I, p.load_J, p.read, p.init_Z);
               }
               break;
            case HEARTBEAT:
               if (packet instanceof autismclient.util.d.LanSyncPacket.NetHelper && packet.init_Str().equals(this.read_Str)) {
                  autismclient.util.d.LanSyncPacket.NetHelper p = (autismclient.util.d.LanSyncPacket.NetHelper)packet;
                  if (!p.load.equals(this.write_Str)) {
                     this.pop.put(p.load, System.currentTimeMillis());
                     if (this.load_Z) {
                        this.init_V_DataUtil_Str(packet, p.load);
                     }
                  }
               }
               break;
            case REQUEST_SYNC:
               if (packet instanceof autismclient.util.d.LanSyncPacket.BitSetUtil && packet.init_Str().equals(this.read_Str)) {
                  autismclient.util.d.LanSyncPacket.BitSetUtil p = (autismclient.util.d.LanSyncPacket.BitSetUtil)packet;
                  this.init(p.load, p.init_Z, p.read, p.write);
               }
               break;
            case OFFSET_SYNC:
               if (packet instanceof autismclient.util.d.LanSyncPacket.ByteReader && packet.init_Str().equals(this.read_Str)) {
                  autismclient.util.d.LanSyncPacket.ByteReader p = (autismclient.util.d.LanSyncPacket.ByteReader)packet;
                  this.init_V_Str_2(p.load, p.read, p.init_I);
               }
               break;
            case MACRO_STEP_PROGRESS:
               if (packet instanceof autismclient.util.d.LanSyncPacket.TextFormat && packet.init_Str().equals(this.read_Str)) {
                  autismclient.util.d.LanSyncPacket.TextFormat p = (autismclient.util.d.LanSyncPacket.TextFormat)packet;
                  this.init(p.load_Str, p.init_I, p.load_I, p.read);
               }
               break;
            case SYNC_STATE_UPDATE:
               if (packet instanceof autismclient.util.d.LanSyncPacket.FieldCache && packet.init_Str().equals(this.read_Str)) {
                  autismclient.util.d.LanSyncPacket.FieldCache p = (autismclient.util.d.LanSyncPacket.FieldCache)packet;
                  aE.Inner_JsonUtil[] states = aE.Inner_JsonUtil.values();
                  if (p.init_I >= 0 && p.init_I < states.length) {
                     aE.Inner_NetHelper ctx = this.init_aE_NetHelper;
                     if (ctx == null || !ctx.load_Z) {
                        this.init_aE_JsonUtil = states[p.init_I];
                        this.push(this.sync_Runnable);
                     }
                  }
               }
               break;
            case MACRO_ASSIGNMENT_SYNC:
               if (packet instanceof autismclient.util.d.LanSyncPacket.SyncManager && packet.init_Str().equals(this.read_Str)) {
                  autismclient.util.d.LanSyncPacket.SyncManager p = (autismclient.util.d.LanSyncPacket.SyncManager)packet;
                  if (!p.load.equals(this.write_Str)) {
                     this.update(p.load, p.read);
                  }
               }
         }
      } catch (Exception e) {
         AutismClientAddon.LOG.error("[Autism-LAN] Error handling packet: {}", packet.init_StreamUtil(), e);
      }
   }

   private void stop() {
      try {
         this.init_ServerSocket = new ServerSocket();
         this.init_ServerSocket.setReuseAddress(true);
         this.init_ServerSocket.bind(new InetSocketAddress(InetAddress.getLoopbackAddress(), 25568));
         this.init_Thread = new Thread(() -> {
            while (this.init_AtomicBoolean.get() && this.load_Z && this.init_ServerSocket != null && !this.init_ServerSocket.isClosed()) {
               try {
                  Socket clientSocket = this.init_ServerSocket.accept();
                  clientSocket.setTcpNoDelay(true);
                  this.init_V_Socket(clientSocket);
               } catch (IOException e) {
                  if (this.init_AtomicBoolean.get() && this.load_Z) {
                     AutismClientAddon.LOG.warn("[TCP] Accept error: {}", e.getMessage());
                  }
               }
            }
         }, "TCP-Accept");
         this.init_Thread.setDaemon(true);
         this.init_Thread.start();
         AutismClientAddon.LOG.info("[TCP] Server started on port {}", 25568);
      } catch (IOException e) {
         AutismClientAddon.LOG.error("[TCP] Failed to start server: {}", e.getMessage());
      }
   }

   private void init_V_Socket(Socket clientSocket) {
      (new Thread(() -> {
         try {
            DataInputStream in = new DataInputStream(clientSocket.getInputStream());
            DataOutputStream out = new DataOutputStream(clientSocket.getOutputStream());
            int packetType = in.readInt();
            String packetSession = in.readUTF();
            if (packetType == autismclient.util.d.LanSyncPacketType.SEARCH_REQUEST.init()) {
               AutismClientAddon.LOG.info("[TCP] Received search request, responding with session info");
               out.writeInt(autismclient.util.d.LanSyncPacketType.SESSION.init());
               out.writeUTF(this.read_Str);
               new autismclient.util.d.LanSyncPacket.TokenStore(this.read_Str, 0L, this.write_Str).init(out);
               out.flush();
               clientSocket.close();
               return;
            }

            if (packetType != autismclient.util.d.LanSyncPacketType.JOIN.init()) {
               return;
            }

            autismclient.util.d.LanSyncPacket.JsonUtil joinPacket = new autismclient.util.d.LanSyncPacket.JsonUtil();
            joinPacket.init(packetSession);
            joinPacket.init(in);
            String clientUsername = joinPacket.load;
            if (clientUsername == null || clientUsername.isBlank()) {
               AutismClientAddon.LOG.warn("[TCP] Rejected client with empty username");
               clientSocket.close();
               return;
            }

            if (this.read_Str == null || !this.read_Str.equals(packetSession)) {
               AutismClientAddon.LOG.warn("[TCP] Rejected client '{}' with wrong/missing session code", clientUsername);
               clientSocket.close();
               return;
            }

            Socket oldSocket = this.poll_Map.put(clientUsername, clientSocket);
            if (oldSocket != null && oldSocket != clientSocket) {
               try {
                  oldSocket.close();
               } catch (IOException var281) {
               }
            }

            this.push.put(clientUsername, new Object());
            this.pop.put(clientUsername, System.currentTimeMillis());
            if (!clientUsername.equals(this.write_Str) && !this.init_Map.containsKey(clientUsername)) {
               this.init_Map.put(clientUsername, new aE.Inner_DataUtil(clientUsername, false));
               AutismClientMessaging.sendPrefixed("§e" + clientUsername + " joined.");
               this.sync_Map.clear();
               this.valid();
               this.push(this.init_Runnable);
               this.push(this.close_Runnable);
            }

            if (!clientUsername.equals(this.write_Str)) {
               this.load(clientSocket, clientUsername);
               this.read(clientSocket, clientUsername);
            }

            this.init_V_Socket_Str(clientSocket, clientUsername);
            AutismClientAddon.LOG.info("[TCP] Client joined: {}", clientUsername);

            while (this.init_AtomicBoolean.get() && !clientSocket.isClosed()) {
               int nextPacketType = in.readInt();
               String nextPacketSession = in.readUTF();
               autismclient.util.d.LanSyncPacketType type = autismclient.util.d.LanSyncPacketType.init(nextPacketType);
               autismclient.util.d.LanSyncPacket packet = type == null ? null : autismclient.util.d.LanSyncPacket.init(type, nextPacketSession);
               if (packet == null) {
                  throw new IOException("Unknown LAN packet id " + nextPacketType);
               }

               packet.init(in);
               this.init_V_DataUtil(packet);
            }
         } catch (Exception var28) {
            return;
         } finally {
            try {
               clientSocket.close();
            } catch (IOException var27) {
            }

            if (this.init_AtomicBoolean.get() && this.load_Z) {
               String disconnectedUser = null;

               for (Entry<String, Socket> entry : this.poll_Map.entrySet()) {
                  if (entry.getValue() == clientSocket) {
                     disconnectedUser = entry.getKey();
                     break;
                  }
               }

               if (disconnectedUser != null) {
                  this.poll_Map.remove(disconnectedUser);
                  this.push.remove(disconnectedUser);
                  this.init_Map.remove(disconnectedUser);
                  this.init_ConcurrentHashMap.remove(disconnectedUser);
                  this.init_AtomicLong.incrementAndGet();
                  this.pop.remove(disconnectedUser);
                  this.open_Map.remove(disconnectedUser);
                  AutismClientMessaging.sendPrefixed("§e" + disconnectedUser + " disconnected.");
                  AutismClientAddon.LOG.info("[TCP] Client disconnected: {}", disconnectedUser);
                  this.valid();
                  this.push(this.load_Runnable);
                  MacroWaitRegistry.update();
               }
            }
         }
      }, "TCP-Client-Handler") {
         {
            this.setDaemon(true);
         }
      }).start();
   }

   private void init_V_Socket_Str(Socket clientSocket, String clientUsername) {
      try {
         List<autismclient.util.d.LanSyncPacket.Inner_StreamUtil.Inner_DataUtil> entries = new ArrayList<>();

         for (Entry<String, aE.Inner_DataUtil> entry : this.init_Map.entrySet()) {
            entries.add(new autismclient.util.d.LanSyncPacket.Inner_StreamUtil.Inner_DataUtil(entry.getKey(), entry.getValue().init_Z));
         }

         autismclient.util.d.LanSyncPacket.Inner_StreamUtil packet = new autismclient.util.d.LanSyncPacket.Inner_StreamUtil(
            this.read_Str, entries, this.init_aE_StreamUtil.ordinal()
         );
         byte[] data = this.init_BArr_DataUtil(packet);
         synchronized (this.push.computeIfAbsent(clientUsername, k -> new Object())) {
            clientSocket.getOutputStream().write(data);
            clientSocket.getOutputStream().flush();
         }
      } catch (IOException e) {
         AutismClientAddon.LOG.warn("[TCP] Failed to send client list: {}", e.getMessage());
      }
   }

   private void load(Socket clientSocket, String clientUsername) {
      try {
         List<AutismMacro> localMacros = aN.init_aN().init_List();
         Object lock = this.push.computeIfAbsent(clientUsername, k -> new Object());
         OutputStream out = clientSocket.getOutputStream();

         for (AutismMacro macro : localMacros) {
            this.init(out, lock, this.write_Str, macro, (byte)0);
         }

         this.init_V_OutputStream(
            out, lock, new autismclient.util.d.LanSyncPacket.FileCache(this.read_Str, this.write_Str, init_List_List(localMacros))
         );
      } catch (IOException e) {
         AutismClientAddon.LOG.warn("[TCP] Failed to send macro list: {}", e.getMessage());
      }
   }

   private void read(Socket clientSocket, String clientUsername) {
      String session = this.read_Str;
      if (session != null) {
         try {
            Object lock = this.push.computeIfAbsent(clientUsername, k -> new Object());
            OutputStream out = clientSocket.getOutputStream();

            for (String peer : this.init_Map.keySet()) {
               if (!peer.equals(this.write_Str) && !peer.equals(clientUsername)) {
                  List<String> names = new ArrayList<>();
                  Map<String, AutismMacro> peerMacros = this.init_ConcurrentHashMap.get(peer);
                  if (peerMacros != null) {
                     for (AutismMacro macro : peerMacros.values()) {
                        if (macro != null) {
                           this.init(out, lock, peer, macro, (byte)0);
                           names.add(macro.init_Str);
                        }
                     }
                  }

                  if (!names.isEmpty()) {
                     this.init_V_OutputStream(out, lock, new autismclient.util.d.LanSyncPacket.FileCache(session, peer, names));
                  }
               }
            }
         } catch (IOException e) {
            AutismClientAddon.LOG.warn("[TCP] Failed to forward peer rosters: {}", e.getMessage());
         }
      }
   }

   private void close(String hostIp) {
      (new Thread(() -> {
         try {
            this.init_Socket = new Socket();
            this.init_Socket.connect(new InetSocketAddress(InetAddress.getLoopbackAddress(), 25568), 5000);
            this.init_Socket.setTcpNoDelay(true);
            DataOutputStream out = new DataOutputStream(this.init_Socket.getOutputStream());
            autismclient.util.d.LanSyncPacket.JsonUtil joinPacket = new autismclient.util.d.LanSyncPacket.JsonUtil(this.read_Str, this.write_Str);
            synchronized (this.init_Obj) {
               out.writeInt(joinPacket.init_StreamUtil().init());
               out.writeUTF(joinPacket.init_Str());
               joinPacket.init(out);
               out.flush();
            }

            AutismClientAddon.LOG.info("[TCP] Connected to host at {}:{}", hostIp, 25568);
            this.write_Z = false;
            this.load_Thread = new Thread(() -> {
               try {
                  DataInputStream in = new DataInputStream(this.init_Socket.getInputStream());

                  while (this.init_AtomicBoolean.get() && this.init_Socket != null && !this.init_Socket.isClosed()) {
                     int packetType = in.readInt();
                     String packetSession = in.readUTF();
                     autismclient.util.d.LanSyncPacketType type = autismclient.util.d.LanSyncPacketType.init(packetType);
                     autismclient.util.d.LanSyncPacket packet = type == null ? null : autismclient.util.d.LanSyncPacket.init(type, packetSession);
                     if (packet == null) {
                        throw new IOException("Unknown LAN packet id " + packetType);
                     }

                     packet.init(in);
                     this.init_V_DataUtil(packet);
                  }
               } catch (Exception ex) {
                  AutismClientAddon.LOG.warn("[TCP] Disconnected from host: {}", ex.getMessage());
                  this.next();
               }
            }, "TCP-Reader");
            this.load_Thread.setDaemon(true);
            this.load_Thread.start();
            this.load(new autismclient.util.d.LanSyncPacket.TableIndex(this.read_Str));
            this.init_V_DataOutputStream(out);
         } catch (IOException e) {
            AutismClientAddon.LOG.warn("[TCP] Failed to connect to host: {}", e.getMessage());
            this.next();
         }
      }, "TCP-Connect") {
         {
            this.setDaemon(true);
         }
      }).start();
   }

   private void next() {
      if (!this.write_Z && this.init_AtomicBoolean.get() && this.sync_Str != null && this.flush_Str != null && !this.load_Z) {
         this.write_Z = true;
         AutismClientMessaging.sendPrefixed("§eConnection lost. Reconnecting...");
         (new Thread(() -> {
            long backoffMs = 1000L;
            int attempt = 0;

            while (this.write_Z && this.init_AtomicBoolean.get() && this.sync_Str != null) {
               try {
                  Thread.sleep(backoffMs);
               } catch (InterruptedException e) {
                  return;
               }

               if (!this.write_Z || !this.init_AtomicBoolean.get() || this.sync_Str == null) {
                  return;
               }

               AutismClientAddon.LOG.info("[TCP] Reconnect attempt {}...", ++attempt);

               try {
                  Socket testSocket = new Socket();
                  testSocket.connect(new InetSocketAddress(InetAddress.getLoopbackAddress(), 25568), 2000);
                  testSocket.setTcpNoDelay(true);
                  testSocket.close();
                  this.read_Str = this.flush_Str;
                  if (this.write_Str.isEmpty()) {
                     this.write_Str = this.update_Str();
                  }

                  this.init_Map.putIfAbsent(this.write_Str, new aE.Inner_DataUtil(this.write_Str, false));
                  this.close(this.sync_Str);
                  AutismClientMessaging.sendPrefixed("§aReconnected to session!");
                  return;
               } catch (IOException e) {
                  AutismClientAddon.LOG.warn("[TCP] Reconnect attempt {} failed", attempt);
                  backoffMs = Math.min(backoffMs * 2L, 16000L);
               }
            }

            this.write_Z = false;
         }, "TCP-Reconnect") {
            {
               this.setDaemon(true);
            }
         }).start();
      }
   }

   private void init_V_DataOutputStream(DataOutputStream out) {
      try {
         List<AutismMacro> localMacros = aN.init_aN().init_List();
         synchronized (this.init_Obj) {
            for (AutismMacro macro : localMacros) {
               this.init(out, null, this.write_Str, macro, (byte)0);
            }

            this.init_V_OutputStream(
               out, null, new autismclient.util.d.LanSyncPacket.FileCache(this.read_Str, this.write_Str, init_List_List(localMacros))
            );
         }
      } catch (IOException e) {
         AutismClientAddon.LOG.warn("[TCP] Failed to send macro list: {}", e.getMessage());
      }
   }

   private byte[] init_BArr_DataUtil(autismclient.util.d.LanSyncPacket packet) throws IOException {
      ByteArrayOutputStream baos = new ByteArrayOutputStream();
      DataOutputStream out = new DataOutputStream(baos);
      out.writeInt(packet.init_StreamUtil().init());
      out.writeUTF(packet.init_Str());
      packet.init(out);
      return baos.toByteArray();
   }

   private void init(OutputStream out, Object lock, String senderUsername, AutismMacro macro, byte sourceType) throws IOException {
      if (macro != null) {
         String nbtString = macro.init_CompoundTag().toString();

         for (autismclient.util.d.LanSyncPacket packet : this.init(senderUsername, macro.init_Str, nbtString, sourceType)) {
            this.init_V_OutputStream(out, lock, packet);
         }
      }
   }

   private void init_V_OutputStream(OutputStream out, Object lock, autismclient.util.d.LanSyncPacket packet) throws IOException {
      byte[] data = this.init_BArr_DataUtil(packet);
      if (lock != null) {
         synchronized (lock) {
            out.write(data);
            out.flush();
         }
      } else {
         out.write(data);
         out.flush();
      }
   }

   private void load(autismclient.util.d.LanSyncPacket packet) {
      if (!autismclient.modules.aD.read_Z()) {
         try {
            byte[] data = this.init_BArr_DataUtil(packet);
            if (this.load_Z) {
               for (Entry<String, Socket> entry : this.poll_Map.entrySet()) {
                  Socket client = entry.getValue();
                  String clientName = entry.getKey();

                  try {
                     if (!client.isClosed()) {
                        synchronized (this.push.computeIfAbsent(clientName, k -> new Object())) {
                           client.getOutputStream().write(data);
                           client.getOutputStream().flush();
                        }
                     }
                  } catch (IOException var12) {
                  }
               }
            } else {
               synchronized (this.init_Obj) {
                  if (this.init_Socket != null && !this.init_Socket.isClosed()) {
                     this.init_Socket.getOutputStream().write(data);
                     this.init_Socket.getOutputStream().flush();
                  }
               }
            }
         } catch (IOException e) {
            AutismClientAddon.LOG.warn("[TCP] Failed to send packet: {}", e.getMessage());
         }
      }
   }

   private void init_V_DataUtil_Str(autismclient.util.d.LanSyncPacket packet, String excludedUsername) {
      if (!autismclient.modules.aD.read_Z() && this.load_Z) {
         try {
            byte[] data = this.init_BArr_DataUtil(packet);

            for (Entry<String, Socket> entry : this.poll_Map.entrySet()) {
               String clientName = entry.getKey();
               if (excludedUsername == null || !excludedUsername.equals(clientName)) {
                  Socket client = entry.getValue();

                  try {
                     if (!client.isClosed()) {
                        synchronized (this.push.computeIfAbsent(clientName, k -> new Object())) {
                           client.getOutputStream().write(data);
                           client.getOutputStream().flush();
                        }
                     }
                  } catch (IOException var11) {
                  }
               }
            }
         } catch (IOException e) {
            AutismClientAddon.LOG.warn("[TCP] Failed to relay packet: {}", e.getMessage());
         }
      }
   }

   private void prev() {
      try {
         if (this.init_ServerSocket != null) {
            this.init_ServerSocket.close();
            this.init_ServerSocket = null;
         }

         if (this.init_Socket != null) {
            this.init_Socket.close();
            this.init_Socket = null;
         }

         for (Socket s : this.poll_Map.values()) {
            try {
               s.close();
            } catch (IOException var4) {
            }
         }

         this.poll_Map.clear();
         this.push.clear();
         this.write_Map.clear();
      } catch (IOException e) {
         AutismClientAddon.LOG.warn("[TCP] Stop error: {}", e.getMessage());
      }
   }

   private void valid() {
      if (this.load_Z) {
         List<autismclient.util.d.LanSyncPacket.Inner_StreamUtil.Inner_DataUtil> entries = new ArrayList<>();

         for (Entry<String, aE.Inner_DataUtil> entry : this.init_Map.entrySet()) {
            entries.add(new autismclient.util.d.LanSyncPacket.Inner_StreamUtil.Inner_DataUtil(entry.getKey(), entry.getValue().init_Z));
         }

         this.load(new autismclient.util.d.LanSyncPacket.Inner_StreamUtil(this.read_Str, entries, this.init_aE_StreamUtil.ordinal()));
      }
   }

   private void init_V_aE_JsonUtil_Str(aE.Inner_JsonUtil state, String detail) {
      if (this.load_Z) {
         this.load(new autismclient.util.d.LanSyncPacket.FieldCache(this.read_Str, state.ordinal(), detail));
      }
   }

   public void push() {
      String session = this.read_Str;
      if (session != null) {
         List<AutismMacro> localMacros = aN.init_aN().init_List();
         String sender = this.write_Str;
         List<autismclient.util.d.LanSyncPacket> packets = new ArrayList<>();

         for (AutismMacro macro : localMacros) {
            packets.addAll(this.init_List_Str(sender, macro, (byte)0));
         }

         packets.add(new autismclient.util.d.LanSyncPacket.FileCache(session, sender, init_List_List(localMacros)));
         this.init_V_List(packets);
      }
   }

   private void init_V_List(List<autismclient.util.d.LanSyncPacket> packets) {
      if (!packets.isEmpty()) {
         try {
            this.init_ExecutorService.execute(() -> {
               for (autismclient.util.d.LanSyncPacket packet : packets) {
                  this.load(packet);
               }
            });
         } catch (RejectedExecutionException var3) {
         }
      }
   }

   private static List<String> init_List_List(List<AutismMacro> local) {
      List<String> names = new ArrayList<>(local.size());

      for (AutismMacro macro : local) {
         if (macro != null && macro.init_Str != null) {
            names.add(macro.init_Str);
         }
      }

      return names;
   }

   private void init_V_Str(String senderUsername, AutismMacro macro, byte sourceType) {
      for (autismclient.util.d.LanSyncPacket packet : this.init_List_Str(senderUsername, macro, sourceType)) {
         this.load(packet);
      }
   }

   private List<autismclient.util.d.LanSyncPacket> init_List_Str(String senderUsername, AutismMacro macro, byte sourceType) {
      return macro != null && this.read_Str != null ? this.init(senderUsername, macro.init_Str, macro.init_CompoundTag().toString(), sourceType) : List.of();
   }

   private List<autismclient.util.d.LanSyncPacket> init(String senderUsername, String macroName, String nbtData, byte sourceType) {
      String safeSender = senderUsername != null ? senderUsername : "";
      String safeName = macroName != null ? macroName : "";
      String safeNbt = nbtData != null ? nbtData : "";
      List<autismclient.util.d.LanSyncPacket> packets = new ArrayList<>();
      if (read_I_Str(safeNbt) <= 60000) {
         packets.add(new autismclient.util.d.LanSyncPacket.RenderState(this.read_Str, safeSender, safeName, safeNbt, sourceType));
         return packets;
      }

      byte[] bytes = safeNbt.getBytes(StandardCharsets.UTF_8);
      int totalChunks = Math.max(1, (bytes.length + 32000 - 1) / 32000);

      for (int i = 0; i < totalChunks; i++) {
         int start = i * 32000;
         int len = Math.min(32000, bytes.length - start);
         byte[] chunk = new byte[len];
         System.arraycopy(bytes, start, chunk, 0, len);
         packets.add(new autismclient.util.d.LanSyncPacket.UiHelper(this.read_Str, safeSender, safeName, sourceType, i, totalChunks, chunk));
      }

      return packets;
   }

   private static int read_I_Str(String value) {
      if (value == null) {
         return 0;
      }

      int length = 0;

      for (int i = 0; i < value.length(); i++) {
         int c = value.charAt(i);
         if (c >= 1 && c <= 127) {
            length++;
         } else if (c <= 2047) {
            length += 2;
         } else {
            length += 3;
         }
      }

      return length;
   }

   private void read_V_DataUtil(autismclient.util.d.LanSyncPacket packet) {
      this.init_V_DataUtil_Z(packet, true);
   }

   private void init_V_DataUtil_Z(autismclient.util.d.LanSyncPacket packet, boolean relayIfHost) {
      if (packet instanceof autismclient.util.d.LanSyncPacket.RenderState && packet.init_Str().equals(this.read_Str)) {
         autismclient.util.d.LanSyncPacket.RenderState p = (autismclient.util.d.LanSyncPacket.RenderState)packet;
         String sender = p.load;
         if (sender == null || !sender.equals(this.write_Str)) {
            try {
               CompoundTag nbt = TagParser.parseCompoundFully(p.write);
               AutismMacro macro = new AutismMacro();
               macro.init_aM_CompoundTag(nbt);
               String relayNbt = macro.load_CompoundTag().toString();
               if (p.init_B != 1) {
                  this.init_ConcurrentHashMap.computeIfAbsent(sender, k -> new ConcurrentHashMap<>()).put(cZ.init(macro.init_Str), macro);
               }

               this.init_AtomicLong.incrementAndGet();
               if (p.init_B == 2) {
                  this.init_V_aM_Str(macro, sender);
               }

               if (relayIfHost && this.load_Z && sender != null && !sender.isBlank()) {
                  this.init_V_DataUtil_Str(new autismclient.util.d.LanSyncPacket.RenderState(this.read_Str, sender, macro.init_Str, relayNbt, p.init_B), sender);
               }
            } catch (Exception e) {
               AutismClientAddon.LOG.error("[Autism-LAN] Failed to deserialize macro", e);
            }
         }
      }
   }

   private void write(autismclient.util.d.LanSyncPacket packet) {
      if (packet instanceof autismclient.util.d.LanSyncPacket.UiHelper && packet.init_Str().equals(this.read_Str)) {
         autismclient.util.d.LanSyncPacket.UiHelper p = (autismclient.util.d.LanSyncPacket.UiHelper)packet;
         String sender = p.load_Str;
         if ((sender == null || !sender.equals(this.write_Str))
            && p.read_I > 0
            && p.read_I <= 4096
            && p.load_I >= 0
            && p.load_I < p.read_I
            && p.init_BArr != null
            && p.init_BArr.length != 0
            && p.init_BArr.length <= 32000) {
            if (this.load_Z && sender != null && !sender.isBlank()) {
               this.init_V_DataUtil_Str(
                  new autismclient.util.d.LanSyncPacket.UiHelper(this.read_Str, sender, p.read_Str, p.init_B, p.load_I, p.read_I, p.init_BArr), sender
               );
            }

            this.data();
            String key = sender + "\u0000" + p.read_Str + "\u0000" + p.init_B + "\u0000" + p.read_I;
            aE.Inner_BufferHelper transfer = this.write_Map.computeIfAbsent(key, ignored -> new aE.Inner_BufferHelper(p.read_I));
            byte[] complete = transfer.init(p.load_I, p.init_BArr);
            if (complete != null) {
               this.write_Map.remove(key);
               String nbtData = new String(complete, StandardCharsets.UTF_8);
               this.init_V_DataUtil_Z(new autismclient.util.d.LanSyncPacket.RenderState(this.read_Str, sender, p.read_Str, nbtData, p.init_B), false);
            }
         }
      }
   }

   private void data() {
      long now = System.currentTimeMillis();
      this.write_Map.entrySet().removeIf(entry -> now - entry.getValue().init_J > 120000L);
   }

   private void init_V_aM_Str(AutismMacro macro, String sender) {
      if (macro != null && macro.init_Str != null && !macro.init_Str.isBlank()) {
         Minecraft.getInstance().execute(() -> this.load(macro, sender));
      }
   }

   private void load(AutismMacro macro, String sender) {
      aN manager = aN.init_aN();
      AutismMacro existing = manager.init_aM_Str(macro.init_Str);
      if (existing != null) {
         AutismMacro fresh = macro.init_aM_Str(macro.init_Str);
         existing.init_Str = fresh.init_Str;
         existing.load_Str = fresh.load_Str;
         existing.read = fresh.read;
         existing.load_I = fresh.load_I;
         existing.init_List = new ArrayList<>(fresh.init_List);
         existing.init_Z = fresh.init_Z;
         existing.init_I = fresh.init_I;
         manager.load_V();
         AutismClientMessaging.sendPrefixed("§aUpdated macro from " + sender + ": " + existing.init_Str);
      } else {
         AutismMacro installed = macro.init_aM_Str(macro.init_Str);
         manager.init_V_aM(installed);
         AutismClientMessaging.sendPrefixed("§aReceived macro from " + sender + ": " + installed.init_Str);
      }
   }

   private void update(autismclient.util.d.LanSyncPacket packet) {
      if (packet instanceof autismclient.util.d.LanSyncPacket.RingBuffer && packet.init_Str().equals(this.read_Str)) {
         autismclient.util.d.LanSyncPacket.RingBuffer p = (autismclient.util.d.LanSyncPacket.RingBuffer)packet;
         String requestedName = p.load == null ? "" : p.load.trim();
         if (!requestedName.isEmpty()) {
            AutismMacro macro = aN.init_aN().init_aM_Str(requestedName);
            if (macro != null) {
               this.init_V_Str(this.write_Str, macro, (byte)2);
            }
         }
      }
   }

   private void sync(autismclient.util.d.LanSyncPacket packet) {
      if (packet instanceof autismclient.util.d.LanSyncPacket.IoWorker && packet.init_Str().equals(this.read_Str)) {
         autismclient.util.d.LanSyncPacket.IoWorker p = (autismclient.util.d.LanSyncPacket.IoWorker)packet;
         if ((p.load == null || !p.load.equals(this.write_Str))
            && PacketPresetManager.init_bT().init(p.read, p.write, p.load)
            && this.load_Z
            && p.load != null
            && !p.load.isBlank()) {
            this.init_V_DataUtil_Str(new autismclient.util.d.LanSyncPacket.IoWorker(this.read_Str, p.load, p.read, p.write), p.load);
         }
      }
   }

   private void flush(autismclient.util.d.LanSyncPacket packet) {
      if (packet instanceof autismclient.util.d.LanSyncPacket.EventBus && packet.init_Str().equals(this.read_Str)) {
         autismclient.util.d.LanSyncPacket.EventBus p = (autismclient.util.d.LanSyncPacket.EventBus)packet;
         if (p.load == null || !p.load.equals(this.write_Str)) {
            this.init_AtomicLong.incrementAndGet();
            Map<String, AutismMacro> senderMacros = this.init_ConcurrentHashMap.get(p.load);
            if (senderMacros != null) {
               senderMacros.remove(cZ.init(p.read));
            }

            if (this.load_Z && p.load != null && !p.load.isBlank()) {
               this.init_V_DataUtil_Str(new autismclient.util.d.LanSyncPacket.EventBus(this.read_Str, p.load, p.read), p.load);
            }
         }
      }
   }

   private void close(autismclient.util.d.LanSyncPacket packet) {
      if (packet instanceof autismclient.util.d.LanSyncPacket.FileCache && packet.init_Str().equals(this.read_Str)) {
         autismclient.util.d.LanSyncPacket.FileCache p = (autismclient.util.d.LanSyncPacket.FileCache)packet;
         if (p.load != null && !p.load.isBlank() && !p.load.equals(this.write_Str)) {
            Set<String> live = new HashSet<>();

            for (String name : p.init_List) {
               live.add(cZ.init(name));
            }

            this.init_AtomicLong.incrementAndGet();
            Map<String, AutismMacro> senderMacros = this.init_ConcurrentHashMap.get(p.load);
            if (senderMacros != null) {
               senderMacros.keySet().retainAll(live);
            }

            if (this.load_Z) {
               this.init_V_DataUtil_Str(new autismclient.util.d.LanSyncPacket.FileCache(this.read_Str, p.load, new ArrayList<>(p.init_List)), p.load);
            }
         }
      }
   }

   private void read(String queueData, String senderUsername) {
      try {
         if (senderUsername != null && senderUsername.equals(this.write_Str)) {
            return;
         }

         ck.Inner_CachePool.load_V();
         List<ck.Inner_CachePool> receivedQueue = MacroClipboardCodec.init_List(queueData);
         if (receivedQueue == null || receivedQueue.isEmpty()) {
            AutismClientMessaging.sendPrefixed("§cReceived empty queue");
            return;
         }

         ck.init_ck().load_V_List(receivedQueue);
         AutismClientMessaging.sendPrefixed("§aReceived queue: " + receivedQueue.size() + " packets");
      } catch (Exception e) {
         AutismClientAddon.LOG.error("[Autism-LAN] Failed to process queue sync", e);
         AutismClientMessaging.sendPrefixed("§cFailed to receive queue");
      }
   }

   private void write(String senderUsername, String message) {
      String safeSender = senderUsername != null && !senderUsername.isBlank() ? senderUsername : "Peer";
      String safeMessage = message == null ? "" : message.trim();
      if (!safeMessage.isEmpty()) {
         if ("__SYNC__".equals(safeSender)) {
            AutismClientMessaging.sendPrefixed(safeMessage);
         } else if ("__STOP_ALL__".equals(safeSender)) {
            MacroExecutor.read_V();
            AutismClientMessaging.sendPrefixed("§c[LAN] Macros stopped by " + safeMessage);
         } else if ("__SPREAD_DATA__".equals(safeSender)) {
            try {
               String[] parts = safeMessage.split("\t");
               if (parts.length >= 3) {
                  long nanos = Long.parseLong(parts[0]);
                  String lateClient = parts[1];
                  int count = Integer.parseInt(parts[2]);
                  this.init_aE_TaskQueue = new aE.Inner_TaskQueue(nanos, lateClient, count);
                  this.init_CopyOnWriteArrayLi.add(this.init_aE_TaskQueue);

                  while (this.init_CopyOnWriteArrayLi.size() > 10) {
                     this.init_CopyOnWriteArrayLi.remove(0);
                  }

                  this.push(this.flush_Runnable);
               }
            } catch (NumberFormatException var13) {
            }
         } else if ("__TICK_SPREAD_DATA__".equals(safeSender)) {
            try {
               String[] parts = safeMessage.split("\t", 5);
               if (parts.length >= 5) {
                  long targetTick = Long.parseLong(parts[0]);
                  int maxOffset = Integer.parseInt(parts[1]);
                  int count = Integer.parseInt(parts[2]);
                  boolean perfect = Boolean.parseBoolean(parts[3]);
                  String tickReport = parts[4];
                  this.init_aE_TaskQueue = new aE.Inner_TaskQueue(
                     maxOffset * 50000000L, perfect ? "" : "tick", count, aE.Inner_StreamUtil.TICK, targetTick, maxOffset, perfect, tickReport
                  );
                  this.init_CopyOnWriteArrayLi.add(this.init_aE_TaskQueue);

                  while (this.init_CopyOnWriteArrayLi.size() > 10) {
                     this.init_CopyOnWriteArrayLi.remove(0);
                  }

                  this.push(this.flush_Runnable);
               }
            } catch (NumberFormatException var12) {
            }
         } else {
            boolean isExecMethodControl = "__EXEC_METHOD__".equals(safeSender) || safeMessage != null && safeMessage.startsWith("__EXEC_METHOD__:");
            if (!isExecMethodControl) {
               if (!safeSender.equals(this.write_Str)) {
                  AutismClientMessaging.send("[LAN] <" + safeSender + "> " + safeMessage);
               }
            } else {
               String modeName;
               String originator;
               if (safeMessage != null && safeMessage.startsWith("__EXEC_METHOD__:")) {
                  originator = "__EXEC_METHOD__".equals(safeSender) ? "host" : safeSender;
                  modeName = safeMessage.substring("__EXEC_METHOD__:".length());
               } else {
                  originator = "host";
                  modeName = safeMessage;
               }

               aE.Inner_StreamUtil method = this.init_aE_StreamUtil_Str(modeName);
               if (this.load_Z) {
                  if (this.init_aE_JsonUtil != aE.Inner_JsonUtil.IDLE && this.init_aE_JsonUtil != aE.Inner_JsonUtil.DONE) {
                     this.load(
                        new autismclient.util.d.LanSyncPacket.Inner_DataUtil(this.read_Str, this.write_Str, "__EXEC_METHOD__:" + this.init_aE_StreamUtil.name())
                     );
                     return;
                  }

                  this.init_aE_StreamUtil = method;
                  this.valid();
                  this.load(new autismclient.util.d.LanSyncPacket.Inner_DataUtil(this.read_Str, originator, "__EXEC_METHOD__:" + this.init_aE_StreamUtil.name()));
                  if (!originator.equals(this.write_Str)) {
                     AutismClientMessaging.sendPrefixed("§eExecution method set to " + this.init_aE_StreamUtil.name() + " by " + originator);
                  }

                  this.push(this.close_Runnable);
               } else {
                  this.init_aE_StreamUtil = method;
                  if (!originator.equals(this.write_Str)) {
                     AutismClientMessaging.sendPrefixed("§eExecution method set to " + this.init_aE_StreamUtil.name() + " by " + originator);
                  }

                  this.push(this.close_Runnable);
               }
            }
         }
      }
   }

   private void init(String senderUsername, boolean isMacro, String macroName, String command) {
      if (this.open_Z() && senderUsername != null && !senderUsername.equals(this.write_Str)) {
         if (!this.load_Z) {
            AutismClientAddon.LOG.warn("[Sync] Non-host received REQUEST_SYNC from {} - ignoring", senderUsername);
         } else {
            AutismClientAddon.LOG.info("[Sync] Received sync request from {}: isMacro={}, name={}, cmd={}", senderUsername, isMacro, macroName, command);
            if ("__QUEUE_FLUSH__".equals(command)) {
               this.add();
            } else if (command != null && command.startsWith("__DELAY_PACKETS__\t")) {
               this.init_V_Z(false, "", command);
            } else if (command != null && command.startsWith("__PER_USER__\t")) {
               String assignStr = command.substring("__PER_USER__\t".length());
               String[] parts = assignStr.split("\t");
               Map<String, String> assignments = new LinkedHashMap<>();

               for (int pi = 0; pi + 1 < parts.length; pi += 2) {
                  assignments.put(parts[pi], parts[pi + 1]);
               }

               this.init(isMacro, macroName, "", assignments);
            } else {
               this.init_V_Z(isMacro, macroName, command);
            }
         }
      }
   }

   private void init(String senderUsername, int completedStep, int totalSteps, String macroName) {
      if (senderUsername != null && !senderUsername.equals(this.write_Str)) {
         if (this.load_Z) {
            this.load(new autismclient.util.d.LanSyncPacket.TextFormat(this.read_Str, senderUsername, completedStep, totalSteps, macroName));
         }

         if (completedStep < 0) {
            this.open_Map.remove(senderUsername);
         } else {
            this.open_Map.put(senderUsername, completedStep);
         }

         MacroWaitRegistry.update();
         Runnable listener = this.poll_Runnable;
         if (listener != null) {
            listener.run();
         }
      }
   }

   private void init_V_Str_2(String senderUsername, String targetUsername, int offsetMs) {
      if (senderUsername != null && !senderUsername.equals(this.write_Str)) {
         aE.Inner_DataUtil info = this.init_Map.get(targetUsername);
         if (info != null) {
            info.init_I = Math.max(0, offsetMs);
            AutismClientAddon.LOG.info("[Sync] Offset for {} set to {}ms by {}", targetUsername, offsetMs, senderUsername);
         }

         if (this.load_Z) {
            this.load(new autismclient.util.d.LanSyncPacket.ByteReader(this.read_Str, senderUsername, targetUsername, offsetMs));
         }
      }
   }

   public boolean init_Z() {
      return this.init_AtomicBoolean.get();
   }

   private boolean close_Z() {
      return this.init_aE_JsonUtil == aE.Inner_JsonUtil.IDLE || this.init_aE_JsonUtil == aE.Inner_JsonUtil.DONE;
   }

   private boolean open_Z() {
      Minecraft minecraft = Minecraft.getInstance();
      return !this.init_Z
         && !autismclient.modules.aD.read_Z()
         && minecraft != null
         && minecraft.level != null
         && minecraft.player != null
         && minecraft.getConnection() != null;
   }

   public void load(boolean configurationOnly) {
      this.init_Z = configurationOnly;
      if (configurationOnly) {
         this.init_aE_NetHelper = null;
         this.init_aE_ConfigStore = null;
         this.load_Set.clear();
         if (this.init_aE_JsonUtil != aE.Inner_JsonUtil.IDLE && this.init_aE_JsonUtil != aE.Inner_JsonUtil.DONE) {
            this.init_V_aE_JsonUtil(aE.Inner_JsonUtil.IDLE);
         }
      }
   }

   public boolean load_Z() {
      return this.init_Z;
   }

   public boolean read_Z() {
      return this.init_AtomicBoolean.get() && (this.read_Str != null || this.read_Z || this.write_Z);
   }

   public boolean write_Z() {
      return this.read_Str != null;
   }

   public aE.Inner_StreamUtil init_aE_StreamUtil() {
      return this.init_aE_StreamUtil;
   }

   public int init_I() {
      return 10;
   }

   public void init_V_aE_StreamUtil(aE.Inner_StreamUtil method) {
      aE.Inner_StreamUtil next = method != null ? method : aE.Inner_StreamUtil.INSTANT;
      if (this.init_aE_JsonUtil != aE.Inner_JsonUtil.IDLE && this.init_aE_JsonUtil != aE.Inner_JsonUtil.DONE) {
         AutismClientMessaging.sendPrefixed("§cCannot change execution method while LAN Sync is active.");
      } else if (this.read_Str != null && !this.load_Z) {
         this.load(new autismclient.util.d.LanSyncPacket.Inner_DataUtil(this.read_Str, this.write_Str, "__EXEC_METHOD__:" + next.name()));
         AutismClientMessaging.sendPrefixed("§eRequested execution method: " + next.name());
      } else {
         aE.Inner_StreamUtil previous = this.init_aE_StreamUtil;
         this.init_aE_StreamUtil = next;
         if (this.read_Str != null) {
            this.valid();
            this.load(new autismclient.util.d.LanSyncPacket.Inner_DataUtil(this.read_Str, this.write_Str, "__EXEC_METHOD__:" + next.name()));
         }

         if (previous != next) {
            AutismClientMessaging.sendPrefixed("§eExecution method set to " + next.name());
         }

         this.push(this.close_Runnable);
      }
   }

   public void pop() {
      this.init_V_aE_StreamUtil(this.init_aE_StreamUtil == aE.Inner_StreamUtil.INSTANT ? aE.Inner_StreamUtil.TICK : aE.Inner_StreamUtil.INSTANT);
   }

   public void tick() {
      if (this.read_Str != null) {
         if (this.load_Z) {
            this.valid();
         } else {
            this.load(new autismclient.util.d.LanSyncPacket.TableIndex(this.read_Str));
         }
      }
   }

   public boolean update_Z() {
      return this.load_Z;
   }

   public String init_Str() {
      return this.read_Str;
   }

   public int load_I() {
      return this.init_Map.size();
   }

   public boolean sync_Z() {
      return this.read_Z;
   }

   public int read_I() {
      return this.write_I / 20;
   }

   public boolean flush_Z() {
      return this.write_Z;
   }

   public aE.Inner_JsonUtil init_aE_JsonUtil() {
      return this.init_aE_JsonUtil;
   }

   public aE.Inner_NetHelper init_aE_NetHelper() {
      return this.init_aE_NetHelper;
   }

   public aE.Inner_TaskQueue init_aE_TaskQueue() {
      return this.init_aE_TaskQueue;
   }

   public List<aE.Inner_TaskQueue> init_List() {
      return new ArrayList<>(this.init_CopyOnWriteArrayLi);
   }

   public String load_Str() {
      return this.write_Str;
   }

   public Map<String, aE.Inner_DataUtil> init_Map() {
      return new ConcurrentHashMap<>(this.init_Map);
   }

   public void read_V_Str(String macroName) {
      if (this.read_Str != null) {
         this.load(new autismclient.util.d.LanSyncPacket.EventBus(this.read_Str, this.write_Str, macroName));
      }
   }

   public void write(String queueData) {
      if (!this.init_Z && this.write_Z()) {
         this.load(new autismclient.util.d.LanSyncPacket.PoolEntry(this.read_Str, queueData, this.write_Str));
      }
   }

   public void run() {
      if (!this.init_Z && this.write_Z()) {
         MacroExecutor.read_V();
         AutismClientMessaging.sendPrefixed("§c[LAN] Macros stopped by " + this.write_Str);
         this.load(new autismclient.util.d.LanSyncPacket.Inner_DataUtil(this.read_Str, "__STOP_ALL__", this.write_Str));
      }
   }

   public boolean init_Z_Str(String message) {
      if (!this.open_Z()) {
         AutismClientMessaging.sendPrefixed("§cLAN Sync execution requires a connected world.");
         return false;
      }

      String safeMessage = message == null ? "" : message.trim();
      if (safeMessage.isEmpty()) {
         return false;
      }

      if (!this.write_Z()) {
         AutismClientMessaging.sendPrefixed("Join a LAN Sync session first.");
         return false;
      }

      if (!this.close_Z()) {
         AutismClientMessaging.sendPrefixed("§cLAN Sync is busy with another synchronized action.");
         return false;
      }

      String command = "__CHAT__\t" + safeMessage;
      if (this.load_Z) {
         this.init_V_Z(false, null, command);
      } else {
         autismclient.util.d.LanSyncPacket.BitSetUtil request = new autismclient.util.d.LanSyncPacket.BitSetUtil(this.read_Str, this.write_Str, false, "", command);
         request.init_I = this.init_aE_StreamUtil.ordinal();
         this.load(request);
         AutismClientMessaging.sendPrefixed("§eRequested sync chat from host...");
      }

      return true;
   }

   public int write_I() {
      return this.init_Map.size();
   }

   public void init_V_I(int completedStep, int totalSteps, String macroName) {
      if (this.read_Str != null) {
         if (completedStep < 0) {
            this.open_Map.remove(this.write_Str);
         } else {
            this.open_Map.put(this.write_Str, completedStep);
         }

         this.load(new autismclient.util.d.LanSyncPacket.TextFormat(this.read_Str, this.write_Str, completedStep, totalSteps, macroName != null ? macroName : ""));
         MacroWaitRegistry.update();
      }
   }

   public int init_I_Str(String username) {
      return this.open_Map.getOrDefault(username, 0);
   }

   public Map<String, Integer> load_Map() {
      return new ConcurrentHashMap<>(this.open_Map);
   }

   public void get() {
      this.open_Map.clear();
   }

   public void init_V_Runnable(Runnable callback) {
      this.poll_Runnable = callback;
   }

   public long init_J() {
      return this.init_AtomicLong.get();
   }

   public Map<String, Map<String, AutismMacro>> read_Map() {
      return new HashMap<>(this.init_ConcurrentHashMap);
   }

   public List<String> init_List_Str(String macroName) {
      List<String> missing = new ArrayList<>();
      if (macroName != null && !macroName.isBlank()) {
         Map<String, Map<String, AutismMacro>> remote = this.read_Map();

         for (String peer : new TreeSet<>(this.init_Map.keySet())) {
            if (!peer.equals(this.write_Str)) {
               Map<String, AutismMacro> peerMacros = remote.get(peer);
               if (peerMacros == null || !peerMacros.containsKey(cZ.init(macroName))) {
                  missing.add(peer);
               }
            }
         }

         return missing;
      } else {
         return missing;
      }
   }

   public Map<String, String> init_Map_Map(Map<String, String> assignments) {
      Map<String, String> missing = new LinkedHashMap<>();
      if (assignments != null && !assignments.isEmpty()) {
         Map<String, Map<String, AutismMacro>> remote = this.read_Map();

         for (Entry<String, String> entry : new LinkedHashMap<>(assignments).entrySet()) {
            String username = entry.getKey();
            String macroName = entry.getValue();
            if (username != null && !username.isBlank() && macroName != null && !macroName.isBlank()) {
               if (username.equals(this.write_Str)) {
                  if (aN.init_aN().init_aM_Str(macroName) == null) {
                     missing.put(username, macroName);
                  }
               } else {
                  Map<String, AutismMacro> peerMacros = remote.get(username);
                  if (peerMacros == null || !peerMacros.containsKey(cZ.init(macroName))) {
                     missing.put(username, macroName);
                  }
               }
            }
         }

         return missing;
      } else {
         return missing;
      }
   }

   public List<String> load_List() {
      Map<String, String> remoteMacros = new LinkedHashMap<>();

      for (Entry<String, Map<String, AutismMacro>> entry : this.init_ConcurrentHashMap.entrySet()) {
         if (!entry.getKey().equals(this.write_Str)) {
            for (AutismMacro macro : entry.getValue().values()) {
               if (macro != null) {
                  String key = cZ.init(macro.init_Str);
                  remoteMacros.putIfAbsent(key, macro.init_Str);
               }
            }
         }
      }

      return new ArrayList<>(remoteMacros.values());
   }

   public void load(String clientUsername, String macroName) {
      Map<String, AutismMacro> clientMacros = this.init_ConcurrentHashMap.get(clientUsername);
      AutismMacro remoteMacro = clientMacros == null ? null : clientMacros.get(cZ.init(macroName));
      if (remoteMacro == null) {
         AutismClientMessaging.sendPrefixed("§cMacro not found: " + macroName);
      } else {
         String newName = macroName;

         for (int suffix = 1; aN.init_aN().init_aM_Str(newName) != null; suffix++) {
            newName = macroName + " (" + suffix + ")";
         }

         AutismMacro newMacro = new AutismMacro(newName);
         newMacro.init_List.addAll(remoteMacro.init_List);
         newMacro.read = cY.init_Str_Str(remoteMacro.read);
         aN.init_aN().init_V_aM(newMacro);
         aN.init_aN().load_V();
         AutismClientMessaging.sendPrefixed("§aImported macro: " + newName);
         AutismClientAddon.LOG.info("[Autism-LAN] Imported macro '{}' from {}", newName, clientUsername);
      }
   }

   public void load(Map<String, String> assignments) {
      if (this.read_Str != null && this.write_Z()) {
         this.update_Map.clear();
         this.update_Map.putAll(assignments);
         this.update_Str = this.write_Str;
         StringBuilder sb = new StringBuilder();

         for (Entry<String, String> entry : assignments.entrySet()) {
            if (sb.length() > 0) {
               sb.append('\t');
            }

            sb.append(entry.getKey()).append('\t').append(entry.getValue());
         }

         this.load(new autismclient.util.d.LanSyncPacket.SyncManager(this.read_Str, this.write_Str, sb.toString()));
      }
   }

   private void update(String sender, String encoded) {
      this.update_Map.clear();
      this.update_Str = sender;
      if (encoded != null && !encoded.isEmpty()) {
         String[] parts = encoded.split("\t");

         for (int i = 0; i + 1 < parts.length; i += 2) {
            this.update_Map.put(parts[i], parts[i + 1]);
         }
      }

      this.push(this.open_Runnable);
   }

   public Map<String, String> update_Map() {
      return new LinkedHashMap<>(this.update_Map);
   }

   public String read_Str() {
      return this.update_Str;
   }

   public void load(Runnable callback) {
      this.open_Runnable = callback;
   }

   public List<String> load_List(String peerUsername) {
      Map<String, AutismMacro> peerMacros = this.init_ConcurrentHashMap.get(peerUsername);
      if (peerMacros != null && !peerMacros.isEmpty()) {
         List<String> names = new ArrayList<>(peerMacros.size());

         for (AutismMacro macro : peerMacros.values()) {
            if (macro != null) {
               names.add(macro.init_Str);
            }
         }

         return names;
      } else {
         return Collections.emptyList();
      }
   }

   public boolean load_Z(String macroName) {
      if (this.read_Str == null || !this.write_Z()) {
         AutismClientMessaging.sendPrefixed("§cJoin a session first.");
         return false;
      }

      if (macroName != null && !macroName.isBlank()) {
         AutismMacro macro = aN.init_aN().init_aM_Str(macroName);
         if (macro == null) {
            AutismClientMessaging.sendPrefixed("§cMacro not found: " + macroName);
            return false;
         } else {
            this.init_V_Str(this.write_Str, macro, (byte)2);
            AutismClientMessaging.sendPrefixed("§aSent macro to peers: " + macro.init_Str);
            return true;
         }
      } else {
         AutismClientMessaging.sendPrefixed("§cSelect a macro first.");
         return false;
      }
   }

   public void update(String presetName) {
      if (this.read_Str == null) {
         AutismClientMessaging.sendPrefixed("§cNot in session.");
      } else {
         File file = new File(new File(AutismClientAddon.FOLDER, "presets"), presetName + ".json");
         if (!file.exists()) {
            AutismClientMessaging.sendPrefixed("§cPreset not found: " + presetName);
         } else {
            try {
               String json = new String(Files.readAllBytes(file.toPath()), StandardCharsets.UTF_8);
               this.load(new autismclient.util.d.LanSyncPacket.IoWorker(this.read_Str, this.write_Str, presetName, json));
               AutismClientMessaging.sendPrefixed("§aShared preset '" + presetName + "' with LAN.");
            } catch (IOException e) {
               AutismClientMessaging.sendPrefixed("§cFailed to read preset: " + e.getMessage());
            }
         }
      }
   }

   public void sync(String macroName) {
      if (this.read_Str != null && this.write_Z()) {
         this.load(new autismclient.util.d.LanSyncPacket.RingBuffer(this.read_Str, macroName));
         AutismClientMessaging.sendPrefixed("§eRequesting macro: " + macroName);
      } else {
         AutismClientMessaging.sendPrefixed("§cNot in a LAN Sync session!");
      }
   }

   public void init_V_Str_Z(String targetName, boolean isMacro) {
      if (this.write_Z()) {
         if (isMacro) {
            this.load_V(targetName);
         } else {
            this.flush_V();
         }
      }
   }

   private String init_Str_List(List<String> names) {
      if (names != null && !names.isEmpty()) {
         return names.size() <= 3 ? String.join(", ", names) : String.join(", ", names.subList(0, 3)) + " +" + (names.size() - 3) + " more";
      } else {
         return "";
      }
   }

   private String init_Str_Map(Map<String, String> assignments) {
      if (assignments != null && !assignments.isEmpty()) {
         List<String> parts = new ArrayList<>();

         for (Entry<String, String> entry : assignments.entrySet()) {
            parts.add(entry.getKey() + "=" + entry.getValue());
            if (parts.size() == 3 && assignments.size() > 3) {
               break;
            }
         }

         if (assignments.size() > 3) {
            parts.add("+" + (assignments.size() - 3) + " more");
         }

         return String.join(", ", parts);
      } else {
         return "";
      }
   }

   public void read_V_Runnable(Runnable callback) {
      this.init_Runnable = callback;
   }

   public void write(Runnable callback) {
      this.load_Runnable = callback;
   }

   public void update(Runnable callback) {
      this.read_Runnable = callback;
   }

   public void sync(Runnable callback) {
      this.write_Runnable = callback;
   }

   public void flush(Runnable callback) {
      this.update_Runnable = callback;
   }

   public void close(Runnable callback) {
      this.sync_Runnable = callback;
   }

   public void open(Runnable callback) {
      this.flush_Runnable = callback;
   }

   public void poll(Runnable callback) {
      this.close_Runnable = callback;
   }

   private void push(Runnable callback) {
      if (callback != null) {
         Minecraft.getInstance().execute(callback);
      }
   }

   private String write_Str() {
      return String.format("%04d", (int)(Math.random() * 10000.0));
   }

   private String update_Str() {
      Minecraft mc = Minecraft.getInstance();
      return mc.player != null ? mc.player.getName().getString() : "Player";
   }

   public void init_V_Str_I(String username, int offsetMs) {
      aE.Inner_DataUtil info = this.init_Map.get(username);
      if (info != null) {
         int clamped = Math.max(0, offsetMs);
         info.init_I = clamped;
         if (this.read_Str != null) {
            this.load(new autismclient.util.d.LanSyncPacket.ByteReader(this.read_Str, this.write_Str, username, clamped));
         }
      }
   }

   public int load_I(String username) {
      aE.Inner_DataUtil info = this.init_Map.get(username);
      return info != null ? info.init_I : 0;
   }

   public void flush(String message) {
      this.init_Z_Str(message);
   }

   private static final class Inner_BufferHelper {
      private final long init_J = System.currentTimeMillis();
      private final byte[][] init_BArr2;
      private int init_I;
      private int load;

      private Inner_BufferHelper(int totalChunks) {
         this.init_BArr2 = new byte[totalChunks][];
      }

      private synchronized byte[] init(int index, byte[] data) {
         if (index >= 0 && index < this.init_BArr2.length && data != null) {
            if (this.init_BArr2[index] == null) {
               this.init_BArr2[index] = data;
               this.init_I++;
               this.load += data.length;
            }

            if (this.init_I != this.init_BArr2.length) {
               return null;
            }

            byte[] complete = new byte[this.load];
            int offset = 0;

            for (byte[] chunk : this.init_BArr2) {
               if (chunk == null) {
                  return null;
               }

               System.arraycopy(chunk, 0, complete, offset, chunk.length);
               offset += chunk.length;
            }

            return complete;
         } else {
            return null;
         }
      }
   }

   public enum Inner_CachePool {
      HEALTHY,
      STALE,
      UNKNOWN;
   }

   private static final class Inner_ConfigStore {
      final long init_J;
      final long load;
      volatile long read = 0L;
      volatile Thread init_Thread = null;

      Inner_ConfigStore(long executionId, long targetTick) {
         this.init_J = executionId;
         this.load = targetTick;
      }
   }

   public static class Inner_DataUtil {
      public String init_Str;
      public boolean init_Z;
      public long init_J;
      public int init_I;

      Inner_DataUtil(String username, boolean isHost) {
         this.init_Str = username;
         this.init_Z = isHost;
         this.init_J = System.currentTimeMillis();
         this.init_I = 0;
      }
   }

   public enum Inner_JsonUtil {
      IDLE,
      PREPARING,
      ALL_READY,
      DISPATCHING_GO,
      EXECUTING,
      REPORTING,
      DONE;
   }

   public static class Inner_NetHelper {
      public final long init_J;
      public final boolean init_Z;
      public final String init_Str;
      public final String load_Str;
      public final AutismMacro init_aM;
      public final boolean load_Z;
      public final aE.Inner_StreamUtil init_aE_StreamUtil;
      public volatile long load_J;

      public Inner_NetHelper(long executionId, boolean isMacro, String macroName, String command, AutismMacro macro, boolean isInitiator) {
         this(executionId, isMacro, macroName, command, macro, isInitiator, aE.Inner_StreamUtil.INSTANT, -1L);
      }

      public Inner_NetHelper(
         long executionId,
         boolean isMacro,
         String macroName,
         String command,
         AutismMacro macro,
         boolean isInitiator,
         aE.Inner_StreamUtil executionMethod,
         long targetTick
      ) {
         this.init_J = executionId;
         this.init_Z = isMacro;
         this.init_Str = macroName;
         this.load_Str = command;
         this.init_aM = macro;
         this.load_Z = isInitiator;
         this.init_aE_StreamUtil = executionMethod != null ? executionMethod : aE.Inner_StreamUtil.INSTANT;
         this.load_J = targetTick;
      }
   }

   private static final class Inner_StateCache {
      final long init_J;
      final boolean init_Z;

      Inner_StateCache(long actualTick, boolean late) {
         this.init_J = actualTick;
         this.init_Z = late;
      }
   }

   public enum Inner_StreamUtil {
      INSTANT,
      TICK;
   }

   public static class Inner_TaskQueue {
      public final long init_J;
      public final String init_Str;
      public final int init_I;
      public final aE.Inner_StreamUtil init_aE_StreamUtil;
      public final long load_J;
      public final int load_I;
      public final boolean init_Z;
      public final String load_Str;

      public Inner_TaskQueue(long spreadNanos, String lateClient, int clientCount) {
         this(spreadNanos, lateClient, clientCount, aE.Inner_StreamUtil.INSTANT, -1L, 0, false, "");
      }

      public Inner_TaskQueue(
         long spreadNanos,
         String lateClient,
         int clientCount,
         aE.Inner_StreamUtil executionMethod,
         long targetTick,
         int maxTickOffset,
         boolean perfectTickSync,
         String tickReport
      ) {
         this.init_J = spreadNanos;
         this.init_Str = lateClient;
         this.init_I = clientCount;
         this.init_aE_StreamUtil = executionMethod != null ? executionMethod : aE.Inner_StreamUtil.INSTANT;
         this.load_J = targetTick;
         this.load_I = maxTickOffset;
         this.init_Z = perfectTickSync;
         this.load_Str = tickReport != null ? tickReport : "";
      }

      public double init() {
         return this.init_J / 1000000.0;
      }
   }
}

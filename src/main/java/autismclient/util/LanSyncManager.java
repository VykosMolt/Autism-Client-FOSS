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

public class LanSyncManager {
   private static volatile LanSyncManager instance;
   private static final int MAX_INLINE_MACRO_UTF_BYTES = 60000;
   private static final int MACRO_CHUNK_SIZE_BYTES = 32000;
   private static final long CHUNK_TRANSFER_TIMEOUT_MS = 120000L;
   private static final int TICK_SYNC_LEAD_TICKS = 10;
   private static final String QUEUE_FLUSH_COMMAND = "__QUEUE_FLUSH__";
   private static final String DELAY_PACKETS_PREFIX = "__DELAY_PACKETS__\t";
   private final AtomicBoolean running = new AtomicBoolean(false);
   private volatile boolean configurationOnly;
   private volatile String sessionId;
   private volatile boolean isHost = false;
   private volatile String localUsername = "";
   private final Map<String, LanSyncManager.ClientInfo> connectedClients = new ConcurrentHashMap<>();
   private final Map<String, String> discoveredSessions = new ConcurrentHashMap<>();
   private final ExecutorService asyncSendExecutor = Executors.newSingleThreadExecutor(r -> {
      Thread t = new Thread(r, "Autism-LAN-Send");
      t.setDaemon(true);
      return t;
   });
   private final Set<String> readyClients = ConcurrentHashMap.newKeySet();
   private volatile boolean searching = false;
   private int searchTicksRemaining = 0;
   private static final int SEARCH_DURATION_TICKS = 400;
   private static final int UNUSED_SEARCH_CONSTANT = 5;
   private volatile Runnable onClientJoinedCallback;
   private volatile Runnable onClientLeftCallback;
   private volatile Runnable unusedEventCallback1;
   private volatile Runnable unusedEventCallback2;
   private volatile Runnable onSessionChangedCallback;
   private volatile Runnable onSyncStateChangedCallback;
   private volatile Runnable onSpreadUpdatedCallback;
   private volatile Runnable onPeerStateChangedCallback;
   private volatile ConcurrentHashMap<String, Map<String, AutismMacro>> peerMacros = new ConcurrentHashMap<>();
   private final Map<String, LanSyncManager.ChunkTransfer> incomingChunkTransfers = new ConcurrentHashMap<>();
   private volatile Map<String, String> syncedMacroAssignments = new ConcurrentHashMap<>();
   private volatile String syncedAssignmentsSender = "";
   private volatile Runnable onAssignmentsChangedCallback;
   private volatile LanSyncManager.SpreadResult lastSpreadResult = null;
   private final CopyOnWriteArrayList<LanSyncManager.SpreadResult> spreadHistory = new CopyOnWriteArrayList<>();
   private static final int MAX_SPREAD_HISTORY = 10;
   private volatile LanSyncManager.ExecutionContext activeExecution = null;
   private volatile LanSyncManager.SyncState syncState = LanSyncManager.SyncState.IDLE;
   private volatile LanSyncManager.ExecutionMethod executionMethod = LanSyncManager.ExecutionMethod.INSTANT;
   private volatile LanSyncManager.ScheduledTickExecution scheduledTickExecution = null;
   private volatile long lastKnownGameTick = -1L;
   private final Map<String, Long> executionClientTimestampsMs = new ConcurrentHashMap<>();
   private final Map<String, Long> executionHostReceiptNanos = new ConcurrentHashMap<>();
   private final Map<String, LanSyncManager.TickReport> executionTickReports = new ConcurrentHashMap<>();
   private final Set<Long> executedIds = ConcurrentHashMap.newKeySet();
   private final Map<String, Integer> peerMacroStepProgress = new ConcurrentHashMap<>();
   private volatile Runnable onStepProgressChangedCallback;
   private static final int TCP_PORT = 25568;
   private ServerSocket tcpServerSocket;
   private Thread tcpAcceptThread;
   private final Map<String, Socket> hostClientSockets = new ConcurrentHashMap<>();
   private Socket clientSocket;
   private Thread clientReaderThread;
   private final Object clientWriteLock = new Object();
   private final Map<String, Object> hostClientWriteLocks = new ConcurrentHashMap<>();
   private final Map<String, Long> peerLastHeartbeatMs = new ConcurrentHashMap<>();
   private long lastHeartbeatSentMs = 0L;
   private static final long HEARTBEAT_INTERVAL_MS = 9000L;
   private static final long PEER_STALE_MS = 15000L;
   private static final long PEER_TIMEOUT_MS = 30000L;
   private volatile boolean reconnecting = false;
   private volatile String reconnectHostIp = null;
   private volatile String reconnectSessionId = null;
   private final AtomicLong peerMacrosRevision = new AtomicLong();

   private LanSyncManager() {
   }

   public static LanSyncManager getInstance() {
      LanSyncManager local = instance;
      if (local == null) {
         synchronized (LanSyncManager.class) {
            if (instance == null) {
               instance = new LanSyncManager();
            }

            local = instance;
         }
      }

      return local;
   }

   public void startService() {
      if (!autismclient.modules.aD.read_Z() && !this.running.get()) {
         this.running.set(true);
         AutismClientMessaging.sendPrefixed("§aLAN Sync started (TCP mode on port 25568).");
         AutismClientAddon.LOG.info("[Autism-LAN] Started in TCP-only mode on port {}", 25568);
      }
   }

   public void stopService() {
      if (this.running.get()) {
         if (this.sessionId != null) {
            this.leaveSession();
         }

         this.running.set(false);
         this.resetSessionState();
         this.discoveredSessions.clear();
         AutismClientMessaging.sendPrefixed("§eLAN Sync stopped.");
      }
   }

   public void shutdownSilently() {
      if (this.sessionId != null) {
         this.reconnecting = false;
         this.reconnectHostIp = null;
         this.reconnectSessionId = null;
         this.sendPacket(new autismclient.util.sync.LanSyncPacket.LeavePacket(this.sessionId, this.resolvePlayerUsername()));
      }

      this.running.set(false);
      this.resetSessionState();
      this.discoveredSessions.clear();
   }

   public void createSession() {
      if (!this.running.get()) {
         AutismClientMessaging.sendPrefixed("§cStart LAN Sync first.");
      } else {
         this.cancelSessionSearch();
         this.discoveredSessions.clear();
         if (this.sessionId != null) {
            this.leaveSession();
         }

         this.closeAllSockets();
         this.sessionId = this.generateSessionCode();
         this.isHost = true;
         this.localUsername = this.resolvePlayerUsername();
         if (this.localUsername.isEmpty()) {
            this.localUsername = "Host";
         }

         this.connectedClients.put(this.localUsername, new LanSyncManager.ClientInfo(this.localUsername, true));
         this.clearExecutionState();
         this.startTcpServer();
         AutismClientMessaging.sendPrefixed("§aSession created: " + this.sessionId + " [TCP Sync]");
         this.dispatchCallbackOnClientThread(this.onSessionChangedCallback);
      }
   }

   public void joinSession(String targetSessionId) {
      this.joinSession(targetSessionId, "127.0.0.1");
   }

   public void joinSession(String targetSessionId, String hostIp) {
      if (!this.running.get()) {
         AutismClientMessaging.sendPrefixed("§cStart LAN Sync first.");
      } else if (this.sessionId != null && this.sessionId.equals(targetSessionId)) {
         AutismClientMessaging.sendPrefixed("§cAlready in session " + this.sessionId);
      } else {
         this.cancelSessionSearch();
         this.discoveredSessions.clear();
         if (this.sessionId != null) {
            this.leaveSession();
         }

         this.closeAllSockets();
         this.sessionId = targetSessionId;
         this.reconnectSessionId = targetSessionId;
         this.isHost = false;
         this.localUsername = this.resolvePlayerUsername();
         if (this.localUsername.isEmpty()) {
            this.localUsername = "Player";
         }

         this.connectedClients.put(this.localUsername, new LanSyncManager.ClientInfo(this.localUsername, false));
         this.clearExecutionState();
         if (hostIp != null && !hostIp.isBlank() && !isLoopbackHost(hostIp)) {
            AutismClientMessaging.sendPrefixed("§cLAN Sync is same-machine only; ignoring remote host " + hostIp + ".");
            AutismClientAddon.LOG.warn("[TCP] Refused non-loopback LAN Sync host {}", hostIp);
         } else {
            this.reconnectHostIp = "127.0.0.1";
            this.connectToHost(this.reconnectHostIp);
            AutismClientAddon.LOG.info("[TCP] Connecting to {}:{}", this.reconnectHostIp, 25568);
            this.dispatchCallbackOnClientThread(this.onClientJoinedCallback);
            this.dispatchCallbackOnClientThread(this.onSessionChangedCallback);
         }
      }
   }

   private static boolean isLoopbackHost(String host) {
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

   public void leaveSession() {
      if (this.sessionId != null) {
         this.reconnecting = false;
         this.reconnectHostIp = null;
         this.reconnectSessionId = null;
         this.sendPacket(new autismclient.util.sync.LanSyncPacket.LeavePacket(this.sessionId, this.resolvePlayerUsername()));
         this.resetSessionState();
         AutismClientMessaging.sendPrefixed("§eLeft session.");
         this.dispatchCallbackOnClientThread(this.onSessionChangedCallback);
      }
   }

   private void resetSessionState() {
      this.sessionId = null;
      this.isHost = false;
      this.localUsername = "";
      this.reconnecting = false;
      this.reconnectHostIp = null;
      this.reconnectSessionId = null;
      this.connectedClients.clear();
      this.peerMacros.clear();
      this.peerMacrosRevision.incrementAndGet();
      this.syncedMacroAssignments.clear();
      this.syncedAssignmentsSender = "";
      this.searching = false;
      this.searchTicksRemaining = 0;
      this.lastSpreadResult = null;
      this.lastKnownGameTick = -1L;
      this.clearExecutionState();
      this.peerLastHeartbeatMs.clear();
      this.closeAllSockets();
   }

   public void resetActiveExecution() {
      if (this.sessionId != null) {
         this.clearExecutionState();
      }
   }

   private void clearExecutionState() {
      this.activeExecution = null;
      this.setSyncState(LanSyncManager.SyncState.IDLE);
      this.executionClientTimestampsMs.clear();
      this.executionHostReceiptNanos.clear();
      this.executionTickReports.clear();
      this.scheduledTickExecution = null;
      this.executedIds.clear();
      this.readyClients.clear();
      this.peerMacroStepProgress.clear();
   }

   private void setSyncState(LanSyncManager.SyncState state) {
      this.syncState = state;
      this.dispatchCallbackOnClientThread(this.onSyncStateChangedCallback);
   }

   private void initiateGoSync(boolean isMacro, String macroName, String command) {
      this.initiateGoSync(isMacro, macroName, command, null);
   }

   private void initiateGoSync(boolean isMacro, String macroName, String command, Map<String, String> macroAssignments) {
      if (this.canExecuteInWorld()) {
         if (!this.isSyncIdle()) {
            AutismClientMessaging.sendPrefixed("§cLAN Sync is busy with another synchronized action.");
         } else {
            long executionId = System.nanoTime();
            if (isMacro) {
               if (macroAssignments != null && !macroAssignments.isEmpty()) {
                  Map<String, String> missingAssignments = this.findMissingAssignedMacros(macroAssignments);
                  if (!missingAssignments.isEmpty()) {
                     AutismClientMessaging.sendPrefixed("§cMissing assigned macros on: " + this.formatAssignmentPreview(missingAssignments));
                     return;
                  }
               } else if (macroName != null && !macroName.isBlank()) {
                  List<String> missingPeers = this.findPeersMissingMacro(macroName);
                  if (!missingPeers.isEmpty()) {
                     AutismClientMessaging.sendPrefixed("§cMissing '" + macroName + "' on: " + this.formatNamePreview(missingPeers));
                     return;
                  }
               }
            }

            String myMacroName = macroName;
            if (macroAssignments != null && macroAssignments.containsKey(this.localUsername)) {
               myMacroName = macroAssignments.get(this.localUsername);
            }

            AutismMacro macro = null;
            if (isMacro && myMacroName != null) {
               macro = MacroLibraryManager.getInstance().findByName(myMacroName);
               if (macro == null) {
                  AutismClientMessaging.sendPrefixed("§cMacro not found: " + myMacroName);
                  return;
               }

               macro.regeneratePackets();
            }

            LanSyncManager.ExecutionMethod method = this.executionMethod;
            this.activeExecution = new LanSyncManager.ExecutionContext(executionId, isMacro, myMacroName, command, macro, true, method, -1L);
            this.readyClients.clear();
            this.executionClientTimestampsMs.clear();
            this.executionHostReceiptNanos.clear();
            this.executionTickReports.clear();
            this.executedIds.clear();
            this.scheduledTickExecution = null;
            this.peerMacroStepProgress.clear();
            this.readyClients.add(this.localUsername);
            this.setSyncState(LanSyncManager.SyncState.PREPARING);
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

            autismclient.util.sync.LanSyncPacket.PrepareExecutionPacket preparePacket = new autismclient.util.sync.LanSyncPacket.PrepareExecutionPacket(
               this.sessionId, executionId, isMacro, macroName != null ? macroName : "", command, this.localUsername
            );
            preparePacket.macroAssignments = assignmentStr;
            preparePacket.executionMethod = method.ordinal();
            preparePacket.targetTick = -1L;
            this.sendPacket(preparePacket);
            this.broadcastSyncStateUpdate(LanSyncManager.SyncState.PREPARING, this.readyClients.size() + "/" + this.connectedClients.size() + " ready");
            (new Thread(
                  () -> {
                     Thread.currentThread().setPriority(10);

                     try {
                        long waitStart = System.currentTimeMillis();
                        int expectedClients = this.connectedClients.size();

                        while (this.readyClients.size() < expectedClients) {
                           if (System.currentTimeMillis() - waitStart > 3000L) {
                              AutismClientAddon.LOG.warn("[Sync] Timeout waiting for clients. Got {}/{}", this.readyClients.size(), expectedClients);
                              break;
                           }

                           LockSupport.parkNanos(500000L);
                        }

                        this.setSyncState(LanSyncManager.SyncState.ALL_READY);
                        this.broadcastSyncStateUpdate(LanSyncManager.SyncState.ALL_READY, this.readyClients.size() + "/" + expectedClients + " ready");
                        AutismClientAddon.LOG.info("[Sync] All {} clients ready!", this.readyClients.size());
                        this.setSyncState(LanSyncManager.SyncState.DISPATCHING_GO);
                        long targetTick = -1L;
                        if (method == LanSyncManager.ExecutionMethod.TICK) {
                           long currentTick = this.resolveCurrentGameTick();
                           if (currentTick < 0L) {
                              AutismClientMessaging.sendPrefixed("§cCannot tick-sync without a loaded world.");
                              this.setSyncState(LanSyncManager.SyncState.IDLE);
                              return;
                           }

                           targetTick = currentTick + 10L;
                           LanSyncManager.ExecutionContext ctx = this.activeExecution;
                           if (ctx != null && ctx.executionId == executionId) {
                              ctx.targetTick = targetTick;
                           }
                        }

                        byte[] goPayload = this.encodePacket(
                           new autismclient.util.sync.LanSyncPacket.GoPacket(this.sessionId, executionId, method.ordinal(), targetTick)
                        );
                        this.setSyncState(LanSyncManager.SyncState.EXECUTING);

                        for (Entry<String, Socket> entryx : this.hostClientSockets.entrySet()) {
                           Socket clientSocket = entryx.getValue();
                           String clientName = entryx.getKey();

                           try {
                              if (!clientSocket.isClosed()) {
                                 synchronized (this.hostClientWriteLocks.computeIfAbsent(clientName, k -> new Object())) {
                                    OutputStream out = clientSocket.getOutputStream();
                                    out.write(goPayload);
                                    out.flush();
                                 }
                              }
                           } catch (IOException e) {
                              AutismClientAddon.LOG.warn("[Sync] Failed to send GO to {}", clientName);
                           }
                        }

                        if (method == LanSyncManager.ExecutionMethod.TICK) {
                           this.scheduleTickExecution(executionId, targetTick);
                        } else {
                           int hostOffset = this.getClientOffsetMs(this.localUsername);
                           if (hostOffset > 0) {
                              try {
                                 Thread.sleep(hostOffset);
                              } catch (InterruptedException var17) {
                              }
                           }

                           this.executePreparedAction();
                        }
                     } catch (Exception e) {
                        AutismClientAddon.LOG.error("[Sync] Error in GO-sync: ", e);
                        this.setSyncState(LanSyncManager.SyncState.IDLE);
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

   private void initiateQueueFlushGoSync() {
      if (this.canExecuteInWorld()) {
         long executionId = System.nanoTime();
         LanSyncManager.ExecutionMethod method = this.executionMethod;
         this.activeExecution = new LanSyncManager.ExecutionContext(executionId, false, null, "__QUEUE_FLUSH__", null, true, method, -1L);
         this.readyClients.clear();
         this.executionClientTimestampsMs.clear();
         this.executionHostReceiptNanos.clear();
         this.executionTickReports.clear();
         this.executedIds.clear();
         this.scheduledTickExecution = null;
         this.readyClients.add(this.localUsername);
         this.setSyncState(LanSyncManager.SyncState.PREPARING);
         AutismClientAddon.LOG.info("[Sync] Initiating queue flush GO-sync: execId={}", executionId);
         autismclient.util.sync.LanSyncPacket.PrepareExecutionPacket preparePacket = new autismclient.util.sync.LanSyncPacket.PrepareExecutionPacket(
            this.sessionId, executionId, false, "", "__QUEUE_FLUSH__", this.localUsername
         );
         preparePacket.executionMethod = method.ordinal();
         preparePacket.targetTick = -1L;
         this.sendPacket(preparePacket);
         this.broadcastSyncStateUpdate(LanSyncManager.SyncState.PREPARING, this.readyClients.size() + "/" + this.connectedClients.size() + " ready");
         (new Thread(() -> {
            Thread.currentThread().setPriority(10);

            try {
               long waitStart = System.currentTimeMillis();
               int expectedClients = this.connectedClients.size();

               while (this.readyClients.size() < expectedClients) {
                  if (System.currentTimeMillis() - waitStart > 3000L) {
                     AutismClientAddon.LOG.warn("[Sync] Timeout waiting for clients. Got {}/{}", this.readyClients.size(), expectedClients);
                     break;
                  }

                  LockSupport.parkNanos(500000L);
               }

               this.setSyncState(LanSyncManager.SyncState.ALL_READY);
               this.broadcastSyncStateUpdate(LanSyncManager.SyncState.ALL_READY, this.readyClients.size() + "/" + expectedClients + " ready");
               this.setSyncState(LanSyncManager.SyncState.DISPATCHING_GO);
               long targetTick = -1L;
               if (method == LanSyncManager.ExecutionMethod.TICK) {
                  long currentTick = this.resolveCurrentGameTick();
                  if (currentTick < 0L) {
                     AutismClientMessaging.sendPrefixed("§cCannot tick-sync without a loaded world.");
                     this.setSyncState(LanSyncManager.SyncState.IDLE);
                     return;
                  }

                  targetTick = currentTick + 10L;
                  LanSyncManager.ExecutionContext ctx = this.activeExecution;
                  if (ctx != null && ctx.executionId == executionId) {
                     ctx.targetTick = targetTick;
                  }
               }

               byte[] goPayload = this.encodePacket(new autismclient.util.sync.LanSyncPacket.GoPacket(this.sessionId, executionId, method.ordinal(), targetTick));
               this.setSyncState(LanSyncManager.SyncState.EXECUTING);

               for (Entry<String, Socket> entry : this.hostClientSockets.entrySet()) {
                  Socket clientSocket = entry.getValue();
                  String clientName = entry.getKey();

                  try {
                     if (!clientSocket.isClosed()) {
                        synchronized (this.hostClientWriteLocks.computeIfAbsent(clientName, k -> new Object())) {
                           OutputStream out = clientSocket.getOutputStream();
                           out.write(goPayload);
                           out.flush();
                        }
                     }
                  } catch (IOException e) {
                     AutismClientAddon.LOG.warn("[Sync] Failed to send GO to {}", clientName);
                  }
               }

               if (method == LanSyncManager.ExecutionMethod.TICK) {
                  this.scheduleTickExecution(executionId, targetTick);
               } else {
                  int hostOffset = this.getClientOffsetMs(this.localUsername);
                  if (hostOffset > 0) {
                     try {
                        Thread.sleep(hostOffset);
                     } catch (InterruptedException var17) {
                     }
                  }

                  this.executePreparedAction();
               }
            } catch (Exception e) {
               AutismClientAddon.LOG.error("[Sync] Error in queue GO-sync: ", e);
               this.setSyncState(LanSyncManager.SyncState.IDLE);
            }
         }, "Sync-GO-QueueFlush") {
            {
               this.setDaemon(true);
            }
         }).start();
      }
   }

   private void handlePrepareExecution(
      long executionId, boolean isMacro, String macroName, String command, String senderUsername, String macroAssignments, int methodOrdinal, long targetTick
   ) {
      if (this.canExecuteInWorld() && (senderUsername == null || !senderUsername.equals(this.localUsername))) {
         AutismClientAddon.LOG.info("[Sync] Received PREPARE from {}", senderUsername);
         this.peerMacroStepProgress.clear();
         this.executionTickReports.clear();
         this.executedIds.clear();
         this.scheduledTickExecution = null;
         String myMacroName = macroName;
         if (macroAssignments != null && !macroAssignments.isEmpty()) {
            String[] parts = macroAssignments.split("\t");

            for (int pi = 0; pi + 1 < parts.length; pi += 2) {
               if (parts[pi].equals(this.localUsername)) {
                  myMacroName = parts[pi + 1];
                  break;
               }
            }
         }

         AutismMacro macro = null;
         if (isMacro && myMacroName != null && !myMacroName.isEmpty()) {
            macro = MacroLibraryManager.getInstance().findByName(myMacroName);
            if (macro != null) {
               macro.regeneratePackets();
            }
         } else if ("__QUEUE_FLUSH__".equals(command)) {
            AutismSessionState.getInstance().regenerateQueuedPackets();
         }

         LanSyncManager.ExecutionMethod method = this.executionMethodFromOrdinal(methodOrdinal);
         this.executionMethod = method;
         this.activeExecution = new LanSyncManager.ExecutionContext(executionId, isMacro, myMacroName, command, macro, false, method, targetTick);
         this.setSyncState(LanSyncManager.SyncState.PREPARING);
         this.sendPacket(new autismclient.util.sync.LanSyncPacket.ClientReadyPacket(this.sessionId, executionId, this.localUsername));
         AutismClientAddon.LOG.info("[Sync] Sent READY signal");
      }
   }

   private void handleClientReady(long executionId, String username) {
      LanSyncManager.ExecutionContext ctx = this.activeExecution;
      if (ctx != null && executionId == ctx.executionId) {
         this.readyClients.add(username);
         this.broadcastSyncStateUpdate(LanSyncManager.SyncState.PREPARING, this.readyClients.size() + "/" + this.connectedClients.size() + " ready");
         AutismClientAddon.LOG.info("[Sync] Client ready: {} (total: {})", username, this.readyClients.size());
      }
   }

   private void handleGoSignal(long executionId, int methodOrdinal, long targetTick) {
      if (this.canExecuteInWorld()) {
         LanSyncManager.ExecutionContext ctx = this.activeExecution;
         if (ctx == null || executionId != ctx.executionId) {
            AutismClientAddon.LOG.warn("[Sync] GO for wrong execId, ignoring");
         } else if (!ctx.isInitiator) {
            LanSyncManager.ExecutionMethod method = this.executionMethodFromOrdinal(methodOrdinal);
            ctx.targetTick = targetTick;
            this.executionMethod = method;
            if (method == LanSyncManager.ExecutionMethod.TICK && targetTick >= 0L) {
               AutismClientAddon.LOG.info("[Sync] Received GO! Scheduling tick execution for {}", targetTick);
               this.setSyncState(LanSyncManager.SyncState.EXECUTING);
               this.scheduleTickExecution(executionId, targetTick);
            } else {
               int offset = this.getClientOffsetMs(this.localUsername);
               if (offset > 0) {
                  AutismClientAddon.LOG.info("[Sync] Received GO! Applying {}ms offset before executing.", offset);
                  this.setSyncState(LanSyncManager.SyncState.EXECUTING);

                  try {
                     Thread.sleep(offset);
                  } catch (InterruptedException var10) {
                  }

                  this.executePreparedAction();
               } else {
                  AutismClientAddon.LOG.info("[Sync] Received GO! Executing immediately.");
                  this.setSyncState(LanSyncManager.SyncState.EXECUTING);
                  this.executePreparedAction();
               }
            }
         }
      }
   }

   private void executePreparedAction() {
      if ((AutismLiteVariant.enabled() || autismclient.e.BitSetUtil.init()) && this.canExecuteInWorld() && !autismclient.modules.aD.read_Z()) {
         LanSyncManager.ExecutionContext ctx = this.activeExecution;
         if (ctx != null) {
            Minecraft mc = Minecraft.getInstance();
            if (!mc.isSameThread()) {
               mc.execute(this::executePreparedAction);
            } else if (this.executedIds.add(ctx.executionId)) {
               long executedAtMs = System.currentTimeMillis();
               long actualTick = this.resolveCurrentGameTick();
               boolean tickMode = ctx.executionMethod == LanSyncManager.ExecutionMethod.TICK;
               boolean late = tickMode && (actualTick < 0L || ctx.targetTick >= 0L && actualTick > ctx.targetTick);
               if ("__QUEUE_FLUSH__".equals(ctx.command)) {
                  ClientPacketListener nh = mc.getConnection();
                  if (nh != null) {
                     int count = AutismSessionState.getInstance().flushDelayedPackets(nh);
                     AutismClientAddon.LOG.info("[Sync] Queue flushed! {} packets sent", count);
                  }
               } else if (ctx.command != null && ctx.command.startsWith("__DELAY_PACKETS__\t")) {
                  boolean enabled = "1".equals(ctx.command.substring("__DELAY_PACKETS__\t".length()));
                  autismclient.modules.ModuleRuntimeManager module = autismclient.modules.ModuleRuntimeManager.getInstance();
                  int flushed = module.toggleDelayPackets(enabled);
                  module.notifyDelayToggle(enabled, flushed);
                  AutismClientAddon.LOG.info("[Sync] Delay packets set to {} (flushed={})", enabled, flushed);
               } else if (ctx.command != null && ctx.command.startsWith("__CHAT__\t")) {
                  String chatMsg = ctx.command.substring("__CHAT__\t".length());
                  ClientPacketListener nh = mc.getConnection();
                  if (nh != null) {
                     if (chatMsg.startsWith("/")) {
                        nh.sendCommand(chatMsg.substring(1));
                     } else {
                        nh.sendChat(chatMsg);
                     }

                     AutismClientAddon.LOG.info("[Sync] Chat sent: {}", chatMsg);
                  }
               } else if (ctx.isMacro && ctx.macro != null) {
                  ctx.macro.execute(false);
               }

               AutismClientAddon.LOG.info("[Sync] EXECUTED at {} tick {}", executedAtMs, actualTick);
               this.executionClientTimestampsMs.put(this.localUsername, executedAtMs);
               this.executionHostReceiptNanos.put(this.localUsername, System.nanoTime());
               if (tickMode) {
                  this.executionTickReports.put(this.localUsername, new LanSyncManager.TickReport(actualTick, late));
               }

               this.sendPacket(
                  new autismclient.util.sync.LanSyncPacket.CommandAckPacket(
                     this.sessionId, this.localUsername, executedAtMs, ctx.executionMethod.ordinal(), ctx.targetTick, actualTick, late
                  )
               );
               this.setSyncState(LanSyncManager.SyncState.REPORTING);
               if (ctx.isInitiator) {
                  (new Thread(() -> {
                     try {
                        int expected = this.connectedClients.size();
                        long start = System.currentTimeMillis();

                        while (this.getReceivedReportCount(ctx.executionMethod) < expected && System.currentTimeMillis() - start < 1500L) {
                           LockSupport.parkNanos(1000000L);
                        }

                        if (ctx.executionMethod == LanSyncManager.ExecutionMethod.TICK) {
                           this.computeAndBroadcastTickSpread(ctx.targetTick);
                        } else {
                           this.computeAndBroadcastInstantSpread();
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

   private void handleExecutionAck(String username, long timestampMs, int methodOrdinal, long targetTick, long actualTick, boolean late) {
      this.executionClientTimestampsMs.put(username, timestampMs);
      this.executionHostReceiptNanos.put(username, System.nanoTime());
      LanSyncManager.ExecutionMethod method = this.executionMethodFromOrdinal(methodOrdinal);
      if (method == LanSyncManager.ExecutionMethod.TICK) {
         this.executionTickReports.put(username, new LanSyncManager.TickReport(actualTick, late || targetTick >= 0L && actualTick > targetTick));
      }

      AutismClientAddon.LOG
         .info("[Sync] ACK from {}: remoteMs={}, hostReceiptNano={}, tick={}", username, timestampMs, this.executionHostReceiptNanos.get(username), actualTick);
   }

   private long resolveCurrentGameTick() {
      Minecraft mc = Minecraft.getInstance();
      if (mc.level == null) {
         return -1L;
      }

      if (mc.isSameThread()) {
         this.lastKnownGameTick = mc.level.getGameTime();
      }

      return this.lastKnownGameTick >= 0L ? this.lastKnownGameTick : mc.level.getGameTime();
   }

   private void scheduleTickExecution(long executionId, long targetTick) {
      LanSyncManager.ExecutionContext ctx = this.activeExecution;
      if (ctx != null && ctx.executionId == executionId) {
         ctx.targetTick = targetTick;
         long currentTick = this.resolveCurrentGameTick();
         if (currentTick >= 0L && currentTick < targetTick) {
            LanSyncManager.ScheduledTickExecution sched = new LanSyncManager.ScheduledTickExecution(executionId, targetTick);
            if (autismclient.util.macro.ServerTickPhaseTracker.isWarmedUp()) {
               long ticksOut = targetTick - currentTick;
               long now = System.nanoTime();
               long nextTickNano = autismclient.util.macro.ServerTickPhaseTracker.nextTickNanos();
               long targetBoundaryNano = nextTickNano + Math.max(0L, ticksOut - 1L) * 50000000L;
               int pingMs = autismclient.util.macro.ServerTickPhaseTracker.getLatencyMs();
               long halfPingNano = pingMs * 1000000L / 2L;
               long fireAt = targetBoundaryNano - halfPingNano;
               if (fireAt <= now) {
                  fireAt = now;
               }

               sched.fireAtNano = fireAt;
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

                  LanSyncManager.ExecutionContext sc = this.activeExecution;
                  if (sc != null && sc.executionId == execId && sc.executionMethod == LanSyncManager.ExecutionMethod.TICK) {
                     LanSyncManager.ScheduledTickExecution s = this.scheduledTickExecution;
                     if (s != null && s.executionId == execId) {
                        this.scheduledTickExecution = null;
                        this.setSyncState(LanSyncManager.SyncState.EXECUTING);
                        this.executePreparedAction();
                     }
                  }
               }, "Sync-TickPrecise-" + executionId);
               sched.timerThread = t;
               t.setDaemon(true);
               t.start();
            }

            this.scheduledTickExecution = sched;
         } else {
            this.scheduledTickExecution = null;
            this.executePreparedAction();
         }
      }
   }

   public void onWorldTick(long currentTick) {
      this.lastKnownGameTick = currentTick;
      LanSyncManager.ScheduledTickExecution scheduled = this.scheduledTickExecution;
      if (scheduled != null && currentTick >= scheduled.targetTick) {
         LanSyncManager.ExecutionContext ctx = this.activeExecution;
         if (ctx != null && ctx.executionId == scheduled.executionId && ctx.executionMethod == LanSyncManager.ExecutionMethod.TICK) {
            ctx.targetTick = scheduled.targetTick;
            this.scheduledTickExecution = null;
            this.setSyncState(LanSyncManager.SyncState.EXECUTING);
            this.executePreparedAction();
         } else {
            this.scheduledTickExecution = null;
         }
      }
   }

   private int getReceivedReportCount(LanSyncManager.ExecutionMethod method) {
      return method == LanSyncManager.ExecutionMethod.TICK ? this.executionTickReports.size() : this.executionHostReceiptNanos.size();
   }

   private LanSyncManager.ExecutionMethod executionMethodFromOrdinal(int ordinal) {
      LanSyncManager.ExecutionMethod[] values = LanSyncManager.ExecutionMethod.values();
      return ordinal >= 0 && ordinal < values.length ? values[ordinal] : LanSyncManager.ExecutionMethod.INSTANT;
   }

   private LanSyncManager.ExecutionMethod parseExecutionMethod(String name) {
      if (name == null) {
         return LanSyncManager.ExecutionMethod.INSTANT;
      }

      try {
         return LanSyncManager.ExecutionMethod.valueOf(name.trim().toUpperCase(Locale.ROOT));
      } catch (IllegalArgumentException e) {
         return LanSyncManager.ExecutionMethod.INSTANT;
      }
   }

   private void computeAndBroadcastInstantSpread() {
      Map<String, Long> timings = this.executionHostReceiptNanos.isEmpty() ? this.executionClientTimestampsMs : this.executionHostReceiptNanos;
      if (timings.size() < 2) {
         this.setSyncState(LanSyncManager.SyncState.DONE);
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

         boolean isNanos = !this.executionHostReceiptNanos.isEmpty();
         long spreadNanos = latest - earliest;
         long totalSpreadMs = isNanos ? spreadNanos / 1000000L : latest - earliest;
         this.lastSpreadResult = new LanSyncManager.SpreadResult(isNanos ? spreadNanos : totalSpreadMs * 1000000L, latestClient, timings.size());
         this.spreadHistory.add(this.lastSpreadResult);

         while (this.spreadHistory.size() > 10) {
            this.spreadHistory.remove(0);
         }

         String report;
         if (totalSpreadMs == 0L) {
            report = "§aPerfect sync!";
         } else {
            report = "§e" + latestClient + " §7was §c+" + totalSpreadMs + "ms §7late";
         }

         AutismClientMessaging.sendPrefixed(report);
         this.sendPacket(new autismclient.util.sync.LanSyncPacket.ChatMessagePacket(this.sessionId, "__SYNC__", report));
         this.sendPacket(new autismclient.util.sync.LanSyncPacket.ChatMessagePacket(this.sessionId, "__SPREAD_DATA__", spreadNanos + "\t" + latestClient + "\t" + timings.size()));
         this.broadcastSyncStateUpdate(LanSyncManager.SyncState.DONE, totalSpreadMs + "ms spread");
         this.setSyncState(LanSyncManager.SyncState.DONE);
         this.dispatchCallbackOnClientThread(this.onSpreadUpdatedCallback);
         (new Thread(() -> {
            try {
               Thread.sleep(3000L);
            } catch (InterruptedException var2x) {
            }

            if (this.syncState == LanSyncManager.SyncState.DONE) {
               this.setSyncState(LanSyncManager.SyncState.IDLE);
            }
         }, "Sync-ResetIdle") {
            {
               this.setDaemon(true);
            }
         }).start();
      }
   }

   private void computeAndBroadcastTickSpread(long targetTick) {
      int expected = this.connectedClients.size();
      TreeSet<String> peers = new TreeSet<>(this.connectedClients.keySet());
      boolean perfect = targetTick >= 0L && this.executionTickReports.size() >= expected;
      int maxOffset = 0;
      List<String> offPeers = new ArrayList<>();

      for (String peer : peers) {
         LanSyncManager.TickReport report = this.executionTickReports.get(peer);
         if (report == null) {
            perfect = false;
            offPeers.add(peer + " no report");
         } else {
            long deltaLong = targetTick >= 0L ? report.actualTick - targetTick : 0L;
            int delta = (int)Math.max(-2147483648L, Math.min(2147483647L, deltaLong));
            maxOffset = Math.max(maxOffset, Math.abs(delta));
            if (delta != 0 || report.late) {
               perfect = false;
               String direction = delta >= 0 ? "+" : "";
               String timing = delta >= 0 ? "late" : "early";
               offPeers.add(peer + " was " + direction + delta + " tick(s) " + timing + " on tick " + report.actualTick + ", target " + targetTick);
            }
         }
      }

      String report;
      if (perfect) {
         report = "§aPerfect sync on server tick " + targetTick;
      } else {
         report = "§eTick sync target " + targetTick + ": §c" + String.join("; ", offPeers);
      }

      this.lastSpreadResult = new LanSyncManager.SpreadResult(
         maxOffset * 50000000L,
         perfect ? "" : "tick",
         Math.max(this.executionTickReports.size(), expected),
         LanSyncManager.ExecutionMethod.TICK,
         targetTick,
         maxOffset,
         perfect,
         report
      );
      this.spreadHistory.add(this.lastSpreadResult);

      while (this.spreadHistory.size() > 10) {
         this.spreadHistory.remove(0);
      }

      AutismClientMessaging.sendPrefixed(report);
      this.sendPacket(new autismclient.util.sync.LanSyncPacket.ChatMessagePacket(this.sessionId, "__SYNC__", report));
      this.sendPacket(
         new autismclient.util.sync.LanSyncPacket.ChatMessagePacket(
            this.sessionId,
            "__TICK_SPREAD_DATA__",
            targetTick + "\t" + maxOffset + "\t" + Math.max(this.executionTickReports.size(), expected) + "\t" + perfect + "\t" + report
         )
      );
      this.broadcastSyncStateUpdate(LanSyncManager.SyncState.DONE, perfect ? "tick perfect" : maxOffset + " tick offset");
      this.setSyncState(LanSyncManager.SyncState.DONE);
      this.dispatchCallbackOnClientThread(this.onSpreadUpdatedCallback);
      (new Thread(() -> {
         try {
            Thread.sleep(3000L);
         } catch (InterruptedException var2) {
         }

         if (this.syncState == LanSyncManager.SyncState.DONE) {
            this.setSyncState(LanSyncManager.SyncState.IDLE);
         }
      }, "Sync-ResetIdle") {
         {
            this.setDaemon(true);
         }
      }).start();
   }

   public void triggerQueueFlushSync() {
      if (this.canExecuteInWorld()) {
         if (this.sessionId == null) {
            AutismClientMessaging.sendPrefixed("§cJoin a session first.");
         } else {
            AutismSessionState.getInstance().regenerateQueuedPackets();
            if (this.isHost) {
               this.initiateQueueFlushGoSync();
            } else {
               autismclient.util.sync.LanSyncPacket.RequestSyncPacket request = new autismclient.util.sync.LanSyncPacket.RequestSyncPacket(
                  this.sessionId, this.localUsername, false, "", "__QUEUE_FLUSH__"
               );
               request.executionMethod = this.executionMethod.ordinal();
               this.sendPacket(request);
               AutismClientMessaging.sendPrefixed("§eRequested sync execution from host...");
            }
         }
      }
   }

   public void triggerDelayPacketsSync(boolean enabled) {
      if (this.canExecuteInWorld()) {
         if (this.sessionId == null) {
            AutismClientMessaging.sendPrefixed("§cJoin a session first.");
         } else {
            String command = "__DELAY_PACKETS__\t" + (enabled ? "1" : "0");
            if (this.isHost) {
               this.initiateGoSync(false, "", command);
            } else {
               autismclient.util.sync.LanSyncPacket.RequestSyncPacket request = new autismclient.util.sync.LanSyncPacket.RequestSyncPacket(this.sessionId, this.localUsername, false, "", command);
               request.executionMethod = this.executionMethod.ordinal();
               this.sendPacket(request);
               AutismClientMessaging.sendPrefixed("§eRequested sync delay toggle from host...");
            }
         }
      }
   }

   public void triggerMacroSync(String macroName) {
      if (!this.canExecuteInWorld()) {
         AutismClientMessaging.sendPrefixed("§cLAN Sync execution requires a connected world.");
      } else if (this.sessionId == null) {
         AutismClientMessaging.sendPrefixed("§cJoin a session first.");
      } else if (!this.isSyncIdle()) {
         AutismClientMessaging.sendPrefixed("§cLAN Sync is busy with another synchronized action.");
      } else if (this.isHost) {
         this.initiateGoSync(true, macroName, "");
      } else {
         autismclient.util.sync.LanSyncPacket.RequestSyncPacket request = new autismclient.util.sync.LanSyncPacket.RequestSyncPacket(this.sessionId, this.localUsername, true, macroName, "");
         request.executionMethod = this.executionMethod.ordinal();
         this.sendPacket(request);
         AutismClientMessaging.sendPrefixed("§eRequested sync execution from host...");
      }
   }

   public void triggerPerUserMacroSync(Map<String, String> assignments) {
      if (this.canExecuteInWorld()) {
         if (this.sessionId == null) {
            AutismClientMessaging.sendPrefixed("§cJoin a session first.");
         } else if (assignments != null && !assignments.isEmpty()) {
            String defaultMacro = assignments.values().iterator().next();
            if (this.isHost) {
               this.initiateGoSync(true, defaultMacro, "", assignments);
            } else {
               StringBuilder sb = new StringBuilder();

               for (Entry<String, String> entry : assignments.entrySet()) {
                  if (sb.length() > 0) {
                     sb.append('\t');
                  }

                  sb.append(entry.getKey()).append('\t').append(entry.getValue());
               }

               autismclient.util.sync.LanSyncPacket.RequestSyncPacket rsp = new autismclient.util.sync.LanSyncPacket.RequestSyncPacket(
                  this.sessionId, this.localUsername, true, defaultMacro, "__PER_USER__\t" + sb
               );
               rsp.executionMethod = this.executionMethod.ordinal();
               this.sendPacket(rsp);
               AutismClientMessaging.sendPrefixed("§eRequested per-user sync from host...");
            }
         }
      }
   }

   public void startSessionSearch() {
      if (!this.running.get()) {
         AutismClientMessaging.sendPrefixed("§cStart LAN Sync first.");
      } else if (this.isInSession()) {
         AutismClientMessaging.sendPrefixed("§cAlready in session.");
      } else {
         if (this.localUsername.isEmpty()) {
            this.localUsername = this.resolvePlayerUsername();
         }

         this.discoveredSessions.clear();
         this.searching = true;
         this.searchTicksRemaining = 400;
         (new Thread(() -> {
            while (this.searching && this.searchTicksRemaining > 0) {
               try {
                  Socket searchSocket = new Socket();
                  searchSocket.connect(new InetSocketAddress(InetAddress.getLoopbackAddress(), 25568), 500);
                  searchSocket.setTcpNoDelay(true);
                  DataOutputStream out = new DataOutputStream(searchSocket.getOutputStream());
                  DataInputStream in = new DataInputStream(searchSocket.getInputStream());
                  out.writeInt(autismclient.util.sync.LanSyncPacketType.SEARCH_REQUEST.id());
                  out.writeUTF("");
                  out.flush();
                  int typeId = in.readInt();
                  String foundSessionId = in.readUTF();
                  if (typeId == autismclient.util.sync.LanSyncPacketType.SESSION.id()) {
                     autismclient.util.sync.LanSyncPacket.SessionPacket session = new autismclient.util.sync.LanSyncPacket.SessionPacket();
                     session.setSessionId(foundSessionId);
                     session.read(in);
                     this.discoveredSessions.put(foundSessionId, session.hostName);
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

   public void cancelSessionSearch() {
      this.searching = false;
      this.searchTicksRemaining = 0;
   }

   public void tick() {
      if (this.running.get() || this.sessionId != null) {
         if (this.sessionId != null) {
            long nowMs = System.currentTimeMillis();
            if (nowMs - this.lastHeartbeatSentMs >= 9000L) {
               this.lastHeartbeatSentMs = nowMs;
               this.sendPacket(new autismclient.util.sync.LanSyncPacket.HeartbeatPacket(this.sessionId, this.localUsername, nowMs));
               this.pruneTimedOutPeers();
            }
         }

         Minecraft mc = Minecraft.getInstance();
         if (mc.level != null) {
            long currentTick = mc.level.getGameTime();
            if (this.sessionId != null && !this.connectedClients.isEmpty() && currentTick % 600L == 37L) {
               this.broadcastLocalMacroRoster();
            }

            if (this.searching && this.searchTicksRemaining > 0) {
               this.searchTicksRemaining--;
               if (!this.discoveredSessions.isEmpty()) {
                  String firstSessionId = this.discoveredSessions.keySet().iterator().next();
                  String hostName = this.discoveredSessions.get(firstSessionId);
                  this.searching = false;
                  this.searchTicksRemaining = 0;
                  this.joinSession(firstSessionId);
                  AutismClientMessaging.sendPrefixed("§aJoined session " + firstSessionId + " (Host: " + hostName + ").");
                  return;
               }

               if (this.searchTicksRemaining == 0) {
                  this.searching = false;
                  AutismClientMessaging.sendPrefixed("§cNo sessions found.");
               }
            }
         }
      }
   }

   private void pruneTimedOutPeers() {
      long now = System.currentTimeMillis();
      List<String> deadPeers = new ArrayList<>();

      for (Entry<String, Long> entry : this.peerLastHeartbeatMs.entrySet()) {
         String peer = entry.getKey();
         if (!peer.equals(this.localUsername)) {
            long elapsed = now - entry.getValue();
            if (elapsed > 30000L) {
               deadPeers.add(peer);
            }
         }
      }

      for (String dead : deadPeers) {
         this.connectedClients.remove(dead);
         this.peerLastHeartbeatMs.remove(dead);
         this.peerMacros.remove(dead);
         this.peerMacrosRevision.incrementAndGet();
         this.peerMacroStepProgress.remove(dead);
         if (this.isHost) {
            Socket s = this.hostClientSockets.remove(dead);
            if (s != null) {
               try {
                  s.close();
               } catch (IOException var9) {
               }
            }

            this.hostClientWriteLocks.remove(dead);
         }

         AutismClientMessaging.sendPrefixed("§e" + dead + " timed out.");
         AutismClientAddon.LOG.info("[Heartbeat] Peer timed out: {}", dead);
         this.dispatchCallbackOnClientThread(this.onClientLeftCallback);
         this.dispatchCallbackOnClientThread(this.onPeerStateChangedCallback);
         MacroWaitRegistry.pollWaits();
      }

      if (this.isHost && !deadPeers.isEmpty()) {
         this.broadcastClientList();
      }
   }

   public LanSyncManager.PeerHealth getPeerHealth(String username) {
      if (username.equals(this.localUsername)) {
         return LanSyncManager.PeerHealth.HEALTHY;
      }

      Long lastBeat = this.peerLastHeartbeatMs.get(username);
      if (lastBeat == null) {
         return LanSyncManager.PeerHealth.UNKNOWN;
      }

      long elapsed = System.currentTimeMillis() - lastBeat;
      return elapsed > 15000L ? LanSyncManager.PeerHealth.STALE : LanSyncManager.PeerHealth.HEALTHY;
   }

   private void handlePacket(autismclient.util.sync.LanSyncPacket packet) {
      try {
         switch (packet.getType()) {
            case SEARCH_REQUEST:
            case PLAYER_OFFSETS:
            case CLIENT_OFFSET_UPDATE:
            case EXECUTE_NOW:
            default:
               break;
            case SESSION:
               if (packet instanceof autismclient.util.sync.LanSyncPacket.SessionPacket p) {
                  String sid = p.getSessionId();
                  String host = p.hostName;
                  if (!host.equals(this.localUsername) && !this.isInSession()) {
                     boolean isNew = !this.discoveredSessions.containsKey(sid);
                     this.discoveredSessions.put(sid, host);
                     if (isNew) {
                        AutismClientMessaging.sendPrefixed("§eFound session " + sid + " (Host: " + host + ").");
                     }
                  }
               }
               break;
            case JOIN:
               if (packet instanceof autismclient.util.sync.LanSyncPacket.JoinPacket && packet.getSessionId().equals(this.sessionId)) {
                  autismclient.util.sync.LanSyncPacket.JoinPacket p = (autismclient.util.sync.LanSyncPacket.JoinPacket)packet;
                  String username = p.username;
                  if (username != null && !username.isEmpty() && !username.equals(this.localUsername) && !this.connectedClients.containsKey(username)) {
                     this.connectedClients.put(username, new LanSyncManager.ClientInfo(username, false));
                     this.peerLastHeartbeatMs.put(username, System.currentTimeMillis());
                     AutismClientMessaging.sendPrefixed("§e" + username + " joined.");
                     this.executionClientTimestampsMs.clear();
                     if (this.isHost) {
                        this.broadcastClientList();
                        this.broadcastLocalMacroRoster();
                     }

                     this.dispatchCallbackOnClientThread(this.onClientJoinedCallback);
                     this.dispatchCallbackOnClientThread(this.onPeerStateChangedCallback);
                  }
               }
               break;
            case CLIENT_LIST:
               if (packet instanceof autismclient.util.sync.LanSyncPacket.ClientListPacket && packet.getSessionId().equals(this.sessionId)) {
                  autismclient.util.sync.LanSyncPacket.ClientListPacket p = (autismclient.util.sync.LanSyncPacket.ClientListPacket)packet;
                  if (!this.isHost) {
                     LanSyncManager.ExecutionMethod hostMethod = this.executionMethodFromOrdinal(p.executionMethod);
                     if (this.executionMethod != hostMethod) {
                        this.executionMethod = hostMethod;
                        this.dispatchCallbackOnClientThread(this.onPeerStateChangedCallback);
                     }
                  }

                  if (!p.clients.isEmpty()) {
                     LanSyncManager.ClientInfo ourInfo = this.connectedClients.get(this.localUsername);
                     long now = System.currentTimeMillis();
                     Set<String> listedPeers = new HashSet<>();
                     Set<String> listed = new HashSet<>();

                     for (autismclient.util.sync.LanSyncPacket.ClientListPacket.ClientEntry entry : p.clients) {
                        this.connectedClients.put(entry.name, new LanSyncManager.ClientInfo(entry.name, entry.isHost));
                        listed.add(entry.name);
                        if (!entry.name.equals(this.localUsername)) {
                           this.peerLastHeartbeatMs.put(entry.name, now);
                           listedPeers.add(entry.name);
                        }
                     }

                     if (!listed.contains(this.localUsername) && ourInfo != null) {
                        this.connectedClients.put(this.localUsername, ourInfo);
                        listed.add(this.localUsername);
                     }

                     this.connectedClients.keySet().retainAll(listed);
                     this.peerLastHeartbeatMs.keySet().retainAll(listedPeers);
                  }
               }
               break;
            case LEAVE:
               if (packet instanceof autismclient.util.sync.LanSyncPacket.LeavePacket && packet.getSessionId().equals(this.sessionId)) {
                  autismclient.util.sync.LanSyncPacket.LeavePacket p = (autismclient.util.sync.LanSyncPacket.LeavePacket)packet;
                  String username = p.username;
                  if (username != null && !username.isEmpty() && !username.equals(this.localUsername)) {
                     LanSyncManager.ClientInfo leavingClient = this.connectedClients.get(username);
                     boolean leavingWasHost = leavingClient != null && leavingClient.isHost;
                     this.connectedClients.remove(username);
                     this.peerLastHeartbeatMs.remove(username);
                     this.peerMacros.remove(username);
                     this.peerMacrosRevision.incrementAndGet();
                     this.peerMacroStepProgress.remove(username);
                     AutismClientMessaging.sendPrefixed("§e" + username + " left.");
                     if (this.isHost) {
                        Socket s = this.hostClientSockets.remove(username);
                        if (s != null) {
                           try {
                              s.close();
                           } catch (IOException var10) {
                           }
                        }

                        this.hostClientWriteLocks.remove(username);
                        this.relayPacketToOthers(packet, username);
                        this.broadcastClientList();
                     }

                     if (leavingWasHost && !this.connectedClients.isEmpty()) {
                        String newHostUsername = this.connectedClients.keySet().stream().sorted().findFirst().orElse(null);
                        if (newHostUsername != null && newHostUsername.equals(this.localUsername)) {
                           this.isHost = true;
                           this.connectedClients.put(this.localUsername, new LanSyncManager.ClientInfo(this.localUsername, true));
                           AutismClientMessaging.sendPrefixed("§aYou are now the host!");
                           this.executionClientTimestampsMs.clear();
                           this.broadcastClientList();
                        }
                     }

                     this.dispatchCallbackOnClientThread(this.onClientLeftCallback);
                     this.dispatchCallbackOnClientThread(this.onPeerStateChangedCallback);
                     MacroWaitRegistry.pollWaits();
                  }
               }
               break;
            case REQUEST_CLIENT_LIST:
               if (this.isHost) {
                  this.broadcastClientList();
               }
               break;
            case REQUEST_MACRO:
               this.handleRequestMacroPacket(packet);
               break;
            case MACRO_DATA:
               this.handleMacroDataPacket(packet);
               break;
            case MACRO_DATA_CHUNK:
               this.handleMacroDataChunkPacket(packet);
               break;
            case MACRO_DELETE:
               this.handleMacroDeletePacket(packet);
               break;
            case MACRO_LIST:
               this.handleMacroListPacket(packet);
               break;
            case PRESET_DATA:
               this.handlePresetDataPacket(packet);
               break;
            case QUEUE_SYNC:
               if (packet instanceof autismclient.util.sync.LanSyncPacket.QueueSyncPacket && packet.getSessionId().equals(this.sessionId)) {
                  autismclient.util.sync.LanSyncPacket.QueueSyncPacket qp = (autismclient.util.sync.LanSyncPacket.QueueSyncPacket)packet;
                  this.handleQueueSync(qp.queueData, qp.senderUsername);
               }
               break;
            case CHAT_MESSAGE:
               if (packet instanceof autismclient.util.sync.LanSyncPacket.ChatMessagePacket && packet.getSessionId().equals(this.sessionId)) {
                  autismclient.util.sync.LanSyncPacket.ChatMessagePacket cp = (autismclient.util.sync.LanSyncPacket.ChatMessagePacket)packet;
                  this.handleChatOrControlMessage(cp.senderUsername, cp.message);
               }
               break;
            case PREPARE_EXECUTION:
               if (packet instanceof autismclient.util.sync.LanSyncPacket.PrepareExecutionPacket && packet.getSessionId().equals(this.sessionId)) {
                  autismclient.util.sync.LanSyncPacket.PrepareExecutionPacket p = (autismclient.util.sync.LanSyncPacket.PrepareExecutionPacket)packet;
                  this.handlePrepareExecution(p.executionId, p.isMacro, p.targetName, p.queueData, p.senderUsername, p.macroAssignments, p.executionMethod, p.targetTick);
               }
               break;
            case CLIENT_READY:
               if (packet instanceof autismclient.util.sync.LanSyncPacket.ClientReadyPacket && packet.getSessionId().equals(this.sessionId)) {
                  autismclient.util.sync.LanSyncPacket.ClientReadyPacket p = (autismclient.util.sync.LanSyncPacket.ClientReadyPacket)packet;
                  this.handleClientReady(p.executionId, p.senderUsername);
               }
               break;
            case GO:
               if (packet instanceof autismclient.util.sync.LanSyncPacket.GoPacket && packet.getSessionId().equals(this.sessionId)) {
                  autismclient.util.sync.LanSyncPacket.GoPacket p = (autismclient.util.sync.LanSyncPacket.GoPacket)packet;
                  this.handleGoSignal(p.executionId, p.executionMethod, p.targetTick);
               }
               break;
            case TCP_COMMAND_ACK:
               if (packet instanceof autismclient.util.sync.LanSyncPacket.CommandAckPacket && packet.getSessionId().equals(this.sessionId)) {
                  autismclient.util.sync.LanSyncPacket.CommandAckPacket p = (autismclient.util.sync.LanSyncPacket.CommandAckPacket)packet;
                  this.handleExecutionAck(p.senderUsername, p.executeTimestamp, p.executionMethod, p.targetTick, p.actualTick, p.late);
               }
               break;
            case HEARTBEAT:
               if (packet instanceof autismclient.util.sync.LanSyncPacket.HeartbeatPacket && packet.getSessionId().equals(this.sessionId)) {
                  autismclient.util.sync.LanSyncPacket.HeartbeatPacket p = (autismclient.util.sync.LanSyncPacket.HeartbeatPacket)packet;
                  if (!p.senderUsername.equals(this.localUsername)) {
                     this.peerLastHeartbeatMs.put(p.senderUsername, System.currentTimeMillis());
                     if (this.isHost) {
                        this.relayPacketToOthers(packet, p.senderUsername);
                     }
                  }
               }
               break;
            case REQUEST_SYNC:
               if (packet instanceof autismclient.util.sync.LanSyncPacket.RequestSyncPacket && packet.getSessionId().equals(this.sessionId)) {
                  autismclient.util.sync.LanSyncPacket.RequestSyncPacket p = (autismclient.util.sync.LanSyncPacket.RequestSyncPacket)packet;
                  this.handleSyncRequest(p.senderUsername, p.isMacro, p.macroName, p.command);
               }
               break;
            case OFFSET_SYNC:
               if (packet instanceof autismclient.util.sync.LanSyncPacket.OffsetSyncPacket && packet.getSessionId().equals(this.sessionId)) {
                  autismclient.util.sync.LanSyncPacket.OffsetSyncPacket p = (autismclient.util.sync.LanSyncPacket.OffsetSyncPacket)packet;
                  this.handleOffsetSync(p.senderUsername, p.targetUsername, p.offsetMs);
               }
               break;
            case MACRO_STEP_PROGRESS:
               if (packet instanceof autismclient.util.sync.LanSyncPacket.MacroStepProgressPacket && packet.getSessionId().equals(this.sessionId)) {
                  autismclient.util.sync.LanSyncPacket.MacroStepProgressPacket p = (autismclient.util.sync.LanSyncPacket.MacroStepProgressPacket)packet;
                  this.handleMacroStepProgress(p.senderUsername, p.completedStep, p.totalSteps, p.macroName);
               }
               break;
            case SYNC_STATE_UPDATE:
               if (packet instanceof autismclient.util.sync.LanSyncPacket.SyncStateUpdatePacket && packet.getSessionId().equals(this.sessionId)) {
                  autismclient.util.sync.LanSyncPacket.SyncStateUpdatePacket p = (autismclient.util.sync.LanSyncPacket.SyncStateUpdatePacket)packet;
                  LanSyncManager.SyncState[] states = LanSyncManager.SyncState.values();
                  if (p.stateOrdinal >= 0 && p.stateOrdinal < states.length) {
                     LanSyncManager.ExecutionContext ctx = this.activeExecution;
                     if (ctx == null || !ctx.isInitiator) {
                        this.syncState = states[p.stateOrdinal];
                        this.dispatchCallbackOnClientThread(this.onSyncStateChangedCallback);
                     }
                  }
               }
               break;
            case MACRO_ASSIGNMENT_SYNC:
               if (packet instanceof autismclient.util.sync.LanSyncPacket.MacroAssignmentSyncPacket && packet.getSessionId().equals(this.sessionId)) {
                  autismclient.util.sync.LanSyncPacket.MacroAssignmentSyncPacket p = (autismclient.util.sync.LanSyncPacket.MacroAssignmentSyncPacket)packet;
                  if (!p.senderUsername.equals(this.localUsername)) {
                     this.applyRemoteMacroAssignments(p.senderUsername, p.assignments);
                  }
               }
         }
      } catch (Exception e) {
         AutismClientAddon.LOG.error("[Autism-LAN] Error handling packet: {}", packet.getType(), e);
      }
   }

   private void startTcpServer() {
      try {
         this.tcpServerSocket = new ServerSocket();
         this.tcpServerSocket.setReuseAddress(true);
         this.tcpServerSocket.bind(new InetSocketAddress(InetAddress.getLoopbackAddress(), 25568));
         this.tcpAcceptThread = new Thread(() -> {
            while (this.running.get() && this.isHost && this.tcpServerSocket != null && !this.tcpServerSocket.isClosed()) {
               try {
                  Socket clientSocket = this.tcpServerSocket.accept();
                  clientSocket.setTcpNoDelay(true);
                  this.acceptTcpClient(clientSocket);
               } catch (IOException e) {
                  if (this.running.get() && this.isHost) {
                     AutismClientAddon.LOG.warn("[TCP] Accept error: {}", e.getMessage());
                  }
               }
            }
         }, "TCP-Accept");
         this.tcpAcceptThread.setDaemon(true);
         this.tcpAcceptThread.start();
         AutismClientAddon.LOG.info("[TCP] Server started on port {}", 25568);
      } catch (IOException e) {
         AutismClientAddon.LOG.error("[TCP] Failed to start server: {}", e.getMessage());
      }
   }

   private void acceptTcpClient(Socket clientSocket) {
      (new Thread(() -> {
         try {
            DataInputStream in = new DataInputStream(clientSocket.getInputStream());
            DataOutputStream out = new DataOutputStream(clientSocket.getOutputStream());
            int packetType = in.readInt();
            String packetSession = in.readUTF();
            if (packetType == autismclient.util.sync.LanSyncPacketType.SEARCH_REQUEST.id()) {
               AutismClientAddon.LOG.info("[TCP] Received search request, responding with session info");
               out.writeInt(autismclient.util.sync.LanSyncPacketType.SESSION.id());
               out.writeUTF(this.sessionId);
               new autismclient.util.sync.LanSyncPacket.SessionPacket(this.sessionId, 0L, this.localUsername).write(out);
               out.flush();
               clientSocket.close();
               return;
            }

            if (packetType != autismclient.util.sync.LanSyncPacketType.JOIN.id()) {
               return;
            }

            autismclient.util.sync.LanSyncPacket.JoinPacket joinPacket = new autismclient.util.sync.LanSyncPacket.JoinPacket();
            joinPacket.setSessionId(packetSession);
            joinPacket.read(in);
            String clientUsername = joinPacket.username;
            if (clientUsername == null || clientUsername.isBlank()) {
               AutismClientAddon.LOG.warn("[TCP] Rejected client with empty username");
               clientSocket.close();
               return;
            }

            if (this.sessionId == null || !this.sessionId.equals(packetSession)) {
               AutismClientAddon.LOG.warn("[TCP] Rejected client '{}' with wrong/missing session code", clientUsername);
               clientSocket.close();
               return;
            }

            Socket oldSocket = this.hostClientSockets.put(clientUsername, clientSocket);
            if (oldSocket != null && oldSocket != clientSocket) {
               try {
                  oldSocket.close();
               } catch (IOException var281) {
               }
            }

            this.hostClientWriteLocks.put(clientUsername, new Object());
            this.peerLastHeartbeatMs.put(clientUsername, System.currentTimeMillis());
            if (!clientUsername.equals(this.localUsername) && !this.connectedClients.containsKey(clientUsername)) {
               this.connectedClients.put(clientUsername, new LanSyncManager.ClientInfo(clientUsername, false));
               AutismClientMessaging.sendPrefixed("§e" + clientUsername + " joined.");
               this.executionClientTimestampsMs.clear();
               this.broadcastClientList();
               this.dispatchCallbackOnClientThread(this.onClientJoinedCallback);
               this.dispatchCallbackOnClientThread(this.onPeerStateChangedCallback);
            }

            if (!clientUsername.equals(this.localUsername)) {
               this.sendLocalMacrosToClient(clientSocket, clientUsername);
               this.forwardPeerMacrosToClient(clientSocket, clientUsername);
            }

            this.sendClientListToSocket(clientSocket, clientUsername);
            AutismClientAddon.LOG.info("[TCP] Client joined: {}", clientUsername);

            while (this.running.get() && !clientSocket.isClosed()) {
               int nextPacketType = in.readInt();
               String nextPacketSession = in.readUTF();
               autismclient.util.sync.LanSyncPacketType type = autismclient.util.sync.LanSyncPacketType.byId(nextPacketType);
               autismclient.util.sync.LanSyncPacket packet = type == null ? null : autismclient.util.sync.LanSyncPacket.create(type, nextPacketSession);
               if (packet == null) {
                  throw new IOException("Unknown LAN packet id " + nextPacketType);
               }

               packet.read(in);
               this.handlePacket(packet);
            }
         } catch (Exception var28) {
            return;
         } finally {
            try {
               clientSocket.close();
            } catch (IOException var27) {
            }

            if (this.running.get() && this.isHost) {
               String disconnectedUser = null;

               for (Entry<String, Socket> entry : this.hostClientSockets.entrySet()) {
                  if (entry.getValue() == clientSocket) {
                     disconnectedUser = entry.getKey();
                     break;
                  }
               }

               if (disconnectedUser != null) {
                  this.hostClientSockets.remove(disconnectedUser);
                  this.hostClientWriteLocks.remove(disconnectedUser);
                  this.connectedClients.remove(disconnectedUser);
                  this.peerMacros.remove(disconnectedUser);
                  this.peerMacrosRevision.incrementAndGet();
                  this.peerLastHeartbeatMs.remove(disconnectedUser);
                  this.peerMacroStepProgress.remove(disconnectedUser);
                  AutismClientMessaging.sendPrefixed("§e" + disconnectedUser + " disconnected.");
                  AutismClientAddon.LOG.info("[TCP] Client disconnected: {}", disconnectedUser);
                  this.broadcastClientList();
                  this.dispatchCallbackOnClientThread(this.onClientLeftCallback);
                  MacroWaitRegistry.pollWaits();
               }
            }
         }
      }, "TCP-Client-Handler") {
         {
            this.setDaemon(true);
         }
      }).start();
   }

   private void sendClientListToSocket(Socket clientSocket, String clientUsername) {
      try {
         List<autismclient.util.sync.LanSyncPacket.ClientListPacket.ClientEntry> entries = new ArrayList<>();

         for (Entry<String, LanSyncManager.ClientInfo> entry : this.connectedClients.entrySet()) {
            entries.add(new autismclient.util.sync.LanSyncPacket.ClientListPacket.ClientEntry(entry.getKey(), entry.getValue().isHost));
         }

         autismclient.util.sync.LanSyncPacket.ClientListPacket packet = new autismclient.util.sync.LanSyncPacket.ClientListPacket(
            this.sessionId, entries, this.executionMethod.ordinal()
         );
         byte[] data = this.encodePacket(packet);
         synchronized (this.hostClientWriteLocks.computeIfAbsent(clientUsername, k -> new Object())) {
            clientSocket.getOutputStream().write(data);
            clientSocket.getOutputStream().flush();
         }
      } catch (IOException e) {
         AutismClientAddon.LOG.warn("[TCP] Failed to send client list: {}", e.getMessage());
      }
   }

   private void sendLocalMacrosToClient(Socket clientSocket, String clientUsername) {
      try {
         List<AutismMacro> localMacros = MacroLibraryManager.getInstance().getMacros();
         Object lock = this.hostClientWriteLocks.computeIfAbsent(clientUsername, k -> new Object());
         OutputStream out = clientSocket.getOutputStream();

         for (AutismMacro macro : localMacros) {
            this.writeMacroPackets(out, lock, this.localUsername, macro, (byte)0);
         }

         this.writePacketToStream(
            out, lock, new autismclient.util.sync.LanSyncPacket.MacroListPacket(this.sessionId, this.localUsername, extractMacroNames(localMacros))
         );
      } catch (IOException e) {
         AutismClientAddon.LOG.warn("[TCP] Failed to send macro list: {}", e.getMessage());
      }
   }

   private void forwardPeerMacrosToClient(Socket clientSocket, String clientUsername) {
      String session = this.sessionId;
      if (session != null) {
         try {
            Object lock = this.hostClientWriteLocks.computeIfAbsent(clientUsername, k -> new Object());
            OutputStream out = clientSocket.getOutputStream();

            for (String peer : this.connectedClients.keySet()) {
               if (!peer.equals(this.localUsername) && !peer.equals(clientUsername)) {
                  List<String> names = new ArrayList<>();
                  Map<String, AutismMacro> peerMacros = this.peerMacros.get(peer);
                  if (peerMacros != null) {
                     for (AutismMacro macro : peerMacros.values()) {
                        if (macro != null) {
                           this.writeMacroPackets(out, lock, peer, macro, (byte)0);
                           names.add(macro.name);
                        }
                     }
                  }

                  if (!names.isEmpty()) {
                     this.writePacketToStream(out, lock, new autismclient.util.sync.LanSyncPacket.MacroListPacket(session, peer, names));
                  }
               }
            }
         } catch (IOException e) {
            AutismClientAddon.LOG.warn("[TCP] Failed to forward peer rosters: {}", e.getMessage());
         }
      }
   }

   private void connectToHost(String hostIp) {
      (new Thread(() -> {
         try {
            this.clientSocket = new Socket();
            this.clientSocket.connect(new InetSocketAddress(InetAddress.getLoopbackAddress(), 25568), 5000);
            this.clientSocket.setTcpNoDelay(true);
            DataOutputStream out = new DataOutputStream(this.clientSocket.getOutputStream());
            autismclient.util.sync.LanSyncPacket.JoinPacket joinPacket = new autismclient.util.sync.LanSyncPacket.JoinPacket(this.sessionId, this.localUsername);
            synchronized (this.clientWriteLock) {
               out.writeInt(joinPacket.getType().id());
               out.writeUTF(joinPacket.getSessionId());
               joinPacket.write(out);
               out.flush();
            }

            AutismClientAddon.LOG.info("[TCP] Connected to host at {}:{}", hostIp, 25568);
            this.reconnecting = false;
            this.clientReaderThread = new Thread(() -> {
               try {
                  DataInputStream in = new DataInputStream(this.clientSocket.getInputStream());

                  while (this.running.get() && this.clientSocket != null && !this.clientSocket.isClosed()) {
                     int packetType = in.readInt();
                     String packetSession = in.readUTF();
                     autismclient.util.sync.LanSyncPacketType type = autismclient.util.sync.LanSyncPacketType.byId(packetType);
                     autismclient.util.sync.LanSyncPacket packet = type == null ? null : autismclient.util.sync.LanSyncPacket.create(type, packetSession);
                     if (packet == null) {
                        throw new IOException("Unknown LAN packet id " + packetType);
                     }

                     packet.read(in);
                     this.handlePacket(packet);
                  }
               } catch (Exception ex) {
                  AutismClientAddon.LOG.warn("[TCP] Disconnected from host: {}", ex.getMessage());
                  this.startReconnectLoop();
               }
            }, "TCP-Reader");
            this.clientReaderThread.setDaemon(true);
            this.clientReaderThread.start();
            this.sendPacket(new autismclient.util.sync.LanSyncPacket.RequestClientListPacket(this.sessionId));
            this.sendLocalMacrosToHost(out);
         } catch (IOException e) {
            AutismClientAddon.LOG.warn("[TCP] Failed to connect to host: {}", e.getMessage());
            this.startReconnectLoop();
         }
      }, "TCP-Connect") {
         {
            this.setDaemon(true);
         }
      }).start();
   }

   private void startReconnectLoop() {
      if (!this.reconnecting && this.running.get() && this.reconnectHostIp != null && this.reconnectSessionId != null && !this.isHost) {
         this.reconnecting = true;
         AutismClientMessaging.sendPrefixed("§eConnection lost. Reconnecting...");
         (new Thread(() -> {
            long backoffMs = 1000L;
            int attempt = 0;

            while (this.reconnecting && this.running.get() && this.reconnectHostIp != null) {
               try {
                  Thread.sleep(backoffMs);
               } catch (InterruptedException e) {
                  return;
               }

               if (!this.reconnecting || !this.running.get() || this.reconnectHostIp == null) {
                  return;
               }

               AutismClientAddon.LOG.info("[TCP] Reconnect attempt {}...", ++attempt);

               try {
                  Socket testSocket = new Socket();
                  testSocket.connect(new InetSocketAddress(InetAddress.getLoopbackAddress(), 25568), 2000);
                  testSocket.setTcpNoDelay(true);
                  testSocket.close();
                  this.sessionId = this.reconnectSessionId;
                  if (this.localUsername.isEmpty()) {
                     this.localUsername = this.resolvePlayerUsername();
                  }

                  this.connectedClients.putIfAbsent(this.localUsername, new LanSyncManager.ClientInfo(this.localUsername, false));
                  this.connectToHost(this.reconnectHostIp);
                  AutismClientMessaging.sendPrefixed("§aReconnected to session!");
                  return;
               } catch (IOException e) {
                  AutismClientAddon.LOG.warn("[TCP] Reconnect attempt {} failed", attempt);
                  backoffMs = Math.min(backoffMs * 2L, 16000L);
               }
            }

            this.reconnecting = false;
         }, "TCP-Reconnect") {
            {
               this.setDaemon(true);
            }
         }).start();
      }
   }

   private void sendLocalMacrosToHost(DataOutputStream out) {
      try {
         List<AutismMacro> localMacros = MacroLibraryManager.getInstance().getMacros();
         synchronized (this.clientWriteLock) {
            for (AutismMacro macro : localMacros) {
               this.writeMacroPackets(out, null, this.localUsername, macro, (byte)0);
            }

            this.writePacketToStream(
               out, null, new autismclient.util.sync.LanSyncPacket.MacroListPacket(this.sessionId, this.localUsername, extractMacroNames(localMacros))
            );
         }
      } catch (IOException e) {
         AutismClientAddon.LOG.warn("[TCP] Failed to send macro list: {}", e.getMessage());
      }
   }

   private byte[] encodePacket(autismclient.util.sync.LanSyncPacket packet) throws IOException {
      ByteArrayOutputStream baos = new ByteArrayOutputStream();
      DataOutputStream out = new DataOutputStream(baos);
      out.writeInt(packet.getType().id());
      out.writeUTF(packet.getSessionId());
      packet.write(out);
      return baos.toByteArray();
   }

   private void writeMacroPackets(OutputStream out, Object lock, String senderUsername, AutismMacro macro, byte sourceType) throws IOException {
      if (macro != null) {
         String nbtString = macro.toSharedTag().toString();

         for (autismclient.util.sync.LanSyncPacket packet : this.buildMacroPacketsFromNbt(senderUsername, macro.name, nbtString, sourceType)) {
            this.writePacketToStream(out, lock, packet);
         }
      }
   }

   private void writePacketToStream(OutputStream out, Object lock, autismclient.util.sync.LanSyncPacket packet) throws IOException {
      byte[] data = this.encodePacket(packet);
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

   private void sendPacket(autismclient.util.sync.LanSyncPacket packet) {
      if (!autismclient.modules.aD.read_Z()) {
         try {
            byte[] data = this.encodePacket(packet);
            if (this.isHost) {
               for (Entry<String, Socket> entry : this.hostClientSockets.entrySet()) {
                  Socket client = entry.getValue();
                  String clientName = entry.getKey();

                  try {
                     if (!client.isClosed()) {
                        synchronized (this.hostClientWriteLocks.computeIfAbsent(clientName, k -> new Object())) {
                           client.getOutputStream().write(data);
                           client.getOutputStream().flush();
                        }
                     }
                  } catch (IOException var12) {
                  }
               }
            } else {
               synchronized (this.clientWriteLock) {
                  if (this.clientSocket != null && !this.clientSocket.isClosed()) {
                     this.clientSocket.getOutputStream().write(data);
                     this.clientSocket.getOutputStream().flush();
                  }
               }
            }
         } catch (IOException e) {
            AutismClientAddon.LOG.warn("[TCP] Failed to send packet: {}", e.getMessage());
         }
      }
   }

   private void relayPacketToOthers(autismclient.util.sync.LanSyncPacket packet, String excludedUsername) {
      if (!autismclient.modules.aD.read_Z() && this.isHost) {
         try {
            byte[] data = this.encodePacket(packet);

            for (Entry<String, Socket> entry : this.hostClientSockets.entrySet()) {
               String clientName = entry.getKey();
               if (excludedUsername == null || !excludedUsername.equals(clientName)) {
                  Socket client = entry.getValue();

                  try {
                     if (!client.isClosed()) {
                        synchronized (this.hostClientWriteLocks.computeIfAbsent(clientName, k -> new Object())) {
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

   private void closeAllSockets() {
      try {
         if (this.tcpServerSocket != null) {
            this.tcpServerSocket.close();
            this.tcpServerSocket = null;
         }

         if (this.clientSocket != null) {
            this.clientSocket.close();
            this.clientSocket = null;
         }

         for (Socket s : this.hostClientSockets.values()) {
            try {
               s.close();
            } catch (IOException var4) {
            }
         }

         this.hostClientSockets.clear();
         this.hostClientWriteLocks.clear();
         this.incomingChunkTransfers.clear();
      } catch (IOException e) {
         AutismClientAddon.LOG.warn("[TCP] Stop error: {}", e.getMessage());
      }
   }

   private void broadcastClientList() {
      if (this.isHost) {
         List<autismclient.util.sync.LanSyncPacket.ClientListPacket.ClientEntry> entries = new ArrayList<>();

         for (Entry<String, LanSyncManager.ClientInfo> entry : this.connectedClients.entrySet()) {
            entries.add(new autismclient.util.sync.LanSyncPacket.ClientListPacket.ClientEntry(entry.getKey(), entry.getValue().isHost));
         }

         this.sendPacket(new autismclient.util.sync.LanSyncPacket.ClientListPacket(this.sessionId, entries, this.executionMethod.ordinal()));
      }
   }

   private void broadcastSyncStateUpdate(LanSyncManager.SyncState state, String detail) {
      if (this.isHost) {
         this.sendPacket(new autismclient.util.sync.LanSyncPacket.SyncStateUpdatePacket(this.sessionId, state.ordinal(), detail));
      }
   }

   public void broadcastLocalMacroRoster() {
      String session = this.sessionId;
      if (session != null) {
         List<AutismMacro> localMacros = MacroLibraryManager.getInstance().getMacros();
         String sender = this.localUsername;
         List<autismclient.util.sync.LanSyncPacket> packets = new ArrayList<>();

         for (AutismMacro macro : localMacros) {
            packets.addAll(this.buildMacroPackets(sender, macro, (byte)0));
         }

         packets.add(new autismclient.util.sync.LanSyncPacket.MacroListPacket(session, sender, extractMacroNames(localMacros)));
         this.enqueueAsyncPackets(packets);
      }
   }

   private void enqueueAsyncPackets(List<autismclient.util.sync.LanSyncPacket> packets) {
      if (!packets.isEmpty()) {
         try {
            this.asyncSendExecutor.execute(() -> {
               for (autismclient.util.sync.LanSyncPacket packet : packets) {
                  this.sendPacket(packet);
               }
            });
         } catch (RejectedExecutionException var3) {
         }
      }
   }

   private static List<String> extractMacroNames(List<AutismMacro> local) {
      List<String> names = new ArrayList<>(local.size());

      for (AutismMacro macro : local) {
         if (macro != null && macro.name != null) {
            names.add(macro.name);
         }
      }

      return names;
   }

   private void sendMacroPackets(String senderUsername, AutismMacro macro, byte sourceType) {
      for (autismclient.util.sync.LanSyncPacket packet : this.buildMacroPackets(senderUsername, macro, sourceType)) {
         this.sendPacket(packet);
      }
   }

   private List<autismclient.util.sync.LanSyncPacket> buildMacroPackets(String senderUsername, AutismMacro macro, byte sourceType) {
      return macro != null && this.sessionId != null ? this.buildMacroPacketsFromNbt(senderUsername, macro.name, macro.toSharedTag().toString(), sourceType) : List.of();
   }

   private List<autismclient.util.sync.LanSyncPacket> buildMacroPacketsFromNbt(String senderUsername, String macroName, String nbtData, byte sourceType) {
      String safeSender = senderUsername != null ? senderUsername : "";
      String safeName = macroName != null ? macroName : "";
      String safeNbt = nbtData != null ? nbtData : "";
      List<autismclient.util.sync.LanSyncPacket> packets = new ArrayList<>();
      if (computeModifiedUtf8Length(safeNbt) <= 60000) {
         packets.add(new autismclient.util.sync.LanSyncPacket.MacroDataPacket(this.sessionId, safeSender, safeName, safeNbt, sourceType));
         return packets;
      }

      byte[] bytes = safeNbt.getBytes(StandardCharsets.UTF_8);
      int totalChunks = Math.max(1, (bytes.length + 32000 - 1) / 32000);

      for (int i = 0; i < totalChunks; i++) {
         int start = i * 32000;
         int len = Math.min(32000, bytes.length - start);
         byte[] chunk = new byte[len];
         System.arraycopy(bytes, start, chunk, 0, len);
         packets.add(new autismclient.util.sync.LanSyncPacket.MacroDataChunkPacket(this.sessionId, safeSender, safeName, sourceType, i, totalChunks, chunk));
      }

      return packets;
   }

   private static int computeModifiedUtf8Length(String value) {
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

   private void handleMacroDataPacket(autismclient.util.sync.LanSyncPacket packet) {
      this.handleMacroDataPacket(packet, true);
   }

   private void handleMacroDataPacket(autismclient.util.sync.LanSyncPacket packet, boolean relayIfHost) {
      if (packet instanceof autismclient.util.sync.LanSyncPacket.MacroDataPacket && packet.getSessionId().equals(this.sessionId)) {
         autismclient.util.sync.LanSyncPacket.MacroDataPacket p = (autismclient.util.sync.LanSyncPacket.MacroDataPacket)packet;
         String sender = p.senderUsername;
         if (sender == null || !sender.equals(this.localUsername)) {
            try {
               CompoundTag nbt = TagParser.parseCompoundFully(p.nbtData);
               AutismMacro macro = new AutismMacro();
               macro.fromTag(nbt);
               String relayNbt = macro.toTag().toString();
               if (p.sourceType != 1) {
                  this.peerMacros.computeIfAbsent(sender, k -> new ConcurrentHashMap<>()).put(MacroNameNormalizer.normalize(macro.name), macro);
               }

               this.peerMacrosRevision.incrementAndGet();
               if (p.sourceType == 2) {
                  this.scheduleInstallSharedMacro(macro, sender);
               }

               if (relayIfHost && this.isHost && sender != null && !sender.isBlank()) {
                  this.relayPacketToOthers(new autismclient.util.sync.LanSyncPacket.MacroDataPacket(this.sessionId, sender, macro.name, relayNbt, p.sourceType), sender);
               }
            } catch (Exception e) {
               AutismClientAddon.LOG.error("[Autism-LAN] Failed to deserialize macro", e);
            }
         }
      }
   }

   private void handleMacroDataChunkPacket(autismclient.util.sync.LanSyncPacket packet) {
      if (packet instanceof autismclient.util.sync.LanSyncPacket.MacroDataChunkPacket && packet.getSessionId().equals(this.sessionId)) {
         autismclient.util.sync.LanSyncPacket.MacroDataChunkPacket p = (autismclient.util.sync.LanSyncPacket.MacroDataChunkPacket)packet;
         String sender = p.senderUsername;
         if ((sender == null || !sender.equals(this.localUsername))
            && p.totalChunks > 0
            && p.totalChunks <= 4096
            && p.chunkIndex >= 0
            && p.chunkIndex < p.totalChunks
            && p.chunkData != null
            && p.chunkData.length != 0
            && p.chunkData.length <= 32000) {
            if (this.isHost && sender != null && !sender.isBlank()) {
               this.relayPacketToOthers(
                  new autismclient.util.sync.LanSyncPacket.MacroDataChunkPacket(this.sessionId, sender, p.macroName, p.sourceType, p.chunkIndex, p.totalChunks, p.chunkData), sender
               );
            }

            this.purgeExpiredChunkTransfers();
            String key = sender + "\u0000" + p.macroName + "\u0000" + p.sourceType + "\u0000" + p.totalChunks;
            LanSyncManager.ChunkTransfer transfer = this.incomingChunkTransfers.computeIfAbsent(key, ignored -> new LanSyncManager.ChunkTransfer(p.totalChunks));
            byte[] complete = transfer.acceptChunk(p.chunkIndex, p.chunkData);
            if (complete != null) {
               this.incomingChunkTransfers.remove(key);
               String nbtData = new String(complete, StandardCharsets.UTF_8);
               this.handleMacroDataPacket(new autismclient.util.sync.LanSyncPacket.MacroDataPacket(this.sessionId, sender, p.macroName, nbtData, p.sourceType), false);
            }
         }
      }
   }

   private void purgeExpiredChunkTransfers() {
      long now = System.currentTimeMillis();
      this.incomingChunkTransfers.entrySet().removeIf(entry -> now - entry.getValue().createdAtMs > 120000L);
   }

   private void scheduleInstallSharedMacro(AutismMacro macro, String sender) {
      if (macro != null && macro.name != null && !macro.name.isBlank()) {
         Minecraft.getInstance().execute(() -> this.installSharedMacroOnClientThread(macro, sender));
      }
   }

   private void installSharedMacroOnClientThread(AutismMacro macro, String sender) {
      MacroLibraryManager manager = MacroLibraryManager.getInstance();
      AutismMacro existing = manager.findByName(macro.name);
      if (existing != null) {
         AutismMacro fresh = macro.copyWithName(macro.name);
         existing.name = fresh.name;
         existing.description = fresh.description;
         existing.folder = fresh.folder;
         existing.keyCode = fresh.keyCode;
         existing.actions = new ArrayList<>(fresh.actions);
         existing.loop = fresh.loop;
         existing.loopCount = fresh.loopCount;
         manager.save();
         AutismClientMessaging.sendPrefixed("§aUpdated macro from " + sender + ": " + existing.name);
      } else {
         AutismMacro installed = macro.copyWithName(macro.name);
         manager.saveMacro(installed);
         AutismClientMessaging.sendPrefixed("§aReceived macro from " + sender + ": " + installed.name);
      }
   }

   private void handleRequestMacroPacket(autismclient.util.sync.LanSyncPacket packet) {
      if (packet instanceof autismclient.util.sync.LanSyncPacket.RequestMacroPacket && packet.getSessionId().equals(this.sessionId)) {
         autismclient.util.sync.LanSyncPacket.RequestMacroPacket p = (autismclient.util.sync.LanSyncPacket.RequestMacroPacket)packet;
         String requestedName = p.macroName == null ? "" : p.macroName.trim();
         if (!requestedName.isEmpty()) {
            AutismMacro macro = MacroLibraryManager.getInstance().findByName(requestedName);
            if (macro != null) {
               this.sendMacroPackets(this.localUsername, macro, (byte)2);
            }
         }
      }
   }

   private void handlePresetDataPacket(autismclient.util.sync.LanSyncPacket packet) {
      if (packet instanceof autismclient.util.sync.LanSyncPacket.PresetDataPacket && packet.getSessionId().equals(this.sessionId)) {
         autismclient.util.sync.LanSyncPacket.PresetDataPacket p = (autismclient.util.sync.LanSyncPacket.PresetDataPacket)packet;
         if ((p.senderUsername == null || !p.senderUsername.equals(this.localUsername))
            && PacketPresetManager.getInstance().importShared(p.presetName, p.presetJson, p.senderUsername)
            && this.isHost
            && p.senderUsername != null
            && !p.senderUsername.isBlank()) {
            this.relayPacketToOthers(new autismclient.util.sync.LanSyncPacket.PresetDataPacket(this.sessionId, p.senderUsername, p.presetName, p.presetJson), p.senderUsername);
         }
      }
   }

   private void handleMacroDeletePacket(autismclient.util.sync.LanSyncPacket packet) {
      if (packet instanceof autismclient.util.sync.LanSyncPacket.MacroDeletePacket && packet.getSessionId().equals(this.sessionId)) {
         autismclient.util.sync.LanSyncPacket.MacroDeletePacket p = (autismclient.util.sync.LanSyncPacket.MacroDeletePacket)packet;
         if (p.senderUsername == null || !p.senderUsername.equals(this.localUsername)) {
            this.peerMacrosRevision.incrementAndGet();
            Map<String, AutismMacro> senderMacros = this.peerMacros.get(p.senderUsername);
            if (senderMacros != null) {
               senderMacros.remove(MacroNameNormalizer.normalize(p.macroName));
            }

            if (this.isHost && p.senderUsername != null && !p.senderUsername.isBlank()) {
               this.relayPacketToOthers(new autismclient.util.sync.LanSyncPacket.MacroDeletePacket(this.sessionId, p.senderUsername, p.macroName), p.senderUsername);
            }
         }
      }
   }

   private void handleMacroListPacket(autismclient.util.sync.LanSyncPacket packet) {
      if (packet instanceof autismclient.util.sync.LanSyncPacket.MacroListPacket && packet.getSessionId().equals(this.sessionId)) {
         autismclient.util.sync.LanSyncPacket.MacroListPacket p = (autismclient.util.sync.LanSyncPacket.MacroListPacket)packet;
         if (p.senderUsername != null && !p.senderUsername.isBlank() && !p.senderUsername.equals(this.localUsername)) {
            Set<String> live = new HashSet<>();

            for (String name : p.macroNames) {
               live.add(MacroNameNormalizer.normalize(name));
            }

            this.peerMacrosRevision.incrementAndGet();
            Map<String, AutismMacro> senderMacros = this.peerMacros.get(p.senderUsername);
            if (senderMacros != null) {
               senderMacros.keySet().retainAll(live);
            }

            if (this.isHost) {
               this.relayPacketToOthers(new autismclient.util.sync.LanSyncPacket.MacroListPacket(this.sessionId, p.senderUsername, new ArrayList<>(p.macroNames)), p.senderUsername);
            }
         }
      }
   }

   private void handleQueueSync(String queueData, String senderUsername) {
      try {
         if (senderUsername != null && senderUsername.equals(this.localUsername)) {
            return;
         }

         AutismSessionState.DelayedPacket.resetIdCounter();
         List<AutismSessionState.DelayedPacket> receivedQueue = MacroClipboardCodec.decodeQueue(queueData);
         if (receivedQueue == null || receivedQueue.isEmpty()) {
            AutismClientMessaging.sendPrefixed("§cReceived empty queue");
            return;
         }

         AutismSessionState.getInstance().replaceDelayedPackets(receivedQueue);
         AutismClientMessaging.sendPrefixed("§aReceived queue: " + receivedQueue.size() + " packets");
      } catch (Exception e) {
         AutismClientAddon.LOG.error("[Autism-LAN] Failed to process queue sync", e);
         AutismClientMessaging.sendPrefixed("§cFailed to receive queue");
      }
   }

   private void handleChatOrControlMessage(String senderUsername, String message) {
      String safeSender = senderUsername != null && !senderUsername.isBlank() ? senderUsername : "Peer";
      String safeMessage = message == null ? "" : message.trim();
      if (!safeMessage.isEmpty()) {
         if ("__SYNC__".equals(safeSender)) {
            AutismClientMessaging.sendPrefixed(safeMessage);
         } else if ("__STOP_ALL__".equals(safeSender)) {
            MacroExecutor.stopAll();
            AutismClientMessaging.sendPrefixed("§c[LAN] Macros stopped by " + safeMessage);
         } else if ("__SPREAD_DATA__".equals(safeSender)) {
            try {
               String[] parts = safeMessage.split("\t");
               if (parts.length >= 3) {
                  long nanos = Long.parseLong(parts[0]);
                  String lateClient = parts[1];
                  int count = Integer.parseInt(parts[2]);
                  this.lastSpreadResult = new LanSyncManager.SpreadResult(nanos, lateClient, count);
                  this.spreadHistory.add(this.lastSpreadResult);

                  while (this.spreadHistory.size() > 10) {
                     this.spreadHistory.remove(0);
                  }

                  this.dispatchCallbackOnClientThread(this.onSpreadUpdatedCallback);
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
                  this.lastSpreadResult = new LanSyncManager.SpreadResult(
                     maxOffset * 50000000L, perfect ? "" : "tick", count, LanSyncManager.ExecutionMethod.TICK, targetTick, maxOffset, perfect, tickReport
                  );
                  this.spreadHistory.add(this.lastSpreadResult);

                  while (this.spreadHistory.size() > 10) {
                     this.spreadHistory.remove(0);
                  }

                  this.dispatchCallbackOnClientThread(this.onSpreadUpdatedCallback);
               }
            } catch (NumberFormatException var12) {
            }
         } else {
            boolean isExecMethodControl = "__EXEC_METHOD__".equals(safeSender) || safeMessage != null && safeMessage.startsWith("__EXEC_METHOD__:");
            if (!isExecMethodControl) {
               if (!safeSender.equals(this.localUsername)) {
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

               LanSyncManager.ExecutionMethod method = this.parseExecutionMethod(modeName);
               if (this.isHost) {
                  if (this.syncState != LanSyncManager.SyncState.IDLE && this.syncState != LanSyncManager.SyncState.DONE) {
                     this.sendPacket(
                        new autismclient.util.sync.LanSyncPacket.ChatMessagePacket(this.sessionId, this.localUsername, "__EXEC_METHOD__:" + this.executionMethod.name())
                     );
                     return;
                  }

                  this.executionMethod = method;
                  this.broadcastClientList();
                  this.sendPacket(new autismclient.util.sync.LanSyncPacket.ChatMessagePacket(this.sessionId, originator, "__EXEC_METHOD__:" + this.executionMethod.name()));
                  if (!originator.equals(this.localUsername)) {
                     AutismClientMessaging.sendPrefixed("§eExecution method set to " + this.executionMethod.name() + " by " + originator);
                  }

                  this.dispatchCallbackOnClientThread(this.onPeerStateChangedCallback);
               } else {
                  this.executionMethod = method;
                  if (!originator.equals(this.localUsername)) {
                     AutismClientMessaging.sendPrefixed("§eExecution method set to " + this.executionMethod.name() + " by " + originator);
                  }

                  this.dispatchCallbackOnClientThread(this.onPeerStateChangedCallback);
               }
            }
         }
      }
   }

   private void handleSyncRequest(String senderUsername, boolean isMacro, String macroName, String command) {
      if (this.canExecuteInWorld() && senderUsername != null && !senderUsername.equals(this.localUsername)) {
         if (!this.isHost) {
            AutismClientAddon.LOG.warn("[Sync] Non-host received REQUEST_SYNC from {} - ignoring", senderUsername);
         } else {
            AutismClientAddon.LOG.info("[Sync] Received sync request from {}: isMacro={}, name={}, cmd={}", senderUsername, isMacro, macroName, command);
            if ("__QUEUE_FLUSH__".equals(command)) {
               this.initiateQueueFlushGoSync();
            } else if (command != null && command.startsWith("__DELAY_PACKETS__\t")) {
               this.initiateGoSync(false, "", command);
            } else if (command != null && command.startsWith("__PER_USER__\t")) {
               String assignStr = command.substring("__PER_USER__\t".length());
               String[] parts = assignStr.split("\t");
               Map<String, String> assignments = new LinkedHashMap<>();

               for (int pi = 0; pi + 1 < parts.length; pi += 2) {
                  assignments.put(parts[pi], parts[pi + 1]);
               }

               this.initiateGoSync(isMacro, macroName, "", assignments);
            } else {
               this.initiateGoSync(isMacro, macroName, command);
            }
         }
      }
   }

   private void handleMacroStepProgress(String senderUsername, int completedStep, int totalSteps, String macroName) {
      if (senderUsername != null && !senderUsername.equals(this.localUsername)) {
         if (this.isHost) {
            this.sendPacket(new autismclient.util.sync.LanSyncPacket.MacroStepProgressPacket(this.sessionId, senderUsername, completedStep, totalSteps, macroName));
         }

         if (completedStep < 0) {
            this.peerMacroStepProgress.remove(senderUsername);
         } else {
            this.peerMacroStepProgress.put(senderUsername, completedStep);
         }

         MacroWaitRegistry.pollWaits();
         Runnable listener = this.onStepProgressChangedCallback;
         if (listener != null) {
            listener.run();
         }
      }
   }

   private void handleOffsetSync(String senderUsername, String targetUsername, int offsetMs) {
      if (senderUsername != null && !senderUsername.equals(this.localUsername)) {
         LanSyncManager.ClientInfo info = this.connectedClients.get(targetUsername);
         if (info != null) {
            info.offsetMs = Math.max(0, offsetMs);
            AutismClientAddon.LOG.info("[Sync] Offset for {} set to {}ms by {}", targetUsername, offsetMs, senderUsername);
         }

         if (this.isHost) {
            this.sendPacket(new autismclient.util.sync.LanSyncPacket.OffsetSyncPacket(this.sessionId, senderUsername, targetUsername, offsetMs));
         }
      }
   }

   public boolean isRunning() {
      return this.running.get();
   }

   private boolean isSyncIdle() {
      return this.syncState == LanSyncManager.SyncState.IDLE || this.syncState == LanSyncManager.SyncState.DONE;
   }

   private boolean canExecuteInWorld() {
      Minecraft minecraft = Minecraft.getInstance();
      return !this.configurationOnly
         && !autismclient.modules.aD.read_Z()
         && minecraft != null
         && minecraft.level != null
         && minecraft.player != null
         && minecraft.getConnection() != null;
   }

   public void setConfigurationOnly(boolean configurationOnly) {
      this.configurationOnly = configurationOnly;
      if (configurationOnly) {
         this.activeExecution = null;
         this.scheduledTickExecution = null;
         this.executedIds.clear();
         if (this.syncState != LanSyncManager.SyncState.IDLE && this.syncState != LanSyncManager.SyncState.DONE) {
            this.setSyncState(LanSyncManager.SyncState.IDLE);
         }
      }
   }

   public boolean isConfigurationOnly() {
      return this.configurationOnly;
   }

   public boolean isActiveOrConnecting() {
      return this.running.get() && (this.sessionId != null || this.searching || this.reconnecting);
   }

   public boolean isInSession() {
      return this.sessionId != null;
   }

   public LanSyncManager.ExecutionMethod getExecutionMethod() {
      return this.executionMethod;
   }

   public int getTickSyncLeadTicks() {
      return 10;
   }

   public void setExecutionMethod(LanSyncManager.ExecutionMethod method) {
      LanSyncManager.ExecutionMethod next = method != null ? method : LanSyncManager.ExecutionMethod.INSTANT;
      if (this.syncState != LanSyncManager.SyncState.IDLE && this.syncState != LanSyncManager.SyncState.DONE) {
         AutismClientMessaging.sendPrefixed("§cCannot change execution method while LAN Sync is active.");
      } else if (this.sessionId != null && !this.isHost) {
         this.sendPacket(new autismclient.util.sync.LanSyncPacket.ChatMessagePacket(this.sessionId, this.localUsername, "__EXEC_METHOD__:" + next.name()));
         AutismClientMessaging.sendPrefixed("§eRequested execution method: " + next.name());
      } else {
         LanSyncManager.ExecutionMethod previous = this.executionMethod;
         this.executionMethod = next;
         if (this.sessionId != null) {
            this.broadcastClientList();
            this.sendPacket(new autismclient.util.sync.LanSyncPacket.ChatMessagePacket(this.sessionId, this.localUsername, "__EXEC_METHOD__:" + next.name()));
         }

         if (previous != next) {
            AutismClientMessaging.sendPrefixed("§eExecution method set to " + next.name());
         }

         this.dispatchCallbackOnClientThread(this.onPeerStateChangedCallback);
      }
   }

   public void toggleExecutionMethod() {
      this.setExecutionMethod(this.executionMethod == LanSyncManager.ExecutionMethod.INSTANT ? LanSyncManager.ExecutionMethod.TICK : LanSyncManager.ExecutionMethod.INSTANT);
   }

   public void refreshClientList() {
      if (this.sessionId != null) {
         if (this.isHost) {
            this.broadcastClientList();
         } else {
            this.sendPacket(new autismclient.util.sync.LanSyncPacket.RequestClientListPacket(this.sessionId));
         }
      }
   }

   public boolean isHost() {
      return this.isHost;
   }

   public String getSessionId() {
      return this.sessionId;
   }

   public int getConnectedClientCount() {
      return this.connectedClients.size();
   }

   public boolean isSearching() {
      return this.searching;
   }

   public int getSearchSecondsRemaining() {
      return this.searchTicksRemaining / 20;
   }

   public boolean isReconnecting() {
      return this.reconnecting;
   }

   public LanSyncManager.SyncState getSyncState() {
      return this.syncState;
   }

   public LanSyncManager.ExecutionContext getActiveExecution() {
      return this.activeExecution;
   }

   public LanSyncManager.SpreadResult getLastSpreadResult() {
      return this.lastSpreadResult;
   }

   public List<LanSyncManager.SpreadResult> getSpreadHistory() {
      return new ArrayList<>(this.spreadHistory);
   }

   public String getLocalUsername() {
      return this.localUsername;
   }

   public Map<String, LanSyncManager.ClientInfo> getConnectedClients() {
      return new ConcurrentHashMap<>(this.connectedClients);
   }

   public void notifyMacroDeleted(String macroName) {
      if (this.sessionId != null) {
         this.sendPacket(new autismclient.util.sync.LanSyncPacket.MacroDeletePacket(this.sessionId, this.localUsername, macroName));
      }
   }

   public void broadcastQueueData(String queueData) {
      if (!this.configurationOnly && this.isInSession()) {
         this.sendPacket(new autismclient.util.sync.LanSyncPacket.QueueSyncPacket(this.sessionId, queueData, this.localUsername));
      }
   }

   public void stopAllMacrosOnLan() {
      if (!this.configurationOnly && this.isInSession()) {
         MacroExecutor.stopAll();
         AutismClientMessaging.sendPrefixed("§c[LAN] Macros stopped by " + this.localUsername);
         this.sendPacket(new autismclient.util.sync.LanSyncPacket.ChatMessagePacket(this.sessionId, "__STOP_ALL__", this.localUsername));
      }
   }

   public boolean sendSyncedChat(String message) {
      if (!this.canExecuteInWorld()) {
         AutismClientMessaging.sendPrefixed("§cLAN Sync execution requires a connected world.");
         return false;
      }

      String safeMessage = message == null ? "" : message.trim();
      if (safeMessage.isEmpty()) {
         return false;
      }

      if (!this.isInSession()) {
         AutismClientMessaging.sendPrefixed("Join a LAN Sync session first.");
         return false;
      }

      if (!this.isSyncIdle()) {
         AutismClientMessaging.sendPrefixed("§cLAN Sync is busy with another synchronized action.");
         return false;
      }

      String command = "__CHAT__\t" + safeMessage;
      if (this.isHost) {
         this.initiateGoSync(false, null, command);
      } else {
         autismclient.util.sync.LanSyncPacket.RequestSyncPacket request = new autismclient.util.sync.LanSyncPacket.RequestSyncPacket(this.sessionId, this.localUsername, false, "", command);
         request.executionMethod = this.executionMethod.ordinal();
         this.sendPacket(request);
         AutismClientMessaging.sendPrefixed("§eRequested sync chat from host...");
      }

      return true;
   }

   public int getSessionPeerCount() {
      return this.connectedClients.size();
   }

   public void reportMacroStepProgress(int completedStep, int totalSteps, String macroName) {
      if (this.sessionId != null) {
         if (completedStep < 0) {
            this.peerMacroStepProgress.remove(this.localUsername);
         } else {
            this.peerMacroStepProgress.put(this.localUsername, completedStep);
         }

         this.sendPacket(new autismclient.util.sync.LanSyncPacket.MacroStepProgressPacket(this.sessionId, this.localUsername, completedStep, totalSteps, macroName != null ? macroName : ""));
         MacroWaitRegistry.pollWaits();
      }
   }

   public int getPeerCompletedStep(String username) {
      return this.peerMacroStepProgress.getOrDefault(username, 0);
   }

   public Map<String, Integer> getPeerStepProgressMap() {
      return new ConcurrentHashMap<>(this.peerMacroStepProgress);
   }

   public void clearStepProgress() {
      this.peerMacroStepProgress.clear();
   }

   public void setOnStepProgressChanged(Runnable callback) {
      this.onStepProgressChangedCallback = callback;
   }

   public long getPeerMacrosRevision() {
      return this.peerMacrosRevision.get();
   }

   public Map<String, Map<String, AutismMacro>> getPeerMacros() {
      return new HashMap<>(this.peerMacros);
   }

   public List<String> findPeersMissingMacro(String macroName) {
      List<String> missing = new ArrayList<>();
      if (macroName != null && !macroName.isBlank()) {
         Map<String, Map<String, AutismMacro>> remote = this.getPeerMacros();

         for (String peer : new TreeSet<>(this.connectedClients.keySet())) {
            if (!peer.equals(this.localUsername)) {
               Map<String, AutismMacro> peerMacros = remote.get(peer);
               if (peerMacros == null || !peerMacros.containsKey(MacroNameNormalizer.normalize(macroName))) {
                  missing.add(peer);
               }
            }
         }

         return missing;
      } else {
         return missing;
      }
   }

   public Map<String, String> findMissingAssignedMacros(Map<String, String> assignments) {
      Map<String, String> missing = new LinkedHashMap<>();
      if (assignments != null && !assignments.isEmpty()) {
         Map<String, Map<String, AutismMacro>> remote = this.getPeerMacros();

         for (Entry<String, String> entry : new LinkedHashMap<>(assignments).entrySet()) {
            String username = entry.getKey();
            String macroName = entry.getValue();
            if (username != null && !username.isBlank() && macroName != null && !macroName.isBlank()) {
               if (username.equals(this.localUsername)) {
                  if (MacroLibraryManager.getInstance().findByName(macroName) == null) {
                     missing.put(username, macroName);
                  }
               } else {
                  Map<String, AutismMacro> peerMacros = remote.get(username);
                  if (peerMacros == null || !peerMacros.containsKey(MacroNameNormalizer.normalize(macroName))) {
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

   public List<String> getRemoteMacroNames() {
      Map<String, String> remoteMacros = new LinkedHashMap<>();

      for (Entry<String, Map<String, AutismMacro>> entry : this.peerMacros.entrySet()) {
         if (!entry.getKey().equals(this.localUsername)) {
            for (AutismMacro macro : entry.getValue().values()) {
               if (macro != null) {
                  String key = MacroNameNormalizer.normalize(macro.name);
                  remoteMacros.putIfAbsent(key, macro.name);
               }
            }
         }
      }

      return new ArrayList<>(remoteMacros.values());
   }

   public void importPeerMacro(String clientUsername, String macroName) {
      Map<String, AutismMacro> clientMacros = this.peerMacros.get(clientUsername);
      AutismMacro remoteMacro = clientMacros == null ? null : clientMacros.get(MacroNameNormalizer.normalize(macroName));
      if (remoteMacro == null) {
         AutismClientMessaging.sendPrefixed("§cMacro not found: " + macroName);
      } else {
         String newName = macroName;

         for (int suffix = 1; MacroLibraryManager.getInstance().findByName(newName) != null; suffix++) {
            newName = macroName + " (" + suffix + ")";
         }

         AutismMacro newMacro = new AutismMacro(newName);
         newMacro.actions.addAll(remoteMacro.actions);
         newMacro.folder = MacroFolderLayout.sanitize(remoteMacro.folder);
         MacroLibraryManager.getInstance().saveMacro(newMacro);
         MacroLibraryManager.getInstance().save();
         AutismClientMessaging.sendPrefixed("§aImported macro: " + newName);
         AutismClientAddon.LOG.info("[Autism-LAN] Imported macro '{}' from {}", newName, clientUsername);
      }
   }

   public void broadcastMacroAssignments(Map<String, String> assignments) {
      if (this.sessionId != null && this.isInSession()) {
         this.syncedMacroAssignments.clear();
         this.syncedMacroAssignments.putAll(assignments);
         this.syncedAssignmentsSender = this.localUsername;
         StringBuilder sb = new StringBuilder();

         for (Entry<String, String> entry : assignments.entrySet()) {
            if (sb.length() > 0) {
               sb.append('\t');
            }

            sb.append(entry.getKey()).append('\t').append(entry.getValue());
         }

         this.sendPacket(new autismclient.util.sync.LanSyncPacket.MacroAssignmentSyncPacket(this.sessionId, this.localUsername, sb.toString()));
      }
   }

   private void applyRemoteMacroAssignments(String sender, String encoded) {
      this.syncedMacroAssignments.clear();
      this.syncedAssignmentsSender = sender;
      if (encoded != null && !encoded.isEmpty()) {
         String[] parts = encoded.split("\t");

         for (int i = 0; i + 1 < parts.length; i += 2) {
            this.syncedMacroAssignments.put(parts[i], parts[i + 1]);
         }
      }

      this.dispatchCallbackOnClientThread(this.onAssignmentsChangedCallback);
   }

   public Map<String, String> getSyncedMacroAssignments() {
      return new LinkedHashMap<>(this.syncedMacroAssignments);
   }

   public String getSyncedAssignmentsSender() {
      return this.syncedAssignmentsSender;
   }

   public void setOnAssignmentsChanged(Runnable callback) {
      this.onAssignmentsChangedCallback = callback;
   }

   public List<String> getPeerMacroNames(String peerUsername) {
      Map<String, AutismMacro> peerMacros = this.peerMacros.get(peerUsername);
      if (peerMacros != null && !peerMacros.isEmpty()) {
         List<String> names = new ArrayList<>(peerMacros.size());

         for (AutismMacro macro : peerMacros.values()) {
            if (macro != null) {
               names.add(macro.name);
            }
         }

         return names;
      } else {
         return Collections.emptyList();
      }
   }

   public boolean shareMacroWithPeers(String macroName) {
      if (this.sessionId == null || !this.isInSession()) {
         AutismClientMessaging.sendPrefixed("§cJoin a session first.");
         return false;
      }

      if (macroName != null && !macroName.isBlank()) {
         AutismMacro macro = MacroLibraryManager.getInstance().findByName(macroName);
         if (macro == null) {
            AutismClientMessaging.sendPrefixed("§cMacro not found: " + macroName);
            return false;
         } else {
            this.sendMacroPackets(this.localUsername, macro, (byte)2);
            AutismClientMessaging.sendPrefixed("§aSent macro to peers: " + macro.name);
            return true;
         }
      } else {
         AutismClientMessaging.sendPrefixed("§cSelect a macro first.");
         return false;
      }
   }

   public void sharePresetWithPeers(String presetName) {
      if (this.sessionId == null) {
         AutismClientMessaging.sendPrefixed("§cNot in session.");
      } else {
         File file = new File(new File(AutismClientAddon.FOLDER, "presets"), presetName + ".json");
         if (!file.exists()) {
            AutismClientMessaging.sendPrefixed("§cPreset not found: " + presetName);
         } else {
            try {
               String json = new String(Files.readAllBytes(file.toPath()), StandardCharsets.UTF_8);
               this.sendPacket(new autismclient.util.sync.LanSyncPacket.PresetDataPacket(this.sessionId, this.localUsername, presetName, json));
               AutismClientMessaging.sendPrefixed("§aShared preset '" + presetName + "' with LAN.");
            } catch (IOException e) {
               AutismClientMessaging.sendPrefixed("§cFailed to read preset: " + e.getMessage());
            }
         }
      }
   }

   public void requestMacroFromPeers(String macroName) {
      if (this.sessionId != null && this.isInSession()) {
         this.sendPacket(new autismclient.util.sync.LanSyncPacket.RequestMacroPacket(this.sessionId, macroName));
         AutismClientMessaging.sendPrefixed("§eRequesting macro: " + macroName);
      } else {
         AutismClientMessaging.sendPrefixed("§cNot in a LAN Sync session!");
      }
   }

   public void triggerActionSync(String targetName, boolean isMacro) {
      if (this.isInSession()) {
         if (isMacro) {
            this.triggerMacroSync(targetName);
         } else {
            this.triggerQueueFlushSync();
         }
      }
   }

   private String formatNamePreview(List<String> names) {
      if (names != null && !names.isEmpty()) {
         return names.size() <= 3 ? String.join(", ", names) : String.join(", ", names.subList(0, 3)) + " +" + (names.size() - 3) + " more";
      } else {
         return "";
      }
   }

   private String formatAssignmentPreview(Map<String, String> assignments) {
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

   public void setOnClientJoined(Runnable callback) {
      this.onClientJoinedCallback = callback;
   }

   public void setOnClientLeft(Runnable callback) {
      this.onClientLeftCallback = callback;
   }

   public void setUnusedEventCallback1(Runnable callback) {
      this.unusedEventCallback1 = callback;
   }

   public void setUnusedEventCallback2(Runnable callback) {
      this.unusedEventCallback2 = callback;
   }

   public void setOnSessionChanged(Runnable callback) {
      this.onSessionChangedCallback = callback;
   }

   public void setOnSyncStateChanged(Runnable callback) {
      this.onSyncStateChangedCallback = callback;
   }

   public void setOnSpreadUpdated(Runnable callback) {
      this.onSpreadUpdatedCallback = callback;
   }

   public void setOnPeerStateChanged(Runnable callback) {
      this.onPeerStateChangedCallback = callback;
   }

   private void dispatchCallbackOnClientThread(Runnable callback) {
      if (callback != null) {
         Minecraft.getInstance().execute(callback);
      }
   }

   private String generateSessionCode() {
      return String.format("%04d", (int)(Math.random() * 10000.0));
   }

   private String resolvePlayerUsername() {
      Minecraft mc = Minecraft.getInstance();
      return mc.player != null ? mc.player.getName().getString() : "Player";
   }

   public void setClientOffsetMs(String username, int offsetMs) {
      LanSyncManager.ClientInfo info = this.connectedClients.get(username);
      if (info != null) {
         int clamped = Math.max(0, offsetMs);
         info.offsetMs = clamped;
         if (this.sessionId != null) {
            this.sendPacket(new autismclient.util.sync.LanSyncPacket.OffsetSyncPacket(this.sessionId, this.localUsername, username, clamped));
         }
      }
   }

   public int getClientOffsetMs(String username) {
      LanSyncManager.ClientInfo info = this.connectedClients.get(username);
      return info != null ? info.offsetMs : 0;
   }

   public void broadcastSyncedChat(String message) {
      this.sendSyncedChat(message);
   }

   private static final class ChunkTransfer {
      private final long createdAtMs = System.currentTimeMillis();
      private final byte[][] chunks;
      private int receivedChunks;
      private int totalBytes;

      private ChunkTransfer(int totalChunks) {
         this.chunks = new byte[totalChunks][];
      }

      private synchronized byte[] acceptChunk(int index, byte[] data) {
         if (index >= 0 && index < this.chunks.length && data != null) {
            if (this.chunks[index] == null) {
               this.chunks[index] = data;
               this.receivedChunks++;
               this.totalBytes += data.length;
            }

            if (this.receivedChunks != this.chunks.length) {
               return null;
            }

            byte[] complete = new byte[this.totalBytes];
            int offset = 0;

            for (byte[] chunk : this.chunks) {
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

   public enum PeerHealth {
      HEALTHY,
      STALE,
      UNKNOWN;
   }

   private static final class ScheduledTickExecution {
      final long executionId;
      final long targetTick;
      volatile long fireAtNano = 0L;
      volatile Thread timerThread = null;

      ScheduledTickExecution(long executionId, long targetTick) {
         this.executionId = executionId;
         this.targetTick = targetTick;
      }
   }

   public static class ClientInfo {
      public String username;
      public boolean isHost;
      public long joinedAtMs;
      public int offsetMs;

      ClientInfo(String username, boolean isHost) {
         this.username = username;
         this.isHost = isHost;
         this.joinedAtMs = System.currentTimeMillis();
         this.offsetMs = 0;
      }
   }

   public enum SyncState {
      IDLE,
      PREPARING,
      ALL_READY,
      DISPATCHING_GO,
      EXECUTING,
      REPORTING,
      DONE;
   }

   public static class ExecutionContext {
      public final long executionId;
      public final boolean isMacro;
      public final String macroName;
      public final String command;
      public final AutismMacro macro;
      public final boolean isInitiator;
      public final LanSyncManager.ExecutionMethod executionMethod;
      public volatile long targetTick;

      public ExecutionContext(long executionId, boolean isMacro, String macroName, String command, AutismMacro macro, boolean isInitiator) {
         this(executionId, isMacro, macroName, command, macro, isInitiator, LanSyncManager.ExecutionMethod.INSTANT, -1L);
      }

      public ExecutionContext(
         long executionId,
         boolean isMacro,
         String macroName,
         String command,
         AutismMacro macro,
         boolean isInitiator,
         LanSyncManager.ExecutionMethod executionMethod,
         long targetTick
      ) {
         this.executionId = executionId;
         this.isMacro = isMacro;
         this.macroName = macroName;
         this.command = command;
         this.macro = macro;
         this.isInitiator = isInitiator;
         this.executionMethod = executionMethod != null ? executionMethod : LanSyncManager.ExecutionMethod.INSTANT;
         this.targetTick = targetTick;
      }
   }

   private static final class TickReport {
      final long actualTick;
      final boolean late;

      TickReport(long actualTick, boolean late) {
         this.actualTick = actualTick;
         this.late = late;
      }
   }

   public enum ExecutionMethod {
      INSTANT,
      TICK;
   }

   public static class SpreadResult {
      public final long spreadNanos;
      public final String lateClient;
      public final int clientCount;
      public final LanSyncManager.ExecutionMethod executionMethod;
      public final long targetTick;
      public final int maxTickOffset;
      public final boolean perfectTickSync;
      public final String tickReport;

      public SpreadResult(long spreadNanos, String lateClient, int clientCount) {
         this(spreadNanos, lateClient, clientCount, LanSyncManager.ExecutionMethod.INSTANT, -1L, 0, false, "");
      }

      public SpreadResult(
         long spreadNanos,
         String lateClient,
         int clientCount,
         LanSyncManager.ExecutionMethod executionMethod,
         long targetTick,
         int maxTickOffset,
         boolean perfectTickSync,
         String tickReport
      ) {
         this.spreadNanos = spreadNanos;
         this.lateClient = lateClient;
         this.clientCount = clientCount;
         this.executionMethod = executionMethod != null ? executionMethod : LanSyncManager.ExecutionMethod.INSTANT;
         this.targetTick = targetTick;
         this.maxTickOffset = maxTickOffset;
         this.perfectTickSync = perfectTickSync;
         this.tickReport = tickReport != null ? tickReport : "";
      }

      public double getSpreadMs() {
         return this.spreadNanos / 1000000.0;
      }
   }
}

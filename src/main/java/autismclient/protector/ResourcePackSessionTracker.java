package autismclient.protector;

import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.Map.Entry;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.function.BiConsumer;
import java.util.function.LongSupplier;
import java.util.function.Supplier;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.server.PackLoadFeedback;
import net.minecraft.client.resources.server.PackLoadFeedback.FinalResult;
import net.minecraft.client.resources.server.PackLoadFeedback.Update;
import net.minecraft.network.PacketListener;
import net.minecraft.network.protocol.common.ServerboundResourcePackPacket;
import net.minecraft.network.protocol.common.ServerboundResourcePackPacket.Action;

public final class ResourcePackSessionTracker {
   static final int init_I = 32;
   static final int load_I = 64;
   private static final Object LOCK = new Object();
   private static final Map<ConnectionPrivacyRegistry.ConnectionSession, ResourcePackSessionTracker.PackSession> sessionsByContext = new IdentityHashMap<>();
   private static final Map<Object, ResourcePackSessionTracker.PackSession> sessionsBySource = new IdentityHashMap<>();
   private static final Supplier<Executor> DEFAULT_GAME_THREAD_EXECUTOR = () -> {
      Minecraft minecraft = Minecraft.getInstance();
      return minecraft == null ? null : minecraft::execute;
   };
   private static final Executor WORKER = Executors.newSingleThreadExecutor(runnable -> {
      Thread thread = new Thread(runnable, "autism-pack-response-drain");
      thread.setDaemon(true);
      return thread;
   });
   static final Executor DEFAULT_DRAIN_EXECUTOR = command -> {
      Executor gameThread = ResourcePackSessionTracker.gameThreadExecutor.get();
      if (gameThread != null) {
         gameThread.execute(command);
      } else {
         WORKER.execute(command);
      }
   };
   private static long nextSessionId;
   private static boolean init_Z;
   private static boolean load_Z;
   private static boolean read_Z;
   private static boolean write;
   private static BiConsumer<ConnectionPrivacyRegistry.ConnectionSession, ServerboundResourcePackPacket> responseOutput;
   private static Executor drainExecutor = DEFAULT_DRAIN_EXECUTOR;
   private static Supplier<Executor> gameThreadExecutor = DEFAULT_GAME_THREAD_EXECUTOR;

   private ResourcePackSessionTracker() {
   }

   public static ResourcePackSessionTracker.PackSession init(ConnectionPrivacyRegistry.ConnectionSession context, Object source, Object manager, Runnable disconnectHandler) {
      if (context == null) {
         return null;
      }

      List<ResourcePackSessionTracker.PackSession> replaced = new ArrayList<>(2);
      ResourcePackSessionTracker.PackSession session;
      synchronized (LOCK) {
         Object resolvedManager = manager != null ? manager : context.serverPackManager();
         session = sessionsByContext.get(context);
         if (session != null && session.load_Z && isSameSession(session, source, resolvedManager)) {
            if (session.source == null && source != null) {
               ResourcePackSessionTracker.PackSession sourceOwner = sessionsBySource.get(source);
               if (sourceOwner != null && sourceOwner != session) {
                  replaced.add(sourceOwner);
               }

               session.source = source;
               sessionsBySource.put(source, session);
            }

            if (session.manager == null && resolvedManager != null) {
               session.manager = resolvedManager;
            }

            if (disconnectHandler != null) {
               session.disconnectHandler = disconnectHandler;
            }
         } else {
            if (session != null) {
               replaced.add(session);
            }

            if (source != null) {
               ResourcePackSessionTracker.PackSession sourceOwner = sessionsBySource.get(source);
               if (sourceOwner != null && sourceOwner != session) {
                  replaced.add(sourceOwner);
               }
            }

            session = new ResourcePackSessionTracker.PackSession(++nextSessionId, context, source, resolvedManager, disconnectHandler);
            sessionsByContext.put(context, session);
            if (source != null) {
               sessionsBySource.put(source, session);
            }
         }
      }

      closeReplaced(replaced);
      return session;
   }

   private static boolean isSameSession(ResourcePackSessionTracker.PackSession session, Object source, Object manager) {
      ConnectionPrivacyRegistry.ConnectionSession context = session.context;
      return (source == null || session.source == null || session.source == source)
         && (manager == null || session.manager == null || session.manager == manager)
         && session.lifecycleId == context.lifecycleId()
         && session.packetListener == context.packetListener()
         && Objects.equals(session.accountIdentity, context.accountIdentity())
         && Objects.equals(session.serverEndpoint, context.serverEndpoint())
         && session.downloadGeneration == context.downloadGeneration();
   }

   private static void closeReplaced(List<ResourcePackSessionTracker.PackSession> replaced) {
      for (ResourcePackSessionTracker.PackSession session : replaced) {
         load_V(session);
      }
   }

   public static ResourcePackSessionTracker.PackSession sessionFor(ConnectionPrivacyRegistry.ConnectionSession context) {
      if (context == null) {
         return null;
      }

      synchronized (LOCK) {
         ResourcePackSessionTracker.PackSession session = sessionsByContext.get(context);
         return session != null && session.load_Z ? session : null;
      }
   }

   public static ResourcePackSessionTracker.PackSession init(Object source) {
      if (source == null) {
         return null;
      }

      synchronized (LOCK) {
         ResourcePackSessionTracker.PackSession session = sessionsBySource.get(source);
         return session != null && session.load_Z ? session : null;
      }
   }

   public static boolean isActive(ResourcePackSessionTracker.PackSession session) {
      if (session != null && session.load_Z) {
         synchronized (LOCK) {
            ConnectionPrivacyRegistry.ConnectionSession context = session.context;
            return session.load_Z
               && sessionsByContext.get(context) == session
               && ConnectionPrivacyRegistry.isActive(context)
               && session.lifecycleId == context.lifecycleId()
               && session.packetListener == context.packetListener()
               && Objects.equals(session.accountIdentity, context.accountIdentity())
               && Objects.equals(session.serverEndpoint, context.serverEndpoint())
               && session.downloadGeneration == context.downloadGeneration()
               && session.manager == context.serverPackManager();
         }
      } else {
         return false;
      }
   }

   public static boolean isCurrent(ResourcePackSessionTracker.PackSession session, UUID packId, long generation) {
      if (isActive(session) && packId != null) {
         synchronized (LOCK) {
            ResourcePackSessionTracker.PackEntry push = session.packs.get(packId);
            return push != null && !push.pop && push.generation == generation;
         }
      } else {
         return false;
      }
   }

   public static boolean load_Z(ResourcePackSessionTracker.PackSession session) {
      if (session == null) {
         return false;
      }

      synchronized (LOCK) {
         return session.init_Z;
      }
   }

   static long revision(ResourcePackSessionTracker.PackSession session) {
      synchronized (LOCK) {
         return isActive(session) ? session.revision : -1L;
      }
   }

   public static long init(ConnectionPrivacyRegistry.ConnectionSession context, UUID packId, ResourcePackSessionTracker.PackPolicy policy) {
      if (context != null && packId != null && policy != null) {
         ResourcePackSessionTracker.PackSession session = sessionFor(context);
         if (session == null) {
            session = init(context, null, null, null);
         }

         boolean tracked = true;
         List<ResourcePackSessionTracker.PendingWait> waits;
         long generation;
         synchronized (LOCK) {
            ResourcePackSessionTracker.PackEntry previous = session.packs.get(packId);
            session.revision++;
            generation = ++session.generationCounter;
            if (policy != ResourcePackSessionTracker.PackPolicy.FULL) {
               session.init_Z = true;
            }

            ResourcePackSessionTracker.PackEntry push = new ResourcePackSessionTracker.PackEntry(packId, generation, policy);
            if (policy != ResourcePackSessionTracker.PackPolicy.FULL && !isPendingPack(previous) && pendingPackCount(session) >= 32) {
               push.pop = true;
               tracked = false;
            }

            session.packs.remove(packId);
            session.packs.put(packId, push);
            pruneTerminalPacks(session);
            waits = takeWaits(session, packId);
         }

         cancelWaits(waits);
         ServerPackSelectionRegistry.init_V(session, packId);
         IsolatedPackDownloader.init_V(session, packId);
         return tracked ? generation : -1L;
      } else {
         return -1L;
      }
   }

   private static boolean isPendingPack(ResourcePackSessionTracker.PackEntry push) {
      return push != null && push.policy != ResourcePackSessionTracker.PackPolicy.FULL && !push.pop && !push.push;
   }

   private static int pendingPackCount(ResourcePackSessionTracker.PackSession session) {
      int count = 0;

      for (ResourcePackSessionTracker.PackEntry push : session.packs.values()) {
         if (isPendingPack(push)) {
            count++;
         }
      }

      return count;
   }

   private static boolean load(ResourcePackSessionTracker.PackEntry push) {
      return push.policy == ResourcePackSessionTracker.PackPolicy.FULL || push.pop || push.push;
   }

   private static void pruneTerminalPacks(ResourcePackSessionTracker.PackSession session) {
      int terminal = 0;

      for (ResourcePackSessionTracker.PackEntry push : session.packs.values()) {
         if (load(push)) {
            terminal++;
         }
      }

      if (terminal > 64) {
         Iterator<Entry<UUID, ResourcePackSessionTracker.PackEntry>> iterator = session.packs.entrySet().iterator();

         while (iterator.hasNext() && terminal > 64) {
            if (load(iterator.next().getValue())) {
               iterator.remove();
               terminal--;
            }
         }
      }
   }

   public static ResourcePackSessionTracker.PackPolicy policyFor(ResourcePackSessionTracker.PackSession session, UUID packId) {
      if (session != null && packId != null) {
         synchronized (LOCK) {
            ResourcePackSessionTracker.PackEntry push = session.packs.get(packId);
            return push == null ? null : push.policy;
         }
      } else {
         return null;
      }
   }

   public static long generation(ResourcePackSessionTracker.PackSession session, UUID packId) {
      if (session != null && packId != null) {
         synchronized (LOCK) {
            ResourcePackSessionTracker.PackEntry push = session.packs.get(packId);
            return push == null ? -1L : push.generation;
         }
      } else {
         return -1L;
      }
   }

   public static boolean init_Z(ResourcePackSessionTracker.PackSession session, UUID packId) {
      if (session != null && packId != null) {
         synchronized (LOCK) {
            return session.init_Z;
         }
      } else {
         return false;
      }
   }

   public static long load(ResourcePackSessionTracker.PackSession session, UUID packId) {
      if (session != null && packId != null) {
         boolean progress = false;
         long generation;
         synchronized (LOCK) {
            ResourcePackSessionTracker.PackEntry push = session.packs.get(packId);
            if (push == null) {
               return session.init_Z ? -1L : 0L;
            }

            if (push.policy == ResourcePackSessionTracker.PackPolicy.FULL) {
               return 0L;
            }

            if (!isCurrent(session, packId, push.generation) || push.policy == ResourcePackSessionTracker.PackPolicy.DECLINE || push.discarded || push.push) {
               return -1L;
            }

            if (!push.load_Z) {
               push.load_Z = true;
               push.read_J++;
               progress = push.downloadFailed;
            }

            generation = push.generation;
         }

         if (progress) {
            write_V(session);
         }

         return generation;
      } else {
         return 0L;
      }
   }

   public static boolean load(ResourcePackSessionTracker.PackSession session, UUID packId, long generation) {
      return isCurrent(session, packId, generation);
   }

   public static boolean init(ResourcePackSessionTracker.PackSession session, UUID packId, Update update, PackLoadFeedback vanilla) {
      if (session != null && packId != null && update != null && vanilla != null) {
         boolean progress = false;
         synchronized (LOCK) {
            ResourcePackSessionTracker.PackEntry push = session.packs.get(packId);
            if (push == null || push.policy == ResourcePackSessionTracker.PackPolicy.FULL || !isCurrent(session, packId, push.generation)) {
               return false;
            }

            push.feedback = vanilla;
            if (!push.discarded && !push.push) {
               if (update == Update.ACCEPTED) {
                  if (!push.init_Z && push.policy != ResourcePackSessionTracker.PackPolicy.DECLINE) {
                     push.init_Z = true;
                     push.read_J++;
                     progress = true;
                  }
               } else if (update == Update.DOWNLOADED && !push.read_Z && !push.downloadFailed && push.init_Z && push.load_Z) {
                  push.read_Z = true;
                  push.read_J++;
                  progress = true;
               }
            }
         }

         if (progress) {
            write_V(session);
         }

         return true;
      } else {
         return false;
      }
   }

   public static boolean init(ResourcePackSessionTracker.PackSession session, UUID packId, FinalResult result, PackLoadFeedback vanilla) {
      if (session != null && packId != null && result != null && vanilla != null) {
         boolean progress = false;
         synchronized (LOCK) {
            ResourcePackSessionTracker.PackEntry push = session.packs.get(packId);
            if (push == null || push.policy == ResourcePackSessionTracker.PackPolicy.FULL || !isCurrent(session, packId, push.generation)) {
               return false;
            }

            push.feedback = vanilla;
            if (!push.discarded && !push.push) {
               switch (result) {
                  case APPLIED:
                     if (!push.applied && push.read_Z && !push.activationFailed && !push.downloadFailed) {
                        push.applied = true;
                        push.read_J++;
                        progress = true;
                     }
                     break;
                  case DOWNLOAD_FAILED:
                     if (!push.downloadFailed && !push.read_Z && !push.activationFailed) {
                        push.downloadFailed = true;
                        push.read_J++;
                        progress = true;
                     }
                     break;
                  case ACTIVATION_FAILED:
                     if (!push.activationFailed && !push.applied && !push.downloadFailed) {
                        push.activationFailed = true;
                        push.read_J++;
                        progress = true;
                     }
                     break;
                  case DECLINED:
                     if (!push.declined) {
                        push.declined = true;
                        push.read_J++;
                        progress = true;
                     }
                     break;
                  case DISCARDED:
                     if (!push.discarded) {
                        push.discarded = true;
                        push.load_J++;
                        push.read_J++;
                        progress = true;
                     }
               }
            }
         }

         IsolatedPackDownloader.init_V(session, packId);
         if (progress) {
            write_V(session);
         }

         return true;
      } else {
         return false;
      }
   }

   public static void init_V() {
      requestDrain(true);
   }

   public static void scheduleDrain() {
      requestDrain(false);
   }

   public static void read() {
      synchronized (LOCK) {
         if (!write) {
            return;
         }

         write = false;
      }

      init_V();
   }

   private static void write_V(ResourcePackSessionTracker.PackSession session) {
      requestDrain(!session.context.isMultiConnection());
   }

   private static void requestDrain(boolean includePrimary) {
      synchronized (LOCK) {
         if (init_Z) {
            load_Z = true;
            read_Z |= includePrimary;
            return;
         }

         init_Z = true;
      }

      dispatchDrain(includePrimary);
   }

   private static void dispatchDrain(boolean includePrimary) {
      try {
         if (drainExecutor == DEFAULT_DRAIN_EXECUTOR) {
            Executor gameThread = gameThreadExecutor.get();
            if (includePrimary) {
               if (gameThread == null) {
                  resetDrainFlags();
                  return;
               }

               gameThread.execute(() -> runDrain(true));
            } else if (gameThread != null) {
               gameThread.execute(() -> runDrain(false));
            } else {
               DEFAULT_DRAIN_EXECUTOR.execute(() -> runDrain(false));
            }
         } else {
            drainExecutor.execute(() -> runDrain(includePrimary));
         }
      } catch (Throwable rejected) {
         synchronized (LOCK) {
            init_Z = false;
            write = true;
         }
      }
   }

   private static void resetDrainFlags() {
      synchronized (LOCK) {
         init_Z = false;
         load_Z = false;
         read_Z = false;
      }
   }

   private static void runDrain(boolean includePrimary) {
      boolean followUp;
      boolean followUpPrimary;
      try {
         drainResponses(includePrimary);
      } finally {
         synchronized (LOCK) {
            init_Z = false;
            followUp = load_Z;
            followUpPrimary = read_Z;
            load_Z = false;
            read_Z = false;
         }
      }

      if (followUp) {
         requestDrain(followUpPrimary);
      }
   }

   private static void drainResponses(boolean includePrimary) {
      List<ResourcePackSessionTracker.ResponseEvent> sends = new ArrayList<>();
      synchronized (LOCK) {
         for (ResourcePackSessionTracker.PackSession session : sessionsByContext.values()) {
            if ((includePrimary || session.context.isMultiConnection()) && isActive(session)) {
               for (ResourcePackSessionTracker.PackEntry push : session.packs.values()) {
                  Action action = nextResponseAction(push);
                  if (action != null) {
                     push.pendingAction = action;
                     sends.add(
                        new ResourcePackSessionTracker.ResponseEvent(session, push.id, push.generation, action, push.feedback, push.load_J, push.read_J)
                     );
                  }
               }
            }
         }
      }

      for (ResourcePackSessionTracker.ResponseEvent initial : sends) {
         ResourcePackSessionTracker.ResponseEvent send = initial;

         while (send != null) {
            boolean sent = false;
            if (isResponseCurrent(send)) {
               try {
                  deliverResponse(send);
                  sent = true;
               } catch (Throwable ignored) {
                  synchronized (LOCK) {
                     write = true;
                  }
               }
            }

            send = init(send, sent);
         }
      }
   }

   private static ResourcePackSessionTracker.ResponseEvent init(ResourcePackSessionTracker.ResponseEvent send, boolean sent) {
      synchronized (LOCK) {
         ResourcePackSessionTracker.PackEntry push = send.session.packs.get(send.packId);
         if (push != null && push.generation == send.generation && push.pendingAction == send.action) {
            push.pendingAction = null;
            if (!sent) {
               return null;
            }

            recordSentAction(send.session, push, send.action);
            if (push.read_J != send.read) {
               return null;
            }

            Action next = nextResponseAction(push);
            if (next == null) {
               return null;
            }

            push.pendingAction = next;
            return new ResourcePackSessionTracker.ResponseEvent(
               send.session, push.id, push.generation, next, push.feedback, push.load_J, push.read_J
            );
         } else {
            return null;
         }
      }
   }

   private static void deliverResponse(ResourcePackSessionTracker.ResponseEvent send) {
      Objects.requireNonNull(send.feedback, "Missing vanilla resource-pack feedback");
      if (responseOutput != null) {
         responseOutput.accept(
            send.session.context, new ServerboundResourcePackPacket(send.packId, send.action)
         );
      }

      switch (send.action) {
         case ACCEPTED:
            send.feedback.reportUpdate(send.packId, Update.ACCEPTED);
            break;
         case DOWNLOADED:
            send.feedback.reportUpdate(send.packId, Update.DOWNLOADED);
            break;
         case DECLINED:
            send.feedback.reportFinalResult(send.packId, FinalResult.DECLINED);
            break;
         case DISCARDED:
            send.feedback.reportFinalResult(send.packId, FinalResult.DISCARDED);
            break;
         case FAILED_DOWNLOAD:
            send.feedback.reportFinalResult(send.packId, FinalResult.DOWNLOAD_FAILED);
            break;
         case SUCCESSFULLY_LOADED:
            send.feedback.reportFinalResult(send.packId, FinalResult.APPLIED);
            break;
         case FAILED_RELOAD:
            send.feedback.reportFinalResult(send.packId, FinalResult.ACTIVATION_FAILED);
            break;
         default:
            throw new IllegalStateException("Unexpected resource-pack response " + send.action);
      }
   }

   private static boolean isResponseCurrent(ResourcePackSessionTracker.ResponseEvent send) {
      synchronized (LOCK) {
         if (!isCurrent(send.session, send.packId, send.generation)) {
            return false;
         }

         ResourcePackSessionTracker.PackEntry push = send.session.packs.get(send.packId);
         if (push == null || push.policy == ResourcePackSessionTracker.PackPolicy.FULL || push.pendingAction != send.action) {
            return false;
         }

         if (push.discarded && send.action != Action.DISCARDED && send.load != push.load_J) {
            return false;
         }

         boolean var10000;
         switch (send.action) {
            case ACCEPTED:
               var10000 = !push.acceptedSent && !push.push;
               break;
            case DOWNLOADED:
               var10000 = push.acceptedSent && !push.downloadedSent && !push.push;
               break;
            case DECLINED:
            case FAILED_DOWNLOAD:
            case SUCCESSFULLY_LOADED:
            case FAILED_RELOAD:
               var10000 = !push.push;
               break;
            case DISCARDED:
               var10000 = push.discarded && !push.push;
               break;
            default:
               var10000 = false;
         }

         return var10000;
      }
   }

   private static Action nextResponseAction(ResourcePackSessionTracker.PackEntry push) {
      if (push.policy == ResourcePackSessionTracker.PackPolicy.FULL || push.pop || push.push || push.pendingAction != null) {
         return null;
      }

      if (push.declined) {
         return Action.DECLINED;
      }

      if (!push.acceptedSent) {
         if (push.init_Z) {
            return Action.ACCEPTED;
         } else {
            return push.discarded ? Action.DISCARDED : null;
         }
      } else {
         if (push.downloadFailed && push.load_Z) {
            return Action.FAILED_DOWNLOAD;
         }

         if (!push.downloadedSent) {
            if (push.read_Z) {
               return Action.DOWNLOADED;
            } else {
               return push.discarded ? Action.DISCARDED : null;
            }
         } else if (push.discarded) {
            return Action.DISCARDED;
         } else if (push.applied) {
            return Action.SUCCESSFULLY_LOADED;
         } else {
            return push.activationFailed ? Action.FAILED_RELOAD : null;
         }
      }
   }

   private static void recordSentAction(ResourcePackSessionTracker.PackSession session, ResourcePackSessionTracker.PackEntry push, Action action) {
      switch (action) {
         case ACCEPTED:
            push.acceptedSent = true;
            break;
         case DOWNLOADED:
            push.downloadedSent = true;
            break;
         case DECLINED:
         case DISCARDED:
         case FAILED_DOWNLOAD:
         case SUCCESSFULLY_LOADED:
         case FAILED_RELOAD:
            push.push = true;
      }

      pruneTerminalPacks(session);
   }

   public static void init(ResourcePackSessionTracker.PackSession session, UUID packId, long generation, CompletableFuture<?> future, Runnable restore) {
      if (session != null && packId != null && future != null && restore != null) {
         boolean current;
         synchronized (LOCK) {
            current = isCurrent(session, packId, generation);
            if (current) {
               session.waits.add(new ResourcePackSessionTracker.PendingWait(session, packId, generation, future, restore));
            }
         }

         if (!current) {
            future.cancel(false);
            restore.run();
         }
      }
   }

   public static void init_V(ResourcePackSessionTracker.PackSession session, UUID packId, long generation) {
      if (session != null && packId != null) {
         synchronized (LOCK) {
            session.waits.removeIf(wait -> wait.session == session && wait.packId.equals(packId) && wait.generation == generation);
         }
      }
   }

   public static void init_V(ResourcePackSessionTracker.PackSession session) {
      if (session != null) {
         List<ResourcePackSessionTracker.PendingWait> waits;
         synchronized (LOCK) {
            waits = takeWaits(session, null);
         }

         cancelWaits(waits);
      }
   }

   private static List<ResourcePackSessionTracker.PendingWait> takeWaits(ResourcePackSessionTracker.PackSession session, UUID packId) {
      List<ResourcePackSessionTracker.PendingWait> taken = new ArrayList<>();
      session.waits.removeIf(wait -> {
         if (packId != null && !packId.equals(wait.packId)) {
            return false;
         }

         taken.add(wait);
         return true;
      });
      return taken;
   }

   private static void cancelWaits(List<ResourcePackSessionTracker.PendingWait> waits) {
      for (ResourcePackSessionTracker.PendingWait wait : waits) {
         wait.future.cancel(false);

         try {
            wait.restore.run();
         } catch (Throwable var4) {
         }
      }
   }

   public static void init(ConnectionPrivacyRegistry.ConnectionSession context, UUID packId) {
      ResourcePackSessionTracker.PackSession session = sessionFor(context);
      if (session != null) {
         init_V(session, packId);
      }
   }

   public static void init_V(ResourcePackSessionTracker.PackSession session, UUID packId) {
      if (session != null) {
         List<ResourcePackSessionTracker.PendingWait> waits;
         synchronized (LOCK) {
            session.revision++;
            if (packId == null) {
               for (ResourcePackSessionTracker.PackEntry push : session.packs.values()) {
                  push.pop = true;
               }
            } else {
               ResourcePackSessionTracker.PackEntry push = session.packs.get(packId);
               if (push != null) {
                  push.pop = true;
               }
            }

            pruneTerminalPacks(session);
            waits = takeWaits(session, packId);
         }

         cancelWaits(waits);
         ServerPackSelectionRegistry.init_V(session, packId);
         IsolatedPackDownloader.init_V(session, packId);
      }
   }

   public static void init_V(ConnectionPrivacyRegistry.ConnectionSession context) {
      load_V(sessionFor(context));
   }

   public static void load_V(ResourcePackSessionTracker.PackSession session) {
      if (session != null) {
         Runnable handler;
         List<ResourcePackSessionTracker.PendingWait> waits;
         synchronized (LOCK) {
            if (!session.load_Z) {
               return;
            }

            session.load_Z = false;
            sessionsByContext.remove(session.context, session);
            if (session.source != null) {
               sessionsBySource.remove(session.source, session);
            }

            waits = takeWaits(session, null);
            handler = session.disconnectHandler;
         }

         cancelWaits(waits);
         ServerPackSelectionRegistry.forget(session);
         IsolatedPackDownloader.init_V(session, null);
         if (handler != null) {
            try {
               handler.run();
            } catch (Throwable var5) {
            }
         }
      }
   }

   public static void write() {
      List<ResourcePackSessionTracker.PackSession> sessions;
      synchronized (LOCK) {
         sessions = new ArrayList<>(sessionsByContext.values());
      }

      for (ResourcePackSessionTracker.PackSession session : sessions) {
         load_V(session);
      }
   }

   public static boolean anyPendingPack() {
      synchronized (LOCK) {
         for (ResourcePackSessionTracker.PackSession session : sessionsByContext.values()) {
            if (hasPendingPackLocked(session)) {
               return true;
            }
         }

         return false;
      }
   }

   public static boolean hasPendingPack(ResourcePackSessionTracker.PackSession session) {
      synchronized (LOCK) {
         return hasPendingPackLocked(session);
      }
   }

   private static boolean hasPendingPackLocked(ResourcePackSessionTracker.PackSession session) {
      if (!isActive(session)) {
         return false;
      }

      for (ResourcePackSessionTracker.PackEntry push : session.packs.values()) {
         if (isPendingPack(push)) {
            return true;
         }
      }

      return false;
   }

   static void configure(LongSupplier clock, LongSupplier random, BiConsumer<ConnectionPrivacyRegistry.ConnectionSession, ServerboundResourcePackPacket> output) {
      configure(clock, random, output, (task, delayMs) -> () -> {}, Runnable::run);
   }

   static void configure(
      LongSupplier clock,
      LongSupplier random,
      BiConsumer<ConnectionPrivacyRegistry.ConnectionSession, ServerboundResourcePackPacket> output,
      ResourcePackSessionTracker.DelayScheduler scheduler,
      Executor drains
   ) {
      configure(clock, random, output, scheduler, drains, DEFAULT_GAME_THREAD_EXECUTOR);
   }

   static void configure(
      LongSupplier clock,
      LongSupplier random,
      BiConsumer<ConnectionPrivacyRegistry.ConnectionSession, ServerboundResourcePackPacket> output,
      ResourcePackSessionTracker.DelayScheduler scheduler,
      Executor drains,
      Supplier<Executor> gameThread
   ) {
      synchronized (LOCK) {
         init_Z = false;
         load_Z = false;
         read_Z = false;
         write = false;
         responseOutput = output;
         drainExecutor = drains;
         gameThreadExecutor = gameThread;
      }
   }

   static void reset() {
      synchronized (LOCK) {
         init_Z = false;
         load_Z = false;
         read_Z = false;
         write = false;
         responseOutput = null;
         drainExecutor = DEFAULT_DRAIN_EXECUTOR;
         gameThreadExecutor = DEFAULT_GAME_THREAD_EXECUTOR;
      }
   }

   public static final class PackSession {
      private final long id;
      private final ConnectionPrivacyRegistry.ConnectionSession context;
      private final long lifecycleId;
      private final PacketListener packetListener;
      private final ConnectionPrivacyRegistry.AccountIdentity accountIdentity;
      private final ConnectionPrivacyRegistry.ServerEndpoint serverEndpoint;
      private final long downloadGeneration;
      private final Map<UUID, ResourcePackSessionTracker.PackEntry> packs = new LinkedHashMap<>();
      private final List<ResourcePackSessionTracker.PendingWait> waits = new ArrayList<>();
      private long generationCounter;
      private long revision;
      private Object source;
      private Object manager;
      private Runnable disconnectHandler;
      private boolean init_Z;
      private boolean load_Z = true;

      private PackSession(long id, ConnectionPrivacyRegistry.ConnectionSession context, Object source, Object manager, Runnable disconnectHandler) {
         this.id = id;
         this.context = context;
         this.lifecycleId = context.lifecycleId();
         this.packetListener = context.packetListener();
         this.accountIdentity = context.accountIdentity();
         this.serverEndpoint = context.serverEndpoint();
         this.downloadGeneration = context.downloadGeneration();
         this.source = source;
         this.manager = manager;
         this.disconnectHandler = disconnectHandler;
      }

      public long id() {
         return this.id;
      }

      public ConnectionPrivacyRegistry.ConnectionSession context() {
         return this.context;
      }

      public Object source() {
         return this.source;
      }

      public Object manager() {
         return this.manager;
      }
   }

   private record ResponseEvent(
      ResourcePackSessionTracker.PackSession session,
      UUID packId,
      long generation,
      Action action,
      PackLoadFeedback feedback,
      long load,
      long read
   ) {
   }

   private record PendingWait(
      ResourcePackSessionTracker.PackSession session, UUID packId, long generation, CompletableFuture<?> future, Runnable restore
   ) {
   }

   public enum PackPolicy {
      ISOLATED,
      FULL,
      DECLINE;
   }

   @FunctionalInterface
   interface DelayScheduler {
      ResourcePackSessionTracker.ScheduledTask schedule(Runnable var1, long var2);
   }

   private static final class PackEntry {
      private final UUID id;
      private final long generation;
      private final ResourcePackSessionTracker.PackPolicy policy;
      private PackLoadFeedback feedback;
      private boolean init_Z;
      private boolean load_Z;
      private boolean read_Z;
      private boolean applied;
      private boolean downloadFailed;
      private boolean activationFailed;
      private boolean declined;
      private boolean discarded;
      private long load_J;
      private long read_J;
      private boolean acceptedSent;
      private boolean downloadedSent;
      private boolean push;
      private boolean pop;
      private Action pendingAction;

      private PackEntry(UUID id, long generation, ResourcePackSessionTracker.PackPolicy policy) {
         this.id = id;
         this.generation = generation;
         this.policy = policy;
      }
   }

   @FunctionalInterface
   interface ScheduledTask {
      void cancel();
   }
}

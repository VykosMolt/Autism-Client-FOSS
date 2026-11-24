package autismclient.g;

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

public final class FileCache {
   static final int init_I = 32;
   static final int load_I = 64;
   private static final Object init_Obj = new Object();
   private static final Map<BufferHelper.Inner_StreamUtil, FileCache.Inner_BufferHelper> init_Map = new IdentityHashMap<>();
   private static final Map<Object, FileCache.Inner_BufferHelper> load_Map = new IdentityHashMap<>();
   private static final Supplier<Executor> init_Supplier = () -> {
      Minecraft minecraft = Minecraft.getInstance();
      return minecraft == null ? null : minecraft::execute;
   };
   private static final Executor load_Executor = Executors.newSingleThreadExecutor(runnable -> {
      Thread thread = new Thread(runnable, "autism-pack-response-drain");
      thread.setDaemon(true);
      return thread;
   });
   static final Executor init_Executor = command -> {
      Executor gameThread = FileCache.load_Supplier.get();
      if (gameThread != null) {
         gameThread.execute(command);
      } else {
         load_Executor.execute(command);
      }
   };
   private static long init_J;
   private static boolean init_Z;
   private static boolean load_Z;
   private static boolean read_Z;
   private static boolean write;
   private static BiConsumer<BufferHelper.Inner_StreamUtil, ServerboundResourcePackPacket> init_BiConsumer;
   private static Executor read_Executor = init_Executor;
   private static Supplier<Executor> load_Supplier = init_Supplier;

   private FileCache() {
   }

   public static FileCache.Inner_BufferHelper init(BufferHelper.Inner_StreamUtil context, Object source, Object manager, Runnable disconnectHandler) {
      if (context == null) {
         return null;
      }

      List<FileCache.Inner_BufferHelper> replaced = new ArrayList<>(2);
      FileCache.Inner_BufferHelper session;
      synchronized (init_Obj) {
         Object resolvedManager = manager != null ? manager : context.init_ServerPackManager();
         session = init_Map.get(context);
         if (session != null && session.load_Z && init(session, source, resolvedManager)) {
            if (session.init_Obj == null && source != null) {
               FileCache.Inner_BufferHelper sourceOwner = load_Map.get(source);
               if (sourceOwner != null && sourceOwner != session) {
                  replaced.add(sourceOwner);
               }

               session.init_Obj = source;
               load_Map.put(source, session);
            }

            if (session.load_Obj == null && resolvedManager != null) {
               session.load_Obj = resolvedManager;
            }

            if (disconnectHandler != null) {
               session.init_Runnable = disconnectHandler;
            }
         } else {
            if (session != null) {
               replaced.add(session);
            }

            if (source != null) {
               FileCache.Inner_BufferHelper sourceOwner = load_Map.get(source);
               if (sourceOwner != null && sourceOwner != session) {
                  replaced.add(sourceOwner);
               }
            }

            session = new FileCache.Inner_BufferHelper(++init_J, context, source, resolvedManager, disconnectHandler);
            init_Map.put(context, session);
            if (source != null) {
               load_Map.put(source, session);
            }
         }
      }

      init(replaced);
      return session;
   }

   private static boolean init(FileCache.Inner_BufferHelper session, Object source, Object manager) {
      BufferHelper.Inner_StreamUtil context = session.init_BufferHelper_Strea;
      return (source == null || session.init_Obj == null || session.init_Obj == source)
         && (manager == null || session.load_Obj == null || session.load_Obj == manager)
         && session.load_J == context.init_J()
         && session.init_PacketListener == context.init_PacketListener()
         && Objects.equals(session.init_BufferHelper_DataU, context.init_BufferHelper_DataU())
         && Objects.equals(session.init_BufferHelper_Buffe, context.init_BufferHelper_Buffe())
         && session.read == context.load_J();
   }

   private static void init(List<FileCache.Inner_BufferHelper> replaced) {
      for (FileCache.Inner_BufferHelper session : replaced) {
         load_V(session);
      }
   }

   public static FileCache.Inner_BufferHelper init_FileCache_BufferHe(BufferHelper.Inner_StreamUtil context) {
      if (context == null) {
         return null;
      }

      synchronized (init_Obj) {
         FileCache.Inner_BufferHelper session = init_Map.get(context);
         return session != null && session.load_Z ? session : null;
      }
   }

   public static FileCache.Inner_BufferHelper init(Object source) {
      if (source == null) {
         return null;
      }

      synchronized (init_Obj) {
         FileCache.Inner_BufferHelper session = load_Map.get(source);
         return session != null && session.load_Z ? session : null;
      }
   }

   public static boolean init_Z(FileCache.Inner_BufferHelper session) {
      if (session != null && session.load_Z) {
         synchronized (init_Obj) {
            BufferHelper.Inner_StreamUtil context = session.init_BufferHelper_Strea;
            return session.load_Z
               && init_Map.get(context) == session
               && BufferHelper.init_Z(context)
               && session.load_J == context.init_J()
               && session.init_PacketListener == context.init_PacketListener()
               && Objects.equals(session.init_BufferHelper_DataU, context.init_BufferHelper_DataU())
               && Objects.equals(session.init_BufferHelper_Buffe, context.init_BufferHelper_Buffe())
               && session.read == context.load_J()
               && session.load_Obj == context.init_ServerPackManager();
         }
      } else {
         return false;
      }
   }

   public static boolean init_Z(FileCache.Inner_BufferHelper session, UUID packId, long generation) {
      if (init_Z(session) && packId != null) {
         synchronized (init_Obj) {
            FileCache.Inner_StreamUtil push = session.init_Map.get(packId);
            return push != null && !push.pop && push.init_J == generation;
         }
      } else {
         return false;
      }
   }

   public static boolean load_Z(FileCache.Inner_BufferHelper session) {
      if (session == null) {
         return false;
      }

      synchronized (init_Obj) {
         return session.init_Z;
      }
   }

   static long init_J(FileCache.Inner_BufferHelper session) {
      synchronized (init_Obj) {
         return init_Z(session) ? session.update : -1L;
      }
   }

   public static long init(BufferHelper.Inner_StreamUtil context, UUID packId, FileCache.Inner_DataUtil policy) {
      if (context != null && packId != null && policy != null) {
         FileCache.Inner_BufferHelper session = init_FileCache_BufferHe(context);
         if (session == null) {
            session = init(context, null, null, null);
         }

         boolean tracked = true;
         List<FileCache.Inner_ConfigStore> waits;
         long generation;
         synchronized (init_Obj) {
            FileCache.Inner_StreamUtil previous = session.init_Map.get(packId);
            session.update++;
            generation = ++session.write;
            if (policy != FileCache.Inner_DataUtil.load) {
               session.init_Z = true;
            }

            FileCache.Inner_StreamUtil push = new FileCache.Inner_StreamUtil(packId, generation, policy);
            if (policy != FileCache.Inner_DataUtil.load && !init_Z(previous) && init_I(session) >= 32) {
               push.pop = true;
               tracked = false;
            }

            session.init_Map.remove(packId);
            session.init_Map.put(packId, push);
            read_V(session);
            waits = init_List(session, packId);
         }

         load(waits);
         CharEncoder.init_V(session, packId);
         EventBus.init_V(session, packId);
         return tracked ? generation : -1L;
      } else {
         return -1L;
      }
   }

   private static boolean init_Z(FileCache.Inner_StreamUtil push) {
      return push != null && push.init_FileCache_DataUtil != FileCache.Inner_DataUtil.load && !push.pop && !push.push;
   }

   private static int init_I(FileCache.Inner_BufferHelper session) {
      int count = 0;

      for (FileCache.Inner_StreamUtil push : session.init_Map.values()) {
         if (init_Z(push)) {
            count++;
         }
      }

      return count;
   }

   private static boolean load(FileCache.Inner_StreamUtil push) {
      return push.init_FileCache_DataUtil == FileCache.Inner_DataUtil.load || push.pop || push.push;
   }

   private static void read_V(FileCache.Inner_BufferHelper session) {
      int terminal = 0;

      for (FileCache.Inner_StreamUtil push : session.init_Map.values()) {
         if (load(push)) {
            terminal++;
         }
      }

      if (terminal > 64) {
         Iterator<Entry<UUID, FileCache.Inner_StreamUtil>> iterator = session.init_Map.entrySet().iterator();

         while (iterator.hasNext() && terminal > 64) {
            if (load(iterator.next().getValue())) {
               iterator.remove();
               terminal--;
            }
         }
      }
   }

   public static FileCache.Inner_DataUtil init_FileCache_DataUtil(FileCache.Inner_BufferHelper session, UUID packId) {
      if (session != null && packId != null) {
         synchronized (init_Obj) {
            FileCache.Inner_StreamUtil push = session.init_Map.get(packId);
            return push == null ? null : push.init_FileCache_DataUtil;
         }
      } else {
         return null;
      }
   }

   public static long init_J(FileCache.Inner_BufferHelper session, UUID packId) {
      if (session != null && packId != null) {
         synchronized (init_Obj) {
            FileCache.Inner_StreamUtil push = session.init_Map.get(packId);
            return push == null ? -1L : push.init_J;
         }
      } else {
         return -1L;
      }
   }

   public static boolean init_Z(FileCache.Inner_BufferHelper session, UUID packId) {
      if (session != null && packId != null) {
         synchronized (init_Obj) {
            return session.init_Z;
         }
      } else {
         return false;
      }
   }

   public static long load(FileCache.Inner_BufferHelper session, UUID packId) {
      if (session != null && packId != null) {
         boolean progress = false;
         long generation;
         synchronized (init_Obj) {
            FileCache.Inner_StreamUtil push = session.init_Map.get(packId);
            if (push == null) {
               return session.init_Z ? -1L : 0L;
            }

            if (push.init_FileCache_DataUtil == FileCache.Inner_DataUtil.load) {
               return 0L;
            }

            if (!init_Z(session, packId, push.init_J) || push.init_FileCache_DataUtil == FileCache.Inner_DataUtil.read || push.close || push.push) {
               return -1L;
            }

            if (!push.load_Z) {
               push.load_Z = true;
               push.read_J++;
               progress = push.update;
            }

            generation = push.init_J;
         }

         if (progress) {
            write_V(session);
         }

         return generation;
      } else {
         return 0L;
      }
   }

   public static boolean load(FileCache.Inner_BufferHelper session, UUID packId, long generation) {
      return init_Z(session, packId, generation);
   }

   public static boolean init(FileCache.Inner_BufferHelper session, UUID packId, Update update, PackLoadFeedback vanilla) {
      if (session != null && packId != null && update != null && vanilla != null) {
         boolean progress = false;
         synchronized (init_Obj) {
            FileCache.Inner_StreamUtil push = session.init_Map.get(packId);
            if (push == null || push.init_FileCache_DataUtil == FileCache.Inner_DataUtil.load || !init_Z(session, packId, push.init_J)) {
               return false;
            }

            push.init_PackLoadFeedback = vanilla;
            if (!push.close && !push.push) {
               if (update == Update.ACCEPTED) {
                  if (!push.init_Z && push.init_FileCache_DataUtil != FileCache.Inner_DataUtil.read) {
                     push.init_Z = true;
                     push.read_J++;
                     progress = true;
                  }
               } else if (update == Update.DOWNLOADED && !push.read_Z && !push.update && push.init_Z && push.load_Z) {
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

   public static boolean init(FileCache.Inner_BufferHelper session, UUID packId, FinalResult result, PackLoadFeedback vanilla) {
      if (session != null && packId != null && result != null && vanilla != null) {
         boolean progress = false;
         synchronized (init_Obj) {
            FileCache.Inner_StreamUtil push = session.init_Map.get(packId);
            if (push == null || push.init_FileCache_DataUtil == FileCache.Inner_DataUtil.load || !init_Z(session, packId, push.init_J)) {
               return false;
            }

            push.init_PackLoadFeedback = vanilla;
            if (!push.close && !push.push) {
               switch (result) {
                  case APPLIED:
                     if (!push.write && push.read_Z && !push.sync && !push.update) {
                        push.write = true;
                        push.read_J++;
                        progress = true;
                     }
                     break;
                  case DOWNLOAD_FAILED:
                     if (!push.update && !push.read_Z && !push.sync) {
                        push.update = true;
                        push.read_J++;
                        progress = true;
                     }
                     break;
                  case ACTIVATION_FAILED:
                     if (!push.sync && !push.write && !push.update) {
                        push.sync = true;
                        push.read_J++;
                        progress = true;
                     }
                     break;
                  case DECLINED:
                     if (!push.flush) {
                        push.flush = true;
                        push.read_J++;
                        progress = true;
                     }
                     break;
                  case DISCARDED:
                     if (!push.close) {
                        push.close = true;
                        push.load_J++;
                        push.read_J++;
                        progress = true;
                     }
               }
            }
         }

         EventBus.init_V(session, packId);
         if (progress) {
            write_V(session);
         }

         return true;
      } else {
         return false;
      }
   }

   public static void init_V() {
      init(true);
   }

   public static void load() {
      init(false);
   }

   public static void read() {
      synchronized (init_Obj) {
         if (!write) {
            return;
         }

         write = false;
      }

      init_V();
   }

   private static void write_V(FileCache.Inner_BufferHelper session) {
      init(!session.init_BufferHelper_Strea.init_Z());
   }

   private static void init(boolean includePrimary) {
      synchronized (init_Obj) {
         if (init_Z) {
            load_Z = true;
            read_Z |= includePrimary;
            return;
         }

         init_Z = true;
      }

      load(includePrimary);
   }

   private static void load(boolean includePrimary) {
      try {
         if (read_Executor == init_Executor) {
            Executor gameThread = load_Supplier.get();
            if (includePrimary) {
               if (gameThread == null) {
                  sync();
                  return;
               }

               gameThread.execute(() -> read(true));
            } else if (gameThread != null) {
               gameThread.execute(() -> read(false));
            } else {
               init_Executor.execute(() -> read(false));
            }
         } else {
            read_Executor.execute(() -> read(includePrimary));
         }
      } catch (Throwable rejected) {
         synchronized (init_Obj) {
            init_Z = false;
            write = true;
         }
      }
   }

   private static void sync() {
      synchronized (init_Obj) {
         init_Z = false;
         load_Z = false;
         read_Z = false;
      }
   }

   private static void read(boolean includePrimary) {
      boolean followUp;
      boolean followUpPrimary;
      try {
         write(includePrimary);
      } finally {
         synchronized (init_Obj) {
            init_Z = false;
            followUp = load_Z;
            followUpPrimary = read_Z;
            load_Z = false;
            read_Z = false;
         }
      }

      if (followUp) {
         init(followUpPrimary);
      }
   }

   private static void write(boolean includePrimary) {
      List<FileCache.Inner_CachePool> sends = new ArrayList<>();
      synchronized (init_Obj) {
         for (FileCache.Inner_BufferHelper session : init_Map.values()) {
            if ((includePrimary || session.init_BufferHelper_Strea.init_Z()) && init_Z(session)) {
               for (FileCache.Inner_StreamUtil push : session.init_Map.values()) {
                  Action action = init_ServerboundResourc(push);
                  if (action != null) {
                     push.init_ServerboundResourc = action;
                     sends.add(
                        new FileCache.Inner_CachePool(session, push.init_UUID, push.init_J, action, push.init_PackLoadFeedback, push.load_J, push.read_J)
                     );
                  }
               }
            }
         }
      }

      for (FileCache.Inner_CachePool initial : sends) {
         FileCache.Inner_CachePool send = initial;

         while (send != null) {
            boolean sent = false;
            if (init_Z(send)) {
               try {
                  init_V(send);
                  sent = true;
               } catch (Throwable ignored) {
                  synchronized (init_Obj) {
                     write = true;
                  }
               }
            }

            send = init(send, sent);
         }
      }
   }

   private static FileCache.Inner_CachePool init(FileCache.Inner_CachePool send, boolean sent) {
      synchronized (init_Obj) {
         FileCache.Inner_StreamUtil push = send.init_FileCache_BufferHe.init_Map.get(send.init_UUID);
         if (push != null && push.init_J == send.init_J && push.init_ServerboundResourc == send.init_ServerboundResourc) {
            push.init_ServerboundResourc = null;
            if (!sent) {
               return null;
            }

            init(send.init_FileCache_BufferHe, push, send.init_ServerboundResourc);
            if (push.read_J != send.read) {
               return null;
            }

            Action next = init_ServerboundResourc(push);
            if (next == null) {
               return null;
            }

            push.init_ServerboundResourc = next;
            return new FileCache.Inner_CachePool(
               send.init_FileCache_BufferHe, push.init_UUID, push.init_J, next, push.init_PackLoadFeedback, push.load_J, push.read_J
            );
         } else {
            return null;
         }
      }
   }

   private static void init_V(FileCache.Inner_CachePool send) {
      Objects.requireNonNull(send.init_PackLoadFeedback, "Missing vanilla resource-pack feedback");
      if (init_BiConsumer != null) {
         init_BiConsumer.accept(
            send.init_FileCache_BufferHe.init_BufferHelper_Strea, new ServerboundResourcePackPacket(send.init_UUID, send.init_ServerboundResourc)
         );
      }

      switch (send.init_ServerboundResourc) {
         case ACCEPTED:
            send.init_PackLoadFeedback.reportUpdate(send.init_UUID, Update.ACCEPTED);
            break;
         case DOWNLOADED:
            send.init_PackLoadFeedback.reportUpdate(send.init_UUID, Update.DOWNLOADED);
            break;
         case DECLINED:
            send.init_PackLoadFeedback.reportFinalResult(send.init_UUID, FinalResult.DECLINED);
            break;
         case DISCARDED:
            send.init_PackLoadFeedback.reportFinalResult(send.init_UUID, FinalResult.DISCARDED);
            break;
         case FAILED_DOWNLOAD:
            send.init_PackLoadFeedback.reportFinalResult(send.init_UUID, FinalResult.DOWNLOAD_FAILED);
            break;
         case SUCCESSFULLY_LOADED:
            send.init_PackLoadFeedback.reportFinalResult(send.init_UUID, FinalResult.APPLIED);
            break;
         case FAILED_RELOAD:
            send.init_PackLoadFeedback.reportFinalResult(send.init_UUID, FinalResult.ACTIVATION_FAILED);
            break;
         default:
            throw new IllegalStateException("Unexpected resource-pack response " + send.init_ServerboundResourc);
      }
   }

   private static boolean init_Z(FileCache.Inner_CachePool send) {
      synchronized (init_Obj) {
         if (!init_Z(send.init_FileCache_BufferHe, send.init_UUID, send.init_J)) {
            return false;
         }

         FileCache.Inner_StreamUtil push = send.init_FileCache_BufferHe.init_Map.get(send.init_UUID);
         if (push == null || push.init_FileCache_DataUtil == FileCache.Inner_DataUtil.load || push.init_ServerboundResourc != send.init_ServerboundResourc) {
            return false;
         }

         if (push.close && send.init_ServerboundResourc != Action.DISCARDED && send.load != push.load_J) {
            return false;
         }

         boolean var10000;
         switch (send.init_ServerboundResourc) {
            case ACCEPTED:
               var10000 = !push.open && !push.push;
               break;
            case DOWNLOADED:
               var10000 = push.open && !push.poll && !push.push;
               break;
            case DECLINED:
            case FAILED_DOWNLOAD:
            case SUCCESSFULLY_LOADED:
            case FAILED_RELOAD:
               var10000 = !push.push;
               break;
            case DISCARDED:
               var10000 = push.close && !push.push;
               break;
            default:
               var10000 = false;
         }

         return var10000;
      }
   }

   private static Action init_ServerboundResourc(FileCache.Inner_StreamUtil push) {
      if (push.init_FileCache_DataUtil == FileCache.Inner_DataUtil.load || push.pop || push.push || push.init_ServerboundResourc != null) {
         return null;
      }

      if (push.flush) {
         return Action.DECLINED;
      }

      if (!push.open) {
         if (push.init_Z) {
            return Action.ACCEPTED;
         } else {
            return push.close ? Action.DISCARDED : null;
         }
      } else {
         if (push.update && push.load_Z) {
            return Action.FAILED_DOWNLOAD;
         }

         if (!push.poll) {
            if (push.read_Z) {
               return Action.DOWNLOADED;
            } else {
               return push.close ? Action.DISCARDED : null;
            }
         } else if (push.close) {
            return Action.DISCARDED;
         } else if (push.write) {
            return Action.SUCCESSFULLY_LOADED;
         } else {
            return push.sync ? Action.FAILED_RELOAD : null;
         }
      }
   }

   private static void init(FileCache.Inner_BufferHelper session, FileCache.Inner_StreamUtil push, Action action) {
      switch (action) {
         case ACCEPTED:
            push.open = true;
            break;
         case DOWNLOADED:
            push.poll = true;
            break;
         case DECLINED:
         case DISCARDED:
         case FAILED_DOWNLOAD:
         case SUCCESSFULLY_LOADED:
         case FAILED_RELOAD:
            push.push = true;
      }

      read_V(session);
   }

   public static void init(FileCache.Inner_BufferHelper session, UUID packId, long generation, CompletableFuture<?> future, Runnable restore) {
      if (session != null && packId != null && future != null && restore != null) {
         boolean current;
         synchronized (init_Obj) {
            current = init_Z(session, packId, generation);
            if (current) {
               session.init_List.add(new FileCache.Inner_ConfigStore(session, packId, generation, future, restore));
            }
         }

         if (!current) {
            future.cancel(false);
            restore.run();
         }
      }
   }

   public static void init_V(FileCache.Inner_BufferHelper session, UUID packId, long generation) {
      if (session != null && packId != null) {
         synchronized (init_Obj) {
            session.init_List.removeIf(wait -> wait.init_FileCache_BufferHe == session && wait.init_UUID.equals(packId) && wait.init_J == generation);
         }
      }
   }

   public static void init_V(FileCache.Inner_BufferHelper session) {
      if (session != null) {
         List<FileCache.Inner_ConfigStore> waits;
         synchronized (init_Obj) {
            waits = init_List(session, null);
         }

         load(waits);
      }
   }

   private static List<FileCache.Inner_ConfigStore> init_List(FileCache.Inner_BufferHelper session, UUID packId) {
      List<FileCache.Inner_ConfigStore> taken = new ArrayList<>();
      session.init_List.removeIf(wait -> {
         if (packId != null && !packId.equals(wait.init_UUID)) {
            return false;
         }

         taken.add(wait);
         return true;
      });
      return taken;
   }

   private static void load(List<FileCache.Inner_ConfigStore> waits) {
      for (FileCache.Inner_ConfigStore wait : waits) {
         wait.init_CompletableFuture.cancel(false);

         try {
            wait.init_Runnable.run();
         } catch (Throwable var4) {
         }
      }
   }

   public static void init(BufferHelper.Inner_StreamUtil context, UUID packId) {
      FileCache.Inner_BufferHelper session = init_FileCache_BufferHe(context);
      if (session != null) {
         init_V(session, packId);
      }
   }

   public static void init_V(FileCache.Inner_BufferHelper session, UUID packId) {
      if (session != null) {
         List<FileCache.Inner_ConfigStore> waits;
         synchronized (init_Obj) {
            session.update++;
            if (packId == null) {
               for (FileCache.Inner_StreamUtil push : session.init_Map.values()) {
                  push.pop = true;
               }
            } else {
               FileCache.Inner_StreamUtil push = session.init_Map.get(packId);
               if (push != null) {
                  push.pop = true;
               }
            }

            read_V(session);
            waits = init_List(session, packId);
         }

         load(waits);
         CharEncoder.init_V(session, packId);
         EventBus.init_V(session, packId);
      }
   }

   public static void init_V(BufferHelper.Inner_StreamUtil context) {
      load_V(init_FileCache_BufferHe(context));
   }

   public static void load_V(FileCache.Inner_BufferHelper session) {
      if (session != null) {
         Runnable handler;
         List<FileCache.Inner_ConfigStore> waits;
         synchronized (init_Obj) {
            if (!session.load_Z) {
               return;
            }

            session.load_Z = false;
            init_Map.remove(session.init_BufferHelper_Strea, session);
            if (session.init_Obj != null) {
               load_Map.remove(session.init_Obj, session);
            }

            waits = init_List(session, null);
            handler = session.init_Runnable;
         }

         load(waits);
         CharEncoder.init(session);
         EventBus.init_V(session, null);
         if (handler != null) {
            try {
               handler.run();
            } catch (Throwable var5) {
            }
         }
      }
   }

   public static void write() {
      List<FileCache.Inner_BufferHelper> sessions;
      synchronized (init_Obj) {
         sessions = new ArrayList<>(init_Map.values());
      }

      for (FileCache.Inner_BufferHelper session : sessions) {
         load_V(session);
      }
   }

   public static boolean init_Z() {
      synchronized (init_Obj) {
         for (FileCache.Inner_BufferHelper session : init_Map.values()) {
            if (write_Z(session)) {
               return true;
            }
         }

         return false;
      }
   }

   public static boolean read_Z(FileCache.Inner_BufferHelper session) {
      synchronized (init_Obj) {
         return write_Z(session);
      }
   }

   private static boolean write_Z(FileCache.Inner_BufferHelper session) {
      if (!init_Z(session)) {
         return false;
      }

      for (FileCache.Inner_StreamUtil push : session.init_Map.values()) {
         if (init_Z(push)) {
            return true;
         }
      }

      return false;
   }

   static void init(LongSupplier clock, LongSupplier random, BiConsumer<BufferHelper.Inner_StreamUtil, ServerboundResourcePackPacket> output) {
      init(clock, random, output, (task, delayMs) -> () -> {}, Runnable::run);
   }

   static void init(
      LongSupplier clock,
      LongSupplier random,
      BiConsumer<BufferHelper.Inner_StreamUtil, ServerboundResourcePackPacket> output,
      FileCache.Inner_NetHelper scheduler,
      Executor drains
   ) {
      init(clock, random, output, scheduler, drains, init_Supplier);
   }

   static void init(
      LongSupplier clock,
      LongSupplier random,
      BiConsumer<BufferHelper.Inner_StreamUtil, ServerboundResourcePackPacket> output,
      FileCache.Inner_NetHelper scheduler,
      Executor drains,
      Supplier<Executor> gameThread
   ) {
      synchronized (init_Obj) {
         init_Z = false;
         load_Z = false;
         read_Z = false;
         write = false;
         init_BiConsumer = output;
         read_Executor = drains;
         load_Supplier = gameThread;
      }
   }

   static void update() {
      synchronized (init_Obj) {
         init_Z = false;
         load_Z = false;
         read_Z = false;
         write = false;
         init_BiConsumer = null;
         read_Executor = init_Executor;
         load_Supplier = init_Supplier;
      }
   }

   public static final class Inner_BufferHelper {
      private final long init_J;
      private final BufferHelper.Inner_StreamUtil init_BufferHelper_Strea;
      private final long load_J;
      private final PacketListener init_PacketListener;
      private final BufferHelper.Inner_DataUtil init_BufferHelper_DataU;
      private final BufferHelper.Inner_BufferHelper init_BufferHelper_Buffe;
      private final long read;
      private final Map<UUID, FileCache.Inner_StreamUtil> init_Map = new LinkedHashMap<>();
      private final List<FileCache.Inner_ConfigStore> init_List = new ArrayList<>();
      private long write;
      private long update;
      private Object init_Obj;
      private Object load_Obj;
      private Runnable init_Runnable;
      private boolean init_Z;
      private boolean load_Z = true;

      private Inner_BufferHelper(long id, BufferHelper.Inner_StreamUtil context, Object source, Object manager, Runnable disconnectHandler) {
         this.init_J = id;
         this.init_BufferHelper_Strea = context;
         this.load_J = context.init_J();
         this.init_PacketListener = context.init_PacketListener();
         this.init_BufferHelper_DataU = context.init_BufferHelper_DataU();
         this.init_BufferHelper_Buffe = context.init_BufferHelper_Buffe();
         this.read = context.load_J();
         this.init_Obj = source;
         this.load_Obj = manager;
         this.init_Runnable = disconnectHandler;
      }

      public long init_J() {
         return this.init_J;
      }

      public BufferHelper.Inner_StreamUtil init_BufferHelper_Strea() {
         return this.init_BufferHelper_Strea;
      }

      public Object init_Obj() {
         return this.init_Obj;
      }

      public Object load() {
         return this.load_Obj;
      }
   }

   private record Inner_CachePool(
      FileCache.Inner_BufferHelper init_FileCache_BufferHe,
      UUID init_UUID,
      long init_J,
      Action init_ServerboundResourc,
      PackLoadFeedback init_PackLoadFeedback,
      long load,
      long read
   ) {
   }

   private record Inner_ConfigStore(
      FileCache.Inner_BufferHelper init_FileCache_BufferHe, UUID init_UUID, long init_J, CompletableFuture<?> init_CompletableFuture, Runnable init_Runnable
   ) {
   }

   public enum Inner_DataUtil {
      init,
      load,
      read;
   }

   @FunctionalInterface
   interface Inner_NetHelper {
      FileCache.Inner_TaskQueue schedule(Runnable var1, long var2);
   }

   private static final class Inner_StreamUtil {
      private final UUID init_UUID;
      private final long init_J;
      private final FileCache.Inner_DataUtil init_FileCache_DataUtil;
      private PackLoadFeedback init_PackLoadFeedback;
      private boolean init_Z;
      private boolean load_Z;
      private boolean read_Z;
      private boolean write;
      private boolean update;
      private boolean sync;
      private boolean flush;
      private boolean close;
      private long load_J;
      private long read_J;
      private boolean open;
      private boolean poll;
      private boolean push;
      private boolean pop;
      private Action init_ServerboundResourc;

      private Inner_StreamUtil(UUID id, long generation, FileCache.Inner_DataUtil policy) {
         this.init_UUID = id;
         this.init_J = generation;
         this.init_FileCache_DataUtil = policy;
      }
   }

   @FunctionalInterface
   interface Inner_TaskQueue {
      void cancel();
   }
}

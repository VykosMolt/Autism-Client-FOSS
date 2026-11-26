package autismclient.g;

import java.util.IdentityHashMap;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;
import java.util.stream.Stream;
import net.minecraft.client.Minecraft;
import net.minecraft.network.PacketListener;
import net.minecraft.server.packs.PackResources;
import net.minecraft.server.packs.resources.ResourceManager;

public final class ArrayUtil {
   private static final AtomicReference<ArrayUtil.Inner_DataUtil> init = new AtomicReference<>();

   private ArrayUtil() {
   }

   public static void init(ArrayUtil.Inner_DataUtil callback) {
      ArrayUtil.Inner_DataUtil previous = init.getAndSet(callback);
      load(previous);
   }

   public static ArrayUtil.Inner_StreamUtil init(ResourceManager resourceManager) {
      try {
         HashUtil.init_V();
         Object policyIdentity = null;
         BufferHelper.Inner_CachePool mode = null;
         boolean stable = false;

         for (int attempt = 0; attempt < 4; attempt++) {
            policyIdentity = HashUtil.init_Obj();
            mode = init(BufferHelper.init(init_PacketListener()));
            if (policyIdentity == HashUtil.init_Obj()) {
               stable = true;
               break;
            }
         }

         return !stable
            ? init(new ArrayUtil.Inner_CachePool())
            : load(resourceManager, ArrayUtil::init_ResourceManager, ArrayUtil::init_PacketListener, policyIdentity, mode);
      } catch (Throwable ignored) {
         return init(new ArrayUtil.Inner_CachePool());
      }
   }

   static ArrayUtil.Inner_StreamUtil init(
      ResourceManager resourceManager,
      Supplier<ResourceManager> currentManager,
      Supplier<PacketListener> currentListener,
      Object policyIdentity,
      BufferHelper.Inner_CachePool mode
   ) {
      return load(resourceManager, currentManager, currentListener, policyIdentity, mode);
   }

   private static ArrayUtil.Inner_StreamUtil load(
      ResourceManager resourceManager,
      Supplier<ResourceManager> currentManager,
      Supplier<PacketListener> currentListener,
      Object policyIdentity,
      BufferHelper.Inner_CachePool mode
   ) {
      try {
         Objects.requireNonNull(resourceManager, "resourceManager");
         Objects.requireNonNull(currentManager, "currentManager");
         Objects.requireNonNull(currentListener, "currentListener");
         Objects.requireNonNull(policyIdentity, "policyIdentity");
         Objects.requireNonNull(mode, "mode");

         List<PackResources> selectedPacks;
         try (Stream<PackResources> stream = resourceManager.listPacks()) {
            selectedPacks = stream.toList();
         }

         IdentityHashMap<PackResources, String> packIds = new IdentityHashMap<>();

         for (PackResources pack : selectedPacks) {
            packIds.put(Objects.requireNonNull(pack, "selected pack"), Objects.requireNonNull(pack.packId(), "selected pack id"));
         }

         PacketListener listener = currentListener.get();
         BufferHelper.Inner_StreamUtil context = BufferHelper.init(listener);
         BufferHelper.Inner_CachePool effectiveMode = context == null ? mode : context.init_BufferHelper_Cache();
         FileCache.Inner_BufferHelper session = FileCache.init_FileCache_BufferHe(context);
         IdentityHashMap<PackResources, ArrayUtil.Inner_BufferHelper> serverBindings = new IdentityHashMap<>();

         for (PackResources pack : selectedPacks) {
            CharEncoder.Inner_StreamUtil provenance = CharEncoder.init_CharEncoder_Stream(session, pack);
            if (provenance != null) {
               serverBindings.put(
                  pack, new ArrayUtil.Inner_BufferHelper(context, context.init_J(), listener, context.init_BufferHelper_Buffe(), session, provenance)
               );
            }
         }

         ArrayUtil.Inner_CachePool scope = new ArrayUtil.Inner_CachePool(
            resourceManager, currentManager, currentListener, policyIdentity, effectiveMode, selectedPacks, packIds, serverBindings, listener, context
         );
         return init(scope);
      } catch (Throwable ignored) {
         return init(new ArrayUtil.Inner_CachePool());
      }
   }

   public static void init(ArrayUtil.Inner_StreamUtil token) {
      if (token != null) {
         token.init.load();
      }
   }

   public static boolean init(ResourceManager resourceManager, PackResources pack, String sourcePackId) {
      if (resourceManager != null && pack != null && sourcePackId != null) {
         ArrayUtil.Inner_DataUtil current = init.get();
         if (current == null) {
            return false;
         }

         try {
            return current.init(resourceManager, pack, sourcePackId) && init.get() == current;
         } catch (Throwable ignored) {
            if (current instanceof ArrayUtil.Inner_CachePool scope) {
               scope.load();
            }

            return false;
         }
      } else {
         return false;
      }
   }

   public static boolean init(ResourceManager resourceManager, PackResources pack, String sourcePackId, BufferHelper.Inner_StreamUtil origin) {
      ArrayUtil.Inner_DataUtil current = init.get();
      return origin != null && current instanceof ArrayUtil.Inner_CachePool scope && scope.init_BufferHelper_Strea == origin
         ? init(resourceManager, pack, sourcePackId) && init.get() == current
         : false;
   }

   private static ArrayUtil.Inner_StreamUtil init(ArrayUtil.Inner_CachePool scope) {
      ArrayUtil.Inner_DataUtil previous = init.getAndSet(scope);
      load(previous);
      return new ArrayUtil.Inner_StreamUtil(scope);
   }

   private static void load(ArrayUtil.Inner_DataUtil callback) {
      if (callback instanceof ArrayUtil.Inner_CachePool scope) {
         scope.init_Z = false;
      }
   }

   private static ResourceManager init_ResourceManager() {
      Minecraft minecraft = Minecraft.getInstance();
      return minecraft == null ? null : minecraft.getResourceManager();
   }

   private static PacketListener init_PacketListener() {
      Minecraft minecraft = Minecraft.getInstance();
      return minecraft == null ? null : minecraft.getConnection();
   }

   private static BufferHelper.Inner_CachePool init(BufferHelper.Inner_StreamUtil context) {
      if (context != null) {
         return context.init_BufferHelper_Cache();
      } else {
         return HashUtil.sync() ? BufferHelper.Inner_CachePool.VANILLA : BufferHelper.Inner_CachePool.MODDED;
      }
   }

   private record Inner_BufferHelper(
      BufferHelper.Inner_StreamUtil init_BufferHelper_Strea,
      long init_J,
      PacketListener init_PacketListener,
      BufferHelper.Inner_BufferHelper init_BufferHelper_Buffe,
      FileCache.Inner_BufferHelper init_FileCache_BufferHe,
      CharEncoder.Inner_StreamUtil init_CharEncoder_Stream
   ) {
   }

   private static final class Inner_CachePool implements ArrayUtil.Inner_DataUtil {
      private final ResourceManager init_ResourceManager;
      private final Supplier<ResourceManager> init_Supplier;
      private final Supplier<PacketListener> load_Supplier;
      private final Object init_Obj;
      private final BufferHelper.Inner_CachePool init_BufferHelper_Cache;
      private final List<PackResources> init_List;
      private final IdentityHashMap<PackResources, String> init_IdentityHashMap;
      private final IdentityHashMap<PackResources, ArrayUtil.Inner_BufferHelper> load_IdentityHashMap;
      private final PacketListener init_PacketListener;
      private final BufferHelper.Inner_StreamUtil init_BufferHelper_Strea;
      private volatile boolean init_Z;

      private Inner_CachePool(
         ResourceManager buildingManager,
         Supplier<ResourceManager> currentManager,
         Supplier<PacketListener> currentListener,
         Object policyIdentity,
         BufferHelper.Inner_CachePool mode,
         List<PackResources> selectedPacks,
         IdentityHashMap<PackResources, String> packIds,
         IdentityHashMap<PackResources, ArrayUtil.Inner_BufferHelper> serverBindings,
         PacketListener capturedListener,
         BufferHelper.Inner_StreamUtil capturedContext
      ) {
         this.init_ResourceManager = buildingManager;
         this.init_Supplier = currentManager;
         this.load_Supplier = currentListener;
         this.init_Obj = policyIdentity;
         this.init_BufferHelper_Cache = mode;
         this.init_List = selectedPacks;
         this.init_IdentityHashMap = packIds;
         this.load_IdentityHashMap = serverBindings;
         this.init_PacketListener = capturedListener;
         this.init_BufferHelper_Strea = capturedContext;
         this.init_Z = true;
      }

      private Inner_CachePool() {
         this.init_ResourceManager = null;
         this.init_Supplier = null;
         this.load_Supplier = null;
         this.init_Obj = null;
         this.init_BufferHelper_Cache = null;
         this.init_List = List.of();
         this.init_IdentityHashMap = new IdentityHashMap<>();
         this.load_IdentityHashMap = new IdentityHashMap<>();
         this.init_PacketListener = null;
         this.init_BufferHelper_Strea = null;
         this.init_Z = false;
      }

      @Override
      public boolean init(ResourceManager resourceManager, PackResources pack, String sourcePackId) {
         if (!this.init_Z) {
            return false;
         }

         try {
            if (resourceManager == this.init_ResourceManager && this.init_Supplier.get() == this.init_ResourceManager) {
               PacketListener listener = this.load_Supplier.get();
               if (listener != this.init_PacketListener) {
                  return this.load();
               }

               BufferHelper.Inner_StreamUtil context = BufferHelper.init(listener);
               if (context != this.init_BufferHelper_Strea) {
                  return this.load();
               }

               if (ArrayUtil.init(context) == this.init_BufferHelper_Cache && this.init_Obj == HashUtil.init_Obj()) {
                  ArrayUtil.Inner_BufferHelper server = this.load_IdentityHashMap.get(pack);
                  if (this.init_BufferHelper_Cache == BufferHelper.Inner_CachePool.VANILLA && server == null) {
                     return false;
                  } else if (!this.init()) {
                     return this.load();
                  } else {
                     String capturedId = this.init_IdentityHashMap.get(pack);
                     if (capturedId != null && capturedId.equals(sourcePackId)) {
                        return server == null ? false : this.init(server, pack) || this.load();
                     } else {
                        return this.load();
                     }
                  }
               } else {
                  return this.load();
               }
            } else {
               return this.load();
            }
         } catch (Throwable ignored) {
            return this.load();
         }
      }

      private boolean init() {
         List<PackResources> current;
         try (Stream<PackResources> stream = this.init_ResourceManager.listPacks()) {
            current = stream.toList();
         }

         if (current.size() != this.init_List.size()) {
            return false;
         }

         for (int index = 0; index < current.size(); index++) {
            PackResources selected = this.init_List.get(index);
            PackResources actual = current.get(index);
            if (actual != selected || !Objects.equals(this.init_IdentityHashMap.get(selected), actual.packId())) {
               return false;
            }
         }

         return true;
      }

      private boolean init(ArrayUtil.Inner_BufferHelper server, PackResources pack) {
         BufferHelper.Inner_StreamUtil context = server.init_BufferHelper_Strea();
         if (BufferHelper.init_Z(context)
            && context.init_J() == server.init_J()
            && context.init_PacketListener() == server.init_PacketListener()
            && Objects.equals(context.init_BufferHelper_Buffe(), server.init_BufferHelper_Buffe())) {
            FileCache.Inner_BufferHelper session = server.init_FileCache_BufferHe();
            if (FileCache.init_FileCache_BufferHe(context) == session && FileCache.init_Z(session)) {
               CharEncoder.Inner_StreamUtil captured = server.init_CharEncoder_Stream();
               return captured.equals(CharEncoder.init_CharEncoder_Stream(session, pack));
            } else {
               return false;
            }
         } else {
            return false;
         }
      }

      private boolean load() {
         this.init_Z = false;
         ArrayUtil.init.compareAndSet(this, null);
         return false;
      }
   }

   @FunctionalInterface
   public interface Inner_DataUtil {
      boolean init(ResourceManager var1, PackResources var2, String var3);
   }

   public static final class Inner_StreamUtil {
      private final ArrayUtil.Inner_CachePool init;

      private Inner_StreamUtil(ArrayUtil.Inner_CachePool scope) {
         this.init = scope;
      }
   }
}

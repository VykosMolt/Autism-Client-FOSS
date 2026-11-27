package autismclient.protector;

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

public final class PackTranslationPolicyResolver {
   private static final AtomicReference<PackTranslationPolicyResolver.Inner_DataUtil> init = new AtomicReference<>();

   private PackTranslationPolicyResolver() {
   }

   public static void init(PackTranslationPolicyResolver.Inner_DataUtil callback) {
      PackTranslationPolicyResolver.Inner_DataUtil previous = init.getAndSet(callback);
      load(previous);
   }

   public static PackTranslationPolicyResolver.Inner_StreamUtil init(ResourceManager resourceManager) {
      try {
         ProtectorFeatureFlags.refresh();
         Object policyIdentity = null;
         ConnectionPrivacyRegistry.PrivacyMode mode = null;
         boolean stable = false;

         for (int attempt = 0; attempt < 4; attempt++) {
            policyIdentity = ProtectorFeatureFlags.identity();
            mode = init(ConnectionPrivacyRegistry.getByListener(init_PacketListener()));
            if (policyIdentity == ProtectorFeatureFlags.identity()) {
               stable = true;
               break;
            }
         }

         return !stable
            ? init(new PackTranslationPolicyResolver.Inner_CachePool())
            : load(resourceManager, PackTranslationPolicyResolver::init_ResourceManager, PackTranslationPolicyResolver::init_PacketListener, policyIdentity, mode);
      } catch (Throwable ignored) {
         return init(new PackTranslationPolicyResolver.Inner_CachePool());
      }
   }

   static PackTranslationPolicyResolver.Inner_StreamUtil init(
      ResourceManager resourceManager,
      Supplier<ResourceManager> currentManager,
      Supplier<PacketListener> currentListener,
      Object policyIdentity,
      ConnectionPrivacyRegistry.PrivacyMode mode
   ) {
      return load(resourceManager, currentManager, currentListener, policyIdentity, mode);
   }

   private static PackTranslationPolicyResolver.Inner_StreamUtil load(
      ResourceManager resourceManager,
      Supplier<ResourceManager> currentManager,
      Supplier<PacketListener> currentListener,
      Object policyIdentity,
      ConnectionPrivacyRegistry.PrivacyMode mode
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
         ConnectionPrivacyRegistry.ConnectionSession context = ConnectionPrivacyRegistry.getByListener(listener);
         ConnectionPrivacyRegistry.PrivacyMode effectiveMode = context == null ? mode : context.privacyMode();
         ResourcePackSessionTracker.PackSession session = ResourcePackSessionTracker.sessionFor(context);
         IdentityHashMap<PackResources, PackTranslationPolicyResolver.Inner_BufferHelper> serverBindings = new IdentityHashMap<>();

         for (PackResources pack : selectedPacks) {
            ServerPackSelectionRegistry.Inner_StreamUtil provenance = ServerPackSelectionRegistry.provenanceOf(session, pack);
            if (provenance != null) {
               serverBindings.put(
                  pack, new PackTranslationPolicyResolver.Inner_BufferHelper(context, context.lifecycleId(), listener, context.serverEndpoint(), session, provenance)
               );
            }
         }

         PackTranslationPolicyResolver.Inner_CachePool scope = new PackTranslationPolicyResolver.Inner_CachePool(
            resourceManager, currentManager, currentListener, policyIdentity, effectiveMode, selectedPacks, packIds, serverBindings, listener, context
         );
         return init(scope);
      } catch (Throwable ignored) {
         return init(new PackTranslationPolicyResolver.Inner_CachePool());
      }
   }

   public static void init(PackTranslationPolicyResolver.Inner_StreamUtil token) {
      if (token != null) {
         token.init.load();
      }
   }

   public static boolean init(ResourceManager resourceManager, PackResources pack, String sourcePackId) {
      if (resourceManager != null && pack != null && sourcePackId != null) {
         PackTranslationPolicyResolver.Inner_DataUtil current = init.get();
         if (current == null) {
            return false;
         }

         try {
            return current.init(resourceManager, pack, sourcePackId) && init.get() == current;
         } catch (Throwable ignored) {
            if (current instanceof PackTranslationPolicyResolver.Inner_CachePool scope) {
               scope.load();
            }

            return false;
         }
      } else {
         return false;
      }
   }

   public static boolean init(ResourceManager resourceManager, PackResources pack, String sourcePackId, ConnectionPrivacyRegistry.ConnectionSession origin) {
      PackTranslationPolicyResolver.Inner_DataUtil current = init.get();
      return origin != null && current instanceof PackTranslationPolicyResolver.Inner_CachePool scope && scope.init_BufferHelper_Strea == origin
         ? init(resourceManager, pack, sourcePackId) && init.get() == current
         : false;
   }

   private static PackTranslationPolicyResolver.Inner_StreamUtil init(PackTranslationPolicyResolver.Inner_CachePool scope) {
      PackTranslationPolicyResolver.Inner_DataUtil previous = init.getAndSet(scope);
      load(previous);
      return new PackTranslationPolicyResolver.Inner_StreamUtil(scope);
   }

   private static void load(PackTranslationPolicyResolver.Inner_DataUtil callback) {
      if (callback instanceof PackTranslationPolicyResolver.Inner_CachePool scope) {
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

   private static ConnectionPrivacyRegistry.PrivacyMode init(ConnectionPrivacyRegistry.ConnectionSession context) {
      if (context != null) {
         return context.privacyMode();
      } else {
         return ProtectorFeatureFlags.sync() ? ConnectionPrivacyRegistry.PrivacyMode.VANILLA : ConnectionPrivacyRegistry.PrivacyMode.MODDED;
      }
   }

   private record Inner_BufferHelper(
      ConnectionPrivacyRegistry.ConnectionSession init_BufferHelper_Strea,
      long init_J,
      PacketListener init_PacketListener,
      ConnectionPrivacyRegistry.ServerEndpoint init_BufferHelper_Buffe,
      ResourcePackSessionTracker.PackSession init_FileCache_BufferHe,
      ServerPackSelectionRegistry.Inner_StreamUtil init_CharEncoder_Stream
   ) {
   }

   private static final class Inner_CachePool implements PackTranslationPolicyResolver.Inner_DataUtil {
      private final ResourceManager init_ResourceManager;
      private final Supplier<ResourceManager> init_Supplier;
      private final Supplier<PacketListener> load_Supplier;
      private final Object init_Obj;
      private final ConnectionPrivacyRegistry.PrivacyMode init_BufferHelper_Cache;
      private final List<PackResources> init_List;
      private final IdentityHashMap<PackResources, String> init_IdentityHashMap;
      private final IdentityHashMap<PackResources, PackTranslationPolicyResolver.Inner_BufferHelper> load_IdentityHashMap;
      private final PacketListener init_PacketListener;
      private final ConnectionPrivacyRegistry.ConnectionSession init_BufferHelper_Strea;
      private volatile boolean init_Z;

      private Inner_CachePool(
         ResourceManager buildingManager,
         Supplier<ResourceManager> currentManager,
         Supplier<PacketListener> currentListener,
         Object policyIdentity,
         ConnectionPrivacyRegistry.PrivacyMode mode,
         List<PackResources> selectedPacks,
         IdentityHashMap<PackResources, String> packIds,
         IdentityHashMap<PackResources, PackTranslationPolicyResolver.Inner_BufferHelper> serverBindings,
         PacketListener capturedListener,
         ConnectionPrivacyRegistry.ConnectionSession capturedContext
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

               ConnectionPrivacyRegistry.ConnectionSession context = ConnectionPrivacyRegistry.getByListener(listener);
               if (context != this.init_BufferHelper_Strea) {
                  return this.load();
               }

               if (PackTranslationPolicyResolver.init(context) == this.init_BufferHelper_Cache && this.init_Obj == ProtectorFeatureFlags.identity()) {
                  PackTranslationPolicyResolver.Inner_BufferHelper server = this.load_IdentityHashMap.get(pack);
                  if (this.init_BufferHelper_Cache == ConnectionPrivacyRegistry.PrivacyMode.VANILLA && server == null) {
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

      private boolean init(PackTranslationPolicyResolver.Inner_BufferHelper server, PackResources pack) {
         ConnectionPrivacyRegistry.ConnectionSession context = server.init_BufferHelper_Strea();
         if (ConnectionPrivacyRegistry.isActive(context)
            && context.lifecycleId() == server.init_J()
            && context.packetListener() == server.init_PacketListener()
            && Objects.equals(context.serverEndpoint(), server.init_BufferHelper_Buffe())) {
            ResourcePackSessionTracker.PackSession session = server.init_FileCache_BufferHe();
            if (ResourcePackSessionTracker.sessionFor(context) == session && ResourcePackSessionTracker.isActive(session)) {
               ServerPackSelectionRegistry.Inner_StreamUtil captured = server.init_CharEncoder_Stream();
               return captured.equals(ServerPackSelectionRegistry.provenanceOf(session, pack));
            } else {
               return false;
            }
         } else {
            return false;
         }
      }

      private boolean load() {
         this.init_Z = false;
         PackTranslationPolicyResolver.init.compareAndSet(this, null);
         return false;
      }
   }

   @FunctionalInterface
   public interface Inner_DataUtil {
      boolean init(ResourceManager var1, PackResources var2, String var3);
   }

   public static final class Inner_StreamUtil {
      private final PackTranslationPolicyResolver.Inner_CachePool init;

      private Inner_StreamUtil(PackTranslationPolicyResolver.Inner_CachePool scope) {
         this.init = scope;
      }
   }
}

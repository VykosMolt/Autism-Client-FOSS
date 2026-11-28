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
   private static final AtomicReference<PackTranslationPolicyResolver.TranslationPolicy> init = new AtomicReference<>();

   private PackTranslationPolicyResolver() {
   }

   public static void init(PackTranslationPolicyResolver.TranslationPolicy callback) {
      PackTranslationPolicyResolver.TranslationPolicy previous = init.getAndSet(callback);
      load(previous);
   }

   public static PackTranslationPolicyResolver.ScopeHandle init(ResourceManager resourceManager) {
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
            ? init(new PackTranslationPolicyResolver.PolicyScope())
            : load(resourceManager, PackTranslationPolicyResolver::init_ResourceManager, PackTranslationPolicyResolver::init_PacketListener, policyIdentity, mode);
      } catch (Throwable ignored) {
         return init(new PackTranslationPolicyResolver.PolicyScope());
      }
   }

   static PackTranslationPolicyResolver.ScopeHandle init(
      ResourceManager resourceManager,
      Supplier<ResourceManager> currentManager,
      Supplier<PacketListener> currentListener,
      Object policyIdentity,
      ConnectionPrivacyRegistry.PrivacyMode mode
   ) {
      return load(resourceManager, currentManager, currentListener, policyIdentity, mode);
   }

   private static PackTranslationPolicyResolver.ScopeHandle load(
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
         IdentityHashMap<PackResources, PackTranslationPolicyResolver.ServerBinding> serverBindings = new IdentityHashMap<>();

         for (PackResources pack : selectedPacks) {
            ServerPackSelectionRegistry.Inner_StreamUtil provenance = ServerPackSelectionRegistry.provenanceOf(session, pack);
            if (provenance != null) {
               serverBindings.put(
                  pack, new PackTranslationPolicyResolver.ServerBinding(context, context.lifecycleId(), listener, context.serverEndpoint(), session, provenance)
               );
            }
         }

         PackTranslationPolicyResolver.PolicyScope scope = new PackTranslationPolicyResolver.PolicyScope(
            resourceManager, currentManager, currentListener, policyIdentity, effectiveMode, selectedPacks, packIds, serverBindings, listener, context
         );
         return init(scope);
      } catch (Throwable ignored) {
         return init(new PackTranslationPolicyResolver.PolicyScope());
      }
   }

   public static void init(PackTranslationPolicyResolver.ScopeHandle token) {
      if (token != null) {
         token.init.load();
      }
   }

   public static boolean init(ResourceManager resourceManager, PackResources pack, String sourcePackId) {
      if (resourceManager != null && pack != null && sourcePackId != null) {
         PackTranslationPolicyResolver.TranslationPolicy current = init.get();
         if (current == null) {
            return false;
         }

         try {
            return current.init(resourceManager, pack, sourcePackId) && init.get() == current;
         } catch (Throwable ignored) {
            if (current instanceof PackTranslationPolicyResolver.PolicyScope scope) {
               scope.load();
            }

            return false;
         }
      } else {
         return false;
      }
   }

   public static boolean init(ResourceManager resourceManager, PackResources pack, String sourcePackId, ConnectionPrivacyRegistry.ConnectionSession origin) {
      PackTranslationPolicyResolver.TranslationPolicy current = init.get();
      return origin != null && current instanceof PackTranslationPolicyResolver.PolicyScope scope && scope.capturedContext == origin
         ? init(resourceManager, pack, sourcePackId) && init.get() == current
         : false;
   }

   private static PackTranslationPolicyResolver.ScopeHandle init(PackTranslationPolicyResolver.PolicyScope scope) {
      PackTranslationPolicyResolver.TranslationPolicy previous = init.getAndSet(scope);
      load(previous);
      return new PackTranslationPolicyResolver.ScopeHandle(scope);
   }

   private static void load(PackTranslationPolicyResolver.TranslationPolicy callback) {
      if (callback instanceof PackTranslationPolicyResolver.PolicyScope scope) {
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

   private record ServerBinding(
      ConnectionPrivacyRegistry.ConnectionSession init_BufferHelper_Strea,
      long init_J,
      PacketListener init_PacketListener,
      ConnectionPrivacyRegistry.ServerEndpoint init_BufferHelper_Buffe,
      ResourcePackSessionTracker.PackSession init_FileCache_BufferHe,
      ServerPackSelectionRegistry.Inner_StreamUtil provenance
   ) {
   }

   private static final class PolicyScope implements PackTranslationPolicyResolver.TranslationPolicy {
      private final ResourceManager buildingManager;
      private final Supplier<ResourceManager> currentManager;
      private final Supplier<PacketListener> currentListener;
      private final Object policyIdentity;
      private final ConnectionPrivacyRegistry.PrivacyMode mode;
      private final List<PackResources> selectedPacks;
      private final IdentityHashMap<PackResources, String> packIds;
      private final IdentityHashMap<PackResources, PackTranslationPolicyResolver.ServerBinding> serverBindings;
      private final PacketListener capturedListener;
      private final ConnectionPrivacyRegistry.ConnectionSession capturedContext;
      private volatile boolean init_Z;

      private PolicyScope(
         ResourceManager buildingManager,
         Supplier<ResourceManager> currentManager,
         Supplier<PacketListener> currentListener,
         Object policyIdentity,
         ConnectionPrivacyRegistry.PrivacyMode mode,
         List<PackResources> selectedPacks,
         IdentityHashMap<PackResources, String> packIds,
         IdentityHashMap<PackResources, PackTranslationPolicyResolver.ServerBinding> serverBindings,
         PacketListener capturedListener,
         ConnectionPrivacyRegistry.ConnectionSession capturedContext
      ) {
         this.buildingManager = buildingManager;
         this.currentManager = currentManager;
         this.currentListener = currentListener;
         this.policyIdentity = policyIdentity;
         this.mode = mode;
         this.selectedPacks = selectedPacks;
         this.packIds = packIds;
         this.serverBindings = serverBindings;
         this.capturedListener = capturedListener;
         this.capturedContext = capturedContext;
         this.init_Z = true;
      }

      private PolicyScope() {
         this.buildingManager = null;
         this.currentManager = null;
         this.currentListener = null;
         this.policyIdentity = null;
         this.mode = null;
         this.selectedPacks = List.of();
         this.packIds = new IdentityHashMap<>();
         this.serverBindings = new IdentityHashMap<>();
         this.capturedListener = null;
         this.capturedContext = null;
         this.init_Z = false;
      }

      @Override
      public boolean init(ResourceManager resourceManager, PackResources pack, String sourcePackId) {
         if (!this.init_Z) {
            return false;
         }

         try {
            if (resourceManager == this.buildingManager && this.currentManager.get() == this.buildingManager) {
               PacketListener listener = this.currentListener.get();
               if (listener != this.capturedListener) {
                  return this.load();
               }

               ConnectionPrivacyRegistry.ConnectionSession context = ConnectionPrivacyRegistry.getByListener(listener);
               if (context != this.capturedContext) {
                  return this.load();
               }

               if (PackTranslationPolicyResolver.init(context) == this.mode && this.policyIdentity == ProtectorFeatureFlags.identity()) {
                  PackTranslationPolicyResolver.ServerBinding server = this.serverBindings.get(pack);
                  if (this.mode == ConnectionPrivacyRegistry.PrivacyMode.VANILLA && server == null) {
                     return false;
                  } else if (!this.init()) {
                     return this.load();
                  } else {
                     String capturedId = this.packIds.get(pack);
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
         try (Stream<PackResources> stream = this.buildingManager.listPacks()) {
            current = stream.toList();
         }

         if (current.size() != this.selectedPacks.size()) {
            return false;
         }

         for (int index = 0; index < current.size(); index++) {
            PackResources selected = this.selectedPacks.get(index);
            PackResources actual = current.get(index);
            if (actual != selected || !Objects.equals(this.packIds.get(selected), actual.packId())) {
               return false;
            }
         }

         return true;
      }

      private boolean init(PackTranslationPolicyResolver.ServerBinding server, PackResources pack) {
         ConnectionPrivacyRegistry.ConnectionSession context = server.init_BufferHelper_Strea();
         if (ConnectionPrivacyRegistry.isActive(context)
            && context.lifecycleId() == server.init_J()
            && context.packetListener() == server.init_PacketListener()
            && Objects.equals(context.serverEndpoint(), server.init_BufferHelper_Buffe())) {
            ResourcePackSessionTracker.PackSession session = server.init_FileCache_BufferHe();
            if (ResourcePackSessionTracker.sessionFor(context) == session && ResourcePackSessionTracker.isActive(session)) {
               ServerPackSelectionRegistry.Inner_StreamUtil captured = server.provenance();
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
   public interface TranslationPolicy {
      boolean init(ResourceManager var1, PackResources var2, String var3);
   }

   public static final class ScopeHandle {
      private final PackTranslationPolicyResolver.PolicyScope init;

      private ScopeHandle(PackTranslationPolicyResolver.PolicyScope scope) {
         this.init = scope;
      }
   }
}

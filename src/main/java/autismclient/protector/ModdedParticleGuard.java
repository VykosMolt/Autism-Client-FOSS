package autismclient.protector;

import com.mojang.serialization.MapCodec;
import java.util.Arrays;
import java.util.function.Supplier;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.VarInt;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.game.ClientboundLevelParticlesPacket;

public final class ModdedParticleGuard {
   private static final ThreadLocal<int[]> init = ThreadLocal.withInitial(() -> new int[2]);

   private ModdedParticleGuard() {
   }

   public static void push(boolean modded) {
      int[] scope = init.get();
      int depth = scope[0] + 1;
      if (depth >= scope.length) {
         scope = Arrays.copyOf(scope, Math.max(depth + 1, scope.length * 2));
         init.set(scope);
      }

      scope[0] = depth;
      scope[depth] = modded ? 1 : 0;
   }

   public static void pop() {
      int[] scope = init.get();
      int depth = scope[0];
      if (depth > 0) {
         scope[depth] = 0;
         scope[0] = depth - 1;
      }
   }

   public static ParticleOptions init(RegistryFriendlyByteBuf input, Supplier<ParticleOptions> originalDecode) {
      int startIndex = input.readerIndex();

      try {
         return originalDecode.get();
      } catch (IllegalArgumentException failure) {
         int[] scope = init.get();
         int depth = scope[0];
         if (depth > 0 && scope[depth] != 0) {
            int id;
            try {
               id = VarInt.read(input.duplicate().readerIndex(startIndex));
            } catch (RuntimeException inspectionFailure) {
               throw failure;
            }

            if (!("No value with id " + id).equals(failure.getMessage())) {
               throw failure;
            } else if (input.registryAccess().lookupOrThrow(Registries.PARTICLE_TYPE).byId(id) != null) {
               throw failure;
            } else {
               return ModdedParticleGuard.PlaceholderParticle.INSTANCE;
            }
         } else {
            throw failure;
         }
      }
   }

   public static boolean init(ClientboundLevelParticlesPacket packet) {
      return packet != null && packet.particle() == ModdedParticleGuard.PlaceholderParticle.INSTANCE;
   }

   public static boolean init(ParticleProvider<?> provider, ConnectionPrivacyRegistry.ConnectionSession context) {
      return provider != null
         && context != null
         && context.privacyMode() == ConnectionPrivacyRegistry.PrivacyMode.VANILLA
         && !provider.getClass().getName().startsWith("net.minecraft.client.particle.");
   }

   public static <T> T init(ParticleProvider<?> provider, ConnectionPrivacyRegistry.ConnectionSession context, Supplier<T> invocation) {
      return init(provider, context) ? null : invocation.get();
   }

   private static final class PlaceholderParticle implements ParticleOptions {
      private static final ModdedParticleGuard.PlaceholderParticle INSTANCE = new ModdedParticleGuard.PlaceholderParticle();
      private static final ParticleType<ModdedParticleGuard.PlaceholderParticle> TYPE = new ParticleType<ModdedParticleGuard.PlaceholderParticle>(false) {
         @Override
         public MapCodec<ModdedParticleGuard.PlaceholderParticle> codec() {
            return MapCodec.unit(ModdedParticleGuard.PlaceholderParticle.INSTANCE);
         }

         @Override
         public StreamCodec<? super RegistryFriendlyByteBuf, ModdedParticleGuard.PlaceholderParticle> streamCodec() {
            return StreamCodec.unit(ModdedParticleGuard.PlaceholderParticle.INSTANCE);
         }
      };

      @Override
      public ParticleType<?> getType() {
         return TYPE;
      }
   }
}

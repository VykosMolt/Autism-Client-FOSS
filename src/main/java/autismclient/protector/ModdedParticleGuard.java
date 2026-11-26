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

   public static void init(boolean modded) {
      int[] scope = init.get();
      int depth = scope[0] + 1;
      if (depth >= scope.length) {
         scope = Arrays.copyOf(scope, Math.max(depth + 1, scope.length * 2));
         init.set(scope);
      }

      scope[0] = depth;
      scope[depth] = modded ? 1 : 0;
   }

   public static void init() {
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
               return ModdedParticleGuard.Inner_DataUtil.init_ByteReader_DataUti;
            }
         } else {
            throw failure;
         }
      }
   }

   public static boolean init(ClientboundLevelParticlesPacket packet) {
      return packet != null && packet.particle() == ModdedParticleGuard.Inner_DataUtil.init_ByteReader_DataUti;
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

   private static final class Inner_DataUtil implements ParticleOptions {
      private static final ModdedParticleGuard.Inner_DataUtil init_ByteReader_DataUti = new ModdedParticleGuard.Inner_DataUtil();
      private static final ParticleType<ModdedParticleGuard.Inner_DataUtil> init_ParticleType = new ParticleType<ModdedParticleGuard.Inner_DataUtil>(false) {
         @Override
         public MapCodec<ModdedParticleGuard.Inner_DataUtil> codec() {
            return MapCodec.unit(ModdedParticleGuard.Inner_DataUtil.init_ByteReader_DataUti);
         }

         @Override
         public StreamCodec<? super RegistryFriendlyByteBuf, ModdedParticleGuard.Inner_DataUtil> streamCodec() {
            return StreamCodec.unit(ModdedParticleGuard.Inner_DataUtil.init_ByteReader_DataUti);
         }
      };

      @Override
      public ParticleType<?> getType() {
         return init_ParticleType;
      }
   }
}

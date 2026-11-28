package autismclient.mixin.security;

import autismclient.protector.ConnectionPrivacyRegistry;
import autismclient.protector.ModdedParticleGuard;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleEngine;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.util.RandomSource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(ParticleEngine.class)
public class AutismProtectorParticleEngineMixin {
   @WrapOperation(
      method = "makeParticle",
      at = @At(
         value = "INVOKE",
         target = "Lnet/minecraft/client/particle/ParticleProvider;createParticle(Lnet/minecraft/core/particles/ParticleOptions;Lnet/minecraft/client/multiplayer/ClientLevel;DDDDDDLnet/minecraft/util/RandomSource;)Lnet/minecraft/client/particle/Particle;"
      )
   )
   private Particle autism$rejectNonVanillaProvider(
      ParticleProvider provider,
      ParticleOptions options,
      ClientLevel level,
      double x,
      double y,
      double z,
      double xSpeed,
      double ySpeed,
      double zSpeed,
      RandomSource random,
      Operation<Particle> original
   ) {
      ConnectionPrivacyRegistry.ConnectionSession context = ConnectionPrivacyRegistry.getByListener(Minecraft.getInstance().getConnection());
      return ModdedParticleGuard.guardCreate(provider, context, () -> original.call(provider, options, level, x, y, z, xSpeed, ySpeed, zSpeed, random));
   }
}

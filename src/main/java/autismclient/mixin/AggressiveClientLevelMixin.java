package autismclient.mixin;

import autismclient.aggressivepvp.Versions;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientLevel.class)
public abstract class AggressiveClientLevelMixin {
    @Inject(method = "addEntity(Lnet/minecraft/world/entity/Entity;)V", at = @At("TAIL"))
    private void ap$added(Entity entity, CallbackInfo ci) {
        if ((Object) this == Minecraft.getInstance().level) Versions.onEntityAdded(entity);
    }

    @Inject(method = "removeEntity(ILnet/minecraft/world/entity/Entity$RemovalReason;)V", at = @At("HEAD"))
    private void ap$removed(int id, Entity.RemovalReason reason, CallbackInfo ci) {
        if ((Object) this == Minecraft.getInstance().level) Versions.onEntityRemoved(((ClientLevel) (Object) this).getEntity(id));
    }
}

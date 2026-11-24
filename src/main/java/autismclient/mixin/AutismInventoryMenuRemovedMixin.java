package autismclient.mixin;

import autismclient.modules.PoolEntry;
import autismclient.util.ck;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.InventoryMenu;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(InventoryMenu.class)
public abstract class AutismInventoryMenuRemovedMixin {
   @Inject(method = "removed", at = @At("HEAD"), cancellable = true)
   private void autism$skipDrainWhenXCarryForced(Player player, CallbackInfo ci) {
      if (player instanceof LocalPlayer) {
         ck shared = ck.init_ck();
         PoolEntry module = PoolEntry.init_PoolEntry();
         boolean passive = module != null && module.get_Z();
         if (passive || shared.sync_Z()) {
            InventoryMenu self = (InventoryMenu)(Object)this;
            if (player.inventoryMenu == self) {
               ci.cancel();
            }
         }
      }
   }
}

package autismclient.mixin.accessor;

import net.minecraft.client.resources.server.DownloadedPackSource;
import net.minecraft.client.resources.server.ServerPackManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(DownloadedPackSource.class)
public interface AutismDownloadedPackSourceAccessor {
   @Accessor("manager")
   ServerPackManager autism$manager();
}

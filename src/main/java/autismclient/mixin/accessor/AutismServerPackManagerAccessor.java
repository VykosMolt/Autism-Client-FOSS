package autismclient.mixin.accessor;

import net.minecraft.client.resources.server.PackDownloader;
import net.minecraft.client.resources.server.ServerPackManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(ServerPackManager.class)
public interface AutismServerPackManagerAccessor {
   @Accessor("downloader")
   PackDownloader autism$downloader();

   @Mutable
   @Accessor("downloader")
   void autism$downloader(PackDownloader var1);
}

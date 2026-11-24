package autismclient.compat263;

import java.io.IOException;
import net.minecraft.server.packs.PackLocationInfo;
import net.minecraft.server.packs.PackResources;
import net.minecraft.server.packs.metadata.MetadataSectionType;

public abstract class CompatAbstractPackResources implements PackResources {
   private final PackLocationInfo location;

   protected CompatAbstractPackResources(PackLocationInfo var1) {
      this.location = var1;
   }

   @Override
   public <T> T getMetadataSection(MetadataSectionType<T> var1) throws IOException {
      return null;
   }

   @Override
   public PackLocationInfo location() {
      return this.location;
   }
}

package autismclient.g;

import java.io.IOException;
import java.io.InputStream;
import java.util.Optional;
import java.util.Set;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.PackLocationInfo;
import net.minecraft.server.packs.PackResources;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.PackResources.ResourceOutput;
import net.minecraft.server.packs.metadata.MetadataSectionType;
import net.minecraft.server.packs.repository.KnownPack;
import net.minecraft.server.packs.resources.IoSupplier;

public final class TableIndex implements PackResources {
   private final PackResources init;

   public TableIndex(PackResources delegate) {
      this.init = delegate;
   }

   @Override
   public IoSupplier<InputStream> getRootResource(String... paths) {
      return this.init.getRootResource(paths);
   }

   @Override
   public IoSupplier<InputStream> getResource(PackType type, Identifier location) {
      return !init(type, location.getPath()) ? null : this.init.getResource(type, location);
   }

   @Override
   public void listResources(PackType type, String namespace, String path, ResourceOutput output) {
      if (load(type, path)) {
         this.init.listResources(type, namespace, path, output);
      }
   }

   @Override
   public Set<String> getNamespaces(PackType type) {
      return type != PackType.CLIENT_RESOURCES ? Set.of() : Set.copyOf(this.init.getNamespaces(type));
   }

   @Override
   public <T> T getMetadataSection(MetadataSectionType<T> type) throws IOException {
      return this.init.getMetadataSection(type);
   }

   @Override
   public PackLocationInfo location() {
      return this.init.location();
   }

   @Override
   public String packId() {
      return this.init.packId();
   }

   @Override
   public Optional<KnownPack> knownPackInfo() {
      return this.init.knownPackInfo();
   }

   @Override
   public void close() {
      this.init.close();
   }

   private static boolean init(PackType type, String path) {
      return type == PackType.CLIENT_RESOURCES && path.startsWith("lang/") && path.endsWith(".json");
   }

   private static boolean load(PackType type, String path) {
      return type == PackType.CLIENT_RESOURCES && (path.equals("lang") || path.startsWith("lang/"));
   }
}

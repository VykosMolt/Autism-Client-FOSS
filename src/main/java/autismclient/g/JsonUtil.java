package autismclient.g;

import autismclient.compat263.CompatAbstractPackResources;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.Map.Entry;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.PackLocationInfo;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.PackResources.ResourceOutput;
import net.minecraft.server.packs.resources.IoSupplier;

public final class JsonUtil extends CompatAbstractPackResources implements EventBus.Inner_BufferHelper {
   private final JsonUtil.Inner_DataUtil init_JsonUtil_DataUtil;
   private final String init_Str;
   private final boolean init_Z;
   private final EventBus.Inner_ConfigStore init_EventBus_ConfigSto;

   private JsonUtil(PackLocationInfo location, JsonUtil.Inner_DataUtil snapshot, String prefix, boolean metadataOnly, EventBus.Inner_ConfigStore isolation) {
      super(location);
      this.init_JsonUtil_DataUtil = snapshot;
      this.init_Str = prefix;
      this.init_Z = metadataOnly;
      this.init_EventBus_ConfigSto = isolation;
   }

   @Override
   public EventBus.Inner_ConfigStore init() {
      return this.init_EventBus_ConfigSto;
   }

   @Override
   public IoSupplier<InputStream> getRootResource(String... path) {
      return this.init_Str.isEmpty() && path.length == 1 && "pack.mcmeta".equals(path[0]) && this.init_JsonUtil_DataUtil.init_BArr != null
         ? init(this.init_JsonUtil_DataUtil.init_BArr)
         : null;
   }

   @Override
   public IoSupplier<InputStream> getResource(PackType type, Identifier location) {
      if (!this.init_Z && type == PackType.CLIENT_RESOURCES && init(location.getPath())) {
         byte[] bytes = this.init_JsonUtil_DataUtil.init_Map.get(this.init(location.getNamespace(), location.getPath()));
         return bytes == null ? null : init(bytes);
      } else {
         return null;
      }
   }

   @Override
   public void listResources(PackType type, String namespace, String directory, ResourceOutput output) {
      if (!this.init_Z && type == PackType.CLIENT_RESOURCES && init(directory) && Identifier.isValidNamespace(namespace)) {
         String root = this.init_Str + type.getDirectory() + "/" + namespace + "/";
         String directoryPrefix = root + directory + "/";

         for (Entry<String, byte[]> entry : this.init_JsonUtil_DataUtil.init_Map.entrySet()) {
            String path = entry.getKey();
            if (path.startsWith(directoryPrefix)) {
               Identifier identifier = Identifier.tryBuild(namespace, path.substring(root.length()));
               if (identifier != null && init(identifier.getPath())) {
                  output.accept(identifier, init(entry.getValue()));
               }
            }
         }
      }
   }

   @Override
   public Set<String> getNamespaces(PackType type) {
      if (!this.init_Z && type == PackType.CLIENT_RESOURCES) {
         String root = this.init_Str + type.getDirectory() + "/";
         Set<String> namespaces = new HashSet<>();

         for (String path : this.init_JsonUtil_DataUtil.init_Map.keySet()) {
            if (path.startsWith(root)) {
               int end = path.indexOf(47, root.length());
               if (end >= 0) {
                  String namespace = path.substring(root.length(), end);
                  if (Identifier.isValidNamespace(namespace)) {
                     namespaces.add(namespace);
                  }
               }
            }
         }

         return Set.copyOf(namespaces);
      } else {
         return Set.of();
      }
   }

   @Override
   public void close() {
   }

   private String init(String namespace, String path) {
      return this.init_Str + PackType.CLIENT_RESOURCES.getDirectory() + "/" + namespace + "/" + path;
   }

   private static IoSupplier<InputStream> init(byte[] bytes) {
      return () -> new ByteArrayInputStream(bytes);
   }

   private static boolean init(String path) {
      return path.equals("lang") || path.startsWith("lang/") && path.length() > "lang/".length();
   }

   public static final class Inner_DataUtil {
      private static final int init_I = 1048576;
      private static final int load = 8388608;
      private static final int read = 67108864;
      private static final int write = 4096;
      private final byte[] init_BArr;
      private final Map<String, byte[]> init_Map;

      private Inner_DataUtil(byte[] packMetadata, Map<String, byte[]> languages) {
         this.init_BArr = packMetadata;
         this.init_Map = languages;
      }

      static JsonUtil.Inner_DataUtil init(Path path) {
         byte[] metadata = null;
         Map<String, byte[]> languages = new HashMap<>();
         long languageBytes = 0L;

         try (ZipFile zip = new ZipFile(path.toFile())) {
            Enumeration<? extends ZipEntry> entries = zip.entries();

            while (entries.hasMoreElements()) {
               ZipEntry entry = entries.nextElement();
               if (!entry.isDirectory()) {
                  String name = entry.getName();
                  boolean metadataEntry = "pack.mcmeta".equals(name);
                  if (metadataEntry || load(name)) {
                     if ((!metadataEntry || metadata == null) && (languages.size() < 4096 || metadataEntry)) {
                        int limit = metadataEntry ? 1048576 : 8388608;
                        if (entry.getSize() > limit) {
                           return init();
                        }

                        byte[] bytes;
                        try (InputStream input = zip.getInputStream(entry)) {
                           bytes = init(input, limit);
                        }

                        if (metadataEntry) {
                           metadata = bytes;
                           continue;
                        }

                        languageBytes += bytes.length;
                        if (languageBytes <= 67108864L && languages.putIfAbsent(name, bytes) == null) {
                           continue;
                        }

                        return init();
                     }

                     return init();
                  }
               }
            }
         } catch (IOException | RuntimeException ignored) {
            return init();
         }

         return new JsonUtil.Inner_DataUtil(metadata, Collections.unmodifiableMap(new TreeMap<>(languages)));
      }

      static JsonUtil.Inner_DataUtil init() {
         return new JsonUtil.Inner_DataUtil(null, Map.of());
      }

      JsonUtil init(PackLocationInfo location, String overlay, boolean metadataOnly, EventBus.Inner_ConfigStore isolation) {
         String prefix = overlay.isEmpty() ? "" : overlay + "/";
         return new JsonUtil(location, this, prefix, metadataOnly, isolation);
      }

      static boolean init(String overlay) {
         if (overlay != null && !overlay.isEmpty()) {
            for (int index = 0; index < overlay.length(); index++) {
               char character = overlay.charAt(index);
               if (character != '-'
                  && character != '_'
                  && character != '.'
                  && (character < 'a' || character > 'z')
                  && (character < 'A' || character > 'Z')
                  && (character < '0' || character > '9')) {
                  return false;
               }
            }

            return true;
         } else {
            return false;
         }
      }

      private static boolean load(String name) {
         String path = name;
         if (!path.startsWith(PackType.CLIENT_RESOURCES.getDirectory() + "/")) {
            int separator = path.indexOf(47);
            if (separator <= 0 || !init(path.substring(0, separator))) {
               return false;
            }

            path = path.substring(separator + 1);
         }

         String assets = PackType.CLIENT_RESOURCES.getDirectory() + "/";
         if (!path.startsWith(assets)) {
            return false;
         } else {
            int namespaceEnd = path.indexOf(47, assets.length());
            if (namespaceEnd > assets.length() && path.startsWith("lang/", namespaceEnd + 1)) {
               String namespace = path.substring(assets.length(), namespaceEnd);
               String languagePath = path.substring(namespaceEnd + 1);
               return JsonUtil.init(languagePath) && Identifier.tryBuild(namespace, languagePath) != null;
            } else {
               return false;
            }
         }
      }

      private static byte[] init(InputStream input, int limit) throws IOException {
         ByteArrayOutputStream output = new ByteArrayOutputStream();
         byte[] buffer = new byte[8192];
         int total = 0;

         int read;
         while ((read = input.read(buffer, 0, Math.min(buffer.length, limit + 1 - total))) > 0) {
            total += read;
            if (total > limit) {
               throw new IOException("Isolated resource-pack entry is too large");
            }

            output.write(buffer, 0, read);
         }

         return output.toByteArray();
      }
   }
}

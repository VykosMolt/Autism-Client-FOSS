package autismclient.ai;

import java.util.List;
import java.util.Objects;

final class AiModelCatalog {
   private AiModelCatalog.CatalogKey init_av_DataUtil;
   private volatile long init_J;
   private List<String> init_List = List.of();
   private boolean init_Z;
   private boolean load;
   private String init_Str = "";

   boolean retarget(AiModelCatalog.CatalogKey next) {
      if (Objects.equals(this.init_av_DataUtil, next)) {
         return false;
      }

      this.reset();
      this.init_av_DataUtil = next;
      return true;
   }

   void reset() {
      this.init_J++;
      this.init_av_DataUtil = null;
      this.init_List = List.of();
      this.init_Z = false;
      this.load = false;
      this.init_Str = "";
   }

   long beginFetch(AiModelCatalog.CatalogKey next) {
      this.retarget(next);
      long token = ++this.init_J;
      this.init_List = List.of();
      this.init_Z = true;
      this.load = true;
      this.init_Str = "";
      return token;
   }

   boolean init(long token, AiModelCatalog.CatalogKey identity, AiModels.Inner_CachePool result) {
      if (token == this.init_J && Objects.equals(this.init_av_DataUtil, identity)) {
         this.load = false;
         this.init_List = result.ok() ? List.copyOf(result.models()) : List.of();
         this.init_Str = result.ok() ? "" : result.error();
         if (result.ok()) {
            this.init_av_DataUtil = new AiModelCatalog.CatalogKey(
               result.provider() != null ? result.provider() : identity.provider(),
               result.endpoint() != null && !result.endpoint().isBlank() ? result.endpoint() : identity.endpoint(),
               identity.credentialDigest()
            );
         }

         return true;
      } else {
         return false;
      }
   }

   boolean isCurrent(long token) {
      return this.init_J == token;
   }

   boolean init_Z() {
      return this.init_Z;
   }

   boolean isScanning() {
      return this.load;
   }

   List<String> models() {
      return this.init_List;
   }

   String message() {
      return this.init_Str;
   }

   record CatalogKey(AiSettings.Provider provider, String endpoint, String credentialDigest) {
      static AiModelCatalog.CatalogKey of(AiSettings.Provider provider, String endpoint, String key) {
         return new AiModelCatalog.CatalogKey(provider, AiModels.baseUrl(provider, endpoint), AiJson.sha256Hex(key == null ? "" : key.trim()));
      }
   }
}

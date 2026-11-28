package autismclient.ai;

import java.util.List;
import java.util.Objects;

final class AiModelCatalog {
   private AiModelCatalog.CatalogKey currentKey;
   private volatile long requestToken;
   private List<String> models = List.of();
   private boolean requested;
   private boolean loading;
   private String error = "";

   boolean retarget(AiModelCatalog.CatalogKey next) {
      if (Objects.equals(this.currentKey, next)) {
         return false;
      }

      this.reset();
      this.currentKey = next;
      return true;
   }

   void reset() {
      this.requestToken++;
      this.currentKey = null;
      this.models = List.of();
      this.requested = false;
      this.loading = false;
      this.error = "";
   }

   long beginFetch(AiModelCatalog.CatalogKey next) {
      this.retarget(next);
      long token = ++this.requestToken;
      this.models = List.of();
      this.requested = true;
      this.loading = true;
      this.error = "";
      return token;
   }

   boolean acceptResult(long token, AiModelCatalog.CatalogKey identity, AiModels.FetchResult result) {
      if (token == this.requestToken && Objects.equals(this.currentKey, identity)) {
         this.loading = false;
         this.models = result.ok() ? List.copyOf(result.models()) : List.of();
         this.error = result.ok() ? "" : result.error();
         if (result.ok()) {
            this.currentKey = new AiModelCatalog.CatalogKey(
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
      return this.requestToken == token;
   }

   boolean wasRequested() {
      return this.requested;
   }

   boolean isScanning() {
      return this.loading;
   }

   List<String> models() {
      return this.models;
   }

   String message() {
      return this.error;
   }

   record CatalogKey(AiSettings.Provider provider, String endpoint, String credentialDigest) {
      static AiModelCatalog.CatalogKey of(AiSettings.Provider provider, String endpoint, String key) {
         return new AiModelCatalog.CatalogKey(provider, AiModels.baseUrl(provider, endpoint), AiJson.sha256Hex(key == null ? "" : key.trim()));
      }
   }
}

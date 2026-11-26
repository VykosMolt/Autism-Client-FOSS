package autismclient.ai;

import java.util.List;
import java.util.Objects;

final class AiModelCatalog {
   private AiModelCatalog.Inner_DataUtil init_av_DataUtil;
   private volatile long init_J;
   private List<String> init_List = List.of();
   private boolean init_Z;
   private boolean load;
   private String init_Str = "";

   boolean init_Z(AiModelCatalog.Inner_DataUtil next) {
      if (Objects.equals(this.init_av_DataUtil, next)) {
         return false;
      }

      this.init_V();
      this.init_av_DataUtil = next;
      return true;
   }

   void init_V() {
      this.init_J++;
      this.init_av_DataUtil = null;
      this.init_List = List.of();
      this.init_Z = false;
      this.load = false;
      this.init_Str = "";
   }

   long init_J(AiModelCatalog.Inner_DataUtil next) {
      this.init_Z(next);
      long token = ++this.init_J;
      this.init_List = List.of();
      this.init_Z = true;
      this.load = true;
      this.init_Str = "";
      return token;
   }

   boolean init(long token, AiModelCatalog.Inner_DataUtil identity, AiModels.Inner_CachePool result) {
      if (token == this.init_J && Objects.equals(this.init_av_DataUtil, identity)) {
         this.load = false;
         this.init_List = result.ok() ? List.copyOf(result.models()) : List.of();
         this.init_Str = result.ok() ? "" : result.error();
         if (result.ok()) {
            this.init_av_DataUtil = new AiModelCatalog.Inner_DataUtil(
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

   boolean init(long token) {
      return this.init_J == token;
   }

   boolean init_Z() {
      return this.init_Z;
   }

   boolean load() {
      return this.load;
   }

   List<String> init_List() {
      return this.init_List;
   }

   String init_Str() {
      return this.init_Str;
   }

   record Inner_DataUtil(AiSettings.Provider provider, String endpoint, String credentialDigest) {
      static AiModelCatalog.Inner_DataUtil of(AiSettings.Provider provider, String endpoint, String key) {
         return new AiModelCatalog.Inner_DataUtil(provider, AiModels.init_Str(provider, endpoint), AiJson.init_Str_Str(key == null ? "" : key.trim()));
      }
   }
}

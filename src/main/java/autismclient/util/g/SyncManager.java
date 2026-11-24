package autismclient.util.g;

import com.mojang.authlib.minecraft.SessionService;
import com.mojang.authlib.minecraft.UserApiService;
import com.mojang.authlib.services.MinecraftServicesDiscoveryService;
import com.mojang.util.UndashedUuid;
import java.util.Optional;
import java.util.UUID;
import net.minecraft.client.Minecraft;
import net.minecraft.client.User;
import net.minecraft.client.multiplayer.ProfileKeyPairManager;
import net.minecraft.core.UUIDUtil;
import net.minecraft.server.Services;
import net.minecraft.util.SignatureValidator;

final class SyncManager {
   private SyncManager() {
   }

   static SyncManager.Inner_DataUtil init_SyncManager_DataUt(String accountId) {
      Minecraft minecraft = Minecraft.getInstance();
      if ("default".equals(accountId)) {
         User user = autismclient.util.FieldCache.init_User();
         MinecraftServicesDiscoveryService authentication = MinecraftServicesDiscoveryService.createOffline(autismclient.util.MapStore.init_Proxy());
         Services services = Services.create(authentication, minecraft.gameDirectory);
         return new SyncManager.Inner_DataUtil(
            accountId,
            user.getAccessToken().isBlank() ? autismclient.util.MethodUtil.init : autismclient.util.MethodUtil.load,
            user,
            services.sessionService(),
            init(minecraft, authentication, user),
            init(services)
         );
      }

      autismclient.util.BitSetUtil stored = autismclient.util.TokenStore.init_TokenStore().init_BitSetUtil(accountId);
      if (stored == null) {
         throw new IllegalStateException("Account no longer exists");
      }

      autismclient.util.BitSetUtil account = init_BitSetUtil(stored);
      if (!account.load_Z()) {
         throw new IllegalStateException(account.load_Str().isBlank() ? "Account authentication failed" : account.load_Str());
      }

      autismclient.util.TokenStore.init_TokenStore().init_V(account);
      User user = new User(account.update, init_UUID(account), init_Str(account), Optional.empty(), Optional.empty());
      boolean providerSession = account.init_MethodUtil == autismclient.util.MethodUtil.write;
      MinecraftServicesDiscoveryService authentication = providerSession
         ? autismclient.util.PathHelper.init_YggdrasilAuthentic()
         : MinecraftServicesDiscoveryService.createOffline(autismclient.util.MapStore.init_Proxy());
      Services services = Services.create(authentication, minecraft.gameDirectory);
      return new SyncManager.Inner_DataUtil(
         account.init_Str,
         account.init_MethodUtil,
         user,
         services.sessionService(),
         providerSession ? ProfileKeyPairManager.EMPTY_KEY_MANAGER : init(minecraft, authentication, user),
         providerSession ? null : init(services)
      );
   }

   private static ProfileKeyPairManager init(Minecraft minecraft, MinecraftServicesDiscoveryService auth, User user) {
      if (user.getAccessToken() != null && !user.getAccessToken().isBlank()) {
         try {
            UserApiService userApi = auth.createUserApiService(user.getAccessToken());
            return ProfileKeyPairManager.create(userApi, user, minecraft.gameDirectory.toPath());
         } catch (RuntimeException ignored) {
            return ProfileKeyPairManager.EMPTY_KEY_MANAGER;
         }
      } else {
         return ProfileKeyPairManager.EMPTY_KEY_MANAGER;
      }
   }

   private static SignatureValidator init(Services services) {
      try {
         return services.canValidateProfileKeys() ? services.profileKeySignatureValidator() : null;
      } catch (RuntimeException ignored) {
         return null;
      }
   }

   private static UUID init_UUID(autismclient.util.BitSetUtil account) {
      if (account.sync != null && !account.sync.isBlank()) {
         try {
            return UndashedUuid.fromStringLenient(account.sync);
         } catch (RuntimeException ignored) {
            try {
               return UUID.fromString(account.sync);
            } catch (RuntimeException var3) {
            }
         }
      }

      return UUIDUtil.createOfflinePlayerUUID(account.update);
   }

   private static String init_Str(autismclient.util.BitSetUtil account) {
      if (account.init_MethodUtil == autismclient.util.MethodUtil.write) {
         return init_Str(account.write);
      } else {
         return account.init_MethodUtil != autismclient.util.MethodUtil.init && account.init_MethodUtil != autismclient.util.MethodUtil.update
            ? init_Str(account.read_Str)
            : "";
      }
   }

   private static autismclient.util.BitSetUtil init_BitSetUtil(autismclient.util.BitSetUtil source) {
      autismclient.util.BitSetUtil copy = new autismclient.util.BitSetUtil();
      copy.init_Str = source.write_Str();
      copy.init_MethodUtil = source.init_MethodUtil;
      copy.load_Str = init_Str(source.load_Str);
      copy.read_Str = init_Str(source.read_Str);
      copy.write = init_Str(source.write);
      copy.update = init_Str(source.update);
      copy.sync = init_Str(source.sync);
      copy.init_J = source.init_J;
      return copy;
   }

   private static String init_Str(String value) {
      return value == null ? "" : value;
   }

   record Inner_DataUtil(
      String init_Str,
      autismclient.util.MethodUtil init_MethodUtil,
      User init_User,
      SessionService init_MinecraftSessionSe,
      ProfileKeyPairManager init_ProfileKeyPairMana,
      SignatureValidator init_SignatureValidator
   ) {
   }
}

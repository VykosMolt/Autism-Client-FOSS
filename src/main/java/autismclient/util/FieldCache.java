package autismclient.util;

import autismclient.AutismClientAddon;
import autismclient.mixin.accessor.AutismMinecraftAccessor;
import com.mojang.authlib.minecraft.UserApiService;
import com.mojang.authlib.services.FriendsService;
import com.mojang.authlib.services.MinecraftServicesDiscoveryService;
import java.nio.file.Path;
import java.util.concurrent.CompletableFuture;
import net.minecraft.client.Minecraft;
import net.minecraft.client.User;
import net.minecraft.client.gui.screens.social.PlayerSocialManager;
import net.minecraft.client.gui.screens.social.RemoteFriendListUpdateHandler;
import net.minecraft.client.multiplayer.ProfileKeyPairManager;
import net.minecraft.client.multiplayer.chat.report.ReportEnvironment;
import net.minecraft.client.multiplayer.chat.report.ReportingContext;
import net.minecraft.client.renderer.texture.SkinTextureDownloader;
import net.minecraft.client.resources.SkinManager;
import net.minecraft.server.Services;
import net.minecraft.util.Util;

public final class FieldCache {
   private static User init_User;
   private static String init_Str = "";

   private FieldCache() {
   }

   public static User init_User() {
      if (init_User == null) {
         init_User = Minecraft.getInstance().getUser();
      }

      return init_User;
   }

   public static boolean init(User user) {
      return init(user, MinecraftServicesDiscoveryService.createOffline(MapStore.init_Proxy()));
   }

   public static boolean init(User user, MinecraftServicesDiscoveryService authService) {
      return init(user, authService, false);
   }

   public static boolean load(User user) {
      init_Str = "";

      try {
         return init(user, PathHelper.init_YggdrasilAuthentic(), true);
      } catch (Exception e) {
         init_Str = init(e);
         AutismClientAddon.LOG.error("Failed to prepare TheAltening session services", e);
         return false;
      }
   }

   private static boolean init(User user, MinecraftServicesDiscoveryService authService, boolean providerSession) {
      init_Str = "";

      try {
         Minecraft mc = Minecraft.getInstance();
         if (init_User == null) {
            init_User = mc.getUser();
         }

         AutismMinecraftAccessor accessor = (AutismMinecraftAccessor)mc;
         MinecraftServicesDiscoveryService userApiAuthService = providerSession
            ? authService
            : MinecraftServicesDiscoveryService.createOffline(MapStore.init_Proxy());
         Services services = Services.create(authService, mc.gameDirectory);
         UserApiService apiService = providerSession ? UserApiService.OFFLINE : userApiAuthService.createUserApiService(user.getAccessToken());
         FriendsService friendsService = providerSession ? PathHelper.init_FriendsService : userApiAuthService.createFriendsService(user.getAccessToken());
         RemoteFriendListUpdateHandler friendListUpdateHandler = new RemoteFriendListUpdateHandler(friendsService, mc);
         Path skinCachePath = mc.gameDirectory.toPath().resolve("assets").resolve("skins");
         PlayerSocialManager socialManager = new PlayerSocialManager(mc, apiService, friendsService, friendListUpdateHandler);
         ProfileKeyPairManager keys = providerSession
            ? ProfileKeyPairManager.EMPTY_KEY_MANAGER
            : ProfileKeyPairManager.create(apiService, user, mc.gameDirectory.toPath());
         ReportingContext reporting = ReportingContext.create(ReportEnvironment.local(), apiService);
         SkinManager skins = new SkinManager(skinCachePath, services, new SkinTextureDownloader(MapStore.init_Proxy(), mc.getTextureManager(), mc), mc);
         accessor.autism$setServices(services);
         accessor.autism$setUser(user);
         accessor.autism$setUserApiService(apiService);
         accessor.autism$setRemoteFriendListUpdateHandler(friendListUpdateHandler);
         accessor.autism$setPlayerSocialManager(socialManager);
         accessor.autism$setProfileKeyPairManager(keys);
         accessor.autism$setReportingContext(reporting);
         accessor.autism$setProfileFuture(
            CompletableFuture.supplyAsync(() -> services.sessionService().fetchProfile(user.getProfileId(), true), Util.nonCriticalIoPool())
         );
         accessor.autism$setSkinManager(skins);
         return true;
      } catch (Exception e) {
         init_Str = init(e);
         AutismClientAddon.LOG.error("Failed to switch Autism account session", e);
         return false;
      }
   }

   public static String init_Str() {
      return init_Str == null ? "" : init_Str;
   }

   private static String init(Throwable error) {
      if (error == null) {
         return "unknown error";
      }

      String name = error.getClass().getSimpleName();
      String message = error.getMessage();
      if (message != null && !message.isBlank()) {
         message = message.replace('\n', ' ').replace('\r', ' ').trim();
         if (message.length() > 120) {
            message = message.substring(0, 117) + "...";
         }

         return name + ": " + message;
      } else {
         return name;
      }
   }
}

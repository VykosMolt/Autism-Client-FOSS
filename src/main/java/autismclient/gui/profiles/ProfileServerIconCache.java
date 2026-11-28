package autismclient.gui.profiles;

import autismclient.gui.f.SyncManager;
import autismclient.util.AutismProfileManager;
import autismclient.util.GuiIconAtlas;
import com.mojang.blaze3d.platform.NativeImage;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.client.multiplayer.ServerList;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.resources.Identifier;
import net.minecraft.util.PngInfo;

final class ProfileServerIconCache {
   private static int nextTextureId;
   private ServerList serverList;
   private final Map<String, Optional<ServerData>> serversByPattern = new HashMap<>();
   private final Map<ServerData, ProfileServerIconCache.CachedIcon> iconCache = new HashMap<>();

   ServerData serverFor(String pattern) {
      if (pattern != null && !pattern.isBlank() && !pattern.equals("*")) {
         ServerData current = Minecraft.getInstance().getCurrentServer();
         boolean currentMatches = current != null && AutismProfileManager.matchesAddress(current.ip, pattern);
         return currentMatches && this.hasIcon(current)
            ? current
            : this.serversByPattern.computeIfAbsent(pattern, key -> Optional.ofNullable(this.findServer(key))).orElse(currentMatches ? current : null);
      } else {
         return null;
      }
   }

   private ServerData findServer(String pattern) {
      if (this.serverList == null) {
         this.serverList = new ServerList(Minecraft.getInstance());
         this.serverList.load();
      }

      ServerData firstMatch = this.serverList.get(pattern);
      if (this.hasIcon(firstMatch)) {
         return firstMatch;
      }

      for (int i = 0; i < this.serverList.size(); i++) {
         ServerData server = this.serverList.get(i);
         if (AutismProfileManager.matchesAddress(server.ip, pattern)) {
            if (this.hasIcon(server)) {
               return server;
            }

            if (firstMatch == null) {
               firstMatch = server;
            }
         }
      }

      return firstMatch;
   }

   boolean hasIcon(ServerData server) {
      return this.iconFor(server) != null;
   }

   void draw(SyncManager context, ServerData server, Identifier fallback, int x, int y, int size) {
      Identifier texture = this.iconFor(server);
      if (texture != null) {
         context.init_GuiGraphicsExtract().blit(texture, x, y, x + size, y + size, 0.0F, 1.0F, 0.0F, 1.0F);
      } else {
         GuiIconAtlas.draw(context.init_GuiGraphicsExtract(), fallback, x, y, size, context.init_TableIndex().colors().textSecondary);
      }
   }

   private Identifier iconFor(ServerData server) {
      if (server == null) {
         return null;
      }

      byte[] bytes = server.getIconBytes();
      ProfileServerIconCache.CachedIcon cached = this.iconCache.get(server);
      if (cached != null && cached.bytes() == bytes) {
         return cached.texture();
      }

      if (cached != null && cached.texture() != null) {
         Minecraft.getInstance().getTextureManager().release(cached.texture());
      }

      Identifier texture = bytes == null ? null : registerIcon(bytes);
      this.iconCache.put(server, new ProfileServerIconCache.CachedIcon(bytes, texture));
      return texture;
   }

   private static Identifier registerIcon(byte[] png) {
      try {
         PngInfo info = PngInfo.fromBytes(png);
         if (info.width() == 64 && info.height() == 64) {
            NativeImage pixels = NativeImage.read(png);
            try {
               DynamicTexture texture = new DynamicTexture(() -> "Profile server icon", pixels);
               Identifier id = Identifier.fromNamespaceAndPath("autismclient", "dynamic/profile_server/" + nextTextureId++);
               Minecraft.getInstance().getTextureManager().register(id, texture);
               return id;
            } catch (Throwable t) {
               pixels.close();
               throw t;
            }
         } else {
            return null;
         }
      } catch (IOException | IllegalArgumentException invalidIcon) {
         return null;
      }
   }

   void release() {
      TextureManager manager = Minecraft.getInstance().getTextureManager();

      for (ProfileServerIconCache.CachedIcon cached : this.iconCache.values()) {
         if (cached.texture() != null) {
            manager.release(cached.texture());
         }
      }

      this.iconCache.clear();
      this.serversByPattern.clear();
      this.serverList = null;
   }

   private record CachedIcon(byte[] bytes, Identifier texture) {
   }
}

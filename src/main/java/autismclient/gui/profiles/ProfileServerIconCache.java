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
   private static int init_I;
   private ServerList init_ServerList;
   private final Map<String, Optional<ServerData>> init_Map = new HashMap<>();
   private final Map<ServerData, ProfileServerIconCache.Inner_DataUtil> load = new HashMap<>();

   ServerData serverFor(String pattern) {
      if (pattern != null && !pattern.isBlank() && !pattern.equals("*")) {
         ServerData current = Minecraft.getInstance().getCurrentServer();
         boolean currentMatches = current != null && AutismProfileManager.load_Z_Str_Str(current.ip, pattern);
         return currentMatches && this.hasIcon(current)
            ? current
            : this.init_Map.computeIfAbsent(pattern, key -> Optional.ofNullable(this.load(key))).orElse(currentMatches ? current : null);
      } else {
         return null;
      }
   }

   private ServerData load(String pattern) {
      if (this.init_ServerList == null) {
         this.init_ServerList = new ServerList(Minecraft.getInstance());
         this.init_ServerList.load();
      }

      ServerData firstMatch = this.init_ServerList.get(pattern);
      if (this.hasIcon(firstMatch)) {
         return firstMatch;
      }

      for (int i = 0; i < this.init_ServerList.size(); i++) {
         ServerData server = this.init_ServerList.get(i);
         if (AutismProfileManager.load_Z_Str_Str(server.ip, pattern)) {
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
      return this.init_Identifier_ServerData(server) != null;
   }

   void init(SyncManager context, ServerData server, Identifier fallback, int x, int y, int size) {
      Identifier texture = this.init_Identifier_ServerData(server);
      if (texture != null) {
         context.init_GuiGraphicsExtract().blit(texture, x, y, x + size, y + size, 0.0F, 1.0F, 0.0F, 1.0F);
      } else {
         GuiIconAtlas.draw(context.init_GuiGraphicsExtract(), fallback, x, y, size, context.init_TableIndex().colors().textSecondary);
      }
   }

   private Identifier init_Identifier_ServerData(ServerData server) {
      if (server == null) {
         return null;
      }

      byte[] bytes = server.getIconBytes();
      ProfileServerIconCache.Inner_DataUtil cached = this.load.get(server);
      if (cached != null && cached.init_BArr() == bytes) {
         return cached.init_Identifier();
      }

      if (cached != null && cached.init_Identifier() != null) {
         Minecraft.getInstance().getTextureManager().release(cached.init_Identifier());
      }

      Identifier texture = bytes == null ? null : init_Identifier_BArr(bytes);
      this.load.put(server, new ProfileServerIconCache.Inner_DataUtil(bytes, texture));
      return texture;
   }

   private static Identifier init_Identifier_BArr(byte[] png) {
      try {
         PngInfo info = PngInfo.fromBytes(png);
         if (info.width() == 64 && info.height() == 64) {
            NativeImage pixels = NativeImage.read(png);
            try {
               DynamicTexture texture = new DynamicTexture(() -> "Profile server icon", pixels);
               Identifier id = Identifier.fromNamespaceAndPath("autismclient", "dynamic/profile_server/" + init_I++);
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

   void init() {
      TextureManager manager = Minecraft.getInstance().getTextureManager();

      for (ProfileServerIconCache.Inner_DataUtil cached : this.load.values()) {
         if (cached.init_Identifier() != null) {
            manager.release(cached.init_Identifier());
         }
      }

      this.load.clear();
      this.init_Map.clear();
      this.init_ServerList = null;
   }

   private record Inner_DataUtil(byte[] init_BArr, Identifier init_Identifier) {
   }
}

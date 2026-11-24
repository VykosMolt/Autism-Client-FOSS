package autismclient.util;

import autismclient.AutismClientAddon;
import autismclient.compat263.Compat263;
import com.google.gson.GsonBuilder;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Optional;
import java.util.UUID;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.ConfirmScreen;
import net.minecraft.client.gui.screens.ConnectScreen;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.client.multiplayer.ServerData.ServerPackStatus;
import net.minecraft.client.multiplayer.ServerData.Type;
import net.minecraft.client.multiplayer.resolver.ServerAddress;
import net.minecraft.locale.Language;
import net.minecraft.network.protocol.common.ClientboundResourcePackPushPacket;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;

public final class bU {
   private static Path init_Path;
   private static String init_Str;
   private static String load_Str;
   private static int init_I;
   private static int load_I;
   private static boolean init_Z;
   private static boolean load_Z;
   private static boolean read;
   private static final UUID init_UUID = UUID.fromString("ebc8d70a-0870-40af-9355-44b7a931e622");

   private bU() {
   }

   public static void init() {
      String directory = System.getProperty("autism.privacy.compat.dir", "");
      if (!directory.isBlank() && FabricLoader.getInstance().getGameDir().toString().contains("autism-ai-qualification-")) {
         init_Path = Path.of(directory);
         init_Str = System.getProperty("autism.privacy.compat.server", "");
         load_Str = System.getProperty("autism.privacy.compat.target", "");
         if (!init_Str.isBlank() && !init_Str.startsWith("127.0.0.1:")) {
            throw new IllegalArgumentException("Privacy qualification only connects to IPv4 loopback");
         }

         ClientTickEvents.END_CLIENT_TICK.register(bU::init);
      }
   }

   private static void init(Minecraft mc) {
      if (!load_Z) {
         try {
            if (mc.level == null || mc.player == null) {
               if (!init_Str.isBlank() && !init_Z && ++init_I > 80 && mc.gui.screen() != null) {
                  init_Z = true;
                  if (!load_Str.isBlank()) {
                     Class<?> versions = Class.forName("com.viaversion.viaversion.api.protocol.version.ProtocolVersion");
                     Object version = versions.getMethod("getClosest", String.class).invoke(null, load_Str);
                     if (version == null) {
                        throw new IllegalStateException("Unknown qualification Via target");
                     }

                     try {
                        Class.forName("com.viaversion.viafabricplus.protocoltranslator.ProtocolTranslator")
                           .getMethod("setTargetVersion", versions, boolean.class)
                           .invoke(null, version, true);
                     } catch (ClassNotFoundException | NoSuchMethodException newerApi) {
                        Object api = Class.forName("com.viaversion.viafabricplus.ViaFabricPlus").getMethod("api").invoke(null);
                        api.getClass().getMethod("setTargetVersion", versions).invoke(api, version);
                     }
                  }

                  ServerData server = new ServerData("Privacy loopback qualification", init_Str, Type.OTHER);
                  server.setResourcePackStatus(ServerPackStatus.ENABLED);
                  ConnectScreen.startConnecting(mc.gui.screen(), mc, ServerAddress.parseString(init_Str), server, false, null);
               }

               return;
            }

            load_I++;
            String packUrl = System.getProperty("autism.privacy.compat.pack", "");
            if (!packUrl.isBlank() && !read && load_I >= 20 && mc.hasSingleplayerServer()) {
               if (!packUrl.startsWith("http://127.0.0.1:")) {
                  throw new IllegalArgumentException("Qualification pack must use loopback");
               }

               read = true;
               UUID playerId = mc.player.getUUID();
               mc.getSingleplayerServer()
                  .execute(
                     () -> {
                        ServerPlayer player = mc.getSingleplayerServer().getPlayerList().getPlayer(playerId);
                        if (player != null) {
                           player.connection
                              .send(
                                 new ClientboundResourcePackPushPacket(
                                    init_UUID, packUrl, System.getProperty("autism.privacy.compat.hash", ""), false, Optional.empty()
                                 )
                              );
                        }
                     }
                  );
            }

            if (read && mc.gui.screen() instanceof ConfirmScreen prompt) {
               prompt.children()
                  .stream()
                  .filter(child -> child instanceof Button)
                  .map(child -> (Button)child)
                  .filter(button -> button.active)
                  .findFirst()
                  .ifPresent(button -> button.onPress(Compat263.newGlfwKeyEvent(257, 0, 0)));
            }

            if (load_I < 240) {
               return;
            }

            boolean mounted = mc.getResourceManager().listPacks().anyMatch(pack -> pack.packId().startsWith("server/"));
            if (!packUrl.isBlank() && !mounted && load_I < 1200) {
               return;
            }

            ClientPacketListener listener = mc.getConnection();
            autismclient.g.BufferHelper.Inner_StreamUtil context = autismclient.g.BufferHelper.init(listener);
            LinkedHashMap<String, Object> result = new LinkedHashMap<>();
            result.put("worldTicks", load_I);
            result.put("local", listener != null && listener.getConnection().isMemoryConnection());
            result.put("mode", context == null ? "missing" : context.init_BufferHelper_Cache().name());
            result.put("fabricInbound", listener != null && autismclient.g.ConfigStore.init(listener));
            result.put("axiomInstalled", FabricLoader.getInstance().isModLoaded("axiom"));
            result.put("olderProtocol", listener != null && autismclient.compat.a.DataUtil.init_Z(listener.getConnection()));
            result.put("bypass", AutismConfig.getGlobal().pretendPackAccepted);
            result.put(
               "serverDirtTexture",
               mc.getResourceManager()
                  .getResource(Identifier.withDefaultNamespace("textures/block/dirt.png"))
                  .map(resource -> resource.sourcePackId().startsWith("server/"))
                  .orElse(false)
            );
            result.put("serverTranslation", Language.getInstance().getOrDefault("privacy_compat.server_texture"));
            result.put("serverPackMounted", mounted);
            boolean packOk = packUrl.isBlank() || mounted && (Boolean)result.get("serverDirtTexture") != AutismConfig.getGlobal().pretendPackAccepted;
            result.put("passed", context != null && listener != null && mc.player.connection == listener && packOk);
            Files.createDirectories(init_Path);
            Files.writeString(init_Path.resolve("join.json"), new GsonBuilder().setPrettyPrinting().create().toJson(result));
            load_Z = true;
         } catch (Throwable error) {
            load_Z = true;
            AutismClientAddon.LOG.error("[Privacy-compat] qualification failed", error);
         }
      }
   }
}

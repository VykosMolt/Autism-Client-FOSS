package autismclient.modules;

import autismclient.mixin.accessor.AutismWindowAccessor;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Screenshot;
import net.minecraft.client.server.IntegratedServer;
import net.minecraft.core.BlockPos;
import net.minecraft.network.protocol.game.ClientboundBlockUpdatePacket;
import net.minecraft.network.protocol.game.ClientboundForgetLevelChunkPacket;
import net.minecraft.network.protocol.game.ClientboundLevelChunkWithLightPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.AABB;

public final class H {
   private static final int init_I = 512;
   private static final int load_I = 150;
   private static final int read = 512;
   private static final int write = 64;
   private static final int update = 4096;
   private static int sync;
   private static int flush;
   private static boolean init_Z;
   private static boolean load_Z;
   private static Module init_Module;
   private static Module load_Module;
   private static CompletableFuture<?> init_CompletableFuture;
   private static CompletableFuture<ClientboundLevelChunkWithLightPacket> load_CompletableFuture;
   private static ClientboundBlockUpdatePacket init_ClientboundBlockUp;
   private static final JsonObject init_JsonObject = new JsonObject();
   private static final JsonArray init_JsonArray = new JsonArray();

   private H() {
   }

   public static void init() {
      if (Boolean.getBoolean("autism.esp.density.check")) {
         ClientTickEvents.END_CLIENT_TICK
            .register(
               mc -> {
                  if (!load_Z
                     && mc.player != null
                     && mc.level != null
                     && mc.getSingleplayerServer() != null
                     && mc.gameDirectory.toString().contains("autism-ai-qualification-")) {
                     try {
                        init(mc);
                     } catch (Throwable failure) {
                        init_JsonObject.addProperty("error", failure.toString());
                        load();
                     }
                  }
               }
            );
      }
   }

   private static void init(Minecraft mc) throws IOException {
      ((AutismWindowAccessor)(Object)mc.getWindow()).autism$setFocused(true);
      if (mc.gui.screen() != null) {
         mc.gui.setScreen(null);
      }

      if (++sync >= 40) {
         if (init_Z) {
            if (init_CompletableFuture != null) {
               if (!init_CompletableFuture.isDone()) {
                  return;
               }

               init_CompletableFuture.join();
               init_CompletableFuture = null;
            }

            flush++;
            mc.player.setYRot(0.0F);
            mc.player.setXRot(25.0F);
            if (flush == 100) {
               init("storage-4096-meshed", 4096, 0);
               init(mc, "storage-meshed.png");
            }

            if (flush == 110) {
               init_Module.setValueTransient("meshing", "false");
            }

            if (flush == 135) {
               init("storage-4096-boxes", 4096, 0);
               init(mc, "storage-boxes.png");
            }

            if (flush == 145) {
               init_Module.setValueTransient("meshing", "true");
               load_Module.setEnabled(true);
            }

            if (flush == 170) {
               init("storage-and-block-4096", 4096, 4096);
               init(mc, "both.png");
            }

            if (flush == 180) {
               init_ClientboundBlockUp = new ClientboundBlockUpdatePacket(new BlockPos(511, 150, 512), Blocks.CHEST.defaultBlockState());
               af.init_V_Packet(init_ClientboundBlockUp);
            }

            if (flush == 183) {
               mc.getConnection().handleBlockUpdate(init_ClientboundBlockUp);
            }

            if (flush == 190) {
               init("update-after-preapply-scan", 4097, 4097);
            }

            if (flush == 200) {
               ClientboundForgetLevelChunkPacket packet = new ClientboundForgetLevelChunkPacket(new ChunkPos(32, 32));
               af.init_V_Packet(packet);
               mc.getConnection().handleForgetLevelChunk(packet);
               IntegratedServer server = mc.getSingleplayerServer();
               load_CompletableFuture = server.submit(
                  () -> new ClientboundLevelChunkWithLightPacket(server.overworld().getChunk(32, 32), server.overworld().getLightEngine(), null, null)
               );
            }

            if (flush == 210) {
               init("chunk-unloaded", 3841, 3841);
            }

            if (flush == 220) {
               if (!load_CompletableFuture.isDone()) {
                  flush--;
                  return;
               }

               af.init_V_Packet(load_CompletableFuture.join());
            }

            if (flush == 223) {
               mc.getConnection().handleLevelChunkWithLight(load_CompletableFuture.join());
            }

            if (flush == 235) {
               init("chunk-applied-after-preapply-scan", 4097, 4097);
               init(mc, "reloaded.png");
            }

            if (flush == 245) {
               mc.level.setBlock(new BlockPos(510, 150, 512), Blocks.CHEST.defaultBlockState(), 19);
            }

            if (flush == 350) {
               init("local-change-safety-refresh", 4098, 4098);
               init(mc, "safety-refresh.png");
               load();
            }
         } else {
            init_Z = true;

            for (Module module : al.init_Collection()) {
               if (module.isEnabled()) {
                  module.setEnabled(false);
               }
            }

            init_Module = al.init_Module_Str("storage-esp");
            load_Module = al.init_Module_Str("block-esp");
            init_Module.setValueTransient("storage-list", "minecraft:chest");
            init_Module.setValueTransient("fill", "true");
            init_Module.setValueTransient("tracers", "true");
            init_Module.setValueTransient("meshing", "true");

            for (aq.Inner_DataUtil structure : aq.init) {
               init_Module.setValueTransient(structure.init_Str(), "false");
            }

            load_Module.setValueTransient("blocks", "minecraft:chest");
            load_Module.setValueTransient("max-targets", "8192");
            load_Module.setValueTransient("fill", "true");
            load_Module.setValueTransient("tracers", "true");
            load_Module.setValueTransient("meshing", "true");
            init_Module.setEnabled(true);
            IntegratedServer server = mc.getSingleplayerServer();
            UUID id = mc.player.getUUID();
            init_CompletableFuture = server.submit(() -> {
               ServerLevel level = server.overworld();

               for (int cx = 31; cx <= 36; cx++) {
                  for (int cz = 30; cz <= 36; cz++) {
                     level.getChunk(cx, cz);
                  }
               }

               for (BlockPos pos : BlockPos.betweenClosed(510, 149, 510, 577, 156, 577)) {
                  level.setBlock(pos, Blocks.AIR.defaultBlockState(), 2);
               }

               for (int x = 0; x < 64; x++) {
                  for (int z = 0; z < 64; z++) {
                     level.setBlock(new BlockPos(512 + x, 150, 512 + z), Blocks.CHEST.defaultBlockState(), 2);
                  }
               }

               ServerPlayer player = server.getPlayerList().getPlayer(id);
               player.setGameMode(GameType.SPECTATOR);
               player.teleportTo(544.5, 176.0, 484.5);
               player.setYRot(0.0F);
               player.setXRot(25.0F);
               return true;
            });
         }
      }
   }

   private static void init(String name, int storageMin, int blockMin) {
      JsonObject row = new JsonObject();
      row.addProperty("case", name);
      AABB region = new AABB(509.0, 150.0, 512.0, 576.0, 151.0, 576.0);
      int storageCount = ar.init_I_AABB(region);
      int blockCount = ar.load(region);
      row.addProperty("storage", storageCount);
      row.addProperty("block", blockCount);
      row.addProperty("storageTraces", ar.load_I());
      row.addProperty("blockTraces", ar.write_I());
      row.addProperty("passed", storageCount == storageMin && (blockMin == 0 || blockCount == blockMin));
      init_JsonArray.add(row);
      System.out.println("[ESP density] " + row);
   }

   private static void init(Minecraft mc, String name) throws IOException {
      Path directory = Path.of(System.getProperty("autism.esp.density.dir"));
      Files.createDirectories(directory);
      Screenshot.grab(directory.toFile(), name, mc.gameRenderer.mainRenderTarget(), 1, ignored -> {});
   }

   private static void load() {
      load_Z = true;
      if (init_Module != null) {
         init_Module.setEnabled(false);
      }

      if (load_Module != null) {
         load_Module.setEnabled(false);
      }

      boolean passed = !init_JsonObject.has("error") && init_JsonArray.size() == 7;

      for (JsonElement row : init_JsonArray) {
         passed &= row.getAsJsonObject().get("passed").getAsBoolean();
      }

      init_JsonObject.addProperty("passed", passed);
      init_JsonObject.add("checks", init_JsonArray);

      try {
         Files.writeString(
            Path.of(System.getProperty("autism.esp.density.dir"), "report.json"), new GsonBuilder().setPrettyPrinting().create().toJson(init_JsonObject)
         );
      } catch (Exception error) {
         throw new RuntimeException(error);
      }

      System.out.println("[ESP density] FINAL " + init_JsonObject);
   }
}

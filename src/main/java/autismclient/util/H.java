package autismclient.util;

import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.mojang.logging.LogUtils;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.ConnectScreen;
import net.minecraft.client.gui.screens.DisconnectedScreen;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.client.multiplayer.ServerData.Type;
import net.minecraft.client.multiplayer.resolver.ServerAddress;
import net.minecraft.core.BlockPos;
import net.minecraft.network.protocol.Packet;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;

public final class H {
   private static boolean init_Z;
   private static int init_I;
   private static int load_I;
   private static boolean load_Z;

   private H() {
   }

   public static void init() {
      String address = System.getProperty("autism.craft.check");
      if (address != null && !address.isBlank()) {
         ClientTickEvents.END_CLIENT_TICK.register(mc -> init(mc, address.trim()));
      }
   }

   private static void init(Minecraft mc, String address) {
      if (!load_Z) {
         if (mc.level == null) {
            if (init_Z && mc.gui.screen() instanceof DisconnectedScreen) {
               init_Z = false;
               init_I = 0;
            }

            if (!init_Z && mc.gui.screen() != null && ++init_I >= 100) {
               init_Z = true;
               ConnectScreen.startConnecting(
                  mc.gui.screen(), mc, ServerAddress.parseString(address), new ServerData("Craft check", address, Type.OTHER), false, null
               );
            }
         } else if (mc.player != null && mc.getConnection() != null && ++load_I >= 40 && init(mc, Items.OAK_LOG) >= 64 && init_Z(mc)) {
            load_Z = true;
            Thread worker = new Thread(() -> init_V(mc), "Autism-CraftCheck");
            worker.setDaemon(true);
            worker.start();
         }
      }
   }

   private static void init_V(Minecraft mc) {
      JsonObject report = new JsonObject();
      JsonArray steps = new JsonArray();
      int pass = 0;
      int fail = 0;

      try {
         init(1500L);
         report.add("pipeline", init(mc, () -> J.init_JsonObject(mc)));

         for (H.Inner_DataUtil step : List.of(
            new H.Inner_DataUtil("minecraft:oak_planks", 4, Items.OAK_PLANKS, 4),
            new H.Inner_DataUtil("minecraft:oak_planks", 100, Items.OAK_PLANKS, 100),
            new H.Inner_DataUtil("minecraft:stick", 8, Items.STICK, 8),
            new H.Inner_DataUtil("minecraft:crafting_table", 1, Items.CRAFTING_TABLE, 1),
            new H.Inner_DataUtil("minecraft:wooden_pickaxe", 1, Items.WOODEN_PICKAXE, 1),
            new H.Inner_DataUtil("minecraft:stone_pickaxe", 1, Items.STONE_PICKAXE, 1),
            new H.Inner_DataUtil("minecraft:furnace", 1, Items.FURNACE, 1),
            new H.Inner_DataUtil("minecraft:torch", 8, Items.TORCH, 8),
            new H.Inner_DataUtil("minecraft:iron_block", 1, Items.IRON_BLOCK, 1),
            new H.Inner_DataUtil("minecraft:chest", 1, Items.CHEST, 1)
         )) {
            JsonObject row = init(mc, step.init_Str(), step.init_I(), step.init_Item(), step.load());
            steps.add(row);
            if (row.get("pass").getAsBoolean()) {
               pass++;
            } else {
               fail++;
            }
         }

         JsonObject queued = init(mc, "minecraft:stick", 4, Items.STICK);
         steps.add(queued);
         if (queued.get("pass").getAsBoolean()) {
            pass++;
         } else {
            fail++;
         }

         for (String key : List.of("minecraft:stick", "minecraft:oak_planks")) {
            J.Inner_TaskQueue option = J.init(mc, key);
            int max = option == null ? 0 : option.sync;
            JsonObject row = init(mc, key, max, option == null ? Items.AIR : option.init_ItemStack.getItem(), max);
            row.addProperty("max", true);
            steps.add(row);
            if (max > 0 && row.get("pass").getAsBoolean()) {
               pass++;
            } else {
               fail++;
            }
         }
      } catch (Throwable t) {
         report.addProperty("error", t.toString());
         fail++;
      }

      report.add("steps", steps);
      report.addProperty("pass", pass);
      report.addProperty("fail", fail);
      report.addProperty("done", true);

      try {
         Path out = FabricLoader.getInstance().getGameDir().resolve("craft-check.json");
         Files.writeString(out, new GsonBuilder().setPrettyPrinting().create().toJson(report), StandardCharsets.UTF_8);
      } catch (Exception e) {
         LogUtils.getLogger().warn("[CraftCheck] report not written: {}", e.toString());
      }

      LogUtils.getLogger().info("[CraftCheck] DONE pass={} fail={}", pass, fail);
   }

   private static int init_I(Minecraft mc) {
      JsonObject pipeline = init(mc, () -> J.init_JsonObject(mc));
      return pipeline.has("syncedBookEntries") ? pipeline.get("syncedBookEntries").getAsInt() : -1;
   }

   private static JsonObject init(Minecraft mc, String recipe, int amount, Item item, int expected) {
      int book = init_I(mc);
      int before = init(mc, () -> init(mc, item));
      long started = System.nanoTime();
      J.Inner_CachePool result = J.init_J_CachePool(mc, recipe, -1, amount);
      long ms = (System.nanoTime() - started) / 1000000L;
      init(300L);
      int after = init(mc, () -> init(mc, item));
      boolean ok = result.init_Z && after - before == expected && result.init_I == expected;
      JsonObject row = new JsonObject();
      row.addProperty("recipe", recipe);
      row.addProperty("amount", amount);
      row.addProperty("expected", expected);
      row.addProperty("made", after - before);
      row.addProperty("reported", result.init_I);
      row.addProperty("ms", ms);
      row.addProperty("message", result.init_Str);
      row.addProperty("book", book);
      row.addProperty("pass", ok);
      LogUtils.getLogger()
         .info(
            "[CraftCheck] {} x{}: {} made {} (expected {}) in {} ms - {}", recipe, amount, ok ? "PASS" : "FAIL", after - before, expected, ms, result.init_Str
         );
      return row;
   }

   private static JsonObject init(Minecraft mc, String recipe, int amount, Item item) {
      int before = init(mc, () -> init(mc, item));
      long started = System.nanoTime();
      List<Packet<?>> packets = init(mc, () -> J.init_List(mc, recipe, -1, amount));
      init(mc, () -> {
         if (mc.getConnection() != null) {
            for (Packet<?> packet : packets) {
               mc.getConnection().send(packet);
            }
         }

         return null;
      });
      int after = before;

      for (int i = 0; i < 60 && after - before < amount; i++) {
         init(50L);
         after = init(mc, () -> init(mc, item));
      }

      init(300L);
      after = init(mc, () -> init(mc, item));
      boolean ok = !packets.isEmpty() && after - before == amount;
      JsonObject row = new JsonObject();
      row.addProperty("recipe", recipe + " (queue)");
      row.addProperty("amount", amount);
      row.addProperty("expected", amount);
      row.addProperty("made", after - before);
      row.addProperty("packets", packets.size());
      row.addProperty("ms", (System.nanoTime() - started) / 1000000L);
      row.addProperty("pass", ok);
      LogUtils.getLogger().info("[CraftCheck] {} queue x{}: {} made {} with {} packets", recipe, amount, ok ? "PASS" : "FAIL", after - before, packets.size());
      return row;
   }

   private static int init(Minecraft mc, Item item) {
      if (mc.player == null) {
         return 0;
      }

      int total = 0;

      for (int i = 0; i < 36; i++) {
         ItemStack stack = mc.player.getInventory().getItem(i);
         if (stack.is(item)) {
            total += stack.getCount();
         }
      }

      return total;
   }

   private static boolean init_Z(Minecraft mc) {
      if (mc.player != null && mc.level != null) {
         BlockPos origin = mc.player.blockPosition();

         for (BlockPos pos : BlockPos.betweenClosed(origin.offset(-3, -2, -3), origin.offset(3, 2, 3))) {
            if (mc.level.getBlockState(pos).is(Blocks.CRAFTING_TABLE)) {
               return true;
            }
         }

         return false;
      } else {
         return false;
      }
   }

   private static <T> T init(Minecraft mc, Supplier<T> task) {
      if (mc.isSameThread()) {
         return task.get();
      }

      CompletableFuture<T> future = new CompletableFuture<>();
      mc.execute(() -> {
         try {
            future.complete(task.get());
         } catch (Throwable t) {
            future.completeExceptionally(t);
         }
      });

      try {
         return future.get(10L, TimeUnit.SECONDS);
      } catch (Exception e) {
         throw new IllegalStateException("client task failed: " + e, e);
      }
   }

   private static void init(long ms) {
      try {
         Thread.sleep(ms);
      } catch (InterruptedException e) {
         Thread.currentThread().interrupt();
      }
   }

   private record Inner_DataUtil(String init_Str, int init_I, Item init_Item, int load) {
   }
}

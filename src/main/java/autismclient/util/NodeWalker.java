package autismclient.util;

import autismclient.mixin.accessor.AutismWindowAccessor;
import autismclient.modules.Module;
import autismclient.modules.j;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.Map.Entry;
import java.util.concurrent.CompletableFuture;
import java.util.stream.StreamSupport;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Screenshot;
import net.minecraft.client.server.IntegratedServer;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.protocol.game.ClientboundSetEntityMotionPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

public final class NodeWalker {
   private static int init_I;
   private static int load_I;
   private static int read_I;
   private static int write_I = Integer.getInteger("autism.bed.check.start", 0);
   private static boolean init_Z;
   private static boolean load_Z;
   private static boolean read_Z;
   private static CompletableFuture<?> init_CompletableFuture;
   private static final JsonArray init_JsonArray = new JsonArray();
   private static autismclient.modules.ListPool init_ListPool;
   private static int update_I;
   private static boolean write_Z;
   private static boolean update_Z;
   private static boolean sync_Z;
   private static boolean flush_Z;
   private static boolean close_Z;
   private static boolean open;
   private static int sync_I = -1;
   private static int flush_I = -1;
   private static final BlockPos init_BlockPos = new BlockPos(-1, 150, 0);
   private static final JsonArray load_JsonArray = new JsonArray();
   private static final String[] init_StrArr = new String[]{
      "own-white-bed", "nearest-bed-toggle", "layered-shell", "missing-layer-waits", "layered-without-bed-crouch", "guard-defend-combat"
   };
   private static final int close_I = Integer.getInteger("autism.bed.check.cases", init_StrArr.length);

   private NodeWalker() {
   }

   public static void init() {
      if (Boolean.getBoolean("autism.bed.check")) {
         ClientTickEvents.END_CLIENT_TICK
            .register(
               mc -> {
                  if (!load_Z
                     && mc.level != null
                     && mc.player != null
                     && mc.getSingleplayerServer() != null
                     && mc.gameDirectory.toPath().toString().contains("autism-ai-qualification-")) {
                     try {
                        init(mc);
                     } catch (Throwable error) {
                        init(mc, error.toString());
                     }
                  }
               }
            );
      }
   }

   private static void init(Minecraft mc) throws IOException, IllegalAccessException, NoSuchMethodException, NoSuchFieldException, InvocationTargetException {
      ((AutismWindowAccessor)(Object)mc.getWindow()).autism$setFocused(true);
      if (mc.gui.screen() != null) {
         mc.gui.setScreen(null);
      }

      if (!mc.mouseHandler.isMouseGrabbed()) {
         mc.mouseHandler.grabMouse();
      }

      for (KeyMapping key : List.of(
         mc.options.keyUp, mc.options.keyDown, mc.options.keyLeft, mc.options.keyRight, mc.options.keyJump, mc.options.keySprint, mc.options.keyShift
      )) {
         key.setDown(false);
      }

      if (++init_I >= 50) {
         if (!init_Z) {
            init_Z = true;

            for (Module m : autismclient.modules.al.init_Collection()) {
               if (m.isEnabled()) {
                  m.setEnabled(false);
               }
            }

            init_ListPool = (autismclient.modules.ListPool)autismclient.modules.al.init_Module_Str("bed-defender");
            read(mc);
         } else {
            if (init_CompletableFuture != null) {
               if (!init_CompletableFuture.isDone()) {
                  return;
               }

               init_CompletableFuture.join();
               init_CompletableFuture = null;
               read_I = 0;
            }

            read_I++;
            if (read_Z
               || mc.level.getBlockState(new BlockPos(0, 150, 0)).getBlock().getClass().getSimpleName().equals("BedBlock")
                  && !(mc.player.position().distanceToSqr(new Vec3(write_I < 2 ? -1.6 : -2.5, write_I < 2 ? 151.0 : 150.0, -0.5)) > 1.0)) {
               if (read_I >= 25) {
                  if (!read_Z) {
                     init_ListPool.setEnabled(write_I != 5);
                     read_Z = true;
                     load_I = 0;
                  } else {
                     load_I++;
                     if (load_I % 10 == 0 || load_I >= 26 && load_I <= 35) {
                        JsonObject row = new JsonObject();
                        row.addProperty("lane", write_I);
                        row.addProperty("tick", load_I);
                        row.addProperty("position", mc.player.position().toString());
                        row.addProperty("camera", mc.getCameraEntity().position().toString());
                        row.addProperty("dimension", mc.level.dimension().identifier().toString());
                        row.addProperty("info", init_ListPool.info());

                        try {
                           for (String key : List.of("cachedBed", "activeLayer", "targets", "cursor", "traceDetail", "visibilityWindows")) {
                              Field field = autismclient.modules.ListPool.class.getDeclaredField(key);
                              field.setAccessible(true);
                              row.addProperty(key, String.valueOf(field.get(init_ListPool)));
                           }

                           Method method = autismclient.modules.ListPool.class.getDeclaredMethod("close");
                           method.setAccessible(true);
                           row.addProperty("canPlan", (Boolean)method.invoke(init_ListPool));
                        } catch (ReflectiveOperationException renamed) {
                           row.addProperty("details", "Private diagnostics unavailable in obfuscated build");
                        }

                        if (write_I == 5) {
                           autismclient.modules.aa aura = (autismclient.modules.aa)autismclient.modules.al.init_Module_Str("kill-aura");
                           row.addProperty("slot", mc.player.getInventory().getSelectedSlot());
                           row.addProperty("item", mc.player.getMainHandItem().toString());
                           row.addProperty("auraEnabled", aura.isEnabled());
                           row.addProperty("auraTarget", String.valueOf(aura.init_LivingEntity_3()));
                           row.addProperty("guard", s.init_Str());
                           row.addProperty("using", mc.player.isUsingItem());
                           row.addProperty("owner", aD.init_Str());
                           row.addProperty("winding", aD.init_Z());
                           row.addProperty(
                              "targetPresent",
                              StreamSupport.stream(mc.level.entitiesForRendering().spliterator(), false)
                                 .anyMatch(entity -> entity.getType() == EntityTypes.PIG)
                           );

                           for (String name : List.of("canRun", "heldItemAllowedThisTick", "isUsingHeldItem", "throwableHeldThisTick")) {
                              Method method = aura.getClass().getDeclaredMethod(name);
                              method.setAccessible(true);
                              row.addProperty(name, String.valueOf(method.invoke(aura)));
                           }
                        }

                        load_JsonArray.add(row);
                     }

                     mc.player.setYRot(-90.0F);
                     mc.player.setXRot(25.0F);
                     if (load_I == 18) {
                        update_I = init_ListPool.init_List_5().size();
                        Path folder = Path.of(System.getProperty("autism.bed.check.dir"));
                        Files.createDirectories(folder);
                        Screenshot.grab(folder.toFile(), init_StrArr[write_I] + ".png", mc.gameRenderer.mainRenderTarget(), 1, ignored -> {});
                     }

                     if (load_I == 25) {
                        init(
                           mc,
                           List.of(
                              "give @s minecraft:end_stone 128",
                              "give @s minecraft:white_wool 128",
                              write_I == 3 ? "give @s minecraft:oak_planks 64" : "give @s minecraft:obsidian 128"
                           )
                        );
                     }

                     if (write_I < 2 && load_I == 100) {
                        init(mc, List.of("tp @s " + (write_I == 0 ? 2.5 : -5.5) + " 151 0.5"));
                     }

                     if (write_I < 2 && load_I == 150) {
                        init(mc, List.of("tp @s " + (write_I == 0 ? 0.5 : -3.5) + " 151 3.5"));
                     }

                     if (write_I < 2 && load_I == 190) {
                        init(mc, List.of("tp @s " + (write_I == 0 ? 0.5 : -3.5) + " 151 -2.5"));
                     }

                     if ((write_I == 2 || write_I == 4) && load_I >= 50 && load_I % 70 == 0) {
                        double[][] positions = new double[][]{
                           {-3.5, 0.5}, {-2.5, -2.5}, {0.5, -3.5}, {3.5, -2.5}, {4.5, 0.5}, {3.5, 3.5}, {0.5, 5.5}, {-2.5, 3.5}
                        };
                        double[][] ground = new double[][]{
                           {-2.5, 0.5}, {-1.5, -1.5}, {0.5, -2.5}, {2.5, -1.5}, {3.5, 0.5}, {2.5, 2.5}, {0.5, 4.5}, {-1.5, 2.5}
                        };
                        boolean elevated = load_I >= (write_I == 4 ? 560 : 280);
                        if (!elevated) {
                           positions = ground;
                        }

                        double[] p = positions[load_I / 70 % positions.length];
                        init(mc, List.of("tp @s " + p[0] + " " + (elevated ? 152 : 150) + " " + p[1]));
                     }

                     if (write_I == 5) {
                        load(mc);
                     }

                     int duration = write_I == 5 ? 220 : (write_I == 2 ? 1250 : (write_I == 4 ? 650 : (write_I == 3 ? 100 : 300)));
                     if (load_I >= duration) {
                        init_ListPool.setEnabled(false);
                        autismclient.modules.al.init_Module_Str("kill-aura").setEnabled(false);
                        int which = write_I;
                        CompletableFuture<JsonObject> report = mc.getSingleplayerServer().submit(() -> init(mc, which));
                        JsonObject row = report.join();
                        init_JsonArray.add(row);
                        System.out.println("[Bed check] " + row);
                        if (++write_I == close_I) {
                           init(mc, (String)null);
                        } else {
                           read(mc);
                        }
                     }
                  }
               }
            } else if (read_I > 600) {
               throw new IllegalStateException("Fixture teleport/chunks did not become ready: " + mc.player.position());
            }
         }
      }
   }

   private static void load(Minecraft mc) throws IllegalAccessException, NoSuchFieldException, NoSuchMethodException, InvocationTargetException {
      autismclient.modules.aa aura = (autismclient.modules.aa)autismclient.modules.al.init_Module_Str("kill-aura");
      if (!write_Z && load_I >= 30 && mc.player.getInventory().getItem(1).getItem() instanceof BlockItem) {
         j clutch = j.init_j();
         clutch.setEnabled(true);
         Method begin = clutch.getClass().getDeclaredMethod("begin", int.class);
         begin.setAccessible(true);
         begin.invoke(clutch, ck.init_ck().state_I());
         Method canPlan = init_ListPool.getClass().getDeclaredMethod("canPlan");
         canPlan.setAccessible(true);
         close_Z = j.init_Z_49() && !(Boolean)canPlan.invoke(init_ListPool);
         clutch.setEnabled(false);
         aura.setValueTransient("entities", "minecraft:pig");
         aura.setValueTransient("fov", "360");
         aura.setValueTransient("auto-block", "true");
         aura.setValueTransient("miss-chance", "0");
         aura.setEnabled(true);
         aw.init_V(mc, 0);
         InteractionHand hand = InteractionHand.OFF_HAND;
         ItemStack stack = mc.player.getOffhandItem();

         for (Entry<String, Object> pair : Map.of("autoBlockHand", hand, "autoBlockStack", stack, "autoBlockSlot", 0).entrySet()) {
            Field field = aura.getClass().getDeclaredField(pair.getKey());
            field.setAccessible(true);
            field.set(aura, pair.getValue());
         }

         if (!s.init("kill-aura", hand)) {
            throw new IllegalStateException("Guard could not be seeded");
         }

         mc.gameMode.useItem(mc.player, hand);
         update_Z = mc.player.isUsingItem() && autismclient.modules.aa.read_Z_14();
         write_Z = true;
         init_ListPool.setEnabled(true);
      }

      if (write_Z) {
         sync_Z = sync_Z | autismclient.modules.ListPool.init_Z_10();
         if (sync_Z && sync_I < 0 && !mc.level.getBlockState(init_BlockPos).isAir()) {
            sync_I = load_I;
         }

         if (sync_I >= 0 && !autismclient.modules.ListPool.init_Z_10() && mc.player.getInventory().getSelectedSlot() == 0 && aura.init_LivingEntity_3() != null
            )
          {
            if (flush_I < 0) {
               flush_I = load_I - sync_I;
            }

            flush_Z = true;
         }

         if (load_I == 110) {
            init(mc, List.of("setblock -1 150 0 minecraft:air"));
         }

         if (load_I > 120 && !mc.level.getBlockState(init_BlockPos).isAir()) {
            open = true;
         }
      }
   }

   private static void read(Minecraft mc) {
      read_Z = false;
      init_ListPool.setValueTransient("auto-bed", Boolean.toString(write_I != 1));
      init_ListPool.setValueTransient("max-layers", write_I == 5 ? "1" : (write_I == 4 ? "2" : (write_I >= 2 ? "3" : "1")));
      init_ListPool.setValueTransient("layer-materials", Boolean.toString(write_I >= 2 && write_I != 5));
      init_ListPool.setValueTransient("layer-1-blocks", "minecraft:obsidian");
      init_ListPool.setValueTransient("layer-2-blocks", "minecraft:end_stone");
      init_ListPool.setValueTransient("layer-3-blocks", "minecraft:white_wool");
      init_ListPool.setValueTransient("visualize", "true");
      init_ListPool.setValueTransient("ghost-opacity", "45");
      init_ListPool.setValueTransient("switch-back", "true");
      init_ListPool.setValueTransient("blocks", init_ListPool.setting("blocks").defaultValue());
      init_ListPool.setValueTransient("sneak-for-bed", Boolean.toString(write_I != 4));
      init_ListPool.setValueTransient("require-sneak", "false");
      ArrayList<String> setupCommands = new ArrayList<>(
         List.of(
            "gamemode survival @s",
            "clear @s",
            "time set 6000",
            "weather clear",
            "effect give @s minecraft:regeneration infinite 5 true",
            "fill -12 149 -12 12 149 12 minecraft:stone",
            "fill -12 150 -12 12 158 12 minecraft:air",
            "setblock 0 150 0 minecraft:white_bed[facing=south,part=foot]",
            "setblock 0 150 1 minecraft:white_bed[facing=south,part=head]",
            "setblock " + (write_I >= 2 ? -8 : -4) + " 150 0 minecraft:red_bed[facing=south,part=foot]",
            "setblock " + (write_I >= 2 ? -8 : -4) + " 150 1 minecraft:red_bed[facing=south,part=head]",
            "team add BedCheck",
            "team modify BedCheck color white",
            "team join BedCheck @s",
            write_I < 2 ? "tp @s -1.6 151 -0.5 -90 25" : "tp @s -2.5 150 -0.5 -90 25"
         )
      );
      if (write_I < 2) {
         setupCommands.addAll(
            List.of(
               "setblock -2 150 -1 minecraft:stone",
               "setblock " + (write_I == 0 ? 2 : -6) + " 150 0 minecraft:stone",
               "setblock " + (write_I == 0 ? 0 : -4) + " 150 3 minecraft:stone",
               "setblock " + (write_I == 0 ? 0 : -4) + " 150 -3 minecraft:stone"
            )
         );
      }

      if (write_I == 2 || write_I == 4) {
         setupCommands.addAll(
            List.of(
               "fill -4 150 0 -4 151 0 minecraft:stone",
               "fill 4 150 0 4 151 0 minecraft:stone",
               "fill 0 150 -4 0 151 -4 minecraft:stone",
               "fill 0 150 5 0 151 5 minecraft:stone",
               "fill -3 150 -3 -3 151 -3 minecraft:stone",
               "fill 3 150 -3 3 151 -3 minecraft:stone",
               "fill 3 150 3 3 151 3 minecraft:stone",
               "fill -3 150 3 -3 151 3 minecraft:stone"
            )
         );
      }

      if (write_I == 5) {
         setupCommands.addAll(
            List.of(
               "give @s minecraft:diamond_sword",
               "item replace entity @s weapon.offhand with minecraft:shield",
               "kill @e[type=minecraft:pig]",
               "summon minecraft:pig -3.7 150 -1.8 {NoAI:1b,PersistenceRequired:1b,Invulnerable:1b}"
            )
         );

         for (int x = -1; x <= 1; x++) {
            for (int y = 0; y <= 1; y++) {
               for (int z = -1; z <= 2; z++) {
                  if (Math.abs(x) + Math.max(0, Math.max(-z, z - 1)) + y == 1 && (x != -1 || y != 0 || z != 0)) {
                     setupCommands.add("setblock " + x + " " + (150 + y) + " " + z + " minecraft:obsidian");
                  }
               }
            }
         }
      }

      init_CompletableFuture = init(mc, setupCommands);
   }

   private static JsonObject init(Minecraft mc, int which) {
      ServerLevel level = mc.getSingleplayerServer().overworld();
      int layers = which == 5 ? 1 : (which == 4 ? 2 : (which >= 2 ? 3 : 1));
      int origin = which == 1 ? -4 : 0;
      int missing = 0;
      int wrong = 0;
      int placed = 0;

      for (int x = -layers; x <= layers; x++) {
         for (int y = 0; y <= layers; y++) {
            for (int z = -layers; z <= layers + 1; z++) {
               int layer = Math.abs(x) + Math.max(0, Math.max(-z, z - 1)) + y;
               if (layer >= 1 && layer <= layers) {
                  BlockState state = level.getBlockState(new BlockPos(origin + x, 150 + y, z));
                  if (state.isAir()) {
                     missing++;
                  } else {
                     placed++;
                     String expected = layer == 1 ? "minecraft:obsidian" : (layer == 2 ? "minecraft:end_stone" : "minecraft:white_wool");
                     if ((which == 2 || which == 4) && !BuiltInRegistries.BLOCK.getKey(state.getBlock()).toString().equals(expected)) {
                        wrong++;
                     }
                  }
               }
            }
         }
      }

      boolean enemyTouched = false;
      int other = which >= 2 ? -8 : (origin == 0 ? -4 : 0);

      for (int x = -1; x <= 1; x++) {
         for (int y = 0; y <= 1; y++) {
            for (int z = -1; z <= 2; z++) {
               int distance = Math.abs(x) + Math.max(0, Math.max(-z, z - 1)) + y;
               if (distance == 1 && !level.getBlockState(new BlockPos(other + x, 150 + y, z)).isAir()) {
                  enemyTouched = true;
               }
            }
         }
      }

      JsonObject row = new JsonObject();
      row.addProperty("case", init_StrArr[which]);
      row.addProperty("ghosts", update_I);
      row.addProperty("placed", placed);
      row.addProperty("missing", missing);
      row.addProperty("wrongMaterial", wrong);
      row.addProperty("otherBedTouched", enemyTouched);
      List<String> defaults = Arrays.asList(init_ListPool.value("blocks").split("\\|"));
      boolean palette = new HashSet<>(defaults).size() == 19 && !defaults.contains("minecraft:ender_chest");
      row.addProperty("defaultPalette", palette);
      if (which == 5) {
         row.addProperty("guardSeen", update_Z);
         row.addProperty("defenceSeen", sync_Z);
         row.addProperty("combatReturned", flush_Z);
         row.addProperty("returnDelay", flush_I);
         row.addProperty("clutchYield", close_Z);
         row.addProperty("repairSeen", open);
      }

      row.addProperty(
         "passed",
         (which != 5 || update_Z && sync_Z && flush_Z && flush_I <= 1 && close_Z && open)
            && palette
            && (which == 5 || update_I > 0)
            && (which == 1 || !enemyTouched)
            && (which == 3 ? placed == 0 : missing == 0 && wrong == 0)
      );
      return row;
   }

   private static CompletableFuture<?> init(Minecraft mc, List<String> commands) {
      IntegratedServer server = mc.getSingleplayerServer();
      UUID id = mc.player.getUUID();
      return server.submit(() -> {
         ServerPlayer player = server.getPlayerList().getPlayer(id);
         CommandSourceStack source = server.createCommandSourceStack().withEntity(player).withSuppressedOutput();
         ServerLevel level = player.level();
         if (commands.stream().anyMatch(c -> c.startsWith("fill -12"))) {
            for (int x = -1; x <= 0; x++) {
               for (int z = -1; z <= 0; z++) {
                  level.getChunk(x, z);
               }
            }
         }

         for (String command : commands) {
            if (command.startsWith("fill -12")) {
               int low = command.contains(" 149 ") ? 149 : 150;
               int high = low == 149 ? 149 : 158;
               BlockState state = low == 149 ? Blocks.STONE.defaultBlockState() : Blocks.AIR.defaultBlockState();

               for (BlockPos p : BlockPos.betweenClosed(-12, low, -12, 12, high, 12)) {
                  level.setBlock(p, state, 2);
               }
            } else {
               server.getCommands().performPrefixedCommand(source, command);
            }
         }

         if (commands.stream().anyMatch(c -> c.startsWith("tp @s"))) {
            player.setDeltaMovement(Vec3.ZERO);
            player.connection.send(new ClientboundSetEntityMotionPacket(player.getId(), Vec3.ZERO));
         }

         return true;
      });
   }

   private static void init(Minecraft mc, String error) {
      load_Z = true;
      if (init_ListPool != null) {
         init_ListPool.setEnabled(false);
      }

      boolean pass = error == null && init_JsonArray.size() == close_I - Integer.getInteger("autism.bed.check.start", 0);

      for (JsonElement r : init_JsonArray) {
         pass &= r.getAsJsonObject().get("passed").getAsBoolean();
      }

      JsonObject report = new JsonObject();
      report.addProperty("passed", pass);
      report.add("cases", init_JsonArray);
      report.add("trace", load_JsonArray);
      if (error != null) {
         report.addProperty("error", error);
      }

      try {
         Files.writeString(Path.of(System.getProperty("autism.bed.check.dir"), "report.json"), new GsonBuilder().setPrettyPrinting().create().toJson(report));
      } catch (Exception e) {
         throw new RuntimeException(e);
      }

      System.out.println("[Bed check] FINAL: " + (pass ? "PASS" : "FAIL"));
   }
}

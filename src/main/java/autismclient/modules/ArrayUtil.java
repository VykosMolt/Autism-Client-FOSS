package autismclient.modules;

import autismclient.util.ck;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Screenshot;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.entity.player.Input;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.ClipContext.Block;
import net.minecraft.world.level.ClipContext.Fluid;
import net.minecraft.world.level.block.BedBlock;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.state.properties.BedPart;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.HitResult.Type;

public final class ArrayUtil {
   private static int init_I;
   private static int load_I;
   private static boolean init_Z;
   private static CompletableFuture<?> init_CompletableFuture;
   private static final JsonObject init_JsonObject = new JsonObject();
   private static final BlockPos init_BlockPos = new BlockPos(-6, 150, 0);
   private static final BlockPos load_BlockPos = new BlockPos(-1, 150, 0);
   private static final BlockPos read = new BlockPos(1, 150, 0);
   private static ClassCache init_ClassCache;
   private static ListPool init_ListPool;
   private static Path init_Path;
   private static ClassCache.Inner_DataUtil init_ClassCache_DataUti;

   private ArrayUtil() {
   }

   public static void init() {
      if (Boolean.getBoolean("autism.bedesp.check")) {
         init_Path = Path.of(System.getProperty("autism.bedesp.check.dir"));
         ClientTickEvents.END_CLIENT_TICK
            .register(
               mc -> {
                  if (!init_Z
                     && mc.level != null
                     && mc.player != null
                     && mc.getSingleplayerServer() != null
                     && mc.gameDirectory.toPath().toString().contains("autism-ai-qualification-")) {
                     try {
                        init(mc);
                     } catch (Throwable error) {
                        init_JsonObject.addProperty("error", error.toString());
                        load();
                     }
                  }
               }
            );
      }
   }

   private static void init(Minecraft mc) throws IOException, IllegalAccessException, NoSuchMethodException, InvocationTargetException {
      if (++load_I >= 35) {
         if (init_I == 0) {
            for (Module module : al.init_Collection()) {
               if (module.isEnabled()) {
                  module.setEnabled(false);
               }
            }

            init_ClassCache = (ClassCache)al.init_Module_Str("bed-esp");
            init_ListPool = (ListPool)al.init_Module_Str("bed-defender");
            init_CompletableFuture = init(
               mc,
               player -> {
                  ServerLevel level = player.level();

                  for (int x = -1; x <= 0; x++) {
                     for (int z = -1; z <= 0; z++) {
                        level.getChunk(x, z);
                     }
                  }

                  for (BlockPos p : BlockPos.betweenClosed(-14, 149, -14, 14, 160, 14)) {
                     level.setBlock(p, p.getY() == 149 ? Blocks.STONE.defaultBlockState() : Blocks.AIR.defaultBlockState(), 2);
                  }

                  init(level, new BlockPos(0, 150, 0), init("white_bed"));
                  init(level, init_BlockPos, init("red_bed"));
                  load(level, new BlockPos(0, 150, 0), Blocks.OBSIDIAN);
                  load(level, init_BlockPos, Blocks.OAK_PLANKS);
                  level.setBlock(new BlockPos(-4, 150, -4), Blocks.GOLD_BLOCK.defaultBlockState(), 2);
                  CommandSourceStack source = mc.getSingleplayerServer().createCommandSourceStack().withEntity(player).withSuppressedOutput();

                  for (String c : List.of(
                     "team add bedqa",
                     "team modify bedqa color white",
                     "team join bedqa @s",
                     "tp @s -5.5 150 -7.5 0 15",
                     "clear @s",
                     "give @s wooden_axe 1",
                     "give @s white_wool 64"
                  )) {
                     mc.getSingleplayerServer().getCommands().performPrefixedCommand(source, c);
                  }

                  player.getInventory().setSelectedSlot(8);
                  player.setGameMode(GameType.SURVIVAL);
               }
            );
            init_I++;
         } else {
            if (init_CompletableFuture != null) {
               if (!init_CompletableFuture.isDone()) {
                  return;
               }

               init_CompletableFuture.join();
               init_CompletableFuture = null;
            }

            init_I++;
            if (init_I == 25) {
               mc.player.getInventory().setSelectedSlot(8);
               init_ClassCache.setValueTransient("own-bed", "false");
               init_ClassCache.setValueTransient("my-tools", "true");
               init_ClassCache.setEnabled(true);
               Module blocks = al.init_Module_Str("block-esp");
               blocks.setValueTransient("blocks", "minecraft:white_bed|minecraft:red_bed|minecraft:gold_block");
               blocks.setEnabled(true);
            }

            if (init_I == 60) {
               List<ClassCache.Inner_DataUtil> beds = init_ClassCache.init_List_2();
               init_JsonObject.addProperty("oneLayerNoHighlights", beds.stream().noneMatch(ClassCache.Inner_DataUtil::init_Z));
               init_JsonObject.addProperty("ownHidden", beds.size() == 1 && beds.getFirst().init_DyeColor() == DyeColor.RED);
               init_JsonObject.addProperty(
                  "coverCount", beds.isEmpty() ? -1 : beds.getFirst().init_List().stream().mapToInt(ClassCache.Inner_StreamUtil::init_I).sum()
               );
               init_JsonObject.addProperty(
                  "axeShown", !beds.isEmpty() && beds.getFirst().init_List().stream().anyMatch(m -> m.init_ItemStack().is(ItemTags.AXES))
               );
               init_JsonObject.addProperty(
                  "diamondAxeIcon",
                  !beds.isEmpty() && beds.getFirst().init_List().stream().anyMatch(m -> NodeWalker.init(m.init_ItemStack()) == Items.DIAMOND_AXE)
               );
               init_JsonObject.addProperty("fillAlpha", NodeWalker.init(DyeColor.WHITE.getTextureDiffuseColor(), 32) >>> 24);
               init_JsonObject.addProperty("hotbarTicks", beds.isEmpty() ? -1.0 : beds.getFirst().init_D());
               init_JsonObject.addProperty("unheldHotbarTool", mc.player.getMainHandItem().isEmpty() && !beds.isEmpty() && beds.getFirst().init_D() == 30.0);
               int[] counts = new int[]{0, 0};
               ad.init(
                  al.init_Module_Str("block-esp"),
                  mc.level,
                  mc.player,
                  (box, color) -> {
                     if (!box.intersects(new AABB(init_BlockPos).minmax(new AABB(init_BlockPos.south())))
                        && !box.intersects(new AABB(0.0, 150.0, 0.0, 1.0, 151.0, 2.0))) {
                        counts[1]++;
                     } else {
                        counts[0]++;
                     }
                  },
                  null
               );
               init_JsonObject.addProperty("noDoubleBeds", counts[0] == 0 && counts[1] >= 1);
               init_JsonObject.addProperty("blockEspBeds", counts[0]);
               init_JsonObject.addProperty("blockEspOther", counts[1]);
               init(mc, "bedesp-far.png");
               init_ClassCache.setValueTransient("my-tools", "false");
            }

            if (init_I == 90) {
               List<ClassCache.Inner_DataUtil> beds = init_ClassCache.init_List_2();
               double fastest = beds.isEmpty() ? 1.0 / 0.0 : beds.getFirst().init_D();
               init_JsonObject.addProperty("idealRouteTicks", fastest);
               init_JsonObject.addProperty("routeModeKeepsHotbarTime", fastest == init_JsonObject.get("hotbarTicks").getAsDouble());
               mc.player.getInventory().setSelectedSlot(0);
               init_CompletableFuture = init(mc, p -> p.getInventory().setSelectedSlot(0));
            }

            if (init_I == 100) {
               init_JsonObject.addProperty(
                  "heldSlotKeepsTime",
                  init_ClassCache.init_List_2()
                     .stream()
                     .anyMatch(b -> b.init_BlockPos().equals(init_BlockPos) && b.init_D() == init_JsonObject.get("hotbarTicks").getAsDouble())
               );
               PlayerInfo info = mc.getConnection().getPlayerInfo(mc.player.getUUID());
               info.setTabListDisplayName(
                  Component.literal("[MVP] ")
                     .withStyle(ChatFormatting.GOLD)
                     .append(Component.literal(mc.player.getGameProfile().name()).withStyle(ChatFormatting.RED))
               );
            }

            if (init_I == 110) {
               init_JsonObject.addProperty(
                  "nameOverridesTeam",
                  init_ClassCache.init_List_2().size() == 1
                     && init_ClassCache.init_List_2().getFirst().init_DyeColor() == DyeColor.WHITE
                     && b.init() == DyeColor.RED
               );
               init(mc, "name-colour-beats-team.png");
               mc.getConnection().getPlayerInfo(mc.player.getUUID()).setTabListDisplayName(null);
            }

            if (init_I == 115) {
               init_JsonObject.addProperty(
                  "teamChangeRechecksOwner",
                  init_ClassCache.init_List_2().size() == 1 && init_ClassCache.init_List_2().getFirst().init_DyeColor() == DyeColor.RED
               );
               init_ClassCache.setValueTransient("own-bed", "true");
            }

            if (init_I == 120) {
               init_JsonObject.addProperty("ownToggle", init_ClassCache.init_List_2().size() == 2);
               init_CompletableFuture = init(mc, p -> p.teleportTo(0.5, 150.0, -2.5));
            }

            if (init_I == 150) {
               init(mc, "bedesp-near.png");
               init_ListPool.setValueTransient("auto-bed", "true");
               init_ListPool.setValueTransient("max-layers", "3");
               init_ListPool.setValueTransient("layer-materials", "true");
               init_ListPool.setValueTransient("layer-1-blocks", "minecraft:obsidian");
               init_ListPool.setValueTransient("layer-2-blocks", "minecraft:end_stone");
               init_ListPool.setValueTransient("layer-3-blocks", "minecraft:white_wool");
               init_ListPool.setEnabled(true);
            }

            if (init_I == 180) {
               init_CompletableFuture = init(mc, p -> p.level().setBlock(load_BlockPos, Blocks.AIR.defaultBlockState(), 3));
            }

            if (init_I == 240) {
               init_JsonObject.addProperty("enemyHoleRepaired", mc.level.getBlockState(load_BlockPos).is(init("white_wool")));
               init_JsonObject.addProperty("newLayerWaits", mc.level.getBlockState(new BlockPos(0, 152, 0)).isAir());
               init(mc, "defender-near.png");
               init_CompletableFuture = init(mc, p -> {
                  p.setGameMode(GameType.CREATIVE);
                  p.teleportTo(2.5, 150.0, -0.5);
               });
            }

            if (init_I == 260) {
               mc.gameMode.startDestroyBlock(read, Direction.UP);
            }

            if (init_I == 300) {
               init_JsonObject.addProperty("ownHoleLeftOpen", mc.level.getBlockState(read).isAir());
               init_CompletableFuture = init(mc, p -> p.teleportTo(0.5, 150.0, -7.5));
            }

            if (init_I == 330) {
               init(mc, "defender-far.png");
               init_ListPool.setEnabled(false);
               init_ClassCache.setValueTransient("own-bed", "false");
               init_ClassCache.setValueTransient("my-tools", "true");
               init_CompletableFuture = init(mc, p -> {
                  p.setGameMode(GameType.SURVIVAL);
                  p.getInventory().clearContent();
                  load(p.level(), init_BlockPos, Blocks.END_STONE);
                  p.teleportTo(-5.5, 150.0, -7.5);
                  p.setYRot(0.0F);
                  p.setXRot(15.0F);
                  CommandSourceStack source = mc.getSingleplayerServer().createCommandSourceStack().withEntity(p).withSuppressedOutput();
                  mc.getSingleplayerServer().getCommands().performPrefixedCommand(source, "time set 6000");
               });
            }

            if (init_I == 370) {
               ClassCache.Inner_DataUtil bed = init_ClassCache.init_List_2()
                  .stream()
                  .filter(b -> b.init_BlockPos().equals(init_BlockPos))
                  .findFirst()
                  .orElseThrow();
               init_JsonObject.addProperty("endStoneByHand", bed.init_Str().isEmpty() && Double.isFinite(bed.init_D()) && bed.init_D() > 0.0);
               init_JsonObject.addProperty("handTicks", bed.init_D());
               init_JsonObject.addProperty(
                  "pickaxeShown", bed.init_List().stream().anyMatch(m -> m.init_Block() == Blocks.END_STONE && m.init_ItemStack().is(ItemTags.PICKAXES))
               );
               init_JsonObject.addProperty(
                  "diamondPickIcon",
                  bed.init_List().stream().anyMatch(m -> m.init_Block() == Blocks.END_STONE && NodeWalker.init(m.init_ItemStack()) == Items.DIAMOND_PICKAXE)
               );
               init(mc, "end-stone-hand.png");
               init_CompletableFuture = init(mc, p -> {
                  ItemStack pick = new ItemStack(Items.NETHERITE_PICKAXE);
                  pick.enchant(p.level().registryAccess().lookupOrThrow(Registries.ENCHANTMENT).getOrThrow(Enchantments.EFFICIENCY), 5);
                  p.getInventory().setItem(8, pick);
               });
            }

            if (init_I == 390) {
               ClassCache.Inner_DataUtil bed = init_ClassCache.init_List_2()
                  .stream()
                  .filter(b -> b.init_BlockPos().equals(init_BlockPos))
                  .findFirst()
                  .orElseThrow();
               init_JsonObject.addProperty("unheldEnchantedPickTicks", bed.init_D());
               init_JsonObject.addProperty("unheldEnchantedPick", mc.player.getMainHandItem().isEmpty() && bed.init_D() == 3.0);
               init(mc, "end-stone-hotbar-pick.png");
               init_CompletableFuture = init(mc, p -> {
                  ItemStack pick = p.getInventory().getItem(8);
                  p.getInventory().setItem(10, pick);
                  p.getInventory().setItem(8, ItemStack.EMPTY);
               });
            }

            if (init_I == 400) {
               ClassCache.Inner_DataUtil bed = init_ClassCache.init_List_2()
                  .stream()
                  .filter(b -> b.init_BlockPos().equals(init_BlockPos))
                  .findFirst()
                  .orElseThrow();
               init_JsonObject.addProperty("backpackToolIgnored", bed.init_D() == init_JsonObject.get("handTicks").getAsDouble());
               init_CompletableFuture = init(mc, p -> load(p.level(), init_BlockPos, init("white_wool")));
            }

            if (init_I == 410) {
               ClassCache.Inner_DataUtil bed = init_ClassCache.init_List_2()
                  .stream()
                  .filter(b -> b.init_BlockPos().equals(init_BlockPos))
                  .findFirst()
                  .orElseThrow();
               init_JsonObject.addProperty("shearsShownWithoutTool", bed.init_List().stream().anyMatch(m -> m.init_ItemStack().is(Items.SHEARS)));
               init_JsonObject.addProperty("woolHandTicks", bed.init_D());
               init(mc, "wool-hand.png");
               init_CompletableFuture = init(mc, p -> {
                  for (BlockPos at : BlockPos.betweenClosed(-7, 149, -32, -4, 149, -14)) {
                     p.level().setBlock(at, Blocks.STONE.defaultBlockState(), 2);
                  }

                  p.teleportTo(-5.5, 150.0, -29.5);
                  p.setYRot(0.0F);
                  p.setXRot(5.0F);
               });
            }

            if (init_I == 450) {
               init(mc, "wool-far.png");
            }

            if (init_I == 460) {
               mc.options.keyUp.setDown(true);
            }

            if (init_I == 475 || init_I == 480 || init_I == 485) {
               init(mc, "wool-walk-" + init_I + ".png");
            }

            if (init_I == 490) {
               mc.options.keyUp.setDown(false);
            }

            if (init_I == 520) {
               init_CompletableFuture = init(mc, p -> {
                  init(p.level(), new BlockPos(6, 150, 4), init("gray_bed"));
                  load(p.level(), new BlockPos(6, 150, 4), Blocks.OAK_PLANKS);
                  p.teleportTo(6.5, 150.0, -3.5);
                  p.setYRot(0.0F);
                  p.setXRot(12.0F);
               });
            }

            if (init_I == 550) {
               double minimum = 255.0;

               for (DyeColor dye : DyeColor.values()) {
                  int rgb = NodeWalker.init(dye.getTextureDiffuseColor());
                  minimum = Math.min(minimum, 0.2126 * (rgb >> 16 & 0xFF) + 0.7152 * (rgb >> 8 & 0xFF) + 0.0722 * (rgb & 0xFF));
               }

               init_JsonObject.addProperty("minimumLabelBrightness", minimum);
               init_JsonObject.addProperty("readableBedColours", minimum >= 179.5);
               init(mc, "gray-bed-item-label.png");
               init_ClassCache.setEnabled(false);
            }

            if (init_I >= 560 && init_I < 800) {
               init(mc, (init_I - 560) / 40, (init_I - 560) % 40);
            }

            if (init_I == 800) {
               j.init_j().setEnabled(false);
               init_ClassCache.setEnabled(true);
               init_ClassCache.setValueTransient("own-bed", "false");
               init_CompletableFuture = init(mc, p -> {
                  for (BlockPos at : BlockPos.betweenClosed(-10, 150, -5, -2, 155, 6)) {
                     p.level().setBlock(at, Blocks.AIR.defaultBlockState(), 2);
                  }

                  init(p.level(), init_BlockPos, init("red_bed"));

                  for (int x = -3; x <= 3; x++) {
                     for (int z = -3; z <= 4; z++) {
                        for (int y = 0; y <= 3; y++) {
                           int d = Math.abs(x) + Math.max(0, Math.max(-z, z - 1)) + y;
                           if (d >= 1 && d <= 3) {
                              p.level().setBlock(init_BlockPos.offset(x, y, z), init("white_wool").defaultBlockState(), 2);
                           }
                        }
                     }
                  }

                  p.teleportTo(-5.5, 150.0, -7.5);
                  p.setYRot(0.0F);
                  p.setXRot(15.0F);
               });
            }

            if (init_I == 830) {
               init_ClassCache_DataUti = init_ClassCache.init_List_2().stream().filter(b -> b.init_BlockPos().equals(init_BlockPos)).findFirst().orElseThrow();
               init_JsonObject.addProperty("multiLayerHighlights", init_ClassCache_DataUti.init_Z() && init_ClassCache_DataUti.load_List().size() >= 3);
               init_JsonObject.addProperty("multiLayerBreaks", init_ClassCache_DataUti.load_List().size());
               init(mc, "three-layer-path.png");
               init_CompletableFuture = init(mc, p -> {
                  for (BlockPos at : init_ClassCache_DataUti.load_List()) {
                     p.level().setBlock(at, Blocks.AIR.defaultBlockState(), 3);
                  }

                  BlockPos feetx = init_ClassCache_DataUti.write().getLast();
                  p.teleportTo(feetx.getX() + 0.5, feetx.getY(), feetx.getZ() + 0.5);
               });
            }

            if (init_I == 850) {
               BlockHitResult hit = mc.level
                  .clip(new ClipContext(init_ClassCache_DataUti.load_Vec3(), init_ClassCache_DataUti.read_Vec3(), Block.OUTLINE, Fluid.NONE, mc.player));
               init_JsonObject.addProperty(
                  "realBedRay", hit.getType() == Type.BLOCK && mc.level.getBlockState(hit.getBlockPos()).getBlock() instanceof BedBlock
               );
               BlockPos feet = init_ClassCache_DataUti.write().getLast();
               init_JsonObject.addProperty(
                  "realPlayerClearance",
                  mc.level
                     .noCollision(
                        mc.player, new AABB(feet.getX() + 0.2, feet.getY(), feet.getZ() + 0.2, feet.getX() + 0.8, feet.getY() + 1.8, feet.getZ() + 0.8)
                     )
               );
               init(mc, "three-layer-cleared.png");
            }

            if (init_I == 870) {
               load();
            }
         }
      }
   }

   private static void init(Minecraft mc, int index, int phase) throws IllegalAccessException, NoSuchMethodException, IOException, InvocationTargetException {
      j clutch = j.init_j();
      if (phase == 0) {
         clutch.setEnabled(false);
         init_CompletableFuture = init(mc, p -> {
            ServerLevel level = p.level();

            for (BlockPos at : BlockPos.betweenClosed(7, 150, -1, 10, 154, 1)) {
               level.setBlock(at, Blocks.AIR.defaultBlockState(), 2);
            }

            double x = 8.5;
            double z = 0.5;
            double y = 150.5;
            if (index < 4) {
               Direction direction = Direction.from2DDataValue(index);
               level.setBlock(new BlockPos(8, 150, 0), Blocks.OAK_STAIRS.defaultBlockState().setValue(StairBlock.FACING, direction), 3);
               x -= direction.getStepX() * 0.35;
               z -= direction.getStepZ() * 0.35;
            } else if (index == 4) {
               level.setBlock(new BlockPos(8, 150, 0), Blocks.OAK_SLAB.defaultBlockState(), 3);
            } else {
               level.setBlock(new BlockPos(8, 150, 0), Blocks.STONE.defaultBlockState(), 3);
               x = 9.25;
               y = 151.0;
            }

            p.teleportTo(x, y, z);
            p.setDeltaMovement(Vec3.ZERO);
         });
      }

      if (phase == 20) {
         if (!mc.player.onGround()) {
            throw new IllegalStateException("Recovery fixture is not grounded: " + index + " " + mc.player.position());
         }

         if (Math.abs(mc.player.getY() - (index < 5 ? 150.5 : 151.0)) > 0.01) {
            throw new IllegalStateException("Recovery fixture height: " + index + " " + mc.player.position());
         }

         init_JsonObject.addProperty("recovery-height-" + index, mc.player.getY());
         clutch.setValueTransient("own-knockback", "false");
         clutch.setValueTransient("guard-time", "0");
         clutch.setValueTransient("steer", "true");
         clutch.setEnabled(true);
         Method begin = j.class.getDeclaredMethod(System.getProperty("autism.bedesp.clutch.begin", "begin"), int.class, boolean.class);
         begin.setAccessible(true);
         begin.invoke(clutch, ck.init_ck().state_I(), true);
      }

      if (phase == 21) {
         Input input = new Input(true, false, false, false, false, false, true);
         boolean released = !j.read_Z_20() && !j.init_Z_49() && j.init_Input_Input_3(input).equals(input);
         init_JsonObject.addProperty("recovery-" + index, released);
         init_JsonObject.addProperty("recovery-status-" + index, clutch.init_Str_16());
         init(mc, "recovery-" + index + ".png");
      }

      if (phase == 22) {
         clutch.setEnabled(false);
      }
   }

   private static void init(ServerLevel level, BlockPos pos, net.minecraft.world.level.block.Block block) {
      level.setBlock(pos, block.defaultBlockState().setValue(BedBlock.FACING, Direction.SOUTH).setValue(BedBlock.PART, BedPart.FOOT), 2);
      level.setBlock(pos.south(), block.defaultBlockState().setValue(BedBlock.FACING, Direction.SOUTH).setValue(BedBlock.PART, BedPart.HEAD), 2);
   }

   private static net.minecraft.world.level.block.Block init(String id) {
      return BuiltInRegistries.BLOCK.getOptional(Identifier.withDefaultNamespace(id)).orElseThrow();
   }

   private static void load(ServerLevel level, BlockPos bed, net.minecraft.world.level.block.Block block) {
      for (int x = -1; x <= 1; x++) {
         for (int y = 0; y <= 1; y++) {
            for (int z = -1; z <= 2; z++) {
               if (Math.abs(x) + Math.max(0, Math.max(-z, z - 1)) + y == 1) {
                  level.setBlock(bed.offset(x, y, z), block.defaultBlockState(), 2);
               }
            }
         }
      }
   }

   private static CompletableFuture<?> init(Minecraft mc, Consumer<ServerPlayer> action) {
      UUID id = mc.player.getUUID();
      return mc.getSingleplayerServer().submit(() -> {
         action.accept(mc.getSingleplayerServer().getPlayerList().getPlayer(id));
         return true;
      });
   }

   private static void init(Minecraft mc, String name) throws IOException {
      Files.createDirectories(init_Path);
      Screenshot.grab(init_Path.toFile(), name, mc.gameRenderer.mainRenderTarget(), 1, ignored -> {});
   }

   private static void load() {
      init_Z = true;
      boolean passed = !init_JsonObject.has("error");

      for (String key : List.of(
         "oneLayerNoHighlights",
         "multiLayerHighlights",
         "realBedRay",
         "realPlayerClearance",
         "ownHidden",
         "axeShown",
         "noDoubleBeds",
         "routeModeKeepsHotbarTime",
         "unheldHotbarTool",
         "heldSlotKeepsTime",
         "nameOverridesTeam",
         "teamChangeRechecksOwner",
         "ownToggle",
         "enemyHoleRepaired",
         "newLayerWaits",
         "ownHoleLeftOpen",
         "endStoneByHand",
         "pickaxeShown",
         "diamondAxeIcon",
         "diamondPickIcon",
         "unheldEnchantedPick",
         "backpackToolIgnored",
         "shearsShownWithoutTool"
      )) {
         passed &= init_JsonObject.has(key) && init_JsonObject.get(key).getAsBoolean();
      }

      passed &= init_JsonObject.has("coverCount") && init_JsonObject.get("coverCount").getAsInt() == 8;
      passed &= init_JsonObject.has("fillAlpha") && init_JsonObject.get("fillAlpha").getAsInt() == 32;
      passed &= init_JsonObject.has("readableBedColours") && init_JsonObject.get("readableBedColours").getAsBoolean();

      for (int i = 0; i < 6; i++) {
         passed &= init_JsonObject.has("recovery-" + i) && init_JsonObject.get("recovery-" + i).getAsBoolean();
      }

      init_JsonObject.addProperty("passed", passed);

      try {
         Files.createDirectories(init_Path);
         Files.writeString(init_Path.resolve("report.json"), new GsonBuilder().setPrettyPrinting().create().toJson(init_JsonObject));
      } catch (Exception error) {
         throw new RuntimeException(error);
      }

      System.out.println("[BedESP check] FINAL: " + (passed ? "PASS" : "FAIL") + " " + init_JsonObject);
   }
}

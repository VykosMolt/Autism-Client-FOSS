package autismclient.compat263;

import com.mojang.blaze3d.Blaze3D;
import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.platform.Window;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.renderpearl.api.buffers.GpuBuffer;
import com.mojang.renderpearl.api.commands.CommandEncoder;
import com.mojang.renderpearl.api.commands.RenderPass;
import com.mojang.renderpearl.api.pipeline.BindGroupLayout;
import com.mojang.renderpearl.api.pipeline.ColorTargetState;
import com.mojang.renderpearl.api.pipeline.DepthStencilState;
import com.mojang.renderpearl.api.pipeline.IndexType;
import com.mojang.renderpearl.api.pipeline.PolygonMode;
import com.mojang.renderpearl.api.pipeline.PrimitiveTopology;
import com.mojang.renderpearl.api.pipeline.RenderPipeline;
import com.mojang.renderpearl.api.pipeline.ShaderType;
import com.mojang.renderpearl.api.textures.GpuTextureView;
import com.mojang.renderpearl.api.vertex.VertexFormat;
import java.io.File;
import java.io.InputStream;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Method;
import java.net.URI;
import java.nio.file.Path;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.OptionalDouble;
import java.util.Set;
import java.util.Map.Entry;
import java.util.function.Predicate;
import java.util.function.Supplier;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Options;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.HangingSignEditScreen;
import net.minecraft.client.gui.screens.inventory.SignEditScreen;
import net.minecraft.client.gui.screens.options.OptionsScreen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.input.MouseButtonInfo;
import net.minecraft.client.model.Model;
import net.minecraft.client.multiplayer.ClientChunkCache;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.player.RemotePlayer;
import net.minecraft.client.renderer.OrderedSubmitNodeCollector;
import net.minecraft.client.renderer.ShaderDefines;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.StagedVertexBuffer.ExecuteInfo;
import net.minecraft.client.renderer.chunk.ChunkSectionLayer;
import net.minecraft.client.renderer.extract.LevelExtractor;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer.CrumblingOverlay;
import net.minecraft.client.renderer.item.ItemStackRenderState.LayerRenderState;
import net.minecraft.client.renderer.rendertype.PreparedRenderType;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderSetup.RenderSetupBuilder;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.geometry.BakedQuad;
import net.minecraft.client.resources.model.geometry.BakedQuad.MaterialInfo;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.PositionAndRotation;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.component.TypedDataComponent;
import net.minecraft.core.component.DataComponentPatch.SplitResult;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MessageSignature;
import net.minecraft.network.chat.LastSeenMessages.Update;
import net.minecraft.network.protocol.game.ClientboundEntityPositionSyncPacket;
import net.minecraft.network.protocol.game.ClientboundMoveEntityPacket;
import net.minecraft.network.protocol.game.ClientboundMoveVehiclePacket;
import net.minecraft.network.protocol.game.ClientboundOpenSignEditorPacket;
import net.minecraft.network.protocol.game.ClientboundPlayerChatPacket;
import net.minecraft.network.protocol.game.CommonPlayerSpawnInfo;
import net.minecraft.network.protocol.game.ServerboundAcceptTeleportationPacket;
import net.minecraft.network.protocol.game.ServerboundChatPacket;
import net.minecraft.network.protocol.game.ServerboundMoveVehiclePacket;
import net.minecraft.network.protocol.game.ServerboundPlayerActionPacket;
import net.minecraft.network.protocol.game.ServerboundSignUpdatePacket;
import net.minecraft.network.protocol.game.VecDelta;
import net.minecraft.network.protocol.game.ServerboundPlayerActionPacket.Action;
import net.minecraft.network.protocol.game.VecDelta.Linear;
import net.minecraft.network.protocol.game.VecDelta.Stepped;
import net.minecraft.network.protocol.game.VecDelta.Stepped.DeltaStep;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.VanillaPackResources;
import net.minecraft.server.packs.PackResources.ResourceOutput;
import net.minecraft.server.packs.resources.IoSupplier;
import net.minecraft.tags.ItemTags;
import net.minecraft.util.Util.OS;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.InterpolationHandler;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.PositionMoveRotation;
import net.minecraft.world.entity.PositionPath;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.component.PiercingWeapon;
import net.minecraft.world.item.component.SwingAnimation;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ClipContext.Block;
import net.minecraft.world.level.block.BonemealSource;
import net.minecraft.world.level.block.BonemealableBlock;
import net.minecraft.world.level.block.entity.SignBlockEntity;
import net.minecraft.world.level.block.entity.SignText;
import net.minecraft.world.level.block.entity.SignTextSlot;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.LevelChunkSection;
import net.minecraft.world.level.chunk.status.ChunkStatus;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;

public final class Compat263 {
   private static final Supplier<String> AUTISM_PASS_NAME;
   private static final MethodHandle SET_SECTION_DIRTY_URGENT = findSetSectionDirtyUrgent();

   private Compat263() {
   }

   public static float zeroFloat(Object var0) {
      return 0.0F;
   }

   private static MethodHandle findSetSectionDirtyUrgent() {
      try {
         Method m = LevelExtractor.class.getDeclaredMethod("setSectionDirty", int.class, int.class, int.class, boolean.class);
         m.setAccessible(true);
         return MethodHandles.lookup().unreflect(m);
      } catch (Throwable ignored) {
         return null;
      }
   }

   private static int markChunkDirty(LevelExtractor extractor, ClientChunkCache chunkSource, int cx, int cz, int minSecY, int playerSecY, boolean urgentRing) {
      LevelChunk chunk = chunkSource.getChunk(cx, cz, ChunkStatus.FULL, false);
      if (chunk != null && !chunk.isEmpty()) {
         int count = 0;
         LevelChunkSection[] sections = chunk.getSections();

         for (int i = 0; i < sections.length; i++) {
            LevelChunkSection sec = sections[i];
            if (sec != null && !sec.hasOnlyAir()) {
               int sy = minSecY + i;
               if (urgentRing && SET_SECTION_DIRTY_URGENT != null && Math.abs(sy - playerSecY) <= 2) {
                  try {
                     SET_SECTION_DIRTY_URGENT.invokeExact((LevelExtractor)extractor, (int)cx, (int)sy, (int)cz, (boolean)true);
                     count++;
                     continue;
                  } catch (Throwable var14) {
                  }
               }

               extractor.setSectionDirty(cx, sy, cz);
               count++;
            }
         }

         return count;
      } else {
         return 0;
      }
   }

   public static void softReloadChunks(LevelExtractor var0) {
      Minecraft var1 = Minecraft.getInstance();
      if (var1 != null && var1.level != null && var1.player != null && var0 != null) {
         try {
            ClientLevel var2 = var1.level;
            ClientChunkCache var3 = var2.getChunkSource();
            int radius = Math.max(2, var1.options.getEffectiveRenderDistance() + 1);
            int pcx = var1.player.chunkPosition().x();
            int pcz = var1.player.chunkPosition().z();
            int minSecY = var2.getMinSectionY();
            int playerSecY = var1.player.blockPosition().getY() >> 4;
            int marked = markChunkDirty(var0, var3, pcx, pcz, minSecY, playerSecY, true);

            for (int r = 1; r <= radius; r++) {
               boolean urgent = r <= 2;

               for (int d = -r; d <= r; d++) {
                  marked += markChunkDirty(var0, var3, pcx + d, pcz - r, minSecY, playerSecY, urgent);
                  marked += markChunkDirty(var0, var3, pcx + d, pcz + r, minSecY, playerSecY, urgent);
               }

               for (int d = -r + 1; d <= r - 1; d++) {
                  marked += markChunkDirty(var0, var3, pcx - r, pcz + d, minSecY, playerSecY, urgent);
                  marked += markChunkDirty(var0, var3, pcx + r, pcz + d, minSecY, playerSecY, urgent);
               }
            }

            if (marked == 0) {
               var0.allChanged();
            }
         } catch (Throwable var18) {
            var0.allChanged();
         }
      } else if (var0 != null) {
         var0.allChanged();
      }
   }

   public static CompatItemInHandRenderer getItemInHandRenderer(Object var0) {
      return CompatItemInHandRenderer.INSTANCE;
   }

   public static boolean isKeyDown(Window var0, int var1) {
      return InputConstants.isKeyDown(var1);
   }

   public static boolean isKeyDown(int var0) {
      return CompatGLFW.sdlIsKeyDown(var0);
   }

   public static int keyEventGlfwKey(KeyEvent var0) {
      return var0 == null ? -1 : CompatGLFW.sdlToGlfw(var0.key());
   }

   public static int keyEventGlfwModifiers(KeyEvent var0) {
      return var0 == null ? 0 : sdlModsToGlfw(var0.modifiers());
   }

   public static int mouseEventGlfwButton(MouseButtonEvent var0) {
      return var0 == null ? 0 : CompatGLFW.sdlMouseToGlfw(var0.button());
   }

   public static int mouseInfoGlfwButton(MouseButtonInfo var0) {
      return var0 == null ? 0 : CompatGLFW.sdlMouseToGlfw(var0.button());
   }

   public static int mouseEventGlfwModifiers(MouseButtonEvent var0) {
      return var0 == null ? 0 : sdlModsToGlfw(var0.modifiers());
   }

   public static int sdlModsToGlfw(int var0) {
      byte var1 = 0;
      if ((var0 & 3) != 0) {
         var1 = (byte)(var1 | 1);
      }

      if ((var0 & 192) != 0) {
         var1 = (byte)(var1 | 2);
      }

      if ((var0 & 768) != 0) {
         var1 = (byte)(var1 | 4);
      }

      if ((var0 & 3072) != 0) {
         var1 = (byte)(var1 | 8);
      }

      if ((var0 & 8192) != 0) {
         var1 = (byte)(var1 | 16);
      }

      if ((var0 & 4096) != 0) {
         var1 = (byte)(var1 | 32);
      }

      return var1;
   }

   public static float getPartialTick() {
      Minecraft var0 = Minecraft.getInstance();
      return var0 != null ? var0.getDeltaTracker().getGameTimeDeltaPartialTick(true) : 0.0F;
   }

   public static int glfwModsToSdl(int glfwMods) {
      int out = 0;
      if ((glfwMods & 1) != 0) {
         out |= 3;
      }

      if ((glfwMods & 2) != 0) {
         out |= 192;
      }

      if ((glfwMods & 4) != 0) {
         out |= 768;
      }

      if ((glfwMods & 8) != 0) {
         out |= 3072;
      }

      if ((glfwMods & 16) != 0) {
         out |= 8192;
      }

      if ((glfwMods & 32) != 0) {
         out |= 4096;
      }

      return out;
   }

   public static KeyEvent newGlfwKeyEvent(int glfwKey, int scancode, int glfwMods) {
      int sdlKey = CompatGLFW.glfwToSdl(glfwKey);
      if (sdlKey < 0) {
         sdlKey = glfwKey;
      }

      return new KeyEvent(sdlKey, scancode, glfwModsToSdl(glfwMods));
   }

   public static MouseButtonInfo newGlfwMouseButtonInfo(int glfwButton, int glfwMods) {
      return new MouseButtonInfo(CompatGLFW.glfwMouseToSdl(glfwButton), glfwModsToSdl(glfwMods));
   }

   public static void drawFromBuffer(PreparedRenderType var0, ExecuteInfo var1) {
      if (var0 != null && var1 != null) {
         Minecraft var2 = Minecraft.getInstance();
         if (var2 != null && var2.gameRenderer != null) {
            RenderTarget var3 = var2.gameRenderer.mainRenderTarget();
            if (var3 != null) {
               GpuTextureView var4 = var3.getColorTextureView();
               GpuTextureView var5 = var3.getDepthTextureView();
               if (var4 != null) {
                  CommandEncoder var6 = RenderSystem.getDevice().createCommandEncoder();

                  try (RenderPass var7 = var5 != null
                        ? var6.createRenderPass(AUTISM_PASS_NAME, var4, Optional.empty(), var5, OptionalDouble.empty())
                        : var6.createRenderPass(AUTISM_PASS_NAME, var4, Optional.empty())) {
                     try {
                        var0.drawFromBuffer(var1, var7);
                     } catch (Throwable var12) {
                        try {
                           var7.popDebugGroup();
                        } catch (Throwable var11) {
                        }

                        throw var12;
                     }
                  }
               }
            }
         }
      }
   }

   public static void drawFromBuffer(PreparedRenderType var0, GpuBuffer var1, GpuBuffer var2, IndexType var3, int var4, int var5, int var6) {
      if (var0 != null && var1 != null && var6 > 0) {
         PrimitiveTopology var7 = var0.pipeline() != null ? var0.pipeline().getPrimitiveTopology() : PrimitiveTopology.TRIANGLES;
         ExecuteInfo var8 = new ExecuteInfo(var1, var2, var3, var4, var5, var6, var7);
         drawFromBuffer(var0, var8);
      }
   }

   public static MaterialInfo newMaterialInfo(TextureAtlasSprite var0, ChunkSectionLayer var1, RenderType var2, int var3, boolean var4, int var5) {
      return new MaterialInfo(var0, var1, var2, var2, var2, var3, var4 ? null : Direction.UP, var5);
   }

   public static ColorTargetState[] getColorTargetStates(RenderPipeline var0) {
      return var0.getColorTargetStates().toArray(Compat263::lambda$getColorTargetStates$0);
   }

   public static VertexFormat[] getVertexFormatBindings(RenderPipeline var0) {
      return var0.getVertexFormatBindings().toArray(Compat263::lambda$getVertexFormatBindings$0);
   }

   public static Identifier getVertexShader(RenderPipeline var0) {
      return var0.getShaders().get(ShaderType.VERTEX);
   }

   public static List<?> getSamplers(BindGroupLayout var0) {
      return List.of();
   }

   public static RenderSetupBuilder setOutputTarget(RenderSetupBuilder var0, Object var1) {
      return var0;
   }

   public static List<BakedQuad> prepareQuadList(LayerRenderState layer) {
      return new ArrayList<>();
   }

   public static RenderPipeline createRenderPipeline(
      Identifier var0,
      Identifier var1,
      Identifier var2,
      ShaderDefines var3,
      List<BindGroupLayout> var4,
      ColorTargetState[] var5,
      DepthStencilState var6,
      PolygonMode var7,
      boolean var8,
      VertexFormat[] var9,
      PrimitiveTopology var10,
      int var11
   ) {
      EnumMap var12 = new EnumMap<>(ShaderType.class);
      if (var1 != null) {
         var12.put(ShaderType.VERTEX, var1);
      }

      if (var2 != null) {
         var12.put(ShaderType.FRAGMENT, var2);
      }

      return new Compat263.CustomRenderPipeline(var0, var12, var3, var4, var5, var6, var7, var8, var9, var10, var11);
   }

   public static void swing(LocalPlayer var0, InteractionHand var1) {
      if (var0 != null) {
         var0.swing(var1, SwingAnimation.DEFAULT, false);
      }
   }

   public static void swing(RemotePlayer var0, InteractionHand var1) {
      if (var0 != null) {
         var0.swing(var1, SwingAnimation.DEFAULT, false);
      }
   }

   public static float getAttackAnim(LivingEntity var0, float var1) {
      return var0 == null ? 0.0F : var0.getSwingAnimation(var1);
   }

   public static boolean drop(LocalPlayer var0, boolean var1) {
      if (var0 == null) {
         return false;
      } else {
         Minecraft mc = Minecraft.getInstance();
         if (mc != null && mc.gameMode != null) {
            mc.gameMode.dropItem(var0, var1);
            return true;
         } else if (var0.connection != null) {
            Action var2 = var1 ? Action.DROP_ALL_ITEMS : Action.DROP_ITEM;
            var0.connection.send(new ServerboundPlayerActionPacket(var2, BlockPos.ZERO, Direction.DOWN));
            return true;
         } else {
            return false;
         }
      }
   }

   public static void piercingAttack(MultiPlayerGameMode var0, PiercingWeapon var1) {
      if (var0 != null && var1 != null) {
         var0.piercingAttack(SwingAnimation.DEFAULT, var1);
      }
   }

   public static boolean isAxe(Object var0) {
      return var0 instanceof Item var1 && var1.builtInRegistryHolder().is(ItemTags.AXES);
   }

   public static boolean isHoe(Object var0) {
      return var0 instanceof Item var1 && var1.builtInRegistryHolder().is(ItemTags.HOES);
   }

   public static boolean isShovel(Object var0) {
      return var0 instanceof Item var1 && var1.builtInRegistryHolder().is(ItemTags.SHOVELS);
   }

   public static OptionsScreen newOptionsScreen(Screen var0, Options var1, boolean var2) {
      return new OptionsScreen(var0, var1);
   }

   public static SignEditScreen newSignEditScreen(SignBlockEntity var0, boolean var1, boolean var2) {
      return new SignEditScreen(var0, var1 ? SignTextSlot.FRONT : SignTextSlot.BACK, var2);
   }

   public static HangingSignEditScreen newHangingSignEditScreen(SignBlockEntity var0, boolean var1, boolean var2) {
      return new HangingSignEditScreen(var0, var1 ? SignTextSlot.FRONT : SignTextSlot.BACK, var2);
   }

   public static void addBreakingBlockEffect(ClientLevel var0, BlockPos var1, Direction var2) {
      if (var0 != null) {
         var0.addBreakingBlockEffects(var1, var2, false);
      }
   }

   public static SignText getFrontText(SignBlockEntity var0) {
      return var0.getText(SignTextSlot.FRONT);
   }

   public static SignText getBackText(SignBlockEntity var0) {
      return var0.getText(SignTextSlot.BACK);
   }

   public static Component[] getMessages(SignText var0, boolean var1) {
      return var0.getMessages(var1).toArray(Compat263::lambda$getMessages$0);
   }

   public static boolean isValidBonemealTarget(BonemealableBlock var0, LevelReader var1, BlockPos var2, BlockState var3) {
      return var0.isValidBonemealTarget(var1, var2, var3, BonemealSource.INTERACTION);
   }

   public static PositionMoveRotation entityPosSyncValues(ClientboundEntityPositionSyncPacket var0) {
      return new PositionMoveRotation(var0.position().endPosition(), Vec3.ZERO, var0.yRot(), var0.xRot());
   }

   public static short moveEntityXa(ClientboundMoveEntityPacket var0) {
      if (var0 == null) {
         return 0;
      } else {
         VecDelta delta = var0.getPositionDelta();
         if (delta instanceof Linear lin) {
            return lin.xa();
         } else if (!(delta instanceof Stepped step)) {
            return 0;
         } else {
            int sum = 0;

            for (DeltaStep s : step.steps()) {
               sum += s.xa();
            }

            return (short)Math.clamp(sum, -32768, 32767);
         }
      }
   }

   public static short moveEntityYa(ClientboundMoveEntityPacket var0) {
      if (var0 == null) {
         return 0;
      } else {
         VecDelta delta = var0.getPositionDelta();
         if (delta instanceof Linear lin) {
            return lin.ya();
         } else if (!(delta instanceof Stepped step)) {
            return 0;
         } else {
            int sum = 0;

            for (DeltaStep s : step.steps()) {
               sum += s.ya();
            }

            return (short)Math.clamp(sum, -32768, 32767);
         }
      }
   }

   public static short moveEntityZa(ClientboundMoveEntityPacket var0) {
      if (var0 == null) {
         return 0;
      } else {
         VecDelta delta = var0.getPositionDelta();
         if (delta instanceof Linear lin) {
            return lin.za();
         } else if (!(delta instanceof Stepped step)) {
            return 0;
         } else {
            int sum = 0;

            for (DeltaStep s : step.steps()) {
               sum += s.za();
            }

            return (short)Math.clamp(sum, -32768, 32767);
         }
      }
   }

   public static Vec3 moveVehiclePos(ClientboundMoveVehiclePacket var0) {
      return var0.movingTo().position();
   }

   public static float moveVehicleXRot(ClientboundMoveVehiclePacket var0) {
      return var0.movingTo().xRot();
   }

   public static float moveVehicleYRot(ClientboundMoveVehiclePacket var0) {
      return var0.movingTo().yRot();
   }

   public static Vec3 sbMoveVehiclePos(ServerboundMoveVehiclePacket var0) {
      return var0.movingTo().position();
   }

   public static float sbMoveVehicleXRot(ServerboundMoveVehiclePacket var0) {
      return var0.movingTo().xRot();
   }

   public static float sbMoveVehicleYRot(ServerboundMoveVehiclePacket var0) {
      return var0.movingTo().yRot();
   }

   public static ServerboundMoveVehiclePacket newServerboundMoveVehiclePacket(Vec3 var0, float var1, float var2, boolean var3) {
      return new ServerboundMoveVehiclePacket(PositionAndRotation.of(var0, var1, var2), var3);
   }

   public static boolean openSignIsFrontText(ClientboundOpenSignEditorPacket var0) {
      return var0.slot() == SignTextSlot.FRONT;
   }

   public static MessageSignature chatSignature(ClientboundPlayerChatPacket var0) {
      return var0.signature().orElse(null);
   }

   public static Component chatUnsignedContent(ClientboundPlayerChatPacket var0) {
      return var0.unsignedContent().orElse(null);
   }

   public static GameType previousGameType(CommonPlayerSpawnInfo var0) {
      return var0.previousGameType().orElse(null);
   }

   public static ServerboundAcceptTeleportationPacket newAcceptTeleportationPacket(int var0) {
      LocalPlayer var1 = Minecraft.getInstance().player;
      return var1 != null
         ? new ServerboundAcceptTeleportationPacket(var0, var1.getX(), var1.getY(), var1.getZ(), var1.getYRot(), var1.getXRot())
         : new ServerboundAcceptTeleportationPacket(var0, 0.0, 0.0, 0.0, 0.0F, 0.0F);
   }

   public static ServerboundChatPacket newChatPacket(String var0, Instant var1, long var2, MessageSignature var4, Update var5) {
      return new ServerboundChatPacket(var0, var1, var2, Optional.ofNullable(var4), var5);
   }

   public static ServerboundSignUpdatePacket newSignUpdatePacket(BlockPos var0, boolean var1, String var2, String var3, String var4, String var5) {
      return new ServerboundSignUpdatePacket(var0, List.of(var2, var3, var4, var5), var1 ? SignTextSlot.FRONT : SignTextSlot.BACK);
   }

   public static Vec3 interpPos(InterpolationHandler var0) {
      return var0.target().position();
   }

   public static float interpXRot(InterpolationHandler var0) {
      return var0.target().xRot();
   }

   public static float interpYRot(InterpolationHandler var0) {
      return var0.target().yRot();
   }

   public static void interpTo(InterpolationHandler var0, Vec3 var1, float var2, float var3) {
      var0.interpolateTo(PositionPath.of(var1), var2, var3, false);
   }

   public static void interpSetLength(InterpolationHandler var0, int var1) {
   }

   public static Collection<EntityHitResult> getManyEntityHitResult(
      Level var0, Entity var1, Vec3 var2, Vec3 var3, AABB var4, Predicate<Entity> var5, float var6, Block var7, boolean var8
   ) {
      return ProjectileUtil.getManyEntityHitResult(var0, var1, var2, var3, var4, var5, var6, var7, var8, false);
   }

   public static boolean materialInfoShade(MaterialInfo var0) {
      return var0 == null || var0.shadeDirectionOverride() == null;
   }

   public static Set<Entry<DataComponentType<?>, Optional<?>>> dataComponentPatchEntrySet(DataComponentPatch var0) {
      if (var0 != null && !var0.isEmpty()) {
         try {
            SplitResult split = var0.split();
            LinkedHashMap<DataComponentType<?>, Optional<?>> out = new LinkedHashMap<>();

            for (TypedDataComponent<?> comp : split.added()) {
               out.put(comp.type(), Optional.ofNullable(comp.value()));
            }

            for (DataComponentType<?> rem : split.removed()) {
               out.put(rem, Optional.empty());
            }

            return out.entrySet();
         } catch (Throwable ignored) {
            return Set.of();
         }
      } else {
         return Set.of();
      }
   }

   public static IoSupplier<InputStream> vanillaGetResource(VanillaPackResources var0, PackType var1, Identifier var2) {
      return var0.fullResources().getResource(var1, var2);
   }

   public static void vanillaListResources(VanillaPackResources var0, PackType var1, String var2, String var3, ResourceOutput var4) {
      var0.fullResources().listResources(var1, var2, var3, var4);
   }

   public static void vanillaClose(VanillaPackResources var0) {
      var0.fullResources().close();
   }

   public static void openUri(OS var0, String var1) {
      if (var1 != null && !var1.isEmpty()) {
         try {
            Blaze3D.openUri(URI.create(var1));
         } catch (Throwable var4) {
            try {
               new ProcessBuilder("xdg-open", var1).start();
            } catch (Throwable var3) {
            }
         }
      }
   }

   public static void openUri(OS var0, URI var1) {
      if (var1 != null) {
         try {
            Blaze3D.openUri(var1);
         } catch (Throwable ignored) {
            openUri(var0, var1.toString());
         }
      }
   }

   public static void openFile(OS var0, File var1) {
      if (var1 != null) {
         try {
            Blaze3D.openPath(var1.toPath());
         } catch (Throwable ignored) {
            openUri(var0, var1.toURI());
         }
      }
   }

   public static void openPath(OS var0, Path var1) {
      if (var1 != null) {
         try {
            Blaze3D.openPath(var1);
         } catch (Throwable ignored) {
            openUri(var0, var1.toUri());
         }
      }
   }

   public static void submitModel(
      SubmitNodeCollector var0, Model var1, Object var2, PoseStack var3, RenderType var4, int var5, int var6, int var7, CrumblingOverlay var8
   ) {
      var0.submitModel(var1, var2, var3, var4, var5, var6, var7);
      if (var8 != null) {
         var0.submitCrumblingOverlay(var1, var2, var3, var4, var5, var6, -1, var8);
      }
   }

   public static void submitModel(
      SubmitNodeCollector var0,
      Model var1,
      Object var2,
      PoseStack var3,
      RenderType var4,
      int var5,
      int var6,
      int var7,
      TextureAtlasSprite var8,
      int var9,
      CrumblingOverlay var10
   ) {
      var0.submitModel(var1, var2, var3, var4, var5, var6, var7, var8, var9);
      if (var10 != null) {
         var0.submitCrumblingOverlay(var1, var2, var3, var4, var5, var6, var7, var10);
      }
   }

   public static void submitModel(
      OrderedSubmitNodeCollector var0,
      Model var1,
      Object var2,
      PoseStack var3,
      RenderType var4,
      int var5,
      int var6,
      int var7,
      TextureAtlasSprite var8,
      int var9,
      CrumblingOverlay var10
   ) {
      var0.submitModel(var1, var2, var3, var4, var5, var6, var7, var8, var9);
      if (var10 != null) {
         var0.submitCrumblingOverlay(var1, var2, var3, var4, var5, var6, var7, var10);
      }
   }

   private static Component[] lambda$getMessages$0(int var0) {
      return new Component[var0];
   }

   private static VertexFormat[] lambda$getVertexFormatBindings$0(int var0) {
      return new VertexFormat[var0];
   }

   private static ColorTargetState[] lambda$getColorTargetStates$0(int var0) {
      return new ColorTargetState[var0];
   }

   private static String lambda$static$0() {
      return "autism:overlay";
   }

   static {
      AUTISM_PASS_NAME = Compat263::lambda$static$0;
   }

   private static final class CustomRenderPipeline extends RenderPipeline {
      CustomRenderPipeline(
         Identifier var1,
         Map<ShaderType, Identifier> var2,
         ShaderDefines var3,
         Collection<BindGroupLayout> var4,
         ColorTargetState[] var5,
         DepthStencilState var6,
         PolygonMode var7,
         boolean var8,
         VertexFormat[] var9,
         PrimitiveTopology var10,
         int var11
      ) {
         super(var1, var2, var3, var4, var5, var6, var7, var8, var9, var10, var11, 0);
      }
   }
}

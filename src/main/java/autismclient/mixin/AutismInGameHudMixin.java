package autismclient.mixin;

import autismclient.AutismClientAddon;
import autismclient.ai.AiVisualization;
import autismclient.e.PathHelper;
import autismclient.gui.f.UiClipStack;
import autismclient.gui.f.NetHelper;
import autismclient.gui.f.UiHelper;
import autismclient.gui.f.b.StatusToastCardRenderer;
import autismclient.gui.macro.editor.ActionEditorOverlay;
import autismclient.modules.EndPortalRenderer;
import autismclient.modules.Module;
import autismclient.modules.BedEspOverlayRenderer;
import autismclient.modules.ModuleRuntimeManager;
import autismclient.modules.AntiVanishModule;
import autismclient.modules.aD;
import autismclient.modules.TrajectoryOffscreenIndicator;
import autismclient.modules.NameTagsRenderer;
import autismclient.modules.am;
import autismclient.modules.EntityEsp2DRenderer;
import autismclient.modules.DangerHudRenderer;
import autismclient.util.AutismLiteVariant;
import autismclient.util.AutismServerInfoOverlay;
import autismclient.util.HudBannerSpec;
import autismclient.util.MacroStatusHudRenderer;
import autismclient.util.AutismHudManager;
import autismclient.util.PayloadStudyTracker;
import autismclient.util.bK;
import autismclient.util.OverlayManager;
import autismclient.util.cA;
import autismclient.util.PacketQueueHudRenderer;
import autismclient.util.cf;
import autismclient.util.AutismSessionState;
import autismclient.util.macro.MacroExecutor;
import java.util.ArrayList;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.Hud;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult.Type;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Hud.class)
public abstract class AutismInGameHudMixin {
   @Unique
   private static final Minecraft MC = Minecraft.getInstance();
   @Unique
   private static final int PACKUTIL_RIGHT_PANEL_W = 172;
   @Unique
   private static long autism$lastRenderErrorMs;
   @Unique
   private BlockState autism$cachedBlockState;
   @Unique
   private String autism$cachedBlockName = "";
   @Unique
   private EntityType<?> autism$cachedEntityType;
   @Unique
   private String autism$cachedEntityLabel = "";

   @Inject(method = "extractRenderState", at = @At("TAIL"))
   private void yang$renderAutismQueue(GuiGraphicsExtractor context, DeltaTracker deltaTracker, CallbackInfo ci) {
      try {
         this.autism$renderHudBody(context);
      } catch (Throwable t) {
         this.autism$logRenderError("hudRoot", t);
      }
   }

   @Unique
   private void autism$renderHudBody(GuiGraphicsExtractor context) {
      if (this.isAutismActive() && (AutismLiteVariant.enabled() || PathHelper.init())) {
         boolean macroFrameWork = MacroExecutor.flush_Z();
         cf.Inner_DataUtil activity = cf.init_cf_DataUtil();
         long hudWork = 31168L;
         boolean aiPlan = !AutismLiteVariant.enabled() && MC.gui.screen() == null && AiVisualization.load_Z();
         boolean portalGuide = !AutismLiteVariant.enabled() && EndPortalRenderer.load();
         if (macroFrameWork || activity.init(hudWork) || aiPlan || portalGuide) {
            UiClipStack.init_HashUtil().load(context);
            if (macroFrameWork) {
               MacroExecutor.init_V_F(1.0F);
            }

            if (!MC.gui.hud.isHidden() && !aD.init_Z()) {
               AutismSessionState shared = AutismSessionState.init_ck();
               Screen screen = MC.gui.screen();
               boolean macroRunning = MacroExecutor.load_Z();
               boolean queueSending = shared.run_Z();
               boolean queueVisible = shared.write_Z() || shared.open_Z() || queueSending;
               boolean captureActive = this.hasAnyCaptureSession(shared);
               boolean payloadStudyActive = PayloadStudyTracker.init_Z();
               Module hud = activity.init(64L) ? autismclient.modules.al.init_Module_Str("hud") : null;
               boolean nativeHudVisible = hud != null && AutismHudManager.init_Z_Screen_Module(screen, hud);
               boolean esp2dVisible = am.add();
               boolean nametagsVisible = activity.init(128L);
               boolean bedsVisible = activity.init(16384L);
               boolean antiVanishHudVisible = screen == null && AntiVanishModule.init_Z_19();
               boolean mainHudVisible = nativeHudVisible
                  || antiVanishHudVisible
                  || macroRunning
                  || queueVisible
                  || captureActive
                  || payloadStudyActive
                  || esp2dVisible
                  || nametagsVisible
                  || bedsVisible
                  || aiPlan
                  || portalGuide
                  || activity.init(4096L)
                  || activity.init(8192L);
               AutismServerInfoOverlay serverInfoOverlay = null;
               boolean serverProbeBannerVisible = false;
               OverlayManager overlayManager = null;
               boolean overlayVisible = false;
               if (screen == null) {
                  serverInfoOverlay = ModuleRuntimeManager.init_PoolEntry().load_AutismServerInfoOv();
                  serverProbeBannerVisible = serverInfoOverlay != null && serverInfoOverlay.run_Z_5();
                  if (activity.init(256L)) {
                     overlayManager = OverlayManager.init_bk();
                     overlayVisible = overlayManager.load_Z();
                  }
               }

               if (mainHudVisible || serverProbeBannerVisible || overlayVisible || aiPlan) {
                  Runnable renderHudElements = () -> {
                     int screenWidth = cA.init_I();
                     int x = Math.max(0, screenWidth - 172);
                     int y = 0;
                     HudBannerSpec captureBanner = captureActive ? this.captureBannerSpec(shared, context) : null;
                     HudBannerSpec payloadStudyBanner = payloadStudyActive
                        ? this.payloadStudyBannerSpec(context, captureBanner == null ? 0 : captureBanner.write())
                        : null;
                     ArrayList<AutismHudManager.Inner_BufferHelper> hudOccluders = new ArrayList<>(3);
                     if (!AutismLiteVariant.enabled() && aiPlan) {
                        NetHelper b = AiVisualization.init_NetHelper(context);
                        hudOccluders.add(new AutismHudManager.Inner_BufferHelper("ai_plan", b.read(), b.write(), b.update(), b.sync()));
                     }

                     if (!AutismLiteVariant.enabled() && portalGuide) {
                        NetHelper b = EndPortalRenderer.init_NetHelper(context);
                        hudOccluders.add(new AutismHudManager.Inner_BufferHelper("end_portal", b.read(), b.write(), b.update(), b.sync()));
                     }

                     if (captureBanner != null) {
                        hudOccluders.add(
                           new AutismHudManager.Inner_BufferHelper(
                              "capture_banner", captureBanner.init_I(), captureBanner.load_I(), captureBanner.read_I(), captureBanner.write()
                           )
                        );
                     }

                     if (payloadStudyBanner != null) {
                        hudOccluders.add(
                           new AutismHudManager.Inner_BufferHelper(
                              "payload_study_banner",
                              payloadStudyBanner.init_I(),
                              payloadStudyBanner.load_I(),
                              payloadStudyBanner.read_I(),
                              payloadStudyBanner.write()
                           )
                        );
                     } else if (captureActive) {
                        int fallbackW = Math.min(screenWidth - 16, 300);
                        hudOccluders.add(new AutismHudManager.Inner_BufferHelper("capture_banner", Math.max(0, (screenWidth - fallbackW) / 2), 0, fallbackW, 56));
                     }

                     if (queueVisible) {
                        int queueHeight = PacketQueueHudRenderer.init_I(MC.font, 172, 8);
                        if (queueHeight > 0) {
                           hudOccluders.add(new AutismHudManager.Inner_BufferHelper("packet_queue", x, y, 172, queueHeight));
                           y += queueHeight;
                        }
                     }

                     if (macroRunning) {
                        int macroHeight = MacroStatusHudRenderer.init_I(MC.font, 172, 10);
                        if (macroHeight > 0) {
                           hudOccluders.add(new AutismHudManager.Inner_BufferHelper("macro_queue", x, y, 172, macroHeight));
                        }
                     }

                     if (nativeHudVisible) {
                        AutismHudManager.init_V_GuiGraphicsExtract_4(context, MC.font, false, null, -1, -1, hudOccluders);
                     } else if (antiVanishHudVisible) {
                        AutismHudManager.init_V_GuiGraphicsExtract_2(context, MC.font, "anti_vanish");
                     }

                     if (captureBanner != null) {
                        StatusToastCardRenderer.init(
                           UiHelper.init(context, MC.font, 0, 0),
                           NetHelper.init(captureBanner.init_I(), captureBanner.load_I(), captureBanner.read_I(), captureBanner.write()),
                           captureBanner.init_Str(),
                           captureBanner.load_Str(),
                           captureBanner.read_Str()
                        );
                     }

                     if (payloadStudyBanner != null) {
                        StatusToastCardRenderer.init(
                           UiHelper.init(context, MC.font, 0, 0),
                           NetHelper.init(payloadStudyBanner.init_I(), payloadStudyBanner.load_I(), payloadStudyBanner.read_I(), payloadStudyBanner.write()),
                           payloadStudyBanner.init_Str(),
                           payloadStudyBanner.load_Str(),
                           payloadStudyBanner.read_Str()
                        );
                     }

                     if (!AutismLiteVariant.enabled() && aiPlan) {
                        AiVisualization.init_V(context);
                     }

                     y = 0;
                     if (queueVisible) {
                        int queueHeight = PacketQueueHudRenderer.init(context, MC.font, x, y, 172, 8, false, !macroRunning, false);
                        y += queueHeight;
                     }

                     if (macroRunning) {
                        MacroStatusHudRenderer.init(context, MC.font, x, y, 172, 10, false, true, false);
                     }

                     if (!AutismLiteVariant.enabled() && portalGuide) {
                        EndPortalRenderer.init_V(context);
                     }

                     if (esp2dVisible) {
                        EntityEsp2DRenderer.init_V_GuiGraphicsExtract(context);
                     }

                     if (nametagsVisible) {
                        NameTagsRenderer.init(context);
                     }

                     if (!AutismLiteVariant.enabled() && bedsVisible) {
                        BedEspOverlayRenderer.init(context);
                     }

                     if (!AutismLiteVariant.enabled() && activity.init(4096L)) {
                        autismclient.modules.DangerHudRenderer.init(context);
                     }

                     if (!AutismLiteVariant.enabled() && activity.init(8192L)) {
                        TrajectoryOffscreenIndicator.init(context);
                     }
                  };
                  if (mainHudVisible) {
                     long perfStart = bK.load_J();
                     cA.init_V_GuiGraphicsExtract(context);

                     try {
                        renderHudElements.run();
                     } catch (Throwable t) {
                        this.autism$logRenderError("hudElements", t);
                     } finally {
                        cA.load_V_GuiGraphicsExtract(context);
                     }

                     bK.init("hud.section.hudElements", perfStart);
                  }

                  if (MC.gui.screen() == null) {
                     if (serverProbeBannerVisible) {
                        long perfStart = bK.load_J();
                        cA.init_V_GuiGraphicsExtract(context);

                        try {
                           serverInfoOverlay.init_V_GuiGraphicsExtract(context);
                        } catch (Throwable t) {
                           this.autism$logRenderError("probeBanner", t);
                        } finally {
                           cA.load_V_GuiGraphicsExtract(context);
                        }

                        bK.init("hud.section.probeBanner", perfStart);
                     }

                     if (overlayVisible) {
                        long perfStart = bK.load_J();

                        try {
                           overlayManager.init(context, -1, -1, 0.0F);
                        } catch (Throwable t) {
                           this.autism$logRenderError("overlays", t);
                        }

                        bK.init("hud.section.overlays", perfStart);
                     }
                  }
               }
            }
         }
      }
   }

   @Unique
   private void autism$logRenderError(String where, Throwable t) {
      long now = System.currentTimeMillis();
      if (now - autism$lastRenderErrorMs >= 5000L) {
         autism$lastRenderErrorMs = now;
         AutismClientAddon.LOG.warn("[Autism] HUD section '{}' failed; isolated to protect the UI", where, t);
      }
   }

   @Unique
   private boolean isAutismActive() {
      ModuleRuntimeManager module = ModuleRuntimeManager.init_PoolEntry();
      return module != null && module.init_Z();
   }

   @Unique
   private HudBannerSpec captureBannerSpec(AutismSessionState shared, GuiGraphicsExtractor graphics) {
      boolean blockCap = shared.pool();
      boolean entityCap = shared.frame();
      boolean attackCap = shared.count();
      boolean gbreakCap = shared.cache_Z();
      if (!blockCap && !entityCap && !attackCap && !gbreakCap) {
         return null;
      }

      String title = gbreakCap ? "GBreak Capture" : (blockCap ? "Block Capture" : (entityCap ? "Entity Capture" : "Position Capture"));
      String line1 = gbreakCap
         ? "Break a block to capture the insta-break packet. Esc = cancel"
         : (
            blockCap
               ? "Right-click a block to capture it. Esc = cancel"
               : (entityCap ? "Right-click an entity to capture it. Esc = cancel" : "Left-click to capture the target position. Esc = cancel")
         );
      String line2 = "";
      if (gbreakCap) {
         line2 = "Waiting for the block-break packet from your next block break";
      } else if (blockCap && MC.hitResult != null && MC.hitResult.getType() == Type.BLOCK && MC.level != null) {
         BlockHitResult bhr = (BlockHitResult)MC.hitResult;
         BlockPos bp = bhr.getBlockPos();
         BlockState state = MC.level.getBlockState(bp);
         if (state != this.autism$cachedBlockState) {
            this.autism$cachedBlockName = state.getBlock().getName().getString();
            this.autism$cachedBlockState = state;
         }

         line2 = "Aimed at: " + this.autism$cachedBlockName + " (" + bp.getX() + ", " + bp.getY() + ", " + bp.getZ() + ")";
      } else if (entityCap && MC.crosshairPickEntity != null && MC.crosshairPickEntity != MC.player) {
         EntityType<?> type = MC.crosshairPickEntity.getType();
         if (type != this.autism$cachedEntityType) {
            String eName = type.getDescription().getString();
            String eId = BuiltInRegistries.ENTITY_TYPE.getKey(type).toString();
            this.autism$cachedEntityLabel = eName + " (" + eId + ")";
            this.autism$cachedEntityType = type;
         }

         line2 = "Aimed at: " + this.autism$cachedEntityLabel;
      }

      int sw = cA.init_I();
      autismclient.gui.f.UiTextRenderer text = UiHelper.init(MC.font);
      int boxWidth = Math.min(sw - 16, Math.max(270, Math.max(text.init(title), Math.max(text.init(line1), line2.isEmpty() ? 0 : text.init(line2))) + 18));
      int height = StatusToastCardRenderer.init(UiHelper.init(graphics, MC.font, 0, 0), boxWidth, line1, line2);
      return new HudBannerSpec((sw - boxWidth) / 2, 0, boxWidth, height, title, line1, line2);
   }

   @Unique
   private HudBannerSpec payloadStudyBannerSpec(GuiGraphicsExtractor graphics, int topOffset) {
      String title = PayloadStudyTracker.init_Str();
      if (title.isBlank()) {
         return null;
      }

      String line1 = PayloadStudyTracker.load_Str();
      String line2 = PayloadStudyTracker.read();
      int sw = cA.init_I();
      autismclient.gui.f.UiTextRenderer text = UiHelper.init(MC.font);
      int boxWidth = Math.min(sw - 16, Math.max(276, Math.max(text.init(title), Math.max(text.init(line1), text.init(line2))) + 18));
      int height = StatusToastCardRenderer.init(UiHelper.init(graphics, MC.font, 0, 0), boxWidth, line1, line2);
      return new HudBannerSpec((sw - boxWidth) / 2, Math.max(0, topOffset), boxWidth, height, title, line1, line2);
   }

   @Unique
   private boolean hasAnyCaptureSession(AutismSessionState shared) {
      if (shared == null) {
         return false;
      } else if (!shared.chunk() && !shared.level() && !shared.count() && !shared.pool() && !shared.frame() && !shared.cache_Z()) {
         ActionEditorOverlay editor = ActionEditorOverlay.load_ActionEditorOverla();
         return editor != null && editor.push_Z_2();
      } else {
         return true;
      }
   }
}

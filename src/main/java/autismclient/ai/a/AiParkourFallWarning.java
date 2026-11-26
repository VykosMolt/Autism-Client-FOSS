package autismclient.ai.a;

import autismclient.mixin.accessor.AutismLivingEntityAccessor;
import autismclient.modules.Module;
import autismclient.modules.al;
import autismclient.util.OverlayScreenController;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.function.Consumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction.Axis;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.level.block.BedBlock;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.TrapDoorBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.VoxelShape;

public final class AiParkourFallWarning {
   private final Minecraft putAt_Minecraft;
   private final autismclient.gui.d.PromptDialogScreen putAt_RingBuffer;

   public AiParkourFallWarning(Minecraft var1, AiParkourFallWarning.Inner_DataUtil var2, Consumer<Boolean> var3, Runnable var4) {
      this.putAt_Minecraft = var1;
      ArrayList var5 = new ArrayList();
      var5.add(
         new autismclient.gui.d.PromptDialogScreen.Inner_DataUtil(
            "Cancel", autismclient.gui.f.b.CachePool.Inner_DataUtil.init, this::putAt_V
         )
      );
      var5.add(
         new autismclient.gui.d.PromptDialogScreen.Inner_DataUtil(
            "Go", autismclient.gui.f.b.CachePool.Inner_DataUtil.read, () -> this.putAt_V_Consumer_Z(var3, false)
         )
      );
      Module var6 = al.init_Module_Str("no-fall");
      if (var6 != null && !var6.isEnabled()) {
         var5.add(
            new autismclient.gui.d.PromptDialogScreen.Inner_DataUtil(
               "NoFall & Go", autismclient.gui.f.b.CachePool.Inner_DataUtil.update, () -> this.putAt_V_Consumer_Z(var3, true)
            )
         );
      }

      String var7 = putAt_Str_BufferCore_CacheHe(var2);
      this.putAt_RingBuffer = new autismclient.gui.d.PromptDialogScreen(
         "Parkour fall warning",
         List.of(
            var2.lethal() ? "This route could kill you in vanilla." : "This route can cause fall damage.",
            var7,
            "Go assumes the server has fall damage disabled."
         ),
         var5,
         var4
      );
   }

   static String putAt_Str_BufferCore_CacheHe(AiParkourFallWarning.Inner_DataUtil var0) {
      return String.format(Locale.ROOT, "About %s HP of damage · %s HP available", putAt_Str_F(var0.damage()), putAt_Str_F(var0.health()));
   }

   static String putAt_Str_F(float var0) {
      float var1 = Math.max(0.0F, var0);
      float var2 = Math.round(var1 * 2.0F) / 2.0F;
      return Math.abs(var2 - Math.rint(var2)) < 0.0010000000474974513 ? Integer.toString(Math.round(var2)) : String.format(Locale.ROOT, "%.1f", var2);
   }

   static boolean putAt(boolean var0, boolean var1, boolean var2, boolean var3, boolean var4, boolean var5) {
      return var0 || var1 || var2 || var3 || var4 || var5;
   }

   private void putAt_V_Consumer_Z(Consumer<Boolean> var1, boolean var2) {
      this.putAt_RingBuffer.init_V_Runnable(() -> var1.accept(var2));
   }

   public boolean putAt_Z() {
      return OverlayScreenController.init_Z(this.putAt_Minecraft, this.putAt_RingBuffer);
   }

   public void putAt_V() {
      this.putAt_RingBuffer.onClose();
   }

   public static AiParkourFallWarning.Inner_DataUtil putAt_BufferCore_CacheHe_Minecraft_List(Minecraft var0, List<AiPathfinder.UiHelper> var1) {
      LocalPlayer var2 = var0.player;
      double var3 = var2.getHealth() + var2.getAbsorptionAmount();
      if (!var2.getAbilities().invulnerable && !var2.hasEffect(MobEffects.SLOW_FALLING)) {
         AutismLivingEntityAccessor var5 = (AutismLivingEntityAccessor)var2;
         ClientLevel var6 = var0.level;
         AiLiveWorld var7 = new AiLiveWorld(var6, var2);
         float var8 = 0.0F;

         for (AiPathfinder.UiHelper var10 : var1) {
            if (var10.type() != AiPathfinder.Inner_SyncManager.get) {
               BlockState var11 = var0.level.getBlockState(var10.to().below());
               BlockState var12 = var0.level.getBlockState(var10.to());
               boolean var13 = var12.is(BlockTags.CLIMBABLE)
                  || var12.getBlock() instanceof TrapDoorBlock && AiPathfinder.Inner_StreamUtil.putAt_Z_BlockState_BlockState(var12, var11);
               if (!putAt(
                  var13,
                  !var12.getFluidState().isEmpty(),
                  var12.is(Blocks.COBWEB),
                  var12.is(Blocks.POWDER_SNOW),
                  var12.is(Blocks.SWEET_BERRY_BUSH),
                  var11.is(Blocks.SLIME_BLOCK)
               )) {
                  float var14 = !var11.is(Blocks.HAY_BLOCK) && !var11.is(Blocks.HONEY_BLOCK) ? (var11.getBlock() instanceof BedBlock ? 0.5F : 1.0F) : 0.2F;
                  double var15 = var10.jump() != null ? var10.jump().landY() : putAt_D_ClientLevel_BlockPos(var6, var10.to());
                  double var17 = putAt_D_ClientLevel_BlockPos(var6, var10.from());
                  double var19 = var17;
                  if (var10.jump() != null) {
                     AiJumpManeuver var21 = var10.jump().maneuver();
                     if (var21 == null) {
                        if (var10.type() == AiPathfinder.Inner_SyncManager.SLIME_BOUNCE) {
                           continue;
                        }

                        var19 += 1.253;
                     } else {
                        var var22 = var21.inputs();
                        AiPhysics.Inner_StreamUtil var23 = var21.start();
                        var19 = var23.y();
                        boolean var24 = false;

                        for (int var25 = 0; var25 < var22.size(); var25++) {
                           var23 = AiPhysics.fill(var23, (AiPhysics.Inner_DataUtil)var22.get(var25), var7);
                           int var26 = (int)Math.floor(var23.x());
                           int var27 = (int)Math.floor(var23.y());
                           int var28 = (int)Math.floor(var23.z());
                           if (!var7.state(var26, var27, var28)
                              && !var7.fill_Z(var26, var27, var28)
                              && (!var23.onGround() || !(var23.vy() > 0.1) && !var7.closeOf(var26, var27 - 1, var28))) {
                              if (!var23.onGround()) {
                                 var24 = true;
                                 var19 = Math.max(var19, var23.y());
                              } else {
                                 if (var24) {
                                    break;
                                 }

                                 var19 = var23.y();
                              }
                           } else {
                              var19 = var23.y();
                              var24 = false;
                           }
                        }
                     }
                  }

                  if (!(var19 <= var15)) {
                     int var29 = var5.autism$calculateFallDamage(var19 - var15, var14);
                     if (var29 > 0) {
                        float var30 = var5.autism$getDamageAfterMagicAbsorb(var2.damageSources().fall(), var29);
                        var8 += Math.max(0.0F, var30);
                     }
                  }
               }
            }
         }

         return new AiParkourFallWarning.Inner_DataUtil(var8, (float)var3);
      } else {
         return new AiParkourFallWarning.Inner_DataUtil(0.0F, (float)var3);
      }
   }

   private static double putAt_D_ClientLevel_BlockPos(ClientLevel var0, BlockPos var1) {
      BlockPos var2 = var1.below();
      VoxelShape var3 = var0.getBlockState(var2).getCollisionShape(var0, var2);
      return var3.isEmpty() ? var1.getY() : var2.getY() + var3.max(Axis.Y);
   }

   public record Inner_DataUtil(float damage, float health) {
      public boolean lethal() {
         return this.damage >= this.health;
      }
   }
}

package autismclient.modules;

import com.google.gson.JsonObject;
import com.mojang.authlib.GameProfile;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.UUID;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.RemotePlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

public final class TaskQueue {
   private TaskQueue() {
   }

   public static JsonObject init(Minecraft mc) throws IllegalAccessException, NoSuchMethodException, InvocationTargetException {
      JsonObject report = new JsonObject();
      if (mc.player != null && mc.level != null && mc.gameDirectory.toString().contains("autism-ai-qualification-")) {
         Module antiBot = al.init_Module_Str("antibot");
         Module aura = al.init_Module_Str("kill-aura");
         boolean antiEnabled = antiBot.isEnabled();
         boolean auraEnabled = aura.isEnabled();
         ItemStack main = mc.player.getMainHandItem().copy();
         ItemStack off = mc.player.getOffhandItem().copy();
         HitResult previousHit = mc.hitResult;
         final int[] calls = new int[]{0};
         UUID id = UUID.fromString("12345678-1234-2234-8234-123456789abc");
         var npc = new RemotePlayer(mc.level, new GameProfile(id, "Shopkeeper")) {
            @Override
            public InteractionResult interact(Player player, InteractionHand hand, Vec3 location) {
               calls[0]++;
               return InteractionResult.SUCCESS;
            }
         };
         npc.setId(-17501);
         Vec3 at = mc.player.position().add(Vec3.directionFromRotation(0.0F, mc.player.getYRot()).scale(2.0));
         npc.setPos(at);
         mc.level.addEntity(npc);

         try {
            antiBot.setEnabled(true);
            aura.setEnabled(true);
            mc.player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.SHIELD));
            mc.player.setItemInHand(InteractionHand.OFF_HAND, ItemStack.EMPTY);
            mc.hitResult = new EntityHitResult(npc, at.add(0.0, 1.0, 0.0));
            report.addProperty("detectedBot", ByteReader.init_Z(npc));
            report.addProperty("convertedToItemBlock", aa.init_Z_31());
            Method use = Minecraft.class.getDeclaredMethod("startUseItem");
            use.setAccessible(true);
            use.invoke(mc);
            report.addProperty("vanillaEntityInteractions", calls[0]);
            report.addProperty("passed", ByteReader.init_Z(npc) && calls[0] == 1 && !mc.player.isUsingItem());
         } finally {
            mc.player.stopUsingItem();
            mc.hitResult = previousHit;
            npc.discard();
            mc.player.setItemInHand(InteractionHand.MAIN_HAND, main);
            mc.player.setItemInHand(InteractionHand.OFF_HAND, off);
            aura.setEnabled(auraEnabled);
            antiBot.setEnabled(antiEnabled);
            ByteReader.load();
         }

         return report;
      } else {
         return report;
      }
   }
}

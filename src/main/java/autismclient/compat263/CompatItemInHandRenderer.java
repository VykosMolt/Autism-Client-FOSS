package autismclient.compat263;

import net.minecraft.client.Minecraft;
import net.minecraft.world.InteractionHand;

public final class CompatItemInHandRenderer {
   public static final CompatItemInHandRenderer INSTANCE = new CompatItemInHandRenderer();

   private CompatItemInHandRenderer() {
   }

   public void itemUsed(InteractionHand var1) {
      Minecraft var2 = Minecraft.getInstance();
      if (var2 != null && var2.player != null && var1 != null) {
         var2.player.itemUsed(var1);
      }
   }
}

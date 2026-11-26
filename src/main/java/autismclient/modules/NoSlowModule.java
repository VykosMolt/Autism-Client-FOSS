package autismclient.modules;

import autismclient.api.module.BoolSetting;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;

public final class NoSlowModule extends Module {
   public NoSlowModule() {
      super("no-slow", "NoSlow", ae.load_ae, "Remove selected item and movement slowdowns.");
      this.add(new BoolSetting("food", "Eating and Drinking", true).group("Items").build());
      this.add(new BoolSetting("blocking", "Blocking", true).group("Items").build());
      this.add(new BoolSetting("charging", "Charging", true).group("Items").build());
      this.add(new BoolSetting("other", "Other Items", true).group("Items").build());
      this.add(new BoolSetting("sneak", "Sneaking", false).group("Movement").build());
      this.add(new BoolSetting("crawl", "Crawling", false).group("Movement").build());
      this.add(new BoolSetting("honey", "Honey", false).group("Blocks").build());
      this.add(new BoolSetting("soul-sand", "Soul Sand", false).group("Blocks").build());
      this.add(new BoolSetting("berry-bush", "Berry Bush", false).group("Blocks").build());
   }

   private static NoSlowModule init(Entity entity) {
      return entity != null
            && entity == MC.player
            && !entity.isPassenger()
            && !aD.read_Z()
            && al.init_Module_Str("no-slow") instanceof NoSlowModule module
            && module.isEnabled()
         ? module
         : null;
   }

   public static boolean init(LocalPlayer player) {
      NoSlowModule module = init((Entity)player);
      if (module != null && player.isUsingItem()) {
         ItemStack stack = player.getUseItem();
         if (!stack.is(Items.BOW) && !stack.is(Items.CROSSBOW) && !stack.is(Items.TRIDENT)) {
            boolean var10000;
            switch (stack.getUseAnimation()) {
               case EAT:
               case DRINK:
                  var10000 = module.bool("food");
                  break;
               case BLOCK:
                  var10000 = module.bool("blocking");
                  break;
               default:
                  var10000 = module.bool("other");
            }

            return var10000;
         } else {
            return module.bool("charging");
         }
      } else {
         return false;
      }
   }

   public static boolean load(LocalPlayer player) {
      NoSlowModule module = init((Entity)player);
      return module != null && (player.isCrouching() && module.bool("sneak") || player.isVisuallyCrawling() && module.bool("crawl"));
   }

   public static boolean init(Entity entity, Block block) {
      NoSlowModule module = init(entity);
      return module != null
         && (
            block == Blocks.HONEY_BLOCK && module.bool("honey")
               || block == Blocks.SOUL_SAND && module.bool("soul-sand")
               || block == Blocks.SWEET_BERRY_BUSH && module.bool("berry-bush")
         );
   }
}

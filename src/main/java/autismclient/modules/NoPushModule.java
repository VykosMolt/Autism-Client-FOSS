package autismclient.modules;

import autismclient.api.module.BoolSetting;
import net.minecraft.world.entity.Entity;

public final class NoPushModule extends Module {
   public NoPushModule() {
      super("no-push", "NoPush", ae.load_ae, "Prevent blocks or entities from pushing you.");
      this.add(new BoolSetting("blocks", "Blocks", true).description("Prevent escape pushes from inside blocks").build());
      this.add(new BoolSetting("entities", "Entities", false).description("Prevent collision pushes from entities").build());
   }

   public static boolean cancelsPush(Entity receiving, String option) {
      return receiving != null
         && receiving == MC.player
         && !MC.player.isAutoSpinAttack()
         && !aD.read_Z()
         && al.init_Module_Str("no-push") instanceof NoPushModule module
         && module.isEnabled()
         && module.bool(option);
   }
}

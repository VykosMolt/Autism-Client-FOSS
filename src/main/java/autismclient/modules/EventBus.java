package autismclient.modules;

import autismclient.api.module.ChoiceSetting;
import autismclient.api.module.DoubleSetting;

public final class EventBus extends Module {
   public EventBus() {
      super("aspect-ratio", "AspectRatio", ae.update, "Change the world projection's width.");
      this.add(new ChoiceSetting("preset", "Preset", "Custom", "Custom", "16:9", "16:10", "4:3").build());
      this.add(new DoubleSetting("ratio", "Ratio", 1.7777777777777777, 0.1, 5.0, 0.01).visibleWhen(() -> "Custom".equals(this.choice("preset"))).build());
   }

   public static float init(float width, float height) {
      if (height > 0.0F
         && !aD.read_Z()
         && al.init_Module_Str("aspect-ratio") instanceof EventBus module
         && module.isEnabled()) {
         double var10000;
         switch (module.choice("preset")) {
            case "16:9":
               var10000 = 1.7777777777777777;
               break;
            case "16:10":
               var10000 = 1.6;
               break;
            case "4:3":
               var10000 = 1.3333333333333333;
               break;
            default:
               var10000 = module.decimal("ratio");
         }

         double ratio = var10000;
         return Double.isFinite(ratio) ? (float)(Math.clamp(ratio, 0.1, 5.0) * height) : width;
      } else {
         return width;
      }
   }
}

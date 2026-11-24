package autismclient.api.macro;

import autismclient.util.macro.MacroAction;
import net.minecraft.client.Minecraft;

public interface ContextMacroAction extends MacroAction {
   void run(MacroExecutionContext var1);

   @Override
   default void execute(Minecraft mc) {
   }
}

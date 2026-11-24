package autismclient.mixin;

import autismclient.modules.as;
import net.minecraft.client.gui.components.ChatComponent;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(ChatComponent.class)
public class AutismChatComponentCensorMixin {
   @ModifyVariable(method = "addClientSystemMessage", at = @At("HEAD"), argsOnly = true, ordinal = 0)
   private Component autism$censorClientSystemChat(Component component) {
      return as.init_Component(component);
   }

   @ModifyVariable(method = "addServerSystemMessage", at = @At("HEAD"), argsOnly = true, ordinal = 0)
   private Component autism$censorServerSystemChat(Component component) {
      return as.load(component);
   }

   @ModifyVariable(method = "addPlayerMessage", at = @At("HEAD"), argsOnly = true, ordinal = 0)
   private Component autism$censorPlayerChat(Component component) {
      return as.load(component);
   }

   @ModifyVariable(method = "addMessage", at = @At("HEAD"), argsOnly = true, ordinal = 0, require = 0)
   private Component autism$censorPrivateChatFallback(Component component) {
      return as.load(component);
   }
}

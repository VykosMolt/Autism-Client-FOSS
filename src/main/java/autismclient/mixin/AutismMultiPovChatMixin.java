package autismclient.mixin;

import autismclient.util.g.RingBuffer;
import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import net.minecraft.client.gui.components.ChatComponent;
import net.minecraft.client.gui.components.ChatComponent.State;
import net.minecraft.client.multiplayer.chat.GuiMessageSource;
import net.minecraft.client.multiplayer.chat.GuiMessageTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MessageSignature;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(ChatComponent.class)
public class AutismMultiPovChatMixin {
   @WrapMethod(method = "addMessage")
   private void autism$wrapMainChatMessage(
      Component content, MessageSignature signature, GuiMessageSource source, GuiMessageTag tag, Operation<Void> original
   ) {
      ChatComponent self = (ChatComponent)(Object)this;
      State state = RingBuffer.init(self);
      try {
         original.call(content, signature, source, tag);
      } finally {
         RingBuffer.init(self, state);
      }
   }

   @WrapMethod(method = "deleteMessage")
   private void autism$wrapMainChatDelete(MessageSignature signature, Operation<Void> original) {
      ChatComponent self = (ChatComponent)(Object)this;
      State state = RingBuffer.init(self);
      try {
         original.call(signature);
      } finally {
         RingBuffer.init(self, state);
      }
   }

   @WrapMethod(method = "clearMessages")
   private void autism$wrapMainChatClear(boolean clearHistory, Operation<Void> original) {
      ChatComponent self = (ChatComponent)(Object)this;
      State state = RingBuffer.init(self);
      try {
         original.call(clearHistory);
      } finally {
         RingBuffer.init(self, state);
      }
   }
}

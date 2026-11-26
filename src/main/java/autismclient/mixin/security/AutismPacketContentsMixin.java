package autismclient.mixin.security;

import autismclient.protector.ConnectionPrivacyRegistry;
import autismclient.protector.PacketOriginDuck;
import autismclient.protector.PacketDecodeOriginStack;
import net.minecraft.network.chat.contents.NbtContents;
import net.minecraft.network.chat.contents.ObjectContents;
import net.minecraft.network.chat.contents.ScoreContents;
import net.minecraft.network.chat.contents.SelectorContents;
import net.minecraft.network.chat.contents.PlainTextContents.LiteralContents;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin({LiteralContents.class, NbtContents.class, SelectorContents.class, ScoreContents.class, ObjectContents.class})
public class AutismPacketContentsMixin implements PacketOriginDuck {
   @Unique
   private boolean autism$fromPacket;
   @Unique
   private ConnectionPrivacyRegistry.ConnectionSession autism$packetOrigin;

   @Override
   public void autism$setFromPacket() {
      this.autism$setFromPacket(PacketDecodeOriginStack.init_BufferHelper_Strea());
   }

   @Override
   public void autism$setFromPacket(ConnectionPrivacyRegistry.ConnectionSession context) {
      if (!this.autism$fromPacket) {
         this.autism$packetOrigin = context;
      }

      this.autism$fromPacket = true;
   }

   @Override
   public ConnectionPrivacyRegistry.ConnectionSession autism$packetOrigin() {
      return this.autism$packetOrigin;
   }

   @Override
   public boolean autism$isFromPacket() {
      return this.autism$fromPacket;
   }
}

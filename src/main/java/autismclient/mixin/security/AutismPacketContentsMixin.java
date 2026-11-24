package autismclient.mixin.security;

import autismclient.g.BufferHelper;
import autismclient.g.NetHelper;
import autismclient.g.TokenStore;
import net.minecraft.network.chat.contents.NbtContents;
import net.minecraft.network.chat.contents.ObjectContents;
import net.minecraft.network.chat.contents.ScoreContents;
import net.minecraft.network.chat.contents.SelectorContents;
import net.minecraft.network.chat.contents.PlainTextContents.LiteralContents;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin({LiteralContents.class, NbtContents.class, SelectorContents.class, ScoreContents.class, ObjectContents.class})
public class AutismPacketContentsMixin implements NetHelper {
   @Unique
   private boolean autism$fromPacket;
   @Unique
   private BufferHelper.Inner_StreamUtil autism$packetOrigin;

   @Override
   public void autism$setFromPacket() {
      this.autism$setFromPacket(TokenStore.init_BufferHelper_Strea());
   }

   @Override
   public void autism$setFromPacket(BufferHelper.Inner_StreamUtil context) {
      if (!this.autism$fromPacket) {
         this.autism$packetOrigin = context;
      }

      this.autism$fromPacket = true;
   }

   @Override
   public BufferHelper.Inner_StreamUtil autism$packetOrigin() {
      return this.autism$packetOrigin;
   }

   @Override
   public boolean autism$isFromPacket() {
      return this.autism$fromPacket;
   }
}

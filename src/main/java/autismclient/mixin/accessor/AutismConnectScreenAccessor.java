package autismclient.mixin.accessor;

import io.netty.channel.ChannelFuture;
import net.minecraft.client.gui.screens.ConnectScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.Connection;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(ConnectScreen.class)
public interface AutismConnectScreenAccessor {
   @Accessor("aborted")
   boolean autism$aborted();

   @Accessor("aborted")
   void autism$setAborted(boolean var1);

   @Accessor("connection")
   Connection autism$connection();

   @Accessor("channelFuture")
   ChannelFuture autism$channelFuture();

   @Accessor("channelFuture")
   void autism$setChannelFuture(ChannelFuture var1);

   @Accessor("parent")
   Screen autism$parent();
}

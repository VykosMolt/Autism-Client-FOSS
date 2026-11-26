package autismclient.mixin.security;

import autismclient.g.BufferHelper;
import autismclient.g.ByteReader;
import autismclient.g.TokenStore;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.authlib.GameProfile;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.DecoderException;
import java.util.List;
import java.util.UUID;
import net.minecraft.network.Connection;
import net.minecraft.network.PacketDecoder;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.game.ClientboundSetEntityDataPacket;
import net.minecraft.network.protocol.login.ClientboundLoginFinishedPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(PacketDecoder.class)
public class AutismProtectorPacketDecoderMixin {
   @WrapOperation(
      method = "decode",
      at = @At(value = "INVOKE", target = "Lnet/minecraft/network/codec/StreamCodec;decode(Ljava/lang/Object;)Ljava/lang/Object;")
   )
   private Object autism$wrapDecode(
      StreamCodec instance, Object buffer, Operation<Object> original, @Local(argsOnly = true) ChannelHandlerContext channelContext
   ) {
      BufferHelper.Inner_StreamUtil context = autism$connectionContext(channelContext);
      boolean modded = context != null && context.init_BufferHelper_Cache() == BufferHelper.Inner_CachePool.MODDED;
      TokenStore.init(context);
      ByteReader.init(modded);

      try {
         return autism$decodeCompatibly(instance, buffer, original, modded);
      } finally {
         ByteReader.init();
         TokenStore.init(false);
      }
   }

   @Unique
   private static BufferHelper.Inner_StreamUtil autism$connectionContext(ChannelHandlerContext channelContext) {
      if (channelContext != null && channelContext.channel() != null) {
         Connection connection = channelContext.channel().pipeline().get(Connection.class);
         return BufferHelper.init_BufferHelper_Strea(connection);
      } else {
         return null;
      }
   }

   @Unique
   private static Object autism$decodeCompatibly(StreamCodec instance, Object buffer, Operation<Object> original, boolean modded) {
      if (buffer instanceof ByteBuf byteBuf) {
         int startIndex = byteBuf.readerIndex();

         try {
            return original.call(instance, buffer);
         } catch (DecoderException var10) {
            DecoderException decodeFailure = var10;
            if (!modded) {
               throw var10;
            }

            if (autism$isMissingLoginSessionId(var10)) {
               byteBuf.readerIndex(startIndex);

               try {
                  autism$readVarInt(byteBuf);
                  GameProfile profile = ByteBufCodecs.GAME_PROFILE.decode(byteBuf);
                  if (byteBuf.isReadable()) {
                     byteBuf.readerIndex(startIndex);
                     throw decodeFailure;
                  } else {
                     return new ClientboundLoginFinishedPacket(profile, UUID.randomUUID());
                  }
               } catch (RuntimeException fallbackFailure) {
                  byteBuf.readerIndex(startIndex);
                  throw var10;
               }
            } else if (autism$isTruncatedEntityData(var10)) {
               byteBuf.readerIndex(startIndex);

               try {
                  autism$readVarInt(byteBuf);
                  int entityId = autism$readVarInt(byteBuf);
                  byteBuf.skipBytes(byteBuf.readableBytes());
                  return new ClientboundSetEntityDataPacket(entityId, List.of());
               } catch (RuntimeException fallbackFailure) {
                  byteBuf.readerIndex(startIndex);
                  throw var10;
               }
            } else {
               throw var10;
            }
         }
      } else {
         return original.call(instance, buffer);
      }
   }

   @Unique
   private static boolean autism$isTruncatedEntityData(DecoderException failure) {
      String message = failure.getMessage();
      if (message != null && message.contains("clientbound/minecraft:set_entity_data")) {
         for (Throwable cause = failure; cause != null; cause = cause.getCause()) {
            if (cause instanceof IndexOutOfBoundsException) {
               return true;
            }
         }

         return false;
      } else {
         return false;
      }
   }

   @Unique
   private static boolean autism$isMissingLoginSessionId(DecoderException failure) {
      String message = failure.getMessage();
      if (message != null && message.contains("clientbound/minecraft:login_finished")) {
         for (Throwable cause = failure; cause != null; cause = cause.getCause()) {
            if (cause instanceof IndexOutOfBoundsException) {
               return true;
            }
         }

         return false;
      } else {
         return false;
      }
   }

   @Unique
   private static int autism$readVarInt(ByteBuf buffer) {
      int value = 0;

      for (int byteIndex = 0; byteIndex < 5; byteIndex++) {
         int current = buffer.readByte();
         value |= (current & 127) << byteIndex * 7;
         if ((current & 128) == 0) {
            return value;
         }
      }

      throw new DecoderException("VarInt is too big");
   }
}

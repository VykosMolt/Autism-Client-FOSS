package autismclient.mixin;

import autismclient.modules.aD;
import autismclient.util.AutismPayloadSupport;
import autismclient.util.PacketCaptureState;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload.Type;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(targets = "net.minecraft.network.protocol.common.custom.CustomPacketPayload$1")
public abstract class AutismCustomPayloadCodecMixin {
   @Unique
   private static final ThreadLocal<AutismCustomPayloadCodecMixin.CaptureCursor> AUTISM_CAPTURE_CURSOR = ThreadLocal.withInitial(
      AutismCustomPayloadCodecMixin.CaptureCursor::new
   );

   @Inject(
      method = "encode(Lnet/minecraft/network/FriendlyByteBuf;Lnet/minecraft/network/protocol/common/custom/CustomPacketPayload;)V",
      at = @At("HEAD"),
      cancellable = true
   )
   private void autism$encodeRawCustomPacketPayload(FriendlyByteBuf buf, CustomPacketPayload payload, CallbackInfo ci) {
      if (!aD.read_Z()) {
         long captureState = PacketCaptureState.load_J();
         boolean capturePayload = PacketCaptureState.load(captureState);
         if (capturePayload && buf != null) {
            AutismCustomPayloadCodecMixin.CaptureCursor cursor = AUTISM_CAPTURE_CURSOR.get();
            cursor.encodeStartIndex = buf.writerIndex();
            cursor.encodeState = captureState;
         }

         boolean isRaw = payload instanceof AutismPayloadSupport.RawCustomPacketPayload;
         if (isRaw || capturePayload) {
            byte[] rememberedBytes = AutismPayloadSupport.init_BArr(payload);
            if (rememberedBytes != null || isRaw) {
               Type<?> type = payload.type();
               if (type != null && type.id() != null) {
                  byte[] bytes = rememberedBytes != null ? rememberedBytes : ((AutismPayloadSupport.RawCustomPacketPayload)payload).bytes();
                  buf.writeIdentifier(type.id());
                  if (bytes.length > 0) {
                     buf.writeBytes(bytes);
                  }

                  ci.cancel();
               }
            }
         }
      }
   }

   @Inject(method = "encode(Lnet/minecraft/network/FriendlyByteBuf;Lnet/minecraft/network/protocol/common/custom/CustomPacketPayload;)V", at = @At("TAIL"))
   private void autism$captureEncodedCustomPacketPayload(FriendlyByteBuf buf, CustomPacketPayload payload, CallbackInfo ci) {
      AutismCustomPayloadCodecMixin.CaptureCursor cursor = AUTISM_CAPTURE_CURSOR.get();
      int recordedStart = cursor.encodeStartIndex;
      long recordedState = cursor.encodeState;
      cursor.encodeStartIndex = -1;
      cursor.encodeState = 0L;
      if (!aD.read_Z()) {
         long captureState = PacketCaptureState.load_J();
         if (PacketCaptureState.load(captureState)) {
            int startIndex = recordedState == captureState ? recordedStart : -1;
            if (payload != null && buf != null && startIndex >= 0) {
               int end = buf.writerIndex();
               if (end > startIndex) {
                  byte[] encodedBytes = new byte[end - startIndex];
                  buf.getBytes(startIndex, encodedBytes);
                  AutismPayloadSupport.init_V(payload, autism$payloadChannel(payload), encodedBytes);
               }
            }
         }
      }
   }

   @Inject(method = "decode(Lnet/minecraft/network/FriendlyByteBuf;)Lnet/minecraft/network/protocol/common/custom/CustomPacketPayload;", at = @At("HEAD"))
   private void autism$captureDecodeStart(FriendlyByteBuf buf, CallbackInfoReturnable<CustomPacketPayload> cir) {
      if (!aD.read_Z()) {
         long captureState = PacketCaptureState.load_J();
         if (PacketCaptureState.load(captureState) && buf != null) {
            AutismCustomPayloadCodecMixin.CaptureCursor cursor = AUTISM_CAPTURE_CURSOR.get();
            cursor.decodeStartIndex = buf.readerIndex();
            cursor.decodeState = captureState;
         }
      }
   }

   @Inject(method = "decode(Lnet/minecraft/network/FriendlyByteBuf;)Lnet/minecraft/network/protocol/common/custom/CustomPacketPayload;", at = @At("RETURN"))
   private void autism$captureDecodedCustomPacketPayload(FriendlyByteBuf buf, CallbackInfoReturnable<CustomPacketPayload> cir) {
      AutismCustomPayloadCodecMixin.CaptureCursor cursor = AUTISM_CAPTURE_CURSOR.get();
      int recordedStart = cursor.decodeStartIndex;
      long recordedState = cursor.decodeState;
      cursor.decodeStartIndex = -1;
      cursor.decodeState = 0L;
      if (!aD.read_Z()) {
         long captureState = PacketCaptureState.load_J();
         if (PacketCaptureState.load(captureState)) {
            int startIndex = recordedState == captureState ? recordedStart : -1;
            CustomPacketPayload payload = cir.getReturnValue();
            if (payload != null && buf != null && startIndex >= 0) {
               int end = buf.readerIndex();
               if (end > startIndex) {
                  byte[] encodedBytes = new byte[end - startIndex];
                  buf.getBytes(startIndex, encodedBytes);
                  AutismPayloadSupport.init_V(payload, autism$payloadChannel(payload), encodedBytes);
               }
            }
         }
      }
   }

   @Unique
   private static String autism$payloadChannel(CustomPacketPayload payload) {
      try {
         Type<?> type = payload == null ? null : payload.type();
         if (type != null && type.id() != null) {
            return type.id().toString();
         }
      } catch (Throwable var2) {
      }

      return "";
   }

   @Unique
   private static final class CaptureCursor {
      private int encodeStartIndex = -1;
      private int decodeStartIndex = -1;
      private long encodeState;
      private long decodeState;
   }
}

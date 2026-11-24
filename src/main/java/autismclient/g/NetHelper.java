package autismclient.g;

public interface NetHelper {
   void autism$setFromPacket();

   default void autism$setFromPacket(BufferHelper.Inner_StreamUtil context) {
      this.autism$setFromPacket();
   }

   default BufferHelper.Inner_StreamUtil autism$packetOrigin() {
      return null;
   }

   default boolean autism$isFromPacket() {
      return false;
   }

   default void autism$setSilent() {
   }
}

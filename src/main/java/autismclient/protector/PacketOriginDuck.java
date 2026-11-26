package autismclient.protector;

public interface PacketOriginDuck {
   void autism$setFromPacket();

   default void autism$setFromPacket(ConnectionPrivacyRegistry.Inner_StreamUtil context) {
      this.autism$setFromPacket();
   }

   default ConnectionPrivacyRegistry.Inner_StreamUtil autism$packetOrigin() {
      return null;
   }

   default boolean autism$isFromPacket() {
      return false;
   }

   default void autism$setSilent() {
   }
}

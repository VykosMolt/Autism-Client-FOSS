package autismclient.protector;

public interface PacketOriginDuck {
   void autism$setFromPacket();

   default void autism$setFromPacket(ConnectionPrivacyRegistry.ConnectionSession context) {
      this.autism$setFromPacket();
   }

   default ConnectionPrivacyRegistry.ConnectionSession autism$packetOrigin() {
      return null;
   }

   default boolean autism$isFromPacket() {
      return false;
   }

   default void autism$setSilent() {
   }
}

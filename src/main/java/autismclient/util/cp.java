package autismclient.util;

public interface cp {
   void autism$closeWithPacket();

   void autism$closeWithoutPacket();

   void autism$desync();

   default void autism$closeWithPacket(boolean notify) {
      this.autism$closeWithPacket();
   }

   default void autism$closeWithoutPacket(boolean notify) {
      this.autism$closeWithoutPacket();
   }

   default void autism$desync(boolean notify) {
      this.autism$desync();
   }
}

package autismclient.util.sync;

public enum LanSyncPacketType {
   SEARCH_REQUEST(1),
   SESSION(2),
   JOIN(3),
   REQUEST_CLIENT_LIST(4),
   CLIENT_LIST(5),
   LEAVE(6),
   REQUEST_MACRO(16),
   MACRO_DATA(17),
   MACRO_DELETE(23),
   MACRO_LIST(18),
   QUEUE_SYNC(19),
   PRESET_DATA(20),
   CHAT_MESSAGE(21),
   TCP_COMMAND_ACK(28),
   PLAYER_OFFSETS(30),
   CLIENT_OFFSET_UPDATE(31),
   PREPARE_EXECUTION(33),
   CLIENT_READY(34),
   EXECUTE_NOW(35),
   GO(40),
   HEARTBEAT(42),
   SYNC_STATE_UPDATE(43),
   REQUEST_SYNC(44),
   OFFSET_SYNC(45),
   MACRO_STEP_PROGRESS(46),
   MACRO_DATA_CHUNK(50),
   MACRO_ASSIGNMENT_SYNC(51);

   private final int init_I;

   LanSyncPacketType(int id) {
      this.init_I = id;
   }

   public int id() {
      return this.init_I;
   }

   public static LanSyncPacketType byId(int id) {
      for (LanSyncPacketType type : values()) {
         if (type.init_I == id) {
            return type;
         }
      }

      return null;
   }
}

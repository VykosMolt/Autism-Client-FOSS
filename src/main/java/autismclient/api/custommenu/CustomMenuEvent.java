package autismclient.api.custommenu;

public record CustomMenuEvent(CustomMenuEvent.Type type, CustomMenuSnapshot snapshot) {
   public static final CustomMenuEvent NONE = new CustomMenuEvent(CustomMenuEvent.Type.NONE, null);
   public static final CustomMenuEvent CLEAR = new CustomMenuEvent(CustomMenuEvent.Type.CLEAR, null);

   public static CustomMenuEvent open(CustomMenuSnapshot snapshot) {
      return new CustomMenuEvent(CustomMenuEvent.Type.OPEN, snapshot);
   }

   public enum Type {
      NONE,
      OPEN,
      CLEAR;
   }
}

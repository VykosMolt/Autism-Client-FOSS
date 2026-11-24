package autismclient.gui.f.b;

public final class UiHelper {
   private UiHelper() {
   }

   public static void init(
      autismclient.gui.f.SyncManager context, autismclient.gui.f.NetHelper bounds, String text, String placeholder, boolean focused, int cursor
   ) {
      e.init(context, bounds, text, placeholder, focused, cursor);
   }

   public static void init(
      autismclient.gui.f.SyncManager context,
      autismclient.gui.f.NetHelper bounds,
      String text,
      String placeholder,
      boolean focused,
      int cursor,
      int selectionStart,
      int selectionEnd
   ) {
      e.init(context, bounds, text, placeholder, focused, cursor, selectionStart, selectionEnd);
   }

   public static void init(autismclient.gui.f.SyncManager context, autismclient.gui.f.NetHelper bounds, boolean enabled, boolean hovered) {
      g.init(context, bounds, enabled, hovered);
   }

   public static void init(autismclient.gui.f.SyncManager context, autismclient.gui.f.NetHelper bounds, boolean enabled, boolean hovered, String animationKey) {
      g.init(context, bounds, enabled, hovered, animationKey);
   }

   public static void init(autismclient.gui.f.SyncManager context, autismclient.gui.f.NetHelper bounds, String label, boolean hovered, boolean open) {
      RefQueue.init_V_SyncManager(context, bounds, label, hovered, open);
   }

   public static void load(autismclient.gui.f.SyncManager context, autismclient.gui.f.NetHelper bounds, String label, boolean hovered, boolean open) {
      RefQueue.load(context, bounds, label, hovered, open);
   }
}

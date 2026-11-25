package autismclient.gui.f.a;

import net.minecraft.resources.Identifier;

public final class DataUtil {
   public static final Identifier init = Identifier.fromNamespaceAndPath("autismclient", "pack_ui_body");
   public static final Identifier load = Identifier.fromNamespaceAndPath("autismclient", "pack_ui_label");
   public static final Identifier read = Identifier.fromNamespaceAndPath("autismclient", "pack_ui_title");
   public static final Identifier write = init("keybinds");
   public static final Identifier update = init("lansync");
   public static final Identifier sync = init("macros");
   public static final Identifier flush = init("fabricator");
   public static final Identifier close = init("filter");
   public static final Identifier open = init("packetlogger");
   public static final Identifier poll = init("packetqeditor");
   public static final Identifier push = init("serverinfo");
   public static final Identifier pop = init("mainmenucategory");
   public static final Identifier tick = init("packetcategory");
   public static final Identifier run = init("screencategory");
   public static final Identifier get = init("chatcategory");
   public static final Identifier add = init("profiles");
   public static final Identifier clear = init("schematics");
   public static final Identifier reset = init("cat_combat");
   public static final Identifier start = init("cat_movement");
   public static final Identifier stop = init("cat_player");
   public static final Identifier next = init("cat_misc");
   public static final Identifier prev = init("cat_render");
   public static final Identifier valid = init("cat_addon");

   private DataUtil() {
   }

   private static Identifier init(String name) {
      return Identifier.fromNamespaceAndPath("autismclient", "textures/gui/vanillaui/icons/" + name + ".png");
   }
}

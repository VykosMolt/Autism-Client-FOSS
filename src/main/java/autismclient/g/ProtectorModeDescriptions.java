package autismclient.g;

public final class ProtectorModeDescriptions {
   private ProtectorModeDescriptions() {
   }

   public static String init(boolean vanilla, boolean bypass) {
      return bypass ? "Packs: Bypass" : "Packs: Normal";
   }

   public static String init(boolean vanilla) {
      return vanilla
         ? "Vanilla: uses a vanilla brand and blocks remote mod channels. Your Via protocol and resource-pack choices still apply. Single-player keeps local mod networking. Translated protocols and pack bypass can differ from a stock client."
         : "Modded: uses your selected Via protocol. Choose Normal to use server textures, or Bypass to skip applying them.";
   }

   public static String load(boolean vanilla, boolean bypass) {
      return bypass
         ? "Isolated bypass: real download, then a real reload with a neutral generated pack instead of applying server resources. Auto-deny declines without downloading."
         : "Normal: use the server's textures after the normal prompt, download and reload. Local mod translation/keybind names stay protected. Choose Bypass to skip applying server textures; Auto-deny declines without downloading.";
   }
}

package autismclient.api;

public abstract class AutismAddon {
   public String name = "";
   public String authors = "";
   public int color = -1;

   public abstract int apiVersion();

   public void onRegisterCategories() {
   }

   public abstract void onInitialize();

   public void onUnload() {
   }

   public abstract String getPackage();
}

package autismclient.util;

import net.minecraft.client.gui.GuiGraphicsExtractor;

public interface AutismOverlay {
   void init_V_GuiGraphicsExtract_15(GuiGraphicsExtractor var1, int var2, int var3, float var4);

   boolean init_Z_D_3(double var1, double var3, int var5);

   boolean load_Z_D_3(double var1, double var3, int var5);

   boolean init_Z_D(double var1, double var3, int var5, double var6, double var8);

   boolean init_Z_D_2(double var1, double var3, double var5);

   boolean init_Z_I(int var1, int var2, int var3);

   boolean init_Z_C_I(char var1, int var2);

   boolean sync_Z();

   void init_V_Z(boolean var1);

   default boolean a__Z() {
      return OverlayManager.init_bk().init_Z(this);
   }

   boolean init_Z_D_D(double var1, double var3);

   boolean load_Z_D_D(double var1, double var3);

   default int b__I() {
      return 0;
   }

   default boolean flush_Z() {
      return false;
   }

   default void write_V_Z(boolean collapsed) {
   }

   default void start_V() {
      this.write_V_Z(!this.flush_Z());
   }

   default boolean write_Z() {
      return false;
   }

   default void push_V() {
   }

   default boolean e_() {
      return false;
   }

   default String init_Str() {
      return this.getClass().getSimpleName();
   }

   default AutismOverlay.Inner_DataUtil init_cX_DataUtil() {
      return AutismOverlay.Inner_DataUtil.HOST_SCREEN;
   }

   default boolean f_() {
      return false;
   }

   default boolean update_Z() {
      return false;
   }

   default OverlayBounds init_cH() {
      return new OverlayBounds(0, 0, this.init_I(), this.load_I(), this.sync_Z(), this.flush_Z());
   }

   default void init_V_cH(OverlayBounds bounds) {
   }

   default int init_I() {
      return 240;
   }

   default int load_I() {
      return 140;
   }

   default boolean read_Z() {
      return false;
   }

   default boolean read_Z_D_D(double mouseX, double mouseY) {
      return this.read_Z() && this.sync_Z() && !this.flush_Z() ? autismclient.gui.f.b.ValueSink.init(this.init_cH(), mouseX, mouseY) : false;
   }

   default void init_V_I_I(int width, int height) {
      OverlayBounds b = this.init_cH();
      this.init_V_cH(new OverlayBounds(b.init_I, b.load_I, Math.max(this.init_I(), width), Math.max(this.load_I(), height), b.init_Z, b.load_Z));
   }

   default void tick_V() {
   }

   default void run_V() {
   }

   default boolean load_Z() {
      return false;
   }

   default void a__V() {
      AutismSessionState.init_ck().init_V_Str_cH(this.init_Str(), this.init_cH());
   }

   default void b__V() {
      OverlayBounds layout = AutismSessionState.init_ck().init_cH_Str(this.init_Str());
      if (layout != null) {
         this.init_V_cH(layout);
      }
   }

   enum Inner_DataUtil {
      HOST_SCREEN,
      MODULE_MENU,
      CONTAINER_GUI,
      BACKGROUND_STATUS;
   }
}

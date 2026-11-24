package autismclient.util.g;

import autismclient.api.custommenu.CustomMenuSnapshot;
import autismclient.api.custommenu.CustomMenuSubmission;
import autismclient.api.custommenu.CustomMenuSubmitResult;
import autismclient.util.AutismLiteVariant;
import autismclient.util.macro.A;
import autismclient.util.macro.I;
import autismclient.util.macro.aA;
import autismclient.util.macro.aD;
import autismclient.util.macro.aM;
import autismclient.util.macro.aQ;
import autismclient.util.macro.ac;
import autismclient.util.macro.ah;
import autismclient.util.macro.ai;
import autismclient.util.macro.ao;
import autismclient.util.macro.y;
import java.util.List;
import java.util.Map;
import net.minecraft.client.player.RemotePlayer;
import net.minecraft.world.phys.Vec3;

public interface EventBus extends b {
   boolean init_Z();

   default boolean load_Z() {
      return this.init_Z();
   }

   boolean read_Z();

   default void init_V_Str(String note) {
   }

   default CustomMenuSnapshot init_CustomMenuSnapshot() {
      return null;
   }

   default CustomMenuSubmitResult init_CustomMenuSubmitRe_CustomMenuSnapshot_CustomMenuSubmissi(CustomMenuSnapshot snapshot, CustomMenuSubmission submission) {
      return CustomMenuSubmitResult.failure("Custom menus are unavailable");
   }

   default String init_Str_Str_Map(String template, Map<String, String> macroVariables) {
      return template == null ? "" : template;
   }

   default String init_Str() {
      return "";
   }

   default String load_Str() {
      return "";
   }

   default String read_Str() {
      return "";
   }

   default FileCache.Inner_StreamUtil init_FileCache_StreamUt() {
      boolean ready = this.init_Z();
      return new FileCache.Inner_StreamUtil(this.read_Str(), false, ready, ready ? 1 : 0, ready, ready, ready ? 2 : 0, false, false, "");
   }

   default String init_Str_Str(String address) {
      return "Joining is unavailable on this host";
   }

   default void init_V() {
   }

   default void load_V() {
   }

   default void load_V_Str(String reason) {
      this.read_V_Str(reason);
   }

   default String write_Str() {
      return "";
   }

   float init_F();

   float load_F();

   int init_I();

   boolean write_Z();

   double init_D();

   double load_D();

   double read_D();

   String update_Str();

   String sync_Str();

   int load_I();

   String flush_Str();

   boolean update_Z();

   long init_J();

   int init_I_Str(String var1);

   int init_I_ArrayUtil(autismclient.util.macro.ArrayUtil var1);

   int read_I();

   boolean init_Z_I(int var1);

   boolean sync_Z();

   String close_Str();

   boolean init_Z_ArrayUtil(autismclient.util.macro.ArrayUtil var1);

   float read_F();

   int[] init_IArr();

   int[] init_IArr_I(int var1);

   int[] init_IArr_ArrayUtil(autismclient.util.macro.ArrayUtil var1);

   long load_J();

   int write_I();

   long read_J();

   List<String> init_List_J(long var1);

   boolean init(List<String> var1, boolean var2, boolean var3, double var4, double var6, double var8, double var10);

   void init_V_Z(boolean var1);

   long write_J();

   boolean init_Z_J_List(long var1, List<String> var3);

   boolean init_Z_ac(ac var1, String var2, String var3, String var4, String var5);

   void load_V_Z(boolean var1);

   long update_J();

   boolean init_Z_J(long var1, List<String> var3, boolean var4, double var5);

   boolean init_Z_J_aM(long var1, aM var3);

   boolean init_Z_ArrayUtil_Z(autismclient.util.macro.ArrayUtil var1, boolean var2);

   String init_Str_TaskQueue_ArrayUtil(autismclient.util.macro.TaskQueue var1, autismclient.util.macro.ArrayUtil var2);

   List<String> init_List_Z(boolean var1);

   int load_I_Str(String var1);

   List<String> init_List_I(int var1);

   List<autismclient.util.macro.TaskQueue.Inner_JsonUtil> init_List();

   long sync_J();

   default EventBus.Inner_DataUtil init_EventBus_DataUtil() {
      return EventBus.Inner_DataUtil.init;
   }

   void init_V_Str_Str(String var1, String var2);

   boolean init_Z_I(int var1, int var2, int var3, List<String> var4, boolean var5, boolean var6);

   String[] init_StrArr_aD(aD var1);

   boolean init_Z_aD_StrArr(aD var1, String[] var2);

   List<int[]> init_List_MapStore(autismclient.util.macro.MapStore var1);

   List<int[]> init_List_ah(ah var1);

   List<int[]> init_List_ai(ai var1);

   List<int[]> init_List_I_2(I var1);

   List<int[]> init_List_StateCache(autismclient.util.macro.StateCache var1);

   void init_V_I(int var1, int var2, int var3);

   boolean init_Z_y(y var1);

   int init_I_List(List<String> var1, String var2, boolean var3, boolean var4, int var5);

   boolean init_Z_aA(aA var1);

   boolean init_Z_Z_Z(boolean var1, boolean var2);

   boolean flush_Z();

   boolean close_Z();

   int init_I_aQ_J(aQ var1, long var2);

   void read_V();

   int read_I_Str(String var1);

   double[] init_DArr_I(int var1);

   String init_Str_Str_Str(String var1, String var2);

   void init_V_ao_DataUtil_Z(ao.Inner_DataUtil var1, boolean var2);

   String load_Str_Str(String var1);

   boolean init_Z_Str(String var1);

   void write_V();

   void read_V_Str(String var1);

   float write_F();

   void init_V_F_F(float var1, float var2);

   void init_V_D(double var1, double var3, long var5);

   int init_I_D(double var1, double var3, double var5, int var7, boolean var8);

   boolean open_Z();

   long flush_J();

   default boolean poll_Z() {
      return false;
   }

   void read_V_Z(boolean var1);

   void write_V_Z(boolean var1);

   boolean push_Z();

   void update_V();

   String init_Str_I_Z(int var1, boolean var2);

   String init_Str_I_2(int var1, int var2, int var3, String var4);

   String load(int var1, int var2, int var3, String var4);

   @Override
   default b.Inner_StreamUtil init_b_StreamUtil() {
      return AutismLiteVariant.enabled() ? null : c.init_b_StreamUtil(this);
   }

   @Override
   default b.Inner_DataUtil init_b_DataUtil_RemotePlayer(RemotePlayer bot) {
      return AutismLiteVariant.enabled() ? null : c.init(this, bot);
   }

   @Override
   default void init_V_b_DataUtil(b.Inner_DataUtil handle, Vec3 position, boolean onGround) {
      if (!AutismLiteVariant.enabled()) {
         c.init(this, handle, position, onGround);
      }
   }

   @Override
   default boolean init_Z_b_DataUtil_Z(b.Inner_DataUtil handle, boolean onGround) {
      return AutismLiteVariant.enabled() ? false : c.init(this, handle, onGround);
   }

   @Override
   default boolean init_Z_b_DataUtil_List(b.Inner_DataUtil handle, List<A.Inner_DataUtil> steps) {
      return AutismLiteVariant.enabled() ? false : c.init(this, handle, steps);
   }

   @Override
   default String init_Str_b_DataUtil_Str(b.Inner_DataUtil handle, String dimension) {
      return AutismLiteVariant.enabled() ? "POV is unavailable" : c.init(this, handle, dimension);
   }

   @Override
   default b.Inner_DataUtil init_b_DataUtil() {
      return AutismLiteVariant.enabled() ? null : c.init_b_DataUtil(this);
   }

   @Override
   default b.Inner_DataUtil init_b_DataUtil_b_DataUtil(b.Inner_DataUtil handle) {
      return AutismLiteVariant.enabled() ? null : c.init_b_DataUtil(this, handle);
   }

   @Override
   default boolean pop_Z() {
      return AutismLiteVariant.enabled() ? false : c.init_Z(this);
   }

   @Override
   default void init_V_b_DataUtil(b.Inner_DataUtil handle) {
      if (!AutismLiteVariant.enabled()) {
         c.init_V(this, handle);
      }
   }

   enum Inner_DataUtil {
      init,
      load,
      read;
   }
}

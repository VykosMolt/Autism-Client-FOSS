package autismclient.ai;

import java.util.List;

final class cF {
   private static final long load_J = 100L;
   private static final long read_J = 250L;
   private static final long write_J = 500L;
   private final cc init_cc;
   private long update_J;
   private long sync_J;
   private long flush_J;
   cc.Inner_FileCache init_cc_FileCache = cc.Inner_FileCache.init;
   boolean init_Z;
   String init_Str = "";
   String load_Str = "";
   String read_Str = "";
   String write_Str = "";
   String update_Str = "";
   List<cc.Inner_JsonUtil> init_List = List.of();
   List<String> load_List = List.of();
   List<String> read_List = List.of();
   List<String> write_List = List.of();
   String sync_Str = "high";
   cc.Inner_ByteReader init_cc_ByteReader = new cc.Inner_ByteReader(0L, 0L, 0L, 0L, 0L, 0, 0L);
   cc.Inner_BufferHelper init_cc_BufferHelper = cc.Inner_BufferHelper.init;
   String flush_Str = "";
   boolean load_Z;
   String close = "Chat mode off";
   cc.Inner_UiHelper init_cc_UiHelper;
   AiSettings init_AiSettings = new AiSettings();
   boolean read_Z;
   List<cd.Inner_StreamUtil> update_List = List.of();
   long init_J;
   int init_I;

   cF(cc service) {
      this.init_cc = service;
   }

   void init_V() {
      this.update_J = this.sync_J = this.flush_J = 0L;
   }

   void init(long now, boolean wantSessions) {
      try {
         if (now >= this.update_J) {
            this.update_J = now + 100L;
            this.init_I++;
            cc.Inner_FileCache next = this.init_cc.init_cc_FileCache();
            boolean wasWorking = this.init_Z();
            this.init_cc_FileCache = next;
            this.init_Z = this.init_cc.sync_Z();
            if (this.init_Z() && !wasWorking) {
               this.init_J = now;
            }

            this.init_Str = cE.init_Str_Str(this.init_Z() ? W.init(this.init_cc.open_Str()) : this.init_cc.open_Str());
            this.load_Str = this.init_cc.tick_Str();
            this.read_Str = this.init_cc.pop_Str();
            this.write_Str = this.init_cc.load_Str();
            this.update_Str = this.init_cc.write_Str();
            this.init_List = this.init_cc.read_List();
            this.load_List = this.init_cc.init_List();
            this.read_List = this.init_cc.sync_List();
            this.init_cc_ByteReader = this.init_cc.init_cc_ByteReader();
            this.write_List = this.init_cc.update_List();
            this.sync_Str = this.init_cc.get_Str();
            this.init_cc_BufferHelper = this.init_cc.init_cc_BufferHelper();
            this.flush_Str = this.init_cc.set_Str();
            this.load_Z = this.init_cc.read_Z();
            this.close = this.init_cc.init_Str();
            this.init_cc_UiHelper = this.init_cc.init_cc_UiHelper();
         }

         if (now >= this.sync_J) {
            this.sync_J = now + 250L;
            this.init_AiSettings = this.init_cc.init_AiSettings();
            this.read_Z = this.init_cc.init_Z_AiSettings_Provide(this.init_AiSettings.provider);
         }

         if (wantSessions && now >= this.flush_J) {
            this.flush_J = now + 500L;
            this.update_List = this.init_cc.write_List();
         }
      } catch (RuntimeException ignored) {
         this.update_J = 0L;
      }
   }

   boolean init_Z() {
      return this.init_cc_FileCache == cc.Inner_FileCache.load || this.init_cc_FileCache == cc.Inner_FileCache.read && !this.init_Z;
   }

   boolean load() {
      return this.init_Z() || this.init_cc_FileCache == cc.Inner_FileCache.write || this.init_Z || this.load_Z;
   }

   cc.Inner_JsonUtil init_cc_JsonUtil() {
      return this.init_List.isEmpty() ? null : this.init_List.get(this.init_List.size() - 1);
   }
}

package autismclient.ai;

import java.util.List;

final class AiViewState {
   private static final long load_J = 100L;
   private static final long read_J = 250L;
   private static final long write_J = 500L;
   private final AiService init_cc;
   private long update_J;
   private long sync_J;
   private long flush_J;
   AiService.RunState init_cc_FileCache = AiService.RunState.IDLE;
   boolean init_Z;
   String init_Str = "";
   String load_Str = "";
   String read_Str = "";
   String write_Str = "";
   String update_Str = "";
   List<AiService.TranscriptEntry> init_List = List.of();
   List<String> load_List = List.of();
   List<String> read_List = List.of();
   List<String> write_List = List.of();
   String sync_Str = "high";
   AiService.TokenUsage init_cc_ByteReader = new AiService.TokenUsage(0L, 0L, 0L, 0L, 0L, 0, 0L);
   AiService.ProviderTestState init_cc_BufferHelper = AiService.ProviderTestState.UNKNOWN;
   String flush_Str = "";
   boolean load_Z;
   String close = "Chat mode off";
   AiService.UserPromptQuestion init_cc_UiHelper;
   AiSettings init_AiSettings = new AiSettings();
   boolean read_Z;
   List<AiSessions.Inner_StreamUtil> update_List = List.of();
   long init_J;
   int init_I;

   AiViewState(AiService service) {
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
            AiService.RunState next = this.init_cc.getRunState();
            boolean wasWorking = this.init_Z();
            this.init_cc_FileCache = next;
            this.init_Z = this.init_cc.isAwaitingPlanApproval();
            if (this.init_Z() && !wasWorking) {
               this.init_J = now;
            }

            this.init_Str = AiUiKit.flattenSingleLine(this.init_Z() ? AiGoal.init(this.init_cc.getStatusLine()) : this.init_cc.getStatusLine());
            this.load_Str = this.init_cc.getActiveSessionTitle();
            this.read_Str = this.init_cc.getActiveSessionId();
            this.write_Str = this.init_cc.getObjective();
            this.update_Str = this.init_cc.getPlanNotes();
            this.init_List = this.init_cc.getTranscript();
            this.load_List = this.init_cc.getQueuedObjectives();
            this.read_List = this.init_cc.getSteeringNotes();
            this.init_cc_ByteReader = this.init_cc.getTokenUsage();
            this.write_List = this.init_cc.getAvailableEffortLevels();
            this.sync_Str = this.init_cc.getEffectiveEffortLevel();
            this.init_cc_BufferHelper = this.init_cc.getProviderTestState();
            this.flush_Str = this.init_cc.getProviderTestDetail();
            this.load_Z = this.init_cc.isChatModeActive();
            this.close = this.init_cc.getChatModeSummary();
            this.init_cc_UiHelper = this.init_cc.getPendingQuestion();
         }

         if (now >= this.sync_J) {
            this.sync_J = now + 250L;
            this.init_AiSettings = this.init_cc.getSettings();
            this.read_Z = this.init_cc.hasProviderKey(this.init_AiSettings.provider);
         }

         if (wantSessions && now >= this.flush_J) {
            this.flush_J = now + 500L;
            this.update_List = this.init_cc.listSessions();
         }
      } catch (RuntimeException ignored) {
         this.update_J = 0L;
      }
   }

   boolean init_Z() {
      return this.init_cc_FileCache == AiService.RunState.RUNNING || this.init_cc_FileCache == AiService.RunState.PLANNING && !this.init_Z;
   }

   boolean load() {
      return this.init_Z() || this.init_cc_FileCache == AiService.RunState.PAUSED || this.init_Z || this.load_Z;
   }

   AiService.TranscriptEntry init_cc_JsonUtil() {
      return this.init_List.isEmpty() ? null : this.init_List.get(this.init_List.size() - 1);
   }
}

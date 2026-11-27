package autismclient.ai;

import java.util.List;

final class AiViewState {
   private static final long STATUS_POLL_MS = 100L;
   private static final long SETTINGS_POLL_MS = 250L;
   private static final long SESSIONS_POLL_MS = 500L;
   private final AiService service;
   private long nextStatusPollMs;
   private long nextSettingsPollMs;
   private long nextSessionsPollMs;
   AiService.RunState runState = AiService.RunState.IDLE;
   boolean awaitingPlanApproval;
   String statusLine = "";
   String activeSessionTitle = "";
   String activeSessionId = "";
   String objective = "";
   String planNotes = "";
   List<AiService.TranscriptEntry> transcript = List.of();
   List<String> queuedObjectives = List.of();
   List<String> steeringNotes = List.of();
   List<String> availableEffortLevels = List.of();
   String effectiveEffortLevel = "high";
   AiService.TokenUsage tokenUsage = new AiService.TokenUsage(0L, 0L, 0L, 0L, 0L, 0, 0L);
   AiService.ProviderTestState providerTestState = AiService.ProviderTestState.UNKNOWN;
   String providerTestDetail = "";
   boolean chatModeActive;
   String chatModeSummary = "Chat mode off";
   AiService.UserPromptQuestion pendingQuestion;
   AiSettings settings = new AiSettings();
   boolean hasProviderKey;
   List<AiSessions.Inner_StreamUtil> sessions = List.of();
   long workStartedMs;
   int pollCount;

   AiViewState(AiService service) {
      this.service = service;
   }

   void invalidate() {
      this.nextStatusPollMs = this.nextSettingsPollMs = this.nextSessionsPollMs = 0L;
   }

   void refresh(long now, boolean wantSessions) {
      try {
         if (now >= this.nextStatusPollMs) {
            this.nextStatusPollMs = now + 100L;
            this.pollCount++;
            AiService.RunState next = this.service.getRunState();
            boolean wasWorking = this.isWorking();
            this.runState = next;
            this.awaitingPlanApproval = this.service.isAwaitingPlanApproval();
            if (this.isWorking() && !wasWorking) {
               this.workStartedMs = now;
            }

            this.statusLine = AiUiKit.flattenSingleLine(this.isWorking() ? AiGoal.init(this.service.getStatusLine()) : this.service.getStatusLine());
            this.activeSessionTitle = this.service.getActiveSessionTitle();
            this.activeSessionId = this.service.getActiveSessionId();
            this.objective = this.service.getObjective();
            this.planNotes = this.service.getPlanNotes();
            this.transcript = this.service.getTranscript();
            this.queuedObjectives = this.service.getQueuedObjectives();
            this.steeringNotes = this.service.getSteeringNotes();
            this.tokenUsage = this.service.getTokenUsage();
            this.availableEffortLevels = this.service.getAvailableEffortLevels();
            this.effectiveEffortLevel = this.service.getEffectiveEffortLevel();
            this.providerTestState = this.service.getProviderTestState();
            this.providerTestDetail = this.service.getProviderTestDetail();
            this.chatModeActive = this.service.isChatModeActive();
            this.chatModeSummary = this.service.getChatModeSummary();
            this.pendingQuestion = this.service.getPendingQuestion();
         }

         if (now >= this.nextSettingsPollMs) {
            this.nextSettingsPollMs = now + 250L;
            this.settings = this.service.getSettings();
            this.hasProviderKey = this.service.hasProviderKey(this.settings.provider);
         }

         if (wantSessions && now >= this.nextSessionsPollMs) {
            this.nextSessionsPollMs = now + 500L;
            this.sessions = this.service.listSessions();
         }
      } catch (RuntimeException ignored) {
         this.nextStatusPollMs = 0L;
      }
   }

   boolean isWorking() {
      return this.runState == AiService.RunState.RUNNING || this.runState == AiService.RunState.PLANNING && !this.awaitingPlanApproval;
   }

   boolean isActive() {
      return this.isWorking() || this.runState == AiService.RunState.PAUSED || this.awaitingPlanApproval || this.chatModeActive;
   }

   AiService.TranscriptEntry latestTranscriptEntry() {
      return this.transcript.isEmpty() ? null : this.transcript.get(this.transcript.size() - 1);
   }
}

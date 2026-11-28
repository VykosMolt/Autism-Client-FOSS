package autismclient.ai;

import autismclient.AutismClientAddon;
import autismclient.api.ai.AiCapabilityDescriptor;
import autismclient.api.ai.AiOperation;
import autismclient.compat263.CompatGLFW;
import autismclient.util.AutismClientMessaging;
import autismclient.util.macro.MacroExecutor;
import com.google.gson.JsonObject;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.function.Consumer;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;

public final class AiDirectOps {
   public static final Set<String> UNPAUSABLE_TOOL_IDS = Set.of("game:fight", "game:shoot", "game:dodge", "game:mlg", "game:escape_hazard", "game:telly", "game:scaffold");
   public static final Set<String> CAMPAIGN_TOOL_IDS = Set.of(
      "game:dimension", "game:plan", "game:nether_entry", "game:speedrun", "game:dragon_fight", "game:nether_run", "game:stronghold", "game:end_run"
   );
   private static final AiCatalog CATALOG = new AiCatalog();
   private static volatile AiDirectOps.Inner_DataUtil active;
   private static volatile autismclient.util.macro.MacroToolBridge.Inner_StreamUtil latestJob;
   private static final List<AiDirectOps.Inner_DataUtil> waiters = new ArrayList<>();
   private static volatile AiToolSettings cachedToolSettings;
   private static int pendingInputCleanups;
   private static int tickCount;
   private static volatile autismclient.util.macro.MacroToolBridge.Inner_StreamUtil lastJob;
   private static volatile AiOperation.Result lastResult;

   public static boolean init_Z(String id) {
      return CAMPAIGN_TOOL_IDS.contains(id);
   }

   private AiDirectOps() {
   }

   public static void reloadToolSettings() {
      cachedToolSettings = AiToolSettings.load(aiFolder());
   }

   static void init(Minecraft client) {
      try {
         AiGhastReflect.tickAll();
         if (++tickCount % 20 == 0 || CATALOG.snapshot().entries().isEmpty()) {
            CATALOG.refresh();
         }

         for (AiDirectOps.Inner_DataUtil waiter : List.copyOf(waiters)) {
            tickOp(client, waiter);
         }

         AiDirectOps.Inner_DataUtil body = active;
         if (body != null) {
            tickOp(client, body);
         }
      } catch (Throwable t) {
         AutismClientAddon.LOG.warn("[Tools] tick failed", t);
         stopAll("survival supervisor failed; controls released");
      }
   }

   public static AiCatalog.Snapshot catalogSnapshot() {
      CATALOG.refresh();
      return CATALOG.snapshot();
   }

   public static AiCatalog.Entry catalogEntry(String id) {
      return id == null ? null : catalogSnapshot().get(id);
   }

   public static boolean isToolId(String id) {
      return id != null && id.contains(":") && !id.startsWith("module:") && !id.startsWith("command:") && !id.startsWith("macro:") && !id.startsWith("menu:");
   }

   public static String resolveToolId(String raw) {
      if (raw != null && !raw.isBlank()) {
         String id = raw.trim();
         if (catalogEntry(id) != null) {
            return id;
         }

         if (!id.contains(":")) {
            if (catalogEntry("game:" + id) != null) {
               return "game:" + id;
            }

            if (catalogEntry("client:" + id) != null) {
               return "client:" + id;
            }
         }

         return null;
      } else {
         return null;
      }
   }

   public static String summarizePurpose(String purpose) {
      return AiTools.shortPurpose(purpose);
   }

   public static boolean isRunning() {
      return active != null;
   }

   public static String activeToolId() {
      AiDirectOps.Inner_DataUtil a = active;
      return a == null ? "" : a.id;
   }

   public static void run(String id, JsonObject args) {
      start(id, args);
   }

   public static autismclient.util.macro.MacroToolBridge.Inner_StreamUtil start(String id, JsonObject args) {
      AiDirectOps.Inner_DataUtil running = active;
      if (running != null && !id.equals("client:run_macro")) {
         throw new IllegalStateException("Already running " + running.id + " (" + elapsedText(running) + ") - " + toolsCommand() + " stop first");
      }

      AiCatalog.Entry entry = catalogSnapshot().get(id);
      if (entry == null) {
         throw new IllegalArgumentException("Unknown capability " + id);
      }

      if (!id.startsWith("module:") && !id.startsWith("command:") && !id.startsWith("macro:") && !id.startsWith("menu:")) {
         if (entry.contract().classification() == AiCapabilityDescriptor.Classification.DISCOVER_ONLY) {
            throw new IllegalArgumentException(id + " is discover-only; it documents alternatives but performs nothing");
         }

         AiSettings ai = settings();
         JsonObject schema = AiJson.parseObject(entry.contract().inputSchema());
         JsonObject effective = applyToolDefaults(id, args, schema);
         if (schema.has("type") || schema.has("properties")) {
            AiSchemas.init(schema, effective);
         }

         boolean action = entry.contract().classification() != AiCapabilityDescriptor.Classification.OBSERVE;
         List<String> requiredResources = id.equals("client:run_macro") ? List.of() : entry.contract().resources();
         if (action && AiService.isGameplayActionOperation(id)) {
            Minecraft client = Minecraft.getInstance();
            if (client.player != null && client.player.isPassenger()) {
               throw new IllegalStateException("Dismount first (game:ride action:dismount) before " + id + "; movement keys would steer the mount");
            }

            if (client.player != null && client.player.isFallFlying()) {
               throw new IllegalStateException("Land first; movement keys are meaningless while elytra-flying (" + id + ")");
            }
         }

         Runnable prepare = () -> {
            if (requiredResources.contains("INPUT")) {
               autismclient.ai.engine.AiMotionSafety.releaseMovementKeys();
               AiAssistant.getInstance().pop();
            }

            if (action && id.startsWith("game:")) {
               AiModuleScope.init();
            }
         };
         AutoCloseable lease = id.equals("client:run_macro") ? () -> {} : MacroExecutor.acquireExternalResources(entry.contract().resources());
         AiOperation op = AiModuleScope.wrap(() -> {
            prepare.run();
            return AiTools.operationFor(id, effective, ai, null);
         });
         Minecraft mc = Minecraft.getInstance();
         AiDirectOps.Inner_DataUtil created = new AiDirectOps.Inner_DataUtil(id, op, lease, requiredResources, mc.level, ai.emergencyKey);
         if (id.equals("client:run_macro")) {
            waiters.add(created);
            latestJob = created.job;
         } else {
            active = created;
         }

         lastJob = created.job;
         created.job.init(() -> Minecraft.getInstance().execute(() -> init(created.job)));
         if (lease != null) {
            created.job.init(autismclient.util.macro.MacroToolBridge.Inner_ConfigStore.WAITING, "Starting " + id, "{}");
         } else {
            created.job.init(autismclient.util.macro.MacroToolBridge.Inner_ConfigStore.WAITING, "Waiting for shared controls", "{}");
         }

         lastResult = null;
         AiEventLog.getInstance().init("op_started", AiJson.objectOf("id", id, "source", "direct"));
         AutismClientMessaging.sendPrefixed("§7Running §f" + id + "§7 - §8" + cancelHint() + "§7 cancels");
         return created.job;
      } else {
         throw new IllegalArgumentException(
            id + " is not directly callable - use the module menu, the matching chat command, or client:run_macro for saved macros"
         );
      }
   }

   public static autismclient.util.macro.MacroToolBridge.Inner_StreamUtil currentHandle() {
      AiDirectOps.Inner_DataUtil a = active;
      return a != null ? a.job : (latestJob != null ? latestJob : lastJob);
   }

   public static void init(autismclient.util.macro.MacroToolBridge.Inner_StreamUtil job) {
      AiDirectOps.Inner_DataUtil a = active;
      if (a != null && a.job == job) {
         failOp(a, AiFailure.CANCELLED, "Cancelled by its owner");
      } else {
         for (AiDirectOps.Inner_DataUtil waiter : List.copyOf(waiters)) {
            if (waiter.job == job) {
               failOp(waiter, AiFailure.CANCELLED, "Cancelled by its owner");
            }
         }
      }
   }

   private static JsonObject applyToolDefaults(String id, JsonObject args, JsonObject schema) {
      AiToolSettings s = toolSettings();
      JsonObject out = args.deepCopy();
      if (id.equals("game:goto") || id.equals("game:navigate")) {
         putDefaultString(schema, out, "strategy", s.strategy);
         putDefaultBoolean(schema, out, "allowDig", s.allowDig);
         putDefaultBoolean(schema, out, "allowPillar", s.allowPillar);
         putDefaultBoolean(schema, out, "allowBridge", s.allowBridge);
         putDefaultBoolean(schema, out, "allowInventory", s.allowInventory);
         putDefaultInt(schema, out, "digReluctance", s.digReluctance);
      }

      if ((id.equals("game:surface") || id.equals("game:cave")) && !out.has("strategy")) {
         putDefaultString(schema, out, "strategy", s.strategy);
      }

      if (id.equals("game:fight") && !out.has("useShield")) {
         putDefaultBoolean(schema, out, "useShield", s.useShield);
      }

      if (!id.equals("game:goto") && !out.has("timeoutSeconds") && schemaHas(schema, "timeoutSeconds")) {
         out.addProperty("timeoutSeconds", s.timeoutSeconds);
      }

      return out;
   }

   private static void putDefaultString(JsonObject schema, JsonObject out, String key, String value) {
      if (!out.has(key) && schemaHas(schema, key)) {
         out.addProperty(key, value);
      }
   }

   private static void putDefaultBoolean(JsonObject schema, JsonObject out, String key, boolean value) {
      if (!out.has(key) && schemaHas(schema, key)) {
         out.addProperty(key, value);
      }
   }

   private static void putDefaultInt(JsonObject schema, JsonObject out, String key, int value) {
      if (!out.has(key) && schemaHas(schema, key)) {
         out.addProperty(key, value);
      }
   }

   private static boolean schemaHas(JsonObject schema, String key) {
      return schema.has("properties") && schema.getAsJsonObject("properties").has(key);
   }

   public static void stop() {
      stopAll("");
   }

   public static void stopAll(String reason) {
      AiMacroTools.cancelAll("Tools stopped");
      AiDirectOps.Inner_DataUtil a = active;

      for (AiDirectOps.Inner_DataUtil waiter : List.copyOf(waiters)) {
         failOp(waiter, AiFailure.CANCELLED, "Macro tool stopped");
      }

      if (a == null) {
         AutismClientMessaging.sendPrefixed("§7No tool operation running.");
      } else {
         failOp(a, AiFailure.CANCELLED, "Tool operation stopped" + (reason != null && !reason.isBlank() ? ": " + reason : ""));
         AutismClientMessaging.sendPrefixed("§e" + a.id + " stopped" + (reason != null && !reason.isBlank() ? ": " + reason : "."));
      }
   }

   public static void pause() {
      AiDirectOps.Inner_DataUtil a = active;
      if (a == null) {
         AutismClientMessaging.sendPrefixed("§7No tool operation running.");
      } else if (a.paused) {
         AutismClientMessaging.sendPrefixed("§7" + a.id + " is already paused - " + toolsCommand() + " resume continues.");
      } else if (a.defense == null && !UNPAUSABLE_TOOL_IDS.contains(a.id)) {
         a.paused = true;
         a.job.init(autismclient.util.macro.MacroToolBridge.Inner_ConfigStore.PAUSED, "Paused", a.lastData);
         releaseInputs(a);
         AutismClientMessaging.sendPrefixed("§e" + a.id + " paused (inputs released; its deadline keeps running) - " + toolsCommand() + " resume continues.");
      } else {
         stopAll("pause is unsafe for this op (live combat/reflex state)");
      }
   }

   public static void resume() {
      AiDirectOps.Inner_DataUtil a = active;
      if (a == null) {
         AutismClientMessaging.sendPrefixed("§7No tool operation running.");
      } else if (!a.paused) {
         AutismClientMessaging.sendPrefixed("§7" + a.id + " is not paused.");
      } else {
         a.paused = false;
         a.job
            .init(
               a.lease == null ? autismclient.util.macro.MacroToolBridge.Inner_ConfigStore.WAITING : autismclient.util.macro.MacroToolBridge.Inner_ConfigStore.RUNNING,
               "Resumed",
               a.lastData
            );
         AutismClientMessaging.sendPrefixed("§7" + a.id + " resumed.");
      }
   }

   public static List<String> statusLines() {
      AiDirectOps.Inner_DataUtil a = active;
      if (a == null) {
         return List.of("§7No tool operation running.");
      }

      List<String> lines = new ArrayList<>();
      lines.add("§7Op: §f" + a.id + " §8(" + elapsedText(a) + (a.paused ? ", paused" : ", running") + ")");
      if (!a.lastMessage.isBlank()) {
         lines.add("§7Progress: §f" + a.lastMessage);
      }

      if (toolSettings().verbosityLevel().includesData() && !a.lastData.isBlank() && !a.lastData.equals("{}")) {
         lines.add("§8" + clipJson(a.lastData, 200));
      }

      return lines;
   }

   private static void tickOp(Minecraft mc, AiDirectOps.Inner_DataUtil a) {
      if (autismclient.modules.aD.init_Z()) {
         stopAll("client panic mode");
      } else if (!autismclient.modules.AutoTotemModule.init_Z_6()) {
         if (a.world != null && mc.level != a.world) {
            if (!init_Z(a.id)) {
               failOp(a, AiFailure.WORLD_CHANGED, "World or session changed");
               AutismClientMessaging.sendPrefixed("§e" + a.id + " cancelled [WORLD_CHANGED]: world or session changed.");
               return;
            }

            a.world = mc.level;
         }

         if (mc.player != null && !mc.player.isAlive()) {
            failOp(a, AiFailure.CANCELLED, "Player died");
            AutismClientMessaging.sendPrefixed("§e" + a.id + " cancelled: player died.");
         } else if (CompatGLFW.glfwGetKey(mc.getWindow().handle(), a.emergencyKey) == 1) {
            stopAll("emergency key");
         } else if (!a.paused) {
            if (a.lease == null) {
               a.lease = MacroExecutor.acquireExternalResources(a.resources);
               if (a.lease == null) {
                  a.lastMessage = "Waiting for shared controls";
                  return;
               }

               a.job.init(autismclient.util.macro.MacroToolBridge.Inner_ConfigStore.WAITING, "Starting " + a.id, a.lastData);
            }

            if (mc.player != null && mc.player.isAlive() && a.resources.contains("INPUT") && a.resources.contains("ROTATION")) {
               String hazard = AiHazardEscape.init(mc);
               if (hazard != null && !a.kind.equals("environment") && !a.id.equals("game:escape_hazard") && !AiGuard.isSuppressed()) {
                  interruptForDefense(a, AiHazardEscape.init(AiJson.objectOf("timeoutSeconds", 25)), "environment", AiFailure.ENVIRONMENTAL_HAZARD, hazard);
               }

               if (hazard == null
                  && a.defense != null
                  && a.kind.equals("food")
                  && AiService.isSafeStanceForReflex(mc.player.onGround(), mc.player.isInWater(), mc.player.onClimbable())) {
                  if (AiDodge.init(mc, false)) {
                     interruptForDefense(
                        a,
                        AiDodge.init(AiJson.objectOf("deflect", true, "shield", true, "timeoutSeconds", 4)),
                        "projectile",
                        AiFailure.THREAT_INTERRUPT,
                        "incoming projectile while eating"
                     );
                  } else if (a.defense.canInterruptNow() && (AiGuard.sampleStatus().hostilesNear() > 0 || AiSurvival.nearestThreat(mc) != null)) {
                     interruptForDefense(a, AiSurvival.defend(mc), "combat", AiFailure.THREAT_INTERRUPT, "approaching enemy interrupted eating");
                  }
               }

               if (a.defense == null
                  && hazard == null
                  && AiService.isInterruptibleOperation(a.id)
                  && mc.player.isInWall()
                  && AiService.isSafeStanceForReflex(mc.player.onGround(), mc.player.isInWater(), mc.player.onClimbable())) {
                  AiOperation.Result interrupted = AiService.interruptedFailureResult(
                     a.op, AiFailure.ENVIRONMENTAL_HAZARD, "Suffocating inside a block; observe the obstruction before restarting"
                  );
                  a.op.cancel();
                  recordProgress(a, interrupted);
                  completeOp(a, interrupted);
                  return;
               }

               if (a.defense == null
                  && AiService.isInterruptibleOperation(a.id)
                  && !AiGuard.isSuppressed()
                  && AiService.isSafeStanceForReflex(mc.player.onGround(), mc.player.isInWater(), mc.player.onClimbable())) {
                  boolean projectile = AiDodge.init(mc, mc.player.getDeltaMovement().horizontalDistance() >= 0.12);
                  if (projectile) {
                     interruptForDefense(
                        a,
                        AiDodge.init(AiJson.objectOf("deflect", true, "shield", true, "timeoutSeconds", 4)),
                        "projectile",
                        AiFailure.THREAT_INTERRUPT,
                        "incoming projectile"
                     );
                  } else if (a.op.canInterruptNow()) {
                     AiGuard.StatusSnapshot vitals = AiGuard.sampleStatus();
                     boolean movingPast = AiGuard.canSprintPastThreats(mc, mc.player.getDeltaMovement());
                     boolean unsafeMeal = a.id.equals("game:eat") && AiSurvival.nearestThreat(mc) != null;
                     if (unsafeMeal || !movingPast && vitals.hostilesNear() > 0 && (vitals.underAttack() || a.id.equals("game:eat"))) {
                        AiOperation defense = a.id.equals("game:eat")
                           ? AiSurvival.defend(mc)
                           : AiGuard.createResponseOperation(AiGuard.GuardPolicy.standard(), vitals, 15);
                        if (defense != null) {
                           interruptForDefense(
                              a,
                              defense,
                              "combat",
                              AiFailure.THREAT_INTERRUPT,
                              a.id.equals("game:eat") ? "nearby enemy prevents a safe meal" : AiGuard.decisionReason(AiGuard.GuardPolicy.standard(), vitals)
                           );
                        }
                     } else if (!a.id.equals("game:eat") && vitals.food() <= 6 && vitals.hasFood() && vitals.hostilesNear() == 0) {
                        interruptForDefense(a, AiSurvival.init(AiJson.objectOf("timeoutSeconds", 15)), "food", AiFailure.HUNGER, "critical hunger");
                     }
                  }
               }
            }

            AiOperation.Result result;
            try {
               result = a.defense == null ? a.op.tick() : a.defense.tick();
               if (a.defense != null && result.terminal()) {
                  JsonObject receipt = AiRunResources.parseEvidenceJson(a.interrupted.dataJson());
                  receipt.add("defense", AiRunResources.parseEvidenceJson(result.dataJson()));
                  receipt.addProperty("defenseKind", a.kind);
                  receipt.addProperty("defenseStatus", result.status().name());
                  AiOperation.Result defense = result;
                  boolean reconcile = a.interrupted.status() == AiOperation.Status.UNCERTAIN
                     || defense.status() == AiOperation.Status.UNCERTAIN
                     || a.defense.requiresReconciliationAfterEmergency();
                  receipt.addProperty("interruptedActionNeedsReconciliation", reconcile);
                  a.defense.cancel();
                  a.defense = null;
                  result = new AiOperation.Result(
                     reconcile ? AiOperation.Status.UNCERTAIN : a.interrupted.status(),
                     a.interrupted.message()
                        + "; defense "
                        + defense.status().name().toLowerCase(Locale.ROOT)
                        + ": "
                        + defense.message()
                        + "; reobserve before restarting work",
                     receipt.toString(),
                     defense.evidence(),
                     a.interrupted.failure()
                  );
               }
            } catch (Throwable t) {
               removeOp(a);
               lastResult = AiOperation.Result.failed(AiFailure.EXECUTION_FAILED, t.getMessage() == null ? t.getClass().getSimpleName() : t.getMessage(), "{}");
               finishOp(a, true, lastResult);
               AiEventLog.getInstance().init("op_failed", AiJson.objectOf("id", a.id, "status", "FAILED", "failure", AiFailure.UNKNOWN.name()));
               AutismClientMessaging.sendPrefixed(
                  "§c" + a.id + " failed [UNKNOWN]: " + (t.getMessage() == null ? t.getClass().getSimpleName() : t.getMessage())
               );
               return;
            }

            recordProgress(a, result);
            if (result.terminal()) {
               completeOp(a, result);
            }
         }
      }
   }

   private static void interruptForDefense(AiDirectOps.Inner_DataUtil a, AiOperation defense, String kind, AiFailure cause, String reason) {
      if (a.interrupted == null) {
         a.interrupted = AiService.interruptedFailureResult(a.op, cause, "Work interrupted: " + reason);
      }

      if (a.defense != null) {
         if (a.defense.requiresReconciliationAfterEmergency()) {
            JsonObject receipt = AiRunResources.parseEvidenceJson(a.interrupted.dataJson());
            receipt.addProperty("interruptedActionNeedsReconciliation", true);
            a.interrupted = new AiOperation.Result(
               AiOperation.Status.UNCERTAIN,
               a.interrupted.message(),
               receipt.toString(),
               a.interrupted.evidence(),
               a.interrupted.failure()
            );
         }

         a.defense.cancel();
      } else {
         a.op.cancel();
      }

      a.defense = defense;
      a.kind = kind;
      AiEventLog.getInstance()
         .init(
            "tools_survival_priority",
            AiJson.objectOf(
               "id",
               a.id,
               "defense",
               kind,
               "reason",
               reason,
               "reconciliationRequired",
               a.interrupted.status() == AiOperation.Status.UNCERTAIN
            )
         );
   }

   private static void recordProgress(AiDirectOps.Inner_DataUtil a, AiOperation.Result result) {
      String data = result.dataJson();
      a.lastMessage = result.message();
      a.lastData = data == null ? "{}" : data;
      if (!result.terminal()) {
         a.job.init(autismclient.util.macro.MacroToolBridge.Inner_ConfigStore.RUNNING, a.lastMessage, a.lastData);
         if (active == a) {
            String title = a.id.substring(a.id.indexOf(':') + 1).replace('_', ' ');
            String pretty = title.isEmpty() ? a.id : Character.toUpperCase(title.charAt(0)) + title.substring(1);
            AiVisualization.init(a, pretty, AiTaskStatus.init(a.lastMessage, "In progress"), -1);
         }
      }
   }

   public static AiOperation.Result init_AiOperation_Result() {
      return lastResult;
   }

   static AiOperation.Result failureResult(AiOperation op, AiOperation reflex, AiOperation.Result interrupted, AiFailure cause, String message, String lastData) {
      boolean pending = op.requiresReconciliationAfterEmergency()
         || reflex != null && reflex.requiresReconciliationAfterEmergency()
         || interrupted != null && interrupted.status() == AiOperation.Status.UNCERTAIN;
      JsonObject data = AiRunResources.parseEvidenceJson(lastData == null ? "{}" : lastData);
      if (interrupted != null) {
         data.add("interruptedWork", AiRunResources.parseEvidenceJson(interrupted.dataJson()));
      }

      return AiGuardLayer.wrapPendingTermination(
         pending, new AiOperation.Result(AiOperation.Status.CANCELLED, message, data.toString(), List.of(), cause)
      );
   }

   private static void failOp(AiDirectOps.Inner_DataUtil a, AiFailure cause, String message) {
      if (active == a || waiters.contains(a)) {
         AiOperation.Result result = failureResult(a.op, a.defense, a.interrupted, cause, message, a.lastData);
         removeOp(a);
         lastResult = result;
         finishOp(a, true, result);
         AiEventLog.getInstance()
            .init(
               "op_failed",
               AiJson.objectOf(
                  "id", a.id, "status", result.status().name(), "failure", cause.name(), "data", AiRunResources.parseEvidenceJson(result.dataJson())
               )
            );
         if (result.status() == AiOperation.Status.UNCERTAIN) {
            AutismClientMessaging.sendPrefixed(a.id + ": pending changes need observation before restarting work.");
         }
      }
   }

   private static void completeOp(AiDirectOps.Inner_DataUtil a, AiOperation.Result result) {
      if (active == a || waiters.contains(a)) {
         removeOp(a);
         lastResult = result;
         finishOp(a, false, result);
         AiToolSettings.Verbosity verbosity = toolSettings().verbosityLevel();
         String data = a.lastData == null ? "{}" : a.lastData;
         switch (result.status()) {
            case SUCCEEDED:
               AiEventLog.getInstance().init("op_completed", AiJson.objectOf("id", a.id, "status", "SUCCEEDED"));
               AutismClientMessaging.sendPrefixed("§a" + a.id + ": " + result.message());
               if (!data.isBlank() && !data.equals("{}")) {
                  AutismClientMessaging.sendPrefixed("§8" + clipJson(data, dataLimitFor(verbosity)));
               }
               break;
            case UNCERTAIN:
               AiEventLog.getInstance().init("op_failed", AiJson.objectOf("id", a.id, "status", "UNCERTAIN", "failure", result.failure().name()));
               AutismClientMessaging.sendPrefixed("§e" + a.id + " uncertain: " + result.message() + "§7 - reobserve before retrying");
               break;
            case CANCELLED:
               AiEventLog.getInstance().init("op_failed", AiJson.objectOf("id", a.id, "status", "CANCELLED", "failure", result.failure().name()));
               AutismClientMessaging.sendPrefixed("§e" + a.id + " cancelled: " + result.message());
               break;
            case TIMED_OUT:
               AiFailure failure = result.failure() == AiFailure.UNKNOWN ? AiFailure.TIMEOUT : result.failure();
               AiEventLog.getInstance().init("op_failed", AiJson.objectOf("id", a.id, "status", "TIMED_OUT", "failure", failure.name()));
               AutismClientMessaging.sendPrefixed(failureLine(a.id, "timed out", failure, result.message(), verbosity));
               break;
            case FAILED:
               AiEventLog.getInstance().init("op_failed", AiJson.objectOf("id", a.id, "status", "FAILED", "failure", result.failure().name()));
               AutismClientMessaging.sendPrefixed(failureLine(a.id, "failed", result.failure(), result.message(), verbosity));
         }
      }
   }

   private static String failureLine(String id, String verb, AiFailure failure, String message, AiToolSettings.Verbosity verbosity) {
      String line = "§c" + id + " " + verb + " [" + failure.name() + "]: " + message;
      if (verbosity.includesFailureSuggestions() && failure.isRecoverable() && !failure.suggestedActions().isEmpty()) {
         line = line + " §7(recoverable - try: " + String.join(", ", failure.suggestedActions()) + ")";
      }

      return line;
   }

   public static boolean isDrivingInput() {
      AiDirectOps.Inner_DataUtil a = active;
      return pendingInputCleanups > 0 || a != null && a.lease != null && a.resources.contains("INPUT");
   }

   private static void finishOp(AiDirectOps.Inner_DataUtil a, boolean cancel, AiOperation.Result result) {
      boolean input = a.lease != null && a.resources.contains("INPUT");
      if (input) {
         pendingInputCleanups++;
      }

      releaseInputs(a);
      AiClientTick.execute(() -> {
         try {
            stopOp(a, cancel);
         } finally {
            if (input) {
               pendingInputCleanups--;
            }

            a.job.init(autismclient.util.macro.MacroToolBridge.Inner_ConfigStore.valueOf(result.status().name()), result.message(), result.dataJson());
         }
      });
   }

   private static void removeOp(AiDirectOps.Inner_DataUtil a) {
      AiVisualization.clearOwner(a);
      if (active == a) {
         active = null;
      }

      waiters.remove(a);
      latestJob = waiters.isEmpty() ? null : waiters.getLast().job;
   }

   private static void stopOp(AiDirectOps.Inner_DataUtil a, boolean cancel) {
      try {
         if (a.defense != null) {
            try {
               a.defense.cancel();
            } catch (Throwable t) {
               AutismClientAddon.LOG.warn("[Tools] defense cleanup failed", t);
            } finally {
               a.defense = null;
            }
         }

         if (cancel) {
            a.op.cancel();
         }
      } catch (Throwable t) {
         AutismClientAddon.LOG.warn("[Tools] operation cleanup failed", t);
      } finally {
         releaseInputs(a);

         try {
            if (a.lease != null) {
               a.lease.close();
            }
         } catch (Throwable t) {
            AutismClientAddon.LOG.warn("[Tools] resource release failed", t);
         }
      }
   }

   private static void releaseInputs(AiDirectOps.Inner_DataUtil a) {
      if (a.lease != null && a.resources.contains("INPUT")) {
         autismclient.ai.engine.AiMotionSafety.releaseMovementKeys();
         Minecraft mc = Minecraft.getInstance();
         if (mc.options != null) {
            for (KeyMapping key : List.of(mc.options.keyAttack, mc.options.keyUse)) {
               key.setDown(autismclient.util.KeyMappingControl.of(key).autism$physicalForBody());
            }
         }
      }
   }

   public static Path aiFolder() {
      return AutismClientAddon.FOLDER.toPath().resolve("ai");
   }

   public static Path blueprintsDir() {
      return aiFolder().resolve("blueprints");
   }

   public static Path schematicsDir() {
      return AiSchematic.init();
   }

   public static AiToolSettings toolSettings() {
      AiToolSettings s = cachedToolSettings;
      if (s == null) {
         s = AiToolSettings.load(aiFolder());
         cachedToolSettings = s;
      }

      return s;
   }

   public static void init(AiToolSettings next) {
      next.clamp();
      cachedToolSettings = next;

      try {
         next.save(aiFolder());
      } catch (Exception e) {
         AutismClientMessaging.sendPrefixed("§cCould not save tool settings: " + e.getMessage());
      }
   }

   public static AiSettings settings() {
      AiService existing = AiService.getExistingInstance();
      if (existing != null) {
         return existing.getSettings();
      }

      try {
         return AiSettings.load(aiFolder());
      } catch (Exception e) {
         return new AiSettings();
      }
   }

   public static void init(Consumer<AiSettings> edit) {
      AiService existing = AiService.getExistingInstance();
      if (existing != null) {
         existing.editSettingsAsync(edit);
      } else {
         try {
            AiSettings s;
            try {
               s = AiSettings.load(aiFolder());
            } catch (Exception e) {
               s = new AiSettings();
            }

            edit.accept(s);
            s.save(aiFolder());
         } catch (Exception e) {
            AutismClientMessaging.sendPrefixed("§cCould not save AI settings: " + e.getMessage());
         }
      }
   }

   private static String elapsedText(AiDirectOps.Inner_DataUtil a) {
      long seconds = Math.max(0L, (System.nanoTime() - a.startedAt) / 1000000000L);
      return seconds < 60L ? seconds + "s" : String.format(Locale.ROOT, "%dm%02ds", seconds / 60L, seconds % 60L);
   }

   private static String cancelHint() {
      AiDirectOps.Inner_DataUtil a = active;
      int key = a == null ? settings().emergencyKey : a.emergencyKey;
      return toolsCommand() + " stop or " + (key == 299 ? "F10" : "key " + key);
   }

   static String toolsCommand() {
      return autismclient.commands.CommandManager.getPrefix() + "tools";
   }

   private static String verbosityHint() {
      String var10000;
      switch (toolSettings().verbosityLevel()) {
         case QUIET:
            var10000 = "";
            break;
         case NORMAL:
            var10000 = " (progress every 5s)";
            break;
         case VERBOSE:
            var10000 = " (progress every 2s)";
            break;
         default:
            throw new MatchException(null, null);
      }

      return var10000;
   }

   static String clipJson(String json, int max) {
      if (json == null) {
         return "{}";
      } else {
         return json.length() <= max ? json : json.substring(0, Math.max(0, max - 3)) + "...";
      }
   }

   private static int dataLimitFor(AiToolSettings.Verbosity verbosity) {
      short var10000;
      switch (verbosity) {
         case QUIET:
            var10000 = 160;
            break;
         case NORMAL:
            var10000 = 320;
            break;
         case VERBOSE:
            var10000 = 900;
            break;
         default:
            throw new MatchException(null, null);
      }

      return var10000;
   }

   private static final class Inner_DataUtil {
      final String id;
      final AiOperation op;
      AutoCloseable lease;
      final autismclient.util.macro.MacroToolBridge.Inner_StreamUtil job;
      final List<String> resources;
      final int emergencyKey;
      final long startedAt = System.nanoTime();
      Object world;
      boolean paused;
      AiOperation defense;
      AiOperation.Result interrupted;
      String kind = "";
      String lastMessage = "";
      String lastData = "{}";
      long load_J;

      Inner_DataUtil(String id, AiOperation op, AutoCloseable lease, List<String> resources, Object world, int emergencyKey) {
         this.id = id;
         this.op = op;
         this.lease = lease;
         this.resources = resources;
         this.world = world;
         this.emergencyKey = emergencyKey;
         this.job = new autismclient.util.macro.MacroToolBridge.Inner_StreamUtil(id);
      }
   }
}

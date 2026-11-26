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
   public static final Set<String> init_Set = Set.of("game:fight", "game:shoot", "game:dodge", "game:mlg", "game:escape_hazard", "game:telly", "game:scaffold");
   public static final Set<String> load_Set = Set.of(
      "game:dimension", "game:plan", "game:nether_entry", "game:speedrun", "game:dragon_fight", "game:nether_run", "game:stronghold", "game:end_run"
   );
   private static final AiCatalog init_AiCatalog = new AiCatalog();
   private static volatile AiDirectOps.Inner_DataUtil init_o_DataUtil;
   private static volatile autismclient.util.macro.MacroToolBridge.Inner_StreamUtil init_k_StreamUtil;
   private static final List<AiDirectOps.Inner_DataUtil> init_List = new ArrayList<>();
   private static volatile AiToolSettings init_AiToolSettings;
   private static int init_I;
   private static int load_I;
   private static volatile autismclient.util.macro.MacroToolBridge.Inner_StreamUtil load_k_StreamUtil;
   private static volatile AiOperation.Result init_AiOperation_Result;

   public static boolean init_Z(String id) {
      return load_Set.contains(id);
   }

   private AiDirectOps() {
   }

   public static void init_V() {
      init_AiToolSettings = AiToolSettings.load(init_Path());
   }

   static void init(Minecraft client) {
      try {
         AiGhastReflect.init_V();
         if (++load_I % 20 == 0 || init_AiCatalog.init_AiCatalog_Snapshot().entries().isEmpty()) {
            init_AiCatalog.init_V();
         }

         for (AiDirectOps.Inner_DataUtil waiter : List.copyOf(init_List)) {
            init(client, waiter);
         }

         AiDirectOps.Inner_DataUtil body = init_o_DataUtil;
         if (body != null) {
            init(client, body);
         }
      } catch (Throwable t) {
         AutismClientAddon.LOG.warn("[Tools] tick failed", t);
         init_V("survival supervisor failed; controls released");
      }
   }

   public static AiCatalog.Snapshot init_AiCatalog_Snapshot() {
      init_AiCatalog.init_V();
      return init_AiCatalog.init_AiCatalog_Snapshot();
   }

   public static AiCatalog.Entry init_AiCatalog_Entry(String id) {
      return id == null ? null : init_AiCatalog_Snapshot().get(id);
   }

   public static boolean load_Z(String id) {
      return id != null && id.contains(":") && !id.startsWith("module:") && !id.startsWith("command:") && !id.startsWith("macro:") && !id.startsWith("menu:");
   }

   public static String init_Str(String raw) {
      if (raw != null && !raw.isBlank()) {
         String id = raw.trim();
         if (init_AiCatalog_Entry(id) != null) {
            return id;
         }

         if (!id.contains(":")) {
            if (init_AiCatalog_Entry("game:" + id) != null) {
               return "game:" + id;
            }

            if (init_AiCatalog_Entry("client:" + id) != null) {
               return "client:" + id;
            }
         }

         return null;
      } else {
         return null;
      }
   }

   public static String load_Str(String purpose) {
      return AiTools.init_Str_Str(purpose);
   }

   public static boolean init_Z() {
      return init_o_DataUtil != null;
   }

   public static String init_Str() {
      AiDirectOps.Inner_DataUtil a = init_o_DataUtil;
      return a == null ? "" : a.init_Str;
   }

   public static void init_V(String id, JsonObject args) {
      init_k_StreamUtil(id, args);
   }

   public static autismclient.util.macro.MacroToolBridge.Inner_StreamUtil init_k_StreamUtil(String id, JsonObject args) {
      AiDirectOps.Inner_DataUtil running = init_o_DataUtil;
      if (running != null && !id.equals("client:run_macro")) {
         throw new IllegalStateException("Already running " + running.init_Str + " (" + init_Str(running) + ") - " + load_Str() + " stop first");
      }

      AiCatalog.Entry entry = init_AiCatalog_Snapshot().get(id);
      if (entry == null) {
         throw new IllegalArgumentException("Unknown capability " + id);
      }

      if (!id.startsWith("module:") && !id.startsWith("command:") && !id.startsWith("macro:") && !id.startsWith("menu:")) {
         if (entry.contract().classification() == AiCapabilityDescriptor.Classification.DISCOVER_ONLY) {
            throw new IllegalArgumentException(id + " is discover-only; it documents alternatives but performs nothing");
         }

         AiSettings ai = init_AiSettings();
         JsonObject schema = AiJson.parseObject(entry.contract().inputSchema());
         JsonObject effective = init(id, args, schema);
         if (schema.has("type") || schema.has("properties")) {
            AiSchemas.init(schema, effective);
         }

         boolean action = entry.contract().classification() != AiCapabilityDescriptor.Classification.OBSERVE;
         List<String> requiredResources = id.equals("client:run_macro") ? List.of() : entry.contract().resources();
         if (action && AiService.read_Z_Str(id)) {
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
               AiAssistant.init_ConfigStore().pop();
            }

            if (action && id.startsWith("game:")) {
               AiModuleScope.init();
            }
         };
         AutoCloseable lease = id.equals("client:run_macro") ? () -> {} : MacroExecutor.acquireExternalResources(entry.contract().resources());
         AiOperation op = AiModuleScope.init_AiOperation_Supplier(() -> {
            prepare.run();
            return AiTools.init_AiOperation_Str(id, effective, ai, null);
         });
         Minecraft mc = Minecraft.getInstance();
         AiDirectOps.Inner_DataUtil created = new AiDirectOps.Inner_DataUtil(id, op, lease, requiredResources, mc.level, ai.emergencyKey);
         if (id.equals("client:run_macro")) {
            init_List.add(created);
            init_k_StreamUtil = created.init_k_StreamUtil;
         } else {
            init_o_DataUtil = created;
         }

         load_k_StreamUtil = created.init_k_StreamUtil;
         created.init_k_StreamUtil.init(() -> Minecraft.getInstance().execute(() -> init(created.init_k_StreamUtil)));
         if (lease != null) {
            created.init_k_StreamUtil.init(autismclient.util.macro.MacroToolBridge.Inner_ConfigStore.WAITING, "Starting " + id, "{}");
         } else {
            created.init_k_StreamUtil.init(autismclient.util.macro.MacroToolBridge.Inner_ConfigStore.WAITING, "Waiting for shared controls", "{}");
         }

         init_AiOperation_Result = null;
         AiEventLog.init_x().init("op_started", AiJson.objectOf("id", id, "source", "direct"));
         AutismClientMessaging.sendPrefixed("§7Running §f" + id + "§7 - §8" + read_Str() + "§7 cancels");
         return created.init_k_StreamUtil;
      } else {
         throw new IllegalArgumentException(
            id + " is not directly callable - use the module menu, the matching chat command, or client:run_macro for saved macros"
         );
      }
   }

   public static autismclient.util.macro.MacroToolBridge.Inner_StreamUtil init_k_StreamUtil() {
      AiDirectOps.Inner_DataUtil a = init_o_DataUtil;
      return a != null ? a.init_k_StreamUtil : (init_k_StreamUtil != null ? init_k_StreamUtil : load_k_StreamUtil);
   }

   public static void init(autismclient.util.macro.MacroToolBridge.Inner_StreamUtil job) {
      AiDirectOps.Inner_DataUtil a = init_o_DataUtil;
      if (a != null && a.init_k_StreamUtil == job) {
         init(a, AiFailure.CANCELLED, "Cancelled by its owner");
      } else {
         for (AiDirectOps.Inner_DataUtil waiter : List.copyOf(init_List)) {
            if (waiter.init_k_StreamUtil == job) {
               init(waiter, AiFailure.CANCELLED, "Cancelled by its owner");
            }
         }
      }
   }

   private static JsonObject init(String id, JsonObject args, JsonObject schema) {
      AiToolSettings s = init_AiToolSettings();
      JsonObject out = args.deepCopy();
      if (id.equals("game:goto") || id.equals("game:navigate")) {
         init(schema, out, "strategy", s.strategy);
         init(schema, out, "allowDig", s.allowDig);
         init(schema, out, "allowPillar", s.allowPillar);
         init(schema, out, "allowBridge", s.allowBridge);
         init(schema, out, "allowInventory", s.allowInventory);
         init(schema, out, "digReluctance", s.digReluctance);
      }

      if ((id.equals("game:surface") || id.equals("game:cave")) && !out.has("strategy")) {
         init(schema, out, "strategy", s.strategy);
      }

      if (id.equals("game:fight") && !out.has("useShield")) {
         init(schema, out, "useShield", s.useShield);
      }

      if (!id.equals("game:goto") && !out.has("timeoutSeconds") && init(schema, "timeoutSeconds")) {
         out.addProperty("timeoutSeconds", s.timeoutSeconds);
      }

      return out;
   }

   private static void init(JsonObject schema, JsonObject out, String key, String value) {
      if (!out.has(key) && init(schema, key)) {
         out.addProperty(key, value);
      }
   }

   private static void init(JsonObject schema, JsonObject out, String key, boolean value) {
      if (!out.has(key) && init(schema, key)) {
         out.addProperty(key, value);
      }
   }

   private static void init(JsonObject schema, JsonObject out, String key, int value) {
      if (!out.has(key) && init(schema, key)) {
         out.addProperty(key, value);
      }
   }

   private static boolean init(JsonObject schema, String key) {
      return schema.has("properties") && schema.getAsJsonObject("properties").has(key);
   }

   public static void load_V() {
      init_V("");
   }

   public static void init_V(String reason) {
      AiMacroTools.init_V_Str("Tools stopped");
      AiDirectOps.Inner_DataUtil a = init_o_DataUtil;

      for (AiDirectOps.Inner_DataUtil waiter : List.copyOf(init_List)) {
         init(waiter, AiFailure.CANCELLED, "Macro tool stopped");
      }

      if (a == null) {
         AutismClientMessaging.sendPrefixed("§7No tool operation running.");
      } else {
         init(a, AiFailure.CANCELLED, "Tool operation stopped" + (reason != null && !reason.isBlank() ? ": " + reason : ""));
         AutismClientMessaging.sendPrefixed("§e" + a.init_Str + " stopped" + (reason != null && !reason.isBlank() ? ": " + reason : "."));
      }
   }

   public static void read_V() {
      AiDirectOps.Inner_DataUtil a = init_o_DataUtil;
      if (a == null) {
         AutismClientMessaging.sendPrefixed("§7No tool operation running.");
      } else if (a.init_Z) {
         AutismClientMessaging.sendPrefixed("§7" + a.init_Str + " is already paused - " + load_Str() + " resume continues.");
      } else if (a.load_AiOperation == null && !init_Set.contains(a.init_Str)) {
         a.init_Z = true;
         a.init_k_StreamUtil.init(autismclient.util.macro.MacroToolBridge.Inner_ConfigStore.PAUSED, "Paused", a.write);
         load(a);
         AutismClientMessaging.sendPrefixed("§e" + a.init_Str + " paused (inputs released; its deadline keeps running) - " + load_Str() + " resume continues.");
      } else {
         init_V("pause is unsafe for this op (live combat/reflex state)");
      }
   }

   public static void write_V() {
      AiDirectOps.Inner_DataUtil a = init_o_DataUtil;
      if (a == null) {
         AutismClientMessaging.sendPrefixed("§7No tool operation running.");
      } else if (!a.init_Z) {
         AutismClientMessaging.sendPrefixed("§7" + a.init_Str + " is not paused.");
      } else {
         a.init_Z = false;
         a.init_k_StreamUtil
            .init(
               a.init_AutoCloseable == null ? autismclient.util.macro.MacroToolBridge.Inner_ConfigStore.WAITING : autismclient.util.macro.MacroToolBridge.Inner_ConfigStore.RUNNING,
               "Resumed",
               a.write
            );
         AutismClientMessaging.sendPrefixed("§7" + a.init_Str + " resumed.");
      }
   }

   public static List<String> init_List() {
      AiDirectOps.Inner_DataUtil a = init_o_DataUtil;
      if (a == null) {
         return List.of("§7No tool operation running.");
      }

      List<String> lines = new ArrayList<>();
      lines.add("§7Op: §f" + a.init_Str + " §8(" + init_Str(a) + (a.init_Z ? ", paused" : ", running") + ")");
      if (!a.read.isBlank()) {
         lines.add("§7Progress: §f" + a.read);
      }

      if (init_AiToolSettings().verbosityLevel().includesData() && !a.write.isBlank() && !a.write.equals("{}")) {
         lines.add("§8" + init(a.write, 200));
      }

      return lines;
   }

   private static void init(Minecraft mc, AiDirectOps.Inner_DataUtil a) {
      if (autismclient.modules.aD.init_Z()) {
         init_V("client panic mode");
      } else if (!autismclient.modules.AutoTotemModule.init_Z_6()) {
         if (a.init_Obj != null && mc.level != a.init_Obj) {
            if (!init_Z(a.init_Str)) {
               init(a, AiFailure.WORLD_CHANGED, "World or session changed");
               AutismClientMessaging.sendPrefixed("§e" + a.init_Str + " cancelled [WORLD_CHANGED]: world or session changed.");
               return;
            }

            a.init_Obj = mc.level;
         }

         if (mc.player != null && !mc.player.isAlive()) {
            init(a, AiFailure.CANCELLED, "Player died");
            AutismClientMessaging.sendPrefixed("§e" + a.init_Str + " cancelled: player died.");
         } else if (CompatGLFW.glfwGetKey(mc.getWindow().handle(), a.init_I) == 1) {
            init_V("emergency key");
         } else if (!a.init_Z) {
            if (a.init_AutoCloseable == null) {
               a.init_AutoCloseable = MacroExecutor.acquireExternalResources(a.init_List);
               if (a.init_AutoCloseable == null) {
                  a.read = "Waiting for shared controls";
                  return;
               }

               a.init_k_StreamUtil.init(autismclient.util.macro.MacroToolBridge.Inner_ConfigStore.WAITING, "Starting " + a.init_Str, a.write);
            }

            if (mc.player != null && mc.player.isAlive() && a.init_List.contains("INPUT") && a.init_List.contains("ROTATION")) {
               String hazard = AiHazardEscape.init(mc);
               if (hazard != null && !a.load_Str.equals("environment") && !a.init_Str.equals("game:escape_hazard") && !AiGuard.isSuppressed()) {
                  init(a, AiHazardEscape.init(AiJson.objectOf("timeoutSeconds", 25)), "environment", AiFailure.ENVIRONMENTAL_HAZARD, hazard);
               }

               if (hazard == null
                  && a.load_AiOperation != null
                  && a.load_Str.equals("food")
                  && AiService.init_Z_Z(mc.player.onGround(), mc.player.isInWater(), mc.player.onClimbable())) {
                  if (AiDodge.init(mc, false)) {
                     init(
                        a,
                        AiDodge.init(AiJson.objectOf("deflect", true, "shield", true, "timeoutSeconds", 4)),
                        "projectile",
                        AiFailure.THREAT_INTERRUPT,
                        "incoming projectile while eating"
                     );
                  } else if (a.load_AiOperation.canInterruptNow() && (AiGuard.sampleStatus().hostilesNear() > 0 || AiSurvival.init_Entity(mc) != null)) {
                     init(a, AiSurvival.init_AiOperation(mc), "combat", AiFailure.THREAT_INTERRUPT, "approaching enemy interrupted eating");
                  }
               }

               if (a.load_AiOperation == null
                  && hazard == null
                  && AiService.init_Z_Str(a.init_Str)
                  && mc.player.isInWall()
                  && AiService.init_Z_Z(mc.player.onGround(), mc.player.isInWater(), mc.player.onClimbable())) {
                  AiOperation.Result interrupted = AiService.init_AiOperation_Result_AiOperation(
                     a.init_AiOperation, AiFailure.ENVIRONMENTAL_HAZARD, "Suffocating inside a block; observe the obstruction before restarting"
                  );
                  a.init_AiOperation.cancel();
                  init(a, interrupted);
                  load(a, interrupted);
                  return;
               }

               if (a.load_AiOperation == null
                  && AiService.init_Z_Str(a.init_Str)
                  && !AiGuard.isSuppressed()
                  && AiService.init_Z_Z(mc.player.onGround(), mc.player.isInWater(), mc.player.onClimbable())) {
                  boolean projectile = AiDodge.init(mc, mc.player.getDeltaMovement().horizontalDistance() >= 0.12);
                  if (projectile) {
                     init(
                        a,
                        AiDodge.init(AiJson.objectOf("deflect", true, "shield", true, "timeoutSeconds", 4)),
                        "projectile",
                        AiFailure.THREAT_INTERRUPT,
                        "incoming projectile"
                     );
                  } else if (a.init_AiOperation.canInterruptNow()) {
                     AiGuard.StatusSnapshot vitals = AiGuard.sampleStatus();
                     boolean movingPast = AiGuard.canSprintPastThreats(mc, mc.player.getDeltaMovement());
                     boolean unsafeMeal = a.init_Str.equals("game:eat") && AiSurvival.init_Entity(mc) != null;
                     if (unsafeMeal || !movingPast && vitals.hostilesNear() > 0 && (vitals.underAttack() || a.init_Str.equals("game:eat"))) {
                        AiOperation defense = a.init_Str.equals("game:eat")
                           ? AiSurvival.init_AiOperation(mc)
                           : AiGuard.createResponseOperation(AiGuard.GuardPolicy.standard(), vitals, 15);
                        if (defense != null) {
                           init(
                              a,
                              defense,
                              "combat",
                              AiFailure.THREAT_INTERRUPT,
                              a.init_Str.equals("game:eat") ? "nearby enemy prevents a safe meal" : AiGuard.decisionReason(AiGuard.GuardPolicy.standard(), vitals)
                           );
                        }
                     } else if (!a.init_Str.equals("game:eat") && vitals.food() <= 6 && vitals.hasFood() && vitals.hostilesNear() == 0) {
                        init(a, AiSurvival.init(AiJson.objectOf("timeoutSeconds", 15)), "food", AiFailure.HUNGER, "critical hunger");
                     }
                  }
               }
            }

            AiOperation.Result result;
            try {
               result = a.load_AiOperation == null ? a.init_AiOperation.tick() : a.load_AiOperation.tick();
               if (a.load_AiOperation != null && result.terminal()) {
                  JsonObject receipt = AiRunResources.parseEvidenceJson(a.init_AiOperation_Result.dataJson());
                  receipt.add("defense", AiRunResources.parseEvidenceJson(result.dataJson()));
                  receipt.addProperty("defenseKind", a.load_Str);
                  receipt.addProperty("defenseStatus", result.status().name());
                  AiOperation.Result defense = result;
                  boolean reconcile = a.init_AiOperation_Result.status() == AiOperation.Status.UNCERTAIN
                     || defense.status() == AiOperation.Status.UNCERTAIN
                     || a.load_AiOperation.requiresReconciliationAfterEmergency();
                  receipt.addProperty("interruptedActionNeedsReconciliation", reconcile);
                  a.load_AiOperation.cancel();
                  a.load_AiOperation = null;
                  result = new AiOperation.Result(
                     reconcile ? AiOperation.Status.UNCERTAIN : a.init_AiOperation_Result.status(),
                     a.init_AiOperation_Result.message()
                        + "; defense "
                        + defense.status().name().toLowerCase(Locale.ROOT)
                        + ": "
                        + defense.message()
                        + "; reobserve before restarting work",
                     receipt.toString(),
                     defense.evidence(),
                     a.init_AiOperation_Result.failure()
                  );
               }
            } catch (Throwable t) {
               init_V(a);
               init_AiOperation_Result = AiOperation.Result.failed(AiFailure.EXECUTION_FAILED, t.getMessage() == null ? t.getClass().getSimpleName() : t.getMessage(), "{}");
               init(a, true, init_AiOperation_Result);
               AiEventLog.init_x().init("op_failed", AiJson.objectOf("id", a.init_Str, "status", "FAILED", "failure", AiFailure.UNKNOWN.name()));
               AutismClientMessaging.sendPrefixed(
                  "§c" + a.init_Str + " failed [UNKNOWN]: " + (t.getMessage() == null ? t.getClass().getSimpleName() : t.getMessage())
               );
               return;
            }

            init(a, result);
            if (result.terminal()) {
               load(a, result);
            }
         }
      }
   }

   private static void init(AiDirectOps.Inner_DataUtil a, AiOperation defense, String kind, AiFailure cause, String reason) {
      if (a.init_AiOperation_Result == null) {
         a.init_AiOperation_Result = AiService.init_AiOperation_Result_AiOperation(a.init_AiOperation, cause, "Work interrupted: " + reason);
      }

      if (a.load_AiOperation != null) {
         if (a.load_AiOperation.requiresReconciliationAfterEmergency()) {
            JsonObject receipt = AiRunResources.parseEvidenceJson(a.init_AiOperation_Result.dataJson());
            receipt.addProperty("interruptedActionNeedsReconciliation", true);
            a.init_AiOperation_Result = new AiOperation.Result(
               AiOperation.Status.UNCERTAIN,
               a.init_AiOperation_Result.message(),
               receipt.toString(),
               a.init_AiOperation_Result.evidence(),
               a.init_AiOperation_Result.failure()
            );
         }

         a.load_AiOperation.cancel();
      } else {
         a.init_AiOperation.cancel();
      }

      a.load_AiOperation = defense;
      a.load_Str = kind;
      AiEventLog.init_x()
         .init(
            "tools_survival_priority",
            AiJson.objectOf(
               "id",
               a.init_Str,
               "defense",
               kind,
               "reason",
               reason,
               "reconciliationRequired",
               a.init_AiOperation_Result.status() == AiOperation.Status.UNCERTAIN
            )
         );
   }

   private static void init(AiDirectOps.Inner_DataUtil a, AiOperation.Result result) {
      String data = result.dataJson();
      a.read = result.message();
      a.write = data == null ? "{}" : data;
      if (!result.terminal()) {
         a.init_k_StreamUtil.init(autismclient.util.macro.MacroToolBridge.Inner_ConfigStore.RUNNING, a.read, a.write);
         if (init_o_DataUtil == a) {
            String title = a.init_Str.substring(a.init_Str.indexOf(':') + 1).replace('_', ' ');
            String pretty = title.isEmpty() ? a.init_Str : Character.toUpperCase(title.charAt(0)) + title.substring(1);
            AiVisualization.init(a, pretty, AiTaskStatus.init(a.read, "In progress"), -1);
         }
      }
   }

   public static AiOperation.Result init_AiOperation_Result() {
      return init_AiOperation_Result;
   }

   static AiOperation.Result init(AiOperation op, AiOperation reflex, AiOperation.Result interrupted, AiFailure cause, String message, String lastData) {
      boolean pending = op.requiresReconciliationAfterEmergency()
         || reflex != null && reflex.requiresReconciliationAfterEmergency()
         || interrupted != null && interrupted.status() == AiOperation.Status.UNCERTAIN;
      JsonObject data = AiRunResources.parseEvidenceJson(lastData == null ? "{}" : lastData);
      if (interrupted != null) {
         data.add("interruptedWork", AiRunResources.parseEvidenceJson(interrupted.dataJson()));
      }

      return AiGuardLayer.init_AiOperation_Result_Z_AiOperation_Result(
         pending, new AiOperation.Result(AiOperation.Status.CANCELLED, message, data.toString(), List.of(), cause)
      );
   }

   private static void init(AiDirectOps.Inner_DataUtil a, AiFailure cause, String message) {
      if (init_o_DataUtil == a || init_List.contains(a)) {
         AiOperation.Result result = init(a.init_AiOperation, a.load_AiOperation, a.init_AiOperation_Result, cause, message, a.write);
         init_V(a);
         init_AiOperation_Result = result;
         init(a, true, result);
         AiEventLog.init_x()
            .init(
               "op_failed",
               AiJson.objectOf(
                  "id", a.init_Str, "status", result.status().name(), "failure", cause.name(), "data", AiRunResources.parseEvidenceJson(result.dataJson())
               )
            );
         if (result.status() == AiOperation.Status.UNCERTAIN) {
            AutismClientMessaging.sendPrefixed(a.init_Str + ": pending changes need observation before restarting work.");
         }
      }
   }

   private static void load(AiDirectOps.Inner_DataUtil a, AiOperation.Result result) {
      if (init_o_DataUtil == a || init_List.contains(a)) {
         init_V(a);
         init_AiOperation_Result = result;
         init(a, false, result);
         AiToolSettings.Verbosity verbosity = init_AiToolSettings().verbosityLevel();
         String data = a.write == null ? "{}" : a.write;
         switch (result.status()) {
            case SUCCEEDED:
               AiEventLog.init_x().init("op_completed", AiJson.objectOf("id", a.init_Str, "status", "SUCCEEDED"));
               AutismClientMessaging.sendPrefixed("§a" + a.init_Str + ": " + result.message());
               if (!data.isBlank() && !data.equals("{}")) {
                  AutismClientMessaging.sendPrefixed("§8" + init(data, init(verbosity)));
               }
               break;
            case UNCERTAIN:
               AiEventLog.init_x().init("op_failed", AiJson.objectOf("id", a.init_Str, "status", "UNCERTAIN", "failure", result.failure().name()));
               AutismClientMessaging.sendPrefixed("§e" + a.init_Str + " uncertain: " + result.message() + "§7 - reobserve before retrying");
               break;
            case CANCELLED:
               AiEventLog.init_x().init("op_failed", AiJson.objectOf("id", a.init_Str, "status", "CANCELLED", "failure", result.failure().name()));
               AutismClientMessaging.sendPrefixed("§e" + a.init_Str + " cancelled: " + result.message());
               break;
            case TIMED_OUT:
               AiFailure failure = result.failure() == AiFailure.UNKNOWN ? AiFailure.TIMEOUT : result.failure();
               AiEventLog.init_x().init("op_failed", AiJson.objectOf("id", a.init_Str, "status", "TIMED_OUT", "failure", failure.name()));
               AutismClientMessaging.sendPrefixed(init(a.init_Str, "timed out", failure, result.message(), verbosity));
               break;
            case FAILED:
               AiEventLog.init_x().init("op_failed", AiJson.objectOf("id", a.init_Str, "status", "FAILED", "failure", result.failure().name()));
               AutismClientMessaging.sendPrefixed(init(a.init_Str, "failed", result.failure(), result.message(), verbosity));
         }
      }
   }

   private static String init(String id, String verb, AiFailure failure, String message, AiToolSettings.Verbosity verbosity) {
      String line = "§c" + id + " " + verb + " [" + failure.name() + "]: " + message;
      if (verbosity.includesFailureSuggestions() && failure.init_Z() && !failure.init_List().isEmpty()) {
         line = line + " §7(recoverable - try: " + String.join(", ", failure.init_List()) + ")";
      }

      return line;
   }

   public static boolean load_Z() {
      AiDirectOps.Inner_DataUtil a = init_o_DataUtil;
      return init_I > 0 || a != null && a.init_AutoCloseable != null && a.init_List.contains("INPUT");
   }

   private static void init(AiDirectOps.Inner_DataUtil a, boolean cancel, AiOperation.Result result) {
      boolean input = a.init_AutoCloseable != null && a.init_List.contains("INPUT");
      if (input) {
         init_I++;
      }

      load(a);
      AiClientTick.init_V_Runnable(() -> {
         try {
            init(a, cancel);
         } finally {
            if (input) {
               init_I--;
            }

            a.init_k_StreamUtil.init(autismclient.util.macro.MacroToolBridge.Inner_ConfigStore.valueOf(result.status().name()), result.message(), result.dataJson());
         }
      });
   }

   private static void init_V(AiDirectOps.Inner_DataUtil a) {
      AiVisualization.init_V(a);
      if (init_o_DataUtil == a) {
         init_o_DataUtil = null;
      }

      init_List.remove(a);
      init_k_StreamUtil = init_List.isEmpty() ? null : init_List.getLast().init_k_StreamUtil;
   }

   private static void init(AiDirectOps.Inner_DataUtil a, boolean cancel) {
      try {
         if (a.load_AiOperation != null) {
            try {
               a.load_AiOperation.cancel();
            } catch (Throwable t) {
               AutismClientAddon.LOG.warn("[Tools] defense cleanup failed", t);
            } finally {
               a.load_AiOperation = null;
            }
         }

         if (cancel) {
            a.init_AiOperation.cancel();
         }
      } catch (Throwable t) {
         AutismClientAddon.LOG.warn("[Tools] operation cleanup failed", t);
      } finally {
         load(a);

         try {
            if (a.init_AutoCloseable != null) {
               a.init_AutoCloseable.close();
            }
         } catch (Throwable t) {
            AutismClientAddon.LOG.warn("[Tools] resource release failed", t);
         }
      }
   }

   private static void load(AiDirectOps.Inner_DataUtil a) {
      if (a.init_AutoCloseable != null && a.init_List.contains("INPUT")) {
         autismclient.ai.engine.AiMotionSafety.releaseMovementKeys();
         Minecraft mc = Minecraft.getInstance();
         if (mc.options != null) {
            for (KeyMapping key : List.of(mc.options.keyAttack, mc.options.keyUse)) {
               key.setDown(autismclient.util.KeyMappingControl.of(key).autism$physicalForBody());
            }
         }
      }
   }

   public static Path init_Path() {
      return AutismClientAddon.FOLDER.toPath().resolve("ai");
   }

   public static Path load_Path() {
      return init_Path().resolve("blueprints");
   }

   public static Path read_Path() {
      return AiSchematic.init();
   }

   public static AiToolSettings init_AiToolSettings() {
      AiToolSettings s = init_AiToolSettings;
      if (s == null) {
         s = AiToolSettings.load(init_Path());
         init_AiToolSettings = s;
      }

      return s;
   }

   public static void init(AiToolSettings next) {
      next.clamp();
      init_AiToolSettings = next;

      try {
         next.save(init_Path());
      } catch (Exception e) {
         AutismClientMessaging.sendPrefixed("§cCould not save tool settings: " + e.getMessage());
      }
   }

   public static AiSettings init_AiSettings() {
      AiService existing = AiService.load_cc();
      if (existing != null) {
         return existing.init_AiSettings();
      }

      try {
         return AiSettings.load(init_Path());
      } catch (Exception e) {
         return new AiSettings();
      }
   }

   public static void init(Consumer<AiSettings> edit) {
      AiService existing = AiService.load_cc();
      if (existing != null) {
         existing.init_V_Consumer(edit);
      } else {
         try {
            AiSettings s;
            try {
               s = AiSettings.load(init_Path());
            } catch (Exception e) {
               s = new AiSettings();
            }

            edit.accept(s);
            s.save(init_Path());
         } catch (Exception e) {
            AutismClientMessaging.sendPrefixed("§cCould not save AI settings: " + e.getMessage());
         }
      }
   }

   private static String init_Str(AiDirectOps.Inner_DataUtil a) {
      long seconds = Math.max(0L, (System.nanoTime() - a.init_J) / 1000000000L);
      return seconds < 60L ? seconds + "s" : String.format(Locale.ROOT, "%dm%02ds", seconds / 60L, seconds % 60L);
   }

   private static String read_Str() {
      AiDirectOps.Inner_DataUtil a = init_o_DataUtil;
      int key = a == null ? init_AiSettings().emergencyKey : a.init_I;
      return load_Str() + " stop or " + (key == 299 ? "F10" : "key " + key);
   }

   static String load_Str() {
      return autismclient.commands.CommandManager.getPrefix() + "tools";
   }

   private static String write_Str() {
      String var10000;
      switch (init_AiToolSettings().verbosityLevel()) {
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

   static String init(String json, int max) {
      if (json == null) {
         return "{}";
      } else {
         return json.length() <= max ? json : json.substring(0, Math.max(0, max - 3)) + "...";
      }
   }

   private static int init(AiToolSettings.Verbosity verbosity) {
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
      final String init_Str;
      final AiOperation init_AiOperation;
      AutoCloseable init_AutoCloseable;
      final autismclient.util.macro.MacroToolBridge.Inner_StreamUtil init_k_StreamUtil;
      final List<String> init_List;
      final int init_I;
      final long init_J = System.nanoTime();
      Object init_Obj;
      boolean init_Z;
      AiOperation load_AiOperation;
      AiOperation.Result init_AiOperation_Result;
      String load_Str = "";
      String read = "";
      String write = "{}";
      long load_J;

      Inner_DataUtil(String id, AiOperation op, AutoCloseable lease, List<String> resources, Object world, int emergencyKey) {
         this.init_Str = id;
         this.init_AiOperation = op;
         this.init_AutoCloseable = lease;
         this.init_List = resources;
         this.init_Obj = world;
         this.init_I = emergencyKey;
         this.init_k_StreamUtil = new autismclient.util.macro.MacroToolBridge.Inner_StreamUtil(id);
      }
   }
}

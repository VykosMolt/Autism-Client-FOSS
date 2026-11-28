package autismclient.ai;

import autismclient.AutismClientAddon;
import autismclient.ai.engine.AiGoto;
import autismclient.api.ai.AiCapabilities;
import autismclient.api.ai.AiCapabilityDescriptor;
import autismclient.api.custommenu.CustomMenuAdapterRegistry;
import autismclient.api.macro.MacroActionEntry;
import autismclient.api.macro.MacroActionRegistry;
import autismclient.api.module.Setting;
import autismclient.commands.Command;
import autismclient.gui.macro.editor.ActionFieldSchema;
import autismclient.modules.Module;
import autismclient.util.macro.MacroAction;
import autismclient.util.macro.MacroActionType;
import com.google.gson.JsonArray;
import com.mojang.serialization.JsonOps;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.TreeMap;
import java.util.function.Supplier;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;

public final class AiCatalog {
   private long init_J = -9223372036854775808L;
   private volatile AiCatalog.Snapshot init_AiCatalog_Snapshot = new AiCatalog.Snapshot("empty", List.of());
   private static final int init_I = 40;

   public AiCatalog.Snapshot snapshot() {
      return this.init_AiCatalog_Snapshot;
   }

   public void refresh() {
      long source = Objects.hash(
         autismclient.modules.al.init_I(),
         autismclient.commands.CommandManager.getRegistryVersion(),
         AiCapabilities.revision(),
         AiSourceKnowledge.init_Str(),
         MacroActionRegistry.entries().stream().map(ex -> ex.typeId()).toList(),
         CustomMenuAdapterRegistry.ids()
      );
      if (source != this.init_J) {
         List<AiCatalog.Entry> entries = new ArrayList<>();

         for (Module m : autismclient.modules.al.init_Collection()) {
            JsonArray settings = new JsonArray();
            JsonArray actionIds = new JsonArray();

            for (Setting<?, ?> s : m.settings()) {
               settings.add(
                  AiJson.objectOf(
                     "id",
                     s.id(),
                     "label",
                     s.label(),
                     "description",
                     s.description(),
                     "kind",
                     s.kind().name(),
                     "default",
                     m.settingsShareable() ? s.defaultValue() : "[private]",
                     "min",
                     finiteOrNull(s.min()),
                     "max",
                     finiteOrNull(s.max()),
                     "choices",
                     s.choices(),
                     "availability",
                     s.availability().name(),
                     "group",
                     s.group()
                  )
               );
               if (s.kind().name().equals("ACTION")) {
                  actionIds.add(s.id());
               }
            }

            String schema = AiJson.objectOf(
                  "enabled",
                  "boolean, optional",
                  "settings",
                  settings,
                  "actions",
                  "array of action-setting ids to run (buttons): " + actionIds,
                  "keybind",
                  "GLFW key code to bind this module's toggle to, or -1 to unbind (optional)",
                  "persistent",
                  "boolean, default false: false borrows activation only for this operation and restores temporary settings when the run ends; pass true only when the user asked for this change itself"
               )
               .toString();
            AiCapabilityDescriptor d = init(
               "module:" + m.id(),
               m.isAddon() ? "addon-module" : "autism",
               "modules/" + m.category().load(),
               m.name() + ": " + m.description() + AiSourceKnowledge.init(m.getClass()),
               List.of(m.name(), m.category().load()),
               schema,
               "Available in the installed registry; world/container requirements and hidden-state restrictions are checked at execution.",
               "Changes this module's activation/settings. " + m.description(),
               List.of("MODULES"),
               "Module activation and setting readback match the requested values; gameplay outcomes require separate observations.",
               "Inspect current state and availability after rejection. Do not repeatedly enable a self-disabling module.",
               AiJson.objectOf("capability", "module:" + m.id(), "arguments", AiJson.objectOf("enabled", true)).toString(),
               m.settingsShareable() && !m.id().equals("ai-assistant")
                  ? AiCapabilityDescriptor.Classification.CONFIGURE
                  : AiCapabilityDescriptor.Classification.DISCOVER_ONLY
            );
            addEntry(entries, d, "{}");
         }

         for (MacroActionType type : MacroActionType.values()) {
            CompoundTag tag = new CompoundTag();
            tag.putString("type", type.name());
            MacroAction action = autismclient.util.AutismMacro.deserializeAction(tag);
            if (action == null) {
               throw new IllegalStateException("Built-in macro type has no catalog adapter: " + type.name());
            }

            addActionEntry(entries, type.name(), action, autismclient.gui.macro.editor.ActionFieldRegistry.init(type), "autism", "");
         }

         for (MacroActionEntry entry : MacroActionRegistry.entries()) {
            Supplier<MacroAction> factory = MacroActionRegistry.factory(entry.typeId());
            if (factory != null) {
               try {
                  MacroAction addonAction = factory.get();
                  if (addonAction == null) {
                     AutismClientAddon.LOG.warn("[AI] Addon macro action '{}' produced no instance; skipping its catalog entry", entry.typeId());
                  } else {
                     addActionEntry(
                        entries,
                        entry.typeId(),
                        addonAction,
                        MacroActionRegistry.schema(entry.typeId()),
                        Objects.toString(MacroActionRegistry.ownerOf(entry.typeId()), "addon"),
                        entry.pickerTip()
                     );
                  }
               } catch (Throwable t) {
                  AutismClientAddon.LOG.warn("[AI] Addon macro action '{}' failed catalog construction; skipping", entry.typeId(), t);
               }
            }
         }

         for (Command command : autismclient.commands.CommandManager.getCommands()) {
            addEntry(entries, commandDescriptor(command, autismclient.commands.CommandManager.getPrimaryName(command), autismclient.commands.CommandManager.getUsageForms(command)), "{}");
         }

         for (String id : CustomMenuAdapterRegistry.ids()) {
            addEntry(
               entries,
               init(
                  "menu:" + id,
                  "autism",
                  "server-menus",
                  "Server custom-menu adapter " + id,
                  List.of("form", "server", "menu"),
                  "{}",
                  "An active matching custom-menu snapshot is required.",
                  "Discover adapter; use CUSTOM_MENU macro actions with a fresh menu snapshot.",
                  List.of("GUI"),
                  "Fresh menu generation and server response confirm submission.",
                  "Reobserve after a menu generation change.",
                  "Inspect macro:CUSTOM_MENU before constructing a submission.",
                  AiCapabilityDescriptor.Classification.DISCOVER_ONLY
               ),
               "{}"
            );
         }

         for (AiCapabilityDescriptor d : AiTools.capabilities()) {
            addEntry(entries, d, "{}");
         }

         for (AiCapabilities.Entry e : AiCapabilities.entries()) {
            addEntry(entries, e.descriptor(), "{}");
         }

         Set<String> ids = new HashSet<>();
         List<AiCatalog.Entry> unique = new ArrayList<>(entries.size());

         for (AiCatalog.Entry e : entries) {
            if (ids.add(e.contract().id())) {
               unique.add(e);
            } else {
               AutismClientAddon.LOG.warn("[AI] Duplicate capability '{}'; keeping the first registration", e.contract().id());
            }
         }

         this.init_AiCatalog_Snapshot = new AiCatalog.Snapshot(AiJson.sha256Hex(AiJson.GSON.toJson(unique)), unique);
         this.init_J = source;
      }
   }

   private static Double finiteOrNull(double d) {
      return Double.isFinite(d) ? d : null;
   }

   static AiCapabilityDescriptor commandDescriptor(Command command, String name, List<String> forms) {
      List<String> shown = forms.size() > 40 ? forms.subList(0, 40) : forms;
      String syntax = String.join(" | ", shown) + (shown.size() < forms.size() ? " | ... " + (forms.size() - shown.size()) + " more" : "");
      String description = command.description().isBlank() ? "Runs the " + name + " client command." : command.description().trim();
      if (!description.endsWith(".")) {
         description = description + ".";
      }

      List<String> aliases = Arrays.stream(command.aliases()).filter(a -> a != null && !a.isBlank()).toList();
      JsonArray calls = new JsonArray();

      for (String form : shown) {
         if (!form.contains("<")) {
            calls.add(AiJson.objectOf("capability", "command:" + name, "arguments", AiJson.objectOf("body", form)));
         }
      }

      if (calls.isEmpty()) {
         calls.add(AiJson.objectOf("capability", "command:" + name, "arguments", AiJson.objectOf("body", shown.get(0))));
      }

      String schema = AiJson.objectOf(
            "type",
            "object",
            "properties",
            AiJson.objectOf(
               "body", AiJson.objectOf("type", "string", "description", "Chat line without the client prefix. Forms: " + syntax)
            )
         )
         .toString();
      return init(
         "command:" + name,
         "autism",
         "commands",
         name + ": " + description + " Syntax: " + syntax + "." + (aliases.isEmpty() ? "" : " Aliases: " + String.join(", ", aliases) + "."),
         aliases,
         schema,
         "A living player in a world. body is the line after the client prefix; the host strips a prefix and tolerates an alias, letter case or a missing command word.",
         "Runs the client-side command exactly as if typed in chat. " + description,
         List.of("NETWORK", "WORLD_ACTION"),
         "SUCCEEDED means the client parsed and ran the line; data.replies[] holds the command's own chat replies (a red reply FAILS the call with its text). Read them for the outcome instead of re-observing.",
         "A parse FAILURE lists every accepted form: fix the body and perform again. Never repeat a refused line unchanged.",
         AiJson.objectOf("note", "replace <placeholders> with values", "calls", calls).toString(),
         AiCapabilityDescriptor.Classification.ACTION
      );
   }

   static void addActionEntry(List<AiCatalog.Entry> entries, String id, MacroAction action, ActionFieldSchema schema, String owner, String tip) {
      String defaults = NbtOps.INSTANCE.convertTo(JsonOps.INSTANCE, autismclient.util.AutismMacro.serializeAction(action)).toString();
      String fields = AiJson.objectOf(
            "fields",
            schema == null ? List.of() : schema.fields(),
            "serializedDefaults",
            AiJson.parseObject(defaults),
            "dynamicBindings",
            "Optional _dynamicBindings object maps schema fields marked dynamic to native {variable} templates. Keep typed fallback values in ordinary fields."
         )
         .toString();
      AiCapabilityDescriptor contributed = AiCapabilities.entries()
         .stream()
         .filter(e -> e.owner().equals(owner) && e.descriptor().id().equals(id))
         .map(AiCapabilities.Entry::descriptor)
         .findFirst()
         .orElse(null);
      String semantics = (
            action.getType() == null ? "Legacy add-on action; behavior is undocumented until its owner contributes an AI contract." : AiMacroValidator.init(action.getType())
         )
         + AiSourceKnowledge.init(action.getClass());
      if (contributed != null) {
         semantics = "Owning contract v"
            + contributed.version()
            + ". "
            + contributed.purpose()
            + " "
            + contributed.effects()
            + " Prerequisites: "
            + contributed.prerequisites()
            + " Recovery: "
            + contributed.failureRecovery();
      }

      AiCapabilityDescriptor descriptor = init(
         "macro:" + id,
         owner,
         "macros/" + (id.startsWith("WAIT") ? "conditions" : "actions"),
         action.getDisplayName() + ". " + tip + " " + semantics,
         List.of(id.toLowerCase(Locale.ROOT).replace('_', ' ')),
         fields,
         "Use serializedDefaults as the wire format. Validate fields, control flow, live targets, and action-specific world prerequisites before execution.",
         semantics,
         contributed == null ? autismclient.util.macro.MacroResourceClassifier.init(action).stream().map(Enum::name).toList() : contributed.resources(),
         "Verify the macro run's terminal status, then check the intended game-state postcondition.",
         "Inspect the failed step and live targets; missing add-ons or unavailable navigation support cannot be repaired by repeating the same action.",
         defaults,
         schema == null && contributed == null || action.getType() == null && contributed == null
            ? AiCapabilityDescriptor.Classification.DISCOVER_ONLY
            : AiCapabilityDescriptor.Classification.ACTION
      );
      if (contributed != null) {
         descriptor = new AiCapabilityDescriptor(
            descriptor.id(),
            descriptor.owner(),
            contributed.version(),
            descriptor.family(),
            descriptor.purpose(),
            descriptor.aliases(),
            descriptor.inputSchema(),
            descriptor.prerequisites(),
            descriptor.effects(),
            descriptor.resources(),
            descriptor.successCriteria(),
            descriptor.failureRecovery(),
            descriptor.examples(),
            descriptor.classification()
         );
      }

      addEntryWith(entries, descriptor, defaults, AiSourceKnowledge.load(action.getClass()) + AiSourceKnowledge.load(autismclient.util.AutismMacro.class) + AiSourceKnowledge.load(AiMacroValidator.class));
   }

   static AiCapabilityDescriptor init(
      String id,
      String owner,
      String family,
      String purpose,
      List<String> aliases,
      String schema,
      String prerequisites,
      String effects,
      List<String> resources,
      String success,
      String recovery,
      String examples,
      AiCapabilityDescriptor.Classification classification
   ) {
      return new AiCapabilityDescriptor(
         id, owner, 1, family, purpose, aliases, schema, prerequisites, effects, resources, success, recovery, examples, classification
      );
   }

   private static void addEntry(List<AiCatalog.Entry> entries, AiCapabilityDescriptor d, String defaults) {
      String dependency = "";
      if (d.id().startsWith("module:")) {
         Module module = autismclient.modules.al.init_Module_Str(d.id().substring(7));
         if (module != null) {
            dependency = AiSourceKnowledge.load(module.getClass());
         }
      } else if (d.id().startsWith("command:")) {
         dependency = autismclient.commands.CommandManager.getCommands()
            .stream()
            .filter(c -> d.id().equals("command:" + autismclient.commands.CommandManager.getPrimaryName(c)))
            .map(c -> AiSourceKnowledge.load(c.getClass()))
            .findFirst()
            .orElse("");
      } else if (d.owner().equals("autism")) {
         dependency = AiSourceKnowledge.load(AiGameOperations.class)
            + AiSourceKnowledge.load(AiGameplay.class)
            + AiSourceKnowledge.load(AiInventoryController.class)
            + AiSourceKnowledge.load(AiContainerTransactions.class)
            + AiSourceKnowledge.load(AiExpectations.class)
            + AiSourceKnowledge.load(AiTerrain.class)
            + AiSourceKnowledge.load(AiChatOps.class)
            + AiSourceKnowledge.load(AiChatLedger.class)
            + AiSourceKnowledge.load(AiPacketLog.class)
            + AiSourceKnowledge.load(AiPacketSend.class)
            + AiSourceKnowledge.load(AiServerIntel.class)
            + AiSourceKnowledge.load(AiLanSync.class)
            + AiSourceKnowledge.load(AiWorldReaders.class)
            + AiSourceKnowledge.load(AiScreens.class)
            + AiSourceKnowledge.load(AiSurvival.class)
            + AiSourceKnowledge.load(AiBuilder.class)
            + AiSourceKnowledge.load(AiCommandOp.class)
            + AiSourceKnowledge.load(AiEnchant.class)
            + AiSourceKnowledge.load(AiOracle.class)
            + AiSourceKnowledge.load(AiAddons.class)
            + AiSourceKnowledge.load(AiTelly.class)
            + AiSourceKnowledge.load(AiScaffoldWalk.class)
            + AiSourceKnowledge.load(AiDodge.class)
            + AiSourceKnowledge.load(AiProjectiles.class)
            + AiSourceKnowledge.load(AiDanger.class)
            + AiSourceKnowledge.load(AiMlg.class)
            + AiSourceKnowledge.load(AiWorldCreate.class)
            + AiSourceKnowledge.load(AiChop.class)
            + AiSourceKnowledge.load(AiCollect.class)
            + AiSourceKnowledge.load(AiItems.class)
            + AiSourceKnowledge.load(AiCraft.class)
            + AiSourceKnowledge.load(AiCraftPlanner.class)
            + AiSourceKnowledge.load(AiObtain.class)
            + AiSourceKnowledge.load(AiCombat.class)
            + AiSourceKnowledge.load(AiContainer.class)
            + AiSourceKnowledge.load(AiEquip.class)
            + AiSourceKnowledge.load(AiSmelt.class)
            + AiSourceKnowledge.load(AiTrade.class)
            + AiSourceKnowledge.load(AiFarm.class)
            + AiSourceKnowledge.load(AiDimension.class)
            + AiSourceKnowledge.load(AiProspect.class)
            + AiSourceKnowledge.load(AiGuard.class)
            + AiSourceKnowledge.load(AiDeathLog.class)
            + AiSourceKnowledge.load(AiRecover.class)
            + AiSourceKnowledge.load(AiPlan.class)
            + AiSourceKnowledge.load(AiPlanRunner.class)
            + AiSourceKnowledge.load(AiSleep.class)
            + AiSourceKnowledge.load(AiFish.class)
            + AiSourceKnowledge.load(AiBucket.class)
            + AiSourceKnowledge.load(AiRide.class)
            + AiSourceKnowledge.load(AiRedstone.class)
            + AiSourceKnowledge.load(AiRanged.class)
            + AiSourceKnowledge.load(AiTidy.class)
            + AiSourceKnowledge.load(autismclient.ai.engine.AiAim.class)
            + AiSourceKnowledge.load(autismclient.ai.engine.AiPlacer.class)
            + AiSourceKnowledge.load(autismclient.ai.engine.AiPathfinder.class)
            + AiSourceKnowledge.load(autismclient.ai.engine.AiWrExecutor.class)
            + AiSourceKnowledge.load(autismclient.ai.engine.AiPaceScript.class)
            + AiSourceKnowledge.load(autismclient.ai.engine.AiPacePlayback.class)
            + AiSourceKnowledge.load(autismclient.ai.engine.AiWallJump.class)
            + AiSourceKnowledge.load(autismclient.ai.engine.AiLadderJump.class)
            + AiSourceKnowledge.load(autismclient.ai.engine.AiSlimeBounce.class)
            + AiSourceKnowledge.load(AiParkourRoute.class)
            + AiSourceKnowledge.load(AiGoto.class)
            + AiSourceKnowledge.load(autismclient.ai.engine.AiMine.class);
      }

      addEntryWith(entries, d, defaults, dependency);
   }

   private static void addEntryWith(List<AiCatalog.Entry> entries, AiCapabilityDescriptor d, String defaults, String dependency) {
      entries.add(new AiCatalog.Entry(d, AiJson.sha256Hex(AiJson.GSON.toJson(d) + defaults + dependency), defaults));
   }

   public record Entry(AiCapabilityDescriptor contract, String fingerprint, String defaultsJson) {
   }

   public record Snapshot(String revision, List<AiCatalog.Entry> entries) {
      public Snapshot {
         entries = List.copyOf(entries);
      }

      public AiCatalog.Entry get(String id) {
         return this.entries.stream().filter(e -> e.contract().id().equals(id)).findFirst().orElse(null);
      }

      public List<AiCatalog.Entry> search(String query, int offset, int limit) {
         String[] terms = query.toLowerCase(Locale.ROOT).split("[^a-z0-9_]+");
         return this.entries
            .stream()
            .filter(e -> query.isBlank() || score(e, terms) > 0)
            .sorted(Comparator.<AiCatalog.Entry>comparingInt(e -> score(e, terms)).reversed().thenComparing(e -> e.contract().id()))
            .skip(Math.max(0, offset))
            .limit(Math.max(1, Math.min(50, limit)))
            .toList();
      }

      private static int score(AiCatalog.Entry e, String[] terms) {
         String title = e.contract().id().toLowerCase(Locale.ROOT);
         String text = (e.contract().purpose() + " " + e.contract().family() + " " + e.contract().aliases()).toLowerCase(Locale.ROOT);
         int n = 0;

         for (String term : terms) {
            if (!term.isBlank()) {
               if (title.contains(term)) {
                  n += 5;
               }

               if (text.contains(term)) {
                  n++;
               }
            }
         }

         return n;
      }

      public Map<String, Long> families() {
         Map<String, Long> map = new TreeMap<>();

         for (AiCatalog.Entry e : this.entries) {
            map.merge(e.contract().family(), 1L, Long::sum);
         }

         return map;
      }
   }
}

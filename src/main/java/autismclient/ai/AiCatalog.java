package autismclient.ai;

import autismclient.AutismClientAddon;
import autismclient.ai.a.HashUtil;
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
import autismclient.util.macro.StreamGate;
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

   public AiCatalog.Snapshot init_AiCatalog_Snapshot() {
      return this.init_AiCatalog_Snapshot;
   }

   public void init_V() {
      long source = Objects.hash(
         autismclient.modules.al.init_I(),
         autismclient.commands.CommandManager.init_I(),
         AiCapabilities.revision(),
         ck.init_Str(),
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
                  ag.init_JsonObject_ObjArr(
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
                     init(s.min()),
                     "max",
                     init(s.max()),
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

            String schema = ag.init_JsonObject_ObjArr(
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
               m.name() + ": " + m.description() + ck.init(m.getClass()),
               List.of(m.name(), m.category().load()),
               schema,
               "Available in the installed registry; world/container requirements and hidden-state restrictions are checked at execution.",
               "Changes this module's activation/settings. " + m.description(),
               List.of("MODULES"),
               "Module activation and setting readback match the requested values; gameplay outcomes require separate observations.",
               "Inspect current state and availability after rejection. Do not repeatedly enable a self-disabling module.",
               ag.init_JsonObject_ObjArr("capability", "module:" + m.id(), "arguments", ag.init_JsonObject_ObjArr("enabled", true)).toString(),
               m.settingsShareable() && !m.id().equals("ai-assistant")
                  ? AiCapabilityDescriptor.Classification.CONFIGURE
                  : AiCapabilityDescriptor.Classification.DISCOVER_ONLY
            );
            init(entries, d, "{}");
         }

         for (StreamGate type : StreamGate.values()) {
            CompoundTag tag = new CompoundTag();
            tag.putString("type", type.name());
            MacroAction action = autismclient.util.AutismMacro.init_MacroAction_CompoundTag(tag);
            if (action == null) {
               throw new IllegalStateException("Built-in macro type has no catalog adapter: " + type.name());
            }

            init(entries, type.name(), action, autismclient.gui.macro.editor.ActionFieldRegistry.init(type), "autism", "");
         }

         for (MacroActionEntry entry : MacroActionRegistry.entries()) {
            Supplier<MacroAction> factory = MacroActionRegistry.factory(entry.typeId());
            if (factory != null) {
               try {
                  MacroAction addonAction = factory.get();
                  if (addonAction == null) {
                     AutismClientAddon.LOG.warn("[AI] Addon macro action '{}' produced no instance; skipping its catalog entry", entry.typeId());
                  } else {
                     init(
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

         for (Command command : autismclient.commands.CommandManager.init_List()) {
            init(entries, init(command, autismclient.commands.CommandManager.init_Str(command), autismclient.commands.CommandManager.init_List(command)), "{}");
         }

         for (String id : CustomMenuAdapterRegistry.ids()) {
            init(
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

         for (AiCapabilityDescriptor d : cz.init_List()) {
            init(entries, d, "{}");
         }

         for (AiCapabilities.Entry e : AiCapabilities.entries()) {
            init(entries, e.descriptor(), "{}");
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

         this.init_AiCatalog_Snapshot = new AiCatalog.Snapshot(ag.init_Str_Str(ag.init.toJson(unique)), unique);
         this.init_J = source;
      }
   }

   private static Double init(double d) {
      return Double.isFinite(d) ? d : null;
   }

   static AiCapabilityDescriptor init(Command command, String name, List<String> forms) {
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
            calls.add(ag.init_JsonObject_ObjArr("capability", "command:" + name, "arguments", ag.init_JsonObject_ObjArr("body", form)));
         }
      }

      if (calls.isEmpty()) {
         calls.add(ag.init_JsonObject_ObjArr("capability", "command:" + name, "arguments", ag.init_JsonObject_ObjArr("body", shown.get(0))));
      }

      String schema = ag.init_JsonObject_ObjArr(
            "type",
            "object",
            "properties",
            ag.init_JsonObject_ObjArr(
               "body", ag.init_JsonObject_ObjArr("type", "string", "description", "Chat line without the client prefix. Forms: " + syntax)
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
         ag.init_JsonObject_ObjArr("note", "replace <placeholders> with values", "calls", calls).toString(),
         AiCapabilityDescriptor.Classification.ACTION
      );
   }

   static void init(List<AiCatalog.Entry> entries, String id, MacroAction action, ActionFieldSchema schema, String owner, String tip) {
      String defaults = NbtOps.INSTANCE.convertTo(JsonOps.INSTANCE, autismclient.util.AutismMacro.init_CompoundTag_MacroAction(action)).toString();
      String fields = ag.init_JsonObject_ObjArr(
            "fields",
            schema == null ? List.of() : schema.fields(),
            "serializedDefaults",
            ag.init_JsonObject_Str(defaults),
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
            action.getType() == null ? "Legacy add-on action; behavior is undocumented until its owner contributes an AI contract." : an.init(action.getType())
         )
         + ck.init(action.getClass());
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
         contributed == null ? autismclient.util.macro.DataPipe.init(action).stream().map(Enum::name).toList() : contributed.resources(),
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

      init(entries, descriptor, defaults, ck.load(action.getClass()) + ck.load(autismclient.util.AutismMacro.class) + ck.load(an.class));
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

   private static void init(List<AiCatalog.Entry> entries, AiCapabilityDescriptor d, String defaults) {
      String dependency = "";
      if (d.id().startsWith("module:")) {
         Module module = autismclient.modules.al.init_Module_Str(d.id().substring(7));
         if (module != null) {
            dependency = ck.load(module.getClass());
         }
      } else if (d.id().startsWith("command:")) {
         dependency = autismclient.commands.CommandManager.init_List()
            .stream()
            .filter(c -> d.id().equals("command:" + autismclient.commands.CommandManager.init_Str(c)))
            .map(c -> ck.load(c.getClass()))
            .findFirst()
            .orElse("");
      } else if (d.owner().equals("autism")) {
         dependency = ck.load(R.class)
            + ck.load(T.class)
            + ck.load(ae.class)
            + ck.load(PkgCls_b.class)
            + ck.load(y.class)
            + ck.load(cu.class)
            + ck.load(MethodUtil.class)
            + ck.load(TokenStore.class)
            + ck.load(aI.class)
            + ck.load(aJ.class)
            + ck.load(cb.class)
            + ck.load(ai.class)
            + ck.load(cN.class)
            + ck.load(bY.class)
            + ck.load(cq.class)
            + ck.load(TableIndex.class)
            + ck.load(UtilCore.class)
            + ck.load(s.class)
            + ck.load(aG.class)
            + ck.load(CachePool.class)
            + ck.load(ct.class)
            + ck.load(bN.class)
            + ck.load(p.class)
            + ck.load(by.class)
            + ck.load(i.class)
            + ck.load(au.class)
            + ck.load(cK.class)
            + ck.load(RefQueue.class)
            + ck.load(KeyHolder.class)
            + ck.load(af.class)
            + ck.load(e.class)
            + ck.load(g.class)
            + ck.load(aE.class)
            + ck.load(ValueSink.class)
            + ck.load(PkgCls_a.class)
            + ck.load(w.class)
            + ck.load(ci.class)
            + ck.load(cB.class)
            + ck.load(B.class)
            + ck.load(n.class)
            + ck.load(bz.class)
            + ck.load(X.class)
            + ck.load(j.class)
            + ck.load(bE.class)
            + ck.load(aS.class)
            + ck.load(aU.class)
            + ck.load(ch.class)
            + ck.load(L.class)
            + ck.load(TextFormat.class)
            + ck.load(bI.class)
            + ck.load(bF.class)
            + ck.load(bC.class)
            + ck.load(cx.class)
            + ck.load(autismclient.ai.a.DataUtil.class)
            + ck.load(autismclient.ai.a.DataPipe.class)
            + ck.load(autismclient.ai.a.ClassCache.class)
            + ck.load(autismclient.ai.a.AiWrExecutor.class)
            + ck.load(autismclient.ai.a.AiPaceScript.class)
            + ck.load(autismclient.ai.a.AiPacePlayback.class)
            + ck.load(autismclient.ai.a.AiWallJump.class)
            + ck.load(autismclient.ai.a.AiLadderJump.class)
            + ck.load(autismclient.ai.a.AiSlimeBounce.class)
            + ck.load(AiParkourRoute.class)
            + ck.load(HashUtil.class)
            + ck.load(autismclient.ai.a.TokenStore.class);
      }

      init(entries, d, defaults, dependency);
   }

   private static void init(List<AiCatalog.Entry> entries, AiCapabilityDescriptor d, String defaults, String dependency) {
      entries.add(new AiCatalog.Entry(d, ag.init_Str_Str(ag.init.toJson(d) + defaults + dependency), defaults));
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

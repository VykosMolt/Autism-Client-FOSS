package autismclient.commands;

import autismclient.AutismClientAddon;
import autismclient.api.AddonRegistrationResult;
import autismclient.commands.impl.PrefixCommand;
import autismclient.commands.impl.ChangeSlotCommand;
import autismclient.commands.impl.FakeGmCommand;
import autismclient.commands.impl.ToolsCommand;
import autismclient.commands.impl.RemoteViewCommand;
import autismclient.commands.impl.ClearCommand;
import autismclient.commands.impl.SyncCommand;
import autismclient.commands.impl.DisconnectCommand;
import autismclient.commands.impl.DismountCommand;
import autismclient.commands.impl.GamemodeCommand;
import autismclient.commands.impl.GhostBlockCommand;
import autismclient.commands.impl.SendCommand;
import autismclient.commands.impl.OreSimCommand;
import autismclient.commands.impl.PluginsCommand;
import autismclient.commands.impl.ModulesCommand;
import autismclient.commands.impl.ClickSlotCommand;
import autismclient.commands.impl.SayCommand;
import autismclient.commands.impl.FriendCommand;
import autismclient.commands.impl.GiveCommand;
import autismclient.commands.impl.NbtCommand;
import autismclient.commands.impl.DelayCommand;
import autismclient.commands.impl.HelpCommand;
import autismclient.commands.impl.CommandsCommand;
import autismclient.commands.impl.ToggleCommand;
import autismclient.commands.impl.CopyPosCommand;
import autismclient.commands.impl.HClipCommand;
import autismclient.commands.impl.ClickItemCommand;
import autismclient.commands.impl.NameScrapeCommand;
import autismclient.commands.impl.DropCommand;
import autismclient.commands.impl.MacroCommand;
import autismclient.commands.impl.DamageCommand;
import autismclient.commands.impl.TpCommand;
import autismclient.commands.impl.ServerCommand;
import autismclient.commands.impl.VClipCommand;
import autismclient.commands.impl.WaypointsCommand;
import autismclient.commands.impl.XCarryCommand;
import autismclient.modules.aD;
import autismclient.util.AutismClientMessaging;
import autismclient.util.AutismLiteVariant;
import autismclient.util.CommandPrefixHelper;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.tree.CommandNode;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import net.minecraft.client.multiplayer.ClientPacketListener;

public final class CommandManager {
   private static final boolean init_Z = false;
   private static final CommandDispatcher<AutismCommandSource> init_CommandDispatcher = new CommandDispatcher<>();
   private static final List<Command> init_List = new ArrayList<>();
   private static final Map<String, Command> init_Map = new LinkedHashMap<>();
   private static final Map<String, String> load_Map = new LinkedHashMap<>();
   private static final Map<Command, String> read = new IdentityHashMap<>();
   private static final Map<Command, String> write = new IdentityHashMap<>();
   private static final Set<String> init_Set = new HashSet<>();
   private static final List<String> load_List = List.of(".", ",", ";", ":", "'", "\"", "\\", "|", "-", "_", "+", "=", "*", "#", "@", "!", "$", "%", "&", "~");
   private static boolean load_Z = false;
   private static int init_I;
   private static final ThreadLocal<Boolean> init_ThreadLocal = ThreadLocal.withInitial(() -> false);

   private CommandManager() {
   }

   public static synchronized void init_V() {
      if (!load_Z) {
         load_Z = true;
         init_V(new MacroCommand());
         init_V(new SyncCommand());
         init_V(new DelayCommand());
         init_V(new SendCommand());
         init_V(new VClipCommand());
         init_V(new HClipCommand());
         init_V(new TpCommand());
         init_V(new CopyPosCommand());
         init_V(new NbtCommand());
         init_V(new ServerCommand());
         init_V(new PluginsCommand());
         init_V(new PrefixCommand());
         init_V(new CommandsCommand());
         init_V(new HelpCommand());
         init_V(new autismclient.commands.impl.BindsCommand());
         init_V(new autismclient.commands.impl.BindCommand());
         init_V(new ClearCommand());
         init_V(new DismountCommand());
         init_V(new DisconnectCommand());
         init_V(new SayCommand());
         init_V(new DropCommand());
         init_V(new ChangeSlotCommand());
         init_V(new ClickSlotCommand());
         init_V(new ClickItemCommand());
         init_V(new GiveCommand());
         init_V(new DamageCommand());
         if (!AutismLiteVariant.enabled()) {
            init_V(new ToolsCommand());
            init_V(new autismclient.commands.impl.AiCommand());
            init_V(new ToggleCommand());
            init_V(new ModulesCommand());
            init_V(new OreSimCommand());
            init_V(new GhostBlockCommand());
            init_V(new FriendCommand());
            init_V(new WaypointsCommand());
            init_V(new GamemodeCommand());
            init_V(new FakeGmCommand());
            init_V(new XCarryCommand());
            init_V(new RemoteViewCommand());
            init_V(new NameScrapeCommand());
         }
      }
   }

   private static void init_V(Command command) {
      init(command, command.name(), true);
   }

   private static List<String> init(Command command, String primaryName, boolean registerAliases) {
      List<String> registeredNames = new ArrayList<>();
      init_I++;
      init_List.add(command);
      write.put(command, primaryName);
      LiteralArgumentBuilder<AutismCommandSource> primary = LiteralArgumentBuilder.literal(primaryName);
      command.build(primary);
      init_CommandDispatcher.register(primary);
      init_Map.put(primaryName.toLowerCase(Locale.ROOT), command);
      init_Set.remove(primaryName.toLowerCase(Locale.ROOT));
      registeredNames.add(primaryName.toLowerCase(Locale.ROOT));
      if (!registerAliases) {
         return registeredNames;
      }

      for (String alias : command.aliases()) {
         if (alias != null && !alias.isBlank() && !init_Map.containsKey(alias.toLowerCase(Locale.ROOT))) {
            LiteralArgumentBuilder<AutismCommandSource> aliasBuilder = LiteralArgumentBuilder.literal(alias);
            command.build(aliasBuilder);
            init_CommandDispatcher.register(aliasBuilder);
            init_Map.put(alias.toLowerCase(Locale.ROOT), command);
            init_Set.remove(alias.toLowerCase(Locale.ROOT));
            registeredNames.add(alias.toLowerCase(Locale.ROOT));
         }
      }

      return registeredNames;
   }

   public static synchronized void init_V(Command command, String addonId) {
      init_AddonRegistrationR(command, addonId);
   }

   public static synchronized AddonRegistrationResult init_AddonRegistrationR(Command command, String addonId) {
      if (command == null) {
         return AddonRegistrationResult.rejected("command", "", "command was null");
      }

      if (addonId != null && !addonId.isBlank()) {
         String name = command.name();
         if (name != null && !name.isBlank()) {
            boolean collision = init_Map.containsKey(name.toLowerCase(Locale.ROOT));
            List<String> registered;
            if (collision) {
               String namespaced = (addonId != null && !addonId.isBlank() ? addonId : "addon") + ":" + name;
               AutismClientAddon.LOG
                  .warn(
                     "[Commands] Command name '{}' from addon '{}' collides with an existing command; registering it as '{}' instead",
                     name,
                     addonId,
                     namespaced
                  );
               registered = init(command, namespaced, false);
            } else {
               registered = init(command, name, true);
            }

            read.put(command, addonId);

            for (String registeredName : registered) {
               load_Map.put(registeredName, addonId);
            }

            String id = registered.isEmpty() ? name : registered.get(0);
            autismclient.b.DataUtil.init_V("command", id);
            return AddonRegistrationResult.accepted("command", id);
         } else {
            return init(addonId, "", "blank command name");
         }
      } else {
         return init(addonId, "", "registration outside an addon lifecycle - register from onInitialize() or onRegisterCategories()");
      }
   }

   private static AddonRegistrationResult init(String addonId, String id, String reason) {
      AutismClientAddon.LOG.warn("[Commands] Rejecting addon command '{}': {}", id, reason);
      autismclient.b.DataUtil.init(addonId, "command", id, reason);
      return AddonRegistrationResult.rejected("command", id, reason);
   }

   public static synchronized void init_V(String addonId) {
      if (addonId != null && !addonId.isBlank()) {
         List<String> removeNames = new ArrayList<>();

         for (Entry<String, String> owner : load_Map.entrySet()) {
            if (addonId.equals(owner.getValue())) {
               removeNames.add(owner.getKey());
            }
         }

         for (String name : removeNames) {
            init_Map.remove(name);
            load_Map.remove(name);
            init_Set.add(name);
            auditRemoveRootLiteral(name);
         }

         init_List.removeIf(command -> addonId.equals(read.get(command)));
         write.keySet().removeIf(command -> addonId.equals(read.get(command)));
         read.entrySet().removeIf(entry -> addonId.equals(entry.getValue()));
         if (!removeNames.isEmpty()) {
            init_I++;
         }
      }
   }

   @SuppressWarnings("unchecked")
   private static void auditRemoveRootLiteral(String lowerName) {
      if (lowerName == null || lowerName.isEmpty()) {
         return;
      }

      try {
         Object root = init_CommandDispatcher.getRoot();
         java.lang.reflect.Field childrenField = CommandNode.class.getDeclaredField("children");
         childrenField.setAccessible(true);
         Map<String, ?> children = (Map<String, ?>)childrenField.get(root);
         if (children != null) {
            children.keySet().removeIf(k -> k != null && k.equalsIgnoreCase(lowerName));
         }

         java.lang.reflect.Field literalsField = CommandNode.class.getDeclaredField("literals");
         literalsField.setAccessible(true);
         Map<String, ?> literals = (Map<String, ?>)literalsField.get(root);
         if (literals != null) {
            literals.keySet().removeIf(k -> k != null && k.equalsIgnoreCase(lowerName));
         }
      } catch (Throwable t) {
         AutismClientAddon.LOG.warn("[Commands] Failed to remove Brigadier root node '{}'", lowerName, t);
      }
   }

   public static CommandDispatcher<AutismCommandSource> init_CommandDispatcher() {
      return init_CommandDispatcher;
   }

   public static int init_I() {
      return init_I;
   }

   public static List<Command> init_List() {
      return Collections.unmodifiableList(init_List);
   }

   public static synchronized String init_Str(Command command) {
      String name = write.get(command);
      return name == null ? command.name() : name;
   }

   public static synchronized List<String> init_List(Command command) {
      String name = init_Str(command);
      CommandNode<AutismCommandSource> node = init_CommandDispatcher.getRoot().getChild(name);
      return node == null ? List.of(name) : init(node, name);
   }

   public static List<String> init(CommandNode<AutismCommandSource> node, String name) {
      List<String> forms = new ArrayList<>();

      for (String usage : new CommandDispatcher<AutismCommandSource>().getAllUsage(node, AutismCommandSource.INSTANCE, false)) {
         forms.add(usage.isEmpty() ? name : name + " " + usage);
      }

      if (forms.isEmpty()) {
         forms.add(name);
      }

      return forms;
   }

   public static Command init_Command(String nameOrAlias) {
      return nameOrAlias == null ? null : init_Map.get(nameOrAlias.trim().toLowerCase(Locale.ROOT));
   }

   public static String init_Str() {
      return CommandPrefixHelper.init_Str();
   }

   public static boolean init_Z(String message) {
      if (message != null && !message.isBlank()) {
         String trimmed = message.trim();
         String prefix = init_Str();
         return !prefix.isEmpty() && trimmed.startsWith(prefix);
      } else {
         return false;
      }
   }

   public static boolean init_Z() {
      return aD.read_Z();
   }

   public static boolean load_Z(String message) {
      return init_Z() && write(message);
   }

   private static boolean write(String message) {
      if (message != null && !message.isBlank()) {
         String trimmed = message.trim();
         if ("^toggleautism".equalsIgnoreCase(trimmed)) {
            return true;
         }

         String prefix = init_Str();
         if (!prefix.isEmpty() && trimmed.startsWith(prefix)) {
            return true;
         }

         for (String fallback : load_List) {
            if (fallback != null && !fallback.isEmpty() && !fallback.equals(prefix) && trimmed.startsWith(fallback)) {
               String body = trimmed.substring(fallback.length()).trim();
               if (!body.isEmpty()) {
                  String first = load_Str(body).toLowerCase(Locale.ROOT);
                  if (init_Map.containsKey(first) || init_Set.contains(first)) {
                     return true;
                  }
               }
            }
         }

         return false;
      } else {
         return false;
      }
   }

   public static String init_Str(String message) {
      if (!init_Z(message)) {
         return "";
      }

      String trimmed = message.trim();
      int prefixLength = init_Str().length();
      return trimmed.length() <= prefixLength ? "" : trimmed.substring(prefixLength).trim();
   }

   public static boolean load() {
      return init_ThreadLocal.get();
   }

   public static void init(ClientPacketListener conn, String message) {
      init_ThreadLocal.set(true);

      try {
         conn.sendChat(message);
      } finally {
         init_ThreadLocal.remove();
      }
   }

   public static boolean read(String body) {
      return init_DataUtil_DataUtil(body).init_Z();
   }

   public static CommandManager.Inner_DataUtil init_DataUtil_DataUtil(String body) {
      if (init_Z()) {
         return new CommandManager.Inner_DataUtil(CommandManager.Inner_StreamUtil.BLOCKED, "Client commands are blocked while the client is hidden (panic mode)");
      }

      if (!AutismLiteVariant.enabled() && !autismclient.e.RingBuffer.init()) {
         AutismClientMessaging.sendPrefixed("§cAutism is locked: early access requires the donator role. Sign in to unlock.");
         return new CommandManager.Inner_DataUtil(CommandManager.Inner_StreamUtil.BLOCKED, "Autism is locked: early access requires the donator role");
      }

      if (body == null) {
         return new CommandManager.Inner_DataUtil(CommandManager.Inner_StreamUtil.EMPTY, "");
      }

      String trimmed = body.trim();
      if (trimmed.isEmpty()) {
         return new CommandManager.Inner_DataUtil(CommandManager.Inner_StreamUtil.EMPTY, "");
      }

      if (init_Set.contains(load_Str(trimmed).toLowerCase(Locale.ROOT))) {
         init(trimmed, CommandSyntaxException.BUILT_IN_EXCEPTIONS.dispatcherUnknownCommand().create());
         return new CommandManager.Inner_DataUtil(CommandManager.Inner_StreamUtil.UNKNOWN_COMMAND, "Unknown command: " + load_Str(trimmed) + " (its add-on is disabled)");
      }

      try {
         init_CommandDispatcher.execute(trimmed, AutismCommandSource.INSTANCE);
         return new CommandManager.Inner_DataUtil(CommandManager.Inner_StreamUtil.EXECUTED, "");
      } catch (CommandSyntaxException e) {
         init(trimmed, e);
         String message = e.getMessage() != null && !e.getMessage().isBlank() ? e.getMessage() : "Incomplete or invalid command";
         boolean unknown = init_Command(load_Str(trimmed)) == null;
         return new CommandManager.Inner_DataUtil(
            unknown ? CommandManager.Inner_StreamUtil.UNKNOWN_COMMAND : CommandManager.Inner_StreamUtil.SYNTAX_ERROR, unknown ? "Unknown command: " + load_Str(trimmed) : message
         );
      } catch (Throwable t) {
         AutismClientAddon.LOG.warn("[Commands] dispatch failed for '{}'", trimmed, t);
         String message = t.getMessage() == null ? t.getClass().getSimpleName() : t.getMessage();
         AutismClientMessaging.sendPrefixed("§cCommand error: " + message);
         return new CommandManager.Inner_DataUtil(CommandManager.Inner_StreamUtil.ERROR, message);
      }
   }

   private static void init(String body, CommandSyntaxException e) {
      String first = load_Str(body);
      Command command = init_Command(first);
      String prefix = init_Str();
      if (command == null) {
         AutismClientMessaging.sendPrefixed("§cUnknown AUTISM command: §f" + first);
         AutismClientMessaging.sendPrefixed("§7Use §f" + prefix + "commands §7or §f" + prefix + "help§7.");
      } else {
         String message = e.getMessage();
         if (message == null || message.isBlank()) {
            message = "Incomplete or invalid command.";
         }

         AutismClientMessaging.sendPrefixed("§c" + message);
         AutismClientMessaging.sendPrefixed("§7Use §f" + prefix + "help " + command.name() + "§7.");
      }
   }

   private static String load_Str(String body) {
      if (body == null) {
         return "";
      }

      String trimmed = body.trim();
      if (trimmed.isEmpty()) {
         return "";
      }

      int space = trimmed.indexOf(32);
      return space < 0 ? trimmed : trimmed.substring(0, space);
   }

   public record Inner_DataUtil(CommandManager.Inner_StreamUtil init_DataUtil_StreamUti, String init_Str) {
      public boolean init_Z() {
         return this.init_DataUtil_StreamUti != CommandManager.Inner_StreamUtil.EMPTY;
      }
   }

   public enum Inner_StreamUtil {
      EXECUTED,
      SYNTAX_ERROR,
      UNKNOWN_COMMAND,
      ERROR,
      BLOCKED,
      EMPTY;
   }
}

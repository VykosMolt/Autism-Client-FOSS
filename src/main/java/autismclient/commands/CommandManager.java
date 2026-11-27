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
   private static final boolean DEBUG_DISPATCH = false;
   private static final CommandDispatcher<AutismCommandSource> DISPATCHER = new CommandDispatcher<>();
   private static final List<Command> COMMANDS = new ArrayList<>();
   private static final Map<String, Command> COMMAND_BY_NAME = new LinkedHashMap<>();
   private static final Map<String, String> ADDON_BY_NAME = new LinkedHashMap<>();
   private static final Map<Command, String> ADDON_BY_COMMAND = new IdentityHashMap<>();
   private static final Map<Command, String> PRIMARY_NAME_BY_COMMAND = new IdentityHashMap<>();
   private static final Set<String> REMOVED_ADDON_NAMES = new HashSet<>();
   private static final List<String> FALLBACK_PREFIXES = List.of(".", ",", ";", ":", "'", "\"", "\\", "|", "-", "_", "+", "=", "*", "#", "@", "!", "$", "%", "&", "~");
   private static boolean initialized = false;
   private static int registryVersion;
   private static final ThreadLocal<Boolean> BYPASS_INTERCEPT = ThreadLocal.withInitial(() -> false);

   private CommandManager() {
   }

   public static synchronized void initialize() {
      if (!initialized) {
         initialized = true;
         registerBuiltIn(new MacroCommand());
         registerBuiltIn(new SyncCommand());
         registerBuiltIn(new DelayCommand());
         registerBuiltIn(new SendCommand());
         registerBuiltIn(new VClipCommand());
         registerBuiltIn(new HClipCommand());
         registerBuiltIn(new TpCommand());
         registerBuiltIn(new CopyPosCommand());
         registerBuiltIn(new NbtCommand());
         registerBuiltIn(new ServerCommand());
         registerBuiltIn(new PluginsCommand());
         registerBuiltIn(new PrefixCommand());
         registerBuiltIn(new CommandsCommand());
         registerBuiltIn(new HelpCommand());
         registerBuiltIn(new autismclient.commands.impl.BindsCommand());
         registerBuiltIn(new autismclient.commands.impl.BindCommand());
         registerBuiltIn(new ClearCommand());
         registerBuiltIn(new DismountCommand());
         registerBuiltIn(new DisconnectCommand());
         registerBuiltIn(new SayCommand());
         registerBuiltIn(new DropCommand());
         registerBuiltIn(new ChangeSlotCommand());
         registerBuiltIn(new ClickSlotCommand());
         registerBuiltIn(new ClickItemCommand());
         registerBuiltIn(new GiveCommand());
         registerBuiltIn(new DamageCommand());
         if (!AutismLiteVariant.enabled()) {
            registerBuiltIn(new ToolsCommand());
            registerBuiltIn(new autismclient.commands.impl.AiCommand());
            registerBuiltIn(new ToggleCommand());
            registerBuiltIn(new ModulesCommand());
            registerBuiltIn(new OreSimCommand());
            registerBuiltIn(new GhostBlockCommand());
            registerBuiltIn(new FriendCommand());
            registerBuiltIn(new WaypointsCommand());
            registerBuiltIn(new GamemodeCommand());
            registerBuiltIn(new FakeGmCommand());
            registerBuiltIn(new XCarryCommand());
            registerBuiltIn(new RemoteViewCommand());
            registerBuiltIn(new NameScrapeCommand());
         }
      }
   }

   private static void registerBuiltIn(Command command) {
      registerInternal(command, command.name(), true);
   }

   private static List<String> registerInternal(Command command, String primaryName, boolean registerAliases) {
      List<String> registeredNames = new ArrayList<>();
      registryVersion++;
      COMMANDS.add(command);
      PRIMARY_NAME_BY_COMMAND.put(command, primaryName);
      LiteralArgumentBuilder<AutismCommandSource> primary = LiteralArgumentBuilder.literal(primaryName);
      command.build(primary);
      DISPATCHER.register(primary);
      COMMAND_BY_NAME.put(primaryName.toLowerCase(Locale.ROOT), command);
      REMOVED_ADDON_NAMES.remove(primaryName.toLowerCase(Locale.ROOT));
      registeredNames.add(primaryName.toLowerCase(Locale.ROOT));
      if (!registerAliases) {
         return registeredNames;
      }

      for (String alias : command.aliases()) {
         if (alias != null && !alias.isBlank() && !COMMAND_BY_NAME.containsKey(alias.toLowerCase(Locale.ROOT))) {
            LiteralArgumentBuilder<AutismCommandSource> aliasBuilder = LiteralArgumentBuilder.literal(alias);
            command.build(aliasBuilder);
            DISPATCHER.register(aliasBuilder);
            COMMAND_BY_NAME.put(alias.toLowerCase(Locale.ROOT), command);
            REMOVED_ADDON_NAMES.remove(alias.toLowerCase(Locale.ROOT));
            registeredNames.add(alias.toLowerCase(Locale.ROOT));
         }
      }

      return registeredNames;
   }

   public static synchronized void registerAddonCommand(Command command, String addonId) {
      registerAddonCommandChecked(command, addonId);
   }

   public static synchronized AddonRegistrationResult registerAddonCommandChecked(Command command, String addonId) {
      if (command == null) {
         return AddonRegistrationResult.rejected("command", "", "command was null");
      }

      if (addonId != null && !addonId.isBlank()) {
         String name = command.name();
         if (name != null && !name.isBlank()) {
            boolean collision = COMMAND_BY_NAME.containsKey(name.toLowerCase(Locale.ROOT));
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
               registered = registerInternal(command, namespaced, false);
            } else {
               registered = registerInternal(command, name, true);
            }

            ADDON_BY_COMMAND.put(command, addonId);

            for (String registeredName : registered) {
               ADDON_BY_NAME.put(registeredName, addonId);
            }

            String id = registered.isEmpty() ? name : registered.get(0);
            autismclient.b.DataUtil.init_V("command", id);
            return AddonRegistrationResult.accepted("command", id);
         } else {
            return rejectAddonCommand(addonId, "", "blank command name");
         }
      } else {
         return rejectAddonCommand(addonId, "", "registration outside an addon lifecycle - register from onInitialize() or onRegisterCategories()");
      }
   }

   private static AddonRegistrationResult rejectAddonCommand(String addonId, String id, String reason) {
      AutismClientAddon.LOG.warn("[Commands] Rejecting addon command '{}': {}", id, reason);
      autismclient.b.DataUtil.init(addonId, "command", id, reason);
      return AddonRegistrationResult.rejected("command", id, reason);
   }

   public static synchronized void unregisterAddonCommands(String addonId) {
      if (addonId != null && !addonId.isBlank()) {
         List<String> removeNames = new ArrayList<>();

         for (Entry<String, String> owner : ADDON_BY_NAME.entrySet()) {
            if (addonId.equals(owner.getValue())) {
               removeNames.add(owner.getKey());
            }
         }

         for (String name : removeNames) {
            COMMAND_BY_NAME.remove(name);
            ADDON_BY_NAME.remove(name);
            REMOVED_ADDON_NAMES.add(name);
            auditRemoveRootLiteral(name);
         }

         COMMANDS.removeIf(command -> addonId.equals(ADDON_BY_COMMAND.get(command)));
         PRIMARY_NAME_BY_COMMAND.keySet().removeIf(command -> addonId.equals(ADDON_BY_COMMAND.get(command)));
         ADDON_BY_COMMAND.entrySet().removeIf(entry -> addonId.equals(entry.getValue()));
         if (!removeNames.isEmpty()) {
            registryVersion++;
         }
      }
   }

   @SuppressWarnings("unchecked")
   private static void auditRemoveRootLiteral(String lowerName) {
      if (lowerName == null || lowerName.isEmpty()) {
         return;
      }

      try {
         Object root = DISPATCHER.getRoot();
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

   public static CommandDispatcher<AutismCommandSource> getDispatcher() {
      return DISPATCHER;
   }

   public static int getRegistryVersion() {
      return registryVersion;
   }

   public static List<Command> getCommands() {
      return Collections.unmodifiableList(COMMANDS);
   }

   public static synchronized String getPrimaryName(Command command) {
      String name = PRIMARY_NAME_BY_COMMAND.get(command);
      return name == null ? command.name() : name;
   }

   public static synchronized List<String> getUsageForms(Command command) {
      String name = getPrimaryName(command);
      CommandNode<AutismCommandSource> node = DISPATCHER.getRoot().getChild(name);
      return node == null ? List.of(name) : collectUsageForms(node, name);
   }

   public static List<String> collectUsageForms(CommandNode<AutismCommandSource> node, String name) {
      List<String> forms = new ArrayList<>();

      for (String usage : new CommandDispatcher<AutismCommandSource>().getAllUsage(node, AutismCommandSource.INSTANCE, false)) {
         forms.add(usage.isEmpty() ? name : name + " " + usage);
      }

      if (forms.isEmpty()) {
         forms.add(name);
      }

      return forms;
   }

   public static Command getCommand(String nameOrAlias) {
      return nameOrAlias == null ? null : COMMAND_BY_NAME.get(nameOrAlias.trim().toLowerCase(Locale.ROOT));
   }

   public static String getPrefix() {
      return CommandPrefixHelper.currentPrefix();
   }

   public static boolean hasCommandPrefix(String message) {
      if (message != null && !message.isBlank()) {
         String trimmed = message.trim();
         String prefix = getPrefix();
         return !prefix.isEmpty() && trimmed.startsWith(prefix);
      } else {
         return false;
      }
   }

   public static boolean isHiddenMode() {
      return aD.read_Z();
   }

   public static boolean shouldSuppressInHiddenMode(String message) {
      return isHiddenMode() && looksLikeClientCommand(message);
   }

   private static boolean looksLikeClientCommand(String message) {
      if (message != null && !message.isBlank()) {
         String trimmed = message.trim();
         if ("^toggleautism".equalsIgnoreCase(trimmed)) {
            return true;
         }

         String prefix = getPrefix();
         if (!prefix.isEmpty() && trimmed.startsWith(prefix)) {
            return true;
         }

         for (String fallback : FALLBACK_PREFIXES) {
            if (fallback != null && !fallback.isEmpty() && !fallback.equals(prefix) && trimmed.startsWith(fallback)) {
               String body = trimmed.substring(fallback.length()).trim();
               if (!body.isEmpty()) {
                  String first = extractFirstToken(body).toLowerCase(Locale.ROOT);
                  if (COMMAND_BY_NAME.containsKey(first) || REMOVED_ADDON_NAMES.contains(first)) {
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

   public static String stripPrefix(String message) {
      if (!hasCommandPrefix(message)) {
         return "";
      }

      String trimmed = message.trim();
      int prefixLength = getPrefix().length();
      return trimmed.length() <= prefixLength ? "" : trimmed.substring(prefixLength).trim();
   }

   public static boolean isBypassingIntercept() {
      return BYPASS_INTERCEPT.get();
   }

   public static void sendChatRaw(ClientPacketListener conn, String message) {
      BYPASS_INTERCEPT.set(true);

      try {
         conn.sendChat(message);
      } finally {
         BYPASS_INTERCEPT.remove();
      }
   }

   public static boolean execute(String body) {
      return dispatch(body).isHandled();
   }

   public static CommandManager.Inner_DataUtil dispatch(String body) {
      if (isHiddenMode()) {
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

      if (REMOVED_ADDON_NAMES.contains(extractFirstToken(trimmed).toLowerCase(Locale.ROOT))) {
         reportSyntaxError(trimmed, CommandSyntaxException.BUILT_IN_EXCEPTIONS.dispatcherUnknownCommand().create());
         return new CommandManager.Inner_DataUtil(CommandManager.Inner_StreamUtil.UNKNOWN_COMMAND, "Unknown command: " + extractFirstToken(trimmed) + " (its add-on is disabled)");
      }

      try {
         DISPATCHER.execute(trimmed, AutismCommandSource.INSTANCE);
         return new CommandManager.Inner_DataUtil(CommandManager.Inner_StreamUtil.EXECUTED, "");
      } catch (CommandSyntaxException e) {
         reportSyntaxError(trimmed, e);
         String message = e.getMessage() != null && !e.getMessage().isBlank() ? e.getMessage() : "Incomplete or invalid command";
         boolean unknown = getCommand(extractFirstToken(trimmed)) == null;
         return new CommandManager.Inner_DataUtil(
            unknown ? CommandManager.Inner_StreamUtil.UNKNOWN_COMMAND : CommandManager.Inner_StreamUtil.SYNTAX_ERROR, unknown ? "Unknown command: " + extractFirstToken(trimmed) : message
         );
      } catch (Throwable t) {
         AutismClientAddon.LOG.warn("[Commands] dispatch failed for '{}'", trimmed, t);
         String message = t.getMessage() == null ? t.getClass().getSimpleName() : t.getMessage();
         AutismClientMessaging.sendPrefixed("§cCommand error: " + message);
         return new CommandManager.Inner_DataUtil(CommandManager.Inner_StreamUtil.ERROR, message);
      }
   }

   private static void reportSyntaxError(String body, CommandSyntaxException e) {
      String first = extractFirstToken(body);
      Command command = getCommand(first);
      String prefix = getPrefix();
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

   private static String extractFirstToken(String body) {
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

   public record Inner_DataUtil(CommandManager.Inner_StreamUtil status, String message) {
      public boolean isHandled() {
         return this.status != CommandManager.Inner_StreamUtil.EMPTY;
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

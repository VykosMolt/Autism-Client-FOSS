package autismclient.commands;

import autismclient.AutismClientAddon;
import autismclient.api.AddonRegistrationResult;
import autismclient.commands.b.ArrayUtil;
import autismclient.commands.b.BufferHelper;
import autismclient.commands.b.ByteReader;
import autismclient.commands.b.CacheEntry;
import autismclient.commands.b.ClassCache;
import autismclient.commands.b.ConfigStore;
import autismclient.commands.b.DataPipe;
import autismclient.commands.b.EventBus;
import autismclient.commands.b.FileCache;
import autismclient.commands.b.HashUtil;
import autismclient.commands.b.IoWorker;
import autismclient.commands.b.KeyHolder;
import autismclient.commands.b.ListPool;
import autismclient.commands.b.MapStore;
import autismclient.commands.b.MethodUtil;
import autismclient.commands.b.NetHelper;
import autismclient.commands.b.NodeWalker;
import autismclient.commands.b.PacketUtil;
import autismclient.commands.b.PoolEntry;
import autismclient.commands.b.RefQueue;
import autismclient.commands.b.RenderState;
import autismclient.commands.b.RingBuffer;
import autismclient.commands.b.StateCache;
import autismclient.commands.b.StreamGate;
import autismclient.commands.b.SyncManager;
import autismclient.commands.b.TableIndex;
import autismclient.commands.b.TaskQueue;
import autismclient.commands.b.TaskTimer;
import autismclient.commands.b.TextFormat;
import autismclient.commands.b.TokenStore;
import autismclient.commands.b.UiHelper;
import autismclient.commands.b.UtilCore;
import autismclient.commands.b.ValueSink;
import autismclient.commands.b.a;
import autismclient.commands.b.b;
import autismclient.commands.b.c;
import autismclient.modules.aD;
import autismclient.util.AutismClientMessaging;
import autismclient.util.AutismLiteVariant;
import autismclient.util.z;
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

public final class DataUtil {
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

   private DataUtil() {
   }

   public static synchronized void init_V() {
      if (!load_Z) {
         load_Z = true;
         init_V(new TokenStore());
         init_V(new DataPipe());
         init_V(new RenderState());
         init_V(new KeyHolder());
         init_V(new a());
         init_V(new TableIndex());
         init_V(new UtilCore());
         init_V(new SyncManager());
         init_V(new RefQueue());
         init_V(new ValueSink());
         init_V(new MapStore());
         init_V(new ArrayUtil());
         init_V(new StateCache());
         init_V(new RingBuffer());
         init_V(new autismclient.commands.b.CachePool());
         init_V(new autismclient.commands.b.StreamUtil());
         init_V(new ConfigStore());
         init_V(new FileCache());
         init_V(new EventBus());
         init_V(new NodeWalker());
         init_V(new TextFormat());
         init_V(new BufferHelper());
         init_V(new NetHelper());
         init_V(new TaskQueue());
         init_V(new PoolEntry());
         init_V(new UiHelper());
         if (!AutismLiteVariant.enabled()) {
            init_V(new CacheEntry());
            init_V(new autismclient.commands.b.DataUtil());
            init_V(new StreamGate());
            init_V(new MethodUtil());
            init_V(new ListPool());
            init_V(new IoWorker());
            init_V(new PacketUtil());
            init_V(new b());
            init_V(new HashUtil());
            init_V(new ByteReader());
            init_V(new c());
            init_V(new ClassCache());
            init_V(new TaskTimer());
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
      return z.init_Str();
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

   public static DataUtil.Inner_DataUtil init_DataUtil_DataUtil(String body) {
      if (init_Z()) {
         return new DataUtil.Inner_DataUtil(DataUtil.Inner_StreamUtil.BLOCKED, "Client commands are blocked while the client is hidden (panic mode)");
      }

      if (!AutismLiteVariant.enabled() && !autismclient.e.RingBuffer.init()) {
         AutismClientMessaging.sendPrefixed("§cAutism is locked: early access requires the donator role. Sign in to unlock.");
         return new DataUtil.Inner_DataUtil(DataUtil.Inner_StreamUtil.BLOCKED, "Autism is locked: early access requires the donator role");
      }

      if (body == null) {
         return new DataUtil.Inner_DataUtil(DataUtil.Inner_StreamUtil.EMPTY, "");
      }

      String trimmed = body.trim();
      if (trimmed.isEmpty()) {
         return new DataUtil.Inner_DataUtil(DataUtil.Inner_StreamUtil.EMPTY, "");
      }

      if (init_Set.contains(load_Str(trimmed).toLowerCase(Locale.ROOT))) {
         init(trimmed, CommandSyntaxException.BUILT_IN_EXCEPTIONS.dispatcherUnknownCommand().create());
         return new DataUtil.Inner_DataUtil(DataUtil.Inner_StreamUtil.UNKNOWN_COMMAND, "Unknown command: " + load_Str(trimmed) + " (its add-on is disabled)");
      }

      try {
         init_CommandDispatcher.execute(trimmed, AutismCommandSource.INSTANCE);
         return new DataUtil.Inner_DataUtil(DataUtil.Inner_StreamUtil.EXECUTED, "");
      } catch (CommandSyntaxException e) {
         init(trimmed, e);
         String message = e.getMessage() != null && !e.getMessage().isBlank() ? e.getMessage() : "Incomplete or invalid command";
         boolean unknown = init_Command(load_Str(trimmed)) == null;
         return new DataUtil.Inner_DataUtil(
            unknown ? DataUtil.Inner_StreamUtil.UNKNOWN_COMMAND : DataUtil.Inner_StreamUtil.SYNTAX_ERROR, unknown ? "Unknown command: " + load_Str(trimmed) : message
         );
      } catch (Throwable t) {
         AutismClientAddon.LOG.warn("[Commands] dispatch failed for '{}'", trimmed, t);
         String message = t.getMessage() == null ? t.getClass().getSimpleName() : t.getMessage();
         AutismClientMessaging.sendPrefixed("§cCommand error: " + message);
         return new DataUtil.Inner_DataUtil(DataUtil.Inner_StreamUtil.ERROR, message);
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

   public record Inner_DataUtil(DataUtil.Inner_StreamUtil init_DataUtil_StreamUti, String init_Str) {
      public boolean init_Z() {
         return this.init_DataUtil_StreamUti != DataUtil.Inner_StreamUtil.EMPTY;
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

package autismclient.ai;

import autismclient.AutismClientAddon;
import autismclient.api.ai.AiOperation;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BooleanSupplier;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import javax.tools.Diagnostic;
import javax.tools.DiagnosticCollector;
import javax.tools.JavaCompiler;
import javax.tools.JavaFileObject;
import javax.tools.StandardJavaFileManager;
import javax.tools.ToolProvider;
import net.fabricmc.loader.api.FabricLoader;

public final class AiAddons {
   static final Pattern init_Pattern = Pattern.compile("[a-z][a-z0-9_-]{1,31}");
   static final Set<String> init_Set = Set.of("module", "macro-action", "macro-condition", "command", "ai-capability");
   static final int init_I = 200000;
   static final int load_I = 64000;
   static final int read = 20;
   static final int write = 200;
   static final int update = 40;
   static final long init_J = TimeUnit.SECONDS.toNanos(120L);
   static final String init_Str = "no Java compiler found; point the game at a full JDK";
   private static final String[] init_StrArr = new String[]{
      "80,220,120", "120,200,255", "255,190,90", "230,140,255", "255,120,130", "120,230,220", "200,220,120", "180,170,255"
   };
   private static final AtomicReference<Path> init_AtomicReference = new AtomicReference<>();
   private static final Pattern load_Pattern = Pattern.compile("^(.+?\\.java):(\\d+):\\s+(error|warning|note):\\s+(.*)$");

   private AiAddons() {
   }

   static void init_V(Path root) {
      init_AtomicReference.set(root);
   }

   static Path init_Path() {
      Path override = init_AtomicReference.get();
      return override != null ? override : AutismClientAddon.FOLDER.toPath().resolve("addons");
   }

   public static AiOperation init_AiOperation(JsonObject args) {
      String action = AiJson.getString(args, "action", "").toLowerCase(Locale.ROOT);
      if (("build".equals(action) || "load".equals(action) || "install".equals(action)) && !auditAddonExecutionAllowed()) {
         return init_AiOperation(
            AiOperation.Status.FAILED,
            "Addon " + action + " is disabled while 'Unrestricted scripts' is off in AI settings",
            AiJson.objectOf("action", action)
         );
      }

      AiOperation var10000;
      switch (action) {
         case "scaffold":
            var10000 = load(args);
            break;
         case "write":
            var10000 = read(args);
            break;
         case "read":
            var10000 = write(args);
            break;
         case "build":
            var10000 = init(args, AiAddons::init_CachePool_StreamUt, AiAddons::init_List);
            break;
         case "load":
            var10000 = update(args);
            break;
         case "unload":
            var10000 = sync(args);
            break;
         case "list":
            var10000 = init_AiOperation();
            break;
         case "install":
            var10000 = flush(args);
            break;
         case "verify":
            var10000 = close(args);
            break;
         default:
            throw new IllegalArgumentException("Unknown addon action '" + action + "'; expected scaffold|write|read|build|load|unload|list|install|verify");
      }

      return var10000;
   }

   private static boolean auditAddonExecutionAllowed() {
      AiService controller = AiService.getExistingInstance();
      return controller == null || controller.getSettings().unrestrictedScripts;
   }

   private static boolean auditIsExistingAutismAddonJar(Path jarPath, String expectedId) {
      try (ZipFile zip = new ZipFile(jarPath.toFile())) {
         ZipEntry entry = zip.getEntry("fabric.mod.json");
         if (entry == null) {
            return false;
         }

         byte[] bytes = zip.getInputStream(entry).readNBytes(65536);
         JsonElement parsed = JsonParser.parseString(new String(bytes, StandardCharsets.UTF_8));
         if (!parsed.isJsonObject()) {
            return false;
         }

         JsonObject modJson = parsed.getAsJsonObject();
         String modId = AiJson.getString(modJson, "id", "");
         if (!expectedId.equals(modId)) {
            return false;
         }

         JsonElement entrypoints = modJson.get("entrypoints");
         return entrypoints != null && entrypoints.isJsonObject() && entrypoints.getAsJsonObject().has("autism-addon");
      } catch (Exception ignored) {
         return false;
      }
   }

   private static AiOperation.Result init_AiOperation_Result(AiOperation.Status status, String message, JsonObject data) {
      return new AiOperation.Result(status, message, data.toString(), List.of());
   }

   private static AiOperation init_AiOperation(final AiOperation.Status status, final String message, final JsonObject data) {
      return new AiOperation() {
         @Override
         public AiOperation.Result tick() {
            return AiAddons.init_AiOperation_Result(status, message, data);
         }

         @Override
         public void cancel() {
         }
      };
   }

   static String init_Str(String id) {
      if (id != null && init_Pattern.matcher(id).matches()) {
         return id;
      } else {
         throw new IllegalArgumentException("Invalid addon id '" + id + "'; must match " + init_Pattern.pattern());
      }
   }

   static Path init(Path addonDir, String relative) {
      if (relative != null && !relative.isBlank()) {
         Path parsed;
         try {
            parsed = Path.of(relative);
         } catch (InvalidPathException e) {
            throw new IllegalArgumentException("Invalid path: " + e.getMessage());
         }

         if (parsed.isAbsolute()) {
            throw new IllegalArgumentException("Path must be relative to the addon directory: " + relative);
         } else {
            Path base = addonDir.normalize();
            Path resolved = base.resolve(parsed).normalize();
            if (resolved.startsWith(base) && !resolved.equals(base)) {
               // the lexical check above can't see symlinks, so the deepest existing part must also resolve inside
               if (Files.exists(base)) {
                  Path existing = resolved;
                  while (existing != null && !Files.exists(existing, LinkOption.NOFOLLOW_LINKS)) {
                     existing = existing.getParent();
                  }

                  try {
                     if (existing == null || !existing.toRealPath().startsWith(base.toRealPath())) {
                        throw new IllegalArgumentException("Path escapes the addon directory through a link: " + relative);
                     }
                  } catch (IOException e) {
                     throw new IllegalArgumentException("Path escapes the addon directory through a link: " + relative);
                  }
               }

               return resolved;
            } else {
               throw new IllegalArgumentException("Path escapes the addon directory: " + relative);
            }
         }
      } else {
         throw new IllegalArgumentException("A path inside the addon directory is required");
      }
   }

   static String load_Str(String addonId) {
      return "aiaddon." + addonId.replace('-', '_');
   }

   static String init(String name, String addonId) {
      String base = write(name);
      if (base.isEmpty()) {
         base = write(addonId);
      }

      if (base.isEmpty() || !Character.isLetter(base.charAt(0))) {
         base = "Generated" + base;
      }

      return base.endsWith("Addon") ? base : base + "Addon";
   }

   private static String write(String value) {
      StringBuilder out = new StringBuilder();
      boolean upper = true;
      if (value != null) {
         for (char c : value.toCharArray()) {
            if (Character.isLetterOrDigit(c)) {
               out.append(upper ? Character.toUpperCase(c) : c);
               upper = false;
            } else {
               upper = true;
            }
         }
      }

      return out.toString();
   }

   static String read(String value) {
      StringBuilder out = new StringBuilder();

      for (char c : (value == null ? "" : value).toCharArray()) {
         switch (c) {
            case '\t':
               out.append("\\t");
               break;
            case '\n':
               out.append("\\n");
            case '\r':
               break;
            case '"':
               out.append("\\\"");
               break;
            case '\\':
               out.append("\\\\");
               break;
            default:
               if (c >= ' ') {
                  out.append(c);
               }
         }
      }

      return out.toString();
   }

   static JsonObject init(String id, String name, String description, String entryClass) {
      JsonObject json = new JsonObject();
      json.addProperty("schemaVersion", 1);
      json.addProperty("id", id);
      json.addProperty("version", "1.0.0");
      json.addProperty("name", name);
      json.addProperty("description", description != null && !description.isBlank() ? description : "AI-authored AUTISM addon.");
      JsonObject custom = new JsonObject();
      custom.addProperty("autism:color", init_StrArr[Math.floorMod(id.hashCode(), init_StrArr.length)]);
      json.add("custom", custom);
      json.addProperty("environment", "client");
      JsonObject entrypoints = new JsonObject();
      JsonArray autism = new JsonArray();
      autism.add(entryClass);
      entrypoints.add("autism", autism);
      json.add("entrypoints", entrypoints);
      JsonObject depends = new JsonObject();
      depends.addProperty("java", ">=25");
      depends.addProperty("autism", ">=5.0");
      json.add("depends", depends);
      return json;
   }

   static List<AiAddons.Inner_BufferHelper> init_List(String output) {
      List<AiAddons.Inner_BufferHelper> out = new ArrayList<>();
      if (output == null) {
         return out;
      }

      for (String line : output.split("\\R")) {
         Matcher matcher = load_Pattern.matcher(line.trim());
         if (matcher.matches() && out.size() < 20) {
            out.add(new AiAddons.Inner_BufferHelper(matcher.group(1), Long.parseLong(matcher.group(2)), matcher.group(3), matcher.group(4).trim()));
         }
      }

      return out;
   }

   static AiAddons.Inner_StreamUtil init_CachePool_StreamUt() {
      return init(
         ToolProvider::getSystemJavaCompiler,
         Path.of(System.getProperty("java.home", ".")),
         AiAddons::init_Path,
         () -> System.getenv("JAVA_HOME"),
         AiAddons::load
      );
   }

   static AiAddons.Inner_StreamUtil init(
      Supplier<JavaCompiler> system, Path javaHome, Function<String, Path> pathLookup, Supplier<String> javaHomeEnv, Supplier<List<Path>> prismRuntimeDirs
   ) {
      JavaCompiler compiler = system.get();
      if (compiler != null) {
         return new AiAddons.Inner_StreamUtil(compiler, null);
      }

      Path home = init_Path(javaHome == null ? null : javaHome.resolve("bin"));
      if (home != null) {
         return new AiAddons.Inner_StreamUtil(null, home);
      }

      Path onPath = pathLookup == null ? null : pathLookup.apply("javac");
      if (onPath != null) {
         return new AiAddons.Inner_StreamUtil(null, onPath);
      }

      String env = javaHomeEnv == null ? null : javaHomeEnv.get();
      if (env != null && !env.isBlank()) {
         Path fromEnv = init_Path(Path.of(env).resolve("bin"));
         if (fromEnv != null) {
            return new AiAddons.Inner_StreamUtil(null, fromEnv);
         }
      }

      if (prismRuntimeDirs != null) {
         for (Path dir : prismRuntimeDirs.get()) {
            Path found = init_Path(dir.resolve("bin"));
            if (found != null) {
               return new AiAddons.Inner_StreamUtil(null, found);
            }
         }
      }

      return null;
   }

   private static Path init_Path(Path binDir) {
      if (binDir == null) {
         return null;
      }

      Path exe = binDir.resolve("javac.exe");
      if (Files.isRegularFile(exe)) {
         return exe;
      }

      exe = binDir.resolve("javac");
      return Files.isRegularFile(exe) ? exe : null;
   }

   private static Path init_Path(String executable) {
      String path = System.getenv("PATH");
      if (path == null) {
         return null;
      }

      for (String dir : path.split(Pattern.quote(File.pathSeparator))) {
         if (!dir.isBlank()) {
            Path found = init_Path(Path.of(dir));
            if (found != null) {
               return found;
            }
         }
      }

      return null;
   }

   private static List<Path> load() {
      List<Path> roots = new ArrayList<>();
      String appData = System.getenv("APPDATA");
      if (appData != null && !appData.isBlank()) {
         roots.add(Path.of(appData).resolve("PrismLauncher").resolve("java"));
      }

      String home = System.getProperty("user.home", "");
      if (!home.isBlank()) {
         roots.add(Path.of(home).resolve(".local").resolve("share").resolve("PrismLauncher").resolve("java"));
         roots.add(Path.of(home).resolve("Library").resolve("Application Support").resolve("PrismLauncher").resolve("java"));
      }

      List<Path> out = new ArrayList<>();

      for (Path root : roots) {
         if (Files.isDirectory(root)) {
            try (Stream<Path> children = Files.list(root)) {
               List<Path> sorted = children.filter(x$0 -> Files.isDirectory(x$0))
                  .sorted(Comparator.<Path, String>comparing(p -> p.getFileName().toString()).reversed())
                  .limit(16L)
                  .toList();
               out.addAll(sorted);
            } catch (IOException var11) {
            }
         }
      }

      return out;
   }

   static List<Path> init_List() {
      LinkedHashSet<Path> out = new LinkedHashSet<>();
      FabricLoader.getInstance().getModContainer("autism").flatMap(container -> container.getOrigin().getPaths().stream().findFirst()).ifPresent(out::add);

      for (String entry : System.getProperty("java.class.path", "").split(Pattern.quote(File.pathSeparator))) {
         if (!entry.isBlank()) {
            out.add(Path.of(entry));
         }
      }

      return List.copyOf(out);
   }

   private static AiAddons.Inner_ConfigStore init_CachePool_ConfigSt(Path addonDir) {
      Path file = addonDir.resolve("addon.json");
      if (!Files.isRegularFile(file)) {
         return null;
      }

      try {
         if (Files.size(file) > 1_000_000L) {
            return null;
         }

         return AiAddons.Inner_ConfigStore.fromJson(JsonParser.parseString(Files.readString(file, StandardCharsets.UTF_8)).getAsJsonObject());
      } catch (Exception e) {
         return null;
      }
   }

   private static void init(Path addonDir, AiAddons.Inner_ConfigStore meta) throws IOException {
      AiJson.writeAtomicJson(addonDir.resolve("addon.json"), meta.toJson());
   }

   private static Path load_Path(String id) {
      return init_Path().resolve(id).normalize();
   }

   private static AiAddons.Inner_ConfigStore init(String id, Path dir) {
      AiAddons.Inner_ConfigStore meta = init_CachePool_ConfigSt(dir);
      if (meta != null) {
         return meta;
      }

      String className = init(id, id);
      return new AiAddons.Inner_ConfigStore(id, id, "", load_Str(id), load_Str(id) + "." + className, List.of(), List.of());
   }

   private static AiOperation load(JsonObject args) {
      String id = init_Str(AiJson.getString(args, "id", ""));
      String name = AiJson.getString(args, "name", "").trim();
      if (name.isEmpty()) {
         name = id;
      }

      if (name.length() > 64) {
         throw new IllegalArgumentException("Addon name must be at most 64 characters");
      }

      String description = AiJson.getString(args, "description", "AI-authored AUTISM addon " + name + ".").trim();
      if (description.length() > 256) {
         throw new IllegalArgumentException("Addon description must be at most 256 characters");
      }

      List<String> features = init_List(args);
      Path dir = load_Path(id);
      boolean force = AiJson.getBoolean(args, "force", false);

      try {
         if (Files.isDirectory(dir) && init_Z(dir)) {
            if (!force) {
               return init_AiOperation(
                  AiOperation.Status.FAILED,
                  "Addon '" + id + "' already exists and is not empty; pass force:true to overwrite",
                  AiJson.objectOf("id", id, "dir", dir.toString())
               );
            }

            load(dir);
         }

         String pkg = load_Str(id);
         String className = init(name, id);
         String entryClass = pkg + "." + className;
         Path source = dir.resolve("src").resolve(pkg.replace('.', '/')).resolve(className + ".java");
         Files.createDirectories(source.getParent());
         Files.writeString(source, init(id, name, description, features), StandardCharsets.UTF_8);
         init(dir, new AiAddons.Inner_ConfigStore(id, name, description, pkg, entryClass, features, List.of()));
         return init_AiOperation(
            AiOperation.Status.SUCCEEDED,
            "Scaffolded addon '" + id + "' (" + String.join(", ", features) + ")",
            AiJson.objectOf(
               "id", id, "dir", dir.toString(), "package", pkg, "entryClass", entryClass, "source", dir.relativize(source).toString(), "features", features
            )
         );
      } catch (IOException e) {
         return init_AiOperation(AiOperation.Status.FAILED, "Scaffold failed: " + e.getMessage(), AiJson.objectOf("id", id));
      }
   }

   private static List<String> init_List(JsonObject args) {
      List<String> out = new ArrayList<>();
      if (args.has("features") && args.get("features").isJsonArray()) {
         for (JsonElement element : args.getAsJsonArray("features")) {
            String kind = element.getAsString().toLowerCase(Locale.ROOT).trim();
            if (!init_Set.contains(kind)) {
               throw new IllegalArgumentException("Unknown feature kind '" + kind + "'; expected one of " + init_Set);
            }

            if (!out.contains(kind)) {
               out.add(kind);
            }
         }
      }

      if (out.isEmpty()) {
         out.add("module");
      }

      return out;
   }

   private static boolean init_Z(Path dir) throws IOException {
      try (Stream<Path> entries = Files.list(dir)) {
         return entries.findAny().isPresent();
      }
   }

   private static void load(Path dir) throws IOException {
      if (Files.exists(dir)) {
         try (Stream<Path> walk = Files.walk(dir)) {
            for (Path p : walk.sorted(Comparator.reverseOrder()).toList()) {
               Files.delete(p);
            }
         }
      }
   }

   static String init(String id, String name, String description, List<String> features) {
      String pkg = load_Str(id);
      String className = init(name, id);
      String n = read(name);
      String d = read(description);
      StringBuilder src = new StringBuilder();
      src.append("package ").append(pkg).append(";\n\n");
      src.append("import autismclient.api.ApiVersion;\n");
      src.append("import autismclient.api.SimpleAddon;\n");
      src.append("import autismclient.api.module.SimpleModule;\n");
      src.append("import autismclient.util.AutismClientMessaging;\n\n");
      src.append("/**\n * ").append(n).append(" — ").append(d).append("\n");
      src.append(" * Runtime-loaded AUTISM addon. Rules for this load path: no mixins and no\n");
      src.append(" * net.minecraft.* references (the production runtime is intermediary-mapped, so\n");
      src.append(" * named Minecraft classes do not exist here); register everything in initialize().\n */\n");
      src.append("public final class ").append(className).append(" extends SimpleAddon {\n");
      src.append("    public ").append(className).append("() {\n");
      src.append("        super(ApiVersion.CURRENT, \"").append(pkg).append("\");\n    }\n\n");
      src.append("    @Override\n    protected void initialize() {\n");
      if (features.contains("module")) {
         src.append(
            "        SimpleModule module = new SimpleModule(\"hello-module\", \"%s\", \"%s\")\n            .addBool(\"greet\", \"Greet On Enable\", true)\n            .addText(\"message\", \"Message\", \"Hello from %s\")\n            .onEnabled(self -> {\n                if (self.getBool(\"greet\")) AutismClientMessaging.sendPrefixed(\"\\u00a7a\" + self.getText(\"message\"));\n            });\n        registerModule(module);\n"
               .formatted(n, d, n)
         );
      }

      if (features.contains("macro-action")) {
         src.append(
            "registerSimpleAction(\"say-hello\", \"Say Hello\", \"Send a short addon message.\", \"S\",\n    action(ignored -> AutismClientMessaging.sendPrefixed(\"\\u00a7aHello from %s!\")));\n"
               .formatted(n)
         );
      }

      if (features.contains("macro-condition")) {
         src.append(
            "registerSimpleCondition(\"always-ready\", \"Always Ready\", \"Passes immediately; replace with real logic.\", \"Waiting\", \"P\",\n    condition(ignored -> true));\n"
         );
      }

      if (features.contains("command")) {
         src.append(
            "autismclient.api.AutismAddons.commands().register(new autismclient.commands.Command(\"%s-hello\", \"%s\") {\n    @Override public void build(com.mojang.brigadier.builder.LiteralArgumentBuilder<autismclient.commands.AutismCommandSource> root) {\n        root.executes(ctx -> {\n            AutismClientMessaging.sendPrefixed(\"\\u00a7aHello from %s!\");\n            return SUCCESS;\n        });\n    }\n});\n"
               .formatted(id, d, n)
         );
      }

      if (features.contains("ai-capability")) {
         src.append(
            "autismclient.api.AutismAddons.ai().register(new autismclient.api.ai.AiCapabilityProvider() {\n    @Override public java.util.List<autismclient.api.ai.AiCapabilityDescriptor> capabilities() {\n        return java.util.List.of(new autismclient.api.ai.AiCapabilityDescriptor(\n            \"%s:echo\", \"%s\", 1, \"addon\", \"Echoes the text argument back.\", java.util.List.of(\"echo\"),\n            \"{\\\"type\\\":\\\"object\\\",\\\"properties\\\":{\\\"text\\\":{\\\"type\\\":\\\"string\\\"}},\\\"required\\\":[\\\"text\\\"]}\",\n            \"None.\", \"Returns the text unchanged in data.echo.\", java.util.List.of(),\n            \"data.echo equals the input text.\", \"Nothing to recover; the call is read-only.\",\n            \"{\\\"id\\\":\\\"%s:echo\\\",\\\"arguments\\\":{\\\"text\\\":\\\"hello\\\"}}\",\n            autismclient.api.ai.AiCapabilityDescriptor.Classification.OBSERVE));\n    }\n    @Override public autismclient.api.ai.AiOperation start(String capabilityId, String argumentsJson, autismclient.api.ai.AiObservation observation) {\n        String parsed = \"\";\n        try { parsed = com.google.gson.JsonParser.parseString(argumentsJson).getAsJsonObject().get(\"text\").getAsString(); }\n        catch (Exception ignored) {}\n        final String echo = parsed;\n        return new autismclient.api.ai.AiOperation() {\n            @Override public Result tick() {\n                com.google.gson.JsonObject data = new com.google.gson.JsonObject();\n                data.addProperty(\"echo\", echo);\n                return new Result(Status.SUCCEEDED, \"Echoed text\", data.toString(), java.util.List.of());\n            }\n            @Override public void cancel() {}\n        };\n    }\n});\n"
               .formatted(id, id, id)
         );
      }

      src.append("    }\n");
      if (features.contains("macro-action") || features.contains("macro-condition")) {
         src.append(
            "// Raw functional-interface targets keep net.minecraft out of this class's bytecode: the inferred\n// Consumer<Minecraft>/Predicate<Minecraft> lambda form would link against the named Minecraft\n// class, which does not exist in the intermediary-mapped production runtime. Keep these casts.\n@SuppressWarnings({\"rawtypes\", \"unchecked\"})\nprivate static java.util.function.Consumer action(java.util.function.Consumer<Object> body) {\n    return (java.util.function.Consumer) body;\n}\n\n@SuppressWarnings({\"rawtypes\", \"unchecked\"})\nprivate static java.util.function.Predicate condition(java.util.function.Predicate<Object> body) {\n    return (java.util.function.Predicate) body;\n}\n"
         );
      }

      src.append("}\n");
      return src.toString();
   }

   private static AiOperation read(JsonObject args) {
      String id = init_Str(AiJson.getString(args, "id", ""));
      Path dir = load_Path(id);
      Path target = init(dir, AiJson.getString(args, "path", ""));
      if (!target.getFileName().toString().endsWith(".java")) {
         throw new IllegalArgumentException("Only .java source files can be written: " + target.getFileName());
      }

      if (args.has("content") && !args.get("content").isJsonNull()) {
         String content = args.get("content").getAsString();
         if (content.length() > 200000) {
            throw new IllegalArgumentException("content exceeds 200000 characters");
         }

         try {
            Files.createDirectories(target.getParent());
            Files.writeString(target, content, StandardCharsets.UTF_8);
            return init_AiOperation(
               AiOperation.Status.SUCCEEDED,
               "Wrote " + dir.relativize(target) + " (" + content.length() + " chars)",
               AiJson.objectOf("id", id, "path", dir.relativize(target).toString(), "chars", content.length())
            );
         } catch (IOException e) {
            return init_AiOperation(AiOperation.Status.FAILED, "Write failed: " + e.getMessage(), AiJson.objectOf("id", id));
         }
      } else {
         throw new IllegalArgumentException("content is required");
      }
   }

   private static AiOperation write(JsonObject args) {
      String id = init_Str(AiJson.getString(args, "id", ""));
      Path dir = load_Path(id);
      if (!Files.isDirectory(dir)) {
         return init_AiOperation(AiOperation.Status.FAILED, "No scaffolded addon '" + id + "'", AiJson.objectOf("id", id));
      }

      String relative = AiJson.getString(args, "path", "");
      if (!relative.isBlank()) {
         Path target = init(dir, relative);
         if (!Files.isRegularFile(target)) {
            return init_AiOperation(AiOperation.Status.FAILED, "No such file in addon '" + id + "': " + relative, AiJson.objectOf("id", id));
         }

         try {
            long fileSize = Files.size(target);
            StringBuilder sb = new StringBuilder();
            boolean truncated = false;
            try (java.io.BufferedReader reader = Files.newBufferedReader(target, StandardCharsets.UTF_8)) {
               char[] buf = new char[4096];
               int n;
               while ((n = reader.read(buf)) != -1) {
                  int remaining = 64000 - sb.length();
                  if (n > remaining) {
                     sb.append(buf, 0, remaining);
                     truncated = true;
                     break;
                  }

                  sb.append(buf, 0, n);
               }
            }

            String content = sb.toString();
            return init_AiOperation(
               AiOperation.Status.SUCCEEDED,
               "Read " + dir.relativize(target),
               AiJson.objectOf(
                  "id", id, "path", dir.relativize(target).toString(), "size", fileSize, "content", content, "truncated", truncated
               )
            );
         } catch (IOException e) {
            return init_AiOperation(AiOperation.Status.FAILED, "Read failed: " + e.getMessage(), AiJson.objectOf("id", id));
         }
      } else {
         JsonArray files = new JsonArray();
         int count = 0;

         try (Stream<Path> walk = Files.walk(dir)) {
            for (Path p : walk.filter(x$0 -> Files.isRegularFile(x$0)).sorted().toList()) {
               if (++count > 200) {
                  break;
               }

               JsonObject entry = new JsonObject();
               entry.addProperty("path", dir.relativize(p).toString());
               entry.addProperty("size", Files.size(p));
               files.add(entry);
            }

            return init_AiOperation(
               AiOperation.Status.SUCCEEDED,
               "Listed " + Math.min(count, 200) + " files in addon '" + id + "'",
               AiJson.objectOf("id", id, "files", files, "truncated", count > 200)
            );
         } catch (IOException e) {
            return init_AiOperation(AiOperation.Status.FAILED, "List failed: " + e.getMessage(), AiJson.objectOf("id", id));
         }
      }
   }

   static AiOperation init(JsonObject args, final AiAddons.Inner_CachePool locator, final Supplier<List<Path>> classpath) {
      final String id = init_Str(AiJson.getString(args, "id", ""));
      final Path dir = load_Path(id);

      final List<Path> sources;
      try {
         sources = init_List(dir);
      } catch (IOException e) {
         return init_AiOperation(AiOperation.Status.FAILED, "Source scan failed: " + e.getMessage(), AiJson.objectOf("id", id));
      }

      if (sources.isEmpty()) {
         return init_AiOperation(
            AiOperation.Status.FAILED, "Nothing to build: no .java sources under " + dir + " (scaffold or write first)", AiJson.objectOf("id", id)
         );
      }

      final AiAddons.Inner_ConfigStore meta = init(id, dir);
      return new AiOperation() {
         final long init_J;
         final AtomicReference<Process> init_AtomicReference;
         final AtomicBoolean init_AtomicBoolean;
         volatile AiAddons.Inner_DataUtil init_CachePool_DataUtil;
         Thread init_Thread;

         {
            this.init_J = System.nanoTime() + AiAddons.init_J;
            this.init_AtomicReference = new AtomicReference<>();
            this.init_AtomicBoolean = new AtomicBoolean();
         }

         @Override
         public AiOperation.Result tick() {
            if (this.init_AtomicBoolean.get()) {
               return AiAddons.init_AiOperation_Result(AiOperation.Status.CANCELLED, "Addon build cancelled", AiJson.objectOf("id", id));
            }

            if (this.init_Thread == null) {
               this.init_Thread = new Thread(
                  () -> this.init_CachePool_DataUtil = AiAddons.init(
                     dir, meta, sources, locator.find(), classpath.get(), this.init_AtomicReference, this.init_AtomicBoolean::get
                  ),
                  "ai-addon-build-" + id
               );
               this.init_Thread.setDaemon(true);
               this.init_Thread.start();
            }

            AiAddons.Inner_DataUtil done = this.init_CachePool_DataUtil;
            if (done == null) {
               if (System.nanoTime() > this.init_J) {
                  this.cancel();
                  return AiAddons.init_AiOperation_Result(AiOperation.Status.TIMED_OUT, "Addon build exceeded 120s", AiJson.objectOf("id", id));
               } else {
                  return AiAddons.init_AiOperation_Result(
                     AiOperation.Status.RUNNING,
                     "Compiling addon '" + id + "' (" + sources.size() + " sources)",
                     AiJson.objectOf("id", id, "sources", sources.size())
                  );
               }
            } else {
               JsonObject data = AiJson.objectOf(
                  "id", id, "compiler", done.compiler(), "classes", done.classCount(), "jar", done.jar(), "warnings", done.warningCount()
               );
               if (!done.errors().isEmpty()) {
                  JsonArray errors = new JsonArray();

                  for (AiAddons.Inner_BufferHelper diag : done.errors()) {
                     errors.add(diag.toJson());
                  }

                  data.add("errors", errors);
               }

               return done.cancelled()
                  ? AiAddons.init_AiOperation_Result(AiOperation.Status.CANCELLED, "Addon build cancelled", data)
                  : AiAddons.init_AiOperation_Result(done.success() ? AiOperation.Status.SUCCEEDED : AiOperation.Status.FAILED, done.message(), data);
            }
         }

         @Override
         public void cancel() {
            this.init_AtomicBoolean.set(true);
            Process process = this.init_AtomicReference.get();
            if (process != null && process.isAlive()) {
               long self = ProcessHandle.current().pid();

               try {
                  process.descendants().filter(h -> h.pid() != self).forEach(h -> {
                     try {
                        h.destroyForcibly();
                     } catch (RuntimeException var2x) {
                     }
                  });
               } catch (RuntimeException var6) {
               }
            }

            if (process != null) {
               try {
                  process.destroyForcibly();
               } catch (RuntimeException var5) {
               }
            }

            Thread w = this.init_Thread;
            if (w != null) {
               w.interrupt();
            }
         }
      };
   }

   private static List<Path> init_List(Path dir) throws IOException {
      if (!Files.isDirectory(dir)) {
         return List.of();
      }

      try (Stream<Path> walk = Files.walk(dir)) {
         return walk.filter(p -> Files.isRegularFile(p) && p.getFileName().toString().endsWith(".java"))
            .filter(p -> !p.normalize().startsWith(dir.resolve("classes")))
            .sorted()
            .toList();
      }
   }

   static AiAddons.Inner_DataUtil init(
      Path dir,
      AiAddons.Inner_ConfigStore meta,
      List<Path> sources,
      AiAddons.Inner_StreamUtil compiler,
      List<Path> classpath,
      AtomicReference<Process> running,
      BooleanSupplier cancelled
   ) {
      if (compiler == null) {
         return new AiAddons.Inner_DataUtil(false, "no Java compiler found; point the game at a full JDK", List.of(), 0, 0, "", "none", false);
      }

      Path classesDir = dir.resolve("classes");

      try {
         load(classesDir);
         Files.createDirectories(classesDir);
      } catch (IOException e) {
         return new AiAddons.Inner_DataUtil(false, "Could not prepare classes dir: " + e.getMessage(), List.of(), 0, 0, "", compiler.describe(), false);
      }

      List<AiAddons.Inner_BufferHelper> diags;
      boolean ok;
      try {
         if (compiler.inProcess()) {
            diags = new ArrayList<>();
            ok = init(compiler.system(), sources, classesDir, classpath, diags);
         } else {
            diags = init(compiler.executable(), sources, classesDir, classpath, running, cancelled);
            ok = diags.stream().noneMatch(d -> d.kind().equals("error"));
         }
      } catch (InterruptedException e) {
         Thread.currentThread().interrupt();
         return new AiAddons.Inner_DataUtil(false, "Addon build cancelled", List.of(), 0, 0, "", compiler.describe(), true);
      } catch (Exception e) {
         return new AiAddons.Inner_DataUtil(false, "Compiler failed to run: " + e.getMessage(), List.of(), 0, 0, "", compiler.describe(), false);
      }

      if (cancelled.getAsBoolean()) {
         return new AiAddons.Inner_DataUtil(false, "Addon build cancelled", List.of(), 0, 0, "", compiler.describe(), true);
      }

      List<AiAddons.Inner_BufferHelper> errors = diags.stream().filter(d -> d.kind().equals("error")).limit(20L).toList();
      int warnings = (int)diags.stream().filter(d -> d.kind().equals("warning")).count();
      if (!ok) {
         return new AiAddons.Inner_DataUtil(
            false,
            "Compilation failed with " + errors.size() + " error(s); fix with action=write and rebuild",
            errors,
            warnings,
            0,
            "",
            compiler.describe(),
            false
         );
      }

      try {
         int classCount = init_I(classesDir);
         Path jar = init(dir, meta, classesDir);
         return new AiAddons.Inner_DataUtil(
            true, "Built " + classCount + " classes into " + jar.getFileName(), List.of(), warnings, classCount, jar.toString(), compiler.describe(), false
         );
      } catch (IOException e) {
         return new AiAddons.Inner_DataUtil(
            false, "Compiled but jar packaging failed: " + e.getMessage(), List.of(), warnings, 0, "", compiler.describe(), false
         );
      }
   }

   private static int init_I(Path classesDir) throws IOException {
      try (Stream<Path> walk = Files.walk(classesDir)) {
         return (int)walk.filter(p -> Files.isRegularFile(p) && p.getFileName().toString().endsWith(".class")).count();
      }
   }

   private static boolean init(JavaCompiler compiler, List<Path> sources, Path classesDir, List<Path> classpath, List<AiAddons.Inner_BufferHelper> diags) throws IOException {
      DiagnosticCollector<JavaFileObject> collector = new DiagnosticCollector<>();
      StringWriter log = new StringWriter();

      boolean ok;
      try (StandardJavaFileManager files = compiler.getStandardFileManager(collector, Locale.ROOT, StandardCharsets.UTF_8)) {
         List<String> options = new ArrayList<>(
            List.of("-classpath", init(classpath), "-d", classesDir.toString(), "-proc:none", "-nowarn", "-encoding", "UTF-8")
         );
         ok = compiler.getTask(log, files, collector, options, null, files.getJavaFileObjectsFromPaths(sources)).call();
      }

      for (Diagnostic<? extends JavaFileObject> diagnostic : collector.getDiagnostics()) {
         if (diags.size() >= 80) {
            break;
         }

         String var10000;
         switch (diagnostic.getKind()) {
            case ERROR:
               var10000 = "error";
               break;
            case WARNING:
            case MANDATORY_WARNING:
               var10000 = "warning";
               break;
            default:
               var10000 = "note";
         }

         String kind = var10000;
         diags.add(
            new AiAddons.Inner_BufferHelper(
               diagnostic.getSource() == null ? "" : diagnostic.getSource().getName(), diagnostic.getLineNumber(), kind, diagnostic.getMessage(Locale.ROOT)
            )
         );
      }

      return ok;
   }

   private static List<AiAddons.Inner_BufferHelper> init(
      Path executable, List<Path> sources, Path classesDir, List<Path> classpath, AtomicReference<Process> running, BooleanSupplier cancelled
   ) throws IOException, InterruptedException {
      Path argsFile = Files.createTempFile("ai-addon-javac", ".args");

      try {
         StringBuilder args = new StringBuilder();
         args.append("-classpath\n\"").append(update(init(classpath))).append("\"\n");
         args.append("-d\n\"").append(update(classesDir.toString())).append("\"\n");
         args.append("-proc:none\n-nowarn\n-encoding\nUTF-8\n");

         for (Path source : sources) {
            args.append('"').append(update(source.toString())).append("\"\n");
         }

         Files.writeString(argsFile, args.toString(), StandardCharsets.UTF_8);
         Process process = new ProcessBuilder(executable.toString(), "@" + argsFile).redirectErrorStream(true).start();
         running.set(process);
         StringBuilder output = new StringBuilder();
         Thread gobbler = new Thread(() -> {
            try (java.io.InputStream in = process.getInputStream()) {
               byte[] buf = new byte[4096];
               int kept = 0;
               int n;
               while ((n = in.read(buf)) >= 0) {
                  if (kept < 64000) {
                     int take = Math.min(n, 64000 - kept);
                     output.append(new String(buf, 0, take, StandardCharsets.UTF_8));
                     kept += take;
                  }
               }
            } catch (IOException var3x) {
            }
         }, "ai-addon-javac-out");
         gobbler.setDaemon(true);
         gobbler.start();

         int exitCode;
         try {
            while (true) {
               if (cancelled.getAsBoolean()) {
                  process.destroyForcibly();
                  throw new InterruptedException("cancelled");
               }

               try {
                  if (process.waitFor(250L, TimeUnit.MILLISECONDS)) {
                     break;
                  }
               } catch (InterruptedException e) {
                  process.destroyForcibly();
                  throw e;
               }
            }

            gobbler.join(2000L);
            exitCode = process.exitValue();
         } finally {
            running.compareAndSet(process, null);
         }

         List<AiAddons.Inner_BufferHelper> parsed = new ArrayList<>(init_List(output.toString()));
         if (exitCode != 0 && parsed.stream().noneMatch(d -> d.kind().equals("error"))) {
            String summary = output.toString().trim();
            if (summary.isEmpty()) {
               summary = "javac exited with status " + exitCode;
            } else if (summary.length() > 400) {
               summary = summary.substring(0, 400);
            }

            parsed.add(new AiAddons.Inner_BufferHelper("", 0L, "error", summary));
         }

         return parsed;
      } finally {
         Files.deleteIfExists(argsFile);
      }
   }

   private static String init(List<Path> classpath) {
      StringBuilder out = new StringBuilder();

      for (Path entry : classpath) {
         if (out.length() > 0) {
            out.append(File.pathSeparatorChar);
         }

         out.append(entry);
      }

      return out.toString();
   }

   private static String update(String path) {
      return path.replace('\\', '/');
   }

   private static Path init(Path dir, AiAddons.Inner_ConfigStore meta, Path classesDir) throws IOException {
      Path jar = dir.resolve(meta.id() + ".jar");
      String manifest = AiJson.GSON.toJson(init(meta.id(), meta.name(), meta.description(), meta.entryClass()));

      try (JarOutputStream out = new JarOutputStream(Files.newOutputStream(jar))) {
         out.putNextEntry(new JarEntry("fabric.mod.json"));
         out.write(manifest.getBytes(StandardCharsets.UTF_8));
         out.closeEntry();

         List<Path> classes;
         try (Stream<Path> walk = Files.walk(classesDir)) {
            classes = walk.filter(p -> Files.isRegularFile(p) && p.getFileName().toString().endsWith(".class")).sorted().toList();
         }

         for (Path clazz : classes) {
            out.putNextEntry(new JarEntry(update(classesDir.relativize(clazz).toString())));
            out.write(Files.readAllBytes(clazz));
            out.closeEntry();
         }
      }

      return jar;
   }

   private static AiOperation update(JsonObject args) {
      String id = init_Str(AiJson.getString(args, "id", ""));
      Path dir = load_Path(id);
      Path classesDir = dir.resolve("classes");

      int classCount;
      try {
         classCount = init_I(classesDir);
      } catch (IOException e) {
         classCount = 0;
      }

      if (classCount == 0) {
         return init_AiOperation(AiOperation.Status.FAILED, "No compiled classes for '" + id + "'; run action=build first", AiJson.objectOf("id", id));
      }

      AiAddons.Inner_ConfigStore meta = init(id, dir);
      String entryClass = AiJson.getString(args, "entryClass", meta.entryClass());
      if (entryClass.isBlank()) {
         return init_AiOperation(
            AiOperation.Status.FAILED, "No entry class known for '" + id + "'; pass entryClass or scaffold first", AiJson.objectOf("id", id)
         );
      }

      autismclient.b.DataUtil.TaskQueue result = autismclient.b.DataUtil.init(id, meta.name().isBlank() ? id : meta.name(), classesDir, entryClass);
      List<String> registered = result.init_List() == null ? List.of() : result.init_List().stream().limit(40L).toList();

      try {
         init(dir, meta.withRegistered(registered));
      } catch (IOException var11) {
      }

      JsonObject data = AiJson.objectOf(
         "id", id, "status", result.load(), "detail", result.read(), "registered", registered, "entryClass", entryClass
      );
      boolean loaded = "LOADED".equals(result.load());
      return init_AiOperation(
         loaded ? AiOperation.Status.SUCCEEDED : AiOperation.Status.FAILED,
         loaded
            ? "Runtime addon '" + id + "' loaded with " + registered.size() + " registration(s)"
            : "Runtime load of '" + id + "' " + result.load() + ": " + result.read(),
         data
      );
   }

   private static AiOperation sync(JsonObject args) {
      String id = init_Str(AiJson.getString(args, "id", ""));
      boolean unloaded = autismclient.b.DataUtil.init_Z(id);
      return init_AiOperation(
         unloaded ? AiOperation.Status.SUCCEEDED : AiOperation.Status.FAILED,
         unloaded ? "Runtime addon '" + id + "' unloaded" : "Runtime addon '" + id + "' was not loaded",
         AiJson.objectOf("id", id, "unloaded", unloaded)
      );
   }

   private static AiOperation flush(JsonObject args) {
      String id = init_Str(AiJson.getString(args, "id", ""));
      Path dir = load_Path(id);
      Path jar = dir.resolve(id + ".jar");
      if (!Files.isRegularFile(jar)) {
         return init_AiOperation(AiOperation.Status.FAILED, "No built jar for '" + id + "'; run action=build first", AiJson.objectOf("id", id));
      }

      Path mods = FabricLoader.getInstance().getGameDir().resolve("mods");
      Path target = mods.resolve(id + ".jar");
      boolean force = AiJson.getBoolean(args, "force", false);

      try {
         if (Files.exists(target)) {
            if (!auditIsExistingAutismAddonJar(target, id)) {
               return init_AiOperation(
                  AiOperation.Status.FAILED,
                  "Refusing to overwrite non-addon mod jar mods/" + id + ".jar",
                  AiJson.objectOf("id", id, "target", target.toString())
               );
            }

            if (!force) {
               return init_AiOperation(
                  AiOperation.Status.FAILED,
                  "mods/" + id + ".jar already exists; pass force:true to overwrite",
                  AiJson.objectOf("id", id, "target", target.toString())
               );
            }
         }

         Files.createDirectories(mods);
         Files.copy(jar, target, StandardCopyOption.REPLACE_EXISTING);
         boolean runtimeLoaded = init_Z(id);
         return init_AiOperation(
            AiOperation.Status.SUCCEEDED,
            "Installed " + id + ".jar into mods/ for discovery on next restart",
            AiJson.objectOf(
               "id",
               id,
               "jar",
               target.toString(),
               "runtimeLoaded",
               runtimeLoaded,
               "note",
               runtimeLoaded
                  ? "The runtime-loaded instance stays active until restart or action=unload; the restart replaces it with the jar"
                  : "The jar activates on next restart"
            )
         );
      } catch (IOException e) {
         return init_AiOperation(AiOperation.Status.FAILED, "Install failed: " + e.getMessage(), AiJson.objectOf("id", id));
      }
   }

   private static AiOperation init_AiOperation() {
      Path root = init_Path();
      JsonArray addons = new JsonArray();
      if (Files.isDirectory(root)) {
         try (Stream<Path> entries = Files.list(root)) {
            for (Path dir : entries.filter(x$0 -> Files.isDirectory(x$0)).sorted().toList()) {
               String id = dir.getFileName().toString();
               if (init_Pattern.matcher(id).matches()) {
                  JsonObject entry = new JsonObject();
                  entry.addProperty("id", id);

                  int classes;
                  try {
                     classes = init_I(dir.resolve("classes"));
                  } catch (IOException e) {
                     classes = 0;
                  }

                  entry.addProperty("built", classes > 0);
                  entry.addProperty("classes", classes);
                  entry.addProperty("jar", Files.isRegularFile(dir.resolve(id + ".jar")));
                  entry.addProperty("runtimeLoaded", init_Z(id));
                  entry.addProperty("installed", load_Z(id));
                  AiAddons.Inner_ConfigStore meta = init_CachePool_ConfigSt(dir);
                  if (meta != null && !meta.entryClass().isBlank()) {
                     entry.addProperty("entryClass", meta.entryClass());
                  }

                  addons.add(entry);
               }
            }
         } catch (IOException e) {
            return init_AiOperation(AiOperation.Status.FAILED, "List failed: " + e.getMessage(), new JsonObject());
         }
      }

      return init_AiOperation(AiOperation.Status.SUCCEEDED, "Found " + addons.size() + " scaffolded addon(s)", AiJson.objectOf("addons", addons));
   }

   private static AiOperation close(JsonObject args) {
      String id = init_Str(AiJson.getString(args, "id", ""));
      boolean loaded = init_Z(id);
      JsonObject data = AiJson.objectOf("id", id, "runtimeLoaded", loaded);
      autismclient.b.DataUtil.StreamUtil report = init_DataUtil_StreamUti(id);
      if (report != null) {
         data.addProperty("reportStatus", String.valueOf(report.init_DataUtil_DataUtil()));
         data.addProperty("reportPhase", report.update_Str());
         data.addProperty("accepted", report.close());
         if (!report.sync_Str().isBlank()) {
            data.addProperty("failureReason", report.sync_Str());
         }

         if (report.sync_I() > 0) {
            data.addProperty("rejected", report.sync_I());
         }
      }

      AiAddons.Inner_ConfigStore meta = init_CachePool_ConfigSt(load_Path(id));
      if (meta != null && !meta.lastRegistered().isEmpty()) {
         data.add("registered", AiJson.GSON.toJsonTree(meta.lastRegistered().stream().limit(40L).toList()));
      }

      return init_AiOperation(
         loaded ? AiOperation.Status.SUCCEEDED : AiOperation.Status.FAILED,
         loaded
            ? "Runtime addon '" + id + "' is live; the AI catalog picks its capabilities up within ~20 ticks"
            : "Runtime addon '" + id + "' is not loaded" + (report == null ? "" : " (" + report.init_DataUtil_DataUtil() + ": " + report.sync_Str() + ")"),
         data
      );
   }

   private static boolean init_Z(String id) {
      try {
         return autismclient.b.DataUtil.load_Z(id);
      } catch (Throwable t) {
         return false;
      }
   }

   private static autismclient.b.DataUtil.StreamUtil init_DataUtil_StreamUti(String id) {
      try {
         return autismclient.b.DataUtil.init_DataUtil_StreamUti(id);
      } catch (Throwable t) {
         return null;
      }
   }

   private static boolean load_Z(String id) {
      try {
         return Files.isRegularFile(FabricLoader.getInstance().getGameDir().resolve("mods").resolve(id + ".jar"));
      } catch (Throwable t) {
         return false;
      }
   }

   record Inner_BufferHelper(String file, long line, String kind, String message) {
      JsonObject toJson() {
         JsonObject out = new JsonObject();
         out.addProperty("file", this.file);
         out.addProperty("line", this.line);
         out.addProperty("message", this.message);
         return out;
      }
   }

   interface Inner_CachePool {
      AiAddons.Inner_StreamUtil find();
   }

   record Inner_ConfigStore(String id, String name, String description, String pkg, String entryClass, List<String> features, List<String> lastRegistered) {
      JsonObject toJson() {
         JsonObject out = new JsonObject();
         out.addProperty("id", this.id);
         out.addProperty("name", this.name);
         out.addProperty("description", this.description);
         out.addProperty("package", this.pkg);
         out.addProperty("entryClass", this.entryClass);
         out.add("features", AiJson.GSON.toJsonTree(this.features));
         out.add("lastRegistered", AiJson.GSON.toJsonTree(this.lastRegistered));
         return out;
      }

      AiAddons.Inner_ConfigStore withRegistered(List<String> registered) {
         return new AiAddons.Inner_ConfigStore(this.id, this.name, this.description, this.pkg, this.entryClass, this.features, registered);
      }

      static AiAddons.Inner_ConfigStore fromJson(JsonObject json) {
         List<String> features = new ArrayList<>();
         List<String> registered = new ArrayList<>();
         if (json.has("features")) {
            for (JsonElement e : json.getAsJsonArray("features")) {
               features.add(e.getAsString());
            }
         }

         if (json.has("lastRegistered")) {
            for (JsonElement e : json.getAsJsonArray("lastRegistered")) {
               registered.add(e.getAsString());
            }
         }

         return new AiAddons.Inner_ConfigStore(
            AiJson.getString(json, "id", ""),
            AiJson.getString(json, "name", ""),
            AiJson.getString(json, "description", ""),
            AiJson.getString(json, "package", ""),
            AiJson.getString(json, "entryClass", ""),
            features,
            registered
         );
      }
   }

   record Inner_DataUtil(
      boolean success,
      String message,
      List<AiAddons.Inner_BufferHelper> errors,
      int warningCount,
      int classCount,
      String jar,
      String compiler,
      boolean cancelled
   ) {
   }

   record Inner_StreamUtil(JavaCompiler system, Path executable) {
      boolean inProcess() {
         return this.system != null;
      }

      String describe() {
         return this.system != null ? "in-process JDK compiler" : this.executable.toString();
      }
   }
}

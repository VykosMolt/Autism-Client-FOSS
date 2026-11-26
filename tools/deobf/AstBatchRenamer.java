import com.sun.source.tree.*;
import com.sun.source.util.*;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;
import java.util.regex.*;
import javax.lang.model.element.*;
import javax.lang.model.type.*;
import javax.lang.model.util.*;
import javax.tools.*;

/**
 * Compiler-grade JDK-25 AST Batch Renamer for AUTISM Client deobfuscation.
 * Uses jdk.compiler (JavacTask.analyze()) for exact symbol resolution across:
 *   - Enum constant renames (E): decl, MemberSelect, Identifier (incl. switch cases & static imports)
 *   - In-place top-level class renames (C): decl, constructors, imports, all type/expr refs, file move
 * Never reads or modifies frozen integrity/attestation/AI/API files.
 * Synchronizes mappings/disambig.tsv atomically so ReverseRemapJar preserves 100% runtime ABI.
 */
public final class AstBatchRenamer {

    private static final List<String> FROZEN_PREFIXES = List.of(
        "autismclient/e/", "autismclient/sys/", "autismclient/obf/", "autismclient/a/",
        "autismclient/util/mm/guardian/", "autismclient/api/", "autismclient/ai/"
    );
    // Never read or edit these source files at all (untouched integrity / attestation layer)
    private static final Set<String> UNTOUCHABLE_SOURCE_EXACT = Set.of(
        "autismclient/modules/al",
        "autismclient/util/AutismLiteVariant",
        "autismclient/util/bX",
        "autismclient/gui/a/DataUtil",
        "autismclient/gui/d/JsonUtil"
    );
    // Non-integrity files whose OWN class name is compile-time ABI-pinned by untouched files
    // (cannot be renamed themselves, but references inside them to OTHER renamed classes must still be updated!)
    private static final Set<String> FROZEN_SYMBOL_EXACT = Set.of(
        "autismclient/modules/f",
        "autismclient/modules/as",
        "autismclient/util/macro/ak",
        "autismclient/util/Q",
        "autismclient/util/mm/a/StreamUtil"
    );
    private static final Set<String> FROZEN_ENUMS = Set.of(
        "autismclient/gui/f/b/CachePool$Inner_DataUtil",
        "autismclient/gui/f/b/l",
        "autismclient/util/bW$Inner_StreamUtil",
        "autismclient/util/macro/ak$Inner_StreamUtil"
    );

    record EnumRename(String curEnum, String curConst, String newConst, String origEnum, String origConst) {}
    record ClassRename(String curClass, String newClass, String origClass) {}
    record TextEdit(int start, int end, String oldText, String newText, String kind) {}

    private static boolean isFrozenInternal(String internal) {
        if (internal == null) return true;
        if (FROZEN_ENUMS.contains(internal)) return true;
        String base = internal.contains("$") ? internal.substring(0, internal.indexOf('$')) : internal;
        if (UNTOUCHABLE_SOURCE_EXACT.contains(base) || FROZEN_SYMBOL_EXACT.contains(base)) return true;
        for (String p : FROZEN_PREFIXES) {
            if (base.startsWith(p)) return true;
        }
        return false;
    }

    private static boolean isHardSkipSource(Path srcRoot, Path file) {
        String rel = srcRoot.relativize(file).toString().replace('\\', '/');
        if (!rel.endsWith(".java")) return true;
        String internal = rel.substring(0, rel.length() - 5);
        // Never read integrity / auth / attestation source files at all!
        if (internal.startsWith("autismclient/e/") || internal.startsWith("autismclient/sys/")
            || internal.startsWith("autismclient/obf/") || internal.startsWith("autismclient/a/")
            || internal.startsWith("autismclient/util/mm/guardian/")) return true;
        if (UNTOUCHABLE_SOURCE_EXACT.contains(internal)) return true;
        return false;
    }

    private static String toInternalName(TypeElement te, Elements elements) {
        String binary = elements.getBinaryName(te).toString();
        return binary.replace('.', '/');
    }

    public static void main(String[] args) throws Exception {
        if (args.length < 4) {
            System.err.println("Usage: AstBatchRenamer <repo-dir> <classpath-file> <plan-tsv> <--dry-run|--apply>");
            System.exit(2);
        }
        Path repo = Path.of(args[0]).toAbsolutePath().normalize();
        String cp = Files.readString(Path.of(args[1]), StandardCharsets.UTF_8).trim();
        Path planFile = Path.of(args[2]).toAbsolutePath().normalize();
        boolean apply = "--apply".equals(args[3]);
        if (!apply && !"--dry-run".equals(args[3])) {
            throw new IllegalArgumentException("Expected --dry-run or --apply, got: " + args[3]);
        }

        // 1. Parse rename plan
        Map<String, EnumRename> enumRenames = new LinkedHashMap<>(); // curEnum + "#" + curConst -> EnumRename
        Map<String, ClassRename> classRenames = new LinkedHashMap<>(); // curClass -> ClassRename
        for (String raw : Files.readAllLines(planFile, StandardCharsets.UTF_8)) {
            if (raw.isEmpty() || raw.startsWith("#")) continue;
            String[] p = raw.split("\t", -1);
            switch (p[0]) {
                case "E" -> {
                    EnumRename er = new EnumRename(p[1], p[2], p[3], p[4], p[5]);
                    if (isFrozenInternal(er.curEnum)) throw new IllegalStateException("Refused frozen enum: " + er.curEnum);
                    enumRenames.put(er.curEnum + "#" + er.curConst, er);
                }
                case "C" -> {
                    ClassRename cr = new ClassRename(p[1], p[2], p[3]);
                    if (isFrozenInternal(cr.curClass)) throw new IllegalStateException("Refused frozen class: " + cr.curClass);
                    if (!parentPkg(cr.curClass).equals(parentPkg(cr.newClass))) {
                        throw new IllegalStateException("Cross-package class move not enabled in in-place mode: " + cr);
                    }
                    classRenames.put(cr.curClass, cr);
                }
                default -> throw new IllegalArgumentException("Unknown plan tag: " + p[0]);
            }
        }
        System.out.printf("Loaded plan: %d enum constant renames, %d class renames (%s)%n",
            enumRenames.size(), classRenames.size(), apply ? "APPLY" : "DRY-RUN");

        // 2. Collect non-integrity source files in src/main/java
        Path srcRoot = repo.resolve("src/main/java");
        List<Path> srcFiles = new ArrayList<>();
        try (var stream = Files.walk(srcRoot)) {
            stream.filter(Files::isRegularFile)
                  .filter(f -> !isHardSkipSource(srcRoot, f))
                  .sorted()
                  .forEach(srcFiles::add);
        }
        System.out.printf("Analyzing %d non-integrity source files via JDK-25 JavacTask...%n", srcFiles.size());

        Map<Path, List<TextEdit>> editsByFile = new TreeMap<>();
        Set<String> matchedEnumKeys = new HashSet<>();
        Set<String> matchedClassKeys = new HashSet<>();
        List<String> warnings = new ArrayList<>();

        analyzeBatch(srcFiles, cp, enumRenames, classRenames, editsByFile, matchedEnumKeys, matchedClassKeys, warnings, false);

        // 3. Also analyze tools/checks/*Check.java individually against build/classes/java/main + cp
        Path checksDir = repo.resolve("tools/checks");
        List<Path> checkFiles = new ArrayList<>();
        try (var stream = Files.list(checksDir)) {
            stream.filter(f -> f.getFileName().toString().endsWith("Check.java"))
                  .sorted()
                  .forEach(checkFiles::add);
        }
        int checkEditsTotal = 0;
        for (Path cf : checkFiles) {
            int before = editsByFile.getOrDefault(cf, List.of()).size();
            analyzeBatch(List.of(cf), cp, enumRenames, classRenames, editsByFile, matchedEnumKeys, matchedClassKeys, warnings, true);
            checkEditsTotal += editsByFile.getOrDefault(cf, List.of()).size() - before;
        }
        System.out.printf("Analyzed %d check files (%d edits in tools/checks/).%n", checkFiles.size(), checkEditsTotal);

        // 4. Verify coverage & non-overlapping edits
        int totalEdits = 0;
        int filesTouched = 0;
        for (var entry : editsByFile.entrySet()) {
            Path f = entry.getKey();
            List<TextEdit> list = new ArrayList<>(entry.getValue());
            if (list.isEmpty()) continue;
            // Deduplicate identical (start, end, newText) edits
            Map<Long, TextEdit> dedup = new TreeMap<>();
            for (TextEdit te : list) {
                long key = (((long) te.start) << 32) | (te.end & 0xFFFFFFFFL);
                TextEdit prev = dedup.putIfAbsent(key, te);
                if (prev != null && !prev.newText.equals(te.newText)) {
                    throw new IllegalStateException("Conflicting edits at " + f + " [" + te.start + "," + te.end + "]: " + prev + " vs " + te);
                }
            }
            List<TextEdit> sorted = new ArrayList<>(dedup.values());
            sorted.sort(Comparator.comparingInt(TextEdit::start));
            String content = Files.readString(f, StandardCharsets.UTF_8);
            int prevEnd = -1;
            for (TextEdit te : sorted) {
                if (te.start < prevEnd || te.start < 0 || te.end > content.length() || te.start >= te.end) {
                    throw new IllegalStateException("Invalid/overlapping edit in " + f + ": " + te + " (prevEnd=" + prevEnd + ")");
                }
                String actual = content.substring(te.start, te.end);
                if (!actual.equals(te.oldText)) {
                    throw new IllegalStateException("Token mismatch in " + f + " at " + te.start + ": expected '" + te.oldText + "' got '" + actual + "' (" + te.kind + ")");
                }
                prevEnd = te.end;
            }
            entry.setValue(sorted);
            filesTouched++;
            totalEdits += sorted.size();
        }

        // Report any unmatched plan entries
        List<String> unmatchedEnums = new ArrayList<>();
        for (String k : enumRenames.keySet()) if (!matchedEnumKeys.contains(k)) unmatchedEnums.add(k);
        List<String> unmatchedClasses = new ArrayList<>();
        for (String k : classRenames.keySet()) if (!matchedClassKeys.contains(k)) unmatchedClasses.add(k);

        System.out.printf("RESULT: %d files to edit, %d total AST-verified token edits.%n", filesTouched, totalEdits);
        System.out.printf("Unmatched enum declarations: %d, unmatched class declarations: %d, warnings: %d%n",
            unmatchedEnums.size(), unmatchedClasses.size(), warnings.size());
        for (int i = 0; i < Math.min(20, unmatchedEnums.size()); i++) System.out.println("  UNMATCHED_ENUM: " + unmatchedEnums.get(i));
        for (int i = 0; i < Math.min(20, unmatchedClasses.size()); i++) System.out.println("  UNMATCHED_CLASS: " + unmatchedClasses.get(i));
        for (int i = 0; i < Math.min(20, warnings.size()); i++) System.out.println("  WARN: " + warnings.get(i));

        // 5. Simulate disambig.tsv update
        Path mappingsPath = repo.resolve("mappings/disambig.tsv");
        List<String> updatedMappings = computeUpdatedDisambig(mappingsPath, enumRenames, classRenames);
        System.out.printf("mappings/disambig.tsv: %d -> %d lines%n",
            Files.readAllLines(mappingsPath, StandardCharsets.UTF_8).size(), updatedMappings.size());

        if (!apply) {
            System.out.println("DRY-RUN COMPLETE (no files modified).");
            return;
        }
        if (!unmatchedEnums.isEmpty() || !unmatchedClasses.isEmpty()) {
            throw new IllegalStateException("Aborting --apply because plan has unmatched entries!");
        }

        // 6. Apply source edits
        for (var entry : editsByFile.entrySet()) {
            List<TextEdit> sorted = entry.getValue();
            if (sorted.isEmpty()) continue;
            String content = Files.readString(entry.getKey(), StandardCharsets.UTF_8);
            StringBuilder sb = new StringBuilder(content.length() + 256);
            int pos = 0;
            for (TextEdit te : sorted) {
                sb.append(content, pos, te.start);
                sb.append(te.newText);
                pos = te.end;
            }
            sb.append(content, pos, content.length());
            Files.writeString(entry.getKey(), sb.toString(), StandardCharsets.UTF_8);
        }

        // 7. Rename top-level class files
        for (ClassRename cr : classRenames.values()) {
            Path oldPath = srcRoot.resolve(cr.curClass + ".java");
            Path newPath = srcRoot.resolve(cr.newClass + ".java");
            if (Files.exists(newPath)) {
                throw new IllegalStateException("Target file already exists: " + newPath);
            }
            Files.move(oldPath, newPath);
        }

        // 8. Write updated mappings/disambig.tsv
        Files.write(mappingsPath, updatedMappings, StandardCharsets.UTF_8);
        System.out.println("APPLIED ALL EDITS, FILE MOVES, AND MAPPINGS CLEANLY.");
    }

    private static void analyzeBatch(
        List<Path> files,
        String cp,
        Map<String, EnumRename> enumRenames,
        Map<String, ClassRename> classRenames,
        Map<Path, List<TextEdit>> editsByFile,
        Set<String> matchedEnumKeys,
        Set<String> matchedClassKeys,
        List<String> warnings,
        boolean singleCheckMode
    ) throws Exception {
        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        DiagnosticCollector<JavaFileObject> diags = new DiagnosticCollector<>();
        try (StandardJavaFileManager fm = compiler.getStandardFileManager(diags, Locale.ROOT, StandardCharsets.UTF_8)) {
            Iterable<? extends JavaFileObject> fos = fm.getJavaFileObjectsFromPaths(files);
            List<String> opts = List.of("--release", "25", "-proc:none", "-nowarn", "-classpath", cp);
            JavacTask task = (JavacTask) compiler.getTask(null, fm, diags, opts, null, fos);
            Iterable<? extends CompilationUnitTree> cus = task.parse();
            task.analyze();

            int errors = 0;
            for (Diagnostic<? extends JavaFileObject> d : diags.getDiagnostics()) {
                if (d.getKind() == Diagnostic.Kind.ERROR) {
                    errors++;
                    if (errors <= 10) warnings.add("JAVAC_ERR: " + d);
                }
            }
            if (errors > 0) {
                throw new IllegalStateException("JavacTask.analyze() reported " + errors + " errors (mode=" + (singleCheckMode ? "check" : "main") + "); first: " + warnings);
            }

            Trees trees = Trees.instance(task);
            Elements elements = task.getElements();
            SourcePositions sp = trees.getSourcePositions();

            for (CompilationUnitTree cu : cus) {
                Path file = Path.of(cu.getSourceFile().toUri()).toAbsolutePath().normalize();
                String content = Files.readString(file, StandardCharsets.UTF_8);
                List<TextEdit> edits = editsByFile.computeIfAbsent(file, k -> new ArrayList<>());

                // Handle explicit imports separately (Javac Trees.getElement on ImportTree can be null in some forms)
                for (ImportTree imp : cu.getImports()) {
                    Tree q = imp.getQualifiedIdentifier();
                    if (q instanceof MemberSelectTree ms) {
                        String full = q.toString();
                        int dot = full.lastIndexOf('.');
                        if (dot > 0) {
                            String left = full.substring(0, dot);
                            String right = full.substring(dot + 1);
                            if (imp.isStatic()) {
                                // import static pkg.Enum.OLD_CONST; or import static pkg.OldClass.*; or import static pkg.OldClass.member;
                                String leftInt = left.replace('.', '/');
                                // Could be nested enum: try replacing dots from right until match
                                EnumRename er = findEnumImport(left, right, enumRenames);
                                if (er != null) {
                                    addMemberSelectEdit(cu, sp, content, ms, er.curConst, er.newConst, "static-import-enum", edits);
                                }
                                ClassRename crLeft = classRenames.get(leftInt);
                                if (crLeft != null) {
                                    // left is pkg.OldClass -> rename identifier OldClass inside ms.getExpression()
                                    if (ms.getExpression() instanceof MemberSelectTree lhsMs) {
                                        addMemberSelectEdit(cu, sp, content, lhsMs, simpleName(crLeft.curClass), simpleName(crLeft.newClass), "static-import-class", edits);
                                    }
                                }
                            } else {
                                // import pkg.OldClass; or import pkg.OldClass.Inner; or import pkg.OldClass.*;
                                if (!"*".equals(right)) {
                                    ClassRename cr = classRenames.get(full.replace('.', '/'));
                                    if (cr != null) {
                                        addMemberSelectEdit(cu, sp, content, ms, simpleName(cr.curClass), simpleName(cr.newClass), "import-class", edits);
                                    }
                                }
                                // Walk up ms expression chain in case of import pkg.OldClass.Inner; or import pkg.OldClass.*;
                                ExpressionTree expr = ms.getExpression();
                                while (expr instanceof MemberSelectTree ems) {
                                    ClassRename cr = classRenames.get(ems.toString().replace('.', '/'));
                                    if (cr != null) {
                                        addMemberSelectEdit(cu, sp, content, ems, simpleName(cr.curClass), simpleName(cr.newClass), "import-outer-class", edits);
                                    }
                                    expr = ems.getExpression();
                                }
                            }
                        }
                    }
                }

                new TreePathScanner<Void, Void>() {
                    @Override
                    public Void visitImport(ImportTree node, Void p) {
                        return null; // already handled above
                    }

                    @Override
                    public Void visitClass(ClassTree node, Void p) {
                        Element el = trees.getElement(getCurrentPath());
                        if (el instanceof TypeElement te) {
                            String internal = toInternalName(te, elements);
                            ClassRename cr = classRenames.get(internal);
                            if (cr != null) {
                                matchedClassKeys.add(internal);
                                String oldSimple = simpleName(cr.curClass);
                                String newSimple = simpleName(cr.newClass);
                                int s = (int) sp.getStartPosition(cu, node);
                                int e = (int) sp.getEndPosition(cu, node);
                                int headerEnd = content.indexOf('{', s);
                                if (headerEnd < 0 || headerEnd > e) headerEnd = e;
                                int tokStart = findClassDeclName(content, s, headerEnd, oldSimple);
                                if (tokStart < 0) throw new IllegalStateException("Cannot locate class decl name " + oldSimple + " in " + file);
                                edits.add(new TextEdit(tokStart, tokStart + oldSimple.length(), oldSimple, newSimple, "class-decl"));
                            }
                        }
                        return super.visitClass(node, p);
                    }

                    @Override
                    public Void visitMethod(MethodTree node, Void p) {
                        if (node.getName().contentEquals("<init>")) {
                            Element el = trees.getElement(getCurrentPath());
                            if (el instanceof ExecutableElement ee && ee.getEnclosingElement() instanceof TypeElement te) {
                                String internal = toInternalName(te, elements);
                                ClassRename cr = classRenames.get(internal);
                                if (cr != null) {
                                    int s = (int) sp.getStartPosition(cu, node);
                                    int e = (int) sp.getEndPosition(cu, node);
                                    // Skip synthetic/default constructors (start == -1 or end == -1)
                                    if (s >= 0 && e > s) {
                                        String oldSimple = simpleName(cr.curClass);
                                        String newSimple = simpleName(cr.newClass);
                                        int paren = content.indexOf('(', s);
                                        if (paren > s && paren <= e) {
                                            int tokStart = findLastIdentToken(content, s, paren, oldSimple);
                                            if (tokStart >= 0) {
                                                edits.add(new TextEdit(tokStart, tokStart + oldSimple.length(), oldSimple, newSimple, "ctor-decl"));
                                            }
                                        }
                                    }
                                }
                            }
                        }
                        return super.visitMethod(node, p);
                    }

                    @Override
                    public Void visitVariable(VariableTree node, Void p) {
                        Element el = trees.getElement(getCurrentPath());
                        if (el instanceof VariableElement ve && ve.getKind() == ElementKind.ENUM_CONSTANT) {
                            if (ve.getEnclosingElement() instanceof TypeElement te) {
                                String enumInt = toInternalName(te, elements);
                                String key = enumInt + "#" + ve.getSimpleName().toString();
                                EnumRename er = enumRenames.get(key);
                                if (er != null) {
                                    matchedEnumKeys.add(key);
                                    int s = (int) sp.getStartPosition(cu, node);
                                    int e = (int) sp.getEndPosition(cu, node);
                                    // Start after annotations on the enum constant if any
                                    int searchStart = s;
                                    ModifiersTree mods = node.getModifiers();
                                    if (mods != null && !mods.getAnnotations().isEmpty()) {
                                        int modEnd = (int) sp.getEndPosition(cu, mods);
                                        if (modEnd > searchStart) searchStart = modEnd;
                                    }
                                    int tokStart = findFirstIdentToken(content, searchStart, e, er.curConst);
                                    if (tokStart < 0) throw new IllegalStateException("Cannot locate enum const decl " + er + " in " + file);
                                    edits.add(new TextEdit(tokStart, tokStart + er.curConst.length(), er.curConst, er.newConst, "enum-const-decl"));
                                }
                            }
                        }
                        return super.visitVariable(node, p);
                    }

                    @Override
                    public Void visitMemberSelect(MemberSelectTree node, Void p) {
                        Element el = trees.getElement(getCurrentPath());
                        if (el instanceof VariableElement ve && ve.getKind() == ElementKind.ENUM_CONSTANT
                            && ve.getEnclosingElement() instanceof TypeElement te) {
                            String key = toInternalName(te, elements) + "#" + ve.getSimpleName().toString();
                            EnumRename er = enumRenames.get(key);
                            if (er != null) {
                                addMemberSelectEdit(cu, sp, content, node, er.curConst, er.newConst, "enum-select", edits);
                            }
                        } else if (el instanceof TypeElement te) {
                            String internal = toInternalName(te, elements);
                            ClassRename cr = classRenames.get(internal);
                            if (cr != null) {
                                addMemberSelectEdit(cu, sp, content, node, simpleName(cr.curClass), simpleName(cr.newClass), "class-select", edits);
                            }
                        }
                        return super.visitMemberSelect(node, p);
                    }

                    @Override
                    public Void visitIdentifier(IdentifierTree node, Void p) {
                        Element el = trees.getElement(getCurrentPath());
                        if (el instanceof VariableElement ve && ve.getKind() == ElementKind.ENUM_CONSTANT
                            && ve.getEnclosingElement() instanceof TypeElement te) {
                            String key = toInternalName(te, elements) + "#" + ve.getSimpleName().toString();
                            EnumRename er = enumRenames.get(key);
                            if (er != null) {
                                int s = (int) sp.getStartPosition(cu, node);
                                int e = (int) sp.getEndPosition(cu, node);
                                if (s >= 0 && e > s && content.substring(s, e).equals(er.curConst)) {
                                    edits.add(new TextEdit(s, e, er.curConst, er.newConst, "enum-ident"));
                                }
                            }
                        } else if (el instanceof TypeElement te) {
                            String internal = toInternalName(te, elements);
                            ClassRename cr = classRenames.get(internal);
                            if (cr != null) {
                                String oldSimple = simpleName(cr.curClass);
                                int s = (int) sp.getStartPosition(cu, node);
                                int e = (int) sp.getEndPosition(cu, node);
                                if (s >= 0 && e > s && content.substring(s, e).equals(oldSimple)) {
                                    edits.add(new TextEdit(s, e, oldSimple, simpleName(cr.newClass), "class-ident"));
                                }
                            }
                        }
                        return super.visitIdentifier(node, p);
                    }
                }.scan(cu, null);
            }
        }
    }

    private static EnumRename findEnumImport(String leftDotted, String right, Map<String, EnumRename> enumRenames) {
        // Direct top-level enum: pkg.Enum
        String candidate = leftDotted.replace('.', '/');
        EnumRename hit = enumRenames.get(candidate + "#" + right);
        if (hit != null) return hit;
        // Nested enum: replace trailing dots with $
        String cur = candidate;
        for (int i = 0; i < 4; i++) {
            int slash = cur.lastIndexOf('/');
            if (slash < 0) break;
            cur = cur.substring(0, slash) + "$" + cur.substring(slash + 1);
            hit = enumRenames.get(cur + "#" + right);
            if (hit != null) return hit;
        }
        return null;
    }

    private static void addMemberSelectEdit(
        CompilationUnitTree cu, SourcePositions sp, String content,
        MemberSelectTree ms, String oldName, String newName, String kind, List<TextEdit> edits
    ) {
        int recvEnd = (int) sp.getEndPosition(cu, ms.getExpression());
        int nodeEnd = (int) sp.getEndPosition(cu, ms);
        if (recvEnd < 0 || nodeEnd <= recvEnd) return;
        int tokStart = findLastIdentToken(content, recvEnd, nodeEnd, oldName);
        if (tokStart < 0) {
            throw new IllegalStateException("Cannot locate member select token '" + oldName + "' in slice: " + content.substring(recvEnd, nodeEnd));
        }
        edits.add(new TextEdit(tokStart, tokStart + oldName.length(), oldName, newName, kind));
    }

    private static int findClassDeclName(String content, int start, int end, String ident) {
        Matcher m = Pattern.compile("\\b(?:class|interface|enum|record)\\s+(" + Pattern.quote(ident) + ")\\b").matcher(content);
        m.region(start, end);
        return m.find() ? m.start(1) : -1;
    }

    private static int findFirstIdentToken(String content, int start, int end, String ident) {
        Matcher m = Pattern.compile("(?<![A-Za-z0-9_$])" + Pattern.quote(ident) + "(?![A-Za-z0-9_$])").matcher(content);
        m.region(start, end);
        return m.find() ? m.start() : -1;
    }

    private static int findLastIdentToken(String content, int start, int end, String ident) {
        Matcher m = Pattern.compile("(?<![A-Za-z0-9_$])" + Pattern.quote(ident) + "(?![A-Za-z0-9_$])").matcher(content);
        m.region(start, end);
        int last = -1;
        while (m.find()) last = m.start();
        return last;
    }

    private static String simpleName(String internal) {
        int i = internal.lastIndexOf('/');
        return i >= 0 ? internal.substring(i + 1) : internal;
    }

    private static String parentPkg(String internal) {
        int i = internal.lastIndexOf('/');
        return i >= 0 ? internal.substring(0, i) : "";
    }

    private static String remapClassPrefix(String internal, Map<String, ClassRename> classRenames) {
        if (classRenames.isEmpty()) return internal;
        ClassRename exact = classRenames.get(internal);
        if (exact != null) return exact.newClass;
        int dollar = internal.indexOf('$');
        if (dollar > 0) {
            String outer = internal.substring(0, dollar);
            ClassRename outerCr = classRenames.get(outer);
            if (outerCr != null) return outerCr.newClass + internal.substring(dollar);
        }
        return internal;
    }

    private static String remapDescriptor(String desc, Map<String, ClassRename> classRenames) {
        if (classRenames.isEmpty() || !desc.contains("Lautismclient/")) return desc;
        StringBuilder sb = new StringBuilder(desc.length() + 32);
        int i = 0;
        while (i < desc.length()) {
            int l = desc.indexOf("Lautismclient/", i);
            if (l < 0) {
                sb.append(desc, i, desc.length());
                break;
            }
            sb.append(desc, i, l);
            int semi = desc.indexOf(';', l);
            String cls = desc.substring(l + 1, semi);
            sb.append('L').append(remapClassPrefix(cls, classRenames)).append(';');
            i = semi + 1;
        }
        return sb.toString();
    }

    private static List<String> computeUpdatedDisambig(
        Path mappingsPath,
        Map<String, EnumRename> enumRenames,
        Map<String, ClassRename> classRenames
    ) throws IOException {
        List<String> lines = Files.readAllLines(mappingsPath, StandardCharsets.UTF_8);
        List<String> out = new ArrayList<>(lines.size() + enumRenames.size() + classRenames.size());

        Set<String> seenFieldKeys = new HashSet<>(); // origOwner + "\t" + origName + "\t" + origDesc
        Set<String> seenClassOrig = new HashSet<>(); // origClass

        // Index enum renames by (origEnum, origConst, origDesc)
        Map<String, EnumRename> enumByOrig = new HashMap<>();
        for (EnumRename er : enumRenames.values()) {
            enumByOrig.put(er.origEnum + "\t" + er.origConst + "\tL" + er.origEnum + ";", er);
        }

        for (String line : lines) {
            if (line.isEmpty() || line.startsWith("#")) {
                out.add(line);
                continue;
            }
            String[] p = line.split("\t", -1);
            switch (p[0]) {
                case "I" -> out.add(line);
                case "C" -> {
                    seenClassOrig.add(p[1]);
                    String newCur = remapClassPrefix(p[2], classRenames);
                    out.add("C\t" + p[1] + "\t" + newCur);
                }
                case "F" -> {
                    String origKey = p[1] + "\t" + p[2] + "\t" + p[3];
                    seenFieldKeys.add(origKey);
                    String newOwner = remapClassPrefix(p[4], classRenames);
                    String newField = p[5];
                    String newDesc = remapDescriptor(p[6], classRenames);
                    EnumRename er = enumByOrig.get(origKey);
                    if (er != null) {
                        newField = er.newConst;
                    }
                    out.add(String.join("\t", "F", p[1], p[2], p[3], newOwner, newField, newDesc));
                }
                case "M" -> {
                    String newOwner = remapClassPrefix(p[4], classRenames);
                    String newDesc = remapDescriptor(p[6], classRenames);
                    out.add(String.join("\t", "M", p[1], p[2], p[3], newOwner, p[5], newDesc));
                }
                default -> out.add(line);
            }
        }

        // Add new C rows for renamed classes that weren't previously in disambig.tsv
        for (ClassRename cr : classRenames.values()) {
            if (!seenClassOrig.contains(cr.origClass)) {
                out.add("C\t" + cr.origClass + "\t" + cr.newClass);
            }
        }

        // Add new F rows for renamed enum constants that weren't previously collided in disambig.tsv
        for (EnumRename er : enumRenames.values()) {
            String origDesc = "L" + er.origEnum + ";";
            String origKey = er.origEnum + "\t" + er.origConst + "\t" + origDesc;
            if (!seenFieldKeys.contains(origKey)) {
                String newOwner = remapClassPrefix(er.curEnum, classRenames);
                String newDesc = "L" + newOwner + ";";
                out.add(String.join("\t", "F", er.origEnum, er.origConst, origDesc, newOwner, er.newConst, newDesc));
            }
        }

        return out;
    }
}

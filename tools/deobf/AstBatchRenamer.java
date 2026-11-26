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
 *   - Top-level class renames & cross-package moves (C): package decl, decl, constructors, imports,
 *     all type/expr/FQN refs, same-package split import synthesis, file move & empty dir cleanup
 *   - Nested member class renames (N): decl, constructors (incl. compact record ctors), imports,
 *     all type/expr refs, multi-level $ disambig.tsv sync (no file moves; local $N classes rejected)
 *   - Field renames (F): decl, MemberSelect, Identifier, static imports (blocks frozen, Gson, MacroAction public fields)
 *   - Method renames (M): decl, MemberSelect, Identifier, MemberReference (::), static imports, full override-family check
 *   - Record component renames (R): header decl, compact/canonical ctor params, field refs, accessor decl/calls/refs, F+M sync
 * Never reads or modifies untouched integrity/attestation files; never renames symbols declared in autismclient/api/.
 * Synchronizes mappings/disambig.tsv atomically so ReverseRemapJar preserves 100% runtime ABI.
 */
public final class AstBatchRenamer {

    private static final List<String> FROZEN_PREFIXES = List.of(
        "autismclient/e/", "autismclient/sys/", "autismclient/obf/", "autismclient/a/",
        "autismclient/util/mm/guardian/", "autismclient/api/"
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
        "autismclient/modules/aD",
        "autismclient/modules/ae",
        "autismclient/modules/am",
        "autismclient/modules/ar",
        "autismclient/util/macro/ak",
        "autismclient/util/L",
        "autismclient/util/Q",
        "autismclient/util/RefQueue",
        "autismclient/util/ValueSink",
        "autismclient/util/aJ",
        "autismclient/util/at",
        "autismclient/util/bK",
        "autismclient/util/bW",
        "autismclient/util/bi",
        "autismclient/util/cA",
        "autismclient/util/cf",
        "autismclient/b/DataUtil",
        "autismclient/util/g/PkgCls_a",
        "autismclient/util/mm/IoWorker",
        "autismclient/util/mm/StateCache",
        "autismclient/util/mm/a/CachePool",
        "autismclient/util/mm/a/DataUtil",
        "autismclient/util/mm/a/StreamUtil",
        // Compile-time GUI ABI pins directly referenced by untouched gui/d/JsonUtil.java
        "autismclient/gui/f/NetHelper",
        "autismclient/gui/f/PacketUtil",
        "autismclient/gui/f/SyncManager",
        "autismclient/gui/f/UiHelper",
        "autismclient/gui/f/a/DataUtil",
        "autismclient/gui/f/b/BufferHelper",
        "autismclient/gui/f/b/CachePool",
        "autismclient/gui/f/b/CharEncoder",
        "autismclient/gui/f/b/k",
        "autismclient/gui/f/b/l",
        // Compile-time AI ABI pin directly referenced by untouched modules/al.java
        "autismclient/ai/aP"
    );
    private static final Set<String> FROZEN_ENUMS = Set.of(
        "autismclient/gui/f/b/CachePool$Inner_DataUtil",
        "autismclient/gui/f/b/l",
        "autismclient/util/bW$Inner_StreamUtil",
        "autismclient/util/macro/ak$Inner_StreamUtil"
    );
    // Gson-serialized top-level classes (0 @SerializedName annotations: NEVER rename fields or record components!)
    private static final Set<String> GSON_LOCKED_BASE = Set.of(
        "autismclient/util/AutismConfig",
        "autismclient/util/AutismWaypoints",
        "autismclient/util/ServerPluginScanCache",
        "autismclient/util/AutismPacketPreset",
        "autismclient/util/AutismProfile",
        "autismclient/util/oresim/AutismOreSimSeedStore",
        "autismclient/modules/ScaffoldFailureRecorder",
        "autismclient/ai/AiSettings",
        "autismclient/ai/AiToolSettings",
        "autismclient/ai/AiMemoryStore",
        "autismclient/ai/AiRegions",
        "autismclient/ai/AiCatalog"
    );
    private static final Set<String> FORBIDDEN_METHOD_NAMES = Set.of(
        "<init>", "<clinit>", "toString", "equals", "hashCode", "values", "valueOf",
        "compareTo", "clone", "finalize", "getClass", "notify", "notifyAll", "wait"
    );

    record EnumRename(String curEnum, String curConst, String newConst, String origEnum, String origConst) {}
    record ClassRename(String curClass, String newClass, String origClass) {}
    record FieldRename(String curOwner, String curName, String curDesc, String newName, String origOwner, String origName, String origDesc) {}
    record MethodRename(String curOwner, String curName, String curDesc, String newName, String origOwner, String origName, String origDesc) {}
    record RecordCompRename(String curRecord, String curName, String curDesc, String newName, String origRecord, String origFieldName, String origMethodName, String origDesc) {}
    record TextEdit(int start, int end, String oldText, String newText, String kind) {}

    private static boolean isGsonLocked(String internal) {
        if (internal == null) return true;
        String base = internal.contains("$") ? internal.substring(0, internal.indexOf('$')) : internal;
        return GSON_LOCKED_BASE.contains(base);
    }

    private static boolean isValidJavaIdent(String s) {
        if (s == null || s.isEmpty() || !Character.isJavaIdentifierStart(s.charAt(0))) return false;
        for (int i = 1; i < s.length(); i++) {
            if (!Character.isJavaIdentifierPart(s.charAt(i))) return false;
        }
        return !javax.lang.model.SourceVersion.isKeyword(s);
    }

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
        Map<String, FieldRename> fieldRenames = new LinkedHashMap<>(); // curOwner + "#" + curName + "#" + curDesc -> FieldRename
        Map<String, MethodRename> methodRenames = new LinkedHashMap<>(); // curOwner + "#" + curName + "#" + curDesc -> MethodRename
        Map<String, RecordCompRename> recordCompRenames = new LinkedHashMap<>(); // curRecord + "#" + curName + "#" + curDesc -> RecordCompRename
        Map<String, RecordCompRename> recordCompByMethodKey = new LinkedHashMap<>(); // curRecord + "#" + curName + "#()" + curDesc -> RecordCompRename
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
                    if (cr.curClass.contains("$") || cr.newClass.contains("$")) {
                        throw new IllegalArgumentException("Use tag N for nested class renames, got C: " + cr);
                    }
                    if (isFrozenInternal(cr.curClass)) throw new IllegalStateException("Refused frozen class: " + cr.curClass);
                    if (isFrozenInternal(cr.newClass)) throw new IllegalStateException("Refused frozen target class: " + cr.newClass);
                    classRenames.put(cr.curClass, cr);
                }
                case "N" -> {
                    ClassRename nr = new ClassRename(p[1], p[2], p[3]);
                    if (!nr.curClass.contains("$") || !nr.newClass.contains("$")) {
                        throw new IllegalArgumentException("Tag N requires nested class internal name with '$': " + nr);
                    }
                    if (nr.curClass.matches(".*\\$\\d+.*") || nr.newClass.matches(".*\\$\\d+.*")) {
                        throw new IllegalStateException("Refused local class rename ($N): " + nr);
                    }
                    if (!outerOwner(nr.curClass).equals(outerOwner(nr.newClass))) {
                        throw new IllegalStateException("Nested class rename cannot change enclosing owner: " + nr);
                    }
                    if (isFrozenInternal(nr.curClass)) throw new IllegalStateException("Refused frozen nested class: " + nr.curClass);
                    if (isFrozenInternal(nr.newClass)) throw new IllegalStateException("Refused frozen target nested class: " + nr.newClass);
                    classRenames.put(nr.curClass, nr);
                }
                case "F" -> {
                    FieldRename fr = new FieldRename(p[1], p[2], p[3], p[4], p[5], p[6], p[7]);
                    if (isFrozenInternal(fr.curOwner)) throw new IllegalStateException("Refused frozen field owner: " + fr.curOwner);
                    if (isGsonLocked(fr.curOwner)) throw new IllegalStateException("Refused Gson-locked field owner: " + fr.curOwner);
                    if (!isValidJavaIdent(fr.newName)) throw new IllegalArgumentException("Invalid target field name: " + fr.newName);
                    fieldRenames.put(fr.curOwner + "#" + fr.curName + "#" + fr.curDesc, fr);
                }
                case "M" -> {
                    MethodRename mr = new MethodRename(p[1], p[2], p[3], p[4], p[5], p[6], p[7]);
                    if (isFrozenInternal(mr.curOwner)) throw new IllegalStateException("Refused frozen method owner: " + mr.curOwner);
                    if (FORBIDDEN_METHOD_NAMES.contains(mr.curName) || FORBIDDEN_METHOD_NAMES.contains(mr.newName)) {
                        throw new IllegalStateException("Refused forbidden method rename: " + mr);
                    }
                    if (!isValidJavaIdent(mr.newName)) throw new IllegalArgumentException("Invalid target method name: " + mr.newName);
                    methodRenames.put(mr.curOwner + "#" + mr.curName + "#" + mr.curDesc, mr);
                }
                case "R" -> {
                    RecordCompRename rr = new RecordCompRename(p[1], p[2], p[3], p[4], p[5], p[6], p[7], p[8]);
                    if (isFrozenInternal(rr.curRecord)) throw new IllegalStateException("Refused frozen record: " + rr.curRecord);
                    if (isGsonLocked(rr.curRecord)) throw new IllegalStateException("Refused Gson-locked record: " + rr.curRecord);
                    if (FORBIDDEN_METHOD_NAMES.contains(rr.curName) || FORBIDDEN_METHOD_NAMES.contains(rr.newName)) {
                        throw new IllegalStateException("Refused forbidden record component rename: " + rr);
                    }
                    if (!isValidJavaIdent(rr.newName)) throw new IllegalArgumentException("Invalid target record component name: " + rr.newName);
                    recordCompRenames.put(rr.curRecord + "#" + rr.curName + "#" + rr.curDesc, rr);
                    recordCompByMethodKey.put(rr.curRecord + "#" + rr.curName + "#()" + rr.curDesc, rr);
                }
                default -> throw new IllegalArgumentException("Unknown plan tag: " + p[0]);
            }
        }
        long topCount = classRenames.values().stream().filter(cr -> !cr.curClass.contains("$")).count();
        long nestedCount = classRenames.size() - topCount;
        System.out.printf("Loaded plan: %d enums, %d top-level, %d nested, %d fields, %d methods, %d record comps (%s)%n",
            enumRenames.size(), topCount, nestedCount, fieldRenames.size(), methodRenames.size(), recordCompRenames.size(), apply ? "APPLY" : "DRY-RUN");

        // 1a. Validate no target collisions within field/record/method renames
        Map<String, String> targetFieldNames = new HashMap<>(); // curOwner + "#" + newName -> sourceKey
        for (var e : fieldRenames.entrySet()) {
            FieldRename fr = e.getValue();
            String tk = fr.curOwner + "#" + fr.newName;
            String prev = targetFieldNames.putIfAbsent(tk, "F:" + e.getKey());
            if (prev != null) throw new IllegalStateException("Target field collision on " + tk + ": " + prev + " vs F:" + e.getKey());
        }
        for (var e : recordCompRenames.entrySet()) {
            RecordCompRename rr = e.getValue();
            String tk = rr.curRecord + "#" + rr.newName;
            String prev = targetFieldNames.putIfAbsent(tk, "R:" + e.getKey());
            if (prev != null) throw new IllegalStateException("Target field/record-comp collision on " + tk + ": " + prev + " vs R:" + e.getKey());
        }
        Map<String, String> targetMethodSigs = new HashMap<>(); // curOwner + "#" + newName + "#" + paramDesc -> origMethodKey
        for (var e : recordCompRenames.entrySet()) {
            RecordCompRename rr = e.getValue();
            String tk = rr.curRecord + "#" + rr.newName + "#()";
            String origMk = rr.origRecord + "\t" + rr.origMethodName + "\t()" + rr.origDesc;
            String prev = targetMethodSigs.putIfAbsent(tk, origMk);
            if (prev != null && !prev.equals(origMk)) {
                throw new IllegalStateException("Target record accessor collision on " + tk + ": " + prev + " vs " + origMk);
            }
        }
        for (var e : methodRenames.entrySet()) {
            MethodRename mr = e.getValue();
            String tk = mr.curOwner + "#" + mr.newName + "#" + paramDescOf(mr.curDesc);
            String origMk = mr.origOwner + "\t" + mr.origName + "\t" + mr.origDesc;
            String prev = targetMethodSigs.putIfAbsent(tk, origMk);
            if (prev != null && !prev.equals(origMk)) {
                throw new IllegalStateException("Target method signature collision on " + tk + ": " + prev + " vs " + origMk);
            }
        }

        // 1b. Classify whole-package moves vs partial-package moves (using filenames only; never reads excluded files!)
        Path srcRoot = repo.resolve("src/main/java");
        Map<String, String> wholeMovedPkgs = new LinkedHashMap<>();
        Map<String, Set<String>> partialMovedPkgs = new LinkedHashMap<>();
        Set<String> newClassTargets = new HashSet<>();
        Map<String, List<ClassRename>> movesByOldPkg = new LinkedHashMap<>();
        for (ClassRename cr : classRenames.values()) {
            if (!newClassTargets.add(cr.newClass)) {
                throw new IllegalStateException("Duplicate target class in plan: " + cr.newClass);
            }
            if (cr.curClass.contains("$")) continue;
            if (!cr.curClass.equals(cr.newClass) && Files.exists(srcRoot.resolve(cr.newClass + ".java")) && !classRenames.containsKey(cr.newClass)) {
                throw new IllegalStateException("Target file already exists and is not moving: " + cr.newClass);
            }
            String oldPkg = parentPkg(cr.curClass);
            String newPkg = parentPkg(cr.newClass);
            if (!oldPkg.equals(newPkg)) {
                movesByOldPkg.computeIfAbsent(oldPkg, k -> new ArrayList<>()).add(cr);
            }
        }
        for (var e : movesByOldPkg.entrySet()) {
            String oldPkg = e.getKey();
            List<ClassRename> moved = e.getValue();
            Set<String> targetPkgs = new LinkedHashSet<>();
            for (ClassRename cr : moved) targetPkgs.add(parentPkg(cr.newClass));
            int totalJavaInOldPkg = 0;
            Path oldDir = srcRoot.resolve(oldPkg);
            if (Files.isDirectory(oldDir)) {
                try (var stream = Files.list(oldDir)) {
                    for (Path f : stream.toList()) {
                        if (Files.isRegularFile(f) && f.getFileName().toString().endsWith(".java")) {
                            totalJavaInOldPkg++;
                        }
                    }
                }
            }
            if (targetPkgs.size() == 1 && moved.size() == totalJavaInOldPkg) {
                wholeMovedPkgs.put(oldPkg, targetPkgs.iterator().next());
            } else {
                partialMovedPkgs.put(oldPkg, targetPkgs);
            }
        }
        if (!wholeMovedPkgs.isEmpty() || !partialMovedPkgs.isEmpty()) {
            System.out.printf("Package moves: %d whole-package moves, %d partial-package moves%n",
                wholeMovedPkgs.size(), partialMovedPkgs.size());
        }

        // 2. Collect non-integrity source files in src/main/java
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
        Set<String> matchedFieldKeys = new HashSet<>();
        Set<String> matchedMethodKeys = new HashSet<>();
        Set<String> matchedRecordCompKeys = new HashSet<>();
        List<String> warnings = new ArrayList<>();

        analyzeBatch(srcFiles, srcRoot, cp, enumRenames, classRenames, fieldRenames, methodRenames,
            recordCompRenames, recordCompByMethodKey, wholeMovedPkgs, partialMovedPkgs,
            editsByFile, matchedEnumKeys, matchedClassKeys, matchedFieldKeys, matchedMethodKeys, matchedRecordCompKeys,
            warnings, false);

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
            analyzeBatch(List.of(cf), srcRoot, cp, enumRenames, classRenames, fieldRenames, methodRenames,
                recordCompRenames, recordCompByMethodKey, wholeMovedPkgs, partialMovedPkgs,
                editsByFile, matchedEnumKeys, matchedClassKeys, matchedFieldKeys, matchedMethodKeys, matchedRecordCompKeys,
                warnings, true);
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
            Map<String, TextEdit> dedup = new LinkedHashMap<>();
            for (TextEdit te : list) {
                String key = te.start + ":" + te.end + ":" + (te.start == te.end ? te.kind : "R");
                TextEdit prev = dedup.putIfAbsent(key, te);
                if (prev != null && !prev.newText.equals(te.newText)) {
                    throw new IllegalStateException("Conflicting edits at " + f + " [" + te.start + "," + te.end + "]: " + prev + " vs " + te);
                }
            }
            List<TextEdit> sorted = new ArrayList<>(dedup.values());
            sorted.sort(Comparator.comparingInt(TextEdit::start).thenComparingInt(TextEdit::end));
            String content = Files.readString(f, StandardCharsets.UTF_8);
            int prevEnd = -1;
            for (TextEdit te : sorted) {
                boolean isInsert = te.start == te.end && te.oldText.isEmpty();
                if (te.start < prevEnd || te.start < 0 || te.end > content.length() || (!isInsert && te.start >= te.end)) {
                    throw new IllegalStateException("Invalid/overlapping edit in " + f + ": " + te + " (prevEnd=" + prevEnd + ")");
                }
                if (!isInsert) {
                    String actual = content.substring(te.start, te.end);
                    if (!actual.equals(te.oldText)) {
                        throw new IllegalStateException("Token mismatch in " + f + " at " + te.start + ": expected '" + te.oldText + "' got '" + actual + "' (" + te.kind + ")");
                    }
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
        List<String> unmatchedFields = new ArrayList<>();
        for (String k : fieldRenames.keySet()) if (!matchedFieldKeys.contains(k)) unmatchedFields.add(k);
        List<String> unmatchedMethods = new ArrayList<>();
        for (String k : methodRenames.keySet()) if (!matchedMethodKeys.contains(k)) unmatchedMethods.add(k);
        List<String> unmatchedRecComps = new ArrayList<>();
        for (String k : recordCompRenames.keySet()) if (!matchedRecordCompKeys.contains(k)) unmatchedRecComps.add(k);

        System.out.printf("RESULT: %d files to edit, %d total AST-verified token edits.%n", filesTouched, totalEdits);
        System.out.printf("Unmatched: enums=%d, classes=%d, fields=%d, methods=%d, recordComps=%d, warnings=%d%n",
            unmatchedEnums.size(), unmatchedClasses.size(), unmatchedFields.size(), unmatchedMethods.size(), unmatchedRecComps.size(), warnings.size());
        for (int i = 0; i < Math.min(20, unmatchedEnums.size()); i++) System.out.println("  UNMATCHED_ENUM: " + unmatchedEnums.get(i));
        for (int i = 0; i < Math.min(20, unmatchedClasses.size()); i++) System.out.println("  UNMATCHED_CLASS: " + unmatchedClasses.get(i));
        for (int i = 0; i < Math.min(20, unmatchedFields.size()); i++) System.out.println("  UNMATCHED_FIELD: " + unmatchedFields.get(i));
        for (int i = 0; i < Math.min(20, unmatchedMethods.size()); i++) System.out.println("  UNMATCHED_METHOD: " + unmatchedMethods.get(i));
        for (int i = 0; i < Math.min(20, unmatchedRecComps.size()); i++) System.out.println("  UNMATCHED_REC_COMP: " + unmatchedRecComps.get(i));
        for (int i = 0; i < Math.min(20, warnings.size()); i++) System.out.println("  WARN: " + warnings.get(i));

        // 5. Simulate disambig.tsv update
        Path mappingsPath = repo.resolve("mappings/disambig.tsv");
        List<String> updatedMappings = computeUpdatedDisambig(mappingsPath, enumRenames, classRenames, fieldRenames, methodRenames, recordCompRenames);
        System.out.printf("mappings/disambig.tsv: %d -> %d lines%n",
            Files.readAllLines(mappingsPath, StandardCharsets.UTF_8).size(), updatedMappings.size());

        if (!apply) {
            System.out.println("DRY-RUN COMPLETE (no files modified).");
            return;
        }
        if (!unmatchedEnums.isEmpty() || !unmatchedClasses.isEmpty() || !unmatchedFields.isEmpty()
            || !unmatchedMethods.isEmpty() || !unmatchedRecComps.isEmpty()) {
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

        // 7. Rename / move top-level class files via two-phase temp staging (safe for chains/swaps) & clean empty source dirs
        Set<Path> oldDirs = new LinkedHashSet<>();
        List<Map.Entry<Path, Path>> stagedMoves = new ArrayList<>();
        int stageIdx = 0;
        for (ClassRename cr : classRenames.values()) {
            if (cr.curClass.contains("$")) continue;
            Path oldPath = srcRoot.resolve(cr.curClass + ".java");
            Path newPath = srcRoot.resolve(cr.newClass + ".java");
            if (oldPath.equals(newPath)) continue;
            if (Files.exists(newPath) && !classRenames.containsKey(cr.newClass)) {
                throw new IllegalStateException("Target file already exists: " + newPath);
            }
            Path tmpPath = oldPath.resolveSibling(oldPath.getFileName() + ".astmove." + (stageIdx++));
            Files.move(oldPath, tmpPath);
            stagedMoves.add(Map.entry(tmpPath, newPath));
            if (!oldPath.getParent().equals(newPath.getParent())) {
                oldDirs.add(oldPath.getParent());
            }
        }
        for (var mv : stagedMoves) {
            Path newPath = mv.getValue();
            if (Files.exists(newPath)) {
                throw new IllegalStateException("Target file already exists after staging: " + newPath);
            }
            Files.createDirectories(newPath.getParent());
            Files.move(mv.getKey(), newPath);
        }
        for (Path dir : oldDirs) {
            Path cur = dir;
            while (cur != null && !cur.equals(srcRoot) && cur.startsWith(srcRoot) && Files.isDirectory(cur)) {
                try (var s = Files.list(cur)) {
                    if (s.findAny().isPresent()) break;
                }
                Files.delete(cur);
                cur = cur.getParent();
            }
        }

        // 8. Write updated mappings/disambig.tsv
        Files.write(mappingsPath, updatedMappings, StandardCharsets.UTF_8);
        System.out.println("APPLIED ALL EDITS, FILE MOVES, AND MAPPINGS CLEANLY.");
    }

    private static void analyzeBatch(
        List<Path> files,
        Path srcRoot,
        String cp,
        Map<String, EnumRename> enumRenames,
        Map<String, ClassRename> classRenames,
        Map<String, FieldRename> fieldRenames,
        Map<String, MethodRename> methodRenames,
        Map<String, RecordCompRename> recordCompRenames,
        Map<String, RecordCompRename> recordCompByMethodKey,
        Map<String, String> wholeMovedPkgs,
        Map<String, Set<String>> partialMovedPkgs,
        Map<Path, List<TextEdit>> editsByFile,
        Set<String> matchedEnumKeys,
        Set<String> matchedClassKeys,
        Set<String> matchedFieldKeys,
        Set<String> matchedMethodKeys,
        Set<String> matchedRecordCompKeys,
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
            Types types = task.getTypes();
            SourcePositions sp = trees.getSourcePositions();

            Set<String> renamedMethodOldNames = new HashSet<>();
            Set<String> renamedMethodOwners = new HashSet<>();
            for (MethodRename mr : methodRenames.values()) {
                renamedMethodOldNames.add(mr.curName);
                renamedMethodOwners.add(mr.curOwner);
            }
            for (RecordCompRename rr : recordCompByMethodKey.values()) {
                renamedMethodOldNames.add(rr.curName);
                renamedMethodOwners.add(rr.curRecord);
            }

            for (CompilationUnitTree cu : cus) {
                Path file = Path.of(cu.getSourceFile().toUri()).toAbsolutePath().normalize();
                String content = Files.readString(file, StandardCharsets.UTF_8);
                List<TextEdit> edits = editsByFile.computeIfAbsent(file, k -> new ArrayList<>());

                String fileOldPkg = cu.getPackageName() == null ? "" : cu.getPackageName().toString().replace('.', '/');
                String fileNewPkg = fileOldPkg;
                if (singleCheckMode) {
                    if (wholeMovedPkgs.containsKey(fileOldPkg)) {
                        fileNewPkg = wholeMovedPkgs.get(fileOldPkg);
                    }
                } else if (file.startsWith(srcRoot)) {
                    String rel = srcRoot.relativize(file).toString().replace('\\', '/');
                    if (rel.endsWith(".java")) {
                        ClassRename selfCr = classRenames.get(rel.substring(0, rel.length() - 5));
                        if (selfCr != null) {
                            fileNewPkg = parentPkg(selfCr.newClass);
                        }
                    }
                }
                if (!fileOldPkg.equals(fileNewPkg) && cu.getPackageName() != null) {
                    int ps = (int) sp.getStartPosition(cu, cu.getPackageName());
                    int pe = (int) sp.getEndPosition(cu, cu.getPackageName());
                    String oldPkgDot = fileOldPkg.replace('/', '.');
                    String newPkgDot = fileNewPkg.replace('/', '.');
                    if (ps >= 0 && pe > ps && content.substring(ps, pe).equals(oldPkgDot)) {
                        edits.add(new TextEdit(ps, pe, oldPkgDot, newPkgDot, singleCheckMode ? "check-pkg-decl" : "pkg-decl"));
                    } else {
                        throw new IllegalStateException("Cannot locate package name '" + oldPkgDot + "' in " + file);
                    }
                }

                // Track already-existing explicit imports (post-rewrite) and synthesized imports for split packages
                Set<String> existingImports = new HashSet<>();
                Set<String> extraImports = new TreeSet<>();

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
                                // import static pkg.Enum.OLD_CONST; or pkg.Outer.OldInner; or pkg.Owner.oldField/oldMethod; or pkg.OldClass.*;
                                EnumRename er = findEnumImport(left, right, enumRenames);
                                if (er != null) {
                                    addMemberSelectEdit(cu, sp, content, ms, er.curConst, er.newConst, "static-import-enum", edits);
                                } else if (!"*".equals(right) && (!fieldRenames.isEmpty() || !methodRenames.isEmpty())) {
                                    String staticMemberNew = findStaticMemberImportRename(left, right, fieldRenames, methodRenames, elements, types);
                                    if (staticMemberNew != null) {
                                        addMemberSelectEdit(cu, sp, content, ms, right, staticMemberNew, "static-import-member", edits);
                                    }
                                }
                                if (!"*".equals(right)) {
                                    ClassRename directStaticCr = lookupClassByDotted(full, classRenames);
                                    if (directStaticCr != null) {
                                        addClassMoveOrRenameImportEdits(cu, sp, content, ms, directStaticCr, "static-import-pkg", "static-import-class", edits);
                                    }
                                }
                                ExpressionTree expr = ms.getExpression();
                                while (expr instanceof MemberSelectTree ems) {
                                    ClassRename cr = lookupClassByDotted(ems.toString(), classRenames);
                                    if (cr != null) {
                                        addClassMoveOrRenameImportEdits(cu, sp, content, ems, cr, "static-import-pkg", "static-import-class", edits);
                                    }
                                    expr = ems.getExpression();
                                }
                            } else if ("*".equals(right)) {
                                // import pkg.OldClass.*; OR import pkg.Outer.OldInner.*; OR import oldPkg.*;
                                String leftInt = left.replace('.', '/');
                                ClassRename cr = lookupClassByDotted(left, classRenames);
                                if (cr != null && ms.getExpression() instanceof MemberSelectTree lhsMs) {
                                    addClassMoveOrRenameImportEdits(cu, sp, content, lhsMs, cr, "import-wildcard-class-pkg", "import-wildcard-class", edits);
                                    // Also check any enclosing outer classes in the chain
                                    ExpressionTree expr = lhsMs.getExpression();
                                    while (expr instanceof MemberSelectTree ems) {
                                        ClassRename ocr = lookupClassByDotted(ems.toString(), classRenames);
                                        if (ocr != null) {
                                            addClassMoveOrRenameImportEdits(cu, sp, content, ems, ocr, "import-outer-pkg", "import-outer-class", edits);
                                        }
                                        expr = ems.getExpression();
                                    }
                                } else if (wholeMovedPkgs.containsKey(leftInt)) {
                                    String targetPkg = wholeMovedPkgs.get(leftInt);
                                    replaceExprExact(cu, sp, content, ms.getExpression(), left, targetPkg.replace('/', '.'), "import-wildcard-pkg", edits);
                                    existingImports.add(targetPkg.replace('/', '.') + ".*");
                                } else if (partialMovedPkgs.containsKey(leftInt)) {
                                    existingImports.add(left + ".*");
                                    for (String tp : partialMovedPkgs.get(leftInt)) {
                                        String impStr = tp.replace('/', '.') + ".*";
                                        if (!tp.equals(fileNewPkg)) {
                                            extraImports.add("import " + impStr + ";");
                                        }
                                    }
                                } else {
                                    // Could be import pkg.OldClass.Inner.*;
                                    ExpressionTree expr = ms.getExpression();
                                    while (expr instanceof MemberSelectTree ems) {
                                        ClassRename ocr = lookupClassByDotted(ems.toString(), classRenames);
                                        if (ocr != null) {
                                            addClassMoveOrRenameImportEdits(cu, sp, content, ems, ocr, "import-outer-pkg", "import-outer-class", edits);
                                        }
                                        expr = ems.getExpression();
                                    }
                                    existingImports.add(full);
                                }
                            } else {
                                // import pkg.OldClass; or import pkg.OldClass.OldInner; or import pkg.OldClass.OldInner.Sub;
                                ClassRename directCr = lookupClassByDotted(full, classRenames);
                                boolean rewritten = false;
                                if (directCr != null) {
                                    addClassMoveOrRenameImportEdits(cu, sp, content, ms, directCr, "import-pkg", "import-class", edits);
                                    if (!directCr.curClass.contains("$")) {
                                        existingImports.add(directCr.newClass.replace('/', '.'));
                                    }
                                    rewritten = true;
                                }
                                ExpressionTree expr = ms.getExpression();
                                while (expr instanceof MemberSelectTree ems) {
                                    ClassRename ocr = lookupClassByDotted(ems.toString(), classRenames);
                                    if (ocr != null) {
                                        addClassMoveOrRenameImportEdits(cu, sp, content, ems, ocr, "import-outer-pkg", "import-outer-class", edits);
                                        rewritten = true;
                                    }
                                    expr = ems.getExpression();
                                }
                                if (!rewritten) existingImports.add(full);
                            }
                        }
                    }
                }

                final String finalFileOldPkg = fileOldPkg;
                final String finalFileNewPkg = fileNewPkg;

                new TreePathScanner<Void, Void>() {
                    @Override
                    public Void visitImport(ImportTree node, Void p) {
                        return null; // already handled above
                    }

                    @Override
                    public Void visitPackage(PackageTree node, Void p) {
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
                                if (!oldSimple.equals(newSimple)) {
                                    int s = (int) sp.getStartPosition(cu, node);
                                    int e = (int) sp.getEndPosition(cu, node);
                                    int headerEnd = content.indexOf('{', s);
                                    if (headerEnd < 0 || headerEnd > e) headerEnd = e;
                                    int tokStart = findClassDeclName(content, s, headerEnd, oldSimple);
                                    if (tokStart < 0) throw new IllegalStateException("Cannot locate class decl name " + oldSimple + " in " + file);
                                    edits.add(new TextEdit(tokStart, tokStart + oldSimple.length(), oldSimple, newSimple, "class-decl"));
                                }
                            }
                            // Guard against silent widening/overload-resolution shifts: refuse merging two
                            // previously distinct method names with the same arity into one overloaded name.
                            if (renamedMethodOwners.contains(internal)) {
                                Map<String, String[]> byFinalNameArity = new HashMap<>(); // finalName#arity -> [oldName, origKey]
                                for (Element mem : te.getEnclosedElements()) {
                                    if (!(mem instanceof ExecutableElement mee) || mee.getKind() != ElementKind.METHOD) continue;
                                    String oldM = mee.getSimpleName().toString();
                                    String mDesc = jvmMethodDesc(mee, types, elements);
                                    String mKey = internal + "#" + oldM + "#" + mDesc;
                                    MethodRename mr = methodRenames.get(mKey);
                                    RecordCompRename rcr = recordCompByMethodKey.get(mKey);
                                    String finalM = mr != null ? mr.newName : (rcr != null ? rcr.newName : oldM);
                                    String origKey = mr != null
                                        ? (mr.origOwner + "\t" + mr.origName + "\t" + mr.origDesc)
                                        : (rcr != null ? (rcr.origRecord + "\t" + rcr.origMethodName + "\t()" + rcr.origDesc) : mKey);
                                    String sig = finalM + "#" + mee.getParameters().size();
                                    String[] prev = byFinalNameArity.putIfAbsent(sig, new String[]{oldM, origKey});
                                    if (prev != null && !prev[0].equals(oldM) && !prev[1].equals(origKey)) {
                                        throw new IllegalStateException(
                                            "Refused arity-colliding overload merge on " + internal
                                                + " (risks silent widening/overload-resolution shift): '" + prev[0] + "' and '" + oldM
                                                + "' both map to '" + finalM + "' with arity " + mee.getParameters().size());
                                    }
                                }
                            }
                        }
                        return super.visitClass(node, p);
                    }

                    @Override
                    public Void visitMethod(MethodTree node, Void p) {
                        Element el = trees.getElement(getCurrentPath());
                        if (node.getName().contentEquals("<init>")) {
                            if (el instanceof ExecutableElement ee && ee.getEnclosingElement() instanceof TypeElement te) {
                                String internal = toInternalName(te, elements);
                                ClassRename cr = classRenames.get(internal);
                                if (cr != null) {
                                    String oldSimple = simpleName(cr.curClass);
                                    String newSimple = simpleName(cr.newClass);
                                    if (!oldSimple.equals(newSimple)) {
                                        int s = (int) sp.getStartPosition(cu, node);
                                        int e = (int) sp.getEndPosition(cu, node);
                                        if (s >= 0 && e > s) {
                                            int searchStart = s;
                                            ModifiersTree mods = node.getModifiers();
                                            if (mods != null && !mods.getAnnotations().isEmpty()) {
                                                int modEnd = (int) sp.getEndPosition(cu, mods);
                                                if (modEnd > searchStart && modEnd < e) searchStart = modEnd;
                                            }
                                            int brace = content.indexOf('{', searchStart);
                                            int paren = content.indexOf('(', searchStart);
                                            int limit = (paren > searchStart && (brace < 0 || paren < brace)) ? paren : brace;
                                            if (limit > searchStart && limit <= e) {
                                                int tokStart = findLastIdentToken(content, searchStart, limit, oldSimple);
                                                if (tokStart >= 0) {
                                                    edits.add(new TextEdit(tokStart, tokStart + oldSimple.length(), oldSimple, newSimple, "ctor-decl"));
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        } else if ((!methodRenames.isEmpty() || !recordCompByMethodKey.isEmpty())
                            && el instanceof ExecutableElement ee && ee.getEnclosingElement() instanceof TypeElement te) {
                            String ownerInt = toInternalName(te, elements);
                            String mName = ee.getSimpleName().toString();
                            String mDesc = jvmMethodDesc(ee, types, elements);
                            String mKey = ownerInt + "#" + mName + "#" + mDesc;
                            MethodRename mr = methodRenames.get(mKey);
                            RecordCompRename rcr = recordCompByMethodKey.get(mKey);
                            String targetNewName = null;
                            if (mr != null) {
                                matchedMethodKeys.add(mKey);
                                targetNewName = mr.newName;
                            } else if (rcr != null) {
                                targetNewName = rcr.newName;
                            }
                            if (!ee.getModifiers().contains(Modifier.STATIC) && !ee.getModifiers().contains(Modifier.PRIVATE)) {
                                if (targetNewName != null) {
                                    checkRenamedMethodOverrides(ee, te, targetNewName, methodRenames, recordCompByMethodKey, types, elements);
                                } else if (renamedMethodOldNames.contains(mName)) {
                                    checkUnrenamedMethodDoesNotOverrideRenamed(ee, te, renamedMethodOwners, methodRenames, recordCompByMethodKey, types, elements);
                                }
                            }
                            if (targetNewName != null && !mName.equals(targetNewName)) {
                                int s = (int) sp.getStartPosition(cu, node);
                                int e = (int) sp.getEndPosition(cu, node);
                                if (s >= 0 && e > s) {
                                    int searchStart = s;
                                    Tree retTree = node.getReturnType();
                                    if (retTree != null) {
                                        int rtEnd = (int) sp.getEndPosition(cu, retTree);
                                        if (rtEnd > searchStart && rtEnd < e) searchStart = rtEnd;
                                    } else {
                                        ModifiersTree mods = node.getModifiers();
                                        if (mods != null) {
                                            int modEnd = (int) sp.getEndPosition(cu, mods);
                                            if (modEnd > searchStart && modEnd < e) searchStart = modEnd;
                                        }
                                    }
                                    int paren = content.indexOf('(', searchStart);
                                    if (paren <= searchStart || paren > e) {
                                        throw new IllegalStateException("Cannot locate '(' for method decl " + mKey + " in " + file);
                                    }
                                    int tokStart = findLastIdentToken(content, searchStart, paren, mName);
                                    if (tokStart < 0) {
                                        throw new IllegalStateException("Cannot locate method decl name " + mKey + " in " + file);
                                    }
                                    edits.add(new TextEdit(tokStart, tokStart + mName.length(), mName, targetNewName,
                                        mr != null ? "method-decl" : "record-accessor-decl"));
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
                        } else if ((!fieldRenames.isEmpty() || !recordCompRenames.isEmpty())
                            && el instanceof VariableElement ve && ve.getKind() == ElementKind.FIELD
                            && ve.getEnclosingElement() instanceof TypeElement te) {
                            String ownerInt = toInternalName(te, elements);
                            String fName = ve.getSimpleName().toString();
                            String fDesc = jvmTypeDesc(ve.asType(), types, elements);
                            String fKey = ownerInt + "#" + fName + "#" + fDesc;
                            FieldRename fr = fieldRenames.get(fKey);
                            RecordCompRename rcr = recordCompRenames.get(fKey);
                            if (fr != null && te.getKind() == ElementKind.RECORD && !ve.getModifiers().contains(Modifier.STATIC)) {
                                throw new IllegalStateException("Use tag R (not F) for record component: " + fKey);
                            }
                            if (rcr != null && (te.getKind() != ElementKind.RECORD || ve.getModifiers().contains(Modifier.STATIC))) {
                                throw new IllegalStateException("Tag R used on non-record-component field: " + fKey);
                            }
                            if (fr != null && ve.getModifiers().contains(Modifier.PUBLIC)
                                && isSubclassOf(te, "autismclient/util/macro/MacroAction", types, elements)) {
                                throw new IllegalStateException("Refused MacroAction public field rename (reflection-bound): " + fKey);
                            }
                            String targetNewName = null;
                            if (fr != null) {
                                matchedFieldKeys.add(fKey);
                                targetNewName = fr.newName;
                            } else if (rcr != null) {
                                matchedRecordCompKeys.add(fKey);
                                targetNewName = rcr.newName;
                            }
                            if (targetNewName != null && !fName.equals(targetNewName)) {
                                int s = (int) sp.getStartPosition(cu, node);
                                int e = (int) sp.getEndPosition(cu, node);
                                if (s >= 0 && e > s) {
                                    int searchStart = s;
                                    if (node.getType() != null) {
                                        int teEnd = (int) sp.getEndPosition(cu, node.getType());
                                        if (teEnd > searchStart && teEnd < e) searchStart = teEnd;
                                    }
                                    int searchEnd = e;
                                    if (node.getInitializer() != null) {
                                        int initStart = (int) sp.getStartPosition(cu, node.getInitializer());
                                        if (initStart > searchStart && initStart < searchEnd) searchEnd = initStart;
                                    }
                                    int tokStart = findFirstIdentToken(content, searchStart, searchEnd, fName);
                                    if (tokStart < 0) {
                                        throw new IllegalStateException("Cannot locate field/record-comp decl name " + fKey + " in " + file);
                                    }
                                    edits.add(new TextEdit(tokStart, tokStart + fName.length(), fName, targetNewName,
                                        rcr != null ? "record-comp-decl" : "field-decl"));
                                }
                            }
                        } else if (!recordCompRenames.isEmpty()
                            && el instanceof VariableElement ve && ve.getKind() == ElementKind.PARAMETER
                            && ve.getEnclosingElement() instanceof ExecutableElement ee
                            && ee.getSimpleName().contentEquals("<init>")
                            && ee.getEnclosingElement() instanceof TypeElement te
                            && te.getKind() == ElementKind.RECORD
                            && isCanonicalRecordConstructor(ee, te, types, elements)) {
                            int s = (int) sp.getStartPosition(cu, node);
                            int e = (int) sp.getEndPosition(cu, node);
                            if (s >= 0 && e > s) {
                                String ownerInt = toInternalName(te, elements);
                                String pName = ve.getSimpleName().toString();
                                String pDesc = jvmTypeDesc(ve.asType(), types, elements);
                                RecordCompRename rcr = recordCompRenames.get(ownerInt + "#" + pName + "#" + pDesc);
                                if (rcr != null && !pName.equals(rcr.newName)) {
                                    int searchStart = s;
                                    if (node.getType() != null) {
                                        int teEnd = (int) sp.getEndPosition(cu, node.getType());
                                        if (teEnd > searchStart && teEnd < e) searchStart = teEnd;
                                    }
                                    int tokStart = findFirstIdentToken(content, searchStart, e, pName);
                                    if (tokStart < 0) {
                                        throw new IllegalStateException("Cannot locate canonical record ctor param " + pName + " in " + file);
                                    }
                                    edits.add(new TextEdit(tokStart, tokStart + pName.length(), pName, rcr.newName, "record-ctor-param-decl"));
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
                        } else if ((!fieldRenames.isEmpty() || !recordCompRenames.isEmpty())
                            && el instanceof VariableElement ve && ve.getKind() == ElementKind.FIELD
                            && ve.getEnclosingElement() instanceof TypeElement te) {
                            String fKey = toInternalName(te, elements) + "#" + ve.getSimpleName().toString() + "#" + jvmTypeDesc(ve.asType(), types, elements);
                            FieldRename fr = fieldRenames.get(fKey);
                            if (fr != null && !fr.curName.equals(fr.newName)) {
                                addMemberSelectEdit(cu, sp, content, node, fr.curName, fr.newName, "field-select", edits);
                            } else {
                                RecordCompRename rcr = recordCompRenames.get(fKey);
                                if (rcr != null && !rcr.curName.equals(rcr.newName)) {
                                    addMemberSelectEdit(cu, sp, content, node, rcr.curName, rcr.newName, "record-comp-field-select", edits);
                                }
                            }
                        } else if ((!methodRenames.isEmpty() || !recordCompByMethodKey.isEmpty())
                            && el instanceof ExecutableElement ee && ee.getEnclosingElement() instanceof TypeElement te) {
                            String mKey = toInternalName(te, elements) + "#" + ee.getSimpleName().toString() + "#" + jvmMethodDesc(ee, types, elements);
                            MethodRename mr = methodRenames.get(mKey);
                            if (mr != null && !mr.curName.equals(mr.newName)) {
                                addMemberSelectEdit(cu, sp, content, node, mr.curName, mr.newName, "method-select", edits);
                            } else {
                                RecordCompRename rcr = recordCompByMethodKey.get(mKey);
                                if (rcr != null && !rcr.curName.equals(rcr.newName)) {
                                    addMemberSelectEdit(cu, sp, content, node, rcr.curName, rcr.newName, "record-accessor-select", edits);
                                }
                            }
                        } else if (el instanceof TypeElement te) {
                            String internal = toInternalName(te, elements);
                            ClassRename cr = classRenames.get(internal);
                            if (cr != null) {
                                if (!cr.curClass.contains("$")) {
                                    Element exprEl = trees.getElement(new TreePath(getCurrentPath(), node.getExpression()));
                                    if (exprEl instanceof PackageElement && !parentPkg(cr.curClass).equals(parentPkg(cr.newClass))) {
                                        replaceExprExact(cu, sp, content, node.getExpression(),
                                            parentPkg(cr.curClass).replace('/', '.'),
                                            parentPkg(cr.newClass).replace('/', '.'),
                                            "fqn-pkg", edits);
                                    }
                                }
                                if (!simpleName(cr.curClass).equals(simpleName(cr.newClass))) {
                                    addMemberSelectEdit(cu, sp, content, node, simpleName(cr.curClass), simpleName(cr.newClass), "class-select", edits);
                                }
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
                        } else if ((!fieldRenames.isEmpty() || !recordCompRenames.isEmpty())
                            && el instanceof VariableElement ve && ve.getKind() == ElementKind.FIELD
                            && ve.getEnclosingElement() instanceof TypeElement te) {
                            String fKey = toInternalName(te, elements) + "#" + ve.getSimpleName().toString() + "#" + jvmTypeDesc(ve.asType(), types, elements);
                            FieldRename fr = fieldRenames.get(fKey);
                            RecordCompRename rcr = fr == null ? recordCompRenames.get(fKey) : null;
                            String oldName = fr != null ? fr.curName : (rcr != null ? rcr.curName : null);
                            String newName = fr != null ? fr.newName : (rcr != null ? rcr.newName : null);
                            if (oldName != null && !oldName.equals(newName)) {
                                int s = (int) sp.getStartPosition(cu, node);
                                int e = (int) sp.getEndPosition(cu, node);
                                if (s >= 0 && e > s && content.substring(s, e).equals(oldName)) {
                                    edits.add(new TextEdit(s, e, oldName, newName, fr != null ? "field-ident" : "record-comp-field-ident"));
                                }
                            }
                        } else if (!recordCompRenames.isEmpty()
                            && el instanceof VariableElement ve && ve.getKind() == ElementKind.PARAMETER
                            && ve.getEnclosingElement() instanceof ExecutableElement ee
                            && ee.getSimpleName().contentEquals("<init>")
                            && ee.getEnclosingElement() instanceof TypeElement te
                            && te.getKind() == ElementKind.RECORD
                            && isCanonicalRecordConstructor(ee, te, types, elements)) {
                            String fKey = toInternalName(te, elements) + "#" + ve.getSimpleName().toString() + "#" + jvmTypeDesc(ve.asType(), types, elements);
                            RecordCompRename rcr = recordCompRenames.get(fKey);
                            if (rcr != null && !rcr.curName.equals(rcr.newName)) {
                                int s = (int) sp.getStartPosition(cu, node);
                                int e = (int) sp.getEndPosition(cu, node);
                                if (s >= 0 && e > s && content.substring(s, e).equals(rcr.curName)) {
                                    edits.add(new TextEdit(s, e, rcr.curName, rcr.newName, "record-ctor-param-ident"));
                                }
                            }
                        } else if ((!methodRenames.isEmpty() || !recordCompByMethodKey.isEmpty())
                            && el instanceof ExecutableElement ee && ee.getEnclosingElement() instanceof TypeElement te) {
                            String mKey = toInternalName(te, elements) + "#" + ee.getSimpleName().toString() + "#" + jvmMethodDesc(ee, types, elements);
                            MethodRename mr = methodRenames.get(mKey);
                            RecordCompRename rcr = mr == null ? recordCompByMethodKey.get(mKey) : null;
                            String oldName = mr != null ? mr.curName : (rcr != null ? rcr.curName : null);
                            String newName = mr != null ? mr.newName : (rcr != null ? rcr.newName : null);
                            if (oldName != null && !oldName.equals(newName)) {
                                int s = (int) sp.getStartPosition(cu, node);
                                int e = (int) sp.getEndPosition(cu, node);
                                if (s >= 0 && e > s && content.substring(s, e).equals(oldName)) {
                                    edits.add(new TextEdit(s, e, oldName, newName, mr != null ? "method-ident" : "record-accessor-ident"));
                                }
                            }
                        } else if (el instanceof TypeElement te) {
                            String internal = toInternalName(te, elements);
                            ClassRename cr = classRenames.get(internal);
                            if (cr != null) {
                                String oldSimple = simpleName(cr.curClass);
                                String newSimple = simpleName(cr.newClass);
                                if (!oldSimple.equals(newSimple)) {
                                    int s = (int) sp.getStartPosition(cu, node);
                                    int e = (int) sp.getEndPosition(cu, node);
                                    if (s >= 0 && e > s && content.substring(s, e).equals(oldSimple)) {
                                        edits.add(new TextEdit(s, e, oldSimple, newSimple, "class-ident"));
                                    }
                                }
                            }
                            // If this file and top-level type were previously same-package and are now split across packages, synthesize explicit import!
                            if (te.getNestingKind() == NestingKind.TOP_LEVEL) {
                                String topInt = toInternalName(te, elements);
                                String topOldPkg = parentPkg(topInt);
                                ClassRename topCr = classRenames.get(topInt);
                                String topNewInt = topCr != null ? topCr.newClass : topInt;
                                String topNewPkg = parentPkg(topNewInt);
                                if (!finalFileOldPkg.isEmpty() && finalFileOldPkg.equals(topOldPkg) && !finalFileNewPkg.equals(topNewPkg)) {
                                    String fqNew = topNewInt.replace('/', '.');
                                    if (!existingImports.contains(fqNew) && !existingImports.contains(topNewPkg.replace('/', '.') + ".*")) {
                                        extraImports.add("import " + fqNew + ";");
                                    }
                                }
                            }
                        }
                        return super.visitIdentifier(node, p);
                    }

                    @Override
                    public Void visitMemberReference(MemberReferenceTree node, Void p) {
                        if (node.getMode() == MemberReferenceTree.ReferenceMode.INVOKE
                            && (!methodRenames.isEmpty() || !recordCompByMethodKey.isEmpty())) {
                            Element el = trees.getElement(getCurrentPath());
                            if (el instanceof ExecutableElement ee && ee.getEnclosingElement() instanceof TypeElement te) {
                                String mKey = toInternalName(te, elements) + "#" + ee.getSimpleName().toString() + "#" + jvmMethodDesc(ee, types, elements);
                                MethodRename mr = methodRenames.get(mKey);
                                RecordCompRename rcr = mr == null ? recordCompByMethodKey.get(mKey) : null;
                                String oldName = mr != null ? mr.curName : (rcr != null ? rcr.curName : null);
                                String newName = mr != null ? mr.newName : (rcr != null ? rcr.newName : null);
                                if (oldName != null && !oldName.equals(newName)) {
                                    int qualEnd = (int) sp.getEndPosition(cu, node.getQualifierExpression());
                                    int nodeEnd = (int) sp.getEndPosition(cu, node);
                                    if (qualEnd >= 0 && nodeEnd > qualEnd) {
                                        int tokStart = findLastIdentToken(content, qualEnd, nodeEnd, oldName);
                                        if (tokStart < 0) {
                                            throw new IllegalStateException("Cannot locate method reference token '" + oldName + "' in " + file);
                                        }
                                        edits.add(new TextEdit(tokStart, tokStart + oldName.length(), oldName, newName, "method-ref"));
                                    }
                                }
                            }
                        }
                        return super.visitMemberReference(node, p);
                    }
                }.scan(cu, null);

                // Synthesize any extra imports required by partial-package splits
                if (!extraImports.isEmpty()) {
                    boolean hadImports = cu.getImports() != null && !cu.getImports().isEmpty();
                    int insertPos = findImportInsertOffset(cu, sp, content);
                    StringBuilder ins = new StringBuilder();
                    boolean needsLeadingNl = insertPos > 0 && content.charAt(insertPos - 1) != '\n';
                    if (needsLeadingNl) ins.append('\n');
                    if (!hadImports && cu.getPackage() != null) ins.append('\n');
                    for (String impLine : extraImports) {
                        ins.append(impLine).append('\n');
                    }
                    edits.add(new TextEdit(insertPos, insertPos, "", ins.toString(), "insert-imports"));
                }
            }
        }
    }

    private static int findImportInsertOffset(CompilationUnitTree cu, SourcePositions sp, String content) {
        List<? extends ImportTree> imps = cu.getImports();
        int anchorEnd = -1;
        if (imps != null && !imps.isEmpty()) {
            anchorEnd = (int) sp.getEndPosition(cu, imps.get(imps.size() - 1));
        } else if (cu.getPackage() != null) {
            anchorEnd = (int) sp.getEndPosition(cu, cu.getPackage());
        }
        if (anchorEnd < 0) return 0;
        // Advance past trailing '\r' / '\n' on the anchor line
        int p = anchorEnd;
        if (p < content.length() && content.charAt(p) == '\r') p++;
        if (p < content.length() && content.charAt(p) == '\n') p++;
        return p;
    }

    private static void replaceExprExact(
        CompilationUnitTree cu, SourcePositions sp, String content,
        ExpressionTree expr, String oldText, String newText, String kind, List<TextEdit> edits
    ) {
        int s = (int) sp.getStartPosition(cu, expr);
        int e = (int) sp.getEndPosition(cu, expr);
        if (s < 0 || e <= s) return;
        String actual = content.substring(s, e);
        if (!actual.equals(oldText)) {
            throw new IllegalStateException("Expected '" + oldText + "' at [" + s + "," + e + "] got '" + actual + "' (" + kind + ")");
        }
        edits.add(new TextEdit(s, e, oldText, newText, kind));
    }

    private static void addClassMoveOrRenameImportEdits(
        CompilationUnitTree cu, SourcePositions sp, String content,
        MemberSelectTree ms, ClassRename cr, String pkgKind, String clsKind, List<TextEdit> edits
    ) {
        if (!cr.curClass.contains("$")) {
            String oldPkg = parentPkg(cr.curClass);
            String newPkg = parentPkg(cr.newClass);
            if (!oldPkg.equals(newPkg)) {
                replaceExprExact(cu, sp, content, ms.getExpression(), oldPkg.replace('/', '.'), newPkg.replace('/', '.'), pkgKind, edits);
            }
        }
        if (!simpleName(cr.curClass).equals(simpleName(cr.newClass))) {
            addMemberSelectEdit(cu, sp, content, ms, simpleName(cr.curClass), simpleName(cr.newClass), clsKind, edits);
        }
    }

    private static ClassRename lookupClassByDotted(String dotted, Map<String, ClassRename> classRenames) {
        String candidate = dotted.replace('.', '/');
        ClassRename hit = classRenames.get(candidate);
        if (hit != null) return hit;
        String cur = candidate;
        for (int i = 0; i < 4; i++) {
            int slash = cur.lastIndexOf('/');
            if (slash < 0) break;
            cur = cur.substring(0, slash) + "$" + cur.substring(slash + 1);
            hit = classRenames.get(cur);
            if (hit != null) return hit;
        }
        return null;
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
        int i = Math.max(internal.lastIndexOf('/'), internal.lastIndexOf('$'));
        return i >= 0 ? internal.substring(i + 1) : internal;
    }

    private static String outerOwner(String internal) {
        int i = internal.lastIndexOf('$');
        return i >= 0 ? internal.substring(0, i) : "";
    }

    private static String parentPkg(String internal) {
        int i = internal.lastIndexOf('/');
        return i >= 0 ? internal.substring(0, i) : "";
    }

    private static String remapClassPrefix(String internal, Map<String, ClassRename> classRenames) {
        if (classRenames.isEmpty()) return internal;
        if (!internal.contains("$")) {
            ClassRename exact = classRenames.get(internal);
            return exact != null ? exact.newClass : internal;
        }
        String[] parts = internal.split("\\$", -1);
        StringBuilder oldPrefix = new StringBuilder(parts[0]);
        ClassRename topCr = classRenames.get(parts[0]);
        StringBuilder newOut = new StringBuilder(topCr != null ? topCr.newClass : parts[0]);
        for (int i = 1; i < parts.length; i++) {
            oldPrefix.append('$').append(parts[i]);
            ClassRename nestedCr = classRenames.get(oldPrefix.toString());
            if (nestedCr != null) {
                newOut.append('$').append(simpleName(nestedCr.newClass));
            } else {
                newOut.append('$').append(parts[i]);
            }
        }
        return newOut.toString();
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

    private static String paramDescOf(String methodDesc) {
        int close = methodDesc.indexOf(')');
        if (!methodDesc.startsWith("(") || close < 1) {
            throw new IllegalArgumentException("Invalid method descriptor: " + methodDesc);
        }
        return methodDesc.substring(0, close + 1);
    }

    private static String jvmTypeDesc(TypeMirror tm, Types types, Elements elements) {
        TypeMirror erased = types.erasure(tm);
        return switch (erased.getKind()) {
            case BOOLEAN -> "Z";
            case BYTE -> "B";
            case CHAR -> "C";
            case SHORT -> "S";
            case INT -> "I";
            case LONG -> "J";
            case FLOAT -> "F";
            case DOUBLE -> "D";
            case VOID -> "V";
            case ARRAY -> "[" + jvmTypeDesc(((ArrayType) erased).getComponentType(), types, elements);
            case DECLARED -> "L" + toInternalName((TypeElement) ((DeclaredType) erased).asElement(), elements) + ";";
            case INTERSECTION -> jvmTypeDesc(((IntersectionType) erased).getBounds().get(0), types, elements);
            default -> throw new IllegalStateException("Unsupported erased type kind: " + erased.getKind() + " (" + tm + ")");
        };
    }

    private static String jvmMethodDesc(ExecutableElement ee, Types types, Elements elements) {
        StringBuilder sb = new StringBuilder("(");
        for (VariableElement param : ee.getParameters()) {
            sb.append(jvmTypeDesc(param.asType(), types, elements));
        }
        sb.append(')').append(jvmTypeDesc(ee.getReturnType(), types, elements));
        return sb.toString();
    }

    private static boolean isSubclassOf(TypeElement te, String targetInternal, Types types, Elements elements) {
        TypeElement cur = te;
        while (cur != null) {
            if (toInternalName(cur, elements).equals(targetInternal)) return true;
            TypeMirror sup = cur.getSuperclass();
            if (sup == null || sup.getKind() != TypeKind.DECLARED) break;
            Element se = ((DeclaredType) sup).asElement();
            cur = se instanceof TypeElement ste ? ste : null;
        }
        return false;
    }

    private static boolean isCanonicalRecordConstructor(ExecutableElement ee, TypeElement recordTe, Types types, Elements elements) {
        List<? extends RecordComponentElement> comps = recordTe.getRecordComponents();
        List<? extends VariableElement> params = ee.getParameters();
        if (comps.size() != params.size()) return false;
        for (int i = 0; i < comps.size(); i++) {
            String cd = jvmTypeDesc(comps.get(i).asType(), types, elements);
            String pd = jvmTypeDesc(params.get(i).asType(), types, elements);
            if (!cd.equals(pd)) return false;
        }
        return true;
    }

    private static void checkRenamedMethodOverrides(
        ExecutableElement ee,
        TypeElement te,
        String targetNewName,
        Map<String, MethodRename> methodRenames,
        Map<String, RecordCompRename> recordCompByMethodKey,
        Types types,
        Elements elements
    ) {
        Deque<TypeMirror> q = new ArrayDeque<>(types.directSupertypes(te.asType()));
        Set<String> visited = new HashSet<>();
        while (!q.isEmpty()) {
            TypeMirror stm = q.poll();
            if (stm.getKind() != TypeKind.DECLARED) continue;
            Element se = ((DeclaredType) stm).asElement();
            if (!(se instanceof TypeElement ste)) continue;
            String supInt = toInternalName(ste, elements);
            if (!visited.add(supInt)) continue;
            for (Element mem : ste.getEnclosedElements()) {
                if (mem instanceof ExecutableElement supEe && supEe.getKind() == ElementKind.METHOD
                    && supEe.getSimpleName().equals(ee.getSimpleName())
                    && elements.overrides(ee, supEe, te)) {
                    String supKey = supInt + "#" + supEe.getSimpleName() + "#" + jvmMethodDesc(supEe, types, elements);
                    MethodRename supMr = methodRenames.get(supKey);
                    RecordCompRename supRcr = recordCompByMethodKey.get(supKey);
                    String supTarget = supMr != null ? supMr.newName : (supRcr != null ? supRcr.newName : null);
                    if (!targetNewName.equals(supTarget)) {
                        throw new IllegalStateException("Refused override-breaking rename: " + toInternalName(te, elements) + "."
                            + ee.getSimpleName() + " -> " + targetNewName + " overrides " + supKey + " (plan target=" + supTarget + ")");
                    }
                }
            }
            q.addAll(types.directSupertypes(stm));
        }
    }

    private static void checkUnrenamedMethodDoesNotOverrideRenamed(
        ExecutableElement ee,
        TypeElement te,
        Set<String> renamedMethodOwners,
        Map<String, MethodRename> methodRenames,
        Map<String, RecordCompRename> recordCompByMethodKey,
        Types types,
        Elements elements
    ) {
        Deque<TypeMirror> q = new ArrayDeque<>(types.directSupertypes(te.asType()));
        Set<String> visited = new HashSet<>();
        while (!q.isEmpty()) {
            TypeMirror stm = q.poll();
            if (stm.getKind() != TypeKind.DECLARED) continue;
            Element se = ((DeclaredType) stm).asElement();
            if (!(se instanceof TypeElement ste)) continue;
            String supInt = toInternalName(ste, elements);
            if (!visited.add(supInt)) continue;
            if (renamedMethodOwners.contains(supInt)) {
                for (Element mem : ste.getEnclosedElements()) {
                    if (mem instanceof ExecutableElement supEe && supEe.getKind() == ElementKind.METHOD
                        && supEe.getSimpleName().equals(ee.getSimpleName())
                        && elements.overrides(ee, supEe, te)) {
                        String supKey = supInt + "#" + supEe.getSimpleName() + "#" + jvmMethodDesc(supEe, types, elements);
                        if (methodRenames.containsKey(supKey) || recordCompByMethodKey.containsKey(supKey)) {
                            throw new IllegalStateException("Missing subclass override in plan: " + toInternalName(te, elements)
                                + "#" + ee.getSimpleName() + "#" + jvmMethodDesc(ee, types, elements)
                                + " overrides renamed supertype method " + supKey);
                        }
                    }
                }
            }
            q.addAll(types.directSupertypes(stm));
        }
    }

    private static String findStaticMemberImportRename(
        String leftDotted,
        String right,
        Map<String, FieldRename> fieldRenames,
        Map<String, MethodRename> methodRenames,
        Elements elements,
        Types types
    ) {
        TypeElement ownerTe = elements.getTypeElement(leftDotted);
        if (ownerTe == null) return null;
        String ownerInt = toInternalName(ownerTe, elements);
        Set<String> targets = new LinkedHashSet<>();
        boolean sawUnrenamed = false;
        for (Element mem : ownerTe.getEnclosedElements()) {
            if (!mem.getModifiers().contains(Modifier.STATIC)) continue;
            if (!mem.getSimpleName().contentEquals(right)) continue;
            if (mem instanceof VariableElement ve && ve.getKind() == ElementKind.FIELD) {
                String fKey = ownerInt + "#" + right + "#" + jvmTypeDesc(ve.asType(), types, elements);
                FieldRename fr = fieldRenames.get(fKey);
                if (fr != null) targets.add(fr.newName);
                else sawUnrenamed = true;
            } else if (mem instanceof ExecutableElement ee && ee.getKind() == ElementKind.METHOD) {
                String mKey = ownerInt + "#" + right + "#" + jvmMethodDesc(ee, types, elements);
                MethodRename mr = methodRenames.get(mKey);
                if (mr != null) targets.add(mr.newName);
                else sawUnrenamed = true;
            }
        }
        if (targets.isEmpty()) return null;
        if (targets.size() > 1 || sawUnrenamed) {
            throw new IllegalStateException("Ambiguous static import " + leftDotted + "." + right
                + ": partial/divergent renames " + targets + " (unrenamedPresent=" + sawUnrenamed + ")");
        }
        String single = targets.iterator().next();
        return single.equals(right) ? null : single;
    }

    private static List<String> computeUpdatedDisambig(
        Path mappingsPath,
        Map<String, EnumRename> enumRenames,
        Map<String, ClassRename> classRenames,
        Map<String, FieldRename> fieldRenames,
        Map<String, MethodRename> methodRenames,
        Map<String, RecordCompRename> recordCompRenames
    ) throws IOException {
        List<String> lines = Files.readAllLines(mappingsPath, StandardCharsets.UTF_8);
        List<String> out = new ArrayList<>(lines.size() + enumRenames.size() + classRenames.size()
            + fieldRenames.size() + methodRenames.size() + recordCompRenames.size() * 2);

        Set<String> seenFieldKeys = new HashSet<>(); // origOwner + "\t" + origName + "\t" + origDesc
        Set<String> seenMethodKeys = new HashSet<>(); // origOwner + "\t" + origName + "\t" + origDesc
        Set<String> seenClassOrig = new HashSet<>(); // origClass

        // Index enum, field, method, and record-component renames by origKey
        Map<String, EnumRename> enumByOrig = new HashMap<>();
        for (EnumRename er : enumRenames.values()) {
            enumByOrig.put(er.origEnum + "\t" + er.origConst + "\tL" + er.origEnum + ";", er);
        }
        Map<String, String> fieldNewByOrig = new HashMap<>();
        for (FieldRename fr : fieldRenames.values()) {
            String ok = fr.origOwner + "\t" + fr.origName + "\t" + fr.origDesc;
            String prev = fieldNewByOrig.putIfAbsent(ok, fr.newName);
            if (prev != null && !prev.equals(fr.newName)) {
                throw new IllegalStateException("Conflicting field origKey in plan: " + ok);
            }
        }
        for (RecordCompRename rr : recordCompRenames.values()) {
            String ok = rr.origRecord + "\t" + rr.origFieldName + "\t" + rr.origDesc;
            String prev = fieldNewByOrig.putIfAbsent(ok, rr.newName);
            if (prev != null && !prev.equals(rr.newName)) {
                throw new IllegalStateException("Conflicting record-comp field origKey in plan: " + ok);
            }
        }
        Map<String, String> methodNewByOrig = new HashMap<>();
        for (MethodRename mr : methodRenames.values()) {
            String ok = mr.origOwner + "\t" + mr.origName + "\t" + mr.origDesc;
            String prev = methodNewByOrig.putIfAbsent(ok, mr.newName);
            if (prev != null && !prev.equals(mr.newName)) {
                throw new IllegalStateException("Conflicting method origKey in plan: " + ok);
            }
        }
        for (RecordCompRename rr : recordCompRenames.values()) {
            String ok = rr.origRecord + "\t" + rr.origMethodName + "\t()" + rr.origDesc;
            String prev = methodNewByOrig.putIfAbsent(ok, rr.newName);
            if (prev != null && !prev.equals(rr.newName)) {
                throw new IllegalStateException("Conflicting record-comp accessor origKey in plan: " + ok + " (" + prev + " vs " + rr.newName + ")");
            }
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
                    } else if (fieldNewByOrig.containsKey(origKey)) {
                        newField = fieldNewByOrig.get(origKey);
                    }
                    out.add(String.join("\t", "F", p[1], p[2], p[3], newOwner, newField, newDesc));
                }
                case "M" -> {
                    String origKey = p[1] + "\t" + p[2] + "\t" + p[3];
                    seenMethodKeys.add(origKey);
                    String newOwner = remapClassPrefix(p[4], classRenames);
                    String newMethod = methodNewByOrig.getOrDefault(origKey, p[5]);
                    String newDesc = remapDescriptor(p[6], classRenames);
                    out.add(String.join("\t", "M", p[1], p[2], p[3], newOwner, newMethod, newDesc));
                }
                default -> out.add(line);
            }
        }

        // Add new C rows for renamed classes that weren't previously in disambig.tsv
        for (ClassRename cr : classRenames.values()) {
            if (!seenClassOrig.contains(cr.origClass)) {
                out.add("C\t" + cr.origClass + "\t" + remapClassPrefix(cr.curClass, classRenames));
            }
        }

        // Add new F rows for renamed enum constants that weren't previously collided in disambig.tsv
        for (EnumRename er : enumRenames.values()) {
            String origDesc = "L" + er.origEnum + ";";
            String origKey = er.origEnum + "\t" + er.origConst + "\t" + origDesc;
            if (seenFieldKeys.add(origKey)) {
                String newOwner = remapClassPrefix(er.curEnum, classRenames);
                String newDesc = "L" + newOwner + ";";
                out.add(String.join("\t", "F", er.origEnum, er.origConst, origDesc, newOwner, er.newConst, newDesc));
            }
        }

        // Add new F rows for renamed fields that weren't previously in disambig.tsv
        for (FieldRename fr : fieldRenames.values()) {
            String origKey = fr.origOwner + "\t" + fr.origName + "\t" + fr.origDesc;
            if (seenFieldKeys.add(origKey)) {
                String newOwner = remapClassPrefix(fr.curOwner, classRenames);
                String newDesc = remapDescriptor(fr.curDesc, classRenames);
                out.add(String.join("\t", "F", fr.origOwner, fr.origName, fr.origDesc, newOwner, fr.newName, newDesc));
            }
        }

        // Add new F and M rows for renamed record components that weren't previously in disambig.tsv
        for (RecordCompRename rr : recordCompRenames.values()) {
            String fOrigKey = rr.origRecord + "\t" + rr.origFieldName + "\t" + rr.origDesc;
            if (seenFieldKeys.add(fOrigKey)) {
                String newOwner = remapClassPrefix(rr.curRecord, classRenames);
                String newDesc = remapDescriptor(rr.curDesc, classRenames);
                out.add(String.join("\t", "F", rr.origRecord, rr.origFieldName, rr.origDesc, newOwner, rr.newName, newDesc));
            }
            String mOrigDesc = "()" + rr.origDesc;
            String mOrigKey = rr.origRecord + "\t" + rr.origMethodName + "\t" + mOrigDesc;
            if (seenMethodKeys.add(mOrigKey)) {
                String newOwner = remapClassPrefix(rr.curRecord, classRenames);
                String newDesc = "()" + remapDescriptor(rr.curDesc, classRenames);
                out.add(String.join("\t", "M", rr.origRecord, rr.origMethodName, mOrigDesc, newOwner, rr.newName, newDesc));
            }
        }

        // Add new M rows for renamed methods that weren't previously in disambig.tsv
        for (MethodRename mr : methodRenames.values()) {
            String origKey = mr.origOwner + "\t" + mr.origName + "\t" + mr.origDesc;
            if (seenMethodKeys.add(origKey)) {
                String newOwner = remapClassPrefix(mr.curOwner, classRenames);
                String newDesc = remapDescriptor(mr.curDesc, classRenames);
                out.add(String.join("\t", "M", mr.origOwner, mr.origName, mr.origDesc, newOwner, mr.newName, newDesc));
            }
        }

        // Integrity check: verify zero duplicate current-side F/M keys in updated disambig.tsv
        Set<String> curFieldKeys = new HashSet<>();
        Set<String> curMethodKeys = new HashSet<>();
        for (String line : out) {
            if (line.isEmpty() || line.startsWith("#")) continue;
            String[] p = line.split("\t", -1);
            if ("F".equals(p[0])) {
                String ck = p[4] + "\t" + p[5] + "\t" + p[6];
                if (!curFieldKeys.add(ck)) throw new IllegalStateException("Duplicate current F key in disambig.tsv: " + ck);
            } else if ("M".equals(p[0])) {
                String ck = p[4] + "\t" + p[5] + "\t" + p[6];
                if (!curMethodKeys.add(ck)) throw new IllegalStateException("Duplicate current M key in disambig.tsv: " + ck);
            }
        }

        return out;
    }
}

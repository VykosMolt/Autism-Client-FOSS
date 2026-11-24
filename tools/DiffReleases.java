import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.security.MessageDigest;
import java.util.*;
import java.util.zip.*;
import org.objectweb.asm.*;
import org.objectweb.asm.tree.*;

/**
 * Structural cross-release class matcher, semantic differ, patch impact analyzer,
 * and addon ABI/Mixin compatibility checker for obfuscated AUTISM Client releases.
 */
public final class DiffReleases {
    private static final Set<String> OBF_WORDS = Set.of(
        "ArrayUtil", "BitSetUtil", "BufferHelper", "ByteReader", "CacheEntry", "CachePool", "CharEncoder",
        "ClassCache", "ConfigStore", "DataPipe", "DataUtil", "EventBus", "FieldCache", "FileCache",
        "HashUtil", "IoWorker", "JsonUtil", "KeyHolder", "ListPool", "MapStore", "MethodUtil",
        "NetHelper", "NodeWalker", "PacketUtil", "PathHelper", "PoolEntry", "RefQueue",
        "RenderState", "RingBuffer", "StateCache", "StreamGate", "StreamUtil", "SyncManager",
        "TableIndex", "TaskQueue", "TaskTimer", "TextFormat", "TokenStore", "UiHelper", "UtilCore", "ValueSink"
    );

    static final class ClassInfo {
        final String name;       // e.g. autismclient/ai/cc
        final String pkg;        // e.g. autismclient/ai
        final String simple;     // e.g. cc
        final boolean obf;
        String superName = "";
        final List<String> interfaces = new ArrayList<>();
        String exactSha = "";
        String normHash = "";
        final Set<String> strings = new TreeSet<>();
        final Set<String> extCalls = new TreeSet<>();
        final Map<String, Integer> shapeBag = new HashMap<>();
        int innerCount = 0;
        int insnCount = 0;

        ClassInfo(String name) {
            this.name = name;
            int slash = name.lastIndexOf('/');
            this.pkg = slash < 0 ? "" : name.substring(0, slash);
            this.simple = slash < 0 ? name : name.substring(slash + 1);
            this.obf = isObfName(this.simple);
        }
    }

    static boolean isObfName(String simple) {
        if (simple.length() <= 2) return true;
        return OBF_WORDS.contains(simple);
    }

    static String normType(String internal) {
        if (internal == null) return "null";
        if (internal.startsWith("autismclient/")) {
            int slash = internal.lastIndexOf('/');
            String pkg = slash < 0 ? "" : internal.substring(0, slash);
            String simp = slash < 0 ? internal : internal.substring(slash + 1);
            int dollar = simp.indexOf('$');
            String outer = dollar < 0 ? simp : simp.substring(0, dollar);
            if (isObfName(outer)) return pkg + "/<OBF>" + (dollar >= 0 ? "$<IN>" : "");
        }
        return internal;
    }

    static String normDesc(String desc) {
        StringBuilder sb = new StringBuilder(desc.length());
        int i = 0;
        while (i < desc.length()) {
            char c = desc.charAt(i);
            if (c == 'L') {
                int semi = desc.indexOf(';', i);
                if (semi > i) {
                    String inner = desc.substring(i + 1, semi);
                    sb.append('L').append(normType(inner)).append(';');
                    i = semi + 1;
                    continue;
                }
            }
            sb.append(c);
            i++;
        }
        return sb.toString();
    }

    static Map<String, ClassInfo> loadJar(Path jarPath, Map<String, byte[]> rawOut) throws Exception {
        Map<String, List<byte[]>> grouped = new TreeMap<>();
        try (ZipFile zf = new ZipFile(jarPath.toFile())) {
            for (ZipEntry ze : Collections.list(zf.entries())) {
                if (ze.isDirectory() || !ze.getName().endsWith(".class")) continue;
                String cn = ze.getName().substring(0, ze.getName().length() - 6);
                byte[] bytes;
                try (InputStream in = zf.getInputStream(ze)) {
                    bytes = in.readAllBytes();
                }
                rawOut.put(cn, bytes);
                int dollar = cn.indexOf('$');
                String outer = dollar < 0 ? cn : cn.substring(0, dollar);
                grouped.computeIfAbsent(outer, k -> new ArrayList<>()).add(bytes);
            }
        }

        Map<String, ClassInfo> out = new TreeMap<>();
        for (var e : grouped.entrySet()) {
            String outer = e.getKey();
            ClassInfo info = new ClassInfo(outer);
            MessageDigest exactMd = MessageDigest.getInstance("SHA-256");
            List<String> normParts = new ArrayList<>();

            List<byte[]> parts = e.getValue();
            info.innerCount = parts.size() - 1;
            for (byte[] bytes : parts) {
                exactMd.update(bytes);
                ClassNode cn = new ClassNode();
                new ClassReader(bytes).accept(cn, ClassReader.SKIP_DEBUG | ClassReader.SKIP_FRAMES);
                if (cn.name.equals(outer)) {
                    info.superName = normType(cn.superName);
                    if (cn.interfaces != null) {
                        for (String itf : cn.interfaces) info.interfaces.add(normType(itf));
                        Collections.sort(info.interfaces);
                    }
                }
                StringBuilder norm = new StringBuilder();
                norm.append("S:").append(normType(cn.superName));
                if (cn.interfaces != null) {
                    List<String> itfs = new ArrayList<>();
                    for (String itf : cn.interfaces) itfs.add(normType(itf));
                    Collections.sort(itfs);
                    norm.append("|I:").append(itfs);
                }
                List<String> fSigs = new ArrayList<>();
                for (FieldNode fn : cn.fields) {
                    String fd = normDesc(fn.desc);
                    fSigs.add((fn.access & 0x1F) + ":" + fd + ":" + fn.value);
                    info.shapeBag.merge("F:" + fd, 1, Integer::sum);
                }
                Collections.sort(fSigs);
                norm.append("|F:").append(fSigs);

                List<String> mSigs = new ArrayList<>();
                for (MethodNode mn : cn.methods) {
                    String md = normDesc(mn.desc);
                    info.shapeBag.merge("M:" + md, 1, Integer::sum);
                    StringBuilder mb = new StringBuilder();
                    mb.append((mn.access & 0x1F)).append(':').append(md).append('{');
                    for (AbstractInsnNode insn : mn.instructions) {
                        int op = insn.getOpcode();
                        if (op < 0) continue;
                        info.insnCount++;
                        mb.append(op);
                        if (insn instanceof LdcInsnNode ldc) {
                            if (ldc.cst instanceof String s) {
                                if (!s.isEmpty() && s.length() <= 200) info.strings.add(s);
                                mb.append("S(").append(s).append(')');
                            } else {
                                mb.append("C(").append(ldc.cst).append(')');
                            }
                        } else if (insn instanceof MethodInsnNode min) {
                            String ow = normType(min.owner);
                            String d = normDesc(min.desc);
                            if (!min.owner.startsWith("autismclient/")) {
                                info.extCalls.add(min.owner + "." + min.name + min.desc);
                                mb.append("M(").append(min.owner).append('.').append(min.name).append(d).append(')');
                            } else {
                                mb.append("M(").append(ow).append(d).append(')');
                            }
                        } else if (insn instanceof FieldInsnNode fin) {
                            String d = normDesc(fin.desc);
                            if (!fin.owner.startsWith("autismclient/")) {
                                info.extCalls.add(fin.owner + "#" + fin.name);
                                mb.append("F(").append(fin.owner).append('.').append(fin.name).append(':').append(d).append(')');
                            } else {
                                mb.append("F(").append(normType(fin.owner)).append(':').append(d).append(')');
                            }
                        } else if (insn instanceof TypeInsnNode tin) {
                            mb.append("T(").append(normType(tin.desc)).append(')');
                        } else if (insn instanceof IntInsnNode iin) {
                            mb.append("I(").append(iin.operand).append(')');
                        }
                        mb.append(',');
                    }
                    mb.append('}');
                    mSigs.add(mb.toString());
                }
                Collections.sort(mSigs);
                norm.append("|M:").append(mSigs);
                normParts.add(norm.toString());
            }
            Collections.sort(normParts);
            MessageDigest normMd = MessageDigest.getInstance("SHA-256");
            for (String p : normParts) normMd.update(p.getBytes(StandardCharsets.UTF_8));
            info.exactSha = hex(exactMd.digest());
            info.normHash = hex(normMd.digest());
            out.put(outer, info);
        }
        return out;
    }

    static String hex(byte[] b) {
        StringBuilder sb = new StringBuilder(b.length * 2);
        for (byte x : b) sb.append(String.format("%02x", x));
        return sb.toString();
    }

    static double jaccard(Set<String> a, Set<String> b) {
        if (a.isEmpty() && b.isEmpty()) return 1.0;
        if (a.isEmpty() || b.isEmpty()) return 0.0;
        int inter = 0;
        for (String s : a) if (b.contains(s)) inter++;
        return (double) inter / (a.size() + b.size() - inter);
    }

    static double bagSim(Map<String, Integer> a, Map<String, Integer> b) {
        if (a.isEmpty() && b.isEmpty()) return 1.0;
        int inter = 0, union = 0;
        Set<String> keys = new HashSet<>(a.keySet());
        keys.addAll(b.keySet());
        for (String k : keys) {
            int va = a.getOrDefault(k, 0);
            int vb = b.getOrDefault(k, 0);
            inter += Math.min(va, vb);
            union += Math.max(va, vb);
        }
        return union == 0 ? 1.0 : (double) inter / union;
    }

    static double similarity(ClassInfo a, ClassInfo b) {
        if (!a.superName.equals(b.superName)) return 0.0;
        double score = 0.0, weight = 0.0;
        if (!a.strings.isEmpty() || !b.strings.isEmpty()) {
            score += 5.0 * jaccard(a.strings, b.strings);
            weight += 5.0;
        }
        if (!a.extCalls.isEmpty() || !b.extCalls.isEmpty()) {
            score += 3.0 * jaccard(a.extCalls, b.extCalls);
            weight += 3.0;
        }
        score += 2.0 * bagSim(a.shapeBag, b.shapeBag);
        weight += 2.0;
        if (!a.interfaces.isEmpty() || !b.interfaces.isEmpty()) {
            score += 2.0 * jaccard(new HashSet<>(a.interfaces), new HashSet<>(b.interfaces));
            weight += 2.0;
        }
        return weight == 0.0 ? 0.0 : score / weight;
    }

    public static void main(String[] args) throws Exception {
        if (args.length < 2) {
            System.err.println("Usage: DiffReleases <old-jar> <new-jar> [--patched=<file>] [--disambig=<tsv>] [--addons=<jar1,jar2,...>] [--map-out=<tsv>]");
            System.exit(2);
        }
        Path oldJar = Path.of(args[0]);
        Path newJar = Path.of(args[1]);
        Path patchedList = null;
        Path oldDisambig = null;
        Path mapOut = null;
        List<Path> addons = new ArrayList<>();
        for (int i = 2; i < args.length; i++) {
            if (args[i].startsWith("--patched=")) patchedList = Path.of(args[i].substring(10));
            else if (args[i].startsWith("--disambig=")) oldDisambig = Path.of(args[i].substring(11));
            else if (args[i].startsWith("--map-out=")) mapOut = Path.of(args[i].substring(10));
            else if (args[i].startsWith("--addons=")) {
                for (String p : args[i].substring(9).split(",")) {
                    if (!p.isBlank()) addons.add(Path.of(p));
                }
            }
        }

        Map<String, byte[]> oldRaw = new HashMap<>();
        Map<String, byte[]> newRaw = new HashMap<>();
        Map<String, ClassInfo> oldClasses = loadJar(oldJar, oldRaw);
        Map<String, ClassInfo> newClasses = loadJar(newJar, newRaw);

        // Group by package
        Map<String, List<ClassInfo>> oldByPkg = new TreeMap<>();
        Map<String, List<ClassInfo>> newByPkg = new TreeMap<>();
        for (ClassInfo c : oldClasses.values()) oldByPkg.computeIfAbsent(c.pkg, k -> new ArrayList<>()).add(c);
        for (ClassInfo c : newClasses.values()) newByPkg.computeIfAbsent(c.pkg, k -> new ArrayList<>()).add(c);

        Map<String, String> oldToNew = new TreeMap<>();
        Map<String, String> matchReason = new HashMap<>();
        Map<String, Double> matchScore = new HashMap<>();
        Set<String> matchedNew = new HashSet<>();

        Set<String> allPkgs = new TreeSet<>(oldByPkg.keySet());
        allPkgs.addAll(newByPkg.keySet());

        for (String pkg : allPkgs) {
            List<ClassInfo> oList = oldByPkg.getOrDefault(pkg, List.of());
            List<ClassInfo> nList = newByPkg.getOrDefault(pkg, List.of());
            Map<String, ClassInfo> nByName = new HashMap<>();
            for (ClassInfo n : nList) nByName.put(n.name, n);

            // Pass 1: Non-obfuscated exact class name match
            for (ClassInfo o : oList) {
                if (!o.obf && nByName.containsKey(o.name)) {
                    ClassInfo n = nByName.get(o.name);
                    oldToNew.put(o.name, n.name);
                    matchedNew.add(n.name);
                    boolean sameLogic = o.normHash.equals(n.normHash);
                    matchReason.put(o.name, sameLogic ? "EXACT_SAME" : "NAMED_CHANGED");
                    matchScore.put(o.name, sameLogic ? 1.0 : similarity(o, n));
                }
            }

            // Pass 2: Same-name AND same normHash for obfuscated classes
            for (ClassInfo o : oList) {
                if (oldToNew.containsKey(o.name)) continue;
                ClassInfo n = nByName.get(o.name);
                if (n != null && !matchedNew.contains(n.name) && o.normHash.equals(n.normHash)) {
                    oldToNew.put(o.name, n.name);
                    matchedNew.add(n.name);
                    matchReason.put(o.name, "EXACT_SAME");
                    matchScore.put(o.name, 1.0);
                }
            }

            // Pass 3: Different-name AND exact same normHash within same package (pure obfuscator rename!)
            Map<String, Deque<ClassInfo>> newByNorm = new HashMap<>();
            for (ClassInfo n : nList) {
                if (!matchedNew.contains(n.name)) {
                    newByNorm.computeIfAbsent(n.normHash, k -> new ArrayDeque<>()).add(n);
                }
            }
            for (ClassInfo o : oList) {
                if (oldToNew.containsKey(o.name)) continue;
                Deque<ClassInfo> q = newByNorm.get(o.normHash);
                if (q != null && !q.isEmpty()) {
                    ClassInfo n = q.poll();
                    oldToNew.put(o.name, n.name);
                    matchedNew.add(n.name);
                    matchReason.put(o.name, "RENAMED_IDENTICAL");
                    matchScore.put(o.name, 1.0);
                }
            }

            // Pass 4: Structural similarity matching for changed obfuscated classes in same package
            List<ClassInfo> remOld = new ArrayList<>();
            List<ClassInfo> remNew = new ArrayList<>();
            for (ClassInfo o : oList) if (!oldToNew.containsKey(o.name)) remOld.add(o);
            for (ClassInfo n : nList) if (!matchedNew.contains(n.name)) remNew.add(n);

            record Pair(ClassInfo o, ClassInfo n, double sim) {}
            List<Pair> pairs = new ArrayList<>();
            for (ClassInfo o : remOld) {
                for (ClassInfo n : remNew) {
                    double sim = similarity(o, n);
                    if (sim >= 0.45) pairs.add(new Pair(o, n, sim));
                }
            }
            pairs.sort((a, b) -> Double.compare(b.sim, a.sim));
            for (Pair p : pairs) {
                if (oldToNew.containsKey(p.o.name) || matchedNew.contains(p.n.name)) continue;
                oldToNew.put(p.o.name, p.n.name);
                matchedNew.add(p.n.name);
                matchReason.put(p.o.name, p.o.name.equals(p.n.name) ? "OBF_CHANGED" : "RENAMED_AND_CHANGED");
                matchScore.put(p.o.name, p.sim);
            }
        }

        int exactSame = 0, renamedIdentical = 0;
        List<String> changedList = new ArrayList<>();
        List<String> removedList = new ArrayList<>();
        List<String> addedList = new ArrayList<>();

        for (ClassInfo o : oldClasses.values()) {
            String r = matchReason.get(o.name);
            if ("EXACT_SAME".equals(r)) exactSame++;
            else if ("RENAMED_IDENTICAL".equals(r)) renamedIdentical++;
            else if (r != null) {
                String nn = oldToNew.get(o.name);
                double sc = matchScore.getOrDefault(o.name, 0.0);
                ClassInfo n = newClasses.get(nn);
                int deltaInsn = n.insnCount - o.insnCount;
                changedList.add(String.format("  %-52s -> %-42s [%s, sim=%.2f, insn %+d]", o.name, nn, r, sc, deltaInsn));
            } else {
                removedList.add(String.format("  %-52s (insns=%d, strings=%s)", o.name, o.insnCount, sampleStrings(o.strings)));
            }
        }
        for (ClassInfo n : newClasses.values()) {
            if (!matchedNew.contains(n.name)) {
                addedList.add(String.format("  %-52s (insns=%d, strings=%s)", n.name, n.insnCount, sampleStrings(n.strings)));
            }
        }

        System.out.println("=== STRUCTURAL RELEASE DIFF SUMMARY ===");
        System.out.printf("Top-level classes: old=%d, new=%d%n", oldClasses.size(), newClasses.size());
        System.out.printf("  Identical (same name):      %d%n", exactSame);
        System.out.printf("  Pure obfuscator renames:    %d (100%% identical logic)%n", renamedIdentical);
        System.out.printf("  Genuinely changed logic:    %d%n", changedList.size());
        System.out.printf("  Genuinely added classes:    %d%n", addedList.size());
        System.out.printf("  Genuinely removed classes:  %d%n", removedList.size());

        System.out.println("\n=== GENUINELY ADDED CLASSES (" + addedList.size() + ") ===");
        addedList.forEach(System.out::println);

        System.out.println("\n=== GENUINELY REMOVED CLASSES (" + removedList.size() + ") ===");
        removedList.forEach(System.out::println);

        System.out.println("\n=== GENUINELY LOGIC-CHANGED CLASSES (" + changedList.size() + ") ===");
        changedList.forEach(System.out::println);

        // Write full old->new map if requested
        if (mapOut != null) {
            List<String> lines = new ArrayList<>();
            lines.add("# oldClass\tnewClass\tstatus\tsimilarity");
            for (var e : oldToNew.entrySet()) {
                lines.add(e.getKey() + "\t" + e.getValue() + "\t" + matchReason.get(e.getKey()) + "\t" + String.format(Locale.ROOT, "%.3f", matchScore.getOrDefault(e.getKey(), 0.0)));
            }
            Files.write(mapOut, lines, StandardCharsets.UTF_8);
        }

        // Patch impact analysis
        if (patchedList != null && Files.exists(patchedList)) {
            Map<String, String> newToOrigDisambig = new HashMap<>();
            if (oldDisambig != null && Files.exists(oldDisambig)) {
                for (String line : Files.readAllLines(oldDisambig, StandardCharsets.UTF_8)) {
                    if (line.startsWith("C\t")) {
                        String[] p = line.split("\t");
                        newToOrigDisambig.put(p[2], p[1]);
                    }
                }
            }
            System.out.println("\n=== IMPACT ON OUR PATCHED FILES ===");
            int pSame = 0, pRenamed = 0, pChanged = 0, pMissing = 0;
            for (String rel : Files.readAllLines(patchedList, StandardCharsets.UTF_8)) {
                rel = rel.trim();
                if (!rel.endsWith(".java") || !rel.startsWith("src/main/java/")) continue;
                String disambigClass = rel.substring("src/main/java/".length(), rel.length() - 5);
                String origClass = newToOrigDisambig.getOrDefault(disambigClass, disambigClass);
                String newClass = oldToNew.get(origClass);
                String reason = matchReason.get(origClass);
                if ("EXACT_SAME".equals(reason)) {
                    pSame++;
                } else if ("RENAMED_IDENTICAL".equals(reason)) {
                    pRenamed++;
                    System.out.printf("  [RENAMED_ONLY] %-48s -> %s%n", origClass, newClass);
                } else if (newClass != null) {
                    pChanged++;
                    System.out.printf("  [UPSTREAM_MOD] %-48s -> %-40s (%s, sim=%.2f)%n", origClass, newClass, reason, matchScore.getOrDefault(origClass, 0.0));
                } else {
                    pMissing++;
                    System.out.printf("  [NOT_IN_NEW  ] %-48s%n", origClass);
                }
            }
            System.out.printf("Patched summary: %d unchanged (exact name), %d renamed-only (identical logic), %d modified upstream, %d not in new jar%n",
                pSame, pRenamed, pChanged, pMissing);
        }

        // Addon compatibility check against newJar
        if (!addons.isEmpty()) {
            System.out.println("\n=== ADDON COMPATIBILITY CHECK AGAINST NEW JAR ===");
            Map<String, Set<String>> newMembers = indexAllMembers(newRaw);
            for (Path addon : addons) {
                if (!Files.exists(addon)) continue;
                checkAddon(addon, newRaw.keySet(), newMembers, oldToNew, matchReason);
            }
        }
    }

    static String sampleStrings(Set<String> s) {
        List<String> list = new ArrayList<>();
        for (String x : s) {
            if (x.length() >= 3 && x.length() <= 40 && !x.contains("\n")) {
                list.add("\"" + x + "\"");
                if (list.size() == 3) break;
            }
        }
        return list.toString();
    }

    static Map<String, Set<String>> indexAllMembers(Map<String, byte[]> raw) {
        Map<String, Set<String>> map = new HashMap<>();
        for (var e : raw.entrySet()) {
            Set<String> m = new HashSet<>();
            ClassNode cn = new ClassNode();
            new ClassReader(e.getValue()).accept(cn, ClassReader.SKIP_CODE | ClassReader.SKIP_DEBUG | ClassReader.SKIP_FRAMES);
            for (FieldNode f : cn.fields) m.add("F:" + f.name + ":" + f.desc);
            for (MethodNode mn : cn.methods) m.add("M:" + mn.name + mn.desc);
            map.put(e.getKey(), m);
        }
        return map;
    }

    static void checkAddon(Path addonJar, Set<String> newClassNames, Map<String, Set<String>> newMembers,
                           Map<String, String> oldToNew, Map<String, String> matchReason) throws Exception {
        int refs = 0, broken = 0;
        List<String> issues = new ArrayList<>();
        try (ZipFile zf = new ZipFile(addonJar.toFile())) {
            for (ZipEntry ze : Collections.list(zf.entries())) {
                if (ze.isDirectory() || !ze.getName().endsWith(".class")) continue;
                byte[] bytes;
                try (InputStream in = zf.getInputStream(ze)) { bytes = in.readAllBytes(); }
                ClassNode cn = new ClassNode();
                new ClassReader(bytes).accept(cn, ClassReader.SKIP_DEBUG | ClassReader.SKIP_FRAMES);

                // Check @Mixin annotation targets (both class refs and string targets)
                if (cn.invisibleAnnotations != null) {
                    for (AnnotationNode an : cn.invisibleAnnotations) {
                        if ("Lorg/spongepowered/asm/mixin/Mixin;".equals(an.desc) && an.values != null) {
                            for (int i = 0; i < an.values.size(); i += 2) {
                                String k = (String) an.values.get(i);
                                Object v = an.values.get(i + 1);
                                if (("targets".equals(k) || "value".equals(k)) && v instanceof List<?> list) {
                                    for (Object item : list) {
                                        String target = item instanceof Type t ? t.getInternalName() : item.toString().replace('.', '/');
                                        if (target.startsWith("autismclient/")) {
                                            refs++;
                                            String outer = target.contains("$") ? target.substring(0, target.indexOf('$')) : target;
                                            String mappedOuter = oldToNew.get(outer);
                                            if (!newClassNames.contains(target)) {
                                                broken++;
                                                issues.add("  MIXIN TARGET MISSING: " + target + " (in " + cn.name + "; outer mapped to " + mappedOuter + ")");
                                            } else if (mappedOuter != null && !mappedOuter.equals(outer)) {
                                                broken++;
                                                issues.add("  MIXIN TARGET SHIFTED: " + target + " -> old " + outer + " moved to " + mappedOuter + " (in " + cn.name + ")");
                                            } else {
                                                String reason = matchReason.get(outer);
                                                if (reason != null && !"EXACT_SAME".equals(reason)) {
                                                    issues.add("  MIXIN TARGET CHANGED: " + target + " (" + reason + ", in " + cn.name + ")");
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // Check direct bytecode refs and string Class.forName("autismclient.*") refs
                for (MethodNode mn : cn.methods) {
                    for (AbstractInsnNode insn : mn.instructions) {
                        if (insn instanceof MethodInsnNode min && min.owner.startsWith("autismclient/")) {
                            refs++;
                            String outer = min.owner.contains("$") ? min.owner.substring(0, min.owner.indexOf('$')) : min.owner;
                            String mappedOuter = oldToNew.get(outer);
                            if (mappedOuter != null && !mappedOuter.equals(outer)) {
                                broken++;
                                if (issues.size() < 25) issues.add("  REF OWNER SHIFTED: " + min.owner + "." + min.name + " (outer moved to " + mappedOuter + ")");
                            } else if (!newClassNames.contains(min.owner)) {
                                broken++;
                                if (issues.size() < 25) issues.add("  REF CLASS MISSING: " + min.owner + "." + min.name);
                            }
                        } else if (insn instanceof FieldInsnNode fin && fin.owner.startsWith("autismclient/")) {
                            refs++;
                            String outer = fin.owner.contains("$") ? fin.owner.substring(0, fin.owner.indexOf('$')) : fin.owner;
                            String mappedOuter = oldToNew.get(outer);
                            if (mappedOuter != null && !mappedOuter.equals(outer)) {
                                broken++;
                                if (issues.size() < 25) issues.add("  FIELD OWNER SHIFTED: " + fin.owner + "." + fin.name + " (outer moved to " + mappedOuter + ")");
                            } else if (!newClassNames.contains(fin.owner)) {
                                broken++;
                                if (issues.size() < 25) issues.add("  FIELD CLASS MISSING: " + fin.owner + "." + fin.name);
                            }
                        } else if (insn instanceof LdcInsnNode ldc && ldc.cst instanceof String s && s.startsWith("autismclient.") && !s.contains(" ")) {
                            String internal = s.replace('.', '/');
                            String outer = internal.contains("$") ? internal.substring(0, internal.indexOf('$')) : internal;
                            String mappedOuter = oldToNew.get(outer);
                            if (mappedOuter != null && !mappedOuter.equals(outer)) {
                                broken++;
                                issues.add("  REFLECTIVE TARGET SHIFTED: " + s + " -> moved to " + mappedOuter.replace('/', '.') + " (in " + cn.name + ")");
                            } else if (!newClassNames.contains(internal)) {
                                broken++;
                                issues.add("  REFLECTIVE TARGET MISSING: " + s + " (in " + cn.name + ")");
                            }
                        }
                    }
                }
            }
        }
        System.out.printf("%s: %d autismclient refs checked, %d broken/shifted%n", addonJar.getFileName(), refs, broken);
        for (String iss : issues) System.out.println(iss);
    }
}

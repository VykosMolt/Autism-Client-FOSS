import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;
import java.util.zip.*;
import org.objectweb.asm.*;
import org.objectweb.asm.commons.*;
import org.objectweb.asm.tree.*;

/**
 * Lossless forward ASM disambiguator for AUTISM Client 5.3 (26.2).
 *
 * Resolves all Java-source-illegal obfuscation collisions BEFORE Vineflower decompilation:
 *  1. Java keyword class names (e.g. autismclient/util/do -> Kw_do)
 *  2. Class-vs-package name clashes (e.g. autismclient/util/g vs autismclient/util/g/* -> PkgCls_g)
 *  3. Duplicate outer/inner class names (e.g. DataUtil$DataUtil -> DataUtil$Inner_DataUtil)
 *  4. Same-class field name collisions (17,090 fields with identical names & different types)
 *  5. Same-class/hierarchy return-type method overloads (10,061 methods with identical name+params & diff return)
 *
 * Preserves the 12 Integrity-protected classes byte-for-byte in precompiled/integrity-classes.jar
 * while emitting disambiguated compile-time stubs in stubs/integrity-stubs.jar and writing the
 * exact bidirectional mapping table to mappings/disambig.tsv so ReverseRemapJar can restore the
 * original runtime ABI (keeping Integrity hashes and all 7 addons 100% intact).
 */
public final class DisambiguateJar {
    private static final Set<String> KEYWORDS = Set.of(
        "abstract","assert","boolean","break","byte","case","catch","char","class","const","continue",
        "default","do","double","else","enum","extends","final","finally","float","for","goto","if",
        "implements","import","instanceof","int","interface","long","native","new","package","private",
        "protected","public","return","short","static","strictfp","super","switch","synchronized","this",
        "throw","throws","transient","try","void","volatile","while","true","false","null","_","var","record","yield","sealed","permits"
    );

    static final class ExtClassInfo {
        final String superName;
        final List<String> interfaces;
        final Set<String> methodKeys; // name + desc and REF:name + params (non-private, non-static, for override pinning)
        final Set<String> allNameParams; // ALL non-private methods' name + params (for conflict detection)
        ExtClassInfo(String superName, List<String> interfaces, Set<String> methodKeys, Set<String> allNameParams) {
            this.superName = superName;
            this.interfaces = interfaces;
            this.methodKeys = methodKeys;
            this.allNameParams = allNameParams;
        }
    }

    public static void main(String[] args) throws Exception {
        if (args.length < 3) {
            System.err.println("Usage: DisambiguateJar <input-jar> <project-dir> <ext-cp-colon-separated>");
            System.exit(2);
        }
        Path inputJar = Path.of(args[0]);
        Path outDir = Path.of(args[1]);
        String extCp = args[2];

        // 1. Load all class bytes and non-class resources from input JAR
        Map<String, byte[]> rawClasses = new LinkedHashMap<>();
        Map<String, ClassNode> classNodes = new LinkedHashMap<>();
        Map<String, byte[]> resources = new LinkedHashMap<>();
        try (ZipFile zf = new ZipFile(inputJar.toFile())) {
            for (ZipEntry e : Collections.list(zf.entries())) {
                if (e.isDirectory()) continue;
                byte[] bytes;
                try (InputStream in = zf.getInputStream(e)) {
                    bytes = in.readAllBytes();
                }
                if (e.getName().endsWith(".class")) {
                    String cn = e.getName().substring(0, e.getName().length() - 6);
                    rawClasses.put(cn, bytes);
                    ClassNode node = new ClassNode();
                    new ClassReader(bytes).accept(node, ClassReader.SKIP_CODE | ClassReader.SKIP_DEBUG);
                    classNodes.put(cn, node);
                } else {
                    resources.put(e.getName(), bytes);
                }
            }
        }
        System.out.println("Loaded " + classNodes.size() + " classes and " + resources.size() + " resources.");

        // 2. Identify the 12 Integrity-protected classes + their 39 inner classes (NEVER modified in runtime output)
        Set<String> integrityOuter = readIntegrityClasses(rawClasses.get("autismclient/obf/Integrity"));
        Set<String> integrityClasses = new LinkedHashSet<>(integrityOuter);
        for (String cn : classNodes.keySet()) {
            for (String outer : integrityOuter) {
                if (cn.startsWith(outer + "$")) {
                    integrityClasses.add(cn);
                    break;
                }
            }
        }
        System.out.println("Integrity-protected bundle: " + integrityOuter.size() + " outer + "
            + (integrityClasses.size() - integrityOuter.size()) + " inner = " + integrityClasses.size() + " classes.");

        // 3. Index external classes (JDK + MC 26.2 + Fabric + Sodium + JiJ jars) for override pinning
        Map<String, ExtClassInfo> extIndex = indexExternalClasspath(extCp, resources);
        System.out.println("Indexed " + extIndex.size() + " external classpath classes.");

        // 4. Compute class renames (keyword classes, class==package clashes, duplicate outer/inner names)
        Map<String, String> classMap = computeClassRenames(classNodes, integrityOuter);
        System.out.println("Class renames: " + classMap.size());

        // 5. Compute field renames (same-class collisions + inheritance shadowing + short class name clashes + keywords)
        Map<String, String> fieldMap = computeFieldRenames(classNodes, classMap);
        System.out.println("Field renames: " + fieldMap.size());

        // 6. Compute hierarchy-consistent method renames (return-type overloads + keyword names + record accessors)
        Map<String, String> methodMap = computeMethodRenames(classNodes, extIndex, fieldMap);
        System.out.println("Method renames: " + methodMap.size());

        // 7. Build ASM Remapper with full superclass/interface resolution
        Remapper remapper = new HierarchyRemapper(classNodes, classMap, fieldMap, methodMap);

        // 8. Write outputs
        Files.createDirectories(outDir.resolve("mappings"));
        Files.createDirectories(outDir.resolve("precompiled"));
        Files.createDirectories(outDir.resolve("stubs"));
        Files.createDirectories(outDir.resolve("build"));
        Path resDir = outDir.resolve("src/main/resources");
        Files.createDirectories(resDir);

        // Extract resources
        for (var entry : resources.entrySet()) {
            Path p = resDir.resolve(entry.getKey());
            Files.createDirectories(p.getParent());
            Files.write(p, entry.getValue());
        }

        // Write precompiled/integrity-classes.jar (100% untouched original bytes!)
        try (ZipOutputStream zos = new ZipOutputStream(Files.newOutputStream(outDir.resolve("precompiled/integrity-classes.jar")))) {
            for (String ic : new TreeSet<>(integrityClasses)) {
                byte[] orig = rawClasses.get(ic);
                if (orig == null) throw new IllegalStateException("Missing integrity class: " + ic);
                ZipEntry ze = new ZipEntry(ic + ".class");
                ze.setTime(0L);
                zos.putNextEntry(ze);
                zos.write(orig);
                zos.closeEntry();
            }
        }

        // Remap all classes:
        //  - Non-integrity -> build/disambig-decompile.jar
        //  - Integrity     -> stubs/integrity-stubs.jar AND build/integrity-for-ref.jar
        try (
            ZipOutputStream mainOut = new ZipOutputStream(Files.newOutputStream(outDir.resolve("build/disambig-decompile.jar")));
            ZipOutputStream stubOut = new ZipOutputStream(Files.newOutputStream(outDir.resolve("stubs/integrity-stubs.jar")));
            ZipOutputStream refOut = new ZipOutputStream(Files.newOutputStream(outDir.resolve("build/integrity-for-ref.jar")))
        ) {
            for (var entry : rawClasses.entrySet()) {
                String origName = entry.getKey();
                byte[] remapped = remapClass(entry.getValue(), remapper);
                String newName = remapper.map(origName);
                ZipEntry ze = new ZipEntry(newName + ".class");
                ze.setTime(0L);
                if (integrityClasses.contains(origName)) {
                    stubOut.putNextEntry(ze);
                    stubOut.write(remapped);
                    stubOut.closeEntry();

                    ZipEntry ze2 = new ZipEntry(newName + ".class");
                    ze2.setTime(0L);
                    refOut.putNextEntry(ze2);
                    refOut.write(remapped);
                    refOut.closeEntry();
                } else {
                    mainOut.putNextEntry(ze);
                    mainOut.write(remapped);
                    mainOut.closeEntry();
                }
            }
        }

        // 9. Write lossless bidirectional mapping table mappings/disambig.tsv
        // Format:
        //   I <integrityClassInternalName>
        //   C <origClass> <newClass>
        //   F <origOwner> <origName> <origDesc> <newOwner> <newName> <newDesc>
        //   M <origOwner> <origName> <origDesc> <newOwner> <newName> <newDesc>
        List<String> lines = new ArrayList<>();
        lines.add("# AUTISM 5.3 lossless bidirectional disambiguation mappings");
        for (String ic : new TreeSet<>(integrityClasses)) {
            lines.add("I\t" + ic);
        }
        for (var e : new TreeMap<>(classMap).entrySet()) {
            lines.add("C\t" + e.getKey() + "\t" + e.getValue());
        }
        for (var e : new TreeMap<>(fieldMap).entrySet()) {
            String[] parts = e.getKey().split("\t");
            String origOwner = parts[0], origName = parts[1], origDesc = parts[2];
            String newOwner = remapper.map(origOwner);
            String newName = e.getValue();
            String newDesc = remapper.mapDesc(origDesc);
            lines.add("F\t" + origOwner + "\t" + origName + "\t" + origDesc + "\t" + newOwner + "\t" + newName + "\t" + newDesc);
        }
        for (var e : new TreeMap<>(methodMap).entrySet()) {
            String[] parts = e.getKey().split("\t");
            String origOwner = parts[0], origName = parts[1], origDesc = parts[2];
            String newOwner = remapper.map(origOwner);
            String newName = e.getValue();
            String newDesc = remapper.mapMethodDesc(origDesc);
            lines.add("M\t" + origOwner + "\t" + origName + "\t" + origDesc + "\t" + newOwner + "\t" + newName + "\t" + newDesc);
        }
        Files.write(outDir.resolve("mappings/disambig.tsv"), lines, StandardCharsets.UTF_8);
        System.out.println("Wrote mappings/disambig.tsv (" + lines.size() + " lines).");
    }

    private static Set<String> readIntegrityClasses(byte[] integrityBytes) {
        Set<String> set = new LinkedHashSet<>();
        set.add("autismclient/obf/Integrity");
        if (integrityBytes == null) return set;
        ClassNode ic = new ClassNode();
        new ClassReader(integrityBytes).accept(ic, 0);
        for (MethodNode m : ic.methods) {
            if ("entries".equals(m.name)) {
                for (AbstractInsnNode insn : m.instructions) {
                    if (insn instanceof LdcInsnNode ldc && ldc.cst instanceof String s && s.startsWith("autismclient.")) {
                        set.add(s.replace('.', '/'));
                    }
                }
            }
        }
        return set;
    }

    private static Map<String, ExtClassInfo> indexExternalClasspath(String extCp, Map<String, byte[]> resources) throws IOException {
        Map<String, ExtClassInfo> map = new HashMap<>();
        List<Path> jars = new ArrayList<>();
        for (String p : extCp.split(":")) {
            if (p.isEmpty()) continue;
            Path path = Path.of(p);
            if (Files.isRegularFile(path) && p.endsWith(".jar")) jars.add(path);
        }
        for (Path jar : jars) {
            try (ZipFile zf = new ZipFile(jar.toFile())) {
                indexZip(zf, map);
            } catch (IOException ignored) {}
        }
        // Also index nested META-INF/jars/*.jar
        for (var e : resources.entrySet()) {
            if (e.getKey().startsWith("META-INF/jars/") && e.getKey().endsWith(".jar")) {
                try (ZipInputStream zis = new ZipInputStream(new ByteArrayInputStream(e.getValue()))) {
                    ZipEntry ze;
                    while ((ze = zis.getNextEntry()) != null) {
                        if (ze.getName().endsWith(".class")) {
                            indexClassBytes(zis.readAllBytes(), map);
                        }
                    }
                } catch (IOException ignored) {}
            }
        }
        return map;
    }

    private static void indexZip(ZipFile zf, Map<String, ExtClassInfo> map) throws IOException {
        for (ZipEntry ze : Collections.list(zf.entries())) {
            if (!ze.getName().endsWith(".class")) continue;
            try (InputStream in = zf.getInputStream(ze)) {
                indexClassBytes(in.readAllBytes(), map);
            } catch (Exception ignored) {}
        }
    }

    private static void indexClassBytes(byte[] bytes, Map<String, ExtClassInfo> map) {
        ClassNode cn = new ClassNode();
        new ClassReader(bytes).accept(cn, ClassReader.SKIP_CODE | ClassReader.SKIP_DEBUG | ClassReader.SKIP_FRAMES);
        Set<String> keys = new HashSet<>();
        Set<String> allNameParams = new HashSet<>();
        for (MethodNode m : cn.methods) {
            if (m.name.equals("<init>") || m.name.equals("<clinit>")) continue;
            if ((m.access & Opcodes.ACC_PRIVATE) != 0) continue;
            String params = m.desc.substring(0, m.desc.indexOf(')') + 1);
            allNameParams.add(m.name + params);
            if ((m.access & Opcodes.ACC_STATIC) != 0) continue;
            keys.add(m.name + m.desc);
            String ret = m.desc.substring(m.desc.indexOf(')') + 1);
            // Only allow covariant return-type matching for non-primitive, non-void return types (e.g. Object/generic return)
            if (ret.startsWith("L") || ret.startsWith("[")) {
                keys.add("REF:" + m.name + params);
            }
        }
        map.putIfAbsent(cn.name, new ExtClassInfo(cn.superName, cn.interfaces == null ? List.of() : List.copyOf(cn.interfaces), keys, allNameParams));
    }

    private static ExtClassInfo resolveExternal(String internalName, Map<String, ExtClassInfo> extIndex) {
        ExtClassInfo info = extIndex.get(internalName);
        if (info != null) return info;
        // Fallback: load JDK class via Class.forName on boot/platform classloader
        try {
            Class<?> cls = Class.forName(internalName.replace('/', '.'), false, ClassLoader.getSystemClassLoader());
            Set<String> keys = new HashSet<>();
            Set<String> allNameParams = new HashSet<>();
            for (var m : cls.getDeclaredMethods()) {
                int mod = m.getModifiers();
                if (java.lang.reflect.Modifier.isPrivate(mod)) continue;
                String desc = Type.getMethodDescriptor(m);
                String params = desc.substring(0, desc.indexOf(')') + 1);
                allNameParams.add(m.getName() + params);
                if (java.lang.reflect.Modifier.isStatic(mod)) continue;
                keys.add(m.getName() + desc);
                String ret = desc.substring(desc.indexOf(')') + 1);
                if (ret.startsWith("L") || ret.startsWith("[")) {
                    keys.add("REF:" + m.getName() + params);
                }
            }
            String sup = cls.getSuperclass() == null ? null : Type.getInternalName(cls.getSuperclass());
            List<String> ifaces = new ArrayList<>();
            for (Class<?> itf : cls.getInterfaces()) ifaces.add(Type.getInternalName(itf));
            info = new ExtClassInfo(sup, ifaces, keys, allNameParams);
            extIndex.put(internalName, info);
            return info;
        } catch (Throwable ignored) {
            extIndex.put(internalName, new ExtClassInfo(null, List.of(), Set.of(), Set.of()));
            return extIndex.get(internalName);
        }
    }

    private static Map<String, String> computeClassRenames(Map<String, ClassNode> classNodes, Set<String> integrityClasses) {
        Set<String> allPackages = new HashSet<>();
        for (String cn : classNodes.keySet()) {
            int slash = cn.lastIndexOf('/');
            while (slash > 0) {
                String pkg = cn.substring(0, slash);
                allPackages.add(pkg);
                slash = pkg.lastIndexOf('/');
            }
        }
        // Top-level renames first (keyword names or class == package name)
        Map<String, String> topRenames = new HashMap<>();
        Map<String, Set<String>> pkgTopSimpleNames = new HashMap<>();
        for (String cn : classNodes.keySet()) {
            if (cn.contains("$")) continue;
            int slash = cn.lastIndexOf('/');
            String pkg = slash < 0 ? "" : cn.substring(0, slash + 1);
            String simple = slash < 0 ? cn : cn.substring(slash + 1);
            String mappedSimple = simple;
            if (!integrityClasses.contains(cn)) {
                if (KEYWORDS.contains(simple)) {
                    mappedSimple = "Kw_" + simple;
                    topRenames.put(cn, pkg + mappedSimple);
                } else if (allPackages.contains(cn)) {
                    mappedSimple = "PkgCls_" + simple;
                    topRenames.put(cn, pkg + mappedSimple);
                }
            }
            pkgTopSimpleNames.computeIfAbsent(pkg, k -> new HashSet<>()).add(mappedSimple);
        }

        // Sort classes by nesting depth (parent BEFORE children!) then lexicographically
        List<String> ordered = new ArrayList<>(classNodes.keySet());
        ordered.sort(Comparator.comparingInt((String s) -> (int) s.chars().filter(c -> c == '$').count()).thenComparing(Comparator.naturalOrder()));

        Map<String, String> classMap = new LinkedHashMap<>();
        Set<String> usedNames = new HashSet<>(classNodes.keySet());
        for (String orig : ordered) {
            if (!orig.contains("$")) {
                String mapped = topRenames.get(orig);
                if (mapped != null && !mapped.equals(orig)) {
                    classMap.put(orig, mapped);
                    usedNames.add(mapped);
                }
                continue;
            }
            int slash = orig.lastIndexOf('/');
            String pkg = slash < 0 ? "" : orig.substring(0, slash + 1);
            int lastDollar = orig.lastIndexOf('$');
            String origParent = orig.substring(0, lastDollar);
            String seg = orig.substring(lastDollar + 1);

            // Reuse parent's ALREADY-COMPUTED mapped name! (Fixes Inner_StreamUtil vs Inner_StreamUtil_2 bug!)
            String mappedParent = classMap.getOrDefault(origParent, origParent);
            // If origParent wasn't a class in classNodes (rare synthetic), walk parts fallback
            if (!classNodes.containsKey(origParent)) {
                String fullParentSimple = slash < 0 ? origParent : origParent.substring(slash + 1);
                String[] pParts = fullParentSimple.split("\\$", -1);
                StringBuilder pb = new StringBuilder(pkg).append(topRenames.getOrDefault(pkg + pParts[0], pkg + pParts[0]).substring(pkg.length()));
                for (int i = 1; i < pParts.length; i++) {
                    String prefix = pkg + String.join("$", Arrays.copyOf(pParts, i + 1));
                    if (classMap.containsKey(prefix)) {
                        pb = new StringBuilder(classMap.get(prefix));
                    } else {
                        pb.append('$').append(pParts[i]);
                    }
                }
                mappedParent = pb.toString();
            }

            // Collect enclosing simple names along mappedParent
            String parentFullSimple = slash < 0 ? mappedParent : mappedParent.substring(slash + 1);
            Set<String> seenEnclosing = new HashSet<>(Arrays.asList(parentFullSimple.split("\\$", -1)));
            Set<String> pkgTops = pkgTopSimpleNames.getOrDefault(pkg, Set.of());

            if (!seg.matches("\\d+")) {
                if (KEYWORDS.contains(seg)) {
                    seg = "Kw_" + seg;
                }
                // Rename if inner simple name matches ANY enclosing class OR any top-level class in the same package!
                if (seenEnclosing.contains(seg) || pkgTops.contains(seg)) {
                    String candidate = "Inner_" + seg;
                    int n = 2;
                    while (seenEnclosing.contains(candidate) || pkgTops.contains(candidate) || usedNames.contains(mappedParent + "$" + candidate)) {
                        candidate = "Inner_" + seg + "_" + (n++);
                    }
                    seg = candidate;
                }
            }
            String mapped = mappedParent + "$" + seg;
            if (!mapped.equals(orig)) {
                classMap.put(orig, mapped);
                usedNames.add(mapped);
            }
        }
        return classMap;
    }

    private static Map<String, String> computeFieldRenames(Map<String, ClassNode> classNodes, Map<String, String> classMap) {
        // Collect short (<=2 char) top-level simple class names across the JAR so fields like `a`, `b`, `g` don't shadow imported classes!
        Set<String> shortTopClassNames = new HashSet<>();
        for (String orig : classNodes.keySet()) {
            if (orig.contains("$")) continue;
            String mapped = classMap.getOrDefault(orig, orig);
            int slash = mapped.lastIndexOf('/');
            String simple = slash < 0 ? mapped : mapped.substring(slash + 1);
            if (simple.length() <= 2) {
                shortTopClassNames.add(simple);
            }
        }

        // Order classes superclass-first so mapped ancestor field names are known when processing subclasses
        List<ClassNode> orderedClasses = new ArrayList<>(classNodes.values());
        Map<String, Integer> depthCache = new HashMap<>();
        for (ClassNode cn : orderedClasses) {
            int d = 0;
            String sup = cn.superName;
            Set<String> seen = new HashSet<>();
            while (sup != null && seen.add(sup) && classNodes.containsKey(sup)) {
                d++;
                sup = classNodes.get(sup).superName;
            }
            depthCache.put(cn.name, d);
        }
        orderedClasses.sort(Comparator.comparingInt((ClassNode c) -> depthCache.get(c.name)).thenComparing(c -> c.name));

        Map<String, String> fieldMap = new LinkedHashMap<>();
        for (ClassNode cn : orderedClasses) {
            boolean isRecord = "java/lang/Record".equals(cn.superName);
            Set<String> ancFields = new HashSet<>();
            String sup = cn.superName;
            Set<String> seen = new HashSet<>();
            while (sup != null && seen.add(sup)) {
                ClassNode sn = classNodes.get(sup);
                if (sn == null) break;
                for (FieldNode sf : sn.fields) {
                    ancFields.add(sf.name);
                    String mappedSf = fieldMap.get(sn.name + "\t" + sf.name + "\t" + sf.desc);
                    if (mappedSf != null) ancFields.add(mappedSf);
                }
                sup = sn.superName;
            }
            Set<String> used = new HashSet<>(ancFields);
            Map<String, List<FieldNode>> byName = new LinkedHashMap<>();
            for (FieldNode f : cn.fields) {
                byName.computeIfAbsent(f.name, k -> new ArrayList<>()).add(f);
                used.add(f.name);
            }
            for (var entry : byName.entrySet()) {
                String name = entry.getKey();
                List<FieldNode> list = entry.getValue();
                boolean isKw = KEYWORDS.contains(name);
                boolean shadowsAnc = ancFields.contains(name);
                boolean shadowsShortClass = !isRecord && name.length() <= 2 && shortTopClassNames.contains(name);
                if (list.size() <= 1 && !isKw && !shadowsAnc && !shadowsShortClass) continue;
                // Deterministic order by descriptor
                list.sort(Comparator.comparing(f -> f.desc));
                for (FieldNode f : list) {
                    String prefix = isKw ? "kw_" + name : (shadowsShortClass ? "f_" + name : name);
                    String base = prefix + "_" + shortTypeTag(f.desc);
                    String candidate = base;
                    int idx = 2;
                    while (used.contains(candidate) || shortTopClassNames.contains(candidate)) {
                        candidate = base + "_" + (idx++);
                    }
                    used.add(candidate);
                    fieldMap.put(cn.name + "\t" + f.name + "\t" + f.desc, candidate);
                }
            }
        }
        return fieldMap;
    }

    // Union-Find for method inheritance groups
    static final class DSU {
        final Map<String, String> parent = new HashMap<>();
        String find(String x) {
            parent.putIfAbsent(x, x);
            String p = parent.get(x);
            if (!p.equals(x)) {
                p = find(p);
                parent.put(x, p);
            }
            return p;
        }
        void union(String a, String b) {
            String ra = find(a), rb = find(b);
            if (!ra.equals(rb)) parent.put(ra, rb);
        }
    }

    private static Map<String, String> computeMethodRenames(Map<String, ClassNode> classNodes, Map<String, ExtClassInfo> extIndex, Map<String, String> fieldMap) {
        // 1. Build class -> all ancestors (internal + external)
        Map<String, Set<String>> allInternalAncestors = new HashMap<>();
        Map<String, Set<String>> allExternalAncestors = new HashMap<>();
        for (String cn : classNodes.keySet()) {
            Set<String> intAnc = new LinkedHashSet<>();
            Set<String> extAnc = new LinkedHashSet<>();
            Deque<String> q = new ArrayDeque<>();
            ClassNode node = classNodes.get(cn);
            if (node.superName != null) q.add(node.superName);
            if (node.interfaces != null) q.addAll(node.interfaces);
            Set<String> visited = new HashSet<>();
            while (!q.isEmpty()) {
                String cur = q.poll();
                if (!visited.add(cur)) continue;
                ClassNode inNode = classNodes.get(cur);
                if (inNode != null) {
                    intAnc.add(cur);
                    if (inNode.superName != null) q.add(inNode.superName);
                    if (inNode.interfaces != null) q.addAll(inNode.interfaces);
                } else {
                    extAnc.add(cur);
                    ExtClassInfo ei = resolveExternal(cur, extIndex);
                    if (ei.superName != null) q.add(ei.superName);
                    q.addAll(ei.interfaces);
                }
            }
            allInternalAncestors.put(cn, intAnc);
            allExternalAncestors.put(cn, extAnc);
        }

        // 2. Index all declared non-bridge methods per class
        Map<String, Map<String, MethodNode>> declared = new HashMap<>();
        for (ClassNode cn : classNodes.values()) {
            Map<String, MethodNode> mMap = new LinkedHashMap<>();
            for (MethodNode m : cn.methods) {
                if (m.name.equals("<init>") || m.name.equals("<clinit>")) continue;
                mMap.put(m.name + "\t" + m.desc, m);
            }
            declared.put(cn.name, mMap);
        }

        // 3. Union virtual/interface methods across internal inheritance & pin methods overriding external types
        DSU dsu = new DSU();
        Set<String> pinnedGroups = new HashSet<>();

        // Also pin all Mixin methods, Record accessor methods, Enum synthetic methods (values/valueOf), and autismclient/api/* public methods
        Set<String> pinnedOwners = new HashSet<>();
        for (ClassNode cn : classNodes.values()) {
            boolean isMixin = false;
            if (cn.invisibleAnnotations != null) {
                for (AnnotationNode ann : cn.invisibleAnnotations) {
                    if ("Lorg/spongepowered/asm/mixin/Mixin;".equals(ann.desc)) isMixin = true;
                }
            }
            if (isMixin || cn.name.startsWith("autismclient/api/")) {
                pinnedOwners.add(cn.name);
            }
        }

        for (ClassNode cn : classNodes.values()) {
            boolean isEnum = (cn.access & Opcodes.ACC_ENUM) != 0;
            boolean isRecord = "java/lang/Record".equals(cn.superName);
            for (MethodNode m : cn.methods) {
                if (m.name.equals("<init>") || m.name.equals("<clinit>")) continue;
                String id = cn.name + "\t" + m.name + "\t" + m.desc;
                dsu.find(id);
                boolean isPrivateOrStatic = (m.access & (Opcodes.ACC_PRIVATE | Opcodes.ACC_STATIC)) != 0;
                String params = m.desc.substring(0, m.desc.indexOf(')') + 1);

                if (pinnedOwners.contains(cn.name)
                        || (isEnum && (m.name.equals("values") || m.name.equals("valueOf")))
                        || (isRecord && (m.name.equals("equals") || m.name.equals("hashCode") || m.name.equals("toString")))) {
                    pinnedGroups.add(id);
                }

                if (!isPrivateOrStatic) {
                    String ret = m.desc.substring(m.desc.indexOf(')') + 1);
                    boolean isRefRet = ret.startsWith("L") || ret.startsWith("[");
                    // Check external ancestors (exact descriptor match, or covariant ref return match)
                    for (String ext : allExternalAncestors.get(cn.name)) {
                        ExtClassInfo ei = resolveExternal(ext, extIndex);
                        if (ei.methodKeys.contains(m.name + m.desc) || (isRefRet && ei.methodKeys.contains("REF:" + m.name + params))) {
                            pinnedGroups.add(id);
                            break;
                        }
                    }
                    // Union with matching methods in internal ancestors (same name + same desc, or bridge-covariant same name+params)
                    for (String anc : allInternalAncestors.get(cn.name)) {
                        Map<String, MethodNode> ancMethods = declared.get(anc);
                        MethodNode exact = ancMethods.get(m.name + "\t" + m.desc);
                        if (exact != null && (exact.access & (Opcodes.ACC_PRIVATE | Opcodes.ACC_STATIC)) == 0) {
                            dsu.union(id, anc + "\t" + exact.name + "\t" + exact.desc);
                        }
                    }
                }
            }
        }

        // Propagate pin flag to DSU roots
        Set<String> pinnedRoots = new HashSet<>();
        for (String p : pinnedGroups) pinnedRoots.add(dsu.find(p));

        // 4. Collect members per DSU root
        Map<String, List<String>> rootToMembers = new LinkedHashMap<>();
        for (String id : dsu.parent.keySet()) {
            rootToMembers.computeIfAbsent(dsu.find(id), k -> new ArrayList<>()).add(id);
        }

        // Build reverse map: internal class -> all internal descendants (subclasses/implementors)
        Map<String, Set<String>> allInternalDescendants = new HashMap<>();
        for (var e : allInternalAncestors.entrySet()) {
            String sub = e.getKey();
            for (String anc : e.getValue()) {
                allInternalDescendants.computeIfAbsent(anc, k -> new HashSet<>()).add(sub);
            }
        }

        // Helper lookup: methodId -> MethodNode
        Map<String, MethodNode> methodById = new HashMap<>();
        for (ClassNode cn : classNodes.values()) {
            for (MethodNode m : cn.methods) {
                if (m.name.equals("<init>") || m.name.equals("<clinit>")) continue;
                methodById.put(cn.name + "\t" + m.name + "\t" + m.desc, m);
            }
        }

        // 5. Find all DSU roots involved in:
        //    (a) same-class or inherited (internal + external) name+params collision (including static/private/final clashes)
        //    (b) keyword method names
        //    (c) same-arity overload ambiguity (mixed static/instance, functional interfaces, numeric widening, varargs)
        Set<String> rootsToRename = new LinkedHashSet<>();
        Set<String> paramDisambigRoots = new LinkedHashSet<>();
        for (ClassNode cn : classNodes.values()) {
            Map<String, List<String>> byNameParams = new LinkedHashMap<>();
            Map<String, List<String>> byNameArity = new LinkedHashMap<>();
            Map<String, List<String>> byNameAll = new LinkedHashMap<>();

            // Add declared non-bridge methods
            for (MethodNode m : cn.methods) {
                if (m.name.equals("<init>") || m.name.equals("<clinit>")) continue;
                if ((m.access & Opcodes.ACC_BRIDGE) != 0) continue;
                String id = cn.name + "\t" + m.name + "\t" + m.desc;
                String root = dsu.find(id);
                if (KEYWORDS.contains(m.name)) {
                    rootsToRename.add(root);
                }
                String params = m.desc.substring(0, m.desc.indexOf(')') + 1);
                byNameParams.computeIfAbsent(m.name + params, k -> new ArrayList<>()).add(id);

                int arity = Type.getArgumentTypes(m.desc).length;
                byNameArity.computeIfAbsent(m.name + "#" + arity, k -> new ArrayList<>()).add(id);
                byNameAll.computeIfAbsent(m.name, k -> new ArrayList<>()).add(id);

                // Check illegal conflict against external ancestors (e.g. private int init(int,int) vs Screen.init(int,int)V)
                if (!pinnedRoots.contains(root)) {
                    for (String ext : allExternalAncestors.get(cn.name)) {
                        ExtClassInfo ei = resolveExternal(ext, extIndex);
                        if (ei.allNameParams.contains(m.name + params)) {
                            rootsToRename.add(root);
                            break;
                        }
                    }
                }
            }

            // Include non-private internal ancestor methods (INCLUDING static!) for conflict & overload checks
            for (String anc : allInternalAncestors.get(cn.name)) {
                ClassNode ancNode = classNodes.get(anc);
                for (MethodNode am : ancNode.methods) {
                    if (am.name.equals("<init>") || am.name.equals("<clinit>")) continue;
                    if ((am.access & (Opcodes.ACC_PRIVATE | Opcodes.ACC_BRIDGE)) != 0) continue;
                    String params = am.desc.substring(0, am.desc.indexOf(')') + 1);
                    String ancId = anc + "\t" + am.name + "\t" + am.desc;
                    String ancRoot = dsu.find(ancId);

                    List<String> list = byNameParams.get(am.name + params);
                    if (list != null) {
                        boolean alreadySameRoot = false;
                        for (String existing : list) {
                            if (dsu.find(existing).equals(ancRoot)) { alreadySameRoot = true; break; }
                        }
                        if (!alreadySameRoot) {
                            // Check if this is a legitimate Java covariant override (i.e. cn has a bridge method with anc's exact desc)
                            boolean hasBridge = declared.get(cn.name).containsKey(am.name + "\t" + am.desc)
                                && (declared.get(cn.name).get(am.name + "\t" + am.desc).access & Opcodes.ACC_BRIDGE) != 0;
                            if (!hasBridge) {
                                list.add(ancId);
                            }
                        }
                    }

                    int arity = Type.getArgumentTypes(am.desc).length;
                    List<String> arityList = byNameArity.get(am.name + "#" + arity);
                    if (arityList != null) {
                        arityList.add(ancId);
                    }
                    List<String> nameList = byNameAll.get(am.name);
                    if (nameList != null) {
                        nameList.add(ancId);
                    }
                }
            }

            // 5a. Exact name+params collisions (different return types, private/static clashes)
            for (var entry : byNameParams.entrySet()) {
                Set<String> distinctRoots = new LinkedHashSet<>();
                for (String id : entry.getValue()) distinctRoots.add(dsu.find(id));
                if (distinctRoots.size() > 1) {
                    for (String root : distinctRoots) {
                        if (!pinnedRoots.contains(root)) {
                            rootsToRename.add(root);
                        }
                    }
                }
            }

            // 5b. Same-arity overload ambiguity (mixed static/instance, functional interfaces, numeric primitive widening)
            for (var entry : byNameArity.entrySet()) {
                String key = entry.getKey();
                int arity = Integer.parseInt(key.substring(key.indexOf('#') + 1));
                if (arity == 0) continue;
                Map<String, MethodNode> rootSample = new LinkedHashMap<>();
                for (String id : entry.getValue()) {
                    rootSample.putIfAbsent(dsu.find(id), methodById.get(id));
                }
                if (rootSample.size() <= 1) continue;

                boolean hasStatic = false, hasInstance = false;
                for (MethodNode mn : rootSample.values()) {
                    if ((mn.access & Opcodes.ACC_STATIC) != 0) hasStatic = true;
                    else hasInstance = true;
                }
                boolean ambiguous = (hasStatic && hasInstance);
                if (!ambiguous) {
                    // Check per argument position for functional-interface or numeric-primitive ambiguity
                    for (int argIdx = 0; argIdx < arity && !ambiguous; argIdx++) {
                        Set<String> funcTypes = new HashSet<>();
                        Set<String> numPrimTypes = new HashSet<>();
                        for (MethodNode mn : rootSample.values()) {
                            Type argT = Type.getArgumentTypes(mn.desc)[argIdx];
                            if (isFunctionalInterfaceType(argT)) {
                                funcTypes.add(argT.getDescriptor());
                            } else if (isNumericPrimitive(argT)) {
                                numPrimTypes.add(argT.getDescriptor());
                            }
                        }
                        if (funcTypes.size() >= 2 || numPrimTypes.size() >= 2) {
                            ambiguous = true;
                        }
                    }
                }
                if (ambiguous) {
                    for (String root : rootSample.keySet()) {
                        if (!pinnedRoots.contains(root)) {
                            rootsToRename.add(root);
                            paramDisambigRoots.add(root);
                        }
                    }
                }
            }

            // 5c. Varargs overload ambiguity (e.g. init(Object...) vs init(Object, int, String...))
            for (var entry : byNameAll.entrySet()) {
                Map<String, MethodNode> rootSample = new LinkedHashMap<>();
                boolean hasVarargs = false;
                for (String id : entry.getValue()) {
                    MethodNode mn = methodById.get(id);
                    rootSample.putIfAbsent(dsu.find(id), mn);
                    if ((mn.access & Opcodes.ACC_VARARGS) != 0) hasVarargs = true;
                }
                if (hasVarargs && rootSample.size() > 1) {
                    for (String root : rootSample.keySet()) {
                        if (!pinnedRoots.contains(root)) {
                            rootsToRename.add(root);
                            paramDisambigRoots.add(root);
                        }
                    }
                }
            }
        }

        // 6. Track used method (name + params) AND (name + "#" + arity) per class so new names never introduce a secondary collision
        Map<String, Set<String>> usedByClass = new HashMap<>();
        Map<String, Set<String>> usedArityByClass = new HashMap<>();
        for (ClassNode cn : classNodes.values()) {
            Set<String> u = new HashSet<>();
            Set<String> ua = new HashSet<>();
            for (MethodNode m : cn.methods) {
                String params = m.desc.substring(0, m.desc.indexOf(')') + 1);
                u.add(m.name + params);
                ua.add(m.name + "#" + Type.getArgumentTypes(m.desc).length);
            }
            // Reserve external ancestor signatures too!
            for (String ext : allExternalAncestors.get(cn.name)) {
                ExtClassInfo ei = resolveExternal(ext, extIndex);
                u.addAll(ei.allNameParams);
            }
            usedByClass.put(cn.name, u);
            usedArityByClass.put(cn.name, ua);
        }

        // 7. Assign deterministic clean names to every root in rootsToRename
        Map<String, String> methodMap = new LinkedHashMap<>();

        // 7a. First sync Java Record PUBLIC component accessor methods to match their remapped field/component names,
        //     and ensure NO non-public 0-arg method collides with a record component name!
        for (ClassNode cn : classNodes.values()) {
            if (!"java/lang/Record".equals(cn.superName) || cn.recordComponents == null) continue;
            Set<String> mappedComponentNames = new HashSet<>();
            for (RecordComponentNode rc : cn.recordComponents) {
                String fKey = cn.name + "\t" + rc.name + "\t" + rc.descriptor;
                String mappedField = fieldMap.getOrDefault(fKey, rc.name);
                mappedComponentNames.add(mappedField);
                if (!mappedField.equals(rc.name)) {
                    String mId = cn.name + "\t" + rc.name + "\t()" + rc.descriptor;
                    MethodNode accessor = methodById.get(mId);
                    if (accessor != null && (accessor.access & Opcodes.ACC_PUBLIC) != 0) {
                        String root = dsu.find(mId);
                        if (!pinnedRoots.contains(root) && rootToMembers.containsKey(root)) {
                            for (String mem : rootToMembers.get(root)) {
                                methodMap.put(mem, mappedField);
                                usedByClass.get(mem.split("\t")[0]).add(mappedField + "()");
                                usedArityByClass.get(mem.split("\t")[0]).add(mappedField + "#0");
                            }
                            rootsToRename.remove(root);
                        }
                    }
                }
            }
            // Reserve all mapped component names + "()" so no non-public helper method can steal a component accessor name
            for (String compName : mappedComponentNames) {
                usedByClass.get(cn.name).add(compName + "()");
                usedArityByClass.get(cn.name).add(compName + "#0");
            }
            for (MethodNode m : cn.methods) {
                if (m.name.equals("<init>") || m.name.equals("<clinit>")) continue;
                if ((m.access & Opcodes.ACC_PUBLIC) == 0 && m.desc.startsWith("()")) {
                    String mId = cn.name + "\t" + m.name + "\t" + m.desc;
                    if (mappedComponentNames.contains(m.name)) {
                        String root = dsu.find(mId);
                        if (!pinnedRoots.contains(root)) {
                            rootsToRename.add(root);
                        }
                    }
                }
            }
        }

        List<String> sortedRoots = new ArrayList<>(rootsToRename);
        Collections.sort(sortedRoots);
        for (String root : sortedRoots) {
            if (pinnedRoots.contains(root)) continue;
            List<String> members = rootToMembers.get(root);
            String[] sample = members.getFirst().split("\t");
            String origName = sample[1];
            String origDesc = sample[2];
            String params = origDesc.substring(0, origDesc.indexOf(')') + 1);
            String retDesc = origDesc.substring(origDesc.indexOf(')') + 1);
            Type[] argTypes = Type.getArgumentTypes(origDesc);
            int arity = argTypes.length;

            String prefix = KEYWORDS.contains(origName) ? "kw_" + origName : origName;
            String base;
            if (paramDisambigRoots.contains(root) && arity > 0) {
                // Include first parameter tag (+ second if needed) so same-arity overloads get distinct readable names!
                StringBuilder pb = new StringBuilder(prefix).append('_').append(shortTypeTag(retDesc)).append('_').append(shortTypeTag(argTypes[0].getDescriptor()));
                if (arity == 2) {
                    pb.append('_').append(shortTypeTag(argTypes[1].getDescriptor()));
                }
                base = pb.toString();
            } else {
                base = prefix + "_" + shortTypeTag(retDesc);
            }

            // Collect all classes touched by this group (owners + all internal ancestors + all internal descendants!)
            Set<String> touchedClasses = new HashSet<>();
            for (String mem : members) {
                String owner = mem.split("\t")[0];
                touchedClasses.add(owner);
                touchedClasses.addAll(allInternalAncestors.getOrDefault(owner, Set.of()));
                touchedClasses.addAll(allInternalDescendants.getOrDefault(owner, Set.of()));
            }

            boolean checkArity = paramDisambigRoots.contains(root);
            String candidate = base;
            int idx = 2;
            while (conflictsInAny(touchedClasses, candidate + params, usedByClass)
                    || (checkArity && conflictsInAny(touchedClasses, candidate + "#" + arity, usedArityByClass))) {
                candidate = base + "_" + (idx++);
            }
            for (String cls : touchedClasses) {
                Set<String> u = usedByClass.get(cls);
                if (u != null) u.add(candidate + params);
                Set<String> ua = usedArityByClass.get(cls);
                if (ua != null) ua.add(candidate + "#" + arity);
            }
            for (String mem : members) {
                methodMap.put(mem, candidate);
            }
        }
        return methodMap;
    }

    private static boolean isFunctionalInterfaceType(Type t) {
        if (t.getSort() != Type.OBJECT) return false;
        String name = t.getInternalName();
        return name.startsWith("java/util/function/")
            || name.equals("java/lang/Runnable")
            || name.equals("java/util/Comparator")
            || name.equals("java/util/concurrent/Callable");
    }

    private static boolean isNumericPrimitive(Type t) {
        return switch (t.getSort()) {
            case Type.BYTE, Type.SHORT, Type.INT, Type.LONG, Type.FLOAT, Type.DOUBLE -> true;
            default -> false;
        };
    }

    private static boolean conflictsInAny(Set<String> classes, String nameParams, Map<String, Set<String>> usedByClass) {
        for (String c : classes) {
            Set<String> u = usedByClass.get(c);
            if (u != null && u.contains(nameParams)) return true;
        }
        return false;
    }

    private static String shortTypeTag(String desc) {
        int dims = 0;
        while (dims < desc.length() && desc.charAt(dims) == '[') dims++;
        String core = desc.substring(dims);
        String base = switch (core.charAt(0)) {
            case 'V' -> "V";
            case 'Z' -> "Z";
            case 'B' -> "B";
            case 'C' -> "C";
            case 'S' -> "S";
            case 'I' -> "I";
            case 'J' -> "J";
            case 'F' -> "F";
            case 'D' -> "D";
            case 'L' -> {
                String internal = core.substring(1, core.length() - 1);
                String simple = internal.contains("/") ? internal.substring(internal.lastIndexOf('/') + 1) : internal;
                simple = simple.replace('$', '_');
                if (simple.equals("String")) yield "Str";
                if (simple.equals("Object")) yield "Obj";
                if (simple.length() > 18) simple = simple.substring(0, 18);
                yield simple;
            }
            default -> "T";
        };
        return dims > 0 ? base + "Arr" + (dims > 1 ? dims : "") : base;
    }

    private static byte[] remapClass(byte[] raw, Remapper remapper) {
        ClassReader cr = new ClassReader(raw);
        ClassWriter cw = new ClassWriter(0);
        String origOwner = cr.getClassName();
        ClassVisitor cv = new ClassRemapper(cw, remapper) {
            @Override
            public RecordComponentVisitor visitRecordComponent(String name, String descriptor, String signature) {
                String mappedName = remapper.mapFieldName(origOwner, name, descriptor);
                return super.visitRecordComponent(mappedName, descriptor, signature);
            }

            @Override
            public MethodVisitor visitMethod(int access, String name, String descriptor, String signature, String[] exceptions) {
                MethodVisitor mv = super.visitMethod(access, name, descriptor, signature, exceptions);
                return new MethodVisitor(Opcodes.ASM9, mv) {
                    @Override
                    public void visitInvokeDynamicInsn(String name, String descriptor, Handle bsm, Object... bsmArgs) {
                        if ("java/lang/runtime/ObjectMethods".equals(bsm.getOwner())
                                && "bootstrap".equals(bsm.getName())
                                && bsmArgs.length >= 2 && bsmArgs[1] instanceof String namesStr) {
                            // Rebuild semicolon-separated record component names from the already-remapped getter Handles in bsmArgs[2..]
                            StringBuilder sb = new StringBuilder();
                            for (int i = 2; i < bsmArgs.length; i++) {
                                if (i > 2) sb.append(';');
                                if (bsmArgs[i] instanceof Handle h) {
                                    sb.append(remapper.mapFieldName(h.getOwner(), h.getName(), h.getDesc()));
                                }
                            }
                            Object[] updated = Arrays.copyOf(bsmArgs, bsmArgs.length);
                            updated[1] = sb.toString();
                            super.visitInvokeDynamicInsn(name, descriptor, bsm, updated);
                            return;
                        }
                        super.visitInvokeDynamicInsn(name, descriptor, bsm, bsmArgs);
                    }
                };
            }

            @Override
            public void visitInnerClass(String name, String outerName, String innerName, int access) {
                String mappedName = remapper.mapType(name);
                String mappedOuter = outerName == null ? null : remapper.mapType(outerName);
                String mappedInner = innerName;
                if (innerName != null && mappedName != null) {
                    int dollar = mappedName.lastIndexOf('$');
                    if (dollar >= 0 && dollar + 1 < mappedName.length()) {
                        mappedInner = mappedName.substring(dollar + 1);
                    }
                }
                super.cv.visitInnerClass(mappedName, mappedOuter, mappedInner, access);
            }
        };
        cr.accept(cv, 0);
        return cw.toByteArray();
    }

    static final class HierarchyRemapper extends Remapper {
        private final Map<String, ClassNode> classNodes;
        private final Map<String, String> classMap;
        private final Map<String, String> fieldMap;
        private final Map<String, String> methodMap;

        HierarchyRemapper(Map<String, ClassNode> classNodes, Map<String, String> classMap,
                          Map<String, String> fieldMap, Map<String, String> methodMap) {
            this.classNodes = classNodes;
            this.classMap = classMap;
            this.fieldMap = fieldMap;
            this.methodMap = methodMap;
        }

        @Override
        public String map(String internalName) {
            return classMap.getOrDefault(internalName, internalName);
        }

        @Override
        public String mapFieldName(String owner, String name, String descriptor) {
            String direct = fieldMap.get(owner + "\t" + name + "\t" + descriptor);
            if (direct != null) return direct;
            // Walk superclasses & interfaces in our JAR
            Deque<String> q = new ArrayDeque<>();
            Set<String> seen = new HashSet<>();
            q.add(owner);
            while (!q.isEmpty()) {
                String cur = q.poll();
                if (!seen.add(cur)) continue;
                String hit = fieldMap.get(cur + "\t" + name + "\t" + descriptor);
                if (hit != null) return hit;
                ClassNode cn = classNodes.get(cur);
                if (cn != null) {
                    if (cn.superName != null) q.add(cn.superName);
                    if (cn.interfaces != null) q.addAll(cn.interfaces);
                }
            }
            return name;
        }

        @Override
        public String mapMethodName(String owner, String name, String descriptor) {
            if (name.equals("<init>") || name.equals("<clinit>")) return name;
            String direct = methodMap.get(owner + "\t" + name + "\t" + descriptor);
            if (direct != null) return direct;
            Deque<String> q = new ArrayDeque<>();
            Set<String> seen = new HashSet<>();
            q.add(owner);
            while (!q.isEmpty()) {
                String cur = q.poll();
                if (!seen.add(cur)) continue;
                String hit = methodMap.get(cur + "\t" + name + "\t" + descriptor);
                if (hit != null) return hit;
                ClassNode cn = classNodes.get(cur);
                if (cn != null) {
                    if (cn.superName != null) q.add(cn.superName);
                    if (cn.interfaces != null) q.addAll(cn.interfaces);
                }
            }
            return name;
        }
    }
}

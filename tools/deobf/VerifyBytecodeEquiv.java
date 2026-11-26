import java.io.*;
import java.nio.file.*;
import java.security.MessageDigest;
import java.util.*;
import java.util.jar.*;
import java.util.regex.*;
import org.objectweb.asm.*;
import org.objectweb.asm.tree.*;
import org.objectweb.asm.util.Textifier;
import org.objectweb.asm.util.TraceMethodVisitor;

/**
 * Strict class-by-class & resource semantic equivalence verifier between two packaged mod JARs.
 * Checks:
 *  - Identical class set AND byte-identical non-class resources
 *  - Identical SHA-256 on all excluded/frozen integrity classes (without inspecting their bytecode)
 *  - Identical classfile version, access, superName, interfaces, generic signatures, OuterClass/EnclosingMethod,
 *    InnerClasses, NestHost, NestMembers, PermittedSubclasses, visible/invisible/type/parameter annotations,
 *    annotation defaults, record components, fields, methods, exceptions, and bytecode instructions
 *  - Strict 1-to-1 bijection validation for javac synthetic $SwitchMap$ field renames (never masks swapped tables)
 */
public final class VerifyBytecodeEquiv {
    private static final Set<String> EXCLUDED_EXACT = Set.of(
        "autismclient/modules/al",
        "autismclient/util/AutismLiteVariant",
        "autismclient/util/bX",
        "autismclient/gui/a/DataUtil",
        "autismclient/gui/d/JsonUtil"
    );
    private static final Pattern SWITCHMAP_REF = Pattern.compile("\\.\\$SwitchMap\\$[A-Za-z0-9_$]+");

    private static boolean isExcluded(String name) {
        String base = name.contains("$") ? name.substring(0, name.indexOf('$')) : name;
        if (EXCLUDED_EXACT.contains(base)) return true;
        return name.startsWith("autismclient/e/")
            || name.startsWith("autismclient/sys/")
            || name.startsWith("autismclient/obf/")
            || name.startsWith("autismclient/a/")
            || name.startsWith("autismclient/util/mm/guardian/");
    }

    private static Map<String, byte[]> readEntries(Path jar, boolean classesOnly) throws Exception {
        Map<String, byte[]> map = new TreeMap<>();
        try (JarFile jf = new JarFile(jar.toFile())) {
            Enumeration<JarEntry> en = jf.entries();
            while (en.hasMoreElements()) {
                JarEntry e = en.nextElement();
                if (e.isDirectory()) continue;
                boolean isCls = e.getName().endsWith(".class");
                if (classesOnly && isCls) {
                    try (InputStream in = jf.getInputStream(e)) {
                        map.put(e.getName().substring(0, e.getName().length() - 6), in.readAllBytes());
                    }
                } else if (!classesOnly && !isCls) {
                    try (InputStream in = jf.getInputStream(e)) {
                        map.put(e.getName(), in.readAllBytes());
                    }
                }
            }
        }
        return map;
    }

    private static String sha256(byte[] b) throws Exception {
        byte[] d = MessageDigest.getInstance("SHA-256").digest(b);
        StringBuilder sb = new StringBuilder(64);
        for (byte x : d) sb.append(String.format("%02x", x & 0xff));
        return sb.toString();
    }

    private static String formatAnnValue(Object v) {
        if (v == null) return "null";
        if (v instanceof AnnotationNode an) return formatAnn(an);
        if (v instanceof String[] arr) return "ENUM:" + Arrays.toString(arr);
        if (v instanceof Type t) return "TYPE:" + t.getDescriptor();
        if (v instanceof List<?> list) {
            StringBuilder sb = new StringBuilder("[");
            for (int i = 0; i < list.size(); i++) {
                if (i > 0) sb.append(", ");
                sb.append(formatAnnValue(list.get(i)));
            }
            return sb.append(']').toString();
        }
        if (v instanceof byte[] a) return Arrays.toString(a);
        if (v instanceof boolean[] a) return Arrays.toString(a);
        if (v instanceof short[] a) return Arrays.toString(a);
        if (v instanceof char[] a) return Arrays.toString(a);
        if (v instanceof int[] a) return Arrays.toString(a);
        if (v instanceof long[] a) return Arrays.toString(a);
        if (v instanceof float[] a) return Arrays.toString(a);
        if (v instanceof double[] a) return Arrays.toString(a);
        if (v instanceof Object[] a) {
            StringBuilder sb = new StringBuilder("[");
            for (int i = 0; i < a.length; i++) {
                if (i > 0) sb.append(", ");
                sb.append(formatAnnValue(a[i]));
            }
            return sb.append(']').toString();
        }
        return v.getClass().getSimpleName() + ":" + v;
    }

    private static String formatAnn(AnnotationNode a) {
        StringBuilder sb = new StringBuilder(a.desc).append('(');
        if (a.values != null) {
            List<String> pairs = new ArrayList<>();
            for (int i = 0; i + 1 < a.values.size(); i += 2) {
                pairs.add(a.values.get(i) + "=" + formatAnnValue(a.values.get(i + 1)));
            }
            Collections.sort(pairs);
            sb.append(String.join(", ", pairs));
        }
        return sb.append(')').toString();
    }

    private static void appendAnns(StringBuilder sb, String tag, List<AnnotationNode> anns) {
        if (anns == null || anns.isEmpty()) return;
        List<String> rendered = new ArrayList<>();
        for (AnnotationNode a : anns) rendered.add(formatAnn(a));
        Collections.sort(rendered);
        for (String r : rendered) sb.append(tag).append(r).append('\n');
    }

    private static void appendTypeAnns(StringBuilder sb, String tag, List<TypeAnnotationNode> anns) {
        if (anns == null || anns.isEmpty()) return;
        List<String> rendered = new ArrayList<>();
        for (TypeAnnotationNode a : anns) {
            rendered.add(a.typeRef + "@" + a.typePath + ":" + formatAnn(a));
        }
        Collections.sort(rendered);
        for (String r : rendered) sb.append(tag).append(r).append('\n');
    }

    private static void appendParamAnns(StringBuilder sb, String tag, List<AnnotationNode>[] paramAnns, int count) {
        if (paramAnns == null) return;
        sb.append(tag).append("CNT:").append(count).append('\n');
        for (int i = 0; i < paramAnns.length; i++) {
            appendAnns(sb, tag + i + ":", paramAnns[i]);
        }
    }

    static String fingerprintClass(byte[] raw, Map<String, String> switchMapMap) {
        ClassNode cn = new ClassNode();
        new ClassReader(raw).accept(cn, ClassReader.SKIP_DEBUG);
        StringBuilder sb = new StringBuilder();
        sb.append("H:").append(cn.version).append('|').append(cn.access).append('|').append(cn.name)
          .append('|').append(cn.signature).append('|').append(cn.superName)
          .append('|').append(cn.interfaces).append('|').append(cn.outerClass)
          .append('|').append(cn.outerMethod).append('|').append(cn.outerMethodDesc)
          .append('|').append(cn.nestHostClass).append('|').append(cn.permittedSubclasses).append('\n');
        if (cn.nestMembers != null) {
            List<String> nm = new ArrayList<>(cn.nestMembers);
            Collections.sort(nm);
            sb.append("NM:").append(nm).append('\n');
        }
        if (cn.innerClasses != null) {
            List<String> ics = new ArrayList<>();
            for (InnerClassNode ic : cn.innerClasses) {
                ics.add(ic.name + "|" + ic.outerName + "|" + ic.innerName + "|" + ic.access);
            }
            Collections.sort(ics);
            for (String ic : ics) sb.append("IC:").append(ic).append('\n');
        }
        appendAnns(sb, "AV:", cn.visibleAnnotations);
        appendAnns(sb, "AI:", cn.invisibleAnnotations);
        appendTypeAnns(sb, "TAV:", cn.visibleTypeAnnotations);
        appendTypeAnns(sb, "TAI:", cn.invisibleTypeAnnotations);
        if (cn.recordComponents != null) {
            for (RecordComponentNode rc : cn.recordComponents) {
                sb.append("RC:").append(rc.name).append(':').append(rc.descriptor).append(':').append(rc.signature).append('\n');
                appendAnns(sb, "RCAV:", rc.visibleAnnotations);
                appendAnns(sb, "RCAI:", rc.invisibleAnnotations);
                appendTypeAnns(sb, "RCTAV:", rc.visibleTypeAnnotations);
                appendTypeAnns(sb, "RCTAI:", rc.invisibleTypeAnnotations);
            }
        }
        List<FieldNode> fields = new ArrayList<>(cn.fields);
        fields.sort(Comparator.comparing((FieldNode f) -> mapSwitchMap(f.name, switchMapMap)).thenComparing(f -> f.desc));
        for (FieldNode f : fields) {
            sb.append("F:").append(f.access).append('|').append(mapSwitchMap(f.name, switchMapMap))
              .append('|').append(f.desc).append('|').append(f.signature).append('|').append(f.value).append('\n');
            appendAnns(sb, "FAV:", f.visibleAnnotations);
            appendAnns(sb, "FAI:", f.invisibleAnnotations);
            appendTypeAnns(sb, "FTAV:", f.visibleTypeAnnotations);
            appendTypeAnns(sb, "FTAI:", f.invisibleTypeAnnotations);
        }
        List<MethodNode> methods = new ArrayList<>(cn.methods);
        methods.sort(Comparator.comparing((MethodNode m) -> m.name).thenComparing(m -> m.desc));
        for (MethodNode m : methods) {
            sb.append("M:").append(m.access).append('|').append(m.name).append('|').append(m.desc)
              .append('|').append(m.signature).append('|').append(m.exceptions).append('\n');
            if (m.annotationDefault != null) {
                sb.append("MAD:").append(formatAnnValue(m.annotationDefault)).append('\n');
            }
            appendAnns(sb, "MAV:", m.visibleAnnotations);
            appendAnns(sb, "MAI:", m.invisibleAnnotations);
            appendTypeAnns(sb, "MTAV:", m.visibleTypeAnnotations);
            appendTypeAnns(sb, "MTAI:", m.invisibleTypeAnnotations);
            appendParamAnns(sb, "MPAV:", m.visibleParameterAnnotations, m.visibleAnnotableParameterCount);
            appendParamAnns(sb, "MPAI:", m.invisibleParameterAnnotations, m.invisibleAnnotableParameterCount);
            Textifier t = new Textifier();
            TraceMethodVisitor tmv = new TraceMethodVisitor(t);
            m.accept(tmv);
            StringWriter sw = new StringWriter();
            t.print(new PrintWriter(sw));
            for (String line : sw.toString().split("\n")) {
                String tr = line.trim();
                if (tr.startsWith("LINENUMBER ") || tr.startsWith("LOCALVARIABLE ")) continue;
                if (tr.contains(".$SwitchMap$")) {
                    Matcher mat = SWITCHMAP_REF.matcher(tr);
                    StringBuilder out = new StringBuilder();
                    while (mat.find()) {
                        String origField = mat.group().substring(1);
                        mat.appendReplacement(out, Matcher.quoteReplacement("." + mapSwitchMap(origField, switchMapMap)));
                    }
                    mat.appendTail(out);
                    tr = out.toString();
                }
                sb.append("  ").append(tr).append('\n');
            }
        }
        return sb.toString();
    }

    private static String mapSwitchMap(String fieldName, Map<String, String> switchMapMap) {
        if (!fieldName.startsWith("$SwitchMap$")) return fieldName;
        return switchMapMap.getOrDefault(fieldName, fieldName);
    }

    /**
     * Build a verified 1-to-1 bijection of javac synthetic $SwitchMap$ field names (cand -> base)
     * by matching each field's <clinit> enum owner inside synthetic switch-map classes.
     * Validates global injectivity across ALL $SwitchMap$ fields (including identity mappings).
     */
    private static Map<String, String> buildSwitchMapBijection(Map<String, byte[]> base, Map<String, byte[]> cand) {
        Map<String, String> allCandToBase = new HashMap<>();
        Map<String, String> renamedCandToBase = new HashMap<>();
        for (String name : base.keySet()) {
            if (isExcluded(name) || !cand.containsKey(name)) continue;
            Map<String, String> baseByEnum = extractSwitchMapEnums(name, base.get(name));
            if (baseByEnum.isEmpty()) continue;
            Map<String, String> candByEnum = extractSwitchMapEnums(name, cand.get(name));
            for (var e : candByEnum.entrySet()) {
                String baseField = baseByEnum.get(e.getKey());
                if (baseField != null) {
                    String candField = e.getValue();
                    String prev = allCandToBase.put(candField, baseField);
                    if (prev != null && !prev.equals(baseField)) {
                        throw new IllegalStateException("Conflicting $SwitchMap$ bijection for " + candField + ": " + prev + " vs " + baseField);
                    }
                    if (!baseField.equals(candField)) {
                        renamedCandToBase.put(candField, baseField);
                    }
                }
            }
        }
        // Verify global injectivity across ALL switch-map fields (renamed AND identity)
        if (new HashSet<>(allCandToBase.values()).size() != allCandToBase.size()) {
            throw new IllegalStateException("Non-bijective $SwitchMap$ mapping detected across all switch maps: " + allCandToBase);
        }
        return renamedCandToBase;
    }

    /** Maps remapped enum owner internal name -> $SwitchMap$ field name in a synthetic switch-table class. */
    private static Map<String, String> extractSwitchMapEnums(String className, byte[] raw) {
        ClassNode cn = new ClassNode();
        new ClassReader(raw).accept(cn, ClassReader.SKIP_DEBUG);
        Map<String, String> enumToField = new HashMap<>();
        boolean hasSwitchMap = false;
        for (FieldNode f : cn.fields) {
            if (f.name.startsWith("$SwitchMap$") && "[I".equals(f.desc) && (f.access & Opcodes.ACC_SYNTHETIC) != 0) {
                hasSwitchMap = true;
                break;
            }
        }
        if (!hasSwitchMap) return enumToField;
        for (MethodNode m : cn.methods) {
            if (!"<clinit>".equals(m.name)) continue;
            String lastEnumOwner = null;
            for (AbstractInsnNode insn : m.instructions) {
                if (insn instanceof MethodInsnNode mi && "values".equals(mi.name) && mi.desc.startsWith("()[L")) {
                    lastEnumOwner = mi.owner;
                } else if (insn instanceof FieldInsnNode fi && fi.getOpcode() == Opcodes.PUTSTATIC
                        && fi.name.startsWith("$SwitchMap$") && lastEnumOwner != null) {
                    String prev = enumToField.put(lastEnumOwner, fi.name);
                    if (prev != null && !prev.equals(fi.name)) {
                        throw new IllegalStateException("Duplicate $SwitchMap$ enum owner " + lastEnumOwner + " in " + className + ": " + prev + " vs " + fi.name);
                    }
                    lastEnumOwner = null;
                }
            }
        }
        return enumToField;
    }

    public static void main(String[] args) throws Exception {
        if (args.length < 2) {
            System.err.println("Usage: VerifyBytecodeEquiv <baseline-jar> <candidate-jar>");
            System.exit(2);
        }
        Path basePath = Path.of(args[0]);
        Path candPath = Path.of(args[1]);
        Map<String, byte[]> base = readEntries(basePath, true);
        Map<String, byte[]> cand = readEntries(candPath, true);
        Map<String, byte[]> baseRes = readEntries(basePath, false);
        Map<String, byte[]> candRes = readEntries(candPath, false);

        Set<String> onlyBase = new TreeSet<>(base.keySet());
        onlyBase.removeAll(cand.keySet());
        Set<String> onlyCand = new TreeSet<>(cand.keySet());
        onlyCand.removeAll(base.keySet());

        Map<String, String> switchMapBijection = buildSwitchMapBijection(base, cand);

        List<String> diffs = new ArrayList<>();
        int exact = 0;
        int excludedIdentical = 0;
        for (String name : base.keySet()) {
            byte[] b2 = cand.get(name);
            if (b2 == null) continue;
            byte[] b1 = base.get(name);
            if (isExcluded(name)) {
                if (sha256(b1).equals(sha256(b2))) excludedIdentical++;
                else diffs.add("EXCLUDED_CHANGED: " + name);
                continue;
            }
            if (Arrays.equals(b1, b2) || fingerprintClass(b1, Map.of()).equals(fingerprintClass(b2, switchMapBijection))) {
                exact++;
            } else {
                diffs.add("BYTECODE_DIFF: " + name);
            }
        }
        int resDiffs = 0;
        Set<String> allRes = new TreeSet<>(baseRes.keySet());
        allRes.addAll(candRes.keySet());
        for (String r : allRes) {
            if (!Arrays.equals(baseRes.get(r), candRes.get(r))) {
                resDiffs++;
                diffs.add("RESOURCE_DIFF: " + r);
            }
        }
        System.out.printf(
            "BASE_CLASSES=%d CAND_CLASSES=%d ONLY_BASE=%d ONLY_CAND=%d EQUIV=%d EXCLUDED_IDENTICAL=%d SWITCHMAP_BIJECTIONS=%d RES_DIFFS=%d DIFFS=%d%n",
            base.size(), cand.size(), onlyBase.size(), onlyCand.size(), exact, excludedIdentical, switchMapBijection.size(), resDiffs, diffs.size()
        );
        onlyBase.stream().limit(20).forEach(s -> System.out.println("  ONLY_BASE: " + s));
        onlyCand.stream().limit(20).forEach(s -> System.out.println("  ONLY_CAND: " + s));
        diffs.stream().limit(30).forEach(s -> System.out.println("  " + s));
        if (!onlyBase.isEmpty() || !onlyCand.isEmpty() || !diffs.isEmpty()) {
            System.exit(1);
        }
    }
}

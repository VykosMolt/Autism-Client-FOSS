import java.io.*;
import java.nio.file.*;
import java.security.MessageDigest;
import java.util.*;
import java.util.jar.*;
import org.objectweb.asm.*;
import org.objectweb.asm.tree.*;
import org.objectweb.asm.util.Textifier;
import org.objectweb.asm.util.TraceMethodVisitor;

/**
 * Class-by-class semantic bytecode equivalence verifier between two packaged mod JARs.
 * Ignores debug metadata (SourceFile, line numbers, local variable names) and checks:
 *  - Identical class set
 *  - Identical SHA-256 on untouched integrity classes (without inspecting their bytecode)
 *  - Identical superclass, interfaces, access flags, fields, record components, methods, and instruction streams
 */
public final class VerifyBytecodeEquiv {
    private static boolean isExcluded(String name) {
        return name.startsWith("autismclient/e/")
            || name.startsWith("autismclient/sys/")
            || name.startsWith("autismclient/obf/")
            || name.startsWith("autismclient/a/")
            || name.startsWith("autismclient/util/mm/guardian/");
    }

    private static Map<String, byte[]> readClasses(Path jar) throws Exception {
        Map<String, byte[]> map = new TreeMap<>();
        try (JarFile jf = new JarFile(jar.toFile())) {
            Enumeration<JarEntry> en = jf.entries();
            while (en.hasMoreElements()) {
                JarEntry e = en.nextElement();
                if (!e.isDirectory() && e.getName().endsWith(".class")) {
                    map.put(e.getName().substring(0, e.getName().length() - 6), jf.getInputStream(e).readAllBytes());
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

    private static String fingerprintClass(byte[] raw) {
        ClassNode cn = new ClassNode();
        new ClassReader(raw).accept(cn, ClassReader.SKIP_DEBUG);
        StringBuilder sb = new StringBuilder();
        sb.append("H:").append(cn.access).append('|').append(cn.name).append('|').append(cn.superName)
          .append('|').append(cn.interfaces).append('|').append(cn.nestHostClass).append('\n');
        if (cn.recordComponents != null) {
            for (RecordComponentNode rc : cn.recordComponents) {
                sb.append("RC:").append(rc.name).append(':').append(rc.descriptor).append('\n');
            }
        }
        List<FieldNode> fields = new ArrayList<>(cn.fields);
        fields.sort(Comparator.comparing((FieldNode f) -> normalizeSwitchMap(f.name)).thenComparing(f -> f.desc));
        for (FieldNode f : fields) {
            sb.append("F:").append(f.access).append('|').append(normalizeSwitchMap(f.name)).append('|').append(f.desc)
              .append('|').append(f.value).append('\n');
        }
        List<MethodNode> methods = new ArrayList<>(cn.methods);
        methods.sort(Comparator.comparing((MethodNode m) -> m.name).thenComparing(m -> m.desc));
        for (MethodNode m : methods) {
            sb.append("M:").append(m.access).append('|').append(m.name).append('|').append(m.desc).append('\n');
            Textifier t = new Textifier();
            TraceMethodVisitor tmv = new TraceMethodVisitor(t);
            m.accept(tmv);
            StringWriter sw = new StringWriter();
            t.print(new PrintWriter(sw));
            for (String line : sw.toString().split("\n")) {
                String tr = line.trim();
                if (tr.startsWith("LINENUMBER ") || tr.startsWith("LOCALVARIABLE ")) continue;
                if (tr.contains(".$SwitchMap$")) {
                    tr = tr.replaceAll("\\.\\$SwitchMap\\$[A-Za-z0-9_$]+", ".\\$SwitchMap\\$<NORM>");
                }
                sb.append("  ").append(tr).append('\n');
            }
        }
        return sb.toString();
    }

    private static String normalizeSwitchMap(String name) {
        return name.startsWith("$SwitchMap$") ? "$SwitchMap$<NORM>" : name;
    }

    public static void main(String[] args) throws Exception {
        if (args.length < 2) {
            System.err.println("Usage: VerifyBytecodeEquiv <baseline-jar> <candidate-jar>");
            System.exit(2);
        }
        Map<String, byte[]> base = readClasses(Path.of(args[0]));
        Map<String, byte[]> cand = readClasses(Path.of(args[1]));

        Set<String> onlyBase = new TreeSet<>(base.keySet());
        onlyBase.removeAll(cand.keySet());
        Set<String> onlyCand = new TreeSet<>(cand.keySet());
        onlyCand.removeAll(base.keySet());

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
            if (Arrays.equals(b1, b2) || fingerprintClass(b1).equals(fingerprintClass(b2))) {
                exact++;
            } else {
                diffs.add("BYTECODE_DIFF: " + name);
            }
        }
        System.out.printf("BASE_CLASSES=%d CAND_CLASSES=%d ONLY_BASE=%d ONLY_CAND=%d EQUIV=%d EXCLUDED_IDENTICAL=%d DIFFS=%d%n",
            base.size(), cand.size(), onlyBase.size(), onlyCand.size(), exact, excludedIdentical, diffs.size());
        onlyBase.stream().limit(20).forEach(s -> System.out.println("  ONLY_BASE: " + s));
        onlyCand.stream().limit(20).forEach(s -> System.out.println("  ONLY_CAND: " + s));
        diffs.stream().limit(30).forEach(s -> System.out.println("  " + s));
        if (!onlyBase.isEmpty() || !onlyCand.isEmpty() || !diffs.isEmpty()) {
            System.exit(1);
        }
    }
}

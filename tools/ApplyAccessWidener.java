import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;
import java.util.zip.*;
import org.objectweb.asm.*;

/**
 * Applies a Fabric accessWidener v2 file to a Minecraft client JAR for compile-time visibility.
 */
public final class ApplyAccessWidener {
    private static final int ACCESS_MASK = Opcodes.ACC_PUBLIC | Opcodes.ACC_PRIVATE | Opcodes.ACC_PROTECTED;

    public static void main(String[] args) throws Exception {
        if (args.length < 3) {
            System.err.println("Usage: ApplyAccessWidener <input-jar> <accesswidener-file> <output-jar>");
            System.exit(2);
        }
        Path inputJar = Path.of(args[0]);
        Path awFile = Path.of(args[1]);
        Path outputJar = Path.of(args[2]);

        Map<String, Integer> classFlags = new HashMap<>();
        Map<String, Map<String, Integer>> fieldFlags = new HashMap<>();
        Map<String, Map<String, Integer>> methodFlags = new HashMap<>();
        Set<String> touchedClasses = new HashSet<>();

        for (String raw : Files.readAllLines(awFile, StandardCharsets.UTF_8)) {
            int hash = raw.indexOf('#');
            String line = (hash >= 0 ? raw.substring(0, hash) : raw).trim();
            if (line.isEmpty() || line.startsWith("accessWidener")) continue;
            String[] p = line.split("\\s+");
            if (p.length < 3) continue;
            int flag = switch (p[0]) {
                case "accessible" -> 1;
                case "extendable" -> 2;
                case "mutable" -> 4;
                default -> 0;
            };
            if (flag == 0) continue;
            String target = p[1];
            String owner = p[2];
            touchedClasses.add(owner);
            int dollar = owner.indexOf('$');
            if (dollar > 0) touchedClasses.add(owner.substring(0, dollar));

            if ("class".equals(target)) {
                classFlags.merge(owner, flag, (a, b) -> a | b);
            } else if ("field".equals(target) && p.length >= 5) {
                fieldFlags.computeIfAbsent(owner, k -> new HashMap<>()).merge(p[3] + ":" + p[4], flag, (a, b) -> a | b);
            } else if ("method".equals(target) && p.length >= 5) {
                methodFlags.computeIfAbsent(owner, k -> new HashMap<>()).merge(p[3] + p[4], flag, (a, b) -> a | b);
            }
        }

        Files.createDirectories(outputJar.toAbsolutePath().getParent());
        Path tmp = outputJar.resolveSibling(outputJar.getFileName() + ".tmp");
        try (ZipFile zf = new ZipFile(inputJar.toFile());
             ZipOutputStream zos = new ZipOutputStream(Files.newOutputStream(tmp))) {
            for (ZipEntry e : Collections.list(zf.entries())) {
                if (e.isDirectory()) continue;
                String name = e.getName();
                if (name.startsWith("META-INF/")
                    && (name.endsWith(".SF") || name.endsWith(".RSA") || name.endsWith(".DSA") || name.endsWith(".EC"))) {
                    continue;
                }
                byte[] bytes;
                try (InputStream in = zf.getInputStream(e)) { bytes = in.readAllBytes(); }
                if (name.endsWith(".class")) {
                    String cn = name.substring(0, name.length() - 6);
                    if (touchedClasses.contains(cn)) {
                        bytes = widen(bytes, classFlags, fieldFlags.getOrDefault(cn, Map.of()), methodFlags.getOrDefault(cn, Map.of()));
                    }
                }
                ZipEntry out = new ZipEntry(name);
                out.setTime(0L);
                zos.putNextEntry(out);
                zos.write(bytes);
                zos.closeEntry();
            }
        }
        Files.move(tmp, outputJar, StandardCopyOption.REPLACE_EXISTING);
    }

    private static int applyFlags(int access, int flags, boolean isClass) {
        if ((flags & 1) != 0) access = (access & ~ACCESS_MASK) | Opcodes.ACC_PUBLIC;
        if ((flags & 2) != 0) {
            access &= ~Opcodes.ACC_FINAL;
            if (!isClass && (access & Opcodes.ACC_PUBLIC) == 0) {
                access = (access & ~ACCESS_MASK) | Opcodes.ACC_PROTECTED;
            }
        }
        if ((flags & 4) != 0) access &= ~Opcodes.ACC_FINAL;
        return access;
    }

    private static byte[] widen(byte[] raw, Map<String, Integer> classFlags, Map<String, Integer> fields, Map<String, Integer> methods) {
        ClassReader cr = new ClassReader(raw);
        ClassWriter cw = new ClassWriter(cr, 0);
        ClassVisitor cv = new ClassVisitor(Opcodes.ASM9, cw) {
            @Override
            public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
                Integer f = classFlags.get(name);
                if (f != null) access = applyFlags(access, f, true);
                super.visit(version, access, name, signature, superName, interfaces);
            }

            @Override
            public void visitInnerClass(String name, String outerName, String innerName, int access) {
                Integer f = classFlags.get(name);
                if (f != null) access = applyFlags(access, f, true);
                super.visitInnerClass(name, outerName, innerName, access);
            }

            @Override
            public FieldVisitor visitField(int access, String name, String descriptor, String signature, Object value) {
                Integer f = fields.get(name + ":" + descriptor);
                if (f != null) access = applyFlags(access, f, false);
                return super.visitField(access, name, descriptor, signature, value);
            }

            @Override
            public MethodVisitor visitMethod(int access, String name, String descriptor, String signature, String[] exceptions) {
                Integer f = methods.get(name + descriptor);
                if (f != null) access = applyFlags(access, f, false);
                return super.visitMethod(access, name, descriptor, signature, exceptions);
            }
        };
        cr.accept(cv, 0);
        return cw.toByteArray();
    }
}

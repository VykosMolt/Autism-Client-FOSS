import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;
import java.util.zip.*;
import org.objectweb.asm.*;
import org.objectweb.asm.tree.*;

/**
 * Extracts exact runtime Enum.name() strings per (origEnumClass, origFieldName)
 * from an obfuscated AUTISM Client JAR into mappings/enum-names.tsv.
 */
public final class ExtractEnumNames {
    public static void main(String[] args) throws Exception {
        if (args.length < 2) {
            System.err.println("Usage: ExtractEnumNames <input-jar> <output-enum-names-tsv>");
            System.exit(2);
        }
        Path inputJar = Path.of(args[0]);
        Path outputFile = Path.of(args[1]);

        List<String> lines = new ArrayList<>();
        lines.add("# Exact runtime Enum.name() strings per (origEnumClass, origFieldName)");
        int enumCount = 0;
        int entryCount = 0;

        Map<String, byte[]> classes = new TreeMap<>();
        try (ZipFile zf = new ZipFile(inputJar.toFile())) {
            for (ZipEntry ze : Collections.list(zf.entries())) {
                if (!ze.isDirectory() && ze.getName().endsWith(".class")) {
                    try (InputStream in = zf.getInputStream(ze)) {
                        classes.put(ze.getName().substring(0, ze.getName().length() - 6), in.readAllBytes());
                    }
                }
            }
        }

        try (java.net.URLClassLoader ucl = new java.net.URLClassLoader(new java.net.URL[]{inputJar.toUri().toURL()}, ExtractEnumNames.class.getClassLoader())) {
            for (var entry : classes.entrySet()) {
                ClassNode cn = new ClassNode();
                new ClassReader(entry.getValue()).accept(cn, ClassReader.SKIP_DEBUG | ClassReader.SKIP_FRAMES);
                if (!"java/lang/Enum".equals(cn.superName)) continue;

                // Collect static final fields of this enum's own type
                String ownDesc = "L" + cn.name + ";";
                List<String> enumFieldOrder = new ArrayList<>();
                Set<String> enumFields = new HashSet<>();
                for (FieldNode fn : cn.fields) {
                    if ((fn.access & Opcodes.ACC_ENUM) != 0 && ownDesc.equals(fn.desc)) {
                        enumFields.add(fn.name);
                        enumFieldOrder.add(fn.name);
                    }
                }
                if (enumFields.isEmpty()) continue;

                MethodNode clinit = null;
                for (MethodNode mn : cn.methods) {
                    if ("<clinit>".equals(mn.name) && "()V".equals(mn.desc)) {
                        clinit = mn;
                        break;
                    }
                }
                if (clinit == null) continue;

                // Scan <clinit> for: NEW <enum or enum$N>, DUP, LDC <runtimeName>, ..., PUTSTATIC <enum>.<fieldName>
                List<String> enumEntries = new ArrayList<>();
                String pendingName = null;
                boolean inEnumNew = false;
                for (AbstractInsnNode insn : clinit.instructions) {
                    if (insn instanceof TypeInsnNode tin && tin.getOpcode() == Opcodes.NEW) {
                        if (tin.desc.equals(cn.name) || tin.desc.startsWith(cn.name + "$")) {
                            inEnumNew = true;
                            pendingName = null;
                        }
                    } else if (inEnumNew && pendingName == null && insn instanceof LdcInsnNode ldc && ldc.cst instanceof String s) {
                        pendingName = s;
                    } else if (inEnumNew && insn instanceof FieldInsnNode fin
                            && fin.getOpcode() == Opcodes.PUTSTATIC
                            && fin.owner.equals(cn.name)
                            && ownDesc.equals(fin.desc)
                            && enumFields.contains(fin.name)) {
                        if (pendingName != null) {
                            enumEntries.add(cn.name + "\t" + fin.name + "\t" + pendingName);
                        }
                        inEnumNew = false;
                        pendingName = null;
                    }
                }

                // Fallback for string-encrypted enum <clinit> (e.g., autismclient.e.DataUtil.init(int[]))
                if (enumEntries.size() < enumFields.size()) {
                    try {
                        Class<?> cls = Class.forName(cn.name.replace('/', '.'), true, ucl);
                        List<String> reflected = new ArrayList<>();
                        for (String fname : enumFieldOrder) {
                            java.lang.reflect.Field f = cls.getDeclaredField(fname);
                            f.setAccessible(true);
                            Object val = f.get(null);
                            if (val instanceof Enum<?> e) {
                                reflected.add(cn.name + "\t" + fname + "\t" + e.name());
                            }
                        }
                        if (reflected.size() > enumEntries.size()) {
                            enumEntries = reflected;
                        }
                    } catch (Throwable ignored) {
                    }
                }

                if (!enumEntries.isEmpty()) {
                    enumCount++;
                    entryCount += enumEntries.size();
                    lines.addAll(enumEntries);
                }
            }
        }

        Files.createDirectories(outputFile.getParent());
        Files.write(outputFile, lines, StandardCharsets.UTF_8);
        System.out.println("Wrote " + outputFile + " (" + entryCount + " entries across " + enumCount + " enums).");
    }
}

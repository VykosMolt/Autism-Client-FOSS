import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;
import java.util.zip.*;
import org.objectweb.asm.*;
import org.objectweb.asm.commons.*;
import org.objectweb.asm.tree.*;

/**
 * Lossless post-compile ASM remapper for AUTISM Client 5.3 (26.2).
 *
 * Reads mappings/disambig.tsv in reverse to restore the exact original runtime ABI
 * (keeping all 7 addons 100% binary-compatible), overlays the 12 untouched stock
 * Integrity-protected classes verbatim from precompiled/integrity-classes.jar, and
 * bundles src/main/resources into the final Fabric mod JAR.
 */
public final class ReverseRemapJar {
    public static void main(String[] args) throws Exception {
        if (args.length < 6) {
            System.err.println("Usage: ReverseRemapJar <compiled-classes-dir-or-jar> <stubs-jar> <integrity-jar> <resources-dir> <mappings-tsv> <output-jar>");
            System.exit(2);
        }
        Path compiledInput = Path.of(args[0]);
        Path stubsJar = Path.of(args[1]);
        Path integrityJar = Path.of(args[2]);
        Path resourcesDir = Path.of(args[3]);
        Path mappingsFile = Path.of(args[4]);
        Path outputJar = Path.of(args[5]);

        // 1. Parse mappings/disambig.tsv in reverse (new -> orig)
        Set<String> integrityClasses = new HashSet<>();
        Map<String, String> revClassMap = new HashMap<>();
        Map<String, String> revFieldMap = new HashMap<>();
        Map<String, String> revMethodMap = new HashMap<>();
        for (String line : Files.readAllLines(mappingsFile, StandardCharsets.UTF_8)) {
            if (line.isEmpty() || line.startsWith("#")) continue;
            String[] p = line.split("\t", -1);
            switch (p[0]) {
                case "I" -> integrityClasses.add(p[1]);
                case "C" -> revClassMap.put(p[2], p[1]);
                case "F" -> revFieldMap.put(p[4] + "\t" + p[5] + "\t" + p[6], p[2]);
                case "M" -> revMethodMap.put(p[4] + "\t" + p[5] + "\t" + p[6], p[2]);
            }
        }

        // 1b. Load exact original runtime Enum.name() strings (mappings/enum-names.tsv next to disambig.tsv)
        Map<String, Map<String, String>> enumRuntimeNames = new HashMap<>();
        Path enumNamesFile = args.length >= 7 ? Path.of(args[6]) : mappingsFile.resolveSibling("enum-names.tsv");
        // Throws if the table is missing: shipping without it would change Enum.name() strings.
        for (String line : Files.readAllLines(enumNamesFile, StandardCharsets.UTF_8)) {
            if (line.isEmpty() || line.startsWith("#")) continue;
            String[] p = line.split("\t", -1);
            if (p.length >= 3) {
                enumRuntimeNames.computeIfAbsent(p[0], k -> new HashMap<>()).put(p[1], p[2]);
            }
        }

        // 1c. Record component names that toString/equals print (mappings/record-names.tsv next to disambig.tsv)
        Map<String, String> recordNames = new HashMap<>();
        for (String line : Files.readAllLines(mappingsFile.resolveSibling("record-names.tsv"), StandardCharsets.UTF_8)) {
            if (line.isEmpty() || line.startsWith("#")) continue;
            String[] p = line.split("\t", -1);
            recordNames.put(p[0], p[1]);
        }

        // 2. Load all compiled classes + stub hierarchy (for superclass/interface field/method resolution)
        Map<String, byte[]> compiledClasses = new LinkedHashMap<>();
        Map<String, ClassNode> hierarchy = new HashMap<>();
        if (Files.isDirectory(compiledInput)) {
            try (var stream = Files.walk(compiledInput)) {
                for (Path f : stream.filter(Files::isRegularFile).sorted().toList()) {
                    String rel = compiledInput.relativize(f).toString().replace('\\', '/');
                    if (!rel.endsWith(".class")) continue;
                    String cn = rel.substring(0, rel.length() - 6);
                    byte[] bytes = Files.readAllBytes(f);
                    compiledClasses.put(cn, bytes);
                    hierarchy.put(cn, readHeader(bytes));
                }
            }
        } else {
            try (ZipFile zf = new ZipFile(compiledInput.toFile())) {
                for (ZipEntry e : Collections.list(zf.entries())) {
                    if (!e.getName().endsWith(".class")) continue;
                    String cn = e.getName().substring(0, e.getName().length() - 6);
                    byte[] bytes;
                    try (InputStream in = zf.getInputStream(e)) { bytes = in.readAllBytes(); }
                    compiledClasses.put(cn, bytes);
                    hierarchy.put(cn, readHeader(bytes));
                }
            }
        }
        try (ZipFile zf = new ZipFile(stubsJar.toFile())) {
            for (ZipEntry e : Collections.list(zf.entries())) {
                if (!e.getName().endsWith(".class")) continue;
                String cn = e.getName().substring(0, e.getName().length() - 6);
                try (InputStream in = zf.getInputStream(e)) {
                    hierarchy.putIfAbsent(cn, readHeader(in.readAllBytes()));
                }
            }
        }

        Remapper revRemapper = new Remapper() {
            private String normalizeLocalOwner(String internalName) {
                if (internalName != null && internalName.contains("Inner_")) {
                    String stripped = internalName.replaceAll("\\$\\d+(Inner_)", "\\$$1");
                    if (revClassMap.containsKey(stripped)) {
                        return stripped;
                    }
                }
                return internalName;
            }

            @Override
            public String map(String internalName) {
                String hit = revClassMap.get(internalName);
                if (hit != null) return hit;
                String norm = normalizeLocalOwner(internalName);
                hit = revClassMap.get(norm);
                if (hit != null) return hit;
                // Outer-class prefix fallback for anonymous/nested classes (e.g. RenamedClass$1 -> orig$1)
                if (internalName != null) {
                    int idx = internalName.lastIndexOf('$');
                    while (idx > 0) {
                        String outer = internalName.substring(0, idx);
                        String mappedOuter = revClassMap.get(outer);
                        if (mappedOuter == null) {
                            mappedOuter = revClassMap.get(normalizeLocalOwner(outer));
                        }
                        if (mappedOuter != null) {
                            return mappedOuter + internalName.substring(idx);
                        }
                        idx = internalName.lastIndexOf('$', idx - 1);
                    }
                }
                return internalName;
            }

            @Override
            public String mapFieldName(String owner, String name, String descriptor) {
                String normOwner = normalizeLocalOwner(owner);
                String direct = revFieldMap.get(normOwner + "\t" + name + "\t" + descriptor);
                if (direct != null) return direct;
                Deque<String> q = new ArrayDeque<>();
                Set<String> seen = new HashSet<>();
                q.add(owner);
                if (!normOwner.equals(owner)) q.add(normOwner);
                while (!q.isEmpty()) {
                    String cur = q.poll();
                    if (!seen.add(cur)) continue;
                    String normCur = normalizeLocalOwner(cur);
                    String hit = revFieldMap.get(normCur + "\t" + name + "\t" + descriptor);
                    if (hit != null) return hit;
                    ClassNode cn = hierarchy.get(cur);
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
                String normOwner = normalizeLocalOwner(owner);
                String direct = revMethodMap.get(normOwner + "\t" + name + "\t" + descriptor);
                if (direct != null) return direct;
                Deque<String> q = new ArrayDeque<>();
                Set<String> seen = new HashSet<>();
                q.add(owner);
                if (!normOwner.equals(owner)) q.add(normOwner);
                while (!q.isEmpty()) {
                    String cur = q.poll();
                    if (!seen.add(cur)) continue;
                    String normCur = normalizeLocalOwner(cur);
                    String hit = revMethodMap.get(normCur + "\t" + name + "\t" + descriptor);
                    if (hit != null) return hit;
                    ClassNode cn = hierarchy.get(cur);
                    if (cn != null) {
                        if (cn.superName != null) q.add(cn.superName);
                        if (cn.interfaces != null) q.addAll(cn.interfaces);
                    }
                }
                return name;
            }
        };

        // 3. Write output JAR: reverse-remapped compiled classes + untouched 12 Integrity classes + resources
        Files.createDirectories(outputJar.toAbsolutePath().getParent());
        Set<String> written = new HashSet<>();
        try (ZipOutputStream zos = new ZipOutputStream(Files.newOutputStream(outputJar))) {
            // 3a. Untouched stock Integrity classes FIRST (authoritative, never overwritten!)
            try (ZipFile zf = new ZipFile(integrityJar.toFile())) {
                for (ZipEntry e : Collections.list(zf.entries())) {
                    if (e.isDirectory()) continue;
                    byte[] bytes;
                    try (InputStream in = zf.getInputStream(e)) { bytes = in.readAllBytes(); }
                    putEntry(zos, e.getName(), bytes);
                    written.add(e.getName());
                }
            }

            // 3b. Reverse-remapped compiled classes (skipping any Integrity class)
            int remappedCount = 0;
            for (var entry : compiledClasses.entrySet()) {
                String origName = revRemapper.map(entry.getKey());
                if (integrityClasses.contains(origName)) continue;
                String zipPath = origName + ".class";
                if (!written.add(zipPath)) {
                    throw new IllegalStateException("Reverse-remap collision on " + zipPath + " from compiled class " + entry.getKey());
                }
                byte[] remapped = remapClass(entry.getValue(), revRemapper);
                Map<String, String> enumMap = enumRuntimeNames.get(origName);
                if (enumMap != null) {
                    remapped = restoreEnumRuntimeNames(remapped, origName, enumMap);
                }
                String names = recordNames.get(origName);
                if (names != null) {
                    remapped = restoreRecordNames(remapped, names);
                }
                remapped = deduplicateRecordMethods(remapped);
                putEntry(zos, zipPath, remapped);
                remappedCount++;
            }

            // 3c. Resources
            int resCount = 0;
            if (Files.isDirectory(resourcesDir)) {
                try (var stream = Files.walk(resourcesDir)) {
                    for (Path f : stream.filter(Files::isRegularFile).sorted().toList()) {
                        String rel = resourcesDir.relativize(f).toString().replace('\\', '/');
                        if (!written.add(rel)) continue;
                        putEntry(zos, rel, Files.readAllBytes(f));
                        resCount++;
                    }
                }
            }
            System.out.println("Packaged " + outputJar + ": " + remappedCount + " remapped classes + "
                + integrityClasses.size() + " untouched Integrity classes + " + resCount + " resources.");
        }
    }

    private static ClassNode readHeader(byte[] bytes) {
        ClassNode cn = new ClassNode();
        new ClassReader(bytes).accept(cn, ClassReader.SKIP_CODE | ClassReader.SKIP_DEBUG | ClassReader.SKIP_FRAMES);
        return cn;
    }

    private static byte[] remapClass(byte[] raw, Remapper remapper) {
        ClassReader cr = new ClassReader(raw);
        ClassWriter cw = new ClassWriter(0);
        String curOwner = cr.getClassName();
        ClassVisitor cv = new ClassRemapper(cw, remapper) {
            @Override
            public RecordComponentVisitor visitRecordComponent(String name, String descriptor, String signature) {
                String mappedName = remapper.mapFieldName(curOwner, name, descriptor);
                return super.visitRecordComponent(mappedName, descriptor, signature);
            }

            @Override
            public void visitInnerClass(String name, String outerName, String innerName, int access) {
                String mappedName = remapper.mapType(name);
                String mappedOuter = outerName == null ? null : remapper.mapType(outerName);
                String mappedInner = innerName;
                if (innerName != null && mappedName != null) {
                    int dollar = mappedName.lastIndexOf('$');
                    if (dollar >= 0 && dollar + 1 < mappedName.length()) {
                        String tail = mappedName.substring(dollar + 1);
                        int k = 0;
                        while (k < tail.length() && Character.isDigit(tail.charAt(k))) k++;
                        mappedInner = (k > 0 && k < tail.length()) ? tail.substring(k) : tail;
                    }
                }
                super.cv.visitInnerClass(mappedName, mappedOuter, mappedInner, access);
            }
        };
        cr.accept(cv, 0);
        return cw.toByteArray();
    }

    private static byte[] restoreRecordNames(byte[] raw, String names) {
        ClassWriter cw = new ClassWriter(0);
        new ClassReader(raw).accept(new ClassVisitor(Opcodes.ASM9, cw) {
            @Override
            public MethodVisitor visitMethod(int access, String name, String descriptor, String signature, String[] exceptions) {
                return new MethodVisitor(Opcodes.ASM9, super.visitMethod(access, name, descriptor, signature, exceptions)) {
                    @Override
                    public void visitInvokeDynamicInsn(String name, String descriptor, Handle bsm, Object... args) {
                        if ("java/lang/runtime/ObjectMethods".equals(bsm.getOwner()) && args.length > 1 && args[1] instanceof String) {
                            int count = names.isEmpty() ? 0 : names.split(";", -1).length;
                            if (count != args.length - 2) {
                                throw new IllegalStateException("record-names.tsv lists " + count + " names but the record has "
                                    + (args.length - 2) + " components; regenerate it or update the entry");
                            }
                            args = args.clone();
                            args[1] = names;
                        }
                        super.visitInvokeDynamicInsn(name, descriptor, bsm, args);
                    }
                };
            }
        }, 0);
        return cw.toByteArray();
    }

    private static byte[] restoreEnumRuntimeNames(byte[] raw, String origEnumName, Map<String, String> fieldToRuntimeName) {
        ClassNode cn = new ClassNode();
        new ClassReader(raw).accept(cn, 0);
        if ((cn.access & Opcodes.ACC_ENUM) == 0) return raw;
        String selfDesc = "L" + origEnumName + ";";
        boolean changed = false;
        for (MethodNode mn : cn.methods) {
            if (!"<clinit>".equals(mn.name)) continue;
            List<AbstractInsnNode> insns = new ArrayList<>();
            for (AbstractInsnNode x : mn.instructions) {
                if (x.getOpcode() >= 0) insns.add(x);
            }
            int lastPut = -1;
            for (int i = 0; i < insns.size(); i++) {
                if (insns.get(i) instanceof FieldInsnNode fi
                        && fi.getOpcode() == Opcodes.PUTSTATIC
                        && origEnumName.equals(fi.owner)
                        && selfDesc.equals(fi.desc)) {
                    String wanted = fieldToRuntimeName.get(fi.name);
                    if (wanted != null) {
                        for (int j = lastPut + 1; j < i; j++) {
                            if (insns.get(j) instanceof TypeInsnNode ti
                                    && ti.getOpcode() == Opcodes.NEW
                                    && (ti.desc.equals(origEnumName) || ti.desc.startsWith(origEnumName + "$"))
                                    && j + 2 < i && insns.get(j + 1).getOpcode() == Opcodes.DUP
                                    && insns.get(j + 2) instanceof LdcInsnNode ldc
                                    && ldc.cst instanceof String) {
                                if (!wanted.equals(ldc.cst)) {
                                    ldc.cst = wanted;
                                    changed = true;
                                }
                                break;
                            }
                        }
                    }
                    lastPut = i;
                }
            }
        }
        if (!changed) return raw;
        ClassWriter cw = new ClassWriter(0);
        cn.accept(cw);
        return cw.toByteArray();
    }

    private static byte[] deduplicateRecordMethods(byte[] raw) {
        ClassNode cn = new ClassNode();
        new ClassReader(raw).accept(cn, 0);
        if (!"java/lang/Record".equals(cn.superName)) return raw;
        Set<String> seen = new HashSet<>();
        boolean changed = false;
        Iterator<MethodNode> it = cn.methods.iterator();
        while (it.hasNext()) {
            MethodNode mn = it.next();
            if (!seen.add(mn.name + mn.desc)) {
                it.remove();
                changed = true;
            }
        }
        if (!changed) return raw;
        ClassWriter cw = new ClassWriter(0);
        cn.accept(cw);
        return cw.toByteArray();
    }

    private static void putEntry(ZipOutputStream zos, String name, byte[] data) throws IOException {
        ZipEntry ze = new ZipEntry(name);
        ze.setTime(0L);
        zos.putNextEntry(ze);
        zos.write(data);
        zos.closeEntry();
    }
}

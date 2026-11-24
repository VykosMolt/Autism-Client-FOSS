import java.nio.file.*;
import java.util.*;
import java.util.zip.*;
import org.objectweb.asm.*;
import org.objectweb.asm.tree.*;

/**
 * Extracts the component names each record's toString/equals bootstrap carries in an obfuscated
 * AUTISM Client JAR into mappings/record-names.tsv (class, names), so the rebuilt jar prints the same.
 */
public final class ExtractRecordNames {
    public static void main(String[] args) throws Exception {
        if (args.length < 2) {
            System.err.println("Usage: ExtractRecordNames <input-jar> <output-record-names-tsv>");
            System.exit(2);
        }
        TreeMap<String, String> names = new TreeMap<>();
        try (ZipFile zf = new ZipFile(args[0])) {
            for (ZipEntry ze : Collections.list(zf.entries())) {
                if (!ze.getName().endsWith(".class")) continue;
                ClassNode cn = new ClassNode();
                new ClassReader(zf.getInputStream(ze).readAllBytes()).accept(cn, ClassReader.SKIP_DEBUG | ClassReader.SKIP_FRAMES);
                for (MethodNode mn : cn.methods) {
                    for (AbstractInsnNode insn : mn.instructions) {
                        if (insn instanceof InvokeDynamicInsnNode indy && "java/lang/runtime/ObjectMethods".equals(indy.bsm.getOwner())
                            && indy.bsmArgs.length > 1 && indy.bsmArgs[1] instanceof String s) {
                            names.put(cn.name, s);
                        }
                    }
                }
            }
        }
        List<String> lines = new ArrayList<>();
        lines.add("# Record component names used by ObjectMethods.bootstrap per (origRecordClass)");
        names.forEach((k, v) -> lines.add(k + "\t" + v));
        Files.write(Path.of(args[1]), lines);
        System.out.println("Wrote " + names.size() + " records to " + args[1]);
    }
}

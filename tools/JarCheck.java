import java.util.*;
import java.util.zip.*;
import org.objectweb.asm.*;
import org.objectweb.asm.tree.*;

/**
 * Compares a built jar with the stock AUTISM jar for decompile errors javac cannot see:
 * missing bridge/forwarder methods, methods with a loop that can never exit, and record toString names.
 * Usage: java -cp <asm, asm-tree> tools/JarCheck.java <stock.jar> <built.jar>
 */
public final class JarCheck {
    // Bridges the fix commits removed on purpose, or that are equivalent to the inherited method.
    private static final Set<String> KNOWN = Set.of(
        "autismclient/mixin/AutismBookTitleCodecMixin$1",
        "autismclient/gui/f/b/BitSetUtil.init(Z)Lautismclient/gui/f/c/TableIndex;",
        "autismclient/gui/f/c/ByteReader.init(Z)Lautismclient/gui/f/c/TableIndex;",
        "autismclient/gui/f/c/PoolEntry.init(Z)Lautismclient/gui/f/c/TableIndex;",
        "autismclient/gui/f/c/JsonUtil.init(Z)Lautismclient/gui/f/c/TableIndex;"
    );

    public static void main(String[] a) throws Exception {
        Map<String, ClassNode> stock = load(a[0]), built = load(a[1]);
        int problems = 0;
        for (ClassNode c : stock.values()) {
            ClassNode b = built.get(c.name);
            for (MethodNode m : c.methods) {
                boolean forwarder = (m.access & (Opcodes.ACC_SYNTHETIC | Opcodes.ACC_PUBLIC | Opcodes.ACC_STATIC)) == (Opcodes.ACC_SYNTHETIC | Opcodes.ACC_PUBLIC);
                if ((m.access & Opcodes.ACC_BRIDGE) == 0 && !forwarder) continue;
                String id = c.name + "." + m.name + m.desc;
                if (KNOWN.contains(id) || KNOWN.contains(c.name)) continue;
                if (b == null || b.methods.stream().noneMatch(x -> x.name.equals(m.name) && x.desc.equals(m.desc))) {
                    System.out.println("MISSING BRIDGE " + id);
                    problems++;
                }
            }
            if (b == null) continue;
            int stuckStock = 0, stuckBuilt = 0;
            for (MethodNode m : c.methods) if (stuck(m)) stuckStock++;
            for (MethodNode m : b.methods) if (stuck(m)) stuckBuilt++;
            if (stuckBuilt > stuckStock) {
                System.out.println("LOOP WITHOUT EXIT " + c.name + " stock=" + stuckStock + " built=" + stuckBuilt);
                problems++;
            }
            String sn = recordNames(c), bn = recordNames(b);
            if (sn != null && !sn.equals(bn)) {
                System.out.println("RECORD NAMES " + c.name + " stock=" + sn + " built=" + bn);
                problems++;
            }
        }
        System.out.println(problems == 0 ? "JAR CHECK PASSED (" + stock.size() + " classes)" : "JAR CHECK FAILED: " + problems + " problems");
        System.exit(problems == 0 ? 0 : 1);
    }

    private static String recordNames(ClassNode c) {
        for (MethodNode m : c.methods) {
            for (AbstractInsnNode x : m.instructions) {
                if (x instanceof InvokeDynamicInsnNode indy && "java/lang/runtime/ObjectMethods".equals(indy.bsm.getOwner())
                    && indy.bsmArgs.length > 1 && indy.bsmArgs[1] instanceof String names) {
                    return names;
                }
            }
        }
        return null;
    }

    // True when a reachable instruction can never reach a return or throw (an endless loop).
    private static boolean stuck(MethodNode m) {
        InsnList in = m.instructions;
        int n = in.size();
        if (n == 0) return false;
        List<List<Integer>> succ = new ArrayList<>();
        for (int i = 0; i < n; i++) succ.add(new ArrayList<>());
        boolean[] exit = new boolean[n];
        for (int i = 0; i < n; i++) {
            AbstractInsnNode x = in.get(i);
            int o = x.getOpcode();
            if (o >= Opcodes.IRETURN && o <= Opcodes.RETURN || o == Opcodes.ATHROW) {
                exit[i] = true;
                continue;
            }
            if (x instanceof JumpInsnNode j) {
                succ.get(i).add(in.indexOf(j.label));
                if (o == Opcodes.GOTO) continue;
            } else if (x instanceof TableSwitchInsnNode t) {
                succ.get(i).add(in.indexOf(t.dflt));
                for (LabelNode l : t.labels) succ.get(i).add(in.indexOf(l));
                continue;
            } else if (x instanceof LookupSwitchInsnNode t) {
                succ.get(i).add(in.indexOf(t.dflt));
                for (LabelNode l : t.labels) succ.get(i).add(in.indexOf(l));
                continue;
            }
            if (i + 1 < n) succ.get(i).add(i + 1);
        }
        boolean[] reach = new boolean[n];
        Deque<Integer> q = new ArrayDeque<>(List.of(0));
        for (TryCatchBlockNode tc : m.tryCatchBlocks) q.add(in.indexOf(tc.handler));
        while (!q.isEmpty()) {
            int i = q.poll();
            if (!reach[i]) {
                reach[i] = true;
                q.addAll(succ.get(i));
            }
        }
        List<List<Integer>> pred = new ArrayList<>();
        for (int i = 0; i < n; i++) pred.add(new ArrayList<>());
        for (int i = 0; i < n; i++) for (int j : succ.get(i)) pred.get(j).add(i);
        boolean[] canExit = new boolean[n];
        for (int i = 0; i < n; i++) if (exit[i]) q.add(i);
        while (!q.isEmpty()) {
            int i = q.poll();
            if (!canExit[i]) {
                canExit[i] = true;
                q.addAll(pred.get(i));
            }
        }
        for (int i = 0; i < n; i++) if (reach[i] && !canExit[i] && in.get(i).getOpcode() >= 0) return true;
        return false;
    }

    private static Map<String, ClassNode> load(String jar) throws Exception {
        Map<String, ClassNode> out = new HashMap<>();
        try (ZipFile z = new ZipFile(jar)) {
            for (ZipEntry e : Collections.list(z.entries())) {
                if (!e.getName().endsWith(".class")) continue;
                ClassNode cn = new ClassNode();
                new ClassReader(z.getInputStream(e).readAllBytes()).accept(cn, ClassReader.SKIP_DEBUG | ClassReader.SKIP_FRAMES);
                out.put(cn.name, cn);
            }
        }
        return out;
    }
}

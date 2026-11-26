package autismclient.util.macro;

import java.io.InputStream;
import java.lang.classfile.ClassFile;
import java.lang.classfile.CodeElement;
import java.lang.classfile.MethodModel;
import java.lang.classfile.instruction.InvokeInstruction;

public class KickDupeBlockingCheck {
   public static void main(String[] args) throws Exception {
      UseItemPhaseAction instant = new UseItemPhaseAction();
      UseItemPhaseAction hold = new UseItemPhaseAction();
      hold.holdTicks = 5;
      DisconnectAction autoDisconnect = new DisconnectAction();
      autoDisconnect.mode = DisconnectAction.Inner_StreamUtil.AUTO_DISCONNECT;
      check(!DisconnectAction.blocksClientThread(instant), "use item without hold must run");
      check(DisconnectAction.blocksClientThread(hold), "use item with hold must be skipped");
      check(DisconnectAction.blocksClientThread(new BundleDupeV2Action()), "bundle dupe must be skipped");
      check(DisconnectAction.blocksClientThread(new CustomMenuAction()), "custom menu form must be skipped");
      check(DisconnectAction.blocksClientThread(new ReportAction()), "report must be skipped");
      check(DisconnectAction.blocksClientThread(autoDisconnect), "auto disconnect wait must be skipped");
      check(!DisconnectAction.blocksClientThread(new DisconnectAction()), "plain disconnect must run");
      check(!DisconnectAction.blocksClientThread(new PacketGateAction()), "packet gate must run");

      // Running write() needs a connected player, so check its bytecode: the guard is called before each follow-up execute().
      byte[] bytes;
      try (InputStream in = DisconnectAction.class.getResourceAsStream(DisconnectAction.class.getSimpleName() + ".class")) {
         bytes = in.readAllBytes();
      }
      MethodModel write = ClassFile.of().parse(bytes).methods().stream()
         .filter(m -> m.methodName().equalsString("write") && m.methodType().equalsString("(Lnet/minecraft/client/Minecraft;)V"))
         .findFirst()
         .orElseThrow();
      int index = 0;
      int guard = -1;
      int execute = -1;
      for (CodeElement element : write.code().orElseThrow()) {
         if (element instanceof InvokeInstruction call) {
            if (call.name().equalsString("blocksClientThread") && guard < 0) {
               guard = index;
            }
            if (call.name().equalsString("execute") && call.owner().asInternalName().equals("autismclient/util/macro/MacroAction")) {
               execute = index;
            }
         }
         index++;
      }
      check(guard >= 0 && execute > guard, "ByteReader.write must call blocksClientThread before running a follow-up action");
      System.out.println("PASS KickDupeBlockingCheck");
   }

   private static void check(boolean ok, String what) {
      if (!ok) {
         System.out.println("FAIL " + what);
         System.exit(1);
      }
   }
}

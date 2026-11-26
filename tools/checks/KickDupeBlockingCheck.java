package autismclient.util.macro;

import java.io.InputStream;
import java.lang.classfile.ClassFile;
import java.lang.classfile.CodeElement;
import java.lang.classfile.MethodModel;
import java.lang.classfile.instruction.InvokeInstruction;

public class KickDupeBlockingCheck {
   public static void main(String[] args) throws Exception {
      ao instant = new ao();
      ao hold = new ao();
      hold.holdTicks = 5;
      ByteReader autoDisconnect = new ByteReader();
      autoDisconnect.mode = ByteReader.Inner_StreamUtil.AUTO_DISCONNECT;
      check(!ByteReader.blocksClientThread(instant), "use item without hold must run");
      check(ByteReader.blocksClientThread(hold), "use item with hold must be skipped");
      check(ByteReader.blocksClientThread(new BufferHelper()), "bundle dupe must be skipped");
      check(ByteReader.blocksClientThread(new UiHelper()), "custom menu form must be skipped");
      check(ByteReader.blocksClientThread(new O()), "report must be skipped");
      check(ByteReader.blocksClientThread(autoDisconnect), "auto disconnect wait must be skipped");
      check(!ByteReader.blocksClientThread(new ByteReader()), "plain disconnect must run");
      check(!ByteReader.blocksClientThread(new B()), "packet gate must run");

      // Running write() needs a connected player, so check its bytecode: the guard is called before each follow-up execute().
      byte[] bytes;
      try (InputStream in = ByteReader.class.getResourceAsStream("ByteReader.class")) {
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

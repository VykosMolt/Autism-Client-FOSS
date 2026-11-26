package autismclient.util.mm.a;

import java.lang.foreign.Arena;
import java.lang.foreign.FunctionDescriptor;
import java.lang.foreign.Linker;
import java.lang.foreign.MemoryLayout;
import java.lang.foreign.MemorySegment;
import java.lang.foreign.SymbolLookup;
import java.lang.foreign.ValueLayout;
import java.lang.invoke.MethodHandle;
import java.util.Locale;

final class WindowsDpapiBridge {
   private static final MemoryLayout init_MemoryLayout = MemoryLayout.structLayout(
      ValueLayout.JAVA_INT.withName("cbData"), MemoryLayout.paddingLayout(4L), ValueLayout.ADDRESS.withName("pbData")
   );
   private static final long init_J = 8L;
   private static final int init_I = 1;
   private static final MethodHandle init_MethodHandle;
   private static final MethodHandle load;
   private static final MethodHandle read;
   private static final boolean init_Z;

   private WindowsDpapiBridge() {
   }

   static boolean init() {
      return init_Z;
   }

   static byte[] init(byte[] plain) {
      return init(init_MethodHandle, plain);
   }

   static byte[] load(byte[] cipher) {
      return init(load, cipher);
   }

   private static byte[] init(MethodHandle fn, byte[] in) {
      if (init_Z && fn != null && in != null) {
         try (Arena arena = Arena.ofConfined()) {
            MemorySegment inData = arena.allocate(Math.max(1, in.length));
            MemorySegment.copy(MemorySegment.ofArray(in), 0L, inData, 0L, in.length);
            MemorySegment inBlob = arena.allocate(init_MemoryLayout);
            inBlob.set(ValueLayout.JAVA_INT, 0L, in.length);
            inBlob.set(ValueLayout.ADDRESS, 8L, inData);
            MemorySegment outBlob = arena.allocate(init_MemoryLayout);
            outBlob.set(ValueLayout.JAVA_INT, 0L, 0);
            outBlob.set(ValueLayout.ADDRESS, 8L, MemorySegment.NULL);
            int ok = (int)fn.invoke(
               (MemorySegment)inBlob,
               (MemorySegment)MemorySegment.NULL,
               (MemorySegment)MemorySegment.NULL,
               (MemorySegment)MemorySegment.NULL,
               (MemorySegment)MemorySegment.NULL,
               (int)1,
               (MemorySegment)outBlob
            );
            if (ok == 0) {
               return null;
            } else {
               int cb = outBlob.get(ValueLayout.JAVA_INT, 0L);
               MemorySegment pb = outBlob.get(ValueLayout.ADDRESS, 8L);
               if (pb.address() != 0L) {
                  try {
                     if (cb < 0) {
                        return null;
                     }

                     byte[] out = new byte[cb];
                     MemorySegment.copy(pb.reinterpret(cb), 0L, MemorySegment.ofArray(out), 0L, cb);
                     return out;
                  } finally {
                     read.invoke((MemorySegment)pb);
                  }
               } else {
                  return null;
               }
            }
         } catch (Throwable t) {
            return null;
         }
      } else {
         return null;
      }
   }

   static {
      MethodHandle protect = null;
      MethodHandle unprotect = null;
      MethodHandle localFree = null;
      boolean ok = false;
      if (System.getProperty("os.name", "").toLowerCase(Locale.ROOT).contains("win")) {
         try {
            Linker linker = Linker.nativeLinker();
            Arena global = Arena.global();
            SymbolLookup crypt32 = SymbolLookup.libraryLookup("Crypt32.dll", global);
            SymbolLookup kernel32 = SymbolLookup.libraryLookup("Kernel32.dll", global);
            FunctionDescriptor blobFn = FunctionDescriptor.of(
               ValueLayout.JAVA_INT,
               ValueLayout.ADDRESS,
               ValueLayout.ADDRESS,
               ValueLayout.ADDRESS,
               ValueLayout.ADDRESS,
               ValueLayout.ADDRESS,
               ValueLayout.JAVA_INT,
               ValueLayout.ADDRESS
            );
            protect = linker.downcallHandle(crypt32.find("CryptProtectData").orElseThrow(), blobFn);
            unprotect = linker.downcallHandle(crypt32.find("CryptUnprotectData").orElseThrow(), blobFn);
            localFree = linker.downcallHandle(kernel32.find("LocalFree").orElseThrow(), FunctionDescriptor.of(ValueLayout.ADDRESS, ValueLayout.ADDRESS));
            ok = true;
         } catch (Throwable t) {
            ok = false;
         }
      }

      init_MethodHandle = protect;
      load = unprotect;
      read = localFree;
      init_Z = ok;
   }
}

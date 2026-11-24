package autismclient.util.g;

import autismclient.util.aA;
import net.minecraft.client.multiplayer.resolver.ServerAddress;

public final class FileCache {
   private final String init_Str;
   private final aA.Inner_StreamUtil init_aA_StreamUtil;
   private long init_J;
   private long load_J = -9223372036854775808L;
   private long read;
   private boolean init_Z;
   private String load_Str = "Joining";

   FileCache(String address, aA.Inner_StreamUtil timing, int timeoutSec) {
      this.init_Str = address == null ? "" : address.trim();
      this.init_aA_StreamUtil = timing == null ? aA.Inner_StreamUtil.flush : timing;
      this.init_J = Math.max(1, Math.min(300, timeoutSec)) * 1000L;
   }

   FileCache.Inner_DataUtil init(long now, EventBus host) {
      if (!this.init_Str.isBlank() && ServerAddress.isValidAddress(this.init_Str)) {
         if (this.init_Z && this.load_J != -9223372036854775808L) {
            this.init_J = this.init_J - Math.max(0L, now - this.load_J);
         }

         this.load_J = now;
         FileCache.Inner_StreamUtil state = host.init_FileCache_StreamUt();
         if (state.sync()) {
            this.load_Str = "Join cancelled";
            return FileCache.Inner_DataUtil.read;
         }

         boolean same = autismclient.util.macro.ClassCache.init_Z_Str_Str(this.init_Str, state.init_Str());
         if (same && state.load_Z()) {
            this.init_Z = false;
            this.load_Str = "Waiting: " + this.init_aA_StreamUtil.load();
            if (!state.init(this.init_aA_StreamUtil)) {
               return FileCache.Inner_DataUtil.init;
            }

            host.init_V();
            return FileCache.Inner_DataUtil.load;
         } else {
            if (this.init_J <= 0L) {
               this.load_Str = "Join timed out: " + this.init_Str;
               return FileCache.Inner_DataUtil.read;
            }

            this.init_Z = !same || !state.init_Z() || !state.update();
            if (same && state.init_Z()) {
               if (this.read == 0L) {
                  this.read = now + 5000L;
               }

               this.load_Str = "Joining " + this.init_Str;
               return FileCache.Inner_DataUtil.init;
            } else {
               if (now >= this.read) {
                  this.read = now + 5000L;
                  String error = host.init_Str_Str(this.init_Str);
                  this.load_Str = error != null && !error.isBlank() ? "Join retry: " + error : "Joining " + this.init_Str;
               } else {
                  this.load_Str = "Join retry: " + (state.load_Str() == null ? "connection closed" : state.load_Str());
               }

               return FileCache.Inner_DataUtil.init;
            }
         }
      } else {
         this.load_Str = this.init_Str.isBlank() ? "Join: this bot has no last server" : "Join: invalid server address";
         return FileCache.Inner_DataUtil.read;
      }
   }

   String init() {
      return this.load_Str;
   }

   enum Inner_DataUtil {
      init,
      load,
      read;
   }

   public record Inner_StreamUtil(
      String init_Str, boolean init_Z, boolean load_Z, int init_I, boolean read, boolean write, int load_I, boolean update, boolean sync, String load_Str
   ) {
      boolean init(aA.Inner_StreamUtil timing) {
         if (!this.load_Z) {
            return false;
         }

         boolean var10000;
         switch (timing) {
            case init_aA_StreamUtil:
            case load_aA_StreamUtil:
               var10000 = true;
               break;
            case read:
               var10000 = this.init_I >= 1;
               break;
            case write:
               var10000 = this.read;
               break;
            case update:
               var10000 = this.write;
               break;
            case sync:
               var10000 = this.write && this.load_I >= 2;
               break;
            default:
               throw new MatchException(null, null);
         }

         return var10000;
      }
   }
}

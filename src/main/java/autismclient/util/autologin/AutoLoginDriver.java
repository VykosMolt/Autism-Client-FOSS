package autismclient.util.autologin;

import autismclient.api.custommenu.CustomMenuButton;
import autismclient.api.custommenu.CustomMenuInput;
import autismclient.api.custommenu.CustomMenuSnapshot;
import autismclient.api.custommenu.CustomMenuSubmission;
import autismclient.util.macro.CustomMenuActionPlanner;
import java.util.LinkedHashMap;
import java.util.Map;

public final class AutoLoginDriver {
   private final AutoLoginBackend init_BufferHelper;
   private volatile AuthPromptParser.Inner_DataUtil init_DataUtil_DataUtil;
   private volatile long init_J;
   private volatile AutoLoginTimings init_StreamUtil;
   private volatile AutoLoginDriver.Inner_DataUtil init_CachePool_DataUtil;
   private long load_J;
   private long read_J;
   private long write;
   private long update;
   private long sync;
   private long flush;
   private boolean init_Z;
   private int init_I;
   private boolean load_Z;
   private boolean read_Z;
   private long close;
   private String init_Str;
   private String load_Str;

   public AutoLoginDriver(AutoLoginBackend host) {
      this.init_DataUtil_DataUtil = AuthPromptParser.Inner_DataUtil.init_DataUtil_DataUtil;
      this.init_StreamUtil = AutoLoginTimings.load_StreamUtil();
      this.init_CachePool_DataUtil = AutoLoginDriver.Inner_DataUtil.IDLE;
      this.close = -9223372036854775808L;
      this.init_Str = "";
      this.load_Str = "";
      this.init_BufferHelper = host;
   }

   public void init(AutoLoginTimings newConfig) {
      if (newConfig != null) {
         this.init_StreamUtil = newConfig;
      }
   }

   public void init_V(long now) {
      this.init_DataUtil_DataUtil = AuthPromptParser.Inner_DataUtil.init_DataUtil_DataUtil;
      this.init_J = now == 0L ? 1L : now;
   }

   public void init(String line) {
      AuthPromptParser.Inner_DataUtil detection = AuthPromptParser.init(line);
      if (detection.init_DataUtil_StreamUti() != AuthPromptParser.Inner_StreamUtil.NONE) {
         this.init_DataUtil_DataUtil = detection;
      }
   }

   public AutoLoginDriver.Inner_DataUtil init_CachePool_DataUtil() {
      return this.init_CachePool_DataUtil;
   }

   public boolean init_Z() {
      AutoLoginDriver.Inner_DataUtil current = this.init_CachePool_DataUtil;
      return current == AutoLoginDriver.Inner_DataUtil.DONE || current == AutoLoginDriver.Inner_DataUtil.GAVE_UP;
   }

   public int init_I() {
      return this.init_I;
   }

   public void load(long now) {
      this.init_V();
      if (this.init_CachePool_DataUtil != AutoLoginDriver.Inner_DataUtil.IDLE && !this.init_Z()) {
         if (now - this.load_J >= this.init_StreamUtil.init_J()) {
            this.init_CachePool_DataUtil = AutoLoginDriver.Inner_DataUtil.GAVE_UP;
         } else if (!this.init_Z(now)) {
            this.read(now);
         }
      }
   }

   private void init_V() {
      long requested = this.init_J;
      if (requested != 0L) {
         this.init_J = 0L;
         this.load_J = requested;
         this.read_J = 0L;
         this.write = 0L;
         this.update = 0L;
         this.sync = 0L;
         this.flush = 0L;
         this.init_Z = false;
         this.init_I = 0;
         this.load_Z = false;
         this.read_Z = false;
         this.close = -9223372036854775808L;
         this.init_Str = "";
         this.load_Str = "";
         this.init_CachePool_DataUtil = AutoLoginDriver.Inner_DataUtil.WATCHING;
      }
   }

   private boolean init_Z(long now) {
      CustomMenuSnapshot snapshot = this.init_BufferHelper.init_CustomMenuSnapshot();
      if (snapshot == null) {
         this.close = -9223372036854775808L;
         return false;
      }

      if (this.init_BufferHelper.read()) {
         return true;
      }

      if (snapshot.generation() == this.close) {
         return true;
      }

      if (now < this.update) {
         return true;
      }

      boolean hasText = false;

      for (CustomMenuInput input : snapshot.inputs()) {
         if (input.kind() == CustomMenuInput.Kind.TEXT) {
            hasText = true;
            break;
         }
      }

      String password = this.init_BufferHelper.init_Str();
      if (hasText && password.isEmpty()) {
         this.load(snapshot.title());
         return true;
      }

      CustomMenuButton button;
      Map<String, String> values;
      try {
         CustomMenuButton accept = hasText ? null : CustomMenuActionPlanner.init(snapshot.buttons());
         button = accept != null ? accept : CustomMenuActionPlanner.read(snapshot.buttons());
         if (button == null) {
            this.update = now + this.init_StreamUtil.write();
            return true;
         }

         values = new LinkedHashMap<>();

         for (CustomMenuInput input : snapshot.inputs()) {
            values.put(input.key(), input.kind() == CustomMenuInput.Kind.TEXT ? password : input.initialValue());
         }
      } catch (RuntimeException error) {
         this.update = now + this.init_StreamUtil.write();
         return true;
      }

      if (!this.init_BufferHelper.init(snapshot, new CustomMenuSubmission(values, button))) {
         this.update = now + this.init_StreamUtil.write();
         return true;
      }

      this.close = snapshot.generation();
      if (hasText) {
         this.init_CachePool_DataUtil = AutoLoginDriver.Inner_DataUtil.LOGGING_IN;
         this.sync = now + this.init_StreamUtil.read();
      }

      return true;
   }

   private void read(long now) {
      if (!this.init_BufferHelper.init_Z()) {
         this.read_J = 0L;
      } else {
         if (this.read_J == 0L) {
            this.read_J = now;
         }

         if (now - this.read_J >= this.init_StreamUtil.load_J()) {
            AuthPromptParser.Inner_DataUtil detection = this.init_DataUtil_DataUtil;
            if (detection.init_DataUtil_StreamUti() != AuthPromptParser.Inner_StreamUtil.NONE) {
               if (now >= this.write) {
                  if (this.init_I >= this.init_StreamUtil.init_I()) {
                     this.load();
                  } else {
                     String password = this.init_BufferHelper.init_Str();
                     if (password.isEmpty()) {
                        this.load("AuthMe login");
                     } else if (this.init_BufferHelper.load()) {
                        this.init_DataUtil_DataUtil = AuthPromptParser.Inner_DataUtil.init_DataUtil_DataUtil;
                        if (!this.init_BufferHelper.init_Z(detection.init(password))) {
                           this.init_DataUtil_DataUtil = detection;
                           this.write = now + 250L;
                        } else {
                           this.init_I++;
                           this.write = now + this.init_StreamUtil.read();
                           if (detection.init_DataUtil_StreamUti() == AuthPromptParser.Inner_StreamUtil.REGISTER) {
                              this.init_Str = detection.init_Str();
                              this.init_CachePool_DataUtil = AutoLoginDriver.Inner_DataUtil.REGISTERING;
                              this.init_Z = true;
                              this.flush = now + this.init_StreamUtil.update();
                           } else {
                              this.load_Str = detection.init_Str();
                              this.init_CachePool_DataUtil = AutoLoginDriver.Inner_DataUtil.LOGGING_IN;
                              this.init_Z = false;
                              this.sync = now + this.init_StreamUtil.read();
                           }
                        }
                     }
                  }
               }
            } else if (this.init_Z && now >= this.flush) {
               this.write(now);
            } else if (this.init_CachePool_DataUtil == AutoLoginDriver.Inner_DataUtil.LOGGING_IN && this.sync != 0L && now >= this.sync) {
               this.init_CachePool_DataUtil = AutoLoginDriver.Inner_DataUtil.DONE;
            }
         }
      }
   }

   private void write(long now) {
      this.init_Z = false;
      if (this.init_I >= this.init_StreamUtil.init_I()) {
         this.load();
      } else {
         String password = this.init_BufferHelper.init_Str();
         if (!password.isEmpty() && this.init_BufferHelper.load()) {
            String token = AuthPromptParser.init(this.init_Str, this.load_Str);
            if (!this.init_BufferHelper.init_Z(token + " " + password)) {
               this.init_Z = true;
               this.flush = now + 250L;
            } else {
               this.init_I++;
               this.write = now + this.init_StreamUtil.read();
               this.init_CachePool_DataUtil = AutoLoginDriver.Inner_DataUtil.LOGGING_IN;
               this.sync = now + this.init_StreamUtil.read();
            }
         }
      }
   }

   private void load() {
      if (!this.load_Z) {
         this.load_Z = true;
         this.init_BufferHelper.init_V("Auto login failed after " + this.init_StreamUtil.init_I() + " attempts - check the password.");
      }

      this.init_CachePool_DataUtil = AutoLoginDriver.Inner_DataUtil.GAVE_UP;
   }

   private void load(String context) {
      if (!this.read_Z) {
         this.read_Z = true;
         this.init_BufferHelper.load(context == null ? "" : context);
      }
   }

   public enum Inner_DataUtil {
      IDLE,
      WATCHING,
      REGISTERING,
      LOGGING_IN,
      DONE,
      GAVE_UP;
   }
}

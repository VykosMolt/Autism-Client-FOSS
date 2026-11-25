package autismclient.util;

import com.google.gson.JsonObject;
import com.mojang.util.UndashedUuid;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import net.minecraft.client.User;
import net.minecraft.core.UUIDUtil;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;

public class BitSetUtil {
   public String init_Str = UUID.randomUUID().toString();
   public MethodUtil init_MethodUtil = MethodUtil.init;
   public String load_Str = "";
   public String read_Str = "";
   public String write = "";
   public String update = "";
   public String sync = "";
   public String flush = "";
   public long init_J;
   private static final long load_J = 300000L;
   public transient volatile BitSetUtil.Inner_DataUtil init_BitSetUtil_DataUti = BitSetUtil.Inner_DataUtil.init;
   private transient String close = "";
   private transient PathHelper.Inner_DataUtil init_PathHelper_DataUti;
   private transient long read_J;
   private transient String open = "";
   private transient boolean load_Z;
   transient boolean init_Z;

   public BitSetUtil() {
   }

   public BitSetUtil(Tag tag) {
      if (tag instanceof CompoundTag compoundTag) {
         this.init_BitSetUtil_CompoundTag(compoundTag);
      }
   }

   public String init_Str() {
      if (this.update != null && !this.update.isBlank()) {
         return this.update;
      } else {
         return this.load_Str == null ? "" : this.load_Str;
      }
   }

   public boolean init_Z() {
      this.close = "";
      boolean var10000;
      switch (this.init_MethodUtil) {
         case init:
         case update:
            var10000 = this.update_Z();
            break;
         case load:
            var10000 = this.sync();
            break;
         case read:
            var10000 = this.flush();
            break;
         case write:
            var10000 = this.close();
            break;
         default:
            throw new MatchException(null, null);
      }

      return var10000;
   }

   public boolean load_Z() {
      this.load_Z = true;

      try {
         return this.init_Z();
      } finally {
         this.load_Z = false;
      }
   }

   public boolean read_Z() {
      this.close = "";
      boolean needFetch = this.update == null || this.update.isBlank() || this.init_MethodUtil == MethodUtil.read && !this.write_Z();
      if (needFetch && !this.init_Z()) {
         return false;
      }

      boolean var10000;
      switch (this.init_MethodUtil) {
         case init:
         case update:
            boolean ok = FieldCache.init(new User(this.update, UUIDUtil.createOfflinePlayerUUID(this.update), "", Optional.empty(), Optional.empty()));
            if (!ok) {
               this.close = FieldCache.init_Str();
            }

            var10000 = ok;
            break;
         case load:
         case read:
            if (this.read_Str != null && !this.read_Str.isBlank() && this.sync != null && !this.sync.isBlank()) {
               boolean ok2 = FieldCache.init(new User(this.update, UndashedUuid.fromStringLenient(this.sync), this.read_Str, Optional.empty(), Optional.empty()));
               if (!ok2) {
                  this.close = FieldCache.init_Str();
               }

               var10000 = ok2;
            } else {
               this.close = "missing token or uuid";
               var10000 = false;
            }
            break;
         case write:
            var10000 = this.open();
            break;
         default:
            throw new MatchException(null, null);
      }

      return var10000;
   }

   private boolean update_Z() {
      if (this.load_Str != null && !this.load_Str.isBlank()) {
         this.update = this.load_Str.trim();
         this.sync = UUIDUtil.createOfflinePlayerUUID(this.update).toString();
         return true;
      } else {
         this.close = "missing username";
         return false;
      }
   }

   private boolean sync() {
      if (this.read_Str != null && !this.read_Str.isBlank()) {
         try {
            JsonObject profile = AutismHttp.load_JsonObject("https://api.minecraftservices.com/minecraft/profile", this.read_Str);
            if (profile != null && profile.has("id") && profile.has("name")) {
               this.sync = profile.get("id").getAsString();
               this.update = profile.get("name").getAsString();
               return true;
            } else {
               this.close = "profile response missing id/name";
               return false;
            }
         } catch (Exception e) {
            this.close = init_Str_Throwable(e);
            return false;
         }
      } else {
         this.close = "missing access token";
         return false;
      }
   }

   public boolean write_Z() {
      return this.read_Str != null
         && !this.read_Str.isBlank()
         && this.update != null
         && !this.update.isBlank()
         && this.sync != null
         && !this.sync.isBlank()
         && this.init_J > System.currentTimeMillis() + 300000L;
   }

   private boolean flush() {
      if (this.load_Str == null || this.load_Str.isBlank()) {
         this.close = "missing refresh token";
         return false;
      } else if (this.write_Z()) {
         return true;
      } else {
         MicrosoftAuth.Inner_StreamUtil data = MicrosoftAuth.init_aW_StreamUtil(this.load_Str);
         if (data.load != null && !data.load.isBlank() && !data.load.equals(this.load_Str)) {
            this.load_Str = data.load;
            this.init_Z = true;
         }

         if (data.init_Z()) {
            this.read_Str = data.init_Str;
            this.sync = data.read;
            this.update = data.write;
            this.init_J = data.init_J();
            return true;
         } else {
            this.close = data.update != null && !data.update.isBlank() ? data.update : "Microsoft login failed";
            return false;
         }
      }
   }

   private synchronized boolean close() {
      if (this.read_Str != null && !this.read_Str.isBlank()) {
         String credential = this.read_Str.strip();

         try {
            PathHelper.Inner_DataUtil session = PathHelper.init(credential);
            if (!credential.equals(this.read_Str == null ? "" : this.read_Str.strip())) {
               throw new IOException("TheAltening token changed during login; try again");
            }

            this.update = session.init_GameProfile().name();
            this.sync = session.init_GameProfile().id().toString();
            this.write = session.init_Str();
            this.init_PathHelper_DataUti = session;
            this.open = credential;
            this.read_J = System.nanoTime();
            return true;
         } catch (Exception e) {
            this.init_V();
            this.close = init_Str_Throwable(e);
            if (!this.load_Z) {
               AutismClientMessaging.sendPrefixed("Failed to fetch TheAltening account info.");
            }

            return false;
         }
      } else {
         this.init_V();
         this.close = "missing TheAltening token";
         return false;
      }
   }

   private synchronized boolean open() {
      if (!this.poll() && !this.close()) {
         return false;
      }

      PathHelper.Inner_DataUtil session = this.init_PathHelper_DataUti;
      boolean ok = FieldCache.load(
         new User(session.init_GameProfile().name(), session.init_GameProfile().id(), session.init_Str(), Optional.empty(), Optional.empty())
      );
      if (!ok) {
         this.close = FieldCache.init_Str();
      }

      return ok;
   }

   private boolean poll() {
      long age = System.nanoTime() - this.read_J;
      return this.init_PathHelper_DataUti != null
         && this.read_Str != null
         && !this.read_Str.isBlank()
         && this.read_Str.strip().equals(this.open)
         && age >= 0L
         && age < TimeUnit.MINUTES.toNanos(5L);
   }

   private void init_V() {
      this.init_PathHelper_DataUti = null;
      this.open = "";
      this.read_J = 0L;
      this.write = "";
   }

   public String load_Str() {
      return this.close == null ? "" : this.close;
   }

   public String read_Str() {
      String error = this.load_Str();
      return error.isBlank() ? "" : " (" + error + ")";
   }

   private static String init_Str_Throwable(Throwable error) {
      if (error == null) {
         return "unknown error";
      }

      String name = error.getClass().getSimpleName();
      String message = error.getMessage();
      if (message != null && !message.isBlank()) {
         message = message.replace('\n', ' ').replace('\r', ' ').trim();
         if (message.length() > 120) {
            message = message.substring(0, 117) + "...";
         }

         return name + ": " + message;
      } else {
         return name;
      }
   }

   public CompoundTag init_CompoundTag() {
      CompoundTag tag = new CompoundTag();
      tag.putString("id", this.write_Str());
      tag.putString("type", this.init_MethodUtil == null ? MethodUtil.init.name() : this.init_MethodUtil.name());
      tag.putString("username", this.update == null ? "" : this.update);
      tag.putString("uuid", this.sync == null ? "" : this.sync);
      tag.putString("encToken", init_Str_Str(this.read_Str));
      tag.putString("encSessionToken", init_Str_Str(this.write));
      tag.putString("encPassword", init_Str_Str(this.flush));
      if (this.init_MethodUtil == MethodUtil.read) {
         tag.putString("encLabel", init_Str_Str(this.load_Str));
      } else {
         tag.putString("label", this.load_Str == null ? "" : this.load_Str);
      }

      tag.putLong("sessionExpiresAt", this.init_J);
      return tag;
   }

   public BitSetUtil init_BitSetUtil_CompoundTag(CompoundTag tag) {
      String storedId = tag.getStringOr("id", "");
      if (storedId.isBlank()) {
         this.init_Str = UUID.randomUUID().toString();
         this.init_Z = true;
      } else {
         this.init_Str = storedId;
      }

      String typeName = tag.getStringOr("type", MethodUtil.init.name());

      try {
         this.init_MethodUtil = MethodUtil.valueOf(typeName);
      } catch (IllegalArgumentException e) {
         this.init_MethodUtil = MethodUtil.init;
      }

      this.update = tag.getStringOr("username", "");
      this.sync = tag.getStringOr("uuid", "");
      this.read_Str = init(tag.getStringOr("encToken", ""), tag.getStringOr("token", ""));
      this.write = init(tag.getStringOr("encSessionToken", ""), tag.getStringOr("sessionToken", ""));
      this.flush = init(tag.getStringOr("encPassword", ""), tag.getStringOr("password", ""));
      String plainLabel = tag.getStringOr("label", tag.getStringOr("name", ""));
      String encLabel = tag.getStringOr("encLabel", "");
      this.load_Str = encLabel.isEmpty() ? plainLabel : init(encLabel, plainLabel);
      this.init_J = tag.getLongOr("sessionExpiresAt", 0L);
      return this;
   }

   public String write_Str() {
      if (this.init_Str == null || this.init_Str.isBlank()) {
         this.init_Str = UUID.randomUUID().toString();
         this.init_Z = true;
      }

      return this.init_Str;
   }

   private static String init_Str_Str(String value) {
      if (value != null && !value.isEmpty()) {
         byte[] sealed = autismclient.util.mm.a.DataUtil.init(value.getBytes(StandardCharsets.UTF_8));
         return sealed == null ? "" : Base64.getEncoder().encodeToString(sealed);
      } else {
         return "";
      }
   }

   private static String init(String encoded, String plainFallback) {
      String fallback = plainFallback == null ? "" : plainFallback;
      if (encoded != null && !encoded.isEmpty()) {
         try {
            byte[] plain = autismclient.util.mm.a.DataUtil.read(Base64.getDecoder().decode(encoded));
            return plain == null ? fallback : new String(plain, StandardCharsets.UTF_8);
         } catch (Throwable t) {
            return fallback;
         }
      } else {
         return fallback;
      }
   }

   @Override
   public boolean equals(Object obj) {
      if (this == obj) {
         return true;
      } else if (obj instanceof BitSetUtil account) {
         return this.init_MethodUtil != account.init_MethodUtil ? false : Objects.equals(this.update_Str(), account.update_Str());
      } else {
         return false;
      }
   }

   @Override
   public int hashCode() {
      return Objects.hash(this.init_MethodUtil, this.update_Str());
   }

   private String update_Str() {
      String var10000;
      switch (this.init_MethodUtil) {
         case init:
         case update:
            var10000 = this.update != null && !this.update.isBlank() ? this.update : this.load_Str;
            break;
         case load:
         case write:
            var10000 = this.read_Str != null && !this.read_Str.isBlank() ? this.read_Str : this.load_Str;
            break;
         case read:
            var10000 = this.load_Str != null && !this.load_Str.isBlank() ? this.load_Str : this.update;
            break;
         default:
            throw new MatchException(null, null);
      }

      return var10000;
   }

   public enum Inner_DataUtil {
      init,
      load,
      read,
      write;
   }
}

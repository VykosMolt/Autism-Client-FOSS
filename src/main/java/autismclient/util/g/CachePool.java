package autismclient.util.g;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;

public final class CachePool {
   public static final int init_I = 6;
   public String init_Str = "/tpahere {bot}";
   public String load_Str = "/tpa {bot}";
   public String read_Str = "/trade {bot}";
   public boolean init_Z = true;
   public String write_Str = "/tpaccept {name}";
   public boolean load_Z = false;
   public String update_Str = "";
   public int load_I = 15000;
   public int read_I = 300;
   public boolean read_Z = true;
   public String sync = "/trade accept";
   public boolean write_Z = false;
   public String flush = "";
   public int write_I = 15000;
   public int update_I = 300;
   public final List<CachePool.Inner_DataUtil> init_List = new ArrayList<>();

   public CachePool() {
   }

   public CachePool(CachePool source) {
      if (source != null) {
         this.init_Str = source.init_Str;
         this.load_Str = source.load_Str;
         this.read_Str = source.read_Str;
         this.init_Z = source.init_Z;
         this.write_Str = source.write_Str;
         this.load_Z = source.load_Z;
         this.update_Str = source.update_Str;
         this.load_I = source.load_I;
         this.read_I = source.read_I;
         this.read_Z = source.read_Z;
         this.sync = source.sync;
         this.write_Z = source.write_Z;
         this.flush = source.flush;
         this.write_I = source.write_I;
         this.update_I = source.update_I;
         this.init_List.addAll(source.init_List);
      }
   }

   public void init_V() {
      this.init_Str = init(this.init_Str, "/tpahere {bot}");
      this.load_Str = init(this.load_Str, "/tpa {bot}");
      this.read_Str = init(this.read_Str, "/trade {bot}");
      this.write_Str = init(this.write_Str, "/tpaccept {name}");
      this.sync = init(this.sync, "/trade accept");
      this.update_Str = this.update_Str == null ? "" : this.update_Str.trim();
      this.flush = this.flush == null ? "" : this.flush.trim();
      this.load_I = init(this.load_I);
      this.read_I = load(this.read_I);
      this.write_I = init(this.write_I);
      this.update_I = load(this.update_I);
      this.init_List.removeIf(r -> r == null || !r.init_Z());

      while (this.init_List.size() > 6) {
         this.init_List.remove(this.init_List.size() - 1);
      }
   }

   public static String init(String template, String botName, String userName) {
      String out = template == null ? "" : template;
      out = load(out, "{bot}", botName == null ? "" : botName);
      out = load(out, "{name}", userName == null ? "" : userName);
      out = load(out, "{me}", userName == null ? "" : userName);
      return out.trim();
   }

   private static String load(String in, String token, String value) {
      StringBuilder sb = new StringBuilder();
      String lower = in.toLowerCase(Locale.ROOT);
      String tok = token.toLowerCase(Locale.ROOT);
      int i = 0;

      while (i < in.length()) {
         int at = lower.indexOf(tok, i);
         if (at < 0) {
            sb.append(in, i, in.length());
            break;
         }

         sb.append(in, i, at).append(value);
         i = at + tok.length();
      }

      return sb.toString();
   }

   private static String init(String value, String fallback) {
      String v = value == null ? "" : value.trim();
      return v.isEmpty() ? fallback : v;
   }

   private static int init(int value) {
      return Math.max(2000, Math.min(60000, value));
   }

   private static int load(int value) {
      return Math.max(0, Math.min(10000, value));
   }

   public CompoundTag init_CompoundTag() {
      this.init_V();
      CompoundTag tag = new CompoundTag();
      tag.putString("tpaToMe", this.init_Str);
      tag.putString("tpaToBot", this.load_Str);
      tag.putString("tradeCmd", this.read_Str);
      tag.putBoolean("tpaEnabled", this.init_Z);
      tag.putString("tpaAccept", this.write_Str);
      tag.putBoolean("tpaUseMacro", this.load_Z);
      tag.putString("tpaMacro", this.update_Str);
      tag.putInt("tpaArmMs", this.load_I);
      tag.putInt("tpaDelayMs", this.read_I);
      tag.putBoolean("tradeEnabled", this.read_Z);
      tag.putString("tradeAccept", this.sync);
      tag.putBoolean("tradeUseMacro", this.write_Z);
      tag.putString("tradeMacro", this.flush);
      tag.putInt("tradeArmMs", this.write_I);
      tag.putInt("tradeDelayMs", this.update_I);
      ListTag list = new ListTag();

      for (CachePool.Inner_DataUtil responder : this.init_List) {
         list.add(responder.init_CompoundTag());
      }

      tag.put("responders", list);
      return tag;
   }

   public static CachePool init(CompoundTag tag) {
      CachePool auto = new CachePool();
      auto.init_Str = tag.getStringOr("tpaToMe", auto.init_Str);
      auto.load_Str = tag.getStringOr("tpaToBot", auto.load_Str);
      auto.read_Str = tag.getStringOr("tradeCmd", auto.read_Str);
      auto.init_Z = tag.getBooleanOr("tpaEnabled", true);
      auto.write_Str = tag.getStringOr("tpaAccept", auto.write_Str);
      auto.load_Z = tag.getBooleanOr("tpaUseMacro", false);
      auto.update_Str = tag.getStringOr("tpaMacro", "");
      auto.read_Z = tag.getBooleanOr("tradeEnabled", true);
      auto.sync = tag.getStringOr("tradeAccept", auto.sync);
      auto.write_Z = tag.getBooleanOr("tradeUseMacro", false);
      auto.flush = tag.getStringOr("tradeMacro", "");
      int legacyArm = tag.getIntOr("armWindowMs", 15000);
      int legacyDelay = tag.getIntOr("acceptDelayMs", 300);
      auto.load_I = tag.getIntOr("tpaArmMs", legacyArm);
      auto.read_I = tag.getIntOr("tpaDelayMs", legacyDelay);
      auto.write_I = tag.getIntOr("tradeArmMs", legacyArm);
      auto.update_I = tag.getIntOr("tradeDelayMs", legacyDelay);
      auto.init_List.clear();

      for (Tag value : tag.getListOrEmpty("responders")) {
         if (value instanceof CompoundTag compound) {
            auto.init_List.add(CachePool.Inner_DataUtil.init(compound));
         }
      }

      auto.init_V();
      return auto;
   }

   public record Inner_DataUtil(String init_Str, String load, boolean init_Z, String read, int init_I) {
      public Inner_DataUtil {
         init_Str = init_Str == null ? "" : init_Str.trim();
         load = load == null ? "" : load.trim();
         read = read == null ? "" : read.trim();
         init_I = Math.max(0, Math.min(60000, init_I));
      }

      public boolean init_Z() {
         return !this.init_Str.isBlank() && (this.init_Z ? !this.read.isBlank() : !this.load.isBlank());
      }

      CompoundTag init_CompoundTag() {
         CompoundTag tag = new CompoundTag();
         tag.putString("trigger", this.init_Str);
         tag.putString("response", this.load);
         tag.putBoolean("useMacro", this.init_Z);
         tag.putString("macroName", this.read);
         tag.putInt("delayMs", this.init_I);
         return tag;
      }

      static CachePool.Inner_DataUtil init(CompoundTag tag) {
         return new CachePool.Inner_DataUtil(
            tag.getStringOr("trigger", ""),
            tag.getStringOr("response", ""),
            tag.getBooleanOr("useMacro", false),
            tag.getStringOr("macroName", ""),
            tag.getIntOr("delayMs", 0)
         );
      }

      public String load_Str() {
         return this.load;
      }

      public boolean load_Z() {
         return this.init_Z;
      }
   }
}

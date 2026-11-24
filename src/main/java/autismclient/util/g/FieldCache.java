package autismclient.util.g;

import autismclient.util.aL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;

public final class FieldCache {
   public static final int init_I = 500;
   public static final String init_Str = "default";
   public static final String load_Str = "best";
   public static final int load_I = 4;
   public String read_Str = UUID.randomUUID().toString();
   public String write_Str = "New profile";
   public String update_Str = "";
   public FieldCache.Inner_StreamUtil init_FieldCache_StreamU = FieldCache.Inner_StreamUtil.load_FieldCache_StreamU;
   public FieldCache.Inner_CachePool init_FieldCache_CachePo = FieldCache.Inner_CachePool.init;
   public int read_I = 4;
   public int write_I = 250;
   public int update_I = 200;
   public String sync = "";
   public boolean init_Z;
   public FieldCache.Inner_DataUtil init_FieldCache_DataUti = FieldCache.Inner_DataUtil.load;
   public final List<FieldCache.Inner_BufferHelper> init_List = new ArrayList<>();
   private final Map<String, Map<String, String>> init_Map = new LinkedHashMap<>();
   public IoWorker init_IoWorker = new IoWorker();
   public CachePool init_CachePool = new CachePool();
   public final List<ListPool> load_List = new ArrayList<>(init_List());

   public FieldCache() {
   }

   public FieldCache(FieldCache source) {
      if (source != null) {
         this.read_Str = source.read_Str;
         this.write_Str = source.write_Str;
         this.update_Str = source.update_Str;
         this.init_FieldCache_StreamU = source.init_FieldCache_StreamU;
         this.init_FieldCache_CachePo = source.init_FieldCache_CachePo;
         this.read_I = source.read_I;
         this.write_I = source.write_I;
         this.update_I = source.update_I;
         this.sync = source.sync;
         this.init_Z = source.init_Z;
         this.init_FieldCache_DataUti = source.init_FieldCache_DataUti;
         this.init_List.addAll(source.init_List);
         source.init_Map.forEach((account, values) -> this.init_Map.put(account, new LinkedHashMap<>((Map<? extends String, ? extends String>)values)));
         this.init_IoWorker = new IoWorker(source.init_IoWorker);
         this.init_CachePool = new CachePool(source.init_CachePool);
         this.load_List.clear();

         for (ListPool action : source.load_List) {
            this.load_List.add(new ListPool(action));
         }

         this.init_V();
      }
   }

   public int init_I() {
      return this.init_FieldCache_StreamU == FieldCache.Inner_StreamUtil.update ? this.read_I : this.init_FieldCache_StreamU.init();
   }

   public int load_I() {
      return this.init_FieldCache_StreamU == FieldCache.Inner_StreamUtil.update ? this.write_I : this.init_FieldCache_StreamU.load();
   }

   public void init_V() {
      if (this.read_Str == null || this.read_Str.isBlank()) {
         this.read_Str = UUID.randomUUID().toString();
      }

      this.write_Str = this.write_Str != null && !this.write_Str.isBlank() ? this.write_Str.trim() : "New profile";
      this.update_Str = this.update_Str == null ? "" : this.update_Str.trim();
      if (this.init_FieldCache_StreamU == null) {
         this.init_FieldCache_StreamU = FieldCache.Inner_StreamUtil.load_FieldCache_StreamU;
      }

      if (this.init_FieldCache_CachePo == null) {
         this.init_FieldCache_CachePo = FieldCache.Inner_CachePool.init;
      }

      this.sync = this.sync == null ? "" : this.sync.trim();
      this.read_I = Math.max(1, Math.min(500, this.read_I));
      this.write_I = Math.max(0, Math.min(5000, this.write_I));
      this.update_I = Math.max(50, Math.min(1000, this.update_I));
      if (this.init_IoWorker == null) {
         this.init_IoWorker = new IoWorker();
      }

      if (this.init_CachePool == null) {
         this.init_CachePool = new CachePool();
      }

      this.init_CachePool.init_V();
      this.read();
      Set<String> accounts = new HashSet<>();
      this.init_List.removeIf(spec -> spec == null || spec.init_Str().isBlank() || !accounts.add(spec.init_Str()));

      while (this.init_List.size() > 500) {
         this.init_List.remove(this.init_List.size() - 1);
      }

      this.init_Map.keySet().removeIf(account -> !accounts.contains(account));
      this.init_Map
         .values()
         .forEach(
            values -> values.entrySet().removeIf(entry -> init_Str_Str(entry.getKey()).isEmpty() || entry.getValue() == null || entry.getValue().isBlank())
         );
      this.init_Map.entrySet().removeIf(entry -> entry.getValue().isEmpty());
   }

   public boolean init(String accountId, String name, String value) {
      String account = accountId == null ? "" : accountId.trim();
      String key = init_Str_Str(name);
      if (!account.isEmpty() && !key.isEmpty() && value != null) {
         byte[] sealed = autismclient.util.mm.a.DataUtil.init(value.getBytes(StandardCharsets.UTF_8));
         this.init_Map.computeIfAbsent(account, ignored -> new LinkedHashMap<>()).put(key, Base64.getEncoder().encodeToString(sealed));
         return true;
      } else {
         return false;
      }
   }

   public void init_V(String accountId, String name) {
      Map<String, String> values = this.init_Map.get(accountId == null ? "" : accountId.trim());
      if (values != null) {
         values.remove(init_Str_Str(name));
         if (values.isEmpty()) {
            this.init_Map.remove(accountId == null ? "" : accountId.trim());
         }
      }
   }

   public void init_V_Str(String accountId) {
      this.init_Map.remove(accountId == null ? "" : accountId.trim());
   }

   public Set<String> init_Set_Str(String accountId) {
      Map<String, String> values = this.init_Map.get(accountId == null ? "" : accountId.trim());
      return values == null ? Set.of() : Collections.unmodifiableSet(new LinkedHashSet<>(values.keySet()));
   }

   public Map<String, String> init_Map_Str(String accountId) {
      Map<String, String> sealed = this.init_Map.get(accountId == null ? "" : accountId.trim());
      if (sealed != null && !sealed.isEmpty()) {
         Map<String, String> opened = new LinkedHashMap<>();
         sealed.forEach((name, encoded) -> {
            try {
               byte[] plain = autismclient.util.mm.a.DataUtil.read(Base64.getDecoder().decode(encoded));
               if (plain != null) {
                  opened.put(name, new String(plain, StandardCharsets.UTF_8));
               }
            } catch (IllegalArgumentException var4) {
            }
         });
         return Map.copyOf(opened);
      } else {
         return Map.of();
      }
   }

   public static String init_Str_Str(String name) {
      return aL.init(name);
   }

   public ListPool init_ListPool_I(int index) {
      if (index >= 0 && index < 4) {
         this.read();
         return new ListPool(this.load_List.get(index));
      } else {
         throw new IndexOutOfBoundsException(index);
      }
   }

   public void init(int index, ListPool action) {
      if (index >= 0 && index < 4) {
         this.read();
         this.load_List.set(index, action == null ? new ListPool() : new ListPool(action));
      } else {
         throw new IndexOutOfBoundsException(index);
      }
   }

   private void read() {
      while (this.load_List.size() < 4) {
         this.load_List.add(new ListPool());
      }

      while (this.load_List.size() > 4) {
         this.load_List.remove(this.load_List.size() - 1);
      }

      for (int i = 0; i < this.load_List.size(); i++) {
         ListPool action = this.load_List.get(i);
         if (action == null) {
            action = new ListPool();
         }

         action.init_V();
         this.load_List.set(i, action);
      }
   }

   public void load_V() {
      this.load_List.clear();
      this.load_List.addAll(init_List());
   }

   public boolean init_Z(String oldName, String newName) {
      String before = oldName == null ? "" : oldName.trim();
      if (before.isBlank()) {
         return false;
      }

      String after = newName == null ? "" : newName.trim();
      boolean changed = false;
      if (before.equals(this.sync)) {
         this.sync = after;
         changed = true;
      }

      for (int i = 0; i < this.init_List.size(); i++) {
         FieldCache.Inner_BufferHelper spec = this.init_List.get(i);
         if (before.equals(spec.read())) {
            this.init_List.set(i, spec.init_FieldCache_BufferH_Str(after));
            changed = true;
         }
      }

      return changed;
   }

   public CompoundTag init_CompoundTag() {
      this.init_V();
      CompoundTag tag = new CompoundTag();
      tag.putString("id", this.read_Str);
      tag.putString("name", this.write_Str);
      tag.putString("server", this.update_Str);
      tag.putString("pacing", this.init_FieldCache_StreamU.name());
      tag.putString("proxyMode", this.init_FieldCache_CachePo.name());
      tag.putInt("customConcurrency", this.read_I);
      tag.putInt("customDelayMs", this.write_I);
      tag.putInt("autoMaxPingMs", this.update_I);
      tag.putString("allMacroName", this.sync);
      tag.putBoolean("runMacroWhileJoining", this.init_Z);
      tag.putString("loginMode", this.init_FieldCache_DataUti.name());
      ListTag sessionTags = new ListTag();

      for (FieldCache.Inner_BufferHelper session : this.init_List) {
         sessionTags.add(session.init_CompoundTag());
      }

      tag.put("sessions", sessionTags);
      ListTag formAccounts = new ListTag();
      this.init_Map.forEach((accountId, values) -> {
         CompoundTag accountTag = new CompoundTag();
         accountTag.putString("accountId", accountId);
         ListTag valueTags = new ListTag();
         values.forEach((key, sealed) -> {
            CompoundTag valueTag = new CompoundTag();
            valueTag.putString("name", key);
            valueTag.putString("sealed", sealed);
            valueTags.add(valueTag);
         });
         accountTag.put("values", valueTags);
         formAccounts.add(accountTag);
      });
      tag.put("formValues", formAccounts);
      tag.put("packetPolicy", this.init_IoWorker.init_CompoundTag());
      tag.put("autoAccept", this.init_CachePool.init_CompoundTag());
      ListTag actionTags = new ListTag();

      for (ListPool action : this.load_List) {
         actionTags.add(action.init_CompoundTag());
      }

      tag.put("quickActions", actionTags);
      return tag;
   }

   public static FieldCache init_FieldCache_CompoundTag(CompoundTag tag) {
      FieldCache profile = new FieldCache();
      profile.read_Str = tag.getStringOr("id", profile.read_Str);
      profile.write_Str = tag.getStringOr("name", profile.write_Str);
      profile.update_Str = tag.getStringOr("server", "");
      String pacingName = tag.getStringOr("pacing", FieldCache.Inner_StreamUtil.load_FieldCache_StreamU.name());
      profile.init_FieldCache_StreamU = FieldCache.Inner_StreamUtil.load_FieldCache_StreamU;

      for (FieldCache.Inner_StreamUtil value : FieldCache.Inner_StreamUtil.values()) {
         if (value.name().equalsIgnoreCase(pacingName)) {
            profile.init_FieldCache_StreamU = value;
            break;
         }
      }

      profile.read_I = tag.getIntOr("customConcurrency", 4);
      profile.write_I = tag.getIntOr("customDelayMs", 250);
      profile.update_I = tag.getIntOr("autoMaxPingMs", 200);
      profile.sync = tag.getStringOr("allMacroName", "");
      profile.init_Z = tag.getBooleanOr("runMacroWhileJoining", false);
      profile.init_FieldCache_DataUti = FieldCache.Inner_DataUtil.load;
      String loginModeName = tag.getStringOr("loginMode", "");

      for (FieldCache.Inner_DataUtil value : FieldCache.Inner_DataUtil.values()) {
         if (value.name().equalsIgnoreCase(loginModeName)) {
            profile.init_FieldCache_DataUti = value;
            break;
         }
      }

      profile.init_List.clear();

      for (Tag value : tag.getListOrEmpty("sessions")) {
         if (value instanceof CompoundTag compound) {
            profile.init_List.add(FieldCache.Inner_BufferHelper.init_FieldCache_BufferH_CompoundTag(compound));
         }
      }

      profile.init_Map.clear();

      for (Tag accountValue : tag.getListOrEmpty("formValues")) {
         if (accountValue instanceof CompoundTag accountTag) {
            String accountId = accountTag.getStringOr("accountId", "").trim();
            if (!accountId.isEmpty()) {
               Map<String, String> values = new LinkedHashMap<>();

               for (Tag formValue : accountTag.getListOrEmpty("values")) {
                  if (formValue instanceof CompoundTag valueTag) {
                     String key = init_Str_Str(valueTag.getStringOr("name", ""));
                     String sealed = valueTag.getStringOr("sealed", "");
                     if (!key.isEmpty() && !sealed.isBlank()) {
                        values.put(key, sealed);
                     }
                  }
               }

               if (!values.isEmpty()) {
                  profile.init_Map.put(accountId, values);
               }
            }
         }
      }

      String proxyModeName = tag.getStringOr("proxyMode", "");
      if (proxyModeName.isBlank()) {
         boolean anyProxy = profile.init_List.stream().anyMatch(spec -> !spec.load_Str().isBlank());
         profile.init_FieldCache_CachePo = anyProxy ? FieldCache.Inner_CachePool.read : FieldCache.Inner_CachePool.init;
      } else {
         profile.init_FieldCache_CachePo = FieldCache.Inner_CachePool.init;

         for (FieldCache.Inner_CachePool value : FieldCache.Inner_CachePool.values()) {
            if (value.name().equalsIgnoreCase(proxyModeName)) {
               profile.init_FieldCache_CachePo = value;
               break;
            }
         }
      }

      profile.init_IoWorker = tag.getCompound("packetPolicy").map(IoWorker::init_IoWorker_CompoundTag).orElseGet(IoWorker::new);
      profile.init_CachePool = tag.getCompound("autoAccept").map(CachePool::init).orElseGet(CachePool::new);
      profile.load_List.clear();
      ListTag actionTags = tag.getListOrEmpty("quickActions");
      if (actionTags.isEmpty()) {
         profile.load_List.addAll(init_List_IoWorker(profile.init_IoWorker));
      } else {
         for (Tag value : actionTags) {
            if (value instanceof CompoundTag compound) {
               profile.load_List.add(ListPool.init_ListPool_CompoundTag(compound));
            }
         }
      }

      profile.init_V();
      return profile;
   }

   public static List<ListPool> init_List() {
      List<ListPool> actions = new ArrayList<>(4);

      for (int i = 0; i < 4; i++) {
         actions.add(new ListPool());
      }

      return actions;
   }

   private static List<ListPool> init_List_IoWorker(IoWorker policy) {
      List<ListPool> actions = init_List();
      if (policy == null) {
         return actions;
      }

      List<IoWorker.Inner_BufferHelper> slots = policy.init_List();

      for (int i = 0; i < Math.min(4, slots.size()); i++) {
         IoWorker.Inner_BufferHelper slot = slots.get(i);
         if (slot != null && slot.init_Z() && !slot.init_Str().isBlank()) {
            actions.set(i, new ListPool("Slot " + (i + 1), slot.init_Str(), ""));
         }
      }

      return actions;
   }

   public record Inner_BufferHelper(String init, String load, String read) {
      public Inner_BufferHelper {
         init = init == null ? "" : init.trim();
         load = load == null ? "" : load.trim();
         read = read == null ? "" : read.trim();
      }

      public Inner_BufferHelper(String accountId, String proxyId) {
         this(accountId, proxyId, "");
      }

      public boolean init_Z() {
         return this.load.isBlank();
      }

      public boolean load_Z() {
         return "best".equals(this.load);
      }

      public FieldCache.Inner_BufferHelper init_FieldCache_BufferH_Str(String macro) {
         return new FieldCache.Inner_BufferHelper(this.init, this.load, macro);
      }

      CompoundTag init_CompoundTag() {
         CompoundTag tag = new CompoundTag();
         tag.putString("accountId", this.init);
         tag.putString("proxyId", this.load);
         if (!this.read.isBlank()) {
            tag.putString("macroName", this.read);
         }

         return tag;
      }

      static FieldCache.Inner_BufferHelper init_FieldCache_BufferH_CompoundTag(CompoundTag tag) {
         return new FieldCache.Inner_BufferHelper(tag.getStringOr("accountId", ""), tag.getStringOr("proxyId", ""), tag.getStringOr("macroName", ""));
      }

      public String init_Str() {
         return this.init;
      }

      public String load_Str() {
         return this.load;
      }
   }

   public enum Inner_CachePool {
      init,
      load,
      read;
   }

   public enum Inner_DataUtil {
      init,
      load,
      read;
   }

   public enum Inner_StreamUtil {
      init_FieldCache_StreamU(1, 1000),
      load_FieldCache_StreamU(4, 250),
      read(8, 100),
      write(500, 0),
      update(4, 250);

      private final int init_I;
      private final int load_I;

      Inner_StreamUtil(int concurrency, int delayMs) {
         this.init_I = concurrency;
         this.load_I = delayMs;
      }

      public int init() {
         return this.init_I;
      }

      public int load() {
         return this.load_I;
      }
   }
}

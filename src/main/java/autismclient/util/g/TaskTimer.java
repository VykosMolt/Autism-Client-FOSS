package autismclient.util.g;

import autismclient.util.df;
import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.CompoundTag;

public final class TaskTimer extends df<FieldCache> implements Iterable<FieldCache> {
   private static final TaskTimer init_TaskTimer = new TaskTimer();
   private static final int init_I = 64;
   private static final Pattern init_Pattern = Pattern.compile("^(.*) \\((\\d+)\\)$");
   private long init_J_2;
   private boolean init_Z_2;
   private String init_Str = "";

   private TaskTimer() {
   }

   public static TaskTimer init_TaskTimer() {
      init_TaskTimer.load_V();
      init_TaskTimer.init_V_3();
      return init_TaskTimer;
   }

   @Override
   protected File init_File() {
      return new File(Minecraft.getInstance().gameDirectory, "autism-multi.nbt");
   }

   @Override
   protected String init_Str() {
      return "profiles";
   }

   @Override
   protected FieldCache init_Obj_CompoundTag(CompoundTag tag) {
      return FieldCache.init_FieldCache_CompoundTag(tag);
   }

   @Override
   protected CompoundTag init_CompoundTag_Obj(FieldCache item) {
      return item.init_CompoundTag();
   }

   @Override
   protected String load_Str() {
      return "Multi profiles";
   }

   @Override
   protected void load_V_CompoundTag(CompoundTag root) {
      root.putString("selected", this.init_Str == null ? "" : this.init_Str);
   }

   @Override
   protected void init_V_CompoundTag(CompoundTag root) {
      this.init_Str = root.getStringOr("selected", "");
   }

   public synchronized String read_Str() {
      if (this.init_Str != null && !this.init_Str.isBlank()) {
         for (FieldCache profile : this.init_List) {
            if (this.init_Str.equals(profile.read_Str)) {
               return this.init_Str;
            }
         }
      }

      return this.init_List.isEmpty() ? "" : this.init_List.get(0).read_Str;
   }

   public synchronized void init_V_Str(String id) {
      String value = id == null ? "" : id;
      if (!value.equals(this.init_Str)) {
         this.init_Str = value;
         this.read_V();
      }
   }

   public synchronized void init_V_FieldCache(FieldCache profile) {
      if (profile != null) {
         profile.init_V();
         profile.write_Str = init_Str(profile.write_Str, profile.read_Str, this.init_List);

         for (int i = 0; i < this.init_List.size(); i++) {
            if (this.init_List.get(i).read_Str.equals(profile.read_Str)) {
               this.init_List.set(i, new FieldCache(profile));
               this.init_J_2++;
               this.read_V();
               return;
            }
         }

         this.init_List.add(new FieldCache(profile));
         this.init_J_2++;
         this.read_V();
      }
   }

   public synchronized void load(String id) {
      if (id != null && this.init_List.removeIf(profile -> id.equals(profile.read_Str))) {
         this.init_J_2++;
         this.read_V();
      }
   }

   public synchronized FieldCache init_FieldCache_Str(String id) {
      if (id == null) {
         return null;
      }

      for (FieldCache profile : this.init_List) {
         if (id.equals(profile.read_Str)) {
            return new FieldCache(profile);
         }
      }

      return null;
   }

   @Override
   public synchronized List<FieldCache> init_List() {
      List<FieldCache> copies = new ArrayList<>(this.init_List.size());

      for (FieldCache profile : this.init_List) {
         copies.add(new FieldCache(profile));
      }

      return copies;
   }

   public synchronized long init_J_2() {
      return this.init_J_2;
   }

   public synchronized String init_Str(String requestedName, String profileId) {
      return init_Str(requestedName, profileId, this.init_List);
   }

   static String init_Str(String requestedName, String profileId, List<FieldCache> profiles) {
      String requested = init_Str_Str_2(requestedName);
      if (!init_Z(requested, profileId, profiles)) {
         return requested;
      }

      String base = requested;
      long number = 1L;
      Matcher matcher = init_Pattern.matcher(requested);
      if (matcher.matches() && !matcher.group(1).isBlank()) {
         base = matcher.group(1).stripTrailing();

         try {
            number = Math.max(1L, Long.parseLong(matcher.group(2)) + 1L);
         } catch (NumberFormatException ignored) {
            number = 1L;
         }
      }

      while (true) {
         String suffix = " (" + number + ")";
         int baseLimit = Math.max(1, 64 - suffix.length());
         String fittedBase = base.length() > baseLimit ? base.substring(0, baseLimit).stripTrailing() : base;
         if (fittedBase.isBlank()) {
            fittedBase = "Profile";
         }

         String candidate = fittedBase + suffix;
         if (!init_Z(candidate, profileId, profiles)) {
            return candidate;
         }

         number++;
      }
   }

   private static String init_Str_Str_2(String requestedName) {
      String value = PacketUtil.update(requestedName == null ? "" : requestedName, 64);
      return value.isBlank() ? "New profile" : value;
   }

   private static boolean init_Z(String name, String profileId, List<FieldCache> profiles) {
      if (profiles != null && !profiles.isEmpty()) {
         String key = name.toLowerCase(Locale.ROOT);

         for (FieldCache profile : profiles) {
            if (profile != null
               && (profileId == null || !profileId.equals(profile.read_Str))
               && init_Str_Str_2(profile.write_Str).toLowerCase(Locale.ROOT).equals(key)) {
               return true;
            }
         }

         return false;
      } else {
         return false;
      }
   }

   private synchronized void init_V_3() {
      if (!this.init_Z_2) {
         this.init_Z_2 = true;
         List<FieldCache> accepted = new ArrayList<>(this.init_List.size());
         boolean changed = false;

         for (FieldCache profile : this.init_List) {
            String unique = init_Str(profile.write_Str, null, accepted);
            if (!unique.equals(profile.write_Str)) {
               profile.write_Str = unique;
               changed = true;
            }

            accepted.add(profile);
         }

         if (changed) {
            this.init_J_2++;
            this.read_V();
         }
      }
   }

   public synchronized int init_I(String oldName, String newName) {
      String before = oldName == null ? "" : oldName.trim();
      if (before.isBlank()) {
         return 0;
      }

      String after = newName == null ? "" : newName.trim();
      int changed = 0;

      for (FieldCache profile : this.init_List) {
         boolean touched = profile.init_Z(before, after);
         if (touched) {
            profile.init_V();
            changed++;
         }
      }

      if (changed > 0) {
         this.init_J_2++;
         this.read_V();
      }

      return changed;
   }

   @Override
   public Iterator<FieldCache> iterator() {
      return this.init_List().iterator();
   }
}

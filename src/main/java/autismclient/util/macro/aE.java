package autismclient.util.macro;

import autismclient.util.cc;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;

public class aE implements CacheEntry, MacroAction, aP {
   public List<String> soundIds = new ArrayList<>();
   public boolean waitForGuiBefore = false;
   public boolean waitForGuiAfter = false;
   public String waitGuiName = "";
   public boolean checkDistance = false;
   public double maxDistance = 16.0;
   public boolean listenDuringPreviousAction = false;
   public String saveAs = "";
   public transient volatile String matchedSoundId = "";
   public transient volatile double matchedX;
   public transient volatile double matchedY;
   public transient volatile double matchedZ;
   private boolean init = true;

   @Override
   public StreamGate getType() {
      return StreamGate.f_c_StreamGate;
   }

   @Override
   public void execute(Minecraft mc) {
   }

   @Override
   public String getDisplayName() {
      if (this.soundIds.isEmpty()) {
         return "Wait Sound: Any" + aP.init_Str_aP(this);
      }

      String first = cc.write(this.soundIds.get(0));
      String s = this.soundIds.size() == 1 ? first : first + " (+" + (this.soundIds.size() - 1) + ")";
      return "Wait Sound: " + s + aP.init_Str_aP(this);
   }

   @Override
   public String getIcon() {
      return "SND";
   }

   @Override
   public boolean isEnabled() {
      return this.init;
   }

   @Override
   public void setEnabled(boolean e) {
      this.init = e;
   }

   @Override
   public boolean read_Z() {
      return this.waitForGuiBefore;
   }

   @Override
   public void load(boolean v) {
      this.waitForGuiBefore = v;
   }

   @Override
   public boolean write_Z() {
      return this.waitForGuiAfter;
   }

   @Override
   public void read(boolean v) {
      this.waitForGuiAfter = v;
   }

   @Override
   public String init_Str_7() {
      return this.waitGuiName;
   }

   @Override
   public void init_V_Str(String name) {
      this.waitGuiName = name;
   }

   @Override
   public CompoundTag toTag() {
      CompoundTag tag = new CompoundTag();
      tag.putString("type", this.getType().name());
      ListTag list = new ListTag();

      for (String id : this.soundIds) {
         list.add(StringTag.valueOf(id));
      }

      tag.put("soundIds", list);
      tag.putBoolean("waitForGuiBefore", this.waitForGuiBefore);
      tag.putBoolean("waitForGuiAfter", this.waitForGuiAfter);
      tag.putString("waitGuiName", this.waitGuiName);
      tag.putBoolean("checkDistance", this.checkDistance);
      tag.putDouble("maxDistance", this.maxDistance);
      tag.putBoolean("enabled", this.init);
      p.init(tag, this);
      tag.putString("saveAs", this.saveAs);
      return tag;
   }

   @Override
   public void fromTag(CompoundTag tag) {
      this.soundIds.clear();
      if (tag.contains("soundIds")) {
         for (Tag el : tag.getList("soundIds").orElse(new ListTag())) {
            String s = el.asString().orElse("");
            if (!s.isEmpty()) {
               this.soundIds.add(s);
            }
         }
      }

      this.waitForGuiBefore = aP.init_Z_CompoundTag_Z(tag, false);
      this.waitForGuiAfter = aP.load_Z_CompoundTag_Z(tag, false);
      this.waitGuiName = tag.getStringOr("waitGuiName", "");
      this.checkDistance = tag.getBooleanOr("checkDistance", false);
      this.maxDistance = tag.getDoubleOr("maxDistance", 16.0);
      if (tag.contains("enabled")) {
         this.init = tag.getBooleanOr("enabled", true);
      }

      p.load(tag, this);
      this.saveAs = tag.getStringOr("saveAs", "");
   }

   public void init(String soundId, double x, double y, double z) {
      this.matchedSoundId = soundId == null ? "" : soundId;
      this.matchedX = x;
      this.matchedY = y;
      this.matchedZ = z;
   }

   @Override
   public String load() {
      return this.saveAs;
   }

   @Override
   public void a_(String name) {
      this.saveAs = name == null ? "" : name;
   }
}

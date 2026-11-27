package autismclient.util.macro;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;

public final class MacroCondition {
   public MacroCondition.Inner_BufferHelper init_a_BufferHelper = MacroCondition.Inner_BufferHelper.GROUP;
   public boolean init_Z = false;
   public MacroCondition.Inner_StreamUtil init_a_StreamUtil = MacroCondition.Inner_StreamUtil.ALL;
   public List<MacroCondition> init_List = new ArrayList<>();
   public MacroCondition.Inner_CachePool init_a_CachePool = MacroCondition.Inner_CachePool.ALWAYS;
   public String init_Str = "";
   public String load_Str = "";
   public String read = "THIS_MACRO";
   public String write = "SUCCEEDED";
   public String update = "";
   public MacroCondition.Inner_ConfigStore init_a_ConfigStore = MacroCondition.Inner_ConfigStore.EQ;
   public MacroCondition.Inner_DataUtil init_a_DataUtil = MacroCondition.Inner_DataUtil.AT_LEAST;
   public int init_I = 0;
   public int load_I = 0;
   public double init_D = 5.0;
   public WaitDurabilityAction.Inner_CachePool init_ar_CachePool = WaitDurabilityAction.Inner_CachePool.HELD;
   public WaitDurabilityAction.Inner_StreamUtil init_ar_StreamUtil = WaitDurabilityAction.Inner_StreamUtil.PERCENT_REMAINING;
   public WaitFreeSlotsAction.Inner_StreamUtil init_aH_StreamUtil = WaitFreeSlotsAction.Inner_StreamUtil.FREE_SLOTS;

   public static MacroCondition init_a_a_StreamUtil(MacroCondition.Inner_StreamUtil c) {
      MacroCondition m = new MacroCondition();
      m.init_a_BufferHelper = MacroCondition.Inner_BufferHelper.GROUP;
      m.init_a_StreamUtil = c;
      return m;
   }

   public static MacroCondition init_a_a_CachePool(MacroCondition.Inner_CachePool k) {
      MacroCondition m = new MacroCondition();
      m.init_a_BufferHelper = MacroCondition.Inner_BufferHelper.LEAF;
      m.init_a_CachePool = k;
      return m;
   }

   public static MacroCondition createDefault() {
      MacroCondition root = init_a_a_StreamUtil(MacroCondition.Inner_StreamUtil.ALL);
      root.init_List.add(init_a_a_CachePool(MacroCondition.Inner_CachePool.ALWAYS));
      return root;
   }

   public MacroCondition load_a() {
      return fromTag(this.toTag());
   }

   public MacroCondition asLeaf() {
      if (this.init_a_BufferHelper == MacroCondition.Inner_BufferHelper.LEAF) {
         return this;
      } else {
         return !this.init_Z
               && this.init_a_BufferHelper == MacroCondition.Inner_BufferHelper.GROUP
               && this.init_List.size() == 1
               && this.init_List.get(0).init_a_BufferHelper == MacroCondition.Inner_BufferHelper.LEAF
            ? this.init_List.get(0)
            : null;
      }
   }

   public void writeFlat(CompoundTag t, String p) {
      MacroCondition leaf = this.asLeaf();
      if (leaf != null) {
         t.putString(p + "kind", leaf.init_a_CachePool.name());
         t.putBoolean(p + "negate", leaf.init_Z);
         t.putString(p + "item", leaf.init_Str == null ? "" : leaf.init_Str);
         t.putString(p + "op", leaf.init_a_ConfigStore.name());
         t.putString(p + "value", leaf.load_Str == null ? "" : leaf.load_Str);
         t.putString(p + "cmp", leaf.init_a_DataUtil.name());
         t.putInt(p + "amount", leaf.init_I);
         t.putInt(p + "slot", leaf.load_I);
         t.putString(p + "toolScope", leaf.read);
         t.putString(p + "toolState", leaf.write);
         t.putString(p + "toolFilter", leaf.update);
      }
   }

   public static MacroCondition init(CompoundTag tag, String nestedKey, String flatPrefix) {
      MacroCondition nested = tag.get(nestedKey) instanceof CompoundTag c ? fromTag(c) : null;
      boolean nestedSimple = nested == null || nested.asLeaf() != null;
      if (nestedSimple && tag.contains(flatPrefix + "kind")) {
         return readFlat(tag, flatPrefix);
      } else {
         return nested != null ? nested : createDefault();
      }
   }

   public static MacroCondition readFlat(CompoundTag t, String p) {
      MacroCondition leaf = init_a_a_CachePool(MacroCondition.Inner_CachePool.ALWAYS);
      leaf.init_a_CachePool = MacroNbtCodecHelper.parseEnum(MacroCondition.Inner_CachePool.class, t.getStringOr(p + "kind", "ALWAYS"), MacroCondition.Inner_CachePool.ALWAYS);
      leaf.init_Z = t.getBooleanOr(p + "negate", false);
      leaf.init_Str = t.getStringOr(p + "item", "");
      leaf.init_a_ConfigStore = MacroNbtCodecHelper.parseEnum(MacroCondition.Inner_ConfigStore.class, t.getStringOr(p + "op", "EQ"), MacroCondition.Inner_ConfigStore.EQ);
      leaf.load_Str = t.getStringOr(p + "value", "");
      leaf.init_a_DataUtil = MacroNbtCodecHelper.parseEnum(MacroCondition.Inner_DataUtil.class, t.getStringOr(p + "cmp", "AT_LEAST"), MacroCondition.Inner_DataUtil.AT_LEAST);
      leaf.init_I = t.getIntOr(p + "amount", 0);
      leaf.load_I = t.getIntOr(p + "slot", 0);
      leaf.read = t.getStringOr(p + "toolScope", "THIS_MACRO");
      leaf.write = t.getStringOr(p + "toolState", "SUCCEEDED");
      leaf.update = t.getStringOr(p + "toolFilter", "");
      MacroCondition root = init_a_a_StreamUtil(MacroCondition.Inner_StreamUtil.ALL);
      root.init_List.add(leaf);
      return root;
   }

   public boolean init_Z() {
      if (this.init_Z) {
         return false;
      }

      if (this.init_a_BufferHelper == MacroCondition.Inner_BufferHelper.LEAF) {
         return this.init_a_CachePool == MacroCondition.Inner_CachePool.ALWAYS;
      }

      if (this.init_List.isEmpty()) {
         return true;
      }

      for (MacroCondition c : this.init_List) {
         if (!c.init_Z()) {
            return false;
         }
      }

      return true;
   }

   public boolean evaluate(Minecraft mc) {
      boolean result = this.init_a_BufferHelper == MacroCondition.Inner_BufferHelper.GROUP ? this.load(mc) : this.read(mc);
      return this.init_Z != result;
   }

   private boolean load(Minecraft mc) {
      if (this.init_List.isEmpty()) {
         return true;
      }

      if (this.init_a_StreamUtil == MacroCondition.Inner_StreamUtil.ANY) {
         for (MacroCondition c : this.init_List) {
            if (c.evaluate(mc)) {
               return true;
            }
         }

         return false;
      } else {
         for (MacroCondition c : this.init_List) {
            if (!c.evaluate(mc)) {
               return false;
            }
         }

         return true;
      }
   }

   private boolean read(Minecraft mc) {
      if (this.init_a_CachePool == MacroCondition.Inner_CachePool.TOOLS) {
         return MacroToolBridge.init(MacroExecutor.getCurrentRunId(), this.read, this.update, this.write);
      }

      if (mc == null) {
         return false;
      }

      try {
         boolean var10000;
         switch (this.init_a_CachePool) {
            case ALWAYS:
               var10000 = true;
               break;
            case HELD_ITEM:
               var10000 = this.init_Z_Minecraft_DataUtil_DataUtil(mc, AssertAction.Inner_DataUtil.HELD_ITEM);
               break;
            case INVENTORY_ITEM:
               var10000 = this.init_Z_Minecraft_DataUtil_DataUtil(mc, AssertAction.Inner_DataUtil.INVENTORY_ITEM);
               break;
            case ITEM_COUNT:
               var10000 = this.init_Z_D_D(this.init_I_Minecraft(mc), this.init_I);
               break;
            case SLOT_EMPTY:
               var10000 = this.init_Z_Minecraft_aK_DataUtil(mc, WaitInventoryPredicateAction.Inner_DataUtil.SLOT_EMPTY);
               break;
            case SLOT_FILLED:
               var10000 = this.init_Z_Minecraft_aK_DataUtil(mc, WaitInventoryPredicateAction.Inner_DataUtil.SLOT_FILLED);
               break;
            case INVENTORY_FULL:
               var10000 = this.init_Z_Minecraft_aK_DataUtil(mc, WaitInventoryPredicateAction.Inner_DataUtil.INVENTORY_FULL);
               break;
            case INVENTORY_EMPTY:
               var10000 = this.init_Z_Minecraft_aK_DataUtil(mc, WaitInventoryPredicateAction.Inner_DataUtil.INVENTORY_EMPTY);
               break;
            case CURSOR_EMPTY:
               var10000 = this.init_Z_Minecraft_aK_DataUtil(mc, WaitInventoryPredicateAction.Inner_DataUtil.CURSOR_EMPTY);
               break;
            case CURSOR_FILLED:
               var10000 = this.init_Z_Minecraft_aK_DataUtil(mc, WaitInventoryPredicateAction.Inner_DataUtil.CURSOR_FILLED);
               break;
            case CURSOR_MATCHES:
               var10000 = this.init_Z_Minecraft_aK_DataUtil(mc, WaitInventoryPredicateAction.Inner_DataUtil.CURSOR_MATCHES);
               break;
            case SELECTED_SLOT:
               var10000 = this.init_Z_Minecraft_aK_DataUtil(mc, WaitInventoryPredicateAction.Inner_DataUtil.SELECTED_SLOT);
               break;
            case FREE_SLOTS:
               var10000 = this.write(mc);
               break;
            case DURABILITY:
               var10000 = this.update(mc);
               break;
            case GUI_OPEN:
               var10000 = this.init_Z_Minecraft_DataUtil_DataUtil(mc, AssertAction.Inner_DataUtil.GUI_TYPE);
               break;
            case LOOKING_AT_ENTITY:
               var10000 = this.init_Z_Minecraft_DataUtil_DataUtil(mc, AssertAction.Inner_DataUtil.LOOKING_AT_ENTITY);
               break;
            case LOOKING_AT_CONTAINER_ENTITY:
               var10000 = this.init_Z_Minecraft_DataUtil_DataUtil(mc, AssertAction.Inner_DataUtil.LOOKING_AT_CONTAINER_ENTITY);
               break;
            case MOUNTED_ENTITY:
               var10000 = this.init_Z_Minecraft_DataUtil_DataUtil(mc, AssertAction.Inner_DataUtil.MOUNTED_ENTITY);
               break;
            case ENTITY_NEARBY:
               var10000 = this.sync(mc);
               break;
            case LOOKING_AT_BLOCK:
               var10000 = this.init_Z_Minecraft_DataUtil_DataUtil(mc, AssertAction.Inner_DataUtil.LOOKING_AT_BLOCK);
               break;
            case ENTITY_TARGET:
               var10000 = mc.crosshairPickEntity != null;
               break;
            case VILLAGER_ITEM:
               var10000 = autismclient.util.villagerdupe.VillagerDupeDriver.init_Z_Minecraft(
                  mc, autismclient.util.villagerdupe.VillagerDupeDriver.Inner_DataUtil.init_DataUtil_DataUtil_Str(init_Str_Str_Minecraft(this.init_Str, mc)), true
               );
               break;
            case CONNECTION:
               var10000 = this.init_Z_Minecraft_DataUtil_DataUtil(mc, AssertAction.Inner_DataUtil.CONNECTION);
               break;
            case HAS_BUNDLE:
               var10000 = this.init_Z_Minecraft_DataUtil_DataUtil(mc, AssertAction.Inner_DataUtil.HAS_BUNDLE);
               break;
            case BUNDLE_V2_READY:
               var10000 = this.init_Z_Minecraft_DataUtil_DataUtil(mc, AssertAction.Inner_DataUtil.BUNDLE_V2_READY);
               break;
            case HAS_WRITABLE_BOOK:
               var10000 = this.init_Z_Minecraft_DataUtil_DataUtil(mc, AssertAction.Inner_DataUtil.HAS_WRITABLE_BOOK);
               break;
            case HEALTH:
               var10000 = mc.player != null && this.init_Z_D_D(mc.player.getHealth(), this.init_I);
               break;
            case VARIABLE:
               var10000 = this.flush(mc);
               break;
            case TOOLS:
               var10000 = false;
               break;
            default:
               throw new MatchException(null, null);
         }

         return var10000;
      } catch (Throwable ignored) {
         return false;
      }
   }

   private boolean init_Z_Minecraft_DataUtil_DataUtil(Minecraft mc, AssertAction.Inner_DataUtil type) {
      AssertAction a = new AssertAction();
      a.check = type;
      a.itemName = this.init_Str == null ? "" : this.init_Str;
      a.entityId = this.init_Str == null ? "" : this.init_Str;
      a.guiType = this.init_Str != null && !this.init_Str.isBlank() ? this.init_Str : "ANY";
      return a.init(mc);
   }

   private boolean init_Z_Minecraft_aK_DataUtil(Minecraft mc, WaitInventoryPredicateAction.Inner_DataUtil cond) {
      WaitInventoryPredicateAction w = new WaitInventoryPredicateAction();
      w.condition = cond;
      w.itemName = this.init_Str == null ? "" : this.init_Str;
      w.count = this.init_I;
      w.slot = this.load_I;
      return w.init(mc);
   }

   private int init_I_Minecraft(Minecraft mc) {
      MacroTemplateResolver.ResolveResult r = MacroVariableContext.resolveTemplate(this.init_Str == null ? "" : this.init_Str, mc);
      return !r.success() ? 0 : WaitInventoryPredicateAction.countItem(mc, MacroItemTarget.parse(r.value()));
   }

   private boolean write(Minecraft mc) {
      WaitFreeSlotsAction w = new WaitFreeSlotsAction();
      w.countMode = this.init_aH_StreamUtil;
      w.comparison = this.init_aH_DataUtil();
      w.slots = this.init_I;
      return w.init(mc);
   }

   private boolean update(Minecraft mc) {
      WaitDurabilityAction w = new WaitDurabilityAction();
      w.targetMode = this.init_ar_CachePool;
      w.itemName = this.init_Str == null ? "" : this.init_Str;
      w.slot = this.load_I;
      w.measurement = this.init_ar_StreamUtil;
      w.comparison = this.init_ar_DataUtil();
      w.value = this.init_I;
      return w.init(mc);
   }

   private boolean sync(Minecraft mc) {
      WaitEntityTargetAction w = new WaitEntityTargetAction();
      w.condition = WaitEntityTargetAction.Inner_DataUtil.NEARBY;
      w.entityId = this.init_Str == null ? "" : this.init_Str;
      w.range = this.init_D;
      return w.init(mc);
   }

   private boolean flush(Minecraft mc) {
      String a = init_Str_Str_Minecraft(this.init_Str, mc);
      if (this.init_a_ConfigStore == autismclient.util.macro.MacroCondition.Inner_ConfigStore.IS_EMPTY) {
         return a.isBlank();
      }

      if (this.init_a_ConfigStore == autismclient.util.macro.MacroCondition.Inner_ConfigStore.IS_TRUE) {
         return init_Z_Str(a);
      }

      String b = init_Str_Str_Minecraft(this.load_Str, mc);
      Double da = init_Double_Str(a);
      Double db = init_Double_Str(b);
      if (da != null && db != null) {
         double x = da;
         double y = db;
         boolean var10;
         switch (this.init_a_ConfigStore) {
            case EQ:
               var10 = x == y;
               break;
            case NEQ:
               var10 = x != y;
               break;
            case LT:
               var10 = x < y;
               break;
            case LE:
               var10 = x <= y;
               break;
            case GT:
               var10 = x > y;
               break;
            case GE:
               var10 = x >= y;
               break;
            case CONTAINS:
               var10 = a.contains(b);
               break;
            case STARTS_WITH:
               var10 = a.startsWith(b);
               break;
            case ENDS_WITH:
               var10 = a.endsWith(b);
               break;
            case REGEX:
               var10 = a.matches(b);
               break;
            default:
               var10 = false;
         }

         return var10;
      } else {
         boolean var10000;
         switch (this.init_a_ConfigStore) {
            case EQ:
               var10000 = a.equalsIgnoreCase(b);
               break;
            case NEQ:
               var10000 = !a.equalsIgnoreCase(b);
               break;
            case LT:
               var10000 = a.compareTo(b) < 0;
               break;
            case LE:
               var10000 = a.compareTo(b) <= 0;
               break;
            case GT:
               var10000 = a.compareTo(b) > 0;
               break;
            case GE:
               var10000 = a.compareTo(b) >= 0;
               break;
            case CONTAINS:
               var10000 = a.toLowerCase().contains(b.toLowerCase());
               break;
            case STARTS_WITH:
               var10000 = a.toLowerCase().startsWith(b.toLowerCase());
               break;
            case ENDS_WITH:
               var10000 = a.toLowerCase().endsWith(b.toLowerCase());
               break;
            case REGEX:
               var10000 = a.matches(b);
               break;
            default:
               var10000 = false;
         }

         return var10000;
      }
   }

   private boolean init_Z_D_D(double actual, double target) {
      boolean var10000;
      switch (this.init_a_DataUtil) {
         case BELOW:
            var10000 = actual < target;
            break;
         case AT_MOST:
            var10000 = actual <= target;
            break;
         case EXACT:
            var10000 = actual == target;
            break;
         case AT_LEAST:
            var10000 = actual >= target;
            break;
         case ABOVE:
            var10000 = actual > target;
            break;
         default:
            throw new MatchException(null, null);
      }

      return var10000;
   }

   private WaitDurabilityAction.Inner_DataUtil init_ar_DataUtil() {
      WaitDurabilityAction.Inner_DataUtil var10000;
      switch (this.init_a_DataUtil) {
         case BELOW:
            var10000 = WaitDurabilityAction.Inner_DataUtil.BELOW;
            break;
         case AT_MOST:
            var10000 = WaitDurabilityAction.Inner_DataUtil.AT_MOST;
            break;
         case EXACT:
            var10000 = WaitDurabilityAction.Inner_DataUtil.EXACT;
            break;
         case AT_LEAST:
            var10000 = WaitDurabilityAction.Inner_DataUtil.AT_LEAST;
            break;
         case ABOVE:
            var10000 = WaitDurabilityAction.Inner_DataUtil.ABOVE;
            break;
         default:
            throw new MatchException(null, null);
      }

      return var10000;
   }

   private WaitFreeSlotsAction.Inner_DataUtil init_aH_DataUtil() {
      WaitFreeSlotsAction.Inner_DataUtil var10000;
      switch (this.init_a_DataUtil) {
         case BELOW:
            var10000 = WaitFreeSlotsAction.Inner_DataUtil.BELOW;
            break;
         case AT_MOST:
            var10000 = WaitFreeSlotsAction.Inner_DataUtil.AT_MOST;
            break;
         case EXACT:
            var10000 = WaitFreeSlotsAction.Inner_DataUtil.EXACT;
            break;
         case AT_LEAST:
            var10000 = WaitFreeSlotsAction.Inner_DataUtil.AT_LEAST;
            break;
         case ABOVE:
            var10000 = WaitFreeSlotsAction.Inner_DataUtil.ABOVE;
            break;
         default:
            throw new MatchException(null, null);
      }

      return var10000;
   }

   private static String init_Str_Str_Minecraft(String template, Minecraft mc) {
      MacroTemplateResolver.ResolveResult r = MacroVariableContext.resolveTemplate(template == null ? "" : template, mc);
      return r.success() ? r.value() : "";
   }

   private static Double init_Double_Str(String s) {
      if (s != null && !s.isBlank()) {
         try {
            return Double.parseDouble(s.trim());
         } catch (NumberFormatException e) {
            return null;
         }
      } else {
         return null;
      }
   }

   private static boolean init_Z_Str(String s) {
      if (s == null) {
         return false;
      }

      String t = s.trim().toLowerCase(Locale.ROOT);
      return t.equals("true") || t.equals("1") || t.equals("yes") || t.equals("on");
   }

   public String describe() {
      String s = this.init_a_BufferHelper == MacroCondition.Inner_BufferHelper.GROUP ? this.load_Str() : this.read_Str();
      return this.init_Z ? "NOT(" + s + ")" : s;
   }

   private String load_Str() {
      if (this.init_List.isEmpty()) {
         return "always";
      }

      StringBuilder sb = new StringBuilder();
      String join = this.init_a_StreamUtil == MacroCondition.Inner_StreamUtil.ANY ? " OR " : " AND ";

      for (int i = 0; i < this.init_List.size(); i++) {
         if (i > 0) {
            sb.append(join);
         }

         sb.append(this.init_List.get(i).describe());
      }

      return this.init_List.size() > 1 ? "(" + sb + ")" : sb.toString();
   }

   private String read_Str() {
      String var10000;
      switch (this.init_a_CachePool) {
         case ALWAYS:
            var10000 = "always";
            break;
         case HELD_ITEM:
            var10000 = "held=" + this.write();
            break;
         case INVENTORY_ITEM:
            var10000 = "has " + this.write();
            break;
         case ITEM_COUNT:
            var10000 = this.write() + " " + this.sync() + " " + this.init_I;
            break;
         case SLOT_EMPTY:
         case SLOT_FILLED:
         case INVENTORY_FULL:
         case INVENTORY_EMPTY:
         case CURSOR_EMPTY:
         case CURSOR_FILLED:
         case CURSOR_MATCHES:
         case SELECTED_SLOT:
         case LOOKING_AT_ENTITY:
         case LOOKING_AT_CONTAINER_ENTITY:
         case MOUNTED_ENTITY:
         case LOOKING_AT_BLOCK:
         case ENTITY_TARGET:
         case CONNECTION:
         case HAS_BUNDLE:
         case BUNDLE_V2_READY:
         case HAS_WRITABLE_BOOK:
         default:
            var10000 = this.init_a_CachePool.name().toLowerCase(Locale.ROOT);
            break;
         case FREE_SLOTS:
            var10000 = (this.init_aH_StreamUtil == WaitFreeSlotsAction.Inner_StreamUtil.FILLED_SLOTS ? "filled" : "free") + " " + this.sync() + " " + this.init_I;
            break;
         case DURABILITY:
            var10000 = "dura " + this.sync() + " " + this.init_I;
            break;
         case GUI_OPEN:
            var10000 = "gui=" + (this.init_Str.isBlank() ? "any" : this.init_Str);
            break;
         case ENTITY_NEARBY:
            var10000 = "near " + (this.init_Str.isBlank() ? "entity" : this.init_Str);
            break;
         case VILLAGER_ITEM:
            var10000 = "villager holds " + (this.init_Str.isBlank() ? "any" : this.write());
            break;
         case HEALTH:
            var10000 = "hp " + this.sync() + " " + this.init_I;
            break;
         case VARIABLE:
            var10000 = this.update();
            break;
         case TOOLS:
            var10000 = "Tools: " + this.write.toLowerCase(Locale.ROOT);
      }

      return var10000;
   }

   private String write() {
      String s = this.init_Str == null ? "" : this.init_Str;
      int colon = s.indexOf(58);
      return colon >= 0 && colon + 1 < s.length() ? s.substring(colon + 1) : s;
   }

   private String update() {
      String left = this.init_Str == null ? "" : this.init_Str;
      String var10000;
      switch (this.init_a_ConfigStore) {
         case IS_EMPTY:
            var10000 = left + " empty";
            break;
         case IS_TRUE:
            var10000 = left + " true";
            break;
         default:
            var10000 = left + " " + this.flush() + " " + this.load_Str;
      }

      return var10000;
   }

   private String sync() {
      String var10000;
      switch (this.init_a_DataUtil) {
         case BELOW:
            var10000 = "<";
            break;
         case AT_MOST:
            var10000 = "<=";
            break;
         case EXACT:
            var10000 = "=";
            break;
         case AT_LEAST:
            var10000 = ">=";
            break;
         case ABOVE:
            var10000 = ">";
            break;
         default:
            throw new MatchException(null, null);
      }

      return var10000;
   }

   private String flush() {
      String var10000;
      switch (this.init_a_ConfigStore) {
         case EQ:
            var10000 = "=";
            break;
         case NEQ:
            var10000 = "!=";
            break;
         case LT:
            var10000 = "<";
            break;
         case LE:
            var10000 = "<=";
            break;
         case GT:
            var10000 = ">";
            break;
         case GE:
            var10000 = ">=";
            break;
         case CONTAINS:
            var10000 = "has";
            break;
         case STARTS_WITH:
            var10000 = "starts";
            break;
         case ENDS_WITH:
            var10000 = "ends";
            break;
         case REGEX:
            var10000 = "regex";
            break;
         case IS_EMPTY:
            var10000 = "empty";
            break;
         case IS_TRUE:
            var10000 = "true";
            break;
         default:
            throw new MatchException(null, null);
      }

      return var10000;
   }

   public CompoundTag toTag() {
      CompoundTag t = new CompoundTag();
      t.putString("node", this.init_a_BufferHelper.name());
      t.putBoolean("negate", this.init_Z);
      if (this.init_a_BufferHelper == MacroCondition.Inner_BufferHelper.GROUP) {
         t.putString("combine", this.init_a_StreamUtil.name());
         ListTag list = new ListTag();

         for (MacroCondition c : this.init_List) {
            if (c != null) {
               list.add(c.toTag());
            }
         }

         t.put("children", list);
      } else {
         t.putString("kind", this.init_a_CachePool.name());
         t.putString("item", this.init_Str == null ? "" : this.init_Str);
         t.putString("varB", this.load_Str == null ? "" : this.load_Str);
         t.putString("op", this.init_a_ConfigStore.name());
         t.putString("cmp", this.init_a_DataUtil.name());
         t.putInt("amount", this.init_I);
         t.putInt("slot", this.load_I);
         t.putDouble("range", this.init_D);
         t.putString("durMode", this.init_ar_CachePool.name());
         t.putString("durMeasure", this.init_ar_StreamUtil.name());
         t.putString("freeMode", this.init_aH_StreamUtil.name());
         t.putString("toolScope", this.read);
         t.putString("toolState", this.write);
         t.putString("toolFilter", this.update);
      }

      return t;
   }

   public static MacroCondition fromTag(CompoundTag t) {
      MacroCondition m = new MacroCondition();
      if (t == null) {
         return createDefault();
      }

      m.init_a_BufferHelper = MacroNbtCodecHelper.parseEnum(MacroCondition.Inner_BufferHelper.class, t.getStringOr("node", "GROUP"), MacroCondition.Inner_BufferHelper.GROUP);
      m.init_Z = t.getBooleanOr("negate", false);
      if (m.init_a_BufferHelper == MacroCondition.Inner_BufferHelper.GROUP) {
         m.init_a_StreamUtil = MacroNbtCodecHelper.parseEnum(MacroCondition.Inner_StreamUtil.class, t.getStringOr("combine", "ALL"), MacroCondition.Inner_StreamUtil.ALL);
         m.init_List = new ArrayList<>();
         Tag var3 = t.get("children");
         if (var3 instanceof ListTag) {
            for (Tag el : (ListTag)var3) {
               if (el instanceof CompoundTag ct) {
                  m.init_List.add(fromTag(ct));
               }
            }
         }
      } else {
         m.init_a_CachePool = MacroNbtCodecHelper.parseEnum(MacroCondition.Inner_CachePool.class, t.getStringOr("kind", "ALWAYS"), MacroCondition.Inner_CachePool.ALWAYS);
         m.init_Str = t.getStringOr("item", "");
         m.load_Str = t.getStringOr("varB", "");
         m.init_a_ConfigStore = MacroNbtCodecHelper.parseEnum(MacroCondition.Inner_ConfigStore.class, t.getStringOr("op", "EQ"), MacroCondition.Inner_ConfigStore.EQ);
         m.init_a_DataUtil = MacroNbtCodecHelper.parseEnum(MacroCondition.Inner_DataUtil.class, t.getStringOr("cmp", "AT_LEAST"), MacroCondition.Inner_DataUtil.AT_LEAST);
         m.init_I = t.getIntOr("amount", 0);
         m.load_I = t.getIntOr("slot", 0);
         m.read = t.getStringOr("toolScope", "THIS_MACRO");
         m.write = t.getStringOr("toolState", "SUCCEEDED");
         m.update = t.getStringOr("toolFilter", "");
         m.init_D = t.getDoubleOr("range", 5.0);
         m.init_ar_CachePool = MacroNbtCodecHelper.parseEnum(WaitDurabilityAction.Inner_CachePool.class, t.getStringOr("durMode", "HELD"), WaitDurabilityAction.Inner_CachePool.HELD);
         m.init_ar_StreamUtil = MacroNbtCodecHelper.parseEnum(WaitDurabilityAction.Inner_StreamUtil.class, t.getStringOr("durMeasure", "PERCENT_REMAINING"), WaitDurabilityAction.Inner_StreamUtil.PERCENT_REMAINING);
         m.init_aH_StreamUtil = MacroNbtCodecHelper.parseEnum(WaitFreeSlotsAction.Inner_StreamUtil.class, t.getStringOr("freeMode", "FREE_SLOTS"), WaitFreeSlotsAction.Inner_StreamUtil.FREE_SLOTS);
      }

      return m;
   }

   public enum Inner_BufferHelper {
      GROUP,
      LEAF;
   }

   public enum Inner_CachePool {
      ALWAYS,
      HELD_ITEM,
      INVENTORY_ITEM,
      ITEM_COUNT,
      SLOT_EMPTY,
      SLOT_FILLED,
      INVENTORY_FULL,
      INVENTORY_EMPTY,
      CURSOR_EMPTY,
      CURSOR_FILLED,
      CURSOR_MATCHES,
      SELECTED_SLOT,
      FREE_SLOTS,
      DURABILITY,
      GUI_OPEN,
      LOOKING_AT_ENTITY,
      LOOKING_AT_CONTAINER_ENTITY,
      MOUNTED_ENTITY,
      ENTITY_NEARBY,
      LOOKING_AT_BLOCK,
      ENTITY_TARGET,
      VILLAGER_ITEM,
      CONNECTION,
      HAS_BUNDLE,
      BUNDLE_V2_READY,
      HAS_WRITABLE_BOOK,
      HEALTH,
      VARIABLE,
      TOOLS;
   }

   public enum Inner_ConfigStore {
      EQ,
      NEQ,
      LT,
      LE,
      GT,
      GE,
      CONTAINS,
      STARTS_WITH,
      ENDS_WITH,
      REGEX,
      IS_EMPTY,
      IS_TRUE;
   }

   public enum Inner_DataUtil {
      BELOW,
      AT_MOST,
      EXACT,
      AT_LEAST,
      ABOVE;
   }

   public enum Inner_StreamUtil {
      ALL,
      ANY;
   }
}

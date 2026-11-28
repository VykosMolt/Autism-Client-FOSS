package autismclient.util.macro;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;

public final class MacroCondition {
   public MacroCondition.Inner_BufferHelper nodeType = MacroCondition.Inner_BufferHelper.GROUP;
   public boolean negate = false;
   public MacroCondition.Inner_StreamUtil combine = MacroCondition.Inner_StreamUtil.ALL;
   public List<MacroCondition> children = new ArrayList<>();
   public MacroCondition.Inner_CachePool kind = MacroCondition.Inner_CachePool.ALWAYS;
   public String item = "";
   public String value = "";
   public String toolScope = "THIS_MACRO";
   public String toolState = "SUCCEEDED";
   public String toolFilter = "";
   public MacroCondition.Inner_ConfigStore op = MacroCondition.Inner_ConfigStore.EQ;
   public MacroCondition.Inner_DataUtil cmp = MacroCondition.Inner_DataUtil.AT_LEAST;
   public int amount = 0;
   public int slot = 0;
   public double range = 5.0;
   public WaitDurabilityAction.TargetMode durMode = WaitDurabilityAction.TargetMode.HELD;
   public WaitDurabilityAction.Measurement durMeasure = WaitDurabilityAction.Measurement.PERCENT_REMAINING;
   public WaitFreeSlotsAction.Inner_StreamUtil freeMode = WaitFreeSlotsAction.Inner_StreamUtil.FREE_SLOTS;

   public static MacroCondition group(MacroCondition.Inner_StreamUtil c) {
      MacroCondition m = new MacroCondition();
      m.nodeType = MacroCondition.Inner_BufferHelper.GROUP;
      m.combine = c;
      return m;
   }

   public static MacroCondition leaf(MacroCondition.Inner_CachePool k) {
      MacroCondition m = new MacroCondition();
      m.nodeType = MacroCondition.Inner_BufferHelper.LEAF;
      m.kind = k;
      return m;
   }

   public static MacroCondition createDefault() {
      MacroCondition root = group(MacroCondition.Inner_StreamUtil.ALL);
      root.children.add(leaf(MacroCondition.Inner_CachePool.ALWAYS));
      return root;
   }

   public MacroCondition copy() {
      return fromTag(this.toTag());
   }

   public MacroCondition asLeaf() {
      if (this.nodeType == MacroCondition.Inner_BufferHelper.LEAF) {
         return this;
      } else {
         return !this.negate
               && this.nodeType == MacroCondition.Inner_BufferHelper.GROUP
               && this.children.size() == 1
               && this.children.get(0).nodeType == MacroCondition.Inner_BufferHelper.LEAF
            ? this.children.get(0)
            : null;
      }
   }

   public void writeFlat(CompoundTag t, String p) {
      MacroCondition leaf = this.asLeaf();
      if (leaf != null) {
         t.putString(p + "kind", leaf.kind.name());
         t.putBoolean(p + "negate", leaf.negate);
         t.putString(p + "item", leaf.item == null ? "" : leaf.item);
         t.putString(p + "op", leaf.op.name());
         t.putString(p + "value", leaf.value == null ? "" : leaf.value);
         t.putString(p + "cmp", leaf.cmp.name());
         t.putInt(p + "amount", leaf.amount);
         t.putInt(p + "slot", leaf.slot);
         t.putString(p + "toolScope", leaf.toolScope);
         t.putString(p + "toolState", leaf.toolState);
         t.putString(p + "toolFilter", leaf.toolFilter);
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
      MacroCondition leaf = leaf(MacroCondition.Inner_CachePool.ALWAYS);
      leaf.kind = MacroNbtCodecHelper.parseEnum(MacroCondition.Inner_CachePool.class, t.getStringOr(p + "kind", "ALWAYS"), MacroCondition.Inner_CachePool.ALWAYS);
      leaf.negate = t.getBooleanOr(p + "negate", false);
      leaf.item = t.getStringOr(p + "item", "");
      leaf.op = MacroNbtCodecHelper.parseEnum(MacroCondition.Inner_ConfigStore.class, t.getStringOr(p + "op", "EQ"), MacroCondition.Inner_ConfigStore.EQ);
      leaf.value = t.getStringOr(p + "value", "");
      leaf.cmp = MacroNbtCodecHelper.parseEnum(MacroCondition.Inner_DataUtil.class, t.getStringOr(p + "cmp", "AT_LEAST"), MacroCondition.Inner_DataUtil.AT_LEAST);
      leaf.amount = t.getIntOr(p + "amount", 0);
      leaf.slot = t.getIntOr(p + "slot", 0);
      leaf.toolScope = t.getStringOr(p + "toolScope", "THIS_MACRO");
      leaf.toolState = t.getStringOr(p + "toolState", "SUCCEEDED");
      leaf.toolFilter = t.getStringOr(p + "toolFilter", "");
      MacroCondition root = group(MacroCondition.Inner_StreamUtil.ALL);
      root.children.add(leaf);
      return root;
   }

   public boolean isTriviallyTrue() {
      if (this.negate) {
         return false;
      }

      if (this.nodeType == MacroCondition.Inner_BufferHelper.LEAF) {
         return this.kind == MacroCondition.Inner_CachePool.ALWAYS;
      }

      if (this.children.isEmpty()) {
         return true;
      }

      for (MacroCondition c : this.children) {
         if (!c.isTriviallyTrue()) {
            return false;
         }
      }

      return true;
   }

   public boolean evaluate(Minecraft mc) {
      boolean result = this.nodeType == MacroCondition.Inner_BufferHelper.GROUP ? this.evaluateGroup(mc) : this.evaluateLeaf(mc);
      return this.negate != result;
   }

   private boolean evaluateGroup(Minecraft mc) {
      if (this.children.isEmpty()) {
         return true;
      }

      if (this.combine == MacroCondition.Inner_StreamUtil.ANY) {
         for (MacroCondition c : this.children) {
            if (c.evaluate(mc)) {
               return true;
            }
         }

         return false;
      } else {
         for (MacroCondition c : this.children) {
            if (!c.evaluate(mc)) {
               return false;
            }
         }

         return true;
      }
   }

   private boolean evaluateLeaf(Minecraft mc) {
      if (this.kind == MacroCondition.Inner_CachePool.TOOLS) {
         return MacroToolBridge.init(MacroExecutor.getCurrentRunId(), this.toolScope, this.toolFilter, this.toolState);
      }

      if (mc == null) {
         return false;
      }

      try {
         boolean var10000;
         switch (this.kind) {
            case ALWAYS:
               var10000 = true;
               break;
            case HELD_ITEM:
               var10000 = this.checkAssert(mc, AssertAction.CheckKind.HELD_ITEM);
               break;
            case INVENTORY_ITEM:
               var10000 = this.checkAssert(mc, AssertAction.CheckKind.INVENTORY_ITEM);
               break;
            case ITEM_COUNT:
               var10000 = this.compare(this.countItem(mc), this.amount);
               break;
            case SLOT_EMPTY:
               var10000 = this.checkInventoryPredicate(mc, WaitInventoryPredicateAction.InventoryCondition.SLOT_EMPTY);
               break;
            case SLOT_FILLED:
               var10000 = this.checkInventoryPredicate(mc, WaitInventoryPredicateAction.InventoryCondition.SLOT_FILLED);
               break;
            case INVENTORY_FULL:
               var10000 = this.checkInventoryPredicate(mc, WaitInventoryPredicateAction.InventoryCondition.INVENTORY_FULL);
               break;
            case INVENTORY_EMPTY:
               var10000 = this.checkInventoryPredicate(mc, WaitInventoryPredicateAction.InventoryCondition.INVENTORY_EMPTY);
               break;
            case CURSOR_EMPTY:
               var10000 = this.checkInventoryPredicate(mc, WaitInventoryPredicateAction.InventoryCondition.CURSOR_EMPTY);
               break;
            case CURSOR_FILLED:
               var10000 = this.checkInventoryPredicate(mc, WaitInventoryPredicateAction.InventoryCondition.CURSOR_FILLED);
               break;
            case CURSOR_MATCHES:
               var10000 = this.checkInventoryPredicate(mc, WaitInventoryPredicateAction.InventoryCondition.CURSOR_MATCHES);
               break;
            case SELECTED_SLOT:
               var10000 = this.checkInventoryPredicate(mc, WaitInventoryPredicateAction.InventoryCondition.SELECTED_SLOT);
               break;
            case FREE_SLOTS:
               var10000 = this.checkFreeSlots(mc);
               break;
            case DURABILITY:
               var10000 = this.checkDurability(mc);
               break;
            case GUI_OPEN:
               var10000 = this.checkAssert(mc, AssertAction.CheckKind.GUI_TYPE);
               break;
            case LOOKING_AT_ENTITY:
               var10000 = this.checkAssert(mc, AssertAction.CheckKind.LOOKING_AT_ENTITY);
               break;
            case LOOKING_AT_CONTAINER_ENTITY:
               var10000 = this.checkAssert(mc, AssertAction.CheckKind.LOOKING_AT_CONTAINER_ENTITY);
               break;
            case MOUNTED_ENTITY:
               var10000 = this.checkAssert(mc, AssertAction.CheckKind.MOUNTED_ENTITY);
               break;
            case ENTITY_NEARBY:
               var10000 = this.checkEntityNearby(mc);
               break;
            case LOOKING_AT_BLOCK:
               var10000 = this.checkAssert(mc, AssertAction.CheckKind.LOOKING_AT_BLOCK);
               break;
            case ENTITY_TARGET:
               var10000 = mc.crosshairPickEntity != null;
               break;
            case VILLAGER_ITEM:
               var10000 = autismclient.util.villagerdupe.VillagerDupeDriver.init_Z_Minecraft(
                  mc, autismclient.util.villagerdupe.VillagerDupeDriver.Inner_DataUtil.parse(resolveTemplate(this.item, mc)), true
               );
               break;
            case CONNECTION:
               var10000 = this.checkAssert(mc, AssertAction.CheckKind.CONNECTION);
               break;
            case HAS_BUNDLE:
               var10000 = this.checkAssert(mc, AssertAction.CheckKind.HAS_BUNDLE);
               break;
            case BUNDLE_V2_READY:
               var10000 = this.checkAssert(mc, AssertAction.CheckKind.BUNDLE_V2_READY);
               break;
            case HAS_WRITABLE_BOOK:
               var10000 = this.checkAssert(mc, AssertAction.CheckKind.HAS_WRITABLE_BOOK);
               break;
            case HEALTH:
               var10000 = mc.player != null && this.compare(mc.player.getHealth(), this.amount);
               break;
            case VARIABLE:
               var10000 = this.checkVariable(mc);
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

   private boolean checkAssert(Minecraft mc, AssertAction.CheckKind type) {
      AssertAction a = new AssertAction();
      a.check = type;
      a.itemName = this.item == null ? "" : this.item;
      a.entityId = this.item == null ? "" : this.item;
      a.guiType = this.item != null && !this.item.isBlank() ? this.item : "ANY";
      return a.holds(mc);
   }

   private boolean checkInventoryPredicate(Minecraft mc, WaitInventoryPredicateAction.InventoryCondition cond) {
      WaitInventoryPredicateAction w = new WaitInventoryPredicateAction();
      w.condition = cond;
      w.itemName = this.item == null ? "" : this.item;
      w.count = this.amount;
      w.slot = this.slot;
      return w.isSatisfied(mc);
   }

   private int countItem(Minecraft mc) {
      MacroTemplateResolver.ResolveResult r = MacroVariableContext.resolveTemplate(this.item == null ? "" : this.item, mc);
      return !r.success() ? 0 : WaitInventoryPredicateAction.countItem(mc, MacroItemTarget.parse(r.value()));
   }

   private boolean checkFreeSlots(Minecraft mc) {
      WaitFreeSlotsAction w = new WaitFreeSlotsAction();
      w.countMode = this.freeMode;
      w.comparison = this.freeSlotsComparison();
      w.slots = this.amount;
      return w.init(mc);
   }

   private boolean checkDurability(Minecraft mc) {
      WaitDurabilityAction w = new WaitDurabilityAction();
      w.targetMode = this.durMode;
      w.itemName = this.item == null ? "" : this.item;
      w.slot = this.slot;
      w.measurement = this.durMeasure;
      w.comparison = this.durabilityComparison();
      w.value = this.amount;
      return w.isSatisfied(mc);
   }

   private boolean checkEntityNearby(Minecraft mc) {
      WaitEntityTargetAction w = new WaitEntityTargetAction();
      w.condition = WaitEntityTargetAction.TargetCondition.NEARBY;
      w.entityId = this.item == null ? "" : this.item;
      w.range = this.range;
      return w.isSatisfied(mc);
   }

   private boolean checkVariable(Minecraft mc) {
      String a = resolveTemplate(this.item, mc);
      if (this.op == autismclient.util.macro.MacroCondition.Inner_ConfigStore.IS_EMPTY) {
         return a.isBlank();
      }

      if (this.op == autismclient.util.macro.MacroCondition.Inner_ConfigStore.IS_TRUE) {
         return isTruthy(a);
      }

      String b = resolveTemplate(this.value, mc);
      Double da = parseDouble(a);
      Double db = parseDouble(b);
      if (da != null && db != null) {
         double x = da;
         double y = db;
         boolean var10;
         switch (this.op) {
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
         switch (this.op) {
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

   private boolean compare(double actual, double target) {
      boolean var10000;
      switch (this.cmp) {
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

   private WaitDurabilityAction.Comparison durabilityComparison() {
      WaitDurabilityAction.Comparison var10000;
      switch (this.cmp) {
         case BELOW:
            var10000 = WaitDurabilityAction.Comparison.BELOW;
            break;
         case AT_MOST:
            var10000 = WaitDurabilityAction.Comparison.AT_MOST;
            break;
         case EXACT:
            var10000 = WaitDurabilityAction.Comparison.EXACT;
            break;
         case AT_LEAST:
            var10000 = WaitDurabilityAction.Comparison.AT_LEAST;
            break;
         case ABOVE:
            var10000 = WaitDurabilityAction.Comparison.ABOVE;
            break;
         default:
            throw new MatchException(null, null);
      }

      return var10000;
   }

   private WaitFreeSlotsAction.Inner_DataUtil freeSlotsComparison() {
      WaitFreeSlotsAction.Inner_DataUtil var10000;
      switch (this.cmp) {
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

   private static String resolveTemplate(String template, Minecraft mc) {
      MacroTemplateResolver.ResolveResult r = MacroVariableContext.resolveTemplate(template == null ? "" : template, mc);
      return r.success() ? r.value() : "";
   }

   private static Double parseDouble(String s) {
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

   private static boolean isTruthy(String s) {
      if (s == null) {
         return false;
      }

      String t = s.trim().toLowerCase(Locale.ROOT);
      return t.equals("true") || t.equals("1") || t.equals("yes") || t.equals("on");
   }

   public String describe() {
      String s = this.nodeType == MacroCondition.Inner_BufferHelper.GROUP ? this.describeGroup() : this.describeLeaf();
      return this.negate ? "NOT(" + s + ")" : s;
   }

   private String describeGroup() {
      if (this.children.isEmpty()) {
         return "always";
      }

      StringBuilder sb = new StringBuilder();
      String join = this.combine == MacroCondition.Inner_StreamUtil.ANY ? " OR " : " AND ";

      for (int i = 0; i < this.children.size(); i++) {
         if (i > 0) {
            sb.append(join);
         }

         sb.append(this.children.get(i).describe());
      }

      return this.children.size() > 1 ? "(" + sb + ")" : sb.toString();
   }

   private String describeLeaf() {
      String var10000;
      switch (this.kind) {
         case ALWAYS:
            var10000 = "always";
            break;
         case HELD_ITEM:
            var10000 = "held=" + this.itemLabel();
            break;
         case INVENTORY_ITEM:
            var10000 = "has " + this.itemLabel();
            break;
         case ITEM_COUNT:
            var10000 = this.itemLabel() + " " + this.cmpSymbol() + " " + this.amount;
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
            var10000 = this.kind.name().toLowerCase(Locale.ROOT);
            break;
         case FREE_SLOTS:
            var10000 = (this.freeMode == WaitFreeSlotsAction.Inner_StreamUtil.FILLED_SLOTS ? "filled" : "free") + " " + this.cmpSymbol() + " " + this.amount;
            break;
         case DURABILITY:
            var10000 = "dura " + this.cmpSymbol() + " " + this.amount;
            break;
         case GUI_OPEN:
            var10000 = "gui=" + (this.item.isBlank() ? "any" : this.item);
            break;
         case ENTITY_NEARBY:
            var10000 = "near " + (this.item.isBlank() ? "entity" : this.item);
            break;
         case VILLAGER_ITEM:
            var10000 = "villager holds " + (this.item.isBlank() ? "any" : this.itemLabel());
            break;
         case HEALTH:
            var10000 = "hp " + this.cmpSymbol() + " " + this.amount;
            break;
         case VARIABLE:
            var10000 = this.describeVariable();
            break;
         case TOOLS:
            var10000 = "Tools: " + this.toolState.toLowerCase(Locale.ROOT);
      }

      return var10000;
   }

   private String itemLabel() {
      String s = this.item == null ? "" : this.item;
      int colon = s.indexOf(58);
      return colon >= 0 && colon + 1 < s.length() ? s.substring(colon + 1) : s;
   }

   private String describeVariable() {
      String left = this.item == null ? "" : this.item;
      String var10000;
      switch (this.op) {
         case IS_EMPTY:
            var10000 = left + " empty";
            break;
         case IS_TRUE:
            var10000 = left + " true";
            break;
         default:
            var10000 = left + " " + this.opSymbol() + " " + this.value;
      }

      return var10000;
   }

   private String cmpSymbol() {
      String var10000;
      switch (this.cmp) {
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

   private String opSymbol() {
      String var10000;
      switch (this.op) {
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
      t.putString("node", this.nodeType.name());
      t.putBoolean("negate", this.negate);
      if (this.nodeType == MacroCondition.Inner_BufferHelper.GROUP) {
         t.putString("combine", this.combine.name());
         ListTag list = new ListTag();

         for (MacroCondition c : this.children) {
            if (c != null) {
               list.add(c.toTag());
            }
         }

         t.put("children", list);
      } else {
         t.putString("kind", this.kind.name());
         t.putString("item", this.item == null ? "" : this.item);
         t.putString("varB", this.value == null ? "" : this.value);
         t.putString("op", this.op.name());
         t.putString("cmp", this.cmp.name());
         t.putInt("amount", this.amount);
         t.putInt("slot", this.slot);
         t.putDouble("range", this.range);
         t.putString("durMode", this.durMode.name());
         t.putString("durMeasure", this.durMeasure.name());
         t.putString("freeMode", this.freeMode.name());
         t.putString("toolScope", this.toolScope);
         t.putString("toolState", this.toolState);
         t.putString("toolFilter", this.toolFilter);
      }

      return t;
   }

   public static MacroCondition fromTag(CompoundTag t) {
      MacroCondition m = new MacroCondition();
      if (t == null) {
         return createDefault();
      }

      m.nodeType = MacroNbtCodecHelper.parseEnum(MacroCondition.Inner_BufferHelper.class, t.getStringOr("node", "GROUP"), MacroCondition.Inner_BufferHelper.GROUP);
      m.negate = t.getBooleanOr("negate", false);
      if (m.nodeType == MacroCondition.Inner_BufferHelper.GROUP) {
         m.combine = MacroNbtCodecHelper.parseEnum(MacroCondition.Inner_StreamUtil.class, t.getStringOr("combine", "ALL"), MacroCondition.Inner_StreamUtil.ALL);
         m.children = new ArrayList<>();
         Tag var3 = t.get("children");
         if (var3 instanceof ListTag) {
            for (Tag el : (ListTag)var3) {
               if (el instanceof CompoundTag ct) {
                  m.children.add(fromTag(ct));
               }
            }
         }
      } else {
         m.kind = MacroNbtCodecHelper.parseEnum(MacroCondition.Inner_CachePool.class, t.getStringOr("kind", "ALWAYS"), MacroCondition.Inner_CachePool.ALWAYS);
         m.item = t.getStringOr("item", "");
         m.value = t.getStringOr("varB", "");
         m.op = MacroNbtCodecHelper.parseEnum(MacroCondition.Inner_ConfigStore.class, t.getStringOr("op", "EQ"), MacroCondition.Inner_ConfigStore.EQ);
         m.cmp = MacroNbtCodecHelper.parseEnum(MacroCondition.Inner_DataUtil.class, t.getStringOr("cmp", "AT_LEAST"), MacroCondition.Inner_DataUtil.AT_LEAST);
         m.amount = t.getIntOr("amount", 0);
         m.slot = t.getIntOr("slot", 0);
         m.toolScope = t.getStringOr("toolScope", "THIS_MACRO");
         m.toolState = t.getStringOr("toolState", "SUCCEEDED");
         m.toolFilter = t.getStringOr("toolFilter", "");
         m.range = t.getDoubleOr("range", 5.0);
         m.durMode = MacroNbtCodecHelper.parseEnum(WaitDurabilityAction.TargetMode.class, t.getStringOr("durMode", "HELD"), WaitDurabilityAction.TargetMode.HELD);
         m.durMeasure = MacroNbtCodecHelper.parseEnum(WaitDurabilityAction.Measurement.class, t.getStringOr("durMeasure", "PERCENT_REMAINING"), WaitDurabilityAction.Measurement.PERCENT_REMAINING);
         m.freeMode = MacroNbtCodecHelper.parseEnum(WaitFreeSlotsAction.Inner_StreamUtil.class, t.getStringOr("freeMode", "FREE_SLOTS"), WaitFreeSlotsAction.Inner_StreamUtil.FREE_SLOTS);
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

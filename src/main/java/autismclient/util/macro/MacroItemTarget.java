package autismclient.util.macro;

import autismclient.util.RegistryDisplayNameCache;
import java.text.Normalizer;
import java.text.Normalizer.Form;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import net.minecraft.client.Minecraft;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;

public final class MacroItemTarget {
   public static final String init_Str = "slot";
   public static final String load = "registryId";
   public static final String read = "display";
   public static final String write = "textJson";
   public static final String update = "manual";
   public static final String sync = "template";
   public int init_I = -1;
   public String flush = "";
   public String close = "";
   public String open = "";
   public boolean init_Z = false;
   public String poll = "";

   public MacroItemTarget init_ArrayUtil() {
      MacroItemTarget copy = new MacroItemTarget();
      copy.init_I = this.init_I;
      copy.flush = this.flush;
      copy.close = this.close;
      copy.open = this.open;
      copy.init_Z = this.init_Z;
      copy.poll = this.poll;
      return copy;
   }

   public boolean init_Z() {
      return this.init_I >= 0;
   }

   public boolean load_Z() {
      return this.flush != null && !this.flush.isBlank();
   }

   public boolean read_Z() {
      return this.close != null && !this.close.isBlank();
   }

   public boolean write_Z() {
      return this.open != null && !this.open.isBlank();
   }

   public boolean update() {
      return this.load_Z() || this.read_Z() || this.write_Z();
   }

   public Component init_Component() {
      Component rich = MacroExecutor.init_Component_Str(this.open);
      return rich != null ? rich.copy() : Component.literal(this.init_Str());
   }

   public Component init_Component_Str(String editedValue) {
      String safeValue = editedValue == null ? "" : editedValue;
      Component rich = MacroExecutor.init_Component_Str(this.open);
      return rich == null ? Component.literal(safeValue) : init_Component_Component_Str(rich, safeValue);
   }

   public MacroItemTarget init_ArrayUtil_Str(String editedValue) {
      MacroItemTarget target = this.init_ArrayUtil();
      String safeValue = load_Str_Str(editedValue);
      target.close = safeValue;
      target.init_Z = true;
      target.open = "";
      target.poll = safeValue.contains("{") ? safeValue : "";
      if (safeValue.isEmpty()) {
         target.flush = "";
         return target;
      } else {
         return target;
      }
   }

   public MacroItemTarget load_ArrayUtil_Str(String editedValue) {
      String safeValue = editedValue == null ? "" : editedValue;
      MacroItemTarget target = this.init_ArrayUtil();
      if (safeValue.isEmpty()) {
         target.close = "";
         target.open = "";
         target.flush = "";
         target.init_Z = true;
         target.poll = "";
         return target;
      } else {
         Component styled = this.init_Component_Str(safeValue);
         target.close = safeValue;
         target.open = styled == null ? "" : MacroExecutor.init_Str_Component(styled);
         target.init_Z = false;
         target.poll = safeValue.contains("{") ? safeValue : "";
         return target;
      }
   }

   public String init_Str() {
      if (this.read_Z()) {
         return this.close;
      } else {
         return this.load_Z() ? this.flush : "";
      }
   }

   public String load_Str() {
      String name = this.read_Str();
      if (this.init_Z() && !name.isBlank()) {
         return "Slot " + this.init_I + ": " + name;
      } else {
         return this.init_Z() ? "Slot " + this.init_I : name;
      }
   }

   public String read_Str() {
      if (this.write_Z()) {
         Component rich = MacroExecutor.init_Component_Str(this.open);
         if (rich != null && !rich.getString().isBlank()) {
            return rich.getString();
         }
      }

      if (this.read_Z()) {
         if (!this.load_Z()) {
            return this.close;
         }

         String registryLabel = RegistryDisplayNameCache.init(this.flush);
         return !RegistryDisplayNameCache.init(this.close, this.flush) && !this.close.equalsIgnoreCase(registryLabel) ? this.close : registryLabel;
      } else {
         return this.load_Z() ? RegistryDisplayNameCache.init(this.flush) : "";
      }
   }

   public Component load_Component() {
      if (this.write_Z()) {
         Component rich = MacroExecutor.init_Component_Str(this.open);
         if (rich != null) {
            return rich.copy();
         }
      }

      return Component.literal(this.read_Str());
   }

   public CompoundTag init_CompoundTag() {
      CompoundTag tag = new CompoundTag();
      if (this.init_Z()) {
         tag.putInt("slot", this.init_I);
      }

      if (this.load_Z()) {
         tag.putString("registryId", this.flush);
      }

      if (this.read_Z()) {
         tag.putString("display", this.close);
      }

      if (this.write_Z()) {
         tag.putString("textJson", this.open);
      }

      if (this.poll != null && !this.poll.isBlank()) {
         tag.putString("template", this.poll);
      }

      tag.putBoolean("manual", this.init_Z);
      return tag;
   }

   public static MacroItemTarget init_ArrayUtil_CompoundTag(CompoundTag tag) {
      MacroItemTarget target = new MacroItemTarget();
      if (tag == null) {
         return target;
      }

      if (tag.contains("slot")) {
         target.init_I = tag.getIntOr("slot", -1);
      }

      if (tag.contains("registryId")) {
         target.flush = tag.getStringOr("registryId", "");
      }

      if (tag.contains("display")) {
         target.close = tag.getStringOr("display", "");
      }

      if (tag.contains("textJson")) {
         target.open = tag.getStringOr("textJson", "");
      }

      if (tag.contains("manual")) {
         target.init_Z = tag.getBooleanOr("manual", false);
      }

      target.poll = tag.getStringOr("template", "");
      if (target.init_Z) {
         target.close = load_Str_Str(target.close);
         target.open = "";
      } else if (!target.write_Z() && target.read_Z()) {
         target.close = load_Str_Str(target.close);
      }

      return target;
   }

   public static MacroItemTarget init_ArrayUtil_Tag(Tag element) {
      return element instanceof CompoundTag compound ? init_ArrayUtil_CompoundTag(compound) : update(element == null ? "" : element.asString().orElse(""));
   }

   public static List<MacroItemTarget> init_List_ListTag(ListTag list) {
      List<MacroItemTarget> targets = new ArrayList<>();
      if (list == null) {
         return targets;
      }

      for (Tag element : list) {
         MacroItemTarget target = init_ArrayUtil_Tag(element);
         if (target.init_Z() || target.update()) {
            targets.add(target);
         }
      }

      return targets;
   }

   public static ListTag init_ListTag_List(List<MacroItemTarget> targets) {
      ListTag list = new ListTag();
      if (targets == null) {
         return list;
      }

      for (MacroItemTarget target : targets) {
         if (target != null && (target.init_Z() || target.update())) {
            list.add(target.init_CompoundTag());
         }
      }

      return list;
   }

   public static List<MacroItemTarget> init_List_List(List<MacroItemTarget> targets) {
      List<MacroItemTarget> copies = new ArrayList<>();
      if (targets == null) {
         return copies;
      }

      for (MacroItemTarget target : targets) {
         if (target != null) {
            copies.add(target.init_ArrayUtil());
         }
      }

      return copies;
   }

   public static MacroItemTarget read(String text) {
      MacroItemTarget target = new MacroItemTarget();
      target.close = load_Str_Str(text);
      target.init_Z = true;
      if (text != null && text.contains("{")) {
         target.poll = text;
      }

      return target;
   }

   public static MacroItemTarget write(String registryId) {
      MacroItemTarget target = new MacroItemTarget();
      target.flush = registryId == null ? "" : registryId.trim();
      target.close = target.flush;
      target.init_Z = false;
      return target;
   }

   public static MacroItemTarget init_ArrayUtil_I(int slot) {
      MacroItemTarget target = new MacroItemTarget();
      target.init_I = slot;
      return target;
   }

   public static MacroItemTarget init_ArrayUtil_ItemStack_I(ItemStack stack, int visibleSlot) {
      MacroItemTarget target = new MacroItemTarget();
      target.init_I = visibleSlot;
      if (stack != null && !stack.isEmpty()) {
         Identifier id = BuiltInRegistries.ITEM.getKey(stack.getItem());
         target.flush = id == null ? "" : id.toString();
         target.close = stack.getHoverName().getString();
         target.init_Z = false;
         if (init_Z_ItemStack(stack)) {
            target.open = MacroExecutor.init_Str_Component(stack.getHoverName());
         }

         return target;
      } else {
         return target;
      }
   }

   public static MacroItemTarget update(String raw) {
      if (raw == null) {
         return new MacroItemTarget();
      }

      String trimmed = raw.trim();
      if (trimmed.isEmpty()) {
         return new MacroItemTarget();
      }

      MacroItemTarget target = new MacroItemTarget();
      String body = trimmed;
      if (trimmed.startsWith("#")) {
         int separator = trimmed.indexOf(124);
         String slotPart = separator >= 0 ? trimmed.substring(1, separator).trim() : trimmed.substring(1).trim();

         try {
            int parsedSlot = Integer.parseInt(slotPart);
            if (parsedSlot >= 0) {
               target.init_I = parsedSlot;
            }
         } catch (NumberFormatException ignored) {
            return sync(trimmed);
         }

         body = separator >= 0 && separator + 1 < trimmed.length() ? trimmed.substring(separator + 1).trim() : "";
      }

      if (!body.isBlank()) {
         MacroItemTarget bodyTarget = sync(body);
         target.flush = bodyTarget.flush;
         target.close = bodyTarget.close;
         target.open = bodyTarget.open;
         target.init_Z = bodyTarget.init_Z;
      }

      return target;
   }

   public String write_Str() {
      String body = this.init_Str();
      if (this.init_Z() && body.isBlank()) {
         return "#" + this.init_I;
      } else {
         return this.init_Z() ? "#" + this.init_I + "|" + body : body;
      }
   }

   public int init_I_ItemStack_I(ItemStack stack, int visibleSlot) {
      if (stack == null || stack.isEmpty()) {
         return -1;
      }

      if (this.init_Z() && this.init_I != visibleSlot) {
         return -1;
      }

      if (this.write_Z()) {
         String stackJson = MacroExecutor.init_Str_Component(stack.getHoverName());
         if (this.open.equals(stackJson)) {
            return 400;
         } else {
            return this.read_Z() && this.close.equals(stack.getHoverName().getString()) ? 350 : -1;
         }
      } else if (this.load_Z()) {
         Identifier id = BuiltInRegistries.ITEM.getKey(stack.getItem());
         if (id == null) {
            return -1;
         }

         String fullId = id.toString();
         String rawPath = id.getPath();
         String spacedPath = rawPath.replace('_', ' ');
         if (this.flush.equalsIgnoreCase(fullId)) {
            return 320;
         }

         if (this.flush.equalsIgnoreCase(rawPath)) {
            return 315;
         }

         if (this.flush.equalsIgnoreCase(spacedPath)) {
            return 310;
         }

         if (this.read_Z()) {
            String normalizedDisplay = init_Str_Str(this.close);
            if (normalizedDisplay.equals(init_Str_Str(fullId))) {
               return 305;
            }

            if (normalizedDisplay.equals(init_Str_Str(rawPath))) {
               return 304;
            }

            if (normalizedDisplay.equals(init_Str_Str(spacedPath))) {
               return 303;
            }
         }

         return -1;
      } else if (this.read_Z()) {
         String normalizedTarget = init_Str_Str(this.close);
         if (normalizedTarget.isBlank()) {
            return this.init_Z() ? 100 : -1;
         }

         String normalizedDisplay = init_Str_Str(stack.getHoverName().getString());
         if (normalizedTarget.equals(normalizedDisplay)) {
            return this.init_Z ? 220 : 240;
         }

         Identifier id = BuiltInRegistries.ITEM.getKey(stack.getItem());
         if (id != null) {
            String fullId = init_Str_Str(id.toString());
            String rawPath = init_Str_Str(id.getPath());
            String spacedPath = init_Str_Str(id.getPath().replace('_', ' '));
            if (normalizedTarget.equals(fullId)) {
               return this.init_Z ? 210 : 230;
            }

            if (normalizedTarget.equals(rawPath)) {
               return this.init_Z ? 205 : 225;
            }

            if (normalizedTarget.equals(spacedPath)) {
               return this.init_Z ? 200 : 220;
            }
         }

         return -1;
      } else {
         return this.init_Z() ? 100 : -1;
      }
   }

   public boolean init_Z_ItemStack_I(ItemStack stack, int visibleSlot) {
      return this.init_I_ItemStack_I(stack, visibleSlot) >= 0;
   }

   public static String init_Str_Str(String text) {
      if (text != null && !text.isBlank()) {
         String normalized = Normalizer.normalize(text, Form.NFKC).toLowerCase(Locale.ROOT);
         StringBuilder out = new StringBuilder(normalized.length());

         for (int i = 0; i < normalized.length(); i++) {
            char ch = normalized.charAt(i);
            if (Character.isLetterOrDigit(ch)) {
               out.append(ch);
            } else if (Character.isWhitespace(ch) || ch == '_' || ch == '-' || ch == ':' || ch == '/' || ch == '.') {
               out.append(' ');
            } else if (ch != '\'' && ch != '"' && ch != '`' && ch != 8217) {
               out.append(' ');
            }
         }

         return out.toString().trim().replaceAll("\\s+", " ");
      } else {
         return "";
      }
   }

   private static MacroItemTarget sync(String raw) {
      String trimmed = raw == null ? "" : raw.trim();
      if (trimmed.isEmpty()) {
         return new MacroItemTarget();
      }

      MacroItemTarget target = new MacroItemTarget();
      if (init_Z_Str(trimmed)) {
         target.flush = trimmed;
         target.close = trimmed;
         target.init_Z = false;
         return target;
      }

      target.close = load_Str_Str(trimmed);
      target.init_Z = true;
      if (trimmed.contains("{")) {
         target.poll = trimmed;
      }

      return target;
   }

   public MacroItemTarget init_ArrayUtil_Minecraft(Minecraft mc) {
      String source = this.poll != null && !this.poll.isBlank() ? this.poll : this.init_Str();
      if (source != null && source.indexOf(123) >= 0) {
         String trimmed = source.trim();
         if (trimmed.matches("\\{[A-Za-z_][A-Za-z0-9_.-]{0,63}}")) {
            String name = trimmed.substring(1, trimmed.length() - 1);
            Optional<MacroVarValue> value = MacroVariableContext.init_Optional_Str(name);
            if (value.isPresent() && value.get().init_l_DataUtil() == MacroVarValue.Inner_DataUtil.ITEM) {
               MacroVarValue item = value.get();
               MacroItemTarget resolved = new MacroItemTarget();
               resolved.init_I = item.init_Optional_Str("slot").map(v -> init_I_Str_I(v.init_Str(), this.init_I)).orElse(this.init_I);
               resolved.flush = item.init_Optional_Str("id").map(MacroVarValue::init_Str).orElse("");
               resolved.close = item.init_Optional_Str("name").map(MacroVarValue::init_Str).orElse(item.init_Str());
               resolved.init_Z = false;
               return resolved;
            }
         }

         MacroTemplateResolver.Inner_CachePool resolution = MacroVariableContext.init_j_CachePool_Str_Minecraft(source, mc);
         if (resolution.init_Z() && !resolution.init_Str().isBlank()) {
            MacroItemTarget resolved = update(resolution.init_Str());
            if (this.init_Z()) {
               resolved.init_I = this.init_I;
            }

            return resolved;
         } else {
            return null;
         }
      } else {
         return this.init_ArrayUtil();
      }
   }

   private static int init_I_Str_I(String raw, int fallback) {
      try {
         return Integer.parseInt(raw);
      } catch (NumberFormatException ignored) {
         return fallback;
      }
   }

   private static String load_Str_Str(String text) {
      return MacroExecutor.init_Str_Str(text);
   }

   private static boolean init_Z_Str(String text) {
      if (text != null && !text.isBlank()) {
         return text.contains(" ") ? false : text.matches("[a-z0-9_.-]+(:[a-z0-9_./-]+)?");
      } else {
         return false;
      }
   }

   private static int init(String normalizedTarget, String normalizedCandidate, int baseScore) {
      if (!normalizedTarget.isBlank() && !normalizedCandidate.isBlank()) {
         String[] queryWords = normalizedTarget.split(" ");
         String[] candidateWords = normalizedCandidate.split(" ");
         int matches = 0;

         for (String queryWord : queryWords) {
            boolean found = false;

            for (String candidateWord : candidateWords) {
               if (queryWord.equals(candidateWord)) {
                  found = true;
                  matches++;
                  break;
               }
            }

            if (!found) {
               return -1;
            }
         }

         return baseScore + matches;
      } else {
         return -1;
      }
   }

   private static boolean init_Z_ItemStack(ItemStack stack) {
      if (stack != null && !stack.isEmpty()) {
         Component stackName = stack.getHoverName();
         Component defaultName = stack.getItem().getName(stack);
         if (stackName != null && defaultName != null && !stackName.getString().equals(defaultName.getString())) {
            return true;
         }

         String json = MacroExecutor.init_Str_Component(stackName);
         return json.contains("\"color\"")
            || json.contains("\"bold\"")
            || json.contains("\"italic\"")
            || json.contains("\"underlined\"")
            || json.contains("\"strikethrough\"")
            || json.contains("\"obfuscated\"");
      } else {
         return false;
      }
   }

   private static Component init_Component_Component_Str(Component previousComponent, String editedValue) {
      String safeValue = editedValue == null ? "" : editedValue;
      if (previousComponent == null) {
         return Component.literal(safeValue);
      }

      String previousValue = previousComponent.getString();
      if (previousValue.equals(safeValue)) {
         return previousComponent.copy();
      }

      if (safeValue.isEmpty()) {
         return Component.empty();
      }

      if (previousValue.isEmpty()) {
         return Component.literal(safeValue);
      }

      List<Style> previousStyles = init_List_Component_I(previousComponent, previousValue.length());
      if (previousStyles.isEmpty()) {
         return Component.literal(safeValue);
      }

      int prefix = init_I_Str_Str(previousValue, safeValue);
      int suffix = load(previousValue, safeValue, prefix);
      int previousLength = previousValue.length();
      int nextLength = safeValue.length();
      MutableComponent rebuilt = Component.empty();
      StringBuilder segment = new StringBuilder();
      Style segmentStyle = null;

      for (int i = 0; i < nextLength; i++) {
         Style style = init(previousStyles, previousLength, nextLength, prefix, suffix, i);
         if (segmentStyle == null) {
            segmentStyle = style;
         } else if (!segmentStyle.equals(style)) {
            rebuilt.append(Component.literal(segment.toString()).setStyle(segmentStyle));
            segment.setLength(0);
            segmentStyle = style;
         }

         segment.append(safeValue.charAt(i));
      }

      if (!segment.isEmpty()) {
         rebuilt.append(Component.literal(segment.toString()).setStyle(segmentStyle == null ? Style.EMPTY : segmentStyle));
      }

      return rebuilt;
   }

   private static List<Style> init_List_Component_I(Component text, int expectedLength) {
      if (text != null && expectedLength > 0) {
         List<Style> rawStyles = new ArrayList<>(expectedLength);
         text.visit((style, part) -> {
            if (part != null && !part.isEmpty()) {
               Style safeStyle = style == null ? Style.EMPTY : style;

               for (int i = 0; i < part.length(); i++) {
                  rawStyles.add(safeStyle);
               }
            }

            return Optional.empty();
         }, Style.EMPTY);
         List<Style> styles = new ArrayList<>(rawStyles);
         if (styles.isEmpty()) {
            for (int i = 0; i < expectedLength; i++) {
               styles.add(Style.EMPTY);
            }
         } else if (styles.size() < expectedLength) {
            Style fill = styles.get(styles.size() - 1);

            while (styles.size() < expectedLength) {
               styles.add(fill);
            }
         } else if (styles.size() > expectedLength) {
            styles = new ArrayList<>(styles.subList(0, expectedLength));
         }

         return styles;
      } else {
         return Collections.emptyList();
      }
   }

   private static int init_I_Str_Str(String left, String right) {
      int max = Math.min(left.length(), right.length());
      int index = 0;

      while (index < max && left.charAt(index) == right.charAt(index)) {
         index++;
      }

      return index;
   }

   private static int load(String previous, String next, int prefixLength) {
      int previousRemaining = previous.length() - prefixLength;
      int nextRemaining = next.length() - prefixLength;
      int max = Math.min(previousRemaining, nextRemaining);
      int suffix = 0;

      while (suffix < max && previous.charAt(previous.length() - 1 - suffix) == next.charAt(next.length() - 1 - suffix)) {
         suffix++;
      }

      return suffix;
   }

   private static Style init(List<Style> previousStyles, int previousLength, int nextLength, int prefixLength, int suffixLength, int nextIndex) {
      if (previousStyles.isEmpty()) {
         return Style.EMPTY;
      }

      if (nextIndex < prefixLength) {
         return previousStyles.get(Math.min(nextIndex, previousStyles.size() - 1));
      }

      int nextSuffixStart = nextLength - suffixLength;
      int previousSuffixStart = previousLength - suffixLength;
      if (suffixLength > 0 && nextIndex >= nextSuffixStart) {
         int mappedIndex = previousSuffixStart + (nextIndex - nextSuffixStart);
         return previousStyles.get(Math.max(0, Math.min(mappedIndex, previousStyles.size() - 1)));
      }

      int anchorIndex;
      if (prefixLength > 0) {
         anchorIndex = prefixLength - 1;
      } else if (suffixLength > 0) {
         anchorIndex = previousSuffixStart;
      } else {
         anchorIndex = 0;
      }

      return previousStyles.get(Math.max(0, Math.min(anchorIndex, previousStyles.size() - 1)));
   }
}

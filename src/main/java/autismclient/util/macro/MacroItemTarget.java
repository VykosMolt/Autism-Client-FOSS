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
   public static final String TAG_SLOT = "slot";
   public static final String TAG_REGISTRY_ID = "registryId";
   public static final String TAG_DISPLAY = "display";
   public static final String TAG_TEXT_JSON = "textJson";
   public static final String TAG_MANUAL = "manual";
   public static final String TAG_TEMPLATE = "template";
   public int slot = -1;
   public String registryId = "";
   public String displayName = "";
   public String textJson = "";
   public boolean manual = false;
   public String template = "";

   public MacroItemTarget copy() {
      MacroItemTarget copy = new MacroItemTarget();
      copy.slot = this.slot;
      copy.registryId = this.registryId;
      copy.displayName = this.displayName;
      copy.textJson = this.textJson;
      copy.manual = this.manual;
      copy.template = this.template;
      return copy;
   }

   public boolean hasSlot() {
      return this.slot >= 0;
   }

   public boolean hasRegistryId() {
      return this.registryId != null && !this.registryId.isBlank();
   }

   public boolean hasDisplayName() {
      return this.displayName != null && !this.displayName.isBlank();
   }

   public boolean hasTextJson() {
      return this.textJson != null && !this.textJson.isBlank();
   }

   public boolean hasItemFilter() {
      return this.hasRegistryId() || this.hasDisplayName() || this.hasTextJson();
   }

   public Component toStyledComponent() {
      Component rich = MacroExecutor.deserializeComponentJson(this.textJson);
      return rich != null ? rich.copy() : Component.literal(this.rawLabel());
   }

   public Component restyleText(String editedValue) {
      String safeValue = editedValue == null ? "" : editedValue;
      Component rich = MacroExecutor.deserializeComponentJson(this.textJson);
      return rich == null ? Component.literal(safeValue) : preserveComponentStyles(rich, safeValue);
   }

   public MacroItemTarget withManualDisplay(String editedValue) {
      MacroItemTarget target = this.copy();
      String safeValue = stripFormatting(editedValue);
      target.displayName = safeValue;
      target.manual = true;
      target.textJson = "";
      target.template = safeValue.contains("{") ? safeValue : "";
      if (safeValue.isEmpty()) {
         target.registryId = "";
         return target;
      } else {
         return target;
      }
   }

   public MacroItemTarget withStyledDisplay(String editedValue) {
      String safeValue = editedValue == null ? "" : editedValue;
      MacroItemTarget target = this.copy();
      if (safeValue.isEmpty()) {
         target.displayName = "";
         target.textJson = "";
         target.registryId = "";
         target.manual = true;
         target.template = "";
         return target;
      } else {
         Component styled = this.restyleText(safeValue);
         target.displayName = safeValue;
         target.textJson = styled == null ? "" : MacroExecutor.serializeComponentJson(styled);
         target.manual = false;
         target.template = safeValue.contains("{") ? safeValue : "";
         return target;
      }
   }

   public String rawLabel() {
      if (this.hasDisplayName()) {
         return this.displayName;
      } else {
         return this.hasRegistryId() ? this.registryId : "";
      }
   }

   public String formatSummary() {
      String name = this.resolvedDisplayName();
      if (this.hasSlot() && !name.isBlank()) {
         return "Slot " + this.slot + ": " + name;
      } else {
         return this.hasSlot() ? "Slot " + this.slot : name;
      }
   }

   public String resolvedDisplayName() {
      if (this.hasTextJson()) {
         Component rich = MacroExecutor.deserializeComponentJson(this.textJson);
         if (rich != null && !rich.getString().isBlank()) {
            return rich.getString();
         }
      }

      if (this.hasDisplayName()) {
         if (!this.hasRegistryId()) {
            return this.displayName;
         }

         String registryLabel = RegistryDisplayNameCache.itemName(this.registryId);
         return !RegistryDisplayNameCache.init(this.displayName, this.registryId) && !this.displayName.equalsIgnoreCase(registryLabel) ? this.displayName : registryLabel;
      } else {
         return this.hasRegistryId() ? RegistryDisplayNameCache.itemName(this.registryId) : "";
      }
   }

   public Component toDisplayComponent() {
      if (this.hasTextJson()) {
         Component rich = MacroExecutor.deserializeComponentJson(this.textJson);
         if (rich != null) {
            return rich.copy();
         }
      }

      return Component.literal(this.resolvedDisplayName());
   }

   public CompoundTag toTag() {
      CompoundTag tag = new CompoundTag();
      if (this.hasSlot()) {
         tag.putInt("slot", this.slot);
      }

      if (this.hasRegistryId()) {
         tag.putString("registryId", this.registryId);
      }

      if (this.hasDisplayName()) {
         tag.putString("display", this.displayName);
      }

      if (this.hasTextJson()) {
         tag.putString("textJson", this.textJson);
      }

      if (this.template != null && !this.template.isBlank()) {
         tag.putString("template", this.template);
      }

      tag.putBoolean("manual", this.manual);
      return tag;
   }

   public static MacroItemTarget fromTag(CompoundTag tag) {
      MacroItemTarget target = new MacroItemTarget();
      if (tag == null) {
         return target;
      }

      if (tag.contains("slot")) {
         target.slot = tag.getIntOr("slot", -1);
      }

      if (tag.contains("registryId")) {
         target.registryId = tag.getStringOr("registryId", "");
      }

      if (tag.contains("display")) {
         target.displayName = tag.getStringOr("display", "");
      }

      if (tag.contains("textJson")) {
         target.textJson = tag.getStringOr("textJson", "");
      }

      if (tag.contains("manual")) {
         target.manual = tag.getBooleanOr("manual", false);
      }

      target.template = tag.getStringOr("template", "");
      if (target.manual) {
         target.displayName = stripFormatting(target.displayName);
         target.textJson = "";
      } else if (!target.hasTextJson() && target.hasDisplayName()) {
         target.displayName = stripFormatting(target.displayName);
      }

      return target;
   }

   public static MacroItemTarget fromNbtElement(Tag element) {
      return element instanceof CompoundTag compound ? fromTag(compound) : parse(element == null ? "" : element.asString().orElse(""));
   }

   public static List<MacroItemTarget> fromListTag(ListTag list) {
      List<MacroItemTarget> targets = new ArrayList<>();
      if (list == null) {
         return targets;
      }

      for (Tag element : list) {
         MacroItemTarget target = fromNbtElement(element);
         if (target.hasSlot() || target.hasItemFilter()) {
            targets.add(target);
         }
      }

      return targets;
   }

   public static ListTag toListTag(List<MacroItemTarget> targets) {
      ListTag list = new ListTag();
      if (targets == null) {
         return list;
      }

      for (MacroItemTarget target : targets) {
         if (target != null && (target.hasSlot() || target.hasItemFilter())) {
            list.add(target.toTag());
         }
      }

      return list;
   }

   public static List<MacroItemTarget> copyList(List<MacroItemTarget> targets) {
      List<MacroItemTarget> copies = new ArrayList<>();
      if (targets == null) {
         return copies;
      }

      for (MacroItemTarget target : targets) {
         if (target != null) {
            copies.add(target.copy());
         }
      }

      return copies;
   }

   public static MacroItemTarget ofManualText(String text) {
      MacroItemTarget target = new MacroItemTarget();
      target.displayName = stripFormatting(text);
      target.manual = true;
      if (text != null && text.contains("{")) {
         target.template = text;
      }

      return target;
   }

   public static MacroItemTarget ofRegistryId(String registryId) {
      MacroItemTarget target = new MacroItemTarget();
      target.registryId = registryId == null ? "" : registryId.trim();
      target.displayName = target.registryId;
      target.manual = false;
      return target;
   }

   public static MacroItemTarget ofSlot(int slot) {
      MacroItemTarget target = new MacroItemTarget();
      target.slot = slot;
      return target;
   }

   public static MacroItemTarget fromItemStack(ItemStack stack, int visibleSlot) {
      MacroItemTarget target = new MacroItemTarget();
      target.slot = visibleSlot;
      if (stack != null && !stack.isEmpty()) {
         Identifier id = BuiltInRegistries.ITEM.getKey(stack.getItem());
         target.registryId = id == null ? "" : id.toString();
         target.displayName = stack.getHoverName().getString();
         target.manual = false;
         if (hasCustomOrStyledName(stack)) {
            target.textJson = MacroExecutor.serializeComponentJson(stack.getHoverName());
         }

         return target;
      } else {
         return target;
      }
   }

   public static MacroItemTarget parse(String raw) {
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
               target.slot = parsedSlot;
            }
         } catch (NumberFormatException ignored) {
            return parseBody(trimmed);
         }

         body = separator >= 0 && separator + 1 < trimmed.length() ? trimmed.substring(separator + 1).trim() : "";
      }

      if (!body.isBlank()) {
         MacroItemTarget bodyTarget = parseBody(body);
         target.registryId = bodyTarget.registryId;
         target.displayName = bodyTarget.displayName;
         target.textJson = bodyTarget.textJson;
         target.manual = bodyTarget.manual;
      }

      return target;
   }

   public String toSpecString() {
      String body = this.rawLabel();
      if (this.hasSlot() && body.isBlank()) {
         return "#" + this.slot;
      } else {
         return this.hasSlot() ? "#" + this.slot + "|" + body : body;
      }
   }

   public int matchScore(ItemStack stack, int visibleSlot) {
      if (stack == null || stack.isEmpty()) {
         return -1;
      }

      if (this.hasSlot() && this.slot != visibleSlot) {
         return -1;
      }

      if (this.hasTextJson()) {
         String stackJson = MacroExecutor.serializeComponentJson(stack.getHoverName());
         if (this.textJson.equals(stackJson)) {
            return 400;
         } else {
            return this.hasDisplayName() && this.displayName.equals(stack.getHoverName().getString()) ? 350 : -1;
         }
      } else if (this.hasRegistryId()) {
         Identifier id = BuiltInRegistries.ITEM.getKey(stack.getItem());
         if (id == null) {
            return -1;
         }

         String fullId = id.toString();
         String rawPath = id.getPath();
         String spacedPath = rawPath.replace('_', ' ');
         if (this.registryId.equalsIgnoreCase(fullId)) {
            return 320;
         }

         if (this.registryId.equalsIgnoreCase(rawPath)) {
            return 315;
         }

         if (this.registryId.equalsIgnoreCase(spacedPath)) {
            return 310;
         }

         if (this.hasDisplayName()) {
            String normalizedDisplay = normalizeForMatch(this.displayName);
            if (normalizedDisplay.equals(normalizeForMatch(fullId))) {
               return 305;
            }

            if (normalizedDisplay.equals(normalizeForMatch(rawPath))) {
               return 304;
            }

            if (normalizedDisplay.equals(normalizeForMatch(spacedPath))) {
               return 303;
            }
         }

         return -1;
      } else if (this.hasDisplayName()) {
         String normalizedTarget = normalizeForMatch(this.displayName);
         if (normalizedTarget.isBlank()) {
            return this.hasSlot() ? 100 : -1;
         }

         String normalizedDisplay = normalizeForMatch(stack.getHoverName().getString());
         if (normalizedTarget.equals(normalizedDisplay)) {
            return this.manual ? 220 : 240;
         }

         Identifier id = BuiltInRegistries.ITEM.getKey(stack.getItem());
         if (id != null) {
            String fullId = normalizeForMatch(id.toString());
            String rawPath = normalizeForMatch(id.getPath());
            String spacedPath = normalizeForMatch(id.getPath().replace('_', ' '));
            if (normalizedTarget.equals(fullId)) {
               return this.manual ? 210 : 230;
            }

            if (normalizedTarget.equals(rawPath)) {
               return this.manual ? 205 : 225;
            }

            if (normalizedTarget.equals(spacedPath)) {
               return this.manual ? 200 : 220;
            }
         }

         return -1;
      } else {
         return this.hasSlot() ? 100 : -1;
      }
   }

   public boolean matches(ItemStack stack, int visibleSlot) {
      return this.matchScore(stack, visibleSlot) >= 0;
   }

   public static String normalizeForMatch(String text) {
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

   private static MacroItemTarget parseBody(String raw) {
      String trimmed = raw == null ? "" : raw.trim();
      if (trimmed.isEmpty()) {
         return new MacroItemTarget();
      }

      MacroItemTarget target = new MacroItemTarget();
      if (looksLikeRegistryId(trimmed)) {
         target.registryId = trimmed;
         target.displayName = trimmed;
         target.manual = false;
         return target;
      }

      target.displayName = stripFormatting(trimmed);
      target.manual = true;
      if (trimmed.contains("{")) {
         target.template = trimmed;
      }

      return target;
   }

   public MacroItemTarget resolveTemplates(Minecraft mc) {
      String source = this.template != null && !this.template.isBlank() ? this.template : this.rawLabel();
      if (source != null && source.indexOf(123) >= 0) {
         String trimmed = source.trim();
         if (trimmed.matches("\\{[A-Za-z_][A-Za-z0-9_.-]{0,63}}")) {
            String name = trimmed.substring(1, trimmed.length() - 1);
            Optional<MacroVarValue> value = MacroVariableContext.get(name);
            if (value.isPresent() && value.get().getType() == MacroVarValue.ValueType.ITEM) {
               MacroVarValue item = value.get();
               MacroItemTarget resolved = new MacroItemTarget();
               resolved.slot = item.resolvePath("slot").map(v -> parseIntOrDefault(v.asString(), this.slot)).orElse(this.slot);
               resolved.registryId = item.resolvePath("id").map(MacroVarValue::asString).orElse("");
               resolved.displayName = item.resolvePath("name").map(MacroVarValue::asString).orElse(item.asString());
               resolved.manual = false;
               return resolved;
            }
         }

         MacroTemplateResolver.ResolveResult resolution = MacroVariableContext.resolveTemplate(source, mc);
         if (resolution.success() && !resolution.value().isBlank()) {
            MacroItemTarget resolved = parse(resolution.value());
            if (this.hasSlot()) {
               resolved.slot = this.slot;
            }

            return resolved;
         } else {
            return null;
         }
      } else {
         return this.copy();
      }
   }

   private static int parseIntOrDefault(String raw, int fallback) {
      try {
         return Integer.parseInt(raw);
      } catch (NumberFormatException ignored) {
         return fallback;
      }
   }

   private static String stripFormatting(String text) {
      return MacroExecutor.cleanVisibleChatText(text);
   }

   private static boolean looksLikeRegistryId(String text) {
      if (text != null && !text.isBlank()) {
         return text.contains(" ") ? false : text.matches("[a-z0-9_.-]+(:[a-z0-9_./-]+)?");
      } else {
         return false;
      }
   }

   private static int scoreWordMatch(String normalizedTarget, String normalizedCandidate, int baseScore) {
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

   private static boolean hasCustomOrStyledName(ItemStack stack) {
      if (stack != null && !stack.isEmpty()) {
         Component stackName = stack.getHoverName();
         Component defaultName = stack.getItem().getName(stack);
         if (stackName != null && defaultName != null && !stackName.getString().equals(defaultName.getString())) {
            return true;
         }

         String json = MacroExecutor.serializeComponentJson(stackName);
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

   private static Component preserveComponentStyles(Component previousComponent, String editedValue) {
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

      List<Style> previousStyles = extractCharStyles(previousComponent, previousValue.length());
      if (previousStyles.isEmpty()) {
         return Component.literal(safeValue);
      }

      int prefix = commonPrefixLength(previousValue, safeValue);
      int suffix = commonSuffixLength(previousValue, safeValue, prefix);
      int previousLength = previousValue.length();
      int nextLength = safeValue.length();
      MutableComponent rebuilt = Component.empty();
      StringBuilder segment = new StringBuilder();
      Style segmentStyle = null;

      for (int i = 0; i < nextLength; i++) {
         Style style = mappedStyleAt(previousStyles, previousLength, nextLength, prefix, suffix, i);
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

   private static List<Style> extractCharStyles(Component text, int expectedLength) {
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

   private static int commonPrefixLength(String left, String right) {
      int max = Math.min(left.length(), right.length());
      int index = 0;

      while (index < max && left.charAt(index) == right.charAt(index)) {
         index++;
      }

      return index;
   }

   private static int commonSuffixLength(String previous, String next, int prefixLength) {
      int previousRemaining = previous.length() - prefixLength;
      int nextRemaining = next.length() - prefixLength;
      int max = Math.min(previousRemaining, nextRemaining);
      int suffix = 0;

      while (suffix < max && previous.charAt(previous.length() - 1 - suffix) == next.charAt(next.length() - 1 - suffix)) {
         suffix++;
      }

      return suffix;
   }

   private static Style mappedStyleAt(List<Style> previousStyles, int previousLength, int nextLength, int prefixLength, int suffixLength, int nextIndex) {
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

package autismclient.gui.macro.editor;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public final class ActionFieldSchema {
   private final List<FieldDef> fields;

   private ActionFieldSchema(List<FieldDef> fields) {
      this.fields = Collections.unmodifiableList(fields);
   }

   public List<FieldDef> fields() {
      return this.fields;
   }

   public ActionFieldSchema withEnumOptions(String key, List<String> options) {
      return new ActionFieldSchema(
         this.fields
            .stream()
            .map(
               f -> f.key().equals(key)
                  ? new FieldDef(
                     f.key(),
                     f.label(),
                     f.type(),
                     f.min(),
                     f.max(),
                     f.decMin(),
                     f.decMax(),
                     options,
                     f.showWhenKey(),
                     f.showWhenInverted(),
                     f.showWhenValue(),
                     f.addLabel(),
                     f.xyzKeys(),
                     f.xyzDouble(),
                     f.captureMode(),
                     f.mutuallyExclusiveWith(),
                     f.dynamic()
                  )
                  : f
            )
            .toList()
      );
   }

   public static ActionFieldSchema.Builder builder() {
      return new ActionFieldSchema.Builder();
   }

   public static final class Builder {
      private final List<FieldDef> fields = new ArrayList<>();
      private String pendingKey;
      private String pendingLabel;
      private CachePool pendingType;
      private int min = -2147483648;
      private int max = 2147483647;
      private double decMin = -1.7976931348623157E308;
      private double decMax = 1.7976931348623157E308;
      private List<String> enumOpts;
      private String showWhenKey;
      private boolean showWhenInv;
      private String showWhenValue;
      private String addLabel;
      private String[] xyzKeys;
      private boolean xyzDouble;
      private StreamUtil captureMode = StreamUtil.init;
      private String[] exclusiveWith;
      private boolean dynamic;

      private void commit() {
         if (this.pendingKey != null) {
            this.fields
               .add(
                  new FieldDef(
                     this.pendingKey,
                     this.pendingLabel,
                     this.pendingType,
                     this.min,
                     this.max,
                     this.decMin,
                     this.decMax,
                     this.enumOpts,
                     this.showWhenKey,
                     this.showWhenInv,
                     this.showWhenValue,
                     this.addLabel,
                     this.xyzKeys,
                     this.xyzDouble,
                     this.captureMode,
                     this.exclusiveWith,
                     this.dynamic
                  )
               );
            this.pendingKey = null;
            this.pendingLabel = null;
            this.pendingType = null;
            this.min = -2147483648;
            this.max = 2147483647;
            this.decMin = -1.7976931348623157E308;
            this.decMax = 1.7976931348623157E308;
            this.enumOpts = null;
            this.showWhenKey = null;
            this.showWhenInv = false;
            this.showWhenValue = null;
            this.addLabel = null;
            this.xyzKeys = null;
            this.xyzDouble = false;
            this.captureMode = StreamUtil.init;
            this.exclusiveWith = null;
            this.dynamic = false;
         }
      }

      private ActionFieldSchema.Builder start(String key, String label, CachePool type) {
         this.commit();
         this.pendingKey = key;
         this.pendingLabel = label;
         this.pendingType = type;
         return this;
      }

      public ActionFieldSchema.Builder toggle(String key, String label) {
         return this.start(key, label, CachePool.init);
      }

      public ActionFieldSchema.Builder number(String key, String label) {
         return this.start(key, label, CachePool.load);
      }

      public ActionFieldSchema.Builder decimal(String key, String label) {
         return this.start(key, label, CachePool.read);
      }

      public ActionFieldSchema.Builder text(String key, String label) {
         return this.start(key, label, CachePool.write);
      }

      public ActionFieldSchema.Builder macroSelect(String key, String label) {
         return this.start(key, label, CachePool.update);
      }

      public ActionFieldSchema.Builder stringList(String key, String label) {
         return this.start(key, label, CachePool.flush);
      }

      public ActionFieldSchema.Builder blockPos(String key, String label) {
         return this.start(key, label, CachePool.close);
      }

      public ActionFieldSchema.Builder slot(String key, String label) {
         return this.start(key, label, CachePool.open);
      }

      public ActionFieldSchema.Builder targetSummary(String key, String label) {
         return this.start(key, label, CachePool.poll);
      }

      public ActionFieldSchema.Builder condition(String key, String label) {
         return this.start(key, label, CachePool.pop);
      }

      public ActionFieldSchema.Builder capturePacketClick(String key, String label) {
         this.start(key, label, CachePool.push);
         this.captureMode = StreamUtil.flush;
         return this;
      }

      public ActionFieldSchema.Builder enumField(String key, String label, String... options) {
         this.start(key, label, CachePool.sync);
         this.enumOpts = new ArrayList<>(Arrays.asList(options));
         return this;
      }

      public ActionFieldSchema.Builder range(int min, int max) {
         this.min = min;
         this.max = max;
         return this;
      }

      public ActionFieldSchema.Builder decRange(double min, double max) {
         this.decMin = min;
         this.decMax = max;
         return this;
      }

      public ActionFieldSchema.Builder showWhen(String key) {
         this.showWhenKey = key;
         this.showWhenInv = false;
         return this;
      }

      public ActionFieldSchema.Builder hideWhen(String key) {
         this.showWhenKey = key;
         this.showWhenInv = true;
         return this;
      }

      public ActionFieldSchema.Builder showWhenEnum(String key, String value) {
         if (key.equals(this.showWhenKey) && !this.showWhenInv && this.showWhenValue != null && !this.showWhenValue.isEmpty()) {
            this.showWhenValue = this.showWhenValue + "|" + value;
         } else {
            this.showWhenKey = key;
            this.showWhenInv = false;
            this.showWhenValue = value;
         }

         return this;
      }

      public ActionFieldSchema.Builder hideWhenEnum(String key, String value) {
         if (key.equals(this.showWhenKey) && this.showWhenInv && this.showWhenValue != null && !this.showWhenValue.isEmpty()) {
            this.showWhenValue = this.showWhenValue + "|" + value;
         } else {
            this.showWhenKey = key;
            this.showWhenInv = true;
            this.showWhenValue = value;
         }

         return this;
      }

      public ActionFieldSchema.Builder addLabel(String label) {
         this.addLabel = label;
         return this;
      }

      public ActionFieldSchema.Builder xyzKeys(String... keys) {
         this.xyzKeys = keys;
         return this;
      }

      public ActionFieldSchema.Builder xyzDouble(boolean v) {
         this.xyzDouble = v;
         return this;
      }

      public ActionFieldSchema.Builder captureBlock() {
         this.captureMode = StreamUtil.load;
         return this;
      }

      public ActionFieldSchema.Builder captureEntity() {
         this.captureMode = StreamUtil.read;
         return this;
      }

      public ActionFieldSchema.Builder captureCatalog() {
         this.captureMode = StreamUtil.write;
         return this;
      }

      public ActionFieldSchema.Builder captureItemSlot() {
         this.captureMode = StreamUtil.update;
         return this;
      }

      public ActionFieldSchema.Builder capturePacketName() {
         this.captureMode = StreamUtil.sync;
         return this;
      }

      public ActionFieldSchema.Builder captureMenuInput() {
         this.captureMode = StreamUtil.close;
         return this;
      }

      public ActionFieldSchema.Builder captureMenuButton() {
         this.captureMode = StreamUtil.open;
         return this;
      }

      public ActionFieldSchema.Builder captureMenuTitle() {
         this.captureMode = StreamUtil.poll;
         return this;
      }

      public ActionFieldSchema.Builder exclusiveWith(String... keys) {
         this.exclusiveWith = keys;
         return this;
      }

      public ActionFieldSchema.Builder dynamic() {
         this.dynamic = true;
         return this;
      }

      public ActionFieldSchema build() {
         this.commit();
         return new ActionFieldSchema(new ArrayList<>(this.fields));
      }
   }
}

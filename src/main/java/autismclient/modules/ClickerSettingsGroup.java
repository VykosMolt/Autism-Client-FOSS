package autismclient.modules;

import autismclient.api.module.ChoiceSetting;
import autismclient.api.module.DoubleSetting;
import autismclient.api.module.IntSetting;
import autismclient.api.module.RangeSetting;
import autismclient.api.module.Setting;
import autismclient.api.module.ValueRange;
import java.util.EnumMap;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.function.BooleanSupplier;

final class ClickerSettingsGroup {
   static final String TECHNIQUE_ID = "technique";
   static final autismclient.util.clicker.ClickTechnique DEFAULT_TECHNIQUE = autismclient.util.clicker.ClickTechnique.BUTTERFLY;
   static final ValueRange DEFAULT_CPS = new ValueRange(12.0, 14.5);
   private static final String load_Str = "Free Spin";
   private static final double init_D = 300.0;
   private final Module module;
   private final ChoiceSetting techniqueSetting;
   private final BooleanSupplier visible;
   private final double defaultCpsCeiling;
   private final ValueRange defaultButterflyBand;
   private final boolean capRage;
   private final Map<autismclient.util.clicker.ClickTechnique, ClickerSettingsGroup.Inner_DataUtil> sections = new EnumMap<>(autismclient.util.clicker.ClickTechnique.class);
   private autismclient.util.clicker.ClickPatternEngine.Inner_ConfigStore init_DataUtil_ConfigSto;

   ClickerSettingsGroup(Module module, String group, autismclient.util.clicker.ClickTechnique fallback, BooleanSupplier visible) {
      this(module, group, fallback, visible, 0.0);
   }

   ClickerSettingsGroup(Module module, String group, autismclient.util.clicker.ClickTechnique fallback, BooleanSupplier visible, double defaultCpsCeiling) {
      this(module, group, fallback, visible, defaultCpsCeiling, null);
   }

   ClickerSettingsGroup(Module module, String group, autismclient.util.clicker.ClickTechnique fallback, BooleanSupplier visible, ValueRange defaultButterflyBand) {
      this(module, group, fallback, visible, defaultButterflyBand.max(), defaultButterflyBand, false);
   }

   private ClickerSettingsGroup(
      Module module,
      String group,
      autismclient.util.clicker.ClickTechnique fallback,
      BooleanSupplier visible,
      double defaultCpsCeiling,
      ValueRange defaultButterflyBand
   ) {
      this(module, group, fallback, visible, defaultCpsCeiling, defaultButterflyBand, true);
   }

   private ClickerSettingsGroup(
      Module module,
      String group,
      autismclient.util.clicker.ClickTechnique fallback,
      BooleanSupplier visible,
      double defaultCpsCeiling,
      ValueRange defaultButterflyBand,
      boolean capRage
   ) {
      this.module = module;
      this.capRage = capRage;
      this.visible = visible == null ? () -> true : visible;
      this.defaultCpsCeiling = defaultCpsCeiling;
      this.defaultButterflyBand = defaultButterflyBand;
      this.techniqueSetting = module.add(
         new ChoiceSetting("technique", "Technique", fallback.init_Str, autismclient.util.clicker.ClickTechnique.labels())
            .group(group)
            .description("Clicking style")
            .visibleWhen(this.visible)
      );
   }

   private ValueRange init_ValueRange_BufferHelper(autismclient.util.clicker.ClickTechnique kind) {
      ValueRange own = kind == autismclient.util.clicker.ClickTechnique.BUTTERFLY && this.defaultButterflyBand != null
         ? this.defaultButterflyBand
         : new ValueRange(kind.read_D, kind.write_D);
      if (this.defaultCpsCeiling <= 0.0 || own.max() <= this.defaultCpsCeiling || kind == autismclient.util.clicker.ClickTechnique.RAGE && !this.capRage) {
         return own;
      }

      double high = Math.max(1.5, this.defaultCpsCeiling);
      double low = Math.max(1.0, Math.min(own.min(), high - 0.5));
      return new ValueRange(low, high);
   }

   void registerSections() {
      for (autismclient.util.clicker.ClickTechnique each : autismclient.util.clicker.ClickTechnique.values()) {
         this.sections.put(each, new ClickerSettingsGroup.Inner_DataUtil(each));
      }
   }

   autismclient.util.clicker.ClickTechnique technique() {
      return autismclient.util.clicker.ClickTechnique.init(this.techniqueSetting.get());
   }

   autismclient.util.clicker.ClickProfile profile() {
      ClickerSettingsGroup.Inner_DataUtil section = this.sections.get(this.technique());
      return section == null ? autismclient.util.clicker.ClickProfile.forTechnique(this.technique()) : section.profile();
   }

   autismclient.util.clicker.ClickPatternEngine.Inner_ConfigStore init_DataUtil_ConfigSto() {
      if (this.init_DataUtil_ConfigSto == null) {
         try {
            autismclient.util.HumanHandProfile hand = autismclient.util.HumanHandProfile.shared();
            this.init_DataUtil_ConfigSto = new autismclient.util.clicker.ClickPatternEngine.Inner_ConfigStore(
               0.85 + 0.3 * init_D_D(hand.open()), 0.9 + 0.2 * init_D_D(hand.fittsSlope()), hand.sync() >= 0.0
            );
         } catch (Throwable unavailable) {
            this.init_DataUtil_ConfigSto = autismclient.util.clicker.ClickPatternEngine.Inner_ConfigStore.DEFAULT;
         }
      }

      return this.init_DataUtil_ConfigSto;
   }

   String init_Str_Str(String settingId) {
      ClickerSettingsGroup.Inner_DataUtil section = this.sectionFor(settingId);
      return section == null ? null : section.init_Str();
   }

   boolean init(String settingId, String value) {
      ClickerSettingsGroup.Inner_DataUtil section = this.sectionFor(settingId);
      if (section == null) {
         return false;
      }

      section.init(value);
      return true;
   }

   private ClickerSettingsGroup.Inner_DataUtil sectionFor(String settingId) {
      if (settingId != null && settingId.endsWith("-interval")) {
         for (ClickerSettingsGroup.Inner_DataUtil section : this.sections.values()) {
            if (section.read_Str.equals(settingId)) {
               return section;
            }
         }

         return null;
      } else {
         return null;
      }
   }

   private static double init_D_D(double trait) {
      double value = Math.abs(trait * 1000.0);
      return value - Math.floor(value);
   }

   private static double load(double value) {
      return Math.round(value * 100.0) / 100.0;
   }

   private final class Inner_DataUtil {
      private final autismclient.util.clicker.ClickTechnique kind;
      private final String title;
      private final String cpsKey;
      private final String read_Str;
      private final BooleanSupplier visible;
      private RangeSetting init_RangeSetting;
      private DoubleSetting init_DoubleSetting;
      private RangeSetting load_RangeSetting;
      private RangeSetting read_RangeSetting;
      private RangeSetting write_RangeSetting;
      private ChoiceSetting init_ChoiceSetting;
      private DoubleSetting load_DoubleSetting;
      private IntSetting init_IntSetting;
      private IntSetting load_IntSetting;
      private IntSetting read_IntSetting;
      private IntSetting write_IntSetting;
      private IntSetting update;
      private String[] cachedValues;
      private autismclient.util.clicker.ClickProfile cachedProfile;

      Inner_DataUtil(autismclient.util.clicker.ClickTechnique kind) {
         this.kind = kind;
         String key = kind.init_Str.toLowerCase(Locale.ROOT);
         this.title = kind.init_Str + " Clicking";
         this.cpsKey = key + "-cps";
         this.read_Str = key + "-interval";
         this.visible = () -> ClickerSettingsGroup.this.visible.getAsBoolean() && ClickerSettingsGroup.this.technique() == kind;
         if (kind == autismclient.util.clicker.ClickTechnique.RAGE) {
            double rate = ClickerSettingsGroup.this.init_ValueRange_BufferHelper(kind).max();
            this.init_DoubleSetting = this.init(
               new DoubleSetting(this.cpsKey, "CPS", rate, 1.0, kind.load_D, 0.1).description("Fixed rate, no humanizing"), null
            );
            this.init(
               new DoubleSetting(this.read_Str, "Interval", 1000.0 / rate, 1000.0 / kind.load_D, 1000.0, 1.0)
                  .sliderRange(1000.0 / kind.load_D, 300.0)
                  .unit("ms")
                  .description("Same rate, in ms")
                  .keepOnReset(),
               null
            );
         } else {
            ValueRange band = ClickerSettingsGroup.this.init_ValueRange_BufferHelper(kind);
            this.init_RangeSetting = this.init(
               new RangeSetting(this.cpsKey, "CPS", band, 1.0, kind.load_D, 0.1).minSeparation(0.5).description("Active click rate band"), null
            );
            this.init(
               new RangeSetting(this.read_Str, "Interval", new ValueRange(1000.0 / band.max(), 1000.0 / band.min()), 1000.0 / kind.load_D, 1000.0, 1.0)
                  .sliderRange(1000.0 / kind.load_D, 300.0)
                  .unit("ms")
                  .description("Same band, in ms")
                  .keepOnReset(),
               null
            );
            switch (kind) {
               case NORMAL:
                  this.load_RangeSetting = this.init(this.init(key, 40, 90, 150), null);
                  break;
               case JITTER:
                  this.load_IntSetting = this.init(
                     new IntSetting(key + "-wobble", "Wobble", 100, 0, 300, 10).unit("%").description("Hand tension variation"), null
                  );
                  break;
               case BUTTERFLY:
                  this.load_RangeSetting = this.init(this.init(key, 10, 80, 120), null);
                  this.read_IntSetting = this.init(
                     new IntSetting(key + "-sync", "Finger Sync", 100, 25, 400, 25).unit("%").description("Finger coordination strength"), null
                  );
                  break;
               case DRAG:
                  this.read_RangeSetting = this.init(
                     new RangeSetting(key + "-stroke", "Stroke", new ValueRange(5, 10), 2.0, 16.0, 1.0).description("Clicks per drag stroke"), null
                  );
                  this.write_RangeSetting = this.init(
                     new RangeSetting(key + "-reset", "Reset", new ValueRange(100, 220), 50.0, 500.0, 5.0).unit("ms").description("Pause between strokes"),
                     null
                  );
                  this.load_RangeSetting = this.init(this.init(key, 5, 35, 60), null);
                  break;
               case SCROLL:
                  this.init_ChoiceSetting = this.init(
                     new ChoiceSetting(key + "-wheel", "Wheel", "Notched", "Notched", "Free Spin").description("Notched or free spinning"), null
                  );
                  this.read_RangeSetting = this.init(
                     new RangeSetting(key + "-steps", "Steps", new ValueRange(4, 9), 2.0, 16.0, 1.0).description("Steps per wheel flick"),
                     () -> !"Free Spin".equals(this.init_ChoiceSetting.get())
                  );
                  this.load_DoubleSetting = this.init(
                     new DoubleSetting(key + "-decay", "Spin Decay", 1.0, 0.2, 6.0, 0.1).unit("/s").description("How fast spins slow"),
                     () -> "Free Spin".equals(this.init_ChoiceSetting.get())
                  );
                  this.write_RangeSetting = this.init(
                     new RangeSetting(key + "-reset", "Reset", new ValueRange(100, 250), 50.0, 500.0, 5.0).unit("ms").description("Pause between flicks"), null
                  );
                  this.init_IntSetting = this.init(new IntSetting(key + "-pulse", "Pulse", 10, 1, 40, 1).unit("ms").description("Press length per step"), null);
            }

            this.write_IntSetting = this.init(
               new IntSetting(key + "-variation", "Variation", 100, 0, 200, 10).unit("%").description("Spread inside the band"), null
            );
            this.update = this.init(new IntSetting(key + "-fatigue", "Fatigue", 50, 0, 100, 5).unit("%").description("Slowdown from long effort"), null);
         }
      }

      private RangeSetting init(String key, int low, int high, int max) {
         return new RangeSetting(key + "-hold", "Hold", new ValueRange(low, high), 1.0, max, 1.0).unit("ms").description("Button hold time");
      }

      private <T, S extends Setting<T, S>> S init(S setting, BooleanSupplier also) {
         BooleanSupplier when = also == null ? this.visible : () -> this.visible.getAsBoolean() && also.getAsBoolean();
         return ClickerSettingsGroup.this.module.add(setting.group(this.title).visibleWhen(when));
      }

      autismclient.util.clicker.ClickProfile profile() {
         Setting<?, ?>[] parts = new Setting[]{
            this.init_RangeSetting,
            this.init_DoubleSetting,
            this.load_RangeSetting,
            this.read_RangeSetting,
            this.write_RangeSetting,
            this.init_ChoiceSetting,
            this.load_DoubleSetting,
            this.init_IntSetting,
            this.load_IntSetting,
            this.read_IntSetting,
            this.write_IntSetting,
            this.update
         };
         String[] raw = new String[parts.length];
         boolean same = this.cachedValues != null;

         for (int i = 0; i < parts.length; i++) {
            raw[i] = parts[i] == null ? null : parts[i].serialize();
            if (same && !Objects.equals(raw[i], this.cachedValues[i])) {
               same = false;
            }
         }

         if (same && this.cachedProfile != null) {
            return this.cachedProfile;
         }

         autismclient.util.clicker.ClickProfile.Inner_DataUtil builder = autismclient.util.clicker.ClickProfile.builder(this.kind);
         if (this.init_DoubleSetting != null) {
            builder.cpsRange(this.init_DoubleSetting.get(), this.init_DoubleSetting.get());
         } else {
            ValueRange band = this.init_RangeSetting.get();
            builder.cpsRange(band.min(), band.max());
         }

         if (this.load_RangeSetting != null) {
            builder.load(this.load_RangeSetting.get().min(), this.load_RangeSetting.get().max());
         }

         if (this.read_RangeSetting != null) {
            builder.init_CachePool_DataUtil_I_I((int)Math.round(this.read_RangeSetting.get().min()), (int)Math.round(this.read_RangeSetting.get().max()));
         }

         if (this.write_RangeSetting != null) {
            builder.read(this.write_RangeSetting.get().min(), this.write_RangeSetting.get().max());
         }

         if (this.init_IntSetting != null) {
            builder.update(this.init_IntSetting.get().intValue());
         }

         if (this.init_ChoiceSetting != null) {
            builder.freeSpin("Free Spin".equals(this.init_ChoiceSetting.get()), this.load_DoubleSetting.get());
         }

         if (this.load_IntSetting != null) {
            builder.read(this.load_IntSetting.get().intValue() / 100.0);
         }

         if (this.read_IntSetting != null) {
            builder.write(this.read_IntSetting.get().intValue() / 100.0);
         }

         if (this.write_IntSetting != null) {
            builder.init(this.write_IntSetting.get().intValue() / 100.0);
         }

         if (this.update != null) {
            builder.load(this.update.get().intValue() / 100.0);
         }

         this.cachedProfile = builder.init();
         this.cachedValues = raw;
         return this.cachedProfile;
      }

      String init_Str() {
         if (this.init_DoubleSetting != null) {
            return Double.toString(ClickerSettingsGroup.load(1000.0 / this.init_DoubleSetting.get()));
         }

         ValueRange band = this.init_RangeSetting.get();
         double[] ms = autismclient.util.clicker.ClickTimingMath.init(band.min(), band.max());
         return new ValueRange(ms[0], ms[1]).toString();
      }

      void init(String value) {
         if (this.init_DoubleSetting != null) {
            double ms;
            try {
               ms = Double.parseDouble(value.trim());
            } catch (RuntimeException bad) {
               return;
            }

            if (Double.isFinite(ms) && !(ms <= 0.0)) {
               ClickerSettingsGroup.this.module.setLinkedValue(this.cpsKey, Double.toString(ClickerSettingsGroup.load(1000.0 / ms)));
            }
         } else {
            ValueRange ms = ValueRange.parse(value, null);
            if (ms != null && ms.min() > 0.0) {
               double[] band = autismclient.util.clicker.ClickTimingMath.load(ms.min(), ms.max());
               ClickerSettingsGroup.this.module.setLinkedValue(this.cpsKey, new ValueRange(band[0], band[1]).toString());
            }
         }
      }
   }
}

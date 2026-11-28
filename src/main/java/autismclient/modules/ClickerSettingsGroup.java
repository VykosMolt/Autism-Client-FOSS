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
   private static final String FREE_SPIN = "Free Spin";
   private static final double INTERVAL_SLIDER_MAX_MS = 300.0;
   private final Module module;
   private final ChoiceSetting techniqueSetting;
   private final BooleanSupplier visible;
   private final double defaultCpsCeiling;
   private final ValueRange defaultButterflyBand;
   private final boolean capRage;
   private final Map<autismclient.util.clicker.ClickTechnique, ClickerSettingsGroup.TechniqueSection> sections = new EnumMap<>(autismclient.util.clicker.ClickTechnique.class);
   private autismclient.util.clicker.ClickPatternEngine.Inner_ConfigStore cachedHandTraits;

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

   private ValueRange defaultCpsBand(autismclient.util.clicker.ClickTechnique kind) {
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
         this.sections.put(each, new ClickerSettingsGroup.TechniqueSection(each));
      }
   }

   autismclient.util.clicker.ClickTechnique technique() {
      return autismclient.util.clicker.ClickTechnique.fromLabel(this.techniqueSetting.get());
   }

   autismclient.util.clicker.ClickProfile profile() {
      ClickerSettingsGroup.TechniqueSection section = this.sections.get(this.technique());
      return section == null ? autismclient.util.clicker.ClickProfile.forTechnique(this.technique()) : section.profile();
   }

   autismclient.util.clicker.ClickPatternEngine.Inner_ConfigStore personality() {
      if (this.cachedHandTraits == null) {
         try {
            autismclient.util.HumanHandProfile hand = autismclient.util.HumanHandProfile.shared();
            this.cachedHandTraits = new autismclient.util.clicker.ClickPatternEngine.Inner_ConfigStore(
               0.85 + 0.3 * traitFraction(hand.open()), 0.9 + 0.2 * traitFraction(hand.fittsSlope()), hand.curvature() >= 0.0
            );
         } catch (Throwable unavailable) {
            this.cachedHandTraits = autismclient.util.clicker.ClickPatternEngine.Inner_ConfigStore.DEFAULT;
         }
      }

      return this.cachedHandTraits;
   }

   String settingValue(String settingId) {
      ClickerSettingsGroup.TechniqueSection section = this.sectionFor(settingId);
      return section == null ? null : section.intervalValue();
   }

   boolean setSettingValue(String settingId, String value) {
      ClickerSettingsGroup.TechniqueSection section = this.sectionFor(settingId);
      if (section == null) {
         return false;
      }

      section.applyDelayText(value);
      return true;
   }

   private ClickerSettingsGroup.TechniqueSection sectionFor(String settingId) {
      if (settingId != null && settingId.endsWith("-interval")) {
         for (ClickerSettingsGroup.TechniqueSection section : this.sections.values()) {
            if (section.intervalKey.equals(settingId)) {
               return section;
            }
         }

         return null;
      } else {
         return null;
      }
   }

   private static double traitFraction(double trait) {
      double value = Math.abs(trait * 1000.0);
      return value - Math.floor(value);
   }

   private static double round2(double value) {
      return Math.round(value * 100.0) / 100.0;
   }

   private final class TechniqueSection {
      private final autismclient.util.clicker.ClickTechnique kind;
      private final String title;
      private final String cpsKey;
      private final String intervalKey;
      private final BooleanSupplier visible;
      private RangeSetting cpsBand;
      private DoubleSetting fixedCps;
      private RangeSetting hold;
      private RangeSetting burstCount;
      private RangeSetting resetPause;
      private ChoiceSetting wheel;
      private DoubleSetting spinDecay;
      private IntSetting pulse;
      private IntSetting wobble;
      private IntSetting fingerSync;
      private IntSetting variation;
      private IntSetting fatigue;
      private String[] cachedValues;
      private autismclient.util.clicker.ClickProfile cachedProfile;

      TechniqueSection(autismclient.util.clicker.ClickTechnique kind) {
         this.kind = kind;
         String key = kind.init_Str.toLowerCase(Locale.ROOT);
         this.title = kind.init_Str + " Clicking";
         this.cpsKey = key + "-cps";
         this.intervalKey = key + "-interval";
         this.visible = () -> ClickerSettingsGroup.this.visible.getAsBoolean() && ClickerSettingsGroup.this.technique() == kind;
         if (kind == autismclient.util.clicker.ClickTechnique.RAGE) {
            double rate = ClickerSettingsGroup.this.defaultCpsBand(kind).max();
            this.fixedCps = this.register(
               new DoubleSetting(this.cpsKey, "CPS", rate, 1.0, kind.load_D, 0.1).description("Fixed rate, no humanizing"), null
            );
            this.register(
               new DoubleSetting(this.intervalKey, "Interval", 1000.0 / rate, 1000.0 / kind.load_D, 1000.0, 1.0)
                  .sliderRange(1000.0 / kind.load_D, 300.0)
                  .unit("ms")
                  .description("Same rate, in ms")
                  .keepOnReset(),
               null
            );
         } else {
            ValueRange band = ClickerSettingsGroup.this.defaultCpsBand(kind);
            this.cpsBand = this.register(
               new RangeSetting(this.cpsKey, "CPS", band, 1.0, kind.load_D, 0.1).minSeparation(0.5).description("Active click rate band"), null
            );
            this.register(
               new RangeSetting(this.intervalKey, "Interval", new ValueRange(1000.0 / band.max(), 1000.0 / band.min()), 1000.0 / kind.load_D, 1000.0, 1.0)
                  .sliderRange(1000.0 / kind.load_D, 300.0)
                  .unit("ms")
                  .description("Same band, in ms")
                  .keepOnReset(),
               null
            );
            switch (kind) {
               case NORMAL:
                  this.hold = this.register(this.holdRange(key, 40, 90, 150), null);
                  break;
               case JITTER:
                  this.wobble = this.register(
                     new IntSetting(key + "-wobble", "Wobble", 100, 0, 300, 10).unit("%").description("Hand tension variation"), null
                  );
                  break;
               case BUTTERFLY:
                  this.hold = this.register(this.holdRange(key, 10, 80, 120), null);
                  this.fingerSync = this.register(
                     new IntSetting(key + "-sync", "Finger Sync", 100, 25, 400, 25).unit("%").description("Finger coordination strength"), null
                  );
                  break;
               case DRAG:
                  this.burstCount = this.register(
                     new RangeSetting(key + "-stroke", "Stroke", new ValueRange(5, 10), 2.0, 16.0, 1.0).description("Clicks per drag stroke"), null
                  );
                  this.resetPause = this.register(
                     new RangeSetting(key + "-reset", "Reset", new ValueRange(100, 220), 50.0, 500.0, 5.0).unit("ms").description("Pause between strokes"),
                     null
                  );
                  this.hold = this.register(this.holdRange(key, 5, 35, 60), null);
                  break;
               case SCROLL:
                  this.wheel = this.register(
                     new ChoiceSetting(key + "-wheel", "Wheel", "Notched", "Notched", "Free Spin").description("Notched or free spinning"), null
                  );
                  this.burstCount = this.register(
                     new RangeSetting(key + "-steps", "Steps", new ValueRange(4, 9), 2.0, 16.0, 1.0).description("Steps per wheel flick"),
                     () -> !"Free Spin".equals(this.wheel.get())
                  );
                  this.spinDecay = this.register(
                     new DoubleSetting(key + "-decay", "Spin Decay", 1.0, 0.2, 6.0, 0.1).unit("/s").description("How fast spins slow"),
                     () -> "Free Spin".equals(this.wheel.get())
                  );
                  this.resetPause = this.register(
                     new RangeSetting(key + "-reset", "Reset", new ValueRange(100, 250), 50.0, 500.0, 5.0).unit("ms").description("Pause between flicks"), null
                  );
                  this.pulse = this.register(new IntSetting(key + "-pulse", "Pulse", 10, 1, 40, 1).unit("ms").description("Press length per step"), null);
            }

            this.variation = this.register(
               new IntSetting(key + "-variation", "Variation", 100, 0, 200, 10).unit("%").description("Spread inside the band"), null
            );
            this.fatigue = this.register(new IntSetting(key + "-fatigue", "Fatigue", 50, 0, 100, 5).unit("%").description("Slowdown from long effort"), null);
         }
      }

      private RangeSetting holdRange(String key, int low, int high, int max) {
         return new RangeSetting(key + "-hold", "Hold", new ValueRange(low, high), 1.0, max, 1.0).unit("ms").description("Button hold time");
      }

      private <T, S extends Setting<T, S>> S register(S setting, BooleanSupplier also) {
         BooleanSupplier when = also == null ? this.visible : () -> this.visible.getAsBoolean() && also.getAsBoolean();
         return ClickerSettingsGroup.this.module.add(setting.group(this.title).visibleWhen(when));
      }

      autismclient.util.clicker.ClickProfile profile() {
         Setting<?, ?>[] parts = new Setting[]{
            this.cpsBand,
            this.fixedCps,
            this.hold,
            this.burstCount,
            this.resetPause,
            this.wheel,
            this.spinDecay,
            this.pulse,
            this.wobble,
            this.fingerSync,
            this.variation,
            this.fatigue
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

         autismclient.util.clicker.ClickProfile.ProfileBuilder builder = autismclient.util.clicker.ClickProfile.builder(this.kind);
         if (this.fixedCps != null) {
            builder.cpsRange(this.fixedCps.get(), this.fixedCps.get());
         } else {
            ValueRange band = this.cpsBand.get();
            builder.cpsRange(band.min(), band.max());
         }

         if (this.hold != null) {
            builder.holdMs(this.hold.get().min(), this.hold.get().max());
         }

         if (this.burstCount != null) {
            builder.burstCount((int)Math.round(this.burstCount.get().min()), (int)Math.round(this.burstCount.get().max()));
         }

         if (this.resetPause != null) {
            builder.resetPause(this.resetPause.get().min(), this.resetPause.get().max());
         }

         if (this.pulse != null) {
            builder.pulse(this.pulse.get().intValue());
         }

         if (this.wheel != null) {
            builder.freeSpin("Free Spin".equals(this.wheel.get()), this.spinDecay.get());
         }

         if (this.wobble != null) {
            builder.wobble(this.wobble.get().intValue() / 100.0);
         }

         if (this.fingerSync != null) {
            builder.fingerSync(this.fingerSync.get().intValue() / 100.0);
         }

         if (this.variation != null) {
            builder.variation(this.variation.get().intValue() / 100.0);
         }

         if (this.fatigue != null) {
            builder.fatigue(this.fatigue.get().intValue() / 100.0);
         }

         this.cachedProfile = builder.build();
         this.cachedValues = raw;
         return this.cachedProfile;
      }

      String intervalValue() {
         if (this.fixedCps != null) {
            return Double.toString(ClickerSettingsGroup.round2(1000.0 / this.fixedCps.get()));
         }

         ValueRange band = this.cpsBand.get();
         double[] ms = autismclient.util.clicker.ClickTimingMath.cpsToMs(band.min(), band.max());
         return new ValueRange(ms[0], ms[1]).toString();
      }

      void applyDelayText(String value) {
         if (this.fixedCps != null) {
            double ms;
            try {
               ms = Double.parseDouble(value.trim());
            } catch (RuntimeException bad) {
               return;
            }

            if (Double.isFinite(ms) && !(ms <= 0.0)) {
               ClickerSettingsGroup.this.module.setLinkedValue(this.cpsKey, Double.toString(ClickerSettingsGroup.round2(1000.0 / ms)));
            }
         } else {
            ValueRange ms = ValueRange.parse(value, null);
            if (ms != null && ms.min() > 0.0) {
               double[] band = autismclient.util.clicker.ClickTimingMath.msToCps(ms.min(), ms.max());
               ClickerSettingsGroup.this.module.setLinkedValue(this.cpsKey, new ValueRange(band[0], band[1]).toString());
            }
         }
      }
   }
}

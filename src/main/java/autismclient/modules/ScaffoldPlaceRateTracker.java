package autismclient.modules;

import java.util.Random;

final class ScaffoldPlaceRateTracker {
   static final int init = 200;
   private int lastRate;
   private int carryPercent;
   private int jitterRate = -1;
   private int jitter;
   private int pendingPlaces;

   boolean arm(int percentage, boolean climbing, Random random) {
      this.pendingPlaces = 0;
      int rate = Math.clamp(percentage, 0, 200);
      if (rate == 0) {
         this.reset();
         return false;
      }

      if (climbing) {
         return false;
      }

      this.pendingPlaces = this.init(rate, random);
      this.carryPercent = this.accumulated(rate);
      this.lastRate = rate;
      this.jitterRate = -1;
      return this.armed();
   }

   int init(int percentage, Random random) {
      int rate = Math.clamp(percentage, 0, 200);
      if (rate == 0) {
         return 0;
      }

      if (this.jitterRate != rate) {
         this.jitterRate = rate;
         this.jitter = random.nextInt(100);
      }

      return Math.clamp(Math.floorDiv(this.accumulated(rate) + 99 - this.jitter, 100), rate / 100, maxPlacesPerTick(rate));
   }

   private int accumulated(int rate) {
      return Math.min(100 * (maxPlacesPerTick(rate) + 1), (rate == this.lastRate ? this.carryPercent : 0) + rate);
   }

   boolean armed() {
      return this.pendingPlaces > 0;
   }

   int pending() {
      return this.pendingPlaces;
   }

   static int maxPlacesPerTick(int percentage) {
      return (Math.clamp(percentage, 0, 200) + 99) / 100;
   }

   void consume() {
      if (this.pendingPlaces > 0) {
         this.pendingPlaces--;
         this.carryPercent = Math.max(-100, this.carryPercent - 100);
      }
   }

   void reset() {
      this.lastRate = 0;
      this.carryPercent = 0;
      this.jitterRate = -1;
      this.pendingPlaces = 0;
   }
}

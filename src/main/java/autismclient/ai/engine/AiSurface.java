package autismclient.ai.engine;

public enum AiSurface {
   NORMAL(0.6, 1.0, 1.0, 0.0),
   ICE(0.98, 1.0, 1.0, 0.0),
   BLUE_ICE(0.989, 1.0, 1.0, 0.0),
   SLIME(0.8, 1.0, 1.0, 1.0),
   HONEY(0.6, 0.4, 0.5, 0.0),
   SOUL_SAND(0.6, 0.4, 1.0, 0.0),
   BED(0.6, 1.0, 1.0, 0.75);

   private static final AiSurface[] init_mArr = values();
   public final double init_D;
   public final double load_D;
   public final double read_D;
   public final double write_D;

   AiSurface(double friction, double speedFactor, double jumpFactor, double bounce) {
      this.init_D = friction;
      this.load_D = speedFactor;
      this.read_D = jumpFactor;
      this.write_D = bounce;
   }

   public byte init() {
      return (byte)this.ordinal();
   }

   public static AiSurface init(int id) {
      return id >= 0 && id < init_mArr.length ? init_mArr[id] : NORMAL;
   }

   public static AiSurface init(double friction, double speedFactor, double jumpFactor, double bounce) {
      if (bounce >= 0.9) {
         return SLIME;
      } else if (bounce > 0.0) {
         return BED;
      } else if (friction >= 0.985) {
         return BLUE_ICE;
      } else if (friction >= 0.9) {
         return ICE;
      } else if (friction >= 0.7) {
         return SLIME;
      } else if (jumpFactor < 0.9) {
         return HONEY;
      } else {
         return speedFactor < 0.9 ? SOUL_SAND : NORMAL;
      }
   }

   public static AiSurface init(AiPathfinder.Inner_FileCache probe) {
      if (probe == null || !probe.loaded()) {
         return NORMAL;
      } else if (probe.ice()) {
         return ICE;
      } else {
         return probe.soulSand() ? SOUL_SAND : NORMAL;
      }
   }
}

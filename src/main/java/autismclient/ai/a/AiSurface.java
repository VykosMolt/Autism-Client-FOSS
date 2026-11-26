package autismclient.ai.a;

public enum AiSurface {
   init_m(0.6, 1.0, 1.0, 0.0),
   load_m(0.98, 1.0, 1.0, 0.0),
   read_m(0.989, 1.0, 1.0, 0.0),
   write_m(0.8, 1.0, 1.0, 1.0),
   update(0.6, 0.4, 0.5, 0.0),
   sync(0.6, 0.4, 1.0, 0.0),
   flush(0.6, 1.0, 1.0, 0.75);

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
      return id >= 0 && id < init_mArr.length ? init_mArr[id] : init_m;
   }

   public static AiSurface init(double friction, double speedFactor, double jumpFactor, double bounce) {
      if (bounce >= 0.9) {
         return write_m;
      } else if (bounce > 0.0) {
         return flush;
      } else if (friction >= 0.985) {
         return read_m;
      } else if (friction >= 0.9) {
         return load_m;
      } else if (friction >= 0.7) {
         return write_m;
      } else if (jumpFactor < 0.9) {
         return update;
      } else {
         return speedFactor < 0.9 ? sync : init_m;
      }
   }

   public static AiSurface init(AiPathfinder.Inner_FileCache probe) {
      if (probe == null || !probe.loaded()) {
         return init_m;
      } else if (probe.ice()) {
         return load_m;
      } else {
         return probe.soulSand() ? sync : init_m;
      }
   }
}

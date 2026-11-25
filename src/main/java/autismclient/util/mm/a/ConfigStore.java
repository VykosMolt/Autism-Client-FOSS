package autismclient.util.mm.a;

public final class ConfigStore {
   private static final String init_Str = "autismclient/mm/v1";
   private static final byte[] init_BArr = StreamUtil.init_BArr_Str("autismclient/mm/v1/hkdf");
   private final String load_Str;
   private final boolean init_Z;
   private final String read;
   private final String write;
   private final String update;
   private final byte[] load_BArr;

   private ConfigStore(String lobbyId, boolean isPublic, String contentTopic, String controlTopic, String sysTopic, byte[] lobbyTag) {
      this.load_Str = lobbyId;
      this.init_Z = isPublic;
      this.read = contentTopic;
      this.write = controlTopic;
      this.update = sysTopic;
      this.load_BArr = lobbyTag;
   }

   public String init_Str() {
      return this.load_Str;
   }

   public boolean init_Z() {
      return this.init_Z;
   }

   public String load() {
      return this.read;
   }

   public String read() {
      return this.read;
   }

   public String write() {
      return this.write;
   }

   public String update() {
      return this.update;
   }

   public byte[] init_BArr() {
      return (byte[])this.load_BArr.clone();
   }

   public static ConfigStore init(String lobbyId, String contentTopic, String controlTopic, String sysTopic, boolean isPublic) {
      if (init(contentTopic) && init(controlTopic) && init(sysTopic)) {
         byte[] tag = StreamUtil.init_BArr_BArr(init_BArr, StreamUtil.init_BArr_Str(sysTopic), "mm-lobby-tag", 8);
         return new ConfigStore(lobbyId, isPublic, contentTopic, controlTopic, sysTopic, tag);
      } else {
         throw new IllegalArgumentException("invalid server topic");
      }
   }

   private static boolean init(String topic) {
      return topic != null && topic.matches("mm/[0-9a-f]+(?:/sys)?");
   }
}

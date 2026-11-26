package autismclient.util;

import autismclient.AutismClientAddon;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.function.Consumer;
import net.fabricmc.loader.api.FabricLoader;

public final class L {
   public static final String init_Str = autismclient.e.DataUtil.init(
      new int[]{
         -400248941,
         -400248926,
         477184763,
         1907824326,
         -986986561,
         -531813790,
         693641315,
         28043662,
         869354784,
         1180194082,
         -2068689430,
         295136332,
         559293094,
         158216174,
         609733474,
         1721868047,
         1838303264,
         1152826031,
         1377868006,
         -235765301
      }
   );
   public static final String load_Str = autismclient.e.DataUtil.init(
      new int[]{
         -400248939,
         -400248835,
         477184888,
         1907826178,
         -986927043,
         -529966812,
         750899636,
         1803051031,
         60008160,
         1860252536,
         1833250917,
         996200846,
         817458489,
         -428589835,
         -401383693,
         442004425,
         817234855,
         -435521804,
         -616276633,
         -1924709101,
         463563318,
         1485561907,
         -1192219649,
         1695893285,
         1033085113,
         1960868437,
         657377846,
         -1096124063,
         379889462,
         -1108327538
      }
   );
   private static final int init_I = 9676;
   private static final String read_Str = autismclient.e.DataUtil.init(
      new int[]{
         -400248939,
         -400248835,
         477184888,
         1907826178,
         -986927043,
         -529966739,
         750899617,
         1803051031,
         60008190,
         1860252459,
         1833250855,
         996200916,
         817458529,
         -428589835,
         -401383774,
         442004370,
         817234914,
         -435521881,
         -616276691,
         -1924709048,
         463563362,
         1485561961,
         -1192219719,
         1695893283,
         1033085109
      }
   );
   private static final String write_Str = set();
   private static final long init_J = 60000L;
   private static volatile HttpServer init_HttpServer;
   private static volatile Consumer<String> init_Consumer;
   private static volatile String update = autismclient.e.DataUtil.init(new int[]{-400248947});
   private static volatile long load_J = 0L;
   private static volatile boolean init_Z;
   private static volatile String sync = autismclient.e.DataUtil.init(new int[]{-400248947, -400248840, 477184615, 1907818507, -987165401});
   private static volatile boolean load_Z;
   private static volatile boolean read_Z;
   private static volatile String flush = autismclient.e.DataUtil.init(new int[]{-400248945});
   private static volatile long read_J;
   private static volatile String close = autismclient.e.DataUtil.init(new int[]{-400248945});
   private static volatile String open = autismclient.e.DataUtil.init(new int[]{-400248943});
   private static volatile String poll = autismclient.e.DataUtil.init(new int[]{-400248943});
   private static volatile String push = autismclient.e.DataUtil.init(new int[]{-400248941});
   private static volatile long write_J = 0L;

   private L() {
   }

   public static String init_Str() {
      return "";
   }

   public static String load_Str() {
      return "";
   }

   public static String read_Str() {
      return write_Str;
   }

   public static boolean init_Z() {
      return true;
   }

   public static boolean load_Z() {
      return true;
   }

   public static String write_Str() {
      return autismclient.util.mm.LocalPrefsStore.init_EventBus().update_Str();
   }

   public static boolean read_Z() {
      return true;
   }

   public static String update_Str() {
      return sync;
   }

   public static boolean write_Z() {
      return true;
   }

   public static boolean update_Z() {
      return false;
   }

   public static String sync() {
      return flush;
   }

   public static long init_J() {
      return read_J;
   }

   public static String flush() {
      return close;
   }

   public static String close() {
      return freshJwt(2305843009213693951L);
   }

   public static void init_V(String newRole) {
      String _role = newRole == null ? autismclient.e.DataUtil.init(new int[]{-400249099}) : newRole;
      if (!_role.equals(autismclient.e.DataUtil.init(new int[]{-400249099, -400249198, 477179843, 1907672498}))
         && !_role.equals(autismclient.e.DataUtil.init(new int[]{-400249097, -400249194, 477179790, 1907674421, -991633982, -675883049}))
         && !_role.equals(autismclient.e.DataUtil.init(new int[]{-400249097, -400249200, 477179781, 1907674420, -991633969}))
         && !_role.equals(autismclient.e.DataUtil.init(new int[]{-400249095, -400249192, 477179993, 1907676335, -991574520}))
         && !_role.equals(
            autismclient.e.DataUtil.init(new int[]{-400249095, -400249193, 477179975, 1907676334, -991574517, -674036023, 579719365, 791430391, -1235464562})
         )
         && !_role.equals(autismclient.e.DataUtil.init(new int[]{-400249093, -400249191, 477179914, 1907678249, -991514814}))) {
         sync = autismclient.e.DataUtil.init(new int[]{-400249093, -400249202, 477179925, 1907678265, -991514795});
      } else {
         sync = newRole;
      }
   }

   public static String open() {
      return autismclient.util.mm.LocalPrefsStore.init_EventBus().sync_Str();
   }

   private static String run() {
      try {
         return Base64.getEncoder().encodeToString(autismclient.util.mm.a.CachePool.init_CachePool().init_BArr());
      } catch (Throwable t) {
         return autismclient.e.DataUtil.init(new int[]{-400249091});
      }
   }

   public static String init_Str(String code) {
      String _code = code == null ? autismclient.e.DataUtil.init(new int[]{-400249091}) : code;
      if (_code.equals(
         autismclient.e.DataUtil.init(
            new int[]{-400249089, -400249199, 477180045, 1907682068, -991395588, -668494932, 751494145, 1821485135, 631462983, -1899484954, 1245505428}
         )
      )) {
         return autismclient.e.DataUtil.init(
            new int[]{
               -400249077,
               -400249023,
               477180473,
               1907693573,
               -991038087,
               -657412627,
               1095044023,
               -413372581,
               70350571,
               -2114102097,
               -1112656610,
               -132614759,
               183912552,
               1406319410,
               646229683,
               -1441715008,
               -1743492615,
               1786300979,
               -459245512,
               -1351707875,
               1046731629,
               -1911060314,
               886673812,
               1717086744
            }
         );
      } else if (_code.equals(autismclient.e.DataUtil.init(new int[]{-400249089, -400249187, 477180035, 1907682062, -991395635, -668494940, 751494144}))) {
         return autismclient.e.DataUtil.init(
            new int[]{
               -400249075,
               -400249004,
               477180667,
               1907695515,
               -990978574,
               -655565635,
               1152302451,
               1361634704,
               -738996205,
               -1434043656,
               -1505683477,
               568449809,
               442077878,
               819513377,
               -364887426,
               1573388564,
               1530406201,
               197953140,
               1841577202,
               1254315672,
               229083064,
               -1488358477,
               1105528624
            }
         );
      } else if (_code.equals(
         autismclient.e.DataUtil.init(
            new int[]{
               -400249087, -400248977, 477180239, 1907683990, -991336130, -666647897, 808752589, -698474806, -177883774, -1219426575, 852478539, 657029652
            }
         )
      )) {
         return autismclient.e.DataUtil.init(
            new int[]{
               -400249075,
               -400249016,
               477180661,
               1907695516,
               -990978631,
               -655565642,
               1152302390,
               1361634769,
               -738996172,
               -1434043662,
               -1505683459,
               568449793,
               442077866,
               819513459,
               -364887437,
               1573388615,
               1530406267,
               197953137,
               1841577203,
               1254315672,
               229083068,
               -1488358493,
               1105528689,
               -88351542,
               1556068978,
               993497590,
               733651406,
               1268355606,
               664321176,
               -880877707
            }
         );
      } else if (_code.equals(
         autismclient.e.DataUtil.init(
            new int[]{
               -400249087, -400248978, 477180236, 1907683974, -991336130, -666647883, 808752583, -698474794, -177883760, -1219426580, 852478539, 657029640
            }
         )
      )) {
         return autismclient.e.DataUtil.init(
            new int[]{
               -400249073,
               -400248998,
               477180578,
               1907697428,
               -990918926,
               -653718619,
               1209560625,
               -1158325230,
               -1548342661,
               -753985290,
               -1898710605,
               1269514384,
               700243442,
               232707377,
               -1376004746,
               293524886,
               509337976,
               -1390394782,
               -152567634,
               -434628079,
               -588565272,
               -1065656652,
               1324383295,
               -1893789866,
               1422055540,
               1134046206,
               795690928,
               -1103383672,
               154848177,
               505325630,
               -1514773745,
               286656723
            }
         );
      } else if (_code.equals(
         autismclient.e.DataUtil.init(
            new int[]{
               -400249085,
               -400248975,
               477180223,
               1907685904,
               -991276422,
               -664800870,
               866010764,
               1076532559,
               -987230260,
               -539368210,
               459451414,
               1358094221,
               -848749241
            }
         )
      )) {
         return autismclient.e.DataUtil.init(
            new int[]{
               -400249073,
               -400248997,
               477180605,
               1907697439,
               -990918989,
               -653718596,
               1209560629,
               -1158325156,
               -1548342676,
               -753985357,
               -1898710622,
               1269514374,
               700243454,
               232707376,
               -1376004821,
               293524888,
               509337913,
               -1390394816,
               -152567621,
               -434628079,
               -588565265,
               -1065656583,
               1324383293,
               -1893789926,
               1422055551,
               1134046194,
               795690922,
               -1103383598
            }
         );
      } else if (_code.equals(
            autismclient.e.DataUtil.init(
               new int[]{
                  -400249085, -400248991, 477180223, 1907685888, -991276480, -664800842, 866010757, 1076532565, -987230254, -539368210, 459451405, 1358094214
               }
            )
         )
         || _code.equals(
            autismclient.e.DataUtil.init(
               new int[]{
                  -400249083,
                  -400248992,
                  477180388,
                  1907687830,
                  -991216972,
                  -662953803,
                  923269243,
                  -1443427380,
                  -1796577024,
                  140690170,
                  66424517,
                  2059158809,
                  -590583918,
                  -1128229854,
                  -615385779,
                  -1897091262
               }
            )
         )
         || _code.equals(
            autismclient.e.DataUtil.init(
               new int[]{
                  -400249083,
                  -400248971,
                  477180398,
                  1907687817,
                  -991216974,
                  -662953823,
                  923269185,
                  -1443427366,
                  -1796576966,
                  140690168,
                  66424533,
                  2059158787,
                  -590583917,
                  -1128229842,
                  -615385786
               }
            )
         )) {
         return autismclient.e.DataUtil.init(
            new int[]{
               -400249071,
               -400249022,
               477180793,
               1907699349,
               -990859457,
               -651871562,
               1266819062,
               616682132,
               1937277884,
               -73926944,
               2003229792,
               1970579032,
               958408821,
               -354098684,
               1907845375,
               -986338721,
               -511730503,
               1316224569,
               -2146712207,
               -2123571820,
               -1406213767,
               -642954822,
               1543238141,
               595739101,
               1288041906,
               1274594803,
               857730610
            }
         );
      } else if (_code.equals(
         autismclient.e.DataUtil.init(
            new int[]{-400249081, -400248978, 477180330, 1907689783, -991157256, -661106783, 980527421, 331579972, 1689043834, 820748526, -326602566}
         )
      )) {
         return autismclient.e.DataUtil.init(
            new int[]{
               -400249071,
               -400248993,
               477180789,
               1907699333,
               -990859407,
               -651871555,
               1266819063,
               616682176,
               1937277860,
               -73926918,
               2003229798,
               1970578973,
               958408821,
               -354098637,
               1907845363,
               -986338740,
               -511730510,
               1316224634,
               -2146712212,
               -2123571809,
               -1406213827,
               -642954763,
               1543238074,
               595739119,
               1288041906,
               1274594810,
               857730674,
               819844446,
               -354624972,
               1891529009,
               -1492141378,
               988256545,
               571182776,
               526798144,
               -849127607,
               -553153172,
               32117583
            }
         );
      } else if (_code.equals(
         autismclient.e.DataUtil.init(
            new int[]{-400249081, -400248976, 477180347, 1907689754, -991157258, -661106784, 980527410, 331579981, 1689043778, 820748542, -326602578}
         )
      )) {
         return autismclient.e.DataUtil.init(
            new int[]{
               -400249069,
               -400249024,
               477180715,
               1907701254,
               -990799751,
               -650024528,
               1324077218,
               -1903277802,
               1127931362,
               606131427,
               1610202675,
               -1623323766,
               1216574311,
               -940904656,
               896727994,
               2028764893,
               -1532798795,
               -272123282,
               154110246,
               482451794,
               2071105079,
               -220253041,
               1762092733,
               -1209699254,
               1154028523,
               1415143398,
               919770234,
               -1551894669,
               -864097932,
               -1017234930,
               -1469509198,
               1689856492
            }
         );
      } else if (_code.equals(
         autismclient.e.DataUtil.init(
            new int[]{-400249079, -400248982, 477180537, 1907691652, -991097798, -659259730, 1037785846, 2106587328, 879697086, 1500806889}
         )
      )) {
         return autismclient.e.DataUtil.init(
            new int[]{
               -400249069,
               -400249024,
               477180711,
               1907701267,
               -990799775,
               -650024456,
               1324077241,
               -1903277736,
               1127931313,
               606131444,
               1610202675,
               -1623323754,
               1216574320,
               -940904644,
               896727992,
               2028764886,
               -1532798736,
               -272123265,
               154110328
            }
         );
      } else {
         return !_code.equals(
                  autismclient.e.DataUtil.init(
                     new int[]{-400249079, -400248981, 477180537, 1907691662, -991097850, -659259736, 1037785845, 2106587336, 879697086}
                  )
               )
               && !_code.equals(
                  autismclient.e.DataUtil.init(
                     new int[]{
                        -400249077,
                        -400248983,
                        477180471,
                        1907693576,
                        -991038136,
                        -657412673,
                        1095044029,
                        -413372577,
                        70350572,
                        -2114102038,
                        -1112656599,
                        -132614780
                     }
                  )
               )
            ? autismclient.e.DataUtil.init(
               new int[]{
                  -400249067,
                  -400249002,
                  477180909,
                  1907703192,
                  -990740246,
                  -648177501,
                  1381335598,
                  -128270390,
                  318584618,
                  1286189816,
                  1217175795,
                  -922259182,
                  1474739697,
                  -1527710665,
                  -114389123,
                  748901211,
                  1741100090,
                  -1860471181,
                  -1840034374,
                  -1206491938,
                  1253456513,
                  202448813,
                  1980947567,
                  1279829728,
                  1020014902,
                  1555691974,
                  981809913,
                  371333355,
                  -1373571145,
                  368968525
               }
            )
            : autismclient.e.DataUtil.init(
               new int[]{
                  -400249067,
                  -400249018,
                  477180901,
                  1907703185,
                  -990740317,
                  -648177414,
                  1381335655,
                  -128270378,
                  318584687,
                  1286189823,
                  1217175793,
                  -922259181,
                  1474739645,
                  -1527710658,
                  -114389130,
                  748901138,
                  1741100147,
                  -1860471223,
                  -1840034330,
                  -1206492025,
                  1253456629,
                  202448830,
                  1980947569,
                  1279829665,
                  1020014910,
                  1555691983,
                  981809846
               }
            );
      }
   }

   public static boolean init_Z(String code) {
      return code != null && !code.isBlank();
   }

   private static void init(JsonObject r, boolean success) {
      if (success) {
         open = autismclient.e.DataUtil.init(new int[]{-400249065});
      } else if (r != null && r.has(autismclient.e.DataUtil.init(new int[]{-400249063, -400248964, 477181050, 1907707016, -990621146, -644483415}))) {
         String code = r.get(autismclient.e.DataUtil.init(new int[]{-400249063, -400248964, 477181050, 1907707016, -990621146, -644483415})).getAsString();
         if (init_Z(code)) {
            open = code;
            if (r.has(
               autismclient.e.DataUtil.init(
                  new int[]{-400249061, -400248970, 477180975, 1907708946, -990561455, -642636360, 1553110458, 901784397, -2109455136, -968602384, 38094628}
               )
            )) {
               poll = r.get(
                     autismclient.e.DataUtil.init(
                        new int[]{
                           -400249061, -400248970, 477180975, 1907708946, -990561455, -642636360, 1553110458, 901784397, -2109455136, -968602384, 38094628
                        }
                     )
                  )
                  .getAsString();
            }
         }
      }
   }

   public static synchronized void init(Consumer<String> done) {
      init(done, aJ::init_V);
   }

   public static synchronized void init(Consumer<String> done, Consumer<String> presentLink) {
      init_Consumer = done;
      read_V();
      if (init_HttpServer != null) {
         String url = poll();
         AutismClientAddon.LOG
            .info(
               autismclient.e.DataUtil.init(
                  new int[]{
                     -400249059,
                     -400249018,
                     477181161,
                     1907710867,
                     -990501912,
                     -640789314,
                     1610368883,
                     -1618175564,
                     1376165423,
                     -288544004,
                     -354932312,
                     1881999302,
                     -1787565920,
                     420032528,
                     136109161,
                     -75586261,
                     1951793977,
                     376071745,
                     -1226678612,
                     627667829,
                     -2017137244,
                     1893256064,
                     -1438600864,
                     -1646956315,
                     483960870,
                     2117886407,
                     1229968304,
                     -525689114,
                     883503800,
                     1618814725,
                     -1356347783,
                     902888556,
                     -2075226757,
                     92478786,
                     -1428125468,
                     -1322218901,
                     1960884155,
                     657867471,
                     -1080943749,
                     850479025,
                     595046969,
                     1266587844,
                     609517049,
                     1715156478,
                     1630239925,
                     -1002168822,
                     -1002459078,
                     -1011461315,
                     -1290528963,
                     -1351691243,
                     1047245541,
                     -1895128711,
                     1380549032,
                     -152651387,
                     -437222082,
                     -668983622,
                     736345133,
                     1351864278,
                     -1041881129,
                     2061421801,
                     -520436092,
                     1046353161,
                     -1922786814,
                     523151928,
                     -962158324,
                     237865816,
                     -1216095017,
                     955758704,
                     -436253758,
                     -638963573,
                     1666969350,
                     136443279,
                     -65223145,
                     -2021914972,
                     1745146310,
                     -1735040117,
                     2048331987,
                     -926217053,
                     1352041553,
                     -1036386023
                  }
               ),
               url
            );
         presentLink.accept(url);
      }
   }

   public static String poll() {
      return "https://discord.com/oauth2/authorize?client_id=1520738158457655407&response_type=code&redirect_uri="
         + load_Str(
            autismclient.e.DataUtil.init(
               new int[]{
                  -400249059,
                  -400248971,
                  477181168,
                  1907710858,
                  -990501963,
                  -640789275,
                  1610368809,
                  -1618175505,
                  1376165494,
                  -288544109,
                  -354932289,
                  1881999276,
                  -1787565831,
                  420032589,
                  136109114,
                  -75586198,
                  1951794042,
                  376071711,
                  -1226678603,
                  627667760,
                  -2017137158,
                  1893256145,
                  -1438600927,
                  -1646956373,
                  483960877
               }
            )
         )
         + "&scope=identify&prompt=consent";
   }

   public static synchronized void init_V() {
      if (init_Consumer != null) {
         write_V();
         load_V(
            autismclient.e.DataUtil.init(
               new int[]{-400249057, -400248964, 477181091, 1907713006, -990442272, -638942332, 1667627048, 156831790, 566818912, 391514311}
            )
         );
      }
   }

   public static synchronized void load_V() {
      update = autismclient.e.DataUtil.init(new int[]{-400249057});
      load_J = 0L;
      init_Z = false;
      sync = autismclient.e.DataUtil.init(new int[]{-400249055, -400249004, 477181299, 1907714919, -990382797});
      load_Z = false;
      read_Z = false;
      flush = autismclient.e.DataUtil.init(new int[]{-400249055});
      autismclient.util.mm.LocalPrefsStore.init_EventBus().init_V();
      push = autismclient.e.DataUtil.init(new int[]{-400249053});
   }

   public static synchronized void init_V(String newJwt, long expSeconds) {
      if (newJwt != null && !newJwt.isEmpty()) {
         update = newJwt;
         load_J = expSeconds * 1000L;
         init_Z = init(newJwt, autismclient.e.DataUtil.init(new int[]{-400249053, -400249022, 477181274, 1907716841, -990323306, -635248245}));
         autismclient.util.mm.IoWorker.init(newJwt);
      }
   }

   public static String push() {
      String j = update;
      if (j != null && !j.isEmpty()) {
         try {
            int d1 = j.indexOf(46);
            int d2 = j.indexOf(46, d1 + 1);
            if (d1 > 0 && d2 > d1) {
               byte[] payload = Base64.getUrlDecoder().decode(j.substring(d1 + 1, d2));
               JsonObject o = JsonParser.parseString(new String(payload, StandardCharsets.UTF_8)).getAsJsonObject();
               return o.has(autismclient.e.DataUtil.init(new int[]{-400249049, -400249004, 477181391, 1907720682}))
                  ? o.get(autismclient.e.DataUtil.init(new int[]{-400249049, -400249004, 477181391, 1907720682})).getAsString()
                  : autismclient.e.DataUtil.init(new int[]{-400249047});
            } else {
               return autismclient.e.DataUtil.init(new int[]{-400249051});
            }
         } catch (Throwable t) {
            return autismclient.e.DataUtil.init(new int[]{-400249047});
         }
      } else {
         return autismclient.e.DataUtil.init(new int[]{-400249051});
      }
   }

   public static String pop() {
      String j = update;
      return j != null && !j.isEmpty() ? j : null;
   }

   public static String tick() {
      return freshJwt(60000L);
   }

   public static String freshJwt(long minTtlMs) {
      String session;
      String before;
      synchronized (L.class) {
         long now = autismclient.util.mm.IoWorker.init();
         if (!update.isEmpty() && now < load_J - minTtlMs) {
            return update;
         }

         session = get();
         if (session == null) {
            return !update.isEmpty() && now < load_J ? update : null;
         }

         before = update;
      }

      JsonObject r = null;
      int status = -1;
      close = autismclient.e.DataUtil.init(new int[]{-400249045});

      try {
         String form = "session="
            + load_Str(session)
            + "&version="
            + load_Str(write_Str)
            + "&pubkey="
            + load_Str(run())
            + RefQueue.init(
               autismclient.e.DataUtil.init(
                  new int[]{
                     -400249045,
                     -400249021,
                     477181506,
                     1907724536,
                     -990084985,
                     -627860066,
                     2011176834,
                     -2078025887,
                     5706326,
                     176897230,
                     1188845903,
                     -1800483228,
                     19592083,
                     607357791,
                     1648222941,
                     -444697433,
                     -900717303,
                     2142538330,
                     1994177613,
                     1689963513,
                     849258140,
                     557201561,
                     93382057,
                     -1400122829,
                     -454133521,
                     -1193240637,
                     1664245612,
                     52005243,
                     1612159596,
                     -1562663404
                  }
               ),
               write_Str
            );
         Map<String, String> proof = init(
            autismclient.e.DataUtil.init(new int[]{-400249043, -400248963, 477181499, 1907726429, -990025247}),
            autismclient.e.DataUtil.init(new int[]{-400249043, -400249086, 477181440, 1907726433, -990025250, -626013046, 2068435096}),
            form
         );
         if (proof.isEmpty()) {
            synchronized (L.class) {
               long now = autismclient.util.mm.IoWorker.init();
               return !update.isEmpty() && now < load_J ? update : null;
            }
         }

         AutismHttp.Inner_DataUtil result = AutismHttp.read(
            autismclient.e.DataUtil.init(
               new int[]{
                  -400249041,
                  -400249017,
                  477181638,
                  1907728356,
                  -989965821,
                  -624166014,
                  2125693454,
                  1471988861,
                  -1612986918,
                  1537013970,
                  402791875,
                  -398354080,
                  535922975,
                  -566254245,
                  -374011559,
                  1290542499,
                  1352113293,
                  -1034157474,
                  -1994111799,
                  -1687923979,
                  -786038760,
                  1402605205,
                  531091541,
                  -716032193,
                  -722160749,
                  -912143409,
                  1788324752,
                  -396505985,
                  593213200,
                  1209743376,
                  -1152657553,
                  -1372647254,
                  397609838,
                  -559000236,
                  -149138777,
                  -328333874
               }
            ),
            form,
            proof
         );
         status = result.init_I();
         r = result.init_JsonObject();
         if ((
               status == 401
                  || r != null
                     && r.has(autismclient.e.DataUtil.init(new int[]{-400249041, -400249014, 477181632, 1907728354, -989965796, -624166013}))
                     && autismclient.e.DataUtil.init(
                           new int[]{
                              -400249039,
                              -400249023,
                              477181570,
                              1907730301,
                              -989906082,
                              -622318955,
                              -2112015571,
                              -1047971162,
                              1872633814,
                              -2077894972,
                              9764737,
                              302710527,
                              794088519,
                              -1153060334,
                              -1385128942
                           }
                        )
                        .equals(
                           r.get(autismclient.e.DataUtil.init(new int[]{-400249039, -400249004, 477181570, 1907730272, -989906082, -622318975})).getAsString()
                        )
            )
            && r != null
            && r.has(
               autismclient.e.DataUtil.init(
                  new int[]{-400249037, -400249024, 477181771, 1907732198, -989846631, -620471920, -2054757182, 727036162, 1063287064, -1397836582, -383262121}
               )
            )) {
            autismclient.util.mm.IoWorker.init(
               r.get(
                     autismclient.e.DataUtil.init(
                        new int[]{
                           -400249037, -400249024, 477181771, 1907732198, -989846631, -620471920, -2054757182, 727036162, 1063287064, -1397836582, -383262121
                        }
                     )
                  )
                  .getAsLong()
            );

            try {
               String retrySession = get();
               if (retrySession != null) {
                  String retryForm = "session="
                     + load_Str(retrySession)
                     + "&version="
                     + load_Str(write_Str)
                     + "&pubkey="
                     + load_Str(run())
                     + RefQueue.init(
                        autismclient.e.DataUtil.init(
                           new int[]{
                              -400249035,
                              -400248995,
                              477181720,
                              1907734114,
                              -989786915,
                              -618624892,
                              -1997498924,
                              -1792923657,
                              253940480,
                              -717778216,
                              -776289275,
                              1704839662,
                              1310419417,
                              1968294997,
                              887603859,
                              1745918761,
                              -1711091641,
                              -1504233900,
                              613388551,
                              1835179379,
                              1055983318,
                              -1624256621,
                              1187655583,
                              -1837379963,
                              -1124201127,
                              -490497611,
                              1974443606,
                              1078210753,
                              -935205930,
                              1073386478
                           }
                        ),
                        write_Str
                     );
                  Map<String, String> retryProof = init(
                     autismclient.e.DataUtil.init(new int[]{-400249035, -400248987, 477181731, 1907734085, -989786887}),
                     autismclient.e.DataUtil.init(new int[]{-400249033, -400249064, 477181918, 1907736055, -989727488, -616777828, -1940240574}),
                     retryForm
                  );
                  if (!retryProof.isEmpty()) {
                     AutismHttp.Inner_DataUtil retry = AutismHttp.read(
                        autismclient.e.DataUtil.init(
                           new int[]{
                              -400249033,
                              -400248993,
                              477181918,
                              1907736044,
                              -989727461,
                              -616777846,
                              -1940240618,
                              -17916299,
                              -555406142,
                              -37719846,
                              -1169316133,
                              -1889063064,
                              1568584711,
                              1381488979,
                              -123513263,
                              466055083,
                              1562807173,
                              1202385494,
                              -1380756031,
                              146235597,
                              238334736,
                              -1201554579,
                              1406510429,
                              652148999,
                              -1258214757,
                              -349949001,
                              2036483224,
                              -1293528505,
                              -1444679144,
                              -1835377432,
                              -1062128617,
                              1433752210,
                              1496648086,
                              -848546516,
                              -535137313,
                              590611398
                           }
                        ),
                        retryForm,
                        retryProof
                     );
                     status = retry.init_I();
                     r = retry.init_JsonObject();
                  }
               }
            } catch (Throwable var14) {
            }
         }
      } catch (Throwable var17) {
      }

      synchronized (L.class) {
         long now = autismclient.util.mm.IoWorker.init();
         if (!update.equals(before) && !update.isEmpty() && now < load_J - minTtlMs) {
            if (status == 200
               && r != null
               && !r.has(autismclient.e.DataUtil.init(new int[]{-400249031, -400248996, 477181850, 1907737960, -989667770, -614930807}))) {
               init(r);
            }

            return update;
         } else if (status == 200
            && r != null
            && r.has(autismclient.e.DataUtil.init(new int[]{-400249031, -400249005, 477181855, 1907737966}))
            && r.has(autismclient.e.DataUtil.init(new int[]{-400249029, -400248994, 477182046, 1907739884}))) {
            String stored = autismclient.util.mm.LocalPrefsStore.init_EventBus().write_Str();
            if (stored != null && !stored.isEmpty()) {
               update = r.get(autismclient.e.DataUtil.init(new int[]{-400249029, -400249007, 477182033, 1907739880})).getAsString();
               load_J = r.get(autismclient.e.DataUtil.init(new int[]{-400249027, -400249000, 477181980, 1907741806})).getAsLong() * 1000L;
               init_Z = r.has(autismclient.e.DataUtil.init(new int[]{-400249027, -400248996, 477181952, 1907741811, -989548596, -611236719}))
                  && r.get(autismclient.e.DataUtil.init(new int[]{-400249025, -400248994, 477182150, 1907743693, -989489142, -609389457})).getAsBoolean();
               load_Z = r.has(
                     autismclient.e.DataUtil.init(new int[]{-400249025, -400248997, 477182157, 1907743694, -989489150, -609389451, -1711207349, -1507821552})
                  )
                  && r.get(
                        autismclient.e.DataUtil.init(new int[]{-400249023, -400249051, 477182095, 1907745612, -989429440, -607542409, -1653949171, 267185814})
                     )
                     .getAsBoolean();
               read_Z = r.has(
                  autismclient.e.DataUtil.init(new int[]{-400249023, -400249051, 477182095, 1907745612, -989429440, -607542409, -1653949171, 267185814})
               );
               flush = r.has(
                     autismclient.e.DataUtil.init(
                        new int[]{
                           -400249021,
                           -400249054,
                           477182333,
                           1907747527,
                           -989369926,
                           -605695370,
                           -1596690733,
                           2042193185,
                           -1116518637,
                           -252336986,
                           767489100,
                           -1977642916
                        }
                     )
                  )
                  ? r.get(
                        autismclient.e.DataUtil.init(
                           new int[]{
                              -400249021,
                              -400249054,
                              477182333,
                              1907747527,
                              -989369926,
                              -605695370,
                              -1596690733,
                              2042193185,
                              -1116518637,
                              -252336986,
                              767489100,
                              -1977642916
                           }
                        )
                     )
                     .getAsString()
                  : autismclient.e.DataUtil.init(new int[]{-400249019});
               if (r.has(autismclient.e.DataUtil.init(new int[]{-400249019, -400249033, 477182259, 1907749450, -989310216}))) {
                  init_V(r.get(autismclient.e.DataUtil.init(new int[]{-400249017, -400249035, 477182453, 1907751364, -989250754})).getAsString());
               }

               autismclient.util.mm.IoWorker.init(update);
               init(r, true);
               push = autismclient.e.DataUtil.init(new int[]{-400249017});
               read_J++;
               if (r.has(
                  autismclient.e.DataUtil.init(new int[]{-400249015, -400249056, 477182396, 1907753310, -989191050, -600154272, -1424915905, -1222719358})
               )) {
                  autismclient.util.mm.LocalPrefsStore.init_EventBus()
                     .write_V(
                        r.get(
                              autismclient.e.DataUtil.init(
                                 new int[]{-400249015, -400249056, 477182396, 1907753310, -989191050, -600154272, -1424915905, -1222719358}
                              )
                           )
                           .getAsString()
                     );
               }

               init(r);
               return update;
            } else {
               return null;
            }
         } else {
            if (status == 200
               && r != null
               && r.has(autismclient.e.DataUtil.init(new int[]{-400249013, -400249042, 477182564, 1907755230, -989131592, -598307201}))) {
               String code = r.get(autismclient.e.DataUtil.init(new int[]{-400249013, -400249042, 477182564, 1907755230, -989131592, -598307201}))
                  .getAsString();
               close = code;
               init(r, false);
               if (load_Z(code) && init_Z(code, now)) {
                  bi.init(init_Str(code), -42149);
                  load_V();
                  if (!AutismLiteVariant.enabled()) {
                     autismclient.a.DataUtil.init_V(
                        autismclient.e.DataUtil.init(
                              new int[]{
                                 -400249011,
                                 -400249053,
                                 477182523,
                                 1907757146,
                                 -989071926,
                                 -596460181,
                                 -1310399303,
                                 -1967671906,
                                 -868284298,
                                 -1147012443,
                                 -1197646025,
                                 1527679936
                              }
                           )
                           .equals(code)
                     );
                  }
               }
            }

            return !update.isEmpty() && now < load_J ? update : null;
         }
      }
   }

   private static boolean init_Z(String code, long now) {
      String bucket = autismclient.e.DataUtil.init(
               new int[]{
                  -400249011,
                  -400249048,
                  477182508,
                  1907757150,
                  -989071876,
                  -596460163,
                  -1310399309,
                  -1967671916,
                  -868284344,
                  -1147012446,
                  -1197646019,
                  1527679937,
                  113437418,
                  -778409734,
                  1639098805,
                  -727543526
               }
            )
            .equals(code)
         ? autismclient.e.DataUtil.init(
            new int[]{
               -400249009,
               -400249043,
               477182707,
               1907759060,
               -989012468,
               -594613150,
               -1253140879,
               -192664575,
               -1677631066,
               -466954054,
               -1590672903,
               -2066222758
            }
         )
         : code;
      if (bucket.equals(push) && now - write_J >= 5000L) {
         push = autismclient.e.DataUtil.init(new int[]{-400249009});
         write_J = 0L;
         return true;
      }

      if (!bucket.equals(push)) {
         push = bucket;
         write_J = now;
      }

      return false;
   }

   private static boolean load_Z(String code) {
      return autismclient.e.DataUtil.init(
               new int[]{
                  -400249007,
                  -400249037,
                  477182641,
                  1907760982,
                  -988952754,
                  -592766112,
                  -1195882697,
                  1582342791,
                  1807989728,
                  213104316,
                  -1983700037,
                  -1365158184
               }
            )
            .equals(code)
         || autismclient.e.DataUtil.init(
               new int[]{
                  -400249007,
                  -400249036,
                  477182632,
                  1907760962,
                  -988952712,
                  -592766111,
                  -1195882697,
                  1582342800,
                  1807989708,
                  213104294,
                  -1983700047,
                  -1365158203,
                  629768294,
                  -1952021762,
                  -383135687,
                  1007696406
               }
            )
            .equals(code)
         || autismclient.e.DataUtil.init(new int[]{-400249005, -400249039, 477182831, 1907762906, -988893279, -590919056, -1138624268}).equals(code)
         || autismclient.e.DataUtil.init(
               new int[]{-400249005, -400249027, 477182817, 1907762880, -988893296, -590919048, -1138624267, -937617125, 998642995, 893162674, 1918240352}
            )
            .equals(code)
         || autismclient.e.DataUtil.init(
               new int[]{
                  -400249003, -400249029, 477182755, 1907764802, -988833582, -589072013, -1081366111, 837390230, 189296494, 1573221037, 1525213247, 36970952
               }
            )
            .equals(code)
         || autismclient.e.DataUtil.init(
               new int[]{-400249003, -400249028, 477182780, 1907764841, -988833554, -589072001, -1081366097, 837390230, 189296488, 1573221052, 1525213236}
            )
            .equals(code);
   }

   private static void init(JsonObject r) {
      String rotated = r.has(
            autismclient.e.DataUtil.init(new int[]{-400249001, -400249052, 477182959, 1907766731, -988774088, -587224976, -1024107677, -1682569708})
         )
         ? r.get(autismclient.e.DataUtil.init(new int[]{-400249001, -400249052, 477182959, 1907766731, -988774088, -587224976, -1024107677, -1682569708}))
            .getAsString()
         : autismclient.e.DataUtil.init(new int[]{-400248999});
      if (!rotated.isEmpty()) {
         byte[] sealed = autismclient.util.mm.a.DataUtil.init(rotated.getBytes(StandardCharsets.UTF_8));
         if (sealed != null) {
            autismclient.util.mm.LocalPrefsStore.init_EventBus().update_V(Base64.getEncoder().encodeToString(sealed));
            if (autismclient.util.mm.LocalPrefsStore.init_EventBus().sync_Z()) {
               AutismClientAddon.LOG
                  .info(
                     autismclient.e.DataUtil.init(
                        new int[]{
                           -400248999,
                           -400249086,
                           477182885,
                           1907768663,
                           -988714460,
                           -585377926,
                           -966849473,
                           92437640,
                           -1429396765,
                           -1361629568,
                           739159276,
                           1439100109,
                           1662430056,
                           -4278547,
                           -132637384,
                           183209189,
                           1384514557,
                           -29725119,
                           -921477726,
                           1498962989,
                           -776786843,
                           1689409090,
                           832073254,
                           24468577,
                           758523127,
                           2039377349,
                           -1203810656,
                           1336576942,
                           -1515786896,
                           255243655,
                           -677380315,
                           476048365,
                           1872594404,
                           -2079118054,
                           -28147791,
                           -872580315,
                           -1280184713,
                           -1031023349,
                           -1896951762,
                           1324039011,
                           -1904466192,
                           1091087496,
                           -536022862,
                           563161845,
                           278146877,
                           32617692,
                           1011147050,
                           1280789247,
                           1049757677,
                           -1817252844,
                           -500260476,
                           1671797362,
                           286109781,
                           279467099,
                           73544199,
                           -2015094220,
                           1956587728,
                           524675968,
                           -914910585,
                           1702545845,
                           1239314752,
                           -235949116,
                           1275511573,
                           886155767,
                           1701022165
                        }
                     )
                  );
            }
         }
      }
   }

   private static String get() {
      String sealedB64 = autismclient.util.mm.LocalPrefsStore.init_EventBus().write_Str();
      if (sealedB64 != null && !sealedB64.isEmpty()) {
         try {
            byte[] plain = autismclient.util.mm.a.DataUtil.read(Base64.getDecoder().decode(sealedB64));
            if (plain != null) {
               return new String(plain, StandardCharsets.UTF_8);
            }
         } catch (Throwable var2) {
         }

         autismclient.util.mm.LocalPrefsStore.init_EventBus().init_V();
         return null;
      } else {
         return null;
      }
   }

   private static void read_V() {
      if (init_HttpServer == null) {
         try {
            HttpServer s = HttpServer.create(
               new InetSocketAddress(
                  autismclient.e.DataUtil.init(
                     new int[]{-400248997, -400248982, 477183028, 1907770507, -988654871, -583530963, -909591130, 1867445070, 2056223847, -681571090}
                  ),
                  9676
               ),
               0
            );
            s.createContext(autismclient.e.DataUtil.init(new int[]{-400248997, -400248972}), L::init);
            s.setExecutor(Executors.newVirtualThreadPerTaskExecutor());
            s.start();
            init_HttpServer = s;
         } catch (IOException e) {
            write_V();
            load_V(autismclient.e.DataUtil.init(new int[]{-400248995, -400249037, 477183009, 1907772490, -988595214, -581683856, -852332876, -652514709}));
         }
      }
   }

   private static void write_V() {
      HttpServer s = init_HttpServer;
      init_HttpServer = null;
      if (s != null) {
         s.stop(0);
      }
   }

   private static void init(HttpExchange ex) throws IOException {
      String code = null;
      String q = ex.getRequestURI().getRawQuery();
      if (q != null) {
         for (String part : q.split(autismclient.e.DataUtil.init(new int[]{-400248995, -400248965}))) {
            int eq = part.indexOf(61);
            if (eq > 0 && autismclient.e.DataUtil.init(new int[]{-400248993, -400249028, 477183213, 1907774372, -988535770}).equals(part.substring(0, eq))) {
               code = read(part.substring(eq + 1));
               break;
            }
         }
      }

      String err = code == null
         ? autismclient.e.DataUtil.init(
            new int[]{-400248993, -400249028, 477183203, 1907774382, -988535776, -579836860, -795074456, 1122492654, 437530528, 678545543}
         )
         : exchangeCode(code);
      init(
         ex,
         err.isEmpty()
            ? autismclient.e.DataUtil.init(
               new int[]{
                  -400248991,
                  -400249038,
                  477183145,
                  1907776293,
                  -988476049,
                  -577989818,
                  -737816282,
                  -1397467356,
                  -371815958,
                  1358603915,
                  -832948758,
                  -51608858,
                  -1599875492,
                  1943464648,
                  117860915,
                  -641278296,
                  1595208420,
                  -2088149496,
                  -308121946,
                  -961844630,
                  247586666,
                  -914750905,
                  1707492133,
                  1392649855,
                  222469102,
                  -1693395507,
                  -955652168,
                  439554470,
                  741288036,
                  1505090012,
                  -586851218,
                  -1012519456,
                  -1323334418,
                  1926302998,
                  -414146379,
                  46365042,
                  1437315440,
                  1607102813,
                  -1719421642,
                  -1762462412,
                  1198239201,
                  -1509292912,
                  456563104,
                  1268555939,
                  670530151,
                  -688404263,
                  134303350,
                  -131564308,
                  216471288,
                  -1879327655,
                  1870385836,
                  2147387442,
                  2144502652,
                  2055069723,
                  -717347011,
                  -762919811,
                  2119286774,
                  1273377688,
                  820005874
               }
            )
            : autismclient.e.DataUtil.init(
               new int[]{
                  -400248991,
                  -400249038,
                  477183145,
                  1907776293,
                  -988476049,
                  -577989874,
                  -737816277,
                  -1397467286,
                  -371816029,
                  1358603907,
                  -832948827,
                  -51608913,
                  -1599875479,
                  1943464642,
                  117860898,
                  -641278298,
                  1595208359,
                  -2088149445,
                  -308121939,
                  -961844674,
                  247586684,
                  -914750887,
                  1707492132,
                  1392649772,
                  222469119,
                  -1693395582,
                  -955652116,
                  439554490,
                  741288037,
                  1505089994,
                  -586851218,
                  -1012519433,
                  -1323334418,
                  1926303004,
                  -414146379,
                  46365042,
                  1437315429,
                  1607102812,
                  -1719421582,
                  -1762462361,
                  1198239222,
                  -1509292912,
                  456563188,
                  1268555937,
                  670530173,
                  -688404274,
                  134303352
               }
            )
      );
      write_V();
      load_V(err);
   }

   private static String exchangeCode(String code) {
      try {
         JsonObject r = AutismHttp.postForm(
            autismclient.e.DataUtil.init(
               new int[]{
                  -400248989,
                  -400249077,
                  477183114,
                  1907778224,
                  -988416561,
                  -576142762,
                  -680557894,
                  377540009,
                  -1181162642,
                  2038662278,
                  -1225975689,
                  649455676,
                  -1341710165,
                  1356658567,
                  -893256219,
                  -1921141889,
                  574139953,
                  618469890,
                  1992700789,
                  1644178849,
                  -570061916,
                  -492049087,
                  1926346977,
                  -412788501,
                  88455463,
                  -1552846949,
                  -893612508,
                  -1932184781,
                  231814820,
                  -1403673892,
                  -564218973,
                  -310919554,
                  -1048574942,
                  1853916436,
                  1636837735,
                  -797640518
               }
            ),
            "code="
               + load_Str(code)
               + "&redirect_uri="
               + load_Str(
                  autismclient.e.DataUtil.init(
                     new int[]{
                        -400248989,
                        -400249077,
                        477183114,
                        1907778224,
                        -988416561,
                        -576142817,
                        -680557905,
                        377540009,
                        -1181162640,
                        2038662357,
                        -1225975755,
                        649455718,
                        -1341710093,
                        1356658567,
                        -893256268,
                        -1921141980,
                        574140020,
                        618469969,
                        1992700735,
                        1644178938,
                        -570061840,
                        -492049125,
                        1926346919,
                        -412788499,
                        88455467
                     }
                  )
               )
               + "&version="
               + load_Str(write_Str)
               + "&pubkey="
               + load_Str(run())
               + RefQueue.init(
                  autismclient.e.DataUtil.init(
                     new int[]{
                        -400248987,
                        -400249075,
                        477183304,
                        1907780146,
                        -988357107,
                        -574295724,
                        -623299708,
                        -2142419929,
                        -1990509360,
                        -1576246648,
                        -1619002827,
                        1350520254,
                        -1083544599,
                        769852549,
                        -1904373597,
                        1093961721,
                        -446928265,
                        -969878012,
                        -1444041,
                        -44764893,
                        -1387710234,
                        -69347261,
                        2145201583,
                        2076740469,
                        -45557911,
                        -1412298363,
                        -831572890,
                        -8956751,
                        -277658138,
                        -17470498
                     }
                  ),
                  write_Str
               )
         );
         if (r == null) {
            return autismclient.e.DataUtil.init(new int[]{-400248987, -400249077, 477183321, 1907780146, -988357110, -574295736, -623299636, -2142419869});
         }

         if (r.has(autismclient.e.DataUtil.init(new int[]{-400248985, -400249086, 477183240, 1907782074, -988297388, -572448677}))) {
            init(r, false);
            return r.get(autismclient.e.DataUtil.init(new int[]{-400248985, -400249086, 477183240, 1907782074, -988297388, -572448677})).getAsString();
         }

         if (r.has(autismclient.e.DataUtil.init(new int[]{-400248983, -400249062, 477183453, 1907783993, -988237942, -570601662, -508783019, 1407594850}))
            && r.has(autismclient.e.DataUtil.init(new int[]{-400248983, -400249085, 477183439, 1907783998}))
            && r.has(autismclient.e.DataUtil.init(new int[]{-400248981, -400249074, 477183374, 1907785916}))) {
            byte[] sealed = autismclient.util.mm.a.DataUtil.init(
               r.get(autismclient.e.DataUtil.init(new int[]{-400248979, -400249058, 477183569, 1907787837, -988118778, -566907578, -394266407, 662642302}))
                  .getAsString()
                  .getBytes(StandardCharsets.UTF_8)
            );
            String name = r.has(autismclient.e.DataUtil.init(new int[]{-400248979, -400249085, 477183573, 1907787811, -988118768}))
               ? r.get(autismclient.e.DataUtil.init(new int[]{-400248977, -400249087, 477183507, 1907789757, -988059050})).getAsString()
               : autismclient.e.DataUtil.init(
                  new int[]{
                     -400248977,
                     -400249045,
                     477183515,
                     1907789731,
                     -988059056,
                     -565060514,
                     -337008250,
                     -1857317642,
                     -1742275179,
                     1824045190,
                     710829317,
                     560875825,
                     207282501
                  }
               );
            String idToken = r.has(
                  autismclient.e.DataUtil.init(new int[]{-400248975, -400249064, 477183700, 1907791654, -987999586, -563213480, -279749801, -82310278})
               )
               ? r.get(autismclient.e.DataUtil.init(new int[]{-400248975, -400249064, 477183700, 1907791654, -987999586, -563213480, -279749801, -82310278}))
                  .getAsString()
               : autismclient.e.DataUtil.init(new int[]{-400248973});
            autismclient.util.mm.LocalPrefsStore.init_EventBus()
               .init_V_Str_2(sealed == null ? autismclient.e.DataUtil.init(new int[]{-400248973}) : Base64.getEncoder().encodeToString(sealed), name, idToken);
            update = r.get(autismclient.e.DataUtil.init(new int[]{-400248971, -400249057, 477183835, 1907795490})).getAsString();
            load_J = r.get(autismclient.e.DataUtil.init(new int[]{-400248971, -400249072, 477183828, 1907795494})).getAsLong() * 1000L;
            init_Z = r.has(autismclient.e.DataUtil.init(new int[]{-400248969, -400249066, 477183758, 1907797429, -987820734, -557672361}))
               && r.get(autismclient.e.DataUtil.init(new int[]{-400248969, -400249066, 477183758, 1907797429, -987820734, -557672361})).getAsBoolean();
            load_Z = r.has(
                  autismclient.e.DataUtil.init(new int[]{-400248967, -400249059, 477183943, 1907799348, -987761272, -555825329, -50716603, -1572215442})
               )
               && r.get(autismclient.e.DataUtil.init(new int[]{-400248967, -400249059, 477183943, 1907799348, -987761272, -555825329, -50716603, -1572215442}))
                  .getAsBoolean();
            read_Z = r.has(autismclient.e.DataUtil.init(new int[]{-400248965, -400249057, 477183881, 1907801266, -987701562, -553978295, 6541575, 202791916}));
            flush = r.has(
                  autismclient.e.DataUtil.init(
                     new int[]{
                        -400248965,
                        -400249062,
                        477183877,
                        1907801279,
                        -987701566,
                        -553978290,
                        6541595,
                        202791897,
                        1991579739,
                        1609428126,
                        -1647332732,
                        472295956
                     }
                  )
               )
               ? r.get(
                     autismclient.e.DataUtil.init(
                        new int[]{
                           -400248963,
                           -400249060,
                           477184071,
                           1907803197,
                           -987642112,
                           -552131252,
                           63800021,
                           1977799271,
                           1182232981,
                           -2005480608,
                           -2040359610,
                           1173360534
                        }
                     )
                  )
                  .getAsString()
               : autismclient.e.DataUtil.init(new int[]{-400248963});
            if (r.has(autismclient.e.DataUtil.init(new int[]{-400248961, -400249075, 477184013, 1907805068, -987582394}))) {
               init_V(r.get(autismclient.e.DataUtil.init(new int[]{-400248961, -400249075, 477184013, 1907805068, -987582394})).getAsString());
            }

            autismclient.util.mm.IoWorker.init(update);
            init(r, true);
            return autismclient.e.DataUtil.init(new int[]{-400248959});
         } else {
            return autismclient.e.DataUtil.init(new int[]{-400248981, -400249083, 477183379, 1907785912, -988178240, -568754622, -451524854, -1112365083});
         }
      } catch (Throwable t) {
         return autismclient.e.DataUtil.init(new int[]{-400248959, -400248849, 477184197, 1907806998, -987522922, -548437204, 178316624, 1232846671});
      }
   }

   private static void load_V(String error) {
      Consumer<String> cb = init_Consumer;
      init_Consumer = null;
      if (cb != null) {
         try {
            cb.accept(error == null ? autismclient.e.DataUtil.init(new int[]{-400248957}) : error);
         } catch (Throwable var3) {
         }
      }
   }

   public static Map<String, String> init(String method, String path, String body) {
      try {
         String verb = method == null
            ? autismclient.e.DataUtil.init(new int[]{-400248957, -400248892, 477184155, 1907808944})
            : method.toUpperCase(Locale.ROOT);
         String requestPath = path != null && !path.isBlank() ? path : autismclient.e.DataUtil.init(new int[]{-400248955, -400248918});
         byte[] bytes = (body == null ? autismclient.e.DataUtil.init(new int[]{-400248955}) : body).getBytes(StandardCharsets.UTF_8);
         long timestamp = autismclient.util.mm.IoWorker.init();
         String nonce = Base64.getUrlEncoder().withoutPadding().encodeToString(autismclient.util.mm.a.StreamUtil.init_BArr_I(18));
         String digest = autismclient.util.mm.a.StreamUtil.init_Str_BArr(autismclient.util.mm.a.StreamUtil.init_BArr_BArr2(bytes));
         String canonical = "MM-POP-V1\n" + verb + "\n" + requestPath + "\n" + digest + "\n" + timestamp + "\n" + nonce;
         String signature = Base64.getEncoder()
            .encodeToString(autismclient.util.mm.a.CachePool.init_CachePool().init_BArr_BArr(canonical.getBytes(StandardCharsets.UTF_8)));
         return Map.of(
            autismclient.e.DataUtil.init(
               new int[]{-400248953, -400248865, 477184375, 1907812773, -987344042, -542896028, 350091400, -2032065853, 1430467312, 1394811246}
            ),
            Long.toString(timestamp),
            autismclient.e.DataUtil.init(
               new int[]{-400248953, -400248865, 477184375, 1907812773, -987344042, -542896028, 350091410, -2032065851, 1430467315, 1394811240, 289472571}
            ),
            nonce,
            autismclient.e.DataUtil.init(
               new int[]{
                  -400248951,
                  -400248879,
                  477184437,
                  1907814695,
                  -987284588,
                  -541048986,
                  407349833,
                  -257058491,
                  621120572,
                  2074869603,
                  -103554307,
                  1084780698,
                  -731534168,
                  -1202721091,
                  1370352507
               }
            ),
            signature
         );
      } catch (Throwable t) {
         return Map.of();
      }
   }

   private static boolean init(String token, String name) {
      if (token != null && !token.isEmpty()) {
         try {
            int d1 = token.indexOf(46);
            int d2 = token.indexOf(46, d1 + 1);
            byte[] payload = Base64.getUrlDecoder().decode(token.substring(d1 + 1, d2));
            JsonObject o = JsonParser.parseString(new String(payload, StandardCharsets.UTF_8)).getAsJsonObject();
            return o.has(name) && o.get(name).getAsBoolean();
         } catch (Throwable t) {
            return false;
         }
      } else {
         return false;
      }
   }

   private static String set() {
      try {
         return FabricLoader.getInstance()
            .getModContainer(autismclient.e.DataUtil.init(new int[]{-400248951, -400248856, 477184493, 1907814686, -987284560, -541049032, 407349879}))
            .map(m -> m.getMetadata().getVersion().getFriendlyString())
            .orElse(autismclient.e.DataUtil.init(new int[]{-400248949, -400248834, 477184440, 1907816583, -987224839, -539202014, 464608047, 1517948864}));
      } catch (Throwable t) {
         return autismclient.e.DataUtil.init(new int[]{-400248949, -400248834, 477184440, 1907816583, -987224839, -539202014, 464608047, 1517948864});
      }
   }

   private static String load_Str(String s) {
      return URLEncoder.encode(s, StandardCharsets.UTF_8);
   }

   private static String read(String s) {
      return URLDecoder.decode(s, StandardCharsets.UTF_8);
   }

   private static void init(HttpExchange ex, String text) throws IOException {
      byte[] b = text.getBytes(StandardCharsets.UTF_8);
      ex.sendResponseHeaders(200, b.length);

      try (OutputStream o = ex.getResponseBody()) {
         o.write(b);
      }
   }
}

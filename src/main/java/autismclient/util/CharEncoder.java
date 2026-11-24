package autismclient.util;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Random;
import java.util.Set;
import java.util.regex.Pattern;
import net.minecraft.core.UUIDUtil;

public final class CharEncoder {
   private static final Pattern init_Pattern = Pattern.compile("^[A-Za-z0-9_]{3,16}$");
   private static final int init_I = 5000;
   private static final String[] init_StrArr = new String[]{
      "Swift",
      "Silent",
      "Dark",
      "Lucky",
      "Wild",
      "Rapid",
      "Clever",
      "Mighty",
      "Tiny",
      "Brave",
      "Calm",
      "Eager",
      "Fuzzy",
      "Golden",
      "Happy",
      "Jolly",
      "Keen",
      "Lazy",
      "Merry",
      "Noble",
      "Proud",
      "Quiet",
      "Sneaky",
      "Witty"
   };
   private static final String[] load = new String[]{
      "Fox",
      "Wolf",
      "Bear",
      "Hawk",
      "Tiger",
      "Panda",
      "Otter",
      "Raven",
      "Cobra",
      "Falcon",
      "Badger",
      "Lynx",
      "Moose",
      "Bison",
      "Viper",
      "Heron",
      "Gecko",
      "Llama",
      "Mantis",
      "Newt",
      "Osprey",
      "Puma",
      "Sloth",
      "Wren"
   };

   private CharEncoder() {
   }

   public static List<String> init(int count) {
      int wanted = Math.max(1, Math.min(5000, count));
      Set<String> taken = init();
      Set<String> batch = new LinkedHashSet<>();
      Random random = new Random();
      int guard = wanted * 30;

      while (batch.size() < wanted && guard-- > 0) {
         String adjective = init_StrArr[random.nextInt(init_StrArr.length)];
         String noun = load[random.nextInt(load.length)];
         int style = random.nextInt(4);
         String var11;
         switch (style) {
            case 0:
               var11 = adjective + noun + init(random, 2, 3);
               break;
            case 1:
               var11 = adjective + noun;
               break;
            case 2:
               var11 = noun + init(random, 2, 4);
               break;
            default:
               var11 = adjective + noun + "_" + init(random, 1, 2);
         }

         String name = var11;
         if (name.length() > 16) {
            name = name.substring(0, 16);
         }

         if (init_Pattern.matcher(name).matches()) {
            String key = name.toLowerCase(Locale.ROOT);
            if (!taken.contains(key) && !batch.contains(name)) {
               batch.add(name);
               taken.add(key);
            }
         }
      }

      return new ArrayList<>(batch);
   }

   public static CharEncoder.Inner_DataUtil init(String raw) {
      List<String> names = new ArrayList<>();
      if (raw != null && !raw.isBlank()) {
         Set<String> taken = init();
         Set<String> seen = new LinkedHashSet<>();
         int skipped = 0;
         int duplicates = 0;

         for (String token : raw.split("[\\s,;]+")) {
            if (!token.isBlank()) {
               String candidate = token;
               int colon = candidate.indexOf(58);
               if (colon >= 0) {
                  candidate = candidate.substring(0, colon);
               }

               candidate = candidate.trim();
               if (!candidate.isEmpty() && init_Pattern.matcher(candidate).matches()) {
                  String key = candidate.toLowerCase(Locale.ROOT);
                  if (!taken.contains(key) && seen.add(key)) {
                     names.add(candidate);
                  } else {
                     duplicates++;
                  }
               } else {
                  skipped++;
               }
            }
         }

         return new CharEncoder.Inner_DataUtil(names, skipped, duplicates);
      } else {
         return new CharEncoder.Inner_DataUtil(names, 0, 0);
      }
   }

   public static int init(List<String> names) {
      if (names != null && !names.isEmpty()) {
         if (names.size() > 5000) {
            names = names.subList(0, 5000);
         }

         TokenStore manager = TokenStore.init_TokenStore();
         AutismConfig config = AutismConfig.getGlobal();
         boolean setPassword = config.accountGenSetPassword;
         boolean sharedForAll = "Set For All".equalsIgnoreCase(config.accountGenPasswordMode);
         String shared = config.accountGenSharedPassword == null ? "" : config.accountGenSharedPassword.trim();
         List<BitSetUtil> batch = new ArrayList<>();

         for (String name : names) {
            if (name != null && !name.isBlank()) {
               BitSetUtil account = new BitSetUtil();
               account.init_MethodUtil = MethodUtil.update;
               account.load_Str = name;
               account.update = name;
               account.sync = UUIDUtil.createOfflinePlayerUUID(name).toString();
               if (setPassword) {
                  account.flush = sharedForAll ? shared : aL.init();
               }

               batch.add(account);
            }
         }

         return manager.init(batch);
      } else {
         return 0;
      }
   }

   private static Set<String> init() {
      Set<String> keys = new LinkedHashSet<>();

      for (BitSetUtil account : TokenStore.init_TokenStore().init_List()) {
         String name = account.init_Str();
         if (name != null && !name.isBlank()) {
            keys.add(name.toLowerCase(Locale.ROOT));
         }
      }

      return keys;
   }

   private static String init(Random random, int min, int max) {
      int length = min + random.nextInt(max - min + 1);
      StringBuilder out = new StringBuilder(length);

      for (int i = 0; i < length; i++) {
         out.append(random.nextInt(10));
      }

      return out.toString();
   }

   public record Inner_DataUtil(List<String> init_List, int init_I, int load) {
   }
}

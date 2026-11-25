package autismclient.util;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Locale.Builder;

public final class ProxyGeoLookup {
   private static final String init = "https://api.country.is/?fields=city,continent,subdivision,location,asn";

   private ProxyGeoLookup() {
   }

   public static ProxyGeoLookup.Inner_StreamUtil init_bX_StreamUtil_Str(String address) {
      long now = System.currentTimeMillis();

      try {
         String clean = read(address);
         if (clean.isBlank()) {
            return new ProxyGeoLookup.Inner_StreamUtil("", ProxyGeoLookup.Inner_DataUtil.init("", now));
         }

         InetAddress inetAddress = InetAddress.getByName(clean);
         String ip = inetAddress.getHostAddress();
         return !init_Z_InetAddress(inetAddress) ? new ProxyGeoLookup.Inner_StreamUtil(ip, ProxyGeoLookup.Inner_DataUtil.load(ip, now)) : new ProxyGeoLookup.Inner_StreamUtil(ip, null);
      } catch (Exception ignored) {
         return new ProxyGeoLookup.Inner_StreamUtil("", ProxyGeoLookup.Inner_DataUtil.init("", now));
      }
   }

   public static Map<String, ProxyGeoLookup.Inner_DataUtil> init_Map_List(List<String> ips) {
      Map<String, ProxyGeoLookup.Inner_DataUtil> out = new HashMap<>();
      if (ips != null && !ips.isEmpty()) {
         long now = System.currentTimeMillis();

         try {
            HttpURLConnection connection = (HttpURLConnection)URI.create("https://api.country.is/?fields=city,continent,subdivision,location,asn")
               .toURL()
               .openConnection();
            connection.setRequestMethod("POST");
            connection.setConnectTimeout(8000);
            connection.setReadTimeout(8000);
            connection.setRequestProperty("Accept", "application/json");
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setRequestProperty("User-Agent", "Autism-ProxyGeo");
            connection.setDoOutput(true);
            String body = init_Str_List(ips);
            byte[] bytes = body.getBytes(StandardCharsets.UTF_8);
            connection.setRequestProperty("Content-Length", Integer.toString(bytes.length));

            try (OutputStream output = connection.getOutputStream()) {
               output.write(bytes);
            }

            int code = connection.getResponseCode();
            if (code < 200 || code >= 300) {
               return init_Map_List_J(ips, now);
            }

            String raw;
            try (InputStream input = connection.getInputStream()) {
               raw = new String(input.readAllBytes(), StandardCharsets.UTF_8);
            }

            JsonElement parsed = JsonParser.parseString(raw);
            if (parsed.isJsonArray()) {
               for (JsonElement element : parsed.getAsJsonArray()) {
                  ProxyGeoLookup.Inner_DataUtil result = init_bX_DataUtil_JsonElement_J(element, now);
                  if (result != null && !result.init_Str().isBlank()) {
                     out.put(result.init_Str(), result);
                  }
               }
            } else {
               ProxyGeoLookup.Inner_DataUtil result = init_bX_DataUtil_JsonElement_J(parsed, now);
               if (result != null && !result.init_Str().isBlank()) {
                  out.put(result.init_Str(), result);
               }
            }
         } catch (Exception ignored) {
            return init_Map_List_J(ips, now);
         }

         for (String ip : ips) {
            out.putIfAbsent(ip, ProxyGeoLookup.Inner_DataUtil.init(ip, now));
         }

         return out;
      } else {
         return out;
      }
   }

   private static ProxyGeoLookup.Inner_DataUtil init_bX_DataUtil_JsonElement_J(JsonElement element, long now) {
      if (element != null && element.isJsonObject()) {
         JsonObject object = element.getAsJsonObject();
         String ip = init_Str_JsonObject_StrArr(object, "ip", "query");
         if (ip.isBlank()) {
            return null;
         }

         String countryCode = init_Str_JsonObject_StrArr(object, "country", "countryCode", "country_code").toUpperCase(Locale.ROOT);
         String countryName = init_Str_JsonObject_StrArr(object, "countryName", "country_name");
         if (countryName.isBlank()) {
            countryName = init_Str_Str(countryCode);
         }

         String continent = init_Str_JsonObject_StrArr(object, "continent", "continentCode", "continent_code");
         String region = load(continent);
         String city = init_Str_JsonObject_StrArr(object, "city");
         String subdivision = init_Str_JsonElement(object.get("subdivision"));
         return countryCode.isBlank() && region.isBlank() && city.isBlank()
            ? ProxyGeoLookup.Inner_DataUtil.init(ip, now)
            : new ProxyGeoLookup.Inner_DataUtil(ProxyEntry.Inner_StreamUtil.read, ip, countryCode, countryName, region, city, subdivision, now);
      } else {
         return null;
      }
   }

   private static Map<String, ProxyGeoLookup.Inner_DataUtil> init_Map_List_J(List<String> ips, long now) {
      Map<String, ProxyGeoLookup.Inner_DataUtil> out = new HashMap<>();

      for (String ip : ips) {
         out.put(ip, ProxyGeoLookup.Inner_DataUtil.init(ip, now));
      }

      return out;
   }

   private static String init_Str_List(List<String> ips) {
      JsonArray array = new JsonArray();

      for (String ip : ips) {
         array.add(ip);
      }

      return array.toString();
   }

   private static String init_Str_JsonElement(JsonElement element) {
      if (element == null || element.isJsonNull()) {
         return "";
      } else if (element.isJsonPrimitive()) {
         return element.getAsString();
      } else {
         return element.isJsonObject() ? init_Str_JsonObject_StrArr(element.getAsJsonObject(), "name", "code", "iso_code") : "";
      }
   }

   private static String init_Str_Str(String countryCode) {
      if (countryCode != null && !countryCode.isBlank()) {
         try {
            return new Builder().setRegion(countryCode.toUpperCase(Locale.ROOT)).build().getDisplayCountry(Locale.ENGLISH);
         } catch (RuntimeException ignored) {
            return "";
         }
      } else {
         return "";
      }
   }

   private static String load(String continent) {
      String code = write(continent).toUpperCase(Locale.ROOT);
      String var10000;
      switch (code) {
         case "AF":
            var10000 = "Africa";
            break;
         case "AN":
            var10000 = "Antarctica";
            break;
         case "AS":
            var10000 = "Asia";
            break;
         case "EU":
            var10000 = "Europe";
            break;
         case "NA":
            var10000 = "North America";
            break;
         case "OC":
            var10000 = "Oceania";
            break;
         case "SA":
            var10000 = "South America";
            break;
         default:
            var10000 = write(continent);
      }

      return var10000;
   }

   private static String init_Str_JsonObject_StrArr(JsonObject object, String... keys) {
      if (object != null && keys != null) {
         for (String key : keys) {
            JsonElement element = object.get(key);
            if (element != null && element.isJsonPrimitive()) {
               String value = element.getAsString();
               if (value != null && !value.isBlank()) {
                  return value.trim();
               }
            }
         }

         return "";
      } else {
         return "";
      }
   }

   private static String read(String address) {
      String clean = write(address).trim();
      return clean.startsWith("[") && clean.endsWith("]") ? clean.substring(1, clean.length() - 1) : clean;
   }

   private static boolean init_Z_InetAddress(InetAddress address) {
      if (address != null
         && !address.isAnyLocalAddress()
         && !address.isLoopbackAddress()
         && !address.isLinkLocalAddress()
         && !address.isSiteLocalAddress()
         && !address.isMulticastAddress()) {
         byte[] bytes = address.getAddress();
         if (bytes.length == 4) {
            return init_Z_BArr(bytes);
         } else {
            return bytes.length == 16 ? load(bytes) : true;
         }
      } else {
         return false;
      }
   }

   private static boolean init_Z_BArr(byte[] bytes) {
      int a = Byte.toUnsignedInt(bytes[0]);
      int b = Byte.toUnsignedInt(bytes[1]);
      int c = Byte.toUnsignedInt(bytes[2]);
      if (a == 0 || a == 10 || a == 127 || a >= 224) {
         return false;
      } else if (a == 100 && b >= 64 && b <= 127) {
         return false;
      } else if (a == 169 && b == 254) {
         return false;
      } else if (a == 172 && b >= 16 && b <= 31) {
         return false;
      } else if (a != 192 || b != 0 && b != 2 && b != 168) {
         return a != 198 || b != 18 && b != 19 && (b != 51 || c != 100) ? a != 203 || b != 0 || c != 113 : false;
      } else {
         return false;
      }
   }

   private static boolean load(byte[] bytes) {
      int first = Byte.toUnsignedInt(bytes[0]);
      int second = Byte.toUnsignedInt(bytes[1]);
      int third = Byte.toUnsignedInt(bytes[2]);
      int fourth = Byte.toUnsignedInt(bytes[3]);
      return (first & 254) == 252 ? false : first != 32 || second != 1 || third != 13 || fourth != 184;
   }

   private static String write(String value) {
      return value == null ? "" : value;
   }

   public record Inner_DataUtil(
      ProxyEntry.Inner_StreamUtil init_bW_StreamUtil, String init_Str, String load, String read, String write, String update, String sync, long init_J
   ) {
      public static ProxyGeoLookup.Inner_DataUtil init(String ip, long now) {
         return new ProxyGeoLookup.Inner_DataUtil(ProxyEntry.Inner_StreamUtil.write, ProxyGeoLookup.write(ip), "", "", "", "", "", now);
      }

      public static ProxyGeoLookup.Inner_DataUtil load(String ip, long now) {
         return new ProxyGeoLookup.Inner_DataUtil(ProxyEntry.Inner_StreamUtil.update, ProxyGeoLookup.write(ip), "", "", "", "", "", now);
      }
   }

   public record Inner_StreamUtil(String init_Str, ProxyGeoLookup.Inner_DataUtil init_bX_DataUtil) {
   }
}

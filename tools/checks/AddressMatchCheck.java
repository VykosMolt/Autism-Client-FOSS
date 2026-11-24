package autismclient.util;

public class AddressMatchCheck {
   public static void main(String[] args) {
      expect("2001:db8::2", "[2001:db8::2]:25565", true);
      expect("2001:db8::2", "[2001:db8::2]", true);
      expect("[2001:db8::2]:25566", "[2001:db8::2]:25565", false);
      expect("example.test:25565", "example.test.:25565", true);
      expect("play.example.com:25565", "*.example.com:25565", true);
      expect("play.example.com:25565", "*:25565", true);
      expect("play.example.com:25565", "*.example.com:25566", false);
      expect("play.example.com:25566", "play.example.com", true);

      String host = "play.example.com";
      int exact = bV.auditScore(host, 25565, "play.example.com");
      int wildApex = bV.auditScore(host, 25565, "*.play.example.com");
      int wildParent = bV.auditScore(host, 25565, "*.example.com");
      int exactPort = bV.auditScore(host, 25565, "play.example.com:25565");
      if (!(exactPort > exact && exact > wildApex && wildApex > wildParent && wildParent > bV.auditScore(host, 25565, "*:25565")
         && bV.auditScore(host, 25565, "*:25565") > bV.auditScore(host, 25565, "*"))) {
         fail("ranking exact+port > exact > longer wildcard > shorter wildcard > *:port > *");
      }

      System.out.println("PASS AddressMatchCheck: IPv6, root dot, wildcard+port, exact beats wildcard");
   }

   private static void expect(String address, String pattern, boolean match) {
      if (bV.load_Z_Str_Str(address, pattern) != match) {
         fail("match(" + address + ", " + pattern + ") != " + match);
      }
   }

   private static void fail(String message) {
      System.out.println("FAIL AddressMatchCheck: " + message);
      System.exit(1);
   }
}

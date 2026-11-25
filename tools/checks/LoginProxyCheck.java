package autismclient.util;

import com.sun.net.httpserver.HttpServer;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.atomic.AtomicInteger;

// Account requests must go through a SOCKS launch proxy, and must not go direct when it is down.
public class LoginProxyCheck {
   static void check(boolean ok, String what) {
      if (!ok) {
         System.out.println("FAIL LoginProxyCheck: " + what);
         System.exit(1);
      }
   }

   static void pump(InputStream in, OutputStream out) {
      Thread.ofVirtual().start(() -> {
         try (in; out) {
            in.transferTo(out);
         } catch (Exception e) {
         }
      });
   }

   // Just enough SOCKS5: no auth, CONNECT, relay.
   static void relay(Socket client, AtomicInteger used) {
      try {
         InputStream in = client.getInputStream();
         OutputStream out = client.getOutputStream();
         in.readNBytes(in.readNBytes(2)[1]);
         out.write(new byte[]{5, 0});
         byte[] head = in.readNBytes(4);
         String host = switch (head[3]) {
            case 1 -> java.net.InetAddress.getByAddress(in.readNBytes(4)).getHostAddress();
            case 3 -> new String(in.readNBytes(in.read()), StandardCharsets.US_ASCII);
            default -> java.net.InetAddress.getByAddress(in.readNBytes(16)).getHostAddress();
         };
         byte[] port = in.readNBytes(2);
         Socket target = new Socket(host, (port[0] & 255) << 8 | port[1] & 255);
         used.incrementAndGet();
         out.write(new byte[]{5, 0, 0, 1, 0, 0, 0, 0, 0, 0});
         pump(in, target.getOutputStream());
         pump(target.getInputStream(), out);
      } catch (Exception e) {
      }
   }

   public static void main(String[] args) throws Exception {
      // one connection per request, so that connections through the proxy can be counted
      System.setProperty("http.keepAlive", "false");
      HttpServer web = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
      web.createContext("/", exchange -> {
         String body = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
         String auth = String.valueOf(exchange.getRequestHeaders().getFirst("Authorization"));
         String type = String.valueOf(exchange.getRequestHeaders().getFirst("Content-Type"));
         byte[] answer = (exchange.getRequestMethod() + "|" + type + "|" + body + "|" + auth).getBytes(StandardCharsets.UTF_8);
         exchange.sendResponseHeaders(exchange.getRequestURI().getPath().equals("/refused") ? 401 : 200, answer.length);
         exchange.getResponseBody().write(answer);
         exchange.close();
      });
      web.start();
      String url = "http://127.0.0.1:" + web.getAddress().getPort();

      AtomicInteger used = new AtomicInteger();
      ServerSocket socks = new ServerSocket(0, 50, java.net.InetAddress.getLoopbackAddress());
      Thread acceptor = Thread.ofVirtual().start(() -> {
         while (true) {
            try {
               Socket client = socks.accept();
               Thread.ofVirtual().start(() -> relay(client, used));
            } catch (Exception e) {
               return;
            }
         }
      });
      Proxy proxy = new Proxy(Proxy.Type.SOCKS, new InetSocketAddress("127.0.0.1", socks.getLocalPort()));

      MicrosoftAuth.Inner_DataUtil post = MicrosoftAuth.auditSend(proxy, url + "/token", "application/json", "{\"a\":1}", null);
      check(post.init_I() == 200 && "POST|application/json|{\"a\":1}|null".equals(post.init_Str()), "POST through the proxy, got " + post);
      check(used.get() == 1, "the POST went through the SOCKS proxy");
      MicrosoftAuth.Inner_DataUtil get = MicrosoftAuth.auditSend(proxy, url + "/profile", null, null, "tok");
      check(get.init_I() == 200 && get.init_Str().equals("GET|null||Bearer tok") && used.get() == 2, "GET with a bearer through the proxy, got " + get);
      MicrosoftAuth.Inner_DataUtil refused = MicrosoftAuth.auditSend(proxy, url + "/refused", "application/x-www-form-urlencoded", "x=1", null);
      check(refused.init_I() == 401 && refused.init_Str().startsWith("POST|"), "an error answer keeps its status and body, got " + refused);

      // without a proxy nothing changes: java.net.http, direct
      MicrosoftAuth.Inner_DataUtil direct = MicrosoftAuth.auditSend(Proxy.NO_PROXY, url + "/token", "application/json", "{}", null);
      check(direct.init_I() == 200 && used.get() == 3, "no proxy: direct, got " + direct);

      // a SOCKS proxy that is down fails the request; it must not fall back to a direct connection
      int deadPort = socks.getLocalPort();
      socks.close();
      acceptor.join();
      MicrosoftAuth.Inner_DataUtil down = MicrosoftAuth.auditSend(new Proxy(Proxy.Type.SOCKS, new InetSocketAddress("127.0.0.1", deadPort)), url + "/token", "application/json", "{}", null);
      check(down.init_I() == -1, "proxy down: the request fails, got " + down);
      web.stop(0);
      System.out.println("PASS LoginProxyCheck");
      System.exit(0);
   }
}

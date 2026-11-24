package autismclient.util;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class AuthCallbackCheck {
   static void check(boolean ok, String what) {
      if (!ok) {
         System.out.println("FAIL: " + what);
         System.exit(1);
      }
   }

   public static void main(String[] args) throws Exception {
      Method handler = aW.class.getDeclaredMethod("init", HttpExchange.class);
      handler.setAccessible(true);
      Field state = aW.class.getDeclaredField("flush");
      state.setAccessible(true);
      state.set(null, "expected-state");
      List<String> results = new ArrayList<>();
      Field consumer = aW.class.getDeclaredField("init_Consumer");
      consumer.setAccessible(true);
      consumer.set(null, (Consumer<String>) results::add);

      HttpServer server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
      server.createContext("/", exchange -> {
         try {
            handler.invoke(null, exchange);
         } catch (ReflectiveOperationException e) {
            throw new RuntimeException(e);
         }
      });
      server.start();
      String base = "http://127.0.0.1:" + server.getAddress().getPort();
      HttpClient client = HttpClient.newHttpClient();
      try {
         HttpResponse<String> post = client.send(
            HttpRequest.newBuilder(URI.create(base + "/?code=x")).POST(HttpRequest.BodyPublishers.noBody()).build(), HttpResponse.BodyHandlers.ofString());
         check(post.statusCode() == 405, "POST gets 405, got " + post.statusCode());
         HttpResponse<String> favicon = client.send(HttpRequest.newBuilder(URI.create(base + "/favicon.ico")).build(), HttpResponse.BodyHandlers.ofString());
         check(favicon.statusCode() == 400, "GET without code gets 400, got " + favicon.statusCode());
         check(results.isEmpty(), "stray requests do not end the login");
         HttpResponse<String> denied = client.send(
            HttpRequest.newBuilder(URI.create(base + "/?error=access_denied&state=expected-state")).build(), HttpResponse.BodyHandlers.ofString());
         check(denied.statusCode() == 200 && denied.body().contains("Cannot authenticate"), "error param ends the login");
         check(results.size() == 1 && results.get(0) == null, "error param reports failure to the caller");
      } finally {
         server.stop(0);
      }
      System.out.println("PASS AuthCallbackCheck");
   }
}

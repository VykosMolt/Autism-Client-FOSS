package autismclient.util;

import autismclient.compat263.Compat263;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ActiveTextCollector;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.TextAlignment;
import net.minecraft.client.gui.ActiveTextCollector.Parameters;
import net.minecraft.client.gui.components.SplashRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.resources.IoSupplier;
import net.minecraft.util.Mth;
import net.minecraft.util.Util;
import org.joml.Matrix3x2f;

public final class CustomSplashProvider {
   private static final Random RANDOM = new Random();
   private static final int init_I = -256;
   private static List<String> cachedLines;
   private static Component cachedSplash;

   private CustomSplashProvider() {
   }

   public static SplashRenderer splashRenderer(Minecraft client) {
      List<String> lines = load(client);
      if (lines.isEmpty()) {
         return null;
      }

      String text = lines.get(RANDOM.nextInt(lines.size()));
      return new SplashRenderer(Component.literal(text).setStyle(Style.EMPTY.withColor(-256)));
   }

   public static List<Component> splashLines(Minecraft client) {
      return load(client).stream().map(text -> (Component)Component.literal(text).setStyle(Style.EMPTY.withColor(-256))).toList();
   }

   public static void render(Minecraft client, GuiGraphicsExtractor graphics, int screenWidth, Font font, float alpha) {
      Component splash = init_Component(client);
      if (splash != null) {
         int textWidth = font.width(splash);
         ActiveTextCollector textRenderer = graphics.textRenderer();
         float textPhase = 1.8F - Mth.abs(Mth.sin((float)(Util.getMillis() % 1000L) / 1000.0F * 6.2831855F) * 0.1F);
         float textScale = textPhase * 100.0F / (textWidth + 32);
         Matrix3x2f transform = new Matrix3x2f(textRenderer.defaultParameters().pose())
            .translate(screenWidth / 2.0F + 123.0F, 69.0F)
            .rotate(-0.34906584F)
            .scale(textScale);
         Parameters renderParameters = textRenderer.defaultParameters().withOpacity(alpha).withPose(transform);
         textRenderer.accept(TextAlignment.LEFT, -textWidth / 2, -8, renderParameters, splash);
      }
   }

   private static Component init_Component(Minecraft client) {
      if (cachedSplash != null) {
         return cachedSplash;
      }

      List<String> lines = load(client);
      if (lines.isEmpty()) {
         return null;
      }

      String text = lines.get(RANDOM.nextInt(lines.size()));
      cachedSplash = Component.literal(text).setStyle(Style.EMPTY.withColor(-256));
      return cachedSplash;
   }

   private static List<String> load(Minecraft client) {
      if (cachedLines != null) {
         return cachedLines;
      }

      List<String> lines = new ArrayList<>();

      try {
         IoSupplier<InputStream> supplier = Compat263.vanillaGetResource(
            client.getVanillaPackResources(), PackType.CLIENT_RESOURCES, Identifier.withDefaultNamespace("texts/splashes.txt")
         );
         if (supplier != null) {
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(supplier.get(), StandardCharsets.UTF_8))) {
               reader.lines().map(String::trim).filter(line -> !line.isEmpty() && line.hashCode() != 125780783).forEach(lines::add);
            }
         }
      } catch (Exception var8) {
      }

      cachedLines = List.copyOf(lines);
      return cachedLines;
   }
}

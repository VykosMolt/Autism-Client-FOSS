package autismclient.util.c;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Iterator;
import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.metadata.IIOMetadata;
import javax.imageio.stream.MemoryCacheImageInputStream;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public final class DataUtil {
   private static final int init_I = 67108864;
   private static final int load = 4096;
   private static final int read = 1000;
   private static final int write = 2048;
   private static final long init_J = 64000000L;
   private static final int update = 100;
   private static final String init_Str = "javax_imageio_gif_image_1.0";

   private DataUtil() {
   }

   public static DataUtil.Inner_DataUtil init_DataUtil_DataUtil(byte[] bytes) throws DataUtil.Inner_CachePool {
      if (bytes != null && bytes.length != 0) {
         if (bytes.length > 67108864) {
            throw new DataUtil.Inner_CachePool("file too large (max 25 MB)");
         } else if (init_Z(bytes)) {
            return read_DataUtil_DataUtil(bytes);
         } else if (!load_Z(bytes) && !read_Z(bytes)) {
            throw new DataUtil.Inner_CachePool("unsupported image format");
         } else {
            return load_DataUtil_DataUtil(bytes);
         }
      } else {
         throw new DataUtil.Inner_CachePool("no image data");
      }
   }

   private static boolean init_Z(byte[] b) {
      return b.length >= 6 && b[0] == 71 && b[1] == 73 && b[2] == 70 && b[3] == 56 && (b[4] == 55 || b[4] == 57) && b[5] == 97;
   }

   private static boolean load_Z(byte[] b) {
      return b.length >= 4 && (b[0] & 255) == 137 && b[1] == 80 && b[2] == 78 && b[3] == 71;
   }

   private static boolean read_Z(byte[] b) {
      return b.length >= 2 && (b[0] & 255) == 255 && (b[1] & 255) == 216;
   }

   private static DataUtil.Inner_DataUtil load_DataUtil_DataUtil(byte[] bytes) throws DataUtil.Inner_CachePool {
      if (load_Z(bytes) && bytes.length >= 24) {
         int w = load(bytes, 16);
         int h = load(bytes, 20);
         if (w > 0 && h > 0) {
            init(w, h);
         }
      }

      BufferedImage image;
      try {
         image = ImageIO.read(new ByteArrayInputStream(bytes));
      } catch (IOException | RuntimeException e) {
         throw new DataUtil.Inner_CachePool("could not decode image: " + init(e));
      }

      if (image == null) {
         throw new DataUtil.Inner_CachePool("could not decode image");
      } else {
         int w = image.getWidth();
         int h = image.getHeight();
         init(w, h);
         double scale = init(w, h, 1);
         if (scale < 1.0) {
            int outW = Math.max(1, (int)Math.round(w * scale));
            int outH = Math.max(1, (int)Math.round(h * scale));
            BufferedImage scaled = new BufferedImage(outW, outH, 2);
            Graphics2D g = scaled.createGraphics();
            g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            g.drawImage(image, 0, 0, outW, outH, null);
            g.dispose();
            int[] scaledArgb = new int[outW * outH];
            scaled.getRGB(0, 0, outW, outH, scaledArgb, 0, outW);
            return new DataUtil.Inner_DataUtil(outW, outH, new int[][]{scaledArgb}, new int[]{100}, 100);
         } else {
            int[] argb = new int[w * h];
            image.getRGB(0, 0, w, h, argb, 0, w);
            return new DataUtil.Inner_DataUtil(w, h, new int[][]{argb}, new int[]{100}, 100);
         }
      }
   }

   private static double init(int w, int h, int frames) {
      double scale = 1.0;
      int maxSide = Math.max(w, h);
      if (maxSide > 2048) {
         scale = 2048.0 / maxSide;
      }

      long src = (long)w * h * Math.max(1, frames);
      if (src > 64000000L) {
         scale = Math.min(scale, Math.sqrt(6.4E7 / src));
      }

      return scale;
   }

   private static DataUtil.Inner_DataUtil read_DataUtil_DataUtil(byte[] bytes) throws DataUtil.Inner_CachePool {
      if (bytes.length < 13) {
         throw new DataUtil.Inner_CachePool("corrupt GIF header");
      }

      int screenW = init(bytes, 6);
      int screenH = init(bytes, 8);
      if (screenW > 0 && screenH > 0) {
         init(screenW, screenH);
         ImageReader reader = null;
         MemoryCacheImageInputStream input = new MemoryCacheImageInputStream(new ByteArrayInputStream(bytes));

         try {
            Iterator<ImageReader> readers = ImageIO.getImageReadersByFormatName("gif");
            if (!readers.hasNext()) {
               throw new DataUtil.Inner_CachePool("no GIF decoder available");
            }

            reader = readers.next();
            reader.setInput(input);
            int frames = reader.getNumImages(true);
            if (frames <= 0) {
               throw new DataUtil.Inner_CachePool("GIF has no frames");
            }

            if (frames > 1000) {
               throw new DataUtil.Inner_CachePool("too many frames (max 1000)");
            }

            int outW = screenW;
            int outH = screenH;
            double scale = init(screenW, screenH, frames);
            if (scale < 1.0) {
               outW = Math.max(1, (int)Math.round(screenW * scale));
               outH = Math.max(1, (int)Math.round(screenH * scale));
            }

            boolean downscaling = outW != screenW || outH != screenH;
            int[][] outFrames = new int[frames][];
            int[] delays = new int[frames];
            BufferedImage canvas = new BufferedImage(screenW, screenH, 2);
            Graphics2D graphics = canvas.createGraphics();
            BufferedImage outCanvas = downscaling ? new BufferedImage(outW, outH, 2) : null;
            Graphics2D outGraphics = null;
            if (downscaling) {
               outGraphics = outCanvas.createGraphics();
               outGraphics.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            }

            try {
               graphics.setBackground(new Color(0, 0, 0, 0));

               for (int i = 0; i < frames; i++) {
                  DataUtil.Inner_StreamUtil info = init(reader.getImageMetadata(i), screenW, screenH);
                  int[] previous = info.load_Z ? canvas.getRGB(0, 0, screenW, screenH, null, 0, screenW) : null;
                  BufferedImage sub = reader.read(i);
                  if (sub != null) {
                     graphics.drawImage(sub, info.init_I, info.load_I, null);
                  }

                  int[] argb;
                  if (downscaling) {
                     outGraphics.setComposite(AlphaComposite.Src);
                     outGraphics.drawImage(canvas, 0, 0, outW, outH, null);
                     argb = new int[outW * outH];
                     outCanvas.getRGB(0, 0, outW, outH, argb, 0, outW);
                  } else {
                     argb = new int[screenW * screenH];
                     canvas.getRGB(0, 0, screenW, screenH, argb, 0, screenW);
                  }

                  outFrames[i] = argb;
                  delays[i] = info.update;
                  if (info.init_Z) {
                     graphics.clearRect(info.init_I, info.load_I, info.read, info.write);
                  } else if (info.load_Z) {
                     canvas.setRGB(0, 0, screenW, screenH, previous, 0, screenW);
                  }
               }
            } finally {
               graphics.dispose();
               if (outGraphics != null) {
                  outGraphics.dispose();
               }
            }

            long total = 0L;

            for (int delay : delays) {
               total += delay;
            }

            int average = Math.max(1, (int)Math.round((double)total / delays.length));
            return new DataUtil.Inner_DataUtil(outW, outH, outFrames, delays, average);
         } catch (DataUtil.Inner_CachePool e) {
            throw e;
         } catch (IOException | RuntimeException e) {
            throw new DataUtil.Inner_CachePool("could not decode GIF: " + init(e));
         } finally {
            if (reader != null) {
               reader.dispose();
            }

            try {
               input.close();
            } catch (IOException var37) {
            }
         }
      } else {
         throw new DataUtil.Inner_CachePool("corrupt GIF header");
      }
   }

   private static DataUtil.Inner_StreamUtil init(IIOMetadata metadata, int screenW, int screenH) throws DataUtil.Inner_CachePool {
      DataUtil.Inner_StreamUtil info = new DataUtil.Inner_StreamUtil();
      Node root = null;
      if (metadata != null) {
         try {
            root = metadata.getAsTree("javax_imageio_gif_image_1.0");
         } catch (RuntimeException e) {
            root = null;
         }
      }

      if (root != null) {
         NodeList children = root.getChildNodes();

         for (int n = 0; n < children.getLength(); n++) {
            Node node = children.item(n);
            String name = node.getNodeName();
            if ("GraphicControlExtension".equals(name)) {
               int centiseconds = init(init(node, "delayTime"), -1);
               info.update = centiseconds > 0 ? centiseconds * 10 : 100;
               String disposal = init(node, "disposalMethod");
               if ("restoreToBackgroundColor".equals(disposal)) {
                  info.init_Z = true;
               } else if ("restoreToPrevious".equals(disposal)) {
                  info.load_Z = true;
               }
            } else if ("ImageDescriptor".equals(name)) {
               info.init_I = init(init(node, "imageLeftPosition"), 0);
               info.load_I = init(init(node, "imageTopPosition"), 0);
               info.read = init(init(node, "imageWidth"), screenW);
               info.write = init(init(node, "imageHeight"), screenH);
            }
         }
      }

      if (info.read <= 0) {
         info.read = screenW;
      }

      if (info.write <= 0) {
         info.write = screenH;
      }

      if (info.read <= 4096 && info.write <= 4096) {
         info.init_I = Math.max(0, Math.min(info.init_I, screenW));
         info.load_I = Math.max(0, Math.min(info.load_I, screenH));
         info.read = Math.max(0, Math.min(info.read, screenW - info.init_I));
         info.write = Math.max(0, Math.min(info.write, screenH - info.load_I));
         return info;
      } else {
         throw new DataUtil.Inner_CachePool("GIF frame too large (max 4096×4096)");
      }
   }

   private static String init(Node node, String name) {
      Node attribute = node.getAttributes() == null ? null : node.getAttributes().getNamedItem(name);
      return attribute == null ? null : attribute.getNodeValue();
   }

   private static int init(String value, int fallback) {
      if (value == null) {
         return fallback;
      }

      try {
         return Integer.parseInt(value.trim());
      } catch (NumberFormatException e) {
         return fallback;
      }
   }

   private static void init(int w, int h) throws DataUtil.Inner_CachePool {
      if (w <= 0 || h <= 0) {
         throw new DataUtil.Inner_CachePool("corrupt image (bad dimensions)");
      } else if (w > 4096 || h > 4096) {
         throw new DataUtil.Inner_CachePool("image too large (max 4096×4096)");
      }
   }

   private static int init(byte[] b, int offset) {
      return b[offset] & 0xFF | (b[offset + 1] & 0xFF) << 8;
   }

   private static int load(byte[] b, int offset) {
      return (b[offset] & 0xFF) << 24 | (b[offset + 1] & 0xFF) << 16 | (b[offset + 2] & 0xFF) << 8 | b[offset + 3] & 0xFF;
   }

   private static String init(Throwable t) {
      String message = t.getMessage();
      if (message != null && !message.isBlank()) {
         message = message.replace('\n', ' ').trim();
         return message.length() > 120 ? message.substring(0, 120) : message;
      } else {
         return t.getClass().getSimpleName();
      }
   }

   public static final class Inner_CachePool extends Exception {
      public Inner_CachePool(String message) {
         super(message);
      }
   }

   public record Inner_DataUtil(int init_I, int load, int[][] init_IArr2, int[] init_IArr, int read) {
   }

   private static final class Inner_StreamUtil {
      int init_I;
      int load_I;
      int read;
      int write;
      int update = 100;
      boolean init_Z;
      boolean load_Z;
   }
}

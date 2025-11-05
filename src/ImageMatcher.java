import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.awt.Graphics2D;
import java.io.File;

public class ImageMatcher {
    public static BufferedImage load(String path) throws Exception {
        return ImageIO.read(new File(path));
    
    }
   
    public static BufferedImage toGray(BufferedImage img) {
        BufferedImage g = new BufferedImage(img.getWidth(), img.getHeight(), BufferedImage.TYPE_BYTE_GRAY);
        Graphics2D gr = g.createGraphics();
        gr.drawImage(img, 0, 0, null); gr.dispose();
        return g;
    }
    public static BufferedImage resize(BufferedImage src, int w, int h) {
        BufferedImage dst = new BufferedImage(w, h, BufferedImage.TYPE_BYTE_GRAY);
        Graphics2D g2 = dst.createGraphics();
        g2.drawImage(src, 0, 0, w, h, null); g2.dispose();
        return dst;
    }
    public static double mse(BufferedImage a, BufferedImage b) {
        long sum = 0; int w = a.getWidth(), h = a.getHeight();
        for(int y=0; y<h; ++y) for(int x=0; x<w; ++x) {
            int pa = (a.getRGB(x,y)>>16)&0xff, pb = (b.getRGB(x,y)>>16)&0xff, diff = pa-pb;
            sum += diff*diff;
        }
        return (double)sum/(w*h);
    }
    public static double distance(String f1, String f2, int w, int h) throws Exception {
    	BufferedImage i1 = resize(toGray(load(f1)),w,h), i2 = resize(toGray(load(f2)),w,h);
        return mse(i1,i2);
    }
}



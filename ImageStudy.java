import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import java.io.IOException;
import java.io.File;

public class ImageStudy {
  private BufferedImage img;

  public ImageStudy(String imgFile) {
    try {
      this.img = ImageIO.read(new File(imgFile));
    } catch(IOException e) {
      System.err.println("error read image file " + imgFile + " " + e.getMessage());
     }
  }

  public void toGrayScale() {
    int width = img.getWidth();
    int height = img.getHeight();
    for (int x = 0; x < width; ++x) {
      for (int y = 0; y < height; ++y) {
        int pxcl = img.getRGB(x, y);
        int gray = (int)getLightness(pxcl);
        img.setRGB(x, y, toRGB(0, gray, gray, gray));
      }
    }
  }

  // preprocess of Fourier Transform
  public void toSquareImage() {
    int x = img.getWidth();
    int y = img.getHeight();
    int size;
    if (x < y) size = y;
    else size = x;
    int tmpsize = 2;
    while(size > tmpsize) {
      tmpsize *= 2;
    }
    size = tmpsize;
    BufferedImage newimg = new BufferedImage(size, size, BufferedImage.TYPE_INT_RGB);
    //newimg.setData(this.img.getData());
    copy(this.img, newimg);
    this.img = newimg;
  }


  public void write(String file) {
    try {
      ImageIO.write(img, "jpg",new File(file));
    } catch(IOException e) {
      System.err.println("error write image file " + file + " " + e.getMessage());
    }
  }

  // util function
  public static int getA(int rgb) { return rgb >> 24; }
  public static int getR(int rgb) { return (rgb >> 16)&0xff; }
  public static int getG(int rgb) { return (rgb >> 8)&0xff; }
  public static int getB(int rgb) { return rgb&0xff; }
  public static int toRGB(int a, int r, int g, int b) {
    return (a<<24)|(r<<16)|(b<<8)|b;
  }

  public static void copy(BufferedImage source, BufferedImage dest) {
    int x1 = source.getWidth();
    int x2 = dest.getWidth();
    int y1 = source.getHeight();
    int y2 = dest.getHeight();
    int width = ( x1 < x2 )? x1:x2;
    int height = ( y1 < y2 )? y1:y2;
    for(int y=0; y<height; ++y) {
      for(int x=0; x<width; ++x) {
        dest.setRGB(x, y, source.getRGB(x, y));
      }
    }
  }

  public static float getLightness(int r, int g, int b) {
    return 0.299f * (float)r 
            + 0.587f * (float)g 
            + 0.114f * (float)b;
  }
  public static float getLightness(int rgb) {
    return 0.299f * (float)getR(rgb) 
            + 0.587f * (float)getG(rgb) 
            + 0.114f * (float)getB(rgb);
  }

  public static void main(String[] args) {
    ImageStudy is = new ImageStudy(args[0]);
    is.toSquareImage();
    is.write(args[1]);
  }
}

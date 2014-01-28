import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import java.io.IOException;
import java.io.File;
import java.util.Arrays;

public class ImageStudy {
  private BufferedImage img;
  private float[] _workingCopy;
  private int _width, _height;
  private float[] ft_real;
  private float[] ft_img;

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
  public void to2PowerImage() {
    int ori_width = img.getWidth();
    int ori_height = img.getHeight();
    int tmpsize = 2;
    while(ori_width > tmpsize) {
      tmpsize *= 2;
    }
    this._width = tmpsize;
    tmpsize = 2;
    while(ori_height > tmpsize) {
      tmpsize *= 2;
    }
    this._height = tmpsize;
    this._workingCopy = new float[_width * _height];
    Arrays.fill(_workingCopy, 0.0f);
    for (int y=0; y < ori_height; ++y) {
      for (int x=0; x < ori_width; ++x) {
        setPixcel(x, y, getLightness(img.getRGB(x, y)));
      }
    }
  }

  // prepare
  public void prepareFourierTransform() {
    ft_real = Arrays.copyOf(_workingCopy, _workingCopy.length);
    ft_img = new float[_width * _height];
    Arrays.fill(ft_img, 0.0f);
  }
  public void correct(int num) {
    float factor = 1.0f / (float)num;
    for (int i=0; i<ft_real.length; ++i) {
      ft_real[i] *= factor;
      ft_img[i] *= factor;
    }
  }

  public void fourierTransform() {
    to2PowerImage();
    prepareFourierTransform();
    xFourierTransform(false);
    correct(_width);
    yFourierTransform(false);
    correct(_height);
  }
  public void inverseFourierTransform() {
    xFourierTransform(true);
    yFourierTransform(true);
  }
  public void xDirectionTest() {
    to2PowerImage();
    prepareFourierTransform();
    xFourierTransform(false);
    correct(_width);
    xFourierTransform(true);
  }
  public void yDirectionTest() {
    to2PowerImage();
    prepareFourierTransform();
    yFourierTransform(false);
    correct(_height);
    yFourierTransform(true);
  }

  // x-direction Fourier Transform
  public void xFourierTransform(boolean inverse) {
    float inv_factor = (inverse)? 1.0f:-1.0f;
    float[] new_ft_real = new float[_width * _height];
    float[] new_ft_img = new float[_width * _height];

    float omega = (2.0f * (float)Math.PI) / (float)_width;

    // prepare working memory
    float[] ft_working_real = new float[_width / 2];
    float[] ft_working_img = new float[_width / 2];
    for (int i = 0; i < _width / 2; ++i) {
      ft_working_real[i] = (float)Math.cos(omega * (float)i);
      ft_working_img[i] = (float)Math.sin(omega * (float)i);
    }

    // Fourier Transform calculation
    int length = _width / 2;
    for (int y=0; y<_height; ++y) {
      for (int k=0; k<_width; ++k) {
        setFloat(new_ft_real, k, y, 0.0f);
        setFloat(new_ft_img, k, y, 0.0f);
        for (int x=0; x<_width; ++x) {
          int index = (k * x) % _width;
          float real_factor, img_factor;
          if (index < length) {
            real_factor = ft_working_real[index];
            img_factor = inv_factor * ft_working_img[index];
          } else {
            index -= length;
            real_factor = -ft_working_real[index];
            img_factor = -ft_working_img[index] * inv_factor;
          }
          float real = getFloat(ft_real, x, y);
          float img = getFloat(ft_img, x, y);
          // real x real - img x img
          addFloat(new_ft_real, k, y, ((real * real_factor) - (img * img_factor)));
          // real x img + img x real
          addFloat(new_ft_img, k, y, ((real * img_factor) + (real_factor * img)));
        }
      }
    }
    // replace new coefficients
    ft_real = new_ft_real;
    ft_img = new_ft_img;
  }

  // y-direction Fourier Transform
  public void yFourierTransform(boolean inverse) {
    float inv_factor = (inverse)? 1.0f:-1.0f;
    float[] new_ft_real = new float[_width * _height];
    float[] new_ft_img = new float[_width * _height];

    float omega = (2.0f * (float)Math.PI) / (float)_height;

    // prepare working memory
    float[] ft_working_real = new float[_height / 2];
    float[] ft_working_img = new float[_height / 2];
    for (int i = 0; i < _height / 2; ++i) {
      ft_working_real[i] = (float)Math.cos(omega * (float)i);
      ft_working_img[i] = (float)Math.sin(omega * (float)i);
    }

    // Fourier Transform calculation
    int length = _height / 2;
    for (int x=0; x<_width; ++x) {
      for (int k=0; k<_height; ++k) {
        setFloat(new_ft_real, x, k, 0.0f);
        setFloat(new_ft_img, x, k, 0.0f);
        for (int y=0; y<_height; ++y) {
          int index = (k * y) % _height;
          float real_factor, img_factor;
          if (index < length) {
            real_factor = ft_working_real[index];
            img_factor = ft_working_img[index] * inv_factor;
          } else {
            index -= length;
            real_factor = -ft_working_real[index];
            img_factor = -ft_working_img[index] * inv_factor;
          }
          float real = getFloat(ft_real, x, y);
          float img = getFloat(ft_img, x, y);
          // real x real - img x img
          addFloat(new_ft_real, x, k, ((real * real_factor) - (img * img_factor)));
          // real x img + img x real
          addFloat(new_ft_img, x, k, ((real * img_factor) + (real_factor * img)));
        }
      }
    }
    // replace new coefficients
    ft_real = new_ft_real;
    ft_img = new_ft_img;
  }

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

  public void writeWorkingCopy(String file) {
    BufferedImage newimg = new BufferedImage(_width, _height, BufferedImage.TYPE_INT_RGB);
    for (int x=0; x<_width; ++x) {
      for (int y=0; y<_height; ++y) {
        int val = (int)getPixcel(x, y);
        newimg.setRGB(x, y, toRGB(0, val, val, val));
      }
    }
    try {
      ImageIO.write(newimg, "jpg",new File(file));
    } catch(IOException e) {
      System.err.println("error write image file " + file + " " + e.getMessage());
    }
  }

  public void writeWorkingCopy(float[] array, String file) {
    BufferedImage newimg = new BufferedImage(_width, _height, BufferedImage.TYPE_INT_RGB);
    for (int x=0; x<_width; ++x) {
      for (int y=0; y<_height; ++y) {
        int val = (int)getFloat(array, x, y);
        newimg.setRGB(x, y, toRGB(0, val, val, val));
      }
    }
    try {
      ImageIO.write(newimg, "jpg",new File(file));
    } catch(IOException e) {
      System.err.println("error write image file " + file + " " + e.getMessage());
    }
  }

  public void outputFourierReal(String file) {
    writeWorkingCopy(ft_real, file);
  }
  public void outputFourierImg(String file) {
    writeWorkingCopy(ft_img, file);
  }

  public void setPixcel(int x, int y, float val) {
    this._workingCopy[ (this._width * y) + x ] = val;
  }
  public float getPixcel(int x, int y) {
    return this._workingCopy[ (this._width * y) + x ];
  }

  public void setFloat(float[] array, int x, int y, float val) {
    array[ (this._width * y) + x ] = val;
  }
  public void addFloat(float[] array, int x, int y, float val) {
    array[ (this._width * y) + x ] += val;
  }
  public float getFloat(float[] array, int x, int y) {
    return array[ (this._width * y) + x ];
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
    //is.to2PowerImage();
    //is.writeWorkingCopy(args[1]);

    is.fourierTransform();
    is.inverseFourierTransform();
    //is.yDirectionTest();
    is.outputFourierReal(args[1]);
  }
}

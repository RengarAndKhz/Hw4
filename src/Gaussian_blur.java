/**
 * Created by Tianyang on 2016/3/2.
 */

import ij.ImagePlus;
import ij.plugin.filter.PlugInFilter;
import ij.process.ImageProcessor;

import java.awt.*;

public class Gaussian_Blur implements PlugInFilter {
    final static double sigma = 1;
    final static double radius = sigma * 3;

    @Override
    public int setup(String s, ImagePlus imagePlus) {
        return DOES_8G;
    }

    @Override
    public void run(ImageProcessor imageProcessor) {
        ImageProcessor i = imageProcessor.convertToFloat();
        ImageProcessor copy = i.duplicate();
        float[] h = makeGaussKernel1d(sigma);
        convolveFloat1D(copy, h, 1, h.length);
        convolveFloat1D(copy, h, h.length, 1);
        imageProcessor.insert(copy.convertToByte(false), 0, 0);

    }

    public float[] makeGaussKernel1d(double sigma) {
        int center = (int) (3.0 * sigma);
        float[] kernel = new float[2 * center + 1];
        double sigma2 = sigma * sigma;
        for (int i = 0; i < kernel.length; i++) {
            double r = center - i;
            kernel[i] = (float) Math.exp(-.05 * (r * r) / sigma2);
        }
        return kernel;
    }

    public float getPixel(int x, int y, float[] pixels, int width, int height) {
        if (x <= 0) {
            x = 0;
        }
        if (x >= width) {
            x = width - 1;
        }
        if (y <= 0) {
            y = 0;
        }
        if (y >= height) {
            y = height - 1;
        }
        return pixels[x + y * width];
    }

    void convolveFloat1D(ImageProcessor ip, float[] kernel, int kw, int kh) {
        int width = ip.getWidth();
        int height = ip.getHeight();
        Rectangle r = ip.getRoi();
        int x1 = r.x;
        int y1 = r.y;
        int x2 = x1 + r.width;
        int y2 = y1 + r.height;
        int uc = kw / 2;
        int vc = kh / 2;
        float[] pixels = (float[]) ((float[]) ip.getPixels());
        float[] pixels2 = (float[]) ((float[]) ip.getSnapshotPixels());
        if (pixels2 == null) {
            pixels2 = (float[]) ((float[]) ip.getPixelsCopy());
        }

        double scale = this.getScale(kernel);
        boolean vertical = kw == 1;
        int xedge = width - uc;
        int yedge = height - vc;

        for (int y = y1; y < y2; ++y) {
            for (int x = x1; x < x2; ++x) {
                double sum = 0.0D;
                int i = 0;
                int offset;
                boolean edgePixel;
                int u;
                if (vertical) {
                    edgePixel = y < vc || y >= yedge;
                    offset = x + (y - vc) * width;

                    for (u = -vc; u <= vc; ++u) {
                        if (edgePixel) {
                            sum += (double) (this.getPixel(x + uc, y + u, pixels2, width, height) * kernel[i++]);
                        } else {
                            sum += (double) (pixels2[offset + uc] * kernel[i++]);
                        }

                        offset += width;
                    }
                } else {
                    edgePixel = x < uc || x >= xedge;
                    offset = x + (y - vc) * width;
                    for (u = -uc; u <= uc; ++u) {
                        if (edgePixel) {
                            sum += (double) (this.getPixel(x + u, y + vc, pixels2, width, height) * kernel[i++]);
                        } else {
                            sum += (double) (pixels2[offset + u] * kernel[i++]);
                        }
                    }
                }

                pixels[x + y * width] = (float) (sum * scale);
            }
        }

    }

    double getScale(float[] kernel) {
        double scale = 1.0D;
        double sum = 0.0D;

        for (int i = 0; i < kernel.length; ++i) {
            sum += (double) kernel[i];
        }

        if (sum != 0.0D) {
            scale = (double) ((float) (1.0D / sum));
        }
        return scale;
    }
}

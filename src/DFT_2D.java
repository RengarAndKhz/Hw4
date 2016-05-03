import ij.ImagePlus;
import ij.plugin.filter.PlugInFilter;
import ij.process.FloatProcessor;
import ij.process.ImageProcessor;


/**
 * Created by Tianyang on 5/3/2016.
 */
public class DFT_2D implements PlugInFilter {
    @Override
    public int setup(String s, ImagePlus imagePlus) {
        return DOES_ALL;
    }

    @Override
    public void run(ImageProcessor imageProcessor) {

        FloatProcessor floatProcessor = (FloatProcessor) imageProcessor.convertToFloat();

        //Map<Pair<Integer, Integer>, Double> map = new HashMap<Pair<Integer, Integer>, Double>();
        int w = floatProcessor.getWidth();
        int h = floatProcessor.getHeight();
        Complex[][] g = new Complex[h][w];
        FloatProcessor gProcessor = new FloatProcessor(w,h);
        FloatProcessor inverseProcessor = new FloatProcessor(w,h);

        //double[] rowDFT = new double[w];
        //d-dim dft of col
        for (int m = 0; m < w; m++){
            Complex[] temp = DFT(getColumn(floatProcessor, m), true);
            for (int n = 0; n < h; n++){
                g[n][m] = temp[n];

            }
        }
        //1-dim dft of row
        for (int n = 0; n < h; n++){
            Complex[] temp = DFT(g[n], true);
            float[] f = displayToFloat(temp);
            for (int m = 0; m < w; m++){
                gProcessor.putPixelValue((m+w/2)%w, (n+h/2)%h, Math.log(1 + f[m]));
            }
        }
        new ImagePlus("result", gProcessor).show();

        // trying to reverse
        for (int m = 0; m < w; m++){
            Complex[] temp = DFT(getColumn(g, m), false);
            for (int n = 0; n < h; n++){
                g[n][m] = temp[n];
            }
        }
        for (int n = 0; n < h; n++){
            Complex[] temp = DFT(g[n], false);
            float[] f = displayToFloat(temp);
            for (int m = 0; m < w; m++){
                inverseProcessor.putPixelValue(m,n,f[m]);
            }
        }
        new ImagePlus("inverse", inverseProcessor).show();
    }

    private Complex[] getColumn(FloatProcessor fp, int x) {
        Complex[] val = new Complex[fp.getHeight()];
        for(int y = 0; y < fp.getHeight(); y++){
            val[y] = new Complex(fp.getPixel(x, y), 0);
        }

        return val;
    }
    private Complex[] getColumn(Complex[][] g, int x) {
        Complex[] val = new Complex[g[0].length];
        for(int y = 0; y < g.length; y++){
            val[y] = g[y][x];
        }
        return val;
    }
    private float[] displayToFloat(Complex[] c) {
        float[] f = new float[c.length];
        for(int i = 0; i < c.length; i++){
            f[i] = (float) Math.sqrt(c[i].re*c[i].re + c[i].im*c[i].im);
        }
        return f;
    }


    /**
     * dft function from book
     * @param g
     * @param forward
     * @return
     */
    public Complex[] DFT(Complex[] g, boolean forward){
        int M = g.length;
        double s = 1/ Math.sqrt(M);
        Complex[] G = new Complex[M];

        for(int m = 0; m < M; m++){
            double sumRe = 0;
            double sumIm = 0;
            double phim = 2* Math.PI * m/M;

            for(int u = 0; u < M; u++){
                double gRe = g[u].re;
                double gIm = g[u].im;
                double cosw = Math.cos(phim * u);
                double sinw = Math.sin(phim * u);
                if(!forward) sinw *= -1;
                sumRe += gRe * cosw + gIm * sinw;
                sumIm += gIm * cosw - gRe * sinw;
            }
            G[m] = new Complex(s * sumRe, s * sumIm);
        }
        return G;
    }
}

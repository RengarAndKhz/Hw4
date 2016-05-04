import ij.ImagePlus;
import ij.plugin.filter.PlugInFilter;
import ij.process.FloatProcessor;
import ij.process.ImageProcessor;
import ij.plugin.FFT;
import ij.plugin.filter.GaussianBlur;
import ij.plugin.filter.FFTFilter;

/**
 * Created by Tianyang on 5/3/16.
 */
public class Motion_Deblur implements PlugInFilter {
    @Override
    public int setup(String s, ImagePlus imagePlus) {
        return DOES_ALL;
    }

    @Override
    public void run(ImageProcessor imageProcessor) {
        GaussianBlur gaussianBlur = new GaussianBlur();
        float[][] kernel = gaussianBlur.makeGaussianKernel(3,0.001, 5);
        gaussianBlur.blurGaussian(imageProcessor, 5, 5, 0.001);
        FloatProcessor floatProcessor = (FloatProcessor) imageProcessor.convertToFloat().duplicate();
        Complex[][] gImage = new Complex[floatProcessor.getHeight()][floatProcessor.getWidth()];
        Complex[][] gKernel = new Complex[kernel.length][kernel[0].length];

        //String lol = FFT.fileName;

        FFT fft = new FFT();
        fft.run("coins.png");
    }
}

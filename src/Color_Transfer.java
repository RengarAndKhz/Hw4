import ij.ImagePlus;
import ij.WindowManager;
import ij.plugin.filter.Convolver;
import ij.plugin.filter.PlugInFilter;
import ij.process.ColorProcessor;
import ij.process.ImageConverter;
import ij.process.ImageProcessor;
import javafx.util.Pair;

import java.awt.Color;
import java.util.DoubleSummaryStatistics;
import java.util.HashMap;
import java.util.Vector;

/**
 * Created by Tianyang on 4/29/2016.
 */
public class Color_Transfer implements PlugInFilter{
    final double[][] lmsTransferMatrix = new double[][]{{0.381, 0.5783, 0.402},{0.1967, 0.7244, 0.0782},{0.0241, 0.1288, 0.8444}};
    final double[][] lmsTolab1 = new double[][]{{1/Math.sqrt(3), 0.0, 0.0},{0.0, 1/Math.sqrt(6), 0.0}, {0.0, 0.0, 1/Math.sqrt(2)}};
    final int[][] lmsTolab2 = new int[][]{{1,1,1},{1,1,-1},{1,-1,0}};
    final int[][] labTolms1 = new int[][]{{1,1,1},{1,1,-1}, {1,-2,0}};
    final double[][] labTolms2 = new double[][]{{Math.sqrt(3)/3, 0, 0}, {0, Math.sqrt(6)/6, 0}, {0, 0, Math.sqrt(2)/2}};
    @Override
    public int setup(String s, ImagePlus imagePlus) {
        return DOES_ALL;
    }

    @Override
    public void run(ImageProcessor imageProcessor) {
        ImagePlus fullerImagePlus = WindowManager.getImage("fuller.jpg");
        ImagePlus vangoghImagePlus = WindowManager.getImage("vangogh.jpg");
        ColorProcessor fullerColorProcessor = (ColorProcessor) fullerImagePlus.getProcessor();
        ColorProcessor vangoghColorProcessor = (ColorProcessor) vangoghImagePlus.getProcessor();
        //convertToLab(fullerImagePlus).show();



    }

    public HashMap<Pair<Integer,Integer>, double[]> convertToLab(ImagePlus imagePlus){
        HashMap<Pair<Integer, Integer>, double[]> resultMap = new HashMap<Pair<Integer, Integer>, double[]>();
        ColorProcessor colorProcessor = (ColorProcessor) imagePlus.getProcessor();
        ColorProcessor resultProcessor = (ColorProcessor) colorProcessor.duplicate();
        Pair<Integer, Integer> coordinate;
        for (int x = 0; x < colorProcessor.getWidth(); x++){
            for (int y = 0; y < colorProcessor.getHeight(); y++){
                coordinate = new Pair<Integer, Integer>(x,y);
                Color tempColor = colorProcessor.getColor(x,y);
                int red = tempColor.getRed();
                int green = tempColor.getGreen();
                int blue = tempColor.getBlue();
                int[] rgb = new int[]{red, green, blue};
                double[] lms = new double[3];
                for (int i = 0; i < lmsTransferMatrix.length; i++){
                    for (int j = 0; j < lmsTransferMatrix[0].length; j++){
                        lms[i] += lmsTransferMatrix[i][j]*rgb[j];
                    }
                }
                // scaling from 1 to 100
                for (int i = 0; i < 3; i++){
                    lms[i] = lms[i] * 99 / 255 + 1;
                    lms[i] = Math.log(lms[i]);
                }
                //transfer to lab
                double[] lab = new double[3];
                double[][] lmsTolab3 = new double[3][3];
                for (int i = 0; i < 3; i++){
                    for (int j = 0; j < 3; j++){
                        for (int k = 0; k < 3; k++){
                            lmsTolab3[i][j] += lmsTolab1[j][k]*lmsTolab2[k][j];
                        }
                    }
                }
                for (int i = 0; i < 3; i++){
                    for (int j = 0; j < 3; j++){
                        lab[i] += lmsTolab3[i][j]*lms[j];
                    }
                }
                resultMap.put(coordinate, lab);
            }
        }
        return resultMap;
    }

    /**
     * calculate the mean value of lab
     * @param sourceImage
     * @param sourceProcessor
     * @return
     */
    public double[] meanLab(HashMap<Pair<Integer, Integer>, double[]> sourceImage,ColorProcessor sourceProcessor){
        double[] meanOfLab = new double[3];
        for (int i = 0; i < sourceProcessor.getWidth(); i++){
            for (int j = 0; j < sourceProcessor.getHeight(); j++){
                for (int subIndex = 0; subIndex < 3; subIndex++){
                    meanOfLab[subIndex] += sourceImage.get(new Pair<>(i, j))[subIndex]/(sourceProcessor.getHeight()*sourceProcessor.getWidth());
                }
            }
        }
        return meanOfLab;
    }

    /**
     * calculate the standard deviation of lab
     * @param imageMap
     * @param colorProcessor
     * @param mean
     * @return
     */
    public double[] standardDeviation(HashMap<Pair<Integer, Integer>, double[]> imageMap, ColorProcessor colorProcessor, double[] mean ){
        double[] deviation = new double[3];
        double[] sqare = new double[3];
        int w = colorProcessor.getWidth();
        int h = colorProcessor.getHeight();
        for (int i = 0; i < w; i++){
            for (int j = 0; j < h; j++){
                for (int subIndex = 0; subIndex < 3; subIndex++){
                    sqare[subIndex] += Math.pow(imageMap.get(new Pair<>(i, j))[subIndex] - mean[subIndex], 2)/(w*h);
                }
            }
        }
        for (int index = 0; index < 3; index++){
            deviation[index] = Math.sqrt(sqare[index]);
        }
        return deviation;
    }

    /**
     * transfer color between target and source
     * @param sourceImage
     * @param targetImage
     * @param sourceProcessor
     * @param targetProcessor
     * @return
     */
    public HashMap<Pair<Integer, Integer>, double[]> transfer(HashMap<Pair<Integer, Integer>, double[]> sourceImage, HashMap<Pair<Integer, Integer>, double[]> targetImage, ColorProcessor sourceProcessor, ColorProcessor targetProcessor){
        double[] sourceMean = meanLab(sourceImage, sourceProcessor);
        double[] targetMean = meanLab(targetImage, targetProcessor);
        double[] sourceDeviation = standardDeviation(sourceImage, sourceProcessor, sourceMean);
        double[] targetDeviation = standardDeviation(targetImage, targetProcessor, targetMean);
        for (int i = 0; i < sourceProcessor.getWidth(); i++){
            for (int j = 0; j < sourceProcessor.getHeight(); j++){

            }
        }
    }


}

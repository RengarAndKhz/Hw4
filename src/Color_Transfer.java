import ij.ImagePlus;
import ij.WindowManager;
import ij.plugin.filter.PlugInFilter;
import ij.process.ColorProcessor;
import ij.process.ImageProcessor;
import javafx.util.Pair;
import sun.security.x509.RFC822Name;

import java.awt.Color;
import java.util.HashMap;

/**
 * Created by Tianyang on 4/29/2016.
 */
public class Color_Transfer implements PlugInFilter{
    final double[][] lmsTransferMatrix = new double[][]{{0.381, 0.5783, 0.0402},{0.1967, 0.7244, 0.0782},{0.0241, 0.1288, 0.8444}};
    final double[][] lmsTolab1 = new double[][]{{1/Math.sqrt(3), 0.0, 0.0},{0.0, 1/Math.sqrt(6), 0.0}, {0.0, 0.0, 1/Math.sqrt(2)}};
    final int[][] lmsTolab2 = new int[][]{{1,1,1},{1,1,-2},{1,-1,0}};
    final int[][] labTolms1 = new int[][]{{1,1,1},{1,1,-1}, {1,-2,0}};
    final double[][] labTolms2 = new double[][]{{Math.sqrt(3)/3, 0, 0}, {0, Math.sqrt(6)/6, 0}, {0, 0, Math.sqrt(2)/2}};
    final double[][] lmsToRGB = new double[][]{{4.4679, -3.5873, 0.1193}, {-1.2186, 2.3809, -0.1624}, {0.0497, -0.2439, 1.2045}};
    @Override
    public int setup(String s, ImagePlus imagePlus) {
        return DOES_ALL;
    }

    @Override
    public void run(ImageProcessor imageProcessor) {
        //ImagePlus fullerImagePlus = WindowManager.getImage("fuller.jpg");
        ImagePlus fullerImagePlus = WindowManager.getImage("Place_du_Forum.png");
        ImagePlus vangoghImagePlus = WindowManager.getImage("vangogh.jpg");
        ColorProcessor fullerColorProcessor = (ColorProcessor) fullerImagePlus.getProcessor();
        ColorProcessor vangoghColorProcessor = (ColorProcessor) vangoghImagePlus.getProcessor();
        ColorProcessor result = (ColorProcessor) fullerColorProcessor.duplicate();
        HashMap<Pair<Integer, Integer>, double[]> targetLab = convertToLab(fullerImagePlus);
        HashMap<Pair<Integer, Integer>, double[]> sourceLab = convertToLab(vangoghImagePlus);
        HashMap<Pair<Integer, Integer>, double[]> changedTargetLab = transferLab(sourceLab, targetLab, vangoghColorProcessor, fullerColorProcessor);
        HashMap<Pair<Integer, Integer>, int[]> resultRGB = LabToRGB(changedTargetLab, fullerColorProcessor);

        for (int i = 0; i < result.getWidth(); i++){
            for (int j = 0; j < result.getHeight(); j++){
                int[] RGB = resultRGB.get(new Pair<Integer, Integer>(i, j));
                result.putPixel(i,j,RGB);
            }
        }
        new ImagePlus("result", result).show();
    }

    public HashMap<Pair<Integer,Integer>, double[]> convertToLab(ImagePlus imagePlus){
        HashMap<Pair<Integer, Integer>, double[]> resultMap = new HashMap<Pair<Integer, Integer>, double[]>();
        ColorProcessor colorProcessor = (ColorProcessor) imagePlus.getProcessor();
        ColorProcessor resultProcessor = (ColorProcessor) colorProcessor.duplicate();
        Pair<Integer, Integer> coordinate;
        //transfer to lab

        double[][] lmsTolab3 = new double[3][3];
        for (int i = 0; i < 3; i++){
            for (int j = 0; j < 3; j++){
                for (int k = 0; k < 3; k++){
                    lmsTolab3[i][j] += lmsTolab1[i][k]*lmsTolab2[k][j];
                }
            }
        }
        for (int x = 0; x < colorProcessor.getWidth(); x++){
            for (int y = 0; y < colorProcessor.getHeight(); y++){
                coordinate = new Pair<Integer, Integer>(x,y);
                Color tempColor = colorProcessor.getColor(x,y);
                int red = tempColor.getRed();
                int green = tempColor.getGreen();
                int blue = tempColor.getBlue();
                int[] rgb = new int[]{red, green, blue};
                double[] lms = new double[3];
                for (int i = 0; i < 3; i++){
                    for (int j = 0; j < 3; j++){
                        lms[i] += lmsTransferMatrix[i][j]*rgb[j];
                    }
                }
                // scaling from 1 to 100
                for (int i = 0; i < 3; i++){
                    lms[i] = (lms[i]+1) * 100 / 256;
                    lms[i] = Math.log(lms[i]);
                }
                double[] lab = new double[3];
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
     * @param sourceImage lab of source image
     * @param sourceProcessor color processor of source image
     * @return
     */
    public double[] meanLab(HashMap<Pair<Integer, Integer>, double[]> sourceImage,ColorProcessor sourceProcessor){
        double[] meanOfLab = new double[3];
        int w = sourceProcessor.getWidth();
        int h = sourceProcessor.getHeight();
        for (int i = 0; i < sourceProcessor.getWidth(); i++){
            for (int j = 0; j < sourceProcessor.getHeight(); j++){
                for (int subIndex = 0; subIndex < 3; subIndex++){
                    meanOfLab[subIndex] += sourceImage.get(new Pair<>(i, j))[subIndex]/(h*w);
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
     * @param sourceImage this is the lab value of the image
     * @param targetImage this is the lab value of the image
     * @param sourceProcessor imageProcessor
     * @param targetProcessor imageProcessor
     * @return targetImage the lab value of the target image
     */
    public HashMap<Pair<Integer, Integer>, double[]> transferLab(HashMap<Pair<Integer, Integer>, double[]> sourceImage, HashMap<Pair<Integer, Integer>, double[]> targetImage, ColorProcessor sourceProcessor, ColorProcessor targetProcessor){
        double[] sourceMean = meanLab(sourceImage, sourceProcessor);
        double[] targetMean = meanLab(targetImage, targetProcessor);
        double[] sourceDeviation = standardDeviation(sourceImage, sourceProcessor, sourceMean);
        double[] targetDeviation = standardDeviation(targetImage, targetProcessor, targetMean);
        double k = 1/3;
        double r = 1;

        double[] ratioDeviation = new double[3];
        for (int index = 0; index < 3; index++){
            ratioDeviation[index] = targetDeviation[index] / sourceDeviation[index];
        }

        for (int i = 0; i < targetProcessor.getWidth(); i++){
            for (int j = 0; j < targetProcessor.getHeight(); j++){
                double[] tarLab = targetImage.get(new Pair<Integer, Integer>(i, j));
                for (int index = 0; index < 3; index++){
                    tarLab[index] -= targetMean[index];
                    tarLab[index] *= ratioDeviation[index];
                    tarLab[index] += r*(k*targetMean[index] + (1-k) * sourceMean[index]);
                }
                targetImage.put(new Pair<Integer, Integer>(i, j), tarLab);
            }
        }
        return targetImage;
    }

    /**
     * convert lab to rgb, on target image side
     * @param imageLab the changed lab of target
     * @param colorProcessor target image processor
     * @return a hashmap of rgb of the target image
     */
    public HashMap<Pair<Integer, Integer>, int[]> LabToRGB(HashMap<Pair<Integer, Integer>, double[]> imageLab, ColorProcessor colorProcessor){
        double[][] labToLMS = new double[3][3];

        HashMap<Pair<Integer, Integer>, int[]> imageRGB = new HashMap<Pair<Integer, Integer>, int[]>();

        for (int x = 0; x < 3; x++){
            for (int y = 0; y < 3; y++){
                for (int k = 0; k < 3; k++){
                    labToLMS[x][y] += labTolms1[x][k] * labTolms2[k][y];
                }
            }
        }
        //convert lab to lms
        for (int i = 0; i < colorProcessor.getWidth(); i++){
            for (int j = 0; j < colorProcessor.getHeight(); j++){
                double[] rgb = new double[3];
                double[] lms = new double[3];
                double[] lab = imageLab.get(new Pair<Integer, Integer>(i, j));
                for (int x = 0; x < 3; x++){
                    for (int y = 0; y < 3; y++){
                        lms[x] += labToLMS[x][y] * lab[y];
                    }
                }

                for (int index = 0; index < 3; index++){
                    lms[index] = Math.exp(lms[index]);
                    lms[index] = lms[index] * 256 / 100 -1;
                }
                //convert lms to RGB
                for (int x = 0; x < 3; x++){
                    for (int y = 0; y < 3; y++){
                        rgb[x] += lmsToRGB[x][y] * lms[y];
                    }
                }
                int[] rgbInt = new int[3];
                for (int index = 0; index < 3; index++){
                    rgbInt[index] = (int) Math.round(rgb[index]);
                }
                imageRGB.put(new Pair<Integer, Integer>(i,j), rgbInt);
            }
        }
        return imageRGB;
    }


}

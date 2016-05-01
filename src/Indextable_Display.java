import ij.ImagePlus;
import ij.gui.NewImage;
import ij.plugin.Converter;
import ij.plugin.filter.Binary;
import ij.plugin.filter.PlugInFilter;
import ij.process.ImageConverter;
import ij.process.ImageProcessor;

import java.awt.image.ColorModel;
import java.awt.image.IndexColorModel;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Created by twang7 on 4/28/2016.
 */
public class Indextable_Display implements PlugInFilter{
    @Override
    public int setup(String s, ImagePlus imagePlus) {
        return DOES_8C;
    }

    @Override
    public void run(ImageProcessor imageProcessor) {
        ImagePlus result = NewImage.createRGBImage("Color board", 16*60, 16*60, 1, 0);
        //test.show();
        ImagePlus imagePlus = new ImagePlus("new", imageProcessor);

        ImageConverter ic = new ImageConverter(imagePlus);
        ic.convertToRGB();
        ImageProcessor rgbProcessor = imagePlus.getProcessor();
        ImageProcessor resultProcessor = result.getProcessor();
        imagePlus.show();

        int w = resultProcessor.getWidth();
        int h = resultProcessor.getHeight();
        for (int i = 0; i < h; i++){
            for (int j = 0; j < w; j++){
                if (i%60 == 0 && j%60 == 0){
                    for (int subi = 0; subi < 16; subi++){
                        for (int subj = 0; subj < 16; subj++){
                            resultProcessor.set(i+subi, j+subj, 0);
                        }
                    }
                }
            }
        }

        Map<Integer, Integer> colorHis = new HashMap<Integer, Integer>();
        Map<Integer, Integer> realValue = new HashMap<Integer, Integer>();
        for (int i = 0; i < imageProcessor.getWidth(); i++){
            for (int j = 0; j < imageProcessor.getHeight(); j++){
                int tempColor = imageProcessor.get(i,j);
                if (colorHis.containsKey(tempColor)) colorHis.put(tempColor, colorHis.get(tempColor) + 1);
                else{
                    colorHis.put(tempColor, 1);
                }
                if (!realValue.containsKey(tempColor)) realValue.put(tempColor, rgbProcessor.get(i, j));
            }
        }


        for (Integer curr : colorHis.keySet()){
            int y = (curr / 16)*60;
            int x = (curr % 16)*60;
            for (int i = 0; i < 16; i++){
                for (int j = 0; j < 16; j++){
                    resultProcessor.putPixel(x+i, y+j, realValue.get(curr));
                }
            }
            resultProcessor.drawString(Integer.toString(curr)+"." + Integer.toString(colorHis.get(curr)), x, y + 30);
        }


        result.show();


    }
}

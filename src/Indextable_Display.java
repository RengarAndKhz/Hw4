import ij.ImagePlus;
import ij.gui.NewImage;
import ij.plugin.filter.PlugInFilter;
import ij.process.ImageConverter;
import ij.process.ImageProcessor;

import java.awt.image.IndexColorModel;

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
        //imageProcessor.drawString("abcd", 100, 100);
        //ImagePlus result = new ImagePlus("Color board");
        //ImageProcessor resultProcessor = result.getProcessor();
        ImagePlus result = NewImage.createRGBImage("Color board", 16*40, 16*40, 1, 0);
        ImageConverter ic = new ImageConverter(result);
        //ic.convertRGBtoIndexedColor(255);

        //test.show();
        ImageProcessor resultProcessor = result.getProcessor();

        //int indexedColor = 0;
        int w = resultProcessor.getWidth();
        int h = resultProcessor.getHeight();
        for (int i = 0; i < h; i++){
            for (int j = 0; j < w; j++){
                if (i%40 == 0 && j%40 == 0){
                    for (int subi = 0; subi < 16; subi++){
                        for (int subj = 0; subj < 16; subj++){
                            resultProcessor.set(i+subi, j+subj, 0);
                        }
                    }
                    //indexedColor ++;
                }
            }
        }

        /*
        get the color from input image
         */
        IndexColorModel icm = (IndexColorModel) imageProcessor.getColorModel();
        int pixBits = icm.getPixelSize();
        int mapSize = icm.getMapSize();
        //retrieve the current lookup tables for RGB
        byte[] Rmap = new byte[mapSize]; icm.getRed(Rmap);
        byte[] Gmap = new byte[mapSize]; icm.getGreen(Gmap);
        byte[] Bmap = new byte[mapSize]; icm.getBlue()
        resultProcessor.drawString("111", 0, 30);
        result.show();


    }
}

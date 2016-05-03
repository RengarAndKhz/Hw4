import javafx.util.Pair;

import java.util.HashMap;

/**
 * Created by Tianyang on 5/2/2016.
 */
public class Test {
    final static double[][] lmsTransferMatrix = new double[][]{{0.381, 0.5783, 0.0402},{0.1967, 0.7244, 0.0782},{0.0241, 0.1288, 0.8444}};
    final static double[][] lmsTolab1 = new double[][]{{1.0/Math.sqrt(3), 0.0, 0.0},{0.0, 1.0/Math.sqrt(6), 0.0}, {0.0, 0.0, 1.0/Math.sqrt(2)}};
    final static double[][] lmsTolab2 = new double[][]{{1,1,1},{1,1,-2},{1,-1,0}};
    final static double[][] labTolms1 = new double[][]{{1,1,1},{1,1,-1}, {1,-2,0}};
    final static double[][] labTolms2 = new double[][]{{Math.sqrt(3)/3, 0, 0}, {0, Math.sqrt(6)/6, 0}, {0, 0, Math.sqrt(2)/2}};
    final static double[][] lmsToRGB = new double[][]{{4.4679, -3.5873, 0.1193}, {-1.2186, 2.3809, -0.1624}, {0.0497, -0.2439, 1.2045}};

    public static void main(String[] args) {
        int[] rgb = new int[]{1,2,3};
        double[] lms = new double[3];
        for (int i = 0; i < 3; i++){
            for (int j = 0; j < 3; j++){
                lms[i] += lmsTransferMatrix[i][j]*rgb[j];
            }
        }
        for (int i = 0; i < 3; i++) System.out.println(lms[i]);

        double[] againrgb = new double[3];
        for (int i = 0; i < 3; i++){
            for (int j = 0; j < 3; j++){
                againrgb[i] += lmsToRGB[i][j]*lms[j];
            }
        }
        for (double curr : againrgb) System.out.println(curr);
    }
}

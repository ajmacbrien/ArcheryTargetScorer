package com.example.targetfacescorer;

import android.graphics.Bitmap;

import org.opencv.android.Utils;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

public class Scorer {
    public static int faceScorer(Bitmap bmp) {
        //converts the bitmap image to Mat
        Bitmap bitmap = bmp;
        Mat img = new Mat (bitmap.getHeight(), bitmap.getWidth(), CvType.CV_8UC1);
        Utils.bitmapToMat(bitmap, img);


        //resizes the image to reduce processing time
        Size newRes = new Size(800, 800);

        //makes an array of the pixels in the image
        double[][][] pixels = imgToPixels(img);

        //find the locations of the arrows and the information about the target face
        int[] test = centerFinder(pixels);
        int width = findWidth(pixels, test);
        double[] arrows = findArrows(pixels, test, width);

        //calculates the score
        int score = 10 * arrows.length;

        for (int i = 0; i < arrows.length; i++) {
            score -= arrows[i]/(width/2);
        }

        return score;
    }


    //finds the center of the target
    private static int[] centerFinder(double[][][] pixels) {
        double[] xcenters = new double[pixels[0].length], ycenters = new double[pixels[0].length];
        int yCenterIndex = -777, xCenterIndex = -777, yMax = -777, xMax = -777;

        //iterates over the mage finds the edges of the target face
        for (int i = 0; i < pixels.length; i++) {

            int max = -777, min = 777;

            for (int j = 0; j < pixels[0].length; j++) {
                if (pixels[i][j][0] > 0) {
                    if (j > max) {
                        max = j;
                    }
                    if (j < min) {
                        min = j;
                    }
                }
            }

            if (max != -777 && min != 777) {
                ycenters[(max-min)/2]++;
            }
        }

        for (int i = 0; i < pixels[0].length; i++) {
            int max = -777, min = 777;

            for (int j = 0; j < pixels.length; j++) {
                if (pixels[j][i][0] > 0) {
                    if (j > max) {
                        max = j;
                    }
                    if (j < min) {
                        min = j;
                    }
                }
            }

            if (max != -777 && min != 777) {
                xcenters[(max-min)/2]++;
            }
        }

        //uses the edge to calcualte an average center
        for (int i = 0; i < xcenters.length; i++) {
            if (xcenters[i] > xMax) {
                xCenterIndex = i;
                xMax = (int) xcenters[i];
            }
        }

        for (int i = 0; i < ycenters.length; i++) {
            if (ycenters[i] > yMax) {
                yCenterIndex = i;
                yMax = (int) ycenters[i];
            }
        }
        int[] ans = {xCenterIndex, yCenterIndex};

        return ans;
    }



    private static int findWidth(double[][][] pixels, int[] center) {
        int max = -777, min = 777;

        //uses the center to find the diamter of the targer
        for (int j = 0; j < pixels[0].length; j++) {
            if (pixels[center[0]][j][0] > 0) {
                if (j > max) {
                    max = j;
                }
                if (j < min) {
                    min = j;
                }
            }
        }

        return (max - min);
    }

    private static double[] findArrows(double[][][] pixels, int[] center, double width) {
        double[] distances = new double[(int) (width/5)];
        double[] ans = {-777, -777, -777};

        //finds all the pixels that are not black and how far away from the cetner they are
        for (int i = 0; i < pixels.length; i++) {
            for (int j = 0; j < pixels[0].length; j++) {
                if (pixels[i][j][0] > 0) {
                    int x = i, y =j;
                    int distance = (int) Math.sqrt((center[0] - x) * (center[0] - x) + (center[1] - y) * (center[1] - y));
                    distances[distance/5]++;
                }
            }
        }

        //locates the three arrows and returns their distance from the center
        for (int i = 0; i < 3; i++) {
            int min = 777;
            int curr = -777;

            for (int j = 0; j < width/10; j++) {
                if (distances[j] < min && distances[j] > 0) {
                    curr = j;
                }
            }

            distances[curr] = 0;
            ans[i] = curr;
        }

        return ans;
    }

    //takes an image and returns an array of the pixels in the image
    private static double[][][] imgToPixels(Mat img) {
        Size imageSize = img.size();
        double[][][] pixels
                = new double[(int) imageSize.height][(int) imageSize.width][3]; //this does not assume an rgb img

        for (int i = 0; i < ((int) imageSize.height); i++) {
            for (int j = 0; j < ((int) imageSize.width); j++) {
                pixels[i][j] = img.get(i, j);
            }
        }

        return pixels;
    }

    //detects the edges and transforms the image to black and white
    private static Mat edgeDetector(Mat img) {
        Imgproc.cvtColor(img, img, Imgproc.COLOR_RGB2GRAY);
        Imgproc.Canny(img, img, 240, 100*3);

        return img;
    }
}

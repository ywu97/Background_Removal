import java.awt.*;
import java.awt.image.*;
import java.io.*;
import java.util.HashMap;
import java.util.Map;
import java.util.*;
import javax.swing.*;

//To compile the program:
//Terminal: javac MyImageComposition.java
//          java MyImageComposition /Path of foregroundImage/ /Path of backgroundImage/
public class MyImageComposition {
    JFrame frame;
    JLabel lbIm1;
    BufferedImage backgroundImgBackUp;
    int width = 960;
    int height = 540;

    int[][] directions = { { -1, -1 }, { 0, -1 }, { 1, -1 }, { -1, 0 },{ 1, 0 }, { -1, 1 }, { 0, 1 }, { 1, 1 } };

    int maxRangeHKey;
    int maxRangeSKey;
    double maxRangeVKey;

    Set<int[]> neededAnti = new HashSet<>();

    /**
     * Read Image RGB Reads the image of given width and height at the given imgPath
     * into the provided BufferedImage.
     */
    private void readImageRGB(int width, int height, String imgPath, BufferedImage img) {
        try {
            int frameLength = width * height * 3;

            File file = new File(imgPath);
            RandomAccessFile raf = new RandomAccessFile(file, "r");
            raf.seek(0);

            long len = frameLength;
            byte[] bytes = new byte[(int) len];

            raf.read(bytes);

            int ind = 0;
            for (int y = 0; y < height; y++) {
                for (int x = 0; x < width; x++) {
                    byte a = 0;
                    byte r = bytes[ind];
                    byte g = bytes[ind + height * width];
                    byte b = bytes[ind + height * width * 2];

                    int pix = 0xff000000 | ((r & 0xff) << 16) | ((g & 0xff) << 8) | (b & 0xff);
                    // int pix = ((a << 24) + (r << 16) + (g << 8) + b);

                    img.setRGB(x, y, pix);
                    ind++;
                }
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void showIms(String[] args) throws IOException {

        String param0 = args[0];
        System.out.println("The foregroundImage is: " + param0);

        String param1 = args[1];
        System.out.println("The backgroundImage is: " + param1);

        // Read in the specified image
        BufferedImage foregroundImg = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        readImageRGB(width, height, args[0], foregroundImg);

        BufferedImage backgroundImg = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        readImageRGB(width, height, args[1], backgroundImg);

        backgroundImgBackUp = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        readImageRGB(width, height, args[1], backgroundImgBackUp);

        hsvHistrogram(foregroundImg);

        compositeImg(foregroundImg, backgroundImg);

        // Use label to display the image
        frame = new JFrame();
        GridBagLayout gLayout = new GridBagLayout();
        frame.getContentPane().setLayout(gLayout);

        lbIm1 = new JLabel(new ImageIcon(backgroundImg));

        GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.HORIZONTAL;
        c.anchor = GridBagConstraints.CENTER;
        c.weightx = 0.5;
        c.gridx = 0;
        c.gridy = 0;

        c.fill = GridBagConstraints.HORIZONTAL;
        c.gridx = 0;
        c.gridy = 1;
        frame.getContentPane().add(lbIm1, c);

        frame.pack();
        frame.setVisible(true);
    }

    private void hsvHistrogram (BufferedImage foregroundImg){
        Map<Integer, Integer> map_h = new HashMap<>();
//        map_h.put(0, 0); // h in 330 ~ 30 mid =  0
//        map_h.put(60, 0); //h in 30 ~ 90 mid = 60
//        map_h.put(120, 0); // h in 90 ~ 150 mid = 120
//        map_h.put(180, 0); // h in 150 ~ 210 mid = 180
//        map_h.put(240,0); // h in 210 ~ 270 mid = 240
//        map_h.put(300,0); // h in 270 ~ 330 mid = 300
//        map_h.put(1000, 0); // for NAN
        int maxRangeKey_h = 0;
        int m = 0;
        while(m <= 300){
            map_h.put(m, 0);
            m += 60;
        }

        Map<Integer, Integer> map_s = new HashMap<>();
//        map_s.put(25, 0);
//        map_s.put(50, 0);
//        map_s.put(75, 0);
//        map_s.put(100, 0);
        int maxRangeKey_s = 25;
        int j = 25;
        while(j <= 100){
            map_s.put(j, 0);
            j += 25;
        }

        Map<Double, Integer> map_v = new HashMap<>();
//        map_v.put(12.5, 0);
//        map_v.put(25.0, 0);
//        map_v.put(37.5, 0);
//        map_v.put(50.0,0);
//        map_v.put(62.5,0);
//        map_v.put(75.0, 0);
//        map_v.put(87.5, 0);
//        map_v.put(100.0, 0);
        double i = 12.5;
        while(i <= 100){
            map_v.put(i, 0);
            i += 12.5;
        }
        double maxRangeKey_v = 12.5;

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int rgb = foregroundImg.getRGB(x, y);
                int red = (rgb >> 16) & 0x000000FF;
                int green = (rgb >>8 ) & 0x000000FF;
                int blue = (rgb) & 0x000000FF;
                double[] hsv = rbgToHSV((double)red, (double)green, (double)blue);
                double h = hsv[0];
                double s = hsv[1];
                double v = hsv[2];
//              h-histrogram
                if( h > 30 && h <= 90){
                    map_h.put(60, (map_h.get(60) + 1));
                    if(map_h.get(60) > map_h.get(maxRangeKey_h) && maxRangeKey_h != 1000){
                        maxRangeKey_h = 60;
                    }
                }else if(h > 90 && h <= 150){
                    map_h.put(120, (map_h.get(120) + 1));
                    if(map_h.get(120) > map_h.get(maxRangeKey_h) && maxRangeKey_h != 1000){
                        maxRangeKey_h = 120;
                    }
                }else if(h > 150 && h <= 210){
                    map_h.put(180, (map_h.get(180) + 1));
                    if(map_h.get(180) > map_h.get(maxRangeKey_h) && maxRangeKey_h != 1000){
                        maxRangeKey_h = 180;
                    }
                }else if(h > 210 && h <= 270){
                    map_h.put(240, (map_h.get(240) + 1));
                    if(map_h.get(240) > map_h.get(maxRangeKey_h) && maxRangeKey_h != 1000){
                        maxRangeKey_h = 240;
                    }
                }else if(h > 270 && h <= 330){
                    map_h.put(300, (map_h.get(300) + 1));
                    if(map_h.get(300) > map_h.get(maxRangeKey_h) && maxRangeKey_h != 1000){
                        maxRangeKey_h = 300;
                    }
                }else if(h < 0){
                    map_h.put(1000, (map_h.get(1000) + 1));
                }else{
                    map_h.put(0, (map_h.get(0) + 1));
                    if(map_h.get(0) > map_h.get(maxRangeKey_h) && maxRangeKey_h != 1000){
                        maxRangeKey_h = 0;
                    }
                }

                //s-histrogram
                if(s >= 0 && s < 25){
                    map_s.put(25, map_s.get(25) + 1);
                    if(map_s.get(25) > map_s.get(maxRangeKey_s)){
                        maxRangeKey_s = 25;
                    }
                }else if(s >= 25 && s < 50){
                    map_s.put(50, map_s.get(50) + 1);
                    if(map_s.get(50) > map_s.get(maxRangeKey_s)){
                        maxRangeKey_s = 50;
                    }
                }else if(s >= 50 && s < 75){
                    map_s.put(75, map_s.get(75) + 1);
                    if(map_s.get(75) > map_s.get(maxRangeKey_s)){
                        maxRangeKey_s = 75;
                    }
                }else if(s >= 75 && s <= 100){
                    map_s.put(100, map_s.get(100) + 1);
                    if(map_s.get(100) > map_s.get(maxRangeKey_s)){
                        maxRangeKey_s = 100;
                    }
                }

                //v-histrogram
                if(v >= 0 && v < 12.5){
                    map_v.put(12.5, map_v.get(12.5) + 1);
                    if(map_v.get(12.5) > map_v.get(maxRangeKey_v)){
                        maxRangeKey_v = 12.5;
                    }
                }else if(v >= 12.5 && v < 25.0){
                    map_v.put(25.0, map_v.get(25.0) + 1);
                    if(map_v.get(25.0) > map_v.get(maxRangeKey_v)){
                        maxRangeKey_v = 25.0;
                    }
                }else if(v >= 25.0 && v < 37.5){
                    map_v.put(37.5, map_v.get(37.5) + 1);
                    if(map_v.get(37.5) > map_v.get(maxRangeKey_v)){
                        maxRangeKey_v = 37.5;
                    }
                }else if(v >= 37.5 && v < 50.0){
                    map_v.put(50.0, map_v.get(50.0) + 1);
                    if(map_v.get(50.0) > map_v.get(maxRangeKey_v)){
                        maxRangeKey_v = 50.0;
                    }
                }else if(v >= 50.0 && v < 62.5){
                    map_v.put(62.5, map_v.get(62.5) + 1);
                    if(map_v.get(62.5) > map_v.get(maxRangeKey_v)){
                        maxRangeKey_v = 62.5;
                    }
                }else if(v >= 62.5 && v < 75.0){
                    map_v.put(75.0, map_v.get(75.0) + 1);
                    if(map_v.get(75.0) > map_v.get(maxRangeKey_v)){
                        maxRangeKey_v = 75.0;
                    }
                }else if(v >= 75.0 && v < 87.5){
                    map_v.put(87.5, map_v.get(87.5) + 1);
                    if(map_v.get(87.5) > map_v.get(maxRangeKey_v)){
                        maxRangeKey_v = 87.5;
                    }
                }else if(v >= 87.5 && v <= 100.0) {
                    map_v.put(100.0, map_v.get(100.0) + 1);
                    if (map_v.get(100.0) > map_v.get(maxRangeKey_v)) {
                        maxRangeKey_v = 100.0;
                    }
                }
            }
        }

//        for(Integer in : map_h.keySet()){
//            System.out.println("h_Key:" + in + " h_#Count: " + map_h.get(in));
//        }
        maxRangeHKey = maxRangeKey_h;
        System.out.println("maxRangeHKey: " + maxRangeHKey);

//        for(Integer in : map_s.keySet()){
//            System.out.println("s_Key:" + in + " s_#Count: " + map_s.get(in));
//        }
        maxRangeSKey = maxRangeKey_s;
        System.out.println("maxRangeSKey: " + maxRangeSKey);

//        for(Double in : map_v.keySet()){
//            System.out.println("v_Key:" + in + " v_#Count: " + map_v.get(in));
//        }
        maxRangeVKey = maxRangeKey_v;
        System.out.println("maxRangeVKey: " + maxRangeVKey);
    }

    private void compositeImg(BufferedImage foregroundImg, BufferedImage backgroundImg) {
        int white_color = ((255 << 24) + (255 << 16) + (255 << 8) + 255);

        int lower_h = maxRangeHKey == 0 ? 320 : maxRangeHKey - 40;

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int rgb = foregroundImg.getRGB(x, y);
                int red = (rgb >> 16) & 0x000000FF;
                int green = (rgb >> 8) & 0x000000FF;
                int blue = (rgb) & 0x000000FF;

                double[] hsv = rbgToHSV((double) red, (double) green, (double) blue);
                double h = hsv[0];

                //background chroma
                if(lower_h == 320){
                    if(((h >= lower_h && h < 360)|| (h >= 0 && h < maxRangeHKey + 41))
                            && ( (hsv[1] >= maxRangeSKey * 0.55 && hsv[2] >= maxRangeVKey * 0.55 )
                            || (hsv[1] <= 15 && hsv[2] >= 95))){
                        foregroundImg.setRGB(x,y, white_color);
                    }else{
                        backgroundImg.setRGB(x, y, rgb);
                    }
                } else {
                    if (h >= 0 && h >= lower_h && h < maxRangeHKey + 41
                            && ((hsv[1] >= maxRangeSKey * 0.55 && hsv[2] >= maxRangeVKey * 0.55)
                            || (hsv[1] <= 15 && hsv[2] >= 95))) {
                        foregroundImg.setRGB(x, y, white_color);
                    } else {
                        backgroundImg.setRGB(x, y, rgb);
                    }
                }
            }
        }

        //check boarder color
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int rgb = foregroundImg.getRGB(x, y);
                int rgbBackup = backgroundImgBackUp.getRGB(x, y);
                int red = (rgb >> 16) & 0x000000FF;
                int green = (rgb >> 8) & 0x000000FF;
                int blue = (rgb) & 0x000000FF;

                double[] hsv = rbgToHSV((double) red, (double) green, (double) blue);
                double h = hsv[0];

                if(lower_h == 320){
                    if(((h >= lower_h && h < 360)|| (h >= 0 && h < maxRangeHKey + 41))
                            && hsv[1] >= 50 && hsv[2] >= 20 && hsv[2] <= 34){
                        backgroundImg.setRGB(x, y, rgbBackup);
                        neededAnti.add(new int[]{x, y});
                    }
                }else if(h >= lower_h && h < maxRangeHKey + 41 && hsv[1] >= 50 && hsv[2] >= 20 && hsv[2] <= 34){
                    backgroundImg.setRGB(x, y, rgbBackup);
                    neededAnti.add(new int[]{x, y});
                }
                if((red * blue) != 0 && (green * green) / (red * blue) > 1.2){
//                    backgroundImg.setRGB(x, y, rgbBackup);
                    neededAnti.add(new int[]{x, y});
                }
                if((red * green) != 0 && (blue * blue) / (red * green) > 1.2){
//                    backgroundImg.setRGB(x, y, rgbBackup);
                    neededAnti.add(new int[]{x, y});
                }
            }
        }
        filter(backgroundImg);
    }

    private void filter(BufferedImage img) {
        int imgH = img.getHeight();
        int imgW = img.getWidth();
        int avgPix = 0;
        int[][] filterRGB = new int[imgH + 2][imgW + 2];
        for (int m = 1; m < imgH + 1; m++) {
            for (int n = 1; n < imgW + 1; n++) {
                filterRGB[m][n] = img.getRGB(n - 1, m - 1);
            }
        }
        // extend boarder
        filterRGB[0][0] = filterRGB[1][1];
        filterRGB[imgH + 1][0] = filterRGB[imgH][0];
        filterRGB[0][imgW + 1] = filterRGB[0][imgW];
        filterRGB[imgH + 1][imgW + 1] = filterRGB[imgH][imgW];

        for (int i = 1; i < imgW + 1; i++) {
            filterRGB[0][i] = filterRGB[1][i];
            filterRGB[imgH + 1][i] = filterRGB[imgH][i];
        }
        for (int i = 1; i < imgH + 1; i++) {
            filterRGB[i][0] = filterRGB[i][1];
            filterRGB[i][imgW + 1] = filterRGB[i][imgW];
        }

        // averaging filter
        for(int[] arr: neededAnti) {
            int row = 0, col = 0;
            long red = 0, green = 0, blue = 0, a = 0;
            int x = arr[0];
            int y = arr[1];
            for (int[] dir : directions) {
                row = y + dir[1] + 1;
                col = x + dir[0] + 1;
                int pixVal = filterRGB[row][col];
                a += (pixVal >> 24) & 0xff;
                red += (pixVal >> 16) & 0xff;
                green += (pixVal >> 8) & 0xff;
                blue += pixVal & 0xff;

            }

            int avgA = (int) a / 9;
            int avgRed = (int) red / 9;
            int avgGreen = (int) green / 9;
            int avgBlue = (int) blue / 9;

            avgPix = avgA << 24 | avgRed << 16 | avgGreen << 8 | avgBlue;
            img.setRGB(x,y,avgPix);
        }
    }

    private double[] rbgToHSV(double r, double g, double b) {
        double h, s, v;

        double min, max, delta;

        min = Math.min(Math.min(r, g), b);
        max = Math.max(Math.max(r, g), b);

        // V
        v = max;

        delta = max - min;

        if (delta < 0.00001)
        {
            s = 0;
            h = 0;
            return new double[]{h, s, v};
        }
        // S
        if( max != 0 )
            s = delta / max;
        else {
//            if max is 0, then r = g = b = 0
//            s = 0, h is undefined
            s = 0;
            h = -1; //NaN
            return new double[]{h,s,v};
        }

        // H
        if( r == max )
            h = ( g - b ) / delta; // between yellow & magenta
        else if( g == max )
            h = 2 + ( b - r ) / delta; // between cyan & yellow
        else
            h = 4 + ( r - g ) / delta; // between magenta & cyan

        h *= 60;    // degrees

        if( h < 0 )
            h += 360;

        h = h * 1.0;
        s = s * 100.0;
        v = (v / 256.0) * 100.0;
        return new double[]{h,s,v};
    }

    public static void main(String[] args) throws IOException {
        MyImageComposition imgcomp = new MyImageComposition();
        imgcomp.showIms(args);
    }
}

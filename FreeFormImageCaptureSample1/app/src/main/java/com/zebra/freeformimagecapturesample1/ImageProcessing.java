package com.zebra.freeformimagecapturesample1;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageFormat;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.graphics.YuvImage;

import java.io.ByteArrayOutputStream;

public class ImageProcessing {

    private final String IMG_FORMAT_YUV = "YUV";
    private final String IMG_FORMAT_Y8 = "Y8";

    private static ImageProcessing instance = null;

    public static ImageProcessing getInstance() {

        if (instance == null) {
            instance = new ImageProcessing();
        }
        return instance;
    }

    private ImageProcessing() {
        //Private Constructor
    }

    public Bitmap getBitmap(byte[] data, String imageFormat, int orientation, int stride, int width, int height)
    {
        if(imageFormat.equalsIgnoreCase(IMG_FORMAT_YUV))
        {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            YuvImage yuvImage = new YuvImage(data, ImageFormat.NV21, width, height, new int[]{stride, stride});
            yuvImage.compressToJpeg(new Rect(0, 0, stride, height), 100, out);
            yuvImage.getYuvData();
            byte[] imageBytes = out.toByteArray();
            if(orientation != 0)
            {
                Matrix matrix = new Matrix();
                matrix.postRotate(orientation);
                Bitmap bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length);
                return Bitmap.createBitmap(bitmap, 0 , 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
            }
            else
            {
                return BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length);
            }
        }
        else if(imageFormat.equalsIgnoreCase(IMG_FORMAT_Y8))
        {
            return convertYtoJPG_CPU(data, orientation, stride, height);
        }

        return null;
    }


    private Bitmap convertYtoJPG_CPU(byte[] data, int orientation, int stride, int height)
    {
        int mLength = data.length;
        int [] pixels = new int[mLength];
        for(int i = 0; i < mLength; i++)
        {
            int p = data[i] & 0xFF;
            pixels[i] = 0xff000000 | p << 16 | p << 8 | p;
        }
        if(orientation != 0)
        {
            Matrix matrix = new Matrix();
            matrix.postRotate(orientation);
            Bitmap bitmap = Bitmap.createBitmap(pixels, stride, height, Bitmap.Config.ARGB_8888);
            return Bitmap.createBitmap(bitmap, 0 , 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
        }
        else
        {
            return Bitmap.createBitmap(pixels, stride, height, Bitmap.Config.ARGB_8888);
        }
    }

}

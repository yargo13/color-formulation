package com.example.myapplication;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;

import java.io.FileNotFoundException;
import java.io.InputStream;

public class ImageActivity extends AppCompatActivity {

    DrawView drawView;
    static public String value_a = "com.application.myApplication.value_a";
    static public String value_b = "com.application.myApplication.value_b";
    static public String value_L = "com.application.myApplication.value_L";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image);

        Intent intent = getIntent();
        Uri imageData = Uri.parse(intent.getStringExtra(MainActivity.EXTRA_URI_PICTURE));


        drawView = findViewById(R.id.drawView);
        try {
            InputStream inputStream = getContentResolver().openInputStream(imageData);
            drawView.setBackground(Drawable.createFromStream(inputStream, imageData.toString()));
        } catch (FileNotFoundException e) {
        }
    }

    public void confirmImage(View view) {
        BitmapDrawable imgDrawable = drawView.getSelectedRegion();
        Bitmap bitmap = (imgDrawable).getBitmap();
        LAB average_lab = getAverageLAB(bitmap);

        setResult(RESULT_OK,
                new Intent().putExtra(
                        value_a, average_lab.getA()
                ).putExtra(
                        value_b, average_lab.getB()
                ).putExtra(
                        value_L, average_lab.getL()
                )
        );
        finish();
    }

    protected LAB getAverageLAB(Bitmap bitmap) {
        int r_int, g_int, b_int;
        double L_avg = 0;
        double A_avg = 0;
        double B_avg = 0;
        XYZSet xyzSet = new XYZSet();
        LAB lab;
        int size = bitmap.getWidth()*bitmap.getHeight();
        int[] pixels = new int[size];
        bitmap.getPixels(pixels, 0, bitmap.getWidth(), 0, 0, bitmap.getWidth(), bitmap.getHeight());
        for (int pixel: pixels) {
            r_int = (pixel >> 16) & 0xff;
            g_int = (pixel >> 8) & 0xff;
            b_int = pixel & 0xff;
            xyzSet.RGBtoLAB(r_int, g_int, b_int);
            lab = xyzSet.getLAB();
            L_avg += lab.getL();
            A_avg += lab.getA();
            B_avg += lab.getB();
        }

        return new LAB(L_avg/size, A_avg/size,B_avg/size);
    }
}
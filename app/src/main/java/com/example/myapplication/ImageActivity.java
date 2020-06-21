package com.example.myapplication;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

public class ImageActivity extends AppCompatActivity {

    ImageView imageView;
    static public String value_a = "com.application.myApplication.value_a";
    static public String value_b = "com.application.myApplication.value_b";
    static public String value_L = "com.application.myApplication.value_L";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image);

        Intent intent = getIntent();
        Uri imageData = Uri.parse(intent.getStringExtra(MainActivity.EXTRA_URI_PICTURE));

        imageView = findViewById(R.id.imageView);
        imageView.setImageURI(imageData);

    }

    public void confirmImage(View view) {
        Drawable imgDrawable = imageView.getDrawable();
        Bitmap bitmap = ((BitmapDrawable)imgDrawable).getBitmap();
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
        return new LAB(70.5, 5.69,16.42);
    }
}
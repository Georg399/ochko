package com.example.ochko;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.mlkit.vision.label.ImageLabel;
import com.google.mlkit.vision.label.ImageLabeler;
import com.google.mlkit.vision.label.ImageLabeling;
import com.google.mlkit.vision.label.defaults.ImageLabelerOptions;
import com.google.mlkit.vision.common.InputImage;

import java.io.IOException;

public class MainActivity extends AppCompatActivity {
    private static final int camera1 = 100;
    private static final int camera2 = 101;
    private static final int gallery1 = 102;

    // Элементы интерфейса
    private Button button1;
    private Button button2;
    private Button button3;
    private ImageView image1;
    private TextView text1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        button1 = findViewById(R.id.photo_button);
        button2 = findViewById(R.id.gallery_button);
        button3 = findViewById(R.id.exit);
        image1 = findViewById(R.id.image_view);
        text1 = findViewById(R.id.recognized_text_view);
        button1.setOnClickListener(v -> {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                    == PackageManager.PERMISSION_GRANTED) {
                methodCamera1();
            } else {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.CAMERA},
                        camera2);
            }
        });

        button2.setOnClickListener(v -> methodGallery1());
        button3.setOnClickListener(v -> finish());
    }
    private void methodCamera1() {
        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(cameraIntent, camera1);
    }

    private void methodGallery1() {
        Intent galleryIntent = new Intent(Intent.ACTION_PICK);
        galleryIntent.setType("image/*");
        startActivityForResult(galleryIntent, gallery1);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == camera2) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                methodCamera1();
            } else {
                Toast.makeText(this, "Нужно разрешение на камеру", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK && data != null) {
            if (requestCode == camera1) {
                Bitmap bitmap1 = (Bitmap) data.getExtras().get("data");
                image1.setImageBitmap(bitmap1);
                methodML1(InputImage.fromBitmap(bitmap1, 0));
            }
            else if (requestCode == gallery1) {
                Uri uri1 = data.getData();
                image1.setImageURI(uri1);
                try {
                    methodML1(InputImage.fromFilePath(this, uri1));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void methodML1(InputImage inputImage1) {
        ImageLabeler labeler1 = ImageLabeling.getClient(ImageLabelerOptions.DEFAULT_OPTIONS);

        labeler1.process(inputImage1)
                .addOnSuccessListener(labels1 -> {
                    if (labels1.isEmpty()) {
                        text1.setText("Ничего не найдено");
                        return;
                    }

                    StringBuilder resultText1 = new StringBuilder();
                    for (ImageLabel label1 : labels1) {
                        String text = label1.getText();
                        float confidence = label1.getConfidence();

                        if (confidence > 0.5f) {
                            resultText1.append(text)
                                    .append(" (")
                                    .append((int)(confidence * 100))
                                    .append("%)\n");
                        }
                    }

                    if (resultText1.length() == 0) {
                        text1.setText("Объекты не распознаны уверенно");
                    } else {
                        text1.setText(resultText1.toString());
                    }
                });

    }
}

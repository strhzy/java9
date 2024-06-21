package com.example.photoactivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;

import androidx.activity.ComponentActivity;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;

public class MainActivity extends ComponentActivity {

    private final ArrayList<Uri> images = new ArrayList<>();
    private ArrayAdapter<Uri> adapter;

    private final ActivityResultLauncher<Intent> takePictureLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    Bitmap photo = (Bitmap) result.getData().getExtras().get("data");
                    if (photo != null) {
                        Uri imageUri = saveImageToCache(photo);
                        if (imageUri != null) {
                            images.add(imageUri);
                            adapter.notifyDataSetChanged();
                        }
                    }
                }
            }
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button cameraOpenButton = findViewById(R.id.CameraButton);
        ListView imageListView = findViewById(R.id.ImageList);

        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, images);
        imageListView.setAdapter(adapter);

        loadImagesFromCache();

        cameraOpenButton.setOnClickListener(v -> {
            Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            takePictureLauncher.launch(takePictureIntent);
        });

        imageListView.setOnItemClickListener((parent, view, position, id) -> {
            Uri imageUri = (Uri) parent.getItemAtPosition(position);
            Intent intent = new Intent(MainActivity.this, FullImageActivity.class);
            intent.putExtra("image", imageUri.toString());
            startActivity(intent);
        });
    }

    private void loadImagesFromCache() {
        File cacheDir = getCacheDir();
        File[] files = cacheDir.listFiles();
        if (files != null) {
            for (File file : files) {
                Uri uri = Uri.fromFile(file);
                images.add(uri);
            }
            adapter.notifyDataSetChanged();
        }
    }

    private Uri saveImageToCache(@NonNull Bitmap bitmapImage) {
        File outputDir = getCacheDir();
        File imageFile = new File(outputDir, System.currentTimeMillis() + ".jpg");

        try (FileOutputStream fos = new FileOutputStream(imageFile)) {
            bitmapImage.compress(Bitmap.CompressFormat.JPEG, 100, fos);
            return Uri.fromFile(imageFile);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}

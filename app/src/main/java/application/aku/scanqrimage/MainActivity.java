package application.aku.scanqrimage;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.canhub.cropper.CropImageContract;
import com.canhub.cropper.CropImageContractOptions;
import com.canhub.cropper.CropImageOptions;
import com.google.zxing.BinaryBitmap;
import com.google.zxing.LuminanceSource;
import com.google.zxing.MultiFormatReader;
import com.google.zxing.RGBLuminanceSource;
import com.google.zxing.Result;
import com.google.zxing.common.HybridBinarizer;

import java.io.FileNotFoundException;
import java.io.InputStream;

public class MainActivity extends AppCompatActivity {
    Button btncamera, btnimage, btncrop;
    public static TextView tvresult;

    ActivityResultLauncher<Intent> activityResultLauncher;
    ActivityResultLauncher<CropImageContractOptions> activityResultLauncherImage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btncamera = findViewById(R.id.btncamera);
        btnimage = findViewById(R.id.btnimage);
        btncrop = findViewById(R.id.btncrop);
        tvresult = findViewById(R.id.tvresult);

        activityResultLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        Intent data = result.getData();
                        try {
                            final Uri imageUri = data.getData();
                            final InputStream imageStream = getContentResolver().openInputStream(imageUri);
                            final Bitmap selectedImage = BitmapFactory.decodeStream(imageStream);
                            try {
                                Bitmap bMap = selectedImage;
                                String contents = null;

                                int[] intArray = new int[bMap.getWidth()*bMap.getHeight()];
                                bMap.getPixels(intArray, 0, bMap.getWidth(), 0, 0, bMap.getWidth(), bMap.getHeight());

                                LuminanceSource source = new RGBLuminanceSource(bMap.getWidth(), bMap.getHeight(), intArray);
                                BinaryBitmap bitmap = new BinaryBitmap(new HybridBinarizer(source));

                                MultiFormatReader reader = new MultiFormatReader();
                                Result resultx = reader.decode(bitmap);
                                tvresult.setText(resultx.getText());
                            }catch (Exception e){
                                e.printStackTrace();
                            }
                        } catch (FileNotFoundException e) {
                            e.printStackTrace();
                            Toast.makeText(getApplicationContext(), "Error Exception", Toast.LENGTH_LONG).show();
                        }
                    }else if (result.getResultCode() == Activity.RESULT_CANCELED){
                        Toast.makeText(getApplicationContext(), "Error Result", Toast.LENGTH_LONG).show();
                    }
                });

        activityResultLauncherImage = registerForActivityResult(new CropImageContract(), result -> {
            if (result.isSuccessful()) {
                Uri imageUri = result.getUriContent();

                try {
                    InputStream imageStream = getContentResolver().openInputStream(imageUri);
                    Bitmap selectedImage = BitmapFactory.decodeStream(imageStream);
                    int width = selectedImage.getWidth(), height = selectedImage.getHeight();
                    int[] intArray = new int[width * height];
                    selectedImage.getPixels(intArray, 0, width, 0, 0, width, height);
                    selectedImage.recycle();
                    RGBLuminanceSource source = new RGBLuminanceSource(width, height, intArray);
                    BinaryBitmap bitmap = new BinaryBitmap(new HybridBinarizer(source));
                    MultiFormatReader reader = new MultiFormatReader();
                    Result resultx = reader.decode(bitmap);
                    tvresult.setText(resultx.getText());
                } catch (Exception e) {
                    e.printStackTrace();
                    Toast.makeText(getApplicationContext(), "Error Exception", Toast.LENGTH_LONG).show();
                }
            } else {
                Exception error = result.getError();
                Toast.makeText(getApplicationContext(), "Error Result", Toast.LENGTH_LONG).show();
            }
        });

        btncamera.setOnClickListener(v -> {
            if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.CAMERA}, 1);
            } else {
                Intent i = new Intent(MainActivity.this, Scan.class);
                i.putExtra("from", "MainActivity");
                i.setFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                startActivity(i);
            }
        });

        btnimage.setOnClickListener(view -> {
            if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 2);
            } else {
                Intent intentImage = new Intent(Intent.ACTION_PICK);
                intentImage.setType("image/*");
                activityResultLauncher.launch(intentImage);
            }
        });

        btncrop.setOnClickListener(v -> {
            if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 2);
            } else {
                CropImageContractOptions options = new CropImageContractOptions(null, new CropImageOptions())
                        .setBorderCornerThickness(5)
                        .setAspectRatio(1, 1);
                activityResultLauncherImage.launch(options);
            }
        });
    }
}

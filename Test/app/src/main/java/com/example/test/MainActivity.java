package com.example.test;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.Camera;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.ImageCaptureException;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.ImageProxy;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.content.ContextCompat;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.text.Text;
import com.google.mlkit.vision.text.TextRecognition;
import com.google.mlkit.vision.text.latin.TextRecognizerOptions;
import androidx.camera.core.ExperimentalGetImage; // ExperimentalGetImage 주석 추가
import java.io.File;
import java.util.concurrent.ExecutionException;

public class MainActivity extends AppCompatActivity {
    private PreviewView previewView;
    private ImageCapture imageCapture;
    private Camera camera;
    private ImageAnalysis imageAnalysis;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        previewView = findViewById(R.id.previewView);
        requestCameraPermission();

        Button photoButton = findViewById(R.id.Photo);
        photoButton.setOnClickListener(view -> takePhoto());
    }

    private void requestCameraPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            startCamera();
        } else {
            requestPermissionLauncher.launch(Manifest.permission.CAMERA);
        }
    }

    private final ActivityResultLauncher<String> requestPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (isGranted) {
                    startCamera();
                } else {
                    Toast.makeText(this, "권한 필요", Toast.LENGTH_SHORT).show();
                }
            });

    // 카메라 구동 및 OCR 분석 추가
    private void startCamera() {
        ListenableFuture<ProcessCameraProvider> cameraProviderFuture = ProcessCameraProvider.getInstance(this);

        cameraProviderFuture.addListener(() -> {
            try {
                ProcessCameraProvider cameraProvider = cameraProviderFuture.get();

                Preview preview = new Preview.Builder().build();
                preview.setSurfaceProvider(previewView.getSurfaceProvider());

                imageCapture = new ImageCapture.Builder().build();

                CameraSelector cameraSelector = new CameraSelector.Builder()
                        .requireLensFacing(CameraSelector.LENS_FACING_BACK)
                        .build();

                // 추가: ImageAnalysis 설정 - OCR을 위한 이미지 분석기
                imageAnalysis = new ImageAnalysis.Builder()
                        .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                        .build();

                // 추가: imageAnalysis에 이미지 분석기를 설정하여 OCR 처리
                imageAnalysis.setAnalyzer(ContextCompat.getMainExecutor(this), new ImageAnalysis.Analyzer() {
                    @ExperimentalGetImage
                    @Override
                    public void analyze(@NonNull ImageProxy imageProxy) {
                        processImageProxy(imageProxy);  // 이미지 분석 및 OCR 처리
                    }
                });


                cameraProvider.unbindAll();
                camera = cameraProvider.bindToLifecycle(this, cameraSelector, preview, imageCapture, imageAnalysis);

            } catch (ExecutionException | InterruptedException e) {
                e.printStackTrace();
            }
        }, ContextCompat.getMainExecutor(this));
    }

    // 사진 촬영 (기존 코드)
    private void takePhoto() {
        if (imageCapture != null) {
            File photoFile = new File(getOutputDirectory(), System.currentTimeMillis() + ".jpg");

            ImageCapture.OutputFileOptions outputOptions = new ImageCapture.OutputFileOptions.Builder(photoFile).build();

            imageCapture.takePicture(
                    outputOptions,
                    ContextCompat.getMainExecutor(this),
                    new ImageCapture.OnImageSavedCallback() {
                        @Override
                        public void onImageSaved(@NonNull ImageCapture.OutputFileResults outputFileResults) {
                            Toast.makeText(MainActivity.this, "저장 성공: " + photoFile.getAbsolutePath(), Toast.LENGTH_SHORT).show();
                        }

                        @Override
                        public void onError(@NonNull ImageCaptureException exception) {
                            exception.printStackTrace();
                            Toast.makeText(MainActivity.this, "저장 실패", Toast.LENGTH_SHORT).show();
                        }
                    }
            );
        }
    }

    // 경로 설정 (기존 코드)
    private File getOutputDirectory() {
        File mediaDir = getExternalMediaDirs()[0];
        File outputDir = new File(mediaDir, getResources().getString(R.string.app_name));
        if (!outputDir.exists()) {
            outputDir.mkdir();
        }
        return outputDir;
    }

    // 추가: 이미지 분석 및 OCR 처리 메서드
    @ExperimentalGetImage
    private void processImageProxy(ImageProxy imageProxy) {
        // ImageProxy에서 InputImage 생성
        InputImage inputImage = InputImage.fromMediaImage(imageProxy.getImage(), imageProxy.getImageInfo().getRotationDegrees());

        // 텍스트 인식기 생성 (Latin 기반)
        com.google.mlkit.vision.text.TextRecognizer recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS);

        // 이미지에서 텍스트 인식
        recognizer.process(inputImage)
                .addOnSuccessListener(visionText -> {
                    // 텍스트 인식 성공 시 처리
                    for (Text.TextBlock block : visionText.getTextBlocks()) {
                        String recognizedText = block.getText();
                        // 인식된 텍스트를 처리하거나 UI에 표시할 수 있음
                        Toast.makeText(MainActivity.this, recognizedText, Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    // 텍스트 인식 실패 시 처리
                    Toast.makeText(MainActivity.this, "텍스트 인식 실패", Toast.LENGTH_SHORT).show();
                })
                .addOnCompleteListener(task -> {
                    // 이미지 분석이 완료되면 이미지 리소스를 해제
                    imageProxy.close();
                });
    }
}

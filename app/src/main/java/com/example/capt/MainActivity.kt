package com.example.capt

import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.google.cloud.vision.v1.AnnotateImageRequest
import com.google.cloud.vision.v1.Image
import com.google.cloud.vision.v1.ImageAnnotatorClient
import com.google.cloud.vision.v1.Feature
import com.google.protobuf.ByteString
import java.io.File

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        System.setProperty("GOOGLE_APPLICATION_CREDENTIALS", "C:/kkk/kk22.json")

        val textButton: Button = findViewById(R.id.buttonTextRecognition)
        val objectButton: Button = findViewById(R.id.buttonObjectRecognition)

        textButton.setOnClickListener {
            onlyText("C:/kkk/new.jpg")
        }

        objectButton.setOnClickListener {
            onlyObject("C:/kkk/new.jpg")
        }
    }

    private fun onlyText(imagePath: String) {
        val imageBytes = File(imagePath).readBytes()
        val imgBytes = ByteString.copyFrom(imageBytes)

        val img = Image.newBuilder().setContent(imgBytes).build()
        val feature = Feature.newBuilder().setType(Feature.Type.TEXT_DETECTION).build()
        val request = AnnotateImageRequest.newBuilder().addFeatures(feature).setImage(img).build()

        ImageAnnotatorClient.create().use { client ->
            val response = client.batchAnnotateImages(listOf(request))
            val texts = response.responsesList[0].textAnnotationsList

            if (texts.isNotEmpty()) {
                println("텍스트 인식 결과: ${texts[0].description}")
            } else {
                println("인식된 텍스트가 없습니다.")
            }
        }
    }

    private fun onlyObject(imagePath: String) {
        val imageBytes = File(imagePath).readBytes()
        val imgBytes = ByteString.copyFrom(imageBytes)

        val img = Image.newBuilder().setContent(imgBytes).build()
        val feature = Feature.newBuilder().setType(Feature.Type.LABEL_DETECTION).build()
        val request = AnnotateImageRequest.newBuilder().addFeatures(feature).setImage(img).build()

        ImageAnnotatorClient.create().use { client ->
            val response = client.batchAnnotateImages(listOf(request))
            val labels = response.responsesList[0].labelAnnotationsList

            if (labels.isNotEmpty()) {
                labels.forEach { label ->
                    println("인식된 객체: ${label.description}")
                }
            } else {
                println("인식된 객체가 없습니다.")
            }
        }
    }
}

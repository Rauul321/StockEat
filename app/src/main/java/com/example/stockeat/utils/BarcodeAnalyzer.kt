package com.example.stockeat.utils

import androidx.annotation.OptIn
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.common.InputImage

/**
 * Analyzer class for CameraX that processes frames to detect barcodes using ML Kit.
 *
 * It uses [BarcodeScanning] to identify barcodes in the camera feed and triggers
 * the provided callback when a valid code is found.
 *
 * @property onBarcodeDetected Callback function invoked with the raw value of the detected barcode.
 */
class BarcodeAnalyzer(private val onBarcodeDetected: (String) -> Unit) : ImageAnalysis.Analyzer {
    private val scanner = BarcodeScanning.getClient()
    /** Flag to prevent multiple detections of the same barcode in rapid succession. */
    private var isScanning = true

    /**
     * Analyzes an image frame from the camera.
     *
     * Converts the [ImageProxy] to an [InputImage] and processes it with the ML Kit scanner.
     * If a barcode is found and [isScanning] is true, it disables further scanning and
     * invokes the callback.
     *
     * @param imageProxy The image frame to analyze.
     */
    @OptIn(ExperimentalGetImage::class)
    override fun analyze(imageProxy: ImageProxy) {
        if (!isScanning) {
            imageProxy.close()
            return
        }

        val mediaImage = imageProxy.image
        if (mediaImage != null) {
            val image = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)
            scanner.process(image)
                .addOnSuccessListener { barcodes ->
                    for (barcode in barcodes) {
                        barcode.rawValue?.let { 
                            isScanning = false
                            onBarcodeDetected(it)
                        }
                    }
                }
                .addOnCompleteListener {
                    imageProxy.close()
                }
        } else {
            imageProxy.close()
        }
    }
}

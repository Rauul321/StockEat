package com.example.stockeat.utils

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.stockeat.R
import com.example.stockeat.StockEatDatabase
import java.util.concurrent.TimeUnit

class ExpiryWorker(
    appContext: Context,
    workerParams: WorkerParameters,
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        val database = StockEatDatabase.getDatabase(applicationContext)
        val dao = database.ingredientDao()

        val ingredients = dao.getInventoryList()
        val now = System.currentTimeMillis()
        val twoDaysInMillis = TimeUnit.DAYS.toMillis(2)
        
        // LOG PARA DEPURAR
        android.util.Log.d("ExpiryWorker", "Revisando ${ingredients.size} productos. Ahora: $now")

        val expiringSoon = ingredients.filter { ingredient ->
            if (ingredient.expiryDate == null) return@filter false
            val diff = ingredient.expiryDate - now
            android.util.Log.d("ExpiryWorker", "Producto: ${ingredient.nombre}, Dif: $diff ms")
            diff in -86400000..twoDaysInMillis // Aceptamos desde hace 1 día hasta dentro de 2
        }

        if(expiringSoon.isNotEmpty()) {
            val names = expiringSoon.joinToString(", ") { it.nombre }
            showNotification("¡Atención! Productos por caducar", "Los siguientes productos caducan pronto o ya han caducado: $names")
        } else {
            // Notificación de prueba para confirmar que el sistema funciona aunque no haya productos
            showNotification("StockEat", "Revisión diaria completada. No hay productos por caducar.")
        }
        return Result.success()
    }

    @SuppressLint("ServiceCast")
    private fun showNotification(title: String, message: String) {
        val notificationManager = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channelId = "expiry_notifications"

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Avisos de Caducidad",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Notificaciones para productos próximos a caducar"
            }
            notificationManager.createNotificationChannel(channel)
        }

        val notification = NotificationCompat.Builder(applicationContext, channelId)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .build()

        notificationManager.notify(1, notification)
    }

}
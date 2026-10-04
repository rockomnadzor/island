package com.i.dynamic.island

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.PixelFormat
import android.os.Build
import android.os.IBinder
import android.view.Gravity
import android.view.WindowManager
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

class DynamicIslandService : Service() {

    private lateinit var windowManager: WindowManager
    private var islandView: ComposeView? = null
    private lateinit var params: WindowManager.LayoutParams
    private lateinit var prefs: SharedPreferences

    companion object {
        const val CHANNEL_ID = "dynamic_island_channel"
        const val NOTIFICATION_ID = 1

        const val KEY_ISLAND_TYPE = "island_type"
        const val KEY_WIDTH = "island_width"
        const val KEY_HEIGHT = "island_height"
        const val KEY_POS_X = "pos_x"
        const val KEY_POS_Y = "pos_y"
        const val KEY_IS_DRAGGABLE = "is_draggable"

        const val TYPE_PILL = 0
        const val TYPE_CIRCLE = 1
        const val TYPE_CAPSULE = 2

        const val DEFAULT_WIDTH = 200
        const val DEFAULT_HEIGHT = 50
    }

    override fun onCreate() {
        super.onCreate()
        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager
        prefs = getSharedPreferences("dynamic_island_prefs", Context.MODE_PRIVATE)
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val notification = createNotification()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            startForeground(NOTIFICATION_ID, notification, android.content.pm.ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE)
        } else {
            startForeground(NOTIFICATION_ID, notification)
        }

        intent?.let { updateSettingsFromIntent(it) }

        showIsland()
        return START_STICKY
    }

    private fun updateSettingsFromIntent(intent: Intent) {
        val editor = prefs.edit()

        intent.getIntExtra(KEY_ISLAND_TYPE, -1).let { if (it >= 0) editor.putInt(KEY_ISLAND_TYPE, it) }
        intent.getIntExtra(KEY_WIDTH, -1).let { if (it > 0) editor.putInt(KEY_WIDTH, it) }
        intent.getIntExtra(KEY_HEIGHT, -1).let { if (it > 0) editor.putInt(KEY_HEIGHT, it) }
        intent.getBooleanExtra(KEY_IS_DRAGGABLE, false).let { editor.putBoolean(KEY_IS_DRAGGABLE, it) }

        editor.apply()
    }

    private fun showIsland() {
        islandView?.let {
            try {
                windowManager.removeView(it)
            } catch (e: Exception) {}
        }

        val islandType = prefs.getInt(KEY_ISLAND_TYPE, TYPE_PILL)
        val width = prefs.getInt(KEY_WIDTH, DEFAULT_WIDTH)
        val height = prefs.getInt(KEY_HEIGHT, DEFAULT_HEIGHT)
        val posX = prefs.getInt(KEY_POS_X, 0)
        val posY = prefs.getInt(KEY_POS_Y, 50)
        val isDraggable = prefs.getBoolean(KEY_IS_DRAGGABLE, true)

        params = WindowManager.LayoutParams(
            width,
            height,
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
            else
                WindowManager.LayoutParams.TYPE_PHONE,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
            WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.START
            x = posX
            y = posY
        }

        val composeView = ComposeView(this).apply {
            setContent {
                DynamicIslandContent(
                    islandType = islandType,
                    isDraggable = isDraggable,
                    onDrag = { dx, dy ->
                        params.x += dx.toInt()
                        params.y += dy.toInt()
                        windowManager.updateViewLayout(this, params)

                        prefs.edit()
                            .putInt(KEY_POS_X, params.x)
                            .putInt(KEY_POS_Y, params.y)
                            .apply()
                    }
                )
            }
        }

        islandView = composeView
        windowManager.addView(composeView, params)
    }

    @Composable
    fun DynamicIslandContent(
        islandType: Int,
        isDraggable: Boolean,
        onDrag: (Float, Float) -> Unit
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(isDraggable) {
                    if (isDraggable) {
                        detectDragGestures(
                            onDrag = { change, dragAmount ->
                                change.consume()
                                onDrag(dragAmount.x, dragAmount.y)
                            }
                        )
                    }
                },
            contentAlignment = Alignment.Center
        ) {
            when (islandType) {
                TYPE_PILL -> PillIsland()
                TYPE_CIRCLE -> CircleIsland()
                TYPE_CAPSULE -> CapsuleIsland()
                else -> PillIsland()
            }
        }
    }

    @Composable
    fun PillIsland() {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clip(RoundedCornerShape(50))
                .background(Color.Black),
            contentAlignment = Alignment.CenterEnd
        ) {
            Box(
                modifier = Modifier
                    .padding(end = 12.dp)
                    .size(24.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF1a1a1a)),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(12.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF0d0d0d))
                )
            }
        }
    }

    @Composable
    fun CircleIsland() {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clip(CircleShape)
                .background(Color.Black),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .size(30.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF1a1a1a)),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(15.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF0d0d0d))
                )
            }
        }
    }

    @Composable
    fun CapsuleIsland() {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clip(RoundedCornerShape(25))
                .background(Color.Black),
            contentAlignment = Alignment.Center
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(20.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF1a1a1a))
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Island",
                    color = Color.White,
                    fontSize = 12.sp
                )
            }
        }
    }

    private fun createNotificationChannel() {
        val channel = NotificationChannel(
            CHANNEL_ID,
            "Dynamic Island Service",
            NotificationManager.IMPORTANCE_LOW
        ).apply {
            description = "Keeps Dynamic Island running"
            setShowBadge(false)
        }

        val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(channel)
    }

    private fun createNotification(): Notification {
        val intent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        return Notification.Builder(this, CHANNEL_ID)
            .setContentTitle("Dynamic Island активен")
            .setContentText("Нажми для настройки")
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .build()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        super.onDestroy()
        islandView?.let {
            try {
                windowManager.removeView(it)
            } catch (e: Exception) {}
        }
    }
}

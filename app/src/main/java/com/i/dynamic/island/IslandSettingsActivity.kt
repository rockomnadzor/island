package com.i.dynamic.island

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

class IslandSettingsActivity : ComponentActivity() {
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        val prefs = getSharedPreferences("dynamic_island_prefs", MODE_PRIVATE)
        
        setContent {
            MaterialTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = Color(0xFF1A1A2E)
                ) {
                    SettingsScreen(
                        prefs = prefs,
                        onApply = { type, width, height, draggable ->
                            val intent = Intent(this, DynamicIslandService::class.java).apply {
                                putExtra(DynamicIslandService.KEY_ISLAND_TYPE, type)
                                putExtra(DynamicIslandService.KEY_WIDTH, width)
                                putExtra(DynamicIslandService.KEY_HEIGHT, height)
                                putExtra(DynamicIslandService.KEY_IS_DRAGGABLE, draggable)
                            }
                            startService(intent)
                            finish()
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun SettingsScreen(
    prefs: android.content.SharedPreferences,
    onApply: (Int, Int, Int, Boolean) -> Unit
) {
    var selectedType by remember { mutableStateOf(prefs.getInt(DynamicIslandService.KEY_ISLAND_TYPE, 0)) }
    var width by remember { mutableStateOf(prefs.getInt(DynamicIslandService.KEY_WIDTH, 200).toFloat()) }
    var height by remember { mutableStateOf(prefs.getInt(DynamicIslandService.KEY_HEIGHT, 50).toFloat()) }
    var isDraggable by remember { mutableStateOf(prefs.getBoolean(DynamicIslandService.KEY_IS_DRAGGABLE, true)) }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(20.dp)
    ) {
        Text(
            "⚙️ Настройки Dynamic Island",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Выбор типа острова
        Text("Тип острова:", color = Color.White, fontSize = 16.sp)
        Spacer(modifier = Modifier.height(12.dp))
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            IslandTypeOption(
                type = 0,
                name = "Таблетка",
                selected = selectedType == 0,
                onSelect = { selectedType = 0 }
            )
            IslandTypeOption(
                type = 1,
                name = "Круг",
                selected = selectedType == 1,
                onSelect = { selectedType = 1 }
            )
            IslandTypeOption(
                type = 2,
                name = "Капсула",
                selected = selectedType == 2,
                onSelect = { selectedType = 2 }
            )
        }
        
        Spacer(modifier = Modifier.height(32.dp))
        
        // Размер
        Text("Ширина: ${width.toInt()}dp", color = Color.White, fontSize = 16.sp)
        Slider(
            value = width,
            onValueChange = { width = it },
            valueRange = 100f..400f,
            modifier = Modifier.fillMaxWidth()
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text("Высота: ${height.toInt()}dp", color = Color.White, fontSize = 16.sp)
        Slider(
            value = height,
            onValueChange = { height = it },
            valueRange = 30f..150f,
            modifier = Modifier.fillMaxWidth()
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Переключатель перетаскивания
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("Можно перетаскивать", color = Color.White, fontSize = 16.sp)
            Switch(
                checked = isDraggable,
                onCheckedChange = { isDraggable = it }
            )
        }
        
        Spacer(modifier = Modifier.height(32.dp))
        
        // Предпросмотр
        Text("Предпросмотр:", color = Color.White, fontSize = 16.sp)
        Spacer(modifier = Modifier.height(12.dp))
        
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(80.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(Color(0xFF16213E)),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .width(width.dp)
                    .height(height.dp)
                    .clip(
                        when (selectedType) {
                            0 -> RoundedCornerShape(50)
                            1 -> CircleShape
                            else -> RoundedCornerShape(25)
                        }
                    )
                    .background(Color.Black),
                contentAlignment = Alignment.Center
            ) {
                if (selectedType == 0) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.CenterEnd)
                            .padding(end = 8.dp)
                            .size((height * 0.5f).dp)
                            .clip(CircleShape)
                            .background(Color(0xFF333333))
                    )
                } else if (selectedType == 1) {
                    Box(
                        modifier = Modifier
                            .size((height * 0.6f).dp)
                            .clip(CircleShape)
                            .background(Color(0xFF333333))
                    )
                }
            }
        }
        
        Spacer(modifier = Modifier.height(32.dp))
        
        Button(
            onClick = { onApply(selectedType, width.toInt(), height.toInt(), isDraggable) },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50))
        ) {
            Text("✅ Применить", fontSize = 18.sp)
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            "💡 Совет: После применения перетащи остров зажав его пальцем",
            color = Color.Gray,
            fontSize = 12.sp
        )
    }
}

@Composable
fun IslandTypeOption(
    type: Int,
    name: String,
    selected: Boolean,
    onSelect: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.clickable { onSelect() }
    ) {
        Box(
            modifier = Modifier
                .size(60.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(if (selected) Color(0xFF4CAF50) else Color(0xFF16213E))
                .border(
                    width = 2.dp,
                    color = if (selected) Color.White else Color.Transparent,
                    shape = RoundedCornerShape(12.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp, 20.dp)
                    .clip(
                        when (type) {
                            0 -> RoundedCornerShape(50)
                            1 -> CircleShape
                            else -> RoundedCornerShape(10)
                        }
                    )
                    .background(Color.Black)
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(name, color = if (selected) Color(0xFF4CAF50) else Color.Gray, fontSize = 12.sp)
    }
}

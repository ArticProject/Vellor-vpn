package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog

@Composable
fun VellorNoInternetDialog(
    isRussian: Boolean = true,
    onDismissRequest: () -> Unit,
    onRetry: () -> Unit
) {
    Dialog(onDismissRequest = onDismissRequest) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .clip(RoundedCornerShape(28.dp))
                .testTag("no_internet_dialog"),
            color = Color.White,
            shape = RoundedCornerShape(28.dp),
            tonalElevation = 8.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 28.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // 8-dots morphing animation 1-in-1
                VellorMorphingDotsAnimation(
                    modifier = Modifier
                        .padding(top = 8.dp, bottom = 16.dp)
                        .testTag("morphing_dots_animation"),
                    dotColor = Color(0xFF000000),
                    size = 130.dp,
                    dotRadiusDp = 5.5.dp
                )

                Text(
                    text = if (isRussian) "Нет подключения к интернету" else "No internet connection",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF141416),
                    textAlign = TextAlign.Center,
                    lineHeight = 26.sp
                )

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = if (isRussian)
                        "Проверьте доступ к сети Wi-Fi или сотовой связи и повторите попытку подключения к узлам Vellor."
                    else
                        "Please check your Wi-Fi or cellular network connection and try reconnecting to Vellor nodes.",
                    fontSize = 14.sp,
                    color = Color(0xFF71717A),
                    textAlign = TextAlign.Center,
                    lineHeight = 20.sp,
                    modifier = Modifier.padding(horizontal = 8.dp)
                )

                Spacer(modifier = Modifier.height(26.dp))

                Button(
                    onClick = onRetry,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .testTag("try_again_button"),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF141416),
                        contentColor = Color.White
                    )
                ) {
                    Text(
                        text = if (isRussian) "Повторить попытку" else "Try again",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}

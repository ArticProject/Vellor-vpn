package com.example.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ColorMatrix
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.R
import com.example.model.ServerLocation

fun getLocalCityDrawable(context: android.content.Context, cityCode: String): Int? {
    val name = when (cityCode.uppercase()) {
        "TYO" -> "img_city_tokyo"
        "LON" -> "img_city_london"
        "PAR" -> "img_city_paris"
        "BER" -> "img_city_berlin"
        "AMS" -> "img_city_amsterdam"
        "HEL" -> "img_city_helsinki"
        "RKV" -> "img_city_reykjavik"
        "WAS", "DC", "NYC" -> "img_city_washington"
        "ZRH" -> "img_city_zurich"
        "FRA" -> "img_city_frankfurt"
        "WAW" -> "img_city_warsaw"
        else -> "img_city_helsinki"
    }
    val resId = context.resources.getIdentifier(name, "drawable", context.packageName)
    return if (resId != 0) resId else null
}

@Composable
fun CapitalCityPhotoView(
    server: ServerLocation,
    modifier: Modifier = Modifier,
    darkenFactor: Float = 0.35f,
    showCoordinates: Boolean = false
) {
    val context = LocalContext.current
    val monochromeMatrix = remember {
        ColorMatrix().apply {
            setToSaturation(0f)
        }
    }

    val localDrawable = remember(server.cityCode) {
        getLocalCityDrawable(context, server.cityCode)
    }

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .background(Color(0xFF18181B))
    ) {
        if (localDrawable != null) {
            Image(
                painter = painterResource(id = localDrawable),
                contentDescription = "${server.city} architectural photo",
                contentScale = ContentScale.Crop,
                colorFilter = ColorFilter.colorMatrix(monochromeMatrix),
                modifier = Modifier.fillMaxSize()
            )
        } else if (server.photoUrl.isNotBlank()) {
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(server.photoUrl)
                    .crossfade(true)
                    .build(),
                contentDescription = "${server.city} architectural photo",
                contentScale = ContentScale.Crop,
                colorFilter = ColorFilter.colorMatrix(monochromeMatrix),
                modifier = Modifier.fillMaxSize()
            )
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            Color.Black.copy(alpha = darkenFactor * 0.45f),
                            Color.Transparent,
                            Color.Black.copy(alpha = darkenFactor + 0.40f)
                        )
                    )
                )
        )
    }
}

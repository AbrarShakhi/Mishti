package com.abrarshakhi.mishti.common.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialShapes
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.toShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.abrarshakhi.mishti.R

/** The scalloped "cookie" outline Mishti's mark sits in. */
val CookieShape: Shape
    @OptIn(ExperimentalMaterial3ExpressiveApi::class)
    @Composable
    get() = MaterialShapes.Cookie9Sided.toShape()

/** The app icon set in a cookie shape, used wherever Mishti itself is speaking. */
@Composable
fun AppMark(
    modifier: Modifier = Modifier,
    size: Dp = 40.dp,
    containerColor: Color = MaterialTheme.colorScheme.primaryContainer,
) {
    Surface(
        modifier = modifier.size(size),
        shape = CookieShape,
        color = containerColor,
    ) {
        Image(
            painter = painterResource(R.mipmap.ic_launcher_foreground),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
        )
    }
}

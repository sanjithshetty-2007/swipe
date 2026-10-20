package com.swipeit.app.core.designsystem.component

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.tooling.preview.Preview
import com.swipeit.app.R
import com.swipeit.app.core.designsystem.SwipeItTextStyles
import com.swipeit.app.core.designsystem.SwipeItTheme

/** "SwipeIt" wordmark in the bundled handwritten font with a green glow. */
@Composable
fun SwipeItLogo(
    modifier: Modifier = Modifier,
    style: TextStyle = SwipeItTextStyles.Logo,
) {
    Text(
        text = stringResource(R.string.app_name),
        style = style,
        modifier = modifier,
    )
}

@Preview(showBackground = true, backgroundColor = 0xFF0B0B10)
@Composable
private fun SwipeItLogoPreview() {
    SwipeItTheme {
        Column {
            SwipeItLogo()
            SwipeItLogo(style = SwipeItTextStyles.LogoLarge)
        }
    }
}

package com.example.medimart.ui.components

import android.graphics.Color as AndroidColor
import android.text.method.LinkMovementMethod
import android.util.TypedValue
import android.widget.TextView
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.text.HtmlCompat

/**
 * Displays trusted catalog HTML using Android's supported text spans.
 * Unsupported markup is ignored, and no scripts or remote page content are executed.
 */
@Composable
fun HtmlText(
    html: String,
    modifier: Modifier = Modifier,
    color: Color = LocalContentColor.current,
    linkColor: Color = MaterialTheme.colorScheme.primary
) {
    val textStyle = LocalTextStyle.current

    AndroidView(
        modifier = modifier,
        factory = { context ->
            TextView(context).apply {
                setBackgroundColor(AndroidColor.TRANSPARENT)
                includeFontPadding = false
                linksClickable = true
                movementMethod = LinkMovementMethod.getInstance()
                setLineSpacing(0f, 1.2f)
            }
        },
        update = { textView ->
            textView.text = HtmlCompat.fromHtml(html, HtmlCompat.FROM_HTML_MODE_COMPACT)
            textView.setTextColor(color.toArgb())
            textView.setLinkTextColor(linkColor.toArgb())
            if (textStyle.fontSize.value > 0f) {
                textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, textStyle.fontSize.value)
            }
        }
    )
}

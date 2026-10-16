package com.example.weather.ui.insets

import android.view.View
import android.view.ViewGroup
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.doOnAttach
import androidx.core.view.updatePadding
import kotlin.math.max

private data class ViewPadding(
    val left: Int,
    val top: Int,
    val right: Int,
    val bottom: Int,
)

private fun View.currentPadding(): ViewPadding = ViewPadding(
    left = paddingLeft,
    top = paddingTop,
    right = paddingRight,
    bottom = paddingBottom,
)

internal fun ViewGroup.applyScreenInsets(
    topBar: View,
    bottomContent: View,
    includeIme: Boolean = false,
) {
    val rootPadding = currentPadding()
    val topBarPadding = topBar.currentPadding()
    val topBarMinimumHeight = topBar.minimumHeight
    val bottomContentPadding = bottomContent.currentPadding()

    ViewCompat.setOnApplyWindowInsetsListener(this) { _, windowInsets ->
        val safeDrawingInsets = windowInsets.getInsets(
            WindowInsetsCompat.Type.systemBars() or
                WindowInsetsCompat.Type.displayCutout(),
        )
        val imeBottomInset = if (includeIme) {
            windowInsets.getInsets(WindowInsetsCompat.Type.ime()).bottom
        } else {
            0
        }

        updatePadding(
            left = rootPadding.left + safeDrawingInsets.left,
            top = rootPadding.top,
            right = rootPadding.right + safeDrawingInsets.right,
            bottom = rootPadding.bottom,
        )
        topBar.updatePadding(
            left = topBarPadding.left,
            top = topBarPadding.top + safeDrawingInsets.top,
            right = topBarPadding.right,
            bottom = topBarPadding.bottom,
        )
        topBar.minimumHeight = topBarMinimumHeight + safeDrawingInsets.top
        bottomContent.updatePadding(
            left = bottomContentPadding.left,
            top = bottomContentPadding.top,
            right = bottomContentPadding.right,
            bottom = bottomContentPadding.bottom + max(
                safeDrawingInsets.bottom,
                imeBottomInset,
            ),
        )

        WindowInsetsCompat.CONSUMED
    }

    doOnAttach { attachedView ->
        ViewCompat.requestApplyInsets(attachedView)
    }
}

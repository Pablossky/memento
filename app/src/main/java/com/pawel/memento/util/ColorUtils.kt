package com.pawel.memento.util

import com.pawel.memento.data.model.Priority

object ColorUtils {
    val PASTEL_COLORS = intArrayOf(
        0xFFFFB3BA.toInt(), 0xFFFFDFBA.toInt(), 0xFFFFFFBA.toInt(), 0xFFBAFFC9.toInt(),
        0xFFBAE1FF.toInt(), 0xFFE8BAFF.toInt(), 0xFFC8F5ED.toInt(), 0xFFFFCCE5.toInt()
    )
    val CATEGORY_COLORS = intArrayOf(
        0xFFEF9A9A.toInt(), 0xFFFFCC80.toInt(), 0xFFFFF176.toInt(), 0xFFA5D6A7.toInt(),
        0xFF80DEEA.toInt(), 0xFF90CAF9.toInt(), 0xFFCE93D8.toInt(), 0xFFF48FB1.toInt(),
        0xFFBCAAA4.toInt(), 0xFFB0BEC5.toInt()
    )
    val PRIORITY_COLORS = mapOf(
        Priority.LOW to 0xFF81C784.toInt(), Priority.MEDIUM to 0xFFFFB74D.toInt(),
        Priority.HIGH to 0xFFE57373.toInt(), Priority.URGENT to 0xFFB71C1C.toInt()
    )
}

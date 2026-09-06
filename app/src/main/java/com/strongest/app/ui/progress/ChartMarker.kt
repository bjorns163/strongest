package com.strongest.app.ui.progress

import android.content.Context
import android.graphics.drawable.GradientDrawable
import android.widget.TextView
import com.github.mikephil.charting.components.MarkerView
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.highlight.Highlight
import com.github.mikephil.charting.utils.MPPointF
import com.strongest.app.R

/**
 * The tooltip that appears above a selected chart point, so a value can be read exactly
 * instead of estimated off the axis. [labelFor] turns the selected entry into its text.
 */
class ChartMarkerView(
    context: Context,
    textColor: Int,
    backgroundColor: Int,
    strokeColor: Int,
    private val labelFor: (Entry, Highlight?) -> String
) : MarkerView(context, R.layout.chart_marker) {

    private val label: TextView = findViewById(R.id.chartMarkerText)

    init {
        label.setTextColor(textColor)
        (label.background as? GradientDrawable)?.apply {
            mutate()
            setColor(backgroundColor)
            setStroke(2, strokeColor)
        }
    }

    override fun refreshContent(e: Entry?, highlight: Highlight?) {
        label.text = e?.let { labelFor(it, highlight) }.orEmpty()
        super.refreshContent(e, highlight)
    }

    // Sit centred above the point rather than under the fingertip.
    override fun getOffset(): MPPointF = MPPointF(-(width / 2f), -height.toFloat() - 12f)
}

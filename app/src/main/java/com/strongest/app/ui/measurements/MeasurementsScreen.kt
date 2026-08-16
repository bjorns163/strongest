package com.strongest.app.ui.measurements

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.strongest.app.data.db.LatestMeasurement
import com.strongest.app.data.model.BodyMetric
import com.strongest.app.data.model.MetricCategory
import com.strongest.app.data.repository.WeightUnit
import java.text.SimpleDateFormat
import java.util.Date

@Composable
fun MeasurementsScreen(
    onBack: () -> Unit = {},
    onMetricClick: (BodyMetric) -> Unit = {},
    viewModel: MeasurementsListViewModel = hiltViewModel()
) {
    val latest by viewModel.latestByMetric.collectAsState()
    val weightUnit by viewModel.weightUnit.collectAsState()

    Scaffold { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Default.ArrowBack, "Back")
                    }
                    Text(
                        text = "Body Measurements",
                        style = MaterialTheme.typography.titleLarge,
                        modifier = Modifier.padding(start = 4.dp)
                    )
                }
            }

            val grouped = BodyMetric.entries.groupBy { it.category }
            for (category in MetricCategory.entries) {
                val metrics = grouped[category] ?: continue
                item(key = "cat-${category.name}") {
                    Text(
                        text = category.displayName,
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                    )
                }
                items(metrics.size, key = { metrics[it].name }) { idx ->
                    val metric = metrics[idx]
                    MetricRow(
                        metric = metric,
                        latest = latest[metric],
                        weightUnit = weightUnit,
                        onClick = { onMetricClick(metric) }
                    )
                }
            }
        }
    }
}

@Composable
private fun MetricRow(
    metric: BodyMetric,
    latest: LatestMeasurement?,
    weightUnit: WeightUnit,
    onClick: () -> Unit
) {
    val dateFormat = SimpleDateFormat("MMM d, yyyy", LocalConfiguration.current.locales[0])
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp)
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = metric.displayName,
                    style = MaterialTheme.typography.bodyLarge
                )
                Text(
                    text = if (latest != null) {
                        "${formatMetricValue(metric, latest.value, weightUnit)} · ${dateFormat.format(Date(latest.timestamp))} · ${latest.entryCount} entries"
                    } else {
                        "No entries yet"
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Icon(Icons.AutoMirrored.Default.KeyboardArrowRight, null)
        }
    }
}

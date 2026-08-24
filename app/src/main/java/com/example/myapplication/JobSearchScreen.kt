package com.example.myapplication

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.ui.Alignment
import androidx.compose.material3.FilledTonalButton

@Composable
fun JobSearchScreen(
    majors: List<Major>,
    initialJob: String? = null,
    onInitialJobConsumed: () -> Unit = {},
    onReturn: (() -> Unit)? = null,
    onMajorClick: (String) -> Unit
) {
    val listState = rememberLazyListState()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val allJobs = remember { MajorRepository.loadJobs(context) }
    val allDetails = remember { MajorRepository.loadAll(context) }

    var query by remember { mutableStateOf("") }
    var selectedJob by remember {
        mutableStateOf(
            if (initialJob != null) allJobs.find { it.job == initialJob } else null
        )
    }
    var selectedProfession by remember { mutableStateOf<String?>(null) }

    val favJobs by FavoriteStore.favorites(context, FavoriteType.JOB)
        .collectAsState(initial = emptySet())

    BackHandler(enabled = selectedJob != null) {
        if (onReturn != null) onReturn() else selectedJob = null
    }

    LaunchedEffect(Unit) {
        if (initialJob != null) onInitialJobConsumed()
    }

    val professions = remember(allJobs) {
        allJobs.mapNotNull { it.profession }
            .filter { it.isNotBlank() }
            .distinct()
            .sorted()
    }

    val visibleJobs = remember(query, selectedProfession, allJobs) {
        allJobs.filter { job ->
            val name = job.job ?: return@filter false
            val matchQuery = query.isBlank() || name.contains(query)
            val matchProfession = selectedProfession == null ||
                    job.profession == selectedProfession
            matchQuery && matchProfession
        }.sortedBy { it.job }
    }

    val relatedMajors = remember(selectedJob, allDetails) {
        val jobName = selectedJob?.job ?: return@remember emptyList()
        allDetails.filter { it.job?.contains(jobName) == true }
    }

    val current = selectedJob
    if (current == null) {
        LazyColumn {
            item {
                OutlinedTextField(
                    value = query,
                    onValueChange = { query = it },
                    label = { Text("직업 검색") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                )

                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    item {
                        FilterChip(
                            selected = selectedProfession == null,
                            onClick = { selectedProfession = null },
                            label = { Text("전체") }
                        )
                    }
                    items(professions) { profession ->
                        FilterChip(
                            selected = selectedProfession == profession,
                            onClick = {
                                selectedProfession =
                                    if (selectedProfession == profession) null else profession
                            },
                            label = { Text(profession) }
                        )
                    }
                }

                Text(
                    text = "${visibleJobs.size}개 직업",
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )
            }

            items(visibleJobs) { job ->
                Card(
                    onClick = { selectedJob = job },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = job.job ?: "",
                            style = MaterialTheme.typography.titleMedium
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            job.salery?.takeIf { it.isNotBlank() }?.let {
                                Text(it, style = MaterialTheme.typography.bodySmall)
                            }
                            job.possibility?.takeIf { it.isNotBlank() }?.let {
                                Text("전망 $it", style = MaterialTheme.typography.bodySmall)
                            }
                        }
                    }
                }
            }
        }
    } else {
        LazyColumn(
            state = listState,
            modifier = Modifier.padding(16.dp)
        ) {
            item {
                DetailHeader(
                    isFavorite = (current.job ?: "") in favJobs,
                    onBack = {
                        if (onReturn != null) onReturn() else selectedJob = null
                    },
                    onToggleFavorite = {
                        current.job?.let { name ->
                            scope.launch {
                                FavoriteStore.toggle(context, FavoriteType.JOB, name)
                            }
                        }
                    }
                )
                Spacer(modifier = Modifier.height(16.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = current.job ?: "",
                        style = MaterialTheme.typography.headlineMedium,
                        modifier = Modifier.weight(1f)
                    )
                    if (relatedMajors.isNotEmpty()) {
                        FilledTonalButton(
                            onClick = {
                                scope.launch {
                                    listState.animateScrollToItem(1)
                                }
                            }
                        ) {
                            Text("관련 학과 ${relatedMajors.size}")
                        }
                    }
                }
                current.profession?.takeIf { it.isNotBlank() }?.let {
                    Text(text = it, style = MaterialTheme.typography.bodyLarge)
                }
                Spacer(modifier = Modifier.height(16.dp))

                DetailSection("하는 일", current.summary?.cleanHtml())
                DetailSection("평균 연봉", current.salery)
                DetailSection("일자리 전망", current.possibility)
                DetailSection("비슷한 직업", current.similarJob?.cleanHtml())

                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "관련 학과 ${relatedMajors.size}개",
                    style = MaterialTheme.typography.titleMedium
                )
                Spacer(modifier = Modifier.height(8.dp))
            }

            items(relatedMajors) { major ->
                Card(
                    onClick = { major.major?.let { onMajorClick(it) } },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = major.major ?: "",
                            style = MaterialTheme.typography.titleMedium
                        )
                        major.employment?.cleanHtml()?.let {
                            Text("취업률 $it", style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }
            }
        }
    }
}
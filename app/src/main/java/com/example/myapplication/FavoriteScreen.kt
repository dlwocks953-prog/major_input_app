package com.example.myapplication

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp

@Composable
fun FavoriteScreen(
    majors: List<Major>,
    onMajorClick: (String) -> Unit,
    onJobClick: (String) -> Unit,
    onSchoolClick: (String) -> Unit
) {
    val context = LocalContext.current

    val favMajors by FavoriteStore.favorites(context, FavoriteType.MAJOR)
        .collectAsState(initial = emptySet())
    val favJobs by FavoriteStore.favorites(context, FavoriteType.JOB)
        .collectAsState(initial = emptySet())
    val favSchools by FavoriteStore.favorites(context, FavoriteType.SCHOOL)
        .collectAsState(initial = emptySet())

    val allJobs = remember { MajorRepository.loadJobs(context) }
    val allSchools = remember { MajorRepository.loadSchools(context) }

    val majorItems = remember(favMajors, majors) {
        majors.filter { it.name in favMajors }.sortedBy { it.name }
    }

    val jobItems = remember(favJobs, allJobs) {
        allJobs.filter { it.job in favJobs }.sortedBy { it.job }
    }

    val schoolItems = remember(favSchools, allSchools) {
        allSchools.filter { it.schoolName in favSchools }
            .distinctBy { it.schoolName }
            .sortedBy { it.schoolName }
    }

    val isEmpty = majorItems.isEmpty() && jobItems.isEmpty() && schoolItems.isEmpty()

    if (isEmpty) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "관심 목록이 비어 있습니다",
                style = MaterialTheme.typography.titleMedium
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "학과·직업·대학 상세 화면에서 ☆ 관심 버튼을 눌러 추가해보세요.",
                style = MaterialTheme.typography.bodyMedium
            )
        }
    } else {
        LazyColumn {
            if (majorItems.isNotEmpty()) {
                item {
                    SectionHeader("관심 학과", majorItems.size)
                }
                items(majorItems) { major ->
                    MajorItem(
                        name = major.name,
                        field = major.field,
                        onClick = { onMajorClick(major.name) }
                    )
                }
            }

            if (jobItems.isNotEmpty()) {
                item {
                    SectionHeader("관심 직업", jobItems.size)
                }
                items(jobItems) { job ->
                    Card(
                        onClick = { job.job?.let { onJobClick(it) } },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 6.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = job.job ?: "",
                                style = MaterialTheme.typography.titleMedium
                            )
                            job.profession?.takeIf { it.isNotBlank() }?.let {
                                Text(text = it, style = MaterialTheme.typography.bodySmall)
                            }
                        }
                    }
                }
            }

            if (schoolItems.isNotEmpty()) {
                item {
                    SectionHeader("관심 대학", schoolItems.size)
                }
                items(schoolItems) { school ->
                    Card(
                        onClick = { school.schoolName?.let { onSchoolClick(it) } },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 6.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = school.schoolName ?: "",
                                style = MaterialTheme.typography.titleMedium
                            )
                            Text(
                                text = "${school.region} · ${school.schoolGubun}",
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

@Composable
private fun SectionHeader(title: String, count: Int) {
    Text(
        text = "$title ${count}개",
        style = MaterialTheme.typography.titleMedium,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
    )
}
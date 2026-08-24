package com.example.myapplication

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp

private val SUBJECTS = listOf(
    "국어", "영어", "수학",
    "물리", "화학", "생명과학", "지구과학",
    "한국사", "세계사", "지리", "경제", "정치와 법", "사회문화",
    "생활과 윤리", "윤리와 사상", "철학", "심리학",
    "정보", "기술", "가정", "음악", "미술", "체육",
    "제2외국어", "한문"
)

@Composable
fun SubjectScreen(
    majors: List<Major>,
    onMajorClick: (String) -> Unit
) {
    val context = LocalContext.current
    val allDetails = remember { MajorRepository.loadAll(context) }

    var picked by remember { mutableStateOf<Set<String>>(emptySet()) }

    val fieldOf = remember(majors) {
        majors.associate { it.name to it.field }
    }

    val results = remember(picked, allDetails) {
        if (picked.isEmpty()) {
            emptyList()
        } else {
            allDetails.filter { detail ->
                val text = buildString {
                    detail.relate_subject?.forEach { rs ->
                        append(rs.subject_description ?: "")
                        append(" ")
                    }
                }
                picked.all { text.contains(it) }
            }.sortedBy { it.major }
        }
    }

    LazyColumn {
        item {
            Text(
                text = "잘하거나 좋아하는 과목을 골라보세요",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(16.dp)
            )

            FlowRowChips(
                items = SUBJECTS,
                picked = picked,
                onToggle = { subject ->
                    picked = if (subject in picked) picked - subject else picked + subject
                }
            )

            Spacer(modifier = Modifier.height(8.dp))

            when {
                picked.isEmpty() -> {
                    Text(
                        text = "과목을 고르면 관련 학과를 찾아드려요.",
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(16.dp)
                    )
                }
                results.isEmpty() -> {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "조건에 맞는 학과가 없습니다",
                            style = MaterialTheme.typography.titleMedium
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "선택한 ${picked.size}개 과목을 모두 다루는 학과가 없어요.\n과목을 줄여서 다시 찾아보세요.",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
                else -> {
                    Text(
                        text = "선택한 과목을 모두 다루는 학과 ${results.size}개",
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                    )
                }
            }
        }

        items(results) { detail ->
            val name = detail.major ?: ""
            Card(
                onClick = { onMajorClick(name) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(text = name, style = MaterialTheme.typography.titleMedium)
                    fieldOf[name]?.let {
                        Text(text = it, style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
        }
    }
}

@Composable
private fun FlowRowChips(
    items: List<String>,
    picked: Set<String>,
    onToggle: (String) -> Unit
) {
    Column(modifier = Modifier.padding(horizontal = 16.dp)) {
        items.chunked(3).forEach { row ->
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                row.forEach { subject ->
                    FilterChip(
                        selected = subject in picked,
                        onClick = { onToggle(subject) },
                        label = { Text(subject) },
                        modifier = Modifier.weight(1f)
                    )
                }
                repeat(3 - row.size) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
            Spacer(modifier = Modifier.height(4.dp))
        }
    }
}
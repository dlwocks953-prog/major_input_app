package com.example.myapplication

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp

private data class Question(
    val text: String,
    val keywords: List<String>
)

private val QUESTIONS = listOf(
    Question("사람을 만나고 돕는 일이 좋다", listOf("사람", "돕", "봉사", "배려", "소통", "대인")),
    Question("수학이나 과학 문제를 푸는 게 재미있다", listOf("수학", "과학", "논리", "분석", "탐구")),
    Question("만들거나 조립하는 걸 좋아한다", listOf("제작", "만들", "기계", "실습", "손재주", "조작")),
    Question("그림, 음악, 글쓰기 같은 창작이 좋다", listOf("창의", "예술", "표현", "감성", "디자인", "상상")),
    Question("컴퓨터나 새로운 기술에 관심이 많다", listOf("컴퓨터", "프로그", "정보", "기술", "디지털", "소프트")),
    Question("사회 문제나 역사에 관심이 있다", listOf("사회", "역사", "문화", "정치", "법", "경제")),
    Question("자연이나 동식물을 좋아한다", listOf("자연", "동물", "식물", "환경", "생명", "생물")),
    Question("계획을 세우고 정리하는 걸 잘한다", listOf("계획", "관리", "꼼꼼", "체계", "조직", "정확")),
    Question("몸을 움직이는 활동을 좋아한다", listOf("체력", "운동", "활동", "건강", "신체"))
)

@Composable
fun AptitudeScreen(
    majors: List<Major>,
    onMajorClick: (String) -> Unit
) {
    val context = LocalContext.current
    val allDetails = remember { MajorRepository.loadAll(context) }

    var picked by remember { mutableStateOf<Set<Int>>(emptySet()) }
    var showResult by remember { mutableStateOf(false) }

    val fieldOf = remember(majors) {
        majors.associate { it.name to it.field }
    }

    val results = remember(picked, showResult, allDetails) {
        if (!showResult || picked.isEmpty()) {
            emptyList()
        } else {
            val keywords = picked.flatMap { QUESTIONS[it].keywords }
            allDetails.mapNotNull { detail ->
                val text = (detail.interest ?: "") + " " +
                        (detail.property ?: "") + " " +
                        (detail.summary ?: "")
                val score = keywords.count { text.contains(it) }
                if (score > 0) detail to score else null
            }.sortedByDescending { it.second }.take(30)
        }
    }

    if (!showResult) {
        LazyColumn {
            item {
                Text(
                    text = "나에게 맞는 학과 찾기",
                    style = MaterialTheme.typography.headlineSmall,
                    modifier = Modifier.padding(16.dp)
                )
                Text(
                    text = "해당하는 항목을 모두 골라주세요.",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
                Spacer(modifier = Modifier.height(8.dp))
            }

            items(QUESTIONS.indices.toList()) { index ->
                Card(
                    onClick = {
                        picked = if (index in picked) picked - index else picked + index
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp),
                    colors = if (index in picked) {
                        CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer
                        )
                    } else {
                        CardDefaults.cardColors()
                    }
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp).fillMaxWidth(),
                        verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = index in picked,
                            onCheckedChange = {
                                picked = if (index in picked) picked - index else picked + index
                            }
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = QUESTIONS[index].text,
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = { showResult = true },
                    enabled = picked.isNotEmpty(),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                ) {
                    Text("결과 보기 (${picked.size}개 선택)")
                }
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    } else {
        LazyColumn {
            item {
                Row(
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 8.dp)
                ) {
                    TextButton(onClick = { showResult = false }) {
                        Text("← 다시 고르기")
                    }
                    TextButton(onClick = {
                        picked = emptySet()
                        showResult = false
                    }) {
                        Text("처음부터")
                    }
                }
                Text(
                    text = "추천 학과 ${results.size}개",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
                Spacer(modifier = Modifier.height(8.dp))
            }

            items(results) { pair ->
                val detail = pair.first
                val score = pair.second
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
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "적합도 $score",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }
    }
}
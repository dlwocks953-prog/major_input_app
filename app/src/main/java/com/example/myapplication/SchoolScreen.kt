package com.example.myapplication

import android.content.Intent
import android.net.Uri
import android.util.Log
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.clickable
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

@Composable
fun SchoolScreen(
    initialSchool: String? = null,
    onInitialSchoolConsumed: () -> Unit = {},
    onReturn: (() -> Unit)? = null,
    onMajorClick: (String) -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val allSchools = remember { MajorRepository.loadSchools(context) }
    val allDetails = remember { MajorRepository.loadAll(context) }

    var query by remember { mutableStateOf("") }
    var selectedRegion by remember { mutableStateOf<String?>(null) }
    var selectedGubun by remember { mutableStateOf<String?>(null) }
    var selectedSchool by remember {
        mutableStateOf(
            if (initialSchool != null) allSchools.find { it.schoolName == initialSchool } else null
        )
    }

    val favSchools by FavoriteStore.favorites(context, FavoriteType.SCHOOL)
        .collectAsState(initial = emptySet())

    BackHandler(enabled = selectedSchool != null) {
        if (onReturn != null) onReturn() else selectedSchool = null
    }

    LaunchedEffect(Unit) {
        if (initialSchool != null) onInitialSchoolConsumed()
    }

    val regions = remember(allSchools) {
        allSchools.mapNotNull { it.region }.filter { it.isNotBlank() }.distinct().sorted()
    }

    val gubuns = remember(allSchools) {
        allSchools.mapNotNull { it.schoolGubun }.filter { it.isNotBlank() }.distinct().sorted()
    }

    val visibleSchools = remember(query, selectedRegion, selectedGubun, allSchools) {
        allSchools.filter { school ->
            val name = school.schoolName ?: return@filter false
            val matchQuery = query.isBlank() || name.contains(query)
            val matchRegion = selectedRegion == null || school.region == selectedRegion
            val matchGubun = selectedGubun == null || school.schoolGubun == selectedGubun
            matchQuery && matchRegion && matchGubun
        }.sortedBy { it.schoolName }
    }

    val schoolMajors = remember(selectedSchool, allDetails) {
        val name = selectedSchool?.schoolName ?: return@remember emptyList()
        allDetails.filter { detail ->
            detail.university?.any { it.schoolName == name } == true
        }
    }

    val current = selectedSchool
    if (current == null) {
        LazyColumn {
            item {
                OutlinedTextField(
                    value = query,
                    onValueChange = { query = it },
                    label = { Text("대학 검색") },
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
                            selected = selectedGubun == null,
                            onClick = { selectedGubun = null },
                            label = { Text("전체") }
                        )
                    }
                    items(gubuns) { gubun ->
                        FilterChip(
                            selected = selectedGubun == gubun,
                            onClick = {
                                selectedGubun = if (selectedGubun == gubun) null else gubun
                            },
                            label = { Text(gubun) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    item {
                        FilterChip(
                            selected = selectedRegion == null,
                            onClick = { selectedRegion = null },
                            label = { Text("전 지역") }
                        )
                    }
                    items(regions) { region ->
                        FilterChip(
                            selected = selectedRegion == region,
                            onClick = {
                                selectedRegion = if (selectedRegion == region) null else region
                            },
                            label = { Text(region) }
                        )
                    }
                }

                Text(
                    text = "${visibleSchools.size}개 대학",
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )
            }

            items(visibleSchools) { school ->
                Card(
                    onClick = { selectedSchool = school },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = school.schoolName ?: "",
                            style = MaterialTheme.typography.titleMedium
                        )
                        Text(
                            text = "${school.region} · ${school.schoolGubun} · ${school.estType}",
                            style = MaterialTheme.typography.bodySmall
                        )
                        school.campusName?.takeIf { it != "제1캠퍼스" }?.let {
                            Text(
                                text = it,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }
        }
    } else {
        LazyColumn(modifier = Modifier.padding(16.dp)) {
            item {
                DetailHeader(
                    isFavorite = (current.schoolName ?: "") in favSchools,
                    onBack = {
                        if (onReturn != null) onReturn() else selectedSchool = null
                    },
                    onToggleFavorite = {
                        current.schoolName?.let { name ->
                            scope.launch {
                                FavoriteStore.toggle(context, FavoriteType.SCHOOL, name)
                            }
                        }
                    }
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = current.schoolName ?: "",
                    style = MaterialTheme.typography.headlineMedium
                )
                Text(
                    text = "${current.region} · ${current.schoolGubun} · ${current.estType}",
                    style = MaterialTheme.typography.bodyLarge
                )
                Spacer(modifier = Modifier.height(16.dp))

                DetailSection("캠퍼스", current.campusName)
                DetailSection("주소", current.adres)

                current.link?.takeIf { it.isNotBlank() }?.let { url ->
                    Text(
                        text = "홈페이지 바로가기",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier
                            .clickable {
                                try {
                                    context.startActivity(
                                        Intent(Intent.ACTION_VIEW, Uri.parse(url))
                                    )
                                } catch (e: Exception) {
                                    Log.e("SCHOOL", "링크 실패: ${e.message}")
                                }
                            }
                            .padding(vertical = 8.dp)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "개설 학과 ${schoolMajors.size}개",
                    style = MaterialTheme.typography.titleMedium
                )
                Spacer(modifier = Modifier.height(8.dp))
            }

            items(schoolMajors) { detail ->
                Card(
                    onClick = { detail.major?.let { onMajorClick(it) } },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = detail.major ?: "",
                            style = MaterialTheme.typography.titleMedium
                        )
                        detail.employment?.cleanHtml()?.let {
                            Text("취업률 $it", style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }
            }
        }
    }
}
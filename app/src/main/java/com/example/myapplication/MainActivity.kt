package com.example.myapplication

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.LocationCity
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material.icons.filled.Work
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.lifecycleScope
import com.google.gson.Gson
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.File
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.material3.NavigationBarDefaults
import androidx.core.view.WindowCompat

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, true)

        // 데이터를 새로 받아야 할 때만 아래 줄의 주석을 푸세요.
        // 다 받고 나면 반드시 다시 주석 처리하세요.
        // lifecycleScope.launch { dumpAllDetails(this@MainActivity) }

        setContent {
            AppScreen()
        }
    }
}

// ---------------------------------------------------------------
// 메인 화면 (탭 구조)
// ---------------------------------------------------------------

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppScreen() {
    val context = LocalContext.current

    var majors by remember { mutableStateOf<List<Major>>(emptyList()) }
    var selected by remember { mutableStateOf<Major?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var currentTab by remember { mutableStateOf(0) }
    var pendingJob by remember { mutableStateOf<String?>(null) }
    var pendingSchool by remember { mutableStateOf<String?>(null) }
    var returnTab by remember { mutableStateOf<Int?>(null) }
    var showSplash by remember { mutableStateOf(true) }

    BackHandler(enabled = selected != null) {
        selected = null
    }

    LaunchedEffect(Unit) {
        majors = MajorRepository.loadMajorList(context)
        if (majors.isEmpty()) {
            errorMessage = "학과 데이터를 불러올 수 없습니다"
        }
        isLoading = false
    }

    LaunchedEffect(Unit) {
        delay(1500)
        showSplash = false
    }

    if (showSplash) {
        SplashScreen()
        return
    }

    Scaffold(
        contentWindowInsets = WindowInsets.safeDrawing,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        selected?.name ?: when (currentTab) {
                            0 -> "학과 찾기"
                            1 -> "직업으로 찾기"
                            2 -> "과목으로 찾기"
                            //3 -> "적성 진단"
                            4 -> "관심 목록"
                            5 -> "대학 찾기"
                            else -> "학과 정보"
                        }
                    )
                }
            )
        },
        bottomBar = {
            if (selected == null) {
                NavigationBar(
                    windowInsets = NavigationBarDefaults.windowInsets
                ) {
                    NavigationBarItem(
                        selected = currentTab == 0,
                        onClick = { currentTab = 0 },
                        icon = { Icon(Icons.Default.School, contentDescription = null) },
                        label = { Text("학과") }
                    )
                    NavigationBarItem(
                        selected = currentTab == 1,
                        onClick = { currentTab = 1 },
                        icon = { Icon(Icons.Default.Work, contentDescription = null) },
                        label = { Text("직업") }
                    )
                    NavigationBarItem(
                        selected = currentTab == 5,
                        onClick = { currentTab = 5 },
                        icon = { Icon(Icons.Default.LocationCity, contentDescription = null) },
                        label = { Text("대학") }
                    )
                    NavigationBarItem(
                        selected = currentTab == 2,
                        onClick = { currentTab = 2 },
                        icon = { Icon(Icons.Default.MenuBook, contentDescription = null) },
                        label = { Text("과목") }
                    )
                    //NavigationBarItem(
                        //selected = currentTab == 3,
                        //onClick = { currentTab = 3 },
                        //icon = { Icon(Icons.Default.Psychology, contentDescription = null) },
                        //label = { Text("적성") }
                    //)
                    NavigationBarItem(
                        selected = currentTab == 4,
                        onClick = { currentTab = 4 },
                        icon = { Icon(Icons.Default.Star, contentDescription = null) },
                        label = { Text("관심") }
                    )
                }
            }
        }
    ) { innerPadding ->
        Column(modifier = Modifier.padding(innerPadding)) {
            val current = selected
            if (current != null) {
                MajorDetail(
                    major = current,
                    onBack = { selected = null }
                )
            } else {
                when (currentTab) {
                    0 -> when {
                        isLoading -> LoadingView()
                        errorMessage != null -> Text(
                            text = "불러오기 실패: $errorMessage",
                            modifier = Modifier.padding(16.dp)
                        )
                        else -> MajorList(
                            majors = majors,
                            onMajorClick = { selected = it }
                        )
                    }
                    1 -> JobSearchScreen(
                        majors = majors,
                        initialJob = pendingJob,
                        onInitialJobConsumed = { pendingJob = null },
                        onReturn = returnTab?.let {
                            { currentTab = it; returnTab = null }
                        },
                        onMajorClick = { name ->
                            selected = majors.find { it.name == name }
                        }
                    )
                    2 -> SubjectScreen(
                        majors = majors,
                        onMajorClick = { name ->
                            selected = majors.find { it.name == name }
                        }
                    )
                    //3 -> AptitudeScreen(
                       // majors = majors,
                        //onMajorClick = { name ->
                            //selected = majors.find { it.name == name }
                        //}
                    //)
                    4 -> FavoriteScreen(
                        majors = majors,
                        onMajorClick = { name ->
                            selected = majors.find { it.name == name }
                        },
                        onJobClick = { jobName ->
                            pendingJob = jobName
                            returnTab = 4
                            currentTab = 1
                        },
                        onSchoolClick = { schoolName ->
                            pendingSchool = schoolName
                            returnTab = 4
                            currentTab = 5
                        }
                    )
                    5 -> SchoolScreen(
                        initialSchool = pendingSchool,
                        onInitialSchoolConsumed = { pendingSchool = null },
                        onReturn = returnTab?.let {
                            { currentTab = it; returnTab = null }
                        },
                        onMajorClick = { name ->
                            selected = majors.find { it.name == name }
                        }
                    )
                }
            }
        }
    }
}

// ---------------------------------------------------------------
// 학과 목록 + 통합 검색 + 계열 필터
// ---------------------------------------------------------------

@Composable
fun MajorList(majors: List<Major>, onMajorClick: (Major) -> Unit) {
    val context = LocalContext.current
    var query by remember { mutableStateOf("") }
    var selectedField by remember { mutableStateOf<String?>(null) }

    val details = remember { MajorRepository.loadAll(context) }
    val detailMap = remember(details) { details.associateBy { it.major } }

    val fields = remember(majors) {
        majors.map { it.field }.distinct().sorted()
    }

    val filtered = remember(query, selectedField, majors) {
        majors.filter { major ->
            val matchField = selectedField == null || major.field == selectedField
            if (!matchField) return@filter false
            if (query.isBlank()) return@filter true

            val d = detailMap[major.name]
            major.name.contains(query) ||
                    major.field.contains(query) ||
                    d?.job?.contains(query) == true ||
                    d?.qualifications?.contains(query) == true
        }
    }

    LazyColumn {
        item {
            OutlinedTextField(
                value = query,
                onValueChange = { query = it },
                label = { Text("학과·직업·자격증 검색") },
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
                        selected = selectedField == null,
                        onClick = { selectedField = null },
                        label = { Text("전체") }
                    )
                }
                items(fields) { field ->
                    FilterChip(
                        selected = selectedField == field,
                        onClick = {
                            selectedField = if (selectedField == field) null else field
                        },
                        label = { Text(field) }
                    )
                }
            }

            Text(
                text = "${filtered.size}개 학과",
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )
        }

        items(filtered) { major ->
            val d = detailMap[major.name]
            val reason = when {
                query.isBlank() -> null
                major.name.contains(query) -> null
                d?.job?.contains(query) == true -> "관련 직업에 포함"
                d?.qualifications?.contains(query) == true -> "관련 자격에 포함"
                else -> null
            }
            MajorItem(
                name = major.name,
                field = major.field,
                reason = reason,
                onClick = { onMajorClick(major) }
            )
        }
    }
}

@Composable
fun MajorItem(
    name: String,
    field: String,
    reason: String? = null,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = name, style = MaterialTheme.typography.titleMedium)
            Text(text = field, style = MaterialTheme.typography.bodySmall)
            if (reason != null) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = reason,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

// ---------------------------------------------------------------
// 학과 상세 화면
// ---------------------------------------------------------------

@Composable
fun MajorDetail(major: Major, onBack: () -> Unit) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var detail by remember { mutableStateOf<MajorDetailDto?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var selectedArea by remember { mutableStateOf<String?>(null) }

    val favorites by FavoriteStore.favorites(context, FavoriteType.MAJOR)
        .collectAsState(initial = emptySet())
    val isFavorite = major.name in favorites

    LaunchedEffect(major.majorSeq) {
        isLoading = true
        val all = MajorRepository.loadAll(context)
        detail = all.find { it.major == major.name }
        isLoading = false
    }

    LazyColumn(modifier = Modifier.padding(16.dp)) {
        item {
            DetailHeader(
                isFavorite = isFavorite,
                onBack = onBack,
                onToggleFavorite = {
                    scope.launch {
                        FavoriteStore.toggle(context, FavoriteType.MAJOR, major.name)
                    }
                }
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(text = major.name, style = MaterialTheme.typography.headlineMedium)
            Text(text = major.field, style = MaterialTheme.typography.bodyLarge)
            Spacer(modifier = Modifier.height(16.dp))
        }

        if (isLoading) {
            item { LoadingView() }
        } else {
            val d = detail
            if (d == null) {
                item { Text("상세 정보가 없습니다.") }
            } else {
                item {
                    DetailSection("학과 소개", d.summary.cleanHtml())
                    DetailSection("이런 사람에게 어울려요", d.interest.cleanHtml())
                    DetailSection("학과 특성", d.property.cleanHtml())
                    DetailSection("관련 직업", d.job.cleanHtml())
                    DetailSection("관련 자격", d.qualifications.cleanHtml())
                    DetailSection("취업률", d.employment.cleanHtml())
                    DetailSection("졸업 후 평균 임금", d.salary?.let { "${it}만원" })

                    DetailListSection(
                        "관련 교과목",
                        d.relate_subject?.map {
                            it.subject_name to it.subject_description?.cleanHtml()
                        } ?: emptyList()
                    )

                    DetailListSection(
                        "이런 활동을 해보세요",
                        d.career_act?.map {
                            it.act_name?.cleanHtml() to it.act_description?.cleanHtml()
                        } ?: emptyList()
                    )

                    DetailListSection(
                        "졸업 후 진출 분야",
                        d.enter_field?.map {
                            it.gradeuate to it.description?.cleanHtml()
                        } ?: emptyList()
                    )

                    DetailListSection(
                        "주요 배우는 과목",
                        d.main_subject?.map {
                            it.name to it.summary?.cleanHtml()
                        } ?: emptyList()
                    )
                }

                // 개설 대학 - 지역 필터
                item {
                    val universities = d.university ?: emptyList()
                    val areas = universities.mapNotNull { it.area }.distinct().sorted()
                    val filteredUniv = if (selectedArea == null) universities
                    else universities.filter { it.area == selectedArea }

                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "개설 대학 (${filteredUniv.size}개)",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        item {
                            FilterChip(
                                selected = selectedArea == null,
                                onClick = { selectedArea = null },
                                label = { Text("전체") }
                            )
                        }
                        items(areas) { area ->
                            FilterChip(
                                selected = selectedArea == area,
                                onClick = {
                                    selectedArea = if (selectedArea == area) null else area
                                },
                                label = { Text(area) }
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))

                    Column {
                        filteredUniv.forEach { univ ->
                            Text(
                                text = "· ${univ.schoolName} ${univ.majorName}",
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        val url = univ.schoolURL
                                        if (!url.isNullOrBlank()) {
                                            try {
                                                context.startActivity(
                                                    Intent(Intent.ACTION_VIEW, Uri.parse(url))
                                                )
                                            } catch (e: Exception) {
                                                Log.e("APP", "링크 실패: ${e.message}")
                                            }
                                        }
                                    }
                                    .padding(vertical = 8.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

// ---------------------------------------------------------------
// 공용 컴포넌트
// ---------------------------------------------------------------

@Composable
fun DetailHeader(
    isFavorite: Boolean,
    onBack: () -> Unit,
    onToggleFavorite: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Button(onClick = onBack) {
            Icon(Icons.Default.ArrowBack, contentDescription = null)
            Spacer(modifier = Modifier.width(4.dp))
            Text("목록")
        }
        TextButton(onClick = onToggleFavorite) {
            Icon(
                imageVector = if (isFavorite) Icons.Default.Star else Icons.Default.StarBorder,
                contentDescription = null
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text("관심")
        }
    }
}

@Composable
fun DetailSection(title: String, content: String?) {
    if (!content.isNullOrBlank()) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 6.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            )
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = content,
                    style = MaterialTheme.typography.bodyMedium,
                    lineHeight = 22.sp
                )
            }
        }
    }
}

@Composable
fun DetailListSection(
    title: String,
    items: List<Pair<String?, String?>>
) {
    val valid = items.filter { !it.first.isNullOrBlank() }
    if (valid.isEmpty()) return

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(8.dp))
            valid.forEach { (name, desc) ->
                Text(
                    text = name ?: "",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold
                )
                if (!desc.isNullOrBlank()) {
                    Text(
                        text = desc,
                        style = MaterialTheme.typography.bodySmall,
                        lineHeight = 20.sp,
                        modifier = Modifier.padding(top = 2.dp, bottom = 10.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun LoadingView() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator()
    }
}

// ---------------------------------------------------------------
// 데이터 덤프용 (평소에는 실행되지 않음)
// ---------------------------------------------------------------

suspend fun dumpAllDetails(context: android.content.Context) {
    try {
        val listResponse = RetrofitClient.api.getMajors(apiKey = API_KEY)
        val majors = listResponse.dataSearch.content
        Log.d("DUMP", "총 ${majors.size}개 시작")

        val results = mutableListOf<String>()
        val failed = mutableListOf<MajorDto>()

        majors.forEachIndexed { index, dto ->
            try {
                val detailResponse = RetrofitClient.api.getMajorDetail(
                    apiKey = API_KEY,
                    majorSeq = dto.majorSeq
                )
                detailResponse.dataSearch?.content?.firstOrNull()?.let {
                    results.add(Gson().toJson(it))
                }
                if (index % 20 == 0) {
                    Log.d("DUMP", "진행 ${index}/${majors.size}")
                }
                delay(200)
            } catch (e: Exception) {
                failed.add(dto)
                Log.e("DUMP", "${dto.mClass} 실패: ${e.message}")
            }
        }

        if (failed.isNotEmpty()) {
            Log.d("DUMP", "재시도 ${failed.size}개")
            failed.forEach { dto ->
                try {
                    val response = RetrofitClient.api.getMajorDetail(
                        apiKey = API_KEY,
                        majorSeq = dto.majorSeq
                    )
                    response.dataSearch?.content?.firstOrNull()?.let {
                        results.add(Gson().toJson(it))
                        Log.d("DUMP", "재시도 성공: ${dto.mClass}")
                    }
                } catch (e: Exception) {
                    Log.e("DUMP", "재시도도 실패: ${dto.mClass}")
                }
                delay(1000)
            }
        }

        val file = File(context.getExternalFilesDir(null), "majors.json")
        file.writeText("[" + results.joinToString(",") + "]")

        Log.d("DUMP", "완료! ${results.size}개 저장")
        Log.d("DUMP", "경로: ${file.absolutePath}")
    } catch (e: Exception) {
        Log.e("DUMP", "전체 실패: ${e.message}")
    }
}
package com.example.myapplication

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query
import com.google.gson.annotations.SerializedName

const val API_KEY = ""

data class MajorResponse(
    val dataSearch: DataSearch
)

data class DataSearch(
    val content: List<MajorDto>
)

data class MajorDto(
    val majorSeq: String,
    val mClass: String,
    val lClass: String,
    val facilName: String?
)

data class Major(val majorSeq: String, val name: String, val field: String)

interface CareerApi {
    @GET("cnet/openapi/getOpenApi")
    suspend fun getMajors(
        @Query("apiKey") apiKey: String,
        @Query("svcType") svcType: String = "api",
        @Query("svcCode") svcCode: String = "MAJOR",
        @Query("contentType") contentType: String = "json",
        @Query("gubun") gubun: String = "univ_list",
        @Query("perPage") perPage: Int = 600
    ): MajorResponse
    @GET("cnet/openapi/getOpenApi")
    suspend fun getMajorDetail(
        @Query("apiKey") apiKey: String,
        @Query("majorSeq") majorSeq: String,
        @Query("svcType") svcType: String = "api",
        @Query("svcCode") svcCode: String = "MAJOR_VIEW",
        @Query("contentType") contentType: String = "json",
        @Query("gubun") gubun: String = "univ_list"
    ): MajorDetailResponse
}

object RetrofitClient {
    private const val BASE_URL = "https://www.career.go.kr/"

    val api: CareerApi = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .addConverterFactory(GsonConverterFactory.create())
        .build()
        .create(CareerApi::class.java)
}

fun MajorDto.toMajor(): Major {
    return Major(
        majorSeq = majorSeq,
        name = mClass,
        field = lClass
    )
}

data class MajorDetailResponse(
    val dataSearch: DetailDataSearch?
)

data class DetailDataSearch(
    val content: List<MajorDetailDto>?
)

data class MajorDetailDto(
    val major: String?,
    val summary: String?,
    val job: String?,
    val qualifications: String?,
    val interest: String?,
    val property: String?,
    val salary: String?,
    val employment: String?,
    val department: String?,
    val university: List<UniversityDto>?,
    val main_subject: List<SubjectDto>?,
    val relate_subject: List<RelateSubjectDto>?,
    val career_act: List<CareerActDto>?,
    val enter_field: List<EnterFieldDto>?,
)

data class RelateSubjectDto(
    val subject_name: String?,
    val subject_description: String?
)

data class CareerActDto(
    val act_name: String?,
    val act_description: String?
)

data class EnterFieldDto(
    val gradeuate: String?,
    val description: String?
)

data class UniversityDto(
    val schoolName: String?,
    val majorName: String?,
    val area: String?,
    val schoolURL: String?
)

data class SubjectDto(
    @SerializedName("SBJECT_NM") val name: String?,
    @SerializedName("SBJECT_SUMRY") val summary: String?
)

data class JobResponse(
    val dataSearch: JobDataSearch?
)

data class JobDataSearch(
    val content: List<JobDto>?
)

data class JobDto(
    val job: String?,
    val profession: String?,
    val summary: String?,
    val similarJob: String?,
    val salery: String?,
    val possibility: String?,
    val jobdicSeq: String?
)

data class SchoolResponse(
    val dataSearch: SchoolDataSearch?
)

data class SchoolDataSearch(
    val content: List<SchoolDto>?
)

data class SchoolDto(
    val schoolName: String?,
    val campusName: String?,
    val region: String?,
    val schoolGubun: String?,
    val estType: String?,
    val schoolType: String?,
    val adres: String?,
    val link: String?,
    val seq: String?
)

fun String?.cleanHtml(): String? {
    return this?.replace(Regex("<br\\s*/?>"), "\n")
        ?.replace(Regex("<.*?>"), "")
        ?.trim()
}
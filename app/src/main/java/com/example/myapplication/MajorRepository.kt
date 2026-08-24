package com.example.myapplication

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

object MajorRepository {
    private var cached: List<MajorDetailDto>? = null

    fun loadAll(context: Context): List<MajorDetailDto> {
        cached?.let { return it }

        return try {
            val json = context.assets.open("majors.json")
                .bufferedReader()
                .use { it.readText() }

            val type = object : TypeToken<List<MajorDetailDto>>() {}.type
            val list: List<MajorDetailDto> = Gson().fromJson(json, type)
            cached = list
            list
        } catch (e: Exception) {
            emptyList()
        }
    }
    private var cachedJobs: List<JobDto>? = null

    fun loadJobs(context: Context): List<JobDto> {
        cachedJobs?.let { return it }

        return try {
            val json = context.assets.open("jobs.json")
                .bufferedReader()
                .use { it.readText() }

            val response = Gson().fromJson(json, JobResponse::class.java)
            val list = response.dataSearch?.content ?: emptyList()
            cachedJobs = list
            list
        } catch (e: Exception) {
            emptyList()
        }
    }
    private var cachedList: List<Major>? = null

    fun loadMajorList(context: Context): List<Major> {
        cachedList?.let { return it }

        return try {
            val json = context.assets.open("major_list.json")
                .bufferedReader()
                .use { it.readText() }

            val response = Gson().fromJson(json, MajorResponse::class.java)
            val list = response.dataSearch.content.map { it.toMajor() }
            cachedList = list
            list
        } catch (e: Exception) {
            emptyList()
        }
    }
    private var cachedSchools: List<SchoolDto>? = null

    fun loadSchools(context: Context): List<SchoolDto> {
        cachedSchools?.let { return it }

        return try {
            val json = context.assets.open("schools.json")
                .bufferedReader()
                .use { it.readText() }

            val response = Gson().fromJson(json, SchoolResponse::class.java)
            val list = response.dataSearch?.content ?: emptyList()
            cachedSchools = list
            list
        } catch (e: Exception) {
            emptyList()
        }
    }
}
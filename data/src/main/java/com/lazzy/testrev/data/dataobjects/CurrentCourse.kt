package com.lazzy.testrev.data.dataobjects

data class CurrentCourse(
    val base: String,
    val date: String,
    val currencies: Map<String, Double>
)
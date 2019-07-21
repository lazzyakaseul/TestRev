package com.lazzy.testrev.data.dataobjects

import com.lazzy.testrev.domain.entity.Course
import com.lazzy.testrev.domain.entity.Currency

fun CurrentCourse.convertToEntity() = Course(Currency(base, 1.0), currencies)

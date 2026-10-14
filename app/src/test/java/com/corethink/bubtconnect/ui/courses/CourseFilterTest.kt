package com.corethink.bubtconnect.ui.courses

import com.corethink.bubtconnect.data.model.Course
import com.corethink.bubtconnect.data.model.CourseStatus
import com.corethink.bubtconnect.data.model.CurriculumCourse
import org.junit.Assert.assertEquals
import org.junit.Test

class CourseFilterTest {

    private val courses = listOf(
        CurriculumCourse(Course("CSE 101", "Intro to Programming"), 3.0, CourseStatus.Completed),
        CurriculumCourse(Course("CSE 231", "Data Structures"), 3.0, CourseStatus.InProgress),
        CurriculumCourse(Course("CSE 241", "Operating Systems"), 3.0, CourseStatus.Remaining)
    )

    private fun codes(query: String, filter: CourseFilter) =
        CoursesViewModel.filterCourses(courses, query, filter).map { it.course.code }

    @Test
    fun searchMatchesCodeWithOrWithoutSpaceAndTitle() {
        assertEquals(listOf("CSE 231"), codes("cse231", CourseFilter.All))
        assertEquals(listOf("CSE 231"), codes("CSE 231", CourseFilter.All))
        assertEquals(listOf("CSE 241"), codes("operating", CourseFilter.All))
    }

    @Test
    fun remainingIncludesInProgress() {
        assertEquals(listOf("CSE 101"), codes("", CourseFilter.Completed))
        assertEquals(listOf("CSE 231", "CSE 241"), codes("", CourseFilter.Remaining))
    }
}

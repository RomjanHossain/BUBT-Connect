package com.corethink.bubtconnect.data.model

/** Basic profile captured during onboarding (1c). No login required. */
data class StudentProfile(
    val roll: String,
    val department: String,
    val semester: String,
    val name: String? = null
)

/** Departments offered in the profile setup dropdown. */
val BubtDepartments = listOf(
    "CSE", "EEE", "Civil", "Textile", "BBA", "Accounting",
    "Finance", "Management", "Marketing", "Economics", "English", "Law"
)

/** BUBT runs three semesters a year, so a four-year degree spans 12. */
val BubtSemesters = listOf(
    "1st", "2nd", "3rd", "4th", "5th", "6th",
    "7th", "8th", "9th", "10th", "11th", "12th"
)

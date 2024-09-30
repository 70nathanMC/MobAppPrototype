package com.example.mobappprototype.model

data class TutorProfile(
    val tutor: Tutor, // Reference to the original Tutor object
    val bioDescription: String,
    val schedules: List<Schedule>,
    val reviews: List<Review>
) {
    val name: String by tutor::name // Delegate name property from Tutor
    val profilePic: Int by tutor::profilePic // Delegate profilePic property from Tutor
    val strengths: List<Subject> by tutor::strengths // Delegate strengths property from Tutor
    val rating: Float by tutor::rating // Delegate rating property from Tutor
}

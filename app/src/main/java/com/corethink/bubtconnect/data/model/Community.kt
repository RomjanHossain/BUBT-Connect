package com.corethink.bubtconnect.data.model

enum class PostCategory { Notes, LabReport, Internship }

/** A shared PDF in the community feed. [author] is null for anonymous uploads. */
data class CommunityPost(
    val id: String,
    val title: String,
    val author: String?,
    val department: String,
    val category: PostCategory,
    val tags: List<String>,
    val daysAgo: Int,
    val views: Int,
    val likes: Int,
    val likedByMe: Boolean = false
)

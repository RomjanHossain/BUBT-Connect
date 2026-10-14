package com.corethink.bubtconnect.data.repository

import com.corethink.bubtconnect.data.model.CommunityPost
import com.corethink.bubtconnect.data.model.PostCategory

interface CommunityRepository {
    fun posts(): List<CommunityPost>
}

/** Sample feed until uploads and likes are backed by data/source/remote. */
object SampleCommunityRepository : CommunityRepository {
    override fun posts(): List<CommunityPost> = listOf(
        CommunityPost(
            id = "p1",
            title = "DS Lab Report: Linked Lists",
            author = "Nusrat J.",
            department = "CSE",
            category = PostCategory.LabReport,
            tags = listOf("cse231", "datastructures", "lab"),
            daysAgo = 2,
            views = 1200,
            likes = 84
        ),
        CommunityPost(
            id = "p2",
            title = "Internship Report Template",
            author = null,
            department = "BBA",
            category = PostCategory.Internship,
            tags = listOf("internship", "template"),
            daysAgo = 5,
            views = 860,
            likes = 41,
            likedByMe = true
        ),
        CommunityPost(
            id = "p3",
            title = "Calculus II Mid Notes",
            author = "Tanvir H.",
            department = "CSE",
            category = PostCategory.Notes,
            tags = listOf("mat", "calculus", "midterm"),
            daysAgo = 7,
            views = 2400,
            likes = 132
        ),
        CommunityPost(
            id = "p4",
            title = "Digital Logic Lab: Karnaugh Maps",
            author = "Sadia R.",
            department = "CSE",
            category = PostCategory.LabReport,
            tags = listOf("cse233", "dld", "lab"),
            daysAgo = 10,
            views = 640,
            likes = 27
        ),
        CommunityPost(
            id = "p5",
            title = "Internship Experience: Software QA",
            author = "Arif M.",
            department = "CSE",
            category = PostCategory.Internship,
            tags = listOf("internship", "qa"),
            daysAgo = 16,
            views = 1500,
            likes = 58
        )
    )
}

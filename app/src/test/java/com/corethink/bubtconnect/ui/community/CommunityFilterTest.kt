package com.corethink.bubtconnect.ui.community

import com.corethink.bubtconnect.data.model.CommunityPost
import com.corethink.bubtconnect.data.model.PostCategory
import com.corethink.bubtconnect.ui.community.CommunityViewModel.Companion.toItem
import com.corethink.bubtconnect.ui.components.formatCompactCount
import com.corethink.bubtconnect.ui.components.formatDaysAgo
import org.junit.Assert.assertEquals
import org.junit.Test

class CommunityFilterTest {

    private val posts = listOf(
        CommunityPost("a", "DS Lab Report", "Nusrat J.", "CSE", PostCategory.LabReport, listOf("lab"), 2, 1200, 84),
        CommunityPost("b", "Internship Template", null, "BBA", PostCategory.Internship, listOf("template"), 5, 860, 41, likedByMe = true),
        CommunityPost("c", "Calculus Notes", "Tanvir H.", "CSE", PostCategory.Notes, listOf("midterm"), 7, 2400, 132)
    )

    private fun ids(query: String, filter: CommunityFilter = CommunityFilter.All) =
        CommunityViewModel.filterPosts(posts, query, filter).map { it.id }

    @Test
    fun searchMatchesTitleAuthorAnonymousAndHashTags() {
        assertEquals(listOf("c"), ids("calculus"))
        assertEquals(listOf("a"), ids("nusrat"))
        assertEquals(listOf("b"), ids("anonymous"))
        assertEquals(listOf("c"), ids("#midterm"))
    }

    @Test
    fun chipsFilterByDepartmentAndCategory() {
        assertEquals(listOf("a", "c"), ids("", CommunityFilter.Cse))
        assertEquals(listOf("a"), ids("", CommunityFilter.LabReports))
        assertEquals(listOf("b"), ids("", CommunityFilter.Internship))
    }

    @Test
    fun likeCountFollowsToggle() {
        assertEquals(85, posts[0].toItem(setOf("a")).likeCount)
        assertEquals(40, posts[1].toItem(emptySet()).likeCount)
        assertEquals(41, posts[1].toItem(setOf("b")).likeCount)
    }

    @Test
    fun compactFormatting() {
        assertEquals("860", formatCompactCount(860))
        assertEquals("1.2k", formatCompactCount(1200))
        assertEquals("2k", formatCompactCount(2000))
        assertEquals("2d ago", formatDaysAgo(2))
        assertEquals("1w ago", formatDaysAgo(7))
    }
}

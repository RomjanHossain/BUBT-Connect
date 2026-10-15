package com.corethink.bubtconnect.ui.community

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.corethink.bubtconnect.data.model.CommunityPost
import com.corethink.bubtconnect.data.model.PostCategory
import com.corethink.bubtconnect.data.repository.CommunityRepository
import com.corethink.bubtconnect.data.repository.SampleCommunityRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update

enum class CommunityFilter(val label: String) {
    All("All"),
    Cse("CSE"),
    LabReports("Lab Reports"),
    Internship("Internship"),
    Bba("BBA")
}

data class CommunityPostItem(
    val post: CommunityPost,
    val liked: Boolean,
    val likeCount: Int
)

data class CommunityUiState(
    val query: String = "",
    val filter: CommunityFilter = CommunityFilter.All,
    val posts: List<CommunityPostItem> = emptyList()
)

class CommunityViewModel(
    repository: CommunityRepository = SampleCommunityRepository
) : ViewModel() {

    private val query = MutableStateFlow("")
    private val filter = MutableStateFlow(CommunityFilter.All)
    private val likedIds = MutableStateFlow(repository.posts.value.filter { it.likedByMe }.map { it.id }.toSet())

    val uiState: StateFlow<CommunityUiState> = combine(
        repository.posts,
        query,
        filter,
        likedIds
    ) { posts, query, filter, liked ->
        CommunityUiState(
            query = query,
            filter = filter,
            posts = filterPosts(posts, query, filter).map { it.toItem(liked) }
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = CommunityUiState(posts = repository.posts.value.map { it.toItem(likedIds.value) })
    )

    fun onQueryChange(value: String) {
        query.value = value
    }

    fun selectFilter(value: CommunityFilter) {
        filter.value = value
    }

    fun toggleLike(postId: String) {
        likedIds.update { if (postId in it) it - postId else it + postId }
    }

    companion object {
        const val ANONYMOUS = "Anonymous"

        /** [CommunityPost.likes] already counts the user's own like, so adjust it when they toggle. */
        fun CommunityPost.toItem(likedIds: Set<String>): CommunityPostItem {
            val liked = id in likedIds
            val othersLikes = likes - if (likedByMe) 1 else 0
            return CommunityPostItem(this, liked, othersLikes + if (liked) 1 else 0)
        }

        fun filterPosts(
            posts: List<CommunityPost>,
            query: String,
            filter: CommunityFilter
        ): List<CommunityPost> {
            // "#lab" searches tags the same way "lab" does.
            val needle = query.trim().removePrefix("#")
            return posts.filter { post ->
                val matchesQuery = needle.isEmpty() ||
                    post.title.contains(needle, ignoreCase = true) ||
                    (post.author ?: ANONYMOUS).contains(needle, ignoreCase = true) ||
                    post.department.contains(needle, ignoreCase = true) ||
                    post.tags.any { it.contains(needle, ignoreCase = true) }
                val matchesFilter = when (filter) {
                    CommunityFilter.All -> true
                    CommunityFilter.Cse -> post.department == "CSE"
                    CommunityFilter.LabReports -> post.category == PostCategory.LabReport
                    CommunityFilter.Internship -> post.category == PostCategory.Internship
                    CommunityFilter.Bba -> post.department == "BBA"
                }
                matchesQuery && matchesFilter
            }
        }
    }
}

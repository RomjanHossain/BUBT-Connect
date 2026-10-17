package com.corethink.bubtconnect.ui.community

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.outlined.Bookmarks
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.FileDownload
import androidx.compose.material.icons.outlined.Functions
import androidx.compose.material.icons.outlined.IosShare
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material.icons.outlined.VisibilityOff
import androidx.compose.material.icons.outlined.Work
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.corethink.bubtconnect.data.model.PostCategory
import com.corethink.bubtconnect.data.repository.SampleCommunityRepository
import com.corethink.bubtconnect.ui.community.CommunityViewModel.Companion.ANONYMOUS
import com.corethink.bubtconnect.ui.community.CommunityViewModel.Companion.toItem
import com.corethink.bubtconnect.ui.components.BubtButton
import com.corethink.bubtconnect.ui.components.EmptyIllustration
import com.corethink.bubtconnect.ui.components.EmptyState
import com.corethink.bubtconnect.ui.components.FilterPill
import com.corethink.bubtconnect.ui.components.formatCompactCount
import com.corethink.bubtconnect.ui.components.formatDaysAgo
import com.corethink.bubtconnect.ui.components.showNotAvailableYet
import com.corethink.bubtconnect.ui.design.BUBTConnectMaterial3Theme
import com.corethink.bubtconnect.ui.design.BubtCategoryTile
import com.corethink.bubtconnect.ui.design.BubtError
import com.corethink.bubtconnect.ui.design.BubtShapes
import com.corethink.bubtconnect.ui.design.BubtSpacing
import com.corethink.bubtconnect.ui.design.BubtSuccess
import com.corethink.bubtconnect.ui.design.BubtTag
import com.corethink.bubtconnect.ui.design.BubtTagStyle
import com.corethink.bubtconnect.ui.design.BubtWarning
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking

@Composable
fun CommunityRoute(
    onShareNotes: () -> Unit = {},
    viewModel: CommunityViewModel = viewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    CommunityScreen(
        state = state,
        onQueryChange = viewModel::onQueryChange,
        onSelectFilter = viewModel::selectFilter,
        onOpenPost = { showNotAvailableYet(context, "PDF preview") },
        onToggleLike = { viewModel.toggleLike(it.post.id) },
        onDownload = { showNotAvailableYet(context, "Offline downloads") },
        // 1x · "Share your notes" opens the scanner at Step 1 with publish pre-enabled.
        onShare = onShareNotes,
        onOpenBookmarks = { showNotAvailableYet(context, "Bookmarks") }
    )
}

/** 1t · Community feed. Card tap = PDF preview, filled heart = liked; Share FAB shrinks while scrolling down. */
@Composable
fun CommunityScreen(
    state: CommunityUiState,
    onQueryChange: (String) -> Unit,
    onSelectFilter: (CommunityFilter) -> Unit,
    onOpenPost: (CommunityPostItem) -> Unit,
    onToggleLike: (CommunityPostItem) -> Unit,
    onDownload: (CommunityPostItem) -> Unit,
    onShare: () -> Unit,
    onOpenBookmarks: () -> Unit,
    modifier: Modifier = Modifier
) {
    val listState = rememberLazyListState()
    var fabExpanded by remember { mutableStateOf(true) }
    LaunchedEffect(listState) {
        var lastIndex = 0
        var lastOffset = 0
        snapshotFlow { listState.firstVisibleItemIndex to listState.firstVisibleItemScrollOffset }
            .collect { (index, offset) ->
                val goingDown = index > lastIndex || (index == lastIndex && offset > lastOffset)
                val goingUp = index < lastIndex || (index == lastIndex && offset < lastOffset)
                if (goingDown && (index > 0 || offset > 24)) fabExpanded = false
                if (goingUp) fabExpanded = true
                lastIndex = index
                lastOffset = offset
            }
    }

    Box(modifier = modifier.fillMaxSize()) {
        LazyColumn(
            state = listState,
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = BubtSpacing.Screen, vertical = BubtSpacing.Lg),
            verticalArrangement = Arrangement.spacedBy(BubtSpacing.Md)
        ) {
            item {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "Community",
                        style = MaterialTheme.typography.headlineSmall,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier
                            .weight(1f)
                            .padding(bottom = BubtSpacing.Sm)
                    )
                    IconButton(onClick = onOpenBookmarks) {
                        Icon(
                            imageVector = Icons.Outlined.Bookmarks,
                            contentDescription = "Bookmarks",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
            item {
                CommunitySearchField(query = state.query, onQueryChange = onQueryChange)
            }
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState())
                        .padding(bottom = BubtSpacing.Xs),
                    horizontalArrangement = Arrangement.spacedBy(BubtSpacing.Sm)
                ) {
                    CommunityFilter.entries.forEach { filter ->
                        FilterPill(
                            text = filter.label,
                            selected = filter == state.filter,
                            onClick = { onSelectFilter(filter) }
                        )
                    }
                }
            }
            if (state.posts.isEmpty()) {
                item {
                    EmptyState(
                        illustration = EmptyIllustration.NoNotes,
                        title = if (state.query.isBlank()) "No shared notes yet. Be the first!"
                        else "No posts match \"${state.query.trim()}\".",
                        message = if (state.query.isBlank())
                            "Nobody has shared a note here yet. Yours could help the next intake."
                        else "Try a different search or filter.",
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(460.dp),
                        action = {
                            if (state.query.isBlank()) {
                                BubtButton(
                                    text = "Share your notes",
                                    onClick = onShare,
                                    leadingIcon = Icons.Outlined.IosShare
                                )
                            }
                        }
                    )
                }
            }
            items(state.posts, key = { it.post.id }) { item ->
                CommunityPostCard(
                    item = item,
                    onOpen = { onOpenPost(item) },
                    onToggleLike = { onToggleLike(item) },
                    onDownload = { onDownload(item) }
                )
            }
        }
        ExtendedFloatingActionButton(
            onClick = onShare,
            expanded = fabExpanded,
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
            icon = { Icon(imageVector = Icons.Outlined.IosShare, contentDescription = "Share") },
            text = {
                Text(
                    text = "Share",
                    style = MaterialTheme.typography.labelLarge
                )
            },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(BubtSpacing.Screen)
        )
    }
}

@Composable
private fun CommunitySearchField(query: String, onQueryChange: (String) -> Unit) {
    OutlinedTextField(
        value = query,
        onValueChange = onQueryChange,
        modifier = Modifier.fillMaxWidth(),
        placeholder = { Text("Search notes, reports, #tags", style = MaterialTheme.typography.bodyMedium) },
        leadingIcon = { Icon(Icons.Outlined.Search, contentDescription = null) },
        trailingIcon = if (query.isNotEmpty()) {
            {
                IconButton(onClick = { onQueryChange("") }) {
                    Icon(Icons.Filled.Close, contentDescription = "Clear search")
                }
            }
        } else null,
        singleLine = true,
        shape = BubtShapes.Field,
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = MaterialTheme.colorScheme.primary,
            unfocusedBorderColor = MaterialTheme.colorScheme.outline,
            unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainer,
            focusedContainerColor = MaterialTheme.colorScheme.surface
        )
    )
}

@Composable
private fun CommunityPostCard(
    item: CommunityPostItem,
    onOpen: () -> Unit,
    onToggleLike: () -> Unit,
    onDownload: () -> Unit
) {
    val post = item.post
    Surface(
        onClick = onOpen,
        modifier = Modifier.fillMaxWidth(),
        shape = BubtShapes.Card,
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
    ) {
        Column(modifier = Modifier.padding(BubtSpacing.CardPadding)) {
            Row(verticalAlignment = Alignment.Top) {
                BubtCategoryTile(
                    icon = iconFor(post.category),
                    tint = tintFor(post.category)
                )
                Spacer(modifier = Modifier.width(BubtSpacing.Md))
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = post.title,
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f, fill = false)
                        )
                        if (post.tags.isNotEmpty()) {
                            Spacer(modifier = Modifier.width(BubtSpacing.Sm))
                            BubtTag(text = post.tags.first(), style = BubtTagStyle.Primary)
                        }
                    }
                    Spacer(modifier = Modifier.height(2.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        if (post.author == null) {
                            Icon(
                                imageVector = Icons.Outlined.VisibilityOff,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(BubtSpacing.Xs))
                        }
                        Text(
                            text = post.author ?: ANONYMOUS,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(BubtSpacing.Md))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Outlined.Visibility,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(BubtSpacing.Xs))
                Text(
                    text = "${formatCompactCount(post.views)} · ${formatDaysAgo(post.daysAgo)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.weight(1f)
                )
                IconButton(onClick = onToggleLike, modifier = Modifier.size(ActionHeight)) {
                    Icon(
                        imageVector = Icons.Filled.Favorite,
                        contentDescription = if (item.liked) "Unlike" else "Like",
                        tint = if (item.liked) BubtError else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Text(
                    text = formatCompactCount(item.likeCount),
                    style = MaterialTheme.typography.labelMedium,
                    color = if (item.liked) BubtError else MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.width(BubtSpacing.Sm))
                IconButton(onClick = onDownload, modifier = Modifier.size(ActionHeight)) {
                    Icon(
                        imageVector = Icons.Outlined.FileDownload,
                        contentDescription = "Download",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}

private val ActionHeight = 40.dp

private fun iconFor(category: PostCategory): ImageVector = when (category) {
    PostCategory.Notes -> Icons.Outlined.Description
    PostCategory.LabReport -> Icons.Outlined.Functions
    PostCategory.Internship -> Icons.Outlined.Work
}

@Composable
private fun tintFor(category: PostCategory): Color = when (category) {
    PostCategory.Notes -> MaterialTheme.colorScheme.primary
    PostCategory.LabReport -> BubtSuccess
    PostCategory.Internship -> BubtWarning
}

@Preview(showBackground = true, heightDp = 800)
@Composable
private fun CommunityScreenPreview() {
    val posts = runBlocking { SampleCommunityRepository.posts.first() }
    val liked = posts.filter { it.likedByMe }.map { it.id }.toSet()
    BUBTConnectMaterial3Theme {
        Surface(color = MaterialTheme.colorScheme.background) {
            CommunityScreen(
                state = CommunityUiState(posts = posts.map { it.toItem(liked) }),
                onQueryChange = {},
                onSelectFilter = {},
                onOpenPost = {},
                onToggleLike = {},
                onDownload = {},
                onShare = {},
                onOpenBookmarks = {}
            )
        }
    }
}

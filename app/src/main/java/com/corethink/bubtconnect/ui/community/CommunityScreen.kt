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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.FileDownload
import androidx.compose.material.icons.outlined.PictureAsPdf
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.corethink.bubtconnect.data.repository.SampleCommunityRepository
import com.corethink.bubtconnect.ui.community.CommunityViewModel.Companion.ANONYMOUS
import com.corethink.bubtconnect.ui.community.CommunityViewModel.Companion.toItem
import com.corethink.bubtconnect.ui.components.FilterPill
import com.corethink.bubtconnect.ui.components.formatCompactCount
import com.corethink.bubtconnect.ui.components.formatDaysAgo
import com.corethink.bubtconnect.ui.components.showNotAvailableYet
import com.corethink.bubtconnect.ui.design.BUBTConnectMaterial3Theme
import com.corethink.bubtconnect.ui.design.BubtShapes
import com.corethink.bubtconnect.ui.design.BubtSpacing

@Composable
fun CommunityRoute(viewModel: CommunityViewModel = viewModel()) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    CommunityScreen(
        state = state,
        onQueryChange = viewModel::onQueryChange,
        onSelectFilter = viewModel::selectFilter,
        onOpenPost = { showNotAvailableYet(context, "PDF preview") },
        onToggleLike = { viewModel.toggleLike(it.post.id) },
        onDownload = { showNotAvailableYet(context, "Offline downloads") }
    )
}

/** 1r · Community feed. Card tap = PDF preview, filled heart = liked, downloads cached for offline. */
@Composable
fun CommunityScreen(
    state: CommunityUiState,
    onQueryChange: (String) -> Unit,
    onSelectFilter: (CommunityFilter) -> Unit,
    onOpenPost: (CommunityPostItem) -> Unit,
    onToggleLike: (CommunityPostItem) -> Unit,
    onDownload: (CommunityPostItem) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = BubtSpacing.Screen, vertical = BubtSpacing.Lg),
        verticalArrangement = Arrangement.spacedBy(BubtSpacing.Md)
    ) {
        item {
            Text(
                text = "Community",
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(bottom = BubtSpacing.Sm)
            )
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
                Text(
                    text = if (state.query.isBlank()) "Nothing shared here yet."
                    else "No posts match \"${state.query.trim()}\".",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(vertical = BubtSpacing.Lg)
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
                Box(
                    modifier = Modifier
                        .size(width = 48.dp, height = 56.dp)
                        .clip(BubtShapes.Skeleton)
                        .background(MaterialTheme.colorScheme.surfaceContainerHigh),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Outlined.PictureAsPdf,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Spacer(modifier = Modifier.width(BubtSpacing.Md))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = post.title,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "by ${post.author ?: ANONYMOUS} · ${formatDaysAgo(post.daysAgo)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
            Spacer(modifier = Modifier.height(BubtSpacing.Md))
            Row(verticalAlignment = Alignment.CenterVertically) {
                DepartmentTag(post.department)
                Spacer(modifier = Modifier.width(BubtSpacing.Sm))
                Text(
                    text = "${formatCompactCount(post.views)} views",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.weight(1f)
                )
                LikeButton(liked = item.liked, count = item.likeCount, onClick = onToggleLike)
                Spacer(modifier = Modifier.width(BubtSpacing.Sm))
                Surface(
                    onClick = onDownload,
                    modifier = Modifier.size(ActionHeight),
                    shape = BubtShapes.Field,
                    color = MaterialTheme.colorScheme.surface,
                    contentColor = MaterialTheme.colorScheme.onSurface,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Outlined.FileDownload,
                            contentDescription = "Download",
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }
    }
}

private val ActionHeight = 36.dp

@Composable
private fun DepartmentTag(department: String) {
    Surface(
        shape = BubtShapes.Chip,
        color = MaterialTheme.colorScheme.surface,
        contentColor = MaterialTheme.colorScheme.onSurface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
    ) {
        Text(
            text = department,
            style = MaterialTheme.typography.labelSmall,
            modifier = Modifier.padding(horizontal = BubtSpacing.Sm, vertical = 2.dp)
        )
    }
}

/** Outlined when not liked, solid primary once liked. */
@Composable
private fun LikeButton(liked: Boolean, count: Int, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        modifier = Modifier.height(ActionHeight),
        shape = BubtShapes.Field,
        color = if (liked) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface,
        contentColor = if (liked) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface,
        border = if (liked) null else BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = BubtSpacing.Md),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = if (liked) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                contentDescription = if (liked) "Unlike" else "Like",
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(BubtSpacing.Xs))
            Text(text = formatCompactCount(count), style = MaterialTheme.typography.labelLarge)
        }
    }
}

@Preview(showBackground = true, heightDp = 800)
@Composable
private fun CommunityScreenPreview() {
    val posts = SampleCommunityRepository.posts.value
    val liked = posts.filter { it.likedByMe }.map { it.id }.toSet()
    BUBTConnectMaterial3Theme {
        Surface(color = MaterialTheme.colorScheme.background) {
            CommunityScreen(
                state = CommunityUiState(posts = posts.map { it.toItem(liked) }),
                onQueryChange = {},
                onSelectFilter = {},
                onOpenPost = {},
                onToggleLike = {},
                onDownload = {}
            )
        }
    }
}

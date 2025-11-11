package com.mpieterse.stride.data.remote.models

data class PushItem(
    val requestId: String,
    val id: String,
    val habitId: String,
    val dayKey: String,
    val completedAt: String,
    val deleted: Boolean,
    val baseVersion: String?
)

data class PushResult(
    val id: String,
    val updatedAt: String,
    val rowVersion: String,
    val status: String,
    val conflictReason: String?
)

data class Change(
    val id: String,
    val habitId: String,
    val dayKey: String,
    val completedAt: String,
    val deleted: Boolean,
    val updatedAt: String,
    val rowVersion: String
)

data class ChangesPage(
    val items: List<Change>,
    val hasMore: Boolean,
    val nextSince: String?
)

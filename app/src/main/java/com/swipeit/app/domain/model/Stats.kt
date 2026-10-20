package com.swipeit.app.domain.model

data class Stats(
    val unsorted: Int,
    val kept: Int,
    val trashed: Int,
    val trashBytes: Long,
)

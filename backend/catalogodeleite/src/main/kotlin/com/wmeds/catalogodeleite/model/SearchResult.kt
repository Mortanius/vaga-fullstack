package com.wmeds.catalogodeleite.model

data class SearchResult<T>(
    val items: Collection<T>,
    val totalCount: Int
)
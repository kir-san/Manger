package com.san.kir.data.models.datastore

data class Download(
    val concurrent: Boolean,
    val retry: Boolean,
    val wifi: Boolean,
)

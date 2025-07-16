package id.creatodidak.kp3k.api.newModel

import id.creatodidak.kp3k.helper.MediaType

data class MediaResponse(
    val id: Int,
    val nrp: String,
    val filename: String,
    val url: String,
    val type: MediaType,
    val createdAt: String
)

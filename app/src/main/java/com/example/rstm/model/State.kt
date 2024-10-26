package com.example.rstm.model

import android.net.Uri

data class State(
    var videoUriList: List<Uri>?,
    var accelerometerUri: String?,
    var gyroUri: String?,
    var locationUri: String?,
    var lightUri: String?,
    var timeUri: String?,
    var csvUri: Uri?
){
    constructor() : this(null, null, null, null, null, null, null)
}
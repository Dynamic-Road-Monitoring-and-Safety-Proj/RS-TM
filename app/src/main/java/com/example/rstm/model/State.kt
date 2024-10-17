package com.example.rstm.model

data class State(
    var videoUriList: String,
    var accelerometerUri: String?,
    var gyroUri: String?,
    var locationUri: String?,
    var lightUri: String?,
    var timeUri: String?
){
    constructor() : this("", null, null, null, null, null)
}
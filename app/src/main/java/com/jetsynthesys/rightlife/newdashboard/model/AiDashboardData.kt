package com.jetsynthesys.rightlife.newdashboard.model

import com.google.gson.annotations.SerializedName


data class AiDashboardData(
    @SerializedName("facial_scan") val facialScan: ArrayList<FacialScan>?,
    @SerializedName("userAnswers") val userAnswers: ArrayList<UserAnswer>?,
    @SerializedName("discoverData") val discoverData: List<DiscoverDataItem>?

)

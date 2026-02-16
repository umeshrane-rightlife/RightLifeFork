package com.jetsynthesys.rightlife.ui.deeplink


import android.content.Intent
import android.os.Bundle
import com.jetsynthesys.rightlife.BaseActivity
import com.jetsynthesys.rightlife.newdashboard.HomeNewActivity
import com.jetsynthesys.rightlife.ui.new_design.DataControlActivity


class DeepLinkRouterActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val authToken = sharedPreferenceManager.accessToken
        if (authToken.isEmpty()) {
            startActivity(Intent(this, DataControlActivity::class.java))
            finish()
        } else {
            handleDeepLink()
        }
    }

    private fun handleDeepLink() {
        val data = intent?.data ?: return
        val rawPath = data.path ?: ""

        // Remove the leading slash if it exists to match our Enum slugs
        val cleanPath = if (rawPath.startsWith("/")) rawPath.substring(1) else rawPath

        val nextIntent = Intent(this, HomeNewActivity::class.java)

        // 1. Try to find a direct match in our Enum
        val target = DeepLinkTarget.fromSlug(cleanPath)

        if (target != null) {
            nextIntent.putExtra(DeepLinkTarget.EXTRA_DEEP_LINK_TARGET, target)
        }
        // 2. Handle special cases or legacy dynamic paths (Article/Audio/Video)
        else {
            val fallbackTarget = when {
                cleanPath.contains("article") -> DeepLinkTarget.ARTICLE
                cleanPath.contains("audio") -> DeepLinkTarget.AUDIO
                cleanPath.contains("video") -> DeepLinkTarget.VIDEO
                else -> DeepLinkTarget.HOME
            }
            nextIntent.putExtra(DeepLinkTarget.EXTRA_DEEP_LINK_TARGET, fallbackTarget)

            // If it was a dynamic path, you might want to pass the raw path as a Detail ID
            nextIntent.putExtra(DeepLinkTarget.EXTRA_DEEP_LINK_DETAIL_ID, rawPath)
        }

        startActivity(nextIntent)
        finish()
    }
}



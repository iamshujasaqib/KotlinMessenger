package com.group.messenger

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ImageView
import com.group.messenger.messages.MessagesActivity

class Splash_Screen : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash_screen)

        var splash: ImageView = findViewById(R.id.splashLogo)

        splash.alpha =0f
        splash.animate().setDuration(1500).alpha(1f).withEndAction{
            startActivity(Intent(baseContext, MessagesActivity::class.java))
            overridePendingTransition(android.R.anim.fade_in,android.R.anim.fade_out)
            finish()
        }
    }
}
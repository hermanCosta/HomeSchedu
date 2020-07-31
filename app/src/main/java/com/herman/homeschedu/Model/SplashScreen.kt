package com.herman.homeschedu.Model

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.herman.homeschedu.Activity.MainActivity

public class SplashScreen : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish()
    }
}

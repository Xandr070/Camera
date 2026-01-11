package com.example.camera

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.commit
import com.example.camera.fragment.PhotoFragment

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        supportActionBar?.hide()
        
        if (savedInstanceState == null) {
            supportFragmentManager.commit {
                replace(android.R.id.content, PhotoFragment())
            }
        }
    }
}


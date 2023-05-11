package org.meicode.ceylonexportsadmin

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import org.meicode.ceylonexportsadmin.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.toolbar.setBackgroundResource(R.drawable.bottom_corer_round)
    }
}
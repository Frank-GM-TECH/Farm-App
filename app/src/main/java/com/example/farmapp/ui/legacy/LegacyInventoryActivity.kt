package com.example.farmapp.ui.legacy

import android.os.Bundle
import androidx.fragment.app.FragmentActivity
import com.example.farmapp.R

/** Actividad de laboratorio Views/XML. La app principal sigue utilizando Compose. */
class LegacyInventoryActivity : FragmentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_legacy_inventory)
    }
}

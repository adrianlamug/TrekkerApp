package com.example.myapplication.ui.fragments

import android.content.SharedPreferences
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import com.example.myapplication.R
import com.example.myapplication.other.Constants.KEY_NAME
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.fragment_settings.*
import javax.inject.Inject

// Settings Fragment where the user can change their name
@AndroidEntryPoint
class SettingsFragment : Fragment(R.layout.fragment_settings) {

    @Inject
    lateinit var sharedPreferences: SharedPreferences

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        loadFieldsFromSharedPreferences()
        btnApplyChanges.setOnClickListener{
            val success = applyChangesToSharedPreferences()
            if (success) {
                Snackbar.make(view, "Saved changes", Snackbar.LENGTH_LONG).show()
            } else {
                Snackbar.make(view, "Please fill out the fields", Snackbar.LENGTH_LONG).show()
            }
        }
    }
    // loads data that was saved from shared preferences
    private fun loadFieldsFromSharedPreferences() {
        val name = sharedPreferences.getString(KEY_NAME, "")
        etName.setText(name)
    }

    // edits and saves the values on the shared preferences
    // only currently changes the name
    private fun applyChangesToSharedPreferences(): Boolean {
        val nameText = etName.text.toString()
        if(nameText.isEmpty()) {
            return false
        }
        sharedPreferences.edit()
                .putString(KEY_NAME, nameText)
                .apply()
        val toolbarText = "Hello $nameText"
        requireActivity().tvToolbarTitle.text = toolbarText
        return true
    }
}
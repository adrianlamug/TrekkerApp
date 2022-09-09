package com.example.myapplication.ui.fragments

import android.content.SharedPreferences
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.navigation.NavOptions
import androidx.navigation.fragment.findNavController
import com.example.myapplication.R
import com.example.myapplication.other.Constants.KEY_FIRST_TOGGLE
import com.example.myapplication.other.Constants.KEY_NAME
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.fragment_setup.*
import javax.inject.Inject

// Setup Fragment only pops up when user launches app for the first time
@AndroidEntryPoint
class SetupFragment : Fragment(R.layout.fragment_setup) {

    @Inject
    lateinit var sharedPreferences: SharedPreferences

    @set:Inject
    var firstTimeAppOpen = true

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // If it's not the user's first time opening the app then navigate to main landing fragment
        if(!firstTimeAppOpen) {
            val navOptions = NavOptions.Builder()
                    .setPopUpTo(R.id.setupFragment, true)
                    .build()
            findNavController().navigate(
                    R.id.action_setupFragment_to_trekkFragment,
                    savedInstanceState,
                    navOptions
            )
        }

        // on click listener for continute button, writing name to shared preferences
        tvContinue.setOnClickListener {
            val success = writePersonalDatatoSharedPreferences()
            if(success){
                findNavController().navigate(R.id.action_setupFragment_to_trekkFragment)
            } else {
                Snackbar.make(requireView(), "Please fill in the field", Snackbar.LENGTH_SHORT).show()
            }
        }
    }

    // saves user data to shared preferences
    private fun writePersonalDatatoSharedPreferences(): Boolean {
        val name = etName.text.toString()
        if(name.isEmpty()) {
            return false
        }
        sharedPreferences.edit()
                .putString(KEY_NAME, name)
                .putBoolean(KEY_FIRST_TOGGLE, false)
                .apply()
        val toolbarText = "Hello $name"
        requireActivity().tvToolbarTitle.text = toolbarText
        return true
    }
}
package com.example.myapplication.ui.fragments

import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModel
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.myapplication.R
import com.example.myapplication.adapters.TrailAdapter
import com.example.myapplication.other.SortType
import com.example.myapplication.ui.viewModels.MainViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.fragment_customtrails.*
import kotlinx.android.synthetic.main.fragment_trail.*

// Fragment for custom trails, displays all the user saved trails
@AndroidEntryPoint
class CustomTrailsFragment : Fragment(R.layout.fragment_customtrails) {
    private val viewModel: MainViewModel by viewModels()
    private lateinit var trailAdapter : TrailAdapter


    private fun setupRecyclerView() = rvRuns.apply {
        trailAdapter = TrailAdapter()
        adapter = trailAdapter
        layoutManager = LinearLayoutManager(requireContext())

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
        when(viewModel.sortType) {
            SortType.DATE -> spFilter.setSelection(0)
            SortType.TRAIL_TIME -> spFilter.setSelection(1)
            SortType.AVG_SPEED -> spFilter.setSelection(2)
            SortType.DISTANCE -> spFilter.setSelection(3)
        }
        spFilter.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) {}
            // selecting the sort filters
            override fun onItemSelected(adapterView: AdapterView<*>?, view: View?, position: Int, id: Long) {
                when(position) {
                    0 -> viewModel.sortTrails(SortType.DATE)
                    1 -> viewModel.sortTrails(SortType.TRAIL_TIME)
                    2 -> viewModel.sortTrails(SortType.AVG_SPEED)
                    3 -> viewModel.sortTrails(SortType.DISTANCE)
                }
            }
        }

        viewModel.trails.observe(viewLifecycleOwner, Observer {
            trailAdapter.submitList(it)

        })
    }
}
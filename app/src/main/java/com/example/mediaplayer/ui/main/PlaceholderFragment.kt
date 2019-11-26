package com.example.mediaplayer.ui.main

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.mediaplayer.R
import com.example.mediaplayer.databinding.FragmentPlaceholderBinding
import com.example.mediaplayer.ui.favourite.FavouriteFragment
import com.example.mediaplayer.ui.playlist.PlayListFragment
import com.google.android.material.tabs.TabLayoutMediator

/**
 * A placeholder fragment that control the flow of rest of fragments through tab layout
 */
class PlaceholderFragment : Fragment() {
    override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        val binding = FragmentPlaceholderBinding.inflate(inflater)

        binding.pager.adapter = object : FragmentStateAdapter(this) {
            override fun getItemCount(): Int = 2
            override fun createFragment(position: Int): Fragment {
                // Return a NEW fragment instance in createFragment(int)
                return when (position) {
                    0 -> {
                        PlayListFragment()

                    }
                    else -> {
                        FavouriteFragment()
                    }
                }
            }
        }
        TabLayoutMediator(binding.tabLayout, binding.pager) { tab, position ->
            when (position) {
                0 -> {
                    tab.text = getString(R.string.playlist)
                    tab.icon = resources.getDrawable(R.drawable.ic_playlist, null)
                }
                1 -> {
                    tab.text = getString(R.string.favourite)
                    tab.icon = resources.getDrawable(R.drawable.ic_favourite, null)

                }

            }
        }.attach()
        return binding.root
    }
}
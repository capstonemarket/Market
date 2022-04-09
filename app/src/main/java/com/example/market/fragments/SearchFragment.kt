package com.example.market.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.navigation.findNavController
import com.example.market.R
import com.example.market.databinding.FragmentSearchBinding


class SearchFragment : Fragment() {

    private lateinit var binding : FragmentSearchBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_search, container, false)

        // Tap을 클릭하면 Fragment간 전환 부분
        binding.homeTap.setOnClickListener {

            it.findNavController().navigate(R.id.action_searchFragment_to_homeFragment)

        }

        binding.bookmarkTap.setOnClickListener {

            it.findNavController().navigate(R.id.action_searchFragment_to_bookmarkFragment)

        }

        binding.chatTap.setOnClickListener {

            it.findNavController().navigate(R.id.action_searchFragment_to_chatFragment)
        }

        binding.userTap.setOnClickListener {

            it.findNavController().navigate(R.id.action_searchFragment_to_userFragment)
        }

        return binding.root
    }

}
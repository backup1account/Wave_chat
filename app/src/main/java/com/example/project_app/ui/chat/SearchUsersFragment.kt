package com.example.project_app.ui.chat

import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.ImageButton
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.widget.SearchView
import androidx.navigation.fragment.findNavController
import com.example.project_app.FirebaseManager
import com.example.project_app.R
import com.example.project_app.auth.UserRepository
import com.example.project_app.auth.data_classes.User
import com.example.project_app.ui.profile.UserViewModel


class SearchUsersFragment : Fragment(), OnItemClickListener {

    private lateinit var userRepository: UserRepository
    private lateinit var userViewModel: UserViewModel

    private lateinit var rViewAdapter: SearchUsersRecyclerViewAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        userRepository = UserRepository(FirebaseManager.auth)
        userViewModel = UserViewModel(userRepository)
        rViewAdapter = SearchUsersRecyclerViewAdapter(this)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_search_users_list, container, false)
        val rView = view.findViewById<RecyclerView>(R.id.rViewSearchList)

        val loadingUsersProgressBar = view.findViewById<ProgressBar>(R.id.loadingIndicatorSearchUsers)
        val searchView = view.findViewById<SearchView>(R.id.searchUsersSearchView)
        val goBackBtn = view.findViewById<ImageButton>(R.id.go_back_search_users_btn)

        goBackBtn.setOnClickListener {
            requireActivity().onBackPressed()
        }

        searchView.post {
            searchView.requestFocus()
            val imm = requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.showSoftInput(searchView, InputMethodManager.SHOW_IMPLICIT)
        }

        if (rView is RecyclerView) {
            with(rView) {
                this.adapter = rViewAdapter
            }
        }

        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String): Boolean {
                return true
            }
            override fun onQueryTextChange(newText: String): Boolean {
                userViewModel.searchUsers(newText)
                return true
            }
        })

        userViewModel.searchUsersResult.observe(viewLifecycleOwner) { users ->
            rViewAdapter.submitList(users)
            if (users.isEmpty()) {
                loadingUsersProgressBar.visibility = View.VISIBLE
                rView.visibility = View.GONE
            } else {
                loadingUsersProgressBar.visibility = View.GONE
                rView.visibility = View.VISIBLE
            }
        }

        loadingUsersProgressBar.visibility = View.GONE
        rView.visibility = View.GONE

        return view
    }

    override fun onItemClick(user: User) {
        val action = SearchUsersFragmentDirections.actionSearchUsersFragmentToConversationFragment(user)
        findNavController().navigate(action)
    }

}
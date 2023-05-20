package com.example.project_app.ui.chat

import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.appcompat.widget.SearchView
import androidx.navigation.fragment.findNavController
import com.example.project_app.R
import com.example.project_app.auth.UserRepository
import com.example.project_app.placeholder.PlaceholderContent
import com.example.project_app.ui.profile.UserViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase


class ChatFragment : Fragment() {

    private lateinit var auth: FirebaseAuth
    private lateinit var userRepository: UserRepository
    private lateinit var userSettingsViewModel: UserViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        auth = Firebase.auth
        userRepository = UserRepository(auth)
        userSettingsViewModel = UserViewModel(userRepository)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_chat_list, container, false)
        val rView = view.findViewById<RecyclerView>(R.id.chat_messages_list)

        val userSettingsBtn = view.findViewById<ImageView>(R.id.user_settings)
        val userLogoutBtn = view.findViewById<ImageView>(R.id.user_logout)

        //  ____ Search users field redirection ___
        val searchView = view.findViewById<SearchView>(R.id.searchViewChat)
        searchView.setOnQueryTextFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                val action = ChatFragmentDirections.actionChatFragmentToSearchUsersFragment()
                findNavController().navigate(action)
            }
        }

        searchView.setOnClickListener {
            val action = ChatFragmentDirections.actionChatFragmentToSearchUsersFragment()
            findNavController().navigate(action)
        }
        //  _________________________


        userSettingsBtn.setOnClickListener {
            val action = ChatFragmentDirections.actionChatFragmentToUserSettingsFragment()
            findNavController().navigate(action)
        }

        userLogoutBtn.setOnClickListener {
            val action = ChatFragmentDirections.actionChatFragmentToLoginFragment()
            userSettingsViewModel.signOut()
            findNavController().navigate(action)
        }

        // Set the adapter
        if (rView is RecyclerView) {
            with(rView) {
//                layoutManager = when {
//                    columnCount <= 1 -> LinearLayoutManager(context)
//                    else -> GridLayoutManager(context, columnCount)
//                }
                adapter = MessagesRecyclerViewAdapter(PlaceholderContent.ITEMS)
            }
        }
        return view
    }

}
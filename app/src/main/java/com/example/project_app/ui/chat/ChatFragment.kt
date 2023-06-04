package com.example.project_app.ui.chat

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
import androidx.appcompat.widget.SearchView
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.project_app.FirebaseManager
import com.example.project_app.R
import com.example.project_app.auth.MessageRepository
import com.example.project_app.auth.UserRepository
import com.example.project_app.ui.profile.UserViewModel
import com.google.android.material.appbar.MaterialToolbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.ktx.Firebase


class ChatFragment : Fragment() {

    private lateinit var auth: FirebaseAuth
    private lateinit var userRepository: UserRepository
    private lateinit var userSettingsViewModel: UserViewModel
    private lateinit var messageRepository: MessageRepository
    private lateinit var conversationViewModel : ConversationViewModel

    private lateinit var chatRecyclerViewAdapter: ChatRecyclerViewAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        auth = Firebase.auth
        userRepository = UserRepository(auth)
        userSettingsViewModel = UserViewModel(userRepository)

        messageRepository = MessageRepository(auth)
        conversationViewModel = ConversationViewModel(messageRepository)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_chat_list, container, false)
        val rView = view.findViewById<RecyclerView>(R.id.chat_messages_list)
        val topBar = view.findViewById<MaterialToolbar>(R.id.topAppBar)

        var receivedConversations = mutableListOf<DocumentSnapshot>() //  last messages only exactly ???

        chatRecyclerViewAdapter = ChatRecyclerViewAdapter(receivedConversations)

        topBar.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.user_settings -> {
                    val action = ChatFragmentDirections.actionChatFragmentToUserSettingsFragment()
                    findNavController().navigate(action)
                    true
                }
                R.id.user_logout -> {
                    val action = ChatFragmentDirections.actionChatFragmentToLoginFragment()
                    userSettingsViewModel.signOut()
                    findNavController().navigate(action)
                    true
                }
                else -> false
            }
        }

//          ____ Search users field redirection ___
        val searchView = view.findViewById<EditText>(R.id.searchViewChat)

        searchView.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                val action = ChatFragmentDirections.actionChatFragmentToSearchUsersFragment()
                findNavController().navigate(action)
            }
        }

//        conversationViewModel.getAllUsersConversations().observe(viewLifecycleOwner) {
//            Log.d("documentsnapshots", "$it")
//
//            receivedConversations.clear()
//            receivedConversations.addAll(it)
//            chatRecyclerViewAdapter.notifyDataSetChanged()
//        }

        // Set the adapter
        if (rView is RecyclerView) {
            with(rView) {
                layoutManager = LinearLayoutManager(context)
                adapter = chatRecyclerViewAdapter
            }
        }
        return view
    }

}
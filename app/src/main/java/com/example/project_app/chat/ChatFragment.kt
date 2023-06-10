package com.example.project_app.chat

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ProgressBar
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.project_app.R
import com.example.project_app.repositories.MessageRepository
import com.example.project_app.repositories.UserRepository
import com.example.project_app.data_classes.Conversation
import com.example.project_app.interfaces.OnConversationClickListener
import com.example.project_app.ui.profile.UserViewModel
import com.google.android.material.appbar.MaterialToolbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase


class ChatFragment : Fragment(), OnConversationClickListener {

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

        val loadingIndicator = view.findViewById<ProgressBar>(R.id.loadingIndicator)
        loadingIndicator.visibility = View.VISIBLE

        val receivedConversations = mutableListOf<Conversation>()
        chatRecyclerViewAdapter = ChatRecyclerViewAdapter(receivedConversations, this)

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

        conversationViewModel.getAllUsersConversations().observe(viewLifecycleOwner) {
            loadingIndicator.visibility = View.GONE
            rView.visibility = View.VISIBLE

            receivedConversations.clear()
            receivedConversations.addAll(it)
            chatRecyclerViewAdapter.notifyDataSetChanged()
            Log.d("a", "$receivedConversations")
        }

        // Set the adapter
        if (rView is RecyclerView) {
            with(rView) {
                layoutManager = LinearLayoutManager(context)
                adapter = chatRecyclerViewAdapter
            }
        }
        return view
    }

    override fun onConversationClick(conversation: Conversation) {
        val action = conversation.receiverUser?.let {
            ChatFragmentDirections.actionChatFragmentToConversationFragment(it)
        }
        action?.let { findNavController().navigate(it) }
    }

}
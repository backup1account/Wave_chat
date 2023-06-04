package com.example.project_app.ui.chat

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CircleCrop
import com.bumptech.glide.request.RequestOptions

import com.example.project_app.FirebaseManager
import com.example.project_app.R
import com.example.project_app.auth.MessageRepository
import com.example.project_app.auth.data_classes.Message
import com.example.project_app.auth.data_classes.User
import com.google.android.material.textfield.TextInputEditText
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking


class ConversationFragment : Fragment() {

    private lateinit var messageRepository: MessageRepository
    private lateinit var conversationViewModel : ConversationViewModel
    private lateinit var conversationRecyclerViewAdapter: ConversationRecyclerViewAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        messageRepository = MessageRepository(FirebaseManager.auth)
        conversationViewModel = ConversationViewModel(messageRepository)
    }

    @SuppressLint("SetTextI18n")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_conversation_list, container, false)
        val rView = view.findViewById<RecyclerView>(R.id.conversationRView)

        // initially set adapter with messages as empty
        val fetchedMessages : MutableList<Message> = mutableListOf()

        conversationRecyclerViewAdapter = ConversationRecyclerViewAdapter(fetchedMessages)
        // _________

        val user: User? = arguments?.getParcelable("user")

        val senderId = FirebaseManager.auth.uid
        val receiverId = user?.documentId

        val nameNavbar = view.findViewById<TextView>(R.id.conversationUserNameNavbar)
//        val imageNavbar = view.findViewById<ImageView>(R.id.conversationUserPfpNavbar)
        val goBackBtn = view.findViewById<ImageButton>(R.id.go_back_from_conversation_btn)

        val messageContentTextField = view.findViewById<TextInputEditText>(R.id.sendMessageTextInputEditText)
        messageContentTextField.maxLines = 4   // set max height to lines

        val dictionaryMessages = view.findViewById<ImageButton>(R.id.selectCompleteSentencesMsgBtn)
        val sendMessages = view.findViewById<ImageButton>(R.id.sendMessageBtn)

        goBackBtn.setOnClickListener {
            requireActivity().onBackPressed()
        }

        if (user != null) {
            nameNavbar.text = "${user.name} #${user.randomIndex}"

//            Glide.with(this)
//                .load(user.profPictureUrl)
//                .apply(RequestOptions().transform(CircleCrop()))
//                .into(imageNavbar)
        }

        sendMessages.setOnClickListener {
            if ( !(messageContentTextField.text.isNullOrBlank()) && (user != null) ) {
                val messageToSend = Message(
                    senderId = senderId,
                    receiverId = receiverId,
                    messageContent = messageContentTextField.text!!.toString(),
                    messageType = "text",  //  button's only for text messages, images should be sent separately
                    timestamp = System.currentTimeMillis()
                )

                conversationViewModel.sendMessage(messageToSend)
                messageContentTextField.setText("")  // clear text
            }
        }

        if (senderId != null && receiverId != null) {
            conversationViewModel.getConversation(senderId, receiverId).observe(viewLifecycleOwner) { messages ->
                fetchedMessages.clear()
                fetchedMessages.addAll(messages)
                conversationRecyclerViewAdapter.notifyDataSetChanged()
            }
        }

        if (rView is RecyclerView) {
            with(rView) {
                layoutManager = LinearLayoutManager(context)
                adapter = conversationRecyclerViewAdapter
            }
        }
        return view
    }

}
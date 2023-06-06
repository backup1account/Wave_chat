package com.example.project_app.ui.chat

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
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
import com.example.project_app.auth.MessagesDictionaryRepository
import com.example.project_app.auth.UserRepository
import com.example.project_app.auth.data_classes.Message
import com.example.project_app.auth.data_classes.User
import com.example.project_app.ui.profile.UserViewModel
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textfield.TextInputEditText
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking


class ConversationFragment : Fragment() {

    private lateinit var messageRepository: MessageRepository
    private lateinit var conversationViewModel : ConversationViewModel
    private lateinit var conversationRecyclerViewAdapter: ConversationRecyclerViewAdapter

    private lateinit var userRepository: UserRepository
    private lateinit var userViewModel: UserViewModel

    private lateinit var messagesDictionaryRepository: MessagesDictionaryRepository
    private lateinit var messagesDictionaryViewModel: MessagesDictionaryViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        messageRepository = MessageRepository(FirebaseManager.auth)
        conversationViewModel = ConversationViewModel(messageRepository)

        userRepository = UserRepository(FirebaseManager.auth)
        userViewModel = UserViewModel(userRepository)

        messagesDictionaryRepository = MessagesDictionaryRepository(FirebaseManager.auth)
        messagesDictionaryViewModel = MessagesDictionaryViewModel(messagesDictionaryRepository)
    }

    @SuppressLint("SetTextI18n")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_conversation_list, container, false)
        val rView = view.findViewById<RecyclerView>(R.id.conversationRView)

        val topNavbar = view.findViewById<MaterialToolbar>(R.id.topAppBar)

        val dictionaryMessagesList = mutableListOf<String>()

        // initially set adapter with messages as empty
        val fetchedMessages : MutableList<Message> = mutableListOf()

        conversationRecyclerViewAdapter = ConversationRecyclerViewAdapter(fetchedMessages)
        // _________

        val user: User? = arguments?.getParcelable("user")

        val senderId = FirebaseManager.auth.uid
        val receiverId = user?.documentId

        topNavbar.setNavigationOnClickListener {
            requireActivity().onBackPressed()
        }

        val sendMessageEditText = view.findViewById<EditText>(R.id.sendMessageEditText)
        sendMessageEditText.maxLines = 4   // set max height to lines

        val dictionaryMessages = view.findViewById<ImageButton>(R.id.selectCompleteSentencesMsgBtn)
        val sendMessages = view.findViewById<ImageButton>(R.id.sendMessageBtn)

        if (user != null) {
            topNavbar.title = "${user.name}"
        }


        dictionaryMessages.setOnClickListener {
            messagesDictionaryViewModel.getDictionaryMessages().observe(viewLifecycleOwner) {
                dictionaryMessagesList.clear()
                dictionaryMessagesList.addAll(it)

                context?.let { it1 ->
                    MaterialAlertDialogBuilder(
                        it1,
                        com.google.android.material.R.style.ThemeOverlay_MaterialComponents_MaterialAlertDialog
                    )
                        .setTitle(resources.getString(R.string.messages_dictionary_title))
                        .setItems(dictionaryMessagesList.toTypedArray()) { dialog, which ->
                            if (dictionaryMessagesList.isNotEmpty()) {
                                val selectedItem = dictionaryMessagesList[which]
                                sendMessageEditText.setText(selectedItem)
                            }
                            dialog.dismiss()
                        }
                        .setNeutralButton(R.string.messages_dictionary_cancel, null)
                        .setPositiveButton("Add item") { dialog, _ ->
                            dialog.dismiss()
                            showNewWordDialog()
                        }
                        .show()
                }
            }
        }

        sendMessages.setOnClickListener {
            if ( !(sendMessageEditText.text.isNullOrBlank()) && (user != null) ) {
                val messageToSend = Message(
                    senderId = senderId,
                    receiverId = receiverId,
                    messageContent = sendMessageEditText.text!!.toString(),
                    messageType = "text",  //  button's only for text messages, images should be sent separately
                    timestamp = System.currentTimeMillis()
                )

                conversationViewModel.sendMessage(messageToSend)
                sendMessageEditText.setText("")  // clear text
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

    private fun showNewWordDialog() {
        val dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_add_item_dictionary, null)
        val editText = dialogView.findViewById<EditText>(R.id.editTextNewWordToDictionary)

        val dialog = context?.let {
            MaterialAlertDialogBuilder(it)
                .setTitle("Add message do dictionary")
                .setView(dialogView)
                .setPositiveButton("Save") { dialog, _ ->
                    val enteredText = editText.text.toString()
                    if (enteredText.isNotBlank()) {
                        Log.d("a", "$enteredText")
                        messagesDictionaryViewModel.saveMessageToDictionary(enteredText)
                    }
                    dialog.dismiss()
                }
                .setNegativeButton("Cancel") { dialog, _ ->
                    dialog.dismiss()
                }
                .create()
        }

        dialog?.show()
    }

}
package com.example.project_app.ui.chat

import android.content.Context
import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import com.example.project_app.FirebaseManager
import com.example.project_app.R
import com.example.project_app.auth.data_classes.Message


class ConversationRecyclerViewAdapter(
    private val messageList: List<Message>,
) : ListAdapter<Message, RecyclerView.ViewHolder>(DiffCallback) {

    companion object {
        const val VIEW_TYPE_SENDER = 0
        const val VIEW_TYPE_RECEIVER = 1
    }

    override fun getItemCount(): Int {
        return messageList.size
    }

    override fun getItemViewType(position: Int): Int {
        val message = messageList[position]
        return if (message.senderId == FirebaseManager.auth.uid) {
            VIEW_TYPE_SENDER
        } else {
            VIEW_TYPE_RECEIVER
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        return when (viewType) {
            VIEW_TYPE_SENDER -> {
                val senderView = layoutInflater.inflate(
                    R.layout.fragment_conversation_sender, parent, false
                )
                SenderViewHolder(senderView)
            }
            VIEW_TYPE_RECEIVER -> {
                val receiverView = layoutInflater.inflate(
                    R.layout.fragment_conversation_receiver, parent, false
                )
                ReceiverViewHolder(receiverView)
            }
            else -> throw IllegalArgumentException("Invalid view type")
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val message = messageList[position]

        when (holder) {
            is SenderViewHolder -> {
                holder.bind(message)
            }
            is ReceiverViewHolder -> {
                holder.bind(message)
            }
        }
    }

    class SenderViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val senderMessageTextView: TextView = itemView.findViewById(R.id.senderContent)
        fun bind(message: Message) {
            senderMessageTextView.text = message.messageContent
        }
    }

    class ReceiverViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val receiverMessageTextView: TextView = itemView.findViewById(R.id.receiverContent)
        fun bind(message: Message) {
            receiverMessageTextView.text = message.messageContent
        }
    }

    object DiffCallback : DiffUtil.ItemCallback<Message>() {
        override fun areItemsTheSame(oldItem: Message, newItem: Message): Boolean {
            return oldItem.senderId == newItem.senderId &&
                    oldItem.receiverId == newItem.receiverId
        }

        override fun areContentsTheSame(oldItem: Message, newItem: Message): Boolean {
            return oldItem == newItem
        }
    }

}

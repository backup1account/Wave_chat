package com.example.project_app.ui.chat

import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CircleCrop
import com.bumptech.glide.request.RequestOptions
import java.text.SimpleDateFormat
import java.util.*

import com.example.project_app.FirebaseManager
import com.example.project_app.auth.data_classes.Conversation
import com.example.project_app.databinding.FragmentChatBinding


/*
    Adapter for displaying lists of conversations on chat main page
*/

class ChatRecyclerViewAdapter(
    private val conversations: List<Conversation>,
    private val conversationClickListener: OnConversationClickListener
) : RecyclerView.Adapter<ChatRecyclerViewAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(FragmentChatBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        ))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = conversations[position]
        val format = SimpleDateFormat("HH:mm", Locale.getDefault())
        val date = item.latestMessage?.timestamp?.let { Date(it) }

        holder.timeMessageView.text = date?.let { format.format(it).toString() }
        holder.receiverNameView.text = item.receiverUser?.name

        if (item.latestMessage?.senderId == FirebaseManager.auth.uid) {
            holder.latestMessageView.text = "Me: " + item.latestMessage?.messageContent
        } else {
            holder.latestMessageView.text = item.latestMessage?.messageContent
        }

        Glide.with(holder.itemView)
            .load(item.receiverUser?.profPictureUrl)
            .apply(RequestOptions().transform(CircleCrop()))
            .into(holder.receiverPfpView)

        holder.itemView.setOnClickListener {
            conversationClickListener.onConversationClick(item)
        }
    }

    override fun getItemCount(): Int = conversations.size

    inner class ViewHolder(binding: FragmentChatBinding) : RecyclerView.ViewHolder(binding.root) {
        val receiverNameView: TextView = binding.receiverNameChatHomepage
        val receiverPfpView: ImageView = binding.receiverPfpChatHomepage
        val latestMessageView: TextView = binding.latestMessageChatHomepage
        val timeMessageView: TextView = binding.timeMessageChatHomepage
    }

}
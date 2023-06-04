package com.example.project_app.ui.chat

import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView

import com.example.project_app.databinding.FragmentChatBinding
import com.google.firebase.firestore.DocumentSnapshot


/*
    Adapter for displaying lists of conversations on chat main page
*/

class ChatRecyclerViewAdapter(
    private val conversations: List<DocumentSnapshot>
) : RecyclerView.Adapter<ChatRecyclerViewAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(FragmentChatBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        ))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = conversations[position]
        holder.idView.text = item.id
//        holder.contentView.text = item.content
    }

    override fun getItemCount(): Int = conversations.size

    inner class ViewHolder(binding: FragmentChatBinding) : RecyclerView.ViewHolder(binding.root) {
        val idView: TextView = binding.itemNumber
        val contentView: TextView = binding.content

        override fun toString(): String {
            return super.toString() + " '" + contentView.text + "'"
        }
    }

}
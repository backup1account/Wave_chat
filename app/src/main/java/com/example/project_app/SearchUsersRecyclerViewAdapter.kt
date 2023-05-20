package com.example.project_app

import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CircleCrop
import com.bumptech.glide.request.RequestOptions
import com.example.project_app.auth.data_classes.User
import com.example.project_app.databinding.FragmentSearchUsersBinding
import com.example.project_app.ui.chat.OnItemClickListener


class SearchUsersRecyclerViewAdapter(private val itemClickListener: OnItemClickListener) :
    ListAdapter<User, SearchUsersRecyclerViewAdapter.ViewHolder>(UserDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = FragmentSearchUsersBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = getItem(position)
        holder.bind(item)
        holder.itemView.setOnClickListener {
            itemClickListener.onItemClick(item)
        }
    }

    inner class ViewHolder(private val binding: FragmentSearchUsersBinding) :
        RecyclerView.ViewHolder(binding.root) {
        private val userIdView: TextView = binding.searchedUserIndex
        private val userNameView: TextView = binding.searchedUserName
        private val userPfpView: ImageView = binding.searchedUserPfp

        fun bind(user: User) {
            userIdView.text = "#${user.randomIndex}"
            userNameView.text = user.name
            Glide.with(binding.root)
                .load(user.profPictureUrl)
                .apply(RequestOptions().transform(CircleCrop()))
                .into(userPfpView)
        }
    }

    private class UserDiffCallback : DiffUtil.ItemCallback<User>() {
        override fun areItemsTheSame(oldItem: User, newItem: User): Boolean {
            return oldItem.randomIndex == newItem.randomIndex
        }

        override fun areContentsTheSame(oldItem: User, newItem: User): Boolean {
            return oldItem == newItem
        }
    }

}
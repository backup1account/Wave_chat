package com.example.project_app.ui.chat

import com.example.project_app.auth.data_classes.User

interface OnItemClickListener {
    fun onItemClick(user: User)  // on user click
}
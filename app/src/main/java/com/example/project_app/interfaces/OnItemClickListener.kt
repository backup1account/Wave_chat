package com.example.project_app.interfaces

import com.example.project_app.data_classes.User

interface OnItemClickListener {
    fun onItemClick(user: User)  // on user click
}
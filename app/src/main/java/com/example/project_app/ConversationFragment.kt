package com.example.project_app

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CircleCrop
import com.bumptech.glide.request.RequestOptions
import com.example.project_app.auth.data_classes.User
import com.example.project_app.placeholder.PlaceholderContent


class ConversationFragment : Fragment() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    @SuppressLint("SetTextI18n")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_conversation_list, container, false)
        val rView = view.findViewById<RecyclerView>(R.id.conversationRView)

        val user: User? = arguments?.getParcelable("user")

        val nameNavbar = view.findViewById<TextView>(R.id.conversationUserNameNavbar)
        val imageNavbar = view.findViewById<ImageView>(R.id.conversationUserPfpNavbar)
        val goBackBtn = view.findViewById<ImageButton>(R.id.go_back_from_conversation_btn)

        goBackBtn.setOnClickListener {
            requireActivity().onBackPressed()
        }

        if (user != null) {
            nameNavbar.text = "${user.name} #${user.randomIndex}"

            Glide.with(this)
                .load(user.profPictureUrl)
                .apply(RequestOptions().transform(CircleCrop()))
                .into(imageNavbar)
        }

        // Set the adapter
        if (rView is RecyclerView) {
            with(rView) {
                adapter = ConversationRecyclerViewAdapter(PlaceholderContent.ITEMS)
            }
        }
        return view
    }

}
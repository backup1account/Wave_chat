package com.example.project_app.ui.profile

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.example.project_app.FirebaseManager
import com.example.project_app.R
import com.example.project_app.auth.UserRepository
import com.example.project_app.utils.Validators.checkName
import com.example.project_app.utils.Validators.checkPassword
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import jp.wasabeef.glide.transformations.RoundedCornersTransformation


class UserSettingsFragment : Fragment() {
    private val GALLERY_REQUEST_CODE = 1  // request code to identify the gallery intent
    private lateinit var userId: String

    private lateinit var userRepository: UserRepository
    private lateinit var userSettingsViewModel: UserViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        userRepository = UserRepository(FirebaseManager.auth)
        userSettingsViewModel = UserViewModel(userRepository)
        userId = FirebaseManager.auth.currentUser?.uid.toString()
    }

    @SuppressLint("IntentReset")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_user_settings, container, false)
        val goBackBtn = view.findViewById<ImageButton>(R.id.go_back_settings_btn)

        val nameTextInputLayout = view.findViewById<TextInputLayout>(R.id.nameTextInputLayout)
        val nameTextInputEditText = view.findViewById<TextInputEditText>(R.id.nameTextInputEditText)

        val emailTextInputEditText = view.findViewById<TextInputEditText>(R.id.emailTextInputEditText)

        val passwordTextInputLayout = view.findViewById<TextInputLayout>(R.id.passwordTextInputLayout)
        val passwordTextInputEditText = view.findViewById<TextInputEditText>(R.id.passwordTextInputEditText)

        val profilePictureChangeBtn = view.findViewById<ImageButton>(R.id.changeProfilePictureBtn)

        emailTextInputEditText.setText("${FirebaseManager.auth.currentUser?.email}")
        emailTextInputEditText.isEnabled = false

        val successButton = view.findViewById<Button>(R.id.save_btn_settings_user)

        val newPasswordHelperText = "Password must contain at least: \n• 5 letters" +
                "\t• 1 number \t• 1 uppercase letter"


        val userRef = FirebaseManager.firestore.collection("users").document(
            FirebaseManager.auth.currentUser?.uid ?: ""
        )

        val picturePlaceholderSettings = view.findViewById<ImageView>(R.id.settings_user_pfp)

        //  an image button to return to the previous fragment
        goBackBtn.setOnClickListener {
            requireActivity().onBackPressed()
        }


        // to generalnie do osobnej funkcji - to bylo probne
//        userRef.get().addOnSuccessListener { documentSnapshot ->
//            if (documentSnapshot.exists()) {
//                val profilePictureUrl = documentSnapshot.getString("profPictureUrl")
//
//                Glide.with(this)
//                    .load(profilePictureUrl)
//                    .transform(CenterCrop(), RoundedCornersTransformation(20, 0))
//                    .into(picturePlaceholderSettings)
//            }
//        }.addOnFailureListener { exception ->
//            // TODO: Dialogboxa z errorem ze nie udalo sie zaladowac obrazka
//            Log.e("PFP RETRIEVING ERROR", "Error retrieving profile picture: ${exception.message}", exception)
//        }

        // ___________  SELECT IMAGE FROM GALLERY  ____________

        profilePictureChangeBtn.setOnClickListener {
            val galleryIntent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            galleryIntent.type = "image/*"

            startActivityForResult(galleryIntent, GALLERY_REQUEST_CODE)  // start gallery activity and wait for the result
        }


        // ___________  NAME, PASSWORD UPDATE  ____________

        passwordTextInputLayout.helperText = newPasswordHelperText

        successButton.setOnClickListener {
            val name = nameTextInputEditText.text.toString().trim()
            val password = passwordTextInputEditText.text.toString().trim()

            if (name.isNotEmpty()) {
                val checkIfNameValid = checkName(name)

                if (!checkIfNameValid) {
                    nameTextInputLayout.error = "Enter valid new name"
                    nameTextInputEditText.text = null
                } else {
                    userSettingsViewModel.updateName(name)
                    clearFields(nameTextInputLayout, nameTextInputEditText)
                }
            }

            if (password.isNotEmpty()) {
                val checkIfPasswordValid = checkPassword(password)

                if (!checkIfPasswordValid) {
                    passwordTextInputLayout.error = "Enter valid new password"
                    passwordTextInputEditText.text = null
                } else {
                    userSettingsViewModel.updatePassword(password)
                    clearFields(passwordTextInputLayout, passwordTextInputEditText)
                }
            }
        }

        return view
    }


    private fun clearFields(textInputLayout: TextInputLayout, textInputEditText: TextInputEditText) {
        textInputEditText.text = null  // clear the text
        textInputLayout.error = null  // clear the error message
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == GALLERY_REQUEST_CODE && resultCode == Activity.RESULT_OK && data != null) {
            val selectedImageUri = data.data

            val profilePicRef = FirebaseManager.firebaseStorage.reference.child(
                "profile_pictures/${FirebaseManager.auth.currentUser?.uid}"
            )


        }
    }

}
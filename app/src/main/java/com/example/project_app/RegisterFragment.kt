package com.example.project_app

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.navigation.fragment.findNavController
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

import com.example.project_app.data.Validators.checkEmail
import com.example.project_app.data.Validators.checkPassword


class RegisterFragment : Fragment() {

    private lateinit var rName: TextInputEditText
    private lateinit var rEmail: TextInputEditText
    private lateinit var rPassword: TextInputEditText
    private lateinit var btnSignUp: Button
    private lateinit var btnLoginRedirection: Button

    private lateinit var nameLayout: TextInputLayout
    private lateinit var emailLayout: TextInputLayout
    private lateinit var passwordLayout: TextInputLayout

    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        auth = Firebase.auth
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_register, container, false)
        val redirectionAction = RegisterFragmentDirections.actionRegisterFragmentToLoginFragment2()

        val helperText = "Password must contain at least: \n• 5 letters" +
                "\n• 1 number \n• 1 uppercase letter"

        rName = view?.findViewById(R.id.rNameField)!!
        rEmail = view.findViewById(R.id.rEmailField)!!
        rPassword = view.findViewById(R.id.rPasswordField)!!
        btnSignUp = view.findViewById(R.id.registerButton)
        btnLoginRedirection = view.findViewById(R.id.loginRedirectionButton)

        nameLayout = view.findViewById(R.id.rNameLayoutField)
        emailLayout = view.findViewById(R.id.rEmailLayoutField)
        passwordLayout = view.findViewById(R.id.rPasswordLayoutField)

        passwordLayout.helperText = helperText

        btnSignUp.setOnClickListener {
            signUpUser()
        }

        btnLoginRedirection.setOnClickListener {
            findNavController().navigate(redirectionAction)
        }

        return view
    }

    private fun signUpUser() {
        val name = rName.text.toString()
        val email = rEmail.text.toString()
        val password = rPassword.text.toString()

        var authStepAvailable = true

        if (name.isBlank()) {
            nameLayout.error = "Name cannot be blank"
            authStepAvailable = false
        }
        if (!checkEmail(email)) {
            emailLayout.error = "Enter valid e-mail address"
            authStepAvailable = false
        }
        if (email.isBlank()) {
            emailLayout.error = "E-mail cannot be blank"
            authStepAvailable = false
        }
        if (!checkPassword(password)) {
            passwordLayout.error = "Enter valid password"
            authStepAvailable = false
        }
        if (password.isBlank()) {
            passwordLayout.error = "Password cannot be blank"
            authStepAvailable = false
        }

        if (!authStepAvailable) {
            return
        }

        // If all credentials are correct
        auth.createUserWithEmailAndPassword(email, password).addOnCompleteListener {
            if (it.isSuccessful) {
                val action = RegisterFragmentDirections.actionRegisterFragmentToChatFragment()
                findNavController().navigate(action)
            } else {
                Log.d("a", "registration failed!")
                // TODO: make some dialog box with information about failed registration or sth
            }
        }
    }

}
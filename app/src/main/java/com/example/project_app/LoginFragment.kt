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

class LoginFragment : Fragment() {

    private lateinit var lEmail: TextInputEditText
    private lateinit var lPassword: TextInputEditText
    private lateinit var btnSignIn: Button
    private lateinit var btnRegisterRedirection: Button

    private lateinit var emailLayout: TextInputLayout
    private  lateinit var passwordLayout: TextInputLayout

    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        auth = Firebase.auth
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_login, container, false)
        val redirectionAction = LoginFragmentDirections.actionLoginFragmentToRegisterFragment2()

        lEmail = view.findViewById(R.id.lEmailField)
        lPassword = view.findViewById(R.id.lPasswordField)
        btnSignIn = view.findViewById(R.id.loginButton)
        btnRegisterRedirection = view.findViewById(R.id.registerRedirectionButton)

        emailLayout = view?.findViewById(R.id.lEmailLayoutField)!!
        passwordLayout = view.findViewById(R.id.lPasswordLayoutField)!!

        btnSignIn.setOnClickListener {
            signIn()
        }

        btnRegisterRedirection.setOnClickListener {
            findNavController().navigate(redirectionAction)
        }

        return view
    }

    private fun signIn() {
        val email = lEmail.text.toString()
        val password = lPassword.text.toString()

        var authStepAvailable = true

        if (!checkEmail(email)) {
            emailLayout.error = "Enter valid e-mail address"
            authStepAvailable = false
        }
        if (email.isBlank()) {
            emailLayout.error = "E-mail cannot be blank"
            authStepAvailable = false
        }
        if (password.isBlank()) {
            passwordLayout.error = "Password cannot be blank"
            authStepAvailable = false
        }

        if (!authStepAvailable) {
            return
        }

        auth.signInWithEmailAndPassword(email, password).addOnCompleteListener {
            if(it.isSuccessful) {
                val action = LoginFragmentDirections.actionLoginFragmentToChatFragment()
                findNavController().navigate(action)
            } else {
                Log.d("a", "login failed!")
                // TODO: make some dialog box with information about failed login or sth
            }
        }
    }

}
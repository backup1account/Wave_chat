package com.example.project_app.ui.auth

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.project_app.FirebaseManager
import com.example.project_app.R
import com.example.project_app.auth.AuthRepository
import com.example.project_app.utils.Result
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.example.project_app.utils.Validators.checkEmail
import com.example.project_app.utils.Validators.checkPassword
import kotlinx.coroutines.launch


class LoginFragment : Fragment() {

    private lateinit var lEmail: TextInputEditText
    private lateinit var lPassword: TextInputEditText
    private lateinit var btnSignIn: Button
    private lateinit var btnRegisterRedirection: Button

    private lateinit var emailLayout: TextInputLayout
    private  lateinit var passwordLayout: TextInputLayout

    private lateinit var authRepository: AuthRepository
    private lateinit var loginViewModel: LoginViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        authRepository = AuthRepository(FirebaseManager.auth)
        loginViewModel = LoginViewModel(AuthRepository(FirebaseManager.auth))
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_login, container, false)
        val redirectionAction = LoginFragmentDirections.actionLoginFragmentToRegisterFragment2()

        lEmail = view.findViewById(R.id.lEmailField)
        lPassword = view.findViewById(R.id.lPasswordField)
        btnSignIn = view.findViewById(R.id.loginButton)
        btnRegisterRedirection = view.findViewById(R.id.registerRedirectionButton)

        emailLayout = view?.findViewById(R.id.lEmailLayoutField)!!
        passwordLayout = view.findViewById(R.id.lPasswordLayoutField)!!

        btnSignIn.setOnClickListener {
            login()
        }

        btnRegisterRedirection.setOnClickListener {
            findNavController().navigate(redirectionAction)
        }

        return view
    }

    private fun login() {
        val email = lEmail.text.toString()
        val password = lPassword.text.toString()

        emailLayout.error = null
        passwordLayout.error = null

        var authStepAvailable = true

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

        lifecycleScope.launch {
            loginViewModel.signIn(email, password).observe(viewLifecycleOwner) {
                when (it) {
                    is Result.Success -> {
                        val action = LoginFragmentDirections.actionLoginFragmentToChatFragment()
                        findNavController().navigate(action)
                    }

                    is Result.Error -> {
                        val text = "Login process failed"
                        Toast.makeText(context, text, Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

}
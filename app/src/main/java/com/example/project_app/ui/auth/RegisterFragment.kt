package com.example.project_app.ui.auth

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.project_app.FirebaseManager
import com.example.project_app.R
import com.example.project_app.auth.AuthRepository
import com.example.project_app.auth.UserRepository
import com.example.project_app.auth.data_classes.User
import com.example.project_app.ui.profile.UserViewModel
import com.example.project_app.utils.Generators
import com.example.project_app.utils.Result
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.example.project_app.utils.Validators.checkEmail
import com.example.project_app.utils.Validators.checkName
import com.example.project_app.utils.Validators.checkPassword
import kotlinx.coroutines.launch


class RegisterFragment : Fragment() {

    private lateinit var rName: TextInputEditText
    private lateinit var rEmail: TextInputEditText
    private lateinit var rPassword: TextInputEditText
    private lateinit var btnSignUp: Button
    private lateinit var btnLoginRedirection: Button

    private lateinit var nameLayout: TextInputLayout
    private lateinit var emailLayout: TextInputLayout
    private lateinit var passwordLayout: TextInputLayout

    private lateinit var authRepository: AuthRepository
    private lateinit var registerViewModel: RegisterViewModel

    private lateinit var userRepository: UserRepository
    private lateinit var profileViewModel: UserViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        authRepository = AuthRepository(FirebaseManager.auth)
        registerViewModel = RegisterViewModel(authRepository)

        userRepository = UserRepository(FirebaseManager.auth)
        profileViewModel = UserViewModel(userRepository)
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
            registration()
        }

        btnLoginRedirection.setOnClickListener {
            findNavController().navigate(redirectionAction)
        }

        return view
    }

    private fun registration() {
        val name = rName.text.toString()
        val email = rEmail.text.toString()
        val password = rPassword.text.toString()

        var authStepAvailable = true

        if (!checkName(name)) {
            nameLayout.error = "Only letters allowed"
            authStepAvailable = false
        }
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
        lifecycleScope.launch {
            registerViewModel.signUp(email, password).observe(viewLifecycleOwner) {
                when (it) {
                    is Result.Success -> {
                        val randomGeneratedIndex = Generators.generateRandomUserIndex()

                        val registeredUser = User(
                            name = name,
                            email = email,
                            profPictureUrl = "https://pm1.narvii.com/6755/8bf4fe6f5365d373ffe14121d94ffb75f9281e9fv2_hq.jpg",
                            randomIndex = randomGeneratedIndex
                        )

                        registerViewModel.addUserToDatabase(registeredUser)

                        val action = RegisterFragmentDirections.actionRegisterFragmentToChatFragment()
                        findNavController().navigate(action)
                    }

                    is Result.Error -> {
                        Log.d("a", "registration failed: ${it.exception.message}")
                        // TODO: make some dialog box with information about failed registration or sth
                    }
                }
            }
        }
    }

}
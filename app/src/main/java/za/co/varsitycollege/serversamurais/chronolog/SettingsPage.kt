package za.co.varsitycollege.serversamurais.chronolog

import android.os.Bundle
import android.text.Editable
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.Toast
import com.google.firebase.auth.FirebaseUser
import za.co.varsitycollege.serversamurais.chronolog.Helpers.FirebaseHelper

private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"


class SettingsPage : Fragment() {
    private var param1: String? = null
    private var param2: String? = null
    private lateinit var firebaseHelper: FirebaseHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
        firebaseHelper = FirebaseHelper(object : FirebaseHelper.FirebaseOperationListener {
            override fun onSuccess(user: FirebaseUser?) {
                Toast.makeText(context, "Operation successful!", Toast.LENGTH_SHORT).show()
                // Reload user data to update UI
                loadUserData()
            }

            override fun onFailure(errorMessage: String) {
                Toast.makeText(context, "Error: $errorMessage", Toast.LENGTH_SHORT).show()
            }
        })
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_settings_page, container, false).apply {
            val fullNameEditText = findViewById<EditText>(R.id.editTextFullName)
            val emailEditText = findViewById<EditText>(R.id.editEmail)
            val oldPasswordEditText = findViewById<EditText>(R.id.editOldPassword)
            val newPasswordEditText = findViewById<EditText>(R.id.editPassword)

            val editTexts = listOf(fullNameEditText, emailEditText, oldPasswordEditText, newPasswordEditText)
            editTexts.forEach { it.isEnabled = false }

            val editorButtons = listOf(
                findViewById<ImageButton>(R.id.fullnameEditor),
                findViewById<ImageButton>(R.id.emailEditor),
                findViewById<ImageButton>(R.id.passwordOldEditor),
                findViewById<ImageButton>(R.id.passwordEditor)
            )

            editorButtons.zip(editTexts).forEach { (button, editText) ->
                button.setOnClickListener {
                    editText.isEnabled = !editText.isEnabled
                    if (editText.isEnabled) {
                        editText.requestFocus()
                    } else {
                        editText.clearFocus()
                    }
                }
            }

            try {
                val user = firebaseHelper.getCurrentUser()
                user?.let {
                    Log.d("SettingsPage", "User: $user, Name: ${it.displayName}, Email: ${it.email}")
                    fullNameEditText.hint = user.displayName.toString()
                    emailEditText.hint = user.email.toString()
                }
            } catch (exception: Exception) {
                Log.e("SettingsPage", "Error: ${exception.message}")
            }

            findViewById<Button>(R.id.updateBtn).setOnClickListener {
                handleUpdateButtonClick(fullNameEditText, emailEditText, oldPasswordEditText, newPasswordEditText)
            }

            findViewById<Button>(R.id.logoutBtn).setOnClickListener {
                firebaseHelper.signOut()
            }
        }
    }

    private fun handleUpdateButtonClick(
        fullNameEditText: EditText, emailEditText: EditText,
        oldPasswordEditText: EditText, newPasswordEditText: EditText
    ) {
        val fullName = fullNameEditText.text.toString().trim()
        val email = emailEditText.text.toString().trim()
        val oldPassword = oldPasswordEditText.text.toString().trim()
        val newPassword = newPasswordEditText.text.toString().trim()

        if (newPassword.isNotEmpty() && oldPassword.isEmpty()) {
            Toast.makeText(context, "Old password is required to update the password.", Toast.LENGTH_SHORT).show()
            return
        }

        if (email.isNotEmpty() && oldPassword.isEmpty()) {
            Toast.makeText(context, "Old password is required to update the email.", Toast.LENGTH_SHORT).show()
            return
        }

        setFieldsEnabled(false, fullNameEditText, emailEditText, oldPasswordEditText, newPasswordEditText)

        try {
            updateDetails(fullName, email, oldPassword, newPassword)
        } catch (exception: Exception) {
            Toast.makeText(context, "Error: ${exception.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun updateDetails(fullName: String, email: String, oldPassword: String, newPassword: String) {
        val user = firebaseHelper.getCurrentUser()

        user?.let {
            if (fullName.isNotEmpty()) {
                firebaseHelper.updateFullName(fullName)
            }
            if (email.isNotEmpty() && oldPassword.isNotEmpty()) {
                firebaseHelper.sendEmailVerification(email, oldPassword)
                Toast.makeText(context, "Please check your new email address for a verification link.", Toast.LENGTH_LONG).show()
            } else {
                Toast.makeText(context, "Please enter the current password and new email.", Toast.LENGTH_SHORT).show()
            }

            if (newPassword.isNotEmpty() && oldPassword.isNotEmpty()) {
                firebaseHelper.updateUserPassword(oldPassword, newPassword)
            }
        }
    }

    private fun setFieldsEnabled(enabled: Boolean, vararg fields: EditText) {
        fields.forEach { it.isEnabled = enabled }
    }

    private fun loadUserData() {
        val user = firebaseHelper.getCurrentUser()
        val fullNameEditText = view?.findViewById<EditText>(R.id.editTextFullName)
        val emailEditText = view?.findViewById<EditText>(R.id.editEmail)

        user?.let {
            fullNameEditText?.setText(it.displayName)
            emailEditText?.setText(it.email)
        }
    }

    companion object {
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            SettingsPage().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}
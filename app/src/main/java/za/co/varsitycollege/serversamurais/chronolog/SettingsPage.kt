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

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [SettingsPage.newInstance] factory method to
 * create an instance of this fragment.
 */
class SettingsPage : Fragment() {
    // TODO: Rename and change types of parameters
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
            /**
             * Called when a Firebase operation is successful.
             * Displays a success message.
             */
            override fun onSuccess(user: FirebaseUser?) {
                Toast.makeText(context, "Operation successful!", Toast.LENGTH_SHORT).show()
            }

            /**
             * Called when a Firebase operation fails.
             * Displays the error message.
             */
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
            val editTexts = listOf(
                findViewById(R.id.editTextFullName),
                findViewById<EditText>(R.id.editPassword)
            )

            val emailTxt = findViewById<EditText>(R.id.editEmail)

            emailTxt.isEnabled = false




            val editorButtons = listOf(
                findViewById(R.id.fullnameEditor),
                findViewById<ImageButton>(R.id.passwordEditor)
            )

            editTexts.forEach { it.isEnabled = false }

            editorButtons.zip(editTexts).forEach { (button, editText) ->
                button.setOnClickListener {
                    editText.isEnabled = true
                    editText.requestFocus()
                }
            }

           try {
            // Get the current user
            val user = firebaseHelper.getCurrentUser()

            // Log the user object along with the displayName and email
            user?.let {
                Log.d("SettingsPage", "User: $user, Name: ${it.displayName}, Email: ${it.email}")
            }

            // Set the user's full name as the text of the EditText field
            user?.let {
                editTexts[0].text = Editable.Factory.getInstance().newEditable(it.displayName)
                emailTxt.text = Editable.Factory.getInstance().newEditable(it.email)
            }
        } catch (exception: Exception) {
            // Log the error
            Log.e("SettingsPage", "Error: ${exception.message}")
        }

            findViewById<Button>(R.id.updateBtn).setOnClickListener {
                handleUpdateButtonClick(editTexts[0], editTexts[1])
            }
        }
    }

    /**
     * Handles the click event of the update button.
     * Updates the user's full name and password.
     */
    private fun handleUpdateButtonClick(fullNameEditText: EditText, passwordEditText: EditText) {
        val fullname = fullNameEditText.text.toString()
        val password = passwordEditText.text.toString()

        // Make everything read-only again
        setFieldsEnabled(false, fullNameEditText, passwordEditText)

        try {
            // Perform update operation here
            updateDetails(fullname, password)

        } catch (exception: Exception) {
            Toast.makeText(context, "Error: ${exception.message}", Toast.LENGTH_SHORT).show()
        }
    }

    /**
     * Updates the user's full name and password.
     */
    private fun updateDetails(fullName: String, password: String) {
        listOf(fullName, password).takeIf { it.all(String::isNotEmpty) }?.let {
            firebaseHelper.run {
                updateFullName(fullName)
                updateUserPassword(password)
            }
        }
    }

    /**
     * Sets the enabled state of the specified EditText fields.
     */
    private fun setFieldsEnabled(enabled: Boolean, vararg fields: EditText) {
        fields.forEach { it.isEnabled = enabled }
    }
    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment SettingsPage.
         */
        // TODO: Rename and change types and number of parameters
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
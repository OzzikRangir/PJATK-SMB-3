package com.ozzikrangir.productlist.ui.login

import android.app.Activity
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.Toast
import androidx.annotation.NonNull
import androidx.annotation.RequiresApi
import androidx.annotation.StringRes
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.preference.PreferenceManager
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.ozzikrangir.productlist.R
import com.ozzikrangir.productlist.data.RealtimeDBConnector
import com.ozzikrangir.productlist.data.model.Product
import com.ozzikrangir.productlist.data.model.ProductList
import com.ozzikrangir.productlist.data.model.User
import com.ozzikrangir.productlist.ui.main.MainActivity


class LoginActivity : AppCompatActivity() {

    private lateinit var loginViewModel: LoginViewModel
    private lateinit var mAuth: FirebaseAuth

    @RequiresApi(Build.VERSION_CODES.R)
    override fun onCreate(savedInstanceState: Bundle?) {
        val preferences = PreferenceManager.getDefaultSharedPreferences(applicationContext)
        val alternative = preferences.getBoolean("alternative", false)
        if (alternative)
            setTheme(R.style.Theme_ProductListAlternative_NoActionBar)

        super.onCreate(savedInstanceState)

        mAuth = FirebaseAuth.getInstance()

        val currentUser = mAuth.currentUser
        if (currentUser != null) {
            val intent = Intent(this, MainActivity::class.java).apply {
            }
            mAuth = FirebaseAuth.getInstance()

            RealtimeDBConnector.init()
            RealtimeDBConnector.getUserData(currentUser.uid)
            if (RealtimeDBConnector.user == null)
                RealtimeDBConnector.addUser(User(currentUser.uid))

            startActivity(intent)
        }

        setContentView(R.layout.activity_login)

        var darkMode = preferences.getBoolean("darkmode", false)
        if (darkMode)
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
        else
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        delegate.applyDayNight()


        val username = findViewById<EditText>(R.id.username)
        val password = findViewById<EditText>(R.id.password)
        val login = findViewById<Button>(R.id.login)
        val loading = findViewById<ProgressBar>(R.id.loading)

        loginViewModel = ViewModelProvider(this, LoginViewModelFactory())
            .get(LoginViewModel::class.java)

        loginViewModel.loginFormState.observe(this@LoginActivity, Observer {
            val loginState = it ?: return@Observer

            // disable login button unless both username / password is valid
            login.isEnabled = loginState.isDataValid

            if (loginState.usernameError != null) {
                username.error = getString(loginState.usernameError)
            }
            if (loginState.passwordError != null) {
                password.error = getString(loginState.passwordError)
            }
        })

        loginViewModel.loginResult.observe(this@LoginActivity, Observer {
            val loginResult = it ?: return@Observer

            loading.visibility = View.GONE
            if (loginResult.error != null) {
                showLoginFailed(loginResult.error)
            }
            if (loginResult.success != null) {
                updateUiWithUser(loginResult.success)
                finish()
            }
            setResult(Activity.RESULT_OK)

            //Complete and destroy login activity once successful

        })

        username.afterTextChanged {
            loginViewModel.loginDataChanged(
                username.text.toString(),
                password.text.toString()
            )
        }

        password.apply {
            afterTextChanged {
                loginViewModel.loginDataChanged(
                    username.text.toString(),
                    password.text.toString()
                )
            }

            setOnEditorActionListener { _, actionId, _ ->
                when (actionId) {
                    EditorInfo.IME_ACTION_DONE -> {
                        mAuth.signInWithEmailAndPassword(
                            username.text.toString(),
                            password.text.toString()
                        )
                            .addOnCompleteListener { task ->
                                if (task.isSuccessful) {
                                    // Sign in success, update UI with the signed-in user's information
                                    loginViewModel._loginResult.value =
                                        LoginResult(success = LoggedInUserView(displayName = ""))
                                } else {

                                    loginViewModel._loginResult.value =
                                        LoginResult(error = R.string.login_failed)

                                }
                            }
                    }
                }
                false
            }

            login.setOnClickListener {
                loading.visibility = View.VISIBLE
                mAuth.signInWithEmailAndPassword(username.text.toString(), password.text.toString())
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            // Sign in success, update UI with the signed-in user's information
                            val user = mAuth.currentUser
                            loginViewModel._loginResult.value =
                                LoginResult(success = LoggedInUserView(displayName = ""))
                        } else {
                            // If sign in fails, display a message to the user.
                            loginViewModel._loginResult.value =
                                LoginResult(error = R.string.login_failed)

                        }
                    }
            }

            login.setOnLongClickListener() {
                loading.visibility = View.VISIBLE
                mAuth.createUserWithEmailAndPassword(
                    username.text.toString(),
                    password.text.toString()
                )
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            // Sign in success, update UI with the signed-in user's information
                            Toast.makeText(
                                this@LoginActivity, "User registered",
                                Toast.LENGTH_SHORT
                            ).show()
                        } else {
                            // If sign in fails, display a message to the user.
                            Toast.makeText(
                                this@LoginActivity, "Cannot register user",
                                Toast.LENGTH_SHORT
                            ).show()

                        }
                    }
                true
            }
        }
    }

    private fun updateUiWithUser(model: LoggedInUserView) {
        val welcome = getString(R.string.welcome)
        val displayName = model.displayName

        val intent = Intent(this, MainActivity::class.java).apply {
        }
        startActivity(intent)
        Toast.makeText(
            applicationContext,
            "$welcome $displayName",
            Toast.LENGTH_LONG
        ).show()
    }

    private fun showLoginFailed(@StringRes errorString: Int) {
        Toast.makeText(applicationContext, errorString, Toast.LENGTH_SHORT).show()
    }

}

/**
 * Extension function to simplify setting an afterTextChanged action to EditText components.
 */
fun EditText.afterTextChanged(afterTextChanged: (String) -> Unit) {
    this.addTextChangedListener(object : TextWatcher {
        override fun afterTextChanged(editable: Editable?) {
            afterTextChanged.invoke(editable.toString())
        }

        override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}

        override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {}
    })
}


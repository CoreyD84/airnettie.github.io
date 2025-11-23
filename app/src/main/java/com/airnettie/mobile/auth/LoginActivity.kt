package com.airnettie.mobile.auth

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.EditText
import android.widget.LinearLayout
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.core.content.edit
import com.airnettie.mobile.R
import com.airnettie.mobile.databinding.ActivityLoginBinding
import com.airnettie.mobile.mobilenettie.GuardianDashboard
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private val auth: FirebaseAuth by lazy { FirebaseAuth.getInstance() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d("LoginActivity", "✅ onCreate reached")

        try {
            if (FirebaseApp.getApps(this).isEmpty()) {
                FirebaseApp.initializeApp(this)
                Log.d("LoginActivity", "✅ Firebase initialized manually")
            }

            binding = ActivityLoginBinding.inflate(layoutInflater)
            setContentView(binding.root)
            Log.d("LoginActivity", "✅ Layout inflated")
        } catch (e: Exception) {
            Log.e("LoginActivity", "❌ Crash during layout or Firebase init", e)
            return
        }

        val prefs = getSharedPreferences("nettie_prefs", MODE_PRIVATE)
        val savedEmail = prefs.getString("guardian_email", "")
        val savedPassword = prefs.getString("guardian_password", "")
        binding.emailEditText.setText(savedEmail)
        binding.passwordEditText.setText(savedPassword)
        binding.rememberMeCheckbox.isChecked = prefs.contains("guardian_email")

        binding.biometricPromptText.visibility = View.GONE
        binding.loginProgress.visibility = View.GONE

        val biometricManager = BiometricManager.from(this)
        if (
            biometricManager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG) ==
            BiometricManager.BIOMETRIC_SUCCESS &&
            !savedEmail.isNullOrEmpty() &&
            !savedPassword.isNullOrEmpty()
        ) {
            Log.d("LoginActivity", "✅ Showing biometric prompt")
            binding.biometricPromptText.visibility = View.VISIBLE
            showBiometricPrompt(savedEmail, savedPassword)
        }

        binding.loginButton.setOnClickListener {
            Log.d("LoginActivity", "🔐 Login button clicked")
            val email = binding.emailEditText.text.toString().trim()
            val password = binding.passwordEditText.text.toString().trim()

            if (email.isEmpty() || password.isEmpty()) {
                binding.statusText.text = getString(R.string.error_enter_credentials)
                return@setOnClickListener
            }

            handleRememberMe(email, password)
            binding.loginProgress.visibility = View.VISIBLE

            auth.signInWithEmailAndPassword(email, password)
                .addOnSuccessListener {
                    binding.loginProgress.visibility = View.GONE
                    Log.d("LoginActivity", "✅ Login successful")
                    saveGuardianIdLocally()
                    launchDashboard()
                }
                .addOnFailureListener {
                    binding.loginProgress.visibility = View.GONE
                    Log.e("LoginActivity", "❌ Login failed: ${it.localizedMessage}")
                    binding.statusText.text = getString(R.string.error_login_failed, it.localizedMessage)
                }
        }

        binding.createAccountButton.setOnClickListener {
            Log.d("LoginActivity", "🆕 Create account button clicked")
            val email = binding.emailEditText.text.toString().trim()
            val password = binding.passwordEditText.text.toString().trim()

            if (email.isEmpty() || password.isEmpty()) {
                binding.statusText.text = getString(R.string.error_enter_credentials)
                return@setOnClickListener
            }

            handleRememberMe(email, password)
            binding.loginProgress.visibility = View.VISIBLE

            auth.createUserWithEmailAndPassword(email, password)
                .addOnSuccessListener {
                    binding.loginProgress.visibility = View.GONE
                    Log.d("LoginActivity", "✅ Account created")
                    promptForGuardianName()
                }
                .addOnFailureListener {
                    binding.loginProgress.visibility = View.GONE
                    Log.e("LoginActivity", "❌ Signup failed: ${it.localizedMessage}")
                    binding.statusText.text = getString(R.string.error_signup_failed, it.localizedMessage)
                }
        }
    }

    override fun onStart() {
        super.onStart()
        Log.d("LoginActivity", "🚀 onStart reached")
        auth.currentUser?.let {
            Log.d("LoginActivity", "✅ User already logged in")
            saveGuardianIdLocally()
            launchDashboard()
        }
    }

    private fun handleRememberMe(email: String, password: String) {
        getSharedPreferences("nettie_prefs", MODE_PRIVATE).edit {
            if (binding.rememberMeCheckbox.isChecked) {
                putString("guardian_email", email)
                putString("guardian_password", password)
            } else {
                remove("guardian_email")
                remove("guardian_password")
            }
        }
    }

    private fun saveGuardianIdLocally() {
        val guardianId = auth.currentUser?.uid ?: return
        getSharedPreferences("nettie_prefs", MODE_PRIVATE).edit {
            putString("guardian_id", guardianId)
        }
    }

    private fun promptForGuardianName() {
        val input = EditText(this).apply {
            hint = getString(R.string.hint_enter_name)
            setPadding(32, 32, 32, 32)
        }

        val container = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(48, 24, 48, 0)
            addView(input)
        }

        AlertDialog.Builder(this)
            .setTitle(getString(R.string.title_welcome))
            .setMessage(getString(R.string.message_ask_name))
            .setView(container)
            .setCancelable(false)
            .setPositiveButton(getString(R.string.button_confirm)) { _, _ ->
                val name = input.text.toString().trim()
                if (name.isEmpty()) {
                    input.error = getString(R.string.error_enter_name)
                    return@setPositiveButton
                }

                val guardianId = auth.currentUser?.uid
                if (guardianId.isNullOrEmpty()) {
                    binding.statusText.text = getString(R.string.error_missing_identity)
                    return@setPositiveButton
                }

                getSharedPreferences("nettie_prefs", MODE_PRIVATE).edit {
                    putString("guardian_name", name)
                    putString("guardian_id", guardianId)
                }

                FirebaseDatabase.getInstance()
                    .getReference("guardian_profiles/$guardianId")
                    .setValue(mapOf("name" to name))

                AlertDialog.Builder(this)
                    .setTitle(getString(R.string.title_nice_to_meet, name))
                    .setMessage(getString(R.string.message_ready_to_protect))
                    .setPositiveButton(getString(R.string.button_lets_go)) { _, _ ->
                        launchDashboard()
                    }
                    .show()
            }
            .show()
    }

    private fun showBiometricPrompt(email: String, password: String) {
        binding.loginProgress.visibility = View.VISIBLE

        val executor = ContextCompat.getMainExecutor(this)
        val biometricPrompt = BiometricPrompt(this, executor,
            object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                    super.onAuthenticationSucceeded(result)
                    Log.d("LoginActivity", "✅ Biometric auth succeeded")
                    auth.signInWithEmailAndPassword(email, password)
                        .addOnSuccessListener {
                            binding.loginProgress.visibility = View.GONE
                            saveGuardianIdLocally()
                            launchDashboard()
                        }
                        .addOnFailureListener {
                            binding.loginProgress.visibility = View.GONE
                            binding.statusText.text = getString(R.string.error_biometric_failed, it.localizedMessage)
                        }
                }

                override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                    super.onAuthenticationError(errorCode, errString)
                    binding.loginProgress.visibility = View.GONE
                    Log.e("LoginActivity", "❌ Biometric error: $errString")
                    binding.statusText.text = getString(R.string.error_biometric_error, errString)
                }

                override fun onAuthenticationFailed() {
                    super.onAuthenticationFailed()
                    binding.loginProgress.visibility = View.GONE
                    Log.w("LoginActivity", "⚠️ Biometric auth failed")
                    binding.statusText.text = getString(R.string.error_biometric_auth_failed)
                }
            })

        val promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle(getString(R.string.biometric_title))
            .setSubtitle(getString(R.string.biometric_subtitle))
            .setNegativeButtonText(getString(R.string.biometric_cancel))
            .build()

        biometricPrompt.authenticate(promptInfo)
    }

    private fun launchDashboard() {
        Log.d("LoginActivity", "🚀 Launching GuardianDashboard")
        startActivity(Intent(this, GuardianDashboard::class.java))
        finish()
    }
}
package com.guptamansi.ghrepo

import android.os.Bundle
import android.util.Log
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GithubAuthProvider
import org.json.JSONObject
import okhttp3.*
import okhttp3.Call
import okhttp3.FormBody
import okhttp3.Callback
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import java.io.IOException




/*
class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }
}

 */
class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        intent?.data?.let { uri ->
            if (uri.toString().startsWith("https://gh-repo-f1896.firebaseapp.com/__/auth/handler")) {
                val githubAuthCode = uri.getQueryParameter("code")
                githubAuthCode?.let { exchangeGitHubAuthCodeForToken(it) }
            }
        }
    }

    private fun exchangeGitHubAuthCodeForToken(authCode: String) {
        val githubClientId = "Ov23liDkIrIUCIc3FPmR"
        val githubClientSecret = "b4d0557d568e68e2a74f00bae4b07f84313afd88T"

        val requestBody = FormBody.Builder()
            .add("client_id", githubClientId)
            .add("client_secret", githubClientSecret)
            .add("code", authCode)
            .add("redirect_uri", "https://gh-repo-f1896.firebaseapp.com/__/auth/handler")
            .add("grant_type", "authorization_code")
            .build()

        val request = Request.Builder()
            .url("https://github.com/login/oauth/access_token")
            .post(requestBody)
            .addHeader("Accept", "application/json")
            .build()

        OkHttpClient().newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.e("GitHubLogin", "Token exchange failed: ${e.message}")
            }

            override fun onResponse(call: Call, response: Response) {
                response.body?.string()?.let { responseBody ->
                    val json = JSONObject(responseBody)
                    val githubToken = json.getString("access_token")
                    signInWithGitHub(githubToken)
                }
            }
        })
    }

    private fun signInWithGitHub(token: String) {
        val credential = GithubAuthProvider.getCredential(token)
        FirebaseAuth.getInstance().signInWithCredential(credential)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Log.d("GitHubLogin", "GitHub sign-in successful")
                } else {
                    Log.e("GitHubLogin", "GitHub sign-in failed", task.exception)
                }
            }
    }
}

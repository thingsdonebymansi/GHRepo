package com.guptamansi.ghrepo

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.content.Intent
import android.net.Uri
import android.util.Log
import android.widget.Button
import androidx.activity.result.contract.ActivityResultContracts
import androidx.credentials.provider.BeginGetCredentialRequest
import com.google.android.gms.auth.api.identity.BeginSignInRequest
import com.google.android.gms.auth.api.identity.Identity
import com.google.android.gms.auth.api.identity.SignInClient
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import org.json.JSONObject
import com.android.volley.Request
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException

/*
// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [SignInFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class SignInFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    private lateinit var auth: FirebaseAuth
    private lateinit var oneTapClient: SignInClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_sign_in, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        auth = FirebaseAuth.getInstance()
        oneTapClient = Identity.getSignInClient(requireContext())

        view.findViewById<View>(R.id.btnSignIn).setOnClickListener{
            signInWithGoogle()
        }
    }

    private fun signInWithGoogle(){
        val signInRequest = BeginSignInRequest.builder()
            .setGoogleIdTokenRequestOptions(
                BeginSignInRequest.GoogleIdTokenRequestOptions.builder()
                    .setSupported(true)
                    .setServerClientId(getString(R.string.default_web_client_id))
                    .setFilterByAuthorizedAccounts(false)
                    .build()
            )
            .build()

        oneTapClient.beginSignIn(signInRequest)
            .addOnSuccessListener { result ->
                startIntentSenderForResult(
                    result.pendingIntent.intentSender, 1001, null, 0, 0, 0, null
                )
            }
            .addOnFailureListener{ e ->
                Log.e("SignIn", "Google Sign-In failed", e)

            }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 1001){
            val credential = oneTapClient.getSignInCredentialFromIntent(data)
            val idToken = credential.googleIdToken
            if(idToken != null){
                firebaseAuthWithGoogle(idToken)
            }
        }
    }

    private fun firebaseAuthWithGoogle(idToken: String) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        auth.signInWithCredential(credential)
            .addOnCompleteListener{task ->
                if (task.isSuccessful) {
                    Log.d("SignIn", "Google Sign-In successful")
                    val user = auth.currentUser
                    user?.getIdToken(true)?.addOnSuccessListener { result ->
                        val firebaseToken = result.token
                        sendTokenToCloudFunction(firebaseToken!!)
                    }
                } else {
                    Log.e("SignIn", "Google Sign-In failed", task.exception)
                }
            }
    }

    private fun sendTokenToCloudFunction(firebaseToken: String) {
        val url = ""
        val requestBody = JSONObject().apply {
            put("Token", firebaseToken)
            put("githubAuthCode", "YourGitHubAuthCode")
        }

        val request = JsonObjectRequest(
            Request.Method.POST, url, requestBody,
            { response ->
                Log.d("CloudFunction", "Success: $response")

            },
            { error ->
                Log.e("CloudFunction", "Error: ${error.message}")
            }
        )
        Volley.newRequestQueue(requireContext()).add(request)

    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment SignInFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            SignInFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}

 */

class SignInFragment : Fragment() {
    private lateinit var auth: FirebaseAuth
    private val googleSignInLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
            try {
                val account = task.getResult(ApiException::class.java)
                val idToken = account?.idToken
                if (idToken != null) {
                    firebaseAuthWithGoogle(idToken)
                }
            } catch (e: ApiException) {
                Log.e("GoogleSignIn", "Sign-in failed: ${e.message}")
            }
        }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_sign_in, container, false)
        auth = FirebaseAuth.getInstance()

        view.findViewById<Button>(R.id.btnSignIn).setOnClickListener {
            signInWithGoogle()
        }
        return view
    }

    private fun signInWithGoogle() {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id)) // Web Client ID from Firebase
            .requestEmail()
            .build()

        val googleSignInClient = GoogleSignIn.getClient(requireActivity(), gso)
        googleSignInLauncher.launch(googleSignInClient.signInIntent)
    }

    private fun firebaseAuthWithGoogle(idToken: String) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        auth.signInWithCredential(credential)
            .addOnCompleteListener(requireActivity()) { task ->
                if (task.isSuccessful) {
                    val user = auth.currentUser
                    Log.d("GoogleSignIn", "Google sign-in successful: ${user?.email}")
                    startGitHubLogin() // Proceed to GitHub login after Google login
                } else {
                    Log.e("GoogleSignIn", "Google sign-in failed", task.exception)
                }
            }
    }

    private fun startGitHubLogin() {
        val githubClientId = "Ov23liDkIrIUCIc3FPmR"
        val githubRedirectUri = "https://gh-repo-f1896.firebaseapp.com/__/auth/handler"

        val githubAuthUrl = "https://github.com/login/oauth/authorize" +
                "?client_id=$githubClientId" +
                "&redirect_uri=$githubRedirectUri" +
                "&scope=read:user,user:email"

        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(githubAuthUrl))
        startActivity(intent)
    }

}

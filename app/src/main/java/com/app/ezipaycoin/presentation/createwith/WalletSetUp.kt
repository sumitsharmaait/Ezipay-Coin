package com.app.ezipaycoin.presentation.createwith

import android.app.Activity
import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.GetCredentialResponse
import androidx.navigation.NavController
import com.app.ezipaycoin.R
import com.app.ezipaycoin.R.drawable.ezipay_wallet
import com.app.ezipaycoin.navigation.Screen
import com.app.ezipaycoin.ui.composables.AppGreyButton
import com.app.ezipaycoin.ui.composables.Dialogue
import com.app.ezipaycoin.ui.composables.GoldGradientButton
import com.app.ezipaycoin.ui.composables.GradientText
import com.app.ezipaycoin.ui.theme.GoldAccentColor
import com.app.ezipaycoin.ui.theme.GoldTextColor
import com.app.ezipaycoin.ui.theme.OnboardingGold1
import com.app.ezipaycoin.utils.ResponseState
import com.facebook.AccessToken
import com.facebook.CallbackManager
import com.facebook.FacebookCallback
import com.facebook.FacebookException
import com.facebook.login.LoginManager
import com.facebook.login.LoginResult
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.firebase.auth.FacebookAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.launch


@Composable
fun WalletSetUp(
    navController: NavController,
    viewModel: WalletSetupVM
) {
    val state by viewModel.uiState.collectAsState()
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        viewModel.eventFlow.collect { event ->
            when (event) {
                is WalletSetupVMEvent.MoveToSuccess -> {
                    navController.navigate(Screen.Auth.CreatePassword)
                }
            }
        }
    }


    val auth = FirebaseAuth.getInstance()
    val credentialManager = remember { CredentialManager.create(context) }
    val callbackManager = remember { CallbackManager.Factory.create() }


    DisposableEffect(callbackManager) {
        val loginManager = LoginManager.getInstance()
        loginManager.registerCallback(callbackManager, object : FacebookCallback<LoginResult> {
            override fun onSuccess(result: LoginResult) {
                Log.d("FacebookSignIn", "Facebook token: ${result.accessToken.token}")
                handleFacebookAccessToken(
                    result.accessToken,
                    auth,
                    onSuccess = { name, email, pic ->
                        Log.d("FacebookSignIn", "Success: $name, $email")
                        viewModel.onEvent(WalletSetupEvent.SocialDetailsFetched(name, email, pic))
                    })
            }

            override fun onCancel() {
                Log.d("FacebookSignIn", "Login canceled")
            }

            override fun onError(error: FacebookException) {
                Log.e("FacebookSignIn", "Login error", error)
            }
        })
        onDispose {
            loginManager.unregisterCallback(callbackManager)
        }
    }

    fun handleCredentialResponse(response: GetCredentialResponse) {
        val credential = response.credential
        if (credential is CustomCredential &&
            credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
        ) {
            val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
            val idToken = googleIdTokenCredential.idToken
            val firebaseCredential = GoogleAuthProvider.getCredential(idToken, null)
            auth.signInWithCredential(firebaseCredential)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        auth.currentUser?.let {
                            if (!it.displayName.isNullOrEmpty() && !it.email.isNullOrEmpty()) {
                                viewModel.onEvent(
                                    WalletSetupEvent.SocialDetailsFetched(
                                        it.displayName!!,
                                        it.email!!,
                                        it.photoUrl.toString()
                                    )
                                )
                                Log.d("GoogleSignIn", "Success: ${auth.currentUser?.displayName}")
                            }
                        }
                    } else {
                        Log.e("GoogleSignIn", "Firebase Auth failed", task.exception)
                    }
                }
        } else {
            Log.e("GoogleSignIn", "Unexpected credential type: ${credential.type}")
        }
    }

    fun launchGoogleSignIn() {
        scope.launch {

            viewModel.onEvent(WalletSetupEvent.SocialSignInLoading)

            val googleIdOption = GetGoogleIdOption.Builder()
                .setFilterByAuthorizedAccounts(false)
                .setServerClientId(context.getString(R.string.default_web_client_id))
                .build()

            val request = GetCredentialRequest.Builder()
                .addCredentialOption(googleIdOption)
                .build()

            try {
                val result = credentialManager.getCredential(context, request)
                handleCredentialResponse(result)
            } catch (e: Exception) {
                Log.e("GoogleSignIn", "Google sign-in failed", e)
            } finally {
                viewModel.onEvent(WalletSetupEvent.SocialSignInLoading)
            }
        }

    }


    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(color = Color.Black)
    ) { // Main Box for background and orb positioning
        // Decorative Orbs (positioned absolutely)

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(80.dp)) // Adjust for status bar and top spacing

            // Wallet Image
            Image(
                painter = painterResource(id = ezipay_wallet), // Replace with your actual drawable
                contentDescription = "Ezipay Wallet Graphic",
                modifier = Modifier
                    .fillMaxWidth(0.9f) // Adjust size as needed
                    .aspectRatio(1.0f), // Adjust based on your image's aspect ratio
                contentScale = ContentScale.Fit
            )

            Spacer(modifier = Modifier.height(40.dp))

            // "Ezipay Wallet" Text

            Row(
                modifier = Modifier,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "Ezipay",
                    style = MaterialTheme.typography.displayMedium.copy(fontWeight = FontWeight(400)),
                    color = Color.White,
                )
                GradientText(
                    text = "Wallet",
                    modifier = Modifier,
                    style = MaterialTheme.typography.displayMedium,
                    align = TextAlign.Center
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Subtitle Text
            Text(
                text = "  The best crypto wallet app\nof this century  ",
                style = MaterialTheme.typography.titleLarge,
                color = Color.White,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(40.dp))

            // Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {

                AppGreyButton(
                    labelColor = Color.White,
                    label = "Import Seed",
                    onClick = { navController.navigate(Screen.Auth.ImportFromSeed) },
                    modifier = Modifier
                        .weight(1f)
                )

                GoldGradientButton(
                    label = "Create New",
                    onClick = { navController.navigate(Screen.Auth.CreatePassword) },
                    modifier = Modifier
                        .weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(30.dp))

            // "Or Login with" Divider
            OrLoginWithDivider()

            Spacer(modifier = Modifier.height(30.dp))
            // Social Login Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly // Or Arrangement.spacedBy(16.dp)
            ) {
                SocialLoginButton(
                    iconRes = R.drawable.sign_in_facebook,
                    contentDescription = "Login with Facebook",
                    onClick = {
                        LoginManager.getInstance().logInWithReadPermissions(
                            context as Activity,
                            listOf("email", "public_profile")
                        )
                    }
                )
                SocialLoginButton(
                    iconRes = R.drawable.sign_in_google,
                    contentDescription = "Login with Google",
                    onClick = {
                        launchGoogleSignIn()
                    }
                )
            }

            Spacer(modifier = Modifier.weight(1f)) // Push bottom content down

            // "Don't have an account?" Text
            ClickableRegisterNowText()

            Spacer(modifier = Modifier.height(40.dp)) // Bottom padding
        }

        if (state.responseState is ResponseState.Loading || state.loadingSocialSignIn) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.4f)), // optional: dim background
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = OnboardingGold1)
            }
        } else if (state.responseState is ResponseState.Error) {
            Dialogue(
                isError = true,
                msg = (state.responseState as ResponseState.Error).message
            ) {
                viewModel.onEvent(WalletSetupEvent.DismissDialog)
            }
        }
    }


}

@Composable
fun OrLoginWithDivider() {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(horizontal = 16.dp)
    ) {
        HorizontalDivider(modifier = Modifier.weight(1f), thickness = 1.dp, color = GoldAccentColor)
        Text(
            "Or Create with",
            style = MaterialTheme.typography.bodySmall.copy(color = Color.White),
            modifier = Modifier.padding(horizontal = 16.dp)
        )
        HorizontalDivider(modifier = Modifier.weight(1f), thickness = 1.dp, color = GoldAccentColor)
    }
}

@Composable
fun SocialLoginButton(iconRes: Int, contentDescription: String, onClick: () -> Unit = {}) {
    Button(
        onClick = onClick,
        modifier = Modifier
            .size(width = 80.dp, height = 45.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = Color.White,
            contentColor = Color.Unspecified // Let icon tint itself
        ),// Adjust size as needed
        contentPadding = PaddingValues(0.dp) // Remove default padding to center icon properly
    ) {
        Image(
            painter = painterResource(id = iconRes),
            contentDescription = contentDescription,
            modifier = Modifier.fillMaxSize()// Adjust icon size
        )
    }
}

@Composable
fun ClickableRegisterNowText() {
    Text(
        text = buildAnnotatedString {
            withStyle(style = SpanStyle(color = Color.White, fontWeight = FontWeight(500))) {
                append("Don't have an account? ")
            }
            withStyle(style = SpanStyle(color = GoldTextColor, fontWeight = FontWeight(700))) {
                append("Register Now")
            }
        },
        style = MaterialTheme.typography.bodySmall,
        modifier = Modifier.clickable { /* Handle Register Now click */ }
    )
}


private fun handleFacebookAccessToken(
    token: AccessToken,
    auth: FirebaseAuth,
    onSuccess: (name: String, email: String, pic: String) -> Unit
) {
    val credential = FacebookAuthProvider.getCredential(token.token)
    auth.signInWithCredential(credential)
        .addOnCompleteListener { task ->
            if (task.isSuccessful) {
                auth.currentUser?.let {
                    if (!it.displayName.isNullOrEmpty() && !it.email.isNullOrEmpty()) {
                        onSuccess(it.displayName!!, it.email!!, it.photoUrl.toString())
                        Log.d("FacebookSignIn", "Success: ${auth.currentUser?.displayName}")
                    }
                }
            } else {
                Log.e("FacebookSignIn", "Firebase Auth failed", task.exception)
            }
        }
}


//@Preview
//@Composable
//fun PayScreenUtilitiesPreview() {
//    EzipayCoinTheme {
//        WalletSetUp(navController = rememberNavController())
//    }
//}

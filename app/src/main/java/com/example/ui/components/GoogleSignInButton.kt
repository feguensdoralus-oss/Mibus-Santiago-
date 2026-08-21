package com.example.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialException
import com.example.data.AppConstants
import com.example.firebase.FirebaseManager
import com.example.firebase.LinkResult
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import kotlinx.coroutines.launch

@Composable
fun GoogleSignInButton(
    firebaseManager: FirebaseManager,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val credentialManager = remember { CredentialManager.create(context) }

    var showCollisionDialog by remember { mutableStateOf(false) }
    var pendingIdToken by remember { mutableStateOf<String?>(null) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var isSuccess by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }

    val userEmail = firebaseManager.getCurrentUserEmail()
    val isSignedIn = firebaseManager.isUserSignedIn() || isSuccess

    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        shape = RoundedCornerShape(24.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Text(
                text = "SINCRONIZACIÓN EN LA NUBE",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Black,
                letterSpacing = 1.4.sp,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = if (isSignedIn) Icons.Default.CheckCircle else Icons.Default.AccountCircle,
                    contentDescription = null,
                    tint = if (isSignedIn) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.width(10.dp))
                Column {
                    Text(
                        text = if (isSignedIn) "Cuenta Vinculada" else "Google Cloud Sync",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Black,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = if (isSignedIn) (userEmail ?: "Conectado con Google") else "Guarda tus paraderos y recargas de forma segura",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(Modifier.height(14.dp))

            if (!isSignedIn) {
                Button(
                    onClick = {
                        isLoading = true
                        errorMessage = null
                        scope.launch {
                            try {
                                val googleIdOption = GetGoogleIdOption.Builder()
                                    .setFilterByAuthorizedAccounts(false)
                                    .setServerClientId(AppConstants.GOOGLE_WEB_CLIENT_ID)
                                    .setAutoSelectEnabled(false)
                                    .build()

                                val request = GetCredentialRequest.Builder()
                                    .addCredentialOption(googleIdOption)
                                    .build()

                                val result = credentialManager.getCredential(context = context, request = request)
                                val credential = result.credential

                                if (credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                                    val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
                                    val idToken = googleIdTokenCredential.idToken
                                    when (val resultLink = firebaseManager.linkWithGoogle(idToken)) {
                                        is LinkResult.Success -> {
                                            isSuccess = true
                                            errorMessage = null
                                        }
                                        is LinkResult.AccountAlreadyExists -> {
                                            pendingIdToken = idToken
                                            showCollisionDialog = true
                                        }
                                        is LinkResult.Error -> {
                                            errorMessage = resultLink.message
                                        }
                                    }
                                }
                            } catch (e: GetCredentialException) {
                                e.printStackTrace()
                                errorMessage = "Autenticación cancelada o no disponible en este dispositivo"
                            } catch (e: Exception) {
                                e.printStackTrace()
                                errorMessage = "Error: ${e.localizedMessage ?: "Fallo de conexión"}"
                            } finally {
                                isLoading = false
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .testTag("google_sign_in_button"),
                    enabled = !isLoading,
                    shape = RoundedCornerShape(20.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(18.dp),
                            color = MaterialTheme.colorScheme.onPrimary,
                            strokeWidth = 2.dp
                        )
                        Spacer(Modifier.width(8.dp))
                    }
                    Text("Vincular con Google", fontWeight = FontWeight.Black)
                }
            } else {
                OutlinedButton(
                    onClick = {
                        firebaseManager.signOut()
                        isSuccess = false
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    shape = RoundedCornerShape(20.dp)
                ) {
                    Text("Cerrar Sesión", fontWeight = FontWeight.Bold)
                }
            }

            errorMessage?.let { msg ->
                Spacer(Modifier.height(8.dp))
                Text(
                    text = msg,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }

    if (showCollisionDialog && pendingIdToken != null) {
        AlertDialog(
            onDismissRequest = { showCollisionDialog = false },
            shape = RoundedCornerShape(28.dp),
            title = { Text("Cuenta de Google Ya Registrada", fontWeight = FontWeight.Black) },
            text = {
                Text("Esta cuenta de Google ya está vinculada a otro perfil en MiBus Santiago. ¿Deseas iniciar sesión directamente con tu cuenta registrada para sincronizar tus favoritos?")
            },
            confirmButton = {
                Button(
                    onClick = {
                        scope.launch {
                            showCollisionDialog = false
                            val switched = firebaseManager.switchToExistingGoogleAccount(pendingIdToken!!)
                            if (switched) {
                                isSuccess = true
                                errorMessage = null
                            } else {
                                errorMessage = "No se pudo iniciar sesión con la cuenta existente."
                            }
                        }
                    },
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Text("Iniciar Sesión", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showCollisionDialog = false }) {
                    Text("Cancelar", fontWeight = FontWeight.Bold)
                }
            }
        )
    }
}


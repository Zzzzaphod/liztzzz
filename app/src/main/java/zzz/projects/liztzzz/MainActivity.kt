package zzz.projects.liztzzz

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.runtime.*
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.auth.FirebaseAuth
import zzz.projects.liztzzz.data.LiztViewModel
import zzz.projects.liztzzz.ui.screens.LiztDetailScreen
import zzz.projects.liztzzz.ui.screens.LiztGridScreen
import zzz.projects.liztzzz.ui.screens.LoginScreen
import zzz.projects.liztzzz.ui.theme.LiztzzTheme

class MainActivity : ComponentActivity() {

    private val viewModel: LiztViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val auth = Firebase.auth

        enableEdgeToEdge()
        setContent {
            LiztzzTheme {
                MainScreen(auth, viewModel)
            }
        }
    }
}

@Composable
fun MainScreen(auth: FirebaseAuth, viewModel: LiztViewModel) {
    var user by remember { mutableStateOf(auth.currentUser) }
    var selectedLiztUid by remember { mutableStateOf<String?>(null) }

    if (user == null) {
        LoginScreen(auth = auth, onLoginSuccess = {
            user = auth.currentUser
        })
    } else {
        if (selectedLiztUid == null) {
            LiztGridScreen(
                viewModel = viewModel,
                onLiztClick = { selectedLiztUid = it.uid },
                onSignOut = {
                    auth.signOut()
                    user = null
                }
            )
        } else {
            val lizts by viewModel.lizts.collectAsState()
            val selectedLizt = lizts.find { it.uid == selectedLiztUid }
            if (selectedLizt != null) {
                LiztDetailScreen(
                    lizt = selectedLizt,
                    onBack = { selectedLiztUid = null },
                    onToggleChecked = { index, isChecked ->
                        viewModel.updateLiztItemChecked(selectedLizt.uid, index, isChecked)
                    }
                )
            } else {
                selectedLiztUid = null
            }
        }
    }
}

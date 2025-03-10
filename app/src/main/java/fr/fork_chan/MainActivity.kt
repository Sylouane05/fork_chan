package fr.fork_chan

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import fr.fork_chan.ui.theme.Fork_chanTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            Fork_chanTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    FeedContent() // Call the feed screen content
                }
            }
        }
    }
}

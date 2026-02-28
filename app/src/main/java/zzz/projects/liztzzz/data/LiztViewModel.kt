package zzz.projects.liztzzz.data

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class LiztViewModel : ViewModel() {

    private val _lizts = MutableStateFlow<List<Lizt>>(emptyList())
    val lizts = _lizts.asStateFlow()

    private val database: FirebaseDatabase = FirebaseDatabase.getInstance()
    
    // Die Referenz zeigt nun direkt auf den Root-Knoten "liztz"
    private val liztRef: DatabaseReference = database.getReference("liztz")

    init {
        loadLizts()
    }

    private fun loadLizts() {
        viewModelScope.launch {
            // Wir laden die Daten direkt vom Root-Knoten "liztz"
            liztRef.addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val lizts = snapshot.children
                        .filter { it.key?.toIntOrNull() != null } // Nur numerische Positions-Schlüssel
                        .sortedBy { it.key?.toInt() } // Sortierung nach Position
                        .mapNotNull {
                            val lizt = it.getValue(Lizt::class.java)
                            lizt?.uid = it.key ?: ""
                            lizt
                        }
                    _lizts.value = lizts
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.w("LiztViewModel", "loadLizts:onCancelled", error.toException())
                }
            })
        }
    }

    fun addLizt(liztName: String, hasSuggests: Boolean) {
        val nextPosition = _lizts.value.size
        val newLizt = Lizt(
            liztName = liztName,
            hasSuggests = hasSuggests
        )

        // Speichern direkt unter "liztz/{nächstePosition}"
        liztRef.child(nextPosition.toString()).setValue(newLizt)
    }

    fun updateOrder(newOrder: List<Lizt>) {
        val updates = mutableMapOf<String, Any?>()
        
        newOrder.forEachIndexed { index, lizt ->
            // Die UID wird beim Speichern entfernt, da sie dem Schlüssel entspricht
            val liztData = lizt.copy(uid = "")
            updates[index.toString()] = liztData
        }

        // Überschreibt den Knoten "liztz" mit der neuen flachen Struktur
        liztRef.setValue(updates).addOnFailureListener {
            Log.e("LiztViewModel", "updateOrder failed", it)
        }
    }
}

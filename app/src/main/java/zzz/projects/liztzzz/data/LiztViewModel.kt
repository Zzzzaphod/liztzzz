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
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()

    private lateinit var liztRef: DatabaseReference

    init {
        auth.currentUser?.uid?.let {
            liztRef = database.getReference("liztz").child(it).child("liztz")
            loadLizts()
        }
    }

    private fun loadLizts() {
        viewModelScope.launch {
            liztRef.orderByChild("position").addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val lizts = snapshot.children.mapNotNull {
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
        val newPosition = _lizts.value.size
        val newLizt = Lizt(
            liztName = liztName,
            hasSuggests = hasSuggests,
            position = newPosition
        )

        liztRef.push().setValue(newLizt)
    }

    fun updateOrder(lizts: List<Lizt>) {
        val updates = hashMapOf<String, Any>()
        lizts.forEachIndexed { index, lizt ->
            updates["/${lizt.uid}/position"] = index
        }
        liztRef.updateChildren(updates)
    }
}
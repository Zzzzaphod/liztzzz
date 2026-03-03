package zzz.projects.liztzzz.data

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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
    private val liztRef: DatabaseReference = database.getReference("liztz")
    private val crashRef: DatabaseReference = database.getReference("crashes")

    init {
        loadLizts()
    }

    private fun loadLizts() {
        viewModelScope.launch {
            liztRef.addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val lizts = snapshot.children
                        .filter { it.key?.toIntOrNull() != null }
                        .sortedBy { it.key?.toInt() }
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

    fun uploadCrashReport(report: String) {
        val timestamp = System.currentTimeMillis()
        crashRef.push().setValue(mapOf(
            "timestamp" to timestamp,
            "report" to report
        )).addOnFailureListener {
            Log.e("LiztViewModel", "Failed to upload crash report", it)
        }
    }

    fun addLizt(liztName: String, hasSuggests: Boolean) {
        val nextPosition = _lizts.value.size
        val newLizt = Lizt(
            liztName = liztName,
            hasSuggests = hasSuggests
        )
        liztRef.child(nextPosition.toString()).setValue(newLizt)
    }

    fun updateOrder(newOrder: List<Lizt>) {
        val updates = mutableMapOf<String, Any?>()
        newOrder.forEachIndexed { index, lizt ->
            val liztData = lizt.copy(uid = "")
            updates[index.toString()] = liztData
        }
        liztRef.setValue(updates).addOnFailureListener {
            Log.e("LiztViewModel", "updateOrder failed", it)
        }
    }

    fun updateLiztItemChecked(liztUid: String, itemIndex: Int, isChecked: Boolean) {
        val lizt = _lizts.value.find { it.uid == liztUid } ?: return
        val updatedUnchecked = lizt.liztUnchecked.toMutableList()
        if (itemIndex in updatedUnchecked.indices) {
            updatedUnchecked[itemIndex] = updatedUnchecked[itemIndex].copy(isChecked = isChecked)
            liztRef.child(liztUid).child("liztUnchecked").setValue(updatedUnchecked)
        }
    }

    fun addLiztItem(liztUid: String, itemName: String) {
        val lizt = _lizts.value.find { it.uid == liztUid } ?: return
        val trimmedName = itemName.trim()
        val updatedUnchecked = lizt.liztUnchecked.toMutableList()
        if (updatedUnchecked.none { it.itemName.trim().equals(trimmedName, ignoreCase = true) }) {
            updatedUnchecked.add(LiztItem(itemName = trimmedName))
            liztRef.child(liztUid).child("liztUnchecked").setValue(updatedUnchecked)
        }
    }

    fun deleteCheckedItems(liztUid: String) {
        val lizt = _lizts.value.find { it.uid == liztUid } ?: return
        val remainingItems = lizt.liztUnchecked.filter { !it.isChecked }
        liztRef.child(liztUid).child("liztUnchecked").setValue(remainingItems)
    }

    fun toggleHasSuggests(liztUid: String) {
        val lizt = _lizts.value.find { it.uid == liztUid } ?: return
        liztRef.child(liztUid).child("hasSuggests").setValue(!lizt.hasSuggests)
    }

    fun copySuggestedToUnchecked(liztUid: String, item: LiztItem) {
        val lizt = _lizts.value.find { it.uid == liztUid } ?: return
        val trimmedName = item.itemName.trim()
        val updatedUnchecked = lizt.liztUnchecked.toMutableList()
        
        if (updatedUnchecked.none { it.itemName.trim().equals(trimmedName, ignoreCase = true) }) {
            updatedUnchecked.add(item.copy(itemName = trimmedName, isChecked = false))
            liztRef.child(liztUid).child("liztUnchecked").setValue(updatedUnchecked)
        }
    }

    fun deleteSuggestedItem(liztUid: String, item: LiztItem) {
        val lizt = _lizts.value.find { it.uid == liztUid } ?: return
        val updatedSuggested = lizt.liztSuggested.filter { it.itemName.trim() != item.itemName.trim() }
        liztRef.child(liztUid).child("liztSuggested").setValue(updatedSuggested)
    }

    fun copyUncheckedToSuggested(liztUid: String, item: LiztItem) {
        val lizt = _lizts.value.find { it.uid == liztUid } ?: return
        val trimmedName = item.itemName.trim()
        val updatedSuggested = lizt.liztSuggested.toMutableList()
        
        if (updatedSuggested.none { it.itemName.trim().equals(trimmedName, ignoreCase = true) }) {
            updatedSuggested.add(item.copy(itemName = trimmedName, isChecked = false, isSuggest = true))
            val sortedSuggested = updatedSuggested.sortedBy { it.itemName }
            liztRef.child(liztUid).child("liztSuggested").setValue(sortedSuggested)
        }
    }

    fun renameLizt(liztUid: String, newName: String) {
        liztRef.child(liztUid).child("liztName").setValue(newName)
    }
}

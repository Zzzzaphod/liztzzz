package zzz.projects.liztzzz.data

import com.google.firebase.database.Exclude
import com.google.firebase.database.PropertyName

data class Lizt(
    @get:Exclude
    var uid: String = "", // To hold the unique key from Firebase
    var position: Int = 0,
    var hasSuggests: Boolean = false,
    @get:PropertyName("isDeletable")
    val isDeletable: Boolean = false,
    var liztName: String = "",
    val liztSuggested: List<LiztItem> = emptyList(),
    val liztUnchecked: List<LiztItem> = emptyList(),
    val liztChecked: List<LiztItem> = emptyList(),
    val showUnchecked: Boolean = true
)

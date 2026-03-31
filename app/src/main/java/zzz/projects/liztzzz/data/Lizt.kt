package zzz.projects.liztzzz.data

import com.google.firebase.database.Exclude
import com.google.firebase.database.PropertyName

data class Lizt(
    @get:Exclude
    var uid: String = "", // Hält den Schlüssel (z.B. die Positionsnummer "0", "1"...)
    var hasSuggests: Boolean = false,
    @get:PropertyName("isDeletable")
    var isDeletable: Boolean = false,
    var liztName: String = "",
    var liztSuggested: List<LiztItem> = emptyList(),
    var liztUnchecked: List<LiztItem> = emptyList(),
    var liztChecked: List<LiztItem> = emptyList(),
    var showUnchecked: Boolean = true,
    var color: String = "#FFFFFFFF" // Default to white
)

package zzz.projects.liztzzz.data
import com.google.firebase.database.PropertyName
import zzz.projects.liztzzz.data.LiztItem

data class Lizt(
    val hasSuggests: Boolean = false,
    @get:PropertyName("isDeletable")
    val isDeletable: Boolean = false,
    val liztName: String = "",
    val liztSuggested: List<LiztItem> = emptyList(),
    val liztUnchecked: List<LiztItem> = emptyList(),
    val liztChecked: List<LiztItem> = emptyList(),
    val showUnchecked: Boolean = true
)

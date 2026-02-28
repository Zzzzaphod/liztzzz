package zzz.projects.liztzzz.data

import com.google.firebase.database.PropertyName

data class LiztItem(
    @get:PropertyName("isChecked")
    @set:PropertyName("isChecked")
    var isChecked: Boolean = false,
    @get:PropertyName("isSuggest")
    @set:PropertyName("isSuggest")
    var isSuggest: Boolean = false,
    @get:PropertyName("itemName")
    @set:PropertyName("itemName")
    var itemName: String = ""
)

package com.rose.clubs.models.firebase

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.rose.clubs.data.Match
import com.rose.clubs.viewmodels.addmatch.AddMatchModel
import com.rose.clubs.viewmodels.addmatch.SaveMatchResult

class FirebaseAddMatchModel(
    private val firestore: FirebaseFirestore = Firebase.firestore
) : AddMatchModel {
    override suspend fun saveMatch(clubId: String, match: Match): SaveMatchResult {
        return firestore.saveMatch(clubId, match)
    }
}
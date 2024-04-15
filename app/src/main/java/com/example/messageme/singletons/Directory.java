package com.example.messageme.singletons;

import android.util.Log;

import androidx.annotation.NonNull;

import com.example.messageme.data.Message;
import com.example.messageme.data.User;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.HashMap;

public final class Directory {
    private Directory() {}

    public static HashMap<String, User> directory = new HashMap<>();

    public static void GetDirectory() {
        FirebaseFirestore mStorage = FirebaseFirestore.getInstance();

        mStorage.collection("directory")
            .addSnapshotListener((value, error) -> {
                directory.clear();
                for (QueryDocumentSnapshot document: value) {
                    User u = document.toObject(User.class);
                    directory.put(u.getUid(), u);
                }
        });
    }
}

package com.example.medshering.Models;

import android.support.annotation.NonNull;

import com.google.firebase.firestore.Exclude;

public class AdPostId {

    @Exclude
    public String adPostId;

    public <T extends AdPostId> T withId(@NonNull final String id) {
        this.adPostId = id;
        return (T) this;
    }

}

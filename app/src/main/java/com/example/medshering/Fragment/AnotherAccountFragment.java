package com.example.medshering.Fragment;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.medshering.Models.AdPost;
import com.example.medshering.Adapter.AnotherAccountAdRecycleAdapter;
import com.example.medshering.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;



public class AnotherAccountFragment extends Fragment {

    FirebaseAuth mAuth;
    FirebaseFirestore mFirestore;
    String userId;

    RecyclerView userAdListView;
    List<AdPost> userAdList;
    AnotherAccountAdRecycleAdapter anotherAccountAdRecycleAdapter;

    public AnotherAccountFragment() {
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view =  inflater.inflate(R.layout.fragment_another_account, container, false);

        mAuth = FirebaseAuth.getInstance();
        mFirestore = FirebaseFirestore.getInstance();
        userId = getArguments().getString("userId");

        userAdListView = view.findViewById(R.id.another_user_ad_list);
        userAdList = new ArrayList<>();
        anotherAccountAdRecycleAdapter = new AnotherAccountAdRecycleAdapter(userAdList);

        userAdListView.setLayoutManager(new LinearLayoutManager(container.getContext()));
        userAdListView.setAdapter(anotherAccountAdRecycleAdapter);

        Query accountQuery = mFirestore.collection("Posts").whereEqualTo("user_id", userId);
        accountQuery.addSnapshotListener(getActivity(), new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
                for (DocumentChange doc : queryDocumentSnapshots.getDocumentChanges()) {

                    if (doc.getType() == DocumentChange.Type.ADDED) {

                        AdPost adPost = doc.getDocument().toObject(AdPost.class);
                        userAdList.add(adPost);
                        anotherAccountAdRecycleAdapter.notifyDataSetChanged();

                    }
                }
            }
        });

        return view;
    }

}

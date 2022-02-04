package com.example.medshering.Fragment;


import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.medshering.Models.AdPost;
import com.example.medshering.Adapter.AdRecycleAdapter;
import com.example.medshering.R;
import com.example.medshering.Models.User;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;



public class HomeFragment extends Fragment {

    FirebaseFirestore mFirestore;
    FirebaseAuth mAuth;

    RecyclerView homeListView;
    List<AdPost> adList;
    AdRecycleAdapter adRecycleAdapter;
    List<User> userList;

    DocumentSnapshot lastVisible;
    Boolean isFirstPageFirstLoaded = true;

    public HomeFragment() {

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        mAuth = FirebaseAuth.getInstance();

        homeListView = view.findViewById(R.id.home_ad_list);
        adList = new ArrayList<>();
        userList = new ArrayList<>();

        adRecycleAdapter = new AdRecycleAdapter(adList, userList);

        homeListView.setLayoutManager(new LinearLayoutManager(container.getContext()));
        homeListView.setAdapter(adRecycleAdapter);

        if (mAuth.getCurrentUser() != null) {

            mFirestore = FirebaseFirestore.getInstance();


            homeListView.addOnScrollListener(new RecyclerView.OnScrollListener() {
                @Override
                public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                    super.onScrolled(recyclerView, dx, dy);

                    Boolean reachedBottom = !recyclerView.canScrollVertically(1);
                    if (reachedBottom) {
                        loadMorePost();
                    }
                }
            });

            Query firstQuery = mFirestore.collection("Posts").orderBy("timestamp", Query.Direction.DESCENDING).limit(3);

            firstQuery.addSnapshotListener(getActivity(), new EventListener<QuerySnapshot>() {
                @Override
                public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {

                    if (isFirstPageFirstLoaded) {
                        lastVisible = queryDocumentSnapshots.getDocuments().get(queryDocumentSnapshots.size() - 1);
                        adList.clear();
                        userList.clear();
                    }

                    for (DocumentChange doc : queryDocumentSnapshots.getDocumentChanges()) {
                        if (doc.getType() == DocumentChange.Type.ADDED) {

                            String adPostId = doc.getDocument().getId();
                            final AdPost adPost = doc.getDocument().toObject(AdPost.class).withId(adPostId);

                            String adUserId = doc.getDocument().getString("user_id");
                            mFirestore.collection("Users").document(adUserId).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                @Override
                                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                    if (task.isSuccessful()) {
                                        User user = task.getResult().toObject(User.class);

                                        if (isFirstPageFirstLoaded) {
                                            adList.add(adPost);
                                            userList.add(user);
                                        } else {
                                            adList.add(0, adPost);
                                            userList.add(0, user);
                                        }

                                        adRecycleAdapter.notifyDataSetChanged();
                                    }
                                }
                            });
                        }
                    }
                    isFirstPageFirstLoaded = false;
                }
            });
        }

        return view;
    }

    public void loadMorePost() {
        if (mAuth.getCurrentUser() != null) {

            Query nextQuery = mFirestore.collection("Posts")
                    .orderBy("timestamp", Query.Direction.DESCENDING)
                    .startAfter(lastVisible)
                    .limit(3);

            nextQuery.addSnapshotListener(getActivity(), new EventListener<QuerySnapshot>() {
                @Override
                public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {

                    if (!queryDocumentSnapshots.isEmpty()) {

                        lastVisible = queryDocumentSnapshots.getDocuments().get(queryDocumentSnapshots.size() - 1);

                        for (DocumentChange doc : queryDocumentSnapshots.getDocumentChanges()) {
                            if (doc.getType() == DocumentChange.Type.ADDED) {

                                String adPostId = doc.getDocument().getId();
                                final AdPost adPost = doc.getDocument().toObject(AdPost.class).withId(adPostId);

                                String adUserId = doc.getDocument().getString("user_id");

                                mFirestore.collection("Users").document(adUserId).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                    @Override
                                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {

                                        if (task.isSuccessful()) {
                                            User user = task.getResult().toObject(User.class);

                                            adList.add(adPost);
                                            userList.add(user);

                                            adRecycleAdapter.notifyDataSetChanged();
                                        }
                                    }
                                });
                            }
                        }

                    }
                }
            });
        }
    }
}

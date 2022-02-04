package com.example.medshering.Fragment;


import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.v4.app.Fragment;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.example.medshering.Activity.CommentsActivity;
import com.example.medshering.Models.AdPost;
import com.example.medshering.R;
import com.example.medshering.Models.User;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.annotation.Nullable;

import de.hdodenhof.circleimageview.CircleImageView;


public class PostFragment extends Fragment {

    FirebaseAuth mAuth;
    FirebaseFirestore mFirestore;

    private CircleImageView userProfileImageView;
    private TextView userNameView;
    private TextView dateView;
    private TextView titleView;
    private ImageView ImageView;
    private TextView descView;
    private TextView contentView;
    private ImageView likeBtn;
    private TextView likeCounter;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_ad_post, container, false);


        mAuth = FirebaseAuth.getInstance();
        mFirestore = FirebaseFirestore.getInstance();


        final String currentUserId = mAuth.getCurrentUser().getUid();
        final String adPostId = getArguments().getString("adPostId");
        final String userId = getArguments().getString("userId");

        userProfileImageView = view.findViewById(R.id.post_user_image);
        userNameView = view.findViewById(R.id.post_user_name);
        dateView = view.findViewById(R.id.post_date);
        titleView = view.findViewById(R.id.post_title);
        ImageView = view.findViewById(R.id.post_image);
        descView = view.findViewById(R.id.post_desc);
        contentView = view.findViewById(R.id.post_content);
        android.widget.ImageView commBtn = view.findViewById(R.id.post_comm_btn);
        likeBtn = view.findViewById(R.id.post_like_btn);
        likeCounter = view.findViewById(R.id.post_like_counter);


        commBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent commentIntent = new Intent(getActivity(), CommentsActivity.class);
                commentIntent.putExtra("adPostId", adPostId);
                startActivity(commentIntent);
            }
        });

        mFirestore.collection("Posts/" + adPostId + "/Likes").addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {

                if (!queryDocumentSnapshots.isEmpty()) {
                    int counter = queryDocumentSnapshots.size();
                    likeCounter.setText(Integer.toString(counter));
                } else {
                    likeCounter.setText(" ");
                }
            }
        });

        mFirestore.collection("Posts/" + adPostId + "/Likes").document(currentUserId).addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
            @Override
            public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException e) {

                if (documentSnapshot.exists()) {
                    likeBtn.setImageDrawable(getActivity().getDrawable(R.mipmap.action_like_btn_accent));
                } else {
                    likeBtn.setImageDrawable(getActivity().getDrawable(R.mipmap.action_like_btn_gray));
                }

            }
        });

        likeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                mFirestore.collection("Posts/" + adPostId + "/Likes").document(currentUserId).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {

                        if(!task.getResult().exists()){

                            Map<String, Object> likesMap = new HashMap<>();
                            likesMap.put("timestamp", FieldValue.serverTimestamp());

                            mFirestore.collection("Posts/" + adPostId + "/Likes").document(currentUserId).set(likesMap);
                        } else {
                            mFirestore.collection("Posts/" + adPostId + "/Likes").document(currentUserId).delete();
                        }
                    }
                });
            }
        });

        DocumentReference userDocRef = mFirestore.collection("Users").document(userId);
        userDocRef.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                User user = documentSnapshot.toObject(User.class);

                String firstName = user.getFirst();
                String lastName = user.getLast();
                String profileImageUrl = user.getImage();
                setUserData(firstName, lastName, profileImageUrl);

            }
        });


        DocumentReference adDocRef = mFirestore.collection("Posts").document(adPostId);
        adDocRef.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                AdPost adpost = documentSnapshot.toObject(AdPost.class);

                try {
                    long millisecond = adpost.getTimestamp().getTime();
                    String adDate = DateFormat.format("d/MM/yyyy", new Date(millisecond)).toString();
                    dateView.setText(adDate);
                } catch (Exception e) {
                    Toast.makeText(getActivity(), e.getMessage(), Toast.LENGTH_SHORT).show();
                }

                String adTitle = adpost.getTitle();
                String adImageUrl = adpost.getImage();
                String adImageThumbUrl = adpost.getThumb();
                String adDesc = adpost.getDescription();
                String adContent = adpost.getContent();

                setAdData(adTitle, adImageUrl, adImageThumbUrl, adDesc, adContent);

            }
        });



        return view;
    }

    public void setUserData(String firstName, String lastName, String profileImageUrl) {
        RequestOptions placeholderOption = new RequestOptions();
        placeholderOption.placeholder(R.drawable.ellipse);
        Glide.with(this).applyDefaultRequestOptions(placeholderOption).load(profileImageUrl).into(userProfileImageView);
        userNameView.setText(firstName + " " + lastName);
    }

    public void setAdData(String adTitle, String adImageUrl, String adImageThumbUrl, String adDesc, String adContent) {
        titleView.setText(adTitle);

        RequestOptions placeholderOption = new RequestOptions();
        placeholderOption.placeholder(R.drawable.rectangle);
        Glide.with(this).applyDefaultRequestOptions(placeholderOption)
                .load(adImageUrl)
                .thumbnail(Glide.with(this).load(adImageThumbUrl))
                .into(ImageView);

        descView.setText(adDesc);
        contentView.setText(adContent);
    }


}

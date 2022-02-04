package com.example.medshering.Adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.example.medshering.Activity.CommentsActivity;
import com.example.medshering.Fragment.PostFragment;
import com.example.medshering.Models.AdPost;
import com.example.medshering.Fragment.AnotherAccountFragment;
import com.example.medshering.R;
import com.example.medshering.Models.User;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Nullable;

import de.hdodenhof.circleimageview.CircleImageView;

import static com.example.medshering.Activity.MainActivity.PostFragment;

public class AdRecycleAdapter extends RecyclerView.Adapter<AdRecycleAdapter.ViewHolder> {

    public List<AdPost> adList;
    public List<User> userList;
    public Context context;

    private FirebaseFirestore mFirestore;
    private FirebaseAuth mAuth;

    Boolean currUserLiked = false;

    public class ViewHolder extends RecyclerView.ViewHolder {

        private final View mView;

        private final TextView titleView;
        private ImageView ImageView;
        private TextView userNameView;
        private CircleImageView userProfileImageView;
        private final ImageView likeBtn;
        private final ImageView commBtn;

        public ViewHolder(View itemView) {
            super(itemView);
            mView = itemView;

            commBtn = mView.findViewById(R.id.ad_comm_btn);
            likeBtn = mView.findViewById(R.id.add_like_btn);
            titleView = mView.findViewById(R.id.acc_ad_title);

        }

        public void setDescText(String descString) {
            TextView descView = mView.findViewById(R.id.ad_desc);
            descView.setText(descString);
        }

        public void setTitleText(String titleString) {
            titleView.setText(titleString);

        }

        public void setAdImage(String imageUrl, String thumbnailUrl) {
            ImageView = mView.findViewById(R.id.ad_image);
            RequestOptions placeholderOption = new RequestOptions();
            placeholderOption.placeholder(R.drawable.rectangle);
            Glide.with(context).applyDefaultRequestOptions(placeholderOption)
                    .load(imageUrl)
                    .thumbnail(Glide.with(context).load(thumbnailUrl))
                    .into(ImageView);
        }

        public void setTime(String dateString) {
            TextView dateView = mView.findViewById(R.id.ad_date);
            dateView.setText(dateString);
        }

        @SuppressLint("CheckResult")
        public void setUserData(String firstName, String lastName, String profileImageUrl) {
            userNameView = mView.findViewById(R.id.ad_user_name);
            userProfileImageView = mView.findViewById(R.id.ad_user_image);

            RequestOptions placeholderOption = new RequestOptions();
            placeholderOption.placeholder(R.drawable.ellipse);
            Glide.with(context).applyDefaultRequestOptions(placeholderOption).load(profileImageUrl).into(userProfileImageView);

            userNameView.setText(firstName + " " + lastName);
        }

        public void updateLikeCounter(int counter) {
            TextView likeCounter = mView.findViewById(R.id.ad_like_count);

            if (counter > 0)
                likeCounter.setText(Integer.toString(counter));
            else
                likeCounter.setText(" ");

        }

    }

    public void goToAdPostFragment(String adPostId, String user_id, View view) {
        Bundle args = new Bundle();
        args.putString("adPostId", adPostId);
        args.putString("userId", user_id);

        AppCompatActivity activity = (AppCompatActivity) view.getContext();
        PostFragment blogPostFragment = new PostFragment();
        PostFragment.setArguments(args);
        activity.getSupportFragmentManager().beginTransaction().replace(R.id.main_container, PostFragment).addToBackStack(null).commit();
    }

    public void goToUserProfile(String user_id, View view) {
        Bundle args = new Bundle();
        args.putString("userId", user_id);

        AppCompatActivity activity = (AppCompatActivity) view.getContext();
        AnotherAccountFragment anotherAccountFragment = new AnotherAccountFragment();
        anotherAccountFragment.setArguments(args);
        activity.getSupportFragmentManager().beginTransaction().replace(R.id.main_container, anotherAccountFragment).addToBackStack(null).commit();

    }

    public AdRecycleAdapter(List<AdPost> adList, List<User> userList) {
        this.adList = adList;
        this.userList = userList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.ad_list_item, parent, false);

        context = parent.getContext();
        mFirestore = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, int position) {

        holder.setIsRecyclable(false);

        final String currentUserId = mAuth.getCurrentUser().getUid();
        final String adPostId = adList.get(position).adPostId;

        String desc_data = adList.get(position).getDescription();
        holder.setDescText(desc_data);

        String title_data = adList.get(position).getTitle();
        holder.setTitleText(title_data);

        String image_url = adList.get(position).getImage();
        String thumbnail_url = adList.get(position).getThumb();
        holder.setAdImage(image_url, thumbnail_url);


        try {
            long millisecond = adList.get(position).getTimestamp().getTime();
            String dateString = DateFormat.format("d/MM/yyyy", new Date(millisecond)).toString();
            holder.setTime(dateString);
        } catch (Exception e) {

            Toast.makeText(context, "Exception : " + e.getMessage(), Toast.LENGTH_SHORT).show();

        }

        final String user_id = adList.get(position).getUser_id();
    if (userList.get(position)!=null) {
    final String firstName = userList.get(position).getFirst();
    final String lastName = userList.get(position).getLast();
    String profileImageUrl = userList.get(position).getImage();

    holder.setUserData(firstName, lastName, profileImageUrl);
    }

        //Лайки
        mFirestore.collection("Posts/" + adPostId + "/Likes").addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {

                if (!queryDocumentSnapshots.isEmpty()) {
                    int counter = queryDocumentSnapshots.size();
                    holder.updateLikeCounter(counter);
                } else {
                    holder.updateLikeCounter(0);
                }
            }
        });

        mFirestore.collection("Posts/" + adPostId + "/Likes").document(currentUserId).addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
            @Override
            public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException e) {

                if (documentSnapshot.exists()) {
                    holder.likeBtn.setImageDrawable(context.getDrawable(R.mipmap.action_like_btn_accent));
                } else {
                    holder.likeBtn.setImageDrawable(context.getDrawable(R.mipmap.action_like_btn_gray));
                }

            }
        });


        holder.likeBtn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {

                currUserLiked = !currUserLiked;

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


        holder.commBtn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent commentIntent = new Intent(context, CommentsActivity.class);
                commentIntent.putExtra("adPostId", adPostId);
                context.startActivity(commentIntent);
            }
        });

        holder.titleView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                goToAdPostFragment(adPostId, user_id, view);
            }
        });

        holder.ImageView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                goToAdPostFragment(adPostId, user_id, view);
            }
        });

        if (holder.userNameView !=null) {
            holder.userNameView.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View view) {

                    goToUserProfile(user_id, view);
                }
            });
        }
        if (holder.userProfileImageView != null) {
            holder.userProfileImageView.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View view) {
                    goToUserProfile(user_id, view);
                }
            });

        }

    }

    @Override
    public int getItemCount() {
        return adList.size();
    }

}

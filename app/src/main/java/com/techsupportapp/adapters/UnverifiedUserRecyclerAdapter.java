package com.techsupportapp.adapters;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.techsupportapp.R;
import com.techsupportapp.UserProfileActivity;
import com.techsupportapp.databaseClasses.UnverifiedUser;
import com.techsupportapp.databaseClasses.User;
import com.techsupportapp.utility.GlobalsMethods;

import java.util.ArrayList;

public class UnverifiedUserRecyclerAdapter extends RecyclerView.Adapter<UnverifiedUserRecyclerAdapter.ViewHolder> {

    private Context context;
    private final ArrayList<UnverifiedUser> values;


    public class ViewHolder extends RecyclerView.ViewHolder {
        public ImageView userImage;
        public TextView userNameText;
        public TextView userDateText;

        public ViewHolder(View view) {
            super(view);
            userImage = (ImageView) view.findViewById(R.id.userImage);
            userNameText = (TextView) view.findViewById(R.id.userName);
            userDateText = (TextView) view.findViewById(R.id.userDate);
        }
    }


    public UnverifiedUserRecyclerAdapter(Context context, ArrayList<UnverifiedUser> values) {
        this.context = context;
        this.values = values;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_user_unverified, parent, false);

        return new ViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {
        holder.userNameText.setText(values.get(position).getUserName());
        holder.userDateText.setText(values.get(position).getRegistrationDate());
        holder.userImage.setImageBitmap(GlobalsMethods.ImageMethods.createUserImage(values.get(position).getLogin(), context));
    }

    @Override
    public int getItemCount() {
        return values.size();
    }
}
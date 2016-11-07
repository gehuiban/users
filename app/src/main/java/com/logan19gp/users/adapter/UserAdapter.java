package com.logan19gp.users.adapter;

import android.app.Activity;
import android.content.Intent;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.util.Pair;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.logan19gp.users.R;
import com.logan19gp.users.UserActivity;
import com.logan19gp.users.model.User;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by george on 11/4/2016.
 */
public class UserAdapter extends RecyclerView.Adapter<UserAdapter.ViewHolder> {
    private List<User> usersItems = new ArrayList<>();
    private Activity mActivity;

    public void addUsers(List<User> newDays) {
        usersItems.addAll(newDays);
        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        return usersItems.size();
    }

    public UserAdapter(Activity mActivity) {
        this.mActivity = mActivity;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(parent.getContext()), parent);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        final User user = usersItems.get(position);
        if (user == null) {
            return;
        }
        if (user.getName() != null) {
            holder.nameText.setText(user.getName());
        }
        if (user.getAge() != null) {
            String age = String.format(mActivity.getString(R.string.age_formatted), user.getAge());
            holder.ageText.setText(age);
        }
        if (user.getHobbies() != null) {
            String hobbies = "";
            for (String hobby : user.getHobbies()) {
                hobbies += (hobbies.length() > 0 ? ", " : "") + hobby;
            }
            holder.hobbyText.setText(hobbies);
        }

        Picasso.with(mActivity).load(user.getPhoto()).placeholder(R.drawable.ic_noperson).into(holder.userPhoto);
        holder.container.setBackgroundColor(mActivity.getResources().getColor(user.getGenderColorId()));
        holder.container.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(view.getContext(), UserActivity.class);
                ActivityOptionsCompat options = ActivityOptionsCompat.makeSceneTransitionAnimation(
                        mActivity, new Pair<>(view.findViewById(R.id.photo), "primary_image"));

                intent.putExtra(UserActivity.EXTRA_USER, user);
                mActivity.startActivity(intent, options.toBundle());
            }
        });
    }

    public void clearData() {
        usersItems.clear();
        notifyDataSetChanged();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        LinearLayout container;
        TextView nameText;
        TextView ageText;
        TextView hobbyText;
        ImageView userPhoto;

        private ViewHolder(LayoutInflater inflater, ViewGroup parent) {
            super(inflater.inflate(R.layout.item_card, parent, false));

            container = (LinearLayout) itemView.findViewById(R.id.card_container);
            nameText = (TextView) itemView.findViewById(R.id.name_text);
            ageText = (TextView) itemView.findViewById(R.id.age_text);
            hobbyText = (TextView) itemView.findViewById(R.id.hobby_text);
            userPhoto = (ImageView) itemView.findViewById(R.id.photo);
        }
    }
}
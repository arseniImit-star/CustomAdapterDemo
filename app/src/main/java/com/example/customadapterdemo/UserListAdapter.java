package com.example.customadapterdemo;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import java.util.ArrayList;

public class UserListAdapter extends BaseAdapter {
    private final Context ctx;
    private final ArrayList<User> users;
    private User selectedUser;

    public UserListAdapter(Context ctx, ArrayList<User> users) {
        this.ctx = ctx;
        this.users = users;
    }

    public void select(User user) {
        selectedUser = user;
        notifyDataSetChanged();
    }

    @Override public int getCount() { return users.size(); }
    @Override public User getItem(int position) { return users.get(position); }
    @Override public long getItemId(int position) { return position; }

    @Override public View getView(int position, View convertView, ViewGroup parent) {
        View row = convertView;
        if (row == null) row = LayoutInflater.from(ctx).inflate(R.layout.useritem, parent, false);

        User user = getItem(position);
        ImageView avatar = row.findViewById(R.id.userpic);
        TextView name = row.findViewById(R.id.name);
        TextView phone = row.findViewById(R.id.phone);
        TextView role = row.findViewById(R.id.role);

        if (user.avatarUri != null && !user.avatarUri.isEmpty()) {
            try {
                avatar.setImageURI(Uri.parse(user.avatarUri));
                if (avatar.getDrawable() == null) avatar.setImageResource(user.sex.imageRes);
            } catch (Exception e) {
                avatar.setImageResource(user.sex.imageRes);
            }
        } else {
            avatar.setImageResource(user.sex.imageRes);
        }

        name.setText(user.name);
        phone.setText(user.phoneNumber);
        role.setText(user.sex.title + ((user.avatarUri != null && !user.avatarUri.isEmpty()) ? " · своё фото" : ""));

        boolean selected = user == selectedUser;
        row.setBackgroundColor(selected ? Color.rgb(218, 239, 239) : Color.WHITE);
        name.setTypeface(null, selected ? Typeface.BOLD : Typeface.NORMAL);
        return row;
    }
}

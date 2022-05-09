package com.ioanmihai.wall_ebluetooth;

import android.content.Context;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class ViewHolder extends RecyclerView.ViewHolder {
    public TextView name;
    public RelativeLayout root;
    public ImageView button;
    public Context context;

    public ViewHolder(@NonNull View itemView){
        super(itemView);
        name = itemView.findViewById(R.id.robot_name);
        root = itemView.findViewById(R.id.parent);
        button = itemView.findViewById(R.id.connect_button);
        this.context = context;
    }

    public void setName(String string) {name.setText(string);}


}

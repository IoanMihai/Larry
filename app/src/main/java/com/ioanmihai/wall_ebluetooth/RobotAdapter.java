package com.ioanmihai.wall_ebluetooth;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class RobotAdapter extends RecyclerView.Adapter<RobotAdapter.MyViewHolder> {

    private Context context;
    private List<String> names;
    private ImageView button;
    public static String EXTRA_ADDRESS = "device_address";

    public RobotAdapter(Context context, List<String> names){
        this.context = context;
        this.names = names;
    }


    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(context).inflate(R.layout.robots_list, parent, false); 
        return new MyViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull RobotAdapter.MyViewHolder holder, int position) {
        holder.mName.setText(names.get(position).substring(18));
        holder.mAddress.setText(names.get(position).substring(0, 17));
    }

    @Override
    public int getItemCount() {
        return names.size();
    }

    public static class MyViewHolder extends RecyclerView.ViewHolder{

        TextView mName, mAddress;
        ImageView mButton;

        public MyViewHolder(@NonNull View itemView){
            super(itemView);

            mName = itemView.findViewById(R.id.robot_name);
            mButton = itemView.findViewById(R.id.connect_button);
            mAddress = itemView.findViewById(R.id.robot_address);
        }

        public TextView getmName() {
            return mName;
        }

        public void setmName(TextView mName) {
            this.mName = mName;
        }

        public TextView getmAddress() {
            return mAddress;
        }

        public void setmAddress(TextView mAddress) {
            this.mAddress = mAddress;
        }
    }



}

package abdoroid.quranradio.adapter;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

import abdoroid.quranradio.R;
import abdoroid.quranradio.pojo.RadioDataModel;
import abdoroid.quranradio.ui.player.PlayerActivity;

public class RadioAdapter extends RecyclerView.Adapter<RadioAdapter.RadioViewHolder> {

    private ArrayList<RadioDataModel> radiosList = new ArrayList<>();
    private final Context context;
    private static String url;
    private final SharedPreferences sharedPreferences;

    public RadioAdapter(Context context, SharedPreferences sharedPreferences) {
        this.context = context;
        this.sharedPreferences = sharedPreferences;
    }

    @NonNull
    @Override
    public RadioAdapter.RadioViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new RadioViewHolder(LayoutInflater.from(parent.getContext())
                .inflate(R.layout.radio_item, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull RadioAdapter.RadioViewHolder holder, int position) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        holder.radioName.setText(radiosList.get(position).getName());
        setFocusableInTouchModeByScreenSize(radiosList.get(position).getName().length(), holder.radioName);
        holder.radioName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Activity activity = (Activity) context;
                Intent intent = new Intent(context.getApplicationContext(), PlayerActivity.class);
                intent.putExtra(PlayerActivity.LIST_POSITION, position);
                intent.putExtra(PlayerActivity.AUDIO_LIST, radiosList);
                activity.startActivity(intent);
            }
        });
        String url = radiosList.get(position).getUrl();
        holder.favIcon.setImageResource(R.drawable.ic_baseline_favorite_white);
        holder.favIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (sharedPreferences.contains(url)){
                    holder.favIcon.setImageResource(R.drawable.ic_baseline_favorite_white);
                    editor.remove(url);
                    editor.apply();
                }else{
                    holder.favIcon.setImageResource(R.drawable.ic_baseline_favorite_red);
                    editor.putString(url, radiosList.get(position).getName());
                    editor.apply();
                }
            }
        });
        if (sharedPreferences.contains(url)) {
            holder.favIcon.setImageResource(R.drawable.ic_baseline_favorite_red);
        }

    }

    @Override
    public int getItemCount() {
        return radiosList.size();
    }

    public void setRadiosList(ArrayList<RadioDataModel> radiosList) {
        this.radiosList = radiosList;
        notifyDataSetChanged();
    }

    private void setFocusableInTouchModeByScreenSize(int stringLength, View view){
        int screenSize = context.getResources().getConfiguration().screenLayout &
                Configuration.SCREENLAYOUT_SIZE_MASK;

        if (screenSize == Configuration.SCREENLAYOUT_SIZE_NORMAL && (stringLength > 26)){
            view.setFocusableInTouchMode(true);
        }

        if (screenSize == Configuration.SCREENLAYOUT_SIZE_SMALL && (stringLength > 19)){
            view.setFocusableInTouchMode(true);
        }
    }

    static class RadioViewHolder extends RecyclerView.ViewHolder{
        TextView radioName;
        ImageView favIcon;
        public RadioViewHolder(@NonNull View itemView) {
            super(itemView);
            radioName = itemView.findViewById(R.id.radio_name);
            favIcon = itemView.findViewById(R.id.remove_icon);
        }
    }
}

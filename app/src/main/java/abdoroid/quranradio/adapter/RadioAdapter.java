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

import com.google.android.material.card.MaterialCardView;

import java.util.ArrayList;

import abdoroid.quranradio.R;
import abdoroid.quranradio.pojo.RadioDataModel;
import abdoroid.quranradio.ui.player.PlayerActivity;

public class RadioAdapter extends RecyclerView.Adapter<RadioAdapter.RadioViewHolder> {

    private ArrayList<RadioDataModel> radiosList = new ArrayList<>();
    private final Context context;
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
        holder.radioName.setSelected(true);
        holder.cardItem.setOnClickListener(view -> {
            Activity activity = (Activity) context;
            Intent intent = new Intent(context.getApplicationContext(), PlayerActivity.class);
            intent.putExtra(PlayerActivity.LIST_POSITION, position);
            intent.putExtra(PlayerActivity.AUDIO_LIST, radiosList);
            activity.startActivity(intent);
        });
        String url = radiosList.get(position).getUrl();
        holder.favIcon.setImageResource(R.drawable.ic_baseline_favorite_white);
        holder.favIcon.setOnClickListener(view -> {
            if (sharedPreferences.contains(url)){
                holder.favIcon.setImageResource(R.drawable.ic_baseline_favorite_white);
                editor.remove(url);
                editor.apply();
            }else{
                holder.favIcon.setImageResource(R.drawable.ic_baseline_favorite_red);
                editor.putString(url, radiosList.get(position).getName());
                editor.apply();
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

    static class RadioViewHolder extends RecyclerView.ViewHolder{
        final TextView radioName;
        final ImageView favIcon;
        final MaterialCardView cardItem;
        public RadioViewHolder(@NonNull View itemView) {
            super(itemView);
            radioName = itemView.findViewById(R.id.radio_name);
            favIcon = itemView.findViewById(R.id.remove_icon);
            cardItem = itemView.findViewById(R.id.card_item);
        }
    }
}

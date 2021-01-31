package abdoroid.quranradio.adapter;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.card.MaterialCardView;

import java.util.ArrayList;

import abdoroid.quranradio.R;
import abdoroid.quranradio.pojo.RadioDataModel;
import abdoroid.quranradio.ui.player.PlayerActivity;
import abdoroid.quranradio.utils.StorageUtils;

public class RadioAdapter extends RecyclerView.Adapter<RadioAdapter.RadioViewHolder> implements Filterable {

    private ArrayList<RadioDataModel> radiosList = new ArrayList<>();
    private final ArrayList<RadioDataModel> fullRadioList;
    private final Context context;
    private StorageUtils storageUtils;
    private final String radioType;

    public RadioAdapter(Context context, String radioType) {
        this.context = context;
        this.radioType = radioType;
        fullRadioList = new ArrayList<>(radiosList);
    }

    public RadioAdapter(Context context, String radioType, ArrayList<RadioDataModel> radiosList) {
        this.context = context;
        this.radiosList = radiosList;
        this.radioType = radioType;
        fullRadioList = new ArrayList<>(radiosList);
    }

    @NonNull
    @Override
    public RadioAdapter.RadioViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new RadioViewHolder(LayoutInflater.from(parent.getContext())
                .inflate(R.layout.radio_item, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull RadioAdapter.RadioViewHolder holder, int position) {
        storageUtils = new StorageUtils(context);
        String url = radiosList.get(position).getUrl();
        String title = radiosList.get(position).getName();
        holder.radioName.setText(title);
        holder.radioName.setSelected(true);
        holder.cardItem.setOnClickListener(view -> {
            Activity activity = (Activity) context;
            Intent intent = new Intent(context.getApplicationContext(), PlayerActivity.class);
            if (radioType.equals(storageUtils.FAVOURITES_PLAYER)){
                storageUtils.setPlayerType(storageUtils.FAVOURITES_PLAYER);
            }else {
                storageUtils.setPlayerType(storageUtils.STATION_PLAYER);
            }
            storageUtils.storeAudio(radiosList);
            storageUtils.storeAudioIndex(position);
            activity.startActivity(intent);
            activity.finish();
        });

        holder.favIcon.setImageResource(R.drawable.ic_baseline_favorite_white);
        holder.favIcon.setOnClickListener(view -> {
            if (storageUtils.checkFavourites(url)){
                holder.favIcon.setImageResource(R.drawable.ic_baseline_favorite_white);
                storageUtils.removeFavourites(url);
            }else{
                holder.favIcon.setImageResource(R.drawable.ic_baseline_favorite_red);
                storageUtils.storeFavourite(url, title);
            }
        });
        if (storageUtils.checkFavourites(url)) {
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

    @Override
    public Filter getFilter() {
        return radioFilter;
    }

    private final Filter radioFilter = new Filter() {
        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            ArrayList<RadioDataModel> filteredList = new ArrayList<>();
            if (constraint == null || constraint.length() == 0) {
                filteredList.addAll(fullRadioList);
            } else {
                String filterPattern = constraint.toString().toLowerCase().trim();
                for (RadioDataModel item : fullRadioList) {
                    if (item.getName().toLowerCase().contains(filterPattern)) {
                        filteredList.add(item);
                    }
                }
            }
            FilterResults results = new FilterResults();
            results.values = filteredList;
            return results;
        }

        @SuppressWarnings({"unchecked", "rawtypes"})
        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            radiosList.clear();
            radiosList.addAll((ArrayList) results.values);
            notifyDataSetChanged();
        }
    };

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

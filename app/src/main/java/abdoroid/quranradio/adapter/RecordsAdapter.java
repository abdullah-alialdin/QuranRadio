package abdoroid.quranradio.adapter;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;

import abdoroid.quranradio.R;
import abdoroid.quranradio.pojo.RadioDataModel;
import abdoroid.quranradio.ui.player.RecordsPlayerActivity;

public class RecordsAdapter extends RecyclerView.Adapter<RecordsAdapter.RadioViewHolder> {

    private ArrayList<RadioDataModel> radiosList = new ArrayList<>();
    private final Context context;
    private final SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;

    public RecordsAdapter(Context context, SharedPreferences sharedPreferences) {
        this.context = context;
        this.sharedPreferences = sharedPreferences;
    }


    @NonNull
    @Override
    public RecordsAdapter.RadioViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new RadioViewHolder(LayoutInflater.from(parent.getContext())
                .inflate(R.layout.recordings_item, parent, false));
    }


    @Override
    public void onBindViewHolder(@NonNull RadioViewHolder holder, int position) {
        editor = sharedPreferences.edit();
        holder.radioName.setText(radiosList.get(position).getName());
        setFocusableInTouchModeByScreenSize(radiosList.get(position).getName().length(), holder.radioName);
        Activity activity = (Activity) context;
        holder.radioName.setOnClickListener(view -> {
            Intent intent = new Intent(context.getApplicationContext(), RecordsPlayerActivity.class);
            intent.putExtra(RecordsPlayerActivity.LIST_POSITION, position);
            intent.putExtra(RecordsPlayerActivity.AUDIO_LIST, radiosList);
            activity.startActivity(intent);
        });

        holder.removeIcon.setOnClickListener(v -> new AlertDialog.Builder(context)
                .setTitle(R.string.alert_dialog)
                .setMessage(R.string.alert_msg)
                .setPositiveButton(android.R.string.yes, (dialog, which) -> {
                    removeItemAt(position);
                    editor.apply();
                    activity.finish();
                    activity.startActivity(activity.getIntent());
                })
                .setNegativeButton(android.R.string.no, null)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show());

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

        if (screenSize == Configuration.SCREENLAYOUT_SIZE_SMALL && (stringLength > 17)){
            view.setFocusableInTouchMode(true);
        }
    }

    static class RadioViewHolder extends RecyclerView.ViewHolder{
        TextView radioName;
        ImageView removeIcon;
        public RadioViewHolder(@NonNull View itemView) {
            super(itemView);
            radioName = itemView.findViewById(R.id.radio_name);
            removeIcon = itemView.findViewById(R.id.remove_icon);
        }
    }

    public void removeItemAt(int position) {
        File file = new File(radiosList.get(position).getUrl());
        if (file.exists()){
            boolean delete = file.delete();
        }
        editor.remove(radiosList.get(position).getName());
        radiosList.remove(position);
        notifyItemRemoved(position);
        notifyItemRangeChanged(position, radiosList.size());

    }
}

package abdoroid.quranradio.adapter;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import java.io.File;
import java.util.ArrayList;

import abdoroid.quranradio.R;
import abdoroid.quranradio.pojo.RadioDataModel;
import abdoroid.quranradio.ui.player.PlayerActivity;
import abdoroid.quranradio.utils.StorageUtils;

public class RecordsAdapter extends RecyclerView.Adapter<RecordsAdapter.RadioViewHolder> {

    private final ArrayList<RadioDataModel> radiosList;
    private final Context context;
    private final StorageUtils storageUtils;

    public RecordsAdapter(Context context) {
        this.context = context;
        storageUtils = new StorageUtils(context);
        radiosList = storageUtils.loadRecordings();
    }


    @NonNull
    @Override
    public RecordsAdapter.RadioViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new RadioViewHolder(LayoutInflater.from(parent.getContext())
                .inflate(R.layout.recordings_item, parent, false));
    }


    @Override
    public void onBindViewHolder(@NonNull RadioViewHolder holder, int position) {
        holder.radioName.setText(radiosList.get(position).getName());
        holder.radioName.setSelected(true);
        Activity activity = (Activity) context;
        holder.radioName.setOnClickListener(view -> {
            Intent intent = new Intent(context.getApplicationContext(), PlayerActivity.class);
            storageUtils.setPlayerType(storageUtils.RECORDINGS_PLAYER);
            storageUtils.storeAudio(radiosList);
            storageUtils.storeAudioIndex(position);
            activity.startActivity(intent);
        });

        holder.removeIcon.setOnClickListener(v -> new AlertDialog.Builder(context, R.style.DialogTheme)
                .setTitle(R.string.alert_dialog)
                .setMessage(R.string.alert_msg)
                .setPositiveButton(R.string.settings_ok, (dialog, which) -> {
                    removeItemAt(position);
                    activity.finish();
                    activity.startActivity(activity.getIntent());
                })
                .setNegativeButton(R.string.settings_cancel, null)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show());

    }

    @Override
    public int getItemCount() {
        return radiosList.size();
    }

    static class RadioViewHolder extends RecyclerView.ViewHolder {
        final TextView radioName;
        final ImageView removeIcon;

        public RadioViewHolder(@NonNull View itemView) {
            super(itemView);
            radioName = itemView.findViewById(R.id.radio_name);
            removeIcon = itemView.findViewById(R.id.remove_icon);
        }
    }

    @SuppressWarnings("unused")
    public void removeItemAt(int position) {
        File file = new File(radiosList.get(position).getUrl());
        if (file.exists()) {
            boolean delete = file.delete();
        }
        storageUtils.removeRecordings(radiosList.get(position).getUrl());
        radiosList.remove(position);
        notifyItemRemoved(position);
        notifyItemRangeChanged(position, radiosList.size());

    }
}

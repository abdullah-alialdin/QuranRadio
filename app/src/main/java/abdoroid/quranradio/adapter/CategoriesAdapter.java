package abdoroid.quranradio.adapter;

import android.content.Context;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import abdoroid.quranradio.R;
import abdoroid.quranradio.pojo.CategoriesDataModel;


public class CategoriesAdapter extends RecyclerView.Adapter<CategoriesAdapter.RecyclerViewViewHolder> {

    private static onRecyclerViewItemClickListener mItemClickListener;
    private final List<CategoriesDataModel> mList;
    private final Context mContext;

    public CategoriesAdapter(Context context, ArrayList<CategoriesDataModel> list) {
        this.mList = list;
        this.mContext = context;
    }

    @NonNull
    @Override
    public RecyclerViewViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.category_item, parent, false);
        return new RecyclerViewViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerViewViewHolder holder, int position) {
        DisplayMetrics displayMetrics = mContext.getResources().getDisplayMetrics();
        int pxHeight = displayMetrics.heightPixels;
        float height = pxHeight / displayMetrics.density;
        float txtsize = height * 0.042f;
        holder.title.setTextSize(txtsize);
        final CategoriesDataModel data = mList.get(position);
        holder.title.setText(data.getTitle());
        holder.description.setText(data.getDescription());
        holder.image.setImageResource(data.getImageSource());
    }

    @Override
    public int getItemCount() {
        return mList.size();
    }

    public void setOnItemClickListener(onRecyclerViewItemClickListener mItemClickListener) {
        CategoriesAdapter.mItemClickListener = mItemClickListener;
    }

    public interface onRecyclerViewItemClickListener {
        void onItemClickListener(View view, int position);
    }

    static class RecyclerViewViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        final TextView title, description;
        final ImageView image;

        RecyclerViewViewHolder(@NonNull View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.title);
            description = itemView.findViewById(R.id.description);
            image = itemView.findViewById(R.id.cat_image);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            if (mItemClickListener != null) {
                mItemClickListener.onItemClickListener(view, getAdapterPosition());
            }
        }
    }
}
package com.fullwipe.fingerprintdrive.explore;

import android.graphics.Color;
import android.graphics.drawable.ShapeDrawable;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.fullwipe.fingerprintdrive.R;
import com.fullwipe.fingerprintdrive.model.Document;
import com.fullwipe.fingerprintdrive.utils.FoldCornerCard;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by Valentina Lipari, Meo Giovanni and Illiano Francesca on 17/08/17.
 */

public class ExploreAdapter extends RecyclerView.Adapter<ExploreAdapter.ViewHolder> {
    private List<Document> documents;

    private final ShapeDrawable shapeDrawable;

    public ExploreAdapter(List<Document> documents) {
        this.documents = documents;

        shapeDrawable = new ShapeDrawable(
                new FoldCornerCard(Color.GREEN, 0.1f));
        shapeDrawable.getPaint().setColor(Color.WHITE);
        shapeDrawable.setIntrinsicHeight(-1);
        shapeDrawable.setIntrinsicWidth(-1);
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.document_grid_item, null);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        Document document = documents.get(position);
        holder.title.setText(document.title);
        holder.description.setText(document.description);
       // holder.container.setBackground(shapeDrawable);
    }

    @Override
    public int getItemCount() {
        return documents.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.title) TextView title;
        @BindView(R.id.description) TextView description;

        public ViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }
}

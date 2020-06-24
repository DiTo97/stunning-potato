package it.unige.ai.speech.synthesis.adapters;

import android.content.Context;
import android.speech.tts.TextToSpeech;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.paging.PagedListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.squareup.picasso.Picasso;

import it.unige.ai.speech.R;
import it.unige.ai.speech.synthesis.listeners.CaptioningViewListener;
import it.unige.ai.speech.synthesis.metadata.ImageMetadata;

public class CaptioningAdapter extends PagedListAdapter<ImageMetadata,
        CaptioningAdapter.GalleryViewHolder> {

    private TextToSpeech mTts;

    public CaptioningAdapter(TextToSpeech tts) {
        super(ImageMetadata.DIFF_itemCallback);
        this.mTts = tts;
    }

    @Override @NonNull
    public GalleryViewHolder onCreateViewHolder(@NonNull ViewGroup parentView, int viewType) {
        // Inflate an item view.
        View itemView = LayoutInflater.from(parentView.getContext()).inflate(
                R.layout.card_img_gallery, parentView, false);

        return new GalleryViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull GalleryViewHolder viewHolder, int position) {
        ImageMetadata imageMetadata = getItem(position);

        if (imageMetadata != null) {
            viewHolder.bindTo(imageMetadata);
        }
    }

    @Override
    public int getItemCount() {
        return super.getItemCount();
    }

    @Override
    public long getItemId(int position) {
        return getCurrentList() == null
                ? 0 : getCurrentList().get(position).getId();
    }

    public class GalleryViewHolder extends RecyclerView.ViewHolder {

        // Card view has only an ImageView
        public ImageView ivGallery;

        public GalleryViewHolder(View itemView) {
            super(itemView);

            ivGallery = itemView.findViewById(
                    R.id.iv_gallery);
        }

        public Context getContext() {
            return itemView.getContext();
        }

        private void bindTo(ImageMetadata imageMetadata) {
            // Retrieve data for that position
            String url = imageMetadata.getUrl();

            // Add data to the view
            ivGallery.setOnClickListener(new CaptioningViewListener(
                    imageMetadata, mTts, getContext()));

            // Download image with Picasso
            Picasso.get().load(url).fit().centerCrop()
                    .into(ivGallery);
        }

    }

}
package it.unige.ai.speech.synthesis.metadata;

import androidx.recyclerview.widget.DiffUtil;

public class ImageMetadata {

    private final long mId;

    private final String mUrl;
    private final String mDescription;

    public ImageMetadata(int id, String url, String description) {
        this.mDescription = description;

        this.mId  = id;
        this.mUrl = url;
    }

    public long getId() {
        return mId;
    }

    public String getUrl() {
        return mUrl;
    }

    public String getDescription() {
        return mDescription;
    }

    // ItemCallback to assist GalleryAdapter in refreshing the UI whenever changes between
    // metadata of two different images from the Internet are detected
    public static final DiffUtil.ItemCallback<ImageMetadata> DIFF_itemCallback =
            new DiffUtil.ItemCallback<ImageMetadata>() {
                @Override
                public boolean areItemsTheSame(ImageMetadata oldItem,
                                               ImageMetadata newItem) {
                    return oldItem.mId == newItem.mId;
                }

                @Override
                public boolean areContentsTheSame(ImageMetadata oldItem,
                                                  ImageMetadata newItem) {
                    return oldItem.mUrl.equals(newItem.mUrl)
                            && oldItem.mDescription.equals(newItem.mDescription);
                }
            };

}

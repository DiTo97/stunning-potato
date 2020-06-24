package it.unige.ai.speech.synthesis.dataSources;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.paging.DataSource;
import androidx.paging.PageKeyedDataSource;

import java.util.List;

import it.unige.ai.speech.synthesis.metadata.ImageMetadata;
import it.unige.ai.speech.synthesis.providers.ImageMetadataListProvider;

import static it.unige.ai.speech.synthesis.utils.CaptioningUtils.URLs;

public class ImageMetadataDataSource extends PageKeyedDataSource<Integer, ImageMetadata> {

    private final String TAG = ImageMetadataDataSource.class.getSimpleName();
    private ImageMetadataListProvider mProvider;

    // True constructor
    public ImageMetadataDataSource(Context context) {
        mProvider = new ImageMetadataListProvider(context);
    }

    // Dummy constructor for testing
    public ImageMetadataDataSource() {
        mProvider = new ImageMetadataListProvider(URLs);
    }

    @Override
    public void loadInitial(@NonNull LoadInitialParams<Integer> params,
                            @NonNull LoadInitialCallback<Integer, ImageMetadata> callback) {
        List<ImageMetadata> imagesMetadata = mProvider.getPagedImagesMetadata(
                0, params.requestedLoadSize);

        callback.onResult(imagesMetadata, null,
                1);
    }

    @Override
    public void loadBefore(@NonNull LoadParams<Integer> params,
                           @NonNull LoadCallback<Integer, ImageMetadata> callback) {
        List<ImageMetadata> imagesMetadata = mProvider.getPagedImagesMetadata(
                params.key, params.requestedLoadSize);

        callback.onResult(imagesMetadata, params.key > 1
                ? params.key - 1 : null);
    }

    @Override
    public void loadAfter(@NonNull LoadParams<Integer> params,
                          @NonNull LoadCallback<Integer, ImageMetadata> callback) {
        List<ImageMetadata> imagesMetadata = mProvider.getPagedImagesMetadata(
                params.key, params.requestedLoadSize);

        callback.onResult(imagesMetadata, !imagesMetadata.isEmpty()
                ? params.key + 1 : null); // Wait for more data
    }

    public static class Factory extends DataSource.Factory<Integer, ImageMetadata> {

        private Context mContext;

        public Factory() {

        }

        public Factory(Context context) {
            this.mContext = context;
        }

        @Override @NonNull
        public DataSource<Integer, ImageMetadata> create() {
            if (mContext != null) {
                return new ImageMetadataDataSource(mContext);
            }

            return new ImageMetadataDataSource();
        }

    }

    @Override
    public void invalidate() {
        super.invalidate();
        Log.d(TAG, "DataSource > invalidate");
    }

}

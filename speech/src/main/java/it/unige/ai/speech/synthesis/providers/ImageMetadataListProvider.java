package it.unige.ai.speech.synthesis.providers;

import android.content.Context;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import it.unige.ai.speech.synthesis.metadata.ImageMetadata;

import static it.unige.ai.speech.synthesis.utils.CaptioningUtils.generateImagesMetadata;
import static it.unige.ai.speech.synthesis.utils.CaptioningUtils.readImagesMetadata;

public class ImageMetadataListProvider {

    private ArrayList<ImageMetadata> mImagesMetadata;

    public ImageMetadataListProvider(String[] dummyUrls) {
        mImagesMetadata = generateImagesMetadata(dummyUrls);
        Collections.shuffle(mImagesMetadata);
    }

    public ImageMetadataListProvider(Context context) {
        mImagesMetadata = readImagesMetadata(context);
        Collections.shuffle(mImagesMetadata);

        mImagesMetadata = new ArrayList<>(mImagesMetadata
                .subList(0, 1000)); // Limit to first 1000 results
    }

    public List<ImageMetadata> getPagedImagesMetadata(int page, int pageSize) {
        int initialIdx = page * pageSize;
        int finalIdx   = initialIdx + pageSize;

        // New data not available yet
        if (initialIdx >= mImagesMetadata.size()) {
            return Collections.emptyList();
        }

        // Not enough data to fill the page
        if (finalIdx >= mImagesMetadata.size()) {
            finalIdx = mImagesMetadata.size() - 1;
        }

        // Paginate the list of string URLs
        return  mImagesMetadata.subList(initialIdx, finalIdx);
    }

}

package it.unige.ai.speech.synthesis.viewModels;

import android.content.Context;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;
import androidx.paging.LivePagedListBuilder;
import androidx.paging.PagedList;

import it.unige.ai.speech.synthesis.dataSources.ImageMetadataDataSource;
import it.unige.ai.speech.synthesis.executors.BackgroundExecutor;
import it.unige.ai.speech.synthesis.metadata.ImageMetadata;

import static it.unige.ai.speech.synthesis.utils.CaptioningUtils.DIMEN_pageSize;

public class CaptioningViewModel extends ViewModel {

    private final int DIMEN_prefetchDistance = 4;

    public LiveData<PagedList<ImageMetadata>> imagesMetadata;
    private Context mContext;

    public CaptioningViewModel initContext(Context context) {
        this.mContext = context;
        return this;
    }
    private PagedList.Config buildPageConfig() {
        return new PagedList.Config.Builder()
                .setEnablePlaceholders(false)
                .setMaxSize(2*(DIMEN_pageSize + DIMEN_prefetchDistance))
                .setInitialLoadSizeHint(DIMEN_pageSize)
                .setPageSize(DIMEN_pageSize)
                .setPrefetchDistance(DIMEN_prefetchDistance)
                .build();
    }
    public void initImagesMetadata() {
        PagedList.Config pageConfig = buildPageConfig();
        imagesMetadata = new LivePagedListBuilder<>(
                new ImageMetadataDataSource.Factory(mContext), pageConfig)
                .setFetchExecutor(new BackgroundExecutor())
                .setInitialLoadKey(0)
                .build();
    }

    public void refresh() {
        if (imagesMetadata.getValue() != null)
            imagesMetadata.getValue()
                    .getDataSource().invalidate();
    }

}

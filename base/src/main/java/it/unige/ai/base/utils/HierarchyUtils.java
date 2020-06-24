package it.unige.ai.base.utils;

import android.app.Activity;
import android.content.Context;
import android.content.ContextWrapper;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;

/*
 * Static-methods-only class (private constructor) that wraps methods to traverse the hierarchical tree
 * of the application and relate different `Context` instances to the appropriate class.
 */
public class HierarchyUtils {

    private HierarchyUtils() {

    }

    public static Activity getActivity(Context context)  {
        if (context == null)  {
            return null;
        } else if (context instanceof ContextWrapper) {
            if (context instanceof Activity) {
                return (Activity) context;
            } else {
                return getActivity(((ContextWrapper) context).getBaseContext()); // Recursively traverse the contextual tree
                                                                                 // if no `Activity` instance is found
            }
        }
        return null;
    }

    public static ArrayList<View> findViewsByTag(ViewGroup root, String tag){
        ArrayList<View> views = new ArrayList<View>();
        final int childCount = root.getChildCount();
        for (int i = 0; i < childCount; i++) {
            final View child = root.getChildAt(i);
            if (child instanceof ViewGroup) {
                views.addAll(findViewsByTag((ViewGroup) child, tag));
            }

            final Object tagObj = child.getTag();
            if (tagObj != null && tagObj.equals(tag)) {
                views.add(child);
            }

        }
        return views;
    }

}

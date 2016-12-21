package xiaoys.me.lrviewpager;

import android.animation.ValueAnimator;
import android.content.Context;
import android.support.v4.view.ViewPager;
import android.view.View;

import java.util.List;

import static android.view.View.OVER_SCROLL_NEVER;

/**
 * Created by xiaoys on 2016/12/20.
 */

class LRUtils {

    static ViewPager providerDefaultVP(Context context) {
        ViewPager viewPager = new ViewPager(context);
        //set default id for ViewPager
        viewPager.setId(R.id.id_vp_default);
        viewPager.setOverScrollMode(OVER_SCROLL_NEVER);
        return viewPager;
    }

    static void layout(View view, int left, int top, int right, int bottom) {
        if (view != null) {
            view.layout(left, top, right, bottom);
        }
    }

    static int computeScrollDistance(int offset, float factor) {
        int absOffset = Math.abs(offset);
        return (int) Math.pow(absOffset, factor);
    }

    static void cancelAnimator(ValueAnimator animator) {
        if (animator != null && animator.isRunning()) {
            animator.cancel();
        }
    }

    static void sendUIHandlerOnBegin(List<DragViewPager.IUIHandler> uiHandlers, @DragViewPager.Which int which, DragViewPager viewPager) {
        if (uiHandlers != null && uiHandlers.size() > 0) {
            for (DragViewPager.IUIHandler uiHandler : uiHandlers) {
                uiHandler.onBegin(viewPager, which);
            }
        }
    }

    static void sendUIHandlerOnPull(List<DragViewPager.IUIHandler> uiHandlers, @DragViewPager.Which int which, DragViewPager viewPager) {
        if (uiHandlers != null && uiHandlers.size() > 0) {
            for (DragViewPager.IUIHandler uiHandler : uiHandlers) {
                uiHandler.onPull(viewPager, which);
            }
        }
    }

    static void sendUIHandlerOnRelease(List<DragViewPager.IUIHandler> uiHandlers, @DragViewPager.Which int which, DragViewPager viewPager) {
        if (uiHandlers != null && uiHandlers.size() > 0) {
            for (DragViewPager.IUIHandler uiHandler : uiHandlers) {
                uiHandler.onRelease(viewPager, which);
            }
        }
    }

    static void sendUIHandlerOnEnd(List<DragViewPager.IUIHandler> uiHandlers, @DragViewPager.Which int which, DragViewPager viewPager) {
        if (uiHandlers != null && uiHandlers.size() > 0) {
            for (DragViewPager.IUIHandler uiHandler : uiHandlers) {
                uiHandler.onEnd(viewPager, which);
            }
        }
    }

    static void sendOnRelease(List<DragViewPager.OnReleaseListener> releaseListeners, boolean isOverThreshold,
                              @DragViewPager.Which int which, @DragViewPager.OnReleaseListener.When int when) {
        if (releaseListeners != null && releaseListeners.size() > 0) {
            for (DragViewPager.OnReleaseListener releaseListener : releaseListeners) {
                releaseListener.onRelease(isOverThreshold, which, when);
            }
        }
    }
}

package xiaoys.me.lrviewpager;

import android.animation.ValueAnimator;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

/**
 * Created by xiaoys on 2016/12/20.
 */

class LRUtils {

    static void setUIHandler(ViewGroup holder, View view, IUIHandler[] uiHandler) {
        holder.removeAllViews();
        ViewGroup.LayoutParams lp =
                new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        holder.addView(view, lp);

        uiHandler[0] = view instanceof IUIHandler ? (IUIHandler) view : null;
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

    static void sendOnBegin(IUIHandler uiHandler, LRPtrViewPager viewPager) {
        if (uiHandler != null) {
            uiHandler.onBegin(viewPager);
        }

    }

    static void sendOnPull(IUIHandler uiHandler, LRPtrViewPager viewPager) {
        if (uiHandler != null) {
            uiHandler.onPull(viewPager);
        }

    }

    static void sendOnRelease(IUIHandler uiHandler, LRPtrViewPager viewPager) {
        if (uiHandler != null) {
            uiHandler.onRelease(viewPager);
        }

    }

    static void sendOnEnd(IUIHandler uiHandler, LRPtrViewPager viewPager) {
        if (uiHandler != null) {
            uiHandler.onEnd(viewPager);
        }
    }
}

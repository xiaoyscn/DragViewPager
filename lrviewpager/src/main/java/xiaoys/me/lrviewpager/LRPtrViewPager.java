package xiaoys.me.lrviewpager;

import android.animation.ValueAnimator;
import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.support.annotation.FloatRange;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.animation.Interpolator;
import android.view.animation.LinearInterpolator;
import android.widget.FrameLayout;

/**
 * Created by xiaoys on 2016/6/15.
 */
public class LRPtrViewPager extends ViewGroup implements ValueAnimator.AnimatorUpdateListener {

    public static final int STATE_NORMAL = 0;
    public static final int STATE_DRAG_LEFT = 1;
    public static final int STATE_DRAG_RIGHT = 2;

    private static final int DEFAULT_RESET_DURATION = 300;

    /**
     * View
     */
    private ViewPager mViewPager;
    private FrameLayout mLeft;
    private FrameLayout mRight;

    private IUIHandler mLeftUIHandler;
    private IUIHandler mRightUIHandler;
    /**
     * can be set properties
     */
    private int mResetDuration = DEFAULT_RESET_DURATION;//reset duration
    private float mScrollSnapRatio = 1f;
    private float mDampingFactor = 0.85f;
    private Interpolator mResetInterpolator = new LinearInterpolator();
    private boolean mFlexible = true;

    /**
     * private properties
     */
    private int mScrollSnap;
    private float mMinTouchDistance;
    private float mActionDownX;
    private float mActionMoveX;
    private int mState;

    private ValueAnimator mResetAnimator;
    private OnRefreshCallback mOnRefreshCallback;

    public LRPtrViewPager(Context context) {
        this(context, null);
    }

    public LRPtrViewPager(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public LRPtrViewPager(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs, defStyleAttr);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public LRPtrViewPager(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(context, attrs, defStyleAttr);
    }

    private void init(Context context, AttributeSet attrs, int defStyleAttr) {
        mMinTouchDistance = ViewConfiguration.get(context).getScaledTouchSlop();
        mViewPager = new ViewPager(context);
        mViewPager.setOverScrollMode(OVER_SCROLL_NEVER);
        //set default id for ViewPager
        mViewPager.setId(R.id.id_vp_default);
        addView(mViewPager);

        mLeft = new FrameLayout(context);
        addView(mLeft);

        mRight = new FrameLayout(context);
        addView(mRight);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        measureChildren(widthMeasureSpec, heightMeasureSpec);
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        int width = getMeasuredWidth();
        int height = getMeasuredHeight();
        int paddingLeft = getPaddingLeft();
        int paddingTop = getPaddingTop();
        int paddingRight = getPaddingRight();
        int paddingBottom = getPaddingBottom();

        mViewPager.layout(paddingLeft, paddingTop, width - paddingRight, height - paddingBottom);
        mLeft.layout(-mLeft.getMeasuredWidth(), paddingTop, 0, height - paddingBottom);
        mRight.layout(width, paddingTop, width - paddingRight + mRight.getMeasuredWidth(), height - paddingBottom);
        mScrollSnap = (int) (mRight.getMeasuredWidth() * mScrollSnapRatio) + 10;
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        switch (ev.getAction() & MotionEvent.ACTION_MASK) {
            case MotionEvent.ACTION_DOWN:
                mActionDownX = ev.getX();
                if (mResetAnimator != null && mResetAnimator.isRunning()) {
                    mResetAnimator.cancel();
                }
                break;
            case MotionEvent.ACTION_MOVE:
                mActionMoveX = ev.getX();
                //如果是最后一个，拦截向左滑动事件
                if (isLastItem() && mActionMoveX - mActionDownX < -mMinTouchDistance) {
                    mState = STATE_DRAG_LEFT;
                    return true;
                }
                //如果是第一个， 拦截向右滑动事件
                else if (isFirstItem() && mActionMoveX - mActionDownX > mMinTouchDistance) {
                    mState = STATE_DRAG_RIGHT;
                    return true;
                }
                resetOffset();
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                resetOffset();
                break;

        }
        return super.onInterceptTouchEvent(ev);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction() & MotionEvent.ACTION_MASK) {
            case MotionEvent.ACTION_MOVE:
                mActionMoveX = event.getX();
                float offset = mActionMoveX - mActionDownX;
                scrollTo(getCorrectScrollX((int) offset), 0);
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                resetOffset();
                if (mOnRefreshCallback != null && Math.abs(getScrollX()) > mScrollSnap) {
                    mOnRefreshCallback.onRefresh();
                }
                break;
        }
        return super.onTouchEvent(event);
    }

    private int getCorrectScrollX(int x) {
        if (mState == STATE_DRAG_LEFT) {
            return (int) Math.pow(Math.max(0, -x), mDampingFactor);
        } else if (mState == STATE_DRAG_RIGHT) {
            return -(int) Math.pow(Math.max(0, x), mDampingFactor);
        } else {
            return 0;
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        if (mResetAnimator != null && mResetAnimator.isStarted()) {
            mResetAnimator.cancel();
            mResetAnimator = null;
        }
        super.onDetachedFromWindow();
    }

    /**
     * Reset widget to origin state
     */
    private void resetOffset() {
        mState = STATE_NORMAL;
        if (getScrollX() == 0) {
            return;
        }
        if (mResetAnimator == null) {
            mResetAnimator = ValueAnimator.ofInt(getScrollX(), 0);
            mResetAnimator.addUpdateListener(this);
            mResetAnimator.setInterpolator(mResetInterpolator);
            mResetAnimator.setDuration(mResetDuration);
        } else {
            mResetAnimator.setIntValues(getScrollX(), 0);
        }
        mResetAnimator.start();
    }

    @Override
    public void onAnimationUpdate(ValueAnimator animation) {
        int value = (int) animation.getAnimatedValue();
        scrollTo(value, 0);
    }

    public void setAdapter(PagerAdapter adapter) {
        mViewPager.setAdapter(adapter);
    }

    public int getCount() {
        PagerAdapter adapter = mViewPager.getAdapter();
        return adapter == null ? 0 : adapter.getCount();
    }

    public void setLeftView(View view) {
        mLeft.removeAllViews();
        FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
        lp.gravity = Gravity.CENTER;
        mLeft.addView(view, lp);
        if (view != null && view instanceof IUIHandler) {
            mLeftUIHandler = (IUIHandler) view;
        } else {
            mLeftUIHandler = null;
        }
    }

    public void setRightView(View view) {
        mRight.removeAllViews();
        FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
        lp.gravity = Gravity.CENTER;
        mRight.addView(view, lp);
        if (view != null && view instanceof IUIHandler) {
            mRightUIHandler = (IUIHandler) view;
        } else {
            mRightUIHandler = null;
        }
    }

    /**
     * @return true if is first item
     */
    public boolean isFirstItem() {
        return mViewPager.getCurrentItem() == 0;
    }

    /**
     * @return true if is last item
     */
    public boolean isLastItem() {
        return mViewPager.getCurrentItem() == getCount() - 1;
    }

    /**
     * 设置拖拽的阻尼系数(0f - 1f)
     * 建议保持在(0.6f - 1f)，越接近1f，阻力越小
     *
     * @param dampingFactor
     */
    public void setDampingFactor(@FloatRange(from = 0f, to = 1f) float dampingFactor) {
        mDampingFactor = dampingFactor;
    }

    public void notifyDataSetChanged() {
        PagerAdapter adapter = mViewPager.getAdapter();
        if (adapter != null) {
            adapter.notifyDataSetChanged();
        }
    }

    public void addOnPageChangeListener(ViewPager.OnPageChangeListener listener) {
        mViewPager.addOnPageChangeListener(listener);
    }

    public void setOnRefreshCallback(OnRefreshCallback onRefreshCallback) {
        mOnRefreshCallback = onRefreshCallback;
    }

    public interface OnRefreshCallback {
        void onRefresh();
    }
}
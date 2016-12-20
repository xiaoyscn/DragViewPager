package xiaoys.me.lrviewpager;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Color;
import android.graphics.PointF;
import android.os.Build;
import android.support.annotation.FloatRange;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
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
public class LRPtrViewPager extends ViewGroup implements ValueAnimator.AnimatorUpdateListener, ValueAnimator.AnimatorListener {

    public static final int STATE_NORMAL = 0;
    public static final int STATE_DRAG_LEFT = 1;
    public static final int STATE_DRAG_RIGHT = 2;

    private static final int DEFAULT_RESET_DURATION = 300;

    private ViewPager mViewPager;
    private FrameLayout mLeftHolder;
    private FrameLayout mRightHolder;

    private IUIHandler[] mLeftUIHandler = new IUIHandler[1];
    private IUIHandler[] mRightUIHandler = new IUIHandler[1];
    private IUIHandler mCurrActiveUiHandler;

    private int mResetDuration = DEFAULT_RESET_DURATION;//reset duration
    private float mMinTouchDistance = 0;
    private float mScrollThresholdRatio = 1f;
    private float mDampingFactor = 0.85f;
    private Interpolator mResetInterpolator = new LinearInterpolator();

    private int mScrollThreshold;
    private PointF mActDownPoint = new PointF();
    private PointF mActMovePoint = new PointF();
    private int mState;

    private ValueAnimator mResetAnimator;
    private OnReleaseListener mOnRefreshCallback;

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
        addView(mViewPager, new ViewGroup.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.MATCH_PARENT));

        mLeftHolder = new FrameLayout(context);
        addView(mLeftHolder, new ViewGroup.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.MATCH_PARENT));

        mRightHolder = new FrameLayout(context);
        addView(mRightHolder, new ViewGroup.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.MATCH_PARENT));
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
        mLeftHolder.layout(-mLeftHolder.getMeasuredWidth(), paddingTop, 0, height - paddingBottom);
        mRightHolder.layout(width, paddingTop, width - paddingRight + mRightHolder.getMeasuredWidth(), height - paddingBottom);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        switch (ev.getAction() & MotionEvent.ACTION_MASK) {
            case MotionEvent.ACTION_DOWN:
                mActDownPoint.set(ev.getX(), ev.getY());
                LRUtils.cancelAnimator(mResetAnimator);
                break;
            case MotionEvent.ACTION_MOVE:
                mActMovePoint.set(ev.getX(), ev.getY());
                //如果是最后一个，拦截向左滑动事件
                if (isLastItem() && mActMovePoint.x - mActDownPoint.x < -mMinTouchDistance) {
                    mState = STATE_DRAG_LEFT;
                    LRUtils.sendOnBegin(mRightUIHandler[0], this);
                    mCurrActiveUiHandler = mRightUIHandler[0];
                    return true;
                }
                //如果是第一个， 拦截向右滑动事件
                else if (isFirstItem() && mActMovePoint.x - mActDownPoint.x > mMinTouchDistance) {
                    mState = STATE_DRAG_RIGHT;
                    LRUtils.sendOnBegin(mLeftUIHandler[0], this);
                    mCurrActiveUiHandler = mLeftUIHandler[0];
                    return true;
                }
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
                mActMovePoint.set(event.getX(), event.getY());
                int offset = (int) (mActMovePoint.x - mActDownPoint.x);
                performScroll(offset);
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                resetOffset();
                performRelease();
                break;
        }
        return super.onTouchEvent(event);
    }

    private void performScroll(int offset) {
        if (mState == STATE_DRAG_LEFT) {
            scrollTo(LRUtils.computeScrollDistance(offset, mDampingFactor), 0);
        } else if (mState == STATE_DRAG_RIGHT) {
            scrollTo(-LRUtils.computeScrollDistance(offset, mDampingFactor), 0);
        }
        LRUtils.sendOnPull(mCurrActiveUiHandler, this);
    }

    private void performRelease() {
        LRUtils.sendOnRelease(mCurrActiveUiHandler, this);

        if (mOnRefreshCallback != null) {
            mOnRefreshCallback.onRelease(isOverThreshold(),
                    mState == STATE_DRAG_LEFT ? OnReleaseListener.RIGHT : OnReleaseListener.LEFT, OnReleaseListener.ON_RELEASE);
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        if (mResetAnimator != null) {
            LRUtils.cancelAnimator(mResetAnimator);
            mResetAnimator.removeAllUpdateListeners();
            mResetAnimator.removeAllListeners();
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
            mResetAnimator.addListener(this);
            mResetAnimator.setInterpolator(mResetInterpolator);
            mResetAnimator.setDuration(mResetDuration);
        } else {
            mResetAnimator.setIntValues(getScrollX(), 0);
        }
        mResetAnimator.start();
    }

    @Override
    public void onAnimationUpdate(ValueAnimator animation) {
        scrollTo((int) animation.getAnimatedValue(), 0);
    }

    @Override
    public void onAnimationStart(Animator animation) {

    }

    @Override
    public void onAnimationEnd(Animator animation) {
        LRUtils.sendOnEnd(mCurrActiveUiHandler, this);
        if (mOnRefreshCallback != null) {
            mOnRefreshCallback.onRelease(isOverThreshold(),
                    mState == STATE_DRAG_LEFT ? OnReleaseListener.RIGHT : OnReleaseListener.LEFT, OnReleaseListener.ON_END);
        }
        mCurrActiveUiHandler = null;
    }

    @Override
    public void onAnimationCancel(Animator animation) {
        LRUtils.sendOnEnd(mCurrActiveUiHandler, this);
        if (mOnRefreshCallback != null) {
            mOnRefreshCallback.onRelease(isOverThreshold(),
                    mState == STATE_DRAG_LEFT ? OnReleaseListener.RIGHT : OnReleaseListener.LEFT, OnReleaseListener.ON_END);
        }
        mCurrActiveUiHandler = null;
    }

    @Override
    public void onAnimationRepeat(Animator animation) {

    }

    //Setters and Getters

    public LRPtrViewPager setAdapter(PagerAdapter adapter) {
        mViewPager.setAdapter(adapter);
        return this;
    }

    public LRPtrViewPager setLeftView(View view) {
        if (view == null) {
            throw new IllegalArgumentException("LRPtrViewPager cannot accept a null object as left view");
        }
        LRUtils.setUIHandler(mLeftHolder, view, mLeftUIHandler);
        return this;
    }

    public LRPtrViewPager setRightView(View view) {
        if (view == null) {
            throw new IllegalArgumentException("LRPtrViewPager cannot accept a null object as right view");
        }
        LRUtils.setUIHandler(mRightHolder, view, mRightUIHandler);
        return this;
    }

    /**
     * 设置拖拽的阻尼系数(0f - 1f)
     * 建议保持在(0.6f - 1f)，越接近1f，阻力越小
     *
     * @param dampingFactor
     */
    public LRPtrViewPager setDampingFactor(@FloatRange(from = 0f, to = 1f) float dampingFactor) {
        mDampingFactor = dampingFactor;
        return this;
    }

    /**
     * 设置释放触发事件，需要拖动的距离
     *
     * @param scrollThresholdRatio 相对左边或者和右边UIHandler的宽度的比例
     * @return
     */
    public LRPtrViewPager setScrollThresholdRatio(float scrollThresholdRatio) {
        mScrollThresholdRatio = scrollThresholdRatio;
        return this;
    }

    /**
     * 设置释放触发事件，需要拖动的距离
     * 调用这个方法后，{@link #setScrollThresholdRatio(float)}将会无效
     * 直到通过这个方法再次把{@link #mScrollThreshold}设置为0
     *
     * @param scrollThreshold 需要拖动的实际距离
     * @return
     */
    public LRPtrViewPager setScrollThreshold(int scrollThreshold) {
        mScrollThreshold = scrollThreshold;
        return this;
    }

    public LRPtrViewPager addOnPageChangeListener(ViewPager.OnPageChangeListener listener) {
        mViewPager.addOnPageChangeListener(listener);
        return this;
    }

    public LRPtrViewPager setOnReleaseListener(OnReleaseListener onReleaseListener) {
        mOnRefreshCallback = onReleaseListener;
        return this;
    }

    public int getCount() {
        PagerAdapter adapter = mViewPager.getAdapter();
        return adapter == null ? 0 : adapter.getCount();
    }

    public boolean isOverThreshold() {
        int distanceX = Math.abs(getScrollX());
        if (mScrollThreshold != 0) {
            return distanceX > mScrollThreshold;
        }

        int tempThreshold = 0;
        if (mCurrActiveUiHandler != null) {
            tempThreshold = ((View) mCurrActiveUiHandler).getWidth();
        }

        return distanceX > tempThreshold;
    }

    /**
     * @return 是否是第一项
     */
    public boolean isFirstItem() {
        return mViewPager.getCurrentItem() == 0;
    }

    /**
     * @return 是否是最后一项
     */
    public boolean isLastItem() {
        return mViewPager.getCurrentItem() == getCount() - 1;
    }

    public void notifyDataSetChanged() {
        PagerAdapter adapter = mViewPager.getAdapter();
        if (adapter != null) {
            adapter.notifyDataSetChanged();
        }
    }

    public interface OnReleaseListener {

        int ON_RELEASE = 1;
        int ON_END = 2;

        int LEFT = 10;
        int RIGHT = 11;

        void onRelease(boolean overThreshold, int orientation, int when);
    }
}
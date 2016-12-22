package xiaoys.me.lrviewpager;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.PointF;
import android.os.Build;
import android.support.annotation.IntDef;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.animation.Interpolator;
import android.view.animation.LinearInterpolator;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.ArrayList;

/**
 * Created by xiaoys on 2016/6/15.
 */
public class DragViewPager extends ViewGroup implements ValueAnimator.AnimatorUpdateListener, ValueAnimator.AnimatorListener {

    public static final int NONE = 0;
    public static final int LEFT = 1;
    public static final int RIGHT = 2;

    private static final int DEFAULT_RESET_DURATION = 300;

    @IntDef({LEFT, RIGHT, NONE})
    @Retention(RetentionPolicy.SOURCE)
    public @interface Which {

    }

    private ViewPager mViewPager;
    private View mLeftHolder;
    private View mRightHolder;

    private PagerAdapter mPagerAdapter;

    private ArrayList<OnReleaseListener> mOnReleaseListeners;
    private ArrayList<IUIHandler> mUIHandlers;

    private int mResetDuration = DEFAULT_RESET_DURATION;//reset duration
    private float mMinTouchDistance = 0;
    private int mScrollThreshold;
    private float mScrollThresholdRatio = 1f;
    private float mDampingFactor = 0.85f;
    private Interpolator mResetInterpolator;

    private PointF mActDownPoint = new PointF();
    private PointF mActMoveStartPoint = new PointF();
    private PointF mActMovingPoint = new PointF();
    private int mWhich;
    private boolean mIsOverThresholdWhenRelease;
    private boolean mIsCancelReset;

    private ValueAnimator mResetAnimator;

    public DragViewPager(Context context) {
        this(context, null);
    }

    public DragViewPager(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public DragViewPager(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs, defStyleAttr);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public DragViewPager(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(context, attrs, defStyleAttr);
    }

    private void init(Context context, AttributeSet attrs, int defStyleAttr) {
        mMinTouchDistance = ViewConfiguration.get(context).getScaledTouchSlop();
        getAttrs(context, attrs);
    }

    private void getAttrs(Context context, AttributeSet attrs) {
        TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.DragViewPager);
        if (ta != null) {
            mResetDuration = ta.getInt(R.styleable.DragViewPager_resetDuration, DEFAULT_RESET_DURATION);
            mDampingFactor = ta.getFloat(R.styleable.DragViewPager_dampingFactor, 0.85f);
            mScrollThreshold = (int) ta.getDimension(R.styleable.DragViewPager_scrollThreshold, 0);
            mScrollThresholdRatio = ta.getFloat(R.styleable.DragViewPager_scrollThresholdRatio, 1);
            ta.recycle();
        }
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

        LRUtils.layout(mViewPager, paddingLeft, paddingTop, width - paddingRight, height - paddingBottom);

        if (mLeftHolder != null) {
            LRUtils.layout(mLeftHolder, -mLeftHolder.getMeasuredWidth(), paddingTop, 0, height - paddingBottom);
        }

        if (mRightHolder != null) {
            LRUtils.layout(mRightHolder, width, paddingTop,
                    width - paddingRight + mRightHolder.getMeasuredWidth(), height - paddingBottom);
        }
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        switch (ev.getAction() & MotionEvent.ACTION_MASK) {
            case MotionEvent.ACTION_DOWN:
                LRUtils.cancelAnimator(mResetAnimator);
                mActDownPoint.set(ev.getX(), ev.getY());
                break;
            case MotionEvent.ACTION_MOVE:
                if (mWhich != NONE) {
                    mActMoveStartPoint.set(ev.getX() - LRUtils.computeTouchDistance(getScrollX(), mDampingFactor), ev.getY());
                    return true;
                }
                mActMoveStartPoint.set(ev.getX(), ev.getY());
                //如果是最后一个，拦截向左滑动事件
                if (isLastItem() && mActMoveStartPoint.x - mActDownPoint.x < -mMinTouchDistance) {
                    mWhich = RIGHT;
                    LRUtils.sendUIHandlerOnBegin(mUIHandlers, mWhich, this);
                    return true;
                }
                //如果是第一个， 拦截向右滑动事件
                else if (isFirstItem() && mActMoveStartPoint.x - mActDownPoint.x > mMinTouchDistance) {
                    mWhich = LEFT;
                    LRUtils.sendUIHandlerOnBegin(mUIHandlers, mWhich, this);
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
            case MotionEvent.ACTION_DOWN:
                return true;
            case MotionEvent.ACTION_MOVE:
                mActMovingPoint.set(event.getX(), event.getY());
                performScroll();
                return true;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                performRelease();
                resetOffset();
                return true;
        }
        return super.onTouchEvent(event);
    }

    private void performScroll() {
        float touchDistance = mActMovingPoint.x - mActMoveStartPoint.x;
        scrollTo(LRUtils.computeScrollDistance(touchDistance, mDampingFactor, mWhich), 0);
        LRUtils.sendUIHandlerOnPull(mUIHandlers, mWhich, this);
    }

    private void performRelease() {
        LRUtils.sendUIHandlerOnRelease(mUIHandlers, mWhich, this);

        mIsOverThresholdWhenRelease = isOverThreshold();
        LRUtils.sendOnRelease(mOnReleaseListeners, mIsOverThresholdWhenRelease, mWhich, OnReleaseListener.ON_RELEASE);
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
        if (getScrollX() == 0) {
            mWhich = NONE;
            return;
        }

        if (mResetAnimator != null && mResetAnimator.isRunning()) {
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
        if (mIsCancelReset) {
            mIsCancelReset = false;
            return;
        }
        LRUtils.sendUIHandlerOnEnd(mUIHandlers, mWhich, this);
        LRUtils.sendOnRelease(mOnReleaseListeners, mIsOverThresholdWhenRelease, mWhich, OnReleaseListener.ON_END);
        mWhich = NONE;
    }

    @Override
    public void onAnimationCancel(Animator animation) {
        mIsCancelReset = true;
    }

    @Override
    public void onAnimationRepeat(Animator animation) {

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
        switch (mWhich) {
            case LEFT:
                tempThreshold = mLeftHolder.getWidth();
                break;
            case RIGHT:
                tempThreshold = mRightHolder.getWidth();
                break;
        }

        return distanceX > tempThreshold;
    }

    public boolean isFirstItem() {
        return mViewPager.getCurrentItem() == 0;
    }

    public boolean isLastItem() {
        return mViewPager.getCurrentItem() == getCount() - 1;
    }

    public void notifyDataSetChanged() {
        PagerAdapter adapter = mViewPager.getAdapter();
        if (adapter != null) {
            adapter.notifyDataSetChanged();
        }
    }

    public void initialize() {
        if (mViewPager == null) {
            mViewPager = LRUtils.providerDefaultVP(getContext());
        }
        addView(mViewPager, new ViewGroup.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));

        if (mLeftHolder != null) {
            addView(mLeftHolder, new ViewGroup.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.MATCH_PARENT));
        }

        if (mRightHolder != null) {
            addView(mRightHolder, new ViewGroup.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.MATCH_PARENT));
        }

        if (mPagerAdapter == null) {
            throw new IllegalStateException("You might forget to set the adapter for LRPtrViewPager!");
        }
        mViewPager.setAdapter(mPagerAdapter);

        if (mResetInterpolator == null) {
            mResetInterpolator = new LinearInterpolator();
        }
    }

    //Setters
    public DragViewPager setViewPager(ViewPager viewPager) {
        mViewPager = viewPager;
        return this;
    }

    public DragViewPager setAdapter(PagerAdapter adapter) {
        mPagerAdapter = adapter;
        return this;
    }

    public DragViewPager setLeftView(View view) {
        mRightHolder = view;
        return this;
    }

    public DragViewPager setRightView(View view) {
        mLeftHolder = view;
        return this;
    }

    /**
     * 设置拖拽的阻尼系数(0f - 1f)
     * 建议保持在(0.6f - 1f)，越接近1f，阻力越小
     *
     * @param dampingFactor
     */
    public DragViewPager setDampingFactor(float dampingFactor) {
        mDampingFactor = dampingFactor;
        return this;
    }

    public DragViewPager setResetInterpolator(Interpolator interpolator) {
        mResetInterpolator = interpolator;
        return this;
    }

    /**
     * 设置释放触发事件，需要拖动的距离
     *
     * @param scrollThresholdRatio 相对左边或者和右边UIHandler的宽度的比例
     * @return
     */
    public DragViewPager setScrollThresholdRatio(float scrollThresholdRatio) {
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
    public DragViewPager setScrollThreshold(int scrollThreshold) {
        mScrollThreshold = scrollThreshold;
        return this;
    }

    public DragViewPager addOnPageChangeListener(ViewPager.OnPageChangeListener listener) {
        mViewPager.addOnPageChangeListener(listener);
        return this;
    }

    public DragViewPager addOnReleaseListener(OnReleaseListener onReleaseListener) {
        if (mOnReleaseListeners == null) {
            mOnReleaseListeners = new ArrayList<>();
        }
        mOnReleaseListeners.add(onReleaseListener);
        return this;
    }

    public DragViewPager addUIHandler(IUIHandler uiHandler) {
        if (mUIHandlers == null) {
            mUIHandlers = new ArrayList<>();
        }
        mUIHandlers.add(uiHandler);
        return this;
    }

    public interface OnReleaseListener {

        int ON_RELEASE = 1;
        int ON_END = 2;

        @IntDef({ON_RELEASE, ON_END})
        @Retention(RetentionPolicy.SOURCE)
        @interface When {

        }

        void onRelease(boolean overThreshold, @Which int which, @When int when);
    }

    public interface IUIHandler {

        void onBegin(DragViewPager viewPager, @Which int which);

        void onPull(DragViewPager viewPager, @Which int which);

        void onRelease(DragViewPager viewPager, @Which int which);

        void onEnd(DragViewPager viewPager, @Which int which);
    }

    public abstract class SimpleUIHandler implements IUIHandler {
        @Override
        public void onBegin(DragViewPager viewPager, @Which int which) {

        }

        @Override
        public void onPull(DragViewPager viewPager, @Which int which) {

        }

        @Override
        public void onRelease(DragViewPager viewPager, @Which int which) {

        }

        @Override
        public void onEnd(DragViewPager viewPager, @Which int which) {

        }
    }
}
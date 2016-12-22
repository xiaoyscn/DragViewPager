package cn.xiaoys.dragviewpager.sample;

import android.support.annotation.LayoutRes;
import android.support.v4.view.PagerAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.LinkedList;
import java.util.List;

/**
 * Created by xiaoys on 2016/9/8.
 */
public abstract class RecyclerPageAdapter<M> extends PagerAdapter {

    public static final String TAG = "RecyclerPageAdapter";
    public static final boolean DEBUG = true;

    private List<M> mPageDataList;
    private LinkedList<ViewHolder<M>> mRecycledViewHolders = new LinkedList<>();

    public RecyclerPageAdapter(List<M> pageDataList) {
        mPageDataList = pageDataList;
    }

    @Override
    public int getCount() {
        return mPageDataList == null ? 0 : mPageDataList.size();
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view == object;
    }

    public M getItem(int position) {
        return mPageDataList == null || mPageDataList.isEmpty() ? null : mPageDataList.get(position);
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        ViewHolder<M> nextViewHolder = null;
        if (mRecycledViewHolders.size() > 1) {
            nextViewHolder = mRecycledViewHolders.removeFirst();
        } else {
            nextViewHolder = onCreateViewHolder(container, 0);
            if (nextViewHolder == null) {
                throw new IllegalStateException("RecyclerPageAdapter cannot receive null ViewHolder");
            }
            nextViewHolder.mItemView.setTag(nextViewHolder);
        }
        container.addView(nextViewHolder.mItemView);
        nextViewHolder.setData(getItem(position));
        return nextViewHolder.mItemView;
    }

    @SuppressWarnings("unchecked")
    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        View child = (View) object;
        ViewHolder<M> viewHolder = (ViewHolder<M>) child.getTag();
        container.removeView(child);
        viewHolder.onRecycler();
        mRecycledViewHolders.add(viewHolder);
    }

    /**
     * Create ViewHolder for RecyclerPageAdapter if need
     *
     * @param parent the parent of itemView
     * @param type   the type of itemView(not support by current version)
     * @return ViewHolder
     */
    protected abstract ViewHolder<M> onCreateViewHolder(ViewGroup parent, int type);

    public abstract static class ViewHolder<M> implements View.OnAttachStateChangeListener{

        public View mItemView;

        public ViewHolder(ViewGroup parent, @LayoutRes int layoutRes) {
            mItemView = LayoutInflater.from(parent.getContext()).inflate(layoutRes, parent, false);
            mItemView.addOnAttachStateChangeListener(this);
        }

        protected abstract void setData(M data);

        protected void onRecycler() {

        }

        @Override
        public void onViewAttachedToWindow(View v) {

        }

        @Override
        public void onViewDetachedFromWindow(View v) {

        }
    }
}

package xiaoys.me.lrviewpager;

/**
 * Created by xiaoys on 2016/10/8.
 */

public interface IUIHandler {

    void onBegin(LRPtrViewPager viewPager);

    void onPull(LRPtrViewPager viewPager);

    void onRelease(LRPtrViewPager viewPager);

    void onEnd(LRPtrViewPager viewPager);
}

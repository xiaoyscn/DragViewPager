package xiaoys.me.lrviewpager;

import android.content.Context;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.util.AttributeSet;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * Created by xiaoys on 2016/10/9.
 */

public class DefaultUIHandler extends FrameLayout implements IUIHandler {

    private TextView mTextContent;
    private ImageView mImageIcon;

    private int mLeftIcon = R.mipmap.ic_arrow_circle_left;
    private int mRightIcon = R.mipmap.ic_arrow_circle_right;
    private String mDragText;
    private String mReleaseText;

    public DefaultUIHandler(Context context) {
        this(context, null);
    }

    public DefaultUIHandler(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public DefaultUIHandler(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs, defStyleAttr);
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public DefaultUIHandler(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(context, attrs, defStyleAttr);
    }

    private void init(Context context, AttributeSet attrs, int defStyleAttr) {
        View.inflate(context, R.layout.default_ui_handler, this);
        mTextContent = (TextView) findViewById(R.id.tv_content);
        mImageIcon = (ImageView) findViewById(R.id.iv_icon);
    }

    public void setDragText(String dragText){
        mDragText = dragText;
    }

    public void setReleaseText(String releaseText){
        mReleaseText = releaseText;
    }

    private void setText(@NonNull String text){
        StringBuilder origin = new StringBuilder(text);
        StringBuilder format = new StringBuilder();
        int textLength = origin.length();
        for (int i = 0; i < textLength; i++){
            format.append(origin.charAt(i));
            format.append("\n");
        }
        mTextContent.setText(format.toString());
    }

    @Override
    public void onBegin(LRPtrViewPager viewPager) {

    }

    @Override
    public void onPull(LRPtrViewPager viewPager) {

    }

    @Override
    public void onRelease(LRPtrViewPager viewPager) {

    }

    @Override
    public void onEnd(LRPtrViewPager viewPager) {

    }
}

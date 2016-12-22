package cn.xiaoys.dragviewpager;

import android.content.Context;
import android.support.annotation.NonNull;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * Created by xiaoys on 2016/10/9.
 */

public class DefaultUIHandler extends FrameLayout implements DragViewPager.IUIHandler {

    private TextView mTextContent;
    private ImageView mImageIcon;

    private int mLeftIcon = R.mipmap.ic_arrow_circle_left;
    private int mRightIcon = R.mipmap.ic_arrow_circle_right;
    private String mDragText = "";
    private String mReleaseText = "";

    public DefaultUIHandler(Context context) {
        super(context);
        init(context);
    }

    private void init(Context context) {
        View.inflate(context, R.layout.default_ui_handler, this);
        mTextContent = (TextView) findViewById(R.id.tv_content);
        mImageIcon = (ImageView) findViewById(R.id.iv_icon);
    }

    public void setDragText(String dragText) {
        mDragText = dragText;
    }

    public void setReleaseText(String releaseText) {
        mReleaseText = releaseText;
    }

    private void setText(@NonNull String text) {
        StringBuilder origin = new StringBuilder(text);
        StringBuilder format = new StringBuilder();
        int textLength = origin.length();
        for (int i = 0; i < textLength; i++) {
            format.append(origin.charAt(i));
            format.append("\n");
        }
        mTextContent.setText(format.toString());
    }

    @Override
    public void onBegin(DragViewPager viewPager, @DragViewPager.Which int which) {
        setText(mDragText);
    }

    @Override
    public void onPull(DragViewPager viewPager, @DragViewPager.Which int which) {
        if (viewPager.isOverThreshold()) {
            if (!mReleaseText.equals(mTextContent.getText().toString())) {
                setText(mReleaseText);
                mImageIcon.setImageResource(DragViewPager.LEFT == which ? mLeftIcon : mRightIcon);
            }
        } else {
            if (!mDragText.equals(mTextContent.getText().toString())) {
                setText(mDragText);
                mImageIcon.setImageResource(DragViewPager.LEFT == which ? mRightIcon : mLeftIcon);
            }
        }
    }

    @Override
    public void onRelease(DragViewPager viewPager, @DragViewPager.Which int which) {

    }

    @Override
    public void onEnd(DragViewPager viewPager, @DragViewPager.Which int which) {

    }
}

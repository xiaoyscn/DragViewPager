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

    public static final int ORIENTATION_LEFT = 0;
    public static final int ORIENTATION_RIGHT = 1;

    private TextView mTextContent;
    private ImageView mImageIcon;

    private int mLeftIcon = R.mipmap.ic_arrow_circle_left;
    private int mRightIcon = R.mipmap.ic_arrow_circle_right;
    private String mDragText = "";
    private String mReleaseText = "";
    private int mOrientation;

    public DefaultUIHandler(Context context, int orientation) {
        this(context, null);
        mOrientation = orientation;
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

    public void setOrientation(int orientation){
        mOrientation = orientation;
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
        setText(mDragText);
    }

    @Override
    public void onPull(LRPtrViewPager viewPager) {
        if (viewPager.isOverSnap()){
            if (!mReleaseText.equals(mTextContent.getText().toString())){
                setText(mReleaseText);
                if (mOrientation == ORIENTATION_LEFT){
                    mImageIcon.setImageResource(mLeftIcon);
                }else{
                    mImageIcon.setImageResource(mRightIcon);
                }
            }
        }else{
            if (!mDragText.equals(mTextContent.getText().toString())){
                setText(mDragText);
                if (mOrientation == ORIENTATION_LEFT){
                    mImageIcon.setImageResource(mRightIcon);
                }else{
                    mImageIcon.setImageResource(mLeftIcon);
                }
            }
        }
    }

    @Override
    public void onRelease(LRPtrViewPager viewPager) {

    }

    @Override
    public void onEnd(LRPtrViewPager viewPager) {

    }
}

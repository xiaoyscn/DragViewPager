package xiaoys.me.sample;

import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import xiaoys.me.lrviewpager.DefaultUIHandler;
import xiaoys.me.lrviewpager.DragViewPager;

public class MainActivity extends AppCompatActivity {

    private DragViewPager mViewPager;
    private TextView mTvState;
    private Button mBtnReset;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mViewPager = (DragViewPager) findViewById(R.id.lr_ptr_view_pager);
        mTvState = (TextView) findViewById(R.id.tv_state);
        mBtnReset = (Button) findViewById(R.id.btn_reset);

        final List<PageEntity> list = new ArrayList<>();
        list.add(new PageEntity(0, Color.parseColor("#ff0000")));
        list.add(new PageEntity(1, Color.parseColor("#ffff00")));
        list.add(new PageEntity(2, Color.parseColor("#00ff00")));
        list.add(new PageEntity(3, Color.parseColor("#00ffff")));

        final DefaultUIHandler left = new DefaultUIHandler(this);
        left.setDragText("继续拖拽");
        left.setReleaseText("释放拖拽");

        final DefaultUIHandler right = new DefaultUIHandler(this);
        right.setDragText("继续拖拽");
        right.setReleaseText("释放拖拽");


        mViewPager
                .setLeftView(left)
                .setRightView(right)
                .addUIHandler(left)
                .addUIHandler(right)
                .setAdapter(new LRAdapter(list))
                .addOnReleaseListener(new DragViewPager.OnReleaseListener() {
                    @Override
                    public void onRelease(boolean overThreshold, int orientation, int when) {
                        if (overThreshold) {
                            String orientationMsg = orientation == DragViewPager.LEFT ?
                                    "左边" : "右边";
                            String whenMsg = when == DragViewPager.OnReleaseListener.ON_RELEASE ?
                                    "释放" : "结束";
                            mTvState.append(orientationMsg + whenMsg + "\n");
                        }
                    }
                })
                .initialize();

        Log.e("sample", "what the fuck log");
        mBtnReset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mTvState.setText("");
            }
        });
    }

    public static class LRAdapter extends RecyclerPageAdapter<PageEntity> {

        LRAdapter(List<PageEntity> pageDataList) {
            super(pageDataList);
        }

        @Override
        protected ViewHolder<PageEntity> onCreateViewHolder(ViewGroup parent, int type) {
            return new LRPageViewHolder(parent);
        }
    }


    public static class LRPageViewHolder extends RecyclerPageAdapter.ViewHolder<PageEntity> {

        TextView tv;

        LRPageViewHolder(ViewGroup parent) {
            super(parent, R.layout.view_holder_lr_page);
            tv = (TextView) mItemView.findViewById(R.id.tv_name);
        }

        @Override
        protected void setData(PageEntity data) {
            tv.setText(String.valueOf(data.getName()));
            mItemView.setBackgroundColor(data.getBgColor());
        }
    }
}

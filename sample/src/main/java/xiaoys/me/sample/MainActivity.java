package xiaoys.me.sample;

import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import xiaoys.me.lrviewpager.DefaultUIHandler;
import xiaoys.me.lrviewpager.LRPtrViewPager;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        LRPtrViewPager viewPager = (LRPtrViewPager) findViewById(R.id.lr_ptr_view_pager);

        List<PageEntity> list = new ArrayList<>();
        list.add(new PageEntity(0, Color.parseColor("#ff0000")));
        list.add(new PageEntity(1, Color.parseColor("#ffff00")));
        list.add(new PageEntity(2, Color.parseColor("#00ff00")));
        list.add(new PageEntity(3, Color.parseColor("#00ffff")));
        viewPager.setAdapter(new LRAdapter(list));
        DefaultUIHandler left = new DefaultUIHandler(this, DefaultUIHandler.ORIENTATION_LEFT);
        left.setDragText("继续拖拽");
        left.setReleaseText("释放拖拽");
        viewPager.setLeftView(left);
        DefaultUIHandler right = new DefaultUIHandler(this, DefaultUIHandler.ORIENTATION_RIGHT);
        right.setDragText("继续拖拽");
        right.setReleaseText("释放拖拽");
        viewPager.setRightView(right);

        viewPager.setOnReleaseListener(new LRPtrViewPager.OnReleaseListener() {
            @Override
            public void onRelease(boolean isOverSnap) {
                if (isOverSnap){
                    Toast.makeText(MainActivity.this, "事件触发", Toast.LENGTH_SHORT).show();
                }else{
                    Toast.makeText(MainActivity.this, "事件未触发", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    public static class LRAdapter extends RecyclerPageAdapter<PageEntity> {

        public LRAdapter(List<PageEntity> pageDataList) {
            super(pageDataList);
        }

        @Override
        protected ViewHolder<PageEntity> onCreateViewHolder(ViewGroup parent, int type) {
            return new LRPageViewHolder(parent);
        }
    }


    public static class LRPageViewHolder extends RecyclerPageAdapter.ViewHolder<PageEntity> {

        TextView tv;

        public LRPageViewHolder(ViewGroup parent) {
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

package xiaoys.me.sample;

import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import xiaoys.me.lrviewpager.LRPtrViewPager;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        LRPtrViewPager viewPager = (LRPtrViewPager) findViewById(R.id.lr_ptr_view_pager);

        List<PageEntity> list = new ArrayList<>();
        list.add(new PageEntity(0, Color.parseColor("#ff0000")));
        list.add(new PageEntity(0, Color.parseColor("#ffff00")));
        list.add(new PageEntity(0, Color.parseColor("#00ff00")));
        list.add(new PageEntity(0, Color.parseColor("#00ffff")));
        viewPager.setAdapter(new LRAdapter(list));
    }

    public static class LRAdapter extends RecyclerPageAdapter<PageEntity>{

        public LRAdapter(List<PageEntity> pageDataList) {
            super(pageDataList);
        }

        @Override
        protected ViewHolder<PageEntity> onCreateViewHolder(ViewGroup parent, int type) {
            return new LRPageViewHolder(parent);
        }
    }


    public static class LRPageViewHolder extends RecyclerPageAdapter.ViewHolder<PageEntity>{

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

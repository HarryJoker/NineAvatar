package com.harry.joker.holder.avatar;

import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.harry.joker.nine.avatar.JokerNineAvatar;
import com.harry.joker.nine.avatar.layout.WechatLayoutManager;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class MuilteAvatarActivity extends AppCompatActivity {

    private RecyclerView mRecyclerView;

    private List<List<String>> mdatas = new ArrayList<>();

    private String[] IMG_URL_ARR = {
            "http://img.hb.aicdn.com/eca438704a81dd1fa83347cb8ec1a49ec16d2802c846-laesx2_fw658",
            "http://img.hb.aicdn.com/729970b85e6f56b0d029dcc30be04b484e6cf82d18df2-XwtPUZ_fw658",
            "http://img.hb.aicdn.com/85579fa12b182a3abee62bd3fceae0047767857fe6d4-99Wtzp_fw658",
            "http://img.hb.aicdn.com/2814e43d98ed41e8b3393b0ff8f08f98398d1f6e28a9b-xfGDIC_fw658",
            "http://img.hb.aicdn.com/a1f189d4a420ef1927317ebfacc2ae055ff9f212148fb-iEyFWS_fw658",
            "http://img.hb.aicdn.com/69b52afdca0ae780ee44c6f14a371eee68ece4ec8a8ce-4vaO0k_fw658",
            "http://img.hb.aicdn.com/9925b5f679964d769c91ad407e46a4ae9d47be8155e9a-seH7yY_fw658",
            "http://img.hb.aicdn.com/e22ee5730f152c236c69e2242b9d9114852be2bd8629-EKEnFD_fw658",
            "http://img.hb.aicdn.com/73f2fbeb01cd3fcb2b4dccbbb7973aa1a82c420b21079-5yj6fx_fw658",
            "https://pic4.zhimg.com/02685b7a5f2d8cbf74e1fd1ae61d563b_xll.jpg",
            "https://pic4.zhimg.com/fc04224598878080115ba387846eabc3_xll.jpg",
            "https://pic3.zhimg.com/d1750bd47b514ad62af9497bbe5bb17e_xll.jpg",
            "https://pic4.zhimg.com/da52c865cb6a472c3624a78490d9a3b7_xll.jpg",
            "https://pic3.zhimg.com/0c149770fc2e16f4a89e6fc479272946_xll.jpg",
            "https://pic1.zhimg.com/76903410e4831571e19a10f39717988c_xll.png",
            "https://pic3.zhimg.com/33c6cf59163b3f17ca0c091a5c0d9272_xll.jpg",
            "https://pic4.zhimg.com/52e093cbf96fd0d027136baf9b5cdcb3_xll.png",
            "https://pic3.zhimg.com/f6dc1c1cecd7ba8f4c61c7c31847773e_xll.jpg",


            //读不出图片的地址，验证placehold资源替换对应位置的头像
            "https://pic3.zhimg.com/0c149770fc2e16f4a89e6fc479272946_xll_____Can not load image by place image_____.jpg",
            "https://pic1.zhimg.com/76903410e4831571e19a10f39717988c_xll_____Can not load image by place image_____.png",
            "https://pic3.zhimg.com/33c6cf59163b3f17ca0c091a5c0d9272_xll_____Can not load image by place image_____.jpg",
            "https://pic4.zhimg.com/52e093cbf96fd0d027136baf9b5cdcb3_xll_____Can not load image by place image_____.png",
            "https://pic3.zhimg.com/f6dc1c1cecd7ba8f4c61c7c31847773e_xll_____Can not load image by place image_____.jpg",
    };

    private void initData() {
        for (int n = 0; n < 50; n++) {
            List<String> urls = new ArrayList<>();
            for (int m = 0; m < new Random().nextInt(9) + 1; m++) {
                urls.add(IMG_URL_ARR[new Random().nextInt(IMG_URL_ARR.length)]);
            }
            mdatas.add(urls);
        }
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_muilte_avatar);

        initData();

        mRecyclerView =  findViewById(R.id.rc_avatar);

        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        mRecyclerView.setAdapter(new AvatarAdapter());

    }

    class AvatarAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

        @NonNull
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            return new NineAvatarViewHolder(getLayoutInflater().inflate(R.layout.item_avatar, parent, false));
        }

        @Override
        public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
           ((NineAvatarViewHolder)holder).bindAvatar(mdatas.get(position), position);
        }

        @Override
        public long getItemId(int i) {
            return i;
        }

        @Override
        public int getItemCount() {
            return mdatas.size();
        }

    }

    class NineAvatarViewHolder extends RecyclerView.ViewHolder {

        ImageView avatar;
        TextView content;

        public NineAvatarViewHolder(@NonNull View itemView) {
            super(itemView);
            avatar = itemView.findViewById(R.id.avatar);
            content = itemView.findViewById(R.id.tv_content);
        }

        public void bindAvatar(List<String> urls, int position) {
            content.setText("position:" + position+  ", Size:" + (urls == null ? "0" : urls.size()));
            JokerNineAvatar.init(MuilteAvatarActivity.this)
                    .setUniqueTag(position)
                    .setImageWidth(120)
                    .setDividerWidth(5)
                    .setLayoutManager(new WechatLayoutManager())
                    .setUrls(urls.toArray(new String[]{}))
                    .setPlaceholder(R.mipmap.a)
                    .setImageView(avatar)
                    .build();
        }
    }
}

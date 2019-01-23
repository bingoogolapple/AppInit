package com.sankuai.erp.component.appinitmodule2;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;

import com.sankuai.waimai.router.Router;
import com.sankuai.waimai.router.annotation.RouterUri;

@RouterUri(path = "/module2")
public class Module2Activity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_module2);

        Fragment topFragment = Router.getService(Fragment.class, "fragment3");
        if (topFragment != null) {
            getSupportFragmentManager().beginTransaction().add(R.id.fl_top, topFragment).commit();
        }
        Fragment bottomFragment = Router.getService(Fragment.class, "fragment1");
        if (bottomFragment != null) {
            getSupportFragmentManager().beginTransaction().add(R.id.fl_bottom, bottomFragment).commit();
        }

        // 测试子进程初始化
//        new Handler().postDelayed(new Runnable() {
//            @Override
//            public void run() {
//                Toast.makeText(Module2Activity.this, "测试", Toast.LENGTH_SHORT).show();
//            }
//        }, 30000);
    }
}

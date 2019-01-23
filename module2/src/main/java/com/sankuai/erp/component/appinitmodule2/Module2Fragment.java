package com.sankuai.erp.component.appinitmodule2;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.sankuai.waimai.router.Router;
import com.sankuai.waimai.router.annotation.RouterService;

@RouterService(interfaces = Fragment.class, key = "fragment2")
public class Module2Fragment extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_module2, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        getView().findViewById(R.id.btn_go).setOnClickListener(v -> {
            Router.startUri(getContext(), "/module2");
            getActivity().finish();
        });
    }
}

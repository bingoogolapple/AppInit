package com.sankuai.erp.component.appinitmodule3

import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import com.sankuai.waimai.router.Router
import com.sankuai.waimai.router.annotation.RouterService

/**
 * 作者:王浩
 * 创建时间:2018/11/28
 * 描述:
 */
@RouterService(interfaces = [Fragment::class], key = ["fragment3"])
class Module3Fragment : Fragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_module3, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        getView()!!.findViewById<Button>(R.id.btn_go).setOnClickListener {
            Router.startUri(context, "/module3")
            activity!!.finish()
        }
    }
}
package com.xwray.groupie.example.paging

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.paging.PagedList
import com.xwray.groupie.Item
import com.xwray.groupie.ViewHolder
import com.xwray.groupie.paging.GroupAdapterPaging

class MainActivity : AppCompatActivity() {

    private val groupAdapter = GroupAdapterPaging<ViewHolder>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val viewModel = ViewModelProviders.of(this).get(MainViewModel::class.java)

        viewModel.pagedList.observe(this, Observer {
            groupAdapter.submitList(it as PagedList<Item<ViewHolder>>)
        })
    }
}

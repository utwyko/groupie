package com.xwray.groupie.example.paging

import androidx.lifecycle.ViewModel
import androidx.paging.LivePagedListBuilder
import androidx.paging.PagedList


class MainViewModel : ViewModel() {

    private val factory = ExampleDataSourceFactory()

    val pagedList = LivePagedListBuilder(factory, PagedList.Config.Builder().build()).build()
}
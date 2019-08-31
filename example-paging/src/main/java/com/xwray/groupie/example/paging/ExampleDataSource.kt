package com.xwray.groupie.example.paging

import androidx.paging.DataSource
import androidx.paging.PositionalDataSource
import com.xwray.groupie.kotlinandroidextensions.Item


private class ExampleDataSource : PositionalDataSource<Item>() {

    override fun loadRange(params: LoadRangeParams, callback: LoadRangeCallback<Item>) {
        callback.onResult(
                List(params.loadSize) {
                    ExampleItem()
                }
        )
    }

    override fun loadInitial(params: LoadInitialParams, callback: LoadInitialCallback<Item>) {
        callback.onResult(
                List(params.requestedLoadSize) {
                    ExampleItem()
                },
                10
        )
    }
}

class ExampleDataSourceFactory: DataSource.Factory<Int, Item>() {

    override fun create(): DataSource<Int, Item> {
        return ExampleDataSource()
    }
}
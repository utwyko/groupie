package com.xwray.groupie.kotlinandroidextensions

import android.view.View
import kotlinx.android.extensions.CacheImplementation
import kotlinx.android.extensions.ContainerOptions
import kotlinx.android.extensions.LayoutContainer

// Need to specify ContainerOptions in order for caching to work.
// See: https://youtrack.jetbrains.com/oauth?state=%2Fissue%2FKT-28617
@ContainerOptions(cache = CacheImplementation.HASH_MAP)
class ViewHolder(override val containerView: View) : com.xwray.groupie.ViewHolder(containerView),
        LayoutContainer
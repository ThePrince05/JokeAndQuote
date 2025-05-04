package com.project.jokeandquote.view

import android.os.Bundle
import androidx.annotation.LayoutRes
import androidx.fragment.app.Fragment
import com.google.android.material.transition.MaterialFadeThrough

open class FadeThroughFragment(@LayoutRes layoutId: Int) : Fragment(layoutId) {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // apply FadeThrough to all fragment swaps
         enterTransition = MaterialFadeThrough()

    }
}
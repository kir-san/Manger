package com.san.kir.ankofork.appcompat

import androidx.appcompat.widget.Toolbar
import com.san.kir.ankofork.AnkoInternals

@Suppress("unused")
var Toolbar.subtitleResource: Int
    @Deprecated(AnkoInternals.NO_GETTER, level = DeprecationLevel.ERROR) get() = AnkoInternals.noGetter()
    set(v) = setSubtitle(v)

var Toolbar.titleResource: Int
    @Deprecated(AnkoInternals.NO_GETTER, level = DeprecationLevel.ERROR) get() = AnkoInternals.noGetter()
    set(v) = setTitle(v)


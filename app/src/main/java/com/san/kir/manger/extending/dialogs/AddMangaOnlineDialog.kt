package com.san.kir.manger.extending.dialogs

import android.graphics.Color
import android.view.Gravity
import androidx.lifecycle.lifecycleScope
import com.san.kir.ankofork.Binder
import com.san.kir.ankofork.design.textInputEditText
import com.san.kir.ankofork.design.textInputLayout
import com.san.kir.ankofork.dialogs.alert
import com.san.kir.ankofork.dialogs.customView
import com.san.kir.ankofork.dip
import com.san.kir.ankofork.horizontalProgressBar
import com.san.kir.ankofork.matchParent
import com.san.kir.ankofork.negative
import com.san.kir.ankofork.padding
import com.san.kir.ankofork.positive
import com.san.kir.ankofork.sdk28.hintResource
import com.san.kir.ankofork.sdk28.onClick
import com.san.kir.ankofork.sdk28.textChangedListener
import com.san.kir.ankofork.sdk28.textColor
import com.san.kir.ankofork.sdk28.textView
import com.san.kir.ankofork.verticalLayout
import com.san.kir.ankofork.verticalPadding
import com.san.kir.manger.R
import com.san.kir.manger.components.parsing.ManageSites
import com.san.kir.manger.utils.extensions.BaseActivity
import com.san.kir.manger.utils.extensions.typeText
import com.san.kir.manger.utils.extensions.visibleOrGone
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class AddMangaOnlineDialog(ctx: BaseActivity) {
    init {
        with(ctx) {
            alert {
                val siteNames: List<String> = ManageSites.CATALOG_SITES.map { it.catalogName }
                val validate = Binder(siteNames.toString().removeSurrounding("[", "]"))
                val check = Binder(false)
                val isEnable = Binder(false)
                var url = ""

                titleResource = R.string.library_add_manga_title
                customView {
                    verticalLayout {
                        padding = dip(16)

                        horizontalProgressBar {
                            verticalPadding = dip(5)
                            isIndeterminate = true
                            visibleOrGone(check)
                        }.lparams(height = dip(15), width = matchParent)

                        textInputLayout {
                            textInputEditText {
                                hintResource = R.string.library_add_manga_hint
                                typeText()
                                textChangedListener {
                                    onTextChanged { text, _, _, _ ->
                                        text?.let {
                                            validate.item = when {
                                                text.isNotBlank() -> {
                                                    url = text.toString()
                                                    val temp = siteNames
                                                        .filter { it.contains(text) }
                                                        .toString()
                                                        .removeSurrounding("[", "]")
                                                    if (temp.isNotBlank()) {
                                                        isEnable.negative()
                                                        temp
                                                    } else {
                                                        isEnable.positive()
                                                        siteNames
                                                            .filter { text.contains(it) }
                                                            .toString()
                                                            .removeSurrounding("[", "]")
                                                    }
                                                }
                                                else -> {
                                                    isEnable.negative()
                                                    siteNames
                                                        .toString()
                                                        .removeSurrounding("[", "]")
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                        textView(validate) {
                            textColor = Color.RED
                        }.lparams {
                            gravity = Gravity.END
                        }

                        textView(R.string.library_add_manga_add_btn) {
                            isEnable.bind { isEnabled = it }
                            textSize = 17f
                            padding = dip(10)
                            onClick {
                                ctx.lifecycleScope.launch(Dispatchers.Default) {
                                    check.positive()
                                    ManageSites.getElementOnline(url)?.also {
                                        AddMangaDialog(ctx, it)
                                    } ?: run {
                                        validate.item = getString(R.string.library_add_manga_error)
                                    }
                                    check.negative()
                                }

                            }
                        }.lparams {
                            gravity = Gravity.END
                        }
                    }
                }
            }.show()
        }
    }
}

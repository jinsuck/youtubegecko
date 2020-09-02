package com.hyperisk.youtubegecko.ui.gecko_1.pierfranui.menu

import android.view.View
import androidx.annotation.DrawableRes

data class MenuItem @JvmOverloads constructor(val text: String, @DrawableRes val icon: Int? = null, val onClickListener: View.OnClickListener)
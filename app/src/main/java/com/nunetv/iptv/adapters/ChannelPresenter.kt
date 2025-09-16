package com.nunetv.iptv.adapters

import android.graphics.drawable.ColorDrawable
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.leanback.widget.ImageCardView
import androidx.leanback.widget.Presenter
import coil.ImageLoader
import coil.request.ImageRequest
import com.nunetv.iptv.R
import com.nunetv.iptv.model.Channel

class ChannelPresenter(private val imageLoader: ImageLoader) : Presenter() {

    override fun onCreateViewHolder(parent: ViewGroup): ViewHolder {
        val cardView = ImageCardView(parent.context).apply {
            isFocusable = true
            isFocusableInTouchMode = true
            setMainImageDimensions(CARD_WIDTH, CARD_HEIGHT)
            badgeImage = ContextCompat.getDrawable(context, R.drawable.ic_app_logo)
        }
        cardView.mainImage = ColorDrawable(ContextCompat.getColor(parent.context, R.color.primaryVariant))
        return ViewHolder(cardView)
    }

    override fun onBindViewHolder(viewHolder: ViewHolder, item: Any?) {
        val channel = item as? Channel ?: return
        val cardView = viewHolder.view as ImageCardView
        cardView.titleText = channel.name
        cardView.contentText = channel.group
        cardView.badgeImage = if (channel.isFavorite) {
            ContextCompat.getDrawable(cardView.context, R.drawable.ic_app_logo)
        } else null
        val request = ImageRequest.Builder(cardView.context)
            .data(channel.logo)
            .target(cardView::setMainImage)
            .error(ColorDrawable(ContextCompat.getColor(cardView.context, R.color.primaryVariant)))
            .fallback(ColorDrawable(ContextCompat.getColor(cardView.context, R.color.primaryVariant)))
            .build()
        imageLoader.enqueue(request)
    }

    override fun onUnbindViewHolder(viewHolder: ViewHolder) {
        val cardView = viewHolder.view as ImageCardView
        cardView.badgeImage = null
        cardView.mainImage = ColorDrawable(ContextCompat.getColor(cardView.context, R.color.primaryVariant))
    }

    companion object {
        private const val CARD_WIDTH = 320
        private const val CARD_HEIGHT = 180
    }
}

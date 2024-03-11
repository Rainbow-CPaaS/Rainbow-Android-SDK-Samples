package com.ale.rainbowsample.conversations

import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.ale.infra.contact.IRainbowContact
import com.ale.infra.contact.RainbowPresence
import com.ale.rainbowsample.R
import com.ale.rainbowsample.databinding.FavoriteAdapterItemBinding
import com.ale.rainbowx.rainbowadapter.RainbowAdapter
import com.ale.rainbowx.rainbowadapter.RainbowViewHolder

class FavoritesAdapter : ListAdapter<FavoriteUiState, RecyclerView.ViewHolder>(FavoriteDiffCallBack()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return FavoriteViewHolder(FavoriteAdapterItemBinding.inflate(LayoutInflater.from(parent.context), parent, false))
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        (holder as FavoriteViewHolder).bind(getItem(position))
    }

    override fun onViewAttachedToWindow(holder: RecyclerView.ViewHolder) {
        if (holder is FavoriteViewHolder)
            holder.addObserver()
    }

    override fun onViewDetachedFromWindow(holder: RecyclerView.ViewHolder) {
        if (holder is FavoriteViewHolder)
            holder.removeObserver()
    }

    internal class FavoriteViewHolder(private val binding: FavoriteAdapterItemBinding) : RainbowViewHolder<FavoriteUiState>(binding), IRainbowContact.IContactListener {

        private val uiHandler = Handler(Looper.getMainLooper())
        private var contact: IRainbowContact? = null

        override fun bind(data: FavoriteUiState, onClick: ((FavoriteUiState, Int) -> Unit)?, onLongClick: ((FavoriteUiState, Int) -> Boolean)?) {

            println("**** biiind")
            contact = data.favorite.contact

            data.favorite.contact?.let { contact ->
                binding.favoriteAvatar.displayContact(contact)
                displayPresence()
            }

            data.favorite.room?.let { room ->
                binding.favoriteAvatar.displayRoom(room)
            }
        }

        private fun displayPresence() {
            binding.favoriteAvatar.displayPresence(contact)
        }

        fun addObserver() {
            contact?.registerChangeListener(this)
        }

        fun removeObserver() {
            contact?.unregisterChangeListener(this)
        }


        override fun contactUpdated(updatedContact: IRainbowContact) {

        }

        override fun onPresenceChanged(contact: IRainbowContact, presence: RainbowPresence?) {
            uiHandler.post { displayPresence() }
        }
    }

    private class FavoriteDiffCallBack : DiffUtil.ItemCallback<FavoriteUiState>() {
        override fun areItemsTheSame(oldItem: FavoriteUiState, newItem: FavoriteUiState): Boolean {
            return oldItem.favorite.id == newItem.favorite.id
        }

        override fun areContentsTheSame(oldItem: FavoriteUiState, newItem: FavoriteUiState): Boolean {
            return oldItem.position == newItem.position
        }
    }
}
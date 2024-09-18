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
import com.ale.infra.manager.favorites.IRainbowFavorite
import com.ale.infra.manager.room.IRainbowRoom
import com.ale.infra.manager.room.Room
import com.ale.infra.manager.room.RoomListener
import com.ale.rainbowsample.databinding.FavoriteAdapterItemBinding
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

    internal class FavoriteViewHolder(private val binding: FavoriteAdapterItemBinding) : RainbowViewHolder<FavoriteUiState>(binding), IRainbowContact.IContactListener, RoomListener {

        private val uiHandler = Handler(Looper.getMainLooper())
        private lateinit var favorite: IRainbowFavorite

        override fun bind(data: FavoriteUiState, onClick: ((FavoriteUiState, Int) -> Unit)?, onLongClick: ((FavoriteUiState, Int) -> Boolean)?) {
            favorite = data.favorite
            updateLayout()
        }

        private fun updateLayout() {
            favorite.contact?.let { contact ->
                binding.favoriteAvatar.displayContact(contact)
                binding.favoriteAvatar.displayPresence(contact)
            }

            favorite.room?.let { room ->
                binding.favoriteAvatar.displayRoom(room)
            }
        }

        fun addObserver() {
            favorite.contact?.registerChangeListener(this)
            favorite.room?.registerChangeListener(this)
        }

        fun removeObserver() {
            favorite.contact?.unregisterChangeListener(this)
            favorite.room?.unregisterChangeListener(this)
        }


        override fun contactUpdated(updatedContact: IRainbowContact) {
            uiHandler.post { updateLayout() }
        }

        override fun onPresenceChanged(contact: IRainbowContact, presence: RainbowPresence?) {
            uiHandler.post { updateLayout() }
        }

        override fun roomUpdated(updatedRoom: Room) {
            uiHandler.post { updateLayout() }
        }
    }

    private class FavoriteDiffCallBack : DiffUtil.ItemCallback<FavoriteUiState>() {
        override fun areItemsTheSame(oldItem: FavoriteUiState, newItem: FavoriteUiState): Boolean {
            return oldItem.favorite.id == newItem.favorite.id
        }

        override fun areContentsTheSame(oldItem: FavoriteUiState, newItem: FavoriteUiState): Boolean {
            return oldItem.position == newItem.position && oldItem.name == newItem.name
        }
    }
}
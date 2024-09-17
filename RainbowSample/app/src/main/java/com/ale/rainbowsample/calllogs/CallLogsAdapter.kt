package com.ale.rainbowsample.calllogs

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.ale.infra.contact.IRainbowContact
import com.ale.infra.contact.RainbowPresence
import com.ale.infra.xmpp.packetextension.calllog.CallLog
import com.ale.rainbowsample.R
import com.ale.rainbowsample.databinding.CalllogAdapterItemBinding
import com.ale.rainbowsample.utils.presenceAsString
import com.ale.rainbowsample.utils.toRelativeTimeString

class CallLogsAdapter : androidx.recyclerview.widget.ListAdapter<CallLog, RecyclerView.ViewHolder>(CallLogsDiffCallback()) {

    override fun getItemViewType(position: Int): Int {
        return R.layout.calllog_adapter_item
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            R.layout.calllog_adapter_item -> CallLogsViewHolder(CalllogAdapterItemBinding.inflate(LayoutInflater.from(parent.context), parent, false))
            else -> throw IllegalStateException("Not handled")
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is CallLogsViewHolder -> holder.bind(getItem(position) as CallLog)
        }
    }

    override fun onViewAttachedToWindow(holder: RecyclerView.ViewHolder) {
        if (holder is CallLogsViewHolder)
            holder.addContactObserver()
    }

    override fun onViewDetachedFromWindow(holder: RecyclerView.ViewHolder) {
        if (holder is CallLogsViewHolder)
            holder.removeContactObserver()
    }

    internal inner class CallLogsViewHolder(private val binding: CalllogAdapterItemBinding) : RecyclerView.ViewHolder(binding.root), IRainbowContact.IContactListener {

        val context: Context
            get() = binding.root.context

        private lateinit var callLog: CallLog
        private val uiHandler = Handler(Looper.getMainLooper())

        fun bind(data: CallLog) {
            callLog = data
            updateLayout()
        }

        private fun updateLayout() {
            binding.title.text = callLog.contact.getDisplayName(context.getString(R.string.unknown))
            binding.avatar.displayContact(callLog.contact)
            binding.avatar.displayPresence(callLog.contact)
            binding.subtitle.text = callLog.contact.presenceAsString(context)
            binding.calllogDate.text = callLog.date.toRelativeTimeString()

            val iconRes = when {
                callLog.isMissed -> R.drawable.baseline_call_missed_24
                callLog.isOutgoing -> R.drawable.baseline_call_made_24
                else -> R.drawable.baseline_call_received_24
            }

            binding.calllogIcon.setImageResource(iconRes)
        }

        override fun contactUpdated(updatedContact: IRainbowContact) {
            uiHandler.post { updateLayout() }
        }

        override fun onPresenceChanged(contact: IRainbowContact, presence: RainbowPresence?) {
            uiHandler.post { updateLayout() }
        }

        fun addContactObserver() {
            callLog.contact.registerChangeListener(this)
        }

        fun removeContactObserver() {
            callLog.contact.unregisterChangeListener(this)
        }
    }


    private class CallLogsDiffCallback : DiffUtil.ItemCallback<CallLog>() {
        override fun areItemsTheSame(oldItem: CallLog, newItem: CallLog): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: CallLog, newItem: CallLog): Boolean {
            return oldItem.id == newItem.callId
        }
    }
}
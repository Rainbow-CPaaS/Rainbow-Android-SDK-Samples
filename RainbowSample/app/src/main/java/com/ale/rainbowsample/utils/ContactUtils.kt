package com.ale.rainbowsample.utils

import android.content.Context
import com.ale.infra.contact.IRainbowContact
import com.ale.rainbowsample.R
import com.ale.rainbowsdk.RainbowSdk

fun IRainbowContact.presenceAsString(context: Context) : String {
    if (isTerminated)
        return context.getString(R.string.removed_user)
    
    if (isLocalContact || !isRoster || RainbowSdk.instance().contacts().isLoggedInUser(this))
        return ""

    return when {
        presence.isOnline -> context.getString(R.string.online)
        presence.isOffline -> lastPresenceReceivedDate?.let { date -> context.getString(R.string.offline_since, date.toShortFormat()) } ?: context.getString(R.string.offline)
        presence.isBusyVideo -> context.getString(R.string.busy_video)
        presence.isBusyAudio || presence.isBusyPhone -> context.getString(R.string.busy_audio)
        presence.isBusyPresentation -> context.getString(R.string.busy_screen_sharing)
        presence.isBusyOnly -> context.getString(R.string.busy)
        presence.isManualAway -> context.getString(R.string.away)
        presence.isAway -> lastPresenceReceivedDate?.let { date -> context.getString(R.string.away_since, date.toShortFormat()) } ?: context.getString(R.string.away)
        presence.isDNDonly -> context.getString(R.string.do_not_disturb)
        presence.isDNDCalendar -> context.getString(R.string.do_not_disturb_appointment)
        presence.isDNDPresentation -> context.getString(R.string.do_not_disturb_presentation)
        presence.isMobileOnline -> context.getString(R.string.online_on_mobile)
        presence.isDNDTeams -> context.getString(R.string.do_not_disturb_teams)
        presence.isXA -> context.getString(R.string.offline)
        else -> ""
    }
}
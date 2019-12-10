package com.ale.conversations.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.viewpager2.widget.ViewPager2
import com.ale.conversations.R
import com.ale.conversations.activities.StartupActivity
import com.ale.conversations.pager.BubblePagerAdapter
import com.ale.conversations.pager.ZoomOutPageTransformer
import com.ale.infra.manager.room.Room
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import kotlinx.android.synthetic.main.main_pager_fragment.*

class BubbleConversationPagerFragment(val room: Room) : Fragment() {

    private lateinit var activity: StartupActivity
    private lateinit var pager: ViewPager2
    private lateinit var pagerAdapter: BubblePagerAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        activity = getActivity() as StartupActivity
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        activity.supportActionBar?.show()
        activity.supportActionBar?.title = room.name

        return inflater.inflate(R.layout.bubble_conversation_pager_fragment_, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        pager = view_pager
        pagerAdapter = BubblePagerAdapter(this)
        pager.adapter = pagerAdapter

        pager.setPageTransformer(ZoomOutPageTransformer())

        val tabLayout : TabLayout = view.findViewById(R.id.tab_layout
        )
        TabLayoutMediator(tabLayout, pager) { tab, position ->
            tab.text = pagerAdapter.getTitle(position)
        }.attach()

    }
}
package com.ale.conversations.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.fragment.app.Fragment
import androidx.viewpager2.widget.ViewPager2
import com.ale.conversations.R
import com.ale.conversations.activities.StartupActivity
import com.ale.conversations.pager.MainPagerAdapter
import com.ale.conversations.pager.ZoomOutPageTransformer
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import kotlinx.android.synthetic.main.main_pager_fragment.*

class MainPagerFragment : Fragment() {

    private lateinit var activity: StartupActivity
    private lateinit var pager: ViewPager2
    private lateinit var mainPagerAdapter: MainPagerAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        activity = getActivity() as StartupActivity
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.main_pager_fragment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        activity.window.clearFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS)
        activity.supportActionBar?.hide()

        pager = view_pager
        mainPagerAdapter = MainPagerAdapter(this)
        pager.adapter = mainPagerAdapter

        pager.setPageTransformer(ZoomOutPageTransformer())

        val tabLayout : TabLayout = view.findViewById(R.id.tab_layout
        )
        TabLayoutMediator(tabLayout, pager) { tab, position ->
            tab.text = mainPagerAdapter.getTitle(position)
        }.attach()
    }
}
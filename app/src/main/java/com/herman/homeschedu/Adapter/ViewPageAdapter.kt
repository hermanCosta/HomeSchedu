package com.herman.homeschedu.Adapter

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import com.herman.homeschedu.Fragments.ScheduleStep1Fragment
import com.herman.homeschedu.Fragments.ScheduleStep2Fragment
import com.herman.homeschedu.Fragments.ScheduleStep3Fragment
import com.herman.homeschedu.Fragments.ScheduleStep4Fragment

class ViewPageAdapter(fm: FragmentManager) : FragmentPagerAdapter(fm) {


    override fun getItem(position: Int) : Fragment {
        when (position) {

            0 -> return ScheduleStep1Fragment.getInstance()!!
            1 -> return ScheduleStep2Fragment.getInstance()!!
            2 -> return ScheduleStep3Fragment.getInstance()!!
            3 -> return ScheduleStep4Fragment.getInstance()!!

        }
        return Fragment()
    }

    override fun getCount(): Int {
        return 4
    }

}
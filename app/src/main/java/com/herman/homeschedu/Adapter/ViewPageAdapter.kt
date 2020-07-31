package com.herman.homeschedu.Adapter

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import com.herman.homeschedu.Fragments.BookingStep1Fragment
import com.herman.homeschedu.Fragments.BookingStep2Fragment
import com.herman.homeschedu.Fragments.BookingStep3Fragment
import com.herman.homeschedu.Fragments.BookingStep4Fragment

class ViewPageAdapter(fm: FragmentManager) : FragmentPagerAdapter(fm) {


    override fun getItem(position: Int) : Fragment {
        when (position) {

            0 -> return BookingStep1Fragment.getInstance()!!
            1 -> return BookingStep2Fragment.getInstance()!!
            2 -> return BookingStep3Fragment.getInstance()!!
            3 -> return BookingStep4Fragment.getInstance()!!

        }
        return Fragment()
    }

    override fun getCount(): Int {
        return 4
    }

}
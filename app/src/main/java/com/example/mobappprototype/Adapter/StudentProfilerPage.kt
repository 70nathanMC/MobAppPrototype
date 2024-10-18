package com.example.mobappprototype.Adapter

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.mobappprototype.fragments.AboutFragment
import com.example.mobappprototype.fragments.ReviewFragment
import com.example.mobappprototype.fragments.SchedFragment
import com.example.mobappprototype.fragments.StrengthFragment
import com.example.mobappprototype.ui.TutorProfileActivity

class StudentProfilePagerAdapter(activity: FragmentActivity, private val bio: String) : FragmentStateAdapter(activity) {

    override fun getItemCount(): Int = 2 // Number of tabs

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> AboutFragment.newInstance(bio)
            1 -> StrengthFragment()
            else -> throw IllegalStateException("Invalid tab position")
        }
    }
}

package com.example.mobappprototype.ViewModel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.mobappprototype.model.User


class UserViewModel : ViewModel() {
    val user: MutableLiveData<User> by lazy {
        MutableLiveData<User>()
    }

    fun setUser(userData: User) {
        user.value = userData
    }
}
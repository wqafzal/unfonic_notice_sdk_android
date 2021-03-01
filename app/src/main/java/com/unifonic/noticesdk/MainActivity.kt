package com.unifonic.noticesdk

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.unifonic.noticesdk.base.BaseActivity
import com.unifonic.noticesdk.base.BaseViewModel

class MainActivity : BaseActivity() {
    override fun inject() {
        component.inject(this)
    }

    override fun getViewModel(): BaseViewModel? = null

    override fun layoutId(): Int? = R.layout.activity_main

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }
}
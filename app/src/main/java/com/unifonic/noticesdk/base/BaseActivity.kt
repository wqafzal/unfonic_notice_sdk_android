package com.unifonic.noticesdk.base

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.unifonic.noticesdk.AndroidApp
import com.unifonic.noticesdk.di.modules.ViewModule

abstract class BaseActivity : AppCompatActivity() {

    abstract fun inject()

    abstract fun getViewModel(): BaseViewModel?

    abstract fun layoutId() : Int?

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        layoutId()?.let {
            setContentView(it)
        }
    }

    val component by lazy {
        app.component.plus(ViewModule(this))
    }

    val app: AndroidApp
        get() = application as AndroidApp



}
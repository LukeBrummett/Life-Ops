package com.lifeops.app

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

/**
 * Life-Ops Application class
 * 
 * Annotated with @HiltAndroidApp to enable Hilt dependency injection
 * As per Technical Architecture: Hilt for DI across the application
 */
@HiltAndroidApp
class LifeOpsApplication : Application()

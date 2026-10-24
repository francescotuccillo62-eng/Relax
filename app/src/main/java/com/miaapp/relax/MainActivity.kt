package com.miaapp.relax

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.miaapp.relax.ui.theme.RelaxTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        // installSplashScreen() DEVE essere chiamato prima di super.onCreate()
        val splashScreen = installSplashScreen()

        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // ═══════════════════════════════════════════════
        // EXIT ANIMATION DELLA SPLASH
        // ═══════════════════════════════════════════════
        // Quando la Home è pronta, la splash scivola verso l'alto
        // e svanisce in 400 ms.
        //
        // NOTA: `splashScreenView` è di tipo SplashScreenViewProvider,
        // non è una View. La View vera si ottiene con `splashScreenView.view`.
        splashScreen.setOnExitAnimationListener { splashScreenViewProvider ->
            val splashView = splashScreenViewProvider.view

            val slideUp = android.animation.ObjectAnimator.ofFloat(
                splashView,
                android.view.View.TRANSLATION_Y,
                0f,
                -splashView.height.toFloat()
            )
            val fadeOut = android.animation.ObjectAnimator.ofFloat(
                splashView,
                android.view.View.ALPHA,
                1f,
                0f
            )

            val animatorSet = android.animation.AnimatorSet()
            animatorSet.playTogether(slideUp, fadeOut)
            animatorSet.duration = 450L
            animatorSet.interpolator = android.view.animation.DecelerateInterpolator()
            animatorSet.addListener(object : android.animation.AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: android.animation.Animator) {
                    splashScreenViewProvider.remove()
                }
            })
            animatorSet.start()
        }

        setContent {
            RelaxTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    AppNavigazione(innerPadding = innerPadding)
                }
            }
        }
    }
}
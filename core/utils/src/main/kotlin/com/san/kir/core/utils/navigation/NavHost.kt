package com.san.kir.core.utils.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Composer
import androidx.compose.runtime.remember
import com.arkivanov.decompose.Child
import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.FaultyDecomposeApi
import com.arkivanov.decompose.extensions.compose.jetpack.stack.animation.StackAnimation
import com.arkivanov.decompose.extensions.compose.jetpack.stack.animation.StackAnimator
import com.arkivanov.decompose.extensions.compose.jetpack.stack.animation.isFront
import com.arkivanov.decompose.extensions.compose.jetpack.stack.animation.stackAnimation
import com.san.kir.core.utils.ManualDI
import com.san.kir.core.utils.viewModel.LocalComponentContext
import kotlin.jvm.internal.Intrinsics

@OptIn(FaultyDecomposeApi::class)
private val defaultAnimations: StackAnimation<NavConfig, NavContainer> =
    stackAnimation(false) { initial, target, direction ->
        if (direction.isFront) frontAnimation(initial.configuration)
        else frontAnimation(target.configuration)
    }

private fun frontAnimation(config: NavConfig): StackAnimator {
    val navAnimation = ManualDI.navAnimation(config)
    return navAnimation ?: EmptyStackAnimator
}

@Composable
fun NavHost(
    componentContext: ComponentContext,
    startConfig: NavConfig,
    stackAnimation: StackAnimation<NavConfig, NavContainer> = defaultAnimations,
) {
    val navHostComponent = remember {
        NavHostComponent(componentContext, startConfig, stackAnimation)
    }
    navHostComponent.Show()
}

@Composable
fun NavHost(
    startConfig: NavConfig,
    stackAnimation: StackAnimation<NavConfig, NavContainer> = defaultAnimations,
) = NavHost(LocalComponentContext.current, startConfig, stackAnimation)

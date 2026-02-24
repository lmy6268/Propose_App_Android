package com.hanadulset.pro_poseapp.domain.model.wrapper

/**
 * A wrapper interface for Android's LifecycleOwner. This ensures the Domain module does not
 * rely on androidx.lifecycle.LifecycleOwner.
 */
interface ProposeLifecycleOwner {
    // Empty wrapper interface.
    // Implementations in Data/UI layers will hold the actual androidx.lifecycle.LifecycleOwner.
}

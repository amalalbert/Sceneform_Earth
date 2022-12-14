package com.ar.sceneformdemo

import android.animation.ObjectAnimator
import android.view.animation.LinearInterpolator
import androidx.annotation.Nullable
import com.google.ar.sceneform.FrameTime
import com.google.ar.sceneform.Node
import com.google.ar.sceneform.math.Quaternion
import com.google.ar.sceneform.math.QuaternionEvaluator
import com.google.ar.sceneform.math.Vector3

class RotatingNode(
    solarSettings: SliderSettings, isOrbit: Boolean, clockwise: Boolean, axisTiltDeg: Float
) :
    Node() {
    // We'll use Property Animation to make this node rotate.
    @Nullable
    private var orbitAnimation: ObjectAnimator? = null
    private var degreesPerSecond = 90.0f
    private val solarSettings: SliderSettings
    private val isOrbit: Boolean
    private val clockwise: Boolean
    private val axisTiltDeg: Float
    private var lastSpeedMultiplier = 1.0f
    override fun onUpdate(frameTime: FrameTime) {
        super.onUpdate(frameTime)

        // Animation hasn't been set up.
        if (orbitAnimation == null) {
            return
        }

        // Check if we need to change the speed of rotation.
        val speedMultiplier = speedMultiplier

        // Nothing has changed. Continue rotating at the same speed.
        if (lastSpeedMultiplier == speedMultiplier) {
            return
        }
        if (speedMultiplier == 0.0f) {
            orbitAnimation!!.pause()
        } else {
            orbitAnimation!!.resume()
            val animatedFraction = orbitAnimation!!.animatedFraction
            orbitAnimation!!.duration = animationDuration
            orbitAnimation!!.setCurrentFraction(animatedFraction)
        }
        lastSpeedMultiplier = speedMultiplier
    }

    /** Sets rotation speed  */
    fun setDegreesPerSecond(degreesPerSecond: Float) {
        this.degreesPerSecond = degreesPerSecond
    }

    override fun onActivate() {
        startAnimation()
    }

    override fun onDeactivate() {
        stopAnimation()
    }

    private val animationDuration: Long
        get() = (1000 * 360 / (degreesPerSecond * speedMultiplier)).toLong()
    private val speedMultiplier: Float
        get() = if (isOrbit) {
            solarSettings.getOrbitSpeedMultiplier()
        } else {
            solarSettings.getRotationSpeedMultiplier()
        }

    private fun startAnimation() {
        if (orbitAnimation != null) {
            return
        }
        orbitAnimation = createAnimator()
        orbitAnimation!!.target = this
        orbitAnimation!!.duration = animationDuration
        orbitAnimation!!.start()
    }

    private fun stopAnimation() {
        if (orbitAnimation == null) {
            return
        }
        orbitAnimation!!.cancel()
        orbitAnimation = null
    }

    companion object {
        /** Returns an ObjectAnimator that makes this node rotate.  */
        private fun createAnimator(): ObjectAnimator? {
            // Node's setLocalRotation method accepts Quaternions as parameters.
            // First, set up orientations that will animate a circle.
            val orientation1 = Quaternion.axisAngle(Vector3(0.0f, 1.0f, 0.0f), 0f)
            val orientation2 = Quaternion.axisAngle(Vector3(0.0f, 1.0f, 0.0f), 120f)
            val orientation3 = Quaternion.axisAngle(Vector3(0.0f, 1.0f, 0.0f), 240f)
            val orientation4 = Quaternion.axisAngle(Vector3(0.0f, 1.0f, 0.0f), 360f)
            val orbitAnimation = ObjectAnimator()
            orbitAnimation.setObjectValues(orientation1, orientation2, orientation3, orientation4)

            // Next, give it the localRotation property.
            orbitAnimation.setPropertyName("localRotation")

            // Use Sceneform's QuaternionEvaluator.
            orbitAnimation.setEvaluator(QuaternionEvaluator())

            //  Allow orbitAnimation to repeat forever
            orbitAnimation.repeatCount = ObjectAnimator.INFINITE
            orbitAnimation.repeatMode = ObjectAnimator.RESTART
            orbitAnimation.interpolator = LinearInterpolator()
            orbitAnimation.setAutoCancel(true)
            return orbitAnimation
        }
    }

    init {
        this.solarSettings = solarSettings
        this.isOrbit = isOrbit
        this.clockwise = clockwise
        this.axisTiltDeg = axisTiltDeg
    }
}
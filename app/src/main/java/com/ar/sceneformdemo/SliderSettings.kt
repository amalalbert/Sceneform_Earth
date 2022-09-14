package com.ar.sceneformdemo

class SliderSettings {
    private var orbitSpeedMultiplier = 1.0f
    private var rotationSpeedMultiplier = 1.0f

    fun setOrbitSpeedMultiplier(orbitSpeedMultiplier: Float) {
        this.orbitSpeedMultiplier = orbitSpeedMultiplier
    }

    fun getOrbitSpeedMultiplier(): Float {
        return orbitSpeedMultiplier
    }

    fun setRotationSpeedMultiplier(rotationSpeedMultiplier: Float) {
        this.rotationSpeedMultiplier = rotationSpeedMultiplier
    }

    fun getRotationSpeedMultiplier(): Float {
        return rotationSpeedMultiplier
    }
}
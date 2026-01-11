package com.example.rentmycar_android_app.model

class DrivingDataRequestBuilder {
    private var maxAccelerationForce: Double = 0.0
    private var maxBrakingForce: Double = 0.0
    private var avgSpeed: Double = 0.0
    private var maxSpeed: Double = 0.0
    private var distance: Double = 0.0
    private var duration: Long = 0L
    private var harshAccelerationCount: Int = 0
    private var harshBrakingCount: Int = 0
    private var carName: String?  = null

    fun setMaxAccelerationForce(force: Double) = apply { this.maxAccelerationForce = force }
    fun setMaxBrakingForce(force: Double) = apply { this.maxBrakingForce = force }
    fun setAvgSpeed(speed: Double) = apply { this.avgSpeed = speed }
    fun setMaxSpeed(speed: Double) = apply { this.maxSpeed = speed }
    fun setDistance(distance: Double) = apply { this.distance = distance }
    fun setDuration(duration: Long) = apply { this.duration = duration }
    fun setHarshAccelerationCount(count: Int) = apply { this.harshAccelerationCount = count }
    fun setHarshBrakingCount(count: Int) = apply { this.harshBrakingCount = count }
    fun setCarName(name: String?) = apply { this.carName = name }

    fun build() = DrivingDataRequest(
        maxAccelerationForce = maxAccelerationForce,
        maxBrakingForce = maxBrakingForce,
        avgSpeed = avgSpeed,
        maxSpeed = maxSpeed,
        distance = distance,
        duration = duration,
        harshAccelerationCount = harshAccelerationCount,
        harshBrakingCount = harshBrakingCount,
        carName = carName
    )
}
package com.fig7.trackpacer

const val totalTime = 420000.0
const val totalDistance = 1609.34

class WaypointCalculator {
    private var waypointList: Array<Double> = Array(32) { i-> ((59.34 + 50.0*i) / totalDistance) }
    private var currentWaypoint = 0

    private fun waypointTime(): Double {
        return waypointList[currentWaypoint] * totalTime
    }

    private fun nextWaypointTime(): Double {
        return waypointList[currentWaypoint+1] * totalTime
    }

    fun beginRun(): Double {
        currentWaypoint = 0
        return waypointTime()
    }

    fun nextWaypointIn() : Double {
        val timeToNext = nextWaypointTime() - waypointTime()

        currentWaypoint += 1
        return timeToNext
    }

    fun waypointNum(): Int {
        return currentWaypoint
    }

    fun waypointsRemaining(): Boolean {
        return (currentWaypoint < 31)
    }
}
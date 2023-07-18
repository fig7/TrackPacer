package com.fig7.trackpacer

val distanceMap = mapOf("400m"   to 400.0,
                        "800m"   to 800.0,
                        "1200m"  to 1200.0,
                        "1500m"  to 1500.0,
                        "2000m"  to 2000.0,
                        "3000m"  to 3000.0,
                        "4000m"  to 4000.0,
                        "5000m"  to 5000.0,
                        "10000m" to 10000.0,
                        "1 mile" to 1609.34)

val waypointMap = mapOf("400m"    to Array(8)   { i -> 50.0 + 50.0*i },
                        "800m"    to Array(16)  { i -> 50.0 + 50.0*i },
                        "1200m"   to Array(24)  { i -> 50.0 + 50.0*i },
                        "1500m"   to Array(30)  { i -> 50.0 + 50.0*i },
                        "2000m"   to Array(40)  { i -> 50.0 + 50.0*i },
                        "3000m"   to Array(60)  { i -> 50.0 + 50.0*i },
                        "4000m"   to Array(80)  { i -> 50.0 + 50.0*i },
                        "5000m"   to Array(100) { i -> 50.0 + 50.0*i },
                        "10000m"  to Array(200) { i -> 50.0 + 50.0*i },
                        "1 mile"  to Array(32)  { i -> 59.34 + 50.0*i })

class WaypointCalculator {
    private lateinit var waypointList: Array<Double>
    private var currentWaypoint = -1

    private var totalDistance = -1.0
    private var totalTime = -1.0

    fun waypointTime(): Double {
        return (waypointList[currentWaypoint] * totalTime) / totalDistance
    }

    fun initRun(runDist: String, runTime: Double) {
        totalDistance = distanceMap[runDist]!!
        totalTime = runTime

        currentWaypoint = -1
        waypointList = waypointMap[runDist]!!
    }

    fun beginRun(): Double {
        currentWaypoint = 0
        return waypointTime()
    }

    fun waypointNum(): Int {
        return currentWaypoint
    }

    fun waypointsRemaining(): Boolean {
        return (currentWaypoint < (waypointList.size - 1))
    }

    fun nextWaypoint(): Double {
        currentWaypoint += 1
        return waypointTime()
    }

    fun runTime(): Long {
        return totalTime.toLong()
    }
}
package com.fig7.trackpacer.waypoint

import com.fig7.trackpacer.util.Bool
import com.fig7.trackpacer.util.Int64

val distanceMap = mapOf(
                        "400m"     to 400.0,
                        "800m"     to 800.0,
                        "1000m"    to 1000.0,
                        "1200m"    to 1200.0,
                        "1500m"    to 1500.0,
                        "2000m"    to 2000.0,
                        "3000m"    to 3000.0,
                        "4000m"    to 4000.0,
                        "5000m"    to 5000.0,
                        "10000m"   to 10000.0,
                        "1 mile"   to 1609.34)

val waypointMap = mapOf(
                        "400m"    to Array(  8) { i ->  50.0 + 50.0*i.toDouble() },
                        "800m"    to Array( 16) { i ->  50.0 + 50.0*i.toDouble() },
                        "1000m"   to Array( 20) { i ->  50.0 + 50.0*i.toDouble() },
                        "1200m"   to Array( 24) { i ->  50.0 + 50.0*i.toDouble() },
                        "1500m"   to Array( 30) { i ->  50.0 + 50.0*i.toDouble() },
                        "2000m"   to Array( 40) { i ->  50.0 + 50.0*i.toDouble() },
                        "3000m"   to Array( 60) { i ->  50.0 + 50.0*i.toDouble() },
                        "4000m"   to Array( 80) { i ->  50.0 + 50.0*i.toDouble() },
                        "5000m"   to Array(100) { i ->  50.0 + 50.0*i.toDouble() },
                        "10000m"  to Array(200) { i ->  50.0 + 50.0*i.toDouble() },
                        "1 mile"  to Array( 32) { i -> 59.34 + 50.0*i.toDouble() })

val rDiff        = Array(8) {i -> 1.22*i.toDouble() }
val arcAngle     = arrayOf(1.358696, 1.358696, 0.424201, 0.0, 1.358696, 1.358696, 0.424201, 0.0)
val arcAngle1500 = arrayOf(0.424201, 0.0, 1.358696, 1.358696, 0.424201, 0.0, 1.358696, 1.358696)

val runMultiplier     = Array(8) { i ->
    val r = 36.8 + rDiff[i]
    (2.0*Math.PI*r + 168.78)/400.0
}

val runMultiplier1500 = Array(8) { i ->
    val r = 36.8 + rDiff[i]
    ((Math.PI + 0.424201)*r + 168.78 + (6.0*Math.PI*r) + 506.34)/1500.0
}

val runMultiplierMile = Array(8) { i ->
    val r = 36.8 + rDiff[i]
    (8.0*Math.PI*r + 675.12 + 9.34)/1609.34
}

fun distanceFor(runDist: String, runLane: Int): Double {
    val runLaneIndex = runLane - 1
    return when (runDist) {
        "1500m" -> {
            // Special case, 1500m is 3.75 laps
            distanceMap[runDist]!! * runMultiplier1500[runLaneIndex]
        }

        "1 mile" -> {
            // Special case, 1 mile is 4 laps + 9.34m
            distanceMap[runDist]!! * runMultiplierMile[runLaneIndex]
        }

        else -> {
            distanceMap[runDist]!! * runMultiplier[runLaneIndex]
        }
    }
}

fun timeFor(runDist: String, runLane: Int, runTime:Double): Double {
    val runLaneIndex = runLane - 1
    return when (runDist) {
        "1500m" -> {
            // Special case, 1500m is 3.75 laps
            runTime * runMultiplier1500[runLaneIndex]
        }

        "1 mile" -> {
            // Special case, 1 mile is 4 laps + 9.34m
            runTime * runMultiplierMile[runLaneIndex]
        }

        else -> {
            runTime * runMultiplier[runLaneIndex]
        }
    }
}

class WaypointCalculator {
    private lateinit var waypointList: Array<Double>
    private lateinit var waypointArcAngle: Array<Double>
    private var currentWaypoint = -1
    private var currentExtra    = -1.0

    private var totalDist     = -1.0
    private var totalTime     = -1.0
    private var runLaneIndex  = -1

    private fun arcExtra() : Double {
        val arcIndex = currentWaypoint % 8
        val arcAngle = waypointArcAngle[arcIndex]
        return arcAngle * rDiff[runLaneIndex]
    }

    private fun waypointDistance() : Double {
        return waypointList[currentWaypoint] + currentExtra
    }

    fun waypointTime(): Double {
        return (waypointDistance() * totalTime) / totalDist
    }

    private fun initRunParams(runDist: String, runTime: Double, runLane: Int) {
        runLaneIndex = runLane - 1
        waypointList = waypointMap[runDist]!!

        when (runDist) {
            "1500m" -> {
                // Special case, 1500m is 3.75 laps
                totalDist = distanceMap[runDist]!! * runMultiplier1500[runLaneIndex]
                totalTime = runTime * runMultiplier1500[runLaneIndex]
                waypointArcAngle = arcAngle1500
            }

            "1 mile" -> {
                // Special case, 1 mile is 4 laps + 9.34m
                totalDist = distanceMap[runDist]!! * runMultiplierMile[runLaneIndex]
                totalTime = runTime * runMultiplierMile[runLaneIndex]
                waypointArcAngle = arcAngle
            }

            else -> {
                totalDist = distanceMap[runDist]!! * runMultiplier[runLaneIndex]
                totalTime = runTime * runMultiplier[runLaneIndex]
                waypointArcAngle = arcAngle
            }
        }
    }

    fun initRun(runDist: String, runTime: Double, runLane: Int) {
        initRunParams(runDist, runTime, runLane)

        currentWaypoint = 0
        currentExtra    = arcExtra()
        // Log.d("TP", totalDistance.toString())
        // Log.d("TP", totalTime.toString())

        // Log.d("TP", waypointTime().toString())
    }

    fun initResume(runDist: String, runTime: Double, runLane: Int, resumeTime: Double): Double {
        initRunParams(runDist, runTime, runLane)

        var prevTime = 0.0
        for (i in waypointList.indices) {
            currentWaypoint = i
            currentExtra += arcExtra()

            val waypointTime = waypointTime()
            if (waypointTime > resumeTime) {
                break
            }

            prevTime = waypointTime
        }

        return prevTime
    }

    fun waypointNum(): Int {
        return currentWaypoint
    }

    fun waypointsRemaining(): Bool {
        return (currentWaypoint < (waypointList.size - 1))
    }

    fun nextWaypoint(): Double {
        // val waypoint1 = waypointTime()
        currentWaypoint += 1
        currentExtra += arcExtra()

        // Log.d("TP", (waypointTime()-waypoint1).toString())
        return waypointTime()
    }

    fun runTime(): Int64 {
        return totalTime.toLong()
    }

    fun distOnPace(elapsedTime: Double): Double {
        return if(elapsedTime > totalTime) totalDist else (elapsedTime*totalDist)/totalTime
    }
}
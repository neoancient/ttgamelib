package ttgamelib.javafx

import ttgamelib.*
import java.util.*

private val bundle = ResourceBundle.getBundle("StringResources")

private fun getString(key: String): String {
    return try {
        bundle.getString(key)
    } catch(e: MissingResourceException) {
        "[$key]"
    }
}

public fun HexBoard.facingName(facing: Int): String {
    return getString(
        if (verticalGrid) {
            when (facing) {
                V_DIR_N -> "facing.n"
                V_DIR_NE -> "facing.ne"
                V_DIR_SE -> "facing.se"
                V_DIR_S -> "facing.s"
                V_DIR_SW -> "facing.sw"
                V_DIR_NW -> "facing.nw"
                else -> "facing.error"
            }
        } else {
            when (facing) {
                H_DIR_E -> "facing.e"
                H_DIR_SE -> "facing.se"
                H_DIR_SW -> "facing.sw"
                H_DIR_W -> "facing.w"
                H_DIR_NW -> "facing.nw"
                H_DIR_NE -> "facing.ne"
                else -> "facing.error"
            }
        }
    )
}

public fun HexBoard.facingAbbreviation(facing: Int): String {
    return getString(
        if (verticalGrid) {
            when (facing) {
                V_DIR_N -> "facing.abbrev.n"
                V_DIR_NE -> "facing.abbrev.ne"
                V_DIR_SE -> "facing.abbrev.se"
                V_DIR_S -> "facing.abbrev.s"
                V_DIR_SW -> "facing.abbrev.sw"
                V_DIR_NW -> "facing.abbrev.nw"
                else -> "facing.abbrev.error"
            }
        } else {
            when (facing) {
                H_DIR_E -> "facing.abbrev.e"
                H_DIR_SE -> "facing.abbrev.se"
                H_DIR_SW -> "facing.abbrev.sw"
                H_DIR_W -> "facing.abbrev.w"
                H_DIR_NW -> "facing.abbrev.nw"
                H_DIR_NE -> "facing.abbrev.ne"
                else -> "facing.abbrev.error"
            }
        }
    )
}

public fun WindStrength.displayName(): String {
    return getString(
        when (this) {
            WindStrength.BECALMED -> "WindStrength.BECALMED"
            WindStrength.GENTLE_BREEZE -> "WindStrength.GENTLE_BREEZE"
            WindStrength.LIGHT_WIND -> "WindStrength.LIGHT_WIND"
            WindStrength.MODERATE_WIND -> "WindStrength.MODERATE_WIND"
            WindStrength.STRONG_WIND -> "WindStrength.STRONG_WIND"
            WindStrength.GALE -> "WindStrength.GALE"
            WindStrength.STORM -> "WindStrength.STORM"
            WindStrength.HURRICANE -> "WindStrength.HURRICANE"
        }
    )
}

public fun MapRegion.displayName(): String {
    return getString(
        when (this) {
            MapRegion.NORTH -> "MapRegion.NORTH.displayName"
            MapRegion.SOUTH -> "MapRegion.SOUTH.displayName"
            MapRegion.EAST -> "MapRegion.EAST.displayName"
            MapRegion.WEST -> "MapRegion.WEST.displayName"
            MapRegion.NORTHEAST -> "MapRegion.NORTHEAST.displayName"
            MapRegion.NORTHWEST -> "MapRegion.NORTHWEST.displayName"
            MapRegion.SOUTHEAST -> "MapRegion.SOUTHEAST.displayName"
            MapRegion.SOUTHWEST -> "MapRegion.SOUTHWEST.displayName"
            MapRegion.ANY -> "MapRegion.ANY.displayName"
            MapRegion.ANY_EDGE -> "MapRegion.ANY_EDGE.displayName"
            MapRegion.CENTER -> "MapRegion.CENTER.displayName"
            MapRegion.NONE -> "MapRegion.NONE.displayName"
            is MapRegion.RangeOf -> "MapRegion.RANGE.displayName"
            is MapRegion.Custom -> "MapRegion.CUSTOM.displayName"
        }
    )
}
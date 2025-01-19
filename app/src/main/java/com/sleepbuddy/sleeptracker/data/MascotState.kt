package com.sleepbuddy.sleeptracker.data

import com.sleepbuddy.sleeptracker.R

enum class MascotState(val animationRes: Int) {
    HAPPY(R.raw.happy_pet),
    NEUTRAL(R.raw.neutral_pet),
    ANGRY(R.raw.angry_pet),
    ENCOURAGING(R.raw.encouraging_pet),
    EXTREMELY_HAPPY(R.raw.extremely_happy_pet),
    SPECIAL(R.raw.special_pet)
}

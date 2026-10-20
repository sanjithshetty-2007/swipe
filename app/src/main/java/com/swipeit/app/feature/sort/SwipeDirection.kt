package com.swipeit.app.feature.sort

/** Where a card left the deck. LEFT trashes; RIGHT and UP keep. */
enum class SwipeDirection {
    LEFT,
    RIGHT,
    UP,
    ;

    val isKeep: Boolean get() = this != LEFT
}

package com.san.kir.background.logic

sealed interface Command {
    data object None : Command
    data object Stop : Command
    data object Start : Command
    data object Destroy : Command
    data object Update : Command
}

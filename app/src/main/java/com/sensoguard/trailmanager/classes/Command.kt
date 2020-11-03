package com.sensoguard.trailmanager.classes

class Command(val commandName: String, val commandContent: String?, val icId: Int) {
    var selectionsTitles = ArrayList<String>()
    var selectionsCommands = ArrayList<String>()
    var defaultSelected: Int = 1

}
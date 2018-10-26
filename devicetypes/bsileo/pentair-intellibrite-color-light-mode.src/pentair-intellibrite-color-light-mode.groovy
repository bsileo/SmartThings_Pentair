/**
 *  Copyright 2018 Brad Sileo
 *
 *
 *  Intellibrite Color Mode Tile
 *
 *  Author: Brad Sileo
 *
 *  Date: 2018-10-21
 */
metadata {
	definition (name: "Pentair Intellibrite Color Light Mode", namespace: "bsileo", author: "Brad Sileo") {
		capability "Actuator"
		capability "Switch"
		capability "Momentary"
		capability "Sensor"
	}

	// simulator metadata
	simulator {
	}

	// UI tile definitions
	tiles {
		standardTile("switch", "device.switch", width: 2, height: 2, canChangeIcon: true) {
			state "off", label: "", action: "momentary.push", backgroundColor: "#ffffff", nextState: "on"
			state "on", label: "", action: "momentary.push", icon:"st.Lighting.light21", backgroundColor: "#A9A9A9"

			state "Party", label:"", action:"momentary.push", icon:"https://bsileo.github.io/SmartThings_Pentair/party.png", backgroundColor:"#4250f4", nextState:"on"
            state "Romance", label:"", action:"momentary.push", icon:"https://bsileo.github.io/SmartThings_Pentair/romance.png", backgroundColor:"#d28be8", nextState:"on"
            state "Caribbean", label:"", action:"momentary.push", icon:"https://bsileo.github.io/SmartThings_Pentair/caribbean.png", backgroundColor:"#46f2e9", nextState:"on"        
            state "American", label:"", action:"momentary.push", icon:"https://bsileo.github.io/SmartThings_Pentair/american.png", backgroundColor:"#d42729", nextState:"on"        
            state "Sunset", label:"", action:"momentary.push", icon:"https://bsileo.github.io/SmartThings_Pentair/sunset.png", backgroundColor:"#ffff00", nextState:"on"        
            state "Royal", label:"", action:"momentary.push", icon:"https://bsileo.github.io/SmartThings_Pentair/royal.png", backgroundColor:"#9933ff", nextState:"on"        

			state "Blue", label:"Blue", action: "momentary.push", icon:"st.Lighting.light21", backgroundColor:"#0000FF", nextState:"on"
		    state "Green", label:"Green", action: "momentary.push", icon:"st.Lighting.light21", backgroundColor:"#33cc33", nextState:"on"
			state "Red", label: "Red", action: "momentary.push", icon:"st.Lighting.light21",backgroundColor: "#bc3a2f", nextState: "on"
            state "White", label:"White", action:"momentary.push", icon:"st.Lighting.light21", backgroundColor:"#ffffff", nextState:"on"
            state "Magenta", label:"Magenta", action:"momentary.push", icon:"st.Lighting.light21", backgroundColor:"#ff00ff", nextState:"on"
            
		}
		main "switch"
		details "switch"
	}
}


def installed() {
	log.debug("Installed Intellibrite Color Mode color=" + device.deviceNetworkId)
    def mode = getDataValue("modeName")
	sendEvent(name: "switch", value: mode, isStateChange: true, displayed: false)
}

def parse(String description) {
}

def push() {
	sendEvent(name: "switch", value: "on", isStateChange: true, displayed: false)
    def mode = getDataValue("modeName")
    def circuitID = getDataValue("circuitID")
    parent.setColor(circuitID, getColorOrModeID())
 	sendEvent(name: "switch", value: mode, isStateChange: true, displayed: false)
	sendEvent(name: "momentary", value: "pushed", isStateChange: true)
}

def getColorOrModeID() {
	def colorID 
    def colorIDLookup = ["White" : 0,
        "Custom" :1,
        "Light Green":2,
        "Green":4,
        "Cyan":6,
        "Blue":8,
        "Lavender":10,
        "Magenta":12,
        "Light Magenta":14,
        'Off': 0,
        'On': 1,
        'Color Sync': 128,
        'Color Swim': 144,
        'Color Set': 160,
        'Party': 177,
        'Romance': 178,
        'Caribbean': 179,
        'American': 180,
        'Sunset': 181,
        'Royal': 182,
        'Save': 190,
        'Recall': 191,
        'Blue': 193,
        'Green': 194,
        'Red': 195,
        'White': 196,
        'Magenta': 197
        ]
    def mode = getDataValue("modeName")
    return colorIDLookup[mode]    
}

def on() {
	push()
}

def off() {
	push()
}
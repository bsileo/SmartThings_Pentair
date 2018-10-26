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
	definition (name: "Pentair Intellibrite Color Light", namespace: "bsileo", author: "Brad Sileo") {		
		capability "Switch"
		capability "Momentary"
        attribute "FriendlyName","string"
        attribute "circuitID","string"
	}

	// simulator metadata
	simulator {
	}

	// UI tile definitions
	tiles {
		standardTile("switch", "device.switch", width: 2, height: 2, canChangeIcon: true) {
			state "off", label: "Off", action: "on", icon:"st.Lighting.light21", nextState: "on", backgroundColor: "#ffffff"
			state "on", label: "On", action: "off", icon:"st.Lighting.light21",  nextState: "off", backgroundColor: "#79b821"

			state "Party", label:"", action:"off", icon:"https://bsileo.github.io/SmartThings_Pentair/party.png", backgroundColor:"#4250f4", nextState:"off"
            state "Romance", label:"", action:"off", icon:"https://bsileo.github.io/SmartThings_Pentair/romance.png", backgroundColor:"#d28be8", nextState:"off"
            state "Caribbean", label:"", action:"off", icon:"https://bsileo.github.io/SmartThings_Pentair/caribbean.png", backgroundColor:"#46f2e9", nextState:"off"        
            state "American", label:"", action:"off", icon:"https://bsileo.github.io/SmartThings_Pentair/american.png", backgroundColor:"#d42729", nextState:"off"        
            state "Sunset", label:"", action:"off", icon:"https://bsileo.github.io/SmartThings_Pentair/sunset.png", backgroundColor:"#ffff00", nextState:"off"        
            state "Royal", label:"", action:"off", icon:"https://bsileo.github.io/SmartThings_Pentair/royal.png", backgroundColor:"#9933ff", nextState:"off"        

			state "Blue", label:"Blue", action: "off", icon:"st.Lighting.light21", backgroundColor:"#0000FF", nextState:"off"
		    state "Green", label:"Green", action: "off", icon:"st.Lighting.light21", backgroundColor:"#33cc33", nextState:"off"
			state "Red", label: "Red", action: "off", icon:"st.Lighting.light21",backgroundColor: "#bc3a2f", nextState: "off"
            state "White", label:"White", action:"off", icon:"st.Lighting.light21", backgroundColor:"#ffffff", nextState:"off"
            state "Magenta", label:"Magenta", action:"off", icon:"st.Lighting.light21", backgroundColor:"#ff00ff", nextState:"off"
            
		}
 			
		main "switch"
		details "switch"
	}
}


def configure() {

}

def installed() {
	log.debug("Installed Intellibrite Color Light " + device.deviceNetworkId)
    manageData()
    manageChildren()       	
}

def updated() {
  manageData()
  manageChildren()
}

def manageData() {
 	def cid = getDataValue("circuitID")
	sendEvent(name: "circuitID", value: cid, isStateChange: true, displayed: false)
    def name = getDataValue("FriendlyName")
	sendEvent(name: "FriendlyName", value: name, isStateChange: true, displayed: false)
}

def getChildDNI(name) {
	def cid = getDataValue("circuitID")
	return parent.getChildDNI("IB" + cid + "-" + name)
}

def manageChildren() {
	def hub = location.hubs[0]    
	log.debug "TODO - Connect to Intellibrite Light Mode Children for this device"
	//def colors = ['Off','On','Color Sync','Color Swim','Color Set', 'Party','Romance','Caribbean','American','Sunset','Royal','Save','Recall','Blue','Green','Red','White','Magenta']
    def colors = ['Party','Romance','Caribbean','American','Sunset','Royal','Green','Red','White','Magenta','Blue']
        
 	def displayName
    def deviceID
    def existingButton
    def cDNI
    def circuitID = getDataValue("circuitID")
	// Create selected devices
	colors.each {
    	log.debug ("Create " + it + " light mode button")
 	    displayName = "IBM-${circuitID}-" + it
        deviceID = it
        existingButton = parent.childDevices.find({it.deviceNetworkId == parent.getChildDNI(deviceID)})
        // TODO - need to register with this device here - "subscribe" via a smartapp to update my modeName correctly
      }
}


def parse(String description) {
}

def on() {
	sendEvent(name: "switch", value: "on", isStateChange: true, displayed: true)
    parent.setCircuit(getDataValue("circuitID"), 1)
}

def off() {
	sendEvent(name: "switch", value: "off", isStateChange: true, displayed: true)
    parent.setCircuit(getDataValue("circuitID"), 0)
}
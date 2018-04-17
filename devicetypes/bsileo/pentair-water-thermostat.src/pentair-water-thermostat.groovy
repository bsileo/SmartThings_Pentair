/**
 *  Copyright 2015 SmartThings
 *
 *  Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License. You may obtain a copy of the License at:
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software distributed under the License is distributed
 *  on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License
 *  for the specific language governing permissions and limitations under the License.
 *
 */
metadata {
	definition (name: "Pentair Water Thermostat", namespace: "bsileo", author: "Brad Sileo") {
		capability "Actuator"
		capability "Temperature Measurement"
		capability "Thermostat"
		capability "Refresh"
		capability "Sensor"
		capability "Health Check"
        
		command "lowerHeatingSetpoint"
		command "raiseHeatingSetpoint"		
		command "poll"	    	
	    command "heaterOn"
        command "heaterOff"
        command "nextMode"
	}

	tiles {
		standardTile("mode", "device.thermostatMode", width:2, height:2, inactiveLabel: false, decoration: "flat") {
			state "OFF",  action:"nextMode",  nextState: "updating", icon: "st.thermostat.heating-cooling-off"
			state "Heater", action:"nextMode", nextState: "updating", icon: "st.thermostat.heat"	            
        	state "Solar Only", label:'${currentValue}', action:"nextMode",  nextState: "updating", icon: "https://bsileo.github.io/SmartThings_Pentair/solar-only.png"
            state "Solar Pref", label:'${currentValue}', action:"nextMode",  nextState: "updating", icon: "https://bsileo.github.io/SmartThings_Pentair/solar-preferred.jpg"
			state "updating", label:"Updating...", icon: "st.Home.home1"
		}
               
        multiAttributeTile(name:"temperature", type:"generic", width:3, height:2, canChangeIcon: true) {
			tileAttribute("device.temperature", key: "PRIMARY_CONTROL") {
				attributeState("temperature", label:'${currentValue}°',
					backgroundColors:[
							// Celsius
							[value: 0, color: "#153591"],
							[value: 7, color: "#1e9cbb"],
							[value: 15, color: "#90d2a7"],
							[value: 23, color: "#44b621"],
							[value: 28, color: "#f1d801"],
							[value: 35, color: "#d04e00"],
							[value: 37, color: "#bc2323"],
							// Fahrenheit
							[value: 40, color: "#153591"],
							[value: 44, color: "#1e9cbb"],
							[value: 59, color: "#90d2a7"],
							[value: 74, color: "#44b621"],
							[value: 84, color: "#f1d801"],
							[value: 95, color: "#d04e00"],
							[value: 96, color: "#bc2323"]
					]
				)
			}           
		}
		
                    
		standardTile("lowerHeatingSetpoint", "device.heatingSetpoint", width:2, height:1, inactiveLabel: false, decoration: "flat") {
			state "heatingSetpoint", action:"lowerHeatingSetpoint", icon:"st.thermostat.thermostat-left"
		}
		valueTile("heatingSetpoint", "device.heatingSetpoint", width:2, height:1, inactiveLabel: false, decoration: "flat") {
			state "heatingSetpoint", label:'${currentValue}° heat', backgroundColor:"#ffffff"
		}
		standardTile("raiseHeatingSetpoint", "device.heatingSetpoint", width:2, height:1, inactiveLabel: false, decoration: "flat") {
			state "heatingSetpoint", action:"raiseHeatingSetpoint", icon:"st.thermostat.thermostat-right"
		}
	
	    standardTile("refresh", "device.thermostatMode", width:2, height:2, inactiveLabel: false, decoration: "flat") {
			state "default", action:"refresh.refresh", icon:"st.secondary.refresh"
		}
		main "mode"
		details(["temperature", "lowerHeatingSetpoint", "heatingSetpoint", "raiseHeatingSetpoint","mode", "refresh"])
	}
}

def installed() {

}

def updated() {
    if (!state.updatedLastRanAt || now() >= state.updatedLastRanAt + 5000) {
		state.updatedLastRanAt = now()
		log.debug "Executing 'updated'"
        initialize()
	}
	else {
		log.trace "updated(): Ran within last 5 seconds so aborting."
	}
}

def initialize() {
	
    state.scale = "F"
}

def parse(String description)
{
	
}



// Command Implementations
def poll() {
	// Call refresh which will cap the polling to once every 2 minutes
	refresh()
}

def refresh() {
	pollDevice()
}

def pollDevice() {
    parent.poll()
}

def raiseHeatingSetpoint() {
	alterSetpoint(true)
}

def lowerHeatingSetpoint() {
	alterSetpoint(false)
}


// Adjusts nextHeatingSetpoint either .5° C/1° F) if raise true/false
def alterSetpoint(raise) {
	def locationScale = getTemperatureScale()
	def deviceScale = (state.scale == 1) ? "F" : "C"
	def heatingSetpoint = getTempInLocalScale("heatingSetpoint")
	def targetValue = heatingSetpoint
	def delta = (locationScale == "F") ? 1 : 0.5
	targetValue += raise ? delta : - delta
	
	sendEvent("name": "heatingSetpoint", "value": targetValue,
				unit: getTemperatureScale(), eventType: "ENTITY_UPDATE", displayed: true)    
    parent.updateSetpoint(device,targetValue)
}

// set the local value for the heatingSetpoint. Doesd NOT update the parent / Pentair platform!!!
def setHeatingSetpoint(degrees) {
   log.debug "setHeatingSetpoint " + device.deviceNetworkId + "-" + degrees
	def timeNow = now()
    if (degrees) {	
    	if (!state.heatingSetpointTriggeredAt || (1 * 2 * 1000 < (timeNow - state.heatingSetpointTriggeredAt))) {
			state.heatingSetpointTriggeredAt = timeNow               
			state.heatingSetpoint = degrees.toDouble()
			sendEvent(name: "heatingSetpoint", value:state.heatingSetpoint, unit: getTemperatureScale())    	
		}
	}
}

// local action to move me to the next available heater mode and update the poolController
def nextMode() {
	//log.debug("Going to nextMode()")
    def currentMode = device.currentValue("thermostatMode")
	def supportedModes = getModeMap()
    def nextIndex = 0;
    //log.debug("${currentMode} moving to next in ${supportedModes}")
    supportedModes.eachWithIndex {name, index ->
    	//log.debug("${index}:${name} -->${nextIndex}  ${name} == ${currentMode}")
    	if (name == currentMode) { 
        	nextIndex = index +1
            return
         }
    }
    //log.debug("nextMode id=${nextIndex}  compare to " + supportedModes.size())    
    if (nextIndex >= supportedModes.size()) {nextIndex=0 }
    log.info("Going to nextMode with id =${nextIndex}")
    heaterToMode(nextIndex)
}

def getModeMap() { 
    def mm = null
    if (parent.getDataValue("includeSolar")=='true') {
    	mm =  ["OFF",
            "Heater",
        	"Solar Pref",
        	"Solar Only"
     	]
    }
    else {
     mm = 
    	[
        "OFF",
        "Heater"     
     	]
    }
    return mm
}

// These do NOT update poolController!!
def switchToModeID(id) {
 	log.info("Going to mode ID ${id}")
	def mm = getModeMap()
    log.debug("Map it via ${mm} = ${mm[id]}")
	switchToMode(mm[id])
}

def switchToMode(nextMode) {
 	log.debug("switchToMode from parent--> '${nextMode}'")
   	sendEvent(name: "thermostatMode", value: nextMode, displayed:true, descriptionText: "$device.displayName is in ${nextMode} mode")
}



// called by parent to me to change the mode locally in ST - these do NOT update poolController
def setThermostatMode(String value) {
	switchToMode(value)
}

def off() {
	switchToMode("Off")
}

def heat() {
	switchToMode("Heat")
}

// Command actions locally to update the poolController with a new mode from my commands
def heaterOn() {
	// set it to mode 1
    log.debug("HEATER ON ${device}")
	parent.heaterOn(device)
}

def heaterOff() {
	// set it to mode 0
	log.debug("HEATER OFF ${device}")
	parent.heaterOff(device)
}

def heaterToMode(modeID) {
	// mode is the code to pass to poolControl for this device to set the correct heater mode
	log.debug("HEATER ${device} to ${modeID}")
	parent.heaterSetMode(device, modeID)
}



def setTemperature(t) {
	log.debug(device.label + " current temp set to ${t}") 
    sendEvent(name: 'temperature', value: t, unit:"F")    
    log.debug(device.label + " DONE current temp set to ${t}") 
}

// Get stored temperature from currentState in current local scale
def getTempInLocalScale(state) {
	def temp = device.currentState(state)
	if (temp && temp.value && temp.unit) {
		return getTempInLocalScale(temp.value.toBigDecimal(), temp.unit)
	}
	return 0
}

// get/convert temperature to current local scale
def getTempInLocalScale(temp, scale) {
	if (temp && scale) {
		def scaledTemp = convertTemperatureIfNeeded(temp.toBigDecimal(), scale).toDouble()
		return (getTemperatureScale() == "F" ? scaledTemp.round(0).toInteger() : roundC(scaledTemp))
	}
	return 0
}

def getTempInDeviceScale(state) {
	def temp = device.currentState(state)
	if (temp && temp.value && temp.unit) {
		return getTempInDeviceScale(temp.value.toBigDecimal(), temp.unit)
	}
	return 0
}

def getTempInDeviceScale(temp, scale) {
	if (temp && scale) {
		def deviceScale = (state.scale == 1) ? "F" : "C"
		return (deviceScale == scale) ? temp :
				(deviceScale == "F" ? celsiusToFahrenheit(temp).toDouble().round(0).toInteger() : roundC(fahrenheitToCelsius(temp)))
	}
	return 0
}

def roundC (tempC) {
	return (Math.round(tempC.toDouble() * 2))/2
}
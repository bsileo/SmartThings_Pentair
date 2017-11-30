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
        
		command "switchMode"
		command "lowerHeatingSetpoint"
		command "raiseHeatingSetpoint"		
		command "poll"	
        command "heaterOn"
        command "heaterOff"
	}

	tiles {
		standardTile("mode", "device.thermostatMode", width:2, height:2, inactiveLabel: false, decoration: "flat") {
			state "off",  action:"heaterOn", nextState: "updating", icon: "st.thermostat.heating-cooling-off"
			state "heat", action:"heaterOff",  nextState: "updating", icon: "st.thermostat.heat"			
			state "updating", label:"Updating...", icon: "st.secondary.secondary"
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
	
	    standardTile("refresh", "device.thermostatMode", width:2, height:1, inactiveLabel: false, decoration: "flat") {
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
	// Only allow refresh every 2 minutes to prevent flooding the Zwave network
	def timeNow = now()
	if (!state.refreshTriggeredAt || (2 * 60 * 1000 < (timeNow - state.refreshTriggeredAt))) {
		state.refreshTriggeredAt = timeNow
		// use runIn with overwrite to prevent multiple DTH instances run before state.refreshTriggeredAt has been saved
		runIn(2, "pollDevice", [overwrite: true])
	}
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
				unit: getTemperatureScale(), eventType: "ENTITY_UPDATE", displayed: false)    
    parent.updateSetpoint(device,targetValue)
}

// set the local value for the heatingSetpoint. Doesd NOT update the parent / Pentair platform!!!
def setHeatingSetpoint(degrees) {
   //log.debug "setHeatingSetpoint " + device.deviceNetworkId + "-" + degrees
	def timeNow = now()
    if (degrees) {	
    	if (!state.heatingSetpointTriggeredAt || (1 * 2 * 1000 < (timeNow - state.heatingSetpointTriggeredAt))) {
			state.heatingSetpointTriggeredAt = timeNow               
			state.heatingSetpoint = degrees.toDouble()
			sendEvent(name: "heatingSetpoint", value:state.heatingSetpoint, unit: getTemperatureScale())    	
		}
	}
}


def switchMode() {
	def currentMode = device.currentValue("thermostatMode")
	def supportedModes = state.supportedModes
}

def switchToMode(nextMode) {
	def timeNow = now()
	if (!state.thermostatModeTriggeredAt || (1 * 2 * 1000 < (timeNow - state.thermostatModeTriggeredAt))) {
		state.thermostatModeTriggeredAt = timeNow        
       	sendEvent(name: "thermostatMode", value: nextMode,	isStateChange: true, descriptionText: "$device.displayName is in ${nextMode} mode")
    }
}

def getModeMap() { [
	"off": 0,
	"heat": 1,
]}

def setThermostatMode(String value) {
	switchToMode(value)
}

def off() {
	switchToMode("off")
}

def heat() {
	switchToMode("heat")
}

def heaterOn() {
	log.debug("HEATER ON ${device}")
	parent.heaterOn(device)
}

def heaterOff() {
	log.debug("HEATER OFF ${device}")
	parent.heaterOff(device)
}


def setTemperature(t) {
	log.debug(device.name + " set temp to ${t}") 
    sendEvent(name: 'temperature', value: t, unit:"F")
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
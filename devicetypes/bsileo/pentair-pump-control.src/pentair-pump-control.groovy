/**
 * Copyright 2019 Brad Sileo brad@sileo.name
 */
metadata {
	definition (name: "Pentair Pump Control", namespace: "bsileo", author: "Brad Sileo") {
	capability "Switch"
        capability "Switch Level"
        capability "Power Meter"
        command onConfirmed
        command offConfirmed
        attribute "friendlyName", "string"
        attribute "circuitID","string"
        attribute "pumpID","string"
	}

	// simulator metadata
	simulator {
		// status messages
		status "on": "switch:on"
		status "off": "switch:off"

		// reply messages
		reply "on": "switch:on"
		reply "off": "switch:off"
	}

	// UI tile definitions
	tiles (scale:2) {
		multiAttributeTile(name:"pump", type: "generic", width: 1, height: 1, canChangeIcon: true)  {
        	tileAttribute("device.switch", key: "PRIMARY_CONTROL") {
             	attributeState "off",  label:"Off", action:"on", nextState: "turningOn", icon: "http://cdn.device-icons.smartthings.com/Weather/weather12-icn@2x.png",backgroundColor: "#ffffff"
            	attributeState "on", label:"On", action:"off",  nextState: "turningOff", icon: "http://cdn.device-icons.smartthings.com/Weather/weather12-icn@2x.png",backgroundColor: "#00a0dc"
                attributeState "turningOn", label:'${name}', icon:"http://cdn.device-icons.smartthings.com/Weather/weather12-icn@2x.png", backgroundColor:"#00a0dc", nextState: "on"
                attributeState "turningOff", label:'${name}', icon:"http://cdn.device-icons.smartthings.com/Weather/weather12-icn@2x.png", backgroundColor:"#ffffff", nextState: "off"            	      
            }             
            // Note - this Approach works to display this name in the Child Device but does not carry through to the parent. Multi-attribute tiles do not work on a childTile??
            tileAttribute ("device.friendlyName", key: "SECONDARY_CONTROL") {
        		attributeState "name", label:'${currentValue}'
    		}		
        }
        controlTile("pumpSpeedControl", "device.level", "slider", height: 2, width: 4,  range:"(450..3450)") {
             state "level", 
             action:"switch level.setLevel"
        }
        valueTile("power", "device.power", decoration: "flat", width: 2, height: 2) {
            state "power", label:'${currentValue} Watts'
        }
    }
	main "pump"
	details "pump", "pumpSpeedControl", "power"
}

def installed() {
	log.debug("Installed Pump Control " + device.deviceNetworkId)
    manageData()
    manageChildren()       	
}

def updated() {
  manageData()
  manageChildren()
}

def manageChildren() {
}

def manageData() {
 	def cid = getDataValue("circuitID")
	sendEvent(name: "circuitID", value: cid, isStateChange: true, displayed: false)
    def pid = getDataValue("pumpID")
	sendEvent(name: "pumpID", value: pid, isStateChange: true, displayed: false)
    def name = getDataValue("friendlyName")
	sendEvent(name: "friendlyName", value: name, isStateChange: true, displayed: false)
}

def parsePumpData(pumpInfo) {
	log.debug("Pump Parse--${pumpInfo}")
    try {
         def programMode = pumpInfo['currentrunning']?.mode
         def programDuration = pumpInfo['currentrunning']?.remainingduration
         def programRPM = pumpInfo['currentrunning']?.value
         def friendlyName = pumpInfo.friendlyName
         def name = pumpInfo.name
         sendEvent(name: "friendlyName", value: friendlyName, isStateChange: true, displayed: false)         
         def mode = pumpInfo.mode
         def rpm = pumpInfo.rpm
         sendEvent(name: 'level', value: rpm)
         sendEvent(name: 'rpm', value: rpm)
         sendEvent(name: 'programLevel', value: programRPM)
         def watts = pumpInfo.watts
         sendEvent(name: 'power', value: watts)
     }
     catch (java.lang.ArrayIndexOutOfBoundsException e) {
           log.debug "Error! " + e   
    }
	
}

def onConfirmed() {
    //log.debug("CONF ${device} turned on")
	sendEvent(name: "switch", value: "on", displayed:true)    
}

def offConfirmed() {
	//log.debug("CONF ${device} turned off")
	sendEvent(name: "switch", value: "off", displayed:true)  
}

def on() {
	parent.setCircuit(getDataValue("circuitID"), 1)
    sendEvent(name: "switch", value: "turningOn", displayed:false,isStateChange:false)    
}

def off() {
	parent.setCircuit(getDataValue("circuitID"), 0)
    sendEvent(name: "switch", value: "turningOff", displayed:false,isStateChange:false)
}

def setLevel(speed) {
	def pid = getDataValue("pumpID")
	log.debug("Pump Control ${pid} set speed to ${speed}")
	parent.setPumpSpeed(pid, speed)
    sendEvent(name: "rpmProgramRequest", value: speed, displayed:true,isStateChange:false)
}
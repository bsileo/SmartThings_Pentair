/**
 *  Copyright 2017 Brad Sileo / Kevin Chartrand
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
	definition (name: "Pentair Intellichem", namespace: "bsileo", author: "Brad Sileo") {
		capability "Refresh"
		capability "sensor"
        capability "switch"
		capability "Health Check"
        capability "phMeasurement"
        
		attribute "pH", "string"
		attribute "ORP", "string"
		attribute "flowAlarm", "string"
		attribute "SI", "string"
		attribute "setpointpH", "string"
		attribute "setpointORP", "string"
		attribute "CYA", "string"
		attribute "CALCIUMHARDNESS", "string"
		attribute "TOTALALKALINITY", "string"
		attribute "tankpH", "string"
		attribute "tankORP", "string"
		attribute "modepH", "string"
		attribute "modeORP", "string"
        
		command "poll"	
        
	}

	tiles (scale:2) {
	    standardTile("chlorinate", "device.switch", width:1, height:1, inactiveLabel: false, decoration: "flat") {
			state "off",  action:"on", nextState: "updating", icon: "https://raw.githubusercontent.com/bsileo/SmartThings_Pentair/master/chlorine.png",backgroundColor: "#ffffff"
			state "on", action:"off",  nextState: "updating", icon: "https://raw.githubusercontent.com/bsileo/SmartThings_Pentair/master/chlorine.png",backgroundColor: "#00a0dc"
			state "updating", label:"Updating...", icon: "st.secondary.secondary"
		}
        
        valueTile("pH", "device.pH", width:1, height:1, decoration:"flat") {
            state("pH", label:'pH ${currentValue}',
		      backgroundColors:[							
			    [value: 0, color: "#153591"],
			    [value: 7.0, color: "#44b621"],
			    [value: 7.2, color: "#f1d801"],
			    [value: 7.4, color: "#d04e00"],
			    [value: 7.5, color: "#bc2323"]
              ]
           )
		}
        
        valueTile("ORP", "device.ORP", width:1, height:1, decoration:"flat") {
           state("ORP", label:'${currentValue}',
		      backgroundColors:[							
			    [value: 0, color: "#153591"],
			    [value: 700, color: "#44b621"],
			    [value: 720, color: "#f1d801"],
			    [value: 740, color: "#d04e00"],
			    [value: 750, color: "#bc2323"]
              ]
		   )
	    }  
  
	    valueTile("flowAlarm","flowAlarm",width:1, height:1, decoration:"flat")  {
        	state("flowAlarm", label:'${currentValue}') 
        }
        
        valueTile("SI","SI",width:1, height:1, decoration:"flat")  {
        	state("SI", label:'SI: ${currentValue}') 
        }
       
        valueTile("CYA","CYA",width:1, height:1, decoration:"flat")  {
        	state("CYA", label:'CYA: ${currentValue}') 
        }     
        
        valueTile("CALCIUMHARDNESS","CALCIUMHARDNESS",width:1, height:1, decoration:"flat")  {
        	state("CALCIUMHARDNESS", label:'CH: ${currentValue}') 
        }   
        
        valueTile("TOTALALKALINITY","TOTALALKALINITY",width:1, height:1, decoration:"flat")  {
        	state("TOTALALKALINITY", label:'TA: ${currentValue}') 
        }   
        
        controlTile("tankpH","device.tankpH","slider", width:1, height:1, decoration:"flat", range:"(0..6)")  {
        	state("tankpH", action:"setTankLevelpH", label:'${currentValue}%') 
        }   
        
        controlTile("tankORP", "device.tankORP", "slider", width:1, height:1, decoration:"flat", range:"(0..6)")  {
        	state("tankORP", action:"setTankLevelORP", label:'${currentValue}%') 
        }     
        
        valueTile("modepH","modepH",width:2, height:1, decoration:"flat")  {
        	state("modepH", label:'${currentValue}') 
        }   
        
        valueTile("modeORP","modeORP",width:2, height:1, decoration:"flat")  {
        	state("modeORP", label:'${currentValue}') 
        }    
        
	    standardTile("refresh", "device.thermostatMode", width:2, height:1, inactiveLabel: false, decoration: "flat") {
		    state "default", action:"refresh.refresh", icon:"st.secondary.refresh"
	    }
/*        
        valueTile("setpointpH","setpointpH",width:1, height:1, decoration:"flat")  {
        	state("setpointpH", label:'pH: ${currentValue}') 
        }
        
        valueTile("setpointORP","setpointORP",width:2, height:1, decoration:"flat")  {
        	state("setpointORP", label:'ORP: ${currentValue}') 
        }
*/ 
        controlTile("setpointpH","device.setpointpH","slider", width:1, height:1, decoration:"flat", range:"(72..76)")  {
        	state("setpointpH", action:"setSetpointpH", label:'pH ${currentValue}') 
        }

        controlTile("setpointORP","device.setpointORP","slider", width:1, height:1, decoration:"flat", range:"(40..80)")  {
        	state("setpointORP", action:"setSetpointORP", label:'ORP ${currentValue}0') 
        }

        standardTile("lowerORPSetpoint", "device.ORPSetpoint", width:2, height:1, inactiveLabel: false, decoration: "flat") {
			state "ORPSetpoint", action:"lowerORPSetpoint", icon:"st.thermostat.thermostat-left"
		}
        
		standardTile("raiseORPSetpoint", "device.ORPSetpoint", width:2, height:1, inactiveLabel: false, decoration: "flat") {
			state "ORPSetpoint", action:"raiseORPSetpoint", icon:"st.thermostat.thermostat-right"
		}
        
        standardTile("lowerpHSetpoint", "device.pHSetpoint", width:2, height:1, inactiveLabel: false, decoration: "flat") {
			state "pHSetpoint", action:"lowerpHSetpoint", icon:"st.thermostat.thermostat-left"
		}
        
		standardTile("raisepHSetpoint", "device.pHSetpoint", width:2, height:1, inactiveLabel: false, decoration: "flat") {
			state "pHSetpoint", action:"raisepHSetpoint", icon:"st.thermostat.thermostat-right"
		}        
        
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

}

def parse(json)
{
	def group;
    def subjson;
    def name;
    def val;
	json.each { g, s ->      	 
         group = g;
         subjson = s;
         switch (group) {
        	case "readings":
            	subjson.each { k, v ->
                	switch (k) {
                    	case "PH":
                            sendEvent(name: "pH", value: v) 
                          break;
                        case "ORP":
                            sendEvent(name: "ORP", value: v) 
                          break;
                        case "WATERFLOW":
                        	val = v ? "NO FLOW": "Flow OK"
                            sendEvent(name: "flowAlarm", value: val) 
                          break;
                        case "SI":
                            sendEvent(name: "SI", value: v) 
                          break;
                    }
                }
              break;
            case "settings":
            	subjson.each { k, v ->
                	switch (k) {
                    	case "PH":
                            sendEvent(name: "setpointpH", value: (v*10)) 
                          break;
                        case "ORP":
                            sendEvent(name: "setpointORP", value: (v/10)) 
                          break;
                        case "CYA":
                            sendEvent(name: "CYA", value: v) 
                          break;
                        case "CALCIUMHARDNESS":
                            sendEvent(name: "CALCIUMHARDNESS", value: v) 
                          break;
                        case "TOTALALKALINITY":
                            sendEvent(name: "TOTALALKALINITY", value: v) 
                          break;
                    }
                }
              break;
            case "tankLevels":
                subjson.each { k, v ->
                	switch (k) {
                    	case "1":
                			sendEvent(name: "tankpH", value: v) 
              	  	      break;
                    	case "2":
                			sendEvent(name: "tankORP", value: v) 
              	  	      break;
                     }
                 }
              break;
            case "mode":
                subjson.each { k, v ->
                	switch (k) {
                    	case "1":
                          switch (v) {
                            case "85":
                              sendEvent(name: "modepH", value: "Mixing")
                            break;
                            case "21":
                              sendEvent(name: "modepH", value: "Dosing")
                            break;
                            case "101":
                              sendEvent(name: "modepH", value: "Monitoring")
                            break;
                            default:
                              sendEvent(name: "modepH", value: v)                            
                            break;
                            }
              	  	      break;
                    	case "2":
                          switch (v) {
							case "32":
                              sendEvent(name: "modeORP", value: "Mixing")
                            break;
                            case "34":
                              sendEvent(name: "modeORP", value: "Dosing")
                            break;
                            default:
                              sendEvent(name: "modeORP", value: v)                            
                            break;   
                          }
              	  	      break;  
                    }
                }
              break;
                
                
                
                
/*                
                switch 
            	def sp = json.outputSpaPercent + "%"
            	val = "${v}% / " + sp
            	name = "poolSpaSetpoint"
              break; 
            case "settings":
            	val = v ? "Yes": "No"
              break;
            case "tankLevels":
                def sw = v==0 ? "off":"on"
                log.debug("SWITCH ${sw}")
            	sendEvent(name: "switch", value: sw ) 
              break;
*/
        }
        //log.debug("EVT for ${k}->${v} == [${name}] -> {$val})")
        //sendEvent(name: name, value: val) 
	}
    
}

/**
*  "intellichem":{
*    "readings":{
*      "PH":7.4,
*      "ORP":691,
*      "WATERFLOW":0,
*      "SI":-0.3},
*    "settings":{
*      "PH":7.4,
*      "ORP":700,
*      "CYA":3,
*      "CALCIUMHARDNESS":400,
*      "TOTALALKALINITY":150},
*    "tankLevels":{
*      "1":5,
*      "2":3},
*    "mode":{"1":85,"2":32},
*    "lastPacket":[165,0,16,144,18,41,2,228,2,
*                  179,2,228,2,188,0,0,0,2,0,0,
*                  0,29,0,4,0,63,5,3,244,1,144,
*                  0,3,0,150,20,0,52,0,0,85,32,
*                  60,1,0,0,0,8,65]}}
*/


// Command Implementations
def poll() {
	// Call refresh which will cap the polling to once every 2 minutes
	refresh()
}

def refresh() {
	// Only allow refresh every 1 minutes to prevent flooding the Zwave network
	def timeNow = now()
	if (!state.refreshTriggeredAt || (1 * 60 * 1000 < (timeNow - state.refreshTriggeredAt))) {
		state.refreshTriggeredAt = timeNow
		// use runIn with overwrite to prevent multiple DTH instances run before state.refreshTriggeredAt has been saved
		runIn(2, "pollDevice", [overwrite: true])
	}
}

def pollDevice() {
    parent.poll()
}

// set the local value for the heatingSetpoint. Doesd NOT update the parent / Pentair platform!!!
def setORPSetpoint(v) {
   //log.debug "setHeatingSetpoint " + device.deviceNetworkId + "-" + degrees
	def timeNow = now()
    if (v) {	
    	if (!state.heatingSetpointTriggeredAt || (1 * 2 * 1000 < (timeNow - state.ORPSetpointTriggeredAt))) {
			state.ORPSetpointTriggeredAt = timeNow               
			sendEvent(name: "ORPSetpoint", value:(state.ORPSetpoint*10), eventType: "ENTITY_UPDATE", displayed: true)    	
            parent.setORPSetpoint(state.ORPSetpoint*10)
		}
	}
}

// set the local value for the heatingSetpoint. Doesd NOT update the parent / Pentair platform!!!
def setpHSetpoint(v) {
	log.debug "setpHSetpoint " + v
	state.pHSetpointTriggeredAt = timeNow               
	sendEvent(name: "pHSetpoint", value:(state.pHSetpoint/10), eventType: "ENTITY_UPDATE", displayed: true)
    parent.setpHSetpoint(state.pHSetpoint/10)
}

def setTankLevelpH(level) {
	sendEvent("name": "tankpH", "value": level, eventType: "ENTITY_UPDATE", displayed: true)    
    parent.updateTankpH(device,level)
}

def setTankLevelORP(level) {
	sendEvent("name": "tankORP", "value": level, eventType: "ENTITY_UPDATE", displayed: true)    
    parent.updateTankORP(device,level)
}

/*
def on() {
	parent.chlorinatorOn()
}

def off() {
    parent.chlorinatorOff()
}
*/
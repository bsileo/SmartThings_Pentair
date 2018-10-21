import groovy.util.Eval;


metadata {
	definition (name: "Pentair Pool Controller", namespace: "bsileo", author: "Brad Sileo") {
       capability "Polling"
       capability "Refresh"
       capability "Configuration"
       capability "Switch"
       capability "Actuator"
       capability "Sensor"
       attribute "poolPump","string"
       attribute "spaPump","string"
       attribute "valve","string"       
       command "poolPumpOn"
       command "poolPumpOff"
       command "spaPumpOn"
       command "spaPumpOff"
    }

	preferences {       
        section("Configuration") {
          input "mainSwitchMode", "enum", title: "Main Tile Mode", required:true,  displayDuringSetup: true , options: ["Pool Light","Pool Pump","Spa Pump"], description:"Select what feature to control with the main tile"
        }
	}
	tiles(scale: 2) {
       
        childDeviceTile("poolTemp", "poolHeat", height:2,width:2,childTileName:"temperature")                
        standardTile("mainSwitch", "device.switch", height:1,width:1,inactiveLabel: false,canChangeIcon: true) {
            state "off", label: "off", icon: "st.Lighting.light1", backgroundColor: "#ffffff", action: "switch.on", nextState: "on"
 			state "on", label: "on", icon: "st.Lighting.light1", backgroundColor: "#00a0dc", action: "switch.off", nextState: "off"
            state "updating", label:"Updating...", icon: "st.Lighting.light13"
        }
        childDeviceTile("PoolHeatmode", "poolHeat", height:1,width:2,childTileName:"mode")
		standardTile("poolPump", "device.poolPump", width:2, height:1, inactiveLabel: false, decoration: "flat") {
			state "off",  label:"Off", action:"poolPumpOn", nextState: "updating", icon: "http://cdn.device-icons.smartthings.com/Health & Wellness/health2-icn@2x.png"
			state "on", label:"On", action:"poolPumpOff",  nextState: "updating", icon: "http://cdn.device-icons.smartthings.com/Health & Wellness/health2-icn@2x.png"			
			state "updating", label:"Updating...", icon: "http://cdn.device-icons.smartthings.com/Health & Wellness/health2-icn@2x.png"
		}      
        
        childDeviceTile("PoolHeatlower", "poolHeat", height:1,width:1,childTileName:"lowerHeatingSetpoint")
        childDeviceTile("PoolHeatset", "poolHeat", height:1,width:2,childTileName:"heatingSetpoint")
        childDeviceTile("PoolHeatraise", "poolHeat", height:1,width:1,childTileName:"raiseHeatingSetpoint")
      
        childDeviceTile("spaTemp", "spaHeat", height:2,width:2,childTileName:"temperature")        
        childDeviceTile("SpaHeatmode", "spaHeat", height:1,width:2,childTileName:"mode")           
        childDeviceTile("spaPump", "spaPump", height:1,width:2,childTileName:"switch")           
        
        childDeviceTile("SpaHeatlower", "spaHeat", height:1,width:1,childTileName:"lowerHeatingSetpoint")
        childDeviceTile("SpaHeatset", "spaHeat", height:1,width:2,childTileName:"heatingSetpoint")
        childDeviceTile("SpaHeatraise", "spaHeat", height:1,width:1,childTileName:"raiseHeatingSetpoint")
        
        //Always SPA so do not display here (??)
        // childDeviceTile("Aux 1 Switch", "circuit1", height:1,width:1,childTileName:"switch")    
        
        childDeviceTile("Aux 2 Switch", "circuit2", height:1,width:1,childTileName:"switch")    
        childDeviceTile("Aux 3 Switch", "circuit3", height:1,width:1,childTileName:"switch")    
        childDeviceTile("Aux 4 Switch", "circuit4", height:1,width:1,childTileName:"switch")                        
        childDeviceTile("Aux 5 Switch", "circuit5", height:1,width:1,childTileName:"switch")    
        //Always Pool so do not display here (??)
        //childDeviceTile("Aux 6 Switch", "circuit6", height:1,width:1,childTileName:"switch")                    
        childDeviceTile("Aux 7 Switch", "circuit7", height:1,width:1,childTileName:"switch")    
        childDeviceTile("Aux 8 Switch", "circuit8", height:1,width:1,childTileName:"switch")    
        for (i in 9..20) {
        	childDeviceTile("Aux ${i} Switch", "circuit${i}", height:1,width:1,childTileName:"switch")    
        }
        
               
        childDeviceTile("airTemp", "airTemp", height:1,width:2,childTileName:"temperature")     
        
        // Always have only one of the 2 below
        childDeviceTile("solarTemp", "solarTemp", height:1,width:2,childTileName:"temperature")
        childDeviceTile("solarDummy", "solarDummy", height:1,width:2,childTileName:"dummy")        
        
        valueTile("valve","valve",width:1, height:1, decoration:"flat")  {
        	state("valve", label:' Valve: ${currentValue}') 
        } 
        standardTile("refresh", "device.refresh", height:1,width:1,inactiveLabel: false) {
                state "default", label:'Refresh', action:"refresh.refresh",  icon:"st.secondary.refresh-icon"
        }
   
   		section (hideable:true, hidden:true, "chlorinator") {
            childDeviceTile("saltPPM","poolChlorinator", height:2,width:2,childTileName:"saltPPM")
            childDeviceTile("chlorinateSwitch","poolChlorinator", height:1,width:1,childTileName:"chlorinate")
            childDeviceTile("currentOutput","poolChlorinator", height:1,width:1,childTileName:"currentOutput")
            childDeviceTile("poolSpaSetpoint","poolChlorinator", height:1,width:2,childTileName:"poolSpaSetpoint")
            childDeviceTile("superChlorinate","poolChlorinator", height:1,width:1,childTileName:"superChlorinate")
            childDeviceTile("status","poolChlorinator", height:1,width:3,childTileName:"status")
		}
		//KJC added intellichem section  
        section (hideable:true, hidden:true, "intellichem") {
            childDeviceTile("ORP","poolIntellichem", height:2,width:2,childTileName:"ORP")
            childDeviceTile("modeORP","poolIntellichem", height:1,width:2,childTileName:"modeORP")
            childDeviceTile("tankORP","poolIntellichem", height:1,width:2,childTileName:"tankORP")

            childDeviceTile("ORPSetLower", "poolIntellichem", height:1,width:1,childTileName:"lowerORPSetpoint")
            childDeviceTile("setpointORP","poolIntellichem", height:1,width:2,childTileName:"setpointORP")
            childDeviceTile("ORPSetRaise", "poolIntellichem", height:1,width:1,childTileName:"raiseORPSetpoint")


            childDeviceTile("pH","poolIntellichem", height:2,width:2,childTileName:"pH")
            childDeviceTile("modepH","poolIntellichem", height:1,width:2,childTileName:"modepH")
            childDeviceTile("tankpH","poolIntellichem", height:1,width:2,childTileName:"tankpH")

            childDeviceTile("pHSetLower", "poolIntellichem", height:1,width:1,childTileName:"lowerpHSetpoint")
            childDeviceTile("setpointpH","poolIntellichem", height:1,width:2,childTileName:"setpointpH")
            childDeviceTile("pHSetRaise", "poolIntellichem", height:1,width:1,childTileName:"raisepHSetpoint")   

            childDeviceTile("SI","poolIntellichem", height:2,width:2,childTileName:"SI")      
            childDeviceTile("flowAlarm","poolIntellichem", height:1,width:2,childTileName:"flowAlarm")
            childDeviceTile("CYA","poolIntellichem", height:1,width:2,childTileName:"CYA")
            childDeviceTile("CALCIUMHARDNESS","poolIntellichem", height:1,width:2,childTileName:"CALCIUMHARDNESS")
            childDeviceTile("TOTALALKALINITY","poolIntellichem", height:1,width:2,childTileName:"TOTALALKALINITY")
        }
        
        // Aux Circuits must be manually adjusted for exclusion below since ST does not allow any dynamic processing.  
        // The system will automatically include only the Aux circuits that exist so listing extras below will not impact the User Interface but
        // you must manually remove any entries that DO exist in your poolController configuration (e.g. poolcontroll:30000/config )
        // you do not want to see as the default configuration will display all circuits that exist
        
        main "mainSwitch"
        details "poolTemp","PoolHeatmode","poolPump","PoolHeatlower","PoolHeatset","PoolHeatraise",
                "spaTemp","SpaHeatmode","spaPump","SpaHeatlower","SpaHeatset","SpaHeatraise",                
                "airTemp","solarTemp","solarDummy","valve","refresh",
                "saltPPM","chlorinateSwitch","currentOutput","poolSpaSetpoint","superChlorinate","status",
                "ORP","modeORP","tankORP","ORPSetLower","setpointORP","ORPSetRaise","pH","modepH","tankpH","pHSetLower","setpointpH","pHSetRaise","SI","flowAlarm","CYA","CALCIUMHARDNESS","TOTALALKALINITY",
                "Aux 2 Switch","Aux 3 Switch","Aux 4 Switch","Aux 5 Switch","Aux 7 Switch","Aux 8 Switch",
                "Aux 9 Switch","Aux 10 Switch","Aux 11 Switch","Aux 12 Switch","Aux 13 Switch","Aux 14 Switch",
                "Aux 15 Switch","Aux 16 Switch","Aux 17 Switch","Aux 18 Switch","Aux 19 Switch","Aux 20 Switch"             
	}
}

def configure() {
  log.debug "Executing 'configure()'"
  updateDeviceNetworkID()
}

def installed() {
	manageChildren()    
}

def updated() {
  manageChildren()
  if (!state.updatedLastRanAt || now() >= state.updatedLastRanAt + 5000) {
    state.updatedLastRanAt = now()
    log.debug "Executing 'updated()'"
    runIn(3, "updateDeviceNetworkID")
  } else {
    log.trace "updated(): Ran within last 5 seconds so aborting."
  }  
}

def manageChildren() {
	//log.debug "manageChildren..."
	def hub = location.hubs[0]    
    def poolHeat = childDevices.find({it.deviceNetworkId == getChildDNI("poolHeat")})
    if (!poolHeat) {
        poolHeat = addChildDevice("bsileo","Pentair Water Thermostat", getChildDNI("poolHeat"), hub.id, 
                                  [completedSetup: true, label: "${device.displayName} (Pool Heat)" , isComponent:false, componentName: "poolHeat", componentLabel:"${device.displayName} (Pool Heat)" ])
        log.debug "Created PoolHeat" 
    }
    if (getDataValue("includeSpa")=='true') {
        def spaHeat = childDevices.find({it.deviceNetworkId == getChildDNI("spaHeat")})
        if (!spaHeat) {
            spaHeat = addChildDevice("bsileo","Pentair Water Thermostat", getChildDNI("spaHeat"), hub.id, 
                                     [completedSetup: true, label: "${device.displayName} (Spa Heat)" , isComponent:false, componentName: "spaHeat", componentLabel:"${device.displayName} (Spa Heat)" ])
            log.debug "Created SpaHeat"
        }
        def spaPump = childDevices.find({it.deviceNetworkId == getChildDNI("spaPump")})
        if (!spaPump) {
            spaHeat = addChildDevice("bsileo","Pentair Spa Pump Control", getChildDNI("spaPump"), hub.id, 
                                     [completedSetup: true, label: "${device.displayName} (Spa Pump)" , isComponent:false, componentName: "spaPump", componentLabel:"${device.displayName} (Spa Pump)" ])
            log.debug "Created SpaPump Child"
        }
    }    
    
    manageCircuits()


    def airTemp = childDevices.find({it.deviceNetworkId == getChildDNI("airTemp")})
    if (!airTemp) {
        airTemp = addChildDevice("bsileo","Pentair Temperature Measurement Capability", getChildDNI("airTemp"), hub.id, 
                                 [ label: "${device.displayName} Air Temperature", componentName: "airTemp", componentLabel: "${device.displayName} Air Temperature",
                                  isComponent:false, completedSetup:true])                	
    }

    
    if (getDataValue("includeSolar")=='true') {    
    	def solarTemp = childDevices.find({it.deviceNetworkId == getChildDNI("solarTemp")})        
    	if (!solarTemp) {
    		log.debug("Create Solar temp")
        	solarTemp = addChildDevice("bsileo","Pentair Temperature Measurement Capability", getChildDNI("solarTemp"), hub.id, 
                                   [ label: "${device.displayName} Solar Temperature", componentName: "solarTemp", componentLabel: "${device.displayName} Solar Temperature",
                                    isComponent:false, completedSetup:true])        
    	}
    }
    else {
    	 def solarTemp = childDevices.find({it.deviceNetworkId == getChildDNI("solarDummy")})
         if (!solarTemp) {
    		log.debug("Create Solar dummy")
        	solarTemp = addChildDevice("bsileo","Pentair Dummy Tile", getChildDNI("solarDummy"), hub.id, 
                                   [ label: "${device.displayName} Solar Dummy", componentName: "solarDummy", componentLabel: "${device.displayName} Solar Dummy",
                                    isComponent:false, completedSetup:true])
         }
    }
    
    

    def ichlor = childDevices.find({it.deviceNetworkId == getChildDNI("poolChlorinator")})
    if (!ichlor && getDataValue("includeChlorinator")=='true') {
    	log.debug("Create Chlorinator")
        ichlor = addChildDevice("bsileo","Pentair Chlorinator", getChildDNI("poolChlorinator"), hub.id, 
                                [ label: "${device.displayName} Chlorinator", componentName: "poolChlorinator", componentLabel: "${device.displayName} Chlorinator",
                                 isComponent:true, completedSetup:true])        
    }  
    def ichem = childDevices.find({it.deviceNetworkId == getChildDNI("poolIntellichem")})
    if (!ichem && getDataValue("includeIntellichem")=='true') {          
        ichem = addChildDevice("bsileo","Pentair Intellichem", getChildDNI("poolIntellichem"), hub.id, 
                               [ label: "${device.displayName} Intellichem", componentName: "poolIntellichem", componentLabel: "${device.displayName} Intellichem",
                                isComponent:false, completedSetup:true])  
    }   
}

def manageCircuits() {
	log.debug "Create/Update Circuits for this device"
	manageFeatureCircuits()
    manageLightCircuits()
}


def manageFeatureCircuits() {
	def hub = location.hubs[0]   
    def nLCircuits = parent.state.nonLightCircuits
    nLCircuits.each {i,k ->
    	def cData = parent.state.circuitData[i.toString()]
        if (cData.friendlyName == "NOT USED") return        
        def auxname = "circuit${i}"        
        def auxLabel = "${device.displayName} (${cData.friendlyName})"        
        try {
            def auxButton = childDevices.find({it.deviceNetworkId == getChildDNI(auxname)})
            if (!auxButton) {
            	log.info "Create Aux Circuit switch ${auxLabel} Named=${auxname}" 
                auxButton = addChildDevice("bsileo","Pentair Pool Control Switch", getChildDNI(auxname), hub.id, 
                                           [completedSetup: true, label: auxLabel , isComponent:false, componentName: auxname, componentLabel: auxLabel, 
                                           data: [type:cData.circuitFunction]
                                           ])
                log.debug "Success - Created Aux switch ${i}" 
            }
            else {
                log.info "Found existing Aux Switch ${i} - No Updates Supported" 
            }
        }
        catch(physicalgraph.app.exception.UnknownDeviceTypeException e)
        {
            log.debug "Error! " + e                                                                
        }
    }
}

def manageLightCircuits() {
	def hub = location.hubs[0]
     def lCircuits = parent.state.lightCircuits    
    lCircuits.each {i,k ->
        def auxname = "circuit${i}"        
        def auxLabel = "${device.displayName} (${k.circuitName})"        
        try {
            def auxButton = childDevices.find({it.deviceNetworkId == getChildDNI(auxname)})
            if (!auxButton) {
            	log.info "Create Light switch ${auxLabel} Named=${auxname}" 
                auxButton = addChildDevice("bsileo","Pentair Pool Light Switch", getChildDNI(auxname), hub.id, 
                                           [completedSetup: true, label: auxLabel , isComponent:false, componentName: auxname, componentLabel: auxLabel])
                log.debug "Success - Created Light switch ${i}" 
            }
            else {
                log.info "Found existing Aux Light Switch ${i} - No Updates Supported" 
            }
        }
        catch(physicalgraph.app.exception.UnknownDeviceTypeException e)
        {
            log.debug "Error! " + e                                                                
        }

    }
}

def refresh() {
    log.info "Requested a refresh"
    poll()
}

def poll() {
  sendEthernet("/all")
}

def parse(String description) {  
  //log.debug "Executing parse()"
  def msg = parseLanMessage(description)
  log.debug "Full msg: ${msg}"
  //log.debug "HEADERS: ${msg.headers}"
  //log.debug "JSON: ${msg.json}"
  //log.debug "x-event: ${msg.headers['x-event']}"
  //log.debug "msg.JSON.Circuits: ${msg.json.circuit}"
  //log.debug "msg.JSON.Time: ${msg.json.time}"
  //log.debug "msg.JSON.Temp: ${msg.json.temperature}"
  //log.debug "msg.JSON.Chem: ${msg.json.intellichem}"

  if (msg.json.temperature != null) {parseTemps(msg.json.temperature)} else {log.debug("no Temps in msg")}
  if (msg.json.circuit != null){ parseCircuits(msg.json.circuit)} else {log.debug("no Circuits in msg")}
  if (msg.json.time != null) {parseTime(msg.json.time)} else {log.debug("no Time in msg")}
  if (msg.json.schedule != null) {parseSchedule(msg.json.schedule)} else {log.debug("no Schedule in msg")}
  if (msg.json.pump != null) {parsePump(msg.json.pump)} else {log.debug("no Pumps in msg")}
  if (msg.json.valve != null) {parseValve(msg.json.valve)} else {log.debug("no Valve in msg")}     
  if (msg.json.chlorinator != null) {parseChlorinator(msg.json.chlorinator)} else {log.debug("no Chlor in msg")}
  if (msg.json.intellichem != null) {parseIntellichem(msg.json.intellichem)} else {log.debug("no Chem in msg")}

}

def parseTime(msg) {
	log.info("Parse Time: ${msg}")
}
def parsePump(msg) {
	log.info("Parse Schedule: ${msg}")
}
def parseSchedule(msg) {
	log.info("Parse Schedule: ${msg}")
}
def parseValve(msg) {
	log.info("Parse Valve: ${msg}")
    sendEvent(name: "valve", value: msg.valves)            
}
def parseIntellichem(msg) {
	log.info("Parse Intellichem: ${msg}")
    childDevices.find({it.deviceNetworkId == "poolIntellichem"})?.parse(msg)
}
 

def parseCircuits(msg) {   
	log.info("Parse Circuits: ${msg}")
    msg.each {         
         def child = getChildCircuit(it.key)
         //log.debug "CIR JSON:${it.key}==${it.value}::${child}"
         if (child) {
            def stat = it.value.status ? it.value.status : 0         
            def status = stat == 0 ? "off" : "on"
            //log.debug "Child==${child} --> ${stat}"
            def mainID = getMainModeID()
            def currentID = toIntOrNull(it.key)
         	if (stat == 0) { 
                child.offConfirmed() 
             } 
            else { 
               child.onConfirmed()
            };
            if (currentID == poolPumpCircuitID()) { 
                sendEvent(name: "poolPump", value: status, displayed:true)            
            }
            if (currentID == spaPumpCircuitID()) { 
            	sendEvent(name: "spaPump", value: status, displayed:true)
                def spaPump = getSpaPumpChild()
                if (stat == 0) { 
                	spaPump.offConfirmed() 
             	} 
            	else { 
               		spaPump.onConfirmed()
            	};
            }
            if (currentID == mainID) { 
            	sendEvent(name: "switch", value: status, displayed:true)            
            }
     		child.setCircuitFunction("${it.value.circuitFunction}")
            child.setFriendlyName("${it.value.friendlyName}")               

            sendEvent(name: "circuit${currentID}", value:status, 
             				displayed:true, descriptionText:"Circuit ${child.label} set to ${status}" 
                            )            
  
         }
      }     
}

def getChildCircuit(id) {
	// get the circuit device given the ID number only (e.g. 1,2,3,4,5,6)
    //log.debug "CHECK getChildCircuit:${id}"
	def children = getChildDevices()
    def cname = 'circuit' + id
	def dni = getChildDNI(cname)
    //return childDevices.find {it.deviceNetworkId == dni}
    
    def theChild
    children.each { child ->
    	//log.debug "CHECK Child for :${dni}==${child}::" + child.deviceNetworkId
        if (child.deviceNetworkId == dni) { 
          //log.debug "HIT Child for :${id}==${child}"
          theChild = child          
        }
    }
    return theChild
    
}

def getPoolPumpChild() {
	return childDevices.find({it.deviceNetworkId == getChildDNI("poolPump")})
}

def getPoolHeatChild() {
	return childDevices.find({it.deviceNetworkId == getChildDNI("poolHeat")})
}


def getSpaPumpChild() {
	return childDevices.find({it.deviceNetworkId == getChildDNI("spaPump")})
}

def getSpaHeatChild() {
	return childDevices.find({it.deviceNetworkId == getChildDNI("spaHeat")})
}


def getChildDNI(name) {
	return getDataValue("controllerMac") + "-" + name
}

def parseTemps(msg) {
    log.info("Parse Temps ${msg}")
    def ph=childDevices.find({it.deviceNetworkId == getChildDNI("poolHeat")})
    def sh=childDevices.find({it.deviceNetworkId == getChildDNI("spaHeat")})
    def at = childDevices.find({it.deviceNetworkId == getChildDNI("airTemp")})
    def st = childDevices.find({it.deviceNetworkId == getChildDNI("solarTemp")})
    
    msg.each {k, v ->        	         
         //log.debug "TEMP Key:${k}  Val:${v}"
         switch (k) {
        	case "poolTemp":            	
            	ph?.setTemperature(v)
            	break
        	case "spaTemp":
            	sh?.setTemperature(v)
            	break
        	case "airTemp":            	
                at?.setTemperature(v)
            	break
        	case "solarTemp":
                st?.setTemperature(v)
            	break
        	case "poolSetPoint":            	
                ph?.setHeatingSetpoint(v)
            	break
            case "spaSetPoint":
            	sh?.setHeatingSetpoint(v)
            	break
        	case "poolHeatMode":
                ph?.switchToModeID(v)                            	
                break
            case "spaHeatMode":
            	sh?.switchToModeID(v)
                break
            default:
            	sendEvent(name: k, value: v, displayed:false)
            	break
          }
	}
}

def parseChlorinator(msg) {
	log.info('Parse Chlor')
    childDevices.find({it.deviceNetworkId == getChildDNI("poolChlorinator")})?.parse(msg)
}

def on() {
	if (mainSwitchMode == 'Pool Light') {
		return setCircuit(lightCircuitID(),1)
    }
    else if (mainSwitchMode == 'Pool Pump') {
    	poolPumpOn()
    }
    else if (mainSwitchMode == 'Spa Pump') {
    	spaPumpOn()
    }
}

def off() {
	if (mainSwitchMode == 'Pool Light') {
		return setCircuit(lightCircuitID(),0)
    }
    else if (mainSwitchMode == 'Pool Pump') {
    	poolPumpOff()
    }
    else if (mainSwitchMode == 'Spa Pump') {
    	spaPumpOff()
    }	
}

def getMainModeID() {
	if (mainSwitchMode == 'Pool Light') {
		return lightCircuitID()
    }
    else if (mainSwitchMode == 'Pool Pump') {
    	return poolPumpCircuitID()
    }
    else if (mainSwitchMode == 'Spa Pump') {
    	return spaPumpCircuitID()
    }
}

def chlorinatorOn() {  
  return chlorinatorOn(70)
}

def chlorinatorOn(level) {  
  return sendEthernet("/chlorinator/${level}")
}


def chlorinatorOff() {  
  return sendEthernet("/chlorinator/0")
}


def poolPumpOn() {	
	return setCircuit(poolPumpCircuitID(),1)
}

def poolPumpOff() {
	return setCircuit(poolPumpCircuitID(),0)
}

def spaPumpOn() {
	log.debug "SpaPump ON"
	return setCircuit(spaPumpCircuitID(),1)
}

def spaPumpOff() {
	return setCircuit(spaPumpCircuitID(),0)
}

def lightCircuitID() {
	//log.debug("Get LIGHTS child " + childofType("Intellibrite")?.deviceNetworkId)    
	return childCircuitID(childofType("Intellibrite")?.deviceNetworkId)
}

def poolPumpCircuitID() {
	//log.debug("Get Pool child-"+childofType("Pool")?.deviceNetworkId)
	return childCircuitID(childofType("Pool")?.deviceNetworkId)
}

def spaPumpCircuitID() {
	//log.debug("Get Spa child-"+childofType("Spa")?.deviceNetworkId)
	return childCircuitID(childofType("Spa")?.deviceNetworkId)
}

def childofType(type) {
    //return childDevices.find({it.currentFriendlyName == type})
    return childDevices.find({it.currentcircuitFunction == type})
}

def childOn(cir_name) {
	//log.debug "Got on Request from ${cir_name}"
    def id = childCircuitID(cir_name)
	return setCircuit(id,1)
}

def childOff(cir_name) {
	//log.debug "Got off from ${cir_name}"
	def id = childCircuitID(cir_name)
	return setCircuit(id,0)
}

def childCircuitID(cirName) {
	//log.debug("CCID---${cirName}")
	return toIntOrNull(cirName?.split('-')?.getAt(1)?.substring(7))
}

def setCircuit(circuit, state) {
  log.debug "Executing 'set(${circuit}, ${state})'"
  sendEthernet("/circuit/${circuit}/set/${state}")
}

// **********************************
// Heater control functions to update the current heater state / setpoints on the poolController. 
// spdevice is the child device with the correct DNI to use in referecing SPA or POOL
// **********************************
def heaterOn(spDevice) {
  //log.debug "Executing 'heater on for ${spDevice}'"
  def tag = spDevice.deviceNetworkId.toLowerCase().split("-")[1]
  sendEthernet("/${tag}/mode/1")
}

def heaterOff(spDevice) {
	//log.debug "Executing 'heater off for ${spDevice}'"
    def tag = spDevice.deviceNetworkId.toLowerCase().split("-")[1]
    sendEthernet("/${tag}/mode/0")
}

def heaterSetMode(spDevice, mode) {
  log.debug "Executing 'heater on for ${spDevice}'"
  def tag = spDevice.deviceNetworkId.toLowerCase().split("-")[1]
  sendEthernet("/${tag}/mode/${mode}", heaterModeCallback)
}

def updateSetpoint(spDevice,setPoint) {
  	def tag = spDevice.deviceNetworkId.toLowerCase().split("-")[1]
	sendEthernet("/${tag}/setpoint/${setPoint}")
}

def heaterModeCallback(physicalgraph.device.HubResponse hubResponse) {
    log.debug "Entered heaterModeCallback()..."
	def msg = hubResponse.json    
    //log.debug "Full msg: ${msg}"  
    //log.debug "Heater status = ${msg.status}"    
    //log.debug "${msg.text} -> indexOf:" + msg.text.indexOf('spa')
    if (msg.text.indexOf('spa') > 0) {
    	def ph=getSpaHeatChild()
        log.info("Update Spa heater to ${msg.status}")
    	sh?.switchToMode(msg.status)
    }   
    else {
    	def ph=getPoolHeatChild()
        log.info("Update Pool heater to ${msg.status}")
    	ph?.switchToMode(msg.status)
    }
}



// INTERNAL Methods
private sendEthernet(message) {
	sendEthernet(message,null)
}

private sendEthernet(message, aCallback) {
  def ip = getDataValue('controllerIP')
  def port = getDataValue('controllerPort')
  //log.debug "Try for 'sendEthernet' http://${ip}:${port}${message}"
  if (ip != null && port != null) {
    log.info "SEND http://${ip}:${port}${message}"
    sendHubCommand(new physicalgraph.device.HubAction(
        [
         	method: "GET",
         	path: "${message}",
         	//protocol: Protocol.LAN,
         	headers: [
              	HOST: "${ip}:${port}",
              	"Accept":"application/json" 
            	],
        	query:"",
        	body:""
        ],
        null,
        [
        	callback:aCallback
            //type:LAN_TYPE_CLIENT,
            //protocol:LAN_PROTOCOL_TCP
        ]
    ))
  }
}


private updateDeviceNetworkID(){
  setDeviceNetworkId()
}


private setDeviceNetworkId(){
  	def hex = getDataValue('controllerMac').toUpperCase().replaceAll(':', '')
    if (device.deviceNetworkId != "$hex") {
        device.deviceNetworkId = "$hex"
        log.debug "Device Network Id set to ${device.deviceNetworkId}"
    }    
}

private String convertHostnameToIPAddress(hostname) {
    def params = [
        uri: "http://dns.google.com/resolve?name=" + hostname,
        contentType: 'application/json'
    ]

    def retVal = null

    try {
        retVal = httpGet(params) { response ->
            log.trace "Request was successful, data=$response.data, status=$response.status"
            //log.trace "Result Status : ${response.data?.Status}"
            if (response.data?.Status == 0) { // Success
                for (answer in response.data?.Answer) { // Loop through results looking for the first IP address returned otherwise it's redirects
                    //log.trace "Processing response: ${answer}"
                    if (isIPAddress(answer?.data)) {
                        log.trace "Hostname ${answer?.name} has IP Address ${answer?.data}"
                        return answer?.data // We're done here (if there are more ignore it, we'll use the first IP address returned)
                    } else {
                        log.trace "Hostname ${answer?.name} redirected to ${answer?.data}"
                    }
                }
            } else {
                log.warn "DNS unable to resolve hostname ${response.data?.Question[0]?.name}, Error: ${response.data?.Comment}"
            }
        }
    } catch (Exception e) {
        log.warn("Unable to convert hostname to IP Address, Error: $e")
    }

    //log.trace "Returning IP $retVal for Hostname $hostname"
    return retVal
}

private getHostAddress() {
	return "${ip}:${port}"
}

// gets the address of the Hub
private getCallBackAddress() {
    return device.hub.getDataValue("localIP") + ":" + device.hub.getDataValue("localSrvPortTCP")
}


private String convertIPtoHex(ipAddress) { 
    String hex = ipAddress.tokenize( '.' ).collect {  String.format( '%02x', it.toInteger() ) }.join()
    return hex

}

private String convertPortToHex(port) {
	String hexport = port.toString().format( '%04x', port.toInteger() )
    return hexport
}

// TEMPERATUE Functions
// Get stored temperature from currentState in current local scale

def getTempInLocalScale(state) {
	def temp = device.currentState(state)
	def scaledTemp = convertTemperatureIfNeeded(temp.value.toBigDecimal(), temp.unit).toDouble()
	return (getTemperatureScale() == "F" ? scaledTemp.round(0).toInteger() : roundC(scaledTemp))
}

// Get/Convert temperature to current local scale
def getTempInLocalScale(temp, scale) {
	def scaledTemp = convertTemperatureIfNeeded(temp.toBigDecimal(), scale).toDouble()
	return (getTemperatureScale() == "F" ? scaledTemp.round(0).toInteger() : roundC(scaledTemp))
}

// Get stored temperature from currentState in device scale
def getTempInDeviceScale(state) {
	def temp = device.currentState(state)
	if (temp && temp.value && temp.unit) {
		return getTempInDeviceScale(temp.value.toBigDecimal(), temp.unit)
	}
	return 0
}

def getTempInDeviceScale(temp, scale) {
	if (temp && scale) {
		//API return/expects temperature values in F
		return ("F" == scale) ? temp : celsiusToFahrenheit(temp).toDouble().round(0).toInteger()
	}
	return 0
}

def roundC (tempC) {
	return (Math.round(tempC.toDouble() * 2))/2
}

 def toIntOrNull(it) {
   return it?.isInteger() ? it.toInteger() : null 
 }

def sync(ip, port) {
	def existingIp = getDataValue("controllerIP")
	def existingPort = getDataValue("controllerPort")
	if (ip && ip != existingIp) {
		updateDataValue("ControllerIP", ip)
	}
	if (port && port != existingPort) {
		updateDataValue("controllerPort", port)
	}
}

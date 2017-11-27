import groovy.util.Eval;


metadata {
	definition (name: "Pentair Advanced", namespace: "bsileo", author: "Brad Sileo") {
       capability "Polling"
       capability "Refresh"
       capability "Configuration"
       capability "Switch"
       attribute "poolPump","string"
       attribute "spaPump","string"
    }

	preferences {
        section("Select your controller") {
          input "controllerIP", "text", title: "Controller hostname/IP", required: true
          input "controllerPort", "port", title: "Controller port", required: true
          input "controllerMac", "text", title: "Controller MAC Address (all capitals, no colins)", required: true
        }
        section("Configuration") {
          input "autoname", "bool", title: "Autoname Circuits?", required:true     
        }
	}
	tiles(scale: 2) {
       
        childDeviceTile("poolTemp", "poolHeat", height:2,width:2,childTileName:"temperature")                
        childDeviceTile("type", "poolHeat", height:1,width:1,childTileName:"type")
        childDeviceTile("PoolHeatmode", "poolHeat", height:1,width:1,childTileName:"mode")
		//childDeviceTile("PoolPump", height:1,width:2, :icon,"circuit6", height:1,width:2,childTileName:"switch")
		standardTile("poolPump", "PoolPump", width:2, height:1, inactiveLabel: false, decoration: "flat") {
			state "off",  label:"Off", action:"poolPumpOn", nextState: "updating", icon: "http://cdn.device-icons.smartthings.com/Health & Wellness/health2-icn@2x.png"
			state "on", label:"On", action:"poolPumpOff",  nextState: "updating", icon: "http://cdn.device-icons.smartthings.com/Health & Wellness/health2-icn@2x.png"			
			state "updating", label:"Updating...", icon: "http://cdn.device-icons.smartthings.com/Health & Wellness/health2-icn@2x.png"
		}
        childDeviceTile("PoolHeatlower", "poolHeat", height:1,width:1,childTileName:"lowerHeatingSetpoint")
        childDeviceTile("PoolHeatset", "poolHeat", height:1,width:2,childTileName:"heatingSetpoint")
        childDeviceTile("PoolHeatraise", "poolHeat", height:1,width:1,childTileName:"raiseHeatingSetpoint")
        
		childDeviceTile("spaTemp", "spaHeat", height:2,width:2,childTileName:"temperature")        
		childDeviceTile("type", "spaHeat", height:1,width:1,childTileName:"type")
        childDeviceTile("SpaHeatmode", "spaHeat", height:1,width:1,childTileName:"mode")           
        //childDeviceTile("SpaPump", "circuit1", height:1,width:2,childTileName:"switch")                                 
        standardTile("spaPump", "spaPump", width:2, height:1, inactiveLabel: false, decoration: "flat") {
			state "off",  label:"Off", action:"spaPumpOn", nextState: "updating", icon: "http://cdn.device-icons.smartthings.com/Bath/bath19-icn@2x.png"
			state "on", label:"On", action:"spaPumpOff",  nextState: "updating", icon: "http://cdn.device-icons.smartthings.com/Bath/bath19-icn@2x.png"			
			state "updating", label:"Updating...", icon: "http://cdn.device-icons.smartthings.com/Bath/bath19-icn@2x.png"
		}
        childDeviceTile("SpaHeatlower", "spaHeat", height:1,width:1,childTileName:"lowerHeatingSetpoint")
        childDeviceTile("SpaHeatset", "spaHeat", height:1,width:2,childTileName:"heatingSetpoint")
        childDeviceTile("SpaHeatraise", "spaHeat", height:1,width:1,childTileName:"raiseHeatingSetpoint")
        
        childDeviceTile("Aux 2 Switch", "circuit2", height:1,width:1,childTileName:"switch")    
        childDeviceTile("Aux 3 Switch", "circuit3", height:1,width:1,childTileName:"switch")    
        childDeviceTile("Aux 4 Switch", "circuit4", height:1,width:1,childTileName:"switch")    
        childDeviceTile("Aux 5 Switch", "circuit5", height:1,width:1,childTileName:"switch")    
        
        childDeviceTile("solarTemp", "solarTemp", height:1,width:2,childTileName:"temperature")        
                
        childDeviceTile("Aux 7 Switch", "circuit7", height:1,width:1,childTileName:"switch")    
        childDeviceTile("Aux 8 Switch", "circuit8", height:1,width:1,childTileName:"switch")    
        
        standardTile("refresh", "device.refresh", height:1,width:2,inactiveLabel: false) {
                state "default", label:'Refresh', action:"refresh.refresh",  icon:"st.secondary.refresh-icon"
        }
        childDeviceTile("airTemp", "airTemp", height:1,width:2,childTileName:"temperature")        
        
        standardTile("poolLight", "device.switch", height:1,width:1,inactiveLabel: false) {
            state "off", label: "off", icon: "st.switches.switch.off", backgroundColor: "#ffffff", action: "switch.on"
 			state "on", label: "on", icon: "st.switches.switch.on", backgroundColor: "#00a0dc", action: "switch.off"
        }        
        
        main "poolLight"
	}
}

def configure() {
  log.debug "Executing 'configure()'"
  updateDeviceNetworkID()
}
def installed() {
	createChildren()    
}

def updated() {
  createChildren()
  if (!state.updatedLastRanAt || now() >= state.updatedLastRanAt + 5000) {
    state.updatedLastRanAt = now()
    log.debug "Executing 'updated()'"
    runIn(3, "updateDeviceNetworkID")
  } else {
    log.trace "updated(): Ran within last 5 seconds so aborting."
  }
  def children = getChildDevices()
  children.each { 
    def auxname = it.componentName
    def id = auxname.substring(3)	
    //it.label = "TEST"
  }
}

def createChildren() {

    // Save the device label for updates by updated()
    state.oldLabel = device.label
    state.counter = state.counter ? state.counter + 1 : 1
    if (state.counter == 1) {
    	  def poolHeat = addChildDevice("bsileo","Pentair Water Thermostat", "poolHeat", null, 
                                 [completedSetup: true, label: "${device.displayName} (Pool Heat)" , isComponent:true, componentName: "poolHeat", componentLabel:"${device.displayName} (Pool Heat)" ])
                log.debug "Created PoolHeat" 
          		
          def spaHeat = addChildDevice("bsileo","Pentair Water Thermostat", "spaHeat", null, 
                                 [completedSetup: true, label: "${device.displayName} (Spa Heat)" , isComponent:true, componentName: "spaHeat", componentLabel:"${device.displayName} (Spa Heat)" ])
                log.debug "Created SpaHeat" 
          
          def auxButton = addChildDevice("bsileo","Pentair Pool Control Switch", "circuit1", null, 
                                 [completedSetup: true, label: "${device.displayName} (Pump)" , isComponent:true, componentName: "circuit1", componentLabel:"${device.displayName} (Pump)" ])
                log.debug "Created Aux switch 1-Pool" 
          for (i in 2..8) {
            def auxname = "circuit${i}"
            def auxLabel = "${device.displayName} (Aux ${i})"
            log.debug "Getting ready to create Aux switch ${auxLabel} Named=${auxname}" 
            try {
            	auxButton = addChildDevice("bsileo","Pentair Pool Control Switch", auxname, null, 
                                 [completedSetup: true, label: auxLabel , isComponent:false, componentName: auxname, componentLabel: auxLabel])
                log.debug "Created Aux switch ${i}" 
              }
            catch(physicalgraph.app.exception.UnknownDeviceTypeException e)
                {
                    log.debug "Error! " + e                                                                
                }
                                 
        }
       
       addChildDevice("bsileo","Pentair Temperature Measurement Capability", "airTemp", null, 
                                 [ label: "Air Temperature", componentName: "airTemp", componentLabel: "Air Temperature",
                                 isComponent:true, completedSetup:true])                	
        
       addChildDevice("bsileo","Pentair Temperature Measurement Capability", "solarTemp", null, 
                                 [ label: "Solar Temperature", componentName: "solarTemp", componentLabel: "Solar Temperature",
                                 isComponent:true, completedSetup:true])        
       
        
      
     
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
  //log.debug "${msg}"
  log.debug "HEADERS: ${msg.headers}"
  log.debug "JSON: ${msg.json}"

  switch (msg.headers['x-event']) {
    case 'circuit':
      parseCircuits(msg.json)
      break
    case 'all':
      	parseTemps(msg.json.temperatures)
    	parseCircuits(msg.json.circuits)
     break
    case 'temp':
    	parseTemps(msg.json)      
     break
  }
}

def parseCircuits(msg) {
    def toIntOrNull = { it?.isInteger() ? it.toInteger() : null }
	log.debug('Parse Circuits')
    msg.each {  
         //log.debug "JSON:${it.key}==${it.value}"
         if ([1,2,3,4,5,6,7,8].contains(toIntOrNull(it.key))) {
         	def stat = it.value.status ? it.value.status : 0
            def child = getChildCircuit(it.key)
            //log.debug "Child==${child} --> ${stat}"
         	if (stat == 0) { 
                child.offConfirmed() 
             } 
            else { 
               child.onConfirmed()
            };
            sendEvent(name: "circuit${it.key}", value: stat == 0 ? "off" : "on")            
            log.debug "PP?SP:" + toIntOrNull(it.key) + "==" + poolPumpCircuitID() + ":"
            log.debug "${it.key}:" + (toIntOrNull(it.key) == poolPumpCircuitID())
            if (toIntOrNull(it.key) == poolPumpCircuitID()) { 
            	log.debug "PP?SP=POOL"
                sendEvent(name: "poolPump", value: stat == 0 ? "off" : "on")            
            }
            if (toIntOrNull(it.key) == spaPumpCircuitID()) { 
            	log.debug "PP?SP=SPA"
            	sendEvent(name: "spaPump", value: stat == 0 ? "off" : "on")            
            }

            if (autoname) {
            	child.label = "${device.displayName} (${it.value.friendlyName})"
                child.setFriendlyName("${device.displayName} (${it.value.friendlyName})")
            }
         }
      }
}

def getChildCircuit(id) {
    //  return childDevices.find{it.deviceNetworkId == dni})
	def children = getChildDevices()
	def dni = "circuit${id}"
    def theChild
    children.each { child ->
        if (child.deviceNetworkId == dni) { 
          log.debug "Child for :${id}==${child}"
          theChild = child          
        }
    }
    return theChild
}

def parseTemps(msg) {
	log.debug('Parse Temps')
    def ph=childDevices.find({it.deviceNetworkId == "poolHeat"});
    ph.initialize()    
    def sh=childDevices.find({it.deviceNetworkId == "spaHeat"});
    sh.initialize()
	msg.each { k, v ->    
    	 sendEvent(name: k, value: v)
         //log.debug "TEMP data:${k}==${v}"
         
         switch (k) {
        	case "poolTemp":            	
            	ph?.setTemperature(v)
            break;
        	case "spaTemp":
            	sh?.setTemperature(v)
            break;
        	case "airTemp":
            	childDevices.find({it.deviceNetworkId == "airTemp"})?.setTemperature(v)
            break;
        	case "solarTemp":
            	childDevices.find({it.deviceNetworkId == "solarTemp"})?.setTemperature(v)
            break;
        	case "poolSetPoint":            	
                ph?.setHeatingSetpoint(v)
            break;
            case "spaSetPoint":
            	sh?.setHeatingSetpoint(v)
            break;
        	case "poolHeatMode":
            	ph?.switchToMode(v?"heat":"off")
            break;
            case "spaHeatMode":
            	sh?.switchToMode(v?"heat":"off")
            break;
        }
	}

}

def on() {
	
}

def off() {
	
}

def poolPumpOn() {
	childOn(poolPumpCircuitID())
}

def poolPumpOff() {
	childOff(poolPumpCircuitID())
}

def poolPumpCircuitID() {
	return 6
}

def spaPumpOn() {
	childOn(spaPumpCircuitID())
}

def spaPumpOff() {
	childOff(spaPumpCircuitID())
}

def spaPumpCircuitID() {
	return 1
}

def childOn(cir_id) {
	log.debug "Got on from ${cir_id}"
    def id = cir_id.substring(7)
	return setCircuit(id,1)
}
	
def childOff(cir_id) {
	log.debug "Got off from ${cir_id}"
	def id = cir_id.substring(7)
	return setCircuit(id,0)
}


def setCircuit(circuit, state) {
  log.debug "Executing 'set(${circuit}, ${state})'"
  sendEthernet("/circuit/${circuit}/set/${state}")
}

def heaterOn(spDevice) {
  //log.debug "Executing 'heater on for ${spDevice}'"
  def tag = spDevice.deviceNetworkId.toLowerCase()
  sendEthernet("/${tag}/mode/1")
}

def heaterOff(spDevice) {
	//log.debug "Executing 'heater off for ${spDevice}'"
    def tag = spDevice.deviceNetworkId.toLowerCase()
    sendEthernet("/${tag}/mode/0")
}

def updateSetpoint(spDevice,setPoint) {
  	def tag = spDevice.deviceNetworkId.toLowerCase()
	sendEthernet("/${tag}/setpoint/${setPoint}")
}



// INTERNAL Methods

private sendEthernet(message) {
  if (settings.controllerIP != null && settings.controllerPort != null) {
    log.debug "Executing 'sendEthernet' http://${settings.controllerIP}:${settings.controllerPort}${message}"
    sendHubCommand(new physicalgraph.device.HubAction(
        method: "GET",
        path: "${message}",
        headers: [
            HOST: "${settings.controllerIP}:${settings.controllerPort}",
            "Accept":"application/json" ]
    ))
  }
}

private updateDeviceNetworkID(){
  setDeviceNetworkId(settings.controllerIP,settings.controllerPort)
}


private setDeviceNetworkId(ip,port){
  	def iphex = convertIPtoHex(ip)
  	def porthex = convertPortToHex(port)
  	device.deviceNetworkId = "$iphex"
    device.deviceNetworkId = settings.controllerMac
  	log.debug "Device Network Id set to ${iphex}"
}

private getHostAddress() {
	return "${ip}:${port}"
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




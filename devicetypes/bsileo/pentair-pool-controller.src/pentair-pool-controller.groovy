import groovy.util.Eval;


metadata {
	definition (name: "Pentair Pool Controller", namespace: "bsileo", author: "Brad Sileo") {
       capability "Polling"
       capability "Refresh"
       capability "Configuration"
       capability "Switch"
       attribute "poolPump","string"
       attribute "spaPump","string"
       command "poolPumpOn"
       command "poolPumpOff"
       command "spaPumpOn"
       command "spaPumpOff"
    }

	preferences {
        section("Select your controller") {
          input "controllerIP", "text", title: "Controller hostname/IP", required: true
          input "controllerPort", "port", title: "Controller port", required: true
          input "controllerMac", "text", title: "Controller MAC Address (all capitals, no colins)", required: true
        }
        section("Configuration") {
          input "autoname", "bool", title: "Autoname Circuits? (one-time only)", required:true     
        }
	}
	tiles(scale: 2) {
       
        childDeviceTile("poolTemp", "poolHeat", height:2,width:2,childTileName:"temperature")                
         standardTile("poolLight", "device.switch", height:1,width:1,inactiveLabel: false) {
            state "off", label: "off", icon: "st.Lighting.light1", backgroundColor: "#ffffff", action: "switch.on", nextState: "updating"
 			state "on", label: "on", icon: "st.Lighting.light1", backgroundColor: "#00a0dc", action: "switch.off", nextState: "updating"
            state "updating", label:"Updating...", icon: "st.Lighting.light13"
        }
        childDeviceTile("PoolHeatmode", "poolHeat", height:1,width:1,childTileName:"mode")
		//childDeviceTile("PoolPump", height:1,width:2, :icon,"circuit6", height:1,width:2,childTileName:"switch")
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
        //childDeviceTile("SpaPump", "circuit1", height:1,width:2,childTileName:"switch")                                 
        standardTile("spaPump", "device.spaPump", width:2, height:1, inactiveLabel: false, decoration: "flat") {
			state "off",  label:"Off", action:"spaPumpOn", nextState: "updating", icon: "https://raw.githubusercontent.com/bsileo/SmartThings_Pentair/master/hottub-128.png",backgroundColor: "#ffffff"
			state "on", label:"On", action:"spaPumpOff",  nextState: "updating", icon: "https://raw.githubusercontent.com/bsileo/SmartThings_Pentair/master/hottub-128.png",backgroundColor: "#00a0dc"		
			state "updating", label:"Updating...",  icon: "https://raw.githubusercontent.com/bsileo/SmartThings_Pentair/master/hottub-128.png",backgroundColor: "#cccccc"
		}
        childDeviceTile("SpaHeatlower", "spaHeat", height:1,width:1,childTileName:"lowerHeatingSetpoint")
        childDeviceTile("SpaHeatset", "spaHeat", height:1,width:2,childTileName:"heatingSetpoint")
        childDeviceTile("SpaHeatraise", "spaHeat", height:1,width:1,childTileName:"raiseHeatingSetpoint")
        
        childDeviceTile("Aux 1 Switch", "circuit1", height:1,width:1,childTileName:"switch")    
        childDeviceTile("Aux 2 Switch", "circuit2", height:1,width:1,childTileName:"switch")    
        childDeviceTile("Aux 3 Switch", "circuit3", height:1,width:1,childTileName:"switch")    
        childDeviceTile("Aux 4 Switch", "circuit4", height:1,width:1,childTileName:"switch")    
        
        childDeviceTile("solarTemp", "solarTemp", height:1,width:1,childTileName:"temperature")        
        standardTile("refresh", "device.refresh", height:1,width:1,inactiveLabel: false) {
                state "default", label:'Refresh', action:"refresh.refresh",  icon:"st.secondary.refresh-icon"
        }
        
        childDeviceTile("Aux 5 Switch", "circuit5", height:1,width:1,childTileName:"switch")    
        childDeviceTile("Aux 6 Switch", "circuit6", height:1,width:1,childTileName:"switch")                    
        childDeviceTile("Aux 7 Switch", "circuit7", height:1,width:1,childTileName:"switch")    
        childDeviceTile("Aux 8 Switch", "circuit8", height:1,width:1,childTileName:"switch")    
        
        childDeviceTile("airTemp", "airTemp", height:1,width:2,childTileName:"temperature")        
        
        childDeviceTile("saltPPM","poolChlorinator", height:2,width:2,childTileName:"saltPPM")
        childDeviceTile("chlorinateSwitch","poolChlorinator", height:1,width:1,childTileName:"chlorinate")
        childDeviceTile("currentOutput","poolChlorinator", height:1,width:1,childTileName:"currentOutput")
        childDeviceTile("poolSpaSetpoint","poolChlorinator", height:1,width:2,childTileName:"poolSpaSetpoint")
        childDeviceTile("superChlorinate","poolChlorinator", height:1,width:1,childTileName:"superChlorinate")
        childDeviceTile("status","poolChlorinator", height:1,width:3,childTileName:"status")
        
        main "poolLight"
	}
}

def configure() {
  log.debug "Executing 'configure()'"
  updateDeviceNetworkID()
}
def installed() {
	createChildren()  
    state.autoname=settings.autoname
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
  state.autoname=settings.autoname
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
          
          for (i in 1..8) {
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
       
       addChildDevice("bsileo","Pentair Chlorinator", "poolChlorinator", null, 
                                 [ label: "${device.displayName} Chlorinator", componentName: "poolChlorinator", componentLabel: "${device.displayName} Chlorinator",
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
  //log.debug "HEADERS: ${msg.headers}"
  //log.debug "JSON: ${msg.json}"

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
    case 'chlorinator':
    	parseChlorinator(msg.json)
        break
  }
}

def parseCircuits(msg) {
   
	log.info('Parse Circuits')
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
            if (toIntOrNull(it.key) == poolPumpCircuitID()) { 
                sendEvent(name: "poolPump", value: stat == 0 ? "off" : "on")            
            }
            if (toIntOrNull(it.key) == spaPumpCircuitID()) { 
            	sendEvent(name: "spaPump", value: stat == 0 ? "off" : "on")            
            }
            if (toIntOrNull(it.key) == lightCircuitID()) { 
            	sendEvent(name: "switch", value: stat == 0 ? "off" : "on")            
            }
            sendEvent(name: "circuit${it.key}", value: stat == 0 ? "off" : "on")            
    
            if (state.autoname) {
            	log.info("Completed Autoname Single Pass on Circuit ($it.key} - will not run again")
            	child.label = "${device.displayName} (${it.value.friendlyName})"
                child.setFriendlyName("${it.value.friendlyName}")
            }
         }
      }
      // Always go to False after a pass through since we never want to do this more than once.
     state.autoname=false
}

def getChildCircuit(id) {
    //  return childDevices.find{it.deviceNetworkId == dni})
	def children = getChildDevices()
	def dni = "circuit${id}"
    def theChild
    children.each { child ->
        if (child.deviceNetworkId == dni) { 
          //log.debug "Child for :${id}==${child}"
          theChild = child          
        }
    }
    return theChild
}

def parseTemps(msg) {
	log.info('Parse Temps')
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

def parseChlorinator(msg) {
	log.info('Parse Chlor')
    childDevices.find({it.deviceNetworkId == "poolChlorinator"})?.parse(msg)
}

def on() {
	return setCircuit(lightCircuitID(),1)
}

def off() {
	return setCircuit(lightCircuitID(),0)
}

def chlorinatorOn() {
  log.debug "TODO Turn on Chlor"  
}

def chlorinatorOff() {
  log.debug "TODO Turn off Chlor"  
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
	return childCircuitID(childofType("LIGHTS")?.deviceNetworkId)
}

def poolPumpCircuitID() {
	return childCircuitID(childofType("POOL")?.deviceNetworkId)
}

def spaPumpCircuitID() {
	return childCircuitID(childofType("SPA")?.deviceNetworkId)
}

def childofType(type) {
	return childDevices.find({it.currentFriendlyName == type})
}

def childOn(cir_name) {
	log.debug "Got on from ${cir_name}"
    def id = childCircuitID(cir_name)
	return setCircuit(id,1)
}

def childOff(cir_name) {
	log.debug "Got off from ${cir_name}"
	def id = childCircuitID(cir_name)
	return setCircuit(id,0)
}

def childCircuitID(cirName) {
	return toIntOrNull(cirName?.substring(7))
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

 def toIntOrNull(it) {
   return it?.isInteger() ? it.toInteger() : null 
 }



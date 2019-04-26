/**
 *  Service Manager for attaching SmartThings to a nodejsPoolController
 *
 *  Copyright 2018 Brad Sileo
 *
 *  This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as 
 *  published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.

 *   This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of 
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.

 *   You should have received a copy of the GNU Affero General Public License along with this program. If not, see http://www.gnu.org/licenses/.
 *
 *
 */
definition(
		name: "Pool Controller",
		namespace: "bsileo",
		author: "Brad Sileo",
		description: "This is a SmartApp to connect to the nodejs_poolController and create devices to manage it within SmartThings",
		category: "SmartThings Labs",
		iconUrl: "http://cdn.device-icons.smartthings.com/Health & Wellness/health2-icn.png",
		iconX2Url: "http://cdn.device-icons.smartthings.com/Health & Wellness/health2-icn@2x.png",
		iconX3Url: "http://cdn.device-icons.smartthings.com/Health & Wellness/health2-icn@3x.png")


preferences {
    page(name: "deviceDiscovery", title: "UPnP Device Setup", content: "deviceDiscovery")
    page(name: "poolConfig", title: "Pool Configuration", content: "poolConfig") 
}

// UPNP Device Discovery Code
def deviceDiscovery() {
	def options = [:]
	def devices = getVerifiedDevices()
    state.config=false
    log.debug("DevDisc ${devices}")
	devices.each {
    	//log.debug("Processing ${it}-->${it.value}")
		def value = it.value.name ?: "Pool Controller ${it.value.mac}"
		def key = it.value.mac
		options["${key}"] = value
        //log.debug("SSDP ${key} = ${it.value}")
	}
	/*log.debug("Check Manual device?-- IP:${controllerIP}:${controllerPort}=${controllerMac}")
    if (controllerIP && controllerPort && controllerMac) {
    	log.debug("Add Manual device-- IP:${controllerIP}:${controllerPort}=${controllerMac}")
    	def newdevices = getDevices()           
        newdevices[controllerMac] = [ 
        				networkAddress:controllerIP,
                        deviceAddress:controllerPort ,
                        verified:false,
                        mac:controllerMac,
                        hub:"",
                        ssdpPath:"/device",
                        ssdpTerm:"urn:schemas-upnp-org:device:PoolController:1",
                        name:"",
                        ssdpNTS:"",
                        uuid:"",
                        mode:"",
                        ssdpUSN:"uuid:806f52f4-1f35-4e33-9299-b827eb3bb77a::urn:schemas-upnp-org:device:PoolController:1"                        
        				]  	
                        
    }*/
	ssdpSubscribe()
	ssdpDiscover()
	verifyDevices()
         
	return dynamicPage(name: "deviceDiscovery", title: "Locate Pool Controller...", nextPage: "poolConfig", refreshInterval: 5, install: false, uninstall: true) {		
        section("Please wait while we discover your nodejs-poolController. Discovery can take some time...Select your device below once discovered.", hideable:false, hidden:false) {
			input "selectedDevice", "enum", required: false, title: "Select A Device (${options.size() ?: 0} found)", multiple: false, options: options
		}
        /*section("Manual poolController Configuration (Optional)", hideable:true, hidden:false) {
        	input "controllerIP", "text", title: "Controller IP Address", required: false, displayDuringSetup: true, defaultValue:""
          	input "controllerPort", "port", title: "Controller Port", required: false, displayDuringSetup: true, defaultValue:"3000"
          	input "controllerMac", "text", title: "Controller MAC Address (all capitals, no colins 'AABBCC112233)", required: false, displayDuringSetup: true
        }*/
        
	}
}

// nodejs-PoolController configuration functions
def poolConfig() {	   
    if (state.config) {
    	log.debug("poolConfig STATE=${state}")
    	return dynamicPage(name: "poolConfig", title: "Verify Pool Configuration:", nextPage: "", refreshInterval: 0,install: true, uninstall: false) {
            section("Name:") {
                input name:"deviceName", type:"text", title: "Enter the name for your device", required:true, defaultValue:"Pool"
            }
            section("Please verify the options below.") {
              //input name:"numberCircuits", type:"number", title: "How many circuits:", required:true, defaultValue:state.numCircuits
              input name:"includeSpa", type:"bool", title: "Enable Spa?", required:true, defaultValue:state.includeSpa
              input name:"includeChlorinator", type:"bool", title: "Show Chlorinator Section?", required:true, defaultValue:state.includeChlor
              input name:"includeIntellichem", type:"bool", title: "Show Intellichem Section?", required:true, defaultValue:state.includeChem
              input name:"includeSolar", type:"bool", title: "Enable Solar?", required:true, defaultValue:state.includeSolar             
                }
            }
    	}
    else {
    	return dynamicPage(name: "poolConfig", title: "Getting Pool Configuration...", nextPage: "", refreshInterval: 2,install: false, uninstall: false) {
		section("Name:") {
        	input name:"deviceName", type:"text", title: "Enter the name for your device", required:true, defaultValue:"Pool"
        	}
        getPoolConfig()
    	}
	}
}

def getPoolConfig() {
 	state.config=false    
    def devMAC = selectedDevice    
    def devices = getVerifiedDevices()
    def selectedDeviceInfo = devices.find { it.value.mac == devMAC } 
    if (selectedDeviceInfo) {
        log.debug "Configure [${selectedDeviceInfo.value.mac}]"
        def params = [
            method: "GET",
            path: "/all",
            headers: [
                HOST: "${selectedDeviceInfo.value.networkAddress}:${selectedDeviceInfo.value.deviceAddress}",
                "Accept":"application/json" 
            ]
        ]
        def opts = [
            callback : 'parseConfig',
            type: 'LAN_TYPE_CLIENT'
        ]
        try {
            sendHubCommand(new physicalgraph.device.HubAction(params, null, opts))
            log.debug "SENT: ${params}"
        } catch (e) {
            log.error "something went wrong: $e"
        }
    }
    else {
    	log.error("Failed to locate a verified controller for use with ${devMAC}")
        log.debug("Devices={$state.devices}")
        }
}

def parseConfig(resp) {
    def message = parseLanMessage(resp.description)   
    def msg = message.json
	log.debug("parseConfig - msg=${msg.config}")    	
    log.debug("parseConfig-circuit - msg=${msg.circuit}")
    state.includeSolar = msg.config.equipment.solar.installed == 1
    state.includeChem = msg.config.equipment.intellichem.installed == 1
    state.includeChlor = msg.config.equipment.chlorinator.installed == 1
    state.includeSpa = msg.config.equipment.spa.installed == 1
    state.pumps = msg.config.equipment.pump
    state.controller = msg.config.equipment.controller
    state.circuitHudeAux = msg.config.equipment.circuit.hideAux
    state.numCircuits =  msg.config.equipment.circuit.nonLightCircuit.size() + msg.config.equipment.circuit.lightCircuit.size()
    state.nonLightCircuits = msg.config.equipment.circuit.nonLightCircuit
    state.lightCircuits = msg.config.equipment.circuit.lightCircuit
    state.circuitData = msg.circuit
    state.config=true
    log.info "STATE=${state}"
}

def installed() {
	log.debug "Installed with settings: ${settings}"

	initialize()
}

def updated() {
	log.debug "Updated with settings: ${settings}"

	unsubscribe()
	initialize()
}

def initialize() {
	unsubscribe()
	unschedule()

	ssdpSubscribe()

	if (selectedDevice) {
		addDevices()
	}
    
	addManualDevice()
    
	runEvery5Minutes("ssdpDiscover")
}

def USN() {
	return "urn:schemas-upnp-org:device:PoolController:1"
}

void ssdpDiscover() {
	def searchTarget = "lan discovery " + USN()
    //log.debug("Send command '${searchTarget}'")
	sendHubCommand(new physicalgraph.device.HubAction("${searchTarget}", physicalgraph.device.Protocol.LAN))
}

void ssdpSubscribe() {
	 if (!state.subscribed) {
        log.trace "discover_devices: subscribe to location " + USN()
        //subscribe(location, "ssdpTerm." + USN(), ssdpHandler)
     	subscribe(location, null, ssdpHandler, [filterEvents: false])
        state.subscribed = true
     }
}

def ssdpHandler(evt) {
	def description = evt.description
	def hub = evt?.hubId

	def parsedEvent = parseLanMessage(description)
	parsedEvent << ["hub":hub]
 	if (parsedEvent?.ssdpTerm?.contains("urn:schemas-upnp-org:device:PoolController:1")) {       
		def devices = getDevices()
        String ssdpUSN = parsedEvent.ssdpUSN.toString()
        // log.debug("GET SSDP - found a pool ${parsedEvent}")        
        if (devices."${ssdpUSN}") {
            def d = devices."${ssdpUSN}"
            if (d.networkAddress != parsedEvent.networkAddress || d.deviceAddress != parsedEvent.deviceAddress) {
                d.networkAddress = parsedEvent.networkAddress
                d.deviceAddress = parsedEvent.deviceAddress
                def child = getChildDevice(parsedEvent.mac)
                if (child) {
                    child.sync(parsedEvent.networkAddress, parsedEvent.deviceAddress)
                }
            }
        } else {
            devices << ["${ssdpUSN}": parsedEvent]
        }
    }
    //log.debug("Devices updated! ${devices}")
}

Map verifiedDevices() {
	def devices = getVerifiedDevices()
	def map = [:]
	devices.each {
		def value = it.value.name ?: "UPnP Device ${it.value.ssdpUSN.split(':')[1][-3..-1]}"
		def key = it.value.mac
		map["${key}"] = value
	}
	map
}

void verifyDevices() {
	def devices = getDevices().findAll { it?.value?.verified != true }
	devices.each {
		int port = convertHexToInt(it.value.deviceAddress)
		String ip = convertHexToIP(it.value.networkAddress)
		String host = "${ip}:${port}"
        log.info("Verify UPNP PoolController Device @ http://${host}${it.value.ssdpPath}")
        log.debug("SENDING HubAction: GET ${it.value.ssdpPath} HTTP/1.1\r\nHOST: $host\r\n\r\n")
		sendHubCommand(new physicalgraph.device.HubAction("""GET ${it.value.ssdpPath} HTTP/1.1\r\nHOST: $host\r\n\r\n""", physicalgraph.device.Protocol.LAN, host, [callback: deviceDescriptionHandler]))
	}
}

def getVerifiedDevices() {
	getDevices().findAll{ it.value.verified == true }
}

def getDevices() {	
	if (!state.devices) {
		state.devices = [:]
	}
	return state.devices
}

def addDevices() {
	def devices = getDevices()
	def selectedDeviceInfo = devices.find { it.value.mac == selectedDevice }
    if (selectedDeviceInfo) {        	
        createOrUpdateDevice(selectedDeviceInfo.value.mac,selectedDeviceInfo.value.networkAddress,selectedDeviceInfo.value.deviceAddress)			
    }
}

def addManualDevice() {
	if (controllerMac && controllerIP && controllerPort) {  createOrUpdateDevice(controllerMac,controllerIP,controllerPort)	}
}

def createOrUpdateDevice(mac,ip,port) {
	def hub = location.hubs[0]     
	//log.error("WARNING Using TEST MAC")    
    //mac = mac + "-test"
	def d = getChildDevice(mac)
    if (d) {
        log.info "The Pool Controller Device with dni: ${mac} already exists...cleanup config"        
        d.updateDataValue("controllerIP",ip)
        d.updateDataValue("controllerPort",port)
        d.updateDataValue("includeChlorinator",includeChlorinator?'true':'false')
        d.updateDataValue("includeIntellichem",includeIntellichem?'true':'false')
        d.updateDataValue("includeSolar",includeSolar?'true':'false')
        d.updateDataValue("includeSpa",includeSpa?'true':'false')
        d.updateDataValue("numberCircuits",state.numCircuits as String)
        //these fail due to LazyMap not being supported
        //d.updateDataValue('nonLightCircuits',state.nonLightCircuits)
        //d.updateDataValue('lightCircuits',state.lightCircuits)
        d.manageChildren()
   }
   else {
   		log.info "Creating Pool Controller Device with dni: ${mac}"
		d = addChildDevice("bsileo", "Pentair Pool Controller", mac, hub.id, [
			"label": deviceName,
            "completedSetup" : true,
			"data": [
				"controllerMac": mac,
				"controllerIP": ip,
				"controllerPort": port,
                "includeChlorinator":includeChlorinator,
                "includeIntellichem":includeIntellichem,
                "includeSolar":includeSolar,
                "includeSpa":includeSpa,
                "numberCircuits":state.numCircuits,
                'nonLightCircuits':state.nonLightCircuits,
                'lightCircuits':state.lightCircuits
				]
			])
   }
}



void deviceDescriptionHandler(physicalgraph.device.HubResponse hubResponse) {
	log.debug("DDHandler - > ${hubResponse}")
	def body = hubResponse.xml
    def devices = getDevices()
	if (body) {        	
        def device = devices.find { it?.key?.contains(body?.device?.UDN?.text()) }
        if (device) {
            device.value << [name: body?.device?.roomName?.text(), model:body?.device?.modelName?.text(), serialNumber:body?.device?.serialNum?.text(), verified: true]
            log.info("Verified a device - device.value > ${device.value}")
        }
        
   }
   else {
   		log.error("Cannot verify UPNP device - no XML returned check PoolController /device response")
   }
}

private Integer convertHexToInt(hex) {
	Integer.parseInt(hex,16)
}

private String convertHexToIP(hex) {
	[convertHexToInt(hex[0..1]),convertHexToInt(hex[2..3]),convertHexToInt(hex[4..5]),convertHexToInt(hex[6..7])].join(".")
}
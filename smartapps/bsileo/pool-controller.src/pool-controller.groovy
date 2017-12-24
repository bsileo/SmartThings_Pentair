/**
 *  Generic UPnP Service Manager
 *
 *  Copyright 2016 SmartThings
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
    /*page(name: "searchTargetSelection", title: "UPnP Search Target", nextPage: "deviceDiscovery") {
		section("Search Target") {
			input "searchTarget", "string", title: "Search Target", defaultValue: "urn:schemas-upnp-org:device:PoolController:1", required: true
		}
	}*/
    page(name: "deviceDiscovery", title: "UPnP Device Setup", content: "deviceDiscovery")
    page(name: "poolConfig", title: "Pool Configuration", content: "poolConfig")
    
   
}

// UPNP Device Discovery Code
def deviceDiscovery() {
	def options = [:]
	def devices = getVerifiedDevices()
    //log.debug("DevDisc ${devices}")
	devices.each {
    	//log.debug("Processing ${it}-->${it.value}")
		def value = it.value.name ?: "Pool Controller ${it.value.mac}"
		def key = it.value.mac
		options["${key}"] = value
        //log.debug("SSDP ${key} = ${it.value}")
	}
	//log.debug("Manual device-- IP:${controllerIP}:${controllerPort}=${controllerMac}")
    
	ssdpSubscribe()

	ssdpDiscover()
	verifyDevices()
    
    if (controllerIP && controllerPort && controllerMAC) {
    	options[controllerMAC] = "${controllerIP}:${controllerPort}"
    }
        
	return dynamicPage(name: "deviceDiscovery", title: "Locate Pool Controller...", nextPage: "poolConfig", refreshInterval: 5, install: false, uninstall: true) {		
        section("Please wait while we discover your UPnP Device. Discovery can take five minutes or more, so sit back and relax! Select your device below once discovered.", hideable:false, hidden:false) {
			input "selectedDevices", "enum", required: false, title: "Select Devices (${options.size() ?: 0} found)", multiple: true, options: options
		}
        section("Manual Controller Configuration", hideable:true, hidden:true) {
        	input "controllerIP", "text", title: "Controller hostname/IP", required: false, displayDuringSetup: true, defaultValue:"192.168.1.100"
          	input "controllerPort", "port", title: "Controller port", required: false, displayDuringSetup: true, defaultValue:"3000"
          	input "controllerMac", "text", title: "Controller MAC Address (all capitals, no colins 'AABBCC112233)", required: false, displayDuringSetup: true
        }
        
	}
}

// nodejs-PoolController configuration functions
def poolConfig() {
	getPoolConfig()
	return dynamicPage(name: "poolConfig", title: "Verify Pool Configuration:", nextPage: "", install: true, uninstall: true) {
		section("Name:") {
        	input name:"deviceName", type:"text", title: "Enter the name for your device", required:true, defaultValue:"Pool"
        }
        section("Please verify the options below.") {
	      input name:"includeChlorinator", type:"bool", title: "Show Chlorinator Section?", required:true, defaultValue:state.includeChlor
          input name:"includeIntellichem", type:"bool", title: "Show Intellichem Section?", required:true, defaultValue:state.includeChem
          input name:"includeSolar", type:"bool", title: "Enable Solar?", required:true, defaultValue:state.includeSolar
          input name:"includeSpa", type:"bool", title: "Enable Spa?", required:true, defaultValue:state.includeSpa
		}
	}
}

def getPoolConfig() {
  // TODO - make a /all request to the pool controller and get the results back to set default options
  state.includeChlor = true
  state.includeChem = false
  state.includeSolar = false
  state.includeSpa = true
  
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

	if (selectedDevices) {
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
        // log.debug("SENDING HubAction: GET ${it.value.ssdpPath} HTTP/1.1\r\nHOST: $host\r\n\r\n")
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
	selectedDevices.each { dni ->    	
		def selectedDevice = devices.find { it.value.mac == dni }
		if (selectedDevice) {        	
        	createOrUpdateDevice(selectedDevice.value.mac,selectedDevice.value.networkAddress,selectedDevice.value.deviceAddress)			
    	}
	}
}

def addManualDevice() {
	if (controllerMac && controllerIP && controllerPort) {  createOrUpdateDevice(controllerMac,controllerIP,controllerPort)	}
}

def createOrUpdateDevice(mac,ip,port) {
	def hub = location.hubs[0] 
	def d = getChildDevices()?.find {it.deviceNetworkId == mac }
	if (d) {
        log.info "The Pool Controller Device with dni: ${mac} already exists...cleanup config"        
        d.updateDataValue("controllerIP",ip)
        d.updateDataValue("controllerPort",port)
        d.updateDataValue("includeChlorinator",includeChlorinator?'true':'false')
        d.updateDataValue("includeIntellichem",includeIntellichem?'true':'false')
        d.updateDataValue("includeSolar",includeSolar?'true':'false')
        d.updateDataValue("includeSpa",includeSpa?'true':'false')
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
                "includeSpa":includeSpa
				]
			])
   }
}



void deviceDescriptionHandler(physicalgraph.device.HubResponse hubResponse) {
	// log.debug("DDHandler - > ${hubResponse.xml}")
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
   		log.error("Cannot verify UPNP device - no XML returned check PoolController device.xml file")
   }
}

private Integer convertHexToInt(hex) {
	Integer.parseInt(hex,16)
}

private String convertHexToIP(hex) {
	[convertHexToInt(hex[0..1]),convertHexToInt(hex[2..3]),convertHexToInt(hex[4..5]),convertHexToInt(hex[6..7])].join(".")
}
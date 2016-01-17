/**
 *  Hue Lights and Groups and Scenes (OH MY) - new Hue Service Manager
 *
 *  Author: Anthony Pastor
 *
 *  To-Do:
 *  	- DNI = MAC address
 *
 */
 
definition(
    name: "Hue Lights and Groups and Scenes (OH MY)",
    namespace: "info_fiend",
    author: "Anthony Pastor",
	description: "Allows you to connect your Philips Hue lights with SmartThings and control them from your Things area or Dashboard in the SmartThings Mobile app. Adjust colors by going to the Thing detail screen for your Hue lights (tap the gear on Hue tiles).\n\nPlease update your Hue Bridge first, outside of the SmartThings app, using the Philips Hue app.",
	category: "SmartThings Labs",
	iconUrl: "https://s3.amazonaws.com/smartapp-icons/Partner/hue.png",
	iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Partner/hue@2x.png"
)

preferences {
	page(name:"mainPage", title:"Hue Device Setup", content:"mainPage", refreshTimeout:5)
	page(name:"bridgeDiscovery", title:"Hue Bridge Discovery", content:"bridgeDiscovery", refreshTimeout:5)
	page(name:"bridgeBtnPush", title:"Linking with your Hue", content:"bridgeLinking", refreshTimeout:5)
	page(name:"bulbDiscovery", title:"Bulb Discovery", content:"bulbDiscovery", refreshTimeout:5)
	page(name:"groupDiscovery", title:"Group Discovery", content:"groupDiscovery", refreshTimeout:5)        
	page(name:"sceneDiscovery", title:"Scene Discovery", content:"sceneDiscovery", refreshTimeout:5)
}

def mainPage() {
	if(canInstallLabs()) {
		def bridges = bridgesDiscovered()
		if (state.username && bridges) {
			return bulbDiscovery()
		} else {
			return bridgeDiscovery()
		}
	} else {
		def upgradeNeeded = """To use SmartThings Labs, your Hub should be completely up to date.

To update your Hub, access Location Settings in the Main Menu (tap the gear next to your location name), select your Hub, and choose "Update Hub"."""

		return dynamicPage(name:"bridgeDiscovery", title:"Upgrade needed!", nextPage:"", install:false, uninstall: true) {
			section("Upgrade") {
				paragraph "$upgradeNeeded"
			}
		}
	}
}

def bridgeDiscovery(params=[:])
{
	def bridges = bridgesDiscovered()
	int bridgeRefreshCount = !state.bridgeRefreshCount ? 0 : state.bridgeRefreshCount as int
	state.bridgeRefreshCount = bridgeRefreshCount + 1
	def refreshInterval = 3

	def options = bridges ?: []
	def numFound = options.size() ?: 0

	if(!state.subscribe) {
		subscribe(location, null, locationHandler, [filterEvents:false])
		state.subscribe = true
	}

	//bridge discovery request every 15 //25 seconds
	if((bridgeRefreshCount % 5) == 0) {
		discoverBridges()
	}

	//setup.xml request every 3 seconds except on discoveries
	if(((bridgeRefreshCount % 1) == 0) && ((bridgeRefreshCount % 5) != 0)) {
		verifyHueBridges()
	}

	return dynamicPage(name:"bridgeDiscovery", title:"Discovery Started!", nextPage:"bridgeBtnPush", refreshInterval:refreshInterval, uninstall: true) {
		section("Please wait while we discover your Hue Bridge. Discovery can take five minutes or more, so sit back and relax! Select your device below once discovered.") {
			input "selectedHue", "enum", required:false, title:"Select Hue Bridge (${numFound} found)", multiple:false, options:options
		}
	}
}

def bridgeLinking()
{
	int linkRefreshcount = !state.linkRefreshcount ? 0 : state.linkRefreshcount as int
	state.linkRefreshcount = linkRefreshcount + 1
	def refreshInterval = 3

	def nextPage = ""
	def title = "Linking with your Hue"
	def paragraphText = "Press the button on your Hue Bridge to setup a link."
	if (state.username) { //if discovery worked
		nextPage = "bulbDiscovery"
		title = "Success! - click 'Next'"
		paragraphText = "Linking to your hub was a success! Please click 'Next'!"
	}

	if((linkRefreshcount % 2) == 0 && !state.username) {
		sendDeveloperReq()
	}

	return dynamicPage(name:"bridgeBtnPush", title:title, nextPage:nextPage, refreshInterval:refreshInterval) {
		section("Button Press") {
			paragraph """${paragraphText}"""
		}
	}
}

def bulbDiscovery()
{
	int bulbRefreshCount = !state.bulbRefreshCount ? 0 : state.bulbRefreshCount as int
	state.bulbRefreshCount = bulbRefreshCount + 1
	def refreshInterval = 3

	def options = bulbsDiscovered() ?: []
	def numFound = options.size() ?: 0

//	def optionsGroups = groupsDiscovered() ?: []
//	def numFoundGroups = optionsGroups.size() ?: 0

//	def optionsScenes = scenesDiscovered() ?: []
//	def numFoundScenes = optionsScenes.size() ?: 0

	if((bulbRefreshCount % 3) == 0) {
        log.debug "START BULB DISCOVERY"
        discoverHueBulbs()
//        pause(300)
//		discoverHueGroups()
//        pause(300)        
//        discoverHueScenes()
        log.debug "END BULB DISCOVERY"
	}

	return dynamicPage(name:"bulbDiscovery", title:"Bulb Discovery Started!", nextPage:"groupDiscovery", refreshInterval:refreshInterval, uninstall: true) {
		section("Please wait while we discover your Hue Bulbs. Discovery can take five minutes or more, so sit back and relax! Select your device below once discovered.") {
			input "selectedBulbs", "enum", required:false, title:"Select Hue Bulbs (${numFound} found)", multiple:true, options:options
		}
		section {
			def title = bridgeDni ? "Hue bridge (${bridgeHostname})" : "Find bridges"
			href "bridgeDiscovery", title: title, description: "", state: selectedHue ? "complete" : "incomplete", params: [override: true]

		}
	}
}

def groupDiscovery()
{
	int groupRefreshCount = !state.groupRefreshCount ? 0 : state.groupRefreshCount as int
	state.groupRefreshCount = groupRefreshCount + 1
	def refreshInterval = 3

	def optionsGroups = groupsDiscovered() ?: []
	def numFoundGroups = optionsGroups.size() ?: 0

	if((groupRefreshCount % 3) == 0) {
	    log.debug "START GROUP DISCOVERY"
		discoverHueGroups()
        log.debug "END GROUP DISCOVERY"
	}

	return dynamicPage(name:"groupDiscovery", title:"Group Discovery Started!", nextPage:"sceneDiscovery", refreshInterval:refreshInterval, uninstall: true) {
		section("Please wait while we discover your Hue Groups. Discovery can take a few minutes, so sit back and relax! Select your device below once discovered.") {
			input "selectedGroups", "enum", required:false, title:"Select Hue Groups (${numFoundGroups} found)", multiple:true, options:optionsGroups

		}
		section {
			def title = bridgeDni ? "Hue bridge (${bridgeHostname})" : "Find bridges"
			href "bridgeDiscovery", title: title, description: "", state: selectedHue ? "complete" : "incomplete", params: [override: true]

		}
	}
}


def sceneDiscovery()
{
	int sceneRefreshCount = !state.sceneRefreshCount ? 0 : state.sceneRefreshCount as int
	state.sceneRefreshCount = sceneRefreshCount + 1
	def refreshInterval = 3

	def optionsScenes = scenesDiscovered() ?: []
	def numFoundScenes = optionsScenes.size() ?: 0

	if((sceneRefreshCount % 3) == 0) {
        log.debug "START HUE SCENE DISCOVERY"
        discoverHueScenes()
        log.debug "END HUE SCENE DISCOVERY"
	}

	return dynamicPage(name:"sceneDiscovery", title:"Scene Discovery Started!", nextPage:"", refreshInterval:refreshInterval, install:true, uninstall: true) {
		section("Please wait while we discover your Hue Scenes. Discovery can take a few minutes, so sit back and relax! Select your device below once discovered.") {
			input "selectedScenes", "enum", required:false, title:"Select Hue Scenes (${numFoundScenes} found)", multiple:true, options:optionsScenes
		}
		section {
			def title = bridgeDni ? "Hue bridge (${bridgeHostname})" : "Find bridges"
			href "bridgeDiscovery", title: title, description: "", state: selectedHue ? "complete" : "incomplete", params: [override: true]

		}
	}
}



private discoverBridges() {
	sendHubCommand(new physicalgraph.device.HubAction("lan discovery urn:schemas-upnp-org:device:basic:1", physicalgraph.device.Protocol.LAN))
}

private sendDeveloperReq() {
	def token = app.id
	sendHubCommand(new physicalgraph.device.HubAction([
		method: "POST",
		path: "/api",
		headers: [
			HOST: bridgeHostnameAndPort
		],
		body: [devicetype: "$token-0", username: "$token-0"]], bridgeDni))
}

private discoverHueBulbs() {
	sendHubCommand(new physicalgraph.device.HubAction([
		method: "GET",
		path: "/api/${state.username}/lights",
		headers: [
			HOST: bridgeHostnameAndPort
		]], bridgeDni))
}

private discoverHueGroups() {
	log.trace "discoverHueGroups REACHED"
	sendHubCommand(new physicalgraph.device.HubAction([
		method: "GET",
		path: "/api/${state.username}/groups",
		headers: [
			HOST: bridgeHostnameAndPort
		]], bridgeDni))
}

private discoverHueScenes() {
	sendHubCommand(new physicalgraph.device.HubAction([
		method: "GET",
		path: "/api/${state.username}/scenes",
		headers: [
			HOST: bridgeHostnameAndPort
		]], bridgeDni))
}

private verifyHueBridge(String deviceNetworkId) {
	log.trace "verifyHueBridge($deviceNetworkId)"
	sendHubCommand(new physicalgraph.device.HubAction([
		method: "GET",
		path: "/description.xml",
		headers: [
			HOST: ipAddressFromDni(deviceNetworkId)
		]], deviceNetworkId))
}

private verifyHueBridges() {
	def devices = getHueBridges().findAll { it?.value?.verified != true }
	log.debug "UNVERIFIED BRIDGES!: $devices"
	devices.each {
		verifyHueBridge((it?.value?.ip + ":" + it?.value?.port))
	}
}

Map bridgesDiscovered() {
	def vbridges = getVerifiedHueBridges()
	def map = [:]
	vbridges.each {
		def value = "${it.value.name}"
		def key = it.value.ip + ":" + it.value.port
		map["${key}"] = value
	}
	map
}

Map bulbsDiscovered() {
	def bulbs =  getHueBulbs()
	def map = [:]
	if (bulbs instanceof java.util.Map) {
		bulbs.each {
			def value = "${it?.value?.name}"
			def key = app.id +"/"+ it?.value?.id
			map["${key}"] = value
		}
	} else { //backwards compatable
		bulbs.each {
			def value = "${it?.name}"
			def key = app.id +"/"+ it?.id
			map["${key}"] = value
		}
	}
	map
}

Map groupsDiscovered() {
	def groups =  getHueGroups()
	def map = [:]
	if (groups instanceof java.util.Map) {
		groups.each {
			def value = "${it?.value?.name}"
			def key = app.id +"/"+ it?.value?.id + "g"
			map["${key}"] = value
		}
	} else { //backwards compatable
		groups.each {
			def value = "${it?.name}"
			def key = app.id +"/"+ it?.id + "g"
			map["${key}"] = value
		}
	}
	map
}

Map scenesDiscovered() {
	def scenes =  getHueScenes()
	def map = [:]
	if (scenes instanceof java.util.Map) {
		scenes.each {
			def value = "${it?.value?.name}"
			def key = app.id +"/"+ it?.value?.id + "s"
			map["${key}"] = value
		}
	} else { //backwards compatable
		scenes.each {
			def value = "${it?.name}"
			def key = app.id +"/"+ it?.id + "s"
			map["${key}"] = value
		}
	}
	map
}

def getHueBulbs() {
	log.debug state.bulbs
	log.debug "HUE BULBS:"
	state.bulbs = state.bulbs ?: [:]
}

def getHueGroups() {
	log.debug state.groups
    log.debug "HUE GROUPS:"
	state.groups = state.groups ?: [:]
}

def getHueScenes() {
	log.debug state.scenes
    log.debug "HUE SCENES:"
	state.scenes = state.scenes ?: [:]
}

def getHueBridges() {
	state.bridges = state.bridges ?: [:]
}

def getVerifiedHueBridges() {
	getHueBridges().findAll{ it?.value?.verified == true }
}

def installed() {
	log.trace "Installed with settings: ${settings}"
	initialize()
}

def updated() {
	log.trace "Updated with settings: ${settings}"
	unsubscribe()
	initialize()
}

def initialize() {
	// remove location subscription aftwards
	log.debug "INITIALIZE"
	state.subscribe = false
	state.bridgeSelectedOverride = false

	if (selectedHue) {
		addBridge()
	}
	if (selectedBulbs) {
		addBulbs()
	}
	if (selectedGroups)
	{
		addGroups()
	}
    if (selectedScenes)
	{
		addScenes()
	}
	if (selectedHue) {
		def bridge = getChildDevice(selectedHue)
		subscribe(bridge, "bulbList", bulbListHandler)
		subscribe(bridge, "groupList", groupListHandler)
		subscribe(bridge, "sceneList", sceneListHandler)
    }
    runEvery5Minutes("doDeviceSync")
}

// Handles events to add new bulbs
def bulbListHandler(evt) {
	def bulbs = [:]
	log.trace "Adding bulbs to state..."
	//state.bridgeProcessedLightList = true
	evt.jsonData.each { k,v ->
		log.trace "$k: $v"
		if (v instanceof Map) {
				bulbs[k] = [id: k, name: v.name, type: v.type, hub:evt.value]
		}
	}
	state.bulbs = bulbs
	log.info "${bulbs.size()} bulbs found"
}

def groupListHandler(evt) {
	def groups =[:]
	log.trace "Adding groups to state..."
	state.bridgeProcessedLightList = true
	evt.jsonData.each { k,v ->
		log.trace "$k: $v"
		if (v instanceof Map) {
				groups[k] = [id: k, name: v.name, type: v.type, hub:evt.value]
		}
	}
	state.groups = groups
	log.info "${groups.size()} groups found"
}

def sceneListHandler(evt) {
	def scenes =[:]
	log.trace "Adding scenes to state..."
	state.bridgeProcessedLightList = true
	evt.jsonData.each { k,v ->
		log.trace "$k: $v"
		if (v instanceof Map) {
				scenes[k] = [id: k, name: v.name, type: "Scene", hub:evt.value]
		}
	}
	state.scenes = scenes
	log.info "${scenes.size()} scenes found"
}


def addBulbs() {
	def bulbs = getHueBulbs()
	selectedBulbs.each { dni ->
		def d = getChildDevice(dni)
		if(!d) {
			def newHueBulb
			if (bulbs instanceof java.util.Map) {
				newHueBulb = bulbs.find { (app.id + "/" + it.value.id) == dni }
				if (newHueBulb?.value?.type?.equalsIgnoreCase("Dimmable light")) {
					d = addChildDevice("zpriddy", "ZP Hue Lux Bulb", dni, newHueBulb?.value.hub, ["label":newHueBulb?.value.name])
				} else {
					d = addChildDevice("zpriddy", "ZP Hue Bulb", dni, newHueBulb?.value.hub, ["label":newHueBulb?.value.name])
				}
			} else { 
            	//backwards compatable
				newHueBulb = bulbs.find { (app.id + "/" + it.id) == dni }
				d = addChildDevice("zpriddy", "ZP Hue Bulb", dni, newHueBulb?.hub, ["label":newHueBulb?.name])
			}

			log.debug "created ${d.displayName} with id $dni"
			d.refresh()
		} else {
			log.debug "found ${d.displayName} with id $dni already exists, type: '$d.typeName'"
			if (bulbs instanceof java.util.Map) {
            	def newHueBulb = bulbs.find { (app.id + "/" + it.value.id) == dni }
				if (newHueBulb?.value?.type?.equalsIgnoreCase("Dimmable light") && d.typeName == "Hue Bulb") {
					d.setDeviceType("ZP Hue Lux Bulb")
				}
			}
		}
	}
}

def addGroups() {
	def groups = getHueGroups()
	selectedGroups.each { dni ->
		def d = getChildDevice(dni)
		if(!d) 
		{
			def newHueGroup
			if (groups instanceof java.util.Map) 
			{
				newHueGroup = groups.find { (app.id + "/" + it.value.id + "g") == dni }
				d = addChildDevice("zpriddy", "ZP Hue Group", dni, newHueGroup?.value.hub, ["label":newHueGroup?.value.name, "groupID":newHueGroup?.value.id])
			} 

			log.debug "created ${d.displayName} with id $dni"
			d.refresh()
		} 
		else 
		{
			log.debug "found ${d.displayName} with id $dni already exists, type: '$d.typeName'"
		}
	}
}


def addScenes() {
	def scenes = getHueScenes()
	selectedScenes.each { dni ->
		def d = getChildDevice(dni)
		if(!d) 
		{
			def newHueScene
			if (scenes instanceof java.util.Map) 
			{
				newHueScene = scenes.find { (app.id + "/" + it.value.id + "s") == dni }
				d = addChildDevice("zpriddy", "AP Hue Scene", dni, newHueScene?.value.hub, ["label":newHueScene?.value.name, "sceneID":newHueScene?.value.id])
			} 

			log.debug "created ${d.displayName} with id $dni"
			d.refresh()
		} 
		else 
		{
			log.debug "found ${d.displayName} with id $dni already exists, type: 'Scene'"
		}
	}
}


def addBridge() {
	def vbridges = getVerifiedHueBridges()
	def vbridge = vbridges.find {(it.value.ip + ":" + it.value.port) == selectedHue}

	if(vbridge) {
		def d = getChildDevice(selectedHue)
		if(!d) {
			d = addChildDevice("zpriddy", "ZP Hue Bridge", selectedHue, vbridge.value.hub, ["data":["mac": vbridge.value.mac]]) // ["preferences":["ip": vbridge.value.ip, "port":vbridge.value.port, "path":vbridge.value.ssdpPath, "term":vbridge.value.ssdpTerm]]

			log.debug "created ${d.displayName} with id ${d.deviceNetworkId}"

			sendEvent(d.deviceNetworkId, [name: "networkAddress", value: convertHexToIP(vbridge.value.ip) + ":" +  convertHexToInt(vbridge.value.port)])
			sendEvent(d.deviceNetworkId, [name: "serialNumber", value: vbridge.value.serialNumber])
		}
		else
		{
			log.debug "found ${d.displayName} with id $dni already exists"
		}
	}
}


def locationHandler(evt) {
	log.info "LOCATION HANDLER: $evt.description"
	def description = evt.description
	def hub = evt?.hubId

	def parsedEvent = parseEventMessage(description)
	parsedEvent << ["hub":hub]

	if (parsedEvent?.ssdpTerm?.contains("urn:schemas-upnp-org:device:basic:1"))
	{ //SSDP DISCOVERY EVENTS
		log.trace "SSDP DISCOVERY EVENTS"
		def bridges = getHueBridges()

		if (!(bridges."${parsedEvent.ssdpUSN.toString()}"))
		{ //bridge does not exist
			log.trace "Adding bridge ${parsedEvent.ssdpUSN}"
			bridges << ["${parsedEvent.ssdpUSN.toString()}":parsedEvent]
		}
		else
		{ // update the values

			log.debug "Device was already found in state..."

			def d = bridges."${parsedEvent.ssdpUSN.toString()}"
			def host = parsedEvent.ip + ":" + parsedEvent.port
			if(d.ip != parsedEvent.ip || d.port != parsedEvent.port || host != state.hostname) {

				log.debug "Device's port or ip changed..."
				state.hostname = host
				d.ip = parsedEvent.ip
				d.port = parsedEvent.port
				d.name = "Philips hue ($bridgeHostname)"

				app.updateSetting("selectedHue", host)

				childDevices.each {
					if (it.getDeviceDataByName("mac") == parsedEvent.mac) {
						log.debug "updating dni for device ${it} with mac ${parsedEvent.mac}"
						it.setDeviceNetworkId((parsedEvent.ip + ":" + parsedEvent.port)) //could error if device with same dni already exists
					}
				}
			}
		}
	}
	else if (parsedEvent.headers && parsedEvent.body)
	{ // HUE BRIDGE RESPONSES
		log.trace "HUE BRIDGE RESPONSES"
		def headerString = new String(parsedEvent.headers.decodeBase64())
		def bodyString = new String(parsedEvent.body.decodeBase64())
		def type = (headerString =~ /Content-type:.*/) ? (headerString =~ /Content-type:.*/)[0] : null
		def body

		if (type?.contains("xml"))
		{ // description.xml response (application/xml)
			body = new XmlSlurper().parseText(bodyString)

			if (body?.device?.modelName?.text().startsWith("Philips hue bridge"))
			{
				def bridges = getHueBridges()
				def bridge = bridges.find {it?.key?.contains(body?.device?.UDN?.text())}
				if (bridge)
				{
					bridge.value << [name:body?.device?.friendlyName?.text(), serialNumber:body?.device?.serialNumber?.text(), verified: true]
				}
				else
				{
					log.error "/description.xml returned a bridge that didn't exist"
				}
			}
		}
		else if(type?.contains("json"))
		{ //(application/json)
			body = new groovy.json.JsonSlurper().parseText(bodyString)

			if (body?.success != null)
			{ //POST /api response (application/json)
				if (body?.success?.username)
				{
					state.username = body.success.username[0]
					state.hostname = selectedHue
				}
			}
			else if (body.error != null)
			{
				//TODO: handle retries...
				log.error "ERROR: application/json ${body.error}"
			}
			else
			{ //GET /api/${state.username}/lights response (application/json)
            	log.debug "HERE"
				if (!body.action) 
				{ //check if first time poll made it here by mistake
                
                	if(!body?.type?.equalsIgnoreCase("LightGroup") || !body?.type?.equalsIgnoreCase("Room"))
					{
						log.debug "LIGHT GROUP!!!"
					}
                    
					def bulbs = getHueBulbs()
					def groups = getHueGroups()
                    def scenes = getHueScenes()

					log.debug "Adding bulbs, groups, and scenes to state!"
					body.each { k,v ->
                    	log.debug v.type
						if(v.type == "LightGroup" || v.type == "Room")
						{
							groups[k] = [id: k, name: v.name, type: v.type, hub:parsedEvent.hub]
						}
						else if (v.type == "Extended color light" || v.type == "Color light" || v.type == "Dimmable light" )
						{
							bulbs[k] = [id: k, name: v.name, type: v.type, hub:parsedEvent.hub]
						}
                        else 
                        {
                        	scenes[k] = [id: k, name: v.name, type: "Scene", hub:parsedEvent.hub]
                        }    
					}
				}
			}
		}
	}
	else {
		log.trace "NON-HUE EVENT $evt.description"
	}
}

private def parseEventMessage(Map event) {
	//handles bridge attribute events
	return event
}

private def parseEventMessage(String description) {
	def event = [:]
	def parts = description.split(',')
	parts.each { part ->
		part = part.trim()
		if (part.startsWith('devicetype:')) {
			def valueString = part.split(":")[1].trim()
			event.devicetype = valueString
		}
		else if (part.startsWith('mac:')) {
			def valueString = part.split(":")[1].trim()
			if (valueString) {
				event.mac = valueString
			}
		}
		else if (part.startsWith('networkAddress:')) {
			def valueString = part.split(":")[1].trim()
			if (valueString) {
				event.ip = valueString
			}
		}
		else if (part.startsWith('deviceAddress:')) {
			def valueString = part.split(":")[1].trim()
			if (valueString) {
				event.port = valueString
			}
		}
		else if (part.startsWith('ssdpPath:')) {
			def valueString = part.split(":")[1].trim()
			if (valueString) {
				event.ssdpPath = valueString
			}
		}
		else if (part.startsWith('ssdpUSN:')) {
			part -= "ssdpUSN:"
			def valueString = part.trim()
			if (valueString) {
				event.ssdpUSN = valueString
			}
		}
		else if (part.startsWith('ssdpTerm:')) {
			part -= "ssdpTerm:"
			def valueString = part.trim()
			if (valueString) {
				event.ssdpTerm = valueString
			}
		}
		else if (part.startsWith('headers')) {
			part -= "headers:"
			def valueString = part.trim()
			if (valueString) {
				event.headers = valueString
			}
		}
		else if (part.startsWith('body')) {
			part -= "body:"
			def valueString = part.trim()
			if (valueString) {
				event.body = valueString
			}
		}
	}

	event
}

def doDeviceSync(){
	log.debug "Doing Hue Device Sync!"

	//shrink the large bulb lists
	convertBulbListToMap()
	convertGroupListToMap()
	convertSceneListToMap()
    
	if(!state.subscribe) {
		subscribe(location, null, locationHandler, [filterEvents:false])
		state.subscribe = true
	}

	discoverBridges()
    poll()
}


/////////////////////////////////////
//CHILD DEVICE METHODS
/////////////////////////////////////

def parse(childDevice, description) {
	def parsedEvent = parseEventMessage(description)

	if (parsedEvent.headers && parsedEvent.body) {
		def headerString = new String(parsedEvent.headers.decodeBase64())
		def bodyString = new String(parsedEvent.body.decodeBase64())
		log.debug "parse() - ${bodyString}"
		def body = new groovy.json.JsonSlurper().parseText(bodyString)
		log.debug "BODY - $body"
		if (body instanceof java.util.HashMap)
		{ //poll response
			def bulbs = getChildDevices()
            //for each bulb
            //Group
            //Scene

             for (bulb in body) {
                def d = bulbs.find{it.deviceNetworkId == "${app.id}/${bulb.key}"}
                 if (d) {
                 	if(bulb.value.type == "Extended color light" || bulb.value.type == "Color light" || bulb.value.type == "Dimmable light")
                 	{
	                		log.debug "Reading Poll for Lights"
		                    if (bulb.value.state.reachable) {
		                            sendEvent(d.deviceNetworkId, [name: "switch", value: bulb.value?.state?.on ? "on" : "off"])
		                            sendEvent(d.deviceNetworkId, [name: "level", value: Math.round(bulb.value.state.bri * 100 / 255)])
		                            if (bulb.value.state.sat) {
		                                def hue = Math.min(Math.round(bulb.value.state.hue * 100 / 65535), 65535) as int
		                                def sat = Math.round(bulb.value.state.sat * 100 / 255) as int
		                                def hex = colorUtil.hslToHex(hue, sat)
		                                sendEvent(d.deviceNetworkId, [name: "color", value: hex])
                                        sendEvent(d.deviceNetworkId, [name: "hue", value: hue])
                                        sendEvent(d.deviceNetworkId, [name: "saturation", value: sat])
                                        
		                            }
		                        } else {
		                            sendEvent(d.deviceNetworkId, [name: "switch", value: "off"])
		                            sendEvent(d.deviceNetworkId, [name: "level", value: 100])                     
		                            if (bulb.value.state.sat) {
		                                def hue = 23
		                                def sat = 56
		                                def hex = colorUtil.hslToHex(23, 56)
                                        sendEvent(d.deviceNetworkId, [name: "color", value: hex])
                                        sendEvent(d.deviceNetworkId, [name: "hue", value: hue])
                                        sendEvent(d.deviceNetworkId, [name: "saturation", value: sat])
		                            }    
		                     }
		                 }
	                 }
	             }

	        bulbs = getChildDevices()
            for (bulb in body) {
                def d = bulbs.find{it.deviceNetworkId == "${app.id}/${bulb.key}g"}    
                if (d) {
                	/*
                	if(bulb.value.type != "LightGroup")
                	{
                		log.debug "Reading Poll for Non-LightGroups"
	                    if (bulb.value.state.reachable) {
	                            sendEvent(d.deviceNetworkId, [name: "switch", value: bulb.value?.state?.on ? "on" : "off"])
	                            sendEvent(d.deviceNetworkId, [name: "level", value: Math.round(bulb.value.state.bri * 100 / 255)])
	                            if (bulb.value.state.sat) {
	                                def hue = Math.min(Math.round(bulb.value.state.hue * 100 / 65535), 65535) as int
	                                def sat = Math.round(bulb.value.state.sat * 100 / 255) as int
	                                def hex = colorUtil.hslToHex(hue, sat)
	                                sendEvent(d.deviceNetworkId, [name: "color", value: hex])
	                            }
	                        } else {
	                            sendEvent(d.deviceNetworkId, [name: "switch", value: "off"])
	                            sendEvent(d.deviceNetworkId, [name: "level", value: 100])                     
	                            if (bulb.value.state.sat) {
	                                def hue = 23
	                                def sat = 56
	                                def hex = colorUtil.hslToHex(23, 56)
	                                sendEvent(d.deviceNetworkId, [name: "color", value: hex])
	                            }    
	                     }
	                 }
	                 */
	                if(bulb.value.type == "LightGroup" || bulb.value.type == "Room")
                	{
                		
                		log.trace "Reading Poll for Groups"
	              
                        sendEvent(d.deviceNetworkId, [name: "switch", value: bulb.value?.action?.on ? "on" : "off"])
                        sendEvent(d.deviceNetworkId, [name: "level", value: Math.round(bulb.value.action.bri * 100 / 255)])
                        if (bulb.value.action.sat) 
                        {
                            def hue = Math.min(Math.round(bulb.value.action.hue * 100 / 65535), 65535) as int
                            def sat = Math.round(bulb.value.action.sat * 100 / 255) as int
                            def hex = colorUtil.hslToHex(hue, sat)
                            sendEvent(d.deviceNetworkId, [name: "color", value: hex])
                            sendEvent(d.deviceNetworkId, [name: "hue", value: hue])
                            sendEvent(d.deviceNetworkId, [name: "saturation", value: sat])
                        }
                    }
	                        
                }
             }

   		}     
		else
		{ //put response
			def hsl = [:]
			body.each { payload ->
				log.debug $payload
				if (payload?.success)
				{

					def childDeviceNetworkId = app.id + "/"
					def eventType
					body?.success[0].each { k,v ->
						log.trace "********************************************************"
						log.debug "********************************************************"
						if(k.split("/")[1] == "groups")
						{
							childDeviceNetworkId += k.split("/")[2] + "g"
						}
						else
						{
							childDeviceNetworkId += k.split("/")[2]
						}
						if (!hsl[childDeviceNetworkId]) hsl[childDeviceNetworkId] = [:]
						eventType = k.split("/")[4]
						log.debug "eventType: $eventType"
						switch(eventType) {
							case "on":
								sendEvent(childDeviceNetworkId, [name: "switch", value: (v == true) ? "on" : "off"])
								break
							case "bri":
								sendEvent(childDeviceNetworkId, [name: "level", value: Math.round(v * 100 / 255)])
								break
							case "sat":
								hsl[childDeviceNetworkId].saturation = Math.round(v * 100 / 255) as int
								break
							case "hue":
								hsl[childDeviceNetworkId].hue = Math.min(Math.round(v * 100 / 65535), 65535) as int
								break
						}
					}

				}
				else if (payload.error)
				{
					log.debug "JSON error - ${body?.error}"
				}

			}

			hsl.each { childDeviceNetworkId, hueSat ->
				if (hueSat.hue && hueSat.saturation) {
					def hex = colorUtil.hslToHex(hueSat.hue, hueSat.saturation)
					log.debug "sending ${hueSat} for ${childDeviceNetworkId} as ${hex}"
					sendEvent(hsl.childDeviceNetworkId, [name: "color", value: hex])
				}
			}

		}
	} else {
		log.debug "parse - got something other than headers,body..."
		return []
	}
}


def on(childDevice, transitiontime, percent) {
	def level = Math.min(Math.round(percent * 255 / 100), 255)
	def value = [on: true, bri: level]
    value.transitiontime = transitiontime * 10
	log.debug "Executing 'on'"
	put("lights/${getId(childDevice)}/state", value)
}

def groupOn(childDevice, transitiontime, percent) {
	def level = Math.min(Math.round(percent * 255 / 100), 255)
	def value = [on: true, bri: level]
	value.transitiontime = transitiontime * 10
	log.debug "Executing 'on'"
	put("groups/${getId(childDevice)}/action", value)
}

def off(childDevice, transitiontime) {
	def value = [on: false]
    value.transitiontime = transitiontime * 10
	log.debug "Executing 'off'"
	put("lights/${getId(childDevice)}/state", value)
}

def groupOff(childDevice, transitiontime) {
	log.debug "Executing 'off'"
    def value = [on: false]
	value.transitiontime = transitiontime * 10
	put("groups/${getId(childDevice)}/action", value)
}


def setLevel(childDevice, percent, transitiontime) {
	log.debug "Executing 'setLevel'"
	def level = Math.min(Math.round(percent * 255 / 100), 255)
	def value = [bri: level, on: percent > 0, transitiontime: transitiontime * 10]
	put("lights/${getId(childDevice)}/state", value)
}

def setGroupLevel(childDevice, percent, transitiontime) {
	log.debug "Executing 'setLevel'"
	def level = Math.min(Math.round(percent * 255 / 100), 255)
	def value = [bri: level, on: percent > 0, transitiontime: transitiontime * 10]
	put("groups/${getId(childDevice)}/action", value)
}

def setSaturation(childDevice, percent, transitiontime) {
	log.debug "Executing 'setSaturation($percent)'"
	def level = Math.min(Math.round(percent * 255 / 100), 255)
	put("lights/${getId(childDevice)}/state", [sat: level, transitiontime: transitiontime * 10])
}

def setGroupSaturation(childDevice, percent, transitiontime) {
	log.debug "Executing 'setSaturation($percent)'"
	def level = Math.min(Math.round(percent * 255 / 100), 255)
	put("groups/${getId(childDevice)}/action", [sat: level, transitiontime: transitiontime * 10])
}

def setHue(childDevice, percent, transitiontime) {
	log.debug "Executing 'setHue($percent)'"
	def level =	Math.min(Math.round(percent * 65535 / 100), 65535)
	put("lights/${getId(childDevice)}/state", [hue: level, transitiontime: transitiontime * 10])
}

def setGroupHue(childDevice, percent, transitiontime) {
	log.debug "Executing 'setHue($percent)'"
	def level =	Math.min(Math.round(percent * 65535 / 100), 65535)
	put("groups/${getId(childDevice)}/action", [hue: level, transitiontime: transitiontime * 10])
}

def setGroupScene(childDevice, Number inGroupID, Number inTime) {
	log.trace "setGroupScene: received inGroupID of ${inGroupID} and transitionTime of ${inTime}."
	def sceneID = getId(childDevice) - "s"
    def groupID = inGroupID ?: "0"
	log.debug "setGroupScene: scene = ${sceneID} "
    String path = "groups/${groupID}/action/"
    
	log.debug "Path = ${path} "

	put("${path}", [scene: sceneID, transitiontime: inTime * 10])
}

def setColor(childDevice, color) {
	log.debug "Executing 'setColor($color)'"
	def hue =	Math.min(Math.round(color.hue * 65535 / 100), 65535)
	def sat = Math.min(Math.round(color.saturation * 255 / 100), 255)

	def value = [sat: sat, hue: hue]
	if (color.level != null) {
		value.bri = Math.min(Math.round(color.level * 255 / 100), 255)
		value.on = value.bri > 0
	}

	if (color.transitiontime != null){
		value.transitiontime = color.transitiontime * 10
	}

	if (color.switch) {
		value.on = color.switch == "on"
	}

	log.debug "sending command $value"
	put("lights/${getId(childDevice)}/state", value)
}

def setGroupColor(childDevice, color) {
	log.debug "Executing 'setColor($color)'"
	def hue =	Math.min(Math.round(color.hue * 65535 / 100), 65535)
	def sat = Math.min(Math.round(color.saturation * 255 / 100), 255)

	def value = [sat: sat, hue: hue]
	if (color.level != null) {
		value.bri = Math.min(Math.round(color.level * 255 / 100), 255)
		value.on = value.bri > 0
	}
	if (color.transitiontime != null)
	{
		value.transitiontime = color.transitiontime * 10
	}

	if (color.switch) {
		value.on = color.switch == "on"
	}

	log.debug "sending command $value"
	put("groups/${getId(childDevice)}/action", value)
}

def nextLevel(childDevice) {
	def level = device.latestValue("level") as Integer ?: 0
	if (level < 100) {
		level = Math.min(25 * (Math.round(level / 25) + 1), 100) as Integer
	}
	else {
		level = 25
	}
	setLevel(childDevice,level)
}

private poll() {
	def uri = "/api/${state.username}/lights/"
	log.debug "GET:  $uri"
	sendHubCommand(new physicalgraph.device.HubAction("""GET ${uri} HTTP/1.1
HOST: ${selectedHue}

""", physicalgraph.device.Protocol.LAN, "${selectedHue}"))

	uri = "/api/${state.username}/groups/"
	log.debug "GET:  $uri"
	sendHubCommand(new physicalgraph.device.HubAction("""GET ${uri} HTTP/1.1
HOST: ${selectedHue}

""", physicalgraph.device.Protocol.LAN, "${selectedHue}"))

	uri = "/api/${state.username}/scenes/"
	log.debug "GET:  $uri"
	sendHubCommand(new physicalgraph.device.HubAction("""GET ${uri} HTTP/1.1
HOST: ${selectedHue}

""", physicalgraph.device.Protocol.LAN, "${selectedHue}"))

}

private getId(childDevice) {
	log.debug "WORKING SPOT"
	if (childDevice.device?.deviceNetworkId?.startsWith("HUE")) {
		log.trace childDevice.device?.deviceNetworkId[3..-1]
		return childDevice.device?.deviceNetworkId[3..-1]
	}
	else {
		return childDevice.device?.deviceNetworkId.split("/")[-1]
	}
}


def getSceneID(childDevice) {
	def scenes = getHueScenes()
	scenes.each { dni ->
		def d = getChildDevice(dni)
		if(d) 
		{
			def hueScene
			if (scenes instanceof java.util.Map) 
			{
				hueScene = scenes.find { (app.id + "/" + it.value.id + "s") == dni }
				d = addChildDevice("zpriddy", "AP Hue Scene", dni, newHueScene?.value.hub, ["label":newHueScene?.value.name, "sceneID":newHueScene?.value.id])
			} 

			log.debug "created ${d.displayName} with id $dni"
			d.refresh()
		} 
		
	}
}


private put(path, body) {
	def uri = "/api/${state.username}/$path"
	if(path.startsWith("groups"))
	{
		log.debug "MODIFY GROUPS"
		uri = "/api/${state.username}/$path"[0..-1]

	}
	def bodyJSON = new groovy.json.JsonBuilder(body).toString()
	def length = bodyJSON.getBytes().size().toString()

	log.debug "PUT:  $uri"
	log.debug "BODY: ${bodyJSON}"

	sendHubCommand(new physicalgraph.device.HubAction("""PUT $uri HTTP/1.1
HOST: ${selectedHue}
Content-Length: ${length}

${bodyJSON}
""", physicalgraph.device.Protocol.LAN, "${selectedHue}"))

}

private Integer convertHexToInt(hex) {
	Integer.parseInt(hex,16)
}

private String convertHexToIP(hex) {
	[convertHexToInt(hex[0..1]),convertHexToInt(hex[2..3]),convertHexToInt(hex[4..5]),convertHexToInt(hex[6..7])].join(".")
}

private Boolean canInstallLabs()
{
	return hasAllHubsOver("000.011.00603")
}

private Boolean hasAllHubsOver(String desiredFirmware)
{
	return realHubFirmwareVersions.every { fw -> fw >= desiredFirmware }
}

private List getRealHubFirmwareVersions()
{
	return location.hubs*.firmwareVersionString.findAll { it }
}


def convertBulbListToMap() {
	try {
		if (state.bulbs instanceof java.util.List) {
			def map = [:]
			state.bulbs.unique {it.id}.each { bulb ->
				map << ["${bulb.id}":["id":bulb.id, "name":bulb.name, "hub":bulb.hub]]
			}
			state.bulbs = map
		}
	}
	catch(Exception e) {
		log.error "Caught error attempting to convert bulb list to map: $e"
	}
}


def convertGroupListToMap() {
	log.debug "CONVERT LIST"
	try {
		if (state.groups instanceof java.util.List) {
			def map = [:]
			state.groups.unique {it.id}.each { group ->
				map << ["${group.id}g":["id":group.id+"g", "name":group.name, "hub":group.hub]]
			}
			state.group = map
		}
	}
	catch(Exception e) {
		log.error "Caught error attempting to convert group list to map: $e"
	}
}


def convertSceneListToMap() {
	log.debug "CONVERT LIST"
	try {
		if (state.scenes instanceof java.util.List) {
			def map = [:]
			state.scenes.unique {it.id}.each { scene ->
				map << ["${scene.id}s":["id":scene.id+"s", "name":scene.name, "hub":scene.hub]]
			}
			state.scene = map
		}
	}
	catch(Exception e) {
		log.error "Caught error attempting to convert scene list to map: $e"
	}
}


def ipAddressFromDni(dni) {
	if (dni) {
		def segs = dni.split(":")
		convertHexToIP(segs[0]) + ":" +  convertHexToInt(segs[1])
	} else {
		null
	}
}


def getBridgeDni() {
	state.hostname
}


def getBridgeHostname() {
	def dni = state.hostname
	if (dni) {
		def segs = dni.split(":")
		convertHexToIP(segs[0])
	} else {
		null
	}
}

def getBridgeHostnameAndPort() {
	def result = null
	def dni = state.hostname
	if (dni) {
		def segs = dni.split(":")
		result = convertHexToIP(segs[0]) + ":" +  convertHexToInt(segs[1])
	}
	log.trace "result = $result"
	result
}
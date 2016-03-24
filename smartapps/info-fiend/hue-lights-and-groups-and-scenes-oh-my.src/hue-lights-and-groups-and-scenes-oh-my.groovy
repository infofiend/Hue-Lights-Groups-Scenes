/**
 *  Hue Lights and Groups and Scenes (OH MY) - new Hue Service Manager
 *
 *  Authors: Anthony Pastor (infofiend) and Clayton (claytonjn)
 *
 */
 
definition(
    name: "Hue Lights and Groups and Scenes (OH MY)",
    namespace: "info_fiend",
    author: "Anthony Pastor",
	description: "Allows you to connect your Philips Hue lights with SmartThings and control them from your Things area or Dashboard in the SmartThings Mobile app. Adjust colors by going to the Thing detail screen for your Hue lights (tap the gear on Hue tiles).\n\nPlease update your Hue Bridge first, outside of the SmartThings app, using the Philips Hue app.",
	category: "SmartThings Labs",
	iconUrl: "https://s3.amazonaws.com/smartapp-icons/Partner/hue.png",
	iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Partner/hue@2x.png",
	singleInstance: true
)

preferences {
	page(name:"mainPage", title:"Hue Device Setup", content:"mainPage", refreshTimeout:5)
	page(name:"bridgeDiscovery", title:"Hue Bridge Discovery", content:"bridgeDiscovery", refreshTimeout:5)
	page(name:"bridgeBtnPush", title:"Linking with your Hue", content:"bridgeLinking", refreshTimeout:5)
	page(name:"bulbDiscovery", title:"Bulb Discovery", content:"bulbDiscovery", refreshTimeout:5)
	page(name:"groupDiscovery", title:"Group Discovery", content:"groupDiscovery", refreshTimeout:5)        
	page(name:"sceneDiscovery", title:"Scene Discovery", content:"sceneDiscovery", refreshTimeout:5)
    page(name:"defaultTransition", title:"Default Transition", content:"defaultTransition", refreshTimeout:5)
}

def mainPage() {
	def bridges = bridgesDiscovered()
	if (state.username && bridges) {
		return bulbDiscovery()
	} else {
		return bridgeDiscovery()
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
	if(((bridgeRefreshCount % 3) == 0) && ((bridgeRefreshCount % 5) != 0)) {
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

	return dynamicPage(name:"sceneDiscovery", title:"Scene Discovery Started!", nextPage:"defaultTransition", refreshInterval:refreshInterval, uninstall: true) {
		section("Please wait while we discover your Hue Scenes. Discovery can take a few minutes, so sit back and relax! Select your device below once discovered.") {
			input "selectedScenes", "enum", required:false, title:"Select Hue Scenes (${numFoundScenes} found)", multiple:true, options:optionsScenes
		}
		section {
			def title = bridgeDni ? "Hue bridge (${bridgeHostname})" : "Find bridges"
			href "bridgeDiscovery", title: title, description: "", state: selectedHue ? "complete" : "incomplete", params: [override: true]

		}
	}
}

def defaultTransition()
{
	int sceneRefreshCount = !state.sceneRefreshCount ? 0 : state.sceneRefreshCount as int
	state.sceneRefreshCount = sceneRefreshCount + 1
	def refreshInterval = 3

	return dynamicPage(name:"defaultTransition", title:"Default Transition", nextPage:"", refreshInterval:refreshInterval, install:true, uninstall: true) {
		section("Choose how long bulbs should take to transition between on/off and color changes. This can be modified per-device.") {
			input "selectedTransition", "number", required:true, title:"Transition Time (seconds)", value: 1
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

def manualRefresh() {
    unschedule()
	unsubscribe()
    doDeviceSync()
    runEvery5Minutes("doDeviceSync")
}

def uninstalled(){
	state.bridges = [:]
    state.username = null
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
					d = addChildDevice("info_fiend", "AP Hue Lux Bulb", dni, newHueBulb?.value.hub, ["label":newHueBulb?.value.name])
				} else {
					d = addChildDevice("info_fiend", "AP Hue Bulb", dni, newHueBulb?.value.hub, ["label":newHueBulb?.value.name])
				}
				d.refresh()
			} else { 
            	//backwards compatable
				newHueBulb = bulbs.find { (app.id + "/" + it.id) == dni }
				d = addChildDevice("info_fiend", "AP Hue Bulb", dni, newHueBulb?.hub, ["label":newHueBulb?.name])
				d.refresh()
			}

			log.debug "created ${d.displayName} with id $dni"
		} else {
			log.debug "found ${d.displayName} with id $dni already exists, type: '$d.typeName'"
			if (bulbs instanceof java.util.Map) {
            	def newHueBulb = bulbs.find { (app.id + "/" + it.value.id) == dni }
				if (newHueBulb?.value?.type?.equalsIgnoreCase("Dimmable light") && d.typeName == "Hue Bulb") {
					d.setDeviceType("AP Hue Lux Bulb")
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
				d = addChildDevice("info_fiend", "AP Hue Group", dni, newHueGroup?.value.hub, ["label":newHueGroup?.value.name, "groupID":newHueGroup?.value.id])
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
				d = addChildDevice("info_fiend", "AP Hue Scene", dni, newHueScene?.value.hub, ["label":newHueScene?.value.name, "sceneID":newHueScene?.value.id])
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
			d = addChildDevice("info_fiend", "AP Hue Bridge", selectedHue, vbridge.value.hub, ["data":["mac": vbridge.value.mac]]) // ["preferences":["ip": vbridge.value.ip, "port":vbridge.value.port, "path":vbridge.value.ssdpPath, "term":vbridge.value.ssdpTerm]]

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
		else if(type?.contains("json") && isValidSource(parsedEvent.mac))
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

def isValidSource(macAddress) {
	def vbridges = getVerifiedHueBridges()
	return (vbridges?.find {"${it.value.mac}" == macAddress}) != null
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
                                    if (bulb.value.state.ct) {
                                    	def ct = mireksToKelvin(bulbe.value.state.ct) as int
                                        sendEvent(d.deviceNetworkId, [name: "colorTemperature", value: ct])
                                    }
                                    if (bulb.value.state.effect) { sendEvent(d.deviceNetworkId, [name: "effect", value: bulb.value.state.effect]) }
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
                                    if (bulb.value.state.ct) {
                                    	def ct = 2710
                                        sendEvent(d.deviceNetworkId, [name: "colorTemperature", value: ct])
                                    }
                                    if (bulb.value.state.effect) { sendEvent(d.deviceNetworkId, [name: "effect", value: bulb.value.state.effect]) }
		                     }
		                 }
	                 }
	             }

	        bulbs = getChildDevices()
            for (bulb in body) {
                def d = bulbs.find{it.deviceNetworkId == "${app.id}/${bulb.key}g"}    
                if (d) {
                
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
                        if (bulb.value.state.ct) {
                             def ct = mireksToKelvin(bulbe.value.state.ct) as int
                             sendEvent(d.deviceNetworkId, [name: "colorTemperature", value: ct])
                         }
                        if (bulb.value.state.effect) { sendEvent(d.deviceNetworkId, [name: "effect", value: bulb.value.state.effect]) }
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
                            case "ct":
                            	sendEvent(chileDeviceNetworkId, [name: "colorTemperature", value: mireksToKelvin(v)])
                                break
                            case "effect":
                            	sendEvent(chileDeviceNetworkId, [name: "effect", value: v])
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

def hubVerification(bodytext) {
	log.trace "Bridge sent back description.xml for verification"
    def body = new XmlSlurper().parseText(bodytext)
    if (body?.device?.modelName?.text().startsWith("Philips hue bridge")) {
        def bridges = getHueBridges()
        def bridge = bridges.find {it?.key?.contains(body?.device?.UDN?.text())}
        if (bridge) {
            bridge.value << [name:body?.device?.friendlyName?.text(), serialNumber:body?.device?.serialNumber?.text(), verified: true]
        } else {
            log.error "/description.xml returned a bridge that didn't exist"
        }
    }
}

def on(childDevice, transitiontime, percent, deviceType = "lights") {
	def api = "state" //lights
    if(deviceType == "groups") { api = "action" }
    
    def level = Math.min(Math.round(percent * 255 / 100), 255)
	def value = [on: true, bri: level]
    value.transitiontime = transitiontime * 10
	log.debug "Executing 'on'"
	put("${deviceType}/${getId(childDevice)}/${api}", value)
}

def off(childDevice, transitiontime, deviceType = "lights") {
	def api = "state" //lights
    if(deviceType == "groups") { api = "action" }
    
	def value = [on: false]
    value.transitiontime = transitiontime * 10
	log.debug "Executing 'off'"
	put("${deviceType}/${getId(childDevice)}/${api}", value)
}

def setLevel(childDevice, percent, transitiontime, deviceType = "lights") {
	def api = "state" //lights
    if(deviceType == "groups") { api = "action" }
    
	log.debug "Executing 'setLevel'"
	def level = Math.min(Math.round(percent * 255 / 100), 255)
	def value = [bri: level, on: percent > 0, transitiontime: transitiontime * 10]
	put("${deviceType}/${getId(childDevice)}/${api}", value)
}

def setSaturation(childDevice, percent, transitiontime, deviceType = "lights") {
	def api = "state" //lights
    if(deviceType == "groups") { api = "action" }
    
	log.debug "Executing 'setSaturation($percent)'"
	def level = Math.min(Math.round(percent * 255 / 100), 255)
	put("${deviceType}/${getId(childDevice)}/${api}", [sat: level, transitiontime: transitiontime * 10])
}

def setHue(childDevice, percent, transitiontime, deviceType = "lights") {
	def api = "state" //lights
    if(deviceType == "groups") { api = "action" }
    	
	log.debug "Executing 'setHue($percent)'"
	def level =	Math.min(Math.round(percent * 65535 / 100), 65535)
	put("${deviceType}/${getId(childDevice)}/${api}", [hue: level, transitiontime: transitiontime * 10])
}

def setColorTemperature(childDevice, huesettings, transitionTime, deviceType = "lights") {
	def api = "state" //lights
    if(deviceType == "groups") { api = "action" }
    
	log.debug "Executing 'setColorTemperature($huesettings)'"
	def value = [ct: kelvinToMireks(huesettings), transitiontime: transitionTime * 10, on: true]
	log.trace "sending command $value"
	put("${deviceType}/${getId(childDevice)}/${api}", value)
}

def setColor(childDevice, color, deviceType = "lights") {
	def api = "state" //lights
    if(deviceType == "groups") { api = "action" }
    
	childDevice?.log "Executing 'setColor($color)'"
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

	childDevice?.log "sending command $value"
	put("${deviceType}/${getId(childDevice)}/${api}", value)
}

def setGroupScene(childDevice, Number inGroupID) {
	childDevice?.log "setGroupScene: received inGroupID of ${inGroupID}." // and transitionTime of ${inTime}."
	def sceneID = getId(childDevice) // - "s"
    def groupID = inGroupID ?: "0"
	childDevice?.log "setGroupScene: scene = ${sceneID} "
    String path = "groups/${groupID}/action/"
    
	childDevice?.log "Path = ${path} "

	put("${path}", [scene: sceneID]) // , transitiontime: inTime * 10])
}

def setToGroup(childDevice, Number inGroupID ) {
	childDevice?.log "setToGroup: received inGroupID of ${inGroupID}." //  and transitionTime of ${inTime}."
	def sceneID = getId(childDevice) - "s"
    def groupID = inGroupID ?: "0"
//    def newTT = inTime as Integer
    
	childDevice?.log "setToGroup: sceneID = ${sceneID} "
    String gPath = "groups/${groupID}/action/"
    
//    String sPath = "scenes/${sceneID}/"
    
//	log.debug "Scene path = ${sPath} "

//	put("${sPath}", [transitiontime: newTT * 10])
    
	childDevice?.log "Group path = ${gPath} "

	put("${gPath}", [scene: sceneID])
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

def getId(childDevice) {
	childDevice?.log "Executing getId"
	if (childDevice.device?.deviceNetworkId?.startsWith("HUE")) {
		log.trace childDevice.device?.deviceNetworkId[3..-1]
		return childDevice.device?.deviceNetworkId[3..-1]
	}
	else {
		return childDevice.device?.deviceNetworkId.split("/")[-1]
	}
}

/** 
def getSceneId(childDevice) {
	def scenes = getHueScenes()
	scenes.each { dni ->
		def d = getChildDevice(dni)
		if(d) 
		{
			def hueScene
			if (scenes instanceof java.util.Map) 
			{
				hueScene = scenes.find { (app.id + "/" + it.value.id + "s") == dni }
				d = addChildDevice("info_fiend", "AP Hue Scene", dni, newHueScene?.value.hub, ["label":newHueScene?.value.name, "sceneID":newHueScene?.value.id])
			} 

			log.debug "created ${d.displayName} with id $dni"
			d.refresh()
		} 
		
	}
}
**/

def updateScene(childDevice) {
	childDevice?.log "updateScene: Scene ${childDevice} requests scene use current light states."
	def sceneID = getId(childDevice) - "s"

	childDevice?.log "updateScene: sceneID = ${sceneID} "
    String path = "scenes/${sceneID}/"

	def value = [storelightstate: true]
	log.debug "Path = ${path} "

	put("scenes/${sceneID}/", value)
}

def updateSceneUsingID(childDevice, sceneID) {
//	log.trace "updateScene: Scene ${childDevice} requests scene use current light states."

	childDevice?.log "parent.updateSceneUsingID: child's sceneID = ${sceneID} ","debug"
    childDevice?.log "scenes/${sceneID}/" 

//	log.debug "path = ${path} ."

// 	def value = [storelightstate: true]
//	log.debug "updateSceneUsingID: first attempt: "
//	put("scenes/${sceneID}/", value)
    
	childDevice?.log "updateSceneUsingID: first attempt " 
    
   	put("${path}", ["storelightstate": true])
}

/**
def updateTransTime(childDevice, newTT) {
	def sceneID = getId(childDevice) - "s"
	log.debug "updateTransTime: new transition time of ${newTT} for Scene ${sceneID}."
    def transTime = newTT * 10
    
    String path = "scenes/${sceneID}/"
    
	log.debug "Path = ${path} "

	put("${path}", [transitiontime: transTime])
    
}

**/

def deleteScene(childDevice) {
	childDevice?.log "deleteScene: Delete scene ${childDevice}."
	def sceneID = getId(childDevice) - "s"

	childDevice?.log "deleteScene: sceneID = ${sceneID} "
    String path = "scenes/${sceneID}/"
    
//	log.debug "Path = ${path} "

	delete("${path}")
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

private put(path, body) {
	def uri = "/api/${state.username}/$path"
	if(path.startsWith("groups"))
	{
//		log.debug "MODIFY GROUPS"
		uri = "/api/${state.username}/$path"[0..-1]

	}
    if(path.startsWith("scenes"))
	{
//		log.debug "MODIFY SCENES"
		uri = "/api/${state.username}/$path"[0..-1]

	}
    
	def bodyJSON = new groovy.json.JsonBuilder(body).toString()
//	def length = bodyJSON.getBytes().size().toString()

	childDevice?.log "PUT:  $uri"
	childDevice?.log "BODY: body"  // ${bodyJSON}"
//

sendHubCommand(new physicalgraph.device.HubAction([
method: "PUT",
path: uri,
headers: [
HOST: selectedHue
],
body: body], "${selectedHue}"))

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

private getHextoXY(String colorStr) {
    // For the hue bulb the corners of the triangle are:
    // -Red: 0.675, 0.322
    // -Green: 0.4091, 0.518
    // -Blue: 0.167, 0.04

    def cred = Integer.valueOf( colorStr.substring( 1, 3 ), 16 )
    def cgreen = Integer.valueOf( colorStr.substring( 3, 5 ), 16 )
    def cblue = Integer.valueOf( colorStr.substring( 5, 7 ), 16 )

    double[] normalizedToOne = new double[3];
    normalizedToOne[0] = (cred / 255);
    normalizedToOne[1] = (cgreen / 255);
    normalizedToOne[2] = (cblue / 255);
    float red, green, blue;

    // Make red more vivid
    if (normalizedToOne[0] > 0.04045) {
       red = (float) Math.pow(
                (normalizedToOne[0] + 0.055) / (1.0 + 0.055), 2.4);
    } else {
        red = (float) (normalizedToOne[0] / 12.92);
    }

    // Make green more vivid
    if (normalizedToOne[1] > 0.04045) {
        green = (float) Math.pow((normalizedToOne[1] + 0.055)
                / (1.0 + 0.055), 2.4);
    } else {
        green = (float) (normalizedToOne[1] / 12.92);
    }

    // Make blue more vivid
    if (normalizedToOne[2] > 0.04045) {
        blue = (float) Math.pow((normalizedToOne[2] + 0.055)
                / (1.0 + 0.055), 2.4);
    } else {
        blue = (float) (normalizedToOne[2] / 12.92);
    }

    float X = (float) (red * 0.649926 + green * 0.103455 + blue * 0.197109);
    float Y = (float) (red * 0.234327 + green * 0.743075 + blue * 0.022598);
    float Z = (float) (red * 0.0000000 + green * 0.053077 + blue * 1.035763);

    float x = X / (X + Y + Z);
    float y = Y / (X + Y + Z);

    double[] xy = new double[2];
    xy[0] = x;
    xy[1] = y;
    return xy;
}

private Integer convertHexToInt(hex) {
	Integer.parseInt(hex,16)
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

private String convertHexToIP(hex) {
	[convertHexToInt(hex[0..1]),convertHexToInt(hex[2..3]),convertHexToInt(hex[4..5]),convertHexToInt(hex[6..7])].join(".")
}

private Boolean hasAllHubsOver(String desiredFirmware)
{
	return realHubFirmwareVersions.every { fw -> fw >= desiredFirmware }
}

private List getRealHubFirmwareVersions()
{
	return location.hubs*.firmwareVersionString.findAll { it }
}

def ipAddressFromDni(dni) {
	if (dni) {
		def segs = dni.split(":")
		convertHexToIP(segs[0]) + ":" +  convertHexToInt(segs[1])
	} else {
		null
	}
}

def getSelectedTransition() {
	return settings.selectedTransition
}

def setEffect(childDevice, effect, deviceType = "lights") {
	def api = "state" //lights
    if(deviceType == "groups") { api = "action" }
    
	def value = [effect: effect]
	childDevice?.log "setEffect: Effect ${effect}."
	put("${deviceType}/${getId(childDevice)}/${api}", value)
}

int kelvinToMireks(kelvin) {
	return 1000000 / kelvin //https://en.wikipedia.org/wiki/Mired
}

int mireksToKelvin(mireks) {
	return 1000000 / mireks //https://en.wikipedia.org/wiki/Mired
}
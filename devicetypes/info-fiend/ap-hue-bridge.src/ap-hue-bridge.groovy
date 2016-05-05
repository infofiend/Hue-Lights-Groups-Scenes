/**
 *  AP Hue Bridge
 *
 *  Version 1.2 Added log 
 *  Version 1.3 Conformed to new ST HUE BRIDGE code
 *
 *  Authors: Anthony Pastor (infofiend) and Clayton (claytonjn)
 */

// for the UI
metadata {
	// Automatically generated. Make future change here.
	definition (name: "AP Hue Bridge", namespace: "info_fiend", author: "SmartThings") {
		capability "Refresh"
        
        command "log", ["string","string"]

		attribute "serialNumber", "string"
		attribute "networkAddress", "string"
        
	}


	tiles(scale: 2) {
     	multiAttributeTile(name:"rich-control"){
			tileAttribute ("", key: "PRIMARY_CONTROL") {
	            attributeState "default", label: "Hue Bridge", action: "", icon: "st.Lighting.light99-hue", backgroundColor: "#F3C200"
			}
	        tileAttribute ("serialNumber", key: "SECONDARY_CONTROL") {
	            attributeState "default", label:'SN: ${currentValue}'
			}
        }
		
		valueTile("serialNumber", "device.serialNumber", decoration: "flat", height: 1, width: 2, inactiveLabel: false) {
			state "default", label:'SN: ${currentValue}'
		}
		valueTile("networkAddress", "device.networkAddress", decoration: "flat", height: 1, width: 2, inactiveLabel: false) {
			state "default", label:'${currentValue}'
		}

		main (["rich-control"])
		details(["rich-control", "networkAddress" ]) // , "serialNumber", "refresh"])
	}
}

// parse events into attributes
def parse(description) {

	log.debug "Parsing '${description}'"
	def results = []
	def result = parent.parse(this, description)

	if (result instanceof physicalgraph.device.HubAction){
		results << result
	} else if (description == "updated") {
		//do nothing
		log.debug "AP HUE BRIDGE was updated"
	} else {
		def map = description
		if (description instanceof String)  {
			map = stringToMap(description)
		}
		if (map?.name && map?.value) {
        	log.trace "AP HUE BRIDGE, GENERATING EVENT: $map.name: $map.value"
			results << createEvent(name: "${map?.name}", value: "${map?.value}")
		}
		else {
			log.trace "AP HUE BRIDGE, Parsing Description"
			def msg = parseLanMessage(description)
			if (msg.body) {
				def contentType = msg.headers["Content-Type"]
				if (contentType?.contains("json")) {
					def bgs = new groovy.json.JsonSlurper().parseText(msg.body)
                    boolean isGroup = false
                    boolean isScene = false
                    bgs.each{

                    	isScene = it.toString().contains( "lastupdated" )
                        isGroup = it.toString().contains( "action" )
                    }
					//log.info "BULBS: $bulbs"
					if (bgs.state) {
						log.info "Bridge response: $msg.body"
					}

					else {
						log.trace "AP HUE BRIDGE, GENERATING BULB LIST EVENT"
                        if(isScene)
                        {
                        	log.trace "Sending Scene List to Parent: ${bgs}"
							sendEvent(name: "sceneList", value: device.hub.id, isStateChange: true, data: bgs)
                        } else if(isGroup)
                        {
                        	log.trace "Sending Group List to Parent: ${bgs}"
							sendEvent(name: "groupList", value: device.hub.id, isStateChange: true, data: bgs)
                        }
                        else
                        {
                        	log.trace "Sending Bulb List to Parent: ${bgs}"
							sendEvent(name: "bulbList", value: device.hub.id, isStateChange: true, data: bgs)
                        }
					}
				}
				else if (contentType?.contains("xml")) {
					log.debug "HUE BRIDGE, SWALLOWING BRIDGE DESCRIPTION RESPONSE -- BRIDGE ALREADY PRESENT"
					parent.hubVerification(device.hub.id, msg.body)
				}
			}
		}
	}
	results
}

/**
def poll() {
	log.debug "Executing 'polling'"
	parent.poll()
}

def refresh() {
	log.debug "Executing 'refresh'"
	parent.manualRefresh()
}

**/


def log(message, level = "trace") {
	switch (level) {
    	case "trace":
        	log.trace "LOG FROM PARENT>" + message
            break;

    	case "debug":
        	log.debug "LOG FROM PARENT>" + message
            break

    	case "warn":
        	log.warn "LOG FROM PARENT>" + message
            break

    	case "error":
        	log.error "LOG FROM PARENT>" + message
            break

        default:
        	log.error "LOG FROM PARENT>" + message
            break;
    }

    return null // always child interface call with a return value
}

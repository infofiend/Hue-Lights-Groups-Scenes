/**
 *  AP Hue Bridge
 *
 *  Authors: Anthony Pastor (infofiend) and Clayton (claytonnj)
 */
// for the UI
metadata {
	// Automatically generated. Make future change here.
	definition (name: "AP Hue Bridge", namespace: "info_fiend", author: "SmartThings") {
		capability "Refresh"  
        
		attribute "serialNumber", "string"		
		attribute "networkAddress", "string"        
	}

	simulator {
		// TODO: define status and reply messages here
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
		standardTile("refresh", "device.switch", inactiveLabel: false, decoration: "flat") {
            state "default", label:"", action:"refresh.refresh", icon:"st.secondary.refresh"
        }
		valueTile("serialNumber", "device.serialNumber", decoration: "flat", height: 1, width: 2, inactiveLabel: false) {
			state "default", label:'SN: ${currentValue}'
		}
		valueTile("networkAddress", "device.networkAddress", decoration: "flat", height: 2, width: 4, inactiveLabel: false) {
			state "default", label:'${currentValue}', height: 1, width: 2, inactiveLabel: false
		}

		main (["rich-control"])
		details(["rich-control", "networkAddress", "refresh", "serialNumber"])
	}
}

// parse events into attributes
def parse(description) {
	def results = []
	def result = parent.parse(this, description)

	if (result instanceof physicalgraph.device.HubAction){
		results << result
	} else if (description == "updated") {
		//do nothing
		log.debug "Hue Bridge was updated"
	} else {
		def map = description
		if (description instanceof String)  {
			map = stringToMap(description)
		}
		if (map?.name && map?.value) {
			results << createEvent(name: "${map?.name}", value: "${map?.value}")
		}
		else {
			log.trace "HUE BRIDGE, OTHER"
			def msg = parseLanMessage(description)
			if (msg.body) {
				def contentType = msg.headers["Content-Type"]
				if (contentType?.contains("json")) {
					def bgs = new groovy.json.JsonSlurper().parseText(msg.body)
                    boolean isGroup = false
                    boolean isScene = false
                    bgs.each{
                    	
                    	isScene = it.toString().contains( "recycle" )
                        isGroup = it.toString().contains( "lights" )
                    }
					//log.info "BULBS: $bulbs"
					if (bgs.state) {
						log.warn "NOT PROCESSED: $msg.body"
					}
                    
					else {
						log.trace "HUE BRIDGE, GENERATING BULB LIST EVENT"
                        if(isScene)
                        {
                        	log.trace "Sending Scene List: ${bgs}"
							sendEvent(name: "sceneList", value: device.hub.id, isStateChange: true, data: bgs)
                        } else if(isGroup)
                        {
                        	log.trace "Sending Group List: ${bgs}"
							sendEvent(name: "groupList", value: device.hub.id, isStateChange: true, data: bgs)
                        }
                        else
                        {
                        	log.trace "Sending Bulb List: ${bgs}"
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

def poll() {
	log.debug "Executing 'polling'"
	parent.poll()
}

def refresh() {
	log.debug "Executing 'refresh'"
	parent.poll()
}

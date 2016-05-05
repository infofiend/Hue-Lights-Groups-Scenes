/**
 *  AP Hue Scene
 *
 *	Version 1.3: Fixed getSceneID
 *				 Fixed updateScene
 *
 *  Authors: Anthony Pastor (infofiend) and Clayton (claytonnj)
 */
// for the UI
metadata {
    // Automatically generated. Make future change here.
    definition (name: "AP Hue Scene", namespace: "info_fiend", author: "Anthony Pastor") {
        capability "Actuator"
        capability "Switch"
        capability "Momentary"
        capability "Sensor"
		capability "Refresh"
        capability "Polling"

        attribute "sceneID", "STRING"
        attribute "getSceneID", "STRING"
        attribute "updateScene", "STRING"
		attribute "lights", "STRING"
        attribute "group", "NUMBER"
//        attribute "inGroupID", "NUMBER"

		command "setToGroup"
        command "updateScene"
        command "getSceneID"
        command "deleteScene"

		command "log", ["STRING", "STRING"]

    }

    // simulator metadata
    simulator {
    }

	tiles (scale: 2) {
	    multiAttributeTile(name:"switch", type: "generic", width: 6, height: 4, canChangeIcon: true){
			tileAttribute ("device.switch", key: "PRIMARY_CONTROL") {
				attributeState "on",  label:'Push', action:"momentary.push", icon:"st.lights.philips.hue-multi", backgroundColor:"#F505F5"
			}
		
//        	tileAttribute ("lights", key: "SECONDARY_CONTROL") {
//                attributeState "lights", label:'The scene controls Hue lights ${currentValue}.'
//            }
		}
    
	    standardTile("refresh", "device.switch", inactiveLabel: false, decoration: "flat", width: 2, height: 2) {
			state "default", label:"", action:"refresh.refresh", icon:"st.secondary.refresh"
		}

    	standardTile("sceneID", "device.sceneID", inactiveLabel: false, decoration: "flat", width: 3, height: 2) { //, defaultState: "State1"
	       	state "sceneID", label: '${currentValue}  SceneID ', action:"getSceneID", backgroundColor:"#BDE5F2" //, nextState: "State2"
    	}

		standardTile("updateScene", "device.updateScene", inactiveLabel: false, decoration: "flat", width: 3, height: 2) {
    	   	state "Ready", label: 'Update   Scene', action:"updateSceneFromDevice", backgroundColor:"#FBB215"
	    }
	
 		valueTile("lights", "device.lights", inactiveLabel: false, decoration: "flat", width: 6, height: 2) {
			state "default", label: 'Lights: ${currentValue}'
        }
        
    main "switch"
    details (["switch", "lights", "updateScene", "sceneID", "refresh"]) 
	}
}

def parse(description) {
	log.debug "parse() - $description"
	def results = []

	def map = description
	if (description instanceof String)  {
		log.debug "Hue Scene stringToMap - ${map}"
		map = stringToMap(description)
	}

	if (map?.name && map?.value) {
		results << createEvent(name: "${map?.name}", value: "${map?.value}")
	}

	results

}



def on() {
    push()    
}

def push () {
	def theGroup = device.currentValue("group") ?: 0
	parent.setToGroup(this, theGroup) 
    sendEvent(name: "momentary", value: "pushed", isStateChange: true)
    parent.poll()
}

def setToGroup ( Integer inGroupID ) {

    parent.setToGroup(this, inGroupID)  
    log.debug "Executing 'setToGroup' for ${device.label} using groupID ${inGroupID}."
    parent.poll()
    
}

def updateSceneFromDevice() {
	log.trace "${this}: Update Scene Reached."

    def sceneIDfromD = device.currentValue("sceneID")

    log.debug "Retrieved sceneIDfromD: ${sceneIDfromD}."

	String myScene = sceneIDfromD

    if (sceneIDfromD == null) {
    	def sceneIDfromP = parent.getID(this) - "s"
    	log.debug "Retrieved sceneIDfromP: ${sceneIDfromP}."
        myScene = sceneIDfromP
    }

    parent.updateSceneUsingID(this, myScene)
	log.debug "Executing 'updateScene' for ${device.label} using sceneID ${myScene}."

}


def getSceneID() {

	def sceneIDfromP = parent.getId(this) - "s"
    def realSceneID = sceneIDfromP - "s"
    log.debug "Retrieved sceneID: ${sceneIDfromP}."
	log.debug "Real sceneID: ${realSceneID}."

    sendEvent(name: "sceneID", value: "${realSceneID}", isStateChange: true)

//	refresh()
}

def poll() {
	parent.poll()
}

def refresh() {
	log.debug "Executing 'refresh'"
	parent.manualRefresh()
}


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

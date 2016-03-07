/**
 *  AP Hue Scene
 *
 *  Author: Anthony Pastor
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
        
        attribute "sceneID", "string"
        attribute "updateScene", "string"

		command "setToGroup"
        command "updateScene"
        command "getSceneID"
        command "deleteScene"
    }

    // simulator metadata
    simulator {
    }

    standardTile("switch", "device.switch", type: "momentary", width: 2, height: 2, canChangeIcon: true) {
		state "on",  label:'Push', action:"momentary.push", icon:"st.lights.philips.hue-multi", backgroundColor:"#F505F5"
	}


    standardTile("refresh", "device.switch", inactiveLabel: false, decoration: "flat") {
		state "default", label:"", action:"refresh.refresh", icon:"st.secondary.refresh"
	}
        
    valueTile("sceneID", "device.sceneID", inactiveLabel: false, decoration: "flat") {
		state "sceneID", label: 'sceneID ${currentValue}   '
	}
        
    standardTile("getSceneID", "device.getSceneID", inactiveLabel: false, decoration: "flat", defaultState: "Ready") {
       	state "Normal", label: 'Get SceneID', action:"getSceneID", backgroundColor:"#BDE5F2", nextState: "Retrieving"
	    state "Retrieving", label: 'Retrieving', backgroundColor: "#ffffff", nextState: "Normal"
    }
        
	standardTile("updateScene", "device.updateScene", decoration: "flat", defaultState: "Ready") {
       	state "Ready", label: 'Update Scene', action:"updateScene", backgroundColor:"#F505F5", nextState: "Updating"
	    state "Updating", label: 'Updating...', backgroundColor: "#ffffff", nextState: "Ready"
    }
    standardTile("deleteScene", "device.deleteScene", decoration: "flat", defaultState: "Ready") {
       	state "Ready", label: 'Delete Scene', action:"deleteScene", backgroundColor:"#F505F5", nextState: "Deleting"
	    state "Deleting", label: 'Deleting...', backgroundColor: "#ffffff", nextState: "Ready"
    }
    
    main "switch"
    details (["switch", "sceneID", "refresh", "getSceneID", "updateScene"])
    
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
	def groupID = 0
    parent.setToGroup(this, groupID, 3)
    sendEvent(name: "momentary", value: "pushed", isStateChange: true)
}

def setToGroup ( Integer inGroupID ) {
	def groupID = inGroupID ?: 0
    parent.setToGroup(this, groupID, 3)
//    sendEvent(name: "momentary", value: "pushed", isStateChange: true)
}

def updateScene() {
	log.trace "${this}: Update Scene Reached."
	parent.updateScene(this)
    
}

def deleteScene() {
	log.trace "${this}: Delete Scene Reached."
	parent.deleteScene(this)
    
}

def getSceneID() {
    log.debug "(this) means ${this} "
    
	def sceneIDfromP = parent.getSceneID(this)
    log.debug "Retrieved sceneID: ${sceneIDfromP}."
   
    sendEvent(name: "sceneID", value: "${sceneIDfromP}", isStateChange: true)

}

def poll() {
	parent.poll()
}

def refresh() {
	log.debug "Executing 'refresh'"
	parent.poll()
}

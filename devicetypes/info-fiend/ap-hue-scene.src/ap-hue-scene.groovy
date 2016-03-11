/**
 *  AP Hue Scene
 *
 *	Version 1.3: Fixed getSceneID
 *				 Fixed updateScene
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
        
        attribute "sceneID", "STRING"
        attribute "getSceneID", "STRING"
        attribute "updateScene", "STRING"
//        attribute "transTime", "NUMBER"

		command "setToGroup"
        command "updateScene"
        command "getSceneID"
//        command "deleteScene"
//        command "setTT"
		command "log", ["string","string"]
        
    }

    // simulator metadata
    simulator {
    }

	tiles (scale: 2) {
	    multiAttributeTile(name:"switch", type: "generic", width: 6, height: 4, canChangeIcon: true){
			tileAttribute ("device.switch", key: "PRIMARY_CONTROL") {
				attributeState "on",  label:'Push', action:"momentary.push", icon:"st.lights.philips.hue-multi", backgroundColor:"#F505F5"
			}
		}

	    standardTile("refresh", "device.switch", inactiveLabel: false, decoration: "flat") {
			state "default", label:"", action:"refresh.refresh", icon:"st.secondary.refresh"
		}
        
//    	valueTile("sceneID", "device.sceneID", decoration: "flat", width: 2, height: 1) {
//			state "sceneID", label: 'HUE sceneID ${currentValue}   '
//		}
        
    	standardTile("sceneID", "device.sceneID", inactiveLabel: false, decoration: "flat", width: 3, height: 2) { //, defaultState: "State1"
	       	state "sceneID", label: 'Hue SceneID: ${currentValue}', action:"getSceneID" // , backgroundColor:"#BDE5F2" //, nextState: "State2"
//		    state "State2", label: 'Retrieving', backgroundColor: "#ffffff", nextState: "State1"
    	}
        
		standardTile("updateScene", "device.updateScene", inactiveLabel: false, decoration: "flat", width: 2, height: 2) {
    	   	state "Ready", label: 'UpdateScene                             ', action:"updateScene", backgroundColor:"#FBB215"
	    }
//    standardTile("deleteScene", "device.deleteScene", decoration: "flat", defaultState: "Ready") {
//       	state "Ready", label: 'Delete Scene', action:"deleteScene", backgroundColor:"#F505F5", nextState: "Deleting"
//	    state "Deleting", label: 'Deleting...', backgroundColor: "#ffffff", nextState: "Ready"
//    }

/**		controlTile("transitiontime", "device.transTime", "slider", inactiveLabel: false,  width: 5, height: 1, range:"(0..4)") { 
       		state "setTT", action:"setTT", backgroundColor:"#d04e00"
		}
    
		valueTile("valueTT", "device.transTime", inactiveLabel: false, decoration: "flat", width: 1, height: 1) {
			state "transTime", label: 'Transition    Time: ${currentValue}'
    	}
    
**/ 
	}
    main "switch"
    details (["switch","updateScene","sceneID","refresh"]) //"getSceneID","transitiontime","valueTT",
    
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

/**
void setTT(transitiontime) {

	log.debug "Executing 'setTT' for ${device.label}: setting transition time to ${transitiontime}."
//	parent.updateTransTime(this, transitiontime)
	sendEvent(name: "transTime", value: transitiontime, isStateChange: true)
    
}
**/

def on() {
    push()
}

def push () {
	def groupID = 0
//    def transitionTime = device.currentValue("transTime")
//	if (transitionTime == null) {
//    	transitionTime = 3
//	}
    parent.setToGroup(this, groupID) // , transitionTime
    sendEvent(name: "momentary", value: "pushed", isStateChange: true)
}

def setToGroup ( Integer inGroupID ) {
	def groupID = inGroupID ?: 0
//    def transitionTime = device.currentValue("transTime") as Integer
//	if (transitionTime == null) {
//    	transitionTime = 3
//	}
    parent.setToGroup(this, groupID)  // , transitionTime
    log.debug "Executing 'setToGroup' for ${device.label} using groupID ${groupID} and." // transition time of ${transitiontime}."
//    sendEvent(name: "momentary", value: "pushed", isStateChange: true)
}

def updateScene() {
	log.trace "${this}: Update Scene Reached."
//    def sceneIDfromP = parent.getId(this) 
    def sceneIDfromD = device.currentValue("sceneID") 

    log.debug "Retrieved sceneIDfromD: ${sceneIDfromD}."
    String myScene = sceneIDfromD
    
    if (sceneIDfromD == null) {
    	def sceneIDfromP = parent.getID(this) - "s"
    	log.debug "Retrieved sceneIDfromP: ${sceneIDfromP}."    
    }
   
    
//    parent.updateScene(this)
//    parent.updateSceneUsingID(this, sceneIDfromP)
    parent.updateSceneUsingID(this, myScene)
    

}

def deleteScene() {
	log.trace "${this}: Delete Scene Reached."
	parent.deleteScene(this)
    
}

def getSceneID() {
 //   log.debug "(this) means ${this} "
    
	def sceneIDfromP = parent.getId(this) - "s" 
    def realSceneID = sceneIDfromP - "s"
    log.debug "Retrieved sceneID: ${sceneIDfromP}."
	log.debug "Real sceneID: ${realSceneID}."
   
    sendEvent(name: "sceneID", value: "${realSceneID}", isStateChange: true)
    // sendEvent(name: "getSceneID", state: "State1", isStateChange: true)
//	refresh()
}

def poll() {
	parent.poll()
}

def refresh() {
	log.debug "Executing 'refresh'"
	parent.poll()
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
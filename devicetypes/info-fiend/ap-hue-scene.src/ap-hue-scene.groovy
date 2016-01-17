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

        attribute "sceneID", "string"

    }

    // simulator metadata
    simulator {
    }

    tiles (scale: 2){
        multiAttributeTile(name:"switch", type: "momentary", width: 6, height: 4, canChangeIcon: true){
            tileAttribute ("device.switch", key: "PRIMARY_CONTROL") {
                attributeState "on",  label:'Push', action:"momentary.push", icon:"st.lights.philips.hue-multi", backgroundColor:"#07A4D2"
            }
            tileAttribute ("sceneID", key: "SECONDARY_CONTROL") {
                attributeState "default", label:'${currentValue}'
            }
        }
        main "switch"
        details "switch"
    }
}

def parse(String description) {
}

def on() {
    push()
}

def push () {
	def groupID = 0
    parent.setGroupScene(this, groupID, 3)
    sendEvent(name: "momentary", value: "pushed", isStateChange: true)
}

def setScene ( Integer inGroupID ) {
	def groupID = inGroupID ?: 0
    parent.setGroupScene(this, groupID, 3)
    sendEvent(name: "momentary", value: "pushed", isStateChange: true)
}
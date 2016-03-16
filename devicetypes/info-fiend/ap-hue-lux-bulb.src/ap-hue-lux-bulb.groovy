/**
 *  AP Hue Lux Bulb
 *
 *  Author: Anthony Pastor
 */
// for the UI
metadata {
	// Automatically generated. Make future change here.
	definition (name: "AP Hue Lux Bulb", namespace: "info_fiend", author: "Anthony Pastor") {
		capability "Switch Level"
		capability "Actuator"
		capability "Switch"
		capability "Refresh"
		capability "Sensor"

		//capability "Test Capability" //Hope to replace with Transistion Time
        
        command "refresh"
        command "setTT"
        command "log", ["string","string"]
        
        attribute "transTime", "NUMBER"
        
	}

	simulator {
		// TODO: define status and reply messages here
	}

	tiles(scale: 2) {
        multiAttributeTile(name:"rich-control", type: "lighting", canChangeIcon: true){
            tileAttribute ("device.switch", key: "PRIMARY_CONTROL") {
              attributeState "on", label:'${name}', action:"switch.off", icon:"st.lights.philips.hue-single", backgroundColor:"#79b821", nextState:"turningOff"
              attributeState "off", label:'${name}', action:"switch.on", icon:"st.lights.philips.hue-single", backgroundColor:"#ffffff", nextState:"turningOn"
              attributeState "turningOn", label:'${name}', action:"switch.off", icon:"st.lights.philips.hue-single", backgroundColor:"#79b821", nextState:"turningOff"
              attributeState "turningOff", label:'${name}', action:"switch.on", icon:"st.lights.philips.hue-single", backgroundColor:"#ffffff", nextState:"turningOn"
            }
            tileAttribute ("device.level", key: "SLIDER_CONTROL") {
              attributeState "level", action:"switch level.setLevel", range:"(0..100)"
            }
            tileAttribute ("device.level", key: "SECONDARY_CONTROL") {
	            attributeState "level", label: 'Level ${currentValue}%'
			}
        }

		standardTile("switch", "device.switch", width: 2, height: 2, canChangeIcon: true) {
			state "on", label:'${name}', action:"switch.off", icon:"st.lights.philips.hue-single", backgroundColor:"#79b821", nextState:"turningOff"
			state "off", label:'${name}', action:"switch.on", icon:"st.lights.philips.hue-single", backgroundColor:"#ffffff", nextState:"turningOn"
			state "turningOn", label:'${name}', action:"switch.off", icon:"st.lights.philips.hue-single", backgroundColor:"#79b821", nextState:"turningOff"
			state "turningOff", label:'${name}', action:"switch.on", icon:"st.lights.philips.hue-single", backgroundColor:"#ffffff", nextState:"turningOn"
        }

        controlTile("levelSliderControl", "device.level", "slider", height: 1, width: 2, inactiveLabel: false, range:"(0..100)") {
            state "level", action:"switch level.setLevel"
        }
        
        controlTile("transitiontime", "device.transTime", "slider", inactiveLabel: false,  width: 5, height: 1, range:"(0..4)") { 
        	state "setTT", action:"setTT", backgroundColor:"#d04e00"
		}
		valueTile("valueTT", "device.transTime", inactiveLabel: false, decoration: "flat", width: 1, height: 1) {
			state "transTime", label: 'Transition    Time: ${currentValue}'
        }

        standardTile("refresh", "device.switch", inactiveLabel: false, decoration: "flat", width: 2, height: 2) {
            state "default", label:"", action:"refresh.refresh", icon:"st.secondary.refresh"
        }

        main(["switch"])
        details(["rich-control", "transitiontime", "valueTT", "refresh"])
    }

}

// parse events into attributes
def parse(description) {
	log.debug "parse() - $description"
	def results = []

	def map = description
	if (description instanceof String)  {
		log.debug "AP Hue Bulb stringToMap - ${map}"
		map = stringToMap(description)
	}

	if (map?.name && map?.value) {
		results << createEvent(name: "${map?.name}", value: "${map?.value}")
	}

	results

}

// handle commands
void setTT(transitiontime) {
	log.debug "Executing 'setTT': transition time is now ${transitiontime}."
	sendEvent(name: "transTime", value: transitiontime, isStateChange: true)
}

void on() {
	def level = device.currentValue("level")
    if(level == null) {
    	level = 100
    }
    
	def transitionTime = device.currentValue("transTime")
    if(transitionTime == null) {
    	transitionTime = 3
    }
	parent.on(this, transitionTime, level)
	sendEvent(name: "switch", value: "on")
	sendEvent(name: "transTime", value: transitionTime, isStateChange: true)
}

void on(transitiontime){
	def level = device.currentValue("level")
    if(level == null) {
    	level = 100
    }
	parent.on(this, transitiontime, level)
	sendEvent(name: "switch", value: "on")
	sendEvent(name: "transTime", value: transitiontime, isStateChange: true)
}

void off() {
	def transitionTime = device.currentValue("transTime")
    if(transitionTime == null) {
    	transitionTime = 3
    }
    
	parent.off(this, transitionTime)
	sendEvent(name: "switch", value: "off")
	sendEvent(name: "transTime", value: transitionTime, isStateChange: true)
}

void off(transitiontime) {
	parent.off(this, transitiontime)
	sendEvent(name: "switch", value: "off")
	sendEvent(name: "transTime", value: transitionTime, isStateChange: true)
}

void setLevel(percent) {
	def transitionTime = device.currentValue("transTime")
    if(transitionTime == null) {
    	transitionTime = 3
    }
    
    if(device.latestValue("level") as Integer == 0) (
    	transitionTime = 0
    )
    
	log.debug "Executing 'setLevel'"
	parent.setLevel(this, percent, transitionTime)
	sendEvent(name: "level", value: percent)
	sendEvent(name: "transTime", value: transitionTime)
    sendEvent(name: "switch", value: "on", isStateChange: true)

}

void setLevel(percent, transitiontime) {
	log.debug "Executing 'setLevel'"
	parent.setLevel(this, percent, transitiontime)
	sendEvent(name: "level", value: percent)
    sendEvent(name: "transTime", value: transitionTime)
    sendEvent(name: "switch", value: "on", isStateChange: true)
}

void refresh() {
	log.debug "Executing 'refresh'"
	parent.poll()
}

def poll() {
	parent.poll()
}

def save() {
	log.debug "Executing 'save'"
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

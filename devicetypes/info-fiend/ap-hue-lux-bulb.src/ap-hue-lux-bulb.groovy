/**
 *  AP Hue Lux Bulb
 *
 *  Authors: Anthony Pastor (infofiend) and Clayton (claytonjn)
 */
 
// for the UI
metadata {
	// Automatically generated. Make future change here.
	definition (name: "AP Hue Lux Bulb", namespace: "info_fiend", author: "Anthony Pastor") {
		capability "Switch Level"
		capability "Actuator"
		capability "Switch"
		capability "Polling"
		capability "Refresh"
		capability "Sensor"

		//capability "Test Capability" //Hope to replace with Transistion Time
        
        command "refresh"
        command "setTransitionTime"
        command "log", ["string","string"]
        
        attribute "transitionTime", "NUMBER"
        
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

        standardTile("refresh", "device.switch", inactiveLabel: false, decoration: "flat", width: 2, height: 2) {
            state "default", label:"", action:"refresh.refresh", icon:"st.secondary.refresh"
        }
		
		controlTile("transitionTimeSliderControl", "device.transitionTime", "slider", inactiveLabel: false,  width: 5, height: 1, range:"(0..4)") { 
        	state "setTransitionTime", action:"setTransitionTime", backgroundColor:"#d04e00"
		}
		valueTile("transTime", "device.transitionTime", inactiveLabel: false, decoration: "flat", width: 1, height: 1) {
			state "transitionTime", label: 'Transition    Time: ${currentValue}'
        }

        main(["rich-control"])
        details(["rich-control", "transitionTimeSliderControl", "transTime", "refresh"])
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
void setTransitionTime(transitionTime) {
	log.debug "Executing 'setTransitionTime': transition time is now ${transitionTime}."
	sendEvent(name: "transitionTime", value: transitionTime, isStateChange: true)
}

void on(transitionTime = device.currentValue("transitionTime")) {
	if(transitionTime == null) { transitionTime = parent.getSelectedTransition() }

	def level = device.currentValue("level")
    if(level == null) { level = 100 }
    
	parent.on(this, transitionTime, level, deviceType)
	sendEvent(name: "switch", value: "on", isStateChange: true)
	sendEvent(name: "transitionTime", value: transitionTime, isStateChange: true)
}

void off(transitionTime = device.currentValue("transitionTime")) {
    if(transitionTime == null) { transitionTime = parent.getSelectedTransition() }
    
	parent.off(this, transitionTime, deviceType)
	sendEvent(name: "switch", value: "off", isStateChange: true)
	sendEvent(name: "transitionTime", value: transitionTime, isStateChange: true)
}

void nextLevel(transitionTime = device.currentValue("transitionTime")) {
	if(transitionTime == null) { transitionTime = parent.getSelectedTransition() }
    
    def level = device.latestValue("level") as Integer ?: 0
	if (level < 100) { level = Math.min(25 * (Math.round(level / 25) + 1), 100) as Integer }
	else { level = 25 }
	setLevel(level, transitionTime)
}

void setLevel(percent, transitionTime = device.currentValue("transitionTime")) {
    if(transitionTime == null) { transitionTime = parent.getSelectedTransition() }
       
	log.debug "Executing 'setLevel'"
	if (percent != null && percent >= 0 && percent <= 100) {
		parent.setLevel(this, percent, transitionTime, deviceType)
		sendEvent(name: "switch", "on", isStateChange: true)
		sendEvent(name: "level", value: percent, descriptionText: "Level has changed to ${percent}%", isStateChange: true)
		sendEvent(name: "transitionTime", value: transitionTime, isStateChange: true)
	} else {
		log.warn "$percent is not 0-100"
	}
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

def getDeviceType() { return "lights" }
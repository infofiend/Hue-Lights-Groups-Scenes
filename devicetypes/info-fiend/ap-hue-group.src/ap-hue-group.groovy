/**
 *  AP Hue Group
 *
 *	Version 1.3: Added Color Temp slider & valueTile
 *				 Added Transition Time slider & valueTile	
 *
 *  Authors: Anthony Pastor (infofiend) and Clayton (claytonjn)
 */
 
// for the UI
metadata {
	// Automatically generated. Make future change here.
	definition (name: "AP Hue Group", namespace: "info_fiend", author: "Anthony Pastor") {
		capability "Switch Level"
		capability "Actuator"
		capability "Color Control"
        capability "Color Temperature"
		capability "Switch"
		capability "Polling"
		capability "Refresh"
		capability "Sensor"

		command "setAdjustedColor"
		command "reset"        
        command "refresh"
		command "setColorTemperature"
        command "setTransitionTime"
        command "colorloopOn"
        command "colorloopOff"
		command "getGroupID"
		command "log", ["string","string"]        
        
		attribute "transitionTime", "NUMBER"
        attribute "colorTemperature", "NUMBER"
		attribute "groupID", "STRING"
		attribute "effect", "enum", ["none", "colorloop"]
        attribute "deviceType", "STRING"
	}

	simulator {
		// TODO: define status and reply messages here
	}

	tiles (scale: 2){
		multiAttributeTile(name:"rich-control", type: "lighting", width: 6, height: 4, canChangeIcon: true){
			tileAttribute ("device.switch", key: "PRIMARY_CONTROL") {
				attributeState "on", label:'${name}', action:"switch.off", icon:"st.lights.philips.hue-multi", backgroundColor:"#00A0DC", nextState:"turningOff"
				attributeState "off", label:'${name}', action:"switch.on", icon:"st.lights.philips.hue-multi", backgroundColor:"#C6C7CC", nextState:"turningOn"
				attributeState "turningOn", label:'${name}', action:"switch.off", icon:"st.lights.philips.hue-multi", backgroundColor:"#00A0DC", nextState:"turningOff"
				attributeState "turningOff", label:'${name}', action:"switch.on", icon:"st.lights.philips.hue-multi", backgroundColor:"#C6C7CC", nextState:"turningOn"
			}
			tileAttribute ("device.level", key: "SLIDER_CONTROL") {
				attributeState "level", action:"switch level.setLevel", range:"(0..100)"
            }
            tileAttribute ("device.level", key: "SECONDARY_CONTROL") {
	            attributeState "level", label: 'Level ${currentValue}%'
			}
			tileAttribute ("device.color", key: "COLOR_CONTROL") {
				attributeState "color", action:"setAdjustedColor"
			}
		}

        controlTile("colorTempSliderControl", "device.colorTemperature", "slider", width: 5, height: 1, inactiveLabel: false, range:"(2000..6500)") {
            state "colorTemperature", action:"color temperature.setColorTemperature"
        }
        valueTile("colorTemp", "device.colorTemperature", inactiveLabel: false, decoration: "flat", width: 1, height: 1) {
            state "colorTemperature", label: '${currentValue} K'
        }

		standardTile("reset", "device.reset", height: 2, width: 2, inactiveLabel: false, decoration: "flat") {
			state "default", label:"Reset Color", action:"reset", icon:"st.lights.philips.hue-multi"
		}
		standardTile("refresh", "device.refresh", height: 2, width: 2, inactiveLabel: false, decoration: "flat") {
			state "default", label:"", action:"refresh.refresh", icon:"st.secondary.refresh"
		}
		
        controlTile("transitionTimeSliderControl", "device.transitionTime", "slider", inactiveLabel: false,  width: 5, height: 1, range:"(0..4)") { 
        	state "setTransitionTime", action:"setTransitionTime", backgroundColor:"#d04e00"
		}
		valueTile("transTime", "device.transitionTime", inactiveLabel: false, decoration: "flat", width: 1, height: 1) {
			state "transitionTime", label: 'Transition    Time: ${currentValue}'
        }

		valueTile("groupID", "device.groupID", inactiveLabel: false, decoration: "flat") {
			state "groupID", label: 'groupID ${currentValue}   '
		}
		standardTile("getGroupID", "device.getGroupID", inactiveLabel: false, decoration: "flat", defaultState: "Ready") {
       		state "Normal", label: 'Get groupID', action:"switch groupID.getGroupID", backgroundColor:"#BDE5F2", nextState: "Retrieving"
	    	state "Retrieving", label: 'Retrieving', backgroundColor: "#ffffff", nextState: "Normal"
    	}
		
		standardTile("toggleColorloop", "device.effect", height: 2, width: 2, inactiveLabel: false, decoration: "flat") {
			state "colorloop", label:"On", action:"colorloopOff", nextState: "updating", icon:"https://raw.githubusercontent.com/infofiend/Hue-Lights-Groups-Scenes/master/smartapp-icons/hue/png/colorloop-on.png"
            state "none", label:"Off", action:"colorloopOn", nextState: "updating", icon:"https://raw.githubusercontent.com/infofiend/Hue-Lights-Groups-Scenes/master/smartapp-icons/hue/png/colorloop-off.png"
            state "updating", label:"Working", icon: "st.secondary.secondary"
		}
	}
	
	main(["rich-control"])
	details(["rich-control", "colorTempSliderControl", "colorTemp", "transitionTimeSliderControl", "transTime", "toggleColorloop", "refresh", "reset", "groupID", "getGroupID"])

}

// parse events into attributes
def parse(description) {
	log.debug "parse() - $description"
	def results = []

	def map = description
	if (description instanceof String)  {
		log.debug "Hue Group stringToMap - ${map}"
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
	if(transitionTime == null) { transitionTime = parent.getSelectedTransition() ?: 3 }
    
    def level = device.currentValue("level")
    if(level == null) { level = 100 }
	
	parent.on(this, transitionTime, level, deviceType)
	sendEvent(name: "switch", value: "on")
	sendEvent(name: "transitionTime", value: transitionTime, isStateChange: true)
}

void off(transitionTime = device.currentValue("transitionTime")) {
    if(transitionTime == null) { transitionTime = parent.getSelectedTransition() ?: 3} 
	
    parent.off(this, transitionTime, deviceType)
	sendEvent(name: "switch", value: "off")
    sendEvent(name: "effect", value: "none")
	sendEvent(name: "transitionTime", value: transitionTime, isStateChange: true)
}

void nextLevel(transitionTime = device.currentValue("transitionTime")) {
	if(transitionTime == null) { transitionTime = parent.getSelectedTransition() ?: 3}
    
    def level = device.latestValue("level") as Integer ?: 0
	if (level < 100) { level = Math.min(25 * (Math.round(level / 25) + 1), 100) as Integer }
	else { level = 25 }
	setLevel(level, transitionTime)
}

void setLevel(percent, transitionTime = device.currentValue("transitionTime")) {
    if(transitionTime == null) { transitionTime = parent.getSelectedTransition() ?: 3 }
	
    log.debug "Executing 'setLevel'"
	parent.setLevel(this, percent, transitionTime, deviceType)
	sendEvent(name: "switch", value: "on")
    sendEvent(name: "level", value: percent, descriptionText: "Level has changed to ${percent}%")
	sendEvent(name: "transitionTime", value: transitionTime, isStateChange: true)
}

void setSaturation(percent, transitionTime = device.currentValue("transitionTime")) {
    if(transitionTime == null) { transitionTime = parent.getSelectedTransition() ?: 3 }
	
    log.debug "Executing 'setSaturation'"
	parent.setSaturation(this, percent, transitionTime, deviceType)
	sendEvent(name: "saturation", value: percent, displayed: false)
	sendEvent(name: "transitionTime", value: transitionTime, isStateChange: true)
}

void setHue(percent, transitionTime = device.currentValue("transitionTime")) {
    if(transitionTime == null) { transitionTime = parent.getSelectedTransition() ?: 3 }
	
    log.debug "Executing 'setHue'"
	parent.setHue(this, percent, transitionTime, deviceType)
	sendEvent(name: "hue", value: percent, displayed: false)
	sendEvent(name: "transitionTime", value: transitionTime, isStateChange: true)
}

void setColor(value) {
	log.debug "setColor: ${value}, $this"
	def isOff = false
	
	if(value.transitiontime) { sendEvent(name: "transitionTime", value: value.transitiontime) }
	else {
		def transitionTime = device.currentValue("transitionTime")
	    if(transitionTime == null) { transitionTime = parent.getSelectedTransition() ?: 3 }
		value << [transitionTime: transitionTime]
	}
	if (value.hex) { sendEvent(name: "color", value: value.hex, isStateChange: true) } 
	if (value.hue) { sendEvent(name: "hue", value: value.hue, displayed: false) }
	if (value.saturation) { sendEvent(name: "saturation", value: value.saturation, displayed: false) }
	if (value.level) { sendEvent(name: "level", value: value.level, descriptionText: "Level has changed to ${value.level}%") }
	else {
    	// sendEvent(name: "level", value: 1)
        value.level = 1
        value.transitionTime = 0
        isOff = true
    }
	
	sendEvent(name: "switch", value: "on", isStateChange: true)
	
	parent.setColor(this, value, deviceType)
    if (isOff) { parent.off(this, 0, deviceType) }
}

void reset() {
	log.debug "Executing 'reset'"
    setColorTemperature(2710)
	parent.poll()
}

void setAdjustedColor(value) {
	if (value) {
        log.trace "setAdjustedColor: ${value}"
        def adjusted = value + [:]
        adjusted.hue = adjustOutgoingHue(value.hue)
        // Needed because color picker always sends 100
        adjusted.level = device.currentValue("level") // null 
        setColor(adjusted)
    }
}

void setColorTemperature(value, transitionTime = device.currentValue("transitionTime")) {
    if(transitionTime == null) { transitionTime = parent.getSelectedTransition() ?: 3 }
	
	if (value) {
        log.trace "setColorTemperature: ${value}k"
        parent.setColorTemperature(this, value, transitionTime, deviceType)
        sendEvent(name: "colorTemperature", value: value)
		sendEvent(name: "transitionTime", value: transitionTime, isStateChange: true)
	}
}

void refresh() {
	log.debug "Executing 'refresh'"
	parent.poll()
}

def adjustOutgoingHue(percent) {
	def adjusted = percent
	if (percent > 31) {
		if (percent < 63.0) {
			adjusted = percent + (7 * (percent -30 ) / 32)
		}
		else if (percent < 73.0) {
			adjusted = 69 + (5 * (percent - 62) / 10)
		}
		else {
			adjusted = percent + (2 * (100 - percent) / 28)
		}
	}
	log.info "adjustOutgoingHue: $percent, adjusted: $adjusted"
	adjusted
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

def getDeviceType() { return "groups" }

void getGroupID() {
    log.debug "(this) means ${this} "
    
	def groupIDfromP = parent.getId(this)
    log.debug "Retrieved groupID: ${groupIDfromP}."
   
    sendEvent(name: "groupID", value: "${groupIDfromP}", isStateChange: true)
}

void poll() {
	parent.poll()
}

void colorloopOn() {   
    log.debug "Executing 'colorloopOn'"
    def dState = device.latestValue("switch") as String ?: "off"

    if (dState == "off") { 
        def level = device.currentValue("level")
	    if(level == null) { level = 100 }

		parent.on(this, 0, level, deviceType)
	}
	parent.setEffect(this, "colorloop", deviceType)
    
    sendEvent(name: "switch", value: "on")
    sendEvent(name: "effect", value: "colorloop", isStateChange: true)
}

void colorloopOff() {
    log.debug "Executing 'colorloopOff'"
    parent.setEffect(this, "none", deviceType)
    sendEvent(name: "effect", value: "none", isStateChange: true)
}
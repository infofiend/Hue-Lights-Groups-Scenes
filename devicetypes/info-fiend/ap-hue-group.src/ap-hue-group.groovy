/**
 *  AP Hue Group
 *
 *	Version 1.3: Added Color Temp slider & valueTile
 *				 Added Transition Time slider & valueTile	
 *
 *  Author: Anthony Pastor
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
		capability "Test Capability" //Hope to replace with Transistion Time

		command "setAdjustedColor"
        command "setTT"
        command "getGroupID"
        command "setColorTemperature"
		command "log", ["string","string"]        
        
		attribute "groupID", "STRING"
        attribute "transTime", "NUMBER"
        attribute "colorTemp", "NUMBER"
	}

	simulator {
		// TODO: define status and reply messages here
	}

	tiles (scale: 2){
		multiAttributeTile(name:"switch", type: "lighting", width: 6, height: 4, canChangeIcon: true){
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
			tileAttribute ("device.color", key: "COLOR_CONTROL") {
				attributeState "color", action:"setAdjustedColor"
			}
		}

		standardTile("switch", "device.switch", width: 2, height: 2, canChangeIcon: true) {
			state "on", label:'${name}', action:"switch.off", icon:"st.lights.philips.hue-single", backgroundColor:"#79b821", nextState:"turningOff"
			state "off", label:'${name}', action:"switch.on", icon:"st.lights.philips.hue-single", backgroundColor:"#ffffff", nextState:"turningOn"
			state "turningOn", label:'${name}', action:"switch.off", icon:"st.lights.philips.hue-single", backgroundColor:"#79b821", nextState:"turningOff"
			state "turningOff", label:'${name}', action:"switch.on", icon:"st.lights.philips.hue-single", backgroundColor:"#ffffff", nextState:"turningOn"
		}

	        controlTile("colorTempSliderControl", "device.colorTemperature", "slider", width: 4, height: 2, inactiveLabel: false, range:"(2000..6500)") {
	            state "colorTemperature", action:"color temperature.setColorTemperature"
	        }
	        valueTile("colorTemp", "device.colorTemperature", inactiveLabel: false, decoration: "flat", width: 2, height: 2) {
	            state "colorTemperature", label: '${currentValue} K'
	        }
		standardTile("reset", "device.reset", height: 2, width: 2, inactiveLabel: false, decoration: "flat") {
			state "default", label:"Reset Color", action:"reset", icon:"st.lights.philips.hue-single"
		}
		standardTile("refresh", "device.switch", height: 2, width: 2, inactiveLabel: false, decoration: "flat") {
			state "default", label:"", action:"refresh.refresh", icon:"st.secondary.refresh"
		}
		controlTile("levelSliderControl", "device.level", "slider", height: 1, width: 2, inactiveLabel: false, range:"(0..100)") {
			state "level", action:"switch level.setLevel"
		}
		valueTile("level", "device.level", inactiveLabel: false, decoration: "flat") {
			state "level", label: 'Level ${currentValue}%'
		}
		controlTile("saturationSliderControl", "device.saturation", "slider", height: 1, width: 2, inactiveLabel: false) {
			state "saturation", action:"color control.setSaturation"
		}
		valueTile("saturation", "device.saturation", inactiveLabel: false, decoration: "flat") {
			state "saturation", label: 'Sat ${currentValue}    '
		}
		controlTile("hueSliderControl", "device.hue", "slider", height: 1, width: 2, inactiveLabel: false) {
			state "hue", action:"color control.setHue"
		}
		valueTile("hue", "device.hue", inactiveLabel: false, decoration: "flat") {
			state "hue", label: 'Hue ${currentValue}   '
		}
		controlTile("transitiontime", "device.transTime", "slider", inactiveLabel: false,  width: 5, height: 1, range:"(0..4)") { 
        		state "setTT", action:"setTT", backgroundColor:"#d04e00"
		}
		valueTile("valueTT", "device.transTime", inactiveLabel: false, decoration: "flat", width: 1, height: 1) {
			state "transTime", label: 'Transition    Time: ${currentValue}'
        	}

		valueTile("groupID", "device.groupID", inactiveLabel: false, decoration: "flat") {
			state "groupID", label: 'groupID ${currentValue}   '
		}
		standardTile("getGroupID", "device.getGroupID", inactiveLabel: false, decoration: "flat", defaultState: "Ready") {
       			state "Normal", label: 'Get groupID', action:"switch groupID.getGroupID", backgroundColor:"#BDE5F2", nextState: "Retrieving"
	    		state "Retrieving", label: 'Retrieving', backgroundColor: "#ffffff", nextState: "Normal"
    		}

	}
	main(["switch"])
	details(["rich-control", "levelSliderControl", "rgbSelector", "colorTempSliderControl", "colorTemp", "transitiontime", "valueTT", "refresh", "reset", "groupID", "getGroupID"])

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
void setTT(transitiontime) {

	log.debug "Executing 'setTT': transition time is now ${transitiontime}."
	sendEvent(name: "transTime", value: transitiontime, isStateChange: true)
    
}

void on() 
{
	def level = device.currentValue("level")
    if(level == null)
    {
    	level = 100
    }
	
    def transitionTime = device.currentValue("transTime")
    if(transitionTime == null)
    {
    	transitionTime = 3
    }
	parent.groupOn(this, transitionTime, level)
	sendEvent(name: "switch", value: "on")
	sendEvent(name: "transTime", value: transitionTime, isStateChange: true)
}

void on(transitiontime)
{
	def level = device.currentValue("level")
    if(level == null)
    {
    	level = 100
    }
	parent.groupOn(this, transitiontime, level)
	sendEvent(name: "switch", value: "on")
	sendEvent(name: "transTime", value: transitiontime, isStateChange: true)
}

void off() 
{
	def transitionTime = device.currentValue("transTime")
    if(transitionTime == null)
    {
    	transitionTime = 3
    }
	parent.groupOff(this, transitionTime)
	sendEvent(name: "switch", value: "off")
	sendEvent(name: "transTime", value: transitionTime, isStateChange: true)
}

void off(transitiontime)
{
	parent.groupOff(this, transitiontime)
	sendEvent(name: "switch", value: "off")
	sendEvent(name: "transTime", value: transitiontime, isStateChange: true)
}

void nextLevel() {
	def level = device.latestValue("level") as Integer ?: 0
	if (level < 100) {
		level = Math.min(25 * (Math.round(level / 25) + 1), 100) as Integer
	}
	else {
		level = 25
	}
	setLevel(level)
}

void setLevel(percent) 
{
	def transitionTime = device.currentValue("transTime")
    if(transitionTime == null)
    {
    	transitionTime = 3
    }
	log.debug "Executing 'setLevel'"
	parent.setGroupLevel(this, percent, transitionTime)
	sendEvent(name: "level", value: percent)
	sendEvent(name: "transTime", value: transitionTime, isStateChange: true)

}
void setLevel(percent, transitiontime) 
{
	log.debug "Executing 'setLevel'"
	parent.setGroupLevel(this, percent, transitiontime)
	sendEvent(name: "level", value: percent)
	sendEvent(name: "transTime", value: transitiontime, isStateChange: true)
}

void setSaturation(percent) 
{
	def transitionTime = device.currentValue("transTime")
    if(transitionTime == null)
    {
    	transitionTime = 3
    }
	log.debug "Executing 'setSaturation'"
	parent.setGroupSaturation(this, percent, transitionTime)
	sendEvent(name: "saturation", value: percent)
	sendEvent(name: "transTime", value: transitionTime, isStateChange: true)
}

void setSaturation(percent, transitiontime) 
{
	log.debug "Executing 'setSaturation'"
	parent.setGroupSaturation(this, percent, transitiontime)
	sendEvent(name: "saturation", value: percent)
	sendEvent(name: "transTime", value: transitiontime, isStateChange: true)
}

void setHue(percent) 
{
	def transitionTime = device.currentValue("transTime")
    if(transitionTime == null)
    {
    	transitionTime = 3
    }
	log.debug "Executing 'setHue'"
	parent.setGroupHue(this, percent, transitionTime)
	sendEvent(name: "hue", value: percent)
	sendEvent(name: "transTime", value: transitionTime, isStateChange: true)
}

void setHue(percent, transitiontime) 
{
	log.debug "Executing 'setHue'"
	parent.setGroupHue(this, percent, transitiontime)
	sendEvent(name: "hue", value: percent)
	sendEvent(name: "transTime", value: transitiontime, isStateChange: true)
}

void setColor(value) {
	// log.debug "setColor: ${value}"

	
	if(value.transitiontime)
	{
		sendEvent(name: "transTime", value: value.transitiontime, isStateChange: true)
	}
	else
	{
		def transitionTime = device.currentValue("transTime")
	    if(transitionTime == null)
    	{
    		transitionTime = 3
    	}
		value << [transitiontime: transitionTime]
	}
	if (value.hex) 
	{
		sendEvent(name: "color", value: value.hex, isStateChange: true)
        
	} 
	else if (value.hue && value.saturation) 
	{
		def hex = colorUtil.hslToHex(value.hue, value.saturation)
		sendEvent(name: "color", value: hex, isStateChange: true)
	}
    if (value.hue && value.saturation) 
	{
		sendEvent(name: "saturation", value:  value.saturation)
        sendEvent(name: "hue", value:  value.hue, isStateChange: true)
	}
	if (value.level) 
	{
		sendEvent(name: "level", value: value.level, isStateChange: true)
	}
	if (value.switch) 
	
	sendEvent(name: "switch", value: value.switch, isStateChange: true)
	parent.setGroupColor(this, value)
}

void reset() {
	log.debug "Executing 'reset'"
    def value = [level:100, hex:"#90C638", saturation:56, hue:23]
    setAdjustedColor(value)
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

void setColorTemperature(colorTkelvin) {
    if(colorTkelvin == null)
    {
    	colorTkelvin = 2400
    }
    
    def transitionTime = device.currentValue("transTime")
    if(transitionTime == null) {
    	transitionTime = 3
    }
    
    def colorTmireks = kelvinToMireks(colorTkelvin)
    
	log.debug "Executing 'setColorTemperature'"
	parent.setGroupCT(this, colorTmireks, transitionTime)
	sendEvent(name: "colorTemp", value: colorTkelvin, isStateChange: true)
    sendEvent(name: "switch", value: "on", isStateChange: true)

}

void setColorTemperature(colorTkelvin, transitiontime) {
    if(colorTkelvin == null)
    {
    	colorTkelvin = 2400
    }
        
    def colorTmireks = kelvinToMireks(colorTkelvin)
    
	log.debug "Executing 'setColorTemperature'"
	parent.setGroupCT(this, colorTmireks, transitiontime)
	sendEvent(name: "colorTemp", value: colorTkelvin, isStateChange: true)
    sendEvent(name: "switch", value: "on", isStateChange: true)

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


void getGroupID() {
    log.debug "(this) means ${this} "
    
	def groupIDfromP = parent.getId(this)
    log.debug "Retrieved groupID: ${groupIDfromP}."
   
    sendEvent(name: "groupID", value: "${groupIDfromP}", isStateChange: true)

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

void poll() {
	parent.poll()
}

int kelvinToMireks(kelvin) {
	return 1000000 / kelvin //https://en.wikipedia.org/wiki/Mired
}

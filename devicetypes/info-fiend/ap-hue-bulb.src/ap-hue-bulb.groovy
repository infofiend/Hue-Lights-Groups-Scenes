/**
 *  AP Hue Bulb
 *
 *  Author: Anthony Pastor
 */
// for the UI
metadata {
	// Automatically generated. Make future change here.
	definition (name: "AP Hue Bulb", namespace: "info_fiend", author: "Anthony Pastor") {
		capability "Switch Level"
		capability "Actuator"
		capability "Color Control"
		capability "Switch"
		capability "Polling"
		capability "Refresh"
		capability "Sensor"
    //    capability "transitiontime" //Hope to replace with Transistion Time

		command "setAdjustedColor"
        command "reset"        
        command "refresh"     
        
        attribute "transitiontime", "number"
        
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
				attributeState "level", action:"switch level.setLevel"
			}
			tileAttribute ("device.color", key: "COLOR_CONTROL") {
				attributeState "color", action:"setAdjustedColor"
			}
		}

		standardTile("reset", "device.reset", inactiveLabel: false, decoration: "flat", width: 2, height: 2) {
			state "default", label:"Reset Color", action:"reset", icon:"st.lights.philips.hue-single"
		}
		standardTile("refresh", "device.switch", inactiveLabel: false, decoration: "flat", width: 2, height: 2) {
			state "default", label:"", action:"refresh.refresh", icon:"st.secondary.refresh"
		}
        valueTile("transitiontime", "device.transitiontime", inactiveLabel: false, decoration: "flat", width: 2, height: 2) {
			state "transitiontime", label: 'Transition    Time: ${currentValue}'
		}

	}

	main(["switch"])
	details(["switch", "levelSliderControl", "rgbSelector", "transitiontime", "refresh", "reset"])
}



// parse events into attributes
def parse(description) {
	log.debug "parse() - $description"
	def results = []

	def map = description
	if (description instanceof String)  {
		log.debug "Hue Bulb stringToMap - ${map}"
		map = stringToMap(description)
	}

	if (map?.name && map?.value) {
		results << createEvent(name: "${map?.name}", value: "${map?.value}")
	}

	results

}

// handle commands
void on() 
{
	def level = device.currentValue("level")
    if(level == null)
    {
    	level = 100
    }

	def transitionTime = device.currentValue("transitiontime")
    if(transitionTime == null)
    {
    	transitionTime = 3
    }
    
//    log.debug this ": level = ${level} & tt= ${transitionTime}"
    
	parent.on(this, transitionTime, level)
	sendEvent(name: "switch", value: "on")
	sendEvent(name: "transitiontime", value: transitionTime, isStateChange: true)
}

void on(transitiontime)
{
	def level = device.currentValue("level")
    if(level == null)
    {
    	level = 100
    }
	parent.on(this, transitiontime, level)
	sendEvent(name: "switch", value: "on")
	sendEvent(name: "transitiontime", value: transitiontime, isStateChange: true)
}

void off() 
{
	def transitionTime = device.currentValue("transitiontime")
    if(transitionTime == null)
    {
    	transitionTime = 3
    }
    
 //   log.debug this ": off & tt = ${transitionTime}"
    
	parent.off(this, transitionTime)
	sendEvent(name: "switch", value: "off")
	sendEvent(name: "transitiontime", value: transitionTime, isStateChange: true)
}

void off(transitiontime)
{
	parent.off(this, transitiontime)
	sendEvent(name: "switch", value: "off")
	sendEvent(name: "transitiontime", value: transitiontime, isStateChange: true)
}

void reset() {
	log.debug "Executing 'reset'"
    def value = [level:100, hex:"#90C638", saturation:56, hue:23]
    setAdjustedColor(value)
	parent.poll()
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

void setLevel(percent) {
	def transitionTime = device.currentValue("transitiontime")
    if(transitionTime == null)
    {
    	transitionTime = 3
    }
        
	if(device.latestValue("level") as Integer == 0)
    (
    	transitionTime = 0
    )
	
	log.debug "Executing 'setLevel'"
	parent.setLevel(this, percent, transitionTime)
	sendEvent(name: "level", value: percent)
	sendEvent(name: "transitiontime", value: transitionTime)
    sendEvent(name: "switch", value: "on", isStateChange: true)

}

void setLevel(percent, transitiontime) {
	
	log.debug "Executing 'setLevel'"
	parent.setLevel(this, percent, transitiontime)
	sendEvent(name: "level", value: percent)
	sendEvent(name: "transitiontime", value: transitiontime)
    sendEvent(name: "switch", value: "on", isStateChange: true)

}

void setSaturation(percent) 
{
	def transitionTime = device.currentValue("transitiontime")
    if(transitionTime == null)
    {
    	transitionTime = 3
    }
    
 //   log.debug this ": tt= ${transitionTime}"
	log.debug "Executing 'setSaturation'"
	parent.setSaturation(this, percent, transitionTime)
	sendEvent(name: "saturation", value: percent)
	sendEvent(name: "transitiontime", value: transitionTime, isStateChange: true)
}

void setSaturation(percent, transitiontime) 
{
	log.debug "Executing 'setSaturation'"
	parent.setSaturation(this, percent, transitiontime)
	sendEvent(name: "saturation", value: percent)
	sendEvent(name: "transitiontime", value: transitiontime, isStateChange: true)
}

void setHue(percent) 
{
	def transitionTime = device.currentValue("transitiontime")
    if(transitionTime == null)
    {
    	transitionTime = 3
    }
    
//    log.debug this ": tt= ${transitionTime}"
    
	log.debug "Executing 'setHue'"
	parent.setHue(this, percent, transitionTime)
	sendEvent(name: "hue", value: percent)
	sendEvent(name: "transitiontime", value: transitionTime, isStateChange: true)
}

void setHue(percent, transitiontime) 
{
	log.debug "Executing 'setHue'"
	parent.setHue(this, percent, transitiontime)
	sendEvent(name: "hue", value: percent)
	sendEvent(name: "transitiontime", value: transitiontime, isStateChange: true)
}

void setColor(value) {
	log.debug "setColor: ${value}"
    def isOff = false

	// TODO: convert hue and saturation to hex and just send a color event
	if(value.transitiontime)
	{
		sendEvent(name: "transitiontime", value: value.transitiontime)
	}
	else
	{
    	def transitionTime = device.currentValue("transitiontime")
	    if(transitionTime == null)
    	{
    		transitionTime = 3
	    }
    
//    	log.debug this ": tt= ${transitionTime}"
		sendEvent(name: "transitiontime", value: transitionTime, isStateChange: true)
		value << [transitiontime: transitionTime]
	}
	if (value.hex) 
	{
		sendEvent(name: "color", value: value.hex, isStateChange: true)
	} 
	else if (value.hue && value.saturation) 
	{
		def hex = colorUtil.hslToHex(value.hue, value.saturation, isStateChange: true)
		sendEvent(name: "color", value: hex)
	}

	if (value.level) 
	{
		sendEvent(name: "level", value: value.level, isStateChange: true)
	}
    else
    {
    	// sendEvent(name: "level", value: 1)
        value.level = 1
        value.transitiontime = 0
        isOff = true
    }
	if (value.switch) 
	{
		sendEvent(name: "switch", value: value.switch, isStateChange: true)
	}

	parent.setColor(this, value)
    if (isOff) 
    {
    	parent.off(this, 0)
    }
    
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
	log.info "percent: $percent, adjusted: $adjusted"
	adjusted
}

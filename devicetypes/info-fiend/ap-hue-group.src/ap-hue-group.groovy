/**
 *  Hue Group
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
		capability "Switch"
		capability "Polling"
		capability "Refresh"
		capability "Sensor"
		capability "Test Capability" //Hope to replace with Transistion Time

		command "setAdjustedColor"

		attribute "groupID", "string"
	}

	simulator {
		// TODO: define status and reply messages here
	}

	standardTile("switch", "device.switch", width: 1, height: 1, canChangeIcon: true) {
		state "on", label:'${name}', action:"switch.off", icon:"st.lights.philips.hue-multi", backgroundColor:"#79b821" 
		state "off", label:'${name}', action:"switch.on", icon:"st.lights.philips.hue-multi", backgroundColor:"#ffffff"
	}
	standardTile("refresh", "device.switch", inactiveLabel: false, decoration: "flat") {
		state "default", label:"", action:"refresh.refresh", icon:"st.secondary.refresh"
	}
	controlTile("rgbSelector", "device.color", "color", height: 3, width: 3, inactiveLabel: false) {
		state "color", action:"setAdjustedColor"
	}
	controlTile("levelSliderControl", "device.level", "slider", height: 1, width: 2, inactiveLabel: false) {
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
	valueTile("transitiontime", "device.transitiontime", inactiveLabel: false, decoration: "flat") {
		state "transitiontime", label: 'Transitiontime ${currentValue}   '
	}
    valueTile("color", "device.color", inactiveLabel: false, decoration: "flat") {
		state "color", label: 'color ${currentValue}   '
	}
	valueTile("groupID", "device.groupID", inactiveLabel: false, decoration: "flat") {
		state "groupID", label: 'groupID ${currentValue}   '
	}


	main(["switch"])
	details(["switch", "levelSliderControl", "rgbSelector", "refresh","transitiontime","color","saturation","groupID","hue"])

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
def on() 
{
	def level = device.currentValue("level")
    if(level == null)
    {
    	level = 100
    }
	def transitiontime = 4
	parent.groupOn(this, 4, level)
	sendEvent(name: "switch", value: "on")
	sendEvent(name: "transitiontime", value: transitiontime)
}

def on(transitiontime)
{
	def level = device.currentValue("level")
    if(level == null)
    {
    	level = 100
    }
	parent.groupOn(this, transitiontime, level)
	sendEvent(name: "switch", value: "off")
	sendEvent(name: "transitiontime", value: transitiontime)
}

def off() 
{
	def transitiontime = 4
	parent.groupOff(this, transitiontime)
	sendEvent(name: "switch", value: "off")
	sendEvent(name: "transitiontime", value: transitiontime)
}

def off(transitiontime)
{
	parent.groupOff(this, transitiontime)
	sendEvent(name: "switch", value: "off")
	sendEvent(name: "transitiontime", value: transitiontime)
}

def poll() {
	parent.poll()
}

def nextLevel() {
	def level = device.latestValue("level") as Integer ?: 0
	if (level < 100) {
		level = Math.min(25 * (Math.round(level / 25) + 1), 100) as Integer
	}
	else {
		level = 25
	}
	setLevel(level)
}

def setLevel(percent) 
{
	def transitiontime = 4
	log.debug "Executing 'setLevel'"
	parent.setGroupLevel(this, percent, transitiontime)
	sendEvent(name: "level", value: percent)
	sendEvent(name: "transitiontime", value: transitiontime)

}
def setLevel(percent, transitiontime) 
{
	log.debug "Executing 'setLevel'"
	parent.setGroupLevel(this, percent, transitiontime)
	sendEvent(name: "level", value: percent)
	sendEvent(name: "transitiontime", value: transitiontime)
}

def setSaturation(percent) 
{
	def transitiontime = 4
	log.debug "Executing 'setSaturation'"
	parent.setGroupSaturation(this, percent, transitiontime)
	sendEvent(name: "saturation", value: percent)
	sendEvent(name: "transitiontime", value: transitiontime)
}
def setSaturation(percent, transitiontime) 
{
	log.debug "Executing 'setSaturation'"
	parent.setGroupSaturation(this, percent, transitiontime)
	sendEvent(name: "saturation", value: percent)
	sendEvent(name: "transitiontime", value: transitiontime)
}

def setHue(percent) 
{
	def transitiontime = 4
	log.debug "Executing 'setHue'"
	parent.setGroupHue(this, percent, transitiontime)
	sendEvent(name: "hue", value: percent)
	sendEvent(name: "transitiontime", value: transitiontime)
}

def setHue(percent, transitiontime) 
{
	log.debug "Executing 'setHue'"
	parent.setGroupHue(this, percent, transitiontime)
	sendEvent(name: "hue", value: percent)
	sendEvent(name: "transitiontime", value: transitiontime)
}

def setColor(value) {
	log.debug "setColor: ${value}"

	
	if(value.transitiontime)
	{
		sendEvent(name: "transitiontime", value: value.transitiontime)
	}
	else
	{
		sendEvent(name: "transitiontime", value: 4)
		value << [transitiontime: 4]
	}
	if (value.hex) 
	{
		sendEvent(name: "color", value: value.hex)
        
	} 
	else if (value.hue && value.saturation) 
	{
		def hex = colorUtil.hslToHex(value.hue, value.saturation)
		sendEvent(name: "color", value: hex)
	}
    if (value.hue && value.saturation) 
	{
		sendEvent(name: "saturation", value:  value.saturation)
        sendEvent(name: "hue", value:  value.hue)
	}
	if (value.level) 
	{
		sendEvent(name: "level", value: value.level)
	}
	if (value.switch) 
	{
		sendEvent(name: "switch", value: value.switch)
	}
	parent.setGroupColor(this, value)
}

def setAdjustedColor(value) {
	log.debug "setAdjustedColor: ${value}"
	def adjusted = value + [:]
	adjusted.hue = adjustOutgoingHue(value.hue)
	adjusted.level = null // needed because color picker always sends 100
	setColor(adjusted)
}

def save() {
	log.debug "Executing 'save'"
}

def refresh() {
	log.debug "Executing 'refresh'"
	parent.poll()
    def GroupIDfromParent = parent.getGroupID()
    sendEvent(groupID: GroupIDfromParent, isStateChange: true)
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
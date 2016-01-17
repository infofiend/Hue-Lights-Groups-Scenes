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

		capability "Test Capability" //Hope to replace with Transistion Time
	}

	simulator {
		// TODO: define status and reply messages here
	}

	standardTile("switch", "device.switch", width: 2, height: 2, canChangeIcon: true) {
		state "on", label:'${name}', action:"switch.off", icon:"st.lights.philips.hue-single", backgroundColor:"#79b821"
		state "off", label:'${name}', action:"switch.on", icon:"st.lights.philips.hue-single", backgroundColor:"#ffffff"
	}
	standardTile("refresh", "device.switch", inactiveLabel: false, decoration: "flat") {
		state "default", label:"", action:"refresh.refresh", icon:"st.secondary.refresh"
	}
	controlTile("levelSliderControl", "device.level", "slider", height: 1, width: 2, inactiveLabel: false) {
		state "level", action:"switch level.setLevel"
	}
	valueTile("level", "device.level", inactiveLabel: false, decoration: "flat") {
		state "level", label: 'Level ${currentValue}%'
	}

	main(["switch"])
	details(["switch", "levelSliderControl", "refresh"])

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
def on() 
{
	def level = device.currentValue("level")
    if(level == null)
    {
    	level = 100
    }
    log.debug level
	def transitiontime = 4
	parent.on(this, 4, level)
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
	parent.on(this, transitiontime, level)
	sendEvent(name: "switch", value: "off")
	sendEvent(name: "transitiontime", value: transitiontime)
}

def off() 
{
	def transitiontime = 4
	parent.off(this, transitiontime)
	sendEvent(name: "switch", value: "off")
	sendEvent(name: "transitiontime", value: transitiontime)
}

def off(transitiontime)
{
	parent.off(this, transitiontime)
	sendEvent(name: "switch", value: "off")
	sendEvent(name: "transitiontime", value: transitiontime)
}

def poll() {
	parent.poll()
}

def setLevel(percent) 
{
	def transitiontime = 4
	log.debug "Executing 'setLevel'"
	parent.setLevel(this, percent, transitiontime)
	sendEvent(name: "level", value: percent)
	sendEvent(name: "transitiontime", value: transitiontime)

}
def setLevel(percent, transitiontime) 
{
	log.debug "Executing 'setLevel'"
	parent.setLevel(this, percent, transitiontime)
	sendEvent(name: "level", value: percent)
	sendEvent(name: "transitiontime", value: transitiontime)
}

def save() {
	log.debug "Executing 'save'"
}

def refresh() {
	log.debug "Executing 'refresh'"
	parent.poll()
}
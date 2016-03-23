/**
 *  AP Hue Lux Bulb
 *
 *	Version 1.3: Added Color Temp slider & valueTile
 *				 Added Transition Time slider & valueTile	
 *
 *  Authors: Anthony Pastor (infofiend) and Clayton (claytonnj)
 */
// for the UI


metadata {
	// Automatically generated. Make future change here.
	definition (name: "AP Hue Lux Bulb", namespace: "info_fiend", author: "Anthony Pastor") {
		capability "Switch Level"
		capability "Actuator"
        capability "Color Temperature"
		capability "Switch"
		capability "Polling"
		capability "Refresh"
		capability "Sensor"
    //    capability "transitiontime" //Hope to replace with Transistion Time

        command "refresh"  
        command "setColorTemperature"
        command "setTT"
		command "log", ["string","string"]        
        
        attribute "transTime", "NUMBER"
        attribute "colorTemperature", "NUMBER"
        
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

        controlTile("colorTempSliderControl", "device.colorTemperature", "slider", width: 4, height: 2, inactiveLabel: false, range:"(2000..6500)") {
            state "colorTemperature", action:"color temperature.setColorTemperature"
        }
        valueTile("colorTemp", "device.colorTemperature", inactiveLabel: false, decoration: "flat", width: 2, height: 2) {
            state "colorTemperature", label: '${currentValue} K'
        }

        standardTile("refresh", "device.switch", inactiveLabel: false, decoration: "flat") {
            state "default", label:"", action:"refresh.refresh", icon:"st.secondary.refresh"
        }
        
        controlTile("transitiontime", "device.transTime", "slider", inactiveLabel: false,  width: 5, height: 1, range:"(0..4)") { 
        	state "setTT", action:"setTT", backgroundColor:"#d04e00"
		}
		valueTile("valueTT", "device.transTime", inactiveLabel: false, decoration: "flat", width: 1, height: 1) {
			state "transTime", label: 'Transition    Time: ${currentValue}'
        }
        
        controlTile("colorTemperature", "device.colorTemperature", "slider", inactiveLabel: false,  width: 5, height: 1, range:"(2000..6500)") { 
        	state "setCT", action:"setColorTemperature", backgroundColor:"#54f832"
		}
		valueTile("valueCT", "device.colorTemperature", inactiveLabel: false, decoration: "flat", width: 1, height: 1) {
			state "colorTemperature", label: ' ColorTemp:  ${currentValue}'
        }

	}

	main(["switch"])
	details(["rich-control", "colorTempSliderControl", "colorTemp", "transitiontime", "valueTT", "refresh"])
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
void setTT(transitiontime) {

	log.debug "Executing 'setTT': transition time is now ${transitiontime}."
	sendEvent(name: "transTime", value: transitiontime, isStateChange: true)
    
}

void on() 
{
	def level = device.currentValue("level")
    if(level == null) {
    	level = 100
    }

	def transitionTime = device.currentValue("transTime")
    if(transitionTime == null) {
    	transitionTime = parent.getSelectedTransition()
    }
    
//    log.debug this ": level = ${level} & tt= ${transitionTime}"
    
	parent.on(this, transitionTime, level)
	sendEvent(name: "switch", value: "on")
	sendEvent(name: "transTime", value: transitionTime, isStateChange: true)
}

void on(transitiontime)
{
	def level = device.currentValue("level")
    if(level == null) {
    	level = 100
    }
	parent.on(this, transitiontime, level)
	sendEvent(name: "switch", value: "on")
	sendEvent(name: "transTime", value: transitiontime, isStateChange: true)
}

void off() 
{
	def transitionTime = device.currentValue("transTime")
    if(transitionTime == null) {
    	transitionTime = parent.getSelectedTransition()
    }
    
 //   log.debug this ": off & tt = ${transitionTime}"
    
	parent.off(this, transitionTime)
	sendEvent(name: "switch", value: "off")
	sendEvent(name: "transTime", value: transitionTime, isStateChange: true)
}

void off(transitiontime)
{
	parent.off(this, transitiontime)
	sendEvent(name: "switch", value: "off")
	sendEvent(name: "transTime", value: transitiontime, isStateChange: true)
}

void setLevel(percent) {
	def transitionTime = device.currentValue("transTime")
    if(transitionTime == null) {
    	transitionTime = parent.getSelectedTransition()
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
	sendEvent(name: "transTime", value: transitiontime)
    sendEvent(name: "switch", value: "on", isStateChange: true)

}

void setColorTemperature(value) {
	def transitionTime = device.currentValue("transTime")
    if(transitionTime == null) {
    	transitionTime = parent.getSelectedTransition()
    }
	if (value) {
        log.trace "setColorTemperature: ${value}k"
        parent.setColorTemperature(this, value, transitionTime)
        sendEvent(name: "colorTemperature", value: value)
	}
}

void setColorTemperature(value, transitiontime) {
	if (value) {
        log.trace "setColorTemperature: ${value}k"
        parent.setColorTemperature(this, value, transitiontime)
        sendEvent(name: "colorTemperature", value: value)
	}
}

void refresh() {
	log.debug "Executing 'refresh'"
	parent.manualRefresh()
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

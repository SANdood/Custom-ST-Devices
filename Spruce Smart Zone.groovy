/**
 *  Sample code to turn a Spruce zone on and off based on moisture or time
 *	Set preferences
 *	Subscribe and unsubscribe to sensors
 *  Spruce specific commands:
 *		z1on, z1off, z2on, z2off, z3on, z3off, z4on,z4off up to 16
 *  	notify(status,message)
 */

definition(
    name: "Zone Moisture",
    namespace: "plaidsystems",
    author: "plaidsystems",
    description: "Zone controlled by moisture",
    category: "My Apps",
    iconUrl: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience.png",
    iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience%402x.png",
    iconX3Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience%402x.png",
    oauth: false)

preferences {
	section("Select switches to control...") {
		input name: "switches", type: "capability.switch", multiple: true
	}    
    section("Select zone to control, Zone 1 for single switch device..."){
		input name: "zone", title: "Zone to control?", metadata: [values: ["1","2","3","4"]], type: "enum"
	}    
    section("Select moisture sensor to read..."){
		input name: "sensor", value: "humidity", type: "capability.relativeHumidityMeasurement", multiple: false, required: false 
	}	
    section("Automatic turn on?") {
		input name: "sensorlowon", title: "Turn On when sensor is low?", type: "bool"
	}
    section("Or start at?") {
		input name: "startTime", title: "Turn On Time?", type: "time", required: false
	}
    section("Set low moisture?") {
		input "low", "number", title: "Turn On When Moisture is below?"
	}        
	section("Turn on water for how many minutes?") {
		input name: "duration", title: "Duration?", type: "number"
	}
    section("Or Automatic turn off?") {
		input name: "sensorhighoff", title: "Turn Off when sensor reaches?", type: "bool"
	}
    section("Set high moisture?") {
		input "high", "number", title: "Turn Off When Moisture is above?"
	}
       
    
}

def installed() {
	log.debug "Installed with settings: ${settings}"
	unschedule()
    unsubscribe()
    initialize()    
}

def updated(settings) {
	unschedule()
    unsubscribe()
    initialize()	             
}

def initialize(){
	log.debug startTime    
    
    if(startTime != null){		//if time is set, schedule every day
    	def runTime = timeToday(startTime, location.timeZone)
        schedule(runTime, startWater)   		        
        }
	if(sensorlowon) subscribe(sensor, "humidity", humidityHandler)	//if sensor low setpoint is on, subscribe to sensor    
}

//called whenever sensor reports value
def humidityHandler(evt){
    
    def soil = sensor.latestValue("humidity")    
    
    log.debug "Soil Moisture = $soil %"
    
    if (soil <= low && sensorlowon) startWater()
    if (soil >= high && sensorhighoff) stopWater()
    
}

//starts water
def startWater() {
	log.debug "start water"
    if (sensorhighoff) subscribe(sensor,"humidity",humidityHandler)    
    
    runIn(duration * 60, stopWater)    //sets off time
    switchOn(zone)
    switches.notify("moisture", "${app.name} turning ${zone} on")    
}

def stopWater() {
	if(!sensorlowon)unsubscribe()
    
    switchOff(zone)
    switches.notify("moisture", "${app.name} turning ${zone} off")
}

private switchOn(zone){    
    log.debug "Turning $zone on"    
    
    if(zone=="1") switches.z1on()
    else if(zone=="2") switches.z2on()    
    else if(zone=="3") switches.z3on()
    else if(zone=="4") switches.z4on()
	
}


private switchOff(zone){
	log.debug "Turning $zone off"
    
	if(zone=="1") switches.z1off()    
    else if(zone=="2") switches.z2off()    
    else if(zone=="3") switches.z3off()
    else if(zone=="4") switches.z4off()
     
}

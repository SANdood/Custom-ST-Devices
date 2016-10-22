/**
 *  SmartApp that will turn a Spruce zone on and off based on Spruce sensor moisture level and (optionally) daily
 *  Spruce specific commands:
 *		zone commands: z1on, z1off, z2on, z2off, z3on, z3off, ... 			// up to 16
 *		controller commands: programOn, programEnd 							// interactions with standard Spruce scheduler SmartApp
 * 							 notify(status,message)							// updates controller status display
 *		zone events: switch1.z1off, switch2.z2off, switch3.z3off, ...		// up to 16 - allows for manual zone shutoff
 *  	
 */
// TODO:
// Validate Pause support
// Enable/Disable
// CLean up UI
//
definition(
    name: "Spruce Smart Zone",
    namespace: "plaidsystems",
    author: "plaidsystems",
    description: "Spruce zone controlled by Spruce moisture sensor. Runs a single zone whenever soil moisture falls below set percentage.",
    category: "My Apps",
    iconUrl: "http://www.plaidsystems.com/smartthings/st_spruce_leaf_250f.png",
    iconX2Url: "http://www.plaidsystems.com/smartthings/st_spruce_leaf_250f.png",
    iconX3Url: "http://www.plaidsystems.com/smartthings/st_spruce_leaf_250f.png",
    oauth: false)

preferences {
	section("Select Spruce Controller or switch") {
		input name: "controller", type: "capability.switch", multiple: false
	}    
    section("Select zone to control, Zone 1 for single switch device..."){
		input name: "zone", title: "Zone to control?", multiple: false, metadata: [values: ['1','2','3','4','5','6','7','8','9','10','11','12','13','14','15','16']], type: "enum"
	}    
    section("Select Spruce moisture sensor"){
		input name: "sensor", value: "humidity", type: "capability.relativeHumidityMeasurement", multiple: false, required: false 
	}	
    section("Automatic turn on") {
		input name: "sensorlowon", title: "Turn On when sensor is low?", type: "bool"
	}
    section("Run daily (optional)") {
		input name: "startTime", title: "At what time?", type: "time", required: false
	}
    section("Set low moisture") {
		input "low", "number", title: "Turn On When Moisture is below?"
	}        
	section("Water for how many minutes?") {
		input name: "duration", title: "Duration?", type: "number"
	}
    section("Automatic turn off") {
		input name: "sensorhighoff", title: "Turn Off when sensor reaches?", type: "bool"
	}
    section("Set high moisture") {
		input "high", "number", title: "Turn Off When Moisture is above?"
	}

	section(''){
    	paragraph(image: 'http://www.plaidsystems.com/smartthings/st_pause.png',
        	title: 'Pause Control Contacts & Switches', required: false,
           	'Selecting contacts or control switches is optional. When a selected contact sensor is opened or closed, or a switch is ' +
            'toggled, watering immediately stops and will not resume until all of the contact sensors and ' +
            'switches are reset.\n\nCaution: if all contacts or switches are left in the stop state, the dependent ' +
            'schedule(s) will never run.')
        input(name: 'contacts', title: 'Select water delay contact sensors', type: 'capability.contactSensor', multiple: true, 
            required: false, submitOnChange: true)        
		if (contacts)
			input(name: 'contactStop', title: 'Stop watering when sensors are...', type: 'enum', required: (settings.contacts != null), 
				options: ['open', 'closed'], defaultValue: 'open')
		input(name: 'toggles', title: 'Select water delay switches', type: 'capability.switch', multiple: true, required: false, 
			submitOnChange: true)
		if (toggles) 
			input(name: 'toggleStop', title: 'Stop watering when switches are...', type: 'enum', 
				required: (settings.toggles != null), options: ['on', 'off'], defaultValue: 'off')
		input(name: 'contactDelay', type: 'number', title: 'Restart watering how many seconds after all contacts and switches ' +
				'are reset? (minimum 10s)', defaultValue: '10', required: false)
	}
	section(''){
    	paragraph(image: 'http://www.plaidsystems.com/smartthings/st_spruce_controller_250.png',
        	title: 'Controller Sync', required: false,
            'For multiple controllers only.  This schedule will wait for the selected controller to finish before ' +
            'starting. Do not set with a single controller!')
       	input(name: 'sync', type: 'capability.switch', title: 'Select Master Controller', 
			  description: 'Only use this setting with multiple controllers', required: false, multiple: false)
    }
}

def installed() {
	log.debug "Installed with settings: ${settings}"
    initialize()    
}

def updated() {
	log.debug "Updated with settings: ${settings}"
    initialize()	             
}

def initialize(){
	if (atomicState.run == null)	 atomicState.run = false
	if (atomicState.delayed == null) atomicState.delayed = false
	if (atomicState.paused == null)	 atomicState.paused = false
	
	unschedule()
    unsubscribe()
	subscribe(app, appTouch)
	
	log.debug "${app.label}: ${startTime}, ${low}, ${high}"
    
	if (low >= high) log.error "Low must be less than High"
	
    if (startTime != null) {		//if time is set, schedule every day
    	def runTime = timeToday(startTime, location.timeZone)
        schedule(runTime, startWatering)   		        
    }
	if (sensorlowon) subscribe(sensor, "humidity", humidityHandler)	//if sensor low setpoint is on, subscribe to sensor    
}

// enable the "Play" button in SmartApp list
def appTouch(evt) {
	startWatering()
}

//called whenever sensor reports value
def humidityHandler(evt){ 
    def soil = sensor.currentHumidity    
	log.debug "Soil Moisture is ${soil}% (${evt.value})"
    
    if ((soil < low) && sensorlowon) startWatering()
    else if ((soil >= high) && sensorhighoff && (atomicState.run || atomicState.delayed)) stopWatering()
}

// called to start watering cycle
def startWatering() {
    if (atomicState.run || atomicState.delayed) return	// don't start this twice
	if (sensor.currentHumidity >= high) return	// already there - don't need more water right now
	
	// Is the controller busy?
	if ((controller.currentSwitch != 'off') || (controller.currentStatus == 'pause')) {
		log.debug "watering delayed, ${controller.displayName} busy"
		atomicState.delayed = true
		subscribe(controller, "switch.off", endDelay)
		// we don't change the status so that we don't disrupt the running schedule (since we aren't in control yet)
		controller.notify(controller.currentStatus, "${app.label}: Waiting for current schedule to complete")
		return
	}
	
	// Is the sync controller busy?
	if (settings.sync) {
		if ((settings.sync.currentSwitch != 'off') || settings.sync.currentStatus == 'pause') {
			log.debug "watering sync delayed, ${sync.displayName} busy"
           	subscribe(settings.sync, 'switch.off', syncOn)
			controller.notify('delayed', "${app.label}: Waiting for ${settings.sync.displayName} to complete")
			return
		}
	}

	// looks like we are good to go
	atomicState.run = true
	
	if (isWaterPaused()) {
		// we have to check this first, in case a pause was effected while no other schedule was running
		String pauseList = getWaterPauseList()
		log.debug "watering paused, ${pauseList}"
		subWaterUnpause()
		controller.programWait()	// Make sure that the status reflects that we are waiting
		controller.notify('pause', "${app.label}: Watering paused, ${pauseList}")
		return
	}

	log.debug "watering starting"
	controller.programOn()
	if (sensorhighoff) subscribe(sensor, "humidity", humidityHandler) 
	subWaterPause()
    subscribe(controller, "switch${zone}.z${zone}off", zoneOffHandler)		// watch for zone being manually turned off
	controller."z${zone}on"()
	atomicState.startTime = now()
	atomicState.pauseSecs = 0
	runIn(duration * 60, stopWater)    //sets off time
	String s = ''
	if (duration > 1) s = 's'
	controller.notify("active", "${app.label}: Zone ${zone} turned on for ${duration} min${s}")
}

def endDelay(evt) {
	unsubscribe(controller)
	atomicState.delayed = false
	Random rand = new Random() 						// just in case there are multiple schedules waiting on the same controller
	int randomSeconds = rand.nextInt(120) + 15
    runIn(randomSeconds, startWatering)	
}
							  
def stopWatering() {
	unsubscribe()
    if (sensorlowon) subscribe(sensor, "humidity", humidityHandler)
	atomicState.delayed = false
	if (atomicState.run) {
		atomicState.run = false
		controller."z${zone}off"()
		controller.notify("finished", "${app.label}: Zone ${zone} turned off")
		controller.programEnd()
	}
}

def zoneOffHandler(evt) {
	atomicState.run = false
	unsubscribe(controller)
	unsubWaterPausers()
	controller.notify("finished", "${app.label}: Zone ${zone} was manually turned off")
	controller.programEnd()
}

// true if one of the stoppers is in Stop state
private boolean isWaterPaused() {
	if (settings.contacts && settings.contacts.currentContact.contains(settings.contactStop)) return true
	if (settings.toggles && settings.toggles.currentSwitch.contains(settings.toggleStop)) return true
	return false
}

// watch for water stoppers
private def subWaterPause() {
	if (settings.contacts) {
		unsubscribe(settings.contacts)
		subscribe(settings.contacts, "contact.${settings.contactStop}", waterPause)
	}
	if (settings.toggles) {
		unsubscribe(settings.toggles)
		subscribe(settings.toggles, "switch.${settings.toggleStop}", waterPause)
	}
}

// watch for water starters
private def subWaterUnpause() {
	if (settings.contacts) {
		unsubscribe(settings.contacts)
		def cond = (settings.contactStop == 'open') ? 'closed' : 'open'
		subscribe(settings.contacts, "contact.${cond}", waterUnpause)
	}
	if (settings.toggles) {
		unsubscribe(settings.toggles)
		def cond = (settings.toggleStop == 'on') ? 'off' : 'on'
		subscribe(settings.toggles, "switch.${cond}", waterUnpause)
	}
}

// stop watching water stoppers and starters
private def unsubWaterPausers() {
	if (settings.contacts) 	unsubscribe(settings.contacts)
	if (settings.toggles) 	unsubscribe(settings.toggles)
}

// which of the stoppers are in stop mode?
private String getWaterPauseList() {
	String deviceList = ''
	int i = 1
	if (settings.contacts) {
		settings.contacts.each {
			if (it.currentContact == settings.contactStop) {
				if (i > 1) deviceList += ', '
				deviceList = "${deviceList}${it.displayName} is ${settings.contactStop}"
				i++
			}
		}
	}
	if (settings.toggles) {
		settings.toggles.each {
			if (it.currentSwitch == settings.toggleStop) {
				if (i > 1) deviceList += ', '
				deviceList = "${deviceList}${it.displayName} is ${settings.toggleStop}"
				i++
			}
		}
	}
	return deviceList
}

// called after a pause to continue the interrupted watering session
def restartWatering() {
	if (!isWaterPaused()) {					// make sure we weren't paused while we were waiting to run
		atomicState.paused = false
		atomicState.pauseSecs += Math.round((now() - state.pauseTime) / 1000)
		atomicState.pauseTime = null
		
		log.debug "restart watering"
		controller.programOn()
		if (sensorhighoff) subscribe(sensor, "humidity", humidityHandler)    	
    	subscribe(controller, "switch${zone}.z${zone}off", zoneOffHandler)		// watch for zone being manually turned off
		controller."z${zone}on"()
		
		def secsLeft = atomicState.timeRemaining
		if (secsLeft < 10) secsLeft = 10
		runIn(secsLeft, stopWatering)    //sets off time
		String s = ''
		if (secsLeft > 1) s = 's'
		controller.notify("active", "${app.label}: Zone ${zone} unpaused for ${secsLeft} more sec${s}")
	}
}
							  
// handle end of pause session     
def waterUnpause(evt){
	if (!isWaterPaused()){ 					// only if ALL of the selected contacts are not open
		def cDelay = 10
        if (settings.contactDelay > 10) cDelay = settings.contactDelay
        runIn(cDelay, restartWatering)
		
		// unsubscribe(settings.controller)
		subWaterPause()							// allow stopping again while we wait for cycleOn to start
		
		log.debug "waterUnpause(): enabling device is ${evt.device} ${evt.value}"
		
		String cond = evt.value
		switch (cond) {
			case 'open':
				cond = 'opened'
				break
			case 'on':
				cond = 'switched on'
				break
			case 'off':
				cond = 'switched off'
				break
			//case 'closed':
			//	cond = 'closed'
			//	break
			case null:
				cond = '????'
				break
			default:
				break
		}
        controller.notify('pause', "${app.label}: ${evt.displayName} ${cond}, watering in ${cDelay} seconds")
	} 
	else {
		log.debug "waterUnpause(): one down - ${evt.displayName}"
	}
}
							  
// handle start of pause session
def waterPause(evt){
	log.debug "waterStop: ${evt.displayName}"

	if (!atomicState.paused) {
		unsubscribe(settings.controller)	// so we can turn off the zone without ending the program
		unschedule(startWatering)			// in case we got stopped again before we restart watering
		unschedule(restartWatering)
		unschedule(stopWatering)

		subWaterUnpause()
		atomicState.paused = true
		atomicState.pauseTime = now()			// figure out how much time is left
		atomicState.timeRemaining = (duration * 60) - Math.round(((now() - automicState.startTime) / 1000) - atomicState.pauseSecs)
		
		String cond = evt.value
		switch (cond) {
			case 'open':
				cond = 'opened'
				break
			case 'on':
				cond = 'switched on'
				break
			case 'off':
				cond = 'switched off'
				break
			//case 'closed':
			//	cond = 'closed'
			//	break
			case null:
				cond = '????'
				break
			default:
				break
		}
	    controller.notify('pause', "${app.label}: Watering paused - ${evt.displayName} ${cond}") // set to Paused
	}
	
	if ( controller.currentValue("switch${zone}") != "z${zone}off" ) {
		runIn(30, subOff)
		controller."z${zone}off"()								// stop the water
	}
	else 
		subscribe(controller, "switch${zone}.z${zone}off", zoneOffHandler) // allow manual off while paused
}
							  
// This is a hack to work around the delay in response from the controller to the above programOff command...
// We frequently see the off notification coming a long time after the command is issued, so we try to catch that so that
// we don't prematurely exit the cycle.
def subOff() {
	subscribe(controller, "switch${zone}.z${zone}off", zoneOffHandler)
}

def syncOn(evt){
	// double check that the switch is actually finished and not just paused
	if ((settings.sync.currentSwitch == 'off') && (settings.sync.currentStatus != 'pause')) {
		unsubscribe(settings.sync)
    	Random rand = new Random() 						// just in case there are multiple schedules waiting on the same controller
		int randomSeconds = rand.nextInt(120) + 15
    	runIn(randomSeconds, waterStart)					// no message so we don't clog the system
    	controller.notify('schedule', "${app.label}: ${settings.sync} finished, starting in ${randomSeconds} seconds")
	} // else, it is just pausing...keep waiting for the next "off"
}

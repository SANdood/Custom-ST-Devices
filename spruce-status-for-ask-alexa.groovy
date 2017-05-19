/**
 * Spruce Status translater for Ask Alexa
 *
 *  Copyright 2017 Barry A. Burke
 *
 *  Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License. You may obtain a copy of the License at:
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software distributed under the License is distributed
 *  on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License
 *  for the specific language governing permissions and limitations under the License.
 *
 */
 def getVersionNum() { return "1.0.1" }
private def getVersionLabel() { return "Spruce Status for Ask Alexa ${getVersionNum()}" }

definition(
    name: "Spruce Status for Ask Alexa",
    namespace: "SANdood",
    author: "Barry A. Burke (storageanarchy at gmail dot com)",
    description: "Translates Spruce Irrigation status notifications into Ask Alexa messages queue(s) - version ${getVersionNum}",
    category: "Convenience",
    singleInstance: true,
    iconUrl: "http://www.plaidsystems.com/smartthings/st_spruce_leaf_250f.png",
    iconX2Url: "http://www.plaidsystems.com/smartthings/st_spruce_leaf_250f.png",
    iconX3Url: "http://www.plaidsystems.com/smartthings/st_spruce_leaf_250f.png")  

preferences {
	page(name: "mainPage")
}

def mainPage() {
	dynamicPage(name: "mainPage", title: "Configure Spruce/Alexa Integration", uninstall: true, install: true) {
    
    	subscribe(location, "askAlexaMQ", askAlexaMQHandler)
        sendLocationEvent(name: "askAlexaMQRefresh", value: "refresh")

        section('') {
        	if (atomicState.askAlexaMQ == null) {
        		paragraph(image: 'https://raw.githubusercontent.com/MichaelStruck/SmartThingsPublic/master/smartapps/michaelstruck/ask-alexa.src/AskAlexa@2x.png', 
                		  title: 'Ask Alexa not Detected!', 
                          		 'Ask Alexa either isn\'t installed, or no Message Queues are defined (sending to the deprecated Primary Message Queue is not supported).\n\n' +
                				 'Please verify your Ask Alexa settings and then return here to complete the integration.')
        	} else {
        		paragraph(image: 'https://raw.githubusercontent.com/MichaelStruck/SmartThingsPublic/master/smartapps/michaelstruck/ask-alexa.src/AskAlexa@2x.png', 
            			  title: 'Ask Alexa Integration', '')
        		input(name: 'askAlexa', type: 'bool', title: 'Send Ecobee Alerts to Ask Alexa?', required: true, submitOnChange: true, defaultValue: false)
            	if (settings.askAlexa) {
            		if (!settings.listOfMQs || (settings.listOfMQs.size() == 0)) {
                		paragraph('Please select one or more Ask Alexa Message Queues below (sending to the deprecated Primary Message Queue is not supported):')
                	}
                	input(name: 'listOfMQs', type: 'enum', title: 'Send Alerts to these Ask Alexa Message Queues', options: atomicState.askAlexaMQ, submitOnChange: true, 
                    		multiple: true, required: true)
                	input(name: 'expire', type: 'number', title: 'Expire Alerts after how many hours (optional)?', submitOnChange: true, required: false, range: "1..*")
            	}
        	}
        }
        
        if (settings.askAlexa) {
        	section(''){
            	paragraph(image: 'http://www.plaidsystems.com/smartthings/st_spruce_leaf_250f.png', title: 'Spruce Irrigation Integration', '')
        		input(name: 'theSpruces', type: 'capability.switch', title: "Monitor these Spruce Controllers", multiple: true, required: true, submitOnChange: true)
            	input(name: 'zoneTimes', type: 'bool', title: 'Include zone run time details?', required: true, defaultValue: false)
                if (settings.lastOnly) {paragraph("Ask Alexa will keep only the latest status notification for each Spruce Controller. You can change this by turning off the toggle below:")}
                else if (settings.typeOnly) {paragraph("Ask Alexa will keep only the latest of each Type of status notification for each Spruce Controller. You can change this by turning off the toggle below:")}
                else if (settings.schedOnly) {paragraph("Ask Alexa will keep only the latest status notification for each Spruce Schedule. You can change this by turning off the toggle below:")}
                else if (settings.schedType) {paragraph("Ask Alexa will keep only the latest of each Type of status notification for each Spruce Schedule. You can change this by turning off the toggle below:")}
                else {paragraph("Ask Alexa will keep all status notifications from all Spruce Controllers regarding all Spruce Schedules. You can change this using the toggles below (select one):")}
            	
                def showMe = !settings.typeOnly && !settings.schedOnly && !settings.schedType
                if (showMe) {
                	input(name: "lastOnly", type: "bool", title: "Keep only the latest notice for each Controller?", defaultValue: true, submitOnChange: true)
                }
                showMe = !settings.lastOnly && !settings.schedOnly && !settings.schedType
                if (showMe) {
               		input(name: "typeOnly", type: "bool", title: "Keep only the latest notice of each Type for each Controller?", defaultValue: false, submitOnChange: true)
                }
                showMe = !settings.lastOnly && !settings.typeOnly && !settings.schedType
                if (showMe) {
                	input(name: "schedOnly", type: "bool", title: "Keep only the latest notice for each Schedule?", defaultValue: false, submitOnChange: true)
                }
                showMe = !settings.lastOnly && !settings.typeOnly && !settings.schedOnly
                if (showMe) {
                	input(name: "schedType", type: "bool", title: "Keep only the latest notice of each Type for each Schedule?", defaultValue: false, submitOnChange: true)
                }
            }
        }
        section (getVersionLabel())
    }
}

def installed() {
	log.trace "Installed with settings: ${settings}"
	initialize()
}

def updated() {
	log.trace "Updated with settings: ${settings}"
	unsubscribe()
	initialize()
}

def initialize() {
	log.trace "Initializing..."
	subscribe(location, "askAlexaMQ", askAlexaMQHandler)
	subscribe(theSpruces,'tileMessage',messageHandler)
    if (!atomicState.msgNumber) atomicState.msgNumber = 0 as BigInteger
//    messageHandler()
}

def messageHandler(evt=false) {
	if (!askAlexa) return // disabled for now...
    
	BigInteger msgNumber = atomicState.msgNumber + 1
    atomicState.msgNumber = msgNumber
    String thisSpruce
    String thisMessage
    if (evt) {
//    	log.debug "${evt.device.displayName}, ${evt.name}, ${evt.value}"
    	thisSpruce = evt.device.displayName
    	thisMessage = evt.stringValue
    } else {
    	return
    	thisSpruce = "Test"
        thisMessage = "Back Yard Sensor status: Adjusting +18% for weather forecast > Front Right, Skip: SS3: Front Right @ 31.0% (30%),"
    }
    String translatedMessage = ''
    Integer idx = thisMessage.indexOf(': ')
    String msgType = 'Other'
    if (idx) {
    	// Handle strings in form of "scheduleName: event"
    	String thisSchedule = thisMessage.take(idx)
        thisMessage = thisMessage.drop(idx+2)
        if (thisMessage.startsWith('Starting...')) { 
        	// Schedule Name: Starting...
        	thisMessage = 'Starting weather and moisture analysis' 
            msgType = 'Anlz'
        } else if (thisMessage.startsWith('Starting -')) {
        	// Schedule Name: Starting - ETC: Mon @ 2:44 AM
            String temp = thisMessage.drop(thisMessage.indexOf('ETC: ')+5)
            thisMessage = 'Watering started, estimated time of completion is ' + fullDay(temp.take(3)) + ' at ' + temp.drop(temp.indexOf('@')+1)
            msgType = 'Strt'
        } else if (thisMessage.startsWith('Watering in')) {
        	// Back Yard: Watering in 1 minute, run time: 24 minutes: Back Front: 2 x 8 min Front Flower Bed: 2 x 4 min
            // Front Yard: Watering in 1 minute, run time: 2 hours & 48 minutes: Front Yard Top: 2
            thisMessage = thisMessage.drop(thisMessage.indexOf('time: ')+6).replace('&', 'and')
            def theDetails = thisMessage.drop(thisMessage.indexOf(':')+2)	// save the zone details
            thisMessage = 'Watering in 1 minute - estimated run time is ' + thisMessage.take(thisMessage.indexOf(':')) 
            if (zoneTimes) thisMessage = thisMessage + '. Zone run times - ' + theDetails
            msgType = '1min'
        } else if (thisMessage.startsWith('Finished')) {
        	// Back Yard: Finished watering at 2:46 AM
            // perfect as it is!
            msgType = 'Done'
        } else if (thisMessage.contains('finished')) {
        	// Back Yard: Spruce New V2 finished, starting in 41 seconds
        	thisMessage = 'Controller ' + thisMessage.replace(',', ', schedule')
            msgType = 'Over'
        } else if (thisMessage.startsWith('Waiting for cu')) {
        	// ${app.label}: Waiting for currently running schedule to complete before starting
            thisMessage = thisMessage.replace('complete', 'finish')
            thisMessage = thisMessage.take(thisMessage.indexOf(' before'))						// Waiting for currently running schedule to finish
            msgType = 'WtSc'
        } else if (thisMessage.startsWith('Waiting')) {
        	// Back Yard: Waiting for Spruce New V2 to complete before starting
            thisMessage = 'Waiting for controller ' + thisMessage.drop(12).replace('complete', 'finish')
            thisMessage = thisMessage.take(thisMessage.indexOf(' before'))
            msgType = 'WtCt'
        } else if (thisSchedule.contains('Sensor st')) {
        	// Back Yard Sensor status: Adjusting +18% for weather forecast > Front Right, Skip: SS3: Front Right @ 31.0% (30%),
            thisSchedule = thisSchedule.take(thisSchedule.indexOf(' Sensor'))
            if (thisMessage.startsWith('Adjusting')) {
            	thisMessage = thisMessage.take(thisMessage.indexOf(' >')).trim()
            } else {
            	thisMessage = 'No weather adjustment applied'
            }
            msgType = 'Adjst'
        } else if (thisMessage.startsWith('Starts')) {
        	// Lower Right: Starts at 1:47 AM
            thisMessage = 'updated, ' + thisMessage
            msgType = 'Updt'
        } else if (thisMessage.startsWith('No ')) {
        	// Lower Left: No watering today
            // perfect as it is!
            msgType = 'NoH2O'
        } else if (thisMessage.startsWith('Watering p')) {
        	// Back Yard: Watering paused - Bathroom Light switched on
            // Back Yard: Watering paused - Bathroom door opened
            // perfect as it is!
            msgType = 'Paus'
        } else if (thisMessage.contains(' watering in')) {
        	// Back Yard: Bathroom Light switched off, watering in 37 seconds
            // perfect as it is!
            msgType = 'UnPaus'
        } else if (thisMessage.startsWith('Resuming')) {
        	// ${app.label}: Resuming - New ETC: ${finishTime}
            String temp = thisMessage.drop(thisMessage.indexOf('ETC: ')+5)
            thisMessage = 'Watering resumed, new estimated time of completion is '  + fullDay(temp.take(3)) + ' at ' + temp.drop(temp.indexOf('@')+1)
            msgType = 'Resm'
        } else if (thisMessage.startsWith('skipping,')) {
        	// All of these are already perfect as is!
            msgType = 'Skip'
        } else if (thisMessage.startsWith('Manual run,')) {
        	// ${app.label}: Manual run, watering in 1 minute: run time: 1 hour & 21 minutes: ${runNowMap}
            thisMessage = thisMessage.drop(thisMessage.indexOf('time: ')+6).replace('&', 'and')
            thisMessage = 'Manual run, watering in 1 minute - estimated run time is ' + thisMessage.take(thisMessage.indexOf(':'))
            msgType = 'Manl'
        } else if (thisMessage.startsWith('Zone ')) {
            // ${app.label}: Zone ${zone} unpaused for ${secsLeft} more sec${s}
            // perfect as it is
            msgType = 'Zone'
        }
        translatedMessage = 'Controller ' + thisSpruce + ', schedule ' + thisSchedule + ': ' + thisMessage + '.'
    } else {
    	translatedMessage = thisMessage
    }
    
    // Are we expiring old messages?
    def exHours = expire ? expire as int : 0
    def exSec=exHours * 3600
    
    // Handle which messages we keep / overwrite
    String messageID
    def overwriteMsg = true
    if (lastOnly) {
       	messageID = thisSpruce
    } else if (typeOnly) {
       	messageID = thisSpruce + msgType
    } else if (schedOnly) {
       	messageID = thisSchedule
    } else if (schedType) {
       	messageID = thisSchedule + msgType
    } else {
       	messageID = msgNumber.toString()
        overwriteMsg = false
    }
        
    String queues = getMQListNames()
        
    log.info "ID '${messageID}' to Ask Alexa ${queues.size()>0?queues:'Primary Message'} queue${listOfMQs.size()>1?'s':''}: ${translatedMessage}"
    sendLocationEvent(name: "AskAlexaMsgQueue", value: "Spruce Status", unit: messageID, isStateChange: true, 
    					descriptionText: translatedMessage, data: [ queues: listOfMQs, overwrite: overwriteMsg, expires: exSec ])
}
String getMQListNames() {
	if ((settings.listOfMQs == null) || (settings.listOfMQs.size() == 0)) return ''
	def temp = []
   	settings.listOfMQs?.each { 
    	temp << atomicState.askAlexaMQ?.getAt(it).value
    }
    return ((temp.size() > 1) ? ('(' + temp.join(", ") + ')') : (temp.toString()))?.replaceAll("\\[",'').replaceAll("\\]",'')
}
String fullDay(String day) {
	String theDay
	switch (day) {
    	case 'Mon':
        case 'Fri':
        case 'Sun':
        	theDay = day + 'day'
            break;
        case 'Tue':
        	theDay = 'Tuesday'
            break;
        case 'Wed':
        	theDay = 'Wednesday'
            break;
        case 'Thu':
        	theDay = 'Thursday'
            break;
        case 'Sat':
       		theDay = 'Saturday'
            break;
    }
    return theDay
}
def askAlexaMQHandler(evt) {
	log.debug "askAlexaMQHandler ${evt?.name} ${evt?.value}"

    if (!evt) return
    switch (evt.value) {
	    case "refresh":
		    atomicState.askAlexaMQ = evt.jsonData && evt.jsonData?.queues ?   evt.jsonData.queues : []
        break
    }
}

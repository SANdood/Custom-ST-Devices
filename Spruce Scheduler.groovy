/**
 *  Spruce Scheduler Pre-release V2.52.6 8/17/2016
 *
 *	
 *  Copyright 2015 Plaid Systems
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
 
-------v2.51---------------------
 schedule function changed so runIn does not overwrite and cancel schedule
 -ln 769 schedule cycleOn-> checkOn
 -ln 841 checkOn function
 -ln 863 state.run = false
 
-------Fixes
 -changed weather from def to Map
 -ln 968 if(runnowmap) -> pumpmap
 
-------Fixes V2.2-------------
-History log messages condensed
-Seasonal adjustment redefined -> weekly & daily
-Learn mode redefined
-No Learn redefined to operate any available days
-ZoneSettings page redefined -> required to setup zones
-Weather rain updated to fix error with some weather stations
-Contact time delay added
-new plants moisture and season redefined
*
*
-------Fixes V2.1-------------
-Many fixes, code cleanup by Jason C
-open fields leading to unexpected errors
-setting and summary improvements
-multi controller support
-Day to run mapping
-Contact delays optimized
-Warning notification added
-manual start subscription added
 *
 */
 
definition(
    name: "Spruce Scheduler v2.52",
    namespace: "plaidsystems",
    author: "Plaid Systems",
    description: "Spruce automatic water scheduling app v2.52.6",
    category: "Green Living",
    iconUrl: "http://www.plaidsystems.com/smartthings/st_spruce_leaf_250f.png",
    iconX2Url: "http://www.plaidsystems.com/smartthings/st_spruce_leaf_250f.png",
    iconX3Url: "http://www.plaidsystems.com/smartthings/st_spruce_leaf_250f.png")    
 
preferences {
    page(name: "startPage")
    page(name: "autoPage")
    page(name: "zipcodePage")
    page(name: "weatherPage")
    page(name: "globalPage")
    page(name: "contactPage")
    page(name: "delayPage")
    page(name: "zonePage")    

	page(name: "zoneSettingsPage")
    page(name: "zoneSetPage")
    page(name: "plantSetPage")
    page(name: "sprinklerSetPage")
    page(name: "optionSetPage")
    
    //found at bottom - transition pages
    page(name: "zoneSetPage1")
    page(name: "zoneSetPage2")
    page(name: "zoneSetPage3")
    page(name: "zoneSetPage4")
    page(name: "zoneSetPage5")
    page(name: "zoneSetPage6")
    page(name: "zoneSetPage7")
    page(name: "zoneSetPage8")
    page(name: "zoneSetPage9")
    page(name: "zoneSetPage10")
    page(name: "zoneSetPage11")
    page(name: "zoneSetPage12")
    page(name: "zoneSetPage13")
    page(name: "zoneSetPage14")
    page(name: "zoneSetPage15")
    page(name: "zoneSetPage16") 
}
 
def startPage(){
    dynamicPage(name: "startPage", title: "Spruce Smart Irrigation setup V2.52", install: true, uninstall: true)
    {                      
            section(""){
            href(name: "globalPage", title: "Schedule settings", required: false, page: "globalPage",
                image: "http://www.plaidsystems.com/smartthings/st_settings.png",                
                description: "Schedule: ${enableString()}\nWatering Time: ${startTimeString()}\nDays:${daysString()}\nNotifications:\n${notifyString()}")
             
            }
             
            section(""){            
            href(name: "weatherPage", title: "Weather Settings", required: false, page: "weatherPage",
                image: "http://www.plaidsystems.com/smartthings/st_rain_225_r.png",
                description: "Weather from: ${zipString()}\nRain Delay: ${isRainString()}\nSeasonal Adjust: ${seasonalAdjString()}")
            }
             
            section(""){            
            href(name: "zonePage", title: "Zone summary and setup", required: false, page: "zonePage",
                image: "http://www.plaidsystems.com/smartthings/st_zone16_225.png",
                description: "${getZoneSummary()}")
            }
             
            section(""){            
            href(name: "delayPage", title: "Valve and Contact delays", required: false, page: "delayPage",
                image: "http://www.plaidsystems.com/smartthings/st_timer.png",
                description: "Valve Delay: ${pumpDelayString()} s\nContact Sensor: ${contactSensorString()}\nSchedule Sync: ${syncString()}")
            }
            section(""){
                href title: "Spruce Irrigation Knowledge Base", //page: "customPage",
                description: "Explore our knowledge base for more information on Spruce and Spruce sensors.  Contact from is also available here.",
                required: false, style:"embedded",             
                image: "http://www.plaidsystems.com/smartthings/st_spruce_leaf_250f.png",
                url: "http://support.spruceirrigation.com"
            }  
       }
}
 
def globalPage() {
    dynamicPage(name: "globalPage", title: "") {
        section("Spruce schedule Settings") {
                label title: "Schedule Name:", description: "Name this schedule", required: false                
                input "switches", "capability.switch", title: "Spruce Irrigation Controller:", description: "Select a Spruce controller", required: true, multiple: false
		}        


        section("Program Scheduling"){
            input "enable", "bool", title: "Enable watering:", defaultValue: 'true', metadata: [values: ['true', 'false']]
            input "enableManual", "bool", title: "Enable this schedule for manual start, only 1 schedule should be enabled for manual start at a time!", defaultValue: 'true', metadata: [values: ['true', 'false']]
            input "startTime", "time", title: "Watering start time", required: true            
            paragraph image: "http://www.plaidsystems.com/smartthings/st_calander.png",
                      title: "Selecting watering days", 
                      "Selecting watering days is optional. Spruce will optimize your watering schedule automatically. If your area has water restrictions or you prefer set days, select the days to meet your requirements. "
			input (name: "days", type: "enum", title: "Water only on these days...", required: false, multiple: true,
            metadata: [values: ['Monday', 'Tuesday', 'Wednesday', 'Thursday', 'Friday', 'Saturday', 'Sunday', 'Even', 'Odd']])            
		}

        section("Push Notifications") {
                input (name: "notify", type: "enum", title: "Select what push notifications to receive.", required: false, multiple: true,
                metadata: [values: ['Daily', 'Weekly', 'Delays', 'Warnings', 'Weather', 'Moisture', 'Events']])
                input("recipients", "contact", title: "Send push notifications to", required: false, multiple: true)
        } 
         
    }
}
 
 
def weatherPage() {
    dynamicPage(name: "weatherPage", title: "Weather settings") {
       section("Location to get weather forecast and conditions:") {
            href(name: "hrefWithImage", title: "${zipString()}", page: "zipcodePage",
             description: "Set local weather station",
             required: false,             
             image: "http://www.plaidsystems.com/smartthings/rain.png")             
            input "isRain", "bool", title: "Enable Rain check:", metadata: [values: ['true', 'false']] 
            input "rainDelay", "decimal", title: "inches of rain that will delay watering, default: 0.2", required: false
            input "isSeason", "bool", title: "Enable Seasonal Weather Adjustment:", metadata: [values: ['true', 'false']]
        }                
    }    
}
 
def zipcodePage() {
    return dynamicPage(name: "zipcodePage", title: "Spruce weather station setup") {
        section(""){input "zipcode", "text", title: "Zipcode or WeatherUnderground station id. Default value is current location.", defaultValue: "${location.zipCode}", required: false, submitOnChange: true
                }
         
        section(""){href title: "Search WeatherUnderground.com for weather stations",
             description: "After page loads, select Change Station for a list of weather stations.  You will need to copy the station code into the zipcode field above",
             required: false, style:"embedded",             
             image: "http://www.plaidsystems.com/smartthings/wu.png",
             url: "http://www.wunderground.com/q/${location.zipCode}"
             }
    }
}
 
private startTimeString(){  
    def newtime = "${settings["startTime"]}"
    if ("${settings["startTime"]}" == "null") return "Please set!"   
    else return "${hhmm(newtime)}"    
}

def enableString(){
	if(enable && enableManual) return "On, Manual Enabled"
    else if (enable) return "On"
    return "Off"
}

def contactSensorString(){
	if(contact) return "${contact} \n  Delay: ${contactDelay} mins"
    return "None"
}

def isRainString(){
	if (isRain && rainDelay == null) return "0.2"
    if(isRain) return "${rainDelay}"
    return "Off"
}    
    
def seasonalAdjString(){
	if(isSeason) return "On"
    return "Off"
}

def syncString(){
	if(sync) return "${sync}"
    return "None"
}

def notifyString(){
	def notifyString = ""
	if("${settings["notify"]}" != "null") {
      if (notify.contains('Weekly')) notifyString = "${notifyString} Weekly"
      if (notify.contains('Daily')) notifyString = "${notifyString} Daily"
      if (notify.contains('Weather')) notifyString = "${notifyString} Weather"
      if (notify.contains('Warnings')) notifyString = "${notifyString} Warnings"
      if (notify.contains('Moisture')) notifyString = "${notifyString} Moisture"
   }
   if(notifyString == "")
   	  notifyString = " None"
   return notifyString
}

def daysString(){
	def daysString = ""
    if ("${settings["days"]}" != "null") {
    	if(days.contains('Even') || days.contains('Odd')) {
        	if (days.contains('Even')) daysString = "${daysString} Even"
      		if (days.contains('Odd')) daysString = "${daysString} Odd"
        } else {
            if (days.contains('Monday')) daysString = "${daysString} M"
        	if (days.contains('Tuesday')) daysString = "${daysString} Tu"
        	if (days.contains('Wednesday')) daysString = "${daysString} W"
        	if (days.contains('Thursday')) daysString = "${daysString} Th"
        	if (days.contains('Friday')) daysString = "${daysString} F"
        	if (days.contains('Saturday')) daysString = "${daysString} Sa"
        	if (days.contains('Sunday')) daysString = "${daysString} Su"
        }
    }
    if(daysString == "")
   	  daysString = " Any"
    return daysString
}
 
    
private hhmm(time, fmt = "h:mm a"){
    def t = timeToday(time, location.timeZone)
    def f = new java.text.SimpleDateFormat(fmt)
    f.setTimeZone(location.timeZone ?: timeZone(time))
    f.format(t)
}
 
def pumpDelayString(){
    if ("${settings["pumpDelay"]}" == "null") return "0"
    else return "${settings["pumpDelay"]}"
}
 
def delayPage() {
    dynamicPage(name: "delayPage", title: "Additional Options") {
        section(""){
            paragraph image: "http://www.plaidsystems.com/smartthings/st_timer.png",
                      title: "Pump and Master valve delay",
                      required: false,
                      "Setting a delay is optional, default is 0.  If you have a pump that feeds water directly into your valves, set this to 0. To fill a tank or build pressure, you may increase the delay.\nStart->Pump On->delay->Valve On->Valve Off->delay"
        }
        section("") {
                input "pumpDelay", "number", title: "Set a delay in seconds?", defaultValue: '0', required: false
        }
        section(""){
            paragraph image: "http://www.plaidsystems.com/smartthings/st_pause.png",
                      title: "Contact delays",
                      required: false,
                      "Selecting contacts is optional. When a selected contact sensor is opened, water immediately stops and will not resume until closed.  Caution: if a contact is set and left open, the watering program will never run."
        }
        section("") {
            input name: "contact", title: "Select water delay contacts", type: "capability.contactSensor", multiple: true, required: false            

			input "contactDelay", "number", title: "How many minutes delay after contact is closed?", defaultValue: '1', required: false
        }
        section(""){
            paragraph image: "http://www.plaidsystems.com/smartthings/st_spruce_controller_250.png",
                      title: "Controller Sync",
                      required: false,
                      "For multiple controllers only.  This schedule will wait for the selected controller to finish. Do not set with a single controller!"
        			  input "sync", "capability.switch", title: "Select Master Controller", description: "Only use this setting with multiple controllers", required: false, multiple: false
        }
    }
}
 
def zonePage() {    
    dynamicPage(name: "zonePage", title: "Zone setup", install: false, uninstall: false) {
		section("") {
            href(name: "hrefWithImage", title: "Zone configuration", page: "zoneSettingsPage",
             description: "${zoneString()}",
             required: false,             
             image: "http://www.plaidsystems.com/smartthings/st_spruce_leaf_250f.png")
        }

		if (zoneActive("1")){
        section(""){
            href(name: "z1Page", title: "1: ${getname("1")}", required: false, page: "zoneSetPage1",
            	image: "${getimage("1")}",                
            	description: "${display("1")}" )
            }
        }
        if (zoneActive("2")){
        section(""){
            href(name: "z2Page", title: "2: ${getname("2")}", required: false, page: "zoneSetPage2",
            	image: "${getimage("2")}",
            	description: "${display("2")}" )
            }
        }
        if (zoneActive("3")){
        section(""){
            href(name: "z3Page", title: "3: ${getname("3")}", required: false, page: "zoneSetPage3",
            	image: "${getimage("3")}",
            	description: "${display("3")}" )
            }
        }
        if (zoneActive("4")){
        section(""){
            href(name: "z4Page", title: "4: ${getname("4")}", required: false, page: "zoneSetPage4",
            	image: "${getimage("4")}",
            	description: "${display("4")}" )
            }
        }
        if (zoneActive("5")){
        section(""){
            href(name: "z5Page", title: "5: ${getname("5")}", required: false, page: "zoneSetPage5",
            	image: "${getimage("5")}",
            	description: "${display("5")}" )
            }
        }
        if (zoneActive("6")){
        section(""){
            href(name: "z6Page", title: "6: ${getname("6")}", required: false, page: "zoneSetPage6",
            	image: "${getimage("6")}",
            	description: "${display("6")}" )
            }
        }
        if (zoneActive("7")){    
        section(""){
            href(name: "z7Page", title: "7: ${getname("7")}", required: false, page: "zoneSetPage7",
            	image: "${getimage("7")}",
            	description: "${display("7")}" )
            }
        }
        if (zoneActive("8")){
        section(""){
            href(name: "z8Page", title: "8: ${getname("8")}", required: false, page: "zoneSetPage8",
            	image: "${getimage("8")}",
            	description: "${display("8")}" )
            }
        }
        if (zoneActive("9")){
        section(""){
            href(name: "z9Page", title: "9: ${getname("9")}", required: false, page: "zoneSetPage9",
            	image: "${getimage("9")}",
                description: "${display("9")}" )
            }
        }
        if (zoneActive("10")){
        section(""){
            href(name: "z10Page", title: "10: ${getname("10")}", required: false, page: "zoneSetPage10",
            	image: "${getimage("10")}",
                description: "${display("10")}" )
            }
        }
        if (zoneActive("11")){
        section(""){
            href(name: "z11Page", title: "11: ${getname("11")}", required: false, page: "zoneSetPage11",
            	image: "${getimage("11")}",
                description: "${display("11")}" )
            }
        }
        if (zoneActive("12")){
        section(""){
            href(name: "z12Page", title: "12: ${getname("12")}", required: false, page: "zoneSetPage12",
            	image: "${getimage("12")}",
                description: "${display("12")}" )
            }
        }
        if (zoneActive("13")){
        section(""){
            href(name: "z13Page", title: "13: ${getname("13")}", required: false, page: "zoneSetPage13",
            	image: "${getimage("13")}",
                description: "${display("13")}" )
            }
        }
        if (zoneActive("14")){
        section(""){
            href(name: "z14Page", title: "14: ${getname("14")}", required: false, page: "zoneSetPage14",
            	image: "${getimage("14")}",
                description: "${display("14")}" )
            }
        }
        if (zoneActive("15")){
        section(""){
            href(name: "z15Page", title: "15: ${getname("15")}", required: false, page: "zoneSetPage15",
            	image: "${getimage("15")}",
                description: "${display("15")}" )
            }
        }
        if (zoneActive("16")){
        section(""){
            href(name: "z16Page", title: "16: ${getname("16")}", required: false, page: "zoneSetPage16",
            	image: "${getimage("16")}",
                description: "${display("16")}" )
            }
        }        

    }
}

// Verify whether a zone is active
boolean zoneActive(String zoneStr){
	if (!zoneNumber) return false
    if (zoneNumber.contains(zoneStr)) return true
    return false
}

String zoneString(){
	def numberString = "Add zones to setup"
    if (zoneNumber) numberString = "Zones enabled: " + "${zoneNumber}"
    if (learn) numberString += "\nSensor mode: Adaptive"
    else numberString += "\nSensor mode: Delay"
    return numberString
    }

/*//code change for ST update file -> change input to zoneNumberEnum   
def zoneActive(z){	
    if (!zoneNumberEnum && zoneNumber && zoneNumber >= z.toInteger()) return true        
    else if (!zoneNumberEnum && zoneNumber && zoneNumber != z.toInteger()) return false
    else if (zoneNumberEnum.contains(z)) return true
    return false
}

def zoneString(){
	def numberString = "Add zones to setup"    
    if (zoneNumberEnum) numberString = "Zones enabled: " + "${zoneNumberEnum}"    
    if (learn) numberString += "\nSensor mode: Adaptive"
    else numberString += "\nSensor mode: Delay"
    return numberString
    }
*/    
def zoneSettingsPage(){
	dynamicPage(name: "zoneSettingsPage", title: "Zone Configuration") {
        	section(""){
        	//input (name: "zoneNumber", type: "number", title: "Enter number of zones to configure?",description: "How many valves do you have? 1-16", required: true)//, defaultValue: 16)
            input "zoneNumber", "enum", title: "Select zones to configure", multiple: true, metadata: [values: ['1','2','3','4','5','6','7','8','9','10','11','12','13','14','15','16']]
            input "gain", "number", title: "Increase or decrease all water times by this %, enter a negative or positive value, Default: 0", required: false
			paragraph image: "http://www.plaidsystems.com/smartthings/st_sensor_200_r.png",
                      title: "Moisture sensor adapt mode",                      
                      "Adaptive mode: Watering times will be adjusted based on the assigned moisture sensor.\n\nNo Adaptive mode: Zones with moisture sensors will water on any available days when the low moisture setpoint has been reached."
         	input "learn", "bool", title: "Enable Adaptive Moisture Control (with moisture sensors)", metadata: [values: ['true', 'false']]
            }
     }
}

def zoneSetPage(){    
    dynamicPage(name: "zoneSetPage", title: "Zone ${state.app} Setup") {
        section(""){
            paragraph image: "http://www.plaidsystems.com/smartthings/st_${state.app}.png",             
            title: "Current Settings",            
            "${display("${state.app}")}"        
            }
        section(""){
                
                input "name${state.app}", "text", title: "Zone name?", required: false, defaultValue: "Zone ${state.app}"
                }        
        section(""){            
			 href(name: "tosprinklerSetPage", title: "Sprinkler type: ${setString("zone")}", required: false, page: "sprinklerSetPage",
                image: "${getimage("${settings."zone${state.app}"}")}",         
                //description: "Set sprinkler nozzle type or turn zone off")
                description: "Sprinkler type descriptions")         
            
             input "zone${state.app}", "enum", title: "Sprinkler Type", multiple: false, required: false, defaultValue: 'Off', submitOnChange: true, metadata: [values: ['Off', 'Spray', 'Rotor', 'Drip', 'Master Valve', 'Pump']]
            
             }
        section(""){            
            href(name: "toplantSetPage", title: "Landscape Select: ${setString("plant")}", required: false, page: "plantSetPage",
                image: "${getimage("${settings["plant${state.app}"]}")}",
                //description: "Set landscape type")
                description: "Landscape type descriptions")
            
            input "plant${state.app}", "enum", title: "Landscape", multiple: false, required: false, submitOnChange: true, metadata: [values: ['Lawn', 'Garden', 'Flowers', 'Shrubs', 'Trees', 'Xeriscape', 'New Plants']]
            
            }  
         
        section(""){            
            href(name: "tooptionSetPage", title: "Options: ${setString("option")}", required: false, page: "optionSetPage",
                image: "${getimage("${settings["option${state.app}"]}")}",
                //description: "Set watering options")
                description: "Watering option descriptions")
            
	            input "option${state.app}", "enum", title: "Options", multiple: false, required: false, defaultValue: 'Cycle 2x', submitOnChange: true,metadata: [values: ['Slope', 'Sand', 'Clay', 'No Cycle', 'Cycle 2x', 'Cycle 3x']]    

            }            
        section(""){
            paragraph image: "http://www.plaidsystems.com/smartthings/st_sensor_200_r.png",
                      title: "Moisture sensor settings",                      
                      "Select a soil moisture sensor to monitor and control watering.  The soil moisture target value is set to a default value but can be adjusted to tune watering"
         
                input "sensor${state.app}", "capability.relativeHumidityMeasurement", title: "Select moisture sensor?", required: false, multiple: false
         
                input "sensorSp${state.app}", "number", title: "Minimum moisture sensor target value, Setpoint: ${getDrySp(state.app)}", required: false
        	}
        section(""){
            paragraph image: "http://www.plaidsystems.com/smartthings/st_timer.png",
                      title: "Optional: Enter total watering time per week", 
                      "This value will replace the calculated time from other settings"
         
                input "minWeek${state.app}", "number", title: "Minimum water time per week.\nDefault: 0 = autoadjust", description: "minutes per week", required: false
                
                input "perDay${state.app}", "number", title: "Guideline value for time per day, this divides minutes per week into watering days. Default: 20", defaultValue: '20', required: false
        }
    }
}    

def setString(i){
	if (i == "zone"){
    	if (settings["zone${state.app}"] != null) return "${settings["zone${state.app}"]}"
        else return "Not Set"
        }
	if (i == "plant"){
    	if (settings["plant${state.app}"] != null) return "${settings["plant${state.app}"]}"
        else return "Not Set"
        }
	if (i == "option"){
    	if (settings["option${state.app}"] != null) return "${settings["option${state.app}"]}"
        else return "Not Set"
        }
}

def plantSetPage(){ 
    dynamicPage(name: "plantSetPage", title: "${settings["name${state.app}"]} Landscape Select") {
        section(""){
            paragraph image: "http://www.plaidsystems.com/img/st_${state.app}.png",             
                title: "${settings["name${state.app}"]}",
                "Current settings ${display("${state.app}")}"
                 
            //input "plant${state.app}", "enum", title: "Landscape", multiple: false, required: false, submitOnChange: true, metadata: [values: ['Lawn', 'Garden', 'Flowers', 'Shrubs', 'Trees', 'Xeriscape', 'New Plants']]
        }        
        section(""){
            paragraph image: "http://www.plaidsystems.com/smartthings/st_lawn_200_r.png",             
            title: "Lawn",            
            "Select Lawn for typical grass applications"
             
            paragraph image: "http://www.plaidsystems.com/smartthings/st_garden_225_r.png",             
            title: "Garden",            
            "Select Garden for vegetable gardens"
            
            paragraph image: "http://www.plaidsystems.com/smartthings/st_flowers_225_r.png",             
            title: "Flowers",            
            "Select Flowers for beds with smaller seasonal plants"
             
            paragraph image: "http://www.plaidsystems.com/smartthings/st_shrubs_225_r.png",             
            title: "Shrubs",            
            "Select Shrubs for beds with larger established plants"
           
            paragraph image: "http://www.plaidsystems.com/smartthings/st_trees_225_r.png",             
            title: "Trees",            
            "Select Trees for deep rooted areas without other plants"
           
            paragraph image: "http://www.plaidsystems.com/smartthings/st_xeriscape_225_r.png",             
            title: "Xeriscape",            
            "Reduces water for native or drought tolorent plants"
            
            paragraph image: "http://www.plaidsystems.com/smartthings/st_newplants_225_r.png",             
            title: "New Plants",            
            "Increases watering time per week and reduces automatic adjustments to help establish new plants. No weekly seasonal adjustment and moisture setpoint set to 40."
        }
    }
}
 
def sprinklerSetPage(){
    dynamicPage(name: "sprinklerSetPage", title: "${settings["name${state.app}"]} Sprinkler Select") {
        section(""){
            paragraph image: "http://www.plaidsystems.com/img/st_${state.app}.png",             
            title: "${settings["name${state.app}"]}",
            "Current settings ${display("${state.app}")}"
            //input "zone${state.app}", "enum", title: "Sprinkler Type", multiple: false, required: false, defaultValue: 'Off', metadata: [values: ['Off', 'Spray', 'Rotor', 'Drip', 'Master Valve', 'Pump']]
                         
            }
        section(""){
            paragraph image: "http://www.plaidsystems.com/smartthings/st_spray_225_r.png",             
            title: "Spray",            
            "Spray sprinkler heads spray a fan of water over the lawn. The water is applied evenly and can be turned on for a shorter duration of time."
             
            paragraph image: "http://www.plaidsystems.com/smartthings/st_rotor_225_r.png",             
            title: "Rotor",            
            "Rotor sprinkler heads rotate, spraying a stream over the lawn.  Because they move back and forth across the lawn, they require a longer water period."
             
            paragraph image: "http://www.plaidsystems.com/smartthings/st_drip_225_r.png",             
            title: "Drip",            
            "Drip lines or low flow emitters water slowely to minimize evaporation, because they are low flow, they require longer watering periods."
             
            paragraph image: "http://www.plaidsystems.com/smartthings/st_master_225_r.png",             
            title: "Master",            
            "Master valves will open before watering begins.  Set the delay between master opening and watering in delay settings."
             
            paragraph image: "http://www.plaidsystems.com/smartthings/st_pump_225_r.png",             
            title: "Pump",            
            "Attach a pump relay to this zone and the pump will turn on before watering begins.  Set the delay between pump start and watering in delay settings."
        }
    }
}
 
def optionSetPage(){
    dynamicPage(name: "optionSetPage", title: "${settings["name${state.app}"]} Options") {
        section(""){
            paragraph image: "http://www.plaidsystems.com/img/st_${state.app}.png",             
            title: "${settings["name${state.app}"]}",
            "Current settings ${display("${state.app}")}"
            //input "option${state.app}", "enum", title: "Options", multiple: false, required: false, defaultValue: 'Cycle 2x', metadata: [values: ['Slope', 'Sand', 'Clay', 'No Cycle', 'Cycle 2x', 'Cycle 3x']]    
        }
        section(""){
            paragraph image: "http://www.plaidsystems.com/smartthings/st_slope_225_r.png",             
            title: "Slope",            
            "Slope sets the sprinklers to cycle 3x, each with a short duration to minimize runoff"
             
            paragraph image: "http://www.plaidsystems.com/smartthings/st_sand_225_r.png",             
            title: "Sand",            
            "Sandy soil drains quickly and requires more frequent but shorter intervals of water."
             
            paragraph image: "http://www.plaidsystems.com/smartthings/st_clay_225_r.png",             
            title: "Clay",            
            "Clay sets the sprinklers to cycle 2x, each with a short duration to maximize absorption"
             
            paragraph image: "http://www.plaidsystems.com/smartthings/st_cycle1x_225_r.png",             
            title: "No Cycle",            
            "The sprinklers will run for 1 long duration"
             
            paragraph image: "http://www.plaidsystems.com/smartthings/st_cycle2x_225_r.png",             
            title: "Cycle 2x",            
            "Cycle 2x will break the water period up into 2 shorter cycles to help minimize runoff and maximize adsorption"
             
            paragraph image: "http://www.plaidsystems.com/smartthings/st_cycle3x_225_r.png",             
            title: "Cycle 3x",            
            "Cycle 3x will break the water period up into 3 shorter cycles to help minimize runoff and maximize adsorption"
        }
    }
}
 
def setPage(i){
    if (i != "null") state.app = i
    return state.app
}

String getaZoneSummary(int zone){
  	//log.trace "getZoneSummary(${zone})"
  	
  	String daysString = ""
    int tpw = initTPW(zone)
  	int dpw = initDPW(zone)
  	int runTime = calcRunTime(tpw, dpw)
  	
  	if ( !learn && (settings["sensor${zone}"] != null) ) {
  	 	daysString = 'if Moisture is low on: '
     	dpw = daysAvailable()
  	}  
  	if (days && (days.contains('Even') || days.contains('Odd'))) {
    	if (dpw == 1) daysString = 'Every 8 days'
    	if (dpw == 2) daysString = 'Every 4 days'
    	if (dpw == 4) daysString = 'Every 2 days'
    	if (days.contains('Even') && days.contains('Odd')) daysString = 'any day'
  	} else {
    	def int[] dpwMap = [0,0,0,0,0,0,0]
     	dpwMap = getDPWDays(dpw)
     	daysString += getRunDays(dpwMap)
  	}  
  	return "${zone}: ${runTime} minutes, ${daysString}"
}

def getZoneSummary(){
 	String summary = ''
    if (learn) summary = 'Moisture Learning enabled'
    else summary = 'Moisture Learning disabled'
         
    int zone = 1
    createDPWMap()
    while(zone <= 16) {	  
      	String zoneSum = getaZoneSummary(zone)
      	if (nozzle(zone) == 4) summary += "\n${zone}: ${settings["zone${zone}"]}"
      	else if ( "${initDPW(zone)}" != "0" && zoneActive(zone.toString()) ) summary += "\n${zoneSum}"
      	zone++
    }
    if(summary == '') return zoneString()	//"Setup all 16 zones"
    
    return summary
}
 
String display(String i){
	//log.trace "display(${i})"
    String displayString = ''    
    int tpw = initTPW(i.toInteger())
    int dpw = initDPW(i.toInteger())
    int runTime = calcRunTime(tpw, dpw)
    if (settings."zone${i}") displayString += settings."zone${i}" + " : "
    if (settings."plant${i}") displayString += settings."plant${i}" + " : "
    if (settings."option${i}") displayString += settings."option${i}" + " : "
    int j = i.toInteger()
s
    if ((runTime != 0) && (dpw != 0)) displayString += "${runTime} minutes, ${dpw} days per week"
    return displayString
}

String getimage(String i){
    log.debug i
    if (settings."zone${i}" == 'Off') return 'http://www.plaidsystems.com/smartthings/off2.png'   
    else if (settings."zone${i}" == 'Master Valve') return 'http://www.plaidsystems.com/smartthings/master.png'
    else if (settings."zone${i}" == 'Pump') return 'http://www.plaidsystems.com/smartthings/pump.png'
    else if (settings."plant${i}" != null && settings."zone${i}" != null) i = settings."plant${i}"
    log.debug i
    switch(i){
        case "null":
        // case null:
            return 'http://www.plaidsystems.com/smartthings/off2.png'
        case 'Off':
            return 'http://www.plaidsystems.com/smartthings/off2.png'
        case 'Lawn':
            return 'http://www.plaidsystems.com/smartthings/st_lawn_200_r.png'
        case 'Garden':
            return 'http://www.plaidsystems.com/smartthings/st_garden_225_r.png'
        case 'Flowers':
            return 'http://www.plaidsystems.com/smartthings/st_flowers_225_r.png'
        case 'Shrubs':
            return 'http://www.plaidsystems.com/smartthings/st_shrubs_225_r.png'
        case 'Trees':
            return 'http://www.plaidsystems.com/smartthings/st_trees_225_r.png'
        case 'Xeriscape':
            return 'http://www.plaidsystems.com/smartthings/st_xeriscape_225_r.png'
        case 'New Plants':
            return 'http://www.plaidsystems.com/smartthings/st_newplants_225_r.png'
        case 'Spray':
            return 'http://www.plaidsystems.com/smartthings/st_spray_225_r.png'
        case 'Rotor':
            return 'http://www.plaidsystems.com/smartthings/st_rotor_225_r.png'
        case 'Drip':
            return 'http://www.plaidsystems.com/smartthings/st_drip_225_r.png'
        case 'Master Valve':
            return "http://www.plaidsystems.com/smartthings/st_master_225_r.png"
        case 'Pump':
            return 'http://www.plaidsystems.com/smartthings/st_pump_225_r.png'
        case 'Slope':
            return 'http://www.plaidsystems.com/smartthings/st_slope_225_r.png'
        case 'Sand':
            return 'http://www.plaidsystems.com/smartthings/st_sand_225_r.png'
        case 'Clay':
            return 'http://www.plaidsystems.com/smartthings/st_clay_225_r.png'
        case 'No Cycle':
            return 'http://www.plaidsystems.com/smartthings/st_cycle1x_225_r.png'
        case 'Cycle 2x':
            return 'http://www.plaidsystems.com/smartthings/st_cycle2x_225_r.png'
        case "Cycle 3x":
            return 'http://www.plaidsystems.com/smartthings/st_cycle3x_225_r.png'
        default:
            return 'http://www.plaidsystems.com/smartthings/off2.png'            
        }
    }
 
String getname(String i) { 
    if (settings."name${i}" != "null") return settings."name${i}"  
    else return "Zone ${i}"
}

private String zipString() {
    if (!zipcode) return "${location.zipCode}"
    //add pws for correct weatherunderground lookup
    if (!zipcode.isNumber()) return "pws:${zipcode}"
    else return zipcode
}
         
//app install
def installed() {
    state.dpwMap = [0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0]
    state.tpwMap = [0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0]
    state.Rain = [0,0,0,0,0,0,0]    
    state.daycount = [0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0]    
    
    log.debug "Installed with settings: ${settings}"
    installSchedule()
}
 
def updated() {    
    log.debug "Installed with settings: ${settings}"
    installSchedule()    
}
 
def installSchedule(){
	state.seasonAdj = 100
    state.weekseasonAdj = 0
    unsubscribe()
    unschedule()
    state.run = false
    int randomOffset = 0
    
    if (enableManual) subscribe(switches, "switch.programOn", manualStart)
    randomOffset = getRandomNumber(1) * 1000	//random number added to start time so multiple schedules do not have identical time
    if (switches && startTime && enable){    	
        def checktime = timeToday(startTime, location.timeZone).getTime() + randomOffset
    	schedule(checktime, preCheck)	//check weather & Days
		Random rand = new Random()
    	def randomSeconds = rand.nextInt(59)    	
        schedule("${randomSeconds} 57 23 1/1 * ? *", getRainToday)		// capture today's rainfall just before midnight

        writeSettings()
        note("schedule", "${app.label} schedule set to start at ${startTimeString()}", "i")
    }
    else note("disable", "Automatic watering turned off or incomplete setup.", "w")
}
 
//write initial zone settings to device at install/update
def writeSettings(){    
    if(!state.tpwMap) state.tpwMap = [0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0]
    if(!state.dpwMap) state.dpwMap = [0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0]
    if(state.setMoisture) state.setMoisture = null
    if(!state.seasonAdj) state.seasonAdj = 0
    if(!state.weekseasonAdj) state.weekseasonAdj = 0    
    setSeason()	    
}

//get day of week integer
def getWeekDay(day)
{
	def weekdays = ['Monday', 'Tuesday', 'Wednesday', 'Thursday', 'Friday', 'Saturday', 'Sunday']
	def mapDay = [Monday:1, Tuesday:2, Wednesday:3, Thursday:4, Friday:5, Saturday:6, Sunday:7]  
	if(day && weekdays.contains(day)) {
    	return mapDay.get(day)
    }
	def today = new Date().format("EEEE", location.timeZone)
	return mapDay.get(today)
}

// Get string of run days from dpwMap
def getRunDays(day1,day2,day3,day4,day5,day6,day7)
{
    def str = ""
    if(day1)
    	str += "M"
    if(day2)
    	str += "T"
    if(day3)
    	str += "W"
    if(day4)
    	str += "Th"
    if(day5)
    	str += "F"
    if(day6)
    	str += "Sa"
    if(day7)
    	str += "Su"
    if(string == "")
    	str = "0 Days/week"
    return str
}

int getRandomNumber(int num){
    Random random = new Random()
    return random.nextInt(30 ** num) + 20
}

//start manual schedule
def manualStart(evt){
	if (enableManual && state.run == false){
        def runNowMap = []
        runNowMap = cycleLoop(0)    
        if (runNowMap)
        { 
			subscribe switches, "switch.off", cycleOff
			switches.programWait()
            state.run = true        
            runNowMap = "${app.label} manually started, watering in 1 minute:\n" + runNowMap
            note('active', "${runNowMap}", 'd')                      
            runIn(60, cycleOn)   //start water program
        }

        else {
            switches.programOff()
            state.run = false        
            note('skipping', 'Check schedule setup', 'a')
        }
    }
}

//is another schedule running
def busy(){
	// Check if we are already running (somebody changed the schedule time while this schedule is running)
    
    if (state.run == true){
    	note("active", "${app.label} already running, skipping additional start", "d")
    	return true
    }
    
    // dCheck that the controller isn't running some other schedule
    def switchVal = switches.currentValue('switch')
	//def switchStat = switches.currentValue('status')
	// log.debug "${switches.displayName} current value: ${switchVal}, status ${switchStat}"

    if (switchVal.contains('off')){// && !switchStat.contains('active') && !switchStat.contains('season') && !switchStat.contains('pause') && !switchStat.contains('moisture')) {
    	return false	// nope - the controller isn't busy
    }    

    // Something else is running, but are we even supposed to run today?
    if (isDay()) {						
    	subscribe switches, "switch.off", busyOff      
    	note('active', "Another schedule running, waiting to start ${app.label}", 'i')
       	return true

    }
    
    // Don't need to do busyOff if not scheduled for today (but I don't think this will ever happen)
    log.debug "Another schedule running, but ${app.label} is not scheduled for today anyway."
    return true
}

def busyOff(evt){
	unsubscribe(switches)    
    runIn(10, preCheck)   
}

//run check every day
def preCheck(){	
	unsubscribe(switches)
    subscribe switches, "switch.off", cycleOff
    if (!isDay()) {
		log.debug "Skipping: ${app.label} is not scheduled for today."		// Silent - no note
		return
	}
	if (!busy()) {    	
        note('active', "${app.label} starting pre-check.", 'd')
		switches.programWait()
	   	state.run = true

       	if (!isWeather()) checkRunMap()
       	else {
           	switches.programOff()
           	state.run = false            
		}
	}
}

//start water program
def cycleOn(){       
    
	if (state.run == true){				//block if manually stopped during precheck which goes to cycleOff
    
        if (sync != null ) {
            def syncSwitch = sync.currentValue('switch')
            def syncStatus = sync.currentValue('status')

            if ( !syncSwitch.contains('off') || syncStatus.contains('active') || syncStatus.contains('pause') || syncStatus.contains('season') ) {
                subscribe sync, 'switch.off', syncOn
                if (!state.startTime) state.pauseTime = null		// haven't started yet
                note('pause', "Waiting for ${sync} to complete before starting schedule", 'w')
                return
            }
        }

        // master schedule complete (or null), check the control contact

        if (contact == null || !contact.currentValue('contact').contains('open')) {

            // All clear, let's start running!
            subscribe switches, 'switch.off', cycleOff
            subscribe contact, 'contact.open', doorOpen

            String newString = ''
            if (state.totalTime && !state.startTime) {
                state.startTime = new Date()
                def finishTime = new Date(now() + (60000 * state.totalTime).toLong()).format('EEEE @ h:mm a', location.timeZone)
                newString = "\nETC: ${finishTime}"
            }
            note('active', "${app.label} starting" + newString, 'd')
            state.pauseTime = null
            resume()
        }

        else {
            // Ready to run, but the control contact is open, so we wait
            subscribe switches, 'switch.off', cycleOff	// this is weird- does it allow someone to stop the schedule while in pause?
            state.pauseTime = new Date()

            note('pause', "${contact} opened, ${switches} paused watering", 'w')

        }
    }
}


//when switch reports off, watering program is finished
def cycleOff(evt){
	state.run = false 
	if (contact) unsubscribe(contact)
    unsubscribe(switches)    
    if (enableManual) subscribe(switches, 'switch.programOn', manualStart)
    // if the control contact is closed, we are done...else we're waiting for it to close
    if (contact == null || !contact.currentValue('contact').contains('open')){    
    	note('finished', 'Finished watering for today', 'i')
    } else
    	log.debug "${switches} turned off, but ${contact} is open"

}

//run check each day at scheduled time
def checkRunMap(){
	boolean isDebug = true

	if (isDebug) log.debug 'checkRunMap()'
	
    // Create weekly water summary, if requested, on Sunday	
    if(notify && notify.contains('Weekly') && (getWeekDay() == 3))
    {
    	int zone = 1
        String zoneSummary = ''
        while(zone <= 16) {
        	if(settings["zone${zone}"] != null && settings["zone${zone}"] != 'Off' && nozzle(zone) != 4) {
			   zoneSummary += getaZoneSummary(zone)

            }
            zone++
        }
        if (isDebug) log.debug "Weekly water summary: ${zoneSummary}"
        note('season', "Weekly water summary: ${zoneSummary}", 'w' )
    }    
    


    //get & set watering times for today
    def runNowMap = []    
    runNowMap = cycleLoop(1)
    if (isDebug) log.debug "runNowMap: ${runNowMap}"
    
    if (runNowMap)

    { 
        state.run = true
        state.startTime = null
        runIn(60, cycleOn)			// start water
        runNowMap = "${app.label} watering begins in 1 minute,\nTotal runtime: ${state.totalTime} minutes:\n" + runNowMap
        note('active', "${runNowMap}", 'd')
    }
    else {
        state.run = false 
        switches.programOff()                   
        note('skipping', 'No watering today.', 'd')
    }

}


//get todays schedule
def cycleLoop(int i)
{
	log.debug "cycleLoop(${i})"
    int zone = 1
    int dpw = 0
    int tpw = 0
    int cyc = 0

    int rtime = 0
    def timeMap = [:]
    def pumpMap = ""
    def runNowMap = ""

    def soilString = ""
    int totalCycles = 0
    int totalTime = 0

    while(zone <= 16)
    {
    	if (isDebug) log.debug "cycleLoop(): Zone ${zone}"

        rtime = 0

        if( settings."zone${zone}" != null && settings."zone${zone}" != 'Off' && nozzle(zone) != 4 && zoneActive(zone.toString()) )
        {
		  	// First check if we run this zone today, use either dpwMap or even/odd date
		  	dpw = getDPW(zone)          
          	def runToday = 0
          	if (settings["sensor${zone}"] != null) runToday = 1		// run every day if using a sensor on this zone
          	if (i == 0) runToday = 1								// Run if this a manual cycle
          	
          	if (runToday == 0) {									// figure out if we need to run (if we don't already know we do)
	          	if (days && (days.contains('Even') || days.contains('Odd'))) {
            		def daynum = new Date().format('dd', location.timeZone)
            		int dayint = Integer.parseInt(daynum)
        			if(days.contains('Odd') && (dayint +1) % Math.round(31 / (dpw * 4)) == 0) runToday = 1
          			if(days.contains('Even') && dayint % Math.round(31 / (dpw * 4)) == 0) runToday = 1
          		} else {

            		def weekDay = getWeekDay()-1
            		def dpwMap = getDPWDays(dpw)
            		def today = dpwMap[weekDay]
            		if (isDebug) log.debug "Zone: ${zone} dpw: ${dpw} weekDay: ${weekDay} dpwMap: ${dpwMap} today: ${today}"
            		runToday = dpwMap[weekDay]	//1 or 0
          		}
          	}
			
			// OK, we're supposed to run (or at least adjust the sensors)
          	if(runToday) 
          	{
				def soil
            	if (i == 0) soil = moisture(0) 	// manual
            	else soil = moisture(zone)		// moisture check
          		soilString += "${soil[1]}"

				// Run this zone if soil moisture needed 
            	if ( soil[0] == 1 )
            	{

                	cyc = cycles(zone)
                	tpw = getTPW(zone)
                	dpw = getDPW(zone)					// moisture() may have changed DPW

                	rtime = calcRunTime(tpw, dpw)                
                	//daily weather adjust if no sensor
                	if(isSeason && (settings["sensor${zone}"] == null || !learn)) 
                		rtime = Math.round(((rtime / cyc) * (state.seasonAdj.toFloat() / 100.0))+0.5)
                	else 
                		rtime = Math.round((rtime / cyc) + 0.5)                
					totalCycles += cyc
					totalTime += (rtime * cyc)
                	runNowMap += "${settings["name${zone}"]}: ${cyc} x ${rtime} min\n"
                	if (isDebug) log.debug "Zone ${zone} Map: ${cyc} x ${rtime} min - totalTime: ${totalTime}"
            	}
        	}
		}


        if (nozzle(zone) == 4) pumpMap += "${settings["name${zone}"]}: ${settings["zone${zone}"]} on\n"
        timeMap."${zone+1}" = "${rtime}"
        zone++  
    }
	if (soilString) {
        note('moisture', "Moisture Sensors:\n${soilString}",'m')

        }
        
    if (!runNowMap) return runNowMap			// nothing to run today
    
    int pDelay = 0
    //if ("${settings["pumpDelay"]}" != "null") pDelay = "${settings["pumpDelay"]}" as Integer
    if ((settings.pumpDelay != null) && settings.pumpDelay.isNumber()) pDelay = settings.pumpDelay.toInteger()
    
    totalTime += pDelay * totalCycles  // add in the pump startup and inter-zone delays
    state.totalTime = totalTime
    state.startTime = null
    state.pauseTime = null


    
    //send settings to Spruce Controller
    switches.settingsMap(timeMap,4002)

	runIn(30, writeCycles)
    return runNowMap += pumpMap    
}

//send cycle settings
def writeCycles(){
	//log.trace "writeCycles()"
	def cyclesMap = [:]
    //add pumpdelay @ 1
    cyclesMap."1" = pumpDelayString()
    def zone = 1
    def cycle = 0	
    while(zone <= 17)
    {      
        if(nozzle(zone) == 4) cycle = 4
        else cycle = cycles(zone)
        //offset by 1, due to pumpdelay @ 1
        cyclesMap."${zone+1}" = "${cycle}"
        zone++
    }
    switches.settingsMap(cyclesMap, 4001)
    
}

def resume(){
	log.debug 'resume()'
	switches.on()    
}

def syncOn(evt){
    unsubscribe(sync)
    String newString = ''
    if (state.totalTime) {
       	def finishTime = new Date(now() + (30000 + (60000 * state.totalTime)).toLong()).format('EEEE @ h:mm a', location.timeZone) // add in the 30 second delay
       	newString = "\nETC: ${finishTime}"
    }
    note('active', "${sync} complete, starting scheduled program" + newString, 'i')
    runIn(30, cycleOn)
}

def doorOpen(evt){
    note('pause', "${contact} opened, ${switches} paused watering", 'c')
    unsubscribe(switches)
    subscribe contact, 'contact.closed', doorClosed
    switches.off()
    state.pauseTime = new Date()
}
     
def doorClosed(evt){
	String newString = ''
	if (state.pauseTIme && state.startTime) {
		def elapsedTime = (new Date(now() + (60000 * contactDelay).toLong())) - state.pauseTime
    	def finishTime = (state.startTime + state.totalTime + elapsedTime).format('EEEE @ h:mm a', location.timeZone) 
    	state.pauseTime = null
    	newString = "\nNew ETC: ${finishTime}"
	}
    note('active', "${contact} closed, ${switches} will resume watering in ${contactDelay} minute(s)" + newString, 'c')    
    runIn(contactDelay * 60, cycleOn)
}

//Initialize Days per week, based on TPW, perDay and daysAvailable settings
int initDPW(int zone){
	log.debug "initDPW(${zone})"
	if(!state.dpwMap) state.dpwMap = [0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0]
	
	//int zone = i.toInteger()
	int tpw = getTPW(zone)		// was getTPW -does not update times in scheduler without initTPW

	int dpw = 0
	if(tpw > 0) {

        float perDay = 20.0

        if(settings["perDay${zone}"]) perDay = settings["perDay${zone}"].toFloat()
    	dpw = Math.round(tpw.toFloat() / perDay)
    	if(dpw <= 1) dpw = 1
		// 3 days per week not allowed for even or odd day selection
	    if(dpw == 3 && days && (days.contains('Even') || days.contains('Odd')) && !(days.contains('Even') && days.contains('Odd')))
			if((tpw.toFloat() / perDay) < 3.0) dpw = 2

			else dpw = 4
		int daycheck = daysAvailable()
    	if(daycheck < dpw) dpw = daycheck

    }

	state.dpwMap[zone-1] = dpw
    return dpw
}

// Get current days per week value, calls init if not defined
int getDPW(int zone) {


	if(state.dpwMap) return state.dpwMap[zone-1]
	return initDPW(zone)
}

//Initialize Time per Week
int initTPW(int zone) {   

    //log.trace "initTPW(${zone})"
    if (!state.tpwMap) state.tpwMap = [0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0]
    
    if ("${settings["zone${zone}"]}" == null || nozzle(zone) == 0 || nozzle(zone) == 4 || plant(zone) == 0 || !zoneActive(zone.toString()) ) return 0
    
    // apply gain adjustment
    float gainAdjust = 100.0
    if (gain && gain != 0) gainAdjust += gain
    
    // apply seasonal adjustment if enabled and not set to new plants
    float seasonAdjust = 100.0
    if (state.weekseasonAdj && isSeason && settings["plant${zone}"] != "New Plants") seasonAdjust = state.weekseasonAdj    
	
    //int zone = i.toInteger()
	int tpw = 0
	
	// Use learned, previous tpw if it is available
	if ( settings["sensor${zone}"] != null ) {
		seasonAdjust = 100.0 			// no seasonAdjust if this zone uses a sensor
		if(state.tpwMap && learn) tpw = state.tpwMap[zone-1]
	}
	
	// set user-specified minimum time with seasonal adjust
	int minWeek = 0

	if (settings["minWeek${zone}"] != null) minWeek = settings["minWeek${zone}"].toInteger()
    if (minWeek != 0) {
    	tpw = Math.round(minWeek * (seasonAdjust / 100.0))
    } 

    else if ((tpw == null) || (tpw == 0)) { // use calculated tpw
    	tpw = Math.round((plant(zone) * nozzle(zone) * (gainAdjust / 100.0) * (seasonAdjust / 100.0)))
    }

	state.tpwMap[zone-1] = tpw
    log.debug "initTPW(${zone}) tpw: ${tpw}"
    return tpw
}

// Get the current time per week, calls init if not defined
int getTPW(int zone)
{
	// log.debug "getTPW(${zone})"
	if(state.tpwMap) return state.tpwMap[zone-1]
	return initTPW(zone)
}

// Calculate daily run time based on tpw and dpw
int calcRunTime(int tpw, int dpw)
{           
    int duration = 0
    if ((tpw > 0) && (dpw > 0)) duration = Math.round(tpw.toFloat() / dpw.toFloat())

    return duration
}

// Check the moisture level of a zone returning dry (1) or wet (0) and adjust tpw if overly dry/wet
def moisture(int i)
{
	boolean isDebug = true
	if (isDebug) log.debug "moisture(${i})"
	// No Sensor on this zone or manual start skips moisture checking altogether
	if (settings."sensor${i}" == null || i == 0) {     
        return [1,""]
    }

    // Ensure that the sensor has reported within last 48 hours
    def hours = 48
    def yesterday = new Date(now() - (1000 * 60 * 60 * hours).toLong())    
    def lastHumDate = settings["sensor${i}"].latestState('humidity').date
    if (lastHumDate < yesterday) return [1, "Please check ${settings."sensor${i}"}, no humidity reports in the last ${hours} hours\n"]

    float latestHum = settings."sensor${i}".latestValue('humidity').toFloat()	// state = 29, value = 29.13
    int spHum = getDrySp(i)
    if (!learn)
    {
        // no Delay mode, only looks at target moisture level, doesn't try to adjust tpw
		if(latestHum <= spHum.toFloat()) {
           //dry soil
           return [1,"${settings."name${i}"}, Watering: ${settings."sensor${i}"} reads ${latestHum}%, SP is ${spHum}%\n"]              
        } else {
           //wet soil
           return [0,"${settings."name${i}"}, Skipping: ${settings."sensor${i}"} reads ${latestHum}%, SP is ${spHum}%\n"]           
        }
    }

    //in Adaptive mode
    int tpw = getTPW(i)
    int dpw = getDPW(i)
    int cpd = cycles(i)
    float tpwFloat = tpw.toFloat()
	float dpwFloat = dpw.toFloat()
	float cpdFloat = cpd.toFloat()
    if (isDebug) log.debug "moisture(${i}): tpw: ${tpw}, dpw: ${dpw}, cycles: ${cpd} (before adjustment)"
    
    float diffHum = 0.0
    if (latestHum > 0.0) diffHum = (spHum.toFloat() - latestHum) / 100.0
    else {
    	diffHum = 0.02 // Safety valve in case sensor is reporting 0% humidity (e.g., somebody pulled it out of the ground or flower pot)
    	note("warning", "Please check sensor ${settings."sensor${i}"}, it is currently reading 0%", "a")
    }
	
	int minimum = cpd * dpw				// minimum of 1 minute per scheduled days per week (note - can be 1*1=1)
	if (minimum == 0) minimum = 7		// shouldn't happen - safety check
	int tpwAdjust = 0
	
    if (diffHum > 0.01) { 				// only adjust tpw if more than 1% of target SP
  		tpwAdjust = Math.round((tpwFloat * diffHum) + 0.5) * dpw * cpd	// Compute adjustment as a function of the current tpw
  		if (tpwAdjust > (tpwFloat * 0.5)) tpwAdjust = Math.round((tpwFloat * 0.5) + 0.5) 		// limit fast rise to 50% of tpw per day
		if (tpwAdjust < minimum) tpwAdjust = minimum      // but we need to move at least 1 minute per cycle per day to actually increase the watering time
    } else if (diffHum < -0.01) {
    	if (diffHum < -0.05) diffHum = -0.05			// try not to over-compensate for a heavy rainstorm...
    	tpwAdjust = Math.round((tpwFloat * diffHum) - 0.5) * dpwFloat * cpdFloat
    	if (tpwAdjust < (tpwFloat * -0.20)) tpwAdjust = Math.round((tpwFloat * -0.20) - 0.5)	// limit slow decay to 20% of tpw per day
		if (tpwAdjust > (-1 * minimum)) tpwAdjust = -1 * minimum // but we need to move at least 1 minute per cycle per day to actually increase the watering time
    }
    if (isDebug) log.debug "moisture(${i}): diffHum: ${diffHum}, tpwAdjust: ${tpwAdjust}"
    
    String moistureSum = ''
 
    int newTPW = Math.round(tpw + tpwAdjust)
    if (tpwAdjust > 0) {		// need more water

		int perDay = 20
        if (settings."perDay${i}") perDay = settings."perDay${i}".toInteger()
        if (perDay < 8) perDay = 8
  		if (newTPW < perDay) {
  			newTPW = perDay	// arbitrary minimum if we're adjusting up from a small number
  		} else {
    		int maxTPW = dpw * 120					// arbitrary maximum of 2 hours per scheduled watering day per week
    		if (newTPW > maxTPW) newTPW = maxTPW	// initDPW() below may spread this across more days		
    		if (newTPW >= (maxTPW/2)) note("warning", "Please check ${settings["sensor${i}"]}, Zone ${i} time per week seems high: ${newTPW} mins/week","w")

  		}
    	state.tpwMap[i-1] = newTPW
        state.dpwMap[i-1] = initDPW(i)				// call initDPW not getDPW because it may need to recalculate days per week
    	moistureSum = "${settings."name${i}"}, Watering: ${settings."sensor${i}"} reads ${latestHum}%, SP is ${spHum}%, time adjusted by ${tpwAdjust} mins to ${newTPW} mins/week\n"
    	return [1, moistureSum]
    }
    // else, if we are currently above the humidity SP
    else if (tpwAdjust < 0) { 	// New: No longer water if sensor humidity is above SP
    	// Find the minimum tpw
		int minLimit = 0
		if (settings."minWeek${i}" != null) {		// if minWeek != 0, then use that as the minimum limiter
    		if (settings."minWeek${i}" != 0) minLimit = settings."minWeek${i}".toInteger()
		}
		if (minLimit > 0) {
			if (newTPW < minLimit) newTPW = minLimit
		} else if (newTPW < minimum) {
			newTPW = minimum
    		note("warning", "Please check ${settings."sensor${i}"}, ${settings."name${i}"} time per week is very low: ${newTPW} mins/week",'a')
		}
        if (state.tpwMap[i-1] != newTPW) {	// are we changing the tpw?
        	state.tpwMap[i-1] = newTPW		// store the new tpw
        	state.dpwMap[i-1] = initDPW(i)	// may need to recalculate days per week
        	def adjusted = newTPW - tpw // so that the next note is accurate
    		moistureSum = "${settings."name${i}"}, Skipping: settings."sensor${i}"} reads ${latestHum}% SP is ${spHum}%, adjusted by ${adjusted} mins to ${newTPW} mins/week\n"
        } else {							// not changing tpw
        	moistureSum = "${settings."name${i}"}, Skipping: ${settings."sensor${i}"} reads ${latestHum}%, SP is ${spHum}% (no adjustment, ${tpw} mins/week)\n"
    	}
    	return [0, moistureSum]
    } else if (diffHum >= 0.0) {		// assert tpwAdjust == 0 
        moistureSum = "${settings."name${i}"}, Watering: ${settings."sensor${i}"} reads ${latestHum}%, SP is ${spHum}% (no adjustment, ${tpw} mins/week)\n"
        return [1, moistureSum]
    } else { 							// assert diffUm < 0.0 - never water if current sensor > SP
    	moistureSum = "${settings."name${i}"}, Skipping: ${settings."sensor${i}"} reads ${latestHum}%, SP is ${spHum}% (no adjustment, ${tpw} mins/week)\n"
    	return [0, moistureSum]
    }
    return [0, moistureSum]
}  


//get moisture SP
int getDrySp(int i){

    if ("${settings["sensorSp${i}"]}" != "null") 
    	return ("${settings["sensorSp${i}"]}").toInteger() 
    	
    if (settings["plant${i}"] == "New Plants") 
    	return 40    
    


    switch (settings["option${i}"]) {
        case "Sand":
            return 22
        case "Clay":
            return 38  
        default:
            return 28

    }
}

//notifications to device, pushed if requested
def note(String status, String message, String type){
	log.debug status+': '+ message
    switches.notify(status, message)
    if(notify)
    {
      if (notify.contains('Daily') && type == 'd'){
        send(message)
      }
      if (notify.contains('Weekly') && type == 'w'){     
        send(message)
      }
      if (notify.contains('Delays') && type == 'c'){     
        send(message)
      }
      if (notify.contains('Events') && type == 'i'){        
        send(message)
      }
      if (notify.contains('Weather') && type == 'f'){     
        send(message)
      }
      if (notify.contains('Warnings') && type == 'a'){     
        send(message)
      }
      if (notify.contains('Moisture') && type == 'm'){        
        send(message)
	  }
    }
}

def send(msg) {
	if (location.contactBookEnabled && recipients) {

		sendNotificationToContacts(msg, recipients, [event: true]) 
    }
    else {

		sendPush( msg )
      }
}

//days available
def daysAvailable(){
    int dayCount = 0
	if("${settings["days"]}" == "null") dayCount = 7

    else if(days){    
	    if (days.contains('Even') || days.contains('Odd')) {
          dayCount = 4
          if(days.contains('Even') && days.contains('Odd')) dayCount = 7
        } else {
        	if (days.contains('Monday')) dayCount += 1
        	if (days.contains('Tuesday')) dayCount += 1
        	if (days.contains('Wednesday')) dayCount += 1
        	if (days.contains('Thursday')) dayCount += 1
        	if (days.contains('Friday')) dayCount += 1
        	if (days.contains('Saturday')) dayCount += 1
        	if (days.contains('Sunday')) dayCount += 1
           }
       }
    log.debug "daysAvailable -> ${dayCount}, days= ${days}"
    return dayCount
}    
 
//zone: ['Off', 'Spray', 'rotor', 'Drip', 'Master Valve', 'Pump']
def nozzle(i){
    def getT = settings["zone${i}"]    
    if (!getT) return 0
    switch(getT) {        
        case 'Spray':
            return 1
        case 'Rotor':
            return 1.4
        case 'Drip':
            return 2.4
        case 'Master Valve':
            return 4
        case 'Pump':
            return 4
        default:
            return 0
    }
}
 
//plant: ['Lawn', 'Garden', 'Flowers', 'Shrubs', 'Trees', 'Xeriscape', 'New Plants']
def plant(i){
    def getP = settings["plant${i}"]    
    if(!getP) return 0
    switch(getP) {
        case 'Lawn':
            return 60
        case 'Garden':
            return 50
        case 'Flowers':
            return 40
        case 'Shrubs':
            return 30
        case 'Trees':
            return 20
        case 'Xeriscape':
            return 30
        case 'New Plants':
            return 80
        default:
            return 0
    }
}
 
//option: ['Slope', 'Sand', 'Clay', 'No Cycle', 'Cycle 2x', 'Cycle 3x']
def cycles(i){  
    def getC = settings["option${i}"]    
    if(!getC) return 2
    switch(getC) {
        case 'Slope':
            return 3
        case 'Sand':
            return 1
        case 'Clay':
            return 2
        case 'No Cycle':
            return 1
        case 'Cycle 2x':
            return 2
        case 'Cycle 3x':
            return 3   
        default:
            return 2
    }    
}
 
//check if day is allowed
boolean isDay() {    
    // log.debug "day check"
    if ("${settings["days"]}" == "null") return true
     
    def daynow = new Date()
    def today = daynow.format('EEEE', location.timeZone)    
    def daynum = daynow.format("dd", location.timeZone)
    int dayint = Integer.parseInt(daynum)
         
    log.debug "today: ${today} ${dayint}, days: ${days}"
     
    if (days.contains(today)) return true
    if (days.contains('Even') && (dayint % 2 == 0)) return true
    if (days.contains('Odd') && (dayint % 2 != 0)) return true

    return false      
}

//set season adjustment & remove season adjustment
def setSeason() {
    //log.trace "setSeason()"
    
    int zone = 1
    while(zone <= 16) {    		
    	if ( !learn || (settings["sensor${zone}"] == null) || state.tpwMap[zone-1] == 0) {
            //state.tpwMap.putAt(zone-1, 0) //don't need with ln 1186 modifications
            int tpw = initTPW(zone)
            int dpw = initDPW(zone)
            // No longer need to store these here - handled in initTPW/initDPW
            //state.tpwMap[zone-1] = tpw
    		//state.dpwMap[zone-1] = initDPW(zone)
    		if ((tpw != 0) && (state.weekseasonAdj != 0)) {
            	log.debug "Zone ${zone}: seasonally adjusted by ${state.weekseasonAdj-100}% to ${tpw}"
    		}
    	}
        zone++
    }       
}

//capture today's total rainfall - scheduled for just before midnight each day
def getRainToday() {
	def wzipcode = zipString()   
    Map wdata = getWeatherFeature('conditions', wzipcode)
    if (wdata == null) {
    	log.debug "getRainTotal ${zipString()} error: wdata is null"
    	note("warning", "Check Zipcode setting, error: null" , "w")
    } else {
    	log.debug wdata.response
		if (wdata.response.containsKey('error')) {
   			note("warning", "Check Zipcode setting, error:\n${wdata.response.error.type}: ${wdata.response.error.description}" , "w")
		} else {
			float TRain = 0.0
			if (wdata.current_observation.precip_today_in.isNumber()) { // WU can return "t" for "Trace" - we'll assume that means 0.0
            	TRain = wdata.current_observation.precip_today_in.toFloat()
				if (TRain > 25.0) TRain = 25.0
                log.debug "getRainToday: ${wdata.current_observation.precip_today_in} / ${TRain}"
            }
    		def day = getWeekDay()						// what day is it today?
            if (day == 7) day = 0						// adjust: state.Rain order is Su,Mo,Tu,We,Th,Fr,Sa
    		state.Rain.putAt(day, TRain as Float)		// store today's total rainfall
		}
    }
}

//check weather
boolean isWeather(){
	boolean isDebug = true
	if (isDebug) log.debug 'isWeather()'
	
	if (!isRain && !isSeason) return false		// no need to do any of this	
	
    String wzipcode = zipString()   
   	if (isDebug) log.debug "isWeather(): ${wzipcode}"   



    Map wdata = getWeatherFeature('forecast/conditions/geolookup/astronomy', wzipcode)
    if (wdata != null) {
    	if (isDebug) log.debug wdata.response
		if (wdata.response.containsKey('error')) {
        	if (wdata.response.error.type != "invalidfeature") {
    			note('warning', "Check Zipcode setting, error:\n${wdata.response.error.type}: ${wdata.response.error.description}" , 'w')
        		return false
            } else {
            	// Will find out which one(s) weren't reported later (probably never happens now that we don't ask for history)
            	log.debug 'Rate limited...one or more WU features unavailable at this time.'
            }
		}
    } else {
    	if (isDebug) log.debug 'wdata is null'
    	note('warning', 'Check Zipcode setting, error: null' , 'w')
    	return false
    }
    
    // OK, we have good data, let's start the analysis
    float qpfTodayIn = 0.0
    float qpfTomIn = 0.0
    float popToday = 50.0
    float popTom = 50.0
    float TRain = 0.0
    float YRain = 0.0
    float weeklyRain = 0.0
    

    if (isRain) {
    	if (isDebug) log.debug 'isWeather(): isRain'
    	
    	// Get forecasted rain for today and tomorrow

		if (!wdata.response.features.containsKey('forecast') || (wdata.response.features.forecast.toInteger() != 1) || (wdata.forecast == null)) {
    		log.debug 'isWeather(): Unable to get weather forecast.'

    		return false
    	}

    	if (wdata.forecast.simpleforecast.forecastday[0].qpf_allday.in.isNumber()) qpfTodayIn = wdata.forecast.simpleforecast.forecastday[0].qpf_allday.in.toFloat()
		if (wdata.forecast.simpleforecast.forecastday[0].pop.isNumber()) popToday = wdata.forecast.simpleforecast.forecastday[0].pop.toFloat()
    	if (wdata.forecast.simpleforecast.forecastday[1].qpf_allday.in.isNumber()) qpfTomIn = wdata.forecast.simpleforecast.forecastday[1].qpf_allday.in.toFloat()

		if (wdata.forecast.simpleforecast.forecastday[1].pop.isNumber()) popTom = wdata.forecast.simpleforecast.forecastday[1].pop.toFloat()
		
    	// Get rainfall so far today

		if (!wdata.response.features.containsKey('conditions') || (wdata.response.features.conditions.toInteger() != 1) || (wdata.current_observation == null)) {
    		log.debug 'isWeather(): Unable to get current weather conditions.'

    		return false
    	}

		if (wdata.current_observation.precip_today_in.isNumber()) {
       		TRain = wdata.current_observation.precip_today_in.toFloat()

    	}
    	if (TRain > qpfTodayIn) qpfTodayIn = TRain	// already have more rain than forecast for today

    	// Get yesterday's rainfall

    	int day = getWeekDay()
    	YRain = state.Rain[day - 1]
    

    	if (isDebug) log.debug "TRain ${TRain} qpfTodayIn ${qpfTodayIn} @ ${popToday}%, YRain ${YRain}"

    
    	int i = 0
		while (i <= 6){
    		int factor = 0
        	if ((day - i) > 0) factor = day - i else factor =  day + 7 - i

        	float getrain = state.Rain[i]
    		if (factor != 0) weeklyRain += (getrain / factor)
    		i++
    	}

    	if (isDebug) log.debug "isWeather(): weeklyRain ${weeklyRain}"

    }
     
    if (isDebug) log.debug 'isWeather(): build report'      

    String city = wzipcode
	if (wdata.response.features.containsKey('geolookup') && (wdata.response.features.geolookup.toInteger() == 1) && (wdata.location != null)) {
    	city = wdata.location.city
    }

    
    if (isDebug) log.debug 'isWeather(): get highs'
    //get highs

   	int highToday = 0
   	int highTom = 0
   	if (wdata.forecast.simpleforecast.forecastday[0].high.fahrenheit.isNumber()) highToday = wdata.forecast.simpleforecast.forecastday[0].high.fahrenheit.toInteger()

   	if (wdata.forecast.simpleforecast.forecastday[1].high.fahrenheit.isNumber()) highTom = wdata.forecast.simpleforecast.forecastday[1].high.fahrenheit.toInteger()
   	
    def weatherString = "${city} weather\n Today: ${highToday}F"
    if (isRain) weatherString += ",  ${qpfTodayIn}in rain (${Math.round(popToday)}%)"
    weatherString += "\n Tomorrow: ${highTom}F"
    if (isRain) weatherString += ",  ${qpfTomIn}in rain (${Math.round(popTom)}%)\n Yesterday: ${YRain}in rain"
    
    if (isSeason)
    {   
    	//seasonal q factor
    	float qFact = 0.7
    	
		if (!isRain) { // we need to verify we have good data first if we didn't do it above
			if (!wdata.response.features.containsKey('forecast') || (wdata.response.features.forecast.toInteger() != 1) || (wdata.forecast == null)) {
    			log.debug "Unable to get weather forecast."
    			return false
    		}
		}
		
		// is the temp going up or down for the next few days?
    	float totalHigh = 0.0
    	int j = 0
    	int highs = 0
    	while (j < 4) {	// get forecasted high for today and next 3 days

    		if (wdata.forecast.simpleforecast.forecastday[j].high.fahrenheit.isNumber()) {
    			totalHigh += wdata.forecast.simpleforecast.forecastday[j].high.fahrenheit.toFloat()
    			highs++
    		}
    		j++
    	}
    	float avgHigh = highToday
    	if ( highs > 0 ) avgHigh = totalHigh / highs
    	
        //daily adjust
        state.seasonAdj = Math.round((highToday / avgHigh) * 100.0)        
        weatherString += "\n Adjusted ${state.seasonAdj - 100}% for today"
        
        // Apply seasonal adjustment on Monday each week or at install
        if ((getWeekDay() == 1) || (state.weekseasonAdj == 0)) {
            
            //get humidity
            //def gethum = sdata.forecast.simpleforecast.forecastday.avehumidity
            //def humWeek = Math.round((gethum.get(0).toInteger() + gethum.get(1).toInteger() + gethum.get(2).toInteger() + gethum.get(3).toInteger() + gethum.get(4).toInteger())/5)    

            //get daylight
 			if (wdata.response.features.containsKey('astronomy') && (wdata.response.features.astronomy.toInteger() == 1) && (wdata.moon_phase != null)) {
            	int getsunRH = 0
            	int getsunRM = 0
            	int getsunSH = 0
            	int getsunSM = 0
            	
            	if (wdata.moon_phase.sunrise.hour.isNumber()) getsunRH = wdata.moon_phase.sunrise.hour.toInteger()

        		if (wdata.moon_phase.sunrise.minute.isNumber()) getsunRM = wdata.moon_phase.sunrise.minute.toInteger()

            	if (wdata.moon_phase.sunset.hour.isNumber()) getsunSH = wdata.moon_phase.sunset.hour.toInteger()

            	if (wdata.moon_phase.sunset.minute.isNumber()) getsunSM = wdata.moon_phase.sunset.minute.toInteger()
            	
            	int daylight = ((getsunSH * 60) + getsunSM)-((getsunRH * 60) + getsunRM)
				if (daylight >= 850) daylight = 850
            
            	//set seasonal adjustment
            	//state.weekseasonAdj = Math.round((daylight/700 * avgHigh/75) * ((1-(humWeek/100)) * avgHigh/75)*100)
            	state.weekseasonAdj = Math.round((daylight/700.0) * (avgHigh/70.0) * (qFact * 100))

            	//apply seasonal time adjustment
            	weatherString += "\n Applying seasonal adjustment of ${state.weekseasonAdj-100}% this week"            
            	setSeason()
            } else {

            	log.debug 'isWeather(): Unable to get sunrise/set info for today.'
            }
        }
    }
    note('season', weatherString , 'f')

	// if only doing seasonal adjustments, we are done


    if (!isRain) return false	
    
    float setrainDelay = 0.2
    if (rainDelay) setrainDelay = rainDelay.toFloat()


	// if we have no sensors, rain causes us to skip watering for the day    
    if (!anySensors()) { 						
    	if (switches.latestValue("rainsensor") == "rainsensoron"){
        	note('raintoday', "${app.label} is skipping, rain sensor is on", 'd')        
        	return true
       	}
       	float popRain = qpfTodayIn * (popToday / 100.0)
    	if (popRain > setrainDelay){
    		String rainStr = String.format("%.2f", popRain)
        	note('raintoday', "${app.label} is skipping, ${rainStr}in of rain is probable today.", 'd')        
        	return true
    	}
    	popRain = qpfTom * (popTom / 100.0)
    	if (popRain > setrainDelay){
    		String rainStr = String.format("%.2f", popRain)
        	note('raintom', "${app.label} is skipping, ${rainStr}in of rain is probable tomorrow.", 'd')
        	return true
    	}
	    if (weeklyRain > setrainDelay){
	    	String rainStr = String.format("%.2f", weeklyRain)
    	    note('rainy', "${app.label} is skipping, ${rainStr}in weighted average rain over the past week.", 'd')
        	return true
    	}
    } else { // we have at least one sensor
    	// Ignore rain sensor & historical rain - only skip if more than setrainDelay is expected before midnight tomorrow
    	float popRain = (qpfTodayIn * (popToday / 100.0)) - TRain	// ignore rain that has already fallen so far today - sensors should already reflect that
    	if (popRain > setrainDelay){
    		String rainStr = String.format("%.2f", popRain)
        	note('rainy', "${app.label} is skipping, at least ${rainStr}in of rain is probable later today.", 'd')        
        	return true
    	}

    	popRain += qpfTomIn * (popTom / 100.0)
    	if (popRain > setrainDelay){
    		String rainStr = String.format("%.2f", popRain)
        	note('rainy', "${app.label} is skipping, at least ${rainStr}in of rain is probable later today + tomorrow.", 'd')        
        	return true
    	}
    }
    return false    
}

// true if ANY of this schedule's zones are using sensors
boolean anySensors() {
	int zone=1
	while (zone <= 16) {
		if (settings["sensor${zone}"] != null) return true
		zone++
	}
	return false
}

def getDPWDays(dpw){
  if(dpw == 1)
     return state.DPWDays1
  if(dpw == 2)
     return state.DPWDays2
  if(dpw == 3)
     return state.DPWDays3
  if(dpw == 4)
     return state.DPWDays4
  if(dpw == 5)
     return state.DPWDays5
  if(dpw == 6)
     return state.DPWDays6
  if(dpw == 7)
     return state.DPWDays7
  return [0,0,0,0,0,0,0]
}

// Create a map of what days each possible DPW value will run on
// Example:  User sets allowed days to Monday Wed and Fri
// Map would look like: DPWDays1:[1,0,0,0,0,0,0] (run on Monday)
//                      DPWDays2:[1,0,0,0,1,0,0] (run on Monday and Friday)
//                      DPWDays3:[1,0,1,0,1,0,0] (run on Monday Wed and Fri)
// Everything runs on the first day possible, starting with Monday.
def createDPWMap() {
	state.DPWDays1 = []
    state.DPWDays2 = []
    state.DPWDays3 = []
    state.DPWDays4 = []
    state.DPWDays5 = []
    state.DPWDays6 = []
    state.DPWDays7 = []
	def NDAYS = 7
    // day Distance[NDAYS][NDAYS], easier to just define than calculate everytime
    def int[][] dayDistance = [[0,1,2,3,3,2,1],[1,0,1,2,3,3,2],[2,1,0,1,2,3,3],[3,2,1,0,1,2,3],[3,3,2,1,0,1,2],[2,3,3,2,1,0,1],[1,2,3,3,2,1,0]]
	def ndaysAvailable = daysAvailable() 
	def i = 0
    def int[] daysAvailable = [0,1,2,3,4,5,6]
    if(days) 
    {
      if (days.contains('Even') || days.contains('Odd')) {
      	return
      }
	  if (days.contains('Monday')) {
    	daysAvailable[i] = 0
        i++
      }
      if (days.contains('Tuesday')) {
    	daysAvailable[i] = 1
        i++
      }
      if (days.contains('Wednesday')) {
    	daysAvailable[i] = 2
        i++
      }
      if (days.contains('Thursday')) {
    	daysAvailable[i] = 3
        i++
      }
      if (days.contains('Friday')) {
    	daysAvailable[i] = 4
        i++
      }
      if (days.contains('Saturday')) {
    	daysAvailable[i] = 5
        i++
      }
      if (days.contains('Sunday')) {
    	daysAvailable[i] = 6
        i++
      }
    
      if(i != ndaysAvailable) {
    	log.debug "ERROR: days and daysAvailable do not match."
        log.debug "${i}  ${ndaysAvailable}"
      }
    }
    //log.debug "Ndays: ${ndaysAvailable} Available Days: ${daysAvailable}"
    def maxday = -1
    def max = -1
    def days = new int[7]
    def int[][] runDays = [[0,0,0,0,0,0,0],[0,0,0,0,0,0,0],[0,0,0,0,0,0,0],[0,0,0,0,0,0,0],[0,0,0,0,0,0,0],[0,0,0,0,0,0,0],[0,0,0,0,0,0,0]]
    for(def a=0; a < ndaysAvailable; a++) {

      // Figure out next day using the dayDistance map, getting the farthest away day (max value)
      if(a > 0 && ndaysAvailable >= 2 && a != ndaysAvailable-1) {
        if(a == 1) {
		  for(def c=1; c < ndaysAvailable; c++) {
	        def d = dayDistance[daysAvailable[0]][daysAvailable[c]]
	  		if(d > max) {
	    	  max = d
	    	  maxday = daysAvailable[c]
	        }
	      }
	      //log.debug "max: ${max}  maxday: ${maxday}"
	      days[0] = maxday
        }
 
        // Find successive maxes for the following days
        if(a > 1) {
	      def lmax = max
          def lmaxday = maxday
	      max = -1
	      for(def c = 1; c < ndaysAvailable; c++) {
	        def d = dayDistance[daysAvailable[0]][daysAvailable[c]]
            def t = d > max
            if(a % 2 == 0)
            	t = d >= max
	        if(d < lmax && d >= max) {
              if(d == max) {
              	d = dayDistance[lmaxday][daysAvailable[c]]
                if(d > dayDistance[lmaxday][maxday]) {
	              max = d
	              maxday = daysAvailable[c]
                }
              } else {
	            max = d
	            maxday = daysAvailable[c]
              }
	        }
	      }
          lmax = 5
	      while(max == -1) {
            lmax = lmax -1
	        for(def c = 1; c < ndaysAvailable; c++) {
	          def d = dayDistance[daysAvailable[0]][daysAvailable[c]]
	          if(d < lmax && d >= max) {
                if(d == max) {
              	  d = dayDistance[lmaxday][daysAvailable[c]]
                  if(d > dayDistance[lmaxday][maxday]) {
	                max = d
	                maxday = daysAvailable[c]
                  }
                } else {
	              max = d
	              maxday = daysAvailable[c]
                }
	          }
	        }
            for(def d=0; d< a-2; d++)
              if(maxday == days[d])
                max = -1
	      }
          //log.debug"max: ${max}  maxday: ${maxday}"
	      days[a-1] = maxday
        }
      }
      
      // Set the runDays map using the calculated maxdays
      for(def b=0; b < 7; b++) 
      {
        // Runs every day available
        if(a == ndaysAvailable-1) {
	      runDays[a][b] = 0
	      for(def c=0; c < ndaysAvailable; c++)
	        if(b == daysAvailable[c])
	          runDays[a][b] = 1

        } else
	      // runs weekly, use first available day
	      if(a == 0)
	        if(b == daysAvailable[0])
	          runDays[a][b] = 1
	        else
	          runDays[a][b] = 0
	      else {
	        // Otherwise, start with first available day
	        if(b == daysAvailable[0])
	          runDays[a][b] = 1
	        else {
	          runDays[a][b] = 0
	          for(def c=0; c < a; c++)
	          if(b == days[c])
		        runDays[a][b] = 1
	        }
	      }
      }
    }
  
  	//log.debug "DPW: ${runDays}"
    state.DPWDays1 = runDays[0]
    state.DPWDays2 = runDays[1]
    state.DPWDays3 = runDays[2]
    state.DPWDays4 = runDays[3]
    state.DPWDays5 = runDays[4]
    state.DPWDays6 = runDays[5]
    state.DPWDays7 = runDays[6]
}

//transition page to populate app state - this is a fix for WP param
def zoneSetPage1(){
	state.app = 1
    zoneSetPage()
    }
def zoneSetPage2(){
	state.app = 2
    zoneSetPage()
    }
def zoneSetPage3(){
	state.app = 3
    zoneSetPage()
    }
def zoneSetPage4(){
	state.app = 4
    zoneSetPage()
    }
def zoneSetPage5(){
	state.app = 5
    zoneSetPage()
    }
def zoneSetPage6(){
	state.app = 6
    zoneSetPage()
    }
def zoneSetPage7(){
	state.app = 7
    zoneSetPage()
    }
def zoneSetPage8(){
	state.app = 8
    zoneSetPage()
    }
def zoneSetPage9(i){
	state.app = 9
    zoneSetPage()
    }
def zoneSetPage10(){
	state.app = 10
    zoneSetPage()
    }
def zoneSetPage11(){
	state.app = 11
    zoneSetPage()
    }
def zoneSetPage12(){
	state.app = 12
    zoneSetPage()
    }
def zoneSetPage13(){
	state.app = 13
    zoneSetPage()
    }
def zoneSetPage14(){
	state.app = 14
    zoneSetPage()
    }
def zoneSetPage15(){
	state.app = 15
    zoneSetPage()
    }
def zoneSetPage16(){
	state.app = 16
    zoneSetPage()
    }


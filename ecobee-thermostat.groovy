/**
 *  Based on original version Copyright 2015 SmartThings
 *  Additions Copyright 2016 Sean Kendall Schneyer
 *  Additions Copyright 2017 Barry A. Burke
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
 *	Ecobee Thermostat
 *
 *	Author: SmartThings
 *	Date: 2013-06-13
 *
 * 	Updates by Sean Kendall Schneyer <smartthings@linuxbox.org>
 * 	Date: 2015-12-23
 *
 *	Updates by Barry A. Burke (storageanarchy@gmail.com)
 *  https://github.com/SANdood/Ecobee
 *
 *  See Changelog for change history 
 *
 * 	0.9.12 - Fix for setting custom Thermostat Programs (Comfort Settings)
 *	0.9.13 - Add attributes to indicate custom program names to child thermostats (smart1, smart2, etc)
 * 	0.10.1 - Massive overhaul for performance, efficiency, improved UI, enhanced functionality
 *	0.10.2 - Beta release of Barry's updated version
 *	0.10.3 - Added support for setVacationFanMinOnTime() and deleteVacation()
 *	0.10.4 - Fixed temperatureDisplay
 *	0.10.5 - Tuned up device notifications (icons, colors, etc.)
 *
 */

def getVersionNum() { return "0.10.5" }
private def getVersionLabel() { return "Ecobee Thermostat Version ${getVersionNum()}" }

 
metadata {
	definition (name: "Ecobee Thermostat", namespace: "smartthings", author: "SmartThings") {
		capability "Actuator"
		capability "Thermostat"
        capability "Sensor"
		capability "Refresh"
		capability "Relative Humidity Measurement"
		capability "Temperature Measurement"
		// capability "Presence Sensor"
        capability "Motion Sensor"
        
        // Extended Set of Thermostat Capabilities
        capability "Thermostat Cooling Setpoint"
		capability "Thermostat Fan Mode"
		capability "Thermostat Heating Setpoint"
		capability "Thermostat Mode"
		capability "Thermostat Operating State"
		capability "Thermostat Setpoint"
            

		command "setTemperature"
        command "auxHeatOnly"

		command "generateEvent"
		command "raiseSetpoint"
		command "lowerSetpoint"
		command "resumeProgram"
		command "switchMode"
        
        command "setThermostatProgram"
        command "setFanMinOnTime"
        command "setVacationFanMinOnTime"
        command "deleteVacation"
        command "home"
        command "present"

// Unfortunately we cannot overload the internal definition of 'sleep()', and calling this will silently fail (actually, it does a
// "sleep(0)")
//		command "sleep"
        command "asleep"
        command "night"				// this is probably the appropriate SmartThings device command to call, matches ST mode
        command "away"
        
        command "fanOff"  			// Missing from the Thermostat standard capability set
        command "noOp" 				// Workaround for formatting issues 
        command "setStateVariable"

		// Capability "Thermostat"
        attribute "temperatureScale", "string"
		attribute "thermostatSetpoint","number"
		attribute "thermostatStatus","string"
        attribute "apiConnected","string"
        
		attribute "currentProgramName", "string"
        attribute "currentProgramId","string"
		attribute "currentProgram","string"
		attribute "scheduledProgramName", "string"
        attribute "scheduledProgramId","string"
		attribute "scheduledProgram","string"
        attribute "weatherSymbol", "string"        
        attribute "debugEventFromParent","string"
        attribute "logo", "string"
        attribute "timeOfDate", "enum", ["day", "night"]
        attribute "lastPoll", "string"
        
		attribute "equipmentStatus", "string"
        attribute "humiditySetpoint", "string"
        attribute "weatherTemperature", "number"
		attribute "decimalPrecision", "number"
		attribute "temperatureDisplay", "string"
		attribute "equipmentOperatingState", "string"
        attribute "coolMode", "string"
		attribute "heatMode", "string"
        attribute "autoMode", "string"
		attribute "heatStages", "number"
		attribute "coolStages", "number"
		attribute "hasHeatPump", "string"
        attribute "hasForcedAir", "string"
        attribute "hasElectric", "string"
        attribute "hasBoiler", "string"
		attribute "auxHeatMode", "string"
        attribute "motion", "string"
		attribute "heatRangeHigh", "number"
		attribute "heatRangeLow", "number"
		attribute "coolRangeHigh", "number"
		attribute "coolRangeLow", "number"
		attribute "heatRange", "string"
		attribute "coolRange", "string"
		attribute "thermostatHold", "string"
//        attribute "holdEndsAt", "string"
		attribute "holdStatus", "string"
        attribute "heatDifferential", "number"
        attribute "coolDifferential", "number"
        attribute "fanMinOnTime", "number"
		
		// attribute "debugLevel", "number"
		
        attribute "smart1", "string"
        attribute "smart2", "string"
        attribute "smart3", "string"
        attribute "smart4", "string"
        attribute "smart5", "string"
        attribute "smart6", "string"
        attribute "smart7", "string"
        attribute "smart8", "string"
        attribute "smart9", "string"
        attribute "smart10", "string"
	}

	simulator { }

    tiles(scale: 2) {      
              
		multiAttributeTile(name:"tempSummary", type:"thermostat", width:6, height:4) {
			tileAttribute("device.temperatureDisplay", key: "PRIMARY_CONTROL") {
				attributeState("default", label:'${currentValue}', unit:"dF")
			}

			tileAttribute("device.temperature", key: "VALUE_CONTROL") {
                attributeState("default", action: "setTemperature")
			}
            tileAttribute("device.humidity", key: "SECONDARY_CONTROL") {
				attributeState("default", label:'${currentValue}%', unit:"%")
			}

			tileAttribute("device.thermostatOperatingState", key: "OPERATING_STATE") {
				attributeState("idle", backgroundColor:"#d28de0")			// ecobee purple/magenta
                attributeState("fan only", backgroundColor:"66cc00")		// ecobee green
				attributeState("heating", backgroundColor:"#ff9c14")		// ecobee snowflake blue
				attributeState("cooling", backgroundColor:"#2db9e7")		// ecobee flame orange
			}
			tileAttribute("device.thermostatMode", key: "THERMOSTAT_MODE") {
				attributeState("off", label:'${name}')
				attributeState("heat", label:'${name}')
				attributeState("cool", label:'${name}')
                attributeState("auto", label:'${name}')
			}
            tileAttribute("device.heatingSetpoint", key: "HEATING_SETPOINT") {
            	attributeState("default", label:'${currentValue}°', unit:"dF")
            }
			tileAttribute("device.coolingSetpoint", key: "COOLING_SETPOINT") {
				attributeState("default", label:'${currentValue}°', unit:"dF")
			}

		} // End multiAttributeTile
        

        // Workaround until they fix the Thermostat multiAttributeTile. Only use this one OR the above one, not both
        multiAttributeTile(name:"summary", type: "lighting", width: 6, height: 4) {
        	tileAttribute("device.temperature", key: "PRIMARY_CONTROL") {
				attributeState("temperature", label:'${currentValue}°', unit:"dF",
				backgroundColors: getTempColors())
			}

			tileAttribute("device.temperature", key: "VALUE_CONTROL") {
                attributeState("default", action: "setTemperature")
			}

            tileAttribute("device.humidity", key: "SECONDARY_CONTROL") {
				attributeState("default", label:'${currentValue}%', unit:"%")
			}

			tileAttribute("device.thermostatOperatingState", key: "OPERATING_STATE") {
				attributeState("idle", backgroundColor:"#d28de0")			// ecobee purple/magenta
                attributeState("fan only", backgroundColor:"#66cc00")		// ecobee green
				attributeState("heating", backgroundColor:"#ff9c14")		// ecobee snowflake blue
				attributeState("cooling", backgroundColor:"#2db9e7")		// ecobee flame orange
			}

			tileAttribute("device.thermostatMode", key: "THERMOSTAT_MODE") {
				attributeState("off", label:'${name}')
				attributeState("heat", label:'${name}')
				attributeState("cool", label:'${name}')
                attributeState("auto", label:'${name}')
			}
            tileAttribute("device.heatingSetpoint", key: "HEATING_SETPOINT") {
            	attributeState("default", label:'${currentValue}°', unit:"dF")
            }
			tileAttribute("device.coolingSetpoint", key: "COOLING_SETPOINT") {
				attributeState("default", label:'${currentValue}°', unit:"dF")
			}
        }

        // Show status of the API Connection for the Thermostat
		standardTile("apiStatus", "device.apiConnected", width: 1, height: 1) {
        	state "full", label: "API", backgroundColor: "#44b621", icon: "st.contact.contact.closed"
            state "warn", label: "API ", backgroundColor: "#FFFF33", icon: "st.contact.contact.open"
            state "lost", label: "API ", backgroundColor: "#ffa81e", icon: "st.contact.contact.open"
		}

		valueTile("temperature", "device.temperature", width: 2, height: 2, canChangeIcon: false, icon: "st.Home.home1") {
			state("temperature", label:'${currentValue}°', unit:"F", backgroundColors: getTempColors())
		}
        
        // these are here just to get the colored icons to diplay in the Recently log in the Mobile App
        valueTile("heatingSetpoint", "device.heatingSetpoint", width: 2, height: 2, canChangeIcon: false, icon: "st.Home.home1") {
			state("heatingSetpoint", label:'${currentValue}°', unit:"F", backgroundColor:"#ff9c14")
		}
        valueTile("coolingSetpoint", "device.coolingSetpoint", width: 2, height: 2, canChangeIcon: false, icon: "st.Home.home1") {
			state("coolingSetpoint", label:'${currentValue}°', unit:"F", backgroundColor:"#2db9e7")
		}
        valueTile("thermostatSetpoint", "device.thermostatSetpoint", width: 2, height: 2, canChangeIcon: false, icon: "st.Home.home1") {
			state("thermostatSetpoint", label:'${currentValue}°', unit:"F",	backgroundColors: getTempColors())
		}
        valueTile("weatherTemp", "device.weatherTemperature", width: 2, height: 2, canChangeIcon: false, icon: "st.Home.home1") {
			state("weatherTemperature", label:'${currentValue}°', unit:"F",	backgroundColors: getTempColors())
		}
		
		standardTile("mode", "device.thermostatMode", width: 2, height: 2, inactiveLabel: false, decoration: "flat") {
			state "off", action:"thermostat.heat", label: "Set Mode", nextState: "updating", icon: "https://raw.githubusercontent.com/StrykerSKS/SmartThings/master/smartapp-icons/ecobee/png/systemmode_off.png"
			state "heat", action:"thermostat.cool",  label: "Set Mode", nextState: "updating", icon: "https://raw.githubusercontent.com/StrykerSKS/SmartThings/master/smartapp-icons/ecobee/png/systemmode_heat.png"
			state "cool", action:"thermostat.auto",  label: "Set Mode", nextState: "updating", icon: "https://raw.githubusercontent.com/StrykerSKS/SmartThings/master/smartapp-icons/ecobee/png/systemmode_cool.png"
			state "auto", action:"thermostat.off",  label: "Set Mode", nextState: "updating", icon: "https://raw.githubusercontent.com/StrykerSKS/SmartThings/master/smartapp-icons/ecobee/png/systemmode_auto.png"
            // Not included in the button loop, but if already in "auxHeatOnly" pressing button will go to "auto"
			state "auxHeatOnly", action:"thermostat.auto", icon: "st.thermostat.emergency-heat"
			state "updating", label:"Working", icon: "st.secondary.secondary"
		}
        
        standardTile("modeShow", "device.thermostatMode", width: 2, height: 2, inactiveLabel: false, decoration: "flat") {
			state "off", action:"noOp", label: "Off", nextState: "off", icon: "https://raw.githubusercontent.com/StrykerSKS/SmartThings/master/smartapp-icons/ecobee/png/systemmode_off.png"
			state "heat", action:"noOp",  label: "Heat", nextState: "heat", icon: "https://raw.githubusercontent.com/StrykerSKS/SmartThings/master/smartapp-icons/ecobee/png/systemmode_heat.png"
			state "cool", action:"noOp",  label: "Cool", nextState: "cool", icon: "https://raw.githubusercontent.com/StrykerSKS/SmartThings/master/smartapp-icons/ecobee/png/systemmode_cool.png"
			state "auto", action:"noOp",  label: "Auto", nextState: "auto", icon: "https://raw.githubusercontent.com/StrykerSKS/SmartThings/master/smartapp-icons/ecobee/png/systemmode_auto.png"
            // Not included in the button loop, but if already in "auxHeatOnly" pressing button will go to "auto"
			state "auxHeatOnly", action:"noOp", icon: "st.thermostat.emergency-heat"
			state "updating", label:"Working", icon: "st.secondary.secondary"
		}
        
        // TODO Use a different color for the one that is active
		standardTile("setModeHeat", "device.thermostatMode", width: 2, height: 2, inactiveLabel: false, decoration: "flat") {			
			state "heat", action:"thermostat.heat",  label: "Heat", nextState: "updating", icon: "https://raw.githubusercontent.com/StrykerSKS/SmartThings/master/smartapp-icons/ecobee/png/systemmode_heat.png"
			state "updating", label:"Working...", icon: "st.secondary.secondary"
		}
		standardTile("setModeCool", "device.thermostatMode", width: 2, height: 2, inactiveLabel: false, decoration: "flat") {			
			state "cool", action:"thermostat.cool",  label: "Cool", nextState: "updating", icon: "https://raw.githubusercontent.com/StrykerSKS/SmartThings/master/smartapp-icons/ecobee/png/systemmode_cool.png"
			state "updating", label:"Working...", icon: "st.secondary.secondary"
		}        
		standardTile("setModeAuto", "device.thermostatMode", width: 2, height: 2, inactiveLabel: false, decoration: "flat") {			
			state "auto", action:"thermostat.auto",  label: "Auto", nextState: "updating", icon: "https://raw.githubusercontent.com/StrykerSKS/SmartThings/master/smartapp-icons/ecobee/png/systemmode_auto.png"
			state "updating", label:"Working...", icon: "st.secondary.secondary"
		}
		standardTile("setModeOff", "device.thermostatMode", width: 2, height: 2, inactiveLabel: false, decoration: "flat") {			
			state "off", action:"thermostat.off", label: "Off", nextState: "updating", icon: "https://raw.githubusercontent.com/StrykerSKS/SmartThings/master/smartapp-icons/ecobee/png/systemmode_off.png"
			state "updating", label:"Working...", icon: "st.secondary.secondary"
		}
        

		standardTile("fanModeLabeled", "device.thermostatFanMode", width: 2, height: 2, inactiveLabel: false, decoration: "flat") {
			state "on", label:'On', action:"noOp", nextState: "on", icon: "https://raw.githubusercontent.com/StrykerSKS/SmartThings/master/smartapp-icons/ecobee/png/systemmode_fan.png"
            state "auto", label:'Auto', action:"noOp", nextState: "auto", icon: "https://raw.githubusercontent.com/StrykerSKS/SmartThings/master/smartapp-icons/ecobee/png/systemmode_fan.png"
            state "off", label:'Off', action:"noOp", nextState: "auto", icon: "https://raw.githubusercontent.com/StrykerSKS/SmartThings/master/smartapp-icons/ecobee/png/systemmode_fan.png"
			state "circulate", label:'Circulate', action:"noOp", nextState: "circulate", icon: "https://raw.githubusercontent.com/StrykerSKS/SmartThings/master/smartapp-icons/ecobee/png/systemmode_fan.png"
            state "updating", label:"Working", icon: "st.secondary.secondary"
		}
        
        standardTile("fanOffButton", "device.thermostatFanMode", width: 2, height: 2, inactiveLabel: false, decoration: "flat") {
			state "off", label:"Fan Off", action:"fanOff", nextState: "updating", icon: "https://raw.githubusercontent.com/StrykerSKS/SmartThings/master/smartapp-icons/ecobee/png/systemmode_fan_big_nolabel.png"
            state "updating", label:"Working", icon: "st.secondary.secondary"
		}

		standardTile("fanCirculate", "device.thermostatFanMode", width: 2, height: 2, inactiveLabel: false, decoration: "flat") {
			state "circulate", label:"Fan Cicrulate", action:"thermostat.fanCirculate", nextState: "updating", icon: "https://raw.githubusercontent.com/StrykerSKS/SmartThings/master/smartapp-icons/ecobee/png/systemmode_fan_big_nolabel.png"
            state "updating", label:"Working", icon: "st.secondary.secondary"
		}
        
		standardTile("fanMode", "device.thermostatFanMode", width: 2, height: 2, inactiveLabel: false, decoration: "flat") {
			state "on", action:"thermostat.fanAuto", nextState: "updating", icon: "https://raw.githubusercontent.com/StrykerSKS/SmartThings/master/smartapp-icons/ecobee/png/systemmode_fan_big_nolabel.png"
            state "auto", action:"thermostat.fanOn", nextState: "updating", icon: "https://raw.githubusercontent.com/StrykerSKS/SmartThings/master/smartapp-icons/ecobee/png/systemmode_fan_big_nolabel.png"
            state "off", action:"thermostat.fanAuto", nextState: "updating", icon: "https://raw.githubusercontent.com/StrykerSKS/SmartThings/master/smartapp-icons/ecobee/png/systemmode_fan_big_nolabel.png"
			state "circulate", action:"thermostat.fanAuto", nextState: "updating", icon: "https://raw.githubusercontent.com/StrykerSKS/SmartThings/master/smartapp-icons/ecobee/png/systemmode_fan_big_nolabel.png"
            state "updating", label:"Working", icon: "st.secondary.secondary"
		}
        standardTile("fanModeAutoSlider", "device.thermostatFanMode", width: 2, height: 2, inactiveLabel: false, decoration: "flat") {
        	state "on", action:"thermostat.fanAuto", nextState: "auto", icon: "https://raw.githubusercontent.com/StrykerSKS/SmartThings/master/smartapp-icons/ecobee/png/fanmode_auto_slider_off.png"
            state "auto", icon: "https://raw.githubusercontent.com/StrykerSKS/SmartThings/master/smartapp-icons/ecobee/png/fanmode_auto_slider_on.png"
        }
		standardTile("fanModeOnSlider", "device.thermostatFanMode", width: 2, height: 2, inactiveLabel: false, decoration: "flat") {
        	state "auto", action:"thermostat.fanOn", nextState: "auto", icon: "https://raw.githubusercontent.com/StrykerSKS/SmartThings/master/smartapp-icons/ecobee/png/fanmode_on_slider_off.png"
            state "on", icon: "https://raw.githubusercontent.com/StrykerSKS/SmartThings/master/smartapp-icons/ecobee/png/fanmode_on_slider_on.png"
        }
        
		standardTile("upButtonControl", "device.thermostatSetpoint", width: 2, height: 1, inactiveLabel: false, decoration: "flat") {
			state "setpoint", action:"raiseSetpoint", icon:"st.thermostat.thermostat-up"
		}
		valueTile("thermostatSetpoint", "device.thermostatSetpoint", width: 2, height: 2, decoration: "flat") {
			state "thermostatSetpoint", label:'${currentValue}°',
				backgroundColors: getTempColors()
		}
		valueTile("currentStatus", "device.thermostatStatus", height: 2, width: 4, decoration: "flat") {
			state "thermostatStatus", label:'${currentValue}', backgroundColor:"#ffffff"
		}
		standardTile("downButtonControl", "device.thermostatSetpoint", height: 1, width: 2, inactiveLabel: false, decoration: "flat") {
			state "setpoint", action:"lowerSetpoint", icon:"st.thermostat.thermostat-down"
		}
		controlTile("heatSliderControl", "device.heatingSetpoint", "slider", height: 1, width: 4, inactiveLabel: false, range: getSliderRange() /* "(15..85)" */ ) {
			state "setHeatingSetpoint", action:"thermostat.setHeatingSetpoint", backgroundColor:"#d04e00", unit: 'C'
		}
		valueTile("heatingSetpoint", "device.heatingSetpoint", height: 1, width: 2, inactiveLabel: false, decoration: "flat") {
			state "heat", label:'${currentValue}°\nHeat', unit:"dF", backgroundColor:"#d04e00"
		}
		controlTile("coolSliderControl", "device.coolingSetpoint", "slider", height: 1, width: 4, inactiveLabel: false, range: getSliderRange() /* "(15..85)" */ ) {
			state "setCoolingSetpoint", action:"thermostat.setCoolingSetpoint", backgroundColor: "#1e9cbb", unit: 'C'
		}
		valueTile("coolingSetpoint", "device.coolingSetpoint", width: 2, height: 1, inactiveLabel: false, decoration: "flat") {
			state "cool", label:'${currentValue}°\nCool', unit:"dF", backgroundColor: "#1e9cbb"
		}
		standardTile("refresh", "device.thermostatMode", width: 2, height: 2,inactiveLabel: false, decoration: "flat") {
            state "default", action:"refresh.refresh", label: "Refresh", icon:"https://raw.githubusercontent.com/StrykerSKS/SmartThings/master/smartapp-icons/ecobee/png/header_ecobeeicon_blk.png"
		}
        
        standardTile("resumeProgram", "device.resumeProgram", width: 2, height: 2, inactiveLabel: false, decoration: "flat") {
			state "resume", action:"resumeProgram", nextState: "updating", label:'Resume', icon:"https://raw.githubusercontent.com/StrykerSKS/SmartThings/master/smartapp-icons/ecobee/png/action_resume_program.png"
			state "updating", label:"Working", icon: "st.samsung.da.oven_ic_send"
		}
        
        // TODO: Add icons and handling for Ecobee Comfort Settings
        standardTile("currentProgramIcon", "device.currentProgramName", height: 2, width: 2, inactiveLabel: false, decoration: "flat") {
			state "Home", action:"noOp", label: 'Home', icon: "https://raw.githubusercontent.com/StrykerSKS/SmartThings/master/smartapp-icons/ecobee/png/schedule_home_blue.png"
			state "Away", action:"noOp", label: 'Away', icon: "https://raw.githubusercontent.com/StrykerSKS/SmartThings/master/smartapp-icons/ecobee/png/schedule_away_blue.png"
            state "Sleep", action:"noOp", label: 'Sleep', icon: "https://raw.githubusercontent.com/StrykerSKS/SmartThings/master/smartapp-icons/ecobee/png/schedule_asleep_blue.png"
            state "Auto Away", action:"noOp", label: 'Auto Away', icon: "https://raw.githubusercontent.com/StrykerSKS/SmartThings/master/smartapp-icons/ecobee/png/schedule_away_blue.png" // Fix to auto version
            state "Auto Home", action:"noOp", label: 'Auto Home', icon: "https://raw.githubusercontent.com/StrykerSKS/SmartThings/master/smartapp-icons/ecobee/png/schedule_home_blue.png" // Fix to auto
            state "Hold", action:"noOp", label: "Hold Activated", icon: "https://raw.githubusercontent.com/StrykerSKS/SmartThings/master/smartapp-icons/ecobee/png/schedule_generic_chair_blue.png"
            state "Hold: Fan", action:"noOp", label: "Hold: Fan", icon: "https://raw.githubusercontent.com/StrykerSKS/SmartThings/master/smartapp-icons/ecobee/png/systemmode_fan.png"
            state "Hold: Home", action:"noOp", label: 'Hold: Home', icon: "https://raw.githubusercontent.com/StrykerSKS/SmartThings/master/smartapp-icons/ecobee/png/schedule_home_blue.png"
            state "Hold: Away", action:"noOp", label: 'Hold: Away',  icon: "https://raw.githubusercontent.com/StrykerSKS/SmartThings/master/smartapp-icons/ecobee/png/schedule_away_blue.png"
            state "Hold: Sleep", action:"noOp", label: 'Hold: Sleep',  icon: "https://raw.githubusercontent.com/StrykerSKS/SmartThings/master/smartapp-icons/ecobee/png/schedule_asleep_blue.png"
      		state "Vacation", action: "noOp", label: 'Vacation', icon: "https://raw.githubusercontent.com/SANdood/Ecobee/master/icons/schedule_vacation_airplane_yellow.png"
      		state "default", action:"noOp", label: 'Other: ${currentValue}', icon: "https://raw.githubusercontent.com/StrykerSKS/SmartThings/master/smartapp-icons/ecobee/png/schedule_generic_chair_blue.png"
            
		}        
        
        valueTile("currentProgram", "device.currentProgramName", height: 2, width: 4, inactiveLabel: false, decoration: "flat") {
			state "default", label:'Comfort Setting:\n${currentValue}' 
		}
        
		standardTile("setHome", "device.setHome", width: 2, height: 2, inactiveLabel: false, decoration: "flat") {
			state "home", action:"home", nextState: "updating", label:'Home', icon:"https://raw.githubusercontent.com/StrykerSKS/SmartThings/master/smartapp-icons/ecobee/png/schedule_home_blue.png"
			state "updating", label:"Working...", icon: "st.samsung.da.oven_ic_send"
		}
        
        standardTile("setAway", "device.setAway", width: 2, height: 2, inactiveLabel: false, decoration: "flat") {
			state "away", action:"away", nextState: "updating", label:'Away', icon:"https://raw.githubusercontent.com/StrykerSKS/SmartThings/master/smartapp-icons/ecobee/png/schedule_away_blue.png"
			state "updating", label:"Working...", icon: "st.samsung.da.oven_ic_send"
		}

        standardTile("setSleep", "device.setSleep", width: 2, height: 2, inactiveLabel: false, decoration: "flat") {
			// state "sleep", action:"sleep", nextState: "updating", label:'Set Sleep', icon:"st.Bedroom.bedroom2"
			// can't call "sleep()" because of conflict with internal definition (it silently fails)
            state "sleep", action:"night", nextState: "updating", label:'Sleep', icon:"https://raw.githubusercontent.com/StrykerSKS/SmartThings/master/smartapp-icons/ecobee/png/schedule_asleep_blue.png"
			state "updating", label:"Working...", icon: "st.samsung.da.oven_ic_send"
		}

        standardTile("operatingState", "device.thermostatOperatingState", width: 2, height: 2, inactiveLabel: false, decoration: "flat") {
			//state "idle", label: "Idle", backgroundColor:"#44b621", icon: "st.nest.empty"
            state "idle", icon: "https://raw.githubusercontent.com/StrykerSKS/SmartThings/master/smartapp-icons/ecobee/png/systemmode_idle.png"
            state "fan only", icon: "https://raw.githubusercontent.com/StrykerSKS/SmartThings/master/smartapp-icons/ecobee/png/operatingstate_fan.png"
			state "heating", icon: "https://raw.githubusercontent.com/StrykerSKS/SmartThings/master/smartapp-icons/ecobee/png/operatingstate_heat.png"
			state "cooling", icon: "https://raw.githubusercontent.com/StrykerSKS/SmartThings/master/smartapp-icons/ecobee/png/operatingstate_cool.png"
            // Issue reported that the label overlaps. Need to remove the icon
            state "default", label: '${currentValue}', icon: "st.nest.empty"
		}
			
		standardTile("equipmentState", "device.equipmentOperatingState", width: 2, height: 2, inactiveLabel: false, decoration: "flat") {
			state "idle", icon: "https://raw.githubusercontent.com/SANdood/Ecobee/master/icons/systemmode_idle_purple.png"
            state "fan only", icon: "https://raw.githubusercontent.com/StrykerSKS/SmartThings/master/smartapp-icons/ecobee/png/operatingstate_fan.png"
			state "emergency", icon: "https://raw.githubusercontent.com/SANdood/Ecobee/master/icons/operatingstate_heat_emergency.png"
            state "heat pump", icon: "https://raw.githubusercontent.com/StrykerSKS/SmartThings/master/smartapp-icons/ecobee/png/operatingstate_heat.png"
            state "heat 1", icon: "https://raw.githubusercontent.com/SANdood/Ecobee/master/icons/operatingstate_heat_1.png"
			state "heat 2", icon: "https://raw.githubusercontent.com/SANdood/Ecobee/master/icons/operatingstate_heat_2.png"
			state "heat 3", icon: "https://raw.githubusercontent.com/SANdood/Ecobee/master/icons/operatingstate_heat_3.png"
			state "heat pump 2", icon: "https://raw.githubusercontent.com/SANdood/Ecobee/master/icons/operatingstate_heat_2.png"
			state "heat pump 3", icon: "https://raw.githubusercontent.com/SANdood/Ecobee/master/icons/operatingstate_heat_3.png"
			state "cool 1", icon: "https://raw.githubusercontent.com/SANdood/Ecobee/master/icons/operatingstate_cool_1.png"
			state "cool 2", icon: "https://raw.githubusercontent.com/SANdood/Ecobee/master/icons/operatingstate_cool_2.png"
			state "heating", icon: "https://raw.githubusercontent.com/StrykerSKS/SmartThings/master/smartapp-icons/ecobee/png/operatingstate_heat.png"
			state "cooling", icon: "https://raw.githubusercontent.com/StrykerSKS/SmartThings/master/smartapp-icons/ecobee/png/operatingstate_cool.png"
			state "emergency hum", icon: "https://raw.githubusercontent.com/SANdood/Ecobee/master/icons/operatingstate_heat_emergency+humid.png"
            state "heat pump hum", icon: "https://raw.githubusercontent.com/StrykerSKS/SmartThings/master/smartapp-icons/ecobee/png/operatingstate_heat+humid.png"
            state "heat 1 hum", icon: "https://raw.githubusercontent.com/SANdood/Ecobee/master/icons/operatingstate_heat_1+humid.png"
			state "heat 2 hum", icon: "https://raw.githubusercontent.com/SANdood/Ecobee/master/icons/operatingstate_heat_2+humid.png"
			state "heat 3 hum", icon: "https://raw.githubusercontent.com/SANdood/Ecobee/master/icons/operatingstate_heat_3+humid.png"
			state "heat pump 2 hum", icon: "https://raw.githubusercontent.com/SANdood/Ecobee/master/icons/operatingstate_heat_2+humid.png"
			state "heat pump 3 hum", icon: "https://raw.githubusercontent.com/SANdood/Ecobee/master/icons/operatingstate_heat_3+humid.png"
			state "cool 1 hum", icon: "https://raw.githubusercontent.com/SANdood/Ecobee/master/icons/operatingstate_cool_1-humid.png"
			state "cool 2 hum", icon: "https://raw.githubusercontent.com/SANdood/Ecobee/master/icons/operatingstate_cool_2-humid.png"
			state "heating hum", icon: "https://raw.githubusercontent.com/StrykerSKS/SmartThings/master/smartapp-icons/ecobee/png/operatingstate_heat+humid.png"
			state "cooling hum", icon: "https://raw.githubusercontent.com/StrykerSKS/SmartThings/master/smartapp-icons/ecobee/png/operatingstate_cool-humid.png"
            state "humidifier", icon: "https://raw.githubusercontent.com/SANdood/Ecobee/master/icons/operatingstate_humidifier_only.png"
            state "dehumidifier", icon: "https://raw.githubusercontent.com/SANdood/Ecobee/master/icons/operatingstate_dehumidifier_only.png"
            // Issue reported that the label overlaps. Need to remove the icon
            state "default", action:"noOp", label: '${currentValue}', icon: "st.nest.empty"
		}

        valueTile("humidity", "device.humidity", inactiveLabel: false, decoration: "flat", width: 2, height: 2) {
			state("default", label: '${currentValue}%', unit: "humidity", backgroundColor:"#d28de0")
		}
        
        standardTile("motionState", "device.motion", width: 2, height: 2, inactiveLabel: false, decoration: "flat") {
			state "active", action:"noOp", nextState: "active", label:"Motion", icon:"https://raw.githubusercontent.com/StrykerSKS/SmartThings/master/smartapp-icons/ecobee/png/motion_sensor_motion.png"
			state "inactive", action: "noOp", nextState: "inactive", label:"No Motion", icon:"https://raw.githubusercontent.com/StrykerSKS/SmartThings/master/smartapp-icons/ecobee/png/motion_sensor_nomotion.png"
            state "not supported", action: "noOp", nextState: "not supported", label: "N/A", icon:"https://raw.githubusercontent.com/StrykerSKS/SmartThings/master/smartapp-icons/ecobee/png/notsupported_x.png"
		}

        // Weather Tiles and other Forecast related tiles
		standardTile("weatherIcon", "device.weatherSymbol", inactiveLabel: false, width: 2, height: 2, decoration: "flat") {
			state "-2",			icon: "https://raw.githubusercontent.com/SANdood/Ecobee/master/icons/weather_updating_-2_fc.png" // label: 'updating...',	
			state "0",			icon: "https://raw.githubusercontent.com/SANdood/Ecobee/master/icons/weather_sunny_00_fc.png" // label: 'Sunny',			
			state "1",			icon: "https://raw.githubusercontent.com/SANdood/Ecobee/master/icons/weather_partly_cloudy_02_fc.png" // label: 'Few Clouds',	
			state "2",			icon: "https://raw.githubusercontent.com/SANdood/Ecobee/master/icons/weather_partly_cloudy_02_fc.png"
			state "3",			icon: "https://raw.githubusercontent.com/SANdood/Ecobee/master/icons/weather_mostly_cloudy_03_fc.png"
			state "4",			icon: "https://raw.githubusercontent.com/SANdood/Ecobee/master/icons//weather_cloudy_04_fc.png"
			state "5",			icon: "https://raw.githubusercontent.com/SANdood/Ecobee/master/icons/weather_drizzle_05_fc.png"
			state "6",			icon: "https://raw.githubusercontent.com/SANdood/Ecobee/master/icons/weather_rain_06_fc.png"
			state "7",			icon: "https://raw.githubusercontent.com/SANdood/Ecobee/master/icons/weather_freezing_rain_07_fc.png"
			state "8",			icon: "https://raw.githubusercontent.com/SANdood/Ecobee/master/icons/weather_rain_06_fc.png"
			state "9",			icon: "https://raw.githubusercontent.com/SANdood/Ecobee/master/icons/weather_freezing_rain_07_fc.png"
			state "10",			icon: "https://raw.githubusercontent.com/SANdood/Ecobee/master/icons/weather_snow_10_fc.png"
			state "11",			icon: "https://raw.githubusercontent.com/SANdood/Ecobee/master/icons/weather_flurries_11_fc.png"
			state "12",			icon: "https://raw.githubusercontent.com/SANdood/Ecobee/master/icons/weather_freezing_rain_07_fc.png"
			state "13",			icon: "https://raw.githubusercontent.com/SANdood/Ecobee/master/icons//weather_snow_10_fc.png"
			state "14",			icon: "https://raw.githubusercontent.com/SANdood/Ecobee/master/icons/weather_freezing_rain_07_fc.png"
			state "15",			icon: "https://raw.githubusercontent.com/SANdood/Ecobee/master/icons/weather_thunderstorms_15_fc.png"
			state "16",			icon: "https://raw.githubusercontent.com/StrykerSKS/SmartThings/master/smartapp-icons/ecobee/png/weather_windy_16.png"
			state "17",			icon: "https://raw.githubusercontent.com/StrykerSKS/SmartThings/master/smartapp-icons/ecobee/png/weather_tornado_17.png"
			state "18",			icon: "https://raw.githubusercontent.com/SANdood/Ecobee/master/icons/weather_fog_18_fc.png"
			state "19",			icon: "https://raw.githubusercontent.com/SANdood/Ecobee/master/icons/weather_fog_18_fc.png" // Hazy
			state "20",			icon: "https://raw.githubusercontent.com/SANdood/Ecobee/master/icons/weather_fog_18_fc.png" // Smoke
			state "21",			icon: "https://raw.githubusercontent.com/SANdood/Ecobee/master/icons/weather_fog_18_fc.png" // Dust
            
            // Night Time Icons (Day time Value + 100)
			state "100",			icon: "https://raw.githubusercontent.com/SANdood/Ecobee/master/icons/weather_clear_night_100_fc.png" // label: 'Sunny',			
			state "101",			icon: "https://raw.githubusercontent.com/SANdood/Ecobee/master/icons/weather_night_partly_cloudy_101_fc.png" // label: 'Few Clouds',	
			state "102",			icon: "https://raw.githubusercontent.com/SANdood/Ecobee/master/icons/weather_night_partly_cloudy_101_fc.png"
			state "103",			icon: "https://raw.githubusercontent.com/SANdood/Ecobee/master/icons/weather_night_mostly_cloudy_103_fc.png"
			state "104",			icon: "https://raw.githubusercontent.com/SANdood/Ecobee/master/icons/weather_cloudy_04_fc.png"
			state "105",			icon: "https://raw.githubusercontent.com/SANdood/Ecobee/master/icons/weather_night_drizzle_105_fc.png"
			state "106",			icon: "https://raw.githubusercontent.com/SANdood/Ecobee/master/icons/weather_night_rain_106_fc.png"
			state "107",			icon: "https://raw.githubusercontent.com/SANdood/Ecobee/master/icons/weather_night_freezing_rain_107_fc.png"
			state "108",			icon: "https://raw.githubusercontent.com/SANdood/Ecobee/master/icons/weather_night_rain_106_fc.png"
			state "109",			icon: "https://raw.githubusercontent.com/SANdood/Ecobee/master/icons/weather_night_freezing_rain_107_fc.png"
			state "110",			icon: "https://raw.githubusercontent.com/SANdood/Ecobee/master/icons//weather_night_snow_110_fc.png"
			state "111",			icon: "https://raw.githubusercontent.com/SANdood/Ecobee/master/icons/weather_night_flurries_111_fc.png"
			state "112",			icon: "https://raw.githubusercontent.com/SANdood/Ecobee/master/icons/weather_night_freezing_rain_107_fc.png"
			state "113",			icon: "https://raw.githubusercontent.com/SANdood/Ecobee/master/icons/weather_night_snow_110_fc.png"
			state "114",			icon: "https://raw.githubusercontent.com/SANdood/Ecobee/master/icons/weather_night_freezing_rain_107_fc.png"
			state "115",			icon: "https://raw.githubusercontent.com/SANdood/Ecobee/master/icons/weather_night_thunderstorms_115_fc.png"
			state "116",			icon: "https://raw.githubusercontent.com/StrykerSKS/SmartThings/master/smartapp-icons/ecobee/png/weather_windy_16.png"
			state "117",			icon: "https://raw.githubusercontent.com/StrykerSKS/SmartThings/master/smartapp-icons/ecobee/png/weather_tornado_17.png"
			state "118",			icon: "https://raw.githubusercontent.com/SANdood/Ecobee/master/icons/weather_fog_18_fc.png"
			state "119",			icon: "https://raw.githubusercontent.com/SANdood/Ecobee/master/icons/weather_fog_18_fc.png" // Hazy
			state "120",			icon: "https://raw.githubusercontent.com/SANdood/Ecobee/master/icons/weather_fog_18_fc.png" // Smoke
			state "121",			icon: "https://raw.githubusercontent.com/SANdood/Ecobee/master/icons/weather_fog_18_fc.png" // Dust
		}
        standardTile("weatherTemperature", "device.weatherTemperature", width: 2, height: 2, decoration: "flat") {
			state "default", action: "noOp", nextState: "default", label: 'Out: ${currentValue}°', icon: "https://raw.githubusercontent.com/SANdood/Ecobee/master/icons/thermometer_fc.png"
		}
        
        valueTile("lastPoll", "device.lastPoll", height: 1, width: 5, decoration: "flat") {
			state "thermostatStatus", label:'Last Poll: ${currentValue}', backgroundColor:"#ffffff"
		}
        
		valueTile("holdStatus", "device.holdStatus", height: 1, width: 5, decoration: "flat") {
			state "default", label:'${currentValue}' //, backgroundColor:"#000000", foregroudColor: "#ffffff"
		}
		
        standardTile("ecoLogo", "device.logo", inactiveLabel: false, width: 1, height: 1) {
			state "default",  icon:"https://raw.githubusercontent.com/StrykerSKS/SmartThings/master/smartapp-icons/ecobee/png/header_ecobeeicon_blk.png"			
		}

        standardTile("oneBuffer", "device.logo", inactiveLabel: false, width: 1, height: 1, decoration: "flat") {
        	state "default"
        }
        
        valueTile("fanMinOnTime", "device.fanMinOnTime", width: 1, height: 1, decoration: "flat") {
        	state "fanMinOnTime", /*"default",  action: "noOp", nextState: "default", */ label: 'Fan On\n${currentValue}m/hr'
        }
        standardTile("commandDivider", "device.logo", inactiveLabel: false, width: 4, height: 1, decoration: "flat") {
        	state "default", icon:"https://raw.githubusercontent.com/StrykerSKS/SmartThings/master/smartapp-icons/ecobee/png/command_divider.png"			
        }        
    
		main(["temperature", "tempSummary"])
		details([
        	// Use this if you are on a fully operational device OS (such as iOS or Android)
        	"tempSummary",
            // Use the lines below if you can't (or don't want to) use the multiAttributeTile version
            // To use, uncomment these lines below, and comment out the line above
            // "temperature", "humidity",  "upButtonControl", "thermostatSetpoint", 
            // "currentStatus", "downButtonControl",
            
        	/* "operatingState", */  "equipmentState", "weatherIcon",  "refresh",  
            "currentProgramIcon", "weatherTemperature", "motionState", 
            "holdStatus", "fanMinOnTime", 
            "oneBuffer", "commandDivider", "oneBuffer",
            "modeShow", "fanModeLabeled",  "resumeProgram", 
            "coolSliderControl", "coolingSetpoint",
            "heatSliderControl", "heatingSetpoint",            
            "fanMode", "fanModeAutoSlider", "fanModeOnSlider", 
            // "currentProgram", "apiStatus",
            "setHome", "setAway", "setSleep",
            "setModeHeat", "setModeCool", "setModeAuto",
            "apiStatus", "lastPoll"
            // "fanOffButton", "fanCirculate", "setVariable"
            ])            
	}

	preferences {
    	section () {
			input "holdType", "enum", title: "Hold Type", description: "When changing temperature, use Temporary or Permanent hold (default)", required: false, options:["Temporary", "Permanent"]
        	// TODO: Add a preference for the background color for "idle"
        	// TODO: Allow for a "smart" Setpoint change in "Auto" mode. Why won't the paragraph show up in the Edit Device screen?
        	paragraph "The Smart Auto Temp Adjust flag allows for the temperature to be adjusted manually even when the thermostat is in Auto mode. An attempt to determine if the heat or cool setting should be changed will be made automatically."
            input "smartAuto", "bool", title: "Smart Auto Temp Adjust", description: true, required: false
            // input "detailedTracing", "bool", title: "Enable Detailed Tracing", description: true, required: false
       }
	}
}

// parse events into attributes
def parse(String description) {
	LOG( "parse() --> Parsing '${description}'" )
	// Not needed for cloud connected devices
}

def refresh() {
	LOG("refresh() called", 4)
	poll()
}

void poll() {
	LOG("Executing 'poll' using parent SmartApp")
    parent.pollChildren(this) //parent will poll ALL the thermostats 
}

def generateEvent(Map results) {
	LOG("generateEvent(): parsing data $results", 4)
    LOG("Debug level of parent: ${parent.settings?.debugLevel}", 4, null, "debug")
	def linkText = getLinkText(device)
    def isMetric = wantMetric()

	def updateTempRanges = false
	
	if(results) {
		results.each { name, value ->
			LOG("generateEvent() - In each loop: name: ${name}  value: ${value}", 4)
			def isDisplayed = true
            String tempDisplay = ""
			def eventFront = [name: name, linkText: linkText, handlerName: name]
			def event = [:]
			def isChange = isStateChange(device, name, value.toString())
			
			switch (name) {
				case 'temperature':
				case 'heatingSetpoint':
				case 'coolingSetpoint':
				case 'weatherTemperature':
            		def precision = device.currentValue('decimalPrecision')
                	if (!precision) precision = isMetric ? 1 : 0
					String sendValue = isMetric ? "${convertTemperatureIfNeeded(value.toDouble(), "F", precision.toInteger())}" : "${value}" //API return temperature value in F
                	// LOG("generateEvent(): Temperature ${name} value: ${sendValue}", 5, this, "trace")
					if (isChange) event = eventFront + [value: sendValue,  descriptionText: getTemperatureDescriptionText(name, value, linkText), isStateChange: true, displayed: true]
					if (name=="temperature") {
						// Generate the display value that will preserve decimal positions ending in 0
                    	if (precision == 0) {
                    		tempDisplay = value.toDouble().round(0).toString() + '°'
                    	} else {
							tempDisplay = String.format( "%.${precision.toInteger()}f", value.toDouble().round(precision.toInteger())) + '°'
                    	}
					}
					break;
				
				case 'thermostatOperatingState':
					if (isChange) event = eventFront + [value: "${value}", descriptionText: "Thermostat is ${value}", isStateChange: true, displayed: false]
                	break;
				
				case 'equipmentOperatingState':
					if (isChange) event = eventFront + [value: "${value}", descriptionText: "Equipment is ${value}", isStateChange: true, displayed: true]
					break;
				
				case 'equipmentStatus':
				  	String descText = (value == 'idle') ? 'Equipment is idle' : "Equipment: ${value} running"
					if (isChange) event = eventFront +  [value: "${value}", descriptionText: descText, isStateChange: true, displayed: false]
					break;
				
           		case 'lastPoll':
					if (isChange) event = eventFront + [value: "${value}", descriptionText: "Poll: ${value}", isStateChange: true, displayed: true]
					break;
				
				case 'humidity':
					if (isChange) event = eventFront + [value: "${value}", descriptionText: "Humidity is ${value}%", isStateChange: true, displayed: true]
            		break;
				
				case 'humiditySetpoint':
					if (isChange) event = eventFront + [value: "${value}", descriptionText: "Humidity setpoint is ${value}%", isStateChange: true, displayed: true]
		            break;
				
				case 'currentProgramName':
					if (isChange) event = eventFront + [value: "${value}", descriptionText: "Program is ${value}%", isStateChange: true, displayed: true]
					break;
				
				case 'apiConnected':
                	if (isChange) event = eventFront + [value: "${value}", descriptionText: "API Connection is ${value}", isStateChange: true, displayed: true]
					break;
				
				case 'weatherSymbol':
					// Check to see if it is night time, if so change to a night symbol
					def symbolNum = value.toInteger()
					if (device.currentValue('timeOfDay') == 'night') {
						symbolNum = value.toInteger() + 100
						isChange = isStateChange(device, name, symbolNum.toString())
					}
					if (isChange) event = eventFront + [value: "${symbolNum}", descriptionText: "Weather Symbol is ${symbolNum}", isStateChange: true, displayed: true]
					break;
				
				case 'thermostatHold':
					String descText = (value == "") ? 'Hold finished' : (value == 'hold') ? "Hold: ${device.currentValue('currentProgram')} (${device.currentValue('scheduledProgram')})" : "Hold for ${value}"
					if (isChange) event = eventFront + [value: "${value}", descriptionText: descText, isStateChange: true, displayed: true]
					break;
				
				case 'holdStatus': 
					if (isChange) event = eventFront + [value: "${value}", descriptionText: "${value}", isStateChange: true, displayed: true]
					break;
				
				case 'fanMinOnTime':
					if (isChange) event = eventFront + [value: "${value}", descriptionText: "Fan On ${value} minutes per hour", isStateChange: true, displayed: true]
					break;
				
				case 'thermostatMode':
					if (isChange) event = eventFront + [value: "${value}", descriptionText: "Mode is ${value}", isStateChange: true, displayed: true]
		            break;
				
        		case 'thermostatFanMode':
					if (isChange) event = eventFront + [value: "${value}", descriptionText: "Fan Mode is ${value}", isStateChange: true, displayed: true]
            		break;
				
				case 'debugEventFromParent':
					event = eventFront + [value: "${value}", descriptionText: "-> ${value}", isStateChange: true, displayed: true]
					break;
				
				// These are ones we don't need to display or provide descriptionText for (mostly internal or debug use)
				case 'debugLevel':
				case 'heatRangeLow':
				case 'heatRangeHigh':
				case 'coolRangeLow':
				case 'coolRangeHigh':
				case 'heatRange':
				case 'coolRange':
				case 'decimalPrecision':
				case 'timeOfDay':
				case 'heatMode':
				case 'coolMode':
				case 'autoMode':
				case 'auxHeatMode':
				case 'currentProgramId':
				case 'currentProgram':
				case 'scheduledProgramName':
				case 'scheduledProgramId':
				case 'scheduledProgram':
				case 'heatStages':
				case 'coolStages':
				case 'hasHeatPump':
				case 'hasForcedAir':
				case 'hasElectric':
				case 'hasBoiler':
				case 'auxHeatMode':
				case 'heatDifferential':
				case 'coolDifferential':
					if (isChange) event = eventFront +  [value: "${value}", isStateChange: true, displayed: false]
					break;
				
				// everything else just gets displayed with generic text
				default:
					if (isChange) event = eventFront + [value: "${value}", descriptionText: "${name} is ${value}", isStateChange: true, displayed: true]			
					break;
			}
			if (event != [:]) {
				LOG("generateEvent() - Out of switch{}, calling sendevent(${event})", 5)
				sendEvent(event)
			}
            if (tempDisplay != "") {
        		event = [ name: 'temperatureDisplay', value: tempDisplay, linkText: linkText, descriptionText:"Temperature Display is ${tempDisplay}", displayed: false ]
        		sendEvent(event)
            	LOG("generateEvent() - Temperature updated, calling sendevent(${event})", 5)
        	}
		}
		generateSetpointEvent()
		generateStatusEvent()
	}
}

//return descriptionText to be shown on mobile activity feed
private getTemperatureDescriptionText(name, value, linkText) {
	switch (name) {
		case 'temperature':
			return "Temperature is ${value}°"
            break;
		case 'heatingSetpoint':
			return "Heating setpoint is ${value}°"
            break;
        case 'coolingSetpoint':
			return "Cooling setpoint is ${value}°"
            break;
        case 'weatherTemperature':
        	return "Outside temperature is ${value}°"
            break;
	}
}

// Does not set in absolute values, sets in increments either up or down
def setTemperature(setpoint) {
	LOG("setTemperature() called with setpoint ${setpoint}. Current temperature: ${device.currentValue("temperature")}. Heat Setpoint: ${device.currentValue("heatingSetpoint")}. Cool Setpoint: ${device.currentValue("coolingSetpoint")}. Thermo Setpoint: ${device.currentValue("thermostatSetpoint")}", 4)

    def mode = device.currentValue("thermostatMode")
    def midpoint
	def targetvalue

	if (mode == "off" || (mode == "auto" && !usingSmartAuto() )) {
		LOG("setTemperature(): this mode: $mode does not allow raiseSetpoint", 2, null, "warn")
        return
    }

	def currentTemp = device.currentValue("temperature")
    def deltaTemp = 0

	if (setpoint == 0) { // down arrow pressed
    	deltaTemp = -1
    } else if (setpoint == 1) { // up arrow pressed
    	deltaTemp = 1
    } else {
    	deltaTemp = ( (setpoint - currentTemp) < 0) ? -1 : 1
    }
    
    LOG("deltaTemp = ${deltaTemp}")

    if (mode == "auto") {
    	// In Smart Auto Mode
		LOG("setTemperature(): In Smart Auto Mode", 4)

        if (deltaTemp < 0) {
        	// Decrement the temp for cooling
            LOG("Smart Auto: lowerSetpoint being called", 4)
            lowerSetpoint()
        } else if (deltaTemp > 0) {
        	// Increment the temp for heating
            LOG("Smart Auto: raiseSetpoint being called", 4)
            raiseSetpoint()
        } // Otherwise they are equal and the setpoint does not change

    } else if (mode == "heat") {
    	// Change the heat
        LOG("setTemperature(): change the heat temp", 4)
        // setHeatingSetpoint(setpoint)
        if (deltaTemp < 0) {
        	// Decrement the temp for cooling
            LOG("Heat: lowerSetpoint being called", 4)
            lowerSetpoint()
        } else if (deltaTemp > 0) {
        	// Increment the temp for heating
            LOG("Heat: raiseSetpoint being called", 4)
            raiseSetpoint()
        } // Otherwise they are equal and the setpoint does not change

    } else if (mode == "cool") {
    	// Change the cool
        LOG("setTemperature(): change the cool temp", 4)
        // setCoolingSetpoint(setpoint)
        if (deltaTemp < 0) {
        	// Decrement the temp for cooling
            LOG("Cool: lowerSetpoint being called", 4)
            lowerSetpoint()
        } else if (deltaTemp > 0) {
        	// Increment the temp for heating
            LOG("Cool: raiseSetpoint being called", 4)
            raiseSetpoint()
        } // Otherwise they are equal and the setpoint does not change

    }
}

void setHeatingSetpoint(setpoint) {
	LOG("setHeatingSetpoint() request with setpoint value = ${setpoint} before toDouble()", 4)
	setHeatingSetpoint(setpoint.toDouble())
}

void setHeatingSetpoint(Double setpoint) {
//    def mode = device.currentValue("thermostatMode")
	LOG("setHeatingSetpoint() request with setpoint value = ${setpoint}", 4)

	def heatingSetpoint = setpoint
	def coolingSetpoint = device.currentValue("coolingSetpoint").toDouble()
	def deviceId = getDeviceId()

	LOG("setHeatingSetpoint() before compare: heatingSetpoint == ${heatingSetpoint}   coolingSetpoint == ${coolingSetpoint}", 4)
	//enforce limits of heatingSetpoint vs coolingSetpoint
	def low = device.currentValue("heatRangeLow")
	def high = device.currentValue("heatRangeHigh")
	
	if (heatingSetpoint < low ) { heatingSetpoint = low }
	if (heatingSetpoint > high) { heatingSetpoint = high}
	if (heatingSetpoint > coolingSetpoint) {
		coolingSetpoint = heatingSetpoint
	}

	LOG("Sending setHeatingSetpoint> coolingSetpoint: ${coolingSetpoint}, heatingSetpoint: ${heatingSetpoint}")

	def sendHoldType = whatHoldType()

	if (parent.setHold(this, heatingSetpoint,  coolingSetpoint, deviceId, sendHoldType)) {
		sendEvent("name":"heatingSetpoint", "value": wantMetric() ? heatingSetpoint : heatingSetpoint.toDouble().round(0).toInteger() )
		sendEvent("name":"coolingSetpoint", "value": wantMetric() ? coolingSetpoint : coolingSetpoint.toDouble().round(0).toInteger() )
		LOG("Done setHeatingSetpoint> coolingSetpoint: ${coolingSetpoint}, heatingSetpoint: ${heatingSetpoint}")
		generateSetpointEvent()
		generateStatusEvent()
	} else {
		LOG("Error setHeatingSetpoint(${setpoint})", 2, null, "error") //This error is handled by the connect app
        
	}
}

void setCoolingSetpoint(setpoint) {
	LOG("setCoolingSetpoint() request with setpoint value = ${setpoint} (before toDouble)", 4)

	setCoolingSetpoint(setpoint.toDouble())
}

void setCoolingSetpoint(Double setpoint) {
	LOG("setCoolingSetpoint() request with setpoint value = ${setpoint}", 4)
//    def mode = device.currentValue("thermostatMode")
	def heatingSetpoint = device.currentValue("heatingSetpoint").toDouble()
	def coolingSetpoint = setpoint
	def deviceId = getDeviceId()


	LOG("setCoolingSetpoint() before compare: heatingSetpoint == ${heatingSetpoint}   coolingSetpoint == ${coolingSetpoint}")

	//enforce limits of heatingSetpoint vs coolingSetpoint
	def low = device.currentValue("coolRangeLow")
	def high = device.currentValue("coolRangeHigh")
	
	if (coolingSetpoint < low ) { coolingSetpoint = low }
	if (coolingSetpoint > high) { coolingSetpoint = high}
	if (heatingSetpoint > coolingSetpoint) {
		heatingSetpoint = coolingSetpoint
	}

	LOG("Sending setCoolingSetpoint> coolingSetpoint: ${coolingSetpoint}, heatingSetpoint: ${heatingSetpoint}")
	def sendHoldType = whatHoldType()
    LOG("sendHoldType == ${sendHoldType}", 5)

    // Convert temp to F from C if needed
	if (parent.setHold(this, heatingSetpoint,  coolingSetpoint, deviceId, sendHoldType)) {
		sendEvent("name":"heatingSetpoint", "value": wantMetric() ? heatingSetpoint : heatingSetpoint.toDouble().round(0).toInteger() )
		sendEvent("name":"coolingSetpoint", "value": wantMetric() ? coolingSetpoint : coolingSetpoint.toDouble().round(0).toInteger() )
		LOG("Done setCoolingSetpoint>> coolingSetpoint = ${coolingSetpoint}, heatingSetpoint = ${heatingSetpoint}", 4)
		generateSetpointEvent()
		generateStatusEvent()
	} else {
		LOG("Error setCoolingSetpoint(setpoint)", 2, null, "error") //This error is handled by the connect app
	}
}

void resumeProgram(resumeAll=true) {
	// TODO: Put a check in place to see if we are already running the program. If there is nothing to resume, then save the calls upstream
	def thermostatHold = device.currentValue("thermostatHold")
	if (thermostatHold == "") {
		LOG("resumeProgram() but no current hold", 3)
		return
	} else if (thermostatHold == "vacation") {
		LOG("resumeProgram() - cannot resume from ${thermostatHold} hold", 3, null, "error")
		return
	} else {
		LOG("resumeProgram() is called, hold type is ${thermostatHold}", 4)
	}
	
	sendEvent("name":"thermostatStatus", "value":"Resuming schedule...", "description":statusText, displayed: false)
	def deviceId = getDeviceId()
	if (parent.resumeProgram(this, deviceId, resumeAll)) {
		sendEvent("name":"thermostatStatus", "value":"Setpoint updating...", "description":statusText, displayed: false)
		runIn(15, "poll")
		LOG("resumeProgram() is done", 5)
		sendEvent("name":"resumeProgram", "value":"resume", descriptionText: "resumeProgram is done", displayed: false, isStateChange: true)
	} else {
		sendEvent("name":"thermostatStatus", "value":"failed resume, click refresh", "description":statusText, displayed: false)
		LOG("Error resumeProgram() check parent.resumeProgram(this, deviceId)", 2, null, "error")
	}

	generateSetpointEvent()
	generateStatusEvent()    
}

/*
def fanModes() {
	["off", "on", "auto", "circulate"]
}
*/

def generateQuickEvent(name, value) {
	generateQuickEvent(name, value, 0)
}

def generateQuickEvent(name, value, pollIn) {
	sendEvent(name: name, value: value, displayed: true)
    if (pollIn > 0) { runIn(pollIn, "poll") }
}

void setThermostatMode(String value) {
	// 	"emergencyHeat" "heat" "cool" "off" "auto"
    
    if (value=="emergency" || value=="emergencyHeat") { value = "auxHeatOnly" }    
	LOG("setThermostatMode(${value})", 5)
	generateQuickEvent("thermostatMode", value)

    def deviceId = getDeviceId()
	if (parent.setMode(this, value, deviceId)) {
		// generateQuickEvent("thermostatMode", value, 15)
	} else {
		LOG("Error setting new mode to ${value}.", 1, null, "error")
		def currentMode = device.currentValue("thermostatMode")
		generateQuickEvent("thermostatMode", currentMode) // reset the tile back
	}
	generateSetpointEvent()
	generateStatusEvent()
}

void off() {
	LOG("off()", 5)
    setThermostatMode("off")    
}

void heat() {
	LOG("heat()", 5)
    setThermostatMode("heat")    
}

void auxHeatOnly() {
	LOG("auxHeatOnly()", 5)
    setThermostatMode("auxHeatOnly")
}

void emergency() {
	LOG("emergency()", 5)
    setThermostatMode("auxHeatOnly")
}

// This is the proper definition for the capability
void emergencyHeat() {
	LOG("emergencyHeat()", 5)
    setThermostatMode("auxHeatOnly")
}

void cool() {
	LOG("cool()", 5)
    setThermostatMode("cool")    
}

void auto() {
	LOG("auto()", 5)
    setThermostatMode("auto")    
}

// Handle Comfort Settings
void setThermostatProgram(program, holdType=null) {
	// Change the Comfort Setting to Home
    LOG("setThermostatProgram: program: ${program}  holdType: ${holdType}", 4)
	def deviceId = getDeviceId()    

	LOG("Before calling parent.setProgram()", 5)
	
    def sendHoldType = holdType ?: whatHoldType()
    poll()		// need to know if scheduled program changed recently
	
	// if the requested program is the same as the one that is supposed to be running, then just resumeProgram
	// but only if this is NOT a permanent hold request
	if (sendHoldType == 'nextTransition') {
		if (device.currentValue("scheduledProgram") == program) {
			LOG("setThermostatProgram() - resuming scheduled program ${program}", 4)
			resumeProgram()
			return
		}
	}
  
    if ( parent.setProgram(this, program, deviceId, sendHoldType) ) {
		generateProgramEvent(program)
	} else {
    	LOG("Error setting new comfort setting ${program}.", 2, null, "warn")
		def priorProgram = device.currentState("currentProgramId")?.value
		generateProgramEvent(priorProgram, program) // reset the tile back
	}
 
 	LOG("After calling parent.setProgram()", 5)
    
	generateSetpointEvent()
	generateStatusEvent()    
}

void home() {
	// Change the Comfort Setting to Home
    LOG("home()", 5)
    setThermostatProgram("Home")
}

void present(){
	// Change the Comfort Setting to Home (Nest compatibility)
    LOG("present()", 5)
    setThermostatProgram("Home")
}
void away() {
	// Change the Comfort Setting to Away
    LOG("away()", 5)
    setThermostatProgram("Away")
}

// Unfortunately, we can't overload the internal Java/Groovy/system definition of 'sleep()'
/* def sleep() {
	// Change the Comfort Setting to Sleep    
    LOG("sleep()", 5)
    setThermostatProgram("Sleep")
}
*/
void asleep() {
	// Change the Comfort Setting to Sleep    
    LOG("asleep()", 5)
    setThermostatProgram("Sleep")
}

void night() {
	// Change the Comfort Setting to Sleep    
    LOG("night()", 5)
    setThermostatProgram("Sleep")
}

def generateProgramEvent(program, failedProgram=null) {
	LOG("Generate generateProgramEvent Event: program ${program}", 4)

	sendEvent("name":"thermostatStatus", "value":"Setpoint updating...", "description":statusText, displayed: false)
	sendEvent("name":"currentProgramName", "value":"Hold: "+program.capitalize())
    sendEvent("name":"currentProgramId", "value":program)
    
    def tileName = ""
    
    if (!failedProgram) {
    	tileName = "set" + program.capitalize()    	
    } else {
    	tileName = "set" + failedProgram.capitalize()    	
    }
    sendEvent("name":"${tileName}", "value":"${program}", descriptionText: "${tileName} is done", displayed: false, isStateChange: true)
}

def setThermostatFanMode(value, holdType=null) {
	LOG("setThermostatFanMode(${value})", 4)
	// "auto" "on" "circulate" "off"       
    
    // This is to work around a bug in some SmartApps that are using fanOn and fanAuto as inputs here, which is wrong
    if (value == "fanOn" || value == "on" ) { value = "on" }
    else if (value == "fanAuto" || value == "auto" ) { value = "auto" }
    else if (value == "fanCirculate" || value == "circulate")  { value == "circulate" }
    else if (value == "fanOff" || value == "off") { value = "off" }
	else {
    	LOG("setThermostatFanMode() - Unrecognized Fan Mode: ${value}. Setting to 'auto'", 1, null, "error")
        value = "auto"
    }
    
    // Change the state now to quickly refresh the UI
    generateQuickEvent("thermostatFanMode", value, 0)
    
    def results = parent.setFanMode(this, value, getDeviceId())
    
	if ( results ) {
    	LOG("parent.setFanMode() returned successfully!", 5)
    } else {
    	generateQuickEvent("thermostatFanMode", device.currentValue("thermostatFanMode"))
    }
    
	generateSetpointEvent()
	generateStatusEvent()    
}

def fanOn() {
	LOG("fanOn()", 5)
    setThermostatFanMode("on")
}

def fanAuto() {
	LOG("fanAuto()", 5)
	setThermostatFanMode("auto")
}

def fanCirculate() {
	LOG("fanCirculate()", 5)
    setThermostatFanMode("circulate")
}

def fanOff() {
	LOG("fanOff()", 5)
	setThermostatFanMode("off")
}

void setFanMinOnTime(minutes) {
	LOG("setFanMinOnTime(${minutes})", 5, null, "trace")
    def deviceId = getDeviceId()
    
	def howLong = 10	// default to 10 minutes, if no value supplied
	if (minutes.isNumber()) howLong = minutes
    if ((howLong >=0) && (howLong <=  55)) {
		parent.setFanMinOnTime(this, deviceId, howLong)
    } else {
    	LOG("setFanMinOnTime(${minutes}) - invalid argument",5,null, "error")
    }
}

void setVacationFanMinOnTime(minutes) {
	LOG("setVacationFanMinOnTime(${minutes})", 5, null, "trace")
    def deviceId = getDeviceId()
    
	def howLong = 0		// default to 0 minutes during Vacations, if no value supplied
	if (minutes.isNumber()) howLong = minutes
    if ((howLong >=0) && (howLong <=  55)) {
		parent.setVacationFanMinOnTime(this, deviceId, howLong)
    } else {
    	LOG("setVacationFanMinOnTime(${minutes}) - invalid argument",5,null, "error")
    }
}

void deleteVacation(vacationName = null) {
	LOG("deleteVacation(${vacationName})", 5, null, "trace")
    def deviceId = getDeviceId()
    parent.deleteVacation(this, deviceId, vacationName)
}

def generateSetpointEvent() {
	LOG("Generate SetPoint Event", 5, null, "trace")

	def mode = device.currentValue("thermostatMode")    
    def heatingSetpoint = device.currentValue("heatingSetpoint")
	def coolingSetpoint = device.currentValue("coolingSetpoint")
    
	LOG("Current Mode = ${mode}", 4, null, "debug")
	LOG("Heating Setpoint = ${heatingSetpoint}", 4, null, "debug")
	LOG("Cooling Setpoint = ${coolingSetpoint}", 4, null, "debug")

	switch (mode) {
		case 'heat':
		case 'emergencyHeat':
			sendEvent(name:'thermostatSetpoint', value: "${heatingSetpoint}")
			break;
		
		case 'cool':
			sendEvent(name:'thermostatSetpoint', value: "${coolingSetpoint}")
			break;
		
		case 'auto':
			if (!usingSmartAuto()) {
				// No Smart Auto, just regular auto
				sendEvent(name:'thermostatSetpoint', value:"Auto (${heatingSetpoint}-${coolingSetpoint})")
			} else {
		    	// Smart Auto Enabled
				sendEvent(name:'thermostatSetpoint', value: "${device.currentValue('temperature')}")
			}
			break;
		
		case 'off':
			sendEvent(name:'thermostatSetpoint', value:'Off')
			break;
	}
}

void raiseSetpoint() {
	def mode = device.currentValue("thermostatMode")
	def targetvalue

	if (mode == "off" || (mode == "auto" && !usingSmartAuto() )) {
		LOG("raiseSetpoint(): this mode: $mode does not allow raiseSetpoint")
        return
	}

   	def heatingSetpoint = device.currentValue("heatingSetpoint").toDouble()
	def coolingSetpoint = device.currentValue("coolingSetpoint").toDouble()
    def thermostatSetpoint = device.currentValue("thermostatSetpoint").toDouble()
    if (device.currentValue("thermostatOpertaingState") == 'idle') {
    	if (thermostatSetpoint == heatingSetpoint) {
        	heatingSetpoint = heatingSetpoint + device.currentValue("heatDifferential").toDouble() 	// correct from the display value
            thermostatSetpoint = heatingSetpoint
            coolingSetpoint = coolingSetpoint - device.currentValue("coolDifferential").toDouble()
        } else if (thermostatSetpoint == coolingSetpoint) {
         	coolingSetpoint = coolingSetpoint - device.currentValue("coolDifferential").toDouble()
            thermostatSetpoint = coolingSetpoint
            heatingSetpoint = heatingSetpoint + device.currentValue("heatDifferential").toDouble()
        } else {
          	heatingSetpoint = heatingSetpoint + device.currentValue("heatDifferential").toDouble()
            coolingSetpoint = coolingSetpoint - device.currentValue("coolDifferential").toDouble()
        }
    }
	
	LOG("raiseSetpoint() mode = ${mode}, heatingSetpoint: ${heatingSetpoint}, coolingSetpoint:${coolingSetpoint}, thermostatSetpoint:${thermostatSetpoint}", 4)

   	if (thermostatSetpoint) {
		targetvalue = thermostatSetpoint
	} else {
		targetvalue = 0.0
	}

       if (getTemperatureScale() == "C" ) {
       	targetvalue = targetvalue.toDouble() + 0.5
       } else {
		targetvalue = targetvalue.toDouble() + 1.0
       }

	sendEvent("name":"thermostatSetpoint", "value":( wantMetric() ? targetvalue : targetvalue.round(0).toInteger() ), displayed: true)
	LOG("In mode $mode raiseSetpoint() to $targetvalue", 4)

	def runWhen = parent.settings?.arrowPause ?: 4		
	runIn(runWhen, "alterSetpoint", [data: [value:targetvalue], overwrite: true]) //when user click button this runIn will be overwrite
}

//called by tile when user hit raise temperature button on UI
void lowerSetpoint() {
	def mode = device.currentValue("thermostatMode")
	def targetvalue

	if (mode == "off" || (mode == "auto" && !usingSmartAuto() )) {
		LOG("lowerSetpoint(): this mode: $mode does not allow lowerSetpoint", 2, null, "warn")
    } else {
    	def heatingSetpoint = device.currentValue("heatingSetpoint")
		def coolingSetpoint = device.currentValue("coolingSetpoint")
		def thermostatSetpoint = device.currentValue("thermostatSetpoint").toDouble()
    	if (device.currentValue("thermostatOpertaingState") == 'idle') {
    		if (thermostatSetpoint == heatingSetpoint) {
        		heatingSetpoint = heatingSetpoint + device.currentValue("heatDifferential").toDouble() 	// correct from the display value
            	thermostatSetpoint = heatingSetpoint
            	coolingSetpoint = coolingSetpoint - device.currentValue("coolDifferential").toDouble()
        	} else if (thermostatSetpoint == coolingSetpoint) {
         		coolingSetpoint = coolingSetpoint - device.currentValue("coolDifferential").toDouble()
            	thermostatSetpoint = coolingSetpoint
            	heatingSetpoint = heatingSetpoint + device.currentValue("heatDifferential").toDouble()
        	} else {
          		heatingSetpoint = heatingSetpoint + device.currentValue("heatDifferential").toDouble()
            	coolingSetpoint = coolingSetpoint - device.currentValue("coolDifferential").toDouble()
        	}	
    	}
		LOG("lowerSetpoint() mode = ${mode}, heatingSetpoint: ${heatingSetpoint}, coolingSetpoint:${coolingSetpoint}, thermostatSetpoint:${thermostatSetpoint}", 4)

        if (thermostatSetpoint) {
			targetvalue = thermostatSetpoint
		} else {
			targetvalue = 0.0
		}

        if (getTemperatureScale() == "C" ) {
        	targetvalue = targetvalue.toDouble() - 0.5
        } else {
			targetvalue = targetvalue.toDouble() - 1.0
        }

		sendEvent("name":"thermostatSetpoint", "value":( wantMetric() ? targetvalue : targetvalue.round(0).toInteger() ), displayed: true)
		LOG("In mode $mode lowerSetpoint() to $targetvalue", 5, null, "info")

		// Wait 4 seconds before sending in case we hit the buttons again
		runIn(4, "alterSetpoint", [data: [value:targetvalue], overwrite: true]) //when user click button this runIn will be overwrite
	}
}

//called by raiseSetpoint() and lowerSetpoint()
void alterSetpoint(temp) {
	def mode = device.currentValue("thermostatMode")
	def heatingSetpoint = device.currentValue("heatingSetpoint")
	def coolingSetpoint = device.currentValue("coolingSetpoint")
    def thermostatSetpoint = device.currentValue("thermostatSetpoint")
    if (device.currentValue("thermostatOpertaingState") == 'idle') {
    	if (thermostatSetpoint == heatingSetpoint) {
        	heatingSetpoint = heatingSetpoint + device.currentValue("heatDifferential").toDouble() 	// correct from the display value
            thermostatSetpoint = heatingSetpoint
            coolingSetpoint = coolingSetpoint - device.currentValue("coolDifferential").toDouble()
        } else if (thermostatSetpoint == coolingSetpoint) {
         	coolingSetpoint = coolingSetpoint - device.currentValue("coolDifferential").toDouble()
            thermostatSetpoint = coolingSetpoint
            heatingSetpoint = heatingSetpoint + device.currentValue("heatDifferential").toDouble()
        } else {
          	heatingSetpoint = heatingSetpoint + device.currentValue("heatDifferential").toDouble()
            coolingSetpoint = coolingSetpoint - device.currentValue("coolDifferential").toDouble()
        }
    }
    def currentTemp = device.currentValue("temperature")
    def heatHigh = device.currentValue('heatHigh')
    def heatLow = device.currentValue('heatLow')
    def coolHigh = device.currentValue('coolHigh')
    def coolLow = device.currentValue('coolLow')
    def saveThermostatSetpoint = thermostatSetpoint
	def deviceId = getDeviceId()

	def targetHeatingSetpoint = heatingSetpoint
	def targetCoolingSetpoint = coolingSetpoing

	LOG("alterSetpoint - temp.value is ${temp.value}", 4)

	//step1: check thermostatMode
	if (mode == "heat"){
    	if (temp.value > heatHigh) targetHeatingSetpoint = heatHigh
        if (temp.value < heatLow) targetHeatingSetpoint = heatLow
		if (temp.value > coolingSetpoint){
			targetHeatingSetpoint = temp.value
			targetCoolingSetpoint = temp.value
		} else {
			targetHeatingSetpoint = temp.value
			targetCoolingSetpoint = coolingSetpoint
		}
	} else if (mode == "cool") {
		//enforce limits before sending request to cloud
    	if (temp.value > coolHigh) targetHeatingSetpoint = coolHigh
        if (temp.value < coolLow) targetHeatingSetpoint = coolLow
		if (temp.value < heatingSetpoint){
			targetHeatingSetpoint = temp.value
			targetCoolingSetpoint = temp.value
		} else {
			targetHeatingSetpoint = heatingSetpoint
			targetCoolingSetpoint = temp.value
		}
	} else if (mode == "auto" && usingSmartAuto() ) {
    	// Make changes based on our Smart Auto mode
        if (temp.value > currentTemp) {
        	// Change the heat settings to the new setpoint
            if (temp.value > heatHigh) targetHeatingSetpoint = heatHigh
        	if (temp.value < heatLow) targetHeatingSetpoint = heatLow
            LOG("alterSetpoint() - Smart Auto setting setpoint: ${temp.value}. Updating heat target")
            targetHeatingSetpoint = temp.value
            targetCoolingSetpoint = (temp.value > coolingSetpoint) ? temp.value : coolingSetpoint
		} else {
        	// Change the cool settings to the new setpoint
            if (temp.value > coolHigh) targetHeatingSetpoint = coolHigh
        	if (temp.value < coolLow) targetHeatingSetpoint = coolLow
			LOG("alterSetpoint() - Smart Auto setting setpoint: ${temp.value}. Updating cool target")
            targetCoolingSetpoint = temp.value

            LOG("targetHeatingSetpoint before ${targetHeatingSetpoint}")
            targetHeatingSetpoint = (temp.value < heatingSetpoint) ? temp.value : heatingSetpoint
            LOG("targetHeatingSetpoint after ${targetHeatingSetpoint}")

        }
    } else {
    	LOG("alterSetpoint() called with unsupported mode: ${mode}", 2, null, "warn")
        // return without changing settings on thermostat
        return
    }

	LOG("alterSetpoint >> in mode ${mode} trying to change heatingSetpoint to ${targetHeatingSetpoint} " +
			"coolingSetpoint to ${targetCoolingSetpoint} with holdType : ${whatHoldType()}")

	def sendHoldType = whatHoldType()
	//step2: call parent.setHold to send http request to 3rd party cloud    
	if (parent.setHold(this, targetHeatingSetpoint, targetCoolingSetpoint, deviceId, sendHoldType)) {
		sendEvent("name": "thermostatSetpoint", "value": temp.value.toString(), displayed: false)
		sendEvent("name": "heatingSetpoint", "value": targetHeatingSetpoint)
		sendEvent("name": "coolingSetpoint", "value": targetCoolingSetpoint)
		LOG("alterSetpoint in mode $mode succeed change setpoint to= ${temp.value}", 4)
	} else {
		LOG("WARN: alterSetpoint() - setHold failed. Could be an intermittent problem.", 1, null, "error")
        sendEvent("name": "thermostatSetpoint", "value": saveThermostatSetpoint.toString(), displayed: false)
	}
    // generateSetpointEvent()
	generateStatusEvent()
    // refresh data
    runIn(15, "poll")
}

// This just updates the generic multiAttributeTile - text should match the Thermostat mAT
def generateStatusEvent() {
	def mode = device.currentValue("thermostatMode")
	def heatingSetpoint = device.currentValue("heatingSetpoint")
	def coolingSetpoint = device.currentValue("coolingSetpoint")
	def temperature = device.currentValue("temperature")
    def operatingState = device.currentValue("thermostatOperatingState")

	def statusText	
	LOG("Generate Status Event for Mode = ${mode}", 4)
	LOG("Temperature = ${temperature}", 4)
	LOG("Heating setpoint = ${heatingSetpoint}", 4)
	LOG("Cooling setpoint = ${coolingSetpoint}", 4)
	LOG("HVAC Mode = ${mode}", 4)	
    LOG("Operating State = ${operatingState}", 4)

	if (mode == "heat") {
//		if (temperature >= heatingSetpoint) {
		if (operatingState == "fan only") {
        	statusText = "Fan Only"
        } else if (operatingState != "heating") {
			statusText = "Idle (Heat)"
		} else {
			statusText = "Heating to ${heatingSetpoint}°"
		}
	} else if (mode == "cool") {
//		if (temperature <= coolingSetpoint) {
		if (operatingState == "fan only") {
        	statusText = "Fan Only"
		} else if (operatingState != "cooling") {
			statusText = "Idle (Cool)"
		} else {
			statusText = "Cooling to ${coolingSetpoint}°"
		}
	} else if (mode == "auto") {
		if (operatingState == "fan only") {
        	statusText = "Fan Only"
    	} else if (operatingState == "heating") {
        	statusText = "Heating to ${heatingSetpoint}° (Auto)"
        } else if (operatingState == "cooling") {
        	statusText = "Cooling to ${coolingSetpoint}° (Auto)"
        } else {
			statusText = "Idle (Auto ${heatingSetpoint}°-${coolingSetpoint}°)"
        }
	} else if (mode == "off") {
		statusText = "Right Now: Off"
	} else if (mode == "emergencyHeat" || mode == "emergency heat" || mode == "emergency") {
    	if (operatingState != "heating") {
			statusText = "Idle (Emergency Heat)"
		} else {
			statusText = "Emergency Heating to ${heatingSetpoint}°"
		}
	} else {
		statusText = "${mode}?"
	}
	LOG("Generate Status Event = ${statusText}", 4)
	sendEvent(name:"thermostatStatus", value:statusText, description:statusText, displayed: false)
}

// generate custom mobile activity feeds event
// (Need to clean this up to remove as many characters as possible, else it isn't readable in the Mobile App
def generateActivityFeedsEvent(notificationMessage) {
	sendEvent(name: "notificationMessage", value: "${device.displayName} ${notificationMessage}", descriptionText: "${device.displayName} ${notificationMessage}", displayed: true)
}

def noOp() {
	// Doesn't do anything. Here due to a formatting issue on the Tiles!
}

def getSliderRange() {
	// should be returning the attributes heatRange and coolRange (once they are populated), but you can't get access to those while the forms are created (even after running for days).
	// return "'\${wantMetric()}'" ? "(5..35)" : "(45..95)"
    return "(5..90)" 
}

// Built in functions from SmartThings
// getTemperatureScale()
// fahrenheitToCelsius()
// celsiusToFahrenheit()

def wantMetric() {
	return (getTemperatureScale() == "C")
}

private def cToF(temp) {
    return celsiusToFahrenheit(temp)
}
private def fToC(temp) {
    return fahrenheitToCelsius(temp)
}

private def getImageURLRoot() {
	return "https://raw.githubusercontent.com/StrykerSKS/SmartThings/master/smartapp-icons/ecobee/dark/"
}

private def getDeviceId() {
	def deviceId = device.deviceNetworkId.split(/\./).last()	
    LOG("getDeviceId() returning ${deviceId}", 4)
    return deviceId
}

private def usingSmartAuto() {
	LOG("Entered usingSmartAuto() ", 5)
	if (settings.smartAuto) { return settings.smartAuto }
    if (parent.settings.smartAuto) { return parent.settings.smartAuto }
    return false
}

private def whatHoldType() {
	def sendHoldType = parent.settings.holdType ? (parent.settings.holdType=="Temporary" || parent.settings.holdType=="Until Next Program")? "nextTransition" : (parent.settings.holdType=="Permanent" || parent.settings.holdType=="Until I Change")? "indefinite" : "indefinite" : "indefinite"
	LOG("Entered whatHoldType() with ${sendHoldType}  settings.holdType == ${settings.holdType}")
	if (settings.holdType && settings.holdType != "") { return  holdType ? (settings.holdType=="Temporary" || settings.holdType=="Until Next Program")? "nextTransition" : (settings.holdType=="Permanent" || settings.holdType=="Until I Change")? "indefinite" : "indefinite" : "indefinite" }   
   
    return sendHoldType
}

private debugLevel(level=3) {
	def debugLvlNum
    if (device.currentValue("debugLevel")) {
		debugLvlNum = device.currentValue("debugLevel").toInteger() ?: parent.settings.debugLevel?.toInteger() ?: 3
    } else {
    	debugLvlNum = parent.settings.debugLevel?.toInteger() ?: 3
    }
    def wantedLvl = level?.toInteger()
    
    return ( debugLvlNum >= wantedLvl )
}

private def LOG(message, level=3, child=null, logType="debug", event=false, displayEvent=false) {
	def prefix = ""
	if ( parent.settings.debugLevel?.toInteger() == 5 ) { prefix = "LOG: " }
	if ( debugLevel(level) ) { 
    	log."${logType}" "${prefix}${message}"
        // log.debug message
        if (event) { debugEvent(message, displayEvent) }        
	}    
}

private def debugEvent(message, displayEvent = false) {
	def results = [
		name: "appdebug",
		descriptionText: message,
		displayed: displayEvent
	]
	if ( debugLevel(4) ) { log.debug "Generating AppDebug Event: ${results}" }
	sendEvent (results)
}

def getTempColors() {
	def colorMap

	colorMap = [
		// Celsius Color Range
		[value: 0, color: "#1e9cbb"],
		[value: 15, color: "#1e9cbb"],
		[value: 19, color: "#1e9cbb"],

		[value: 21, color: "#44b621"],
		[value: 22, color: "#44b621"],
		[value: 24, color: "#44b621"],

		[value: 21, color: "#d04e00"],
		[value: 35, color: "#d04e00"],
		[value: 37, color: "#d04e00"],
		// Fahrenheit Color Range
		[value: 40, color: "#1e9cbb"],
		[value: 59, color: "#1e9cbb"],
		[value: 67, color: "#1e9cbb"],

		[value: 69, color: "#44b621"],
		[value: 72, color: "#44b621"],
		[value: 74, color: "#44b621"],

		[value: 76, color: "#d04e00"],
		[value: 95, color: "#d04e00"],
		[value: 99, color: "#d04e00"],
        
        [value: 451, color: "#ffa81e"] // Nod to the book and temp that paper burns. Used to catch when the device is offline
	]
}

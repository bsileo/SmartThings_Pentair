# SmartThings_Pentair
Smarthings UI for use with Pentair Pool Controller

# License

SmartThings_Pentair.  An application to control pool equipment from within Smartthings.
Copyright (C) 2016, 2017.  Brad Sileo, bsileo.  brad@sileo.name

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU Affero General Public License as
published by the Free Software Foundation, either version 3 of the
License, or (at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU Affero General Public License for more details.

You should have received a copy of the GNU Affero General Public License
along with this program.  If not, see <http://www.gnu.org/licenses/>.


## What is nodejs-poolController



<img src="https://github.com/bsileo/SmartThings_Pentair/blob/master/SmartthingsPoolControlScreenshot.png" height="300">

***

## Installation Instructions

1. Install and configure Nodejs-Poolcontroller (version 4.x+)
          https://github.com/tagyoureit/nodejs-poolController
2. Update your Nodejs-Poolcontroller installation with the Smartthings interface
  a. Copy the outputToSmarthings.js into the integrations directory
  b. Update your configuration file to refernce it:  
     
	 "integrations": {
          "socketISY": 0,
          "outputSocketToConsoleExample": 0,
		  "outputToSmartThings":1
         }

      "outputToSmartThings": {
		"address": "<x.x.x.x>", (IP Address of Smartthings HUB on LAN)
		"port": "39500"
	}
		 

3. Install the new Device Handlers into the Smartthings IDE (http://graph.api.smartthings.com/)
   a. Pentair Pool Controller
   b. Pentair Water Thermostat
   c. Pentair Pool Control Switch
   d. Pentair Chlorinator
   For each one, 
      a) Go to https://graph.api.smartthings.com/ide/devices
      b) Hit the "+New Device Type" at the top right corner
      c) Hit the "From Code" tab on the left corner
      d) Copy and paste the code from
         https://github.com/bsileo/SmartThings_Pentair/tree/master/devicetypes/bsileo/
      e) Hit the create button at the bottom
      f) Hit the "publish/for me" button at the top right corner (in the code window)

4. Install a new device of type Pentair Pool Controller and configure it
    Controller IP and Port - Set these to match the device where you have nodejs-PoolController running
	Controller MAC Address - set this to the MAC address for that device. Use all uppercase, no colins to enter it
	Autoname - optionally set this to true. The Device will do a one-time rename of all the Circuits ot match the names set in your Controller
5. Manually adjust the circuit devices
    You will have 8 new devices created as Children to the main pool controller. You can access these in your things view and adjust them to improve the experience by changing their names as well as their icons. 
6. Use all of the devices from the pool controlle rin any of your other Smartapps. You will have access to all Temperature readings, the ability to turn automated relay swicthes on and off, and ocntorl the poll and spa pumps. 
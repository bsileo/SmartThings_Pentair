# SmartThings_Pentair
Smarthings UI for use with the nodjs-PoolController

# License

SmartThings_Pentair.  
An application to control pool equipment from within Smartthings.
Copyright (C) 2017-2018  Brad Sileo / bsileo / brad@sileo.name

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


## What is the Smartthings Pentair Pool Controller?
A collection of devices designed to interface with a nodejs-poolControlller instance which is talking on the RS-485 bus to allow viewing and setting pool control options. Includes devices to manage the Pool pump, lights and heater, the spa pump and heater, the chlorinator, and any installed additional "Features". 


<img src="https://github.com/bsileo/SmartThings_Pentair/blob/master/SmartthingsPoolControlScreenshot-IB.jpg" height="300">

***

## Installation Instructions

1. Install and configure Nodejs-Poolcontroller (version 5.0+)
          https://github.com/tagyoureit/nodejs-poolController/tree/v5.0.0
2. Update your Nodejs-Poolcontroller installation with the SmartThings interface:
   
   Update your configuration file to enable "OutputToSmartThings". Note that the "*" in the Address of the HUB correct as this is dynamically detected.
	 ```"integrations": {
          "socketISY": 0,
          "outputSocketToConsoleExample": 0,
		  "outputToSmartThings":1
          }
		  
         "outputToSmartThings": {
		"address": "*", 
		"port": "39500"
	    }```
3. Install the new SmartApp and Devices into the Smartthings IDE (http://graph.api.smartthings.com/)

    **Github Approach (preferred)**
    1. Add the repository to your Smartthings IDE. Go to [My Device Handlers](https://graph.api.smartthings.com/ide/devices) and click "Settings"
    2. In the blank line at the bottom add:
  
      **Owner | Name | Branch**
      
	  bsileo | SmartThings_Pentair | Master
	  
    3. Click OK
    4. Click "Update from Repo" and select SmartThings_Pentair
    5. In the dialog which appears select all items in the "New (only in GitHub)" group. Click the "Publish" checkbox. Click Execute Update.
    6. Repeat the "Update from Repo" from Steps 4-5 on [My SmartApps](https://graph.api.smartthings.com/ide/apps)

   **Manual Approach**

   - Pool Controller
		1. Go to [https://graph.api.smartthings.com/ide/apps]
		2. Hit the "+New SmartApp" at the top right corner
		3. Hit the "From Code" tab on the left corner
		4. Copy and paste the code from https://github.com/bsileo/SmartThings_Pentair/blob/master/smartapps/bsileo/pool-controller.src/pool-controller.groovy
		5. Hit the create button at the bottom
		6. Hit the "publish/for me" button at the top right corner (in the code window)


    - Install the new Device Handlers into the Smartthings IDE (http://graph.api.smartthings.com/)
	   - Pentair Pool Controller
	   - Pentair Water Thermostat
	   - Pentair Pool Control Switch
	   - Pentair Chlorinator


		 For each one, 

		 1. Go to [https://graph.api.smartthings.com/ide/devices]
		 2. Hit the "+New Device Type" at the top right corner
		 3. Hit the "From Code" tab on the left corner
		 4. Copy and paste the code from https://github.com/bsileo/SmartThings_Pentair/tree/master/devicetypes/bsileo/
		 5. Hit the create button at the bottom
		 6. Hit the "publish/for me" button at the top right corner (in the code window)

   
   
4. Install a new SmartApp Pool Controller and configure it:
    - Go to MarketPlace, click SmartApps
    - Go to My Apps and locate Pool Controller
    - Allow discovery to find your PoolController node.js server, or manually configure it:
       	* Controller IP and Port - Set these to match the device where you have nodejs-PoolController running
    	* Controller MAC Address - set this to the MAC address for that device. Use all uppercase, no colins to enter it
        * NOTE: Initial setup of the Pool Controller device takes longer than the hard timeout of 20 seconds in SmartThings. This will lead to an error when trying to Save from the SmartApp page.  Ignore the error and keep hitting save! It will progressivly make progress creating additional child devices until it finishes. this can take 5-10 times to get through them all. 

5. Manually adjust the circuit devices
    You will have 8 new devices created as Children to the main pool controller. You can access these in your things view and adjust them to improve the experience by changing their names as well as their icons. You cna also go to the Pol COntroler settings, enable Autoname, and hit Refresh to name them all from your PoolController. Autoname will only run once and thus you can change the labels after this if desired.
	
6. Use all of the devices from the pool controller in any of your other Smartapps. You will have access to all Temperature readings, the ability to turn automated relay swicthes on and off, and control the pool and spa pumps as well as set colors and modes for Intellibrite lights.

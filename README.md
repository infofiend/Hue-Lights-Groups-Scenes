# Hue-Lights-Groups-Scenes
SmartThings SmartApps &amp; Device Handlers


I've created a Hue Service Manager that finds and installs (1) individual Hue Lights, (2) Hue Groups, and (3) Hue Scenes.

I say "created" above only in the sense that I modified the amazing work of zpriddy - who created the Hue Lights and Groups (Connect) SmartApp.



Installation Instructions
--------
1. If you already have an existing Hue Service Manager and Hue Lights in your SmartThings system, completely remove the app and all lights.
2. Install and publish the service manager SmartApp into your IDE. You can find the code here2
3. Install and publish the 5 device type handlers into your IDE. You can find the code for each here3
4. In the SmartThings App, go to the "My Apps" section of the SmartApps Marketplace, and install the "Hue Lights and Groups and Scenes (OH MY)" SmartApp.
5. In that app, find and authorize your Hue Hub. Once that is done, there is a separate page for finding and selecting Hub Bulbs, Hue Groups, and Hue Scenes.

Use
--------
1. The Lights work as normal.
2. The Groups work just like the lights do.
3. The Scenes are set up as Momentary Switches. Just press the button and all the lights in the scene are adjusted all at once (and very fast)!
4. The Scenes have a "setScene" function that can be used via other SmartApps. The parameter for the setScene() function is "groupID" - which is the number that your Hue Hub assigned to that group. Use 0 for all lights. So, if you wanted to apply a scene you set up called "Movies" and only have it applied to the group you set up with id of 1 (maybe named "Living Room"), then you would call Movies.setScene(1). If you wanted that scene's settings to apply to all lights in the scene, then you would call Movies.setScene(0).

Known Issue
--------
1. Due to the 20 second limit for routines, you need to limit the number of devices (meaning lights, groups, and scenes) that you install at a single time. Typically less than 20 works for me. This does not mean that you cannot have more than 20 total, just that you will need to go into the "Hue Lights and Groups and Scenes (OH MY)" SmartApp again once you have set it up.

Etc...
1. I'll update this post with more info / screenshots later -- just wanted to distribute this now.


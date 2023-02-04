# Geothermal heating controller



This is a program to control a heat pump and make it work harder when the energy price is low.
It gets two APIs: one for the energy price for every hour of the current day,
and the other for the weather.
Then it determines whether it is low enough to send out a Raspberry Pi output
into the controller of the heating system.


The pump operates mostly automatically, but when the energy price falls below a certain threshold, the Raspberry Pi will activate and
override it and change the goal temperature by the value set on the display of the heat central.


I will add a phone app so it is possible to check the price and change the threshold easily.


Some code is commented out because RPI.GPIO needs a raspberry to function.


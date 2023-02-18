# -- coding: utf-8 --
"""

This is a program to control a heat pump and make it work harder when the energy price is low.
It gets two APIs: one for the energy price for every hour of the current day,
and the other for the weather.
Then it determines if it's low enough to send the output of a Raspberry Pi
into the controller of the heating system.

Some code is commented out because RPI.GPIO needs a raspberry to function.

"""

import json
from datetime import datetime, date
import time
import requests
import schedule
from threading import Thread

import matplotlib.pyplot as plt
import pandas as pd
import numpy as np
#import RPi.GPIO as GPIO


#GPIO.setmode(GPIO.BCM)


def main(): # Main funcions
    #GPIO.cleanup()
    energy_data = get_data()
    write_to_json(energy_data)
    price_threshold, temperature_threshold, data, temp_c = extract_data()
    process_data(price_threshold, temperature_threshold, data, temp_c,)
    plot_data()
    


def get_data(): # Gets current energy price data in Sweden's energy zone 3. 
    today = date.today()
    year = today.strftime("%Y")
    month = today.strftime("%m")
    day = today.strftime("%d")
    url = f"https://www.elprisetjustnu.se/api/v1/prices/{year}/{month}-{day}_SE3.json"
    try:
        response = requests.get(url, timeout=20)
        return response.json()
    except requests.exceptions.RequestException as error:
        print("An error occurred:", error)


def get_weather(): # Gets the current day's forecast.
    with open('api_key.txt', encoding="utf8") as read_file:
        api_key = read_file.read().strip().split("=")[1]
        # Gets the weather data.
        url = f"http://api.weatherapi.com/v1/current.json?key={api_key}&q=kinna&aqi=no"
    try:
        response = requests.get(url, timeout=20)
        return response.json()
    except requests.exceptions.RequestException as error:
        print("An error occurred:", error)


def write_to_json(el_data):
    with open("price.json", "w", encoding="utf8") as outfile:

        json.dump(el_data, outfile)


def extract_data(): # Calculate the percentile to get the lowest prices for the day.
    today_price_list = []
    with open('price.json', mode='r', encoding="utf8") as read_file:
        data = json.load(read_file)
    for item in data:
        result = item['SEK_per_kWh']
        today_price_list.append(result)

    numpy_today_price = np.array(today_price_list)
    price_threshold = np.percentile(numpy_today_price, 20) # Lowest 20%
    weather_data = get_weather()
    temp_c = weather_data["current"]["temp_c"]
    temperature_threshold = 16 # Will make it possible to set it on the phone app.
    return price_threshold, temperature_threshold, data, temp_c

# Loops thru the json file and find the matching hour with the current hour.
def process_data(price_threshold, temperature_threshold, data, temp_c):
    for item in data:
        start_time = item["time_start"]
        price_kwh = item["SEK_per_kWh"]
        reformated_time = datetime.strptime(start_time, "%Y-%m-%dT%H:%M:%S%z")
        hour = reformated_time.hour
        current_hour = datetime.now().hour

    # check if the timestart matches the current time
        if current_hour == hour:
            # Looks for if the price is low and temperature is low so the pump can work
            if price_kwh <= price_threshold and temp_c <= temperature_threshold:
                #GPIO.output(18, GPIO.HIGH)
                app_data = {
                "status": "Kör med extern styrning",
                "Pris per kwh": price_kwh,
                "Pris gräns": price_threshold,
                "Ute temperatur": temp_c,
                "Temperatur gräns": temperature_threshold
                }
                with open("app_data.json", "w") as outfile:
                    json.dump(app_data, outfile)    
            else:
                app_data = {
                "status": "Körs inte med extern styrning",
                "Pris per kwh": price_kwh,
                "Pris gräns": price_threshold,
                "Ute temperatur": temp_c,
                "Temperatur gräns": temperature_threshold
                }
                with open("app_data.json", "w") as outfile:
                    json.dump(app_data, outfile)
        else:
            pass



def plot_data():
    plt.style.use('dark_background')
    with open('price.json', 'r', encoding="utf8") as outfile:
        data = json.load(outfile)
        data_frame = pd.DataFrame(data)
        data_frame['hour'] = data_frame['time_start'].apply(lambda x:
        datetime.strptime(x, '%Y-%m-%dT%H:%M:%S%z').hour)
        data_frame.plot(x='hour', y="SEK_per_kWh")
        plt.xticks(np.arange(0, 24, 1))
        plt.show()



schedule.every().second.do(main) # This code will run every hour
while True:
    schedule.run_pending()
    time.sleep(5)



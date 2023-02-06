from flask import *
import json, time

app = Flask(__name__)

@app.route("/", methods=["GET"])
def home_page():
    data_set={"Page": "Home", "Message": "Successfully loaded the home page"}


from flask import Flask, request, send_file
import json
import logging

app = Flask(__name__)

logging.basicConfig(level=logging.INFO)

@app.route('/', methods=['GET'])
def get_data():
    # Your code to retrieve the data goes here
    # For example: 
    with open('app_data.json', mode='r', encoding="utf8") as read_file:
            data = json.load(read_file)
            client_ip = request.remote_addr
            app.logger.info(f'Request received from {client_ip}')
            return (data)

@app.route('/graph')
def graph():
    return send_file('graph.png', mimetype='image/png')


@app.route('/threshold', methods=['POST'])
def set_threshold():
    threshold = request.json['threshold'] 
    with open('threshold.txt', mode='w', encoding="utf8") as file:
        file.write(str(threshold))
    app.logger.info(f'Threshold set to {threshold}')
    return 'OK'


if __name__ == '__main__':
    from waitress import serve
    serve(app, host='192.168.0.188', port=7777, url_scheme='https')


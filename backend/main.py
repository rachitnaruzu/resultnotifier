from flask import Flask,request, abort
import os
import datetime
import hashlib
from databaseutil import DatabaseUtility
import json
import hmac
import base64
from datetime import datetime as dt, timedelta as td
import config

dbutil = DatabaseUtility()
app = Flask(__name__)

KEY = config.SECRET_KEY # should be same as that in app
NONCE_ALIVE_TIME = 2 * 60 #seconds

def generate_hash(registration_id):
    return str(hashlib.sha256(registration_id.encode()).hexdigest())
    
@app.errorhandler(404)
def page_not_found(error):
    return "{\"error\",\"NOT FOUND\"}",404

def registerid(registration_id):
    hash = generate_hash(registration_id)
    if dbutil.is_present(hash, registration_id):
        return '{\"status\":\"Already Present\"}'
    dbutil.add_id(hash, registration_id)
    return '{\"status\":\"Added\"}'
    
def get_files():
    return {'files' : dbutil.get_files(int(request.form['offset']), request.form['datatypes']) }
    

def get_topmost_files():
    return {'files' : dbutil.get_topmost_files(int(request.form['offset']), request.form['datatypes']) }
    
def get_recent_files():
    return {'files' : dbutil.get_recent_files(int(request.form['offset']), request.form['datatypes']) }


def authenticate():
    if not ('sec' in request.form and 'sig' in request.form):
        abort(404)
    sec = request.form['sec']
    sig = request.form['sig']
    ts = base64.urlsafe_b64decode(sec.encode())
    try:
        nonce_creation_time = dt.strptime(ts.decode(), "%Y-%m-%d %H:%M:%S")
    except Exception:
        abort(404)
    if nonce_creation_time + td(seconds = NONCE_ALIVE_TIME) < dt.utcnow():
        abort(404)
    h = hmac.new(KEY.encode(), sec.encode(), digestmod=hashlib.sha256)
    if sig != h.hexdigest():
        abort(404)
    

@app.route('/register/',methods=['POST'])
def register():
    authenticate()
    registration_id = request.form['registration_id']
    return registerid(registration_id)
  
@app.route('/published/',methods=['POST'])  
def published():
    authenticate()
    return json.dumps(get_files())

@app.route('/datatypes/',methods=['POST'])  
def get_data_types():
    authenticate()
    return json.dumps({'datatypes':dbutil.get_data_types()})

@app.route('/topmost/',methods=['POST'])  
def topmost():
    authenticate()
    return json.dumps(get_topmost_files())
    
@app.route('/updateselfviews/',methods=['POST'])
def updateselfviews():
    authenticate()
    selfviews = json.loads(request.form['selfviews'])
    newviews = {}
    for file in selfviews:
        dbutil.updatefileviews(file['fileid'], int(file['selfviews']))
        newviews['fileid'] = dbutil.getfileviews(file['fileid'])
    return json.dumps(newviews)
    
@app.route('/incrementviews/',methods=['POST'])
def incrementviews():
    authenticate()
    fileid = request.form['fileid']
    dbutil.updatefileviews(fileid, 1)
    return '{"success":"' + fileid + ' views incremented by one"}'
        
@app.route('/recent/',methods=['POST'])  
def recent():
    authenticate()
    return json.dumps(get_recent_files())

@app.route('/')    
def get_time():
    now = datetime.datetime.now()
    nowStr = str(now.year) + "-" + str(now.month) + "-" + str(now.day) + " " + str(now.hour) + " " + str(now.minute) + " " + str(now.second)
    return nowStr

if __name__ == '__main__':
    port = int(os.environ.get("PORT", 8000))
    app.run(host='0.0.0.0', port=port,debug=True)
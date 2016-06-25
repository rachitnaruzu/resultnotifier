import json
import requests as rq
import datetime
from databaseutil import DatabaseUtility
import extractbeta as ex
import config

contentType = "application/json"
API_Key = "key=" + config.GCM_API_KEY
url = "https://android.googleapis.com/gcm/send"
headers = {'Content-Type':contentType,'Authorization':API_Key}


dbutil = DatabaseUtility()
			
def generate_notifications(files):
    notifications = []
    now = datetime.datetime.utcnow()
    nowdate = str(now.year) + "-" + str(now.month) + "-" + str(now.day) + " " + str(now.hour) + ":" + str(now.minute) + ":" + str(now.second)
    for file in files:
        if not dbutil.is_file_present(file['fileid']):
            dbutil.insert_file(
                    fileid = file['fileid'], 
                    displayname = file['displayname'], 
                    url = file['url'], 
                    datatype = file['datatype'], 
                    filetype = file['filetype'], 
                    datecreated = nowdate,
                    views = 0
                )
            notifications.append({
                    'fileid' : file['fileid'], 
                    'displayname' : file['displayname'], 
                    'url' : file['url'], 
                    'datatype' : file['datatype'], 
                    'filetype' : file['filetype'], 
                    'datecreated' : nowdate,
                    'views' : 0
                })
    print(notifications)
    return notifications

def get_registration_ids():
    registration_ids = dbutil.get_all_registrationids()
    return registration_ids
    
def send(notifications, registration_ids):
    for notification in notifications:
        data = {'registration_ids':registration_ids,'data':notification}
        print(resp.status_code, resp.reason)

def send_notifications(registration_ids,notifications):
    length = len(registration_ids)
    offset = 0
    mod = 997
    iter = int(length/mod)
    for it in range(0,iter):
        send(notifications,registration_ids[offset:offset+mod])
        offset += mod
    rem = length%mod
    send(notifications,registration_ids[offset:offset+rem])

def fetch_and_generate():
    newdata = ex.fetch()
    notifications = generate_notifications(newdata)
    registration_ids = get_registration_ids()
    send_notifications(registration_ids, notifications)
    dbutil.close_connection()
    return "notifications successfully sent."
    
fetch_and_generate()
    
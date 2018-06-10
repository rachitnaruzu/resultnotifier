import json
import requests as rq
import datetime
from databaseutil import DatabaseUtility
import extractbeta as ex
import config
from oauth2client import service_account as sa

CONTENT_TYPE = "application/json"
API_KEY = "Bearer " + config.GCM_API_KEY
FCM_SEND_URL = "https://fcm.googleapis.com/v1/projects/" + config.PROJECT_NAME + "/messages:send"
FCM_SCOPES = ['https://www.googleapis.com/auth/firebase.messaging']


def get_and_save_new_files(files, dbutil):
    new_files = []
    now = datetime.datetime.utcnow()
    nowdate = str(now.year) + "-" + str(now.month) + "-" + str(now.day) + " " + str(now.hour) + ":" + str(now.minute) \
              + ":" + str(now.second)
    saved_fileids = set(dbutil.get_all_fileids())
    for file in files:
        if file['fileid'] not in saved_fileids:
            dbutil.insert_file(
                fileid=file['fileid'],
                displayname=file['displayname'],
                url=file['url'],
                datatype=file['datatype'],
                filetype=file['filetype'],
                datecreated=nowdate,
                views=0
            )
            new_files.append(file)

    print("New files=" + str(new_files))
    return new_files


def get_registration_ids(dbutil):
    registration_ids = dbutil.get_all_registrationids()
    return registration_ids


def get_access_toke():
    credentials = sa.ServiceAccountCredentials.from_json_keyfile_name(config.PROJECT_FCM_JSON_FILE, scopes=FCM_SCOPES)
    access_token_info = credentials.get_access_token()
    access_token = access_token_info.access_token
    print("access_toke=" + access_token)
    return access_token

def is_registration_id_not_registered(fcm_resp):
    status_code = fcm_resp.status_code
    if status_code == 404:
        return True


def send_fcm_notifications(registration_ids, new_files):
    access_token = get_access_toke()
    headers = {'Content-Type': CONTENT_TYPE, 'Authorization': 'Bearer ' + access_token}
    unregistered_ids = []
    for file in new_files:
        for registration_id in registration_ids:
            title = 'New ' + file['datatype'] + ' available'
            body = file['displayname']
            notification = {'title': title, 'body': body}
            data = {'message': {'token': registration_id, 'notification': notification}}
            print("sending notification=" + str(data))
            resp = rq.post(FCM_SEND_URL, data=json.dumps(data), headers=headers)
            print("notification response=" + resp.text)
            if (is_registration_id_not_registered(resp)):
                unregistered_ids.append(registration_id)

    print("Unregistered IDs=" + str(unregistered_ids))
    return unregistered_ids

def remove_unregistered_ids(dbutil, unregistered_ids):
    if len(unregistered_ids) == 0:
        return

    dbutil.delete_users(unregistered_ids)

def fetch_and_generate():
    dbutil = DatabaseUtility()
    fetched_files = ex.fetch()
    new_files = get_and_save_new_files(fetched_files, dbutil)
    registration_ids = get_registration_ids(dbutil)
    unregistered_ids = send_fcm_notifications(registration_ids, new_files)
    remove_unregistered_ids(dbutil, unregistered_ids)
    dbutil.close_connection()
    return "notifications successfully sent."


fetch_and_generate()

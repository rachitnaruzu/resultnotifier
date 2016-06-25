import os
import psycopg2

INSERT_CMD = "INSERT INTO users (hash,registrationid) VALUES (%s,%s);"
FETCH_CMD1 = "SELECT registrationid FROM users;"
FETCH_CMD2 = "SELECT registrationid FROM (SELECT registrationid FROM users WHERE hash = %s) AS Temp WHERE registrationid = %s;"
FILE_PRESENCE_CMD = "SELECT fileid FROM files WHERE fileid = %s;"
INSERT_FILE_CMD = "INSERT INTO files (fileid,displayname,url,datatype,filetype,datecreated,views) VALUES (%s, %s, %s, %s, %s,%s, %s);"

GET_FILES_CMD = "SELECT fileid,displayname,url,datatype,filetype,datecreated,views FROM files ORDER BY datecreated DESC, displayname OFFSET %s LIMIT 10;"
GET_FILES_FILTER_CMD_PREFIX = "SELECT fileid,displayname,url,datatype,filetype,datecreated,views FROM files WHERE datatype IN "
GET_FILES_FILTER_CMD_SUFFIX = " ORDER BY datecreated DESC, displayname OFFSET %s LIMIT 10;"

GET_TOPMOST_FILES_CMD = """ SELECT fileid,displayname,url,datatype,filetype,datecreated,
views FROM files WHERE views > 0 ORDER BY views DESC, displayname OFFSET %s LIMIT 10; """
GET_TOPMOST_FILES_FILTER_CMD_PREFIX = "SELECT fileid,displayname,url,datatype,filetype,datecreated,views FROM files WHERE datatype IN "
GET_TOPMOST_FILES_FILTER_CMD_SUFFIX = " AND views > 0 ORDER BY views DESC, displayname OFFSET %s LIMIT 10;"

GET_FILE_VIEWS_CMD = "SELECT fileid,views FROM files WHERE fileid = %s;"
UPDATE_FILE_VIEWS = "UPDATE files SET views = views + %s WHERE fileid = %s;"

GET_ALL_DATA_TYPES = "SELECT DISTINCT(datatype) FROM files;"

ALPHA = "0.5"
UPDATE_AVGHITS_CMD = "UPDATE files SET oldviews = Temp.views, avghits = " + ALPHA + " * Temp.hits + " + ALPHA + " * avghits FROM (SELECT fileid, views, (views - oldviews) as hits FROM files) as Temp WHERE files.fileid = Temp.fileid;"

GET_RECENT_FILES_CMD = """ SELECT fileid,displayname,url,datatype,filetype,datecreated,
views FROM files WHERE avghits > 0 ORDER BY avghits DESC, views OFFSET %s LIMIT 10; """
GET_RECENT_FILES_FILTER_CMD_PREFIX = "SELECT fileid,displayname,url,datatype,filetype,datecreated,views FROM files WHERE datatype IN "
GET_RECENT_FILES_FILTER_CMD_SUFFIX = " AND avghits > 0 ORDER BY avghits DESC, displayname OFFSET %s LIMIT 10;"

class DatabaseUtility(object):

    def make_connection(self):
        self.conn = psycopg2.connect(
            database = config.DATABASE_NAME,
            user = config.DATABASE_URL,
            password = config.DATABASE_PASSWORD,
            host = config.DATABASE_HOST,
            port = config.DATABASE_PORT
        )
        self.cur = self.conn.cursor()
        self.conn.autocommit = True
    
    '''
    def create_table(cur):
        cur.execute(CREATE_CMD)
    '''
    
    def commit(self):
        self.conn.commit()
    
    def update_avghits(self):
        self.cur.execute(UPDATE_AVGHITS_CMD)
        
    def get_recent_files(self, offset, datatypes):
        if datatypes == "()":
            self.cur.execute(GET_RECENT_FILES_CMD, (offset,))
        else:
            self.cur.execute(GET_RECENT_FILES_FILTER_CMD_PREFIX + datatypes + GET_RECENT_FILES_FILTER_CMD_SUFFIX, (offset,))
        rows = self.cur.fetchall()
        files = [{
                    'fileid' : row[0], 
                    'displayname' : row[1], 
                    'url' : row[2], 
                    'datatype' : row[3], 
                    'filetype' : row[4], 
                    'datecreated' : str(row[5]),
                    'views' : row[6]
                } for row in rows]
        return files
    
    def get_files(self, offset, datatypes):
        if datatypes == "()":
            self.cur.execute(GET_FILES_CMD, (offset,))
        else:
            self.cur.execute(GET_FILES_FILTER_CMD_PREFIX + datatypes + GET_FILES_FILTER_CMD_SUFFIX, (offset,))
        rows = self.cur.fetchall()
        files = [{
                    'fileid' : row[0], 
                    'displayname' : row[1], 
                    'url' : row[2], 
                    'datatype' : row[3], 
                    'filetype' : row[4], 
                    'datecreated' : str(row[5]),
                    'views' : row[6]
                } for row in rows]
        return files
        
    def get_data_types(self):
        self.cur.execute(GET_ALL_DATA_TYPES)
        rows = self.cur.fetchall()
        return [row[0] for row in rows]
        
    def getfileviews(self, fileid):
        self.cur.execute(GET_FILE_VIEWS_CMD, (fileid, ))
        row = self.cur.fetchone()
        return row[1]
        
    def updatefileviews(self, fileid, selfviews):
        self.cur.execute(UPDATE_FILE_VIEWS, (selfviews, fileid))
        
    def get_topmost_files(self, offset, datatypes):
        if datatypes == "()":
            self.cur.execute(GET_TOPMOST_FILES_CMD, (offset,))
        else:
            self.cur.execute(GET_TOPMOST_FILES_FILTER_CMD_PREFIX + datatypes + GET_TOPMOST_FILES_FILTER_CMD_SUFFIX, (offset, ))
        rows = self.cur.fetchall()
        files = [{
                    'fileid' : row[0], 
                    'displayname' : row[1], 
                    'url' : row[2], 
                    'datatype' : row[3], 
                    'filetype' : row[4], 
                    'datecreated' : str(row[5]),
                    'views' : row[6]
                } for row in rows]
        return files
    
    def get_all_registrationids(self):
        self.cur.execute(FETCH_CMD1)
        rows = self.cur.fetchall()
        files = [row[0] for row in rows]
        return files
    
    def is_present(self, hash, registration_id):
        self.cur.execute(FETCH_CMD2,(hash,registration_id))
        if(self.cur.fetchone()):
            return True
        return False    
    
    def is_file_present(self, fileid):
        self.cur.execute(FILE_PRESENCE_CMD,(fileid,))
        if(self.cur.fetchone()):
            return True
        return False
        
    def insert_file(self,fileid, displayname, url, datatype, filetype, datecreated, views):
        self.cur.execute(INSERT_FILE_CMD,(fileid, displayname, url, datatype, filetype, datecreated, views))
    
    def add_id(self, hash, registration_id):
        self.cur.execute(INSERT_CMD,(hash,registration_id))
        
    def close_connection(self):
        self.conn.commit()
        self.cur.close()
        self.conn.close()
        
    def __init__(self):
        self.make_connection()
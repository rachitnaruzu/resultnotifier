from databaseutil import DatabaseUtility
dbutil = DatabaseUtility()
dbutil.update_avghits()
dbutil.cur.execute("SELECT displayname, oldviews, views, avghits FROM files ORDER BY avghits DESC LIMIT 10;")
for row in dbutil.cur.fetchall():
    print(row)
import hashlib
from databaseutil import DatabaseUtility

def generate_hash(registration_id):
    return str(hashlib.sha256(registration_id.encode()).hexdigest());

def register(registration_id):
    hash = generate_hash(registration_id)
    dbutil = DatabaseUtility()
    if dbutil.is_present(hash, registration_id):
        return '{\"status\":\"Already Present\"}'
    dbutil.add_id(hash, registration_id)
    dbutil.close_connection()
    return '{\"status\":\"Added\"}'
    
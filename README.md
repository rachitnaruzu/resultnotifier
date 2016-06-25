ResultNotifier
==============

ResultNotifier is an open source android app, 
which can be used by universities/colleges to notify the users 
about the results, notices, schedules etc. Some of the features of this app are:

- Notify the users when data (result, notice, schedule) arrives.
- Displays the most viewed data.
- Displays the most recently viewed (implemented trend with decay) data.

![mainFragment](/images/mainFragment.JPG) ![multiChoice](/images/multiChoice.JPG) ![navDrawer](/images/navDrawer.JPG)

Installation (Backend)
----------------

**Note:** This installation procedure is meant for Ubuntu OS. 
Replace all the text inclosed with <> with actual values before executing the statements. 
Better execute the statements line by line rather than copy pasting the whole block. 
You might have to replace all 'localhost' text with '0.0.0.0' if you are deploying it on digitalocean or on some other online server. 
This installation assumes that you have alreasy installed python, pip and virtualenv \(sudo pip install virtualenv\).

#### Step 1 : [Implement extractbeta.py]

open /var/resultnotifier/backend/extractbeta.py

implement the method fetch

#### Step 2 : [Installing PostgreSQL database]

We will use PostgreSQL database with resultnotifier. The PostgreSQL installation procedure is taken from [this](https://www.digitalocean.com/community/tutorials/how-to-use-postgresql-with-your-django-application-on-ubuntu-14-04) link:

    sudo apt-get update
	sudo apt-get install postgresql postgresql-contrib
	# switch user to postgres
	sudo su - postgres
    psql
    # execute following sql commands in psql
      CREATE DATABASE resultnotifier;
      CREATE USER resultnotifier_user WITH PASSWORD '<RESULTNOTIFIER_DATABASE_PASSWORD>';
      ALTER ROLE resultnotifier_user SET client_encoding TO 'utf8';
      ALTER ROLE resultnotifier_user SET default_transaction_isolation TO 'read committed';
      ALTER ROLE resultnotifier_user SET timezone TO 'UTC';
      GRANT ALL PRIVILEGES ON DATABASE resultnotifier TO resultnotifier_user;
      \q
    exit
   
    
#### Step 3: [Setting up resultnotifier platform]

    cd /var
    sudo git clone https://github.com/rachitnaruzu/resultnotifier.git
    
open /var/resultnotifier/backend/config.py:
  
    sudo vim /var/resultnotifier/backend/config.py
    
set the following variables:
 
    DATABASE_USER = '<DATABASE_USER>'
	DATABASE_PASSWORD = '<RESULTNOTIFIER_DATABASE_PASSWORD>'
	DATABASE_HOST = '<DATABASE_HOST>' # may be 127.0.0.1 or 0.0.0.0
	DATABASE_PORT = '<DATABASE_PORT>' # generally '5432'
	DATABASE_NAME = '<DATABASE_NAME>'
	SECRET_KEY = '<SECRET_KEY>' # should be same in the app as well as in backend
	GCM_API_KEY = '<GCM_API_KEY>'

In terminal:

    sudo apt-get install libpq-dev python-dev python3-dev libjpeg-dev libjpeg8-dev 
    sudo -i
    cd /var/resultnotifier/backend
    # create virtualenv with python3
    virtualenv venv -p python3
    # activate the virtualenv
    source venv/bin/activate
    pip install --upgrade pip 
    pip install -r requirements.txt
    python seed.py
    # deactivate virtualenv
    deactivate
    exit
    
open /var/resultnotifier/backend/resultnotifier_nginx.conf:
    
    sudo vim /var/resultnotifier/backend/resultnotifier_nginx.conf
    
set the following variables:

	server_name <RESULTNOTIFIER_DOMAIN>; # without quotes
	
#### Step 4: [Setting up nginx]

    sudo apt-get install nginx
    
copy the files to specific locations:

    sudo cp /var/resultnotifier/backend/resultnotifier_nginx.conf /etc/nginx/sites-enabled
    sudo cp /var/resultnotifier/backend/resultnotifier_uwsgi.conf /etc/init

#### Step 5: [Initialize Servers]    

initialize directories:

    sudo mkdir /var/resultnotifier/backend/venv/var
    sudo mkdir /var/resultnotifier/backend/venv/var/run
    sudo mkdir /var/resultnotifier/backend/venv/var/log
    sudo mkdir /var/resultnotifier/backend/venv/var/log/uwsgi

initialize nginx:
    
    # you may have to stop apache2 if it is installed and running
    sudo service apache2 stop # (optional)
    sudo service nginx restart
    
initialize uwsgi:
    
    sudo /var/resultnotifier/backend/venv/bin/uwsgi --ini /var/resultnotifier/backend/resultnotifier_uwsgi.ini
	
initialize webscrape_job:

	sudo crontab -e # select vim if asked	
	# add following lines to crontab
	01 * * * * /var/resultnotifier/backend/venv/bin/python /var/resultnotifier/backend/notification.py
	*/5 * * * * /var/resultnotifier/backend/venv/bin/python /var/resultnotifier/backend/update_recent_viewed.py

All Done.

#### Step 6: [Restart Server]

if you restart your os, you may have to rerun the servers:
    
    sudo service apache2 stop # (optional)
    sudo service nginx restart
    sudo /var/resultnotifier/backend/venv/bin/uwsgi --ini /var/resultnotifier/backend/resultnotifier_uwsgi.ini
	
Installation (APP)
----------------------

**NOTE**: The installation procedure assumes that you have already installed **android studio**.

#### Step 1: [Create new Project]

open android studio and create new project named resultnotifier

#### Step 2: [copy the contents]

open the project directory and 

- replace the resultnotifier/app/src directory with resultnotifier/app/src directory from github
- replace the resultnotifier/app/build.gradle with resultnotifier/app/build.gradle from github
- replace the **contents** of resultnotifier/build.gradle with the contents of  resultnotifier/app/build.gradle.project from github

#### Step 3: [Initializing]

open resultnotifier/app/src/main/java/com/resultnotifier/main/CommonUtility.java:
and set the following variables:

	public final static String DOMAIN = "<DOMAIN>";
    public final static String SECRET_KEY = "<SECRET_KEY>"; //must be same as that in backend
    public final static String APP_NAME = "Result Notifier"; // you may change the app name
    public final static String GCM_SENDER_ID = "<GCM_SENDER_KEY>";
	
open resultnotifier/app/src/main/res/values/strings.xml 
and you may set the app name same as APP_NAME from 
resultnotifier/app/src/main/java/com/resultnotifier/main/CommonUtility.java

	<string name="app_name">Result Notifier</string>
	
#### Step 4: [Changing icons]

replacing app icon:

	replace resultnotifier/app/src/main/res/drwable/ic_app_icon.png with the icon you desire but of same name.
	
replacing app icon:

	replace resultnotifier/app/src/main/res/drwable/ic_notification.png with the icon you desire but of same name.
	
#### Step 5: [Build the app]

Build the app by clicking on the build button in Android Studio.

License
-------

Released under the [MIT License](http://opensource.org/licenses/MIT).


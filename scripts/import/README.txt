This import process requires a working "sendmail"

It also requires the "pony" gem installed

sudo gem install pony



it also requires the nakamura gem

sudo gem install nakamura



to import users call:

./import-users <path to csv file> <path to settings file>

where "settings file" is a json file formatted like the sampleServer.json

to remove users call:

./remove-users.rb <path to csv file> <path to settings file>
 

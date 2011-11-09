This import process requires a working "sendmail"

It also requires the "pony" gem installed

sudo gem install pony



it also requires the nakamura gem

sudo gem install nakamura

***NOTE***
as of now, the nakamura gem doesn't have the right stuff.  to manually install check out nakamura-ruby

git clone https://github.com/sakaiproject/nakamura-ruby.git

build the gem file
cd nakamura-ruby
rake
cd pkg
sudo gem install nakamura-0.2



to run an import call:

./import-users <path to csv file> <path to settings file>

where "settings file" is a json file formatted like the sampleServer.json



#!/usr/bin/env ruby

require 'rubygems'
require 'csv'
require 'json'
require 'nakamura'
require 'nakamura/users'
require 'import-base'
include SlingInterface
include SlingUsers
include OaeImport


$ALLOW_UPDATE=true
$UPDATE_PASSWORD=false # always false

if ARGV.size < 1
    puts "Usage: import-users.rb PATH_TO_CSV_FILE [PATH_TO_SERVER_CONFIG_FILE.json]"
    exit 1
end


class SisUserUploader < OaeImportBase
    
    def createManager(server)
        @userManager = UserManager.new(server)
    end
    
    def processRow(row)
        
        # 0 - EID
        
        @userManager.delete_user(row[0]);
    end
    
    def subject()
        return "removeusers.csv file processed on " + currentDate() 
    end
    
end



csvFile = ARGV[0]
serverInfo = ARGV[1] || nil
@sisUser = SisUserUploader.new(serverInfo)

@sisUser.processFile(csvFile)



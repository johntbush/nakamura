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
$UPDATE_PASSWORD=true

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
        # 1 - last name
        # 2 - first name
        # 3 - email
        # 4 - password
        # 5 - user type
        # 6 - internal id
        # 7, etc - properties

        user = User.new(row[0])
        user.lastName = row[1]
        user.firstName = row[2]
        user.email = row[3]
        user.password = row[4]
        
        if user.name == "admin" 
            raise "You cannot update admin"
        end
        
        props = @userManager.get_user_props(user.name)
        
        if (props["name"].nil?)
            # not found... create
            @userManager.create_user_object(user)
            @created += 1
        else 
            #found... update
            if ($ALLOW_UPDATE)
                user.update_user(@server)
                @updated += 1
            end
    
            if ($UPDATE_PASSWORD)
                # todo
            end
            
        end
        
    end
    
end



csvFile = ARGV[0]
serverInfo = ARGV[1] || nil
@sisUser = SisUserUploader.new(serverInfo)

@sisUser.processFile(csvFile)



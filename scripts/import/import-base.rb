#!/usr/bin/env ruby

require 'rubygems'
require 'json'
require 'nakamura'
require 'nakamura/users'
include SlingInterface
include SlingUsers


module OaeImport
    
    class OaeImportBase
        
        attr_accessor :server
        
        def createManager(server)
            puts 'need to implement "createManager"' 
        end
        
        def initialize(serverInfoFile = nil)
            
            if serverInfoFile != nil 
            else 
                @server = Sling.new()
            end
            
            createManager(@server)
            
        end
    
        def processFile(csvFile) 
           
            CSV.open(csvFile, 'r') do |row|
                processRow(row)
            end
            
        end
        
        def processRow(row)
           puts 'need to implement "processRow"' 
        end
        
    end
    
end

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
        attr_accessor :serverProps
        attr_accessor :log
        attr_accessor :created
        attr_accessor :updated
        attr_accessor :exceptional
        attr_accessor :total
        attr_accessor :exceptions        
        
        
        
        def createManager(server)
            puts 'need to implement "createManager"' 
        end
        
        def processServerProps(serverInfoFile)
            file = File.open(serverInfoFile, 'r')
            json = file.readlines.to_s
            @serverProps = JSON.parse(json)
            
            serverUrl = @serverProps["serverUrl"] || "http://localhost:8080/"
            adminPwd = @serverProps["password"] || "admin"
            
            puts "server info: #{serverUrl} and #{adminPwd}"
            
            s = Sling.new(serverUrl)
            
            admin = User.new("admin", adminPwd)
            s.switch_user(admin)
            
            return s
        end
        
        def sendReport(message) 
            
            @log.warn(message)
            
            # todo email report if email set
            
        end
        
        def initialize(serverInfoFile = nil)
            @log = Logger.new(STDOUT)
            @log.level = Logger::WARN

            begin 
            
                if serverInfoFile != nil 
                    @server = processServerProps(serverInfoFile)
                else 
                    @server = Sling.new()
                end
                
                @server.do_login()
                
                if (!@server.loggedin)
                    sendReport("failed to log into server")
                    raise "failed to log into server"
                end
                
                createManager(@server)
            rescue Exception => e
                sendReport(e.message)
                raise e
            end
        end
    
        def processFile(csvFile) 
            exceptions = ""
            @total = 0
            @exceptional = 0
            @created = 0
            @updated = 0
            
            CSV.open(csvFile, 'r') do |row|
                begin 
                    @total += 1
                    processRow(row)
                rescue Exception => e
                    @exceptional += 1
                    @log.warn(e.message)
                    @log.warn(e.backtrace)
                    exceptions << e.message
                    exceptions << "\n"
                end
            end
            
            report = "created #{created}\nupdated #{updated}\nfailed #{exceptional}\ntotal #{total}\n\n"
            report << exceptions

            sendReport(report)
        end
        
        def processRow(row)
           puts 'need to implement "processRow"' 
        end
        
    end
    
end

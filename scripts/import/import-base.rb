#!/usr/bin/env ruby

require 'rubygems'
require 'json'
require 'nakamura'
require 'nakamura/users'
require 'pony'
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
            
            s = Sling.new(serverUrl)
            
            admin = User.new("admin", adminPwd)
            s.switch_user(admin)
            
            return s
        end

        def currentDate()
            t = Time.now
            ret = t.strftime("run on %m/%d/%Y")   #=> "Printed on 04/09/2003"
            ret += t.strftime(" at %I:%M%p")            #=> "at 08:56AM"
            return ret
        end
        
        def subject()
            return "unkonw file processed on " + currentDate() 
        end
        
        def sendEmail(message) 
            toAddress = @serverProps["reportEmail"]
            
            if (toAddress.nil?) 
                return
            end
            
            fromAddress = @serverProps["reportEmailFrom"] || "no-reply@rsmart.com"
            
            Pony.mail(:to => toAddress, :from => fromAddress, :subject => subject(), :body => message)
        end
            
        def sendReport(message) 
            
            @log.warn(message)

            sendEmail(message)
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
    
        def updatedLabel
          return "updated"
        end
    
        def processFile(csvFile) 
            exceptions = ""
            @total = 0
            @exceptional = 0
            @created = 0
            @updated = 0
            
            skip = skipFirstRow()
            
            CSV.open(csvFile, 'r') do |row|
                if (!skip)
                  if row[0] != nil
                    @total += 1
                    begin 
                        processRow(row)
                    rescue Exception => e
                        @exceptional += 1
                        @log.warn(e.message)
                        @log.warn(e.backtrace)
                        exceptions << e.message
                        exceptions << "\n"
                    end
                  end
                end
                skip = false
            end
            
            report = "created #{created}\n#{updatedLabel} #{updated}\nfailed #{exceptional}\ntotal #{total}\n\n"
            report << exceptions

            sendReport(report)
        end
        
        def processRow(row)
           puts 'need to implement "processRow"' 
        end
        
        def skipFirstRow()
          return true
        end
        
        def skipFirstRowConfig(fileKey) 
          if (@serverProps)
            return @serverProps[fileKey]["skipHeaderRow"]
          else 
            return true
          end
        end
        
    end
    
end

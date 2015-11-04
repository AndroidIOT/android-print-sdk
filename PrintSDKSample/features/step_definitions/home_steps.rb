Then(/^I select "(.*?)" (?:option|button)$/) do |option|
    sleep(3.0)
    if option =="Image"
        touch query("* text:'#{option}'")
    else if option =="Preview"
        touch query("* text:'#{option}'")
    else if option =="I have one"
       if element_exists("* text:'#{option}'")
            touch query("* text:'#{option}'")
        end
    end
    end
    end
end

Given(/^I am on Home screen$/) do
    $proxy_http=ENV['http_proxy']
    $proxy_https=ENV['https_proxy']
    ENV['http_proxy']=nil
    ENV['https_proxy']=nil
    path =File.expand_path("../../support/runme.sh", __FILE__)
    system(path)
    sleep(WAIT_TIMEOUT)
    selenium.start_driver
    element_id="android:id/action_bar_title"
	wait.until { selenium.find_element(:id,element_id) }
    home_title = selenium.find_element(:id,element_id).text
    raise "Error Screen" unless home_title == "PrintSDKSample"        
end

Then(/^I tap on "(.*?)" option$/) do |option|
    sleep(WAIT_SCREENLOAD)
    if option == "Image"
        element_id="com.hp.mss.printsdksample:id/contentImage"
        selenium.find_element(:id,element_id).click
    else
        element_id="android:id/button3"
        if selenium.find_elements(:id,element_id).size > 0
            selenium.find_element(:id,element_id).click
        end
    end
end
Then(/^I select preview button$/) do
    sleep(WAIT_SCREENLOAD)
    element_id="com.hp.mss.printsdksample:id/printBtn"
    selenium.find_element(:id,element_id).click
end

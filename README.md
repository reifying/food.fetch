# Health Inspection Ratings via Alexa 

This project is the result of a 8-hour hackathon.

Enable Alexa voice lookup of restaurant health inspection ratings
for the city of Irving, TX based on public data.

The project includes
1. a Clojure script that 
    1. loads the 
        [health inspection data](https://drive.google.com/drive/folders/1SEJjMt2Ny4IoSc6zpIEGu3w0v4XGhcjv)
    2. merges inspection data with Yelp API data
    3. cleans data
    4. saves in json format
2. Python scripts for AWS Lambda to support Alexa 
    1. grabs user-requested facility and finds it in the json
    2. returns a string containing that facility's health inspection rating in Irving, TX, with the worst location of franchises having multiple locations in Irving having precedence.


## Remaining

1. Eliminate false matches from Yelp data
2. Answer question from Alexa user based on user's location

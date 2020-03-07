import json

def getRate(location):
    with open('foodhealth.json') as food_json:
        data = json.load(food_json)
        if location in data:
            return data[location][0]["LATEST_SCORE"]
        else:
            return "There was an error in your code; this should never have been reached"
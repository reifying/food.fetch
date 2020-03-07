# Original test data. Now we will collect data for every restaurant to relay to the user,
# accounting for multiple locations by finding the nearest one
food_ratings = {"starbucks":"Starbucks in Irving is pretty gross",
"mcdonalds":"The McDonalds in Irving is of an excellent condition",
"taco casa":"The Irving taco casa is abysmal",
"carino's":"Who even goes here?"}
import json
from getRating import getRate

food_json = open("foodhealth.json")
data = json.load(food_json)
restaurants = data.keys()

def lambda_handler(event, context):
    if event['session']['new']:
        on_start()
    if event['request']['type'] == "LaunchRequest":
        return on_launch(event)
    elif event['request']['type'] == "IntentRequest":
        return intent_scheme(event)
    elif event['request']['type'] == "SessionEndedRequest":
        return on_end()


# ------------------------------Part3--------------------------------
# Here we define the Request handler functions
def on_start():
    print("Session Started.")



def on_launch(event):
    onlunch_MSG = """Hi, welcome to Irving Food Info. To find the food inspection rating, just say, get food health rating for a location."""
    reprompt_MSG = "Do you want to hear more about the health rating of a location?"
    card_TEXT = "Pick a location"
    card_TITLE = "Choose a location"
    return output_json_builder_with_reprompt_and_card(onlunch_MSG, card_TEXT, card_TITLE, reprompt_MSG, False)


def on_end():
    print("Session Ended.")


# -----------------------------Part3.1-------------------------------
# The intent_scheme(event) function handles the Intent Request.
# Since we have a few different intents in our skill, we need to
# configure what this function will do upon receiving a particular
# intent. This can be done by introducing the functions which handle
# each of the intents.
def intent_scheme(event):
    intent_name = event['request']['intent']['name']

    if intent_name == "healthRating":
        return food_rating(event)
    elif intent_name in ["AMAZON.NoIntent", "AMAZON.StopIntent", "AMAZON.CancelIntent"]:
        return stop_the_skill(event)
    elif intent_name == "AMAZON.HelpIntent":
        return assistance(event)
    elif intent_name == "AMAZON.FallbackIntent":
        return fallback_call(event)


# ---------------------------Part3.1.1-------------------------------
# Here we define the intent handler functions
def food_rating(event):
    # ['slots']['player']['value'] is replaced with ['slots']['location']['value']; this may be the source of problems,
    # but in the end we're trying to obtain the request information from the user.
    name = event['request']['intent']['slots']['location']['value']
    # if name starts with 'the', tweak it so it does not (thes are mostly redundant)
    if name.lower()[:4] == "the ":
        name = name[4:]
    # location_list=[w.lower() for w in Player_LIST]
    # find the locations in our list of supported restaurants
    restaurant_list = [w.lower() for w in food_ratings.keys()]

    if name.lower() in restaurants:
        reprompt_MSG = "Do you want to hear more about a particular restaurant?"
        card_TEXT = "You've picked " + name.lower()
        card_TITLE = "You've picked " + name.lower()
        return output_json_builder_with_reprompt_and_card("The health rating for that location is " + getRate(name.lower()), card_TEXT, card_TITLE,
                                                          reprompt_MSG, False)
    else:
        wrongname_MSG = "Sorry, I don't know that one. Please repeat the name of your location."
        reprompt_MSG = "Do you want to hear more about a particular player?"
        card_TEXT = "Use the full name."
        card_TITLE = "Wrong name."
        return output_json_builder_with_reprompt_and_card(wrongname_MSG, card_TEXT, card_TITLE, reprompt_MSG, False)


def stop_the_skill(event):
    stop_MSG = "Thank you. Bye!"
    reprompt_MSG = ""
    card_TEXT = "Bye."
    card_TITLE = "Bye Bye."
    return output_json_builder_with_reprompt_and_card(stop_MSG, card_TEXT, card_TITLE, reprompt_MSG, True)


def assistance(event):
    assistance_MSG = "Be sure to list a location in Irving. If you're having trouble, try a more full or alternative name for the location."
    reprompt_MSG = "Do you want to hear more about the health rating of a particular location?"
    card_TEXT = "You've asked for help."
    card_TITLE = "Help"
    return output_json_builder_with_reprompt_and_card(assistance_MSG, card_TEXT, card_TITLE, reprompt_MSG, False)


def fallback_call(event):
    fallback_MSG = "I can't help you with that, try rephrasing the question or ask for help by saying HELP."
    reprompt_MSG = "Do you want to hear more about the health rating of a particular location?"
    card_TEXT = "You've asked a wrong question."
    card_TITLE = "Wrong question."
    return output_json_builder_with_reprompt_and_card(fallback_MSG, card_TEXT, card_TITLE, reprompt_MSG, False)


# ------------------------------Part4--------------------------------
# The response of our Lambda function should be in a json format.
# That is why in this part of the code we define the functions which
# will build the response in the requested format. These functions
# are used by both the intent handlers and the request handlers to
# build the output.
def plain_text_builder(text_body):
    text_dict = {}
    text_dict['type'] = 'PlainText'
    text_dict['text'] = text_body
    return text_dict


def reprompt_builder(repr_text):
    reprompt_dict = {}
    reprompt_dict['outputSpeech'] = plain_text_builder(repr_text)
    return reprompt_dict


def card_builder(c_text, c_title):
    card_dict = {}
    card_dict['type'] = "Simple"
    card_dict['title'] = c_title
    card_dict['content'] = c_text
    return card_dict


def response_field_builder_with_reprompt_and_card(outputSpeach_text, card_text, card_title, reprompt_text, value):
    speech_dict = {}
    speech_dict['outputSpeech'] = plain_text_builder(outputSpeach_text)
    speech_dict['card'] = card_builder(card_text, card_title)
    speech_dict['reprompt'] = reprompt_builder(reprompt_text)
    speech_dict['shouldEndSession'] = value
    return speech_dict


def output_json_builder_with_reprompt_and_card(outputSpeach_text, card_text, card_title, reprompt_text, value):
    response_dict = {}
    response_dict['version'] = '1.0'
    response_dict['response'] = response_field_builder_with_reprompt_and_card(outputSpeach_text, card_text, card_title,
                                                                              reprompt_text, value)
    return response_dict
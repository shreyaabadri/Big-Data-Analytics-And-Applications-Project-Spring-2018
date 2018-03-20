"""
Scrape the USDA website for nutritional info
"""

import secret
import requests

urlSearch = "https://api.nal.usda.gov/ndb/search/?format=json&q=butter&sort=n&max=25&offset=0&api_key=DEMO_KEY"
urlRequest = "https://api.nal.usda.gov/ndb/nutrients/?format=json&api_key="+secret.USDA_API_KEY+"&nutrients=208&ndbno=01009"

reqS = requests.get(urlSearch)
dataS = reqS.json()

reqR = requests.get(urlRequest)
dataR = reqR.json()
base = dataR['report']['foods'][0]['nutrients'][0]
print(base['nutrient'])
print(base['unit'])
print(base['gm'])
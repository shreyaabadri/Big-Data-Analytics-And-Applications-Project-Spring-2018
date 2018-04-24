from clarifai.rest import ClarifaiApp
from clarifai.rest import Image as ClImage
from os import chdir
import glob
from time import sleep

chdir("F:\\Big-Data\\Project\\Increment3\\part2\\data")

app = ClarifaiApp(api_key='ca35bf01d28e4f6e90648399f259111d')
#app = ClarifaiApp(api_key='b14627f026204c0e9aea601882050ac1') real
picsList= []
count = 0
for folder in ["caesar_salad","caprese_salad","donuts","dumplins","frenchfries","greek_salad","guacamole","hotdogs","risotto","sushi"]:
    #for folder in ["guacamole","hotdogs","risotto","sushi"]:
    # add multiple images with concepts
    for pic in glob.glob(folder+"/*.jpg"):
        picsList.append(ClImage(file_obj=open(pic, 'rb'), concepts=[folder]))
        
        #max upload is 128
        count += 1
        if count == 120:
            app.inputs.bulk_create_images(picsList)
            picsList.clear()
            count = 0
            sleep(5)

    app.inputs.bulk_create_images(picsList)
    picsList.clear()
    count = 0
    print("Done with "+ folder)
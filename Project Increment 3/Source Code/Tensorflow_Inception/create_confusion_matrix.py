import label_image
import glob


# caesar_salad caprese_salad donuts dumplins frenchfries greek_salad guacamole hotdogs risotto sushi

def getPos(x):
    key = {'caesar salad': 0, 'caprese salad': 1, 'donuts': 2, 'dumplins': 3, 'frenchfries': 4, 'greek salad':5, 'guacamole': 6, 'hotdogs': 7, 'risotto': 8, 'sushi': 9}
    return key[x]


matrix = {'caesar_salad': [0, 0, 0, 0, 0, 0, 0, 0, 0, 0], 'caprese_salad': [0, 0, 0, 0, 0, 0, 0, 0, 0, 0], 'donuts': [0, 0, 0, 0, 0, 0, 0, 0, 0, 0], 'dumplins': [0, 0, 0, 0, 0, 0, 0, 0, 0, 0],
          'frenchfries': [0, 0, 0, 0, 0, 0, 0, 0, 0, 0], 'greek_salad': [0, 0, 0, 0, 0, 0, 0, 0, 0, 0], 'guacamole': [0, 0, 0, 0, 0, 0, 0, 0, 0, 0], 'hotdogs': [0, 0, 0, 0, 0, 0, 0, 0, 0, 0], 'risotto': [0, 0, 0, 0, 0, 0, 0, 0, 0, 0],
          'sushi': [0, 0, 0, 0, 0, 0, 0, 0, 0, 0]}


for folder in glob.glob('test/*'):
    count = 0
    for img in glob.glob(folder+'/*.jpg'):
        tfolder = folder.split('\\')[1]

        result = label_image.predict_food(img)
        matrix[tfolder][getPos(result)] += 1
        count += 1
        if count == 10:
            break

    print('finish with ' + tfolder)



print(matrix['caesar_salad'])
print(matrix['caprese_salad'])
print(matrix['donuts'])
print(matrix['dumplins'])
print(matrix['frenchfries'])
print(matrix['greek_salad'])
print(matrix['guacamole'])
print(matrix['hotdogs'])
print(matrix['risotto'])
print(matrix['sushi'])
# caesar_salad caprese_salad donuts dumplins frenchfries greek_salad guacamole hotdogs risotto sushi
from os import chdir

chdir('F:/Big-Data/Project/Increment3/part2/CS5542-Tutorial2A-SourceCode/output')
def getPos(x):
    key = {'caesar_salad': 0, 'caprese_salad': 1, 'donuts': 2, 'dumplins': 3, 'frenchfries': 4, 'greek_salad':5, 'guacamole': 6, 'hotdogs': 7, 'risotto': 8, 'sushi': 9}
    return key[x]

matrix = {'caesar_salad': [0, 0, 0, 0, 0, 0, 0, 0, 0, 0], 'caprese_salad': [0, 0, 0, 0, 0, 0, 0, 0, 0, 0], 'donuts': [0, 0, 0, 0, 0, 0, 0, 0, 0, 0], 'dumplins': [0, 0, 0, 0, 0, 0, 0, 0, 0, 0],
          'frenchfries': [0, 0, 0, 0, 0, 0, 0, 0, 0, 0], 'greek_salad': [0, 0, 0, 0, 0, 0, 0, 0, 0, 0], 'guacamole': [0, 0, 0, 0, 0, 0, 0, 0, 0, 0], 'hotdogs': [0, 0, 0, 0, 0, 0, 0, 0, 0, 0], 'risotto': [0, 0, 0, 0, 0, 0, 0, 0, 0, 0],
          'sushi': [0, 0, 0, 0, 0, 0, 0, 0, 0, 0]}

with open("output.txt") as outputfile:
    for line in outputfile:
        line = line.split(' - ')
        line[0] = line[0].split('\\')[1].strip()
        line[1] = line[1].strip()
        if line[1] == line[0]:
            matrix[line[0]][getPos(line[1])] += 1
        else:
            matrix[line[0]][getPos(line[1])] += 1


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
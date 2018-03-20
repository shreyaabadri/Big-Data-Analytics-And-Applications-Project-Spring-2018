# donuts     dumplins    french fries    hot dog     sushi
def getPos(x):
    key = {'donut': 0, 'dumplins': 1, 'frenchfries': 2, 'hotdogs': 3, 'sushis': 4}
    return key[x]

matrix = {'donut': [0, 0, 0, 0, 0], 'dumplins': [0, 0, 0, 0, 0], 'frenchfries': [0, 0, 0, 0, 0], 'hotdogs': [0, 0, 0, 0, 0],
          'sushis': [0, 0, 0, 0, 0]}


with open("output.txt") as outputfile:
    for line in outputfile:
        line = line.split(' - ')
        line[0] = line[0].split('\\')[1].strip()
        line[1] = line[1].strip()
        if line[1] == line[0]:
            matrix[line[0]][getPos(line[1])] += 1
        else:
            matrix[line[0]][getPos(line[1])] += 1


print(matrix['donut'])
print(matrix['dumplins'])
print(matrix['frenchfries'])
print(matrix['hotdogs'])
print(matrix['sushis'])
from os import rename, chdir, listdir

chdir("./train2/4")

for photo in listdir("./"):
    rename(photo, "sushi."+photo)
import tensorflow as tf
import os

os.environ['TF_CPP_MIN_LOG_LEVEL'] = '2'

def predict_food():


    # Loads label file, strips off carriage return
    label_lines = [line.rstrip() for line
                   in tf.gfile.GFile("data/output_labels.txt")]

    # Unpersists graph from file
    with tf.gfile.FastGFile("data/output_graph.pb", 'rb') as f:
        graph_def = tf.GraphDef()
        graph_def.ParseFromString(f.read())
        _ = tf.import_graph_def(graph_def, name='')

    with tf.Session() as sess:
        # Feed the image_data as input to the graph and get first prediction
        softmax_tensor = sess.graph.get_tensor_by_name('final_result:0')

        import glob

        # caesar_salad caprese_salad donuts dumplins frenchfries greek_salad guacamole hotdogs risotto sushi

        def getPos(x):
            key = {'caesar salad': 0, 'caprese salad': 1, 'donuts': 2, 'dumplins': 3, 'frenchfries': 4,
                   'greek salad': 5, 'guacamole': 6, 'hotdogs': 7, 'risotto': 8, 'sushi': 9}
            return key[x]

        matrix = {'caesar_salad': [0, 0, 0, 0, 0, 0, 0, 0, 0, 0], 'caprese_salad': [0, 0, 0, 0, 0, 0, 0, 0, 0, 0],
                  'donuts': [0, 0, 0, 0, 0, 0, 0, 0, 0, 0], 'dumplins': [0, 0, 0, 0, 0, 0, 0, 0, 0, 0],
                  'frenchfries': [0, 0, 0, 0, 0, 0, 0, 0, 0, 0], 'greek_salad': [0, 0, 0, 0, 0, 0, 0, 0, 0, 0],
                  'guacamole': [0, 0, 0, 0, 0, 0, 0, 0, 0, 0], 'hotdogs': [0, 0, 0, 0, 0, 0, 0, 0, 0, 0],
                  'risotto': [0, 0, 0, 0, 0, 0, 0, 0, 0, 0],
                  'sushi': [0, 0, 0, 0, 0, 0, 0, 0, 0, 0]}

        for folder in glob.glob('test/*'):
            for img in glob.glob(folder + '/*.jpg'):
                tfolder = folder.split('\\')[1]

                image_data = tf.gfile.FastGFile(img, 'rb').read()
                predictions = sess.run(softmax_tensor, \
                                       {'DecodeJpeg/contents:0': image_data})

                # Sort to show labels of first prediction in order of confidence
                top_k = predictions[0].argsort()[-len(predictions[0]):][::-1]

                matrix[tfolder][getPos(label_lines[top_k[0]])] += 1

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


predict_food()
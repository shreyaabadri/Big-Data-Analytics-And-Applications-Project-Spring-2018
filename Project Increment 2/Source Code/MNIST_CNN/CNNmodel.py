import time

start = int(round(time.time() * 1000))
from tensorflow.examples.tutorials.mnist import input_data

mnist = input_data.read_data_sets('MNIST_data', one_hot=True, validation_size=200)
import tensorflow as tf

sess = tf.InteractiveSession()
x = tf.placeholder(tf.float32, shape=[None, 10000])
y_ = tf.placeholder(tf.float32, shape=[None, 10])
sess.run(tf.global_variables_initializer())

#Initialize weights with a small amount of noise for symmetry breaking, and to prevent 0 gradients.
def weight_variable(shape):
    initial = tf.truncated_normal(shape, stddev=0.1)
    return tf.Variable(initial)

#Since we're usingReLUneurons, it is also good practice  to initialize them with a slightly positive initial bias to avoid "dead neurons"
def bias_variable(shape):
    initial = tf.constant(0.1, shape=shape)
    return tf.Variable(initial)

#convolutional layer consists of several different feature maps
#We use 32 feature maps for MNIST

#Our convolutions uses a stride of one and are zero padded so that the output is the same size as the input
def conv2d(x, W):
    return tf.nn.conv2d(x, W, strides=[1, 1, 1, 1], padding='SAME')

#stride= 2 (default)
def max_pool_2x2(x):
    return tf.nn.max_pool(x, ksize=[1, 2, 2, 1], strides=[1, 2, 2, 1], padding='SAME')

#convolutional layer 1
W_conv1 = weight_variable([5, 5, 1, 32])
b_conv1 = bias_variable([32])
x_image = tf.reshape(x, [-1, 100, 100, 1])
h_conv1 = tf.nn.relu(conv2d(x_image, W_conv1) + b_conv1)

h_pool1 = max_pool_2x2(h_conv1)
# convolutional layer 2
W_conv2 = weight_variable([5, 5, 32, 32])
b_conv2 = bias_variable([32])
h_conv2 = tf.nn.relu(conv2d(h_pool1, W_conv2) + b_conv2)
h_pool2 = max_pool_2x2(h_conv2)

#Densely Connected Layer - now we reduce from 5*5 patch to 7*7 patch
W_fc1 = weight_variable([25 * 25 * 32, 128])
b_fc1 = bias_variable([128])

h_pool2_flat = tf.reshape(h_pool2, [-1, 25 * 25 * 32])
h_fc1 = tf.nn.relu(tf.matmul(h_pool2_flat, W_fc1) + b_fc1)

# Dropout Layer- To reduce overfitting
keep_prob = tf.placeholder(tf.float32)
h_fc1_drop = tf.nn.dropout(h_fc1, keep_prob)

# readout layer - Finally, we add a layer, for softmaxregression
W_fc2 = weight_variable([128, 10])
b_fc2 = bias_variable([10])


y_conv = tf.matmul(h_fc1_drop, W_fc2) + b_fc2
cross_entropy = tf.reduce_mean(tf.nn.softmax_cross_entropy_with_logits(logits=y_conv, labels=y_))

#AdamOptimizer
train_step = tf.train.AdamOptimizer(1e-4).minimize(cross_entropy)

# #AdagradDAOptimizer
# train_step = tf.train.AdagradDAOptimizerOptimizerOptimizer(1e-4).minimize(cross_entropy)
#
# #ProximalGradientDescentOptimizer
# train_step = tf.train.ProximalGradientDescentOptimizer(1e-4).minimize(cross_entropy)


correct_prediction = tf.equal(tf.argmax(y_conv, 1), tf.argmax(y_, 1))
accuracy = tf.reduce_mean(tf.cast(correct_prediction, tf.float32))

tf.summary.scalar('cross_entropy', cross_entropy)
tf.summary.histogram('cross_hist', cross_entropy)
merged = tf.summary.merge_all()
trainwriter = tf.summary.FileWriter('data/logs', sess.graph)
sess.run(tf.global_variables_initializer())

for i in range(500):
    batch_adam = mnist.train.next_batch(50)
    summary_adam, _ = sess.run([merged, train_step], feed_dict={x: batch_adam[0], y_: batch_adam[1], keep_prob: 0.5})
    trainwriter.add_summary(summary_adam, i)
    if i % 100 == 0:
        train_accuracy_adam = accuracy.eval(feed_dict={x: batch_adam[0], y_: batch_adam[1], keep_prob: 1.0})
        print("step %d, training accuracy %g" % (i, train_accuracy_adam))


print("test accuracy %g" % accuracy.eval(feed_dict={x: mnist.test.images, y_: mnist.test.labels, keep_prob: 1.0}))
end = int(round(time.time() * 1000))
print("Time for building convnet: ")
print(end - start)
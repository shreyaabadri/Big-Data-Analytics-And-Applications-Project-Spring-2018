import java.nio.file.{Files, Paths}

import org.apache.spark.mllib.classification.NaiveBayes
import org.apache.spark.mllib.clustering.{KMeans, KMeansModel}
import org.apache.spark.mllib.linalg.Vectors
import org.apache.spark.mllib.regression.LabeledPoint
import org.apache.spark.mllib.tree.model.RandomForestModel
import org.apache.spark.mllib.tree.{DecisionTree, RandomForest}
import org.apache.spark.rdd.RDD
import org.apache.spark.{SparkConf, SparkContext}

import scala.collection.mutable

object IPApp {
  val featureVectorsCluster = new mutable.MutableList[String]

  val IMAGE_CATEGORIES = List("donut", "dumplins", "frenchfries", "hotdogs", "sushis")

  /**
    *
    * @param sc     : SparkContext
    * @param images : Images list from the training set
    */
  def extractDescriptors(sc: SparkContext, images: RDD[(String, String)]): Unit = {
    if (Files.exists(Paths.get(IPSettings.FEATURES_PATH))) {
      println(s"${IPSettings.FEATURES_PATH} exists, skipping feature extraction.")
      return
    }

    val data = images.map {
      case (name, contents) => {
        val desc = ImageUtils.descriptors(name.split("file:/")(1))
        val list = ImageUtils.matToString(desc)
        //println("-- " + list.size)
        list
      }
    }.reduce((x, y) => x ::: y)

    val featuresSeq = sc.parallelize(data)

    featuresSeq.saveAsTextFile(IPSettings.FEATURES_PATH)
    println("Total size : " + data.size)
  }

  def kMeansCluster(sc: SparkContext): Unit = {
    if (Files.exists(Paths.get(IPSettings.KMEANS_PATH))) {
      println(s"${IPSettings.KMEANS_PATH} exists, skipping clusters formation.")
      return
    }

    // Load and parse the data
    val data = sc.textFile(IPSettings.FEATURES_PATH)
    val parsedData = data.map(s => Vectors.dense(s.split(' ').map(_.toDouble)))

    // Cluster the data into {#5} classes using KMeans
    val numClusters = 5
    val numIterations = 20
    val clusters = KMeans.train(parsedData, numClusters, numIterations)

    // Evaluate clustering by computing Within Set Sum of Squared Errors
    val WSSSE = clusters.computeCost(parsedData)
    println("Within Set Sum of Squared Errors = " + WSSSE)

    clusters.save(sc, IPSettings.KMEANS_PATH)
    println(s"Saves Clusters to ${IPSettings.KMEANS_PATH}")
    sc.parallelize(clusters.clusterCenters.map(v => v.toArray.mkString(" "))).saveAsTextFile(IPSettings.KMEANS_CENTERS_PATH)
  }

  def createHistogram(sc: SparkContext, images: RDD[(String, String)]): Unit = {
    if (Files.exists(Paths.get(IPSettings.HISTOGRAM_PATH))) {
      println(s"${IPSettings.HISTOGRAM_PATH} exists, skipping histograms creation..")
      return
    }

    val sameModel = KMeansModel.load(sc, IPSettings.KMEANS_PATH)

    val kMeansCenters = sc.broadcast(sameModel.clusterCenters)

    val categories = sc.broadcast(IMAGE_CATEGORIES)

    val data = images.map {
      case (name, contents) => {

        val vocabulary = ImageUtils.vectorsToMat(kMeansCenters.value)

        val desc = ImageUtils.bowDescriptors(name.split("file:/")(1), vocabulary)
        val list = ImageUtils.matToString(desc)
        println("-- " + list.size)

        val segments = name.split("/")
        // println(segments)
        val cat = segments(segments.length - 2)
        // println(cat)

        List(categories.value.indexOf(cat) + "," + list.head)

      }
    }.reduce((x, y) => x ::: y)

    val featuresSeq = sc.parallelize(data)

    featuresSeq.saveAsTextFile(IPSettings.HISTOGRAM_PATH)
    println("Total size : " + data.size)
  }

  def generateNaiveBayes(sc: SparkContext): Unit = {
    if (Files.exists(Paths.get(IPSettings.NAIVE_BAYES_PATH))) {
      println(s"${IPSettings.NAIVE_BAYES_PATH} exists, skipping model creation.")
      return
    }

    val data = sc.textFile(IPSettings.HISTOGRAM_PATH)
    val parsedData = data.map { line =>
      val parts = line.split(',')
      LabeledPoint(parts(0).toDouble, Vectors.dense(parts(1).split(' ').map(_.toDouble)))
    }

    val splits = parsedData.randomSplit(Array(0.5, 0.5), seed = 11L)
    val training = splits(0)
    val test = splits(1)

    val naiveBayesModel = NaiveBayes.train(training, lambda = 1.0, modelType = "multinomial")

    val predictionAndLabel = test.map(p => (naiveBayesModel.predict(p.features), p.label))
    val testErr = predictionAndLabel.filter(r => r._1 != r._2).count.toDouble / test.count()

    println("Test Error = " + testErr)
    ModelEvaluation.evaluateModel(predictionAndLabel)

    // Save and load model
    naiveBayesModel.save(sc, IPSettings.NAIVE_BAYES_PATH)
    println("Naive Bayes Model generated")
  }

  def generateBinaryTreeModel(sc: SparkContext): Unit = {
    if (Files.exists(Paths.get(IPSettings.DECISION_TREE_PATH))) {
      println(s"${IPSettings.DECISION_TREE_PATH} exists, skipping model creation.")
      return
    }

    val data = sc.textFile(IPSettings.HISTOGRAM_PATH)
    val parsedData = data.map { line =>
      val parts = line.split(',')
      LabeledPoint(parts(0).toDouble, Vectors.dense(parts(1).split(' ').map(_.toDouble)))
    }

    val splits = parsedData.randomSplit(Array(0.5, 0.5), seed = 11L)
    val training = splits(0)
    val test = splits(1)

    // Train a DecisionTree model.
    //  Empty categoricalFeaturesInfo indicates all features are continuous.
    val numClasses = 5
    val categoricalFeaturesInfo = Map[Int, Int]()
    val impurity = "gini"
    val maxDepth = 5
    val maxBins = 32

    val decisionTreeModel = DecisionTree.trainClassifier(training, numClasses, categoricalFeaturesInfo,
      impurity, maxDepth, maxBins)

    // Evaluate model on test instances and compute test error
    val predictionAndLabel = test.map { point =>
      val prediction = decisionTreeModel.predict(point.features)
      (point.label, prediction)
    }
    val testErr = predictionAndLabel.filter(r => r._1 != r._2).count().toDouble / test.count()
    println("Test Error = " + testErr)
    ModelEvaluation.evaluateModel(predictionAndLabel)

    // Save and load model
    decisionTreeModel.save(sc, IPSettings.DECISION_TREE_PATH)
    println("Decision Tree Model generated")  }

  def generateRandomForestModel(sc: SparkContext): Unit = {
    if (Files.exists(Paths.get(IPSettings.RANDOM_FOREST_PATH))) {
      println(s"${IPSettings.RANDOM_FOREST_PATH} exists, skipping Random Forest model formation.")
      return
    }

    val data = sc.textFile(IPSettings.HISTOGRAM_PATH)
    val parsedData = data.map { line =>
      val parts = line.split(',')
      LabeledPoint(parts(0).toDouble, Vectors.dense(parts(1).split(' ').map(_.toDouble)))
    }

    // Split data into training (80%) and test (20%).
    val splits = parsedData.randomSplit(Array(0.5, 0.5), seed = 11L)
    val training = splits(0)
    val test = splits(1)

    // Train a RandomForest model.
    //  Empty categoricalFeaturesInfo indicates all features are continuous.
    val numClasses = 5
    val categoricalFeaturesInfo = Map[Int, Int]()
    val maxBins = 100

    val numOfTrees = 5 to(10, 1)
    val strategies = List("all", "sqrt", "log2", "onethird")
    val maxDepths = 3 to(8, 1)
    val impurities = List("gini", "entropy")

    var bestModel: Option[RandomForestModel] = None
    var bestErr = 1.0
    val bestParams = new mutable.HashMap[Any, Any]()
    var bestnumTrees = 0
    var bestFeatureSubSet = ""
    var bestimpurity = ""
    var bestmaxdepth = 0

    numOfTrees.foreach(numTrees => {
      strategies.foreach(featureSubsetStrategy => {
        impurities.foreach(impurity => {
          maxDepths.foreach(maxDepth => {

            println("numTrees " + numTrees + " featureSubsetStrategy " + featureSubsetStrategy +
              " impurity " + impurity + " maxDepth " + maxDepth)

            val model = RandomForest.trainClassifier(training, numClasses, categoricalFeaturesInfo,
              numTrees, featureSubsetStrategy, impurity, maxDepth, maxBins)

            val predictionAndLabel = test.map { point =>
              val prediction = model.predict(point.features)
              (point.label, prediction)
            }

            val testErr = predictionAndLabel.filter(r => r._1 != r._2).count.toDouble / test.count()
            println("Test Error = " + testErr)
            ModelEvaluation.evaluateModel(predictionAndLabel)

            if (testErr < bestErr) {
              bestErr = testErr
              bestModel = Some(model)

              bestParams.put("numTrees", numTrees)
              bestParams.put("featureSubsetStrategy", featureSubsetStrategy)
              bestParams.put("impurity", impurity)
              bestParams.put("maxDepth", maxDepth)

              bestFeatureSubSet = featureSubsetStrategy
              bestimpurity = impurity
              bestnumTrees = numTrees
              bestmaxdepth = maxDepth
            }
          })
        })
      })
    })

    println("Best Err " + bestErr)
    println("Best params " + bestParams.toArray.mkString(" "))


    val randomForestModel = RandomForest.trainClassifier(parsedData, numClasses, categoricalFeaturesInfo, bestnumTrees, bestFeatureSubSet, bestimpurity, bestmaxdepth, maxBins)


    // Save and load model
    randomForestModel.save(sc, IPSettings.RANDOM_FOREST_PATH)
    println("Random Forest Model generated")
  }

  def main(args: Array[String]) {
    val conf = new SparkConf()
      .setAppName(s"IPApp")
      .setMaster("local[*]")
      .set("spark.executor.memory", "6g")
      .set("spark.driver.memory", "6g")
    val sparkConf = new SparkConf().setAppName("SparkWordCount").setMaster("local[*]")

    val sc = new SparkContext(sparkConf)

    val images = sc.wholeTextFiles(s"${IPSettings.INPUT_DIR}/*/*.jpg")

    /** Extracts Key Descriptors from the Training set
      * Saves it to a text file
      **/
    extractDescriptors(sc, images)

    /** Reads the Key descriptors and forms a 'K' cluster
      * Saves the centers as a text file
      **/
    kMeansCluster(sc)

    /** Forms a labeled Histogram using the Training set and saves it in the form of label, [Histogram]
      * This shall be used as a input to the classifiers to create a model
      **/
    createHistogram(sc, images)

    /** From the labeled histogram a Naive Bayes Model is created **/
    generateNaiveBayes(sc)

    /** From the labeled Histograms a Binary Tree Model is created **/
    generateBinaryTreeModel(sc)

    /** From the labeled Histograms a Random Forest Model is created **/
    generateRandomForestModel(sc)
  }
}
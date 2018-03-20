import clarifai2.api.ClarifaiBuilder;
import clarifai2.api.ClarifaiClient;
import clarifai2.api.ClarifaiResponse;
import clarifai2.dto.input.ClarifaiImage;
import clarifai2.dto.input.ClarifaiInput;
import clarifai2.dto.model.output.ClarifaiOutput;
import clarifai2.dto.prediction.Concept;
import okhttp3.OkHttpClient;
import org.openimaj.image.DisplayUtilities;
import org.openimaj.image.ImageUtilities;
import org.openimaj.image.MBFImage;
import org.openimaj.image.colour.RGBColour;
import org.openimaj.image.typography.hershey.HersheyFont;

import java.io.*;
import java.util.List;

//import clarifai2.dto.input.image.ClarifaiImage;


public class ImageAnnotation {
    public static void main(String[] args) throws IOException {
        File o = new File("output/output.txt");
        o.delete();

        final ClarifaiClient client = new ClarifaiBuilder("a7e0023b8a7742ffa4c54fb81cf1365c")
                .client(new OkHttpClient())
                .buildSync();

        File[] categories = new File("input/").listFiles(File::isDirectory);
        for (int a = 0; a < categories.length; a++) {

            File path = new File(categories[a].toString());
            File[] files = path.listFiles();
            for (int i = 0; i < files.length; i++) {
                ClarifaiResponse response = client.predict("Project_Increment_1")
                        .withInputs(
                                ClarifaiInput.forImage(ClarifaiImage.of(files[i]))
                        )
                        .executeSync();
                List<ClarifaiOutput<Concept>> predictions = (List<ClarifaiOutput<Concept>>) response.get();
                MBFImage image = ImageUtilities.readMBF(files[i]);
                int x = image.getWidth();
                int y = image.getHeight();

                //System.out.println("*************" + files[i] + "***********");
                List<Concept> data = predictions.get(0).data();
//                for (int j = 0; j < data.size(); j++) {
//                    System.out.println(data.get(j).name() + " - " + data.get(j).value());
//                }
                image.drawText(data.get(0).name(), (int) Math.floor(Math.random() * x) - 20, (int) Math.floor(Math.random() * y) - 20, HersheyFont.ASTROLOGY, 20, RGBColour.RED);
                DisplayUtilities.displayName(image, "image" + i);

                try {
                    String filename = "output/output.txt";
                    String content = files[i] + " - " + data.get(0).name() + "\n";
                    FileWriter fw = new FileWriter(filename, true); //the true will append the new data
                    fw.write(content);//appends the string to the file
                    fw.close();
                } catch (IOException ioe) {
                    System.err.println("IOException: " + ioe.getMessage());
                }
            }
        }
    }
}

import java.io.File;

public class test {
    public static void main(String[] args) {
//        File file = new File("input/");
//        File[] files = file.listFiles();
//        for (int i=0; i<files.length; i++){
//            System.out.println(files[i]);
//        }

        File[] categories = new File("input/").listFiles(File::isDirectory);
        for (int a = 0; a < categories.length; a++) {
            File path = new File(categories[a].toString());
            File[] files = path.listFiles();
            System.out.println(categories[a].toString());
            for (int i = 0; i < files.length; i++) {
                System.out.println(files[i]);
            }
            System.out.println("\n\n\n");
        }
    }
}

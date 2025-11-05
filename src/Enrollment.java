import java.io.File;
public class Enrollment {
    private final File dir;
    private final int W = 120, H = 120;
    private final double THRESH;
    public Enrollment(File dir, double thresh) { 
    	this.dir = dir; 
    	this.THRESH = thresh; 
    }
    public boolean enroll(String imgPath) {
        try {
            File src = new File(imgPath);
            if (!src.exists()) {
                System.out.println("Source file does not exist: " + imgPath);
                return false;
            }
            if (!src.isFile()) {
                System.out.println("Source path is not a file: " + imgPath);
                return false;
            }
            File dst = new File(dir, System.currentTimeMillis() + "_" + src.getName());
            java.nio.file.Files.copy(src.toPath(), dst.toPath());
            return true;
        } catch (Exception e) {
            System.out.println("Error during enrollment: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    public boolean authenticate(String Path) {
        File[] faces = dir.listFiles((d, n) -> n.toLowerCase().matches(".*\\.(png|jpg|jpeg)"));
        if (faces == null || faces.length == 0) {
            System.out.println("No enrolled face images found in directory: " + dir.getAbsolutePath());
            return false;
        }
        try {
            for (File f : faces) {
                double dist = ImageMatcher.distance(Path, f.getAbsolutePath(), W, H);
                System.out.println("Comparing with " + f.getName() + " distance: " + dist);
                if (dist <= THRESH) return true;
            }
        } catch (Exception e) {
            System.out.println("Error during authentication: " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }


}



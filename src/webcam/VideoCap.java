//http://computervisionandjava.blogspot.co.uk/2013/10/java-opencv-webcam.html

package webcam;

import java.awt.image.BufferedImage;

import org.opencv.core.Core;
import org.opencv.videoio.VideoCapture;

public class VideoCap {
	
	public static int source = 0;
	
    static{
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
    }

    public VideoCapture cap;
    Mat2Image mat2Img = new Mat2Image();

    VideoCap(){
        cap = new VideoCapture();
        cap.open(source);
    } 
 
    BufferedImage getOneFrame() {
        cap.read(mat2Img.mat);
        return mat2Img.getImage(mat2Img.mat);
    }
}
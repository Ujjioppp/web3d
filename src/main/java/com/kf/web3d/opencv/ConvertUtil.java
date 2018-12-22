package com.kf.web3d.opencv;

import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.core.Size;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import sun.misc.BASE64Encoder;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.io.*;
import java.net.URL;

/**
 * Created by ye on 2018/12/4.
 */
public class ConvertUtil {

    /**
     * 读取本地图片文件
     * @param filePath
     * @return Mat
     */
    public static final Mat matRead(String filePath) {
        //return Highgui.imread("/home/night/webvr/vr.jpg");
//      return Imgcodecs.imread("/home/night/webvr/vr.jpg");
        return Imgcodecs.imread(filePath);
    }

    /**
     * 读取网络图片
     * @param urlStr
     * @return
     */
    public static final BufferedImage bufferReadUrl(String urlStr){
        BufferedImage image = null;
        try
        {
            URL url = new URL(urlStr);
            image = ImageIO.read(url);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        return image;
    }

    /**
     * 图片文件保存
     * @param fileName
     * @param mat
     */
    public static final void matSave(String fileName,Mat mat) {
        //Highgui.imwrite(fileName, mat);
        //Imgcodecs.imwrite(fileName, mat);
        BufferedImage buff = matToBuffer(".jpg",mat);
        bufferSave(fileName,buff);
    }

    /**
     * 读取图片文件
     * @param filePath
     * @return
     */
    public static final BufferedImage bufferRead(String filePath) {
        try {
            return ImageIO.read(new File(filePath));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 图片文件保存
     * @param fileName
     * @param buff
     */
    public static final void bufferSave(String fileName,BufferedImage buff) {
        try {
            ImageIO.write(buff, "JPEG", new File(fileName));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static final Mat matResize(Mat mat,int width,int height){
        Mat target = new Mat(width,height,mat.type());
        //Imgproc.resize(src, dst, dsize);
        Imgproc.resize(mat, target, new Size(width,height), 0, 0, Imgproc.INTER_LINEAR);
        return target;
    }

    /**
     * Mat转换为BufferedImage，已测，可用
     * @param fileExt
     * @param mat
     * @return
     */
    public static final BufferedImage matToBuffer(String fileExt,Mat mat)
    {
        MatOfByte mob = new MatOfByte();
        Imgcodecs.imencode(fileExt, mat, mob);
        // convert the "matrix of bytes" into a byte array
        byte[] byteArray = mob.toArray();
        BufferedImage bufImage = null;
        try {
            InputStream in = new ByteArrayInputStream(byteArray);
            bufImage = ImageIO.read(in);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return bufImage;
    }


    /**
     * BufferedImage转换为Mat，已测，可用
     * @param bufferedImage
     * @param imgType
     * @return
     */
    public static Mat bufferToMat(BufferedImage bufferedImage, int imgType)
    {
        final int matType = CvType.CV_8UC3;

        if (bufferedImage == null) {
            throw new IllegalArgumentException("bufferToMat-> BufferedImage == null");
        }

        if (bufferedImage.getType() != imgType) {
            BufferedImage image = new BufferedImage(bufferedImage.getWidth(), bufferedImage.getHeight(), imgType);
            Graphics2D g = image.createGraphics();
            try {
                g.setComposite(AlphaComposite.Src);
                g.drawImage(bufferedImage, 0, 0, null);
            } finally {
                g.dispose();
            }
        }

        byte[] pixels = ((DataBufferByte) bufferedImage.getRaster().getDataBuffer()).getData();
        Mat mat = Mat.eye(bufferedImage.getHeight(), bufferedImage.getWidth(), matType);
        mat.put(0, 0, pixels);

        return mat;
    }

    /**
     * 将Mat图片进行Base64编码
     * base64编码后的字符串中经常包含“+”号，在C#环境中发送给服务器后，服务器把“+”存成了“ ”空格，而在Java环境下，“+”号依然是加号
     * 所以在java环境中，解码之前，需要先把编码后的字符串中的“ ”替换成“+”号
     * <code>String des = des.replaceAll("\\+", "%2B");</code>
     *
     * @param mat
     * @return
     */
    public static final String matEncoder(Mat mat) {
        BufferedImage buff = matToBuffer(".jpg",mat);
        ByteArrayOutputStream byteout = new ByteArrayOutputStream();
        try {
            ImageIO.write(buff, "JPEG", byteout);
        } catch (IOException e) {
            e.printStackTrace();
        }

        BASE64Encoder encoder = new BASE64Encoder();
        return encoder.encode( byteout.toByteArray());
    }
}

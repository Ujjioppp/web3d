package com.kf.web3d.opencv;

import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Scalar;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import java.awt.image.BufferedImage;

/**
 * Created by ye on 2018/12/4.
 */
public class OpenCVUtil {
    static {
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
    }

    double[][] imageTransform =
            {
                    {0, 0},
                    {Math.PI / 2, 0},
                    {Math.PI, 0},
                    {-Math.PI / 2, 0},
                    {0, -Math.PI / 2},
                    {0, Math.PI / 2}
            };

    public static void main(String[] args) {
        OpenCVUtil util = new OpenCVUtil();

//        BufferedImage buff = ConvertUtil.bufferRead("/Users/yl/Downloads/tx.jpg");// /home/night/webvr/vr.jpg

//        Mat[] cube = util.shear(buff, 1024, 1024);  //全景图切割

        Mat[] pics = new Mat[4];
        for (int i = 0; i < pics.length; i++) {
            pics[i] = Imgcodecs.imread("/Users/yl/Downloads/t" + i + ".jpg");
        }

        Mat preview = util.mergeImage(pics, 512);           //预览图合成
    }

    /**
     * 全景图切割，返回6张图
     */
    public Mat[] shear(BufferedImage buff, int targetWidth, int targetHeight) {
        //Mat mat = ConvertUtil.matRead("/home/night/webvr/vr.jpg");
        Mat mat = ConvertUtil.bufferToMat(buff, buff.getType());

        Mat[] cube = new Mat[6];
        for (int i = 0; i < 6; i++) {
            cube[i] = sideCubeMapImage(mat, i, targetWidth, targetHeight);
        }

        return cube;
    }

    /**
     * 全景图切割，单面处理
     */
    private Mat sideCubeMapImage(Mat source, final int sideId, final int sideWidth, final int sideHeight) {
        Mat result = new Mat(sideWidth, sideHeight, source.type());

        System.out.println("==========handle " + sideId + " start ===========");
        float sourceWidth = source.cols();
        float sourceHeight = source.rows();     // 获取图片的行列数量

        Mat mapx = new Mat(sideHeight, sideWidth, CvType.CV_32F);
        Mat mapy = new Mat(sideHeight, sideWidth, CvType.CV_32F);       //分配图的x,y轴

        // Calculate adjacent (ak) and opposite (an) of the
        // triangle that is spanned from the sphere center
        //to our cube face.
        final double an = Math.sin(Math.PI / 4);
        final double ak = Math.cos(Math.PI / 4);                                          //计算相邻ak和相反an的三角形张成球体中心

        double ftu = imageTransform[sideId][0];
        double ftv = imageTransform[sideId][1];

        // For each point in the target image,
        // calculate the corresponding source coordinates.                      对于每个图像计算相应的源坐标
        for (int y = 0; y < sideHeight; y++) {
            for (int x = 0; x < sideWidth; x++) {

                // Map face pixel coordinates to [-1, 1] on plane               将坐标映射在平面上
                float nx = (float) y / (float) sideHeight - 0.5f;
                float ny = (float) x / (float) sideWidth - 0.5f;

                nx *= 2;
                ny *= 2;

                // Map [-1, 1] plane coord to [-an, an]
                // thats the coordinates in respect to a unit sphere
                // that contains our box.
                nx *= an;
                ny *= an;

                double u, v;

                // Project from plane to sphere surface.
                if (ftv == 0) {
                    // Center faces
                    u = Math.atan2(nx, ak);
                    v = Math.atan2(ny * Math.cos(u), ak);
                    u += ftu;
                } else if (ftv > 0) {
                    // Bottom face
                    double d = Math.sqrt(nx * nx + ny * ny);
                    v = Math.PI / 2 - Math.atan2(d, ak);
                    u = Math.atan2(ny, nx);
                } else {
                    // Top face
                    //cout << "aaa";
                    double d = Math.sqrt(nx * nx + ny * ny);
                    v = -Math.PI / 2 + Math.atan2(d, ak);
                    u = Math.atan2(-ny, nx);
                }

                // Map from angular coordinates to [-1, 1], respectively.
                u = u / (Math.PI);
                v = v / (Math.PI / 2);

                // Warp around, if our coordinates are out of bounds.
                while (v < -1) {
                    v += 2;
                    u += 1;
                }
                while (v > 1) {
                    v -= 2;
                    u += 1;
                }

                while (u < -1) {
                    u += 2;
                }
                while (u > 1) {
                    u -= 2;
                }

                // Map from [-1, 1] to in texture space
                u = u / 2.0f + 0.5f;
                v = v / 2.0f + 0.5f;

                u = u * (sourceWidth - 1);
                v = v * (sourceHeight - 1);

                mapx.put(x, y, u);
                mapy.put(x, y, v);
            }
        }

        // Recreate output image if it has wrong size or type.
        /**
         if (result.cols() != width || result.rows() != height ||
         result.type() != source.type()) {
         result = new Mat(width, height, source.type());
         }**/

        // Do actual  using OpenCV's remap
        Imgproc.remap(source, result, mapx, mapy, Imgproc.INTER_LINEAR, Core.BORDER_CONSTANT, new Scalar(0, 0, 0));

        if (sideId == 0) {
            ConvertUtil.matSave("/Users/yl/Downloads/test2_cube00.jpg", result);
        } else if (sideId == 1) {
            ConvertUtil.matSave("/Users/yl/Downloads/test2_cube01.jpg", result);
        } else if (sideId == 2) {
            ConvertUtil.matSave("/Users/yl/Downloads/test2_cube02.jpg", result);
        } else if (sideId == 3) {
            ConvertUtil.matSave("/Users/yl/Downloads/test2_cube03.jpg", result);
        } else if (sideId == 4) {
            ConvertUtil.matSave("/Users/yl/Downloads/test2_cube04.jpg", result);
        } else if (sideId == 5) {
            ConvertUtil.matSave("/Users/yl/Downloads/test2_cube05.jpg", result);
        }

        System.out.println("==========handle " + sideId + " over ===========");

        return result;
    }

    /**
     * 全景预览图合成
     */
    private Mat mergeImage(Mat[] cube, int w) {
        int width = cube[0].width();
        int height = cube[0].height();
        Mat mat = new Mat(width, height * cube.length, cube[0].type());
        Mat side = ConvertUtil.matResize(cube[0], width, height * cube.length);
        mat.put(0, 0, getByte(side));

        ConvertUtil.matSave("/users/yl/Downloads/res.jpg", mat);
//        for (int i = 0; i < cube.length; i++) {
//            Mat side = ConvertUtil.matResize(cube[i], width, width);
//            mat.put(i, i * width, getByte(side));
//        }
//        ConvertUtil.matSave("/users/yl/Downloads/res.jpg", mat);
        return mat;
    }

    public byte[] getByte(Mat mat) {
        int width = mat.cols();
        int height = mat.rows();
        int dims = mat.channels();
        byte[] rgbdata = new byte[width * height * dims];
        mat.get(0, 0, rgbdata);
        return rgbdata;
    }
}

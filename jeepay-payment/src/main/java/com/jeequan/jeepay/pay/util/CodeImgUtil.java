/*
 * Copyright (c) 2021-2031, 河北计全科技有限公司 (https://www.jeequan.com & jeequan@126.com).
 * <p>
 * Licensed under the GNU LESSER GENERAL PUBLIC LICENSE 3.0;
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.gnu.org/licenses/lgpl.html
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.jeequan.jeepay.pay.util;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.google.zxing.*;
import com.google.zxing.client.j2se.BufferedImageLuminanceSource;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.common.HybridBinarizer;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URL;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/*
* 二维码生成器
*
* @author terrfly
* @site https://www.jeequan.com
* @date 2021/6/8 17:54
*/
public class CodeImgUtil {

	private static final Logger _log = LoggerFactory.getLogger(CodeImgUtil.class);

	// 二维码尺寸List
	private static List<Integer> sizeList = new ArrayList<Integer>();

	static {
		sizeList.add(258);
		sizeList.add(344);
		sizeList.add(430);
		sizeList.add(860);
		sizeList.add(1280);
	}

	public static List<Integer> getEwmSizeList() {
		return sizeList;
	}


	// 图片宽度的一般
	private static final int IMAGE_WIDTH = 25;
	private static final int IMAGE_HEIGHT = 25;
	private static final int IMAGE_HALF_WIDTH = IMAGE_WIDTH / 2;
	private static final int FRAME_WIDTH = 2;

	// 二维码写码器
	private static MultiFormatWriter mutiWriter = new MultiFormatWriter();

	/**
	 *
	 * @param content
	 *            二维码显示的文本
	 * @param width
	 *            二维码的宽度
	 * @param height
	 *            二维码的高度
	 * @param srcImagePath
	 *            中间嵌套的图片
	 * @param destImagePath
	 *            二维码生成的地址
	 */
	public static void encode(String content, int width, int height,
			String srcImagePath, String destImagePath, String fileName) {
		try {
	        File dir = new File(destImagePath);
	        _log.error("==================" + destImagePath);
	        _log.error("==================" + srcImagePath);
			if (!dir.exists()) {
				_log.error("==================notExist");
				boolean result = dir.mkdirs();
				_log.error("==================midirsResult" + result);
			}
			// ImageIO.write 参数 1、BufferedImage 2、输出的格式 3、输出的文件
			ImageIO.write(genBarcode(content, width, height, srcImagePath),
					"jpg", new File(destImagePath + fileName));

		} catch (Exception e) {
			_log.error("生成二维码出错", e);
		}
	}

	/**
	 * 得到BufferedImage
	 *
	 * @param content
	 *            二维码显示的文本
	 * @param width
	 *            二维码的宽度
	 * @param height
	 *            二维码的高度
	 * @param srcImagePath
	 *            中间嵌套的图片
	 * @return
	 * @throws WriterException
	 * @throws IOException
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	private static BufferedImage genBarcode(String content, int width,
			int height, String srcImagePath) throws WriterException,
			IOException {
		// 读取源图像
		BufferedImage scaleImage = scale(srcImagePath, IMAGE_WIDTH,
				IMAGE_HEIGHT, false);

		int[][] srcPixels = new int[IMAGE_WIDTH][IMAGE_HEIGHT];
		for (int i = 0; i < scaleImage.getWidth(); i++) {
			for (int j = 0; j < scaleImage.getHeight(); j++) {
				srcPixels[i][j] = scaleImage.getRGB(i, j);
			}
		}

		java.util.Hashtable hint = new java.util.Hashtable();
		hint.put(EncodeHintType.CHARACTER_SET, "utf-8");
		hint.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.H);
		hint.put(EncodeHintType.MARGIN, 1);
		// 生成二维码
		BitMatrix matrix = mutiWriter.encode(content, BarcodeFormat.QR_CODE,
				width, height, hint);

		// 二维矩阵转为一维像素数组
		int halfW = matrix.getWidth() / 2;
		int halfH = matrix.getHeight() / 2;
		int[] pixels = new int[width * height];

		for (int y = 0; y < matrix.getHeight(); y++) {
			for (int x = 0; x < matrix.getWidth(); x++) {
				// 读取图片
				if (x > halfW - IMAGE_HALF_WIDTH
						&& x < halfW + IMAGE_HALF_WIDTH
						&& y > halfH - IMAGE_HALF_WIDTH
						&& y < halfH + IMAGE_HALF_WIDTH) {
					pixels[y * width + x] = srcPixels[x - halfW
							+ IMAGE_HALF_WIDTH][y - halfH + IMAGE_HALF_WIDTH];
				}
				// 在图片四周形成边框
				else if ((x > halfW - IMAGE_HALF_WIDTH - FRAME_WIDTH
						&& x < halfW - IMAGE_HALF_WIDTH + FRAME_WIDTH
						&& y > halfH - IMAGE_HALF_WIDTH - FRAME_WIDTH && y < halfH
						+ IMAGE_HALF_WIDTH + FRAME_WIDTH)
						|| (x > halfW + IMAGE_HALF_WIDTH - FRAME_WIDTH
								&& x < halfW + IMAGE_HALF_WIDTH + FRAME_WIDTH
								&& y > halfH - IMAGE_HALF_WIDTH - FRAME_WIDTH && y < halfH
								+ IMAGE_HALF_WIDTH + FRAME_WIDTH)
						|| (x > halfW - IMAGE_HALF_WIDTH - FRAME_WIDTH
								&& x < halfW + IMAGE_HALF_WIDTH + FRAME_WIDTH
								&& y > halfH - IMAGE_HALF_WIDTH - FRAME_WIDTH && y < halfH
								- IMAGE_HALF_WIDTH + FRAME_WIDTH)
						|| (x > halfW - IMAGE_HALF_WIDTH - FRAME_WIDTH
								&& x < halfW + IMAGE_HALF_WIDTH + FRAME_WIDTH
								&& y > halfH + IMAGE_HALF_WIDTH - FRAME_WIDTH && y < halfH
								+ IMAGE_HALF_WIDTH + FRAME_WIDTH)) {
					pixels[y * width + x] = 0xfffffff;
				} else {
					// 此处可以修改二维码的颜色，可以分别制定二维码和背景的颜色；
					pixels[y * width + x] = matrix.get(x, y) ? 0xff000000
							: 0xfffffff;
				}
			}
		}

		BufferedImage image = new BufferedImage(width, height,
				BufferedImage.TYPE_INT_RGB);
		image.getRaster().setDataElements(0, 0, width, height, pixels);

		return image;
	}

	/**
	 * 把传入的原始图像按高度和宽度进行缩放，生成符合要求的图标
	 *
	 * @param srcImageFile
	 *            源文件地址
	 * @param height
	 *            目标高度
	 * @param width
	 *            目标宽度
	 * @param hasFiller
	 *            比例不对时是否需要补白：true为补白; false为不补白;
	 * @throws IOException
	 */
	private static BufferedImage scale(String srcImageFile, int height,
			int width, boolean hasFiller) throws IOException {
		double ratio = 0.0; // 缩放比例

		URL url = new URL(srcImageFile);

		BufferedImage srcImage = ImageIO.read(url);
		Image destImage = srcImage.getScaledInstance(width, height,
				BufferedImage.SCALE_SMOOTH);
		// 计算比例
		if ((srcImage.getHeight() > height) || (srcImage.getWidth() > width)) {
			if (srcImage.getHeight() > srcImage.getWidth()) {
				ratio = (new Integer(height)).doubleValue()
						/ srcImage.getHeight();
			} else {
				ratio = (new Integer(width)).doubleValue()
						/ srcImage.getWidth();
			}
			AffineTransformOp op = new AffineTransformOp(AffineTransform
					.getScaleInstance(ratio, ratio), null);
			destImage = op.filter(srcImage, null);
		}
		if (hasFiller) {// 补白
			BufferedImage image = new BufferedImage(width, height,
					BufferedImage.TYPE_INT_RGB);
			Graphics2D graphic = image.createGraphics();
			graphic.setColor(Color.white);
			graphic.fillRect(0, 0, width, height);
			if (width == destImage.getWidth(null)) {
                graphic.drawImage(destImage, 0, (height - destImage
                        .getHeight(null)) / 2, destImage.getWidth(null),
                        destImage.getHeight(null), Color.white, null);
            } else {
                graphic.drawImage(destImage,
                        (width - destImage.getWidth(null)) / 2, 0, destImage
                                .getWidth(null), destImage.getHeight(null),
                        Color.white, null);
            }
			graphic.dispose();
			destImage = image;
		}
		return (BufferedImage) destImage;
	}

	/**
     * 生成图像
     *  filePath 存放图片的路径
     *  fileName 图片的名称
     *  info     生成图片的链接地址（例如：weixin://wxpay/s/Anp43md）
     *  width    图片的宽度
     *  height   图片的高度
     * @throws WriterException
     * @throws IOException
     */
    public static String codeImgEncode(String filePath, String fileName, String info, int width, int height) throws WriterException, IOException {
    	String format="png";
        Map<EncodeHintType, Object> hints = new HashMap<EncodeHintType, Object>();
        hints.put(EncodeHintType.CHARACTER_SET, "UTF-8");
        BitMatrix bitMatrix = new MultiFormatWriter().encode(info,
                BarcodeFormat.QR_CODE, width, height, hints);// 生成矩阵
        Path path = FileSystems.getDefault().getPath(filePath, fileName);
        File dir = new File(filePath);
        _log.error("==================" + filePath);
		if (!dir.exists()) {
			_log.error("==================notExist");
			boolean result = dir.mkdirs();
			_log.error("==================midirsResult" + result);
		}
        MatrixToImageWriter.writeToPath(bitMatrix, format, path);// 输出图像
        return path.toString();
    }


	public static void writeQrCode(OutputStream stream, String info, int width, int height) throws WriterException, IOException {
		String format="png";
		Map<EncodeHintType, Object> hints = new HashMap<EncodeHintType, Object>();
		hints.put(EncodeHintType.CHARACTER_SET, "UTF-8");
		hints.put(EncodeHintType.MARGIN,0);
		BitMatrix bitMatrix = new MultiFormatWriter().encode(info,
				BarcodeFormat.QR_CODE, width, height, hints);// 生成矩阵
		bitMatrix = deleteWhite(bitMatrix);
		BufferedImage bi = MatrixToImageWriter.toBufferedImage(bitMatrix);
		bi = zoomInImage(bi,width,height);
		ImageIO.write(bi,format,stream);            // 输出图像
		//MatrixToImageWriter.writeToStream(bitMatrix, format, stream);// 输出图像
	}


	/**
     * 解析图像
     */
    public static void codeImgDecode() {
        String filePath = "D://zxing.png";
        BufferedImage image;
        try {
            image = ImageIO.read(new File(filePath));
            LuminanceSource source = new BufferedImageLuminanceSource(image);
            Binarizer binarizer = new HybridBinarizer(source);
            BinaryBitmap binaryBitmap = new BinaryBitmap(binarizer);
            Map<DecodeHintType, Object> hints = new HashMap<DecodeHintType, Object>();
            hints.put(DecodeHintType.CHARACTER_SET, "UTF-8");
            Result result = new MultiFormatReader().decode(binaryBitmap, hints);// 对图像进行解码
            JSONObject content = JSON.parseObject(result.getText());
            System.out.println("图片中内容：  ");
            System.out.println("author： " + content.getString("author"));
            System.out.println("zxing：  " + content.getString("zxing"));
            System.out.println("图片中格式：  ");
            System.out.println("encode： " + result.getBarcodeFormat());
        } catch (IOException e) {
            e.printStackTrace();
        } catch (NotFoundException e) {
            e.printStackTrace();
        }
    }

    /**
	 * 去除白边
	 * */
	private static BitMatrix deleteWhite(BitMatrix matrix) {
		int[] rec = matrix.getEnclosingRectangle();
		int resWidth = rec[2] + 1;
		int resHeight = rec[3] + 1;

		BitMatrix resMatrix = new BitMatrix(resWidth, resHeight);
		resMatrix.clear();
		for (int i = 0; i < resWidth; i++) {
			for (int j = 0; j < resHeight; j++) {
				if (matrix.get(i + rec[0], j + rec[1])) {
                    resMatrix.set(i, j);
                }
			}
		}
		return resMatrix;
	}

	public static BufferedImage zoomInImage(BufferedImage originalImage, int wigth, int height){
		BufferedImage newImage = new BufferedImage(wigth,height,originalImage.getType());
		Graphics g = newImage.getGraphics();
		g.drawImage(originalImage,0,0,wigth,height,null);
		g.dispose();
		return newImage;
	}

}

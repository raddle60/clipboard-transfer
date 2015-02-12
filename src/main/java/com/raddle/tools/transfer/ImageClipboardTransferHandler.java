/**
 * 
 */
package com.raddle.tools.transfer;

import java.awt.Graphics;
import java.awt.Image;
import java.awt.datatransfer.DataFlavor;
import java.awt.image.BufferedImage;
import java.awt.image.RenderedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import javax.imageio.ImageIO;

/**
 * @author raddle
 * 
 */
public class ImageClipboardTransferHandler extends AbstractClipboardTransferHandler {

	@Override
	public boolean isSupported(DataFlavor dataFlavor) {
		if (dataFlavor.getRepresentationClass() != null) {
			return Image.class.isAssignableFrom(dataFlavor.getRepresentationClass());
		}
		return false;
	}

	@Override
	public Object toClipboardData(DataFlavor dataFlavor, byte[] bytes) {
		if (!isSupported(dataFlavor)) {
			throw new IllegalArgumentException("not supported dataFlavor, " + dataFlavor);
		}
		if (bytes != null) {
			try {
				return toImage(bytes);
			} catch (IOException e) {
				throw new RuntimeException(e.getMessage(), e);
			}
		}
		return null;
	}

	@Override
	public byte[] toBytes(DataFlavor dataFlavor, Object clipboardData) {
		if (!isSupported(dataFlavor)) {
			throw new IllegalArgumentException("not supported dataFlavor, " + dataFlavor);
		}
		if (clipboardData != null) {
			try {
				return toByteArray((Image) clipboardData);
			} catch (IOException e) {
				throw new RuntimeException(e.getMessage(), e);
			}
		}
		return null;
	}

	private byte[] toByteArray(Image image) throws IOException {
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		if (image instanceof RenderedImage) {
			ImageIO.write((RenderedImage) image, "png", out);
			out.flush();
			out.close();
		} else {
			int width = image.getWidth(null);
			int height = image.getHeight(null);
			BufferedImage bufferedImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
			Graphics g = bufferedImage.getGraphics();
			g.drawImage(image, 0, 0, width, height, null);
			g.dispose();
			ImageIO.write((BufferedImage) image, "png", out);
			out.flush();
			out.close();
		}
		return out.toByteArray();
	}

	private Image toImage(byte[] bytes) throws IOException {
		return ImageIO.read(new ByteArrayInputStream(bytes));
	}
}

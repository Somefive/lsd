package edu.cmu.mat.lsd.components;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.awt.image.ImageObserver;

import javax.swing.JPanel;

import edu.cmu.mat.scores.Block;

public class JBlock extends JPanel implements ImageObserver {
	private static final long serialVersionUID = -4507069873908072713L;

	private Block _block;
	private int _width;
	private int _height;
	private BufferedImage _image;

	public JBlock(int blockHeight) {
		_height = blockHeight;
		setBackground(Color.WHITE);
	}

	public void setWidth(int width) {
		_width = width;
		this.setSize(_width, _height);
	}

	public void setBlock(Block block) {
		_block = block;
		_image = _block.getImage(_height*2);
	}

	public int getHeight() {
		return _height;
	}

	public int getImageWidth() {
		int i= _image.getWidth();
		//java.lang.System.out.println(i);
      return i;
	}

	public int getImageOffset() {
		return _width / 2 - _image.getWidth() / 2;
//        return _width / 2 - 536 / 2;
	}

	@Override
	public Dimension getPreferredSize() {
		return new Dimension(_width, _height);
	}

	@Override
	public void paint(Graphics graphics) {
		super.paint(graphics);
		Graphics2D g2 = (Graphics2D) graphics;
		g2.setRenderingHints(new RenderingHints(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY));
		int imageWidth = _image.getWidth();
//		int imageWidth = 536;
		int dx1 = _width /2 - imageWidth / 2;
		int dx2 = dx1 + imageWidth;
		g2.drawImage(_image, dx1, 0, dx2, _image.getHeight(),0, 0,
				_image.getWidth(), _image.getHeight(), Color.WHITE, null);
//		graphics.drawImage(_image, dx1, 0, dx2, _image.getHeight(), 0, 0,
//				536, _image.getHeight(), Color.WHITE, null);
	}
}

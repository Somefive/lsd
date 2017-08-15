package edu.cmu.mat.lsd.panels;

import edu.cmu.mat.lsd.Model;
import edu.cmu.mat.lsd.logger.HCMPLogger;
import edu.cmu.mat.scores.Image;
import edu.cmu.mat.scores.Page;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;

import static javax.swing.ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER;
import static javax.swing.ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS;

public class ScorePreviewPanel extends JLayeredPane {
	
	protected JPanel _panel = new JPanel();
	protected JScrollPane _scrollPane = new JScrollPane();
	protected ScorePreviewPanel self = this;
	protected BufferedImage previewImage;
	protected Thread renderingThread;
	protected int _previewWindowSideSize = 0;
	protected JLabel _previewWindow = new JLabel();
	
	public ScorePreviewPanel() {
		_panel.setLayout(new BoxLayout(_panel, BoxLayout.Y_AXIS));
		_panel.setBackground(Color.DARK_GRAY);
		_panel.add(new JLabel());
		_scrollPane.setHorizontalScrollBarPolicy(HORIZONTAL_SCROLLBAR_NEVER);
		_scrollPane.setVerticalScrollBarPolicy(VERTICAL_SCROLLBAR_ALWAYS);
		_scrollPane.getVerticalScrollBar().setUnitIncrement(16);
		_scrollPane.setViewportView(_panel);
		this.addComponentListener(new ComponentAdapter() {
			@Override
			public void componentResized(ComponentEvent e) {
				super.componentResized(e);
				_scrollPane.setSize(self.getWidth()/3, self.getHeight());
				_panel.setSize(self.getWidth()/3, self.getHeight());
				_scrollPane.setLocation(self.getWidth()*2/3, 0);
				update();
			}
		});
		_panel.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseEntered(MouseEvent e) {
				super.mouseEntered(e);
				_previewWindow.setVisible(true);
				_previewWindowSideSize = Math.min(_panel.getWidth()*2, _panel.getHeight()*2);
				_previewWindow.setSize(_previewWindowSideSize, _previewWindowSideSize);
				_previewWindow.setBackground(Color.green);
				movePreviewWindow(e);
			}

			@Override
			public void mouseExited(MouseEvent e) {
				super.mouseExited(e);
				_previewWindow.setVisible(false);
			}
		});
		_panel.addMouseMotionListener(new MouseAdapter() {
			@Override
			public void mouseMoved(MouseEvent e) {
				super.mouseMoved(e);
				if (_previewWindow.isVisible()) {
					movePreviewWindow(e);
				}
			}
		});
		this.add(_scrollPane, new Integer(1));
		this.add(_previewWindow, new Integer(2));
	}
	
	void movePreviewWindow(MouseEvent e) {
		_previewWindow.setLocation(0, e.getY() - _scrollPane.getVerticalScrollBar().getValue() - _previewWindowSideSize/2);
		java.awt.Image img = previewImage.getSubimage(0, e.getY()*2-previewImage.getWidth()/2, previewImage.getWidth(), previewImage.getWidth())
				.getScaledInstance(_previewWindowSideSize, _previewWindowSideSize, BufferedImage.SCALE_SMOOTH);
		_previewWindow.setIcon(new ImageIcon(img));
	}
	
	BufferedImage createImage(boolean highQuality, int targetWidth) {
		int totalHeight = Model.Instance.getCurrentScore().getPages().stream().map(page -> {
			BufferedImage img = page.getImage().getImage();
			return img.getHeight()*targetWidth/img.getWidth();
		}).reduce((x,y) -> x+y).orElse(0);
		BufferedImage image = new BufferedImage(targetWidth, totalHeight, BufferedImage.TYPE_INT_RGB);
		Graphics2D g2d = image.createGraphics();
		int heightCur = 0, pageCount = Model.Instance.getCurrentScore().getNumberPages();
		for (int i=0; i<pageCount; ++i) {
			Page page = Model.Instance.getCurrentScore().getPage(i);
			BufferedImage img = page.getImage().RESIZE(targetWidth, Image.DIMENSION_WIDTH, highQuality ? BufferedImage.SCALE_SMOOTH : BufferedImage.SCALE_FAST);
			g2d.drawImage(img, 0, heightCur, null);
			heightCur += img.getHeight();
		}
		g2d.dispose();
		return image;
	}
	
	void update() {
		if (renderingThread != null) renderingThread.interrupt();
		renderingThread = new Thread(() -> {
			int realWidth = _scrollPane.getWidth();
			previewImage = createImage(false, realWidth - _scrollPane.getVerticalScrollBar().getPreferredSize().width);
			((JLabel) _panel.getComponent(0)).setIcon(new ImageIcon(previewImage));
			previewImage = createImage(true, realWidth*2 - _scrollPane.getVerticalScrollBar().getPreferredSize().width);
			((JLabel) _panel.getComponent(0)).setIcon(new ImageIcon(Image.RESIZE(previewImage, realWidth - _scrollPane.getVerticalScrollBar().getPreferredSize().width, Image.DIMENSION_WIDTH, BufferedImage.SCALE_SMOOTH)));
		});
		renderingThread.start();
	}
}

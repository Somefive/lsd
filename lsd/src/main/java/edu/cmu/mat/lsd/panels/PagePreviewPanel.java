package edu.cmu.mat.lsd.panels;

import edu.cmu.mat.lsd.Model;
import edu.cmu.mat.lsd.logger.HCMPLogger;
import edu.cmu.mat.scores.Image;
import edu.cmu.mat.scores.Score;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.util.ArrayList;

public class PagePreviewPanel extends JScrollPane {
	private Model _model;
	private JPanel _panel;
	private PageNotationEditPanel _editPanel;
	private Score _score;
	private ArrayList<BufferedImage> imageBuffer = new ArrayList<>();
	private int bufferedWidth = 0;
	private PagePreviewPanel self;
	private Thread _renderThread;
	private int selectedPageIndex = 0;
	private int scrollBarWidth;
	PagePreviewPanel(Model model, PageNotationEditPanel editPanel) {
		super();
		self = this;
		_model = model;
		_editPanel = editPanel;
		_panel = new JPanel();
		_panel.setBackground(Color.DARK_GRAY);
		_panel.setLayout(new BoxLayout(_panel, BoxLayout.Y_AXIS));
		_panel.addComponentListener(new ComponentAdapter() {
			@Override
			public void componentResized(ComponentEvent e) {
				self.update();
			}
		});
		setHorizontalScrollBarPolicy(HORIZONTAL_SCROLLBAR_NEVER);
		setVerticalScrollBarPolicy(VERTICAL_SCROLLBAR_ALWAYS);
		setViewportView(_panel);
		update();
	}
	
	void update() {
		int width = getWidth()-scrollBarWidth;
		if (bufferedWidth == width) return;
		bufferedWidth = width;
		imageBuffer.clear();
		_score = _model.getCurrentScore();
		_score.getPages().forEach(page -> imageBuffer.add(page.getImage().RESIZE(
			bufferedWidth, Image.DIMENSION_WIDTH, BufferedImage.SCALE_FAST
		)));
		flushImageBuffer();
		renderWithHighQuality();
	}
	
	protected void renderWithHighQuality() {
		if (_renderThread != null && _renderThread.isAlive()) _renderThread.interrupt();
		_renderThread = new Thread(() -> {
			int size = imageBuffer.size();
			for (int index=0; index<size; ++index) {
				imageBuffer.set(index, _score.getPage(index).getImage().RESIZE(bufferedWidth, Image.DIMENSION_WIDTH, BufferedImage.SCALE_SMOOTH));
				flushImageBuffer(index);
			}
		});
		_renderThread.start();
	}
	
	protected void flushImageBuffer() {
		int size = imageBuffer.size();
		if (_panel.getComponentCount() != size) {
			_panel.removeAll();
			for (int index=0; index<size; ++index) {
				PageIcon pageIcon = new PageIcon(index);
				_panel.add(pageIcon);
				pageIcon.setImage(imageBuffer.get(index));
			}
		} else {
			for (int index=0;index<size;++index)
				flushImageBuffer(index);
		}
	}
	
	protected void flushImageBuffer(int index) {
		((PageIcon)_panel.getComponent(index)).setImage(imageBuffer.get(index));
	}
	
	@Override
	public void paint(Graphics g) {
		super.paint(g);
		scrollBarWidth = verticalScrollBar.getWidth();
		update();
	}
	
	public void select(int index) {
		PageIcon oldSelected = (PageIcon) _panel.getComponent(selectedPageIndex),
				 newSelected = (PageIcon) _panel.getComponent(index);
		oldSelected.setSelect(false);
		newSelected.setSelect(true);
		selectedPageIndex = index;
	}
	
	public class PageIcon extends JPanel {
		public final float HOVER_OPACITY = 0.55f;
		public final int SELECTED_STROKE_WIDTH = 3;
		private boolean _hover = false, _selected = false;
		private int _index;
		BufferedImage _image;
		PageIcon(int index) {
			super();
			_index = index;
			addMouseListener(new MouseAdapter() {
				@Override
				public void mouseEntered(MouseEvent e) {
					super.mouseEntered(e);
					setHover(true);
				}
				@Override
				public void mouseExited(MouseEvent e) {
					super.mouseExited(e);
					setHover(false);
				}
				@Override
				public void mouseClicked(MouseEvent e) {
					super.mouseClicked(e);
					select(_index);
				}
			});
			this.setToolTipText("Page "+(index+1));
		}
		
		void setSelect(boolean selected) {
			if (selected && !this._selected) _editPanel.scrollToPage(_index);
			this._selected = selected;
			this.repaint();
		}
		
		void setHover(boolean hover) {
			this._hover = hover;
			this.repaint();
		}
		
		void setImage(BufferedImage image) {
			this._image = image;
			this.setMinimumSize(new Dimension(image.getWidth()+scrollBarWidth, image.getHeight()));
			this.setPreferredSize(new Dimension(image.getWidth()+scrollBarWidth, image.getHeight()));
			this.setMaximumSize(new Dimension(image.getWidth()+scrollBarWidth, image.getHeight()));
			this.setSize(image.getWidth()+scrollBarWidth, image.getHeight());
			this.repaint();
		}
		
		@Override
		public void paint(Graphics g) {
			super.paint(g);
			g.setColor(Color.BLACK);
			g.fillRect(0, 0, getWidth(), getHeight());
			g.clearRect(0, 0, getWidth(), getHeight());
			((Graphics2D)g).setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER,
					(_selected || _hover) ? 1.0f : HOVER_OPACITY));
			((Graphics2D) g).setStroke(new BasicStroke(SELECTED_STROKE_WIDTH));
			g.drawImage(_image, 0, 0, _image.getWidth(), _image.getHeight(), null);
			if (_selected) g.drawRect(0, 0,
					_image.getWidth()-SELECTED_STROKE_WIDTH, _image.getHeight()-SELECTED_STROKE_WIDTH);
		}
	}
}

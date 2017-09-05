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

/**
 * This is the left preview panel for notation view.
 */
public class NotationPreviewSubPanel extends JScrollPane {
	private Model _model;
	private JPanel _panel;
	private NotationEditSubPanel _editPanel;
	private Score _score = null;
	private int _scorePageSize = -1;
	private ArrayList<BufferedImage> imageBuffer = new ArrayList<>();
	private int bufferedWidth = 0;
	private NotationPreviewSubPanel self;
	private Thread _renderThread;
	private int selectedPageIndex = 0;
	private int scrollBarWidth;
	NotationPreviewSubPanel(Model model, NotationEditSubPanel editPanel) {
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
	
	/**
	 * This function will check the necessarily of updating and if it is necessary to update then it will cost some time and some computation resource to do it.
	 * If it runs, it will be time consuming. However, since there is some check mechanism inside, you can call it easily.
	 */
	void update() {
		int width = getWidth()-scrollBarWidth;
		// Here is the check mechanism
		if (bufferedWidth == width && _model.getCurrentScore() == _score && (_model.getCurrentScore() == null || _model.getCurrentScore().getPages().size() == _scorePageSize)) return;
		_scorePageSize = _model.getCurrentScore().getPages().size();
		bufferedWidth = width;
		if (bufferedWidth == 0) return;
		imageBuffer.clear();
		_score = _model.getCurrentScore();
		_score.getPages().forEach(page -> imageBuffer.add(page.getImage().RESIZE(
			bufferedWidth, Image.DIMENSION_WIDTH, BufferedImage.SCALE_FAST
		)));
		flushImageBuffer();
		renderWithHighQuality();
	}
	
	/**
	 * Rendering in high quality is really time-costing. There is a thread that set up for doing it.
	 * This function will resize all the page images with smooth scale. So the quality is good but it responses slow.
	 */
	protected void renderWithHighQuality() {
		if (_renderThread != null && _renderThread.isAlive()) _renderThread.interrupt();
		_renderThread = new Thread(() -> {
			int size = imageBuffer.size();
			for (int index=0; index<size; ++index) {
				imageBuffer.set(index, _score.getPage(index).getImage().RESIZE(bufferedWidth, Image.DIMENSION_WIDTH, BufferedImage.SCALE_SMOOTH));
				flushImageBuffer(index);
			}
			_panel.revalidate();
			_panel.repaint();
		});
		_renderThread.start();
	}
	
	/**
	 * When the image buffer is prepared, you can call this function to flush the buffer to screen components.
	 */
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
		_panel.revalidate();
		_panel.repaint();
	}
	
	/**
	 * Only flush one specified image.
	 * @param index The index of page that buffer should be flushed.
	 */
	protected void flushImageBuffer(int index) {
		((PageIcon)_panel.getComponent(index)).setImage(imageBuffer.get(index));
	}
	
	@Override
	public void paint(Graphics g) {
		super.paint(g);
		scrollBarWidth = verticalScrollBar.getWidth();
		update();
	}
	
	/**
	 * There is a select border for the selected image.
	 * This is used for changing render options for image icon.
	 * @param index The index of current selected page.
	 */
	public void select(int index) {
		PageIcon oldSelected = (PageIcon) _panel.getComponent(selectedPageIndex),
				 newSelected = (PageIcon) _panel.getComponent(index);
		oldSelected.setSelect(false);
		newSelected.setSelect(true);
		selectedPageIndex = index;
	}
	
	/**
	 * This is a sub-component contained in the panel. One pageIcon includes one page. If this page is selected, then the additional border will be printed.
	 */
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

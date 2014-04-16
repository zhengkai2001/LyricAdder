package com.zhengkai.media;

import javax.swing.JLabel;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Desktop;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;

/**
 * 带有超链接的JLabel
 * 
 * @author zhengkai
 * @date 2014年4月15日
 */
class URLLabel extends JLabel {
	private static final long serialVersionUID = 8358220090263304480L;

	// 超链接
	private String urlText;

	// 标签文字的本来颜色
	private Color normalColor;

	/**
	 * 根据指定文字和超链接构造URLLabel
	 * 
	 * @param text
	 *        指定的文字
	 * @param urlText
	 *        指定的超链接文字
	 */
	public URLLabel(String text, String urlText) {
		super("<html><u>" + text + "</u></html>");
		if (urlText.startsWith("http://") || urlText.startsWith("https://")) {
			this.urlText = urlText;
		} else {
			this.urlText = "http://" + urlText;
		}
		normalColor = URLLabel.this.getForeground();

		this.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseEntered(MouseEvent e) {
				URLLabel.this.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
				URLLabel.this.setForeground(Color.BLUE);
			}

			@Override
			public void mouseExited(MouseEvent e) {
				URLLabel.this.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
				URLLabel.this.setForeground(normalColor);
			}

			@Override
			public void mouseClicked(MouseEvent e) {
				try {
					URL url = new URL(URLLabel.this.urlText);
					Desktop.getDesktop().browse(url.toURI());
				} catch (IOException | URISyntaxException e1) {
					e1.printStackTrace();
				}
			}
		});
	}
}
package com.zhengkai.media;

import java.io.OutputStream;
import java.io.PrintStream;
import javax.swing.SwingUtilities;
import javax.swing.text.JTextComponent;

/**
 * 可以定向到指定GUI组件上的输出流
 * 
 * @author zhengkai
 * @date 2014年4月15日
 */
public class GUIPrintStream extends PrintStream {
	private StringBuffer stringBuffer;
	private JTextComponent component;

	public GUIPrintStream(OutputStream out, JTextComponent component) {
		super(out);
		stringBuffer = new StringBuffer();
		this.component = component;
	}

	@Override
	public void write(byte[] buf, int off, int len) {
		final String message = new String(buf, off, len);
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				stringBuffer.append(message);
				component.setText(stringBuffer.toString());
			}
		});
	}

	public void clear() {
		stringBuffer = new StringBuffer();
		component.setText(stringBuffer.toString());
	}
}
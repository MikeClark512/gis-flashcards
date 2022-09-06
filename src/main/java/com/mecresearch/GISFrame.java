package com.mecresearch;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Image;
import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.HashMap;
import java.util.Map;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.text.BadLocationException;
import javax.swing.text.Utilities;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import serpapi.GoogleSearch;
import serpapi.SerpApiSearchException;
import java.awt.Font;
import javax.swing.JTextArea;
import javax.swing.SwingConstants;

public class GISFrame extends JFrame {

	private JPanel contentPane;
	private JTextField textField;
	private JLabel lblNewLabel;
	private BufferedImage _bufferedImage;
	private ImageIcon _scaledImage;
	private JTextField textFieldWord;
	private JsonArray _array;
	private String _lastSearch;
	private int _lastIndex;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					GISFrame frame = new GISFrame();
					Dimension desktopSize = getDesktopSize(frame);
					frame.setSize(1080, desktopSize.height);
					frame.setLocationRelativeTo(null);
					frame.setVisible(true);
//					frame.setExtendedState(frame.getExtendedState() | JFrame.MAXIMIZED_BOTH);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	public static Dimension getDesktopSize(Component c) {
		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		Insets scnMax = Toolkit.getDefaultToolkit().getScreenInsets(c.getGraphicsConfiguration());
		int insetWidth = Math.abs(scnMax.left - scnMax.right);
		int insetHeight = Math.abs(scnMax.bottom - scnMax.top);
		int availWidth = screenSize.width - insetWidth;
		int availHeight = screenSize.height - insetHeight;
		return new Dimension(availWidth, availHeight);
	}

	/**
	 * Create the frame.
	 */
	public GISFrame() {
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		contentPane = new JPanel();
		setContentPane(contentPane);
		contentPane.setLayout(new BorderLayout(0, 0));

		JPanel panelNorth = new JPanel();
		contentPane.add(panelNorth, BorderLayout.NORTH);
		GridBagLayout gbl_panelNorth = new GridBagLayout();
		panelNorth.setLayout(gbl_panelNorth);

		textField = new JTextField();
		textField.setHorizontalAlignment(SwingConstants.CENTER);
		textField.setFont(new Font("Tahoma", Font.BOLD, 31));
		textField.addKeyListener(new KeyAdapter() {
			@Override
			public void keyReleased(KeyEvent e) {
				if (e.getKeyCode() == KeyEvent.VK_ENTER) {
					textField.setText("");
				}
			}
		});
		textField.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseReleased(MouseEvent e) {
				try {
					if (SwingUtilities.isRightMouseButton(e)) {
						int offset = textField.viewToModel2D(e.getPoint());
						int start = Utilities.getWordStart(textField, offset);
						int end = Utilities.getWordEnd(textField, offset);
						String word = textField.getText(start, end - start);
						textFieldWord.setText(word);
						search(word);
					}
				} catch (BadLocationException e1) {
					e1.printStackTrace();
				}
			}
		});
		GridBagConstraints gbc_textField = new GridBagConstraints();
		gbc_textField.insets = new Insets(0, 9, 5, 9);
		gbc_textField.fill = GridBagConstraints.BOTH;
		gbc_textField.weighty = 1.0;
		gbc_textField.weightx = 1.0;
		gbc_textField.gridx = 0;
		gbc_textField.gridy = 0;
		panelNorth.add(textField, gbc_textField);
		textField.setColumns(10);

		JPanel panelWest = new JPanel();
		contentPane.add(panelWest, BorderLayout.WEST);

		JPanel panelEast = new JPanel();
		contentPane.add(panelEast, BorderLayout.EAST);

		JPanel panelCenter = new JPanel();
		contentPane.add(panelCenter, BorderLayout.CENTER);
		GridBagLayout gbl_panelCenter = new GridBagLayout();
		panelCenter.setLayout(gbl_panelCenter);

		lblNewLabel = new JLabel((String) null);
		lblNewLabel.addComponentListener(new ComponentAdapter() {
			@Override
			public void componentResized(ComponentEvent e) {
				super.componentResized(e);
				drawImage();
			}
		});
		GridBagConstraints gbc_lblNewLabel = new GridBagConstraints();
		gbc_lblNewLabel.weighty = 1.0;
		gbc_lblNewLabel.weightx = 1.0;
		gbc_lblNewLabel.fill = GridBagConstraints.BOTH;
		gbc_lblNewLabel.gridx = 0;
		gbc_lblNewLabel.gridy = 0;
		panelCenter.add(lblNewLabel, gbc_lblNewLabel);

		JPanel panelSouth = new JPanel();
		contentPane.add(panelSouth, BorderLayout.SOUTH);
		GridBagLayout gbl_panelSouth = new GridBagLayout();
		panelSouth.setLayout(gbl_panelSouth);

		textFieldWord = new JTextField();
		textFieldWord.setHorizontalAlignment(SwingConstants.CENTER);
		textFieldWord.setFont(new Font("Tahoma", Font.PLAIN, 180));
		textFieldWord.setEditable(false);
		GridBagConstraints gbc_textFieldWord = new GridBagConstraints();
		gbc_textFieldWord.insets = new Insets(0, 9, 0, 9);
		gbc_textFieldWord.weighty = 1.0;
		gbc_textFieldWord.weightx = 1.0;
		gbc_textFieldWord.fill = GridBagConstraints.BOTH;
		gbc_textFieldWord.gridx = 0;
		gbc_textFieldWord.gridy = 0;
		panelSouth.add(textFieldWord, gbc_textFieldWord);
	}

	protected void search(String word) {
		if (_lastSearch != null && _lastSearch.equalsIgnoreCase(word)) {
			_lastIndex++;
			findImage(word, _lastIndex);
			return;
		}
		try {
			Map<String, String> parameter = new HashMap<>();
			parameter.put("q", word + " black and white drawing -stock");
			parameter.put("tbm", "isch");
			parameter.put("ijn", "0");
			parameter.put("num", "10");
			GoogleSearch search = new GoogleSearch(parameter);
			GoogleSearch.serp_api_key_default = System.getenv("SERP_API_KEY");
			search.buildQuery("/search", "html");
			JsonObject results = search.getJson();

			_array = results.getAsJsonArray("images_results");
			_lastIndex = findImage(word, 0);

		} catch (SerpApiSearchException e) {
			e.printStackTrace();
		}
	}

	private int findImage(String word, int startI) {
		System.out.println("findImage word=" + word + " startI=" + startI);
		for (int i = startI; i < _array.size(); i++) {
			try {
				drawElement(i);
				_lastSearch = word;
				System.out.println("findImage returning i=" + i);
				return i;
			} catch (IOException e) {
				e.printStackTrace();
				continue;
			}
		}
		return -1;
	}

	private void drawElement(int i) throws MalformedURLException, IOException {
		JsonObject element = _array.get(i).getAsJsonObject();
		String url = element.get("original").getAsString();
		System.out.println("url=" + url);
		_bufferedImage = getBufferedImageForURL(url);
		drawImage();
	}

	private void drawImage() {
		if (_bufferedImage == null) {
			return;
		}
		_scaledImage = scaleImage(_bufferedImage, lblNewLabel.getWidth(), lblNewLabel.getHeight());
		lblNewLabel.setIcon(_scaledImage);
		lblNewLabel.invalidate();
		lblNewLabel.repaint();
		this.invalidate();
		this.repaint();
	}

	private BufferedImage getBufferedImageForURL(String url) throws MalformedURLException, IOException {
		URL u = new URL(url);
		String ua = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/105.0.0.0 Safari/537.36";
		URLConnection connection = u.openConnection();
		connection.setRequestProperty("User-Agent", ua);
		InputStream is = connection.getInputStream();
		BufferedImage bi = ImageIO.read(is);
		if (bi == null) {
			throw new IOException("null image");
		}
		return bi;
	}

	private ImageIcon scaleImage(BufferedImage bi, int width, int height) {
		Image scaledInstance = bi.getScaledInstance(width, height, Image.SCALE_AREA_AVERAGING);
		ImageIcon imageIcon = new ImageIcon(scaledInstance);
		return imageIcon;
	}

}

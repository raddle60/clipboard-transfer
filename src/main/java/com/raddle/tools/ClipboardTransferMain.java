package com.raddle.tools;

import java.awt.Frame;
import java.awt.SystemTray;
import java.awt.Toolkit;
import java.awt.TrayIcon;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketAddress;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Properties;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingDeque;

import javax.imageio.ImageIO;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

import org.apache.commons.lang3.StringUtils;
import org.omg.CORBA.BooleanHolder;

import com.raddle.tools.monitor.ClipboardListener;
import com.raddle.tools.monitor.impl.TextClipboardMonitor;

/**
 * This code was edited or generated using CloudGarden's Jigloo SWT/Swing GUI
 * Builder, which is free for non-commercial use. If Jigloo is being used
 * commercially (ie, by a corporation, company or business for any purpose
 * whatever) then you should purchase a license for each developer using Jigloo.
 * Please visit www.cloudgarden.com for details. Use of Jigloo implies
 * acceptance of these licensing terms. A COMMERCIAL LICENSE HAS NOT BEEN
 * PURCHASED FOR THIS MACHINE, SO JIGLOO OR THIS CODE CANNOT BE USED LEGALLY FOR
 * ANY CORPORATE OR COMMERCIAL PURPOSE.
 */
public class ClipboardTransferMain extends javax.swing.JFrame {

    private static final long serialVersionUID = 1L;
    private JButton remoteClipSetBtn;
    private JLabel serverLeb;
    private JCheckBox autoChk;
    private JButton clearBtn;
    private JTextField serverAddrTxt;
    private JCheckBox modifyClipChk;
    private JTextField portTxt;
    private JLabel jLabel3;
    private JButton clipServerStopBtn;
    private JButton clipServerStartBtn;
    private JLabel jLabel2;
    private JLabel jLabel1;
    private JButton remoteClipGetBtn;
    private ServerSocket server;
    private boolean tostop = false;
    // 双向同步，防止并发来回
    private volatile boolean isProcessing = false;
    private TextClipboardMonitor m = new TextClipboardMonitor(Toolkit.getDefaultToolkit().getSystemClipboard());
    private TrayIcon trayIcon = null;
	private BufferedImage pasteImage = null;  //  @jve:decl-index=0:
	private BufferedImage grayImage = null;
	private BufferedImage sendImage = null;
	private BlockingQueue<String> iconQueue = new LinkedBlockingDeque<String>();
    private JTextArea messageArea;
    {
        // Set Look & Feel
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Auto-generated main method to display this JFrame
     */
    public static void main(final String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                ClipboardTransferMain inst = new ClipboardTransferMain();
                inst.setLocationRelativeTo(null);
                inst.setDefaultCloseOperation(EXIT_ON_CLOSE);
				if (args != null) {
					for (String string : args) {
						if ("-s".equals(string)) {
							inst.startServer();
						}
					}
					for (String string : args) {
						if ("-m".equals(string)) {
							inst.setState(Frame.ICONIFIED);
						}
					}
				}
				if (inst.getState() != Frame.ICONIFIED) {
					inst.setVisible(true);
				}
            }
        });
    }

    public ClipboardTransferMain() {
        super();
        initGUI();
        //// 初始化配置
        Properties p =new Properties();
		File pf = new File(System.getProperty("user.home") + "/clip-trans/conf.properties");
		if (pf.exists()) {
			try {
				p.load(new FileInputStream(pf));
				serverAddrTxt.setText(StringUtils.defaultString(p.getProperty("server.addr")));
				portTxt.setText(StringUtils.defaultString(p.getProperty("local.port")));
				modifyClipChk.setSelected("true".equals(p.getProperty("allow.modify.local.clip")));
				autoChk.setSelected("true".equals(p.getProperty("auto.modify.remote.clip")));
			} catch (Exception e) {
				updateMessage(e.getMessage());
			}
		}
		m.addListener(new ClipboardListener() {

			@Override
			public void contentChanged(Clipboard clipboard) {
				setRemoteClipboard(false);
			}
		});
		m.setEnabled(autoChk.isSelected());
		//
		try {
			pasteImage = ImageIO.read(ClipboardTransferMain.class.getResourceAsStream("/clipboard_paste.png"));
			grayImage = ImageIO.read(ClipboardTransferMain.class.getResourceAsStream("/clipboard_gray.png"));
			sendImage = ImageIO.read(ClipboardTransferMain.class.getResourceAsStream("/mail-send.png"));
			BufferedImage taskImage = ImageIO.read(ClipboardTransferMain.class.getResourceAsStream("/taskbar.png"));
			setIconImage(taskImage);
			SystemTray systemTray = SystemTray.getSystemTray();
			trayIcon = new TrayIcon(grayImage, "剪切板同步");
			systemTray.add(trayIcon);
			////
			trayIcon.addMouseListener(new MouseAdapter() {
				@Override
                public void mouseClicked(MouseEvent e) {
					if (e.getClickCount() == 2) {// 双击托盘窗口再现
						if (ClipboardTransferMain.this.isVisible()) {
							ClipboardTransferMain.this.setState(ICONIFIED);
						} else {
							ClipboardTransferMain.this.setVisible(true);
							ClipboardTransferMain.this.setState(NORMAL);
						}
					}
				}
			});
			////// event 
			this.addWindowListener(new WindowAdapter() {

				@Override
				public void windowIconified(WindowEvent e) {
					ClipboardTransferMain.this.setVisible(false);
					super.windowIconified(e);
				}
				
				@Override
				public void windowClosing(WindowEvent e) {
					File pf = new File(System.getProperty("user.home") + "/clip-trans/conf.properties");
					pf.getParentFile().mkdirs();
					try {
						Properties op = new Properties();
						op.setProperty("server.addr", serverAddrTxt.getText());
						op.setProperty("local.port", portTxt.getText());
						op.setProperty("allow.modify.local.clip", modifyClipChk.isSelected() + "");
						op.setProperty("auto.modify.remote.clip", autoChk.isSelected() + "");
						FileOutputStream os = new FileOutputStream(pf);
						op.store(os, "clip-trans");
						os.flush();
						os.close();
					} catch (Exception e1) {
					}
					shutdown();
					super.windowClosing(e);
				}
			});
		} catch (Exception e) {
			updateMessage(e.getMessage());
		}
		Thread thread = new Thread() {

			@Override
			public void run() {
				while (true) {
					try {
						String poll = iconQueue.take();
						if ("send".equals(poll)) {
							trayIcon.setImage(grayImage);
						} else if ("paste".equals(poll)) {
							Thread.sleep(20);
							trayIcon.setImage(grayImage);
						}
					} catch (InterruptedException e1) {
						return;
					}
				}
			}
		};
		thread.setDaemon(true);
		thread.start();
    }

    private void initGUI() {
        try {
            {
                this.setTitle("\u8fdc\u7a0b\u526a\u5207\u677f");
                getContentPane().setLayout(null);
                {
                    remoteClipGetBtn = new JButton();
                    getContentPane().add(remoteClipGetBtn);
                    remoteClipGetBtn.setText("\u83b7\u5f97\u8fdc\u7a0b\u526a\u5207\u677f");
                    remoteClipGetBtn.setBounds(12, 22, 151, 29);
                    remoteClipGetBtn.addActionListener(new ActionListener() {
                        @Override
                        public void actionPerformed(ActionEvent evt) {
                            doInSocket(new SocketCallback() {

                                @Override
                                public Object connected(Socket socket) throws Exception {
                                    if (!isProcessing) {
                                        isProcessing = true;
                                        try {
                                            ClipCommand cmd = new ClipCommand();
                                            cmd.setCmdCode(ClipCommand.CMD_GET_CLIP);
                                            updateMessage("远程剪切板获取中");
                                            // 发送命令
                                            ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
                                            out.writeObject(cmd);
                                            // 获得结果
                                            ObjectInputStream in = new ObjectInputStream(socket.getInputStream());
                                            ClipResult result = (ClipResult) in.readObject();
                                            if (result.isSuccess()) {
                                            	setLocalClipboard(result);
                                                StringBuilder sb = new StringBuilder();
                                                for (DataFlavor dataFlavor : result.getClipdata().keySet()) {
                                                    sb.append("\n");
                                                    sb.append(dataFlavor.getPrimaryType()).append("/").append(dataFlavor.getSubType());
                                                }
                                                updateMessage("获得剪切板成功，剪切板类型 " + sb);
                                            } else {
                                                updateMessage("获得剪切板失败:" + result.getMessage());
                                            }
                                            in.close();
                                            out.close();
										} catch (Exception e) {
											updateMessage("获得剪切板失败:" + e.getMessage());
										} finally {
											isProcessing = false;
										}
                                    }
                                    return null;
                                }
                            });
                        }
                    });
                }
                {
                    remoteClipSetBtn = new JButton();
                    getContentPane().add(remoteClipSetBtn);
                    remoteClipSetBtn.setText("\u8bbe\u7f6e\u8fdc\u7a0b\u526a\u5207\u677f");
                    remoteClipSetBtn.setBounds(181, 22, 159, 29);
                    remoteClipSetBtn.addActionListener(new ActionListener() {
                        @Override
                        public void actionPerformed(ActionEvent evt) {
                            setRemoteClipboard(true);
                        }
                    });
                }
                {
                    serverLeb = new JLabel();
                    getContentPane().add(serverLeb);
                    serverLeb.setText("\u8fdc\u7a0b\u670d\u52a1\u5668\u5730\u5740(IP:PORT)");
                    serverLeb.setBounds(12, 63, 162, 17);
                }
                {
                    serverAddrTxt = new JTextField();
                    getContentPane().add(serverAddrTxt);
                    serverAddrTxt.setBounds(169, 58, 186, 27);
                }
                {
                    jLabel1 = new JLabel();
                    getContentPane().add(jLabel1);
                    jLabel1.setText("\u6d88\u606f\uff1a");
                    jLabel1.setBounds(12, 97, 48, 24);
                }
                {
                    jLabel2 = new JLabel();
                    getContentPane().add(jLabel2);
                    jLabel2.setText("\u672c\u5730\u526a\u5207\u677f\u670d\u52a1");
                    jLabel2.setBounds(12, 297, 91, 20);
                }
                {
                    clipServerStartBtn = new JButton();
                    getContentPane().add(clipServerStartBtn);
                    clipServerStartBtn.setText("\u542f\u52a8");
                    clipServerStartBtn.setBounds(12, 329, 79, 29);
                    clipServerStartBtn.addActionListener(new ActionListener() {
                        @Override
                        public void actionPerformed(ActionEvent evt) {
                            startServer();
                        }
                    });
                }
                {
                    clipServerStopBtn = new JButton();
                    getContentPane().add(clipServerStopBtn);
                    clipServerStopBtn.setText("\u505c\u6b62");
                    clipServerStopBtn.setBounds(103, 329, 81, 29);
                    clipServerStopBtn.setEnabled(false);
                    clipServerStopBtn.addActionListener(new ActionListener() {
                        @Override
                        public void actionPerformed(ActionEvent evt) {
                            shutdown();
                        }
                    });
                }
                {
                    jLabel3 = new JLabel();
                    getContentPane().add(jLabel3);
                    jLabel3.setText("\u7aef\u53e3\uff1a");
                    jLabel3.setBounds(196, 335, 44, 17);
                }
                {
                    portTxt = new JTextField();
                    getContentPane().add(portTxt);
                    portTxt.setText("11221");
                    portTxt.setBounds(252, 330, 88, 27);
                }
                {
                    modifyClipChk = new JCheckBox();
                    getContentPane().add(modifyClipChk);
                    modifyClipChk.setText("\u5141\u8bb8\u8fdc\u7a0b\u4fee\u6539\u526a\u5207\u677f");
                    modifyClipChk.setBounds(12, 377, 172, 22);
                }
                {
                    clearBtn = new JButton();
                    getContentPane().add(clearBtn);
                    clearBtn.setText("\u6e05\u7a7a\u672c\u5730\u7cfb\u7edf\u526a\u5207\u677f");
                    clearBtn.setBounds(196, 374, 159, 29);
                    clearBtn.addActionListener(new ActionListener() {
                        @Override
                        public void actionPerformed(ActionEvent evt) {
                            Clipboard sysc = Toolkit.getDefaultToolkit().getSystemClipboard();
                            Transferable tText = new StringSelection(null);
                            sysc.setContents(tText, null);
                        }
                    });
                }
                {
                    autoChk = new JCheckBox();
                    autoChk.addActionListener(new ActionListener() {
                    	@Override
                        public void actionPerformed(ActionEvent e) {
							m.setEnabled(autoChk.isSelected());
                    	}
                    });
                    getContentPane().add(autoChk);
                    autoChk.setText("\u81ea\u52a8\u8bbe\u7f6e\u8fdc\u7a0b\u526a\u5207\u677f");
                    autoChk.setBounds(12, 405, 172, 22);
                }
            }

            JScrollPane scrollPane = new JScrollPane();
            scrollPane.setBounds(55, 97, 542, 199);
            getContentPane().add(scrollPane);
            {
                messageArea = new JTextArea();
                scrollPane.setViewportView(messageArea);
            }
            this.setSize(611, 465);
            {
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private class CommandReceiveThread extends Thread {

        private Socket client;

        public CommandReceiveThread(Socket client) {
            this.client = client;
        }

        @Override
        public void run() {
            try {
                if (!isProcessing) {
                    isProcessing = true;
                    client.setSoTimeout(5000);
                    ObjectInputStream in = new ObjectInputStream(client.getInputStream());
                    ClipCommand command = (ClipCommand) in.readObject();
                    ClipResult result = new ClipResult();
                    if (ClipCommand.CMD_SHUTDOWN.equals(command.getCmdCode())) {
                        tostop = true;
                        result.setSuccess(true);
                    } else if (ClipCommand.CMD_GET_CLIP.equals(command.getCmdCode())) {
                        result = ClipboardUtils.getClipResult();
                    } else if (ClipCommand.CMD_SET_CLIP.equals(command.getCmdCode())) {
                        if (modifyClipChk.isSelected()) {
                            ClipResult received = command.getResult();
                            setLocalClipboard(received);
                            StringBuilder sb = new StringBuilder();
                            for (DataFlavor dataFlavor : received.getClipdata().keySet()) {
                                sb.append("\n");
                                sb.append(dataFlavor.getPrimaryType()).append("/").append(dataFlavor.getSubType());
                            }
                            result.setSuccess(true);
                            updateMessage("接收剪切板成功，剪切板类型 " + sb);
                        } else {
                            result.setSuccess(false);
                            result.setMessage("服务端禁止设置剪切板");
                        }
                    } else {
                        result.setSuccess(false);
                        result.setMessage("服务端禁止设置剪切板");
                    }
                    ObjectOutputStream out = new ObjectOutputStream(client.getOutputStream());
                    out.writeObject(result);
                    in.close();
                    out.close();
                    client.close();
                }
            } catch (Exception e) {
                e.printStackTrace();
                ClipResult result = new ClipResult();
                result.setSuccess(false);
                result.setMessage(e.getMessage());
                try {
                    ObjectOutputStream out = new ObjectOutputStream(client.getOutputStream());
                    out.writeObject(result);
                    client.close();
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            } finally {
                isProcessing = false;
            }
        }

    }

    private void updateMessage(String msg) {
        SimpleDateFormat df = new SimpleDateFormat("HH:mm:ss");
        messageArea.setText(df.format(new Date()) + " " + msg);
    }

    private void shutdown() {
        Socket socket = null;
        try {
            tostop = true;
            socket = new Socket("127.0.0.1", Integer.parseInt(portTxt.getText()));
            socket.setSoTimeout(2000);
            ClipCommand cmd = new ClipCommand();
            cmd.setCmdCode(ClipCommand.CMD_SHUTDOWN);
            // 发送命令
            ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
            out.writeObject(cmd);
        } catch (Exception e) {
            updateMessage("停止服务失败:" + e.getMessage());
        } finally {
            if (socket != null) {
                try {
                    socket.close();
                } catch (IOException e) {
                }
            }
        }
    }

    private Object doInSocket(SocketCallback callback) {
        Socket socket = null;
        try {
            String address = serverAddrTxt.getText();
            String[] ipport = address.split(":");
            if (ipport.length != 2) {
                updateMessage("服务器地址格式不正确");
                return null;
            }
            socket = new Socket();
            SocketAddress socketAddress = new InetSocketAddress(ipport[0], Integer.parseInt(ipport[1]));
            socket.connect(socketAddress, 2000);
            socket.setSoTimeout(10000);
            return callback.connected(socket);
        } catch (Exception e) {
            e.printStackTrace();
            updateMessage("连接服务器失败：" + e.getMessage());
            return null;
        } finally {
            if (socket != null) {
                try {
                    socket.getInputStream().close();
                } catch (IOException e) {
                }
                try {
                    socket.getOutputStream().close();
                } catch (IOException e) {
                }
                try {
                    socket.close();
                } catch (IOException e) {
                }
            }
        }
    }

    private void setRemoteClipboard(boolean alert) {
        if (!isProcessing) {
            isProcessing = true;
            try {
                Clipboard sysc = Toolkit.getDefaultToolkit().getSystemClipboard();
                Transferable clipT = sysc.getContents(null);
                if (alert && !ClipboardUtils.isClipboardNotEmpty(clipT)) {
                	updateMessage("剪切板内容为空");
                    return;
                }
                updateMessage("剪切板内容发送中");
				trayIcon.setImage(sendImage);
                final BooleanHolder success =  new BooleanHolder();
                doInSocket(new SocketCallback() {

                    @Override
                    public Object connected(Socket socket) throws Exception {
                        ClipCommand cmd = new ClipCommand();
                        cmd.setCmdCode(ClipCommand.CMD_SET_CLIP);
                        cmd.setResult(ClipboardUtils.getClipResult());
                        // 发送命令
                        ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
                        out.writeObject(cmd);
                        // 获得结果
                        ObjectInputStream in = new ObjectInputStream(socket.getInputStream());
                        ClipResult result = (ClipResult) in.readObject();
                        if (result.isSuccess()) {
                            StringBuilder sb = new StringBuilder();
                            for (DataFlavor dataFlavor : cmd.getResult().getClipdata().keySet()) {
                                sb.append("\n");
                                sb.append(dataFlavor.getPrimaryType()).append("/").append(dataFlavor.getSubType());
                            }
                            iconQueue.add("send");
                            success.value = true;
                            updateMessage("发送剪切板成功，剪切板类型 " + sb);
                        } else {
                        	updateMessage("发送剪切板失败:" + result.getMessage());
                        }
                        in.close();
                        out.close();
                        return null;
                    }
                });
				if (!success.value) {
					trayIcon.setImage(grayImage);
				}
			} catch (Exception e) {
				trayIcon.setImage(grayImage);
				updateMessage("发送剪切板失败：" + e.getMessage());
			} finally {
                isProcessing = false;
            }
        }
    }

	/**
	 * 防止循环触发变更通知，需要同步
	 * @param received
	 */
	private synchronized void setLocalClipboard(ClipResult received) {
		m.setEnabled(false);
		try {
			trayIcon.setImage(pasteImage);
			ClipboardUtils.setClipResult(received);
			try {
				Thread.sleep(50);
			} catch (InterruptedException e) {
			}
			m.reset();
			iconQueue.add("paste");
		} catch (Exception e) {
			trayIcon.setImage(grayImage);
			updateMessage("粘帖剪切板失败：" + e.getMessage());
		}  finally {
			m.setEnabled(autoChk.isSelected());
		}
	}

	public synchronized void startServer() {
		if (!clipServerStartBtn.isEnabled()) {
			return;
		}
		try {
			final int port = Integer.parseInt(portTxt.getText());
			Thread t = new Thread() {
				@Override
				public void run() {
					try {
						// 给关闭留点时间,保证重启时服务已经停止
						Thread.sleep(200);
						server = new ServerSocket(port);
						//
						clipServerStartBtn.setEnabled(false);
						clipServerStopBtn.setEnabled(true);
						portTxt.setEnabled(false);
						tostop = false;
						while (!tostop) {
							Socket client = server.accept();
							if (!tostop) {
								CommandReceiveThread t = new CommandReceiveThread(client);
								t.start();
							}
						}
						server.close();
						Thread.sleep(200);
						clipServerStartBtn.setEnabled(true);
						clipServerStopBtn.setEnabled(false);
						portTxt.setEnabled(true);
					} catch (Exception e) {
						throw new RuntimeException(e.getMessage(), e);
					}
				}
			};
			t.start();
		} catch (NumberFormatException e) {
			updateMessage("端口号错误:" + e.getMessage());
		}
	}

	private interface SocketCallback {

        public Object connected(Socket socket) throws Exception;
    }
}

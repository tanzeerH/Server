package com.networking.server;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.Label;
import java.awt.ScrollPane;
import java.awt.TextField;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.plaf.SliderUI;

import com.networking.server.Common.Common;
import com.networking.server.model.Constraints;
import com.networking.server.model.Student;

public class Server extends JFrame implements ActionListener {

	private JTextField txtDirectory, txtFiletypes, txtNum, txtMaxSize,
			txtStudentID, txtFileNames;
	private JLabel lblDirectory, lblFiletypes, lblNum, lblMaxSize,
			lblStudentId, lblFolder, lblMB, lblDemo, lblRoot, lblFileNames;
	private Socket socket;
	private ServerSocket serverSocket;
	private JTextArea serverText;
	int num_of_connection = 0;
	JCheckBox chBoxFolder;
	JButton btnConfigure;
	// HashMap<String,>
	ArrayList<String> studentList = new ArrayList<String>();
	ServerData serverData = new ServerData(Server.this);
	boolean isServerStarted = false;
	private Constraints serverConstraints = new Constraints();
	String rootDir = "";
	int CHUNK_SIZE = 512;
	int count = 0;

	// InputStream inputStream;

	public Server() {
		super("Server");
		lblDirectory = new JLabel("Direcoory: ");
		lblFiletypes = new JLabel("File types: ");
		lblNum = new JLabel("Num of Files:");
		lblMaxSize = new JLabel("Max Size: ");
		lblMB = new JLabel("MB");
		lblStudentId = new JLabel("Student Id: ");
		lblFileNames = new JLabel("File Names");
		lblFolder = new JLabel(
				"Folder Enabled:                                                                    ");
		btnConfigure = new JButton("   Configure     ");
		lblDemo = new JLabel("                              ");
		lblRoot = new JLabel();
		lblRoot.setVisible(false);
		btnConfigure.setSize(300, 20);
		btnConfigure.addActionListener(this);
		setLayout(new FlowLayout());

		txtDirectory = new JTextField(35);
		txtFiletypes = new JTextField(35);
		txtNum = new JTextField(35);
		txtMaxSize = new JTextField(30);
		txtStudentID = new JTextField(35);
		txtFileNames = new JTextField(35);
		chBoxFolder = new JCheckBox();
		chBoxFolder.setSize(100, 20);

		serverText = new JTextArea(10, 40);
		// serverText.setSize(400,400);
		add(lblDirectory);
		add(txtDirectory);
		add(lblFiletypes);
		add(txtFiletypes);
		add(lblNum);
		add(txtNum);
		add(lblMaxSize);
		add(txtMaxSize);
		add(lblMB);
		add(lblStudentId);
		add(txtStudentID);
		add(lblFileNames);
		add(txtFileNames);
		add(lblFolder);
		add(chBoxFolder);
		add(lblDemo);
		add(btnConfigure);
		add(new JScrollPane(serverText));
		setSize(500, 500);
		setVisible(true);

	}

	public void runServer() {
		try {
			serverSocket = new ServerSocket(12345, 100);
			while (true) {
				waitForConnection();
				// getStream();
				createThreadForNewConnection();
			}
		} catch (IOException e) {

			e.printStackTrace();
		}
	}

	private void waitForConnection() {
		displayMessage("Main Thread Waiting for Connection\n");
		try {
			socket = serverSocket.accept();
			num_of_connection += 1;
			displayMessage("Connecion" + "received from "
					+ socket.getRemoteSocketAddress().toString()
					+ "\nconnection no: " + num_of_connection);
		} catch (IOException e) {

			e.printStackTrace();
		}

	}

	/*
	 * private void getStream() { try { InputStream inputStream =
	 * socket.getInputStream(); displayMessage("server: got stream"); } catch
	 * (IOException e) {
	 * 
	 * e.printStackTrace(); } }
	 */

	private void createThreadForNewConnection() {
		try {
			InputStream inputStream = socket.getInputStream();
			OutputStream outputStream = socket.getOutputStream();
			displayMessage("server: got stream");
			new ManageConnection(inputStream, outputStream, num_of_connection,
					socket.getRemoteSocketAddress().toString(), rootDir);
		} catch (IOException e) {

			e.printStackTrace();
		}

	}

	private void displayMessage(String str) {
		System.out.println(str);
		// txtField.setText(str);
		serverText.append(str + "\n");
	}

	private class ManageConnection implements Runnable {

		InputStream inputStream;
		OutputStream outputStream, fileOutputStream;
		int connection_num;
		DataInputStream dataInputStream;
		DataOutputStream dataOutputStream;
		boolean isConnectionOk = true;
		String ParentFolderPath = "";
		boolean isInitialFolder = true;
		String localRoot = "";
		String ip;

		Student student;

		public ManageConnection(InputStream IS, OutputStream Os, int num,
				String ip, String root) {
			inputStream = IS;
			outputStream = Os;
			connection_num = num;
			this.ip = ip;
			this.localRoot = root;
			new Thread(this).start();
		}

		@Override
		public void run() {
			try {
				handleIncomingData();
			} catch (EOFException e) {
				e.printStackTrace();
			}

		}

		private void handleIncomingData() throws EOFException {

			while (isConnectionOk) {
				dataInputStream = new DataInputStream(inputStream);
				displayMessage("waiting for file...");
				// isInitialFolder=true;
				int type;
				try {
					type = dataInputStream.readInt();
					if (type == Common.CONSTANT_FOLDER) {
						// displayMessage("0 Received in Server");
						handleDirectory();

					} else if (type == Common.CONSTANT_FILE) {
						// handleFile();
						handleFileAsChunk();
					} else if (type == Common.CONSTANT_FOLDER_COMPLETE) {
						ParentFolderPath = "";
						isInitialFolder = true;
					} else if (type == Common.CONSTANT_CHECK_VALIDITY) {
						responseValidity();
					} else if (type == Common.CONSTANT_REQUEST_CONSTRAINTS) {
						if (student.getFileSize() + 1 > serverConstraints
								.getFileNum()) {
							responseFileNumberOverFlow();
						} else
							responseConstraints();
					} else if (type == Common.CONSTANT_CONNECTION_EXIT) {
						isConnectionOk = false;
						num_of_connection -= 1;
					} else if (type == Common.CONSTANT_RESPONSE_FILE_BYTES) {
						saveChunk();
					} else if (type == 0) {
						count = count + 1;
					}
					displayMessage("type" + type + "count: " + count);
				} catch (IOException e) {
					displayMessage("exception");
					num_of_connection -= 1;
					e.printStackTrace();
					isConnectionOk = false;
				}

			}

		}

		private boolean checkDuplicate(int idX, String ipX) {
			if (!serverData.checkDuplicateID(idX, ipX)) {
				int x = JOptionPane.showConfirmDialog(Server.this,
						"Same Id with more than one ip. Accept?", "Warning",
						JOptionPane.YES_NO_OPTION);
				if (x == 0) {
					return true;
				} else
					return false;
			}
			if (!serverData.checkDuplicateIPAddress(idX, ipX)) {
				int x = JOptionPane.showConfirmDialog(Server.this,
						"Same Ip with more than one Student. Accept?",
						"Warning", JOptionPane.YES_NO_OPTION);
				if (x == 0) {
					return true;
				} else
					return false;
			}
			return true;

		}

		private void responseValidity() {
			dataOutputStream = new DataOutputStream(outputStream);
			try {
				int id = dataInputStream.readInt();
				if (checkDuplicate(id, ip)) {
					
					if (serverData.isStudentIdAvailable(id)) {
						dataOutputStream.writeInt(Common.CONSTANT_ID_OK);
						if (serverData.isStudentAlreadyConnected(id)) {
							displayMessage(id + "  connected before");
							student = serverData.getStudentById(id);
						} else {
							student = new Student(ip, id);
							serverData.addStudentInMap(student);
							displayMessage(id + "  new Student");

						}
						if (localRoot.equals("")) {
							File file = new File(id + "");
							file.mkdir();
						} else {
							File file = new File(localRoot, id + "");
							file.mkdir();

						}
					} else {
						dataOutputStream.writeInt(Common.CONSTANT_ID_INVALID);
						isConnectionOk = false;
					}
				}
				else
				{
					dataOutputStream.writeInt(Common.CONSTANT_RESPONSE_CONNECTION_REJECTED);
					isConnectionOk=false;
				}
				displayMessage("responsing");
			} catch (IOException e) {

				e.printStackTrace();
			}
		}

		private void responseConstraints() {
			// dataOutputStream = new DataOutputStream(outputStream);
			try {
				dataOutputStream.writeInt(Common.CONSTANT_REQUEST_CONSTRAINTS);
				dataOutputStream.writeUTF(serverConstraints.getFileTypes());
				// dataOutputStream.writeInt(serverConstraints.getFileNum());
				dataOutputStream.writeDouble(serverConstraints.getSizeMB());
				dataOutputStream.writeBoolean(serverConstraints
						.getCanUploadFolder());
				dataOutputStream.writeInt(serverConstraints.getFileNum());
				dataOutputStream.writeInt(student.getFileSize());
				dataOutputStream.writeUTF(serverConstraints.getFileNames());
			} catch (IOException e) {
				e.printStackTrace();
			}

		}

		private void responseFileNumberOverFlow() {
			dataOutputStream = new DataOutputStream(outputStream);

			try {

				dataOutputStream.writeInt(Common.CONSTANT_FILE_NUM_OVERFLOW);
			} catch (IOException e) {
				e.printStackTrace();
			}

		}

		private void responseFileBytesReceived() {
			dataOutputStream = new DataOutputStream(outputStream);
			try {

				dataOutputStream.writeInt(Common.CONSTANT_RESPONSE_FILE_BYTES);
			} catch (IOException e) {
				e.printStackTrace();
			}

		}

		private void handleFile() {
			try {
				int bytesRead;
				OutputStream output;
				String path = dataInputStream.readUTF();
				String fileName = dataInputStream.readUTF();
				String tempRootDir = "";
				if (localRoot.equals(""))
					tempRootDir = "";
				else
					tempRootDir = localRoot + "\\";
				if (ParentFolderPath.equals("")) {
					output = new FileOutputStream((tempRootDir
							+ student.getID() + "\\" + fileName));
				} else {
					String temp = determinePath(path);
					File file = new File(tempRootDir
							+ student.getID()
							+ "\\"
							+ temp.substring(0,
									temp.length() - fileName.length()),
							fileName);
					output = new FileOutputStream(file);
				}
				long size = dataInputStream.readLong();
				// displayMessage("size: " + size);
				// output = new FileOutputStream(("received_from_client_" +
				// fileName));
				byte[] buffer = new byte[1024];
				while (size > 0
						&& (bytesRead = dataInputStream.read(buffer, 0,
								(int) Math.min(buffer.length, size))) != -1) {
					output.write(buffer, 0, bytesRead);
					size -= bytesRead;
					// displayMessage("loop" + size);
				}
				// displayMessage("not loop");
				output.close();
				// clientData.close();
				/*
				 * try { Thread.sleep(500); } catch (InterruptedException e) {
				 * // TODO Auto-generated catch block e.printStackTrace(); }
				 */
				System.out.println("File " + fileName
						+ " received from clinet." + connection_num);
			} catch (IOException e) {
				displayMessage("exception");
				num_of_connection -= 1;
				e.printStackTrace();
				isConnectionOk = false;
			}
		}

		private void handleFileAsChunk() {
			try {
				String path = dataInputStream.readUTF();
				String fileName = dataInputStream.readUTF();
				student.addFile(fileName);
				String tempRootDir = "";
				if (localRoot.equals(""))
					tempRootDir = "";
				else
					tempRootDir = localRoot + "\\";
				if (ParentFolderPath.equals("")) {
					fileOutputStream = new FileOutputStream((tempRootDir
							+ student.getID() + "\\" + fileName));
				} else {
					String temp = determinePath(path);
					File file = new File(tempRootDir
							+ student.getID()
							+ "\\"
							+ temp.substring(0,
									temp.length() - fileName.length()),
							fileName);
					fileOutputStream = new FileOutputStream(file);
				}
				responseFileBytesReceived();
				/*
				 * long size = dataInputStream.readLong();
				 * displayMessage("size: " + size); // output = new
				 * FileOutputStream(("received_from_client_" + // fileName));
				 * byte[] buffer = new byte[1024]; while (size > 0 && (bytesRead
				 * = dataInputStream.read(buffer, 0, (int)
				 * Math.min(buffer.length, size))) != -1) { output.write(buffer,
				 * 0, bytesRead); size -= bytesRead; // displayMessage("loop" +
				 * size); } // displayMessage("not loop"); output.close(); //
				 * clientData.close(); /* try { Thread.sleep(500); } catch
				 * (InterruptedException e) { // TODO Auto-generated catch block
				 * e.printStackTrace(); }
				 */
				// System.out.println("File " + fileName +
				// " received from clinet." + connection_num);
			} catch (IOException e) {
				displayMessage("exception");
				num_of_connection -= 1;
				e.printStackTrace();
				isConnectionOk = false;
			}
		}

		private void saveChunk() {
			try {
				String filename = dataInputStream.readUTF();
				long offset = dataInputStream.readLong();
				int size = dataInputStream.readInt();
				displayMessage(filename + "     " + offset + "  " + size);
				byte[] fileData = new byte[CHUNK_SIZE];
				int bytesRead;
				while (size > 0
						&& (bytesRead = dataInputStream.read(fileData, 0,
								(int) Math.min(CHUNK_SIZE, size))) != -1) {
					fileOutputStream.write(fileData, 0, bytesRead);
					size -= bytesRead;
					// displayMessage("loop" + size);
				}
				responseFileBytesReceived();
			} catch (IOException e) {

				e.printStackTrace();
			}
		}

		private void handleDirectory() {

			try {
				String path = dataInputStream.readUTF();
				String tempRootDir = "";
				if (localRoot.equals(""))
					tempRootDir = "";
				else
					tempRootDir = localRoot + "\\";
				if (isInitialFolder) {
					String fileName = dataInputStream.readUTF();
					student.addFile(fileName);
					File file = new File(tempRootDir + student.getID() + "\\"
							+ ParentFolderPath, fileName);
					file.mkdir();
					ParentFolderPath = path.substring(0, path.length()
							- fileName.length());
					displayMessage(fileName);
					isInitialFolder = false;
				} else {
					String fileName = dataInputStream.readUTF();
					String temp = determinePath(path);
					File file = new File(tempRootDir
							+ student.getID()
							+ "\\"
							+ temp.substring(0,
									temp.length() - fileName.length()),
							fileName);
					file.mkdirs();
					displayMessage(fileName + "path: " + ParentFolderPath
							+ "  " + path + "  " + determinePath(path));
				}

				// displayMessage(parentpath+fileName);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			// File direc=new File(fil,)
		}

		private String determinePath(String absPath) {

			return absPath.substring(ParentFolderPath.length(),
					absPath.length());

		}

	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == btnConfigure) {
			serverConstraints.setDirectory(txtDirectory.getText().toString());
			serverConstraints.setFileTypes(txtFiletypes.getText().toString());
			serverConstraints.setStudents(txtStudentID.getText().toString());
			serverConstraints.setFileNames(txtFileNames.getText().toString());
			String temp = txtNum.getText().toString();
			if (!temp.equals(""))
				serverConstraints.setFileNum(Integer.valueOf(txtNum.getText()
						.toString()));
			temp = txtMaxSize.getText().toString();
			if (!temp.equals(""))
				serverConstraints.setSizeMB(Double.valueOf(txtMaxSize.getText()
						.toString()));
			String students = serverConstraints.getStudents();
			if (!students.equals("")) {
				serverData.initStudentList(students);
			}
			// if(!serverConstraints.getDirectory().equals(""))
			rootDir = serverConstraints.getDirectory();

			serverConstraints.setCanUploadFolder(chBoxFolder.isSelected());
			displayMessage(chBoxFolder.isSelected() + "");
		}
	}

}

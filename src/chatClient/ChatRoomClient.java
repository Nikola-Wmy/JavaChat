package chatClient;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.MulticastSocket;
import java.net.SocketAddress;
import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TitledPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;

class List extends Thread {
	//��������ϴ�������Ϣ�����շ������·��û��б�
	DatagramSocket socket_list;//����socket
	SocketAddress serverAddr_list;// ָ����������ַ

	public void run() {
		try {
			MulticastSocket socket = new MulticastSocket(7000);
			serverAddr_list = new InetSocketAddress("127.0.0.1", 9000);// ָ����������ַ
			socket_list = new DatagramSocket(0);//��������socket����ϵͳ�������˿�
			socket_list.connect(serverAddr_list);// �󶨷�������ַ��֮���͵�DatagramPacket��Ŀ�ĵ�ַ��Ϊ�õ�ַ
			InetAddress ia = InetAddress.getByName("225.0.1.2");//�鲥��ַ
			socket.joinGroup(ia);
			//���������Ϣ����ʽ("name|ip|port")
			String msg = "F" + "|" + ChatRoomClient.s.getLocalAddress().toString() + "|" + ChatRoomClient.s.getLocalPort();
			DatagramPacket pout = new DatagramPacket(msg.getBytes(), msg.getBytes().length, serverAddr_list);
			socket_list.send(pout);//�ϴ�������Ϣ
			while (true) {
				DatagramPacket packet = new DatagramPacket(new byte[512], 512);
				socket.receive(packet);//���շ������·����û��б���ʽ("name";"name")
				String str = new String(packet.getData(), 0, packet.getLength());
				String[] sou = str.split(";");
				ChatRoomClient.items.clear();
				//���������ַ������з�";"�ļ��붼�û��б���
				for (String s:sou) {
					if(!s.equals(";")) {
						ChatRoomClient.items.add(s);
					}
				}
			}
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}
	}
}
class MulticastClient extends Thread{
	//�����鲥��Ϣ
	public void run() {
		try (MulticastSocket socket = new MulticastSocket(4000)) {

			InetAddress ia = InetAddress.getByName("225.0.1.1");
			socket.joinGroup(ia);

			while (true) {
				DatagramPacket packet = new DatagramPacket(new byte[512], 512);
				socket.receive(packet);
				String msg = new String(packet.getData(), 0, packet.getLength());
				System.out.println(msg);
				if (msg.equals("bye")) {
					break;
				}
			}
			socket.leaveGroup(ia);
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}
	}
}

class PrivateClient extends Thread{
	//����˽������
	public void run() {
		try{
			while (true) {
				DatagramPacket pin = new DatagramPacket(new byte[512],512);
				ChatRoomClient.s.receive(pin);
				System.out.println(new String(pin.getData(),0, pin.getLength()));
			}
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}
	}
}

public class ChatRoomClient extends Application {
	TextArea ta_Input;//������Ϣ
	TextArea ta_Speaking;//�����
	Button btn_Send,btn_History,btn_Picture,btn_File;//���Ͱ�ť���鿴��ʷ��¼��ť������ͼƬ��ť�������ļ���ť
	GridPane ChatRoom,Send,Btn;//���������
	BorderPane IPpane;
	BorderPane box,box1,box2;//ƴ�Ͻ���
	ListView<String> IPlist; //��ʾ�����û�
	TitledPane pane_ip,pane_send;
	static DatagramSocket s;// ��ϵͳ�������һ�����ö˿ڣ�Ҳ����������һ�����Լ�IP��˿�
	SocketAddress serverAddr;//�����������ַ
	String type = "2"; //����Э�����/��Ϣ����
	MulticastClient multicastclient = new MulticastClient(); //�����鲥�߳�
	PrivateClient privateclient = new PrivateClient();//����udp�߳�
	List list = new List();
	static ObservableList<String> items;//�û��б�
	@Override
	
	public void start(Stage primaryStage) throws Exception {
		//�����������
		ChatRoom = new GridPane();//�������
		ChatRoom.setHgap(10);//���ÿ���
		ChatRoom.setVgap(10);//���ø߼��
		
		ta_Speaking = new TextArea("");//�����
		ta_Speaking.setPrefHeight(450);//�����߶�
		ta_Speaking.setPrefWidth(648);
		
		ChatRoom.add(ta_Speaking,0,0);//�������0��0��
		ChatRoom.setPrefSize(655, 455);//��������750��531
		
		//���÷��ͽ���
		Send = new GridPane();//�������
		Send.setHgap(10);
		btn_Send = new Button("Send");//���Ͱ�ť
		btn_Send.setOnAction(this::btnSendHandler);
		
		ta_Input = new TextArea("");//�����
		ta_Input.setPrefWidth(648);//�������
		ta_Input.setPrefHeight(10);//�����߶�
		
		Send.add(btn_Send,1,0);//���Ͱ�ť��1��1��
		Send.add(ta_Input,0,0);//�������0��1��
		
		//���ù��ܰ�ť����
		Btn = new GridPane();//��ť
		Btn.setVgap(10);
		Btn.setHgap(10);
		Btn.setPrefHeight(35);
		
		btn_History = new Button("History");//��ʷ��¼��ť
		btn_History.setOnAction(this::btnHistoryHandler);
		
		btn_Picture = new Button("Picture");//����ͼƬ��ť
		btn_Picture.setOnAction(this::btnPictureHandler);
		
		btn_File = new Button("File");//�����ļ���ť
		btn_File.setOnAction(this::btnFileHandler);
		
		Btn.add(btn_Picture,0,0);
		Btn.add(btn_File,1,0);
		Btn.add(btn_History,2,0);
		
		//�����û���������
		IPpane = new BorderPane();//�����û���������
		IPlist = new ListView<>();//�û������б�
		items =FXCollections.observableArrayList ();//�û���������
		IPlist.setItems(items);//��ʾ����
	    IPpane.setLeft(IPlist);//�б���ʾ�ڽ������
	    IPpane.setPrefWidth(100);//�û��б������
	    IPpane.setPrefHeight(561);//�û��б����߶�
	    
		
		//ƴ�Ͻ���
	    box1 = new BorderPane();
	    box1.setTop(ChatRoom);
	    box1.setCenter(Btn);
	    box1.setBottom(Send);
	    
	    
		pane_ip = new TitledPane("OnLine",IPpane);//�û��б������
		pane_ip.setCollapsible(false);//�շŹ���Ϊ��
		
		pane_send = new TitledPane("Chat",box1);//���������
		pane_send.setCollapsible(false);//�շŹ���Ϊ��
		
		box = new BorderPane();//һ��װ��������ĺ���
		box.setLeft(pane_ip);//������û��б�
		box.setRight(pane_send);//�ұ��������
		Scene scene = new Scene(box);
		primaryStage.setScene(scene);
		primaryStage.setTitle("ChatRoom");//�ܽ�����
		primaryStage.show();//��ʾ����

		s = new DatagramSocket(0);// ����socket����ϵͳ�������һ�����ö˿�
		serverAddr = new InetSocketAddress("127.0.0.1", 8000);// ָ����������ַ
		s.connect(serverAddr);//�󶨷�������ַ��֮���͵�DatagramPacket��Ŀ�ĵ�ַ��Ϊ�õ�ַ
		multicastclient.start();
		privateclient.start();
		list.start();
		System.out.println(s.getLocalAddress().toString()+"|"+s.getLocalPort());//����
	}

	public void btnSendHandler(ActionEvent event){
		try {
			String msg = ta_Input.getText();//��ȡ���������
			String msg_send = type + "|" + msg + "|" + "127.0.0.1|52331";//���������ַ���
			DatagramPacket pout = new DatagramPacket(msg_send.getBytes(), msg_send.getBytes().length, serverAddr);
			s.send(pout);//����DatagramPacket
		} catch (Exception e) {
			// TODO: handle exception
		}
		
	}
	
	public void btnHistoryHandler(ActionEvent event){
		
		
	}
	
	public void btnPictureHandler(ActionEvent event){
	
	
	}
	
	public void btnFileHandler(ActionEvent event){
	
	
	}
	
	
	
	public static void main(String[] args) {
		
		 Application.launch();
	}
}
